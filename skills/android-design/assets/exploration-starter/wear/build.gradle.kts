plugins {
  alias(libs.plugins.screenshot)
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "com.example.designsandbox.wear"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.example.designsandbox"
    minSdk = libs.versions.wearMinSdk.get().toInt()
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
  implementation(project(":core:data"))
  implementation(project(":core:strings"))
  implementation(project(":core:designsystem:common"))
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.koin.android)
  implementation(libs.koin.androidx.compose)
  implementation(platform(libs.compose.bom))
  implementation(libs.compose.runtime)
  implementation(libs.compose.ui)
  implementation(libs.compose.foundation)
  implementation(libs.wear.compose.material3)
  implementation(libs.wear.compose.foundation)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.wear.compose.navigation3)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.compose.ui.tooling.preview)
  debugImplementation(libs.compose.ui.tooling)
  debugImplementation(libs.wear.tooling.preview)
}
