package server

import io.ktor.server.application.*
import io.ktor.server.html.*
import kotlinx.html.*
import java.io.File

suspend fun ApplicationCall.respondWithHtml(
  dir: File,
  baseDir: File,
) {
  val relativePath = dir.relativeTo(baseDir).path
  val alert = request.queryParameters["alert"]
  respondHtml {
    head {
      title("File Share - ${if (relativePath.isEmpty()) "/" else relativePath}")
      link(rel = "stylesheet", href = "https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css")
      meta(name = "viewport", content = "width=device-width, initial-scale=1, shrink-to-fit=no")
    }
    body {
      if (alert == "empty_folder") {
        script {
          unsafe {
            +"alert('This folder is empty and cannot be entered.');"
          }
        }
      }
      div(classes = "container mt-4") {
        div(classes = "row") {
          div(classes = "col-md-8") {
            h1 { +"File Share" }
            h5 {
              +"Current Directory: ${if (relativePath.isEmpty()) "/" else relativePath}"
            }

            if (relativePath.isNotEmpty()) {
              val parentPath = dir.parentFile?.relativeTo(baseDir)?.path ?: ""
              a(
                href = if (parentPath.isEmpty()) "/" else "/browse/$parentPath",
                classes = "btn btn-secondary mb-3",
              ) { +"../" }
            }

            form(action = "/upload", method = FormMethod.post, encType = FormEncType.multipartFormData) {
              id = "upload-form"
              div(classes = "form-group") {
                input(type = InputType.file, name = "file", classes = "form-control-file") {
                  id = "file-input"
                }
              }
              button(type = ButtonType.submit, classes = "btn btn-primary") { +"Upload File" }
            }

            div {
              id = "upload-progress-container"
              style = "display: none; margin-top: 10px; border: 1px solid #ccc; padding: 5px;"
              div(classes = "progress") {
                div {
                  id = "progress-bar"
                  classes = setOf("progress-bar")
                  attributes["role"] = "progressbar"
                  attributes["style"] = "width: 0%;"
                  attributes["aria-valuenow"] = "0"
                  attributes["aria-valuemin"] = "0"
                  attributes["aria-valuemax"] = "100"
                }
              }
              div {
                id = "progress-text"
                style = "text-align: center; margin-top: 5px;"
              }
              button(classes = "btn btn-danger btn-sm mt-2") {
                id = "cancel-upload-button"
                style = "display: none;"
                +"Cancel"
              }
            }

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
          div(classes = "col-md-4 d-flex flex-column") {
            h1 { +"Chat" }
            div(classes = "card flex-grow-1") {
              div(classes = "card-body d-flex flex-column") {
                div {
                  id = "chat-messages"
                  classes = setOf("mb-3 flex-grow-1")
                  attributes["style"] =
                    "overflow-y: scroll; padding: 10px; font-size: 0.9em; max-height: 70vh; min-height: 300px;"
                }
                div(classes = "input-group mt-auto") {
                  textArea(classes = "form-control") {
                    id = "message-input"
                    rows = "3"
                    placeholder = "Type here..."
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
      script(src = "/static/script.js") {}
    }
  }
}
