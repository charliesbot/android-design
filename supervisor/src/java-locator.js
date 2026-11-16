import { execFile } from "node:child_process";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { promisify } from "node:util";

const execFileAsync = promisify(execFile);

export const MIN_JAVA_VERSION = 17;

/**
 * Parses `java -version` output into a major integer (`"1.8.0_392"` -> 8, `"17.0.20"` -> 17).
 */
export function parseJavaMajorVersion(output) {
  if (!output || typeof output !== "string") return null;
  const quoted = output.match(/version\s+"([^"]+)"/i);
  const raw = quoted ? quoted[1] : output;
  const parts = raw.match(/(\d+)(?:\.(\d+))?/);
  if (!parts) return null;
  const first = Number.parseInt(parts[1], 10);
  if (Number.isNaN(first)) return null;
  if (first === 1 && parts[2]) {
    const legacyMinor = Number.parseInt(parts[2], 10);
    return Number.isNaN(legacyMinor) ? null : legacyMinor;
  }
  return first;
}

function javaExeName(platform) {
  return platform === "win32" ? "java.exe" : "java";
}

function listSubdirJavas(baseDir, suffixParts) {
  try {
    if (!fs.existsSync(baseDir)) return [];
    return fs
      .readdirSync(baseDir, { withFileTypes: true })
      .filter((entry) => entry.isDirectory() || entry.isSymbolicLink())
      .map((entry) => path.join(baseDir, entry.name, ...suffixParts));
  } catch {
    return [];
  }
}

/**
 * Collects unique candidate `java` binary paths in preference order across OSes.
 */
export function collectJavaCandidates({
  javaBin,
  env = process.env,
  platform = process.platform,
  homedir = os.homedir(),
} = {}) {
  const exe = javaExeName(platform);
  const out = [];
  const seen = new Set();

  function push(candidate) {
    if (!candidate) return;
    const normalized = path.normalize(candidate);
    if (!seen.has(normalized)) {
      seen.add(normalized);
      out.push(normalized);
    }
  }

  push(javaBin);
  push(env.ANDROID_DESIGN_PREVIEW_JAVA);

  if (env.JAVA_HOME) {
    push(path.join(env.JAVA_HOME, "bin", exe));
  }

  const pathVar = env.PATH || "";
  for (const dir of pathVar.split(path.delimiter).filter(Boolean)) {
    push(path.join(dir, exe));
  }

  if (platform === "darwin") {
    push("/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java");
    push("/Applications/Android Studio Preview.app/Contents/jbr/Contents/Home/bin/java");
    push(path.join(homedir, "Applications", "Android Studio.app", "Contents", "jbr", "Contents", "Home", "bin", "java"));
    push(path.join(homedir, "Library", "Application Support", "JetBrains", "Toolbox", "apps", "AndroidStudio", "ch-0"));
    push("/opt/homebrew/opt/openjdk@21/bin/java");
    push("/opt/homebrew/opt/openjdk@17/bin/java");
    push("/opt/homebrew/opt/openjdk/bin/java");
    for (const p of listSubdirJavas("/Library/Java/JavaVirtualMachines", ["Contents", "Home", "bin", "java"])) {
      push(p);
    }
  } else if (platform === "linux") {
    push("/opt/android-studio/jbr/bin/java");
    push("/usr/local/android-studio/jbr/bin/java");
    push(path.join(homedir, "android-studio", "jbr", "bin", "java"));
    for (const p of listSubdirJavas("/usr/lib/jvm", ["bin", "java"])) {
      push(p);
    }
  } else if (platform === "win32") {
    const programFiles = env.ProgramFiles || "C:\\Program Files";
    const localAppData = env.LOCALAPPDATA || path.join(homedir, "AppData", "Local");
    push(path.join(programFiles, "Android", "Android Studio", "jbr", "bin", "java.exe"));
    push(path.join(localAppData, "Programs", "Android Studio", "jbr", "bin", "java.exe"));
  }

  return out;
}

async function defaultProbeVersion(binPath) {
  const { stdout, stderr } = await execFileAsync(binPath, ["-version"], {
    timeout: 5_000,
    windowsHide: true,
  });
  return `${stderr || ""}\n${stdout || ""}`;
}

/**
 * Finds the first usable Java 17+ executable, or throws a clear diagnostic error.
 */
export async function locateJava(options = {}) {
  const minVersion = options.minVersion ?? MIN_JAVA_VERSION;
  const exists = options.existsSync ?? fs.existsSync;
  const probe = options.probeVersion ?? defaultProbeVersion;
  const candidates = collectJavaCandidates(options);
  const rejected = [];

  for (const candidate of candidates) {
    if (!exists(candidate)) continue;
    try {
      const versionOutput = await probe(candidate);
      const major = parseJavaMajorVersion(versionOutput);
      if (major != null && major >= minVersion) {
        return { bin: candidate, major };
      }
      rejected.push(`${candidate} (version ${major ?? "unknown"} < ${minVersion})`);
    } catch (err) {
      rejected.push(`${candidate} (${err.message})`);
    }
  }

  const detail = rejected.length > 0 ? ` Rejected: ${rejected.join("; ")}` : "";
  throw new Error(
    `Java ${minVersion}+ is required to run android-design-preview. Install JDK 17+ or Android Studio (bundled JBR).${detail}`,
  );
}
