package androidx.core.os

object HandlerCompat {
  fun createAsync(looper: android.os.Looper): android.os.Handler = android.os.Handler()
}
