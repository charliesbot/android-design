"""Checks the Wear Compose sync script without touching the network. Run: python3 -m unittest discover scripts"""
import io
import tempfile
import unittest
import zipfile
from pathlib import Path

import sync_wear_compose as sync

VALUES = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <plurals name="hours"><item quantity="one">%d Hour</item><item quantity="other">%d Hours</item></plurals>
    <string name="zeta">Costs $5 \\"now\\"</string>
    <string name="alpha" description="x">%1$s, %2$d</string>
</resources>
"""


def make_zip(files):
    buffer = io.BytesIO()
    with zipfile.ZipFile(buffer, "w") as archive:
        for name, content in files.items():
            archive.writestr(name, content)
    return buffer.getvalue()


class GenerateRKtTest(unittest.TestCase):
    def setUp(self):
        self.source = sync.generate_r_kt(VALUES, ["check_anim"], "9.9.9")

    def test_ids_are_sorted_and_sequential_per_kind(self):
        self.assertIn("const val check_anim = 1", self.source)
        self.assertIn("const val hours = 2", self.source)
        self.assertIn("const val alpha = 3", self.source)
        self.assertIn("const val zeta = 4", self.source)

    def test_strings_are_escaped_for_kotlin(self):
        self.assertIn('R.string.alpha to "%1\\$s, %2\\$d"', self.source)
        self.assertIn('R.string.zeta to "Costs \\$5 \\"now\\""', self.source)

    def test_plurals_keep_one_and_other(self):
        self.assertIn('R.plurals.hours to ("%d Hour" to "%d Hours")', self.source)

    def test_output_is_deterministic(self):
        self.assertEqual(self.source, sync.generate_r_kt(VALUES, ["check_anim"], "9.9.9"))


class ExtractSourcesTest(unittest.TestCase):
    def test_replaces_the_directory_and_drops_meta_inf(self):
        with tempfile.TemporaryDirectory() as tmp:
            destination = Path(tmp) / "foundation"
            destination.mkdir()
            (destination / "stale.kt").write_text("old")
            jar = make_zip({"META-INF/MANIFEST.MF": "x", "a/b/C.kt": "class C", "a/": ""})
            sync.extract_sources(jar, destination)
            self.assertEqual(["a/b/C.kt"], [str(p.relative_to(destination)) for p in destination.rglob("*.kt")])
            self.assertFalse((destination / "META-INF").exists())
            self.assertEqual(["foundation"], [p.name for p in Path(tmp).iterdir()])

    def test_rejects_paths_that_escape(self):
        with tempfile.TemporaryDirectory() as tmp:
            with self.assertRaises(ValueError):
                sync.extract_sources(make_zip({"../evil.kt": "x"}), Path(tmp) / "foundation")


class SyncTest(unittest.TestCase):
    def test_writes_every_artifact_the_version_and_r_kt(self):
        aar = make_zip({"res/values/values.xml": VALUES, "res/drawable/check_anim.xml": "<vector/>"})
        requested = []

        def fetch(url):
            requested.append(url)
            return aar if url.endswith(".aar") else make_zip({"p/F.kt": url})

        with tempfile.TemporaryDirectory() as tmp:
            upstream = Path(tmp) / "upstream"
            generated = Path(tmp) / "gen/R.kt"
            sync.sync("9.9.9", fetch, upstream, generated)
            self.assertEqual(sorted(sync.ARTIFACTS.values()), sorted(p.name for p in upstream.iterdir() if p.is_dir()))
            self.assertEqual("9.9.9\n", (upstream / "VERSION").read_text())
            self.assertIn("object WearStrings", generated.read_text())
        self.assertEqual(5, len(requested))
        self.assertTrue(all("/9.9.9/" in url for url in requested))


if __name__ == "__main__":
    unittest.main()
