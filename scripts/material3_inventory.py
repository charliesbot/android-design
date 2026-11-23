#!/usr/bin/env python3
"""Lists the public names of the pinned AndroidX Material 3 Compose artifact.

Usage: material3_inventory.py

Reads the androidxMaterial3 version from app/gradle/libs.versions.toml, finds the matching
androidx.compose.material3:material3-android AAR in the local Gradle cache, reads its classes.jar,
and writes scripts/material3-inventory.txt: one name per line (`Class`, `Class.member`, or a bare
top-level function name), no signatures.

Needs `javap` from a JDK. Run `cd app && ./gradlew :host:exportBootstrap` once so Gradle caches it.
"""
import io
import re
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LIBS_TOML = ROOT / "app/gradle/libs.versions.toml"
INVENTORY = ROOT / "scripts/material3-inventory.txt"
GRADLE_FILES = Path.home() / ".gradle/caches/modules-2/files-2.1"

MATERIAL3_ROOT = "androidx/compose/material3/"
COMPOSER_PARAMS = "androidx.compose.runtime.Composer, int"
NOISE = {"<init>", "<clinit>", "equals", "hashCode", "toString", "values", "valueOf"}


class MissingArtifact(Exception):
    pass


def material3_version(toml_text):
    match = re.search(r'^androidxMaterial3\s*=\s*"([^"]+)"', toml_text, re.M)
    if not match:
        raise MissingArtifact(f"No androidxMaterial3 version in {LIBS_TOML}")
    return match.group(1)


def version_key(version):
    return [int(part) if part.isdigit() else part for part in re.split(r"[.\-]", version)]


def find_aar(group, artifact, version=None, gradle_files=GRADLE_FILES):
    """Finds the binary AAR of an artifact in the Gradle cache; without a version, the newest cached one."""
    base = gradle_files / group / artifact
    versions = (
        [version]
        if version
        else sorted((p.name for p in base.glob("*")), key=version_key, reverse=True)
        if base.exists()
        else []
    )
    for candidate in versions:
        aars = [
            p
            for p in (base / candidate).glob("*/*.aar")
            if not p.name.endswith("-sources.aar") and not p.name.endswith("-javadoc.aar")
        ]
        if aars:
            return candidate, aars[0]
    return None, None


def extract_classes_from_aar(aar_path, target_dir):
    """Extracts filtered material3 classes from an Android AAR's classes.jar into [target_dir]."""
    with zipfile.ZipFile(aar_path) as aar:
        classes_bytes = aar.read("classes.jar")
    with zipfile.ZipFile(io.BytesIO(classes_bytes)) as jar:
        wanted = material3_class_names(jar)
        jar.extractall(target_dir, [n.replace(".", "/") + ".class" for n in wanted])
    return wanted


def strip_mangling(name):
    """Removes Kotlin's inline-class mangling suffix: shapesFor-8Feqmps -> shapesFor."""
    return name.split("-", 1)[0]


def property_name(getter, metadata_names):
    """Turns getFoo into the Kotlin property name, using the class metadata to pick Foo or foo."""
    base = getter[3:]
    if base in metadata_names:
        return base
    return base[0].lower() + base[1:]


def parse_javap(text):
    """Yields (class binary name, [(member name, params, is_static, is_field)]) for each class in javap output."""
    classes = []
    current = None
    for line in text.splitlines():
        header = re.match(r"^(?:public |protected )[\w ]*?(?:class|interface|enum|@interface) ([\w.$]+)", line)
        if header and not line.startswith(" "):
            current = (header.group(1), [])
            classes.append(current)
            continue
        if not line.startswith(" ") and line.rstrip().endswith("{"):
            current = None  # a non-public class: its members are not API
            continue
        if current is None or not line.startswith("  ") or not line.rstrip().endswith(";"):
            continue
        body = line.strip().rstrip(";")
        if "(" in body:
            head, rest = body.split("(", 1)
            params = rest.rsplit(")", 1)[0].strip()
            name = head.split()[-1]
            current[1].append((name, params, " static " in f" {head} ", False))
        else:
            current[1].append((body.split()[-1], "", " static " in f" {body} ", True))
    return classes


def kotlin_property_names(verbose_text):
    """Maps each class in `javap -v` output to the strings in its Kotlin metadata (property names among them)."""
    names = {}
    current = None
    for line in verbose_text.splitlines():
        if line.startswith("Classfile "):
            current = None
        match = re.match(r"^(?:public |protected )?[\w ]*?(?:class|interface|enum) ([\w.$]+)", line)
        if match and not line.startswith(" "):
            current = match.group(1)
            names[current] = set()
        elif current and line.strip().startswith("d2=["):
            names[current].update(re.findall(r'"([^"]*)"', line))
    return names


def entries_for_class(binary_name, members, metadata_names):
    """Returns the inventory entries one class contributes."""
    simple = binary_name.rsplit(".", 1)[-1]
    if simple.endswith("Kt"):
        return top_level_entries(members, metadata_names)
    cls = simple.replace("$", ".")
    entries = set()
    if cls.endswith(".Companion"):
        cls = cls.removesuffix(".Companion")  # companion members are used as Outer.member
    else:
        entries.add(cls)
    for name, params, _is_static, is_field in members:
        if is_field:
            if "$" not in name and name not in ("INSTANCE", "Companion"):
                entries.add(f"{cls}.{name}")
            continue
        member = member_name(name, params, metadata_names)
        if member:
            entries.add(f"{cls}.{member}")
    return entries


def top_level_entries(members, metadata_names):
    entries = set()
    for name, params, is_static, is_field in members:
        if is_field or not is_static:
            continue
        member = member_name(name, params, metadata_names)
        if member:
            entries.add(member)
    return entries


def member_name(name, params, metadata_names):
    name = strip_mangling(name)
    if "$" in name or "." in name or name in NOISE or re.match(r"component\d+$", name):
        return None  # "." is a constructor, which javap prints under the class's full name
    if name.startswith("set") and name[3:4].isupper():
        return None
    if re.match(r"get[A-Z]", name) and params in ("", COMPOSER_PARAMS):
        return property_name(name, metadata_names)
    return name


def run_javap(args):
    result = subprocess.run(["javap", *args], capture_output=True, text=True)
    if result.returncode != 0:
        sys.exit(f"javap failed: {result.stderr.strip()}")
    return result.stdout


def batched(items, size=200):
    for start in range(0, len(items), size):
        yield items[start:start + size]


def material3_class_names(jar):
    names = []
    for entry in jar.namelist():
        if not entry.endswith(".class") or not entry.startswith(MATERIAL3_ROOT):
            continue
        path = entry[:-len(".class")]
        parts = path.split("/")
        if "internal" in parts or "tokens" in parts:
            continue
        simple = parts[-1]
        if re.search(r"\$(\d|\$)|WhenMappings|DefaultImpls|\$Companion\$|ComposableSingletons|\$lambda", simple):
            continue
        names.append(path.replace("/", "."))
    return sorted(names)


def material3_entries(classes_dir, class_names):
    entries = set()
    for batch in batched(class_names):
        javap = parse_javap(run_javap(["-public", "-cp", str(classes_dir), *batch]))
        verbose = kotlin_property_names(run_javap(["-v", "-public", "-cp", str(classes_dir), *batch]))
        for binary_name, members in javap:
            simple = binary_name.removeprefix("androidx.compose.material3.")
            if "." in simple:  # a subpackage such as carousel keeps its class name only
                simple = simple.rsplit(".", 1)[-1]
            entries |= entries_for_class(simple, members, verbose.get(binary_name, set()))
    return entries


def main():
    version = material3_version(LIBS_TOML.read_text())
    _, aar_path = find_aar("androidx.compose.material3", "material3-android", version)
    if not aar_path:
        sys.exit(
            f"material3-android {version} is not in {GRADLE_FILES}. Run `cd app && ./gradlew :host:exportBootstrap` once so Gradle caches it."
        )

    with tempfile.TemporaryDirectory() as tmp:
        classes = Path(tmp) / "material3" / "classes"
        wanted = extract_classes_from_aar(aar_path, classes)
        entries = material3_entries(classes, wanted)

    header = f"# androidx.compose.material3:material3-android:{version}"
    lines = [header, *sorted(entries)]
    INVENTORY.write_text("\n".join(lines) + "\n")

    print(f"{INVENTORY.relative_to(ROOT)}: {len(entries)} material3 names from {aar_path.name} {version}")


if __name__ == "__main__":
    main()
