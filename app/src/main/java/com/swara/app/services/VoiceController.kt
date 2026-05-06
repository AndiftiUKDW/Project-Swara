package com.swara.app.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.swara.app.data.model.VoiceState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale

class VoiceController(
    context: Context
) : TextToSpeech.OnInitListener {
    private val appContext = context.applicationContext
    private val recognizer = SpeechRecognizer.createSpeechRecognizer(appContext)
    private val tts = TextToSpeech(appContext, this)
    private val _state = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val state: StateFlow<VoiceState> = _state
    private var ttsReady = false

    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (ttsReady) {
            tts.language = Locale.getDefault()
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _state.value = VoiceState.Speaking
                }

                override fun onDone(utteranceId: String?) {
                    _state.value = VoiceState.Idle
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _state.value = VoiceState.Error("Text-to-speech playback failed")
                }
            })
        } else {
            _state.value = VoiceState.Error("Text-to-speech initialization failed")
        }
    }

    fun listen(): Flow<String> = callbackFlow {
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            _state.value = VoiceState.Error("Speech recognition unavailable")
            close()
            return@callbackFlow
        }
        stopSpeaking()
        _state.value = VoiceState.Listening
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() {
                _state.value = VoiceState.Transcribing
            }

            override fun onError(error: Int) {
                _state.value = VoiceState.Error("Speech recognition error: $error")
                close()
            }

            override fun onResults(results: Bundle) {
                val text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                _state.value = VoiceState.Idle
                trySend(text)
                close()
            }

            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        recognizer.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            }
        )
        awaitClose {
            recognizer.cancel()
        }
    }

    fun speak(text: String) {
        if (!ttsReady) {
            _state.value = VoiceState.Error("Text-to-speech unavailable")
            return
        }
        val spokenText = sanitizeForSpeech(text)
        if (spokenText.isBlank()) {
            _state.value = VoiceState.Idle
            return
        }
        tts.speak(
            spokenText,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "gemma-response"
        )
    }

    fun stopSpeaking() {
        tts.stop()
        if (_state.value is VoiceState.Speaking) {
            _state.value = VoiceState.Idle
        }
    }

    private fun sanitizeForSpeech(raw: String): String {
        return raw
            .replace("\\n", "\n")
            .replace("\\t", " ")
            .replace("\\r", "\n")
            .replace(Regex("\\r\\n?"), "\n")
            .let(::stripSpeechPromptEchoIntro)
            .replace(Regex("(?m)^\\s*\\[Source:[^\\]]*]\\s*$"), "")
            .replace(Regex("(?m)^\\s{0,3}#{1,6}\\s*(.+)$")) { match ->
                "${match.groupValues[1].trim().trimEnd(':', '.', ',', ';')}. "
            }
            .replace(Regex("(?m)^\\s*[-*]\\s+"), "")
            .replace(Regex("(?m)^\\s*>\\s*"), "")
            .replace(Regex("`([^`]+)`"), "$1")
            .replace(Regex("\\[(.*?)\\]\\([^)]*\\)"), "$1")
            .replace(Regex("\\[(.*?)\\]"), "$1")
            .replace(Regex("\\*\\*\\*([^*]+)\\*\\*\\*"), "$1")
            .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")
            .replace(Regex("\\*([^*]+)\\*"), "$1")
            .replace(Regex("__([^_]+)__"), "$1")
            .replace(Regex("_([^_]+)_"), "$1")
            .replace(Regex("(?m)^\\s*\\*{1,3}(?=\\S)"), "")
            .replace(Regex("(?m)(?<=\\S)\\*{1,3}(?=\\s|$)"), "")
            .replace(Regex("(?m)^\\s*[-–—]\\s*"), "")
            .replace(Regex("(?m)^\\s+"), "")
            .replace(Regex("[ \\t]{2,}"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")
            .lines()
            .joinToString("\n") { it.trim() }
            .replace(Regex("(?m)^\\s*\\([^)]*\\)\\s*$"), "")
            .replace(Regex("\\s+([,.;:?!])"), "$1")
            .replace(Regex("\\n(?=\\S)"), "\n")
            .trim()
    }

    private fun stripSpeechPromptEchoIntro(text: String): String {
        var result = text.trim()
        val introPatterns = listOf(
            Regex("^The provided context consists of excerpts from (a|the) document titled\\s*", RegexOption.IGNORE_CASE),
            Regex("^The provided context consists of excerpts from\\s*", RegexOption.IGNORE_CASE),
            Regex("^The context consists of excerpts from\\s*", RegexOption.IGNORE_CASE),
            Regex("^Here are the key points from the text:\\s*", RegexOption.IGNORE_CASE),
            Regex("^Here are the main points of the document:\\s*", RegexOption.IGNORE_CASE),
            Regex("^Based on the provided context,\\s*", RegexOption.IGNORE_CASE),
            Regex("^According to the provided context,\\s*", RegexOption.IGNORE_CASE)
        )
        introPatterns.forEach { pattern ->
            result = result.replaceFirst(pattern, "")
        }
        return result.trimStart(' ', '"', '\'', '`')
    }
}
