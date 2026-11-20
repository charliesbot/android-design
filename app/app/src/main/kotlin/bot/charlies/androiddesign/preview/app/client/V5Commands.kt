package bot.charlies.androiddesign.preview.app.client

import bot.charlies.androiddesign.preview.app.cli.Command
import bot.charlies.androiddesign.preview.app.server.CheckRequest
import bot.charlies.androiddesign.preview.app.server.CheckResponse
import bot.charlies.androiddesign.preview.app.server.ContentHasher
import bot.charlies.androiddesign.preview.app.server.ErrorEnvelope
import bot.charlies.androiddesign.preview.app.server.SelectionResponse
import bot.charlies.androiddesign.preview.app.server.ServerPaths
import bot.charlies.androiddesign.preview.app.server.TreeRequest
import bot.charlies.androiddesign.preview.app.server.TreeResponse
import bot.charlies.androiddesign.preview.model.PreviewJson
import java.io.PrintStream
import java.net.SocketTimeoutException
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.Serializable

/** Fixed exit codes for v5 CLI commands. */
object V5ExitCode {
  const val OK = 0
  const val FAILED = 1
  const val USAGE = 2
  const val NOT_READY = 3
}

private fun emitJson(out: PrintStream, json: String) {
  out.println(json.replace("\r", " ").replace("\n", " "))
  out.flush()
}

private fun emitError(
  out: PrintStream,
  code: Int,
  error: String,
  message: String,
  next: String? = null,
  ask: String? = null,
  tell: String? = null,
): Int {
  val envelope =
    ErrorEnvelope(ok = false, error = error, message = message, next = next, ask = ask, tell = tell)
  emitJson(out, PreviewJson.compact.encodeToString(ErrorEnvelope.serializer(), envelope))
  return code
}

private fun wantsHelp(args: List<String>): Boolean = args.any { it == "--help" || it == "-h" }

private data class SimpleArgs(
  val positionals: List<String>,
  val flags: Map<String, String>,
)

private fun parseV5Args(
  args: List<String>,
  valueFlags: Set<String>,
): Pair<SimpleArgs?, String?> {
  val positionals = mutableListOf<String>()
  val flags = LinkedHashMap<String, String>()
  var i = 0
  while (i < args.size) {
    val arg = args[i]
    when {
      arg in valueFlags -> {
        val value = args.getOrNull(++i) ?: return null to "$arg requires a value"
        flags[arg] = value
      }
      arg.startsWith("--") -> return null to "Unknown option: $arg"
      else -> positionals += arg
    }
    i++
  }
  return SimpleArgs(positionals, flags) to null
}

/** `check <file> [--direction d] [--screen s] [--view v] [--design path]` */
class CheckCommand(private val client: PreviewClient = PreviewClient()) : Command {
  override val name = "check"
  override val usage =
    "check <file> [--direction d] [--screen s] [--view v] [--design path] [--help]"

  override fun run(args: List<String>, out: PrintStream, err: PrintStream): Int {
    if (wantsHelp(args)) {
      out.println(HELP)
      return V5ExitCode.OK
    }
    val (parsed, parseErr) =
      parseV5Args(args, setOf("--direction", "--screen", "--view", "--design"))
    if (parseErr != null || parsed == null) {
      return emitError(
        out,
        V5ExitCode.USAGE,
        "usage",
        parseErr ?: "Invalid arguments",
        next = "android-design-preview check --help",
      )
    }
    val rawFile = parsed.positionals.firstOrNull()
    if (rawFile.isNullOrBlank() || parsed.positionals.size > 1) {
      return emitError(
        out,
        V5ExitCode.USAGE,
        "usage",
        "Provide exactly one .explore.kt file path",
        next = "android-design-preview check <file>",
      )
    }
    val sourcePath = Path.of(rawFile).toAbsolutePath().normalize()
    if (!Files.isRegularFile(sourcePath)) {
      return emitError(
        out,
        V5ExitCode.USAGE,
        "no_such_file",
        "No such file: $sourcePath",
      )
    }
    val explicitDesign = parsed.flags["--design"]?.let(Path::of)
    val repoRoot = ServerPaths.resolveRepoRoot(sourcePath)
    val direction = parsed.flags["--direction"]
    val screen = parsed.flags["--screen"]
    val view = parsed.flags["--view"]
    val scoped = !direction.isNullOrBlank() || !screen.isNullOrBlank() || !view.isNullOrBlank()
    val scopeKey =
      listOfNotNull(
          direction?.takeIf { it.isNotBlank() }?.let { "dir=$it" },
          screen?.takeIf { it.isNotBlank() }?.let { "screen=$it" },
          view?.takeIf { it.isNotBlank() }?.let { "view=$it" },
        )
        .ifEmpty { listOf("full") }
        .joinToString(",")

    // Check recorded watchdog timeouts BEFORE overwriting a dead server record on respawn.
    val snapshot = ContentHasher.snapshot(sourcePath, explicitDesign)
    if (client.checkRecordedTimeout(repoRoot, snapshot, scopeKey, direction, screen, view)) {
      val resp =
        CheckResponse(
          ok = false,
          valid = false,
          pass = false,
          scoped = scoped,
          contentHash = snapshot.contentHash,
          sourcePath = sourcePath.toString(),
          designPath = snapshot.designPath?.toString(),
          error = "timeout",
          tell =
            "Previous attempt for this exact file timed out ($scopeKey). Edit and save the file to clear the timeout.",
        )
      emitJson(out, PreviewJson.compact.encodeToString(CheckResponse.serializer(), resp))
      return V5ExitCode.FAILED
    }

    val lookup = client.ensureServer(repoRoot)
    val ready =
      when (lookup) {
        is PreviewClient.ServerLookup.Ready -> lookup
        is PreviewClient.ServerLookup.NotReady ->
          return emitError(out, V5ExitCode.NOT_READY, "server_not_ready", lookup.message)
      }

    val req =
      CheckRequest(
        path = sourcePath.toString(),
        design = explicitDesign?.toAbsolutePath()?.normalize()?.toString(),
        direction = direction,
        screen = screen,
        view = view,
      )
    val body = PreviewJson.compact.encodeToString(CheckRequest.serializer(), req)
    val timeoutMs = client.deadlineMs(isCold = ready.status.compileCount == 0, scoped = scoped)

    val http =
      try {
        client.postJson(ready.record, "/api/check", body, readTimeoutMs = timeoutMs)
      } catch (_: SocketTimeoutException) {
        client.killAndRespawn(repoRoot, ready.record)
        return emitError(
          out,
          V5ExitCode.FAILED,
          "timeout",
          "Request exceeded ${timeoutMs} ms deadline; restarted preview server.",
          tell =
            "Check timed out (possible infinite loop or slow composition). Simplify the view and retry.",
        )
      } catch (e: Exception) {
        if (client.checkRecordedTimeout(repoRoot, snapshot, scopeKey, direction, screen, view)) {
          val resp =
            CheckResponse(
              ok = false,
              valid = false,
              pass = false,
              scoped = scoped,
              contentHash = snapshot.contentHash,
              sourcePath = sourcePath.toString(),
              designPath = snapshot.designPath?.toString(),
              error = "timeout",
              tell =
                "Preview server halted after timing out ($scopeKey). Simplify the view and save the file to retry.",
            )
          emitJson(out, PreviewJson.compact.encodeToString(CheckResponse.serializer(), resp))
          return V5ExitCode.FAILED
        }
        return emitError(
          out,
          V5ExitCode.NOT_READY,
          "server_unreachable",
          e.message ?: "Failed contacting preview server",
        )
      }

    if (http.status == 400 || http.status == 404) {
      emitJson(out, http.body.ifBlank { """{"ok":false,"error":"bad_request"}""" })
      return V5ExitCode.USAGE
    }
    if (http.status >= 500) {
      emitJson(out, http.body.ifBlank { """{"ok":false,"error":"server_error"}""" })
      return V5ExitCode.FAILED
    }
    emitJson(out, http.body)
    val parsedResp =
      try {
        PreviewJson.compact.decodeFromString(CheckResponse.serializer(), http.body)
      } catch (_: Exception) {
        return V5ExitCode.FAILED
      }
    return if (parsedResp.ok && parsedResp.valid && parsedResp.pass) V5ExitCode.OK
    else V5ExitCode.FAILED
  }

  companion object {
    val HELP =
      """
      Usage: android-design-preview check <file> [--direction d] [--screen s] [--view v] [--design path]

      Compile and check a Compose exploration file.
      - Scoped (any of --direction, --screen, --view): checks only the target and writes 1x PNG path(s), whether findings pass or fail.
      - Unscoped (no scope flags): full gate across every direction, screen, overlay, and view at 2x density.
        Failing full checks return at most 2 deduplicated failing PNG paths; passing full checks return light + desktop (or watch + watch-large on Wear).

      Output: one JSON line with ok, valid, pass, scoped, contentHash, compileMs, renderMs, compileCount,
      cached, surface, boardUrl, errors, renders (with check findings, reference anchors, and hints),
      images (path, direction, screen, view, density, pass), and selection (present when the user clicked
      on the board since the previous command).

      Exit codes:
        0  All requested checks passed
        1  Compile error, finding failure, or timeout
        2  Usage error (missing file, unknown direction/screen/view)
        3  Server or prerequisites not ready
      """
        .trimIndent()
  }
}

/** `tree <file> --direction d --view v [--screen s] [--overlay o] [--max-depth n]` */
class TreeCommand(private val client: PreviewClient = PreviewClient()) : Command {
  override val name = "tree"
  override val usage =
    "tree <file> --direction d --view v [--screen s] [--overlay o] [--max-depth n] [--help]"

  override fun run(args: List<String>, out: PrintStream, err: PrintStream): Int {
    if (wantsHelp(args)) {
      out.println(HELP)
      return V5ExitCode.OK
    }
    val (parsed, parseErr) =
      parseV5Args(args, setOf("--direction", "--screen", "--overlay", "--view", "--max-depth"))
    if (parseErr != null || parsed == null) {
      return emitError(
        out,
        V5ExitCode.USAGE,
        "usage",
        parseErr ?: "Invalid arguments",
        next = "android-design-preview tree --help",
      )
    }
    val rawFile = parsed.positionals.firstOrNull()
    val direction = parsed.flags["--direction"]
    val view = parsed.flags["--view"]
    if (rawFile.isNullOrBlank() || direction.isNullOrBlank() || view.isNullOrBlank()) {
      return emitError(
        out,
        V5ExitCode.USAGE,
        "usage",
        "tree requires <file>, --direction <d>, and --view <v>",
        next = "android-design-preview tree <file> --direction <d> --view <v>",
      )
    }
    val maxDepth =
      parsed.flags["--max-depth"]?.let { raw ->
        raw.toIntOrNull()
          ?: return emitError(
            out,
            V5ExitCode.USAGE,
            "usage",
            "--max-depth must be an integer",
          )
      }
    val sourcePath = Path.of(rawFile).toAbsolutePath().normalize()
    if (!Files.isRegularFile(sourcePath)) {
      return emitError(out, V5ExitCode.USAGE, "no_such_file", "No such file: $sourcePath")
    }
    val repoRoot = ServerPaths.resolveRepoRoot(sourcePath)
    val ready =
      when (val lookup = client.ensureServer(repoRoot)) {
        is PreviewClient.ServerLookup.Ready -> lookup
        is PreviewClient.ServerLookup.NotReady ->
          return emitError(out, V5ExitCode.NOT_READY, "server_not_ready", lookup.message)
      }

    val req =
      TreeRequest(
        path = sourcePath.toString(),
        direction = direction,
        screen = parsed.flags["--screen"],
        overlay = parsed.flags["--overlay"],
        view = view,
        maxDepth = maxDepth,
      )
    val timeoutMs = client.deadlineMs(isCold = ready.status.compileCount == 0, scoped = true)
    val snapshot = ContentHasher.snapshot(sourcePath)
    val screen = parsed.flags["--screen"]
    val treeScope =
      listOfNotNull(
          direction.takeIf { it.isNotBlank() }?.let { "dir=$it" },
          screen?.takeIf { it.isNotBlank() }?.let { "screen=$it" },
          view.takeIf { it.isNotBlank() }?.let { "view=$it" },
        )
        .joinToString(",")
    val http =
      try {
        client.postJson(
          ready.record,
          "/api/tree",
          PreviewJson.compact.encodeToString(TreeRequest.serializer(), req),
          readTimeoutMs = timeoutMs,
        )
      } catch (_: SocketTimeoutException) {
        client.killAndRespawn(repoRoot, ready.record)
        return emitError(out, V5ExitCode.FAILED, "timeout", "tree request timed out")
      } catch (e: Exception) {
        if (client.checkRecordedTimeout(repoRoot, snapshot, treeScope, direction, screen, view)) {
          return emitError(
            out,
            V5ExitCode.FAILED,
            "timeout",
            "Preview server halted after timing out ($treeScope).",
          )
        }
        return emitError(
          out,
          V5ExitCode.NOT_READY,
          "server_unreachable",
          e.message ?: "Failed contacting preview server",
        )
      }
    emitJson(out, http.body)
    return when (http.status) {
      200 -> {
        val resp =
          try {
            PreviewJson.compact.decodeFromString(TreeResponse.serializer(), http.body)
          } catch (_: Exception) {
            null
          }
        if (resp?.ok == true) V5ExitCode.OK else V5ExitCode.FAILED
      }
      400,
      404 -> V5ExitCode.USAGE
      else -> V5ExitCode.FAILED
    }
  }

  companion object {
    val HELP =
      """
      Usage: android-design-preview tree <file> --direction <d> --view <v> [--screen <s>] [--overlay <o>] [--max-depth <n>]

      Return layout bounds, Composable call names, and file:line source locations for one render.
      Use this when a finding needs exact coordinates or source lines.

      Exit codes:
        0  Tree returned
        1  Compile or render failure
        2  Usage error (missing flags or unknown direction/screen/view)
        3  Server or prerequisites not ready
      """
        .trimIndent()
  }
}

/** `selection <file>` */
class SelectionCommand(private val client: PreviewClient = PreviewClient()) : Command {
  override val name = "selection"
  override val usage = "selection <file> [--help]"

  override fun run(args: List<String>, out: PrintStream, err: PrintStream): Int {
    if (wantsHelp(args)) {
      out.println(HELP)
      return V5ExitCode.OK
    }
    val (parsed, parseErr) = parseV5Args(args, emptySet())
    if (parseErr != null || parsed == null) {
      return emitError(out, V5ExitCode.USAGE, "usage", parseErr ?: "Invalid arguments")
    }
    val rawFile = parsed.positionals.firstOrNull()
    if (rawFile.isNullOrBlank() || parsed.positionals.size > 1) {
      return emitError(
        out,
        V5ExitCode.USAGE,
        "usage",
        "selection requires one <file> path",
        next = "android-design-preview selection <file>",
      )
    }
    val sourcePath = Path.of(rawFile).toAbsolutePath().normalize()
    val repoRoot = ServerPaths.resolveRepoRoot(sourcePath)
    val ready =
      when (val lookup = client.probe(repoRoot)) {
        is PreviewClient.ServerLookup.Ready -> lookup
        is PreviewClient.ServerLookup.NotReady -> {
          val resp =
            SelectionResponse(
              ok = false,
              error = "no_selection",
              ask = "Click the element you mean on the preview board, then tell me to continue.",
            )
          emitJson(out, PreviewJson.compact.encodeToString(SelectionResponse.serializer(), resp))
          return V5ExitCode.FAILED
        }
      }
    val encoded = URLEncoder.encode(sourcePath.toString(), Charsets.UTF_8)
    val http =
      try {
        client.get(ready.record, "/api/selection?path=$encoded", readTimeoutMs = 3_000)
      } catch (e: Exception) {
        return emitError(
          out,
          V5ExitCode.NOT_READY,
          "server_unreachable",
          e.message ?: "Failed contacting preview server",
        )
      }
    emitJson(out, http.body)
    return if (http.status == 200) V5ExitCode.OK else V5ExitCode.FAILED
  }

  companion object {
    val HELP =
      """
      Usage: android-design-preview selection <file>

      Return the last node clicked on the browser board for <file> (file, line, callName,
      callChain, direction, screen, view, bounds, contentHash, stale).

      Exit codes:
        0  Selection returned
        1  No selection recorded (includes ask prompt for the person)
        2  Usage error
        3  Server or prerequisites not ready
      """
        .trimIndent()
  }
}

/** `open <file>` */
class OpenCommand(private val client: PreviewClient = PreviewClient()) : Command {
  override val name = "open"
  override val usage = "open <file> [--help]"

  @Serializable
  data class OpenResponse(
    val ok: Boolean = true,
    val boardUrl: String,
    val opened: Boolean,
    val sourcePath: String? = null,
    val tell: String? = null,
  )

  override fun run(args: List<String>, out: PrintStream, err: PrintStream): Int {
    if (wantsHelp(args)) {
      out.println(HELP)
      return V5ExitCode.OK
    }
    val (parsed, parseErr) = parseV5Args(args, emptySet())
    if (parseErr != null || parsed == null) {
      return emitError(out, V5ExitCode.USAGE, "usage", parseErr ?: "Invalid arguments")
    }
    val rawFile = parsed.positionals.firstOrNull()
    val targetPath =
      rawFile?.let { Path.of(it).toAbsolutePath().normalize() }
        ?: Path.of(".").toAbsolutePath().normalize()
    val repoRoot = ServerPaths.resolveRepoRoot(targetPath)
    val ready =
      when (val lookup = client.ensureServer(repoRoot)) {
        is PreviewClient.ServerLookup.Ready -> lookup
        is PreviewClient.ServerLookup.NotReady ->
          return emitError(out, V5ExitCode.NOT_READY, "server_not_ready", lookup.message)
      }

    // If a file was provided and nothing is compiled yet, trigger a quick scoped check so the board
    // has content.
    if (
      rawFile != null &&
        Files.isRegularFile(targetPath) &&
        targetPath.fileName.toString().endsWith(".explore.kt") &&
        ready.status.activeFile == null
    ) {
      try {
        val req = CheckRequest(path = targetPath.toString())
        client.postJson(
          ready.record,
          "/api/check",
          PreviewJson.compact.encodeToString(CheckRequest.serializer(), req),
          readTimeoutMs = client.deadlineMs(isCold = true, scoped = false),
        )
      } catch (_: Exception) {
        // Still return boardUrl even if initial compile failed or timed out.
      }
    }

    val url = ready.record.boardUrl()
    val noOpen = System.getenv(PreviewClient.NO_OPEN_ENV) == "1"
    val opened = if (noOpen) false else openBrowser(url)
    val resp =
      OpenResponse(
        ok = true,
        boardUrl = url,
        opened = opened,
        sourcePath = if (rawFile != null) targetPath.toString() else null,
      )
    emitJson(out, PreviewJson.compact.encodeToString(OpenResponse.serializer(), resp))
    return V5ExitCode.OK
  }

  private fun openBrowser(url: String): Boolean {
    val os = System.getProperty("os.name").orEmpty().lowercase()
    val cmd =
      when {
        "mac" in os -> listOf("open", url)
        else -> listOf("xdg-open", url)
      }
    return try {
      ProcessBuilder(cmd).start()
      true
    } catch (_: Exception) {
      false
    }
  }

  companion object {
    val HELP =
      """
      Usage: android-design-preview open [<file>]

      Ensure the preview server is running, print its boardUrl JSON line, and open it in the
      default browser (suppressed when ANDROID_DESIGN_NO_OPEN=1).

      Exit codes:
        0  Board URL printed
        2  Usage error
        3  Server or prerequisites not ready
      """
        .trimIndent()
  }
}

/** `status [path]` */
class StatusCommand(private val client: PreviewClient = PreviewClient()) : Command {
  override val name = "status"
  override val usage = "status [<file-or-dir>] [--help]"

  override fun run(args: List<String>, out: PrintStream, err: PrintStream): Int {
    if (wantsHelp(args)) {
      out.println(HELP)
      return V5ExitCode.OK
    }
    val (parsed, parseErr) = parseV5Args(args, emptySet())
    if (parseErr != null || parsed == null) {
      return emitError(out, V5ExitCode.USAGE, "usage", parseErr ?: "Invalid arguments")
    }
    val target =
      parsed.positionals.firstOrNull()?.let { Path.of(it).toAbsolutePath().normalize() }
        ?: Path.of(".").toAbsolutePath().normalize()
    val repoRoot = ServerPaths.resolveRepoRoot(target)
    return when (val lookup = client.probe(repoRoot)) {
      is PreviewClient.ServerLookup.Ready -> {
        emitJson(
          out,
          PreviewJson.compact.encodeToString(
            bot.charlies.androiddesign.preview.app.server.StatusResponse.serializer(),
            lookup.status,
          ),
        )
        V5ExitCode.OK
      }
      is PreviewClient.ServerLookup.NotReady ->
        emitError(
          out,
          V5ExitCode.NOT_READY,
          "server_not_running",
          lookup.message,
          next = "android-design-preview check <file>",
        )
    }
  }

  companion object {
    val HELP =
      """
      Usage: android-design-preview status [<file-or-dir>]

      Report server state (pid, port, jarVersion, compileCount, boardUrl) for the repository.

      Exit codes:
        0  Server is running and answering
        2  Usage error
        3  Server is not running
      """
        .trimIndent()
  }
}

/** `stop [path]` */
class StopCommand(private val client: PreviewClient = PreviewClient()) : Command {
  override val name = "stop"
  override val usage = "stop [<file-or-dir>] [--help]"

  @Serializable
  data class StopResponse(
    val ok: Boolean = true,
    val stopped: Boolean,
    val pid: Long? = null,
    val repoRoot: String,
  )

  override fun run(args: List<String>, out: PrintStream, err: PrintStream): Int {
    if (wantsHelp(args)) {
      out.println(HELP)
      return V5ExitCode.OK
    }
    val (parsed, parseErr) = parseV5Args(args, emptySet())
    if (parseErr != null || parsed == null) {
      return emitError(out, V5ExitCode.USAGE, "usage", parseErr ?: "Invalid arguments")
    }
    val target =
      parsed.positionals.firstOrNull()?.let { Path.of(it).toAbsolutePath().normalize() }
        ?: Path.of(".").toAbsolutePath().normalize()
    val repoRoot = ServerPaths.resolveRepoRoot(target)
    val stopped = client.stop(repoRoot)
    val resp =
      StopResponse(
        ok = true,
        stopped = stopped != null,
        pid = stopped?.pid,
        repoRoot = repoRoot.toString(),
      )
    emitJson(out, PreviewJson.compact.encodeToString(StopResponse.serializer(), resp))
    return V5ExitCode.OK
  }

  companion object {
    val HELP =
      """
      Usage: android-design-preview stop [<file-or-dir>]

      Shut down the running preview server for the repository (no-op if already stopped).

      Exit codes:
        0  Server stopped (or already stopped)
        2  Usage error
      """
        .trimIndent()
  }
}

/** `install [--bin-dir <dir>] [--script <path>]` */
class InstallCommand : Command {
  override val name = "install"
  override val usage = "install [--bin-dir <dir>] [--help]"

  @Serializable
  data class InstallResponse(
    val ok: Boolean = true,
    val source: String,
    val target: String,
    val tell: String? = null,
  )

  override fun run(args: List<String>, out: PrintStream, err: PrintStream): Int {
    if (wantsHelp(args)) {
      out.println(HELP)
      return V5ExitCode.OK
    }
    val (parsed, parseErr) = parseV5Args(args, setOf("--bin-dir", "--script"))
    if (parseErr != null || parsed == null) {
      return emitError(out, V5ExitCode.USAGE, "usage", parseErr ?: "Invalid arguments")
    }
    val scriptPathStr =
      parsed.flags["--script"]
        ?: System.getenv(PreviewClient.SCRIPT_ENV)?.takeIf { it.isNotBlank() }
    if (scriptPathStr.isNullOrBlank()) {
      return emitError(
        out,
        V5ExitCode.NOT_READY,
        "missing_script",
        "Run install through plugin/bin/android-design-preview or pass --script <path>",
      )
    }
    val source = Path.of(scriptPathStr).toAbsolutePath().normalize()
    if (!Files.isRegularFile(source)) {
      return emitError(out, V5ExitCode.USAGE, "no_such_file", "Bootstrap script not found: $source")
    }
    val binDir =
      parsed.flags["--bin-dir"]?.let { Path.of(it).toAbsolutePath().normalize() }
        ?: Path.of(System.getProperty("user.home"), ".local", "bin").toAbsolutePath().normalize()
    Files.createDirectories(binDir)
    val target = binDir.resolve("android-design-preview")
    if (target != source) {
      Files.deleteIfExists(target)
      try {
        Files.createSymbolicLink(target, source)
      } catch (_: Exception) {
        Files.writeString(target, "#!/bin/sh\nexec \"$source\" \"\$@\"\n")
        target.toFile().setExecutable(true)
      }
    }
    val pathEnv = System.getenv("PATH").orEmpty()
    val inPath =
      pathEnv.split(java.io.File.pathSeparator).any {
        try {
          Path.of(it).toAbsolutePath().normalize() == binDir
        } catch (_: Exception) {
          false
        }
      }
    val tell =
      if (inPath) null
      else "Add $binDir to your PATH so `android-design-preview` is available in new shells."
    val resp =
      InstallResponse(
        ok = true,
        source = source.toString(),
        target = target.toString(),
        tell = tell,
      )
    emitJson(out, PreviewJson.compact.encodeToString(InstallResponse.serializer(), resp))
    return V5ExitCode.OK
  }

  companion object {
    val HELP =
      """
      Usage: android-design-preview install [--bin-dir <dir>]

      Link the android-design-preview bootstrap script into ~/.local/bin (or --bin-dir) so
      harnesses that do not put the plugin's bin/ directory on PATH can run it by name.

      Exit codes:
        0  Installed link successfully
        2  Usage error
        3  Bootstrap script path unavailable
      """
        .trimIndent()
  }
}
