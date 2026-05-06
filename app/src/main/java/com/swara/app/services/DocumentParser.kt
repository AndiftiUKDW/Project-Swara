package com.swara.app.services

import android.content.Context
import com.swara.app.data.model.IngestionSource
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DocumentParser(
    private val context: Context
) {
    init {
        PDFBoxResourceLoader.init(context)
    }

    suspend fun parse(source: IngestionSource): ParsedDocument = withContext(Dispatchers.IO) {
        when {
            source.mimeType == "application/pdf" || source.displayName.endsWith(".pdf", ignoreCase = true) ->
                parsePdf(source)

            source.mimeType.startsWith("text/") || source.displayName.endsWith(".md", ignoreCase = true) ->
                parseText(source)

            else -> error("Unsupported document type: ${source.mimeType}")
        }
    }

    private fun parseText(source: IngestionSource): ParsedDocument {
        val text = context.contentResolver.openInputStream(source.uri)?.bufferedReader()?.use { it.readText() }
            ?: error("Failed to read ${source.displayName}")
        return ParsedDocument(
            pageCount = 1,
            pages = listOf(ParsedPage(pageNumber = 1, text = text))
        )
    }

    private fun parsePdf(source: IngestionSource): ParsedDocument {
        val inputStream = context.contentResolver.openInputStream(source.uri)
            ?: error("Failed to open ${source.displayName}")
        inputStream.use { stream ->
            PDDocument.load(stream).use { document ->
                val stripper = PDFTextStripper()
                val pages = (1..document.numberOfPages).map { pageNumber ->
                    stripper.startPage = pageNumber
                    stripper.endPage = pageNumber
                    ParsedPage(pageNumber = pageNumber, text = stripper.getText(document))
                }
                return ParsedDocument(
                    pageCount = document.numberOfPages,
                    pages = pages
                )
            }
        }
    }
}
