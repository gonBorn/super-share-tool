package ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import service.WebSocketClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private fun ipToColor(ip: String): Color {
  val hash = ip.hashCode()
  val r = (hash and 0xFF0000) shr 16
  val g = (hash and 0x00FF00) shr 8
  val b = hash and 0x0000FF
  return Color(r, g, b)
}

@OptIn(ExperimentalComposeUiApi::class)
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
        val match = Regex("""(?s)^\[(.*?)\] \[(.*?)\]: (.*)$""").find(msg)
        if (match != null) {
          val (timestampStr, ip, message) = match.destructured
          val date = LocalDateTime.parse(timestampStr)
          val formattedTime = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))
          var isHovered by remember { mutableStateOf(false) }
          val clipboardManager = LocalClipboardManager.current

          Box(
            modifier =
              Modifier
                .fillMaxWidth()
                .onPointerEvent(PointerEventType.Enter) { isHovered = true }
                .onPointerEvent(PointerEventType.Exit) { isHovered = false },
          ) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  "[$ip]:",
                  style =
                    androidx
                      .compose
                      .material
                      .MaterialTheme
                      .typography
                      .caption,
                  color = ipToColor(ip),
                )
              }
              Text(
                message,
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
            if (isHovered) {
              Button(
                onClick = { clipboardManager.setText(AnnotatedString(message)) },
                modifier = Modifier.align(Alignment.TopEnd),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              ) {
                Text("Copy")
              }
            }
          }
        }
      }
    }

    // Chat Input
    Row(
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(modifier = Modifier.weight(1f)) {
        BasicTextField(
          value = chatMessage,
          onValueChange = { chatMessage = it },
          modifier =
            Modifier
              .height(100.dp)
              .fillMaxWidth()
              .border(1.dp, MaterialTheme.colors.primary)
              .padding(8.dp),
          singleLine = false,
        )
        if (chatMessage.isEmpty()) {
          Text(
            text = "Type here...",
            modifier = Modifier.padding(8.dp),
            color = Color.Gray,
          )
        }
      }
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
      Spacer(modifier = Modifier.width(8.dp))
      Button(
        onClick = {
          WebSocketClient.sendClearMessage()
        },
      ) {
        Text("Clear")
      }
    }
  }
}
