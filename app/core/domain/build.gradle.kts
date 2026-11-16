plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  api(project(":core:model"))
  api(libs.kotlinx.coroutines.core)
  testImplementation(libs.kotlinx.coroutines.test)
}
