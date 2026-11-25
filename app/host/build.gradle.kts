import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.Properties
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import org.gradle.api.attributes.Attribute
import org.gradle.api.tasks.ClasspathNormalizer

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
}

val composeCompilerPlugin = configurations.create("composeCompilerPlugin") { isTransitive = false }
val layoutlibRuntimeArchive =
  configurations.create("layoutlibRuntimeArchive") { isTransitive = false }
val layoutlibResourcesArchive =
  configurations.create("layoutlibResourcesArchive") { isTransitive = false }

android {
  namespace = "bot.charlies.androiddesign.preview.host"
  compileSdk {
    version =
      release(37) {
        minorApiLevel = 1
      }
  }

  defaultConfig {
    applicationId = "bot.charlies.androiddesign.preview.host"
    minSdk = 34
    targetSdk = 36
    versionCode = 1
    versionName = "0.1.0"
  }

  buildTypes {
    create("previewRelease") {
      initWith(getByName("release"))
      isDebuggable = false
      isMinifyEnabled = false
      signingConfig = signingConfigs.getByName("debug")
      matchingFallbacks += listOf("release")
    }
  }

  // Stored fonts are memory-mapped; compressed ones are copied onto the 192 MB Java heap once per
  // Typeface, and ProposalFonts keeps one Typeface per weight and icon size.
  androidResources { noCompress += "ttf" }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
      all {
        it.maxHeapSize = "3072m"
        it.testLogging {
          events("passed", "skipped", "failed", "standardOut", "standardError")
          showStandardStreams = true
        }
      }
    }
  }

  lint {
    abortOnError = true
    warningsAsErrors = false
  }
}

androidComponents {
  beforeVariants(selector().withBuildType("previewRelease")) { variantBuilder ->
    variantBuilder.hostTests.forEach { (_, hostTest) -> hostTest.enable = false }
  }
}

composeCompiler { includeSourceInformation = true }

kotlin { jvmToolchain(17) }

dependencies {
  implementation(project(":core:model"))
  implementation(project(":core:domain"))
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.data)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.navigationevent)
  implementation(libs.androidx.navigationevent.compose)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.adaptive)
  implementation(libs.androidx.adaptive.layout)
  implementation(libs.androidx.adaptive.navigation3)
  implementation(libs.androidx.wear.compose.foundation)
  implementation(libs.androidx.wear.compose.material3)
  implementation(libs.androidx.wear.compose.navigation3)
  implementation(libs.material.kolor)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.lz4.java)

  composeCompilerPlugin(libs.kotlin.compose.compiler.embeddable)
  layoutlibRuntimeArchive(libs.layoutlib.runtime) { artifact { type = "jar" } }
  layoutlibResourcesArchive(libs.layoutlib.resources) { artifact { type = "jar" } }

  testImplementation(project(":app"))
  testImplementation(libs.junit4)
  testImplementation(libs.robolectric)
}

val exportCompileBundle =
  tasks.register("exportCompileBundle") {
    group = "bootstrap"
    description =
      "Exports uncompressed androidx-compile-bundle.jar, host-preview.apk, and SDK metadata."
    val compileKotlin = tasks.named("compilePreviewReleaseKotlin")
    val assembleApk = tasks.named("assemblePreviewRelease")
    dependsOn(compileKotlin, assembleApk)

    val compileClasspath = configurations.getByName("previewReleaseCompileClasspath")
    val outDir = layout.buildDirectory.dir("bootstrap")
    val builtApk = layout.buildDirectory.file("outputs/apk/previewRelease/host-previewRelease.apk")
    val compileTaskOutputs = compileKotlin.map { it.outputs.files }
    val compileArtifactView =
      compileClasspath.incoming
        .artifactView {
          attributes {
            attribute(Attribute.of("artifactType", String::class.java), "android-classes-jar")
          }
        }
        .files
    val outBundleJar = layout.buildDirectory.file("bootstrap/androidx-compile-bundle.jar")
    val outHostApk = layout.buildDirectory.file("bootstrap/host-preview.apk")

    inputs.file(builtApk).withPropertyName("builtPreviewReleaseApk")
    inputs
      .files(compileTaskOutputs)
      .withPropertyName("compilePreviewReleaseOutputs")
      .withNormalizer(ClasspathNormalizer::class.java)
    inputs
      .files(compileArtifactView)
      .withPropertyName("previewReleaseCompileJars")
      .withNormalizer(ClasspathNormalizer::class.java)

    outputs.file(outBundleJar)
    outputs.file(outHostApk)

    doLast {
      outDir.get().asFile.mkdirs()
      val bundleFile = outBundleJar.get().asFile
      val copiedApk = outHostApk.get().asFile
      val sourceApk = builtApk.get().asFile
      check(sourceApk.isFile) { "Built APK missing at ${sourceApk.absolutePath}" }
      sourceApk.copyTo(copiedApk, overwrite = true)

      val artifactFiles = compileArtifactView.files

      val seenEntries = HashSet<String>()
      var jarCount = 0
      var classEntries = 0
      var kotlinModuleEntries = 0
      var kotlinBuiltinEntries = 0

      fun writeStoredEntry(zos: ZipOutputStream, name: String, bytes: ByteArray) {
        if (!seenEntries.add(name)) return
        val entry =
          ZipEntry(name).apply {
            method = ZipEntry.STORED
            size = bytes.size.toLong()
            compressedSize = bytes.size.toLong()
            val crcCalc = CRC32()
            crcCalc.update(bytes)
            crc = crcCalc.value
          }
        zos.putNextEntry(entry)
        zos.write(bytes)
        zos.closeEntry()
        if (name.endsWith(".class")) classEntries++
        if (name.startsWith("META-INF/") && name.endsWith(".kotlin_module")) kotlinModuleEntries++
        if (name.startsWith("kotlin/") && name.endsWith(".kotlin_builtins")) kotlinBuiltinEntries++
      }

      ZipOutputStream(FileOutputStream(bundleFile).buffered(1024 * 1024)).use { zos ->
        for (file in artifactFiles) {
          if (!file.exists()) continue
          if (file.isDirectory) {
            file
              .walkTopDown()
              .filter { it.isFile }
              .forEach { child ->
                val rel = child.relativeTo(file).invariantSeparatorsPath
                writeStoredEntry(zos, rel, child.readBytes())
              }
          } else if (file.extension == "jar") {
            jarCount++
            ZipFile(file).use { zip ->
              val entries = zip.entries()
              while (entries.hasMoreElements()) {
                val e = entries.nextElement()
                if (e.isDirectory) continue
                val n = e.name
                if (
                  n == "META-INF/MANIFEST.MF" ||
                    n.endsWith(".SF") ||
                    n.endsWith(".DSA") ||
                    n.endsWith(".RSA") ||
                    n == "module-info.class" ||
                    n.startsWith("META-INF/versions/")
                ) {
                  continue
                }
                writeStoredEntry(zos, n, zip.getInputStream(e).use { it.readBytes() })
              }
            }
          }
        }

        for (root in compileTaskOutputs.get().files) {
          if (!root.exists()) continue
          if (root.isDirectory) {
            root
              .walkTopDown()
              .filter { it.isFile }
              .forEach { child ->
                val rel = child.relativeTo(root).invariantSeparatorsPath
                if (rel != "META-INF/MANIFEST.MF") {
                  writeStoredEntry(zos, rel, child.readBytes())
                }
              }
          } else if (root.extension == "jar") {
            ZipFile(root).use { zip ->
              val entries = zip.entries()
              while (entries.hasMoreElements()) {
                val e = entries.nextElement()
                if (e.isDirectory || e.name == "META-INF/MANIFEST.MF") continue
                writeStoredEntry(zos, e.name, zip.getInputStream(e).use { it.readBytes() })
              }
            }
          }
        }
      }

      logger.lifecycle(
        "Exported compile bundle: ${bundleFile.length() / (1024 * 1024)} MiB from $jarCount jars ($classEntries classes, $kotlinModuleEntries .kotlin_module, $kotlinBuiltinEntries .kotlin_builtins)",
      )
    }
  }

val unpackLayoutlibRuntime =
  tasks.register<Sync>("unpackLayoutlibRuntime") {
    group = "bootstrap"
    description = "Unpacks layoutlib-runtime into the bootstrap directory."
    from(layoutlibRuntimeArchive.elements.map { files -> files.map { zipTree(it) } })
    into(layout.buildDirectory.dir("bootstrap/layoutlib/runtime"))
  }

val unpackLayoutlibResources =
  tasks.register<Sync>("unpackLayoutlibResources") {
    group = "bootstrap"
    description = "Unpacks layoutlib-resources into the bootstrap directory."
    from(layoutlibResourcesArchive.elements.map { files -> files.map { zipTree(it) } })
    into(layout.buildDirectory.dir("bootstrap/layoutlib/resources"))
  }

val exportWorkerHostArtifacts =
  tasks.register("exportWorkerHostArtifacts") {
    group = "bootstrap"
    description = "Exports :host runtime classes, merged R.jar, and AAR resource directories."
    val compileKotlin = tasks.named("compilePreviewReleaseKotlin")
    val processResources = tasks.named("processPreviewReleaseResources")
    dependsOn(compileKotlin, processResources)

    val runtimeClasspath = configurations.getByName("previewReleaseRuntimeClasspath")
    val runtimeResView =
      runtimeClasspath.incoming
        .artifactView {
          attributes {
            attribute(Attribute.of("artifactType", String::class.java), "android-res")
          }
          lenient(true)
        }
        .files
    val compileTaskOutputs = compileKotlin.map { it.outputs.files }
    val mergedRJarDir =
      layout.buildDirectory.dir(
        "intermediates/compile_and_runtime_r_class_jar/previewRelease/processPreviewReleaseResources",
      )
    val outHostClassesJar = layout.buildDirectory.file("bootstrap/worker-host-classes.jar")
    val outWorkerRJar = layout.buildDirectory.file("bootstrap/worker-r.jar")
    val outWorkerResDirs = layout.buildDirectory.file("bootstrap/worker-res-dirs.txt")

    inputs
      .files(compileTaskOutputs)
      .withPropertyName("workerHostCompileOutputs")
      .withNormalizer(ClasspathNormalizer::class.java)
    inputs.files(processResources.map { it.outputs.files }).withPropertyName("workerProcessRes")
    inputs.files(runtimeResView).withPropertyName("workerRuntimeResDirs")

    outputs.file(outHostClassesJar)
    outputs.file(outWorkerRJar)
    outputs.file(outWorkerResDirs)

    doLast {
      val bootstrapDir = layout.buildDirectory.dir("bootstrap").get().asFile
      bootstrapDir.mkdirs()

      val rJarSource =
        mergedRJarDir.get().asFile.walkTopDown().firstOrNull { it.isFile && it.name == "R.jar" }
          ?: error("Merged R.jar missing under ${mergedRJarDir.get().asFile.absolutePath}")
      rJarSource.copyTo(outWorkerRJar.get().asFile, overwrite = true)

      val resDirs =
        runtimeResView.files.filter { it.isDirectory }.map { it.absolutePath }.distinct().sorted()
      outWorkerResDirs
        .get()
        .asFile
        .writeText(resDirs.joinToString("\n") + if (resDirs.isEmpty()) "" else "\n")

      val seen = HashSet<String>()
      fun writeStored(zos: ZipOutputStream, name: String, bytes: ByteArray) {
        if (!seen.add(name)) return
        val entry =
          ZipEntry(name).apply {
            method = ZipEntry.STORED
            size = bytes.size.toLong()
            compressedSize = bytes.size.toLong()
            val crcCalc = CRC32()
            crcCalc.update(bytes)
            crc = crcCalc.value
          }
        zos.putNextEntry(entry)
        zos.write(bytes)
        zos.closeEntry()
      }

      ZipOutputStream(
          FileOutputStream(outHostClassesJar.get().asFile).buffered(1024 * 1024),
        )
        .use { zos ->
          for (root in compileTaskOutputs.get().files) {
            if (!root.exists()) continue
            if (root.isDirectory) {
              root
                .walkTopDown()
                .filter { it.isFile }
                .forEach { child ->
                  val rel = child.relativeTo(root).invariantSeparatorsPath
                  if (rel != "META-INF/MANIFEST.MF") {
                    writeStored(zos, rel, child.readBytes())
                  }
                }
            } else if (root.extension == "jar") {
              ZipFile(root).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                  val e = entries.nextElement()
                  if (e.isDirectory || e.name == "META-INF/MANIFEST.MF") continue
                  writeStored(zos, e.name, zip.getInputStream(e).use { it.readBytes() })
                }
              }
            }
          }
        }
    }
  }

evaluationDependsOn(":app")

evaluationDependsOn(":render")

val exportBootstrap =
  tasks.register("exportBootstrap") {
    group = "bootstrap"
    description =
      "Exports host-preview.apk, androidx-compile-bundle.jar, layoutlib worker runtime, metadata, and @server.cp, @client.cp, @worker.cp argfiles."

    val appProject = project(":app")
    val appJarTask = appProject.tasks.named("jar")
    val renderProject = project(":render")
    val renderJarTask = renderProject.tasks.named("jar")

    dependsOn(
      exportCompileBundle,
      exportWorkerHostArtifacts,
      unpackLayoutlibRuntime,
      unpackLayoutlibResources,
      appJarTask,
      renderJarTask,
    )

    val sdkComponents =
      project.extensions
        .getByType(com.android.build.api.variant.AndroidComponentsExtension::class.java)
        .sdkComponents
    val compileClasspath = configurations.getByName("previewReleaseCompileClasspath")
    val composePluginFiles = composeCompilerPlugin
    val runtimeClasspath = configurations.getByName("previewReleaseRuntimeClasspath")
    val hostRuntimeJars =
      runtimeClasspath.incoming
        .artifactView {
          attributes {
            attribute(Attribute.of("artifactType", String::class.java), "android-classes-jar")
          }
          lenient(true)
        }
        .files
    val appRuntimeConfig = appProject.configurations.getByName("runtimeClasspath")
    val appJarOutputs = appJarTask.map { it.outputs.files }
    val renderRuntimeConfig = renderProject.configurations.getByName("runtimeClasspath")
    val renderJarOutputs = renderJarTask.map { it.outputs.files }

    val bundleFile = layout.buildDirectory.file("bootstrap/androidx-compile-bundle.jar")
    val copiedApk = layout.buildDirectory.file("bootstrap/host-preview.apk")
    val workerHostClassesJar = layout.buildDirectory.file("bootstrap/worker-host-classes.jar")
    val workerRJar = layout.buildDirectory.file("bootstrap/worker-r.jar")
    val workerResDirs = layout.buildDirectory.file("bootstrap/worker-res-dirs.txt")
    val layoutlibRuntimeDir = layout.buildDirectory.dir("bootstrap/layoutlib/runtime")
    val layoutlibResourcesDir = layout.buildDirectory.dir("bootstrap/layoutlib/resources")
    val workerAssetsDir = projectDir.resolve("src/main/assets")

    val outServerCp = layout.buildDirectory.file("bootstrap/server.cp")
    val outClientCp = layout.buildDirectory.file("bootstrap/client.cp")
    val outWorkerCp = layout.buildDirectory.file("bootstrap/worker.cp")
    val outMetaFile = layout.buildDirectory.file("bootstrap/bootstrap-meta.properties")
    val outPathsFile = layout.buildDirectory.file("bootstrap/bootstrap-paths.txt")

    inputs
      .files(appJarOutputs)
      .withPropertyName("appJarOutputs")
      .withNormalizer(ClasspathNormalizer::class.java)
    inputs
      .files(appRuntimeConfig)
      .withPropertyName("appRuntimeClasspath")
      .withNormalizer(ClasspathNormalizer::class.java)
    inputs
      .files(renderJarOutputs)
      .withPropertyName("renderJarOutputs")
      .withNormalizer(ClasspathNormalizer::class.java)
    inputs
      .files(renderRuntimeConfig)
      .withPropertyName("renderRuntimeClasspath")
      .withNormalizer(ClasspathNormalizer::class.java)
    inputs
      .files(hostRuntimeJars)
      .withPropertyName("workerHostRuntimeJars")
      .withNormalizer(ClasspathNormalizer::class.java)
    inputs
      .files(
        compileClasspath.incoming
          .artifactView {
            attributes {
              attribute(Attribute.of("artifactType", String::class.java), "android-classes-jar")
            }
          }
          .files,
      )
      .withPropertyName("compileBundleArtifactCount")
      .withNormalizer(ClasspathNormalizer::class.java)
    inputs
      .files(composePluginFiles)
      .withPropertyName("composeCompilerPlugin")
      .withNormalizer(ClasspathNormalizer::class.java)
    inputs.files(sdkComponents.bootClasspath).withPropertyName("sdkBootClasspath")
    inputs.property("sdkDirectoryPath", sdkComponents.sdkDirectory.map { it.asFile.absolutePath })
    inputs.file(bundleFile).withPropertyName("exportedCompileBundle")
    inputs.file(copiedApk).withPropertyName("exportedApk")
    inputs.file(workerHostClassesJar).withPropertyName("workerHostClassesJar")
    inputs.file(workerRJar).withPropertyName("workerRJar")
    inputs.file(workerResDirs).withPropertyName("workerResDirs")
    inputs.dir(layoutlibRuntimeDir).withPropertyName("layoutlibRuntimeDir")
    inputs.dir(layoutlibResourcesDir).withPropertyName("layoutlibResourcesDir")
    inputs.dir(workerAssetsDir).withPropertyName("workerAssetsDir")

    outputs.file(outServerCp)
    outputs.file(outClientCp)
    outputs.file(outWorkerCp)
    outputs.file(outMetaFile)
    outputs.file(outPathsFile)

    doLast {
      val robolectricConfigDir =
        layout.buildDirectory.dir("bootstrap/robolectric-config").get().asFile
      robolectricConfigDir.deleteRecursively()
      layout.buildDirectory.file("bootstrap/robolectric.cp").get().asFile.delete()

      val appEntries = linkedSetOf<String>()
      for (f in appJarOutputs.get().files) {
        if (f.exists()) appEntries.add(f.absolutePath)
      }
      for (f in appRuntimeConfig.files) {
        if (f.exists()) appEntries.add(f.absolutePath)
      }
      val appCpContent = "-cp\n${appEntries.joinToString(":")}\n"
      outServerCp.get().asFile.apply {
        parentFile.mkdirs()
        writeText(appCpContent)
      }
      outClientCp.get().asFile.apply {
        parentFile.mkdirs()
        writeText(appCpContent)
      }

      val bootClasspath = sdkComponents.bootClasspath.get().map { it.asFile.absolutePath }
      val androidJar = bootClasspath.firstOrNull { it.endsWith("android.jar") } ?: ""
      val androidJarCanonical = if (androidJar.isNotEmpty()) File(androidJar).canonicalPath else ""
      val sdkDir = sdkComponents.sdkDirectory.get().asFile
      val d8Jar =
        sdkDir
          .resolve("build-tools")
          .listFiles()
          ?.filter { it.isDirectory }
          ?.sortedByDescending { it.name }
          ?.map { it.resolve("lib/d8.jar") }
          ?.firstOrNull { it.isFile }
          ?.absolutePath ?: ""
      val composePluginJar = composePluginFiles.files.singleOrNull()?.absolutePath ?: ""
      val apkFile = copiedApk.get().asFile
      val apkDigest = MessageDigest.getInstance("SHA-256")
      apkFile.inputStream().buffered().use { input ->
        val buf = ByteArray(8192)
        while (true) {
          val n = input.read(buf)
          if (n <= 0) break
          apkDigest.update(buf, 0, n)
        }
      }
      val apkSha256 = apkDigest.digest().joinToString("") { "%02x".format(it) }

      val workerEntries = linkedSetOf<String>()
      val seenCanonical = HashSet<String>()
      fun addWorkerEntry(file: File) {
        if (!file.exists()) return
        val canonical = file.canonicalPath
        if (canonical == androidJarCanonical) return
        if (seenCanonical.add(canonical)) {
          workerEntries.add(file.absolutePath)
        }
      }

      // layoutlib must precede android.jar (and all Android framework stubs) on the classpath.
      val renderRuntimeFiles = renderRuntimeConfig.files
      for (f in renderRuntimeFiles) {
        if (f.name.startsWith("layoutlib-") && f.extension == "jar") {
          addWorkerEntry(f)
        }
      }
      for (f in renderJarOutputs.get().files) {
        addWorkerEntry(f)
      }
      for (f in renderRuntimeFiles) {
        addWorkerEntry(f)
      }
      addWorkerEntry(workerRJar.get().asFile)
      addWorkerEntry(workerHostClassesJar.get().asFile)
      for (f in hostRuntimeJars.files) {
        addWorkerEntry(f)
      }
      if (androidJar.isNotEmpty() && File(androidJar).exists()) {
        workerEntries.add(androidJar)
      }
      outWorkerCp.get().asFile.apply {
        parentFile.mkdirs()
        writeText("-cp\n${workerEntries.joinToString(":")}\n")
      }

      val bundle = bundleFile.get().asFile
      val workerCpFile = outWorkerCp.get().asFile
      val llRuntime = layoutlibRuntimeDir.get().asFile
      val llResources = layoutlibResourcesDir.get().asFile
      val resDirsFile = workerResDirs.get().asFile
      val rJarFile = workerRJar.get().asFile

      val props = Properties()
      props.setProperty("androidJar", androidJar)
      props.setProperty("d8Jar", d8Jar)
      props.setProperty("bundleJar", bundle.absolutePath)
      props.setProperty("apkPath", apkFile.absolutePath)
      props.setProperty("apkSha256", apkSha256)
      props.setProperty("composeCompilerPluginJar", composePluginJar)
      props.setProperty("workerCp", workerCpFile.absolutePath)
      props.setProperty("layoutlibRuntimeDir", llRuntime.absolutePath)
      props.setProperty("layoutlibResourcesDir", llResources.absolutePath)
      props.setProperty("workerResDirsFile", resDirsFile.absolutePath)
      props.setProperty("workerAssetsDir", workerAssetsDir.absolutePath)
      props.setProperty("workerRJar", rJarFile.absolutePath)
      props.setProperty("bundleSizeBytes", bundle.length().toString())
      outMetaFile.get().asFile.bufferedWriter().use { writer ->
        props.store(writer, "android-design-preview bootstrap metadata")
      }
      outPathsFile
        .get()
        .asFile
        .writeText(
          listOf(
              androidJar,
              d8Jar,
              bundle.absolutePath,
              apkFile.absolutePath,
              composePluginJar,
              workerCpFile.absolutePath,
              llRuntime.absolutePath,
              llResources.absolutePath,
              resDirsFile.absolutePath,
              workerAssetsDir.absolutePath,
              rJarFile.absolutePath,
            )
            .filter { it.isNotEmpty() }
            .joinToString("\n") + "\n",
        )
    }
  }

tasks.withType<Test>().configureEach {
  dependsOn(exportBootstrap)
  systemProperty(
    "adp.bootstrap.dir",
    layout.buildDirectory.dir("bootstrap").get().asFile.absolutePath,
  )
  systemProperty(
    "adp.runEmulatorTests",
    System.getProperty("adp.runEmulatorTests", "false"),
  )
}
