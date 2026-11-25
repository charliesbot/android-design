package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.app.client.ClientExitCode
import bot.charlies.androiddesign.preview.app.client.PreviewClient
import bot.charlies.androiddesign.preview.app.server.CheckExecutor
import bot.charlies.androiddesign.preview.app.server.CompileEntry
import bot.charlies.androiddesign.preview.app.server.ErrorEnvelope
import bot.charlies.androiddesign.preview.app.server.RenderRequest
import bot.charlies.androiddesign.preview.app.server.RenderResponse
import bot.charlies.androiddesign.preview.app.server.RenderWorker
import bot.charlies.androiddesign.preview.app.server.ReportRenderer
import bot.charlies.androiddesign.preview.app.server.ServerPaths
import bot.charlies.androiddesign.preview.model.PreviewJson
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path

/**
 * `render <exploration.explore.kt> --out <dir> [--design <DESIGN.md>] [--views a,b]
 * [--screens a,b]`
 *
 * Compiles and draws each exploration screen and overlay at each view via the shared Android
 * preview server (`POST /api/render`), writing `<screen>-<view>.png`, `<screen>-<view>.tree.json`,
 * and `report.json` into `<dir>`. Exit 0 when valid and every render passes, 1 when a render has
 * error findings or threw, 2 when the exploration fails validation/compilation, and 3 when the
 * preview server or prerequisites are not ready.
 */
class RenderCommand(
  private val workerFactory: (() -> RenderWorker)? = null,
  private val client: PreviewClient = PreviewClient(),
) : Command {
  override val name = "render"
  override val usage =
    "render <exploration.explore.kt> --out <dir> [--design <DESIGN.md>] [--views light,dark,...] [--screens a,b]"

  private class Options(
    val source: Path,
    val out: Path,
    val design: Path?,
    val views: List<String>?,
    val screens: List<String>?,
  )

  override fun run(args: List<String>, out: PrintStream, err: PrintStream): Int {
    if (args.any { it == "--help" || it == "-h" }) {
      out.println(
        """
        Usage: android-design-preview $usage

        Compile and render every screen, overlay, and view headlessly into <dir>, writing
        <screen>-<view>.png, <screen>-<view>.tree.json, and report.json.

        Exit codes:
          0  Valid and every render passed
          1  One or more renders failed checks or threw
          2  Invalid exploration or compilation error
          3  Preview server or prerequisites not ready
        """
          .trimIndent(),
      )
      return ExitCode.OK
    }
    val options = parse(args, err) ?: return ExitCode.USAGE
    val sourcePath = options.source.toAbsolutePath().normalize()
    if (!Files.isRegularFile(sourcePath)) {
      err.println("no such file: $sourcePath")
      return ExitCode.USAGE
    }
    val outDir = options.out.toAbsolutePath().normalize()
    val explicitDesign = options.design?.toAbsolutePath()?.normalize()

    val factory = workerFactory
    if (factory != null) {
      val executor = CheckExecutor(factory())
      val result =
        try {
          ReportRenderer.render(
            sourcePath = sourcePath,
            outDir = outDir,
            explicitDesign = explicitDesign,
            requestedViews = options.views,
            requestedScreens = options.screens,
            compile = { snapshot ->
              val compiled = executor.compile(snapshot)
              CompileEntry(
                sourcePath = sourcePath,
                designPath = compiled.loaded.designPath,
                contentHash = snapshot.contentHash,
                loaded = compiled.loaded,
                fromCache = false,
                compileCountAtLoad = 1,
                dexHash = compiled.dexHash,
                surface = compiled.surface,
                boardName = compiled.boardName,
                boardMeta = compiled.boardMeta,
                androidData = compiled.androidData,
                boardTheme = compiled.boardTheme,
              )
            },
            renderSingle = { entry, req -> executor.renderSingle(entry, req) },
          )
        } finally {
          executor.close()
        }
      result.messages.forEach(err::println)
      return result.exitCode
    }

    // Fast local input validation so invalid files don't boot a server/emulator just to fail.
    if (
      !sourcePath.fileName.toString().endsWith(".explore.kt") ||
        !isValidUtf8(sourcePath) ||
        DesignMdLoader.load(sourcePath, explicitDesign).errors.isNotEmpty()
    ) {
      val localOnly =
        ReportRenderer.render(
          sourcePath = sourcePath,
          outDir = outDir,
          explicitDesign = explicitDesign,
          requestedViews = options.views,
          requestedScreens = options.screens,
          compile = { error("Unreachable compile during local validation") },
          renderSingle = { _, _ -> error("Unreachable render during local validation") },
        )
      localOnly.messages.forEach(err::println)
      return localOnly.exitCode
    }

    val repoRoot = ServerPaths.resolveRepoRoot(sourcePath)
    val ready =
      when (val lookup = client.ensureServer(repoRoot, headless = true)) {
        is PreviewClient.ServerLookup.Ready -> lookup
        is PreviewClient.ServerLookup.NotReady -> {
          err.println(lookup.message)
          return ClientExitCode.NOT_READY
        }
      }

    val req =
      RenderRequest(
        path = sourcePath.toString(),
        outDir = outDir.toString(),
        design = explicitDesign?.toString(),
        views = options.views,
        screens = options.screens,
        repoRoot = repoRoot.toString(),
      )
    val timeoutMs =
      maxOf(client.deadlineMs(isCold = ready.status.compileCount == 0, scoped = false), 120_000)
    val http =
      try {
        client.postJson(
          ready.record,
          "/api/render",
          PreviewJson.compact.encodeToString(RenderRequest.serializer(), req),
          readTimeoutMs = timeoutMs,
        )
      } catch (e: Exception) {
        val repVer = client.checkReplacedByVersion(ready.record)
        if (repVer != null) {
          err.println("replaced by v$repVer, rerun")
          return ExitCode.FINDINGS_FAILED
        }
        err.println(e.message ?: "Failed contacting preview server")
        return ClientExitCode.NOT_READY
      }

    return when (http.status) {
      in 200..299 -> {
        val parsed =
          try {
            PreviewJson.compact.decodeFromString(RenderResponse.serializer(), http.body)
          } catch (e: Exception) {
            err.println("Invalid render response from server: ${e.message}")
            return ExitCode.FINDINGS_FAILED
          }
        parsed.messages.forEach(err::println)
        parsed.exitCode
      }
      409 -> {
        val msg = parseErrorMessage(http.body) ?: "Preview server was replaced, rerun"
        err.println(msg)
        ExitCode.FINDINGS_FAILED
      }
      400 -> {
        err.println(parseErrorMessage(http.body) ?: http.body)
        ExitCode.USAGE
      }
      else -> {
        err.println(parseErrorMessage(http.body) ?: http.body)
        ExitCode.FINDINGS_FAILED
      }
    }
  }

  private fun isValidUtf8(path: Path): Boolean =
    try {
      Files.readString(path, Charsets.UTF_8)
      true
    } catch (_: Exception) {
      false
    }

  private fun parseErrorMessage(body: String): String? =
    try {
      PreviewJson.compact.decodeFromString(ErrorEnvelope.serializer(), body).message
    } catch (_: Exception) {
      null
    }

  private fun parse(args: List<String>, err: PrintStream): Options? {
    val parsed =
      parseArgs(args, setOf("--out", "--design", "--views", "--screens"), err) ?: return null
    val source = parsed.positional
    val out = parsed.flags["--out"]
    val design = parsed.flags["--design"]
    val views = parsed.flags["--views"]
    val screens = parsed.flags["--screens"]
    if (source == null || out == null) {
      err.println("usage: $usage")
      return null
    }
    return Options(
      source = Path.of(source),
      out = Path.of(out),
      design = design?.let(Path::of),
      views = views?.split(',')?.map { it.trim() },
      screens = screens?.split(',')?.map { it.trim() },
    )
  }
}
