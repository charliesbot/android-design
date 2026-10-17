plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.example.designsandbox.data"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
}

dependencies {
  implementation(project(":core:model"))
  implementation(project(":core:domain"))
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)
  ksp(libs.androidx.room.compiler)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.koin.android)
}
