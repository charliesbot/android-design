package android.content.pm

class PackageManager {
  fun hasSystemFeature(name: String): Boolean = false
}

class ApplicationInfo {
  var targetSdkVersion: Int = 0
}
