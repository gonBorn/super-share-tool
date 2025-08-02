package state

import androidx.compose.runtime.mutableStateOf
import java.io.File

object AppState {
  val baseDir = mutableStateOf(File("."))
}
