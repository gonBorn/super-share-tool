package server

import io.ktor.http.*
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import net.lingala.zip4j.ZipFile
import state.AppState
import utils.IpAddressUtil.getLocalIpAddress
import java.io.File
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

val connections = ConcurrentHashMap.newKeySet<DefaultWebSocketSession>()
private var server: NettyApplicationEngine? = null

suspend fun broadcast(message: String) {
  connections.forEach { session ->
    session.send(message)
  }
}

fun startServer(port: Int) {
  val ipAddress = getLocalIpAddress()
  println("Server running at http://$ipAddress:$port")

  server =
    embeddedServer(Netty, port) {
      install(WebSockets)
      install(ContentNegotiation) {
        json()
      }

      routing {
        staticFiles("/static", File(AppState.baseDir.value, "static/webapp/build/static"))

        get("/") {
          call.respondFile(File(AppState.baseDir.value, "static/webapp/build/index.html"))
        }

        get("/browse/{...}") {
          call.respondFile(File(AppState.baseDir.value, "static/webapp/build/index.html"))
        }

        post("/upload") {
          val multipart = call.receiveMultipart()
          var path = ""

          multipart.forEachPart { part ->
            when (part) {
              is PartData.FormItem -> {
                if (part.name == "path") {
                  path = part.value
                }
              }
              is PartData.FileItem -> {
                val fileName = part.originalFileName as String
                val baseDir = AppState.baseDir.value
                val targetDir = if (path.isBlank() || path == "/") baseDir else File(baseDir, path)
                if (!targetDir.exists()) {
                  targetDir.mkdirs()
                }
                val file = File(targetDir, fileName)
                part.streamProvider().use { input ->
                  file.outputStream().buffered().use { output ->
                    input.copyTo(output)
                  }
                }
                broadcast("UPLOAD: $fileName")
              }
              else -> {}
            }
            part.dispose()
          }
          call.respond(HttpStatusCode.OK)
        }

        get("/api/files/{path...}") {
          val path = call.parameters.getAll("path")?.joinToString(File.separator) ?: ""
          val baseDir = AppState.baseDir.value
          val requestedFile = File(baseDir, path)
          if (requestedFile.isDirectory) {
            val files =
              requestedFile
                .listFiles()
                ?.filter { !it.isHidden }
                ?.map {
                  FileInfo(it.name, it.relativeTo(baseDir).path, it.isDirectory, if (it.isFile) it.length() else 0)
                }?.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
            call.respond(files ?: emptyList())
          } else {
            call.respond(HttpStatusCode.NotFound)
          }
        }

        get("/api/files") {
          val baseDir = AppState.baseDir.value
          val files =
            baseDir
              .listFiles()
              ?.filter { !it.isHidden }
              ?.map {
                FileInfo(it.name, it.relativeTo(baseDir).path, it.isDirectory, if (it.isFile) it.length() else 0)
              }?.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
          call.respond(files ?: emptyList())
        }

        get("/download/{path...}") {
          val path = call.parameters.getAll("path")?.joinToString(File.separator) ?: ""
          val baseDir = AppState.baseDir.value
          val requestedFile = File(baseDir, path)
          if (requestedFile.exists() && requestedFile.isFile) {
            broadcast("DOWNLOAD: ${requestedFile.name}")
            call.respondFile(requestedFile)
          } else {
            call.respond(HttpStatusCode.NotFound)
          }
        }

        get("/download-zip/{path...}") {
          val path = call.parameters.getAll("path")?.joinToString(File.separator) ?: ""
          val baseDir = AppState.baseDir.value
          val requestedDir = File(baseDir, path)
          if (requestedDir.exists() && requestedDir.isDirectory) {
            val tempZipFile = File.createTempFile(requestedDir.name, ".zip")
            try {
              ZipFile(tempZipFile).addFolder(requestedDir)
              call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition
                  .Attachment
                  .withParameter(
                    ContentDisposition.Parameters.FileName,
                    "${requestedDir.name}.zip",
                  ).toString(),
              )
              broadcast("DOWNLOAD_ZIP: ${requestedDir.name}")
              call.respondFile(tempZipFile)
            } finally {
              tempZipFile.delete()
            }
          } else {
            call.respond(HttpStatusCode.NotFound)
          }
        }

        webSocket("/ws") {
          connections += this
          try {
            for (frame in incoming) {
              if (frame is Frame.Text) {
                val text = frame.readText()
                val remoteHost = call.request.local.remoteHost
                if (text == "CLEAR") {
                  broadcast("CLEAR")
                } else {
                  val timestamp = LocalDateTime.now()
                  broadcast("[$timestamp] [$remoteHost]: $text")
                }
              }
            }
          } finally {
            connections -= this
          }
        }
      }
    }.start(wait = true)
}

fun stopServer() {
  server?.stop(1, 5, TimeUnit.SECONDS)
  server = null
}
