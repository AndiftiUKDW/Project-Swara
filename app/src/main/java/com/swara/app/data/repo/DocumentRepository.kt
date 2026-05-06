package com.swara.app.data.repo

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import com.swara.app.data.model.DocumentRecord
import com.swara.app.data.model.DocumentStatus
import com.swara.app.data.model.DocumentChunk
import com.swara.app.data.model.IngestionSource
import com.swara.app.data.model.RetrievalResult
import com.swara.app.services.Chunker
import com.swara.app.services.DocumentParser
import com.swara.app.services.SearchQuery
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.min
import java.util.UUID

class DocumentRepository(
    private val context: Context,
    private val parser: DocumentParser,
    private val chunker: Chunker
) {
    private val gson = Gson()
    private val indexFile = File(context.filesDir, "document-index.json")
    private val _documents = MutableStateFlow<List<DocumentRecord>>(emptyList())
    private val chunksByDocument = linkedMapOf<String, List<DocumentChunk>>()
    val documents: Flow<List<DocumentRecord>> = _documents.asStateFlow()

    init {
        loadIndex()
    }

    suspend fun importDocuments(uris: List<Uri>) = withContext(Dispatchers.IO) {
        uris.forEach { importDocument(it) }
    }

    suspend fun deleteDocument(documentId: String) = withContext(Dispatchers.IO) {
        chunksByDocument.remove(documentId)
        _documents.value = _documents.value.filterNot { it.id == documentId }
        saveIndex()
    }

    suspend fun search(question: String, documentScope: Set<String>, limit: Int): List<RetrievalResult> =
        withContext(Dispatchers.IO) {
            val query = chunker.searchTerms(question)
            if (query.terms.isEmpty() && query.phrases.isEmpty()) {
                return@withContext emptyList()
            }
            val allowedDocuments = if (documentScope.isEmpty()) {
                _documents.value.associateBy { it.id }
            } else {
                _documents.value.filter { it.id in documentScope }.associateBy { it.id }
            }
            val candidates = chunksByDocument
                .asSequence()
                .filter { (documentId, _) -> documentId in allowedDocuments }
                .flatMap { (documentId, chunks) ->
                    val document = allowedDocuments.getValue(documentId)
                    chunks.asSequence().mapNotNull { chunk ->
                        val score = scoreChunk(chunk, document.name, query) ?: return@mapNotNull null
                        ScoredChunk(
                            chunk = chunk,
                            documentName = document.name,
                            score = score
                        )
                    }
                }
                .sortedWith(
                    compareByDescending<ScoredChunk> { it.score.total }
                        .thenBy { it.chunk.pageNumber ?: Int.MAX_VALUE }
                        .thenBy { it.chunk.chunkIndex }
                )
                .toList()

            val selected = selectDiverseCandidates(candidates, limit)
            selected.mapIndexed { index, candidate ->
                buildRetrievalResult(
                    candidate = candidate,
                    documentRank = index + 1
                )
            }
        }

    private suspend fun importDocument(uri: Uri) {
        val source = resolveSource(uri)
        val id = UUID.randomUUID().toString()
        upsertDocument(
            DocumentRecord(
                id = id,
                name = source.displayName,
                mimeType = source.mimeType,
                importedAt = System.currentTimeMillis(),
                pageCount = null,
                status = DocumentStatus.IMPORTING
            )
        )
        runCatching {
            val parsed = parser.parse(source)
            val chunks = chunker.chunk(parsed.pages).mapIndexed { index, chunk ->
                DocumentChunk(
                    id = UUID.randomUUID().toString(),
                    documentId = id,
                    pageNumber = chunk.pageNumber,
                    chunkIndex = index,
                    text = chunk.text
                )
            }
            chunksByDocument[id] = chunks
            upsertDocument(
                DocumentRecord(
                    id = id,
                    name = source.displayName,
                    mimeType = source.mimeType,
                    importedAt = System.currentTimeMillis(),
                    pageCount = parsed.pageCount,
                    status = DocumentStatus.READY
                )
            )
            saveIndex()
        }.onFailure { throwable ->
            upsertDocument(
                DocumentRecord(
                    id = id,
                    name = source.displayName,
                    mimeType = source.mimeType,
                    importedAt = System.currentTimeMillis(),
                    pageCount = null,
                    status = DocumentStatus.ERROR
                )
            )
            chunksByDocument.remove(id)
            saveIndex()
        }
    }

    private fun upsertDocument(document: DocumentRecord) {
        _documents.value = (_documents.value.filterNot { it.id == document.id } + document)
            .sortedByDescending { it.importedAt }
    }

    private fun scoreChunk(
        chunk: DocumentChunk,
        documentName: String,
        query: SearchQuery
    ): ChunkScore? {
        val chunkText = chunk.text
        val haystack = normalizeForSearch(chunkText)
        val tokenPositions = tokenPositions(chunkText)
        val matchedTerms = linkedSetOf<String>()
        var score = 0.0

        query.terms.forEach { term ->
            val positions = tokenPositions[term].orEmpty()
            if (positions.isNotEmpty()) {
                matchedTerms += term
                score += positions.size * 1.4
            }
        }

        val matchedPhrases = query.phrases.filter { phrase ->
            haystack.contains(phrase)
        }
        score += matchedPhrases.size * 3.5

        val normalizedDocumentName = normalizeForSearch(documentName)
        val titleMatch = query.terms.any { normalizedDocumentName.contains(it) } ||
            matchedPhrases.any { normalizedDocumentName.contains(it) }
        if (titleMatch) {
            score += 2.25
        }

        val proximityWindow = smallestWindow(
            tokenPositions = tokenPositions,
            queryTerms = matchedTerms.toList()
        )
        if (proximityWindow != null) {
            score += when {
                proximityWindow <= 3 -> 3.0
                proximityWindow <= 6 -> 1.75
                proximityWindow <= 10 -> 0.75
                else -> 0.0
            }
        }

        score += when (chunk.pageNumber) {
            1, 2 -> 0.8
            3, 4, 5 -> 0.35
            else -> 0.0
        }

        if (score <= 0.0) {
            return null
        }

        return ChunkScore(
            total = score,
            matchedTerms = matchedTerms.toList(),
            matchedPhrases = matchedPhrases,
            titleMatch = titleMatch
        )
    }

    private fun selectDiverseCandidates(
        candidates: List<ScoredChunk>,
        limit: Int
    ): List<ScoredChunk> {
        if (limit <= 0) return emptyList()

        val selected = mutableListOf<ScoredChunk>()
        val selectedIds = mutableSetOf<String>()
        val pageCounts = mutableMapOf<String, Int>()
        val documentCounts = mutableMapOf<String, Int>()

        fun accept(candidate: ScoredChunk, strict: Boolean): Boolean {
            if (!selectedIds.add(candidate.chunk.id)) {
                return false
            }
            val pageKey = pageKey(candidate.chunk)
            val pageCount = pageCounts[pageKey] ?: 0
            val documentCount = documentCounts[candidate.chunk.documentId] ?: 0
            val allowed = if (strict) {
                pageCount == 0 && documentCount < 2
            } else {
                pageCount < 2
            }
            if (!allowed) {
                selectedIds.remove(candidate.chunk.id)
                return false
            }
            selected += candidate
            pageCounts[pageKey] = pageCount + 1
            documentCounts[candidate.chunk.documentId] = documentCount + 1
            return true
        }

        candidates.forEach { candidate ->
            if (selected.size >= limit) return@forEach
            accept(candidate, strict = true)
        }
        candidates.forEach { candidate ->
            if (selected.size >= limit) return@forEach
            accept(candidate, strict = false)
        }
        return selected
    }

    private fun buildRetrievalResult(
        candidate: ScoredChunk,
        documentRank: Int
    ): RetrievalResult {
        val chunkList = chunksByDocument[candidate.chunk.documentId].orEmpty()
        val centerIndex = chunkList.indexOfFirst { it.id == candidate.chunk.id }
        val neighbors = buildList {
            if (centerIndex > 0) {
                val previous = chunkList[centerIndex - 1]
                if (isNeighbor(candidate.chunk, previous)) add(previous)
            }
            add(candidate.chunk)
            if (centerIndex >= 0 && centerIndex < chunkList.lastIndex) {
                val next = chunkList[centerIndex + 1]
                if (isNeighbor(candidate.chunk, next)) add(next)
            }
        }

        val mergedText = neighbors.joinToString("\n\n") { it.text.trim() }.trim()
        return RetrievalResult(
            chunkId = candidate.chunk.id,
            documentId = candidate.chunk.documentId,
            documentName = candidate.documentName,
            pageNumber = candidate.chunk.pageNumber,
            chunkIndex = candidate.chunk.chunkIndex,
            score = candidate.score.total,
            text = mergedText,
            snippet = buildSnippet(
                text = mergedText,
                matchedTerms = candidate.score.matchedTerms,
                matchedPhrases = candidate.score.matchedPhrases
            ),
            matchedTerms = candidate.score.matchedTerms,
            matchedPhrases = candidate.score.matchedPhrases,
            titleMatch = candidate.score.titleMatch,
            neighborChunkIds = neighbors.map { it.id }.filterNot { it == candidate.chunk.id },
            documentRank = documentRank
        )
    }

    private fun isNeighbor(anchor: DocumentChunk, candidate: DocumentChunk): Boolean {
        if (anchor.documentId != candidate.documentId) return false
        if (kotlin.math.abs(anchor.chunkIndex - candidate.chunkIndex) != 1) return false
        return anchor.pageNumber == null || candidate.pageNumber == null || anchor.pageNumber == candidate.pageNumber
    }

    private fun pageKey(chunk: DocumentChunk): String {
        return "${chunk.documentId}:${chunk.pageNumber ?: -1}"
    }

    private fun normalizeForSearch(text: String): String {
        return text.lowercase().replace(Regex("\\s+"), " ").trim()
    }

    private fun tokenPositions(text: String): Map<String, List<Int>> {
        val tokens = Regex("[A-Za-z0-9]+").findAll(text.lowercase()).map { it.value }.toList()
        val positions = linkedMapOf<String, MutableList<Int>>()
        tokens.forEachIndexed { index, token ->
            positions.getOrPut(token) { mutableListOf() }.add(index)
            val singular = singularize(token)
            if (singular != token) {
                positions.getOrPut(singular) { mutableListOf() }.add(index)
            }
        }
        return positions
    }

    private fun smallestWindow(
        tokenPositions: Map<String, List<Int>>,
        queryTerms: List<String>
    ): Int? {
        if (queryTerms.size < 2) return null
        val points = queryTerms.mapNotNull { term ->
            tokenPositions[term]?.firstOrNull()
        }
        if (points.size < 2) return null
        return (points.maxOrNull() ?: return null) - (points.minOrNull() ?: return null) + 1
    }

    private fun buildSnippet(
        text: String,
        matchedTerms: List<String>,
        matchedPhrases: List<String>,
        maxLength: Int = 220
    ): String {
        if (text.isBlank()) return ""
        val lowercaseText = text.lowercase()
        val anchors = matchedPhrases + matchedTerms
        val firstHit = anchors.mapNotNull { anchor ->
            val index = lowercaseText.indexOf(anchor.lowercase())
            if (index >= 0) index else null
        }.minOrNull() ?: 0

        val start = max(0, firstHit - 60)
        val end = min(text.length, start + maxLength)
        val prefix = if (start > 0) "..." else ""
        val suffix = if (end < text.length) "..." else ""
        return prefix + text.substring(start, end).trim() + suffix
    }

    private fun singularize(token: String): String {
        return when {
            token.endsWith("ies") && token.length > 4 -> token.dropLast(3) + "y"
            token.endsWith("es") && token.length > 4 && !token.endsWith("ses") -> token.dropLast(2)
            token.endsWith("s") && token.length > 3 && !token.endsWith("ss") && !token.endsWith("is") -> token.dropLast(1)
            else -> token
        }
    }

    private fun loadIndex() {
        if (!indexFile.exists()) return
        runCatching {
            val payload: PersistedIndex = gson.fromJson(indexFile.readText(), PersistedIndex::class.java)
            _documents.value = payload.documents.sortedByDescending { it.importedAt }
            chunksByDocument.clear()
            payload.chunks.groupBy { it.documentId }.forEach { (documentId, chunks) ->
                chunksByDocument[documentId] = chunks.sortedBy { it.chunkIndex }
            }
        }
    }

    private fun saveIndex() {
        val payload = PersistedIndex(
            documents = _documents.value,
            chunks = chunksByDocument.values.flatten()
        )
        indexFile.parentFile?.mkdirs()
        indexFile.writeText(gson.toJson(payload))
    }

    private fun resolveSource(uri: Uri): IngestionSource {
        val documentFile = DocumentFile.fromSingleUri(context, uri)
        val displayName = documentFile?.name ?: queryDisplayName(uri) ?: "document"
        val mimeType = documentFile?.type ?: context.contentResolver.getType(uri) ?: "application/octet-stream"
        return IngestionSource(uri, displayName, mimeType)
    }

    private fun queryDisplayName(uri: Uri): String? {
        return context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(0)
                } else {
                    null
                }
            }
    }

    private data class PersistedIndex(
        val documents: List<DocumentRecord> = emptyList(),
        val chunks: List<DocumentChunk> = emptyList()
    )

    private data class ChunkScore(
        val total: Double,
        val matchedTerms: List<String>,
        val matchedPhrases: List<String>,
        val titleMatch: Boolean
    )

    private data class ScoredChunk(
        val chunk: DocumentChunk,
        val documentName: String,
        val score: ChunkScore
    )
}
