import { spawn } from "node:child_process";
import { EventEmitter } from "node:events";
import path from "node:path";
import {
  BOOT_WARMUP_TIMEOUT_MS,
  IDLE_SHUTDOWN_MS,
  MAX_COMPILES_BEFORE_RECYCLE,
  WARM_CALL_TIMEOUT_MS,
} from "./constants.js";
import { JarCache } from "./jar-cache.js";
import { locateJava } from "./java-locator.js";

const STDERR_RING_LIMIT = 40;
const SHUTDOWN_GRACE_MS = 1_000;

export class WorkerTimeoutError extends Error {
  constructor(message, { pid, timeoutMs, phase } = {}) {
    super(message);
    this.name = "WorkerTimeoutError";
    this.code = "worker_timeout";
    this.pid = pid ?? null;
    this.timeoutMs = timeoutMs ?? null;
    this.phase = phase ?? "warm";
  }
}

export class WorkerRpcError extends Error {
  constructor(code, message) {
    super(message || code);
    this.name = "WorkerRpcError";
    this.code = code || "worker_error";
  }
}

function resolveDefaultSpawnSpec(javaBin, jarPath) {
  return {
    command: javaBin,
    args: ["-XX:+DisplayVMOutputToStderr", "-Djava.awt.headless=true", "-jar", jarPath, "worker"],
    env: process.env,
  };
}

export class WorkerSupervisor extends EventEmitter {
  #queue = Promise.resolve();
  #worker = null;
  #idleTimer = null;
  #closed = false;
  #lastCompileParams = null;
  #latestBoard = null;
  #stats = {
    spawns: 0,
    totalCompiles: 0,
    recycles: 0,
    sigkills: 0,
    idleShutdowns: 0,
  };

  constructor(options = {}) {
    super();
    this.jarCache = options.jarCache ?? new JarCache(options);
    this.locateJavaFn = options.locateJava ?? locateJava;
    this.spawnFn = options.spawn ?? spawn;
    this.workerCommandFactory = options.workerCommandFactory ?? null;
    this.bootTimeoutMs = options.bootTimeoutMs ?? BOOT_WARMUP_TIMEOUT_MS;
    this.warmTimeoutMs = options.warmTimeoutMs ?? WARM_CALL_TIMEOUT_MS;
    this.maxCompilesBeforeRecycle =
      options.maxCompilesBeforeRecycle ?? MAX_COMPILES_BEFORE_RECYCLE;
    this.idleShutdownMs = options.idleShutdownMs ?? IDLE_SHUTDOWN_MS;
    this.javaOptions = options.javaOptions ?? {};
  }

  get activePid() {
    return this.#worker?.pid ?? null;
  }

  get compileCount() {
    return this.#worker?.compileCount ?? 0;
  }

  get latestBoard() {
    return this.#latestBoard;
  }

  stats() {
    return {
      ...this.#stats,
      activePid: this.activePid,
      compileCount: this.compileCount,
    };
  }

  warmJarInBackground() {
    return this.jarCache.warmUp();
  }

  /**
   * Resolves Java + JAR (downloading if uncached) before the JVM boot timer starts,
   * compiles [params.path], runs checks, and updates latest board state.
   */
  compileAndCheck(params) {
    return this.#enqueue(async () => {
      this.#assertOpen();
      this.#clearIdleTimer();

      const resolvedPath = path.resolve(params.path);
      const resolvedDesign = params.design ? path.resolve(params.design) : undefined;
      const normalizedParams = {
        ...params,
        path: resolvedPath,
        ...(resolvedDesign ? { design: resolvedDesign } : {}),
      };

      if (this.#worker && this.#worker.compileCount >= this.maxCompilesBeforeRecycle) {
        await this.#shutdownWorker("recycle");
        this.#stats.recycles += 1;
      }

      const worker = await this.#ensureWorker();
      const timeoutMs = worker.warmedUp
        ? this.warmTimeoutMs
        : this.#remainingBootBudgetMs(worker);
      const phase = worker.warmedUp ? "warm" : "boot-warmup";

      const result = await this.#sendRpc(worker, "compileAndCheck", normalizedParams, timeoutMs, phase);
      worker.warmedUp = true;
      worker.compileCount = result.compileCount ?? worker.compileCount + 1;
      this.#stats.totalCompiles += 1;
      this.#lastCompileParams = {
        path: normalizedParams.path,
        ...(normalizedParams.design ? { design: normalizedParams.design } : {}),
      };
      this.#latestBoard = {
        result,
        sourcePath: result.sourcePath || normalizedParams.path,
        designPath: result.designPath || normalizedParams.design || null,
        updatedAt: Date.now(),
      };
      this.emit("board", this.#latestBoard);
      this.#scheduleIdleTimer();
      return result;
    });
  }

  /**
   * Renders a single view with optional live overrides (seed/variant/dark/screen/overlay).
   * Never exposes compilation: only re-hydrates a previously MCP/CLI-compiled board if the
   * worker restarted due to idle shutdown or 25-compile rotation.
   */
  renderView(params = {}) {
    return this.#enqueue(async () => {
      this.#assertOpen();
      this.#clearIdleTimer();

      let worker = await this.#ensureWorker();
      if (!worker.warmedUp && this.#lastCompileParams) {
        await this.#rehydrateWorker(worker);
      }

      try {
        const result = await this.#sendRpc(
          worker,
          "renderView",
          params,
          this.warmTimeoutMs,
          "warm",
        );
        this.#scheduleIdleTimer();
        return result;
      } catch (err) {
        if (err instanceof WorkerRpcError && err.code === "no_compiled_board" && this.#lastCompileParams) {
          worker = await this.#ensureWorker();
          await this.#rehydrateWorker(worker);
          const retried = await this.#sendRpc(
            worker,
            "renderView",
            params,
            this.warmTimeoutMs,
            "warm",
          );
          this.#scheduleIdleTimer();
          return retried;
        }
        throw err;
      }
    });
  }

  getTree(params = {}) {
    return this.#enqueue(async () => {
      this.#assertOpen();
      this.#clearIdleTimer();

      let worker = await this.#ensureWorker();
      if (!worker.warmedUp && this.#lastCompileParams) {
        await this.#rehydrateWorker(worker);
      }

      try {
        const result = await this.#sendRpc(worker, "getTree", params, this.warmTimeoutMs, "warm");
        this.#scheduleIdleTimer();
        return result;
      } catch (err) {
        if (err instanceof WorkerRpcError && err.code === "no_compiled_board" && this.#lastCompileParams) {
          worker = await this.#ensureWorker();
          await this.#rehydrateWorker(worker);
          const retried = await this.#sendRpc(worker, "getTree", params, this.warmTimeoutMs, "warm");
          this.#scheduleIdleTimer();
          return retried;
        }
        throw err;
      }
    });
  }

  async recycleNow() {
    return this.#enqueue(async () => {
      if (this.#worker) {
        await this.#shutdownWorker("manual-recycle");
        this.#stats.recycles += 1;
      }
    });
  }

  async close() {
    this.#closed = true;
    this.#clearIdleTimer();
    const active = this.#worker;
    if (active) {
      await this.#shutdownWorker("close");
    }
    this.removeAllListeners();
  }

  #assertOpen() {
    if (this.#closed) {
      throw new Error("WorkerSupervisor has been closed");
    }
  }

  #enqueue(task) {
    const run = this.#queue.then(task, task);
    this.#queue = run.catch(() => {});
    return run;
  }

  #clearIdleTimer() {
    if (this.#idleTimer) {
      clearTimeout(this.#idleTimer);
      this.#idleTimer = null;
    }
  }

  #scheduleIdleTimer() {
    this.#clearIdleTimer();
    if (this.#closed || !this.idleShutdownMs || this.idleShutdownMs <= 0) {
      return;
    }
    this.#idleTimer = setTimeout(() => {
      this.#idleTimer = null;
      this.#enqueue(async () => {
        if (this.#worker && !this.#worker.inFlight) {
          await this.#shutdownWorker("idle");
          this.#stats.idleShutdowns += 1;
        }
      }).catch(() => {});
    }, this.idleShutdownMs);
    if (typeof this.#idleTimer.unref === "function") {
      this.#idleTimer.unref();
    }
  }

  async #rehydrateWorker(worker) {
    const params = {
      ...this.#lastCompileParams,
      includeImages: false,
      allImages: false,
    };
    const res = await this.#sendRpc(
      worker,
      "compileAndCheck",
      params,
      this.#remainingBootBudgetMs(worker),
      "boot-warmup",
    );
    worker.warmedUp = true;
    worker.compileCount = res.compileCount ?? worker.compileCount + 1;
  }

  async #ensureWorker() {
    if (this.#worker && !this.#worker.exited) {
      return this.#worker;
    }

    let spec;
    if (this.workerCommandFactory) {
      spec = await this.workerCommandFactory();
    } else {
      // Finish any JAR download and Java lookup BEFORE starting the 30 s JVM boot timer.
      const [jarPath, javaInfo] = await Promise.all([
        this.jarCache.ensureJar(),
        this.locateJavaFn(this.javaOptions),
      ]);
      spec = resolveDefaultSpawnSpec(javaInfo.bin, jarPath);
    }

    const child = this.spawnFn(spec.command, spec.args, {
      stdio: ["pipe", "pipe", "pipe"],
      env: spec.env ?? process.env,
      windowsHide: true,
    });

    const worker = {
      child,
      pid: child.pid ?? null,
      spawnStartedAt: Date.now(),
      nextId: 1,
      compileCount: 0,
      warmedUp: false,
      exited: false,
      inFlight: null,
      stdoutBuffer: "",
      stderrTail: [],
    };
    this.#stats.spawns += 1;

    child.stderr?.on("data", (chunk) => {
      const lines = chunk.toString("utf8").split(/\r?\n/);
      for (const line of lines) {
        if (!line) continue;
        worker.stderrTail.push(line);
        if (worker.stderrTail.length > STDERR_RING_LIMIT) {
          worker.stderrTail.shift();
        }
      }
    });

    child.stdout?.on("data", (chunk) => {
      worker.stdoutBuffer += chunk.toString("utf8");
      while (true) {
        const nl = worker.stdoutBuffer.indexOf("\n");
        if (nl < 0) break;
        const line = worker.stdoutBuffer.slice(0, nl).trim();
        worker.stdoutBuffer = worker.stdoutBuffer.slice(nl + 1);
        if (!line) continue;
        this.#handleWorkerLine(worker, line);
      }
    });

    child.once("error", (err) => {
      worker.exited = true;
      if (this.#worker === worker) this.#worker = null;
      this.#failInFlight(
        worker,
        new Error(`Worker process error: ${err.message}${this.#formatStderrTail(worker)}`),
      );
    });

    child.once("exit", (code, signal) => {
      worker.exited = true;
      if (this.#worker === worker) this.#worker = null;
      this.#failInFlight(
        worker,
        new Error(
          `Worker exited unexpectedly (code=${code}, signal=${signal})${this.#formatStderrTail(worker)}`,
        ),
      );
    });

    this.#worker = worker;

    try {
      await this.#sendRpc(
        worker,
        "ping",
        {},
        this.#remainingBootBudgetMs(worker),
        "boot-ping",
      );
      return worker;
    } catch (err) {
      if (this.#worker === worker) {
        this.#killWorker(worker);
      }
      throw err;
    }
  }

  #remainingBootBudgetMs(worker) {
    const elapsed = Date.now() - worker.spawnStartedAt;
    return Math.max(50, this.bootTimeoutMs - elapsed);
  }

  #handleWorkerLine(worker, line) {
    let msg;
    try {
      msg = JSON.parse(line);
    } catch {
      worker.stderrTail.push(`[non-json stdout] ${line}`);
      if (worker.stderrTail.length > STDERR_RING_LIMIT) {
        worker.stderrTail.shift();
      }
      return;
    }

    const pending = worker.inFlight;
    if (!pending) return;
    if (msg.id !== pending.id && msg.id != null) return;

    clearTimeout(pending.timer);
    worker.inFlight = null;

    if (msg.error) {
      pending.reject(new WorkerRpcError(msg.error.code, msg.error.message));
    } else {
      pending.resolve(msg.result);
    }
  }

  #failInFlight(worker, err) {
    const pending = worker.inFlight;
    if (!pending) return;
    clearTimeout(pending.timer);
    worker.inFlight = null;
    pending.reject(err);
  }

  #killWorker(worker) {
    if (this.#worker === worker) {
      this.#worker = null;
    }
    this.#stats.sigkills += 1;
    try {
      worker.child.kill("SIGKILL");
    } catch {
      // Ignore ESRCH if child already exited.
    }
  }

  async #shutdownWorker(reason) {
    const worker = this.#worker;
    if (!worker) return;
    this.#worker = null;
    if (worker.exited) return;

    try {
      await this.#sendRpc(worker, "shutdown", {}, SHUTDOWN_GRACE_MS, `shutdown:${reason}`);
    } catch {
      // Ignore shutdown timeout/error and force kill below if still alive.
    }

    if (!worker.exited) {
      try {
        worker.child.kill("SIGKILL");
      } catch {
        // Ignore already-dead process.
      }
    }
  }

  #formatStderrTail(worker) {
    if (!worker.stderrTail || worker.stderrTail.length === 0) return "";
    return `\nWorker stderr:\n${worker.stderrTail.slice(-10).join("\n")}`;
  }

  #sendRpc(worker, method, params, timeoutMs, phase) {
    if (worker.exited || !worker.child.stdin || worker.child.stdin.destroyed) {
      return Promise.reject(
        new Error(`Worker process is not running${this.#formatStderrTail(worker)}`),
      );
    }

    const id = worker.nextId++;
    const payload = `${JSON.stringify({ id, method, params })}\n`;

    return new Promise((resolve, reject) => {
      const timer = setTimeout(() => {
        if (worker.inFlight?.id !== id) return;
        worker.inFlight = null;
        const killedPid = worker.pid;
        this.#killWorker(worker);
        reject(
          new WorkerTimeoutError(
            `Worker timed out after ${timeoutMs} ms during '${method}' (${phase}); killed pid ${killedPid} with SIGKILL and a clean worker will handle the next request.${this.#formatStderrTail(worker)}`,
            { pid: killedPid, timeoutMs, phase },
          ),
        );
      }, timeoutMs);

      worker.inFlight = { id, resolve, reject, timer };

      try {
        worker.child.stdin.write(payload, "utf8");
      } catch (err) {
        clearTimeout(timer);
        worker.inFlight = null;
        reject(new Error(`Failed writing to worker stdin: ${err.message}`));
      }
    });
  }
}
