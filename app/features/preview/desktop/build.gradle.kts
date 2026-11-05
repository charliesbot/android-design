plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.compose.multiplatform)
}

dependencies {
  api(project(":core:domain"))
  implementation(project(":wear:compose"))
  implementation(compose.desktop.currentOs)
  implementation(compose.components.uiToolingPreview)
  implementation(libs.compose.material3)
  implementation(libs.compose.ui.test)
  implementation(libs.navigation3.ui)
  implementation(libs.adaptive.navigation3)
  implementation(libs.material.kolor)
  implementation(libs.kotlinx.coroutines.swing)
}

dependencies { testImplementation(project(":core:data")) }

tasks.test {
  // Tests share the fixtures of :core:data.
  systemProperty(
    "fixtures.dir",
    project(":core:data")
      .layout
      .projectDirectory
      .dir("src/test/resources/fixtures")
      .asFile
      .absolutePath,
  )
  // The verify proposal lives in the repository's evals folder, above this standalone build.
  systemProperty("repo.dir", rootProject.layout.projectDirectory.asFile.parentFile.absolutePath)
}
