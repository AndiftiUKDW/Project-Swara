package com.swara.app.data.model

import android.net.Uri
import androidx.compose.runtime.Immutable

enum class Role {
    USER,
    ASSISTANT
}

enum class EmergencyCategory(
    val label: String,
    val guidanceLabel: String
) {
    MEDICAL("Medical", "medical emergency"),
    FIRE("Fire", "fire emergency"),
    FLOOD("Flood", "flood emergency"),
    EARTHQUAKE("Earthquake", "earthquake emergency"),
    VIOLENCE("Violence", "violence or personal safety emergency"),
    LOST("Lost", "lost or stranded emergency"),
    OTHER("Other", "general emergency")
}

enum class ResponseMode(
    val label: String,
    val helper: String
) {
    QUICK_HELP("Quick Help", "Short, immediate survival actions"),
    DETAILED_STEPS("Detailed Steps", "Longer guided steps with more context")
}

@Immutable
data class CitationRef(
    val documentId: String,
    val documentName: String,
    val pageNumber: Int? = null,
    val chunkId: String,
    val snippet: String = "",
    val score: Double = 0.0,
    val chunkIndex: Int? = null,
    val excerptCount: Int = 1,
    val matchedTerms: List<String> = emptyList()
)

@Immutable
data class ChatMessage(
    val id: String,
    val role: Role,
    val text: String,
    val citations: List<CitationRef> = emptyList(),
    val isStreaming: Boolean = false
)

sealed interface ModelState {
    data object NotInstalled : ModelState
    data object Validating : ModelState
    data object Loading : ModelState
    data class Ready(val modelPath: String) : ModelState
    data class Error(val message: String) : ModelState
}

sealed interface VoiceState {
    data object Idle : VoiceState
    data object Listening : VoiceState
    data object Transcribing : VoiceState
    data object Speaking : VoiceState
    data class Error(val message: String) : VoiceState
}

enum class DocumentStatus {
    IMPORTING,
    READY,
    ERROR
}

data class DocumentRecord(
    val id: String,
    val name: String,
    val mimeType: String,
    val importedAt: Long,
    val pageCount: Int? = null,
    val status: DocumentStatus
)

data class DocumentChunk(
    val id: String,
    val documentId: String,
    val pageNumber: Int? = null,
    val chunkIndex: Int,
    val text: String
)

data class RetrievalResult(
    val chunkId: String,
    val documentId: String,
    val documentName: String,
    val pageNumber: Int?,
    val chunkIndex: Int,
    val score: Double,
    val text: String,
    val snippet: String,
    val matchedTerms: List<String> = emptyList(),
    val matchedPhrases: List<String> = emptyList(),
    val titleMatch: Boolean = false,
    val neighborChunkIds: List<String> = emptyList(),
    val documentRank: Int = 0
)

data class IngestionSource(
    val uri: Uri,
    val displayName: String,
    val mimeType: String
)

data class AppSettings(
    val temperature: Float = 0.7f,
    val topK: Int = 20,
    val topP: Float = 0.9f,
    val maxContextChunks: Int = 4,
    val autoSpeakResponses: Boolean = true,
    val documentScope: Set<String> = emptySet(),
    val selectedCategory: EmergencyCategory = EmergencyCategory.OTHER,
    val responseMode: ResponseMode = ResponseMode.QUICK_HELP
)

data class ChatSession(
    val id: String,
    val title: String,
    val selectedCategory: EmergencyCategory = EmergencyCategory.OTHER,
    val summary: String = "",
    val createdAt: Long,
    val updatedAt: Long
)
