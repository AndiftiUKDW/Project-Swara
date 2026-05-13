package com.swara.app.services

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.swara.app.data.model.ModelState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class ModelManager(
    private val context: Context
) {
    private val _state = MutableStateFlow<ModelState>(ModelState.NotInstalled)
    val state: StateFlow<ModelState> = _state

    private val modelFile: File
        get() = File(context.filesDir, "models/gemma-4-e2b-it.litertlm")

    init {
        val existing = modelFile.takeIf(File::exists)
        if (existing != null) {
            _state.value = ModelState.Ready(existing.absolutePath)
        }
    }

    suspend fun importModel(uri: Uri) = withContext(Dispatchers.IO) {
        _state.value = ModelState.Validating
        runCatching {
            val name = DocumentFile.fromSingleUri(context, uri)?.name.orEmpty()
            require(name.endsWith(".litertlm", ignoreCase = true)) {
                "Model file must end with .litertlm"
            }
            modelFile.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri)?.use { input ->
                modelFile.outputStream().use { output -> input.copyTo(output) }
            } ?: error("Unable to open model file")
            _state.value = ModelState.Ready(modelFile.absolutePath)
        }.onFailure { throwable ->
            _state.value = ModelState.Error(throwable.message ?: "Model import failed")
        }
    }

    suspend fun downloadDefaultModel() = withContext(Dispatchers.IO) {
        _state.value = ModelState.Downloading()
        runCatching {
            modelFile.parentFile?.mkdirs()
            val tempFile = File(modelFile.parentFile, "${modelFile.name}.download")
            if (tempFile.exists()) tempFile.delete()

            val connection = (URL(DEFAULT_MODEL_DOWNLOAD_URL).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                connectTimeout = 30_000
                readTimeout = 60_000
                requestMethod = "GET"
            }
            try {
                val code = connection.responseCode
                require(code in 200..299) { "Model download failed: HTTP $code" }
                val totalBytes = connection.contentLengthLong.takeIf { it > 0L }
                var copiedBytes = 0L
                var lastProgress = -1
                connection.inputStream.use { input ->
                    tempFile.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            output.write(buffer, 0, read)
                            copiedBytes += read
                            val progress = totalBytes?.let { ((copiedBytes * 100) / it).toInt().coerceIn(0, 99) }
                            if (progress != null && progress != lastProgress) {
                                _state.value = ModelState.Downloading(progress)
                                lastProgress = progress
                            }
                        }
                    }
                }
                require(tempFile.length() > 0L) { "Downloaded model file is empty" }
                if (modelFile.exists()) modelFile.delete()
                require(tempFile.renameTo(modelFile)) { "Unable to save downloaded model" }
                _state.value = ModelState.Ready(modelFile.absolutePath)
            } finally {
                connection.disconnect()
                if (tempFile.exists() && _state.value !is ModelState.Ready) tempFile.delete()
            }
        }.onFailure { throwable ->
            _state.value = ModelState.Error(throwable.message ?: "Model download failed")
        }
    }

    fun markLoading() {
        val ready = _state.value as? ModelState.Ready ?: return
        _state.value = ModelState.Loading
        _state.value = ready
    }

    fun currentModelPath(): String? = (state.value as? ModelState.Ready)?.modelPath

    companion object {
        private const val DEFAULT_MODEL_DOWNLOAD_URL =
            "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm?download=true"
    }
}
