plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  api(project(":core:domain"))
  implementation(libs.kaml)
  testImplementation(libs.kotlinx.coroutines.test)
}
