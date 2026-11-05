package androidx.core.content

import android.content.Context
import java.util.concurrent.Executor

object ContextCompat {
  fun getMainExecutor(context: Context): Executor = Executor { it.run() }
}
