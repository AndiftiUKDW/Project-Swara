package com.swara.app.services

data class ParsedPage(
    val pageNumber: Int?,
    val text: String
)

data class ParsedDocument(
    val pageCount: Int?,
    val pages: List<ParsedPage>
)

data class ChunkDraft(
    val pageNumber: Int?,
    val text: String
)

data class SearchQuery(
    val normalizedInput: String,
    val terms: List<String>,
    val phrases: List<String>
)

class Chunker(
    private val chunkSize: Int = 900,
    private val overlap: Int = 180
) {
    private val stopWords = setOf(
        "about", "after", "again", "also", "among", "and", "are", "because", "been", "before",
        "being", "between", "both", "but", "can", "does", "from", "have", "into", "main",
        "many", "more", "most", "only", "other", "over", "same", "some", "such", "than",
        "that", "their", "them", "then", "there", "these", "they", "this", "those", "under",
        "using", "what", "when", "where", "which", "while", "with", "would", "your"
    )

    fun chunk(pages: List<ParsedPage>): List<ChunkDraft> {
        val chunks = mutableListOf<ChunkDraft>()
        pages.forEach { page ->
            val normalized = normalize(page.text)
            if (normalized.isBlank()) return@forEach
            var cursor = 0
            while (cursor < normalized.length) {
                val end = (cursor + chunkSize).coerceAtMost(normalized.length)
                val slice = normalized.substring(cursor, end).trim()
                if (slice.isNotBlank()) {
                    chunks += ChunkDraft(page.pageNumber, slice)
                }
                if (end >= normalized.length) {
                    break
                }
                cursor = (end - overlap).coerceAtLeast(cursor + 1)
            }
        }
        return chunks
    }

    fun searchTerms(input: String): SearchQuery {
        val normalizedInput = normalize(input)
        if (normalizedInput.isBlank()) {
            return SearchQuery("", emptyList(), emptyList())
        }

        val rawTokens = Regex("[A-Za-z0-9]+").findAll(normalizedInput)
            .map { it.value }
            .toList()

        val normalizedTokens = rawTokens.mapNotNull { token ->
            val normalized = normalizeToken(token)
            when {
                normalized.length < 3 -> null
                normalized in stopWords -> null
                else -> normalized
            }
        }

        val terms = linkedSetOf<String>()
        normalizedTokens.forEach { token ->
            terms += token
            val singular = singularize(token)
            if (singular.length >= 3 && singular !in stopWords) {
                terms += singular
            }
        }

        val phrases = linkedSetOf<String>()
        Regex("\"([^\"]+)\"").findAll(input).forEach { match ->
            val phrase = normalize(match.groupValues[1])
            if (phrase.split(" ").size >= 2) {
                phrases += phrase.lowercase()
            }
        }

        val meaningfulTokens = rawTokens.mapNotNull { token ->
            val normalized = normalizeToken(token)
            when {
                normalized.length < 3 -> null
                normalized in stopWords -> null
                else -> normalized
            }
        }
        for (windowSize in 2..3) {
            meaningfulTokens.windowed(windowSize).forEach { window ->
                val phrase = window.joinToString(" ")
                if (phrase.length >= 7) {
                    phrases += phrase
                }
            }
        }

        return SearchQuery(
            normalizedInput = normalizedInput,
            terms = terms.take(8),
            phrases = phrases.take(6)
        )
    }

    private fun normalize(text: String): String {
        return text.replace(Regex("\\s+"), " ").trim()
    }

    private fun normalizeToken(token: String): String {
        return token.lowercase().replace(Regex("[^a-z0-9]"), "")
    }

    private fun singularize(token: String): String {
        return when {
            token.endsWith("ies") && token.length > 4 -> token.dropLast(3) + "y"
            token.endsWith("es") && token.length > 4 && !token.endsWith("ses") -> token.dropLast(2)
            token.endsWith("s") && token.length > 3 && !token.endsWith("ss") && !token.endsWith("is") -> token.dropLast(1)
            else -> token
        }
    }
}
