plugins {
  alias(libs.plugins.screenshot)
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "com.example.wearsandbox"
  // Wear Compose 1.8.0-alpha03 pulls Compose 1.13 beta, which needs compileSdk 37.1.
  compileSdk { version = release(libs.versions.compileSdk.get().toInt()) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.example.wearsandbox"
    minSdk = libs.versions.minSdk.get().toInt()
    targetSdk = libs.versions.compileSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"
  }

  experimentalProperties["android.experimental.enableScreenshotTest"] = true

  buildFeatures { compose = true }
}

dependencies {
  screenshotTestImplementation(libs.screenshot.validation.api)
  screenshotTestImplementation(libs.compose.ui.tooling)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.compose.bom))
  implementation(libs.compose.ui)
  implementation(libs.compose.ui.tooling.preview)
  implementation(libs.wear.compose.material3)
  implementation(libs.wear.compose.foundation)
  implementation(libs.wear.compose.navigation3)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.kotlinx.serialization.json)
  debugImplementation(libs.compose.ui.tooling)
}
