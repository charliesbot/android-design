package android.database

abstract class ContentObserver(handler: android.os.Handler?) {
  open fun onChange(selfChange: Boolean, uri: android.net.Uri?) {}
}
