package com.swara.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.HtmlInline
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text as MdText
import org.commonmark.node.ThematicBreak
import org.commonmark.parser.Parser

private sealed interface RichBlock {
    data class HeadingBlock(val level: Int, val text: AnnotatedString) : RichBlock
    data class ParagraphBlock(val text: AnnotatedString) : RichBlock
    data class BulletListBlock(val items: List<ListItemBlock>) : RichBlock
    data class OrderedListBlock(val startNumber: Int, val items: List<ListItemBlock>) : RichBlock
    data class QuoteBlock(val blocks: List<RichBlock>) : RichBlock
    data class CodeBlock(val code: String) : RichBlock
    data object DividerBlock : RichBlock
}

private data class ListItemBlock(
    val blocks: List<RichBlock>
)

@Composable
fun MarkdownMessageText(
    rawText: String,
    color: Color
) {
    val normalized = remember(rawText) { normalizeMarkdownInput(rawText) }
    val blocks = remember(normalized) { parseMarkdownBlocks(normalized) }

    if (blocks.isEmpty()) {
        PlainMessageText(rawText = normalized.ifBlank { rawText }, color = color)
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        blocks.forEach { block ->
            RenderBlock(
                block = block,
                color = color
            )
        }
    }
}

@Composable
private fun RenderBlock(
    block: RichBlock,
    color: Color,
    modifier: Modifier = Modifier
) {
    when (block) {
        is RichBlock.HeadingBlock -> MarkdownHeading(
            level = block.level,
            text = block.text,
            color = color,
            modifier = modifier
        )

        is RichBlock.ParagraphBlock -> MarkdownParagraph(
            text = block.text,
            color = color,
            modifier = modifier
        )

        is RichBlock.BulletListBlock -> MarkdownBulletList(
            items = block.items,
            color = color,
            modifier = modifier
        )

        is RichBlock.OrderedListBlock -> MarkdownOrderedList(
            startNumber = block.startNumber,
            items = block.items,
            color = color,
            modifier = modifier
        )

        is RichBlock.QuoteBlock -> MarkdownQuoteBlock(
            blocks = block.blocks,
            color = color,
            modifier = modifier
        )

        is RichBlock.CodeBlock -> MarkdownCodeBlock(
            code = block.code,
            color = color,
            modifier = modifier
        )

        RichBlock.DividerBlock -> HorizontalDivider(
            modifier = modifier.padding(vertical = 2.dp),
            color = color.copy(alpha = 0.14f)
        )
    }
}

@Composable
private fun MarkdownHeading(
    level: Int,
    text: AnnotatedString,
    color: Color,
    modifier: Modifier = Modifier
) {
    val style = when (level) {
        1 -> MaterialTheme.typography.headlineSmall
        2 -> MaterialTheme.typography.titleLarge
        3 -> MaterialTheme.typography.titleMedium
        else -> MaterialTheme.typography.titleSmall
    }
    Text(
        text = text,
        style = style,
        color = color,
        modifier = modifier
    )
}

@Composable
private fun MarkdownParagraph(
    text: AnnotatedString,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (text.text.isBlank()) return
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = color,
        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.18f,
        modifier = modifier
    )
}

@Composable
private fun MarkdownBulletList(
    items: List<ListItemBlock>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEach { item ->
            ListRow(
                marker = "\u2022",
                blocks = item.blocks,
                color = color
            )
        }
    }
}

@Composable
private fun MarkdownOrderedList(
    startNumber: Int,
    items: List<ListItemBlock>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEachIndexed { index, item ->
            ListRow(
                marker = "${startNumber + index}.",
                blocks = item.blocks,
                color = color
            )
        }
    }
}

@Composable
private fun ListRow(
    marker: String,
    blocks: List<RichBlock>,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = marker,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            modifier = Modifier.width(20.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            blocks.forEach { block ->
                RenderBlock(block = block, color = color)
            }
        }
    }
}

@Composable
private fun MarkdownQuoteBlock(
    blocks: List<RichBlock>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .background(
                    color = color.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(999.dp)
                )
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            blocks.forEach { block ->
                RenderBlock(block = block, color = color.copy(alpha = 0.92f))
            }
        }
    }
}

@Composable
private fun MarkdownCodeBlock(
    code: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (code.isBlank()) return
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = color.copy(alpha = 0.08f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = code.trim(),
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            color = color,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.15f
        )
    }
}

@Composable
private fun PlainMessageText(
    rawText: String,
    color: Color
) {
    val normalized = remember(rawText) {
        rawText
            .replace("\\n", "\n")
            .replace("\\t", "    ")
            .replace("\\r", "\n")
            .replace(Regex("\\r\\n?"), "\n")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }
    if (normalized.isBlank()) return
    Text(
        text = normalized,
        style = MaterialTheme.typography.bodyLarge,
        color = color,
        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.18f
    )
}

private fun parseMarkdownBlocks(normalized: String): List<RichBlock> {
    if (normalized.isBlank()) return emptyList()
    return runCatching {
        Parser.builder().build().parse(normalized)
            .children()
            .mapNotNull(::toRichBlock)
            .toList()
    }.getOrElse { emptyList() }
}

private fun toRichBlock(node: Node): RichBlock? {
    return when (node) {
        is Heading -> {
            val text = buildInlineText(node)
            if (text.text.isBlank()) null else RichBlock.HeadingBlock(node.level, text)
        }

        is Paragraph -> {
            val text = buildInlineText(node)
            if (text.text.isBlank()) null else RichBlock.ParagraphBlock(text)
        }

        is BulletList -> {
            val items = node.children().mapNotNull(::toListItemBlock).toList()
            if (items.isEmpty()) null else RichBlock.BulletListBlock(items)
        }

        is OrderedList -> {
            val items = node.children().mapNotNull(::toListItemBlock).toList()
            if (items.isEmpty()) null else RichBlock.OrderedListBlock(node.startNumber, items)
        }

        is BlockQuote -> {
            val blocks = node.children().mapNotNull(::toRichBlock).toList()
            if (blocks.isEmpty()) null else RichBlock.QuoteBlock(blocks)
        }

        is FencedCodeBlock -> RichBlock.CodeBlock(node.literal.orEmpty())
        is IndentedCodeBlock -> RichBlock.CodeBlock(node.literal.orEmpty())
        is ThematicBreak -> RichBlock.DividerBlock

        else -> {
            val text = buildInlineText(node)
            if (text.text.isBlank()) null else RichBlock.ParagraphBlock(text)
        }
    }
}

private fun toListItemBlock(node: Node): ListItemBlock? {
    if (node !is ListItem) return null
    val childBlocks = node.children().mapNotNull(::toRichBlock).toList()
    if (childBlocks.isNotEmpty()) return ListItemBlock(childBlocks)

    val fallback = buildInlineText(node)
    return if (fallback.text.isBlank()) null else {
        ListItemBlock(listOf(RichBlock.ParagraphBlock(fallback)))
    }
}

private fun buildInlineText(node: Node): AnnotatedString {
    return buildAnnotatedString {
        appendInline(node)
    }
}

private fun AnnotatedString.Builder.appendInline(node: Node?) {
    var current = node?.firstChild
    if (current == null) {
        when (node) {
            is MdText -> append(node.literal)
            is Code -> {
                pushStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                )
                append(node.literal)
                pop()
            }

            is HtmlInline -> append(node.literal)
        }
        return
    }

    while (current != null) {
        when (current) {
            is MdText -> append(current.literal)
            is SoftLineBreak -> append(" ")
            is HardLineBreak -> append("\n")

            is Emphasis -> {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Medium))
                appendInline(current)
                pop()
            }

            is StrongEmphasis -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold))
                appendInline(current)
                pop()
            }

            is Code -> {
                pushStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                )
                append(current.literal)
                pop()
            }

            is Link -> appendInline(current)
            is HtmlInline -> append(current.literal)
            else -> appendInline(current)
        }
        current = current.next
    }
}

private fun Node.children(): Sequence<Node> = sequence {
    var child = firstChild
    while (child != null) {
        yield(child)
        child = child.next
    }
}

private fun normalizeMarkdownInput(raw: String): String {
    return raw
        .replace("\\n", "\n")
        .replace("\\t", "    ")
        .replace("\\r", "\n")
        .replace(Regex("\\r\\n?"), "\n")
        .replace(Regex("[ \\t]+\\n"), "\n")
        .let(::stripPromptEchoIntro)
        .let(::normalizeEmergencyResponseSpacing)
        .let(::convertSectionsToMarkdown)
        .let(::dedupeEmergencySections)
        .let(::normalizeMarkdownLists)
        .let(::normalizeQuotesAndRules)
        .let(::stripUnmatchedInlineMarkers)
        .let(::stripDanglingFormattingTokens)
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()
}

private fun normalizeEmergencyResponseSpacing(text: String): String {
    val sectionNames = "(RISK|SITUATION|DO NOW|DO NOT|NEXT QUESTION)"
    return text
        .replace(Regex("""(?i)\b$sectionNames\s*:?\s*""")) { match ->
            "\n\n${match.groupValues[1].uppercase()}\n"
        }
        .replace(Regex("""(?<=[a-zA-Z)])\.(?=$sectionNames\b)"""), ".\n\n")
        .replace(Regex("""(?m)(?<!^)(?<!\n)(\d+\.\s*)"""), "\n$1")
        .replace(Regex("""(?m)(\d+)\.(?=\S)"""), "$1. ")
        .replace(Regex("""(?m)^(\d+\.)\s*"""), "$1 ")
        .replace(Regex("""[ \t]{2,}"""), " ")
}

private fun convertSectionsToMarkdown(text: String): String {
    return text
        .replace(
            Regex("""\*{1,3}\s*([A-Z][A-Za-z0-9 /&()'-]{1,40}):\s*"""),
            "\n\n## $1\n"
        )
        .replace(
            Regex("""(?m)^\s*\*{1,3}\s*([A-Z][A-Za-z0-9 /&()'-]{1,40})\s*$"""),
            "## $1"
        )
        .replace(
            Regex("""(?m)^([A-Z][A-Za-z0-9 /&()'-]{1,40}):\s*(.+)$"""),
            "## $1\n$2"
        )
        .replace(
            Regex("""(?m)^\s*(RISK|SITUATION|DO NOW|DO NOT|NEXT QUESTION)\s*$"""),
            "## $1"
        )
        .replace(Regex("""(?<=[\.\)])\s*([A-Z][A-Za-z0-9 /&()'-]{1,40}:)\s*(?=[A-Z])""")) { match ->
            "\n\n## ${match.groupValues[1].removeSuffix(":")}\n"
        }
        .replace(Regex("""(?m)(?<!\n)(##\s)"""), "\n\n$1")
}

private fun dedupeEmergencySections(text: String): String {
    val headingRegex = Regex("""^##\s+(RISK|SITUATION|DO NOW|DO NOT|NEXT QUESTION)\s*$""")
    val punctuationOnlyRegex = Regex("""^[\s.:;,-]+$""")
    val seen = mutableSetOf<String>()
    var skipDuplicateNoise = false
    return text.lines().mapNotNull { line ->
        val heading = headingRegex.matchEntire(line.trim())?.groupValues?.getOrNull(1)
        if (heading != null) {
            skipDuplicateNoise = false
            if (!seen.add(heading)) {
                skipDuplicateNoise = true
                return@mapNotNull null
            }
        }
        if (skipDuplicateNoise && (line.isBlank() || punctuationOnlyRegex.matches(line))) {
            return@mapNotNull null
        }
        skipDuplicateNoise = false
        line
    }.joinToString("\n")
}

private fun normalizeMarkdownLists(text: String): String {
    return text
        .replace(Regex("""(?m)^\s*[-*]\s+"""), "- ")
        .replace(Regex("""(?m)^\s*\d+\.\s+""")) { match -> match.value.trimStart() }
        .replace(Regex("""(?<=\S)\s+-\s+(?=[A-Z0-9])"""), "\n- ")
        .replace(Regex("""(?<=[\.\)])-\s+(?=[A-Z0-9])"""), "\n- ")
        .replace(Regex("""(?<=[a-z0-9])\.\s*-\s+(?=[A-Z0-9])""", RegexOption.IGNORE_CASE), ".\n- ")
}

private fun normalizeQuotesAndRules(text: String): String {
    return text
        .replace(Regex("""(?m)^\s*>{2,}\s*"""), "> ")
        .replace(Regex("""(?m)^\s*[-*_]{3,}\s*$"""), "\n---\n")
}

private fun stripUnmatchedInlineMarkers(text: String): String {
    return text.lines().joinToString("\n") { line ->
        var current = line
        val trimmed = current.trimStart()
        current = current.replace(Regex("""^(\s*)\*{2,}(?=\S)"""), "$1")
        if (trimmed.startsWith("**") && !trimmed.drop(2).contains("**")) {
            current = current.replaceFirst("**", "")
        }
        if (current.trimStart().startsWith("*") && !current.trimStart().drop(1).contains("*")) {
            current = current.replaceFirst("*", "")
        }
        current.replace(Regex("""\*\*(\s*$)"""), "$1")
    }
}

private fun stripDanglingFormattingTokens(text: String): String {
    return text
        .replace(Regex("""(?m)^(\s*)[*_`]{1,3}(?=\S)"""), "$1")
        .replace(Regex("""(?m)(?<=\S)[*_`]{1,3}$"""), "")
        .replace(Regex("""(?m)^(\s*)[*_`]{1,3}\s*$"""), "")
}

private fun stripPromptEchoIntro(text: String): String {
    var result = text.trim()
    val introPatterns = listOf(
        Regex("^The provided context consists of excerpts from (a|the) document titled\\s*", RegexOption.IGNORE_CASE),
        Regex("^The provided context consists of excerpts from\\s*", RegexOption.IGNORE_CASE),
        Regex("^The context consists of excerpts from\\s*", RegexOption.IGNORE_CASE),
        Regex("^Here are the key points from the text:\\s*", RegexOption.IGNORE_CASE),
        Regex("^Here are the main points of the document:\\s*", RegexOption.IGNORE_CASE),
        Regex("^The main points of the document are:\\s*", RegexOption.IGNORE_CASE),
        Regex("^Based on the provided context,\\s*", RegexOption.IGNORE_CASE),
        Regex("^According to the provided context,\\s*", RegexOption.IGNORE_CASE)
    )
    introPatterns.forEach { pattern ->
        result = result.replaceFirst(pattern, "")
    }
    result = result.replace(Regex("^\\\""), "")
    result = result.replace(Regex("^`[^`]+`\\)\\.?\\s*"), "")
    result = result.replace(Regex("^\\([^)]*\\)\\.?\\s*"), "")
    result = result.replace(Regex("^['\\\"]?\\s*"), "")
    result = result.replace(Regex("""(?m)^\s*\[Source:[^\]]*]\s*$"""), "")
    return result.trimStart()
}
