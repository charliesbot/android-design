import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  implementation(project(":core:model"))
  implementation(project(":core:domain"))
  implementation(project(":core:data"))
  implementation(project(":features:preview:desktop"))
  implementation(project(":mcp"))
  implementation(compose.desktop.currentOs)
  implementation(libs.koin.core)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.coroutines.swing)
  testImplementation(libs.mcp.sdk)
  testImplementation(libs.kotlinx.coroutines.test)
}

compose.desktop {
  application {
    mainClass = "bot.charlies.androiddesign.preview.app.MainKt"
    // HotSpot prints warnings to stdout by default; stdout carries only JSON-RPC in MCP mode.
    jvmArgs += "-XX:+DisplayVMOutputToStderr"
    nativeDistributions {
      targetFormats(TargetFormat.Dmg)
      packageName = "AndroidDesignPreview"
      packageVersion = "1.0.0"
      includeAllModules = true
    }
  }
}

// `run` gets an empty stdin by default; forward Gradle's own stdin so MCP can talk to it.
tasks.withType<JavaExec>().configureEach { standardInput = System.`in` }

val fixturesDir =
  project(":core:data")
    .layout
    .projectDirectory
    .dir("src/test/resources/fixtures")
    .asFile
    .absolutePath
// The verify proposal lives in the repository's evals folder, above this standalone build.
val repoDir = rootProject.layout.projectDirectory.asFile.parentFile.absolutePath

tasks.test {
  // The CLI tests render the proposal fixture that :core:data keeps.
  systemProperty("fixtures.dir", fixturesDir)
  systemProperty("repo.dir", repoDir)
  useJUnitPlatform { excludeTags("benchmark") }
}

// Reports how long the window takes to adopt a saved change. Numbers, not assertions.
tasks.register<Test>("saveToAdopt") {
  group = "verification"
  description = "Prints the save-to-adopt and get_tree timings for the verify proposal."
  val test = tasks.test.get()
  testClassesDirs = test.testClassesDirs
  classpath = test.classpath
  systemProperty("fixtures.dir", fixturesDir)
  systemProperty("repo.dir", repoDir)
  useJUnitPlatform { includeTags("benchmark") }
  testLogging.showStandardStreams = true
  outputs.upToDateWhen { false }
}

// Builds the macOS app and copies it into ~/Applications, replacing any old copy. `ditto` keeps
// the bundle's symlinks, permissions and code signature intact, which a Gradle copy would not.
tasks.register<Exec>("installApp") {
  group = "distribution"
  description = "Builds AndroidDesignPreview.app and copies it to ~/Applications."
  dependsOn("createDistributable")
  val built = layout.buildDirectory.dir("compose/binaries/main/app/AndroidDesignPreview.app")
  val installed = File(System.getProperty("user.home"), "Applications/AndroidDesignPreview.app")
  doFirst {
    installed.deleteRecursively()
    installed.parentFile.mkdirs()
  }
  commandLine("ditto", built.get().asFile.path, installed.path)
}
