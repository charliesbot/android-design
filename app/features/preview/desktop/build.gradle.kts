plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.compose.multiplatform)
}

dependencies {
  api(project(":core:domain"))
  api(project(":core:data"))
  implementation(project(":wear:compose"))
  implementation(compose.desktop.currentOs)
  implementation(compose.components.uiToolingPreview)
  implementation(libs.compose.material3)
  implementation(libs.compose.ui.test)
  implementation(libs.navigation3.ui)
  implementation(libs.adaptive.navigation3)
  implementation(libs.material.kolor)
  implementation(libs.kotlinx.coroutines.swing)
  implementation(libs.kotlin.compiler.embeddable)
  implementation(libs.kotlin.compose.compiler.embeddable)
  implementation(libs.jetbrains.annotations)
  testImplementation(kotlin("test"))
}
