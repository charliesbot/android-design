import { execFile } from "node:child_process";
import assert from "node:assert/strict";
import fs from "node:fs";
import http from "node:http";
import os from "node:os";
import path from "node:path";
import test from "node:test";
import { fileURLToPath } from "node:url";
import { promisify } from "node:util";
import { BoardServer } from "../src/board-server.js";
import { PreviewMcpServer } from "../src/mcp-server.js";
import { WorkerSupervisor } from "../src/worker-supervisor.js";

const execFileAsync = promisify(execFile);

const TEST_DIR = path.dirname(fileURLToPath(import.meta.url));
const REPO_ROOT = path.resolve(TEST_DIR, "..", "..");
const BUILT_JAR = path.join(REPO_ROOT, "app", "app", "build", "libs", "android-design-preview.jar");
const JAR_EXISTS = fs.existsSync(BUILT_JAR);

const SAMPLE_EXPLORE_KT = `
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.kit.*

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SampleHomeScreen() {
  Scaffold { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = "Daily harvest",
        style = MaterialTheme.typography.headlineMedium,
        maxLines = 2,
      )
      Button(
        onClick = {},
        modifier = Modifier.testTag("ad:primary"),
      ) {
        Text("Cook now")
      }
    }
  }
}

val preview = designBoard(
  name = "Harvest",
  seed = 0xFFB5523B,
  variant = PaletteVariant.TonalSpot,
  brandFont = BundledFont.Fraunces,
) {
  direction("Editorial") {
    screen("home") {
      SampleHomeScreen()
    }
  }
}
`.trimStart();

function postJson({ port, token, body }) {
  return new Promise((resolve, reject) => {
    const req = http.request(
      {
        host: "127.0.0.1",
        port,
        method: "POST",
        path: "/api/render",
        headers: {
          Host: `127.0.0.1:${port}`,
          Origin: `http://127.0.0.1:${port}`,
          "X-Session-Token": token,
          "Content-Type": "application/json",
        },
      },
      (res) => {
        const chunks = [];
        res.on("data", (c) => chunks.push(c));
        res.on("end", () => {
          resolve({ status: res.statusCode, body: JSON.parse(Buffer.concat(chunks).toString("utf8")) });
        });
      },
    );
    req.on("error", reject);
    req.write(JSON.stringify(body));
    req.end();
  });
}

test(
  "real JVM worker executes check_design, get_tree, and BoardServer POST /api/render with sourceLine alignment",
  { skip: !JAR_EXISTS ? "Built worker JAR not found at app/app/build/libs/android-design-preview.jar" : false },
  async () => {
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "ad-real-jar-"));
    const explorePath = path.join(tmpDir, "Sample.explore.kt");
    fs.writeFileSync(explorePath, SAMPLE_EXPLORE_KT, "utf8");

    const supervisor = new WorkerSupervisor({
      env: { ...process.env, ANDROID_DESIGN_PREVIEW_JAR: BUILT_JAR },
    });

    const mcp = new PreviewMcpServer({
      supervisor,
      enableBoard: false,
      openBrowser: false,
    });
    const board = new BoardServer({ supervisor });

    try {
      const checkResp = await mcp.handleMessage({
        jsonrpc: "2.0",
        id: 1,
        method: "tools/call",
        params: { name: "check_design", arguments: { path: explorePath } },
      });
      assert.equal(checkResp.result.isError, false);
      assert.ok(checkResp.result.content.length >= 3);

      const summary = JSON.parse(checkResp.result.content[0].text);
      assert.equal(summary.valid, true);
      assert.equal(summary.pass, true);
      assert.equal(checkResp.result.content[1].type, "image");
      const pngBytes = Buffer.from(checkResp.result.content[1].data, "base64");
      assert.equal(pngBytes.subarray(0, 4).toString("latin1"), "\x89PNG");

      const treeResp = await mcp.handleMessage({
        jsonrpc: "2.0",
        id: 2,
        method: "tools/call",
        params: { name: "get_tree", arguments: { direction: "Editorial", screen: "home", view: "light" } },
      });
      const treePayload = JSON.parse(treeResp.result.content[0].text);
      const sourcedNodes = treePayload.tree.nodes.filter((n) => n.sourceFile === "Sample.explore.kt" && n.sourceLine > 0);
      assert.ok(sourcedNodes.length > 0, "Expected SlotTreeExtractor to populate Sample.explore.kt line numbers");
      assert.ok(Array.isArray(sourcedNodes[0].bounds) && sourcedNodes[0].bounds.length === 4);

      await board.start();
      const renderRes = await postJson({
        port: board.port,
        token: board.token,
        body: { direction: "Editorial", screen: "home", view: "light", dark: true },
      });
      assert.equal(renderRes.status, 200);
      assert.ok(renderRes.body.pngBase64.length > 100);
      const laidOutRealNodes = renderRes.body.tree.nodes.filter(
        (n) => n.sourceFile === "Sample.explore.kt" && n.sourceLine > 0 && Array.isArray(n.bounds) && n.bounds[2] > 0 && n.bounds[3] > 0,
      );
      assert.ok(laidOutRealNodes.length > 0, "Expected laid-out 4-element [x, y, w, h] bounds arrays on real JAR nodes");
      assert.ok(
        renderRes.body.check.notes === undefined || Array.isArray(renderRes.body.check.notes),
      );

      // Verify CLI `render <file> --out <dir>` writes report.json matching RenderEntry.
      const cliOutDir = path.join(tmpDir, "cli-out");
      const binPath = path.resolve(TEST_DIR, "..", "bin", "android-design-preview.js");
      await execFileAsync(process.execPath, [binPath, "render", explorePath, "--out", cliOutDir], {
        env: { ...process.env, ANDROID_DESIGN_PREVIEW_JAR: BUILT_JAR },
      });
      const reportPath = path.join(cliOutDir, "report.json");
      assert.equal(fs.existsSync(reportPath), true);
      const cliReport = JSON.parse(fs.readFileSync(reportPath, "utf8"));
      assert.equal(cliReport.valid, true);
      assert.equal(cliReport.pass, true);
      assert.ok(cliReport.renders[0].png.endsWith(".png"));
      assert.ok(cliReport.renders[0].tree.endsWith(".tree.json"));

      // Verify `--directions` path emits the exact same RenderEntry schema (`png`, `tree`, `pass`, `failures`).
      const dirOutDir = path.join(tmpDir, "cli-dir-out");
      await execFileAsync(
        process.execPath,
        [binPath, "render", explorePath, "--directions", "Editorial", "--views", "light", "--out", dirOutDir],
        { env: { ...process.env, ANDROID_DESIGN_PREVIEW_JAR: BUILT_JAR } },
      );
      const dirReport = JSON.parse(fs.readFileSync(path.join(dirOutDir, "report.json"), "utf8"));
      assert.equal(dirReport.valid, true);
      assert.equal(dirReport.pass, true);
      assert.equal(dirReport.renders.length, 1);
      assert.equal(dirReport.renders[0].png, "home-light.png");
      assert.equal(dirReport.renders[0].tree, "home-light.tree.json");
      assert.equal(fs.existsSync(path.join(dirOutDir, dirReport.renders[0].png)), true);
      assert.equal(fs.existsSync(path.join(dirOutDir, dirReport.renders[0].tree)), true);
    } finally {
      await board.close();
      await mcp.close();
      fs.rmSync(tmpDir, { recursive: true, force: true });
    }
  },
);
