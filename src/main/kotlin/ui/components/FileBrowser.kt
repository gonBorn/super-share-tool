package ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun fileBrowser(baseDir: File) {
  Text(
    "Shared Files:",
    style =
      androidx
        .compose
        .material
        .MaterialTheme
        .typography
        .h6,
    modifier = Modifier.padding(bottom = 8.dp),
  )
  LazyColumn(modifier = Modifier.fillMaxSize()) {
    items(baseDir.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name }))?.toList() ?: emptyList()) { file ->
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(if (file.isDirectory) "📁" else "📄", modifier = Modifier.padding(end = 8.dp))
        Text(file.name)
      }
    }
  }
}
