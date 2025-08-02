package ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import utils.IpAddressUtil
import java.awt.image.BufferedImage

@Composable
fun serverInfo(
  modifier: Modifier = Modifier,
  port: Int,
) {
  Column(modifier = modifier) {
    val ipAddress = remember { IpAddressUtil.getLocalIpAddress() }
    Text(
      "Server running at:",
      style = MaterialTheme.typography.h4,
      color = MaterialTheme.colors.primary,
      modifier = Modifier.padding(bottom = 12.dp),
    )
    Text("http://$ipAddress:$port", style = MaterialTheme.typography.h5)

    Spacer(modifier = Modifier.height(16.dp))

    val qrCodeBitmap = remember(ipAddress, port) { generateQRCode("http://$ipAddress:$port") }
    Image(
      bitmap = qrCodeBitmap,
      contentDescription = "QR Code",
      modifier = Modifier.size(200.dp),
    )
  }
}

fun generateQRCode(text: String): ImageBitmap {
  val writer = QRCodeWriter()
  val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 200, 200)
  val bufferedImage = BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB)
  for (x in 0 until 200) {
    for (y in 0 until 200) {
      bufferedImage.setRGB(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
    }
  }
  return bufferedImage.toComposeImageBitmap()
}
