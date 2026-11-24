package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.model.BoardSurface
import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.DeviceRenderTarget
import bot.charlies.androiddesign.preview.model.Finding
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.Severity
import bot.charlies.androiddesign.preview.model.Views
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CheckExecutorTest {

  private fun compileEntry(
    tmp: Path,
    surface: BoardSurface = BoardSurface.Phone,
  ): CompileEntry {
    val source = tmp.resolve("Board.explore.kt")
    if (!Files.exists(source)) Files.writeString(source, "val preview = Unit\n")
    return CompileEntry(
      sourcePath = source,
      designPath = null,
      contentHash = "abcdef1234567890abcdef1234567890",
      loaded =
        LoadedExploration(
          sourcePath = source,
          designPath = null,
          designTheme = null,
          designSystem = DesignSystemResolver.resolve(null, null),
          compileMs = 1L,
          errors = emptyList(),
        ),
      fromCache = false,
      compileCountAtLoad = 1,
      surface = surface,
    )
  }

  private fun pngBytes(tag: String): ByteArray =
    InMemoryRenderWorker.PNG_SIGNATURE + tag.toByteArray()

  private fun touchFinding(id: String): Finding =
    Finding(
      id = id,
      rule = FindingRule.TouchTarget,
      severity = Severity.Error,
      detail = "Too small",
    )

  private fun sampleTree(
    target: DeviceRenderTarget,
    findings: List<Finding> = emptyList(),
  ): LayoutTree =
    LayoutTree(
      screen = target.screen,
      view = Views.byName(target.viewName) ?: Views.Light,
      nodes = listOf(LayoutNode("root", "Box", bounds = Bounds(0f, 0f, 100f, 100f))),
      findings = findings,
    )

  @Test
  fun `primaryCheckViews returns Phone and Wear primary views`() {
    assertEquals(setOf("light", "desktop"), CheckExecutor.primaryCheckViews(BoardSurface.Phone))
    assertEquals(
      setOf("watch", "watch-large"),
      CheckExecutor.primaryCheckViews(BoardSurface.Wear),
    )
  }

  @Test
  fun `scoped batch check respects worker scopedCheckDensity, requests LZ4 for every target, and writes PNGs`() {
    val tmp = createTempDirectory("check-exec-scoped-")
    try {
      for (expectedDensity in listOf(1, 2)) {
        val worker = InMemoryRenderWorker(scopedCheckDensity = expectedDensity)
        val executor = CheckExecutor(worker)
        val entry = compileEntry(tmp)
        val targets =
          listOf(
            CheckTargetSpec("Main", "home", "Main-home", null, Views.Dark),
            CheckTargetSpec("Main", "home", "Main-home", "sheet", Views.Foldable),
          )
        val rendersRoot = tmp.resolve("renders-$expectedDensity")

        val artifacts = executor.renderBatchCheck(entry, targets, scoped = true, rendersRoot)

        assertEquals(2, artifacts.size)
        val requested = worker.recordedBatches.single()
        assertEquals(
          listOf(FrameFormat.LZ4_RGBA, FrameFormat.LZ4_RGBA),
          requested.map { it.frameFormat },
        )
        assertEquals(
          listOf(expectedDensity.toFloat(), expectedDensity.toFloat()),
          requested.map { it.densityScale },
        )
        assertTrue(artifacts.all { it.densityScale == expectedDensity })
        assertTrue(artifacts.all { it.pngPath != null && Files.isRegularFile(it.pngPath) })
      }
    } finally {
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `unscoped passing check requests LZ4 only for primary non-overlay targets and writes only primary PNGs`() {
    val tmp = createTempDirectory("check-exec-unscoped-")
    try {
      val worker = InMemoryRenderWorker()
      val executor = CheckExecutor(worker)
      val entry = compileEntry(tmp)
      val targets =
        PhoneSurface.views.map { view ->
          CheckTargetSpec("Main", "home", "Main-home", null, view)
        } + CheckTargetSpec("Main", "home", "Main-home", "dialog", Views.Light)

      val artifacts =
        executor.renderBatchCheck(entry, targets, scoped = false, tmp.resolve("renders"))

      assertEquals(6, artifacts.size)
      assertEquals(1, worker.renderBatchCount.get())
      val batchTargets = worker.recordedBatches.single()
      val requestedFormats = batchTargets.associate {
        "${it.viewName}:${it.overlayId ?: "base"}" to it.frameFormat
      }
      assertEquals(FrameFormat.LZ4_RGBA, requestedFormats["light:base"])
      assertEquals(FrameFormat.LZ4_RGBA, requestedFormats["desktop:base"])
      assertEquals(FrameFormat.NONE, requestedFormats["dark:base"])
      assertEquals(FrameFormat.NONE, requestedFormats["foldable:base"])
      assertEquals(FrameFormat.NONE, requestedFormats["tablet:base"])
      assertEquals(FrameFormat.NONE, requestedFormats["light:dialog"])

      val writtenViews =
        artifacts
          .filter { it.pngPath != null }
          .map { "${it.view.name}:${it.overlay ?: "base"}" }
          .toSet()
      assertEquals(setOf("light:base", "desktop:base"), writtenViews)
    } finally {
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `overlay evaluation diffs against base check and drops overlay layout`() {
    val tmp = createTempDirectory("check-exec-overlay-")
    try {
      val sharedFinding = touchFinding("shared-btn")
      val overlayOnlyFinding = touchFinding("overlay-btn")
      val worker =
        InMemoryRenderWorker(
          targetBuilder = { target ->
            val findings =
              when (target.overlayId) {
                null -> listOf(sharedFinding)
                "inherited-only" -> listOf(sharedFinding)
                else -> listOf(sharedFinding, overlayOnlyFinding)
              }
            WorkerTargetOutput(
              direction = target.direction,
              screen = target.screen,
              viewName = target.viewName,
              overlayId = target.overlayId,
              renderMs = 2L,
              tree = sampleTree(target, findings),
              frame =
                if (target.frameFormat == FrameFormat.NONE) null
                else
                  WorkerFrame(
                    pngBytes("${target.viewName}-${target.overlayId ?: "base"}"),
                    "image/png",
                  ),
            )
          },
        )
      val executor = CheckExecutor(worker)
      val entry = compileEntry(tmp)
      val targets =
        listOf(
          CheckTargetSpec("Main", "home", "Main-home", null, Views.Light),
          CheckTargetSpec("Main", "home", "Main-home", "inherited-only", Views.Light),
          CheckTargetSpec("Main", "home", "Main-home", "new-issue", Views.Light),
        )

      val artifacts =
        executor.renderBatchCheck(entry, targets, scoped = false, tmp.resolve("renders"))

      val base = artifacts[0]
      val inherited = artifacts[1]
      val added = artifacts[2]

      assertFalse(base.pass)
      assertNotNull(base.layout)
      assertEquals(listOf("shared-btn"), base.check.errors.flatMap { it.ids })

      assertTrue(inherited.pass, "Inherited base errors must be suppressed on overlay")
      assertNull(inherited.layout)

      assertFalse(added.pass)
      assertNull(added.layout)
      assertEquals(listOf("overlay-btn"), added.check.errors.flatMap { it.ids })
    } finally {
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `failing non-primary check fetches up to 3 missing frames and writes at most 2 deduplicated PNGs`() {
    val tmp = createTempDirectory("check-exec-followup-")
    try {
      val sharedFailBytes = pngBytes("duplicate-fail")
      val distinctFailBytes = pngBytes("unique-fail")
      val worker =
        InMemoryRenderWorker(
          targetBuilder = { target ->
            val findings =
              if (target.viewName == "light" || target.viewName == "desktop") emptyList()
              else listOf(touchFinding("fail-${target.screen}"))
            val frame =
              when {
                target.frameFormat == FrameFormat.NONE -> null
                target.viewName == "dark" || target.viewName == "foldable" ->
                  WorkerFrame(sharedFailBytes, "image/png")
                else -> WorkerFrame(distinctFailBytes + target.viewName.toByteArray(), "image/png")
              }
            WorkerTargetOutput(
              direction = target.direction,
              screen = target.screen,
              viewName = target.viewName,
              overlayId = target.overlayId,
              renderMs = 1L,
              tree = sampleTree(target, findings),
              frame = frame,
            )
          },
        )
      val executor = CheckExecutor(worker)
      val entry = compileEntry(tmp)
      // 4 failing non-primary targets + 1 passing primary target -> follow-up must cap at 3.
      val targets =
        listOf(
          CheckTargetSpec("Main", "home", "Main-home", null, Views.Light),
          CheckTargetSpec("Main", "home", "Main-home", null, Views.Dark),
          CheckTargetSpec("Main", "home", "Main-home", null, Views.Foldable),
          CheckTargetSpec("Main", "home", "Main-home", null, Views.Tablet),
          CheckTargetSpec("Main", "home", "Main-home", "sheet", Views.Light),
        )

      val artifacts =
        executor.renderBatchCheck(entry, targets, scoped = false, tmp.resolve("renders"))

      assertEquals(2, worker.renderBatchCount.get(), "Expected initial batch + 1 follow-up batch")
      val followUpTargets = worker.recordedBatches[1]
      assertEquals(listOf("dark", "foldable", "tablet"), followUpTargets.map { it.viewName })
      assertTrue(followUpTargets.all { it.frameFormat == FrameFormat.LZ4_RGBA })

      // dark and foldable share identical SHA-256 bytes, so dark and tablet are written (cap 2).
      val written = artifacts.filter { it.pngPath != null }
      assertEquals(listOf("dark", "tablet"), written.map { it.view.name })
      assertTrue(written.all { Files.isRegularFile(it.pngPath) })
    } finally {
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `unscoped check skips follow-up batch when 2 distinct failing primary PNGs already exist`() {
    val tmp = createTempDirectory("check-exec-skip-followup-")
    try {
      val worker =
        InMemoryRenderWorker(
          targetBuilder = { target ->
            WorkerTargetOutput(
              direction = target.direction,
              screen = target.screen,
              viewName = target.viewName,
              renderMs = 1L,
              tree = sampleTree(target, listOf(touchFinding("err-${target.viewName}"))),
              frame =
                if (target.frameFormat == FrameFormat.NONE) null
                else WorkerFrame(pngBytes("fail-${target.viewName}"), "image/png"),
            )
          },
        )
      val executor = CheckExecutor(worker)
      val entry = compileEntry(tmp)
      val targets =
        listOf(
          CheckTargetSpec("Main", "home", "Main-home", null, Views.Light),
          CheckTargetSpec("Main", "home", "Main-home", null, Views.Desktop),
          CheckTargetSpec("Main", "home", "Main-home", null, Views.Dark),
        )

      val artifacts =
        executor.renderBatchCheck(entry, targets, scoped = false, tmp.resolve("renders"))

      assertEquals(1, worker.renderBatchCount.get())
      val written = artifacts.filter { it.pngPath != null }
      assertEquals(listOf("light", "desktop"), written.map { it.view.name })
    } finally {
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `endTree and endFrame propagate onto RenderArtifact and write end PNG when writeFiles is true`() {
    val tmp = createTempDirectory("check-exec-endframe-")
    try {
      val worker =
        InMemoryRenderWorker(
          targetBuilder = { target ->
            val mainTree = sampleTree(target)
            val settledTree =
              mainTree.copy(
                nodes = listOf(LayoutNode("end", "Text", bounds = Bounds(0f, 0f, 50f, 20f))),
              )
            WorkerTargetOutput(
              direction = target.direction,
              screen = target.screen,
              viewName = target.viewName,
              overlayId = target.overlayId,
              renderMs = 3L,
              tree = mainTree,
              endTree = settledTree,
              frame = WorkerFrame(pngBytes("main"), "image/png"),
              endFrame = WorkerFrame(pngBytes("end"), "image/png"),
            )
          },
        )
      val executor = CheckExecutor(worker)
      val entry = compileEntry(tmp)

      val artifact =
        executor.renderSingle(
          entry,
          SingleRenderRequest(
            direction = "Main",
            screenId = "home",
            prefix = "Main-home",
            view = Views.Light,
            writeFiles = true,
            rendersRoot = tmp.resolve("renders"),
            frameFormat = FrameFormat.LZ4_RGBA,
          ),
        )

      val endTree = assertNotNull(artifact.endTree)
      assertEquals("end", endTree.nodes.single().id)
      assertNotNull(artifact.pngPath)
      val endPng = assertNotNull(artifact.endPngPath)
      assertTrue(endPng.fileName.toString().endsWith("-end.png"))
      assertTrue(Files.isRegularFile(endPng))
      assertEquals("Main-home-light-end.png", artifact.entry.endPng)
    } finally {
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `renderSingle converts WEBP_LOSSLESS to LZ4_RGBA when writeFiles is true and keeps WEBP when false`() {
    val tmp = createTempDirectory("check-exec-single-format-")
    try {
      val worker = InMemoryRenderWorker()
      val executor = CheckExecutor(worker)
      val entry = compileEntry(tmp)
      val rendersRoot = tmp.resolve("renders")

      val written =
        executor.renderSingle(
          entry,
          SingleRenderRequest(
            direction = "Main",
            screenId = "home",
            prefix = "Main-home",
            view = Views.Light,
            writeFiles = true,
            rendersRoot = rendersRoot,
            frameFormat = FrameFormat.WEBP_LOSSLESS,
          ),
        )
      assertEquals(FrameFormat.LZ4_RGBA, worker.recordedBatches[0].single().frameFormat)
      assertEquals("image/png", written.imageMime)
      assertNotNull(written.pngPath)
      assertTrue(Files.isRegularFile(written.pngPath))

      val inMemory =
        executor.renderSingle(
          entry,
          SingleRenderRequest(
            direction = "Main",
            screenId = "home",
            prefix = "Main-home",
            view = Views.Light,
            writeFiles = false,
            rendersRoot = rendersRoot,
            frameFormat = FrameFormat.WEBP_LOSSLESS,
          ),
        )
      assertEquals(FrameFormat.WEBP_LOSSLESS, worker.recordedBatches[1].single().frameFormat)
      assertEquals("image/webp", inMemory.imageMime)
      assertNull(inMemory.pngPath)
    } finally {
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `batch failure yields a failed RenderArtifact for every target`() {
    val tmp = createTempDirectory("check-exec-batch-fail-")
    try {
      val worker =
        InMemoryRenderWorker(
          batchOverride = { _, _, _ ->
            WorkerBatchResult(ok = false, renderTotalMs = 15L, error = "boom")
          },
        )
      val executor = CheckExecutor(worker)
      val entry = compileEntry(tmp)
      val targets =
        listOf(
          CheckTargetSpec("Main", "home", "Main-home", null, Views.Light),
          CheckTargetSpec("Main", "home", "Main-home", "dialog", Views.Dark),
        )

      val artifacts =
        executor.renderBatchCheck(entry, targets, scoped = false, tmp.resolve("renders"))

      assertEquals(2, artifacts.size)
      assertTrue(artifacts.all { !it.pass })
      assertTrue(artifacts.all { it.check.errors.any { err -> "boom" in err.detail.orEmpty() } })
      assertTrue(artifacts.all { it.pngPath == null && it.tree == null })
      assertEquals(listOf(15L, 15L), artifacts.map { it.renderMs })
    } finally {
      tmp.toFile().deleteRecursively()
    }
  }

  @Test
  fun `finishCheck runs once after two-pass batch check and even when renderTargets throws`() {
    val tmp = createTempDirectory("check-exec-finish-")
    try {
      val finishCalls = AtomicInteger(0)
      val baseWorker =
        InMemoryRenderWorker(
          targetBuilder = { target ->
            WorkerTargetOutput(
              direction = target.direction,
              screen = target.screen,
              viewName = target.viewName,
              renderMs = 1L,
              tree = sampleTree(target, listOf(touchFinding("err-${target.viewName}"))),
              frame =
                if (target.frameFormat == FrameFormat.NONE) null
                else WorkerFrame(pngBytes("fail-${target.viewName}"), "image/png"),
            )
          },
        )
      val countingWorker =
        object : RenderWorker by baseWorker {
          override fun finishCheck() {
            finishCalls.incrementAndGet()
          }
        }
      val executor = CheckExecutor(countingWorker)
      val entry = compileEntry(tmp)
      executor.renderBatchCheck(
        entry,
        listOf(
          CheckTargetSpec("Main", "home", "Main-home", null, Views.Light),
          CheckTargetSpec("Main", "home", "Main-home", null, Views.Dark),
        ),
        scoped = false,
        rendersRoot = tmp.resolve("renders"),
      )
      assertEquals(2, baseWorker.renderBatchCount.get())
      assertEquals(1, finishCalls.get())

      val throwingWorker =
        object : RenderWorker by InMemoryRenderWorker(onRenderBatch = { error("boom") }) {
          override fun finishCheck() {
            finishCalls.incrementAndGet()
          }
        }
      runCatching {
        CheckExecutor(throwingWorker)
          .renderSingle(
            entry,
            SingleRenderRequest(
              direction = "Main",
              screenId = "home",
              prefix = "Main-home",
              view = Views.Light,
              rendersRoot = tmp.resolve("renders"),
            ),
          )
      }
      assertEquals(2, finishCalls.get())
    } finally {
      tmp.toFile().deleteRecursively()
    }
  }
}
