package utils

import java.net.Inet4Address
import java.net.NetworkInterface

object IpAddressUtil {
  fun getLocalIpAddress(): String {
    val ipCandidates = mutableListOf<String>()

    NetworkInterface
      .getNetworkInterfaces()
      .asSequence()
      .filter { it.isUp && !it.isLoopback && !it.isVirtual }
      .forEach { nif ->
        nif
          .inetAddresses
          .asSequence()
          .filterIsInstance<Inet4Address>()
          .filter { !it.isLoopbackAddress && !it.isLinkLocalAddress }
          .forEach { addr ->
            val ip = addr.hostAddress
            ipCandidates += ip
          }
      }

    return ipCandidates.firstOrNull { it.startsWith("192.168.") }
      ?: "127.0.0.1"
  }
}
