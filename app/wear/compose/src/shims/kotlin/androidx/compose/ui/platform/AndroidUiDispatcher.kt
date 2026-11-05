package androidx.compose.ui.platform

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

object AndroidUiDispatcher {
  val Main: CoroutineDispatcher = Dispatchers.Unconfined
}
