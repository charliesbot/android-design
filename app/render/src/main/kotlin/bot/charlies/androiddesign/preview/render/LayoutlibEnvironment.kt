package bot.charlies.androiddesign.preview.render

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Environment
import app.cash.paparazzi.PaparazziSdk
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts
import java.nio.file.Files
import java.util.zip.ZipFile

/** Creates and prepares a [PaparazziSdk] instance from a [BootstrapMeta] bundle. */
internal object LayoutlibEnvironment {
  fun open(meta: BootstrapMeta): PaparazziSdk {
    System.setProperty(
      "paparazzi.layoutlib.runtime.root",
      meta.layoutlibRuntimeDir.toAbsolutePath().normalize().toString(),
    )
    System.setProperty(
      "paparazzi.layoutlib.resources.root",
      meta.layoutlibResourcesDir.toAbsolutePath().normalize().toString(),
    )

    val rPackages =
      ZipFile(meta.workerRJar.toFile()).use { zip ->
        zip
          .entries()
          .asSequence()
          .map { it.name }
          .filter { it.endsWith("/R.class") }
          .map { it.removeSuffix("/R.class").replace('/', '.') }
          .sorted()
          .toList()
      }

    val libraryResourceDirs =
      Files.readAllLines(meta.workerResDirsFile).map { it.trim() }.filter { it.isNotEmpty() }

    val env =
      Environment(
        appTestDir = meta.bootstrapDir.toAbsolutePath().normalize().toString(),
        packageName = "bot.charlies.androiddesign.preview.host",
        compileSdkVersion = 37,
        resourcePackageNames = rPackages,
        localResourceDirs = emptyList(),
        moduleResourceDirs = emptyList(),
        libraryResourceDirs = libraryResourceDirs,
        allModuleAssetDirs = listOf(meta.workerAssetsDir.toAbsolutePath().normalize().toString()),
        libraryAssetDirs = emptyList(),
      )

    val sdk =
      PaparazziSdk(
        environment = env,
        deviceConfig = DeviceConfig.NEXUS_5,
        appCompatEnabled = false,
      )
    sdk.setup()
    sdk.prepare()
    ProposalFonts.init(sdk.context.assets)
    return sdk
  }
}
