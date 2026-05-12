package com.swara.app.services

import com.swara.app.data.model.AppSettings
import com.swara.app.data.model.ChatMessage
import com.swara.app.data.model.CitationRef
import com.swara.app.data.model.EmergencyCategory
import com.swara.app.data.model.RetrievalResult
import com.swara.app.data.model.Role
import com.swara.app.data.model.ModelState
import com.swara.app.data.model.ResponseMode
import com.swara.app.data.model.SurvivalPackGuide
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
        survivalPack: SurvivalPackGuide?,
        settings: AppSettings
    ): Flow<ChatMessage> = flow {
        val modelPath = (modelManager.state.value as? ModelState.Ready)?.modelPath
            ?: error("Gemma model not installed")
        val engine = getOrCreateEngine(modelPath)
        val systemPrompt = buildSystemPrompt(retrieval, survivalPack, settings)
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

    private fun buildSystemPrompt(
        retrieval: List<RetrievalResult>,
        survivalPack: SurvivalPackGuide?,
        settings: AppSettings
    ): String {
        val responseContract = buildResponseContract(settings.responseMode)
        val categoryGuidance = buildCategoryGuidance(settings.selectedCategory)
        val survivalPackBlock = buildSurvivalPackBlock(survivalPack, settings.responseMode)
        if (retrieval.isEmpty()) {
            return """
                You are Swara, an offline-first emergency guidance assistant powered by Gemma 4.
                Reply naturally, directly, and calmly.
                Focus on immediate survival guidance instead of generic conversation.
                Do not describe the prompt, retrieved context, or the fact that you were given excerpts.
                Avoid openings like "The provided context", "The document says", or "Based on the excerpts above".
                Do not claim a definitive diagnosis.
                Do not replace emergency responders. If help is reachable, mention it briefly without making it the only answer.
                Stay useful if help is unreachable.
                Do not add generic chatbot filler, disclaimers, or long speculation.

                Active emergency category:
                $categoryGuidance

                Bundled survival pack:
                $survivalPackBlock

                Response goal:
                $responseContract

                Formatting rules:
                - Sound like a calm human emergency guide, not a form.
                - Start with one short sentence about urgency or risk.
                - Prefer short paragraphs and numbered action lines.
                - If you use headings, use each heading once only.
                - Good heading examples: "Do this now:" and "Avoid:".
                - Ask at most one critical follow-up question.
                - Keep numbered steps on separate lines.
                - Never write duplicated headings like "Avoid: Avoid:".
                - Never write "Avoid:" inside an avoid-list item.
                - Bad: "1. Avoid: remove the object"
                - Good: "1. Do not remove the object"
                - Output plain text only. Do not wrap the answer in code fences.
                - Never emit raw source headers like "[Source: ...]".
                - Never leave unmatched "*" or "_" markers in the answer.
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
            Do not claim a definitive diagnosis.
            Do not replace emergency responders. If help is reachable, mention it briefly without making it the only answer.
            Stay useful if help is unreachable.
            Do not add generic chatbot filler, disclaimers, or long speculation.
            Do not say:
            - "The provided context"
            - "The context consists of excerpts"
            - "Here are the key points from the text"
            - "According to the provided context"
            - "Based on the supplied context"
            - "The document titled"
            - "The retrieved context"

            Active emergency category:
            $categoryGuidance

            Bundled survival pack:
            $survivalPackBlock

            Response goal:
            $responseContract

            Formatting guidance:
            - Sound like a calm human emergency guide, not a form.
            - Start with one short sentence about urgency or risk.
            - Prefer short paragraphs and numbered action lines.
            - If you use headings, use each heading once only.
            - Good heading examples: "Do this now:" and "Avoid:".
            - Ask at most one critical follow-up question.
            - Keep numbered steps on separate lines.
            - Never write duplicated headings like "Avoid: Avoid:".
            - Never write "Avoid:" inside an avoid-list item.
            - Bad: "1. Avoid: remove the object"
            - Good: "1. Do not remove the object"
            - Output plain text only. Do not wrap the answer in code fences.
            - Never output raw source headers like "[Source: ...]".
            - Keep survival-pack references natural, for example "(flood_pack.md)" or "(page 2)".
            - If the answer is not supported by the retrieved material, say so plainly.
            - Prefer guidance that helps during the first critical hours of an emergency.
            - Start immediately with the answer. Do not add an introduction about the context you received.

            Retrieved context:
            $contextBlock
        """.trimIndent()
    }

    private fun buildSurvivalPackBlock(
        survivalPack: SurvivalPackGuide?,
        mode: ResponseMode
    ): String {
        if (survivalPack == null) {
            return """
                No bundled survival pack is available for this category in the current Swara scope.
                Do not invent source-specific guidance for this category.
            """.trimIndent()
        }
        val primarySteps = when (mode) {
            ResponseMode.QUICK_HELP -> survivalPack.quickHelp
            ResponseMode.DETAILED_STEPS -> survivalPack.detailedSteps
        }
        return """
            ${survivalPack.title}
            Pack version: ${survivalPack.version}
            Last updated: ${survivalPack.lastUpdated}
            Scope: ${survivalPack.scope}
            Source label: ${survivalPack.sourceLabel}
            Source URLs: ${survivalPack.sourceUrls.joinToString(", ")}
            Use these as the primary safety facts when relevant.
            Immediate guidance:
            ${primarySteps.toNumberedLines()}
            Avoid:
            ${survivalPack.doNot.toNumberedLines()}
            Useful supplies:
            ${survivalPack.kit.joinToString(", ")}
        """.trimIndent()
    }

    private fun buildResponseContract(mode: ResponseMode): String {
        val modeRule = when (mode) {
            ResponseMode.QUICK_HELP -> """
                Mode: Quick Help.
                Keep the whole answer short.
                Use 3 or fewer action steps and 2 or fewer avoid/warning steps.
                Each step should be one short sentence.
            """.trimIndent()
            ResponseMode.DETAILED_STEPS -> """
                Mode: Detailed Steps.
                Provide more complete guidance, but keep it practical.
                Use 4 to 7 action steps and 2 to 4 avoid/warning steps.
                Add brief condition checks when they change the action.
            """.trimIndent()
        }
        return """
            $modeRule

            Human response structure:
            First sentence: direct urgency/risk statement, for example "This sounds urgent."

            Include:
            - A short risk/urgency sentence.
            - Immediate survival actions.
            - Unsafe actions to avoid.
            - One critical question only if it changes the next instruction.
        """.trimIndent()
    }

    private fun buildCategoryGuidance(category: EmergencyCategory): String {
        return when (category) {
            EmergencyCategory.MEDICAL -> """
                Medical emergency.
                Prioritize scene safety, breathing, severe bleeding, consciousness, and safe positioning.
                Avoid definitive diagnosis, medication dosing, or invasive procedures.
            """.trimIndent()
            EmergencyCategory.FIRE -> """
                Fire emergency.
                Prioritize leaving smoke/fire, staying low under smoke, checking door heat, and not re-entering.
                Avoid advice that delays evacuation.
            """.trimIndent()
            EmergencyCategory.FLOOD -> """
                Flood emergency.
                Prioritize moving to higher ground, avoiding moving water, electricity hazards, and contaminated water.
                Avoid advice to drive or walk through floodwater.
            """.trimIndent()
            EmergencyCategory.EARTHQUAKE -> """
                Earthquake emergency.
                Prioritize drop-cover-hold during shaking, avoiding glass and unstable structures, and checking hazards after shaking stops.
                Avoid advice to run outside during active shaking unless already outside in open space.
            """.trimIndent()
            EmergencyCategory.VIOLENCE -> """
                Violence or personal safety emergency.
                Prioritize leaving early if safe, creating distance, avoiding confrontation, reaching public or staffed areas, and contacting trusted local help when safe.
                Do not provide school-shooting or active-shooter tactics. Keep guidance to general personal safety, de-escalation, escape from immediate threat, and post-incident documentation.
            """.trimIndent()
            EmergencyCategory.LOST -> """
                Lost or stranded emergency.
                Prioritize staying visible, conserving battery, marking location, shelter, water safety, and signaling.
                Avoid advice that causes unnecessary wandering or separation.
            """.trimIndent()
            EmergencyCategory.OTHER -> """
                General emergency.
                Prioritize immediate danger removal, airway/breathing/bleeding checks, shelter, communication, and one focused next question.
            """.trimIndent()
        }
    }

    private fun List<String>.toNumberedLines(): String {
        if (isEmpty()) return "None."
        return mapIndexed { index, item -> "${index + 1}. $item" }.joinToString("\n")
    }
}
