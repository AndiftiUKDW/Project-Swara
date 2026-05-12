package com.swara.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.swara.app.data.model.ChatMessage
import com.swara.app.data.model.ChatSession
import com.swara.app.data.model.EmergencyCategory
import com.swara.app.data.model.GuideCatalogItem
import com.swara.app.data.model.ModelState
import com.swara.app.data.model.ResponseMode
import com.swara.app.data.model.Role
import com.swara.app.data.model.SurvivalPackGuide
import com.swara.app.data.model.VoiceState
import kotlinx.coroutines.launch

@Composable
fun SwaraApp(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val hasAudioPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    SwaraScreen(
        state = state,
        hasAudioPermission = hasAudioPermission,
        onDraftChange = viewModel::updateDraft,
        onSend = viewModel::sendDraft,
        onPickModel = viewModel::requestModelPicker,
        onDownloadModel = viewModel::requestModelDownload,
        onPickDocuments = viewModel::requestDocumentPicker,
        onInstallMarketplaceGuide = viewModel::installMarketplaceGuide,
        onStartDistributionServer = viewModel::startDistributionServer,
        onStopDistributionServer = viewModel::stopDistributionServer,
        onStartWebHostServer = viewModel::startWebHostServer,
        onStopWebHostServer = viewModel::stopWebHostServer,
        onStartVoiceInput = { viewModel.startVoiceInput(hasAudioPermission) },
        onStopSpeaking = viewModel::stopSpeaking,
        onSpeakMessage = viewModel::speakMessage,
        onNewChat = viewModel::newChat,
        onSelectSession = viewModel::selectSession,
        onDeleteSession = viewModel::deleteSession,
        onAskGuide = viewModel::askAboutGuide,
        onSetAutoSpeak = viewModel::setAutoSpeak,
        onSetMaxChunks = viewModel::setMaxChunks,
        onSetResponseMode = viewModel::setResponseMode
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwaraScreen(
    state: MainUiState,
    hasAudioPermission: Boolean,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onPickModel: () -> Unit,
    onDownloadModel: () -> Unit,
    onPickDocuments: () -> Unit,
    onInstallMarketplaceGuide: (String) -> Unit,
    onStartDistributionServer: () -> Unit,
    onStopDistributionServer: () -> Unit,
    onStartWebHostServer: () -> Unit,
    onStopWebHostServer: () -> Unit,
    onStartVoiceInput: () -> Unit,
    onStopSpeaking: () -> Unit,
    onSpeakMessage: (ChatMessage) -> Unit,
    onNewChat: () -> Unit,
    onSelectSession: (String) -> Unit,
    onDeleteSession: (String) -> Unit,
    onAskGuide: (SurvivalPackGuide) -> Unit,
    onSetAutoSpeak: (Boolean) -> Unit,
    onSetMaxChunks: (Int) -> Unit,
    onSetResponseMode: (ResponseMode) -> Unit
) {
    var tab by remember { mutableStateOf(MainTab.GUIDE) }
    var guideDetail by remember { mutableStateOf<SurvivalPackGuide?>(null) }
    var settingsPage by remember { mutableStateOf<SettingsPage?>(null) }
    var showSessions by remember { mutableStateOf(false) }
    var showChatSettings by remember { mutableStateOf(false) }
    var qrPayload by remember { mutableStateOf<QrPayload?>(null) }
    var showMarketplaceHelp by remember { mutableStateOf(false) }
    var showInstallHelp by remember { mutableStateOf(false) }

    BackHandler(enabled = guideDetail != null || settingsPage != null || showSessions || showChatSettings) {
        when {
            showSessions -> showSessions = false
            showChatSettings -> showChatSettings = false
            settingsPage != null -> settingsPage = null
            guideDetail != null -> guideDetail = null
        }
    }

    Scaffold(
        bottomBar = {
            SwaraBottomBar(
                selected = tab,
                onSelect = {
                    tab = it
                    guideDetail = null
                    settingsPage = null
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(padding)
        ) {
            when (tab) {
                MainTab.GUIDE -> {
                    if (guideDetail == null) {
                        GuideHomeScreen(
                            packs = state.survivalPacks,
                            statusMessage = state.statusMessage,
                            onOpenGuide = { guideDetail = it },
                            onAskGuide = {
                                tab = MainTab.ASK
                                onAskGuide(it)
                            }
                        )
                    } else {
                        GuideDetailScreen(
                            guide = guideDetail!!,
                            onBack = { guideDetail = null },
                            onAskGuide = {
                                tab = MainTab.ASK
                                onAskGuide(it)
                            },
                            onShowQr = {
                                qrPayload = QrPayload(
                                    title = "Share Guide",
                                    payload = buildQrPayloadForPack(it),
                                    preview = buildShareTextForPack(it)
                                )
                            }
                        )
                    }
                }

                MainTab.ASK -> AskScreen(
                    state = state,
                    hasAudioPermission = hasAudioPermission,
                    showSessions = showSessions,
                    showSettings = showChatSettings,
                    onToggleSessions = { showSessions = !showSessions },
                    onToggleSettings = { showChatSettings = !showChatSettings },
                    onImportModel = {
                        tab = MainTab.SETTINGS
                        settingsPage = SettingsPage.IMPORT_MODEL
                    },
                    onDraftChange = onDraftChange,
                    onSend = onSend,
                    onStartVoiceInput = onStartVoiceInput,
                    onStopSpeaking = onStopSpeaking,
                    onSpeakMessage = onSpeakMessage,
                    onNewChat = onNewChat,
                    onSelectSession = {
                        onSelectSession(it)
                        showSessions = false
                    },
                    onDeleteSession = onDeleteSession,
                    onSetAutoSpeak = onSetAutoSpeak,
                    onSetMaxChunks = onSetMaxChunks,
                    onSetResponseMode = onSetResponseMode,
                    onShareMessage = { message ->
                        qrPayload = QrPayload(
                            title = "Share Guide",
                            payload = buildQrPayloadForMessage(
                                message = message,
                                conversation = state.messages,
                                category = state.settings.selectedCategory,
                                mode = state.settings.responseMode
                            ),
                            preview = buildShareTextForMessage(
                                message = message,
                                category = state.settings.selectedCategory,
                                mode = state.settings.responseMode
                            )
                        )
                    }
                )

                MainTab.SETTINGS -> {
                    if (settingsPage == null) {
                        SettingsHubScreen(onOpenPage = { settingsPage = it })
                    } else {
                        SettingsPageScreen(
                            page = settingsPage!!,
                            state = state,
                            onBack = { settingsPage = null },
                            onPickModel = onPickModel,
                            onDownloadModel = onDownloadModel,
                            onPickDocuments = onPickDocuments,
                            onInstallMarketplaceGuide = onInstallMarketplaceGuide,
                            onStartDistributionServer = onStartDistributionServer,
                            onStopDistributionServer = onStopDistributionServer,
                            onStartWebHostServer = onStartWebHostServer,
                            onStopWebHostServer = onStopWebHostServer,
                            onShowDistributionQr = { url ->
                                qrPayload = QrPayload("Shareable Local Server", url, url)
                            },
                            onShowWebHostQr = { url ->
                                qrPayload = QrPayload("Swara Web Host", url, url)
                            },
                            onShowMarketplaceHelp = { showMarketplaceHelp = true },
                            onShowInstallHelp = { showInstallHelp = true }
                        )
                    }
                }
            }
        }
    }

    qrPayload?.let { payload ->
        QrPayloadSheet(payload = payload, onDismiss = { qrPayload = null })
    }
    if (showMarketplaceHelp) {
        HelpSheet(
            title = "Add Guides FAQ",
            lines = listOf(
                "Add Guides expands a category without replacing the bundled guide.",
                "Demo catalog items use the same install path as real remote downloads.",
                "Import Guide lets you add local PDF, TXT, or Markdown files.",
                "Downloaded guides appear inside their matching Guide category."
            ),
            onDismiss = { showMarketplaceHelp = false }
        )
    }
    if (showInstallHelp) {
        HelpSheet(
            title = "How to install from local share",
            lines = listOf(
                "Host: turn on hotspot or connect both phones to the same Wi-Fi.",
                "Host: start the Shareable Local Server.",
                "Receiver: scan QR or open the local URL.",
                "Receiver: download APK, model, and guide files.",
                "Receiver: install APK, open Swara, then import the downloaded model."
            ),
            onDismiss = { showInstallHelp = false }
        )
    }
}

@Composable
private fun SwaraBottomBar(selected: MainTab, onSelect: (MainTab) -> Unit) {
    NavigationBar {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(tab.label) }
            )
        }
    }
}

@Composable
private fun GuideHomeScreen(
    packs: List<SurvivalPackGuide>,
    statusMessage: String?,
    onOpenGuide: (SurvivalPackGuide) -> Unit,
    onAskGuide: (SurvivalPackGuide) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        item {
            HeaderBlock(title = "Swara", subtitle = "Guide")
            Text(
                text = "Library of available emergency guides.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            statusMessage?.let {
                Spacer(Modifier.height(10.dp))
                InlineNotice(it)
            }
        }
        items(packs) { pack ->
            GuideCard(
                guide = pack,
                onRead = { onOpenGuide(pack) },
                onAsk = { onAskGuide(pack) }
            )
        }
    }
}

@Composable
private fun GuideCard(
    guide: SurvivalPackGuide,
    onRead: () -> Unit,
    onAsk: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GuideIcon(guide.category)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(guide.category.label, style = MaterialTheme.typography.labelMedium)
                    Text(
                        guide.title.replace("Pack", "Guide"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (guide.addedModules.isNotEmpty()) {
                    AssistChip(
                        onClick = {},
                        label = { Text("+${guide.addedModules.size}") }
                    )
                }
            }
            Text(
                text = guide.quickHelp.firstOrNull().orEmpty(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onRead) { Text("Read guide") }
                OutlinedButton(onClick = onAsk) { Text("Ask Swara") }
            }
        }
    }
}

@Composable
private fun GuideDetailScreen(
    guide: SurvivalPackGuide,
    onBack: () -> Unit,
    onAskGuide: (SurvivalPackGuide) -> Unit,
    onShowQr: (SurvivalPackGuide) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        item {
            TopRowTitle(
                title = guide.title.replace("Pack", "Guide"),
                onBack = onBack
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { onAskGuide(guide) }) {
                    Icon(Icons.Default.Chat, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ask Swara about this")
                }
                OutlinedButton(onClick = { onShowQr(guide) }) {
                    Icon(Icons.Default.QrCode, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Show QR")
                }
            }
        }
        item {
            GuideContentCard(
                title = primaryGuideContentTitle(guide),
                summary = guide.quickHelp.firstOrNull().orEmpty(),
                quickHelp = guide.quickHelp,
                detailedSteps = guide.detailedSteps,
                doNot = guide.doNot,
                source = guide.sourceLabel
            )
        }
        if (guide.addedModules.isNotEmpty()) {
            item {
                Text("Added Guides", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            items(guide.addedModules) { module ->
                GuideContentCard(
                    title = module.title,
                    summary = module.summary,
                    quickHelp = module.quickHelp,
                    detailedSteps = module.detailedSteps,
                    doNot = module.doNot,
                    source = module.sourceName
                )
            }
        }
    }
}

@Composable
private fun GuideContentCard(
    title: String,
    summary: String,
    quickHelp: List<String>,
    detailedSteps: List<String>,
    doNot: List<String>,
    source: String
) {
    Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (summary.isNotBlank()) {
                Text(summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            GuideSection("Quick Help", quickHelp, compact = true)
            GuideSection("Detailed Steps", detailedSteps, compact = true)
            GuideSection("Avoid", doNot, compact = true)
            if (source.isNotBlank()) {
                Text("Source: $source", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun GuideSection(title: String, lines: List<String>, compact: Boolean = false) {
    if (lines.isEmpty()) return
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(if (compact) 12.dp else 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            lines.forEachIndexed { index, line ->
                Row {
                    Text("${index + 1}.", color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(line, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun primaryGuideContentTitle(guide: SurvivalPackGuide): String {
    return when (guide.category) {
        EmergencyCategory.MEDICAL -> "Bleeding Guide"
        EmergencyCategory.FIRE -> "Fire Evacuation Guide"
        EmergencyCategory.FLOOD -> "Flood Safety Guide"
        EmergencyCategory.EARTHQUAKE -> "Earthquake Safety Guide"
        EmergencyCategory.VIOLENCE -> "Personal Safety Guide"
        EmergencyCategory.LOST -> "Lost or Stranded Guide"
        EmergencyCategory.OTHER -> "General Emergency Guide"
    }
}

@Composable
private fun AskScreen(
    state: MainUiState,
    hasAudioPermission: Boolean,
    showSessions: Boolean,
    showSettings: Boolean,
    onToggleSessions: () -> Unit,
    onToggleSettings: () -> Unit,
    onImportModel: () -> Unit,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onStartVoiceInput: () -> Unit,
    onStopSpeaking: () -> Unit,
    onSpeakMessage: (ChatMessage) -> Unit,
    onNewChat: () -> Unit,
    onSelectSession: (String) -> Unit,
    onDeleteSession: (String) -> Unit,
    onSetAutoSpeak: (Boolean) -> Unit,
    onSetMaxChunks: (Int) -> Unit,
    onSetResponseMode: (ResponseMode) -> Unit,
    onShareMessage: (ChatMessage) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            IconButton(onClick = onToggleSessions) { Icon(Icons.Default.Menu, null) }
            Text(
                "Ask Swara",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onToggleSettings) { Icon(Icons.Default.Settings, null) }
        }
        when {
            showSessions -> SessionList(
                sessions = state.sessions,
                activeSessionId = state.activeSessionId,
                onNewChat = onNewChat,
                onSelectSession = onSelectSession,
                onDeleteSession = onDeleteSession,
                modifier = Modifier.weight(1f)
            )
            showSettings -> ChatSettingsPanel(
                state = state,
                onSetAutoSpeak = onSetAutoSpeak,
                onSetMaxChunks = onSetMaxChunks,
                onSetResponseMode = onSetResponseMode,
                modifier = Modifier.weight(1f)
            )
            else -> {
                if (state.modelState !is ModelState.Ready) {
                    MissingModelBanner(onImportModel = onImportModel)
                }
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Bottom),
                    modifier = Modifier.weight(1f)
                ) {
                    if (state.messages.isEmpty()) {
                        item {
                            EmptyAskState()
                        }
                    }
                    items(state.messages) { message ->
                        MessageBubble(
                            message = message,
                            onShare = { onShareMessage(message) },
                            onSpeak = { onSpeakMessage(message) }
                        )
                    }
                }
                Composer(
                    draft = state.draft,
                    busy = state.isBusy,
                    voiceState = state.voiceState,
                    hasAudioPermission = hasAudioPermission,
                    onDraftChange = onDraftChange,
                    onSend = onSend,
                    onStartVoiceInput = onStartVoiceInput,
                    onStopSpeaking = onStopSpeaking
                )
            }
        }
    }
}

@Composable
private fun MissingModelBanner(onImportModel: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Ask Swara needs the Gemma model.", fontWeight = FontWeight.Bold)
            Text("Guide works now. Import or download the model to enable offline AI chat.")
            Button(onClick = onImportModel) { Text("Import Model") }
        }
    }
}

@Composable
private fun EmptyAskState() {
    Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Tell Swara what is happening.", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Swara will choose a guide, use recent chat memory, and answer with practical steps.")
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    onShare: () -> Unit,
    onSpeak: () -> Unit
) {
    val isUser = message.role == Role.USER
    Row(
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(if (isUser) 0.82f else 0.95f)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (isUser) "You" else "Swara", fontWeight = FontWeight.Bold)
                Text(formatHumanMessage(message.text, isUser))
                if (!isUser && message.text.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onShare) {
                            Icon(Icons.Default.Share, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Share Guide")
                        }
                        OutlinedButton(onClick = onSpeak) {
                            Icon(Icons.Default.VolumeUp, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Speak")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Composer(
    draft: String,
    busy: Boolean,
    voiceState: VoiceState,
    hasAudioPermission: Boolean,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onStartVoiceInput: () -> Unit,
    onStopSpeaking: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 6.dp)
        ) {
            Surface(
                onClick = {
                    if (voiceState is VoiceState.Speaking) onStopSpeaking() else onStartVoiceInput()
                },
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.size(58.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(if (voiceState is VoiceState.Speaking) Icons.Default.Stop else Icons.Default.VolumeUp, null)
                }
            }
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                placeholder = { Text(if (hasAudioPermission) "Describe the emergency" else "Type the emergency") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(22.dp),
                minLines = 1,
                maxLines = 3
            )
            Surface(
                onClick = onSend,
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.size(58.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Send, null)
                }
            }
        }
    }
}

@Composable
private fun SessionList(
    sessions: List<ChatSession>,
    activeSessionId: String,
    onNewChat: () -> Unit,
    onSelectSession: (String) -> Unit,
    onDeleteSession: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        item {
            Button(onClick = onNewChat, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("New chat")
            }
        }
        items(sessions) { session ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (session.id == activeSessionId) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSession(session.id) }
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(session.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(session.selectedCategory.label, style = MaterialTheme.typography.labelMedium)
                    }
                    TextButton(onClick = { onDeleteSession(session.id) }) { Text("Delete") }
                }
            }
        }
    }
}

@Composable
private fun ChatSettingsPanel(
    state: MainUiState,
    onSetAutoSpeak: (Boolean) -> Unit,
    onSetMaxChunks: (Int) -> Unit,
    onSetResponseMode: (ResponseMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Response Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        SettingsCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Auto-speak replies", fontWeight = FontWeight.Bold)
                    Text("Voice output may fail without microphone permission.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.settings.autoSpeakResponses, onCheckedChange = onSetAutoSpeak)
            }
        }
        SettingsCard {
            Text("Retrieved guide context: ${state.settings.maxContextChunks} chunks", fontWeight = FontWeight.Bold)
            Slider(
                value = state.settings.maxContextChunks.toFloat(),
                onValueChange = { onSetMaxChunks(it.toInt().coerceIn(1, 8)) },
                valueRange = 1f..8f,
                steps = 6
            )
        }
        SettingsCard {
            Text("Answer mode", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ResponseMode.entries.forEach { mode ->
                    OutlinedButton(onClick = { onSetResponseMode(mode) }) {
                        Text(mode.label)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsHubScreen(onOpenPage: (SettingsPage) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        item { HeaderBlock(title = "Settings", subtitle = "Setup, guides, sharing, and access.") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingsGridRow(
                    SettingsTile(SettingsPage.IMPORT_MODEL, Icons.Default.Download, "Import Model"),
                    SettingsTile(SettingsPage.ADD_GUIDES, Icons.Default.Add, "Add Guides"),
                    onOpenPage
                )
                SettingsGridRow(
                    SettingsTile(SettingsPage.SHARE_LOCAL, Icons.Default.Share, "Share App & Model"),
                    SettingsTile(SettingsPage.WEB_HOST, Icons.Default.PlayArrow, "Web Host"),
                    onOpenPage
                )
                SettingsGridRow(
                    SettingsTile(SettingsPage.LANGUAGE, Icons.Default.Language, "Language"),
                    SettingsTile(SettingsPage.ACCESSIBILITY, Icons.Default.Settings, "Accessibility"),
                    onOpenPage
                )
            }
        }
    }
}

@Composable
private fun SettingsGridRow(left: SettingsTile, right: SettingsTile, onOpenPage: (SettingsPage) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        SettingsTileCard(left, Modifier.weight(1f), onOpenPage)
        SettingsTileCard(right, Modifier.weight(1f), onOpenPage)
    }
}

@Composable
private fun SettingsTileCard(tile: SettingsTile, modifier: Modifier, onOpenPage: (SettingsPage) -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .height(132.dp)
            .clickable { onOpenPage(tile.page) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(tile.icon, null, Modifier.size(34.dp), tint = MaterialTheme.colorScheme.primary)
            Text(tile.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SettingsPageScreen(
    page: SettingsPage,
    state: MainUiState,
    onBack: () -> Unit,
    onPickModel: () -> Unit,
    onDownloadModel: () -> Unit,
    onPickDocuments: () -> Unit,
    onInstallMarketplaceGuide: (String) -> Unit,
    onStartDistributionServer: () -> Unit,
    onStopDistributionServer: () -> Unit,
    onStartWebHostServer: () -> Unit,
    onStopWebHostServer: () -> Unit,
    onShowDistributionQr: (String) -> Unit,
    onShowWebHostQr: (String) -> Unit,
    onShowMarketplaceHelp: () -> Unit,
    onShowInstallHelp: () -> Unit
) {
    when (page) {
        SettingsPage.IMPORT_MODEL -> ImportModelPage(state, onBack, onPickModel, onDownloadModel)
        SettingsPage.ADD_GUIDES -> AddGuidesPage(state, onBack, onPickDocuments, onInstallMarketplaceGuide, onShowMarketplaceHelp)
        SettingsPage.SHARE_LOCAL -> ShareLocalPage(state, onBack, onStartDistributionServer, onStopDistributionServer, onShowDistributionQr, onShowInstallHelp)
        SettingsPage.WEB_HOST -> WebHostPage(state, onBack, onStartWebHostServer, onStopWebHostServer, onShowWebHostQr)
        SettingsPage.LANGUAGE -> SimpleComingSoonPage("Language", "Language marketplace is in demo mode.", onBack)
        SettingsPage.ACCESSIBILITY -> SimpleComingSoonPage("Accessibility", "Coming soon.", onBack)
    }
}

@Composable
private fun ImportModelPage(
    state: MainUiState,
    onBack: () -> Unit,
    onPickModel: () -> Unit,
    onDownloadModel: () -> Unit
) {
    PageColumn {
        TopRowTitle("Import Model", onBack)
        ModelStatusCard(state.modelState, onPickModel)
        SettingsCard {
            Text("Guide works without the model.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Ask Swara needs Gemma 4 LiteRT-LM. Download it when internet is available, or import a local .litertlm file.")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onDownloadModel) {
                    Icon(Icons.Default.Download, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Download Gemma 4 Model")
                }
                OutlinedButton(onClick = onPickModel) {
                    Icon(Icons.Default.UploadFile, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Import manually")
                }
            }
        }
        InlineNotice("Download source is configurable. Manual import and local sharing remain fallback paths.")
    }
}

@Composable
private fun ModelStatusCard(modelState: ModelState, onPickModel: () -> Unit) {
    SettingsCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Emergency, null, Modifier.size(34.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = when (modelState) {
                        ModelState.NotInstalled -> "No model imported"
                        ModelState.Validating -> "Validating model"
                        ModelState.Loading -> "Loading model"
                        is ModelState.Ready -> "Model ready"
                        is ModelState.Error -> "Model error"
                    },
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (modelState) {
                        is ModelState.Ready -> modelState.modelPath.substringAfterLast('/')
                        is ModelState.Error -> modelState.message
                        else -> "Gemma 4 LiteRT-LM"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = onPickModel) {
                Text(if (modelState is ModelState.Ready) "Replace" else "Import")
            }
        }
    }
}

@Composable
private fun AddGuidesPage(
    state: MainUiState,
    onBack: () -> Unit,
    onPickDocuments: () -> Unit,
    onInstallMarketplaceGuide: (String) -> Unit,
    onShowHelp: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = state.marketplaceItems.filter {
        query.isBlank() || it.title.contains(query, ignoreCase = true) || it.summary.contains(query, ignoreCase = true)
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            item {
                TopRowTitle("Add Guides", onBack)
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    placeholder = { Text("Search Guide") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text("Guide marketplace is demo mode. Two downloads are simulated but installed through the real module pipeline.")
            }
            item {
                SettingsCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, null, Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Import Guide", fontWeight = FontWeight.Bold)
                            Text("Add your own PDF, TXT, or Markdown guide locally.")
                        }
                        OutlinedButton(onClick = onPickDocuments) { Text("Import") }
                    }
                }
            }
            items(filtered) { item ->
                MarketplaceCard(
                    item = item,
                    installed = item.id in state.installedGuideModuleIds,
                    onInstall = onInstallMarketplaceGuide
                )
            }
        }
        FloatingActionButton(
            onClick = onShowHelp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) { Text("?") }
    }
}

@Composable
private fun MarketplaceCard(
    item: GuideCatalogItem,
    installed: Boolean,
    onInstall: (String) -> Unit
) {
    Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GuideIcon(item.category)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.category.label, style = MaterialTheme.typography.labelMedium)
                    Text(item.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            Text(item.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Source: ${item.sourceName}", style = MaterialTheme.typography.labelMedium)
            OutlinedButton(
                onClick = { onInstall(item.id) },
                enabled = !item.comingSoon && !installed,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    when {
                        installed -> "Downloaded"
                        item.comingSoon -> "Coming soon"
                        else -> "Download"
                    }
                )
            }
        }
    }
}

@Composable
private fun ShareLocalPage(
    state: MainUiState,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onShowQr: (String) -> Unit,
    onShowInstallHelp: () -> Unit
) {
    PageColumn {
        TopRowTitle("Share App & Model", onBack)
        SettingsCard {
            Text("Shareable Local Server", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Share APK, guide files, and imported model over the same hotspot or Wi-Fi.")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = if (state.distributionServerState.running) onStop else onStart) {
                    Icon(if (state.distributionServerState.running) Icons.Default.Stop else Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.distributionServerState.running) "Stop Server" else "Start Server")
                }
                OutlinedButton(
                    onClick = { state.distributionServerState.url?.let(onShowQr) },
                    enabled = state.distributionServerState.url != null
                ) {
                    Icon(Icons.Default.QrCode, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Share QR")
                }
            }
            Text(state.distributionServerState.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
            state.distributionServerState.url?.let { Text(it, style = MaterialTheme.typography.labelMedium) }
        }
        SettingsCard {
            Text("Guide before starting the server", fontWeight = FontWeight.Bold)
            StepText("Host turns on hotspot or joins same Wi-Fi.")
            StepText("Receiver connects to that hotspot/Wi-Fi.")
            StepText("Host starts server and shares QR.")
            StepText("Receiver downloads APK/model/guides, installs APK, then imports model.")
            OutlinedButton(onClick = onShowInstallHelp, modifier = Modifier.fillMaxWidth()) {
                Text("How to Install")
            }
        }
    }
}

@Composable
private fun WebHostPage(
    state: MainUiState,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onShowQr: (String) -> Unit
) {
    PageColumn {
        TopRowTitle("Web Host", onBack)
        SettingsCard {
            Text("Host browser Swara", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Serve a lightweight web version of Swara from this phone. This is separate from APK/model download sharing.")
            InlineNotice("Trusted local network only. Anyone connected may open the hosted guide UI.")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = if (state.webHostServerState.running) onStop else onStart) {
                    Icon(if (state.webHostServerState.running) Icons.Default.Stop else Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.webHostServerState.running) "Stop Web Host" else "Start Web Host")
                }
                OutlinedButton(
                    onClick = { state.webHostServerState.url?.let(onShowQr) },
                    enabled = state.webHostServerState.url != null
                ) {
                    Icon(Icons.Default.QrCode, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Share QR")
                }
            }
            Text(state.webHostServerState.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
            state.webHostServerState.url?.let { Text(it, style = MaterialTheme.typography.labelMedium) }
        }
        SettingsCard {
            Text("Current web host scope", fontWeight = FontWeight.Bold)
            StepText("Guide list and guide details are available in browser.")
            StepText("AI endpoint is intentionally staged after native chat memory is stable.")
            StepText("Browser clients never receive the model file directly.")
        }
    }
}

@Composable
private fun SimpleComingSoonPage(title: String, message: String, onBack: () -> Unit) {
    PageColumn {
        TopRowTitle(title, onBack)
        SettingsCard {
            Text(message, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun HeaderBlock(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TopRowTitle(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PageColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun InlineNotice(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
private fun StepText(text: String) {
    Row {
        Text("•", color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(text, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun GuideIcon(category: EmergencyCategory) {
    val icon = when (category) {
        EmergencyCategory.MEDICAL -> Icons.Default.MedicalServices
        EmergencyCategory.FIRE -> Icons.Default.Emergency
        EmergencyCategory.FLOOD -> Icons.Default.Folder
        EmergencyCategory.EARTHQUAKE -> Icons.Default.Emergency
        EmergencyCategory.VIOLENCE -> Icons.Default.Emergency
        EmergencyCategory.LOST -> Icons.Default.Search
        EmergencyCategory.OTHER -> Icons.Default.Book
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QrPayloadSheet(payload: QrPayload, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val bitmap: Bitmap = remember(payload.payload) { generateQrBitmap(payload.payload) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(payload.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "QR code",
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(androidx.compose.ui.graphics.Color.White)
                    .padding(12.dp)
            )
            Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Text(payload.preview, Modifier.padding(14.dp), maxLines = 9, overflow = TextOverflow.Ellipsis)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { clipboard.setText(AnnotatedString(payload.preview)) }) {
                    Icon(Icons.Default.ContentCopy, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Copy")
                }
                Button(onClick = { shareText(context, payload.preview) }) {
                    Icon(Icons.Default.Share, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Share")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HelpSheet(title: String, lines: List<String>, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            lines.forEach { StepText(it) }
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
        }
    }
}

private fun shareText(context: android.content.Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share with"))
}

private fun formatHumanMessage(text: String, isUser: Boolean): String {
    if (isUser) return text
    val raw = text
        .replace(Regex("\\s+"), " ")
        .replace(Regex("(?i)Avoid\\s*:\\s*(\\d+\\.\\s*)?Avoid\\s*:"), "Avoid:")
        .replace(Regex("(?i)Do this now\\s*:\\s*(\\d+\\.\\s*)?Do this now\\s*:"), "Do this now:")
        .replace(Regex("(?i)(^|\\s)(\\d+\\.\\s*)Avoid\\s*:\\s*"), " $2")
        .replace(Regex("(?i)(^|\\s)(\\d+\\.\\s*)Do this now\\s*:\\s*"), " $2")
        .replace(Regex("(?i)\\bDo this now\\s*:?\\s*"), "\nDo this now:\n")
        .replace(Regex("(?i)\\bAvoid\\s*:?\\s*"), "\nAvoid:\n")
        .replace(Regex("(?<=[a-z])(?=Can you|Are you|Is the|Do you)", RegexOption.IGNORE_CASE), "\n\n")
        .replace(Regex("(?i)^\\s*RISK\\s*:?\\s*"), "")
        .replace(Regex("(?i)\\bSITUATION\\b\\s*:?\\s*"), "\n\n")
        .replace(Regex("(?i)\\bDO NOW\\b\\s*:?\\s*"), "\n\nDo this now:\n")
        .replace(Regex("(?i)\\bDO NOT\\b\\s*:?\\s*"), "\n\nAvoid:\n")
        .replace(Regex("(?i)\\bNEXT QUESTION\\b\\s*:?\\s*"), "\n\nQuestion:\n")
        .replace(Regex("(?<!\\n)(\\d+\\.\\s*)"), "\n$1")
        .replace(Regex("\\n\\s+"), "\n")
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()
    val cleaned = mutableListOf<String>()
    var lastHeading = ""
    raw.lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .forEach { line ->
            val lineWithoutOrphanNumber = line.replace(Regex("^\\d+\\.\\s*(?=(Do this now|Avoid):$)", RegexOption.IGNORE_CASE), "")
            val heading = when {
                lineWithoutOrphanNumber.equals("Do this now:", ignoreCase = true) -> "Do this now:"
                lineWithoutOrphanNumber.equals("Avoid:", ignoreCase = true) -> "Avoid:"
                lineWithoutOrphanNumber.equals("Question:", ignoreCase = true) -> "Question:"
                else -> ""
            }
            if (heading.isNotBlank()) {
                if (lastHeading != heading) {
                    cleaned += heading
                    lastHeading = heading
                }
            } else {
                val normalizedLine = lineWithoutOrphanNumber
                    .replace(Regex("^\\d+\\.\\s*(Avoid|Do this now):\\s*", RegexOption.IGNORE_CASE), "")
                    .replace(Regex("^(Avoid|Do this now):\\s*", RegexOption.IGNORE_CASE), "")
                    .trim()
                if (normalizedLine.isNotBlank()) {
                    cleaned += normalizedLine
                    lastHeading = ""
                }
            }
        }
    return cleaned.joinToString("\n")
        .replace(Regex("(?i)Avoid:\\nAvoid:\\n"), "Avoid:\n")
        .replace(Regex("(?i)Do this now:\\nDo this now:\\n"), "Do this now:\n")
        .trim()
}

private data class QrPayload(
    val title: String,
    val payload: String,
    val preview: String
)

private data class SettingsTile(
    val page: SettingsPage,
    val icon: ImageVector,
    val label: String
)

private enum class MainTab(val label: String, val icon: ImageVector) {
    GUIDE("Guide", Icons.Default.Book),
    ASK("Ask", Icons.Default.Chat),
    SETTINGS("Settings", Icons.Default.Settings)
}

private enum class SettingsPage {
    IMPORT_MODEL,
    ADD_GUIDES,
    SHARE_LOCAL,
    WEB_HOST,
    LANGUAGE,
    ACCESSIBILITY
}
