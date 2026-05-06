package com.swara.app.services

import com.swara.app.data.model.AppSettings
import com.swara.app.data.model.ChatMessage
import com.swara.app.data.model.CitationRef
import com.swara.app.data.model.RetrievalResult
import com.swara.app.data.model.Role
import com.swara.app.data.model.ModelState
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class GemmaChatService(
    private val modelManager: ModelManager
) {
    private val engineMutex = Mutex()
    private var engine: Engine? = null
    private var engineModelPath: String? = null

    fun streamReply(
        history: List<ChatMessage>,
        question: String,
        retrieval: List<RetrievalResult>,
        settings: AppSettings
    ): Flow<ChatMessage> = flow {
        val modelPath = (modelManager.state.value as? ModelState.Ready)?.modelPath
            ?: error("Gemma model not installed")
        val engine = getOrCreateEngine(modelPath)
        val systemPrompt = buildSystemPrompt(retrieval)
        val initialMessages = history.map { message ->
            when (message.role) {
                Role.USER -> Message.user(message.text)
                Role.ASSISTANT -> Message.model(message.text)
            }
        }
        val conversationConfig = ConversationConfig(
            systemInstruction = com.google.ai.edge.litertlm.Contents.of(systemPrompt),
            initialMessages = initialMessages,
            samplerConfig = SamplerConfig(
                topK = settings.topK,
                topP = settings.topP.toDouble(),
                temperature = settings.temperature.toDouble()
            )
        )
        val citations = retrieval.map {
            CitationRef(
                documentId = it.documentId,
                documentName = it.documentName,
                pageNumber = it.pageNumber,
                chunkId = it.chunkId
            )
        }
        engine.createConversation(conversationConfig).use { conversation ->
            conversation.sendMessageAsync(question).collect { message ->
                emit(
                    ChatMessage(
                        id = UUID.randomUUID().toString(),
                        role = Role.ASSISTANT,
                        text = message.toString(),
                        citations = citations,
                        isStreaming = true
                    )
                )
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun warmUpIfPossible() {
        val modelPath = (modelManager.state.value as? ModelState.Ready)?.modelPath ?: return
        getOrCreateEngine(modelPath)
    }

    private suspend fun getOrCreateEngine(modelPath: String): Engine {
        return engineMutex.withLock {
            if (engine != null && engineModelPath == modelPath) {
                return@withLock engine!!
            }
            engine?.close()
            val created = Engine(
                EngineConfig(
                    modelPath = modelPath,
                    backend = Backend.GPU(),
                    cacheDir = null
                )
            )
            created.initialize()
            engine = created
            engineModelPath = modelPath
            created
        }
    }

    private fun buildSystemPrompt(retrieval: List<RetrievalResult>): String {
        if (retrieval.isEmpty()) {
            return """
                You are Swara, an offline-first emergency guidance assistant powered by Gemma 4.
                Reply naturally, directly, and calmly.
                Focus on immediate survival guidance instead of generic conversation.
                Do not describe the prompt, retrieved context, or the fact that you were given excerpts.
                Avoid openings like "The provided context", "The document says", or "Based on the excerpts above".
                Output plain text or simple Markdown only.
                If you use Markdown, keep it minimal:
                - short paragraphs
                - headings written as "## Heading"
                - bullet lists written as "- item"
                Never emit raw source headers like "[Source: ...]".
                Never leave unmatched "*" or "_" markers in the answer.
                If supporting knowledge is unavailable, say that clearly and continue with general emergency guidance.
            """.trimIndent()
        }
        val contextBlock = retrieval.joinToString("\n\n") { result ->
            buildString {
                append("[Source: ")
                append(result.documentName)
                result.pageNumber?.let { append(", page ").append(it) }
                append("]\n")
                append(result.text)
            }
        }
        return """
            You are Swara, an offline-first emergency guidance assistant in a mobile app.
            Start with the answer itself.
            Use natural user-facing wording, not system-facing wording.
            Sound calm, urgent when necessary, and action-oriented.
            Do not say:
            - "The provided context"
            - "The context consists of excerpts"
            - "Here are the key points from the text"
            - "According to the provided context"
            - "Based on the supplied context"
            - "The document titled"
            - "The retrieved context"

            Formatting guidance:
            - Prefer short paragraphs.
            - Use plain text by default. Use simple Markdown only when structure helps readability.
            - For sections, prefer Markdown headings like "## Objective", "## Methodology", "## Results", "## Conclusion".
            - For lists, use "-" bullets.
            - Only use emphasis when markers are properly paired.
            - Never prefix a sentence with "*" or "**".
            - Never output raw source headers like "[Source: ...]".
            - Keep section labels compact and readable.
            - Keep document references natural, for example "(print_Skripsi.pdf, p. 17)".
            - If the answer is not supported by the retrieved material, say so plainly.
            - Prefer guidance that helps during the first critical hours of an emergency.
            - Start immediately with the answer. Do not add an introduction about the context you received.

            Retrieved context:
            $contextBlock
        """.trimIndent()
    }
}
