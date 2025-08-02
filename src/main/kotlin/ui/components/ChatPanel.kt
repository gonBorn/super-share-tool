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
import androidx.compose.ui.unit.dp
import service.WebSocketClient

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
              style =
                androidx
                  .compose
                  .material
                  .MaterialTheme
                  .typography
                  .caption,
              color =
                androidx
                  .compose
                  .material
                  .MaterialTheme
                  .colors
                  .primary,
            )
            Text(
              content,
              style =
                androidx
                  .compose
                  .material
                  .MaterialTheme
                  .typography
                  .body1,
            )
          }
        } else {
          Text(
            fullMessage,
            style =
              androidx
                .compose
                .material
                .MaterialTheme
                .typography
                .body1,
          )
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
