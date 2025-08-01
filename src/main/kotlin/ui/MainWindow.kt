package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import service.WebSocketClient
import utils.IpAddressUtil.getLocalIpAddress
import java.awt.image.BufferedImage

@Composable
@Preview
fun mainWindow(
  port: Int,
  onCloseRequest: () -> Unit,
) {
  val messages by WebSocketClient.messages.collectAsState()
  var chatMessage by remember { mutableStateOf("") }

  LaunchedEffect(Unit) {
    WebSocketClient.start(port)
  }

  Window(onCloseRequest = onCloseRequest, title = "Super Share") {
    MaterialTheme {
      Row(modifier = Modifier.fillMaxSize()) {
        // Left Panel: Info and QR Code
        Column(
          modifier = Modifier.weight(1f).padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          val ipAddress = remember { getLocalIpAddress() }
          Text("Server running at:", style = MaterialTheme.typography.h6)
          Text("http://$ipAddress:$port", style = MaterialTheme.typography.h5)

          Spacer(modifier = Modifier.height(16.dp))

          val qrCodeBitmap = remember { generateQRCode("http://$ipAddress:$port") }
          Image(
            bitmap = qrCodeBitmap,
            contentDescription = "QR Code",
            modifier = Modifier.size(200.dp),
          )
        }

        // Right Panel: Logs and Chat
        Column(modifier = Modifier.weight(2f).padding(16.dp)) {
          // Event Log
          Text("Event Log", style = MaterialTheme.typography.h6)
          LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp)) {
            items(messages.filter { !it.startsWith("CHAT:") }) { msg ->
              Text(msg, style = MaterialTheme.typography.body2)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Chat
          Text("Chat", style = MaterialTheme.typography.h6)
          LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp)) {
            items(messages.filter { it.startsWith("CHAT:") }) { msg ->
              Text(msg.removePrefix("CHAT: "), style = MaterialTheme.typography.body1)
            }
          }

          // Chat Input
          Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
              value = chatMessage,
              onValueChange = { chatMessage = it },
              modifier = Modifier.weight(1f),
              label = { Text("Enter message") },
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
              if (chatMessage.isNotBlank()) {
                WebSocketClient.sendMessage(chatMessage)
                chatMessage = ""
              }
            }) {
              Text("Send")
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
