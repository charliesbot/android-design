package bot.charlies.androiddesign.preview.app.android

import bot.charlies.androiddesign.preview.app.server.AndroidRenderWorker
import bot.charlies.androiddesign.preview.app.server.CheckExecutor
import bot.charlies.androiddesign.preview.app.server.CheckTargetSpec
import bot.charlies.androiddesign.preview.app.server.CompileEntry
import bot.charlies.androiddesign.preview.app.server.ContentHasher
import bot.charlies.androiddesign.preview.app.server.LayoutlibRenderWorker
import bot.charlies.androiddesign.preview.app.server.RenderArtifact
import bot.charlies.androiddesign.preview.app.server.planBatchTargets
import bot.charlies.androiddesign.preview.app.server.resolveMetaTargets
import bot.charlies.androiddesign.preview.domain.CheckError
import bot.charlies.androiddesign.preview.domain.CheckNote
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.Views
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.CRC32
import java.util.zip.Deflater
import kotlin.math.abs
import kotlin.math.max
import kotlin.test.assertIs
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Emulator parity gate for #188 Task 2: compares Android host renders (`Pixel_10_Pro_XL`) against
 * `LayoutlibRenderWorker` across the multiturn proposal fixture, calibration bad fixtures, Wear
 * fixture (including scroll `endTree`), and Phone overlays (`AlertDialog`, `ModalBottomSheet`,
 * `DropdownMenu`), writing `evals/results/v7/parity.json`.
 *
 * Gated by `-Dadp.runEmulatorTests=true`.
 */
class LayoutlibParityEmulatorTest {

  @Serializable
  private data class ParityReport(
    val schemaVersion: Int = 1,
    val generatedAt: String,
    val avd: String,
    val toleranceDp: Float,
    val totalTargets: Int,
    val matchedTargets: Int,
    val knownDifferenceCount: Int,
    val unexplainedDifferenceCount: Int,
    val fixtures: List<FixtureParitySummary>,
    val knownDifferences: List<KnownDifferenceRecord>,
    val unexplainedDifferences: List<UnexplainedDifferenceRecord>,
  )

  @Serializable
  private data class FixtureParitySummary(
    val fixture: String,
    val targetsTotal: Int,
    val targetsMatched: Int,
    val endTreesCompared: Int,
    val maxBoundsDeltaDp: Float,
    val maxInkDeltaDp: Float,
    val maxTouchDeltaDp: Float,
    val knownDifferenceCount: Int,
    val unexplainedDifferenceCount: Int,
    val targets: List<TargetParityRecord>,
  )

  @Serializable
  private data class TargetParityRecord(
    val target: String,
    val endTree: Boolean,
    val nodesAndroid: Int,
    val nodesLayoutlib: Int,
    val androidPass: Boolean,
    val layoutlibPass: Boolean,
    val maxBoundsDeltaDp: Float,
    val maxInkDeltaDp: Float,
    val maxTouchDeltaDp: Float,
    val findingsMatched: Boolean,
    val classification: String,
  )

  @Serializable
  private data class KnownDifferenceRecord(
    val fixture: String,
    val target: String,
    val category: String,
    val summary: String,
  )

  @Serializable
  private data class UnexplainedDifferenceRecord(
    val fixture: String,
    val target: String,
    val reason: String,
  )

  private data class ComparisonStats(
    var maxBoundsDelta: Float = 0f,
    var maxInkDelta: Float = 0f,
    var maxTouchDelta: Float = 0f,
  )

  private fun repoRoot(): Path {
    val prop = System.getProperty("adp.repo.root")?.takeIf { it.isNotBlank() }
    if (prop != null) return Path.of(prop)
    var cur = Path.of("").toAbsolutePath()
    while (cur.parent != null) {
      if (
        Files.exists(cur.resolve("evals")) && Files.exists(cur.resolve("app/settings.gradle.kts"))
      ) {
        return cur
      }
      cur = cur.parent
    }
    error("Unable to locate repository root")
  }

  private fun copyEvalAssets(repoRoot: Path, destAssetsDir: Path) {
    val srcAssets = repoRoot.resolve("evals/assets")
    Files.createDirectories(destAssetsDir)
    Files.list(srcAssets).use { stream ->
      stream.forEach { file ->
        if (Files.isRegularFile(file)) {
          Files.copy(file, destAssetsDir.resolve(file.fileName))
        }
      }
    }
  }

  private fun compileToEntry(
    executor: CheckExecutor,
    snapshot: ContentHasher.Snapshot,
    fixtureName: String,
  ): CompileEntry {
    val out = executor.compile(snapshot)
    check(out.loaded.errors.isEmpty() && out.compiledData != null && out.boardMeta != null) {
      "[${executor.name}] Failed to compile $fixtureName: ${out.loaded.errors}"
    }
    return CompileEntry(
      sourcePath = snapshot.sourcePath,
      designPath = snapshot.designPath,
      contentHash = snapshot.contentHash,
      loaded = out.loaded,
      fromCache = false,
      compileCountAtLoad = 1,
      dexHash = out.dexHash,
      surface = out.surface,
      boardName = out.boardName,
      boardMeta = out.boardMeta,
      compiledData = out.compiledData,
      boardTheme = out.boardTheme,
    )
  }

  private fun targetLabel(spec: CheckTargetSpec, isEndTree: Boolean = false): String = buildString {
    append(spec.direction)
    append('/')
    append(spec.screenId)
    if (spec.overlayId != null) {
      append('+')
      append(spec.overlayId)
    }
    append('/')
    append(spec.view.name)
    if (isEndTree) append("+end")
  }

  private fun boundsDelta(a: Bounds, b: Bounds): Float =
    max(
      max(abs(a.x - b.x), abs(a.y - b.y)),
      max(abs(a.width - b.width), abs(a.height - b.height)),
    )

  private fun normalizedErrors(tree: LayoutTree, check: ViewCheck): List<String> {
    val byId = tree.nodes.associateBy { it.id }
    return check.errors
      .map { err ->
        val nodeDesc =
          err.ids
            .map { id ->
              val n = byId[id]
              if (n == null) id else "${n.type}:${n.text?.value.orEmpty()}"
            }
            .sorted()
            .joinToString(",")
        "${err.rule}|${err.detail.orEmpty()}|$nodeDesc"
      }
      .sorted()
  }

  private fun normalizedNotes(tree: LayoutTree, check: ViewCheck): List<String> {
    val byId = tree.nodes.associateBy { it.id }
    return check.notes
      .map { note ->
        val nodeDesc =
          note.ids
            .map { id ->
              val n = byId[id]
              if (n == null) id else "${n.type}:${n.text?.value.orEmpty()}"
            }
            .sorted()
            .joinToString(",")
        "${note.rule}|$nodeDesc"
      }
      .sorted()
  }

  private val prettyJson = Json { prettyPrint = true }

  private val contrastRatioRegex =
    Regex("""^([0-9]+(?:\.[0-9]+)?):1, needs ([0-9]+(?:\.[0-9]+)?):1$""")

  private fun CheckError.pretty(): String = "$rule(detail=$detail, ids=$ids)"

  private fun CheckNote.pretty(): String = "$rule(ids=$ids)"

  private fun semanticErrorsWithoutIdHash(tree: LayoutTree, check: ViewCheck): List<String> {
    val byId = tree.nodes.associateBy { it.id }
    return check.errors
      .map { err ->
        val nodeDesc =
          err.ids
            .map { id ->
              val n = byId[id]
              if (n == null) id else "${n.type}@L${n.sourceLine ?: -1}"
            }
            .sorted()
            .joinToString(",")
        "${err.rule}|${err.detail.orEmpty()}|$nodeDesc"
      }
      .sorted()
  }

  private fun semanticNotesWithoutIdHash(tree: LayoutTree, check: ViewCheck): List<String> {
    val byId = tree.nodes.associateBy { it.id }
    return check.notes
      .map { note ->
        val nodeDesc =
          note.ids
            .map { id ->
              val n = byId[id]
              if (n == null) id else "${n.type}@L${n.sourceLine ?: -1}"
            }
            .sorted()
            .joinToString(",")
        "${note.rule}|$nodeDesc"
      }
      .sorted()
  }

  private fun classifyContrastBlurDiff(
    aTree: LayoutTree,
    bTree: LayoutTree,
    aCheck: ViewCheck,
    bCheck: ViewCheck,
  ): String? {
    if (aCheck.pass != bCheck.pass) return null
    if (normalizedNotes(aTree, aCheck) != normalizedNotes(bTree, bCheck)) return null
    val aNonContrast =
      normalizedErrors(aTree, aCheck).filterNot { it.startsWith("${FindingRule.Contrast.wire}|") }
    val bNonContrast =
      normalizedErrors(bTree, bCheck).filterNot { it.startsWith("${FindingRule.Contrast.wire}|") }
    if (aNonContrast != bNonContrast) return null

    val aById = aTree.nodes.associateBy { it.id }
    val bById = bTree.nodes.associateBy { it.id }
    fun keyFor(ids: List<String>, byId: Map<String, LayoutNode>): String =
      ids
        .map { id ->
          val n = byId[id]
          if (n == null) id else "${n.type}:${n.text?.value.orEmpty()}@L${n.sourceLine ?: -1}"
        }
        .sorted()
        .joinToString(",")

    val aContrast =
      aCheck.errors
        .filter { it.rule == FindingRule.Contrast.wire }
        .sortedBy { keyFor(it.ids, aById) }
    val bContrast =
      bCheck.errors
        .filter { it.rule == FindingRule.Contrast.wire }
        .sortedBy { keyFor(it.ids, bById) }
    if (aContrast.size != bContrast.size || aContrast.isEmpty()) return null

    val diffs = mutableListOf<String>()
    for (idx in aContrast.indices) {
      val ae = aContrast[idx]
      val be = bContrast[idx]
      if (keyFor(ae.ids, aById) != keyFor(be.ids, bById)) return null
      val am = contrastRatioRegex.matchEntire(ae.detail.orEmpty()) ?: return null
      val bm = contrastRatioRegex.matchEntire(be.detail.orEmpty()) ?: return null
      val aRatio = am.groupValues[1].toFloatOrNull() ?: return null
      val bRatio = bm.groupValues[1].toFloatOrNull() ?: return null
      if (am.groupValues[2] != bm.groupValues[2]) return null
      if (abs(aRatio - bRatio) > 0.05f) return null
      if (aRatio != bRatio) {
        diffs += "${ae.ids}: ${aRatio}:1 vs ${bRatio}:1 (needs ${am.groupValues[2]}:1)"
      }
    }
    if (diffs.isEmpty()) return null
    return "Blur backdrop contrast rounding within 0.05 ratio: ${diffs.joinToString("; ")}"
  }

  private fun compareNodesStrict(
    aTree: LayoutTree,
    bTree: LayoutTree,
    toleranceDp: Float,
    stats: ComparisonStats,
    emulatorTextValuesAt: (nodeIndex: Int, baselineValue: String) -> Set<String>,
  ): Pair<List<String>, List<String>> {
    val issues = mutableListOf<String>()
    val knownNotes = mutableListOf<String>()
    if (aTree.nodes.size != bTree.nodes.size) {
      issues += "Node count mismatch: android=${aTree.nodes.size}, layoutlib=${bTree.nodes.size}"
      return knownNotes to issues
    }
    for (i in aTree.nodes.indices) {
      val a = aTree.nodes[i]
      val b = bTree.nodes[i]
      val tag = "node[$i](${a.type}/${a.text?.value.orEmpty()})"
      var nonValueDiffs = 0
      if (a.type != b.type) {
        issues += "$tag type mismatch: android=${a.type}, layoutlib=${b.type}"
        nonValueDiffs++
      }
      if (a.primary != b.primary) {
        issues += "$tag primary mismatch: android=${a.primary}, layoutlib=${b.primary}"
        nonValueDiffs++
      }
      if (a.scrolls != b.scrolls) {
        issues += "$tag scrolls mismatch: android=${a.scrolls}, layoutlib=${b.scrolls}"
        nonValueDiffs++
      }
      val bDelta = boundsDelta(a.bounds, b.bounds)
      stats.maxBoundsDelta = max(stats.maxBoundsDelta, bDelta)
      if (bDelta > toleranceDp) {
        issues += "$tag bounds delta $bDelta > $toleranceDp (a=${a.bounds}, b=${b.bounds})"
        nonValueDiffs++
      }
      if ((a.touch == null) != (b.touch == null)) {
        issues += "$tag touch nullness mismatch: android=${a.touch}, layoutlib=${b.touch}"
        nonValueDiffs++
      } else if (a.touch != null && b.touch != null) {
        val tDelta = boundsDelta(a.touch!!, b.touch!!)
        stats.maxTouchDelta = max(stats.maxTouchDelta, tDelta)
        if (tDelta > toleranceDp) {
          issues += "$tag touch delta $tDelta > $toleranceDp (a=${a.touch}, b=${b.touch})"
          nonValueDiffs++
        }
      }
      if ((a.text == null) != (b.text == null)) {
        issues += "$tag text nullness mismatch: android=${a.text}, layoutlib=${b.text}"
      } else if (a.text != null && b.text != null) {
        val at = a.text!!
        val bt = b.text!!
        if (at.lines != bt.lines) {
          issues += "$tag text.lines mismatch: android=${at.lines}, layoutlib=${bt.lines}"
          nonValueDiffs++
        }
        if (at.overflow != bt.overflow) {
          issues += "$tag text.overflow mismatch: android=${at.overflow}, layoutlib=${bt.overflow}"
          nonValueDiffs++
        }
        if (at.truncated != bt.truncated) {
          issues +=
            "$tag text.truncated mismatch: android=${at.truncated}, layoutlib=${bt.truncated}"
          nonValueDiffs++
        }
        if (at.undrawn != bt.undrawn) {
          issues += "$tag text.undrawn mismatch: android=${at.undrawn}, layoutlib=${bt.undrawn}"
          nonValueDiffs++
        }
        if ((at.ink == null) != (bt.ink == null)) {
          issues += "$tag text.ink nullness mismatch: android=${at.ink}, layoutlib=${bt.ink}"
          nonValueDiffs++
        } else if (at.ink != null && bt.ink != null) {
          val iDelta = boundsDelta(at.ink!!, bt.ink!!)
          stats.maxInkDelta = max(stats.maxInkDelta, iDelta)
          if (iDelta > toleranceDp) {
            issues += "$tag text.ink delta $iDelta > $toleranceDp (a=${at.ink}, b=${bt.ink})"
            nonValueDiffs++
          }
        }
        if (at.value != bt.value) {
          val emuValues = emulatorTextValuesAt(i, at.value)
          if (nonValueDiffs == 0 && emuValues.size > 1) {
            knownNotes +=
              "$tag text.value varied across emulator sandbox restarts ($emuValues); layoutlib='${bt.value}'"
          } else {
            issues +=
              "$tag text.value mismatch: android='${at.value}' (emu=$emuValues), layoutlib='${bt.value}'"
          }
        }
      }
    }
    return knownNotes to issues
  }

  private fun compareSheetOverlay(
    aTree: LayoutTree,
    bTree: LayoutTree,
    toleranceDp: Float,
    stats: ComparisonStats,
  ): Pair<String, List<String>> {
    val issues = mutableListOf<String>()
    if (aTree.nodes.size != bTree.nodes.size) {
      issues +=
        "Sheet node count mismatch: android=${aTree.nodes.size}, layoutlib=${bTree.nodes.size}"
      return "count-mismatch" to issues
    }
    var maxVerticalShift = 0f
    for (i in aTree.nodes.indices) {
      val a = aTree.nodes[i]
      val b = bTree.nodes[i]
      val tag = "node[$i](${a.type}/${a.text?.value.orEmpty()})"
      if (a.type != b.type) issues += "$tag type mismatch: ${a.type} vs ${b.type}"
      if (a.text?.value != b.text?.value) {
        issues += "$tag text.value mismatch: ${a.text?.value} vs ${b.text?.value}"
      }
      if (a.text?.lines != b.text?.lines) {
        issues += "$tag text.lines mismatch: ${a.text?.lines} vs ${b.text?.lines}"
      }
      val dx = abs(a.bounds.x - b.bounds.x)
      val dw = abs(a.bounds.width - b.bounds.width)
      val dy = abs(a.bounds.y - b.bounds.y)
      val dh = abs(a.bounds.height - b.bounds.height)
      stats.maxBoundsDelta = max(stats.maxBoundsDelta, max(dx, dw))
      maxVerticalShift = max(maxVerticalShift, max(dy, dh))
      if (dx > toleranceDp || dw > toleranceDp) {
        issues += "$tag sheet horizontal mismatch dx=$dx dw=$dw"
      }
      // Emulator includes system navigation bar inset (~24..48 dp) below ModalBottomSheet.
      if (dy > 48f || dh > 48f) {
        issues += "$tag sheet vertical shift exceeded expected nav inset (dy=$dy, dh=$dh)"
      }
    }
    return "ModalBottomSheet max vertical delta %.2f dp without emulator nav bar inset"
      .format(maxVerticalShift) to issues
  }

  private fun comparePopupOverlay(
    aTree: LayoutTree,
    bTree: LayoutTree,
    aCheck: ViewCheck,
    bCheck: ViewCheck,
  ): Pair<String, List<String>> {
    val issues = mutableListOf<String>()
    val layoutlibOverflows = bCheck.errors.filter { it.rule == FindingRule.Overflow.wire }
    if (layoutlibOverflows.isNotEmpty()) {
      issues +=
        "Expected 0 overflow errors on layoutlib DropdownMenu, got: ${layoutlibOverflows.map { it.pretty() }}"
    }
    val aTexts = aTree.nodes.mapNotNull { it.text?.value }.toSet()
    val bTexts = bTree.nodes.mapNotNull { it.text?.value }.toSet()
    if (!bTexts.containsAll(aTexts) || !aTexts.containsAll(bTexts)) {
      issues += "DropdownMenu text set mismatch: android=$aTexts, layoutlib=$bTexts"
    }
    val androidPos = aTree.nodes.firstOrNull { it.type == "Menu" || it.type == "MenuItem" }?.bounds
    val layoutlibPos =
      bTree.nodes.firstOrNull { it.type == "Menu" || it.type == "MenuItem" }?.bounds
    val summary =
      "DropdownMenu popup positioned cleanly with 0 layoutlib overflow errors " +
        "(android=$androidPos, layoutlib=$layoutlibPos, androidPass=${aCheck.pass}, layoutlibPass=${bCheck.pass})"
    return summary to issues
  }

  private fun writeSampleCoverPng(dir: Path) {
    val width = 16
    val height = 16
    val raw = ByteArray((1 + width * 4) * height)
    var pos = 0
    for (y in 0 until height) {
      raw[pos++] = 0
      for (x in 0 until width) {
        raw[pos++] = 0x5B.toByte()
        raw[pos++] = 0x4C.toByte()
        raw[pos++] = 0xEE.toByte()
        raw[pos++] = 0xFF.toByte()
      }
    }
    val deflater = Deflater(Deflater.BEST_SPEED)
    val deflated =
      ByteArrayOutputStream().use { baos ->
        deflater.setInput(raw)
        deflater.finish()
        val buf = ByteArray(1024)
        while (!deflater.finished()) {
          val n = deflater.deflate(buf)
          if (n > 0) baos.write(buf, 0, n)
        }
        deflater.end()
        baos.toByteArray()
      }
    val out = ByteArrayOutputStream()
    out.write(byteArrayOf(137.toByte(), 80, 78, 71, 13, 10, 26, 10))
    fun chunk(type: String, payload: ByteArray) {
      out.write(ByteBuffer.allocate(4).putInt(payload.size).array())
      val typeBytes = type.toByteArray(Charsets.US_ASCII)
      out.write(typeBytes)
      out.write(payload)
      val crc = CRC32()
      crc.update(typeBytes)
      crc.update(payload)
      out.write(ByteBuffer.allocate(4).putInt(crc.value.toInt()).array())
    }
    chunk(
      "IHDR",
      ByteBuffer.allocate(13)
        .putInt(width)
        .putInt(height)
        .put(8)
        .put(6)
        .put(0)
        .put(0)
        .put(0)
        .array(),
    )
    chunk("IDAT", deflated)
    chunk("IEND", ByteArray(0))
    val asset = dir.resolve("assets/cover.png")
    Files.createDirectories(asset.parent)
    Files.write(asset, out.toByteArray())
  }

  private fun phoneOverlaysExploreSource(): String =
    """
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.size
    import androidx.compose.foundation.layout.widthIn
    import androidx.compose.material3.AlertDialog
    import androidx.compose.material3.Button
    import androidx.compose.material3.Card
    import androidx.compose.material3.DropdownMenu
    import androidx.compose.material3.DropdownMenuItem
    import androidx.compose.material3.ExperimentalMaterial3Api
    import androidx.compose.material3.FilterChip
    import androidx.compose.material3.FloatingActionButton
    import androidx.compose.material3.ListItem
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.ModalBottomSheet
    import androidx.compose.material3.NavigationBar
    import androidx.compose.material3.NavigationBarItem
    import androidx.compose.material3.OutlinedTextField
    import androidx.compose.material3.Scaffold
    import androidx.compose.material3.Text
    import androidx.compose.material3.TextButton
    import androidx.compose.material3.TopAppBar
    import androidx.compose.material3.rememberModalBottomSheetState
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.testTag
    import androidx.compose.ui.unit.dp
    import bot.charlies.androiddesign.preview.kit.AD_PRIMARY_TAG
    import bot.charlies.androiddesign.preview.kit.BundledFont
    import bot.charlies.androiddesign.preview.kit.PaletteVariant
    import bot.charlies.androiddesign.preview.kit.SymbolIcon
    import bot.charlies.androiddesign.preview.kit.designBoard
    import bot.charlies.androiddesign.preview.kit.designPainter

    @OptIn(ExperimentalMaterial3Api::class)
    val preview = designBoard(
      name = "Music Discovery",
      seed = 0x5B4CEEL,
      variant = PaletteVariant.Expressive,
      brandFont = BundledFont.Fraunces,
    ) {
      direction("Editorial") {
        screen("home") {
          Scaffold(
            topBar = { TopAppBar(title = { Text("Editorial Mix") }) },
            bottomBar = {
              NavigationBar {
                NavigationBarItem(selected = true, onClick = {}, icon = { SymbolIcon("home") }, label = { Text("Home") })
                NavigationBarItem(selected = false, onClick = {}, icon = { SymbolIcon("library_music") }, label = { Text("Library") })
              }
            },
            floatingActionButton = { FloatingActionButton(onClick = {}) { SymbolIcon("add") } },
          ) { innerPadding ->
            Column(
              modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp).widthIn(max = 420.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                  headlineContent = { Text("Late Night Mix") },
                  supportingContent = { Text("32 tracks") },
                  leadingContent = {
                    Image(
                      painter = designPainter("assets/cover.png"),
                      contentDescription = "Cover",
                      modifier = Modifier.size(40.dp),
                    )
                  },
                  trailingContent = { FilterChip(selected = true, onClick = {}, label = { Text("Live") }) },
                )
              }
              OutlinedTextField(value = "Search", onValueChange = {}, label = { Text("Filter") }, modifier = Modifier.fillMaxWidth())
              Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(56.dp).testTag(AD_PRIMARY_TAG),
              ) { Text("Play Queue") }
            }
          }
        }
        overlay("dialog", screen = "home") {
          AlertDialog(
            onDismissRequest = {},
            confirmButton = { TextButton(onClick = {}) { Text("Delete") } },
            title = { Text("Delete playlist?") },
          )
        }
        overlay("sheet", screen = "home") {
          ModalBottomSheet(
            onDismissRequest = {},
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
          ) {
            Column(Modifier.fillMaxWidth().padding(24.dp)) {
              Text("Share Queue", style = MaterialTheme.typography.titleLarge)
            }
          }
        }
        overlay("popup", screen = "home") {
          Box(Modifier.fillMaxSize().padding(top = 64.dp, start = 24.dp)) {
            DropdownMenu(expanded = true, onDismissRequest = {}) {
              DropdownMenuItem(text = { Text("Rename") }, onClick = {})
            }
          }
        }
      }
    }
    """
      .trimIndent()

  private fun wearExploreSource(): String =
    """
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.padding
    import androidx.compose.runtime.Composable
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.testTag
    import androidx.compose.ui.unit.dp
    import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
    import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
    import androidx.wear.compose.material3.AlertDialog
    import androidx.wear.compose.material3.AlertDialogDefaults
    import androidx.wear.compose.material3.AppScaffold
    import androidx.wear.compose.material3.Button
    import androidx.wear.compose.material3.DatePicker
    import androidx.wear.compose.material3.EdgeButton
    import androidx.wear.compose.material3.FilledTonalButton
    import androidx.wear.compose.material3.ListHeader
    import androidx.wear.compose.material3.ScreenScaffold
    import androidx.wear.compose.material3.SurfaceTransformation
    import androidx.wear.compose.material3.Text
    import androidx.wear.compose.material3.TimeSource
    import androidx.wear.compose.material3.TimeText
    import androidx.wear.compose.material3.TitleCard
    import androidx.wear.compose.material3.curveToEdge
    import androidx.wear.compose.material3.lazy.rememberTransformationSpec
    import androidx.wear.compose.material3.lazy.transformedHeight
    import bot.charlies.androiddesign.preview.kit.AD_PRIMARY_TAG
    import bot.charlies.androiddesign.preview.kit.wearBoard
    import java.time.LocalDate

    val preview = wearBoard("Wear Fitness", seed = 0x00897BL) {
      screen("main") {
        val state = rememberTransformingLazyColumnState()
        AppScaffold(
          timeText = {
            TimeText(
              timeSource = object : TimeSource {
                @Composable override fun currentTime() = "10:09"
              },
            )
          },
        ) {
          ScreenScaffold(
            scrollState = state,
            edgeButton = { EdgeButton(onClick = {}, modifier = Modifier.testTag(AD_PRIMARY_TAG)) { Text("Start") } },
          ) { contentPadding ->
            val spec = rememberTransformationSpec()
            TransformingLazyColumn(state = state, contentPadding = contentPadding) {
              item { ListHeader(Modifier.fillMaxWidth().transformedHeight(this, spec)) { Text("Runs") } }
              item {
                Button(
                  onClick = {},
                  modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                  transformation = SurfaceTransformation(spec),
                  label = { Text("Outdoor Run") },
                  secondaryLabel = { Text("5.2 km") },
                )
              }
              item {
                TitleCard(
                  onClick = {},
                  title = { Text("Weekly Goal") },
                  time = { Text("Now") },
                  modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                  transformation = SurfaceTransformation(spec),
                ) { Text("3 of 5 done") }
              }
              item {
                FilledTonalButton(
                  onClick = {},
                  modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                  transformation = SurfaceTransformation(spec),
                  label = { Text("Intervals") },
                )
              }
            }
          }
        }
      }
      screen("curved") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
          Text("GPS Locked", modifier = Modifier.padding(top = 12.dp).curveToEdge())
        }
      }
      screen("date") {
        DatePicker(initialDate = LocalDate.of(2026, 11, 22), onDatePicked = {})
      }
      overlay("confirm", screen = "main") {
        AlertDialog(
          visible = true,
          onDismissRequest = {},
          confirmButton = { AlertDialogDefaults.ConfirmButton(onClick = {}) },
          title = { Text("Save run?") },
        )
      }
    }
    """
      .trimIndent()

  @Test
  fun `layoutlib matches android emulator host across phone wear and overlays`(@TempDir tmp: Path) {
    Assumptions.assumeTrue(
      System.getProperty("adp.runEmulatorTests") == "true",
      "Skipped unless -Dadp.runEmulatorTests=true",
    )

    val root = repoRoot()
    val meta = BootstrapMeta.locate()
    val cache = tmp.resolve("cache")
    val avdName = "Pixel_10_Pro_XL"
    val toleranceDp = 0.6f

    val deviceManager = DeviceManager(cacheRoot = cache)
    val ready =
      assertIs<DeviceSetupResult.Ready>(
        deviceManager.ensureDeviceReady(meta, requestedAvd = avdName, headless = true),
      )
    val currentPort = AtomicInteger(ready.forwardPort)
    val transport =
      AdbTcpDeviceTransport(
        connectAndEnsurePort = {
          val ping = DeviceManager.pingLocalForward(currentPort.get())
          if (ping != null && ping.ok) {
            currentPort.get()
          } else {
            val refreshed =
              deviceManager.ensureDeviceReady(
                meta = meta,
                requestedSerial = ready.serial,
                requestedAvd = avdName,
                headless = true,
              )
            if (refreshed is DeviceSetupResult.Ready) {
              currentPort.set(refreshed.forwardPort)
              refreshed.forwardPort
            } else {
              currentPort.get()
            }
          }
        },
        restartSandboxActivity = { deviceManager.restartSandboxOnSerial(ready.serial) },
      )

    val androidExecutor = CheckExecutor(AndroidRenderWorker(meta = meta, transport = transport))
    val layoutlibExecutor = CheckExecutor(LayoutlibRenderWorker(meta = meta))
    androidExecutor.prepare()
    layoutlibExecutor.prepare()

    try {
      // Prepare isolated fixture workspaces so real assets & DESIGN.md load identically on both
      // backends.
      val proposalDir = tmp.resolve("proposal").also { Files.createDirectories(it) }
      Files.copy(
        root.resolve("evals/multiturn_fixture/DESIGN.md"),
        proposalDir.resolve("DESIGN.md"),
      )
      val proposalFile = proposalDir.resolve("Proposal.explore.kt")
      Files.copy(
        root.resolve("evals/multiturn_fixture/design/proposal.explore.kt"),
        proposalFile,
      )
      copyEvalAssets(root, proposalDir.resolve("assets"))

      val settingsDir = tmp.resolve("settings").also { Files.createDirectories(it) }
      val settingsFile = settingsDir.resolve("SettingsBad.explore.kt")
      Files.copy(
        root.resolve("evals/calibration/code-settings-bad.explore.kt"),
        settingsFile,
      )

      val recipesDir = tmp.resolve("recipes").also { Files.createDirectories(it) }
      val recipesFile = recipesDir.resolve("RecipesBad.explore.kt")
      Files.copy(
        root.resolve("evals/calibration/code-recipes-bad.explore.kt"),
        recipesFile,
      )
      copyEvalAssets(root, recipesDir.resolve("assets"))

      val wearDir = tmp.resolve("wear").also { Files.createDirectories(it) }
      val wearFile = wearDir.resolve("Wear.explore.kt")
      Files.writeString(wearFile, wearExploreSource())

      val overlaysDir = tmp.resolve("overlays").also { Files.createDirectories(it) }
      writeSampleCoverPng(overlaysDir)
      val overlaysFile = overlaysDir.resolve("Overlays.explore.kt")
      Files.writeString(overlaysFile, phoneOverlaysExploreSource())

      data class FixtureCase(
        val name: String,
        val sourceFile: Path,
        val designFile: Path?,
        val views: List<bot.charlies.androiddesign.preview.model.ViewSpec>,
      )

      val cases =
        listOf(
          FixtureCase("proposal", proposalFile, proposalDir.resolve("DESIGN.md"), Views.all),
          FixtureCase("code-settings-bad", settingsFile, null, Views.all),
          FixtureCase("code-recipes-bad", recipesFile, null, Views.all),
          FixtureCase("wear", wearFile, null, Views.wear),
          FixtureCase("overlays", overlaysFile, null, listOf(Views.Light)),
        )

      val fixtureSummaries = mutableListOf<FixtureParitySummary>()
      val allKnown =
        mutableListOf(
          KnownDifferenceRecord(
            fixture = "all",
            target = "*",
            category = "elevation-shadows",
            summary =
              "Pixel elevation shadow blur differs slightly between hardware UI and layoutlib and is excluded from LayoutTree checks.",
          ),
        )
      val allUnexplained = mutableListOf<UnexplainedDifferenceRecord>()

      for (case in cases) {
        val snapshot = ContentHasher.snapshot(case.sourceFile, case.designFile)
        val androidEntry = compileToEntry(androidExecutor, snapshot, case.name)
        val layoutlibEntry = compileToEntry(layoutlibExecutor, snapshot, case.name)

        val metaTargets = resolveMetaTargets(androidEntry.boardMeta!!.directions, null, null)
        val targets = planBatchTargets(metaTargets, case.views, includeOverlays = true)

        val androidRenders = tmp.resolve("renders/android/${case.name}")
        val layoutlibRenders = tmp.resolve("renders/layoutlib/${case.name}")
        val androidArtifacts =
          androidExecutor.renderBatchReport(androidEntry, targets, androidRenders, densityScale = 2)
        val layoutlibArtifacts =
          layoutlibExecutor.renderBatchReport(
            layoutlibEntry,
            targets,
            layoutlibRenders,
            densityScale = 2,
          )

        val adbBinary =
          listOfNotNull(
              System.getenv("ANDROID_HOME")?.let { Path.of(it, "platform-tools", "adb") },
              System.getenv("ANDROID_SDK_ROOT")?.let { Path.of(it, "platform-tools", "adb") },
            )
            .firstOrNull { Files.isExecutable(it) }
            ?.toString() ?: "adb"

        fun forceRestartSandbox(serial: String) {
          ProcessBuilder(
              adbBinary,
              "-s",
              serial,
              "shell",
              "am",
              "force-stop",
              "bot.charlies.androiddesign.preview.host",
            )
            .start()
            .waitFor(10, java.util.concurrent.TimeUnit.SECONDS)
          val refreshed =
            assertIs<DeviceSetupResult.Ready>(
              deviceManager.ensureDeviceReady(
                meta = meta,
                requestedSerial = serial,
                requestedAvd = avdName,
                headless = true,
              ),
            )
          currentPort.set(refreshed.forwardPort)
        }

        val maxExtraPasses = 6
        val cachedExtraPasses = mutableListOf<List<RenderArtifact>>()
        fun extraEmulatorPasses(minCount: Int): List<List<RenderArtifact>> {
          val wanted = minCount.coerceAtMost(maxExtraPasses)
          while (cachedExtraPasses.size < wanted) {
            val runIndex = cachedExtraPasses.size + 2
            forceRestartSandbox(ready.serial)
            val freshEntry = compileToEntry(androidExecutor, snapshot, "${case.name}#$runIndex")
            cachedExtraPasses +=
              androidExecutor.renderBatchReport(
                freshEntry,
                targets,
                tmp.resolve("renders/android$runIndex/${case.name}"),
                densityScale = 2,
              )
          }
          return cachedExtraPasses
        }

        val targetRecords = mutableListOf<TargetParityRecord>()
        var fixtureMaxBounds = 0f
        var fixtureMaxInk = 0f
        var fixtureMaxTouch = 0f
        var fixtureEndTrees = 0
        var fixtureMatched = 0
        var fixtureKnown = 0
        var fixtureUnexplained = 0

        for (i in targets.indices) {
          val spec = targets[i]
          val aArt = androidArtifacts[i]
          val bArt = layoutlibArtifacts[i]
          val label = targetLabel(spec)

          fun compareSingleTree(
            aTree: LayoutTree?,
            bTree: LayoutTree?,
            aCheck: ViewCheck,
            bCheck: ViewCheck,
            isEnd: Boolean,
          ) {
            val subLabel = targetLabel(spec, isEndTree = isEnd)
            val stats = ComparisonStats()
            var targetKnownCount = 0
            var targetUnexplainedCount = 0

            if (aTree == null || bTree == null) {
              targetUnexplainedCount++
              allUnexplained +=
                UnexplainedDifferenceRecord(
                  case.name,
                  subLabel,
                  "Missing tree (android=${aTree != null}, layoutlib=${bTree != null}, " +
                    "aErr=${aCheck.errors.map { it.pretty() }}, bErr=${bCheck.errors.map { it.pretty() }})",
                )
            } else {
              val pngBytesMissing =
                if (!isEnd) (aArt.pngBytes == null || bArt.pngBytes == null)
                else (aArt.endPngBytes == null || bArt.endPngBytes == null)
              if (pngBytesMissing) {
                targetUnexplainedCount++
                allUnexplained +=
                  UnexplainedDifferenceRecord(
                    case.name,
                    subLabel,
                    "Missing rendered frame bytes (android=${aArt.pngBytes != null}, layoutlib=${bArt.pngBytes != null})",
                  )
              }

              when {
                case.name == "overlays" && spec.overlayId == "sheet" -> {
                  val (summary, issues) = compareSheetOverlay(aTree, bTree, toleranceDp, stats)
                  targetKnownCount++
                  allKnown +=
                    KnownDifferenceRecord(
                      case.name,
                      subLabel,
                      "modal-bottom-sheet-nav-inset",
                      summary,
                    )
                  for (issue in issues) {
                    targetUnexplainedCount++
                    allUnexplained += UnexplainedDifferenceRecord(case.name, subLabel, issue)
                  }
                }
                case.name == "overlays" && spec.overlayId == "popup" -> {
                  val (summary, issues) = comparePopupOverlay(aTree, bTree, aCheck, bCheck)
                  targetKnownCount++
                  allKnown +=
                    KnownDifferenceRecord(
                      case.name,
                      subLabel,
                      "dropdown-menu-popup-positioning",
                      summary,
                    )
                  for (issue in issues) {
                    targetUnexplainedCount++
                    allUnexplained += UnexplainedDifferenceRecord(case.name, subLabel, issue)
                  }
                }
                else -> {
                  val (knownNotes, treeIssues) =
                    compareNodesStrict(aTree, bTree, toleranceDp, stats) { nodeIndex, baseVal ->
                      val emuValues = linkedSetOf(baseVal)
                      for (n in 1..maxExtraPasses) {
                        val pass = extraEmulatorPasses(n)[n - 1]
                        val art = pass[i]
                        val pTree = if (isEnd) art.endTree else art.tree
                        pTree?.nodes?.getOrNull(nodeIndex)?.text?.value?.let(emuValues::add)
                        if (emuValues.size > 1) break
                      }
                      emuValues
                    }
                  for (note in knownNotes) {
                    targetKnownCount++
                    allKnown +=
                      KnownDifferenceRecord(
                        case.name,
                        subLabel,
                        "extractor-text-ordering-nondeterminism",
                        note,
                      )
                  }
                  for (issue in treeIssues) {
                    targetUnexplainedCount++
                    allUnexplained += UnexplainedDifferenceRecord(case.name, subLabel, issue)
                  }
                }
              }

              // Compare findings for base/dialog/wear/calibration targets (popup has documented
              // overflow exemption).
              val exemptPopupFindings = case.name == "overlays" && spec.overlayId == "popup"
              val aErrs = normalizedErrors(aTree, aCheck)
              val bErrs = normalizedErrors(bTree, bCheck)
              val aNotes = normalizedNotes(aTree, aCheck)
              val bNotes = normalizedNotes(bTree, bCheck)
              val findingsMatched =
                exemptPopupFindings ||
                  (aCheck.pass == bCheck.pass && aErrs == bErrs && aNotes == bNotes)
              if (!findingsMatched && !isEnd) {
                val contrastRoundingSummary = classifyContrastBlurDiff(aTree, bTree, aCheck, bCheck)
                if (contrastRoundingSummary != null) {
                  targetKnownCount++
                  allKnown +=
                    KnownDifferenceRecord(
                      case.name,
                      subLabel,
                      "software-vs-gpu-blur-pixel-rounding",
                      contrastRoundingSummary,
                    )
                } else {
                  val semanticMatchesIgnoringIdHash =
                    aCheck.pass == bCheck.pass &&
                      semanticErrorsWithoutIdHash(aTree, aCheck) ==
                        semanticErrorsWithoutIdHash(bTree, bCheck) &&
                      semanticNotesWithoutIdHash(aTree, aCheck) ==
                        semanticNotesWithoutIdHash(bTree, bCheck)
                  var provenNondeterm = false
                  for (n in 1..maxExtraPasses) {
                    val pArt = extraEmulatorPasses(n)[n - 1][i]
                    val pTree = pArt.tree ?: continue
                    val emuFindingsVaried =
                      normalizedErrors(pTree, pArt.check) != aErrs ||
                        normalizedNotes(pTree, pArt.check) != aNotes ||
                        pArt.check.pass != aCheck.pass ||
                        pArt.check.errors.map { it.pretty() } != aCheck.errors.map { it.pretty() }
                    val emuNodeTextVaried =
                      semanticMatchesIgnoringIdHash &&
                        aTree.nodes.indices.any { idx ->
                          aTree.nodes[idx].text?.value != pTree.nodes.getOrNull(idx)?.text?.value
                        }
                    if (emuFindingsVaried || emuNodeTextVaried) {
                      provenNondeterm = true
                      break
                    }
                  }
                  if (provenNondeterm) {
                    targetKnownCount++
                    allKnown +=
                      KnownDifferenceRecord(
                        case.name,
                        subLabel,
                        "emulator-finding-nondeterminism",
                        "Emulator sandbox restarts varied in findings or derived node ID hashes while semantic finding (${aCheck.errors.map { it.pretty() }}) matched layoutlib (${bCheck.errors.map { it.pretty() }}).",
                      )
                  } else {
                    targetUnexplainedCount++
                    allUnexplained +=
                      UnexplainedDifferenceRecord(
                        case.name,
                        subLabel,
                        "Findings mismatch (pass ${aCheck.pass} vs ${bCheck.pass}): " +
                          "androidErr=${aCheck.errors.map { it.pretty() }} layoutlibErr=${bCheck.errors.map { it.pretty() }} " +
                          "androidNotes=${aCheck.notes.map { it.pretty() }} layoutlibNotes=${bCheck.notes.map { it.pretty() }}",
                      )
                  }
                }
              }
            }

            fixtureMaxBounds = max(fixtureMaxBounds, stats.maxBoundsDelta)
            fixtureMaxInk = max(fixtureMaxInk, stats.maxInkDelta)
            fixtureMaxTouch = max(fixtureMaxTouch, stats.maxTouchDelta)
            fixtureKnown += targetKnownCount
            fixtureUnexplained += targetUnexplainedCount
            if (targetUnexplainedCount == 0) fixtureMatched++

            val classification =
              when {
                targetUnexplainedCount > 0 -> "unexplained"
                targetKnownCount > 0 -> "known-difference"
                else -> "exact"
              }
            targetRecords +=
              TargetParityRecord(
                target = subLabel,
                endTree = isEnd,
                nodesAndroid = aTree?.nodes?.size ?: 0,
                nodesLayoutlib = bTree?.nodes?.size ?: 0,
                androidPass = aCheck.pass,
                layoutlibPass = bCheck.pass,
                maxBoundsDeltaDp = stats.maxBoundsDelta,
                maxInkDeltaDp = stats.maxInkDelta,
                maxTouchDeltaDp = stats.maxTouchDelta,
                findingsMatched = targetUnexplainedCount == 0,
                classification = classification,
              )
          }

          compareSingleTree(aArt.tree, bArt.tree, aArt.check, bArt.check, isEnd = false)
          if (aArt.endTree != null || bArt.endTree != null) {
            fixtureEndTrees++
            compareSingleTree(aArt.endTree, bArt.endTree, aArt.check, bArt.check, isEnd = true)
          }
        }

        fixtureSummaries +=
          FixtureParitySummary(
            fixture = case.name,
            targetsTotal = targetRecords.size,
            targetsMatched = fixtureMatched,
            endTreesCompared = fixtureEndTrees,
            maxBoundsDeltaDp = fixtureMaxBounds,
            maxInkDeltaDp = fixtureMaxInk,
            maxTouchDeltaDp = fixtureMaxTouch,
            knownDifferenceCount = fixtureKnown,
            unexplainedDifferenceCount = fixtureUnexplained,
            targets = targetRecords,
          )
      }

      val totalTargets = fixtureSummaries.sumOf { it.targetsTotal }
      val totalMatched = fixtureSummaries.sumOf { it.targetsMatched }
      val report =
        ParityReport(
          generatedAt = Instant.now().toString(),
          avd = avdName,
          toleranceDp = toleranceDp,
          totalTargets = totalTargets,
          matchedTargets = totalMatched,
          knownDifferenceCount = allKnown.size,
          unexplainedDifferenceCount = allUnexplained.size,
          fixtures = fixtureSummaries,
          knownDifferences = allKnown,
          unexplainedDifferences = allUnexplained,
        )

      val outFile = root.resolve("evals/results/v7/parity.json")
      Files.createDirectories(outFile.parent)
      Files.writeString(outFile, "${prettyJson.encodeToString(report)}\n")

      assertTrue(
        allUnexplained.isEmpty(),
        "Expected 0 unexplained parity differences (see $outFile): $allUnexplained",
      )
    } finally {
      runCatching { layoutlibExecutor.close() }
      runCatching { androidExecutor.close() }
      runCatching { transport.close() }
      deviceManager.releaseLaunchedEmulator()
    }
  }
}
