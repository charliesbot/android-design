package bot.charlies.androiddesign.preview.host.sandbox

/**
 * Which local peers may talk to the device socket. Only `adb forward` (the adb daemon runs as the
 * shell user, or root on userdebug images) drives the service, so any other app uid is rejected.
 */
internal object PeerCredentialPolicy {
  /** Matches `android.os.Process.SHELL_UID`; kept pure so the check is JVM-testable. */
  const val SHELL_UID = 2000
  const val ROOT_UID = 0

  fun isTrusted(uid: Int): Boolean = uid == SHELL_UID || uid == ROOT_UID
}
