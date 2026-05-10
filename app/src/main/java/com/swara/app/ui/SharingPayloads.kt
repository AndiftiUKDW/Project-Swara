package com.swara.app.ui

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.swara.app.data.model.ChatMessage
import com.swara.app.data.model.EmergencyCategory
import com.swara.app.data.model.ResponseMode
import com.swara.app.data.model.Role
import com.swara.app.data.model.SurvivalPackGuide
import java.text.Normalizer
import java.util.Locale

fun buildShareTextForPack(pack: SurvivalPackGuide): String {
    return buildString {
        appendLine("SWARA PACK")
        appendLine(pack.title)
        appendLine("CATEGORY: ${pack.category.label}")
        appendLine("VERSION: ${pack.version}")
        appendLine()
        appendLine("QUICK HELP")
        pack.quickHelp.forEachIndexed { index, item -> appendLine("${index + 1}. $item") }
        appendLine()
        appendLine("DO NOT")
        pack.doNot.forEachIndexed { index, item -> appendLine("${index + 1}. $item") }
        appendLine()
        appendLine("SOURCE: ${pack.sourceLabel}")
    }.trim()
}

fun buildQrPayloadForPack(pack: SurvivalPackGuide): String {
    val lines = mutableListOf<String>()
    lines += "SWARA/PACK/1"
    lines += "CAT:${pack.category.name}"
    pack.quickHelp.take(5).forEachIndexed { index, item ->
        lines += "${index + 1}:${item.toQrLine()}"
    }
    val warning = pack.doNot.firstOrNull()?.toQrLine()
    lines += if (warning.isNullOrBlank()) {
        "END:MOVE AWAY FROM DANGER IF SAFE"
    } else {
        "END:${warning.take(QR_LINE_LIMIT)}"
    }
    return lines.joinToString("\n")
}

fun buildShareTextForMessage(
    message: ChatMessage,
    category: EmergencyCategory,
    mode: ResponseMode
): String {
    return buildString {
        appendLine("SWARA RESPONSE")
        appendLine("CATEGORY: ${category.label}")
        appendLine("MODE: ${mode.label}")
        appendLine()
        appendLine(message.text.trim())
    }.trim()
}

fun buildQrPayloadForMessage(
    message: ChatMessage,
    conversation: List<ChatMessage>,
    category: EmergencyCategory,
    mode: ResponseMode
): String {
    val readable = buildReadableChatQrPayload(
        message = message,
        conversation = conversation,
        category = category,
        mode = mode
    )
    if (readable.length <= QR_CHAT_LIMIT) return readable

    val lastUser = conversation
        .takeWhile { it.id != message.id }
        .lastOrNull { it.role == Role.USER }
    return buildCompactChatQrPayload(
        userMessage = lastUser,
        assistantMessage = message,
        category = category,
        mode = mode
    )
}

private fun buildReadableChatQrPayload(
    message: ChatMessage,
    conversation: List<ChatMessage>,
    category: EmergencyCategory,
    mode: ResponseMode
): String {
    val messageIndex = conversation.indexOfFirst { it.id == message.id }.takeIf { it >= 0 }
        ?: conversation.lastIndex
    val window = conversation
        .take(messageIndex + 1)
        .takeLast(6)
        .filter { it.text.isNotBlank() }
    val lines = mutableListOf<String>()
    lines += "SWARA CHAT"
    lines += "CAT: ${category.name} | MODE: ${if (mode == ResponseMode.QUICK_HELP) "QUICK" else "DETAIL"}"
    window.forEach { chat ->
        lines += ""
        lines += if (chat.role == Role.USER) "[USER] :" else "[SWARA] :"
        val compactLines = if (chat.role == Role.USER) {
            listOf(chat.text.toQrLine().take(120))
        } else {
            chat.text.toAssistantQrLines(maxLines = 6)
        }
        lines += compactLines
    }
    lines += "END:IF DANGER INCREASES, MOVE TO SAFER PLACE AND SEEK HELP IF REACHABLE"
    return lines.joinToString("\n")
}

private fun buildCompactChatQrPayload(
    userMessage: ChatMessage?,
    assistantMessage: ChatMessage,
    category: EmergencyCategory,
    mode: ResponseMode
): String {
    val lines = mutableListOf<String>()
    lines += "SWARA CHAT"
    lines += "CAT: ${category.name} | MODE: ${if (mode == ResponseMode.QUICK_HELP) "QUICK" else "DETAIL"}"
    lines += ""
    lines += "[USER] :"
    lines += (userMessage?.text ?: "EMERGENCY QUESTION").toQrLine().take(120)
    lines += ""
    lines += "[SWARA] :"
    lines += assistantMessage.text.toCompactAssistantQrLines()
    lines += "END:IF DANGER INCREASES, MOVE TO SAFER PLACE AND SEEK HELP IF REACHABLE"
    return lines.joinToString("\n")
}

private fun String.toAssistantQrLines(maxLines: Int): List<String> {
    return normalizedEmergencyLines()
        .map { it.toQrLine() }
        .filter { it.isNotBlank() }
        .filterNot { it in EMERGENCY_SECTION_NAMES }
        .take(maxLines)
        .map { line -> line.take(QR_LINE_LIMIT) }
}

private fun String.toCompactAssistantQrLines(): List<String> {
    val sections = extractEmergencySections()
    val risk = sections["RISK"]?.firstMeaningfulLine()
    val situation = sections["SITUATION"]?.firstMeaningfulLine()
    val actions = sections["DO NOW"].orEmpty()
        .meaningfulContentLines()
        .take(3)
    val avoid = sections["DO NOT"]?.firstMeaningfulLine()

    val lines = mutableListOf<String>()
    risk?.let { lines += "RISK: ${it.toQrLine().take(58)}" }
    situation?.let { lines += "SIT: ${it.toQrLine().take(60)}" }
    actions.forEachIndexed { index, action ->
        lines += "DO: ${action.toQrLine().take(QR_LINE_LIMIT - 4)}"
    }
    avoid?.let { lines += "NO: ${it.toQrLine().take(62)}" }
    if (lines.isNotEmpty()) return lines.take(6)
    return toAssistantQrLines(maxLines = 4)
}

private fun String.extractEmergencySections(): Map<String, List<String>> {
    val result = linkedMapOf<String, MutableList<String>>()
    var current: String? = null
    normalizedEmergencyLines().forEach { rawLine ->
        val line = rawLine.trim()
        val heading = EMERGENCY_SECTION_NAMES.firstOrNull { it == line.uppercase(Locale.US) }
        if (heading != null) {
            current = heading
            result.getOrPut(heading) { mutableListOf() }
        } else {
            current?.let { result.getOrPut(it) { mutableListOf() }.add(line) }
        }
    }
    return result
}

private fun String.normalizedEmergencyLines(): List<String> {
    return replace(
        Regex("""(?i)\b(RISK|SITUATION|DO NOW|DO NOT|NEXT QUESTION)\b\s*:?\s*""")
    ) { match -> "\n${match.groupValues[1].uppercase(Locale.US)}\n" }
        .replace(Regex("""(?m)(?<!^)(?<!\n)(\d+\.\s*)"""), "\n$1")
        .lines()
}

private fun List<String>.firstMeaningfulLine(): String? {
    return meaningfulContentLines().firstOrNull()
}

private fun List<String>.meaningfulContentLines(): List<String> {
    return map { it.trim() }
        .filter { it.isNotBlank() }
        .map { it.replace(Regex("""^\d+\.\s*"""), "") }
        .filter { it.isNotBlank() }
        .filterNot { it.uppercase(Locale.US) in EMERGENCY_SECTION_NAMES }
}

fun generateQrBitmap(payload: String, sizePx: Int = 768): Bitmap {
    val hints = mapOf(
        EncodeHintType.CHARACTER_SET to "US-ASCII",
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
        EncodeHintType.MARGIN to 2
    )
    val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
    val pixels = IntArray(sizePx * sizePx)
    for (y in 0 until sizePx) {
        for (x in 0 until sizePx) {
            pixels[y * sizePx + x] = if (matrix[x, y]) Color.BLACK else Color.WHITE
        }
    }
    return Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888).also {
        it.setPixels(pixels, 0, sizePx, 0, 0, sizePx, sizePx)
    }
}

private fun String.toQrLine(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
    return normalized
        .uppercase(Locale.US)
        .replace(Regex("[^A-Z0-9 .,:;!?/()+\\-]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .take(QR_LINE_LIMIT)
}

private const val QR_LINE_LIMIT = 72
private const val QR_CHAT_LIMIT = 900
private val EMERGENCY_SECTION_NAMES = setOf("RISK", "SITUATION", "DO NOW", "DO NOT", "NEXT QUESTION")
