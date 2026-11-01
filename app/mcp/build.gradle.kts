plugins { alias(libs.plugins.kotlin.jvm) }

dependencies {
  api(project(":core:domain"))
  api(libs.mcp.sdk)
  testImplementation(libs.kotlinx.coroutines.test)
}
