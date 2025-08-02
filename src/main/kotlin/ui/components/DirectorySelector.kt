package ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import java.awt.Cursor
import java.io.File
import javax.swing.JFileChooser

@Composable
fun directorySelector(
  baseDir: File,
  onDirectorySelected: (String) -> Unit,
) {
  Text(
    "Sharing Directory:",
    style =
      androidx
        .compose
        .material
        .MaterialTheme
        .typography
        .h6,
    modifier = Modifier.fillMaxWidth(),
  )
  OutlinedTextField(
    value = baseDir.absolutePath,
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
    modifier = Modifier.fillMaxWidth().pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
  ) {
    Text("Select Folder")
  }
}
