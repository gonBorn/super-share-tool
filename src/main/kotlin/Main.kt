import androidx.compose.runtime.*
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import server.startServer
import ui.mainWindow
import java.io.File

fun main(args: Array<String>) =
  application {
    var sharedDirectory by remember { mutableStateOf(File(".")) }
    val port = args.getOrNull(0)?.toInt() ?: 8080
    var serverScope by remember { mutableStateOf<CoroutineScope?>(null) }

    fun restartServer() {
      serverScope?.cancel()
      serverScope =
        CoroutineScope(Dispatchers.IO).apply {
          launch {
            startServer(port, sharedDirectory)
          }
        }
    }

    LaunchedEffect(sharedDirectory) {
      restartServer()
    }

    mainWindow(
      port = port,
      sharedDirectory = sharedDirectory.absolutePath,
      onDirectorySelected = { path ->
        sharedDirectory = File(path)
      },
      onCloseRequest = {
        serverScope?.cancel()
        exitApplication()
      },
    )
  }
