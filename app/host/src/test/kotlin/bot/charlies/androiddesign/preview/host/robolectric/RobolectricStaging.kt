package bot.charlies.androiddesign.preview.host.robolectric

import bot.charlies.androiddesign.preview.app.android.AndroidExplorationCompiler
import bot.charlies.androiddesign.preview.app.server.RobolectricStage
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.RobolectricRenderRequest
import bot.charlies.androiddesign.preview.model.RobolectricRenderResponse
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Stages a source-only request the way `RobolectricPreviewBackend` does, through the device path's
 * own compiler. The runner uses it for callers that send only `sourcePath` (the eval harness's
 * report renders and tests), so every Robolectric render goes through the same compile,
 * BytecodeGuard, and asset collection as the device.
 */
internal object RobolectricStaging {
  private val meta by lazy { BootstrapMeta.locate() }

  fun stage(request: RobolectricRenderRequest): RobolectricRenderRequest = stageIn(request).second

  private fun stageIn(
    request: RobolectricRenderRequest,
  ): Pair<RobolectricStage, RobolectricRenderRequest> {
    val outcome =
      AndroidExplorationCompiler.compileClassStage(
        Paths.get(request.sourcePath),
        request.designPath?.let(Paths::get),
        meta,
      )
    return when (outcome) {
      is AndroidExplorationCompiler.ClassStageOutcome.Ready ->
        RobolectricStage.write(Files.createTempDirectory("robo-stage-"), outcome.stage).let {
          it to it.applyTo(request)
        }
      is AndroidExplorationCompiler.ClassStageOutcome.Failed ->
        throw StageFailure(outcome.errors.joinToString { "${it.path}: ${it.message}" })
    }
  }

  /** Runs [request] through the staged class path; a failed compile yields an invalid response. */
  fun handle(request: RobolectricRenderRequest): RobolectricRenderResponse {
    if (request.mode == "ping" || request.classesDir != null) {
      return RobolectricBoardRenderer.handleRequest(request)
    }
    if (!Files.isRegularFile(Paths.get(request.sourcePath))) {
      return RobolectricBoardRenderer.handleRequest(request)
    }
    val (stage, staged) =
      try {
        stageIn(request)
      } catch (e: StageFailure) {
        return RobolectricRenderResponse(
          id = request.id,
          ok = true,
          valid = false,
          pass = false,
          errors =
            listOf(
              bot.charlies.androiddesign.preview.model.ValidationError(
                request.sourcePath,
                e.message ?: "",
              ),
            ),
        )
      }
    return try {
      RobolectricBoardRenderer.handleRequest(staged)
    } finally {
      stage.dir.toFile().deleteRecursively()
    }
  }

  private class StageFailure(message: String) : RuntimeException(message)
}
