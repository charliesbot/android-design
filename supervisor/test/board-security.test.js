import assert from "node:assert/strict";
import fs from "node:fs";
import http from "node:http";
import os from "node:os";
import path from "node:path";
import test from "node:test";
import { BoardServer } from "../src/board-server.js";
import { WorkerSupervisor } from "../src/worker-supervisor.js";

function withTempDir(fn) {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), "ad-board-test-"));
  return Promise.resolve()
    .then(() => fn(dir))
    .finally(() => {
      fs.rmSync(dir, { recursive: true, force: true });
    });
}

function writeMinimalWorker(dir) {
  const scriptPath = path.join(dir, "worker.mjs");
  fs.writeFileSync(
    scriptPath,
    `
import fs from "node:fs";
import readline from "node:readline";
let count = 0;
const rl = readline.createInterface({ input: process.stdin, crlfDelay: Infinity });
rl.on("line", (line) => {
  if (!line.trim()) return;
  const req = JSON.parse(line);
  if (req.method === "ping") {
    process.stdout.write(JSON.stringify({ id: req.id, result: { status: "ok", compileCount: count } }) + "\\n");
  } else if (req.method === "shutdown") {
    process.stdout.write(JSON.stringify({ id: req.id, result: { status: "bye" } }) + "\\n");
    process.exit(0);
  } else if (req.method === "compileAndCheck") {
    count += 1;
    const src = fs.readFileSync(req.params.path, "utf8").trim();
    process.stdout.write(JSON.stringify({
      id: req.id,
      result: {
        valid: true,
        pass: true,
        compileMs: 10,
        renderMs: 10,
        compileCount: count,
        surface: "phone",
        boardName: src.includes("V2") ? "Updated V2" : "Initial V1",
        sourcePath: req.params.path,
        theme: { seed: "#6750A4", variant: "tonal-spot", contrast: 0 },
        errors: [],
        boardMeta: {
          directions: [{ name: "Editorial", surface: "phone", startScreen: "feed", screens: ["feed"], overlays: [] }],
          views: [{ name: "light", width: 412, height: 915, round: false, dark: false, density: 2 }],
        },
        renders: [],
      },
    }) + "\\n");
  } else if (req.method === "renderView") {
    process.stdout.write(JSON.stringify({
      id: req.id,
      result: {
        direction: req.params.direction || "Editorial",
        screen: req.params.screen || "feed",
        view: req.params.view || "light",
        pass: true,
        renderMs: 6,
        compileCount: count,
        check: { pass: true, errors: [], notes: {}, colors: {} },
        tree: {
          screen: "feed",
          view: "light",
          nodes: [
            {
              id: "cta",
              type: "Button",
              primary: true,
              scrolls: false,
              bounds: { x: 16, y: 700, width: 120, height: 48 },
              scrollsHorizontally: false,
              sourceFile: "Board.explore.kt",
              sourceLine: 33,
            },
          ],
          findings: [],
        },
        pngBase64: Buffer.from("PNG_DATA").toString("base64"),
      },
    }) + "\\n");
  }
});
`,
    "utf8",
  );
  return scriptPath;
}

function rawHttpRequest({ port, method = "GET", pathAndQuery = "/", headers = {}, body = null }) {
  return new Promise((resolve, reject) => {
    const req = http.request(
      {
        host: "127.0.0.1",
        port,
        method,
        path: pathAndQuery,
        headers,
      },
      (res) => {
        const chunks = [];
        res.on("data", (c) => chunks.push(c));
        res.on("end", () => {
          resolve({
            status: res.statusCode,
            headers: res.headers,
            body: Buffer.concat(chunks).toString("utf8"),
          });
        });
      },
    );
    req.on("error", reject);
    if (body) req.write(body);
    req.end();
  });
}

test("board server enforces Host, Origin, 256-bit token, no compile route, and live SSE watch", async () => {
  await withTempDir(async (dir) => {
    const workerScript = writeMinimalWorker(dir);
    const explorePath = path.join(dir, "Board.explore.kt");
    fs.writeFileSync(explorePath, "// V1\n", "utf8");

    const supervisor = new WorkerSupervisor({
      workerCommandFactory: () => ({ command: process.execPath, args: [workerScript] }),
    });
    const board = new BoardServer({ supervisor, debounceMs: 40 });
    await board.start();

    try {
      await supervisor.compileAndCheck({ path: explorePath });
      board.watchPaths({ sourcePath: explorePath });

      const port = board.port;
      const origin = `http://127.0.0.1:${port}`;
      const token = board.token;
      assert.equal(token.length, 64);

      // 1. Spoofed Host header rejected (DNS rebinding protection)
      const spoofedHost = await rawHttpRequest({
        port,
        pathAndQuery: `/?token=${token}`,
        headers: { Host: "evil.attacker.com:8080" },
      });
      assert.equal(spoofedHost.status, 403);

      const localhostHost = await rawHttpRequest({
        port,
        pathAndQuery: `/?token=${token}`,
        headers: { Host: `localhost:${port}` },
      });
      assert.equal(localhostHost.status, 403);

      // 2. Missing or wrong token rejected
      const missingToken = await rawHttpRequest({
        port,
        pathAndQuery: "/",
        headers: { Host: `127.0.0.1:${port}` },
      });
      assert.equal(missingToken.status, 403);

      const wrongToken = await rawHttpRequest({
        port,
        pathAndQuery: `/?token=${"0".repeat(64)}`,
        headers: { Host: `127.0.0.1:${port}` },
      });
      assert.equal(wrongToken.status, 403);

      // 3. POST /api/render requires BOTH Origin and token
      const missingOrigin = await rawHttpRequest({
        port,
        method: "POST",
        pathAndQuery: "/api/render",
        headers: {
          Host: `127.0.0.1:${port}`,
          "X-Session-Token": token,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ direction: "Editorial" }),
      });
      assert.equal(missingOrigin.status, 403);

      const evilOrigin = await rawHttpRequest({
        port,
        method: "POST",
        pathAndQuery: "/api/render",
        headers: {
          Host: `127.0.0.1:${port}`,
          Origin: "https://evil.example",
          "X-Session-Token": token,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ direction: "Editorial" }),
      });
      assert.equal(evilOrigin.status, 403);

      // 4. Compile endpoints do not exist on HTTP server
      const compileRoute = await rawHttpRequest({
        port,
        method: "POST",
        pathAndQuery: "/api/compile",
        headers: {
          Host: `127.0.0.1:${port}`,
          Origin: origin,
          "X-Session-Token": token,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ path: explorePath }),
      });
      assert.equal(compileRoute.status, 404);

      // 5. Valid HTML board response + valid POST /api/render
      const htmlRes = await rawHttpRequest({
        port,
        pathAndQuery: `/?token=${token}`,
        headers: { Host: `127.0.0.1:${port}` },
      });
      assert.equal(htmlRes.status, 200);
      assert.ok(htmlRes.headers["content-security-policy"].includes("frame-ancestors 'none'"));
      assert.ok(htmlRes.body.includes('id="stage-svg"'));

      const renderRes = await rawHttpRequest({
        port,
        method: "POST",
        pathAndQuery: "/api/render",
        headers: {
          Host: `127.0.0.1:${port}`,
          Origin: origin,
          "X-Session-Token": token,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ direction: "Editorial", view: "light" }),
      });
      assert.equal(renderRes.status, 200);
      const rendered = JSON.parse(renderRes.body);
      assert.equal(rendered.tree.nodes[0].sourceFile, "Board.explore.kt");
      assert.equal(rendered.tree.nodes[0].sourceLine, 33);

      // Oversized body (> 16 KB) cleanly returns HTTP 413 rather than dropping the connection.
      const oversizedRes = await rawHttpRequest({
        port,
        method: "POST",
        pathAndQuery: "/api/render",
        headers: {
          Host: `127.0.0.1:${port}`,
          Origin: origin,
          "X-Session-Token": token,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ seed: "#6750A4", padding: "x".repeat(20 * 1024) }),
      });
      assert.equal(oversizedRes.status, 413);

      // 6. Live SSE push on file edit
      const ssePromise = new Promise((resolve, reject) => {
        const timeout = setTimeout(() => reject(new Error("Timed out waiting for SSE V2 update")), 4_000);
        const sseReq = http.get(
          {
            host: "127.0.0.1",
            port,
            path: `/api/events?token=${token}`,
            headers: { Host: `127.0.0.1:${port}` },
          },
          (res) => {
            assert.equal(res.statusCode, 200);
            let buf = "";
            res.on("data", (chunk) => {
              buf += chunk.toString("utf8");
              if (buf.includes("Updated V2")) {
                clearTimeout(timeout);
                res.destroy();
                resolve(buf);
              }
            });
          },
        );
        sseReq.on("error", () => {});
      });

      // Give SSE subscription a tick to connect, then mutate watched file.
      await new Promise((r) => setTimeout(r, 80));
      fs.writeFileSync(explorePath, "// V2\n", "utf8");
      const ssePayload = await ssePromise;
      assert.ok(ssePayload.includes("event: board"));
      assert.ok(ssePayload.includes("Updated V2"));
    } finally {
      await board.close();
      await supervisor.close();
    }
  });
});
