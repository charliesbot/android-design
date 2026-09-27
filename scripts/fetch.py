#!/usr/bin/env python3
"""Fetches every official source page into sources/ as Markdown.

Usage: fetch.py [--only m3,blog,design-ui,articles,videos]

Each page becomes one file at sources/<domain>/<route>.md whose first lines are its title and URL.
Output is deterministic (no dates, stable ordering), so `git diff sources/` after a fetch shows
exactly what Google changed. Pages that disappear upstream are deleted locally for the same reason.
Needs only the Python standard library, plus yt-dlp for videos.
"""
import argparse
import concurrent.futures
import json
import re
import shutil
import subprocess
import sys
import tempfile
import urllib.error
import urllib.request
from html.parser import HTMLParser
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SOURCES = ROOT / "sources"
M3 = "https://m3.material.io"
ANDROID = "https://developer.android.com"
# developer.android.com/design/ui platforms outside the skill's scope (see docs/research/source-coverage.md).
OUT_OF_SCOPE = ("/design/ui/ai-glasses", "/design/ui/cars", "/design/ui/tv", "/design/ui/xr")
EXTRA_ANDROID_ROUTES = ["/docs/quality-guidelines/widget-quality"]
ARTICLES = [
    "https://design.google/library/google-sans-flex-font",
    "https://design.google/library/expressive-material-design-google-research",
]
VIDEOS = ["6IsFP3gD28E", "t9rrsqfB2tM", "zRBi6oBtpoo", "HbAFGivZ158", "pLNJ-fNYTKU", "qEEo6AwgBjU"]


def get(url, attempts=3):
    request = urllib.request.Request(url, headers={"User-Agent": "android-design-sources/1.0"})
    for attempt in range(attempts):
        try:
            with urllib.request.urlopen(request, timeout=60) as response:
                return response.read().decode("utf-8", "replace")
        except Exception:
            if attempt == attempts - 1:
                raise


def write_all(folder, pages):
    """Replaces folder with exactly `pages` ({relative path: text}), so removed pages show up as deletions."""
    if folder.exists():
        shutil.rmtree(folder)
    for relative, text in pages.items():
        path = folder / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text.rstrip() + "\n")
    print(f"{folder.relative_to(ROOT)}: {len(pages)} pages")


def fetch_parallel(items, fetch_one, workers=8):
    pages, failures = {}, []
    with concurrent.futures.ThreadPoolExecutor(workers) as pool:
        for item, result in zip(items, pool.map(lambda i: _safe(fetch_one, i), items)):
            if isinstance(result, Exception):
                failures.append(f"{item}: {result}")
            elif result:
                pages[result[0]] = result[1]
    if failures:
        # A partial fetch would look like deleted pages in the diff, so stop instead of writing it.
        sys.exit("fetch failed, nothing written:\n  " + "\n  ".join(failures))
    return pages


def _safe(fn, item):
    try:
        return fn(item)
    except Exception as error:
        return error


class HtmlToMd(HTMLParser):
    """Converts article HTML to plain Markdown: headings, paragraphs, lists, tables, emphasis, image alt text.

    Skips scripts, navigation, and page chrome (devsite marks it with data-nosnippet or the nocontent class).
    """

    SKIP = {"script", "style", "nav", "button", "header", "footer", "svg", "devsite-feedback", "devsite-thumb-rating",
            "devsite-toc", "devsite-feature-tooltip", "devsite-recommendations-sidebar", "devsite-hats-survey",
            "devsite-notification", "devsite-actions", "devsite-bookmark"}
    VOID = {"area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "source", "track", "wbr"}

    def __init__(self, heading_offset=0):
        super().__init__(convert_charrefs=True)
        self.out, self.depth, self.offset, self.pre = [], 0, heading_offset, 0
        self.skip_tag, self.skip_nesting = None, 0

    def is_chrome(self, tag, attrs):
        return (tag in self.SKIP or "data-nosnippet" in attrs
                or "nocontent" in (attrs.get("class") or "").split())

    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if self.skip_tag:
            if tag == self.skip_tag:
                self.skip_nesting += 1
            return
        if tag not in self.VOID and self.is_chrome(tag, attrs):
            self.skip_tag, self.skip_nesting = tag, 1
            return
        if re.fullmatch(r"h[1-6]", tag):
            self.out.append("\n\n" + "#" * min(6, int(tag[1]) + self.offset) + " ")
        elif tag in ("p", "div", "figcaption", "tr", "section"):
            if self.depth and self.out and self.out[-1].endswith("- "):
                return  # the first block inside a list item stays on the bullet's line
            self.out.append("\n\n" if tag == "p" and not self.depth else "\n")
        elif tag in ("ul", "ol"):
            self.depth += 1
        elif tag == "li":
            self.out.append("\n" + "  " * max(0, self.depth - 1) + "- ")
        elif tag in ("td", "th"):
            self.out.append(" | ")
        elif tag == "br":
            self.out.append("\n")
        elif tag == "pre":
            self.pre += 1
            self.out.append("\n```\n")
        elif tag in ("strong", "b"):
            self.out.append("**")
        elif tag in ("em", "i"):
            self.out.append("_")
        elif tag == "code" and not self.pre:
            self.out.append("`")
        elif tag == "img" and attrs.get("alt"):
            self.out.append(f"\n[IMAGE] {attrs['alt'].strip()}\n")

    def handle_endtag(self, tag):
        if self.skip_tag:
            if tag == self.skip_tag:
                self.skip_nesting -= 1
                if not self.skip_nesting:
                    self.skip_tag = None
            return
        if tag in ("ul", "ol"):
            self.depth = max(0, self.depth - 1)
            self.out.append("\n")
        elif tag == "pre":
            self.pre = max(0, self.pre - 1)
            self.out.append("\n```\n")
        elif tag in ("strong", "b"):
            self.out.append("**")
        elif tag in ("em", "i"):
            self.out.append("_")
        elif tag == "code" and not self.pre:
            self.out.append("`")

    def handle_data(self, data):
        if self.skip_tag:
            return
        data = data.replace("\u00a0", " ")
        # Outside <pre>, HTML whitespace (source indentation, newlines) collapses to single spaces.
        self.out.append(data if self.pre else re.sub(r"\s+", " ", data))


def html_to_md(html, heading_offset=0):
    parser = HtmlToMd(heading_offset)
    parser.feed(html or "")
    text = re.sub(r"\n[ \t]+(?![ \t]*- )", "\n", "".join(parser.out))  # drop stray indentation, keep nested bullets
    text = re.sub(r"[ \t]+\n", "\n", text)
    text = re.sub(r"^#+ *$", "", text, flags=re.M)  # headings left empty by skipped chrome
    text = re.sub(r"^(#+) +", r"\1 ", text, flags=re.M)
    return re.sub(r"\n{3,}", "\n\n", text).strip()


def page(title, url, body):
    return f"# {title.strip()}\nSource: {url}\n\n{body.strip()}"


# m3.material.io: the site's Angular bundle carries the page manifest and content version. Each page's
# content is a JSON document of sections, blocks, and chunks (HTML, code, images, captions).

def m3_bundle():
    index = get(M3 + "/")
    name = re.search(r"main\.[0-9a-f]+\.js", index).group(0)
    bundle = ""
    for candidate in (f"{M3}/static/angular/{name}", f"{M3}/{name}"):
        try:
            bundle = get(candidate)
        except Exception:
            continue
        if "carbonVersion" in bundle:
            break
    version = re.search(r'carbonVersion:"([^"]+)"', bundle).group(1)
    manifests = []
    for match in re.finditer(r"'\{\"v\":\[", bundle):
        data = json.JSONDecoder().raw_decode(bundle[match.start() + 1:])[0]
        manifests += data["v"] + data.get("Z", [])
    return version, manifests


def render_m3(data, slug):
    lines = [f"# {data.get('headerTitle') or data.get('title')}\nSource: {M3}/{slug}"]
    if data.get("description"):
        lines.append(f"> {data['description']}")
    for section in data.get("sections", []):
        if not section.get("isVisible", True):
            continue
        lines.append(f"## {section.get('name') or ''}")
        for block in section.get("contentBlocks", []):
            if block.get("isHidden"):
                continue
            if block.get("title"):
                lines.append(f"### {block['title']}")
            for chunk in block.get("contentChunks", []):
                kind = chunk.get("contentChunkType")
                if chunk.get("htmlValue"):
                    lines.append(html_to_md(chunk["htmlValue"], heading_offset=2))
                if chunk.get("snippetCode"):
                    lines.append(f"```{chunk.get('snippetLanguage') or ''}\n{chunk['snippetCode']}\n```")
                if kind in ("IMAGE", "VIDEO") or chunk.get("imageUrl") or chunk.get("videoUrl"):
                    modifier = chunk.get("captionModifier")
                    alt = (chunk.get("altText") or "").strip()
                    lines.append(f"[{kind}{' ' + modifier if modifier else ''}] {alt}".rstrip())
                if chunk.get("footer"):
                    modifier = chunk.get("captionModifier")
                    lines.append(f"{'**' + modifier + ':** ' if modifier else ''}_{html_to_md(chunk['footer'])}_")
    return "\n\n".join(line for line in lines if line.strip())


def fetch_m3():
    version, manifest = m3_bundle()
    entries = sorted((e for e in manifest if e.get("exportedCarbonFileId") and e.get("slug") not in ("", "search.html")),
                     key=lambda e: e["slug"])

    def one(entry):
        data = json.loads(get(f"{M3}/_dsm/content/m3/{version}/{entry['exportedCarbonFileId']}"))
        return entry["slug"] + ".md", render_m3(data, entry["slug"])

    write_all(SOURCES / "m3.material.io" / "pages", fetch_parallel(entries, one))


def fetch_blog():
    _, manifest = m3_bundle()
    posts = sorted((e for e in manifest if "document_id" in e), key=lambda e: e["slug"])

    def one(post):
        data = json.loads(get(f"{M3}/page-data/Posts/{post['document_id']}.json"))
        parts = []
        if data.get("subtitle"):
            parts.append(f"> {data['subtitle']}")
        if data.get("published_date"):
            parts.append(f"Published: {data['published_date'][:10]}")
        for block in data.get("body_content", []):
            # Posts use <mio-*> tags for media slots and columns; keep the text, drop the tags (media is listed below).
            text = re.sub(r"</?mio-[^>]*>", "", html_to_md(block.get("content") or ""))
            parts.append(re.sub(r"\n{3,}", "\n\n", text).strip())
            for image in block.get("image", []):
                parts.append(f"[IMAGE] {(image.get('a11y_description') or '').strip()}: {(image.get('caption') or '').strip()}")
            for video in block.get("video", []):
                parts.append(f"[VIDEO] {(video.get('caption') or '').strip()}")
        return post["slug"] + ".md", page(data.get("title") or post["slug"], f"{M3}/blog/{post['slug']}", "\n\n".join(parts))

    write_all(SOURCES / "m3.material.io" / "blog", fetch_parallel(posts, one))


# developer.android.com: the design/ui navigation is only rendered into hub pages, and the sitemaps list a
# fraction of the guides, so routes come from crawling in-page links outward from the section hubs.

HUBS = ["/design/ui", "/design/ui/mobile", "/design/ui/mobile/guides/foundations/system-bars", "/design/ui/large-screens",
        "/design/ui/desktop", "/design/ui/mobile/guides/widgets", "/design/ui/gallery", "/design/ui/wear",
        "/design/ui/wear/guides/get-started"]


def in_scope(route):
    return route.startswith("/design/ui") and not route.startswith(OUT_OF_SCOPE) and "/reference/" not in route


def crawl_design_ui():
    """Returns {route: html} for every in-scope page reachable from the hubs. Dead links are skipped."""
    html, dead, frontier = {}, set(), list(HUBS) + EXTRA_ANDROID_ROUTES

    def one(route):
        try:
            return route, get(ANDROID + route)
        except urllib.error.HTTPError as error:
            if error.code != 404 or route in HUBS:
                raise
            dead.add(route)
            return None

    while frontier:
        fetched = fetch_parallel(frontier, one, workers=10)
        html.update(fetched)
        links = set()
        for page_html in fetched.values():
            for link in re.findall(r'href="(?:https://developer\.android\.com)?(/design/ui[^"#?]*)"', page_html):
                links.add(link.rstrip("/") or link)
        frontier = sorted(link for link in links if in_scope(link) and link not in html and link not in dead)
    return html


def fetch_design_ui():
    pages = {}
    for route, html in sorted(crawl_design_ui().items()):
        # Guides wrap their text in the article body; hub and landing pages only have <main>.
        body = (re.search(r'<div class="devsite-article-body[^"]*"[^>]*>(.*?)<devsite-content-footer', html, re.S)
                or re.search(r"<main[^>]*>(.*?)</main>", html, re.S))
        title = (re.search(r'<h1[^>]*class="devsite-page-title"[^>]*>(.*?)</h1>', html, re.S)
                 or re.search(r"<title[^>]*>(.*?)</title>", html, re.S))
        title = html_to_md(title.group(1)).split(" | ")[0] if title else route.rsplit("/", 1)[-1]
        pages[route.lstrip("/") + ".md"] = page(title, ANDROID + route, html_to_md(body.group(1) if body else html))
    write_all(SOURCES / "developer.android.com", pages)


def fetch_articles():
    def one(url):
        html = get(url)
        main = re.search(r"<main[^>]*>(.*?)</main>", html, re.S)
        title = re.search(r"<h1[^>]*>(.*?)</h1>", html, re.S) or re.search(r"<title[^>]*>(.*?)</title>", html, re.S)
        title = html_to_md(title.group(1)).split(" - ")[0] if title else url
        return url.split("design.google/", 1)[1] + ".md", page(title, url, html_to_md(main.group(1) if main else html))

    write_all(SOURCES / "design.google", fetch_parallel(ARTICLES, one, workers=2))


def fetch_videos():
    if not shutil.which("yt-dlp"):
        sys.exit("videos need yt-dlp (brew install yt-dlp)")

    def one(video_id):
        url = f"https://www.youtube.com/watch?v={video_id}"
        with tempfile.TemporaryDirectory() as tmp:
            title = subprocess.run(
                ["yt-dlp", "--skip-download", "--write-subs", "--write-auto-subs", "--sub-langs", "en", "--sub-format", "vtt",
                 "--print", "%(title)s", "--no-simulate", "-o", f"{tmp}/%(id)s.%(ext)s", url],
                capture_output=True, text=True, check=True).stdout.strip().splitlines()[0]
            vtt = next(Path(tmp).glob("*.vtt")).read_text()
        spoken = []
        for line in vtt.splitlines():
            line = re.sub(r"<[^>]+>", "", line).strip()
            if not line or "-->" in line or line in ("WEBVTT",) or re.match(r"^(Kind|Language):", line):
                continue
            if not spoken or spoken[-1] != line:
                spoken.append(line)
        return video_id + ".md", page(title, url, "\n".join(spoken))

    write_all(SOURCES / "youtube.com", fetch_parallel(VIDEOS, one, workers=3))


FETCHERS = {"m3": fetch_m3, "blog": fetch_blog, "design-ui": fetch_design_ui, "articles": fetch_articles, "videos": fetch_videos}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--only", help="comma-separated subset of: " + ", ".join(FETCHERS))
    args = parser.parse_args()
    names = args.only.split(",") if args.only else list(FETCHERS)
    for name in names:
        FETCHERS[name]()


if __name__ == "__main__":
    main()
