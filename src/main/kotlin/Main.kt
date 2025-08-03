import androidx.compose.runtime.*
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import server.startServer
import server.stopServer
import state.AppState
import ui.mainWindow
import java.io.File

fun main(args: Array<String>) =
  application {
    val port = args.getOrNull(0)?.toInt() ?: 3999
    var serverScope by remember { mutableStateOf<CoroutineScope?>(null) }

    // Start the server only once
    LaunchedEffect(Unit) {
      serverScope =
        CoroutineScope(Dispatchers.IO).apply {
          launch {
            startServer(port)
          }
        }
    }

    mainWindow(
      port = port,
      onDirectorySelected = { path ->
        AppState.baseDir.value = File(path)
      },
      onCloseRequest = {
        serverScope?.cancel()
        stopServer() // Explicitly stop the server
        exitApplication()
      },
    )
  }
