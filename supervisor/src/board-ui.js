const VARIANTS = [
  { value: "tonal-spot", label: "Tonal Spot" },
  { value: "expressive", label: "Expressive" },
  { value: "vibrant", label: "Vibrant" },
  { value: "neutral", label: "Neutral" },
  { value: "monochrome", label: "Monochrome" },
  { value: "content", label: "Content" },
  { value: "fidelity", label: "Fidelity" },
  { value: "rainbow", label: "Rainbow" },
  { value: "fruit-salad", label: "Fruit Salad" },
];

export function renderBoardHtml({ nonce }) {
  const variantOptionsJson = JSON.stringify(VARIANTS);
  return `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Android Design Preview Board</title>
  <style nonce="${nonce}">
    :root {
      color-scheme: light dark;
      --bg: #111318;
      --surface: #1b1e26;
      --surface-2: #242833;
      --border: #323746;
      --text: #e6e8ef;
      --muted: #9aa1b3;
      --accent: #7aa2ff;
      --pass: #4cc38a;
      --fail: #f26d6d;
      --warn: #e5a94c;
    }
    * { box-sizing: border-box; }
    body {
      margin: 0;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      background: var(--bg);
      color: var(--text);
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }
    header {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      padding: 12px 18px;
      background: var(--surface);
      border-bottom: 1px solid var(--border);
    }
    .brand { display: flex; align-items: center; gap: 10px; }
    .brand h1 { font-size: 15px; margin: 0; font-weight: 600; }
    .pill {
      font-size: 11px;
      padding: 3px 8px;
      border-radius: 999px;
      background: var(--surface-2);
      border: 1px solid var(--border);
      color: var(--muted);
    }
    .pill.pass { color: var(--pass); border-color: rgba(76, 195, 138, 0.4); }
    .pill.fail { color: var(--fail); border-color: rgba(242, 109, 109, 0.4); }
    .toolbar-tabs { display: flex; gap: 6px; align-items: center; }
    button, select, input {
      font: inherit;
      color: var(--text);
      background: var(--surface-2);
      border: 1px solid var(--border);
      border-radius: 6px;
      padding: 6px 10px;
      font-size: 13px;
    }
    button { cursor: pointer; }
    button.active { background: var(--accent); color: #0b1020; border-color: var(--accent); font-weight: 600; }
    button:disabled { opacity: 0.45; cursor: not-allowed; }
    main { flex: 1; padding: 18px; display: flex; flex-direction: column; gap: 16px; }
    .banner {
      padding: 12px 14px;
      border-radius: 8px;
      border: 1px solid rgba(242, 109, 109, 0.45);
      background: rgba(242, 109, 109, 0.08);
      font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
      font-size: 12px;
      white-space: pre-wrap;
    }
    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
      gap: 16px;
    }
    .card {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 10px;
      padding: 14px;
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    .card-head { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
    .card-title { font-weight: 600; font-size: 14px; }
    .card-preview {
      background: #0d0f13;
      border: 1px solid var(--border);
      border-radius: 8px;
      min-height: 240px;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      cursor: pointer;
    }
    .card-preview img { max-width: 100%; max-height: 460px; display: block; object-fit: contain; }
    .controls {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      padding: 12px;
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 10px;
      align-items: flex-end;
    }
    .field { display: flex; flex-direction: column; gap: 4px; font-size: 11px; color: var(--muted); }
    .field-row { display: flex; gap: 6px; align-items: center; }
    .seed-picker { width: 34px; height: 31px; padding: 2px; cursor: pointer; }
    .seed-hex { width: 92px; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; }
    .stage-layout {
      display: grid;
      grid-template-columns: minmax(320px, 1fr) 340px;
      gap: 16px;
      align-items: start;
    }
    @media (max-width: 960px) {
      .stage-layout { grid-template-columns: 1fr; }
    }
    .viewport-wrap {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 10px;
      padding: 18px;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      position: relative;
    }
    .frame-box {
      position: relative;
      display: inline-block;
      line-height: 0;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 8px 30px rgba(0, 0, 0, 0.45);
      max-width: 100%;
    }
    .frame-box img {
      display: block;
      max-width: 100%;
      max-height: 76vh;
      width: auto;
      height: auto;
      user-select: none;
    }
    .frame-box svg.overlay {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
      cursor: crosshair;
    }
    .frame-box svg.overlay rect.node {
      fill: transparent;
      stroke: rgba(122, 162, 255, 0.25);
      stroke-width: 1;
      vector-effect: non-scaling-stroke;
    }
    .frame-box svg.overlay rect.node:hover,
    .frame-box svg.overlay rect.node.selected {
      fill: rgba(122, 162, 255, 0.14);
      stroke: #7aa2ff;
      stroke-width: 2;
    }
    .frame-box svg.overlay rect.node.primary {
      stroke: rgba(76, 195, 138, 0.65);
    }
    .inspector {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 10px;
      padding: 14px;
      display: flex;
      flex-direction: column;
      gap: 12px;
      font-size: 13px;
    }
    .mono { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: 12px; }
    .finding { padding: 8px; border-radius: 6px; background: var(--surface-2); font-size: 12px; }
    .finding.error { border-left: 3px solid var(--fail); }
    .finding.info { border-left: 3px solid var(--warn); }
    .hover-hud {
      width: 100%;
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 8px;
      font-size: 12px;
      color: var(--muted);
      border-top: 1px solid var(--border);
      padding-top: 8px;
    }
    .toast {
      position: fixed;
      bottom: 18px;
      right: 18px;
      padding: 10px 14px;
      border-radius: 8px;
      background: #1f2937;
      border: 1px solid var(--accent);
      color: #f9fafb;
      font-size: 12px;
      font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
      opacity: 0;
      pointer-events: none;
      transition: opacity 150ms ease;
    }
    .toast.visible { opacity: 1; }
    .hidden { display: none !important; }
  </style>
</head>
<body>
  <header>
    <div class="brand">
      <h1 id="board-title">Android Design Preview</h1>
      <span id="surface-pill" class="pill">loading</span>
      <span id="status-pill" class="pill">waiting</span>
      <span id="live-pill" class="pill">SSE connecting</span>
    </div>
    <div class="toolbar-tabs">
      <button id="tab-grid" type="button" class="active">All Directions</button>
      <button id="tab-stage" type="button">Focus Stage</button>
    </div>
  </header>

  <main>
    <div id="error-banner" class="banner hidden"></div>
    <section id="grid-view" class="grid"></section>
    <section id="stage-view" class="hidden">
      <div class="controls">
        <div class="field">
          <label for="ctrl-direction">Direction</label>
          <select id="ctrl-direction"></select>
        </div>
        <div class="field">
          <label>Brightness</label>
          <div class="field-row">
            <button id="btn-light" type="button">Light</button>
            <button id="btn-dark" type="button">Dark</button>
          </div>
        </div>
        <div class="field">
          <label for="ctrl-seed-hex">Seed Color</label>
          <div class="field-row">
            <input id="ctrl-seed-picker" class="seed-picker" type="color" value="#6750A4">
            <input id="ctrl-seed-hex" class="seed-hex" type="text" value="#6750A4" maxlength="9">
            <button id="btn-seed-reset" type="button">Reset</button>
          </div>
        </div>
        <div class="field">
          <label for="ctrl-variant">Style Variant</label>
          <select id="ctrl-variant"></select>
        </div>
        <div class="field">
          <label for="ctrl-screen">Screen</label>
          <select id="ctrl-screen"></select>
        </div>
        <div class="field">
          <label for="ctrl-overlay">Overlay</label>
          <select id="ctrl-overlay"></select>
        </div>
        <div class="field">
          <label for="ctrl-view">Device</label>
          <select id="ctrl-view"></select>
        </div>
        <div class="field">
          <label>Inspector</label>
          <div class="field-row">
            <button id="btn-toggle-boxes" type="button" class="active">Boxes</button>
            <button id="btn-scroll-frame" type="button" class="hidden">Top Frame</button>
          </div>
        </div>
      </div>

      <div class="stage-layout">
        <div class="viewport-wrap">
          <div id="frame-box" class="frame-box">
            <img id="stage-img" alt="Rendered screen preview">
            <svg id="stage-svg" class="overlay" aria-label="Interactive component overlay"></svg>
          </div>
          <div class="hover-hud">
            <span id="hover-info" class="mono">Hover a component box to inspect; click to copy source location.</span>
            <span id="render-meta" class="mono"></span>
          </div>
        </div>

        <aside class="inspector">
          <div>
            <strong>Source Location</strong>
            <div id="selected-source" class="mono" style="margin-top:4px;word-break:break-all;">none selected</div>
          </div>
          <div>
            <strong>Render Checks</strong>
            <div id="stage-findings" style="display:flex;flex-direction:column;gap:6px;margin-top:6px;"></div>
          </div>
          <div>
            <strong>Layout &amp; Navigation Nodes</strong>
            <div id="node-list" style="display:flex;flex-direction:column;gap:4px;margin-top:6px;max-height:280px;overflow:auto;"></div>
          </div>
        </aside>
      </div>
    </section>
  </main>

  <div id="toast" class="toast" role="status" aria-live="polite"></div>

  <script nonce="${nonce}">
    const VARIANTS = ${variantOptionsJson};
    const TOKEN = new URLSearchParams(window.location.search).get("token") || "";

    const state = {
      board: null,
      mode: "grid",
      direction: null,
      screen: null,
      overlay: "",
      view: null,
      dark: null,
      seed: null,
      variant: null,
      showBoxes: true,
      showEndFrame: false,
      stageRender: null,
      cardRenders: new Map(),
      selectedNode: null,
    };

    const els = {
      title: document.getElementById("board-title"),
      surfacePill: document.getElementById("surface-pill"),
      statusPill: document.getElementById("status-pill"),
      livePill: document.getElementById("live-pill"),
      errorBanner: document.getElementById("error-banner"),
      tabGrid: document.getElementById("tab-grid"),
      tabStage: document.getElementById("tab-stage"),
      gridView: document.getElementById("grid-view"),
      stageView: document.getElementById("stage-view"),
      ctrlDirection: document.getElementById("ctrl-direction"),
      btnLight: document.getElementById("btn-light"),
      btnDark: document.getElementById("btn-dark"),
      ctrlSeedPicker: document.getElementById("ctrl-seed-picker"),
      ctrlSeedHex: document.getElementById("ctrl-seed-hex"),
      btnSeedReset: document.getElementById("btn-seed-reset"),
      ctrlVariant: document.getElementById("ctrl-variant"),
      ctrlScreen: document.getElementById("ctrl-screen"),
      ctrlOverlay: document.getElementById("ctrl-overlay"),
      ctrlView: document.getElementById("ctrl-view"),
      btnToggleBoxes: document.getElementById("btn-toggle-boxes"),
      btnScrollFrame: document.getElementById("btn-scroll-frame"),
      stageImg: document.getElementById("stage-img"),
      stageSvg: document.getElementById("stage-svg"),
      hoverInfo: document.getElementById("hover-info"),
      renderMeta: document.getElementById("render-meta"),
      selectedSource: document.getElementById("selected-source"),
      stageFindings: document.getElementById("stage-findings"),
      nodeList: document.getElementById("node-list"),
      toast: document.getElementById("toast"),
    };

    function showToast(text) {
      els.toast.textContent = text;
      els.toast.classList.add("visible");
      clearTimeout(showToast.timer);
      showToast.timer = setTimeout(() => els.toast.classList.remove("visible"), 1800);
    }

    async function copySourceLocation(loc) {
      if (!loc) return;
      els.selectedSource.textContent = loc;
      try {
        await navigator.clipboard.writeText(loc);
      } catch {
        // Headless or insecure context fallback keeps selected-source visible.
      }
      showToast("Copied " + loc);
    }

    async function apiRender(params) {
      const res = await fetch("/api/render", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-Session-Token": TOKEN,
        },
        body: JSON.stringify(params),
      });
      if (!res.ok) {
        const err = await res.json().catch(() => ({ error: "HTTP " + res.status }));
        throw new Error(err.message || err.error || "Render failed");
      }
      return res.json();
    }

    function normalizeHexColor(value) {
      if (!value) return "#6750A4";
      const clean = value.trim().replace(/^0x/i, "").replace(/^#/, "");
      if (clean.length === 8) return "#" + clean.slice(2).toUpperCase();
      if (clean.length === 6) return "#" + clean.toUpperCase();
      return "#6750A4";
    }

    function populateVariants() {
      els.ctrlVariant.innerHTML = "";
      for (const item of VARIANTS) {
        const opt = document.createElement("option");
        opt.value = item.value;
        opt.textContent = item.label;
        els.ctrlVariant.appendChild(opt);
      }
    }

    function setMode(mode) {
      state.mode = mode;
      els.tabGrid.classList.toggle("active", mode === "grid");
      els.tabStage.classList.toggle("active", mode === "stage");
      els.gridView.classList.toggle("hidden", mode !== "grid");
      els.stageView.classList.toggle("hidden", mode !== "stage");
      if (mode === "stage" && state.board?.valid) {
        refreshStageRender();
      }
    }

    function currentDirectionMeta() {
      const dirs = state.board?.boardMeta?.directions || [];
      return dirs.find((d) => d.name === state.direction) || dirs[0] || null;
    }

    function syncControlsFromBoard() {
      const b = state.board;
      if (!b) return;

      els.title.textContent = b.boardName || b.sourceFile || "Android Design Preview";
      els.surfacePill.textContent = b.surface || "phone";
      els.statusPill.textContent = b.valid ? (b.pass ? "all checks pass" : "findings need fixes") : "compile error";
      els.statusPill.className = "pill " + (b.valid && b.pass ? "pass" : "fail");

      if (!b.valid || (b.errors && b.errors.length > 0)) {
        const msg = (b.errors || []).map((e) => (e.path ? e.path + ": " : "") + e.message).join("\\n");
        els.errorBanner.textContent = msg || "Compilation failed.";
        els.errorBanner.classList.remove("hidden");
      } else {
        els.errorBanner.classList.add("hidden");
      }

      if (!b.boardMeta) return;
      const dirs = b.boardMeta.directions || [];
      const views = b.boardMeta.views || [];

      if (!dirs.some((d) => d.name === state.direction)) {
        state.direction = dirs[0]?.name || null;
      }
      const dir = currentDirectionMeta();

      els.ctrlDirection.innerHTML = "";
      for (const d of dirs) {
        const opt = document.createElement("option");
        opt.value = d.name;
        opt.textContent = d.name;
        opt.selected = d.name === state.direction;
        els.ctrlDirection.appendChild(opt);
      }

      if (dir) {
        if (!dir.screens.includes(state.screen)) {
          state.screen = dir.startScreen || dir.screens[0] || null;
        }
        if (state.overlay && !dir.overlays.some((o) => o.id === state.overlay)) {
          state.overlay = "";
        }
        els.ctrlScreen.innerHTML = "";
        for (const s of dir.screens) {
          const opt = document.createElement("option");
          opt.value = s;
          opt.textContent = s;
          opt.selected = s === state.screen;
          els.ctrlScreen.appendChild(opt);
        }

        els.ctrlOverlay.innerHTML = "";
        const noneOpt = document.createElement("option");
        noneOpt.value = "";
        noneOpt.textContent = "None";
        noneOpt.selected = !state.overlay;
        els.ctrlOverlay.appendChild(noneOpt);
        for (const ov of dir.overlays) {
          const opt = document.createElement("option");
          opt.value = ov.id;
          opt.textContent = ov.screen ? ov.id + " (" + ov.screen + ")" : ov.id;
          opt.selected = ov.id === state.overlay;
          els.ctrlOverlay.appendChild(opt);
        }
      }

      if (!views.some((v) => v.name === state.view)) {
        state.view = views[0]?.name || null;
      }
      els.ctrlView.innerHTML = "";
      for (const v of views) {
        const opt = document.createElement("option");
        opt.value = v.name;
        opt.textContent = v.name + " (" + v.width + "x" + v.height + ")";
        opt.selected = v.name === state.view;
        els.ctrlView.appendChild(opt);
      }

      const seedHex = normalizeHexColor(state.seed || b.theme?.seed);
      els.ctrlSeedPicker.value = seedHex;
      els.ctrlSeedHex.value = seedHex;
      els.ctrlVariant.value = state.variant || b.theme?.variant || "tonal-spot";

      const isWear = b.surface === "wear";
      const activeViewSpec = views.find((v) => v.name === state.view);
      const effectiveDark = isWear ? true : (state.dark ?? activeViewSpec?.dark ?? false);
      els.btnLight.disabled = isWear;
      els.btnLight.classList.toggle("active", !effectiveDark);
      els.btnDark.classList.toggle("active", effectiveDark);
    }

    async function renderDirectionGrid() {
      const b = state.board;
      els.gridView.innerHTML = "";
      if (!b || !b.valid || !b.boardMeta) return;

      const defaultView = b.surface === "wear" ? "watch" : "light";
      for (const dir of b.boardMeta.directions) {
        const dirRenders = (b.renders || []).filter((r) => r.direction === dir.name);
        const dirPass = dirRenders.every((r) => r.pass !== false);
        const failCount = dirRenders.reduce((n, r) => n + (r.failures?.length || 0), 0);

        const card = document.createElement("article");
        card.className = "card";

        const head = document.createElement("div");
        head.className = "card-head";
        const title = document.createElement("span");
        title.className = "card-title";
        title.textContent = dir.name;

        const right = document.createElement("div");
        right.className = "field-row";
        const status = document.createElement("span");
        status.className = "pill " + (dirPass ? "pass" : "fail");
        status.textContent = dirPass ? "pass" : failCount + " issue(s)";
        const focusBtn = document.createElement("button");
        focusBtn.type = "button";
        focusBtn.textContent = "Focus";
        focusBtn.onclick = () => {
          state.direction = dir.name;
          state.screen = dir.startScreen;
          state.overlay = "";
          syncControlsFromBoard();
          setMode("stage");
        };
        right.append(status, focusBtn);
        head.append(title, right);

        const preview = document.createElement("div");
        preview.className = "card-preview";
        preview.onclick = focusBtn.onclick;
        const img = document.createElement("img");
        img.alt = dir.name + " preview";
        preview.appendChild(img);

        const cached = dirRenders.find(
          (r) => r.screen === dir.startScreen && !r.overlay && r.view === defaultView && r.pngBase64,
        );
        if (cached) {
          img.src = "data:image/png;base64," + cached.pngBase64;
        } else {
          apiRender({
            direction: dir.name,
            screen: dir.startScreen,
            view: defaultView,
          }).then((res) => {
            img.src = "data:image/png;base64," + res.pngBase64;
          }).catch(() => {});
        }

        const meta = document.createElement("div");
        meta.className = "mono";
        meta.style.color = "var(--muted)";
        meta.textContent = "screens: " + dir.screens.join(", ") + (dir.overlays.length ? " | overlays: " + dir.overlays.map((o) => o.id).join(", ") : "");

        card.append(head, preview, meta);
        els.gridView.appendChild(card);
      }
    }

    function maybeSwitchTargetFromNode(node) {
      const dir = currentDirectionMeta();
      if (!dir || !node) return false;
      const roles = Array.isArray(node.roles) ? node.roles : [];
      const candidates = [node.id, node.text?.value]
        .filter(Boolean)
        .map((s) => String(s).trim().toLowerCase());

      if (roles.some((r) => /nav|tab/i.test(r))) {
        const matchedScreen = dir.screens.find((s) =>
          candidates.some((c) => c === s.toLowerCase() || s.toLowerCase().includes(c)),
        );
        if (matchedScreen && matchedScreen !== state.screen) {
          state.screen = matchedScreen;
          state.overlay = "";
          syncControlsFromBoard();
          refreshStageRender();
          return true;
        }
      }

      const matchedOverlay = dir.overlays.find((ov) =>
        candidates.some((c) => c === ov.id.toLowerCase()),
      );
      if (matchedOverlay) {
        state.overlay = state.overlay === matchedOverlay.id ? "" : matchedOverlay.id;
        syncControlsFromBoard();
        refreshStageRender();
        return true;
      }
      return false;
    }

    function renderStageOverlay(render) {
      const views = state.board?.boardMeta?.views || [];
      const viewSpec = views.find((v) => v.name === render.view) || { width: 412, height: 915 };
      const svg = els.stageSvg;
      svg.setAttribute("viewBox", "0 0 " + viewSpec.width + " " + viewSpec.height);
      svg.innerHTML = "";
      els.nodeList.innerHTML = "";

      if (!state.showBoxes || !render.tree?.nodes) {
        svg.classList.add("hidden");
        return;
      }
      svg.classList.remove("hidden");

      const nodes = render.tree.nodes.filter((n) => n.bounds && n.bounds.width > 0 && n.bounds.height > 0);
      nodes.forEach((node, idx) => {
        const rect = document.createElementNS("http://www.w3.org/2000/svg", "rect");
        rect.setAttribute("x", String(node.bounds.x));
        rect.setAttribute("y", String(node.bounds.y));
        rect.setAttribute("width", String(node.bounds.width));
        rect.setAttribute("height", String(node.bounds.height));
        rect.setAttribute("class", "node" + (node.primary ? " primary" : ""));
        rect.dataset.index = String(idx);

        const loc = node.sourceFile && node.sourceLine
          ? node.sourceFile + ":" + node.sourceLine
          : (state.board?.sourceFile || "");
        const dims = Math.round(node.bounds.width) + "x" + Math.round(node.bounds.height) + " dp";
        const label = node.type + (node.id ? "#" + node.id : "") + " (" + dims + ") " + loc;

        rect.addEventListener("mouseenter", () => {
          els.hoverInfo.textContent = label;
        });
        rect.addEventListener("click", (ev) => {
          ev.stopPropagation();
          for (const child of svg.querySelectorAll("rect.selected")) {
            child.classList.remove("selected");
          }
          rect.classList.add("selected");
          state.selectedNode = node;
          copySourceLocation(loc);
          maybeSwitchTargetFromNode(node);
        });
        svg.appendChild(rect);

        if (node.sourceLine || node.primary || (node.roles && node.roles.length)) {
          const item = document.createElement("button");
          item.type = "button";
          item.className = "mono";
          item.style.textAlign = "left";
          item.style.padding = "4px 6px";
          item.textContent = label;
          item.onclick = () => copySourceLocation(loc);
          els.nodeList.appendChild(item);
        }
      });
    }

    function renderStageFindings(render) {
      els.stageFindings.innerHTML = "";
      const errs = render.check?.errors || [];
      const notes = render.check?.notes || {};
      if (errs.length === 0 && Object.keys(notes).length === 0) {
        const ok = document.createElement("div");
        ok.className = "pill pass";
        ok.textContent = "Zero findings for this view";
        els.stageFindings.appendChild(ok);
        return;
      }
      for (const e of errs) {
        const div = document.createElement("div");
        div.className = "finding error";
        div.textContent = (e.rule || "error") + ": " + (e.detail || JSON.stringify(e));
        els.stageFindings.appendChild(div);
      }
      for (const [rule, ids] of Object.entries(notes)) {
        const div = document.createElement("div");
        div.className = "finding info";
        div.textContent = rule + ": " + (Array.isArray(ids) ? ids.join(", ") : String(ids));
        els.stageFindings.appendChild(div);
      }
    }

    async function refreshStageRender() {
      if (!state.board?.valid || !state.direction) return;
      try {
        const params = {
          direction: state.direction,
          screen: state.screen,
          ...(state.overlay ? { overlay: state.overlay } : {}),
          ...(state.view ? { view: state.view } : {}),
          ...(state.seed ? { seed: state.seed } : {}),
          ...(state.variant ? { variant: state.variant } : {}),
          ...(state.dark !== null && state.board.surface !== "wear" ? { dark: state.dark } : {}),
        };
        const render = await apiRender(params);
        state.stageRender = render;
        const b64 = state.showEndFrame && render.endPngBase64 ? render.endPngBase64 : render.pngBase64;
        els.stageImg.src = "data:image/png;base64," + b64;
        els.btnScrollFrame.classList.toggle("hidden", !render.endPngBase64);
        els.btnScrollFrame.textContent = state.showEndFrame ? "Scrolled End" : "Top Frame";
        els.renderMeta.textContent = render.view + " | " + render.renderMs + " ms";
        renderStageOverlay(render);
        renderStageFindings(render);
      } catch (err) {
        els.hoverInfo.textContent = "Render error: " + err.message;
      }
    }

    function applyBoardUpdate(board) {
      state.board = board;
      syncControlsFromBoard();
      renderDirectionGrid();
      if (state.mode === "stage") {
        refreshStageRender();
      }
    }

    async function init() {
      populateVariants();

      els.tabGrid.onclick = () => setMode("grid");
      els.tabStage.onclick = () => setMode("stage");
      els.ctrlDirection.onchange = () => {
        state.direction = els.ctrlDirection.value;
        state.screen = null;
        state.overlay = "";
        syncControlsFromBoard();
        refreshStageRender();
      };
      els.btnLight.onclick = () => {
        state.dark = false;
        syncControlsFromBoard();
        refreshStageRender();
      };
      els.btnDark.onclick = () => {
        state.dark = true;
        syncControlsFromBoard();
        refreshStageRender();
      };
      els.ctrlSeedPicker.oninput = () => {
        state.seed = els.ctrlSeedPicker.value;
        els.ctrlSeedHex.value = state.seed.toUpperCase();
        refreshStageRender();
      };
      els.ctrlSeedHex.onchange = () => {
        const norm = normalizeHexColor(els.ctrlSeedHex.value);
        state.seed = norm;
        els.ctrlSeedPicker.value = norm;
        els.ctrlSeedHex.value = norm;
        refreshStageRender();
      };
      els.btnSeedReset.onclick = () => {
        state.seed = null;
        state.variant = null;
        state.dark = null;
        syncControlsFromBoard();
        refreshStageRender();
      };
      els.ctrlVariant.onchange = () => {
        state.variant = els.ctrlVariant.value;
        refreshStageRender();
      };
      els.ctrlScreen.onchange = () => {
        state.screen = els.ctrlScreen.value;
        refreshStageRender();
      };
      els.ctrlOverlay.onchange = () => {
        state.overlay = els.ctrlOverlay.value;
        refreshStageRender();
      };
      els.ctrlView.onchange = () => {
        state.view = els.ctrlView.value;
        syncControlsFromBoard();
        refreshStageRender();
      };
      els.btnToggleBoxes.onclick = () => {
        state.showBoxes = !state.showBoxes;
        els.btnToggleBoxes.classList.toggle("active", state.showBoxes);
        if (state.stageRender) renderStageOverlay(state.stageRender);
      };
      els.btnScrollFrame.onclick = () => {
        state.showEndFrame = !state.showEndFrame;
        if (state.stageRender) {
          const b64 = state.showEndFrame && state.stageRender.endPngBase64
            ? state.stageRender.endPngBase64
            : state.stageRender.pngBase64;
          els.stageImg.src = "data:image/png;base64," + b64;
          els.btnScrollFrame.textContent = state.showEndFrame ? "Scrolled End" : "Top Frame";
        }
      };

      try {
        const res = await fetch("/api/board", {
          headers: { "X-Session-Token": TOKEN },
        });
        if (res.ok) {
          const board = await res.json();
          if (board) applyBoardUpdate(board);
        }
      } catch (err) {
        els.errorBanner.textContent = "Failed loading initial board: " + err.message;
        els.errorBanner.classList.remove("hidden");
      }

      const events = new EventSource("/api/events?token=" + encodeURIComponent(TOKEN));
      events.onopen = () => {
        els.livePill.textContent = "SSE live";
        els.livePill.className = "pill pass";
      };
      events.onerror = () => {
        els.livePill.textContent = "SSE reconnecting";
        els.livePill.className = "pill fail";
      };
      events.addEventListener("board", (ev) => {
        try {
          applyBoardUpdate(JSON.parse(ev.data));
        } catch {
          // Ignore malformed SSE event payload.
        }
      });
      events.addEventListener("error_event", (ev) => {
        try {
          const payload = JSON.parse(ev.data);
          els.errorBanner.textContent = payload.message || "Recompile failed";
          els.errorBanner.classList.remove("hidden");
        } catch {
          // Ignore.
        }
      });
    }

    init();
  </script>
</body>
</html>`;
}
