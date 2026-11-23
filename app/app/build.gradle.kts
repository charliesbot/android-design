plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  implementation(project(":core:model"))
  implementation(project(":core:domain"))
  implementation(project(":core:data"))
  implementation(libs.kotlin.compiler.embeddable)
  implementation(libs.lz4.java)
  testImplementation(kotlin("test"))
  testImplementation(libs.kotlinx.coroutines.test)
}

// `run` gets an empty stdin by default; forward Gradle's own stdin so worker stdio tests work.
tasks.withType<JavaExec>().configureEach { standardInput = System.`in` }

tasks.test {
  dependsOn(":host:exportBootstrap")
  useJUnitPlatform()
  maxHeapSize = "3072m"
  systemProperty("sun.net.http.allowRestrictedHeaders", "true")
  systemProperty(
    "preview.bin",
    rootProject.projectDir.parentFile
      .resolve("plugin/skills/android-design-explore/scripts/android-design-preview")
      .absolutePath,
  )
  systemProperty("preview.app.dir", rootProject.projectDir.absolutePath)
  systemProperty(
    "adp.bootstrap.dir",
    project(":host").layout.buildDirectory.dir("bootstrap").get().asFile.absolutePath,
  )
  systemProperty(
    "adp.runEmulatorTests",
    System.getProperty("adp.runEmulatorTests", "false"),
  )
}
