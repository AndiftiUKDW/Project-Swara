package com.swara.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swara.app.data.model.ChatMessage
import com.swara.app.data.model.DocumentRecord
import com.swara.app.data.model.DocumentStatus
import com.swara.app.data.model.EmergencyCategory
import com.swara.app.data.model.ModelState
import com.swara.app.data.model.RetrievalResult
import com.swara.app.data.model.ResponseMode
import com.swara.app.data.model.Role
import com.swara.app.data.model.VoiceState

@Composable
fun SwaraApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val showLibrarySheet = remember { mutableStateOf(false) }
    val hasAudioPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    SwaraScreen(
        state = uiState,
        hasAudioPermission = hasAudioPermission,
        onDraftChange = viewModel::updateDraft,
        onSend = viewModel::sendDraft,
        onPickModel = viewModel::requestModelPicker,
        onPickDocuments = viewModel::requestDocumentPicker,
        onStartVoice = { viewModel.startVoiceInput(hasAudioPermission) },
        onStopSpeaking = viewModel::stopSpeaking,
        onSpeakMessage = viewModel::speakMessage,
        onShowLibrary = { showLibrarySheet.value = true },
        onHideLibrary = { showLibrarySheet.value = false },
        showLibrarySheet = showLibrarySheet.value,
        onToggleDocumentScope = viewModel::toggleDocumentScope,
        onSetAutoSpeak = viewModel::setAutoSpeak,
        onSetMaxChunks = viewModel::setMaxChunks,
        onDeleteDocument = viewModel::deleteDocument,
        onSetEmergencyCategory = viewModel::setEmergencyCategory,
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
    onPickDocuments: () -> Unit,
    onStartVoice: () -> Unit,
    onStopSpeaking: () -> Unit,
    onSpeakMessage: (ChatMessage) -> Unit,
    onShowLibrary: () -> Unit,
    onHideLibrary: () -> Unit,
    showLibrarySheet: Boolean,
    onToggleDocumentScope: (String) -> Unit,
    onSetAutoSpeak: (Boolean) -> Unit,
    onSetMaxChunks: (Int) -> Unit,
    onDeleteDocument: (String) -> Unit,
    onSetEmergencyCategory: (EmergencyCategory) -> Unit,
    onSetResponseMode: (ResponseMode) -> Unit
) {
    if (showLibrarySheet) {
        ModalBottomSheet(
            onDismissRequest = onHideLibrary,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            LibrarySheet(
                state = state,
                hasAudioPermission = hasAudioPermission,
                onPickModel = onPickModel,
                onPickDocuments = onPickDocuments,
                onToggleDocumentScope = onToggleDocumentScope,
                onSetAutoSpeak = onSetAutoSpeak,
                onSetMaxChunks = onSetMaxChunks,
                onDeleteDocument = onDeleteDocument
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ChatTopBar(
                state = state,
                onShowLibrary = onShowLibrary
            )
        },
        bottomBar = {
            ComposerPanel(
                draft = state.draft,
                settings = state.settings,
                isBusy = state.isBusy,
                voiceState = state.voiceState,
                onDraftChange = onDraftChange,
                onSend = onSend,
                onStartVoice = onStartVoice,
                onStopSpeaking = onStopSpeaking,
                onSetEmergencyCategory = onSetEmergencyCategory,
                onSetResponseMode = onSetResponseMode
            )
        }
    ) { padding ->
        ChatPanel(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            onPickModel = onPickModel,
            onPickDocuments = onPickDocuments,
            onSpeakMessage = onSpeakMessage,
            onSetEmergencyCategory = onSetEmergencyCategory,
            onSetResponseMode = onSetResponseMode
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    state: MainUiState,
    onShowLibrary: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        CenterAlignedTopAppBar(
            modifier = Modifier.statusBarsPadding(),
            colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            ),
            navigationIcon = {
                IconButton(onClick = onShowLibrary) {
                    Icon(Icons.Rounded.Menu, contentDescription = "Open Swara kit")
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Swara",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    StatusPill(
                        label = topBarSubtitle(state),
                        color = statusColor(state.modelState)
                    )
                }
            },
            actions = {
                Column(
                    modifier = Modifier.padding(end = 16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Powered by Gemma 4",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    StatusDot(state.modelState)
                }
            }
        )
    }
}

@Composable
private fun StatusPill(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.14f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun StatusDot(modelState: ModelState) {
    Box(
        modifier = Modifier
            .padding(end = 16.dp)
            .size(8.dp)
            .clip(CircleShape)
            .background(statusColor(modelState))
    )
}

@Composable
private fun ChatPanel(
    state: MainUiState,
    modifier: Modifier = Modifier,
    onPickModel: () -> Unit,
    onPickDocuments: () -> Unit,
    onSpeakMessage: (ChatMessage) -> Unit,
    onSetEmergencyCategory: (EmergencyCategory) -> Unit,
    onSetResponseMode: (ResponseMode) -> Unit
) {
    val messages = state.messages
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.background,
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    MaterialTheme.colorScheme.background
                )
            )
        )
    ) {
        if (messages.isEmpty()) {
            EmptyChatState(
                state = state,
                onPickModel = onPickModel,
                onPickDocuments = onPickDocuments,
                onSetEmergencyCategory = onSetEmergencyCategory,
                onSetResponseMode = onSetResponseMode
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    if (!state.statusMessage.isNullOrBlank()) {
                        InlineBanner(text = state.statusMessage)
                    }
                }
                items(messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        evidence = state.evidenceByMessage[message.id].orEmpty(),
                        onSpeakMessage = onSpeakMessage
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmptyChatState(
    state: MainUiState,
    onPickModel: () -> Unit,
    onPickDocuments: () -> Unit,
    onSetEmergencyCategory: (EmergencyCategory) -> Unit,
    onSetResponseMode: (ResponseMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "One calm voice when the network goes silent",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = emptyStateSubtitle(state),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        EmergencyCategorySelector(
            selectedCategory = state.settings.selectedCategory,
            onSelect = onSetEmergencyCategory
        )
        Spacer(modifier = Modifier.height(12.dp))
        ResponseModeSelector(
            selectedMode = state.settings.responseMode,
            onSelect = onSetResponseMode
        )
        Spacer(modifier = Modifier.height(22.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AssistChip(
                onClick = onPickModel,
                label = { Text(if (state.modelState is ModelState.Ready) "Replace model" else "Import model") },
                leadingIcon = { Icon(Icons.Rounded.GraphicEq, contentDescription = null) }
            )
            AssistChip(
                onClick = onPickDocuments,
                label = { Text(if (state.documents.isEmpty()) "Add survival pack" else "Add more packs") },
                leadingIcon = { Icon(Icons.Rounded.FolderOpen, contentDescription = null) }
            )
        }
    }
}

@Composable
private fun InlineBanner(text: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.44f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
        )
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    evidence: List<RetrievalResult>,
    onSpeakMessage: (ChatMessage) -> Unit
) {
    val isAssistant = message.role == Role.ASSISTANT
    val bubbleShape = if (isAssistant) {
        RoundedCornerShape(topStart = 12.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 24.dp)
    } else {
        RoundedCornerShape(topStart = 24.dp, topEnd = 12.dp, bottomEnd = 24.dp, bottomStart = 24.dp)
    }
    val bubbleColor = if (isAssistant) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.94f)
    }
    val borderColor = if (isAssistant) {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    }
    val label = if (isAssistant) "Swara" else "You"
    val bubbleWidth = if (isAssistant) 0.95f else 0.78f

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isAssistant) Alignment.Start else Alignment.End
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(bubbleWidth),
            shape = bubbleShape,
            color = bubbleColor,
            tonalElevation = if (isAssistant) 0.dp else 2.dp,
            shadowElevation = 0.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MarkdownMessageText(
                    rawText = message.text,
                    color = if (isAssistant) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
                if (message.citations.isNotEmpty()) {
                    CitationRow(
                        citations = message.citations,
                        evidence = evidence
                    )
                }
                if (isAssistant) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.isStreaming) {
                            Text(
                                text = "Generating...",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                        IconButton(
                            onClick = { onSpeakMessage(message) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = "Speak reply",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CitationRow(
    citations: List<com.swara.app.data.model.CitationRef>,
    evidence: List<RetrievalResult>
) {
    val groupedCitations = remember(citations, evidence) { groupCitations(citations, evidence) }
    var selectedCitation by remember(groupedCitations) { mutableStateOf<CitationGroup?>(null) }

    if (selectedCitation != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedCitation = null },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            CitationSheet(
                citation = selectedCitation!!,
                totalSources = groupedCitations.size
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Sources",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            groupedCitations.forEach { citation ->
                Surface(
                    modifier = Modifier.clickable { selectedCitation = citation },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .defaultMinSize(minHeight = 44.dp)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(
                            modifier = Modifier.width(132.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = buildCitationLabel(citation.documentName, citation.pageNumber),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = buildCitationMeta(citation.excerptCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormattedMessageText(
    text: String,
    color: Color
) {
    val normalized = remember(text) { normalizeMessage(text) }
    val blocks = remember(normalized) { normalized.split("\n\n").filter { it.isNotBlank() } }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        blocks.forEach { block ->
            val trimmed = block.trim()
            if (trimmed.lines().size > 1 && trimmed.lines().all { isBulletLine(it) }) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    trimmed.lines().filter { it.isNotBlank() }.forEach { line ->
                        RichTextLine(
                            text = "• ${cleanBullet(line)}",
                            color = color
                        )
                    }
                }
            } else {
                trimmed.lines().forEach { line ->
                    RichTextLine(
                        text = if (line.isBlank()) " " else line,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
private fun RichTextLine(
    text: String,
    color: Color
) {
    Text(
        text = buildRichText(text),
        style = MaterialTheme.typography.bodyLarge,
        color = color,
        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.18f
    )
}

private fun buildRichText(text: String): AnnotatedString {
    val headingMatch = Regex("""^([A-Z][A-Za-z0-9 /&()'-]{1,40}:)\s*(.*)$""").matchEntire(text.trim())
    if (headingMatch != null) {
        val heading = headingMatch.groupValues[1]
        val rest = headingMatch.groupValues[2]
        return buildAnnotatedString {
            pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold))
            append(heading)
            pop()
            if (rest.isNotBlank()) {
                append(" ")
                append(rest)
            }
        }
    }
    return buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            when {
                text.startsWith("**", cursor) -> {
                    val end = text.indexOf("**", cursor + 2)
                    if (end == -1) {
                        append(text.substring(cursor))
                        break
                    }
                    pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold))
                    append(text.substring(cursor + 2, end))
                    pop()
                    cursor = end + 2
                }

                text[cursor] == '*' -> {
                    val end = text.indexOf('*', cursor + 1)
                    if (end == -1) {
                        append(text.substring(cursor))
                        break
                    }
                    pushStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic
                        )
                    )
                    append(text.substring(cursor + 1, end))
                    pop()
                    cursor = end + 1
                }

                else -> {
                    append(text[cursor])
                    cursor += 1
                }
            }
        }
    }
}

private fun normalizeMessage(raw: String): String {
    return raw
        .replace("\\n", "\n")
        .replace("\\t", "    ")
        .replace(Regex("\r\n?"), "\n")
        .replace(Regex("[ \t]+\n"), "\n")
        .let(::normalizeSectionBlocks)
        .replace(Regex("\n{3,}"), "\n\n")
        .replace(Regex("\\*\\*(\\s*)"), "**")
        .let(::stripOrphanMarkdownMarkers)
        .let(::stripPromptEchoIntro)
        .trim()
}

private fun isBulletLine(line: String): Boolean {
    val trimmed = line.trim()
    return trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ")
}

private fun cleanBullet(line: String): String {
    return line.trim().removePrefix("- ").removePrefix("* ").removePrefix("• ").trim()
}

private fun normalizeSectionBlocks(text: String): String {
    return text
        .replace(
            Regex("""\*{1,3}\s*([A-Z][A-Za-z0-9 /&()'-]{1,40}):\s*"""),
            "\n\n$1:\n"
        )
        .replace(
            Regex("""(?<=[\.\)])\s*([A-Z][A-Za-z0-9 /&()'-]{1,40}:)\s*(?=[A-Z])"""),
            "\n\n$1 "
        )
        .replace(
            Regex("""(?m)^([A-Z][A-Za-z0-9 /&()'-]{1,40}:)(?=[A-Z])"""),
            "$1 "
        )
        .replace(
            Regex("""(?<=[A-Za-z]):(?=[A-Za-z])"""),
            ": "
        )
}

private fun stripOrphanMarkdownMarkers(text: String): String {
    return text.lines().joinToString("\n") { line ->
        val trimmed = line.trimStart()
        when {
            trimmed.startsWith("**") && !trimmed.drop(2).contains("**") -> {
                line.replaceFirst("**", "")
            }
            trimmed.startsWith("*") && !trimmed.drop(1).contains("*") -> {
                line.replaceFirst("*", "")
            }
            else -> line
        }
    }
}

private fun stripPromptEchoIntro(text: String): String {
    var result = text.trim()
    val introPatterns = listOf(
        Regex("^The provided context consists of excerpts from (a|the) document titled\\s*", RegexOption.IGNORE_CASE),
        Regex("^The provided context consists of excerpts from\\s*", RegexOption.IGNORE_CASE),
        Regex("^The context consists of excerpts from\\s*", RegexOption.IGNORE_CASE),
        Regex("^Here are the key points from the text:\\s*", RegexOption.IGNORE_CASE),
        Regex("^Based on the provided context,\\s*", RegexOption.IGNORE_CASE),
        Regex("^According to the provided context,\\s*", RegexOption.IGNORE_CASE)
    )
    introPatterns.forEach { pattern ->
        result = result.replaceFirst(pattern, "")
    }
    result = result.replace(Regex("^\""), "")
    result = result.replace(Regex("^`[^`]+`\\)\\.?\\s*"), "")
    result = result.replace(Regex("^\\([^)]*\\)\\.?\\s*"), "")
    result = result.replace(Regex("^['\"]?\\s*"), "")
    return result.trimStart()
}

private data class CitationGroup(
    val documentId: String,
    val documentName: String,
    val pageNumber: Int?,
    val chunkIds: List<String>,
    val excerptCount: Int,
    val bestScore: Double,
    val matchedTerms: List<String>,
    val previews: List<EvidencePreview>
)

private data class EvidencePreview(
    val title: String,
    val snippet: String,
    val score: Double
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CitationSheet(
    citation: CitationGroup,
    totalSources: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "Evidence",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = buildCitationLabel(citation.documentName, citation.pageNumber),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Referenced in $totalSources source group${if (totalSources == 1) "" else "s"} for this reply.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (citation.matchedTerms.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        citation.matchedTerms.take(6).forEach { term ->
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = { Text(term) }
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Retrieved excerpts",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    citation.previews.forEachIndexed { index, preview ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${index + 1}. ${preview.title}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = preview.snippet,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Score ${"%.1f".format(preview.score)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun ComposerPanel(
    draft: String,
    settings: com.swara.app.data.model.AppSettings,
    isBusy: Boolean,
    voiceState: VoiceState,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onStartVoice: () -> Unit,
    onStopSpeaking: () -> Unit,
    onSetEmergencyCategory: (EmergencyCategory) -> Unit,
    onSetResponseMode: (ResponseMode) -> Unit
) {
    val isSpeaking = voiceState == VoiceState.Speaking
    val actionLabel = when {
        isSpeaking -> "Speaking reply"
        voiceState == VoiceState.Listening -> "Listening"
        voiceState == VoiceState.Transcribing -> "Transcribing"
        isBusy -> "Generating reply"
        else -> ""
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (voiceState is VoiceState.Error) {
                InlineBanner(text = voiceState.message)
            }
            EmergencyContextCard(
                selectedCategory = settings.selectedCategory,
                responseMode = settings.responseMode,
                onSelectCategory = onSetEmergencyCategory,
                onSelectMode = onSetResponseMode
            )
            if (actionLabel.isNotBlank()) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 54.dp, bottom = 1.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = if (isSpeaking) onStopSpeaking else onStartVoice,
                    modifier = Modifier.size(46.dp),
                    enabled = !isBusy || isSpeaking,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Rounded.Stop else Icons.Rounded.Mic,
                        contentDescription = if (isSpeaking) "Stop speaking" else "Start voice input"
                    )
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                    )
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = onDraftChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Describe the emergency or ask what to do next") },
                        shape = RoundedCornerShape(20.dp),
                        minLines = 1,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        enabled = !isBusy && voiceState != VoiceState.Listening && voiceState != VoiceState.Transcribing,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        )
                    )
                }
                FilledIconButton(
                    onClick = onSend,
                    modifier = Modifier.size(46.dp),
                    enabled = draft.isNotBlank() && !isBusy,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send message"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmergencyContextCard(
    selectedCategory: EmergencyCategory,
    responseMode: ResponseMode,
    onSelectCategory: (EmergencyCategory) -> Unit,
    onSelectMode: (ResponseMode) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Emergency context",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            EmergencyCategorySelector(
                selectedCategory = selectedCategory,
                onSelect = onSelectCategory
            )
            ResponseModeSelector(
                selectedMode = responseMode,
                onSelect = onSelectMode
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmergencyCategorySelector(
    selectedCategory: EmergencyCategory,
    onSelect: (EmergencyCategory) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EmergencyCategory.entries.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onSelect(category) },
                label = { Text(category.label) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResponseModeSelector(
    selectedMode: ResponseMode,
    onSelect: (ResponseMode) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ResponseMode.entries.forEach { mode ->
            FilterChip(
                selected = selectedMode == mode,
                onClick = { onSelect(mode) },
                label = { Text(mode.label) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibrarySheet(
    state: MainUiState,
    hasAudioPermission: Boolean,
    onPickModel: () -> Unit,
    onPickDocuments: () -> Unit,
    onToggleDocumentScope: (String) -> Unit,
    onSetAutoSpeak: (Boolean) -> Unit,
    onSetMaxChunks: (Int) -> Unit,
    onDeleteDocument: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "Swara Kit",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Manage model, survival packs, and offline emergency guidance behavior.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SectionCard(title = "Model") {
            SheetActionRow(
                icon = Icons.Rounded.GraphicEq,
                title = when (state.modelState) {
                    ModelState.NotInstalled -> "No model imported"
                    ModelState.Validating -> "Validating model"
                    ModelState.Loading -> "Loading model"
                    is ModelState.Ready -> "Model ready"
                    is ModelState.Error -> "Model error"
                },
                subtitle = when (val modelState = state.modelState) {
                    is ModelState.Ready -> modelState.modelPath.substringAfterLast('/')
                    is ModelState.Error -> modelState.message
                    else -> "Gemma 4 LiteRT-LM"
                },
                actionLabel = if (state.modelState is ModelState.Ready) "Replace" else "Import",
                onClick = onPickModel
            )
        }

        SectionCard(title = "Survival Packs") {
            SheetActionRow(
                icon = Icons.Rounded.Add,
                title = if (state.documents.isEmpty()) "Add your first knowledge pack" else "Import more knowledge packs",
                subtitle = "PDF, TXT, and MD files supported",
                actionLabel = "Import",
                onClick = onPickDocuments
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (state.documents.isEmpty()) {
                Text(
                    text = "No survival packs indexed yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.documents.forEach { document ->
                        DocumentRow(
                            document = document,
                            selected = state.settings.documentScope.isEmpty() ||
                                state.settings.documentScope.contains(document.id),
                            onToggleDocumentScope = onToggleDocumentScope,
                            onDeleteDocument = onDeleteDocument
                        )
                    }
                }
            }
        }

        SectionCard(title = "Responses") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-speak replies",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (hasAudioPermission) {
                            "Read Swara guidance aloud after each response."
                        } else {
                            "Microphone permission missing. Voice input may fail."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = state.settings.autoSpeakResponses,
                    onCheckedChange = onSetAutoSpeak
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Retrieved pack context: ${state.settings.maxContextChunks} chunks",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = state.settings.maxContextChunks.toFloat(),
                onValueChange = { onSetMaxChunks(it.toInt().coerceIn(1, 8)) },
                valueRange = 1f..8f,
                steps = 5
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        ),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SheetActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        AssistChip(
            onClick = onClick,
            label = { Text(actionLabel) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DocumentRow(
    document: DocumentRecord,
    selected: Boolean,
    onToggleDocumentScope: (String) -> Unit,
    onDeleteDocument: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.58f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = document.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = documentStatusLabel(document),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Checkbox(
                    checked = selected,
                    onCheckedChange = { onToggleDocumentScope(document.id) }
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selected,
                    onClick = { onToggleDocumentScope(document.id) },
                    label = { Text(if (selected) "Included in guidance" else "Excluded") }
                )
                AssistChip(
                    onClick = { onDeleteDocument(document.id) },
                    label = { Text("Delete") }
                )
            }
        }
    }
}

private fun documentStatusLabel(document: DocumentRecord): String {
    val base = when (document.status) {
        DocumentStatus.IMPORTING -> "Indexing"
        DocumentStatus.READY -> "Ready"
        DocumentStatus.ERROR -> "Failed"
    }
    val pageText = document.pageCount?.let { " • $it pages" }.orEmpty()
    return "$base$pageText"
}

private fun groupCitations(
    citations: List<com.swara.app.data.model.CitationRef>,
    evidence: List<RetrievalResult>
): List<CitationGroup> {
    if (citations.isEmpty()) return emptyList()
    val evidenceByKey = evidence.groupBy { "${it.documentId}:${it.pageNumber ?: -1}" }
    return citations
        .groupBy { "${it.documentId}:${it.pageNumber ?: -1}" }
        .values
        .mapNotNull { refs ->
            val primary = refs.maxByOrNull { it.score } ?: return@mapNotNull null
            val key = "${primary.documentId}:${primary.pageNumber ?: -1}"
            val matchingEvidence = evidenceByKey[key].orEmpty().sortedByDescending { it.score }
            CitationGroup(
                documentId = primary.documentId,
                documentName = primary.documentName,
                pageNumber = primary.pageNumber,
                chunkIds = refs.map { it.chunkId }.distinct(),
                excerptCount = primary.excerptCount.coerceAtLeast(refs.size),
                bestScore = matchingEvidence.firstOrNull()?.score ?: primary.score,
                matchedTerms = (matchingEvidence.flatMap { it.matchedTerms } + refs.flatMap { it.matchedTerms }).distinct(),
                previews = buildEvidencePreviews(matchingEvidence, refs)
            )
        }
        .sortedWith(
            compareByDescending<CitationGroup> { it.bestScore }
                .thenBy { it.documentName.lowercase() }
                .thenBy { it.pageNumber ?: Int.MAX_VALUE }
        )
}

private fun buildCitationMeta(count: Int): String {
    return if (count == 1) "1 excerpt" else "$count excerpts"
}

private fun buildEvidencePreviews(
    evidence: List<RetrievalResult>,
    citations: List<com.swara.app.data.model.CitationRef>
): List<EvidencePreview> {
    if (evidence.isNotEmpty()) {
        return evidence.take(3).map { result ->
            EvidencePreview(
                title = buildCitationLabel(result.documentName, result.pageNumber),
                snippet = result.snippet.ifBlank { result.text }.condenseForPreview(),
                score = result.score
            )
        }
    }
    return citations.take(3).map { citation ->
        EvidencePreview(
            title = buildCitationLabel(citation.documentName, citation.pageNumber),
            snippet = citation.snippet.condenseForPreview(),
            score = citation.score
        )
    }
}

private fun String.condenseForPreview(): String {
    return replace(Regex("\\s+"), " ")
        .trim()
        .ifBlank { "No excerpt preview available yet." }
}

private fun emptyStateSubtitle(state: MainUiState): String {
    return when {
        state.modelState !is ModelState.Ready -> "Import your Gemma LiteRT model so Swara can run fully offline."
        state.documents.isEmpty() -> "Add a survival pack, choose an emergency category, then start the first response flow."
        else -> "Choose the emergency context below, then ask what is happening and what to do next."
    }
}

private fun topBarSubtitle(state: MainUiState): String {
    return when {
        state.modelState !is ModelState.Ready -> "Offline model needed"
        state.documents.isEmpty() -> "Add survival packs"
        state.isBusy -> "Preparing guidance..."
        else -> "${state.settings.selectedCategory.label} • ${state.settings.responseMode.label}"
    }
}

@Composable
private fun statusColor(modelState: ModelState): Color {
    return when (modelState) {
        ModelState.NotInstalled -> MaterialTheme.colorScheme.error
        ModelState.Validating, ModelState.Loading -> MaterialTheme.colorScheme.tertiary
        is ModelState.Ready -> MaterialTheme.colorScheme.primary
        is ModelState.Error -> MaterialTheme.colorScheme.error
    }
}

private fun buildCitationLabel(documentName: String, pageNumber: Int?): String {
    val page = pageNumber?.let { " p.$it" }.orEmpty()
    return "$documentName$page"
}
