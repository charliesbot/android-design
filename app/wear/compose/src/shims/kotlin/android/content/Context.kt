package android.content

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources

/** Desktop stand-in for android.content.Context: one process-wide instance, no system services. */
open class Context {
  open val applicationContext: Context
    get() = this

  open val contentResolver: ContentResolver = ContentResolver()
  open val packageManager: PackageManager = PackageManager()
  open val applicationInfo: ApplicationInfo = ApplicationInfo()
  open val resources: Resources = Resources()
  open val theme: android.content.res.Theme? = null

  open fun getSystemService(name: String): Any? = null

  open fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?): Intent? = null

  open fun unregisterReceiver(receiver: BroadcastReceiver?) {}

  open fun getMainExecutor(): java.util.concurrent.Executor =
    java.util.concurrent.Executor { it.run() }

  companion object {
    const val ACCESSIBILITY_SERVICE = "accessibility"
    const val VIBRATOR_SERVICE = "vibrator"
  }
}

/** Reads [settings] for `Settings.Global.getInt`; every other setting reads its default. */
class ContentResolver(val settings: Map<String, Int> = emptyMap()) {
  fun registerContentObserver(
    uri: android.net.Uri?,
    notifyForDescendants: Boolean,
    observer: android.database.ContentObserver,
  ) {}

  fun unregisterContentObserver(observer: android.database.ContentObserver) {}
}

open class Intent {
  companion object {
    const val ACTION_TIME_TICK = "android.intent.action.TIME_TICK"
    const val ACTION_TIME_CHANGED = "android.intent.action.TIME_SET"
    const val ACTION_TIMEZONE_CHANGED = "android.intent.action.TIMEZONE_CHANGED"
  }
}

class IntentFilter {
  fun addAction(action: String) {}
}

abstract class BroadcastReceiver {
  abstract fun onReceive(context: Context, intent: Intent)
}
