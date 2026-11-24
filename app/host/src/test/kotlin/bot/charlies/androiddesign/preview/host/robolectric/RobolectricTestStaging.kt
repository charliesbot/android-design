package bot.charlies.androiddesign.preview.host.robolectric

import bot.charlies.androiddesign.preview.app.android.AndroidExplorationCompiler
import bot.charlies.androiddesign.preview.app.server.RobolectricStage
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.RobolectricRenderRequest
import bot.charlies.androiddesign.preview.model.RobolectricRenderResponse
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Stages a request's source the way `RobolectricPreviewBackend` does, through the device path's own
 * compiler, so tests drive the runner with the same inputs it gets in production.
 */
internal object RobolectricTestStaging {
  private val meta by lazy { BootstrapMeta.locate() }

  fun stage(request: RobolectricRenderRequest): RobolectricRenderRequest {
    val outcome =
      AndroidExplorationCompiler.compileClassStage(
        Paths.get(request.sourcePath),
        request.designPath?.let(Paths::get),
        meta,
      )
    return when (outcome) {
      is AndroidExplorationCompiler.ClassStageOutcome.Ready ->
        RobolectricStage.write(Files.createTempDirectory("robo-test-stage-"), outcome.stage)
          .applyTo(request)
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
    val staged =
      try {
        stage(request)
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
    return RobolectricBoardRenderer.handleRequest(staged)
  }

  private class StageFailure(message: String) : RuntimeException(message)
}
