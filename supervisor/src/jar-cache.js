import crypto from "node:crypto";
import fs from "node:fs";
import http from "node:http";
import https from "node:https";
import path from "node:path";
import { pipeline } from "node:stream/promises";
import {
  JAR_FILENAME,
  PACKAGE_VERSION,
  defaultCacheRoot,
  defaultReleaseJarUrl,
  defaultReleaseShaUrl,
} from "./constants.js";

const MAX_REDIRECTS = 5;

export function parseSha256Text(text) {
  if (!text || typeof text !== "string") return null;
  const match = text.trim().match(/\b([a-fA-F0-9]{64})\b/);
  return match ? match[1].toLowerCase() : null;
}

function timingSafeHexEqual(a, b) {
  if (typeof a !== "string" || typeof b !== "string") return false;
  const cleanA = a.trim().toLowerCase();
  const cleanB = b.trim().toLowerCase();
  if (!/^[a-f0-9]{64}$/.test(cleanA) || !/^[a-f0-9]{64}$/.test(cleanB)) {
    return false;
  }
  return crypto.timingSafeEqual(Buffer.from(cleanA, "hex"), Buffer.from(cleanB, "hex"));
}

function requestWithRedirects(urlString, onProgress, redirectsLeft = MAX_REDIRECTS) {
  return new Promise((resolve, reject) => {
    let parsed;
    try {
      parsed = new URL(urlString);
    } catch (err) {
      reject(new Error(`Invalid JAR URL '${urlString}': ${err.message}`));
      return;
    }

    const client = parsed.protocol === "https:" ? https : parsed.protocol === "http:" ? http : null;
    if (!client) {
      reject(new Error(`Unsupported URL scheme '${parsed.protocol}' for '${urlString}'`));
      return;
    }

    const req = client.get(
      parsed,
      {
        headers: {
          "User-Agent": `android-design-preview/${PACKAGE_VERSION}`,
        },
      },
      (res) => {
        const status = res.statusCode ?? 0;
        if (status >= 300 && status < 400 && res.headers.location) {
          res.resume();
          if (redirectsLeft <= 0) {
            reject(new Error(`Too many redirects downloading '${urlString}'`));
            return;
          }
          const nextUrl = new URL(res.headers.location, parsed).toString();
          resolve(requestWithRedirects(nextUrl, onProgress, redirectsLeft - 1));
          return;
        }
        if (status < 200 || status >= 300) {
          res.resume();
          reject(new Error(`HTTP ${status} downloading '${urlString}'`));
          return;
        }
        resolve(res);
      },
    );

    req.setTimeout(120_000, () => {
      req.destroy(new Error(`Timed out downloading '${urlString}'`));
    });
    req.on("error", reject);
  });
}

async function fetchText(urlString) {
  const res = await requestWithRedirects(urlString);
  const chunks = [];
  for await (const chunk of res) {
    chunks.push(Buffer.isBuffer(chunk) ? chunk : Buffer.from(chunk));
  }
  return Buffer.concat(chunks).toString("utf8");
}

async function downloadAndVerify(jarUrl, targetPath, { expectedSha256, shaUrl, onProgress } = {}) {
  let expected = expectedSha256 ? parseSha256Text(expectedSha256) : null;
  if (!expected) {
    const resolvedShaUrl = shaUrl || defaultReleaseShaUrl(jarUrl);
    const shaText = await fetchText(resolvedShaUrl);
    expected = parseSha256Text(shaText);
    if (!expected) {
      throw new Error(`Invalid SHA-256 checksum payload from '${resolvedShaUrl}'`);
    }
  }

  await fs.promises.mkdir(path.dirname(targetPath), { recursive: true });
  const suffix = `${process.pid}-${crypto.randomBytes(6).toString("hex")}`;
  const tmpPath = `${targetPath}.tmp-${suffix}`;

  try {
    const res = await requestWithRedirects(jarUrl, onProgress);
    const totalBytes = Number.parseInt(res.headers["content-length"] || "0", 10) || 0;
    let downloadedBytes = 0;
    const hash = crypto.createHash("sha256");

    res.on("data", (chunk) => {
      hash.update(chunk);
      downloadedBytes += chunk.length;
      if (onProgress) {
        onProgress({ downloadedBytes, totalBytes });
      }
    });

    const out = fs.createWriteStream(tmpPath, { mode: 0o644 });
    await pipeline(res, out);

    const actual = hash.digest("hex");
    if (!timingSafeHexEqual(actual, expected)) {
      throw new Error(
        `SHA-256 checksum mismatch for '${jarUrl}': expected ${expected}, got ${actual}`,
      );
    }

    await fs.promises.rename(tmpPath, targetPath);
    return targetPath;
  } catch (err) {
    await fs.promises.rm(tmpPath, { force: true }).catch(() => {});
    throw err;
  }
}

export class JarCache {
  constructor(options = {}) {
    this.env = options.env ?? process.env;
    this.version = options.version ?? PACKAGE_VERSION;
    this.cacheRoot = options.cacheRoot ?? defaultCacheRoot(this.env, options.homedir);
    this.jarUrl =
      options.jarUrl ?? this.env.ANDROID_DESIGN_PREVIEW_JAR_URL ?? defaultReleaseJarUrl(this.version);
    this.shaUrl =
      options.shaUrl ?? this.env.ANDROID_DESIGN_PREVIEW_JAR_SHA256_URL ?? defaultReleaseShaUrl(this.jarUrl);
    this.expectedSha256 =
      options.expectedSha256 ?? this.env.ANDROID_DESIGN_PREVIEW_JAR_SHA256 ?? null;
    this.onProgress = options.onProgress ?? null;
    this.onError = options.onError ?? null;
    this.inFlight = null;
  }

  overrideJarPath() {
    const raw = this.env.ANDROID_DESIGN_PREVIEW_JAR;
    return raw ? path.resolve(raw) : null;
  }

  cachedJarPath() {
    return path.join(this.cacheRoot, this.version, JAR_FILENAME);
  }

  /**
   * Starts a background download when the cached JAR is missing and no explicit override is set.
   * Never throws synchronously and never spawns a JVM.
   */
  warmUp() {
    const overridePath = this.overrideJarPath();
    if (overridePath) return false;
    const target = this.cachedJarPath();
    if (fs.existsSync(target)) return false;
    if (!this.inFlight) {
      this.inFlight = this.#runDownload(target);
      this.inFlight.catch((err) => {
        if (this.onError) this.onError(err);
      });
    }
    return true;
  }

  async ensureJar() {
    const overridePath = this.overrideJarPath();
    if (overridePath) {
      if (!fs.existsSync(overridePath)) {
        throw new Error(
          `ANDROID_DESIGN_PREVIEW_JAR points to missing file: ${overridePath}`,
        );
      }
      return overridePath;
    }

    const target = this.cachedJarPath();
    if (fs.existsSync(target)) {
      return target;
    }

    if (!this.inFlight) {
      this.inFlight = this.#runDownload(target);
    }
    return this.inFlight;
  }

  async #runDownload(target) {
    try {
      return await downloadAndVerify(this.jarUrl, target, {
        expectedSha256: this.expectedSha256,
        shaUrl: this.shaUrl,
        onProgress: this.onProgress,
      });
    } finally {
      this.inFlight = null;
    }
  }
}
