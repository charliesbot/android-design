package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PreviewJson
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Integration tests for `.explore.kt` CLI `render` and stdio JSON-RPC `worker` flows. */
class WorkerCliIntegrationTest {
  private fun validMusicExplore(): String =
    """
    import androidx.compose.foundation.layout.*
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.testTag
    import androidx.compose.ui.unit.dp
    import bot.charlies.androiddesign.preview.kit.*

    val preview = designBoard("Playlist", seed = 0xFFB5523B) {
      screen("home") {
        Scaffold(
          topBar = { TopAppBar(title = { Text("Late Night Mix") }) },
        ) { innerPadding ->
          Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Card(modifier = Modifier.padding(16.dp)) {
              Text(
                "32 curated ambient tracks",
                modifier = Modifier.padding(16.dp),
              )
            }
            Button(
              onClick = {},
              modifier = Modifier.testTag(AD_PRIMARY_TAG).height(56.dp),
            ) {
              Text("Play all")
            }
          }
        }
      }
    }
    """
      .trimIndent()

  @Test
  fun `cli render draws all 5 views and report json for valid explore kt`() {
    val dir: Path = createTempDirectory("cli-explore-ok-")
    try {
      val srcFile = dir.resolve("music.explore.kt")
      Files.writeString(srcFile, validMusicExplore())
      val outDir = dir.resolve("out")
      val outBuf = ByteArrayOutputStream()
      val errBuf = ByteArrayOutputStream()

      val code =
        Cli.run(
          listOf("render", srcFile.toString(), "--out", outDir.toString()),
          PrintStream(outBuf),
          PrintStream(errBuf),
        )
      assertEquals(ExitCode.OK, code, "err=${errBuf.toString(Charsets.UTF_8)}")

      val reportFile = outDir.resolve("report.json")
      assertTrue(Files.isRegularFile(reportFile))
      val report =
        PreviewJson.pretty.decodeFromString(RenderReport.serializer(), Files.readString(reportFile))
      assertTrue(report.valid)
      assertTrue(report.pass, "Expected passing report, got $report")
      assertEquals(5, report.renders.size)
      for (entry in report.renders) {
        assertTrue(Files.isRegularFile(outDir.resolve(entry.png!!)))
        assertTrue(Files.isRegularFile(outDir.resolve(entry.tree!!)))
      }
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  @Test
  fun `cli render returns FINDINGS_FAILED when error finding present and INVALID on syntax error`() {
    val dir: Path = createTempDirectory("cli-explore-err-")
    try {
      val badHierarchy =
        """
        import androidx.compose.foundation.layout.*
        import androidx.compose.material3.*
        import androidx.compose.ui.Modifier
        import androidx.compose.ui.platform.testTag
        import androidx.compose.ui.unit.dp
        import bot.charlies.androiddesign.preview.kit.*

        val preview = designBoard("Bad") {
          screen("home") {
            Column {
              Button(onClick = {}, modifier = Modifier.testTag(AD_PRIMARY_TAG).height(48.dp)) {
                Text("Small Primary")
              }
              Button(onClick = {}, modifier = Modifier.height(72.dp).fillMaxWidth()) {
                Text("Giant Secondary")
              }
            }
          }
        }
        """
          .trimIndent()
      val failFile = dir.resolve("fail.explore.kt").also { Files.writeString(it, badHierarchy) }
      val failCode =
        Cli.run(
          listOf(
            "render",
            failFile.toString(),
            "--out",
            dir.resolve("fail-out").toString(),
            "--views",
            "light",
          ),
          PrintStream(ByteArrayOutputStream()),
          PrintStream(ByteArrayOutputStream()),
        )
      assertEquals(ExitCode.FINDINGS_FAILED, failCode)

      val brokenSyntax =
        """
        import androidx.compose.material3.Text
        import bot.charlies.androiddesign.preview.kit.*
        val preview = designBoard("Broken") {
          screen { UndefinedFunction() }
        }
        """
          .trimIndent()
      val brokenFile = dir.resolve("broken.explore.kt").also { Files.writeString(it, brokenSyntax) }
      val invalidOut = dir.resolve("invalid-out")
      val invalidCode =
        Cli.run(
          listOf("render", brokenFile.toString(), "--out", invalidOut.toString()),
          PrintStream(ByteArrayOutputStream()),
          PrintStream(ByteArrayOutputStream()),
        )
      assertEquals(ExitCode.INVALID, invalidCode)
      val invalidReport =
        PreviewJson.pretty.decodeFromString(
          RenderReport.serializer(),
          Files.readString(invalidOut.resolve("report.json")),
        )
      assertFalse(invalidReport.valid)
      assertTrue(invalidReport.errors.any { it.path.contains("broken.explore.kt:") })
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  @Test
  fun `worker stdio json-rpc runs ping, compileAndCheck, warm renderView, getTree, and shutdown`() {
    val dir: Path = createTempDirectory("worker-rpc-test-")
    try {
      val srcFile = dir.resolve("playlist.explore.kt")
      Files.writeString(srcFile, validMusicExplore())

      val requests =
        listOf(
          buildJsonObject {
            put("id", JsonPrimitive(1))
            put("method", JsonPrimitive("ping"))
          },
          buildJsonObject {
            put("id", JsonPrimitive(2))
            put("method", JsonPrimitive("compileAndCheck"))
            put(
              "params",
              buildJsonObject {
                put("path", JsonPrimitive(srcFile.toString()))
                put("includeImages", JsonPrimitive(true))
              },
            )
          },
          buildJsonObject {
            put("id", JsonPrimitive(3))
            put("method", JsonPrimitive("renderView"))
            put(
              "params",
              buildJsonObject {
                put("screen", JsonPrimitive("home"))
                put("view", JsonPrimitive("light"))
                put("seed", JsonPrimitive("#1B6B52"))
                put("dark", JsonPrimitive(true))
              },
            )
          },
          buildJsonObject {
            put("id", JsonPrimitive(4))
            put("method", JsonPrimitive("getTree"))
            put(
              "params",
              buildJsonObject {
                put("screen", JsonPrimitive("home"))
                put("view", JsonPrimitive("light"))
              },
            )
          },
          buildJsonObject {
            put("id", JsonPrimitive(5))
            put("method", JsonPrimitive("shutdown"))
          },
        )
      val stdinText =
        requests.joinToString("\n") {
          PreviewJson.compact.encodeToString(JsonObject.serializer(), it)
        } + "\n"

      val outBuf = ByteArrayOutputStream()
      val errBuf = ByteArrayOutputStream()
      val worker =
        WorkerCommand(
          input = ByteArrayInputStream(stdinText.toByteArray(Charsets.UTF_8)),
          redirectSystemOut = false,
        )
      val code = worker.run(emptyList(), PrintStream(outBuf), PrintStream(errBuf))
      assertEquals(ExitCode.OK, code, "err=${errBuf.toString(Charsets.UTF_8)}")

      val lines = outBuf.toString(Charsets.UTF_8).trim().lines().filter { it.isNotBlank() }
      assertEquals(5, lines.size, "Expected 5 JSON-RPC lines, got: $lines")

      val resp1 = PreviewJson.compact.parseToJsonElement(lines[0]).jsonObject
      assertNull(resp1["error"])
      assertEquals("ok", resp1["result"]!!.jsonObject["status"]!!.jsonPrimitive.content)
      assertEquals("0", resp1["result"]!!.jsonObject["compileCount"]!!.jsonPrimitive.content)

      val resp2 = PreviewJson.compact.parseToJsonElement(lines[1]).jsonObject
      assertNull(resp2["error"])
      val checkRes = resp2["result"]!!.jsonObject
      assertEquals("true", checkRes["valid"]!!.jsonPrimitive.content)
      assertEquals("true", checkRes["pass"]!!.jsonPrimitive.content)
      assertEquals("1", checkRes["compileCount"]!!.jsonPrimitive.content)
      val renders = checkRes["renders"]!!.jsonArray
      assertEquals(5, renders.size)
      val lightRender =
        renders.first { it.jsonObject["view"]!!.jsonPrimitive.content == "light" }.jsonObject
      assertNotNull(lightRender["pngBase64"]?.jsonPrimitive?.content)

      val resp3 = PreviewJson.compact.parseToJsonElement(lines[2]).jsonObject
      assertNull(resp3["error"])
      val warmRes = resp3["result"]!!.jsonObject
      assertEquals("1", warmRes["compileCount"]!!.jsonPrimitive.content)
      assertTrue(warmRes["pngBase64"]!!.jsonPrimitive.content.isNotBlank())

      val resp4 = PreviewJson.compact.parseToJsonElement(lines[3]).jsonObject
      assertNull(resp4["error"])
      val tree =
        PreviewJson.compact.decodeFromJsonElement<LayoutTree>(
          resp4["result"]!!.jsonObject["tree"]!!,
        )
      assertTrue(tree.nodes.any { it.sourceFile == "playlist.explore.kt" && it.sourceLine != null })

      val resp5 = PreviewJson.compact.parseToJsonElement(lines[4]).jsonObject
      assertEquals("bye", resp5["result"]!!.jsonObject["status"]!!.jsonPrimitive.content)

      // Overlay filtered check + getTree validation: base has small-primary error, overlay is
      // clean.
      val overlaySrc =
        """
        import androidx.compose.foundation.layout.*
        import androidx.compose.material3.*
        import androidx.compose.ui.Modifier
        import androidx.compose.ui.platform.testTag
        import androidx.compose.ui.unit.dp
        import bot.charlies.androiddesign.preview.kit.*

        val preview = designBoard("Overlay Filter") {
          direction("Main") {
            screen("home") {
              Column {
                Button(onClick = {}, modifier = Modifier.testTag(AD_PRIMARY_TAG).height(48.dp)) {
                  Text("Small Primary")
                }
                Button(onClick = {}, modifier = Modifier.height(72.dp).fillMaxWidth()) {
                  Text("Bigger Rival")
                }
              }
            }
            overlay("info", screen = "home") {
              AlertDialog(
                onDismissRequest = {},
                confirmButton = { TextButton(onClick = {}) { Text("OK") } },
                text = { Text("Clean overlay") },
              )
            }
          }
        }
        """
          .trimIndent()
      val overlayFile = dir.resolve("ov.explore.kt").also { Files.writeString(it, overlaySrc) }
      val ovReqs =
        listOf(
          buildJsonObject {
            put("id", JsonPrimitive(10))
            put("method", JsonPrimitive("compileAndCheck"))
            put(
              "params",
              buildJsonObject {
                put("path", JsonPrimitive(overlayFile.toString()))
                put(
                  "views",
                  kotlinx.serialization.json.buildJsonArray { add(JsonPrimitive("light")) },
                )
              },
            )
          },
          buildJsonObject {
            put("id", JsonPrimitive(11))
            put("method", JsonPrimitive("getTree"))
            put(
              "params",
              buildJsonObject {
                put("direction", JsonPrimitive("Main"))
                put("screen", JsonPrimitive("home"))
                put("overlay", JsonPrimitive(""))
                put("view", JsonPrimitive("light"))
              },
            )
          },
          buildJsonObject {
            put("id", JsonPrimitive(12))
            put("method", JsonPrimitive("getTree"))
            put(
              "params",
              buildJsonObject { put("direction", JsonPrimitive("MissingDir")) },
            )
          },
          buildJsonObject {
            put("id", JsonPrimitive(13))
            put("method", JsonPrimitive("shutdown"))
          },
        )
      val ovIn =
        ovReqs.joinToString("\n") {
          PreviewJson.compact.encodeToString(JsonObject.serializer(), it)
        } + "\n"
      val ovOut = ByteArrayOutputStream()
      val ovErr = ByteArrayOutputStream()
      val ovCode =
        WorkerCommand(
            input = ByteArrayInputStream(ovIn.toByteArray(Charsets.UTF_8)),
            redirectSystemOut = false,
          )
          .run(emptyList(), PrintStream(ovOut), PrintStream(ovErr))
      assertEquals(ExitCode.OK, ovCode, "err=${ovErr.toString(Charsets.UTF_8)}")
      val ovLines = ovOut.toString(Charsets.UTF_8).trim().lines().filter { it.isNotBlank() }
      assertEquals(4, ovLines.size)
      val ovRenders =
        PreviewJson.compact
          .parseToJsonElement(ovLines[0])
          .jsonObject["result"]!!
          .jsonObject["renders"]!!
          .jsonArray
      val baseItem = ovRenders.first { it.jsonObject["overlay"] == null }.jsonObject
      val ovItem = ovRenders.first { it.jsonObject["overlay"] != null }.jsonObject
      assertEquals("false", baseItem["pass"]!!.jsonPrimitive.content)
      assertEquals(
        "true",
        ovItem["pass"]!!.jsonPrimitive.content,
        "ovItem=$ovItem baseItem=$baseItem",
      )
      assertEquals("true", ovItem["check"]!!.jsonObject["pass"]!!.jsonPrimitive.content)
      assertNull(ovItem["check"]!!.jsonObject["errors"])
      assertNull(ovItem["layout"])

      val cachedGet = PreviewJson.compact.parseToJsonElement(ovLines[1]).jsonObject
      assertNull(cachedGet["error"], "Blank overlay should resolve base tree cleanly: $cachedGet")
      val missingDir = PreviewJson.compact.parseToJsonElement(ovLines[2]).jsonObject
      assertEquals(
        "unknown_direction",
        missingDir["error"]!!.jsonObject["code"]!!.jsonPrimitive.content,
      )
    } finally {
      dir.toFile().deleteRecursively()
    }
  }
}
