"""Writes one eval run's results: report.html (scores and renders beside the committed baseline) and summary.md."""
import html
import statistics
from pathlib import Path

# The view a case's baseline image and report lead with, by surface.
PRIMARY = {"phone": "light", "wear": "watch"}

METRIC_KEYS = (
    "backend",
    "model",
    "wall_seconds",
    "turns",
    "input_tokens",
    "output_tokens",
    "thinking_tokens",
    "cache_read_tokens",
    "cache_creation_tokens",
    "cost",
    "check_calls",
    "compile_retries",
    "skill_bytes_read",
    "skill_read_calls",
    "skill_unique_files",
    "skill_files_read",
)
MEDIAN_KEYS = (
    "wall_seconds",
    "turns",
    "input_tokens",
    "output_tokens",
    "check_calls",
    "compile_retries",
    "skill_bytes_read",
)


def case_metrics(result):
    metrics = result.get("generator_metrics")
    if isinstance(metrics, dict):
        return metrics
    agent = result.get("agent") if isinstance(result.get("agent"), dict) else {}
    return {key: agent.get(key) for key in METRIC_KEYS}


def _median(values, round_float=False):
    if not values:
        return None
    mid = statistics.median(values)
    if round_float:
        return round(float(mid), 1)
    if isinstance(mid, float) and mid.is_integer():
        return int(mid)
    return round(float(mid), 1) if isinstance(mid, float) else mid


def generator_medians(results):
    metrics_list = [case_metrics(r) for r in results]
    summary = {}
    for key in MEDIAN_KEYS:
        vals = [m[key] for m in metrics_list if isinstance(m.get(key), (int, float))]
        summary[key] = _median(vals, round_float=(key == "wall_seconds"))
    summary["models"] = sorted({str(m["model"]) for m in metrics_list if m.get("model")})
    return summary


def _fmt(value, suffix=""):
    return "-" if value is None else f"{value}{suffix}"


def score(items):
    passed = sum(1 for v in items.values() if v["pass"] is True)
    return passed, len(items)


def label(render):
    """`screen/view`; `screen+overlay/view` with an overlay open; `(end of list)` for the scrolled list's end."""
    return f"{render.get('screen')}/{render.get('view')}" + (" (end of list)" if render.get("end") else "")


def primary_first(renders, surface):
    """Renders with the surface's primary view first, otherwise in report order."""
    return sorted(renders, key=lambda r: r["view"] != PRIMARY[surface])


def failures(items):
    rows = []
    for key, value in items.items():
        if value["pass"] is True:
            continue
        detail = value.get("detail") or value.get("reason") or ""
        if value["pass"] is None:
            detail = "Pending: " + detail
        rows.append(f"<li><b>{html.escape(key)}</b>: {html.escape(detail)}</li>")
    return f"<ul>{''.join(rows)}</ul>" if rows else "<p class=ok>All passed</p>"


def write_report(out: Path, results, baseline_dir: Path):
    sections = []
    medians = generator_medians(results)
    banner = ""
    if medians["wall_seconds"] is not None or medians["turns"] is not None:
        models = ", ".join(medians["models"]) if medians["models"] else "-"
        banner = (
            "<p class=scores>"
            f"<span>Medians: wall {_fmt(medians['wall_seconds'], 's')}</span>"
            f"<span>turns {_fmt(medians['turns'])}</span>"
            f"<span>out {_fmt(medians['output_tokens'])} tok</span>"
            f"<span>checks {_fmt(medians['check_calls'])}</span>"
            f"<span>retries {_fmt(medians['compile_retries'])}</span>"
            f"<span>skill {_fmt(medians['skill_bytes_read'], ' B')}</span>"
            f"<span>model {html.escape(models)}</span>"
            "</p>"
        )
    for r in results:
        c_pass, c_total = score(r["checks"])
        j_pass, j_total = score(r["judge"])
        baseline = baseline_dir / f"{r['name']}.jpg"
        base_img = f'<figure><img src="{baseline.as_uri()}"><figcaption>baseline</figcaption></figure>' if baseline.exists() else ""
        shots = "".join(
            f'<figure><img src="{html.escape(r["name"])}/renders/{html.escape(n["png"])}">'
            f'<figcaption>{html.escape(label(n))}{"" if n.get("pass") is not False else " (fails checks)"}</figcaption></figure>'
            for n in primary_first(r["renders"], r["surface"])
        )
        m = case_metrics(r)
        cost = (r.get("agent") or {}).get("cost")
        gen_s = r.get("generator_seconds") if r.get("generator_seconds") is not None else m.get("wall_seconds")
        eval_min = r.get("eval_minutes", r.get("minutes", 0))
        meta = [r["surface"]]
        if gen_s is not None:
            meta.append(f"gen {gen_s}s")
            meta.append(f"eval {eval_min} min")
        else:
            meta.append(f"{eval_min} min")
        if m.get("turns") is not None:
            meta.append(f"{m['turns']} turns")
        if m.get("output_tokens") is not None:
            meta.append(f"{m['output_tokens']} out tok")
        if m.get("check_calls") is not None:
            meta.append(f"{m['check_calls']} checks")
        if m.get("compile_retries") is not None:
            meta.append(f"{m['compile_retries']} retries")
        if m.get("skill_bytes_read") is not None:
            meta.append(f"{m['skill_bytes_read']} skill B")
        if m.get("model"):
            meta.append(html.escape(str(m["model"])))
        meta.append(f"agent ${cost if cost is not None else '?'}")
        sections.append(f"""
<section>
  <h2>{html.escape(r['name'])} ({html.escape(r.get('status', 'legacy'))}) <small>{' · '.join(meta)}</small></h2>
  <p class=scores><span>Code checks {c_pass}/{c_total}</span><span>Rubric {j_pass}/{j_total}</span></p>
  <div class=grid><div><h3>Code check failures</h3>{failures(r['checks'])}</div><div><h3>Rubric failures</h3>{failures(r['judge'])}</div></div>
  <div class=shots>{base_img}{shots}</div>
  <details><summary>Agent's final message</summary><pre>{html.escape((r.get('agent') or {}).get('message') or '')}</pre></details>
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
</style></head><body><h1>android-design evals</h1>{banner}{''.join(sections)}</body></html>"""
    path = out / "report.html"
    path.write_text(page)
    return path


def write_summary(out: Path, results):
    """Writes summary.md, a plain scores table plus every failure, for terminals and CI summaries."""
    lines = [
        "| Case | Surface | Status | Code checks | Rubric | Gen (s) | Turns | Out tokens | Retries | Skill bytes |",
        "| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |",
    ]
    for r in results:
        c_pass, c_total = score(r["checks"])
        j_pass, j_total = score(r["judge"])
        m = case_metrics(r)
        gen_s = r.get("generator_seconds") if r.get("generator_seconds") is not None else m.get("wall_seconds")
        lines.append(
            f"| {r['name']} | {r['surface']} | {r.get('status', 'legacy')} | {c_pass}/{c_total} | {j_pass}/{j_total} "
            f"| {_fmt(gen_s)} | {_fmt(m.get('turns'))} | {_fmt(m.get('output_tokens'))} | {_fmt(m.get('compile_retries'))} "
            f"| {_fmt(m.get('skill_bytes_read'))} |"
        )
    medians = generator_medians(results)
    if medians["wall_seconds"] is not None or medians["turns"] is not None:
        models = ", ".join(medians["models"]) if medians["models"] else "-"
        lines.append("")
        lines.append(
            f"Generator medians: wall {_fmt(medians['wall_seconds'], 's')}, turns {_fmt(medians['turns'])}, "
            f"input_tokens {_fmt(medians['input_tokens'])}, output_tokens {_fmt(medians['output_tokens'])}, "
            f"check_calls {_fmt(medians['check_calls'])}, compile_retries {_fmt(medians['compile_retries'])}, "
            f"skill_bytes_read {_fmt(medians['skill_bytes_read'])} "
            f"(model: {models})"
        )
    failed = [(r["name"], k, v.get("detail") or v.get("reason") or "")
              for r in results for k, v in {**r["checks"], **r["judge"]}.items() if not v["pass"]]
    lines += ["", "Failures or pending judgments:" if failed else "No failures."]
    lines += [f"- {name} {item}: {reason}" for name, item, reason in failed]
    path = out / "summary.md"
    path.write_text("\n".join(lines) + "\n")
    return path
