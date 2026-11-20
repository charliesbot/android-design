"""Packages the previewer JAR into one release artifact per OS and CPU.

The fat JAR from `./gradlew :app:workerJar` carries Skiko's native library for six platforms and
compresses each class file on its own. Each artifact keeps only its platform's Skiko library.
macOS and Linux artifacts store the JAR uncompressed inside a `.tar.xz`, so xz compresses the
whole archive as one stream and the download is about a third of the fat JAR. Windows artifacts
stay plain deflated JARs, because Windows' built-in tools cannot be relied on to unpack xz.

Usage: python3 scripts/package_release.py <android-design-preview.jar> <out-dir>
"""

import hashlib
import io
import shutil
import subprocess
import sys
import tarfile
import zipfile
from pathlib import Path

JAR_NAME = "android-design-preview.jar"

# Target name -> Skiko native library bundled for it.
TARGETS = {
    "macos-arm64": "libskiko-macos-arm64.dylib",
    "macos-x64": "libskiko-macos-x64.dylib",
    "linux-arm64": "libskiko-linux-arm64.so",
    "linux-x64": "libskiko-linux-x64.so",
    "windows-arm64": "skiko-windows-arm64.dll",
    "windows-x64": "skiko-windows-x64.dll",
}

ALL_SKIKO = set(TARGETS.values())


def artifact_name(target):
    if target.startswith("windows-"):
        return f"android-design-preview-{target}.jar"
    return f"android-design-preview-{target}.tar.xz"


def _skiko_library(entry):
    """Returns the Skiko library an entry belongs to (the library or its `.sha256`), else None."""
    name = entry[: -len(".sha256")] if entry.endswith(".sha256") else entry
    return name if name in ALL_SKIKO else None


def _write_jar(source, dest, keep_skiko, compression):
    with zipfile.ZipFile(source) as src, zipfile.ZipFile(dest, "w", compression) as out:
        for info in src.infolist():
            library = _skiko_library(info.filename)
            if library is not None and library != keep_skiko:
                continue
            copy = zipfile.ZipInfo(info.filename, date_time=info.date_time)
            copy.external_attr = info.external_attr
            copy.compress_type = compression
            out.writestr(copy, src.read(info))


def _write_tar_xz(jar, dest):
    tar_bytes = io.BytesIO()
    with tarfile.open(fileobj=tar_bytes, mode="w", format=tarfile.USTAR_FORMAT) as tar:
        info = tarfile.TarInfo(JAR_NAME)
        info.size = jar.stat().st_size
        info.mode = 0o644
        with jar.open("rb") as handle:
            tar.addfile(info, handle)
    # The xz CLI compresses on all cores; Python's lzma would take minutes per target.
    with dest.open("wb") as handle:
        subprocess.run(["xz", "-9e", "-T0", "-c"], input=tar_bytes.getvalue(), stdout=handle, check=True)


def _write_checksum(artifact):
    digest = hashlib.sha256(artifact.read_bytes()).hexdigest()
    (artifact.parent / (artifact.name + ".sha256")).write_text(f"{digest}  {artifact.name}\n")


def package(fat_jar, out_dir):
    fat_jar, out_dir = Path(fat_jar), Path(out_dir)
    with zipfile.ZipFile(fat_jar) as jar:
        names = set(jar.namelist())
    missing = sorted(ALL_SKIKO - names)
    if missing:
        sys.exit(f"{fat_jar} is missing Skiko libraries: {', '.join(missing)}")
    if shutil.which("xz") is None:
        sys.exit("xz is required to package the macOS and Linux artifacts")

    out_dir.mkdir(parents=True, exist_ok=True)
    work = out_dir / ".work"
    work.mkdir(exist_ok=True)
    try:
        for target, skiko in TARGETS.items():
            artifact = out_dir / artifact_name(target)
            if target.startswith("windows-"):
                _write_jar(fat_jar, artifact, skiko, zipfile.ZIP_DEFLATED)
            else:
                stored = work / f"{target}.jar"
                _write_jar(fat_jar, stored, skiko, zipfile.ZIP_STORED)
                _write_tar_xz(stored, artifact)
                stored.unlink()
            _write_checksum(artifact)
    finally:
        shutil.rmtree(work, ignore_errors=True)


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    package(sys.argv[1], sys.argv[2])
