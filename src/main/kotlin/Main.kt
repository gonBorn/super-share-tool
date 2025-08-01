import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import server.startServer
import ui.mainWindow

fun main() =
  application {
    // Start the Ktor server in a background coroutine
    CoroutineScope(Dispatchers.IO).launch {
      startServer()
    }

    // Launch the Compose UI
    mainWindow(::exitApplication)
  }
