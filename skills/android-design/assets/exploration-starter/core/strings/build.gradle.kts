plugins { alias(libs.plugins.android.library) }

android {
  namespace = "com.example.designsandbox.strings"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
}
