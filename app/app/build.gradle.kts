import java.util.zip.ZipFile

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
  implementation(compose.desktop.currentOs)
  implementation(libs.compose.material3)
  implementation(libs.kotlinx.coroutines.swing)
  composeCompilerPlugin(libs.kotlin.compose.compiler.embeddable)
  workerRuntime(libs.skiko.awt.runtime.all)
  testImplementation(kotlin("test"))
  testImplementation(libs.compose.ui.test)
  testImplementation(libs.kotlinx.coroutines.test)
}

// `run` gets an empty stdin by default; forward Gradle's own stdin so worker stdio tests work.
tasks.withType<JavaExec>().configureEach { standardInput = System.`in` }

tasks.test { useJUnitPlatform() }

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
    description =
      "Builds the standalone macOS and Linux headless Skiko + Kotlin sandbox worker JAR."
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
                // Windows is unsupported, so its Skiko libraries would only add download size.
                "skiko-windows-*",
              )
            }
          }
        }
      },
    )
  }

tasks.named("assemble") { dependsOn(workerJar) }

tasks.test {
  dependsOn(workerJar)
  systemProperty("sun.net.http.allowRestrictedHeaders", "true")
  systemProperty("preview.jar", workerJar.flatMap { it.archiveFile }.get().asFile.absolutePath)
  systemProperty(
    "preview.bin",
    rootProject.projectDir.parentFile.resolve("plugin/bin/android-design-preview").absolutePath,
  )
}
