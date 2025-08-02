package ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun logPanel(
  modifier: Modifier = Modifier,
  messages: List<String>,
) {
  Column(modifier = modifier) {
    Text(
      "Event Log",
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
    LazyColumn(modifier = Modifier.weight(1f, fill = false).padding(vertical = 8.dp)) {
      items(messages.filter { it.contains("UPLOAD") || it.contains("DOWNLOAD") }) { msg ->
        Text(
          msg,
          style =
            androidx
              .compose
              .material
              .MaterialTheme
              .typography
              .body2,
        )
      }
    }
  }
}
