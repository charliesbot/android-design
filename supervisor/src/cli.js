import { spawn } from "node:child_process";
import fs from "node:fs";
import path from "node:path";
import { PACKAGE_VERSION } from "./constants.js";
import { BoardServer } from "./board-server.js";
import { JarCache } from "./jar-cache.js";
import { locateJava } from "./java-locator.js";
import { PreviewMcpServer, defaultOpenBrowser } from "./mcp-server.js";
import { WorkerSupervisor } from "./worker-supervisor.js";

function splitCsv(value) {
  return value
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean);
}

function parseCliArgs(argv) {
  const args = [...argv];
  let command = "mcp";
  if (args[0] && !args[0].startsWith("-")) {
    command = args.shift();
  }

  let positional = null;
  const flags = {
    noOpen: false,
    port: 0,
  };

  for (let i = 0; i < args.length; i += 1) {
    const arg = args[i];
    if (arg === "--version" || arg === "-v") {
      return { command: "version", flags, positional: null };
    }
    if (arg === "--help" || arg === "-h") {
      return { command: "help", flags, positional: null };
    }
    if (arg === "--no-open") {
      flags.noOpen = true;
      continue;
    }
    if (arg === "--design" || arg === "--out" || arg === "--port") {
      const next = args[++i];
      if (next == null) throw new Error(`${arg} requires a value`);
      const key = arg.slice(2);
      flags[key] = key === "port" ? Number.parseInt(next, 10) : next;
      continue;
    }
    if (arg === "--directions" || arg === "--screens" || arg === "--views") {
      const next = args[++i];
      if (next == null) throw new Error(`${arg} requires a value`);
      flags[arg.slice(2)] = splitCsv(next);
      continue;
    }
    if (arg.startsWith("-")) {
      throw new Error(`Unknown option: ${arg}`);
    }
    if (positional === null) {
      positional = arg;
    } else {
      throw new Error(`Unexpected argument: ${arg}`);
    }
  }

  return { command, flags, positional };
}

function usageText() {
  return [
    "Usage:",
    "  android-design-preview [mcp]              Run the stdio MCP server (default)",
    "  android-design-preview serve <file>       Open interactive browser board with live reload",
    "    [--design <DESIGN.md>] [--port <n>] [--no-open]",
    "  android-design-preview render <file> --out <dir>",
    "    [--design <DESIGN.md>] [--directions a,b] [--screens a,b] [--views a,b]",
    "  android-design-preview --version          Print version",
  ].join("\n");
}

async function runServe(targetPath, flags) {
  if (!targetPath) {
    throw new Error("serve requires a path to a .explore.kt or .proposal.json file");
  }
  const absPath = path.resolve(targetPath);
  if (!fs.existsSync(absPath)) {
    throw new Error(`No such file: ${absPath}`);
  }

  const supervisor = new WorkerSupervisor();
  supervisor.warmJarInBackground();

  const boardServer = new BoardServer({
    supervisor,
    port: Number.isInteger(flags.port) ? flags.port : 0,
  });
  await boardServer.start();

  const shutdown = async () => {
    await boardServer.close();
    await supervisor.close();
    process.exit(0);
  };
  process.once("SIGINT", shutdown);
  process.once("SIGTERM", shutdown);

  const result = await supervisor.compileAndCheck({
    path: absPath,
    ...(flags.design ? { design: path.resolve(flags.design) } : {}),
    includeImages: false,
  });
  boardServer.watchPaths({
    sourcePath: absPath,
    designPath: result.designPath || (flags.design ? path.resolve(flags.design) : null),
  });

  const url = boardServer.tokenizedUrl();
  process.stdout.write(`Board listening at ${url}\n`);
  if (!flags.noOpen && !process.env.CI && process.env.ANDROID_DESIGN_NO_OPEN !== "1") {
    defaultOpenBrowser(url);
  }
}

async function runRender(targetPath, flags) {
  if (!targetPath || !flags.out) {
    throw new Error("render requires <file> and --out <dir>");
  }
  const absPath = path.resolve(targetPath);
  if (!fs.existsSync(absPath)) {
    throw new Error(`No such file: ${absPath}`);
  }
  const outDir = path.resolve(flags.out);
  await fs.promises.mkdir(outDir, { recursive: true });

  if (!flags.directions) {
    const jarCache = new JarCache();
    const [jarPath, javaInfo] = await Promise.all([jarCache.ensureJar(), locateJava()]);
    const args = [
      "-XX:+DisplayVMOutputToStderr",
      "-Djava.awt.headless=true",
      "-jar",
      jarPath,
      "render",
      absPath,
      "--out",
      outDir,
    ];
    if (flags.design) args.push("--design", path.resolve(flags.design));
    if (flags.views) args.push("--views", flags.views.join(","));
    if (flags.screens) args.push("--screens", flags.screens.join(","));

    return new Promise((resolve, reject) => {
      const child = spawn(javaInfo.bin, args, {
        stdio: ["ignore", "pipe", "pipe"],
        windowsHide: true,
      });
      child.stdout.on("data", (chunk) => process.stdout.write(chunk));
      child.stderr.on("data", (chunk) => process.stderr.write(chunk));
      child.once("error", reject);
      child.once("exit", (code) => resolve(code ?? 1));
    });
  }

  const supervisor = new WorkerSupervisor();
  try {
    const result = await supervisor.compileAndCheck({
      path: absPath,
      outDir,
      ...(flags.design ? { design: path.resolve(flags.design) } : {}),
      directions: flags.directions,
      ...(flags.screens ? { screens: flags.screens } : {}),
      ...(flags.views ? { views: flags.views } : {}),
      includeImages: false,
      allImages: false,
    });
    const report = {
      proposal: result.sourcePath,
      design: result.designPath ?? null,
      theme: result.theme,
      valid: result.valid,
      errors: result.errors,
      renders: (result.renders || []).map((item) => {
        const filesBase = `${item.artifactName}-${item.view}`;
        const hasFiles = fs.existsSync(path.join(outDir, `${filesBase}.png`));
        const hasEndFiles = fs.existsSync(path.join(outDir, `${filesBase}-end.png`));
        return {
          screen: item.artifactName,
          view: item.view,
          files: hasFiles ? filesBase : null,
          ...(hasEndFiles ? { endFiles: `${filesBase}-end` } : {}),
          pass: item.pass,
          failures: item.failures,
        };
      }),
      pass: result.pass,
    };
    await fs.promises.writeFile(
      path.join(outDir, "report.json"),
      `${JSON.stringify(report, null, 2)}\n`,
      "utf8",
    );
    if (!result.valid) return 2;
    if (!result.pass) return 1;
    return 0;
  } finally {
    await supervisor.close();
  }
}

export async function runCli(argv = process.argv.slice(2)) {
  const { command, flags, positional } = parseCliArgs(argv);

  if (command === "version") {
    process.stdout.write(`${PACKAGE_VERSION}\n`);
    return 0;
  }
  if (command === "help") {
    process.stdout.write(`${usageText()}\n`);
    return 0;
  }
  if (command === "mcp") {
    const server = new PreviewMcpServer({
      ...(flags.noOpen ? { openBrowser: false } : {}),
    });
    await server.serveStdio();
    return 0;
  }
  if (command === "serve") {
    await runServe(positional, flags);
    return null;
  }
  if (command === "render") {
    return runRender(positional, flags);
  }

  throw new Error(`Unknown command '${command}'.\n\n${usageText()}`);
}
