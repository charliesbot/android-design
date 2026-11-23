package bot.charlies.androiddesign.preview.host

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.zip.CRC32
import java.util.zip.Deflater

/**
 * Shared fixture `.explore.kt` sources and test asset generator for host/Robolectric/emulator
 * tests.
 */
internal object HostTestFixtures {
  const val PHONE_EXPLORE_FILE = "PhoneFixture.explore.kt"
  const val WEAR_EXPLORE_FILE = "WearFixture.explore.kt"

  val PHONE_EXPLORE_SOURCE: String =
    """
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.Row
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
    import androidx.compose.ui.Alignment
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

  val WEAR_EXPLORE_SOURCE: String =
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

  /**
   * Writes a valid solid-purple PNG asset under `dir/assets/cover.png` and returns `(bytes,
   * sha256)`.
   */
  fun writeSampleCoverPng(dir: Path): Pair<ByteArray, String> {
    val bytes = createSolidPng(16, 16, r = 0x5B, g = 0x4C, b = 0xEE, a = 0xFF)
    val asset = dir.resolve("assets/cover.png")
    Files.createDirectories(asset.parent)
    Files.write(asset, bytes)
    val sha256 =
      MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    return bytes to sha256
  }

  private fun createSolidPng(width: Int, height: Int, r: Int, g: Int, b: Int, a: Int): ByteArray {
    val raw = ByteArray((1 + width * 4) * height)
    var pos = 0
    for (y in 0 until height) {
      raw[pos++] = 0
      for (x in 0 until width) {
        raw[pos++] = r.toByte()
        raw[pos++] = g.toByte()
        raw[pos++] = b.toByte()
        raw[pos++] = a.toByte()
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
    val ihdr =
      ByteBuffer.allocate(13)
        .putInt(width)
        .putInt(height)
        .put(8)
        .put(6)
        .put(0)
        .put(0)
        .put(0)
        .array()
    chunk("IHDR", ihdr)
    chunk("IDAT", deflated)
    chunk("IEND", ByteArray(0))
    return out.toByteArray()
  }
}
