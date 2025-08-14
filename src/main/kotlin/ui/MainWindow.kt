package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import service.WebSocketClient
import state.AppConfig
import state.AppState
import ui.components.*

@Composable
@Preview
fun mainWindow(
  port: Int,
  onDirectorySelected: (String) -> Unit,
  onCloseRequest: () -> Unit,
) {
  val messages by WebSocketClient.messages.collectAsState()
  val baseDir by AppState.baseDir
  var showPreferencesDialog by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    WebSocketClient.start(port)
    AppConfig.defaultSharingDirectory?.let {
      onDirectorySelected(it)
    }
  }

  Window(
    onCloseRequest = onCloseRequest,
    title = "Super Share",
    state = rememberWindowState(width = 1300.dp, height = 800.dp),
    icon = painterResource("icons/file-share.ico"),
  ) {
    MenuBar {
      Menu("File", mnemonic = 'F') {
        Item("Preferences", onClick = { showPreferencesDialog = true }, mnemonic = 'P')
      }
    }
    if (showPreferencesDialog) {
      preferencesDialog(onCloseRequest = { showPreferencesDialog = false })
    }
    MaterialTheme {
      SelectionContainer {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
          // Left Panel
          Card(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            elevation = 4.dp,
          ) {
            Column(
              modifier = Modifier.padding(16.dp).fillMaxSize(),
            ) {
              serverInfo(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                port = port,
              )
              Spacer(modifier = Modifier.height(16.dp))
              directorySelector(
                selectedDirectory = baseDir,
                onDirectorySelected = { onDirectorySelected(it.absolutePath) },
              )
              Spacer(modifier = Modifier.height(16.dp))
              fileBrowser(baseDir)
            }
          }

          Spacer(modifier = Modifier.width(16.dp))

          // Right Panel
          Card(
            modifier = Modifier.weight(2f).fillMaxHeight(),
            elevation = 4.dp,
          ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
              logPanel(
                modifier = Modifier.fillMaxWidth(),
                messages = messages,
              )
              Spacer(modifier = Modifier.height(16.dp))
              chatPanel(
                modifier = Modifier.weight(1f),
                messages = messages,
              )
            }
          }
        }
      }
    }
  }
}
