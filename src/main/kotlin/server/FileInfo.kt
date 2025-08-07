package server

import kotlinx.serialization.Serializable

@Serializable
data class FileInfo(
  val name: String,
  val path: String,
  val isDirectory: Boolean,
  val size: Long,
)
