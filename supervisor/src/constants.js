import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { fileURLToPath } from "node:url";

const SRC_DIR = path.dirname(fileURLToPath(import.meta.url));
const PKG_PATH = path.resolve(SRC_DIR, "..", "package.json");
const PKG = JSON.parse(fs.readFileSync(PKG_PATH, "utf8"));

export const PACKAGE_NAME = PKG.name;
export const PACKAGE_VERSION = PKG.version;
export const DEFAULT_REPO = "charliesbot/android-design";
export const JAR_FILENAME = "android-design-preview.jar";
export const JAR_SHA_FILENAME = `${JAR_FILENAME}.sha256`;

export const BOOT_WARMUP_TIMEOUT_MS = 30_000;
export const WARM_CALL_TIMEOUT_MS = 5_000;
export const MAX_COMPILES_BEFORE_RECYCLE = 25;
export const IDLE_SHUTDOWN_MS = 5 * 60 * 1000;

export const PRIMARY_VIEWS_PHONE = Object.freeze(["light", "desktop"]);
export const PRIMARY_VIEWS_WEAR = Object.freeze(["watch", "watch-large"]);

export function defaultCacheRoot(env = process.env, homedir = os.homedir()) {
  if (env.ANDROID_DESIGN_PREVIEW_CACHE_DIR) {
    return path.resolve(env.ANDROID_DESIGN_PREVIEW_CACHE_DIR);
  }
  return path.join(homedir, ".cache", "android-design-preview");
}

export function defaultReleaseJarUrl(version = PACKAGE_VERSION, repo = DEFAULT_REPO) {
  return `https://github.com/${repo}/releases/download/v${version}/${JAR_FILENAME}`;
}

export function defaultReleaseShaUrl(jarUrl) {
  return `${jarUrl}.sha256`;
}
