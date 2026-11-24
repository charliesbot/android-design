package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.android.AndroidExplorationCompiler
import bot.charlies.androiddesign.preview.model.RobolectricRenderRequest
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * A compiled exploration on disk, in the form the Robolectric runner loads: `.class` files and
 * content-addressed asset blobs. It carries what the device receives (guarded bytecode and the
 * collected asset map), so the runner never reads the exploration folder itself.
 */
class RobolectricStage
private constructor(
  val dir: Path,
  private val classesDir: Path,
  private val assetBlobsDir: Path,
  private val assetMap: Map<String, String>,
) {
  /** Adds this stage's classes and assets to [request]. */
  fun applyTo(request: RobolectricRenderRequest): RobolectricRenderRequest =
    request.copy(
      classesDir = classesDir.toString(),
      assetMap = assetMap,
      assetBlobsDir = assetBlobsDir.toString(),
    )

  fun delete() {
    dir.toFile().deleteRecursively()
  }

  companion object {
    /** Writes [stage] beneath [dir], which must be empty or absent. */
    fun write(dir: Path, stage: AndroidExplorationCompiler.ClassStage): RobolectricStage {
      val classes = dir.resolve("classes")
      val blobs = dir.resolve("assets")
      Files.createDirectories(classes)
      Files.createDirectories(blobs)
      for ((className, bytes) in stage.classBytes) {
        val file = classes.resolve(className.replace('.', File.separatorChar) + ".class")
        Files.createDirectories(file.parent)
        Files.write(file, bytes)
      }
      for ((sha, bytes) in stage.assetBlobs) {
        Files.write(blobs.resolve(sha), bytes)
      }
      return RobolectricStage(dir, classes, blobs, stage.assetMap)
    }
  }
}
