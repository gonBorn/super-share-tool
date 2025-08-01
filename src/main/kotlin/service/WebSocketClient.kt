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
import java.time.LocalDateTime

object WebSocketClient {
  private val mutableMessages = MutableStateFlow<List<String>>(emptyList())
  val messages = mutableMessages.asStateFlow()

  private var session: DefaultClientWebSocketSession? = null

  fun start() {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val client =
          HttpClient {
            install(WebSockets)
          }
        val ipAddress = getLocalIpAddress()
        client.webSocket(method = HttpMethod.Get, host = ipAddress, port = 8080, path = "/ws") {
          session = this
          for (frame in incoming) {
            if (frame is Frame.Text) {
              val receivedText = frame.readText()
              mutableMessages.value = mutableMessages.value + receivedText
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
      session?.send(Frame.Text("${LocalDateTime.now()}: $message"))
    }
  }
}
