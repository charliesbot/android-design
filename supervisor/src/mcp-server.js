import { spawn } from "node:child_process";
import fs from "node:fs";
import path from "node:path";
import { PACKAGE_VERSION } from "./constants.js";
import { BoardServer } from "./board-server.js";
import { WorkerSupervisor } from "./worker-supervisor.js";

const DEFAULT_PROTOCOL_VERSION = "2025-03-26";

const CHECK_DESIGN_TOOL = {
  name: "check_design",
  description:
    "Compile and check a .explore.kt (or .proposal.json) file against Material 3 Expressive / Wear M3 design rules across views. Returns compact findings per direction, screen, overlay, and view, plus native-resolution light+desktop (or watch+watch-large) PNG images once checks pass.",
  inputSchema: {
    type: "object",
    properties: {
      path: {
        type: "string",
        description: "Path to the .explore.kt (or .proposal.json) exploration file.",
      },
      design: {
        type: "string",
        description: "Optional path to DESIGN.md (auto-discovered near the file when omitted).",
      },
      directions: {
        type: "array",
        items: { type: "string" },
        description: "Optional direction names to render and check. Defaults to all directions.",
      },
      screens: {
        type: "array",
        items: { type: "string" },
        description: "Optional screen ids to render and check. Defaults to all screens.",
      },
      views: {
        type: "array",
        items: { type: "string" },
        description:
          "Optional views to check (light, dark, foldable, tablet, desktop, watch, watch-large). Defaults to all catalog views.",
      },
      includeImages: {
        type: "boolean",
        description: "Include primary view PNG images even when findings fail.",
      },
      allImages: {
        type: "boolean",
        description: "Include PNG images for every rendered screen and view.",
      },
    },
    required: ["path"],
  },
};

const GET_TREE_TOOL = {
  name: "get_tree",
  description:
    "Return the compact LayoutTree (measured bounds, text metrics, roles, colors, source locations, and findings) for a screen/view rendered by the most recent check_design call.",
  inputSchema: {
    type: "object",
    properties: {
      direction: {
        type: "string",
        description: "Direction name. Defaults to the first direction in the board.",
      },
      screen: {
        type: "string",
        description: "Screen id. Defaults to the direction's startScreen.",
      },
      overlay: {
        type: "string",
        description: "Optional overlay id if inspecting an open modal, dialog, sheet, or menu.",
      },
      view: {
        type: "string",
        description: "View name (light, dark, foldable, tablet, desktop, watch, watch-large). Defaults to light (or watch).",
      },
      maxDepth: {
        type: "integer",
        description: "Optional node tree depth limit.",
      },
    },
  },
};

export const MCP_TOOLS = Object.freeze([CHECK_DESIGN_TOOL, GET_TREE_TOOL]);

/**
 * Formats worker compileAndCheck results for MCP TextContent: strips inline base64 PNG payloads
 * (returned separately as ImageContent blocks) and deduplicates identical views via `same`.
 */
export function formatCheckSummary(workerResult, boardUrl = null) {
  const seenPerTarget = new Map();
  const renders = (workerResult.renders || []).map((item) => {
    const targetKey = `${item.direction}\0${item.screen}\0${item.overlay ?? ""}`;
    let seenSignatures = seenPerTarget.get(targetKey);
    if (!seenSignatures) {
      seenSignatures = new Map();
      seenPerTarget.set(targetKey, seenSignatures);
    }

    const signature = JSON.stringify({
      pass: item.pass,
      failures: item.failures,
      check: item.check,
      layout: item.layout ?? null,
    });

    const firstView = seenSignatures.get(signature);
    if (firstView) {
      return {
        direction: item.direction,
        screen: item.screen,
        ...(item.overlay ? { overlay: item.overlay } : {}),
        view: item.view,
        artifactName: item.artifactName,
        same: firstView,
      };
    }

    seenSignatures.set(signature, item.view);
    return {
      direction: item.direction,
      screen: item.screen,
      ...(item.overlay ? { overlay: item.overlay } : {}),
      view: item.view,
      artifactName: item.artifactName,
      pass: item.pass,
      renderMs: item.renderMs,
      check: item.check,
      ...(item.layout ? { layout: item.layout } : {}),
      failures: item.failures,
    };
  });

  return {
    valid: workerResult.valid,
    pass: workerResult.pass,
    compileMs: workerResult.compileMs,
    renderMs: workerResult.renderMs,
    compileCount: workerResult.compileCount,
    surface: workerResult.surface,
    ...(workerResult.boardName ? { boardName: workerResult.boardName } : {}),
    sourcePath: workerResult.sourcePath,
    ...(workerResult.designPath ? { designPath: workerResult.designPath } : {}),
    theme: workerResult.theme,
    errors: workerResult.errors || [],
    ...(boardUrl ? { boardUrl } : {}),
    renders,
  };
}

export function extractImageContentBlocks(workerResult) {
  const blocks = [];
  for (const item of workerResult.renders || []) {
    if (item.pngBase64) {
      blocks.push({
        type: "image",
        data: item.pngBase64,
        mimeType: "image/png",
      });
    }
    if (item.endPngBase64) {
      blocks.push({
        type: "image",
        data: item.endPngBase64,
        mimeType: "image/png",
      });
    }
  }
  return blocks;
}

export function defaultOpenBrowser(url, platform = process.platform) {
  let command;
  let args;
  if (platform === "darwin") {
    command = "open";
    args = [url];
  } else if (platform === "win32") {
    command = "cmd";
    args = ["/c", "start", "", url];
  } else {
    command = "xdg-open";
    args = [url];
  }
  try {
    const child = spawn(command, args, {
      detached: true,
      stdio: "ignore",
      windowsHide: true,
    });
    child.unref();
  } catch {
    // Non-fatal in headless environments without a desktop browser opener.
  }
}

function shouldAutoOpenBrowser(options, env = process.env) {
  if (options.openBrowser === false) return false;
  if (options.openBrowser === true) return true;
  if (env.CI) return false;
  if (env.ANDROID_DESIGN_NO_OPEN === "1" || env.ANDROID_DESIGN_HEADLESS === "1") return false;
  if (env.NODE_ENV === "test") return false;
  return true;
}

export class PreviewMcpServer {
  constructor(options = {}) {
    this.supervisor = options.supervisor ?? new WorkerSupervisor(options);
    this.env = options.env ?? process.env;
    this.enableBoard =
      options.enableBoard ??
      (this.env.ANDROID_DESIGN_DISABLE_BOARD !== "1");
    this.openBrowserEnabled = shouldAutoOpenBrowser(options, this.env);
    this.openBrowserFn = options.openBrowserFn ?? defaultOpenBrowser;
    this.boardServer = null;
    this.boardOpenedOnce = false;
    this.#queue = Promise.resolve();
  }

  #queue;

  async handleMessage(msg) {
    if (!msg || typeof msg !== "object" || typeof msg.method !== "string") {
      if (msg && Object.hasOwn(msg, "id")) {
        return {
          jsonrpc: "2.0",
          id: msg.id,
          error: { code: -32600, message: "Invalid Request" },
        };
      }
      return null;
    }

    const hasId = Object.hasOwn(msg, "id") && msg.id !== undefined;
    const { id, method, params } = msg;

    // Notifications (no id) never send a JSON-RPC response.
    if (!hasId) {
      return null;
    }

    try {
      const result = await this.#dispatch(method, params || {});
      return { jsonrpc: "2.0", id, result };
    } catch (err) {
      return {
        jsonrpc: "2.0",
        id,
        error: {
          code: err.rpcCode ?? -32603,
          message: err.message || "Internal error",
        },
      };
    }
  }

  async #dispatch(method, params) {
    switch (method) {
      case "initialize": {
        const requested = typeof params.protocolVersion === "string"
          ? params.protocolVersion
          : DEFAULT_PROTOCOL_VERSION;
        return {
          protocolVersion: requested,
          capabilities: { tools: {} },
          serverInfo: {
            name: "android-design-preview",
            version: PACKAGE_VERSION,
          },
        };
      }

      case "ping":
        return {};

      case "tools/list":
        return { tools: MCP_TOOLS };

      case "tools/call":
        return this.#handleToolCall(params);

      default: {
        const err = new Error(`Method not found: ${method}`);
        err.rpcCode = -32601;
        throw err;
      }
    }
  }

  async #handleToolCall(params) {
    const toolName = params.name;
    const args = params.arguments && typeof params.arguments === "object" ? params.arguments : {};

    if (toolName === "check_design") {
      if (typeof args.path !== "string" || !args.path.trim()) {
        return {
          isError: true,
          content: [{ type: "text", text: "Missing required argument 'path'." }],
        };
      }

      const resolvedPath = path.resolve(args.path);
      if (!fs.existsSync(resolvedPath)) {
        return {
          isError: true,
          content: [{ type: "text", text: `File not found: ${resolvedPath}` }],
        };
      }

      try {
        const result = await this.supervisor.compileAndCheck({
          path: resolvedPath,
          ...(args.design ? { design: path.resolve(args.design) } : {}),
          ...(Array.isArray(args.directions) ? { directions: args.directions } : {}),
          ...(Array.isArray(args.screens) ? { screens: args.screens } : {}),
          ...(Array.isArray(args.views) ? { views: args.views } : {}),
          includeImages: Boolean(args.includeImages),
          allImages: Boolean(args.allImages),
        });

        let boardUrl = null;
        if (this.enableBoard) {
          if (!this.boardServer) {
            this.boardServer = new BoardServer({ supervisor: this.supervisor });
            await this.boardServer.start();
          }
          this.boardServer.watchPaths({
            sourcePath: resolvedPath,
            designPath: result.designPath || (args.design ? path.resolve(args.design) : null),
          });
          boardUrl = this.boardServer.tokenizedUrl();
          if (this.openBrowserEnabled && !this.boardOpenedOnce) {
            this.boardOpenedOnce = true;
            this.openBrowserFn(boardUrl);
          }
        }

        const summary = formatCheckSummary(result, boardUrl);
        const imageBlocks = extractImageContentBlocks(result);
        return {
          isError: false,
          content: [
            { type: "text", text: JSON.stringify(summary) },
            ...imageBlocks,
          ],
        };
      } catch (err) {
        return {
          isError: true,
          content: [{ type: "text", text: err.message || String(err) }],
        };
      }
    }

    if (toolName === "get_tree") {
      try {
        const result = await this.supervisor.getTree({
          ...(args.direction ? { direction: args.direction } : {}),
          ...(args.screen ? { screen: args.screen } : {}),
          ...(args.overlay ? { overlay: args.overlay } : {}),
          ...(args.view ? { view: args.view } : {}),
          ...(Number.isInteger(args.maxDepth) ? { maxDepth: args.maxDepth } : {}),
        });
        return {
          isError: false,
          content: [{ type: "text", text: JSON.stringify(result) }],
        };
      } catch (err) {
        return {
          isError: true,
          content: [{ type: "text", text: err.message || String(err) }],
        };
      }
    }

    const err = new Error(`Unknown tool: ${toolName}`);
    err.rpcCode = -32602;
    throw err;
  }

  /**
   * Attaches newline-delimited JSON-RPC stdio transport and warms the JAR cache asynchronously
   * without blocking initialize / tools/list or spawning a JVM process.
   */
  serveStdio({ input = process.stdin, output = process.stdout, warmCache = true } = {}) {
    if (warmCache) {
      this.supervisor.warmJarInBackground();
    }

    let buffer = "";

    const writeResponse = (response) => {
      if (!response) return;
      output.write(`${JSON.stringify(response)}\n`);
    };

    const handleLine = (line) => {
      let msg;
      try {
        msg = JSON.parse(line);
      } catch (err) {
        writeResponse({
          jsonrpc: "2.0",
          id: null,
          error: { code: -32700, message: `Parse error: ${err.message}` },
        });
        return;
      }
      this.#queue = this.#queue
        .then(async () => {
          const response = await this.handleMessage(msg);
          writeResponse(response);
        })
        .catch(() => {});
    };

    const onData = (chunk) => {
      buffer += chunk.toString("utf8");
      while (true) {
        const nl = buffer.indexOf("\n");
        if (nl < 0) break;
        const line = buffer.slice(0, nl).trim();
        buffer = buffer.slice(nl + 1);
        if (line) handleLine(line);
      }
    };

    const closePromise = new Promise((resolve) => {
      input.on("data", onData);
      input.once("end", async () => {
        if (buffer.trim()) {
          handleLine(buffer.trim());
          buffer = "";
        }
        await this.#queue;
        await this.close();
        resolve();
      });
    });

    return closePromise;
  }

  async close() {
    if (this.boardServer) {
      await this.boardServer.close();
      this.boardServer = null;
    }
    await this.supervisor.close();
  }
}
