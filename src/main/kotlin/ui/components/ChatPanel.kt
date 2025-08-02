package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import service.WebSocketClient
import java.awt.Cursor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private fun ipToColor(ip: String): Color {
  val hash = ip.hashCode()
  val r = (hash and 0xFF0000) shr 16
  val g = (hash and 0x00FF00) shr 8
  val b = hash and 0x0000FF
  return Color(r, g, b)
}

@Composable
fun chatPanel(
  modifier: Modifier = Modifier,
  messages: List<String>,
) {
  var chatMessage by remember { mutableStateOf("") }

  Column(modifier = modifier) {
    Text(
      "Chat",
      style =
        androidx
          .compose
          .material
          .MaterialTheme
          .typography
          .h4,
      color =
        androidx
          .compose
          .material
          .MaterialTheme
          .colors
          .primary,
    )
    LazyColumn(
      modifier =
        Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(vertical = 8.dp),
    ) {
      items(messages) { msg ->
        val match = Regex("""^\[(.*?)\] \[(.*?)\]: (.*)$""").find(msg)
        if (match != null) {
          val (timestampStr, ip, message) = match.destructured
          val date = LocalDateTime.parse(timestampStr)
          val formattedTime = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))

          Column(modifier = Modifier.padding(bottom = 8.dp)) {
            Text(
              formattedTime,
              style =
                androidx
                  .compose
                  .material
                  .MaterialTheme
                  .typography
                  .caption,
              color = Color.Gray,
            )
            Text(
              "[$ip]: $message",
              style =
                androidx
                  .compose
                  .material
                  .MaterialTheme
                  .typography
                  .body1,
              color = ipToColor(ip),
            )
          }
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
        modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
      ) {
        Text("Send")
      }
      Spacer(modifier = Modifier.width(8.dp))
      Button(
        onClick = {
          WebSocketClient.sendClearMessage()
        },
        modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
      ) {
        Text("Clear")
      }
    }
  }
}
