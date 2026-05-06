package com.swara.app.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swara.app.AppContainer
import com.swara.app.data.model.AppSettings
import com.swara.app.data.model.CitationRef
import com.swara.app.data.model.ChatMessage
import com.swara.app.data.model.DocumentRecord
import com.swara.app.data.model.EmergencyCategory
import com.swara.app.data.model.ModelState
import com.swara.app.data.model.RetrievalResult
import com.swara.app.data.model.ResponseMode
import com.swara.app.data.model.Role
import com.swara.app.data.model.VoiceState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class MainUiState(
    val messages: List<ChatMessage> = emptyList(),
    val draft: String = "",
    val documents: List<DocumentRecord> = emptyList(),
    val evidenceByMessage: Map<String, List<RetrievalResult>> = emptyMap(),
    val modelState: ModelState = ModelState.NotInstalled,
    val voiceState: VoiceState = VoiceState.Idle,
    val settings: AppSettings = AppSettings(),
    val isBusy: Boolean = false,
    val statusMessage: String? = null
)

class MainViewModel(
    private val container: AppContainer
) : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val _draft = MutableStateFlow("")
    private val _settings = MutableStateFlow(AppSettings())
    private val _busy = MutableStateFlow(false)
    private val _status = MutableStateFlow<String?>(null)
    private val _evidenceByMessage = MutableStateFlow<Map<String, List<RetrievalResult>>>(emptyMap())
    private val _events = MutableSharedFlow<UiEvent>()

    val events = _events.asSharedFlow()

    val uiState: StateFlow<MainUiState> = combine(
        _messages,
        _draft,
        container.documentRepository.documents,
        _evidenceByMessage,
        container.modelManager.state
    ) { messages: List<ChatMessage>,
        draft: String,
        documents: List<DocumentRecord>,
        evidenceByMessage: Map<String, List<RetrievalResult>>,
        modelState: ModelState ->
        UiInputs(
            messages = messages,
            draft = draft,
            documents = documents,
            evidenceByMessage = evidenceByMessage,
            modelState = modelState
        )
    }.combine(container.voiceController.state) { inputs, voiceState ->
        inputs.copy(voiceState = voiceState)
    }.combine(_settings) { inputs, settings ->
        inputs.copy(settings = settings)
    }.combine(_busy) { inputs, busy ->
        inputs.copy(busy = busy)
    }.combine(_status) { inputs, status ->
        inputs.copy(status = status)
    }.map { inputs ->
        MainUiState(
            messages = inputs.messages,
            draft = inputs.draft,
            documents = inputs.documents,
            evidenceByMessage = inputs.evidenceByMessage,
            modelState = inputs.modelState,
            voiceState = inputs.voiceState,
            settings = inputs.settings,
            isBusy = inputs.busy,
            statusMessage = inputs.status
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())

    fun updateDraft(value: String) {
        _draft.value = value
    }

    fun requestModelPicker() {
        viewModelScope.launch { _events.emit(UiEvent.PickModel) }
    }

    fun requestDocumentPicker() {
        viewModelScope.launch { _events.emit(UiEvent.PickDocuments) }
    }

    fun importModel(uri: Uri) {
        viewModelScope.launch {
            _status.value = "Importing offline Gemma model..."
            container.modelManager.importModel(uri)
            container.chatService.warmUpIfPossible()
            _status.value = null
        }
    }

    fun importDocuments(uris: List<Uri>) {
        viewModelScope.launch {
            _status.value = "Indexing survival packs..."
            container.documentRepository.importDocuments(uris)
            _status.value = null
        }
    }

    fun sendDraft() {
        val question = _draft.value.trim()
        if (question.isEmpty() || _busy.value) return
        _draft.value = ""
        askQuestion(question)
    }

    fun startVoiceInput(hasPermission: Boolean) {
        if (!hasPermission) {
            viewModelScope.launch { _events.emit(UiEvent.RequestAudioPermission) }
            return
        }
        viewModelScope.launch {
            container.voiceController.listen().collect { transcript ->
                if (transcript.isNotBlank()) {
                    askQuestion(transcript)
                }
            }
        }
    }

    fun onAudioPermissionResult(granted: Boolean) {
        _status.value = if (granted) {
            "Microphone permission granted. Tap mic again."
        } else {
            "Microphone permission denied. Text guidance still works."
        }
    }

    fun stopSpeaking() {
        container.voiceController.stopSpeaking()
    }

    fun speakMessage(message: ChatMessage) {
        container.voiceController.speak(message.text)
    }

    fun toggleDocumentScope(documentId: String) {
        _settings.value = _settings.value.run {
            val updated = documentScope.toMutableSet()
            if (!updated.add(documentId)) {
                updated.remove(documentId)
            }
            copy(documentScope = updated)
        }
    }

    fun setAutoSpeak(enabled: Boolean) {
        _settings.value = _settings.value.copy(autoSpeakResponses = enabled)
    }

    fun setMaxChunks(value: Int) {
        _settings.value = _settings.value.copy(maxContextChunks = value)
    }

    fun setEmergencyCategory(category: EmergencyCategory) {
        _settings.value = _settings.value.copy(selectedCategory = category)
    }

    fun setResponseMode(mode: ResponseMode) {
        _settings.value = _settings.value.copy(responseMode = mode)
    }

    fun deleteDocument(documentId: String) {
        viewModelScope.launch {
            container.documentRepository.deleteDocument(documentId)
            _settings.value = _settings.value.copy(documentScope = _settings.value.documentScope - documentId)
        }
    }

    private fun askQuestion(question: String) {
        val activeSettings = _settings.value
        val effectiveQuestion = buildOperationalQuestion(question, activeSettings)
        val previousMessages = _messages.value
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = Role.USER,
            text = question
        )
        _messages.value = _messages.value + userMessage
        viewModelScope.launch {
            _busy.value = true
            val retrieval = container.documentRepository.search(
                question = effectiveQuestion,
                documentScope = activeSettings.documentScope,
                limit = activeSettings.maxContextChunks
            )
            val citationSummaries = buildCitationSummaries(retrieval)
            val streamingId = UUID.randomUUID().toString()
            _evidenceByMessage.value = _evidenceByMessage.value + (streamingId to retrieval)
            _messages.value = _messages.value + ChatMessage(
                id = streamingId,
                role = Role.ASSISTANT,
                text = "",
                citations = citationSummaries,
                isStreaming = true
            )
            runCatching {
                container.chatService.streamReply(
                    history = previousMessages,
                    question = effectiveQuestion,
                    retrieval = retrieval,
                    settings = activeSettings
                ).collect { message ->
                    _messages.value = _messages.value.map {
                        if (it.id == streamingId) {
                            it.copy(
                                text = mergeStreamText(it.text, message.text),
                                citations = citationSummaries,
                                isStreaming = true
                            )
                        } else {
                            it
                        }
                    }
                }
            }.onSuccess {
                _messages.value = _messages.value.map {
                    if (it.id == streamingId) it.copy(isStreaming = false) else it
                }
                val finalMessage = _messages.value.firstOrNull { it.id == streamingId }
                if (activeSettings.autoSpeakResponses && !finalMessage?.text.isNullOrBlank()) {
                    container.voiceController.speak(finalMessage!!.text)
                }
                _status.value = if (retrieval.isEmpty()) {
                    "No supporting survival-pack chunks found. Swara will answer from general emergency guidance."
                } else {
                    null
                }
            }.onFailure { throwable ->
                _messages.value = _messages.value.map {
                    if (it.id == streamingId) {
                        it.copy(
                            text = throwable.message ?: "Generation failed",
                            isStreaming = false
                        )
                    } else {
                        it
                    }
                }
                _status.value = throwable.message
            }
            _busy.value = false
        }
    }

    sealed interface UiEvent {
        data object PickModel : UiEvent
        data object PickDocuments : UiEvent
        data object RequestAudioPermission : UiEvent
    }

    class Factory(
        private val container: AppContainer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(container) as T
        }
    }

    private data class UiInputs(
        val messages: List<ChatMessage>,
        val draft: String,
        val documents: List<DocumentRecord>,
        val evidenceByMessage: Map<String, List<RetrievalResult>>,
        val modelState: ModelState,
        val voiceState: VoiceState = VoiceState.Idle,
        val settings: AppSettings = AppSettings(),
        val busy: Boolean = false,
        val status: String? = null
    )

    private fun mergeStreamText(current: String, incoming: String): String {
        if (incoming.isBlank()) return current
        if (current.isBlank()) return incoming
        return when {
            incoming.startsWith(current) -> incoming
            current.endsWith(incoming) -> current
            else -> current + incoming
        }
    }

    private fun buildOperationalQuestion(
        question: String,
        settings: AppSettings
    ): String {
        val modeInstruction = when (settings.responseMode) {
            ResponseMode.QUICK_HELP -> "Quick Help: short, immediate survival actions."
            ResponseMode.DETAILED_STEPS -> "Detailed Steps: more complete guidance with practical checks."
        }
        return """
            Emergency category: ${settings.selectedCategory.guidanceLabel}
            Response mode: $modeInstruction
            User situation:
            ${question.trim()}
        """.trimIndent()
    }

    private fun buildCitationSummaries(retrieval: List<RetrievalResult>): List<CitationRef> {
        return retrieval
            .groupBy { "${it.documentId}:${it.pageNumber ?: -1}" }
            .values
            .map { group ->
                val primary = group.maxByOrNull { it.score } ?: return@map null
                CitationRef(
                    documentId = primary.documentId,
                    documentName = primary.documentName,
                    pageNumber = primary.pageNumber,
                    chunkId = primary.chunkId,
                    snippet = primary.snippet,
                    score = primary.score,
                    chunkIndex = primary.chunkIndex,
                    excerptCount = group.size,
                    matchedTerms = group.flatMap { it.matchedTerms }.distinct()
                )
            }
            .filterNotNull()
            .sortedByDescending { it.score }
    }
}
