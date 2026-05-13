package com.swara.app.services

import android.content.Context
import com.swara.app.data.repo.SurvivalPackRepository
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.concurrent.thread

data class DistributionServerState(
    val running: Boolean = false,
    val url: String? = null,
    val message: String = "Local distribution server stopped."
)

class DistributionServer(
    private val context: Context,
    private val modelManager: ModelManager,
    private val survivalPackRepository: SurvivalPackRepository
) {
    @Volatile private var serverSocket: ServerSocket? = null
    @Volatile private var worker: Thread? = null
    private val running = AtomicBoolean(false)

    fun start(): DistributionServerState {
        if (running.get()) return currentState("Server already running.")
        val socket = ServerSocket(0)
        running.set(true)
        serverSocket = socket
        worker = thread(name = "SwaraDistributionServer", isDaemon = true) {
            while (running.get()) {
                runCatching { socket.accept().use(::handleClient) }
            }
        }
        val url = "http://${localIpAddress() ?: "127.0.0.1"}:${socket.localPort}/"
        return DistributionServerState(
            running = true,
            url = url,
            message = "Connect the other device to the same hotspot or Wi-Fi, then open this URL."
        )
    }

    fun stop(): DistributionServerState {
        running.set(false)
        runCatching { serverSocket?.close() }
        serverSocket = null
        worker = null
        return DistributionServerState()
    }

    fun currentState(message: String = "Server running."): DistributionServerState {
        val socket = serverSocket
        val url = socket?.let { "http://${localIpAddress() ?: "127.0.0.1"}:${it.localPort}/" }
        return DistributionServerState(
            running = running.get() && socket != null,
            url = url,
            message = if (running.get() && socket != null) message else "Local distribution server stopped."
        )
    }

    private fun handleClient(socket: Socket) {
        val input = socket.getInputStream().bufferedReader()
        val requestLine = input.readLine().orEmpty()
        while (input.readLine()?.isNotEmpty() == true) {
            // Drain headers.
        }
        val path = requestLine.split(" ").getOrNull(1)?.substringBefore("?").orEmpty()
        when (URLDecoder.decode(path, StandardCharsets.UTF_8.name())) {
            "/", "" -> sendText(socket, "text/html; charset=utf-8", indexHtml())
            "/survival-pack.json" -> sendText(socket, "application/json; charset=utf-8", survivalPackRepository.rawCatalogJson())
            "/survival-pack.txt" -> sendText(socket, "text/plain; charset=utf-8", survivalPackText())
            "/app.apk" -> {
                val artifact = appInstallArtifact()
                when (artifact) {
                    is AppInstallArtifact.BundledUniversal -> sendAsset(
                        socket = socket,
                        assetPath = artifact.assetPath,
                        contentType = "application/vnd.android.package-archive",
                        downloadName = "Swara.apk"
                    )
                    is AppInstallArtifact.SingleApk -> sendFile(socket, artifact.file, "application/vnd.android.package-archive", "Swara.apk")
                    is AppInstallArtifact.SplitPackage -> sendNotFound(socket, "This sender install uses split APKs and no bundled universal APK is available. Download the APK package ZIP instead.")
                    AppInstallArtifact.Unavailable -> sendNotFound(socket, "Swara APK is unavailable from this install.")
                }
            }
            "/app-package.zip" -> {
                val artifact = appInstallArtifact()
                if (artifact is AppInstallArtifact.SplitPackage) {
                    sendSplitApkZip(socket, artifact)
                } else {
                    sendNotFound(socket, "APK package ZIP is only needed for split APK installs.")
                }
            }
            "/model" -> {
                val modelPath = modelManager.currentModelPath()
                val modelFile = modelPath?.let(::File)
                if (modelFile?.exists() == true) {
                    sendFile(socket, modelFile, "application/octet-stream", "swara-gemma-model.litertlm")
                } else {
                    sendNotFound(socket, "Model not imported on this device.")
                }
            }
            else -> sendNotFound(socket, "Not found.")
        }
    }

    private fun indexHtml(): String {
        val modelAvailable = modelManager.currentModelPath()?.let(::File)?.exists() == true
        val artifact = appInstallArtifact()
        val appDownload = when (artifact) {
            is AppInstallArtifact.BundledUniversal -> """<a href="/app.apk">Download Swara Universal APK</a>"""
            is AppInstallArtifact.SingleApk -> """<a href="/app.apk">Download Swara APK</a>"""
            is AppInstallArtifact.SplitPackage -> """
                <a href="/app-package.zip">Download Swara APK Package ZIP</a>
                <p class="muted">No bundled universal APK was found. This sender app is installed as split APKs, so a single browser APK would fail to install.</p>
            """.trimIndent()
            AppInstallArtifact.Unavailable -> """<p class="muted">Swara APK is unavailable from this install.</p>"""
        }
        return """
            <!doctype html>
            <html>
            <head>
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>Swara Local Share</title>
              <style>
                body{font-family:sans-serif;background:#071923;color:#eef7f6;padding:24px;line-height:1.45}
                a{display:block;margin:12px 0;padding:14px 16px;border:1px solid #8fd7d1;border-radius:14px;color:#eef7f6;text-decoration:none}
                .muted{color:#b8c8c7}
              </style>
            </head>
            <body>
              <h1>Swara Local Share</h1>
              <p class="muted">Download over local hotspot or Wi-Fi. Internet not required.</p>
              $appDownload
              <a href="/survival-pack.txt">Download Survival Pack TXT</a>
              <a href="/survival-pack.json">Download Survival Pack JSON</a>
              ${if (modelAvailable) """<a href="/model">Download Gemma Model</a>""" else """<p class="muted">Gemma model not imported on this sender device.</p>"""}
            </body>
            </html>
        """.trimIndent()
    }

    private fun survivalPackText(): String {
        return survivalPackRepository.allPacks().joinToString("\n\n") { pack ->
            buildString {
                appendLine(pack.title)
                appendLine("Category: ${pack.category.label}")
                appendLine("Updated: ${pack.lastUpdated}")
                appendLine("Quick help:")
                pack.quickHelp.forEachIndexed { index, item -> appendLine("${index + 1}. $item") }
                appendLine("Do not:")
                pack.doNot.forEachIndexed { index, item -> appendLine("${index + 1}. $item") }
            }.trim()
        }
    }

    private fun sendText(socket: Socket, contentType: String, body: String) {
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        writeHeaders(socket, "200 OK", contentType, bytes.size.toLong())
        socket.getOutputStream().write(bytes)
    }

    private fun sendFile(socket: Socket, file: File, contentType: String, downloadName: String = file.name) {
        if (!file.exists() || !file.canRead() || file.length() <= 0L) return sendNotFound(socket, "File unavailable.")
        writeHeaders(
            socket = socket,
            status = "200 OK",
            contentType = contentType,
            contentLength = file.length(),
            contentDisposition = "attachment; filename=\"$downloadName\""
        )
        BufferedInputStream(file.inputStream()).use { input ->
            input.copyTo(socket.getOutputStream())
            socket.getOutputStream().flush()
        }
    }

    private fun sendAsset(socket: Socket, assetPath: String, contentType: String, downloadName: String) {
        val length = runCatching { context.assets.openFd(assetPath).use { it.length } }.getOrNull()
        writeHeaders(
            socket = socket,
            status = "200 OK",
            contentType = contentType,
            contentLength = length,
            contentDisposition = "attachment; filename=\"$downloadName\""
        )
        context.assets.open(assetPath).use { input ->
            input.copyTo(socket.getOutputStream())
            socket.getOutputStream().flush()
        }
    }

    private fun sendSplitApkZip(socket: Socket, artifact: AppInstallArtifact.SplitPackage) {
        writeHeaders(
            socket = socket,
            status = "200 OK",
            contentType = "application/zip",
            contentLength = null,
            contentDisposition = "attachment; filename=\"Swara-apk-package.zip\""
        )
        ZipOutputStream(socket.getOutputStream()).use { zip ->
            artifact.files.forEachIndexed { index, file ->
                zip.putNextEntry(ZipEntry(if (index == 0) "base.apk" else "split-${index}.apk"))
                FileInputStream(file).use { input -> input.copyTo(zip) }
                zip.closeEntry()
            }
            zip.putNextEntry(ZipEntry("README.txt"))
            zip.write(
                """
                Swara APK package

                This sender device has Swara installed as split APKs.
                A single base.apk may show "problem parsing package" on the receiver.

                For demo install, prefer sharing a universal APK built from Android Studio/Gradle.
                This ZIP preserves all split APK files for future package-installer support.
                """.trimIndent().toByteArray(StandardCharsets.UTF_8)
            )
            zip.closeEntry()
            zip.flush()
        }
    }

    private fun sendNotFound(socket: Socket, message: String) {
        val body = message.toByteArray(StandardCharsets.UTF_8)
        writeHeaders(socket, "404 Not Found", "text/plain; charset=utf-8", body.size.toLong())
        socket.getOutputStream().write(body)
    }

    private fun writeHeaders(
        socket: Socket,
        status: String,
        contentType: String,
        contentLength: Long?,
        contentDisposition: String? = null
    ) {
        val disposition = contentDisposition?.let { "Content-Disposition: $it\r\n" }.orEmpty()
        val length = contentLength?.let { "Content-Length: $it\r\n" }.orEmpty()
        val headers = "HTTP/1.1 $status\r\n" +
            "Content-Type: $contentType\r\n" +
            length +
            disposition +
            "Connection: close\r\n\r\n"
        socket.getOutputStream().write(headers.toByteArray(StandardCharsets.UTF_8))
    }

    private fun appInstallArtifact(): AppInstallArtifact {
        if (assetExists(UNIVERSAL_APK_ASSET)) {
            return AppInstallArtifact.BundledUniversal(UNIVERSAL_APK_ASSET)
        }
        val base = File(context.applicationInfo.sourceDir)
        val splitFiles = context.applicationInfo.splitSourceDirs
            ?.map(::File)
            .orEmpty()
            .filter { it.exists() && it.canRead() && it.length() > 0L }
        if (!base.exists() || !base.canRead() || base.length() <= 0L) return AppInstallArtifact.Unavailable
        return if (splitFiles.isEmpty()) {
            AppInstallArtifact.SingleApk(base)
        } else {
            AppInstallArtifact.SplitPackage(listOf(base) + splitFiles)
        }
    }

    private fun assetExists(assetPath: String): Boolean {
        val folder = assetPath.substringBeforeLast("/", "")
        val fileName = assetPath.substringAfterLast("/")
        return runCatching {
            context.assets.list(folder).orEmpty().contains(fileName)
        }.getOrDefault(false)
    }

    private fun localIpAddress(): String? {
        return NetworkInterface.getNetworkInterfaces().toList()
            .flatMap { it.inetAddresses.toList() }
            .filterIsInstance<Inet4Address>()
            .firstOrNull { address ->
                val hostAddress = address.hostAddress.orEmpty()
                !address.isLoopbackAddress && !hostAddress.startsWith("169.254")
            }
            ?.hostAddress
            ?.lowercase(Locale.US)
    }

    private sealed interface AppInstallArtifact {
        data class BundledUniversal(val assetPath: String) : AppInstallArtifact
        data class SingleApk(val file: File) : AppInstallArtifact
        data class SplitPackage(val files: List<File>) : AppInstallArtifact
        data object Unavailable : AppInstallArtifact
    }

    private companion object {
        const val UNIVERSAL_APK_ASSET = "distribution/Swara.apk"
    }
}
