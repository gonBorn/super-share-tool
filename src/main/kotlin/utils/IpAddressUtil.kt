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

    /**
     * 地址范围 常见用途
     *
     * 10.0.0.0/8
     * 企业、学校、大型网络
     *
     * 172.16.0.0/12
     * 企业、VPN
     *
     * 192.168.0.0/16
     * 家用路由器、家庭网络
     *
     */

    return ipCandidates.firstOrNull { it.startsWith("192.168.") }
      ?: ipCandidates.firstOrNull { it.startsWith("10.") }
      ?: ipCandidates.firstOrNull { it.startsWith("172.") }
      ?: "127.0.0.1"
  }
}
