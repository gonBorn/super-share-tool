package state

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

object AppConfig {
  private val configFile = File("config.properties")
  private val properties = Properties()

  private const val KEY_DEFAULT_SHARING_DIRECTORY = "default_sharing_directory"

  init {
    if (configFile.exists()) {
      FileInputStream(configFile).use { properties.load(it) }
    }
  }

  var defaultSharingDirectory: String?
    get() = properties.getProperty(KEY_DEFAULT_SHARING_DIRECTORY)
    set(value) {
      if (value != null) {
        properties.setProperty(KEY_DEFAULT_SHARING_DIRECTORY, value)
      } else {
        properties.remove(KEY_DEFAULT_SHARING_DIRECTORY)
      }
      FileOutputStream(configFile).use { properties.store(it, null) }
    }
}
