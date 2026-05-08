package com.swara.app

import com.swara.app.data.repo.DocumentRepository
import com.swara.app.data.repo.SurvivalPackRepository
import com.swara.app.services.GemmaChatService
import com.swara.app.services.ModelManager
import com.swara.app.services.VoiceController

data class AppContainer(
    val modelManager: ModelManager,
    val chatService: GemmaChatService,
    val documentRepository: DocumentRepository,
    val survivalPackRepository: SurvivalPackRepository,
    val voiceController: VoiceController
)
