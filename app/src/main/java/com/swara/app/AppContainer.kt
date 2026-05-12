package com.swara.app

import com.swara.app.data.repo.DocumentRepository
import com.swara.app.data.repo.ChatSessionRepository
import com.swara.app.data.repo.SurvivalPackRepository
import com.swara.app.services.GemmaChatService
import com.swara.app.services.DistributionServer
import com.swara.app.services.ModelManager
import com.swara.app.services.WebHostServer
import com.swara.app.services.VoiceController

data class AppContainer(
    val modelManager: ModelManager,
    val chatService: GemmaChatService,
    val distributionServer: DistributionServer,
    val webHostServer: WebHostServer,
    val documentRepository: DocumentRepository,
    val chatSessionRepository: ChatSessionRepository,
    val survivalPackRepository: SurvivalPackRepository,
    val voiceController: VoiceController
)
