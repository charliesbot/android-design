#!/usr/bin/env python3
"""Lists the public names of the pinned Material 3 Compose artifact and regenerates the type scale.

Usage: material3_inventory.py

Reads the material3 version from the exploration starter's libs.versions.toml, finds the matching AAR in the
local Gradle cache, and writes:

- skills/android-design-explore/references/material3-inventory.txt: one name per line (`Class`, `Class.member`, or a bare
  top-level function name), no signatures. compose-mapping.md may only use names that appear here.
- skills/android-design-explore/gallery/runtime-src/type-scale.json: sizes, line heights, tracking, and weights read from
  androidx.compose.material3.tokens.TypeScaleTokens.

Needs `javap` from a JDK. Build the exploration starter once so Gradle downloads the artifacts.
"""
import json
import re
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LIBS_TOML = ROOT / "evals/compose-starter/gradle/libs.versions.toml"
INVENTORY = ROOT / "skills/android-design-explore/references/material3-inventory.txt"
TYPE_SCALE = ROOT / "skills/android-design-explore/gallery/runtime-src/type-scale.json"
GRADLE_FILES = Path.home() / ".gradle/caches/modules-2/files-2.1"

MATERIAL3_ROOT = "androidx/compose/material3/"
TYPE_SCALE_CLASS = "androidx.compose.material3.tokens.TypeScaleTokens"
COMPOSER_PARAMS = "androidx.compose.runtime.Composer, int"
ROLE_ORDER = ["Display", "Headline", "Title", "Body", "Label"]
SIZE_ORDER = ["Large", "Medium", "Small"]
NOISE = {"<init>", "<clinit>", "equals", "hashCode", "toString", "values", "valueOf"}
WEIGHTS = {"getNormal": 400, "getMedium": 500, "getBold": 700}


class MissingArtifact(Exception):
    pass


def material3_version(toml_text):
    match = re.search(r'^material3\s*=\s*"([^"]+)"', toml_text, re.M)
    if not match:
        raise MissingArtifact(f"No material3 version in {LIBS_TOML}")
    return match.group(1)


def version_key(version):
    return [int(part) if part.isdigit() else part for part in re.split(r"[.\-]", version)]


def find_aar(group, artifact, version=None):
    """Finds the binary AAR of an artifact in the Gradle cache; without a version, the newest cached one."""
    base = GRADLE_FILES / group / artifact
    versions = [version] if version else sorted((p.name for p in base.glob("*")), key=version_key, reverse=True) if base.exists() else []
    for candidate in versions:
        aars = [p for p in (base / candidate).glob("*/*.aar")]
        if aars:
            return candidate, aars[0]
    return None, None


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
        member = member_name(name, params, metadata_names, top_level=True)
        if member:
            entries.add(member)
    return entries


def member_name(name, params, metadata_names, top_level=False):
    name = strip_mangling(name)
    if "$" in name or "." in name or name in NOISE or re.match(r"component\d+$", name):
        return None  # "." is a constructor, which javap prints under the class's full name
    if name.startswith("set") and name[3:4].isupper():
        return None
    if re.match(r"get[A-Z]", name) and params in ("", COMPOSER_PARAMS):
        return property_name(name, metadata_names)
    if top_level and name.startswith("is") and params == "" and name[2:3].isupper():
        return name
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


def extract_classes_jar(aar, target):
    with zipfile.ZipFile(aar) as archive:
        archive.extract("classes.jar", target)
    return zipfile.ZipFile(Path(target) / "classes.jar")


def extract_type_scale(classes_dir, version):
    code = run_javap(["-p", "-c", "-cp", str(classes_dir), TYPE_SCALE_CLASS])
    methods = {}
    current = None
    for line in code.splitlines():
        header = re.match(r"^  public final .* get(\w+?)(Size|LineHeight|Tracking|Weight)(?:-\w+)?\(\);", line)
        if header:
            current = (header.group(1), header.group(2).lower())
            methods[current] = []
        elif current and re.match(r"^\s+\d+:", line):
            methods[current].append(line.split(":", 1)[1].replace("//", " ").split())
    roles = {}
    for (role, kind), instructions in methods.items():
        roles.setdefault(role, {})[kind] = decode(kind, instructions)
    ordered = sorted(roles, key=role_sort_key)
    return {"source": f"androidx.compose.material3:material3-android:{version}, {TYPE_SCALE_CLASS}",
            "roles": {role: {key: roles[role][key] for key in ("size", "lineheight", "tracking", "weight")} for role in ordered}}


def role_sort_key(role):
    family = next(i for i, name in enumerate(ROLE_ORDER) if role.startswith(name))
    size = next(i for i, name in enumerate(SIZE_ORDER) if role[len(ROLE_ORDER[family]):].startswith(name))
    return family, size, role.endswith("Emphasized")


def decode(kind, instructions):
    ops = [op for op in instructions if op]
    if kind == "weight":
        return next(WEIGHTS[op[-1].split(".")[-1].split(":")[0]] for op in ops if op[0] == "invokevirtual" and "FontWeight$Companion" in " ".join(op))
    # The value is pushed right before the TextUnitKt.getSp call.
    index = next(i for i, op in enumerate(ops) if op[0] == "invokestatic" and "TextUnitKt.getSp" in " ".join(op))
    push = ops[index - 1]
    value = literal(push)
    if kind == "tracking" and any(op[0] == "fneg" for op in ops):
        value = -value
    return value


def literal(op):
    name = op[0]
    if name.startswith("iconst_"):
        return float(name.split("_")[1].replace("m", "-"))
    if name in ("bipush", "sipush"):
        return float(op[1])
    if name == "dconst_0":
        return 0.0
    if name == "dconst_1":
        return 1.0
    if name in ("ldc", "ldc2_w"):
        raw = op[-1]
        return float(raw.rstrip("df"))
    raise ValueError(f"Unhandled type scale instruction: {' '.join(op)}")


def main():
    version = material3_version(LIBS_TOML.read_text())
    _, aar = find_aar("androidx.compose.material3", "material3-android", version)
    if not aar:
        sys.exit(f"material3-android {version} is not in {GRADLE_FILES}. Build the exploration starter once so Gradle downloads it.")

    with tempfile.TemporaryDirectory() as tmp:
        tmp = Path(tmp)
        jar = extract_classes_jar(aar, tmp / "material3")
        wanted = [name for name in material3_class_names(jar)] + [TYPE_SCALE_CLASS]
        with jar:
            jar.extractall(tmp / "material3" / "classes", [n.replace(".", "/") + ".class" for n in wanted])
        classes = tmp / "material3" / "classes"
        entries = material3_entries(classes, [n for n in wanted if n != TYPE_SCALE_CLASS])
        scale = extract_type_scale(classes, version)

    header = f"# androidx.compose.material3:material3-android:{version}"
    lines = [header, *sorted(entries)]
    INVENTORY.write_text("\n".join(lines) + "\n")
    TYPE_SCALE.write_text(json.dumps(scale, indent=1) + "\n")

    print(f"{INVENTORY.relative_to(ROOT)}: {len(entries)} material3 names from {aar.name} {version}")


if __name__ == "__main__":
    main()
