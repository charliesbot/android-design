#!/usr/bin/env python3
"""Serves the local picker for a preference round and saves the owner's picks.

Usage: serve.py ROUND_DIR [--port 8765]

Localhost only. Picks are written to ROUND_DIR/selections.json after every change, so closing the tab
loses nothing and restarting resumes where you left off.
"""
import argparse
import json
import webbrowser
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

HERE = Path(__file__).resolve().parent


def round_state(root):
    manifest = json.loads((root / "manifest.json").read_text())
    selections_file = root / "selections.json"
    selections = json.loads(selections_file.read_text()) if selections_file.exists() else {}
    cases = []
    for case in manifest["cases"]:
        variants = []
        for n in range(1, manifest["variants"] + 1):
            record_file = root / case["name"] / f"v{n}" / "variant.json"
            record = json.loads(record_file.read_text()) if record_file.exists() else None
            variants.append({
                "id": f"v{n}",
                "status": "pending" if record is None else ("ok" if record["renders"] else "failed"),
                "renders": record["renders"] if record else [],
            })
        cases.append({"name": case["name"], "prompt": case["prompt"], "variants": variants})
    return {"cases": cases, "selections": selections}


def make_handler(root):
    class Handler(BaseHTTPRequestHandler):
        def send(self, status, body, content_type):
            self.send_response(status)
            self.send_header("Content-Type", content_type)
            self.send_header("Cache-Control", "no-store")
            self.end_headers()
            self.wfile.write(body)

        def do_GET(self):
            if self.path in ("/", "/index.html"):
                return self.send(200, (HERE / "picker.html").read_bytes(), "text/html; charset=utf-8")
            if self.path == "/api/round":
                return self.send(200, json.dumps(round_state(root)).encode(), "application/json")
            if self.path.startswith("/renders/"):
                # /renders/<case>/<variant>/<file>.png
                parts = self.path.split("/")[2:]
                if len(parts) == 3 and all(p and ".." not in p for p in parts) and parts[2].endswith(".png"):
                    image = root / parts[0] / parts[1] / "renders" / parts[2]
                    if image.is_file():
                        return self.send(200, image.read_bytes(), "image/png")
            self.send(404, b"not found", "text/plain")

        def do_POST(self):
            if self.path != "/api/select":
                return self.send(404, b"not found", "text/plain")
            pick = json.loads(self.rfile.read(int(self.headers["Content-Length"])))
            selections_file = root / "selections.json"
            selections = json.loads(selections_file.read_text()) if selections_file.exists() else {}
            selections[pick.pop("case")] = pick
            selections_file.write_text(json.dumps(selections, indent=2))
            self.send(200, b"{}", "application/json")

        def log_message(self, *args):
            pass

    return Handler


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("round_dir")
    parser.add_argument("--port", type=int, default=8765)
    parser.add_argument("--no-open", action="store_true")
    args = parser.parse_args()
    root = Path(args.round_dir).expanduser().resolve()
    server = ThreadingHTTPServer(("127.0.0.1", args.port), make_handler(root))
    url = f"http://127.0.0.1:{args.port}/"
    print(f"picker: {url}  (picks save to {root / 'selections.json'})", flush=True)
    if not args.no_open:
        webbrowser.open(url)
    server.serve_forever()


if __name__ == "__main__":
    main()
