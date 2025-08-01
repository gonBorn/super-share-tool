package utils

import java.net.InetAddress
import java.net.NetworkInterface

fun getLocalIpAddress(): String {
  try {
    // Try to find a non-loopback, site-local address (e.g., 192.168.x.x)
    NetworkInterface.getNetworkInterfaces().asSequence().forEach { networkInterface ->
      networkInterface
        .inetAddresses
        .asSequence()
        .filter { !it.isLoopbackAddress && it.isSiteLocalAddress && it.hostAddress.matches(Regex("\\d{1,3}(\\.\\d{1,3}){3}")) }
        .firstOrNull()
        ?.let { return it.hostAddress }
    }

    // Fallback to InetAddress.getLocalHost()
    val localHost = InetAddress.getLocalHost()
    if (localHost.isLoopbackAddress) {
      return "127.0.0.1"
    }
    return localHost.hostAddress
  } catch (e: Exception) {
    // If everything fails, return localhost
    e.printStackTrace()
    return "127.0.0.1"
  }
}
