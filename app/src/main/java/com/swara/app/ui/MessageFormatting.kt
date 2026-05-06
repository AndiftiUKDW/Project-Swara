package com.swara.app.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

fun normalizeModelText(raw: String): String {
    return raw
        .replace("\\n", "\n")
        .replace("\\t", "\t")
        .replace(Regex("(?<=[\\.:])\\*{1,2}(?=[A-Z])"), "\n\n$0")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
}

fun buildFormattedAssistantText(text: String): AnnotatedString {
    val bulletLines = text.lines().joinToString("\n") { line ->
        when {
            line.startsWith("* ") -> "• ${line.removePrefix("* ")}"
            line.startsWith("- ") -> "• ${line.removePrefix("- ")}"
            else -> line
        }
    }
    return buildAnnotatedString {
        var index = 0
        while (index < bulletLines.length) {
            when {
                bulletLines.startsWith("**", index) -> {
                    val end = bulletLines.indexOf("**", index + 2)
                    if (end == -1) {
                        append(bulletLines.substring(index))
                        break
                    }
                    pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold))
                    append(bulletLines.substring(index + 2, end))
                    pop()
                    index = end + 2
                }

                bulletLines[index] == '*' -> {
                    val end = bulletLines.indexOf('*', index + 1)
                    if (end == -1) {
                        append(bulletLines.substring(index))
                        break
                    }
                    pushStyle(SpanStyle(fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic))
                    append(bulletLines.substring(index + 1, end))
                    pop()
                    index = end + 1
                }

                else -> {
                    append(bulletLines[index])
                    index += 1
                }
            }
        }
    }
}
