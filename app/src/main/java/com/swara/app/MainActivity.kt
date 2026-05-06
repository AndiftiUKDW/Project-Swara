package com.swara.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import com.swara.app.ui.MainViewModel
import com.swara.app.ui.SwaraApp
import com.swara.app.ui.theme.SwaraTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory((application as SwaraApplication).container)
    }

    private val modelPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                contentResolver.takePersistableUriPermission(
                    uri,
                    IntentFlags.readOnly
                )
                viewModel.importModel(uri)
            }
        }

    private val documentPicker =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if (uris.isNotEmpty()) {
                uris.forEach { uri ->
                    contentResolver.takePersistableUriPermission(
                        uri,
                        IntentFlags.readOnly
                    )
                }
                viewModel.importDocuments(uris)
            }
        }

    private val requestAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            viewModel.onAudioPermissionResult(granted)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SwaraTheme {
                LaunchedEffect(Unit) {
                    viewModel.events.collect { event ->
                        when (event) {
                            MainViewModel.UiEvent.PickModel -> modelPicker.launch(arrayOf("*/*"))
                            MainViewModel.UiEvent.PickDocuments -> documentPicker.launch(
                                arrayOf("application/pdf", "text/plain", "text/markdown", "text/x-markdown")
                            )
                            MainViewModel.UiEvent.RequestAudioPermission -> requestAudioPermission.launch(
                                Manifest.permission.RECORD_AUDIO
                            )
                        }
                    }
                }
                SwaraApp(viewModel = viewModel)
            }
        }
    }
}

private object IntentFlags {
    const val readOnly = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
}
