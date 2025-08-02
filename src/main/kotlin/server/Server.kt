package server

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.html.*
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

      routing {
        staticResources("/static", "static")
        get("/") {
          val baseDir = AppState.baseDir.value
          listDirectory(call, baseDir, baseDir)
        }

        post("/upload") {
          val multipart = call.receiveMultipart()
          val baseDir = AppState.baseDir.value
          multipart.forEachPart { part ->
            if (part is PartData.FileItem) {
              val fileName = part.originalFileName ?: "unknown"
              val file = File(baseDir, fileName)
              part.streamProvider().use { input ->
                file.outputStream().buffered().use { output ->
                  input.copyTo(output)
                }
              }
              broadcast("UPLOAD: $fileName")
            }
            part.dispose()
          }
          call.respondRedirect("/")
        }

        get("/browse/{path...}") {
          val path = call.parameters.getAll("path")?.joinToString(File.separator) ?: ""
          val baseDir = AppState.baseDir.value
          val requestedFile = File(baseDir, path)
          if (requestedFile.isDirectory) {
            if (requestedFile.listFiles()?.isEmpty() == true) {
              val parentPath = requestedFile.parentFile?.relativeTo(baseDir)?.path ?: ""
              val redirectUrl = if (parentPath.isEmpty()) "/" else "/browse/$parentPath"
              call.respondRedirect("$redirectUrl?alert=empty_folder")
            } else {
              listDirectory(call, requestedFile, baseDir)
            }
          } else {
            call.respond(HttpStatusCode.NotFound)
          }
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

private suspend fun listDirectory(
  call: ApplicationCall,
  dir: File,
  baseDir: File,
) {
  call.respondWithHtml(dir, baseDir)
}
