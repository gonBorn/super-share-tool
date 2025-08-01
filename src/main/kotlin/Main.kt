import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import server.startServer
import ui.mainWindow

fun main(args: Array<String>) =
  application {
    // Start the Ktor server in a background coroutine
    val port = args.getOrNull(0)?.toInt() ?: 8080
    CoroutineScope(Dispatchers.IO).launch {
      startServer(port)
    }

    // Launch the Compose UI
    mainWindow(port, ::exitApplication)
  }
