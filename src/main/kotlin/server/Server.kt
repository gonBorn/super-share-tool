package server

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
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
            listDirectory(call, requestedFile, baseDir)
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
  val relativePath = dir.relativeTo(baseDir).path
  call.respondHtml {
    head {
      title("File Share - ${if (relativePath.isEmpty()) "/" else relativePath}")
      link(rel = "stylesheet", href = "https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css")
      meta(name = "viewport", content = "width=device-width, initial-scale=1, shrink-to-fit=no")
    }
    body {
      div(classes = "container mt-4") {
        div(classes = "row") {
          div(classes = "col-md-8") {
            h1 { +"File Share" }
            h5 {
              +"Current Directory: ${if (relativePath.isEmpty()) "/" else relativePath}"
            }

            if (relativePath.isNotEmpty()) {
              val parentPath = dir.parentFile?.relativeTo(baseDir)?.path ?: ""
              a(href = if (parentPath.isEmpty()) "/" else "/browse/$parentPath", classes = "btn btn-secondary mb-3") { +"../" }
            }

            form(action = "/upload", method = FormMethod.post, encType = FormEncType.multipartFormData) {
              div(classes = "form-group") {
                input(type = InputType.file, name = "file", classes = "form-control-file")
              }
              button(type = ButtonType.submit, classes = "btn btn-primary") { +"Upload File" }
            }

            hr()

            table(classes = "table table-hover mt-4") {
              thead {
                tr {
                  th { +"Type" }
                  th { +"Name" }
                  th { +"Actions" }
                }
              }
              tbody {
                dir.listFiles()?.filter { !it.isHidden }?.sortedWith(compareBy({ !it.isDirectory }, { it.name }))?.forEach { file ->
                  tr {
                    td {
                      if (file.isDirectory) {
                        +"📁"
                      } else {
                        +"📄"
                      }
                    }
                    td {
                      val link =
                        if (file.isDirectory) {
                          "/browse/${file.relativeTo(baseDir).path}"
                        } else {
                          "/download/${file.relativeTo(baseDir).path}"
                        }
                      a(href = link) { +file.name }
                    }
                    td {
                      if (file.isDirectory) {
                        a(
                          href = "/download-zip/${file.relativeTo(baseDir).path}",
                          classes = "btn btn-primary btn-sm",
                        ) { +"Download ZIP" }
                      }
                    }
                  }
                }
              }
            }
          }
          div(classes = "col-md-4") {
            h1 { +"Chat" }
            div(classes = "card") {
              div(classes = "card-body") {
                div {
                  id = "chat-messages"
                  classes = setOf("mb-3")
                  attributes["style"] = "height: 250px; overflow-y: scroll; border: 1px solid #ccc; padding: 10px; font-size: 0.9em;"
                }
                div(classes = "input-group") {
                  input(type = InputType.text, classes = "form-control") {
                    id = "message-input"
                  }
                  div(classes = "input-group-append") {
                    button(classes = "btn btn-primary") {
                      id = "send-button"
                      +"Send"
                    }
                  }
                }
                button(classes = "btn btn-secondary btn-sm mt-2") {
                  id = "clear-button"
                  +"Clear Chat"
                }
              }
            }
          }
        }
      }
      script {
        unsafe {
          +"""
            const ws = new WebSocket("ws://" + location.host + "/ws");
            const chatMessages = document.getElementById("chat-messages");
            const messageInput = document.getElementById("message-input");
            const sendButton = document.getElementById("send-button");
            const clearButton = document.getElementById("clear-button");

            function ipToColor(ip) {
                let hash = 0;
                for (let i = 0; i < ip.length; i++) {
                    hash = ip.charCodeAt(i) + ((hash << 5) - hash);
                }
                let color = '#';
                for (let i = 0; i < 3; i++) {
                    const value = (hash >> (i * 8)) & 0xFF;
                    color += ('00' + value.toString(16)).substr(-2);
                }
                return color;
            }

            ws.onmessage = function(event) {
                if (event.data === "CLEAR") {
                    chatMessages.innerHTML = "";
                } else {
                    const match = event.data.match(/^\[(.*?)\] \[(.*?)\]: (.*)$/);
                    if (match) {
                        const timestampStr = match[1];
                        const ip = match[2];
                        const message = match[3];

                        const date = new Date(timestampStr);
                        const formattedTime = date.getFullYear() + '/' +
                            ('0' + (date.getMonth() + 1)).slice(-2) + '/' +
                            ('0' + date.getDate()).slice(-2) + ' ' +
                            ('0' + date.getHours()).slice(-2) + ':' +
                            ('0' + date.getMinutes()).slice(-2) + ':' +
                            ('0' + date.getSeconds()).slice(-2);

                        const messageContainer = document.createElement("div");
                        
                        const timeElement = document.createElement("div");
                        timeElement.textContent = formattedTime;
                        timeElement.style.fontSize = "0.8em";
                        timeElement.style.color = "#888";
                        
                        const messageElement = document.createElement("div");
                        messageElement.textContent = "[" + ip + "]: " + message;
                        messageElement.style.color = ipToColor(ip);

                        messageContainer.appendChild(timeElement);
                        messageContainer.appendChild(messageElement);
                        chatMessages.appendChild(messageContainer);
                        chatMessages.scrollTop = chatMessages.scrollHeight;
                    }
                }
            };

            sendButton.onclick = function() {
                const message = messageInput.value;
                if (message.trim() !== "") {
                    ws.send(message);
                    messageInput.value = "";
                }
            };

            clearButton.onclick = function() {
                ws.send("CLEAR");
            };

            messageInput.addEventListener("keyup", function(event) {
                if (event.key === "Enter") {
                    sendButton.click();
                }
            });
          """
        }
      }
    }
  }
}
