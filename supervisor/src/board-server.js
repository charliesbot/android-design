import crypto from "node:crypto";
import fs from "node:fs";
import http from "node:http";
import path from "node:path";
import { renderBoardHtml } from "./board-ui.js";

const MAX_RENDER_BODY_BYTES = 16 * 1024;
const ALLOWED_RENDER_FIELDS = new Set([
  "direction",
  "screen",
  "overlay",
  "view",
  "seed",
  "variant",
  "dark",
]);

function constantTimeTokenEqual(candidate, expected) {
  if (typeof candidate !== "string" || typeof expected !== "string") return false;
  const bufA = Buffer.from(candidate, "utf8");
  const bufB = Buffer.from(expected, "utf8");
  if (bufA.length !== bufB.length) return false;
  return crypto.timingSafeEqual(bufA, bufB);
}

function sendJson(res, status, payload, extraHeaders = {}) {
  const body = JSON.stringify(payload);
  res.writeHead(status, {
    "Content-Type": "application/json; charset=utf-8",
    "Cache-Control": "no-store",
    "X-Content-Type-Options": "nosniff",
    "Referrer-Policy": "no-referrer",
    ...extraHeaders,
  });
  res.end(body);
}

function formatBoardPayload(latestBoard) {
  if (!latestBoard?.result) return null;
  const r = latestBoard.result;
  return {
    valid: r.valid,
    pass: r.pass,
    compileMs: r.compileMs,
    renderMs: r.renderMs,
    compileCount: r.compileCount,
    surface: r.surface,
    boardName: r.boardName || null,
    sourcePath: r.sourcePath,
    sourceFile: r.sourcePath ? path.basename(r.sourcePath) : null,
    designPath: r.designPath || null,
    theme: r.theme,
    errors: r.errors || [],
    boardMeta: r.boardMeta || null,
    renders: r.renders || [],
    updatedAt: latestBoard.updatedAt,
  };
}

function readJsonBody(req) {
  return new Promise((resolve, reject) => {
    const chunks = [];
    let total = 0;
    let exceeded = false;
    req.on("data", (chunk) => {
      if (exceeded) return;
      total += chunk.length;
      if (total > MAX_RENDER_BODY_BYTES) {
        exceeded = true;
        chunks.length = 0;
        const err = new Error("Request body exceeds 16 KB limit");
        err.statusCode = 413;
        err.closeConnection = true;
        req.resume();
        reject(err);
        return;
      }
      chunks.push(chunk);
    });
    req.on("end", () => {
      if (exceeded) return;
      const raw = Buffer.concat(chunks).toString("utf8").trim();
      if (!raw) {
        resolve({});
        return;
      }
      try {
        const parsed = JSON.parse(raw);
        if (!parsed || typeof parsed !== "object" || Array.isArray(parsed)) {
          const err = new Error("Request body must be a JSON object");
          err.statusCode = 400;
          reject(err);
          return;
        }
        resolve(parsed);
      } catch (err) {
        err.statusCode = 400;
        reject(err);
      }
    });
    req.on("error", (err) => {
      if (!exceeded) reject(err);
    });
  });
}

export class BoardServer {
  #supervisor;
  #token;
  #requestedPort;
  #port = null;
  #server = null;
  #sseClients = new Set();
  #watchers = [];
  #watchedKey = null;
  #debounceMs;
  #debounceTimer = null;
  #onBoardListener = null;

  constructor({ supervisor, token, port = 0, debounceMs = 120 } = {}) {
    this.#supervisor = supervisor;
    this.#token = token ?? crypto.randomBytes(32).toString("hex");
    this.#requestedPort = port;
    this.#debounceMs = debounceMs;
  }

  get port() {
    return this.#port;
  }

  get token() {
    return this.#token;
  }

  origin() {
    if (!this.#port) throw new Error("BoardServer has not started");
    return `http://127.0.0.1:${this.#port}`;
  }

  tokenizedUrl() {
    return `${this.origin()}/?token=${this.#token}`;
  }

  async start() {
    if (this.#server) return this.#port;
    this.#server = http.createServer((req, res) => {
      this.#handleRequest(req, res).catch((err) => {
        const status = err.statusCode ?? 500;
        const extraHeaders = err.closeConnection || status === 413 ? { Connection: "close" } : {};
        if (!res.headersSent) {
          sendJson(res, status, { error: "server_error", message: err.message }, extraHeaders);
        }
        if (err.closeConnection || status === 413) {
          res.once("finish", () => {
            req.socket?.destroy();
          });
        }
      });
    });

    await new Promise((resolve, reject) => {
      this.#server.once("error", reject);
      this.#server.listen(this.#requestedPort, "127.0.0.1", () => {
        this.#server.off("error", reject);
        const addr = this.#server.address();
        this.#port = typeof addr === "object" && addr ? addr.port : this.#requestedPort;
        resolve();
      });
    });

    this.#onBoardListener = (latestBoard) => {
      this.broadcast("board", formatBoardPayload(latestBoard));
    };
    this.#supervisor.on("board", this.#onBoardListener);
    return this.#port;
  }

  broadcast(eventName, payload) {
    const data = JSON.stringify(payload);
    const frame = `event: ${eventName}\ndata: ${data}\n\n`;
    for (const res of this.#sseClients) {
      try {
        res.write(frame);
      } catch {
        this.#sseClients.delete(res);
      }
    }
  }

  watchPaths({ sourcePath, designPath } = {}) {
    if (!sourcePath) return;
    const absSource = path.resolve(sourcePath);
    const absDesign = designPath ? path.resolve(designPath) : null;
    const key = `${absSource}\0${absDesign || ""}`;
    if (this.#watchedKey === key) return;

    this.#clearWatchers();
    this.#watchedKey = key;

    const dirTargets = new Map();
    const registerTarget = (filePath) => {
      if (!filePath) return;
      const dir = path.dirname(filePath);
      const base = path.basename(filePath);
      let names = dirTargets.get(dir);
      if (!names) {
        names = new Set();
        dirTargets.set(dir, names);
      }
      names.add(base);
    };

    registerTarget(absSource);
    registerTarget(absDesign);

    const scheduleRecompile = () => {
      clearTimeout(this.#debounceTimer);
      this.#debounceTimer = setTimeout(() => {
        this.#supervisor
          .compileAndCheck({
            path: absSource,
            ...(absDesign && fs.existsSync(absDesign) ? { design: absDesign } : {}),
          })
          .catch((err) => {
            this.broadcast("error_event", { message: err.message || String(err) });
          });
      }, this.#debounceMs);
    };

    for (const [dir, names] of dirTargets.entries()) {
      if (!fs.existsSync(dir)) continue;
      try {
        const watcher = fs.watch(dir, (_eventType, filename) => {
          if (!filename || names.has(String(filename))) {
            scheduleRecompile();
          }
        });
        this.#watchers.push(watcher);
      } catch {
        // Ignore watch unsupported errors in constrained sandboxes.
      }
    }
  }

  async close() {
    clearTimeout(this.#debounceTimer);
    this.#debounceTimer = null;
    this.#clearWatchers();
    if (this.#onBoardListener) {
      this.#supervisor.off("board", this.#onBoardListener);
      this.#onBoardListener = null;
    }
    for (const res of this.#sseClients) {
      try {
        res.end();
      } catch {
        // Ignore.
      }
    }
    this.#sseClients.clear();
    if (this.#server) {
      const server = this.#server;
      this.#server = null;
      await new Promise((resolve) => server.close(resolve));
    }
  }

  #clearWatchers() {
    for (const w of this.#watchers) {
      try {
        w.close();
      } catch {
        // Ignore.
      }
    }
    this.#watchers = [];
    this.#watchedKey = null;
  }

  #extractToken(req, url) {
    const headerToken = req.headers["x-session-token"];
    if (typeof headerToken === "string" && headerToken) {
      return headerToken;
    }
    const auth = req.headers.authorization;
    if (typeof auth === "string" && auth.startsWith("Bearer ")) {
      return auth.slice("Bearer ".length).trim();
    }
    return url.searchParams.get("token") || "";
  }

  async #handleRequest(req, res) {
    const expectedHost = `127.0.0.1:${this.#port}`;
    if (req.headers.host !== expectedHost) {
      sendJson(res, 403, { error: "forbidden_host", message: "Invalid Host header" });
      return;
    }

    const url = new URL(req.url || "/", this.origin());
    const providedToken = this.#extractToken(req, url);
    if (!constantTimeTokenEqual(providedToken, this.#token)) {
      sendJson(res, 403, { error: "forbidden_token", message: "Missing or invalid session token" });
      return;
    }

    if (req.method === "GET" && (url.pathname === "/" || url.pathname === "/index.html")) {
      const nonce = crypto.randomBytes(16).toString("base64");
      const html = renderBoardHtml({ nonce });
      res.writeHead(200, {
        "Content-Type": "text/html; charset=utf-8",
        "Cache-Control": "no-store",
        "X-Content-Type-Options": "nosniff",
        "Referrer-Policy": "no-referrer",
        "Content-Security-Policy":
          `default-src 'none'; script-src 'nonce-${nonce}'; style-src 'nonce-${nonce}'; ` +
          "img-src 'self' data: blob:; connect-src 'self'; font-src 'none'; object-src 'none'; " +
          "frame-ancestors 'none'; base-uri 'none'",
      });
      res.end(html);
      return;
    }

    if (req.method === "GET" && url.pathname === "/api/board") {
      sendJson(res, 200, formatBoardPayload(this.#supervisor.latestBoard));
      return;
    }

    if (req.method === "GET" && url.pathname === "/api/events") {
      res.writeHead(200, {
        "Content-Type": "text/event-stream",
        "Cache-Control": "no-store",
        Connection: "keep-alive",
        "X-Content-Type-Options": "nosniff",
      });
      res.write(": connected\n\n");
      this.#sseClients.add(res);
      const latest = formatBoardPayload(this.#supervisor.latestBoard);
      if (latest) {
        res.write(`event: board\ndata: ${JSON.stringify(latest)}\n\n`);
      }
      const heartbeat = setInterval(() => {
        try {
          res.write(": heartbeat\n\n");
        } catch {
          clearInterval(heartbeat);
        }
      }, 20_000);
      heartbeat.unref?.();
      req.on("close", () => {
        clearInterval(heartbeat);
        this.#sseClients.delete(res);
      });
      return;
    }

    if (req.method === "POST" && url.pathname === "/api/render") {
      if (req.headers.origin !== this.origin()) {
        sendJson(res, 403, { error: "forbidden_origin", message: "Invalid Origin header" });
        return;
      }
      const body = await readJsonBody(req);
      const filtered = {};
      for (const [k, v] of Object.entries(body)) {
        if (!ALLOWED_RENDER_FIELDS.has(k)) {
          sendJson(res, 400, { error: "invalid_param", message: `Unsupported field '${k}'` });
          return;
        }
        filtered[k] = v;
      }
      try {
        const rendered = await this.#supervisor.renderView(filtered);
        sendJson(res, 200, rendered);
      } catch (err) {
        const status = err.code === "no_compiled_board" ? 409 : 400;
        sendJson(res, status, { error: err.code || "render_error", message: err.message });
      }
      return;
    }

    sendJson(res, 404, { error: "not_found", message: `Unknown route ${req.method} ${url.pathname}` });
  }
}
