plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.compose.multiplatform)
}

// Google's Wear Compose sources, copied in unchanged under upstream/ (version in upstream/VERSION,
// refreshed by scripts/sync_wear_compose.py), compiled for desktop together with the shims in
// src/shims/, the generated R in src/generated/ and the host API in src/host/. Files the build
// skips
// are listed here; app/wear/README.md says why.
sourceSets.main {
  kotlin.srcDirs(
    "upstream/foundation",
    "upstream/material-core",
    "upstream/material3",
    "upstream/navigation3",
    "src/shims/kotlin",
    "src/generated/kotlin",
    "src/host/kotlin",
  )
  kotlin.exclude(
    "**/R.java",
    "**/BasicCurvedText.kt",
    "**/WarpedCurvedTextRenderer.kt",
    "**/LocalAmbientModeManager.kt",
    "**/rotary/Haptics.kt",
    "**/OpenOnPhoneDialog.kt",
    "**/DatePicker.kt",
    "**/TimePicker.kt",
    "**/AnimatedText.kt",
    "**/CurveToEdge.kt",
  )
}

kotlin.compilerOptions.freeCompilerArgs.addAll(
  "-jvm-default=no-compatibility",
  "-opt-in=kotlin.RequiresOptIn",
  "-Xexpect-actual-classes",
  "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
  "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
  "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
  "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
)

dependencies {
  api(compose.desktop.currentOs)
  api("org.jetbrains.compose.material:material-icons-core:1.7.3")
  api("org.jetbrains.compose.material:material-ripple:${libs.versions.composeMultiplatform.get()}")
  api(
    "org.jetbrains.compose.material3:material3-ripple:${libs.versions.composeMultiplatform.get()}",
  )
  api("androidx.graphics:graphics-shapes:1.1.0")
  api(libs.navigation3.ui)
  testImplementation(libs.compose.ui.test)
  testImplementation(libs.kotlinx.coroutines.swing)
}

tasks.test {
  // Where the render tests write their PNGs, for looking at them.
  systemProperty("renders.dir", layout.buildDirectory.dir("renders").get().asFile.absolutePath)
  jvmArgs("-Djava.awt.headless=true")
}
