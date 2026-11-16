import java.util.zip.ZipFile
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

val composeCompilerPlugin = configurations.create("composeCompilerPlugin") { isTransitive = false }

val workerRuntime =
  configurations.create("workerRuntime") {
    extendsFrom(configurations.runtimeClasspath.get())
    listOf(
        "skiko-awt-runtime-macos-arm64",
        "skiko-awt-runtime-macos-x64",
        "skiko-awt-runtime-linux-x64",
        "skiko-awt-runtime-linux-arm64",
        "skiko-awt-runtime-windows-x64",
        "skiko-awt-runtime-windows-arm64",
      )
      .forEach { exclude(group = "org.jetbrains.skiko", module = it) }
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-compose-compiler-plugin-embeddable")
  }

dependencies {
  implementation(project(":core:model"))
  implementation(project(":core:domain"))
  implementation(project(":core:data"))
  implementation(project(":features:preview:desktop"))
  implementation(project(":mcp"))
  implementation(compose.desktop.currentOs)
  implementation(libs.compose.material3)
  implementation(libs.koin.core)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.coroutines.swing)
  composeCompilerPlugin(libs.kotlin.compose.compiler.embeddable)
  workerRuntime(libs.skiko.awt.runtime.all)
  testImplementation(libs.compose.ui.test)
  testImplementation(libs.mcp.sdk)
  testImplementation(libs.kotlinx.coroutines.test)
}

// The packaged app's bundle identifier, which is also its preferences domain (see AppNap).
val appBundleId = "bot.charlies.androiddesign.preview.app"

compose.desktop {
  application {
    mainClass = "bot.charlies.androiddesign.preview.app.MainKt"
    // HotSpot prints warnings to stdout by default; stdout carries only JSON-RPC in MCP mode.
    jvmArgs += "-XX:+DisplayVMOutputToStderr"
    // Read by AppNap.BUNDLE_ID_PROPERTY: only the packaged app has a preferences domain.
    jvmArgs += "-Dapp.bundleId=$appBundleId"
    // The title bar follows macOS light or dark mode instead of staying light.
    jvmArgs += "-Dapple.awt.application.appearance=system"
    nativeDistributions {
      targetFormats(TargetFormat.Dmg)
      packageName = "AndroidDesignPreview"
      packageVersion = "1.0.0"
      includeAllModules = true
      macOS { bundleID = appBundleId }
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

val workerServicesDir = layout.buildDirectory.dir("generated/worker-services")

val mergeWorkerServices =
  tasks.register("mergeWorkerServices") {
    inputs.files(workerRuntime)
    outputs.dir(workerServicesDir)
    doLast {
      val outRoot = workerServicesDir.get().asFile
      outRoot.deleteRecursively()
      val servicesDir = outRoot.resolve("META-INF/services").apply { mkdirs() }
      val entries = linkedMapOf<String, LinkedHashSet<String>>()
      workerRuntime.files
        .filter { it.isFile && it.extension == "jar" }
        .forEach { jarFile ->
          ZipFile(jarFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
              val name = entry.name
              if (
                !entry.isDirectory && name.startsWith("META-INF/services/") && !name.endsWith("/")
              ) {
                val serviceName = name.removePrefix("META-INF/services/")
                if ('/' !in serviceName && serviceName.isNotEmpty()) {
                  val lines = entries.getOrPut(serviceName) { linkedSetOf() }
                  zip.getInputStream(entry).bufferedReader().useLines { seq ->
                    seq
                      .map { it.trim() }
                      .filter { it.isNotEmpty() && !it.startsWith("#") }
                      .forEach { lines += it }
                  }
                }
              }
            }
          }
        }
      entries.forEach { (serviceName, providers) ->
        servicesDir.resolve(serviceName).writeText(providers.joinToString("\n") + "\n")
      }
    }
  }

val workerJar =
  tasks.register<Jar>("workerJar") {
    group = "distribution"
    description = "Builds the standalone cross-OS headless Skiko + Kotlin sandbox worker JAR."
    archiveFileName.set("android-design-preview.jar")
    isZip64 = true
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(tasks.named("jar"), mergeWorkerServices)

    manifest { attributes("Main-Class" to "bot.charlies.androiddesign.preview.app.MainKt") }

    from(sourceSets.main.get().output)
    from(workerServicesDir)
    from(composeCompilerPlugin) {
      into("META-INF/embedded")
      rename { "kotlin-compose-compiler-plugin-embeddable.jar" }
    }
    from(
      {
        workerRuntime.files.map { file ->
          if (file.isDirectory) {
            file
          } else {
            zipTree(file).matching {
              exclude(
                "META-INF/services/**",
                "META-INF/*.SF",
                "META-INF/*.DSA",
                "META-INF/*.RSA",
                "META-INF/MANIFEST.MF",
                "module-info.class",
              )
            }
          }
        }
      },
    )
  }

tasks.named("assemble") { dependsOn(workerJar) }
