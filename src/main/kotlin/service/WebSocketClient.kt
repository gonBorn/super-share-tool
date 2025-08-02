package service

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.HttpMethod
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import utils.IpAddressUtil.getLocalIpAddress

object WebSocketClient {
  private val mutableMessages = MutableStateFlow<List<String>>(emptyList())
  val messages = mutableMessages.asStateFlow()

  private var session: DefaultClientWebSocketSession? = null

  fun start(port: Int) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val client =
          HttpClient {
            install(WebSockets)
          }
        val ipAddress = getLocalIpAddress()
        client.webSocket(method = HttpMethod.Get, host = ipAddress, port = port, path = "/ws") {
          session = this
          for (frame in incoming) {
            if (frame is Frame.Text) {
              val receivedText = frame.readText()
              if (receivedText == "CLEAR") {
                clearMessages()
              } else {
                mutableMessages.value = mutableMessages.value + receivedText
              }
            }
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun sendMessage(message: String) {
    CoroutineScope(Dispatchers.IO).launch {
      session?.send(Frame.Text(message))
    }
  }

  fun sendClearMessage() {
    CoroutineScope(Dispatchers.IO).launch {
      session?.send(Frame.Text("CLEAR"))
    }
  }

  private fun clearMessages() {
    mutableMessages.value = emptyList()
  }
}
