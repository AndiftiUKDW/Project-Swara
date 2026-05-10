package com.swara.app

import android.app.Application
import com.swara.app.data.repo.DocumentRepository
import com.swara.app.data.repo.SurvivalPackRepository
import com.swara.app.services.Chunker
import com.swara.app.services.DocumentParser
import com.swara.app.services.DistributionServer
import com.swara.app.services.GemmaChatService
import com.swara.app.services.ModelManager
import com.swara.app.services.VoiceController

class SwaraApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val modelManager = ModelManager(this)
        val parser = DocumentParser(this)
        val repository = DocumentRepository(
            context = this,
            parser = parser,
            chunker = Chunker()
        )
        val survivalPackRepository = SurvivalPackRepository(this)
        container = AppContainer(
            modelManager = modelManager,
            chatService = GemmaChatService(modelManager),
            distributionServer = DistributionServer(
                context = this,
                modelManager = modelManager,
                survivalPackRepository = survivalPackRepository
            ),
            documentRepository = repository,
            survivalPackRepository = survivalPackRepository,
            voiceController = VoiceController(this)
        )
    }
}
