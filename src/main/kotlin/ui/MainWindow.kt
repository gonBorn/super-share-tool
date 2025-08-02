package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import service.WebSocketClient
import utils.IpAddressUtil.getLocalIpAddress
import java.awt.image.BufferedImage
import javax.swing.JFileChooser

@Composable
@Preview
fun mainWindow(
  port: Int,
  sharedDirectory: String,
  onDirectorySelected: (String) -> Unit,
  onCloseRequest: () -> Unit,
) {
  val messages by WebSocketClient.messages.collectAsState()
  var chatMessage by remember { mutableStateOf("") }

  LaunchedEffect(Unit) {
    WebSocketClient.start(port)
  }

  Window(
    onCloseRequest = onCloseRequest,
    title = "Super Share",
    state = rememberWindowState(width = 1200.dp, height = 800.dp),
  ) {
    MaterialTheme {
      SelectionContainer {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
          // Left Panel: Info and QR Code
          Card(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            elevation = 4.dp,
          ) {
            Column(
              modifier = Modifier.padding(16.dp).fillMaxSize(),
              verticalArrangement = Arrangement.Center,
            ) {
              val ipAddress = remember { getLocalIpAddress() }
              Text(
                "Server running at:",
                style = MaterialTheme.typography.h4,
                color = MaterialTheme.colors.primary,
                modifier = Modifier.padding(bottom = 12.dp),
              )
              Text("http://$ipAddress:$port", style = MaterialTheme.typography.h5)

              Spacer(modifier = Modifier.height(16.dp))

              val qrCodeBitmap = remember(ipAddress, port) { generateQRCode("http://$ipAddress:$port") }
              Image(
                bitmap = qrCodeBitmap,
                contentDescription = "QR Code",
                modifier = Modifier.size(200.dp),
              )

              Spacer(modifier = Modifier.height(16.dp))

              Text(
                "Sharing Directory:",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(bottom = 8.dp),
              )
              OutlinedTextField(
                value = sharedDirectory,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
              )
              Spacer(modifier = Modifier.height(8.dp))
              Button(
                onClick = {
                  val chooser = JFileChooser()
                  chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                  val result = chooser.showOpenDialog(null)
                  if (result == JFileChooser.APPROVE_OPTION) {
                    onDirectorySelected(chooser.selectedFile.absolutePath)
                  }
                },
                modifier = Modifier.fillMaxWidth(),
              ) {
                Text("Select Folder")
              }
            }
          }

          Spacer(modifier = Modifier.width(16.dp))

          // Right Panel: Logs and Chat
          Card(
            modifier = Modifier.weight(2f).fillMaxHeight(),
            elevation = 4.dp,
          ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
              // Event Log
              Text(
                "Event Log",
                style = MaterialTheme.typography.h4,
                color = MaterialTheme.colors.primary,
              )
              LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp)) {
                items(messages.filter { !it.startsWith("CHAT:") }) { msg ->
                  Text(msg, style = MaterialTheme.typography.body2)
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Chat
              Text(
                "Chat",
                style = MaterialTheme.typography.h4,
                color = MaterialTheme.colors.primary,
              )
              LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp)) {
                items(messages.filter { it.startsWith("CHAT:") }) { msg ->
                  val fullMessage = msg.removePrefix("CHAT: ")
                  val timestampEndIndex = fullMessage.indexOf("]")

                  if (timestampEndIndex != -1) {
                    val timestamp = fullMessage.substring(0, timestampEndIndex + 1)
                    val content = fullMessage.substring(timestampEndIndex + 1).trimStart()

                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                      Text(
                        timestamp,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.primary,
                      )
                      Text(
                        content,
                        style = MaterialTheme.typography.body1,
                      )
                    }
                  } else {
                    Text(fullMessage, style = MaterialTheme.typography.body1)
                  }
                }
              }

              // Chat Input
              Row(
                verticalAlignment = Alignment.CenterVertically,
              ) {
                OutlinedTextField(
                  value = chatMessage,
                  onValueChange = { chatMessage = it },
                  modifier = Modifier.weight(1f),
                  label = { Text("Enter message") },
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                  onClick = {
                    if (chatMessage.isNotBlank()) {
                      WebSocketClient.sendMessage(chatMessage)
                      chatMessage = ""
                    }
                  },
                ) {
                  Text("Send")
                }
              }
            }
          }
        }
      }
    }
  }
}

fun generateQRCode(text: String): ImageBitmap {
  val writer = QRCodeWriter()
  val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 200, 200)
  val bufferedImage = BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB)
  for (x in 0 until 200) {
    for (y in 0 until 200) {
      bufferedImage.setRGB(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
    }
  }
  return bufferedImage.toComposeImageBitmap()
}
