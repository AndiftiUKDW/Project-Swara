package com.swara.app.services

import com.swara.app.data.model.AppSettings
import com.swara.app.data.model.EmergencyCategory
import com.swara.app.data.repo.SurvivalPackRepository
import kotlinx.coroutines.runBlocking
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
    private val survivalPackRepository: SurvivalPackRepository,
    private val chatService: GemmaChatService
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
        val target = requestLine.split(" ").getOrNull(1).orEmpty()
        val path = URLDecoder.decode(target.substringBefore("?"), StandardCharsets.UTF_8.name())
        val query = target.substringAfter("?", "")
        when (path) {
            "/", "" -> sendText(socket, "text/html; charset=utf-8", askHtml())
            "/ask" -> {
                val question = queryParameter(query, "q")
                sendText(
                    socket,
                    "text/html; charset=utf-8",
                    askHtml(question = question, answer = question.takeIf { it.isNotBlank() }?.let(::answerQuestion))
                )
            }
            "/guides" -> sendText(socket, "text/html; charset=utf-8", guidesHtml())
            "/guides.json" -> sendText(socket, "application/json; charset=utf-8", survivalPackRepository.rawCatalogJson())
            else -> sendNotFound(socket)
        }
    }

    private fun askHtml(question: String = "", answer: String? = null): String {
        val conversation = buildString {
            append("""<section class="bubble intro"><h2>Tell Swara what is happening.</h2><p>Gemma runs on the host phone. Use Guide for static steps.</p></section>""")
            if (question.isNotBlank()) {
                append("""<section class="bubble user"><h3>You</h3><p>${question.escapeHtml()}</p></section>""")
            }
            answer?.let {
                append("""<section class="bubble swara"><h3>Swara</h3><pre>${it.escapeHtml()}</pre></section>""")
            }
        }
        return page(
            title = "Ask Swara",
            active = "ask",
            body = conversation,
            composer = true,
            question = question
        )
    }

    private fun guidesHtml(): String {
        val cards = survivalPackRepository.allPacks().joinToString("\n") { pack ->
            """
            <article class="guide-card">
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
        return page(
            title = "Guide",
            active = "guide",
            body = """<section class="bubble intro"><h2>Library of available emergency guides.</h2><p>Static guidance served by the host phone.</p></section>$cards""",
            composer = false
        )
    }

    private fun page(
        title: String,
        active: String,
        body: String,
        composer: Boolean,
        question: String = ""
    ): String {
        val bottomPadding = if (composer) "106px" else "74px"
        return """
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Swara Web Host</title>
  <style>
    :root{color-scheme:dark;--bg:#071923;--card:#211f25;--line:#244454;--ink:#f3fbfa;--muted:#c7d0cf;--accent:#a8ddd8;--purple:#5a3a91}
    *{box-sizing:border-box}body{margin:0;background:#161616;color:var(--ink);font-family:Arial,sans-serif;line-height:1.45}
    .app{min-height:100vh;display:flex;flex-direction:column;max-width:460px;margin:0 auto;background:var(--bg);position:relative}
    header{position:sticky;top:0;background:#071923;padding:22px 20px 12px;z-index:2}
    .top{display:flex;align-items:center;gap:18px}.hamb{font-size:30px}.title{font-size:30px;font-weight:800}
    main{padding:18px 20px $bottomPadding;display:flex;flex-direction:column;gap:16px;flex:1}
    .bubble,.guide-card{background:var(--card);border-radius:26px;padding:18px}.user{background:var(--purple);margin-left:64px}.intro p{color:var(--muted)}
    .swara pre{white-space:pre-wrap;font-family:inherit;font-size:16px;margin:0}.eyebrow{color:var(--accent);font-weight:700;margin:0 0 6px}
    h1,h2,h3{margin:.1rem 0 .7rem}p{color:var(--ink)}summary{cursor:pointer;font-weight:700;margin-top:12px}li{margin:8px 0}
    .tabs{position:fixed;left:50%;bottom:0;transform:translateX(-50%);width:min(460px,100%);background:#211f25;display:flex;justify-content:space-around;padding:10px 10px 12px}
    .tabs a{color:var(--muted);text-decoration:none;font-weight:700;padding:8px 18px;border-radius:18px}.tabs a.on{background:#5b5270;color:white}
    form{position:fixed;left:50%;bottom:66px;transform:translateX(-50%);width:min(460px,100%);background:#071923;padding:8px 12px;display:flex;gap:10px;align-items:center}
    input{flex:1;padding:15px 16px;border-radius:22px;border:2px solid var(--accent);background:#071923;color:var(--ink);font-size:16px;min-width:0}
    button{width:58px;height:58px;border-radius:22px;border:0;background:var(--purple);color:white;font-size:28px;font-weight:800}
  </style>
</head>
<body>
  <div class="app">
    <header><div class="top"><div class="hamb">☰</div><div class="title">$title</div></div></header>
    <main>$body</main>
    ${if (composer) """<form action="/ask" method="get"><input name="q" value="${question.escapeHtml()}" placeholder="Describe the emergency"><button>➤</button></form>""" else ""}
    <nav class="tabs"><a class="${if (active == "guide") "on" else ""}" href="/guides">Guide</a><a class="${if (active == "ask") "on" else ""}" href="/">Ask</a></nav>
  </div>
</body>
</html>
        """.trimIndent()
    }

    private fun answerQuestion(question: String): String {
        return runCatching {
            runBlocking {
                val category = inferCategory(question)
                val pack = survivalPackRepository.findFor(category)
                val settings = AppSettings(selectedCategory = category)
                val pieces = StringBuilder()
                chatService.streamReply(
                    history = emptyList(),
                    question = question,
                    retrieval = emptyList(),
                    survivalPack = pack,
                    settings = settings
                ).collect { message ->
                    if (message.text.startsWith(pieces.toString())) {
                        pieces.clear()
                        pieces.append(message.text)
                    } else {
                        pieces.append(message.text)
                    }
                }
                pieces.toString().ifBlank { "No answer generated." }
            }
        }.getOrElse { it.message ?: "Swara could not answer from the host phone." }
    }

    private fun queryParameter(query: String, key: String): String {
        return query.split("&")
            .mapNotNull {
                val parts = it.split("=", limit = 2)
                if (parts.size == 2 && parts[0] == key) parts[1] else null
            }
            .firstOrNull()
            ?.replace("+", " ")
            ?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) }
            .orEmpty()
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
        val header = "HTTP/1.1 $status\r\n" +
            "Content-Type: $contentType\r\n" +
            "Content-Length: $length\r\n" +
            "Cache-Control: no-store\r\n" +
            "Connection: close\r\n\r\n"
        write(header.toByteArray(StandardCharsets.UTF_8))
    }

    private fun localIpAddress(): String? {
        return NetworkInterface.getNetworkInterfaces().toList().asSequence()
            .flatMap { it.inetAddresses.toList().asSequence() }
            .filterIsInstance<Inet4Address>()
            .firstOrNull { !it.isLoopbackAddress && it.hostAddress?.startsWith("127.") != true }
            ?.hostAddress
    }

    private fun String.escapeHtml(): String {
        return replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
    }

    private fun inferCategory(question: String): EmergencyCategory {
        val text = question.lowercase()
        return when {
            listOf("bleed", "blood", "burn", "unconscious", "faint", "pain", "injury", "wound", "heat").any { it in text } -> EmergencyCategory.MEDICAL
            listOf("fire", "smoke", "flame", "burning", "gas").any { it in text } -> EmergencyCategory.FIRE
            listOf("flood", "water rising", "river", "rain", "drowning").any { it in text } -> EmergencyCategory.FLOOD
            listOf("earthquake", "shake", "shaking", "aftershock", "rubble").any { it in text } -> EmergencyCategory.EARTHQUAKE
            listOf("following", "threat", "violence", "attack", "fight", "rob", "unsafe").any { it in text } -> EmergencyCategory.VIOLENCE
            listOf("lost", "stranded", "forest", "trail", "battery low", "can't find").any { it in text } -> EmergencyCategory.LOST
            else -> EmergencyCategory.OTHER
        }
    }
}
