"""Checks that package_release.py builds one compressed previewer artifact per OS and CPU."""

import hashlib
import shutil
import tarfile
import tempfile
import unittest
import zipfile
from pathlib import Path

import package_release

SKIKO = {
    "macos-arm64": "libskiko-macos-arm64.dylib",
    "macos-x64": "libskiko-macos-x64.dylib",
    "linux-arm64": "libskiko-linux-arm64.so",
    "linux-x64": "libskiko-linux-x64.so",
}


@unittest.skipUnless(shutil.which("xz"), "xz is required to package the release artifacts")
class PackageReleaseTest(unittest.TestCase):
    def setUp(self):
        self.tmp = Path(tempfile.mkdtemp())
        self.addCleanup(shutil.rmtree, self.tmp)
        self.jar = self.tmp / "android-design-preview.jar"
        with zipfile.ZipFile(self.jar, "w", zipfile.ZIP_DEFLATED) as jar:
            jar.writestr("META-INF/MANIFEST.MF", "Main-Class: Main\n")
            jar.writestr("bot/Main.class", b"class" * 100)
            jar.writestr("icudtl.dat", b"icu")
            for name in SKIKO.values():
                jar.writestr(name, name.encode())
                jar.writestr(name + ".sha256", "hash")
            # A Windows library that slipped into the fat JAR must not reach any artifact.
            jar.writestr("skiko-windows-x64.dll", b"dll")
        self.out = self.tmp / "dist"
        package_release.package(self.jar, self.out)

    def read_unix_jar(self, target):
        archive = self.out / f"android-design-preview-{target}.tar.xz"
        with tarfile.open(archive, "r:xz") as tar:
            self.assertEqual(tar.getnames(), ["android-design-preview.jar"])
            data = tar.extractfile("android-design-preview.jar").read()
        path = self.tmp / f"{target}.jar"
        path.write_bytes(data)
        return path

    def test_every_target_has_an_artifact_and_a_matching_checksum(self):
        names = sorted(p.name for p in self.out.iterdir())
        expected = []
        for target in package_release.TARGETS:
            artifact = package_release.artifact_name(target)
            expected += [artifact, artifact + ".sha256"]
        self.assertEqual(names, sorted(expected))
        for target in package_release.TARGETS:
            artifact = self.out / package_release.artifact_name(target)
            line = (self.out / (artifact.name + ".sha256")).read_text()
            self.assertEqual(line, f"{hashlib.sha256(artifact.read_bytes()).hexdigest()}  {artifact.name}\n")

    def test_jars_are_stored_and_keep_only_their_own_skiko(self):
        for target in SKIKO:
            with zipfile.ZipFile(self.read_unix_jar(target)) as jar:
                names = set(jar.namelist())
                self.assertIn(SKIKO[target], names)
                self.assertIn(SKIKO[target] + ".sha256", names)
                self.assertEqual({n for n in names if "skiko" in n}, {SKIKO[target], SKIKO[target] + ".sha256"})
                self.assertIn("bot/Main.class", names)
                self.assertIn("icudtl.dat", names)
                self.assertTrue(all(i.compress_type == zipfile.ZIP_STORED for i in jar.infolist()))
                self.assertEqual(jar.read("META-INF/MANIFEST.MF"), b"Main-Class: Main\n")

    def test_rejects_a_jar_missing_a_targets_skiko(self):
        broken = self.tmp / "broken.jar"
        with zipfile.ZipFile(broken, "w") as jar:
            jar.writestr("bot/Main.class", b"x")
        with self.assertRaises(SystemExit):
            package_release.package(broken, self.tmp / "broken-dist")


if __name__ == "__main__":
    unittest.main()
