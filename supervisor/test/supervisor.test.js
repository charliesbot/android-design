import assert from "node:assert/strict";
import crypto from "node:crypto";
import fs from "node:fs";
import http from "node:http";
import os from "node:os";
import path from "node:path";
import { PassThrough } from "node:stream";
import test from "node:test";
import { JarCache, parseSha256Text } from "../../plugin/scripts/jar-cache.js";
import { collectJavaCandidates, locateJava, parseJavaMajorVersion } from "../../plugin/scripts/java-locator.js";
import { PreviewMcpServer, formatCheckSummary } from "../../plugin/scripts/mcp-server.js";
import { WorkerSupervisor, WorkerTimeoutError } from "../../plugin/scripts/worker-supervisor.js";

function withTempDir(fn) {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), "ad-sup-test-"));
  return Promise.resolve()
    .then(() => fn(dir))
    .finally(() => {
      fs.rmSync(dir, { recursive: true, force: true });
    });
}

function writeFakeWorkerScript(dir) {
  const scriptPath = path.join(dir, "fake-worker.mjs");
  const code = `
import readline from "node:readline";
let compileCount = 0;
let hung = false;
const rl = readline.createInterface({ input: process.stdin, crlfDelay: Infinity });
rl.on("line", async (line) => {
  if (!line.trim()) return;
  const req = JSON.parse(line);
  if (req.method === "ping") {
    process.stdout.write(JSON.stringify({ id: req.id, result: { status: "ok", compileCount } }) + "\\n");
    return;
  }
  if (req.method === "shutdown") {
    process.stdout.write(JSON.stringify({ id: req.id, result: { status: "bye" } }) + "\\n");
    process.exit(0);
  }
  if (req.method === "compileAndCheck") {
    const content = fsRead(req.params.path);
    if (content.includes("// HANG_WARM")) {
      // Simulate infinite composition loop without replying.
      hung = true;
      setInterval(() => {}, 1000);
      return;
    }
    const delayMs = content.includes("// SLOW_COLD") && compileCount === 0 ? 120 : 5;
    await new Promise((r) => setTimeout(r, delayMs));
    compileCount += 1;
    const pass = !content.includes("// FAIL_CHECK");
    const sampleViewCheck = {
      pass,
      errors: pass ? [] : [{ rule: "touch-target", ids: ["btn"], detail: "too small" }],
      notes: [],
      colors: { surface: "#FFFFFF" },
    };
    const sampleLayout = { navigation: "none", screens: [{ id: "home", children: ["Button b1 88"] }] };
    const result = {
      valid: true,
      pass,
      compileMs: 42,
      renderMs: 12,
      compileCount,
      surface: "phone",
      boardName: "Fake Board " + compileCount,
      sourcePath: req.params.path,
      designPath: null,
      theme: { seed: "#6750A4", variant: "tonal-spot", contrast: 0 },
      errors: [],
      boardMeta: {
        directions: [{ name: "main", surface: "phone", startScreen: "home", screens: ["home"], overlays: [] }],
        views: [
          { name: "light", width: 412, height: 915, round: false, dark: false, density: 2 },
          { name: "foldable", width: 673, height: 841, round: false, dark: false, density: 2 },
          { name: "desktop", width: 1280, height: 800, round: false, dark: false, density: 2 },
        ],
      },
      renders: [
        {
          direction: "main",
          screen: "home",
          view: "light",
          artifactName: "home",
          pass,
          renderMs: 4,
          check: sampleViewCheck,
          layout: sampleLayout,
          failures: pass ? [] : ["touchTargets"],
          pngBase64: pass ? Buffer.from("PNG_LIGHT").toString("base64") : null,
        },
        {
          direction: "main",
          screen: "home",
          view: "foldable",
          artifactName: "home",
          pass,
          renderMs: 4,
          check: sampleViewCheck,
          layout: sampleLayout,
          failures: pass ? [] : ["touchTargets"],
          pngBase64: null,
        },
        {
          direction: "main",
          screen: "home",
          view: "desktop",
          artifactName: "home",
          pass,
          renderMs: 4,
          check: { ...sampleViewCheck, colors: { surface: "#FAFAFA" } },
          layout: sampleLayout,
          failures: pass ? [] : ["touchTargets"],
          pngBase64: pass ? Buffer.from("PNG_DESKTOP").toString("base64") : null,
        },
      ],
    };
    process.stdout.write(JSON.stringify({ id: req.id, result }) + "\\n");
    return;
  }
  if (req.method === "renderView") {
    if (hung) return;
    process.stdout.write(JSON.stringify({
      id: req.id,
      result: {
        direction: req.params.direction || "main",
        screen: req.params.screen || "home",
        overlay: req.params.overlay || null,
        view: req.params.view || "light",
        pass: true,
        renderMs: 8,
        compileCount,
        check: { pass: true, errors: [], notes: [], colors: {} },
        tree: {
          screen: "home",
          view: req.params.view || "light",
          nodes: [
            {
              id: "btn",
              type: "Button",
              primary: true,
              scrolls: false,
              bounds: [24, 120, 140, 48],
              roles: { container: "primary", content: "onPrimary" },
              scrollsHorizontally: false,
              sourceFile: "Test.explore.kt",
              sourceLine: 18,
            },
          ],
          findings: [],
        },
        pngBase64: Buffer.from("PNG_RENDER_" + process.pid).toString("base64"),
      },
    }) + "\\n");
    return;
  }
  if (req.method === "getTree") {
    process.stdout.write(JSON.stringify({
      id: req.id,
      result: {
        direction: req.params.direction || "main",
        screen: req.params.screen || "home",
        view: req.params.view || "light",
        tree: {
          screen: "home",
          view: req.params.view || "light",
          nodes: [{ id: "root", type: "Scaffold", bounds: [0, 0, 412, 915], primary: false, scrolls: false, scrollsHorizontally: false, sourceFile: "Test.explore.kt", sourceLine: 12 }],
          findings: [],
        },
      },
    }) + "\\n");
  }
});
import fs from "node:fs";
function fsRead(p) { return fs.readFileSync(p, "utf8"); }
`;
  fs.writeFileSync(scriptPath, code, "utf8");
  return scriptPath;
}

test("java-locator parses legacy and modern versions and rejects Java < 17", async () => {
  assert.equal(parseJavaMajorVersion('openjdk version "17.0.20" 2026-07-21'), 17);
  assert.equal(parseJavaMajorVersion('java version "1.8.0_392"'), 8);
  assert.equal(parseJavaMajorVersion('openjdk version "21.0.4"'), 21);

  const candidates = collectJavaCandidates({
    platform: "darwin",
    env: { JAVA_HOME: "/fake/jdk21", PATH: "/usr/bin:/bin" },
    homedir: "/Users/tester",
  });
  assert.ok(candidates.includes("/fake/jdk21/bin/java"));
  assert.ok(candidates.includes("/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java"));

  const found = await locateJava({
    platform: "linux",
    env: { JAVA_HOME: "/jdk8", ANDROID_DESIGN_PREVIEW_JAVA: "/jdk17/bin/java" },
    existsSync: (p) => p === "/jdk8/bin/java" || p === "/jdk17/bin/java",
    probeVersion: async (p) =>
      p === "/jdk8/bin/java" ? 'java version "1.8.0_392"' : 'openjdk version "17.0.20"',
  });
  assert.equal(found.bin, "/jdk17/bin/java");
  assert.equal(found.major, 17);

  await assert.rejects(
    locateJava({
      platform: "linux",
      env: { JAVA_HOME: "/jdk8" },
      existsSync: (p) => p === "/jdk8/bin/java",
      probeVersion: async () => 'java version "1.8.0_392"',
    }),
    /Java 17\+ is required/,
  );
});

test("jar-cache parses sha256, follows redirects, atomically writes, and rejects mismatches", async () => {
  assert.equal(
    parseSha256Text(`${"a".repeat(64)}  android-design-preview.jar\n`),
    "a".repeat(64),
  );

  await withTempDir(async (dir) => {
    const payload = Buffer.from("PK\x03\x04fake-jar-bytes-v4");
    const goodSha = crypto.createHash("sha256").update(payload).digest("hex");
    let mode = "good";

    const server = http.createServer((req, res) => {
      if (req.url === "/redirect.jar") {
        res.writeHead(302, { Location: "/actual.jar" });
        res.end();
        return;
      }
      if (req.url === "/actual.jar") {
        res.writeHead(200, { "Content-Length": String(payload.length) });
        res.end(payload);
        return;
      }
      if (req.url === "/actual.jar.sha256") {
        const digest = mode === "good" ? goodSha : "f".repeat(64);
        res.writeHead(200);
        res.end(`${digest}  android-design-preview.jar\n`);
        return;
      }
      res.writeHead(404);
      res.end();
    });

    await new Promise((resolve) => server.listen(0, "127.0.0.1", resolve));
    const port = server.address().port;

    try {
      // 1. Mismatch must fail and leave no JAR or lingering tmp files.
      mode = "bad";
      const badCache = new JarCache({
        version: "0.4.0-bad",
        cacheRoot: dir,
        jarUrl: `http://127.0.0.1:${port}/redirect.jar`,
        shaUrl: `http://127.0.0.1:${port}/actual.jar.sha256`,
      });
      await assert.rejects(badCache.ensureJar(), /SHA-256 checksum mismatch/);
      assert.equal(fs.existsSync(badCache.cachedJarPath()), false);
      const badDirFiles = fs.existsSync(path.dirname(badCache.cachedJarPath()))
        ? fs.readdirSync(path.dirname(badCache.cachedJarPath()))
        : [];
      assert.deepEqual(badDirFiles, []);

      // 2. Valid background warmup must cache atomically.
      mode = "good";
      const goodCache = new JarCache({
        version: "0.4.0-good",
        cacheRoot: dir,
        jarUrl: `http://127.0.0.1:${port}/redirect.jar`,
        shaUrl: `http://127.0.0.1:${port}/actual.jar.sha256`,
      });
      assert.equal(goodCache.warmUp(), true);
      const cached = await goodCache.ensureJar();
      assert.equal(fs.readFileSync(cached).equals(payload), true);
      assert.equal(goodCache.warmUp(), false);
    } finally {
      await new Promise((resolve) => server.close(resolve));
    }
  });
});

test("MCP initialize + tools/list spawns zero worker processes", async () => {
  await withTempDir(async (dir) => {
    const workerScript = writeFakeWorkerScript(dir);
    let spawns = 0;
    const supervisor = new WorkerSupervisor({
      workerCommandFactory: () => {
        spawns += 1;
        return { command: process.execPath, args: [workerScript] };
      },
    });
    const server = new PreviewMcpServer({
      supervisor,
      enableBoard: false,
      openBrowser: false,
    });

    const input = new PassThrough();
    const output = new PassThrough();
    const lines = [];
    output.on("data", (chunk) => {
      for (const line of chunk.toString("utf8").split("\n")) {
        if (line.trim()) lines.push(JSON.parse(line));
      }
    });

    const loop = server.serveStdio({ input, output, warmCache: false });
    input.write(JSON.stringify({ jsonrpc: "2.0", id: 1, method: "initialize", params: { protocolVersion: "2025-03-26" } }) + "\n");
    input.write(JSON.stringify({ jsonrpc: "2.0", method: "notifications/initialized" }) + "\n");
    input.write(JSON.stringify({ jsonrpc: "2.0", id: 2, method: "tools/list" }) + "\n");
    input.end();
    await loop;

    assert.equal(spawns, 0);
    assert.equal(supervisor.activePid, null);
    assert.equal(lines.length, 2);
    assert.equal(lines[0].result.serverInfo.name, "android-design-preview");
    assert.deepEqual(
      lines[1].result.tools.map((t) => t.name),
      ["check_design", "get_tree"],
    );
  });
});

test("supervisor enforces split boot vs warm SIGKILL deadlines, recovery, rotation, and idle shutdown", async () => {
  await withTempDir(async (dir) => {
    const workerScript = writeFakeWorkerScript(dir);
    const exploreFile = path.join(dir, "Demo.explore.kt");

    const supervisor = new WorkerSupervisor({
      workerCommandFactory: () => ({ command: process.execPath, args: [workerScript] }),
      bootTimeoutMs: 500,
      warmTimeoutMs: 60,
      maxCompilesBeforeRecycle: 3,
      idleShutdownMs: 80,
    });

    try {
      // Round 1 slow cold JIT (120 ms) > warmTimeout (60 ms) must still succeed under bootTimeout (500 ms).
      fs.writeFileSync(exploreFile, "// SLOW_COLD\n");
      const r1 = await supervisor.compileAndCheck({ path: exploreFile });
      const firstPid = supervisor.activePid;
      assert.equal(r1.compileCount, 1);
      assert.ok(firstPid > 0);

      // Round 2 hung worker must get SIGKILLed after warmTimeoutMs (60 ms).
      fs.writeFileSync(exploreFile, "// HANG_WARM\n");
      await assert.rejects(
        supervisor.compileAndCheck({ path: exploreFile }),
        (err) => err instanceof WorkerTimeoutError && err.pid === firstPid && err.phase === "warm",
      );
      assert.equal(supervisor.stats().sigkills, 1);
      assert.equal(supervisor.activePid, null);

      // Clean respawn succeeds immediately after SIGKILL.
      fs.writeFileSync(exploreFile, "// OK\n");
      const r2 = await supervisor.compileAndCheck({ path: exploreFile });
      const secondPid = supervisor.activePid;
      assert.ok(secondPid > 0 && secondPid !== firstPid);
      assert.equal(r2.compileCount, 1);

      // Drive compiles 2 and 3 on secondPid, then compile 4 triggers recycle to a third PID.
      await supervisor.compileAndCheck({ path: exploreFile });
      await supervisor.compileAndCheck({ path: exploreFile });
      assert.equal(supervisor.compileCount, 3);
      assert.equal(supervisor.activePid, secondPid);

      const r4 = await supervisor.compileAndCheck({ path: exploreFile });
      const thirdPid = supervisor.activePid;
      assert.notEqual(thirdPid, secondPid);
      assert.equal(r4.compileCount, 1);
      assert.equal(supervisor.stats().recycles, 1);

      // Wait past idleShutdownMs (80 ms) -> worker closes cleanly and rehydrates on renderView.
      await new Promise((r) => setTimeout(r, 150));
      assert.equal(supervisor.activePid, null);
      assert.ok(supervisor.stats().idleShutdowns >= 1);

      const rendered = await supervisor.renderView({ direction: "main", screen: "home", view: "light" });
      assert.ok(rendered.pngBase64);
      assert.ok(supervisor.activePid > 0);
    } finally {
      await supervisor.close();
    }
  });
});

test("MCP check_design returns multi-block ImageContent on pass and deduplicates views via same", async () => {
  await withTempDir(async (dir) => {
    const workerScript = writeFakeWorkerScript(dir);
    const exploreFile = path.join(dir, "Card.explore.kt");
    fs.writeFileSync(exploreFile, "// PASS\n");

    const supervisor = new WorkerSupervisor({
      workerCommandFactory: () => ({ command: process.execPath, args: [workerScript] }),
    });
    const mcp = new PreviewMcpServer({
      supervisor,
      enableBoard: false,
      openBrowser: false,
    });

    try {
      const passRes = await mcp.handleMessage({
        jsonrpc: "2.0",
        id: 1,
        method: "tools/call",
        params: { name: "check_design", arguments: { path: exploreFile } },
      });
      assert.equal(passRes.result.isError, false);
      // 1 TextContent + 2 ImageContent (light + desktop)
      assert.equal(passRes.result.content.length, 3);
      assert.equal(passRes.result.content[0].type, "text");
      assert.equal(passRes.result.content[1].type, "image");
      assert.equal(passRes.result.content[1].mimeType, "image/png");
      assert.equal(passRes.result.content[2].type, "image");

      const parsed = JSON.parse(passRes.result.content[0].text);
      assert.equal(parsed.pass, true);
      assert.equal(parsed.renders.find((r) => r.view === "foldable").same, "light");
      assert.ok(!passRes.result.content[0].text.includes("PNG_LIGHT"));

      // When check fails, only TextContent is returned unless includeImages is set.
      fs.writeFileSync(exploreFile, "// FAIL_CHECK\n");
      const failRes = await mcp.handleMessage({
        jsonrpc: "2.0",
        id: 2,
        method: "tools/call",
        params: { name: "check_design", arguments: { path: exploreFile } },
      });
      assert.equal(failRes.result.content.length, 1);
      assert.equal(JSON.parse(failRes.result.content[0].text).pass, false);

      const treeRes = await mcp.handleMessage({
        jsonrpc: "2.0",
        id: 3,
        method: "tools/call",
        params: { name: "get_tree", arguments: { direction: "main", screen: "home", view: "light" } },
      });
      const treeJson = JSON.parse(treeRes.result.content[0].text);
      assert.equal(treeJson.tree.nodes[0].sourceLine, 12);

      // Direct unit check on formatCheckSummary
      const directSummary = formatCheckSummary({ valid: true, pass: true, renders: [] }, "http://127.0.0.1:9999/?token=t");
      assert.equal(directSummary.boardUrl, "http://127.0.0.1:9999/?token=t");
    } finally {
      await mcp.close();
    }
  });
});
