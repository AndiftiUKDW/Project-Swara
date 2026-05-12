package com.swara.app.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swara.app.AppContainer
import com.swara.app.data.model.AppSettings
import com.swara.app.data.model.ChatMessage
import com.swara.app.data.model.ChatSession
import com.swara.app.data.model.CitationRef
import com.swara.app.data.model.DocumentRecord
import com.swara.app.data.model.EmergencyCategory
import com.swara.app.data.model.GuideCatalogItem
import com.swara.app.data.model.ModelState
import com.swara.app.data.model.ResponseMode
import com.swara.app.data.model.RetrievalResult
import com.swara.app.data.model.Role
import com.swara.app.data.model.SurvivalPackGuide
import com.swara.app.data.model.VoiceState
import com.swara.app.data.repo.SurvivalPackMetadata
import com.swara.app.services.DistributionServerState
import com.swara.app.services.WebHostServerState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

data class MainUiState(
    val messages: List<ChatMessage> = emptyList(),
    val draft: String = "",
    val documents: List<DocumentRecord> = emptyList(),
    val evidenceByMessage: Map<String, List<RetrievalResult>> = emptyMap(),
    val modelState: ModelState = ModelState.NotInstalled,
    val voiceState: VoiceState = VoiceState.Idle,
    val settings: AppSettings = AppSettings(),
    val survivalPacks: List<SurvivalPackGuide> = emptyList(),
    val survivalPackMetadata: SurvivalPackMetadata = SurvivalPackMetadata(),
    val marketplaceItems: List<GuideCatalogItem> = emptyList(),
    val installedGuideModuleIds: Set<String> = emptySet(),
    val sessions: List<ChatSession> = emptyList(),
    val activeSessionId: String = "",
    val activeSessionSummary: String = "",
    val lockedGuideCategory: EmergencyCategory? = null,
    val distributionServerState: DistributionServerState = DistributionServerState(),
    val webHostServerState: WebHostServerState = WebHostServerState(),
    val isBusy: Boolean = false,
    val statusMessage: String? = null
)

class MainViewModel(
    private val container: AppContainer
) : ViewModel() {
    private val initialSession = createSession()
    private val _activeSession = MutableStateFlow(
        container.chatSessionRepository.sessions.value.firstOrNull() ?: initialSession
    )
    private val _messages = MutableStateFlow(
        sanitizeMessages(container.chatSessionRepository.messagesFor(_activeSession.value.id))
    )
    private val _draft = MutableStateFlow("")
    private val _settings = MutableStateFlow(
        AppSettings(selectedCategory = _activeSession.value.selectedCategory)
    )
    private val _lockedGuideCategory = MutableStateFlow<EmergencyCategory?>(null)
    private val _busy = MutableStateFlow(false)
    private val _status = MutableStateFlow<String?>(null)
    private val _distributionServerState = MutableStateFlow(DistributionServerState())
    private val _webHostServerState = MutableStateFlow(WebHostServerState())
    private val _evidenceByMessage = MutableStateFlow<Map<String, List<RetrievalResult>>>(emptyMap())
    private val _events = MutableSharedFlow<UiEvent>()

    init {
        container.chatSessionRepository.upsertSession(_activeSession.value)
    }

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
    }.combine(_distributionServerState) { inputs, distributionServerState ->
        inputs.copy(distributionServerState = distributionServerState)
    }.combine(_webHostServerState) { inputs, webHostServerState ->
        inputs.copy(webHostServerState = webHostServerState)
    }.combine(container.chatSessionRepository.sessions) { inputs, sessions ->
        inputs.copy(sessions = sessions)
    }.combine(_activeSession) { inputs, session ->
        inputs.copy(activeSession = session)
    }.combine(_lockedGuideCategory) { inputs, lockedGuideCategory ->
        inputs.copy(lockedGuideCategory = lockedGuideCategory)
    }.combine(container.survivalPackRepository.installedModules) { inputs, _ ->
        inputs
    }.map { inputs ->
        MainUiState(
            messages = inputs.messages,
            draft = inputs.draft,
            documents = inputs.documents,
            evidenceByMessage = inputs.evidenceByMessage,
            modelState = inputs.modelState,
            voiceState = inputs.voiceState,
            settings = inputs.settings,
            survivalPacks = container.survivalPackRepository.allPacks(),
            survivalPackMetadata = container.survivalPackRepository.metadata(),
            marketplaceItems = container.survivalPackRepository.marketplaceCatalog(),
            installedGuideModuleIds = container.survivalPackRepository.installedModules.value.map { it.id }.toSet(),
            sessions = inputs.sessions,
            activeSessionId = inputs.activeSession.id,
            activeSessionSummary = inputs.activeSession.summary,
            lockedGuideCategory = inputs.lockedGuideCategory,
            distributionServerState = inputs.distributionServerState,
            webHostServerState = inputs.webHostServerState,
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

    fun requestModelDownload() {
        _status.value = "Model download source is not configured yet. Use Import manually or local sharing."
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
            _status.value = "Indexing guide files..."
            container.documentRepository.importDocuments(uris)
            _status.value = null
        }
    }

    fun installMarketplaceGuide(itemId: String) {
        val result = container.survivalPackRepository.installMarketplaceGuide(itemId)
        _status.value = result.exceptionOrNull()?.message
    }

    fun newChat() {
        persistActiveSession()
        val session = createSession()
        _activeSession.value = session
        _messages.value = emptyList()
        _evidenceByMessage.value = emptyMap()
        _lockedGuideCategory.value = null
        _settings.value = _settings.value.copy(selectedCategory = EmergencyCategory.OTHER)
        container.chatSessionRepository.upsertSession(session)
    }

    fun selectSession(sessionId: String) {
        persistActiveSession()
        val session = container.chatSessionRepository.sessions.value.firstOrNull { it.id == sessionId } ?: return
        _activeSession.value = session
        _messages.value = sanitizeMessages(container.chatSessionRepository.messagesFor(session.id))
        _evidenceByMessage.value = emptyMap()
        _lockedGuideCategory.value = null
        _settings.value = _settings.value.copy(selectedCategory = session.selectedCategory)
    }

    fun deleteSession(sessionId: String) {
        container.chatSessionRepository.deleteSession(sessionId)
        if (_activeSession.value.id == sessionId) {
            newChat()
        }
    }

    fun sendDraft() {
        val question = _draft.value.trim()
        if (question.isEmpty() || _busy.value) return
        _draft.value = ""
        askQuestion(question)
    }

    fun askAboutGuide(guide: SurvivalPackGuide) {
        _lockedGuideCategory.value = guide.category
        _settings.value = _settings.value.copy(selectedCategory = guide.category)
        _status.value = null
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

    fun startDistributionServer() {
        _distributionServerState.value = container.distributionServer.start()
    }

    fun stopDistributionServer() {
        _distributionServerState.value = container.distributionServer.stop()
    }

    fun startWebHostServer() {
        _webHostServerState.value = container.webHostServer.start()
    }

    fun stopWebHostServer() {
        _webHostServerState.value = container.webHostServer.stop()
    }

    private fun askQuestion(question: String) {
        val activeCategory = _lockedGuideCategory.value ?: inferCategory(question)
        _settings.value = _settings.value.copy(selectedCategory = activeCategory)
        val activeSettings = _settings.value
        val sessionBefore = _activeSession.value
        val previousMessages = _messages.value
        val summary = buildSessionSummary(previousMessages)
        val effectiveQuestion = buildOperationalQuestion(
            question = question,
            settings = activeSettings,
            summary = summary,
            isFollowUp = previousMessages.any { it.role == Role.USER }
        )
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = Role.USER,
            text = question
        )
        _messages.value = previousMessages + userMessage
        persistActiveSession()
        viewModelScope.launch {
            _busy.value = true
            val retrieval = container.documentRepository.search(
                question = effectiveQuestion,
                documentScope = activeSettings.documentScope,
                limit = activeSettings.maxContextChunks
            )
            val survivalPack = container.survivalPackRepository.findFor(activeCategory)
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
                    history = previousMessages.takeLast(6),
                    question = effectiveQuestion,
                    retrieval = retrieval,
                    survivalPack = survivalPack,
                    settings = activeSettings.copy(selectedCategory = activeCategory)
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
                val updated = sessionBefore.copy(
                    title = buildSessionTitle(_messages.value),
                    selectedCategory = activeCategory,
                    summary = buildSessionSummary(_messages.value),
                    updatedAt = System.currentTimeMillis()
                )
                _activeSession.value = updated
                container.chatSessionRepository.upsertSession(updated)
                container.chatSessionRepository.saveMessages(updated.id, _messages.value)
                if (activeSettings.autoSpeakResponses && !finalMessage?.text.isNullOrBlank()) {
                    container.voiceController.speak(finalMessage!!.text)
                }
                _status.value = if (retrieval.isEmpty()) null else "Using local guide context."
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
                persistActiveSession()
                _status.value = throwable.message
            }
            _busy.value = false
        }
    }

    private fun persistActiveSession() {
        val messages = _messages.value
        val session = _activeSession.value.copy(
            title = buildSessionTitle(messages),
            selectedCategory = _settings.value.selectedCategory,
            summary = buildSessionSummary(messages),
            updatedAt = System.currentTimeMillis()
        )
        _activeSession.value = session
        container.chatSessionRepository.upsertSession(session)
        container.chatSessionRepository.saveMessages(session.id, messages)
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
        val distributionServerState: DistributionServerState = DistributionServerState(),
        val webHostServerState: WebHostServerState = WebHostServerState(),
        val sessions: List<ChatSession> = emptyList(),
        val activeSession: ChatSession = createSession(),
        val lockedGuideCategory: EmergencyCategory? = null,
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
        settings: AppSettings,
        summary: String,
        isFollowUp: Boolean
    ): String {
        val modeInstruction = when (settings.responseMode) {
            ResponseMode.QUICK_HELP -> "Quick Help: short, immediate survival actions."
            ResponseMode.DETAILED_STEPS -> "Detailed Steps: more complete guidance with practical checks."
        }
        return """
            Session memory:
            ${summary.ifBlank { "No earlier details." }}

            Emergency category:
            ${settings.selectedCategory.guidanceLabel}

            Response mode:
            $modeInstruction

            Conversation behavior:
            ${if (isFollowUp) "This is a follow-up in the same emergency. Acknowledge the user's update and continue from the previous advice. Do not restart like this is the first message." else "This is the first message in this emergency. Start with immediate guidance."}

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

private fun createSession(): ChatSession {
    val now = System.currentTimeMillis()
    return ChatSession(
        id = UUID.randomUUID().toString(),
        title = "New chat",
        createdAt = now,
        updatedAt = now
    )
}

private fun buildSessionTitle(messages: List<ChatMessage>): String {
    val firstUser = messages.firstOrNull { it.role == Role.USER }?.text.orEmpty().trim()
    if (firstUser.isBlank()) return "New chat"
    return firstUser.replace(Regex("\\s+"), " ").take(36)
}

private fun buildSessionSummary(messages: List<ChatMessage>): String {
    val older = messages.filter { it.text.isNotBlank() }.takeLast(8)
    if (older.isEmpty()) return ""
    return older.joinToString("\n") { message ->
        val label = if (message.role == Role.USER) "User" else "Swara"
        "$label: ${message.text.replace(Regex("\\s+"), " ").take(220)}"
    }.take(1200)
}

private fun inferCategory(question: String): EmergencyCategory {
    val text = question.lowercase(Locale.US)
    return when {
        listOf("bleed", "blood", "burn", "unconscious", "faint", "pain", "injury", "wound", "heat").any { it in text } ->
            EmergencyCategory.MEDICAL
        listOf("fire", "smoke", "flame", "burning", "gas").any { it in text } ->
            EmergencyCategory.FIRE
        listOf("flood", "water rising", "river", "rain", "drowning").any { it in text } ->
            EmergencyCategory.FLOOD
        listOf("earthquake", "shake", "shaking", "aftershock", "rubble").any { it in text } ->
            EmergencyCategory.EARTHQUAKE
        listOf("following", "threat", "violence", "attack", "fight", "rob", "unsafe").any { it in text } ->
            EmergencyCategory.VIOLENCE
        listOf("lost", "stranded", "forest", "trail", "battery low", "can't find").any { it in text } ->
            EmergencyCategory.LOST
        else -> EmergencyCategory.OTHER
    }
}

private fun sanitizeMessages(messages: List<ChatMessage>): List<ChatMessage> {
    val syntheticGuidePrompt = Regex(
        "^Use the .+ Guide for this emergency\\.?$",
        setOf(RegexOption.IGNORE_CASE)
    )
    return messages.filterNot { message ->
        message.role == Role.USER && syntheticGuidePrompt.matches(message.text.trim())
    }.filterNot { message ->
        message.role == Role.ASSISTANT && message.text.trim().equals("Gemma model not installed", ignoreCase = true)
    }
}
