import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import net.lingala.zip4j.ZipFile
import java.io.File
import java.net.InetAddress

fun main(args: Array<String>) {
  val port = if (args.isNotEmpty()) args[0].toIntOrNull() ?: 8080 else 8080
  val baseDir = File(".").absoluteFile
  val ipAddress = InetAddress.getLocalHost().hostAddress
  println("Server running at http://$ipAddress:$port")

  embeddedServer(Netty, port = port) {
    routing {
      get("/") {
        listDirectory(call, baseDir)
      }

      get("/browse/{path...}") {
        val path = call.parameters.getAll("path")?.joinToString(File.separator) ?: ""
        val requestedFile = File(baseDir, path)
        if (requestedFile.isDirectory) {
          listDirectory(call, requestedFile)
        } else {
          call.respond(HttpStatusCode.NotFound)
        }
      }

      get("/download/{path...}") {
        val path = call.parameters.getAll("path")?.joinToString(File.separator) ?: ""
        val requestedFile = File(baseDir, path)
        if (requestedFile.exists() && requestedFile.isFile) {
          call.respondFile(requestedFile)
        } else {
          call.respond(HttpStatusCode.NotFound)
        }
      }

      get("/download-zip/{path...}") {
        val path = call.parameters.getAll("path")?.joinToString(File.separator) ?: ""
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
            call.respondFile(tempZipFile)
          } finally {
            tempZipFile.delete()
          }
        } else {
          call.respond(HttpStatusCode.NotFound)
        }
      }
    }
  }.start(wait = true)
}

private suspend fun listDirectory(
  call: ApplicationCall,
  dir: File,
) {
  val baseDir = File(".").absoluteFile
  val relativePath = dir.relativeTo(baseDir).path
  call.respondHtml {
    head {
      title("File Share - ${if (relativePath.isEmpty()) "/" else relativePath}")
      link(rel = "stylesheet", href = "https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css")
      meta(name = "viewport", content = "width=device-width, initial-scale=1, shrink-to-fit=no")
    }
    body {
      div(classes = "container mt-4") {
        h1 { +"File Share" }
        h5 {
          +"Current Directory: ${if (relativePath.isEmpty()) "/" else relativePath}"
        }

        if (relativePath.isNotEmpty()) {
          val parentPath = dir.parentFile?.relativeTo(baseDir)?.path ?: ""
          a(href = if (parentPath.isEmpty()) "/" else "/browse/$parentPath", classes = "btn btn-secondary mb-3") { +"../" }
        }

        table(classes = "table table-hover") {
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
    }
  }
}
