package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import state.AppConfig
import state.AppState
import java.io.File

@Composable
fun preferencesDialog(onCloseRequest: () -> Unit) {
  val (selectedDirectory, setSelectedDirectory) = remember { mutableStateOf(AppConfig.defaultSharingDirectory) }

  Dialog(onCloseRequest = onCloseRequest, title = "Preferences") {
    Column(modifier = Modifier.padding(16.dp)) {
      directorySelector(
        selectedDirectory = selectedDirectory?.let { File(it) },
        onDirectorySelected = {
          setSelectedDirectory(it.absolutePath)
        },
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(modifier = Modifier.align(Alignment.End)) {
        Button(onClick = {
          AppConfig.defaultSharingDirectory = selectedDirectory
          selectedDirectory?.let {
            AppState.baseDir.value = File(it)
          }
          onCloseRequest()
        }) {
          Text("Save")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onCloseRequest) {
          Text("Cancel")
        }
      }
    }
  }
}
