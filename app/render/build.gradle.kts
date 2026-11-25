import java.io.File
import java.util.Properties

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
}

kotlin { jvmToolchain(21) }

composeCompiler { includeSourceInformation = true }

fun resolveAndroidJar(): File {
  val metaFile =
    project(":host").layout.buildDirectory.file("bootstrap/bootstrap-meta.properties").get().asFile
  if (metaFile.isFile) {
    val props = Properties().apply { metaFile.bufferedReader().use { load(it) } }
    val fromMeta = props.getProperty("androidJar")?.trim()?.takeIf { it.isNotEmpty() }?.let(::File)
    if (fromMeta != null && fromMeta.isFile) return fromMeta
  }

  val localPropsFile = rootProject.file("local.properties")
  val localSdkDir =
    if (localPropsFile.isFile) {
      Properties()
        .apply { localPropsFile.bufferedReader().use { load(it) } }
        .getProperty("sdk.dir")
        ?.trim()
    } else {
      null
    }

  val sdkRoots =
    listOfNotNull(
        System.getenv("ANDROID_HOME"),
        System.getenv("ANDROID_SDK_ROOT"),
        localSdkDir,
        "${System.getProperty("user.home")}/Library/Android/sdk",
        "${System.getProperty("user.home")}/Android/Sdk",
        "/opt/android-sdk",
      )
      .map(::File)
      .filter { it.isDirectory }

  for (root in sdkRoots) {
    val platforms = File(root, "platforms")
    if (!platforms.isDirectory) continue
    val preferred = listOf("android-37.1", "android-37.0", "android-37")
    for (name in preferred) {
      val jar = File(platforms, "$name/android.jar")
      if (jar.isFile) return jar
    }
    val highest =
      platforms
        .listFiles { f -> f.isDirectory && f.name.startsWith("android-") }
        ?.sortedByDescending { it.name }
        ?.map { File(it, "android.jar") }
        ?.firstOrNull { it.isFile }
    if (highest != null) return highest
  }
  error("Cannot locate Android SDK android.jar; set ANDROID_HOME or ANDROID_SDK_ROOT")
}

val androidSdkJar = resolveAndroidJar()
val compileBundleJar =
  project(":host").layout.buildDirectory.file("bootstrap/androidx-compile-bundle.jar")

configurations.configureEach {
  resolutionStrategy.eachDependency {
    if (requested.group == "com.android.tools.layoutlib" && requested.name == "layoutlib") {
      useVersion(libs.versions.layoutlib.get())
    }
    if (requested.group == "com.android.tools.layoutlib" && requested.name == "layoutlib-api") {
      useVersion(libs.versions.androidTools.get())
    }
    if (
      requested.group == "com.android.tools" &&
        requested.name in setOf("common", "sdk-common", "ninepatch")
    ) {
      useVersion(libs.versions.androidTools.get())
    }
  }
}

dependencies {
  implementation(project(":core:model"))
  implementation(project(":core:domain"))
  implementation(libs.layoutlib)
  implementation(libs.layoutlib.api)
  implementation(libs.android.tools.common)
  implementation(libs.android.tools.sdk.common)
  implementation(libs.android.tools.ninepatch)
  implementation(libs.bytebuddy)
  implementation(libs.bytebuddy.agent)
  implementation(libs.kxml2)
  implementation(libs.kotlinx.coroutines.core)

  // Real AndroidX Compose + :host kit/extractor classes compiled without ui-tooling.
  compileOnly(files(compileBundleJar) { builtBy(":host:exportCompileBundle") })
  testCompileOnly(files(compileBundleJar) { builtBy(":host:exportCompileBundle") })

  testImplementation(project(":app"))
}

tasks.test {
  dependsOn(":host:exportBootstrap")
  useJUnitPlatform()
  maxHeapSize = "3072m"
  jvmArgs(
    "-Djava.awt.headless=true",
    "-Djdk.attach.allowAttachSelf=true",
    "-XX:+EnableDynamicAgentLoading",
  )
  val bootstrapDir = project(":host").layout.buildDirectory.dir("bootstrap")
  systemProperty("adp.bootstrap.dir", bootstrapDir.get().asFile.absolutePath)
  systemProperty("adp.repo.root", rootProject.projectDir.parentFile.absolutePath)

  doFirst {
    val workerCpFile = bootstrapDir.get().file("worker.cp").asFile
    val workerCpEntries =
      workerCpFile
        .readLines()
        .filter { it.isNotBlank() && it != "-cp" }
        .flatMap { it.split(File.pathSeparator) }
        .filter { it.isNotBlank() }
        .map(::File)

    val existingTestCp = classpath.files
    val androidJarPath = androidSdkJar.canonicalPath
    val layoutlibJars = workerCpEntries.filter {
      it.name.startsWith("layoutlib-") && it.name.endsWith(".jar")
    }
    val nonAndroidWorkerEntries = workerCpEntries.filter {
      it.canonicalPath != androidJarPath &&
        !(it.name.startsWith("layoutlib-") && it.name.endsWith(".jar"))
    }

    // Order: test classes, layoutlib first, :render + worker runtime, test deps, android.jar last.
    val ordered = linkedSetOf<File>()
    ordered.addAll(testClassesDirs.files)
    ordered.addAll(layoutlibJars)
    ordered.addAll(nonAndroidWorkerEntries)
    for (f in existingTestCp) {
      if (f.canonicalPath != androidJarPath) {
        ordered.add(f)
      }
    }
    ordered.add(androidSdkJar)
    classpath = files(ordered)
  }
}
