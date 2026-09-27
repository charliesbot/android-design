"""Writes report.html for one eval run: scores per case and renders beside the committed baseline."""
import html
from pathlib import Path

PRIMARY = {"phone": "light", "wear": "large", "widget": "light-4x2"}


def score(items):
    passed = sum(1 for v in items.values() if v["pass"])
    return passed, len(items)


def failures(items):
    rows = [f"<li><b>{html.escape(k)}</b>: {html.escape(v.get('detail') or v.get('reason') or '')}</li>"
            for k, v in items.items() if not v["pass"]]
    return f"<ul>{''.join(rows)}</ul>" if rows else "<p class=ok>All passed</p>"


def write_report(out: Path, results, baseline_dir: Path):
    sections = []
    for r in results:
        c_pass, c_total = score(r["checks"])
        j_pass, j_total = score(r["judge"])
        baseline = baseline_dir / f"{r['name']}.jpg"
        base_img = f'<figure><img src="{baseline.as_uri()}"><figcaption>baseline</figcaption></figure>' if baseline.exists() else ""
        shots = "".join(
            f'<figure><img src="{r["name"]}/renders/{html.escape(n)}"><figcaption>{html.escape(Path(n).stem)}</figcaption></figure>'
            for n in sorted(r["renders"], key=lambda n: Path(n).stem != PRIMARY[r["surface"]])
        )
        cost = r["agent"]["cost"]
        sections.append(f"""
<section>
  <h2>{html.escape(r['name'])} <small>{r['surface']} · {r['minutes']} min · agent ${cost if cost is not None else '?'}</small></h2>
  <p class=scores><span>Code checks {c_pass}/{c_total}</span><span>Rubric {j_pass}/{j_total}</span></p>
  <div class=grid><div><h3>Code check failures</h3>{failures(r['checks'])}</div><div><h3>Rubric failures</h3>{failures(r['judge'])}</div></div>
  <div class=shots>{base_img}{shots}</div>
  <details><summary>Agent's final message</summary><pre>{html.escape(r['agent']['message'] or '')}</pre></details>
</section>""")
    page = f"""<!doctype html><html><head><meta charset=utf-8><title>android-design evals</title>
<meta name=viewport content="width=device-width,initial-scale=1">
<style>
:root{{--bg:#fbf8fd;--fg:#1b1b1f;--muted:#5d5e66;--card:#efedf1;--ok:#1b6b3a;--line:#d6d4dc}}
@media (prefers-color-scheme:dark){{:root{{--bg:#121316;--fg:#e4e2e6;--muted:#aaa9b1;--card:#1e1f23;--ok:#7fd89c;--line:#34353a}}}}
body{{background:var(--bg);color:var(--fg);font:15px/1.5 system-ui,sans-serif;margin:0 auto;max-width:1200px;padding:24px 16px}}
section{{background:var(--card);border-radius:16px;padding:16px 20px;margin:0 0 20px}}
h2 small{{color:var(--muted);font-weight:400;font-size:14px}}
.scores span{{margin-right:16px;font-weight:600}} .ok{{color:var(--ok)}}
.grid{{display:grid;grid-template-columns:1fr 1fr;gap:16px}} @media(max-width:700px){{.grid{{grid-template-columns:1fr}}}}
.shots{{display:flex;gap:12px;overflow-x:auto;padding-bottom:8px}}
figure{{margin:0;flex:0 0 auto}} img{{height:420px;border-radius:12px;border:1px solid var(--line)}}
figcaption{{color:var(--muted);font-size:13px;text-align:center}} pre{{white-space:pre-wrap}}
</style></head><body><h1>android-design evals</h1>{''.join(sections)}</body></html>"""
    path = out / "report.html"
    path.write_text(page)
    return path
