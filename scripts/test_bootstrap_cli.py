"""Tests for the Gradle-bootstrapped android-design-preview POSIX shell launcher."""

import json
import os
import pathlib
import re
import stat
import subprocess
import tempfile
import unittest


REPO_ROOT = pathlib.Path(__file__).resolve().parent.parent
BIN_WRAPPER = REPO_ROOT / "plugin" / "bin" / "android-design-preview"
SKILL_SCRIPT = (
    REPO_ROOT
    / "plugin"
    / "skills"
    / "android-design-explore"
    / "scripts"
    / "android-design-preview"
)
SCRIPT_VERSION = re.search(
  r'^VERSION="([^"]+)"', SKILL_SCRIPT.read_text(encoding="utf-8"), re.MULTILINE
).group(1)


def _write_executable(path: pathlib.Path, content: str) -> None:
  path.parent.mkdir(parents=True, exist_ok=True)
  path.write_text(content, encoding="utf-8")
  path.chmod(path.stat().st_mode | stat.S_IXUSR | stat.S_IXGRP | stat.S_IXOTH)


def _make_fake_java(bin_dir: pathlib.Path, capture_file: pathlib.Path) -> pathlib.Path:
  java_bin = bin_dir / "java"
  _write_executable(
      java_bin,
      f"""#!/bin/sh
if [ "${{1:-}}" = "-version" ]; then
  echo 'openjdk version "21.0.2" 2024-01-16' 1>&2
  exit 0
fi
{{
  printf 'ARGS=%s\\n' "$*"
  printf 'BOOTSTRAP_DIR=%s\\n' "${{ANDROID_DESIGN_PREVIEW_BOOTSTRAP_DIR:-}}"
  printf 'SERVER_CP=%s\\n' "${{ANDROID_DESIGN_PREVIEW_SERVER_CP:-}}"
  printf 'CLIENT_CP=%s\\n' "${{ANDROID_DESIGN_PREVIEW_CLIENT_CP:-}}"
  printf 'ANDROID_HOME=%s\\n' "${{ANDROID_HOME:-}}"
  printf 'CACHE_DIR=%s\\n' "${{ANDROID_DESIGN_CACHE_DIR:-}}"
  printf 'JAVA_HOME=%s\\n' "${{JAVA_HOME:-}}"
}} > "{capture_file}"
exit 0
""",
  )
  return java_bin


def _make_fake_jdk(
    jdk_dir: pathlib.Path, version: str, capture_file: pathlib.Path
) -> pathlib.Path:
  java_bin = _make_fake_java(jdk_dir / "bin", capture_file)
  text = java_bin.read_text(encoding="utf-8").replace("21.0.2", version)
  java_bin.write_text(text, encoding="utf-8")
  _write_executable(jdk_dir / "bin" / "javac", "#!/bin/sh\nexit 0\n")
  (jdk_dir / "release").write_text(f'JAVA_VERSION="{version}"\n', encoding="utf-8")
  return java_bin


def _make_fake_sdk(root: pathlib.Path) -> pathlib.Path:
  sdk = root / "android-sdk"
  (sdk / "platforms" / "android-35").mkdir(parents=True, exist_ok=True)
  (sdk / "platform-tools").mkdir(parents=True, exist_ok=True)
  return sdk


def _populate_bootstrap(app_dir: pathlib.Path, include_jars: bool = True) -> pathlib.Path:
  bdir = app_dir / "host" / "build" / "bootstrap"
  bdir.mkdir(parents=True, exist_ok=True)
  jar1 = bdir / "dep1.jar"
  jar2 = bdir / "dep2.jar"
  jar1.write_text("jar1", encoding="utf-8")
  if include_jars:
    jar2.write_text("jar2", encoding="utf-8")
  apk = bdir / "host-preview.apk"
  apk.write_text("apk", encoding="utf-8")
  bundle = bdir / "androidx-compile-bundle.jar"
  bundle.write_text("bundle", encoding="utf-8")
  (bdir / "bootstrap-meta.properties").write_text(
      f"apkPath={apk}\napkSha256=deadbeef\n", encoding="utf-8"
  )
  (bdir / "bootstrap-paths.txt").write_text(
      f"{apk}\n{bundle}\n", encoding="utf-8"
  )
  cp_text = f"-cp\n{jar1}:{jar2}\n"
  (bdir / "client.cp").write_text(cp_text, encoding="utf-8")
  (bdir / "server.cp").write_text(cp_text, encoding="utf-8")
  return bdir


def _make_fake_app_dir(root: pathlib.Path, name: str = "fake-app") -> tuple[pathlib.Path, pathlib.Path]:
  app_dir = root / name
  app_dir.mkdir(parents=True, exist_ok=True)
  (app_dir / "settings.gradle.kts").write_text('rootProject.name = "test"\n', encoding="utf-8")
  marker = root / f"{name}_gradle_marker"
  _write_executable(
      app_dir / "gradlew",
      f"""#!/bin/sh
set -eu
printf '%s\\n' "$*" > "{marker}"
printf '%s\\n' "${{JAVA_HOME:-}}" > "{marker}.javahome"
printf 'run\\n' >> "{marker}.runs"
sleep "${{FAKE_GRADLE_SLEEP:-0}}"
bdir="$(pwd)/host/build/bootstrap"
mkdir -p "$bdir"
printf 'jar1' > "$bdir/dep1.jar"
printf 'jar2' > "$bdir/dep2.jar"
printf 'apk' > "$bdir/host-preview.apk"
printf 'bundle' > "$bdir/androidx-compile-bundle.jar"
printf 'apkPath=%s\\napkSha256=deadbeef\\n' "$bdir/host-preview.apk" > "$bdir/bootstrap-meta.properties"
printf '%s\\n%s\\n' "$bdir/host-preview.apk" "$bdir/androidx-compile-bundle.jar" > "$bdir/bootstrap-paths.txt"
printf -- '-cp\\n%s:%s\\n' "$bdir/dep1.jar" "$bdir/dep2.jar" > "$bdir/client.cp"
printf -- '-cp\\n%s:%s\\n' "$bdir/dep1.jar" "$bdir/dep2.jar" > "$bdir/server.cp"
""",
  )
  return app_dir, marker


class BootstrapCliTest(unittest.TestCase):

  def _base_env(self, tmp: pathlib.Path, capture_file: pathlib.Path) -> dict[str, str]:
    java_bin = _make_fake_java(tmp / "bin", capture_file)
    sdk_dir = _make_fake_sdk(tmp)
    env = {
        "PATH": f"{tmp / 'bin'}:/usr/bin:/bin",
        "HOME": str(tmp / "home"),
        "ANDROID_DESIGN_PREVIEW_JAVA": str(java_bin),
        "ANDROID_HOME": str(sdk_dir),
        "ANDROID_SDK_ROOT": str(sdk_dir),
        "ANDROID_DESIGN_CACHE_DIR": str(tmp / "cache"),
    }
    (tmp / "home").mkdir(parents=True, exist_ok=True)
    return env

  def test_no_skiko_tarball_or_curl_downloads_remain(self) -> None:
    text = SKILL_SCRIPT.read_text(encoding="utf-8")
    for forbidden in (
        ".tar.xz",
        "curl",
        "wget",
        "release_target",
        "archive_url",
        "ANDROID_DESIGN_PREVIEW_JAR",
    ):
      self.assertNotIn(forbidden, text)

  def test_warm_cache_skips_gradle_and_selects_client_or_server_cp(self) -> None:
    with tempfile.TemporaryDirectory() as td:
      tmp = pathlib.Path(td)
      capture = tmp / "java_capture.txt"
      env = self._base_env(tmp, capture)
      app_dir, marker = _make_fake_app_dir(tmp)
      _populate_bootstrap(app_dir)
      env["ANDROID_DESIGN_PREVIEW_SRC"] = str(app_dir)

      client_res = subprocess.run(
          [str(BIN_WRAPPER), "status", "--json"],
          env=env,
          capture_output=True,
          text=True,
      )
      self.assertEqual(client_res.returncode, 0, client_res.stderr)
      self.assertFalse(marker.exists(), "Warm cache should not invoke gradlew")
      captured = capture.read_text(encoding="utf-8")
      resolved_bdir = (app_dir / "host" / "build" / "bootstrap").resolve()
      self.assertIn("-XX:+TieredCompilation -XX:TieredStopAtLevel=1", captured)
      self.assertIn(f"@{resolved_bdir / 'client.cp'}", captured)
      self.assertIn(f"BOOTSTRAP_DIR={resolved_bdir}", captured)

      server_res = subprocess.run(
          [str(SKILL_SCRIPT), "server", "--repo", str(tmp)],
          env=env,
          capture_output=True,
          text=True,
      )
      self.assertEqual(server_res.returncode, 0, server_res.stderr)
      self.assertFalse(marker.exists(), "Warm cache should not invoke gradlew for server")
      captured_srv = capture.read_text(encoding="utf-8")
      self.assertNotIn("TieredStopAtLevel=1", captured_srv)
      self.assertIn(f"@{resolved_bdir / 'server.cp'}", captured_srv)

  def test_evicted_cp_entry_triggers_gradle_rebootstrap(self) -> None:
    with tempfile.TemporaryDirectory() as td:
      tmp = pathlib.Path(td)
      capture = tmp / "java_capture.txt"
      env = self._base_env(tmp, capture)
      app_dir, marker = _make_fake_app_dir(tmp)
      bdir = _populate_bootstrap(app_dir, include_jars=False)
      env["ANDROID_DESIGN_PREVIEW_SRC"] = str(app_dir)

      res = subprocess.run(
          [str(BIN_WRAPPER), "status"],
          env=env,
          capture_output=True,
          text=True,
      )
      self.assertEqual(res.returncode, 0, res.stderr)
      self.assertTrue(marker.exists(), "Missing dep2.jar should re-run gradlew")
      self.assertIn(":host:exportBootstrap", marker.read_text(encoding="utf-8"))
      self.assertTrue((bdir / "dep2.jar").is_file())

  def test_evicted_bootstrap_path_triggers_gradle_rebootstrap(self) -> None:
    with tempfile.TemporaryDirectory() as td:
      tmp = pathlib.Path(td)
      capture = tmp / "java_capture.txt"
      env = self._base_env(tmp, capture)
      app_dir, marker = _make_fake_app_dir(tmp)
      bdir = _populate_bootstrap(app_dir)
      (bdir / "androidx-compile-bundle.jar").unlink()
      env["ANDROID_DESIGN_PREVIEW_SRC"] = str(app_dir)

      res = subprocess.run(
          [str(BIN_WRAPPER), "status"],
          env=env,
          capture_output=True,
          text=True,
      )
      self.assertEqual(res.returncode, 0, res.stderr)
      self.assertTrue(marker.exists(), "Deleted bundle jar should trigger gradlew")
      self.assertTrue((bdir / "androidx-compile-bundle.jar").is_file())

  def test_shallow_sparse_checkout_fetches_only_app_dir(self) -> None:
    with tempfile.TemporaryDirectory() as td:
      tmp = pathlib.Path(td)
      capture = tmp / "java_capture.txt"
      env = self._base_env(tmp, capture)

      work_src = tmp / "work-src"
      work_src.mkdir()
      app_sub, _ = _make_fake_app_dir(work_src, name="app")
      (work_src / "docs").mkdir()
      (work_src / "docs" / "huge.md").write_text("docs", encoding="utf-8")
      (work_src / "sources").mkdir()
      (work_src / "sources" / "page.md").write_text("source", encoding="utf-8")
      (work_src / "plugin").mkdir()
      (work_src / "plugin" / "skill.md").write_text("skill", encoding="utf-8")

      def git(*args: str, cwd: pathlib.Path = work_src) -> None:
        subprocess.run(["git", *args], cwd=cwd, check=True, capture_output=True)

      git("init", "-b", "main")
      git("config", "user.email", "test@example.com")
      git("config", "user.name", "Tester")
      git("add", ".")
      git("commit", "-m", "initial")
      git("tag", f"v{SCRIPT_VERSION}")

      bare = tmp / "origin.git"
      subprocess.run(
          ["git", "clone", "--bare", str(work_src), str(bare)],
          check=True,
          capture_output=True,
      )

      env["ANDROID_DESIGN_PREVIEW_FORCE_SPARSE"] = "1"
      env["ANDROID_DESIGN_PREVIEW_GIT_URL"] = bare.as_uri()
      env["ANDROID_DESIGN_PREVIEW_GIT_REF"] = f"v{SCRIPT_VERSION}"

      res = subprocess.run(
          [str(SKILL_SCRIPT), "status"],
          env=env,
          capture_output=True,
          text=True,
      )
      self.assertEqual(res.returncode, 0, res.stderr)
      sparse_src = pathlib.Path(env["ANDROID_DESIGN_CACHE_DIR"]) / SCRIPT_VERSION / "src"
      self.assertTrue((sparse_src / "app" / "settings.gradle.kts").is_file())
      self.assertFalse((sparse_src / "docs").exists(), "Non-app docs/ must not be checked out")
      self.assertFalse((sparse_src / "sources").exists(), "Non-app sources/ must not be checked out")
      self.assertFalse((sparse_src / "plugin").exists(), "Non-app plugin/ must not be checked out")
      self.assertTrue((sparse_src / "app" / "host" / "build" / "bootstrap" / "client.cp").is_file())

  def test_missing_java_and_missing_android_sdk_exit_3_json(self) -> None:
    with tempfile.TemporaryDirectory() as td:
      tmp = pathlib.Path(td)
      capture = tmp / "java_capture.txt"

      env_no_java = self._base_env(tmp, capture)
      env_no_java["ANDROID_DESIGN_PREVIEW_JAVA"] = str(tmp / "missing-java")
      res_java = subprocess.run(
          [str(BIN_WRAPPER), "status"],
          env=env_no_java,
          capture_output=True,
          text=True,
      )
      self.assertEqual(res_java.returncode, 3)
      payload_java = json.loads(res_java.stdout)
      self.assertFalse(payload_java["ok"])
      self.assertEqual(payload_java["errors"][0]["code"], "missing_java")

      env_no_sdk = self._base_env(tmp, capture)
      env_no_sdk.pop("ANDROID_HOME", None)
      env_no_sdk.pop("ANDROID_SDK_ROOT", None)
      res_sdk = subprocess.run(
          [str(BIN_WRAPPER), "status"],
          env=env_no_sdk,
          capture_output=True,
          text=True,
      )
      self.assertEqual(res_sdk.returncode, 3)
      payload_sdk = json.loads(res_sdk.stdout)
      self.assertFalse(payload_sdk["ok"])
      self.assertEqual(payload_sdk["errors"][0]["code"], "missing_android_sdk")

      env_bad_src = self._base_env(tmp, capture)
      env_bad_src["ANDROID_DESIGN_PREVIEW_SRC"] = str(tmp / "not-an-app-dir")
      res_src = subprocess.run(
          [str(BIN_WRAPPER), "status"],
          env=env_bad_src,
          capture_output=True,
          text=True,
      )
      self.assertEqual(res_src.returncode, 3)
      payload_src = json.loads(res_src.stdout)
      self.assertFalse(payload_src["ok"])
      self.assertEqual(payload_src["errors"][0]["code"], "invalid_source_dir")

  def test_ignores_macos_usr_bin_java_stub_in_cache_and_never_exports_usr_java_home(self) -> None:
    if not pathlib.Path("/usr/libexec/java_home").is_file():
      self.skipTest("macOS /usr/libexec/java_home stub test only applies on macOS")
    with tempfile.TemporaryDirectory() as td:
      tmp = pathlib.Path(td)
      capture = tmp / "java_capture.txt"
      jdk_dir = tmp / "jdk"
      java_bin = _make_fake_java(jdk_dir / "bin", capture)
      _write_executable(jdk_dir / "bin" / "javac", "#!/bin/sh\nexit 0\n")
      (jdk_dir / "release").write_text('JAVA_VERSION="17.0.20"\n', encoding="utf-8")

      sdk_dir = _make_fake_sdk(tmp)
      app_dir, _ = _make_fake_app_dir(tmp)
      _populate_bootstrap(app_dir)
      cache_dir = tmp / "cache"
      cache_dir.mkdir(parents=True, exist_ok=True)
      (cache_dir / "java-bin").write_text("/usr/bin/java\n", encoding="utf-8")
      (tmp / "home").mkdir(parents=True, exist_ok=True)

      env = {
          "PATH": "/usr/bin:/bin",
          "HOME": str(tmp / "home"),
          "JAVA_HOME": str(jdk_dir),
          "ANDROID_HOME": str(sdk_dir),
          "ANDROID_SDK_ROOT": str(sdk_dir),
          "ANDROID_DESIGN_CACHE_DIR": str(cache_dir),
          "ANDROID_DESIGN_PREVIEW_SRC": str(app_dir),
      }
      res = subprocess.run(
          [str(SKILL_SCRIPT), "status"],
          env=env,
          capture_output=True,
          text=True,
          timeout=15,
      )
      self.assertEqual(res.returncode, 0, res.stderr)
      self.assertTrue(capture.is_file())
      captured = capture.read_text(encoding="utf-8")
      self.assertNotIn("JAVA_HOME=/usr\n", captured)
      cached_java = (cache_dir / "java-bin").read_text(encoding="utf-8").strip()
      self.assertIn(cached_java, {str(java_bin), str(java_bin.resolve())})

  def test_derives_java_home_for_real_jdk_but_never_usr(self) -> None:
    with tempfile.TemporaryDirectory() as td:
      tmp = pathlib.Path(td)
      capture = tmp / "java_capture.txt"
      jdk_dir = tmp / "jdk"
      java_bin = _make_fake_java(jdk_dir / "bin", capture)
      _write_executable(jdk_dir / "bin" / "javac", "#!/bin/sh\nexit 0\n")
      (jdk_dir / "release").write_text('JAVA_VERSION="17.0.20"\n', encoding="utf-8")

      sdk_dir = _make_fake_sdk(tmp)
      app_dir, _ = _make_fake_app_dir(tmp)
      _populate_bootstrap(app_dir)
      (tmp / "home").mkdir(parents=True, exist_ok=True)

      env = {
          "PATH": "/usr/bin:/bin",
          "HOME": str(tmp / "home"),
          "ANDROID_DESIGN_PREVIEW_JAVA": str(java_bin),
          "ANDROID_HOME": str(sdk_dir),
          "ANDROID_SDK_ROOT": str(sdk_dir),
          "ANDROID_DESIGN_CACHE_DIR": str(tmp / "cache"),
          "ANDROID_DESIGN_PREVIEW_SRC": str(app_dir),
      }
      res = subprocess.run(
          [str(SKILL_SCRIPT), "status"],
          env=env,
          capture_output=True,
          text=True,
          timeout=15,
      )
      self.assertEqual(res.returncode, 0, res.stderr)
      captured = capture.read_text(encoding="utf-8")
      self.assertIn(f"JAVA_HOME={jdk_dir.resolve()}\n", captured)

    script_text = SKILL_SCRIPT.read_text(encoding="utf-8")
    self.assertIn('!= "/usr"', script_text)
    self.assertIn('!= "/"', script_text)

  def _old_java_home_env(self, tmp: pathlib.Path) -> tuple[dict[str, str], pathlib.Path, pathlib.Path]:
    capture = tmp / "java_capture.txt"
    old_jdk = tmp / "jdk11"
    new_jdk = tmp / "jdk21"
    _make_fake_jdk(old_jdk, "11.0.2", capture)
    new_java = _make_fake_jdk(new_jdk, "21.0.2", capture)
    env = self._base_env(tmp, capture)
    env["PATH"] = "/usr/bin:/bin"
    env["JAVA_HOME"] = str(old_jdk)
    env["ANDROID_DESIGN_PREVIEW_JAVA"] = str(new_java)
    app_dir, marker = _make_fake_app_dir(tmp)
    env["ANDROID_DESIGN_PREVIEW_SRC"] = str(app_dir)
    return env, new_jdk, marker

  def test_old_java_home_is_replaced_for_gradle(self) -> None:
    with tempfile.TemporaryDirectory() as td:
      tmp = pathlib.Path(td)
      env, new_jdk, marker = self._old_java_home_env(tmp)
      res = subprocess.run([str(SKILL_SCRIPT), "status"], env=env, capture_output=True, text=True, timeout=30)
      self.assertEqual(res.returncode, 0, res.stderr)
      gradle_home = pathlib.Path(str(marker) + ".javahome").read_text(encoding="utf-8").strip()
      self.assertEqual(gradle_home, str(new_jdk.resolve()))

  def test_old_java_home_is_replaced_when_java_bin_is_cached(self) -> None:
    with tempfile.TemporaryDirectory() as td:
      tmp = pathlib.Path(td)
      env, new_jdk, marker = self._old_java_home_env(tmp)
      del env["ANDROID_DESIGN_PREVIEW_JAVA"]
      cache = pathlib.Path(env["ANDROID_DESIGN_CACHE_DIR"])
      cache.mkdir(parents=True)
      (cache / "java-bin").write_text(f"{new_jdk / 'bin' / 'java'}\n", encoding="utf-8")
      res = subprocess.run([str(SKILL_SCRIPT), "status"], env=env, capture_output=True, text=True, timeout=30)
      self.assertEqual(res.returncode, 0, res.stderr)
      gradle_home = pathlib.Path(str(marker) + ".javahome").read_text(encoding="utf-8").strip()
      self.assertEqual(gradle_home, str(new_jdk.resolve()))

  def test_valid_java_home_is_kept(self) -> None:
    with tempfile.TemporaryDirectory() as td:
      tmp = pathlib.Path(td)
      env, new_jdk, marker = self._old_java_home_env(tmp)
      keep = tmp / "jdk17"
      _make_fake_jdk(keep, "17.0.9", tmp / "other_capture.txt")
      env["JAVA_HOME"] = str(keep)
      res = subprocess.run([str(SKILL_SCRIPT), "status"], env=env, capture_output=True, text=True, timeout=30)
      self.assertEqual(res.returncode, 0, res.stderr)
      gradle_home = pathlib.Path(str(marker) + ".javahome").read_text(encoding="utf-8").strip()
      self.assertEqual(gradle_home, str(keep))

  def test_concurrent_first_runs_build_once(self) -> None:
    with tempfile.TemporaryDirectory() as td:
      tmp = pathlib.Path(td)
      capture = tmp / "java_capture.txt"
      env = self._base_env(tmp, capture)
      app_dir, marker = _make_fake_app_dir(tmp)
      env["ANDROID_DESIGN_PREVIEW_SRC"] = str(app_dir)
      env["FAKE_GRADLE_SLEEP"] = "2"
      procs = [
          subprocess.Popen([str(SKILL_SCRIPT), "status"], env=env, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
          for _ in range(2)
      ]
      results = [p.communicate(timeout=60) + (p.returncode,) for p in procs]
      for out, err, rc in results:
        self.assertEqual(rc, 0, err)
      runs = pathlib.Path(str(marker) + ".runs").read_text(encoding="utf-8").splitlines()
      self.assertEqual(len(runs), 1, "Exactly one process should run Gradle")
      lock = pathlib.Path(env["ANDROID_DESIGN_CACHE_DIR"]) / SCRIPT_VERSION / ".lock"
      self.assertFalse(lock.is_symlink() or lock.exists(), "Lock must be released")

  def test_stale_lock_from_dead_process_is_cleaned(self) -> None:
    with tempfile.TemporaryDirectory() as td:
      tmp = pathlib.Path(td)
      capture = tmp / "java_capture.txt"
      env = self._base_env(tmp, capture)
      app_dir, marker = _make_fake_app_dir(tmp)
      env["ANDROID_DESIGN_PREVIEW_SRC"] = str(app_dir)
      dead = subprocess.Popen(["true"])
      dead.wait()
      lock = pathlib.Path(env["ANDROID_DESIGN_CACHE_DIR"]) / SCRIPT_VERSION / ".lock"
      lock.parent.mkdir(parents=True)
      os.symlink(str(dead.pid), lock)
      res = subprocess.run([str(SKILL_SCRIPT), "status"], env=env, capture_output=True, text=True, timeout=30)
      self.assertEqual(res.returncode, 0, res.stderr)
      self.assertTrue(pathlib.Path(str(marker) + ".runs").exists())
      self.assertFalse(lock.is_symlink())

  def test_live_lock_times_out_with_json_error(self) -> None:
    with tempfile.TemporaryDirectory() as td:
      tmp = pathlib.Path(td)
      capture = tmp / "java_capture.txt"
      env = self._base_env(tmp, capture)
      app_dir, _ = _make_fake_app_dir(tmp)
      env["ANDROID_DESIGN_PREVIEW_SRC"] = str(app_dir)
      env["ANDROID_DESIGN_PREVIEW_LOCK_TIMEOUT"] = "1"
      holder = subprocess.Popen(["sleep", "30"])
      try:
        lock = pathlib.Path(env["ANDROID_DESIGN_CACHE_DIR"]) / SCRIPT_VERSION / ".lock"
        lock.parent.mkdir(parents=True)
        os.symlink(str(holder.pid), lock)
        res = subprocess.run([str(SKILL_SCRIPT), "status"], env=env, capture_output=True, text=True, timeout=30)
      finally:
        holder.kill()
        holder.wait()
      self.assertEqual(res.returncode, 3)
      self.assertIn("bootstrap_lock_timeout", res.stdout)


if __name__ == "__main__":
  unittest.main()
