package com.swara.app.services

import com.swara.app.data.model.SurvivalPackGuide
import com.swara.app.data.repo.SurvivalPackRepository
import java.io.OutputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

data class WebHostServerState(
    val running: Boolean = false,
    val url: String? = null,
    val message: String = "Web Host stopped."
)

class WebHostServer(
    private val survivalPackRepository: SurvivalPackRepository
) {
    @Volatile private var serverSocket: ServerSocket? = null
    @Volatile private var worker: Thread? = null
    private val running = AtomicBoolean(false)

    fun start(): WebHostServerState {
        if (running.get()) return currentState("Web Host already running.")
        val socket = ServerSocket(0)
        running.set(true)
        serverSocket = socket
        worker = thread(name = "SwaraWebHostServer", isDaemon = true) {
            while (running.get()) {
                runCatching { socket.accept().use(::handleClient) }
            }
        }
        return WebHostServerState(
            running = true,
            url = "http://${localIpAddress() ?: "127.0.0.1"}:${socket.localPort}/",
            message = "Open this URL on a trusted phone connected to the same hotspot or Wi-Fi."
        )
    }

    fun stop(): WebHostServerState {
        running.set(false)
        runCatching { serverSocket?.close() }
        serverSocket = null
        worker = null
        return WebHostServerState()
    }

    fun currentState(message: String = "Web Host running."): WebHostServerState {
        val socket = serverSocket
        val url = socket?.let { "http://${localIpAddress() ?: "127.0.0.1"}:${it.localPort}/" }
        return WebHostServerState(
            running = running.get() && socket != null,
            url = url,
            message = if (running.get() && socket != null) message else "Web Host stopped."
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
            "/guides.json" -> sendText(socket, "application/json; charset=utf-8", guidesJson())
            else -> sendNotFound(socket)
        }
    }

    private fun indexHtml(): String {
        val cards = survivalPackRepository.allPacks().joinToString("\n") { pack ->
            """
            <article class="card">
              <p class="eyebrow">${pack.category.label}</p>
              <h2>${pack.title.escapeHtml()}</h2>
              <p>${pack.quickHelp.firstOrNull().orEmpty().escapeHtml()}</p>
              <details>
                <summary>Quick Help</summary>
                <ol>${pack.quickHelp.joinToString("") { "<li>${it.escapeHtml()}</li>" }}</ol>
              </details>
              <details>
                <summary>Do Not</summary>
                <ol>${pack.doNot.joinToString("") { "<li>${it.escapeHtml()}</li>" }}</ol>
              </details>
            </article>
            """.trimIndent()
        }
        return """
            <!doctype html>
            <html>
            <head>
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>Swara Web Host</title>
              <style>
                :root{color-scheme:dark;--bg:#071923;--card:#0c2230;--line:#244454;--ink:#f3fbfa;--muted:#b8c8c7;--accent:#a8ddd8}
                *{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--ink);font-family:system-ui,-apple-system,Segoe UI,sans-serif;line-height:1.45}
                header{position:sticky;top:0;background:rgba(7,25,35,.96);border-bottom:1px solid var(--line);padding:18px}
                main{padding:18px;display:grid;gap:16px}.brief{color:var(--muted);margin:4px 0 0}
                .card{background:var(--card);border:1px solid var(--line);border-radius:22px;padding:18px}
                .eyebrow{color:var(--accent);font-weight:700;margin:0 0 6px}h1,h2{margin:.1rem 0 .7rem}p{color:var(--muted)}
                summary{cursor:pointer;font-weight:700;margin-top:12px}li{margin:8px 0}
              </style>
            </head>
            <body>
              <header>
                <h1>Swara</h1>
                <p class="brief">Guide</p>
              </header>
              <main>
                <section class="card">
                  <h2>Library of available emergency guides.</h2>
                  <p>This web page is served by the host phone. Use it only on a trusted local network.</p>
                </section>
                $cards
              </main>
            </body>
            </html>
        """.trimIndent()
    }

    private fun guidesJson(): String {
        return survivalPackRepository.rawCatalogJson()
    }

    private fun sendText(socket: Socket, contentType: String, body: String) {
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        socket.getOutputStream().use { output ->
            output.writeHeader("200 OK", contentType, bytes.size)
            output.write(bytes)
        }
    }

    private fun sendNotFound(socket: Socket) {
        val body = "Not found.".toByteArray(StandardCharsets.UTF_8)
        socket.getOutputStream().use { output ->
            output.writeHeader("404 Not Found", "text/plain; charset=utf-8", body.size)
            output.write(body)
        }
    }

    private fun OutputStream.writeHeader(status: String, contentType: String, length: Int) {
        write(
            """
                HTTP/1.1 $status
                Content-Type: $contentType
                Content-Length: $length
                Connection: close

            """.trimIndent().replace("\n", "\r\n").toByteArray(StandardCharsets.UTF_8)
        )
    }

    private fun localIpAddress(): String? {
        val interfaces = NetworkInterface.getNetworkInterfaces().toList()
        return interfaces.asSequence()
            .flatMap { it.inetAddresses.toList().asSequence() }
            .filterIsInstance<Inet4Address>()
            .firstOrNull { !it.isLoopbackAddress && it.hostAddress?.startsWith("127.") != true }
            ?.hostAddress
    }

    private fun String.escapeHtml(): String {
        return replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }
}
