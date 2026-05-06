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

    fun markLoading() {
        val ready = _state.value as? ModelState.Ready ?: return
        _state.value = ModelState.Loading
        _state.value = ready
    }

    fun currentModelPath(): String? = (state.value as? ModelState.Ready)?.modelPath
}
