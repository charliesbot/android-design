//#region node_modules/tslib/tslib.es6.mjs
function e(e, t, n, r) {
	var i = arguments.length, a = i < 3 ? t : r === null ? r = Object.getOwnPropertyDescriptor(t, n) : r, o;
	if (typeof Reflect == "object" && typeof Reflect.decorate == "function") a = Reflect.decorate(e, t, n, r);
	else for (var s = e.length - 1; s >= 0; s--) (o = e[s]) && (a = (i < 3 ? o(a) : i > 3 ? o(t, n, a) : o(t, n)) || a);
	return i > 3 && a && Object.defineProperty(t, n, a), a;
}
function t(e, t, n, r) {
	if (n === "a" && !r) throw TypeError("Private accessor was defined without a getter");
	if (typeof t == "function" ? e !== t || !r : !t.has(e)) throw TypeError("Cannot read private member from an object whose class did not declare it");
	return n === "m" ? r : n === "a" ? r.call(e) : r ? r.value : t.get(e);
}
function n(e, t, n, r, i) {
	if (r === "m") throw TypeError("Private method is not writable");
	if (r === "a" && !i) throw TypeError("Private accessor was defined without a setter");
	if (typeof t == "function" ? e !== t || !i : !t.has(e)) throw TypeError("Cannot write private member to an object whose class did not declare it");
	return r === "a" ? i.call(e, n) : i ? i.value = n : t.set(e, n), n;
}
//#endregion
//#region node_modules/@lit/reactive-element/css-tag.js
var r = globalThis, i = r.ShadowRoot && (r.ShadyCSS === void 0 || r.ShadyCSS.nativeShadow) && "adoptedStyleSheets" in Document.prototype && "replace" in CSSStyleSheet.prototype, a = Symbol(), o = /* @__PURE__ */ new WeakMap(), s = class {
	constructor(e, t, n) {
		if (this._$cssResult$ = !0, n !== a) throw Error("CSSResult is not constructable. Use `unsafeCSS` or `css` instead.");
		this.cssText = e, this.t = t;
	}
	get styleSheet() {
		let e = this.o, t = this.t;
		if (i && e === void 0) {
			let n = t !== void 0 && t.length === 1;
			n && (e = o.get(t)), e === void 0 && ((this.o = e = new CSSStyleSheet()).replaceSync(this.cssText), n && o.set(t, e));
		}
		return e;
	}
	toString() {
		return this.cssText;
	}
}, c = (e) => new s(typeof e == "string" ? e : e + "", void 0, a), l = (e, ...t) => new s(e.length === 1 ? e[0] : t.reduce((t, n, r) => t + ((e) => {
	if (!0 === e._$cssResult$) return e.cssText;
	if (typeof e == "number") return e;
	throw Error("Value passed to 'css' function must be a 'css' function result: " + e + ". Use 'unsafeCSS' to pass non-literal values, but take care to ensure page security.");
})(n) + e[r + 1], e[0]), e, a), u = (e, t) => {
	if (i) e.adoptedStyleSheets = t.map((e) => e instanceof CSSStyleSheet ? e : e.styleSheet);
	else for (let n of t) {
		let t = document.createElement("style"), i = r.litNonce;
		i !== void 0 && t.setAttribute("nonce", i), t.textContent = n.cssText, e.appendChild(t);
	}
}, d = i ? (e) => e : (e) => e instanceof CSSStyleSheet ? ((e) => {
	let t = "";
	for (let n of e.cssRules) t += n.cssText;
	return c(t);
})(e) : e, { is: f, defineProperty: p, getOwnPropertyDescriptor: m, getOwnPropertyNames: h, getOwnPropertySymbols: ee, getPrototypeOf: te } = Object, ne = globalThis, g = ne.trustedTypes, re = g ? g.emptyScript : "", _ = ne.reactiveElementPolyfillSupport, ie = (e, t) => e, v = {
	toAttribute(e, t) {
		switch (t) {
			case Boolean:
				e = e ? re : null;
				break;
			case Object:
			case Array: e = e == null ? e : JSON.stringify(e);
		}
		return e;
	},
	fromAttribute(e, t) {
		let n = e;
		switch (t) {
			case Boolean:
				n = e !== null;
				break;
			case Number:
				n = e === null ? null : Number(e);
				break;
			case Object:
			case Array: try {
				n = JSON.parse(e);
			} catch {
				n = null;
			}
		}
		return n;
	}
}, ae = (e, t) => !f(e, t), oe = {
	attribute: !0,
	type: String,
	converter: v,
	reflect: !1,
	useDefault: !1,
	hasChanged: ae
};
Symbol.metadata ??= Symbol("metadata"), ne.litPropertyMetadata ??= /* @__PURE__ */ new WeakMap();
var se = class extends HTMLElement {
	static addInitializer(e) {
		this._$Ei(), (this.l ??= []).push(e);
	}
	static get observedAttributes() {
		return this.finalize(), this._$Eh && [...this._$Eh.keys()];
	}
	static createProperty(e, t = oe) {
		if (t.state && (t.attribute = !1), this._$Ei(), this.prototype.hasOwnProperty(e) && ((t = Object.create(t)).wrapped = !0), this.elementProperties.set(e, t), !t.noAccessor) {
			let n = Symbol(), r = this.getPropertyDescriptor(e, n, t);
			r !== void 0 && p(this.prototype, e, r);
		}
	}
	static getPropertyDescriptor(e, t, n) {
		let { get: r, set: i } = m(this.prototype, e) ?? {
			get() {
				return this[t];
			},
			set(e) {
				this[t] = e;
			}
		};
		return {
			get: r,
			set(t) {
				let a = r?.call(this);
				i?.call(this, t), this.requestUpdate(e, a, n);
			},
			configurable: !0,
			enumerable: !0
		};
	}
	static getPropertyOptions(e) {
		return this.elementProperties.get(e) ?? oe;
	}
	static _$Ei() {
		if (this.hasOwnProperty(ie("elementProperties"))) return;
		let e = te(this);
		e.finalize(), e.l !== void 0 && (this.l = [...e.l]), this.elementProperties = new Map(e.elementProperties);
	}
	static finalize() {
		if (this.hasOwnProperty(ie("finalized"))) return;
		if (this.finalized = !0, this._$Ei(), this.hasOwnProperty(ie("properties"))) {
			let e = this.properties, t = [...h(e), ...ee(e)];
			for (let n of t) this.createProperty(n, e[n]);
		}
		let e = this[Symbol.metadata];
		if (e !== null) {
			let t = litPropertyMetadata.get(e);
			if (t !== void 0) for (let [e, n] of t) this.elementProperties.set(e, n);
		}
		this._$Eh = /* @__PURE__ */ new Map();
		for (let [e, t] of this.elementProperties) {
			let n = this._$Eu(e, t);
			n !== void 0 && this._$Eh.set(n, e);
		}
		this.elementStyles = this.finalizeStyles(this.styles);
	}
	static finalizeStyles(e) {
		let t = [];
		if (Array.isArray(e)) {
			let n = new Set(e.flat(1 / 0).reverse());
			for (let e of n) t.unshift(d(e));
		} else e !== void 0 && t.push(d(e));
		return t;
	}
	static _$Eu(e, t) {
		let n = t.attribute;
		return !1 === n ? void 0 : typeof n == "string" ? n : typeof e == "string" ? e.toLowerCase() : void 0;
	}
	constructor() {
		super(), this._$Ep = void 0, this.isUpdatePending = !1, this.hasUpdated = !1, this._$Em = null, this._$Ev();
	}
	_$Ev() {
		this._$ES = new Promise((e) => this.enableUpdating = e), this._$AL = /* @__PURE__ */ new Map(), this._$E_(), this.requestUpdate(), this.constructor.l?.forEach((e) => e(this));
	}
	addController(e) {
		(this._$EO ??= /* @__PURE__ */ new Set()).add(e), this.renderRoot !== void 0 && this.isConnected && e.hostConnected?.();
	}
	removeController(e) {
		this._$EO?.delete(e);
	}
	_$E_() {
		let e = /* @__PURE__ */ new Map(), t = this.constructor.elementProperties;
		for (let n of t.keys()) this.hasOwnProperty(n) && (e.set(n, this[n]), delete this[n]);
		e.size > 0 && (this._$Ep = e);
	}
	createRenderRoot() {
		let e = this.shadowRoot ?? this.attachShadow(this.constructor.shadowRootOptions);
		return u(e, this.constructor.elementStyles), e;
	}
	connectedCallback() {
		this.renderRoot ??= this.createRenderRoot(), this.enableUpdating(!0), this._$EO?.forEach((e) => e.hostConnected?.());
	}
	enableUpdating(e) {}
	disconnectedCallback() {
		this._$EO?.forEach((e) => e.hostDisconnected?.());
	}
	attributeChangedCallback(e, t, n) {
		this._$AK(e, n);
	}
	_$ET(e, t) {
		let n = this.constructor.elementProperties.get(e), r = this.constructor._$Eu(e, n);
		if (r !== void 0 && !0 === n.reflect) {
			let i = (n.converter?.toAttribute === void 0 ? v : n.converter).toAttribute(t, n.type);
			this._$Em = e, i == null ? this.removeAttribute(r) : this.setAttribute(r, i), this._$Em = null;
		}
	}
	_$AK(e, t) {
		let n = this.constructor, r = n._$Eh.get(e);
		if (r !== void 0 && this._$Em !== r) {
			let e = n.getPropertyOptions(r), i = typeof e.converter == "function" ? { fromAttribute: e.converter } : e.converter?.fromAttribute === void 0 ? v : e.converter;
			this._$Em = r;
			let a = i.fromAttribute(t, e.type);
			this[r] = a ?? this._$Ej?.get(r) ?? a, this._$Em = null;
		}
	}
	requestUpdate(e, t, n, r = !1, i) {
		if (e !== void 0) {
			let a = this.constructor;
			if (!1 === r && (i = this[e]), n ??= a.getPropertyOptions(e), !((n.hasChanged ?? ae)(i, t) || n.useDefault && n.reflect && i === this._$Ej?.get(e) && !this.hasAttribute(a._$Eu(e, n)))) return;
			this.C(e, t, n);
		}
		!1 === this.isUpdatePending && (this._$ES = this._$EP());
	}
	C(e, t, { useDefault: n, reflect: r, wrapped: i }, a) {
		n && !(this._$Ej ??= /* @__PURE__ */ new Map()).has(e) && (this._$Ej.set(e, a ?? t ?? this[e]), !0 !== i || a !== void 0) || (this._$AL.has(e) || (this.hasUpdated || n || (t = void 0), this._$AL.set(e, t)), !0 === r && this._$Em !== e && (this._$Eq ??= /* @__PURE__ */ new Set()).add(e));
	}
	async _$EP() {
		this.isUpdatePending = !0;
		try {
			await this._$ES;
		} catch (e) {
			Promise.reject(e);
		}
		let e = this.scheduleUpdate();
		return e != null && await e, !this.isUpdatePending;
	}
	scheduleUpdate() {
		return this.performUpdate();
	}
	performUpdate() {
		if (!this.isUpdatePending) return;
		if (!this.hasUpdated) {
			if (this.renderRoot ??= this.createRenderRoot(), this._$Ep) {
				for (let [e, t] of this._$Ep) this[e] = t;
				this._$Ep = void 0;
			}
			let e = this.constructor.elementProperties;
			if (e.size > 0) for (let [t, n] of e) {
				let { wrapped: e } = n, r = this[t];
				!0 !== e || this._$AL.has(t) || r === void 0 || this.C(t, void 0, n, r);
			}
		}
		let e = !1, t = this._$AL;
		try {
			e = this.shouldUpdate(t), e ? (this.willUpdate(t), this._$EO?.forEach((e) => e.hostUpdate?.()), this.update(t)) : this._$EM();
		} catch (t) {
			throw e = !1, this._$EM(), t;
		}
		e && this._$AE(t);
	}
	willUpdate(e) {}
	_$AE(e) {
		this._$EO?.forEach((e) => e.hostUpdated?.()), this.hasUpdated || (this.hasUpdated = !0, this.firstUpdated(e)), this.updated(e);
	}
	_$EM() {
		this._$AL = /* @__PURE__ */ new Map(), this.isUpdatePending = !1;
	}
	get updateComplete() {
		return this.getUpdateComplete();
	}
	getUpdateComplete() {
		return this._$ES;
	}
	shouldUpdate(e) {
		return !0;
	}
	update(e) {
		this._$Eq &&= this._$Eq.forEach((e) => this._$ET(e, this[e])), this._$EM();
	}
	updated(e) {}
	firstUpdated(e) {}
};
se.elementStyles = [], se.shadowRootOptions = { mode: "open" }, se[ie("elementProperties")] = /* @__PURE__ */ new Map(), se[ie("finalized")] = /* @__PURE__ */ new Map(), _?.({ ReactiveElement: se }), (ne.reactiveElementVersions ??= []).push("2.1.2");
//#endregion
//#region node_modules/lit-html/lit-html.js
var ce = globalThis, le = (e) => e, ue = ce.trustedTypes, de = ue ? ue.createPolicy("lit-html", { createHTML: (e) => e }) : void 0, fe = "$lit$", pe = `lit$${Math.random().toFixed(9).slice(2)}$`, me = "?" + pe, he = `<${me}>`, ge = document, _e = () => ge.createComment(""), ve = (e) => e === null || typeof e != "object" && typeof e != "function", ye = Array.isArray, be = (e) => ye(e) || typeof e?.[Symbol.iterator] == "function", xe = "[ 	\n\f\r]", Se = /<(?:(!--|\/[^a-zA-Z])|(\/?[a-zA-Z][^>\s]*)|(\/?$))/g, Ce = /-->/g, we = />/g, Te = RegExp(`>|${xe}(?:([^\\s"'>=/]+)(${xe}*=${xe}*(?:[^ \t\n\f\r"'\`<>=]|("|')|))|$)`, "g"), Ee = /'/g, De = /"/g, Oe = /^(?:script|style|textarea|title)$/i, ke = (e) => (t, ...n) => ({
	_$litType$: e,
	strings: t,
	values: n
}), y = ke(1), Ae = ke(2), je = Symbol.for("lit-noChange"), b = Symbol.for("lit-nothing"), Me = /* @__PURE__ */ new WeakMap(), Ne = ge.createTreeWalker(ge, 129);
function Pe(e, t) {
	if (!ye(e) || !e.hasOwnProperty("raw")) throw Error("invalid template strings array");
	return de === void 0 ? t : de.createHTML(t);
}
var Fe = (e, t) => {
	let n = e.length - 1, r = [], i, a = t === 2 ? "<svg>" : t === 3 ? "<math>" : "", o = Se;
	for (let t = 0; t < n; t++) {
		let n = e[t], s, c, l = -1, u = 0;
		for (; u < n.length && (o.lastIndex = u, c = o.exec(n), c !== null);) u = o.lastIndex, o === Se ? c[1] === "!--" ? o = Ce : c[1] === void 0 ? c[2] === void 0 ? c[3] !== void 0 && (o = Te) : (Oe.test(c[2]) && (i = RegExp("</" + c[2], "g")), o = Te) : o = we : o === Te ? c[0] === ">" ? (o = i ?? Se, l = -1) : c[1] === void 0 ? l = -2 : (l = o.lastIndex - c[2].length, s = c[1], o = c[3] === void 0 ? Te : c[3] === "\"" ? De : Ee) : o === De || o === Ee ? o = Te : o === Ce || o === we ? o = Se : (o = Te, i = void 0);
		let d = o === Te && e[t + 1].startsWith("/>") ? " " : "";
		a += o === Se ? n + he : l >= 0 ? (r.push(s), n.slice(0, l) + fe + n.slice(l) + pe + d) : n + pe + (l === -2 ? t : d);
	}
	return [Pe(e, a + (e[n] || "<?>") + (t === 2 ? "</svg>" : t === 3 ? "</math>" : "")), r];
}, Ie = class e {
	constructor({ strings: t, _$litType$: n }, r) {
		let i;
		this.parts = [];
		let a = 0, o = 0, s = t.length - 1, c = this.parts, [l, u] = Fe(t, n);
		if (this.el = e.createElement(l, r), Ne.currentNode = this.el.content, n === 2 || n === 3) {
			let e = this.el.content.firstChild;
			e.replaceWith(...e.childNodes);
		}
		for (; (i = Ne.nextNode()) !== null && c.length < s;) {
			if (i.nodeType === 1) {
				if (i.hasAttributes()) for (let e of i.getAttributeNames()) if (e.endsWith(fe)) {
					let t = u[o++], n = i.getAttribute(e).split(pe), r = /([.?@])?(.*)/.exec(t);
					c.push({
						type: 1,
						index: a,
						name: r[2],
						strings: n,
						ctor: r[1] === "." ? Ve : r[1] === "?" ? He : r[1] === "@" ? Ue : Be
					}), i.removeAttribute(e);
				} else e.startsWith(pe) && (c.push({
					type: 6,
					index: a
				}), i.removeAttribute(e));
				if (Oe.test(i.tagName)) {
					let e = i.textContent.split(pe), t = e.length - 1;
					if (t > 0) {
						i.textContent = ue ? ue.emptyScript : "";
						for (let n = 0; n < t; n++) i.append(e[n], _e()), Ne.nextNode(), c.push({
							type: 2,
							index: ++a
						});
						i.append(e[t], _e());
					}
				}
			} else if (i.nodeType === 8) {
				if (i.data === me) c.push({
					type: 2,
					index: a
				});
				else {
					let e = -1;
					for (; (e = i.data.indexOf(pe, e + 1)) !== -1;) c.push({
						type: 7,
						index: a
					}), e += pe.length - 1;
				}
			}
			a++;
		}
	}
	static createElement(e, t) {
		let n = ge.createElement("template");
		return n.innerHTML = e, n;
	}
};
function Le(e, t, n = e, r) {
	if (t === je) return t;
	let i = r === void 0 ? n._$Cl : n._$Co?.[r], a = ve(t) ? void 0 : t._$litDirective$;
	return i?.constructor !== a && (i?._$AO?.(!1), a === void 0 ? i = void 0 : (i = new a(e), i._$AT(e, n, r)), r === void 0 ? n._$Cl = i : (n._$Co ??= [])[r] = i), i !== void 0 && (t = Le(e, i._$AS(e, t.values), i, r)), t;
}
var Re = class {
	constructor(e, t) {
		this._$AV = [], this._$AN = void 0, this._$AD = e, this._$AM = t;
	}
	get parentNode() {
		return this._$AM.parentNode;
	}
	get _$AU() {
		return this._$AM._$AU;
	}
	u(e) {
		let { el: { content: t }, parts: n } = this._$AD, r = (e?.creationScope ?? ge).importNode(t, !0);
		Ne.currentNode = r;
		let i = Ne.nextNode(), a = 0, o = 0, s = n[0];
		for (; s !== void 0;) {
			if (a === s.index) {
				let t;
				s.type === 2 ? t = new ze(i, i.nextSibling, this, e) : s.type === 1 ? t = new s.ctor(i, s.name, s.strings, this, e) : s.type === 6 && (t = new We(i, this, e)), this._$AV.push(t), s = n[++o];
			}
			a !== s?.index && (i = Ne.nextNode(), a++);
		}
		return Ne.currentNode = ge, r;
	}
	p(e) {
		let t = 0;
		for (let n of this._$AV) n !== void 0 && (n.strings === void 0 ? n._$AI(e[t]) : (n._$AI(e, n, t), t += n.strings.length - 2)), t++;
	}
}, ze = class e {
	get _$AU() {
		return this._$AM?._$AU ?? this._$Cv;
	}
	constructor(e, t, n, r) {
		this.type = 2, this._$AH = b, this._$AN = void 0, this._$AA = e, this._$AB = t, this._$AM = n, this.options = r, this._$Cv = r?.isConnected ?? !0;
	}
	get parentNode() {
		let e = this._$AA.parentNode, t = this._$AM;
		return t !== void 0 && e?.nodeType === 11 && (e = t.parentNode), e;
	}
	get startNode() {
		return this._$AA;
	}
	get endNode() {
		return this._$AB;
	}
	_$AI(e, t = this) {
		e = Le(this, e, t), ve(e) ? e === b || e == null || e === "" ? (this._$AH !== b && this._$AR(), this._$AH = b) : e !== this._$AH && e !== je && this._(e) : e._$litType$ === void 0 ? e.nodeType === void 0 ? be(e) ? this.k(e) : this._(e) : this.T(e) : this.$(e);
	}
	O(e) {
		return this._$AA.parentNode.insertBefore(e, this._$AB);
	}
	T(e) {
		this._$AH !== e && (this._$AR(), this._$AH = this.O(e));
	}
	_(e) {
		this._$AH !== b && ve(this._$AH) ? this._$AA.nextSibling.data = e : this.T(ge.createTextNode(e)), this._$AH = e;
	}
	$(e) {
		let { values: t, _$litType$: n } = e, r = typeof n == "number" ? this._$AC(e) : (n.el === void 0 && (n.el = Ie.createElement(Pe(n.h, n.h[0]), this.options)), n);
		if (this._$AH?._$AD === r) this._$AH.p(t);
		else {
			let e = new Re(r, this), n = e.u(this.options);
			e.p(t), this.T(n), this._$AH = e;
		}
	}
	_$AC(e) {
		let t = Me.get(e.strings);
		return t === void 0 && Me.set(e.strings, t = new Ie(e)), t;
	}
	k(t) {
		ye(this._$AH) || (this._$AH = [], this._$AR());
		let n = this._$AH, r, i = 0;
		for (let a of t) i === n.length ? n.push(r = new e(this.O(_e()), this.O(_e()), this, this.options)) : r = n[i], r._$AI(a), i++;
		i < n.length && (this._$AR(r && r._$AB.nextSibling, i), n.length = i);
	}
	_$AR(e = this._$AA.nextSibling, t) {
		for (this._$AP?.(!1, !0, t); e !== this._$AB;) {
			let t = le(e).nextSibling;
			le(e).remove(), e = t;
		}
	}
	setConnected(e) {
		this._$AM === void 0 && (this._$Cv = e, this._$AP?.(e));
	}
}, Be = class {
	get tagName() {
		return this.element.tagName;
	}
	get _$AU() {
		return this._$AM._$AU;
	}
	constructor(e, t, n, r, i) {
		this.type = 1, this._$AH = b, this._$AN = void 0, this.element = e, this.name = t, this._$AM = r, this.options = i, n.length > 2 || n[0] !== "" || n[1] !== "" ? (this._$AH = Array(n.length - 1).fill(/* @__PURE__ */ new String()), this.strings = n) : this._$AH = b;
	}
	_$AI(e, t = this, n, r) {
		let i = this.strings, a = !1;
		if (i === void 0) e = Le(this, e, t, 0), a = !ve(e) || e !== this._$AH && e !== je, a && (this._$AH = e);
		else {
			let r = e, o, s;
			for (e = i[0], o = 0; o < i.length - 1; o++) s = Le(this, r[n + o], t, o), s === je && (s = this._$AH[o]), a ||= !ve(s) || s !== this._$AH[o], s === b ? e = b : e !== b && (e += (s ?? "") + i[o + 1]), this._$AH[o] = s;
		}
		a && !r && this.j(e);
	}
	j(e) {
		e === b ? this.element.removeAttribute(this.name) : this.element.setAttribute(this.name, e ?? "");
	}
}, Ve = class extends Be {
	constructor() {
		super(...arguments), this.type = 3;
	}
	j(e) {
		this.element[this.name] = e === b ? void 0 : e;
	}
}, He = class extends Be {
	constructor() {
		super(...arguments), this.type = 4;
	}
	j(e) {
		this.element.toggleAttribute(this.name, !!e && e !== b);
	}
}, Ue = class extends Be {
	constructor(e, t, n, r, i) {
		super(e, t, n, r, i), this.type = 5;
	}
	_$AI(e, t = this) {
		if ((e = Le(this, e, t, 0) ?? b) === je) return;
		let n = this._$AH, r = e === b && n !== b || e.capture !== n.capture || e.once !== n.once || e.passive !== n.passive, i = e !== b && (n === b || r);
		r && this.element.removeEventListener(this.name, this, n), i && this.element.addEventListener(this.name, this, e), this._$AH = e;
	}
	handleEvent(e) {
		typeof this._$AH == "function" ? this._$AH.call(this.options?.host ?? this.element, e) : this._$AH.handleEvent(e);
	}
}, We = class {
	constructor(e, t, n) {
		this.element = e, this.type = 6, this._$AN = void 0, this._$AM = t, this.options = n;
	}
	get _$AU() {
		return this._$AM._$AU;
	}
	_$AI(e) {
		Le(this, e);
	}
}, Ge = ce.litHtmlPolyfillSupport;
Ge?.(Ie, ze), (ce.litHtmlVersions ??= []).push("3.3.3");
var Ke = (e, t, n) => {
	let r = n?.renderBefore ?? t, i = r._$litPart$;
	if (i === void 0) {
		let e = n?.renderBefore ?? null;
		r._$litPart$ = i = new ze(t.insertBefore(_e(), e), e, void 0, n ?? {});
	}
	return i._$AI(e), i;
}, qe = globalThis, x = class extends se {
	constructor() {
		super(...arguments), this.renderOptions = { host: this }, this._$Do = void 0;
	}
	createRenderRoot() {
		let e = super.createRenderRoot();
		return this.renderOptions.renderBefore ??= e.firstChild, e;
	}
	update(e) {
		let t = this.render();
		this.hasUpdated || (this.renderOptions.isConnected = this.isConnected), super.update(e), this._$Do = Ke(t, this.renderRoot, this.renderOptions);
	}
	connectedCallback() {
		super.connectedCallback(), this._$Do?.setConnected(!0);
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this._$Do?.setConnected(!1);
	}
	render() {
		return je;
	}
};
x._$litElement$ = !0, x.finalized = !0, qe.litElementHydrateSupport?.({ LitElement: x });
var Je = qe.litElementPolyfillSupport;
Je?.({ LitElement: x }), (qe.litElementVersions ??= []).push("4.2.2");
//#endregion
//#region node_modules/@lit/reactive-element/decorators/property.js
var Ye = {
	attribute: !0,
	type: String,
	converter: v,
	reflect: !1,
	hasChanged: ae
}, Xe = (e = Ye, t, n) => {
	let { kind: r, metadata: i } = n, a = globalThis.litPropertyMetadata.get(i);
	if (a === void 0 && globalThis.litPropertyMetadata.set(i, a = /* @__PURE__ */ new Map()), r === "setter" && ((e = Object.create(e)).wrapped = !0), a.set(n.name, e), r === "accessor") {
		let { name: r } = n;
		return {
			set(n) {
				let i = t.get.call(this);
				t.set.call(this, n), this.requestUpdate(r, i, e, !0, n);
			},
			init(t) {
				return t !== void 0 && this.C(r, void 0, e, t), t;
			}
		};
	}
	if (r === "setter") {
		let { name: r } = n;
		return function(n) {
			let i = this[r];
			t.call(this, n), this.requestUpdate(r, i, e, !0, n);
		};
	}
	throw Error("Unsupported decorator location: " + r);
};
function S(e) {
	return (t, n) => typeof n == "object" ? Xe(e, t, n) : ((e, t, n) => {
		let r = t.hasOwnProperty(n);
		return t.constructor.createProperty(n, e), r ? Object.getOwnPropertyDescriptor(t, n) : void 0;
	})(e, t, n);
}
//#endregion
//#region node_modules/@lit/reactive-element/decorators/state.js
function Ze(e) {
	return S({
		...e,
		state: !0,
		attribute: !1
	});
}
//#endregion
//#region node_modules/@lit/reactive-element/decorators/base.js
var Qe = (e, t, n) => (n.configurable = !0, n.enumerable = !0, Reflect.decorate && typeof t != "object" && Object.defineProperty(e, t, n), n);
//#endregion
//#region node_modules/@lit/reactive-element/decorators/query.js
function C(e, t) {
	return (n, r, i) => {
		let a = (t) => t.renderRoot?.querySelector(e) ?? null;
		if (t) {
			let { get: e, set: t } = typeof r == "object" ? n : i ?? (() => {
				let e = Symbol();
				return {
					get() {
						return this[e];
					},
					set(t) {
						this[e] = t;
					}
				};
			})();
			return Qe(n, r, { get() {
				let n = e.call(this);
				return n === void 0 && (n = a(this), (n !== null || this.hasUpdated) && t.call(this, n)), n;
			} });
		}
		return Qe(n, r, { get() {
			return a(this);
		} });
	};
}
//#endregion
//#region node_modules/@lit/reactive-element/decorators/query-assigned-elements.js
function $e(e) {
	return (t, n) => {
		let { slot: r, selector: i } = e ?? {}, a = "slot" + (r ? `[name=${r}]` : ":not([name])");
		return Qe(t, n, { get() {
			let t = (this.renderRoot?.querySelector(a))?.assignedElements(e) ?? [];
			return i === void 0 ? t : t.filter((e) => e.matches(i));
		} });
	};
}
//#endregion
//#region node_modules/lit-html/directive.js
var et = {
	ATTRIBUTE: 1,
	CHILD: 2,
	PROPERTY: 3,
	BOOLEAN_ATTRIBUTE: 4,
	EVENT: 5,
	ELEMENT: 6
}, tt = (e) => (...t) => ({
	_$litDirective$: e,
	values: t
}), nt = class {
	constructor(e) {}
	get _$AU() {
		return this._$AM._$AU;
	}
	_$AT(e, t, n) {
		this._$Ct = e, this._$AM = t, this._$Ci = n;
	}
	_$AS(e, t) {
		return this.update(e, t);
	}
	update(e, t) {
		return this.render(...t);
	}
}, rt = (e) => e ?? b, it, at, ot, st, ct, lt = class {
	constructor(e, r) {
		it.set(this, null), at.set(this, 0), ot.set(this, !1), st.set(this, void 0), ct.set(this, () => {
			if (!t(this, ot, "f")) return;
			let e = performance.now();
			t(this, st, "f").call(this, (e - t(this, at, "f")) / 1e3, e / 1e3), n(this, at, e, "f"), n(this, it, requestAnimationFrame(t(this, ct, "f")), "f");
		}), n(this, st, r, "f"), e.addController(this);
	}
	hostDisconnected() {
		this.stop();
	}
	start() {
		t(this, ot, "f") || (n(this, ot, !0, "f"), n(this, at, performance.now(), "f"), t(this, ct, "f").call(this));
	}
	stop() {
		t(this, ot, "f") && (n(this, ot, !1, "f"), t(this, it, "f") !== null && (cancelAnimationFrame(t(this, it, "f")), n(this, it, null, "f")));
	}
};
it = /* @__PURE__ */ new WeakMap(), at = /* @__PURE__ */ new WeakMap(), ot = /* @__PURE__ */ new WeakMap(), st = /* @__PURE__ */ new WeakMap(), ct = /* @__PURE__ */ new WeakMap();
var ut, dt, ft, pt = class {
	constructor(e, r) {
		ut.set(this, void 0), dt.set(this, void 0), ft.set(this, /* @__PURE__ */ new Set()), n(this, ut, e, "f"), n(this, dt, r.target, "f"), t(this, ut, "f").addController(this);
	}
	get targets() {
		return t(this, ft, "f").values();
	}
	get hasTargets() {
		return t(this, ft, "f").size > 0;
	}
	hostConnected() {
		t(this, dt, "f") !== null && this.observe(t(this, dt, "f") ?? t(this, ut, "f"));
	}
	hostDisconnected() {
		this.unobserveAll();
	}
	observe(e) {
		t(this, ft, "f").has(e) || (t(this, ft, "f").add(e), this._observe(e));
	}
	isObserving(e) {
		return t(this, ft, "f").has(e);
	}
	unobserve(e) {
		t(this, ft, "f").delete(e) && this._unobserve(e);
	}
	unobserveAll() {
		t(this, ft, "f").forEach((e) => this.unobserve(e)), t(this, ft, "f").clear();
	}
};
ut = /* @__PURE__ */ new WeakMap(), dt = /* @__PURE__ */ new WeakMap(), ft = /* @__PURE__ */ new WeakMap();
var mt, ht, gt, _t, vt, yt = class extends pt {
	constructor(e, r) {
		super(e, r), mt.add(this), ht.set(this, void 0), gt.set(this, (e) => t(this, mt, "m", vt).call(this, e)), _t.set(this, !1), n(this, ht, r.callback, "f");
	}
	_observe() {
		t(this, _t, "f") || (document.addEventListener("click", t(this, gt, "f")), n(this, _t, !0, "f"));
	}
	_unobserve() {
		!this.hasTargets && t(this, _t, "f") && (document.removeEventListener("click", t(this, gt, "f")), n(this, _t, !1, "f"));
	}
};
ht = /* @__PURE__ */ new WeakMap(), gt = /* @__PURE__ */ new WeakMap(), _t = /* @__PURE__ */ new WeakMap(), mt = /* @__PURE__ */ new WeakSet(), vt = function(e) {
	let n = e.composedPath();
	n.some((e) => e instanceof HTMLElement && this.isObserving(e)) || t(this, ht, "f").call(this, n);
};
function bt(e, t) {
	if (!t) return 0;
	let n = document.createElement("div");
	n.style.all = "inherit", n.style.width = t, n.style.position = "absolute", n.style.visibility = "hidden", n.style.pointerEvents = "none", e.appendChild(n);
	try {
		return parseFloat(getComputedStyle(n).width);
	} finally {
		n.remove();
	}
}
function xt(e) {
	let t = parseFloat(getComputedStyle(e).lineHeight);
	return isNaN(t) || t <= 0 ? 0 : Math.round(e.scrollHeight / t);
}
async function St(e, t = 200) {
	e.focus();
	function n(e) {
		let t = e.getRootNode();
		return t instanceof ShadowRoot ? t.activeElement === e : document.activeElement === e;
	}
	let r = performance.now();
	for (; !n(e);) {
		if (!e.isConnected || performance.now() - r > t) return !1;
		await new Promise(requestAnimationFrame), e.focus();
	}
	return !0;
}
function Ct() {
	return matchMedia("(forced-colors: active)").matches;
}
function wt(e, t, n) {
	return e + (t - e) * n;
}
function Tt(e, t, n, r) {
	return {
		x: (1 - r) ** 2 * e.x + 2 * (1 - r) * r * t.x + r ** 2 * n.x,
		y: (1 - r) ** 2 * e.y + 2 * (1 - r) * r * t.y + r ** 2 * n.y
	};
}
function Et(e, t, n, r, i) {
	return {
		x: (1 - i) ** 3 * e.x + 3 * (1 - i) ** 2 * i * t.x + 3 * (1 - i) * i ** 2 * n.x + i ** 3 * r.x,
		y: (1 - i) ** 3 * e.y + 3 * (1 - i) ** 2 * i * t.y + 3 * (1 - i) * i ** 2 * n.y + i ** 3 * r.y
	};
}
function Dt(e) {
	return (e.match(/[a-zA-Z][^a-zA-Z]*/g) || []).map((e) => ({
		type: e[0],
		nums: e.slice(1).trim().split(/[\s,]+/).filter(Boolean).map(Number)
	}));
}
function Ot(e, t) {
	let n = Dt(e), r = {
		x: 0,
		y: 0
	}, i = {
		x: 0,
		y: 0
	}, a = [];
	for (let e of n) if (e.type === "M") r = {
		x: e.nums[0],
		y: e.nums[1]
	}, i = r;
	else if (e.type === "L") {
		let t = {
			x: e.nums[0],
			y: e.nums[1]
		}, n = t.x - r.x, i = t.y - r.y;
		a.push({
			type: "L",
			pts: [r, t],
			length: Math.hypot(n, i)
		}), r = t;
	} else if (e.type === "H") {
		let t = {
			x: e.nums[0],
			y: r.y
		}, n = t.x - r.x;
		a.push({
			type: "L",
			pts: [r, t],
			length: Math.abs(n)
		}), r = t;
	} else if (e.type === "V") {
		let t = {
			x: r.x,
			y: e.nums[0]
		}, n = t.y - r.y;
		a.push({
			type: "L",
			pts: [r, t],
			length: Math.abs(n)
		}), r = t;
	} else if (e.type === "Q") {
		let t = {
			x: e.nums[0],
			y: e.nums[1]
		}, n = {
			x: e.nums[2],
			y: e.nums[3]
		}, i = 0, o = r;
		for (let e = 1; e <= 20; e++) {
			let a = e / 20, s = Tt(r, t, n, a);
			i += Math.hypot(s.x - o.x, s.y - o.y), o = s;
		}
		a.push({
			type: "Q",
			pts: [
				r,
				t,
				n
			],
			length: i
		}), r = n;
	} else if (e.type === "C") {
		let t = {
			x: e.nums[0],
			y: e.nums[1]
		}, n = {
			x: e.nums[2],
			y: e.nums[3]
		}, i = {
			x: e.nums[4],
			y: e.nums[5]
		}, o = 0, s = r;
		for (let e = 1; e <= 20; e++) {
			let a = e / 20, c = Et(r, t, n, i, a);
			o += Math.hypot(c.x - s.x, c.y - s.y), s = c;
		}
		a.push({
			type: "C",
			pts: [
				r,
				t,
				n,
				i
			],
			length: o
		}), r = i;
	} else if (e.type === "Z") {
		let e = i.x - r.x, t = i.y - r.y;
		a.push({
			type: "L",
			pts: [r, i],
			length: Math.hypot(e, t)
		}), r = i;
	}
	let o = a.reduce((e, t) => e + t.length, 0) / (t - 1), s = [], c = 0, l = 0;
	for (let e = 0; e < t; e++) {
		let t = e * o;
		for (; l < a.length && c + a[l].length < t;) c += a[l].length, l++;
		let n = a[l];
		if (!n) break;
		let r = (t - c) / n.length;
		if (n.type === "L") {
			let [e, t] = n.pts;
			s.push({
				x: wt(e.x, t.x, r),
				y: wt(e.y, t.y, r)
			});
		} else if (n.type === "Q") {
			let [e, t, i] = n.pts;
			s.push(Tt(e, t, i, r));
		} else if (n.type === "C") {
			let [e, t, i, a] = n.pts;
			s.push(Et(e, t, i, a, r));
		}
	}
	return s;
}
function kt(e) {
	return `${e.map((e) => {
		let t = At(e.x * 100, 0, 100), n = At(e.y * 100, 0, 100);
		return `${t.toFixed(2)}% ${n.toFixed(2)}%`;
	}).join(", ")}`;
}
function At(e, t, n) {
	return Math.max(t, Math.min(n, e));
}
function jt(e) {
	let t = Infinity, n = Infinity, r = -Infinity, i = -Infinity;
	for (let a of e) a.x < t && (t = a.x), a.x > r && (r = a.x), a.y < n && (n = a.y), a.y > i && (i = a.y);
	return {
		minX: t,
		minY: n,
		maxX: r,
		maxY: i,
		width: r - t,
		height: i - n
	};
}
function Mt(e, t) {
	let n = e.length, r = Array(n);
	for (let i = 0; i < n; i++) r[i] = e[(i + t) % n];
	return r;
}
function Nt(e, t) {
	let n = Math.min(t.length, e.length), r = 0, i = Infinity;
	for (let a = 0; a < n; a++) {
		let o = 0;
		for (let r = 0; r < n; r++) {
			let s = t[(r + a) % n], c = e[r], l = s.x - c.x, u = s.y - c.y;
			if (o += l * l + u * u, o >= i) break;
		}
		o < i && (i = o, r = a);
	}
	return r;
}
function Pt(e) {
	let t = 0, n = e.length;
	for (let r = 0; r < n; r++) {
		let i = e[r], a = e[(r + 1) % n];
		t += i.x * a.y - a.x * i.y;
	}
	return t / 2;
}
function Ft(e) {
	let t = e.map((e) => {
		let t = jt(e), n = e.map((e) => ({
			x: e.x - t.minX,
			y: e.y - t.minY
		})), r = Math.max(t.width, t.height) || 1, i = n.map((e) => ({
			x: e.x / r,
			y: e.y / r
		})), a = t.width / r, o = t.height / r, s = (1 - a) / 2, c = (1 - o) / 2;
		return i.map((e) => ({
			x: e.x + s,
			y: e.y + c
		}));
	}), n = t[0], r = Math.sign(Pt(n));
	for (let e = 1; e < t.length; e++) {
		let i = t[e], a = Math.sign(Pt(i));
		a !== 0 && a !== r && (i = i.slice().reverse());
		let o = Nt(n, i);
		t[e] = Mt(i, o);
	}
	return t;
}
function It(e, t) {
	return Ft(e.map((e) => Ot(e, t))).map((e) => kt(e));
}
function Lt(e, t = !1) {
	let n = "";
	switch (e.nodeType) {
		case Node.TEXT_NODE:
			n = e.nodeValue ?? "";
			break;
		case Node.ELEMENT_NODE: if (e instanceof HTMLSlotElement) for (let r of e.assignedNodes({ flatten: !0 })) n += Lt(r, t);
		else for (let r of e.childNodes) n += Lt(r, t);
	}
	return t && (n = n.trim()), n;
}
function Rt(e) {
	return e.assignedNodes({ flatten: !0 }).length > 0;
}
function zt(e, t, n) {
	let r = Object.getOwnPropertyDescriptor(e, t) ?? Object.getOwnPropertyDescriptor(Object.getPrototypeOf(e), t);
	if (!r) throw Error(`Property ${String(t)} not found on target.`);
	let i = r.get?.bind(e), a = r.set?.bind(e);
	return Object.defineProperty(e, t, {
		configurable: !0,
		enumerable: r.enumerable,
		get() {
			return n.get ? n.get(() => i?.()) : i?.();
		},
		set(e) {
			n.set ? n.set(e, (e) => a?.(e)) : a?.(e);
		}
	}), () => Object.defineProperty(e, t, r);
}
function Bt() {
	return matchMedia("(prefers-reduced-motion)").matches;
}
function Vt(e) {
	if (typeof window > "u") return;
	let t = new CSSStyleSheet();
	t.replaceSync(e.toString()), document.adoptedStyleSheets = [...document.adoptedStyleSheets, t];
}
function Ht(e, t = document) {
	return new Promise((n) => {
		let r = t.querySelector(`#${e}`);
		r ? n(r) : document.readyState === "complete" || document.readyState === "interactive" ? n(t.querySelector(`#${e}`)) : document.addEventListener("DOMContentLoaded", () => n(t.querySelector(`#${e}`)), { once: !0 });
	});
}
function Ut(e) {
	let t = document?.location ?? null;
	return `url(${t ? (t.pathname + t.search).split("#")[0] : ""}#${e})`;
}
async function Wt(e) {
	e.isUpdatePending && await e.updateComplete;
}
async function Gt(e) {
	let t = e.tagName.toLowerCase();
	if (!t.includes("-")) return;
	customElements.get(t) || await customElements.whenDefined(t);
	let n = customElements.get(t);
	!n || e instanceof n || (await Promise.resolve(), !(e instanceof n) && await Promise.resolve());
}
var Kt, qt, Jt, Yt, Xt, Zt, Qt, $t, en, tn, nn, rn, an, on, sn = class extends pt {
	constructor(e, r) {
		super(e, r), Kt.add(this), Jt.set(this, !1), Yt.set(this, void 0), Xt.set(this, void 0), Zt.set(this, (e) => t(this, Kt, "m", rn).call(this, e)), Qt.set(this, (e) => t(this, Kt, "m", an).call(this, e)), $t.set(this, (e) => t(this, Kt, "m", on).call(this, e)), en.set(this, () => n(this, Jt, !0, "f")), tn.set(this, () => n(this, Jt, !1, "f")), n(this, Yt, r.callback, "f"), n(this, Xt, r.filter, "f");
	}
	_observe(e) {
		e.addEventListener("keydown", t(this, Zt, "f")), e.addEventListener("focusin", t(this, Qt, "f")), e.addEventListener("focusout", t(this, $t, "f")), e.addEventListener("touchstart", t(this, en, "f"), { passive: !0 }), e.addEventListener("touchend", t(this, tn, "f")), e.addEventListener("touchcancel", t(this, tn, "f"));
	}
	_unobserve(e) {
		e.removeEventListener("keydown", t(this, Zt, "f")), e.removeEventListener("focusin", t(this, Qt, "f")), e.removeEventListener("focusout", t(this, $t, "f")), e.removeEventListener("touchstart", t(this, en, "f")), e.removeEventListener("touchend", t(this, tn, "f")), e.removeEventListener("touchcancel", t(this, tn, "f"));
	}
};
qt = sn, Jt = /* @__PURE__ */ new WeakMap(), Yt = /* @__PURE__ */ new WeakMap(), Xt = /* @__PURE__ */ new WeakMap(), Zt = /* @__PURE__ */ new WeakMap(), Qt = /* @__PURE__ */ new WeakMap(), $t = /* @__PURE__ */ new WeakMap(), en = /* @__PURE__ */ new WeakMap(), tn = /* @__PURE__ */ new WeakMap(), Kt = /* @__PURE__ */ new WeakSet(), rn = function(e) {
	t(this, Xt, "f")?.call(this, e) || e.currentTarget.matches(":focus-within") && t(this, Kt, "m", an).call(this, e);
}, an = function(e) {
	if (t(this, Xt, "f")?.call(this, e) || t(this, Jt, "f")) return;
	let n = e.currentTarget;
	t(this, Yt, "f").call(this, !0, n.matches(":focus-visible") || t(qt, qt, "f", nn) || Ct(), n);
}, on = function(e) {
	t(this, Xt, "f")?.call(this, e) || t(this, Jt, "f") || t(this, Yt, "f").call(this, !1, !1, e.currentTarget);
}, typeof window < "u" && (window.addEventListener("keydown", () => n(qt, qt, !0, "f", nn), {
	capture: !0,
	passive: !0
}), window.addEventListener("pointerdown", () => n(qt, qt, !1, "f", nn), { capture: !0 })), nn = { value: !1 };
var cn, ln, un, dn, fn, pn, mn, hn, gn, _n, vn, yn = class extends pt {
	constructor(e, r) {
		super(e, r), cn.add(this), ln.set(this, void 0), un.set(this, /* @__PURE__ */ new Map()), dn.set(this, /* @__PURE__ */ new Map()), fn.set(this, (e) => t(this, cn, "m", _n).call(this, e)), pn.set(this, (e) => t(this, cn, "m", vn).call(this, e)), n(this, ln, r.callback, "f"), this.startDelay = r.startDelay ?? 0, this.endDelay = r.endDelay ?? 0;
	}
	clearDelays() {
		for (let e of this.targets) t(this, cn, "m", mn).call(this, e);
	}
	_observe(e) {
		e.addEventListener("pointerenter", t(this, fn, "f")), e.addEventListener("pointerleave", t(this, pn, "f")), e.addEventListener("touchend", t(this, pn, "f"));
	}
	_unobserve(e) {
		e.removeEventListener("pointerenter", t(this, fn, "f")), e.removeEventListener("pointerleave", t(this, pn, "f")), e.removeEventListener("touchend", t(this, pn, "f")), t(this, cn, "m", mn).call(this, e);
	}
};
ln = /* @__PURE__ */ new WeakMap(), un = /* @__PURE__ */ new WeakMap(), dn = /* @__PURE__ */ new WeakMap(), fn = /* @__PURE__ */ new WeakMap(), pn = /* @__PURE__ */ new WeakMap(), cn = /* @__PURE__ */ new WeakSet(), mn = function(e) {
	t(this, cn, "m", hn).call(this, e), t(this, cn, "m", gn).call(this, e);
}, hn = function(e) {
	return t(this, un, "f").has(e) ? (clearTimeout(t(this, un, "f").get(e)), t(this, un, "f").delete(e)) : !1;
}, gn = function(e) {
	return t(this, dn, "f").has(e) ? (clearTimeout(t(this, dn, "f").get(e)), t(this, dn, "f").delete(e)) : !1;
}, _n = function(e) {
	let n = e.target;
	t(this, cn, "m", gn).call(this, n), this.startDelay > 0 ? (t(this, cn, "m", hn).call(this, n), t(this, un, "f").set(n, setTimeout(() => {
		t(this, un, "f").delete(n), t(this, ln, "f").call(this, !0, n);
	}, this.startDelay))) : t(this, ln, "f").call(this, !0, n);
}, vn = function(e) {
	let n = e.target;
	t(this, cn, "m", hn).call(this, n) || (this.endDelay > 0 ? (t(this, cn, "m", gn).call(this, n), t(this, dn, "f").set(n, setTimeout(() => {
		t(this, dn, "f").delete(n), t(this, ln, "f").call(this, !1, n);
	}, this.endDelay))) : t(this, ln, "f").call(this, !1, n));
};
var bn, xn, Sn = class {
	constructor(e) {
		bn.set(this, void 0), xn.set(this, []), n(this, bn, e, "f"), e.addController(this);
	}
	lock() {
		this.unlock();
		for (let e = t(this, bn, "f"); e.parentNode && e.parentNode !== document.documentElement; e = e.parentNode) for (let n of e.parentNode.children) n instanceof HTMLElement && n !== e && !n.inert && (n.inert = !0, t(this, xn, "f").push(n));
	}
	unlock() {
		t(this, xn, "f").forEach((e) => e.inert = !1), t(this, xn, "f").length = 0;
	}
	hostDisconnected() {
		this.unlock();
	}
};
bn = /* @__PURE__ */ new WeakMap(), xn = /* @__PURE__ */ new WeakMap();
var Cn, wn, Tn, En, Dn, On, kn, An, jn, Mn, Nn, Pn, Fn = class extends pt {
	constructor(e, r) {
		super(e, r), Cn.add(this), wn.set(this, void 0), Tn.set(this, !1), En.set(this, void 0), Dn.set(this, void 0), On.set(this, !0), kn.set(this, /* @__PURE__ */ new Map()), An.set(this, /* @__PURE__ */ new Map()), n(this, wn, r.callback, "f"), n(this, Tn, r.skipInitial ?? !1, "f"), n(this, En, r.config, "f"), window.MutationObserver ? n(this, Dn, new MutationObserver((e, n) => t(this, wn, "f").call(this, e, n)), "f") : console.warn("MutationController error: the browser does not support MutationObserver.");
	}
	async hostUpdated() {
		if (t(this, Dn, "f") && !t(this, Tn, "f") && t(this, On, "f")) {
			let e = t(this, Dn, "f").takeRecords();
			e.length > 0 && t(this, wn, "f").call(this, e, t(this, Dn, "f"));
		}
		n(this, On, !1, "f");
	}
	hostDisconnected() {
		super.hostDisconnected(), t(this, Cn, "m", jn).call(this);
	}
	_observe(e) {
		e instanceof HTMLSlotElement ? t(this, Cn, "m", Mn).call(this, e) : (t(this, Dn, "f")?.observe(e, t(this, En, "f")), n(this, On, !0, "f"));
	}
	_unobserve() {
		t(this, Cn, "m", jn).call(this);
		for (let e of this.targets) e instanceof HTMLSlotElement ? t(this, Cn, "m", Mn).call(this, e) : t(this, Dn, "f")?.observe(e, t(this, En, "f"));
	}
};
wn = /* @__PURE__ */ new WeakMap(), Tn = /* @__PURE__ */ new WeakMap(), En = /* @__PURE__ */ new WeakMap(), Dn = /* @__PURE__ */ new WeakMap(), On = /* @__PURE__ */ new WeakMap(), kn = /* @__PURE__ */ new WeakMap(), An = /* @__PURE__ */ new WeakMap(), Cn = /* @__PURE__ */ new WeakSet(), jn = function() {
	t(this, Dn, "f")?.disconnect();
	for (let [e] of t(this, kn, "f")) t(this, Cn, "m", Nn).call(this, e);
	t(this, kn, "f").clear(), t(this, An, "f").clear();
}, Mn = function(e) {
	if (t(this, kn, "f").has(e)) return;
	let n = () => t(this, Cn, "m", Pn).call(this, e);
	e.addEventListener("slotchange", n), t(this, kn, "f").set(e, () => e.removeEventListener("slotchange", n)), t(this, Cn, "m", Pn).call(this, e);
}, Nn = function(e) {
	let n = t(this, kn, "f").get(e);
	n && (n(), t(this, kn, "f").delete(e)), t(this, An, "f").delete(e);
}, Pn = function(e) {
	t(this, Dn, "f")?.disconnect();
	let r = e.assignedElements({ flatten: !0 });
	t(this, An, "f").set(e, r);
	for (let e of r) t(this, Dn, "f")?.observe(e, t(this, En, "f"));
	for (let e of this.targets) e instanceof HTMLSlotElement || t(this, Dn, "f")?.observe(e, t(this, En, "f"));
	n(this, On, !0, "f");
};
var In, Ln, Rn, zn, Bn, Vn, Hn, Un, Wn, Gn, Kn, qn, Jn, Yn, Xn, Zn, Qn, $n, er, tr, nr = class extends pt {
	constructor(e, r) {
		super(e, r), In.add(this), Ln.set(this, void 0), Rn.set(this, void 0), zn.set(this, void 0), Bn.set(this, void 0), Vn.set(this, /* @__PURE__ */ new Map()), Hn.set(this, /* @__PURE__ */ new Map()), Un.set(this, void 0), Wn.set(this, (e) => t(this, In, "m", Yn).call(this, e)), Gn.set(this, (e) => t(this, In, "m", Xn).call(this, e)), Kn.set(this, (e) => t(this, In, "m", Zn).call(this, e)), qn.set(this, (e) => t(this, In, "m", Qn).call(this, e)), Jn.set(this, (e) => t(this, In, "m", $n).call(this, e)), n(this, Ln, r.capture, "f"), n(this, Rn, r.callback, "f"), n(this, zn, r.filter, "f"), n(this, Bn, r.isPressedKey, "f"), n(this, Un, r.minPressedDuration ?? 0, "f");
	}
	hostConnected() {
		document.addEventListener("pointerup", t(this, Gn, "f"), { capture: t(this, Ln, "f") }), document.addEventListener("touchend", t(this, Kn, "f"), { capture: t(this, Ln, "f") }), document.addEventListener("touchcancel", t(this, Kn, "f"), { capture: t(this, Ln, "f") }), super.hostConnected();
	}
	hostDisconnected() {
		document.removeEventListener("pointerup", t(this, Gn, "f"), { capture: t(this, Ln, "f") }), document.removeEventListener("touchend", t(this, Kn, "f"), { capture: t(this, Ln, "f") }), document.removeEventListener("touchcancel", t(this, Kn, "f"), { capture: t(this, Ln, "f") }), super.hostDisconnected();
		for (let e of t(this, Hn, "f").values()) clearTimeout(e);
		t(this, Hn, "f").clear(), t(this, Vn, "f").clear();
	}
	_observe(e) {
		e.addEventListener("pointerdown", t(this, Wn, "f"), { capture: t(this, Ln, "f") }), t(this, Bn, "f") && (e.addEventListener("keydown", t(this, qn, "f"), { capture: t(this, Ln, "f") }), e.addEventListener("keyup", t(this, Jn, "f"), { capture: t(this, Ln, "f") }));
	}
	_unobserve(e) {
		e.removeEventListener("pointerdown", t(this, Wn, "f"), { capture: t(this, Ln, "f") }), t(this, Bn, "f") && (e.removeEventListener("keydown", t(this, qn, "f"), { capture: t(this, Ln, "f") }), e.removeEventListener("keyup", t(this, Jn, "f"), { capture: t(this, Ln, "f") })), t(this, In, "m", tr).call(this, e), t(this, Vn, "f").delete(e);
	}
};
Ln = /* @__PURE__ */ new WeakMap(), Rn = /* @__PURE__ */ new WeakMap(), zn = /* @__PURE__ */ new WeakMap(), Bn = /* @__PURE__ */ new WeakMap(), Vn = /* @__PURE__ */ new WeakMap(), Hn = /* @__PURE__ */ new WeakMap(), Un = /* @__PURE__ */ new WeakMap(), Wn = /* @__PURE__ */ new WeakMap(), Gn = /* @__PURE__ */ new WeakMap(), Kn = /* @__PURE__ */ new WeakMap(), qn = /* @__PURE__ */ new WeakMap(), Jn = /* @__PURE__ */ new WeakMap(), In = /* @__PURE__ */ new WeakSet(), Yn = function(e) {
	if (!t(this, zn, "f")?.call(this, e) && !(e.pointerType === "mouse" && e.button > 1)) {
		for (let n of e.composedPath()) if (n instanceof HTMLElement && this.isObserving(n)) {
			t(this, In, "m", tr).call(this, n), t(this, Vn, "f").set(n, performance.now()), t(this, Rn, "f").call(this, !0, {
				x: e.x,
				y: e.y
			}, n);
			break;
		}
	}
}, Xn = function(e) {
	e.pointerType === "mouse" && e.button > 1 || t(this, In, "m", er).call(this, e.x, e.y);
}, Zn = function(e) {
	t(this, In, "m", er).call(this, e.changedTouches[0]?.clientX ?? 0, e.changedTouches[0]?.clientY ?? 0);
}, Qn = function(e) {
	if (t(this, zn, "f")?.call(this, e) || e.target !== e.currentTarget) return;
	let n = e.currentTarget;
	if (t(this, Bn, "f")?.call(this, e.key) && (e.key === " " && e.preventDefault(), !e.repeat)) {
		t(this, In, "m", tr).call(this, n), t(this, Vn, "f").set(n, performance.now());
		let e = n.getBoundingClientRect();
		t(this, Rn, "f").call(this, !0, {
			x: e.x + e.width / 2,
			y: e.y + e.height / 2
		}, n);
	}
}, $n = function(e) {
	if (e.target !== e.currentTarget) return;
	let n = e.currentTarget;
	if (t(this, Vn, "f").has(n) && t(this, Bn, "f")?.call(this, e.key)) {
		t(this, In, "m", tr).call(this, n);
		let e = t(this, Un, "f") - (performance.now() - t(this, Vn, "f").get(n)), r = n.getBoundingClientRect();
		e > 0 ? t(this, Hn, "f").set(n, setTimeout(() => {
			t(this, Vn, "f").delete(n), t(this, Hn, "f").delete(n), t(this, Rn, "f").call(this, !1, {
				x: r.x + r.width / 2,
				y: r.y + r.height / 2
			}, n);
		}, e)) : (t(this, Vn, "f").delete(n), t(this, Rn, "f").call(this, !1, {
			x: r.x + r.width / 2,
			y: r.y + r.height / 2
		}, n));
	}
}, er = function(e, n) {
	for (let r of t(this, Vn, "f")) {
		t(this, In, "m", tr).call(this, r[0]);
		let i = t(this, Un, "f") - (performance.now() - r[1]);
		i > 0 ? t(this, Hn, "f").set(r[0], setTimeout(() => {
			t(this, Vn, "f").delete(r[0]), t(this, Hn, "f").delete(r[0]), t(this, Rn, "f").call(this, !1, {
				x: e,
				y: n
			}, r[0]);
		}, i)) : (t(this, Vn, "f").delete(r[0]), t(this, Rn, "f").call(this, !1, {
			x: e,
			y: n
		}, r[0]));
	}
}, tr = function(e) {
	let n = t(this, Hn, "f").get(e);
	n !== void 0 && (clearTimeout(n), t(this, Hn, "f").delete(e));
};
var rr, ir, ar, or, sr, cr = class extends pt {
	constructor(e, r) {
		super(e, r), rr.set(this, void 0), ir.set(this, void 0), ar.set(this, void 0), or.set(this, void 0), sr.set(this, !0), n(this, rr, r.callback, "f"), n(this, ir, r.skipInitial ?? !1, "f"), n(this, ar, r.config, "f"), window.ResizeObserver ? n(this, or, new ResizeObserver((e, n) => t(this, rr, "f").call(this, e, n)), "f") : console.warn("ResizeController error: the browser does not support ResizeObserver.");
	}
	async hostUpdated() {
		t(this, or, "f") && !t(this, ir, "f") && t(this, sr, "f") && t(this, rr, "f").call(this, [], t(this, or, "f")), n(this, sr, !1, "f");
	}
	_observe(e) {
		t(this, or, "f")?.observe(e, t(this, ar, "f")), n(this, sr, !0, "f");
	}
	_unobserve(e) {
		t(this, or, "f")?.unobserve(e);
	}
};
rr = /* @__PURE__ */ new WeakMap(), ir = /* @__PURE__ */ new WeakMap(), ar = /* @__PURE__ */ new WeakMap(), or = /* @__PURE__ */ new WeakMap(), sr = /* @__PURE__ */ new WeakMap();
var w = (e) => (t, n) => {
	let r = () => {
		typeof window < "u" && !customElements.get(e) && customElements.define(e, t);
	};
	n ? n.addInitializer(r) : r();
};
function lr(e) {
	let t = Symbol("_id");
	return (n, r, i) => {
		let a = i.value;
		return i.value = function(...n) {
			let r = this;
			clearTimeout(r[t]), r[t] = setTimeout(() => a.apply(this, n), e);
		}, i;
	};
}
var ur, dr, fr, pr, mr, hr, gr, _r, vr, yr = class extends pt {
	constructor(e, r) {
		super(e, r), ur.add(this), dr.set(this, void 0), fr.set(this, void 0), pr.set(this, (e) => t(this, ur, "m", vr).call(this, e)), mr.set(this, /* @__PURE__ */ new Map()), n(this, dr, r.debounce === !0, "f"), n(this, fr, r.callback, "f");
	}
	getScrollContainers(e) {
		return t(this, mr, "f").get(e);
	}
	_observe(e) {
		let n = t(this, ur, "m", _r).call(this, e);
		if (n.length > 0) {
			t(this, mr, "f").set(e, n);
			for (let e of n) (e === document.documentElement ? document : e).addEventListener("scroll", t(this, pr, "f"), { passive: !0 });
		}
	}
	_unobserve(e) {
		if (t(this, mr, "f").has(e)) {
			for (let n of t(this, mr, "f").get(e)) (n === document.documentElement ? document : n).removeEventListener("scroll", t(this, pr, "f"));
			t(this, mr, "f").delete(e);
		}
	}
	_debounceCallback(e) {
		t(this, fr, "f").call(this, e);
	}
};
dr = /* @__PURE__ */ new WeakMap(), fr = /* @__PURE__ */ new WeakMap(), pr = /* @__PURE__ */ new WeakMap(), mr = /* @__PURE__ */ new WeakMap(), ur = /* @__PURE__ */ new WeakSet(), hr = function(e) {
	let n = e.shadowRoot;
	if (!n) return null;
	let r = document.createTreeWalker(n, NodeFilter.SHOW_ELEMENT), i = r.currentNode;
	for (; i;) {
		if (i instanceof Element && t(this, ur, "m", gr).call(this, i)) return i;
		i = r.nextNode();
	}
	return null;
}, gr = function(e) {
	let t = getComputedStyle(e);
	return /(auto|scroll)/.test(t.overflow + t.overflowY + t.overflowX);
}, _r = function(e) {
	let n = [], r = t(this, ur, "m", hr).call(this, e);
	r && n.push(r);
	let i = e;
	for (; i;) t(this, ur, "m", gr).call(this, i) && n.push(i), i = i.parentElement;
	return n;
}, vr = function(e) {
	let n = e.target === document ? document.documentElement : e.target;
	t(this, dr, "f") ? this._debounceCallback(n) : t(this, fr, "f").call(this, n);
}, e([lr(40)], yr.prototype, "_debounceCallback", null);
var br, xr, Sr, Cr, wr, Tr, Er, Dr = class {
	constructor(e) {
		br.add(this), xr.set(this, !1), Sr.set(this, 0), Cr.set(this, 0), wr.set(this, ""), Tr.set(this, ""), e.addController(this);
	}
	lock() {
		t(this, xr, "f") || (n(this, xr, !0, "f"), n(this, Sr, window.scrollY, "f"), n(this, Cr, window.scrollX, "f"), n(this, wr, document.documentElement.style.overflow, "f"), n(this, Tr, document.documentElement.style.scrollbarGutter, "f"), t(this, br, "m", Er).call(this) && (document.documentElement.style.scrollbarGutter = "stable"), document.documentElement.style.overflow = "hidden", window.scrollTo(t(this, Cr, "f"), t(this, Sr, "f")));
	}
	unlock() {
		t(this, xr, "f") && (n(this, xr, !1, "f"), document.documentElement.style.overflow = t(this, wr, "f"), document.documentElement.style.scrollbarGutter = t(this, Tr, "f"), window.scrollTo(t(this, Cr, "f"), t(this, Sr, "f")));
	}
	hostDisconnected() {
		this.unlock();
	}
};
xr = /* @__PURE__ */ new WeakMap(), Sr = /* @__PURE__ */ new WeakMap(), Cr = /* @__PURE__ */ new WeakMap(), wr = /* @__PURE__ */ new WeakMap(), Tr = /* @__PURE__ */ new WeakMap(), br = /* @__PURE__ */ new WeakSet(), Er = function() {
	return document.documentElement.scrollHeight > document.documentElement.clientHeight;
};
var Or = {
	fromAttribute(e) {
		return e ? e.split(/\s+/).map((e) => e.trim()).filter(Boolean) : [];
	},
	toAttribute(e) {
		return e.join(" ");
	}
}, kr = "important", Ar = " !important", jr = -11, Mr = tt(class extends nt {
	constructor(e) {
		if (super(e), e.type !== et.ATTRIBUTE || e.name !== "style" || e.strings?.length > 2) throw Error("The `styleMap` directive must be used in the `style` attribute and must be the only part in the attribute.");
	}
	render(e) {
		return b;
	}
	update(e, [t]) {
		let { style: n } = e.element;
		this._previousStyleProperties === void 0 && (this._previousStyleProperties = new Set(Object.keys(t)));
		for (let e of this._previousStyleProperties) t[e] ?? (this._previousStyleProperties.delete(e), e.includes("-") ? n.removeProperty(e) : n[e] = null);
		for (let e in t) {
			let r = t[e];
			if (r != null) {
				this._previousStyleProperties.add(e);
				let t = typeof r == "string" && r.endsWith(Ar);
				e.includes("-") || t ? n.setProperty(e, t ? r.slice(0, jr) : r, t ? kr : "") : n[e] = r;
			}
		}
		return je;
	}
});
function Nr(e, ...t) {
	return typeof e == "object" && !!e && t.every((t) => t in e);
}
var Pr = Symbol("internals");
function Fr(e) {
	return Nr(e, Pr);
}
var Ir = Symbol("_internals"), Lr = Symbol("_customState");
function T(e, t) {
	var n;
	class r extends e {
		constructor() {
			super(...arguments), this[n] = /* @__PURE__ */ new Set();
		}
		get [(n = Lr, Pr)]() {
			return this[Ir] ?? (this[Ir] = this.attachInternals());
		}
	}
	return r.formAssociated = t, r;
}
function Rr(e, t) {
	return Lr in e ? e[Lr].has(t) : e[Pr].states.has(t);
}
function zr(e, t) {
	Rr(e, t) || (Lr in e && e[Lr].add(t), e[Pr]?.states.add(t), e[Pr]?.states.has(t));
}
function E(e, t) {
	return Lr in e && e[Lr].delete(t), e[Pr]?.states.delete(t) ? (e[Pr]?.states.has(t), !0) : !1;
}
function D(e, t, n) {
	n ? zr(e, t) : E(e, t);
}
function Br(e, t, ...n) {
	n.forEach((n) => D(e, `--${n}`, n === t));
}
function Vr(e) {
	return Nr(e, "checked");
}
function Hr(t) {
	class n extends t {
		constructor() {
			super(...arguments), this.checked = !1;
		}
		update(e) {
			super.update(e), e.has("checked") && (this.role === "button" ? (this.ariaPressed = `${this.checked}`, this.ariaChecked = null) : this.role && this.role !== "none" && this.role !== "presentation" && (this.ariaChecked = `${this.checked}`, this.ariaPressed = null));
		}
	}
	return e([S({
		type: Boolean,
		reflect: !0
	})], n.prototype, "checked", void 0), n;
}
function Ur(e) {
	return Nr(e, "indeterminate") && Vr(e);
}
function Wr(t) {
	class n extends Hr(t) {
		constructor() {
			super(...arguments), this.indeterminate = !1;
		}
		update(e) {
			super.update(e), e.has("indeterminate") && this.role && this.role !== "none" && this.role !== "presentation" && (this.ariaChecked = !this.checked && this.indeterminate ? "mixed" : `${this.checked}`);
		}
	}
	return e([S({
		type: Boolean,
		reflect: !0
	})], n.prototype, "indeterminate", void 0), n;
}
function Gr(e) {
	return Nr(e, "selected");
}
function Kr(t) {
	class n extends t {
		constructor() {
			super(...arguments), this.selected = !1;
		}
		update(e) {
			super.update(e), e.has("selected") && (this.role === "button" ? (this.ariaPressed = `${this.selected}`, this.ariaSelected = null, this.ariaChecked = null) : this.role === "radio" ? (this.ariaChecked = `${this.selected}`, this.ariaSelected = null, this.ariaPressed = null) : this.role && this.role !== "none" && this.role !== "presentation" && (this.ariaSelected = `${this.selected}`, this.ariaPressed = null, this.ariaChecked = null));
		}
	}
	return e([S({
		type: Boolean,
		reflect: !0
	})], n.prototype, "selected", void 0), n;
}
function qr(e) {
	return Vr(e) && e.checked || Gr(e) && e.selected;
}
function Jr(e, t) {
	Vr(e) ? e.checked = t : e.selected = t;
}
function Yr(e) {
	return Nr(e, "dirty", "pristine", "markAsDirty", "markAsPristine");
}
var Xr = Symbol("_eventHandler");
function Zr(e) {
	var t;
	class n extends e {
		constructor() {
			super(...arguments), this[t] = () => this.markAsDirty();
		}
		get dirty() {
			return Rr(this, "--dirty");
		}
		get pristine() {
			return !this.dirty;
		}
		connectedCallback() {
			this.markAsPristine(), super.connectedCallback(), this.addEventListener("change", this[Xr]);
		}
		disconnectedCallback() {
			super.disconnectedCallback(), this.removeEventListener("change", this[Xr]);
		}
		markAsPristine() {
			E(this, "--dirty");
		}
		markAsDirty() {
			zr(this, "--dirty");
		}
	}
	return t = Xr, n;
}
function Qr(e) {
	return Nr(e, "touched", "untouched", "markAsTouched", "markAsUntouched");
}
var $r = Symbol("_eventHandler");
function ei(e) {
	var t;
	class n extends e {
		constructor() {
			super(...arguments), this[t] = () => this.markAsTouched();
		}
		get touched() {
			return Rr(this, "--touched");
		}
		get untouched() {
			return !this.touched;
		}
		connectedCallback() {
			this.markAsUntouched(), super.connectedCallback(), this.addEventListener("focusout", this[$r], { capture: !0 });
		}
		disconnectedCallback() {
			super.disconnectedCallback(), this.removeEventListener("focusout", this[$r], { capture: !0 });
		}
		markAsTouched() {
			zr(this, "--touched");
		}
		markAsUntouched() {
			E(this, "--touched");
		}
	}
	return t = $r, n;
}
var ti = {
	primary: c("var(--md-sys-color-primary, #6750A4)"),
	onPrimary: c("var(--md-sys-color-on-primary, #FFFFFF)"),
	primaryContainer: c("var(--md-sys-color-primary-container, #EADDFF)"),
	onPrimaryContainer: c("var(--md-sys-color-on-primary-container, #4F378B)"),
	primaryFixed: c("var(--md-sys-color-primary-fixed, #EADDFF)"),
	primaryFixedDim: c("var(--md-sys-color-primary-fixed-dim, #D0BCFF)"),
	onPrimaryFixed: c("var(--md-sys-color-on-primary-fixed, #21005D)"),
	onPrimaryFixedVariant: c("var(--md-sys-color-on-primary-fixed-variant, #4F378B)"),
	secondary: c("var(--md-sys-color-secondary, #625B71)"),
	onSecondary: c("var(--md-sys-color-on-secondary, #FFFFFF)"),
	secondaryContainer: c("var(--md-sys-color-secondary-container, #E8DEF8)"),
	onSecondaryContainer: c("var(--md-sys-color-on-secondary-container, #4A4458)"),
	secondaryFixed: c("var(--md-sys-color-secondary-fixed, #E8DEF8)"),
	secondaryFixedDim: c("var(--md-sys-color-secondary-fixed-dim, #CCC2DC)"),
	onSecondaryFixed: c("var(--md-sys-color-on-secondary-fixed, #1D192B)"),
	onSecondaryFixedVariant: c("var(--md-sys-color-on-secondary-fixed-variant, #4A4458)"),
	tertiary: c("var(--md-sys-color-tertiary, #7D5260)"),
	onTertiary: c("var(--md-sys-color-on-tertiary, #FFFFFF)"),
	tertiaryContainer: c("var(--md-sys-color-tertiary-container, #FFD8E4)"),
	onTertiaryContainer: c("var(--md-sys-color-on-tertiary-container, #633B48)"),
	tertiaryFixed: c("var(--md-sys-color-tertiary-fixed, #FFD8E4)"),
	tertiaryFixedDim: c("var(--md-sys-color-tertiary-fixed-dim, #EFB8C8)"),
	onTertiaryFixed: c("var(--md-sys-color-on-tertiary-fixed, #31111D)"),
	onTertiaryFixedVariant: c("var(--md-sys-color-on-tertiary-fixed-variant, #633B48)"),
	error: c("var(--md-sys-color-error, #B3261E)"),
	onError: c("var(--md-sys-color-on-error, #FFFFFF)"),
	errorContainer: c("var(--md-sys-color-error-container, #F9DEDC)"),
	onErrorContainer: c("var(--md-sys-color-on-error-container, #8C1D18)"),
	surface: c("var(--md-sys-color-surface, #FEF7FF)"),
	onSurface: c("var(--md-sys-color-on-surface, #1D1B20)"),
	onSurfaceVariant: c("var(--md-sys-color-on-surface-variant, #49454F)"),
	surfaceContainerLowest: c("var(--md-sys-color-surface-container-lowest, #FFFFFF)"),
	surfaceContainerLow: c("var(--md-sys-color-surface-container-low, #F7F2FA)"),
	surfaceContainer: c("var(--md-sys-color-surface-container, #F3EDF7)"),
	surfaceContainerHigh: c("var(--md-sys-color-surface-container-high, #ECE6F0)"),
	surfaceContainerHighest: c("var(--md-sys-color-surface-container-highest, #E6E0E9)"),
	surfaceDim: c("var(--md-sys-color-surface-dim, #DED8E1)"),
	surfaceBright: c("var(--md-sys-color-surface-bright, #FEF7FF)"),
	surfaceVariant: c("var(--md-sys-color-surface-variant, #E7E0EC)"),
	inverseSurface: c("var(--md-sys-color-inverse-surface, #322F35)"),
	inverseOnSurface: c("var(--md-sys-color-inverse-on-surface, #F5EFF7)"),
	inversePrimary: c("var(--md-sys-color-inverse-primary, #D0BCFF)"),
	outline: c("var(--md-sys-color-outline, #79747E)"),
	outlineVariant: c("var(--md-sys-color-outline-variant, #CAC4D0)"),
	shadow: c("var(--md-sys-color-shadow, #000000)"),
	scrim: c("var(--md-sys-color-scrim, #000000)")
}, ni = {
	scale: c("var(--md-sys-density-scale, 0)"),
	size: c("var(--md-sys-density-size, 4px)")
}, ri = {
	...ni,
	calc(e) {
		return c(`calc(max(${e}, ${ni.scale}) * ${ni.size})`);
	},
	calcHalf(e) {
		return c(`calc(max(${e}, ${ni.scale}) * (${ni.size} / 2))`);
	}
}, ii = `color-mix(in srgb, var(--m3e-elevation-color, ${ti.shadow}) 20%, transparent)`, ai = [
	"0px 0px 0px 0px",
	"0px 2px 1px -1px",
	"0px 3px 1px -2px",
	"0px 3px 3px -2px",
	"0px 2px 4px -1px",
	"0px 3px 5px -1px",
	"0px 3px 5px -1px",
	"0px 4px 5px -2px",
	"0px 5px 5px -3px",
	"0px 5px 6px -3px",
	"0px 6px 6px -3px",
	"0px 6px 7px -4px",
	"0px 7px 8px -4px",
	"0px 7px 8px -4px",
	"0px 7px 9px -4px",
	"0px 8px 9px -5px",
	"0px 8px 10px -5px",
	"0px 8px 11px -5px",
	"0px 9px 11px -5px",
	"0px 9px 12px -6px",
	"0px 10px 13px -6px",
	"0px 10px 13px -6px",
	"0px 10px 14px -6px",
	"0px 11px 14px -7px",
	"0px 11px 15px -7px"
], oi = `color-mix(in srgb, var(--m3e-elevation-color, ${ti.shadow}) 14%, transparent)`, si = [
	"0px 0px 0px 0px",
	"0px 1px 1px 0px",
	"0px 2px 2px 0px",
	"0px 3px 4px 0px",
	"0px 4px 5px 0px",
	"0px 5px 8px 0px",
	"0px 6px 10px 0px",
	"0px 7px 10px 1px",
	"0px 8px 10px 1px",
	"0px 9px 12px 1px",
	"0px 10px 14px 1px",
	"0px 11px 15px 1px",
	"0px 12px 17px 2px",
	"0px 13px 19px 2px",
	"0px 14px 21px 2px",
	"0px 15px 22px 2px",
	"0px 16px 24px 2px",
	"0px 17px 26px 2px",
	"0px 18px 28px 2px",
	"0px 19px 29px 2px",
	"0px 20px 31px 3px",
	"0px 21px 33px 3px",
	"0px 22px 35px 3px",
	"0px 23px 36px 3px",
	"0px 24px 38px 3px"
], ci = `color-mix(in srgb, var(--m3e-elevation-color, ${ti.shadow}) 12%, transparent)`, li = [
	"0px 0px 0px 0px",
	"0px 1px 3px 0px",
	"0px 1px 5px 0px",
	"0px 1px 8px 0px",
	"0px 1px 10px 0px",
	"0px 1px 14px 0px",
	"0px 1px 18px 0px",
	"0px 2px 16px 1px",
	"0px 3px 14px 2px",
	"0px 3px 16px 2px",
	"0px 4px 18px 3px",
	"0px 4px 20px 3px",
	"0px 5px 22px 4px",
	"0px 5px 24px 4px",
	"0px 5px 26px 4px",
	"0px 6px 28px 5px",
	"0px 6px 30px 5px",
	"0px 6px 32px 5px",
	"0px 7px 34px 6px",
	"0px 7px 36px 6px",
	"0px 8px 38px 7px",
	"0px 8px 40px 7px",
	"0px 8px 42px 7px",
	"0px 9px 44px 8px",
	"0px 9px 46px 8px"
];
function ui(e) {
	return `${ii} ${ai[e]},${oi} ${si[e]},${ci} ${li[e]}`;
}
var di = {
	level0: c(`var(--md-sys-elevation-level0, ${ui(0)})`),
	level1: c(`var(--md-sys-elevation-level1, ${ui(1)})`),
	level2: c(`var(--md-sys-elevation-level2, ${ui(3)})`),
	level3: c(`var(--md-sys-elevation-level3, ${ui(6)})`),
	level4: c(`var(--md-sys-elevation-level4, ${ui(8)})`),
	level5: c(`var(--md-sys-elevation-level5, ${ui(12)})`)
};
function O(e) {
	return c(`var(--md-sys-measurement-space${e}, ${e / 100 * 8}px)`);
}
var fi = {
	space0: O(0),
	space25: O(25),
	space50: O(50),
	space75: O(75),
	space100: O(100),
	space125: O(125),
	space150: O(150),
	space175: O(175),
	space200: O(200),
	space225: O(225),
	space250: O(250),
	space300: O(300),
	space325: O(325),
	space350: O(350),
	space375: O(375),
	space400: O(400),
	space425: O(425),
	space450: O(450),
	space475: O(475),
	space500: O(500),
	space525: O(525),
	space550: O(550),
	space575: O(575),
	space600: O(600),
	space625: O(625),
	space650: O(650),
	space675: O(675),
	space700: O(700),
	space725: O(725),
	space750: O(750),
	space775: O(775),
	space800: O(800),
	space825: O(825),
	space850: O(850),
	space875: O(875),
	space900: O(900)
}, pi = {
	emphasized: c("var(--md-sys-motion-easing-emphasized, cubic-bezier(0.2, 0.0, 0, 1.0))"),
	emphasizedDecelerate: c("var(--md-sys-motion-easing-emphasized-decelerate, cubic-bezier(0.05, 0.7, 0.1, 1.0))"),
	emphasizedAccelerate: c("var(--md-sys-motion-easing-emphasized-accelerate, cubic-bezier(0.3, 0.0, 0.8, 0.15))"),
	standard: c("var(--md-sys-motion-easing-standard, cubic-bezier(0.2, 0.0, 0, 1.0))"),
	standardDecelerate: c("var(--md-sys-motion-easing-standard-decelerate, cubic-bezier(0, 0, 0, 1))"),
	standardAccelerate: c("var(--md-sys-motion-easing-standard-accelerate, cubic-bezier(0.3, 0, 1, 1))")
}, mi = {
	fastSpatial: c("var(--md-sys-motion-spring-fast-spatial, 350ms cubic-bezier(0.27, 1.06, 0.18, 1.00))"),
	defaultSpatial: c("var(--md-sys-motion-spring-default-spatial, 500ms cubic-bezier(0.27, 1.06, 0.18, 1.00))"),
	slowSpatial: c("var(--md-sys-motion-spring-slow-spatial, 750ms cubic-bezier(0.27, 1.06, 0.18, 1.00))"),
	fastEffects: c("var(--md-sys-motion-spring-fast-effects, 150ms cubic-bezier(0.31, 0.94, 0.34, 1.00))"),
	defaultEffects: c("var(--md-sys-motion-spring-default-effects, 200ms cubic-bezier(0.34, 0.80, 0.34, 1.00))"),
	slowEffects: c("var(--md-sys-motion-spring-slow-effects, 200ms cubic-bezier(0.34, 0.88, 0.34, 1.00))")
}, hi = {
	easing: pi,
	duration: {
		short1: c("var(--md-sys-motion-duration-short-1, 50ms)"),
		short2: c("var(--md-sys-motion-duration-short-2, 100ms)"),
		short3: c("var(--md-sys-motion-duration-short-3, 150ms)"),
		short4: c("var(--md-sys-motion-duration-short-4, 200ms)"),
		medium1: c("var(--md-sys-motion-duration-medium-1, 250ms)"),
		medium2: c("var(--md-sys-motion-duration-medium-2, 300ms)"),
		medium3: c("var(--md-sys-motion-duration-medium-3, 350ms)"),
		medium4: c("var(--md-sys-motion-duration-medium-4, 400ms)"),
		long1: c("var(--md-sys-motion-duration-long-1, 450ms)"),
		long2: c("var(--md-sys-motion-duration-long-2, 500ms)"),
		long3: c("var(--md-sys-motion-duration-long-3, 550ms)"),
		long4: c("var(--md-sys-motion-duration-long-4, 600ms)"),
		extraLong1: c("var(--md-sys-motion-duration-extra-long-1, 700ms)"),
		extraLong2: c("var(--md-sys-motion-duration-extra-long-2, 800ms)"),
		extraLong3: c("var(--md-sys-motion-duration-extra-long-3, 900ms)"),
		extraLong4: c("var(--md-sys-motion-duration-extra-long-4, 1000ms)")
	},
	spring: mi
}, gi = {
	width: c("var(--m3e-scrollbar-width, auto)"),
	thinWidth: c("var(--m3e-scrollbar-thin-width, thin)"),
	color: c("var(--m3e-scrollbar-thumb-color, #938f94) var(--m3e-scrollbar-track-color, transparent)")
}, k = {
	none: c("var(--md-sys-shape-corner-value-none, 0)"),
	extraSmall: c("var(--md-sys-shape-corner-value-extra-small, 4px)"),
	small: c("var(--md-sys-shape-corner-value-small, 8px)"),
	medium: c("var(--md-sys-shape-corner-value-medium, 12px)"),
	large: c("var(--md-sys-shape-corner-value-large, 16px)"),
	largeIncreased: c("var(--md-sys-shape-corner-value-large-increased, 20px)"),
	extraLarge: c("var(--md-sys-shape-corner-value-extra-large, 28px)"),
	extraLargeIncreased: c("var(--md-sys-shape-corner-value-extra-large-increased, 32px)"),
	extraExtraLarge: c("var(--md-sys-shape-corner-value-extra-extra-large, 48px)")
}, A = {
	color: ti,
	elevation: di,
	motion: hi,
	shape: { corner: {
		full: c("var(--md-sys-shape-corner-full, 9999px)"),
		extraLargeTop: c(`var(--md-sys-shape-corner-extra-large-top, ${k.extraLarge} ${k.extraLarge} ${k.none} ${k.none})`),
		extraLarge: c(`var(--md-sys-shape-corner-extra-large, ${k.extraLarge})`),
		extraLargeEnd: c(`${k.none} ${k.extraLarge} ${k.extraLarge} ${k.none}`),
		extraLargeStart: c(`${k.extraLarge} ${k.none} ${k.none} ${k.extraLarge}`),
		largeTop: c(`var(--md-sys-shape-corner-large-top, ${k.large} ${k.large} ${k.none} ${k.none})`),
		largeEnd: c(`var(--md-sys-shape-corner-large-end, ${k.none} ${k.large} ${k.large} ${k.none})`),
		largeStart: c(`var(--md-sys-shape-corner-large-start, ${k.large} ${k.none} ${k.none} ${k.large})`),
		large: c(`var(--md-sys-shape-corner-large, ${k.large})`),
		medium: c(`var(--md-sys-shape-corner-medium, ${k.medium})`),
		mediumTop: c(`${k.medium} ${k.medium} ${k.none} ${k.none}`),
		mediumEnd: c(`${k.none} ${k.medium} ${k.medium} ${k.none}`),
		mediumStart: c(`${k.medium} ${k.none} ${k.none} ${k.medium}`),
		small: c(`var(--md-sys-shape-corner-small, ${k.small})`),
		smallTop: c(`${k.small} ${k.small} ${k.none} ${k.none}`),
		smallEnd: c(`${k.none} ${k.small} ${k.small} ${k.none}`),
		smallStart: c(`${k.small} ${k.none} ${k.none} ${k.small}`),
		extraSmallTop: c(`var(--md-sys-shape-corner-extra-small-top, ${k.extraSmall} ${k.extraSmall} ${k.none} ${k.none})`),
		extraSmall: c(`var(--md-sys-shape-corner-extra-small, ${k.extraSmall})`),
		extraSmallEnd: c(`${k.none} ${k.extraSmall} ${k.extraSmall} ${k.none}`),
		extraSmallStart: c(`${k.extraSmall} ${k.none} ${k.none} ${k.extraSmall}`),
		extraSmallBottom: c(`${k.none} ${k.none} ${k.extraSmall} ${k.extraSmall}`),
		none: c(`var(--md-sys-shape-corner-none, ${k.none})`),
		largeIncreased: c(`var(--md-sys-shape-corner-large-increased, ${k.largeIncreased})`),
		extraLargeIncreased: c(`var(--md-sys-shape-corner-extra-large-increased, ${k.extraLargeIncreased})`),
		extraExtraLarge: c(`var(--md-sys-shape-corner-extra-extra-large, ${k.extraExtraLarge})`),
		value: k
	} },
	state: {
		focusStateLayerOpacity: c("var(--md-sys-state-focus-state-layer-opacity, 10%)"),
		hoverStateLayerOpacity: c("var(--md-sys-state-hover-state-layer-opacity, 8%)"),
		pressedStateLayerOpacity: c("var(--md-sys-state-pressed-state-layer-opacity, 10%)")
	},
	typescale: {
		standard: {
			display: {
				large: {
					fontSize: c("var(--md-sys-typescale-display-large-font-size, 3.5625rem)"),
					fontWeight: c("var(--md-sys-typescale-display-large-font-weight, 400)"),
					lineHeight: c("var(--md-sys-typescale-display-large-line-height, 4rem)"),
					tracking: c("var(--md-sys-typescale-display-large-tracking, 0.015625rem)")
				},
				medium: {
					fontSize: c("var(--md-sys-typescale-display-medium-font-size, 2.8125rem)"),
					fontWeight: c("var(--md-sys-typescale-display-medium-font-weight, 400)"),
					lineHeight: c("var(--md-sys-typescale-display-medium-line-height, 3.25rem)"),
					tracking: c("var(--md-sys-typescale-display-medium-tracking, 0)")
				},
				small: {
					fontSize: c("var(--md-sys-typescale-display-small-font-size, 2.25rem)"),
					fontWeight: c("var(--md-sys-typescale-display-small-font-weight, 400)"),
					lineHeight: c("var(--md-sys-typescale-display-small-line-height, 2.75rem)"),
					tracking: c("var(--md-sys-typescale-display-small-tracking, 0)")
				}
			},
			headline: {
				large: {
					fontSize: c("var(--md-sys-typescale-headline-large-font-size, 2rem)"),
					fontWeight: c("var(--md-sys-typescale-headline-large-font-weight, 400)"),
					lineHeight: c("var(--md-sys-typescale-headline-large-line-height, 2.5rem)"),
					tracking: c("var(--md-sys-typescale-headline-large-tracking, 0)")
				},
				medium: {
					fontSize: c("var(--md-sys-typescale-headline-medium-font-size, 1.75rem)"),
					fontWeight: c("var(--md-sys-typescale-headline-medium-font-weight, 400)"),
					lineHeight: c("var(--md-sys-typescale-headline-medium-line-height, 2.25rem)"),
					tracking: c("var(--md-sys-typescale-headline-medium-tracking, 0)")
				},
				small: {
					fontSize: c("var(--md-sys-typescale-headline-small-font-size, 1.5rem)"),
					fontWeight: c("var(--md-sys-typescale-headline-small-font-weight, 400)"),
					lineHeight: c("var(--md-sys-typescale-headline-small-line-height, 2rem)"),
					tracking: c("var(--md-sys-typescale-headline-small-tracking, 0)")
				}
			},
			title: {
				large: {
					fontSize: c("var(--md-sys-typescale-title-large-font-size, 1.375rem)"),
					fontWeight: c("var(--md-sys-typescale-title-large-font-weight, 400)"),
					lineHeight: c("var(--md-sys-typescale-title-large-line-height, 1.75rem)"),
					tracking: c("var(--md-sys-typescale-title-large-tracking, 0)")
				},
				medium: {
					fontSize: c("var(--md-sys-typescale-title-medium-font-size, 1rem)"),
					fontWeight: c("var(--md-sys-typescale-title-medium-font-weight, 500)"),
					lineHeight: c("var(--md-sys-typescale-title-medium-line-height, 1.5rem)"),
					tracking: c("var(--md-sys-typescale-title-medium-tracking, 0.009375rem)")
				},
				small: {
					fontSize: c("var(--md-sys-typescale-title-small-font-size, 0.875rem)"),
					fontWeight: c("var(--md-sys-typescale-title-small-font-weight, 500)"),
					lineHeight: c("var(--md-sys-typescale-title-small-line-height, 1.25rem)"),
					tracking: c("var(--md-sys-typescale-title-small-tracking, 0.00625rem)")
				}
			},
			body: {
				large: {
					fontSize: c("var(--md-sys-typescale-body-large-font-size, 1rem)"),
					fontWeight: c("var(--md-sys-typescale-body-large-font-weight, 400)"),
					lineHeight: c("var(--md-sys-typescale-body-large-line-height, 1.5rem)"),
					tracking: c("var(--md-sys-typescale-body-large-tracking, 0.03125rem)")
				},
				medium: {
					fontSize: c("var(--md-sys-typescale-body-medium-font-size, 0.875rem)"),
					fontWeight: c("var(--md-sys-typescale-body-medium-font-weight, 400)"),
					lineHeight: c("var(--md-sys-typescale-body-medium-line-height, 1.25rem)"),
					tracking: c("var(--md-sys-typescale-body-medium-tracking, 0.015625rem)")
				},
				small: {
					fontSize: c("var(--md-sys-typescale-body-small-font-size, 0.75rem)"),
					fontWeight: c("var(--md-sys-typescale-body-small-font-weight, 400)"),
					lineHeight: c("var(--md-sys-typescale-body-small-line-height, 1rem)"),
					tracking: c("var(--md-sys-typescale-body-small-tracking, 0.025rem)")
				}
			},
			label: {
				large: {
					fontSize: c("var(--md-sys-typescale-label-large-font-size, 0.875rem)"),
					fontWeight: c("var(--md-sys-typescale-label-large-font-weight, 500)"),
					lineHeight: c("var(--md-sys-typescale-label-large-line-height, 1.25rem)"),
					tracking: c("var(--md-sys-typescale-label-large-tracking, 0.00625rem)")
				},
				medium: {
					fontSize: c("var(--md-sys-typescale-label-medium-font-size, 0.75rem)"),
					fontWeight: c("var(--md-sys-typescale-label-medium-font-weight, 500)"),
					lineHeight: c("var(--md-sys-typescale-label-medium-line-height, 1rem)"),
					tracking: c("var(--md-sys-typescale-label-medium-tracking, 0.03125rem)")
				},
				small: {
					fontSize: c("var(--md-sys-typescale-label-small-font-size, 0.6875rem)"),
					fontWeight: c("var(--md-sys-typescale-label-small-font-weight, 500)"),
					lineHeight: c("var(--md-sys-typescale-label-small-line-height, 1rem)"),
					tracking: c("var(--md-sys-typescale-label-small-tracking, 0.03125rem)")
				}
			}
		},
		emphasized: {
			display: {
				large: {
					fontSize: c("var(--md-sys-typescale-emphasized-display-large-font-size, 3.5625rem)"),
					fontWeight: c("var(--md-sys-typescale-emphasized-display-large-font-weight, 500)"),
					lineHeight: c("var(--md-sys-typescale-emphasized-display-large-line-height, 4rem)"),
					tracking: c("var(--md-sys-typescale-emphasized-display-large-tracking, 0.015625rem)")
				},
				medium: {
					fontSize: c("var(--md-sys-typescale-emphasized-display-medium-font-size, 2.8125rem)"),
					fontWeight: c("var(--md-sys-typescale-emphasized-display-medium-font-weight, 500)"),
					lineHeight: c("var(--md-sys-typescale-emphasized-display-medium-line-height, 3.25rem)"),
					tracking: c("var(--md-sys-typescale-emphasized-display-medium-tracking, 0)")
				},
				small: {
					fontSize: c("var(--md-sys-typescale-emphasized-display-small-font-size, 2.25rem)"),
					fontWeight: c("var(--md-sys-typescale-emphasized-display-small-font-weight, 500)"),
					lineHeight: c("var(--md-sys-typescale-emphasized-display-small-line-height, 2.75rem)"),
					tracking: c("var(--md-sys-typescale-emphasized-display-small-tracking, 0)")
				}
			},
			headline: {
				large: {
					fontSize: c("var(--md-sys-typescale-emphasized-headline-large-font-size, 2rem)"),
					fontWeight: c("var(--md-sys-typescale-emphasized-headline-large-font-weight, 500)"),
					lineHeight: c("var(--md-sys-typescale-emphasized-headline-large-line-height, 2.5rem)"),
					tracking: c("var(--md-sys-typescale-emphasized-headline-large-tracking, 0)")
				},
				medium: {
					fontSize: c("var(--md-sys-typescale-emphasized-headline-medium-font-size, 1.75rem)"),
					fontWeight: c("var(--md-sys-typescale-emphasized-headline-medium-font-weight, 500)"),
					lineHeight: c("var(--md-sys-typescale-emphasized-headline-medium-line-height, 2.25rem)"),
					tracking: c("var(--md-sys-typescale-emphasized-headline-medium-tracking, 0)")
				},
				small: {
					fontSize: c("var(--md-sys-typescale-emphasized-headline-small-font-size, 1.5rem)"),
					fontWeight: c("var(--md-sys-typescale-emphasized-headline-small-font-weight, 500)"),
					lineHeight: c("var(--md-sys-typescale-emphasized-headline-small-line-height, 2rem)"),
					tracking: c("var(--md-sys-typescale-emphasized-headline-small-tracking, 0)")
				}
			},
			title: {
				large: {
					fontSize: c("var(--md-sys-typescale-emphasized-title-large-font-size, 1.375rem)"),
					fontWeight: c("var(--md-sys-typescale-emphasized-title-large-font-weight, 500)"),
					lineHeight: c("var(--md-sys-typescale-emphasized-title-large-line-height, 1.75rem)"),
					tracking: c("var(--md-sys-typescale-emphasized-title-large-tracking, 0)")
				},
				medium: {
					fontSize: c("var(--md-sys-typescale-emphasized-title-medium-font-size, 1rem)"),
					fontWeight: c("var(--md-sys-typescale-emphasized-title-medium-font-weight, 700)"),
					lineHeight: c("var(--md-sys-typescale-emphasized-title-medium-line-height, 3.5rem)"),
					tracking: c("var(--md-sys-typescale-emphasized-title-medium-tracking, 0.009375rem)")
				},
				small: {
					fontSize: c("var(--md-sys-typescale-emphasized-title-small-font-size, 0.875rem)"),
					fontWeight: c("var(--md-sys-typescale-emphasized-title-small-font-weight, 700)"),
					lineHeight: c("var(--md-sys-typescale-emphasized-title-small-line-height, 1.25rem)"),
					tracking: c("var(--md-sys-typescale-emphasized-title-small-tracking, 0.00625rem)")
				}
			},
			body: {
				large: {
					fontSize: c("var(--md-sys-typescale-emphasized-body-large-font-size, 1rem)"),
					fontWeight: c("var(--md-sys-typescale-emphasized-body-large-font-weight, 500)"),
					lineHeight: c("var(--md-sys-typescale-emphasized-body-large-line-height, 1.5rem)"),
					tracking: c("var(--md-sys-typescale-emphasized-body-large-tracking, 0.03125rem)")
				},
				medium: {
					fontSize: c("var(--md-sys-typescale-emphasized-body-medium-font-size, 0.875rem)"),
					fontWeight: c("var(--md-sys-typescale-emphasized-body-medium-font-weight, 500)"),
					lineHeight: c("var(--md-sys-typescale-emphasized-body-medium-line-height, 1.25rem)"),
					tracking: c("var(--md-sys-typescale-emphasized-body-medium-tracking, 0.015625rem)")
				},
				small: {
					fontSize: c("var(--md-sys-typescale-emphasized-body-small-font-size, 0.75rem)"),
					fontWeight: c("var(--md-sys-typescale-emphasized-body-small-font-weight, 500)"),
					lineHeight: c("var(--md-sys-typescale-emphasized-body-small-line-height, 1rem)"),
					tracking: c("var(--md-sys-typescale-emphasized-body-small-tracking, 0.025rem)")
				}
			},
			label: {
				large: {
					fontSize: c("var(--md-sys-typescale-emphasized-label-large-font-size, 0.875rem)"),
					fontWeight: c("var(--md-sys-typescale-emphasized-label-large-font-weight, 700)"),
					lineHeight: c("var(--md-sys-typescale-emphasized-label-large-line-height, 1.25rem)"),
					tracking: c("var(--md-sys-typescale-emphasized-label-large-tracking, 0.00625rem)")
				},
				medium: {
					fontSize: c("var(--md-sys-typescale-emphasized-label-medium-font-size, 0.75rem)"),
					fontWeight: c("var(--md-sys-typescale-emphasized-label-medium-font-weight, 700)"),
					lineHeight: c("var(--md-sys-typescale-emphasized-label-medium-line-height, 1rem)"),
					tracking: c("var(--md-sys-typescale-emphasized-label-medium-tracking, 0.03125rem)")
				},
				small: {
					fontSize: c("var(--md-sys-typescale-emphasized-label-small-font-size, 0.6875rem)"),
					fontWeight: c("var(--md-sys-typescale-emphasized-label-small-font-weight, 700)"),
					lineHeight: c("var(--md-sys-typescale-emphasized-label-small-line-height, 1rem)"),
					tracking: c("var(--md-sys-typescale-emphasized-label-small-tracking, 0.03125rem)")
				}
			}
		}
	},
	scrollbar: gi,
	density: ri,
	measurement: fi
};
function _i(e) {
	return Nr(e, "disabled");
}
function vi(t, n = !0, r = !0) {
	class i extends t {
		constructor() {
			super(...arguments), this.disabled = !1;
		}
		update(e) {
			super.update(e), e.has("disabled") && r && this.role && this.role !== "none" && this.role !== "presentation" && (this.ariaDisabled = this.disabled ? "true" : null);
		}
	}
	return e([S({
		type: Boolean,
		reflect: n
	})], i.prototype, "disabled", void 0), i;
}
function yi(e) {
	return Nr(e, "disabledInteractive") && _i(e);
}
var bi = [
	"click",
	"dblclick",
	"auxclick",
	"keydown",
	"keyup"
], xi = [
	"Tab",
	"ArrowLeft",
	"ArrowUp",
	"ArrowRight",
	"ArrowDown",
	"Left",
	"Up",
	"Right",
	"Down"
], Si = Symbol("_suppressedEventHandler");
function Ci(t) {
	var n;
	class r extends t {
		constructor() {
			super(...arguments), this[n] = (e) => {
				if (this.disabledInteractive) {
					if (e instanceof KeyboardEvent && xi.includes(e.key)) return;
					e.stopImmediatePropagation(), e.preventDefault();
				}
			}, this.disabledInteractive = !1;
		}
		connectedCallback() {
			bi.forEach((e) => this.addEventListener(e, this[Si], !0)), super.connectedCallback();
		}
		disconnectedCallback() {
			bi.forEach((e) => this.removeEventListener(e, this[Si], !0)), super.disconnectedCallback();
		}
		update(e) {
			super.update(e), (e.has("disabled") || e.has("disabledInteractive")) && this.role && this.role !== "none" && this.role !== "presentation" && this.role !== "none" && (this.ariaDisabled = this.disabled || this.disabledInteractive ? "true" : null);
		}
	}
	return n = Si, e([S({
		attribute: "disabled-interactive",
		type: Boolean,
		reflect: !0
	})], r.prototype, "disabledInteractive", void 0), r;
}
var wi = Symbol("updateLabels");
function Ti(e) {
	return Nr(e, "labels") && Fr(e);
}
var Ei = Symbol("_eventHandler");
function Di(e) {
	var t;
	class n extends e {
		constructor() {
			super(...arguments), this[t] = (e) => {
				e.defaultPrevented || this[wi]();
			};
		}
		get labels() {
			return this[Pr].labels;
		}
		connectedCallback() {
			super.connectedCallback(), this.addEventListener("focusout", this[Ei]), this.addEventListener("change", this[Ei]);
		}
		disconnectedCallback() {
			super.disconnectedCallback(), this.removeEventListener("focusout", this[Ei]), this.removeEventListener("change", this[Ei]);
		}
		update(e) {
			super.update(e), this[wi]();
		}
		[(t = Ei, wi)]() {
			let e = this.hasAttribute("tabindex"), t = _i(this) && this.disabled || yi(this) && this.disabledInteractive, n = Qr(this) && this.touched && (this.ariaInvalid || Rr(this, "--invalid"));
			for (let r of this.labels ?? []) r.style.userSelect = e ? "none" : "", r.style.cursor = !t && e ? "pointer" : "", !t && e ? r.style.setProperty("-webkit-tap-highlight-color", "rgba(0, 0, 0, 0)") : r.style.removeProperty("-webkit-tap-highlight-color"), r.style.color = t ? `color-mix(in srgb, ${A.color.onSurface} 38%, transparent)` : n ? `${A.color.error}` : "";
		}
	}
	return n.formAssociated = !0, n;
}
var Oi = Symbol("formValue"), ki = Symbol("defaultValue"), Ai = Symbol("_defaultValue"), ji = Symbol("_defaultIndeterminate"), Mi = Symbol("_formDisabled");
function Ni(t) {
	var n, r;
	class i extends t {
		constructor() {
			super(...arguments), this[n] = !1, this[r] = !1;
		}
		get form() {
			return this[Pr].form;
		}
		get labels() {
			return this[Pr].labels;
		}
		get [(n = ji, r = Mi, Oi)]() {
			return null;
		}
		get [ki]() {
			return this[Ai];
		}
		get name() {
			return this.getAttribute("name") ?? "";
		}
		set name(e) {
			e ? this.setAttribute("name", e) : this.removeAttribute("name");
		}
		get disabled() {
			return super.disabled || this[Mi];
		}
		set disabled(e) {
			super.disabled = e;
		}
		connectedCallback() {
			super.connectedCallback(), Vr(this) ? (this[Ai] = this.checked, Ur(this) && (this[ji] = this.indeterminate)) : "value" in this && (this[Ai] = this.value);
		}
		requestUpdate(e, t, n) {
			super.requestUpdate(e, t, n), this[Pr].setFormValue(this[Oi]);
		}
		formDisabledCallback(e) {
			let t = this.disabled;
			this[Mi] = e, this.disabled != t && this.requestUpdate("disabled", t);
		}
		formResetCallback() {
			Vr(this) ? (this.checked = this[Ai] === !0, Ur(this) && (this.indeterminate = this[ji])) : "value" in this && (this.value = this[ki]), Yr(this) && this.markAsPristine(), Qr(this) && this.markAsUntouched();
		}
	}
	return i.formAssociated = !0, e([S({ noAccessor: !0 })], i.prototype, "name", null), e([S({ type: Boolean })], i.prototype, "disabled", null), i;
}
var Pi = Symbol("validate"), Fi = Symbol("defaultValidationMessages"), Ii = Symbol("_updateValidity"), Li = Symbol("_validityMessage"), Ri = Symbol("_validationMessages");
function zi(t) {
	var n;
	class r extends t {
		constructor() {
			super(...arguments), this[n] = {};
		}
		get willValidate() {
			return this[Pr].willValidate;
		}
		get validity() {
			return this[Ii](), this[Pr].validity;
		}
		get validationMessage() {
			return this[Ii](), this[Pr].validationMessage;
		}
		get [(n = Ri, Fi)]() {
			return {
				valueMissing: "This field is required.",
				typeMismatch: "The value is not in the correct format.",
				patternMismatch: "The value does not match the required pattern.",
				tooLong: "The value is too long.",
				tooShort: "The value is too short.",
				rangeUnderflow: "The value is too small.",
				rangeOverflow: "The value is too large.",
				stepMismatch: "The value is not a valid step.",
				badInput: "The value is invalid.",
				customError: "The value is invalid."
			};
		}
		set validationMessages(e) {
			this[Ri] = e;
		}
		get validationMessages() {
			return {
				...this[Fi],
				...this[Ri]
			};
		}
		[Pi]() {
			return this[Li] ? { customError: !0 } : void 0;
		}
		reportValidity() {
			return Qr(this) && this.markAsTouched(), this[Ii](), this[Pr].reportValidity();
		}
		checkValidity() {
			return this[Ii](), this[Pr].checkValidity();
		}
		setCustomValidity(e) {
			this[Li] = e || void 0, this[Ii]();
		}
		requestUpdate(e, t, n) {
			super.requestUpdate(e, t, n), this[Ii]();
		}
		firstUpdated(e) {
			super.firstUpdated(e), this[Ii]();
		}
		[Ii]() {
			if (!this.isConnected) return;
			let e = this[Pi](), t = e?.customError && this[Li] || this._getValidityMessage(e), n = !!t;
			this[Pr].setValidity(e, t), this.ariaInvalid = n ? "true" : null, D(this, "--invalid", n === !0), Ti(this) && this[wi]?.();
		}
		_getValidityMessage(e) {
			if (e) {
				let t = this.validationMessages;
				for (let n in e) if (e[n]) {
					let e = t[n];
					if (typeof e == "string") return e;
					if (typeof e == "function") return e(this);
				}
			}
			return "";
		}
	}
	return e([S({ type: Object })], r.prototype, "validationMessages", null), r;
}
var Bi = Symbol("_tabindex");
function Vi(e) {
	var t;
	class n extends e {
		constructor() {
			super(...arguments), this[t] = 0;
		}
		connectedCallback() {
			this[Bi] = Number.parseInt(this.getAttribute("tabindex") ?? "0"), super.connectedCallback();
		}
		firstUpdated(e) {
			super.firstUpdated(e), !this.hasAttribute("tabindex") && !e.has("disabled") && this.setAttribute("tabindex", `${this[Bi]}`);
		}
		update(e) {
			if (super.update(e), e.has("disabled")) {
				if (!this.disabled && this.role !== "none") this.hasAttribute("tabindex") || this.setAttribute("tabindex", `${this[Bi]}`);
				else {
					let e = this.getAttribute("tabindex");
					e && (this[Bi] = Number.parseInt(e)), this.removeAttribute("tabindex");
				}
			}
		}
	}
	return t = Bi, n;
}
var Hi = Symbol("_clickHandler");
function Ui(t) {
	var n;
	class r extends t {
		constructor() {
			super(...arguments), this.type = "button", this[n] = async (e) => {
				if (e.defaultPrevented || _i(this) && this.disabled || yi(this) && this.disabledInteractive) return;
				let t = this[Pr].form;
				if (t && this.type !== "button" && (await new Promise((e) => setTimeout(e)), !e.defaultPrevented)) switch (this.type) {
					case "reset":
						t.reset();
						break;
					case "submit": t.addEventListener("submit", (e) => Object.defineProperty(e, "submitter", {
						configurable: !0,
						enumerable: !0,
						get: () => this
					}), {
						capture: !0,
						once: !0
					}), this[Pr].setFormValue(this.value), t.requestSubmit();
				}
			};
		}
		get name() {
			return this.getAttribute("name") ?? "";
		}
		set name(e) {
			e ? this.setAttribute("name", e) : this.removeAttribute("name");
		}
		get value() {
			return this.getAttribute("value");
		}
		set value(e) {
			e == null ? this.removeAttribute("value") : this.setAttribute("value", e);
		}
		connectedCallback() {
			super.connectedCallback(), this.addEventListener("click", this[Hi]);
		}
		disconnectedCallback() {
			super.disconnectedCallback(), this.removeEventListener("click", this[Hi]);
		}
	}
	return n = Hi, r.formAssociated = !0, e([S()], r.prototype, "name", null), e([S()], r.prototype, "value", null), e([S()], r.prototype, "type", void 0), r;
}
var Wi = Symbol("_control"), Gi = Symbol("_firstUpdated");
function Ki(t) {
	var n;
	class r extends t {
		constructor() {
			super(...arguments), this[n] = !1, this.htmlFor = null;
		}
		get control() {
			return this[Wi]?.deref() ?? null;
		}
		connectedCallback() {
			super.connectedCallback();
			let e = this[Wi]?.deref();
			e && this.attach(e);
		}
		disconnectedCallback() {
			super.disconnectedCallback();
			let e = this[Wi];
			e && (this.detach(), this[Wi] = e);
		}
		firstUpdated(e) {
			super.firstUpdated(e), this[Gi] = !0;
		}
		update(e) {
			if (super.update(e), e.has("htmlFor")) {
				if (this.htmlFor) {
					let e = this.getRootNode();
					e && Ht(this.htmlFor, e).then((e) => {
						e !== this.control && (this.control && this.detach(), e instanceof HTMLElement && this.attach(e));
					});
				} else this.control && this[Gi] && this.detach();
			}
		}
		attach(e) {
			this[Wi] = new WeakRef(e);
		}
		detach() {
			this[Wi] = void 0;
		}
	}
	return n = Gi, e([S({ attribute: "for" })], r.prototype, "htmlFor", void 0), r;
}
function qi(e, r = !0) {
	var i, a, o, s, c, l, u;
	class d extends e {
		constructor() {
			super(...arguments), i.add(this), a.set(this, !1), o.set(this, (e) => t(this, i, "m", l).call(this, e)), s.set(this, (e) => t(this, i, "m", u).call(this, e)), c.set(this, () => n(this, a, !1, "f"));
		}
		connectedCallback() {
			super.connectedCallback(), this.addEventListener("keydown", t(this, o, "f")), this.addEventListener("keyup", t(this, s, "f")), this.addEventListener("focusout", t(this, c, "f"));
		}
		disconnectedCallback() {
			super.disconnectedCallback(), this.removeEventListener("keydown", t(this, o, "f")), this.removeEventListener("keyup", t(this, s, "f")), this.removeEventListener("focusout", t(this, c, "f"));
		}
	}
	return a = /* @__PURE__ */ new WeakMap(), o = /* @__PURE__ */ new WeakMap(), s = /* @__PURE__ */ new WeakMap(), c = /* @__PURE__ */ new WeakMap(), i = /* @__PURE__ */ new WeakSet(), l = function(e) {
		e.target !== e.currentTarget || _i(this) && this.disabled || yi(this) && this.disabledInteractive ? n(this, a, !1, "f") : (e.key === " " || r && e.key === "Enter") && n(this, a, !0, "f");
	}, u = function(e) {
		e.target !== e.currentTarget || _i(this) && this.disabled || yi(this) && this.disabledInteractive || !t(this, a, "f") ? n(this, a, !1, "f") : (n(this, a, !1, "f"), this.dispatchEvent(new MouseEvent("click", {
			cancelable: !0,
			bubbles: !0,
			composed: !0
		})));
	}, d;
}
var Ji = Symbol("renderPseudoLink");
function Yi(e) {
	return Nr(e, "download", "href", "rel", "target");
}
var Xi = Symbol("_clickHandler");
function Zi(n, r = !1) {
	var i, a, o, s, c;
	class l extends n {
		constructor() {
			super(...arguments), i.add(this), this[c] = async (e) => {
				yi(this) && this.disabledInteractive && (e.preventDefault(), e.stopPropagation());
				let t = e.composedPath()[0];
				if (await new Promise((e) => e()), !e.defaultPrevented && this.href) {
					if (e.stopImmediatePropagation(), t instanceof HTMLAnchorElement && t.getRootNode() === this.shadowRoot && e instanceof MouseEvent && (e.ctrlKey || e.metaKey || e.shiftKey || e.altKey)) return;
					e.preventDefault();
					let n = document.createElement("a");
					n.href = this.href, n.rel = this.rel, n.target = this.target, this.download != null && (n.download = this.download), n.addEventListener("click", async () => {
						await new Promise((e) => e()), n.remove();
					}), document.body.appendChild(n), n.click();
				}
			}, this.href = "", this.target = "", this.rel = "";
		}
		get download() {
			return this.getAttribute("download");
		}
		set download(e) {
			let t = this.download;
			t !== e && (e ? this.setAttribute("download", e) : this.removeAttribute("download"), this.requestUpdate("download", t));
		}
		connectedCallback() {
			super.connectedCallback(), r || this.addEventListener("click", this[Xi]), this.hasAttribute("href") && this.role === "button" && (this.role = "link");
		}
		disconnectedCallback() {
			super.disconnectedCallback(), r || this.removeEventListener("click", this[Xi]);
		}
		[(i = /* @__PURE__ */ new WeakSet(), c = Xi, Ji)]() {
			let e = _i(this) && this.disabled, n = yi(this) && this.disabledInteractive;
			return !e && !n && this.href ? y`<a href="${this.href}" target="${rt(this.target || void 0)}" rel="${rt(this.rel || void 0)}" download="${rt(this.download || void 0)}" tabindex="-1" aria-hidden="true" @pointerdown="${t(this, i, "m", a)}" @focus="${t(this, i, "m", o)}" @blur="${t(this, i, "m", s)}"></a>` : b;
		}
	}
	return a = function(e) {
		e.button === 2 ? e.target.removeAttribute("aria-hidden") : e.preventDefault();
	}, o = function(e) {
		e.target.blur(), this.focus();
	}, s = function(e) {
		e.target.setAttribute("aria-hidden", "true");
	}, e([S()], l.prototype, "href", void 0), e([S()], l.prototype, "target", void 0), e([S()], l.prototype, "rel", void 0), e([S({ reflect: !1 })], l.prototype, "download", null), l;
}
function Qi(e) {
	return Nr(e, "readOnly");
}
var $i = Symbol("_wasConnected");
function ea(e) {
	var t;
	class n extends e {
		constructor() {
			super(...arguments), this[t] = !1;
		}
		reconnectedCallback() {}
		connectedCallback() {
			super.connectedCallback(), this[$i] && this.reconnectedCallback();
		}
		disconnectedCallback() {
			super.disconnectedCallback(), this[$i] = !0;
		}
	}
	return t = $i, n;
}
function ta(t, n = !0) {
	class r extends t {
		constructor() {
			super(...arguments), this.required = !1;
		}
		get optional() {
			return !this.required;
		}
		update(e) {
			super.update(e), e.has("required") && n && (this.ariaRequired = `${this.required}`);
		}
	}
	return e([S({
		type: Boolean,
		reflect: !0
	})], r.prototype, "required", void 0), r;
}
function na(e) {
	class t extends e {
		[Pi]() {
			let e = super[Pi]();
			return !e && this.required && (Vr(this) && !this.checked || !this.value) ? { valueMissing: !0 } : e;
		}
	}
	return t;
}
function j(e, t) {
	class n extends e {
		connectedCallback() {
			this.role = this.role || t, super.connectedCallback();
		}
	}
	return n;
}
var ra = Symbol("resumeAnimation");
function ia(e) {
	class t extends e {
		connectedCallback() {
			super.connectedCallback(), zr(this, "--no-animate"), this[ra]();
		}
		[ra]() {
			requestAnimationFrame(() => E(this, "--no-animate"));
		}
	}
	return t;
}
function aa(t) {
	class n extends t {
		constructor() {
			super(...arguments), this.vertical = !1;
		}
		update(e) {
			super.update(e), e.has("vertical") && (this.ariaOrientation = this.vertical ? "vertical" : "horizontal");
		}
	}
	return e([S({
		type: Boolean,
		reflect: !0
	})], n.prototype, "vertical", void 0), n;
}
var oa, sa = class extends x {
	constructor() {
		super(...arguments), oa.set(this, (e) => {
			e.defaultPrevented || this._onClick(e);
		});
	}
	connectedCallback() {
		super.connectedCallback(), this.parentElement?.addEventListener("click", t(this, oa, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.parentElement?.removeEventListener("click", t(this, oa, "f"));
	}
	render() {
		return y`<slot></slot>`;
	}
};
oa = /* @__PURE__ */ new WeakMap(), sa.styles = l`:host { display: contents; } ::slotted(.material-icons) { font-size: inherit !important; }`;
function ca(e) {
	return e === "vertical" || e === "horizontal" || e === "both";
}
var la, ua, da, fa, pa, ma, ha, ga, _a, va = class extends T(x) {
	constructor() {
		super(...arguments), la.add(this), ua.set(this, !1), da.set(this, !1), this.open = !1, this.orientation = "vertical", this.noAnimate = !1, this.slide = !1;
	}
	connectedCallback() {
		super.connectedCallback(), t(this, la, "m", fa).call(this);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("orientation") && t(this, la, "m", fa).call(this);
	}
	update(e) {
		super.update(e);
		let r = this.noAnimate || (e.has("orientation") || e.has("slide")) && !e.has("open");
		zr(this, "--no-animate"), t(this, ua, "f") ? (this.toggleAttribute("inert", !this.open), this.open ? (n(this, da, !0, "f"), r || Bt() || (t(this, la, "m", ma).call(this), D(this, "--overflows", this.orientation === "vertical" ? this.clientHeight < this.scrollHeight : this.orientation === "horizontal" ? this.clientWidth < this.scrollWidth : this.clientHeight < this.scrollHeight || this.clientWidth < this.scrollWidth), t(this, la, "m", ha).call(this)), E(this, "--closing"), zr(this, "--opening"), this.dispatchEvent(new Event("opening")), t(this, la, "m", ha).call(this), E(this, "--no-animate"), t(this, la, "m", ga).call(this), r || Bt() ? (t(this, la, "m", ma).call(this), E(this, "--opening"), this.dispatchEvent(new Event("opened"))) : this.addEventListener("transitionend", () => {
			this.open && (t(this, la, "m", ma).call(this), E(this, "--opening"), this.dispatchEvent(new Event("opened")));
		}, { once: !0 })) : (E(this, "--opening"), zr(this, "--closing"), this.dispatchEvent(new Event("closing")), t(this, la, "m", ga).call(this), t(this, da, "f") && E(this, "--no-animate"), r || Bt() ? (t(this, la, "m", ha).call(this), E(this, "--closing"), this.dispatchEvent(new Event("closed"))) : requestAnimationFrame(() => {
			t(this, la, "m", ha).call(this), this.addEventListener("transitionend", () => {
				this.open || (E(this, "--closing"), this.dispatchEvent(new Event("closed")));
			}, { once: !0 });
		}))) : (this.open && (n(this, da, !0, "f"), t(this, la, "m", ma).call(this)), n(this, ua, !0, "f"));
	}
	render() {
		return y`<slot @slotchange="${t(this, la, "m", pa)}"></slot>`;
	}
};
ua = /* @__PURE__ */ new WeakMap(), da = /* @__PURE__ */ new WeakMap(), la = /* @__PURE__ */ new WeakSet(), fa = function() {
	ca(this.orientation) || (this.orientation = "horizontal"), Br(this, this.orientation, "both", "horizontal", "vertical");
}, pa = function() {
	n(this, ua, !0, "f"), t(this, la, "m", _a).call(this);
}, ma = function() {
	switch (this.orientation) {
		case "vertical":
			this.style.height = "auto";
			break;
		case "horizontal":
			this.style.width = "auto";
			break;
		case "both": this.style.height = this.style.width = "auto";
	}
}, ha = function() {
	switch (this.orientation) {
		case "vertical":
			this.style.height = "";
			break;
		case "horizontal":
			this.style.width = "";
			break;
		case "both": this.style.height = this.style.width = "";
	}
}, ga = function() {
	switch (this.orientation) {
		case "vertical":
			this.style.height = `${this.scrollHeight}px`;
			break;
		case "horizontal":
			this.style.width = `${this.scrollWidth}px`;
			break;
		case "both": this.style.height = `${this.scrollHeight}px`, this.style.width = `${this.scrollWidth}px`;
	}
	t(this, la, "m", _a).call(this);
}, _a = function() {
	if (this.slide) switch (this.orientation) {
		case "vertical":
			this.style.setProperty("--_collapsible-vertical-offset", `${-this.scrollHeight}px`);
			break;
		case "horizontal":
			this.style.setProperty("--_collapsible-horizontal-offset", `${-this.scrollWidth}px`);
			break;
		case "both": this.style.setProperty("--_collapsible-vertical-offset", `${-this.scrollHeight}px`), this.style.setProperty("--_collapsible-horizontal-offset", `${-this.scrollWidth}px`);
	}
}, va.styles = l`:host { display: block; overflow: hidden; --_collapsible-animation: var( --m3e-collapsible-animation, var(--m3e-collapsible-animation-duration, ${A.motion.duration.medium1}) var(--m3e-collapsible-animation-function, ${A.motion.easing.standard}) ); } :host([hidden]) { display: none; } :host(:is(:state(--vertical), :--vertical)) { height: 0px; } :host(:is(:state(--vertical), :--vertical):not([slide])) { transition: ${c("visibility var(--_collapsible-animation),\n        height var(--_collapsible-animation),\n        padding-top var(--_collapsible-animation),\n        padding-bottom var(--_collapsible-animation)")}; } :host(:is(:state(--vertical), :--vertical)[slide]) { transition: ${c("visibility var(--_collapsible-animation),\n        height var(--_collapsible-animation),\n        transform var(--m3e-collapsible-slide-animation, var(--_collapsible-animation)),\n        padding-top var(--_collapsible-animation),\n        padding-bottom var(--_collapsible-animation)")}; } :host(:is(:state(--horizontal), :--horizontal)) { width: 0px; } :host(:is(:state(--horizontal), :--horizontal):not([slide])) { transition: ${c("visibility var(--_collapsible-animation),\n        width var(--_collapsible-animation),\n        padding-left var(--_collapsible-animation),\n        padding-right var(--_collapsible-animation)")}; } :host(:is(:state(--horizontal), :--horizontal)[slide]) { transition: ${c("visibility var(--_collapsible-animation),\n        width var(--_collapsible-animation),\n        transform var(--m3e-collapsible-slide-animation, var(--_collapsible-animation)),\n        padding-left var(--_collapsible-animation),\n        padding-right var(--_collapsible-animation)")}; } :host(:is(:state(--both), :--both)) { height: 0px; width: 0px; } :host(:is(:state(--both), :--both):not([slide])) { transition: ${c("visibility var(--_collapsible-animation),\n        width var(--_collapsible-animation),\n        height var(--_collapsible-animation),\n        padding var(--_collapsible-animation)")}; } :host(:is(:state(--both), :--both)[slide]) { transition: ${c("visibility var(--_collapsible-animation),\n        width var(--_collapsible-animation),\n        height var(--_collapsible-animation),\n        transform var(--m3e-collapsible-slide-animation, var(--_collapsible-animation)),\n        padding var(--_collapsible-animation)")}; } :host(:not(:is(:state(--closing), :--closing)):not([open])) { visibility: hidden; } :host(:is(:state(--vertical), :--vertical):not([open])) { min-height: unset !important; padding-top: 0px !important; padding-bottom: 0px !important; } :host(:is(:state(--horizontal), :--horizontal):not([open])) { min-width: unset !important; padding-left: 0px !important; padding-right: 0px !important; } :host(:is(:state(--both), :--both):not([open])) { min-height: unset !important; min-width: unset !important; padding: 0px !important; } :host([no-animate]), :host(:is(:state(--no-animate), :--no-animate)) { transition-duration: 0ms; } :host(:is(:state(--vertical), :--vertical):is(:state(--opening), :--opening)), :host(:is(:state(--vertical), :--vertical):is(:state(--closing), :--closing)) { overflow-y: hidden !important; } :host(:is(:state(--horizontal), :--horizontal):is(:state(--opening), :--opening)), :host(:is(:state(--horizontal), :--horizontal):is(:state(--closing), :--closing)) { overflow-x: hidden !important; } :host(:is(:state(--both), :--both):is(:state(--opening), :--opening)), :host(:is(:state(--both), :--both):is(:state(--closing), :--closing)) { overflow-y: hidden !important; overflow-x: hidden !important; } :host(:is(:state(--overflows), :--overflows)) { scrollbar-gutter: stable; } :host(:is(:state(--vertical), :--vertical)[slide]:not([open])) { transform: translateY(var(--_collapsible-vertical-offset, 0px)); } :host(:is(:state(--vertical), :--vertical)[slide][open]) { transform: translateY(0px); } :host(:is(:state(--horizontal), :--horizontal)[slide]:not([open])) { transform: translateX(var(--_collapsible-horizontal-offset, 0px)); } :host(:is(:state(--horizontal), :--horizontal)[slide][open]) { transform: translateX(0px); } :host(:is(:state(--both), :--both)[slide]:not([open])) { transform: translateX(var(--_collapsible-horizontal-offset, 0px)) translateY(var(--_collapsible-vertical-offset, 0px)); } :host(:is(:state(--both), :--both)[slide][open]) { transform: translateX(0px) translateY(0px); } ::slotted(*) { --m3e-collapsible-animation: initial; --m3e-collapsible-animation-duration: initial; --m3e-collapsible-animation-function: initial; } @media (prefers-reduced-motion) { :host(:is(:state(--vertical), :--vertical):not([slide])), :host(:is(:state(--vertical), :--vertical)[slide]), :host(:is(:state(--horizontal), :--horizontal):not([slide])), :host(:is(:state(--horizontal), :--horizontal)[slide]), :host(:is(:state(--both), :--both):not([slide])), :host(:is(:state(--both), :--both)[slide]) { transition: none; } }`, e([S({
	type: Boolean,
	reflect: !0
})], va.prototype, "open", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], va.prototype, "orientation", void 0), e([S({
	attribute: "no-animate",
	type: Boolean,
	reflect: !0
})], va.prototype, "noAnimate", void 0), e([S({
	type: Boolean,
	reflect: !0
})], va.prototype, "slide", void 0), va = e([w("m3e-collapsible")], va);
function ya(e) {
	return e === 0 || e === 1 || e === 2 || e === 3 || e === 4 || e === 5;
}
var ba = {
	liftDuration: c(`var(--m3e-elevation-lift-duration, ${A.motion.duration.short4})`),
	liftEasing: c(`var(--m3e-elevation-lift-easing, ${A.motion.easing.standard})`),
	settleDuration: c(`var(--m3e-elevation-settle-duration, ${A.motion.duration.short3})`),
	settleEasing: c(`var(--m3e-elevation-settle-easing, ${A.motion.easing.standardAccelerate})`),
	level: c(`var(--m3e-elevation-level, ${A.elevation.level0})`),
	hoverLevel: c(`var(--m3e-elevation-hover-level, ${A.elevation.level0})`),
	focusLevel: c(`var(--m3e-elevation-focus-level, ${A.elevation.level0})`),
	pressedLevel: c(`var(--m3e-elevation-pressed-level, ${A.elevation.level0})`)
}, xa, Sa, Ca, wa, Ta, Ea, Da, Oa = class extends Ki(j(x, "none")) {
	constructor() {
		super(...arguments), xa.add(this), Sa.set(this, new yn(this, {
			target: null,
			callback: (e) => t(this, xa, "m", Ta).call(this, e)
		})), Ca.set(this, new sn(this, {
			target: null,
			callback: (e) => t(this, xa, "m", Ea).call(this, e)
		})), wa.set(this, new nr(this, {
			target: null,
			callback: (e) => t(this, xa, "m", Da).call(this, e)
		})), this.disabled = !1, this.level = null;
	}
	attach(e) {
		super.attach(e), t(this, Sa, "f").observe(e), t(this, Ca, "f").observe(e), t(this, wa, "f").observe(e);
	}
	detach() {
		this.control && (t(this, Sa, "f").unobserve(this.control), t(this, Ca, "f").unobserve(this.control), t(this, wa, "f").unobserve(this.control)), super.detach();
	}
	connectedCallback() {
		this.ariaHidden = "true", super.connectedCallback();
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this._shadow?.classList.toggle("hover", !1), this._shadow?.classList.toggle("focus", !1), this._shadow?.classList.toggle("pressed", !1), this._shadow?.classList.toggle("resting", !1);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("level") && this.level !== null && !ya(this.level) && (this.level = null);
	}
	updated(e) {
		super.updated(e), e.has("disabled") && this.disabled && (this._shadow?.classList.toggle("hover", !1), this._shadow?.classList.toggle("focus", !1), this._shadow?.classList.toggle("pressed", !1), this._shadow?.classList.toggle("resting", !0));
	}
	render() {
		return y`<div class="shadow"></div>`;
	}
};
Sa = /* @__PURE__ */ new WeakMap(), Ca = /* @__PURE__ */ new WeakMap(), wa = /* @__PURE__ */ new WeakMap(), xa = /* @__PURE__ */ new WeakSet(), Ta = function(e) {
	this.disabled || (this._shadow?.classList.toggle("hover", e), this._shadow?.classList.toggle("resting", !e));
}, Ea = function(e) {
	this.disabled || this._shadow?.classList.toggle("focus", e);
}, Da = function(e) {
	this.disabled || this._shadow?.classList.toggle("pressed", e);
}, Oa.styles = l`:host { display: block; } :host([hidden]) { display: none; } :host, .shadow { position: absolute; left: 0; top: 0; right: 0; bottom: 0; pointer-events: none; border-radius: inherit; } .shadow.resting, .shadow.focus, .shadow.pressed { will-change: box-shadow; transition: ${c(`box-shadow ${ba.settleDuration} ${ba.settleEasing};`)}; } .shadow.hover { will-change: box-shadow; transition: ${c(`box-shadow ${ba.liftDuration} ${ba.liftEasing};`)}; } .shadow { box-shadow: ${ba.level}; } .shadow.focus { box-shadow: ${ba.focusLevel}; } .shadow.hover { box-shadow: ${ba.hoverLevel}; } .shadow.pressed { box-shadow: ${ba.pressedLevel}; } :host([level="0"]) .shadow { --m3e-elevation-level: ${A.elevation.level0}; --m3e-elevation-hover-level: ${A.elevation.level1}; } :host([level="1"]) .shadow { --m3e-elevation-level: ${A.elevation.level1}; --m3e-elevation-hover-level: ${A.elevation.level2}; } :host([level="2"]) .shadow { --m3e-elevation-level: ${A.elevation.level2}; --m3e-elevation-hover-level: ${A.elevation.level3}; } :host([level="3"]) .shadow { --m3e-elevation-level: ${A.elevation.level3}; --m3e-elevation-hover-level: ${A.elevation.level4}; } :host([level="4"]) .shadow { --m3e-elevation-level: ${A.elevation.level4}; --m3e-elevation-hover-level: ${A.elevation.level5}; } :host([level="5"]) .shadow { --m3e-elevation-level: ${A.elevation.level5}; --m3e-elevation-hover-level: ${ba.level}; } :host([level]) .shadow { --m3e-elevation-focus-level: ${ba.level}; --m3e-elevation-pressed-level: ${ba.level}; } @media (prefers-reduced-motion) { .shadow.resting, .shadow.pressed, .shadow.focus, .shadow.hover { transition: none; } } @media (forced-colors: active) { .shadow { display: none; } }`, e([C(".shadow")], Oa.prototype, "_shadow", void 0), e([S({
	type: Boolean,
	reflect: !0
})], Oa.prototype, "disabled", void 0), e([S({
	type: Number,
	reflect: !0,
	useDefault: !0
})], Oa.prototype, "level", void 0), Oa = e([w("m3e-elevation")], Oa);
var ka = {
	color: c(`var(--m3e-focus-ring-color, ${A.color.secondary})`),
	duration: c(`var(--m3e-focus-ring-duration, ${A.motion.duration.long2})`),
	thickness: c("var(--m3e-focus-ring-thickness, 3px)"),
	outwardOffset: c("var(--m3e-focus-ring-outward-offset, 2px)"),
	inwardOffset: c("var(--m3e-focus-ring-inward-offset, 0px)"),
	visibility: c("var(--m3e-focus-ring-visibility, visible)"),
	growthFactor: c("var(--m3e-focus-ring-growth-factor, 2)")
}, Aa, ja, Ma, Na = class extends Ki(j(x, "none")) {
	constructor() {
		super(...arguments), Aa.add(this), ja.set(this, new sn(this, {
			target: null,
			callback: (e, n) => t(this, Aa, "m", Ma).call(this, n)
		})), this.inward = !1, this.disabled = !1;
	}
	show() {
		this._outline?.classList.toggle("visible", !0);
	}
	hide() {
		this._outline?.classList.toggle("visible", !1);
	}
	attach(e) {
		super.attach(e), t(this, ja, "f").observe(e);
	}
	detach() {
		this.control && t(this, ja, "f").unobserve(this.control), super.detach();
	}
	connectedCallback() {
		this.ariaHidden = "true", super.connectedCallback();
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.hide();
	}
	render() {
		return y`<div class="outline"></div>`;
	}
	updated(e) {
		super.updated(e), e.has("disabled") && this.disabled && this.hide();
	}
};
ja = /* @__PURE__ */ new WeakMap(), Aa = /* @__PURE__ */ new WeakSet(), Ma = function(e) {
	this.disabled || (e ? this.show() : this.hide());
}, Na.styles = l`:host { display: block; position: absolute; left: 0; top: 0; right: 0; bottom: 0; pointer-events: none; border-radius: inherit; outline: none; } :host([hidden]) { display: none; } .outline { contain: layout style; position: absolute; left: 0; top: 0; right: 0; bottom: 0; pointer-events: none; border-radius: inherit; z-index: 1; outline-color: ${ka.color}; outline-width: ${ka.thickness}; visibility: ${ka.visibility}; } .outline.visible { outline-style: solid; } :host(:not([inward])) .outline { outline-offset: ${ka.outwardOffset}; } :host([inward]) .outline { outline-offset: calc(${ka.inwardOffset} - ${ka.thickness}); } :host(:not([inward])) .outline.visible { animation: grow-shrink ${ka.duration}; } :host([inward]) .outline.visible { animation: shrink-grow ${ka.duration}; } @keyframes grow-shrink { 50% { outline-width: calc(${ka.thickness} * ${ka.growthFactor}); } } @keyframes shrink-grow { 50% { outline-offset: calc( ${ka.inwardOffset} - calc(${ka.thickness} * ${ka.growthFactor}) ); outline-width: calc(${ka.thickness} * ${ka.growthFactor}); } } @media (prefers-reduced-motion) { :host(:not([inward])) .outline.visible, :host([inward]) .outline.visible { animation: none; } } @media (forced-colors: active) { .outline { outline-color: Highlight; } }`, e([C(".outline")], Na.prototype, "_outline", void 0), e([S({
	type: Boolean,
	reflect: !0
})], Na.prototype, "inward", void 0), e([S({
	type: Boolean,
	reflect: !0
})], Na.prototype, "disabled", void 0), Na = e([w("m3e-focus-ring")], Na);
var Pa = class extends Wr(vi(j(x, "none"))) {
	connectedCallback() {
		super.connectedCallback(), this.ariaHidden = "true";
	}
	render() {
		return this.indeterminate ? y`<svg viewBox="0 -960 960 960" fill="currentColor"><path Required d="M240-440v-80h480v80H240Z"/></svg>` : this.checked ? y`<svg viewBox="0 -960 960 960" fill="currentColor"><path d="M382-240 154-468l57-57 171 171 367-367 57 57-424 424Z"/></svg>` : b;
	}
};
Pa.styles = l`:host { display: inline-block; vertical-align: middle; width: var(--m3e-checkbox-icon-size, 18px); height: var(--m3e-checkbox-icon-size, 18px); border-radius: var(--m3e-checkbox-container-shape, 2px); box-sizing: border-box; flex: none; contain: layout style paint; line-height: 0; } :host([hidden]) { display: none; } :host(:not([checked]):not([indeterminate])) { border-width: var(--m3e-checkbox-unselected-outline-thickness, 2px); border-style: solid; } :host(:not([disabled])[checked]), :host(:not([disabled])[indeterminate]) { background-color: var(--m3e-checkbox-selected-container-color, ${A.color.primary}); color: var(--m3e-checkbox-selected-icon-color, ${A.color.onPrimary}); } :host(:not([disabled]):not([checked]):not([indeterminate])) { border-color: var(--m3e-checkbox-unselected-outline-color, ${A.color.onSurfaceVariant}); } :host([disabled]:not([checked]):not([indeterminate])) { border-color: color-mix( in srgb, var(--m3e-checkbox-unselected-disabled-outline-color, ${A.color.onSurface}) var(--m3e-checkbox-unselected-disabled-outline-opacity, 38%), transparent ); } :host([disabled][checked]), :host([disabled][indeterminate]) { background-color: color-mix( in srgb, var(--m3e-checkbox-selected-disabled-container-color, ${A.color.onSurface}) var(--m3e-checkbox-selected-disabled-container-opacity, 38%), transparent ); color: color-mix( in srgb, var(--m3e-checkbox-selected-disabled-icon-color, ${A.color.surface}) var(--m3e-checkbox-selected-disabled-icon-opacity, 100%), transparent ); } svg { pointer-events: none; } @media (forced-colors: active) { :host(:not([disabled])[checked]), :host(:not([disabled])[indeterminate]) { border-color: Highlight; background-color: Highlight; color: HighlightText; } :host(:not([disabled]):not([checked]):not([indeterminate])) { border-color: CanvasText; background: Canvas; } :host([disabled]:not([checked]):not([indeterminate])) { border-color: GrayText; background-color: Canvas; } :host([disabled][checked]), :host([disabled][indeterminate]) { background-color: GrayText; color: Canvas; } }`, Pa = e([w("m3e-pseudo-checkbox")], Pa);
var Fa = class extends Hr(vi(j(x, "none"))) {
	connectedCallback() {
		super.connectedCallback(), this.ariaHidden = "true";
	}
	render() {
		return y`<svg class="icon" viewBox="0 0 20 20"><mask id="cutout2"><rect width="100%" height="100%" fill="white"></rect><circle cx="10" cy="10" r="8" fill="black"></circle></mask><circle class="outer circle" cx="10" cy="10" r="10" mask="url(#cutout2)"></circle><circle class="inner circle" cx="10" cy="10" r="5"></circle></svg>`;
	}
};
Fa.styles = l`:host { display: inline-block; vertical-align: middle; box-sizing: border-box; width: var(--m3e-radio-icon-size, 20px); height: var(--m3e-radio-icon-size, 20px); flex: none; contain: layout style paint; line-height: 0; } :host([hidden]) { display: none; } .circle { fill: currentColor; } :host(:not([checked])) .circle.inner { opacity: 0; } :host(:not([checked])) { color: var(--m3e-radio-unselected-icon-color, ${A.color.onSurfaceVariant}); } :host([checked]) { color: var(--m3e-radio-selected-icon-color, ${A.color.primary}); } :host([disabled]) { color: color-mix(in srgb, var(--m3e-radio-disabled-icon-color, ${A.color.onSurface}) 38%, transparent); } @media (forced-colors: active) { :host { border-radius: 50%; } :host(:not([checked])) { color: CanvasText; background-color: Canvas; } :host([checked]) { color: HighlightText; background-color: Highlight; } :host([disabled]) { color: GrayText; background-color: Canvas; } }`, Fa = e([w("m3e-pseudo-radio")], Fa);
var Ia = {
	color: c(`var(--m3e-ripple-color, ${A.color.onSurface})`),
	opacity: c(`var(--m3e-ripple-opacity, ${A.state.pressedStateLayerOpacity})`),
	enterDuration: c(`var(--m3e-ripple-enter-duration, ${A.motion.duration.long1})`),
	exitDuration: c(`var(--m3e-ripple-exit-duration, ${A.motion.duration.medium3})`)
}, La, Ra, za, Ba, Va, Ha, Ua = class extends Ki(j(x, "none")) {
	constructor() {
		super(...arguments), La.add(this), Ra.set(this, /* @__PURE__ */ new Set()), za.set(this, new nr(this, {
			target: null,
			minPressedDuration: 0,
			isPressedKey: (e) => e === " ",
			callback: (e, { x: n, y: r }) => t(this, La, "m", Ha).call(this, e, n, r)
		})), this.disabled = !1, this.centered = !1, this.unbounded = !1, this.radius = null;
	}
	get visible() {
		return t(this, Ra, "f").size > 0;
	}
	show(e, n, r = !1) {
		let i = this.getBoundingClientRect();
		this.centered && (e = i.left + i.width / 2, n = i.top + i.height / 2);
		let a = this.radius;
		if (!a || isNaN(a)) {
			let t = Math.max(Math.abs(e - i.left), Math.abs(e - i.right)), r = Math.max(Math.abs(n - i.top), Math.abs(n - i.bottom));
			a = Math.sqrt(t * t + r * r);
		}
		let o = e - i.left, s = n - i.top, c = document.createElement("div");
		c.classList.add("ripple"), r && c.classList.add("persistent"), c.style.left = `${o - a}px`, c.style.top = `${s - a}px`, c.style.width = `${a * 2}px`, c.style.height = `${a * 2}px`, c.addEventListener("animationend", () => t(this, La, "m", Va).call(this, c, r), { once: !0 }), c.addEventListener("transitionend", (e) => {
			e.propertyName === "opacity" && t(this, La, "m", Ba).call(this, c);
		}, { once: !0 }), this.shadowRoot ? (t(this, Ra, "f").add(c), this.shadowRoot.appendChild(c)) : t(this, Ra, "f").delete(c);
	}
	hide() {
		for (let e of t(this, Ra, "f")) e.classList.add("exit");
	}
	attach(e) {
		super.attach(e), t(this, za, "f").observe(e);
	}
	detach() {
		this.control && t(this, za, "f").unobserve(this.control), super.detach();
	}
	connectedCallback() {
		this.ariaHidden = "true", super.connectedCallback();
	}
	disconnectedCallback() {
		super.disconnectedCallback(), t(this, La, "m", Ba).call(this);
	}
	updated(e) {
		super.updated(e), e.has("disabled") && this.disabled && this.hide();
	}
};
Ra = /* @__PURE__ */ new WeakMap(), za = /* @__PURE__ */ new WeakMap(), La = /* @__PURE__ */ new WeakSet(), Ba = function(e) {
	if (e) e.remove(), t(this, Ra, "f").delete(e);
	else {
		for (let e of t(this, Ra, "f")) e.remove();
		t(this, Ra, "f").clear();
	}
}, Va = function(e, t) {
	t ? e.classList.add("pressed") : e.classList.add("exit");
}, Ha = function(e, t, n) {
	this.disabled || (e ? this.show(t, n, !0) : this.hide());
}, Ua.styles = l`:host { display: block; position: absolute; left: 0; top: 0; right: 0; bottom: 0; pointer-events: none; border-radius: inherit; } :host([hidden]) { display: none; } :host(:not([unbounded])) { overflow: hidden; } :host(:not([unbounded])) .ripple { contain: layout style paint; } :host([unbounded]) .ripple { contain: layout style; } .ripple { display: block; position: absolute; inset: 0; pointer-events: none; transform: scale(0); opacity: ${Ia.opacity}; border-radius: 50%; filter: blur(20px); background-color: ${Ia.color}; transition: ${c(`background-color ${A.motion.duration.short1} ${A.motion.easing.standard}`)}; will-change: transform, background-color, opacity; animation: ripple ${Ia.enterDuration} linear; } .ripple.persistent.pressed { transform: scale(4); } .ripple.exit { transition: ${c(`opacity ${Ia.exitDuration} cubic-bezier(0, 0, 0.2, 0.1),
        background-color ${A.motion.duration.short1} ${A.motion.easing.standard}`)}; opacity: 0; } @keyframes ripple { to { transform: scale(4); } } @media (prefers-reduced-motion) { .ripple { transform: scale(4); animation-duration: 90ms; } .ripple:not(.exit), .ripple.exit { transition-duration: 10ms; } } @media (forced-colors: active) { .ripple { display: none; } }`, e([S({
	type: Boolean,
	reflect: !0
})], Ua.prototype, "disabled", void 0), e([S({
	type: Boolean,
	reflect: !0
})], Ua.prototype, "centered", void 0), e([S({
	type: Boolean,
	reflect: !0
})], Ua.prototype, "unbounded", void 0), e([S({ type: Number })], Ua.prototype, "radius", void 0), Ua = e([w("m3e-ripple")], Ua);
function Wa(e) {
	return e === "above" || e === "below" || e === "above-below" || e === "none";
}
var Ga, Ka = class extends T(x) {
	constructor() {
		super(...arguments), Ga.set(this, () => this._updateScroll()), this.dividers = "above-below", this.thin = !1;
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("scroll", t(this, Ga, "f"));
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("dividers") && !Wa(this.dividers) && (this.dividers = "above-below");
	}
	update(e) {
		super.update(e), e.has("dividers") && (this.removeEventListener("scroll", t(this, Ga, "f")), this.dividers !== "none" && this.addEventListener("scroll", t(this, Ga, "f"), { passive: !0 }));
	}
	render() {
		return y`<slot @slotchange="${this._updateScroll}"></slot>`;
	}
	_updateScroll() {
		let e = (this.dividers === "above" || this.dividers === "above-below") && this.scrollTop > 0, t = (this.dividers === "below" || this.dividers === "above-below") && this.scrollHeight - this.scrollTop - this.clientHeight > 1;
		D(this, "--above", e), D(this, "--below", t);
	}
};
Ga = /* @__PURE__ */ new WeakMap(), Ka.styles = l`:host { display: block; overflow-y: auto; position: relative; box-sizing: border-box; scrollbar-color: ${A.scrollbar.color}; border-top: var(--m3e-divider-thickness, 1px) solid transparent; border-bottom: var(--m3e-divider-thickness, 1px) solid transparent; outline-color: ${ka.color}; outline-width: ${ka.thickness}; outline-offset: ${ka.outwardOffset}; } :host([hidden]) { display: none; } :host([thin]) { scrollbar-width: ${A.scrollbar.thinWidth}; } :host(:not([thin])) { scrollbar-width: ${A.scrollbar.width}; } :host(:not(:focus-visible):is(:state(--above), :--above)) { border-top-color: var(--m3e-divider-color, ${A.color.outlineVariant}); } :host(:not(:focus-visible):is(:state(--below), :--below)) { border-bottom-color: var(--m3e-divider-color, ${A.color.outlineVariant}); } :host(:focus-visible) { outline-style: solid; animation: grow-shrink ${ka.duration}; } @keyframes grow-shrink { 50% { outline-width: calc(${ka.thickness} * ${ka.growthFactor}); } } @media (forced-colors: active) { :host { border-top: var(--m3e-divider-thickness, 1px) solid Canvas; border-bottom: var(--m3e-divider-thickness, 1px) solid Canvas; } :host(:not(:focus-visible):is(:state(--above), :--above)) { border-top-color: GrayText; } :host(:not(:focus-visible):is(:state(--below), :--below)) { border-bottom-color: GrayText; } } @media (prefers-reduced-motion) { :host(:focus-visible) { animation: none; } }`, e([S()], Ka.prototype, "dividers", void 0), e([S({
	type: Boolean,
	reflect: !0
})], Ka.prototype, "thin", void 0), e([lr(40)], Ka.prototype, "_updateScroll", null), Ka = e([w("m3e-scroll-container")], Ka);
var qa, Ja, Ya, Xa, Za, Qa = class extends ia(ea(Ki(j(T(x), "none")))) {
	constructor() {
		super(...arguments), qa.add(this), Ja.set(this, new nr(this, {
			target: null,
			isPressedKey: (e) => e === " ",
			callback: (e, n) => t(this, qa, "m", Xa).call(this, e, n)
		})), this.disabled = !1, this.selected = !1, this.bounce = !1, this.centered = !1;
	}
	show(e) {
		this._stateLayer?.show(e);
	}
	hide(e) {
		this._stateLayer?.hide(e);
	}
	attach(e) {
		super.attach(e), t(this, Ja, "f").observe(e), this._stateLayer?.attach(e);
	}
	detach() {
		this.control && (t(this, Ja, "f").unobserve(this.control), this._stateLayer?.detach()), super.detach();
	}
	connectedCallback() {
		this.ariaHidden = "true", super.connectedCallback();
	}
	reconnectedCallback() {
		super.reconnectedCallback(), t(this, qa, "m", Ya).call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), t(this, qa, "m", Ya).call(this);
	}
	render() {
		return y`<div class="base" @animationend="${t(this, qa, "m", Za)}"><div class="indicator"></div><m3e-state-layer class="state-layer" enable-pressed ?disabled="${this.disabled}"></m3e-state-layer></div>`;
	}
};
Ja = /* @__PURE__ */ new WeakMap(), qa = /* @__PURE__ */ new WeakSet(), Ya = function() {
	let e = this._stateLayer;
	e && this.control !== e.control && (this.control ? e.attach(this.control) : e.detach());
}, Xa = function(e, t) {
	if (this.disabled || this.selected || this.centered || Bt()) return;
	let n = this._base;
	if (e && n) {
		let e = n.getBoundingClientRect(), r = Math.min(Math.max(t.x - e.left, e.height), e.width - e.height);
		n?.style.setProperty("--_pressed-origin-x", `${r}px`);
	}
}, Za = function() {
	this._base?.style.removeProperty("--_pressed-origin-x");
}, Qa.styles = l`:host { display: block; } :host([hidden]) { display: none; } .state-layer { --m3e-state-layer-hover-color: var( --m3e-selection-indicator-state-layer-hover-color, ${A.color.onSurface} ); --m3e-state-layer-hover-opacity: var( --m3e-selection-indicator-state-layer-hover-opacity, ${A.state.hoverStateLayerOpacity} ); --m3e-state-layer-focus-color: var( --m3e-selection-indicator-state-layer-focus-color, ${A.color.onSurface} ); --m3e-state-layer-focus-opacity: var( --m3e-selection-indicator-state-layer-focus-opacity, ${A.state.focusStateLayerOpacity} ); --m3e-state-layer-pressed-color: var( --m3e-selection-indicator-state-layer-pressed-color, ${A.color.onSurface} ); --m3e-state-layer-pressed-opacity: var( --m3e-selection-indicator-state-layer-pressed-opacity, ${A.state.pressedStateLayerOpacity} ); --m3e-state-layer-duration: var( --m3e-selection-indicator-state-layer-duration, ${A.motion.duration.medium1} ); --m3e-state-layer-easing: var( --m3e-selection-indicator-state-layer-easing, ${A.motion.easing.standard} ); } :host, .base, .indicator { position: absolute; inset: 0; pointer-events: none; border-radius: inherit; } .indicator { opacity: 0; will-change: opacity; } :host(:not([disabled])) .indicator { background-color: var(--m3e-selection-indicator-color, ${A.color.secondaryContainer}); } :host(:not(:is(:state(--no-animate), :--no-animate))) .indicator { transition: ${c(`opacity var(--m3e-selection-indicator-state-layer-duration, ${A.motion.duration.medium1}) var(--m3e-selection-indicator-state-layer-easing, ${A.motion.easing.standard})`)}; } .base { contain: layout style paint; } :host(:not([centered])) .base { transform-origin: var(--_pressed-origin-x, center) center; } :host([centered]) .base { transform-origin: center center; } :host([selected]:not([bounce]):not(:is(:state(--no-animate), :--no-animate))) .base { animation: grow ${A.motion.spring.fastEffects}; } :host([selected][bounce]:not(:is(:state(--no-animate), :--no-animate))) .base { animation: grow ${A.motion.spring.fastSpatial}; } :host([selected]) .indicator { opacity: 1; transition: none; } :host(:not(:is(:state(--no-animate), :--no-animate))) .indicator { transition: ${c(`opacity ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; } @keyframes grow { 0% { transform: scaleX(0); } 100% { transform: scaleX(1); } } @media (prefers-reduced-motion) { :host(:not(:is(:state(--no-animate), :--no-animate))) .indicator { transition: none; } :host([selected]:not([bounce]):not(:is(:state(--no-animate), :--no-animate))) .base, :host([selected][bounce]:not(:is(:state(--no-animate), :--no-animate))) .base { animation: none; } }`, e([C(".base")], Qa.prototype, "_base", void 0), e([C(".state-layer")], Qa.prototype, "_stateLayer", void 0), e([S({
	type: Boolean,
	reflect: !0
})], Qa.prototype, "disabled", void 0), e([S({
	type: Boolean,
	reflect: !0
})], Qa.prototype, "selected", void 0), e([S({
	type: Boolean,
	reflect: !0
})], Qa.prototype, "bounce", void 0), e([S({
	type: Boolean,
	reflect: !0
})], Qa.prototype, "centered", void 0), Qa = e([w("m3e-selection-indicator")], Qa);
var $a, eo, to, no, ro = class extends T(x) {
	constructor() {
		super(...arguments), $a.add(this), eo.set(this, []), this.selectedIndex = null;
	}
	connectedCallback() {
		super.connectedCallback(), zr(this, "--no-animate");
	}
	update(e) {
		super.update(e), e.has("selectedIndex") && (this.selectedIndex === null && zr(this, "--no-animate"), t(this, $a, "m", no).call(this), this.selectedIndex !== null && Rr(this, "--no-animate") && requestAnimationFrame(() => {
			this.selectedIndex !== null && E(this, "--no-animate");
		}));
	}
	render() {
		return y`<slot @slotchange="${t(this, $a, "m", to)}"></slot>`;
	}
};
eo = /* @__PURE__ */ new WeakMap(), $a = /* @__PURE__ */ new WeakSet(), to = function(e) {
	let r = [...e.target.assignedElements({ flatten: !0 })];
	for (let e of t(this, eo, "f").filter((e) => !r.includes(e))) e.classList.remove("-before"), e.classList.remove("-after"), e.removeAttribute("inert");
	n(this, eo, r, "f"), t(this, $a, "m", no).call(this);
}, no = function() {
	let e = this.selectedIndex ?? t(this, eo, "f").length;
	for (let n = 0; n < t(this, eo, "f").length; n++) {
		let r = t(this, eo, "f")[n];
		r.classList.toggle("-before", n < e), r.classList.toggle("-after", n > e), r.toggleAttribute("inert", n !== e);
	}
}, ro.styles = l`:host { display: flex; position: relative; overflow: hidden; } :host([hidden]) { display: none; } ::slotted(*) { width: 100%; top: 0; transition: ${c(`inset-inline-start var(--m3e-slide-animation-duration, ${A.motion.duration.long2}) ${A.motion.easing.standard},
        visibility var(--m3e-slide-animation-duration, ${A.motion.duration.long2}) ${A.motion.easing.standard} allow-discrete`)}; } ::slotted(.-before), ::slotted(.-after) { visibility: hidden; position: absolute; } ::slotted(.-before) { inset-inline-start: -100%; } ::slotted(.-after) { inset-inline-start: 100%; } ::slotted(:not(.-before):not(.-after)) { position: relative; inset-inline-start: 0; } :host(:is(:state(--no-animate), :--no-animate)) ::slotted(*) { transition: none; } @media (prefers-reduced-motion) { ::slotted(*) { transition: none; } }`, e([S({
	attribute: "selected-index",
	type: Number,
	reflect: !0
})], ro.prototype, "selectedIndex", void 0), ro = e([w("m3e-slide")], ro);
var io = {
	hoverColor: c(`var(--m3e-state-layer-hover-color, ${A.color.onSurface})`),
	hoverOpacity: c(`var(--m3e-state-layer-hover-opacity, ${A.state.hoverStateLayerOpacity})`),
	focusColor: c(`var(--m3e-state-layer-focus-color, ${A.color.onSurface})`),
	focusOpacity: c(`var(--m3e-state-layer-focus-opacity, ${A.state.focusStateLayerOpacity})`),
	pressedColor: c(`var(--m3e-state-layer-pressed-color, ${A.color.onSurface})`),
	pressedOpacity: c(`var(--m3e-state-layer-pressed-opacity, ${A.state.pressedStateLayerOpacity})`),
	duration: c(`var(--m3e-state-layer-duration, ${A.motion.duration.medium1})`),
	easing: c(`var(--m3e-state-layer-easing, ${A.motion.easing.standard})`)
}, ao, oo, so, co, lo, uo, fo, po, mo, ho = class extends Ki(j(x, "none")) {
	constructor() {
		super(...arguments), ao.add(this), oo.set(this, new yn(this, {
			target: null,
			callback: (e) => t(this, ao, "m", lo).call(this, e)
		})), so.set(this, new sn(this, {
			target: null,
			callback: (e, n) => t(this, ao, "m", uo).call(this, n)
		})), co.set(this, new nr(this, {
			target: null,
			capture: !0,
			minPressedDuration: 225,
			isPressedKey: (e) => e === " ",
			callback: (e) => t(this, ao, "m", fo).call(this, e)
		})), this.disabled = !1, this.disableHover = !1, this.enablePressed = !1;
	}
	show(e) {
		this._layer?.classList.toggle(e, !0);
	}
	hide(e) {
		this._layer?.classList.toggle(e, !1);
	}
	attach(e) {
		super.attach(e), t(this, so, "f").observe(e), t(this, ao, "m", po).call(this), t(this, ao, "m", mo).call(this);
	}
	detach() {
		this.control && (t(this, oo, "f").unobserve(this.control), t(this, so, "f").unobserve(this.control), t(this, co, "f").unobserve(this.control)), super.detach();
	}
	connectedCallback() {
		this.ariaHidden = "true", super.connectedCallback();
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.hide("hover"), this.hide("focused");
	}
	updated(e) {
		super.updated(e), e.has("disabled") && this.disabled && (this.hide("hover"), this.hide("focused")), e.has("disableHover") && this.disableHover && (t(this, ao, "m", po).call(this), this.hide("hover")), e.has("enablePressed") && !this.enablePressed && (t(this, ao, "m", mo).call(this), this.hide("pressed"));
	}
	render() {
		return y`<div class="layer"></div>`;
	}
};
oo = /* @__PURE__ */ new WeakMap(), so = /* @__PURE__ */ new WeakMap(), co = /* @__PURE__ */ new WeakMap(), ao = /* @__PURE__ */ new WeakSet(), lo = function(e) {
	!this.disabled && !this.disableHover && (e ? this.show("hover") : this.hide("hover"));
}, uo = function(e) {
	this.disabled || (e ? this.show("focused") : this.hide("focused"));
}, fo = function(e) {
	!this.disabled && this.enablePressed && (e ? this.show("pressed") : this.hide("pressed"));
}, po = function() {
	this.control && (this.disableHover ? t(this, oo, "f").unobserve(this.control) : t(this, oo, "f").observe(this.control));
}, mo = function() {
	this.control && (this.enablePressed ? t(this, co, "f").observe(this.control) : t(this, co, "f").unobserve(this.control));
}, ho.styles = l`:host { display: block; } :host([hidden]) { display: none; } :host, .layer { position: absolute; left: 0; top: 0; right: 0; bottom: 0; pointer-events: none; border-radius: inherit; } .layer { contain: layout style paint; will-change: background-color; transition: ${c(`background-color ${io.duration} ${io.easing}`)}; } .layer.focused { background-color: color-mix(in srgb, ${io.focusColor} ${io.focusOpacity}, transparent); } .layer.hover { background-color: color-mix(in srgb, ${io.hoverColor} ${io.hoverOpacity}, transparent); } .layer.pressed { background-color: color-mix( in srgb, ${io.pressedColor} ${io.pressedOpacity}, transparent ); } @media (prefers-reduced-motion) { .layer { transition: none; } } @media (forced-colors: active) { .layer { display: none; } }`, e([C(".layer")], ho.prototype, "_layer", void 0), e([S({
	type: Boolean,
	reflect: !0
})], ho.prototype, "disabled", void 0), e([S({
	attribute: "disable-hover",
	type: Boolean,
	reflect: !0
})], ho.prototype, "disableHover", void 0), e([S({
	attribute: "enable-pressed",
	type: Boolean,
	reflect: !0
})], ho.prototype, "enablePressed", void 0), ho = e([w("m3e-state-layer")], ho);
var go, _o, vo, yo, bo, xo, So, Co, wo = Co = class extends x {
	constructor() {
		super(), go.add(this), _o.set(this, `m3e-text-highlight-${Co.__nextId++}`), vo.set(this, []), yo.set(this, null), this.disabled = !1, this.term = "", this.caseSensitive = !1, this.mode = "contains", this.isSupported && (n(this, yo, new CSSStyleSheet(), "f"), t(this, yo, "f").replaceSync(l`::highlight(${c(t(this, _o, "f"))}) { background-color: var(--m3e-text-highlight-container-color, Highlight); color: var(--m3e-text-highlight-color, HighlightText); text-decoration: var(--m3e-text-highlight-decoration); text-shadow: var(--m3e-text-highlight-shadow); }`.toString()));
	}
	get isSupported() {
		return !!CSS.highlights;
	}
	get ranges() {
		return t(this, vo, "f");
	}
	firstUpdated(e) {
		super.firstUpdated(e), this.shadowRoot && t(this, yo, "f") && !this.shadowRoot.adoptedStyleSheets.includes(t(this, yo, "f")) && this.shadowRoot.adoptedStyleSheets.push(t(this, yo, "f"));
	}
	updated(e) {
		super.updated(e), (e.has("term") || e.has("caseSensitive") || e.has("disabled")) && t(this, go, "m", So).call(this);
	}
	render() {
		return y`<slot @slotchange="${t(this, go, "m", So)}"></slot>`;
	}
};
_o = /* @__PURE__ */ new WeakMap(), vo = /* @__PURE__ */ new WeakMap(), yo = /* @__PURE__ */ new WeakMap(), go = /* @__PURE__ */ new WeakSet(), bo = function(e) {
	return !/^\s*$/.test(e.data);
}, xo = function e(n, r) {
	n instanceof HTMLSlotElement ? n.assignedNodes({ flatten: !0 }).forEach((n) => {
		switch (n.nodeType) {
			case Node.TEXT_NODE:
				t(this, go, "m", bo).call(this, n) && r.push(n);
				break;
			case Node.ELEMENT_NODE: t(this, go, "m", e).call(this, n, r);
		}
	}) : n.childNodes.forEach((n) => {
		switch (n.nodeType) {
			case Node.TEXT_NODE:
				t(this, go, "m", bo).call(this, n) && r.push(n);
				break;
			case Node.ELEMENT_NODE: t(this, go, "m", e).call(this, n, r);
		}
	});
}, So = function() {
	if (!this.isSupported || !this.isConnected || (CSS.highlights.delete(t(this, _o, "f")), t(this, vo, "f").length = 0, this.disabled)) return;
	if (!this.term) {
		this.dispatchEvent(new CustomEvent("highlight", {
			detail: [...t(this, vo, "f")],
			bubbles: !1,
			composed: !1
		}));
		return;
	}
	let e = [];
	if (t(this, go, "m", xo).call(this, this, e), e.length > 0) {
		let r = this.caseSensitive ? this.term : this.term.toLowerCase();
		switch (this.mode) {
			case "starts-with":
				{
					let n = e[0];
					if (((this.caseSensitive ? n.textContent : n.textContent?.toLowerCase()) ?? "").startsWith(r)) {
						let e = new Range();
						e.setStart(n, 0), e.setEnd(n, r.length), t(this, vo, "f").push(e);
					}
				}
				break;
			case "ends-with":
				{
					let n = e[e.length - 1], i = (this.caseSensitive ? n.textContent : n.textContent?.toLowerCase()) ?? "";
					if (i.endsWith(r)) {
						let e = i.length - r.length, a = e + r.length, o = new Range();
						o.setStart(n, e), o.setEnd(n, a), t(this, vo, "f").push(o);
					}
				}
				break;
			case "contains": n(this, vo, e.map((e) => ({
				el: e,
				text: (this.caseSensitive ? e.textContent : e.textContent?.toLowerCase()) ?? ""
			})).map(({ el: e, text: t }) => {
				let n = [], i = 0;
				for (; i < t.length;) {
					let e = t.indexOf(r, i);
					if (e === -1) break;
					n.push(e), i = e + r.length;
				}
				return n.map((t) => {
					let n = new Range();
					return n.setStart(e, t), n.setEnd(e, t + r.length), n;
				});
			}).flat(), "f");
		}
		t(this, vo, "f").length > 0 && CSS.highlights.set(t(this, _o, "f"), new Highlight(...t(this, vo, "f")));
	}
	this.dispatchEvent(new CustomEvent("highlight", {
		detail: [...t(this, vo, "f")],
		bubbles: !1,
		composed: !1
	}));
}, wo.styles = l`:host { display: contents; } :host([hidden]) { display: none; }`, wo.__nextId = 0, e([S({
	type: Boolean,
	reflect: !0
})], wo.prototype, "disabled", void 0), e([S()], wo.prototype, "term", void 0), e([S({
	attribute: "case-sensitive",
	type: Boolean
})], wo.prototype, "caseSensitive", void 0), e([S()], wo.prototype, "mode", void 0), wo = Co = e([w("m3e-text-highlight")], wo);
var To = class extends x {
	render() {
		return y`<span class="base"><slot></slot></span>`;
	}
};
To.styles = l`:host { flex: 1 1 auto; display: inline-flex; align-items: center; flex-wrap: nowrap; min-width: 0; } :host([hidden]) { display: none; } .base { flex: 1 1 auto; display: inline; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }`, To = e([w("m3e-text-overflow")], To);
//#endregion
//#region node_modules/@m3e/web/dist/theme.js
function Eo(e) {
	return e === "light" || e === "dark" || e === "auto";
}
function Do(e) {
	return e === "high" || e === "medium" || e === "standard";
}
function Oo(e) {
	return e < 0 ? -1 : e === 0 ? 0 : 1;
}
function ko(e, t, n) {
	return (1 - n) * e + n * t;
}
function Ao(e, t, n) {
	return n < e ? e : n > t ? t : n;
}
function jo(e, t, n) {
	return n < e ? e : n > t ? t : n;
}
function Mo(e) {
	return e %= 360, e < 0 && (e += 360), e;
}
function No(e) {
	return e %= 360, e < 0 && (e += 360), e;
}
function Po(e, t) {
	return No(t - e) <= 180 ? 1 : -1;
}
function Fo(e, t) {
	return 180 - Math.abs(Math.abs(e - t) - 180);
}
function Io(e, t) {
	return [
		e[0] * t[0][0] + e[1] * t[0][1] + e[2] * t[0][2],
		e[0] * t[1][0] + e[1] * t[1][1] + e[2] * t[1][2],
		e[0] * t[2][0] + e[1] * t[2][1] + e[2] * t[2][2]
	];
}
var Lo = [
	[
		.41233895,
		.35762064,
		.18051042
	],
	[
		.2126,
		.7152,
		.0722
	],
	[
		.01932141,
		.11916382,
		.95034478
	]
], Ro = [
	[
		3.2413774792388685,
		-1.5376652402851851,
		-.49885366846268053
	],
	[
		-.9691452513005321,
		1.8758853451067872,
		.04156585616912061
	],
	[
		.05562093689691305,
		-.20395524564742123,
		1.0571799111220335
	]
], zo = [
	95.047,
	100,
	108.883
];
function Bo(e, t, n) {
	return (255 << 24 | (e & 255) << 16 | (t & 255) << 8 | n & 255) >>> 0;
}
function Vo(e) {
	return Bo($o(e[0]), $o(e[1]), $o(e[2]));
}
function Ho(e) {
	return e >> 16 & 255;
}
function Uo(e) {
	return e >> 8 & 255;
}
function Wo(e) {
	return e & 255;
}
function Go(e, t, n) {
	let r = Ro, i = r[0][0] * e + r[0][1] * t + r[0][2] * n, a = r[1][0] * e + r[1][1] * t + r[1][2] * n, o = r[2][0] * e + r[2][1] * t + r[2][2] * n;
	return Bo($o(i), $o(a), $o(o));
}
function Ko(e) {
	return Io([
		Qo(Ho(e)),
		Qo(Uo(e)),
		Qo(Wo(e))
	], Lo);
}
function qo(e) {
	let t = Qo(Ho(e)), n = Qo(Uo(e)), r = Qo(Wo(e)), i = Lo, a = i[0][0] * t + i[0][1] * n + i[0][2] * r, o = i[1][0] * t + i[1][1] * n + i[1][2] * r, s = i[2][0] * t + i[2][1] * n + i[2][2] * r, c = zo, l = a / c[0], u = o / c[1], d = s / c[2], f = ts(l), p = ts(u), m = ts(d);
	return [
		116 * p - 16,
		500 * (f - p),
		200 * (p - m)
	];
}
function Jo(e) {
	let t = $o(Xo(e));
	return Bo(t, t, t);
}
function Yo(e) {
	let t = Ko(e)[1];
	return 116 * ts(t / 100) - 16;
}
function Xo(e) {
	return 100 * ns((e + 16) / 116);
}
function Zo(e) {
	return ts(e / 100) * 116 - 16;
}
function Qo(e) {
	let t = e / 255;
	return t <= .040449936 ? t / 12.92 * 100 : ((t + .055) / 1.055) ** 2.4 * 100;
}
function $o(e) {
	let t = e / 100, n = 0;
	return n = t <= .0031308 ? t * 12.92 : 1.055 * t ** (1 / 2.4) - .055, Ao(0, 255, Math.round(n * 255));
}
function es() {
	return zo;
}
function ts(e) {
	return e > .008856451679035631 ? e ** (1 / 3) : (903.2962962962963 * e + 16) / 116;
}
function ns(e) {
	let t = e * e * e;
	return t > .008856451679035631 ? t : (116 * e - 16) / 903.2962962962963;
}
var rs = class e {
	static make(t = es(), n = 200 / Math.PI * Xo(50) / 100, r = 50, i = 2, a = !1) {
		let o = t, s = o[0] * .401288 + o[1] * .650173 + o[2] * -.051461, c = o[0] * -.250268 + o[1] * 1.204414 + o[2] * .045854, l = o[0] * -.002079 + o[1] * .048952 + o[2] * .953127, u = .8 + i / 10, d = u >= .9 ? ko(.59, .69, (u - .9) * 10) : ko(.525, .59, (u - .8) * 10), f = a ? 1 : u * (1 - 1 / 3.6 * Math.exp((-n - 42) / 92));
		f = f > 1 ? 1 : f < 0 ? 0 : f;
		let p = u, m = [
			100 / s * f + 1 - f,
			100 / c * f + 1 - f,
			100 / l * f + 1 - f
		], h = 1 / (5 * n + 1), ee = h * h * h * h, te = 1 - ee, ne = ee * n + .1 * te * te * Math.cbrt(5 * n), g = Xo(r) / t[1], re = 1.48 + Math.sqrt(g), _ = .725 / g ** .2, ie = _, v = [
			(ne * m[0] * s / 100) ** .42,
			(ne * m[1] * c / 100) ** .42,
			(ne * m[2] * l / 100) ** .42
		], ae = [
			400 * v[0] / (v[0] + 27.13),
			400 * v[1] / (v[1] + 27.13),
			400 * v[2] / (v[2] + 27.13)
		], oe = (2 * ae[0] + ae[1] + .05 * ae[2]) * _;
		return new e(g, oe, _, ie, d, p, m, ne, ne ** .25, re);
	}
	constructor(e, t, n, r, i, a, o, s, c, l) {
		this.n = e, this.aw = t, this.nbb = n, this.ncb = r, this.c = i, this.nc = a, this.rgbD = o, this.fl = s, this.fLRoot = c, this.z = l;
	}
};
rs.DEFAULT = rs.make();
var is = class e {
	constructor(e, t, n, r, i, a, o, s, c) {
		this.hue = e, this.chroma = t, this.j = n, this.q = r, this.m = i, this.s = a, this.jstar = o, this.astar = s, this.bstar = c;
	}
	distance(e) {
		let t = this.jstar - e.jstar, n = this.astar - e.astar, r = this.bstar - e.bstar;
		return 1.41 * Math.sqrt(t * t + n * n + r * r) ** .63;
	}
	static fromInt(t) {
		return e.fromIntInViewingConditions(t, rs.DEFAULT);
	}
	static fromIntInViewingConditions(t, n) {
		let r = (t & 16711680) >> 16, i = (t & 65280) >> 8, a = t & 255, o = Qo(r), s = Qo(i), c = Qo(a), l = .41233895 * o + .35762064 * s + .18051042 * c, u = .2126 * o + .7152 * s + .0722 * c, d = .01932141 * o + .11916382 * s + .95034478 * c, f = .401288 * l + .650173 * u - .051461 * d, p = -.250268 * l + 1.204414 * u + .045854 * d, m = -.002079 * l + .048952 * u + .953127 * d, h = n.rgbD[0] * f, ee = n.rgbD[1] * p, te = n.rgbD[2] * m, ne = (n.fl * Math.abs(h) / 100) ** .42, g = (n.fl * Math.abs(ee) / 100) ** .42, re = (n.fl * Math.abs(te) / 100) ** .42, _ = Oo(h) * 400 * ne / (ne + 27.13), ie = Oo(ee) * 400 * g / (g + 27.13), v = Oo(te) * 400 * re / (re + 27.13), ae = (11 * _ + -12 * ie + v) / 11, oe = (_ + ie - 2 * v) / 9, se = (20 * _ + 20 * ie + 21 * v) / 20, ce = (40 * _ + 20 * ie + v) / 20, le = No(Math.atan2(oe, ae) * 180 / Math.PI), ue = le * Math.PI / 180, de = 100 * (ce * n.nbb / n.aw) ** +(n.c * n.z), fe = 4 / n.c * Math.sqrt(de / 100) * (n.aw + 4) * n.fLRoot, pe = le < 20.14 ? le + 360 : le, me = (5e4 / 13 * (.25 * (Math.cos(pe * Math.PI / 180 + 2) + 3.8)) * n.nc * n.ncb * Math.sqrt(ae * ae + oe * oe) / (se + .305)) ** .9 * (1.64 - .29 ** n.n) ** .73, he = me * Math.sqrt(de / 100), ge = he * n.fLRoot, _e = 50 * Math.sqrt(me * n.c / (n.aw + 4)), ve = (1 + 100 * .007) * de / (1 + .007 * de), ye = 1 / .0228 * Math.log(1 + .0228 * ge), be = ye * Math.cos(ue), xe = ye * Math.sin(ue);
		return new e(le, he, de, fe, ge, _e, ve, be, xe);
	}
	static fromJch(t, n, r) {
		return e.fromJchInViewingConditions(t, n, r, rs.DEFAULT);
	}
	static fromJchInViewingConditions(t, n, r, i) {
		let a = 4 / i.c * Math.sqrt(t / 100) * (i.aw + 4) * i.fLRoot, o = n * i.fLRoot, s = n / Math.sqrt(t / 100), c = 50 * Math.sqrt(s * i.c / (i.aw + 4)), l = r * Math.PI / 180, u = (1 + 100 * .007) * t / (1 + .007 * t), d = 1 / .0228 * Math.log(1 + .0228 * o), f = d * Math.cos(l), p = d * Math.sin(l);
		return new e(r, n, t, a, o, c, u, f, p);
	}
	static fromUcs(t, n, r) {
		return e.fromUcsInViewingConditions(t, n, r, rs.DEFAULT);
	}
	static fromUcsInViewingConditions(t, n, r, i) {
		let a = n, o = r, s = Math.sqrt(a * a + o * o), c = (Math.exp(s * .0228) - 1) / .0228 / i.fLRoot, l = 180 / Math.PI * Math.atan2(o, a);
		l < 0 && (l += 360);
		let u = t / (1 - (t - 100) * .007);
		return e.fromJchInViewingConditions(u, c, l, i);
	}
	toInt() {
		return this.viewed(rs.DEFAULT);
	}
	viewed(e) {
		let t = ((this.chroma === 0 || this.j === 0 ? 0 : this.chroma / Math.sqrt(this.j / 100)) / (1.64 - .29 ** e.n) ** .73) ** (1 / .9), n = this.hue * Math.PI / 180, r = .25 * (Math.cos(n + 2) + 3.8), i = e.aw * (this.j / 100) ** (1 / e.c / e.z), a = 5e4 / 13 * r * e.nc * e.ncb, o = i / e.nbb, s = Math.sin(n), c = Math.cos(n), l = 23 * (o + .305) * t / (23 * a + 11 * t * c + 108 * t * s), u = l * c, d = l * s, f = (460 * o + 451 * u + 288 * d) / 1403, p = (460 * o - 891 * u - 261 * d) / 1403, m = (460 * o - 220 * u - 6300 * d) / 1403, h = Math.max(0, 27.13 * Math.abs(f) / (400 - Math.abs(f))), ee = Oo(f) * (100 / e.fl) * h ** (1 / .42), te = Math.max(0, 27.13 * Math.abs(p) / (400 - Math.abs(p))), ne = Oo(p) * (100 / e.fl) * te ** (1 / .42), g = Math.max(0, 27.13 * Math.abs(m) / (400 - Math.abs(m))), re = Oo(m) * (100 / e.fl) * g ** (1 / .42), _ = ee / e.rgbD[0], ie = ne / e.rgbD[1], v = re / e.rgbD[2];
		return Go(1.86206786 * _ - 1.01125463 * ie + .14918677 * v, .38752654 * _ + .62144744 * ie - .00897398 * v, -.0158415 * _ - .03412294 * ie + 1.04996444 * v);
	}
	static fromXyzInViewingConditions(t, n, r, i) {
		let a = .401288 * t + .650173 * n - .051461 * r, o = -.250268 * t + 1.204414 * n + .045854 * r, s = -.002079 * t + .048952 * n + .953127 * r, c = i.rgbD[0] * a, l = i.rgbD[1] * o, u = i.rgbD[2] * s, d = (i.fl * Math.abs(c) / 100) ** .42, f = (i.fl * Math.abs(l) / 100) ** .42, p = (i.fl * Math.abs(u) / 100) ** .42, m = Oo(c) * 400 * d / (d + 27.13), h = Oo(l) * 400 * f / (f + 27.13), ee = Oo(u) * 400 * p / (p + 27.13), te = (11 * m + -12 * h + ee) / 11, ne = (m + h - 2 * ee) / 9, g = (20 * m + 20 * h + 21 * ee) / 20, re = (40 * m + 20 * h + ee) / 20, _ = Math.atan2(ne, te) * 180 / Math.PI, ie = _ < 0 ? _ + 360 : _ >= 360 ? _ - 360 : _, v = ie * Math.PI / 180, ae = 100 * (re * i.nbb / i.aw) ** +(i.c * i.z), oe = 4 / i.c * Math.sqrt(ae / 100) * (i.aw + 4) * i.fLRoot, se = ie < 20.14 ? ie + 360 : ie, ce = (5e4 / 13 * (1 / 4 * (Math.cos(se * Math.PI / 180 + 2) + 3.8)) * i.nc * i.ncb * Math.sqrt(te * te + ne * ne) / (g + .305)) ** .9 * (1.64 - .29 ** i.n) ** .73, le = ce * Math.sqrt(ae / 100), ue = le * i.fLRoot, de = 50 * Math.sqrt(ce * i.c / (i.aw + 4)), fe = (1 + 100 * .007) * ae / (1 + .007 * ae), pe = Math.log(1 + .0228 * ue) / .0228, me = pe * Math.cos(v), he = pe * Math.sin(v);
		return new e(ie, le, ae, oe, ue, de, fe, me, he);
	}
	xyzInViewingConditions(e) {
		let t = ((this.chroma === 0 || this.j === 0 ? 0 : this.chroma / Math.sqrt(this.j / 100)) / (1.64 - .29 ** e.n) ** .73) ** (1 / .9), n = this.hue * Math.PI / 180, r = .25 * (Math.cos(n + 2) + 3.8), i = e.aw * (this.j / 100) ** (1 / e.c / e.z), a = 5e4 / 13 * r * e.nc * e.ncb, o = i / e.nbb, s = Math.sin(n), c = Math.cos(n), l = 23 * (o + .305) * t / (23 * a + 11 * t * c + 108 * t * s), u = l * c, d = l * s, f = (460 * o + 451 * u + 288 * d) / 1403, p = (460 * o - 891 * u - 261 * d) / 1403, m = (460 * o - 220 * u - 6300 * d) / 1403, h = Math.max(0, 27.13 * Math.abs(f) / (400 - Math.abs(f))), ee = Oo(f) * (100 / e.fl) * h ** (1 / .42), te = Math.max(0, 27.13 * Math.abs(p) / (400 - Math.abs(p))), ne = Oo(p) * (100 / e.fl) * te ** (1 / .42), g = Math.max(0, 27.13 * Math.abs(m) / (400 - Math.abs(m))), re = Oo(m) * (100 / e.fl) * g ** (1 / .42), _ = ee / e.rgbD[0], ie = ne / e.rgbD[1], v = re / e.rgbD[2];
		return [
			1.86206786 * _ - 1.01125463 * ie + .14918677 * v,
			.38752654 * _ + .62144744 * ie - .00897398 * v,
			-.0158415 * _ - .03412294 * ie + 1.04996444 * v
		];
	}
}, as = class e {
	static sanitizeRadians(e) {
		return (e + Math.PI * 8) % (Math.PI * 2);
	}
	static trueDelinearized(e) {
		let t = e / 100, n = 0;
		return n = t <= .0031308 ? t * 12.92 : 1.055 * t ** (1 / 2.4) - .055, n * 255;
	}
	static chromaticAdaptation(e) {
		let t = Math.abs(e) ** .42;
		return Oo(e) * 400 * t / (t + 27.13);
	}
	static hueOf(t) {
		let n = Io(t, e.SCALED_DISCOUNT_FROM_LINRGB), r = e.chromaticAdaptation(n[0]), i = e.chromaticAdaptation(n[1]), a = e.chromaticAdaptation(n[2]), o = (11 * r + -12 * i + a) / 11, s = (r + i - 2 * a) / 9;
		return Math.atan2(s, o);
	}
	static areInCyclicOrder(t, n, r) {
		return e.sanitizeRadians(n - t) < e.sanitizeRadians(r - t);
	}
	static intercept(e, t, n) {
		return (t - e) / (n - e);
	}
	static lerpPoint(e, t, n) {
		return [
			e[0] + (n[0] - e[0]) * t,
			e[1] + (n[1] - e[1]) * t,
			e[2] + (n[2] - e[2]) * t
		];
	}
	static setCoordinate(t, n, r, i) {
		let a = e.intercept(t[i], n, r[i]);
		return e.lerpPoint(t, a, r);
	}
	static isBounded(e) {
		return 0 <= e && e <= 100;
	}
	static nthVertex(t, n) {
		let r = e.Y_FROM_LINRGB[0], i = e.Y_FROM_LINRGB[1], a = e.Y_FROM_LINRGB[2], o = n % 4 <= 1 ? 0 : 100, s = n % 2 == 0 ? 0 : 100;
		if (n < 4) {
			let n = o, c = s, l = (t - n * i - c * a) / r;
			return e.isBounded(l) ? [
				l,
				n,
				c
			] : [
				-1,
				-1,
				-1
			];
		}
		if (n < 8) {
			let n = o, c = s, l = (t - c * r - n * a) / i;
			return e.isBounded(l) ? [
				c,
				l,
				n
			] : [
				-1,
				-1,
				-1
			];
		}
		{
			let n = o, c = s, l = (t - n * r - c * i) / a;
			return e.isBounded(l) ? [
				n,
				c,
				l
			] : [
				-1,
				-1,
				-1
			];
		}
	}
	static bisectToSegment(t, n) {
		let r = [
			-1,
			-1,
			-1
		], i = r, a = 0, o = 0, s = !1, c = !0;
		for (let l = 0; l < 12; l++) {
			let u = e.nthVertex(t, l);
			if (u[0] < 0) continue;
			let d = e.hueOf(u);
			s ? (c || e.areInCyclicOrder(a, d, o)) && (c = !1, e.areInCyclicOrder(a, n, d) ? (i = u, o = d) : (r = u, a = d)) : (r = u, i = u, a = d, o = d, s = !0);
		}
		return [r, i];
	}
	static midpoint(e, t) {
		return [
			(e[0] + t[0]) / 2,
			(e[1] + t[1]) / 2,
			(e[2] + t[2]) / 2
		];
	}
	static criticalPlaneBelow(e) {
		return Math.floor(e - .5);
	}
	static criticalPlaneAbove(e) {
		return Math.ceil(e - .5);
	}
	static bisectToLimit(t, n) {
		let r = e.bisectToSegment(t, n), i = r[0], a = e.hueOf(i), o = r[1];
		for (let t = 0; t < 3; t++) if (i[t] !== o[t]) {
			let r = -1, s = 255;
			i[t] < o[t] ? (r = e.criticalPlaneBelow(e.trueDelinearized(i[t])), s = e.criticalPlaneAbove(e.trueDelinearized(o[t]))) : (r = e.criticalPlaneAbove(e.trueDelinearized(i[t])), s = e.criticalPlaneBelow(e.trueDelinearized(o[t])));
			for (let c = 0; c < 8 && !(Math.abs(s - r) <= 1); c++) {
				let c = Math.floor((r + s) / 2), l = e.CRITICAL_PLANES[c], u = e.setCoordinate(i, l, o, t), d = e.hueOf(u);
				e.areInCyclicOrder(a, n, d) ? (o = u, s = c) : (i = u, a = d, r = c);
			}
		}
		return e.midpoint(i, o);
	}
	static inverseChromaticAdaptation(e) {
		let t = Math.abs(e), n = Math.max(0, 27.13 * t / (400 - t));
		return Oo(e) * n ** (1 / .42);
	}
	static findResultByJ(t, n, r) {
		let i = Math.sqrt(r) * 11, a = rs.DEFAULT, o = 1 / (1.64 - .29 ** a.n) ** .73, s = .25 * (Math.cos(t + 2) + 3.8) * (5e4 / 13) * a.nc * a.ncb, c = Math.sin(t), l = Math.cos(t);
		for (let t = 0; t < 5; t++) {
			let u = i / 100, d = ((n === 0 || i === 0 ? 0 : n / Math.sqrt(u)) * o) ** (1 / .9), f = a.aw * u ** (1 / a.c / a.z) / a.nbb, p = 23 * (f + .305) * d / (23 * s + 11 * d * l + 108 * d * c), m = p * l, h = p * c, ee = (460 * f + 451 * m + 288 * h) / 1403, te = (460 * f - 891 * m - 261 * h) / 1403, ne = (460 * f - 220 * m - 6300 * h) / 1403, g = Io([
				e.inverseChromaticAdaptation(ee),
				e.inverseChromaticAdaptation(te),
				e.inverseChromaticAdaptation(ne)
			], e.LINRGB_FROM_SCALED_DISCOUNT);
			if (g[0] < 0 || g[1] < 0 || g[2] < 0) return 0;
			let re = e.Y_FROM_LINRGB[0], _ = e.Y_FROM_LINRGB[1], ie = e.Y_FROM_LINRGB[2], v = re * g[0] + _ * g[1] + ie * g[2];
			if (v <= 0) return 0;
			if (t === 4 || Math.abs(v - r) < .002) return g[0] > 100.01 || g[1] > 100.01 || g[2] > 100.01 ? 0 : Vo(g);
			i -= (v - r) * i / (2 * v);
		}
		return 0;
	}
	static solveToInt(t, n, r) {
		if (n < 1e-4 || r < 1e-4 || r > 99.9999) return Jo(r);
		t = No(t);
		let i = t / 180 * Math.PI, a = Xo(r), o = e.findResultByJ(i, n, a);
		return o === 0 ? Vo(e.bisectToLimit(a, i)) : o;
	}
	static solveToCam(t, n, r) {
		return is.fromInt(e.solveToInt(t, n, r));
	}
};
as.SCALED_DISCOUNT_FROM_LINRGB = [
	[
		.001200833568784504,
		.002389694492170889,
		.0002795742885861124
	],
	[
		.0005891086651375999,
		.0029785502573438758,
		.0003270666104008398
	],
	[
		.00010146692491640572,
		.0005364214359186694,
		.0032979401770712076
	]
], as.LINRGB_FROM_SCALED_DISCOUNT = [
	[
		1373.2198709594231,
		-1100.4251190754821,
		-7.278681089101213
	],
	[
		-271.815969077903,
		559.6580465940733,
		-32.46047482791194
	],
	[
		1.9622899599665666,
		-57.173814538844006,
		308.7233197812385
	]
], as.Y_FROM_LINRGB = [
	.2126,
	.7152,
	.0722
], as.CRITICAL_PLANES = [
	.015176349177441876,
	.045529047532325624,
	.07588174588720938,
	.10623444424209313,
	.13658714259697685,
	.16693984095186062,
	.19729253930674434,
	.2276452376616281,
	.2579979360165119,
	.28835063437139563,
	.3188300904430532,
	.350925934958123,
	.3848314933096426,
	.42057480301049466,
	.458183274052838,
	.4976837250274023,
	.5391024159806381,
	.5824650784040898,
	.6277969426914107,
	.6751227633498623,
	.7244668422128921,
	.775853049866786,
	.829304845476233,
	.8848452951698498,
	.942497089126609,
	1.0022825574869039,
	1.0642236851973577,
	1.1283421258858297,
	1.1946592148522128,
	1.2631959812511864,
	1.3339731595349034,
	1.407011200216447,
	1.4823302800086415,
	1.5599503113873272,
	1.6398909516233677,
	1.7221716113234105,
	1.8068114625156377,
	1.8938294463134073,
	1.9832442801866852,
	2.075074464868551,
	2.1693382909216234,
	2.2660538449872063,
	2.36523901573795,
	2.4669114995532007,
	2.5710888059345764,
	2.6777882626779785,
	2.7870270208169257,
	2.898822059350997,
	3.0131901897720907,
	3.1301480604002863,
	3.2497121605402226,
	3.3718988244681087,
	3.4967242352587946,
	3.624204428461639,
	3.754355295633311,
	3.887192587735158,
	4.022731918402185,
	4.160988767090289,
	4.301978482107941,
	4.445716283538092,
	4.592217266055746,
	4.741496401646282,
	4.893568542229298,
	5.048448422192488,
	5.20615066083972,
	5.3666897647573375,
	5.5300801301023865,
	5.696336044816294,
	5.865471690767354,
	6.037501145825082,
	6.212438385869475,
	6.390297286737924,
	6.571091626112461,
	6.7548350853498045,
	6.941541251256611,
	7.131223617812143,
	7.323895587840543,
	7.5195704746346665,
	7.7182615035334345,
	7.919981813454504,
	8.124744458384042,
	8.332562408825165,
	8.543448553206703,
	8.757415699253682,
	8.974476575321063,
	9.194643831691977,
	9.417930041841839,
	9.644347703669503,
	9.873909240696694,
	10.106627003236781,
	10.342513269534024,
	10.58158024687427,
	10.8238400726681,
	11.069304815507364,
	11.317986476196008,
	11.569896988756009,
	11.825048221409341,
	12.083451977536606,
	12.345119996613247,
	12.610063955123938,
	12.878295467455942,
	13.149826086772048,
	13.42466730586372,
	13.702830557985108,
	13.984327217668513,
	14.269168601521828,
	14.55736596900856,
	14.848930523210871,
	15.143873411576273,
	15.44220572664832,
	15.743938506781891,
	16.04908273684337,
	16.35764934889634,
	16.66964922287304,
	16.985093187232053,
	17.30399201960269,
	17.62635644741625,
	17.95219714852476,
	18.281524751807332,
	18.614349837764564,
	18.95068293910138,
	19.290534541298456,
	19.633915083172692,
	19.98083495742689,
	20.331304511189067,
	20.685334046541502,
	21.042933821039977,
	21.404114048223256,
	21.76888489811322,
	22.137256497705877,
	22.50923893145328,
	22.884842241736916,
	23.264076429332462,
	23.6469514538663,
	24.033477234264016,
	24.42366364919083,
	24.817520537484558,
	25.21505769858089,
	25.61628489293138,
	26.021211842414342,
	26.429848230738664,
	26.842203703840827,
	27.258287870275353,
	27.678110301598522,
	28.10168053274597,
	28.529008062403893,
	28.96010235337422,
	29.39497283293396,
	29.83362889318845,
	30.276079891419332,
	30.722335150426627,
	31.172403958865512,
	31.62629557157785,
	32.08401920991837,
	32.54558406207592,
	33.010999283389665,
	33.4802739966603,
	33.953417292456834,
	34.430438229418264,
	34.911345834551085,
	35.39614910352207,
	35.88485700094671,
	36.37747846067349,
	36.87402238606382,
	37.37449765026789,
	37.87891309649659,
	38.38727753828926,
	38.89959975977785,
	39.41588851594697,
	39.93615253289054,
	40.460400508064545,
	40.98864111053629,
	41.520882981230194,
	42.05713473317016,
	42.597404951718396,
	43.141702194811224,
	43.6900349931913,
	44.24241185063697,
	44.798841244188324,
	45.35933162437017,
	45.92389141541209,
	46.49252901546552,
	47.065252796817916,
	47.64207110610409,
	48.22299226451468,
	48.808024568002054,
	49.3971762874833,
	49.9904556690408,
	50.587870934119984,
	51.189430279724725,
	51.79514187861014,
	52.40501387947288,
	53.0190544071392,
	53.637271562750364,
	54.259673423945976,
	54.88626804504493,
	55.517063457223934,
	56.15206766869424,
	56.79128866487574,
	57.43473440856916,
	58.08241284012621,
	58.734331877617365,
	59.39049941699807,
	60.05092333227251,
	60.715611475655585,
	61.38457167773311,
	62.057811747619894,
	62.7353394731159,
	63.417162620860914,
	64.10328893648692,
	64.79372614476921,
	65.48848194977529,
	66.18756403501224,
	66.89098006357258,
	67.59873767827808,
	68.31084450182222,
	69.02730813691093,
	69.74813616640164,
	70.47333615344107,
	71.20291564160104,
	71.93688215501312,
	72.67524319850172,
	73.41800625771542,
	74.16517879925733,
	74.9167682708136,
	75.67278210128072,
	76.43322770089146,
	77.1981124613393,
	77.96744375590167,
	78.74122893956174,
	79.51947534912904,
	80.30219030335869,
	81.08938110306934,
	81.88105503125999,
	82.67721935322541,
	83.4778813166706,
	84.28304815182372,
	85.09272707154808,
	85.90692527145302,
	86.72564993000343,
	87.54890820862819,
	88.3767072518277,
	89.2090541872801,
	90.04595612594655,
	90.88742016217518,
	91.73345337380438,
	92.58406282226491,
	93.43925555268066,
	94.29903859396902,
	95.16341895893969,
	96.03240364439274,
	96.9059996312159,
	97.78421388448044,
	98.6670533535366,
	99.55452497210776
];
var M = class e {
	static from(t, n, r) {
		return new e(as.solveToInt(t, n, r));
	}
	static fromInt(t) {
		return new e(t);
	}
	toInt() {
		return this.argb;
	}
	get hue() {
		return this.internalHue;
	}
	set hue(e) {
		this.setInternalState(as.solveToInt(e, this.internalChroma, this.internalTone));
	}
	get chroma() {
		return this.internalChroma;
	}
	set chroma(e) {
		this.setInternalState(as.solveToInt(this.internalHue, e, this.internalTone));
	}
	get tone() {
		return this.internalTone;
	}
	set tone(e) {
		this.setInternalState(as.solveToInt(this.internalHue, this.internalChroma, e));
	}
	setValue(e, t) {
		this[e] = t;
	}
	toString() {
		return `HCT(${this.hue.toFixed(0)}, ${this.chroma.toFixed(0)}, ${this.tone.toFixed(0)})`;
	}
	static isBlue(e) {
		return e >= 250 && e < 270;
	}
	static isYellow(e) {
		return e >= 105 && e < 125;
	}
	static isCyan(e) {
		return e >= 170 && e < 207;
	}
	constructor(e) {
		this.argb = e;
		let t = is.fromInt(e);
		this.internalHue = t.hue, this.internalChroma = t.chroma, this.internalTone = Yo(e), this.argb = e;
	}
	setInternalState(e) {
		let t = is.fromInt(e);
		this.internalHue = t.hue, this.internalChroma = t.chroma, this.internalTone = Yo(e), this.argb = e;
	}
	inViewingConditions(t) {
		let n = is.fromInt(this.toInt()).xyzInViewingConditions(t), r = is.fromXyzInViewingConditions(n[0], n[1], n[2], rs.make());
		return e.from(r.hue, r.chroma, Zo(n[1]));
	}
}, os = class e {
	static harmonize(e, t) {
		let n = M.fromInt(e), r = M.fromInt(t), i = Fo(n.hue, r.hue), a = Math.min(i * .5, 15), o = No(n.hue + a * Po(n.hue, r.hue));
		return M.from(o, n.chroma, n.tone).toInt();
	}
	static hctHue(t, n, r) {
		let i = e.cam16Ucs(t, n, r), a = is.fromInt(i), o = is.fromInt(t);
		return M.from(a.hue, o.chroma, Yo(t)).toInt();
	}
	static cam16Ucs(e, t, n) {
		let r = is.fromInt(e), i = is.fromInt(t), a = r.jstar, o = r.astar, s = r.bstar, c = i.jstar, l = i.astar, u = i.bstar, d = a + (c - a) * n, f = o + (l - o) * n, p = s + (u - s) * n;
		return is.fromUcs(d, f, p).toInt();
	}
}, ss = class e {
	static ratioOfTones(t, n) {
		return t = jo(0, 100, t), n = jo(0, 100, n), e.ratioOfYs(Xo(t), Xo(n));
	}
	static ratioOfYs(e, t) {
		let n = e > t ? e : t, r = n === t ? e : t;
		return (n + 5) / (r + 5);
	}
	static lighter(t, n) {
		if (t < 0 || t > 100) return -1;
		let r = Xo(t), i = n * (r + 5) - 5, a = e.ratioOfYs(i, r), o = Math.abs(a - n);
		if (a < n && o > .04) return -1;
		let s = Zo(i) + .4;
		return s < 0 || s > 100 ? -1 : s;
	}
	static darker(t, n) {
		if (t < 0 || t > 100) return -1;
		let r = Xo(t), i = (r + 5) / n - 5, a = e.ratioOfYs(r, i), o = Math.abs(a - n);
		if (a < n && o > .04) return -1;
		let s = Zo(i) - .4;
		return s < 0 || s > 100 ? -1 : s;
	}
	static lighterUnsafe(t, n) {
		let r = e.lighter(t, n);
		return r < 0 ? 100 : r;
	}
	static darkerUnsafe(t, n) {
		let r = e.darker(t, n);
		return r < 0 ? 0 : r;
	}
}, cs = class e {
	static isDisliked(e) {
		let t = Math.round(e.hue) >= 90 && Math.round(e.hue) <= 111, n = Math.round(e.chroma) > 16, r = Math.round(e.tone) < 65;
		return t && n && r;
	}
	static fixIfDisliked(t) {
		return e.isDisliked(t) ? M.from(t.hue, t.chroma, 70) : t;
	}
};
function ls(e, t, n) {
	if (e.name !== n.name) throw Error(`Attempting to extend color ${e.name} with color ${n.name} of different name for spec version ${t}.`);
	if (e.isBackground !== n.isBackground) throw Error(`Attempting to extend color ${e.name} as a ${e.isBackground ? "background" : "foreground"} with color ${n.name} as a ${n.isBackground ? "background" : "foreground"} for spec version ${t}.`);
}
function N(e, t, n) {
	return ls(e, t, n), P.fromPalette({
		name: e.name,
		palette: (r) => r.specVersion === t ? n.palette(r) : e.palette(r),
		tone: (r) => r.specVersion === t ? n.tone(r) : e.tone(r),
		isBackground: e.isBackground,
		chromaMultiplier: (r) => {
			let i = r.specVersion === t ? n.chromaMultiplier : e.chromaMultiplier;
			return i === void 0 ? 1 : i(r);
		},
		background: (r) => {
			let i = r.specVersion === t ? n.background : e.background;
			return i === void 0 ? void 0 : i(r);
		},
		secondBackground: (r) => {
			let i = r.specVersion === t ? n.secondBackground : e.secondBackground;
			return i === void 0 ? void 0 : i(r);
		},
		contrastCurve: (r) => {
			let i = r.specVersion === t ? n.contrastCurve : e.contrastCurve;
			return i === void 0 ? void 0 : i(r);
		},
		toneDeltaPair: (r) => {
			let i = r.specVersion === t ? n.toneDeltaPair : e.toneDeltaPair;
			return i === void 0 ? void 0 : i(r);
		}
	});
}
var P = class e {
	static fromPalette(t) {
		return new e(t.name ?? "", t.palette, t.tone ?? e.getInitialToneFromBackground(t.background), t.isBackground ?? !1, t.chromaMultiplier, t.background, t.secondBackground, t.contrastCurve, t.toneDeltaPair);
	}
	static getInitialToneFromBackground(e) {
		return e === void 0 ? (e) => 50 : (t) => e(t) ? e(t).getTone(t) : 50;
	}
	constructor(e, t, n, r, i, a, o, s, c) {
		if (this.name = e, this.palette = t, this.tone = n, this.isBackground = r, this.chromaMultiplier = i, this.background = a, this.secondBackground = o, this.contrastCurve = s, this.toneDeltaPair = c, this.hctCache = /* @__PURE__ */ new Map(), !a && o) throw Error(`Color ${e} has secondBackgrounddefined, but background is not defined.`);
		if (!a && s) throw Error(`Color ${e} has contrastCurvedefined, but background is not defined.`);
		if (a && !s) throw Error(`Color ${e} has backgrounddefined, but contrastCurve is not defined.`);
	}
	clone() {
		return e.fromPalette({
			name: this.name,
			palette: this.palette,
			tone: this.tone,
			isBackground: this.isBackground,
			chromaMultiplier: this.chromaMultiplier,
			background: this.background,
			secondBackground: this.secondBackground,
			contrastCurve: this.contrastCurve,
			toneDeltaPair: this.toneDeltaPair
		});
	}
	clearCache() {
		this.hctCache.clear();
	}
	getArgb(e) {
		return this.getHct(e).toInt();
	}
	getHct(e) {
		let t = this.hctCache.get(e);
		if (t != null) return t;
		let n = ms(e.specVersion).getHct(e, this);
		return this.hctCache.size > 4 && this.hctCache.clear(), this.hctCache.set(e, n), n;
	}
	getTone(e) {
		return ms(e.specVersion).getTone(e, this);
	}
	static foregroundTone(t, n) {
		let r = ss.lighterUnsafe(t, n), i = ss.darkerUnsafe(t, n), a = ss.ratioOfTones(r, t), o = ss.ratioOfTones(i, t);
		if (e.tonePrefersLightForeground(t)) {
			let e = Math.abs(a - o) < .1 && a < n && o < n;
			return a >= n || a >= o || e ? r : i;
		}
		return o >= n || o >= a ? i : r;
	}
	static tonePrefersLightForeground(e) {
		return Math.round(e) < 60;
	}
	static toneAllowsLightForeground(e) {
		return Math.round(e) <= 49;
	}
	static enableLightForeground(t) {
		return e.tonePrefersLightForeground(t) && !e.toneAllowsLightForeground(t) ? 49 : t;
	}
}, us = class {
	getHct(e, t) {
		let n = t.getTone(e);
		return t.palette(e).getHct(n);
	}
	getTone(e, t) {
		let n = e.contrastLevel < 0, r = t.toneDeltaPair ? t.toneDeltaPair(e) : void 0;
		if (r) {
			let i = r.roleA, a = r.roleB, o = r.delta, s = r.polarity, c = r.stayTogether, l = s === "nearer" || s === "lighter" && !e.isDark || s === "darker" && e.isDark, u = l ? i : a, d = l ? a : i, f = t.name === u.name, p = e.isDark ? 1 : -1, m = u.tone(e), h = d.tone(e);
			if (t.background && u.contrastCurve && d.contrastCurve) {
				let r = t.background(e), i = u.contrastCurve(e), a = d.contrastCurve(e);
				if (r && i && a) {
					let t = r.getTone(e), o = i.get(e.contrastLevel), s = a.get(e.contrastLevel);
					ss.ratioOfTones(t, m) < o && (m = P.foregroundTone(t, o)), ss.ratioOfTones(t, h) < s && (h = P.foregroundTone(t, s)), n && (m = P.foregroundTone(t, o), h = P.foregroundTone(t, s));
				}
			}
			return (h - m) * p < o && (h = jo(0, 100, m + o * p), (h - m) * p >= o || (m = jo(0, 100, h - o * p))), 50 <= m && m < 60 ? p > 0 ? (m = 60, h = Math.max(h, m + o * p)) : (m = 49, h = Math.min(h, m + o * p)) : 50 <= h && h < 60 && (c ? p > 0 ? (m = 60, h = Math.max(h, m + o * p)) : (m = 49, h = Math.min(h, m + o * p)) : h = p > 0 ? 60 : 49), f ? m : h;
		}
		{
			let r = t.tone(e);
			if (t.background == null || t.background(e) === void 0 || t.contrastCurve == null || t.contrastCurve(e) === void 0) return r;
			let i = t.background(e).getTone(e), a = t.contrastCurve(e).get(e.contrastLevel);
			if (ss.ratioOfTones(i, r) >= a || (r = P.foregroundTone(i, a)), n && (r = P.foregroundTone(i, a)), t.isBackground && 50 <= r && r < 60 && (r = ss.ratioOfTones(49, i) >= a ? 49 : 60), t.secondBackground == null || t.secondBackground(e) === void 0) return r;
			let [o, s] = [t.background, t.secondBackground], [c, l] = [o(e).getTone(e), s(e).getTone(e)], [u, d] = [Math.max(c, l), Math.min(c, l)];
			if (ss.ratioOfTones(u, r) >= a && ss.ratioOfTones(d, r) >= a) return r;
			let f = ss.lighter(u, a), p = ss.darker(d, a), m = [];
			return f !== -1 && m.push(f), p !== -1 && m.push(p), P.tonePrefersLightForeground(c) || P.tonePrefersLightForeground(l) ? f < 0 ? 100 : f : m.length === 1 ? m[0] : p < 0 ? 0 : p;
		}
	}
}, ds = class {
	getHct(e, t) {
		let n = t.palette(e), r = t.getTone(e), i = n.hue, a = n.chroma * (t.chromaMultiplier ? t.chromaMultiplier(e) : 1);
		return M.from(i, a, r);
	}
	getTone(e, t) {
		let n = t.toneDeltaPair ? t.toneDeltaPair(e) : void 0;
		if (n) {
			let r = n.roleA, i = n.roleB, a = n.polarity, o = n.constraint, s = a === "darker" || a === "relative_lighter" && e.isDark || a === "relative_darker" && !e.isDark ? -n.delta : n.delta, c = t.name === r.name, l = c ? r : i, u = c ? i : r, d = l.tone(e), f = u.getTone(e), p = s * (c ? 1 : -1);
			if (o === "exact" ? d = jo(0, 100, f + p) : o === "nearer" ? d = p > 0 ? jo(0, 100, jo(f, f + p, d)) : jo(0, 100, jo(f + p, f, d)) : o === "farther" && (d = p > 0 ? jo(f + p, 100, d) : jo(0, f + p, d)), t.background && t.contrastCurve) {
				let n = t.background(e), r = t.contrastCurve(e);
				if (n && r) {
					let t = n.getTone(e), i = r.get(e.contrastLevel);
					d = ss.ratioOfTones(t, d) >= i && e.contrastLevel >= 0 ? d : P.foregroundTone(t, i);
				}
			}
			return t.isBackground && !t.name.endsWith("_fixed_dim") && (d = d >= 57 ? jo(65, 100, d) : jo(0, 49, d)), d;
		}
		{
			let n = t.tone(e);
			if (t.background == null || t.background(e) === void 0 || t.contrastCurve == null || t.contrastCurve(e) === void 0) return n;
			let r = t.background(e).getTone(e), i = t.contrastCurve(e).get(e.contrastLevel);
			if (n = ss.ratioOfTones(r, n) >= i && e.contrastLevel >= 0 ? n : P.foregroundTone(r, i), t.isBackground && !t.name.endsWith("_fixed_dim") && (n = n >= 57 ? jo(65, 100, n) : jo(0, 49, n)), t.secondBackground == null || t.secondBackground(e) === void 0) return n;
			let [a, o] = [t.background, t.secondBackground], [s, c] = [a(e).getTone(e), o(e).getTone(e)], [l, u] = [Math.max(s, c), Math.min(s, c)];
			if (ss.ratioOfTones(l, n) >= i && ss.ratioOfTones(u, n) >= i) return n;
			let d = ss.lighter(l, i), f = ss.darker(u, i), p = [];
			return d !== -1 && p.push(d), f !== -1 && p.push(f), P.tonePrefersLightForeground(s) || P.tonePrefersLightForeground(c) ? d < 0 ? 100 : d : p.length === 1 ? p[0] : f < 0 ? 0 : f;
		}
	}
}, fs = new us(), ps = new ds();
function ms(e) {
	return e === "2025" ? ps : fs;
}
var F = class e {
	static fromInt(t) {
		let n = M.fromInt(t);
		return e.fromHct(n);
	}
	static fromHct(t) {
		return new e(t.hue, t.chroma, t);
	}
	static fromHueAndChroma(t, n) {
		let r = new hs(t, n).create();
		return new e(t, n, r);
	}
	constructor(e, t, n) {
		this.hue = e, this.chroma = t, this.keyColor = n, this.cache = /* @__PURE__ */ new Map();
	}
	tone(e) {
		let t = this.cache.get(e);
		return t === void 0 && (t = e == 99 && M.isYellow(this.hue) ? this.averageArgb(this.tone(98), this.tone(100)) : M.from(this.hue, this.chroma, e).toInt(), this.cache.set(e, t)), t;
	}
	getHct(e) {
		return M.fromInt(this.tone(e));
	}
	averageArgb(e, t) {
		let n = e >>> 16 & 255, r = e >>> 8 & 255, i = e & 255, a = t >>> 16 & 255, o = t >>> 8 & 255, s = t & 255, c = Math.round((n + a) / 2), l = Math.round((r + o) / 2), u = Math.round((i + s) / 2);
		return (255 << 24 | (c & 255) << 16 | (l & 255) << 8 | u & 255) >>> 0;
	}
}, hs = class {
	constructor(e, t) {
		this.hue = e, this.requestedChroma = t, this.chromaCache = /* @__PURE__ */ new Map(), this.maxChromaValue = 200;
	}
	create() {
		let e = 0, t = 100;
		for (; e < t;) {
			let n = Math.floor((e + t) / 2), r = this.maxChroma(n) < this.maxChroma(n + 1);
			if (this.maxChroma(n) >= this.requestedChroma - .01) {
				if (Math.abs(e - 50) < Math.abs(t - 50)) t = n;
				else {
					if (e === n) return M.from(this.hue, this.requestedChroma, e);
					e = n;
				}
			} else r ? e = n + 1 : t = n;
		}
		return M.from(this.hue, this.requestedChroma, e);
	}
	maxChroma(e) {
		if (this.chromaCache.has(e)) return this.chromaCache.get(e);
		let t = M.from(this.hue, this.maxChromaValue, e).chroma;
		return this.chromaCache.set(e, t), t;
	}
}, gs = class e {
	constructor(e) {
		this.input = e, this.hctsByTempCache = [], this.hctsByHueCache = [], this.tempsByHctCache = /* @__PURE__ */ new Map(), this.inputRelativeTemperatureCache = -1, this.complementCache = null;
	}
	get hctsByTemp() {
		if (this.hctsByTempCache.length > 0) return this.hctsByTempCache;
		let e = this.hctsByHue.concat([this.input]), t = this.tempsByHct;
		return e.sort((e, n) => t.get(e) - t.get(n)), this.hctsByTempCache = e, e;
	}
	get warmest() {
		return this.hctsByTemp[this.hctsByTemp.length - 1];
	}
	get coldest() {
		return this.hctsByTemp[0];
	}
	analogous(e = 5, t = 12) {
		let n = Math.round(this.input.hue), r = this.hctsByHue[n], i = this.relativeTemperature(r), a = [r], o = 0;
		for (let e = 0; e < 360; e++) {
			let t = Mo(n + e), r = this.hctsByHue[t], a = this.relativeTemperature(r), s = Math.abs(a - i);
			i = a, o += s;
		}
		let s = 1, c = o / t, l = 0;
		for (i = this.relativeTemperature(r); a.length < t;) {
			let e = Mo(n + s), r = this.hctsByHue[e], o = this.relativeTemperature(r), u = Math.abs(o - i);
			l += u;
			let d = a.length * c, f = l >= d, p = 1;
			for (; f && a.length < t;) {
				a.push(r);
				let e = (a.length + p) * c;
				f = l >= e, p++;
			}
			if (i = o, s++, s > 360) {
				for (; a.length < t;) a.push(r);
				break;
			}
		}
		let u = [this.input], d = Math.floor((e - 1) / 2);
		for (let e = 1; e < d + 1; e++) {
			let t = 0 - e;
			for (; t < 0;) t = a.length + t;
			t >= a.length && (t %= a.length), u.splice(0, 0, a[t]);
		}
		let f = e - d - 1;
		for (let e = 1; e < f + 1; e++) {
			let t = e;
			for (; t < 0;) t = a.length + t;
			t >= a.length && (t %= a.length), u.push(a[t]);
		}
		return u;
	}
	get complement() {
		if (this.complementCache != null) return this.complementCache;
		let t = this.coldest.hue, n = this.tempsByHct.get(this.coldest), r = this.warmest.hue, i = this.tempsByHct.get(this.warmest) - n, a = e.isBetween(this.input.hue, t, r), o = a ? r : t, s = a ? t : r, c = 1e3, l = this.hctsByHue[Math.round(this.input.hue)], u = 1 - this.inputRelativeTemperature;
		for (let t = 0; t <= 360; t += 1) {
			let r = No(o + 1 * t);
			if (!e.isBetween(r, o, s)) continue;
			let a = this.hctsByHue[Math.round(r)], d = (this.tempsByHct.get(a) - n) / i, f = Math.abs(u - d);
			f < c && (c = f, l = a);
		}
		return this.complementCache = l, this.complementCache;
	}
	relativeTemperature(e) {
		let t = this.tempsByHct.get(this.warmest) - this.tempsByHct.get(this.coldest), n = this.tempsByHct.get(e) - this.tempsByHct.get(this.coldest);
		return t === 0 ? .5 : n / t;
	}
	get inputRelativeTemperature() {
		return this.inputRelativeTemperatureCache >= 0 || (this.inputRelativeTemperatureCache = this.relativeTemperature(this.input)), this.inputRelativeTemperatureCache;
	}
	get tempsByHct() {
		if (this.tempsByHctCache.size > 0) return this.tempsByHctCache;
		let t = this.hctsByHue.concat([this.input]), n = /* @__PURE__ */ new Map();
		for (let r of t) n.set(r, e.rawTemperature(r));
		return this.tempsByHctCache = n, n;
	}
	get hctsByHue() {
		if (this.hctsByHueCache.length > 0) return this.hctsByHueCache;
		let e = [];
		for (let t = 0; t <= 360; t += 1) {
			let n = M.from(t, this.input.chroma, this.input.tone);
			e.push(n);
		}
		return this.hctsByHueCache = e, this.hctsByHueCache;
	}
	static isBetween(e, t, n) {
		return t < n ? t <= e && e <= n : t <= e || e <= n;
	}
	static rawTemperature(e) {
		let t = qo(e.toInt()), n = No(Math.atan2(t[2], t[1]) * 180 / Math.PI);
		return -.5 + .02 * Math.sqrt(t[1] * t[1] + t[2] * t[2]) ** 1.07 * Math.cos(No(n - 50) * Math.PI / 180);
	}
}, I = class {
	constructor(e, t, n, r) {
		this.low = e, this.normal = t, this.medium = n, this.high = r;
	}
	get(e) {
		return e <= -1 ? this.low : e < 0 ? ko(this.low, this.normal, (e - -1) / 1) : e < .5 ? ko(this.normal, this.medium, (e - 0) / .5) : e < 1 ? ko(this.medium, this.high, (e - .5) / .5) : this.high;
	}
}, _s = class {
	constructor(e, t, n, r, i, a) {
		this.roleA = e, this.roleB = t, this.delta = n, this.polarity = r, this.stayTogether = i, this.constraint = a, this.constraint = a ?? "exact";
	}
}, L;
(function(e) {
	e[e.MONOCHROME = 0] = "MONOCHROME", e[e.NEUTRAL = 1] = "NEUTRAL", e[e.TONAL_SPOT = 2] = "TONAL_SPOT", e[e.VIBRANT = 3] = "VIBRANT", e[e.EXPRESSIVE = 4] = "EXPRESSIVE", e[e.FIDELITY = 5] = "FIDELITY", e[e.CONTENT = 6] = "CONTENT", e[e.RAINBOW = 7] = "RAINBOW", e[e.FRUIT_SALAD = 8] = "FRUIT_SALAD";
})(L ||= {});
function vs(e) {
	return e.variant === L.FIDELITY || e.variant === L.CONTENT;
}
function ys(e) {
	return e.variant === L.MONOCHROME;
}
function bs(e, t, n, r) {
	let i = n, a = M.from(e, t, n);
	if (a.chroma < t) {
		let n = a.chroma;
		for (; a.chroma < t;) {
			i += r ? -1 : 1;
			let o = M.from(e, t, i);
			if (n > o.chroma || Math.abs(o.chroma - t) < .4) break;
			Math.abs(o.chroma - t) < Math.abs(a.chroma - t) && (a = o), n = Math.max(n, o.chroma);
		}
	}
	return i;
}
var xs = class {
	primaryPaletteKeyColor() {
		return P.fromPalette({
			name: "primary_palette_key_color",
			palette: (e) => e.primaryPalette,
			tone: (e) => e.primaryPalette.keyColor.tone
		});
	}
	secondaryPaletteKeyColor() {
		return P.fromPalette({
			name: "secondary_palette_key_color",
			palette: (e) => e.secondaryPalette,
			tone: (e) => e.secondaryPalette.keyColor.tone
		});
	}
	tertiaryPaletteKeyColor() {
		return P.fromPalette({
			name: "tertiary_palette_key_color",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => e.tertiaryPalette.keyColor.tone
		});
	}
	neutralPaletteKeyColor() {
		return P.fromPalette({
			name: "neutral_palette_key_color",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.neutralPalette.keyColor.tone
		});
	}
	neutralVariantPaletteKeyColor() {
		return P.fromPalette({
			name: "neutral_variant_palette_key_color",
			palette: (e) => e.neutralVariantPalette,
			tone: (e) => e.neutralVariantPalette.keyColor.tone
		});
	}
	errorPaletteKeyColor() {
		return P.fromPalette({
			name: "error_palette_key_color",
			palette: (e) => e.errorPalette,
			tone: (e) => e.errorPalette.keyColor.tone
		});
	}
	background() {
		return P.fromPalette({
			name: "background",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 6 : 98,
			isBackground: !0
		});
	}
	onBackground() {
		return P.fromPalette({
			name: "on_background",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 90 : 10,
			background: (e) => this.background(),
			contrastCurve: (e) => new I(3, 3, 4.5, 7)
		});
	}
	surface() {
		return P.fromPalette({
			name: "surface",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 6 : 98,
			isBackground: !0
		});
	}
	surfaceDim() {
		return P.fromPalette({
			name: "surface_dim",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 6 : new I(87, 87, 80, 75).get(e.contrastLevel),
			isBackground: !0
		});
	}
	surfaceBright() {
		return P.fromPalette({
			name: "surface_bright",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? new I(24, 24, 29, 34).get(e.contrastLevel) : 98,
			isBackground: !0
		});
	}
	surfaceContainerLowest() {
		return P.fromPalette({
			name: "surface_container_lowest",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? new I(4, 4, 2, 0).get(e.contrastLevel) : 100,
			isBackground: !0
		});
	}
	surfaceContainerLow() {
		return P.fromPalette({
			name: "surface_container_low",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? new I(10, 10, 11, 12).get(e.contrastLevel) : new I(96, 96, 96, 95).get(e.contrastLevel),
			isBackground: !0
		});
	}
	surfaceContainer() {
		return P.fromPalette({
			name: "surface_container",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? new I(12, 12, 16, 20).get(e.contrastLevel) : new I(94, 94, 92, 90).get(e.contrastLevel),
			isBackground: !0
		});
	}
	surfaceContainerHigh() {
		return P.fromPalette({
			name: "surface_container_high",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? new I(17, 17, 21, 25).get(e.contrastLevel) : new I(92, 92, 88, 85).get(e.contrastLevel),
			isBackground: !0
		});
	}
	surfaceContainerHighest() {
		return P.fromPalette({
			name: "surface_container_highest",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? new I(22, 22, 26, 30).get(e.contrastLevel) : new I(90, 90, 84, 80).get(e.contrastLevel),
			isBackground: !0
		});
	}
	onSurface() {
		return P.fromPalette({
			name: "on_surface",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 90 : 10,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new I(4.5, 7, 11, 21)
		});
	}
	surfaceVariant() {
		return P.fromPalette({
			name: "surface_variant",
			palette: (e) => e.neutralVariantPalette,
			tone: (e) => e.isDark ? 30 : 90,
			isBackground: !0
		});
	}
	onSurfaceVariant() {
		return P.fromPalette({
			name: "on_surface_variant",
			palette: (e) => e.neutralVariantPalette,
			tone: (e) => e.isDark ? 80 : 30,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new I(3, 4.5, 7, 11)
		});
	}
	inverseSurface() {
		return P.fromPalette({
			name: "inverse_surface",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 90 : 20,
			isBackground: !0
		});
	}
	inverseOnSurface() {
		return P.fromPalette({
			name: "inverse_on_surface",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 20 : 95,
			background: (e) => this.inverseSurface(),
			contrastCurve: (e) => new I(4.5, 7, 11, 21)
		});
	}
	outline() {
		return P.fromPalette({
			name: "outline",
			palette: (e) => e.neutralVariantPalette,
			tone: (e) => e.isDark ? 60 : 50,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new I(1.5, 3, 4.5, 7)
		});
	}
	outlineVariant() {
		return P.fromPalette({
			name: "outline_variant",
			palette: (e) => e.neutralVariantPalette,
			tone: (e) => e.isDark ? 30 : 80,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new I(1, 1, 3, 4.5)
		});
	}
	shadow() {
		return P.fromPalette({
			name: "shadow",
			palette: (e) => e.neutralPalette,
			tone: (e) => 0
		});
	}
	scrim() {
		return P.fromPalette({
			name: "scrim",
			palette: (e) => e.neutralPalette,
			tone: (e) => 0
		});
	}
	surfaceTint() {
		return P.fromPalette({
			name: "surface_tint",
			palette: (e) => e.primaryPalette,
			tone: (e) => e.isDark ? 80 : 40,
			isBackground: !0
		});
	}
	primary() {
		return P.fromPalette({
			name: "primary",
			palette: (e) => e.primaryPalette,
			tone: (e) => ys(e) ? e.isDark ? 100 : 0 : e.isDark ? 80 : 40,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new I(3, 4.5, 7, 7),
			toneDeltaPair: (e) => new _s(this.primaryContainer(), this.primary(), 10, "nearer", !1)
		});
	}
	primaryDim() {}
	onPrimary() {
		return P.fromPalette({
			name: "on_primary",
			palette: (e) => e.primaryPalette,
			tone: (e) => ys(e) ? e.isDark ? 10 : 90 : e.isDark ? 20 : 100,
			background: (e) => this.primary(),
			contrastCurve: (e) => new I(4.5, 7, 11, 21)
		});
	}
	primaryContainer() {
		return P.fromPalette({
			name: "primary_container",
			palette: (e) => e.primaryPalette,
			tone: (e) => vs(e) ? e.sourceColorHct.tone : ys(e) ? e.isDark ? 85 : 25 : e.isDark ? 30 : 90,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new I(1, 1, 3, 4.5),
			toneDeltaPair: (e) => new _s(this.primaryContainer(), this.primary(), 10, "nearer", !1)
		});
	}
	onPrimaryContainer() {
		return P.fromPalette({
			name: "on_primary_container",
			palette: (e) => e.primaryPalette,
			tone: (e) => vs(e) ? P.foregroundTone(this.primaryContainer().tone(e), 4.5) : ys(e) ? e.isDark ? 0 : 100 : e.isDark ? 90 : 30,
			background: (e) => this.primaryContainer(),
			contrastCurve: (e) => new I(3, 4.5, 7, 11)
		});
	}
	inversePrimary() {
		return P.fromPalette({
			name: "inverse_primary",
			palette: (e) => e.primaryPalette,
			tone: (e) => e.isDark ? 40 : 80,
			background: (e) => this.inverseSurface(),
			contrastCurve: (e) => new I(3, 4.5, 7, 7)
		});
	}
	secondary() {
		return P.fromPalette({
			name: "secondary",
			palette: (e) => e.secondaryPalette,
			tone: (e) => e.isDark ? 80 : 40,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new I(3, 4.5, 7, 7),
			toneDeltaPair: (e) => new _s(this.secondaryContainer(), this.secondary(), 10, "nearer", !1)
		});
	}
	secondaryDim() {}
	onSecondary() {
		return P.fromPalette({
			name: "on_secondary",
			palette: (e) => e.secondaryPalette,
			tone: (e) => ys(e) ? e.isDark ? 10 : 100 : e.isDark ? 20 : 100,
			background: (e) => this.secondary(),
			contrastCurve: (e) => new I(4.5, 7, 11, 21)
		});
	}
	secondaryContainer() {
		return P.fromPalette({
			name: "secondary_container",
			palette: (e) => e.secondaryPalette,
			tone: (e) => {
				let t = e.isDark ? 30 : 90;
				return ys(e) ? e.isDark ? 30 : 85 : vs(e) ? bs(e.secondaryPalette.hue, e.secondaryPalette.chroma, t, !e.isDark) : t;
			},
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new I(1, 1, 3, 4.5),
			toneDeltaPair: (e) => new _s(this.secondaryContainer(), this.secondary(), 10, "nearer", !1)
		});
	}
	onSecondaryContainer() {
		return P.fromPalette({
			name: "on_secondary_container",
			palette: (e) => e.secondaryPalette,
			tone: (e) => ys(e) ? e.isDark ? 90 : 10 : vs(e) ? P.foregroundTone(this.secondaryContainer().tone(e), 4.5) : e.isDark ? 90 : 30,
			background: (e) => this.secondaryContainer(),
			contrastCurve: (e) => new I(3, 4.5, 7, 11)
		});
	}
	tertiary() {
		return P.fromPalette({
			name: "tertiary",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => ys(e) ? e.isDark ? 90 : 25 : e.isDark ? 80 : 40,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new I(3, 4.5, 7, 7),
			toneDeltaPair: (e) => new _s(this.tertiaryContainer(), this.tertiary(), 10, "nearer", !1)
		});
	}
	tertiaryDim() {}
	onTertiary() {
		return P.fromPalette({
			name: "on_tertiary",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => ys(e) ? e.isDark ? 10 : 90 : e.isDark ? 20 : 100,
			background: (e) => this.tertiary(),
			contrastCurve: (e) => new I(4.5, 7, 11, 21)
		});
	}
	tertiaryContainer() {
		return P.fromPalette({
			name: "tertiary_container",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => {
				if (ys(e)) return e.isDark ? 60 : 49;
				if (!vs(e)) return e.isDark ? 30 : 90;
				let t = e.tertiaryPalette.getHct(e.sourceColorHct.tone);
				return cs.fixIfDisliked(t).tone;
			},
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new I(1, 1, 3, 4.5),
			toneDeltaPair: (e) => new _s(this.tertiaryContainer(), this.tertiary(), 10, "nearer", !1)
		});
	}
	onTertiaryContainer() {
		return P.fromPalette({
			name: "on_tertiary_container",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => ys(e) ? e.isDark ? 0 : 100 : vs(e) ? P.foregroundTone(this.tertiaryContainer().tone(e), 4.5) : e.isDark ? 90 : 30,
			background: (e) => this.tertiaryContainer(),
			contrastCurve: (e) => new I(3, 4.5, 7, 11)
		});
	}
	error() {
		return P.fromPalette({
			name: "error",
			palette: (e) => e.errorPalette,
			tone: (e) => e.isDark ? 80 : 40,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new I(3, 4.5, 7, 7),
			toneDeltaPair: (e) => new _s(this.errorContainer(), this.error(), 10, "nearer", !1)
		});
	}
	errorDim() {}
	onError() {
		return P.fromPalette({
			name: "on_error",
			palette: (e) => e.errorPalette,
			tone: (e) => e.isDark ? 20 : 100,
			background: (e) => this.error(),
			contrastCurve: (e) => new I(4.5, 7, 11, 21)
		});
	}
	errorContainer() {
		return P.fromPalette({
			name: "error_container",
			palette: (e) => e.errorPalette,
			tone: (e) => e.isDark ? 30 : 90,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new I(1, 1, 3, 4.5),
			toneDeltaPair: (e) => new _s(this.errorContainer(), this.error(), 10, "nearer", !1)
		});
	}
	onErrorContainer() {
		return P.fromPalette({
			name: "on_error_container",
			palette: (e) => e.errorPalette,
			tone: (e) => ys(e) ? e.isDark ? 90 : 10 : e.isDark ? 90 : 30,
			background: (e) => this.errorContainer(),
			contrastCurve: (e) => new I(3, 4.5, 7, 11)
		});
	}
	primaryFixed() {
		return P.fromPalette({
			name: "primary_fixed",
			palette: (e) => e.primaryPalette,
			tone: (e) => ys(e) ? 40 : 90,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new I(1, 1, 3, 4.5),
			toneDeltaPair: (e) => new _s(this.primaryFixed(), this.primaryFixedDim(), 10, "lighter", !0)
		});
	}
	primaryFixedDim() {
		return P.fromPalette({
			name: "primary_fixed_dim",
			palette: (e) => e.primaryPalette,
			tone: (e) => ys(e) ? 30 : 80,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new I(1, 1, 3, 4.5),
			toneDeltaPair: (e) => new _s(this.primaryFixed(), this.primaryFixedDim(), 10, "lighter", !0)
		});
	}
	onPrimaryFixed() {
		return P.fromPalette({
			name: "on_primary_fixed",
			palette: (e) => e.primaryPalette,
			tone: (e) => ys(e) ? 100 : 10,
			background: (e) => this.primaryFixedDim(),
			secondBackground: (e) => this.primaryFixed(),
			contrastCurve: (e) => new I(4.5, 7, 11, 21)
		});
	}
	onPrimaryFixedVariant() {
		return P.fromPalette({
			name: "on_primary_fixed_variant",
			palette: (e) => e.primaryPalette,
			tone: (e) => ys(e) ? 90 : 30,
			background: (e) => this.primaryFixedDim(),
			secondBackground: (e) => this.primaryFixed(),
			contrastCurve: (e) => new I(3, 4.5, 7, 11)
		});
	}
	secondaryFixed() {
		return P.fromPalette({
			name: "secondary_fixed",
			palette: (e) => e.secondaryPalette,
			tone: (e) => ys(e) ? 80 : 90,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new I(1, 1, 3, 4.5),
			toneDeltaPair: (e) => new _s(this.secondaryFixed(), this.secondaryFixedDim(), 10, "lighter", !0)
		});
	}
	secondaryFixedDim() {
		return P.fromPalette({
			name: "secondary_fixed_dim",
			palette: (e) => e.secondaryPalette,
			tone: (e) => ys(e) ? 70 : 80,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new I(1, 1, 3, 4.5),
			toneDeltaPair: (e) => new _s(this.secondaryFixed(), this.secondaryFixedDim(), 10, "lighter", !0)
		});
	}
	onSecondaryFixed() {
		return P.fromPalette({
			name: "on_secondary_fixed",
			palette: (e) => e.secondaryPalette,
			tone: (e) => 10,
			background: (e) => this.secondaryFixedDim(),
			secondBackground: (e) => this.secondaryFixed(),
			contrastCurve: (e) => new I(4.5, 7, 11, 21)
		});
	}
	onSecondaryFixedVariant() {
		return P.fromPalette({
			name: "on_secondary_fixed_variant",
			palette: (e) => e.secondaryPalette,
			tone: (e) => ys(e) ? 25 : 30,
			background: (e) => this.secondaryFixedDim(),
			secondBackground: (e) => this.secondaryFixed(),
			contrastCurve: (e) => new I(3, 4.5, 7, 11)
		});
	}
	tertiaryFixed() {
		return P.fromPalette({
			name: "tertiary_fixed",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => ys(e) ? 40 : 90,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new I(1, 1, 3, 4.5),
			toneDeltaPair: (e) => new _s(this.tertiaryFixed(), this.tertiaryFixedDim(), 10, "lighter", !0)
		});
	}
	tertiaryFixedDim() {
		return P.fromPalette({
			name: "tertiary_fixed_dim",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => ys(e) ? 30 : 80,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new I(1, 1, 3, 4.5),
			toneDeltaPair: (e) => new _s(this.tertiaryFixed(), this.tertiaryFixedDim(), 10, "lighter", !0)
		});
	}
	onTertiaryFixed() {
		return P.fromPalette({
			name: "on_tertiary_fixed",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => ys(e) ? 100 : 10,
			background: (e) => this.tertiaryFixedDim(),
			secondBackground: (e) => this.tertiaryFixed(),
			contrastCurve: (e) => new I(4.5, 7, 11, 21)
		});
	}
	onTertiaryFixedVariant() {
		return P.fromPalette({
			name: "on_tertiary_fixed_variant",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => ys(e) ? 90 : 30,
			background: (e) => this.tertiaryFixedDim(),
			secondBackground: (e) => this.tertiaryFixed(),
			contrastCurve: (e) => new I(3, 4.5, 7, 11)
		});
	}
	highestSurface(e) {
		return e.isDark ? this.surfaceBright() : this.surfaceDim();
	}
};
function R(e, t = 0, n = 100, r = 1) {
	return jo(t, n, Cs(e.hue, e.chroma * r, 100, !0));
}
function Ss(e, t = 0, n = 100) {
	return jo(t, n, Cs(e.hue, e.chroma, 0, !1));
}
function Cs(e, t, n, r) {
	let i = n, a = M.from(e, t, i);
	for (; a.chroma < t && !(n < 0 || n > 100);) {
		n += r ? -1 : 1;
		let o = M.from(e, t, n);
		a.chroma < o.chroma && (a = o, i = n);
	}
	return i;
}
function z(e) {
	return e === 1.5 ? new I(1.5, 1.5, 3, 5.5) : e === 3 ? new I(3, 3, 4.5, 7) : e === 4.5 ? new I(4.5, 4.5, 7, 11) : e === 6 ? new I(6, 6, 7, 11) : e === 7 ? new I(7, 7, 11, 21) : e === 9 ? new I(9, 9, 11, 21) : e === 11 ? new I(11, 11, 21, 21) : e === 21 ? new I(21, 21, 21, 21) : new I(e, e, 7, 21);
}
var ws = class extends xs {
	surface() {
		let e = P.fromPalette({
			name: "surface",
			palette: (e) => e.neutralPalette,
			tone: (e) => (super.surface().tone(e), e.platform === "phone" ? e.isDark ? 4 : M.isYellow(e.neutralPalette.hue) ? 99 : e.variant === L.VIBRANT ? 97 : 98 : 0),
			isBackground: !0
		});
		return N(super.surface(), "2025", e);
	}
	surfaceDim() {
		let e = P.fromPalette({
			name: "surface_dim",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 4 : M.isYellow(e.neutralPalette.hue) ? 90 : e.variant === L.VIBRANT ? 85 : 87,
			isBackground: !0,
			chromaMultiplier: (e) => {
				if (!e.isDark) {
					if (e.variant === L.NEUTRAL) return 2.5;
					if (e.variant === L.TONAL_SPOT) return 1.7;
					if (e.variant === L.EXPRESSIVE) return M.isYellow(e.neutralPalette.hue) ? 2.7 : 1.75;
					if (e.variant === L.VIBRANT) return 1.36;
				}
				return 1;
			}
		});
		return N(super.surfaceDim(), "2025", e);
	}
	surfaceBright() {
		let e = P.fromPalette({
			name: "surface_bright",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 18 : M.isYellow(e.neutralPalette.hue) ? 99 : e.variant === L.VIBRANT ? 97 : 98,
			isBackground: !0,
			chromaMultiplier: (e) => {
				if (e.isDark) {
					if (e.variant === L.NEUTRAL) return 2.5;
					if (e.variant === L.TONAL_SPOT) return 1.7;
					if (e.variant === L.EXPRESSIVE) return M.isYellow(e.neutralPalette.hue) ? 2.7 : 1.75;
					if (e.variant === L.VIBRANT) return 1.36;
				}
				return 1;
			}
		});
		return N(super.surfaceBright(), "2025", e);
	}
	surfaceContainerLowest() {
		let e = P.fromPalette({
			name: "surface_container_lowest",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 0 : 100,
			isBackground: !0
		});
		return N(super.surfaceContainerLowest(), "2025", e);
	}
	surfaceContainerLow() {
		let e = P.fromPalette({
			name: "surface_container_low",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.platform === "phone" ? e.isDark ? 6 : M.isYellow(e.neutralPalette.hue) ? 98 : e.variant === L.VIBRANT ? 95 : 96 : 15,
			isBackground: !0,
			chromaMultiplier: (e) => {
				if (e.platform === "phone") {
					if (e.variant === L.NEUTRAL) return 1.3;
					if (e.variant === L.TONAL_SPOT) return 1.25;
					if (e.variant === L.EXPRESSIVE) return M.isYellow(e.neutralPalette.hue) ? 1.3 : 1.15;
					if (e.variant === L.VIBRANT) return 1.08;
				}
				return 1;
			}
		});
		return N(super.surfaceContainerLow(), "2025", e);
	}
	surfaceContainer() {
		let e = P.fromPalette({
			name: "surface_container",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.platform === "phone" ? e.isDark ? 9 : M.isYellow(e.neutralPalette.hue) ? 96 : e.variant === L.VIBRANT ? 92 : 94 : 20,
			isBackground: !0,
			chromaMultiplier: (e) => {
				if (e.platform === "phone") {
					if (e.variant === L.NEUTRAL) return 1.6;
					if (e.variant === L.TONAL_SPOT) return 1.4;
					if (e.variant === L.EXPRESSIVE) return M.isYellow(e.neutralPalette.hue) ? 1.6 : 1.3;
					if (e.variant === L.VIBRANT) return 1.15;
				}
				return 1;
			}
		});
		return N(super.surfaceContainer(), "2025", e);
	}
	surfaceContainerHigh() {
		let e = P.fromPalette({
			name: "surface_container_high",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.platform === "phone" ? e.isDark ? 12 : M.isYellow(e.neutralPalette.hue) ? 94 : e.variant === L.VIBRANT ? 90 : 92 : 25,
			isBackground: !0,
			chromaMultiplier: (e) => {
				if (e.platform === "phone") {
					if (e.variant === L.NEUTRAL) return 1.9;
					if (e.variant === L.TONAL_SPOT) return 1.5;
					if (e.variant === L.EXPRESSIVE) return M.isYellow(e.neutralPalette.hue) ? 1.95 : 1.45;
					if (e.variant === L.VIBRANT) return 1.22;
				}
				return 1;
			}
		});
		return N(super.surfaceContainerHigh(), "2025", e);
	}
	surfaceContainerHighest() {
		let e = P.fromPalette({
			name: "surface_container_highest",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 15 : M.isYellow(e.neutralPalette.hue) ? 92 : e.variant === L.VIBRANT ? 88 : 90,
			isBackground: !0,
			chromaMultiplier: (e) => e.variant === L.NEUTRAL ? 2.2 : e.variant === L.TONAL_SPOT ? 1.7 : e.variant === L.EXPRESSIVE ? M.isYellow(e.neutralPalette.hue) ? 2.3 : 1.6 : e.variant === L.VIBRANT ? 1.29 : 1
		});
		return N(super.surfaceContainerHighest(), "2025", e);
	}
	onSurface() {
		let e = P.fromPalette({
			name: "on_surface",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.variant === L.VIBRANT ? R(e.neutralPalette, 0, 100, 1.1) : P.getInitialToneFromBackground((e) => e.platform === "phone" ? this.highestSurface(e) : this.surfaceContainerHigh())(e),
			chromaMultiplier: (e) => {
				if (e.platform === "phone") {
					if (e.variant === L.NEUTRAL) return 2.2;
					if (e.variant === L.TONAL_SPOT) return 1.7;
					if (e.variant === L.EXPRESSIVE) return M.isYellow(e.neutralPalette.hue) ? e.isDark ? 3 : 2.3 : 1.6;
				}
				return 1;
			},
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : this.surfaceContainerHigh(),
			contrastCurve: (e) => e.isDark && e.platform === "phone" ? z(11) : z(9)
		});
		return N(super.onSurface(), "2025", e);
	}
	onSurfaceVariant() {
		let e = P.fromPalette({
			name: "on_surface_variant",
			palette: (e) => e.neutralPalette,
			chromaMultiplier: (e) => {
				if (e.platform === "phone") {
					if (e.variant === L.NEUTRAL) return 2.2;
					if (e.variant === L.TONAL_SPOT) return 1.7;
					if (e.variant === L.EXPRESSIVE) return M.isYellow(e.neutralPalette.hue) ? e.isDark ? 3 : 2.3 : 1.6;
				}
				return 1;
			},
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : this.surfaceContainerHigh(),
			contrastCurve: (e) => e.platform === "phone" ? e.isDark ? z(6) : z(4.5) : z(7)
		});
		return N(super.onSurfaceVariant(), "2025", e);
	}
	outline() {
		let e = P.fromPalette({
			name: "outline",
			palette: (e) => e.neutralPalette,
			chromaMultiplier: (e) => {
				if (e.platform === "phone") {
					if (e.variant === L.NEUTRAL) return 2.2;
					if (e.variant === L.TONAL_SPOT) return 1.7;
					if (e.variant === L.EXPRESSIVE) return M.isYellow(e.neutralPalette.hue) ? e.isDark ? 3 : 2.3 : 1.6;
				}
				return 1;
			},
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : this.surfaceContainerHigh(),
			contrastCurve: (e) => e.platform === "phone" ? z(3) : z(4.5)
		});
		return N(super.outline(), "2025", e);
	}
	outlineVariant() {
		let e = P.fromPalette({
			name: "outline_variant",
			palette: (e) => e.neutralPalette,
			chromaMultiplier: (e) => {
				if (e.platform === "phone") {
					if (e.variant === L.NEUTRAL) return 2.2;
					if (e.variant === L.TONAL_SPOT) return 1.7;
					if (e.variant === L.EXPRESSIVE) return M.isYellow(e.neutralPalette.hue) ? e.isDark ? 3 : 2.3 : 1.6;
				}
				return 1;
			},
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : this.surfaceContainerHigh(),
			contrastCurve: (e) => e.platform === "phone" ? z(1.5) : z(3)
		});
		return N(super.outlineVariant(), "2025", e);
	}
	inverseSurface() {
		let e = P.fromPalette({
			name: "inverse_surface",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 98 : 4,
			isBackground: !0
		});
		return N(super.inverseSurface(), "2025", e);
	}
	inverseOnSurface() {
		let e = P.fromPalette({
			name: "inverse_on_surface",
			palette: (e) => e.neutralPalette,
			background: (e) => this.inverseSurface(),
			contrastCurve: (e) => z(7)
		});
		return N(super.inverseOnSurface(), "2025", e);
	}
	primary() {
		let e = P.fromPalette({
			name: "primary",
			palette: (e) => e.primaryPalette,
			tone: (e) => e.variant === L.NEUTRAL ? e.platform === "phone" ? e.isDark ? 80 : 40 : 90 : e.variant === L.TONAL_SPOT ? e.platform === "phone" ? e.isDark ? 80 : R(e.primaryPalette) : R(e.primaryPalette, 0, 90) : e.variant === L.EXPRESSIVE ? e.platform === "phone" ? R(e.primaryPalette, 0, M.isYellow(e.primaryPalette.hue) ? 25 : M.isCyan(e.primaryPalette.hue) ? 88 : 98) : R(e.primaryPalette) : e.platform === "phone" ? R(e.primaryPalette, 0, M.isCyan(e.primaryPalette.hue) ? 88 : 98) : R(e.primaryPalette),
			isBackground: !0,
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : this.surfaceContainerHigh(),
			contrastCurve: (e) => e.platform === "phone" ? z(4.5) : z(7),
			toneDeltaPair: (e) => e.platform === "phone" ? new _s(this.primaryContainer(), this.primary(), 5, "relative_lighter", !0, "farther") : void 0
		});
		return N(super.primary(), "2025", e);
	}
	primaryDim() {
		return P.fromPalette({
			name: "primary_dim",
			palette: (e) => e.primaryPalette,
			tone: (e) => e.variant === L.NEUTRAL ? 85 : e.variant === L.TONAL_SPOT ? R(e.primaryPalette, 0, 90) : R(e.primaryPalette),
			isBackground: !0,
			background: (e) => this.surfaceContainerHigh(),
			contrastCurve: (e) => z(4.5),
			toneDeltaPair: (e) => new _s(this.primaryDim(), this.primary(), 5, "darker", !0, "farther")
		});
	}
	onPrimary() {
		let e = P.fromPalette({
			name: "on_primary",
			palette: (e) => e.primaryPalette,
			background: (e) => e.platform === "phone" ? this.primary() : this.primaryDim(),
			contrastCurve: (e) => e.platform === "phone" ? z(6) : z(7)
		});
		return N(super.onPrimary(), "2025", e);
	}
	primaryContainer() {
		let e = P.fromPalette({
			name: "primary_container",
			palette: (e) => e.primaryPalette,
			tone: (e) => e.platform === "watch" ? 30 : e.variant === L.NEUTRAL ? e.isDark ? 30 : 90 : e.variant === L.TONAL_SPOT ? e.isDark ? Ss(e.primaryPalette, 35, 93) : R(e.primaryPalette, 0, 90) : e.variant === L.EXPRESSIVE ? e.isDark ? R(e.primaryPalette, 30, 93) : R(e.primaryPalette, 78, M.isCyan(e.primaryPalette.hue) ? 88 : 90) : e.isDark ? Ss(e.primaryPalette, 66, 93) : R(e.primaryPalette, 66, M.isCyan(e.primaryPalette.hue) ? 88 : 93),
			isBackground: !0,
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : void 0,
			toneDeltaPair: (e) => e.platform === "phone" ? void 0 : new _s(this.primaryContainer(), this.primaryDim(), 10, "darker", !0, "farther"),
			contrastCurve: (e) => e.platform === "phone" && e.contrastLevel > 0 ? z(1.5) : void 0
		});
		return N(super.primaryContainer(), "2025", e);
	}
	onPrimaryContainer() {
		let e = P.fromPalette({
			name: "on_primary_container",
			palette: (e) => e.primaryPalette,
			background: (e) => this.primaryContainer(),
			contrastCurve: (e) => e.platform === "phone" ? z(6) : z(7)
		});
		return N(super.onPrimaryContainer(), "2025", e);
	}
	primaryFixed() {
		let e = P.fromPalette({
			name: "primary_fixed",
			palette: (e) => e.primaryPalette,
			tone: (e) => {
				let t = Object.assign({}, e, {
					isDark: !1,
					contrastLevel: 0
				});
				return this.primaryContainer().getTone(t);
			},
			isBackground: !0,
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : void 0,
			contrastCurve: (e) => e.platform === "phone" && e.contrastLevel > 0 ? z(1.5) : void 0
		});
		return N(super.primaryFixed(), "2025", e);
	}
	primaryFixedDim() {
		let e = P.fromPalette({
			name: "primary_fixed_dim",
			palette: (e) => e.primaryPalette,
			tone: (e) => this.primaryFixed().getTone(e),
			isBackground: !0,
			toneDeltaPair: (e) => new _s(this.primaryFixedDim(), this.primaryFixed(), 5, "darker", !0, "exact")
		});
		return N(super.primaryFixedDim(), "2025", e);
	}
	onPrimaryFixed() {
		let e = P.fromPalette({
			name: "on_primary_fixed",
			palette: (e) => e.primaryPalette,
			background: (e) => this.primaryFixedDim(),
			contrastCurve: (e) => z(7)
		});
		return N(super.onPrimaryFixed(), "2025", e);
	}
	onPrimaryFixedVariant() {
		let e = P.fromPalette({
			name: "on_primary_fixed_variant",
			palette: (e) => e.primaryPalette,
			background: (e) => this.primaryFixedDim(),
			contrastCurve: (e) => z(4.5)
		});
		return N(super.onPrimaryFixedVariant(), "2025", e);
	}
	inversePrimary() {
		let e = P.fromPalette({
			name: "inverse_primary",
			palette: (e) => e.primaryPalette,
			tone: (e) => R(e.primaryPalette),
			background: (e) => this.inverseSurface(),
			contrastCurve: (e) => e.platform === "phone" ? z(6) : z(7)
		});
		return N(super.inversePrimary(), "2025", e);
	}
	secondary() {
		let e = P.fromPalette({
			name: "secondary",
			palette: (e) => e.secondaryPalette,
			tone: (e) => e.platform === "watch" ? e.variant === L.NEUTRAL ? 90 : R(e.secondaryPalette, 0, 90) : e.variant === L.NEUTRAL ? e.isDark ? Ss(e.secondaryPalette, 0, 98) : R(e.secondaryPalette) : e.variant === L.VIBRANT ? R(e.secondaryPalette, 0, e.isDark ? 90 : 98) : e.isDark ? 80 : R(e.secondaryPalette),
			isBackground: !0,
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : this.surfaceContainerHigh(),
			contrastCurve: (e) => e.platform === "phone" ? z(4.5) : z(7),
			toneDeltaPair: (e) => e.platform === "phone" ? new _s(this.secondaryContainer(), this.secondary(), 5, "relative_lighter", !0, "farther") : void 0
		});
		return N(super.secondary(), "2025", e);
	}
	secondaryDim() {
		return P.fromPalette({
			name: "secondary_dim",
			palette: (e) => e.secondaryPalette,
			tone: (e) => e.variant === L.NEUTRAL ? 85 : R(e.secondaryPalette, 0, 90),
			isBackground: !0,
			background: (e) => this.surfaceContainerHigh(),
			contrastCurve: (e) => z(4.5),
			toneDeltaPair: (e) => new _s(this.secondaryDim(), this.secondary(), 5, "darker", !0, "farther")
		});
	}
	onSecondary() {
		let e = P.fromPalette({
			name: "on_secondary",
			palette: (e) => e.secondaryPalette,
			background: (e) => e.platform === "phone" ? this.secondary() : this.secondaryDim(),
			contrastCurve: (e) => e.platform === "phone" ? z(6) : z(7)
		});
		return N(super.onSecondary(), "2025", e);
	}
	secondaryContainer() {
		let e = P.fromPalette({
			name: "secondary_container",
			palette: (e) => e.secondaryPalette,
			tone: (e) => e.platform === "watch" ? 30 : e.variant === L.VIBRANT ? e.isDark ? Ss(e.secondaryPalette, 30, 40) : R(e.secondaryPalette, 84, 90) : e.variant === L.EXPRESSIVE ? e.isDark ? 15 : R(e.secondaryPalette, 90, 95) : e.isDark ? 25 : 90,
			isBackground: !0,
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : void 0,
			toneDeltaPair: (e) => e.platform === "watch" ? new _s(this.secondaryContainer(), this.secondaryDim(), 10, "darker", !0, "farther") : void 0,
			contrastCurve: (e) => e.platform === "phone" && e.contrastLevel > 0 ? z(1.5) : void 0
		});
		return N(super.secondaryContainer(), "2025", e);
	}
	onSecondaryContainer() {
		let e = P.fromPalette({
			name: "on_secondary_container",
			palette: (e) => e.secondaryPalette,
			background: (e) => this.secondaryContainer(),
			contrastCurve: (e) => e.platform === "phone" ? z(6) : z(7)
		});
		return N(super.onSecondaryContainer(), "2025", e);
	}
	secondaryFixed() {
		let e = P.fromPalette({
			name: "secondary_fixed",
			palette: (e) => e.secondaryPalette,
			tone: (e) => {
				let t = Object.assign({}, e, {
					isDark: !1,
					contrastLevel: 0
				});
				return this.secondaryContainer().getTone(t);
			},
			isBackground: !0,
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : void 0,
			contrastCurve: (e) => e.platform === "phone" && e.contrastLevel > 0 ? z(1.5) : void 0
		});
		return N(super.secondaryFixed(), "2025", e);
	}
	secondaryFixedDim() {
		let e = P.fromPalette({
			name: "secondary_fixed_dim",
			palette: (e) => e.secondaryPalette,
			tone: (e) => this.secondaryFixed().getTone(e),
			isBackground: !0,
			toneDeltaPair: (e) => new _s(this.secondaryFixedDim(), this.secondaryFixed(), 5, "darker", !0, "exact")
		});
		return N(super.secondaryFixedDim(), "2025", e);
	}
	onSecondaryFixed() {
		let e = P.fromPalette({
			name: "on_secondary_fixed",
			palette: (e) => e.secondaryPalette,
			background: (e) => this.secondaryFixedDim(),
			contrastCurve: (e) => z(7)
		});
		return N(super.onSecondaryFixed(), "2025", e);
	}
	onSecondaryFixedVariant() {
		let e = P.fromPalette({
			name: "on_secondary_fixed_variant",
			palette: (e) => e.secondaryPalette,
			background: (e) => this.secondaryFixedDim(),
			contrastCurve: (e) => z(4.5)
		});
		return N(super.onSecondaryFixedVariant(), "2025", e);
	}
	tertiary() {
		let e = P.fromPalette({
			name: "tertiary",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => e.platform === "watch" ? e.variant === L.TONAL_SPOT ? R(e.tertiaryPalette, 0, 90) : R(e.tertiaryPalette) : e.variant === L.EXPRESSIVE || e.variant === L.VIBRANT ? R(e.tertiaryPalette, 0, M.isCyan(e.tertiaryPalette.hue) ? 88 : e.isDark ? 98 : 100) : e.isDark ? R(e.tertiaryPalette, 0, 98) : R(e.tertiaryPalette),
			isBackground: !0,
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : this.surfaceContainerHigh(),
			contrastCurve: (e) => e.platform === "phone" ? z(4.5) : z(7),
			toneDeltaPair: (e) => e.platform === "phone" ? new _s(this.tertiaryContainer(), this.tertiary(), 5, "relative_lighter", !0, "farther") : void 0
		});
		return N(super.tertiary(), "2025", e);
	}
	tertiaryDim() {
		return P.fromPalette({
			name: "tertiary_dim",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => e.variant === L.TONAL_SPOT ? R(e.tertiaryPalette, 0, 90) : R(e.tertiaryPalette),
			isBackground: !0,
			background: (e) => this.surfaceContainerHigh(),
			contrastCurve: (e) => z(4.5),
			toneDeltaPair: (e) => new _s(this.tertiaryDim(), this.tertiary(), 5, "darker", !0, "farther")
		});
	}
	onTertiary() {
		let e = P.fromPalette({
			name: "on_tertiary",
			palette: (e) => e.tertiaryPalette,
			background: (e) => e.platform === "phone" ? this.tertiary() : this.tertiaryDim(),
			contrastCurve: (e) => e.platform === "phone" ? z(6) : z(7)
		});
		return N(super.onTertiary(), "2025", e);
	}
	tertiaryContainer() {
		let e = P.fromPalette({
			name: "tertiary_container",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => e.platform === "watch" ? e.variant === L.TONAL_SPOT ? R(e.tertiaryPalette, 0, 90) : R(e.tertiaryPalette) : e.variant === L.NEUTRAL ? e.isDark ? R(e.tertiaryPalette, 0, 93) : R(e.tertiaryPalette, 0, 96) : e.variant === L.TONAL_SPOT ? R(e.tertiaryPalette, 0, e.isDark ? 93 : 100) : e.variant === L.EXPRESSIVE ? R(e.tertiaryPalette, 75, M.isCyan(e.tertiaryPalette.hue) ? 88 : e.isDark ? 93 : 100) : e.isDark ? R(e.tertiaryPalette, 0, 93) : R(e.tertiaryPalette, 72, 100),
			isBackground: !0,
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : void 0,
			toneDeltaPair: (e) => e.platform === "watch" ? new _s(this.tertiaryContainer(), this.tertiaryDim(), 10, "darker", !0, "farther") : void 0,
			contrastCurve: (e) => e.platform === "phone" && e.contrastLevel > 0 ? z(1.5) : void 0
		});
		return N(super.tertiaryContainer(), "2025", e);
	}
	onTertiaryContainer() {
		let e = P.fromPalette({
			name: "on_tertiary_container",
			palette: (e) => e.tertiaryPalette,
			background: (e) => this.tertiaryContainer(),
			contrastCurve: (e) => e.platform === "phone" ? z(6) : z(7)
		});
		return N(super.onTertiaryContainer(), "2025", e);
	}
	tertiaryFixed() {
		let e = P.fromPalette({
			name: "tertiary_fixed",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => {
				let t = Object.assign({}, e, {
					isDark: !1,
					contrastLevel: 0
				});
				return this.tertiaryContainer().getTone(t);
			},
			isBackground: !0,
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : void 0,
			contrastCurve: (e) => e.platform === "phone" && e.contrastLevel > 0 ? z(1.5) : void 0
		});
		return N(super.tertiaryFixed(), "2025", e);
	}
	tertiaryFixedDim() {
		let e = P.fromPalette({
			name: "tertiary_fixed_dim",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => this.tertiaryFixed().getTone(e),
			isBackground: !0,
			toneDeltaPair: (e) => new _s(this.tertiaryFixedDim(), this.tertiaryFixed(), 5, "darker", !0, "exact")
		});
		return N(super.tertiaryFixedDim(), "2025", e);
	}
	onTertiaryFixed() {
		let e = P.fromPalette({
			name: "on_tertiary_fixed",
			palette: (e) => e.tertiaryPalette,
			background: (e) => this.tertiaryFixedDim(),
			contrastCurve: (e) => z(7)
		});
		return N(super.onTertiaryFixed(), "2025", e);
	}
	onTertiaryFixedVariant() {
		let e = P.fromPalette({
			name: "on_tertiary_fixed_variant",
			palette: (e) => e.tertiaryPalette,
			background: (e) => this.tertiaryFixedDim(),
			contrastCurve: (e) => z(4.5)
		});
		return N(super.onTertiaryFixedVariant(), "2025", e);
	}
	error() {
		let e = P.fromPalette({
			name: "error",
			palette: (e) => e.errorPalette,
			tone: (e) => e.platform === "phone" ? e.isDark ? Ss(e.errorPalette, 0, 98) : R(e.errorPalette) : Ss(e.errorPalette),
			isBackground: !0,
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : this.surfaceContainerHigh(),
			contrastCurve: (e) => e.platform === "phone" ? z(4.5) : z(7),
			toneDeltaPair: (e) => e.platform === "phone" ? new _s(this.errorContainer(), this.error(), 5, "relative_lighter", !0, "farther") : void 0
		});
		return N(super.error(), "2025", e);
	}
	errorDim() {
		return P.fromPalette({
			name: "error_dim",
			palette: (e) => e.errorPalette,
			tone: (e) => Ss(e.errorPalette),
			isBackground: !0,
			background: (e) => this.surfaceContainerHigh(),
			contrastCurve: (e) => z(4.5),
			toneDeltaPair: (e) => new _s(this.errorDim(), this.error(), 5, "darker", !0, "farther")
		});
	}
	onError() {
		let e = P.fromPalette({
			name: "on_error",
			palette: (e) => e.errorPalette,
			background: (e) => e.platform === "phone" ? this.error() : this.errorDim(),
			contrastCurve: (e) => e.platform === "phone" ? z(6) : z(7)
		});
		return N(super.onError(), "2025", e);
	}
	errorContainer() {
		let e = P.fromPalette({
			name: "error_container",
			palette: (e) => e.errorPalette,
			tone: (e) => e.platform === "watch" ? 30 : e.isDark ? Ss(e.errorPalette, 30, 93) : R(e.errorPalette, 0, 90),
			isBackground: !0,
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : void 0,
			toneDeltaPair: (e) => e.platform === "watch" ? new _s(this.errorContainer(), this.errorDim(), 10, "darker", !0, "farther") : void 0,
			contrastCurve: (e) => e.platform === "phone" && e.contrastLevel > 0 ? z(1.5) : void 0
		});
		return N(super.errorContainer(), "2025", e);
	}
	onErrorContainer() {
		let e = P.fromPalette({
			name: "on_error_container",
			palette: (e) => e.errorPalette,
			background: (e) => this.errorContainer(),
			contrastCurve: (e) => e.platform === "phone" ? z(4.5) : z(7)
		});
		return N(super.onErrorContainer(), "2025", e);
	}
	surfaceVariant() {
		let e = Object.assign(this.surfaceContainerHighest().clone(), { name: "surface_variant" });
		return N(super.surfaceVariant(), "2025", e);
	}
	surfaceTint() {
		let e = Object.assign(this.primary().clone(), { name: "surface_tint" });
		return N(super.surfaceTint(), "2025", e);
	}
	background() {
		let e = Object.assign(this.surface().clone(), { name: "background" });
		return N(super.background(), "2025", e);
	}
	onBackground() {
		let e = Object.assign(this.onSurface().clone(), {
			name: "on_background",
			tone: (e) => e.platform === "watch" ? 100 : this.onSurface().getTone(e)
		});
		return N(super.onBackground(), "2025", e);
	}
}, B = class e {
	constructor() {
		this.allColors = [
			this.background(),
			this.onBackground(),
			this.surface(),
			this.surfaceDim(),
			this.surfaceBright(),
			this.surfaceContainerLowest(),
			this.surfaceContainerLow(),
			this.surfaceContainer(),
			this.surfaceContainerHigh(),
			this.surfaceContainerHighest(),
			this.onSurface(),
			this.onSurfaceVariant(),
			this.outline(),
			this.outlineVariant(),
			this.inverseSurface(),
			this.inverseOnSurface(),
			this.primary(),
			this.primaryDim(),
			this.onPrimary(),
			this.primaryContainer(),
			this.onPrimaryContainer(),
			this.primaryFixed(),
			this.primaryFixedDim(),
			this.onPrimaryFixed(),
			this.onPrimaryFixedVariant(),
			this.inversePrimary(),
			this.secondary(),
			this.secondaryDim(),
			this.onSecondary(),
			this.secondaryContainer(),
			this.onSecondaryContainer(),
			this.secondaryFixed(),
			this.secondaryFixedDim(),
			this.onSecondaryFixed(),
			this.onSecondaryFixedVariant(),
			this.tertiary(),
			this.tertiaryDim(),
			this.onTertiary(),
			this.tertiaryContainer(),
			this.onTertiaryContainer(),
			this.tertiaryFixed(),
			this.tertiaryFixedDim(),
			this.onTertiaryFixed(),
			this.onTertiaryFixedVariant(),
			this.error(),
			this.errorDim(),
			this.onError(),
			this.errorContainer(),
			this.onErrorContainer()
		].filter((e) => e !== void 0);
	}
	highestSurface(t) {
		return e.colorSpec.highestSurface(t);
	}
	primaryPaletteKeyColor() {
		return e.colorSpec.primaryPaletteKeyColor();
	}
	secondaryPaletteKeyColor() {
		return e.colorSpec.secondaryPaletteKeyColor();
	}
	tertiaryPaletteKeyColor() {
		return e.colorSpec.tertiaryPaletteKeyColor();
	}
	neutralPaletteKeyColor() {
		return e.colorSpec.neutralPaletteKeyColor();
	}
	neutralVariantPaletteKeyColor() {
		return e.colorSpec.neutralVariantPaletteKeyColor();
	}
	errorPaletteKeyColor() {
		return e.colorSpec.errorPaletteKeyColor();
	}
	background() {
		return e.colorSpec.background();
	}
	onBackground() {
		return e.colorSpec.onBackground();
	}
	surface() {
		return e.colorSpec.surface();
	}
	surfaceDim() {
		return e.colorSpec.surfaceDim();
	}
	surfaceBright() {
		return e.colorSpec.surfaceBright();
	}
	surfaceContainerLowest() {
		return e.colorSpec.surfaceContainerLowest();
	}
	surfaceContainerLow() {
		return e.colorSpec.surfaceContainerLow();
	}
	surfaceContainer() {
		return e.colorSpec.surfaceContainer();
	}
	surfaceContainerHigh() {
		return e.colorSpec.surfaceContainerHigh();
	}
	surfaceContainerHighest() {
		return e.colorSpec.surfaceContainerHighest();
	}
	onSurface() {
		return e.colorSpec.onSurface();
	}
	surfaceVariant() {
		return e.colorSpec.surfaceVariant();
	}
	onSurfaceVariant() {
		return e.colorSpec.onSurfaceVariant();
	}
	outline() {
		return e.colorSpec.outline();
	}
	outlineVariant() {
		return e.colorSpec.outlineVariant();
	}
	inverseSurface() {
		return e.colorSpec.inverseSurface();
	}
	inverseOnSurface() {
		return e.colorSpec.inverseOnSurface();
	}
	shadow() {
		return e.colorSpec.shadow();
	}
	scrim() {
		return e.colorSpec.scrim();
	}
	surfaceTint() {
		return e.colorSpec.surfaceTint();
	}
	primary() {
		return e.colorSpec.primary();
	}
	primaryDim() {
		return e.colorSpec.primaryDim();
	}
	onPrimary() {
		return e.colorSpec.onPrimary();
	}
	primaryContainer() {
		return e.colorSpec.primaryContainer();
	}
	onPrimaryContainer() {
		return e.colorSpec.onPrimaryContainer();
	}
	inversePrimary() {
		return e.colorSpec.inversePrimary();
	}
	primaryFixed() {
		return e.colorSpec.primaryFixed();
	}
	primaryFixedDim() {
		return e.colorSpec.primaryFixedDim();
	}
	onPrimaryFixed() {
		return e.colorSpec.onPrimaryFixed();
	}
	onPrimaryFixedVariant() {
		return e.colorSpec.onPrimaryFixedVariant();
	}
	secondary() {
		return e.colorSpec.secondary();
	}
	secondaryDim() {
		return e.colorSpec.secondaryDim();
	}
	onSecondary() {
		return e.colorSpec.onSecondary();
	}
	secondaryContainer() {
		return e.colorSpec.secondaryContainer();
	}
	onSecondaryContainer() {
		return e.colorSpec.onSecondaryContainer();
	}
	secondaryFixed() {
		return e.colorSpec.secondaryFixed();
	}
	secondaryFixedDim() {
		return e.colorSpec.secondaryFixedDim();
	}
	onSecondaryFixed() {
		return e.colorSpec.onSecondaryFixed();
	}
	onSecondaryFixedVariant() {
		return e.colorSpec.onSecondaryFixedVariant();
	}
	tertiary() {
		return e.colorSpec.tertiary();
	}
	tertiaryDim() {
		return e.colorSpec.tertiaryDim();
	}
	onTertiary() {
		return e.colorSpec.onTertiary();
	}
	tertiaryContainer() {
		return e.colorSpec.tertiaryContainer();
	}
	onTertiaryContainer() {
		return e.colorSpec.onTertiaryContainer();
	}
	tertiaryFixed() {
		return e.colorSpec.tertiaryFixed();
	}
	tertiaryFixedDim() {
		return e.colorSpec.tertiaryFixedDim();
	}
	onTertiaryFixed() {
		return e.colorSpec.onTertiaryFixed();
	}
	onTertiaryFixedVariant() {
		return e.colorSpec.onTertiaryFixedVariant();
	}
	error() {
		return e.colorSpec.error();
	}
	errorDim() {
		return e.colorSpec.errorDim();
	}
	onError() {
		return e.colorSpec.onError();
	}
	errorContainer() {
		return e.colorSpec.errorContainer();
	}
	onErrorContainer() {
		return e.colorSpec.onErrorContainer();
	}
	static highestSurface(t) {
		return e.colorSpec.highestSurface(t);
	}
};
B.contentAccentToneDelta = 15, B.colorSpec = new ws(), B.primaryPaletteKeyColor = B.colorSpec.primaryPaletteKeyColor(), B.secondaryPaletteKeyColor = B.colorSpec.secondaryPaletteKeyColor(), B.tertiaryPaletteKeyColor = B.colorSpec.tertiaryPaletteKeyColor(), B.neutralPaletteKeyColor = B.colorSpec.neutralPaletteKeyColor(), B.neutralVariantPaletteKeyColor = B.colorSpec.neutralVariantPaletteKeyColor(), B.background = B.colorSpec.background(), B.onBackground = B.colorSpec.onBackground(), B.surface = B.colorSpec.surface(), B.surfaceDim = B.colorSpec.surfaceDim(), B.surfaceBright = B.colorSpec.surfaceBright(), B.surfaceContainerLowest = B.colorSpec.surfaceContainerLowest(), B.surfaceContainerLow = B.colorSpec.surfaceContainerLow(), B.surfaceContainer = B.colorSpec.surfaceContainer(), B.surfaceContainerHigh = B.colorSpec.surfaceContainerHigh(), B.surfaceContainerHighest = B.colorSpec.surfaceContainerHighest(), B.onSurface = B.colorSpec.onSurface(), B.surfaceVariant = B.colorSpec.surfaceVariant(), B.onSurfaceVariant = B.colorSpec.onSurfaceVariant(), B.inverseSurface = B.colorSpec.inverseSurface(), B.inverseOnSurface = B.colorSpec.inverseOnSurface(), B.outline = B.colorSpec.outline(), B.outlineVariant = B.colorSpec.outlineVariant(), B.shadow = B.colorSpec.shadow(), B.scrim = B.colorSpec.scrim(), B.surfaceTint = B.colorSpec.surfaceTint(), B.primary = B.colorSpec.primary(), B.onPrimary = B.colorSpec.onPrimary(), B.primaryContainer = B.colorSpec.primaryContainer(), B.onPrimaryContainer = B.colorSpec.onPrimaryContainer(), B.inversePrimary = B.colorSpec.inversePrimary(), B.secondary = B.colorSpec.secondary(), B.onSecondary = B.colorSpec.onSecondary(), B.secondaryContainer = B.colorSpec.secondaryContainer(), B.onSecondaryContainer = B.colorSpec.onSecondaryContainer(), B.tertiary = B.colorSpec.tertiary(), B.onTertiary = B.colorSpec.onTertiary(), B.tertiaryContainer = B.colorSpec.tertiaryContainer(), B.onTertiaryContainer = B.colorSpec.onTertiaryContainer(), B.error = B.colorSpec.error(), B.onError = B.colorSpec.onError(), B.errorContainer = B.colorSpec.errorContainer(), B.onErrorContainer = B.colorSpec.onErrorContainer(), B.primaryFixed = B.colorSpec.primaryFixed(), B.primaryFixedDim = B.colorSpec.primaryFixedDim(), B.onPrimaryFixed = B.colorSpec.onPrimaryFixed(), B.onPrimaryFixedVariant = B.colorSpec.onPrimaryFixedVariant(), B.secondaryFixed = B.colorSpec.secondaryFixed(), B.secondaryFixedDim = B.colorSpec.secondaryFixedDim(), B.onSecondaryFixed = B.colorSpec.onSecondaryFixed(), B.onSecondaryFixedVariant = B.colorSpec.onSecondaryFixedVariant(), B.tertiaryFixed = B.colorSpec.tertiaryFixed(), B.tertiaryFixedDim = B.colorSpec.tertiaryFixedDim(), B.onTertiaryFixed = B.colorSpec.onTertiaryFixed(), B.onTertiaryFixedVariant = B.colorSpec.onTertiaryFixedVariant();
var Ts = class e {
	static maybeFallbackSpecVersion(e, t) {
		switch (t) {
			case L.EXPRESSIVE:
			case L.VIBRANT:
			case L.TONAL_SPOT:
			case L.NEUTRAL: return e;
			default: return "2021";
		}
	}
	constructor(t) {
		this.sourceColorArgb = t.sourceColorHct.toInt(), this.variant = t.variant, this.contrastLevel = t.contrastLevel, this.isDark = t.isDark, this.platform = t.platform ?? "phone", this.specVersion = e.maybeFallbackSpecVersion(t.specVersion ?? "2021", this.variant), this.sourceColorHct = t.sourceColorHct, this.primaryPalette = t.primaryPalette ?? As(this.specVersion).getPrimaryPalette(this.variant, t.sourceColorHct, this.isDark, this.platform, this.contrastLevel), this.secondaryPalette = t.secondaryPalette ?? As(this.specVersion).getSecondaryPalette(this.variant, t.sourceColorHct, this.isDark, this.platform, this.contrastLevel), this.tertiaryPalette = t.tertiaryPalette ?? As(this.specVersion).getTertiaryPalette(this.variant, t.sourceColorHct, this.isDark, this.platform, this.contrastLevel), this.neutralPalette = t.neutralPalette ?? As(this.specVersion).getNeutralPalette(this.variant, t.sourceColorHct, this.isDark, this.platform, this.contrastLevel), this.neutralVariantPalette = t.neutralVariantPalette ?? As(this.specVersion).getNeutralVariantPalette(this.variant, t.sourceColorHct, this.isDark, this.platform, this.contrastLevel), this.errorPalette = t.errorPalette ?? As(this.specVersion).getErrorPalette(this.variant, t.sourceColorHct, this.isDark, this.platform, this.contrastLevel) ?? F.fromHueAndChroma(25, 84), this.colors = new B();
	}
	toString() {
		return `Scheme: variant=${L[this.variant]}, mode=${this.isDark ? "dark" : "light"}, platform=${this.platform}, contrastLevel=${this.contrastLevel.toFixed(1)}, seed=${this.sourceColorHct.toString()}, specVersion=${this.specVersion}`;
	}
	static getPiecewiseHue(e, t, n) {
		let r = Math.min(t.length - 1, n.length), i = e.hue;
		for (let e = 0; e < r; e++) if (i >= t[e] && i < t[e + 1]) return No(n[e]);
		return i;
	}
	static getRotatedHue(t, n, r) {
		let i = e.getPiecewiseHue(t, n, r);
		return Math.min(n.length - 1, r.length) <= 0 && (i = 0), No(t.hue + i);
	}
	getArgb(e) {
		return e.getArgb(this);
	}
	getHct(e) {
		return e.getHct(this);
	}
	get primaryPaletteKeyColor() {
		return this.getArgb(this.colors.primaryPaletteKeyColor());
	}
	get secondaryPaletteKeyColor() {
		return this.getArgb(this.colors.secondaryPaletteKeyColor());
	}
	get tertiaryPaletteKeyColor() {
		return this.getArgb(this.colors.tertiaryPaletteKeyColor());
	}
	get neutralPaletteKeyColor() {
		return this.getArgb(this.colors.neutralPaletteKeyColor());
	}
	get neutralVariantPaletteKeyColor() {
		return this.getArgb(this.colors.neutralVariantPaletteKeyColor());
	}
	get errorPaletteKeyColor() {
		return this.getArgb(this.colors.errorPaletteKeyColor());
	}
	get background() {
		return this.getArgb(this.colors.background());
	}
	get onBackground() {
		return this.getArgb(this.colors.onBackground());
	}
	get surface() {
		return this.getArgb(this.colors.surface());
	}
	get surfaceDim() {
		return this.getArgb(this.colors.surfaceDim());
	}
	get surfaceBright() {
		return this.getArgb(this.colors.surfaceBright());
	}
	get surfaceContainerLowest() {
		return this.getArgb(this.colors.surfaceContainerLowest());
	}
	get surfaceContainerLow() {
		return this.getArgb(this.colors.surfaceContainerLow());
	}
	get surfaceContainer() {
		return this.getArgb(this.colors.surfaceContainer());
	}
	get surfaceContainerHigh() {
		return this.getArgb(this.colors.surfaceContainerHigh());
	}
	get surfaceContainerHighest() {
		return this.getArgb(this.colors.surfaceContainerHighest());
	}
	get onSurface() {
		return this.getArgb(this.colors.onSurface());
	}
	get surfaceVariant() {
		return this.getArgb(this.colors.surfaceVariant());
	}
	get onSurfaceVariant() {
		return this.getArgb(this.colors.onSurfaceVariant());
	}
	get inverseSurface() {
		return this.getArgb(this.colors.inverseSurface());
	}
	get inverseOnSurface() {
		return this.getArgb(this.colors.inverseOnSurface());
	}
	get outline() {
		return this.getArgb(this.colors.outline());
	}
	get outlineVariant() {
		return this.getArgb(this.colors.outlineVariant());
	}
	get shadow() {
		return this.getArgb(this.colors.shadow());
	}
	get scrim() {
		return this.getArgb(this.colors.scrim());
	}
	get surfaceTint() {
		return this.getArgb(this.colors.surfaceTint());
	}
	get primary() {
		return this.getArgb(this.colors.primary());
	}
	get primaryDim() {
		let e = this.colors.primaryDim();
		if (e === void 0) throw Error("`primaryDim` color is undefined prior to 2025 spec.");
		return this.getArgb(e);
	}
	get onPrimary() {
		return this.getArgb(this.colors.onPrimary());
	}
	get primaryContainer() {
		return this.getArgb(this.colors.primaryContainer());
	}
	get onPrimaryContainer() {
		return this.getArgb(this.colors.onPrimaryContainer());
	}
	get primaryFixed() {
		return this.getArgb(this.colors.primaryFixed());
	}
	get primaryFixedDim() {
		return this.getArgb(this.colors.primaryFixedDim());
	}
	get onPrimaryFixed() {
		return this.getArgb(this.colors.onPrimaryFixed());
	}
	get onPrimaryFixedVariant() {
		return this.getArgb(this.colors.onPrimaryFixedVariant());
	}
	get inversePrimary() {
		return this.getArgb(this.colors.inversePrimary());
	}
	get secondary() {
		return this.getArgb(this.colors.secondary());
	}
	get secondaryDim() {
		let e = this.colors.secondaryDim();
		if (e === void 0) throw Error("`secondaryDim` color is undefined prior to 2025 spec.");
		return this.getArgb(e);
	}
	get onSecondary() {
		return this.getArgb(this.colors.onSecondary());
	}
	get secondaryContainer() {
		return this.getArgb(this.colors.secondaryContainer());
	}
	get onSecondaryContainer() {
		return this.getArgb(this.colors.onSecondaryContainer());
	}
	get secondaryFixed() {
		return this.getArgb(this.colors.secondaryFixed());
	}
	get secondaryFixedDim() {
		return this.getArgb(this.colors.secondaryFixedDim());
	}
	get onSecondaryFixed() {
		return this.getArgb(this.colors.onSecondaryFixed());
	}
	get onSecondaryFixedVariant() {
		return this.getArgb(this.colors.onSecondaryFixedVariant());
	}
	get tertiary() {
		return this.getArgb(this.colors.tertiary());
	}
	get tertiaryDim() {
		let e = this.colors.tertiaryDim();
		if (e === void 0) throw Error("`tertiaryDim` color is undefined prior to 2025 spec.");
		return this.getArgb(e);
	}
	get onTertiary() {
		return this.getArgb(this.colors.onTertiary());
	}
	get tertiaryContainer() {
		return this.getArgb(this.colors.tertiaryContainer());
	}
	get onTertiaryContainer() {
		return this.getArgb(this.colors.onTertiaryContainer());
	}
	get tertiaryFixed() {
		return this.getArgb(this.colors.tertiaryFixed());
	}
	get tertiaryFixedDim() {
		return this.getArgb(this.colors.tertiaryFixedDim());
	}
	get onTertiaryFixed() {
		return this.getArgb(this.colors.onTertiaryFixed());
	}
	get onTertiaryFixedVariant() {
		return this.getArgb(this.colors.onTertiaryFixedVariant());
	}
	get error() {
		return this.getArgb(this.colors.error());
	}
	get errorDim() {
		let e = this.colors.errorDim();
		if (e === void 0) throw Error("`errorDim` color is undefined prior to 2025 spec.");
		return this.getArgb(e);
	}
	get onError() {
		return this.getArgb(this.colors.onError());
	}
	get errorContainer() {
		return this.getArgb(this.colors.errorContainer());
	}
	get onErrorContainer() {
		return this.getArgb(this.colors.onErrorContainer());
	}
};
Ts.DEFAULT_SPEC_VERSION = "2021", Ts.DEFAULT_PLATFORM = "phone";
var Es = class {
	getPrimaryPalette(e, t, n, r, i) {
		switch (e) {
			case L.CONTENT:
			case L.FIDELITY: return F.fromHueAndChroma(t.hue, t.chroma);
			case L.FRUIT_SALAD: return F.fromHueAndChroma(No(t.hue - 50), 48);
			case L.MONOCHROME: return F.fromHueAndChroma(t.hue, 0);
			case L.NEUTRAL: return F.fromHueAndChroma(t.hue, 12);
			case L.RAINBOW: return F.fromHueAndChroma(t.hue, 48);
			case L.TONAL_SPOT: return F.fromHueAndChroma(t.hue, 36);
			case L.EXPRESSIVE: return F.fromHueAndChroma(No(t.hue + 240), 40);
			case L.VIBRANT: return F.fromHueAndChroma(t.hue, 200);
			default: throw Error(`Unsupported variant: ${e}`);
		}
	}
	getSecondaryPalette(e, t, n, r, i) {
		switch (e) {
			case L.CONTENT:
			case L.FIDELITY: return F.fromHueAndChroma(t.hue, Math.max(t.chroma - 32, t.chroma * .5));
			case L.FRUIT_SALAD: return F.fromHueAndChroma(No(t.hue - 50), 36);
			case L.MONOCHROME: return F.fromHueAndChroma(t.hue, 0);
			case L.NEUTRAL: return F.fromHueAndChroma(t.hue, 8);
			case L.RAINBOW: return F.fromHueAndChroma(t.hue, 16);
			case L.TONAL_SPOT: return F.fromHueAndChroma(t.hue, 16);
			case L.EXPRESSIVE: return F.fromHueAndChroma(Ts.getRotatedHue(t, [
				0,
				21,
				51,
				121,
				151,
				191,
				271,
				321,
				360
			], [
				45,
				95,
				45,
				20,
				45,
				90,
				45,
				45,
				45
			]), 24);
			case L.VIBRANT: return F.fromHueAndChroma(Ts.getRotatedHue(t, [
				0,
				41,
				61,
				101,
				131,
				181,
				251,
				301,
				360
			], [
				18,
				15,
				10,
				12,
				15,
				18,
				15,
				12,
				12
			]), 24);
			default: throw Error(`Unsupported variant: ${e}`);
		}
	}
	getTertiaryPalette(e, t, n, r, i) {
		switch (e) {
			case L.CONTENT: return F.fromHct(cs.fixIfDisliked(new gs(t).analogous(3, 6)[2]));
			case L.FIDELITY: return F.fromHct(cs.fixIfDisliked(new gs(t).complement));
			case L.FRUIT_SALAD: return F.fromHueAndChroma(t.hue, 36);
			case L.MONOCHROME: return F.fromHueAndChroma(t.hue, 0);
			case L.NEUTRAL: return F.fromHueAndChroma(t.hue, 16);
			case L.RAINBOW:
			case L.TONAL_SPOT: return F.fromHueAndChroma(No(t.hue + 60), 24);
			case L.EXPRESSIVE: return F.fromHueAndChroma(Ts.getRotatedHue(t, [
				0,
				21,
				51,
				121,
				151,
				191,
				271,
				321,
				360
			], [
				120,
				120,
				20,
				45,
				20,
				15,
				20,
				120,
				120
			]), 32);
			case L.VIBRANT: return F.fromHueAndChroma(Ts.getRotatedHue(t, [
				0,
				41,
				61,
				101,
				131,
				181,
				251,
				301,
				360
			], [
				35,
				30,
				20,
				25,
				30,
				35,
				30,
				25,
				25
			]), 32);
			default: throw Error(`Unsupported variant: ${e}`);
		}
	}
	getNeutralPalette(e, t, n, r, i) {
		switch (e) {
			case L.CONTENT:
			case L.FIDELITY: return F.fromHueAndChroma(t.hue, t.chroma / 8);
			case L.FRUIT_SALAD: return F.fromHueAndChroma(t.hue, 10);
			case L.MONOCHROME: return F.fromHueAndChroma(t.hue, 0);
			case L.NEUTRAL: return F.fromHueAndChroma(t.hue, 2);
			case L.RAINBOW: return F.fromHueAndChroma(t.hue, 0);
			case L.TONAL_SPOT: return F.fromHueAndChroma(t.hue, 6);
			case L.EXPRESSIVE: return F.fromHueAndChroma(No(t.hue + 15), 8);
			case L.VIBRANT: return F.fromHueAndChroma(t.hue, 10);
			default: throw Error(`Unsupported variant: ${e}`);
		}
	}
	getNeutralVariantPalette(e, t, n, r, i) {
		switch (e) {
			case L.CONTENT: return F.fromHueAndChroma(t.hue, t.chroma / 8 + 4);
			case L.FIDELITY: return F.fromHueAndChroma(t.hue, t.chroma / 8 + 4);
			case L.FRUIT_SALAD: return F.fromHueAndChroma(t.hue, 16);
			case L.MONOCHROME: return F.fromHueAndChroma(t.hue, 0);
			case L.NEUTRAL: return F.fromHueAndChroma(t.hue, 2);
			case L.RAINBOW: return F.fromHueAndChroma(t.hue, 0);
			case L.TONAL_SPOT: return F.fromHueAndChroma(t.hue, 8);
			case L.EXPRESSIVE: return F.fromHueAndChroma(No(t.hue + 15), 12);
			case L.VIBRANT: return F.fromHueAndChroma(t.hue, 12);
			default: throw Error(`Unsupported variant: ${e}`);
		}
	}
	getErrorPalette(e, t, n, r, i) {}
}, Ds = class e extends Es {
	getPrimaryPalette(e, t, n, r, i) {
		switch (e) {
			case L.NEUTRAL: return F.fromHueAndChroma(t.hue, r === "phone" ? M.isBlue(t.hue) ? 12 : 8 : M.isBlue(t.hue) ? 16 : 12);
			case L.TONAL_SPOT: return F.fromHueAndChroma(t.hue, r === "phone" && n ? 26 : 32);
			case L.EXPRESSIVE: return F.fromHueAndChroma(t.hue, r === "phone" ? n ? 36 : 48 : 40);
			case L.VIBRANT: return F.fromHueAndChroma(t.hue, r === "phone" ? 74 : 56);
			default: return super.getPrimaryPalette(e, t, n, r, i);
		}
	}
	getSecondaryPalette(e, t, n, r, i) {
		switch (e) {
			case L.NEUTRAL: return F.fromHueAndChroma(t.hue, r === "phone" ? M.isBlue(t.hue) ? 6 : 4 : M.isBlue(t.hue) ? 10 : 6);
			case L.TONAL_SPOT: return F.fromHueAndChroma(t.hue, 16);
			case L.EXPRESSIVE: return F.fromHueAndChroma(Ts.getRotatedHue(t, [
				0,
				105,
				140,
				204,
				253,
				278,
				300,
				333,
				360
			], [
				-160,
				155,
				-100,
				96,
				-96,
				-156,
				-165,
				-160
			]), r === "phone" && n ? 16 : 24);
			case L.VIBRANT: return F.fromHueAndChroma(Ts.getRotatedHue(t, [
				0,
				38,
				105,
				140,
				333,
				360
			], [
				-14,
				10,
				-14,
				10,
				-14
			]), r === "phone" ? 56 : 36);
			default: return super.getSecondaryPalette(e, t, n, r, i);
		}
	}
	getTertiaryPalette(e, t, n, r, i) {
		switch (e) {
			case L.NEUTRAL: return F.fromHueAndChroma(Ts.getRotatedHue(t, [
				0,
				38,
				105,
				161,
				204,
				278,
				333,
				360
			], [
				-32,
				26,
				10,
				-39,
				24,
				-15,
				-32
			]), r === "phone" ? 20 : 36);
			case L.TONAL_SPOT: return F.fromHueAndChroma(Ts.getRotatedHue(t, [
				0,
				20,
				71,
				161,
				333,
				360
			], [
				-40,
				48,
				-32,
				40,
				-32
			]), r === "phone" ? 28 : 32);
			case L.EXPRESSIVE: return F.fromHueAndChroma(Ts.getRotatedHue(t, [
				0,
				105,
				140,
				204,
				253,
				278,
				300,
				333,
				360
			], [
				-165,
				160,
				-105,
				101,
				-101,
				-160,
				-170,
				-165
			]), 48);
			case L.VIBRANT: return F.fromHueAndChroma(Ts.getRotatedHue(t, [
				0,
				38,
				71,
				105,
				140,
				161,
				253,
				333,
				360
			], [
				-72,
				35,
				24,
				-24,
				62,
				50,
				62,
				-72
			]), 56);
			default: return super.getTertiaryPalette(e, t, n, r, i);
		}
	}
	static getExpressiveNeutralHue(e) {
		return Ts.getRotatedHue(e, [
			0,
			71,
			124,
			253,
			278,
			300,
			360
		], [
			10,
			0,
			10,
			0,
			10,
			0
		]);
	}
	static getExpressiveNeutralChroma(t, n, r) {
		let i = e.getExpressiveNeutralHue(t);
		return r === "phone" ? n ? M.isYellow(i) ? 6 : 14 : 18 : 12;
	}
	static getVibrantNeutralHue(e) {
		return Ts.getRotatedHue(e, [
			0,
			38,
			105,
			140,
			333,
			360
		], [
			-14,
			10,
			-14,
			10,
			-14
		]);
	}
	static getVibrantNeutralChroma(t, n) {
		let r = e.getVibrantNeutralHue(t);
		return n === "phone" || M.isBlue(r) ? 28 : 20;
	}
	getNeutralPalette(t, n, r, i, a) {
		switch (t) {
			case L.NEUTRAL: return F.fromHueAndChroma(n.hue, i === "phone" ? 1.4 : 6);
			case L.TONAL_SPOT: return F.fromHueAndChroma(n.hue, i === "phone" ? 5 : 10);
			case L.EXPRESSIVE: return F.fromHueAndChroma(e.getExpressiveNeutralHue(n), e.getExpressiveNeutralChroma(n, r, i));
			case L.VIBRANT: return F.fromHueAndChroma(e.getVibrantNeutralHue(n), e.getVibrantNeutralChroma(n, i));
			default: return super.getNeutralPalette(t, n, r, i, a);
		}
	}
	getNeutralVariantPalette(t, n, r, i, a) {
		switch (t) {
			case L.NEUTRAL: return F.fromHueAndChroma(n.hue, (i === "phone" ? 1.4 : 6) * 2.2);
			case L.TONAL_SPOT: return F.fromHueAndChroma(n.hue, (i === "phone" ? 5 : 10) * 1.7);
			case L.EXPRESSIVE:
				let o = e.getExpressiveNeutralHue(n), s = e.getExpressiveNeutralChroma(n, r, i);
				return F.fromHueAndChroma(o, s * (o >= 105 && o < 125 ? 1.6 : 2.3));
			case L.VIBRANT:
				let c = e.getVibrantNeutralHue(n), l = e.getVibrantNeutralChroma(n, i);
				return F.fromHueAndChroma(c, l * 1.29);
			default: return super.getNeutralVariantPalette(t, n, r, i, a);
		}
	}
	getErrorPalette(e, t, n, r, i) {
		let a = Ts.getPiecewiseHue(t, [
			0,
			3,
			13,
			23,
			33,
			43,
			153,
			273,
			360
		], [
			12,
			22,
			32,
			12,
			22,
			32,
			22,
			12
		]);
		switch (e) {
			case L.NEUTRAL: return F.fromHueAndChroma(a, r === "phone" ? 50 : 40);
			case L.TONAL_SPOT: return F.fromHueAndChroma(a, r === "phone" ? 60 : 48);
			case L.EXPRESSIVE: return F.fromHueAndChroma(a, r === "phone" ? 64 : 48);
			case L.VIBRANT: return F.fromHueAndChroma(a, r === "phone" ? 80 : 60);
			default: return super.getErrorPalette(e, t, n, r, i);
		}
	}
}, Os = new Es(), ks = new Ds();
function As(e) {
	return e === "2025" ? ks : Os;
}
var js = class e {
	static of(t) {
		return new e(t, !1);
	}
	static contentOf(t) {
		return new e(t, !0);
	}
	static fromColors(t) {
		return e.createPaletteFromColors(!1, t);
	}
	static contentFromColors(t) {
		return e.createPaletteFromColors(!0, t);
	}
	static createPaletteFromColors(t, n) {
		let r = new e(n.primary, t);
		return n.secondary && (r.a2 = new e(n.secondary, t).a1), n.tertiary && (r.a3 = new e(n.tertiary, t).a1), n.error && (r.error = new e(n.error, t).a1), n.neutral && (r.n1 = new e(n.neutral, t).n1), n.neutralVariant && (r.n2 = new e(n.neutralVariant, t).n2), r;
	}
	constructor(e, t) {
		let n = M.fromInt(e), r = n.hue, i = n.chroma;
		t ? (this.a1 = F.fromHueAndChroma(r, i), this.a2 = F.fromHueAndChroma(r, i / 3), this.a3 = F.fromHueAndChroma(r + 60, i / 2), this.n1 = F.fromHueAndChroma(r, Math.min(i / 12, 4)), this.n2 = F.fromHueAndChroma(r, Math.min(i / 6, 8))) : (this.a1 = F.fromHueAndChroma(r, Math.max(48, i)), this.a2 = F.fromHueAndChroma(r, 16), this.a3 = F.fromHueAndChroma(r + 60, 24), this.n1 = F.fromHueAndChroma(r, 4), this.n2 = F.fromHueAndChroma(r, 8)), this.error = F.fromHueAndChroma(25, 84);
	}
}, Ms = class e {
	get primary() {
		return this.props.primary;
	}
	get onPrimary() {
		return this.props.onPrimary;
	}
	get primaryContainer() {
		return this.props.primaryContainer;
	}
	get onPrimaryContainer() {
		return this.props.onPrimaryContainer;
	}
	get secondary() {
		return this.props.secondary;
	}
	get onSecondary() {
		return this.props.onSecondary;
	}
	get secondaryContainer() {
		return this.props.secondaryContainer;
	}
	get onSecondaryContainer() {
		return this.props.onSecondaryContainer;
	}
	get tertiary() {
		return this.props.tertiary;
	}
	get onTertiary() {
		return this.props.onTertiary;
	}
	get tertiaryContainer() {
		return this.props.tertiaryContainer;
	}
	get onTertiaryContainer() {
		return this.props.onTertiaryContainer;
	}
	get error() {
		return this.props.error;
	}
	get onError() {
		return this.props.onError;
	}
	get errorContainer() {
		return this.props.errorContainer;
	}
	get onErrorContainer() {
		return this.props.onErrorContainer;
	}
	get background() {
		return this.props.background;
	}
	get onBackground() {
		return this.props.onBackground;
	}
	get surface() {
		return this.props.surface;
	}
	get onSurface() {
		return this.props.onSurface;
	}
	get surfaceVariant() {
		return this.props.surfaceVariant;
	}
	get onSurfaceVariant() {
		return this.props.onSurfaceVariant;
	}
	get outline() {
		return this.props.outline;
	}
	get outlineVariant() {
		return this.props.outlineVariant;
	}
	get shadow() {
		return this.props.shadow;
	}
	get scrim() {
		return this.props.scrim;
	}
	get inverseSurface() {
		return this.props.inverseSurface;
	}
	get inverseOnSurface() {
		return this.props.inverseOnSurface;
	}
	get inversePrimary() {
		return this.props.inversePrimary;
	}
	static light(t) {
		return e.lightFromCorePalette(js.of(t));
	}
	static dark(t) {
		return e.darkFromCorePalette(js.of(t));
	}
	static lightContent(t) {
		return e.lightFromCorePalette(js.contentOf(t));
	}
	static darkContent(t) {
		return e.darkFromCorePalette(js.contentOf(t));
	}
	static lightFromCorePalette(t) {
		return new e({
			primary: t.a1.tone(40),
			onPrimary: t.a1.tone(100),
			primaryContainer: t.a1.tone(90),
			onPrimaryContainer: t.a1.tone(10),
			secondary: t.a2.tone(40),
			onSecondary: t.a2.tone(100),
			secondaryContainer: t.a2.tone(90),
			onSecondaryContainer: t.a2.tone(10),
			tertiary: t.a3.tone(40),
			onTertiary: t.a3.tone(100),
			tertiaryContainer: t.a3.tone(90),
			onTertiaryContainer: t.a3.tone(10),
			error: t.error.tone(40),
			onError: t.error.tone(100),
			errorContainer: t.error.tone(90),
			onErrorContainer: t.error.tone(10),
			background: t.n1.tone(99),
			onBackground: t.n1.tone(10),
			surface: t.n1.tone(99),
			onSurface: t.n1.tone(10),
			surfaceVariant: t.n2.tone(90),
			onSurfaceVariant: t.n2.tone(30),
			outline: t.n2.tone(50),
			outlineVariant: t.n2.tone(80),
			shadow: t.n1.tone(0),
			scrim: t.n1.tone(0),
			inverseSurface: t.n1.tone(20),
			inverseOnSurface: t.n1.tone(95),
			inversePrimary: t.a1.tone(80)
		});
	}
	static darkFromCorePalette(t) {
		return new e({
			primary: t.a1.tone(80),
			onPrimary: t.a1.tone(20),
			primaryContainer: t.a1.tone(30),
			onPrimaryContainer: t.a1.tone(90),
			secondary: t.a2.tone(80),
			onSecondary: t.a2.tone(20),
			secondaryContainer: t.a2.tone(30),
			onSecondaryContainer: t.a2.tone(90),
			tertiary: t.a3.tone(80),
			onTertiary: t.a3.tone(20),
			tertiaryContainer: t.a3.tone(30),
			onTertiaryContainer: t.a3.tone(90),
			error: t.error.tone(80),
			onError: t.error.tone(20),
			errorContainer: t.error.tone(30),
			onErrorContainer: t.error.tone(80),
			background: t.n1.tone(10),
			onBackground: t.n1.tone(90),
			surface: t.n1.tone(10),
			onSurface: t.n1.tone(90),
			surfaceVariant: t.n2.tone(30),
			onSurfaceVariant: t.n2.tone(80),
			outline: t.n2.tone(60),
			outlineVariant: t.n2.tone(30),
			shadow: t.n1.tone(0),
			scrim: t.n1.tone(0),
			inverseSurface: t.n1.tone(90),
			inverseOnSurface: t.n1.tone(20),
			inversePrimary: t.a1.tone(40)
		});
	}
	constructor(e) {
		this.props = e;
	}
	toJSON() {
		return { ...this.props };
	}
};
function Ns(e) {
	let t = Ho(e), n = Uo(e), r = Wo(e), i = [
		t.toString(16),
		n.toString(16),
		r.toString(16)
	];
	for (let [e, t] of i.entries()) t.length === 1 && (i[e] = "0" + t);
	return "#" + i.join("");
}
function Ps(e) {
	e = e.replace("#", "");
	let t = e.length === 3, n = e.length === 6, r = e.length === 8;
	if (!t && !n && !r) throw Error("unexpected hex " + e);
	let i = 0, a = 0, o = 0;
	return t ? (i = Fs(e.slice(0, 1).repeat(2)), a = Fs(e.slice(1, 2).repeat(2)), o = Fs(e.slice(2, 3).repeat(2))) : n ? (i = Fs(e.slice(0, 2)), a = Fs(e.slice(2, 4)), o = Fs(e.slice(4, 6))) : r && (i = Fs(e.slice(2, 4)), a = Fs(e.slice(4, 6)), o = Fs(e.slice(6, 8))), (255 << 24 | (i & 255) << 16 | (a & 255) << 8 | o & 255) >>> 0;
}
function Fs(e) {
	return parseInt(e, 16);
}
function Is(e, t = []) {
	let n = js.of(e);
	return {
		source: e,
		schemes: {
			light: Ms.light(e),
			dark: Ms.dark(e)
		},
		palettes: {
			primary: n.a1,
			secondary: n.a2,
			tertiary: n.a3,
			neutral: n.n1,
			neutralVariant: n.n2,
			error: n.error
		},
		customColors: t.map((t) => Ls(e, t))
	};
}
function Ls(e, t) {
	let n = t.value, r = n, i = e;
	t.blend && (n = os.harmonize(r, i));
	let a = js.of(n).a1;
	return {
		color: t,
		value: n,
		light: {
			color: a.tone(40),
			onColor: a.tone(100),
			colorContainer: a.tone(90),
			onColorContainer: a.tone(10)
		},
		dark: {
			color: a.tone(80),
			onColor: a.tone(20),
			colorContainer: a.tone(30),
			onColorContainer: a.tone(90)
		}
	};
}
function Rs(e) {
	return e === "standard" || e === "expressive";
}
function zs(e) {
	return e === "monochrome" || e === "neutral" || e === "tonal-spot" || e === "vibrant" || e === "expressive" || e === "fidelity" || e === "content" || e === "rainbow" || e === "fruit-salad";
}
var Bs, Vs, Hs, Us, Ws, Gs, Ks, qs, Js, Ys;
function Xs(e) {
	let t = "";
	for (let n in e) {
		let r = e[n].toString();
		if (!r.startsWith("var(")) continue;
		let [i, a] = r.trim().slice(4, -1).split(/,(.+)/).map((e) => e.trim());
		i.startsWith("--") && a && (t += `${i}:${a};`);
	}
	return t;
}
function Zs(e, ...t) {
	let n = { ...e };
	for (let e of t) delete n[e];
	return n;
}
var Qs = class extends x {
	constructor() {
		super(...arguments), Bs.add(this), Vs.set(this, new CSSStyleSheet()), Hs.set(this, !1), Us.set(this, void 0), Ws.set(this, void 0), Gs.set(this, void 0), Ks.set(this, () => t(this, Bs, "m", qs).call(this, !0)), this.color = "#6750A4", this.variant = "tonal-spot", this.scheme = "auto", this.contrast = "standard", this.strongFocus = !1, this.density = 0, this.motion = "expressive";
	}
	get isDark() {
		switch (this.scheme) {
			case "light": return !1;
			case "dark": return !0;
			default:
				if (this.parentElement instanceof HTMLBodyElement) switch (document.documentElement.style.colorScheme) {
					case "light": return !1;
					case "dark": return !0;
				}
				return t(this, Ws, "f")?.matches ?? !1;
		}
	}
	connectedCallback() {
		super.connectedCallback(), this.parentElement instanceof HTMLBodyElement ? (this.shadowRoot?.adoptedStyleSheets.includes(t(this, Vs, "f")) && (this.shadowRoot.adoptedStyleSheets = this.shadowRoot.adoptedStyleSheets.filter((e) => e !== t(this, Vs, "f"))), document.adoptedStyleSheets.includes(t(this, Vs, "f")) || (document.adoptedStyleSheets = [...document.adoptedStyleSheets, t(this, Vs, "f")])) : (document.adoptedStyleSheets.includes(t(this, Vs, "f")) && (document.adoptedStyleSheets = document.adoptedStyleSheets.filter((e) => e !== t(this, Vs, "f"))), this.shadowRoot && !this.shadowRoot.adoptedStyleSheets.includes(t(this, Vs, "f")) && (this.shadowRoot.adoptedStyleSheets = [...this.shadowRoot.adoptedStyleSheets, t(this, Vs, "f")])), n(this, Us, matchMedia("(prefers-color-scheme: light)"), "f"), n(this, Ws, matchMedia("(prefers-color-scheme: dark)"), "f"), n(this, Gs, matchMedia("(forced-colors: active)"), "f"), [
			t(this, Us, "f"),
			t(this, Ws, "f"),
			t(this, Gs, "f")
		].forEach((e) => e.addEventListener("change", t(this, Ks, "f")));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), [
			t(this, Us, "f"),
			t(this, Ws, "f"),
			t(this, Gs, "f")
		].forEach((e) => e?.removeEventListener("change", t(this, Ks, "f"))), n(this, Us, n(this, Ws, n(this, Gs, void 0, "f"), "f"), "f"), document.adoptedStyleSheets.includes(t(this, Vs, "f")) && (document.adoptedStyleSheets = document.adoptedStyleSheets.filter((e) => e !== t(this, Vs, "f")));
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && !zs(this.variant) && (this.variant = "neutral"), e.has("scheme") && !Eo(this.scheme) && (this.scheme = "auto"), e.has("contrast") && !Do(this.contrast) && (this.contrast = "standard"), e.has("motion") && !Rs(this.motion) && (this.motion = "standard");
	}
	updated(e) {
		super.updated(e), t(this, Bs, "m", qs).call(this, t(this, Hs, "f") && [
			"color",
			"variant",
			"scheme",
			"contrast"
		].some((t) => e.has(t)));
	}
	firstUpdated(e) {
		super.firstUpdated(e), n(this, Hs, !0, "f");
	}
	render() {
		return y`<slot></slot>`;
	}
};
Vs = /* @__PURE__ */ new WeakMap(), Hs = /* @__PURE__ */ new WeakMap(), Us = /* @__PURE__ */ new WeakMap(), Ws = /* @__PURE__ */ new WeakMap(), Gs = /* @__PURE__ */ new WeakMap(), Ks = /* @__PURE__ */ new WeakMap(), Bs = /* @__PURE__ */ new WeakSet(), qs = function(e) {
	let n = Ps(this.color), r = new Ts({
		sourceColorHct: M.fromInt(n),
		variant: t(this, Bs, "m", Js).call(this),
		contrastLevel: t(this, Bs, "m", Ys).call(this),
		isDark: this.isDark,
		specVersion: "2025"
	}), i = "", a = new B(), o = [
		...a.allColors,
		a.surfaceVariant(),
		a.shadow(),
		a.scrim(),
		a.surfaceTint()
	];
	for (let e of o) {
		let t = `--md-sys-color-${e.name.replace(/_/g, "-").toLowerCase()}`, n = Ns(e.getArgb(r));
		i += `${t}: ${n};`;
	}
	this.motion === "expressive" ? (i += "--md-sys-motion-spring-fast-spatial: 350ms cubic-bezier(0.42, 1.67, 0.21, 0.90);", i += "--md-sys-motion-spring-default-spatial: 500ms cubic-bezier(0.38, 1.21, 0.22, 1.00);", i += "--md-sys-motion-spring-slow-spatial: 650ms cubic-bezier(0.39, 1.29, 0.35, 0.98);", i += "--md-sys-motion-spring-fast-effects: 150ms cubic-bezier(0.31, 0.94, 0.34, 1.00);", i += "--md-sys-motion-spring-default-effects: 200ms cubic-bezier(0.34, 0.80, 0.34, 1.00);", i += "--md-sys-motion-spring-slow-effects: 300ms cubic-bezier(0.34, 0.88, 0.34, 1.00);") : this.parentElement instanceof HTMLBodyElement || (i += Xs(A.motion.spring)), i += `--md-sys-density-scale: ${this.density};`, i += `--m3e-scrollbar-thumb-color: ${Ns(r.neutralPalette.tone(60))};`, i += `--m3e-focus-ring-visibility: ${this.strongFocus ? "visible" : "hidden"};`;
	let s = "";
	if (s += `font-size: ${A.typescale.standard.body.large.fontSize};`, s += `font-weight: ${A.typescale.standard.body.large.fontWeight};`, s += `line-height: ${A.typescale.standard.body.large.lineHeight};`, s += `letter-spacing: ${A.typescale.standard.body.large.tracking};`, this.parentElement instanceof HTMLBodyElement) switch (t(this, Vs, "f").replaceSync(`
        html { ${i} }
        body { ${s} }
        @media not (forced-colors: active) {
          body { 
            background-color: var(--md-sys-color-background);
            color: var(--md-sys-color-on-background);
            scrollbar-color: ${A.scrollbar.color};
          }
        }`), this.scheme) {
		case "light":
			document.documentElement.style.colorScheme = "light";
			break;
		case "dark":
			document.documentElement.style.colorScheme = "dark";
			break;
		default: switch (document.documentElement.style.colorScheme) {
			case "light":
			case "dark": document.documentElement.style.colorScheme = "light dark";
		}
	}
	else t(this, Vs, "f").replaceSync(`:host { ${i} }`);
	t(this, Hs, "f") && this.dispatchEvent(new Event("change", { bubbles: !0 })), e && document.body.offsetHeight;
}, Js = function() {
	switch (this.variant) {
		case "neutral": return 1;
		case "tonal-spot": return 2;
		case "vibrant": return 3;
		case "expressive": return 4;
		case "fidelity": return 5;
		case "content": return 6;
		case "rainbow": return 7;
		case "fruit-salad": return 8;
		default: return 0;
	}
}, Ys = function() {
	switch (this.contrast) {
		case "high": return 1;
		case "medium": return .5;
		default: return 0;
	}
}, (() => {
	if (typeof window < "u") {
		let e = "";
		e += Xs(A.typescale.standard.display.large), e += Xs(A.typescale.standard.display.medium), e += Xs(A.typescale.standard.display.small), e += Xs(A.typescale.standard.headline.large), e += Xs(A.typescale.standard.headline.medium), e += Xs(A.typescale.standard.headline.small), e += Xs(A.typescale.standard.title.large), e += Xs(A.typescale.standard.title.medium), e += Xs(A.typescale.standard.title.small), e += Xs(A.typescale.standard.label.large), e += Xs(A.typescale.standard.label.medium), e += Xs(A.typescale.standard.label.small), e += Xs(A.typescale.standard.body.large), e += Xs(A.typescale.standard.body.medium), e += Xs(A.typescale.standard.body.small), e += Xs(A.elevation), e += Xs(A.shape.corner.value), e += Xs(Zs(A.shape.corner, "value", "extraLargeEnd", "extraLargeStart", "mediumTop", "mediumEnd", "mediumStart", "smallTop", "smallEnd", "smallStart", "extraSmallEnd", "extraSmallStart", "extraSmallBottom")), e += Xs(A.motion.duration), e += Xs(A.motion.easing), e += Xs(A.motion.spring), e += Xs(Zs(A.density, "calc", "calcHalf")), e += Xs(A.measurement), e += Xs(A.state), Vt(c(`html{${e}}`));
	}
})(), Qs.styles = l`:host { display: contents; } :host([hidden]) { display: none; }`, e([S({ reflect: !1 })], Qs.prototype, "color", void 0), e([S({ reflect: !1 })], Qs.prototype, "variant", void 0), e([S({ reflect: !1 })], Qs.prototype, "scheme", void 0), e([S({ reflect: !1 })], Qs.prototype, "contrast", void 0), e([S({
	attribute: "strong-focus",
	type: Boolean,
	reflect: !1
})], Qs.prototype, "strongFocus", void 0), e([S({
	type: Number,
	reflect: !1
})], Qs.prototype, "density", void 0), e([S({ reflect: !1 })], Qs.prototype, "motion", void 0), Qs = e([w("m3e-theme")], Qs);
var $s, ec, tc, nc, rc, ic = class extends j(x, "img") {
	constructor() {
		super(...arguments), $s.add(this), ec.set(this, void 0), tc.set(this, void 0), nc.set(this, () => this.requestUpdate()), this.color = "#6750A4", this.variant = "neutral", this.scheme = "auto";
	}
	get isDark() {
		switch (this.scheme) {
			case "light": return !1;
			case "dark": return !0;
			default:
				if (this.parentElement instanceof HTMLBodyElement) switch (document.documentElement.style.colorScheme) {
					case "light": return !1;
					case "dark": return !0;
				}
				return t(this, tc, "f")?.matches ?? !1;
		}
	}
	connectedCallback() {
		super.connectedCallback(), n(this, ec, matchMedia("(prefers-color-scheme: light)"), "f"), n(this, tc, matchMedia("(prefers-color-scheme: dark)"), "f"), [t(this, ec, "f"), t(this, tc, "f")].forEach((e) => e.addEventListener("change", t(this, nc, "f")));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), [t(this, ec, "f"), t(this, tc, "f")].forEach((e) => e?.removeEventListener("change", t(this, nc, "f"))), n(this, ec, n(this, tc, void 0, "f"), "f");
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && !zs(this.variant) && (this.variant = "neutral"), e.has("scheme") && !Eo(this.scheme) && (this.scheme = "auto");
	}
	updated(e) {
		super.updated(e);
		let n = this.shadowRoot?.querySelector(".base"), r = Ps(this.color), i = Is(r), a = new Ts({
			sourceColorHct: M.fromInt(r),
			variant: t(this, $s, "m", rc).call(this),
			contrastLevel: 0,
			isDark: this.isDark,
			primaryPalette: i.palettes.primary,
			neutralPalette: i.palettes.neutral
		}), o = new B();
		n?.style.setProperty("--_theme-icon-container-color", Ns(o.surface().getArgb(a))), n?.style.setProperty("--_theme-icon-color", Ns(o.primary().getArgb(a)));
	}
	render() {
		return y`<div class="base"><svg viewBox="0 0 80 80" class="icon"><path class="container" d="M77.87 0C79.05 0 80 .95 80 2.13v75.74c0 1.17-.95 2.13-2.13 2.13H2.13C.96 80 0 79.04 0 77.87V2.13C0 .95.96 0 2.13 0h75.74z"/><path class="color" d="M54 40c3.32 0 6 2.69 6 6 0 1.2 0-1.2 0 0 0 3.31-2.68 6-6 6H26c-3.31 0-6-2.69-6-6 0-1.2 0 1.2 0 0 0-3.31 2.69-6 6-6h28z"/><path class="color" d="M0 0h80v17.24H0V0z"/></svg></div>`;
	}
};
ec = /* @__PURE__ */ new WeakMap(), tc = /* @__PURE__ */ new WeakMap(), nc = /* @__PURE__ */ new WeakMap(), $s = /* @__PURE__ */ new WeakSet(), rc = function() {
	switch (this.variant) {
		case "neutral": return 1;
		case "tonal-spot": return 2;
		case "vibrant": return 3;
		case "expressive": return 4;
		case "fidelity": return 5;
		case "content": return 6;
		case "rainbow": return 7;
		case "fruit-salad": return 8;
		default: return 0;
	}
}, ic.styles = l`:host { display: inline-block; width: var(--m3e-theme-icon-size, 24px); height: var(--m3e-theme-icon-size, 24px); } :host([hidden]) { display: none; } .base { width: 100%; height: 100%; border-radius: var(--m3e-theme-icon-shape, ${A.shape.corner.extraSmall}); outline: solid 1px color-mix( in srgb, var(--m3e-theme-icon-outline-color, ${A.color.outlineVariant}) var(--m3e-theme-icon-outline-opacity, 50%), transparent ); } .icon { width: 100%; height: 100%; } .container { fill: var(--m3e-theme-icon-container-color, var(--_theme-icon-container-color)); } .color { fill: var(--m3e-theme-icon-color, var(--_theme-icon-color)); }`, e([S()], ic.prototype, "color", void 0), e([S()], ic.prototype, "variant", void 0), e([S()], ic.prototype, "scheme", void 0), ic = e([w("m3e-theme-icon")], ic);
//#endregion
//#region node_modules/@m3e/web/dist/app-bar.js
function ac(e) {
	return e === "small" || e === "medium" || e === "large";
}
var oc = {
	small: {
		containerHeight: c("var(--m3e-app-bar-small-container-height, 64px)"),
		titleTextFontSize: c(`var(--m3e-app-bar-small-title-text-font-size, ${A.typescale.standard.title.large.fontSize})`),
		titleTextFontWeight: c(`var(--m3e-app-bar-small-title-text-font-weight, ${A.typescale.standard.title.large.fontWeight})`),
		titleTextLineHeight: c(`var(--m3e-app-bar-small-title-text-line-height, ${A.typescale.standard.title.large.lineHeight})`),
		titleTextTracking: c(`var(--m3e-app-bar-small-subtitle-text-tracking, ${A.typescale.standard.title.large.tracking})`),
		subtitleTextFontSize: c(`var(--m3e-app-bar-small-subtitle-text-font-size, ${A.typescale.standard.label.medium.fontSize})`),
		subtitleTextFontWeight: c(`var(--m3e-app-bar-small-subtitle-text-font-weight, ${A.typescale.standard.label.medium.fontWeight})`),
		subtitleTextLineHeight: c(`var(--m3e-app-bar-small-subtitle-text-line-height, ${A.typescale.standard.label.medium.lineHeight})`),
		subtitleTextTracking: c(`var(--m3e-app-bar-small-subtitle-text-tracking, ${A.typescale.standard.label.medium.tracking})`),
		headingPaddingLeft: c(`var(--m3e-app-bar-small-heading-padding-left, ${A.measurement.space50})`),
		headingPaddingRight: c(`var(--m3e-app-bar-small-heading-padding-right, ${A.measurement.space50})`)
	},
	medium: {
		containerHeight: c("var(--m3e-app-bar-medium-container-height, 112px)"),
		containerHeightWithSubtitle: c("var(--m3e-app-bar-medium-container-height-with-subtitle, 136px)"),
		titleTextFontSize: c(`var(--m3e-app-bar-medium-title-text-font-size, ${A.typescale.standard.headline.medium.fontSize})`),
		titleTextFontWeight: c(`var(--m3e-app-bar-medium-title-text-font-weight, ${A.typescale.standard.headline.medium.fontWeight})`),
		titleTextLineHeight: c(`var(--m3e-app-bar-medium-title-text-line-height, ${A.typescale.standard.headline.medium.lineHeight})`),
		titleTextTracking: c(`var(--m3e-app-bar-medium-subtitle-text-tracking, ${A.typescale.standard.headline.medium.tracking})`),
		subtitleTextFontSize: c(`var(--m3e-app-bar-medium-subtitle-text-font-size, ${A.typescale.standard.title.small.fontSize})`),
		subtitleTextFontWeight: c(`var(--m3e-app-bar-medium-subtitle-text-font-weight, ${A.typescale.standard.title.small.fontWeight})`),
		subtitleTextLineHeight: c(`var(--m3e-app-bar-medium-subtitle-text-line-height, ${A.typescale.standard.title.small.lineHeight})`),
		subtitleTextTracking: c(`var(--m3e-app-bar-medium-subtitle-text-tracking, ${A.typescale.standard.title.small.tracking})`),
		headingPaddingLeft: c(`var(--m3e-app-bar-medium-heading-padding-left, ${A.measurement.space200})`),
		headingPaddingRight: c(`var(--m3e-app-bar-medium-heading-padding-right, ${A.measurement.space50})`),
		paddingTop: c(`var(--m3e-app-bar-medium-padding-top, ${A.measurement.space100})`),
		paddingBottom: c(`var(--m3e-app-bar-medium-padding-bottom, ${A.measurement.space150})`),
		titleMaxLines: c("var(--m3e-app-bar-medium-title-max-lines, 2)"),
		subtitleMaxLines: c("var(--m3e-app-bar-medium-subtitle-max-lines, 1)")
	},
	large: {
		containerHeight: c("var(--m3e-app-bar-large-container-height, 120px)"),
		containerHeightWithSubtitle: c("var(--m3e-app-bar-large-container-height-with-subtitle, 152px)"),
		titleTextFontSize: c(`var(--m3e-app-bar-large-title-text-font-size, ${A.typescale.standard.display.small.fontSize})`),
		titleTextFontWeight: c(`var(--m3e-app-bar-large-title-text-font-weight, ${A.typescale.standard.display.small.fontWeight})`),
		titleTextLineHeight: c(`var(--m3e-app-bar-large-title-text-line-height, ${A.typescale.standard.display.small.lineHeight})`),
		titleTextTracking: c(`var(--m3e-app-bar-large-subtitle-text-tracking, ${A.typescale.standard.display.small.tracking})`),
		subtitleTextFontSize: c(`var(--m3e-app-bar-large-subtitle-text-font-size, ${A.typescale.standard.title.medium.fontSize})`),
		subtitleTextFontWeight: c(`var(--m3e-app-bar-large-subtitle-text-font-weight, ${A.typescale.standard.title.medium.fontWeight})`),
		subtitleTextLineHeight: c(`var(--m3e-app-bar-large-subtitle-text-line-height, ${A.typescale.standard.title.medium.lineHeight})`),
		subtitleTextTracking: c(`var(--m3e-app-bar-large-subtitle-text-tracking, ${A.typescale.standard.title.medium.tracking})`),
		headingPaddingLeft: c(`var(--m3e-app-bar-large-heading-padding-left, ${A.measurement.space200})`),
		headingPaddingRight: c(`var(--m3e-app-bar-large-heading-padding-right, ${A.measurement.space50})`),
		paddingTop: c(`var(--m3e-app-bar-large-padding-top, ${A.measurement.space100})`),
		paddingBottom: c(`var(--m3e-app-bar-large-padding-bottom, ${A.measurement.space150})`),
		titleMaxLines: c("var(--m3e-app-bar-large-title-max-lines, 2)"),
		subtitleMaxLines: c("var(--m3e-app-bar-large-subtitle-max-lines, 1)")
	}
};
function sc(e) {
	let t = c(`:is(:state(--${e}), :--${e})`);
	return l`:host(${t}) .base:not(.with-subtitle) { min-height: ${oc[e].containerHeight}; } :host(${t}) .base.with-subtitle { min-height: ${oc[e].containerHeightWithSubtitle ?? oc[e].containerHeight}; } :host(${t}) .title { font-size: ${oc[e].titleTextFontSize}; font-weight: ${oc[e].titleTextFontWeight}; line-height: ${oc[e].titleTextLineHeight}; letter-spacing: ${oc[e].titleTextTracking}; } :host(${t}) .subtitle { font-size: ${oc[e].subtitleTextFontSize}; font-weight: ${oc[e].subtitleTextFontWeight}; line-height: ${oc[e].subtitleTextLineHeight}; letter-spacing: ${oc[e].subtitleTextTracking}; } :host(:not([centered])${t}) .label { padding-inline-start: ${oc[e].headingPaddingLeft}; padding-inline-end: ${oc[e].headingPaddingRight}; } :host([centered]${t}) .label { padding-inline: ${oc[e].headingPaddingLeft}; } :host(${t}) .base { padding-block-start: ${oc[e].paddingTop ?? c("unset")}; padding-block-end: ${oc[e].paddingBottom ?? c("unset")}; } ${oc[e].titleMaxLines ? l`:host(${t}) .title { display: -webkit-box; -webkit-line-clamp: ${oc[e].titleMaxLines}; -webkit-box-orient: vertical; overflow: hidden; line-clamp: ${oc[e].titleMaxLines}; }` : l``} ${oc[e].subtitleMaxLines ? l`:host(${t}) .subtitle { display: -webkit-box; -webkit-line-clamp: ${oc[e].subtitleMaxLines}; -webkit-box-orient: vertical; overflow: hidden; line-clamp: ${oc[e].subtitleMaxLines}; }` : l``}`;
}
var cc = [
	sc("small"),
	sc("medium"),
	sc("large")
], lc = {
	containerColor: c(`var(--m3e-app-bar-container-color, ${A.color.surface})`),
	containerColorOnScroll: c(`var(--m3e-app-bar-container-color-on-scroll, ${A.color.surfaceContainer})`),
	containerElevation: c(`var(--m3e-app-bar-container-elevation, ${A.elevation.level0})`),
	containerElevationOnScroll: c(`var(--m3e-app-bar-container-elevation-on-scroll, ${A.elevation.level1})`),
	titleTextColor: c(`var(--m3e-app-bar-title-text-color, ${A.color.onSurface})`),
	subtitleTextColor: c(`var(--m3e-app-bar-subtitle-text-color, ${A.color.onSurfaceVariant})`),
	paddingLeft: c(`var(--m3e-app-bar-padding-left, ${A.measurement.space50})`),
	paddingRight: c(`var(--m3e-app-bar-padding-right, ${A.measurement.space50})`)
}, uc = l`:host { display: block; flex: none; } :host([hidden]) { display: none; } :host(:is(:state(--small), :--small)) .base, :host(:not(:is(:state(--small), :--small)):not([centered])) .heading { padding-inline-start: ${lc.paddingLeft}; padding-inline-end: ${lc.paddingRight}; } .base { box-sizing: border-box; display: flex; transition: ${c(`background-color ${A.motion.duration.medium1} ${A.motion.easing.standard},
      box-shadow ${A.motion.duration.medium1} ${A.motion.easing.standard}`)}; } .base:not(.on-scroll) { background-color: ${lc.containerColor}; box-shadow: ${lc.containerElevation}; } .base.on-scroll { background-color: ${lc.containerColorOnScroll}; box-shadow: ${lc.containerElevationOnScroll}; } .leading-icon, .trailing-icon { display: flex; flex: none; align-items: center; --_icon-button-size: auto; --_icon-button-min-size: 48px; } .leading-icon { min-width: var(--_leading-icon-min-width); } .trailing-icon { min-width: var(--_trailing-icon-min-width); } .heading { display: flex; align-items: center; } :host(:is(:state(--small), :--small)) .heading { flex: 1 1 auto; } .spacer { flex: 1 1 auto; } .label { display: flex; flex-direction: column; flex: none; } .title { color: ${lc.titleTextColor}; } .subtitle { color: ${lc.subtitleTextColor}; } .base:not(.with-title) .title, .base:not(.with-subtitle) .subtitle, .base:not(.with-title):not(.with-subtitle) .label, .base:not(.with-trailing-icon) .trailing-icon { display: none; } :host(:is(:state(--small), :--small)) .base { align-items: center; } :host(:is(:state(--small), :--small)) .heading { min-width: 0; } :host(:is(:state(--small), :--small)) .label { flex: 1 1 auto; min-width: 0; } :host(:is(:state(--small), :--small)) .title, :host(:is(:state(--small), :--small)) .subtitle { display: block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; } :host(:not(:is(:state(--small), :--small))) .base { flex-direction: column; } :host([centered]) .title, :host([centered]) .subtitle { text-align: center; } @media (prefers-reduced-motion) { .base { transition: none; } } @media (forced-colors: active) { .base { transition: none; } .base:not(.on-scroll), .base.on-scroll { background-color: Canvas; box-shadow: unset; } .title { color: CanvasText; } .subtitle { color: FieldText; } }`, dc, fc, pc, mc, hc, gc, _c, vc, yc, bc, xc, Sc, Cc, wc = class extends Ki(j(T(x), "banner")) {
	constructor() {
		super(...arguments), dc.add(this), fc.set(this, (e) => this._updateScroll(e)), pc.set(this, () => t(this, dc, "m", Sc).call(this)), this.centered = !1, this.size = "small";
	}
	attach(e) {
		super.attach(e), e instanceof HTMLIFrameElement ? (e.addEventListener("load", t(this, pc, "f")), t(this, dc, "m", Sc).call(this)) : e.addEventListener("scroll", t(this, fc, "f"), { passive: !0 });
	}
	detach() {
		this.control instanceof HTMLIFrameElement && (this.control.removeEventListener("load", t(this, pc, "f")), this.control.contentDocument?.removeEventListener("scroll", t(this, fc, "f"))), this.control && this.control.removeEventListener("scroll", t(this, fc, "f")), super.detach();
	}
	connectedCallback() {
		super.connectedCallback(), t(this, dc, "m", mc).call(this);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("size") && t(this, dc, "m", mc).call(this);
	}
	updated(e) {
		super.updated(e), (e.has("centered") || e.has("size")) && (this.centered && this.size === "small" ? t(this, dc, "m", xc).call(this) : t(this, dc, "m", bc).call(this));
	}
	render() {
		switch (this.size) {
			case "small": return y`<div class="base"><div class="leading-icon"><slot name="leading" @slotchange="${t(this, dc, "m", _c)}"><slot name="leading-icon" @slotchange="${t(this, dc, "m", yc)}"></slot></slot></div><div class="heading"><div class="label"><div class="title"><slot name="title" @slotchange="${t(this, dc, "m", hc)}"></slot></div><div class="subtitle"><slot name="subtitle" @slotchange="${t(this, dc, "m", gc)}"></slot></div></div></div><div class="trailing-icon"><slot name="trailing" @slotchange="${t(this, dc, "m", vc)}"><slot name="trailing-icon" @slotchange="${t(this, dc, "m", yc)}"></slot></slot></div></div>`;
			default: return y`<div class="base"><div class="heading"><div class="leading-icon"><slot name="leading" @slotchange="${t(this, dc, "m", _c)}"><slot name="leading-icon" @slotchange="${t(this, dc, "m", yc)}"></slot></slot></div><div class="spacer"></div><div class="trailing-icon"><slot name="trailing" @slotchange="${t(this, dc, "m", vc)}"><slot name="trailing-icon" @slotchange="${t(this, dc, "m", yc)}"></slot></slot></div></div><div class="spacer"></div><div class="label"><div class="title"><slot name="title" @slotchange="${t(this, dc, "m", hc)}"></slot></div><div class="subtitle"><slot name="subtitle" @slotchange="${t(this, dc, "m", gc)}"></slot></div></div></div>`;
		}
	}
	_updateScroll(e) {
		let n = 0;
		this.control instanceof HTMLIFrameElement ? n = t(this, dc, "m", Cc).call(this, this.control) : e.target instanceof HTMLElement && (n = e.target.scrollTop), this._base?.classList.toggle("on-scroll", n > 0);
	}
};
fc = /* @__PURE__ */ new WeakMap(), pc = /* @__PURE__ */ new WeakMap(), dc = /* @__PURE__ */ new WeakSet(), mc = function() {
	ac(this.size) || (this.size = "small"), Br(this, this.size, "small", "medium", "large");
}, hc = function(e) {
	this._base?.classList.toggle("with-title", Rt(e.target));
}, gc = function(e) {
	this._base?.classList.toggle("with-subtitle", Rt(e.target));
}, _c = function(e) {
	this._base?.classList.toggle("with-leading-icon", Rt(e.target)), this.centered && this.size === "small" && setTimeout(() => t(this, dc, "m", xc).call(this), 40);
}, vc = function(e) {
	this._base?.classList.toggle("with-trailing-icon", Rt(e.target)), this.centered && this.size === "small" && setTimeout(() => t(this, dc, "m", xc).call(this), 40);
}, yc = function(e) {
	let t = e.target.name, n = t.replace("-icon", "");
	console.warn(`[m3e-app-bar] Slot "${t}" is deprecated and will be removed in a future release. Use "${n}" instead.`);
}, bc = function() {
	this._base?.style.removeProperty("--_leading-icon-min-width"), this._base?.style.removeProperty("--_trailing-icon-min-width");
}, xc = function() {
	t(this, dc, "m", bc).call(this);
	let e = this._leadingIcon?.getBoundingClientRect().width ?? 0, n = this._trailingIcon?.getBoundingClientRect().width ?? 0;
	e < n ? this._base?.style.setProperty("--_leading-icon-min-width", `${n}px`) : e > n && this._base?.style.setProperty("--_trailing-icon-min-width", `${e}px`);
}, Sc = function() {
	this.control instanceof HTMLIFrameElement && (this.control.contentDocument?.addEventListener("scroll", t(this, fc, "f"), { passive: !0 }), this._base?.classList.toggle("on-scroll", t(this, dc, "m", Cc).call(this, this.control) > 0));
}, Cc = function(e) {
	return Math.max(e.contentDocument?.documentElement?.scrollTop ?? 0, e.contentDocument?.body?.scrollTop ?? 0);
}, wc.styles = [uc, cc], e([C(".base")], wc.prototype, "_base", void 0), e([C(".leading-icon")], wc.prototype, "_leadingIcon", void 0), e([C(".trailing-icon")], wc.prototype, "_trailingIcon", void 0), e([S({
	type: Boolean,
	reflect: !0
})], wc.prototype, "centered", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], wc.prototype, "size", void 0), e([lr(40)], wc.prototype, "_updateScroll", null), wc = e([w("m3e-app-bar")], wc);
//#endregion
//#region node_modules/@m3e/web/dist/button.js
function Tc(e) {
	return e === "rounded" || e === "square";
}
function Ec(e) {
	return e === "extra-small" || e === "small" || e === "medium" || e === "large" || e === "extra-large";
}
function Dc(e) {
	return e === "elevated" || e === "filled" || e === "tonal" || e === "outlined";
}
var Oc = {
	"extra-small": {
		containerHeight: c(`calc(var(--m3e-button-extra-small-container-height, var(--m3e-button-container-height, 32px)) + ${A.density.calc(-3)})`),
		outlineThickness: c("var(--m3e-button-extra-small-outline-thickness, var(--m3e-button-outline-thickness, 1px))"),
		labelTextFontSize: c(`var(--m3e-button-extra-small-label-text-font-size, var(--m3e-button-label-text-font-size, ${A.typescale.standard.label.large.fontSize}))`),
		labelTextFontWeight: c(`var(--m3e-button-extra-small-label-text-font-weight, var(--m3e-button-label-text-font-weight, ${A.typescale.standard.label.large.fontWeight}))`),
		labelTextLineHeight: c(`var(--m3e-button-extra-small-label-text-line-height, var(--m3e-button-label-text-line-height, ${A.typescale.standard.label.large.lineHeight}))`),
		labelTextTracking: c(`var(--m3e-button-extra-small-label-text-tracking, var(--m3e-button-label-text-tracking, ${A.typescale.standard.label.large.tracking}))`),
		iconSize: c("var(--m3e-button-extra-small-icon-size, var(--m3e-button-icon-size, 20px))"),
		shapeRound: c(`var(--m3e-button-extra-small-shape-round, var(--m3e-button-shape-round, ${A.shape.corner.full}))`),
		shapeSquare: c(`var(--m3e-button-extra-small-shape-square, var(--m3e-button-shape-square, ${A.shape.corner.medium}))`),
		selectedShapeRound: c(`var(--m3e-button-extra-small-selected-shape-round, var(--m3e-button-selected-shape-round, ${A.shape.corner.medium}))`),
		selectedShapeSquare: c(`var(--m3e-button-extra-small-selected-shape-square, var(--m3e-button-selected-shape-square, ${A.shape.corner.full}))`),
		shapePressedMorph: c(`var(--m3e-button-extra-small-shape-pressed-morph, var(--m3e-button-shape-pressed-morph, ${A.shape.corner.small}))`),
		leadingSpace: c(`var(--m3e-button-extra-small-leading-space, var(--m3e-button-leading-space, ${A.measurement.space150}))`),
		trailingSpace: c(`var(--m3e-button-extra-small-trailing-space, var(--m3e-button-trailing-space, ${A.measurement.space150}))`),
		iconLabelSpace: c(`var(--m3e-button-extra-small-icon-label-space, var(--m3e-button-icon-label-space, ${A.measurement.space100}))`)
	},
	small: {
		containerHeight: c(`calc(var(--m3e-button-small-container-height, var(--m3e-button-container-height, 40px)) + ${A.density.calc(-3)})`),
		outlineThickness: c("var(--m3e-button-small-outline-thickness, var(--m3e-button-outline-thickness, 1px))"),
		labelTextFontSize: c(`var(--m3e-button-small-label-text-font-size, var(--m3e-button-label-text-font-size, ${A.typescale.standard.label.large.fontSize}))`),
		labelTextFontWeight: c(`var(--m3e-button-small-label-text-font-weight, var(--m3e-button-label-text-font-weight, ${A.typescale.standard.label.large.fontWeight}))`),
		labelTextLineHeight: c(`var(--m3e-button-small-label-text-line-height, var(--m3e-button-label-text-line-height, ${A.typescale.standard.label.large.lineHeight}))`),
		labelTextTracking: c(`var(--m3e-button-small-label-text-tracking, var(--m3e-button-label-text-tracking, ${A.typescale.standard.label.large.tracking}))`),
		iconSize: c("var(--m3e-button-small-icon-size, var(--m3e-button-icon-size, 20px))"),
		shapeRound: c(`var(--m3e-button-small-shape-round, var(--m3e-button-shape-round, ${A.shape.corner.full}))`),
		shapeSquare: c(`var(--m3e-button-small-shape-square, var(--m3e-button-shape-square, ${A.shape.corner.medium}))`),
		selectedShapeRound: c(`var(--m3e-button-small-selected-shape-round, var(--m3e-button-selected-shape-round, ${A.shape.corner.medium}))`),
		selectedShapeSquare: c(`var(--m3e-button-small-selected-shape-square, var(--m3e-button-selected-shape-square, ${A.shape.corner.full}))`),
		shapePressedMorph: c(`var(--m3e-button-small-shape-pressed-morph, var(--m3e-button-shape-pressed-morph, ${A.shape.corner.small}))`),
		leadingSpace: c(`var(--m3e-button-small-leading-space, var(--m3e-button-leading-space, ${A.measurement.space250}))`),
		trailingSpace: c(`var(--m3e-button-small-trailing-space, var(--m3e-button-trailing-space, ${A.measurement.space250}))`),
		iconLabelSpace: c(`var(--m3e-button-small-icon-label-space, var(--m3e-button-icon-label-space, ${A.measurement.space100}))`)
	},
	medium: {
		containerHeight: c(`calc(var(--m3e-button-medium-container-height, var(--m3e-button-container-height, 56px)) + ${A.density.calc(-3)})`),
		outlineThickness: c("var(--m3e-button-medium-outline-thickness, var(--m3e-button-outline-thickness, 1px))"),
		labelTextFontSize: c(`var(--m3e-button-medium-label-text-font-size, var(--m3e-button-label-text-font-size, ${A.typescale.standard.body.large.fontSize}))`),
		labelTextFontWeight: c(`var(--m3e-button-medium-label-text-font-weight, var(--m3e-button-label-text-font-weight, ${A.typescale.standard.body.large.fontWeight}))`),
		labelTextLineHeight: c(`var(--m3e-button-medium-label-text-line-height, var(--m3e-button-label-text-line-height, ${A.typescale.standard.body.large.lineHeight}))`),
		labelTextTracking: c(`var(--m3e-button-medium-label-text-tracking, var(--m3e-button-label-text-tracking, ${A.typescale.standard.body.large.tracking}))`),
		iconSize: c("var(--m3e-button-medium-icon-size, var(--m3e-button-icon-size, 24px))"),
		shapeRound: c(`var(--m3e-button-medium-shape-round, var(--m3e-button-shape-round, ${A.shape.corner.full}))`),
		shapeSquare: c(`var(--m3e-button-medium-shape-square, var(--m3e-button-shape-square, ${A.shape.corner.large}))`),
		selectedShapeRound: c(`var(--m3e-button-medium-selected-shape-round, var(--m3e-button-selected-shape-round, ${A.shape.corner.large}))`),
		selectedShapeSquare: c(`var(--m3e-button-medium-selected-shape-square, var(--m3e-button-selected-shape-square, ${A.shape.corner.full}))`),
		shapePressedMorph: c(`var(--m3e-button-medium-shape-pressed-morph, var(--m3e-button-shape-pressed-morph, ${A.shape.corner.medium}))`),
		leadingSpace: c(`var(--m3e-button-medium-leading-space, var(--m3e-button-leading-space, ${A.measurement.space300}))`),
		trailingSpace: c(`var(--m3e-button-medium-trailing-space, var(--m3e-button-trailing-space, ${A.measurement.space300}))`),
		iconLabelSpace: c(`var(--m3e-button-medium-icon-label-space, var(--m3e-button-icon-label-space, ${A.measurement.space100}))`)
	},
	large: {
		containerHeight: c(`calc(var(--m3e-button-large-container-height, var(--m3e-button-container-height, 96px)) + ${A.density.calc(-3)})`),
		outlineThickness: c("var(--m3e-button-large-outline-thickness, var(--m3e-button-outline-thickness, 2px))"),
		labelTextFontSize: c(`var(--m3e-button-large-label-text-font-size, var(--m3e-button-label-text-font-size, ${A.typescale.standard.headline.small.fontSize}))`),
		labelTextFontWeight: c(`var(--m3e-button-large-label-text-font-weight, var(--m3e-button-label-text-font-weight, ${A.typescale.standard.headline.small.fontWeight}))`),
		labelTextLineHeight: c(`var(--m3e-button-large-label-text-line-height, var(--m3e-button-label-text-line-height, ${A.typescale.standard.headline.small.lineHeight}))`),
		labelTextTracking: c(`var(--m3e-button-large-label-text-tracking, var(--m3e-button-label-text-tracking, ${A.typescale.standard.headline.small.tracking}))`),
		iconSize: c("var(--m3e-button-large-icon-size, var(--m3e-button-icon-size, 32px))"),
		shapeRound: c(`var(--m3e-button-large-shape-round, var(--m3e-button-shape-round, ${A.shape.corner.full}))`),
		shapeSquare: c(`var(--m3e-button-large-shape-square, var(--m3e-button-shape-square, ${A.shape.corner.extraLarge}))`),
		selectedShapeRound: c(`var(--m3e-button-large-selected-shape-round, var(--m3e-button-selected-shape-round, ${A.shape.corner.extraLarge}))`),
		selectedShapeSquare: c(`var(--m3e-button-large-selected-shape-square, var(--m3e-button-selected-shape-square, ${A.shape.corner.full}))`),
		shapePressedMorph: c(`var(--m3e-button-large-shape-pressed-morph, var(--m3e-button-shape-pressed-morph, ${A.shape.corner.large}))`),
		leadingSpace: c(`var(--m3e-button-large-leading-space, var(--m3e-button-leading-space, ${A.measurement.space600}))`),
		trailingSpace: c(`var(--m3e-button-large-trailing-space, var(--m3e-button-trailing-space, ${A.measurement.space600}))`),
		iconLabelSpace: c(`var(--m3e-button-large-icon-label-space, var(--m3e-button-icon-label-space, ${A.measurement.space150}))`)
	},
	"extra-large": {
		containerHeight: c(`calc(var(--m3e-button-extra-large-container-height, var(--m3e-button-container-height, 136px)) + ${A.density.calc(-3)})`),
		outlineThickness: c("var(--m3e-button-extra-large-outline-thickness, var(--m3e-button-outline-thickness, 3px))"),
		labelTextFontSize: c(`var(--m3e-button-extra-large-label-text-font-size, var(--m3e-button-label-text-font-size, ${A.typescale.standard.headline.large.fontSize}))`),
		labelTextFontWeight: c(`var(--m3e-button-extra-large-label-text-font-weight, var(--m3e-button-label-text-font-weight, ${A.typescale.standard.headline.large.fontWeight}))`),
		labelTextLineHeight: c(`var(--m3e-button-extra-large-label-text-line-height, var(--m3e-button-label-text-line-height, ${A.typescale.standard.headline.large.lineHeight}))`),
		labelTextTracking: c(`var(--m3e-button-extra-large-label-text-tracking, var(--m3e-button-label-text-tracking, ${A.typescale.standard.headline.large.tracking}))`),
		iconSize: c("var(--m3e-button-extra-large-icon-size, var(--m3e-button-icon-size, 40px))"),
		shapeRound: c(`var(--m3e-button-extra-large-shape-round, var(--m3e-button-shape-round, ${A.shape.corner.full}))`),
		shapeSquare: c(`var(--m3e-button-extra-large-shape-square, var(--m3e-button-shape-square, ${A.shape.corner.extraLarge}))`),
		selectedShapeRound: c(`var(--m3e-button-extra-large-selected-shape-round, var(--m3e-button-selected-shape-round, ${A.shape.corner.extraLarge}))`),
		selectedShapeSquare: c(`var(--m3e-button-extra-large-selected-shape-square, var(--m3e-button-selected-shape-square, ${A.shape.corner.full}))`),
		shapePressedMorph: c(`var(--m3e-button-extra-large-shape-pressed-morph, var(--m3e-button-shape-pressed-morph, ${A.shape.corner.large}))`),
		leadingSpace: c(`var(--m3e-button-extra-large-leading-space, var(--m3e-button-leading-space, ${A.measurement.space800}))`),
		trailingSpace: c(`var(--m3e-button-extra-large-trailing-space, var(--m3e-button-trailing-space, ${A.measurement.space800}))`),
		iconLabelSpace: c(`var(--m3e-button-extra-large-icon-label-space, var(--m3e-button-icon-label-space, ${A.measurement.space200}))`)
	}
};
function kc(e) {
	let t = c(`:is(:state(--${e}), :--${e})`);
	return l`:host(${t}) .base { height: ${Oc[e].containerHeight}; } :host(${t}) .wrapper { padding-inline-start: calc(${Oc[e].leadingSpace} - calc(var(--_adjacent-shrink, 0px) / 2)); padding-inline-end: calc(${Oc[e].trailingSpace} - calc(var(--_adjacent-shrink, 0px) / 2)); column-gap: ${Oc[e].iconLabelSpace}; } :host(${t}) .label { font-size: ${Oc[e].labelTextFontSize}; font-weight: ${Oc[e].labelTextFontWeight}; line-height: ${Oc[e].labelTextLineHeight}; letter-spacing: ${Oc[e].labelTextTracking}; } :host(${t}) .icon { font-size: ${Oc[e].iconSize}; --m3e-icon-size: ${Oc[e].iconSize}; } :host(${t}) .base { outline-offset: calc(0px - ${Oc[e].outlineThickness}); outline-width: ${Oc[e].outlineThickness}; } :host(:not(:is(:state(--connected), :--connected))${t}:is(:state(--rounded), :--rounded)) .base { border-radius: var(--_button-shape, ${Oc[e].shapeRound}); } :host( :is(:state(--connected), :--connected)${t}:is(:state(--rounded), :--rounded)[toggle][selected]:not( :is(:state(--pressed), :--pressed) ) ) .base { border-radius: var(--_button-shape, ${Oc[e].shapeRound}); } :host( :not(:is(:state(--connected), :--connected))${t}:is(:state(--rounded), :--rounded)[toggle][selected]:not( :is(:state(--pressed), :--pressed) ) ) .base { border-radius: ${Oc[e].selectedShapeRound}; } :host(:not(:is(:state(--connected), :--connected))${t}:is(:state(--square), :--square)) .base { border-radius: ${Oc[e].shapeSquare}; } :host( :not(:is(:state(--connected), :--connected))${t}:is(:state(--square), :--square)[toggle][selected]:not( :is(:state(--pressed), :--pressed) ) ) .base { border-radius: var(--_button-shape, ${Oc[e].selectedShapeSquare}); } :host(:not(:is(:state(--connected), :--connected))${t}:is(:state(--pressed), :--pressed)) .base { border-radius: ${Oc[e].shapePressedMorph}; } :host(:is(:state(--connected), :--connected)${t}:is(:state(--rounded), :--rounded):not([toggle][selected])) .base { border-start-start-radius: var( --_button-rounded-start-shape, var(--_button-shape, ${Oc[e].shapeRound}) ); border-end-start-radius: var( --_button-rounded-start-shape, var(--_button-shape, ${Oc[e].shapeRound}) ); border-start-end-radius: var( --_button-rounded-end-shape, var(--_button-shape, ${Oc[e].shapeRound}) ); border-end-end-radius: var( --_button-rounded-end-shape, var(--_button-shape, ${Oc[e].shapeRound}) ); } :host(:is(:state(--connected), :--connected)${t}:is(:state(--square), :--square)) .base { border-start-start-radius: var(--_button-square-start-shape, ${Oc[e].shapeSquare}); border-end-start-radius: var(--_button-square-start-shape, ${Oc[e].shapeSquare}); border-start-end-radius: var(--_button-square-end-shape, ${Oc[e].shapeSquare}); border-end-end-radius: var(--_button-square-end-shape, ${Oc[e].shapeSquare}); } :host( :is(:state(--connected), :--connected)${t}:is(:state(--square), :--square)[toggle][selected]:not( :is(:state(--pressed), :--pressed) ) ) .base { border-radius: var(--_button-shape, ${Oc[e].selectedShapeSquare}); } :host(:is(:state(--connected), :--connected)${t}:is(:state(--pressed), :--pressed)) .base { border-start-start-radius: var(--_button-start-shape-pressed-morph, ${Oc[e].shapePressedMorph}); border-end-start-radius: var(--_button-start-shape-pressed-morph, ${Oc[e].shapePressedMorph}); border-start-end-radius: var(--_button-end-shape-pressed-morph, ${Oc[e].shapePressedMorph}); border-end-end-radius: var(--_button-end-shape-pressed-morph, ${Oc[e].shapePressedMorph}); }`;
}
var Ac = [
	kc("extra-small"),
	kc("small"),
	kc("medium"),
	kc("large"),
	kc("extra-large")
], jc = l`:host { display: inline-block; outline: none; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } :host([hidden]) { display: none; } .base { box-sizing: border-box; vertical-align: middle; display: inline-flex; align-items: center; justify-content: center; position: relative; width: 100%; transition: ${c(`background-color ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; } .touch { position: absolute; height: 48px; left: 0; right: 0; } :host(:is(:state(--pressed), :--pressed)) .base, :host(:is(:state(--resting), :--resting)) .base { transition: ${c(`background-color ${A.motion.duration.short4} ${A.motion.easing.standard},
          border-radius ${A.motion.spring.fastEffects}`)}; } .wrapper { box-sizing: border-box; width: 100%; overflow: hidden; display: inline-flex; align-items: center; justify-content: center; } .label { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; transition: ${c(`color ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; } .icon { transition: ${c(`color ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; } :host(:not(:disabled):not([disabled-interactive])) { cursor: pointer; } :host([disabled-interactive]) { cursor: not-allowed; } ::slotted([slot="icon"]), ::slotted([slot="selected-icon"]), ::slotted([slot="trailing-icon"]) { font-size: inherit !important; flex: none; } ::slotted(svg[slot="icon"]), ::slotted(svg[slot="selected-icon"]), ::slotted(svg[slot="trailing-icon"]) { width: 1em; height: 1em; } :host([toggle]:not([selected])) .base.with-selected-icon slot[name="selected-icon"], :host([toggle][selected]) .base.with-selected-icon slot[name="icon"] { display: none; } a { all: unset; display: block; position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px; z-index: 1; } :host(:is(:state(--grouped), :--grouped):is(:state(--connected), :--connected)) { flex: 1 1 auto; } :host(:is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected))) { transition: ${c(`width ${A.motion.spring.fastEffects}`)}; } :host(:is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected))) .wrapper { transition: ${c(`padding-inline ${A.motion.spring.fastEffects}`)}; } :host(:is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected))) { flex-shrink: 0; flex-grow: 0; } :host( :is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected)):not( :is(:state(--pressed), :--pressed, :state(--adjacent-pressed), :--adjacent-pressed) ) ) { width: var(--_button-width); } :host( :is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected)):not( :is(:state(--pressed), :--pressed) ):is(:state(--adjacent-pressed), :--adjacent-pressed) ) { width: calc(var(--_button-width) - var(--_adjacent-shrink, 0px)); } :host( :is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected)):is( :state(--pressed), :--pressed ):not([disabled-interactive]):not(:disabled) ) { width: calc( var(--_button-width) + calc(var(--_button-width) * var(--m3e-standard-button-group-width-multiplier, 0.15)) ); } :host( :is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected)):is( :state(--adjacent-pressed), :--adjacent-pressed ):not(:is(:state(--pressed), :--pressed)) ) .label { text-overflow: clip; } @media (forced-colors: active) { .base, .label, .icon { transition: none; } :host(:is(:state(--pressed), :--pressed)) .base, :host(:is(:state(--resting), :--resting)) .base { transition: ${c(`border-radius ${A.motion.spring.fastEffects}`)}; } :host(:not(:disabled):not([disabled-interactive]):not([toggle])) .base { background-color: ButtonFace !important; outline-color: ButtonText !important; } :host(:not(:disabled):not([disabled-interactive]):not([toggle])) .label, :host(:not(:disabled):not([disabled-interactive]):not([toggle])) .icon { color: ButtonText !important; } :host(:not(:disabled):not([disabled-interactive])[toggle]:not([selected])) .base { background-color: ButtonFace !important; outline-color: ButtonText !important; } :host(:not(:disabled):not([disabled-interactive])[toggle]:not([selected])) .label, :host(:hover:not(:disabled):not([disabled-interactive])[toggle]:not([selected])) .label, :host(:not(:disabled):not([disabled-interactive])[toggle]:not([selected]):focus) .label, :host(:not(:disabled):not([disabled-interactive])[toggle]:not([selected])) .icon, :host(:hover:not(:disabled):not([disabled-interactive])[toggle]:not([selected])) .icon, :host(:not(:disabled):not([disabled-interactive])[toggle]:not([selected]):focus) .icon { color: ButtonText !important; } :host(:not(:disabled):not([disabled-interactive])[toggle][selected]) .base { background-color: ButtonText !important; outline: none !important; } :host(:not(:disabled):not([disabled-interactive])[toggle][selected]) .label, :host(:hover:not(:disabled):not([disabled-interactive])[toggle][selected]) .label, :host(:not(:disabled):not([disabled-interactive])[toggle][selected]:focus) .label, :host(:not(:disabled):not([disabled-interactive])[toggle][selected]) .icon, :host(:hover:not(:disabled):not([disabled-interactive])[toggle][selected]) .icon, :host(:not(:disabled):not([disabled-interactive])[toggle][selected]:focus) .icon { forced-color-adjust: none; color: ButtonFace !important; background-color: ButtonText !important; } :host(:disabled) .base, :host([disabled-interactive]) .base { outline-color: GrayText !important; background-color: unset !important; } :host(:disabled) .label, :host([disabled-interactive]) .label, :host(:disabled) .icon, :host([disabled-interactive]) .icon { color: GrayText !important; } .base { outline-style: solid; } :host(:is(:state(--extra-small), :--extra-small)) .base { outline-offset: calc(0px - var(--m3e-button-extra-small-outline-thickness, 1px)); outline-width: var(--m3e-button-extra-small-outline-thickness, 1px); } :host(:is(:state(--small), :--small)) .base { outline-offset: calc(0px - var(--m3e-button-small-outline-thickness, 1px)); outline-width: var(--m3e-button-small-outline-thickness, 1px); } :host(:is(:state(--medium), :--medium)) .base { outline-offset: calc(0px - var(--m3e-button-medium-outline-thickness, 1px)); outline-width: var(--m3e-button-medium-outline-thickness, 1px); } :host(:is(:state(--large), :--large)) .base { outline-offset: calc(0px - var(--m3e-button-large-outline-thickness, 2px)); outline-width: var(--m3e-button-large-outline-thickness, 2px); } :host(:is(:state(--extra-large), :--extra-large)) .base { outline-offset: calc(0px - var(--m3e-button-extra-large-outline-thickness, 3px)); outline-width: var(--m3e-button-extra-large-outline-thickness, 3px); } } @media (prefers-reduced-motion) { :host(:is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected))), :host(:is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected))) .wrapper, :host(:is(:state(--pressed), :--pressed)) .base, :host(:is(:state(--resting), :--resting)) .base, .base, .label, .icon { transition: none; } }`, V = {
	elevated: {
		labelTextColor: c(`var(--m3e-elevated-button-label-text-color, var(--m3e-button-label-text-color, ${A.color.primary}))`),
		iconColor: c(`var(--m3e-elevated-button-icon-color, var(--m3e-button-icon-color, ${A.color.primary}))`),
		containerColor: c(`var(--m3e-elevated-button-container-color, var(--m3e-button-container-color, ${A.color.surfaceContainerLow}))`),
		containerElevation: c(`var(--m3e-elevated-button-container-elevation, var(--m3e-button-container-elevation, ${A.elevation.level1}))`),
		unselectedLabelTextColor: c(`var(--m3e-elevated-button-unselected-label-text-color, var(--m3e-button-unselected-label-text-color, ${A.color.primary}))`),
		unselectedIconColor: c(`var(--m3e-elevated-button-unselected-icon-color, var(--m3e-button-unselected-icon-color, ${A.color.primary}))`),
		unselectedContainerColor: c(`var(--m3e-elevated-button-unselected-container-color, var(--m3e-button-unselected-container-color, ${A.color.surfaceContainerLow}))`),
		selectedLabelTextColor: c(`var(--m3e-elevated-button-selected-label-text-color, var(--m3e-button-selected-label-text-color, ${A.color.onPrimary}))`),
		selectedIconColor: c(`var(--m3e-elevated-button-selected-icon-color, var(--m3e-button-selected-icon-color, ${A.color.onPrimary}))`),
		selectedContainerColor: c(`var(--m3e-elevated-button-selected-container-color, var(--m3e-button-selected-container-color, ${A.color.primary}))`),
		disabled: {
			containerColor: c(`var(--m3e-elevated-button-disabled-container-color, var(--m3e-button-disabled-container-color, ${A.color.onSurface}))`),
			containerOpacity: c("var(--m3e-elevated-button-disabled-container-opacity, var(--m3e-button-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-elevated-button-disabled-icon-color, var(--m3e-button-disabled-icon-color, ${A.color.onSurface}))`),
			iconOpacity: c("var(--m3e-elevated-button-disabled-icon-opacity, var(--m3e-button-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-elevated-button-disabled-label-text-color, var(--m3e-button-disabled-label-text-color, ${A.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-elevated-button-disabled-label-text-opacity, var(--m3e-button-disabled-label-text-opacity, 38%))"),
			containerElevation: c(`var(--m3e-elevated-button-disabled-container-elevation, var(--m3e-button-disabled-container-elevation, ${A.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-elevated-button-hover-icon-color, var(--m3e-button-hover-icon-color, ${A.color.primary}))`),
			labelTextColor: c(`var(--m3e-elevated-button-hover-label-text-color, var(--m3e-button-hover-label-text-color, ${A.color.primary}))`),
			stateLayerColor: c(`var(--m3e-elevated-button-hover-state-layer-color, var(--m3e-button-hover-state-layer-color, ${A.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-elevated-button-hover-state-layer-opacity, var(--m3e-button-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-elevated-button-hover-container-elevation, var(--m3e-button-hover-container-elevation, ${A.elevation.level2}))`),
			unselectedIconColor: c(`var(--m3e-elevated-button-hover-unselected-icon-color, var(--m3e-button-hover-unselected-icon-color, ${A.color.primary}))`),
			unselectedLabelTextColor: c(`var(--m3e-elevated-button-hover-unselected-label-text-color, var(--m3e-button-hover-unselected-label-text-color, ${A.color.primary}))`),
			unselectedStateLayerColor: c(`var(--m3e-elevated-button-hover-unselected-state-layer-color, var(--m3e-button-hover-unselected-state-layer-color, ${A.color.primary}))`),
			selectedIconColor: c(`var(--m3e-elevated-button-hover-selected-icon-color, var(--m3e-button-hover-selected-icon-color, ${A.color.onPrimary}))`),
			selectedLabelTextColor: c(`var(--m3e-elevated-button-hover-selected-label-text-color, var(--m3e-button-hover-selected-label-text-color, ${A.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-elevated-button-hover-selected-state-layer-color, var(--m3e-button-hover-selected-state-layer-color, ${A.color.onPrimary}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-elevated-button-focus-icon-color, var(--m3e-button-focus-icon-color, ${A.color.primary}))`),
			labelTextColor: c(`var(--m3e-elevated-button-focus-label-text-color, var(--m3e-button-focus-label-text-color, ${A.color.primary}))`),
			stateLayerColor: c(`var(--m3e-elevated-button-focus-state-layer-color, var(--m3e-button-focus-state-layer-color, ${A.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-elevated-button-focus-state-layer-opacity, var(--m3e-button-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-elevated-button-focus-container-elevation, var(--m3e-button-focus-container-elevation, ${A.elevation.level1}))`),
			unselectedLabelTextColor: c(`var(--m3e-elevated-button-focus-unselected-label-text-color, var(--m3e-button-focus-unselected-label-text-color, ${A.color.primary}))`),
			unselectedIconColor: c(`var(--m3e-elevated-button-focus-unselected-icon-color, var(--m3e-button-focus-unselected-icon-color, ${A.color.primary}))`),
			unselectedStateLayerColor: c(`var(--m3e-elevated-button-focus-unselected-state-layer-color, var(--m3e-button-focus-unselected-state-layer-color, ${A.color.primary}))`),
			selectedIconColor: c(`var(--m3e-elevated-button-focus-selected-icon-color, var(--m3e-button-focus-selected-icon-color, ${A.color.onPrimary}))`),
			selectedLabelTextColor: c(`var(--m3e-elevated-button-focus-selected-label-text-color, var(--m3e-button-focus-selected-label-text-color, ${A.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-elevated-button-focus-selected-state-layer-color, var(--m3e-button-focus-selected-state-layer-color, ${A.color.onPrimary}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-elevated-button-pressed-icon-color, var(--m3e-button-pressed-icon-color, ${A.color.primary}))`),
			labelTextColor: c(`var(--m3e-elevated-button-pressed-label-text-color, var(--m3e-button-pressed-label-text-color, ${A.color.primary}))`),
			stateLayerColor: c(`var(--m3e-elevated-button-pressed-state-layer-color, var(--m3e-button-pressed-state-layer-color, ${A.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-elevated-button-pressed-state-layer-opacity, var(--m3e-button-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-elevated-button-pressed-container-elevation, var(--m3e-button-pressed-container-elevation, ${A.elevation.level1}))`),
			unselectedLabelTextColor: c(`var(--m3e-elevated-button-pressed-unselected-label-text-color, var(--m3e-button-pressed-unselected-label-text-color, ${A.color.primary}))`),
			unselectedIconColor: c(`var(--m3e-elevated-button-pressed-unselected-icon-color, var(--m3e-button-pressed-unselected-icon-color, ${A.color.primary}))`),
			unselectedStateLayerColor: c(`var(--m3e-elevated-button-pressed-unselected-state-layer-color, var(--m3e-button-pressed-unselected-state-layer-color, ${A.color.primary}))`),
			selectedIconColor: c(`var(--m3e-elevated-button-pressed-selected-icon-color, var(--m3e-button-pressed-selected-icon-color, ${A.color.onPrimary}))`),
			selectedLabelTextColor: c(`var(--m3e-elevated-button-pressed-selected-label-text-color, var(--m3e-button-pressed-selected-label-text-color, ${A.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-elevated-button-pressed-selected-state-layer-color, var(--m3e-button-pressed-selected-state-layer-color, ${A.color.onPrimary}))`)
		}
	},
	outlined: {
		labelTextColor: c(`var(--m3e-outlined-button-label-text-color, var(--m3e-button-label-text-color, ${A.color.onSurfaceVariant}))`),
		iconColor: c(`var(--m3e-outlined-button-icon-color, var(--m3e-button-icon-color, ${A.color.onSurfaceVariant}))`),
		outlineColor: c(`var(--m3e-outlined-button-outline-color, var(--m3e-button-outline-color, ${A.color.outlineVariant}))`),
		unselectedLabelTextColor: c(`var(--m3e-outlined-button-unselected-label-text-color, var(--m3e-button-unselected-label-text-color, ${A.color.onSurfaceVariant}))`),
		unselectedIconColor: c(`var(--m3e-outlined-button-unselected-icon-color, var(--m3e-button-unselected-icon-color, ${A.color.onSurfaceVariant}))`),
		selectedLabelTextColor: c(`var(--m3e-outlined-button-selected-label-text-color, var(--m3e-button-selected-label-text-color, ${A.color.inverseOnSurface}))`),
		selectedIconColor: c(`var(--m3e-outlined-button-selected-icon-color, var(--m3e-button-selected-icon-color, ${A.color.inverseOnSurface}))`),
		selectedContainerColor: c(`var(--m3e-outlined-button-selected-container-color, var(--m3e-button-selected-container-color, ${A.color.inverseSurface}))`),
		disabled: {
			containerColor: c(`var(--m3e-outlined-button-disabled-container-color, var(--m3e-button-disabled-container-color, ${A.color.onSurface}))`),
			containerOpacity: c("var(--m3e-outlined-button-disabled-container-opacity, var(--m3e-button-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-outlined-button-disabled-icon-color, var(--m3e-button-disabled-icon-color, ${A.color.onSurface}))`),
			iconOpacity: c("var(--m3e-outlined-button-disabled-icon-opacity, var(--m3e-button-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-outlined-button-disabled-label-text-color, var(--m3e-button-disabled-label-text-color, ${A.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-outlined-button-disabled-label-text-opacity, var(--m3e-button-disabled-label-text-opacity, 38%))"),
			outlineColor: c(`var(--m3e-outlined-button-disabled-outline-color, var(--m3e-button-disabled-outline-color, ${A.color.outlineVariant}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-outlined-button-hover-icon-color, var(--m3e-button-hover-icon-color, ${A.color.onSurfaceVariant}))`),
			labelTextColor: c(`var(--m3e-outlined-button-hover-label-text-color, var(--m3e-button-hover-label-text-color, ${A.color.onSurfaceVariant}))`),
			outlineColor: c(`var(--m3e-outlined-button-hover-outline-color, var(--m3e-button-hover-outline-color, ${A.color.outlineVariant}))`),
			stateLayerColor: c(`var(--m3e-outlined-button-hover-state-layer-color, var(--m3e-button-hover-state-layer-color, ${A.color.onSurfaceVariant}))`),
			stateLayerOpacity: c(`var(--m3e-outlined-button-hover-state-layer-opacity, var(--m3e-button-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-outlined-button-hover-unselected-icon-color, var(--m3e-button-hover-unselected-icon-color, ${A.color.onSurfaceVariant}))`),
			unselectedLabelTextColor: c(`var(--m3e-outlined-button-hover-unselected-label-text-color, var(--m3e-button-hover-unselected-label-text-color, ${A.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-outlined-button-hover-unselected-state-layer-color, var(--m3e-button-hover-unselected-state-layer-color, ${A.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-outlined-button-hover-selected-icon-color, var(--m3e-button-hover-selected-icon-color, ${A.color.inverseOnSurface}))`),
			selectedLabelTextColor: c(`var(--m3e-outlined-button-hover-selected-label-text-color, var(--m3e-button-hover-selected-label-text-color, ${A.color.inverseOnSurface}))`),
			selectedStateLayerColor: c(`var(--m3e-outlined-button-hover-selected-state-layer-color, var(--m3e-button-hover-selected-state-layer-color, ${A.color.inverseOnSurface}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-outlined-button-focus-icon-color, var(--m3e-button-focus-icon-color, ${A.color.onSurfaceVariant}))`),
			labelTextColor: c(`var(--m3e-outlined-button-focus-label-text-color, var(--m3e-button-focus-label-text-color, ${A.color.onSurfaceVariant}))`),
			outlineColor: c(`var(--m3e-outlined-button-focus-outline-color, var(--m3e-button-focus-outline-color, ${A.color.outlineVariant}))`),
			stateLayerColor: c(`var(--m3e-outlined-button-focus-state-layer-color, var(--m3e-button-focus-state-layer-color, ${A.color.onSurfaceVariant}))`),
			stateLayerOpacity: c(`var(--m3e-outlined-button-focus-state-layer-opacity, var(--m3e-button-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-outlined-button-focus-unselected-icon-color, var(--m3e-button-focus-unselected-icon-color, ${A.color.onSurfaceVariant}))`),
			unselectedLabelTextColor: c(`var(--m3e-outlined-button-focus-unselected-label-text-color, var(--m3e-button-focus-unselected-label-text-color, ${A.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-outlined-button-focus-unselected-state-layer-color, var(--m3e-button-focus-unselected-state-layer-color, ${A.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-outlined-button-focus-selected-icon-color, var(--m3e-button-focus-selected-icon-color, ${A.color.inverseOnSurface}))`),
			selectedLabelTextColor: c(`var(--m3e-outlined-button-focus-selected-label-text-color, var(--m3e-button-focus-selected-label-text-color, ${A.color.inverseOnSurface}))`),
			selectedStateLayerColor: c(`var(--m3e-outlined-button-focus-selected-state-layer-color, var(--m3e-button-focus-selected-state-layer-color, ${A.color.inverseOnSurface}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-outlined-button-pressed-icon-color, var(--m3e-button-pressed-icon-color, ${A.color.onSurfaceVariant}))`),
			labelTextColor: c(`var(--m3e-outlined-button-pressed-label-text-color, var(--m3e-button-pressed-label-text-color, ${A.color.onSurfaceVariant}))`),
			outlineColor: c(`var(--m3e-outlined-button-pressed-outline-color, var(--m3e-button-pressed-outline-color, ${A.color.outlineVariant}))`),
			stateLayerColor: c(`var(--m3e-outlined-button-pressed-state-layer-color, var(--m3e-button-pressed-state-layer-color, ${A.color.onSurfaceVariant}))`),
			stateLayerOpacity: c(`var(--m3e-outlined-button-pressed-state-layer-opacity, var(--m3e-button-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-outlined-button-pressed-unselected-icon-color, var(--m3e-button-pressed-unselected-icon-color, ${A.color.onSurfaceVariant}))`),
			unselectedLabelTextColor: c(`var(--m3e-outlined-button-pressed-unselected-label-text-color, var(--m3e-button-pressed-unselected-label-text-color, ${A.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-outlined-button-pressed-unselected-state-layer-color, var(--m3e-button-pressed-unselected-state-layer-color, ${A.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-outlined-button-pressed-selected-icon-color, var(--m3e-button-pressed-selected-icon-color, ${A.color.inverseOnSurface}))`),
			selectedLabelTextColor: c(`var(--m3e-outlined-button-pressed-selected-label-text-color, var(--m3e-button-pressed-selected-label-text-color, ${A.color.inverseOnSurface}))`),
			selectedStateLayerColor: c(`var(--m3e-outlined-button-pressed-selected-state-layer-color, var(--m3e-button-pressed-selected-state-layer-color, ${A.color.inverseOnSurface}))`)
		}
	},
	filled: {
		labelTextColor: c(`var(--m3e-filled-button-label-text-color, var(--m3e-button-label-text-color, ${A.color.onPrimary}))`),
		iconColor: c(`var(--m3e-filled-button-icon-color, var(--m3e-button-icon-color, ${A.color.onPrimary}))`),
		containerColor: c(`var(--m3e-filled-button-container-color, var(--m3e-button-container-color, ${A.color.primary}))`),
		containerElevation: c(`var(--m3e-filled-button-container-elevation, var(--m3e-button-container-elevation, ${A.elevation.level0}))`),
		unselectedLabelTextColor: c(`var(--m3e-filled-button-unselected-label-text-color, var(--m3e-button-unselected-label-text-color, ${A.color.onSurfaceVariant}))`),
		unselectedIconColor: c(`var(--m3e-filled-button-unselected-icon-color, var(--m3e-button-unselected-icon-color, ${A.color.onSurfaceVariant}))`),
		unselectedContainerColor: c(`var(--m3e-filled-button-unselected-container-color, var(--m3e-button-unselected-container-color, ${A.color.surfaceContainer}))`),
		selectedLabelTextColor: c(`var(--m3e-filled-button-selected-label-text-color, var(--m3e-button-selected-label-text-color, ${A.color.onPrimary}))`),
		selectedIconColor: c(`var(--m3e-filled-button-selected-icon-color, var(--m3e-button-selected-icon-color, ${A.color.onPrimary}))`),
		selectedContainerColor: c(`var(--m3e-filled-button-selected-container-color, var(--m3e-button-selected-container-color, ${A.color.primary}))`),
		disabled: {
			containerColor: c(`var(--m3e-filled-button-disabled-container-color, var(--m3e-button-disabled-container-color, ${A.color.onSurface}))`),
			containerOpacity: c("var(--m3e-filled-button-disabled-container-opacity, var(--m3e-button-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-filled-button-disabled-icon-color, var(--m3e-button-disabled-icon-color, ${A.color.onSurface}))`),
			iconOpacity: c("var(--m3e-filled-button-disabled-icon-opacity, var(--m3e-button-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-filled-button-disabled-label-text-color, var(--m3e-button-disabled-label-text-color, ${A.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-filled-button-disabled-label-text-opacity, var(--m3e-button-disabled-label-text-opacity, 38%))"),
			containerElevation: c(`var(--m3e-filled-button-disabled-container-elevation, var(--m3e-button-disabled-container-elevation, ${A.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-filled-button-hover-icon-color, var(--m3e-button-hover-icon-color, ${A.color.onPrimary}))`),
			labelTextColor: c(`var(--m3e-filled-button-hover-label-text-color, var(--m3e-button-hover-label-text-color, ${A.color.onPrimary}))`),
			stateLayerColor: c(`var(--m3e-filled-button-hover-state-layer-color, var(--m3e-button-hover-state-layer-color, ${A.color.onPrimary}))`),
			stateLayerOpacity: c(`var(--m3e-filled-button-hover-state-layer-opacity, var(--m3e-button-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-filled-button-hover-container-elevation, var(--m3e-button-hover-container-elevation, ${A.elevation.level1}))`),
			unselectedIconColor: c(`var(--m3e-filled-button-hover-unselected-icon-color, var(--m3e-button-hover-unselected-icon-color, ${A.color.onSurfaceVariant}))`),
			unselectedLabelTextColor: c(`var(--m3e-filled-button-hover-unselected-label-text-color, var(--m3e-button-hover-unselected-label-text-color, ${A.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-filled-button-hover-unselected-state-layer-color, var(--m3e-button-hover-unselected-state-layer-color, ${A.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-filled-button-hover-selected-icon-color, var(--m3e-button-hover-selected-icon-color, ${A.color.onPrimary}))`),
			selectedLabelTextColor: c(`var(--m3e-filled-button-hover-selected-label-text-color, var(--m3e-button-hover-selected-label-text-color, ${A.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-filled-button-hover-selected-state-layer-color, var(--m3e-button-hover-selected-state-layer-color, ${A.color.onPrimary}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-filled-button-focus-icon-color, var(--m3e-button-focus-icon-color, ${A.color.onPrimary}))`),
			labelTextColor: c(`var(--m3e-filled-button-focus-label-text-color, var(--m3e-button-focus-label-text-color, ${A.color.onPrimary}))`),
			stateLayerColor: c(`var(--m3e-filled-button-focus-state-layer-color, var(--m3e-button-focus-state-layer-color, ${A.color.onPrimary}))`),
			stateLayerOpacity: c(`var(--m3e-filled-button-focus-state-layer-opacity, var(--m3e-button-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-filled-button-focus-container-elevation, var(--m3e-button-focus-container-elevation, ${A.elevation.level0}))`),
			unselectedIconColor: c(`var(--m3e-filled-button-focus-unselected-icon-color, var(--m3e-button-focus-unselected-icon-color, ${A.color.onSurfaceVariant}))`),
			unselectedLabelTextColor: c(`var(--m3e-filled-button-focus-unselected-label-text-color, var(--m3e-button-focus-unselected-label-text-color, ${A.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-filled-button-focus-unselected-state-layer-color, var(--m3e-button-focus-unselected-state-layer-color, ${A.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-filled-button-focus-selected-icon-color, var(--m3e-button-focus-selected-icon-color, ${A.color.onPrimary}))`),
			selectedLabelTextColor: c(`var(--m3e-filled-button-focus-selected-label-text-color, var(--m3e-button-focus-selected-label-text-color, ${A.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-filled-button-focus-selected-state-layer-color, var(--m3e-button-focus-selected-state-layer-color, ${A.color.onPrimary}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-filled-button-pressed-icon-color, var(--m3e-button-pressed-icon-color, ${A.color.onPrimary}))`),
			labelTextColor: c(`var(--m3e-filled-button-pressed-label-text-color, var(--m3e-button-pressed-label-text-color, ${A.color.onPrimary}))`),
			stateLayerColor: c(`var(--m3e-filled-button-pressed-state-layer-color, var(--m3e-button-pressed-state-layer-color, ${A.color.onPrimary}))`),
			stateLayerOpacity: c(`var(--m3e-filled-button-pressed-state-layer-opacity, var(--m3e-button-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-filled-button-pressed-container-elevation, var(--m3e-button-pressed-container-elevation, ${A.elevation.level0}))`),
			unselectedIconColor: c(`var(--m3e-filled-button-pressed-unselected-icon-color, var(--m3e-button-pressed-unselected-icon-color, ${A.color.onSurfaceVariant}))`),
			unselectedLabelTextColor: c(`var(--m3e-filled-button-pressed-unselected-label-text-color, var(--m3e-button-pressed-unselected-label-text-color, ${A.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-filled-button-pressed-unselected-state-layer-color, var(--m3e-button-pressed-unselected-state-layer-color, ${A.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-filled-button-pressed-selected-icon-color, var(--m3e-button-pressed-selected-icon-color, ${A.color.onPrimary}))`),
			selectedLabelTextColor: c(`var(--m3e-filled-button-pressed-selected-label-text-color, var(--m3e-button-pressed-selected-label-text-color, ${A.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-filled-button-pressed-selected-state-layer-color, var(--m3e-button-pressed-selected-state-layer-color, ${A.color.onPrimary}))`)
		}
	},
	tonal: {
		labelTextColor: c(`var(--m3e-tonal-button-label-text-color, var(--m3e-button-label-text-color, ${A.color.onSecondaryContainer}))`),
		iconColor: c(`var(--m3e-tonal-button-icon-color, var(--m3e-button-icon-color, ${A.color.onSecondaryContainer}))`),
		containerColor: c(`var(--m3e-tonal-button-container-color, var(--m3e-button-container-color, ${A.color.secondaryContainer}))`),
		containerElevation: c(`var(--m3e-tonal-button-container-elevation, var(--m3e-button-container-elevation, ${A.elevation.level0}))`),
		unselectedLabelTextColor: c(`var(--m3e-tonal-button-unselected-label-text-color, var(--m3e-button-unselected-label-text-color, ${A.color.onSecondaryContainer}))`),
		unselectedIconColor: c(`var(--m3e-tonal-button-unselected-icon-color, var(--m3e-button-unselected-icon-color, ${A.color.onSecondaryContainer}))`),
		unselectedContainerColor: c(`var(--m3e-tonal-button-unselected-container-color, var(--m3e-button-unselected-container-color, ${A.color.secondaryContainer}))`),
		selectedLabelTextColor: c(`var(--m3e-tonal-button-selected-label-text-color, var(--m3e-button-selected-label-text-color, ${A.color.onSecondary}))`),
		selectedIconColor: c(`var(--m3e-tonal-button-selected-icon-color, var(--m3e-button-selected-icon-color, ${A.color.onSecondary}))`),
		selectedContainerColor: c(`var(--m3e-tonal-button-selected-container-color, var(--m3e-button-selected-container-color, ${A.color.secondary}))`),
		disabled: {
			containerColor: c(`var(--m3e-tonal-button-disabled-container-color, var(--m3e-button-disabled-container-color, ${A.color.onSurface}))`),
			containerOpacity: c("var(--m3e-tonal-button-disabled-container-opacity, var(--m3e-button-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-tonal-button-disabled-icon-color, var(--m3e-button-disabled-icon-color, ${A.color.onSurface}))`),
			iconOpacity: c("var(--m3e-tonal-button-disabled-icon-opacity, var(--m3e-button-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-tonal-button-disabled-label-text-color, var(--m3e-button-disabled-label-text-color, ${A.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-tonal-button-disabled-label-text-opacity, var(--m3e-button-disabled-label-text-opacity, 38%))"),
			containerElevation: c(`var(--m3e-tonal-button-disabled-container-elevation, var(--m3e-button-disabled-container-elevation, ${A.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-tonal-button-hover-icon-color, var(--m3e-button-hover-icon-color, ${A.color.onSecondaryContainer}))`),
			labelTextColor: c(`var(--m3e-tonal-button-hover-label-text-color, var(--m3e-button-hover-label-text-color, ${A.color.onSecondaryContainer}))`),
			stateLayerColor: c(`var(--m3e-tonal-button-hover-state-layer-color, var(--m3e-button-hover-state-layer-color, ${A.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-tonal-button-hover-state-layer-opacity, var(--m3e-button-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-tonal-button-hover-container-elevation, var(--m3e-button-hover-container-elevation, ${A.elevation.level1}))`),
			unselectedIconColor: c(`var(--m3e-tonal-button-hover-unselected-icon-color, var(--m3e-button-hover-unselected-icon-color, ${A.color.onSecondaryContainer}))`),
			unselectedLabelTextColor: c(`var(--m3e-tonal-button-hover-unselected-label-text-color, var(--m3e-button-hover-unselected-label-text-color, ${A.color.onSecondaryContainer}))`),
			unselectedStateLayerColor: c(`var(--m3e-tonal-button-hover-unselected-state-layer-color, var(--m3e-button-hover-unselected-state-layer-color, ${A.color.onSecondaryContainer}))`),
			selectedIconColor: c(`var(--m3e-tonal-button-hover-selected-icon-color, var(--m3e-button-hover-selected-icon-color, ${A.color.onSecondary}))`),
			selectedLabelTextColor: c(`var(--m3e-tonal-button-hover-selected-label-text-color, var(--m3e-button-hover-selected-label-text-color, ${A.color.onSecondary}))`),
			selectedStateLayerColor: c(`var(--m3e-tonal-button-hover-selected-state-layer-color, var(--m3e-button-hover-selected-state-layer-color, ${A.color.onSecondary}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-tonal-button-focus-icon-color, var(--m3e-button-focus-icon-color, ${A.color.onSecondaryContainer}))`),
			labelTextColor: c(`var(--m3e-tonal-button-focus-label-text-color, var(--m3e-button-focus-label-text-color, ${A.color.onSecondaryContainer}))`),
			stateLayerColor: c(`var(--m3e-tonal-button-focus-state-layer-color, var(--m3e-button-focus-state-layer-color, ${A.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-tonal-button-focus-state-layer-opacity, var(--m3e-button-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-tonal-button-focus-container-elevation, var(--m3e-button-focus-container-elevation, ${A.elevation.level0}))`),
			unselectedIconColor: c(`var(--m3e-tonal-button-focus-unselected-icon-color, var(--m3e-button-focus-unselected-icon-color, ${A.color.onSecondaryContainer}))`),
			unselectedLabelTextColor: c(`var(--m3e-tonal-button-focus-unselected-label-text-color, var(--m3e-button-focus-unselected-label-text-color, ${A.color.onSecondaryContainer}))`),
			unselectedStateLayerColor: c(`var(--m3e-tonal-button-focus-unselected-state-layer-color, var(--m3e-button-focus-unselected-state-layer-color, ${A.color.onSecondaryContainer}))`),
			selectedIconColor: c(`var(--m3e-tonal-button-focus-selected-icon-color, var(--m3e-button-focus-selected-icon-color, ${A.color.onSecondary}))`),
			selectedLabelTextColor: c(`var(--m3e-tonal-button-focus-selected-label-text-color, var(--m3e-button-focus-selected-label-text-color, ${A.color.onSecondary}))`),
			selectedStateLayerColor: c(`var(--m3e-tonal-button-focus-selected-state-layer-color, var(--m3e-button-focus-selected-state-layer-color, ${A.color.onSecondary}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-tonal-button-pressed-icon-color, var(--m3e-button-pressed-icon-color, ${A.color.onSecondaryContainer}))`),
			labelTextColor: c(`var(--m3e-tonal-button-pressed-label-text-color, var(--m3e-button-pressed-label-text-color, ${A.color.onSecondaryContainer}))`),
			stateLayerColor: c(`var(--m3e-tonal-button-pressed-state-layer-color, var(--m3e-button-pressed-state-layer-color, ${A.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-tonal-button-pressed-state-layer-opacity, var(--m3e-button-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-tonal-button-pressed-container-elevation, var(--m3e-button-pressed-container-elevation, ${A.elevation.level0}))`),
			unselectedIconColor: c(`var(--m3e-tonal-button-pressed-unselected-icon-color, var(--m3e-button-pressed-unselected-icon-color, ${A.color.onSecondaryContainer}))`),
			unselectedLabelTextColor: c(`var(--m3e-tonal-button-pressed-unselected-label-text-color, var(--m3e-button-pressed-unselected-label-text-color, ${A.color.onSecondaryContainer}))`),
			unselectedStateLayerColor: c(`var(--m3e-tonal-button-pressed-unselected-state-layer-color, var(--m3e-button-pressed-unselected-state-layer-color, ${A.color.onSecondaryContainer}))`),
			selectedIconColor: c(`var(--m3e-tonal-button-pressed-selected-icon-color, var(--m3e-button-pressed-selected-icon-color, ${A.color.onSecondary}))`),
			selectedLabelTextColor: c(`var(--m3e-tonal-button-pressed-selected-label-text-color, var(--m3e-button-pressed-selected-label-text-color, ${A.color.onSecondary}))`),
			selectedStateLayerColor: c(`var(--m3e-tonal-button-pressed-selected-state-layer-color, var(--m3e-button-pressed-selected-state-layer-color, ${A.color.onSecondary}))`)
		}
	},
	text: {
		labelTextColor: c(`var(--m3e-text-button-label-text-color, var(--m3e-button-label-text-color, ${A.color.primary}))`),
		iconColor: c(`var(--m3e-text-button-icon-color, var(--m3e-button-icon-color, ${A.color.primary}))`),
		containerColor: c("var(--m3e-text-button-container-color)"),
		unselectedLabelTextColor: c(`var(--m3e-text-button-unselected-label-text-color, var(--m3e-button-unselected-label-text-color, ${A.color.primary}))`),
		unselectedIconColor: c(`var(--m3e-text-button-unselected-icon-color, var(--m3e-button-unselected-icon-color, ${A.color.primary}))`),
		unselectedContainerColor: c("var(--m3e-text-button-unselected-container-color)"),
		selectedLabelTextColor: c(`var(--m3e-text-button-selected-label-text-color, var(--m3e-button-selected-label-text-color, ${A.color.primary}))`),
		selectedIconColor: c(`var(--m3e-text-button-selected-icon-color, var(--m3e-button-selected-icon-color, ${A.color.primary}))`),
		selectedContainerColor: c("var(--m3e-text-button-selected-container-color)"),
		disabled: {
			containerColor: c(`var(--m3e-text-button-disabled-container-color, var(--m3e-button-disabled-container-color, ${A.color.onSurface}))`),
			containerOpacity: c("var(--m3e-text-button-disabled-container-opacity, var(--m3e-button-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-text-button-disabled-icon-color, var(--m3e-button-disabled-icon-color, ${A.color.onSurface}))`),
			iconOpacity: c("var(--m3e-text-button-disabled-icon-opacity, var(--m3e-button-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-text-button-disabled-label-text-color, var(--m3e-button-disabled-label-text-color, ${A.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-text-button-disabled-label-text-opacity, var(--m3e-button-disabled-label-text-opacity, 38%))")
		},
		hover: {
			iconColor: c(`var(--m3e-text-button-hover-icon-color, var(--m3e-button-hover-icon-color, ${A.color.primary}))`),
			labelTextColor: c(`var(--m3e-text-button-hover-label-text-color, var(--m3e-button-hover-label-text-color, ${A.color.primary}))`),
			stateLayerColor: c(`var(--m3e-text-button-hover-state-layer-color, var(--m3e-button-hover-state-layer-color, ${A.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-text-button-hover-state-layer-opacity, var(--m3e-button-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-text-button-hover-unselected-icon-color, var(--m3e-button-hover-unselected-icon-color, ${A.color.primary}))`),
			unselectedLabelTextColor: c(`var(--m3e-text-button-hover-unselected-label-text-color, var(--m3e-button-hover-unselected-label-text-color, ${A.color.primary}))`),
			unselectedStateLayerColor: c(`var(--m3e-text-button-hover-unselected-state-layer-color, var(--m3e-button-hover-unselected-state-layer-color, ${A.color.primary}))`),
			selectedIconColor: c(`var(--m3e-text-button-hover-selected-icon-color, var(--m3e-button-hover-selected-icon-color, ${A.color.primary}))`),
			selectedLabelTextColor: c(`var(--m3e-text-button-hover-selected-label-text-color, var(--m3e-button-hover-selected-label-text-color, ${A.color.primary}))`),
			selectedStateLayerColor: c(`var(--m3e-text-button-hover-selected-state-layer-color, var(--m3e-button-hover-selected-state-layer-color, ${A.color.primary}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-text-button-focus-icon-color, var(--m3e-button-focus-icon-color, ${A.color.primary}))`),
			labelTextColor: c(`var(--m3e-text-button-focus-label-text-color, var(--m3e-button-focus-label-text-color, ${A.color.primary}))`),
			stateLayerColor: c(`var(--m3e-text-button-focus-state-layer-color, var(--m3e-button-focus-state-layer-color, ${A.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-text-button-focus-state-layer-opacity, var(--m3e-button-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-text-button-focus-unselected-icon-color, var(--m3e-button-focus-unselected-icon-color, ${A.color.primary}))`),
			unselectedLabelTextColor: c(`var(--m3e-text-button-focus-unselected-label-text-color, var(--m3e-button-focus-unselected-label-text-color, ${A.color.primary}))`),
			unselectedStateLayerColor: c(`var(--m3e-text-button-focus-unselected-state-layer-color, var(--m3e-button-focus-unselected-state-layer-color, ${A.color.primary}))`),
			selectedIconColor: c(`var(--m3e-text-button-focus-selected-icon-color, var(--m3e-button-focus-selected-icon-color, ${A.color.primary}))`),
			selectedLabelTextColor: c(`var(--m3e-text-button-focus-selected-label-text-color, var(--m3e-button-focus-selected-label-text-color, ${A.color.primary}))`),
			selectedStateLayerColor: c(`var(--m3e-text-button-focus-selected-state-layer-color, var(--m3e-button-focus-selected-state-layer-color, ${A.color.primary}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-text-button-pressed-icon-color, var(--m3e-button-pressed-icon-color, ${A.color.primary}))`),
			labelTextColor: c(`var(--m3e-text-button-pressed-label-text-color, var(--m3e-button-pressed-label-text-color, ${A.color.primary}))`),
			stateLayerColor: c(`var(--m3e-text-button-pressed-state-layer-color, var(--m3e-button-pressed-state-layer-color, ${A.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-text-button-pressed-state-layer-opacity, var(--m3e-button-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-text-button-pressed-unselected-icon-color, var(--m3e-button-pressed-unselected-icon-color, ${A.color.primary}))`),
			unselectedLabelTextColor: c(`var(--m3e-text-button-pressed-unselected-label-text-color, var(--m3e-button-pressed-unselected-label-text-color, ${A.color.primary}))`),
			unselectedStateLayerColor: c(`var(--m3e-text-button-pressed-unselected-state-layer-color, var(--m3e-button-pressed-unselected-state-layer-color, ${A.color.primary}))`),
			selectedIconColor: c(`var(--m3e-text-button-pressed-selected-icon-color, var(--m3e-button-pressed-selected-icon-color, ${A.color.primary}))`),
			selectedLabelTextColor: c(`var(--m3e-text-button-pressed-selected-label-text-color, var(--m3e-button-pressed-selected-label-text-color, ${A.color.primary}))`),
			selectedStateLayerColor: c(`var(--m3e-text-button-pressed-selected-state-layer-color, var(--m3e-button-pressed-selected-state-layer-color, ${A.color.primary}))`)
		}
	}
};
function Mc(e) {
	let t = c(`:is(:state(--${e}), :--${e})`);
	return l`:host(${t}:not(:disabled):not([disabled-interactive])) .base { background-color: ${V[e].containerColor ?? c("unset")}; --m3e-state-layer-hover-color: ${V[e].hover.stateLayerColor}; --m3e-state-layer-hover-opacity: ${V[e].hover.stateLayerOpacity}; --m3e-state-layer-focus-color: ${V[e].focus.stateLayerColor}; --m3e-state-layer-focus-opacity: ${V[e].focus.stateLayerOpacity}; --m3e-ripple-color: ${V[e].pressed.stateLayerColor}; --m3e-ripple-opacity: ${V[e].pressed.stateLayerOpacity}; --m3e-elevation-level: ${V[e].containerElevation ?? c("unset")}; --m3e-elevation-hover-level: ${V[e].hover.containerElevation ?? c("unset")}; --m3e-elevation-focus-level: ${V[e].focus.containerElevation ?? c("unset")}; --m3e-elevation-pressed-level: ${V[e].pressed.containerElevation ?? c("unset")}; } :host(${t}[toggle]:not([selected]):not(:disabled):not([disabled-interactive])) .base { background-color: ${V[e].unselectedContainerColor ?? c("unset")}; --m3e-state-layer-hover-color: ${V[e].hover.unselectedStateLayerColor}; --m3e-state-layer-focus-color: ${V[e].focus.unselectedStateLayerColor}; --m3e-ripple-color: ${V[e].pressed.unselectedStateLayerColor}; } :host(${t}[toggle][selected]:not(:disabled):not([disabled-interactive])) .base { background-color: ${V[e].selectedContainerColor ?? c("unset")}; --m3e-state-layer-hover-color: ${V[e].hover.selectedStateLayerColor}; --m3e-state-layer-focus-color: ${V[e].focus.selectedStateLayerColor}; --m3e-ripple-color: ${V[e].pressed.selectedStateLayerColor}; } :host(${t}:not(:disabled):not([disabled-interactive])) .base { outline-color: ${V[e].outlineColor ?? c("unset")}; } :host(${t}:focus:not(:disabled):not([disabled-interactive])) .base { outline-color: ${V[e].focus.outlineColor ?? c("unset")}; } :host(${t}:hover:not(:disabled):not([disabled-interactive])) .base { outline-color: ${V[e].hover.outlineColor ?? c("unset")}; } :host(${t}:is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive])) .base { outline-color: ${V[e].pressed.outlineColor ?? c("unset")}; } :host(${t}:not(:disabled):not([disabled-interactive])) .label { color: ${V[e].labelTextColor}; } :host(${t}[toggle]:not([selected]):not(:disabled):not([disabled-interactive])) .label { color: ${V[e].unselectedLabelTextColor}; } :host(${t}[toggle][selected]:not(:disabled):not([disabled-interactive])) .label { color: ${V[e].selectedLabelTextColor}; } :host(${t}:focus:not(:disabled):not([disabled-interactive])) .label { color: ${V[e].focus.labelTextColor}; } :host(${t}[toggle]:not([selected]):focus:not(:disabled):not([disabled-interactive])) .label { color: ${V[e].focus.unselectedLabelTextColor}; } :host(${t}[toggle][selected]:focus:not(:disabled):not([disabled-interactive])) .label { color: ${V[e].focus.selectedLabelTextColor}; } :host(${t}:hover:not(:disabled):not([disabled-interactive])) .label { color: ${V[e].hover.labelTextColor}; } :host(${t}[toggle]:not([selected]):hover:not(:disabled):not([disabled-interactive])) .label { color: ${V[e].hover.unselectedLabelTextColor}; } :host(${t}[toggle][selected]:hover:not(:disabled):not([disabled-interactive])) .label { color: ${V[e].hover.selectedLabelTextColor}; } :host(${t}:is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive])) .label { color: ${V[e].pressed.labelTextColor}; } :host( ${t}[toggle]:not([selected]):is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive]) ) .label { color: ${V[e].pressed.unselectedLabelTextColor}; } :host(${t}[toggle][selected]:is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive])) .label { color: ${V[e].pressed.selectedLabelTextColor}; } :host(${t}:not(:disabled):not([disabled-interactive])) .icon { color: ${V[e].iconColor}; } :host(${t}[toggle]:not([selected]):not(:disabled):not([disabled-interactive])) .icon { color: ${V[e].unselectedIconColor}; } :host(${t}[toggle][selected]:not(:disabled):not([disabled-interactive])) .icon { color: ${V[e].selectedIconColor}; } :host(${t}:focus:not(:disabled):not([disabled-interactive])) .icon { color: ${V[e].focus.iconColor}; } :host(${t}[toggle]:not([selected]):focus:not(:disabled):not([disabled-interactive])) .icon { color: ${V[e].focus.unselectedIconColor}; } :host(${t}[toggle][selected]:focus:not(:disabled):not([disabled-interactive])) .icon { color: ${V[e].focus.selectedIconColor}; } :host(${t}:hover:not(:disabled):not([disabled-interactive])) .icon { color: ${V[e].hover.iconColor}; } :host(${t}[toggle]:not([selected]):hover:not(:disabled):not([disabled-interactive])) .icon { color: ${V[e].hover.unselectedIconColor}; } :host(${t}[toggle][selected]:hover:not(:disabled):not([disabled-interactive])) .icon { color: ${V[e].hover.selectedIconColor}; } :host(${t}:is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive])) .icon { color: ${V[e].pressed.iconColor}; } :host( ${t}[toggle]:not([selected]):is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive]) ) .icon { color: ${V[e].pressed.unselectedIconColor}; } :host(${t}[toggle][selected]:is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive])) .icon { color: ${V[e].pressed.selectedIconColor}; } :host(${t}:disabled) .base, :host(${t}[disabled-interactive]) .base { --m3e-elevation-level: ${V[e].disabled.containerElevation ?? c("unset")}; outline-color: ${V[e].disabled.outlineColor ?? c("unset")}; background-color: color-mix( in srgb, ${V[e].disabled.containerColor} ${V[e].disabled.containerOpacity}, transparent ); } :host(${t}:disabled) .label, :host(${t}[disabled-interactive]) .label { color: color-mix( in srgb, ${V[e].disabled.labelTextColor} ${V[e].disabled.labelTextOpacity}, transparent ); } :host(${t}:disabled) .icon, :host(${t}[disabled-interactive]) .icon { color: color-mix( in srgb, ${V[e].disabled.iconColor} ${V[e].disabled.iconOpacity}, transparent ); }`;
}
var Nc = [
	Mc("text"),
	Mc("elevated"),
	Mc("outlined"),
	Mc("filled"),
	Mc("tonal"),
	l`:host(:is(:state(--outlined), :--outlined):not([toggle][selected]):not(:disabled):not([disabled-interactive])) .base { outline-style: solid; }`
], Pc, Fc, Ic, Lc, Rc, zc, Bc, Vc, Hc, Uc, Wc = class extends qi(Zi(Ui(Vi(Ci(vi(T(j(x, "button"), !0))))))) {
	constructor() {
		super(), Pc.add(this), this._adjacentPressedTimeout = -1, Fc.set(this, (e) => t(this, Pc, "m", zc).call(this, e)), this.variant = "text", this.shape = "rounded", this.size = "small", this.toggle = !1, this.selected = !1, new cr(this, { callback: () => this._handleResize() }), new sn(this, { callback: (e) => {
			!this.disabledInteractive && !e && !this.grouped && this._base?.style.removeProperty("--_button-shape");
		} }), new nr(this, {
			isPressedKey: (e) => e === " ",
			minPressedDuration: 225,
			callback: (e) => {
				!this.disabled && !this.disabledInteractive && (e ? (t(this, Pc, "m", Vc).call(this), t(this, Pc, "m", Hc).call(this, !0)) : t(this, Pc, "m", Hc).call(this, !1));
			}
		});
	}
	get grouped() {
		return Rr(this, "--grouped");
	}
	render() {
		return y`<div class="base"><m3e-elevation class="elevation" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-elevation><m3e-state-layer class="state-layer" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-state-layer><m3e-focus-ring class="focus-ring" ?disabled="${this.disabled}"></m3e-focus-ring><m3e-ripple class="ripple" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-ripple><div class="touch" aria-hidden="true"></div>${this[Ji]()}<div class="wrapper">${this.toggle ? y`<slot class="icon" name="selected-icon" aria-hidden="true" @slotchange="${t(this, Pc, "m", Bc)}"></slot>` : b}<slot class="icon" name="icon" aria-hidden="true"></slot><div class="label">${this.toggle && this.selected ? y`<slot name="selected"><slot></slot></slot>` : y`<slot></slot>`}</div><slot class="icon" name="trailing-icon" aria-hidden="true"></slot></div></div>`;
	}
	connectedCallback() {
		super.connectedCallback(), this.addEventListener("click", t(this, Fc, "f")), t(this, Pc, "m", Ic).call(this), t(this, Pc, "m", Lc).call(this), t(this, Pc, "m", Rc).call(this);
	}
	disconnectedCallback() {
		super.disconnectedCallback(), [
			"--pressed",
			"--resting",
			"--grouped",
			"--connected"
		].forEach((e) => E(this, e)), this._base?.style.removeProperty("--_button-shape"), this.style.removeProperty("--_button-width"), this.style.removeProperty("--_adjacent-shrink"), E(this, "--adjacent-pressed"), this.removeEventListener("click", t(this, Fc, "f"));
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, Pc, "m", Ic).call(this), e.has("shape") && t(this, Pc, "m", Lc).call(this), e.has("size") && t(this, Pc, "m", Rc).call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), [
			this._elevation,
			this._focusRing,
			this._stateLayer,
			this._ripple
		].forEach((e) => e?.attach(this));
	}
	updated(e) {
		if (super.updated(e), (e.has("disabled") && this.disabled || e.has("disabledInteractive") && this.disabledInteractive) && (E(this, "--pressed"), E(this, "--resting")), (e.has("toggle") || e.has("selected")) && (this.ariaPressed = this.toggle ? `${this.selected}` : null, this.toggle)) for (let e of this.querySelectorAll("m3e-icon")) e.toggleAttribute("filled", this.selected);
	}
	_handleResize() {
		if (this.grouped && !Rr(this, "--no-resize") && this !== document.activeElement) {
			let e = this.getBoundingClientRect().width;
			e > 0 ? (this.style.setProperty("--_button-width", `${e}px`), t(this, Pc, "m", Vc).call(this, !0)) : this.style.removeProperty("--_button-width");
		}
	}
};
Fc = /* @__PURE__ */ new WeakMap(), Pc = /* @__PURE__ */ new WeakSet(), Ic = function() {
	Dc(this.variant) || (this.variant = "text"), Br(this, this.variant, "elevated", "filled", "outlined", "text", "tonal");
}, Lc = function() {
	Tc(this.shape) || (this.shape = "rounded"), Br(this, this.shape, "rounded", "square");
}, Rc = function() {
	Ec(this.size) || (this.size = "small"), Br(this, this.size, "extra-large", "extra-small", "large", "medium", "small");
}, zc = function(e) {
	(this.disabled || this.disabledInteractive) && (e.preventDefault(), e.stopImmediatePropagation()), this.toggle && !e.defaultPrevented && this.dispatchEvent(new Event("beforeinput", {
		bubbles: !0,
		cancelable: !0
	})) && (this.selected = !this.selected, this.dispatchEvent(new Event("input", { bubbles: !0 })), this.dispatchEvent(new Event("change", { bubbles: !0 })));
}, Bc = function(e) {
	this._base?.classList.toggle("with-selected-icon", Rt(e.target));
}, Vc = function(e = !1) {
	if (!this._base) return;
	let t = parseFloat(getComputedStyle(this._base).borderRadius);
	if (!isNaN(t) || e) {
		let n = this.clientHeight / 2;
		(n < t || e) && this._base?.style.setProperty("--_button-shape", `${n}px`);
	}
}, Hc = function(e) {
	let n = this.getBoundingClientRect().width, r = this.closest("m3e-button-group");
	if (r && r.variant === "standard") {
		let i = [...r.querySelectorAll("m3e-button,m3e-icon-button")];
		for (let e of i) clearTimeout(e._adjacentPressedTimeout), e._adjacentPressedTimeout = -1;
		let a = i.indexOf(this);
		if (e) {
			let e = n * parseFloat(getComputedStyle(this).getPropertyValue("--m3e-standard-button-group-width-multiplier") || "0.15");
			a > 0 && a < i.length - 1 && (e /= 2);
			for (let t = 0; t < i.length; t++) t == a - 1 || t == a + 1 ? (zr(i[t], "--no-resize"), i[t].style.setProperty("--_adjacent-shrink", `${e}px`), zr(i[t], "--adjacent-pressed")) : t == a ? (zr(i[t], "--no-resize"), i[t].style.removeProperty("--_adjacent-shrink"), E(i[t], "--adjacent-pressed")) : (E(i[t], "--no-resize"), i[t].style.removeProperty("--_adjacent-shrink"), E(i[t], "--adjacent-pressed"));
		} else {
			for (let e = 0; e < i.length; e++) (e == a - 1 || e == a + 1) && i[e].style.setProperty("--_adjacent-shrink", "0px");
			Bt() ? t(this, Pc, "m", Uc).call(this, i) : this.addEventListener("transitionend", (e) => {
				e.propertyName === "width" && (this._adjacentPressedTimeout = setTimeout(() => {
					this._adjacentPressedTimeout > -1 && t(this, Pc, "m", Uc).call(this, i);
				}, 600));
			}, { once: !0 });
		}
	}
	D(this, "--pressed", e), D(this, "--resting", !e);
}, Uc = function(e) {
	for (let t of e) E(t, "--adjacent-pressed"), E(t, "--no-resize"), t.style.removeProperty("--_adjacent-shrink");
}, Wc.styles = [
	Ac,
	Nc,
	jc
], e([C(".base")], Wc.prototype, "_base", void 0), e([C(".elevation")], Wc.prototype, "_elevation", void 0), e([C(".focus-ring")], Wc.prototype, "_focusRing", void 0), e([C(".state-layer")], Wc.prototype, "_stateLayer", void 0), e([C(".ripple")], Wc.prototype, "_ripple", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], Wc.prototype, "variant", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], Wc.prototype, "shape", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], Wc.prototype, "size", void 0), e([S({
	type: Boolean,
	reflect: !0
})], Wc.prototype, "toggle", void 0), e([S({
	type: Boolean,
	reflect: !0
})], Wc.prototype, "selected", void 0), e([lr(40)], Wc.prototype, "_handleResize", null), Wc = e([w("m3e-button")], Wc);
//#endregion
//#region node_modules/@m3e/web/dist/button-group.js
function Gc(e) {
	return e === "standard" || e === "connected";
}
function Kc(e) {
	return e === "extra-small" || e === "small" || e === "medium" || e === "large" || e === "extra-large";
}
var qc, Jc, Yc, Xc, Zc, Qc, $c, el, tl = class extends j(T(x), "group") {
	constructor() {
		super(...arguments), qc.add(this), Jc.set(this, new nr(this, {
			target: null,
			capture: !0,
			minPressedDuration: 225,
			isPressedKey: (e) => e === " ",
			callback: (e) => t(this, qc, "m", $c).call(this, e)
		})), this.variant = "standard", this.size = "small", this.multi = !1;
	}
	connectedCallback() {
		super.connectedCallback(), t(this, qc, "m", Yc).call(this), t(this, qc, "m", Xc).call(this), this.hasAttribute("disable-role") && (this.role = null);
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this._base?.style.removeProperty("--_button-group-width"), this._base?.classList.remove("pressed");
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, qc, "m", Yc).call(this), e.has("size") && t(this, qc, "m", Xc).call(this);
	}
	update(e) {
		super.update(e), (e.has("multi") || e.has("variant")) && t(this, qc, "m", Zc).call(this), e.has("variant") && this._base?.style.removeProperty("--_button-group-width");
	}
	render() {
		return y`<div class="base"><slot @slotchange="${t(this, qc, "m", Zc)}" @change="${t(this, qc, "m", Qc)}"></slot></div>`;
	}
};
Jc = /* @__PURE__ */ new WeakMap(), qc = /* @__PURE__ */ new WeakSet(), Yc = function() {
	Gc(this.variant) || (this.variant = "standard"), Br(this, this.variant, "connected", "standard");
}, Xc = function() {
	Kc(this.size) || (this.size = "small"), Br(this, this.size, "extra-large", "extra-small", "large", "medium", "small");
}, Zc = async function() {
	let e = this.buttons;
	for (let e of t(this, Jc, "f").targets) t(this, Jc, "f").unobserve(e);
	for (let e of this.buttons) await Gt(e), await Wt(e);
	let n = [...e].some((e) => e.toggle);
	this.hasAttribute("disable-role") || (this.role = n && !this.multi ? "radiogroup" : "group");
	let r = this.role === "radiogroup" ? "radio" : "button";
	e.forEach((n, i) => {
		if (t(this, Jc, "f").observe(n), D(n, "--connected", this.variant === "connected"), zr(n, "--grouped"), D(n, "--first", i == 0), D(n, "--last", i == e.length - 1), !this.hasAttribute("disable-role") && n.role !== r && n.toggle) {
			let e = n.toggle ? n.selected ? "true" : "false" : null;
			n.role = r, n.role === "button" ? (n.ariaPressed = e, n.ariaChecked = null) : (n.ariaChecked = e, n.ariaPressed = null);
		}
	});
}, Qc = function(e) {
	if (!this.multi && e.target instanceof HTMLElement && (e.target.tagName === "M3E-BUTTON" || e.target.tagName === "M3E-ICON-BUTTON")) {
		if (!Gr(e.target) || !e.target.selected) return;
		for (let t of this.buttons) t !== e.target && t.selected && (t.selected = !1);
	}
}, $c = function(e) {
	let n = this._base;
	if (n) {
		if (!e || this.variant === "connected") {
			let e = this.buttons.find((e) => e === document.activeElement);
			!Bt() && e ? e.addEventListener("transitionend", () => queueMicrotask(() => {
				Rr(e, "--pressed") || t(this, qc, "m", el).call(this, n);
			}), { once: !0 }) : t(this, qc, "m", el).call(this, n);
		} else n.classList.add("pressed"), n.style.setProperty("--_button-group-width", `${n.getBoundingClientRect().width}px`);
	}
}, el = function(e) {
	e.style.removeProperty("--_button-group-width"), e.classList.remove("pressed");
}, tl.styles = l`:host { display: flex; vertical-align: middle; flex-wrap: nowrap; align-items: center; } :host([hidden]) { display: none; } .base { display: flex; vertical-align: middle; flex-wrap: nowrap; align-items: center; } :host(:is(:state(--standard), :--standard)) { justify-content: center; } :host(:is(:state(--connected), :--connected)) .base { flex: 1 1 auto; } :host(:is(:state(--standard), :--standard)) .base { width: fit-content; flex: none; } :host(:is(:state(--standard), :--standard)) .base.pressed { justify-content: space-between; width: var(--_button-group-width); } :host(:is(:state(--standard), :--standard):is(:state(--extra-small), :--extra-small)) .base { column-gap: var(--m3e-standard-button-group-extra-small-spacing, ${A.measurement.space225}); } :host(:is(:state(--standard), :--standard):is(:state(--small), :--small)) .base { column-gap: var(--m3e-standard-button-group-small-spacing, ${A.measurement.space150}); } :host(:is(:state(--standard), :--standard):is(:state(--medium), :--medium)).base { column-gap: var(--m3e-standard-button-group-medium-spacing, ${A.measurement.space100}); } :host(:is(:state(--standard), :--standard):is(:state(--large), :--large)) .base { column-gap: var(--m3e-standard-button-group-large-spacing, ${A.measurement.space100}); } :host(:is(:state(--standard), :--standard):is(:state(--extra-large), :--extra-large)) .base { column-gap: var(--m3e-standard-button-group-extra-large-spacing, ${A.measurement.space100}); } :host(:is(:state(--connected), :--connected)) .base { column-gap: var(--m3e-connected-button-group-spacing, ${A.measurement.space25}); } :host(:is(:state(--connected), :--connected):is(:state(--extra-small), :--extra-small)) ::slotted(:is(:state(--first), :--first):is(:state(--extra-small), :--extra-small)), :host(:is(:state(--connected), :--connected):is(:state(--extra-small), :--extra-small)) ::slotted( :not(:is(:state(--first), :--first)):not(:is(:state(--last), :--last)):is(:state(--extra-small), :--extra-small) ) { --_button-rounded-end-shape: var( --m3e-connected-button-group-extra-small-inner-shape, ${A.shape.corner.small} ); --_button-square-end-shape: var( --m3e-connected-button-group-extra-small-inner-shape, ${A.shape.corner.small} ); --_button-square-end-pressed-shape: var( --m3e-connected-button-group-extra-small-inner-pressed-shape, ${A.shape.corner.extraSmall} ); } :host(:is(:state(--connected), :--connected):is(:state(--extra-small), :--extra-small)) ::slotted(:is(:state(--last), :--last):is(:state(--extra-small), :--extra-small)), :host(:is(:state(--connected), :--connected):is(:state(--extra-small), :--extra-small)) ::slotted( :not(:is(:state(--first), :--first)):not(:is(:state(--last), :--last)):is(:state(--extra-small), :--extra-small) ) { --_button-rounded-start-shape: var( --m3e-connected-button-group-extra-small-inner-shape, ${A.shape.corner.small} ); --_button-square-start-shape: var( --m3e-connected-button-group-extra-small-inner-shape, ${A.shape.corner.small} ); --_button-square-start-pressed-shape: var( --m3e-connected-button-group-extra-small-inner-pressed-shape, ${A.shape.corner.extraSmall} ); } :host(:is(:state(--connected), :--connected):is(:state(--small), :--small)) ::slotted(:is(:state(--first), :--first):is(:state(--small), :--small)), :host(:is(:state(--connected), :--connected):is(:state(--small), :--small)) ::slotted(:not(:is(:state(--first), :--first)):not(:is(:state(--last), :--last)):is(:state(--small), :--small)) { --_button-rounded-end-shape: var( --m3e-connected-button-group-small-inner-shape, ${A.shape.corner.small} ); --_button-square-end-shape: var( --m3e-connected-button-group-small-inner-shape, ${A.shape.corner.small} ); --_button-end-shape-pressed-morph: var( --m3e-connected-button-group-small-inner-pressed-shape, ${A.shape.corner.extraSmall} ); } :host(:is(:state(--connected), :--connected):is(:state(--small), :--small)) ::slotted(:is(:state(--last), :--last):is(:state(--small), :--small)), :host(:is(:state(--connected), :--connected):is(:state(--small), :--small)) ::slotted(:not(:is(:state(--first), :--first)):not(:is(:state(--last), :--last)):is(:state(--small), :--small)) { --_button-rounded-start-shape: var( --m3e-connected-button-group-small-inner-shape, ${A.shape.corner.small} ); --_button-square-start-shape: var( --m3e-connected-button-group-small-inner-shape, ${A.shape.corner.small} ); --_button-start-shape-pressed-morph: var( --m3e-connected-button-group-small-inner-pressed-shape, ${A.shape.corner.extraSmall} ); } :host(:is(:state(--connected), :--connected):is(:state(--medium), :--medium)) ::slotted(:is(:state(--first), :--first):is(:state(--medium), :--medium)), :host(:is(:state(--connected), :--connected):is(:state(--medium), :--medium)) ::slotted( :not(:is(:state(--first), :--first)):not(:is(:state(--last), :--last)):is(:state(--medium), :--medium) ) { --_button-rounded-end-shape: var( --m3e-connected-button-group-medium-inner-shape, ${A.shape.corner.small} ); --_button-square-end-shape: var( --m3e-connected-button-group-medium-inner-shape, ${A.shape.corner.small} ); --_button-square-end-pressed-shape: var( --m3e-connected-button-group-medium-inner-pressed-shape, ${A.shape.corner.extraSmall} ); } :host(:is(:state(--connected), :--connected):is(:state(--medium), :--medium)) ::slotted(:is(:state(--last), :--last):is(:state(--medium), :--medium)), :host(:is(:state(--connected), :--connected):is(:state(--medium), :--medium)) ::slotted( :not(:is(:state(--first), :--first)):not(:is(:state(--last), :--last)):is(:state(--medium), :--medium) ) { --_button-rounded-start-shape: var( --m3e-connected-button-group-medium-inner-shape, ${A.shape.corner.small} ); --_button-square-start-shape: var( --m3e-connected-button-group-medium-inner-shape, ${A.shape.corner.small} ); --_button-square-start-pressed-shape: var( --m3e-connected-button-group-medium-inner-pressed-shape, ${A.shape.corner.extraSmall} ); } :host(:is(:state(--connected), :--connected):is(:state(--large), :--large)) ::slotted(:is(:state(--first), :--first):is(:state(--large), :--large)), :host(:is(:state(--connected), :--connected):is(:state(--large), :--large)) ::slotted(:not(:is(:state(--first), :--first)):not(:is(:state(--last), :--last)):is(:state(--large), :--large)) { --_button-rounded-end-shape: var( --m3e-connected-button-group-large-inner-shape, ${A.shape.corner.large} ); --_button-square-end-shape: var( --m3e-connected-button-group-large-inner-shape, ${A.shape.corner.large} ); --_button-square-end-pressed-shape: var( --m3e-connected-button-group-large-inner-pressed-shape, ${A.shape.corner.medium} ); } :host(:is(:state(--connected), :--connected):is(:state(--large), :--large)) ::slotted(:is(:state(--last), :--last):is(:state(--large), :--large)), :host(:is(:state(--connected), :--connected):is(:state(--large), :--large)) ::slotted(:not(:is(:state(--first), :--first)):not(:is(:state(--last), :--last)):is(:state(--large), :--large)) { --_button-rounded-start-shape: var( --m3e-connected-button-group-large-inner-shape, ${A.shape.corner.large} ); --_button-square-start-shape: var( --m3e-connected-button-group-large-inner-shape, ${A.shape.corner.large} ); --_button-square-start-pressed-shape: var( --m3e-connected-button-group-large-inner-pressed-shape, ${A.shape.corner.medium} ); } :host(:is(:state(--connected), :--connected):is(:state(--extra-large), :--extra-large)) ::slotted(:is(:state(--first), :--first):is(:state(--extra-large), :--extra-large)), :host(:is(:state(--connected), :--connected):is(:state(--extra-large), :--extra-large)) ::slotted( :not(:is(:state(--first), :--first)):not(:is(:state(--last), :--last)):is(:state(--extra-large), :--extra-large) ) { --_button-rounded-end-shape: var( --m3e-connected-button-group-extra-large-inner-shape, ${A.shape.corner.largeIncreased} ); --_button-square-end-shape: var( --m3e-connected-button-group-extra-large-inner-shape, ${A.shape.corner.largeIncreased} ); --_button-square-end-pressed-shape: var( --m3e-connected-button-group-extra-large-inner-pressed-shape, ${A.shape.corner.large} ); } :host(:is(:state(--connected), :--connected):is(:state(--extra-large), :--extra-large)) ::slotted(:is(:state(--last), :--last):is(:state(--extra-large), :--extra-large)), :host(:is(:state(--connected), :--connected):is(:state(--extra-large), :--extra-large)) ::slotted( :not(:is(:state(--first), :--first)):not(:is(:state(--last), :--last)):is(:state(--extra-large), :--extra-large) ) { --_button-rounded-start-shape: var( --m3e-connected-button-group-extra-large-inner-shape, ${A.shape.corner.largeIncreased} ); --_button-square-start-shape: var( --m3e-connected-button-group-extra-large-inner-shape, ${A.shape.corner.largeIncreased} ); --_button-square-start-pressed-shape: var( --m3e-connected-button-group-extra-large-inner-pressed-shape, ${A.shape.corner.large} ); }`, e([C(".base")], tl.prototype, "_base", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], tl.prototype, "variant", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], tl.prototype, "size", void 0), e([S({ type: Boolean })], tl.prototype, "multi", void 0), e([$e({
	slot: "",
	selector: "m3e-button,m3e-icon-button",
	flatten: !0
})], tl.prototype, "buttons", void 0), tl = e([w("m3e-button-group")], tl);
//#endregion
//#region node_modules/@m3e/web/dist/icon-button.js
function nl(e) {
	return e === "extra-small" || e === "small" || e === "medium" || e === "large" || e === "extra-large";
}
function rl(e) {
	return e === "rounded" || e === "square";
}
function il(e) {
	return e === "filled" || e === "tonal" || e === "outlined" || e === "elevated" || e === "standard";
}
function al(e) {
	return e === "default" || e === "narrow" || e === "wide";
}
var ol = {
	"extra-small": {
		containerHeight: c(`calc(var(--m3e-icon-button-extra-small-container-height, var(--m3e-icon-button-container-height, 32px)) + ${A.density.calc(-3)})`),
		outlineThickness: c("var(--m3e-icon-button-extra-small-outline-thickness, var(--m3e-icon-button-outline-thickness, 1px))"),
		iconSize: c("var(--m3e-icon-button-extra-small-icon-size, var(--m3e-icon-button-icon-size, 20px))"),
		shapeRound: c(`var(--m3e-icon-button-extra-small-shape-round, var(--m3e-icon-button-shape-round, ${A.shape.corner.full}))`),
		shapeSquare: c(`var(--m3e-icon-button-extra-small-shape-square, var(--m3e-icon-button-shape-square, ${A.shape.corner.medium}))`),
		selectedShapeRound: c(`var(--m3e-icon-button-extra-small-selected-shape-round, var(--m3e-icon-button-selected-shape-round, ${A.shape.corner.medium}))`),
		selectedShapeSquare: c(`var(--m3e-icon-button-extra-small-selected-shape-square, var(--m3e-icon-button-selected-shape-square, ${A.shape.corner.full}))`),
		shapePressedMorph: c(`var(--m3e-icon-button-extra-small-shape-pressed-morph, var(--m3e-icon-button-shape-pressed-morph, ${A.shape.corner.small}))`),
		narrowLeadingSpace: c(`calc(var(--m3e-icon-button-extra-small-narrow-leading-space, var(--m3e-icon-button-narrow-leading-space, ${A.measurement.space50})) + ${A.density.calcHalf(-3)})`),
		narrowTrailingSpace: c(`calc(var(--m3e-icon-button-extra-small-narrow-trailing-space, var(--m3e-icon-button-narrow-trailing-space, ${A.measurement.space50})) + ${A.density.calcHalf(-3)})`),
		defaultLeadingSpace: c(`calc(var(--m3e-icon-button-extra-small-default-leading-space, var(--m3e-icon-button-default-leading-space, ${A.measurement.space75})) + ${A.density.calcHalf(-3)})`),
		defaultTrailingSpace: c(`calc(var(--m3e-icon-button-extra-small-default-trailing-space, var(--m3e-icon-button-default-trailing-space, ${A.measurement.space75})) + ${A.density.calcHalf(-3)})`),
		wideLeadingSpace: c(`calc(var(--m3e-icon-button-extra-small-wide-leading-space, var(--m3e-icon-button-wide-leading-space, ${A.measurement.space125})) + ${A.density.calcHalf(-3)})`),
		wideTrailingSpace: c(`calc(var(--m3e-icon-button-extra-small-wide-trailing-space, var(--m3e-icon-button-wide-trailing-space, ${A.measurement.space125})) + ${A.density.calcHalf(-3)})`)
	},
	small: {
		containerHeight: c(`calc(var(--m3e-icon-button-small-container-height, var(--m3e-icon-button-container-height, 40px)) + ${A.density.calc(-3)})`),
		outlineThickness: c("var(--m3e-icon-button-small-outline-thickness, var(--m3e-icon-button-outline-thickness, 1px))"),
		iconSize: c("var(--m3e-icon-button-small-icon-size, var(--m3e-icon-button-icon-size, 24px))"),
		shapeRound: c(`var(--m3e-icon-button-small-shape-round, var(--m3e-icon-button-shape-round, ${A.shape.corner.full}))`),
		shapeSquare: c(`var(--m3e-icon-button-small-shape-square, var(--m3e-icon-button-shape-square, ${A.shape.corner.medium}))`),
		selectedShapeRound: c(`var(--m3e-icon-button-small-selected-shape-round, var(--m3e-icon-button-selected-shape-round, ${A.shape.corner.medium}))`),
		selectedShapeSquare: c(`var(--m3e-icon-button-small-selected-shape-square, var(--m3e-icon-button-selected-shape-square, ${A.shape.corner.full}))`),
		shapePressedMorph: c(`var(--m3e-icon-button-small-shape-pressed-morph, var(--m3e-icon-button-shape-pressed-morph, ${A.shape.corner.small}))`),
		narrowLeadingSpace: c(`calc(var(--m3e-icon-button-small-narrow-leading-space, var(--m3e-icon-button-narrow-leading-space, ${A.measurement.space50})) + ${A.density.calcHalf(-3)})`),
		narrowTrailingSpace: c(`calc(var(--m3e-icon-button-small-narrow-trailing-space, var(--m3e-icon-button-narrow-trailing-space, ${A.measurement.space50})) + ${A.density.calcHalf(-3)})`),
		defaultLeadingSpace: c(`calc(var(--m3e-icon-button-small-default-leading-space, var(--m3e-icon-button-default-leading-space, ${A.measurement.space100})) + ${A.density.calcHalf(-3)})`),
		defaultTrailingSpace: c(`calc(var(--m3e-icon-button-small-default-trailing-space, var(--m3e-icon-button-default-trailing-space, ${A.measurement.space100})) + ${A.density.calcHalf(-3)})`),
		wideLeadingSpace: c(`calc(var(--m3e-icon-button-small-wide-leading-space, var(--m3e-icon-button-wide-leading-space, ${A.measurement.space175})) + ${A.density.calcHalf(-3)})`),
		wideTrailingSpace: c(`calc(var(--m3e-icon-button-small-wide-trailing-space, var(--m3e-icon-button-wide-trailing-space, ${A.measurement.space175})) + ${A.density.calcHalf(-3)})`)
	},
	medium: {
		containerHeight: c(`calc(var(--m3e-icon-button-medium-container-height, var(--m3e-icon-button-container-height, 56px)) + ${A.density.calc(-3)})`),
		outlineThickness: c("var(--m3e-icon-button-medium-outline-thickness, var(--m3e-icon-button-outline-thickness, 1px))"),
		iconSize: c("var(--m3e-icon-button-medium-icon-size, var(--m3e-icon-button-icon-size, 24px))"),
		shapeRound: c(`var(--m3e-icon-button-medium-shape-round, var(--m3e-icon-button-shape-round, ${A.shape.corner.full}))`),
		shapeSquare: c(`var(--m3e-icon-button-medium-shape-square, var(--m3e-icon-button-shape-square, ${A.shape.corner.large}))`),
		selectedShapeRound: c(`var(--m3e-icon-button-medium-selected-shape-round, var(--m3e-icon-button-selected-shape-round, ${A.shape.corner.large}))`),
		selectedShapeSquare: c(`var(--m3e-icon-button-medium-selected-shape-square, var(--m3e-icon-button-selected-shape-square, ${A.shape.corner.full}))`),
		shapePressedMorph: c(`var(--m3e-icon-button-medium-shape-pressed-morph, var(--m3e-icon-button-shape-pressed-morph, ${A.shape.corner.medium}))`),
		narrowLeadingSpace: c(`calc(var(--m3e-icon-button-medium-narrow-leading-space, var(--m3e-icon-button-narrow-leading-space, ${A.measurement.space150})) + ${A.density.calcHalf(-3)})`),
		narrowTrailingSpace: c(`calc(var(--m3e-icon-button-medium-narrow-trailing-space, var(--m3e-icon-button-narrow-trailing-space, ${A.measurement.space150})) + ${A.density.calcHalf(-3)})`),
		defaultLeadingSpace: c(`calc(var(--m3e-icon-button-medium-default-leading-space, var(--m3e-icon-button-default-leading-space, ${A.measurement.space200})) + ${A.density.calcHalf(-3)})`),
		defaultTrailingSpace: c(`calc(var(--m3e-icon-button-medium-default-trailing-space, var(--m3e-icon-button-default-trailing-space, ${A.measurement.space200})) + ${A.density.calcHalf(-3)})`),
		wideLeadingSpace: c(`calc(var(--m3e-icon-button-medium-wide-leading-space, var(--m3e-icon-button-wide-leading-space, ${A.measurement.space300})) + ${A.density.calcHalf(-3)})`),
		wideTrailingSpace: c(`calc(var(--m3e-icon-button-medium-wide-trailing-space, var(--m3e-icon-button-wide-trailing-space, ${A.measurement.space300})) + ${A.density.calcHalf(-3)})`)
	},
	large: {
		containerHeight: c(`calc(var(--m3e-icon-button-large-container-height, var(--m3e-icon-button-container-height, 96px)) + ${A.density.calc(-3)})`),
		outlineThickness: c("var(--m3e-icon-button-large-outline-thickness, var(--m3e-icon-button-outline-thickness, 2px))"),
		iconSize: c("var(--m3e-icon-button-large-icon-size, var(--m3e-icon-button-icon-size, 32px))"),
		shapeRound: c(`var(--m3e-icon-button-large-shape-round, var(--m3e-icon-button-shape-round, ${A.shape.corner.full}))`),
		shapeSquare: c(`var(--m3e-icon-button-large-shape-square, var(--m3e-icon-button-shape-square, ${A.shape.corner.extraLarge}))`),
		selectedShapeRound: c(`var(--m3e-icon-button-large-selected-shape-round, var(--m3e-icon-button-selected-shape-round, ${A.shape.corner.extraLarge}))`),
		selectedShapeSquare: c(`var(--m3e-icon-button-large-selected-shape-square, var(--m3e-icon-button-selected-shape-square, ${A.shape.corner.full}))`),
		shapePressedMorph: c(`var(--m3e-icon-button-large-shape-pressed-morph, var(--m3e-icon-button-shape-pressed-morph, ${A.shape.corner.large}))`),
		narrowLeadingSpace: c(`calc(var(--m3e-icon-button-large-narrow-leading-space, var(--m3e-icon-button-narrow-leading-space, ${A.measurement.space200})) + ${A.density.calcHalf(-3)})`),
		narrowTrailingSpace: c(`calc(var(--m3e-icon-button-large-narrow-trailing-space, var(--m3e-icon-button-narrow-trailing-space, ${A.measurement.space200})) + ${A.density.calcHalf(-3)})`),
		defaultLeadingSpace: c(`calc(var(--m3e-icon-button-large-default-leading-space, var(--m3e-icon-button-default-leading-space, ${A.measurement.space400})) + ${A.density.calcHalf(-3)})`),
		defaultTrailingSpace: c(`calc(var(--m3e-icon-button-large-default-trailing-space, var(--m3e-icon-button-default-trailing-space, ${A.measurement.space400})) + ${A.density.calcHalf(-3)})`),
		wideLeadingSpace: c(`calc(var(--m3e-icon-button-large-wide-leading-space, var(--m3e-icon-button-wide-leading-space, ${A.measurement.space600})) + ${A.density.calcHalf(-3)})`),
		wideTrailingSpace: c(`calc(var(--m3e-icon-button-large-wide-trailing-space, var(--m3e-icon-button-wide-trailing-space, ${A.measurement.space600})) + ${A.density.calcHalf(-3)})`)
	},
	"extra-large": {
		containerHeight: c(`calc(var(--m3e-icon-button-extra-large-container-height, var(--m3e-icon-button-container-height, 136px)) + ${A.density.calc(-3)})`),
		outlineThickness: c("var(--m3e-icon-button-extra-large-outline-thickness, var(--m3e-icon-button-outline-thickness, 3px))"),
		iconSize: c("var(--m3e-icon-button-extra-large-icon-size, var(--m3e-icon-button-icon-size, 40px))"),
		shapeRound: c(`var(--m3e-icon-button-extra-large-shape-round, var(--m3e-icon-button-shape-round, ${A.shape.corner.full}))`),
		shapeSquare: c(`var(--m3e-icon-button-extra-large-shape-square, var(--m3e-icon-button-shape-square, ${A.shape.corner.extraLarge}))`),
		selectedShapeRound: c(`var(--m3e-icon-button-extra-large-selected-shape-round, var(--m3e-icon-button-selected-shape-round, ${A.shape.corner.extraLarge}))`),
		selectedShapeSquare: c(`var(--m3e-icon-button-extra-large-selected-shape-square, var(--m3e-icon-button-selected-shape-square, ${A.shape.corner.full}))`),
		shapePressedMorph: c(`var(--m3e-icon-button-extra-large-shape-pressed-morph, var(--m3e-icon-button-shape-pressed-morph, ${A.shape.corner.large}))`),
		narrowLeadingSpace: c(`calc(var(--m3e-icon-button-extra-large-narrow-leading-space, var(--m3e-icon-button-narrow-leading-space, ${A.measurement.space400})) + ${A.density.calcHalf(-3)})`),
		narrowTrailingSpace: c(`calc(var(--m3e-icon-button-extra-large-narrow-trailing-space, var(--m3e-icon-button-narrow-trailing-space, ${A.measurement.space400})) + ${A.density.calcHalf(-3)})`),
		defaultLeadingSpace: c(`calc(var(--m3e-icon-button-extra-large-default-leading-space, var(--m3e-icon-button-default-leading-space, ${A.measurement.space600})) + ${A.density.calcHalf(-3)})`),
		defaultTrailingSpace: c(`calc(var(--m3e-icon-button-extra-large-default-trailing-space, var(--m3e-icon-button-default-trailing-space, ${A.measurement.space600})) + ${A.density.calcHalf(-3)})`),
		wideLeadingSpace: c(`calc(var(--m3e-icon-button-extra-large-wide-leading-space, var(--m3e-icon-button-wide-leading-space, ${A.measurement.space900})) + ${A.density.calcHalf(-3)})`),
		wideTrailingSpace: c(`calc(var(--m3e-icon-button-extra-large-wide-trailing-space, var(--m3e-icon-button-wide-trailing-space, ${A.measurement.space900})) + ${A.density.calcHalf(-3)})`)
	}
};
function sl(e) {
	let t = c(`:is(:state(--${e}), :--${e})`);
	return l`:host(${t}) .base { height: ${ol[e].containerHeight}; } :host(${t}:is(:state(--default), :--default)) .wrapper { padding-inline-start: calc( ${ol[e].defaultLeadingSpace} - calc(var(--_adjacent-shrink, 0px) / 2) ); padding-inline-end: calc( ${ol[e].defaultTrailingSpace} - calc(var(--_adjacent-shrink, 0px) / 2) ); } :host(${t}:is(:state(--narrow), :--narrow)) .wrapper { padding-inline-start: calc( ${ol[e].narrowLeadingSpace} - calc(var(--_adjacent-shrink, 0px) / 2) ); padding-inline-end: calc( ${ol[e].narrowTrailingSpace} - calc(var(--_adjacent-shrink, 0px) / 2) ); } :host(${t}:is(:state(--wide), :--wide)) .wrapper { padding-inline-start: calc( ${ol[e].wideLeadingSpace} - calc(var(--_adjacent-shrink, 0px) / 2) ); padding-inline-end: calc(${ol[e].wideTrailingSpace} - calc(var(--_adjacent-shrink, 0px) / 2)); } :host(${t}) .icon { font-size: ${ol[e].iconSize}; } :host(${t}) .base { outline-offset: calc(0px - ${ol[e].outlineThickness}); outline-width: ${ol[e].outlineThickness}; } :host( :not(:is(:state(--connected), :--connected))${t}:is(:state(--rounded), :--rounded):not( :is(:state(--pressed), :--pressed) ) ) .base { border-radius: var(--_button-shape, ${ol[e].shapeRound}); } :host( :is(:state(--connected), :--connected)${t}:is(:state(--rounded), :--rounded)[toggle][selected]:not( :is(:state(--pressed), :--pressed) ) ) .base { border-radius: var(--_button-shape, ${ol[e].shapeRound}); } :host(:not(:is(:state(--connected), :--connected))${t}:is(:state(--square), :--square)) .base { border-radius: ${ol[e].shapeSquare}; } :host( :not(:is(:state(--connected), :--connected))${t}:is(:state(--rounded), :--rounded)[toggle][selected]:not( :is(:state(--pressed), :--pressed) ) ) .base { border-radius: ${ol[e].selectedShapeRound}; } :host( :not(:is(:state(--connected), :--connected))${t}:is(:state(--square), :--square)[toggle][selected]:not( :is(:state(--pressed), :--pressed) ) ) .base { border-radius: var(--_button-shape, ${ol[e].selectedShapeSquare}); } :host(:not(:is(:state(--connected), :--connected))${t}:is(:state(--pressed), :--pressed)) .base { border-radius: ${ol[e].shapePressedMorph}; } :host(:is(:state(--connected), :--connected)${t}:is(:state(--rounded), :--rounded):not([toggle][selected])) .base { border-start-start-radius: var( --_button-rounded-start-shape, var(--_button-shape, ${ol[e].shapeRound}) ); border-end-start-radius: var( --_button-rounded-start-shape, var(--_button-shape, ${ol[e].shapeRound}) ); border-start-end-radius: var( --_button-rounded-end-shape, var(--_button-shape, ${ol[e].shapeRound}) ); border-end-end-radius: var( --_button-rounded-end-shape, var(--_button-shape, ${ol[e].shapeRound}) ); } :host(:is(:state(--connected), :--connected)${t}:is(:state(--square), :--square)) .base { border-start-start-radius: var(--_button-square-start-shape, ${ol[e].shapeSquare}); border-end-start-radius: var(--_button-square-start-shape, ${ol[e].shapeSquare}); border-start-end-radius: var(--_button-square-end-shape, ${ol[e].shapeSquare}); border-end-end-radius: var(--_button-square-end-shape, ${ol[e].shapeSquare}); } :host( :is(:state(--connected), :--connected)${t}:is(:state(--square), :--square)[toggle][selected]:not( :is(:state(--pressed), :--pressed) ) ) .base { border-radius: var(--_button-shape, ${ol[e].selectedShapeSquare}); } :host(:is(:state(--connected), :--connected)${t}:is(:state(--pressed), :--pressed)) .base { border-start-start-radius: var( --_button-start-shape-pressed-morph, ${ol[e].shapePressedMorph} ); border-end-start-radius: var(--_button-start-shape-pressed-morph, ${ol[e].shapePressedMorph}); border-start-end-radius: var(--_button-end-shape-pressed-morph, ${ol[e].shapePressedMorph}); border-end-end-radius: var(--_button-end-shape-pressed-morph, ${ol[e].shapePressedMorph}); }`;
}
var cl = [
	sl("extra-small"),
	sl("small"),
	sl("medium"),
	sl("large"),
	sl("extra-large")
], ll = l`:host { display: inline-block; outline: none; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } :host([hidden]) { display: none; } .layout, .base { box-sizing: border-box; vertical-align: middle; display: inline-flex; align-items: center; justify-content: center; } .layout { width: var(--_icon-button-size, 100%); min-width: var(--_icon-button-min-size); min-height: var(--_icon-button-min-size); height: var(--_icon-button-size, 100%); } .base { position: relative; width: var(--_icon-button-size, 100%); transition: ${c(`background-color ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; } .touch { position: absolute; aspect-ratio: 1 / 1; height: 48px; left: auto; right: auto; } :host(:is(:state(--pressed), :--pressed)) .base, :host(:is(:state(--resting), :--resting)) .base { transition: ${c(`background-color ${A.motion.duration.short4} ${A.motion.easing.standard},
          border-radius ${A.motion.spring.fastEffects}`)}; } .wrapper { width: 100%; overflow: hidden; display: inline-flex; align-items: center; justify-content: center; transition: ${c(`padding-inline ${A.motion.spring.fastEffects}`)}; } .icon { transition: ${c(`color ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; --m3e-icon-size: 1em; } :host(:not(:disabled):not([disabled-interactive])) { cursor: pointer; } :host([disabled-interactive]) { cursor: not-allowed; } ::slotted(*) { font-size: inherit !important; flex: none; transform: var(--_icon-button-icon-transform); transform-origin: center center; transition: ${c(`transform var(--_icon-button-icon-transform-transition, ${A.motion.spring.fastEffects})`)}; } ::slotted(svg) { width: 1em; height: 1em; } :host([toggle]:not([selected])) .base.with-selected-icon slot[name="selected"], :host([toggle][selected]) .base.with-selected-icon slot:not([name]) { display: none; } a { all: unset; display: block; position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px; z-index: 1; } :host(:is(:state(--grouped), :--grouped):is(:state(--connected), :--connected)) { flex: 1 1 auto; } :host(:is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected))) { transition: ${c(`width ${A.motion.spring.fastEffects}`)}; } :host(:is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected))) .wrapper { transition: ${c(`padding-inline ${A.motion.spring.fastEffects}`)}; } :host(:is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected))) { flex-shrink: 0; flex-grow: 0; } :host( :is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected)):not( :is(:state(--pressed), :--pressed, :state(--adjacent-pressed), :--adjacent-pressed) ) ) { width: var(--_button-width); } :host( :is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected)):not( :is(:state(--pressed), :--pressed) ):is(:state(--adjacent-pressed), :--adjacent-pressed) ) { width: calc(var(--_button-width) - var(--_adjacent-shrink, 0px)); } :host( :is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected)):is( :state(--pressed), :--pressed ):not([disabled-interactive]):not(:disabled) ) { width: calc( var(--_button-width) + calc(var(--_button-width) * var(--m3e-standard-button-group-width-multiplier, 0.15)) ); } @media (forced-colors: active) { .base, .icon { transition: none; } :host(:is(:state(--pressed), :--pressed)) .base, :host(:is(:state(--resting), :--resting)) .base { transition: border-radius ${A.motion.spring.fastEffects}; } :host(:not(:disabled):not([disabled-interactive]):not([toggle])) .base { background-color: ButtonFace !important; outline-color: ButtonText !important; } :host(:not(:disabled):not([disabled-interactive]):not([toggle])) .icon { color: ButtonText !important; } :host(:not(:disabled):not([disabled-interactive])[toggle]:not([selected])) .base { background-color: ButtonFace !important; outline-color: ButtonText !important; } :host(:not(:disabled):not([disabled-interactive])[toggle]:not([selected])) .icon, :host(:hover:not(:disabled):not([disabled-interactive])[toggle]:not([selected])) .icon, :host(:not(:disabled):not([disabled-interactive])[toggle]:not([selected]):focus) .icon { color: ButtonText !important; } :host(:not(:disabled):not([disabled-interactive])[toggle][selected]) .base { background-color: ButtonText !important; outline: none !important; } :host(:not(:disabled):not([disabled-interactive])[toggle][selected]) .icon, :host(:hover:not(:disabled):not([disabled-interactive])[toggle][selected]) .icon, :host(:not(:disabled):not([disabled-interactive])[toggle][selected]:focus) .icon { forced-color-adjust: none; color: ButtonFace !important; background-color: ButtonText !important; } :host(:disabled) .base, :host([disabled-interactive]) .base { outline-color: GrayText !important; background-color: unset !important; } :host(:disabled) .icon, :host([disabled-interactive]) .icon { color: GrayText !important; } .base { outline-style: solid; } :host(:is(:state(--extra-small), :--extra-small)) .base { outline-offset: calc(0px - var(--m3e-icon-button-extra-small-outline-thickness, 1px)); outline-width: var(--m3e-icon-button-extra-small-outline-thickness, 1px); } :host(:is(:state(--small), :--small)) .base { outline-offset: calc(0px - var(--m3e-icon-button-small-outline-thickness, 1px)); outline-width: var(--m3e-icon-button-small-outline-thickness, 1px); } :host(:is(:state(--medium), :--medium)) .base { outline-offset: calc(0px - var(--m3e-icon-button-medium-outline-thickness, 1px)); outline-width: var(--m3e-icon-button-medium-outline-thickness, 1px); } :host(:is(:state(--large), :--large)) .base { outline-offset: calc(0px - var(--m3e-icon-button-large-outline-thickness, 2px)); outline-width: var(--m3e-icon-button-large-outline-thickness, 2px); } :host(:is(:state(--extra-large), :--extra-large)) .base { outline-offset: calc(0px - var(--m3e-icon-button-extra-large-outline-thickness, 3px)); outline-width: var(--m3e-icon-button-extra-large-outline-thickness, 3px); } } @media (prefers-reduced-motion) { :host(:is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected))), :host(:is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected))) .wrapper, :host(:is(:state(--pressed), :--pressed)) .base, :host(:is(:state(--resting), :--resting)) .base, .base, .wrapper, .icon { transition: none; } }`, H = {
	elevated: {
		iconColor: c(`var(--m3e-elevated-icon-button-icon-color, var(--m3e-icon-button-icon-color, ${A.color.primary}))`),
		containerColor: c(`var(--m3e-elevated-icon-button-container-color, var(--m3e-icon-button-container-color, ${A.color.surfaceContainerLow}))`),
		containerElevation: c(`var(--m3e-elevated-icon-button-container-elevation, var(--m3e-icon-button-container-elevation, ${A.elevation.level1}))`),
		unselectedIconColor: c(`var(--m3e-elevated-icon-button-unselected-icon-color, var(--m3e-icon-button-unselected-icon-color, ${A.color.primary}))`),
		unselectedContainerColor: c(`var(--m3e-elevated-icon-button-unselected-container-color, var(--m3e-icon-button-unselected-container-color, ${A.color.surfaceContainerLow}))`),
		selectedIconColor: c(`var(--m3e-elevated-icon-button-selected-icon-color, var(--m3e-icon-button-selected-icon-color, ${A.color.onPrimary}))`),
		selectedContainerColor: c(`var(--m3e-elevated-icon-button-selected-container-color, var(--m3e-icon-button-selected-container-color, ${A.color.primary}))`),
		disabled: {
			containerColor: c(`var(--m3e-elevated-icon-button-disabled-container-color, var(--m3e-icon-button-disabled-container-color, ${A.color.onSurface}))`),
			containerOpacity: c("var(--m3e-elevated-icon-button-disabled-container-opacity, var(--m3e-icon-button-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-elevated-icon-button-disabled-icon-color, var(--m3e-icon-button-disabled-icon-color, ${A.color.onSurface}))`),
			iconOpacity: c("var(--m3e-elevated-icon-button-disabled-icon-opacity, var(--m3e-icon-button-disabled-icon-opacity, 38%))"),
			containerElevation: c(`var(--m3e-elevated-icon-button-disabled-container-elevation, var(--m3e-icon-button-disabled-container-elevation, ${A.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-elevated-icon-button-hover-icon-color, var(--m3e-icon-button-hover-icon-color, ${A.color.primary}))`),
			stateLayerColor: c(`var(--m3e-elevated-icon-button-hover-state-layer-color, var(--m3e-icon-button-hover-state-layer-color, ${A.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-elevated-icon-button-hover-state-layer-opacity, var(--m3e-icon-button-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-elevated-icon-button-hover-container-elevation, var(--m3e-icon-button-hover-container-elevation, ${A.elevation.level2}))`),
			unselectedIconColor: c(`var(--m3e-elevated-icon-button-hover-unselected-icon-color, var(--m3e-icon-button-hover-unselected-icon-color, ${A.color.primary}))`),
			unselectedStateLayerColor: c(`var(--m3e-elevated-icon-button-hover-unselected-state-layer-color, var(--m3e-icon-button-hover-unselected-state-layer-color, ${A.color.primary}))`),
			selectedIconColor: c(`var(--m3e-elevated-icon-button-hover-selected-icon-color, var(--m3e-icon-button-hover-selected-icon-color, ${A.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-elevated-icon-button-hover-selected-state-layer-color, var(--m3e-icon-button-hover-selected-state-layer-color, ${A.color.onPrimary}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-elevated-icon-button-focus-icon-color, var(--m3e-icon-button-focus-icon-color, ${A.color.primary}))`),
			stateLayerColor: c(`var(--m3e-elevated-icon-button-focus-state-layer-color, var(--m3e-icon-button-focus-state-layer-color, ${A.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-elevated-icon-button-focus-state-layer-opacity, var(--m3e-icon-button-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-elevated-icon-button-focus-container-elevation, var(--m3e-icon-button-focus-container-elevation, ${A.elevation.level1}))`),
			unselectedIconColor: c(`var(--m3e-elevated-icon-button-focus-unselected-icon-color, var(--m3e-icon-button-focus-unselected-icon-color, ${A.color.primary}))`),
			unselectedStateLayerColor: c(`var(--m3e-elevated-icon-button-focus-unselected-state-layer-color, var(--m3e-icon-button-focus-unselected-state-layer-color, ${A.color.primary}))`),
			selectedIconColor: c(`var(--m3e-elevated-icon-button-focus-selected-icon-color, var(--m3e-icon-button-focus-selected-icon-color, ${A.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-elevated-icon-button-focus-selected-state-layer-color, var(--m3e-icon-button-focus-selected-state-layer-color, ${A.color.onPrimary}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-elevated-icon-button-pressed-icon-color, var(--m3e-icon-button-pressed-icon-color, ${A.color.primary}))`),
			stateLayerColor: c(`var(--m3e-elevated-icon-button-pressed-state-layer-color, var(--m3e-icon-button-pressed-state-layer-color, ${A.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-elevated-icon-button-pressed-state-layer-opacity, var(--m3e-icon-button-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-elevated-icon-button-pressed-container-elevation, var(--m3e-icon-button-pressed-container-elevation, ${A.elevation.level1}))`),
			unselectedIconColor: c(`var(--m3e-elevated-icon-button-pressed-unselected-icon-color, var(--m3e-icon-button-pressed-unselected-icon-color, ${A.color.primary}))`),
			unselectedStateLayerColor: c(`var(--m3e-elevated-icon-button-pressed-unselected-state-layer-color, var(--m3e-icon-button-pressed-unselected-state-layer-color, ${A.color.primary}))`),
			selectedIconColor: c(`var(--m3e-elevated-icon-button-pressed-selected-icon-color, var(--m3e-icon-button-pressed-selected-icon-color, ${A.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-elevated-icon-button-pressed-selected-state-layer-color, var(--m3e-icon-button-pressed-selected-state-layer-color, ${A.color.onPrimary}))`)
		}
	},
	outlined: {
		iconColor: c(`var(--m3e-outlined-icon-button-icon-color, var(--m3e-icon-button-icon-color, ${A.color.onSurfaceVariant}))`),
		outlineColor: c(`var(--m3e-outlined-icon-button-outline-color, var(--m3e-icon-button-outline-color, ${A.color.outlineVariant}))`),
		unselectedIconColor: c(`var(--m3e-outlined-icon-button-unselected-icon-color, var(--m3e-icon-button-unselected-icon-color, ${A.color.onSurfaceVariant}))`),
		selectedIconColor: c(`var(--m3e-outlined-icon-button-selected-icon-color, var(--m3e-icon-button-selected-icon-color, ${A.color.inverseOnSurface}))`),
		selectedContainerColor: c(`var(--m3e-outlined-icon-button-selected-container-color, var(--m3e-icon-button-selected-container-color, ${A.color.inverseSurface}))`),
		disabled: {
			containerColor: c(`var(--m3e-outlined-icon-button-disabled-container-color, var(--m3e-icon-button-disabled-container-color, ${A.color.onSurface}))`),
			containerOpacity: c("var(--m3e-outlined-icon-button-disabled-container-opacity, var(--m3e-icon-button-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-outlined-icon-button-disabled-icon-color, var(--m3e-icon-button-disabled-icon-color, ${A.color.onSurface}))`),
			iconOpacity: c("var(--m3e-outlined-icon-button-disabled-icon-opacity, var(--m3e-icon-button-disabled-icon-opacity, 38%))"),
			outlineColor: c(`var(--m3e-outlined-icon-button-disabled-outline-color, var(--m3e-icon-button-disabled-outline-color, ${A.color.outlineVariant}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-outlined-icon-button-hover-icon-color, var(--m3e-icon-button-hover-icon-color, ${A.color.onSurfaceVariant}))`),
			outlineColor: c(`var(--m3e-outlined-icon-button-hover-outline-color, var(--m3e-icon-button-hover-outline-color, ${A.color.outlineVariant}))`),
			stateLayerColor: c(`var(--m3e-outlined-icon-button-hover-state-layer-color, var(--m3e-icon-button-hover-state-layer-color, ${A.color.onSurfaceVariant}))`),
			stateLayerOpacity: c(`var(--m3e-outlined-icon-button-hover-state-layer-opacity, var(--m3e-icon-button-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-outlined-icon-button-hover-unselected-icon-color, var(--m3e-icon-button-hover-unselected-icon-color, ${A.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-outlined-icon-button-hover-unselected-state-layer-color, var(--m3e-icon-button-hover-unselected-state-layer-color, ${A.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-outlined-icon-button-hover-selected-icon-color, var(--m3e-icon-button-hover-selected-icon-color, ${A.color.inverseOnSurface}))`),
			selectedStateLayerColor: c(`var(--m3e-outlined-icon-button-hover-selected-state-layer-color, var(--m3e-icon-button-hover-selected-state-layer-color, ${A.color.inverseOnSurface}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-outlined-icon-button-focus-icon-color, var(--m3e-icon-button-focus-icon-color, ${A.color.onSurfaceVariant}))`),
			outlineColor: c(`var(--m3e-outlined-icon-button-focus-outline-color, var(--m3e-icon-button-focus-outline-color, ${A.color.outlineVariant}))`),
			stateLayerColor: c(`var(--m3e-outlined-icon-button-focus-state-layer-color, var(--m3e-icon-button-focus-state-layer-color, ${A.color.onSurfaceVariant}))`),
			stateLayerOpacity: c(`var(--m3e-outlined-icon-button-focus-state-layer-opacity, var(--m3e-icon-button-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-outlined-icon-button-focus-unselected-icon-color, var(--m3e-icon-button-focus-unselected-icon-color, ${A.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-outlined-icon-button-focus-unselected-state-layer-color, var(--m3e-icon-button-focus-unselected-state-layer-color, ${A.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-outlined-icon-button-focus-selected-icon-color, var(--m3e-icon-button-focus-selected-icon-color, ${A.color.inverseOnSurface}))`),
			selectedStateLayerColor: c(`var(--m3e-outlined-icon-button-focus-selected-state-layer-color, var(--m3e-icon-button-focus-selected-state-layer-color, ${A.color.inverseOnSurface}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-outlined-icon-button-pressed-icon-color, var(--m3e-icon-button-pressed-icon-color, ${A.color.onSurfaceVariant}))`),
			outlineColor: c(`var(--m3e-outlined-icon-button-pressed-outline-color, var(--m3e-icon-button-pressed-outline-color, ${A.color.outlineVariant}))`),
			stateLayerColor: c(`var(--m3e-outlined-icon-button-pressed-state-layer-color, var(--m3e-icon-button-pressed-state-layer-color, ${A.color.onSurfaceVariant}))`),
			stateLayerOpacity: c(`var(--m3e-outlined-icon-button-pressed-state-layer-opacity, var(--m3e-icon-button-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-outlined-icon-button-pressed-unselected-icon-color, var(--m3e-icon-button-pressed-unselected-icon-color, ${A.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-outlined-icon-button-pressed-unselected-state-layer-color, var(--m3e-icon-button-pressed-unselected-state-layer-color, ${A.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-outlined-icon-button-pressed-selected-icon-color, var(--m3e-icon-button-pressed-selected-icon-color, ${A.color.inverseOnSurface}))`),
			selectedStateLayerColor: c(`var(--m3e-outlined-icon-button-pressed-selected-state-layer-color, var(--m3e-icon-button-pressed-selected-state-layer-color, ${A.color.inverseOnSurface}))`)
		}
	},
	filled: {
		iconColor: c(`var(--m3e-filled-icon-button-icon-color, var(--m3e-icon-button-icon-color, ${A.color.onPrimary}))`),
		containerColor: c(`var(--m3e-filled-icon-button-container-color, var(--m3e-icon-button-container-color, ${A.color.primary}))`),
		unselectedIconColor: c(`var(--m3e-filled-icon-button-unselected-icon-color, var(--m3e-icon-button-unselected-icon-color, ${A.color.onSurfaceVariant}))`),
		unselectedContainerColor: c(`var(--m3e-filled-icon-button-unselected-container-color, var(--m3e-icon-button-unselected-container-color, ${A.color.surfaceContainer}))`),
		selectedIconColor: c(`var(--m3e-filled-icon-button-selected-icon-color, var(--m3e-icon-button-selected-icon-color, ${A.color.onPrimary}))`),
		selectedContainerColor: c(`var(--m3e-filled-icon-button-selected-container-color, var(--m3e-icon-button-selected-container-color, ${A.color.primary}))`),
		disabled: {
			containerColor: c(`var(--m3e-filled-icon-button-disabled-container-color, var(--m3e-icon-button-disabled-container-color, ${A.color.onSurface}))`),
			containerOpacity: c("var(--m3e-filled-icon-button-disabled-container-opacity, var(--m3e-icon-button-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-filled-icon-button-disabled-icon-color, var(--m3e-icon-button-disabled-icon-color, ${A.color.onSurface}))`),
			iconOpacity: c("var(--m3e-filled-icon-button-disabled-icon-opacity, var(--m3e-icon-button-disabled-icon-opacity, 38%))")
		},
		hover: {
			iconColor: c(`var(--m3e-filled-icon-button-hover-icon-color, var(--m3e-icon-button-hover-icon-color, ${A.color.onPrimary}))`),
			stateLayerColor: c(`var(--m3e-filled-icon-button-hover-state-layer-color, var(--m3e-icon-button-hover-state-layer-color, ${A.color.onPrimary}))`),
			stateLayerOpacity: c(`var(--m3e-filled-icon-button-hover-state-layer-opacity, var(--m3e-icon-button-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-filled-icon-button-hover-unselected-icon-color, var(--m3e-icon-button-hover-unselected-icon-color, ${A.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-filled-icon-button-hover-unselected-state-layer-color, var(--m3e-icon-button-hover-unselected-state-layer-color, ${A.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-filled-icon-button-hover-selected-icon-color, var(--m3e-icon-button-hover-selected-icon-color, ${A.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-filled-icon-button-hover-selected-state-layer-color, var(--m3e-icon-button-hover-selected-state-layer-color, ${A.color.onPrimary}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-filled-icon-button-focus-icon-color, var(--m3e-icon-button-focus-icon-color, ${A.color.onPrimary}))`),
			stateLayerColor: c(`var(--m3e-filled-icon-button-focus-state-layer-color, var(--m3e-icon-button-focus-state-layer-color, ${A.color.onPrimary}))`),
			stateLayerOpacity: c(`var(--m3e-filled-icon-button-focus-state-layer-opacity, var(--m3e-icon-button-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-filled-icon-button-focus-unselected-icon-color, var(--m3e-icon-button-focus-unselected-icon-color, ${A.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-filled-icon-button-focus-unselected-state-layer-color, var(--m3e-icon-button-focus-unselected-state-layer-color, ${A.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-filled-icon-button-focus-selected-icon-color, var(--m3e-icon-button-focus-selected-icon-color, ${A.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-filled-icon-button-focus-selected-state-layer-color, var(--m3e-icon-button-focus-selected-state-layer-color, ${A.color.onPrimary}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-filled-icon-button-pressed-icon-color, var(--m3e-icon-button-pressed-icon-color, ${A.color.onPrimary}))`),
			stateLayerColor: c(`var(--m3e-filled-icon-button-pressed-state-layer-color, var(--m3e-icon-button-pressed-state-layer-color, ${A.color.onPrimary}))`),
			stateLayerOpacity: c(`var(--m3e-filled-icon-button-pressed-state-layer-opacity, var(--m3e-icon-button-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-filled-icon-button-pressed-unselected-icon-color, var(--m3e-icon-button-pressed-unselected-icon-color, ${A.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-filled-icon-button-pressed-unselected-state-layer-color, var(--m3e-icon-button-pressed-unselected-state-layer-color, ${A.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-filled-icon-button-pressed-selected-icon-color, var(--m3e-icon-button-pressed-selected-icon-color, ${A.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-filled-icon-button-pressed-selected-state-layer-color, var(--m3e-icon-button-pressed-selected-state-layer-color, ${A.color.onPrimary}))`)
		}
	},
	tonal: {
		iconColor: c(`var(--m3e-tonal-icon-button-icon-color, var(--m3e-icon-button-icon-color, ${A.color.onSecondaryContainer}))`),
		containerColor: c(`var(--m3e-tonal-icon-button-container-color, var(--m3e-icon-button-container-color, ${A.color.secondaryContainer}))`),
		unselectedIconColor: c(`var(--m3e-tonal-icon-button-unselected-icon-color, var(--m3e-icon-button-unselected-icon-color, ${A.color.onSecondaryContainer}))`),
		unselectedContainerColor: c(`var(--m3e-tonal-icon-button-unselected-container-color, var(--m3e-icon-button-unselected-container-color, ${A.color.secondaryContainer}))`),
		selectedIconColor: c(`var(--m3e-tonal-icon-button-selected-icon-color, var(--m3e-icon-button-selected-icon-color, ${A.color.onSecondary}))`),
		selectedContainerColor: c(`var(--m3e-tonal-icon-button-selected-container-color, var(--m3e-icon-button-selected-container-color, ${A.color.secondary}))`),
		disabled: {
			containerColor: c(`var(--m3e-tonal-icon-button-disabled-container-color, var(--m3e-icon-button-disabled-container-color, ${A.color.onSurface}))`),
			containerOpacity: c("var(--m3e-tonal-icon-button-disabled-container-opacity, var(--m3e-icon-button-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-tonal-icon-button-disabled-icon-color, var(--m3e-icon-button-disabled-icon-color, ${A.color.onSurface}))`),
			iconOpacity: c("var(--m3e-tonal-icon-button-disabled-icon-opacity, var(--m3e-icon-button-disabled-icon-opacity, 38%))")
		},
		hover: {
			iconColor: c(`var(--m3e-tonal-icon-button-hover-icon-color, var(--m3e-icon-button-hover-icon-color, ${A.color.onSecondaryContainer}))`),
			stateLayerColor: c(`var(--m3e-tonal-icon-button-hover-state-layer-color, var(--m3e-icon-button-hover-state-layer-color, ${A.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-tonal-icon-button-hover-state-layer-opacity, var(--m3e-icon-button-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-tonal-icon-button-hover-unselected-icon-color, var(--m3e-icon-button-hover-unselected-icon-color, ${A.color.onSecondaryContainer}))`),
			unselectedStateLayerColor: c(`var(--m3e-tonal-icon-button-hover-unselected-state-layer-color, var(--m3e-icon-button-hover-unselected-state-layer-color, ${A.color.onSecondaryContainer}))`),
			selectedIconColor: c(`var(--m3e-tonal-icon-button-hover-selected-icon-color, var(--m3e-icon-button-hover-selected-icon-color, ${A.color.onSecondary}))`),
			selectedStateLayerColor: c(`var(--m3e-tonal-icon-button-hover-selected-state-layer-color, var(--m3e-icon-button-hover-selected-state-layer-color, ${A.color.onSecondary}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-tonal-icon-button-focus-icon-color, var(--m3e-icon-button-focus-icon-color, ${A.color.onSecondaryContainer}))`),
			stateLayerColor: c(`var(--m3e-tonal-icon-button-focus-state-layer-color, var(--m3e-icon-button-focus-state-layer-color, ${A.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-tonal-icon-button-focus-state-layer-opacity, var(--m3e-icon-button-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-tonal-icon-button-focus-unselected-icon-color, var(--m3e-icon-button-focus-unselected-icon-color, ${A.color.onSecondaryContainer}))`),
			unselectedStateLayerColor: c(`var(--m3e-tonal-icon-button-focus-unselected-state-layer-color, var(--m3e-icon-button-focus-unselected-state-layer-color, ${A.color.onSecondaryContainer}))`),
			selectedIconColor: c(`var(--m3e-tonal-icon-button-focus-selected-icon-color, var(--m3e-icon-button-focus-selected-icon-color, ${A.color.onSecondary}))`),
			selectedStateLayerColor: c(`var(--m3e-tonal-icon-button-focus-selected-state-layer-color, var(--m3e-icon-button-focus-selected-state-layer-color, ${A.color.onSecondary}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-tonal-icon-button-pressed-icon-color, var(--m3e-icon-button-pressed-icon-color, ${A.color.onSecondaryContainer}))`),
			stateLayerColor: c(`var(--m3e-tonal-icon-button-pressed-state-layer-color, var(--m3e-icon-button-pressed-state-layer-color, ${A.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-tonal-icon-button-pressed-state-layer-opacity, var(--m3e-icon-button-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-tonal-icon-button-pressed-unselected-icon-color, var(--m3e-icon-button-pressed-unselected-icon-color, ${A.color.onSecondaryContainer}))`),
			unselectedStateLayerColor: c(`var(--m3e-tonal-icon-button-pressed-unselected-state-layer-color, var(--m3e-icon-button-pressed-unselected-state-layer-color, ${A.color.onSecondaryContainer}))`),
			selectedIconColor: c(`var(--m3e-tonal-icon-button-pressed-selected-icon-color, var(--m3e-icon-button-pressed-selected-icon-color, ${A.color.onSecondary}))`),
			selectedStateLayerColor: c(`var(--m3e-tonal-icon-button-pressed-selected-state-layer-color, var(--m3e-icon-button-pressed-selected-state-layer-color, ${A.color.onSecondary}))`)
		}
	},
	standard: {
		iconColor: c(`var(--m3e-standard-icon-button-icon-color, var(--m3e-icon-button-icon-color, ${A.color.onSurfaceVariant}))`),
		containerColor: c("var(--m3e-standard-icon-button-container-color)"),
		unselectedIconColor: c(`var(--m3e-standard-icon-button-unselected-icon-color, var(--m3e-icon-button-unselected-icon-color, ${A.color.onSurfaceVariant}))`),
		unselectedContainerColor: c("var(--m3e-standard-icon-button-unselected-container-color)"),
		selectedContainerColor: c("var(--m3e-standard-icon-button-selected-container-color)"),
		selectedIconColor: c(`var(--m3e-standard-icon-button-selected-icon-color, var(--m3e-icon-button-selected-icon-color, ${A.color.primary}))`),
		disabled: {
			containerColor: c("var(--m3e-standard-icon-button-disabled-container-color, var(--m3e-icon-button-disabled-container-color, transparent))"),
			containerOpacity: c("var(--m3e-standard-icon-button-disabled-container-opacity, var(--m3e-icon-button-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-standard-icon-button-disabled-icon-color, var(--m3e-icon-button-disabled-icon-color, ${A.color.onSurface}))`),
			iconOpacity: c("var(--m3e-standard-icon-button-disabled-icon-opacity, var(--m3e-icon-button-disabled-icon-opacity, 38%))")
		},
		hover: {
			iconColor: c(`var(--m3e-standard-icon-button-hover-icon-color, var(--m3e-icon-button-hover-icon-color, ${A.color.onSurfaceVariant}))`),
			stateLayerColor: c(`var(--m3e-standard-icon-button-hover-state-layer-color, var(--m3e-icon-button-hover-state-layer-color, ${A.color.onSurfaceVariant}))`),
			stateLayerOpacity: c(`var(--m3e-standard-icon-button-hover-state-layer-opacity, var(--m3e-icon-button-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-standard-icon-button-hover-unselected-icon-color, var(--m3e-icon-button-hover-unselected-icon-color, ${A.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-standard-icon-button-hover-unselected-state-layer-color, var(--m3e-icon-button-hover-unselected-state-layer-color, ${A.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-standard-icon-button-hover-selected-icon-color, var(--m3e-icon-button-hover-selected-icon-color, ${A.color.primary}))`),
			selectedStateLayerColor: c(`var(--m3e-standard-icon-button-hover-selected-state-layer-color, var(--m3e-icon-button-hover-selected-state-layer-color, ${A.color.primary}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-standard-icon-button-focus-icon-color, var(--m3e-icon-button-focus-icon-color, ${A.color.onSurfaceVariant}))`),
			stateLayerColor: c(`var(--m3e-standard-icon-button-focus-state-layer-color, var(--m3e-icon-button-focus-state-layer-color, ${A.color.onSurfaceVariant}))`),
			stateLayerOpacity: c(`var(--m3e-standard-icon-button-focus-state-layer-opacity, var(--m3e-icon-button-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-standard-icon-button-focus-unselected-icon-color, var(--m3e-icon-button-focus-unselected-icon-color, ${A.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-standard-icon-button-focus-unselected-state-layer-color, var(--m3e-icon-button-focus-unselected-state-layer-color, ${A.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-standard-icon-button-focus-selected-icon-color, var(--m3e-icon-button-focus-selected-icon-color, ${A.color.primary}))`),
			selectedStateLayerColor: c(`var(--m3e-standard-icon-button-focus-selected-state-layer-color, var(--m3e-icon-button-focus-selected-state-layer-color, ${A.color.primary}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-standard-icon-button-pressed-icon-color, var(--m3e-icon-button-pressed-icon-color, ${A.color.onSurfaceVariant}))`),
			stateLayerColor: c(`var(--m3e-standard-icon-button-pressed-state-layer-color, var(--m3e-icon-button-pressed-state-layer-color, ${A.color.onSurfaceVariant}))`),
			stateLayerOpacity: c(`var(--m3e-standard-icon-button-pressed-state-layer-opacity, var(--m3e-icon-button-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-standard-icon-button-pressed-unselected-icon-color, var(--m3e-icon-button-pressed-unselected-icon-color, ${A.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-standard-icon-button-pressed-unselected-state-layer-color, var(--m3e-icon-button-pressed-unselected-state-layer-color, ${A.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-standard-icon-button-pressed-selected-icon-color, var(--m3e-icon-button-pressed-selected-icon-color, ${A.color.primary}))`),
			selectedStateLayerColor: c(`var(--m3e-standard-icon-button-pressed-selected-state-layer-color, var(--m3e-icon-button-pressed-selected-state-layer-color, ${A.color.primary}))`)
		}
	}
};
function ul(e) {
	let t = c(`:is(:state(--${e}), :--${e})`);
	return l`:host(${t}:not(:disabled):not([disabled-interactive])) .base { background-color: ${H[e].containerColor ?? c("unset")}; --m3e-state-layer-hover-color: ${H[e].hover.stateLayerColor}; --m3e-state-layer-hover-opacity: ${H[e].hover.stateLayerOpacity}; --m3e-state-layer-focus-color: ${H[e].focus.stateLayerColor}; --m3e-state-layer-focus-opacity: ${H[e].focus.stateLayerOpacity}; --m3e-ripple-color: ${H[e].pressed.stateLayerColor}; --m3e-ripple-opacity: ${H[e].pressed.stateLayerOpacity}; --m3e-elevation-level: ${H[e].containerElevation ?? c("unset")}; --m3e-elevation-hover-level: ${H[e].hover.containerElevation ?? c("unset")}; --m3e-elevation-focus-level: ${H[e].focus.containerElevation ?? c("unset")}; --m3e-elevation-pressed-level: ${H[e].pressed.containerElevation ?? c("unset")}; } :host(${t}[toggle]:not([selected]):not(:disabled):not([disabled-interactive])) .base { background-color: ${H[e].unselectedContainerColor ?? c("unset")}; --m3e-state-layer-hover-color: ${H[e].hover.unselectedStateLayerColor}; --m3e-state-layer-focus-color: ${H[e].focus.unselectedStateLayerColor}; --m3e-ripple-color: ${H[e].pressed.unselectedStateLayerColor}; } :host(${t}[toggle][selected]:not(:disabled):not([disabled-interactive])) .base { background-color: ${H[e].selectedContainerColor ?? c("unset")}; --m3e-state-layer-hover-color: ${H[e].hover.selectedStateLayerColor}; --m3e-state-layer-focus-color: ${H[e].focus.selectedStateLayerColor}; --m3e-ripple-color: ${H[e].pressed.selectedStateLayerColor}; } :host(${t}:not(:disabled):not([disabled-interactive])) .base { outline-color: ${H[e].outlineColor ?? c("unset")}; } :host(${t}:focus:not(:disabled):not([disabled-interactive])) .base { outline-color: ${H[e].focus.outlineColor ?? c("unset")}; } :host(${t}:hover:not(:disabled):not([disabled-interactive])) .base { outline-color: ${H[e].hover.outlineColor ?? c("unset")}; } :host(${t}:is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive])) .base { outline-color: ${H[e].pressed.outlineColor ?? c("unset")}; } :host(${t}:not(:disabled):not([disabled-interactive])) .icon { color: ${H[e].iconColor}; } :host(${t}[toggle]:not([selected]):not(:disabled):not([disabled-interactive])) .icon { color: ${H[e].unselectedIconColor}; } :host(${t}[toggle][selected]:not(:disabled):not([disabled-interactive])) .icon { color: ${H[e].selectedIconColor}; } :host(${t}:focus:not(:disabled):not([disabled-interactive])) .icon { color: ${H[e].focus.iconColor}; } :host(${t}[toggle]:not([selected]):focus:not(:disabled):not([disabled-interactive])) .icon { color: ${H[e].focus.unselectedIconColor}; } :host(${t}[toggle][selected]:focus:not(:disabled):not([disabled-interactive])) .icon { color: ${H[e].focus.selectedIconColor}; } :host(${t}:hover:not(:disabled):not([disabled-interactive])) .icon { color: ${H[e].hover.iconColor}; } :host(${t}[toggle]:not([selected]):hover:not(:disabled):not([disabled-interactive])) .icon { color: ${H[e].hover.unselectedIconColor}; } :host(${t}[toggle][selected]:hover:not(:disabled):not([disabled-interactive])) .icon { color: ${H[e].hover.selectedIconColor}; } :host(${t}:is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive])) .icon { color: ${H[e].pressed.iconColor}; } :host( ${t}[toggle]:not([selected]):is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive]) ) .icon { color: ${H[e].pressed.unselectedIconColor}; } :host(${t}[toggle][selected]:is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive])) .icon { color: ${H[e].pressed.selectedIconColor}; } :host(${t}:disabled) .base, :host(${t}[disabled-interactive]) .base { outline-color: ${H[e].disabled.outlineColor ?? c("unset")}; background-color: color-mix( in srgb, ${H[e].disabled.containerColor} ${H[e].disabled.containerOpacity}, transparent ); } :host(${t}:disabled) .icon, :host(${t}[disabled-interactive]) .icon { color: color-mix( in srgb, ${H[e].disabled.iconColor} ${H[e].disabled.iconOpacity}, transparent ); }`;
}
var dl = [
	ul("standard"),
	ul("outlined"),
	ul("filled"),
	ul("tonal"),
	ul("elevated"),
	l`:host(:is(:state(--outlined), :--outlined):not([toggle][selected]):not(:disabled):not([disabled-interactive])) .base { outline-style: solid; }`
], fl, pl, ml, hl, gl, _l, vl, yl, bl, xl, Sl, Cl = class extends qi(Zi(Ui(Vi(Ci(vi(T(j(x, "button"), !0))))))) {
	constructor() {
		super(), fl.add(this), this._adjacentPressedTimeout = -1, pl.set(this, (e) => t(this, fl, "m", xl).call(this, e)), this.variant = "standard", this.shape = "rounded", this.size = "small", this.width = "default", this.toggle = !1, this.selected = !1, new cr(this, { callback: () => this._handleResize() }), new sn(this, { callback: (e) => {
			!this.disabledInteractive && !e && !this.grouped && this._base?.style.removeProperty("--_button-shape");
		} }), new nr(this, {
			isPressedKey: (e) => e === " ",
			minPressedDuration: 225,
			callback: (e) => {
				!this.disabled && !this.disabledInteractive && (e ? (t(this, fl, "m", vl).call(this), t(this, fl, "m", yl).call(this, !0)) : t(this, fl, "m", yl).call(this, !1));
			}
		});
	}
	get grouped() {
		return Rr(this, "--grouped");
	}
	render() {
		return y`<div class="layout"><div class="base"><m3e-state-layer class="state-layer" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-state-layer><m3e-elevation class="elevation" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-elevation><m3e-focus-ring class="focus-ring" ?disabled="${this.disabled}"></m3e-focus-ring><m3e-ripple class="ripple" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-ripple><div class="touch" aria-hidden="true"></div>${this[Ji]()}<div class="wrapper">${this.toggle ? y`<slot class="icon" name="selected" aria-hidden="true" @slotchange="${t(this, fl, "m", Sl)}"></slot>` : b}<slot class="icon" aria-hidden="true"></slot></div></div></div>`;
	}
	connectedCallback() {
		super.connectedCallback(), t(this, fl, "m", ml).call(this), t(this, fl, "m", hl).call(this), t(this, fl, "m", gl).call(this), t(this, fl, "m", _l).call(this), this.addEventListener("click", t(this, pl, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), [
			"--pressed",
			"--resting",
			"--grouped",
			"--connected"
		].forEach((e) => E(this, e)), this._base?.style.removeProperty("--_button-shape"), this.style.removeProperty("--_button-width"), this.style.removeProperty("--_adjacent-shrink"), E(this, "--adjacent-pressed"), this.removeEventListener("click", t(this, pl, "f"));
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, fl, "m", ml).call(this), e.has("shape") && t(this, fl, "m", hl).call(this), e.has("size") && t(this, fl, "m", gl).call(this), e.has("width") && t(this, fl, "m", _l).call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), [
			this._elevation,
			this._focusRing,
			this._stateLayer,
			this._ripple
		].forEach((e) => e?.attach(this));
	}
	updated(e) {
		if (super.updated(e), (e.has("disabled") && this.disabled || e.has("disabledInteractive") && this.disabledInteractive) && (E(this, "--pressed"), E(this, "--resting")), (e.has("toggle") || e.has("selected")) && (this.ariaPressed = this.toggle ? `${this.selected}` : null, this.toggle)) for (let e of this.querySelectorAll("m3e-icon")) e.toggleAttribute("filled", this.selected);
	}
	_handleResize() {
		if (this.grouped && !Rr(this, "--no-resize") && this !== document.activeElement) {
			let e = this.getBoundingClientRect().width;
			e > 0 ? (this.style.setProperty("--_button-width", `${e}px`), t(this, fl, "m", vl).call(this, !0)) : this.style.removeProperty("--_button-width");
		}
	}
};
pl = /* @__PURE__ */ new WeakMap(), fl = /* @__PURE__ */ new WeakSet(), ml = function() {
	il(this.variant) || (this.variant = "standard"), Br(this, this.variant, "elevated", "filled", "outlined", "standard", "tonal");
}, hl = function() {
	rl(this.shape) || (this.shape = "rounded"), Br(this, this.shape, "rounded", "square");
}, gl = function() {
	nl(this.size) || (this.size = "small"), Br(this, this.size, "extra-large", "extra-small", "large", "medium", "small");
}, _l = function() {
	al(this.width) || (this.width = "default"), Br(this, this.width, "default", "narrow", "wide");
}, vl = function(e = !1) {
	if (!this._base) return;
	let t = parseFloat(getComputedStyle(this._base).borderRadius);
	if (!isNaN(t) || e) {
		let n = this.clientHeight / 2;
		(n < t || e) && this._base?.style.setProperty("--_button-shape", `${n}px`);
	}
}, yl = function(e) {
	let n = this.getBoundingClientRect().width, r = this.closest("m3e-button-group");
	if (r && r.variant === "standard") {
		let i = [...r.querySelectorAll("m3e-button,m3e-icon-button")];
		for (let e of i) clearTimeout(e._adjacentPressedTimeout), e._adjacentPressedTimeout = -1;
		let a = i.indexOf(this);
		if (e) {
			let e = n * parseFloat(getComputedStyle(this).getPropertyValue("--m3e-standard-button-group-width-multiplier") || "0.15");
			a > 0 && a < i.length - 1 && (e /= 2);
			for (let t = 0; t < i.length; t++) t == a - 1 || t == a + 1 ? (zr(i[t], "--no-resize"), i[t].style.setProperty("--_adjacent-shrink", `${e}px`), zr(i[t], "--adjacent-pressed")) : t == a ? (zr(i[t], "--no-resize"), i[t].style.removeProperty("--_adjacent-shrink"), E(i[t], "--adjacent-pressed")) : (E(i[t], "--no-resize"), i[t].style.removeProperty("--_adjacent-shrink"), E(i[t], "--adjacent-pressed"));
		} else {
			for (let e = 0; e < i.length; e++) (e == a - 1 || e == a + 1) && i[e].style.setProperty("--_adjacent-shrink", "0px");
			Bt() ? t(this, fl, "m", bl).call(this, i) : this.addEventListener("transitionend", (e) => {
				e.propertyName === "width" && (this._adjacentPressedTimeout = setTimeout(() => {
					this._adjacentPressedTimeout > -1 && t(this, fl, "m", bl).call(this, i);
				}, 600));
			}, { once: !0 });
		}
	}
	D(this, "--pressed", e), D(this, "--resting", !e);
}, bl = function(e) {
	for (let t of e) E(t, "--adjacent-pressed"), E(t, "--no-resize"), t.style.removeProperty("--_adjacent-shrink");
}, xl = function(e) {
	(this.disabled || this.disabledInteractive) && (e.preventDefault(), e.stopImmediatePropagation()), this.toggle && !e.defaultPrevented && this.dispatchEvent(new Event("beforeinput", {
		bubbles: !0,
		cancelable: !0
	})) && (this.selected = !this.selected, this.dispatchEvent(new Event("input", { bubbles: !0 })), this.dispatchEvent(new Event("change", { bubbles: !0 })));
}, Sl = function(e) {
	this._base?.classList.toggle("with-selected-icon", Rt(e.target));
}, Cl.styles = [
	cl,
	dl,
	ll
], e([C(".base")], Cl.prototype, "_base", void 0), e([C(".elevation")], Cl.prototype, "_elevation", void 0), e([C(".focus-ring")], Cl.prototype, "_focusRing", void 0), e([C(".state-layer")], Cl.prototype, "_stateLayer", void 0), e([C(".ripple")], Cl.prototype, "_ripple", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], Cl.prototype, "variant", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], Cl.prototype, "shape", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], Cl.prototype, "size", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], Cl.prototype, "width", void 0), e([S({
	type: Boolean,
	reflect: !0
})], Cl.prototype, "toggle", void 0), e([S({
	type: Boolean,
	reflect: !0
})], Cl.prototype, "selected", void 0), e([lr(40)], Cl.prototype, "_handleResize", null), Cl = e([w("m3e-icon-button")], Cl);
//#endregion
//#region node_modules/@m3e/web/dist/split-button.js
function wl(e) {
	return e === "elevated" || e === "filled" || e === "tonal" || e === "outlined";
}
var Tl, El, Dl, Ol, kl, Al, jl, Ml, Nl, Pl, Fl, Il, Ll, Rl, zl = class extends j(T(x), "group") {
	constructor() {
		super(...arguments), Tl.add(this), El.set(this, void 0), Dl.set(this, void 0), Ol.set(this, !1), kl.set(this, new nr(this, {
			target: null,
			capture: !0,
			minPressedDuration: 225,
			isPressedKey: (e) => e === " ",
			callback: (e, n, r) => {
				switch (r) {
					case t(this, El, "f"):
						t(this, Tl, "m", Il).call(this, e);
						break;
					case t(this, Dl, "f"): t(this, Tl, "m", Ll).call(this, e || t(this, Ol, "f"));
				}
			}
		})), Al.set(this, new yn(this, {
			target: null,
			callback: (e, r) => {
				switch (r) {
					case t(this, Dl, "f"): !t(this, Dl, "f")?.disabled && !t(this, Dl, "f")?.disabledInteractive && (n(this, Ol, e, "f"), t(this, Tl, "m", Ll).call(this, e));
				}
			}
		})), this.variant = "filled", this.size = "small";
	}
	connectedCallback() {
		super.connectedCallback(), t(this, Tl, "m", jl).call(this), t(this, Tl, "m", Ml).call(this);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, Tl, "m", jl).call(this), e.has("size") && t(this, Tl, "m", Ml).call(this);
	}
	update(e) {
		super.update(e), e.has("variant") && t(this, Tl, "m", Fl).call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), e.has("variant") || t(this, Tl, "m", Fl).call(this);
	}
	render() {
		return y`<m3e-button-group class="base" disable-role variant="connected" size="${this.size}"><slot name="leading-button" @slotchange="${t(this, Tl, "m", Nl)}"></slot><slot name="trailing-button" @slotchange="${t(this, Tl, "m", Pl)}"></slot></m3e-button-group>`;
	}
};
El = /* @__PURE__ */ new WeakMap(), Dl = /* @__PURE__ */ new WeakMap(), Ol = /* @__PURE__ */ new WeakMap(), kl = /* @__PURE__ */ new WeakMap(), Al = /* @__PURE__ */ new WeakMap(), Tl = /* @__PURE__ */ new WeakSet(), jl = function() {
	wl(this.variant) || (this.variant = "filled");
}, Ml = function() {
	Ec(this.size) || (this.size = "small"), Br(this, this.size, "extra-large", "extra-small", "large", "medium", "small");
}, Nl = function(e) {
	t(this, El, "f") && t(this, kl, "f").unobserve(t(this, El, "f")), n(this, El, e.target.assignedElements({ flatten: !0 }).find((e) => e instanceof Wc), "f"), t(this, El, "f") && t(this, kl, "f").observe(t(this, El, "f")), t(this, Tl, "m", Fl).call(this);
}, Pl = function(e) {
	t(this, Dl, "f") && (t(this, kl, "f").unobserve(t(this, Dl, "f")), t(this, Al, "f").unobserve(t(this, Dl, "f"))), n(this, Dl, e.target.assignedElements({ flatten: !0 }).find((e) => e instanceof Cl), "f"), t(this, Dl, "f") && (t(this, kl, "f").observe(t(this, Dl, "f")), t(this, Al, "f").observe(t(this, Dl, "f"))), t(this, Tl, "m", Fl).call(this);
}, Fl = function() {
	t(this, El, "f") && (t(this, El, "f").variant = this.variant, t(this, El, "f").size = this.size, t(this, El, "f").shape = "rounded"), t(this, Dl, "f") && (t(this, Dl, "f").width = "default", t(this, Dl, "f").shape = "rounded", t(this, Dl, "f").setAttribute("variant", this.variant), t(this, Dl, "f").size = this.size);
}, Il = function(e) {
	e && t(this, El, "f") ? t(this, Tl, "m", Rl).call(this, t(this, El, "f"), "--_leading-button-shape") : this._base?.style.removeProperty("--_leading-button-shape");
}, Ll = function(e) {
	e && t(this, Dl, "f") ? t(this, Tl, "m", Rl).call(this, t(this, Dl, "f"), "--_trailing-button-shape") : this._base?.style.removeProperty("--_trailing-button-shape");
}, Rl = function(e, t) {
	let n = e.clientHeight / 2;
	n && this._base?.style.setProperty(t, `${n}px`);
}, zl.styles = l`:host { display: inline-flex; vertical-align: middle; } :host([hidden]) { display: none; } ::slotted([slot="leading-button"]) { flex: 1 1 auto; min-width: 0; --_button-start-shape-pressed-morph: var(--_leading-button-shape, ${A.shape.corner.full}); } ::slotted([slot="trailing-button"]:not([aria-expanded="true"])) { --m3e-icon-button-extra-small-default-leading-space: var( --m3e-split-button-extra-small-trailing-button-unselected-leading-space, 12px ); --m3e-icon-button-extra-small-default-trailing-space: var( --m3e-split-button-extra-small-trailing-button-unselected-trailing-space, 14px ); --m3e-icon-button-small-default-leading-space: var( --m3e-split-button-small-trailing-button-unselected-leading-space, 12px ); --m3e-icon-button-small-default-trailing-space: var( --m3e-split-button-small-trailing-button-unselected-trailing-space, 14px ); --m3e-icon-button-medium-default-leading-space: var( --m3e-split-button-medium-trailing-button-unselected-leading-space, 13px ); --m3e-icon-button-medium-default-trailing-space: var( --m3e-split-button-medium-trailing-button-unselected-trailing-space, 17px ); --m3e-icon-button-large-default-leading-space: var( --m3e-split-button-large-trailing-button-unselected-leading-space, 26px ); --m3e-icon-button-large-default-trailing-space: var( --m3e-split-button-large-trailing-button-unselected-trailing-space, 32px ); --m3e-icon-button-extra-large-default-leading-space: var( --m3e-split-button-extra-large-trailing-button-unselected-leading-space, 37px ); --m3e-icon-button-extra-large-default-trailing-space: var( --m3e-split-button-extra-large-trailing-button-unselected-trailing-space, 49px ); } ::slotted([slot="trailing-button"][aria-expanded="true"]) { --m3e-icon-button-extra-small-default-leading-space: var( --m3e-split-button-extra-small-trailing-button-selected-leading-space, 13px ); --m3e-icon-button-extra-small-default-trailing-space: var( --m3e-split-button-extra-small-trailing-button-selected-trailing-space, 13px ); --m3e-icon-button-small-default-leading-space: var( --m3e-split-button-small-trailing-button-selected-leading-space, 13px ); --m3e-icon-button-small-default-trailing-space: var( --m3e-split-button-small-trailing-button-selected-trailing-space, 13px ); --m3e-icon-button-medium-default-leading-space: var( --m3e-split-button-medium-trailing-button-selected-leading-space, 15px ); --m3e-icon-button-medium-default-trailing-space: var( --m3e-split-button-medium-trailing-button-selected-trailing-space, 15px ); --m3e-icon-button-large-default-leading-space: var( --m3e-split-button-large-trailing-button-selected-leading-space, 29px ); --m3e-icon-button-large-default-trailing-space: var( --m3e-split-button-large-trailing-button-selected-trailing-space, 29px ); --m3e-icon-button-extra-large-default-leading-space: var( --m3e-split-button-extra-large-trailing-button-selected-leading-space, 43px ); --m3e-icon-button-extra-large-default-trailing-space: var( --m3e-split-button-extra-large-trailing-button-selected-trailing-space, 43px ); } ::slotted([slot="leading-button"]:not(:hover)), ::slotted([slot="leading-button"]:disabled), ::slotted([slot="leading-button"][disabled-interactive]) { --m3e-connected-button-group-extra-small-inner-shape: var( --m3e-split-button-extra-small-inner-corner-size, ${A.shape.corner.extraSmall} ); --m3e-connected-button-group-small-inner-shape: var( --m3e-split-button-small-inner-corner-size, ${A.shape.corner.extraSmall} ); --m3e-connected-button-group-medium-inner-shape: var( --m3e-split-button-medium-inner-corner-size, ${A.shape.corner.extraSmall} ); --m3e-connected-button-group-large-inner-shape: var( --m3e-split-button-large-inner-corner-size, ${A.shape.corner.small} ); --m3e-connected-button-group-extra-large-inner-shape: var( --m3e-split-button-extra-large-inner-corner-size, ${A.shape.corner.medium} ); } ::slotted([slot="leading-button"]:hover:not(:disabled):not([disabled-interactive])), ::slotted([slot="trailing-button"]:not([aria-expanded="true"]):hover:not(:disabled):not([disabled-interactive])) { --m3e-connected-button-group-extra-small-inner-shape: var( --m3e-split-button-extra-small-inner-corner-hover-size, ${A.shape.corner.small} ); --m3e-connected-button-group-small-inner-shape: var( --m3e-split-button-small-inner-corner-hover-size, ${A.shape.corner.medium} ); --m3e-connected-button-group-medium-inner-shape: var( --m3e-split-button-medium-inner-corner-hover-size, ${A.shape.corner.medium} ); --m3e-connected-button-group-large-inner-shape: var( --m3e-split-button-large-inner-corner-hover-size, ${A.shape.corner.largeIncreased} ); --m3e-connected-button-group-extra-large-inner-shape: var( --m3e-split-button-extra-large-inner-corner-hover-size, ${A.shape.corner.largeIncreased} ); } ::slotted([slot="leading-button"]), ::slotted([slot="trailing-button"]) { --m3e-connected-button-group-extra-small-inner-pressed-shape: var( --m3e-split-button-extra-small-inner-corner-pressed-size, ${A.shape.corner.small} ); --m3e-connected-button-group-small-inner-pressed-shape: var( --m3e-split-button-small-inner-corner-pressed-size, ${A.shape.corner.medium} ); --m3e-connected-button-group-medium-inner-pressed-shape: var( --m3e-split-button-medium-inner-corner-pressed-size, ${A.shape.corner.medium} ); --m3e-connected-button-group-large-inner-pressed-shape: var( --m3e-split-button-large-inner-corner-pressed-size, ${A.shape.corner.largeIncreased} ); --m3e-connected-button-group-extra-large-inner-pressed-shape: var( --m3e-split-button-extra-large-inner-corner-pressed-size, ${A.shape.corner.largeIncreased} ); } ::slotted([slot="trailing-button"]:not([aria-expanded="true"]):not(:hover)), ::slotted([slot="trailing-button"]:disabled), ::slotted([slot="trailing-button"][disabled-interactive]) { --m3e-connected-button-group-extra-small-inner-shape: var( --m3e-split-button-extra-small-inner-corner-size, ${A.shape.corner.extraSmall} ); --m3e-connected-button-group-small-inner-shape: var( --m3e-split-button-small-inner-corner-size, ${A.shape.corner.extraSmall} ); --m3e-connected-button-group-medium-inner-shape: var( --m3e-split-button-medium-inner-corner-size, ${A.shape.corner.extraSmall} ); --m3e-connected-button-group-large-inner-shape: var( --m3e-split-button-large-inner-corner-size, ${A.shape.corner.small} ); --m3e-connected-button-group-extra-large-inner-shape: var( --m3e-split-button-extra-large-inner-corner-size, ${A.shape.corner.medium} ); } ::slotted([slot="trailing-button"][aria-expanded="true"]) { --_icon-button-icon-transform: rotate(180deg); --_button-rounded-start-shape: var(--_trailing-button-shape, ${A.shape.corner.full}); --_button-rounded-end-shape: var(--_trailing-button-shape, ${A.shape.corner.full}); } ::slotted([slot="trailing-button"]) { --_button-end-shape-pressed-morph: var(--_trailing-button-shape, ${A.shape.corner.full}); } .base { --m3e-icon-button-extra-small-icon-size: var(--m3e-spit-button-extra-small-trailing-button-icon-size, 22px); --m3e-button-extra-small-icon-size: var(--m3e-spit-button-extra-small-trailing-button-icon-size, 22px); --m3e-icon-button-small-icon-size: var(--m3e-spit-button-small-trailing-button-icon-size, 22px); --m3e-button-small-icon-size: var(--m3e-spit-button-small-trailing-button-icon-size, 22px); --m3e-icon-button-medium-icon-size: var(--m3e-spit-button-medium-trailing-button-icon-size, 26px); --m3e-button-medium-icon-size: var(--m3e-spit-button-medium-trailing-button-icon-size, 26px); --m3e-icon-button-large-icon-size: var(--m3e-spit-button-large-trailing-button-icon-size, 38px); --m3e-button-large-icon-size: var(--m3e-spit-button-large-trailing-button-icon-size, 38px); --m3e-icon-button-extra-large-icon-size: var(--m3e-spit-button-extra-large-trailing-button-icon-size, 50px); --m3e-button-extra-large-icon-size: var(--m3e-spit-button-extra-large-trailing-button-icon-size, 50px); } :host(:is(:state(--extra-small), :--extra-small)) .base { --m3e-connected-button-group-spacing: var(--m3e-split-button-extra-small-between-spacing, 2px); } :host(:is(:state(--small), :--small)) .base { --m3e-connected-button-group-spacing: var(--m3e-split-button-small-between-spacing, 2px); } :host(:is(:state(--medium), :--medium)).base { --m3e-connected-button-group-spacing: var(--m3e-split-button-medium-between-spacing, 2px); } :host(:is(:state(--large), :--large)) .base { --m3e-connected-button-group-spacing: var(--m3e-split-button-large-between-spacing, 2px); } :host(:is(:state(--extra-large), :--extra-large)) .base { --m3e-connected-button-group-spacing: var(--m3e-split-button-extra-large-between-spacing, 2px); }`, e([C(".base")], zl.prototype, "_base", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], zl.prototype, "variant", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], zl.prototype, "size", void 0), zl = e([w("m3e-split-button")], zl);
//#endregion
//#region node_modules/@m3e/web/dist/fab.js
function Bl(e) {
	return e === "small" || e === "medium" || e === "large";
}
function Vl(e) {
	return e === "primary" || e === "primary-container" || e === "secondary" || e === "secondary-container" || e === "tertiary" || e === "tertiary-container" || e === "surface";
}
var Hl = l`:host { display: inline-block; outline: none; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } :host([hidden]) { display: none; } .base { box-sizing: border-box; vertical-align: middle; display: inline-flex; align-items: center; justify-content: center; position: relative; width: 100%; transition: ${c(`background-color ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; } .touch { position: absolute; height: 48px; left: 0; right: 0; } .wrapper { width: 100%; overflow: hidden; display: inline-flex; align-items: center; box-sizing: border-box; } .label { white-space: nowrap; transition: ${c(`color ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; } .icon { transition: ${c(`color ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; } :host(:not(:disabled):not([disabled-interactive])) { cursor: pointer; } :host([disabled-interactive]) { cursor: not-allowed; } .close-icon, ::slotted(:not([slot])), ::slotted([slot="close-icon"]) { font-size: inherit !important; flex: none; } .close-icon, ::slotted(svg:not([slot])), ::slotted(svg[slot="close-icon"]) { width: 1em; height: 1em; } .base.with-menu ::slotted([slot="label"]), .base:not(.with-menu) ::slotted([slot="close-icon"]), .base:not(.with-menu) .close-icon, :host([aria-expanded="true"]) .base.with-menu ::slotted(:not([slot])), :host([aria-expanded="false"]) .base.with-menu ::slotted([slot="close-icon"]), :host([aria-expanded="false"]) .base.with-menu .close-icon { display: none; } :host([aria-expanded="true"]) .base.with-menu { border-radius: var(--m3e-fab-menu-close-button-container-shape, ${A.shape.corner.full}); min-height: calc(var(--m3e-fab-menu-close-button-container-height, 56px) + ${A.density.calc(-3)}); } :host([aria-expanded="true"]) .base.with-menu .wrapper { padding-block-start: calc( var(--m3e-fab-menu-close-button-top-space, ${A.measurement.space200}) + ${A.density.calcHalf(-3)} ); padding-block-end: calc( var(--m3e-fab-menu-close-button-bottom-space, ${A.measurement.space200}) + ${A.density.calcHalf(-3)} ); padding-inline-start: calc( var(--m3e-fab-menu-close-button-leading-space, ${A.measurement.space200}) + ${A.density.calcHalf(-3)} ); padding-inline-end: calc( var(--m3e-fab-menu-close-button-trailing-space, ${A.measurement.space200}) + ${A.density.calcHalf(-3)} ); } :host([aria-expanded="true"]) .base.with-menu .icon { font-size: calc(var(--m3e-fab-menu-close-button-icon-size, 24px) + ${A.density.calc(-3)}); --m3e-icon-size: calc(var(--m3e-fab-menu-close-button-icon-size, 24px) + ${A.density.calc(-3)}); } .base.with-menu { transition: min-height ${A.motion.spring.fastEffects}; } .base.with-menu .wrapper { transition: padding ${A.motion.spring.fastEffects}; } a { all: unset; display: block; position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px; z-index: 1; } @media (forced-colors: active) { .base, .icon { transition: none; } .base { outline-style: solid; } :host(:not(:disabled):not([disabled-interactive])) .base { background-color: ButtonFace !important; outline-color: ButtonText !important; } :host(:not(:disabled):not([disabled-interactive])) .label, :host(:not(:disabled):not([disabled-interactive])) .icon { color: ButtonText !important; } :host(:disabled) .base, :host([disabled-interactive]) .base { outline-color: GrayText !important; background-color: unset !important; } :host(:disabled) .label, :host([disabled-interactive]) .label, :host(:disabled) .icon, :host([disabled-interactive]) .icon { color: GrayText !important; } :host(:is(:state(--small), :--small)) .base { outline-offset: calc(0px - var(--m3e-button-small-outline-thickness, 1px)); outline-width: var(--m3e-button-small-outline-thickness, 1px); } :host(:is(:state(--medium), :--medium)) .base { outline-offset: calc(0px - var(--m3e-button-medium-outline-thickness, 1px)); outline-width: var(--m3e-button-medium-outline-thickness, 1px); } :host(:is(:state(--large), :--large)) .base { outline-offset: calc(0px - var(--m3e-button-large-outline-thickness, 2px)); outline-width: var(--m3e-button-large-outline-thickness, 2px); } } @media (prefers-reduced-motion) { .base, .base.resting, .base.pressed, .label, .icon { transition: none; } }`, Ul = {
	small: {
		containerHeight: c(`calc(var(--m3e-fab-small-container-height, var(--m3e-fab-container-height, 56px)) + ${A.density.calc(-3)})`),
		labelTextFontSize: c(`var(--m3e-fab-small-label-text-font-size, var(--m3e-fab-label-text-font-size, ${A.typescale.standard.title.medium.fontSize}))`),
		labelTextFontWeight: c(`var(--m3e-fab-small-label-text-font-weight, var(--m3e-fab-label-text-font-weight, ${A.typescale.standard.title.medium.fontWeight}))`),
		labelTextLineHeight: c(`var(--m3e-fab-small-label-text-line-height, var(--m3e-fab-label-text-line-height, ${A.typescale.standard.title.medium.lineHeight}))`),
		labelTextTracking: c(`var(--m3e-fab-small-label-text-tracking, var(--m3e-fab-label-text-tracking, ${A.typescale.standard.title.medium.tracking}))`),
		iconSize: c("var(--m3e-fab-small-icon-size, var(--m3e-fab-icon-size, 24px))"),
		extendedIconSize: c("var(--m3e-fab-small-icon-size, var(--m3e-fab-icon-size, 24px))"),
		shape: c(`var(--m3e-fab-small-shape, var(--m3e-fab-shape, ${A.shape.corner.large}))`),
		leadingSpace: c(`calc(var(--m3e-fab-small-leading-space, var(--m3e-fab-leading-space, ${A.measurement.space200})) + ${A.density.calcHalf(-3)})`),
		trailingSpace: c(`calc(var(--m3e-fab-small-trailing-space, var(--m3e-fab-trailing-space, ${A.measurement.space200})) + ${A.density.calcHalf(-3)})`),
		iconLabelSpace: c(`var(--m3e-fab-small-icon-label-space, var(--m3e-fab-icon-label-space, ${A.measurement.space100}))`),
		extendedLeadingSpace: c(`var(--m3e-fab-small-leading-space, var(--m3e-fab-leading-space, ${A.measurement.space200}))`),
		extendedTrailingSpace: c(`var(--m3e-fab-small-trailing-space, var(--m3e-fab-trailing-space, ${A.measurement.space200}))`)
	},
	medium: {
		containerHeight: c(`calc(var(--m3e-fab-medium-container-height, var(--m3e-fab-container-height, 80px)) + ${A.density.calc(-3)})`),
		labelTextFontSize: c(`var(--m3e-fab-medium-label-text-font-size, var(--m3e-fab-label-text-font-size, ${A.typescale.standard.title.large.fontSize}))`),
		labelTextFontWeight: c(`var(--m3e-fab-medium-label-text-font-weight, var(--m3e-fab-label-text-font-weight, ${A.typescale.standard.title.large.fontWeight}))`),
		labelTextLineHeight: c(`var(--m3e-fab-medium-label-text-line-height, var(--m3e-fab-label-text-line-height, ${A.typescale.standard.title.large.lineHeight}))`),
		labelTextTracking: c(`var(--m3e-fab-medium-label-text-tracking, var(--m3e-fab-label-text-tracking, ${A.typescale.standard.title.large.tracking}))`),
		iconSize: c(`var(--m3e-fab-medium-icon-size, var(--m3e-fab-icon-size, ${A.measurement.space350}))`),
		extendedIconSize: c(`var(--m3e-fab-medium-icon-size, var(--m3e-fab-icon-size, ${A.measurement.space350}))`),
		shape: c(`var(--m3e-fab-medium-shape, var(--m3e-fab-shape, ${A.shape.corner.largeIncreased}))`),
		leadingSpace: c(`calc(var(--m3e-fab-medium-leading-space, var(--m3e-fab-leading-space, ${A.measurement.space325})) + ${A.density.calcHalf(-3)})`),
		trailingSpace: c(`calc(var(--m3e-fab-medium-trailing-space, var(--m3e-fab-trailing-space, ${A.measurement.space325})) + ${A.density.calcHalf(-3)})`),
		iconLabelSpace: c(`var(--m3e-fab-medium-icon-label-space, var(--m3e-fab-icon-label-space, ${A.measurement.space150}))`),
		extendedLeadingSpace: c(`var(--m3e-fab-medium-leading-space, var(--m3e-fab-leading-space, ${A.measurement.space325}))`),
		extendedTrailingSpace: c(`var(--m3e-fab-medium-trailing-space, var(--m3e-fab-trailing-space, ${A.measurement.space325}))`)
	},
	large: {
		containerHeight: c(`calc(var(--m3e-fab-large-container-height, var(--m3e-fab-container-height, 96px)) + ${A.density.calc(-3)})`),
		labelTextFontSize: c(`var(--m3e-fab-large-label-text-font-size, var(--m3e-fab-label-text-font-size, ${A.typescale.standard.headline.small.fontSize}))`),
		labelTextFontWeight: c(`var(--m3e-fab-large-label-text-font-weight, var(--m3e-fab-label-text-font-weight, ${A.typescale.standard.headline.small.fontWeight}))`),
		labelTextLineHeight: c(`var(--m3e-fab-large-label-text-line-height, var(--m3e-fab-label-text-line-height, ${A.typescale.standard.headline.small.lineHeight}))`),
		labelTextTracking: c(`var(--m3e-fab-large-label-text-tracking, var(--m3e-fab-label-text-tracking, ${A.typescale.standard.headline.small.tracking}))`),
		iconSize: c("var(--m3e-fab-large-icon-size, var(--m3e-fab-icon-size, 36px))"),
		extendedIconSize: c("var(--m3e-fab-large-icon-size, var(--m3e-fab-icon-size, 36px))"),
		shape: c(`var(--m3e-fab-large-shape, var(--m3e-fab-shape, ${A.shape.corner.extraLarge}))`),
		leadingSpace: c(`calc(var(--m3e-fab-large-leading-space, var(--m3e-fab-leading-space, ${A.measurement.space350})) + ${A.density.calcHalf(-3)})`),
		trailingSpace: c(`calc(var(--m3e-fab-large-trailing-space, var(--m3e-fab-trailing-space, ${A.measurement.space350})) + ${A.density.calcHalf(-3)})`),
		iconLabelSpace: c(`var(--m3e-fab-large-icon-label-space, var(--m3e-fab-icon-label-space, ${A.measurement.space200}))`),
		extendedLeadingSpace: c(`var(--m3e-fab-large-leading-space, var(--m3e-fab-leading-space, ${A.measurement.space350}))`),
		extendedTrailingSpace: c(`var(--m3e-fab-large-trailing-space, var(--m3e-fab-trailing-space, ${A.measurement.space350}))`)
	}
};
function Wl(e) {
	let t = c(`:is(:state(--${e}), :--${e})`);
	return l`:host(${t}) .base { min-height: ${Ul[e].containerHeight}; } :host(${t}) .base { border-radius: ${Ul[e].shape}; } :host(${t}) .label { font-size: ${Ul[e].labelTextFontSize}; font-weight: ${Ul[e].labelTextFontWeight}; line-height: ${Ul[e].labelTextLineHeight}; letter-spacing: ${Ul[e].labelTextTracking}; } :host(${t}:not([extended])) .wrapper { padding-inline-start: ${Ul[e].leadingSpace}; padding-inline-end: ${Ul[e].trailingSpace}; } :host(${t}:not([extended])) .icon { font-size: ${Ul[e].iconSize}; --m3e-icon-size: ${Ul[e].iconSize}; } :host(${t}[extended]) .wrapper { padding-inline-start: ${Ul[e].extendedLeadingSpace}; padding-inline-end: ${Ul[e].extendedTrailingSpace}; column-gap: ${Ul[e].iconLabelSpace}; } :host(${t}[extended]) .icon { font-size: ${Ul[e].extendedIconSize}; --m3e-icon-size: ${Ul[e].extendedIconSize}; }`;
}
var Gl = [
	Wl("small"),
	Wl("medium"),
	Wl("large")
], U = {
	primary: {
		labelTextColor: c(`var(--m3e-primary-fab-label-text-color, var(--m3e-fab-label-text-color, ${A.color.onPrimary}))`),
		iconColor: c(`var(--m3e-primary-fab-icon-color, var(--m3e-fab-icon-color, ${A.color.onPrimary}))`),
		containerColor: c(`var(--m3e-primary-fab-container-color, var(--m3e-fab-container-color, ${A.color.primary}))`),
		containerElevation: c(`var(--m3e-primary-fab-container-elevation, var(--m3e-fab-container-elevation, ${A.elevation.level3}))`),
		loweredContainerElevation: c(`var(--m3e-primary-fab-lowered-container-elevation, var(--m3e-fab-lowered-container-elevation, ${A.elevation.level2}))`),
		disabled: {
			containerColor: c(`var(--m3e-primary-fab-disabled-container-color, var(--m3e-fab-disabled-container-color, ${A.color.onSurface}))`),
			containerOpacity: c("var(--m3e-primary-fab-disabled-container-opacity, var(--m3e-fab-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-primary-fab-disabled-icon-color, var(--m3e-fab-disabled-icon-color, ${A.color.onSurface}))`),
			iconOpacity: c("var(--m3e-primary-fab-disabled-icon-opacity, var(--m3e-fab-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-primary-fab-disabled-label-text-color, var(--m3e-fab-disabled-label-text-color, ${A.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-primary-fab-disabled-label-text-opacity, var(--m3e-fab-disabled-label-text-opacity, 38%))"),
			containerElevation: c(`var(--m3e-primary-fab-disabled-container-elevation, var(--m3e-fab-disabled-container-elevation, ${A.elevation.level0}))`),
			loweredContainerElevation: c(`var(--m3e-primary-fab-lowered-disabled-container-elevation, var(--m3e-fab-lowered-disabled-container-elevation, ${A.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-primary-fab-hover-icon-color, var(--m3e-fab-hover-icon-color, ${A.color.onPrimary}))`),
			labelTextColor: c(`var(--m3e-primary-fab-hover-label-text-color, var(--m3e-fab-hover-label-text-color, ${A.color.onPrimary}))`),
			stateLayerColor: c(`var(--m3e-primary-fab-hover-state-layer-color, var(--m3e-fab-hover-state-layer-color, ${A.color.onPrimary}))`),
			stateLayerOpacity: c(`var(--m3e-primary-fab-hover-state-layer-opacity, var(--m3e-fab-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-primary-fab-hover-container-elevation, var(--m3e-fab-hover-container-elevation, ${A.elevation.level4}))`),
			loweredContainerElevation: c(`var(--m3e-primary-fab-lowered-hover-container-elevation, var(--m3e-fab-lowered-hover-container-elevation, ${A.elevation.level3}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-primary-fab-focus-icon-color, var(--m3e-fab-focus-icon-color, ${A.color.onPrimary}))`),
			labelTextColor: c(`var(--m3e-primary-fab-focus-label-text-color, var(--m3e-fab-focus-label-text-color, ${A.color.onPrimary}))`),
			stateLayerColor: c(`var(--m3e-primary-fab-focus-state-layer-color, var(--m3e-fab-focus-state-layer-color, ${A.color.onPrimary}))`),
			stateLayerOpacity: c(`var(--m3e-primary-fab-focus-state-layer-opacity, var(--m3e-fab-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-primary-fab-focus-container-elevation, var(--m3e-fab-focus-container-elevation, ${A.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-primary-fab-lowered-focus-container-elevation, var(--m3e-fab-lowered-focus-container-elevation, ${A.elevation.level2}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-primary-fab-pressed-icon-color, var(--m3e-fab-pressed-icon-color, ${A.color.onPrimary}))`),
			labelTextColor: c(`var(--m3e-primary-fab-pressed-label-text-color, var(--m3e-fab-pressed-label-text-color, ${A.color.onPrimary}))`),
			stateLayerColor: c(`var(--m3e-primary-fab-pressed-state-layer-color, var(--m3e-fab-pressed-state-layer-color, ${A.color.onPrimary}))`),
			stateLayerOpacity: c(`var(--m3e-primary-fab-pressed-state-layer-opacity, var(--m3e-fab-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-primary-fab-pressed-container-elevation, var(--m3e-fab-pressed-container-elevation, ${A.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-primary-fab-lowered-pressed-container-elevation, var(--m3e-fab-lowered-pressed-container-elevation, ${A.elevation.level2}))`)
		}
	},
	secondary: {
		labelTextColor: c(`var(--m3e-secondary-fab-label-text-color, var(--m3e-fab-label-text-color, ${A.color.onSecondary}))`),
		iconColor: c(`var(--m3e-secondary-fab-icon-color, var(--m3e-fab-icon-color, ${A.color.onSecondary}))`),
		containerColor: c(`var(--m3e-secondary-fab-container-color, var(--m3e-fab-container-color, ${A.color.secondary}))`),
		containerElevation: c(`var(--m3e-secondary-fab-container-elevation, var(--m3e-fab-container-elevation, ${A.elevation.level3}))`),
		loweredContainerElevation: c(`var(--m3e-secondary-fab-lowered-container-elevation, var(--m3e-fab-lowered-container-elevation, ${A.elevation.level2}))`),
		disabled: {
			containerColor: c(`var(--m3e-secondary-fab-disabled-container-color, var(--m3e-fab-disabled-container-color, ${A.color.onSurface}))`),
			containerOpacity: c("var(--m3e-secondary-fab-disabled-container-opacity, var(--m3e-fab-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-secondary-fab-disabled-icon-color, var(--m3e-fab-disabled-icon-color, ${A.color.onSurface}))`),
			iconOpacity: c("var(--m3e-secondary-fab-disabled-icon-opacity, var(--m3e-fab-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-secondary-fab-disabled-label-text-color, var(--m3e-fab-disabled-label-text-color, ${A.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-secondary-fab-disabled-label-text-opacity, var(--m3e-fab-disabled-label-text-opacity, 38%))"),
			containerElevation: c(`var(--m3e-secondary-fab-disabled-container-elevation, var(--m3e-fab-disabled-container-elevation, ${A.elevation.level0}))`),
			loweredContainerElevation: c(`var(--m3e-secondary-fab-lowered-disabled-container-elevation, var(--m3e-fab-lowered-disabled-container-elevation, ${A.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-secondary-fab-hover-icon-color, var(--m3e-fab-hover-icon-color, ${A.color.onSecondary}))`),
			labelTextColor: c(`var(--m3e-secondary-fab-hover-label-text-color, var(--m3e-fab-hover-label-text-color, ${A.color.onSecondary}))`),
			stateLayerColor: c(`var(--m3e-secondary-fab-hover-state-layer-color, var(--m3e-fab-hover-state-layer-color, ${A.color.onSecondary}))`),
			stateLayerOpacity: c(`var(--m3e-secondary-fab-hover-state-layer-opacity, var(--m3e-fab-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-secondary-fab-hover-container-elevation, var(--m3e-fab-hover-container-elevation, ${A.elevation.level4}))`),
			loweredContainerElevation: c(`var(--m3e-secondary-fab-lowered-hover-container-elevation, var(--m3e-fab-lowered-hover-container-elevation, ${A.elevation.level3}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-secondary-fab-focus-icon-color, var(--m3e-fab-focus-icon-color, ${A.color.onSecondary}))`),
			labelTextColor: c(`var(--m3e-secondary-fab-focus-label-text-color, var(--m3e-fab-focus-label-text-color, ${A.color.onSecondary}))`),
			stateLayerColor: c(`var(--m3e-secondary-fab-focus-state-layer-color, var(--m3e-fab-focus-state-layer-color, ${A.color.onSecondary}))`),
			stateLayerOpacity: c(`var(--m3e-secondary-fab-focus-state-layer-opacity, var(--m3e-fab-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-secondary-fab-focus-container-elevation, var(--m3e-fab-focus-container-elevation, ${A.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-secondary-fab-lowered-focus-container-elevation, var(--m3e-fab-lowered-focus-container-elevation, ${A.elevation.level2}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-secondary-fab-pressed-icon-color, var(--m3e-fab-pressed-icon-color, ${A.color.onSecondary}))`),
			labelTextColor: c(`var(--m3e-secondary-fab-pressed-label-text-color, var(--m3e-fab-pressed-label-text-color, ${A.color.onSecondary}))`),
			stateLayerColor: c(`var(--m3e-secondary-fab-pressed-state-layer-color, var(--m3e-fab-pressed-state-layer-color, ${A.color.onSecondary}))`),
			stateLayerOpacity: c(`var(--m3e-secondary-fab-pressed-state-layer-opacity, var(--m3e-fab-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-secondary-fab-pressed-container-elevation, var(--m3e-fab-pressed-container-elevation, ${A.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-secondary-fab-lowered-pressed-container-elevation, var(--m3e-fab-lowered-pressed-container-elevation, ${A.elevation.level2}))`)
		}
	},
	tertiary: {
		labelTextColor: c(`var(--m3e-tertiary-fab-label-text-color, var(--m3e-fab-label-text-color, ${A.color.onTertiary}))`),
		iconColor: c(`var(--m3e-tertiary-fab-icon-color, var(--m3e-fab-icon-color, ${A.color.onTertiary}))`),
		containerColor: c(`var(--m3e-tertiary-fab-container-color, var(--m3e-fab-container-color, ${A.color.tertiary}))`),
		containerElevation: c(`var(--m3e-tertiary-fab-container-elevation, var(--m3e-fab-container-elevation, ${A.elevation.level3}))`),
		loweredContainerElevation: c(`var(--m3e-tertiary-fab-lowered-container-elevation, var(--m3e-fab-lowered-container-elevation, ${A.elevation.level2}))`),
		disabled: {
			containerColor: c(`var(--m3e-tertiary-fab-disabled-container-color, var(--m3e-fab-disabled-container-color, ${A.color.onSurface}))`),
			containerOpacity: c("var(--m3e-tertiary-fab-disabled-container-opacity, var(--m3e-fab-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-tertiary-fab-disabled-icon-color, var(--m3e-fab-disabled-icon-color, ${A.color.onSurface}))`),
			iconOpacity: c("var(--m3e-tertiary-fab-disabled-icon-opacity, var(--m3e-fab-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-tertiary-fab-disabled-label-text-color, var(--m3e-fab-disabled-label-text-color, ${A.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-tertiary-fab-disabled-label-text-opacity, var(--m3e-fab-disabled-label-text-opacity, 38%))"),
			containerElevation: c(`var(--m3e-tertiary-fab-disabled-container-elevation, var(--m3e-fab-disabled-container-elevation, ${A.elevation.level0}))`),
			loweredContainerElevation: c(`var(--m3e-tertiary-fab-lowered-disabled-container-elevation, var(--m3e-fab-lowered-disabled-container-elevation, ${A.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-tertiary-fab-hover-icon-color, var(--m3e-fab-hover-icon-color, ${A.color.onTertiary}))`),
			labelTextColor: c(`var(--m3e-tertiary-fab-hover-label-text-color, var(--m3e-fab-hover-label-text-color, ${A.color.onTertiary}))`),
			stateLayerColor: c(`var(--m3e-tertiary-fab-hover-state-layer-color, var(--m3e-fab-hover-state-layer-color, ${A.color.onTertiary}))`),
			stateLayerOpacity: c(`var(--m3e-tertiary-fab-hover-state-layer-opacity, var(--m3e-fab-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-tertiary-fab-hover-container-elevation, var(--m3e-fab-hover-container-elevation, ${A.elevation.level4}))`),
			loweredContainerElevation: c(`var(--m3e-tertiary-fab-lowered-hover-container-elevation, var(--m3e-fab-lowered-hover-container-elevation, ${A.elevation.level3}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-tertiary-fab-focus-icon-color, var(--m3e-fab-focus-icon-color, ${A.color.onTertiary}))`),
			labelTextColor: c(`var(--m3e-tertiary-fab-focus-label-text-color, var(--m3e-fab-focus-label-text-color, ${A.color.onTertiary}))`),
			stateLayerColor: c(`var(--m3e-tertiary-fab-focus-state-layer-color, var(--m3e-fab-focus-state-layer-color, ${A.color.onTertiary}))`),
			stateLayerOpacity: c(`var(--m3e-tertiary-fab-focus-state-layer-opacity, var(--m3e-fab-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-tertiary-fab-focus-container-elevation, var(--m3e-fab-focus-container-elevation, ${A.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-tertiary-fab-lowered-focus-container-elevation, var(--m3e-fab-lowered-focus-container-elevation, ${A.elevation.level2}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-tertiary-fab-pressed-icon-color, var(--m3e-fab-pressed-icon-color, ${A.color.onTertiary}))`),
			labelTextColor: c(`var(--m3e-tertiary-fab-pressed-label-text-color, var(--m3e-fab-pressed-label-text-color, ${A.color.onTertiary}))`),
			stateLayerColor: c(`var(--m3e-tertiary-fab-pressed-state-layer-color, var(--m3e-fab-pressed-state-layer-color, ${A.color.onTertiary}))`),
			stateLayerOpacity: c(`var(--m3e-tertiary-fab-pressed-state-layer-opacity, var(--m3e-fab-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-tertiary-fab-pressed-container-elevation, var(--m3e-fab-pressed-container-elevation, ${A.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-tertiary-fab-lowered-pressed-container-elevation, var(--m3e-fab-lowered-pressed-container-elevation, ${A.elevation.level2}))`)
		}
	},
	"primary-container": {
		labelTextColor: c(`var(--m3e-primary-container-fab-label-text-color, var(--m3e-primary-container-fab-label-text-color, ${A.color.onPrimaryContainer}))`),
		iconColor: c(`var(--m3e-primary-container-fab-icon-color, var(--m3e-primary-container-fab-icon-color, ${A.color.onPrimaryContainer}))`),
		containerColor: c(`var(--m3e-primary-container-fab-container-color, var(--m3e-fab-container-color, ${A.color.primaryContainer}))`),
		containerElevation: c(`var(--m3e-primary-container-fab-container-elevation, var(--m3e-fab-container-elevation, ${A.elevation.level3}))`),
		loweredContainerElevation: c(`var(--m3e-primary-container-fab-lowered-container-elevation, var(--m3e-fab-lowered-container-elevation, ${A.elevation.level2}))`),
		disabled: {
			containerColor: c(`var(--m3e-primary-container-fab-disabled-container-color, var(--m3e-fab-disabled-container-color, ${A.color.onSurface}))`),
			containerOpacity: c("var(--m3e-primary-container-fab-disabled-container-opacity, var(--m3e-fab-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-primary-container-fab-disabled-icon-color, var(--m3e-fab-disabled-icon-color, ${A.color.onSurface}))`),
			iconOpacity: c("var(--m3e-primary-container-fab-disabled-icon-opacity, var(--m3e-fab-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-primary-container-fab-disabled-label-text-color, var(--m3e-fab-disabled-label-text-color, ${A.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-primary-container-fab-disabled-label-text-opacity, var(--m3e-fab-disabled-label-text-opacity, 38%))"),
			containerElevation: c(`var(--m3e-primary-container-fab-disabled-container-elevation, var(--m3e-fab-disabled-container-elevation, ${A.elevation.level0}))`),
			loweredContainerElevation: c(`var(--m3e-primary-container-fab-lowered-disabled-container-elevation, var(--m3e-fab-lowered-disabled-container-elevation, ${A.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-primary-container-fab-hover-icon-color, var(--m3e-fab-hover-icon-color, ${A.color.onPrimaryContainer}))`),
			labelTextColor: c(`var(--m3e-primary-container-fab-hover-label-text-color, var(--m3e-fab-hover-label-text-color, ${A.color.onPrimaryContainer}))`),
			stateLayerColor: c(`var(--m3e-primary-container-fab-hover-state-layer-color, var(--m3e-fab-hover-state-layer-color, ${A.color.onPrimaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-primary-container-fab-hover-state-layer-opacity, var(--m3e-fab-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-primary-container-fab-hover-container-elevation, var(--m3e-fab-hover-container-elevation, ${A.elevation.level4}))`),
			loweredContainerElevation: c(`var(--m3e-primary-container-fab-lowered-hover-container-elevation, var(--m3e-fab-lowered-hover-container-elevation, ${A.elevation.level3}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-primary-container-fab-focus-icon-color, var(--m3e-fab-focus-icon-color, ${A.color.onPrimaryContainer}))`),
			labelTextColor: c(`var(--m3e-primary-container-fab-focus-label-text-color, var(--m3e-fab-focus-label-text-color, ${A.color.onPrimaryContainer}))`),
			stateLayerColor: c(`var(--m3e-primary-container-fab-focus-state-layer-color, var(--m3e-fab-focus-state-layer-color, ${A.color.onPrimaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-primary-container-fab-focus-state-layer-opacity, var(--m3e-fab-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-primary-container-fab-focus-container-elevation, var(--m3e-fab-focus-container-elevation, ${A.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-primary-container-fab-lowered-focus-container-elevation, var(--m3e-fab-lowered-focus-container-elevation, ${A.elevation.level2}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-primary-container-fab-pressed-icon-color, var(--m3e-fab-pressed-icon-color, ${A.color.onPrimaryContainer}))`),
			labelTextColor: c(`var(--m3e-primary-container-fab-pressed-label-text-color, var(--m3e-fab-pressed-label-text-color, ${A.color.onPrimaryContainer}))`),
			stateLayerColor: c(`var(--m3e-primary-container-fab-pressed-state-layer-color, var(--m3e-fab-pressed-state-layer-color, ${A.color.onPrimaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-primary-container-fab-pressed-state-layer-opacity, var(--m3e-fab-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-primary-container-fab-pressed-container-elevation, var(--m3e-fab-pressed-container-elevation, ${A.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-primary-container-fab-lowered-pressed-container-elevation, var(--m3e-fab-lowered-pressed-container-elevation, ${A.elevation.level2}))`)
		}
	},
	"secondary-container": {
		labelTextColor: c(`var(--m3e-secondary-container-fab-label-text-color, var(--m3e-fab-label-text-color, ${A.color.onSecondaryContainer}))`),
		iconColor: c(`var(--m3e-secondary-container-fab-icon-color, var(--m3e-fab-icon-color, ${A.color.onSecondaryContainer}))`),
		containerColor: c(`var(--m3e-secondary-container-fab-container-color, var(--m3e-fab-container-color, ${A.color.secondaryContainer}))`),
		containerElevation: c(`var(--m3e-secondary-container-fab-container-elevation, var(--m3e-fab-container-elevation, ${A.elevation.level3}))`),
		loweredContainerElevation: c(`var(--m3e-secondary-container-fab-lowered-container-elevation, var(--m3e-fab-lowered-container-elevation, ${A.elevation.level2}))`),
		disabled: {
			containerColor: c(`var(--m3e-secondary-container-fab-disabled-container-color, var(--m3e-fab-disabled-container-color, ${A.color.onSurface}))`),
			containerOpacity: c("var(--m3e-secondary-container-fab-disabled-container-opacity, var(--m3e-fab-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-secondary-container-fab-disabled-icon-color, var(--m3e-fab-disabled-icon-color, ${A.color.onSurface}))`),
			iconOpacity: c("var(--m3e-secondary-container-fab-disabled-icon-opacity, var(--m3e-fab-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-secondary-container-fab-disabled-label-text-color, var(--m3e-fab-disabled-label-text-color, ${A.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-secondary-container-fab-disabled-label-text-opacity, var(--m3e-fab-disabled-label-text-opacity, 38%))"),
			containerElevation: c(`var(--m3e-secondary-container-fab-disabled-container-elevation, var(--m3e-fab-disabled-container-elevation, ${A.elevation.level0}))`),
			loweredContainerElevation: c(`var(--m3e-secondary-container-fab-lowered-disabled-container-elevation, var(--m3e-fab-lowered-disabled-container-elevation, ${A.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-secondary-container-fab-hover-icon-color, var(--m3e-fab-hover-icon-color, ${A.color.onSecondaryContainer}))`),
			labelTextColor: c(`var(--m3e-secondary-container-fab-hover-label-text-color, var(--m3e-fab-hover-label-text-color, ${A.color.onSecondaryContainer}))`),
			stateLayerColor: c(`var(--m3e-secondary-container-fab-hover-state-layer-color, var(--m3e-fab-hover-state-layer-color, ${A.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-secondary-container-fab-hover-state-layer-opacity, var(--m3e-fab-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-secondary-container-fab-hover-container-elevation, var(--m3e-fab-hover-container-elevation, ${A.elevation.level4}))`),
			loweredContainerElevation: c(`var(--m3e-secondary-container-fab-lowered-hover-container-elevation, var(--m3e-fab-lowered-hover-container-elevation, ${A.elevation.level3}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-secondary-container-fab-focus-icon-color, var(--m3e-fab-focus-icon-color, ${A.color.onSecondaryContainer}))`),
			labelTextColor: c(`var(--m3e-secondary-container-fab-focus-label-text-color, var(--m3e-fab-focus-label-text-color, ${A.color.onSecondaryContainer}))`),
			stateLayerColor: c(`var(--m3e-secondary-container-fab-focus-state-layer-color, var(--m3e-fab-focus-state-layer-color, ${A.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-secondary-container-fab-focus-state-layer-opacity, var(--m3e-fab-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-secondary-container-fab-focus-container-elevation, var(--m3e-fab-focus-container-elevation, ${A.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-secondary-container-fab-lowered-focus-container-elevation, var(--m3e-fab-lowered-focus-container-elevation, ${A.elevation.level2}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-secondary-container-fab-pressed-icon-color, var(--m3e-fab-pressed-icon-color, ${A.color.onSecondaryContainer}))`),
			labelTextColor: c(`var(--m3e-secondary-container-fab-pressed-label-text-color, var(--m3e-fab-pressed-label-text-color, ${A.color.onSecondaryContainer}))`),
			stateLayerColor: c(`var(--m3e-secondary-container-fab-pressed-state-layer-color, var(--m3e-fab-pressed-state-layer-color, ${A.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-secondary-container-fab-pressed-state-layer-opacity, var(--m3e-fab-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-secondary-container-fab-pressed-container-elevation, var(--m3e-fab-pressed-container-elevation, ${A.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-secondary-container-fab-lowered-pressed-container-elevation, var(--m3e-fab-lowered-pressed-container-elevation, ${A.elevation.level2}))`)
		}
	},
	"tertiary-container": {
		labelTextColor: c(`var(--m3e-tertiary-container-fab-label-text-color, var(--m3e-fab-label-text-color, ${A.color.onTertiaryContainer}))`),
		iconColor: c(`var(--m3e-tertiary-container-fab-icon-color, var(--m3e-fab-icon-color, ${A.color.onTertiaryContainer}))`),
		containerColor: c(`var(--m3e-tertiary-container-fab-container-color, var(--m3e-fab-container-color, ${A.color.tertiaryContainer}))`),
		containerElevation: c(`var(--m3e-tertiary-container-fab-container-elevation, var(--m3e-fab-container-elevation, ${A.elevation.level3}))`),
		loweredContainerElevation: c(`var(--m3e-tertiary-container-fab-lowered-container-elevation, var(--m3e-fab-lowered-container-elevation, ${A.elevation.level2}))`),
		disabled: {
			containerColor: c(`var(--m3e-tertiary-container-fab-disabled-container-color, var(--m3e-fab-disabled-container-color, ${A.color.onSurface}))`),
			containerOpacity: c("var(--m3e-tertiary-container-fab-disabled-container-opacity, var(--m3e-fab-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-tertiary-container-fab-disabled-icon-color, var(--m3e-fab-disabled-icon-color, ${A.color.onSurface}))`),
			iconOpacity: c("var(--m3e-tertiary-container-fab-disabled-icon-opacity, var(--m3e-fab-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-tertiary-container-fab-disabled-label-text-color, var(--m3e-fab-disabled-label-text-color, ${A.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-tertiary-container-fab-disabled-label-text-opacity, var(--m3e-fab-disabled-label-text-opacity, 38%))"),
			containerElevation: c(`var(--m3e-tertiary-container-fab-disabled-container-elevation, var(--m3e-fab-disabled-container-elevation, ${A.elevation.level0}))`),
			loweredContainerElevation: c(`var(--m3e-tertiary-container-fab-lowered-disabled-container-elevation, var(--m3e-fab-lowered-disabled-container-elevation, ${A.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-tertiary-container-fab-hover-icon-color, var(--m3e-fab-hover-icon-color, ${A.color.onTertiaryContainer}))`),
			labelTextColor: c(`var(--m3e-tertiary-container-fab-hover-label-text-color, var(--m3e-fab-hover-label-text-color, ${A.color.onTertiaryContainer}))`),
			stateLayerColor: c(`var(--m3e-tertiary-container-fab-hover-state-layer-color, var(--m3e-fab-hover-state-layer-color, ${A.color.onTertiaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-tertiary-container-fab-hover-state-layer-opacity, var(--m3e-fab-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-tertiary-container-fab-hover-container-elevation, var(--m3e-fab-hover-container-elevation, ${A.elevation.level4}))`),
			loweredContainerElevation: c(`var(--m3e-tertiary-container-fab-lowered-hover-container-elevation, var(--m3e-fab-lowered-hover-container-elevation, ${A.elevation.level3}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-tertiary-container-fab-focus-icon-color, var(--m3e-fab-focus-icon-color, ${A.color.onTertiaryContainer}))`),
			labelTextColor: c(`var(--m3e-tertiary-container-fab-focus-label-text-color, var(--m3e-fab-focus-label-text-color, ${A.color.onTertiaryContainer}))`),
			stateLayerColor: c(`var(--m3e-tertiary-container-fab-focus-state-layer-color, var(--m3e-fab-focus-state-layer-color, ${A.color.onTertiaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-tertiary-container-fab-focus-state-layer-opacity, var(--m3e-fab-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-tertiary-container-fab-focus-container-elevation, var(--m3e-fab-focus-container-elevation, ${A.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-tertiary-container-fab-lowered-focus-container-elevation, var(--m3e-fab-lowered-focus-container-elevation, ${A.elevation.level2}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-tertiary-container-fab-pressed-icon-color, var(--m3e-fab-pressed-icon-color, ${A.color.onTertiaryContainer}))`),
			labelTextColor: c(`var(--m3e-tertiary-container-fab-pressed-label-text-color, var(--m3e-fab-pressed-label-text-color, ${A.color.onTertiaryContainer}))`),
			stateLayerColor: c(`var(--m3e-tertiary-container-fab-pressed-state-layer-color, var(--m3e-fab-pressed-state-layer-color, ${A.color.onTertiaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-tertiary-container-fab-pressed-state-layer-opacity, var(--m3e-fab-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-tertiary-container-fab-pressed-container-elevation, var(--m3e-fab-pressed-container-elevation, ${A.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-tertiary-container-fab-lowered-pressed-container-elevation, var(--m3e-fab-lowered-pressed-container-elevation, ${A.elevation.level2}))`)
		}
	},
	surface: {
		labelTextColor: c(`var(--m3e-surface-fab-label-text-color, var(--m3e-fab-label-text-color, ${A.color.primary}))`),
		iconColor: c(`var(--m3e-surface-fab-icon-color, var(--m3e-fab-icon-color, ${A.color.primary}))`),
		containerColor: c(`var(--m3e-surface-fab-container-color, var(--m3e-fab-container-color, ${A.color.surfaceContainerHigh}))`),
		containerElevation: c(`var(--m3e-surface-fab-container-elevation, var(--m3e-fab-container-elevation, ${A.elevation.level3}))`),
		loweredContainerElevation: c(`var(--m3e-surface-fab-lowered-container-elevation, var(--m3e-fab-lowered-container-elevation, ${A.elevation.level2}))`),
		loweredContainerColor: c(`var(--m3e-surface-fab-lowered-container-color, var(--m3e-fab-lowered-container-color, ${A.color.surfaceContainerLow}))`),
		disabled: {
			containerColor: c(`var(--m3e-surface-fab-disabled-container-color, var(--m3e-fab-disabled-container-color, ${A.color.onSurface}))`),
			containerOpacity: c("var(--m3e-surface-fab-disabled-container-opacity, var(--m3e-fab-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-surface-fab-disabled-icon-color, var(--m3e-fab-disabled-icon-color, ${A.color.onSurface}))`),
			iconOpacity: c("var(--m3e-surface-fab-disabled-icon-opacity, var(--m3e-fab-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-surface-fab-disabled-label-text-color, var(--m3e-fab-disabled-label-text-color, ${A.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-surface-fab-disabled-label-text-opacity, var(--m3e-fab-disabled-label-text-opacity, 38%))"),
			containerElevation: c(`var(--m3e-surface-fab-disabled-container-elevation, var(--m3e-fab-disabled-container-elevation, ${A.elevation.level0}))`),
			loweredContainerElevation: c(`var(--m3e-surface-fab-lowered-disabled-container-elevation, var(--m3e-fab-lowered-disabled-container-elevation, ${A.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-surface-fab-hover-icon-color, var(--m3e-fab-hover-icon-color, ${A.color.primary}))`),
			labelTextColor: c(`var(--m3e-surface-fab-hover-label-text-color, var(--m3e-fab-hover-label-text-color, ${A.color.primary}))`),
			stateLayerColor: c(`var(--m3e-surface-fab-hover-state-layer-color, var(--m3e-fab-hover-state-layer-color, ${A.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-surface-fab-hover-state-layer-opacity, var(--m3e-fab-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-surface-fab-hover-container-elevation, var(--m3e-fab-hover-container-elevation, ${A.elevation.level4}))`),
			loweredContainerElevation: c(`var(--m3e-surface-fab-lowered-hover-container-elevation, var(--m3e-fab-lowered-hover-container-elevation, ${A.elevation.level3}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-surface-fab-focus-icon-color, var(--m3e-fab-focus-icon-color, ${A.color.primary}))`),
			labelTextColor: c(`var(--m3e-surface-fab-focus-label-text-color, var(--m3e-fab-focus-label-text-color, ${A.color.primary}))`),
			stateLayerColor: c(`var(--m3e-surface-fab-focus-state-layer-color, var(--m3e-fab-focus-state-layer-color, ${A.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-surface-fab-focus-state-layer-opacity, var(--m3e-fab-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-surface-fab-focus-container-elevation, var(--m3e-fab-focus-container-elevation, ${A.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-surface-fab-lowered-focus-container-elevation, var(--m3e-fab-lowered-focus-container-elevation, ${A.elevation.level2}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-surface-fab-pressed-icon-color, var(--m3e-fab-pressed-icon-color, ${A.color.primary}))`),
			labelTextColor: c(`var(--m3e-surface-fab-pressed-label-text-color, var(--m3e-fab-pressed-label-text-color, ${A.color.primary}))`),
			stateLayerColor: c(`var(--m3e-surface-fab-pressed-state-layer-color, var(--m3e-fab-pressed-state-layer-color, ${A.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-surface-fab-pressed-state-layer-opacity, var(--m3e-fab-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-surface-fab-pressed-container-elevation, var(--m3e-fab-pressed-container-elevation, ${A.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-surface-fab-lowered-pressed-container-elevation, var(--m3e-fab-lowered-pressed-container-elevation, ${A.elevation.level2}))`)
		}
	}
};
function Kl(e) {
	let t = c(`:is(:state(--${e}), :--${e})`);
	return l`:host(${t}:not([lowered])) .base { background-color: ${U[e].containerColor}; --m3e-elevation-level: ${U[e].containerElevation}; --m3e-elevation-hover-level: ${U[e].hover.containerElevation}; --m3e-elevation-focus-level: ${U[e].focus.containerElevation}; --m3e-elevation-pressed-level: ${U[e].pressed.containerElevation}; } :host(${t}[lowered]) .base { background-color: ${U[e].loweredContainerColor ?? U[e].containerColor}; --m3e-elevation-level: ${U[e].loweredContainerElevation}; --m3e-elevation-hover-level: ${U[e].hover.loweredContainerElevation}; --m3e-elevation-focus-level: ${U[e].focus.loweredContainerElevation}; --m3e-elevation-pressed-level: ${U[e].pressed.loweredContainerElevation}; } :host(${t}) .base { --m3e-state-layer-hover-color: ${U[e].hover.stateLayerColor}; --m3e-state-layer-hover-opacity: ${U[e].hover.stateLayerOpacity}; --m3e-state-layer-focus-color: ${U[e].focus.stateLayerColor}; --m3e-state-layer-focus-opacity: ${U[e].focus.stateLayerOpacity}; --m3e-ripple-color: ${U[e].pressed.stateLayerColor}; --m3e-ripple-opacity: ${U[e].pressed.stateLayerOpacity}; } :host(${t}) .label { color: ${U[e].labelTextColor}; } :host(${t}:focus) .label { color: ${U[e].focus.labelTextColor}; } :host(${t}:hover) .label { color: ${U[e].hover.labelTextColor}; } :host(${t}) .base.pressed .label { color: ${U[e].pressed.labelTextColor}; } :host(${t}) .icon { color: ${U[e].iconColor}; } :host(${t}:focus) .icon { color: ${U[e].focus.iconColor}; } :host(${t}:hover) .icon { color: ${U[e].hover.iconColor}; } :host(${t}) .base.pressed .icon { color: ${U[e].pressed.iconColor}; } :host(${t}:disabled) .base, :host(${t}[disabled-interactive]) .base { --m3e-elevation-level: ${U[e].disabled.containerElevation}; background-color: color-mix( in srgb, ${U[e].disabled.containerColor} ${U[e].disabled.containerOpacity}, transparent ); } :host(${t}:disabled) .label, :host(${t}[disabled-interactive]) .label { color: color-mix( in srgb, ${U[e].disabled.labelTextColor} ${U[e].disabled.labelTextOpacity}, transparent ); } :host(${t}:disabled) .icon, :host(${t}[disabled-interactive]) .icon { color: color-mix( in srgb, ${U[e].disabled.iconColor} ${U[e].disabled.iconOpacity}, transparent ); }`;
}
var ql = [
	Kl("primary"),
	Kl("secondary"),
	Kl("tertiary"),
	Kl("primary-container"),
	Kl("secondary-container"),
	Kl("tertiary-container"),
	Kl("surface")
], Jl, Yl, Xl, Zl, Ql = class extends qi(Zi(Ui(Vi(Ci(vi(T(j(x, "button"), !0))))))) {
	constructor() {
		super(), Jl.add(this), this.variant = "primary-container", this.lowered = !1, this.size = "medium", this.extended = !1, new nr(this, {
			isPressedKey: (e) => e === " ",
			callback: (e) => {
				!this.disabled && !this.disabledInteractive && (this._base?.classList.toggle("pressed", e), this._base?.classList.toggle("resting", !e));
			}
		});
	}
	connectedCallback() {
		super.connectedCallback(), t(this, Jl, "m", Yl).call(this), t(this, Jl, "m", Xl).call(this);
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this._base?.classList.toggle("pressed", !1), this._base?.classList.toggle("resting", !1);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, Jl, "m", Yl).call(this), e.has("size") && t(this, Jl, "m", Xl).call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), [
			this._elevation,
			this._focusRing,
			this._stateLayer,
			this._ripple
		].forEach((e) => e?.attach(this));
	}
	updated(e) {
		super.updated(e), (e.has("disabled") && this.disabled || e.has("disabledInteractive") && this.disabledInteractive) && (this._base?.classList.toggle("pressed", !1), this._base?.classList.toggle("resting", !1));
	}
	render() {
		return y`<div class="base"><m3e-elevation class="elevation" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-elevation><m3e-state-layer class="state-layer" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-state-layer><m3e-focus-ring class="focus-ring" ?disabled="${this.disabled}"></m3e-focus-ring><m3e-ripple class="ripple" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-ripple><div class="touch" aria-hidden="true"></div>${this[Ji]()}<div class="wrapper"><slot class="icon" aria-hidden="true" @slotchange="${t(this, Jl, "m", Zl)}"></slot><slot class="icon" aria-hidden="true" name="close-icon"><svg class="close-icon" viewBox="0 -960 960 960" fill="currentColor"><path d="m256-200-56-56 224-224-224-224 56-56 224 224 224-224 56 56-224 224 224 224-56 56-224-224-224 224Z"/></svg></slot><m3e-collapsible class="label-wrapper" orientation="horizontal" ?open="${this.extended}"><div class="label"><slot name="label" @slotchange="${t(this, Jl, "m", Zl)}"></slot></div></m3e-collapsible></div></div>`;
	}
};
Jl = /* @__PURE__ */ new WeakSet(), Yl = function() {
	Vl(this.variant) || (this.variant = "primary-container"), Br(this, this.variant, "primary", "primary-container", "secondary", "secondary-container", "surface", "tertiary", "tertiary-container");
}, Xl = function() {
	Bl(this.size) || (this.size = "medium"), Br(this, this.size, "large", "medium", "small");
}, Zl = function() {
	this._base?.classList.toggle("with-menu", this.querySelector("m3e-fab-menu-trigger") !== null);
}, Ql.styles = [
	Gl,
	ql,
	Hl
], e([C(".base")], Ql.prototype, "_base", void 0), e([C(".elevation")], Ql.prototype, "_elevation", void 0), e([C(".focus-ring")], Ql.prototype, "_focusRing", void 0), e([C(".state-layer")], Ql.prototype, "_stateLayer", void 0), e([C(".ripple")], Ql.prototype, "_ripple", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], Ql.prototype, "variant", void 0), e([S({
	type: Boolean,
	reflect: !0
})], Ql.prototype, "lowered", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], Ql.prototype, "size", void 0), e([S({
	type: Boolean,
	reflect: !0
})], Ql.prototype, "extended", void 0), Ql = e([w("m3e-fab")], Ql);
//#endregion
//#region node_modules/@m3e/web/dist/core-a11y.js
var $l = [
	"alt",
	"shift",
	"ctrl",
	"meta"
], eu;
(function(e) {
	e[e.Backspace = 8] = "Backspace", e[e.Tab = 9] = "Tab", e[e.Enter = 13] = "Enter", e[e.Shift = 16] = "Shift", e[e.Ctrl = 17] = "Ctrl", e[e.Alt = 18] = "Alt", e[e.PauseBreak = 19] = "PauseBreak", e[e.CapsLock = 20] = "CapsLock", e[e.Escape = 27] = "Escape", e[e.Space = 32] = "Space", e[e.PageUp = 33] = "PageUp", e[e.PageDown = 34] = "PageDown", e[e.End = 35] = "End", e[e.Home = 36] = "Home", e[e.LeftArrow = 37] = "LeftArrow", e[e.UpArrow = 38] = "UpArrow", e[e.RightArrow = 39] = "RightArrow", e[e.DownArrow = 40] = "DownArrow", e[e.Insert = 45] = "Insert", e[e.Delete = 46] = "Delete", e[e.Zero = 48] = "Zero", e[e.ClosedParen = 48] = "ClosedParen", e[e.One = 49] = "One", e[e.ExclamationMark = 49] = "ExclamationMark", e[e.Two = 50] = "Two", e[e.AtSign = 50] = "AtSign", e[e.Three = 51] = "Three", e[e.PoundSign = 51] = "PoundSign", e[e.Hash = 51] = "Hash", e[e.Four = 52] = "Four", e[e.DollarSign = 52] = "DollarSign", e[e.Five = 53] = "Five", e[e.PercentSign = 53] = "PercentSign", e[e.Six = 54] = "Six", e[e.Caret = 54] = "Caret", e[e.Hat = 54] = "Hat", e[e.Seven = 55] = "Seven", e[e.Ampersand = 55] = "Ampersand", e[e.Eight = 56] = "Eight", e[e.Star = 56] = "Star", e[e.Asterik = 56] = "Asterik", e[e.Nine = 57] = "Nine", e[e.OpenParen = 57] = "OpenParen", e[e.A = 65] = "A", e[e.B = 66] = "B", e[e.C = 67] = "C", e[e.D = 68] = "D", e[e.E = 69] = "E", e[e.F = 70] = "F", e[e.G = 71] = "G", e[e.H = 72] = "H", e[e.I = 73] = "I", e[e.J = 74] = "J", e[e.K = 75] = "K", e[e.L = 76] = "L", e[e.M = 77] = "M", e[e.N = 78] = "N", e[e.O = 79] = "O", e[e.P = 80] = "P", e[e.Q = 81] = "Q", e[e.R = 82] = "R", e[e.S = 83] = "S", e[e.T = 84] = "T", e[e.U = 85] = "U", e[e.V = 86] = "V", e[e.W = 87] = "W", e[e.X = 88] = "X", e[e.Y = 89] = "Y", e[e.Z = 90] = "Z", e[e.LeftWindowKey = 91] = "LeftWindowKey", e[e.RightWindowKey = 92] = "RightWindowKey", e[e.SelectKey = 93] = "SelectKey", e[e.Numpad0 = 96] = "Numpad0", e[e.Numpad1 = 97] = "Numpad1", e[e.Numpad2 = 98] = "Numpad2", e[e.Numpad3 = 99] = "Numpad3", e[e.Numpad4 = 100] = "Numpad4", e[e.Numpad5 = 101] = "Numpad5", e[e.Numpad6 = 102] = "Numpad6", e[e.Numpad7 = 103] = "Numpad7", e[e.Numpad8 = 104] = "Numpad8", e[e.Numpad9 = 105] = "Numpad9", e[e.Multiply = 106] = "Multiply", e[e.Add = 107] = "Add", e[e.Subtract = 109] = "Subtract", e[e.DecimalPoint = 110] = "DecimalPoint", e[e.Divide = 111] = "Divide", e[e.F1 = 112] = "F1", e[e.F2 = 113] = "F2", e[e.F3 = 114] = "F3", e[e.F4 = 115] = "F4", e[e.F5 = 116] = "F5", e[e.F6 = 117] = "F6", e[e.F7 = 118] = "F7", e[e.F8 = 119] = "F8", e[e.F9 = 120] = "F9", e[e.F10 = 121] = "F10", e[e.F11 = 122] = "F11", e[e.F12 = 123] = "F12", e[e.NumLock = 144] = "NumLock", e[e.ScrollLock = 145] = "ScrollLock", e[e.SemiColon = 186] = "SemiColon", e[e.Equals = 187] = "Equals", e[e.Comma = 188] = "Comma", e[e.Dash = 189] = "Dash", e[e.Period = 190] = "Period", e[e.UnderScore = 189] = "UnderScore", e[e.PlusSign = 187] = "PlusSign", e[e.ForwardSlash = 191] = "ForwardSlash", e[e.Tilde = 192] = "Tilde", e[e.GraveAccent = 192] = "GraveAccent", e[e.OpenBracket = 219] = "OpenBracket", e[e.ClosedBracket = 221] = "ClosedBracket", e[e.Quote = 222] = "Quote";
})(eu ||= {});
function tu(e) {
	return e.which || e.charCode || e.keyCode;
}
function nu(e, ...t) {
	return t.length ? t.some((t) => e[`${t}Key`]) : e.altKey || e.shiftKey || e.ctrlKey || e.metaKey;
}
function ru(e, ...t) {
	return $l.every((n) => !nu(e, n) || t.includes(n));
}
var iu, au, ou, su = class {
	constructor() {
		iu.set(this, []), au.set(this, null), ou.set(this, void 0);
	}
	get items() {
		return t(this, iu, "f");
	}
	get activeItem() {
		return t(this, au, "f");
	}
	setItems(e) {
		let t = this.items.filter((t) => !e.includes(t)), r = e.filter((e) => !this.items.includes(e));
		return n(this, iu, e, "f"), this.activeItem && !this.items.includes(this.activeItem) && this.updateActiveItem(null), {
			added: r,
			removed: t
		};
	}
	setActiveItem(e) {
		this.activeItem !== e && (this.updateActiveItem(e), t(this, ou, "f")?.call(this));
	}
	updateActiveItem(e) {
		n(this, au, e ?? null, "f");
	}
	onActiveItemChange(e) {
		return n(this, ou, e, "f"), this;
	}
};
iu = /* @__PURE__ */ new WeakMap(), au = /* @__PURE__ */ new WeakMap(), ou = /* @__PURE__ */ new WeakMap();
var cu, lu, uu, du, fu, pu, mu, hu, gu, _u, vu = Symbol("typeaheadLabel"), yu = class {
	constructor(e) {
		cu.add(this), lu.set(this, void 0), uu.set(this, -1), du.set(this, void 0), fu.set(this, void 0), pu.set(this, []), mu.set(this, -1), hu.set(this, []), n(this, lu, e.debounceInterval ?? 200, "f"), n(this, du, e.callback, "f"), n(this, fu, e.skipPredicate, "f");
	}
	get isTyping() {
		return t(this, pu, "f").length > 0;
	}
	setItems(e) {
		n(this, hu, e, "f");
	}
	setSelectedIndex(e) {
		n(this, mu, e, "f");
	}
	reset() {
		t(this, pu, "f").length = 0;
	}
	onKeyDown(e) {
		if (e.key && e.key.length === 1) t(this, cu, "m", gu).call(this, e.key);
		else {
			let n = tu(e);
			(n >= eu.A && n <= eu.Z || n >= eu.Zero && n <= eu.Nine) && t(this, cu, "m", gu).call(this, String.fromCharCode(n));
		}
	}
};
lu = /* @__PURE__ */ new WeakMap(), uu = /* @__PURE__ */ new WeakMap(), du = /* @__PURE__ */ new WeakMap(), fu = /* @__PURE__ */ new WeakMap(), pu = /* @__PURE__ */ new WeakMap(), mu = /* @__PURE__ */ new WeakMap(), hu = /* @__PURE__ */ new WeakMap(), cu = /* @__PURE__ */ new WeakSet(), gu = function(e) {
	t(this, pu, "f").push(e.toLocaleUpperCase()), clearTimeout(t(this, uu, "f")), n(this, uu, setTimeout(() => t(this, cu, "m", _u).call(this), t(this, lu, "f")), "f");
}, _u = function() {
	let e = t(this, pu, "f").join("");
	for (let n = 1; n < t(this, hu, "f").length + 1; n++) {
		let r = (t(this, mu, "f") + n) % t(this, hu, "f").length, i = t(this, hu, "f")[r], a = i[vu]?.().toLocaleUpperCase().trim();
		if (!t(this, fu, "f")?.call(this, i) && a?.indexOf(e) === 0) {
			t(this, du, "f").call(this, i);
			break;
		}
	}
	this.reset();
};
var bu, xu, Su, Cu, wu, Tu, Eu, Du, Ou = class extends su {
	constructor() {
		super(...arguments), bu.add(this), xu.set(this, void 0), this.wrap = !1, this.homeAndEnd = !1, this.pageUpAndDown = !1, this.pageDelta = 10, this.vertical = !1, this.allowedModifiers = [], this.skipPredicate = (e) => _i(e) && e.disabled, this.directionality = "ltr";
	}
	setItems(e) {
		return t(this, xu, "f")?.setItems(e), super.setItems(e);
	}
	updateActiveItem(e) {
		super.updateActiveItem(e), t(this, xu, "f") && t(this, xu, "f").setSelectedIndex(e ? this.items.indexOf(e) : -1);
	}
	withHomeAndEnd(e = !0) {
		return this.homeAndEnd = e, this;
	}
	withPageUpAndDown(e = !0, t = 10) {
		return this.pageUpAndDown = e, this.pageDelta = t, this;
	}
	withWrap(e = !0) {
		return this.wrap = e, this;
	}
	withVerticalOrientation(e = !0) {
		return this.vertical = e, this;
	}
	withAllowedModifiers(...e) {
		return this.allowedModifiers = e, this;
	}
	withTypeahead(e = !0) {
		return e ? n(this, xu, new yu({ callback: (e) => this.setActiveItem(e) }), "f") : n(this, xu, void 0, "f"), this;
	}
	withSkipPredicate(e) {
		return this.skipPredicate = e, this;
	}
	withDirectionality(e) {
		return this.directionality = e, this;
	}
	onKeyDown(e) {
		if (e.defaultPrevented) return;
		let n = ru(e, ...this.allowedModifiers);
		switch (e.key) {
			case "Left":
			case "ArrowLeft":
				if (n && !this.vertical) {
					e.preventDefault();
					let n = this.directionality === "ltr" ? t(this, bu, "m", Tu).call(this) : t(this, bu, "m", wu).call(this);
					n && this.setActiveItem(n);
				}
				break;
			case "Up":
			case "ArrowUp":
				if (n) {
					e.preventDefault();
					let n = this.directionality === "ltr" ? t(this, bu, "m", Tu).call(this) : t(this, bu, "m", wu).call(this);
					n && this.setActiveItem(n);
				}
				break;
			case "Right":
			case "ArrowRight":
				if (n && !this.vertical) {
					e.preventDefault();
					let n = this.directionality === "ltr" ? t(this, bu, "m", wu).call(this) : t(this, bu, "m", Tu).call(this);
					n && this.setActiveItem(n);
				}
				break;
			case "Down":
			case "ArrowDown":
				if (n) {
					e.preventDefault();
					let n = this.directionality === "ltr" ? t(this, bu, "m", wu).call(this) : t(this, bu, "m", Tu).call(this);
					n && this.setActiveItem(n);
				}
				break;
			case "Home":
				if (n && this.homeAndEnd) {
					e.preventDefault();
					let n = t(this, bu, "m", Su).call(this);
					n && this.setActiveItem(n);
				}
				break;
			case "End":
				if (n && this.homeAndEnd) {
					e.preventDefault();
					let n = t(this, bu, "m", Cu).call(this);
					n && this.setActiveItem(n);
				}
				break;
			case "PageUp":
				if (n && this.pageUpAndDown) {
					e.preventDefault();
					let n = this.activeItem ? Math.max(0, this.items.indexOf(this.activeItem) - this.pageDelta) : 0, r = this.directionality === "ltr" ? t(this, bu, "m", Eu).call(this, n) : t(this, bu, "m", Du).call(this, n);
					r && this.setActiveItem(r);
				}
				break;
			case "PageDown":
				if (n && this.pageUpAndDown) {
					e.preventDefault();
					let n = this.activeItem ? Math.min(this.items.length - 1, this.items.indexOf(this.activeItem) + this.pageDelta) : this.items.length - 1, r = this.directionality === "ltr" ? t(this, bu, "m", Du).call(this, n) : t(this, bu, "m", Eu).call(this, n);
					r && this.setActiveItem(r);
				}
				break;
			default: (n || nu(e, "shift")) && t(this, xu, "f")?.onKeyDown(e);
		}
	}
};
xu = /* @__PURE__ */ new WeakMap(), bu = /* @__PURE__ */ new WeakSet(), Su = function() {
	for (let e = 0; e < this.items.length && this.items[e] !== this.activeItem; e++) {
		let t = this.items[e];
		if (!this.skipPredicate(t)) return t;
	}
	return null;
}, Cu = function() {
	for (let e = this.items.length - 1; e >= 0 && this.items[e] !== this.activeItem; e--) {
		let t = this.items[e];
		if (!this.skipPredicate(t)) return t;
	}
	return null;
}, wu = function() {
	for (let e = 0; e < this.items.length; e++) if (this.items[e] === this.activeItem) {
		for (let t = e + 1; t < this.items.length; t++) {
			let e = this.items[t];
			if (!this.skipPredicate(e)) return e;
		}
		break;
	}
	return this.wrap ? t(this, bu, "m", Su).call(this) : null;
}, Tu = function() {
	for (let e = 0; e < this.items.length; e++) if (this.items[e] === this.activeItem) {
		for (let t = e - 1; t >= 0; t--) {
			let e = this.items[t];
			if (!this.skipPredicate(e)) return e;
		}
		break;
	}
	return this.wrap ? t(this, bu, "m", Cu).call(this) : null;
}, Eu = function(e) {
	for (let t = e; t >= 0; t--) {
		let e = this.items[t];
		if (!this.skipPredicate(e)) return e;
	}
	return null;
}, Du = function(e) {
	for (let t = e; t < this.items.length; t++) {
		let e = this.items[t];
		if (!this.skipPredicate(e)) return e;
	}
	return null;
};
var ku, Au = class extends Ou {
	constructor() {
		super(...arguments), ku.set(this, void 0);
	}
	setActiveItem(e) {
		super.setActiveItem(e), e?.focus(t(this, ku, "f"));
	}
	withOptions(e) {
		return n(this, ku, e, "f"), this;
	}
};
ku = /* @__PURE__ */ new WeakMap();
var ju, Mu = class extends Au {
	constructor() {
		super(...arguments), ju.set(this, !1);
	}
	updateActiveItem(e) {
		if (super.updateActiveItem(e), !t(this, ju, "f")) {
			e?.setAttribute("tabindex", "0");
			for (let t of this.items) t !== e && t.hasAttribute("tabindex") && t.setAttribute("tabindex", "-1");
		}
	}
	setItems(e) {
		let n = super.setItems(e);
		if (!t(this, ju, "f")) for (let e of n.added) e !== this.activeItem && !this.skipPredicate(e) && e.setAttribute("tabindex", "-1");
		return n;
	}
	disableRovingTabIndex(e = !0) {
		if (e !== t(this, ju, "f")) {
			n(this, ju, e, "f");
			for (let e of this.items) this.skipPredicate(e) || e?.setAttribute("tabindex", t(this, ju, "f") || e === this.activeItem ? "0" : "-1");
		}
		return this;
	}
};
ju = /* @__PURE__ */ new WeakMap();
var Nu, Pu = class extends Mu {
	constructor() {
		super(...arguments), Nu.set(this, !1);
	}
	get disabled() {
		return t(this, Nu, "f");
	}
	set disabled(e) {
		n(this, Nu, e, "f"), this.items.forEach((t) => t.disabled = e);
	}
	setItems(e) {
		this.disabled && e.forEach((e) => e.disabled = !0);
		let { added: t, removed: n } = super.setItems(e);
		if ((t.length > 0 || n.length > 0) && (this.activeItem || this.updateActiveItem(t.find((e) => !this.skipPredicate(e)) ?? null), this.activeItem && (this.activeItem.disabled || !qr(this.activeItem)))) {
			let e = t.find((e) => !this.skipPredicate(e) && qr(e));
			e && this.updateActiveItem(e);
		}
		return {
			added: t,
			removed: n
		};
	}
};
Nu = /* @__PURE__ */ new WeakMap();
var Fu, Iu, Lu, Ru, zu, W = Symbol("selectionManager"), Bu = class extends Pu {
	constructor() {
		super(...arguments), Fu.add(this), Iu.set(this, void 0), Lu.set(this, []), Ru.set(this, !1);
	}
	get multi() {
		return t(this, Ru, "f");
	}
	set multi(e) {
		n(this, Ru, e, "f"), t(this, Fu, "m", zu).call(this, !0);
	}
	get selectedItems() {
		return t(this, Lu, "f");
	}
	notifySelectionChange(e) {
		this.items.includes(e) && (qr(e) ? this.select(e) : this.deselect(e));
	}
	deselect(e) {
		if (this.items.includes(e)) {
			qr(e) && Jr(e, !1);
			let n = t(this, Lu, "f").indexOf(e);
			n >= 0 && (t(this, Lu, "f").splice(n, 1), t(this, Iu, "f")?.call(this));
		}
	}
	select(e, n = !0) {
		if (!e || this.items.includes(e)) {
			if (!this.multi) {
				for (let n of t(this, Lu, "f")) n !== e && Jr(n, !1);
				t(this, Lu, "f").length = 0;
			}
			e && (t(this, Lu, "f").push(e), qr(e) || Jr(e, !0)), n && this.updateActiveItem(e), t(this, Iu, "f")?.call(this);
		}
	}
	setItems(e) {
		let { added: r, removed: i } = super.setItems(e);
		return n(this, Lu, t(this, Lu, "f").filter((e) => !i.includes(e)), "f"), t(this, Lu, "f").push(...r.filter((e) => qr(e))), t(this, Fu, "m", zu).call(this), t(this, Iu, "f")?.call(this), {
			added: r,
			removed: i
		};
	}
	onSelectedItemsChange(e) {
		return n(this, Iu, e, "f"), this;
	}
};
Iu = /* @__PURE__ */ new WeakMap(), Lu = /* @__PURE__ */ new WeakMap(), Ru = /* @__PURE__ */ new WeakMap(), Fu = /* @__PURE__ */ new WeakSet(), zu = function(e = !1) {
	if (!this.multi && t(this, Lu, "f").length > 1) {
		for (let e = 1; e < t(this, Lu, "f").length; e++) Jr(t(this, Lu, "f")[e], !1);
		t(this, Lu, "f").length = 1, e && t(this, Iu, "f")?.call(this);
	}
};
function Vu(e, t) {
	return e.getAttribute(t)?.match(/\S+/g) ?? [];
}
function Hu(e, t, n) {
	n = n.trim();
	let r = Vu(e, t);
	r.some((e) => e.trim() === n) || (r.push(n), e.setAttribute(t, r.join(" ")));
}
function Uu(e, t, n) {
	n = n.trim();
	let r = Vu(e, t).filter((e) => e !== n);
	r.length > 0 ? e.setAttribute(t, r.join(" ")) : e.removeAttribute(t);
}
function Wu(e) {
	e.position = "absolute", e.appearance = "none", e.visibility = "hidden", e.border = "0", e.outline = "0", e.overflow = "hidden", e.left = "0", e.height = "1px", e.width = "1px", e.margin = "-1px", e.padding = "0", e.whiteSpace = "nowrap";
}
var Gu, Ku, qu, Ju, Yu, Xu = class {
	static describe(e, n, r = "tooltip") {
		let i = e.getRootNode();
		if (!(i instanceof ShadowRoot || i instanceof Document)) return;
		let a = t(this, Gu, "f", qu).get(i);
		a || (a = {
			containerElement: t(this, Gu, "m", Ju).call(this),
			messageRegistry: /* @__PURE__ */ new Map()
		}, (i instanceof Document ? i.body : i).appendChild(a.containerElement), t(this, Gu, "f", qu).set(i, a));
		let o = `${r}:${n}`, s = a.messageRegistry.get(o);
		s || (s = {
			messageElement: t(this, Gu, "m", Yu).call(this, n, r),
			count: 0
		}, a.containerElement.appendChild(s.messageElement), a.messageRegistry.set(o, s)), s.count++, Hu(e, "aria-describedby", s.messageElement.id);
	}
	static removeDescription(e, n, r = "tooltip") {
		let i = e.getRootNode(), a = t(this, Gu, "f", qu).get(i);
		if (!a) return;
		let o = `${r}:${n}`, s = a.messageRegistry.get(o);
		s && (s.count--, Uu(e, "aria-describedby", s.messageElement.id), s.count <= 0 && (s.messageElement.remove(), a.messageRegistry.delete(o)), a.messageRegistry.size == 0 && (a.containerElement?.remove(), t(this, Gu, "f", qu).delete(i)));
	}
};
Gu = Xu, Ju = function() {
	let e = document.createElement("div");
	return e.classList.add("m3e-describedby-message-container"), Wu(e.style), e;
}, Yu = function(e, r) {
	var i, a;
	let o = document.createElement("span");
	return o.id = `m3e-describedby-message-${n(this, Gu, (a = t(this, Gu, "f", Ku), i = a++, a), "f", Ku), i}`, o.role = r, o.ariaAtomic = "true", o.innerText = e.trim(), o;
}, Ku = { value: 0 }, qu = { value: /* @__PURE__ */ new Map() }, globalThis.M3eAriaDescriber = Xu;
var Zu, Qu, $u, ed = class {
	static isFocusable(e, n, r = !1, i = !1) {
		let a = `:is(button,input,select,textarea,object,:is(a,area)[href],[tabindex]${i ? "" : ":not([tabindex='-1'])"},[contenteditable=true])${i ? "" : ":not(:disabled,[disabled])"}${r ? "" : ":not([hidden])"}`;
		return e.matches(a) ? !t(this, Zu, "m", $u).call(this, e, r) && !t(this, Zu, "m", Qu).call(this, n, r) : !e.localName.includes("-") || !e.matches(":not(:disabled,[disabled])") || e.getAttribute("aria-disabled") === "true" ? !1 : e.shadowRoot?.delegatesFocus ? !t(this, Zu, "m", $u).call(this, e, r) && !t(this, Zu, "m", Qu).call(this, n, r) : !1;
	}
	static findInteractiveElements(e, t = !1) {
		let n = [], r = e.ownerDocument.createTreeWalker(e, NodeFilter.SHOW_ELEMENT);
		for (let e = r.nextNode(); e; e = r.nextNode()) {
			let e = r.currentNode;
			this.isFocusable(e, void 0, t, !0) && n.push(e);
		}
		return n;
	}
};
Zu = ed, Qu = function(e, n = !1) {
	return e?.some((e) => e.matches(`:is([aria-hidden='true'],:disabled,[disabled],[inert]${n ? "" : ",[hidden]"})`) || t(this, Zu, "m", $u).call(this, e, n)) ?? !1;
}, $u = function(e, t) {
	if (t) return !1;
	let n = getComputedStyle(e);
	return n.display === "none" || n.visibility === "hidden";
}, globalThis.M3eInteractivityChecker = ed;
var td, nd, rd, id, ad, od, sd = class extends vi(x) {
	constructor() {
		super(...arguments), td.add(this);
	}
	render() {
		let e = y`<div class="trap" .inert="${this.disabled}" tabindex="0" aria-hidden="true" @focus="${t(this, td, "m", nd)}"></div>`;
		return y`${e}<slot></slot>${e}`;
	}
};
td = /* @__PURE__ */ new WeakSet(), nd = function(e) {
	function n(e, t) {
		return e === t || e instanceof HTMLElement && e.shadowRoot !== null && e.shadowRoot.contains(t);
	}
	let [r, i] = t(this, td, "m", rd).call(this), a = e?.target === this._firstTrap, o = n(e.relatedTarget, r), s = n(e.relatedTarget, i), c = !o && !s;
	!a && s || a && c ? r?.focus() : (a && o || !a && c) && i?.focus();
}, rd = function() {
	let e = null, n = null;
	return t(this, td, "m", id).call(this, this, [], (t, r) => {
		ed.isFocusable(t, r) && t instanceof HTMLElement && t.tabIndex >= 0 && (e ??= t, n = t);
	}), [e, n];
}, id = function e(n, r, i) {
	r = [...r, n];
	for (let a of n.childNodes) {
		if (a.nodeType !== Node.ELEMENT_NODE) continue;
		let n = a;
		i(n, r), n.shadowRoot ? t(this, td, "m", ad).call(this, n, r, i) : n instanceof HTMLSlotElement ? t(this, td, "m", od).call(this, n, r, i) : n.hasChildNodes() && t(this, td, "m", e).call(this, n, r, i);
	}
}, ad = function(e, n, r) {
	for (let i of e.shadowRoot?.childNodes ?? []) {
		if (i.nodeType !== Node.ELEMENT_NODE) continue;
		let e = i;
		r(e, n), e.hasChildNodes() && t(this, td, "m", id).call(this, e, n, r);
	}
}, od = function e(n, r, i) {
	r = [...r, n];
	let a = n.assignedElements();
	a.length == 0 && n.hasChildNodes() && (a = n.childNodes);
	for (let n of a) {
		if (n.nodeType !== Node.ELEMENT_NODE) continue;
		let a = n;
		a instanceof HTMLSlotElement ? t(this, td, "m", e).call(this, a, r, i) : (i(a, r), a.hasChildNodes() && t(this, td, "m", id).call(this, a, r, i));
	}
}, sd.styles = l`:host { display: contents; } .trap { outline: none; }`, e([C(".trap")], sd.prototype, "_firstTrap", void 0), sd = e([w("m3e-focus-trap")], sd);
var cd, ld, ud, dd, fd, pd, md, hd = class {
	static announce(e, ...r) {
		n(this, cd, t(this, cd, "f", ud) ?? t(this, cd, "m", md).call(this), "f", ud);
		let i, a;
		r.length === 1 && typeof r[0] == "number" ? a = r[0] : [i, a] = r, this.clear(), clearTimeout(t(this, cd, "f", dd)), t(this, cd, "f", ud).setAttribute("aria-live", i ?? "polite");
		for (let e of document.querySelectorAll("m3e-dialog")) Hu(e, "aria-owns", t(this, cd, "f", ud).id);
		return n(this, cd, t(this, cd, "f", fd) ?? new Promise((e) => n(this, cd, e, "f", pd)), "f", fd), clearTimeout(t(this, cd, "f", dd)), n(this, cd, setTimeout(() => {
			t(this, cd, "f", ud) && (t(this, cd, "f", ud).textContent = e, a !== void 0 && n(this, cd, setTimeout(() => this.clear(), a), "f", dd), t(this, cd, "f", pd)?.call(this), n(this, cd, n(this, cd, void 0, "f", pd), "f", fd));
		}, 100), "f", dd), t(this, cd, "f", fd);
	}
	static clear() {
		t(this, cd, "f", ud) && (t(this, cd, "f", ud).textContent = "");
	}
};
cd = hd, md = function() {
	var e, r;
	let i = document.getElementsByClassName("m3e-live-announcer");
	for (let e = 0; e < i.length; e++) i[e].remove();
	let a = document.createElement("div");
	return a.classList.add("m3e-live-announcer"), a.setAttribute("aria-atomic", "true"), a.setAttribute("aria-live", "polite"), a.id = `m3e-live-announcer-${n(this, cd, (r = t(this, cd, "f", ld), e = r++, r), "f", ld), e}`, Wu(a.style), document.body.append(a), a;
}, ld = { value: 0 }, ud = { value: void 0 }, dd = { value: void 0 }, fd = { value: void 0 }, pd = { value: void 0 }, globalThis.M3eLiveAnnouncer = hd;
//#endregion
//#region node_modules/@m3e/web/dist/core-bidi.js
var gd, _d, vd, yd, bd, xd = /^(ar|ckb|dv|he|iw|fa|nqo|ps|sd|ug|ur|yi|.*[-_](Adlm|Arab|Hebr|Nkoo|Rohg|Thaa))(?!.*[-_](Latn|Cyrl)($|-|_))($|-|_)/i, Sd = class {
	static get current() {
		return t(this, gd, "f", _d);
	}
	static observe(e) {
		return t(this, gd, "f", vd).push(e), () => {
			t(this, gd, "f", vd).splice(t(this, gd, "f", vd).indexOf(e), 1);
		};
	}
};
gd = Sd, yd = function() {
	let e = (document.body?.dir || document.documentElement?.dir)?.toLowerCase() || "auto";
	n(this, gd, e === "auto" && navigator?.language ? xd.test(navigator.language) ? "rtl" : "ltr" : e === "rtl" ? "rtl" : "ltr", "f", _d);
}, bd = function() {
	t(this, gd, "m", yd).call(this), t(this, gd, "f", vd).forEach((e) => e());
}, _d = { value: "ltr" }, vd = { value: [] }, (() => {
	if (typeof window < "u") {
		if (t(gd, gd, "m", yd).call(gd), MutationObserver) {
			let e = { attributeFilter: ["dir"] }, n = new MutationObserver(() => t(gd, gd, "m", bd).call(gd));
			document.body && n.observe(document.body, e), document.documentElement && n.observe(document.documentElement, e);
		}
		window.addEventListener("languagechange", () => t(gd, gd, "m", bd).call(gd));
	}
})(), globalThis.M3eDirectionality = Sd;
function Cd(e) {
	var r;
	class i extends e {
		constructor() {
			super(...arguments), r.set(this, void 0);
		}
		connectedCallback() {
			super.connectedCallback(), t(this, r, "f")?.call(this), n(this, r, M3eDirectionality.observe(() => D(this, "--rtl", M3eDirectionality.current === "rtl")), "f"), D(this, "--rtl", M3eDirectionality.current === "rtl");
		}
		disconnectedCallback() {
			super.disconnectedCallback(), t(this, r, "f")?.call(this), E(this, "--rtl");
		}
	}
	return r = /* @__PURE__ */ new WeakMap(), i;
}
//#endregion
//#region node_modules/@m3e/web/dist/core-anchoring.js
var wd = Math.min, Td = Math.max, Ed = Math.round, Dd = Math.floor, Od = (e) => ({
	x: e,
	y: e
}), kd = {
	left: "right",
	right: "left",
	bottom: "top",
	top: "bottom"
};
function Ad(e, t, n) {
	return Td(e, wd(t, n));
}
function jd(e, t) {
	return typeof e == "function" ? e(t) : e;
}
function Md(e) {
	return e.split("-")[0];
}
function Nd(e) {
	return e.split("-")[1];
}
function Pd(e) {
	return e === "x" ? "y" : "x";
}
function Fd(e) {
	return e === "y" ? "height" : "width";
}
function Id(e) {
	let t = e[0];
	return t === "t" || t === "b" ? "y" : "x";
}
function Ld(e) {
	return Pd(Id(e));
}
function Rd(e, t, n) {
	n === void 0 && (n = !1);
	let r = Nd(e), i = Ld(e), a = Fd(i), o = i === "x" ? r === (n ? "end" : "start") ? "right" : "left" : r === "start" ? "bottom" : "top";
	return t.reference[a] > t.floating[a] && (o = qd(o)), [o, qd(o)];
}
function zd(e) {
	let t = qd(e);
	return [
		Bd(e),
		t,
		Bd(t)
	];
}
function Bd(e) {
	return e.includes("start") ? e.replace("start", "end") : e.replace("end", "start");
}
var Vd = ["left", "right"], Hd = ["right", "left"], Ud = ["top", "bottom"], Wd = ["bottom", "top"];
function Gd(e, t, n) {
	switch (e) {
		case "top":
		case "bottom": return n ? t ? Hd : Vd : t ? Vd : Hd;
		case "left":
		case "right": return t ? Ud : Wd;
		default: return [];
	}
}
function Kd(e, t, n, r) {
	let i = Nd(e), a = Gd(Md(e), n === "start", r);
	return i && (a = a.map((e) => e + "-" + i), t && (a = a.concat(a.map(Bd)))), a;
}
function qd(e) {
	let t = Md(e);
	return kd[t] + e.slice(t.length);
}
function Jd(e) {
	return {
		top: 0,
		right: 0,
		bottom: 0,
		left: 0,
		...e
	};
}
function Yd(e) {
	return typeof e == "number" ? {
		top: e,
		right: e,
		bottom: e,
		left: e
	} : Jd(e);
}
function Xd(e) {
	let { x: t, y: n, width: r, height: i } = e;
	return {
		width: r,
		height: i,
		top: n,
		left: t,
		right: t + r,
		bottom: n + i,
		x: t,
		y: n
	};
}
function Zd(e, t, n) {
	let { reference: r, floating: i } = e, a = Id(t), o = Ld(t), s = Fd(o), c = Md(t), l = a === "y", u = r.x + r.width / 2 - i.width / 2, d = r.y + r.height / 2 - i.height / 2, f = r[s] / 2 - i[s] / 2, p;
	switch (c) {
		case "top":
			p = {
				x: u,
				y: r.y - i.height
			};
			break;
		case "bottom":
			p = {
				x: u,
				y: r.y + r.height
			};
			break;
		case "right":
			p = {
				x: r.x + r.width,
				y: d
			};
			break;
		case "left":
			p = {
				x: r.x - i.width,
				y: d
			};
			break;
		default: p = {
			x: r.x,
			y: r.y
		};
	}
	switch (Nd(t)) {
		case "start":
			p[o] -= f * (n && l ? -1 : 1);
			break;
		case "end": p[o] += f * (n && l ? -1 : 1);
	}
	return p;
}
async function Qd(e, t) {
	t === void 0 && (t = {});
	let { x: n, y: r, platform: i, rects: a, elements: o, strategy: s } = e, { boundary: c = "clippingAncestors", rootBoundary: l = "viewport", elementContext: u = "floating", altBoundary: d = !1, padding: f = 0 } = jd(t, e), p = Yd(f), m = o[d ? u === "floating" ? "reference" : "floating" : u], h = Xd(await i.getClippingRect({
		element: await (i.isElement == null ? void 0 : i.isElement(m)) ?? !0 ? m : m.contextElement || await (i.getDocumentElement == null ? void 0 : i.getDocumentElement(o.floating)),
		boundary: c,
		rootBoundary: l,
		strategy: s
	})), ee = u === "floating" ? {
		x: n,
		y: r,
		width: a.floating.width,
		height: a.floating.height
	} : a.reference, te = await (i.getOffsetParent == null ? void 0 : i.getOffsetParent(o.floating)), ne = await (i.isElement == null ? void 0 : i.isElement(te)) && await (i.getScale == null ? void 0 : i.getScale(te)) || {
		x: 1,
		y: 1
	}, g = Xd(i.convertOffsetParentRelativeRectToViewportRelativeRect ? await i.convertOffsetParentRelativeRectToViewportRelativeRect({
		elements: o,
		rect: ee,
		offsetParent: te,
		strategy: s
	}) : ee);
	return {
		top: (h.top - g.top + p.top) / ne.y,
		bottom: (g.bottom - h.bottom + p.bottom) / ne.y,
		left: (h.left - g.left + p.left) / ne.x,
		right: (g.right - h.right + p.right) / ne.x
	};
}
var $d = 50, ef = async (e, t, n) => {
	let { placement: r = "bottom", strategy: i = "absolute", middleware: a = [], platform: o } = n, s = o.detectOverflow ? o : {
		...o,
		detectOverflow: Qd
	}, c = await (o.isRTL == null ? void 0 : o.isRTL(t)), l = await o.getElementRects({
		reference: e,
		floating: t,
		strategy: i
	}), { x: u, y: d } = Zd(l, r, c), f = r, p = 0, m = {};
	for (let n = 0; n < a.length; n++) {
		let h = a[n];
		if (!h) continue;
		let { name: ee, fn: te } = h, { x: ne, y: g, data: re, reset: _ } = await te({
			x: u,
			y: d,
			initialPlacement: r,
			placement: f,
			strategy: i,
			middlewareData: m,
			rects: l,
			platform: s,
			elements: {
				reference: e,
				floating: t
			}
		});
		u = ne ?? u, d = g ?? d, m[ee] = {
			...m[ee],
			...re
		}, _ && p < $d && (p++, typeof _ == "object" && (_.placement && (f = _.placement), _.rects && (l = _.rects === !0 ? await o.getElementRects({
			reference: e,
			floating: t,
			strategy: i
		}) : _.rects), {x: u, y: d} = Zd(l, f, c)), n = -1);
	}
	return {
		x: u,
		y: d,
		placement: f,
		strategy: i,
		middlewareData: m
	};
}, tf = function(e) {
	return e === void 0 && (e = {}), {
		name: "flip",
		options: e,
		async fn(t) {
			var n;
			let { placement: r, middlewareData: i, rects: a, initialPlacement: o, platform: s, elements: c } = t, { mainAxis: l = !0, crossAxis: u = !0, fallbackPlacements: d, fallbackStrategy: f = "bestFit", fallbackAxisSideDirection: p = "none", flipAlignment: m = !0, ...h } = jd(e, t);
			if ((n = i.arrow) != null && n.alignmentOffset) return {};
			let ee = Md(r), te = Id(o), ne = Md(o) === o, g = await (s.isRTL == null ? void 0 : s.isRTL(c.floating)), re = d || (ne || !m ? [qd(o)] : zd(o)), _ = p !== "none";
			!d && _ && re.push(...Kd(o, m, p, g));
			let ie = [o, ...re], v = await s.detectOverflow(t, h), ae = [], oe = i.flip?.overflows || [];
			if (l && ae.push(v[ee]), u) {
				let e = Rd(r, a, g);
				ae.push(v[e[0]], v[e[1]]);
			}
			if (oe = [...oe, {
				placement: r,
				overflows: ae
			}], !ae.every((e) => e <= 0)) {
				let e = (i.flip?.index || 0) + 1, t = ie[e];
				if (t && (u !== "alignment" || te === Id(t) || oe.every((e) => Id(e.placement) !== te || e.overflows[0] > 0))) return {
					data: {
						index: e,
						overflows: oe
					},
					reset: { placement: t }
				};
				let n = oe.filter((e) => e.overflows[0] <= 0).sort((e, t) => e.overflows[1] - t.overflows[1])[0]?.placement;
				if (!n) switch (f) {
					case "bestFit": {
						let e = oe.filter((e) => {
							if (_) {
								let t = Id(e.placement);
								return t === te || t === "y";
							}
							return !0;
						}).map((e) => [e.placement, e.overflows.filter((e) => e > 0).reduce((e, t) => e + t, 0)]).sort((e, t) => e[1] - t[1])[0]?.[0];
						e && (n = e);
						break;
					}
					case "initialPlacement": n = o;
				}
				if (r !== n) return { reset: { placement: n } };
			}
			return {};
		}
	};
};
function nf(e) {
	let t = wd(...e.map((e) => e.left)), n = wd(...e.map((e) => e.top)), r = Td(...e.map((e) => e.right)), i = Td(...e.map((e) => e.bottom));
	return {
		x: t,
		y: n,
		width: r - t,
		height: i - n
	};
}
function rf(e) {
	let t = e.slice().sort((e, t) => e.y - t.y), n = [], r = null;
	for (let e = 0; e < t.length; e++) {
		let i = t[e];
		!r || i.y - r.y > r.height / 2 ? n.push([i]) : n[n.length - 1].push(i), r = i;
	}
	return n.map((e) => Xd(nf(e)));
}
var af = function(e) {
	return e === void 0 && (e = {}), {
		name: "inline",
		options: e,
		async fn(t) {
			let { placement: n, elements: r, rects: i, platform: a, strategy: o } = t, { padding: s = 2, x: c, y: l } = jd(e, t), u = Array.from(await (a.getClientRects == null ? void 0 : a.getClientRects(r.reference)) || []), d = rf(u), f = Xd(nf(u)), p = Yd(s);
			function m() {
				if (d.length === 2 && d[0].left > d[1].right && c != null && l != null) return d.find((e) => c > e.left - p.left && c < e.right + p.right && l > e.top - p.top && l < e.bottom + p.bottom) || f;
				if (d.length >= 2) {
					if (Id(n) === "y") {
						let e = d[0], t = d[d.length - 1], r = Md(n) === "top", i = e.top, a = t.bottom, o = r ? e.left : t.left, s = r ? e.right : t.right;
						return {
							top: i,
							bottom: a,
							left: o,
							right: s,
							width: s - o,
							height: a - i,
							x: o,
							y: i
						};
					}
					let e = Md(n) === "left", t = Td(...d.map((e) => e.right)), r = wd(...d.map((e) => e.left)), i = d.filter((n) => e ? n.left === r : n.right === t), a = i[0].top, o = i[i.length - 1].bottom, s = r, c = t;
					return {
						top: a,
						bottom: o,
						left: s,
						right: c,
						width: c - s,
						height: o - a,
						x: s,
						y: a
					};
				}
				return f;
			}
			let h = await a.getElementRects({
				reference: { getBoundingClientRect: m },
				floating: r.floating,
				strategy: o
			});
			return i.reference.x !== h.reference.x || i.reference.y !== h.reference.y || i.reference.width !== h.reference.width || i.reference.height !== h.reference.height ? { reset: { rects: h } } : {};
		}
	};
}, of = /*#__PURE__*/ new Set(["left", "top"]);
async function sf(e, t) {
	let { placement: n, platform: r, elements: i } = e, a = await (r.isRTL == null ? void 0 : r.isRTL(i.floating)), o = Md(n), s = Nd(n), c = Id(n) === "y", l = of.has(o) ? -1 : 1, u = a && c ? -1 : 1, d = jd(t, e), { mainAxis: f, crossAxis: p, alignmentAxis: m } = typeof d == "number" ? {
		mainAxis: d,
		crossAxis: 0,
		alignmentAxis: null
	} : {
		mainAxis: d.mainAxis || 0,
		crossAxis: d.crossAxis || 0,
		alignmentAxis: d.alignmentAxis
	};
	return s && typeof m == "number" && (p = s === "end" ? m * -1 : m), c ? {
		x: p * u,
		y: f * l
	} : {
		x: f * l,
		y: p * u
	};
}
var cf = function(e) {
	return e === void 0 && (e = 0), {
		name: "offset",
		options: e,
		async fn(t) {
			var n;
			let { x: r, y: i, placement: a, middlewareData: o } = t, s = await sf(t, e);
			return a === o.offset?.placement && (n = o.arrow) != null && n.alignmentOffset ? {} : {
				x: r + s.x,
				y: i + s.y,
				data: {
					...s,
					placement: a
				}
			};
		}
	};
}, lf = function(e) {
	return e === void 0 && (e = {}), {
		name: "shift",
		options: e,
		async fn(t) {
			let { x: n, y: r, placement: i, platform: a } = t, { mainAxis: o = !0, crossAxis: s = !1, limiter: c = { fn: (e) => {
				let { x: t, y: n } = e;
				return {
					x: t,
					y: n
				};
			} }, ...l } = jd(e, t), u = {
				x: n,
				y: r
			}, d = await a.detectOverflow(t, l), f = Id(Md(i)), p = Pd(f), m = u[p], h = u[f];
			if (o) {
				let e = p === "y" ? "top" : "left", t = p === "y" ? "bottom" : "right", n = m + d[e], r = m - d[t];
				m = Ad(n, m, r);
			}
			if (s) {
				let e = f === "y" ? "top" : "left", t = f === "y" ? "bottom" : "right", n = h + d[e], r = h - d[t];
				h = Ad(n, h, r);
			}
			let ee = c.fn({
				...t,
				[p]: m,
				[f]: h
			});
			return {
				...ee,
				data: {
					x: ee.x - n,
					y: ee.y - r,
					enabled: {
						[p]: o,
						[f]: s
					}
				}
			};
		}
	};
}, uf = function(e) {
	return e === void 0 && (e = {}), {
		options: e,
		fn(t) {
			let { x: n, y: r, placement: i, rects: a, middlewareData: o } = t, { offset: s = 0, mainAxis: c = !0, crossAxis: l = !0 } = jd(e, t), u = {
				x: n,
				y: r
			}, d = Id(i), f = Pd(d), p = u[f], m = u[d], h = jd(s, t), ee = typeof h == "number" ? {
				mainAxis: h,
				crossAxis: 0
			} : {
				mainAxis: 0,
				crossAxis: 0,
				...h
			};
			if (c) {
				let e = f === "y" ? "height" : "width", t = a.reference[f] - a.floating[e] + ee.mainAxis, n = a.reference[f] + a.reference[e] - ee.mainAxis;
				p < t ? p = t : p > n && (p = n);
			}
			if (l) {
				let e = f === "y" ? "width" : "height", t = of.has(Md(i)), n = a.reference[d] - a.floating[e] + (t && o.offset?.[d] || 0) + (t ? 0 : ee.crossAxis), r = a.reference[d] + a.reference[e] + (t ? 0 : o.offset?.[d] || 0) - (t ? ee.crossAxis : 0);
				m < n ? m = n : m > r && (m = r);
			}
			return {
				[f]: p,
				[d]: m
			};
		}
	};
};
function df() {
	return typeof window < "u";
}
function ff(e) {
	return hf(e) ? (e.nodeName || "").toLowerCase() : "#document";
}
function pf(e) {
	var t;
	return (e == null || (t = e.ownerDocument) == null ? void 0 : t.defaultView) || window;
}
function mf(e) {
	return ((hf(e) ? e.ownerDocument : e.document) || window.document)?.documentElement;
}
function hf(e) {
	return df() ? e instanceof Node || e instanceof pf(e).Node : !1;
}
function gf(e) {
	return df() ? e instanceof Element || e instanceof pf(e).Element : !1;
}
function _f(e) {
	return df() ? e instanceof HTMLElement || e instanceof pf(e).HTMLElement : !1;
}
function vf(e) {
	return !df() || typeof ShadowRoot > "u" ? !1 : e instanceof ShadowRoot || e instanceof pf(e).ShadowRoot;
}
function yf(e) {
	let { overflow: t, overflowX: n, overflowY: r, display: i } = Af(e);
	return /auto|scroll|overlay|hidden|clip/.test(t + r + n) && i !== "inline" && i !== "contents";
}
function bf(e) {
	return /^(table|td|th)$/.test(ff(e));
}
function xf(e) {
	try {
		if (e.matches(":popover-open")) return !0;
	} catch {}
	try {
		return e.matches(":modal");
	} catch {
		return !1;
	}
}
var Sf = /transform|translate|scale|rotate|perspective|filter/, Cf = /paint|layout|strict|content/, wf = (e) => !!e && e !== "none", Tf;
function Ef(e) {
	let t = gf(e) ? Af(e) : e;
	return wf(t.transform) || wf(t.translate) || wf(t.scale) || wf(t.rotate) || wf(t.perspective) || !Of() && (wf(t.backdropFilter) || wf(t.filter)) || Sf.test(t.willChange || "") || Cf.test(t.contain || "");
}
function Df(e) {
	let t = Mf(e);
	for (; _f(t) && !kf(t);) {
		if (Ef(t)) return t;
		if (xf(t)) return null;
		t = Mf(t);
	}
	return null;
}
function Of() {
	return Tf ??= typeof CSS < "u" && CSS.supports && CSS.supports("-webkit-backdrop-filter", "none"), Tf;
}
function kf(e) {
	return /^(html|body|#document)$/.test(ff(e));
}
function Af(e) {
	return pf(e).getComputedStyle(e);
}
function jf(e) {
	return gf(e) ? {
		scrollLeft: e.scrollLeft,
		scrollTop: e.scrollTop
	} : {
		scrollLeft: e.scrollX,
		scrollTop: e.scrollY
	};
}
function Mf(e) {
	if (ff(e) === "html") return e;
	let t = e.assignedSlot || e.parentNode || vf(e) && e.host || mf(e);
	return vf(t) ? t.host : t;
}
function Nf(e) {
	let t = Mf(e);
	return kf(t) ? e.ownerDocument ? e.ownerDocument.body : e.body : _f(t) && yf(t) ? t : Nf(t);
}
function Pf(e, t, n) {
	t === void 0 && (t = []), n === void 0 && (n = !0);
	let r = Nf(e), i = r === e.ownerDocument?.body, a = pf(r);
	if (i) {
		let e = Ff(a);
		return t.concat(a, a.visualViewport || [], yf(r) ? r : [], e && n ? Pf(e) : []);
	}
	return t.concat(r, Pf(r, [], n));
}
function Ff(e) {
	return e.parent && Object.getPrototypeOf(e.parent) ? e.frameElement : null;
}
function If(e) {
	let t = Af(e), n = parseFloat(t.width) || 0, r = parseFloat(t.height) || 0, i = _f(e), a = i ? e.offsetWidth : n, o = i ? e.offsetHeight : r, s = Ed(n) !== a || Ed(r) !== o;
	return s && (n = a, r = o), {
		width: n,
		height: r,
		$: s
	};
}
function Lf(e) {
	return gf(e) ? e : e.contextElement;
}
function Rf(e) {
	let t = Lf(e);
	if (!_f(t)) return Od(1);
	let n = t.getBoundingClientRect(), { width: r, height: i, $: a } = If(t), o = (a ? Ed(n.width) : n.width) / r, s = (a ? Ed(n.height) : n.height) / i;
	return (!o || !Number.isFinite(o)) && (o = 1), (!s || !Number.isFinite(s)) && (s = 1), {
		x: o,
		y: s
	};
}
var zf = /*#__PURE__*/ Od(0);
function Bf(e) {
	let t = pf(e);
	return !Of() || !t.visualViewport ? zf : {
		x: t.visualViewport.offsetLeft,
		y: t.visualViewport.offsetTop
	};
}
function Vf(e, t, n) {
	return t === void 0 && (t = !1), !n || t && n !== pf(e) ? !1 : t;
}
function Hf(e, t, n, r) {
	t === void 0 && (t = !1), n === void 0 && (n = !1);
	let i = e.getBoundingClientRect(), a = Lf(e), o = Od(1);
	t && (r ? gf(r) && (o = Rf(r)) : o = Rf(e));
	let s = Vf(a, n, r) ? Bf(a) : Od(0), c = (i.left + s.x) / o.x, l = (i.top + s.y) / o.y, u = i.width / o.x, d = i.height / o.y;
	if (a) {
		let e = pf(a), t = r && gf(r) ? pf(r) : r, n = e, i = Ff(n);
		for (; i && r && t !== n;) {
			let e = Rf(i), t = i.getBoundingClientRect(), r = Af(i), a = t.left + (i.clientLeft + parseFloat(r.paddingLeft)) * e.x, o = t.top + (i.clientTop + parseFloat(r.paddingTop)) * e.y;
			c *= e.x, l *= e.y, u *= e.x, d *= e.y, c += a, l += o, n = pf(i), i = Ff(n);
		}
	}
	return Xd({
		width: u,
		height: d,
		x: c,
		y: l
	});
}
function Uf(e, t) {
	let n = jf(e).scrollLeft;
	return t ? t.left + n : Hf(mf(e)).left + n;
}
function Wf(e, t) {
	let n = e.getBoundingClientRect();
	return {
		x: n.left + t.scrollLeft - Uf(e, n),
		y: n.top + t.scrollTop
	};
}
function Gf(e) {
	let { elements: t, rect: n, offsetParent: r, strategy: i } = e, a = i === "fixed", o = mf(r), s = t ? xf(t.floating) : !1;
	if (r === o || s && a) return n;
	let c = {
		scrollLeft: 0,
		scrollTop: 0
	}, l = Od(1), u = Od(0), d = _f(r);
	if ((d || !d && !a) && ((ff(r) !== "body" || yf(o)) && (c = jf(r)), d)) {
		let e = Hf(r);
		l = Rf(r), u.x = e.x + r.clientLeft, u.y = e.y + r.clientTop;
	}
	let f = o && !d && !a ? Wf(o, c) : Od(0);
	return {
		width: n.width * l.x,
		height: n.height * l.y,
		x: n.x * l.x - c.scrollLeft * l.x + u.x + f.x,
		y: n.y * l.y - c.scrollTop * l.y + u.y + f.y
	};
}
function Kf(e) {
	return Array.from(e.getClientRects());
}
function qf(e) {
	let t = mf(e), n = jf(e), r = e.ownerDocument.body, i = Td(t.scrollWidth, t.clientWidth, r.scrollWidth, r.clientWidth), a = Td(t.scrollHeight, t.clientHeight, r.scrollHeight, r.clientHeight), o = -n.scrollLeft + Uf(e), s = -n.scrollTop;
	return Af(r).direction === "rtl" && (o += Td(t.clientWidth, r.clientWidth) - i), {
		width: i,
		height: a,
		x: o,
		y: s
	};
}
var Jf = 25;
function Yf(e, t) {
	let n = pf(e), r = mf(e), i = n.visualViewport, a = r.clientWidth, o = r.clientHeight, s = 0, c = 0;
	if (i) {
		a = i.width, o = i.height;
		let e = Of();
		(!e || e && t === "fixed") && (s = i.offsetLeft, c = i.offsetTop);
	}
	let l = Uf(r);
	if (l <= 0) {
		let e = r.ownerDocument, t = e.body, n = getComputedStyle(t), i = e.compatMode === "CSS1Compat" && parseFloat(n.marginLeft) + parseFloat(n.marginRight) || 0, o = Math.abs(r.clientWidth - t.clientWidth - i);
		o <= Jf && (a -= o);
	} else l <= Jf && (a += l);
	return {
		width: a,
		height: o,
		x: s,
		y: c
	};
}
function Xf(e, t) {
	let n = Hf(e, !0, t === "fixed"), r = n.top + e.clientTop, i = n.left + e.clientLeft, a = _f(e) ? Rf(e) : Od(1);
	return {
		width: e.clientWidth * a.x,
		height: e.clientHeight * a.y,
		x: i * a.x,
		y: r * a.y
	};
}
function Zf(e, t, n) {
	let r;
	if (t === "viewport") r = Yf(e, n);
	else if (t === "document") r = qf(mf(e));
	else if (gf(t)) r = Xf(t, n);
	else {
		let n = Bf(e);
		r = {
			x: t.x - n.x,
			y: t.y - n.y,
			width: t.width,
			height: t.height
		};
	}
	return Xd(r);
}
function Qf(e, t) {
	let n = Mf(e);
	return n === t || !gf(n) || kf(n) ? !1 : Af(n).position === "fixed" || Qf(n, t);
}
function $f(e, t) {
	let n = t.get(e);
	if (n) return n;
	let r = Pf(e, [], !1).filter((e) => gf(e) && ff(e) !== "body"), i = null, a = Af(e).position === "fixed", o = a ? Mf(e) : e;
	for (; gf(o) && !kf(o);) {
		let t = Af(o), n = Ef(o);
		!n && t.position === "fixed" && (i = null), (a ? !n && !i : !n && t.position === "static" && i && (i.position === "absolute" || i.position === "fixed") || yf(o) && !n && Qf(e, o)) ? r = r.filter((e) => e !== o) : i = t, o = Mf(o);
	}
	return t.set(e, r), r;
}
function ep(e) {
	let { element: t, boundary: n, rootBoundary: r, strategy: i } = e, a = [...n === "clippingAncestors" ? xf(t) ? [] : $f(t, this._c) : [].concat(n), r], o = Zf(t, a[0], i), s = o.top, c = o.right, l = o.bottom, u = o.left;
	for (let e = 1; e < a.length; e++) {
		let n = Zf(t, a[e], i);
		s = Td(n.top, s), c = wd(n.right, c), l = wd(n.bottom, l), u = Td(n.left, u);
	}
	return {
		width: c - u,
		height: l - s,
		x: u,
		y: s
	};
}
function tp(e) {
	let { width: t, height: n } = If(e);
	return {
		width: t,
		height: n
	};
}
function np(e, t, n) {
	let r = _f(t), i = mf(t), a = n === "fixed", o = Hf(e, !0, a, t), s = {
		scrollLeft: 0,
		scrollTop: 0
	}, c = Od(0);
	function l() {
		c.x = Uf(i);
	}
	if (r || !r && !a) {
		if ((ff(t) !== "body" || yf(i)) && (s = jf(t)), r) {
			let e = Hf(t, !0, a, t);
			c.x = e.x + t.clientLeft, c.y = e.y + t.clientTop;
		} else i && l();
	}
	a && !r && i && l();
	let u = i && !r && !a ? Wf(i, s) : Od(0);
	return {
		x: o.left + s.scrollLeft - c.x - u.x,
		y: o.top + s.scrollTop - c.y - u.y,
		width: o.width,
		height: o.height
	};
}
function rp(e) {
	return Af(e).position === "static";
}
function ip(e, t) {
	if (!_f(e) || Af(e).position === "fixed") return null;
	if (t) return t(e);
	let n = e.offsetParent;
	return mf(e) === n && (n = n.ownerDocument.body), n;
}
function ap(e, t) {
	let n = pf(e);
	if (xf(e)) return n;
	if (!_f(e)) {
		let t = Mf(e);
		for (; t && !kf(t);) {
			if (gf(t) && !rp(t)) return t;
			t = Mf(t);
		}
		return n;
	}
	let r = ip(e, t);
	for (; r && bf(r) && rp(r);) r = ip(r, t);
	return r && kf(r) && rp(r) && !Ef(r) ? n : r || Df(e) || n;
}
var op = async function(e) {
	let t = this.getOffsetParent || ap, n = this.getDimensions, r = await n(e.floating);
	return {
		reference: np(e.reference, await t(e.floating), e.strategy),
		floating: {
			x: 0,
			y: 0,
			width: r.width,
			height: r.height
		}
	};
};
function sp(e) {
	return Af(e).direction === "rtl";
}
var cp = {
	convertOffsetParentRelativeRectToViewportRelativeRect: Gf,
	getDocumentElement: mf,
	getClippingRect: ep,
	getOffsetParent: ap,
	getElementRects: op,
	getClientRects: Kf,
	getDimensions: tp,
	getScale: Rf,
	isElement: gf,
	isRTL: sp
};
function lp(e, t) {
	return e.x === t.x && e.y === t.y && e.width === t.width && e.height === t.height;
}
function up(e, t) {
	let n = null, r, i = mf(e);
	function a() {
		var e;
		clearTimeout(r), (e = n) == null || e.disconnect(), n = null;
	}
	function o(s, c) {
		s === void 0 && (s = !1), c === void 0 && (c = 1), a();
		let l = e.getBoundingClientRect(), { left: u, top: d, width: f, height: p } = l;
		if (s || t(), !f || !p) return;
		let m = Dd(d), h = Dd(i.clientWidth - (u + f)), ee = Dd(i.clientHeight - (d + p)), te = Dd(u), ne = {
			rootMargin: -m + "px " + -h + "px " + -ee + "px " + -te + "px",
			threshold: Td(0, wd(1, c)) || 1
		}, g = !0;
		function re(t) {
			let n = t[0].intersectionRatio;
			if (n !== c) {
				if (!g) return o();
				n ? o(!1, n) : r = setTimeout(() => {
					o(!1, 1e-7);
				}, 1e3);
			}
			n === 1 && !lp(l, e.getBoundingClientRect()) && o(), g = !1;
		}
		try {
			n = new IntersectionObserver(re, {
				...ne,
				root: i.ownerDocument
			});
		} catch {
			n = new IntersectionObserver(re, ne);
		}
		n.observe(e);
	}
	return o(!0), a;
}
function dp(e, t, n, r) {
	r === void 0 && (r = {});
	let { ancestorScroll: i = !0, ancestorResize: a = !0, elementResize: o = typeof ResizeObserver == "function", layoutShift: s = typeof IntersectionObserver == "function", animationFrame: c = !1 } = r, l = Lf(e), u = i || a ? [...l ? Pf(l) : [], ...t ? Pf(t) : []] : [];
	u.forEach((e) => {
		i && e.addEventListener("scroll", n, { passive: !0 }), a && e.addEventListener("resize", n);
	});
	let d = l && s ? up(l, n) : null, f = -1, p = null;
	o && (p = new ResizeObserver((e) => {
		let [r] = e;
		r && r.target === l && p && t && (p.unobserve(t), cancelAnimationFrame(f), f = requestAnimationFrame(() => {
			var e;
			(e = p) == null || e.observe(t);
		})), n();
	}), l && !c && p.observe(l), t && p.observe(t));
	let m, h = c ? Hf(e) : null;
	c && ee();
	function ee() {
		let t = Hf(e);
		h && !lp(h, t) && n(), h = t, m = requestAnimationFrame(ee);
	}
	return n(), () => {
		var e;
		u.forEach((e) => {
			i && e.removeEventListener("scroll", n), a && e.removeEventListener("resize", n);
		}), d?.(), (e = p) == null || e.disconnect(), p = null, c && cancelAnimationFrame(m);
	};
}
var fp = cf, pp = lf, mp = tf, hp = af, gp = uf, _p = (e, t, n) => {
	let r = /* @__PURE__ */ new Map(), i = {
		platform: cp,
		...n
	}, a = {
		...i.platform,
		_c: r
	};
	return ef(e, t, {
		...i,
		platform: a
	});
};
function vp(e) {
	return bp(e);
}
function yp(e) {
	return e.assignedSlot ? e.assignedSlot : e.parentNode instanceof ShadowRoot ? e.parentNode.host : e.parentNode;
}
function bp(e) {
	for (let t = e; t; t = yp(t)) if (t instanceof Element && getComputedStyle(t).display === "none") return null;
	for (let t = yp(e); t; t = yp(t)) {
		if (!(t instanceof Element)) continue;
		let e = getComputedStyle(t);
		if (e.display !== "contents" && (e.position !== "static" || Ef(e) || t.tagName === "BODY")) return t;
	}
	return null;
}
async function xp(e, t, n, r) {
	let i = {
		x: -(2 ** 53 - 1),
		y: -(2 ** 53 - 1),
		placement: "bottom"
	};
	async function a() {
		let a = [];
		n?.inline && a.push(hp()), n.flip && a.push(n.flip === !0 ? mp() : mp({ fallbackPlacements: n.flip })), n.shift && a.push(pp({
			mainAxis: n.shift === "main" || n.shift === "both",
			crossAxis: n.shift === "cross" || n.shift === "both",
			limiter: gp()
		})), n.offset && !isNaN(n.offset) && a.push(fp(n.offset));
		let { x: o, y: s, placement: c } = await _p(t, e, {
			placement: n.position,
			middleware: a,
			platform: {
				...cp,
				getOffsetParent: (e) => cp.getOffsetParent(e, vp)
			}
		});
		(i.x !== o || i.y !== s || i.placement !== c) && r(o, s, c), Object.assign(i, {
			x: o,
			y: s,
			placement: c
		});
	}
	return await a(), dp(t, e, async () => await a());
}
var Sp, Cp, wp, Tp, Ep, Dp, Op, kp, Ap = class extends ia(T(x)) {
	constructor() {
		super(...arguments), Sp.set(this, void 0), Cp.set(this, void 0), wp.set(this, void 0), Tp.set(this, void 0), Ep.set(this, new yt(this, {
			target: null,
			callback: () => this.hide()
		})), Dp.set(this, (e) => {
			switch (e.newState) {
				case "open":
					t(this, Ep, "f").observe(this), t(this, Cp, "f") && t(this, Ep, "f").observe(t(this, Cp, "f"));
					break;
				case "closed": t(this, Ep, "f").unobserveAll(), t(this, wp, "f")?.call(this), n(this, wp, void 0, "f"), n(this, Tp, void 0, "f");
			}
		}), Op.set(this, new yr(this, {
			target: null,
			callback: () => this.hide(!1)
		})), kp.set(this, new cr(this, {
			target: null,
			callback: () => {
				t(this, Cp, "f") && this.fitAnchorWidth && (this.style.minWidth = `${t(this, Cp, "f").clientWidth}px`);
			}
		})), this.scrollStrategy = "hide", this.fitAnchorWidth = !1, this.anchorOffset = 0;
	}
	get isOpen() {
		return t(this, Sp, "f") !== void 0;
	}
	get trigger() {
		return t(this, Sp, "f") ?? null;
	}
	connectedCallback() {
		super.connectedCallback(), this.setAttribute("popover", "manual"), this.addEventListener("toggle", t(this, Dp, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("toggle", t(this, Dp, "f"));
	}
	async show(e, r) {
		t(this, Sp, "f") && t(this, Sp, "f") !== e && this.hide(), n(this, Sp, e, "f"), t(this, Sp, "f").ariaExpanded = "true", n(this, Cp, r ?? e, "f"), this.scrollStrategy === "hide" && t(this, Op, "f").observe(t(this, Cp, "f")), this.fitAnchorWidth && (t(this, kp, "f").observe(t(this, Cp, "f")), this.style.minWidth = `${t(this, Cp, "f").clientWidth}px`), n(this, wp, await xp(this, t(this, Cp, "f"), {
			position: "bottom-start",
			inline: !0,
			flip: !0,
			offset: this.anchorOffset > 0 ? this.anchorOffset : void 0
		}, (e, r, i) => {
			D(this, "--top", i.includes("top")), D(this, "--bottom", i.includes("bottom")), (t(this, Tp, "f")?.dir !== Sd.current || t(this, Tp, "f")?.x !== e) && (Sd.current === "rtl" ? (this.style.right = `${window.innerWidth - e - this.clientWidth}px`, this.style.left = "") : (this.style.left = `${e}px`, this.style.right = "")), t(this, Tp, "f")?.y !== r && (this.style.top = `${r}px`), n(this, Tp, {
				x: e,
				y: r,
				dir: Sd.current
			}, "f");
		}), "f"), this.showPopover();
	}
	hide(e = !1) {
		this.hidePopover(), t(this, Sp, "f") && (t(this, Sp, "f").ariaExpanded = "false", e && t(this, Sp, "f").focus(), t(this, Cp, "f") && (t(this, Op, "f").unobserve(t(this, Cp, "f")), this.fitAnchorWidth && t(this, kp, "f").unobserve(t(this, Cp, "f"))), n(this, Sp, void 0, "f"), n(this, Cp, void 0, "f"));
	}
	async toggle(e, n) {
		t(this, Sp, "f") ? this.hide() : await this.show(e, n);
	}
	render() {
		return y`<div class="base"><slot></slot></div>`;
	}
};
Sp = /* @__PURE__ */ new WeakMap(), Cp = /* @__PURE__ */ new WeakMap(), wp = /* @__PURE__ */ new WeakMap(), Tp = /* @__PURE__ */ new WeakMap(), Ep = /* @__PURE__ */ new WeakMap(), Dp = /* @__PURE__ */ new WeakMap(), Op = /* @__PURE__ */ new WeakMap(), kp = /* @__PURE__ */ new WeakMap(), Ap.styles = l`:host { position: absolute; flex-direction: column; padding: unset; margin: unset; border: unset; overflow-y: auto; scrollbar-width: ${A.scrollbar.thinWidth}; scrollbar-color: ${A.scrollbar.color}; scroll-padding-block: calc( var(--m3e-focus-ring-thickness, 3px) + var(--m3e-floating-panel-container-padding-block, 4px) ); border-radius: var(--m3e-floating-panel-container-shape, ${A.shape.corner.large}); min-width: var(--m3e-floating-panel-container-min-width, 112px); max-width: var(--m3e-floating-panel-container-max-width, 280px); max-height: var(--m3e-floating-panel-container-max-height, 280px); background-color: var(--m3e-floating-panel-container-color, ${A.color.surfaceContainer}); box-shadow: var(--m3e-floating-panel-container-elevation, ${A.elevation.level3}); opacity: 0; display: none; } :host([hidden]) { display: none; } .base { contain: layout style paint; box-sizing: border-box; display: flex; flex-direction: column; padding-block: var(--m3e-floating-panel-container-padding-block, 4px); padding-inline: var(--m3e-floating-panel-container-padding-inline, 4px); } :host(:not(:is(:state(--no-animate), :--no-animate))) { transition: ${c(`opacity ${A.motion.duration.short2} ${A.motion.easing.standard}, 
            transform ${A.motion.duration.short2} ${A.motion.easing.standard},
            overlay ${A.motion.duration.short2} ${A.motion.easing.standard} allow-discrete,
            display ${A.motion.duration.short2} ${A.motion.easing.standard} allow-discrete`)}; } :host { transform: scaleY(0.8); } :host(:popover-open) { transform: scaleY(1); animation: bounce-open ${A.motion.spring.fastSpatial}; display: block; opacity: 1; } @keyframes bounce-open { 0% { transform: scaleY(0); } 100% { transform: scaleY(1); } } :host::backdrop { background-color: transparent; } :host(:is(:state(--bottom), :--bottom)) { transform-origin: top; } :host(:is(:state(--top), :--top)) { transform-origin: bottom; } @starting-style { :host(:popover-open) { transform: scaleY(0.8); } } @media (prefers-reduced-motion) { :host(:not(:is(:state(--no-animate), :--no-animate))) { transition: none; } } @media (forced-colors: active) { :host { background-color: Menu; color: MenuText; outline: 1px solid MenuText; } }`, e([S({ attribute: "scroll-strategy" })], Ap.prototype, "scrollStrategy", void 0), e([S({
	attribute: "fit-anchor-width",
	type: Boolean
})], Ap.prototype, "fitAnchorWidth", void 0), e([S({
	attribute: "anchor-offset",
	type: Number
})], Ap.prototype, "anchorOffset", void 0), Ap = e([w("m3e-floating-panel")], Ap);
//#endregion
//#region node_modules/@m3e/web/dist/fab-menu.js
function jp(e) {
	return e === "primary" || e === "secondary" || e === "tertiary";
}
var Mp, Np, Pp, Fp, Ip, Lp, Rp, zp, Bp, Vp, Hp, Up, Wp, Gp, Kp = class extends ia(T(j(x, "menu"))) {
	constructor() {
		super(...arguments), Mp.add(this), Np.set(this, void 0), Pp.set(this, void 0), Fp.set(this, void 0), Ip.set(this, new Mu().withWrap().withHomeAndEnd().withVerticalOrientation()), Lp.set(this, (e) => t(this, Mp, "m", Up).call(this, e)), Rp.set(this, new yr(this, {
			target: null,
			callback: () => this.hide()
		})), zp.set(this, new yt(this, {
			target: null,
			callback: () => this.hide()
		})), Bp.set(this, (e) => {
			switch (e.newState) {
				case "open":
					t(this, zp, "f").observe(this), t(this, Pp, "f") && t(this, zp, "f").observe(t(this, Pp, "f")), setTimeout(() => t(this, Ip, "f").setActiveItem(t(this, Ip, "f").items.find((e) => !e.disabled)), 40);
					break;
				case "closed": t(this, zp, "f").unobserveAll(), t(this, Fp, "f")?.call(this), n(this, Fp, void 0, "f");
			}
		}), this.variant = "primary";
	}
	get isOpen() {
		return t(this, Pp, "f") !== void 0;
	}
	async show(e) {
		t(this, Pp, "f") && t(this, Pp, "f") !== e && this.hide(), t(this, Fp, "f")?.call(this), n(this, Fp, await xp(this, e, {
			position: "top-end",
			inline: !0,
			shift: "main",
			flip: !0,
			offset: 8
		}, (e, t, n) => {
			D(this, "--right", n.includes("end")), D(this, "--left", n.includes("start")), Sd.current === "rtl" ? (this.style.right = `${window.innerWidth - e - this.clientWidth}px`, this.style.left = "") : (this.style.left = `${e}px`, this.style.right = ""), this.style.top = `${t}px`;
		}), "f"), this.showPopover(), n(this, Pp, e, "f"), t(this, Pp, "f").ariaExpanded = "true", t(this, Rp, "f").observe(t(this, Pp, "f")), t(this, Mp, "m", Wp).call(this);
	}
	hide(e = !1) {
		t(this, Fp, "f")?.call(this), n(this, Fp, void 0, "f"), t(this, Pp, "f") && (t(this, Pp, "f").ariaExpanded = "false", e && t(this, Pp, "f").focus(), t(this, Mp, "m", Gp).call(this), t(this, Rp, "f").unobserve(t(this, Pp, "f")), n(this, Pp, void 0, "f")), this.hidePopover();
	}
	async toggle(e) {
		t(this, Pp, "f") ? this.hide() : await this.show(e);
	}
	connectedCallback() {
		super.connectedCallback(), t(this, Mp, "m", Vp).call(this), this.tabIndex = -1, this.setAttribute("popover", "manual"), this.addEventListener("keydown", t(this, Lp, "f")), this.addEventListener("toggle", t(this, Bp, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("keydown", t(this, Lp, "f")), this.removeEventListener("toggle", t(this, Bp, "f"));
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, Mp, "m", Vp).call(this);
	}
	render() {
		return y`<div class="base"><slot @slotchange="${t(this, Mp, "m", Hp)}"></slot></div>`;
	}
};
Np = /* @__PURE__ */ new WeakMap(), Pp = /* @__PURE__ */ new WeakMap(), Fp = /* @__PURE__ */ new WeakMap(), Ip = /* @__PURE__ */ new WeakMap(), Lp = /* @__PURE__ */ new WeakMap(), Rp = /* @__PURE__ */ new WeakMap(), zp = /* @__PURE__ */ new WeakMap(), Bp = /* @__PURE__ */ new WeakMap(), Mp = /* @__PURE__ */ new WeakSet(), Vp = function() {
	jp(this.variant) || (this.variant = "primary"), Br(this, this.variant, "primary", "secondary", "tertiary");
}, Hp = function() {
	let { added: e } = t(this, Ip, "f").setItems([...this.querySelectorAll("m3e-fab-menu-item")]);
	t(this, Ip, "f").activeItem || t(this, Ip, "f").updateActiveItem(e.find((e) => !e.disabled));
}, Up = function(e) {
	switch (e.key) {
		case "Tab":
			this.hide();
			break;
		case "Escape":
			!e.shiftKey && !e.ctrlKey && this.hide(!0);
			break;
		default: t(this, Ip, "f").onKeyDown(e);
	}
}, Wp = function() {
	let e = t(this, Pp, "f")?.closest("m3e-fab");
	e && (n(this, Np, e.tabIndex, "f"), e.addEventListener("keydown", t(this, Lp, "f")), t(this, Ip, "f").setItems([...t(this, Ip, "f").items, e]));
}, Gp = function() {
	let e = t(this, Pp, "f")?.closest("m3e-fab");
	e && (t(this, Np, "f") !== void 0 && (e.tabIndex = t(this, Np, "f")), e.removeEventListener("keydown", t(this, Lp, "f")), t(this, Ip, "f").setItems([...t(this, Ip, "f").items.filter((t) => t !== e)]));
}, Kp.styles = l`:host { contain: layout style; position: absolute; flex-direction: column; row-gap: var(--m3e-fab-menu-spacing, ${A.measurement.space50}); padding: unset; margin: unset; border: unset; overflow: visible; max-width: var(--m3e-fab-menu-max-width, 280px); opacity: 0; background-color: transparent; display: none; } :host([hidden]) { display: none; } :host(:not(:is(:state(--no-animate), :--no-animate))) { transition: ${c(`opacity ${A.motion.spring.fastEffects}, 
        transform ${A.motion.spring.fastSpatial},
        overlay ${A.motion.spring.fastEffects} allow-discrete,
        display ${A.motion.spring.fastEffects} allow-discrete`)}; } .base { display: contents; } :host(:is(:state(--primary), :--primary)) .base { --_fab-menu-item-color: var(--m3e-primary-fab-color, ${A.color.onPrimaryContainer}); --_fab-menu-item-container-color: var(--m3e-primary-fab-container-color, ${A.color.primaryContainer}); --_fab-menu-background-hover-color: var(--m3e-primary-fab-hover-color, ${A.color.onPrimaryContainer}); --_fab-menu-background-focus-color: var(--m3e-primary-fab-focus-color, ${A.color.onPrimaryContainer}); --_fab-menu-ripple-color: var(--m3e-primary-fab-ripple-color, ${A.color.onPrimaryContainer}); } :host(:is(:state(--secondary), :--secondary)) .base { --_fab-menu-item-color: var(--m3e-secondary-fab-color, ${A.color.onSecondaryContainer}); --_fab-menu-item-container-color: var( --m3e-secondary-fab-container-color, ${A.color.secondaryContainer} ); --_fab-menu-background-hover-color: var( --m3e-secondary-fab-hover-color, ${A.color.onSecondaryContainer} ); --_fab-menu-background-focus-color: var( --m3e-secondary-fab-focus-color, ${A.color.onSecondaryContainer} ); --_fab-menu-ripple-color: var(--m3e-secondary-fab-ripple-color, ${A.color.onSecondaryContainer}); } :host(:is(:state(--tertiary), :--tertiary)) .base { --_fab-menu-item-color: var(--m3e-tertiary-fab-color, ${A.color.onTertiaryContainer}); --_fab-menu-item-container-color: var(--m3e-tertiary-fab-container-color, ${A.color.tertiaryContainer}); --_fab-menu-background-hover-color: var(--m3e-tertiary-fab-hover-color, ${A.color.onTertiaryContainer}); --_fab-menu-background-focus-color: var(--m3e-tertiary-fab-focus-color, ${A.color.onTertiaryContainer}); --_fab-menu-ripple-color: var(--m3e-tertiary-fab-ripple-color, ${A.color.onTertiaryContainer}); } :host { transform: scaleX(0); } :host(:is(:state(--left), :--left)) { align-items: flex-start; transform-origin: left; } :host(:is(:state(--right), :--right)) { align-items: flex-end; transform-origin: right; } :host(:popover-open) { transform: scaleX(1); display: inline-flex; opacity: 1; } :host::backdrop { background-color: transparent; } @starting-style { :host(:popover-open) { opacity: 0; } :host(:popover-open) { transform: scaleX(0); } } @media (prefers-reduced-motion) { :host(:not(:is(:state(--no-animate), :--no-animate))) { transition: none; } } @media (forced-colors: active) { :host { border-radius: ${A.shape.corner.medium}; outline: 1px solid MenuText; background-color: Menu; } }`, e([S({
	reflect: !0,
	useDefault: !0
})], Kp.prototype, "variant", void 0), Kp = e([w("m3e-fab-menu")], Kp);
var qp, Jp, Yp, Xp = class extends qi(Zi(vi(T(j(x, "menuitem"), !0)))) {
	constructor() {
		super(...arguments), qp.add(this), Jp.set(this, (e) => t(this, qp, "m", Yp).call(this, e));
	}
	get menu() {
		return this.closest("m3e-fab-menu");
	}
	connectedCallback() {
		super.connectedCallback(), this.addEventListener("click", t(this, Jp, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("click", t(this, Jp, "f"));
	}
	firstUpdated(e) {
		super.firstUpdated(e), [
			this._elevation,
			this._focusRing,
			this._stateLayer,
			this._ripple
		].forEach((e) => e?.attach(this));
	}
	render() {
		return y`<div class="base"><m3e-elevation class="elevation" ?disabled="${this.disabled}"></m3e-elevation><m3e-state-layer class="state-layer" ?disabled="${this.disabled}"></m3e-state-layer><m3e-focus-ring class="focus-ring" ?disabled="${this.disabled}"></m3e-focus-ring><m3e-ripple class="ripple" ?disabled="${this.disabled}"></m3e-ripple><div class="touch" aria-hidden="true"></div>${this[Ji]()}<div class="wrapper"><slot class="icon" name="icon" aria-hidden="true"></slot><div class="label"><slot></slot></div></div></div>`;
	}
};
Jp = /* @__PURE__ */ new WeakMap(), qp = /* @__PURE__ */ new WeakSet(), Yp = function(e) {
	e.defaultPrevented || this.menu?.hide(!0);
}, Xp.styles = l`:host { display: inline-block; outline: none; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } :host([hidden]) { display: none; } .base { box-sizing: border-box; vertical-align: middle; display: inline-flex; align-items: center; justify-content: center; position: relative; width: 100%; transition: ${c(`background-color ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; min-height: var(--m3e-fab-menu-item-height, 56px); font-size: var(--m3e-fab-menu-item-font-size, ${A.typescale.standard.label.large.fontSize}); font-weight: var(--m3e-fab-menu-item-font-weight, ${A.typescale.standard.label.large.fontWeight}); line-height: var(--m3e-fab-menu-item-line-height, ${A.typescale.standard.label.large.lineHeight}); letter-spacing: var(--m3e-fab-menu-item-tracking, ${A.typescale.standard.label.large.tracking}); border-radius: var(--m3e-fab-menu-item-shape, ${A.shape.corner.full}); } :host(:not(:disabled)) .label, :host(:not(:disabled)) .icon { color: var(--_fab-menu-item-color); } :host(:not(:disabled)) .base { background-color: var(--_fab-menu-item-container-color); --m3e-state-layer-hover-color: var(--_fab-menu-background-hover-color); --m3e-state-layer-focus-color: var(--_fab-menu-background-focus-color); --m3e-ripple-color: var(--_fab-menu-ripple-color); } :host(:disabled) .base { background-color: color-mix( in srgb, var(--m3e-fab-menu-item-disabled-container-color, ${A.color.onSurface}) var(--m3e-fab-menu-item-disabled-container-opacity, 10%), transparent ); } :host(:disabled) .label, :host(:disabled) .icon { color: color-mix( in srgb, var(--m3e-fab-menu-item-disabled-color, ${A.color.onSurface}) var(--m3e-fab-menu-item-disabled-opacity, 38%), transparent ); } .touch { position: absolute; height: 48px; left: 0; right: 0; } .wrapper { width: 100%; overflow: hidden; display: inline-flex; align-items: center; padding-block-start: var(--m3e-fab-menu-item-top-space, ${A.measurement.space200}); padding-block-end: var(--m3e-fab-menu-item-bottom-space, ${A.measurement.space200}); padding-inline-start: var(--m3e-fab-menu-item-leading-space, ${A.measurement.space300}); padding-inline-end: var(--m3e-fab-menu-item-trailing-space, ${A.measurement.space300}); column-gap: var(--m3e-fab-menu-item-spacing, ${A.measurement.space100}); } .label { justify-self: center; flex: 1 1 auto; text-align: center; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; transition: ${c(`color ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; } .icon { font-size: var(--m3e-fab-menu-item-icon-size, 24px); transition: ${c(`color ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; } :host(:not(:disabled)) { cursor: pointer; } ::slotted([slot="icon"]) { font-size: inherit !important; flex: none; } ::slotted(svg[slot="icon"]) { width: 1em; height: 1em; } a { all: unset; display: block; position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px; z-index: 1; } @media (prefers-reduced-motion) { .base, .label, .icon { transition: none; } } @media (forced-colors: active) { .base, .label, .icon { transition: none; } :host(:not(:disabled)) .base { background-color: Menu; } :host(:not(:disabled)) .label, :host(:not(:disabled)) .icon { color: MenuText; } :host(:disabled) .label, :host(:disabled) .icon { color: GrayText; } }`, e([C(".elevation")], Xp.prototype, "_elevation", void 0), e([C(".focus-ring")], Xp.prototype, "_focusRing", void 0), e([C(".state-layer")], Xp.prototype, "_stateLayer", void 0), e([C(".ripple")], Xp.prototype, "_ripple", void 0), Xp = e([w("m3e-fab-menu-item")], Xp);
var Zp = class extends Ki(sa) {
	get menu() {
		return this.control instanceof Kp ? this.control : null;
	}
	attach(e) {
		super.attach(e), this.parentElement && e instanceof Kp && (this.parentElement.ariaHasPopup = "menu", this.parentElement.ariaExpanded = "false", e.id && Hu(this.parentElement, "aria-controls", e.id));
	}
	detach() {
		this.parentElement && (this.parentElement.ariaHasPopup = null, this.parentElement.ariaExpanded = null, this.control?.id && Uu(this.parentElement, "aria-controls", this.control.id)), super.detach();
	}
	_onClick() {
		this.parentElement && this.menu?.toggle(this.parentElement);
	}
};
Zp = e([w("m3e-fab-menu-trigger")], Zp);
//#endregion
//#region node_modules/@m3e/web/dist/card.js
function Qp(e) {
	return e === "elevated" || e === "filled" || e === "outlined";
}
var $p = {
	padding: c(`var(--m3e-card-padding, ${A.measurement.space200})`),
	shape: c(`var(--m3e-card-shape, ${A.shape.corner.medium});`)
}, G = {
	filled: {
		textColor: c(`var(--m3e-filled-card-text-color, var(--m3e-card-text-color, ${A.color.onSurface}))`),
		containerColor: c(`var(--m3e-filled-card-container-color, var(--m3e-card-container-color, ${A.color.surfaceContainerHighest}))`),
		containerElevation: c(`var(--m3e-filled-card-container-elevation, var(--m3e-card-container-elevation, ${A.elevation.level0}))`),
		disabled: {
			textColor: c(`var(--m3e-filled-card-disabled-text-color, var(--m3e-card-disabled-text-color, ${A.color.onSurface}))`),
			textOpacity: c("var(--m3e-filled-card-disabled-text-opacity, var(--m3e-card-disabled-text-opacity, 38%))"),
			imageOpacity: c("var(--m3e-filled-card-disabled-image-opacity, var(--m3e-card-disabled-image-opacity, 38%))"),
			containerColor: c(`var(--m3e-filled-card-disabled-container-color, var(--m3e-card-disabled-container-color, ${A.color.surfaceVariant}))`),
			containerElevation: c(`var(--m3e-filled-card-disabled-container-elevation, var(--m3e-card-disabled-container-elevation, ${A.elevation.level0}))`),
			containerElevationColor: c(`var(--m3e-filled-card-disabled-container-elevation-color, var(--m3e-card-disabled-container-elevation-color, ${A.color.onSurface}))`),
			containerElevationOpacity: c("var(--m3e-filled-card-disabled-container-elevation-opacity, var(--m3e-card-disabled-container-elevation-opacity, 38%))"),
			containerOpacity: c("var(--m3e-filled-card-disabled-container-opacity, var(--m3e-card-disabled-container-opacity, 38%))")
		},
		hover: {
			textColor: c(`var(--m3e-filled-card-hover-text-color, var(--m3e-card-hover-text-color, ${A.color.onSurface}))`),
			stateLayerColor: c(`var(--m3e-filled-card-hover-state-layer-color, var(--m3e-card-hover-state-layer-color, ${A.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-filled-card-hover-state-layer-opacity, var(--m3e-card-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-filled-card-hover-container-elevation, var(--m3e-card-hover-container-elevation, ${A.elevation.level1}))`)
		},
		focus: {
			textColor: c(`var(--m3e-filled-card-focus-text-color, var(--m3e-card-focus-text-color, ${A.color.onSurface}))`),
			stateLayerColor: c(`var(--m3e-filled-card-focus-state-layer-color, var(--m3e-card-focus-state-layer-color, ${A.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-filled-card-focus-state-layer-opacity, var(--m3e-card-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-filled-card-focus-container-elevation, var(--m3e-card-focus-container-elevation, ${A.elevation.level0}))`)
		},
		pressed: {
			textColor: c(`var(--m3e-filled-card-pressed-text-color, var(--m3e-card-pressed-text-color, ${A.color.onSurface}))`),
			stateLayerColor: c(`var(--m3e-filled-card-pressed-state-layer-color, var(--m3e-card-pressed-state-layer-color, ${A.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-filled-card-pressed-state-layer-opacity, var(--m3e-card-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-filled-card-pressed-container-elevation, var(--m3e-card-pressed-container-elevation, ${A.elevation.level0}))`)
		}
	},
	elevated: {
		textColor: c(`var(--m3e-elevated-card-text-color, var(--m3e-card-text-color, ${A.color.onSurface}))`),
		containerColor: c(`var(--m3e-elevated-card-container-color, var(--m3e-card-container-color, ${A.color.surfaceContainerLow}))`),
		containerElevation: c(`var(--m3e-elevated-card-container-elevation, var(--m3e-card-container-elevation, ${A.elevation.level1}))`),
		disabled: {
			textColor: c(`var(--m3e-elevated-card-disabled-text-color, var(--m3e-card-disabled-text-color, ${A.color.onSurface}))`),
			textOpacity: c("var(--m3e-elevated-card-disabled-text-opacity, var(--m3e-card-disabled-text-opacity, 38%))"),
			imageOpacity: c("var(--m3e-elevated-card-disabled-image-opacity, var(--m3e-card-disabled-image-opacity, 38%))"),
			containerColor: c(`var(--m3e-elevated-card-disabled-container-color, var(--m3e-card-disabled-container-color, ${A.color.surface}))`),
			containerElevation: c(`var(--m3e-elevated-card-disabled-container-elevation, var(--m3e-card-disabled-container-elevation, ${A.elevation.level1}))`),
			containerElevationColor: c(`var(--m3e-elevated-card-disabled-container-elevation-color, var(--m3e-card-disabled-container-elevation-color, ${A.color.onSurface}))`),
			containerElevationOpacity: c("var(--m3e-elevated-card-disabled-container-elevation-opacity, var(--m3e-card-disabled-container-elevation-opacity, 38%))"),
			containerOpacity: c("var(--m3e-elevated-card-disabled-container-opacity, var(--m3e-card-disabled-container-opacity, 38%))")
		},
		hover: {
			textColor: c(`var(--m3e-elevated-card-hover-text-color, var(--m3e-card-hover-text-color, ${A.color.onSurface}))`),
			stateLayerColor: c(`var(--m3e-elevated-card-hover-state-layer-color, var(--m3e-card-hover-state-layer-color, ${A.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-elevated-card-hover-state-layer-opacity, var(--m3e-card-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-elevated-card-hover-container-elevation, var(--m3e-card-hover-container-elevation, ${A.elevation.level2}))`)
		},
		focus: {
			textColor: c(`var(--m3e-elevated-card-focus-text-color, var(--m3e-card-focus-text-color, ${A.color.onSurface}))`),
			stateLayerColor: c(`var(--m3e-elevated-card-focus-state-layer-color, var(--m3e-card-focus-state-layer-color, ${A.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-elevated-card-focus-state-layer-opacity, var(--m3e-card-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-elevated-card-focus-container-elevation, var(--m3e-card-focus-container-elevation, ${A.elevation.level1}))`)
		},
		pressed: {
			textColor: c(`var(--m3e-elevated-card-pressed-text-color, var(--m3e-card-pressed-text-color, ${A.color.onSurface}))`),
			stateLayerColor: c(`var(--m3e-elevated-card-pressed-state-layer-color, var(--m3e-card-pressed-state-layer-color, ${A.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-elevated-card-pressed-state-layer-opacity, var(--m3e-card-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-elevated-card-pressed-container-elevation, var(--m3e-card-pressed-container-elevation, ${A.elevation.level1}))`)
		}
	},
	outlined: {
		textColor: c(`var(--m3e-outlined-card-text-color, var(--m3e-card-text-color, ${A.color.onSurface}))`),
		containerColor: c(`var(--m3e-outlined-card-container-color, var(--m3e-card-container-color, ${A.color.surface}))`),
		containerElevation: c(`var(--m3e-outlined-card-container-elevation, var(--m3e-card-container-elevation, ${A.elevation.level0}))`),
		outlineColor: c(`var(--m3e-outlined-card-outline-color, var(--m3e-card-outline-color, ${A.color.outlineVariant}))`),
		outlineThickness: c("var(--m3e-outlined-card-outline-thickness, var(--m3e-card-outline-thickness, 1px))"),
		disabled: {
			textColor: c(`var(--m3e-outlined-card-disabled-text-color, var(--m3e-card-disabled-text-color, ${A.color.onSurface}))`),
			textOpacity: c("var(--m3e-outlined-card-disabled-text-opacity, var(--m3e-card-disabled-text-opacity, 38%))"),
			imageOpacity: c("var(--m3e-outlined-card-disabled-image-opacity, var(--m3e-card-disabled-image-opacity, 38%))"),
			containerElevation: c(`var(--m3e-outlined-card-disabled-container-elevation, var(--m3e-card-disabled-container-elevation, ${A.elevation.level0}))`),
			containerElevationColor: c(`var(--m3e-outlined-card-disabled-container-elevation-color, var(--m3e-card-disabled-container-elevation-color, ${A.color.onSurface}))`),
			containerElevationOpacity: c("var(--m3e-outlined-card-disabled-container-elevation-opacity, var(--m3e-card-disabled-container-elevation-opacity, 38%))"),
			outlineColor: c(`var(--m3e-outlined-card-disabled-outline-color, var(--m3e-card-disabled-outline-color, ${A.color.outline}))`),
			outlineOpacity: c("var(--m3e-outlined-card-disabled-outline-opacity, var(--m3e-card-disabled-outline-opacity, 12%))")
		},
		hover: {
			textColor: c(`var(--m3e-outlined-card-hover-text-color, var(--m3e-card-hover-text-color, ${A.color.onSurface}))`),
			stateLayerColor: c(`var(--m3e-outlined-card-hover-state-layer-color, var(--m3e-card-hover-state-layer-color, ${A.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-outlined-card-hover-state-layer-opacity, var(--m3e-card-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-outlined-card-hover-container-elevation, var(--m3e-card-hover-container-elevation, ${A.elevation.level1}))`),
			outlineColor: c(`var(--m3e-outlined-card-hover-outline-color, var(--m3e-card-hover-outline-color, ${A.color.outlineVariant}))`)
		},
		focus: {
			textColor: c(`var(--m3e-outlined-card-focus-text-color, var(--m3e-card-focus-text-color, ${A.color.onSurface}))`),
			stateLayerColor: c(`var(--m3e-outlined-card-focus-state-layer-color, var(--m3e-card-focus-state-layer-color, ${A.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-outlined-card-focus-state-layer-opacity, var(--m3e-card-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-outlined-card-focus-container-elevation, var(--m3e-card-focus-container-elevation, ${A.elevation.level0}))`),
			outlineColor: c(`var(--m3e-outlined-card-focus-outline-color, var(--m3e-card-focus-outline-color, ${A.color.onSurface}))`)
		},
		pressed: {
			textColor: c(`var(--m3e-outlined-card-pressed-text-color, var(--m3e-card-pressed-text-color, ${A.color.onSurface}))`),
			stateLayerColor: c(`var(--m3e-outlined-card-pressed-state-layer-color, var(--m3e-card-pressed-state-layer-color, ${A.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-outlined-card-pressed-state-layer-opacity, var(--m3e-card-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-outlined-card-pressed-container-elevation, var(--m3e-card-pressed-container-elevation, ${A.elevation.level0}))`),
			outlineColor: c(`var(--m3e-outlined-card-pressed-outline-color, var(--m3e-card-pressed-outline-color, ${A.color.outlineVariant}))`)
		}
	}
}, em = l`:host { outline: none; } :host([hidden]) { display: none; } :host(:not([inline])) { display: block; } :host(:not([inline])) .base { display: flex; } :host([inline]) { display: inline-block; vertical-align: middle; } :host([inline]) .base { display: inline-flex; } .base { width: 100%; height: 100%; position: relative; box-sizing: border-box; border-radius: ${$p.shape}; } :host([actionable]) .base { transition: ${c(`background-color ${A.motion.duration.short4} ${A.motion.easing.standard},
      border-color ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; } :host([actionable]) { user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } :host([actionable]:not(:disabled):not([disabled-interactive])) { cursor: pointer; } :host([actionable][disabled-interactive]) { cursor: not-allowed; } :host(:not([actionable])) .focus-ring, :host(:not([actionable])) .state-layer, :host(:not([actionable])) .ripple { display: none; } :host(:is(:state(--outlined), :--outlined)) .base { border-style: solid; } ::slotted([slot="content"]) { flex: 1 1 auto; } ::slotted(img), ::slotted(video) { inset: 0; object-fit: cover; } ::slotted(img[slot="header"]), ::slotted(video[slot="header"]) { border-radius: ${$p.shape}; } ::slotted([slot="actions"][end]) { justify-content: flex-end; } ::slotted([slot="header"]), ::slotted([slot="actions"]), ::slotted([slot="footer"]) { flex: none; display: flex; align-items: center; } :host(:is(:state(--vertical), :--vertical)) .base, :host(:is(:state(--horizontal), :--horizontal)) ::slotted([slot="header"]), :host(:is(:state(--horizontal), :--horizontal)) ::slotted([slot="actions"]), :host(:is(:state(--horizontal), :--horizontal)) ::slotted([slot="footer"]) { flex-direction: column; } :host(:is(:state(--horizontal), :--horizontal)) .base, :host(:is(:state(--vertical), :--vertical)) ::slotted([slot="header"]), :host(:is(:state(--vertical), :--vertical)) ::slotted([slot="actions"]), :host(:is(:state(--vertical), :--vertical)) ::slotted([slot="footer"]) { flex-direction: row; } :host(:is(:state(--horizontal), :--horizontal)) ::slotted(img), :host(:is(:state(--horizontal), :--horizontal)) ::slotted(video) { aspect-ratio: 16 / 9; } .has-content:not(.has-default) slot[name="content"], .has-content.has-default slot:not([name]) { display: inherit; flex-direction: inherit; flex: 1 1 auto; } .has-header slot[name="header"], .has-actions slot[name="actions"], .has-footer slot[name="footer"] { display: inherit; flex-direction: inherit; flex: none; } :host(:is(:state(--vertical), :--vertical)) .has-content:not(.has-default) slot[name="content"] { margin-inline: ${$p.padding}; } :host(:is(:state(--vertical), :--vertical)) .has-content:not(.has-default):not(.has-header) slot[name="content"] { margin-block-start: ${$p.padding}; } :host(:is(:state(--vertical), :--vertical)) .has-content:not(.has-default):not(.has-actions):not(.has-footer) slot[name="content"] { margin-block-end: ${$p.padding}; } :host(:is(:state(--horizontal), :--horizontal)) .has-content:not(.has-default) slot[name="content"] { margin-block: ${$p.padding}; } :host(:is(:state(--horizontal), :--horizontal)) .has-content:not(.has-default):not(.has-header) slot[name="content"] { margin-inline-start: ${$p.padding}; } :host(:is(:state(--horizontal), :--horizontal)) .has-content:not(.has-default):not(.has-actions):not(.has-footer) slot[name="content"] { margin-inline-end: ${$p.padding}; } :host(:is(:state(--vertical), :--vertical)) .has-header:not(.has-header-media) slot[name="header"] { margin-inline: ${$p.padding}; margin-block-start: ${$p.padding}; } :host(:is(:state(--horizontal), :--horizontal)) .has-header:not(.has-header-media) slot[name="header"] { margin-inline-start: ${$p.padding}; margin-block: ${$p.padding}; } .has-actions slot[name="actions"] { margin-inline: ${$p.padding}; margin-block: ${$p.padding}; } :host(:is(:state(--vertical), :--vertical)) .has-footer slot[name="footer"] { margin-inline: ${$p.padding}; margin-block-end: ${$p.padding}; } :host(:is(:state(--horizontal), :--horizontal)) .has-footer slot[name="footer"] { margin-block: ${$p.padding}; margin-inline-end: ${$p.padding}; } a { all: unset; display: block; position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px; z-index: 1; } @media (forced-colors: active) { :host([actionable]) .base { transition: none; } .base { border-style: solid !important; border-color: CanvasText !important; border-width: ${G.outlined.outlineThickness ?? c("unset")} !important; } :host([actionable]:disabled) .base, :host([actionable][disabled-interactive]) .base { color: GrayText !important; border-color: GrayText !important; } } @media (prefers-reduced-motion) { :host([actionable]) .base { transition: none; } }`;
function tm(e) {
	let t = c(`:is(:state(--${e}), :--${e})`);
	return l`:host(${t}) .base { background-color: ${G[e].containerColor ?? c("unset")}; box-shadow: ${G[e].containerElevation ?? c("unset")}; border-width: ${G[e].outlineThickness ?? c("unset")}; } :host([actionable]${t}) .base { --m3e-state-layer-hover-color: ${G[e].hover.stateLayerColor}; --m3e-state-layer-hover-opacity: ${G[e].hover.stateLayerOpacity}; --m3e-state-layer-focus-color: ${G[e].focus.stateLayerColor}; --m3e-state-layer-focus-opacity: ${G[e].focus.stateLayerOpacity}; --m3e-ripple-color: ${G[e].pressed.stateLayerColor}; --m3e-ripple-opacity: ${G[e].pressed.stateLayerOpacity}; --m3e-elevation-level: ${G[e].containerElevation ?? c("unset")}; --m3e-elevation-hover-level: ${G[e].hover.containerElevation ?? c("unset")}; --m3e-elevation-focus-level: ${G[e].focus.containerElevation ?? c("unset")}; --m3e-elevation-pressed-level: ${G[e].pressed.containerElevation ?? c("unset")}; } :host(${t}) .base { border-color: ${G[e].outlineColor ?? c("unset")}; } :host([actionable]${t}:focus .base) { border-color: ${G[e].focus.outlineColor ?? c("unset")}; } :host([actionable]${t}:hover .base) { border-color: ${G[e].hover.outlineColor ?? c("unset")}; } :host([actionable]${t}) .base.pressed { border-color: ${G[e].pressed.outlineColor ?? c("unset")}; } :host(${t}) .base { color: ${G[e].textColor ?? c("unset")}; } :host([actionable]${t}:focus) .base { color: ${G[e].focus.textColor ?? c("unset")}; } :host([actionable]${t}:hover) .base { color: ${G[e].hover.textColor ?? c("unset")}; } :host([actionable]${t}) .base.pressed { color: ${G[e].pressed.textColor ?? c("unset")}; } :host([actionable]${t}:disabled) .base, :host([actionable]${t}[disabled-interactive]) .base { --m3e-elevation-level: ${G[e].disabled.containerElevation ?? c("unset")}; --m3e-elevation-color: color-mix( in srgb, ${G[e].disabled.containerElevationColor} ${G[e].disabled.containerElevationOpacity}, transparent ); color: color-mix( in srgb, ${G[e].disabled.textColor} ${G[e].disabled.textOpacity}, transparent ); background-color: ${G[e].disabled.containerColor && G[e].disabled.containerOpacity ? c(`color-mix(
        in srgb,
        ${G[e].disabled.containerColor} ${G[e].disabled.containerOpacity},
        transparent
      )`) : c("unset")}; border-color: ${G[e].disabled.outlineColor && G[e].disabled.outlineOpacity ? c(`color-mix(
        in srgb,
        ${G[e].disabled.outlineColor} ${G[e].disabled.outlineOpacity},
        transparent
      )`) : c("unset")}; } :host([actionable]${t}:disabled) ::slotted(img), :host([actionable]${t}[disabled-interactive]) ::slotted(img), :host([actionable]${t}:disabled) ::slotted(video), :host([actionable]${t}[disabled-interactive]) ::slotted(video) { opacity: ${G[e].disabled.imageOpacity}; }`;
}
var nm = [
	tm("filled"),
	tm("elevated"),
	tm("outlined")
];
function rm(e) {
	return e === "horizontal" || e === "vertical";
}
var im, am, om, sm, cm, lm, um, dm, fm, pm, mm = class extends qi(Zi(Ui(Vi(Ci(vi(T(x), !0)))))) {
	constructor() {
		super(), im.add(this), am.set(this, (e) => t(this, im, "m", pm).call(this, e)), this.inline = !1, this.actionable = !1, this.variant = "filled", this.orientation = "vertical", new nr(this, {
			isPressedKey: (e) => e === " ",
			callback: (e) => {
				this.actionable && !this.disabled && !this.disabledInteractive && this._base?.classList.toggle("pressed", e);
			}
		});
	}
	connectedCallback() {
		this.hasAttribute("actionable") && (this.role = "button"), super.connectedCallback(), this.addEventListener("click", t(this, am, "f")), t(this, im, "m", sm).call(this), t(this, im, "m", om).call(this);
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this._base?.classList.toggle("pressed", !1), this.removeEventListener("click", t(this, am, "f"));
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, im, "m", om).call(this), e.has("orientation") && t(this, im, "m", sm).call(this);
	}
	render() {
		return y`<div class="base"><m3e-elevation class="elevation" ?disabled="${!this.actionable || this.disabled || this.disabledInteractive}"></m3e-elevation><m3e-focus-ring class="focus-ring" ?disabled="${!this.actionable || this.disabled}"></m3e-focus-ring><m3e-state-layer class="state-layer" ?disabled="${!this.actionable || this.disabled || this.disabledInteractive}"></m3e-state-layer><m3e-ripple class="ripple" ?disabled="${!this.actionable || this.disabled || this.disabledInteractive}"></m3e-ripple>${this[Ji]()}<slot name="header" @slotchange="${t(this, im, "m", cm)}"></slot><slot name="content" @slotchange="${t(this, im, "m", lm)}"><slot @slotchange="${t(this, im, "m", um)}"></slot></slot><slot name="actions" @slotchange="${t(this, im, "m", dm)}"></slot><slot name="footer" @slotchange="${t(this, im, "m", fm)}"></slot></div>`;
	}
	firstUpdated(e) {
		super.firstUpdated(e), [
			this._elevation,
			this._focusRing,
			this._stateLayer,
			this._ripple
		].forEach((e) => e?.attach(this)), !this.actionable && this.hasAttribute("tabindex") && this.removeAttribute("tabindex");
	}
	update(e) {
		super.update(e), !this.actionable && this.hasAttribute("tabindex") && this.removeAttribute("tabindex");
	}
};
am = /* @__PURE__ */ new WeakMap(), im = /* @__PURE__ */ new WeakSet(), om = function() {
	Qp(this.variant) || (this.variant = "filled"), Br(this, this.variant, "elevated", "filled", "outlined");
}, sm = function() {
	rm(this.orientation) || (this.orientation = "vertical"), Br(this, this.orientation, "horizontal", "vertical");
}, cm = function(e) {
	let t = e.target.assignedNodes({ flatten: !0 }), n = this.shadowRoot?.querySelector(".base");
	n?.classList.toggle("has-header", t.length > 0), n?.classList.toggle("has-header-media", t.some((e) => e instanceof HTMLElement && (e.tagName === "IMG" || e.tagName === "VIDEO")));
}, lm = function() {
	this.shadowRoot?.querySelector(".base")?.classList.toggle("has-content", this.querySelector(":scope > [slot='content']") !== null);
}, um = function(e) {
	this.shadowRoot?.querySelector(".base")?.classList.toggle("has-default", Rt(e.target) && this.querySelector(":scope > [slot='content']") === null);
}, dm = function(e) {
	this.shadowRoot?.querySelector(".base")?.classList.toggle("has-actions", Rt(e.target));
}, fm = function(e) {
	this.shadowRoot?.querySelector(".base")?.classList.toggle("has-footer", Rt(e.target));
}, pm = function(e) {
	(this.disabled || this.disabledInteractive) && (e.preventDefault(), e.stopImmediatePropagation());
}, mm.styles = [nm, em], e([C(".base")], mm.prototype, "_base", void 0), e([C(".elevation")], mm.prototype, "_elevation", void 0), e([C(".focus-ring")], mm.prototype, "_focusRing", void 0), e([C(".state-layer")], mm.prototype, "_stateLayer", void 0), e([C(".ripple")], mm.prototype, "_ripple", void 0), e([S({
	type: Boolean,
	reflect: !0
})], mm.prototype, "inline", void 0), e([S({
	type: Boolean,
	reflect: !0
})], mm.prototype, "actionable", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], mm.prototype, "variant", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], mm.prototype, "orientation", void 0), mm = e([w("m3e-card")], mm);
//#endregion
//#region node_modules/@m3e/web/dist/list.js
function hm(e) {
	return e === "standard" || e === "segmented";
}
var gm, _m, vm, ym, bm, xm, Sm, Cm, wm = class extends ea(T(j(x, "listitem"))) {
	constructor() {
		super(...arguments), gm.add(this), _m.set(this, new cr(this, {
			target: null,
			callback: () => t(this, gm, "m", Sm).call(this)
		})), vm.set(this, void 0), ym.set(this, void 0), bm.set(this, void 0);
	}
	get leadingContentType() {
		return t(this, ym, "f");
	}
	get trailingContentType() {
		return t(this, bm, "f");
	}
	reconnectedCallback() {
		super.reconnectedCallback(), t(this, gm, "m", xm).call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), t(this, gm, "m", xm).call(this);
	}
	render() {
		return y`<div class="base">${this._renderBase()}</div>`;
	}
	_renderBase() {
		return y`<slot name="leading" @slotchange="${this._handleLeadingSlotChange}"><slot name="leading-icon" @slotchange="${this._handleLeadingSlotChange}"></slot></slot><div class="content"><slot name="overline"></slot><slot></slot><slot name="supporting-text"></slot></div><slot name="trailing" @slotchange="${this._handleTrailingSlotChange}"><slot name="trailing-supporting-text" @slotchange="${this._handleTrailingSlotChange}"></slot><slot name="trailing-icon" @slotchange="${this._handleTrailingSlotChange}"></slot></slot>`;
	}
	_handleLeadingSlotChange(e) {
		let r = t(this, ym, "f");
		n(this, ym, t(this, gm, "m", Cm).call(this, e.target), "f"), D(this, "--has-leading", t(this, ym, "f") !== void 0), r !== t(this, ym, "f") && this.closest("m3e-list,m3e-action-list,m3e-selection-list")?.notifyLeadingContentTypeChange(r, t(this, ym, "f"));
	}
	_handleTrailingSlotChange(e) {
		let r = t(this, bm, "f");
		n(this, bm, t(this, gm, "m", Cm).call(this, e.target), "f"), D(this, "--has-trailing", t(this, bm, "f") !== void 0), r !== t(this, bm, "f") && this.closest("m3e-list,m3e-action-list,m3e-selection-list")?.notifyTrailingContentTypeChange(r, t(this, bm, "f"));
	}
};
_m = /* @__PURE__ */ new WeakMap(), vm = /* @__PURE__ */ new WeakMap(), ym = /* @__PURE__ */ new WeakMap(), bm = /* @__PURE__ */ new WeakMap(), gm = /* @__PURE__ */ new WeakSet(), xm = function() {
	let e = this.shadowRoot?.querySelector(".content");
	e && t(this, _m, "f").observe(e);
}, Sm = function() {
	let e = this.shadowRoot?.querySelector(".content") ?? null, r = e === null ? 0 : xt(e);
	t(this, vm, "f") !== r && (n(this, vm, r, "f"), requestAnimationFrame(() => {
		D(this, "--one-line", r <= 1), D(this, "--two-line", r == 2), D(this, "--three-line", r > 2);
	}));
}, Cm = function(e) {
	let t = e.assignedElements({ flatten: !0 });
	return t.some((e) => e.tagName === "VIDEO") ? "video" : t.some((e) => e.tagName === "IMG") ? "image" : t.some((e) => e.tagName === "M3E-AVATAR") ? "avatar" : t.some((e) => e.tagName === "M3E-ICON") ? "icon" : t.length > 0 ? "text" : void 0;
}, wm.styles = l`:host { display: block; } .base { display: flex; min-height: inherit; width: 100%; box-sizing: border-box; column-gap: var(--m3e-list-item-between-space, ${A.measurement.space200}); padding-inline-start: var(--m3e-list-item-leading-space, ${A.measurement.space200}); padding-inline-end: var(--m3e-list-item-trailing-space, ${A.measurement.space200}); border-top-left-radius: var( --_list-item-top-container-shape, var(--m3e-list-item-container-shape, ${A.shape.corner.none}) ); border-top-right-radius: var( --_list-item-top-container-shape, var(--m3e-list-item-container-shape, ${A.shape.corner.none}) ); border-bottom-left-radius: var( --_list-item-bottom-container-shape, var(--m3e-list-item-container-shape, ${A.shape.corner.none}) ); border-bottom-right-radius: var( --_list-item-bottom-container-shape, var(--m3e-list-item-container-shape, ${A.shape.corner.none}) ); transition: ${c(`border-radius ${A.motion.spring.fastEffects}, background-color ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; } :host(:is(:state(--one-line), :--one-line)) { min-height: calc(var(--m3e-list-item-one-line-height, 56px) + ${A.density.calc(-3)}); } :host(:is(:state(--one-line), :--one-line)) .base { padding-block-start: var(--m3e-list-item-one-line-top-space, ${A.measurement.space100}); padding-block-end: var(--m3e-list-item-one-line-bottom-space, ${A.measurement.space100}); } :host(:is(:state(--two-line), :--two-line)) { min-height: calc(var(--m3e-list-item-two-line-height, 72px) + ${A.density.calc(-3)}); } :host(:is(:state(--two-line), :--two-line)) .base { padding-block-start: var(--m3e-list-item-two-line-top-space, ${A.measurement.space100}); padding-block-end: var(--m3e-list-item-two-line-bottom-space, ${A.measurement.space100}); } :host(:is(:state(--three-line), :--three-line)) { min-height: calc(var(--m3e-list-item-three-line-height, 88px) + ${A.density.calc(-3)}); } :host(:is(:state(--three-line), :--three-line)) .base { padding-block-start: var(--m3e-list-item-three-line-top-space, ${A.measurement.space150}); padding-block-end: var(--m3e-list-item-three-line-bottom-space, ${A.measurement.space150}); } :host(:not(:is(:state(--three-line), :--three-line))) .base { align-items: center; } :host(:is(:state(--three-line), :--three-line)) .base { align-items: flex-start; } :host(:not(:disabled):not([selected])) .base:not(.hover).focus-visible:not(.pressed) { border-top-left-radius: var( --_list-item-top-container-shape, var(--m3e-list-item-focus-container-shape, ${A.shape.corner.large}) ); border-top-right-radius: var( --_list-item-top-container-shape, var(--m3e-list-item-focus-container-shape, ${A.shape.corner.large}) ); border-bottom-left-radius: var( --_list-item-bottom-container-shape, var(--m3e-list-item-focus-container-shape, ${A.shape.corner.large}) ); border-bottom-right-radius: var( --_list-item-bottom-container-shape, var(--m3e-list-item-focus-container-shape, ${A.shape.corner.large}) ); } :host(:not(:disabled):not([selected])) .base.hover, :host(:not(:disabled):not([selected])) .base.pressed { border-top-left-radius: var( --_list-item-top-container-shape, var(--m3e-list-item-hover-container-shape, ${A.shape.corner.medium}) ); border-top-right-radius: var( --_list-item-top-container-shape, var(--m3e-list-item-hover-container-shape, ${A.shape.corner.medium}) ); border-bottom-left-radius: var( --_list-item-bottom-container-shape, var(--m3e-list-item-hover-container-shape, ${A.shape.corner.medium}) ); border-bottom-right-radius: var( --_list-item-bottom-container-shape, var(--m3e-list-item-hover-container-shape, ${A.shape.corner.medium}) ); } .content { flex: 1 1 auto; display: flex; flex-direction: column; align-items: unset; justify-content: unset; } ::slotted([slot="overline"]) { font-size: var(--m3e-list-item-overline-font-size, ${A.typescale.standard.label.small.fontSize}); font-weight: var(--m3e-list-item-overline-font-weight, ${A.typescale.standard.label.small.fontWeight}); line-height: var(--m3e-list-item-overline-line-height, ${A.typescale.standard.label.small.lineHeight}); letter-spacing: var(--m3e-list-item-overline-tracking, ${A.typescale.standard.label.small.tracking}); } ::slotted([slot="supporting-text"]) { font-size: var(--m3e-list-item-supporting-text-font-size, ${A.typescale.standard.body.medium.fontSize}); font-weight: var( --m3e-list-item-supporting-text-font-weight, ${A.typescale.standard.body.medium.fontWeight} ); line-height: var( --m3e-list-item-supporting-text-line-height, ${A.typescale.standard.body.medium.lineHeight} ); letter-spacing: var( --m3e-list-item-supporting-text-tracking, ${A.typescale.standard.body.medium.tracking} ); } ::slotted(:not([slot])) { font-size: var(--m3e-list-item-font-size, ${A.typescale.standard.body.large.fontSize}); font-weight: var(--m3e-list-item-font-weight, ${A.typescale.standard.body.large.fontWeight}); line-height: var(--m3e-list-item-line-height, ${A.typescale.standard.body.large.lineHeight}); letter-spacing: var(--m3e-list-item-tracking, ${A.typescale.standard.body.large.tracking}); } :host(:is(:state(--has-leading), :--has-leading)) slot[name="leading"], :host(:is(:state(--has-trailing), :--has-trailing)) slot[name="trailing"] { display: flex; justify-content: center; } :host(:not(:is(:state(--has-leading), :--has-leading))) slot[name="leading"] { display: var(--_list-item-leading-reserved-display, contents); } :host(:not(:is(:state(--has-trailing), :--has-trailing))) slot[name="trailing"] { display: var(--_list-item-trailing-reserved-display, contents); } slot[name="leading"] { min-width: var(--_list-item-leading-reserved-space, 0px); margin-inline-start: calc(0px - var(--_list-item-leading-reserved-outset, 0px)); } slot[name="trailing"] { min-width: var(--_list-item-trailing-reserved-space, 0px); margin-inline-end: calc(0px - var(--_list-item-trailing-reserved-outset, 0px)); } slot[name="leading"], slot[name="leading"] { --_icon-button-size: auto; --_icon-button-min-size: 48px; } ::slotted(span[slot="trailing"]), ::slotted([slot="trailing-supporting-text"]) { white-space: nowrap; font-size: var(--m3e-list-item-trailing-text-font-size, ${A.typescale.standard.label.small.fontSize}); font-weight: var( --m3e-list-item-trailing-text-font-weight, ${A.typescale.standard.label.small.fontWeight} ); line-height: var( --m3e-list-item-trailing-text-line-height, ${A.typescale.standard.label.small.lineHeight} ); letter-spacing: var( --m3e-list-item-trailing-text-tracking, ${A.typescale.standard.label.small.tracking} ); } ::slotted(video[slot="leading"]), ::slotted(video[slot="trailing"]), ::slotted(img[slot="leading"]), ::slotted(img[slot="trailing"]) { display: block; margin: 0 auto; overflow: hidden; object-fit: cover; } ::slotted(video) { width: var(--m3e-list-item-video-width, 100px); height: var(--m3e-list-item-video-height, 56px); } ::slotted(video[slot="leading"]), ::slotted(video[slot="trailing"]) { border-radius: var(--m3e-list-item-video-shape, ${A.shape.corner.none}); } ::slotted(video[slot="leading"]) { margin-inline-start: calc(0px - var(--_list-item-leading-video-outset, 0px)); } ::slotted(video[slot="trailing"]) { margin-inline-end: calc(0px - var(--_list-item-trailing-video-outset, 0px)); } ::slotted(img) { user-drag: none; user-select: none; pointer-events: none; width: var(--m3e-list-item-image-width, 56px); height: var(--m3e-list-item-image-height, 56px); } ::slotted(img[slot="leading"]), ::slotted(img[slot="trailing"]) { border-radius: var(--m3e-list-item-image-shape, ${A.shape.corner.none}); } ::slotted(m3e-icon[slot="leading"]), ::slotted(m3e-icon[slot="leading-icon"]), ::slotted(m3e-icon[slot="trailing"]), ::slotted(m3e-icon[slot="trailing-icon"]) { --m3e-icon-size: var(--m3e-list-item-icon-size, 24px); } :host(:not(:disabled)) ::slotted(m3e-icon[slot="leading"]), :host(:not(:disabled)) ::slotted(m3e-icon[slot="leading-icon"]) { color: var(--m3e-list-item-leading-color, ${A.color.onSurfaceVariant}); } :host(:not(:disabled)) .content { color: var(--m3e-list-item-label-text-color, ${A.color.onSurface}); } :host(:not(:disabled)) ::slotted([slot="overline"]) { color: var(--m3e-list-item-overline-color, ${A.color.onSurfaceVariant}); } :host(:not(:disabled)) ::slotted([slot="supporting-text"]) { color: var(--m3e-list-item-supporting-text-color, ${A.color.onSurfaceVariant}); } :host(:not(:disabled)) ::slotted([slot="trailing"]), :host(:not(:disabled)) ::slotted([slot="trailing-supporting-text"]), :host(:not(:disabled)) ::slotted([slot="trailing-icon"]) { color: var(--m3e-list-item-trailing-color, ${A.color.onSurfaceVariant}); } :host(:not(:disabled)) .base { background-color: var( --_list-item-container-color, var(--m3e-list-item-container-color, ${A.color.surface}) ); } :host(:disabled) .base { background-color: var(--m3e-list-item-disabled-container-color, transparent); } :host(:disabled) ::slotted(video), :host(:disabled) ::slotted(img), :host(:disabled) ::slotted(m3e-avatar) { opacity: var(--m3e-list-item-disabled-media-opacity, 38%); } :host(:disabled) .content { color: color-mix( in srgb, var(--m3e-list-item-disabled-label-text-color, ${A.color.onSurface}) var(--m3e-list-item-disabled-label-text-opacity, 38%), transparent ); } :host(:disabled) ::slotted([slot="overline"]) { color: color-mix( in srgb, var(--m3e-list-item-disabled-overline-color, ${A.color.onSurface}) var(--m3e-list-item-disabled-overline-opacity, 38%), transparent ); } :host(:disabled) ::slotted([slot="supporting-text"]) { color: color-mix( in srgb, var(--m3e-list-item-disabled-supporting-text-color, ${A.color.onSurface}) var(--m3e-list-item-disabled-supporting-text-opacity, 38%), transparent ); } :host(:disabled) ::slotted([slot="leading"]), :host(:disabled) ::slotted([slot="leading-icon"]) { color: color-mix( in srgb, var(--m3e-list-item-disabled-leading-color, ${A.color.onSurface}) var(--m3e-list-item-disabled-leading-opacity, 38%), transparent ); } :host(:disabled) ::slotted([slot="trailing"]), :host(:disabled) ::slotted([slot="trailing-supporting-text"]), :host(:disabled) ::slotted([slot="trailing-icon"]) { color: color-mix( in srgb, var(--m3e-list-item-disabled-trailing-color, ${A.color.onSurface}) var(--m3e-list-item-disabled-trailing-opacity, 38%), transparent ); } :host(:not(:disabled)) .state-layer { --m3e-state-layer-hover-color: var(--m3e-list-item-hover-state-layer-color, ${A.color.onSurface}); --m3e-state-layer-hover-opacity: var( --m3e-list-item-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity} ); --m3e-state-layer-focus-color: var(--m3e-list-item-focus-state-layer-color, ${A.color.onSurface}); --m3e-state-layer-focus-opacity: var( --m3e-list-item-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity} ); } :host(:not(:disabled)) .ripple { --m3e-ripple-color: var(--m3e-list-item-pressed-state-layer-color, ${A.color.onSurface}); --m3e-ripple-opacity: var( --m3e-list-item-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity} ); } @media (forced-colors: active) { .base { transition: none; } :host(:disabled) ::slotted([slot="leading"]), :host(:disabled) ::slotted([slot="leading-icon"]), :host(:disabled) .content, :host(:disabled) ::slotted([slot="overline"]), :host(:disabled) ::slotted([slot="supporting-text"]), :host(:disabled) ::slotted([slot="trailing"]), :host(:disabled) ::slotted([slot="trailing-supporting-text"]), :host(:disabled) ::slotted([slot="trailing-icon"]) { color: GrayText; } } @media (prefers-reduced-motion) { .base { transition: none; } }`, wm = e([w("m3e-list-item")], wm);
var Tm, Em, Dm, Om, km, Am, jm = class extends T(j(x, "list")) {
	constructor() {
		super(...arguments), Tm.add(this), Em.set(this, []), Dm.set(this, {
			video: 0,
			image: 0,
			avatar: 0,
			icon: 0,
			text: 0
		}), Om.set(this, {
			video: 0,
			image: 0,
			avatar: 0,
			icon: 0,
			text: 0
		}), this.variant = "standard";
	}
	get items() {
		return t(this, Em, "f");
	}
	get leadingContentType() {
		return t(this, Dm, "f").video > 0 ? "video" : t(this, Dm, "f").image > 0 ? "image" : t(this, Dm, "f").avatar > 0 ? "avatar" : t(this, Dm, "f").icon > 0 ? "icon" : t(this, Dm, "f").text > 0 ? "text" : void 0;
	}
	get trailingContentType() {
		return t(this, Om, "f").video > 0 ? "video" : t(this, Om, "f").image > 0 ? "image" : t(this, Om, "f").avatar > 0 ? "avatar" : t(this, Om, "f").icon > 0 ? "icon" : t(this, Om, "f").text > 0 ? "text" : void 0;
	}
	connectedCallback() {
		super.connectedCallback(), t(this, Tm, "m", km).call(this);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, Tm, "m", km).call(this);
	}
	render() {
		return y`<slot @slotchange="${t(this, Tm, "m", Am)}"></slot>`;
	}
	notifyItemsChange() {}
	notifyLeadingContentTypeChange(e, n) {
		e && t(this, Dm, "f")[e]--, n && t(this, Dm, "f")[n]++, [
			"video",
			"image",
			"avatar",
			"icon"
		].forEach((e) => {
			D(this, `--has-leading-${e}`, this.leadingContentType === e);
		});
	}
	notifyTrailingContentTypeChange(e, n) {
		e && t(this, Om, "f")[e]--, n && t(this, Om, "f")[n]--, [
			"video",
			"image",
			"avatar",
			"icon"
		].forEach((e) => {
			D(this, `--has-trailing-${e}`, this.trailingContentType === e);
		});
	}
};
Em = /* @__PURE__ */ new WeakMap(), Dm = /* @__PURE__ */ new WeakMap(), Om = /* @__PURE__ */ new WeakMap(), Tm = /* @__PURE__ */ new WeakSet(), km = function() {
	hm(this.variant) || (this.variant = "standard"), Br(this, this.variant, "segmented", "standard");
}, Am = function(e) {
	n(this, Em, e.target.assignedElements({ flatten: !0 }).filter((e) => e instanceof wm), "f"), t(this, Em, "f").forEach((e, n) => {
		D(e, "--first", n === 0), D(e, "--last", n === t(this, Em, "f").length - 1);
	}), this.notifyItemsChange();
}, jm.styles = l`:host { display: flex; flex-direction: column; overflow-y: auto; box-sizing: border-box; padding-block: var(--m3e-list-block-padding, 0px); --m3e-divider-inset-start-size: var(--m3e-list-divider-inset-start-size, ${A.measurement.space200}); --m3e-divider-inset-end-size: var(--m3e-list-divider-inset-end-size, ${A.measurement.space300}); } :host([hidden]) { display: none; } :host(:is(:state(--standard), :--standard)) { --_list-item-leading-video-outset: var(--m3e-list-item-leading-space, ${A.measurement.space200}); --_list-item-trailing-video-outset: var(--m3e-list-item-trailing-space, ${A.measurement.space200}); --_expandable-list-item-expanded-toggle-icon-container-color: transparent; --_expandable-list-item-animation: ${A.motion.spring.fastEffects}; --_expandable-list-item-slide-animation: 0s linear; } :host(:is(:state(--segmented), :--segmented)) { row-gap: var(--m3e-segmented-list-segment-gap, ${A.measurement.space25}); --_expandable-list-item-animation: ${A.motion.spring.fastSpatial}; } :host(:is(:state(--segmented), :--segmented)) { --m3e-list-item-container-color: var(--m3e-segmented-list-item-container-color, ${A.color.surface}); --m3e-list-item-disabled-container-color: var( --m3e-segmented-list-item-disabled-container-color, ${A.color.surface} ); --m3e-list-item-container-shape: var( --m3e-segmented-list-item-container-shape, ${A.shape.corner.extraSmall} ); --m3e-list-item-hover-container-shape: var( --m3e-segmented-list-item-hover-container-shape, ${A.shape.corner.medium} ); --m3e-list-item-focus-container-shape: var( --m3e-segmented-list-item-focus-container-shape, ${A.shape.corner.large} ); --m3e-list-item-selected-container-shape: var( --m3e-segmented-list-item-selected-container-shape, ${A.shape.corner.large} ); --m3e-list-item-video-shape: var(--m3e-segmented-list-item-video-shape, ${A.shape.corner.small}); --m3e-list-item-image-shape: var(--m3e-segmented-list-item-image-shape, ${A.shape.corner.small}); --m3e-list-item-between-space: var(--m3e-segmented-list-item-spacing, ${A.measurement.space150}); --_expandable-list-item-items-segment-gap: var( --m3e-segmented-list-segment-gap, ${A.measurement.space25} ); --_expandable-list-item-expanded-top-shape: var( --m3e-segmented-list-container-shape, ${A.shape.corner.large} ); --_expandable-list-item-bottom-container-shape: var( --m3e-segmented-list-container-shape, ${A.shape.corner.large} ); } :host(:is(:state(--segmented), :--segmented)) ::slotted(:is(:state(--first), :--first)), :host(:is(:state(--segmented), :--segmented)) ::slotted(:is(:state(--has-previous-open), :--has-previous-open):not([open])) { --_list-item-top-container-shape: var(--m3e-segmented-list-container-shape, ${A.shape.corner.large}); } :host(:is(:state(--segmented), :--segmented)) ::slotted(:is(:state(--has-next-open), :--has-next-open):not([open])), :host(:is(:state(--segmented), :--segmented)) ::slotted(:is(:state(--last), :--last)) { --_list-item-bottom-container-shape: var(--m3e-segmented-list-container-shape, ${A.shape.corner.large}); } :host(:is(:state(--segmented), :--segmented)) ::slotted(m3e-divider) { display: none; } :host(:is(:state(--has-leading-video), :--has-leading-video)) { --_list-item-leading-reserved-display: block; --_list-item-leading-reserved-space: var(--m3e-list-item-video-width, 100px); } :host(:is(:state(--standard), :--standard):is(:state(--has-leading-video), :--has-leading-video)) { --_list-item-leading-reserved-outset: var(--m3e-list-item-leading-space, ${A.measurement.space200}); --_list-item-trailing-reserved-outset: var(--m3e-list-item-trailing-space, ${A.measurement.space200}); } :host(:is(:state(--has-leading-image), :--has-leading-image)) { --_list-item-leading-reserved-display: block; --_list-item-leading-reserved-space: var(--m3e-list-item-image-width, 56px); } :host(:is(:state(--has-leading-avatar), :--has-leading-avatar)) { --_list-item-leading-reserved-display: block; --_list-item-leading-reserved-space: var(--m3e-avatar-size, 40px); } :host(:is(:state(--has-leading-icon), :--has-leading-icon)) { --_list-item-leading-reserved-display: block; --_list-item-leading-reserved-space: var(--m3e-list-item-icon-size, 24px); }`, e([S({
	reflect: !0,
	useDefault: !0
})], jm.prototype, "variant", void 0), jm = e([w("m3e-list")], jm);
var Mm, Nm, Pm, Fm, Im, Lm, Rm, zm, Bm, Vm = Bm = class extends wm {
	constructor() {
		super(...arguments), Mm.add(this), Nm.set(this, Bm.__nextId++), Pm.set(this, `m3e-expandable-list-item-${t(this, Nm, "f")}-content`), Fm.set(this, `m3e-expandable-list-item-${t(this, Nm, "f")}-header`), Im.set(this, []), this.disabled = !1, this.open = !1;
	}
	get items() {
		return t(this, Im, "f");
	}
	focus(e) {
		this.button?.focus(e);
	}
	blur() {
		this.button?.blur();
	}
	click() {
		this.button?.click();
	}
	updated(e) {
		if (super.updated(e), e.has("open")) {
			for (let e = this.previousElementSibling; e; e = e.previousElementSibling) if (e instanceof wm) {
				D(e, "--has-next-open", this.open);
				break;
			}
			for (let e = this.nextElementSibling; e; e = e.nextElementSibling) if (e instanceof wm) {
				D(e, "--has-previous-open", this.open);
				break;
			}
		}
	}
	render() {
		return y`<div class="base"><m3e-list-item-button id="${t(this, Fm, "f")}" class="header" ?disabled="${this.disabled}" aria-expanded="${this.open}" aria-controls="${t(this, Pm, "f")}" @click="${t(this, Mm, "m", Lm)}"><slot name="leading" slot="leading" @slotchange="${this._handleLeadingSlotChange}"></slot><slot name="overline" slot="overline"></slot><slot></slot><slot name="supporting-text" slot="supporting-text"></slot><div class="toggle-container" slot="trailing" aria-hidden="true"><div class="toggle"><slot name="toggle-icon"><svg viewBox="0 -960 960 960" fill="currentColor"><path d="M480-344 240-584l56-56 184 184 184-184 56 56-240 240Z"/></svg></slot></div></div></m3e-list-item-button><m3e-collapsible id="${t(this, Pm, "f")}" class="items" role="list" aria-labelledby="${t(this, Fm, "f")}" ?open="${this.open}" slide @opening="${t(this, Mm, "m", Rm)}" @opened="${t(this, Mm, "m", Rm)}" @closing="${t(this, Mm, "m", Rm)}" @closed="${t(this, Mm, "m", Rm)}"><slot name="items" @slotchange="${t(this, Mm, "m", zm)}"></slot></m3e-collapsible></div>`;
	}
};
Nm = /* @__PURE__ */ new WeakMap(), Pm = /* @__PURE__ */ new WeakMap(), Fm = /* @__PURE__ */ new WeakMap(), Im = /* @__PURE__ */ new WeakMap(), Mm = /* @__PURE__ */ new WeakSet(), Lm = function(e) {
	e.defaultPrevented || (this.open = !this.open, this.closest("m3e-action-list")?.[W].updateActiveItem(this.button));
}, Rm = function(e) {
	e.stopPropagation(), [
		"opening",
		"opened",
		"closing",
		"closed"
	].forEach((t) => {
		this.button?.classList.toggle(t, e.type === t);
	}), this.dispatchEvent(new Event(e.type, { bubbles: !0 }));
}, zm = function(e) {
	n(this, Im, e.target.assignedElements({ flatten: !0 }).flatMap((e) => [...e.childNodes].filter((e) => e instanceof wm)), "f"), t(this, Im, "f").forEach((e, n) => {
		n == t(this, Im, "f").length - 1 ? e.style.setProperty("--_list-item-bottom-container-shape", "var(--_expandable-list-item-bottom-container-shape)") : e.style.removeProperty("--_list-item-bottom-container-shape");
	}), this.closest("m3e-list, m3e-action-list, m3e-selection-list")?.notifyItemsChange();
}, Vm.styles = l`:host { display: block; } .header { width: 100%; margin-bottom: 0px; z-index: 1; --_list-item-button-trailing-align-self: stretch; } .toggle-container { display: flex; align-items: center; justify-content: center; align-self: stretch; width: var(--m3e-expandable-list-item-toggle-icon-container-width, 32px); border-radius: var(--m3e-expandable-list-item-toggle-icon-container-shape, ${A.shape.corner.full}); transition: ${c(`background-color ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; } .toggle { display: flex; align-items: center; justify-content: center; vertical-align: middle; font-size: var(--m3e-expandable-list-item-toggle-icon-size, 24px); transition: ${c(`transform ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; } .toggle svg, ::slotted(svg[slot="toggle-icon"]) { width: 1em; height: 1em; } :host([open]) .toggle-container { background-color: var( --_expandable-list-item-expanded-toggle-icon-container-color, var(--m3e-expandable-list-item-expanded-toggle-icon-container-color, ${A.color.surfaceContainer}) ); } :host([open]) .toggle { transform: rotate(180deg); } :host([open]) .header { --_list-item-top-container-shape: var(--_expandable-list-item-expanded-top-shape, initial); --_list-item-bottom-container-shape: initial; margin-bottom: var(--_expandable-list-item-items-segment-gap, 0px); } :host([open]) .header, :host([open]) .items { --_list-item-container-color: var( --m3e-expandable-list-item-expanded-container-color, var(--m3e-list-item-container-color, ${A.color.surface}) ); } .base { overflow-y: hidden; } .base, ::slotted([slot="items"]) { display: flex; flex-direction: column; box-sizing: border-box; } ::slotted([slot="items"]) { flex: none; --_list-item-top-container-shape: initial; --_list-item-bottom-container-shape: initial; row-gap: var(--_expandable-list-item-items-segment-gap, 0px); } .items { --m3e-collapsible-animation: var(--_expandable-list-item-animation); --m3e-collapsible-slide-animation: var(--_expandable-list-item-slide-animation); } @media (forced-colors: active) { .header { transition: none; } :host([open]) .header.opening { margin-bottom: var(--_expandable-list-item-items-segment-gap, 0px); } } @media (prefers-reduced-motion) { .header, .toggle-container, .toggle { transition: none; } }`, Vm.__nextId = 0, e([S({
	type: Boolean,
	reflect: !0
})], Vm.prototype, "disabled", void 0), e([S({
	type: Boolean,
	reflect: !0
})], Vm.prototype, "open", void 0), e([C(".header")], Vm.prototype, "button", void 0), Vm = Bm = e([w("m3e-expandable-list-item")], Vm);
var Hm, Um, Wm = class extends jm {
	constructor() {
		super(...arguments), Hm.set(this, (e) => this[W].onKeyDown(e)), this[Um] = new Mu().withWrap().withHomeAndEnd().withVerticalOrientation().withSkipPredicate((e) => {
			if (e.disabled) return !0;
			let t = e.getRootNode().host;
			if (t.parentElement?.slot === "items") {
				let e = t.closest("m3e-expandable-list-item");
				for (; e;) {
					if (!e.open) return !0;
					let t = e.parentElement?.closest("m3e-expandable-list-item,m3e-action-list");
					if (t instanceof Vm) e = t;
					else break;
				}
			}
			return !1;
		});
	}
	connectedCallback() {
		super.connectedCallback(), this.addEventListener("keydown", t(this, Hm, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("keydown", t(this, Hm, "f"));
	}
	async notifyItemsChange() {
		let e = [...this.querySelectorAll("m3e-list-action,m3e-expandable-list-item")];
		for (let t of e) await Wt(t);
		await Wt(this);
		let { added: t } = this[W].setItems(e.map((e) => (e instanceof Vm, e.button)));
		this[W].activeItem || this[W].updateActiveItem(t.find((e) => !e.disabled));
	}
};
Hm = /* @__PURE__ */ new WeakMap(), Um = W, Wm = e([w("m3e-action-list")], Wm);
var Gm = class extends qi(Zi(Vi(vi(T(j(wm, "button"), !0))))) {
	constructor() {
		super(), new sn(this, { callback: (e, t) => this.shadowRoot?.querySelector(".base")?.classList.toggle("focus-visible", t) }), new nr(this, { callback: (e) => this.shadowRoot?.querySelector(".base")?.classList.toggle("pressed", e) }), new yn(this, { callback: (e) => this.shadowRoot?.querySelector(".base")?.classList.toggle("hover", e) });
	}
	firstUpdated(e) {
		super.firstUpdated(e), [
			this._focusRing,
			this._stateLayer,
			this._ripple
		].forEach((e) => e?.attach(this));
	}
	render() {
		return y`<div class="base"><m3e-state-layer class="state-layer" ?disabled="${this.disabled}"></m3e-state-layer><m3e-focus-ring class="focus-ring" inward ?disabled="${this.disabled}"></m3e-focus-ring><m3e-ripple class="ripple" ?disabled="${this.disabled}"></m3e-ripple>${this[Ji]()} ${super._renderBase()}</div>`;
	}
};
Gm.styles = [wm.styles, l`:host { outline: none; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } .base { position: relative; } :host(:not(:disabled)) { cursor: pointer; } a { all: unset; display: block; position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px; z-index: 1; } slot[name="trailing"] { align-self: var(--_list-item-button-trailing-align-self, auto); }`], e([C(".focus-ring")], Gm.prototype, "_focusRing", void 0), e([C(".state-layer")], Gm.prototype, "_stateLayer", void 0), e([C(".ripple")], Gm.prototype, "_ripple", void 0), Gm = e([w("m3e-list-item-button")], Gm);
var Km, qm, Jm = class extends Zi(wm, !0) {
	constructor() {
		super(...arguments), Km.add(this), this.disabled = !1;
	}
	focus(e) {
		this.button?.focus(e);
	}
	blur() {
		this.button?.blur();
	}
	click() {
		this.button?.click();
	}
	render() {
		return y`<m3e-list-item-button class="button" ?disabled="${this.disabled}" href="${rt(this.href || void 0)}" target="${rt(this.target || void 0)}" download="${rt(this.download || void 0)}" rel="${rt(this.rel || void 0)}" @click="${t(this, Km, "m", qm)}"><slot name="leading" slot="leading" @slotchange="${this._handleLeadingSlotChange}"></slot><slot name="overline" slot="overline"></slot><slot></slot><slot name="supporting-text" slot="supporting-text"></slot><slot name="trailing" slot="trailing" @slotchange="${this._handleTrailingSlotChange}"></slot></m3e-list-item-button>`;
	}
};
Km = /* @__PURE__ */ new WeakSet(), qm = function(e) {
	e.defaultPrevented || this.closest("m3e-action-list")?.[W].updateActiveItem(this.button);
}, Jm.styles = l`:host { display: block; } .button { width: 100%; }`, e([C(".button")], Jm.prototype, "button", void 0), e([S({
	type: Boolean,
	reflect: !0
})], Jm.prototype, "disabled", void 0), Jm = e([w("m3e-list-action")], Jm);
var Ym, Xm, Zm, Qm, $m, eh, th = class extends qi(Vi(Kr(vi(T(j(wm, "option"), !0))))) {
	constructor() {
		super(), Ym.add(this), Xm.set(this, void 0), Zm.set(this, ""), Qm.set(this, (e) => t(this, Ym, "m", eh).call(this, e)), new sn(this, { callback: (e, t) => this.shadowRoot?.querySelector(".base")?.classList.toggle("focus-visible", t) }), new nr(this, { callback: (e) => this.shadowRoot?.querySelector(".base")?.classList.toggle("pressed", e) }), new yn(this, { callback: (e) => this.shadowRoot?.querySelector(".base")?.classList.toggle("hover", e) });
	}
	get value() {
		return t(this, Xm, "f") ?? t(this, Zm, "f");
	}
	set value(e) {
		n(this, Xm, e, "f");
	}
	connectedCallback() {
		super.connectedCallback(), this.addEventListener("click", t(this, Qm, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("click", t(this, Qm, "f"));
	}
	firstUpdated(e) {
		super.firstUpdated(e), [
			this._focusRing,
			this._stateLayer,
			this._ripple
		].forEach((e) => e?.attach(this));
	}
	update(e) {
		if (super.update(e), e.has("selected")) {
			this.closest("m3e-selection-list")?.[W].notifySelectionChange(this);
			for (let e of this.querySelectorAll("m3e-icon")) e.toggleAttribute("filled", this.selected);
		}
	}
	render() {
		return y`<div class="base"><m3e-state-layer class="state-layer" ?disabled="${this.disabled}"></m3e-state-layer><m3e-focus-ring class="focus-ring" inward ?disabled="${this.disabled}"></m3e-focus-ring><m3e-ripple class="ripple" ?disabled="${this.disabled}"></m3e-ripple><slot name="leading" @slotchange="${this._handleLeadingSlotChange}"></slot><div class="content"><slot name="overline"></slot><slot @slotchange="${t(this, Ym, "m", $m)}"></slot><slot name="supporting-text"></slot></div><slot name="trailing" @slotchange="${this._handleTrailingSlotChange}"></slot><div class="indicator">${this.closest("m3e-selection-list")?.multi ? y`<m3e-pseudo-checkbox ?checked="${this.selected}" ?disabled="${this.disabled}"></m3e-pseudo-checkbox>` : y`<m3e-pseudo-radio ?checked="${this.selected}" ?disabled="${this.disabled}"></m3e-pseudo-radio>`}</div></div>`;
	}
};
Xm = /* @__PURE__ */ new WeakMap(), Zm = /* @__PURE__ */ new WeakMap(), Qm = /* @__PURE__ */ new WeakMap(), Ym = /* @__PURE__ */ new WeakSet(), $m = function(e) {
	n(this, Zm, Lt(e.target), "f");
}, eh = function(e) {
	let t = this.closest("m3e-selection-list");
	!e.defaultPrevented && t && (t.multi || !this.selected) && this.dispatchEvent(new Event("beforeinput", {
		bubbles: !0,
		cancelable: !0
	})) && (this.selected = !this.selected, t[W].notifySelectionChange(this), this.dispatchEvent(new Event("input", { bubbles: !0 })), this.dispatchEvent(new Event("change", { bubbles: !0 })));
}, th.styles = [wm.styles, l`:host { outline: none; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } .base { position: relative; } .indicator { min-width: 24px; min-height: 24px; display: flex; align-items: center; justify-content: center; } :host(:not(:disabled)) { cursor: pointer; } :host(:is(:state(--three-line), :--three-line)) .indicator { align-self: flex-start; margin-top: var(--m3e-list-item-three-line-top-offset, ${A.measurement.space50}); } :host([selected]) .base, :host([selected]) .state-layer, :host([selected]) .ripple, :host([selected]) .focus-ring { border-radius: var(--m3e-list-item-selected-container-shape, ${A.shape.corner.large}); } :host([selected]:not(:disabled)) .content { color: var(--m3e-list-item-selected-label-text-color, ${A.color.onSecondaryContainer}); } :host([selected]:not(:disabled)) ::slotted([slot="overline"]) { color: var(--m3e-list-item-selected-overline-color, ${A.color.onSecondaryContainer}); } :host([selected]:not(:disabled)) ::slotted([slot="supporting-text"]) { color: var(--m3e-list-item-selected-supporting-text-color, ${A.color.onSecondaryContainer}); } :host([selected]:not(:disabled)) ::slotted([slot="leading"]) { color: var(--m3e-list-item-selected-leading-color, ${A.color.onSecondaryContainer}); } :host([selected]:not(:disabled)) ::slotted([slot="trailing"]) { color: var(--m3e-list-item-selected-trailing-color, ${A.color.onSecondaryContainer}); } :host([selected]:not(:disabled)) .base { background-color: var(--m3e-list-item-selected-container-color, ${A.color.secondaryContainer}); } :host([selected]:disabled) .base { background-color: color-mix( in srgb, var(--m3e-list-item-selected-disabled-container-color, ${A.color.onSurface}) var(--m3e-list-item-selected-disabled-container-opacity, 10%), transparent ); } :host([selected]:not(:disabled)) .state-layer { --m3e-state-layer-hover-color: var( --m3e-list-item-selected-hover-state-layer-color, ${A.color.onSurface} ); --m3e-state-layer-hover-opacity: var( --m3e-list-item-selected-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity} ); --m3e-state-layer-focus-color: var( --m3e-list-item-selected-focus-state-layer-color, ${A.color.onSurface} ); --m3e-state-layer-focus-opacity: var( --m3e-list-item-selected-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity} ); } :host([selected]:not(:disabled)) .ripple { --m3e-ripple-color: var(--m3e-list-item-selected-pressed-state-layer-color, ${A.color.onSurface}); --m3e-ripple-opacity: var( --m3e-list-item-selected-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity} ); } :host(:is(:state(--hide-selection), :--hide-selection)) .indicator, :host(:not(:is(:state(--hide-selection), :--hide-selection))) ::slotted([slot="trailing"]) { display: none; } @media (forced-colors: active) { :host([selected]:not(:disabled)) .content, :host([selected]:not(:disabled)) ::slotted([slot="overline"]), :host([selected]:not(:disabled)) ::slotted([slot="supporting-text"]), :host([selected]:not(:disabled)) ::slotted([slot="leading"]), :host([selected]:not(:disabled)) ::slotted([slot="trailing"]) { color: HighlightText; forced-color-adjust: none; } :host([selected]:not(:disabled)) .base { background-color: Highlight; } }`], e([C(".focus-ring")], th.prototype, "_focusRing", void 0), e([C(".state-layer")], th.prototype, "_stateLayer", void 0), e([C(".ripple")], th.prototype, "_ripple", void 0), e([S()], th.prototype, "value", null), th = e([w("m3e-list-option")], th);
var nh, rh, ih, ah, oh, sh = class extends Di(Zr(ei(Ni(vi(T(j(jm, "listbox"))))))) {
	constructor() {
		super(...arguments), nh.add(this), rh.set(this, (e) => this[W].onKeyDown(e)), ih.set(this, (e) => t(this, nh, "m", ah).call(this, e)), this[oh] = new Bu().withWrap().withHomeAndEnd().withVerticalOrientation(), this.multi = !1, this.hideSelectionIndicator = !1;
	}
	get options() {
		return this[W]?.items ?? [];
	}
	get selected() {
		return this[W]?.selectedItems ?? [];
	}
	get value() {
		let e = this.selected.filter((e) => !e.disabled).map((e) => e.value);
		switch (e.length) {
			case 0: return null;
			case 1: return e[0];
			default: return e;
		}
	}
	get [(rh = /* @__PURE__ */ new WeakMap(), ih = /* @__PURE__ */ new WeakMap(), nh = /* @__PURE__ */ new WeakSet(), oh = W, Oi)]() {
		let e = this.value;
		if (Array.isArray(e)) {
			let t = new FormData();
			for (let n of e) t.append(this.name, n);
			return t;
		}
		return e;
	}
	connectedCallback() {
		super.connectedCallback(), this.addEventListener("keydown", t(this, rh, "f")), this.addEventListener("change", t(this, ih, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("keydown", t(this, rh, "f")), this.removeEventListener("change", t(this, ih, "f"));
	}
	update(e) {
		super.update(e), e.has("disabled") && (e.get("disabled") !== void 0 || this.disabled) && (this[W].disabled = this.disabled), e.has("multi") && (this[W].multi = this.multi, this.multi ? this.setAttribute("aria-multiselectable", "true") : this.removeAttribute("aria-multiselectable"), this[W].items.forEach((e) => e.requestUpdate())), e.has("hideSelectionIndicator") && this[W].items.forEach((e) => D(e, "--hide-selection", this.hideSelectionIndicator));
	}
	notifyItemsChange() {
		let { added: e } = this[W].setItems(this.items.filter((e) => e instanceof th));
		e.forEach((e) => D(e, "--hide-selection", this.hideSelectionIndicator)), this[W].activeItem || this[W].updateActiveItem(e.find((e) => !e.disabled));
	}
};
ah = function(e) {
	e.target !== this && (e.stopImmediatePropagation(), e.stopPropagation(), this.dispatchEvent(new Event("change", { bubbles: !0 })));
}, e([S({ type: Boolean })], sh.prototype, "multi", void 0), e([S({
	attribute: "hide-selection-indicator",
	type: Boolean
})], sh.prototype, "hideSelectionIndicator", void 0), sh = e([w("m3e-selection-list")], sh);
//#endregion
//#region node_modules/@m3e/web/dist/chips.js
function ch(e) {
	return e === "outlined" || e === "elevated";
}
var lh, uh, dh, fh, ph, mh, hh, gh, _h = class extends T(x) {
	constructor() {
		super(...arguments), lh.add(this), uh.set(this, void 0), dh.set(this, ""), this.variant = "outlined";
	}
	get value() {
		return t(this, uh, "f") ?? t(this, dh, "f");
	}
	set value(e) {
		n(this, uh, e, "f");
	}
	get label() {
		return t(this, dh, "f");
	}
	connectedCallback() {
		super.connectedCallback(), t(this, lh, "m", fh).call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), this.role === "listitem" && this.removeAttribute("tabindex"), [
			this._elevation,
			this._focusRing,
			this._stateLayer,
			this._ripple
		].forEach((e) => {
			e?.htmlFor || e?.attach(this);
		});
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, lh, "m", fh).call(this);
	}
	render() {
		let e = !_i(this) || this.disabled, n = yi(this) && this.disabledInteractive;
		return y`<div class="base"><m3e-elevation class="elevation" ?disabled="${e || n}"></m3e-elevation><m3e-state-layer class="state-layer" ?disabled="${e || n}"></m3e-state-layer><m3e-focus-ring class="focus-ring" ?disabled="${e}"></m3e-focus-ring><m3e-ripple class="ripple" ?disabled="${e || n}"></m3e-ripple><div class="touch" aria-hidden="true"></div>${Yi(this) ? this[Ji]() : b}<div class="wrapper">${t(this, lh, "m", ph).call(this)}</div></div>`;
	}
	_renderIcon() {
		return y`<slot name="icon" aria-hidden="true" @slotchange="${t(this, lh, "m", mh)}"></slot>`;
	}
	_renderTrailingIcon() {
		return y`<slot name="trailing-icon" aria-hidden="true" @slotchange="${t(this, lh, "m", hh)}"></slot>`;
	}
	_renderSlot() {
		return y`<slot @slotchange="${t(this, lh, "m", gh)}"></slot>`;
	}
};
uh = /* @__PURE__ */ new WeakMap(), dh = /* @__PURE__ */ new WeakMap(), lh = /* @__PURE__ */ new WeakSet(), fh = function() {
	ch(this.variant) || (this.variant = "outlined"), Br(this, this.variant, "elevated", "outlined");
}, ph = function() {
	return y`${this._renderIcon()}<div class="label">${this._renderSlot()}</div>${this._renderTrailingIcon()}`;
}, mh = function(e) {
	D(this, "--with-icon", Rt(e.target));
}, hh = function(e) {
	D(this, "--with-trailing-icon", Rt(e.target));
}, gh = function(e) {
	n(this, dh, Lt(e.target), "f");
}, _h.styles = l`:host { display: inline-block; vertical-align: middle; outline: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } :host([hidden]) { display: none; } .base { box-sizing: border-box; vertical-align: middle; display: inline-flex; align-items: center; justify-content: center; position: relative; width: 100%; transition: ${c(`background-color ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; border-radius: var(--m3e-chip-container-shape, ${A.shape.corner.small}); height: calc(var(--m3e-chip-container-height, 32px) + ${A.density.calc(-3)}); font-size: var(--m3e-chip-label-text-font-size, ${A.typescale.standard.label.large.fontSize}); font-weight: var(--m3e-chip-label-text-font-weight, ${A.typescale.standard.label.large.fontWeight}); line-height: var(--m3e-chip-label-text-line-height, ${A.typescale.standard.label.large.lineHeight}); letter-spacing: var(--m3e-chip-label-text-tracking, ${A.typescale.standard.label.large.tracking}); } :host(:not(m3e-chip):not(:disabled):not([disabled-interactive])) { cursor: pointer; } :host(:not(m3e-chip):not(:disabled)[disabled-interactive]) { cursor: not-allowed; } :host(:not(m3e-chip):not(:disabled):not([disabled-interactive])) .base { user-select: none; } .touch { position: absolute; height: 48px; left: 0; right: 0; } .wrapper { width: 100%; overflow: hidden; display: inline-flex; align-items: center; column-gap: var(--m3e-chip-spacing, ${A.measurement.space100}); } .label { flex: 1 1 auto; min-width: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; } a { all: unset; display: block; position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px; z-index: 1; } :host(:is(:state(--elevated), :--elevated)) .base { background-color: var(--m3e-elevated-chip-container-color, ${A.color.surfaceContainerLow}); --m3e-elevation-level: var(--m3e-elevated-chip-elevation, ${A.elevation.level1}); --m3e-elevation-hover-level: var(--m3e-elevated-chip-hover-elevation, ${A.elevation.level2}); --m3e-elevation-focus-level: var(--m3e-elevated-chip-elevation, ${A.elevation.level1}); --m3e-elevation-pressed-level: var(--m3e-elevated-chip-elevation, ${A.elevation.level1}); } :host(:is(:state(--outlined), :--outlined)) .base { outline-width: var(--m3e-outlined-chip-outline-thickness, 1px); outline-style: solid; outline-offset: calc(0px - var(--m3e-outlined-chip-outline-thickness, 1px)); } :host(:not(:disabled):not([disabled-interactive]):is(:state(--outlined), :--outlined)) .base { outline-color: var(--m3e-outlined-chip-outline-color, ${A.color.outlineVariant}); } :host(:disabled:is(:state(--outlined), :--outlined)) .base, :host([disabled-interactive]:is(:state(--outlined), :--outlined)) .base { outline-color: color-mix( in srgb, var(--m3e-outlined-chip-disabled-outline-color, ${A.color.onSurface}) var(--m3e-outlined-chip-disabled-outline-opacity, 12%), transparent ); } :host(:is(:state(--with-icon), :--with-icon)) .wrapper { padding-inline-start: var(--m3e-chip-with-icon-padding-start, ${A.measurement.space100}); } :host(:not(:is(:state(--with-icon), :--with-icon))) .wrapper { padding-inline-start: var(--m3e-chip-padding-start, ${A.measurement.space200}); } :host(:is(:state(--with-trailing-icon), :--with-trailing-icon)) .wrapper { padding-inline-end: var(--m3e-chip-with-icon-padding-end, ${A.measurement.space100}); } :host(:not(:is(:state(--with-trailing-icon), :--with-trailing-icon))) .wrapper { padding-inline-end: var(--m3e-chip-padding-end, ${A.measurement.space200}); } ::slotted([slot="icon"]), ::slotted([slot="trailing-icon"]) { flex: none; width: 1em; font-size: var(--m3e-chip-icon-size, 18px) !important; } :host(:not(:disabled):not([disabled-interactive]):not([selected])) .base { color: var(--m3e-chip-label-text-color, ${A.color.onSurface}); } :host(:not(:disabled):not([disabled-interactive]):not([selected])) ::slotted([slot="icon"]), :host(:not(:disabled):not([disabled-interactive]):not([selected])) ::slotted([slot="trailing-icon"]) { color: var(--m3e-chip-icon-color, ${A.color.primary}); } :host(:disabled) .base, :host([disabled-interactive]) .base { color: color-mix( in srgb, var(--m3e-chip-disabled-label-text-color, ${A.color.onSurface}) var(--m3e-chip-disabled-label-text-opacity, 38%), transparent ); } :host(:disabled) ::slotted([slot="icon"]), :host([disabled-interactive]) ::slotted([slot="icon"]), :host(:disabled) ::slotted([slot="trailing-icon"]), :host([disabled-interactive]) ::slotted([slot="trailing-icon"]) { color: color-mix( in srgb, var(--m3e-chip-disabled-icon-color, ${A.color.onSurface}) var(--m3e-chip-disabled-icon-opacity, 38%), transparent ); } :host(:is(:state(--elevated), :--elevated):disabled) .base, :host(:is(:state(--elevated), :--elevated)[disabled-interactive]) .base { background-color: color-mix( in srgb, var(--m3e-elevated-chip-disabled-container-color, ${A.color.onSurface}) var(--m3e-elevated-chip-disabled-container-opacity, 12%), transparent ); --m3e-elevation-level: var(--m3e-elevated-chip-disabled-elevation, ${A.elevation.level0}); } @media (prefers-reduced-motion) { .base { transition: none; } } @media (forced-colors: active) { .base { transition: none; } :host(:not(:disabled):not([disabled-interactive]):not([selected])) .base, :host(:not(:disabled):not([disabled-interactive]):not([selected])) ::slotted([slot="icon"]), :host(:not(:disabled):not([disabled-interactive]):not([selected])) ::slotted([slot="trailing-icon"]) { color: CanvasText; } :host(:not(:disabled):not([disabled-interactive]):is(:state(--outlined), :--outlined)) .base { outline-color: CanvasText; } :host(:disabled) .base, :host([disabled-interactive]) .base, :host(:disabled) ::slotted([slot="icon"]), :host([disabled-interactive]) ::slotted([slot="icon"]), :host(:disabled) ::slotted([slot="trailing-icon"]), :host([disabled-interactive]) ::slotted([slot="trailing-icon"]) { color: GrayText; } :host(:disabled:is(:state(--outlined), :--outlined)) .base, :host([disabled-interactive]:is(:state(--outlined), :--outlined)) .base { outline-color: GrayText; } }`, e([C(".elevation")], _h.prototype, "_elevation", void 0), e([C(".focus-ring")], _h.prototype, "_focusRing", void 0), e([C(".state-layer")], _h.prototype, "_stateLayer", void 0), e([C(".ripple")], _h.prototype, "_ripple", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], _h.prototype, "variant", void 0), e([S()], _h.prototype, "value", null), _h = e([w("m3e-chip")], _h);
var vh = class extends Ui(Zi(qi(Vi(Ci(vi(j(_h, "button"))))))) {
	_renderTrailingIcon() {
		return b;
	}
};
vh.formAssociated = !0, vh = e([w("m3e-assist-chip")], vh);
var yh = class extends aa(x) {
	render() {
		return y`<slot></slot>`;
	}
};
yh.styles = l`:host { display: inline-flex; flex-wrap: wrap; vertical-align: middle; gap: var(--m3e-chip-set-spacing, ${A.measurement.space100}); outline: none; } :host([hidden]) { display: none; } :host([vertical]) { flex-direction: column; }`, yh = e([w("m3e-chip-set")], yh);
var bh, xh, Sh, Ch = class extends Kr(qi(Vi(Ci(vi(j(_h, "radio")))))) {
	constructor() {
		super(...arguments), bh.add(this), xh.set(this, (e) => t(this, bh, "m", Sh).call(this, e));
	}
	connectedCallback() {
		super.connectedCallback(), this.addEventListener("click", t(this, xh, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("click", t(this, xh, "f"));
	}
	update(e) {
		super.update(e), e.has("selected") && this.closest("m3e-filter-chip-set")?.[W].notifySelectionChange(this);
	}
	_renderIcon() {
		return y`<div class="icon" aria-hidden="true"><svg class="check" viewBox="0 -960 960 960" aria-hidden="true"><path fill="currentColor" d="M382-240 154-468l57-57 171 171 367-367 57 57-424 424Z"/></svg> ${super._renderIcon()}</div>`;
	}
};
xh = /* @__PURE__ */ new WeakMap(), bh = /* @__PURE__ */ new WeakSet(), Sh = function(e) {
	e.defaultPrevented || this.dispatchEvent(new Event("beforeinput", {
		bubbles: !0,
		cancelable: !0
	})) && (this.selected = !this.selected, this.closest("m3e-filter-chip-set")?.[W].notifySelectionChange(this), this.dispatchEvent(new Event("input", { bubbles: !0 })), this.dispatchEvent(new Event("change", { bubbles: !0 })));
}, Ch.formAssociated = !0, Ch.styles = [_h.styles, l`:host([selected]:not(:is(:state(--hide-selection), :--hide-selection))) .wrapper { padding-inline-start: var(--m3e-chip-with-icon-padding-start, ${A.measurement.space100}); } .icon { display: flex; align-items: center; justify-content: center; } .check { width: 1em; font-size: var(--m3e-chip-icon-size, 18px); } :host(:not(:disabled):not([disabled-interactive])) .check { color: var(--m3e-chip-selected-leading-icon-color, ${A.color.onSecondaryContainer}); } :host(:not([selected])) .check, :host(:is(:state(--hide-selection), :--hide-selection)) .check, :host(:is(:state(--hide-selection), :--hide-selection):not(:is(:state(--with-icon), :--with-icon))) .icon { display: none; } :host(:not(:is(:state(--with-icon), :--with-icon))) .icon { margin-inline-start: calc(0px - var(--m3e-chip-with-icon-padding-start, ${A.measurement.space100})); transition: margin-inline-start ${A.motion.spring.fastEffects}; } :host([selected]) .icon { margin-inline-start: 0; } :host([selected]:not(:is(:state(--hide-selection), :--hide-selection))) ::slotted([slot="icon"]) { display: none !important; } :host(:not(:disabled):not([disabled-interactive]):not([selected])) .base { color: var(--m3e-chip-unselected-label-text-color, ${A.color.onSurfaceVariant}); --m3e-ripple-color: var(--m3e-chip-unselected-ripple-color, ${A.color.onSurfaceVariant}); --m3e-state-layer-focus-color: var( --m3e-chip-unselected-state-layer-focus-color, ${A.color.onSurfaceVariant} ); --m3e-state-layer-hover-color: var( --m3e-chip-unselected-state-layer-hover-color, ${A.color.onSurfaceVariant} ); } :host(:not(:disabled):not([disabled-interactive]):not([selected])) ::slotted([slot="icon"]) { color: var(--m3e-chip-unselected-leading-icon-color, ${A.color.primary}); } :host(:not(:disabled):not([disabled-interactive]):not([selected])) ::slotted([slot="trailing-icon"]) { color: var(--m3e-chip-unselected-trailing-icon-color, ${A.color.onSurfaceVariant}); } :host(:not(:disabled):not([disabled-interactive])[selected]) .base { outline-offset: unset; outline-width: var(--m3e-chip-selected-outline-thickness, 0); color: var(--m3e-chip-selected-label-text-color, ${A.color.onSecondaryContainer}); background-color: var(--m3e-chip-selected-container-color, ${A.color.secondaryContainer}); --m3e-state-layer-hover-color: var( --m3e-chip-selected-container-hover-color, ${A.color.onSecondaryContainer} ); --m3e-state-layer-focus-color: var( --m3e-chip-selected-container-focus-color, ${A.color.onSecondaryContainer} ); --m3e-elevation-hover-level: var(--m3e-chip-selected-hover-elevation, ${A.elevation.level1}); --m3e-ripple-color: var(--m3e-chip-selected-ripple-color, ${A.color.onSecondaryContainer}); --m3e-state-layer-focus-color: var( --m3e-chip-selected-state-layer-focus-color, ${A.color.onSecondaryContainer} ); --m3e-state-layer-hover-color: var( --m3e-chip-selected-state-layer-hover-color, ${A.color.onSecondaryContainer} ); } :host(:not(:disabled):not([disabled-interactive])[selected]) ::slotted([slot="icon"]) { color: var(--m3e-chip-selected-leading-icon-color, ${A.color.onSecondaryContainer}); } :host(:not(:disabled):not([disabled-interactive]):not([selected])) ::slotted([slot="trailing-icon"]) { color: var(--m3e-chip-selected-trailing-icon-color, ${A.color.onSecondaryContainer}); } @media (prefers-reduced-motion) { .base, :host(:not(:is(:state(--with-icon), :--with-icon))) .icon { transition: none; } } @media (forced-colors: active) { :host(:not(:disabled):not([disabled-interactive]):not([selected])) .base, :host(:not(:disabled):not([disabled-interactive]):not([selected])) ::slotted([slot="icon"]), :host(:not(:disabled):not([disabled-interactive]):not([selected])) ::slotted([slot="trailing-icon"]) { color: CanvasText; } :host(:not(:disabled):not([disabled-interactive])[selected]) .base, :host(:not(:disabled):not([disabled-interactive])[selected]) ::slotted([slot="icon"]), :host(:not(:disabled):not([disabled-interactive])[selected]) ::slotted([slot="trailing-icon"]), :host(:not(:disabled):not([disabled-interactive])) .check { color: ButtonText; } :host(:not(:disabled):not([disabled-interactive])[selected]) .base { outline-offset: calc(0px - var(--m3e-outlined-chip-outline-thickness, 1px)); outline-width: var(--m3e-outlined-chip-outline-thickness, 1px); outline-color: ButtonText; } }`], Ch = e([w("m3e-filter-chip")], Ch);
var wh, Th, Eh, Dh, Oh, kh, Ah, jh = class extends Di(Zr(ei(Ni(vi(T(j(yh, "radiogroup"))))))) {
	constructor() {
		super(...arguments), wh.add(this), Th.set(this, void 0), this[Ah] = new Bu().onActiveItemChange(() => this[W].activeItem?.focus()).withWrap().withDirectionality(Sd.current), this.multi = !1, this.hideSelectionIndicator = !1;
	}
	get chips() {
		return this[W]?.items ?? [];
	}
	get selected() {
		return this[W]?.selectedItems ?? [];
	}
	get value() {
		let e = this.selected.filter((e) => !e.disabled).map((e) => e.value);
		switch (e.length) {
			case 0: return null;
			case 1: return e[0];
			default: return e;
		}
	}
	get [(Th = /* @__PURE__ */ new WeakMap(), wh = /* @__PURE__ */ new WeakSet(), Ah = W, Oi)]() {
		let e = this.value;
		if (Array.isArray(e)) {
			let t = new FormData();
			for (let n of e) t.append(this.name, n);
			return t;
		}
		return e;
	}
	connectedCallback() {
		super.connectedCallback(), n(this, Th, Sd.observe(() => this[W].directionality = Sd.current), "f");
	}
	disconnectedCallback() {
		super.disconnectedCallback(), t(this, Th, "f")?.call(this);
	}
	update(e) {
		super.update(e), e.has("vertical") && (this[W].vertical = this.vertical), e.has("disabled") && (e.get("disabled") !== void 0 || this.disabled) && (this[W].disabled = this.disabled), e.has("multi") && (this.role = this.multi ? "group" : "radiogroup", t(this, wh, "m", kh).call(this), this[W].multi = this.multi, this[W].disableRovingTabIndex(this.multi)), (e.has("multi") || e.has("disabled")) && (this.ariaDisabled = this.multi && this.disabled ? "true" : null), e.has("hideSelectionIndicator") && this.chips.forEach((e) => D(e, "--hide-selection", this.hideSelectionIndicator));
	}
	render() {
		return y`<slot @slotchange="${t(this, wh, "m", Eh)}" @keydown="${t(this, wh, "m", Dh)}" @change="${t(this, wh, "m", Oh)}"></slot>`;
	}
};
Eh = function() {
	let { added: e } = this[W].setItems([...this.querySelectorAll("m3e-filter-chip")]);
	e.forEach((e) => D(e, "--hide-selection", this.hideSelectionIndicator)), t(this, wh, "m", kh).call(this);
}, Dh = function(e) {
	this.multi || this[W].onKeyDown(e);
}, Oh = function(e) {
	e.stopPropagation(), this.dispatchEvent(new Event("change", { bubbles: !0 }));
}, kh = function() {
	this.chips.forEach((e) => {
		this.multi ? (e.role = "button", e.ariaPressed = `${e.selected}`, e.ariaSelected = e.ariaChecked = null) : (e.role = "radio", e.ariaChecked = `${e.selected}`, e.ariaPressed = e.ariaSelected = null);
	});
}, e([S({ type: Boolean })], jh.prototype, "multi", void 0), e([S({
	attribute: "hide-selection-indicator",
	type: Boolean
})], jh.prototype, "hideSelectionIndicator", void 0), jh = e([w("m3e-filter-chip-set")], jh);
var Mh, Nh, Ph, Fh, Ih = class extends Ci(vi(j(_h, "row"))) {
	constructor() {
		super(...arguments), Mh.add(this), this.removable = !1, this.removeLabel = "Remove";
	}
	connectedCallback() {
		super.connectedCallback(), this.removeAttribute("tabindex");
	}
	update(e) {
		super.update(e), this.removeAttribute("tabindex"), e.has("removable") && D(this, "--with-trailing-icon", this.removable);
	}
	render() {
		return y`<div class="base"><m3e-elevation class="elevation" for="cell" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-elevation><m3e-state-layer class="state-layer" for="cell" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-state-layer><m3e-focus-ring class="focus-ring" for="cell" ?disabled="${this.disabled}"></m3e-focus-ring><m3e-ripple class="ripple" for="cell" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-ripple><div class="wrapper"><div id="cell" class="cell" role="gridcell" tabindex="${rt(this.disabled ? void 0 : "-1")}" @keydown="${t(this, Mh, "m", Fh)}"><slot name="avatar" @slotchange="${t(this, Mh, "m", Nh)}"></slot>${this._renderIcon()}<div class="label">${this._renderSlot()}</div><div class="touch" aria-hidden="true"></div></div>${this._renderTrailingIcon()}</div></div>`;
	}
	_renderTrailingIcon() {
		return this.removable ? y`<span role="gridcell" class="remove"><m3e-icon-button class="remove-button" aria-label="${this.removeLabel}" size="extra-small" tabindex="-1" ?disabled="${this.disabled}" ?disabled-interactive="${this.disabledInteractive}" @click="${t(this, Mh, "m", Ph)}"><slot name="remove-icon"><svg class="remove-icon" viewBox="0 -960 960 960" fill="currentColor"><path d="m256-200-56-56 224-224-224-224 56-56 224 224 224-224 56 56-224 224 224 224-56 56-224-224-224 224Z"/></svg></slot></m3e-icon-button></span>` : b;
	}
};
Mh = /* @__PURE__ */ new WeakSet(), Nh = function(e) {
	D(this, "--with-avatar", Rt(e.target));
}, Ph = function(e) {
	e.stopPropagation(), this.dispatchEvent(new Event("remove"));
}, Fh = function(e) {
	if (this.removable) switch (e.key) {
		case "Backspace":
		case "Delete": this.dispatchEvent(new Event("remove"));
	}
}, Ih.formAssociated = !0, Ih.styles = [_h.styles, l`.cell { display: inline-flex; align-items: center; outline: none; column-gap: var(--m3e-chip-spacing, ${A.measurement.space100}); min-width: 0; } .remove { line-height: 0; } .remove-button { --m3e-icon-button-extra-small-container-height: 24px; --m3e-icon-button-extra-small-icon-size: var(--m3e-chip-icon-size, 18px); width: 24px; } .remove-icon { flex: none; width: var(--m3e-chip-icon-size, 18px); height: var(--m3e-chip-icon-size, 18px); } .touch { top: calc( 0px - calc(calc(48px - calc(var(--m3e-chip-container-height, 32px) + ${A.density.calc(-3)})) / 2) ); } .wrapper { height: 100%; overflow: visible; min-width: 0; } ::slotted([slot="avatar"]) { flex: none; font-size: var(--m3e-chip-avatar-size, 24px); } ::slotted(m3e-avatar[slot="avatar"]) { --m3e-icon-size: var(--m3e-chip-avatar-icon-size, 18px); --m3e-avatar-size: var(--m3e-chip-avatar-size, 24px); --m3e-avatar-font-size: var( --m3e-chip-avatar-font-size, ${A.typescale.standard.title.small.fontSize} ); --m3e-avatar-font-weight: var( --m3e-chip-avatar-font-height, ${A.typescale.standard.title.small.fontWeight} ); --m3e-avatar-line-height: var( --m3e-chip-avatar-line-height, ${A.typescale.standard.title.small.lineHeight} ); --m3e-avatar-tracking: var(--m3e-chip-avatar-tracking, ${A.typescale.standard.title.small.tracking}); } :host(:disabled) ::slotted([slot="avatar"]), :host([disabled-interactive]) ::slotted([slot="avatar"]) { opacity: var(--m3e-chip-disabled-avatar-opacity, 38%); color: var(--m3e-chip-disabled-icon-color, ${A.color.onSurface}); } :host(:is(:state(--with-avatar), :--with-avatar)) ::slotted([slot="icon"]) { display: none; } :host(:is(:state(--with-avatar), :--with-avatar)) .wrapper { padding-inline-start: var(--m3e-chip-with-avatar-padding-start, ${A.measurement.space50}); } @media (forced-colors: active) { :host(:disabled) ::slotted([slot="avatar"]), :host([disabled-interactive]) ::slotted([slot="avatar"]) { color: CanvasText; } }`], e([C(".cell")], Ih.prototype, "cell", void 0), e([C(".remove-button")], Ih.prototype, "removeButton", void 0), e([S({ type: Boolean })], Ih.prototype, "removable", void 0), e([S({ attribute: "remove-label" })], Ih.prototype, "removeLabel", void 0), Ih = e([w("m3e-input-chip")], Ih);
var Lh, Rh, zh, Bh, Vh, Hh, Uh, Wh, Gh, Kh, qh, Jh, Yh, Xh, Zh, Qh, $h, eg, tg, ng, rg, ig, ag, og, sg, cg = class extends na(ta(zi(Zr(ei(Ni(vi(T(j(yh, "grid"))))))), !1)) {
	constructor() {
		super(...arguments), Lh.add(this), Rh.set(this, void 0), zh.set(this, () => t(this, Lh, "m", ag).call(this)), Bh.set(this, (e) => t(this, Lh, "m", og).call(this, e)), Vh.set(this, () => t(this, Lh, "m", eg).call(this)), Hh.set(this, () => t(this, Lh, "m", tg).call(this)), Uh.set(this, () => t(this, Lh, "m", ng).call(this)), Wh.set(this, (e) => t(this, Lh, "m", rg).call(this, e)), Gh.set(this, (e) => t(this, Lh, "m", ig).call(this, e)), Kh.set(this, new su()), qh.set(this, new Ou().onActiveItemChange(() => t(this, qh, "f").activeItem?.focus()).withHomeAndEnd().withSkipPredicate((e) => !e.hasAttribute("tabindex")).withDirectionality(Sd.current)), Jh.set(this, !1), Yh.set(this, null), Xh.set(this, 0), this._focused = !1, this.maxChips = null;
	}
	get chips() {
		return [...this.querySelectorAll("m3e-input-chip")];
	}
	get value() {
		let e = this.chips.filter((e) => !e.disabled).map((e) => e.value);
		return e.length == 0 ? null : e;
	}
	get [(Rh = /* @__PURE__ */ new WeakMap(), zh = /* @__PURE__ */ new WeakMap(), Bh = /* @__PURE__ */ new WeakMap(), Vh = /* @__PURE__ */ new WeakMap(), Hh = /* @__PURE__ */ new WeakMap(), Uh = /* @__PURE__ */ new WeakMap(), Wh = /* @__PURE__ */ new WeakMap(), Gh = /* @__PURE__ */ new WeakMap(), Kh = /* @__PURE__ */ new WeakMap(), qh = /* @__PURE__ */ new WeakMap(), Jh = /* @__PURE__ */ new WeakMap(), Yh = /* @__PURE__ */ new WeakMap(), Xh = /* @__PURE__ */ new WeakMap(), Lh = /* @__PURE__ */ new WeakSet(), Oi)]() {
		let e = this.value;
		if (!e) return null;
		let t = new FormData();
		for (let n of e) t.append(this.name, n);
		return t;
	}
	get shouldLabelFloat() {
		return this.chips.length > 0;
	}
	onContainerClick() {
		t(this, Yh, "f")?.focus();
	}
	connectedCallback() {
		super.connectedCallback(), customElements.get("m3e-form-field") ? this.closest("m3e-form-field")?.notifyControlStateChange() : customElements.whenDefined("m3e-form-field").then(() => {
			this.closest("m3e-form-field")?.notifyControlStateChange();
		}), n(this, Xh, Number.parseInt(this.getAttribute("tabindex") ?? "0"), "f"), this.addEventListener("focus", t(this, Vh, "f")), this.addEventListener("focusin", t(this, Hh, "f")), this.addEventListener("focusout", t(this, Uh, "f")), n(this, Rh, Sd.observe(() => t(this, qh, "f").directionality = Sd.current), "f");
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("focus", t(this, Vh, "f")), this.removeEventListener("focusin", t(this, Hh, "f")), this.removeEventListener("focusout", t(this, Uh, "f")), t(this, Rh, "f")?.call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), this.hasAttribute("tabindex") || this.setAttribute("tabindex", `${t(this, Xh, "f")}`);
	}
	update(e) {
		super.update(e), e.has("vertical") && (this.ariaOrientation = null), e.has("disabled") && (t(this, Kh, "f").items.forEach((e) => e.disabled = this.disabled), t(this, Yh, "f") && (t(this, Yh, "f").disabled = this.disabled));
	}
	updated(e) {
		super.updated(e), (e.has("maxChips") || e.has("_focused")) && t(this, Lh, "m", sg).call(this);
	}
	render() {
		return y`<slot @keydown="${t(this, Lh, "m", Zh)}" @slotchange="${t(this, Lh, "m", Qh)}"></slot>${!this._focused && this.maxChips && this.chips.length > this.maxChips ? y`<div class="overflow">+${this.chips.length - this.maxChips}</div>` : b} <span role="row"><span role="gridcell"><slot name="input" @slotchange="${t(this, Lh, "m", $h)}"></slot></span></span>`;
	}
};
Zh = function(e) {
	t(this, qh, "f").onKeyDown(e);
}, Qh = async function() {
	let { added: e, removed: n } = t(this, Kh, "f").setItems([...this.querySelectorAll("m3e-input-chip")]);
	for (let n of e) await Wt(n), this.disabled && (n.disabled = !0), n.addEventListener("remove", t(this, Wh, "f")), n.cell.addEventListener("click", t(this, Gh, "f"));
	n.forEach((e) => {
		e.removeEventListener("remove", t(this, Wh, "f")), e.cell.removeEventListener("click", t(this, Gh, "f"));
	}), t(this, qh, "f").setItems(t(this, Kh, "f").items.flatMap((e) => e.removeButton ? [e.cell, e.removeButton] : [e.cell])), t(this, qh, "f").activeItem || t(this, qh, "f").updateActiveItem(t(this, qh, "f").items.find((e) => e.hasAttribute("tabindex"))), t(this, Lh, "m", sg).call(this);
}, $h = function() {
	let e = this.querySelector("input");
	if (t(this, Yh, "f") && (t(this, Yh, "f").removeEventListener("change", t(this, zh, "f")), t(this, Yh, "f").removeEventListener("keydown", t(this, Bh, "f"))), n(this, Yh, e, "f"), t(this, Yh, "f")) {
		t(this, Yh, "f").disabled = this.disabled, t(this, Yh, "f").addEventListener("change", t(this, zh, "f")), t(this, Yh, "f").addEventListener("keydown", t(this, Bh, "f"));
		let n = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, "value");
		Object.defineProperty(e, "value", {
			get: () => n.get?.call(e),
			set: (r) => {
				n.set?.call(e, r), t(this, Yh, "f") === e && !t(this, Jh, "f") && t(this, Lh, "m", ag).call(this);
			}
		});
	}
}, eg = function() {
	setTimeout(() => (t(this, qh, "f").activeItem ?? t(this, Yh, "f"))?.focus());
}, tg = function() {
	this.setAttribute("tabindex", "-1"), this._focused = !0;
}, ng = function() {
	this.setAttribute("tabindex", `${t(this, Xh, "f")}`), this._focused = !1;
}, rg = function(e) {
	let n = e.target, r = t(this, Kh, "f").items.indexOf(n), i = t(this, Kh, "f").items.find((e, t) => t > r && !e.disabled && e.removable);
	n.remove(), t(this, qh, "f").setActiveItem(t(this, qh, "f").items.find((e) => e === i?.removeButton)), t(this, qh, "f").activeItem || t(this, Yh, "f")?.focus(), this.dispatchEvent(new CustomEvent("change", {
		bubbles: !0,
		detail: {
			type: "remove",
			value: n.value,
			chip: n
		}
	}));
}, ig = function(e) {
	t(this, qh, "f").updateActiveItem(e.composedPath().find((e) => e instanceof Ih)?.cell);
}, ag = async function() {
	if (!t(this, Yh, "f")) return;
	let e = t(this, Yh, "f").value;
	if (!e) return;
	let r = null, i = this.getRootNode().querySelector(`m3e-autocomplete[for="${t(this, Yh, "f").id}"]`)?.querySelectorAll("m3e-option");
	if (i) {
		for (let t of i) if (t.label === e) {
			r = t;
			break;
		}
	}
	let a = null;
	if (this.chips.some((t) => t.value === (r?.value ?? e)) || (a = document.createElement("m3e-input-chip"), a.removable = !0, a.appendChild(document.createTextNode(e)), r && (a.value = r.value), this.appendChild(a), await Wt(a)), t(this, Yh, "f")) try {
		n(this, Jh, !0, "f"), t(this, Yh, "f").value = "";
	} finally {
		n(this, Jh, !1, "f");
	}
	a && this.dispatchEvent(new CustomEvent("change", {
		bubbles: !0,
		detail: {
			type: "add",
			value: e,
			chip: a
		}
	}));
}, og = function(e) {
	if (e.key === "Backspace" && !t(this, Yh, "f")?.value) {
		let e = [...t(this, Kh, "f").items].reverse().find((e) => !e.disabled && !e.disabledInteractive && e.removable);
		e && e.dispatchEvent(new Event("remove"));
	}
}, sg = function() {
	this.chips.forEach((e, t) => e.hidden = !this._focused && this.maxChips != null && t >= this.maxChips);
}, Vt(l`m3e-input-chip-set [slot="input"]::placeholder { user-select: none; color: currentColor; transition: opacity ${A.motion.duration.extraLong1}; } m3e-input-chip-set:not(:focus-within) [slot="input"]::placeholder { opacity: 0; transition: 0s; } m3e-input-chip-set:hover [slot="input"]::placeholder { transition: 0s; } @media (prefers-reduced-motion) { m3e-input-chip-set [slot="input"]::placeholder { transition: none !important; } }`), cg.styles = [yh.styles, l`::slotted([slot="input"]) { outline: unset; border: unset; background-color: transparent; box-shadow: none; font-family: inherit; font-size: inherit; line-height: initial; letter-spacing: inherit; color: var(--_form-field-input-color, inherit); flex: 1 1 auto; min-width: 0; padding: unset; } ::slotted(m3e-input-chip) { min-width: 0; } span[role="row"], span[role="gridcell"] { display: contents; } .overflow { display: flex; flex: none; align-items: center; justify-content: center; font-size: var(--m3e-chip-label-text-font-size, ${A.typescale.standard.label.large.fontSize}); font-weight: var(--m3e-chip-label-text-font-weight, ${A.typescale.standard.label.large.fontWeight}); line-height: var(--m3e-chip-label-text-line-height, ${A.typescale.standard.label.large.lineHeight}); letter-spacing: var(--m3e-chip-label-text-tracking, ${A.typescale.standard.label.large.tracking}); } :host(:not(:disabled)) .overflow { color: var(--m3e-chip-label-text-color, ${A.color.onSurface}); } :host(:disabled) .overflow { color: color-mix( in srgb, var(--m3e-chip-disabled-label-text-color, ${A.color.onSurface}) var(--m3e-chip-disabled-label-text-opacity, 38%), transparent ); }`], e([Ze()], cg.prototype, "_focused", void 0), e([S({
	attribute: "max-chips",
	type: Number
})], cg.prototype, "maxChips", void 0), cg = e([w("m3e-input-chip-set")], cg);
var lg = class extends Ui(Zi(qi(Vi(Ci(vi(j(_h, "button"))))))) {
	_renderTrailingIcon() {
		return b;
	}
};
lg.formAssociated = !0, lg = e([w("m3e-suggestion-chip")], lg);
//#endregion
//#region node_modules/@m3e/web/dist/toolbar.js
function ug(e) {
	return e === "standard" || e === "vibrant";
}
function dg(e) {
	return e === "rounded" || e === "square";
}
var fg = {
	square: {
		shape: c(`var(--m3e-toolbar-square-shape, ${A.shape.corner.none})`),
		leadingSpace: c(`var(--m3e-toolbar-square-leading-space, ${A.measurement.space200})`),
		trailingSpace: c(`var(--m3e-toolbar-square-trailing-space, ${A.measurement.space200})`),
		topSpace: c(`var(--m3e-toolbar-square-top-space, ${A.measurement.space100})`),
		bottomSpace: c(`var(--m3e-toolbar-square-bottom-space, ${A.measurement.space100})`)
	},
	rounded: {
		shape: c(`var(--m3e-toolbar-rounded-shape, ${A.shape.corner.full})`),
		leadingSpace: c(`var(--m3e-toolbar-rounded-leading-space, ${A.measurement.space100})`),
		trailingSpace: c(`var(--m3e-toolbar-rounded-trailing-space, ${A.measurement.space100})`),
		topSpace: c(`var(--m3e-toolbar-rounded-top-space, ${A.measurement.space100})`),
		bottomSpace: c(`var(--m3e-toolbar-rounded-bottom-space, ${A.measurement.space100})`)
	}
};
function pg(e) {
	let t = c(`:is(:state(--${e}), :--${e})`);
	return l`:host(${t}) .base, :host(${t}) .state-layer, :host(${t}) .elevation { border-radius: ${fg[e].shape}; } :host(${t}:not([vertical])) .base { padding-inline-start: ${fg[e].leadingSpace}; padding-inline-end: ${fg[e].trailingSpace}; padding-block-start: calc(${fg[e].topSpace} + ${A.density.calc(-3)}); padding-block-end: calc(${fg[e].bottomSpace} + ${A.density.calc(-3)}); } :host(${t}[vertical]) .base { padding-block-start: calc(${fg[e].leadingSpace} + ${A.density.calc(-3)}); padding-block-end: calc(${fg[e].trailingSpace} + ${A.density.calc(-3)}); padding-inline-start: ${fg[e].topSpace}; padding-inline-end: ${fg[e].bottomSpace}; }`;
}
var mg = [pg("square"), pg("rounded")], hg = {
	size: c("var(--m3e-toolbar-size, 64px)"),
	spacing: c(`var(--m3e-toolbar-spacing, ${A.measurement.space50})`)
}, gg = l`:host { display: inline-block; position: relative; } :host([hidden]) { display: none; } .base { display: flex; align-items: center; box-sizing: border-box; transition: ${c(`color ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; --_icon-button-size: auto; --_icon-button-min-size: 48px; } :host(:not([vertical])) { height: fit-content; } :host(:not([vertical])) .base { min-height: calc(${hg.size} + ${A.density.calc(-3)}); column-gap: ${hg.spacing}; } :host([vertical]) { width: fit-content; } :host([vertical]) .base { min-width: calc(${hg.size} + ${A.density.calc(-3)}); } :host([vertical]) .base { flex-direction: column; justify-content: center; row-gap: ${hg.spacing}; } .state-layer { transition: ${c(`background-color ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; } @media (forced-colors: active) { :host(:is(:state(--standard), :--standard)) .state-layer, :host(:is(:state(--vibrant), :--vibrant)) .state-layer { background-color: Canvas; } :host(:is(:state(--standard), :--standard)) .base, :host(:is(:state(--vibrant), :--vibrant)) .base { color: CanvasText; outline: 1px solid CanvasText; } .state-layer, .base { transition: none; } } @media (prefers-reduced-motion) { .state-layer, .base { transition: none; } }`, K = {
	standard: {
		containerColor: c(`var(--m3e-toolbar-standard-container-color, var(--m3e-toolbar-container-color, ${A.color.surfaceContainer}))`),
		buttonContainerColor: c(`var(--m3e-toolbar-standard-button-container-color, var(--m3e-toolbar-button-container-color, ${A.color.surfaceContainer}))`),
		selectedButtonContainerColor: c(`var(--m3e-toolbar-standard-selected-button-container-color, var(--m3e-toolbar-selected-button-container-color, ${A.color.secondaryContainer}))`),
		iconColor: c(`var(--m3e-toolbar-standard-icon-color, var(--m3e-toolbar-icon-color, ${A.color.onSurfaceVariant}))`),
		selectedIconColor: c(`var(--m3e-toolbar-standard-selected-icon-color, var(--m3e-toolbar-selected-icon-color, ${A.color.onSecondaryContainer}))`),
		labelColor: c(`var(--m3e-toolbar-standard-label-color, var(--m3e-toolbar-label-color, ${A.color.onSurfaceVariant}))`),
		selectedLabelColor: c(`var(--m3e-toolbar-standard-selected-label-color, var(--m3e-toolbar-selected-label-color, ${A.color.onSecondaryContainer}))`),
		disabled: {
			iconColor: c(`var(--m3e-toolbar-standard-disabled-icon-color, var(--m3e-toolbar-disabled-icon-color, ${A.color.onSurface}))`),
			iconOpacity: c("var(--m3e-toolbar-standard-disabled-icon-opacity, var(--m3e-toolbar-disabled-icon-opacity, 38%))"),
			labelColor: c(`var(--m3e-toolbar-standard-disabled-label-color, var(--m3e-toolbar-disabled-label-color, ${A.color.onSurface}))`),
			labelOpacity: c("var(--m3e-toolbar-standard-disabled-label-opacity, var(--m3e-toolbar-label-icon-opacity, 38%))")
		},
		hover: {
			stateLayerColor: c(`var(--m3e-toolbar-standard-hover-state-layer-color, var(--m3e-toolbar-hover-state-layer-color, ${A.color.onSurfaceVariant}))`),
			selectedStateLayerColor: c(`var(--m3e-toolbar-standard-hover-selected-state-layer-color, var(--m3e-toolbar-hover-selected-state-layer-color, ${A.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-toolbar-standard-hover-state-layer-opacity, var(--m3e-toolbar-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			iconColor: c(`var(--m3e-toolbar-standard-hover-icon-color, var(--m3e-toolbar-hover-icon-color, ${A.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-toolbar-standard-hover-selected-icon-color, var(--m3e-toolbar-hover-selected-icon-color, ${A.color.onSecondaryContainer}))`),
			labelColor: c(`var(--m3e-toolbar-standard-hover-label-color, var(--m3e-toolbar-hover-label-color, ${A.color.onSurfaceVariant}))`),
			selectedLabelColor: c(`var(--m3e-toolbar-standard-hover-selected-label-color, var(--m3e-toolbar-hover-selected-label-color, ${A.color.onSecondaryContainer}))`)
		},
		focus: {
			stateLayerColor: c(`var(--m3e-toolbar-standard-focus-state-layer-color, var(--m3e-toolbar-focus-state-layer-color, ${A.color.onSurfaceVariant}))`),
			selectedStateLayerColor: c(`var(--m3e-toolbar-standard-focus-selected-state-layer-color, var(--m3e-toolbar-focus-selected-state-layer-color, ${A.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-toolbar-standard-focus-state-layer-opacity, var(--m3e-toolbar-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			iconColor: c(`var(--m3e-toolbar-standard-focus-icon-color, var(--m3e-toolbar-focus-icon-color, ${A.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-toolbar-standard-focus-selected-icon-color, var(--m3e-toolbar-focus-selected-icon-color, ${A.color.onSecondaryContainer}))`),
			labelColor: c(`var(--m3e-toolbar-standard-focus-label-color, var(--m3e-toolbar-focus-label-color, ${A.color.onSurfaceVariant}))`),
			selectedLabelColor: c(`var(--m3e-toolbar-standard-focus-selected-label-color, var(--m3e-toolbar-focus-selected-label-color, ${A.color.onSecondaryContainer}))`)
		},
		pressed: {
			stateLayerColor: c(`var(--m3e-toolbar-standard-pressed-state-layer-color, var(--m3e-toolbar-pressed-state-layer-color, ${A.color.onSurfaceVariant}))`),
			selectedStateLayerColor: c(`var(--m3e-toolbar-standard-pressed-selected-state-layer-color, var(--m3e-toolbar-pressed-selected-state-layer-color, ${A.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-toolbar-standard-pressed-state-layer-opacity, var(--m3e-toolbar-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			iconColor: c(`var(--m3e-toolbar-standard-pressed-icon-color, var(--m3e-toolbar-pressed-icon-color, ${A.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-toolbar-standard-pressed-selected-icon-color, var(--m3e-toolbar-pressed-selected-icon-color, ${A.color.onSecondaryContainer}))`),
			labelColor: c(`var(--m3e-toolbar-standard-pressed-label-color, var(--m3e-toolbar-pressed-label-color, ${A.color.onSurfaceVariant}))`),
			selectedLabelColor: c(`var(--m3e-toolbar-standard-pressed-selected-label-color, var(--m3e-toolbar-pressed-selected-label-color, ${A.color.onSecondaryContainer}))`)
		}
	},
	vibrant: {
		containerColor: c(`var(--m3e-toolbar-vibrant-container-color, var(--m3e-toolbar-container-color, ${A.color.primaryContainer}))`),
		buttonContainerColor: c(`var(--m3e-toolbar-vibrant-button-container-color, var(--m3e-toolbar-button-container-color, ${A.color.primaryContainer}))`),
		selectedButtonContainerColor: c(`var(--m3e-toolbar-vibrant-selected-button-container-color, var(--m3e-toolbar-selected-button-container-color, ${A.color.surfaceContainer}))`),
		iconColor: c(`var(--m3e-toolbar-vibrant-icon-color, var(--m3e-toolbar-icon-color, ${A.color.onPrimaryContainer}))`),
		selectedIconColor: c(`var(--m3e-toolbar-vibrant-selected-icon-color, var(--m3e-toolbar-selected-icon-color, ${A.color.onSurface}))`),
		labelColor: c(`var(--m3e-toolbar-vibrant-label-color, var(--m3e-toolbar-label-color, ${A.color.onPrimaryContainer}))`),
		selectedLabelColor: c(`var(--m3e-toolbar-vibrant-selected-label-color, var(--m3e-toolbar-selected-label-color, ${A.color.onSurface}))`),
		disabled: {
			iconColor: c(`var(--m3e-toolbar-vibrant-disabled-icon-color, var(--m3e-toolbar-disabled-icon-color, ${A.color.onSurface}))`),
			iconOpacity: c("var(--m3e-toolbar-vibrant-disabled-icon-opacity, var(--m3e-toolbar-disabled-icon-opacity, 38%))"),
			labelColor: c(`var(--m3e-toolbar-vibrant-disabled-label-color, var(--m3e-toolbar-disabled-label-color, ${A.color.onSurface}))`),
			labelOpacity: c("var(--m3e-toolbar-vibrant-disabled-label-opacity, var(--m3e-toolbar-label-icon-opacity, 38%))")
		},
		hover: {
			stateLayerColor: c(`var(--m3e-toolbar-vibrant-hover-state-layer-color, var(--m3e-toolbar-hover-state-layer-color, ${A.color.onPrimaryContainer}))`),
			selectedStateLayerColor: c(`var(--m3e-toolbar-vibrant-hover-selected-state-layer-color, var(--m3e-toolbar-hover-selected-state-layer-color, ${A.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-toolbar-vibrant-hover-state-layer-opacity, var(--m3e-toolbar-hover-state-layer-opacity, ${A.state.hoverStateLayerOpacity}))`),
			iconColor: c(`var(--m3e-toolbar-vibrant-hover-icon-color, var(--m3e-toolbar-hover-icon-color, ${A.color.onPrimaryContainer}))`),
			selectedIconColor: c(`var(--m3e-toolbar-vibrant-hover-selected-icon-color, var(--m3e-toolbar-hover-selected-icon-color, ${A.color.onSurface}))`),
			labelColor: c(`var(--m3e-toolbar-vibrant-hover-label-color, var(--m3e-toolbar-hover-label-color, ${A.color.onPrimaryContainer}))`),
			selectedLabelColor: c(`var(--m3e-toolbar-vibrant-hover-selected-label-color, var(--m3e-toolbar-hover-selected-label-color, ${A.color.onSurface}))`)
		},
		focus: {
			stateLayerColor: c(`var(--m3e-toolbar-vibrant-focus-state-layer-color, var(--m3e-toolbar-focus-state-layer-color, ${A.color.onPrimaryContainer}))`),
			selectedStateLayerColor: c(`var(--m3e-toolbar-vibrant-focus-selected-state-layer-color, var(--m3e-toolbar-focus-selected-state-layer-color, ${A.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-toolbar-vibrant-focus-state-layer-opacity, var(--m3e-toolbar-focus-state-layer-opacity, ${A.state.focusStateLayerOpacity}))`),
			iconColor: c(`var(--m3e-toolbar-vibrant-focus-icon-color, var(--m3e-toolbar-focus-icon-color, ${A.color.onPrimaryContainer}))`),
			selectedIconColor: c(`var(--m3e-toolbar-vibrant-focus-selected-icon-color, var(--m3e-toolbar-focus-selected-icon-color, ${A.color.onSurface}))`),
			labelColor: c(`var(--m3e-toolbar-vibrant-focus-label-color, var(--m3e-toolbar-focus-label-color, ${A.color.onPrimaryContainer}))`),
			selectedLabelColor: c(`var(--m3e-toolbar-vibrant-focus-selected-label-color, var(--m3e-toolbar-focus-selected-label-color, ${A.color.onSurface}))`)
		},
		pressed: {
			stateLayerColor: c(`var(--m3e-toolbar-vibrant-pressed-state-layer-color, var(--m3e-toolbar-pressed-state-layer-color, ${A.color.onPrimaryContainer}))`),
			selectedStateLayerColor: c(`var(--m3e-toolbar-vibrant-pressed-selected-state-layer-color, var(--m3e-toolbar-pressed-selected-state-layer-color, ${A.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-toolbar-vibrant-pressed-state-layer-opacity, var(--m3e-toolbar-pressed-state-layer-opacity, ${A.state.pressedStateLayerOpacity}))`),
			iconColor: c(`var(--m3e-toolbar-vibrant-pressed-icon-color, var(--m3e-toolbar-pressed-icon-color, ${A.color.onPrimaryContainer}))`),
			selectedIconColor: c(`var(--m3e-toolbar-vibrant-pressed-selected-icon-color, var(--m3e-toolbar-pressed-selected-icon-color, ${A.color.onSurface}))`),
			labelColor: c(`var(--m3e-toolbar-vibrant-pressed-label-color, var(--m3e-toolbar-pressed-label-color, ${A.color.onPrimaryContainer}))`),
			selectedLabelColor: c(`var(--m3e-toolbar-vibrant-pressed-selected-label-color, var(--m3e-toolbar-pressed-selected-label-color, ${A.color.onSurface}))`)
		}
	}
};
function _g(e, t, n) {
	return c(`
    ${t}-${n}-state-layer-color: ${K[e].hover.stateLayerColor};
    ${t}-${n}-unselected-state-layer-color: ${K[e].hover.stateLayerColor};
    ${t}-${n}-selected-state-layer-color: ${K[e].hover.selectedStateLayerColor};
    ${t}-${n}-state-layer-opacity: ${K[e].hover.stateLayerOpacity};
    ${t}-${n}-icon-color: ${K[e].hover.iconColor};
    ${t}-${n}-unselected-icon-color: ${K[e].hover.iconColor};
    ${t}-${n}-selected-icon-color: ${K[e].hover.selectedIconColor};`);
}
function vg(e) {
	let t = "--m3e-standard-icon-button";
	return c(`
      ${t}-container-color: ${K[e].buttonContainerColor};
      ${t}-unselected-container-color: ${K[e].buttonContainerColor};
      ${t}-selected-container-color: ${K[e].selectedButtonContainerColor};
      ${t}-icon-color: ${K[e].iconColor};
      ${t}-unselected-icon-color: ${K[e].iconColor};
      ${t}-selected-icon-color: ${K[e].selectedIconColor};
      ${t}-disabled-icon-color: ${K[e].disabled.iconColor};
      ${t}-disabled-icon-opacity: ${K[e].disabled.iconOpacity};
      ${t}-disabled-container-color: transparent;
      ${_g(e, t, "hover")}
      ${_g(e, t, "focus")}
      ${_g(e, t, "pressed")}
  `);
}
function yg(e, t, n) {
	return c(`
    ${t}-${n}-state-layer-color: ${K[e].hover.stateLayerColor};
    ${t}-${n}-unselected-state-layer-color: ${K[e].hover.stateLayerColor};
    ${t}-${n}-selected-state-layer-color: ${K[e].hover.selectedStateLayerColor};
    ${t}-${n}-state-layer-opacity: ${K[e].hover.stateLayerOpacity};
    ${t}-${n}-icon-color: ${K[e].hover.iconColor};
    ${t}-${n}-unselected-icon-color: ${K[e].hover.iconColor};
    ${t}-${n}-selected-icon-color: ${K[e].hover.selectedIconColor};
    ${t}-${n}-label-text-color: ${K[e].hover.labelColor};
    ${t}-${n}-unselected-label-text-color: ${K[e].hover.labelColor};
    ${t}-${n}-selected-label-text-color: ${K[e].hover.selectedLabelColor};`);
}
function bg(e) {
	let t = "--m3e-text-button";
	return c(`
      ${t}-container-color: ${K[e].buttonContainerColor};
      ${t}-unselected-container-color: ${K[e].buttonContainerColor};
      ${t}-selected-container-color: ${K[e].selectedButtonContainerColor};
      ${t}-icon-color: ${K[e].iconColor};
      ${t}-unselected-icon-color: ${K[e].iconColor};
      ${t}-selected-icon-color: ${K[e].selectedIconColor};
      ${t}-label-text-color: ${K[e].labelColor};
      ${t}-unselected-label-text-color: ${K[e].labelColor};
      ${t}-selected-label-text-color: ${K[e].selectedLabelColor};
      ${t}-disabled-icon-color: ${K[e].disabled.iconColor};
      ${t}-disabled-icon-opacity: ${K[e].disabled.iconOpacity};
      ${t}-disabled-label-text-color: ${K[e].disabled.labelColor};
      ${t}-disabled-label-text-opacity: ${K[e].disabled.labelOpacity};
      ${t}-disabled-container-color: transparent;
      ${yg(e, t, "hover")}
      ${yg(e, t, "focus")}
      ${yg(e, t, "pressed")}
  `);
}
function xg(e) {
	let t = c(`:is(:state(--${e}), :--${e})`);
	return c(`
    :host(${t}) .state-layer {
      background-color: ${K[e].containerColor};
    }
    :host(${t}) .base {
      color: ${K[e].labelColor};
      ${vg(e)}
      ${bg(e)}
    }
  `);
}
var Sg = [xg("standard"), xg("vibrant")], Cg, wg, Tg, Eg, Dg, Og, kg, Ag, jg = class extends aa(j(T(x), "toolbar")) {
	constructor() {
		super(...arguments), Cg.add(this), wg.set(this, void 0), Tg.set(this, new Mu().withHomeAndEnd().withDirectionality(Sd.current)), this.variant = "standard", this.shape = "square", this.elevated = !1;
	}
	connectedCallback() {
		super.connectedCallback(), t(this, Cg, "m", Dg).call(this), t(this, Cg, "m", Eg).call(this), n(this, wg, Sd.observe(() => t(this, Tg, "f").directionality = Sd.current), "f");
	}
	disconnectedCallback() {
		super.disconnectedCallback(), t(this, wg, "f")?.call(this);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("shape") && t(this, Cg, "m", Eg).call(this), e.has("variant") && t(this, Cg, "m", Dg).call(this);
	}
	update(e) {
		super.update(e), e.has("vertical") && (t(this, Tg, "f").vertical = this.vertical);
	}
	render() {
		return y`<m3e-state-layer class="state-layer"></m3e-state-layer><m3e-elevation class="elevation" level="${this.elevated ? 3 : 0}"></m3e-elevation><div class="base"><slot @click="${t(this, Cg, "m", Ag)}" @keydown="${t(this, Cg, "m", kg)}" @slotchange="${t(this, Cg, "m", Og)}"></slot></div>`;
	}
};
wg = /* @__PURE__ */ new WeakMap(), Tg = /* @__PURE__ */ new WeakMap(), Cg = /* @__PURE__ */ new WeakSet(), Eg = function() {
	dg(this.shape) || (this.shape = "square"), Br(this, this.shape, "rounded", "square");
}, Dg = function() {
	ug(this.variant) || (this.variant = "standard"), Br(this, this.variant, "standard", "vibrant");
}, Og = function() {
	let e = ed.findInteractiveElements(this, !0), { added: n } = t(this, Tg, "f").setItems(e);
	if (!t(this, Tg, "f").activeItem) {
		let e = n.find((e) => !e.hasAttribute("disabled"));
		e && t(this, Tg, "f").updateActiveItem(e);
	}
}, kg = function(e) {
	t(this, Tg, "f").onKeyDown(e);
}, Ag = function(e) {
	let n = e.composedPath().find((e) => e instanceof HTMLElement && t(this, Tg, "f").items.includes(e));
	n && t(this, Tg, "f").updateActiveItem(n);
}, jg.styles = [
	gg,
	mg,
	Sg
], e([S({
	reflect: !0,
	useDefault: !0
})], jg.prototype, "variant", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], jg.prototype, "shape", void 0), e([S({
	type: Boolean,
	reflect: !0
})], jg.prototype, "elevated", void 0), jg = e([w("m3e-toolbar")], jg);
//#endregion
//#region node_modules/@m3e/web/dist/core-layout.js
var Mg;
(function(e) {
	e.XSmall = "(max-width: 599.98px)", e.Small = "(min-width: 600px) and (max-width: 959.98px)", e.Medium = "(min-width: 960px) and (max-width: 1279.98px)", e.Large = "(min-width: 1280px) and (max-width: 1919.98px)", e.XLarge = "(min-width: 1920px)", e.Handset = "(max-width: 599.98px) and (orientation: portrait), (max-width: 959.98px) and (orientation: landscape)", e.Tablet = "(min-width: 600px) and (max-width: 839.98px) and (orientation: portrait), (min-width: 960px) and (max-width: 1279.98px) and (orientation: landscape)", e.Web = "(min-width: 840px) and (orientation: portrait), (min-width: 1280px) and (orientation: landscape)", e.HandsetPortrait = "(max-width: 599.98px) and (orientation: portrait)", e.TabletPortrait = "(min-width: 600px) and (max-width: 839.98px) and (orientation: portrait)", e.WebPortrait = "(min-width: 840px) and (orientation: portrait)", e.HandsetLandscape = "(max-width: 959.98px) and (orientation: landscape)", e.TabletLandscape = "(min-width: 960px) and (max-width: 1279.98px) and (orientation: landscape)", e.WebLandscape = "(min-width: 1280px) and (orientation: landscape)";
})(Mg ||= {});
var Ng = class {
	static observe(e, t) {
		if (e.length == 0) return () => {};
		let n = e.map((e) => matchMedia(e)), r = () => {
			let r = /* @__PURE__ */ new Map();
			e.forEach((e, t) => r.set(e, n[t].matches)), t(r);
		};
		return n.forEach((e) => e.addEventListener("change", r)), r(), () => {
			n.forEach((e) => e.removeEventListener("change", r)), n.length = 0;
		};
	}
};
globalThis.M3eBreakpointObserver = Ng;
//#endregion
//#region node_modules/@m3e/web/dist/nav-bar.js
function Pg(e) {
	return e === "compact" || e === "expanded" || e === "auto";
}
var Fg, Ig, Lg, Rg, zg, Bg, Vg, Hg = class extends ea(T(j(x, "navigation"))) {
	constructor() {
		super(...arguments), Fg.add(this), this[Vg] = new Bu().disableRovingTabIndex(), Ig.set(this, void 0), this.mode = "compact";
	}
	get items() {
		return this[W].items;
	}
	get selected() {
		return this[W].selectedItems[0] ?? null;
	}
	get currentMode() {
		return this._mode ?? (this.mode === "compact" ? "compact" : "expanded");
	}
	set currentMode(e) {
		this._mode = e;
	}
	connectedCallback() {
		super.connectedCallback(), t(this, Fg, "m", Lg).call(this);
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this._mode = void 0, t(this, Ig, "f")?.call(this);
	}
	reconnectedCallback() {
		super.reconnectedCallback(), this.mode === "auto" && t(this, Fg, "m", Rg).call(this);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("mode") && (t(this, Fg, "m", Lg).call(this), t(this, Ig, "f")?.call(this), this.mode === "auto" ? t(this, Fg, "m", Rg).call(this) : (this._mode = void 0, this._updateItems())), e.has("_mode") && this._updateItems();
	}
	firstUpdated(e) {
		super.firstUpdated(e), e.has("mode") || this._updateItems();
	}
	render() {
		return y`<div class="base"><slot @change="${t(this, Fg, "m", Bg)}" @slotchange="${t(this, Fg, "m", zg)}"></slot></div>`;
	}
	_updateItems() {
		let e = this.currentMode === "compact" ? "vertical" : "horizontal";
		this._updateOrientation(e), D(this, "--compact", e === "vertical");
	}
	_updateOrientation(e) {
		this[W].items.forEach((t) => t.orientation = e);
	}
};
Ig = /* @__PURE__ */ new WeakMap(), Fg = /* @__PURE__ */ new WeakSet(), Vg = W, Lg = function() {
	Pg(this.mode) || (this.mode = "compact");
}, Rg = function() {
	n(this, Ig, Ng.observe([Mg.XSmall, Mg.Small], (e) => {
		this._mode = e.get(Mg.XSmall) || e.get(Mg.Small) ? "compact" : "expanded", this._updateItems();
	}), "f");
}, zg = function() {
	this[W].setItems([...this.querySelectorAll("m3e-nav-item")]), this._updateItems();
}, Bg = function(e) {
	e.stopPropagation(), this.dispatchEvent(new Event("change", { bubbles: !0 }));
}, Hg.styles = l`:host { display: block; overflow-x: auto; overflow-y: hidden; scrollbar-width: ${A.scrollbar.thinWidth}; scrollbar-color: ${A.scrollbar.color}; min-height: var(--m3e-nav-bar-height, 64px); } :host([hidden]) { display: none; } .base { contain: layout style; display: flex; align-items: stretch; justify-content: center; box-sizing: border-box; min-height: inherit; height: inherit; width: 100%; background-color: var(--m3e-nav-bar-container-color, ${A.color.surfaceContainer}); --_vertical-nav-item-min-width: var(--m3e-nav-bar-vertical-item-width, 112px); --_horizontal-nav-item-leading-space: var( --m3e-nav-bar-horizontal-nav-item-leading-space, ${A.measurement.space200} ); --_horizontal-nav-item-trailing-space: var( --m3e-nav-bar-horizontal-nav-item-trailing-space, ${A.measurement.space200} ); }`, e([Ze()], Hg.prototype, "_mode", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], Hg.prototype, "mode", void 0), Hg = e([w("m3e-nav-bar")], Hg);
function Ug(e) {
	return e === "vertical" || e === "horizontal";
}
var Wg, Gg, Kg, qg, Jg, Yg, Xg, Zg, Qg, $g = class extends Cd(ea(ia(Zi(Kr(qi(Vi(Ci(vi(T(j(x, "button"), !0)))))))))) {
	constructor() {
		super(...arguments), Wg.add(this), Gg.set(this, !1), Kg.set(this, (e) => t(this, Wg, "m", Yg).call(this, e)), qg.set(this, new cr(this, {
			target: null,
			callback: (e) => t(this, Wg, "m", Zg).call(this, e)
		})), this.orientation = "vertical";
	}
	get navBar() {
		return this.closest("m3e-nav-bar") ?? this.closest("m3e-nav-rail") ?? null;
	}
	connectedCallback() {
		n(this, Gg, this.closest("m3e-nav-rail") !== null, "f"), super.connectedCallback(), t(this, Wg, "m", Jg).call(this), this.addEventListener("click", t(this, Kg, "f"), { capture: !0 });
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("click", t(this, Kg, "f"), { capture: !0 }), n(this, Gg, !1, "f");
	}
	reconnectedCallback() {
		super.reconnectedCallback(), t(this, Wg, "m", Qg).call(this);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("orientation") && t(this, Wg, "m", Jg).call(this);
	}
	update(e) {
		if (super.update(e), e.has("selected")) {
			this.ariaSelected = null, this.ariaPressed = null, this.ariaCurrent = `${this.selected}`;
			for (let e of this.querySelectorAll("m3e-icon")) e.toggleAttribute("filled", this.selected);
			this.navBar?.[W].notifySelectionChange(this);
		}
	}
	updated(e) {
		super.updated(e), e.has("orientation") && this._focusRing?.attach(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), [this._focusRing, this._indicator].forEach((e) => e?.attach(this)), t(this, Wg, "m", Qg).call(this);
	}
	render() {
		let e = this.disabled || this.disabledInteractive, n = y`<div class="label"><slot></slot></div>`;
		return y`${this.orientation === "vertical" ? y`<m3e-focus-ring class="focus-ring" inward></m3e-focus-ring>` : b}<div class="outer">${this[Ji]()}<div class="inner">${this.orientation === "horizontal" ? y`<m3e-focus-ring class="focus-ring"></m3e-focus-ring>` : b}<m3e-selection-indicator class="indicator" bounce ?centered="${this.orientation === "vertical"}" ?selected="${this.selected}" ?disabled="${e}"></m3e-selection-indicator><div class="touch" aria-hidden="true"></div><div class="base"><div class="icon-wrapper" aria-hidden="true"><div class="icon"><slot name="icon"></slot><slot name="selected-icon" @slotchange="${t(this, Wg, "m", Xg)}"></slot></div></div><m3e-collapsible orientation="horizontal" ?no-animate="${!t(this, Gg, "f")}" ?open="${this.orientation === "horizontal"}">${this.orientation === "horizontal" ? n : b}</m3e-collapsible>${this.orientation === "horizontal" ? b : n}</div></div></div>`;
	}
};
Gg = /* @__PURE__ */ new WeakMap(), Kg = /* @__PURE__ */ new WeakMap(), qg = /* @__PURE__ */ new WeakMap(), Wg = /* @__PURE__ */ new WeakSet(), Jg = function() {
	Ug(this.orientation) || (this.orientation = "vertical"), Br(this, this.orientation, "horizontal", "vertical");
}, Yg = function(e) {
	e.defaultPrevented || this.dispatchEvent(new Event("beforeinput", {
		bubbles: !0,
		cancelable: !0
	})) && (this.selected = !0, this.navBar?.[W].notifySelectionChange(this), this.dispatchEvent(new Event("input", { bubbles: !0 })), this.dispatchEvent(new Event("change", { bubbles: !0 })));
}, Xg = function(e) {
	D(this, "--with-selected-icon", Rt(e.target));
}, Zg = function(e) {
	e.length !== 0 && this.orientation !== "vertical" && this._inner?.style.setProperty("--_expanded-width", `${e[0].contentRect.width}px`);
}, Qg = function() {
	this._indicator && t(this, Gg, "f") && t(this, qg, "f").observe(this._indicator);
}, $g.styles = l`:host { display: inline-block; vertical-align: middle; position: relative; outline: none; user-select: none; flex: 1; font-size: var(--m3e-nav-item-label-text-font-size, ${A.typescale.standard.label.medium.fontSize}); font-weight: var( --m3e-nav-item-label-text-font-weight, ${A.typescale.standard.label.medium.fontWeight} ); line-height: var( --m3e-nav-item-label-text-line-height, ${A.typescale.standard.label.medium.lineHeight} ); letter-spacing: var(--m3e-nav-item-label-text-tracking, ${A.typescale.standard.label.medium.tracking}); border-radius: var(--m3e-nav-item-shape, ${A.shape.corner.full}); align-self: var(--_nav-item-align-self); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } :host([hidden]) { display: none; } :host(:is(:state(--vertical), :--vertical)) { min-width: var(--_vertical-nav-item-min-width); width: var(--_vertical-nav-item-width); margin-inline: var(--_vertical-nav-item-margin-inline); } :host(:is(:state(--horizontal), :--horizontal)) { max-width: fit-content; } :host(:is(:state(--horizontal), :--horizontal)) .outer { margin-inline-start: var(--_horizontal-nav-item-leading-space); margin-inline-end: var(--_horizontal-nav-item-trailing-space); } :host(:not(:disabled):not([disabled-interactive])) { cursor: pointer; } :host([disabled-interactive]) { cursor: not-allowed; } .outer { height: 100%; } .outer, .inner { display: flex; align-items: center; justify-content: var(--_nav-item-justify-content, center); position: relative; border-radius: inherit; } .icon-wrapper { position: relative; flex: none; line-height: 0; } .base { contain: layout style; justify-content: unset; box-sizing: border-box; vertical-align: middle; display: inline-flex; align-items: center; justify-content: center; position: relative; width: 100%; } :host(:not(:is(:state(--no-animate), :--no-animate))) .base { transition: ${c(`margin-top ${A.motion.duration.short1} ${A.motion.easing.standard}`)}; } .icon { position: absolute; } .label { vertical-align: middle; } * { user-select: none; -webkit-user-select: none; } :host(:is(:state(--horizontal), :--horizontal)) .label { white-space: nowrap; } ::slotted([slot="icon"]), ::slotted([slot="selected-icon"]) { width: 1em; font-size: var(--m3e-nav-item-icon-size, 24px) !important; } :host(:not(:is(:state(--no-animate), :--no-animate))) .indicator { transition: ${c(`height ${A.motion.duration.short1} ${A.motion.easing.standard}`)}; } :host(:not([selected])) slot[name="selected-icon"], :host(:not(:is(:state(--with-selected-icon), :--with-selected-icon))) slot[name="selected-icon"], :host([selected]:is(:state(--with-selected-icon), :--with-selected-icon)) slot[name="icon"] { display: none; } .indicator { --m3e-selection-indicator-color: var( --m3e-nav-item-active-container-color, ${A.color.secondaryContainer} ); } :host(:not([selected]):not(:disabled):not([disabled-interactive])) .indicator { --m3e-selection-indicator-state-layer-hover-color: var( --m3e-nav-item-inactive-hover-state-layer-color, ${A.color.onSecondaryContainer} ); --m3e-selection-indicator-state-layer-focus-color: var( --m3e-nav-item-inactive-focus-state-layer-color, ${A.color.onSecondaryContainer} ); --m3e-selection-indicator-state-layer-pressed-color: var( --m3e-nav-item-inactive-pressed-state-layer-color, ${A.color.onSecondaryContainer} ); } :host(:not([selected]):not(:disabled):not([disabled-interactive])) .label { color: var(--m3e-nav-item-inactive-label-text-color, ${A.color.onSurfaceVariant}); } :host(:not([selected]):not(:disabled):not([disabled-interactive])) .icon { color: var(--m3e-nav-item-inactive-icon-color, ${A.color.onSurfaceVariant}); } :host([selected]:not(:disabled):not([disabled-interactive])) .indicator { --m3e-selection-indicator-state-layer-hover-color: var( --m3e-nav-item-active-hover-state-layer-color, ${A.color.onSecondaryContainer} ); --m3e-selection-indicator-state-layer-focus-color: var( --m3e-nav-item-active-focus-state-layer-color, ${A.color.onSecondaryContainer} ); --m3e-selection-indicator-state-layer-pressed-color: var( --m3e-nav-item-active-pressed-state-layer-color, ${A.color.onSecondaryContainer} ); } :host([selected]:not(:disabled):not([disabled-interactive]):not(:is(:state(--horizontal), :--horizontal))) .label { color: var(--m3e-nav-item-active-label-text-color, ${A.color.secondary}); } :host([selected]:not(:disabled):not([disabled-interactive]):is(:state(--horizontal), :--horizontal)) .label, :host([selected]:not(:disabled):not([disabled-interactive])) .icon { color: var(--m3e-nav-item-active-icon-color, ${A.color.onSecondaryContainer}); } :host(:is(:state(--vertical), :--vertical)) .outer { align-self: stretch; align-items: flex-start; } :host(:is(:state(--vertical), :--vertical)) .label { text-align: center; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; line-clamp: 2; } :host(:is(:state(--vertical), :--vertical)) .base { flex-direction: column; row-gap: var(--m3e-nav-item-spacing, ${A.measurement.space50}); } :host(:is(:state(--horizontal), :--horizontal)) .base { margin-top: 0; } :host(:is(:state(--vertical), :--vertical)) .base { margin-top: var(--m3e-vertical-nav-item-active-indicator-margin, ${A.measurement.space75}); margin-bottom: var(--m3e-vertical-nav-item-active-indicator-margin, ${A.measurement.space75}); } :host(:is(:state(--vertical), :--vertical)) .indicator { top: var(--m3e-vertical-nav-item-active-indicator-margin, ${A.measurement.space75}); bottom: unset; } :host(:is(:state(--vertical), :--vertical):is(:state(--rtl), :--rtl)) .indicator { right: var(--_vertical-nav-item-inset-start); left: unset; } :host(:is(:state(--vertical), :--vertical):not(:is(:state(--rtl), :--rtl))) .indicator { left: var(--_vertical-nav-item-inset-start); right: unset; } :host(:is(:state(--vertical), :--vertical)) .indicator, :host(:is(:state(--vertical), :--vertical)) .icon-wrapper { min-width: var(--m3e-vertical-nav-item-active-indicator-width, 56px); } :host(:not(:is(:state(--no-animate), :--no-animate)):is(:state(--vertical), :--vertical)) .state-layer { ${c(`collapse ${A.motion.duration.medium1}, indicator-grow-bounce ${A.motion.duration.medium2} ${A.motion.easing.standardAccelerate}`)}; } :host(:not(:is(:state(--no-animate), :--no-animate)):is(:state(--vertical), :--vertical)) .indicator { animation: collapse ${A.motion.duration.medium1}; } @keyframes collapse { from { width: var(--_expanded-width, var(--m3e-vertical-nav-item-active-indicator-width, 56px)); } to { width: var(--m3e-vertical-nav-item-active-indicator-width, 56px); } } :host(:is(:state(--vertical), :--vertical)) .indicator, :host(:is(:state(--vertical), :--vertical)) .icon-wrapper { height: var(--m3e-vertical-nav-item-active-indicator-height, 32px); } :host(:is(:state(--vertical), :--vertical)) .icon { top: calc( calc(var(--m3e-vertical-nav-item-active-indicator-height, 32px) / 2) - calc( var(--m3e-nav-item-icon-size, 24px) / 2 ) ); left: calc( calc(var(--m3e-vertical-nav-item-active-indicator-width, 56px) / 2) - calc( var(--m3e-nav-item-icon-size, 24px) / 2 ) ); } :host(:is(:state(--vertical), :--vertical)) .focus-ring { border-radius: var(--m3e-nav-item-focus-ring-shape, ${A.shape.corner.medium}); } :host(:is(:state(--horizontal), :--horizontal)) .icon-wrapper { width: var(--m3e-nav-item-icon-size, 24px); height: var(--m3e-nav-item-icon-size, 24px); } :host(:is(:state(--horizontal), :--horizontal)) .base { padding: var(--m3e-horizontal-nav-item-padding, ${A.measurement.space200}); } :host(:is(:state(--horizontal), :--horizontal)) .label { flex: 1 1 auto; } :host(:is(:state(--horizontal), :--horizontal)) .base { column-gap: var(--m3e-nav-item-spacing, ${A.measurement.space50}); } :host(:is(:state(--horizontal), :--horizontal)) .indicator, :host(:is(:state(--horizontal), :--horizontal)) .inner { height: var(--m3e-horizontal-nav-item-active-indicator-height, 40px); } :host(:is(:state(--horizontal), :--horizontal)) .inner { width: fit-content; } .indicator { margin-inline: auto; } :host( :is(:state(--first), :--first):not(:is(:state(--no-animate), :--no-animate)):is( :state(--horizontal), :--horizontal ) ) .icon-wrapper, :host( :not(:is(:state(--first), :--first)):not(:is(:state(--no-animate), :--no-animate)):is( :state(--vertical), :--vertical ) ) .icon-wrapper { animation: ${c(`slide-down ${A.motion.duration.short2} ${A.motion.easing.standard}`)}; } :host( :not(:is(:state(--first), :--first)):not(:is(:state(--no-animate), :--no-animate)):is( :state(--horizontal), :--horizontal ) ) .icon-wrapper, :host( :is(:state(--first), :--first):not(:is(:state(--no-animate), :--no-animate)):is(:state(--vertical), :--vertical) ) .icon-wrapper { animation: ${c(`slide-up ${A.motion.duration.short2} ${A.motion.easing.standard}`)}; } @keyframes slide-down { from { transform: translateY(-4px); } to { transform: translateY(0); } } @keyframes slide-up { from { transform: translateY(4px); } to { transform: translateY(0); } } :host(:not(:is(:state(--no-animate), :--no-animate)):is(:state(--horizontal), :--horizontal)) .label { animation: horizontal-fade-in ${A.motion.duration.medium1}; } :host(:not(:is(:state(--no-animate), :--no-animate)):is(:state(--vertical), :--vertical)) .label { animation: vertical-fade-in ${A.motion.duration.medium1}; } @keyframes horizontal-fade-in { from { opacity: 0; } to { opacity: 1; } } @keyframes vertical-fade-in { from { opacity: 0; } to { opacity: 1; } } :host(:disabled) .label, :host([disabled-interactive]) .label { color: color-mix( in srgb, var(--m3e-nav-item-disabled-label-text-color, ${A.color.onSurface}) var(--m3e-nav-item-disabled-label-text-opacity, 38%), transparent ); } :host(:disabled) .icon, :host([disabled-interactive]) .icon { color: color-mix( in srgb, var(--m3e-nav-item-disabled-icon-color, ${A.color.onSurface}) var(--m3e-nav-item-disabled-icon-opacity, 38%), transparent ); } a { all: unset; display: block; position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px; z-index: 1; } @media (prefers-reduced-motion) { :host( :is(:state(--first), :--first):not(:is(:state(--no-animate), :--no-animate)):is( :state(--horizontal), :--horizontal ) ) .icon-wrapper, :host( :not(:is(:state(--first), :--first)):not(:is(:state(--no-animate), :--no-animate)):is( :state(--vertical), :--vertical ) ) .icon-wrapper, :host( :not(:is(:state(--first), :--first)):not(:is(:state(--no-animate), :--no-animate)):is( :state(--horizontal), :--horizontal ) ) .icon-wrapper, :host( :is(:state(--first), :--first):not(:is(:state(--no-animate), :--no-animate)):is( :state(--vertical), :--vertical ) ) .icon-wrapper, :host(:not(:is(:state(--no-animate), :--no-animate)):is(:state(--vertical), :--vertical)) .state-layer, :host(:not(:is(:state(--no-animate), :--no-animate)):is(:state(--vertical), :--vertical)) .indicator, :host(:not(:is(:state(--no-animate), :--no-animate)):is(:state(--horizontal), :--horizontal)) .label, :host(:not(:is(:state(--no-animate), :--no-animate)):is(:state(--vertical), :--vertical)) .label { animation: none; } :host(:not(:is(:state(--no-animate), :--no-animate))) .state-layer, :host(:not(:is(:state(--no-animate), :--no-animate))) .indicator, :host(:not(:is(:state(--no-animate), :--no-animate))) .base { transition: none; } } @media (forced-colors: active) { :host(:disabled) .label, :host([disabled-interactive]) .label, :host(:disabled) .icon, :host([disabled-interactive]) .icon { color: GrayText; } :host(:not([selected]):not(:disabled):not([disabled-interactive])) .label, :host(:not([selected]):not(:disabled):not([disabled-interactive])) .icon { color: ButtonText; } :host([selected]:not(:disabled):not([disabled-interactive])) .indicator { --m3e-selection-indicator-color: ButtonText; } :host(:is(:state(--vertical), :--vertical)[selected]:not(:disabled):not([disabled-interactive])) .label { color: ButtonText; } :host(:is(:state(--horizontal), :--horizontal)[selected]:not(:disabled):not([disabled-interactive])) .label, :host([selected]:not(:disabled):not([disabled-interactive])) .icon { forced-color-adjust: none; color: ButtonFace; } }`, e([C(".focus-ring")], $g.prototype, "_focusRing", void 0), e([C(".indicator")], $g.prototype, "_indicator", void 0), e([C(".inner")], $g.prototype, "_inner", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], $g.prototype, "orientation", void 0), $g = e([w("m3e-nav-item")], $g);
//#endregion
//#region node_modules/@m3e/web/dist/nav-rail.js
var e_, t_, n_, r_, i_ = class extends ia(Hg) {
	constructor() {
		super(), e_.add(this), t_.set(this, new Mu().withHomeAndEnd().withWrap()), n_.set(this, (e) => t(this, e_, "m", r_).call(this, e)), this[W].onSelectedItemsChange(() => {
			t(this, t_, "f").updateActiveItem(this[W].activeItem);
		});
	}
	connectedCallback() {
		super.connectedCallback(), this.addEventListener("keydown", t(this, n_, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("keydown", t(this, n_, "f"));
	}
	_updateItems() {
		let e = ed.findInteractiveElements(this, !0), { added: n } = t(this, t_, "f").setItems(e);
		if (!t(this, t_, "f").activeItem) {
			let e = n.find((e) => !e.hasAttribute("disabled"));
			e && t(this, t_, "f").updateActiveItem(e);
		}
		this.items.forEach((e, t) => D(e, "--first", t === 0)), super._updateItems();
	}
	_updateOrientation(e) {
		super._updateOrientation(e), this.querySelectorAll("m3e-fab").forEach((t) => t.toggleAttribute("extended", e === "horizontal"));
	}
};
t_ = /* @__PURE__ */ new WeakMap(), n_ = /* @__PURE__ */ new WeakMap(), e_ = /* @__PURE__ */ new WeakSet(), r_ = function(e) {
	t(this, t_, "f").onKeyDown(e);
}, Vt(l`m3e-nav-rail > m3e-icon-button, m3e-nav-rail > m3e-fab { margin-block-end: var(--m3e-nav-rail-button-item-space, ${A.measurement.space200}); } m3e-nav-rail > m3e-icon-button { margin-inline-start: var(--m3e-nav-rail-icon-button-inset, ${A.measurement.space100}); }`), i_.styles = l`:host { display: block; overflow-x: hidden; overflow-y: auto; scrollbar-width: ${A.scrollbar.thinWidth}; scrollbar-color: ${A.scrollbar.color}; } :host([hidden]) { display: none; } :host(:not(:is(:state(--no-animate), :--no-animate))) { transition: ${c(`width ${A.motion.spring.defaultSpatial}`)}; } .base { contain: layout style; display: flex; width: inherit; flex-direction: column; align-items: flex-start; box-sizing: border-box; padding-block-start: var(--m3e-nav-rail-top-space, ${A.measurement.space550}); padding-block-end: var(--m3e-nav-rail-bottom-space, ${A.measurement.space100}); padding-inline: var(--m3e-nav-rail-inline-padding, ${A.measurement.space250}); } :host(:is(:state(--compact), :--compact)) { width: var(--m3e-nav-rail-compact-width, 96px); } :host(:is(:state(--compact), :--compact)) .base { --_vertical-nav-item-width: var(--m3e-nav-rail-compact-width, 96px); --_vertical-nav-item-margin-inline: calc( 0px - var(--m3e-nav-rail-inline-padding, ${A.measurement.space250}) ); --_vertical-nav-item-inset-start: calc(50% - calc(var(--m3e-vertical-nav-item-active-indicator-width, 56px)) / 2); } :host(:not(:is(:state(--compact), :--compact))) { width: var(--m3e-nav-rail-expanded-width, 220px); } :host(:not(:is(:state(--compact), :--compact))) { --m3e-horizontal-nav-item-active-indicator-height: var(--m3e-nav-rail-expanded-item-height, 56px); --_nav-item-align-self: stretch; --_nav-item-justify-content: flex-start; } ::slotted(*) { flex: none; } ::slotted(m3e-fab) { --m3e-fab-container-elevation: ${A.elevation.level0}; --m3e-fab-disabled-container-elevation: ${A.elevation.level0}; --m3e-fab-focus-container-elevation: ${A.elevation.level0}; --m3e-fab-pressed-container-elevation: ${A.elevation.level0}; --m3e-fab-hover-container-elevation: ${A.elevation.level1}; --m3e-fab-lowered-container-elevation: ${A.elevation.level0}; --m3e-fab-lowered-disabled-container-elevation: ${A.elevation.level0}; --m3e-fab-lowered-focus-container-elevation: ${A.elevation.level0}; --m3e-fab-lowered-pressed-container-elevation: ${A.elevation.level0}; --m3e-fab-lowered-hover-container-elevation: ${A.elevation.level1}; } @media (prefers-reduced-motion) { :host(:not(:is(:state(--no-animate), :--no-animate))) { transition: none; } }`, i_ = e([w("m3e-nav-rail")], i_);
var a_, o_, s_, c_ = class extends Ki(sa) {
	constructor() {
		super(...arguments), a_.add(this), o_.set(this, new Fn(this, {
			target: null,
			config: { attributeFilter: ["class"] },
			callback: () => t(this, a_, "m", s_).call(this)
		}));
	}
	attach(e) {
		e instanceof i_ && t(this, o_, "f").observe(e), this.htmlFor && this.parentElement && Hu(this.parentElement, "aria-controls", this.htmlFor), super.attach(e), t(this, a_, "m", s_).call(this);
	}
	detach() {
		for (let e of t(this, o_, "f").targets) t(this, o_, "f").unobserve(e);
		this.parentElement && (this.htmlFor && Uu(this.parentElement, "aria-controls", this.htmlFor), this.parentElement.ariaExpanded = null), super.detach();
	}
	_onClick() {
		this.control instanceof i_ && (this.control.currentMode = this.control.currentMode === "compact" ? "expanded" : "compact");
	}
};
o_ = /* @__PURE__ */ new WeakMap(), a_ = /* @__PURE__ */ new WeakSet(), s_ = async function() {
	if (!this.parentElement) return;
	let e = this.control instanceof i_ && this.control.currentMode === "expanded";
	this.parentElement.ariaExpanded = `${e}`, this.parentElement.hasAttribute("toggle") && this.parentElement.toggleAttribute("selected", e), this.parentElement instanceof x && (await this.parentElement.updateComplete, this.parentElement.ariaPressed = null);
}, c_ = e([w("m3e-nav-rail-toggle")], c_);
//#endregion
//#region node_modules/@m3e/web/dist/loading-indicator.js
function l_(e) {
	return e === "uncontained" || e === "contained";
}
var u_ = {
	activeIndicatorSize: c("var(--m3e-loading-indicator-size, 38px)"),
	activeIndicatorColor: c(`var(--m3e-loading-indicator-active-indicator-color, ${A.color.primary})`),
	containedActiveIndicatorColor: c(`var(--m3e-loading-indicator-contained-active-indicator-color, ${A.color.onPrimaryContainer})`),
	containedContainerColor: c(`var(--m3e-loading-indicator-contained-container-color, ${A.color.secondaryContainer})`),
	containerShape: c(`var(--m3e-loading-indicator-container-shape, ${A.shape.corner.full})`),
	containerSize: c("var(--m3e-loading-indicator-container-size, 48px)")
}, d_ = {
	"4-sided-cookie": "M230.389 50.473C293.109 23.2328 356.767 86.8908 329.527 149.611L325.023 159.981C316.707 179.13 316.707 200.87 325.023 220.019L329.527 230.389C356.767 293.109 293.109 356.767 230.389 329.527L220.019 325.023C200.87 316.707 179.13 316.707 159.981 325.023L149.611 329.527C86.8908 356.767 23.2328 293.109 50.473 230.389L54.9768 220.019C63.2934 200.87 63.2934 179.13 54.9768 159.981L50.473 149.611C23.2328 86.8908 86.8908 23.2328 149.611 50.473L159.981 54.9768C179.13 63.2934 200.87 63.2934 220.019 54.9768L230.389 50.473Z",
	"9-sided-cookie": "M154.828 43.2756C156.574 41.8498 157.448 41.1369 158.245 40.535C177.03 26.3548 202.97 26.3548 221.755 40.535C222.552 41.1369 223.425 41.8498 225.172 43.2756C225.952 43.9121 226.342 44.2303 226.727 44.5333C235.567 51.4788 246.406 55.4147 257.652 55.7636C258.143 55.7788 258.647 55.785 259.654 55.7975C261.911 55.8255 263.039 55.8395 264.037 55.8898C287.563 57.0742 307.435 73.7107 312.689 96.6205C312.912 97.5928 313.121 98.6991 313.541 100.911C313.728 101.899 313.822 102.393 313.922 102.872C316.219 113.862 321.986 123.828 330.377 131.308C330.743 131.635 331.125 131.962 331.888 132.618C333.599 134.087 334.454 134.821 335.187 135.5C352.445 151.495 356.95 176.983 346.215 197.903C345.76 198.791 345.208 199.773 344.104 201.737C343.611 202.613 343.364 203.052 343.132 203.483C337.812 213.375 335.809 224.708 337.418 235.82C337.488 236.304 337.569 236.8 337.732 237.792C338.096 240.014 338.278 241.125 338.402 242.115C341.318 265.436 328.347 287.851 306.647 296.991C305.726 297.379 304.67 297.778 302.559 298.574C301.617 298.929 301.146 299.107 300.69 299.289C290.241 303.455 281.406 310.852 275.48 320.395C275.221 320.811 274.964 321.243 274.449 322.107C273.297 324.043 272.721 325.011 272.178 325.849C259.387 345.584 235.011 354.436 212.498 347.521C211.543 347.228 210.477 346.856 208.347 346.112C207.396 345.78 206.921 345.614 206.455 345.461C195.767 341.951 184.233 341.951 173.545 345.461C173.079 345.614 172.603 345.78 171.652 346.112C169.522 346.856 168.457 347.228 167.502 347.521C144.989 354.436 120.613 345.584 107.822 325.849C107.279 325.011 106.703 324.043 105.55 322.107C105.036 321.243 104.779 320.811 104.52 320.395C98.5939 310.852 89.7583 303.455 79.3096 299.289C78.8539 299.107 78.3827 298.929 77.4404 298.574C75.3294 297.778 74.274 297.379 73.3529 296.991C51.6523 287.851 38.6819 265.436 41.598 242.115C41.7218 241.125 41.9039 240.014 42.2682 237.792C42.4308 236.8 42.5121 236.304 42.5822 235.82C44.1908 224.708 42.188 213.375 36.8675 203.483C36.6354 203.052 36.389 202.613 35.8962 201.737C34.7921 199.773 34.2401 198.791 33.7845 197.903C23.0499 176.983 27.5544 151.495 44.8128 135.5C45.5454 134.821 46.4007 134.087 48.1113 132.618C48.875 131.962 49.2568 131.635 49.6228 131.308C58.0134 123.828 63.7804 113.862 66.0777 102.872C66.1779 102.393 66.2715 101.899 66.4588 100.911C66.8783 98.699 67.088 97.5928 67.311 96.6204C72.5652 73.7107 92.4369 57.0742 115.962 55.8898C116.961 55.8395 118.089 55.8255 120.346 55.7975C121.353 55.785 121.857 55.7788 122.347 55.7636C133.594 55.4147 144.432 51.4788 153.272 44.5333C153.658 44.2303 154.048 43.9121 154.828 43.2756Z",
	oval: "M271.309 271.309C201.705 340.913 108.877 360.935 63.9707 316.029C19.0648 271.123 39.0867 178.295 108.691 108.691C178.295 39.0867 271.123 19.0648 316.029 63.9707C360.935 108.877 340.913 201.705 271.309 271.309Z",
	pentagon: "M155.064 49.459C176.093 34.1803 204.569 34.1803 225.598 49.459L322.926 120.171C343.955 135.45 352.754 162.532 344.722 187.253L307.546 301.668C299.514 326.39 276.476 343.127 250.483 343.127H130.18C104.186 343.127 81.1489 326.39 73.1164 301.668L35.9407 187.253C27.9082 162.532 36.7077 135.45 57.737 120.171L155.064 49.459Z",
	pill: "M116.116 71.7851C169.162 18.7383 255.168 18.7383 308.215 71.7851C361.262 124.832 361.262 210.838 308.215 263.884L263.884 308.215C210.838 361.262 124.832 361.262 71.7851 308.215C18.7383 255.168 18.7383 169.162 71.7851 116.116L116.116 71.7851Z",
	"soft-burst": "M175.147 33.1508C181.983 22.2831 198.017 22.2831 204.853 33.1508L221.238 59.2009C225.731 66.3458 234.797 69.2506 242.692 66.0751L271.475 54.4972C283.482 49.6671 296.455 58.9613 295.507 71.7154L293.235 102.288C292.612 110.673 298.215 118.278 306.494 120.284L336.681 127.601C349.275 130.653 354.23 145.692 345.861 155.461L325.8 178.877C320.298 185.3 320.298 194.7 325.8 201.123L345.861 224.539C354.23 234.308 349.275 249.347 336.681 252.399L306.494 259.716C298.215 261.722 292.612 269.327 293.235 277.712L295.507 308.285C296.455 321.039 283.482 330.333 271.475 325.503L242.692 313.925C234.797 310.749 225.731 313.654 221.238 320.799L204.853 346.849C198.017 357.717 181.983 357.717 175.147 346.849L158.762 320.799C154.269 313.654 145.203 310.749 137.308 313.925L108.525 325.503C96.5177 330.333 83.5454 321.039 84.4931 308.285L86.7649 277.712C87.388 269.327 81.785 261.722 73.5056 259.716L43.3186 252.399C30.7252 249.347 25.7702 234.308 34.1391 224.539L54.1997 201.123C59.7018 194.7 59.7018 185.3 54.1997 178.877L34.1391 155.461C25.7702 145.692 30.7252 130.653 43.3186 127.601L73.5056 120.284C81.785 118.278 87.388 110.673 86.7649 102.288L84.4931 71.7154C83.5454 58.9613 96.5177 49.6671 108.525 54.4972L137.308 66.0751C145.203 69.2506 154.269 66.3458 158.762 59.201L175.147 33.1508Z",
	sunny: "M276.453 68.8118C286.405 69.4881 291.381 69.8263 295.404 71.5853C301.223 74.1305 305.87 78.7766 308.415 84.5965C310.174 88.6186 310.512 93.5948 311.188 103.547L312.732 126.259C313.005 130.284 313.142 132.296 313.579 134.219C314.212 136.997 315.31 139.648 316.827 142.059C317.877 143.728 319.203 145.248 321.856 148.288L336.824 165.438C343.384 172.954 346.663 176.712 348.263 180.8C350.579 186.715 350.579 193.285 348.263 199.2C346.663 203.288 343.384 207.046 336.824 214.562L321.856 231.712C319.203 234.752 317.877 236.272 316.827 237.941C315.31 240.352 314.212 243.003 313.579 245.781C313.142 247.704 313.005 249.716 312.732 253.741L311.188 276.453C310.512 286.405 310.174 291.381 308.415 295.404C305.87 301.223 301.223 305.87 295.404 308.415C291.381 310.174 286.405 310.512 276.453 311.188L253.741 312.732C249.716 313.005 247.704 313.142 245.781 313.579C243.003 314.212 240.352 315.31 237.941 316.827C236.272 317.877 234.752 319.203 231.712 321.856L214.562 336.824C207.046 343.384 203.288 346.663 199.2 348.263C193.285 350.579 186.715 350.579 180.8 348.263C176.712 346.663 172.954 343.384 165.438 336.824L148.288 321.856C145.248 319.203 143.728 317.877 142.059 316.827C139.648 315.31 136.997 314.212 134.219 313.579C132.296 313.142 130.284 313.005 126.259 312.732L103.547 311.188C93.5947 310.512 88.6186 310.174 84.5965 308.415C78.7766 305.87 74.1305 301.223 71.5853 295.404C69.8263 291.381 69.4881 286.405 68.8118 276.453L67.2684 253.741C66.9949 249.716 66.8581 247.704 66.4206 245.781C65.7883 243.003 64.6903 240.352 63.173 237.941C62.123 236.272 60.7965 234.752 58.1437 231.712L43.1756 214.562C36.6164 207.046 33.3369 203.288 31.7366 199.2C29.4211 193.285 29.4211 186.715 31.7366 180.8C33.3369 176.712 36.6164 172.954 43.1756 165.438L58.1437 148.288C60.7965 145.248 62.123 143.728 63.173 142.059C64.6903 139.648 65.7883 136.997 66.4206 134.219C66.8581 132.296 66.9949 130.284 67.2684 126.259L68.8118 103.547C69.4881 93.5948 69.8263 88.6186 71.5853 84.5965C74.1305 78.7766 78.7766 74.1305 84.5965 71.5853C88.6186 69.8263 93.5948 69.4881 103.547 68.8118L126.259 67.2684C130.284 66.9949 132.296 66.8581 134.219 66.4206C136.997 65.7883 139.648 64.6903 142.059 63.173C143.728 62.123 145.248 60.7966 148.288 58.1437L165.438 43.1756C172.954 36.6164 176.712 33.3369 180.8 31.7366C186.715 29.4211 193.285 29.4211 199.2 31.7366C203.288 33.3369 207.046 36.6164 214.562 43.1756L231.712 58.1437C234.752 60.7966 236.272 62.123 237.941 63.173C240.352 64.6903 243.003 65.7883 245.781 66.4206C247.704 66.8581 249.716 66.9949 253.741 67.2684L276.453 68.8118Z"
}, f_ = [], p_ = /* @__PURE__ */ new Map();
for (let e in d_) f_.push(d_[e]), p_.set(e, f_.length - 1);
var m_ = {};
f_ = It(f_, 300);
for (let e of p_) m_[e[0]] = c(f_[e[1]]);
var h_ = m_, g_, __, v_ = class extends ea(j(T(x), "progressbar")) {
	constructor() {
		super(...arguments), g_.add(this), this.variant = "uncontained";
	}
	connectedCallback() {
		super.connectedCallback(), t(this, g_, "m", __).call(this), this.ariaValueMin = this.ariaValueMin || "0", this.ariaValueMax = this.ariaValueMax || "100";
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this._container?.classList.toggle("animate", !1);
	}
	reconnectedCallback() {
		super.reconnectedCallback(), this._container?.classList.toggle("animate", !0);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, g_, "m", __).call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), this._container?.classList.toggle("animate", !0);
	}
	render() {
		return y`<div class="container" aria-hidden="true"><div class="active-indicator-wrapper"><div class="active-indicator"></div></div></div>`;
	}
};
g_ = /* @__PURE__ */ new WeakSet(), __ = function() {
	l_(this.variant) || (this.variant = "uncontained"), Br(this, this.variant, "contained", "uncontained");
}, v_.styles = l`:host { display: inline-block; aspect-ratio: 1 / 1; contain: strict; vertical-align: middle; content-visibility: auto; } :host([hidden]) { display: none; } :host(:is(:state(--uncontained), :--uncontained)) { width: ${u_.activeIndicatorSize}; } :host(:is(:state(--contained), :--contained)) { width: ${u_.containerSize}; } :host(:is(:state(--uncontained), :--uncontained)) .active-indicator { background-color: ${u_.activeIndicatorColor}; } :host(:is(:state(--contained), :--contained)) .active-indicator { background-color: ${u_.containedActiveIndicatorColor}; } :host(:is(:state(--contained), :--contained)) .container { background-color: ${u_.containedContainerColor}; } .container { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; border-radius: ${u_.containerShape}; } .active-indicator { margin: auto; aspect-ratio: 1 / 1; width: calc(${u_.activeIndicatorSize} * 0.842); transform-origin: center; transition: clip-path ${A.motion.spring.slowEffects}; will-change: transform, clip-path; --_polygon-soft-burst: polygon(${h_["soft-burst"]}); --_polygon-9-sided-cookie: polygon(${h_["9-sided-cookie"]}); --_polygon-pentagon: polygon(${h_.pentagon}); --_polygon-pill: polygon(${h_.pill}); --_polygon-sunny: polygon(${h_.sunny}); --_polygon-4-sided-cookie: polygon(${h_["4-sided-cookie"]}); --_polygon-oval: polygon(${h_.oval}); } .container.animate .active-indicator-wrapper { animation: rotate-outer 4666ms linear infinite; transform-origin: center; display: flex; align-items: center; justify-content: center; will-change: transform; } @keyframes rotate-outer { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } } .container.animate .active-indicator { animation: rotate-inner 4666ms cubic-bezier(0.34, 0.88, 0.34, 1) infinite; } @keyframes rotate-inner { 0% { clip-path: var(--_polygon-soft-burst); transform: rotate(0deg); } 14% { clip-path: var(--_polygon-9-sided-cookie); transform: rotate(154deg) scale(1); } 29% { clip-path: var(--_polygon-pentagon); transform: rotate(309deg) scale(1); } 43% { clip-path: var(--_polygon-pill); transform: rotate(463deg) scale(1); } 57% { clip-path: var(--_polygon-sunny); transform: rotate(617deg) scale(1); } 71% { clip-path: var(--_polygon-4-sided-cookie); transform: rotate(771deg) scale(1); } 83% { clip-path: var(--_polygon-oval); transform: rotate(926deg) scale(1); } 100% { clip-path: var(--_polygon-soft-burst); transform: rotate(1080deg) scale(1); } } @media (forced-colors: active) { .active-indicator { background-color: CanvasText !important; } }`, e([C(".container")], v_.prototype, "_container", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], v_.prototype, "variant", void 0), v_ = e([w("m3e-loading-indicator")], v_);
//#endregion
//#region node_modules/lit-html/directives/class-map.js
var y_ = tt(class extends nt {
	constructor(e) {
		if (super(e), e.type !== et.ATTRIBUTE || e.name !== "class" || e.strings?.length > 2) throw Error("`classMap()` can only be used in the `class` attribute and must be the only part in the attribute.");
	}
	render(e) {
		return " " + Object.keys(e).filter((t) => e[t]).join(" ") + " ";
	}
	update(e, [t]) {
		if (this.st === void 0) {
			this.st = /* @__PURE__ */ new Set(), e.strings !== void 0 && (this.nt = new Set(e.strings.join(" ").split(/\s/).filter((e) => e !== "")));
			for (let e in t) t[e] && !this.nt?.has(e) && this.st.add(e);
			return this.render(t);
		}
		let n = e.element.classList;
		for (let e of this.st) e in t || (n.remove(e), this.st.delete(e));
		for (let e in t) {
			let r = !!t[e];
			r === this.st.has(e) || this.nt?.has(e) || (r ? (n.add(e), this.st.add(e)) : (n.remove(e), this.st.delete(e)));
		}
		return je;
	}
});
//#endregion
//#region node_modules/@m3e/web/dist/progress-indicator.js
function b_(e) {
	return e === "flat" || e === "wavy";
}
var x_, S_, C_ = class extends ea(T(j(x, "progressbar"), !0)) {
	constructor() {
		super(...arguments), x_.add(this), this.value = 0, this.max = 100, this.variant = "flat";
	}
	connectedCallback() {
		super.connectedCallback(), t(this, x_, "m", S_).call(this), this.ariaValueMin = "0";
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, x_, "m", S_).call(this);
	}
	update(e) {
		super.update(e), e.has("value") && (this.ariaValueNow = `${this.value}`), e.has("max") && (this.ariaValueMax = `${this.max}`);
	}
};
x_ = /* @__PURE__ */ new WeakSet(), S_ = function() {
	b_(this.variant) || (this.variant = "flat"), Br(this, this.variant, "flat", "wavy");
}, C_.styles = l`:host([hidden]) { display: none; } @media (forced-colors: active) { :host { --m3e-progress-indicator-color: CanvasText; --m3e-progress-indicator-track-color: Canvas; } }`, e([S({
	type: Number,
	reflect: !0
})], C_.prototype, "value", void 0), e([S({ type: Number })], C_.prototype, "max", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], C_.prototype, "variant", void 0);
var q, w_, T_, E_, D_, O_, k_, A_, j_, M_, N_, P_, F_, I_, L_, R_, z_, B_, V_, H_, U_, W_, G_, K_, q_, J_, Y_ = 1.575, X_ = J_ = class extends C_ {
	constructor() {
		super(...arguments), q.add(this), w_.set(this, `m3e-circular-progress-mask-${J_.__nextMaskId++}`), T_.set(this, 0), E_.set(this, 0), D_.set(this, 0), O_.set(this, 0), k_.set(this, void 0), A_.set(this, void 0), j_.set(this, new cr(this, {
			skipInitial: !0,
			target: null,
			callback: () => {
				t(this, q, "m", R_).call(this), t(this, q, "m", z_).call(this), queueMicrotask(() => this.requestUpdate());
			}
		})), M_.set(this, new lt(this, (e, n) => t(this, q, "m", B_).call(this, n))), this.indeterminate = !1;
	}
	update(e) {
		super.update(e), e.has("indeterminate") && (this.ariaValueNow = this.indeterminate ? null : `${this.value}`);
	}
	reconnectedCallback() {
		super.reconnectedCallback(), t(this, q, "m", N_).call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), t(this, q, "m", N_).call(this);
	}
	updated(e) {
		super.updated(e), e.has("indeterminate") && (n(this, k_, this.shadowRoot?.querySelector(".spinner.active-track"), "f"), n(this, A_, this.shadowRoot?.querySelector(".spinner.track"), "f"), this.indeterminate ? t(this, M_, "f").start() : t(this, M_, "f").stop());
	}
	render() {
		return this.variant === "wavy" ? t(this, q, "m", F_).call(this) : t(this, q, "m", P_).call(this);
	}
};
w_ = /* @__PURE__ */ new WeakMap(), T_ = /* @__PURE__ */ new WeakMap(), E_ = /* @__PURE__ */ new WeakMap(), D_ = /* @__PURE__ */ new WeakMap(), O_ = /* @__PURE__ */ new WeakMap(), k_ = /* @__PURE__ */ new WeakMap(), A_ = /* @__PURE__ */ new WeakMap(), j_ = /* @__PURE__ */ new WeakMap(), M_ = /* @__PURE__ */ new WeakMap(), q = /* @__PURE__ */ new WeakSet(), N_ = function() {
	let e = this.shadowRoot?.querySelector(".diameter-and-stroke");
	e && (t(this, q, "m", R_).call(this), t(this, j_, "f").observe(e));
	let n = this.shadowRoot?.querySelector(".amplitude-and-wavelength");
	n && (t(this, q, "m", R_).call(this), t(this, j_, "f").observe(n));
}, P_ = function() {
	if (this.indeterminate) {
		let e = t(this, q, "m", K_).call(this, {
			startAngle: -45,
			endAngle: 90 + t(this, E_, "f")
		}), n = t(this, q, "m", K_).call(this, {
			startAngle: -t(this, E_, "f"),
			endAngle: 135
		});
		return y`<div class="progress" aria-hidden="true"><div class="spinner"><div class="left"><svg width="${t(this, T_, "f")}" height="${t(this, T_, "f")}" viewBox="${e.viewBox}" class="circle"><path class="active-track" d="${e.path}" stroke="currentColor" stroke-width="${t(this, E_, "f")}" fill="none" stroke-linecap="round"/></svg></div><div class="right"><svg width="${t(this, T_, "f")}" height="${t(this, T_, "f")}" viewBox="${n.viewBox}" class="circle"><path class="active-track" d="${n.path}" stroke="currentColor" stroke-width="${t(this, E_, "f")}" fill="none" stroke-linecap="round"/></svg></div></div></div>${t(this, q, "m", I_).call(this)}`;
	}
	let e = t(this, q, "m", H_).call(this, t(this, E_, "f") * 2, t(this, T_, "f")), n = this.value / this.max * 360;
	n > 0 && (n = Math.max(0, e, n));
	let r = t(this, q, "m", K_).call(this, {
		gap: n < 360 ? t(this, E_, "f") : 0,
		endAngle: n
	}), i = t(this, q, "m", K_).call(this, {
		gap: n > 0 ? t(this, E_, "f") : 0,
		startAngle: n,
		endAngle: 360
	});
	return y`<div class="progress" aria-hidden="true"><svg width="${t(this, T_, "f")}" height="${t(this, T_, "f")}" viewBox="${r.viewBox}">${n > 0 ? Ae`<path
            class="active-track"
            d="${r.path}"
            stroke="currentColor"
            stroke-width=${t(this, E_, "f")}
            fill="none"
            stroke-linecap="round"
          />` : b} ${360 - n >= e ? Ae`<path
                class="track"
                d="${i.path}"
                stroke="currentColor"
                stroke-width=${t(this, E_, "f")}
                fill="none"
                stroke-linecap="round"
              />` : b}</svg></div>${t(this, q, "m", I_).call(this)}${t(this, q, "m", L_).call(this)}`;
}, F_ = function() {
	if (this.indeterminate) return y`<div class="progress" aria-hidden="true"><svg width="${t(this, T_, "f")}" height="${t(this, T_, "f")}" viewBox="${t(this, q, "m", q_).call(this, { endAngle: 20 }).viewBox}"><path class="spinner active-track" stroke="currentColor" stroke-width="${t(this, E_, "f")}" stroke-linecap="round" stroke-linejoin="round" fill="none"/><path class="spinner track" stroke="currentColor" stroke-width="${t(this, E_, "f")}" fill="none" stroke-linecap="round"/></svg></div>${t(this, q, "m", I_).call(this)}`;
	let e = t(this, q, "m", H_).call(this, t(this, E_, "f") * 2, t(this, T_, "f")), n = this.value / this.max * 360;
	n > 0 && (n = Math.max(0, e, n));
	let r = n <= e + e / 2 || n == 360 ? 0 : t(this, D_, "f"), i = t(this, q, "m", K_).call(this, {
		gap: n < 360 ? t(this, E_, "f") : 0,
		endAngle: n
	}), a = r == 0 ? i : t(this, q, "m", q_).call(this, {
		endAngle: 360,
		amplitude: r
	}), o = t(this, q, "m", K_).call(this, {
		gap: n > 0 ? t(this, E_, "f") : 0,
		startAngle: n,
		endAngle: 360
	}), s = r > 0 ? r + t(this, E_, "f") / 2 : t(this, E_, "f");
	return y`<svg class="progress" viewBox="${o.viewBox}" aria-hidden="true">${n > 0 ? Ae`${r > 0 ? Ae`<defs>
          <mask id="${t(this, w_, "f")}">
            <path
              d="${i.path}"
              stroke="white"
              stroke-width="${t(this, E_, "f") + s}"
              fill="none"
              stroke-linecap="round"
            />
          </mask>
        </defs>` : b}
        <g class="active-track" mask="${rt(r > 0 ? Ut(t(this, w_, "f")) : void 0)}">
          <path
            class="${y_({ wave: r > 0 })}"
            d="${a.path}"
            stroke="currentColor"
            stroke-width=${t(this, E_, "f")}
            stroke-linecap="round"
            stroke-linejoin="round"
            fill="none"
          />
        </g>` : b} ${360 - n >= e ? Ae`<path
                class="track"
                d="${o.path}"
                stroke="currentColor"
                stroke-width=${t(this, E_, "f")}
                fill="none"
                stroke-linecap="round"
              />` : b}</svg> ${t(this, q, "m", I_).call(this)}${t(this, q, "m", L_).call(this)}`;
}, I_ = function() {
	return y`<div class="diameter-and-stroke" aria-hidden="true"></div><div class="amplitude-and-wavelength" aria-hidden="true"></div>`;
}, L_ = function() {
	return y`<div class="content" aria-hidden="true"><slot></slot></div>`;
}, R_ = function() {
	let e = this.shadowRoot?.querySelector(".diameter-and-stroke");
	e && (n(this, T_, e.clientWidth, "f"), n(this, E_, e.clientHeight, "f"));
}, z_ = function() {
	let e = this.shadowRoot?.querySelector(".amplitude-and-wavelength");
	e && (n(this, D_, e.clientWidth, "f"), n(this, O_, e.clientHeight, "f"));
}, B_ = function(e) {
	if (t(this, D_, "f") === 0 || t(this, O_, "f") === 0) return;
	let n = t(this, q, "m", V_).call(this, e);
	t(this, k_, "f")?.setAttribute("d", t(this, q, "m", q_).call(this, { endAngle: n }).path), t(this, A_, "f")?.setAttribute("d", t(this, q, "m", K_).call(this, {
		gap: t(this, q, "m", H_).call(this, t(this, E_, "f"), t(this, T_, "f")),
		startAngle: n
	}).path);
}, V_ = function(e) {
	let n = t(this, q, "m", H_).call(this, t(this, E_, "f")) * 2, r = 18 + n, i = 280 - n, a = Y_, o = a, s = a, c = a, l = e % 6.3;
	if (l < o) return r;
	if (l < 3.15) {
		let e = (l - o) / s;
		return r + (i - r) * (e * e * (3 - 2 * e));
	}
	if (l < 4.725) return i;
	let u = (l - 4.725) / c;
	return i - (i - r) * (u * u * (3 - 2 * u));
}, H_ = function(e, n = t(this, D_, "f")) {
	return e * (360 / (2 * Math.PI * t(this, q, "m", G_).call(this, n).r));
}, U_ = function(e) {
	return (e - 90) * (Math.PI / 180);
}, W_ = function(e, n) {
	let r = t(this, q, "m", U_).call(this, n);
	return {
		x: e.cx + e.r * Math.cos(r),
		y: e.cy + e.r * Math.sin(r)
	};
}, G_ = function(e) {
	e += t(this, E_, "f") / 2;
	let n = t(this, T_, "f") / 2;
	return {
		cx: n + e,
		cy: n + e,
		r: n,
		padding: e
	};
}, K_ = function({ startAngle: e = 0, endAngle: n = 360, gap: r = 0, padding: i = t(this, D_, "f") }) {
	if (t(this, T_, "f") === 0 || t(this, E_, "f") === 0) return {
		path: "",
		viewBox: "0 0 0 0"
	};
	let a = t(this, q, "m", G_).call(this, i);
	r > 0 && (e += t(this, q, "m", H_).call(this, r, i), n -= t(this, q, "m", H_).call(this, r, i)), n - e >= 360 && (n = e + 359.999);
	let o = t(this, q, "m", W_).call(this, a, n), s = t(this, q, "m", W_).call(this, a, e);
	return {
		path: `M ${o.x} ${o.y} A ${a.r} ${a.r} 0 ${n - e <= 180 ? "0" : "1"} 0 ${s.x} ${s.y}`,
		viewBox: `0 0 ${t(this, T_, "f") + a.padding * 2} ${t(this, T_, "f") + a.padding * 2}`
	};
}, q_ = function({ startAngle: e = 0, endAngle: n = 360, gap: r = 0, padding: i = t(this, D_, "f"), amplitude: a = t(this, D_, "f"), steps: o = 200 }) {
	if (t(this, T_, "f") === 0 || t(this, E_, "f") === 0) return {
		path: "",
		viewBox: "0 0 0 0"
	};
	let s = t(this, q, "m", G_).call(this, i);
	r > 0 && (e += t(this, q, "m", H_).call(this, r, i), n -= t(this, q, "m", H_).call(this, r, i));
	let c = t(this, q, "m", U_).call(this, e), l = t(this, q, "m", U_).call(this, n);
	e === n ? l = c : l < c && (l += Math.PI * 2);
	let u = l - c, d = 2 * Math.PI * s.r / t(this, O_, "f"), f = Math.PI / 2 * (d - 1), p = [];
	for (let e = 0; e <= o; e++) {
		let t = c + (o === 0 ? 0 : e / o) * u, n = Math.sin(t * d + f), r = s.r - a * n, i = r * Math.cos(t) + s.cx, l = r * Math.sin(t) + s.cy;
		p.push([i, l]);
	}
	return {
		path: p.length === 1 ? `M ${p[0][0]},${p[0][1]}` : `M ${p[0][0]},${p[0][1]} ` + p.slice(1).map(([e, t]) => `L ${e},${t}`).join(" "),
		viewBox: `0 0 ${t(this, T_, "f") + s.padding * 2} ${t(this, T_, "f") + s.padding * 2}`
	};
}, X_.styles = [C_.styles, l`:host { display: inline-flex; vertical-align: middle; aspect-ratio: 1; position: relative; align-items: center; justify-content: center; } :host([hidden]) { display: none; } .progress { --_arc-duration: 1333ms; --_cycle-duration: calc(4 * var(--_arc-duration)); --_linear-rotate-duration: calc(var(--_arc-duration) * 360 / 306); --_indeterminate-easing: cubic-bezier(0.4, 0, 0.2, 1); } .active-track { transition: stroke-dashoffset 500ms cubic-bezier(0, 0, 0.2, 1); } :host(:is(:state(--flat), :--flat)) .progress { flex: 1; align-self: stretch; pointer-events: none; } .progress, .spinner, .left, .right, .content, .circle { position: absolute; inset: 0; } .content { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; } .diameter-and-stroke, .amplitude-and-wavelength { visibility: hidden; position: absolute; } .diameter-and-stroke { width: inherit; height: var(--m3e-circular-progress-indicator-thickness, 4px); } .amplitude-and-wavelength { width: var(--m3e-circular-wavy-progress-indicator-amplitude, 1.6px); height: var(--m3e-circular-wavy-progress-indicator-wavelength, 15px); } :host(:is(:state(--flat), :--flat)) { width: var(--m3e-circular-flat-progress-indicator-diameter, 40px); } :host(:is(:state(--wavy), :--wavy)) { width: var(--m3e-circular-wavy-progress-indicator-diameter, 48px); } :host(:is(:state(--flat), :--flat)[indeterminate]) .progress { animation: linear infinite linear-rotate; animation-duration: var(--_linear-rotate-duration); } :host(:is(:state(--flat), :--flat)[indeterminate]) .spinner { animation: infinite both rotate-arc; animation-duration: var(--_cycle-duration); animation-timing-function: var(--_indeterminate-easing); } :host(:is(:state(--wavy), :--wavy)[indeterminate]) .spinner { transform-origin: 50% 50%; animation: wavy-spin ${Y_}s linear infinite; } .left { clip-path: inset(0); inset: 0 50% 0 0; } .right { clip-path: inset(0); inset: 0 0 0 50%; } .circle { animation: expand-arc; animation-iteration-count: infinite; animation-fill-mode: both; animation-duration: var(--_arc-duration), var(--_cycle-duration); animation-timing-function: var(--_indeterminate-easing); } .left .circle { rotate: 135deg; inset: 0 -100% 0 0; } .right .circle { rotate: 100deg; inset: 0 0 0 -100%; animation-delay: calc(-0.5 * var(--_arc-duration)), 0ms; } .track { color: var(--m3e-progress-indicator-track-color, ${A.color.secondaryContainer}); } .active-track { color: var(--m3e-progress-indicator-color, ${A.color.primary}); } .wave { animation: spin-reverse 8s linear infinite; transform-origin: 50% 50%; } @keyframes expand-arc { 0% { transform: rotate(265deg); } 50% { transform: rotate(130deg); } 100% { transform: rotate(265deg); } } @keyframes rotate-arc { 12.5% { transform: rotate(135deg); } 25% { transform: rotate(270deg); } 37.5% { transform: rotate(405deg); } 50% { transform: rotate(540deg); } 62.5% { transform: rotate(675deg); } 75% { transform: rotate(810deg); } 87.5% { transform: rotate(945deg); } 100% { transform: rotate(1080deg); } } @keyframes linear-rotate { to { transform: rotate(360deg); } } @keyframes spin-reverse { from { transform: rotate(360deg); } to { transform: rotate(0deg); } } @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } } @keyframes wavy-spin { 0% { transform: rotate(0deg); } 10% { transform: rotate(90deg); } 100% { transform: rotate(360deg); } } @media (forced-colors: active) { :host(:is(:state(--flat), :--flat)) circle { fill: Canvas; } :host(:is(:state(--flat), :--flat)) .circle { border-color: var(--m3e-progress-indicator-color, ${A.color.primary}) var(--m3e-progress-indicator-color, ${A.color.primary}) Canvas Canvas; } } @media (forced-colors: active) { .progress { --m3e-progress-indicator-track-color: GrayText; --m3e-progress-indicator-color: CanvasText; } }`], X_.__nextMaskId = 0, e([S({
	type: Boolean,
	reflect: !0
})], X_.prototype, "indeterminate", void 0), X_ = J_ = e([w("m3e-circular-progress-indicator")], X_);
function Z_(e) {
	return e === "determinate" || e === "indeterminate" || e === "buffer" || e === "query";
}
var Q_, $_, ev, tv, nv, rv, iv, av, ov, sv, cv, lv, uv, dv, fv = dv = class extends Cd(C_) {
	constructor() {
		super(...arguments), Q_.add(this), $_.set(this, `m3e-linear-progress-mask-${dv.__nextMaskId++}`), ev.set(this, 0), tv.set(this, 0), nv.set(this, 0), rv.set(this, new cr(this, {
			skipInitial: !0,
			target: null,
			callback: () => {
				t(this, Q_, "m", cv).call(this), t(this, Q_, "m", lv).call(this), queueMicrotask(() => this.requestUpdate());
			}
		})), this.mode = "determinate", this.bufferValue = 0;
	}
	connectedCallback() {
		super.connectedCallback(), t(this, Q_, "m", iv).call(this);
	}
	reconnectedCallback() {
		super.reconnectedCallback(), t(this, Q_, "m", av).call(this);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("mode") && t(this, Q_, "m", iv).call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), t(this, Q_, "m", av).call(this);
	}
	update(e) {
		super.update(e), e.has("mode") && (this.ariaValueNow = this.mode === "indeterminate" || this.mode === "query" ? null : `${this.value}`);
	}
	updated(e) {
		if (super.updated(e), e.has("value") || e.has("bufferValue") || e.has("max")) {
			let e = this.shadowRoot?.querySelector(".progress");
			e?.style.setProperty("--_value", `${this.value / this.max * 100}%`), e?.style.setProperty("--_buffer-value", `${this.bufferValue / this.max * 100}%`);
		}
	}
	render() {
		let e = Math.max(0, Math.min(this.value, this.max)) / this.max * this.clientWidth, n = this.mode === "indeterminate" || this.mode === "query" ? this.clientWidth : e + t(this, nv, "f") * 3, r = this.variant === "wavy" && t(this, ev, "f") > 0 && t(this, tv, "f") > 0 && t(this, nv, "f") > 0 ? t(this, Q_, "m", uv).call(this, n) : void 0;
		return y`<div class="progress" aria-hidden="true" style="${Mr({ "--_translate-x": `-${e}px` })}">${r ? b : y`<div class="track"></div>`} ${(this.mode === "determinate" || this.mode === "buffer") && this.value <= 0 ? b : y`<div class="primary">${r && (this.mode === "determinate" || this.mode === "buffer") ? t(this, Q_, "m", ov).call(this, n, r.height, r.viewBox, r.path) : b}</div>${this.mode === "determinate" || this.mode === "buffer" ? y`<div class="gap"></div>` : b}`} ${this.mode !== "buffer" || this.bufferValue > 0 ? y`<div class="secondary"></div>` : b} ${this.mode === "buffer" && this.bufferValue > 0 ? y`<div class="gap"></div>` : b} ${this.mode === "buffer" ? y`<div class="buffer"></div>` : b} ${(this.mode === "determinate" || this.mode === "buffer") && this.value > 0 ? y`<div class="gap"></div><div class="stop"></div>` : b} ${r && this.mode !== "determinate" && this.mode !== "buffer" ? t(this, Q_, "m", ov).call(this, n, r.height, r.viewBox, r.path) : b}</div>${t(this, Q_, "m", sv).call(this)}`;
	}
};
$_ = /* @__PURE__ */ new WeakMap(), ev = /* @__PURE__ */ new WeakMap(), tv = /* @__PURE__ */ new WeakMap(), nv = /* @__PURE__ */ new WeakMap(), rv = /* @__PURE__ */ new WeakMap(), Q_ = /* @__PURE__ */ new WeakSet(), iv = function() {
	Z_(this.mode) || (this.mode = "determinate"), Br(this, this.mode, "buffer", "determinate", "indeterminate", "query");
}, av = function() {
	t(this, Q_, "m", cv).call(this), t(this, Q_, "m", lv).call(this);
	let e = this.shadowRoot?.querySelector(".stroke");
	e && t(this, rv, "f").observe(e);
	let n = this.shadowRoot?.querySelector(".amplitude-and-wavelength");
	n && t(this, rv, "f").observe(n);
}, ov = function(e, n, r, i) {
	let a = this.mode === "indeterminate" || this.mode === "query";
	return !a && this.value / this.max === 1 ? y`<div class="complete"></div>` : y`<svg class="wave" width="${e}" height="${n}" viewBox="${r}" preserveAspectRatio="none">${a ? Ae`
        <mask id="${t(this, $_, "f")}" maskUnits="userSpaceOnUse">
          <rect width="${e}" height="${n}" fill="black" />
          <rect class="primary" height="${n}" fill="white" />
          <rect class="secondary" height="${n}" fill="white" />
        </mask>
        <mask id="${`${t(this, $_, "f")}-inverse`}" maskUnits="userSpaceOnUse">
          <rect width="${e}" height="${n}" fill="white" />
          <rect class="primary" height="${n}" fill="black" />
          <rect class="secondary" height="${n}" fill="black" />
        </mask>
        <g mask="${Ut(t(this, $_, "f"))}">
          <path d="${i}" stroke="currentColor" stroke-width=${t(this, ev, "f")} fill="none" stroke-linecap="round" />
        </g>
        <g mask="${Ut(`${t(this, $_, "f")}-inverse`)}">
          <rect class="track" width="100%" height="${t(this, ev, "f")}" fill="currentColor" />
        </g>` : Ae`<path d="${i}" stroke="currentColor" stroke-width=${t(this, ev, "f")} fill="none" stroke-linecap="round" />`}</svg>`;
}, sv = function() {
	return y`<div class="stroke" aria-hidden="true"></div><div class="amplitude-and-wavelength" aria-hidden="true"></div>`;
}, cv = function() {
	let e = this.shadowRoot?.querySelector(".stroke");
	e && n(this, ev, e.clientHeight, "f");
}, lv = function() {
	let e = this.shadowRoot?.querySelector(".amplitude-and-wavelength");
	e && (n(this, tv, e.clientHeight, "f"), n(this, nv, e.clientWidth, "f"));
}, uv = function(e, n = 0) {
	let r = t(this, tv, "f") + t(this, ev, "f") / 2, i = r, a = [], o = t(this, nv, "f") / 2, s = 0;
	for (a.push(`M ${s},${i}`); s <= e;) {
		let e = s + o, c = i + r * Math.sin(2 * Math.PI * e / t(this, nv, "f") + n), l = s + o / 2, u = i + r * Math.sin(2 * Math.PI * (s + o / 2) / t(this, nv, "f") + n);
		a.push(`Q ${l},${u} ${e},${c}`), s += o;
	}
	let c = `0 -1 ${e} ${r * 2 + 2}`;
	return {
		path: a.join(" "),
		viewBox: c,
		height: t(this, ev, "f") + t(this, tv, "f") * 2,
		padding: 1
	};
}, fv.styles = [C_.styles, l`:host { display: block; position: relative; } .progress { width: 100%; height: 100%; position: relative; align-items: center; border-radius: var(--m3e-linear-progress-indicator-shape, ${A.shape.corner.extraSmall}); } .stroke, .amplitude-and-wavelength { visibility: hidden; position: absolute; } .stroke { width: 100%; height: var(--m3e-linear-progress-indicator-thickness, 4px); } .amplitude-and-wavelength { height: var(--m3e-linear-wavy-progress-indicator-amplitude, 3px); width: var(--m3e-linear-wavy-progress-indicator-wavelength, 40px); } :host(:is(:state(--indeterminate), :--indeterminate)) .amplitude-and-wavelength, :host(:is(:state(--query), :--query)) .amplitude-and-wavelength { width: var(--m3e-linear-wavy-indeterminate-progress-indicator-wavelength, 24px); } .primary, .secondary, .stop { height: var(--m3e-linear-progress-indicator-thickness, 4px); border-radius: inherit; } .stop { aspect-ratio: 1; flex: none; } :host(:is(:state(--flat), :--flat)) { height: var(--m3e-linear-progress-indicator-thickness, 4px); } :host(:is(:state(--wavy), :--wavy)) { height: calc( var(--m3e-linear-progress-indicator-thickness, 4px) + calc(var(--m3e-linear-wavy-progress-indicator-amplitude, 3px) * 2) ); } :host(:is(:state(--wavy), :--wavy)) .primary, :host(:is(:state(--wavy), :--wavy)) .secondary { position: relative; height: 100%; overflow: hidden; } :host(:is(:state(--wavy), :--wavy)) .complete { position: absolute; margin: auto; top: calc(50% - calc(var(--m3e-linear-progress-indicator-thickness, 4px) / 2)); left: 0; right: 0; height: var(--m3e-linear-progress-indicator-thickness, 4px); border-radius: inherit; } :host(:is(:state(--wavy), :--wavy)) .secondary { height: var(--m3e-linear-progress-indicator-thickness, 4px); } .wave { position: absolute; display: block; } .primary .wave, .secondary .wave { margin-inline-start: calc(0px - var(--m3e-linear-wavy-progress-indicator-wavelength, 40px)); } :host(:is(:state(--wavy), :--wavy):is(:state(--determinate), :--determinate)) .primary path, :host(:is(:state(--wavy), :--wavy):is(:state(--buffer), :--buffer)) .primary path { animation: wave-slide 1.5s linear infinite; } @keyframes wave-slide { from { transform: translateX(0); } to { transform: translateX(calc(0px - var(--m3e-linear-wavy-progress-indicator-wavelength, 40px))); } } :host(:is(:state(--determinate), :--determinate)) .progress, :host(:is(:state(--buffer), :--buffer)) .progress { display: flex; overflow: hidden; } :host(:is(:state(--determinate), :--determinate)) .primary, :host(:is(:state(--buffer), :--buffer)) .primary { width: var(--_value, 0px); flex: none; } :host(:is(:state(--determinate), :--determinate)) .gap, :host(:is(:state(--buffer), :--buffer)) .gap { flex-basis: var(--m3e-linear-progress-indicator-thickness, 4px); flex-shrink: 1; } :host(:is(:state(--determinate), :--determinate)) .secondary, :host(:is(:state(--buffer), :--buffer)) .buffer { flex: 1 1 auto; } :host(:is(:state(--buffer), :--buffer)) .buffer { flex-shrink: 5; height: 100%; width: 100%; background-color: var(--m3e-progress-indicator-track-color, ${A.color.secondaryContainer}); mask-image: radial-gradient( circle, black 0, black calc(var(--m3e-linear-progress-indicator-thickness, 4px) / 2), transparent calc(var(--m3e-linear-progress-indicator-thickness, 4px) / 2) ); mask-size: calc(var(--m3e-linear-progress-indicator-thickness, 4px) * 2) 100%; mask-repeat: repeat; animation: buffer 250ms linear infinite; } :host(:is(:state(--rtl), :--rtl):is(:state(--buffer), :--buffer)) .buffer { transform: scaleX(-1); } @keyframes buffer { from { mask-position: 0 0; } to { mask-position: calc(-1 * calc(var(--m3e-linear-progress-indicator-thickness, 4px) * 2)) 0; } } :host(:is(:state(--buffer), :--buffer)) .secondary { width: var(--_buffer-value, 0px); flex: none; } :host(:is(:state(--indeterminate), :--indeterminate)) .primary, :host(:is(:state(--query), :--query)) .primary { position: absolute; top: 0; height: 100%; border-radius: inherit; animation: indeterminate-primary 2.1s infinite linear; } :host(:is(:state(--wavy), :--wavy):is(:state(--indeterminate), :--indeterminate)) .primary, :host(:is(:state(--wavy), :--wavy):is(:state(--query), :--query)) .primary { animation-name: wavy-indeterminate-primary; } :host(:is(:state(--indeterminate), :--indeterminate)) .secondary, :host(:is(:state(--query), :--query)) .secondary { position: absolute; top: 0; height: 100%; border-radius: inherit; animation: indeterminate-secondary 2.1s infinite linear; animation-delay: 1.15s; animation-fill-mode: backwards; } :host(:is(:state(--wavy), :--wavy):is(:state(--indeterminate), :--indeterminate)) .secondary, :host(:is(:state(--wavy), :--wavy):is(:state(--query), :--query)) .secondary { animation-name: wavy-indeterminate-secondary; } :host(:is(:state(--indeterminate), :--indeterminate)) .progress, :host(:is(:state(--query), :--query)) .progress { overflow: hidden; position: relative; } :host(:not(:is(:state(--rtl), :--rtl)):is(:state(--query), :--query)) .progress, :host(:is(:state(--rtl), :--rtl):is(:state(--indeterminate), :--indeterminate)) .progress { transform: scaleX(-1); } :host(:is(:state(--flat), :--flat)) .primary, :host(:is(:state(--flat), :--flat):is(:state(--indeterminate), :--indeterminate)) .secondary, :host(:is(:state(--flat), :--flat):is(:state(--query), :--query)) .secondary, :host(:is(:state(--wavy), :--wavy)) .complete, .stop { background-color: var(--m3e-progress-indicator-color, ${A.color.primary}); } :host(:is(:state(--wavy), :--wavy)) .progress { color: var(--m3e-progress-indicator-color, ${A.color.primary}); } :host(:is(:state(--determinate), :--determinate)) .secondary, :host(:is(:state(--buffer), :--buffer)) .secondary, :host(:is(:state(--flat), :--flat):is(:state(--indeterminate), :--indeterminate)) .track, :host(:is(:state(--flat), :--flat):is(:state(--query), :--query)) .track { background-color: var(--m3e-progress-indicator-track-color, ${A.color.secondaryContainer}); } :host(:is(:state(--wavy), :--wavy):is(:state(--indeterminate), :--indeterminate)) .track, :host(:is(:state(--wavy), :--wavy):is(:state(--query), :--query)) .track { color: var(--m3e-progress-indicator-track-color, ${A.color.secondaryContainer}); } :host(:is(:state(--wavy), :--wavy):is(:state(--indeterminate), :--indeterminate)) .track, :host(:is(:state(--wavy), :--wavy):is(:state(--query), :--query)) .track { y: calc(50% - calc(var(--m3e-linear-progress-indicator-thickness, 4px) / 2)); border-radius: inherit; } :host(:is(:state(--flat), :--flat):is(:state(--indeterminate), :--indeterminate)) .track, :host(:is(:state(--flat), :--flat):is(:state(--query), :--query)) .track { position: absolute; margin: auto; top: calc(50% - calc(var(--m3e-linear-progress-indicator-thickness, 4px) / 2)); left: 0; right: 0; height: var(--m3e-linear-progress-indicator-thickness, 4px); border-radius: inherit; } @keyframes indeterminate-primary { 0% { left: -145.167%; width: 8%; } 20% { left: -113.333%; width: 48%; } 60% { left: 56.333%; width: 78%; } 100% { left: 100%; width: 8%; } } @keyframes indeterminate-secondary { 0% { left: -54.888%; width: 8%; } 20% { left: -20%; width: 48%; } 60% { left: 60%; width: 78%; } 100% { left: 160%; width: 8%; } } @keyframes wavy-indeterminate-primary { 0% { transform: translateX(-145.167%); width: 8%; } 20% { transform: translateX(-113.333%); width: 48%; } 60% { transform: translateX(56.333%); width: 78%; } 100% { transform: translateX(100%); width: 8%; } } @keyframes wavy-indeterminate-secondary { 0% { transform: translateX(-54.888%); width: 8%; } 20% { transform: translateX(-20%); width: 48%; } 60% { transform: translateX(60%); width: 78%; } 100% { transform: translateX(160%); width: 8%; } } @media (forced-colors: active) { .progress { --m3e-progress-indicator-track-color: GrayText; --m3e-progress-indicator-color: CanvasText; } }`], fv.__nextMaskId = 0, e([S({
	reflect: !0,
	useDefault: !0
})], fv.prototype, "mode", void 0), e([S({
	attribute: "buffer-value",
	type: Number
})], fv.prototype, "bufferValue", void 0), fv = dv = e([w("m3e-linear-progress-indicator")], fv);
//#endregion
//#region node_modules/@m3e/web/dist/shape.js
var pv = {
	size: c("var(--m3e-shape-size, 48px)"),
	containerColor: c(`var(--m3e-shape-container-color, ${A.color.primary})`),
	transition: c(`var(--m3e-shape-transition, clip-path ${A.motion.spring.slowEffects})`)
}, mv = l`:host { display: inline-block; position: relative; aspect-ratio: 1 / 1; width: ${pv.size}; } :host([hidden]) { display: none; } :host([name]) .wrapper { background-color: ${pv.containerColor}; transition: ${pv.transition}; transform-origin: center; } .wrapper { width: 100%; height: 100%; position: relative; isolation: isolate; overflow: hidden; will-change: clip-path; } ::slotted(img), ::slotted(video) { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; } @media (prefers-reduced-motion) { .wrapper { transition: none; } }`, hv = {
	"4-leaf-clover": "M63.0014 190C49.8954 174.339 42 154.126 42 132.06C42 82.321 82.1131 42 131.595 42C153.99 42 174.466 50.2594 190.173 63.9131C205.843 50.2594 226.271 42 248.614 42C297.981 42 338 82.321 338 132.06C338 154.126 330.123 174.339 317.048 190C330.123 205.661 338 225.874 338 247.94C338 297.679 297.981 338 248.614 338C226.271 338 205.843 329.741 190.173 316.087C174.466 329.741 153.99 338 131.595 338C82.1131 338 42 297.679 42 247.94C42 225.874 49.8954 205.661 63.0014 190Z",
	"4-sided-cookie": "M230.389 50.473C293.109 23.2328 356.767 86.8908 329.527 149.611L325.023 159.981C316.707 179.13 316.707 200.87 325.023 220.019L329.527 230.389C356.767 293.109 293.109 356.767 230.389 329.527L220.019 325.023C200.87 316.707 179.13 316.707 159.981 325.023L149.611 329.527C86.8908 356.767 23.2328 293.109 50.473 230.389L54.9768 220.019C63.2934 200.87 63.2934 179.13 54.9768 159.981L50.473 149.611C23.2328 86.8908 86.8908 23.2328 149.611 50.473L159.981 54.9768C179.13 63.2934 200.87 63.2934 220.019 54.9768L230.389 50.473Z",
	"6-sided-cookie": "M134.186 54.5654C165.276 24.4782 214.724 24.4782 245.814 54.5654C255.328 63.7718 266.984 70.4811 279.738 74.0919C321.419 85.8924 346.142 128.586 335.552 170.473C332.312 183.291 332.312 196.709 335.552 209.527C346.142 251.414 321.419 294.108 279.738 305.908C266.984 309.519 255.328 316.228 245.814 325.435C214.724 355.522 165.276 355.522 134.186 325.435C124.672 316.228 113.016 309.519 100.262 305.908C58.5815 294.108 33.8578 251.414 44.4476 209.527C47.6879 196.709 47.6879 183.291 44.4476 170.473C33.8578 128.586 58.5815 85.8924 100.262 74.0919C113.016 70.4811 124.672 63.7718 134.186 54.5654Z",
	"7-sided-cookie": "M142.67 51.7842C146.243 48.6394 148.029 47.067 149.671 45.7954C173.425 27.4015 206.575 27.4015 230.329 45.7954C231.971 47.067 233.757 48.6394 237.33 51.7842C238.526 52.8372 239.124 53.3636 239.724 53.8672C248.136 60.9247 258.171 65.7683 268.92 67.9594C269.688 68.1158 270.471 68.2562 272.038 68.537C276.719 69.3756 279.059 69.7949 281.075 70.2889C310.234 77.4346 330.902 103.411 331.364 133.493C331.396 135.573 331.283 137.953 331.057 142.713C330.982 144.307 330.944 145.104 330.925 145.888C330.665 156.88 333.143 167.763 338.136 177.553C338.493 178.252 338.872 178.953 339.63 180.356C341.894 184.547 343.026 186.642 343.897 188.53C356.503 215.834 349.127 248.226 325.949 267.344C324.347 268.666 322.42 270.061 318.566 272.852C317.276 273.787 316.63 274.254 316.007 274.729C307.27 281.377 300.326 290.105 295.803 300.122C295.48 300.837 295.169 301.571 294.547 303.04C292.69 307.427 291.761 309.62 290.832 311.48C277.393 338.382 247.526 352.798 218.162 346.556C216.132 346.124 213.842 345.484 209.262 344.204C207.728 343.776 206.962 343.562 206.203 343.369C195.569 340.668 184.431 340.668 173.797 343.369C173.038 343.562 172.272 343.776 170.738 344.204C166.158 345.484 163.868 346.124 161.838 346.556C132.474 352.798 102.607 338.382 89.168 311.48C88.2388 309.62 87.3102 307.427 85.453 303.04C84.8311 301.571 84.5202 300.837 84.1975 300.122C79.6741 290.105 72.7297 281.377 63.993 274.729C63.3696 274.254 62.7244 273.787 61.434 272.852C57.5801 270.061 55.6532 268.666 54.0507 267.344C30.873 248.226 23.4965 215.834 36.1027 188.53C36.9742 186.642 38.1062 184.547 40.3703 180.356C41.1283 178.953 41.5074 178.252 41.8636 177.553C46.8568 167.763 49.3353 156.88 49.0745 145.888C49.0559 145.104 49.0182 144.307 48.9426 142.713C48.7168 137.953 48.6039 135.573 48.6359 133.493C49.0982 103.411 69.7665 77.4346 98.9252 70.2889C100.941 69.7949 103.281 69.3756 107.962 68.537C109.529 68.2562 110.312 68.1158 111.08 67.9594C121.829 65.7683 131.864 60.9247 140.276 53.8672C140.876 53.3636 141.474 52.8372 142.67 51.7842Z",
	"8-leaf-clover": "M338.584 189.998C364.427 238.164 344.902 281.771 295.066 295.061C281.771 344.902 238.164 364.422 189.998 338.584C141.831 364.427 98.2245 344.902 84.9337 295.066C35.0981 281.771 15.573 238.164 41.4157 189.998C15.573 141.831 35.0981 98.2245 84.9337 84.9337C98.2245 35.0981 141.831 15.573 189.998 41.4157C238.164 15.573 281.771 35.0981 295.061 84.9337C344.902 98.2245 364.422 141.831 338.584 189.998Z",
	"9-sided-cookie": "M154.828 43.2756C156.574 41.8498 157.448 41.1369 158.245 40.535C177.03 26.3548 202.97 26.3548 221.755 40.535C222.552 41.1369 223.425 41.8498 225.172 43.2756C225.952 43.9121 226.342 44.2303 226.727 44.5333C235.567 51.4788 246.406 55.4147 257.652 55.7636C258.143 55.7788 258.647 55.785 259.654 55.7975C261.911 55.8255 263.039 55.8395 264.037 55.8898C287.563 57.0742 307.435 73.7107 312.689 96.6205C312.912 97.5928 313.121 98.6991 313.541 100.911C313.728 101.899 313.822 102.393 313.922 102.872C316.219 113.862 321.986 123.828 330.377 131.308C330.743 131.635 331.125 131.962 331.888 132.618C333.599 134.087 334.454 134.821 335.187 135.5C352.445 151.495 356.95 176.983 346.215 197.903C345.76 198.791 345.208 199.773 344.104 201.737C343.611 202.613 343.364 203.052 343.132 203.483C337.812 213.375 335.809 224.708 337.418 235.82C337.488 236.304 337.569 236.8 337.732 237.792C338.096 240.014 338.278 241.125 338.402 242.115C341.318 265.436 328.347 287.851 306.647 296.991C305.726 297.379 304.67 297.778 302.559 298.574C301.617 298.929 301.146 299.107 300.69 299.289C290.241 303.455 281.406 310.852 275.48 320.395C275.221 320.811 274.964 321.243 274.449 322.107C273.297 324.043 272.721 325.011 272.178 325.849C259.387 345.584 235.011 354.436 212.498 347.521C211.543 347.228 210.477 346.856 208.347 346.112C207.396 345.78 206.921 345.614 206.455 345.461C195.767 341.951 184.233 341.951 173.545 345.461C173.079 345.614 172.603 345.78 171.652 346.112C169.522 346.856 168.457 347.228 167.502 347.521C144.989 354.436 120.613 345.584 107.822 325.849C107.279 325.011 106.703 324.043 105.55 322.107C105.036 321.243 104.779 320.811 104.52 320.395C98.5939 310.852 89.7583 303.455 79.3096 299.289C78.8539 299.107 78.3827 298.929 77.4404 298.574C75.3294 297.778 74.274 297.379 73.3529 296.991C51.6523 287.851 38.6819 265.436 41.598 242.115C41.7218 241.125 41.9039 240.014 42.2682 237.792C42.4308 236.8 42.5121 236.304 42.5822 235.82C44.1908 224.708 42.188 213.375 36.8675 203.483C36.6354 203.052 36.389 202.613 35.8962 201.737C34.7921 199.773 34.2401 198.791 33.7845 197.903C23.0499 176.983 27.5544 151.495 44.8128 135.5C45.5454 134.821 46.4007 134.087 48.1113 132.618C48.875 131.962 49.2568 131.635 49.6228 131.308C58.0134 123.828 63.7804 113.862 66.0777 102.872C66.1779 102.393 66.2715 101.899 66.4588 100.911C66.8783 98.699 67.088 97.5928 67.311 96.6204C72.5652 73.7107 92.4369 57.0742 115.962 55.8898C116.961 55.8395 118.089 55.8255 120.346 55.7975C121.353 55.785 121.857 55.7788 122.347 55.7636C133.594 55.4147 144.432 51.4788 153.272 44.5333C153.658 44.2303 154.048 43.9121 154.828 43.2756Z",
	"12-sided-cookie": "M166.697 39.8458C167.238 39.3157 167.508 39.0506 167.743 38.8298C180.246 27.0567 199.754 27.0567 212.257 38.8298C212.492 39.0506 212.762 39.3157 213.303 39.8458C213.628 40.1639 213.79 40.323 213.945 40.4703C221.945 48.1056 233.282 51.1433 244.028 48.5311C244.235 48.4807 244.456 48.4242 244.896 48.311C245.629 48.1224 245.996 48.0282 246.31 47.9542C263.024 44.0098 279.919 53.7642 284.86 70.2114C284.953 70.5199 285.055 70.8847 285.258 71.6143C285.38 72.0522 285.442 72.2711 285.502 72.4758C288.613 83.0884 296.912 91.3875 307.524 94.4985C307.729 94.5585 307.948 94.6195 308.386 94.7417C309.115 94.9452 309.48 95.0469 309.789 95.1396C326.236 100.081 335.99 116.976 332.046 133.69C331.972 134.004 331.878 134.371 331.689 135.104C331.576 135.544 331.519 135.765 331.469 135.972C328.857 146.718 331.894 158.055 339.53 166.055C339.677 166.21 339.836 166.372 340.154 166.697C340.684 167.238 340.949 167.508 341.17 167.743C352.943 180.246 352.943 199.754 341.17 212.257C340.949 212.492 340.684 212.762 340.154 213.303C339.836 213.628 339.677 213.79 339.53 213.945C331.894 221.945 328.857 233.282 331.469 244.028C331.519 244.235 331.576 244.456 331.689 244.896C331.878 245.629 331.972 245.996 332.046 246.31C335.99 263.024 326.236 279.919 309.789 284.86C309.48 284.953 309.115 285.055 308.386 285.258C307.948 285.381 307.729 285.442 307.524 285.502C296.912 288.613 288.613 296.912 285.502 307.524C285.442 307.729 285.381 307.948 285.258 308.386C285.055 309.115 284.953 309.48 284.86 309.789C279.919 326.236 263.024 335.99 246.31 332.046C245.996 331.972 245.629 331.878 244.896 331.689C244.456 331.576 244.235 331.519 244.028 331.469C233.282 328.857 221.945 331.894 213.945 339.53C213.79 339.677 213.628 339.836 213.303 340.154C212.762 340.684 212.492 340.949 212.257 341.17C199.754 352.943 180.246 352.943 167.743 341.17C167.508 340.949 167.238 340.684 166.697 340.154C166.372 339.836 166.21 339.677 166.055 339.53C158.055 331.894 146.718 328.857 135.972 331.469C135.765 331.519 135.544 331.576 135.104 331.689C134.371 331.878 134.004 331.972 133.69 332.046C116.976 335.99 100.081 326.236 95.1396 309.789C95.0469 309.48 94.9452 309.115 94.7417 308.386C94.6195 307.948 94.5585 307.729 94.4985 307.524C91.3875 296.912 83.0884 288.613 72.4758 285.502C72.2711 285.442 72.0522 285.38 71.6143 285.258C70.8847 285.055 70.5199 284.953 70.2114 284.86C53.7642 279.919 44.0098 263.024 47.9542 246.31C48.0282 245.996 48.1224 245.629 48.311 244.896C48.4242 244.456 48.4807 244.235 48.5311 244.028C51.1433 233.282 48.1056 221.945 40.4703 213.945C40.323 213.79 40.1639 213.628 39.8458 213.303C39.3157 212.762 39.0506 212.492 38.8298 212.257C27.0567 199.754 27.0567 180.246 38.8298 167.743C39.0506 167.508 39.3157 167.238 39.8458 166.697C40.1639 166.372 40.323 166.21 40.4703 166.055C48.1056 158.055 51.1433 146.718 48.5311 135.972C48.4807 135.765 48.4242 135.544 48.311 135.104C48.1224 134.371 48.0282 134.004 47.9542 133.69C44.0098 116.976 53.7642 100.081 70.2114 95.1396C70.5199 95.0469 70.8847 94.9452 71.6143 94.7417C72.0522 94.6195 72.2711 94.5585 72.4758 94.4985C83.0884 91.3875 91.3875 83.0884 94.4985 72.4758C94.5585 72.2711 94.6195 72.0522 94.7417 71.6143C94.9452 70.8847 95.0469 70.5199 95.1396 70.2114C100.081 53.7642 116.976 44.0098 133.69 47.9542C134.004 48.0282 134.371 48.1224 135.104 48.311C135.544 48.4242 135.765 48.4807 135.972 48.5311C146.718 51.1433 158.055 48.1056 166.055 40.4703C166.21 40.323 166.372 40.1639 166.697 39.8458Z",
	burst: "M187.293 26.6421C188.056 25.2785 188.437 24.5966 188.902 24.3108C189.575 23.8964 190.425 23.8964 191.098 24.3108C191.563 24.5966 191.944 25.2785 192.707 26.6421L218.917 73.4925C219.386 74.3306 219.62 74.7497 219.937 75.0046C220.396 75.3737 220.989 75.5326 221.571 75.4425C221.973 75.3802 222.386 75.1345 223.211 74.6431L269.335 47.1743C270.677 46.3748 271.348 45.9751 271.893 45.9598C272.684 45.9377 273.42 46.3624 273.796 47.0581C274.055 47.5379 274.045 48.3191 274.023 49.8814L273.296 103.56C273.283 104.52 273.277 105 273.424 105.38C273.637 105.929 274.071 106.363 274.62 106.576C275 106.723 275.48 106.717 276.44 106.704L330.119 105.977C331.681 105.955 332.462 105.945 332.942 106.204C333.638 106.58 334.062 107.316 334.04 108.107C334.025 108.652 333.625 109.323 332.826 110.665L305.357 156.789C304.865 157.614 304.62 158.027 304.557 158.429C304.467 159.011 304.626 159.604 304.995 160.063C305.25 160.38 305.669 160.614 306.508 161.083L353.358 187.293C354.722 188.056 355.403 188.437 355.689 188.902C356.104 189.575 356.104 190.425 355.689 191.098C355.403 191.563 354.722 191.944 353.358 192.707L306.508 218.917C305.669 219.386 305.25 219.62 304.995 219.937C304.626 220.396 304.467 220.989 304.557 221.571C304.62 221.973 304.865 222.386 305.357 223.211L332.826 269.335C333.625 270.677 334.025 271.348 334.04 271.893C334.062 272.684 333.638 273.42 332.942 273.796C332.462 274.055 331.681 274.045 330.119 274.023L276.44 273.296C275.48 273.283 275 273.277 274.62 273.424C274.071 273.637 273.637 274.071 273.424 274.62C273.277 275 273.283 275.48 273.296 276.44L274.023 330.119C274.045 331.681 274.055 332.462 273.796 332.942C273.42 333.638 272.684 334.062 271.893 334.04C271.348 334.025 270.677 333.625 269.335 332.826L223.211 305.357C222.386 304.865 221.973 304.62 221.571 304.557C220.989 304.467 220.396 304.626 219.937 304.995C219.62 305.25 219.386 305.669 218.917 306.508L192.707 353.358C191.944 354.722 191.563 355.403 191.098 355.689C190.425 356.104 189.575 356.104 188.902 355.689C188.437 355.403 188.056 354.722 187.293 353.358L161.083 306.508C160.614 305.669 160.38 305.25 160.063 304.995C159.604 304.626 159.011 304.467 158.429 304.557C158.027 304.62 157.614 304.865 156.789 305.357L110.665 332.826C109.323 333.625 108.652 334.025 108.107 334.04C107.316 334.062 106.58 333.638 106.204 332.942C105.945 332.462 105.955 331.681 105.977 330.119L106.704 276.44C106.717 275.48 106.723 275 106.576 274.62C106.363 274.071 105.929 273.637 105.38 273.424C105 273.277 104.52 273.283 103.56 273.296L49.8814 274.023C48.3191 274.045 47.5379 274.055 47.0581 273.796C46.3624 273.42 45.9377 272.684 45.9598 271.893C45.9751 271.348 46.3748 270.677 47.1743 269.335L74.6431 223.211C75.1345 222.386 75.3802 221.973 75.4425 221.571C75.5326 220.989 75.3737 220.396 75.0046 219.937C74.7497 219.62 74.3306 219.386 73.4925 218.917L26.6421 192.707C25.2785 191.944 24.5966 191.563 24.3108 191.098C23.8964 190.425 23.8964 189.575 24.3108 188.902C24.5966 188.437 25.2785 188.056 26.6421 187.293L73.4925 161.083C74.3306 160.614 74.7497 160.38 75.0046 160.063C75.3737 159.604 75.5326 159.011 75.4425 158.429C75.3802 158.027 75.1345 157.614 74.6431 156.789L47.1743 110.665C46.3748 109.323 45.9751 108.652 45.9598 108.107C45.9377 107.316 46.3624 106.58 47.0581 106.204C47.5379 105.945 48.3191 105.955 49.8814 105.977L103.56 106.704C104.52 106.717 105 106.723 105.38 106.576C105.929 106.363 106.363 105.929 106.576 105.38C106.723 105 106.717 104.52 106.704 103.56L105.977 49.8814C105.955 48.3191 105.945 47.5379 106.204 47.0581C106.58 46.3624 107.316 45.9377 108.107 45.9598C108.652 45.9751 109.323 46.3748 110.665 47.1743L156.789 74.6431C157.614 75.1345 158.027 75.3802 158.429 75.4425C159.011 75.5326 159.604 75.3737 160.063 75.0046C160.38 74.7497 160.614 74.3306 161.083 73.4925L187.293 26.6421Z",
	oval: "M271.309 271.309C201.705 340.913 108.877 360.935 63.9707 316.029C19.0648 271.123 39.0867 178.295 108.691 108.691C178.295 39.0867 271.123 19.0648 316.029 63.9707C360.935 108.877 340.913 201.705 271.309 271.309Z",
	pentagon: "M155.064 49.459C176.093 34.1803 204.569 34.1803 225.598 49.459L322.926 120.171C343.955 135.45 352.754 162.532 344.722 187.253L307.546 301.668C299.514 326.39 276.476 343.127 250.483 343.127H130.18C104.186 343.127 81.1489 326.39 73.1164 301.668L35.9407 187.253C27.9082 162.532 36.7077 135.45 57.737 120.171L155.064 49.459Z",
	pill: "M116.116 71.7851C169.162 18.7383 255.168 18.7383 308.215 71.7851C361.262 124.832 361.262 210.838 308.215 263.884L263.884 308.215C210.838 361.262 124.832 361.262 71.7851 308.215C18.7383 255.168 18.7383 169.162 71.7851 116.116L116.116 71.7851Z",
	"soft-burst": "M175.147 33.1508C181.983 22.2831 198.017 22.2831 204.853 33.1508L221.238 59.2009C225.731 66.3458 234.797 69.2506 242.692 66.0751L271.475 54.4972C283.482 49.6671 296.455 58.9613 295.507 71.7154L293.235 102.288C292.612 110.673 298.215 118.278 306.494 120.284L336.681 127.601C349.275 130.653 354.23 145.692 345.861 155.461L325.8 178.877C320.298 185.3 320.298 194.7 325.8 201.123L345.861 224.539C354.23 234.308 349.275 249.347 336.681 252.399L306.494 259.716C298.215 261.722 292.612 269.327 293.235 277.712L295.507 308.285C296.455 321.039 283.482 330.333 271.475 325.503L242.692 313.925C234.797 310.749 225.731 313.654 221.238 320.799L204.853 346.849C198.017 357.717 181.983 357.717 175.147 346.849L158.762 320.799C154.269 313.654 145.203 310.749 137.308 313.925L108.525 325.503C96.5177 330.333 83.5454 321.039 84.4931 308.285L86.7649 277.712C87.388 269.327 81.785 261.722 73.5056 259.716L43.3186 252.399C30.7252 249.347 25.7702 234.308 34.1391 224.539L54.1997 201.123C59.7018 194.7 59.7018 185.3 54.1997 178.877L34.1391 155.461C25.7702 145.692 30.7252 130.653 43.3186 127.601L73.5056 120.284C81.785 118.278 87.388 110.673 86.7649 102.288L84.4931 71.7154C83.5454 58.9613 96.5177 49.6671 108.525 54.4972L137.308 66.0751C145.203 69.2506 154.269 66.3458 158.762 59.201L175.147 33.1508Z",
	sunny: "M276.453 68.8118C286.405 69.4881 291.381 69.8263 295.404 71.5853C301.223 74.1305 305.87 78.7766 308.415 84.5965C310.174 88.6186 310.512 93.5948 311.188 103.547L312.732 126.259C313.005 130.284 313.142 132.296 313.579 134.219C314.212 136.997 315.31 139.648 316.827 142.059C317.877 143.728 319.203 145.248 321.856 148.288L336.824 165.438C343.384 172.954 346.663 176.712 348.263 180.8C350.579 186.715 350.579 193.285 348.263 199.2C346.663 203.288 343.384 207.046 336.824 214.562L321.856 231.712C319.203 234.752 317.877 236.272 316.827 237.941C315.31 240.352 314.212 243.003 313.579 245.781C313.142 247.704 313.005 249.716 312.732 253.741L311.188 276.453C310.512 286.405 310.174 291.381 308.415 295.404C305.87 301.223 301.223 305.87 295.404 308.415C291.381 310.174 286.405 310.512 276.453 311.188L253.741 312.732C249.716 313.005 247.704 313.142 245.781 313.579C243.003 314.212 240.352 315.31 237.941 316.827C236.272 317.877 234.752 319.203 231.712 321.856L214.562 336.824C207.046 343.384 203.288 346.663 199.2 348.263C193.285 350.579 186.715 350.579 180.8 348.263C176.712 346.663 172.954 343.384 165.438 336.824L148.288 321.856C145.248 319.203 143.728 317.877 142.059 316.827C139.648 315.31 136.997 314.212 134.219 313.579C132.296 313.142 130.284 313.005 126.259 312.732L103.547 311.188C93.5947 310.512 88.6186 310.174 84.5965 308.415C78.7766 305.87 74.1305 301.223 71.5853 295.404C69.8263 291.381 69.4881 286.405 68.8118 276.453L67.2684 253.741C66.9949 249.716 66.8581 247.704 66.4206 245.781C65.7883 243.003 64.6903 240.352 63.173 237.941C62.123 236.272 60.7965 234.752 58.1437 231.712L43.1756 214.562C36.6164 207.046 33.3369 203.288 31.7366 199.2C29.4211 193.285 29.4211 186.715 31.7366 180.8C33.3369 176.712 36.6164 172.954 43.1756 165.438L58.1437 148.288C60.7965 145.248 62.123 143.728 63.173 142.059C64.6903 139.648 65.7883 136.997 66.4206 134.219C66.8581 132.296 66.9949 130.284 67.2684 126.259L68.8118 103.547C69.4881 93.5948 69.8263 88.6186 71.5853 84.5965C74.1305 78.7766 78.7766 74.1305 84.5965 71.5853C88.6186 69.8263 93.5948 69.4881 103.547 68.8118L126.259 67.2684C130.284 66.9949 132.296 66.8581 134.219 66.4206C136.997 65.7883 139.648 64.6903 142.059 63.173C143.728 62.123 145.248 60.7966 148.288 58.1437L165.438 43.1756C172.954 36.6164 176.712 33.3369 180.8 31.7366C186.715 29.4211 193.285 29.4211 199.2 31.7366C203.288 33.3369 207.046 36.6164 214.562 43.1756L231.712 58.1437C234.752 60.7966 236.272 62.123 237.941 63.173C240.352 64.6903 243.003 65.7883 245.781 66.4206C247.704 66.8581 249.716 66.9949 253.741 67.2684L276.453 68.8118Z",
	"very-sunny": "M166.725 43.1869C177.261 25.6044 202.739 25.6044 213.275 43.1868L225.124 62.9597C231.268 73.2136 243.399 78.2385 254.995 75.3327L277.355 69.7294C297.237 64.7468 315.253 82.7627 310.271 102.645L304.667 125.005C301.762 136.601 306.786 148.732 317.04 154.876L336.813 166.725C354.396 177.261 354.396 202.739 336.813 213.275L317.04 225.124C306.786 231.268 301.762 243.399 304.667 254.995L310.271 277.355C315.253 297.237 297.237 315.253 277.355 310.271L254.995 304.667C243.399 301.762 231.268 306.786 225.124 317.04L213.275 336.813C202.739 354.396 177.261 354.396 166.725 336.813L154.876 317.04C148.732 306.786 136.601 301.762 125.005 304.667L102.646 310.271C82.7627 315.253 64.7468 297.237 69.7294 277.355L75.3327 254.995C78.2385 243.399 73.2136 231.268 62.9597 225.124L43.1869 213.275C25.6044 202.739 25.6044 177.261 43.1868 166.725L62.9597 154.876C73.2136 148.732 78.2385 136.601 75.3327 125.005L69.7294 102.646C64.7468 82.7627 82.7627 64.7468 102.645 69.7294L125.005 75.3327C136.601 78.2385 148.732 73.2136 154.876 62.9597L166.725 43.1869Z",
	bun: "M38.875 113.322C38.875 69.5137 74.3887 34 118.197 34H261.803C305.611 34 341.125 69.5137 341.125 113.322C341.125 149.45 316.972 179.937 283.932 189.516C283.717 189.579 283.567 189.776 283.567 190C283.567 190.224 283.717 190.421 283.932 190.484C316.972 200.063 341.125 230.55 341.125 266.678C341.125 310.486 305.611 346 261.803 346H118.197C74.3887 346 38.875 310.486 38.875 266.678C38.875 230.785 62.7138 200.461 95.422 190.674C95.72 190.585 95.9264 190.311 95.9264 190C95.9264 189.689 95.72 189.415 95.422 189.326C62.7138 179.539 38.875 149.214 38.875 113.322Z",
	boom: "M186.738 36.413C187.51 30.2399 187.896 27.1534 188.483 26.6019C189.337 25.7994 190.663 25.7994 191.517 26.6019C192.104 27.1534 192.49 30.2399 193.262 36.413L203.079 114.907C203.39 117.398 203.546 118.644 203.993 119.158C204.642 119.904 205.703 120.131 206.598 119.715C207.214 119.428 207.861 118.353 209.153 116.205L249.875 48.5119C253.078 43.1883 254.679 40.5264 255.438 40.2626C256.543 39.8785 257.754 40.4209 258.21 41.5032C258.522 42.247 257.626 45.2245 255.834 51.1796L233.049 126.902C232.326 129.305 231.964 130.507 232.165 131.159C232.456 132.107 233.333 132.748 234.319 132.733C234.998 132.723 236.023 132.006 238.073 130.572L302.659 85.3844C307.738 81.8306 310.278 80.0538 311.078 80.1231C312.243 80.2241 313.13 81.2151 313.108 82.39C313.093 83.1974 311.07 85.5511 307.024 90.2585L255.576 150.116C253.943 152.016 253.126 152.966 253.045 153.644C252.928 154.628 253.47 155.573 254.377 155.963C255.001 156.231 256.228 155.995 258.681 155.524L335.963 140.655C342.041 139.485 345.08 138.901 345.783 139.291C346.806 139.86 347.215 141.128 346.72 142.192C346.38 142.924 343.579 144.247 337.979 146.893L266.764 180.536C264.503 181.604 263.373 182.137 263.025 182.724C262.52 183.575 262.633 184.66 263.304 185.387C263.765 185.887 264.981 186.173 267.413 186.745L344.029 204.766C350.054 206.184 353.067 206.892 353.551 207.537C354.256 208.474 354.117 209.8 353.234 210.57C352.627 211.099 349.534 211.163 343.347 211.289L264.679 212.901C262.182 212.952 260.933 212.977 260.378 213.371C259.572 213.942 259.237 214.979 259.555 215.917C259.774 216.563 260.77 217.322 262.76 218.839L325.462 266.634C330.393 270.393 332.858 272.272 333.04 273.059C333.304 274.203 332.641 275.358 331.523 275.7C330.755 275.935 327.903 274.728 322.2 272.314L249.681 241.615C247.379 240.641 246.228 240.153 245.562 240.286C244.595 240.478 243.869 241.288 243.78 242.275C243.719 242.955 244.321 244.055 245.526 246.255L283.472 315.559C286.456 321.01 287.948 323.735 287.796 324.528C287.574 325.682 286.501 326.466 285.341 326.321C284.544 326.221 282.428 323.952 278.194 319.415L224.364 261.714C222.655 259.882 221.801 258.966 221.139 258.815C220.177 258.594 219.187 259.038 218.706 259.904C218.375 260.5 218.48 261.751 218.691 264.253L225.32 343.084C225.841 349.283 226.102 352.383 225.642 353.045C224.973 354.009 223.676 354.286 222.675 353.679C221.987 353.263 220.971 350.324 218.939 344.447L193.105 269.721C192.285 267.349 191.875 266.163 191.332 265.754C190.542 265.16 189.458 265.16 188.668 265.754C188.125 266.163 187.715 267.349 186.895 269.721L161.061 344.447C159.029 350.324 158.013 353.263 157.325 353.679C156.324 354.286 155.027 354.009 154.358 353.045C153.898 352.383 154.159 349.283 154.68 343.084L161.309 264.253C161.52 261.751 161.625 260.5 161.294 259.904C160.813 259.038 159.823 258.594 158.861 258.815C158.199 258.966 157.345 259.882 155.636 261.714L101.806 319.415C97.5724 323.952 95.4556 326.221 94.6586 326.321C93.4987 326.466 92.4257 325.682 92.2041 324.528C92.0519 323.735 93.544 321.01 96.5282 315.559L134.474 246.255C135.679 244.055 136.281 242.955 136.22 242.275C136.131 241.288 135.405 240.478 134.438 240.286C133.772 240.153 132.621 240.641 130.319 241.615L57.8002 272.314C52.097 274.728 49.2455 275.935 48.4771 275.7C47.3589 275.358 46.6958 274.203 46.9601 273.059C47.1418 272.272 49.6074 270.393 54.5385 266.634L117.24 218.839C119.23 217.322 120.226 216.563 120.445 215.917C120.763 214.979 120.428 213.942 119.622 213.371C119.067 212.977 117.818 212.952 115.321 212.901L36.6531 211.289C30.4664 211.163 27.373 211.099 26.7662 210.57C25.8831 209.8 25.7445 208.474 26.449 207.537C26.9332 206.892 29.9459 206.184 35.9713 204.766L112.587 186.745C115.019 186.173 116.235 185.887 116.697 185.387C117.367 184.66 117.48 183.575 116.975 182.724C116.627 182.137 115.497 181.604 113.236 180.536L42.0212 146.893C36.4205 144.247 33.6202 142.924 33.2799 142.192C32.7846 141.128 33.1944 139.86 34.2174 139.291C34.9204 138.901 37.9592 139.485 44.037 140.655L121.319 155.524C123.772 155.995 124.999 156.231 125.623 155.963C126.53 155.573 127.072 154.628 126.955 153.644C126.874 152.966 126.057 152.016 124.424 150.116L72.9761 90.2585C68.93 85.5511 66.907 83.1974 66.8919 82.39C66.87 81.2151 67.7575 80.2241 68.922 80.1231C69.7222 80.0538 72.2618 81.8306 77.3411 85.3844L141.927 130.572C143.977 132.006 145.002 132.723 145.681 132.733C146.667 132.748 147.544 132.107 147.835 131.159C148.036 130.507 147.674 129.305 146.951 126.902L124.166 51.1796C122.374 45.2245 121.478 42.247 121.79 41.5032C122.246 40.4209 123.457 39.8785 124.562 40.2626C125.321 40.5264 126.922 43.1883 130.125 48.5119L170.847 116.205C172.139 118.354 172.786 119.428 173.402 119.715C174.297 120.131 175.358 119.904 176.007 119.158C176.454 118.644 176.61 117.398 176.921 114.907L186.738 36.413Z",
	arch: "M345 293.727C345 299.96 345 303.076 344.689 305.696C342.259 326.14 326.14 342.259 305.696 344.689C303.076 345 299.96 345 293.727 345H86.2732C80.0405 345 76.9242 345 74.3043 344.689C53.8596 342.259 37.7407 326.14 35.3113 305.696C35 303.076 35 299.96 35 293.727L35 190C35 104.396 104.396 35 190 35C275.604 35 345 104.396 345 190V293.727Z",
	arrow: "M262.422 104.907C253.213 90.2936 243.84 75.4777 231.389 63.8739C218.938 52.2412 202.808 44.023 186.27 45.0937C171.748 46.0486 158.288 54.0932 147.662 64.5974C137.036 75.1015 128.781 88.0365 120.661 100.856C98.7011 135.464 76.7136 170.073 54.7533 204.711C44.3181 221.147 33.6105 238.307 30.7224 257.898C27.2349 281.568 36.6348 305.673 53.3093 321.415C70.7467 337.88 99.0008 336.549 120.225 332.035C143.493 327.086 166.244 317.769 189.975 317.797C210.301 317.797 229.945 324.685 249.726 329.691C269.479 334.668 290.704 337.735 309.776 330.327C333.453 321.154 350.454 295.082 349.991 268.315C349.555 243.892 323.508 201.759 323.508 201.759C323.508 201.759 282.78 137.194 262.422 104.907Z",
	diamond: "M247.442 306.481C230.606 328.514 222.188 339.53 212.466 344.548C198.383 351.817 181.617 351.817 167.534 344.548C157.812 339.53 149.394 328.514 132.558 306.481L76.646 233.308C66.4059 219.906 61.2859 213.206 58.7505 206.032C55.0832 195.654 55.0832 184.346 58.7505 173.968C61.2859 166.794 66.4059 160.094 76.6461 146.692L132.558 73.5194C149.394 51.4863 157.812 40.4697 167.534 35.4518C181.617 28.1827 198.383 28.1827 212.466 35.4518C222.188 40.4697 230.606 51.4863 247.442 73.5195L303.354 146.692C313.594 160.094 318.714 166.794 321.249 173.968C324.917 184.346 324.917 195.654 321.249 206.032C318.714 213.206 313.594 219.906 303.354 233.308L247.442 306.481Z",
	fan: "M40 87.3684C40 85.697 40 84.8613 40.0219 84.1547C40.7686 60.0969 60.0969 40.7686 84.1547 40.0219C84.8613 40 85.697 40 87.3684 40C96.2825 40 100.74 40 104.508 40.117C232.816 44.0992 335.901 147.184 339.883 275.492C340 279.26 340 283.717 340 292.632C340 294.303 340 295.139 339.978 295.845C339.231 319.903 319.903 339.231 295.845 339.978C295.139 340 294.303 340 292.632 340H107.441C87.0642 340 76.8757 340 68.7935 336.8C57.0947 332.168 47.8318 322.905 43.2 311.207C40 303.124 40 292.936 40 272.559V87.3684Z",
	flower: "M303.126 76.8625C291.251 64.988 264.194 70.8597 231.547 89.6813C221.771 53.2855 206.79 30 189.996 30C173.203 30 158.223 53.2841 148.447 89.6779C115.802 70.8581 88.7463 64.9874 76.8722 76.8614C64.997 88.7367 70.8701 115.796 89.6945 148.446C53.2912 158.222 30 173.204 30 190C30 206.793 53.283 221.773 89.6753 231.549C70.8495 264.2 64.9757 291.261 76.8513 303.137C88.7274 315.013 115.79 309.138 148.444 290.31C158.22 326.711 173.201 350 189.996 350C206.792 350 221.774 326.709 231.55 290.307C264.205 309.136 291.27 315.012 303.146 303.136C315.022 291.26 309.148 264.2 290.323 231.549C326.716 221.773 350 206.793 350 190C350 173.204 326.708 158.222 290.304 148.446C309.128 115.797 315.001 88.7376 303.126 76.8625Z",
	gem: "M133.415 65.7498C150.981 52.991 159.763 46.6116 169.124 43.4402C182.663 38.8533 197.337 38.8533 210.876 43.4402C220.237 46.6116 229.019 52.991 246.585 65.7498L286.083 94.4402C296.514 102.017 301.729 105.805 305.981 110.353C312.125 116.926 316.839 124.702 319.823 133.189C321.888 139.062 322.834 145.437 324.725 158.189L332.038 207.501C335.355 229.87 337.014 241.055 335.34 251.136C332.918 265.718 325.6 279.04 314.59 288.907C306.979 295.728 296.649 300.329 275.987 309.532L229.176 330.382C216.707 335.935 210.473 338.712 204.064 340.131C194.8 342.182 185.2 342.182 175.936 340.131C169.527 338.712 163.293 335.935 150.824 330.382L104.013 309.532C83.3515 300.329 73.0207 295.728 65.4097 288.907C54.4005 279.04 47.0819 265.718 44.6602 251.136C42.9859 241.055 44.6447 229.87 47.9621 207.501L55.2752 158.189C57.1662 145.437 58.1117 139.062 60.1768 133.189C63.1612 124.702 67.8747 116.926 74.0191 110.353C78.2706 105.805 83.4862 102.017 93.9172 94.4402L133.415 65.7498Z",
	"ghost-ish": "M40 182.857C40 103.959 107.157 40 190 40C272.843 40 340 103.959 340 182.857L340 282.857C340 314.416 313.137 340 280 340C270.178 340 260.907 337.752 252.724 333.768C248.554 331.737 244.394 329.512 240.216 327.277C225.513 319.411 210.592 311.429 194.27 311.429H185.73C169.408 311.429 154.487 319.411 139.784 327.277C135.606 329.512 131.446 331.737 127.276 333.768C119.093 337.752 109.822 340 100 340C66.8629 340 40 314.416 40 282.857L40 182.857Z",
	heart: "M281.1 47.134C259.439 47.134 240.113 57.3021 227.481 73.1973L190 114.513V114.379L152.519 73.0633C139.887 57.1681 120.561 47 98.8995 47C60.8478 47 30 78.3656 30 117.059C30 151.058 58.3751 176.471 78.9187 200.884C99.3895 225.205 119.856 249.523 140.323 273.844L186.272 328.437C187.516 329.914 188.756 331.388 190 332.866V333C191.244 331.522 192.484 330.048 193.728 328.571C209.043 310.375 224.358 292.178 239.677 273.978C260.144 249.657 280.614 225.339 301.081 201.018C321.625 176.608 350 151.196 350 117.193C350 78.4996 319.152 47.134 281.1 47.134Z",
	hexagon: "M360.196 147.081C367.995 160.591 371.894 167.346 373.864 174.403C376.712 184.606 376.712 195.394 373.864 205.597C371.894 212.654 367.995 219.409 360.196 232.919L336.437 274.079C328.638 287.589 324.738 294.344 319.613 299.58C312.202 307.148 302.86 312.543 292.601 315.177C285.505 317 277.706 317 262.108 317L117.892 317C102.294 317 94.4949 317 87.399 315.178C77.1398 312.543 67.798 307.149 60.3873 299.58C55.2616 294.345 51.3621 287.589 43.5632 274.079L19.8039 232.919C12.0054 219.409 8.10612 212.654 6.13613 205.597C3.28796 195.394 3.28796 184.606 6.13613 174.403C8.10613 167.346 12.0054 160.591 19.804 147.081L43.5632 105.921C51.3622 92.4107 55.2616 85.6554 60.3874 80.4202C67.798 72.8514 77.1398 67.457 87.3989 64.8224C94.4949 63.0002 102.294 63.0002 117.892 63.0002L262.108 63C277.706 63 285.505 63 292.601 64.8222C302.86 67.4567 312.202 72.8511 319.613 80.42C324.738 85.6551 328.638 92.4104 336.437 105.921L360.196 147.081Z",
	"pixel-circle": "M253.556 34H126.444V54.2217H83.1111V80.2222H57.1111V126.444H34V253.556H57.1111V299.778H83.1111V325.777H126.444V346H253.556V325.777H296.889V299.778H322.889V253.556H346V126.444H322.889V80.2222H296.889V54.2217H253.556V34Z",
	"pixel-triangle": "M121.641 30H66V350H121.641V322.174H164.564V295.739H209.077V265.13H245.641V240.086H282.205V209.478H314V170.522H282.205V139.912H245.641V114.869H209.077V84.2609H164.564V57.8262H121.641V30Z",
	puffy: "M371.982 187.559C371.686 165.488 353.541 147.816 331.439 148.112C330.218 148.112 329.016 148.204 327.832 148.334C328.757 147.576 329.663 146.799 330.532 145.949C346.365 130.551 346.698 105.226 331.291 89.4031C330.384 88.4788 329.478 87.5546 328.553 86.6673C312.721 71.2692 287.4 71.6019 271.974 87.4252C271.123 88.294 270.347 89.1997 269.588 90.124C269.718 88.9409 269.792 87.7394 269.81 86.5194C270.125 64.4482 252.443 46.3142 230.359 46C229.711 46 229.064 46 228.417 46H227.103C210.734 46 196.696 55.8341 190.5 69.9013C184.304 55.8341 170.247 46 153.897 46H152.583C151.936 46 151.289 46 150.641 46C128.557 46.2958 110.875 64.4297 111.171 86.5194C111.171 87.7394 111.264 88.9409 111.393 90.124C110.635 89.1997 109.858 88.294 109.007 87.4252C93.6001 71.6019 68.2608 71.2692 52.4283 86.6673C51.5035 87.573 50.5787 88.4788 49.6909 89.4031C34.2839 105.226 34.6168 130.532 50.4493 145.949C51.3186 146.799 52.2249 147.576 53.1497 148.334C51.9659 148.204 50.7637 148.13 49.543 148.112C27.4589 147.816 9.31443 165.488 9 187.559C9 188.206 9 188.853 9 189.5C9 190.147 9 190.794 9 191.441C9.29593 213.512 27.4404 231.184 49.543 230.888C50.7637 230.888 51.9659 230.796 53.1497 230.666C52.2249 231.424 51.3186 232.201 50.4493 233.051C34.6168 248.449 34.2839 273.774 49.6909 289.597C50.5972 290.521 51.5035 291.445 52.4283 292.333C68.2608 307.731 93.5816 307.398 109.007 291.575C109.858 290.706 110.635 289.8 111.393 288.876C111.264 290.059 111.19 291.261 111.171 292.481C110.875 314.552 128.539 332.704 150.641 333C151.289 333 151.936 333 152.583 333H153.897C170.266 333 184.304 323.166 190.5 309.099C196.696 323.166 210.753 333 227.103 333H228.417C229.064 333 229.711 333 230.359 333C252.443 332.704 270.125 314.57 269.829 292.481C269.829 291.261 269.736 290.059 269.607 288.876C270.365 289.8 271.142 290.706 271.993 291.575C287.4 307.398 312.739 307.731 328.572 292.333C329.496 291.427 330.421 290.521 331.309 289.597C346.716 273.774 346.383 248.468 330.551 233.051C329.681 232.201 328.775 231.424 327.85 230.666C329.034 230.796 330.236 230.87 331.457 230.888C353.541 231.184 371.704 213.531 372 191.441C372 190.794 372 190.147 372 189.5C372 188.853 372 188.206 372 187.559H371.982Z",
	"puffy-diamond": "M279.397 279.754C291.485 267.666 295.573 250.608 291.663 235.157C307.124 239.086 324.202 235.001 336.3 222.902C354.567 204.636 354.567 175.02 336.3 156.754C324.273 144.727 307.325 140.619 291.936 144.43C295.368 129.286 291.188 112.766 279.397 100.975C267.282 88.8601 250.174 84.7804 234.696 88.736C238.798 73.1705 234.747 55.9026 222.544 43.6997C204.278 25.4334 174.662 25.4334 156.396 43.6997C144.266 55.83 140.191 72.9655 144.172 88.4584C129.001 84.9862 112.435 89.1585 100.618 100.975C88.8257 112.767 84.6462 129.289 88.0792 144.434C72.6865 140.616 55.7312 144.723 43.6997 156.754C25.4334 175.02 25.4334 204.636 43.6997 222.902C55.8026 235.005 72.8879 239.089 88.3531 235.153C84.4405 250.605 88.5288 267.665 100.618 279.754C112.385 291.521 128.862 295.708 143.98 292.314C140.328 307.602 144.467 324.371 156.396 336.3C174.662 354.567 204.278 354.567 222.544 336.3C234.544 324.301 238.661 307.403 234.894 292.043C250.32 295.914 267.333 291.817 279.397 279.754Z",
	semicircle: "M350 256.921C350 275.19 335.19 290 316.921 290L63.0794 290C44.8102 290 30 275.19 30 256.921L30 250C30 161.634 101.634 90 190 90C278.366 90 350 161.634 350 250V256.921Z",
	slanted: "M45.7147 120.543C48.289 95.9642 49.5761 83.6748 54.3025 73.9777C61.142 59.9453 73.0768 49.1101 87.6278 43.7229C97.6833 40 109.933 40 134.432 40H258.137C287.764 40 302.578 40 313.887 44.8539C330.261 51.8819 342.72 65.8495 347.927 83.0153C351.523 94.8709 349.967 109.733 346.854 139.457L334.285 259.457C331.711 284.036 330.424 296.325 325.697 306.022C318.858 320.055 306.923 330.89 292.372 336.277C282.317 340 270.067 340 245.568 340H121.863C92.2359 340 77.4221 340 66.1134 335.146C49.7395 328.118 37.2802 314.151 32.0729 296.985C28.4765 285.129 30.0331 270.267 33.1463 240.543L45.7147 120.543Z",
	"soft-boom": "M204.57 116.822C226.539 6.39254 153.487 6.39254 175.457 116.822C153.487 6.39254 85.9934 34.3433 148.549 127.966C85.9934 34.3433 34.3433 85.9934 127.966 148.549C34.3433 85.9934 6.39254 153.487 116.822 175.444C6.39254 153.474 6.39254 226.526 116.822 204.556C6.39254 226.526 34.3565 294.007 127.966 231.451C34.3565 294.007 86.0066 345.657 148.549 252.034C85.9934 345.644 153.487 373.594 175.444 263.178C153.474 373.607 226.526 373.607 204.556 263.178C226.526 373.607 294.007 345.644 231.451 252.034C294.007 345.644 345.657 293.993 252.034 231.451C345.644 294.007 373.594 226.513 263.178 204.556C373.607 226.526 373.607 153.474 263.178 175.444C373.607 153.474 345.644 85.9934 252.034 148.549C345.644 85.9934 293.993 34.3433 231.451 127.966C294.007 34.3565 226.513 6.40574 204.556 116.822H204.57Z",
	square: "M350 202C350 246.717 350 269.076 342.978 286.812C332.813 312.485 312.485 332.813 286.812 342.978C269.076 350 246.717 350 202 350H178C133.283 350 110.924 350 93.1875 342.978C67.5145 332.813 47.187 312.485 37.0223 286.812C30 269.076 30 246.717 30 202L30 178C30 133.283 30 110.924 37.0224 93.1875C47.187 67.5145 67.5146 47.187 93.1876 37.0223C110.924 30 133.283 30 178 30L202 30C246.717 30 269.076 30 286.812 37.0224C312.485 47.187 332.813 67.5146 342.978 93.1876C350 110.924 350 133.283 350 178V202Z",
	circle: "M350 190C350 278.366 278.366 350 190 350C101.634 350 30 278.366 30 190C30 101.634 101.634 30 190 30C278.366 30 350 101.634 350 190Z",
	triangle: "M136.461 98.2021C151.597 71.8217 159.165 58.6315 168.342 53.0594C181.648 44.9802 198.352 44.9802 211.658 53.0594C220.835 58.6315 228.403 71.8217 243.539 98.2021L325.309 240.72C340.3 266.847 347.795 279.911 347.993 290.585C348.28 306.06 339.949 320.418 326.364 327.864C316.994 333 301.919 333 271.77 333H108.23C78.0806 333 63.006 333 53.636 327.864C40.0505 320.418 31.7204 306.06 32.0072 290.585C32.2049 279.911 39.7003 266.847 54.6909 240.72L136.461 98.2021Z"
}, gv = [], _v = /* @__PURE__ */ new Map();
for (let e in hv) gv.push(hv[e]), _v.set(e, gv.length - 1);
var vv = {};
gv = It(gv, 300);
for (let e of _v) vv[e[0]] = c(gv[e[1]]);
var yv = vv;
function bv(e) {
	return l`:host([name="${c(e)}"]) .wrapper { clip-path: polygon(${yv[e]}); }`;
}
var xv = [...Object.getOwnPropertyNames(yv).map((e) => bv(e))], Sv = class extends x {
	constructor() {
		super(...arguments), this.name = null;
	}
	render() {
		return y`<div class="wrapper"><slot></slot></div>`;
	}
};
Sv.styles = [mv, xv], e([S({ reflect: !0 })], Sv.prototype, "name", void 0), Sv = e([w("m3e-shape")], Sv);
//#endregion
//#region node_modules/@m3e/web/dist/switch.js
var J = {
	selectedIconColor: c(`var(--m3e-switch-selected-icon-color, ${A.color.primary})`),
	selectedIconSize: c("var(--m3e-switch-selected-icon-size, 16px)"),
	unselectedIconColor: c(`var(--m3e-switch-unselected-icon-color, ${A.color.surfaceContainerHighest})`),
	unselectedIconSize: c("var(--m3e-switch-unselected-icon-size, 16px)"),
	trackHeight: c(`calc(var(--m3e-switch-track-height, 32px) + ${A.density.calc(-3)})`),
	trackWidth: c(`calc(var(--m3e-switch-track-width, 52px) + ${A.density.calc(-3)})`),
	trackOutlineColor: c(`var(--m3e-switch-track-outline-color, ${A.color.outline})`),
	trackOutlineWidth: c("var(--m3e-switch-track-outline-width, 2px)"),
	trackShape: c(`var(--m3e-switch-track-shape, ${A.shape.corner.full})`),
	selectedTrackColor: c(`var(--m3e-switch-selected-track-color, ${A.color.primary})`),
	unselectedTrackColor: c(`var(--m3e-switch-unselected-track-color, ${A.color.surfaceContainerHighest})`),
	unselectedHandleHeight: c(`calc(var(--m3e-switch-unselected-handle-height, 16px) + ${A.density.calc(-3)})`),
	unselectedHandleWidth: c(`calc(var(--m3e-switch-unselected-handle-width, 16px) + ${A.density.calc(-3)})`),
	withIconHandleHeight: c(`calc(var(--m3e-switch-with-icon-handle-height, 24px) + ${A.density.calc(-3)})`),
	withIconHandleWidth: c(`calc(var(--m3e-switch-with-icon-handle-width, 24px) + ${A.density.calc(-3)})`),
	selectedHandleHeight: c(`calc(var(--m3e-switch-selected-handle-height, 24px) + ${A.density.calc(-3)})`),
	selectedHandleWidth: c(`calc(var(--m3e-switch-selected-handle-width, 24px) + ${A.density.calc(-3)})`),
	pressedHandleHeight: c(`calc(var(--m3e-switch-pressed-handle-height, 28px) + ${A.density.calc(-3)})`),
	pressedHandleWidth: c(`calc(var(--m3e-switch-pressed-handle-width, 28px) + ${A.density.calc(-3)})`),
	handleShape: c(`var(--m3e-switch-handle-shape, ${A.shape.corner.full})`),
	selectedHandleColor: c(`var(--m3e-switch-selected-handle-color, ${A.color.onPrimary})`),
	unselectedHandleColor: c(`var(--m3e-switch-unselected-handle-color, ${A.color.outline})`),
	stateLayerSize: c(`calc(var(--m3e-switch-state-layer-size, 40px) + ${A.density.calc(-3)})`),
	stateLayerShape: c(`var(--m3e-switch-state-layer-shape, ${A.shape.corner.full})`),
	disabledSelectedIconColor: c(`var(--m3e-switch-disabled-selected-icon-color, ${A.color.onSurface})`),
	disabledSelectedIconOpacity: c("var(--m3e-switch-disabled-selected-icon-opacity, 38%)"),
	disabledUnselectedIconColor: c(`var(--m3e-switch-disabled-unselected-icon-color, ${A.color.surfaceContainerHighest})`),
	disabledUnselectedIconOpacity: c("var(--m3e-switch-disabled-unselected-icon-opacity, 38%)"),
	disabledTrackOpacity: c("var(--m3e-switch-disabled-track-opacity, 12%)"),
	disabledSelectedTrackColor: c(`var(--m3e-switch-disabled-selected-track-color, ${A.color.onSurface})`),
	disabledUnselectedTrackColor: c(`var(--m3e-switch-disabled-unselected-track-color, ${A.color.surfaceContainerHighest})`),
	disabledUnselectedTrackOutlineColor: c(`var(--m3e-switch-disabled-unselected-track-outline-color, ${A.color.onSurface})`),
	disabledUnselectedHandleOpacity: c("var(--m3e-switch-disabled-unselected-handle-opacity, 38%)"),
	disabledSelectedHandleOpacity: c("var(--m3e-switch-disabled-selected-handle-opacity, 100%)"),
	disabledSelectedHandleColor: c(`var(--m3e-switch-disabled-selected-handle-color, ${A.color.surface})`),
	disabledUnselectedHandleColor: c(`var(--m3e-switch-disabled-unselected-handle-color, ${A.color.onSurface})`),
	selectedHoverIconColor: c(`var(--m3e-switch-selected-hover-icon-color, ${A.color.onPrimaryContainer})`),
	unselectedHoverIconColor: c(`var(--m3e-switch-unselected-hover-icon-color, ${A.color.surfaceContainerHighest})`),
	selectedHoverTrackColor: c(`var(--m3e-switch-selected-hover-track-color, ${A.color.primary})`),
	selectedHoverStateLayerColor: c(`var(--m3e-switch-selected-hover-state-layer-color, ${A.color.primary})`),
	selectedHoverStateLayerOpacity: c("var(--m3e-switch-selected-hover-state-layer-opacity, 8%)"),
	unselectedHoverTrackColor: c(`var(--m3e-switch-unselected-hover-track-color, ${A.color.surfaceContainerHighest})`),
	unselectedHoverTrackOutlineColor: c(`var(--m3e-switch-unselected-hover-track-outline-color, ${A.color.outline})`),
	unselectedHoverStateLayerColor: c(`var(--m3e-switch-unselected-hover-state-layer-color, ${A.color.onSurface})`),
	unselectedHoverStateLayerOpacity: c("var(--m3e-switch-unselected-hover-state-layer-opacity, 8%)"),
	selectedHoverHandleColor: c(`var(--m3e-switch-selected-hover-handle-color, ${A.color.primaryContainer})`),
	unselectedHoverHandleColor: c(`var(--m3e-switch-unselected-hover-handle-color, ${A.color.onSurfaceVariant})`),
	selectedFocusIconColor: c(`var(--m3e-switch-selected-focus-icon-color, ${A.color.onPrimaryContainer})`),
	unselectedFocusIconColor: c(`var(--m3e-switch-unselected-focus-icon-color, ${A.color.surfaceContainerHighest})`),
	selectedFocusTrackColor: c(`var(--m3e-switch-selected-focus-track-color, ${A.color.primary})`),
	selectedFocusStateLayerColor: c(`var(--m3e-switch-selected-focus-state-layer-color, ${A.color.primary})`),
	selectedFocusStateLayerOpacity: c("var(--m3e-switch-selected-focus-state-layer-opacity, 10%)"),
	unselectedFocusTrackColor: c(`var(--m3e-switch-unselected-focus-track-color, ${A.color.surfaceContainerHighest})`),
	unselectedFocusTrackOutlineColor: c(`var(--m3e-switch-unselected-focus-track-outline-color, ${A.color.outline})`),
	unselectedFocusStateLayerColor: c(`var(--m3e-switch-unselected-focus-state-layer-color, ${A.color.onSurface})`),
	unselectedFocusStateLayerOpacity: c("var(--m3e-switch-unselected-focus-state-layer-opacity, 10%)"),
	selectedFocusHandleColor: c(`var(--m3e-switch-selected-focus-handle-color, ${A.color.primaryContainer})`),
	unselectedFocusHandleColor: c(`var(--m3e-switch-unselected-focus-handle-color, ${A.color.onSurfaceVariant})`),
	selectedPressedIconColor: c(`var(--m3e-switch-selected-pressed-icon-color, ${A.color.onPrimaryContainer})`),
	unselectedPressedIconColor: c(`var(--m3e-switch-unselected-pressed-icon-color, ${A.color.surfaceContainerHighest})`),
	selectedPressedTrackColor: c(`var(--m3e-switch-selected-pressed-track-color, ${A.color.primary})`),
	selectedPressedStateLayerColor: c(`var(--m3e-switch-selected-pressed-state-layer-color, ${A.color.primary})`),
	selectedPressedStateLayerOpacity: c("var(--m3e-switch-selected-pressed-state-layer-opacity, 10%)"),
	unselectedPressedTrackColor: c(`var(--m3e-switch-unselected-pressed-track-color, ${A.color.surfaceContainerHighest})`),
	unselectedPressedTrackOutlineColor: c(`var(--m3e-switch-unselected-pressed-track-outline-color, ${A.color.outline})`),
	unselectedPressedStateLayerColor: c(`var(--m3e-switch-unselected-pressed-state-layer-color, ${A.color.onSurface})`),
	unselectedPressedStateLayerOpacity: c("var(--m3e-switch-unselected-pressed-state-layer-opacity, 10%)"),
	selectedPressedHandleColor: c(`var(--m3e-switch-selected-pressed-handle-color, ${A.color.primaryContainer})`),
	unselectedPressedHandleColor: c(`var(--m3e-switch-unselected-pressed-handle-color, ${A.color.onSurfaceVariant})`)
}, Cv = l`.handle { position: relative; display: flex; align-items: center; justify-content: center; pointer-events: none; transform-origin: center center; border-radius: ${J.handleShape}; transition: ${c(`background-color ${A.motion.duration.short4} ${A.motion.easing.standard},
      transform var(--_switch-handle-effect),
      width ${A.motion.spring.fastEffects},
      height ${A.motion.spring.fastEffects}`)}; } .track:not(.pressed) .handle { --_switch-handle-effect: ${A.motion.spring.fastSpatial}; } .track.pressed .handle { --_switch-handle-effect: ${A.motion.spring.fastEffects}; } :host(:not([aria-disabled="true"]):not([checked]):is(:state(--icons-both), :--icons-both)) .track:not(.pressed) .handle { width: ${J.withIconHandleWidth}; height: ${J.withIconHandleHeight}; } :host(:not([checked]):not(:is(:state(--icons-both), :--icons-both))) .track:not(.pressed) .handle, :host([aria-disabled="true"]:not([checked])) .handle { width: ${J.unselectedHandleWidth}; height: ${J.unselectedHandleHeight}; } :host([checked]) .track:not(.pressed) .handle { width: ${J.selectedHandleWidth}; height: ${J.selectedHandleHeight}; } .track.pressed .handle { width: ${J.pressedHandleWidth}; height: ${J.pressedHandleHeight}; } :host(:not([aria-disabled="true"]):not([checked]):not(:focus):not(:hover)) .track:not(.pressed) .handle { background-color: ${J.unselectedHandleColor}; } :host(:not([aria-disabled="true"]):not([checked]):not(:focus):hover) .track:not(.pressed) .handle { background-color: ${J.unselectedHoverHandleColor}; } :host(:not([aria-disabled="true"]):not([checked]):focus) .track:not(.pressed) .handle { background-color: ${J.unselectedFocusHandleColor}; } :host(:not([aria-disabled="true"]):not([checked])) .track.pressed .handle { background-color: ${J.unselectedPressedHandleColor}; } :host(:not([aria-disabled="true"])[checked]:not(:focus):not(:hover)) .track:not(.pressed) .handle { background-color: ${J.selectedHandleColor}; } :host(:not([aria-disabled="true"])[checked]:not(:focus):hover) .track:not(.pressed) .handle { background-color: ${J.selectedHoverHandleColor}; } :host(:not([aria-disabled="true"])[checked]:focus) .track:not(.pressed) .handle { background-color: ${J.selectedFocusHandleColor}; } :host(:not([aria-disabled="true"])[checked]) .track.pressed .handle { background-color: ${J.selectedPressedHandleColor}; } :host([aria-disabled="true"]:not([checked])) .handle { background-color: color-mix( in srgb, ${J.disabledUnselectedHandleColor} ${J.disabledUnselectedHandleOpacity}, transparent ); } :host([aria-disabled="true"][checked]) .handle { background-color: color-mix( in srgb, ${J.disabledSelectedHandleColor} ${J.disabledSelectedHandleOpacity}, transparent ); } :host(:not(:is(:state(--rtl), :--rtl))[checked]) .track:not(.pressed) .handle { transform: translateX( calc(${J.trackWidth} - ${J.selectedHandleWidth} - calc(${J.trackOutlineWidth} * 2)) ); } :host(:is(:state(--rtl), :--rtl)[checked]) .track:not(.pressed) .handle { transform: translateX( calc( 0px - calc( ${J.trackWidth} - ${J.selectedHandleWidth} - calc(${J.trackOutlineWidth} * 2) ) ) ); } :host(:not(:is(:state(--rtl), :--rtl))[checked]) .track.pressed .handle { transform: translateX( calc(${J.trackWidth} - ${J.pressedHandleWidth} - ${J.trackOutlineWidth}) ); } :host(:is(:state(--rtl), :--rtl)[checked]) .track.pressed .handle { transform: translateX( calc(0px - calc(${J.trackWidth} - ${J.pressedHandleWidth} - ${J.trackOutlineWidth})) ); } :host(:not(:is(:state(--rtl), :--rtl)):not([checked]):not(:is(:state(--icons-both), :--icons-both))) .track:not(.pressed) .handle, :host(:not(:is(:state(--rtl), :--rtl))[aria-disabled="true"]:not([checked])) .handle { transform: translateX( calc( ${J.trackOutlineWidth} + calc(${J.pressedHandleWidth} - ${J.withIconHandleWidth}) ) ); } :host(:is(:state(--rtl), :--rtl):not([checked]):not(:is(:state(--icons-both), :--icons-both))) .track:not(.pressed) .handle, :host(:is(:state(--rtl), :--rtl)[aria-disabled="true"]:not([checked])) .handle { transform: translateX( calc( 0px - calc( ${J.trackOutlineWidth} + calc(${J.pressedHandleWidth} - ${J.withIconHandleWidth}) ) ) ); } :host( :not(:is(:state(--rtl), :--rtl)):not([aria-disabled="true"]):not([checked]):is( :state(--icons-both), :--icons-both ) ) .track:not(.pressed) .handle { transform: translateX(${J.trackOutlineWidth}); } :host(:is(:state(--rtl), :--rtl):not([aria-disabled="true"]):not([checked]):is(:state(--icons-both), :--icons-both)) .track:not(.pressed) .handle { transform: translateX(calc(0px - ${J.trackOutlineWidth})); } @media (forced-colors: active) { .handle { transition: ${c(`transform var(--_switch-handle-effect),
      width ${A.motion.spring.fastEffects},
      height ${A.motion.spring.fastEffects}`)}; } :host(:not([aria-disabled="true"]):not([checked]):not(:focus):not(:hover)) .track:not(.pressed) .handle, :host(:not([aria-disabled="true"]):not([checked]):not(:focus):hover) .track:not(.pressed) .handle, :host(:not([aria-disabled="true"]):not([checked]):focus) .track:not(.pressed) .handle, :host(:not([aria-disabled="true"]):not([checked])) .track.pressed .handle { background-color: ButtonText; } :host([aria-disabled="true"]:not([checked])) .handle { background-color: GrayText; } :host(:not([aria-disabled="true"])[checked]:not(:focus):not(:hover)) .track:not(.pressed) .handle, :host(:not([aria-disabled="true"])[checked]:not(:focus):hover) .track:not(.pressed) .handle, :host(:not([aria-disabled="true"])[checked]:focus) .track:not(.pressed) .handle, :host(:not([aria-disabled="true"])[checked]) .track.pressed .handle { background-color: Canvas; } :host([aria-disabled="true"][checked]) .handle { background-color: Canvas; } } @media (prefers-reduced-motion) { .handle { transition: none; } }`, wv = l`:host(:is(:state(--icons-none), :--icons-none)) .icon, :host(:is(:state(--icons-selected), :--icons-selected):not([checked])) .icon, :host([aria-disabled="true"]:not([checked])) .icon { display: none; } .icon { width: 1em; transition: ${c(`color ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; } :host(:not([checked])) .icon { font-size: ${J.unselectedIconSize}; } :host([checked]) .icon { font-size: ${J.selectedIconSize}; } :host(:not([aria-disabled="true"]):not([checked]):not(:focus):not(:hover)) .track:not(.pressed) .icon { color: ${J.unselectedIconColor}; } :host(:not([aria-disabled="true"]):not([checked]):not(:focus):hover) .track:not(.pressed) .icon { color: ${J.unselectedHoverIconColor}; } :host(:not([aria-disabled="true"]):not([checked]):focus) .track:not(.pressed) .icon { color: ${J.unselectedFocusIconColor}; } :host(:not([aria-disabled="true"]):not([checked])) .track.pressed .icon { color: ${J.unselectedPressedIconColor}; } :host(:not([aria-disabled="true"])[checked]:not(:focus):not(:hover)) .track:not(.pressed) .icon { color: ${J.selectedIconColor}; } :host(:not([aria-disabled="true"])[checked]:not(:focus):hover) .track:not(.pressed) .icon { color: ${J.selectedHoverIconColor}; } :host(:not([aria-disabled="true"])[checked]:focus) .track:not(.pressed) .icon { color: ${J.selectedFocusIconColor}; } :host(:not([aria-disabled="true"])[checked]) .track.pressed .icon { color: ${J.selectedPressedIconColor}; } :host([aria-disabled="true"]:not([checked])) .icon { color: color-mix( in srgb, ${J.disabledUnselectedIconColor} ${J.disabledUnselectedIconOpacity}, transparent ); } :host([aria-disabled="true"][checked]) .icon { color: color-mix( in srgb, ${J.disabledSelectedIconColor} ${J.disabledSelectedIconOpacity}, transparent ); } @media (forced-colors: active) { :host(:not([aria-disabled="true"]):not([checked]):not(:focus):not(:hover)) .track:not(.pressed) .icon, :host(:not([aria-disabled="true"]):not([checked]):not(:focus):hover) .track:not(.pressed) .icon, :host(:not([aria-disabled="true"]):not([checked]):focus) .track:not(.pressed) .icon, :host(:not([aria-disabled="true"]):not([checked])) .track.pressed .icon { color: Canvas; } :host(:not([aria-disabled="true"])[checked]:not(:focus):not(:hover)) .track:not(.pressed) .icon, :host(:not([aria-disabled="true"])[checked]:not(:focus):hover) .track:not(.pressed) .icon, :host(:not([aria-disabled="true"])[checked]:focus) .track:not(.pressed) .icon, :host(:not([aria-disabled="true"])[checked]) .track.pressed .icon { color: CanvasText; } :host([aria-disabled="true"]:not([checked])) .icon { color: Canvas; } :host([aria-disabled="true"][checked]) .icon { color: GrayText; } } @media (prefers-reduced-motion) { .icon { transition: none; } }`, Tv = l`.state-layer { width: ${J.stateLayerSize}; height: ${J.stateLayerSize}; border-radius: ${J.stateLayerShape}; transition: ${c(`top ${A.motion.spring.fastEffects},
          left ${A.motion.spring.fastEffects},
          right ${A.motion.spring.fastEffects}`)}; } :host(:not([checked]):is(:state(--icons-both), :--icons-both)) .track:not(.pressed) .state-layer { inset-inline-start: calc(0px - calc(calc(${J.stateLayerSize} - ${J.withIconHandleWidth}) / 2)); top: calc(0px - calc(calc(${J.stateLayerSize} - ${J.withIconHandleHeight}) / 2)); } :host(:not([checked]):not(:is(:state(--icons-both), :--icons-both))) .track:not(.pressed) .state-layer { inset-inline-start: calc( 0px - calc(calc(${J.stateLayerSize} - ${J.unselectedHandleWidth}) / 2) ); top: calc(0px - calc(calc(${J.stateLayerSize} - ${J.unselectedHandleHeight}) / 2)); } :host([checked]) .track:not(.pressed) .state-layer { inset-inline-start: calc(0px - calc(calc(${J.stateLayerSize} - ${J.selectedHandleWidth}) / 2)); top: calc(0px - calc(calc(${J.stateLayerSize} - ${J.selectedHandleHeight}) / 2)); } .track.pressed .state-layer { inset-inline-start: calc(0px - calc(calc(${J.stateLayerSize} - ${J.pressedHandleWidth}) / 2)); top: calc(0px - calc(calc(${J.stateLayerSize} - ${J.pressedHandleWidth}) / 2)); } :host(:not([checked])) .state-layer { --m3e-state-layer-hover-color: ${J.unselectedHoverStateLayerColor}; --m3e-state-layer-hover-opacity: ${J.unselectedHoverStateLayerOpacity}; --m3e-state-layer-focus-color: ${J.unselectedFocusStateLayerColor}; --m3e-state-layer-focus-opacity: ${J.unselectedFocusStateLayerOpacity}; } :host([checked]) .state-layer { --m3e-state-layer-hover-color: ${J.selectedHoverStateLayerColor}; --m3e-state-layer-hover-opacity: ${J.selectedHoverStateLayerOpacity}; --m3e-state-layer-focus-color: ${J.selectedFocusStateLayerColor}; --m3e-state-layer-focus-opacity: ${J.selectedFocusStateLayerOpacity}; }`, Ev = l`:host { display: inline-block; position: relative; outline: none; height: fit-content; width: fit-content; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } :host([hidden]) { display: none; } :host(:not([aria-disabled="true"])) { cursor: pointer; } .focus-ring { border-radius: ${J.trackShape}; } .touch { position: absolute; height: 48px; left: 0; right: 0; } .base { contain: layout style paint; display: flex; align-items: center; justify-content: center; }`, Dv = l`.track { contain: layout style; display: flex; align-items: center; position: relative; box-sizing: border-box; border-radius: ${J.trackShape}; width: ${J.trackWidth}; height: ${J.trackHeight}; transition: ${c(`background-color ${A.motion.duration.short4} ${A.motion.easing.standard}`)}; } :host(:not([checked])) .track { border-width: ${J.trackOutlineWidth}; border-style: solid; } :host(:not([aria-disabled="true"]):not([checked]):not(:focus):not(:hover)) .track:not(.pressed) { border-color: ${J.trackOutlineColor}; background-color: ${J.unselectedTrackColor}; } :host(:not([aria-disabled="true"]):not([checked]):not(:focus):hover) .track:not(.pressed) { border-color: ${J.unselectedHoverTrackOutlineColor}; background-color: ${J.unselectedHoverTrackColor}; } :host(:not([aria-disabled="true"]):not([checked]):focus) .track:not(.pressed) { border-color: ${J.unselectedFocusTrackOutlineColor}; background-color: ${J.unselectedFocusTrackColor}; } :host(:not([aria-disabled="true"]):not([checked])) .track.pressed { border-color: ${J.unselectedPressedTrackOutlineColor}; background-color: ${J.unselectedPressedTrackColor}; } :host([aria-disabled="true"]:not([checked])) .track { border-color: color-mix( in srgb, ${J.disabledUnselectedTrackOutlineColor} ${J.disabledTrackOpacity}, transparent ); background-color: color-mix( in srgb, ${J.disabledUnselectedTrackColor} ${J.disabledTrackOpacity}, transparent ); } :host(:not([aria-disabled="true"])[checked]:not(:focus):not(:hover)) .track:not(.pressed) { background-color: ${J.selectedTrackColor}; } :host(:not([aria-disabled="true"])[checked]:not(:focus):hover) .track:not(.pressed) { background-color: ${J.selectedHoverTrackColor}; } :host(:not([aria-disabled="true"])[checked]:focus) .track:not(.pressed) { background-color: ${J.selectedFocusTrackColor}; } :host(:not([aria-disabled="true"])[checked]) .track.pressed { background-color: ${J.selectedPressedTrackColor}; } :host([aria-disabled="true"][checked]) .track { background-color: color-mix( in srgb, ${J.disabledSelectedTrackColor} ${J.disabledTrackOpacity}, transparent ); } @media (forced-colors: active) { .track { transition: none; } :host(:not([aria-disabled="true"]):not([checked]):not(:focus):not(:hover)) .track:not(.pressed), :host(:not([aria-disabled="true"]):not([checked]):not(:focus):hover) .track:not(.pressed), :host(:not([aria-disabled="true"]):not([checked]):focus) .track:not(.pressed), :host(:not([aria-disabled="true"]):not([checked])) .track.pressed { border-color: ButtonText; background-color: Canvas; } :host([aria-disabled="true"]:not([checked])) .track { border-color: GrayText; background-color: Canvas; } :host(:not([aria-disabled="true"])[checked]:not(:focus):not(:hover)) .track:not(.pressed), :host(:not([aria-disabled="true"])[checked]:not(:focus):hover) .track:not(.pressed), :host(:not([aria-disabled="true"])[checked]:focus) .track:not(.pressed), :host(:not([aria-disabled="true"])[checked]) .track.pressed { background-color: ButtonText; } :host([aria-disabled="true"][checked]) .track { background-color: GrayText; } } @media (prefers-reduced-motion) { .track { transition: none; } }`;
function Ov(e) {
	return e === "none" || e === "selected" || e === "both";
}
var kv, Av, jv, Mv, Nv, Pv, Fv, Iv = class extends Cd(Di(Zr(ei(zi(Hr(Ni(qi(Vi(vi(T(j(x, "switch")))))))))))) {
	constructor() {
		super(), kv.add(this), Av.set(this, (e) => t(this, kv, "m", Fv).call(this, e)), jv.set(this, new yn(this, {
			target: null,
			callback: (e) => {
				this.disabled || (e ? this._stateLayer?.show("hover") : this._stateLayer?.hide("hover"));
			}
		})), Mv.set(this, new nr(this, {
			target: null,
			callback: (e) => this._track?.classList.toggle("pressed", e && !this.disabled)
		})), this.icons = "none", this.value = "on", new nr(this, {
			isPressedKey: (e) => e === " ",
			callback: (e) => this._track?.classList.toggle("pressed", e && !this.disabled)
		});
	}
	get [(Av = /* @__PURE__ */ new WeakMap(), jv = /* @__PURE__ */ new WeakMap(), Mv = /* @__PURE__ */ new WeakMap(), kv = /* @__PURE__ */ new WeakSet(), Oi)]() {
		return this.checked && !this.disabled ? this.value : null;
	}
	connectedCallback() {
		super.connectedCallback(), t(this, kv, "m", Nv).call(this), this.addEventListener("click", t(this, Av, "f"));
		for (let e of this.labels) t(this, jv, "f").observe(e), t(this, Mv, "f").observe(e);
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("click", t(this, Av, "f"));
		for (let e of this.labels) t(this, jv, "f").unobserve(e), t(this, Mv, "f").unobserve(e);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("icons") && t(this, kv, "m", Nv).call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), [this._focusRing, this._stateLayer].forEach((e) => e?.attach(this));
	}
	render() {
		return y`<m3e-focus-ring class="focus-ring"></m3e-focus-ring><div class="track" aria-hidden="true"><div class="touch" aria-hidden="true"></div><div class="handle"><m3e-state-layer class="state-layer" ?disabled="${this.disabled}"></m3e-state-layer><div class="base">${t(this, kv, "m", Pv).call(this)}</div></div></div>`;
	}
};
Nv = function() {
	Ov(this.icons) || (this.icons = "none"), D(this, "--icons-none", this.icons === "none"), D(this, "--icons-selected", this.icons === "selected"), D(this, "--icons-both", this.icons === "both");
}, Pv = function() {
	return this.checked ? y`<svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M19.69,5.23L8.96,15.96l-4.23-4.23L2.96,13.5l6,6L21.46,7L19.69,5.23z"></path></svg>` : y`<svg class="icon" viewBox="0 -960 960 960" fill="currentColor"><path d="m256-200-56-56 224-224-224-224 56-56 224 224 224-224 56 56-224 224 224 224-56 56-224-224-224 224Z"/></svg>`;
}, Fv = function(e) {
	e.defaultPrevented || (this.disabled ? (e.preventDefault(), e.stopImmediatePropagation()) : (this.dispatchEvent(new Event("beforeinput", {
		bubbles: !0,
		cancelable: !0
	})) && (this.checked = !this.checked, this.dispatchEvent(new Event("input", { bubbles: !0 })), this.dispatchEvent(new Event("change", { bubbles: !0 }))), this.closest("label") && e.preventDefault()));
}, Iv.styles = [
	Ev,
	Tv,
	Dv,
	Cv,
	wv
], e([C(".track")], Iv.prototype, "_track", void 0), e([C(".focus-ring")], Iv.prototype, "_focusRing", void 0), e([C(".state-layer")], Iv.prototype, "_stateLayer", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], Iv.prototype, "icons", void 0), e([S()], Iv.prototype, "value", void 0), Iv = e([w("m3e-switch")], Iv);
//#endregion
//#region node_modules/@m3e/web/dist/slider.js
var Lv, Rv, zv = class extends Zr(ei(Ni(Vi(vi(T(j(x, "slider"))))))) {
	constructor() {
		super(...arguments), Lv.add(this), this.value = null;
	}
	get [(Lv = /* @__PURE__ */ new WeakSet(), Oi)]() {
		return this.value?.toString() ?? null;
	}
	focus(e) {
		this._focusRing && (this._focusRing.disabled = !0), super.focus(e), this._focusRing && (this._focusRing.disabled = this.disabled);
	}
	firstUpdated(e) {
		super.firstUpdated(e), this._focusRing?.attach(this);
	}
	update(e) {
		super.update(e), e.has("value") && this.dispatchEvent(new Event("value-change", { bubbles: !0 }));
	}
	render() {
		return y`<div class="label" aria-hidden="true">${t(this, Lv, "a", Rv)}</div><div class="base"><m3e-focus-ring class="focus-ring" ?disabled="${this.disabled}"></m3e-focus-ring><div class="touch" aria-hidden="true"></div><div class="wrapper"><div class="handle"></div></div></div>`;
	}
};
Rv = function() {
	return this.closest("m3e-slider")?.displayWith?.(this.value) ?? this.value?.toString() ?? "";
}, zv.styles = l`:host { display: block; box-sizing: border-box; position: absolute; outline: none; top: 0; bottom: 0; border-radius: var(--m3e-slider-thumb-shape, ${A.shape.corner.full}); user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } :host([hidden]) { display: none; } .base { contain: layout style; box-sizing: border-box; vertical-align: middle; display: inline-flex; align-items: center; justify-content: center; position: relative; width: 100%; height: 100%; border-radius: inherit; } .touch { position: absolute; height: 48px; left: 0; right: 0; touch-action: none; } .wrapper { display: inline-flex; justify-content: center; height: 100%; border-radius: inherit; width: calc(var(--m3e-slider-thumb-width, 4px) + calc(var(--m3e-slider-thumb-padding, 6px) * 2)); } .focus-ring { top: calc(0px - var(--m3e-focus-ring-thickness, 3px)); bottom: calc(0px - var(--m3e-focus-ring-thickness, 3px)); left: var(--m3e-focus-ring-thickness, 3px); right: var(--m3e-focus-ring-thickness, 3px); } .label { user-select: none; position: absolute; display: flex; align-items: center; justify-content: center; visibility: var(--_slider-label-visibility, hidden); opacity: var(--_slider-label-opacity, 0); transform: var(--_slider-label-transform, scale(0)); transition: ${c(`opacity ${A.motion.duration.short3} ${A.motion.easing.standard}, 
          transform ${A.motion.duration.short3} ${A.motion.easing.standard},
          visibility ${A.motion.duration.short3} ${A.motion.easing.standard} allow-discrete`)}; width: var(--m3e-slider-label-width, 48px); height: var(--_m3e-slider-label-height, 44px); top: calc(0px - var(--_m3e-slider-label-height, 44px) - var(--_m3e-slider-label-margin, 4px)); inset-inline-start: calc(0px - 100%); border-radius: var(--m3e-slider-label-shape, ${A.shape.corner.full}); background-color: var(--m3e-slider-label-container-color, ${A.color.inverseSurface}); color: var(--m3e-slider-label-color, ${A.color.inverseOnSurface}); font-size: var(--m3e-slider-label-font-size, ${A.typescale.standard.label.medium.fontSize}); font-weight: var(--m3e-slider-label-font-weight, ${A.typescale.standard.label.medium.fontWeight}); line-height: var(--m3e-slider-label-line-height, ${A.typescale.standard.label.medium.lineHeight}); letter-spacing: var(--m3e-slider-label-tracking, ${A.typescale.standard.label.medium.tracking}); } @starting-style { .label { opacity: 0; transform: scale(0); } } .handle { height: 100%; width: var(--m3e-slider-thumb-width, 4px); border-radius: inherit; transition: ${c(`width ${A.motion.duration.short2} ${A.motion.easing.standard}`)}; } :host(:is(:active, :focus-visible, :is(:state(--active), :--active)):not([aria-disabled="true"])) .handle { width: var(--m3e-slider-thumb-pressed-width, 2px); } :host(:not([aria-disabled="true"])) .handle { background-color: var(--m3e-slider-thumb-color, ${A.color.primary}); } :host([aria-disabled="true"]) .handle { opacity: var(--m3e-slider-thumb-disabled-opacity, 38%); background-color: var(--m3e-slider-thumb-disabled-color, ${A.color.onSurface}); } @media (prefers-reduced-motion) { .label { transition: none; } } @media (forced-colors: active) { .label { forced-color-adjust: none; background-color: CanvasText; color: Canvas; } :host(:not([aria-disabled="true"])) .handle { background-color: CanvasText; } :host([aria-disabled="true"]) .handle { opacity: unset; background-color: GrayText; } } /* ── Vertical orientation ──────────────────────────────────────────────── Set by the parent slider as a custom state: the thumb is slotted light DOM, so its own stylesheet cannot select on the slider's orientation attribute. The thumb is anchored to the bottom, because a vertical slider runs bottom-to-top and the parent positions it with a negative Y translation. Its block size mirrors what .wrapper gives the horizontal thumb on the inline axis, so the parent's clientHeight measurement stays symmetric with clientWidth. */ :host(:is(:state(--vertical), :--vertical)) { top: auto; bottom: 0; inset-inline: 0; height: calc(var(--m3e-slider-thumb-width, 4px) + calc(var(--m3e-slider-thumb-padding, 6px) * 2)); } :host(:is(:state(--vertical), :--vertical)) .touch { height: auto; width: 48px; top: 0; bottom: 0; inset-inline-start: 50%; translate: -50% 0; } :host(:is(:state(--vertical), :--vertical)) .wrapper { width: 100%; height: 100%; align-items: center; } :host(:is(:state(--vertical), :--vertical)) .handle { width: 100%; height: var(--m3e-slider-thumb-width, 4px); transition: ${c(`height ${A.motion.duration.short2} ${A.motion.easing.standard}`)}; } :host( :is(:state(--vertical), :--vertical):is(:active, :focus-visible, :is(:state(--active), :--active)):not( [aria-disabled="true"] ) ) .handle { width: 100%; height: var(--m3e-slider-thumb-pressed-width, 2px); } :host(:is(:state(--vertical), :--vertical)) .focus-ring { top: var(--m3e-focus-ring-thickness, 3px); bottom: var(--m3e-focus-ring-thickness, 3px); left: calc(0px - var(--m3e-focus-ring-thickness, 3px)); right: calc(0px - var(--m3e-focus-ring-thickness, 3px)); } :host(:is(:state(--vertical), :--vertical)) .label { top: calc(50% - calc(var(--_m3e-slider-label-height, 44px) / 2)); inset-inline-start: calc(0px - var(--m3e-slider-label-width, 48px) - var(--_m3e-slider-label-margin, 4px)); }`, e([C(".focus-ring")], zv.prototype, "_focusRing", void 0), e([S({
	type: Number,
	reflect: !0
})], zv.prototype, "value", void 0), zv = e([w("m3e-slider-thumb")], zv);
function Bv(e) {
	return e === "horizontal" || e === "vertical";
}
function Vv(e) {
	return e === "extra-small" || e === "small" || e === "medium" || e === "large" || e === "extra-large";
}
var Y, Hv, Uv, Wv, X, Gv, Kv, qv, Jv, Yv, Xv, Zv, Qv, $v, ey, ty, ny, ry, iy, ay, oy, sy, cy, ly, uy, dy, fy, py, my, hy, gy, _y, vy = class extends Cd(T(x)) {
	constructor() {
		super(), Y.add(this), Hv.set(this, void 0), this._ticks = [], Uv.set(this, /* @__PURE__ */ new Set()), Wv.set(this, []), X.set(this, void 0), Gv.set(this, 0), Kv.set(this, 0), qv.set(this, 0), Jv.set(this, 0), Yv.set(this, 0), Xv.set(this, 0), this.orientation = "horizontal", this.size = "extra-small", this.disabled = !1, this.discrete = !1, this.min = 0, this.max = 100, this.step = 1, this.labelled = !1, this.displayWith = null, new cr(this, { callback: () => t(this, Y, "m", sy).call(this, !0) });
	}
	get thumbs() {
		return t(this, Wv, "f");
	}
	get isRange() {
		return t(this, Wv, "f").length > 1;
	}
	get thumb() {
		return t(this, Wv, "f")[0] ?? null;
	}
	get lowerThumb() {
		return this.thumb;
	}
	get upperThumb() {
		return t(this, Wv, "f")[1] ?? null;
	}
	connectedCallback() {
		super.connectedCallback(), t(this, Y, "m", Qv).call(this), t(this, Y, "m", $v).call(this), n(this, Hv, Sd.observe(() => {
			t(this, Y, "m", sy).call(this, !0), this.requestUpdate();
		}), "f");
	}
	disconnectedCallback() {
		super.disconnectedCallback(), t(this, Uv, "f").clear(), t(this, Hv, "f")?.call(this);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("size") && t(this, Y, "m", Qv).call(this), e.has("orientation") && t(this, Y, "m", $v).call(this);
	}
	updated(e) {
		super.updated(e), e.has("disabled") && t(this, Wv, "f").forEach((e) => e.disabled = this.disabled), e.has("orientation") && (t(this, Y, "m", ny).call(this), t(this, Y, "m", sy).call(this, !0));
	}
	render() {
		return y`<div class="base" tabindex="${rt(this.disabled ? void 0 : "-1")}" @pointerdown="${t(this, Y, "m", uy)}" @pointermove="${t(this, Y, "m", dy)}" @pointerup="${t(this, Y, "m", fy)}" @keydown="${t(this, Y, "m", py)}" @keyup="${t(this, Y, "m", my)}" @value-change="${t(this, Y, "m", hy)}"><div class="track" aria-hidden="true"><div class="track-inactive start"></div><div class="track-active"></div><div class="track-inactive end"></div></div><div class="ticks" aria-hidden="true">${this._ticks.map((e) => t(this, Y, "m", ey).call(this, e))}</div><slot @slotchange="${t(this, Y, "m", ty)}"></slot></div>`;
	}
};
Hv = /* @__PURE__ */ new WeakMap(), Uv = /* @__PURE__ */ new WeakMap(), Wv = /* @__PURE__ */ new WeakMap(), X = /* @__PURE__ */ new WeakMap(), Gv = /* @__PURE__ */ new WeakMap(), Kv = /* @__PURE__ */ new WeakMap(), qv = /* @__PURE__ */ new WeakMap(), Jv = /* @__PURE__ */ new WeakMap(), Yv = /* @__PURE__ */ new WeakMap(), Xv = /* @__PURE__ */ new WeakMap(), Y = /* @__PURE__ */ new WeakSet(), Zv = function() {
	return this.orientation === "vertical";
}, Qv = function() {
	Vv(this.size) || (this.size = "extra-small"), Br(this, this.size, "extra-large", "extra-small", "large", "medium", "small");
}, $v = function() {
	Bv(this.orientation) || (this.orientation = "horizontal"), Br(this, this.orientation, "horizontal", "vertical");
}, ey = function(e) {
	return y`<div class="tick ${e.active ? "active" : "inactive"}${e.hidden ? " hidden" : ""}" style="${Mr({ transform: t(this, Y, "m", ry).call(this, t(this, Y, "m", iy).call(this, e.value)) })}"></div>`;
}, ty = function(e) {
	n(this, Wv, e.target.assignedElements({ flatten: !0 }).filter((e) => e instanceof zv), "f"), t(this, Wv, "f").length > 2 && (t(this, Wv, "f").length = 2), this.isRange ? this._base?.style.setProperty("--_slider-active-track-middle-shape", "0") : this._base?.style.removeProperty("--_slider-active-track-middle-shape"), t(this, Y, "m", ny).call(this);
}, ny = function() {
	t(this, Wv, "f").forEach((e, n) => {
		this.disabled && (e.disabled = !0), e.ariaValueMin = `${t(this, Wv, "f")[n - 1]?.value ?? this.min}`, e.ariaValueMax = `${t(this, Wv, "f")[n + 1]?.value ?? this.max}`, e.ariaValueNow = `${e.value ?? t(this, Wv, "f")[n - 1]?.value ?? this.min}`, e.ariaOrientation = this.orientation, t(this, Y, "a", Zv) ? zr(e, "--vertical") : E(e, "--vertical");
	});
}, ry = function(e) {
	return t(this, Y, "a", Zv) ? `translate(0, ${-e}px)` : `translate(${Sd.current === "rtl" ? -e : e}px, 0)`;
}, iy = function(e) {
	return (t(this, Gv, "f") - t(this, Kv, "f")) * ((e - this.min) / (this.max - this.min));
}, ay = function(e) {
	let n = t(this, Y, "a", Zv) || Sd.current === "rtl" ? t(this, Jv, "f") - (t(this, Y, "a", Zv) ? e.clientY : e.clientX) : e.clientX - t(this, qv, "f"), r = this.step === 0 ? 1 : this.step, i = Math.floor((this.max - this.min) / r), a = t(this, Gv, "f") ? t(this, Kv, "f") / t(this, Gv, "f") : 0, o = (n / t(this, Yv, "f") - a / 2) / (1 - a || 1), s = Math.round(o * i) / i * (this.max - this.min) + this.min;
	return Math.round(s / r) * r;
}, oy = function(e = !1) {
	if (!this.lowerThumb) return;
	let r = t(this, Y, "a", Zv) ? this.clientHeight : this.clientWidth, i = t(this, Y, "a", Zv) ? this.lowerThumb.clientHeight : this.lowerThumb.clientWidth;
	n(this, Gv, !e && t(this, Gv, "f") > 0 ? t(this, Gv, "f") : r, "f"), n(this, Kv, !e && t(this, Kv, "f") > 0 ? t(this, Kv, "f") : i, "f");
	let a = this.getBoundingClientRect(), o = t(this, Y, "a", Zv) ? a.top : a.left, s = t(this, Y, "a", Zv) ? a.bottom : a.right, c = t(this, Y, "a", Zv) ? a.height : a.width;
	n(this, qv, !e && t(this, qv, "f") > 0 ? t(this, qv, "f") : o, "f"), n(this, Jv, !e && t(this, Jv, "f") > 0 ? t(this, Jv, "f") : s, "f"), n(this, Yv, !e && t(this, Yv, "f") > 0 ? t(this, Yv, "f") : c, "f");
}, sy = function(e = !1) {
	if (t(this, Y, "m", oy).call(this, e), !this.lowerThumb) return;
	let n = this.lowerThumb.value ?? this.min, r = t(this, Y, "m", iy).call(this, n);
	if (this.lowerThumb.style.transform = t(this, Y, "m", ry).call(this, r), !this.upperThumb) this._base?.classList.toggle("range", !1), this._base?.style.setProperty("--_slider-active-track-size", `${r}px`), this._base?.style.setProperty("--_slider-inactive-track-after-offset", `${r + t(this, Kv, "f")}px`), this._base?.style.setProperty("--_slider-inactive-track-after-size", `${t(this, Gv, "f") - r - t(this, Kv, "f")}px`), t(this, Y, "m", cy).call(this, (e) => e < n);
	else {
		let e = this.upperThumb.value ?? n, i = t(this, Y, "m", iy).call(this, e);
		this.upperThumb.style.transform = t(this, Y, "m", ry).call(this, i), this._base?.classList.toggle("range", !0), this._base?.style.setProperty("--_slider-inactive-track-before-size", `${r}px`), this._base?.style.setProperty("--_slider-active-track-offset", `${r + t(this, Kv, "f")}px`), this._base?.style.setProperty("--_slider-active-track-size", `${i - r - t(this, Kv, "f")}px`), this._base?.style.setProperty("--_slider-inactive-track-after-offset", `${i + t(this, Kv, "f")}px`), this._base?.style.setProperty("--_slider-inactive-track-after-size", `${t(this, Gv, "f") - t(this, Kv, "f") - i}px`), t(this, Y, "m", cy).call(this, (t) => t > n && t < e);
	}
}, cy = function(e) {
	if (this._ticks = [], this.discrete && this.step > 1) for (let t = this.min; t <= this.max; t += this.step) this._ticks.push({
		value: t,
		active: e(t),
		hidden: !1
	});
	else this._ticks.push({
		value: this.min,
		active: e(this.min),
		hidden: !1
	}), this.min < 0 && this.max > 0 && this._ticks.push({
		value: 0,
		active: e(0),
		hidden: !1
	}), this._ticks.push({
		value: this.max,
		active: e(this.max),
		hidden: !1
	});
	t(this, Y, "m", ly).call(this);
}, ly = function() {
	if (t(this, Gv, "f") === 0 || t(this, Kv, "f") === 0) return;
	let e = t(this, Wv, "f").filter((e) => e.value != null);
	if (e.length === 0) return;
	let n = t(this, Kv, "f") / 2;
	this._ticks = this._ticks.map((r) => {
		let i = t(this, Y, "m", iy).call(this, r.value), a = e.some((e) => {
			let r = t(this, Y, "m", iy).call(this, e.value);
			return Math.abs(i - r) < n;
		});
		return {
			...r,
			hidden: a
		};
	});
}, uy = function(e) {
	if (t(this, Y, "m", oy).call(this, !0), e.pointerType === "mouse" && e.button > 1 || !this.lowerThumb || this.disabled || (e.target instanceof HTMLElement && e.target.setPointerCapture(e.pointerId), t(this, Uv, "f").clear(), n(this, Xv, e.timeStamp, "f"), n(this, X, e.composedPath().find((e) => e instanceof zv), "f"), t(this, X, "f"))) return;
	let r = t(this, Y, "m", ay).call(this, e), i = this.min, a = this.max;
	if (t(this, X, "f") === this.upperThumb ? i = Math.max(i, this.lowerThumb?.value ?? 0) : this.upperThumb && (a = Math.min(a, this.upperThumb.value ?? this.max)), !this.upperThumb) this.lowerThumb.disabled || (t(this, Y, "m", gy).call(this, this.lowerThumb, Math.min(a, Math.max(i, r)), !0), n(this, X, this.lowerThumb, "f"));
	else {
		let e = this.lowerThumb.value ?? this.min, o = this.upperThumb.value ?? e;
		r < e ? this.lowerThumb.disabled || (t(this, Y, "m", gy).call(this, this.lowerThumb, Math.min(a, Math.max(i, r)), !0), n(this, X, this.lowerThumb, "f")) : r > o ? this.upperThumb.disabled || (t(this, Y, "m", gy).call(this, this.upperThumb, Math.min(a, Math.max(i, r)), !0), n(this, X, this.upperThumb, "f")) : r < (e + o) / 2 && !this.lowerThumb.disabled ? (t(this, Y, "m", gy).call(this, this.lowerThumb, Math.min(a, Math.max(i, r)), !0), n(this, X, this.lowerThumb, "f")) : this.upperThumb.disabled || (t(this, Y, "m", gy).call(this, this.upperThumb, Math.min(a, Math.max(i, r)), !0), n(this, X, this.upperThumb, "f"));
	}
	t(this, X, "f") && zr(t(this, X, "f"), "--active");
}, dy = function(e) {
	if (!(e.target instanceof HTMLElement) || !e.target.hasPointerCapture(e.pointerId) || !t(this, X, "f") || t(this, X, "f").disabled) return;
	let r = t(this, Y, "m", ay).call(this, e), i = this.min, a = this.max;
	if (t(this, X, "f") === this.upperThumb ? i = Math.max(i, this.lowerThumb?.value ?? 0) : this.upperThumb && (a = Math.min(a, this.upperThumb.value ?? this.max)), r = Math.min(a, Math.max(i, r)), r === t(this, X, "f").value) return;
	let o = (this.step === 0 ? 1 : this.step) !== 1;
	if (o) for (let r of e.getCoalescedEvents()) r.timeStamp - t(this, Xv, "f") < 90 && (o = !1), n(this, Xv, r.timeStamp, "f");
	o || Rr(this, "--animating") && (E(this, "--animating"), t(this, X, "f").style.transition = ""), t(this, Y, "m", gy).call(this, t(this, X, "f"), r, o);
}, fy = function(e) {
	e.pointerType === "mouse" && e.button > 1 || this.lowerThumb && !this.disabled && (e.target instanceof HTMLElement && e.target.releasePointerCapture(e.pointerId), n(this, Xv, 0, "f"), t(this, X, "f") && !t(this, X, "f").disabled && (t(this, Y, "m", _y).call(this, t(this, X, "f")), t(this, X, "f").focus(), E(t(this, X, "f"), "--active")));
}, py = function(e) {
	if (n(this, X, e.composedPath().find((e) => e instanceof zv), "f"), !t(this, X, "f")) return;
	let r = t(this, X, "f").value ?? 0, i = this.min, a = this.max;
	t(this, X, "f") === this.upperThumb ? i = Math.max(i, this.lowerThumb?.value ?? 0) : this.upperThumb && (a = Math.max(a, this.upperThumb.value ?? this.max));
	let o = t(this, Y, "a", Zv) || Sd.current === "ltr";
	switch (e.key) {
		case "Home":
			t(this, Y, "m", gy).call(this, t(this, X, "f"), i), e.preventDefault();
			break;
		case "End":
			t(this, Y, "m", gy).call(this, t(this, X, "f"), a), e.preventDefault();
			break;
		case "PageUp":
			o ? t(this, Y, "m", gy).call(this, t(this, X, "f"), Math.min(a, r + (this.step > 1 ? this.step : 10))) : t(this, Y, "m", gy).call(this, t(this, X, "f"), Math.max(i, r - (this.step > 1 ? this.step : 10))), e.preventDefault();
			break;
		case "PageDown":
			o ? t(this, Y, "m", gy).call(this, t(this, X, "f"), Math.max(i, r - (this.step > 1 ? this.step : 10))) : t(this, Y, "m", gy).call(this, t(this, X, "f"), Math.min(a, r + (this.step > 1 ? this.step : 10))), e.preventDefault();
			break;
		case "Down":
		case "ArrowDown":
		case "Left":
		case "ArrowLeft":
			o ? t(this, Y, "m", gy).call(this, t(this, X, "f"), Math.max(i, r - this.step)) : t(this, Y, "m", gy).call(this, t(this, X, "f"), Math.min(a, r + this.step)), e.preventDefault();
			break;
		case "Up":
		case "ArrowUp":
		case "Right":
		case "ArrowRight":
			o ? t(this, Y, "m", gy).call(this, t(this, X, "f"), Math.min(a, r + this.step)) : t(this, Y, "m", gy).call(this, t(this, X, "f"), Math.max(i, r - this.step)), e.preventDefault();
			break;
		case " ": e.preventDefault();
	}
}, my = function(e) {
	let n = e.composedPath().find((e) => e instanceof zv);
	n && t(this, Y, "m", _y).call(this, n);
}, hy = function(e) {
	e.stopPropagation(), t(this, Y, "m", ny).call(this), t(this, Y, "m", sy).call(this);
}, gy = function(e, n, r = !1) {
	e.value !== n && e.dispatchEvent(new Event("beforeinput", {
		bubbles: !0,
		cancelable: !0
	})) && (r && !Bt() && (zr(this, "--animating"), e.addEventListener("transitionend", () => {
		e.style.transition = "", E(this, "--animating");
	}, { once: !0 }), e.style.transition = `transform ${A.motion.spring.fastEffects}`), t(this, Uv, "f").add(e), e.value = n, e.markAsDirty(), e.markAsTouched(), e.dispatchEvent(new Event("input", { bubbles: !0 })));
}, _y = function(e) {
	t(this, Uv, "f").has(e) && (e.dispatchEvent(new Event("change", { bubbles: !0 })), t(this, Uv, "f").delete(e));
}, vy.styles = l`:host { display: inline-block; vertical-align: middle; min-inline-size: var(--m3e-slider-min-width, 200px); user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); line-height: 0; } :host([hidden]) { display: none; } :host(:not([disabled])) { cursor: pointer; } :host(:is(:state(--extra-small), :--extra-small)), :host(:is(:state(--small), :--small)) { height: var(--m3e-slider-small-height, 44px); } :host(:not(:is(:state(--rtl), :--rtl)):is(:state(--extra-small), :--extra-small)) .base, :host(:not(:is(:state(--rtl), :--rtl)):is(:state(--small), :--small)) .base { --_slider-active-track-shape: var(--m3e-slider-small-active-track-shape, ${A.shape.corner.smallStart}); --_slider-inactive-track-start-shape: var( --m3e-slider-small-inactive-active-track-start-shape, ${A.shape.corner.smallStart} ); --_slider-inactive-track-end-shape: var( --m3e-slider-small-inactive-track-end-shape, ${A.shape.corner.smallEnd} ); } :host(:is(:state(--rtl), :--rtl):is(:state(--extra-small), :--extra-small)) .base, :host(:is(:state(--rtl), :--rtl):is(:state(--small), :--small)) .base { --_slider-active-track-shape: var(--m3e-slider-small-active-track-shape, ${A.shape.corner.smallEnd}); --_slider-inactive-track-start-shape: var( --m3e-slider-small-inactive-active-track-start-shape, ${A.shape.corner.smallEnd} ); --_slider-inactive-track-end-shape: var( --m3e-slider-small-inactive-track-end-shape, ${A.shape.corner.smallStart} ); } :host(:is(:state(--extra-small), :--extra-small)) .track { height: calc(var(--m3e-slider-extra-small-track-height, 16px)); } :host(:is(:state(--small), :--small)) .track { height: calc(var(--m3e-slider-small-track-height, 24px)); } :host(:is(:state(--medium), :--medium)) { height: var(--m3e-slider-medium-height, 52px); } :host(:not(:is(:state(--rtl), :--rtl)):is(:state(--medium), :--medium)) .base { --_slider-active-track-shape: var( --m3e-slider-medium-active-track-shape, ${A.shape.corner.mediumStart} ); --_slider-inactive-track-start-shape: var( --m3e-slider-medium-inactive-active-track-start-shape, ${A.shape.corner.mediumStart} ); --_slider-inactive-track-end-shape: var( --m3e-slider-medium-inactive-track-end-shape, ${A.shape.corner.mediumEnd} ); } :host(:is(:state(--rtl), :--rtl):is(:state(--medium), :--medium)) .base { --_slider-active-track-shape: var(--m3e-slider-medium-active-track-shape, ${A.shape.corner.mediumEnd}); --_slider-inactive-track-start-shape: var( --m3e-slider-medium-inactive-active-track-start-shape, ${A.shape.corner.mediumEnd} ); --_slider-inactive-track-end-shape: var( --m3e-slider-medium-inactive-track-end-shape, ${A.shape.corner.mediumStart} ); } :host(:is(:state(--medium), :--medium)) .track { height: var(--m3e-slider-medium-track-height, 40px); } :host(:is(:state(--large), :--large)) { height: var(--m3e-slider-large-height, 68px); } :host(:not(:is(:state(--rtl), :--rtl)):is(:state(--large), :--large)) .base { --_slider-active-track-shape: var(--m3e-slider-large-active-track-shape, ${A.shape.corner.largeStart}); --_slider-inactive-track-start-shape: var( --m3e-slider-large-inactive-active-track-start-shape, ${A.shape.corner.largeStart} ); --_slider-inactive-track-end-shape: var( --m3e-slider-large-inactive-track-end-shape, ${A.shape.corner.largeEnd} ); } :host(:is(:state(--rtl), :--rtl):is(:state(--large), :--large)) .base { --_slider-active-track-shape: var(--m3e-slider-large-active-track-shape, ${A.shape.corner.largeEnd}); --_slider-inactive-track-start-shape: var( --m3e-slider-large-inactive-active-track-start-shape, ${A.shape.corner.largeEnd} ); --_slider-inactive-track-end-shape: var( --m3e-slider-large-inactive-track-end-shape, ${A.shape.corner.largeStart} ); } :host(:is(:state(--large), :--large)) .track { height: var(--m3e-slider-large-track-height, 56px); } :host(:is(:state(--extra-large), :--extra-large)) { height: var(--m3e-slider-extra-large-height, 108px); } :host(:not(:is(:state(--rtl), :--rtl)):is(:state(--extra-large), :--extra-large)) .base { --_slider-active-track-shape: var( --m3e-slider-extra-large-active-track-shape, ${A.shape.corner.extraLargeStart} ); --_slider-inactive-track-start-shape: var( --m3e-slider-extra-large-inactive-active-track-start-shape, ${A.shape.corner.extraLargeStart} ); --_slider-inactive-track-end-shape: var( --m3e-slider-extra-large-inactive-track-end-shape, ${A.shape.corner.extraLargeEnd} ); } :host(:is(:state(--rtl), :--rtl):is(:state(--extra-large), :--extra-large)) .base { --_slider-active-track-shape: var( --m3e-slider-extra-large-active-track-shape, ${A.shape.corner.extraLargeEnd} ); --_slider-inactive-track-start-shape: var( --m3e-slider-extra-large-inactive-active-track-start-shape, ${A.shape.corner.extraLargeEnd} ); --_slider-inactive-track-end-shape: var( --m3e-slider-extra-large-inactive-track-end-shape, ${A.shape.corner.extraLargeStart} ); } :host(:is(:state(--extra-large), :--extra-large)) .track { height: var(--m3e-slider-extra-large-track-height, 96px); } :host(:is(:state(--animating), :--animating)) .track-active, :host(:is(:state(--animating), :--animating)) .track-inactive.start, :host(:is(:state(--animating), :--animating)) .track-inactive.end { transition: ${c(`margin-inline-start ${A.motion.spring.fastEffects},
        width ${A.motion.spring.fastEffects}`)}; } .base { contain: layout style; display: inline-flex; align-items: center; position: relative; width: 100%; height: 100%; border-radius: inherit; outline: none; touch-action: none; } .track { contain: layout style paint; position: relative; flex: 1 1 auto; touch-action: none; } .track-inactive, .track-active { contain: layout style paint; position: absolute; height: 100%; touch-action: none; } .track-active { margin-inline-start: var(--_slider-active-track-offset, 0px); width: var(--_slider-active-track-size, 0px); border-radius: var(--_slider-active-track-middle-shape, var(--_slider-active-track-shape)); } .track-inactive.start { width: var(--_slider-inactive-track-before-size, 0px); border-radius: var(--_slider-inactive-track-start-shape); } .track-inactive.end { margin-inline-start: var(--_slider-inactive-track-after-offset, 0px); width: var(--_slider-inactive-track-after-size, 0px); border-radius: var(--_slider-inactive-track-end-shape); } .ticks { position: absolute; width: 100%; height: var(--m3e-slider-tick-size, 4px); overflow: visible; touch-action: none; } .tick { position: absolute; top: 0; touch-action: none; inset-inline-start: calc(var(--m3e-slider-tick-size, 4px) + calc(var(--m3e-slider-tick-size, 4px) / 2)); width: var(--m3e-slider-tick-size, 4px); height: var(--m3e-slider-tick-size, 4px); border-radius: var(--m3e-slider-tick-shape, ${A.shape.corner.full}); } .tick.hidden { visibility: hidden; } :host(:not([disabled])) .track-inactive { background-color: var(--m3e-slider-inactive-track-color, ${A.color.secondaryContainer}); } :host([disabled]) .track-inactive { background-color: color-mix( in srgb, var(--m3e-slider-disabled-inactive-track-color, ${A.color.onSurface}) var(--m3e-slider-disabled-inactive-track-opacity, 12%), transparent ); } :host(:not([disabled])) .track-active { background-color: var(--m3e-slider-active-track-color, ${A.color.primary}); } :host([disabled]) .track-active { background-color: color-mix( in srgb, var(--m3e-slider-disabled-active-track-color, ${A.color.onSurface}) var(--m3e-slider-disabled-active-track-opacity, 38%), transparent ); } :host(:not([disabled])) .tick.active { background-color: var(--m3e-slider-tick-active-color, ${A.color.onPrimary}); } :host([disabled]) .tick.active { background-color: var(--m3e-slider-disabled-tick-active-color, ${A.color.inverseOnSurface}); } :host(:not([disabled])) .tick.inactive { background-color: var(--m3e-slider-tick-inactive-color, ${A.color.onSecondaryContainer}); } :host([disabled]) .tick.inactive { background-color: var(--m3e-slider-disabled-tick-inactive-color, ${A.color.onSurface}); } :host(:not([discrete])) .tick.active { display: none; } :host(:hover[labelled]) .base, :host(:focus-within[labelled]) .base { --_slider-label-visibility: visible; --_slider-label-opacity: 1; --_slider-label-transform: scale(1); } @media (forced-colors: active) { :host(:not([disabled])) .track-inactive { background-color: unset; } :host(:not([disabled])) .base.range .track-inactive.start, :host(:not([disabled])) .track-inactive.end { border: 1px solid CanvasText; box-sizing: border-box; } :host(:not([disabled])) .tick.inactive { background-color: CanvasText; } :host(:not([disabled])) .tick.active { background-color: Canvas; } :host(:not([disabled])) .track-active { background-color: CanvasText; } :host([disabled]) .base.range .track-inactive.start, :host([disabled]) .track-inactive.end { border: 1px solid GrayText; box-sizing: border-box; } :host([disabled]) .track-active { background-color: GrayText; } :host([disabled]) .tick.inactive { background-color: GrayText; } :host([disabled]) .tick.active { background-color: Canvas; } } /* ── Vertical orientation ──────────────────────────────────────────────── Everything above lays the slider out along the inline axis. These rules re-map it to the block axis and are deliberately additive: they come last and so win on source order, which keeps the horizontal path untouched. Segments are anchored with bottom rather than margin-inline-start because a vertical slider runs bottom-to-top, so the offsets the element writes — measured from the end holding the minimum — already point the right way. The five --_slider-* custom properties are plain lengths and need no change; only the property they feed does. Corner radii are composed from the symmetric corner tokens rather than the Start/End ones, whose 4-value radii round the inline edges. */ :host(:is(:state(--vertical), :--vertical)) { min-inline-size: auto; min-block-size: var(--m3e-slider-min-width, 200px); /* Containing block for .base below. */ position: relative; } /* .base is taken out of flow rather than sized with height: 100%. A vertical host has no definite block size of its own - its height comes from min-block-size, from a flex parent, or from the consumer - and a percentage height resolves against the parent's *computed* height, which is auto. That collapses .base to zero and takes the track and both thumbs with it, since .base is their containing block. Insets resolve against the used height, so they work in every case. */ :host(:is(:state(--vertical), :--vertical)) .base { position: absolute; inset: 0; width: auto; height: auto; flex-direction: column; } :host(:is(:state(--vertical), :--vertical):is(:state(--extra-small), :--extra-small)), :host(:is(:state(--vertical), :--vertical):is(:state(--small), :--small)) { height: auto; width: var(--m3e-slider-small-height, 44px); } :host(:is(:state(--vertical), :--vertical):is(:state(--extra-small), :--extra-small)) .track { height: auto; width: calc(var(--m3e-slider-extra-small-track-height, 16px)); } :host(:is(:state(--vertical), :--vertical):is(:state(--small), :--small)) .track { height: auto; width: calc(var(--m3e-slider-small-track-height, 24px)); } :host(:is(:state(--vertical), :--vertical):is(:state(--medium), :--medium)) { height: auto; width: var(--m3e-slider-medium-height, 52px); } :host(:is(:state(--vertical), :--vertical):is(:state(--medium), :--medium)) .track { height: auto; width: var(--m3e-slider-medium-track-height, 40px); } :host(:is(:state(--vertical), :--vertical):is(:state(--large), :--large)) { height: auto; width: var(--m3e-slider-large-height, 68px); } :host(:is(:state(--vertical), :--vertical):is(:state(--large), :--large)) .track { height: auto; width: var(--m3e-slider-large-track-height, 56px); } :host(:is(:state(--vertical), :--vertical):is(:state(--extra-large), :--extra-large)) { height: auto; width: var(--m3e-slider-extra-large-height, 108px); } :host(:is(:state(--vertical), :--vertical):is(:state(--extra-large), :--extra-large)) .track { height: auto; width: var(--m3e-slider-extra-large-track-height, 96px); } :host(:is(:state(--vertical), :--vertical):is(:state(--extra-small), :--extra-small)) .base { --_slider-active-track-shape: var( --m3e-slider-extra-small-active-track-shape, 0 0 ${A.shape.corner.small} ${A.shape.corner.small} ); --_slider-inactive-track-start-shape: var( --m3e-slider-extra-small-inactive-active-track-start-shape, 0 0 ${A.shape.corner.small} ${A.shape.corner.small} ); --_slider-inactive-track-end-shape: var( --m3e-slider-extra-small-inactive-track-end-shape, ${A.shape.corner.small} ${A.shape.corner.small} 0 0 ); } :host(:is(:state(--vertical), :--vertical):is(:state(--small), :--small)) .base { --_slider-active-track-shape: var( --m3e-slider-small-active-track-shape, 0 0 ${A.shape.corner.small} ${A.shape.corner.small} ); --_slider-inactive-track-start-shape: var( --m3e-slider-small-inactive-active-track-start-shape, 0 0 ${A.shape.corner.small} ${A.shape.corner.small} ); --_slider-inactive-track-end-shape: var( --m3e-slider-small-inactive-track-end-shape, ${A.shape.corner.small} ${A.shape.corner.small} 0 0 ); } :host(:is(:state(--vertical), :--vertical):is(:state(--medium), :--medium)) .base { --_slider-active-track-shape: var( --m3e-slider-medium-active-track-shape, 0 0 ${A.shape.corner.medium} ${A.shape.corner.medium} ); --_slider-inactive-track-start-shape: var( --m3e-slider-medium-inactive-active-track-start-shape, 0 0 ${A.shape.corner.medium} ${A.shape.corner.medium} ); --_slider-inactive-track-end-shape: var( --m3e-slider-medium-inactive-track-end-shape, ${A.shape.corner.medium} ${A.shape.corner.medium} 0 0 ); } :host(:is(:state(--vertical), :--vertical):is(:state(--large), :--large)) .base { --_slider-active-track-shape: var( --m3e-slider-large-active-track-shape, 0 0 ${A.shape.corner.large} ${A.shape.corner.large} ); --_slider-inactive-track-start-shape: var( --m3e-slider-large-inactive-active-track-start-shape, 0 0 ${A.shape.corner.large} ${A.shape.corner.large} ); --_slider-inactive-track-end-shape: var( --m3e-slider-large-inactive-track-end-shape, ${A.shape.corner.large} ${A.shape.corner.large} 0 0 ); } :host(:is(:state(--vertical), :--vertical):is(:state(--extra-large), :--extra-large)) .base { --_slider-active-track-shape: var( --m3e-slider-extra-large-active-track-shape, 0 0 ${A.shape.corner.extraLarge} ${A.shape.corner.extraLarge} ); --_slider-inactive-track-start-shape: var( --m3e-slider-extra-large-inactive-active-track-start-shape, 0 0 ${A.shape.corner.extraLarge} ${A.shape.corner.extraLarge} ); --_slider-inactive-track-end-shape: var( --m3e-slider-extra-large-inactive-track-end-shape, ${A.shape.corner.extraLarge} ${A.shape.corner.extraLarge} 0 0 ); } :host(:is(:state(--vertical), :--vertical)) .track-inactive, :host(:is(:state(--vertical), :--vertical)) .track-active { width: 100%; height: auto; margin-inline-start: 0; } :host(:is(:state(--vertical), :--vertical)) .track-active { bottom: var(--_slider-active-track-offset, 0px); height: var(--_slider-active-track-size, 0px); } :host(:is(:state(--vertical), :--vertical)) .track-inactive.start { bottom: 0; height: var(--_slider-inactive-track-before-size, 0px); } :host(:is(:state(--vertical), :--vertical)) .track-inactive.end { bottom: var(--_slider-inactive-track-after-offset, 0px); height: var(--_slider-inactive-track-after-size, 0px); } :host(:is(:state(--vertical), :--vertical):is(:state(--animating), :--animating)) .track-active, :host(:is(:state(--vertical), :--vertical):is(:state(--animating), :--animating)) .track-inactive.start, :host(:is(:state(--vertical), :--vertical):is(:state(--animating), :--animating)) .track-inactive.end { transition: ${c(`bottom ${A.motion.spring.fastEffects},
        height ${A.motion.spring.fastEffects}`)}; } :host(:is(:state(--vertical), :--vertical)) .ticks { width: var(--m3e-slider-tick-size, 4px); height: 100%; } :host(:is(:state(--vertical), :--vertical)) .tick { top: auto; inset-inline-start: 0; bottom: calc(var(--m3e-slider-tick-size, 4px) + calc(var(--m3e-slider-tick-size, 4px) / 2)); }`, e([C(".base")], vy.prototype, "_base", void 0), e([Ze()], vy.prototype, "_ticks", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], vy.prototype, "orientation", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], vy.prototype, "size", void 0), e([S({
	type: Boolean,
	reflect: !0
})], vy.prototype, "disabled", void 0), e([S({
	type: Boolean,
	reflect: !0
})], vy.prototype, "discrete", void 0), e([S({ type: Number })], vy.prototype, "min", void 0), e([S({ type: Number })], vy.prototype, "max", void 0), e([S({ type: Number })], vy.prototype, "step", void 0), e([S({ type: Boolean })], vy.prototype, "labelled", void 0), e([S({ attribute: !1 })], vy.prototype, "displayWith", void 0), vy = e([w("m3e-slider")], vy);
//#endregion
//#region node_modules/@m3e/web/dist/form-field.js
function yy(e) {
	return e === "always" || e === "auto";
}
var by = [
	"m3e-input-chip-set",
	"m3e-select",
	"m3e-date-input"
];
function xy(e) {
	return e instanceof HTMLElement && (e instanceof HTMLInputElement || e instanceof HTMLTextAreaElement || e instanceof HTMLSelectElement || by.includes(e.tagName.toLowerCase()));
}
function Sy(e) {
	for (let t of e.assignedElements({ flatten: !0 })) {
		if (xy(t)) return t;
		let e = document.createTreeWalker(t, NodeFilter.SHOW_ELEMENT);
		for (; e.nextNode();) if (xy(e.currentNode)) return e.currentNode;
	}
	return null;
}
function Cy(e) {
	return e === "filled" || e === "outlined";
}
function wy(e) {
	return e === "always" || e === "auto" || e === "never";
}
var Z, Ty, Q, Ey, Dy, Oy, ky, Ay, jy, My, Ny, Py, Fy, Iy, Ly, Ry, zy, By, Vy, Hy, Uy, Wy, Gy, Ky, qy, Jy, Yy, Xy, Zy, Qy, $y, eb, tb, nb, rb, ib, ab, ob = class extends ea(T(x)) {
	constructor() {
		super(), Z.add(this), Ty.set(this, !1), Q.set(this, null), Ey.set(this, void 0), Dy.set(this, () => t(this, Z, "m", $y).call(this)), Oy.set(this, () => t(this, Z, "m", Zy).call(this)), ky.set(this, new Fn(this, {
			target: null,
			config: { attributeFilter: [
				"disabled",
				"readonly",
				"required"
			] },
			callback: () => this.notifyControlStateChange()
		})), Ay.set(this, new cr(this, {
			target: null,
			callback: () => t(this, Z, "m", Jy).call(this)
		})), jy.set(this, new sn(this, {
			target: null,
			filter: (e) => t(this, Z, "m", eb).call(this, e),
			callback: (e) => {
				e &&= !(t(this, Q, "f")?.disabled ?? !0), D(this, "--no-animate", !1), n(this, Iy, e, "f"), e ? D(this, "--float-label", !0) : (this._invalid = !(t(this, Q, "f")?.checkValidity?.() ?? !0), this.notifyControlStateChange());
			}
		})), My.set(this, new Fn(this, {
			target: null,
			config: {
				childList: !0,
				subtree: !0,
				characterData: !0
			},
			callback: () => t(this, Z, "m", Gy).call(this)
		})), Ny.set(this, new Fn(this, {
			target: null,
			config: {
				childList: !0,
				subtree: !0
			},
			callback: () => t(this, Z, "m", rb).call(this)
		})), Py.set(this, new Fn(this, {
			target: null,
			config: {
				childList: !0,
				subtree: !0
			},
			callback: () => t(this, Z, "m", ib).call(this)
		})), Fy.set(this, new nr(this, {
			target: null,
			filter: (e) => t(this, Z, "m", eb).call(this, e),
			callback: (e) => {
				D(this, "--pressed", e && !(t(this, Q, "f")?.disabled ?? !0)), !e && !t(this, Z, "a", zy) && E(this, "--float-label");
			}
		})), Iy.set(this, !1), this._pseudoLabel = "", this._required = !1, this._invalid = !1, this._validationMessage = "", Ly.set(this, ""), Ry.set(this, ""), this.variant = "outlined", this.hideRequiredMarker = !1, this.hideSubscript = "auto", this.floatLabel = "auto", this.error = !1, new yn(this, { callback: () => D(this, "--no-animate", !1) });
	}
	get menuAnchor() {
		return this._base;
	}
	get control() {
		return t(this, Q, "f");
	}
	notifyControlStateChange(e = !1) {
		this._required = t(this, Q, "f")?.required === !0, D(this, "--required", this._required), D(this, "--disabled", t(this, Q, "f")?.disabled === !0), D(this, "--readonly", Qi(t(this, Q, "f")) && t(this, Q, "f").readOnly === !0), this.floatLabel === "auto" && D(this, "--float-label", t(this, Z, "a", zy)), e && (this._invalid = !(t(this, Q, "f")?.checkValidity?.() ?? !0)), D(this, "--invalid", this._invalid), this._validationMessage = t(this, Q, "f")?.validationMessage ?? "", this.isUpdatePending || this.performUpdate();
	}
	connectedCallback() {
		super.connectedCallback(), t(this, Z, "m", By).call(this), t(this, Z, "m", Vy).call(this), t(this, Z, "m", Hy).call(this), D(this, "--no-animate", !0);
	}
	disconnectedCallback() {
		super.disconnectedCallback(), t(this, Z, "m", tb).call(this, null);
	}
	reconnectedCallback() {
		super.reconnectedCallback(), t(this, Z, "m", Uy).call(this);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, Z, "m", By).call(this), e.has("floatLabel") && t(this, Z, "m", Vy).call(this), e.has("hideSubscript") && t(this, Z, "m", Hy).call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), t(this, Z, "m", Uy).call(this);
	}
	update(e) {
		super.update(e), e.has("error") && (n(this, Ty, !0, "f"), t(this, Z, "m", ab).call(this)), e.has("_invalid") && t(this, Q, "f") && (t(this, Q, "f").ariaInvalid = this._invalid ? "true" : null, t(this, Ry, "f") && (this._invalid ? Xu.describe(t(this, Q, "f"), t(this, Ry, "f")) : Xu.removeDescription(t(this, Q, "f"), t(this, Ry, "f"))));
	}
	render() {
		return y`<div class="base" @click="${t(this, Z, "m", Xy)}">${this.variant === "outlined" ? y`<div class="outline" aria-hidden="true"><div class="outline-start"></div><div class="outline-notch"><div class="pseudo-label">${this._pseudoLabel} ${!this.hideRequiredMarker && this._required ? y`&nbsp;*` : b}</div></div><div class="outline-end"></div></div>` : b}<div class="prefix"><slot name="prefix" @slotchange="${t(this, Z, "m", Ky)}"></slot></div><div class="content"><span class="prefix-text"><slot name="prefix-text"></slot></span><span class="input"><slot @slotchange="${t(this, Z, "m", Yy)}" @change="${t(this, Z, "m", Qy)}"></slot></span><span class="suffix-text"><slot name="suffix-text"></slot></span><span class="label"><slot name="label" @slotchange="${t(this, Z, "m", Wy)}"></slot>${!this.hideRequiredMarker && this._required ? y`<span class="required-marker" aria-hidden="true">&nbsp;*</span>` : b}</span></div><div class="suffix"><slot name="suffix" @slotchange="${t(this, Z, "m", qy)}"></slot></div></div><span class="subscript" aria-hidden="true"><span class="error"><slot name="error">${this._validationMessage}</slot></span><span class="hint"><slot name="hint"></slot></span></span>`;
	}
};
Ty = /* @__PURE__ */ new WeakMap(), Q = /* @__PURE__ */ new WeakMap(), Ey = /* @__PURE__ */ new WeakMap(), Dy = /* @__PURE__ */ new WeakMap(), Oy = /* @__PURE__ */ new WeakMap(), ky = /* @__PURE__ */ new WeakMap(), Ay = /* @__PURE__ */ new WeakMap(), jy = /* @__PURE__ */ new WeakMap(), My = /* @__PURE__ */ new WeakMap(), Ny = /* @__PURE__ */ new WeakMap(), Py = /* @__PURE__ */ new WeakMap(), Fy = /* @__PURE__ */ new WeakMap(), Iy = /* @__PURE__ */ new WeakMap(), Ly = /* @__PURE__ */ new WeakMap(), Ry = /* @__PURE__ */ new WeakMap(), Z = /* @__PURE__ */ new WeakSet(), zy = function() {
	return t(this, Iy, "f") || Rr(this, "--pressed") || (t(this, Q, "f")?.shouldLabelFloat === void 0 ? typeof t(this, Q, "f")?.value == "string" && t(this, Q, "f").value.length > 0 : t(this, Q, "f").shouldLabelFloat === !0);
}, By = function() {
	Cy(this.variant) || (this.variant = "outlined"), Br(this, this.variant, "filled", "outlined");
}, Vy = function() {
	yy(this.floatLabel) || (this.floatLabel = "auto"), D(this, "--float-label-auto", this.floatLabel === "auto");
}, Hy = function() {
	wy(this.hideSubscript) || (this.hideSubscript = "auto"), D(this, "--hide-subscript-always", this.hideSubscript === "always"), D(this, "--hide-subscript-auto", this.hideSubscript === "auto"), D(this, "--hide-subscript-never", this.hideSubscript === "never");
}, Uy = function() {
	t(this, jy, "f").observe(this._base), t(this, Fy, "f").observe(this._base), t(this, My, "f").observe(this._labelSlot), t(this, Ny, "f").observe(this._hint), t(this, Z, "m", rb).call(this), t(this, Py, "f").observe(this._error), t(this, Z, "m", ib).call(this);
}, Wy = function(e) {
	t(this, Z, "m", Gy).call(this, e.target);
}, Gy = function(e = this._labelSlot) {
	e && (D(this, "--with-label", e.assignedElements({ flatten: !0 }).length > 0), this._pseudoLabel = Lt(e));
}, Ky = function(e) {
	D(this, "--with-prefix", Rt(e.target)), t(this, Ay, "f").observe(this._prefix);
}, qy = function(e) {
	D(this, "--with-suffix", Rt(e.target));
}, Jy = function() {
	this.variant === "outlined" && this._base.style.setProperty("--_prefix-width", `${this._prefix.clientWidth}px`);
}, Yy = function(e) {
	t(this, Z, "m", tb).call(this, Sy(e.target));
}, Xy = function(e) {
	t(this, Z, "m", eb).call(this, e, !0) || t(this, Q, "f") && !t(this, Iy, "f") && !t(this, Q, "f").disabled && (t(this, Q, "f").onContainerClick ? t(this, Q, "f").onContainerClick(e) : t(this, Q, "f").focus());
}, Zy = function() {
	this._invalid = !0, this.notifyControlStateChange();
}, Qy = function() {
	this._invalid = !(t(this, Q, "f")?.checkValidity?.() ?? !0), this.notifyControlStateChange();
}, $y = function() {
	this._invalid = !1, setTimeout(() => this.notifyControlStateChange());
}, eb = function(e, t = !1) {
	if (e.composed) {
		let n = e.composedPath();
		return n.includes(this._suffix) || t && this.control !== null && n.includes(this.control);
	}
	return !1;
}, tb = function(e) {
	if (t(this, Q, "f") !== e && (t(this, Q, "f") && (t(this, Ly, "f") && Xu.removeDescription(t(this, Q, "f"), t(this, Ly, "f")), t(this, Ry, "f") && Xu.removeDescription(t(this, Q, "f"), t(this, Ry, "f")), t(this, ky, "f").unobserve(t(this, Q, "f")), t(this, Q, "f").removeEventListener("invalid", t(this, Oy, "f")), t(this, Q, "f").form?.removeEventListener("reset", t(this, Dy, "f")), t(this, Ey, "f")?.call(this), n(this, Ey, void 0, "f")), n(this, Q, e, "f"), [
		"INPUT",
		"TEXTAREA",
		"M3E-DATE-INPUT"
	].includes(t(this, Q, "f")?.tagName ?? "") ? this._base.style.setProperty("--_form-field-cursor", "text") : this._base.style.removeProperty("--_form-field-cursor"), D(this, "--with-select", t(this, Q, "f")?.tagName === "M3E-SELECT"), Rr(this, "--with-select") && this._base.style.setProperty("--_form-field-cursor", "pointer"), n(this, Ty, this.error, "f"), t(this, Q, "f"))) {
		t(this, ky, "f").observe(t(this, Q, "f")), t(this, Q, "f").addEventListener("invalid", t(this, Oy, "f")), t(this, Q, "f").form?.addEventListener("reset", t(this, Dy, "f")), t(this, Q, "f").removeAttribute("aria-invalid"), t(this, Ly, "f") && Xu.describe(t(this, Q, "f"), t(this, Ly, "f"));
		let e = t(this, Q, "f").tagName.toLowerCase();
		e.startsWith("m3e-") && !customElements.get(e) ? customElements.whenDefined(e).then(() => t(this, Z, "m", nb).call(this)) : t(this, Z, "m", nb).call(this), t(this, Ty, "f") ? t(this, Z, "m", ab).call(this) : this.notifyControlStateChange();
	}
}, nb = function() {
	t(this, Q, "f") && n(this, Ey, zt(t(this, Q, "f"), "value", { set: (e, t) => {
		t(e), this.notifyControlStateChange(!0);
	} }), "f");
}, rb = function() {
	let e = Lt(this._hint, !0);
	e !== t(this, Ly, "f") && (t(this, Q, "f") && t(this, Ly, "f") && Xu.removeDescription(t(this, Q, "f"), t(this, Ly, "f")), n(this, Ly, e, "f"), t(this, Q, "f") && t(this, Ly, "f") && Xu.describe(t(this, Q, "f"), t(this, Ly, "f")));
}, ib = function() {
	let e = Lt(this._error, !0);
	e !== t(this, Ry, "f") && (t(this, Ty, "f") && t(this, Z, "m", ab).call(this), t(this, Q, "f") && t(this, Ry, "f") && Xu.removeDescription(t(this, Q, "f"), t(this, Ry, "f")), n(this, Ry, e, "f"), t(this, Q, "f") && t(this, Ry, "f") && this._invalid && Xu.describe(t(this, Q, "f"), t(this, Ry, "f")));
}, ab = function() {
	t(this, Ty, "f") && (this.error ? this.control?.setAttribute("aria-invalid", "true") : this.control?.removeAttribute("aria-invalid"), this.control?.setCustomValidity?.(this.error ? t(this, Ry, "f") : ""), t(this, Z, "m", Qy).call(this));
}, Vt(l`m3e-form-field input::placeholder, m3e-form-field textarea::placeholder { user-select: none; color: currentColor; transition: opacity ${A.motion.duration.extraLong1}; } m3e-form-field:is(:state(--float-label-auto), :--float-label-auto):not( :is(:state(--float-label), :--float-label) ):is(:state(--with-label), :--with-label) input::placeholder, m3e-form-field:is(:state(--float-label-auto), :--float-label-auto):not( :is(:state(--float-label), :--float-label) ):is(:state(--with-label), :--with-label) textarea::placeholder { opacity: 0; transition: opacity 0s; } m3e-form-field:is(:state(--outlined), :--outlined) m3e-input-chip-set { margin-block: calc(calc(56px + ${A.density.calc(-3)}) / 4); } m3e-form-field:is(:state(--outlined), :--outlined) textarea { margin-block: calc( var(--m3e-form-field-label-line-height, var(--md-sys-typescale-body-small-line-height, 16px)) / 2 ); } @media (prefers-reduced-motion) { m3e-form-field input::placeholder, m3e-form-field textarea::placeholder { transition: none !important; } }`), ob.styles = l`:host { display: inline-flex; flex-direction: column; vertical-align: middle; font-size: var(--m3e-form-field-font-size, ${A.typescale.standard.body.large.fontSize}); font-weight: var(--m3e-form-field-font-weight, ${A.typescale.standard.body.large.fontWeight}); line-height: var(--m3e-form-field-line-height, ${A.typescale.standard.body.large.lineHeight}); letter-spacing: var(--m3e-form-field-tracking, ${A.typescale.standard.body.large.tracking}); width: var(--m3e-form-field-width, 270px); color: var(--_form-field-color); } :host([hidden]) { display: none; } :host(:not(:is(:state(--disabled), :--disabled))) .base { cursor: var(--_form-field-cursor); } .base { display: flex; align-items: center; position: relative; min-height: calc(56px + ${A.density.calc(-3)}); --_form-field-label-font-size: var( --m3e-form-field-label-font-size, ${A.typescale.standard.body.small.fontSize} ); --_form-field-label-line-height: var( --m3e-form-field-label-line-height, ${A.typescale.standard.body.small.lineHeight} ); } .content { display: flex; align-items: center; position: relative; flex: 1 1 auto; min-width: 0; min-height: var(--m3e-form-field-icon-size, 24px); } .prefix, .suffix { display: flex; align-items: center; position: relative; user-select: none; flex: none; font-size: var(--m3e-form-field-icon-size, 24px); } .prefix-text, .suffix-text { opacity: 1; transition: opacity ${A.motion.duration.extraLong1}; user-select: none; flex: none; } .input { display: inline-flex; flex-wrap: wrap; flex: 1 1 auto; min-width: 0; } .label { display: flex; position: absolute; pointer-events: none; user-select: none; top: 0; left: 0; right: 0; font-size: var(--m3e-form-field-label-font-size, ${A.typescale.standard.body.small.fontSize}); font-weight: var(--m3e-form-field-label-font-weight, ${A.typescale.standard.body.small.fontWeight}); line-height: var(--m3e-form-field-label-line-height, ${A.typescale.standard.body.small.lineHeight}); letter-spacing: var(--m3e-form-field-label-tracking, ${A.typescale.standard.body.small.tracking}); color: var(--_form-field-label-color, inherit); transition: ${c(`top ${A.motion.duration.short4}, 
        font-size ${A.motion.duration.short4}, 
        line-height ${A.motion.duration.short4}`)}; } :host(:is(:state(--with-select), :--with-select)) .label { margin-inline-end: 24px; } ::slotted([slot="label"]) { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; } .subscript { display: inline-flex; width: 100%; margin-top: 4px; font-size: var(--m3e-form-field-subscript-font-size, ${A.typescale.standard.body.small.fontSize}); font-weight: var(--m3e-form-field-subscript-font-weight, ${A.typescale.standard.body.small.fontWeight}); line-height: var(--m3e-form-field-subscript-line-height, ${A.typescale.standard.body.small.lineHeight}); letter-spacing: var(--m3e-form-field-subscript-tracking, ${A.typescale.standard.body.small.tracking}); min-height: var(--m3e-form-field-subscript-line-height, ${A.typescale.standard.body.small.lineHeight}); color: var(--m3e-form-field-subscript-color, ${A.color.onSurfaceVariant}); } .error, .hint { flex: 1 1 auto; } :host(:is(:state(--hide-subscript-always), :--hide-subscript-always)) .subscript { display: none; } :host(:is(:state(--hide-subscript-auto), :--hide-subscript-auto):not(:is(:state(--invalid), :--invalid))) .subscript { opacity: 0; margin-top: 4px; transform: translateY(-4px); transition: ${c(`opacity ${A.motion.duration.short4}, 
        transform ${A.motion.duration.short4}`)}; } :host( :is(:state(--hide-subscript-auto), :--hide-subscript-auto):not(:is(:state(--invalid), :--invalid)):focus-within ) .subscript, :host( :is(:state(--hide-subscript-auto), :--hide-subscript-auto):not(:is(:state(--invalid), :--invalid)):is( :state(--pressed), :--pressed ) ) .subscript { opacity: 1; transform: translateY(0); } :host(:is(:state(--invalid), :--invalid)) .hint { display: none; } :host(:not(:is(:state(--invalid), :--invalid))) .error { display: none; } ::slotted(input), ::slotted(textarea), ::slotted(select) { outline: unset; border: unset; background-color: transparent; box-shadow: none; font-family: inherit; font-size: inherit; line-height: inherit; letter-spacing: inherit; color: var(--_form-field-input-color, inherit); flex: 1 1 auto; min-width: 0; padding: unset; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } ::slotted(textarea) { scrollbar-width: ${A.scrollbar.thinWidth}; scrollbar-color: ${A.scrollbar.color}; } ::slotted(m3e-select), ::slotted(m3e-input-chip-set), ::slotted(m3e-date-input) { flex: 1 1 auto; min-width: 0; } :host(:is(:state(--float-label-auto), :--float-label-auto):not(:is(:state(--float-label), :--float-label))) .label { font-size: inherit; } :host( :is(:state(--float-label-auto), :--float-label-auto):not(:is(:state(--float-label), :--float-label)):is( :state(--with-label), :--with-label ) ) .prefix-text, :host( :is(:state(--float-label-auto), :--float-label-auto):not(:is(:state(--float-label), :--float-label)):is( :state(--with-label), :--with-label ) ) .suffix-text { opacity: 0; transition: opacity 0s; } .prefix { margin-inline-start: 16px; } :host(:is(:state(--with-prefix), :--with-prefix)) .prefix { margin-inline-end: 16px; margin-inline-start: 12px; } .suffix { margin-inline-end: 16px; } :host(:is(:state(--with-suffix), :--with-suffix)) .suffix { margin-inline-start: 4px; margin-inline-end: 8px; } :host(:is(:state(--with-suffix), :--with-suffix):is(:state(--with-select), :--with-select)) .suffix { margin-inline-start: unset; } :host(:is(:state(--with-select), :--with-select)) .suffix-text { display: none; } :host(:is(:state(--outlined), :--outlined)) .label { margin-top: calc(0px - var(--_form-field-label-line-height) / 2); } :host(:is(:state(--outlined), :--outlined)) .outline { position: absolute; display: flex; pointer-events: none; left: 0; top: 0; bottom: 0; right: 0; } :host(:is(:state(--outlined), :--outlined)) .pseudo-label { visibility: hidden; margin-inline-end: 8px; font-size: var(--_form-field-label-font-size); line-height: var(--_form-field-label-line-height); letter-spacing: var(--_form-field-label-tracking); max-width: 100%; transition-property: max-width, margin-inline-end; transition-duration: 1ms; } :host( :is(:state(--required), :--required):not([hide-required-marker]):not(:is(:state(--with-label), :--with-label)) ) .pseudo-label, :host( :is(:state(--required), :--required):not([hide-required-marker]):not(:is(:state(--with-label), :--with-label)) ) .required-marker { display: none; } :host(:is(:state(--outlined), :--outlined):is(:state(--required), :--required):not([hide-required-marker])) .pseudo-label { margin-inline-end: 4px; } :host( :is(:state(--outlined), :--outlined):is(:state(--float-label-auto), :--float-label-auto):not( :is(:state(--float-label), :--float-label) ) ) .pseudo-label { max-width: 0; margin-inline-end: 0px; transition-delay: ${A.motion.duration.short2}; } :host(:is(:state(--outlined), :--outlined)) .outline-start, :host(:is(:state(--outlined), :--outlined)) .outline-notch, :host(:is(:state(--outlined), :--outlined)) .outline-end { box-sizing: border-box; border-width: var(--_form-field-outline-size, 1px); border-color: var(--_form-field-outline-color); transition: border-color ${A.motion.duration.short4}; } :host(:is(:state(--outlined), :--outlined):not(:is(:state(--with-label), :--with-label))) .outline-notch { display: none; } :host(:is(:state(--outlined), :--outlined)) .outline-start { min-width: 12px; border-top-style: solid; border-inline-start-style: solid; border-bottom-style: solid; border-start-start-radius: var(--m3e-outlined-form-field-container-shape, ${A.shape.corner.extraSmall}); border-end-start-radius: var(--m3e-outlined-form-field-container-shape, ${A.shape.corner.extraSmall}); } :host(:is(:state(--outlined), :--outlined)) .outline-notch { border-bottom-style: solid; } :host(:is(:state(--outlined), :--outlined)) .outline-end { flex-grow: 1; min-width: 16px; border-top-style: solid; border-inline-end-style: solid; border-bottom-style: solid; border-start-end-radius: var(--m3e-outlined-form-field-container-shape, ${A.shape.corner.extraSmall}); border-end-end-radius: var(--m3e-outlined-form-field-container-shape, ${A.shape.corner.extraSmall}); } :host(:is(:state(--outlined), :--outlined):is(:state(--with-prefix), :--with-prefix)) .outline-start { min-width: calc(20px + var(--_prefix-width, 0px) + 4px); } :host(:is(:state(--outlined), :--outlined):not(:is(:state(--disabled), :--disabled))) .base:hover .outline, :host(:is(:state(--outlined), :--outlined):not(:is(:state(--disabled), :--disabled)):focus-within) .outline, :host( :is(:state(--outlined), :--outlined):not(:is(:state(--disabled), :--disabled)):is(:state(--pressed), :--pressed) ) .outline { --_form-field-outline-size: 2px; } :host(:is(:state(--outlined), :--outlined)) .subscript { margin-inline: 16px; width: calc(100% - 32px); } :host(:is(:state(--outlined), :--outlined)) .content { min-height: calc(56px + ${A.density.calc(-3)}); --_form-field-label-font-size: var( --m3e-form-field-label-font-size, ${A.typescale.standard.body.small.fontSize} ); } :host( :is(:state(--outlined), :--outlined):is(:state(--float-label-auto), :--float-label-auto):not( :is(:state(--float-label), :--float-label) ) ) .label { margin-top: unset; line-height: calc(56px + ${A.density.calc(-3)}); --_form-field-label-font-size: var( --m3e-form-field-label-font-size, ${A.typescale.standard.body.small.fontSize} ); } :host(:is(:state(--filled), :--filled)) .base { --_select-arrow-margin-top: calc( 0px - calc(16px / max(calc(0 - calc(var(--md-sys-density-scale, 0) + var(--md-sys-density-scale, 0))), 1)) ); } :host(:is(:state(--filled), :--filled)) .base::before { content: ""; box-sizing: border-box; position: absolute; pointer-events: none; top: 0; left: 0; right: 0; bottom: 0; border-bottom-style: solid; border-width: 1px; border-radius: var(--m3e-form-field-container-shape, ${A.shape.corner.extraSmallTop}); border-color: var(--_form-field-outline-color); background-color: var(--_form-field-container-color); } :host(:is(:state(--filled), :--filled):not(:is(:state(--disabled), :--disabled))) .base:hover::before, :host(:is(:state(--filled), :--filled):not(:is(:state(--disabled), :--disabled)):focus-within) .base::before, :host(:is(:state(--filled), :--filled):not(:is(:state(--disabled), :--disabled)):is(:state(--pressed), :--pressed)) .base::before { border-width: 3px; } :host(:is(:state(--filled), :--filled)) .base::after { content: ""; box-sizing: border-box; position: absolute; pointer-events: none; top: 0; left: 0; right: 0; bottom: 0; background-color: var(--_form-field-hover-container-color); transition: background-color ${A.motion.duration.short4}; } :host(:is(:state(--filled), :--filled)) .subscript { margin-inline: 16px; width: calc(100% - 32px); } :host(:is(:state(--filled), :--filled)) .content { padding-top: calc(24px + ${A.density.calc(-3)}); margin-bottom: 8px; } :host(:is(:state(--filled), :--filled)) .label { top: max(0px, calc(8px + ${A.density.calc(-3)})); } :host( :is(:state(--filled), :--filled):is(:state(--float-label-auto), :--float-label-auto):not( :is(:state(--float-label), :--float-label) ) ) .label { top: 0px; line-height: calc(56px + ${A.density.calc(-3)} - 1px); --_form-field-label-font-size: var( --m3e-form-field-label-font-size, ${A.typescale.standard.body.small.fontSize} ); } :host(:not(:is(:state(--disabled), :--disabled)):not(:focus-within):not(:is(:state(--pressed), :--pressed))) .base:hover { --_form-field-hover-container-color: color-mix( in srgb, var(--m3e-form-field-hover-container-color, ${A.color.onSurface}) var(--m3e-form-field-hover-container-opacity, 8%), transparent ); } :host(:not(:is(:state(--disabled), :--disabled)):not(:is(:state(--invalid), :--invalid))) { color: var(--m3e-form-field-color, ${A.color.onSurface}); } :host( :is(:state(--outlined), :--outlined):not(:is(:state(--disabled), :--disabled)):not( :is(:state(--invalid), :--invalid) ) ) .base { --_form-field-outline-color: var(--m3e-form-field-outline-color, ${A.color.outline}); } :host( :is(:state(--filled), :--filled):not(:is(:state(--disabled), :--disabled)):not( :is(:state(--invalid), :--invalid) ) ) .base { --_form-field-outline-color: var(--m3e-form-field-outline-color, ${A.color.onSurfaceVariant}); } :host( :is(:state(--outlined), :--outlined):not(:is(:state(--disabled), :--disabled)):not( :is(:state(--invalid), :--invalid) ):focus-within ) .base, :host( :is(:state(--outlined), :--outlined):not(:is(:state(--disabled), :--disabled)):not( :is(:state(--invalid), :--invalid) ):is(:state(--pressed), :--pressed) ) .base, :host( :is(:state(--filled), :--filled):not(:is(:state(--disabled), :--disabled)):not( :is(:state(--invalid), :--invalid) ):focus-within ) .base, :host( :is(:state(--filled), :--filled):not(:is(:state(--disabled), :--disabled)):not( :is(:state(--invalid), :--invalid) ):is(:state(--pressed), :--pressed) ) .base { --_form-field-outline-color: var(--m3e-form-field-focused-outline-color, ${A.color.primary}); --_form-field-label-color: var(--m3e-form-field-focused-color, ${A.color.primary}); } :host(:not(:is(:state(--disabled), :--disabled))) .base { --_form-field-container-color: var( --m3e-form-field-container-color, ${A.color.surfaceContainerHighest} ); } :host(:not(:is(:state(--disabled), :--disabled)):is(:state(--invalid), :--invalid)) .base { --_form-field-label-color: var(--m3e-form-field-invalid-color, ${A.color.error}); --_form-field-outline-color: var(--m3e-form-field-invalid-color, ${A.color.error}); } :host(:not(:is(:state(--disabled), :--disabled)):is(:state(--invalid), :--invalid)) .subscript { color: var(--m3e-form-field-invalid-color, ${A.color.error}); } :host(:is(:state(--disabled), :--disabled)) { color: color-mix( in srgb, var(--m3e-form-field-disabled-color, ${A.color.onSurface}) var(--m3e-form-field-disabled-opacity, 38%), transparent ); } :host(:is(:state(--disabled), :--disabled)) .base { --_form-field-container-color: color-mix( in srgb, var(--m3e-form-field-disabled-container-color, ${A.color.onSurface}) var(--m3e-form-field-disabled-container-opacity, 4%), transparent ); } :host(:is(:state(--no-animate), :--no-animate)) *, :host(:is(:state(--no-animate), :--no-animate)) *::before, :host(:is(:state(--no-animate), :--no-animate)) *::after { transition: none !important; } @media (forced-colors: active) { :host(:is(:state(--filled), :--filled)) .base::after { transition: none; } :host { --_form-field-outline-color: CanvasText; } :host(:is(:state(--disabled), :--disabled)) { --_form-field-input-color: GrayText; --_form-field-color: GrayText; --_form-field-label-color: GrayText; --_form-field-outline-color: GrayText; } } @media (prefers-reduced-motion) { .base::before, .prefix-text, .suffix-text, .label, .subscript, .outline-start, .outline-notch, .outline-end, .pseudo-label { transition: none !important; } }`, e([C(".base")], ob.prototype, "_base", void 0), e([C(".prefix")], ob.prototype, "_prefix", void 0), e([C(".suffix")], ob.prototype, "_suffix", void 0), e([C(".error")], ob.prototype, "_error", void 0), e([C(".hint")], ob.prototype, "_hint", void 0), e([C("slot[name=\"label\"]")], ob.prototype, "_labelSlot", void 0), e([Ze()], ob.prototype, "_pseudoLabel", void 0), e([Ze()], ob.prototype, "_required", void 0), e([Ze()], ob.prototype, "_invalid", void 0), e([Ze()], ob.prototype, "_validationMessage", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], ob.prototype, "variant", void 0), e([S({
	attribute: "hide-required-marker",
	type: Boolean,
	reflect: !0
})], ob.prototype, "hideRequiredMarker", void 0), e([S({
	attribute: "hide-subscript",
	reflect: !0,
	useDefault: !0
})], ob.prototype, "hideSubscript", void 0), e([S({
	attribute: "float-label",
	reflect: !0,
	useDefault: !0
})], ob.prototype, "floatLabel", void 0), e([S({
	type: Boolean,
	reflect: !1,
	useDefault: !0
})], ob.prototype, "error", void 0), ob = e([w("m3e-form-field")], ob);
//#endregion
//#region node_modules/@m3e/web/dist/gestures.js
var sb = {
	priority: 1,
	buttons: ["primary"],
	pointerTypes: [
		"mouse",
		"pen",
		"touch"
	]
}, cb, lb, ub, db, fb, pb = /* @__PURE__ */ new Map([
	["primary", 0],
	["middle", 1],
	["secondary", 2],
	["back", 3],
	["forward", 4]
]), mb = class {
	constructor(e, t) {
		cb.add(this), lb.set(this, void 0), ub.set(this, !1), db.set(this, []), n(this, lb, {
			...this.defaultOptions,
			...e
		}, "f"), t && this.addListener(t);
	}
	get options() {
		return t(this, lb, "f");
	}
	set options(e) {
		n(this, lb, {
			...t(this, lb, "f"),
			...e
		}, "f");
	}
	get disabled() {
		return t(this, ub, "f");
	}
	set disabled(e) {
		n(this, ub, e, "f"), t(this, ub, "f") && this.reset();
	}
	get defaultOptions() {
		return { ...sb };
	}
	get eager() {
		return !1;
	}
	canReceiveInput(e) {
		if (this.disabled || !(this.options.inputFilter?.(e) ?? !0)) return !1;
		if (e.kind === "pointerdown" && this.options.buttons.length > 0) {
			let t = !1;
			for (let n of this.options.buttons) {
				let r = pb.get(n);
				if (r === void 0) continue;
				let i = 1 << r;
				if ((e.buttons & i) !== 0) {
					t = !0;
					break;
				}
			}
			if (!t) return !1;
		}
		return !(this.options.pointerTypes.length > 0 && "pointerType" in e && !this.options.pointerTypes.includes(e.pointerType));
	}
	shouldCapturePointer(e) {
		return !1;
	}
	onInput(e) {
		if (this.canReceiveInput(e)) switch (e.kind) {
			case "pointerover":
				this._onPointerOver(e);
				break;
			case "pointerenter":
				this._onPointerEnter(e);
				break;
			case "pointerdown":
				this._onPointerDown(e);
				break;
			case "pointermove":
				this._onPointerMove(e);
				break;
			case "pointerup":
				this._onPointerUp(e);
				break;
			case "pointercancel":
				this._onPointerCancel(e);
				break;
			case "pointerout":
				this._onPointerOut(e);
				break;
			case "pointerleave":
				this._onPointerLeave(e);
				break;
			case "wheel": this._onWheel(e);
		}
	}
	_onPointerOver(e) {}
	_onPointerEnter(e) {}
	_onPointerDown(e) {}
	_onPointerMove(e) {}
	_onPointerUp(e) {}
	_onPointerCancel(e) {}
	_onPointerOut(e) {}
	_onPointerLeave(e) {}
	_onWheel(e) {}
	_accept(e) {
		t(this, cb, "m", fb).call(this, e, "accept");
	}
	_reject(e) {
		t(this, cb, "m", fb).call(this, e, "reject");
	}
	_hold(e) {
		t(this, cb, "m", fb).call(this, e, "hold");
	}
	_release(e) {
		t(this, cb, "m", fb).call(this, e, "release");
	}
	_defer(e) {
		t(this, cb, "m", fb).call(this, e, "defer");
	}
	_emit(e) {
		t(this, db, "f").forEach((t) => t(e));
	}
	addListener(e) {
		t(this, db, "f").includes(e) || t(this, db, "f").push(e);
	}
	removeListener(e) {
		let n = t(this, db, "f").indexOf(e);
		n >= 0 && t(this, db, "f").splice(n, 1);
	}
	onResolution(e, t) {
		switch (t) {
			case "accept":
				this._onAccept(e);
				break;
			case "reject": this._onReject(e);
		}
	}
};
lb = /* @__PURE__ */ new WeakMap(), ub = /* @__PURE__ */ new WeakMap(), db = /* @__PURE__ */ new WeakMap(), cb = /* @__PURE__ */ new WeakSet(), fb = function(e, t) {
	let n = Array.isArray(e) ? e : [e];
	for (let e of n) this.onDisposition?.(e, t);
};
var hb, gb, _b = class extends mb {
	constructor(e, t, n) {
		super(e, t), hb.set(this, (e) => this._handleGesture(e)), gb.set(this, void 0), n && (this._inner = n);
	}
	get _inner() {
		return t(this, gb, "f");
	}
	set _inner(e) {
		t(this, gb, "f") && (t(this, gb, "f").removeListener(t(this, hb, "f")), t(this, gb, "f").onDisposition = void 0, t(this, gb, "f").reset()), n(this, gb, e, "f"), t(this, gb, "f") && (t(this, gb, "f").reset(), t(this, gb, "f").disabled = this.disabled, this._applyOptions(this.options, t(this, gb, "f")), t(this, gb, "f").onDisposition = this._handleDisposition.bind(this), t(this, gb, "f").addListener(t(this, hb, "f")));
	}
	get disabled() {
		return super.disabled;
	}
	set disabled(e) {
		super.disabled = e, this._inner && (this._inner.disabled = e);
	}
	get options() {
		return super.options;
	}
	set options(e) {
		super.options = e, this._inner && this._applyOptions(this.options, this._inner);
	}
	get eager() {
		return this._inner?.eager ?? !1;
	}
	shouldCapturePointer(e) {
		return this._inner?.shouldCapturePointer(e) ?? !1;
	}
	canReceiveInput(e) {
		return this._inner?.canReceiveInput(e) ?? !1;
	}
	_onPointerOver(e) {
		this._inner?.onInput(e);
	}
	_onPointerEnter(e) {
		this._inner?.onInput(e);
	}
	_onPointerDown(e) {
		this._inner?.onInput(e);
	}
	_onPointerMove(e) {
		this._inner?.onInput(e);
	}
	_onPointerUp(e) {
		this._inner?.onInput(e);
	}
	_onPointerCancel(e) {
		this._inner?.onInput(e);
	}
	_onPointerOut(e) {
		this._inner?.onInput(e);
	}
	_onPointerLeave(e) {
		this._inner?.onInput(e);
	}
	_onWheel(e) {
		this._inner?.onInput(e);
	}
	_onAccept(e) {
		this._inner?.onResolution(e, "accept");
	}
	_onReject(e) {
		this._inner?.onResolution(e, "reject");
	}
	reset() {
		this._inner?.reset();
	}
	_applyOptions(e, t) {
		t.options = {
			priority: e.priority,
			buttons: e.buttons,
			pointerTypes: e.pointerTypes,
			inputFilter: e.inputFilter
		};
	}
	_handleDisposition(e, t) {
		switch (t) {
			case "accept":
				this._accept(e);
				break;
			case "defer":
				this._defer(e);
				break;
			case "hold":
				this._hold(e);
				break;
			case "reject":
				this._reject(e);
				break;
			case "release": this._release(e);
		}
	}
};
hb = /* @__PURE__ */ new WeakMap(), gb = /* @__PURE__ */ new WeakMap();
var vb, yb, bb, xb, Sb, Cb, wb, Tb, Eb, Db, Ob, kb, Ab = class {
	constructor() {
		vb.add(this), yb.set(this, []), bb.set(this, /* @__PURE__ */ new Map());
	}
	get recognizers() {
		return t(this, yb, "f");
	}
	addRecognizer(e) {
		t(this, yb, "f").includes(e) || (e.reset(), t(this, yb, "f").push(e), e.onDisposition = t(this, vb, "m", xb).bind(this, e));
	}
	removeRecognizer(e) {
		let n = t(this, yb, "f").indexOf(e);
		n >= 0 && (e.reset(), t(this, yb, "f").splice(n, 1), e.onDisposition = void 0);
	}
	resolve(e) {
		t(this, vb, "m", Db).call(this, e, !0);
	}
	clear() {
		t(this, bb, "f").clear();
	}
	destroy() {
		t(this, yb, "f").forEach((e) => this.removeRecognizer(e)), t(this, bb, "f").clear();
	}
};
yb = /* @__PURE__ */ new WeakMap(), bb = /* @__PURE__ */ new WeakMap(), vb = /* @__PURE__ */ new WeakSet(), xb = function(e, n, r) {
	switch (r) {
		case "accept":
			t(this, vb, "m", Sb).call(this, e, n);
			break;
		case "reject":
			t(this, vb, "m", Cb).call(this, e, n);
			break;
		case "hold":
			t(this, vb, "m", wb).call(this, e, n);
			break;
		case "release":
			t(this, vb, "m", Tb).call(this, e, n);
			break;
		case "defer": t(this, vb, "m", Eb).call(this, e, n);
	}
}, Sb = function(e, n) {
	let r = t(this, bb, "f").get(n) ?? {
		acceptors: [],
		holders: [],
		deferrers: []
	};
	t(this, bb, "f").set(n, r), !r.acceptors.includes(e) && (t(this, vb, "m", Ob).call(this, e, r.deferrers), t(this, vb, "m", Ob).call(this, e, r.holders) ? r.acceptors.unshift(e) : r.acceptors.push(e), (e.eager || r.stale) && (clearTimeout(r.timeout), r.timeout = setTimeout(() => t(this, vb, "m", Db).call(this, n))));
}, Cb = function(e, n) {
	let r = !1, i = t(this, bb, "f").get(n);
	i && (t(this, vb, "m", Ob).call(this, e, i.acceptors), t(this, vb, "m", Ob).call(this, e, i.deferrers), r = t(this, vb, "m", Ob).call(this, e, i.holders)), e.onResolution(n, "reject"), i && (r && i.acceptors.length > 0 && i.holders.length === 0 ? t(this, vb, "m", Db).call(this, n) : i.acceptors.length === 0 && i.holders.length === 0 && i.deferrers.length === 0 && (clearTimeout(i.timeout), t(this, bb, "f").delete(n)));
}, wb = function(e, n) {
	let r = t(this, bb, "f").get(n) ?? {
		acceptors: [],
		holders: [],
		deferrers: []
	};
	t(this, bb, "f").set(n, r), t(this, vb, "m", kb).call(this, e, r.holders), t(this, vb, "m", Ob).call(this, e, r.deferrers);
}, Tb = function(e, n) {
	let r = t(this, bb, "f").get(n);
	r && (t(this, vb, "m", Ob).call(this, e, r.deferrers), t(this, vb, "m", Ob).call(this, e, r.holders) && r.acceptors.length > 0 && r.holders.length === 0 ? t(this, vb, "m", Db).call(this, n) : r.acceptors.length === 0 && r.holders.length === 0 && r.deferrers.length === 0 && (clearTimeout(r.timeout), t(this, bb, "f").delete(n)));
}, Eb = function(e, n) {
	let r = t(this, bb, "f").get(n) ?? {
		acceptors: [],
		holders: [],
		deferrers: []
	};
	t(this, bb, "f").set(n, r), t(this, vb, "m", kb).call(this, e, r.deferrers), t(this, vb, "m", Ob).call(this, e, r.acceptors), t(this, vb, "m", Ob).call(this, e, r.holders);
}, Db = function(e, n = !1) {
	let r = t(this, bb, "f").get(e);
	if (!r) return;
	if (r.holders.length > 0) {
		n && (r.stale = !0);
		return;
	}
	let i = null;
	for (let e of r.acceptors) e.eager && (!i || e.options.priority > i.options.priority) && (i = e);
	if (!i) for (let e of r.acceptors) (!i || e.options.priority > i.options.priority) && (i = e);
	i ? (i.onResolution(e, "accept"), t(this, vb, "m", Ob).call(this, i, r.acceptors), r.acceptors.forEach((t) => t.onResolution(e, "reject")), r.deferrers.forEach((t) => t.onResolution(e, "reject")), clearTimeout(r.timeout), t(this, bb, "f").delete(e)) : n && (r.stale = !0);
}, Ob = function(e, t) {
	let n = t.indexOf(e);
	return n < 0 ? !1 : (t.splice(n, 1), !0);
}, kb = function(e, t) {
	t.includes(e) || t.push(e);
};
var jb, Mb, Nb, Pb, Fb, Ib, Lb, Rb, zb, Bb, Vb, Hb, Ub, Wb = class {
	constructor(e) {
		jb.add(this), this.element = e, Mb.set(this, new Ab()), Nb.set(this, /* @__PURE__ */ new Set()), Pb.set(this, (e) => t(this, jb, "m", zb).call(this, e)), Fb.set(this, (e) => t(this, jb, "m", Bb).call(this, e)), Ib.set(this, (e) => e.preventDefault()), Lb.set(this, void 0), Rb.set(this, void 0), e.addEventListener("pointerover", t(this, Pb, "f")), e.addEventListener("pointerenter", t(this, Pb, "f")), e.addEventListener("pointerdown", t(this, Pb, "f")), e.addEventListener("pointermove", t(this, Pb, "f")), e.addEventListener("pointerup", t(this, Pb, "f")), e.addEventListener("pointercancel", t(this, Pb, "f")), e.addEventListener("pointerleave", t(this, Pb, "f")), e.addEventListener("pointerout", t(this, Pb, "f")), e.addEventListener("lostpointercapture", t(this, Pb, "f")), e.addEventListener("wheel", t(this, Fb, "f"), { passive: !0 }), e.addEventListener("gesturestart", t(this, Ib, "f")), e.addEventListener("gesturechange", t(this, Ib, "f")), e.addEventListener("gestureend", t(this, Ib, "f")), window !== void 0 && window.ResizeObserver && (n(this, Lb, new ResizeObserver(() => n(this, Rb, void 0, "f")), "f"), t(this, Lb, "f").observe(e));
	}
	get empty() {
		return t(this, Mb, "f").recognizers.length == 0;
	}
	addRecognizer(...e) {
		e.forEach((e) => t(this, Mb, "f").addRecognizer(e));
	}
	removeRecognizer(...e) {
		e.forEach((e) => t(this, Mb, "f").removeRecognizer(e));
	}
	reset() {
		for (let e of t(this, Nb, "f")) this.element.hasPointerCapture(e) && this.element.releasePointerCapture(e);
		n(this, Rb, void 0, "f"), t(this, Nb, "f").clear(), t(this, Mb, "f").clear();
	}
	destroy() {
		t(this, Lb, "f")?.disconnect(), n(this, Rb, void 0, "f"), t(this, Nb, "f").clear(), t(this, Mb, "f").destroy(), this.element.removeEventListener("pointerover", t(this, Pb, "f")), this.element.removeEventListener("pointerenter", t(this, Pb, "f")), this.element.removeEventListener("pointerdown", t(this, Pb, "f")), this.element.removeEventListener("pointermove", t(this, Pb, "f")), this.element.removeEventListener("pointerup", t(this, Pb, "f")), this.element.removeEventListener("pointercancel", t(this, Pb, "f")), this.element.removeEventListener("pointerleave", t(this, Pb, "f")), this.element.removeEventListener("pointerout", t(this, Pb, "f")), this.element.removeEventListener("lostpointercapture", t(this, Pb, "f")), this.element.removeEventListener("wheel", t(this, Fb, "f")), this.element.removeEventListener("gesturestart", t(this, Ib, "f")), this.element.removeEventListener("gesturechange", t(this, Ib, "f")), this.element.removeEventListener("gestureend", t(this, Ib, "f"));
	}
};
Mb = /* @__PURE__ */ new WeakMap(), Nb = /* @__PURE__ */ new WeakMap(), Pb = /* @__PURE__ */ new WeakMap(), Fb = /* @__PURE__ */ new WeakMap(), Ib = /* @__PURE__ */ new WeakMap(), Lb = /* @__PURE__ */ new WeakMap(), Rb = /* @__PURE__ */ new WeakMap(), jb = /* @__PURE__ */ new WeakSet(), zb = function(e) {
	let n = t(this, jb, "m", Hb).call(this, e);
	if (e.type === "lostpointercapture") {
		if (t(this, Nb, "f").has(n.inputId)) {
			t(this, Nb, "f").delete(n.inputId);
			return;
		}
		n = {
			...n,
			kind: "pointercancel"
		};
	}
	switch (t(this, jb, "m", Vb).call(this, n), e.type) {
		case "pointerdown":
		case "pointermove":
			t(this, Mb, "f").recognizers.some((e) => e.shouldCapturePointer(n)) && !n.currentTarget.hasPointerCapture(n.inputId) && n.currentTarget.setPointerCapture(n.inputId);
			break;
		case "pointerup":
		case "pointercancel": t(this, Mb, "f").resolve(n.inputId), n.currentTarget.hasPointerCapture(n.inputId) && (n.currentTarget.releasePointerCapture(n.inputId), t(this, Nb, "f").add(n.inputId));
	}
}, Bb = function(e) {
	t(this, jb, "m", Vb).call(this, t(this, jb, "m", Ub).call(this, e));
}, Vb = function(e) {
	for (let n of t(this, Mb, "f").recognizers) n.canReceiveInput(e) && n.onInput(e);
}, Hb = function(e) {
	let r = t(this, Rb, "f") ?? n(this, Rb, this.element.getBoundingClientRect(), "f");
	return {
		inputId: e.pointerId,
		kind: e.type,
		currentTarget: e.currentTarget,
		target: e.target,
		ctrlKey: e.ctrlKey,
		altKey: e.altKey,
		shiftKey: e.shiftKey,
		metaKey: e.metaKey,
		button: e.button,
		buttons: e.buttons,
		pointerType: e.pointerType,
		clientX: e.clientX,
		clientY: e.clientY,
		localX: e.clientX - r.left,
		localY: e.clientY - r.top,
		timestamp: e.timeStamp
	};
}, Ub = function(e) {
	return {
		inputId: -1,
		kind: e.type,
		currentTarget: e.currentTarget,
		target: e.target,
		ctrlKey: e.ctrlKey,
		altKey: e.altKey,
		shiftKey: e.shiftKey,
		metaKey: e.metaKey,
		button: e.button,
		buttons: e.buttons,
		deltaX: e.deltaX,
		deltaY: e.deltaY,
		deltaZ: e.deltaZ,
		deltaMode: e.deltaMode,
		timestamp: e.timeStamp
	};
};
var Gb, Kb, qb = class {
	static bind(e, ...n) {
		if (!n) return;
		let r = t(this, Gb, "f", Kb).get(e);
		r || (r = new Wb(e), t(this, Gb, "f", Kb).set(e, r)), r.addRecognizer(...n);
	}
	static unbind(e, ...n) {
		let r = t(this, Gb, "f", Kb).get(e);
		r && (r.removeRecognizer(...n), r.empty && (r.destroy(), t(this, Gb, "f", Kb).delete(e)));
	}
};
Gb = qb, Kb = { value: /* @__PURE__ */ new WeakMap() };
var Jb, Yb = class extends Ki(x) {
	constructor() {
		super(...arguments), Jb.set(this, (e) => this.dispatchEvent(new CustomEvent("gesture", { detail: e }))), this.disabled = !1, this.priority = sb.priority, this.buttons = sb.buttons, this.pointerTypes = sb.pointerTypes;
	}
	connectedCallback() {
		super.connectedCallback(), this.recognizer.addListener(t(this, Jb, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.recognizer.removeListener(t(this, Jb, "f"));
	}
	attach(e) {
		super.attach(e), qb.bind(e, this.recognizer);
	}
	detach() {
		this.control && qb.unbind(this.control, this.recognizer), super.detach();
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("disabled") && (this.recognizer.disabled = this.disabled), this.recognizer.options = {
			priority: this.priority,
			buttons: this.buttons,
			pointerTypes: this.pointerTypes,
			inputFilter: this.inputFilter
		};
	}
};
Jb = /* @__PURE__ */ new WeakMap(), Yb.styles = l`:host { display: none; }`, e([S({
	type: Boolean,
	reflect: !1
})], Yb.prototype, "disabled", void 0), e([S({
	type: Number,
	reflect: !1
})], Yb.prototype, "priority", void 0), e([S({
	converter: Or,
	reflect: !1
})], Yb.prototype, "buttons", void 0), e([S({
	attribute: "pointer-types",
	converter: Or,
	reflect: !1
})], Yb.prototype, "pointerTypes", void 0), e([S({
	attribute: !1,
	reflect: !1
})], Yb.prototype, "inputFilter", void 0);
var Xb, Zb, Qb, $b, ex, tx = class {
	constructor(e, r = 1e6) {
		Xb.add(this), Zb.set(this, void 0), Qb.set(this, []), $b.set(this, void 0), n(this, Zb, Math.max(4, r), "f"), t(this, Qb, "f").push(e), n(this, $b, t(this, Xb, "m", ex).call(this), "f");
	}
	get current() {
		return t(this, Qb, "f")[t(this, Qb, "f").length - 1];
	}
	get detail() {
		return t(this, $b, "f");
	}
	append(e) {
		if (t(this, Zb, "f") && t(this, Qb, "f").length >= t(this, Zb, "f")) {
			let e = t(this, Qb, "f"), n, r;
			for (let t = e.length - 1; t >= 0; t--) if (e[t].kind === "pointermove") {
				if (!n) n = e[t];
				else if (!r) {
					r = e[t];
					break;
				}
			}
			let i = /* @__PURE__ */ new Set([
				e[0],
				n ?? e[0],
				r ?? e[0]
			]);
			for (; e.length >= t(this, Zb, "f");) {
				let t = e[0];
				i.has(t) ? e.push(e.shift()) : e.shift();
			}
		}
		t(this, Qb, "f").push(e), n(this, $b, t(this, Xb, "m", ex).call(this), "f");
	}
	static centroid(...e) {
		let n = (e) => e.reduce((e, t) => e + t, 0) / e.length, r = e.map((e) => t(e, Qb, "f")), i = r.map((e) => e[0]), a = r.map((e) => e[e.length - 1]), o = r.map((e) => {
			let t, n;
			for (let r = e.length - 1; r >= 0; r--) {
				let i = e[r];
				if (i.kind === "pointermove") {
					if (!t) t = i;
					else {
						n = i;
						break;
					}
				}
			}
			let r = e[0];
			return t ||= r, n ||= r, {
				lastMove: t,
				prevMove: n
			};
		}), s = o.map((e) => e.lastMove), c = o.map((e) => e.prevMove), l = n(i.map((e) => e.clientX)), u = n(i.map((e) => e.clientY)), d = n(i.map((e) => e.localX)), f = n(i.map((e) => e.localY)), p = n(a.map((e) => e.clientX)), m = n(a.map((e) => e.clientY)), h = n(a.map((e) => e.localX)), ee = n(a.map((e) => e.localY)), te = n(s.map((e) => e.clientX)), ne = n(s.map((e) => e.clientY)), g = n(c.map((e) => e.clientX)), re = n(c.map((e) => e.clientY)), _ = te - g, ie = ne - re, v = te - l, ae = ne - u, oe = Math.max(...a.map((e) => e.timestamp)), se = Math.max(...s.map((e) => e.timestamp)), ce = Math.max(...c.map((e) => e.timestamp)), le = Math.min(...i.map((e) => e.timestamp)), ue = se - ce, de = oe - le, fe = ue > 0 ? _ / ue : 0, pe = ue > 0 ? ie / ue : 0;
		return {
			startClientX: l,
			startClientY: u,
			startLocalX: d,
			startLocalY: f,
			clientX: p,
			clientY: m,
			localX: h,
			localY: ee,
			deltaX: _,
			deltaY: ie,
			displacement: Math.hypot(_, ie),
			totalDeltaX: v,
			totalDeltaY: ae,
			totalDisplacement: Math.hypot(v, ae),
			axis: Math.abs(v) >= Math.abs(ae) ? "x" : "y",
			velocityX: fe,
			velocityY: pe,
			directionX: Math.sign(v),
			directionY: Math.sign(ae),
			speed: Math.hypot(fe, pe),
			angle: v === 0 && ae === 0 ? 0 : Math.atan2(ae, v),
			duration: de,
			deltaTime: ue
		};
	}
};
Zb = /* @__PURE__ */ new WeakMap(), Qb = /* @__PURE__ */ new WeakMap(), $b = /* @__PURE__ */ new WeakMap(), Xb = /* @__PURE__ */ new WeakSet(), ex = function() {
	let e = t(this, Qb, "f")[0], n = t(this, Qb, "f")[t(this, Qb, "f").length - 1], r, i;
	for (let e = t(this, Qb, "f").length - 1; e >= 0; e--) {
		let n = t(this, Qb, "f")[e];
		if (n.kind === "pointermove") {
			if (!r) r = n;
			else {
				i = n;
				break;
			}
		}
	}
	r ||= e, i ||= e;
	let a = r.clientX - i.clientX, o = r.clientY - i.clientY, s = r.clientX - e.clientX, c = r.clientY - e.clientY, l = r.timestamp - i.timestamp, u = n.timestamp - e.timestamp, d = l > 0 ? a / l : 0, f = l > 0 ? o / l : 0;
	return {
		startClientX: e.clientX,
		startClientY: e.clientY,
		startLocalX: e.localX,
		startLocalY: e.localY,
		clientX: n.clientX,
		clientY: n.clientY,
		localX: n.localX,
		localY: n.localY,
		deltaX: a,
		deltaY: o,
		displacement: Math.hypot(a, o),
		totalDeltaX: s,
		totalDeltaY: c,
		totalDisplacement: Math.hypot(s, c),
		axis: Math.abs(s) >= Math.abs(c) ? "x" : "y",
		velocityX: d,
		velocityY: f,
		directionX: Math.sign(s),
		directionY: Math.sign(c),
		speed: Math.hypot(d, f),
		angle: s === 0 && c === 0 ? 0 : Math.atan2(c, s),
		duration: u,
		deltaTime: l
	};
};
//#endregion
//#region node_modules/@m3e/web/dist/gestures-pan.js
var nx = {
	...sb,
	activationMode: "press",
	minDisplacement: 4,
	lockAxis: "none",
	axisThreshold: 8,
	deltaThreshold: 0,
	pointers: 1,
	maxPressInterval: 120
}, rx, ix, ax, ox, sx, cx, lx, ux, dx, fx, px, mx, hx = class extends mb {
	constructor() {
		super(...arguments), rx.add(this), ix.set(this, /* @__PURE__ */ new Map()), ax.set(this, !1), ox.set(this, null), sx.set(this, void 0), cx.set(this, !1), lx.set(this, !1), ux.set(this, !1);
	}
	get trackers() {
		return [...t(this, ix, "f").values()].map((e) => e.tracker);
	}
	get defaultOptions() {
		return { ...nx };
	}
	shouldCapturePointer(e) {
		return t(this, ix, "f").has(e.inputId);
	}
	_onPointerDown(e) {
		t(this, ix, "f").has(e.inputId) || t(this, ix, "f").size >= this.options.pointers ? t(this, rx, "m", px).call(this) : (t(this, ix, "f").set(e.inputId, {
			active: !0,
			tracker: new tx(e),
			pressTimestamp: e.timestamp
		}), n(this, ax, this.options.activationMode === "move", "f"), t(this, ax, "f") || (this._defer(e.inputId), !t(this, cx, "f") && this.options.minDisplacement <= 0 && t(this, rx, "m", dx).call(this) && (n(this, cx, !0, "f"), this._emit(t(this, rx, "m", mx).call(this, "start")))));
	}
	_onPointerMove(e) {
		if (t(this, ax, "f")) return;
		if (!t(this, ix, "f").has(e.inputId) && this.options.activationMode === "move" && (t(this, ix, "f").set(e.inputId, {
			active: !0,
			tracker: new tx(e),
			pressTimestamp: e.timestamp
		}), this._defer(e.inputId), !t(this, cx, "f") && this.options.minDisplacement <= 0 && t(this, rx, "m", dx).call(this))) {
			n(this, cx, !0, "f"), this._emit(t(this, rx, "m", mx).call(this, "start"));
			return;
		}
		let r = t(this, ix, "f").get(e.inputId);
		if (!r || !t(this, rx, "m", dx).call(this)) return;
		if (r.tracker.append(e), t(this, cx, "f")) {
			t(this, rx, "m", fx).call(this);
			let e = [...t(this, ix, "f").values()].map((e) => e.tracker), n = tx.centroid(...e);
			switch (t(this, ox, "f")) {
				case "x":
					if (Math.abs(n.deltaY) < this.options.deltaThreshold) return;
					break;
				case "y": if (Math.abs(n.deltaX) < this.options.deltaThreshold) return;
			}
			this._emit(t(this, rx, "m", mx).call(this, "update"));
			return;
		}
		let i = [...t(this, ix, "f").values()].map((e) => e.tracker);
		tx.centroid(...i).totalDisplacement >= this.options.minDisplacement && (t(this, rx, "m", fx).call(this), n(this, cx, !0, "f"), this._emit(t(this, rx, "m", mx).call(this, "start")));
	}
	_onPointerUp(e) {
		let n = t(this, ix, "f").get(e.inputId);
		n && (n.active = !1, t(this, cx, "f") ? [...t(this, ix, "f").values()].every((e) => e.active) || (n.tracker.append(e), this._accept([...t(this, ix, "f").keys()])) : t(this, rx, "m", px).call(this));
	}
	_onPointerCancel(e) {
		t(this, ix, "f").get(e.inputId) && t(this, rx, "m", px).call(this);
	}
	_onAccept(e) {
		let n = t(this, ix, "f").get(e);
		n && (n.accepted = !0, t(this, cx, "f") && !t(this, lx, "f") && [...t(this, ix, "f").values()].every((e) => e.accepted) && (this._emit(t(this, rx, "m", mx).call(this, "end")), this.reset()));
	}
	_onReject(e) {
		t(this, ix, "f").has(e) && t(this, rx, "m", px).call(this, e);
	}
	reset() {
		clearTimeout(t(this, sx, "f")), n(this, sx, void 0, "f"), t(this, ix, "f").clear(), n(this, ax, !1, "f"), n(this, cx, !1, "f"), n(this, lx, !1, "f"), n(this, ox, null, "f");
	}
};
ix = /* @__PURE__ */ new WeakMap(), ax = /* @__PURE__ */ new WeakMap(), ox = /* @__PURE__ */ new WeakMap(), sx = /* @__PURE__ */ new WeakMap(), cx = /* @__PURE__ */ new WeakMap(), lx = /* @__PURE__ */ new WeakMap(), ux = /* @__PURE__ */ new WeakMap(), rx = /* @__PURE__ */ new WeakSet(), dx = function() {
	return t(this, ix, "f").size === this.options.pointers && [...t(this, ix, "f").values()].every((e) => e.active);
}, fx = function() {
	if (this.options.lockAxis === "x" || this.options.lockAxis === "y") {
		n(this, ox, this.options.lockAxis, "f");
		return;
	}
	if (this.options.lockAxis !== "auto" || t(this, ox, "f")) return;
	let e = [...t(this, ix, "f").values()].map((e) => e.tracker), r = tx.centroid(...e);
	r.totalDisplacement >= this.options.axisThreshold && n(this, ox, r.axis, "f");
}, px = function(e) {
	if (!(t(this, ix, "f").size === 0 || t(this, ux, "f"))) try {
		n(this, ux, !0, "f"), t(this, cx, "f") && !t(this, lx, "f") && this._emit(t(this, rx, "m", mx).call(this, "cancel")), n(this, lx, !0, "f"), [...t(this, ix, "f").keys()].filter((t) => t !== e).forEach((e) => this._reject(e)), this.reset();
	} finally {
		n(this, ux, !1, "f");
	}
}, mx = function(e) {
	let n = [...t(this, ix, "f").values()].map((e) => e.tracker), r = tx.centroid(...n);
	return {
		gestureName: "pan",
		phase: e,
		inputId: [...t(this, ix, "f").keys()],
		...r,
		timestamp: Math.max(...n.map((e) => e.current.timestamp))
	};
};
var gx = class extends Yb {
	constructor() {
		super(...arguments), this.recognizer = new hx(), this.pointers = this.recognizer.defaultOptions.pointers, this.activationMode = this.recognizer.defaultOptions.activationMode, this.minDisplacement = this.recognizer.defaultOptions.minDisplacement, this.lockAxis = this.recognizer.defaultOptions.lockAxis, this.axisThreshold = this.recognizer.defaultOptions.axisThreshold, this.deltaThreshold = this.recognizer.defaultOptions.deltaThreshold, this.maxPressInterval = this.recognizer.defaultOptions.maxPressInterval;
	}
	willUpdate(e) {
		super.willUpdate(e), this.recognizer.options = {
			pointers: this.pointers,
			activationMode: this.activationMode,
			minDisplacement: this.minDisplacement,
			lockAxis: this.lockAxis,
			axisThreshold: this.axisThreshold,
			deltaThreshold: this.deltaThreshold,
			maxPressInterval: this.maxPressInterval
		};
	}
};
e([S({
	type: Number,
	reflect: !1
})], gx.prototype, "pointers", void 0), e([S({
	attribute: "activation-mode",
	useDefault: !0,
	reflect: !1
})], gx.prototype, "activationMode", void 0), e([S({
	attribute: "min-displacement",
	type: Number,
	reflect: !1
})], gx.prototype, "minDisplacement", void 0), e([S({
	attribute: "lock-axis",
	reflect: !1
})], gx.prototype, "lockAxis", void 0), e([S({
	attribute: "axis-threshold",
	type: Number,
	reflect: !1
})], gx.prototype, "axisThreshold", void 0), e([S({
	attribute: "delta-threshold",
	type: Number,
	reflect: !1
})], gx.prototype, "deltaThreshold", void 0), e([S({
	attribute: "max-press-interval",
	type: Number,
	reflect: !1
})], gx.prototype, "maxPressInterval", void 0), gx = e([w("m3e-pan-gesture")], gx);
//#endregion
//#region node_modules/@m3e/web/dist/gestures-swipe.js
var _x = {
	...sb,
	pointers: 1,
	startThreshold: 4,
	minVelocity: .3,
	directions: [
		"left",
		"right",
		"up",
		"down"
	],
	directionThreshold: 12,
	directionGracePeriod: 0,
	minDisplacement: 24,
	maxPressInterval: 120
}, vx, yx, bx, xx, Sx, Cx, wx = class extends _b {
	constructor(e, t) {
		super(e, t, new hx()), vx.add(this), yx.set(this, void 0);
	}
	get defaultOptions() {
		return { ..._x };
	}
	get eager() {
		return !0;
	}
	_applyOptions(e, t) {
		super._applyOptions(e, t), t.options = {
			minDisplacement: e.startThreshold,
			pointers: e.pointers,
			maxPressInterval: e.maxPressInterval
		};
	}
	_onAccept(e) {
		t(this, yx, "f") && !t(this, vx, "m", bx).call(this, e, t(this, yx, "f")) && (this._emit(t(this, vx, "m", Sx).call(this, "end", t(this, yx, "f"))), this.reset());
	}
	_onReject(e) {
		t(this, yx, "f") && !t(this, vx, "m", bx).call(this, e, t(this, yx, "f")) && (this._emit(t(this, vx, "m", Sx).call(this, "cancel", t(this, yx, "f"))), this.reset());
	}
	_handleDisposition(e, t) {
		t === "accept" && this._inner?.onResolution(e, "accept");
	}
	_handleGesture(e) {
		switch (e.phase) {
			case "cancel":
				t(this, yx, "f") ? (this._emit(t(this, vx, "m", Sx).call(this, "cancel", t(this, yx, "f"))), t(this, vx, "m", Cx).call(this, e.inputId)) : this.reset();
				break;
			case "start":
				n(this, yx, {
					detail: e,
					direction: t(this, vx, "m", xx).call(this, e),
					axis: e.axis
				}, "f"), this._emit(t(this, vx, "m", Sx).call(this, "start", t(this, yx, "f"))), this._defer(e.inputId);
				break;
			case "update": {
				let r = t(this, yx, "f")?.axis ?? e.axis, i = e.axis;
				if (n(this, yx, {
					detail: e,
					direction: t(this, vx, "m", xx).call(this, e),
					axis: i
				}, "f"), this.options.directionGracePeriod > 0 && e.duration > this.options.directionGracePeriod && (r !== i || !this.options.directions.includes(t(this, yx, "f").direction))) {
					this._emit(t(this, vx, "m", Sx).call(this, "cancel", t(this, yx, "f"))), t(this, vx, "m", Cx).call(this, e.inputId);
					break;
				}
				this._emit(t(this, vx, "m", Sx).call(this, "update", t(this, yx, "f")));
				break;
			}
			case "end":
				if (n(this, yx, {
					detail: e,
					direction: t(this, vx, "m", xx).call(this, e),
					axis: e.axis
				}, "f"), e.totalDisplacement < this.options.minDisplacement || e.speed < this.options.minVelocity || !(t(this, yx, "f").axis === "x" ? Math.abs(e.totalDeltaX) >= this.options.directionThreshold : Math.abs(e.totalDeltaY) >= this.options.directionThreshold) || !this.options.directions.includes(t(this, yx, "f").direction)) {
					this._emit(t(this, vx, "m", Sx).call(this, "cancel", t(this, yx, "f"))), t(this, vx, "m", Cx).call(this, e.inputId);
					break;
				}
				this._accept(e.inputId);
		}
	}
	reset() {
		super.reset(), n(this, yx, void 0, "f");
	}
};
yx = /* @__PURE__ */ new WeakMap(), vx = /* @__PURE__ */ new WeakSet(), bx = function(e, t) {
	return !(Array.isArray(t.detail.inputId) ? t.detail.inputId : [t.detail.inputId]).includes(e);
}, xx = function(e) {
	return e.axis === "x" ? e.totalDeltaX > 0 ? "right" : "left" : e.totalDeltaY > 0 ? "down" : "up";
}, Sx = function(e, t) {
	return {
		inputId: t.detail.inputId,
		gestureName: "swipe",
		phase: e,
		timestamp: t.detail.timestamp,
		direction: t.direction,
		axis: t.detail.axis,
		startClientX: t.detail.startClientX,
		startClientY: t.detail.startClientY,
		startLocalX: t.detail.startLocalX,
		startLocalY: t.detail.startLocalY,
		translationX: t.detail.totalDeltaX,
		translationY: t.detail.totalDeltaY,
		displacement: t.detail.totalDisplacement,
		velocityX: t.detail.velocityX,
		velocityY: t.detail.velocityY,
		speed: t.detail.speed,
		duration: t.detail.duration
	};
}, Cx = function(e) {
	this._release(e), this.reset();
};
var Tx = class extends Yb {
	constructor() {
		super(...arguments), this.recognizer = new wx(), this.pointers = this.recognizer.defaultOptions.pointers, this.startThreshold = this.recognizer.defaultOptions.startThreshold, this.minDisplacement = this.recognizer.defaultOptions.minDisplacement, this.minVelocity = this.recognizer.defaultOptions.minVelocity, this.directionThreshold = this.recognizer.defaultOptions.directionThreshold, this.directionGracePeriod = this.recognizer.defaultOptions.directionGracePeriod, this.directions = this.recognizer.defaultOptions.directions, this.maxPressInterval = this.recognizer.defaultOptions.maxPressInterval;
	}
	willUpdate(e) {
		super.willUpdate(e), this.recognizer.options = {
			pointers: this.pointers,
			startThreshold: this.startThreshold,
			minDisplacement: this.minDisplacement,
			minVelocity: this.minVelocity,
			directionThreshold: this.directionThreshold,
			directionGracePeriod: this.directionGracePeriod,
			directions: this.directions,
			maxPressInterval: this.maxPressInterval
		};
	}
};
e([S({
	type: Number,
	reflect: !1
})], Tx.prototype, "pointers", void 0), e([S({
	attribute: "start-threshold",
	type: Number,
	reflect: !1
})], Tx.prototype, "startThreshold", void 0), e([S({
	attribute: "min-displacement",
	type: Number,
	reflect: !1
})], Tx.prototype, "minDisplacement", void 0), e([S({
	attribute: "min-velocity",
	type: Number,
	reflect: !1
})], Tx.prototype, "minVelocity", void 0), e([S({
	attribute: "direction-threshold",
	type: Number,
	reflect: !1
})], Tx.prototype, "directionThreshold", void 0), e([S({
	attribute: "direction-grace-period",
	type: Number,
	reflect: !1
})], Tx.prototype, "directionGracePeriod", void 0), e([S({
	converter: Or,
	reflect: !1
})], Tx.prototype, "directions", void 0), e([S({
	attribute: "max-press-interval",
	type: Number,
	reflect: !1
})], Tx.prototype, "maxPressInterval", void 0), Tx = e([w("m3e-swipe-gesture")], Tx);
//#endregion
//#region node_modules/@m3e/web/dist/gestures-tap.js
var Ex = {
	...sb,
	pointers: 1,
	maxDuration: 180,
	maxDisplacement: 12,
	maxPressInterval: 120,
	maxReleaseInterval: 120
}, Dx, Ox, kx, Ax, jx, Mx, Nx, Px, Fx = class extends mb {
	constructor() {
		super(...arguments), Dx.add(this), Ox.set(this, /* @__PURE__ */ new Map()), kx.set(this, void 0), Ax.set(this, !1), jx.set(this, !1), Mx.set(this, !1);
	}
	get defaultOptions() {
		return { ...Ex };
	}
	shouldCapturePointer(e) {
		return t(this, Ox, "f").has(e.inputId);
	}
	_onPointerDown(e) {
		if (t(this, Ox, "f").has(e.inputId) || t(this, Ox, "f").size >= this.options.pointers) t(this, Dx, "m", Nx).call(this);
		else {
			if (t(this, Ox, "f").size > 0 && this.options.maxPressInterval > 0) {
				let n = Math.min(...[...t(this, Ox, "f").values()].map((e) => e.pressTimestamp));
				if (e.timestamp - n > this.options.maxPressInterval) {
					t(this, Dx, "m", Nx).call(this);
					return;
				}
			}
			t(this, Ox, "f").set(e.inputId, {
				tracker: new tx(e),
				pressTimestamp: e.timestamp
			}), this.options.maxDuration > 0 && t(this, Ox, "f").size === 1 && n(this, kx, setTimeout(() => t(this, Dx, "m", Nx).call(this), this.options.maxDuration), "f"), this._defer(e.inputId), t(this, Ox, "f").size === this.options.pointers && (n(this, Ax, !0, "f"), this._emit(t(this, Dx, "m", Px).call(this, "start")));
		}
	}
	_onPointerMove(e) {
		let n = t(this, Ox, "f").get(e.inputId);
		if (!n || (n.tracker.append(e), !t(this, Ax, "f") || t(this, jx, "f"))) return;
		let r = [...t(this, Ox, "f").values()].map((e) => e.tracker);
		tx.centroid(...r).totalDisplacement > this.options.maxDisplacement && t(this, Dx, "m", Nx).call(this);
	}
	_onPointerUp(e) {
		let r = t(this, Ox, "f").get(e.inputId);
		if (r) {
			if (!t(this, Ax, "f")) t(this, Dx, "m", Nx).call(this);
			else {
				if (this.options.maxReleaseInterval > 0) {
					let n = [...t(this, Ox, "f").values()];
					if (n.some((e) => e.releaseTimestamp !== void 0)) {
						let r = Math.min(...n.filter((e) => e.releaseTimestamp !== void 0).map((e) => e.releaseTimestamp));
						if (e.timestamp - r > this.options.maxReleaseInterval) {
							t(this, Dx, "m", Nx).call(this);
							return;
						}
					}
				}
				r.tracker.append(e), r.releaseTimestamp = e.timestamp, t(this, Ax, "f") && [...t(this, Ox, "f").values()].every((e) => e.releaseTimestamp !== void 0) && (clearTimeout(t(this, kx, "f")), n(this, kx, void 0, "f")), this._accept(e.inputId);
			}
		}
	}
	_onPointerCancel(e) {
		t(this, Ox, "f").get(e.inputId) && t(this, Dx, "m", Nx).call(this);
	}
	_onAccept(e) {
		let n = t(this, Ox, "f").get(e);
		n && (n.accepted = !0, t(this, Ax, "f") && !t(this, jx, "f") && [...t(this, Ox, "f").values()].every((e) => e.accepted) && (this._emit(t(this, Dx, "m", Px).call(this, "end")), this.reset()));
	}
	_onReject(e) {
		t(this, Ox, "f").has(e) && t(this, Dx, "m", Nx).call(this, e);
	}
	reset() {
		clearTimeout(t(this, kx, "f")), n(this, kx, void 0, "f"), t(this, Ox, "f").clear(), n(this, Ax, !1, "f"), n(this, jx, !1, "f");
	}
};
Ox = /* @__PURE__ */ new WeakMap(), kx = /* @__PURE__ */ new WeakMap(), Ax = /* @__PURE__ */ new WeakMap(), jx = /* @__PURE__ */ new WeakMap(), Mx = /* @__PURE__ */ new WeakMap(), Dx = /* @__PURE__ */ new WeakSet(), Nx = function(e) {
	if (!(t(this, Ox, "f").size === 0 || t(this, Mx, "f"))) try {
		n(this, Mx, !0, "f"), t(this, Ax, "f") && !t(this, jx, "f") && this._emit(t(this, Dx, "m", Px).call(this, "cancel")), n(this, jx, !0, "f"), [...t(this, Ox, "f").keys()].filter((t) => t !== e).forEach((e) => this._reject(e)), this.reset();
	} finally {
		n(this, Mx, !1, "f");
	}
}, Px = function(e) {
	let n = [...t(this, Ox, "f").values()].map((e) => e.tracker), r = tx.centroid(...n);
	return {
		gestureName: "tap",
		phase: e,
		inputId: [...t(this, Ox, "f").keys()],
		timestamp: Math.max(...n.map((e) => e.current.timestamp)),
		clientX: r.startClientX,
		clientY: r.startClientY,
		localX: r.startLocalX,
		localY: r.startLocalY,
		duration: n.reduce((e, t) => e + t.detail.duration, 0) / n.length
	};
};
var Ix = class extends Yb {
	constructor() {
		super(...arguments), this.recognizer = new Fx(), this.pointers = this.recognizer.defaultOptions.pointers, this.maxDuration = this.recognizer.defaultOptions.maxDuration, this.maxDisplacement = this.recognizer.defaultOptions.maxDisplacement, this.maxPressInterval = this.recognizer.defaultOptions.maxPressInterval, this.maxReleaseInterval = this.recognizer.defaultOptions.maxReleaseInterval;
	}
	willUpdate(e) {
		super.willUpdate(e), this.recognizer.options = {
			pointers: this.pointers,
			maxPressInterval: this.maxPressInterval,
			maxReleaseInterval: this.maxReleaseInterval,
			maxDuration: this.maxDuration,
			maxDisplacement: this.maxDisplacement
		};
	}
};
e([S({
	type: Number,
	reflect: !1
})], Ix.prototype, "pointers", void 0), e([S({
	attribute: "max-duration",
	type: Number,
	reflect: !1
})], Ix.prototype, "maxDuration", void 0), e([S({
	attribute: "max-displacement",
	type: Number,
	reflect: !1
})], Ix.prototype, "maxDisplacement", void 0), e([S({
	attribute: "max-press-interval",
	type: Number,
	reflect: !1
})], Ix.prototype, "maxPressInterval", void 0), e([S({
	attribute: "max-release-interval",
	type: Number,
	reflect: !1
})], Ix.prototype, "maxReleaseInterval", void 0), Ix = e([w("m3e-tap-gesture")], Ix);
//#endregion
//#region node_modules/@m3e/web/dist/bottom-sheet.js
var Lx = class extends sa {
	_onClick() {
		this.closest("m3e-bottom-sheet")?.hide();
	}
};
Lx = e([w("m3e-bottom-sheet-action")], Lx);
var $, Rx, zx, Bx, Vx, Hx, Ux, Wx, Gx, Kx, qx, Jx, Yx, Xx, Zx, Qx, $x, eS, tS, nS, rS, iS, aS, oS, sS, cS, lS, uS, dS, fS, pS, mS, hS, gS, _S, vS, yS, bS, xS = bS = class extends ea(ia(T(x))) {
	constructor() {
		super(...arguments), $.add(this), Rx.set(this, (e) => t(this, $, "m", iS).call(this, e)), zx.set(this, (e) => t(this, $, "m", aS).call(this, e)), Bx.set(this, () => t(this, $, "m", oS).call(this)), Vx.set(this, (e) => !(e.target instanceof HTMLElement && (e.target.classList.contains("handle-touch") || ed.isFocusable(e.target)))), Hx.set(this, new Dr(this)), Ux.set(this, new Sn(this)), Wx.set(this, new cr(this, {
			target: null,
			skipInitial: !0,
			callback: (e) => t(this, $, "m", gS).call(this, e)
		})), Gx.set(this, null), Kx.set(this, void 0), qx.set(this, 0), Jx.set(this, void 0), Yx.set(this, 0), Xx.set(this, 0), Zx.set(this, void 0), this.modal = !1, this.open = !1, this.handle = !1, this.handleLabel = "Drag handle", this.detents = [], this.detent = 0, this.hideable = !1, this.hideFriction = .5, this.overshootLimit = 4;
	}
	show(e = this.detent) {
		this.open ? e !== void 0 && t(this, qx, "f") !== e && t(this, $, "m", _S).call(this, e) : (n(this, Jx, e, "f"), this.open = !0);
	}
	hide() {
		this.open = !1;
	}
	toggle(e) {
		this.open ? this.hide() : this.show(e);
	}
	cycle() {
		var e;
		this.open ? this.detents.length > 0 ? t(this, qx, "f") < this.detents.length - 1 ? (n(this, qx, (e = t(this, qx, "f"), e++, e), "f"), t(this, $, "m", _S).call(this, t(this, qx, "f"))) : this.hideable && this.hide() : this.hide() : this.show();
	}
	update(e) {
		super.update(e), e.has("modal") && (this.role = this.modal ? "dialog" : "region", this.ariaModal = this.modal ? "true" : null, this.popover = this.modal ? "manual" : null);
	}
	reconnectedCallback() {
		super.reconnectedCallback(), t(this, $, "m", Qx).call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), t(this, $, "m", Qx).call(this);
	}
	updated(e) {
		if (super.updated(e), e.has("handle") && this.handle && t(this, $, "m", $x).call(this, "handle"), e.has("open")) {
			if (this.open) {
				if (!this.dispatchEvent(new Event("opening", { cancelable: !0 }))) {
					this.open = !1;
					return;
				}
				bS.__openSheet !== this && bS.__openSheet?.hide(), bS.__openSheet = this, this.inert = !1, window.addEventListener("resize", t(this, Bx, "f")), this.detents.length > 0 ? (n(this, qx, Math.min(Math.max(0, t(this, Jx, "f") ?? this.detent), this.detents.length - 1), "f"), t(this, $, "m", yS).call(this, t(this, $, "m", dS).call(this, this.detents[t(this, qx, "f")]))) : t(this, $, "m", yS).call(this, Math.min(t(this, $, "m", dS).call(this, "fit"), t(this, $, "m", dS).call(this, "half"))), n(this, Jx, void 0, "f");
			} else {
				if (!this.dispatchEvent(new Event("closing", { cancelable: !0 }))) {
					this.open = !0;
					return;
				}
				requestAnimationFrame(() => this.inert = !0), window.removeEventListener("resize", t(this, Bx, "f")), bS.__openSheet === this && (bS.__openSheet = void 0);
			}
			if (this.modal) {
				if (this.open) {
					n(this, Gx, document.activeElement, "f"), t(this, Ux, "f").lock(), t(this, Hx, "f").lock(), this.showPopover(), requestAnimationFrame(() => {
						document.addEventListener("click", t(this, Rx, "f")), document.addEventListener("keydown", t(this, zx, "f"));
					});
					let e = this.querySelector("[autofocus]");
					(!e || !ed.isFocusable(e)) && (e = this.shadowRoot?.querySelector(".handle")), e && St(e);
				} else t(this, $, "m", vS).call(this, 0).then(() => {
					t(this, Ux, "f").unlock(), t(this, Hx, "f").unlock(), document.removeEventListener("click", t(this, Rx, "f")), document.removeEventListener("keydown", t(this, zx, "f")), this.hidePopover(), t(this, Gx, "f") instanceof HTMLElement && t(this, Gx, "f").focus(), n(this, Gx, null, "f");
				});
			}
			this.dispatchEvent(new Event(this.open ? "opened" : "closed"));
		}
	}
	render() {
		return y`<m3e-focus-trap ?disabled="${!this.modal}"><div class="base"><m3e-elevation class="elevation"></m3e-elevation><div id="header" class="header">${this.handle ? y`<div class="handle-row"><div id="handle" class="handle" role="button" aria-label="${this.handleLabel}" tabindex="0" @keydown="${t(this, $, "m", sS)}"><m3e-focus-ring class="focus-ring" for="handle"></m3e-focus-ring><div class="handle-touch" aria-hidden="true"></div></div></div><m3e-tap-gesture for="handle" @gesture="${t(this, $, "m", tS)}"></m3e-tap-gesture><m3e-swipe-gesture for="handle" @gesture="${t(this, $, "m", nS)}"></m3e-swipe-gesture>` : b}<slot name="header" @slotchange="${t(this, $, "m", eS)}"></slot></div><m3e-swipe-gesture for="header" @gesture="${t(this, $, "m", nS)}"></m3e-swipe-gesture><div class="body"><div class="content"><slot></slot></div></div></div></m3e-focus-trap>`;
	}
};
Rx = /* @__PURE__ */ new WeakMap(), zx = /* @__PURE__ */ new WeakMap(), Bx = /* @__PURE__ */ new WeakMap(), Vx = /* @__PURE__ */ new WeakMap(), Hx = /* @__PURE__ */ new WeakMap(), Ux = /* @__PURE__ */ new WeakMap(), Wx = /* @__PURE__ */ new WeakMap(), Gx = /* @__PURE__ */ new WeakMap(), Kx = /* @__PURE__ */ new WeakMap(), qx = /* @__PURE__ */ new WeakMap(), Jx = /* @__PURE__ */ new WeakMap(), Yx = /* @__PURE__ */ new WeakMap(), Xx = /* @__PURE__ */ new WeakMap(), Zx = /* @__PURE__ */ new WeakMap(), $ = /* @__PURE__ */ new WeakSet(), Qx = function() {
	let e = this.shadowRoot?.querySelector(".content");
	e && (n(this, Yx, e.clientHeight, "f"), t(this, Wx, "f").observe(e));
	let r = this.shadowRoot?.querySelector(".header");
	r && (n(this, Xx, r.clientHeight, "f"), t(this, Wx, "f").observe(r)), t(this, $, "m", $x).call(this, "header"), t(this, $, "m", $x).call(this, "handle");
}, $x = function(e) {
	let n = this.shadowRoot?.querySelector(`m3e-swipe-gesture[for='${e}']`);
	n && (e === "header" && (n.inputFilter = t(this, Vx, "f")), n.startThreshold = 0, n.directions = ["up", "down"]);
}, eS = function(e) {
	D(this, "--with-header", Rt(e.target));
}, tS = function(e) {
	e.detail.phase === "end" && this.cycle();
}, nS = function(e) {
	switch (e.detail.phase) {
		case "start":
			{
				t(this, $, "m", rS).call(this, "grabbing");
				let e = t(this, $, "m", fS).call(this), r = this.detents.length > 0 ? Math.max(...this.detents.map((e) => t(this, $, "m", dS).call(this, e))) : e;
				n(this, Kx, {
					startHeight: this.clientHeight,
					effectiveMaxHeight: r,
					maxHeight: e,
					minHeight: t(this, $, "m", pS).call(this)
				}, "f");
			}
			break;
		case "update":
			{
				if (!t(this, Kx, "f")) break;
				let n = t(this, Kx, "f").startHeight - e.detail.translationY;
				if (n < t(this, Kx, "f").minHeight) {
					if (this.hideable) {
						let e = (t(this, Kx, "f").minHeight - n) * this.hideFriction;
						n = t(this, Kx, "f").minHeight - e;
					} else {
						let e = t(this, Kx, "f").minHeight - n, r = t(this, Kx, "f").maxHeight * (this.overshootLimit / 100), i = r * e / (e + r);
						n = t(this, Kx, "f").minHeight - i;
					}
				} else if (n > t(this, Kx, "f").effectiveMaxHeight) {
					let e = n - t(this, Kx, "f").effectiveMaxHeight, r = t(this, Kx, "f").maxHeight * (this.overshootLimit / 100), i = r * e / (e + r);
					n = t(this, Kx, "f").effectiveMaxHeight + i;
				}
				t(this, $, "m", yS).call(this, n);
			}
			break;
		case "cancel":
			if (!t(this, Kx, "f")) return;
			if (t(this, $, "m", rS).call(this, ""), this.hideable) {
				let e = t(this, Kx, "f").minHeight;
				if (this.clientHeight < e - 20) {
					this.hide();
					return;
				}
			}
			this.detents.length > 0 ? t(this, $, "m", _S).call(this, t(this, $, "m", uS).call(this)) : this.clientHeight < t(this, Kx, "f").minHeight ? t(this, $, "m", vS).call(this, t(this, Kx, "f").minHeight) : this.clientHeight > t(this, Kx, "f").effectiveMaxHeight && t(this, $, "m", vS).call(this, t(this, Kx, "f").effectiveMaxHeight), n(this, Kx, void 0, "f");
			break;
		case "end":
			if (!t(this, Kx, "f")) return;
			switch (t(this, $, "m", rS).call(this, ""), e.detail.direction) {
				case "up":
					if (this.detents.length > 0) {
						let e = t(this, $, "m", cS).call(this);
						e !== t(this, qx, "f") && t(this, $, "m", _S).call(this, e);
					} else t(this, $, "m", vS).call(this, t(this, $, "m", dS).call(this, "full"));
					break;
				case "down": this.hideable && this.dispatchEvent(new Event("cancel", { cancelable: !0 })) && this.hide();
			}
			n(this, Kx, void 0, "f");
	}
}, rS = function(e) {
	let t = this.shadowRoot?.querySelector(".header");
	t && (t.style.cursor = e);
}, iS = function(e) {
	this.open && this.modal && !e.composedPath().includes(this) && this.dispatchEvent(new Event("cancel", { cancelable: !0 })) && this.hide();
}, aS = function(e) {
	this.open && this.modal && e.key === "Escape" && !e.shiftKey && !e.ctrlKey && (e.preventDefault(), this.dispatchEvent(new Event("cancel", { cancelable: !0 })) && this.hide());
}, oS = function() {
	if (this.detents.length > 0 && this.detents[t(this, qx, "f")] === "half") {
		t(this, $, "m", yS).call(this, t(this, $, "m", dS).call(this, "half"));
		return;
	}
	let e = t(this, $, "m", fS).call(this);
	(Rr(this, "--full") || this.clientHeight > e) && t(this, $, "m", yS).call(this, e);
}, sS = function(e) {
	if (!e.defaultPrevented && ru(e)) switch (e.key) {
		case "Up":
		case "ArrowUp":
			if (e.preventDefault(), this.detents.length > 0) {
				let e = t(this, $, "m", cS).call(this);
				e !== t(this, qx, "f") && t(this, $, "m", _S).call(this, e);
			} else t(this, $, "m", vS).call(this, t(this, $, "m", dS).call(this, "full"));
			break;
		case "Down":
		case "ArrowDown": if (e.preventDefault(), this.detents.length > 0) {
			let e = t(this, $, "m", lS).call(this);
			e === t(this, qx, "f") ? this.hideable && this.hide() : t(this, $, "m", _S).call(this, e);
		} else this.hideable && this.hide();
	}
}, cS = function() {
	let e = this.clientHeight, n = Infinity, r = t(this, qx, "f");
	for (let i = 0; i < this.detents.length; i++) {
		if (i === t(this, qx, "f")) continue;
		let a = t(this, $, "m", dS).call(this, this.detents[i]);
		a > e && a < n && (n = a, r = i);
	}
	return r;
}, lS = function() {
	let e = this.clientHeight, n = -Infinity, r = t(this, qx, "f");
	for (let i = 0; i < this.detents.length; i++) {
		if (i === t(this, qx, "f")) continue;
		let a = t(this, $, "m", dS).call(this, this.detents[i]);
		a < e && a > n && (n = a, r = i);
	}
	return r;
}, uS = function() {
	let e = this.clientHeight, n = t(this, qx, "f"), r = Infinity;
	for (let i = 0; i < this.detents.length; i++) {
		let a = t(this, $, "m", dS).call(this, this.detents[i]), o = Math.abs(e - a);
		o < r && (r = o, n = i);
	}
	return n;
}, dS = function(e) {
	switch (e) {
		case "collapsed": return t(this, $, "m", pS).call(this);
		case "half": return t(this, $, "m", fS).call(this) * .5;
		case "full": return t(this, $, "m", fS).call(this);
		case "fit": return t(this, $, "m", hS).call(this);
	}
	return e.endsWith("%") ? t(this, $, "m", fS).call(this) * (parseFloat(e) / 100) : e.endsWith("px") ? parseFloat(e) : t(this, $, "m", pS).call(this);
}, fS = function() {
	let e = this.shadowRoot?.querySelector(".base");
	return window.innerHeight - (e ? bt(e, "var(--_bottom-sheet-top-space)") : 0);
}, pS = function() {
	return this.detents.includes("fit") && !this.detents.includes("collapsed") ? t(this, $, "m", hS).call(this) : t(this, $, "m", mS).call(this);
}, mS = function() {
	let e = this.shadowRoot?.querySelector(".base");
	return t(this, Xx, "f") + (e ? bt(e, "var(--_bottom-sheet-peek-height)") : 0);
}, hS = function() {
	let e = this.shadowRoot?.querySelector(".body");
	if (!e) return 0;
	let n = getComputedStyle(e);
	return Math.min(t(this, Xx, "f") + t(this, Yx, "f") + parseFloat(n.paddingBlockStart) + parseFloat(n.paddingBlockEnd), t(this, $, "m", fS).call(this));
}, gS = function(e) {
	let r = t(this, Yx, "f"), i = t(this, Xx, "f");
	for (let t of e) t.target.classList.contains("content") ? n(this, Yx, (Array.isArray(t.borderBoxSize) ? t.borderBoxSize[0] : t.borderBoxSize).blockSize, "f") : t.target.classList.contains("header") && n(this, Xx, (Array.isArray(t.borderBoxSize) ? t.borderBoxSize[0] : t.borderBoxSize).blockSize, "f");
	if (this.open && this.detents.length > 0 && (r !== t(this, Yx, "f") || i !== t(this, Xx, "f"))) switch (this.detents[t(this, qx, "f")]) {
		case "fit":
			t(this, $, "m", yS).call(this, t(this, $, "m", hS).call(this));
			break;
		case "collapsed": t(this, $, "m", yS).call(this, t(this, $, "m", pS).call(this));
	}
}, _S = function(e) {
	e >= 0 && e < this.detents.length && (n(this, qx, e, "f"), t(this, $, "m", vS).call(this, t(this, $, "m", dS).call(this, this.detents[e])));
}, vS = async function(e) {
	t(this, Zx, "f") && (t(this, $, "m", yS).call(this, this.clientHeight), t(this, Zx, "f")?.cancel(), n(this, Zx, void 0, "f")), Bt() ? t(this, $, "m", yS).call(this, e) : (n(this, Zx, this.animate([{ "--_bottom-sheet-height": `${this.clientHeight}px` }, { "--_bottom-sheet-height": `${e}px` }], {
		duration: 250,
		easing: "cubic-bezier(0.2, 0.0, 0, 1.0)"
	}), "f"), t(this, Zx, "f").onfinish = () => {
		t(this, $, "m", yS).call(this, e), n(this, Zx, void 0, "f");
	}, await t(this, Zx, "f").finished);
}, yS = function(e) {
	this.style.setProperty("--_bottom-sheet-height", `${e}px`), D(this, "--full", e === t(this, $, "m", fS).call(this));
	let n = this.shadowRoot?.querySelector(".content");
	n && (n.inert = e <= t(this, $, "m", mS).call(this));
}, Vt(l`@property --_bottom-sheet-height { syntax: "<length>"; inherits: false; initial-value: 50vh; } m3e-bottom-sheet > [slot="header"] { margin-block-end: var(--m3e-bottom-sheet-padding-block, ${A.measurement.space100}); margin-inline: var(--m3e-bottom-sheet-padding-inline, ${A.measurement.space200}); }</length>`), xS.styles = l`:host { display: block; position: fixed; left: 50%; top: calc(100vh - var(--_bottom-sheet-height)); margin: 0; padding: 0; outline: none; overflow: hidden; border: none; box-sizing: border-box; width: var(--m3e-bottom-sheet-width, 100%); max-width: var(--m3e-bottom-sheet-max-width, 640px); height: var(--_bottom-sheet-height); color: var(--m3e-bottom-sheet-color, ${A.color.onSurface}); background-color: var(--m3e-bottom-sheet-container-color, ${A.color.surfaceContainerLow}); } @supports (height: 100dvh) { :host { top: calc(100dvh - var(--_bottom-sheet-height)); } } :host([hidden]) { display: none; } :host(:not(:is(:state(--no-animate), :--no-animate))) { transition: ${c(`transform ${A.motion.duration.medium2} ${A.motion.easing.standardDecelerate},
        border-radius ${A.motion.duration.medium2} ${A.motion.easing.standard}`)}; } :host(:not([modal]):not(:is(:state(--full), :--full))) .elevation { --m3e-elevation-level: var(--m3e-bottom-sheet-elevation, ${A.elevation.level1}); } :host([modal]:not(:is(:state(--full), :--full))) .elevation { --m3e-elevation-level: var(--m3e-bottom-sheet-modal-elevation, ${A.elevation.level1}); } :host(:is(:state(--full), :--full)) .elevation { --m3e-elevation-level: var(--m3e-bottom-sheet-full-elevation, ${A.elevation.level1}); } :host(:not([modal])) { z-index: var(--m3e-bottom-sheet-z-index, 10); } :host(:not([modal]):not([open])), :host([modal]:not(:popover-open)) { border-radius: var(--m3e-bottom-sheet-minimized-container-shape, ${A.shape.corner.none}); transform: translateX(-50%) translateY(100%); } :host(:not([modal])[open]:not(:is(:state(--full), :--full))), :host([modal]:not(:is(:state(--full), :--full)):popover-open) { border-radius: var(--m3e-bottom-sheet-container-shape, ${A.shape.corner.extraLargeTop}); } :host(:not([modal])[open]:is(:state(--full), :--full)), :host([modal]:is(:state(--full), :--full):popover-open) { border-radius: var(--m3e-bottom-sheet-full-container-shape, ${A.shape.corner.extraLargeTop}); } :host(:not([modal])[open]), :host([modal]:popover-open) { transform: translateX(-50%) translateY(0); } :host([modal]:not(:is(:state(--no-animate), :--no-animate)))::backdrop { transition: ${c(`background-color ${A.motion.duration.short3} ${A.motion.easing.standard}, 
        overlay ${A.motion.duration.short3} ${A.motion.easing.standard} allow-discrete,
        visibility ${A.motion.duration.short3} ${A.motion.easing.standard} allow-discrete`)}; } :host([modal]:popover-open)::backdrop { background-color: color-mix( in srgb, var(--m3e-bottom-sheet-scrim-color, ${A.color.scrim}) var(--m3e-bottom-sheet-scrim-opacity, 32%), transparent ); } :host([modal]:popover-open:not(:is(:state(--no-animate), :--no-animate)))::backdrop { transition: ${c(`background-color ${A.motion.duration.long2} ${A.motion.easing.standard}, 
        overlay ${A.motion.duration.long2} ${A.motion.easing.standard} allow-discrete,
        visibility ${A.motion.duration.long2} ${A.motion.easing.standard} allow-discrete`)}; } @starting-style { :host([modal]:popover-open)::backdrop { background-color: color-mix( in srgb, var(--m3e-bottom-sheet-scrim-color, ${A.color.scrim}) 0%, transparent ); } } .base { contain: layout style paint; display: flex; border-radius: inherit; flex-direction: column; height: 100%; --_bottom-sheet-peek-height: var(--m3e-bottom-sheet-peek-height, 0); --_bottom-sheet-top-space: var(--m3e-bottom-sheet-compact-top-space, ${A.measurement.space900}); } @media (max-height: 640px) { .base { --_bottom-sheet-top-space: var(--m3e-bottom-sheet-top-space, ${A.measurement.space700}); } } .body { flex: 1 1 auto; overflow-y: auto; scrollbar-width: ${A.scrollbar.thinWidth}; scrollbar-color: ${A.scrollbar.color}; padding-block-end: var(--m3e-bottom-sheet-padding-block, ${A.measurement.space100}); padding-inline: var(--m3e-bottom-sheet-padding-inline, ${A.measurement.space200}); font-size: var(--m3e-bottom-sheet-content-font-size, ${A.typescale.standard.body.medium.fontSize}); font-weight: var( --m3e-bottom-sheet-content-font-weight, ${A.typescale.standard.body.medium.fontWeight} ); line-height: var( --m3e-bottom-sheet-content-line-height, ${A.typescale.standard.body.medium.lineHeight} ); letter-spacing: var(--m3e-bottom-sheet-content-tracking, ${A.typescale.standard.body.medium.tracking}); } .content { height: fit-content; } :host(:not([handle]):not(:is(:state(--with-header), :--with-header))) .header { display: none; } :host(:not([handle])) .body, .header { border-top-left-radius: inherit; border-top-right-radius: inherit; padding-block-start: var(--m3e-bottom-sheet-padding-block, ${A.measurement.space100}); } .header { cursor: grab; touch-action: none; outline: none; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); box-sizing: border-box; flex: none; display: flex; flex-direction: column; min-height: 48px; --m3e-app-bar-container-color: var(--m3e-bottom-sheet-container-color, ${A.color.surfaceContainerLow}); font-size: var(--m3e-bottom-sheet-header-font-size, ${A.typescale.standard.title.large.fontSize}); font-weight: var(--m3e-bottom-sheet-header-font-weight, ${A.typescale.standard.title.large.fontWeight}); line-height: var(--m3e-bottom-sheet-header-line-height, ${A.typescale.standard.title.large.lineHeight}); letter-spacing: var(--m3e-bottom-sheet-header-tracking, ${A.typescale.standard.title.large.tracking}); } .handle-row { position: relative; flex: none; display: flex; align-items: center; justify-content: center; opacity: 1; visibility: visible; height: var(--m3e-bottom-sheet-handle-container-height, 24px); } :host(:not(:is(:state(--no-animate), :--no-animate))) .handle-row { transition: ${c(`opacity ${A.motion.duration.short3} ${A.motion.easing.standard},
        padding ${A.motion.duration.short3} ${A.motion.easing.standard},
        height ${A.motion.duration.short3} ${A.motion.easing.standard},
        visibility ${A.motion.duration.short3} ${A.motion.easing.standard} allow-discrete`)}; } .handle { position: relative; width: var(--m3e-bottom-sheet-handle-width, 32px); height: var(--m3e-bottom-sheet-handle-height, 4px); border-radius: var(--m3e-bottom-sheet-handle-shape, ${A.shape.corner.full}); background-color: var(--m3e-bottom-sheet-handle-color, ${A.color.onSurfaceVariant}); } .handle-touch { position: absolute; aspect-ratio: 1 / 1; height: 48px; left: calc(0px - calc(calc(48px - var(--m3e-bottom-sheet-handle-width, 32px)) / 2)); right: calc(0px - calc(calc(48px - var(--m3e-bottom-sheet-handle-width, 32px)) / 2)); top: calc( 0px - calc( calc(48px - var(--m3e-bottom-sheet-handle-container-height, 24px)) - calc( var(--m3e-bottom-sheet-handle-height, 4px) / 2 ) ) ); } @media (prefers-reduced-motion) { :host(:not(:is(:state(--no-animate), :--no-animate))), :host([modal]:not(:is(:state(--no-animate), :--no-animate)))::backdrop, :host([modal]:popover-open:not(:is(:state(--no-animate), :--no-animate)))::backdrop, :host(:not(:is(:state(--no-animate), :--no-animate))) .handle-row { transition: none; } } @media (forced-colors: active) { :host([modal]:not(:is(:state(--no-animate), :--no-animate)))::backdrop, :host([modal]:popover-open:not(:is(:state(--no-animate), :--no-animate)))::backdrop { transition: none; } .base { border-style: solid; border-width: 1px; border-color: CanvasText; } .handle { background-color: ButtonText; } }`, e([S({
	type: Boolean,
	reflect: !0
})], xS.prototype, "modal", void 0), e([S({
	type: Boolean,
	reflect: !0
})], xS.prototype, "open", void 0), e([S({ type: Boolean })], xS.prototype, "handle", void 0), e([S({ attribute: "handle-label" })], xS.prototype, "handleLabel", void 0), e([S({
	attribute: "detents",
	converter: Or
})], xS.prototype, "detents", void 0), e([S({ type: Number })], xS.prototype, "detent", void 0), e([S({
	type: Boolean,
	reflect: !0
})], xS.prototype, "hideable", void 0), e([S({
	attribute: "hide-friction",
	type: Number
})], xS.prototype, "hideFriction", void 0), e([S({
	attribute: "overshoot-limit",
	type: Number
})], xS.prototype, "overshootLimit", void 0), xS = bS = e([w("m3e-bottom-sheet")], xS);
var SS = class extends Ki(sa) {
	constructor() {
		super(...arguments), this.secondary = !1;
	}
	attach(e) {
		e instanceof xS && (super.attach(e), this.parentElement && (e.modal ? this.parentElement.ariaHasPopup = "dialog" : this.secondary || (this.parentElement.ariaExpanded = "false", Hu(this.parentElement, "aria-controls", e.id), this.parentElement.id && Hu(e, "aria-labelledby", this.parentElement.id))));
	}
	detach() {
		this.control && this.parentElement && !this.secondary && Uu(this.parentElement, "aria-controls", this.control.id), super.detach();
	}
	_onClick() {
		this.control instanceof xS && (this.control.modal ? this.control.show(this.detent) : (this.control.toggle(this.detent), !this.secondary && this.parentElement && (this.parentElement.ariaExpanded = `${this.control.open}`)));
	}
};
e([S({ type: Number })], SS.prototype, "detent", void 0), e([S({ type: Boolean })], SS.prototype, "secondary", void 0), SS = e([w("m3e-bottom-sheet-trigger")], SS);
//#endregion
//#region node_modules/@m3e/web/dist/dialog.js
var CS = class extends sa {
	constructor() {
		super(...arguments), this.returnValue = "";
	}
	_onClick() {
		this.closest("m3e-dialog")?.hide(this.returnValue);
	}
};
e([S({ attribute: "return-value" })], CS.prototype, "returnValue", void 0), CS = e([w("m3e-dialog-action")], CS);
var wS, TS, ES, DS, OS, kS, AS, jS, MS, NS, PS, FS, IS = FS = class extends T(x) {
	constructor() {
		super(...arguments), wS.add(this), TS.set(this, FS.__nextId++), ES.set(this, !1), DS.set(this, !1), this._withActions = !1, OS.set(this, new Dr(this)), this.alert = !1, this.dismissible = !1, this.disableClose = !1, this.noFocusTrap = !1, this.closeLabel = "Close", this.returnValue = "";
	}
	get open() {
		return t(this, ES, "f");
	}
	set open(e) {
		e !== t(this, ES, "f") && (n(this, ES, e, "f"), t(this, ES, "f") ? this.show() : this.hide());
	}
	async show() {
		if (await this.updateComplete, this._base.open) return;
		if (!this.dispatchEvent(new Event("opening", { cancelable: !0 }))) {
			this.open = !1;
			return;
		}
		let e = this.shadowRoot?.querySelector("m3e-collapsible");
		e && (e.open = !0), t(this, OS, "f").lock(), this._base.showModal(), this._content.scrollTop = 0;
		let n = this.querySelector("[autofocus]");
		n && St(n), this.dispatchEvent(new Event("opened"));
	}
	async hide(e = this.returnValue) {
		if (!this.isConnected || (await this.updateComplete, t(this, OS, "f").unlock(), !this._base.open)) {
			this.open = !1;
			return;
		}
		let n = this.shadowRoot?.querySelector("m3e-collapsible");
		n && (n.open = !1);
		let r = this.returnValue;
		this.returnValue = e, this.dispatchEvent(new Event("closing", { cancelable: !0 })) ? (this.open = !1, this._base.close(e), this.dispatchEvent(new Event("closed"))) : this.returnValue = r;
	}
	render() {
		return y`<dialog class="base" role="${rt(this.alert ? "alertdialog" : void 0)}" aria-labelledby="m3e-dialog-${t(this, TS, "f")}-header" .returnvalue="${this.returnValue}" @close="${t(this, wS, "m", AS)}" @cancel="${t(this, wS, "m", jS)}" @click="${t(this, wS, "m", MS)}" @keydown="${t(this, wS, "m", NS)}"><div class="container"><m3e-focus-trap ?disabled="${this.noFocusTrap}"><div class="header"><slot name="header" id="m3e-dialog-${t(this, TS, "f")}-header"></slot>${t(this, wS, "m", kS).call(this)}</div><m3e-scroll-container class="content" dividers="${this._withActions ? "above-below" : "above"}"><slot></slot></m3e-scroll-container><div class="actions"><slot name="actions" @slotchange="${t(this, wS, "m", PS)}"></slot></div></m3e-focus-trap></div></dialog>`;
	}
};
TS = /* @__PURE__ */ new WeakMap(), ES = /* @__PURE__ */ new WeakMap(), DS = /* @__PURE__ */ new WeakMap(), OS = /* @__PURE__ */ new WeakMap(), wS = /* @__PURE__ */ new WeakSet(), kS = function() {
	return this.dismissible ? y`<m3e-icon-button aria-label="${this.closeLabel}" class="close" @click="${this.hide}"><slot name="close-icon"><svg class="close-icon" viewBox="0 -960 960 960" fill="currentColor"><path d="m256-200-56-56 224-224-224-224 56-56 224 224 224-224 56 56-224 224 224 224-56 56-224-224-224 224Z"/></svg></slot></m3e-icon-button>` : b;
}, AS = function() {
	t(this, DS, "f") && (n(this, DS, !0, "f"), this._base?.dispatchEvent(new Event("cancel", { cancelable: !0 })));
}, jS = function(e) {
	e.target === this._base && (n(this, DS, !1, "f"), e.preventDefault(), this.dispatchEvent(new Event("cancel", { cancelable: !0 })) && this.hide());
}, MS = function(e) {
	!this.disableClose && e.target === this._base && this.hide();
}, NS = function(e) {
	e.key === "Escape" && !e.shiftKey && !e.ctrlKey && (e.preventDefault(), this.disableClose || this.hide());
}, PS = function(e) {
	this._withActions = e.target.assignedNodes({ flatten: !0 }).length > 0, D(this, "--with-actions", this._withActions);
}, IS.styles = l`:host { display: contents; } :host([hidden]) { display: none; } .base { contain: layout style; font: inherit; border: unset; outline: unset; padding: unset; display: flex; flex-direction: column; position: fixed; overflow: visible; border-radius: var(--m3e-dialog-shape, ${A.shape.corner.extraLarge}); min-width: var(--m3e-dialog-min-width, 280px); max-width: var(--m3e-dialog-max-width, min(560px, 100% - 48px)); max-height: var(--m3e-dialog-max-height, min(560px, 100% - 48px)); color: var(--m3e-dialog-color, ${A.color.onSurface}); background-color: transparent; visibility: hidden; transform-origin: top; transform: translateY(-50px); } .container { flex: 1 1 auto; display: flex; flex-direction: column; border-radius: inherit; position: relative; min-height: 0; } .container::before { content: ""; position: absolute; inset: 0; border-radius: inherit; background-color: var(--m3e-dialog-container-color, ${A.color.surfaceContainerHigh}); box-shadow: ${A.elevation.level3}; } .header { position: relative; flex: none; display: flex; align-items: center; padding: ${c(`${A.measurement.space300} ${A.measurement.space300} ${A.measurement.space200} ${A.measurement.space300}`)}; background-color: var(--m3e-dialog-header-container-color, transparent); } ::slotted([slot="header"]) { margin: unset; flex: 1 1 auto; color: var(--m3e-dialog-header-color, inherit); font-size: var(--m3e-dialog-header-font-size, ${A.typescale.standard.headline.small.fontSize}); font-weight: var(--m3e-dialog-header-font-weight, ${A.typescale.standard.headline.small.fontWeight}); line-height: var(--m3e-dialog-header-line-height, ${A.typescale.standard.headline.small.lineHeight}); letter-spacing: var(--m3e-dialog-header-tracking, ${A.typescale.standard.headline.small.tracking}); } .close { margin-inline-start: ${A.measurement.space100}; align-self: flex-start; } ::slotted([slot="close-icon"]), .close-icon { width: 1em; font-size: var(--m3e-icon-button-icon-size, 24px) !important; } .content { flex: 1 1 auto; padding-inline: ${A.measurement.space300}; color: var(--m3e-dialog-content-color, ${A.color.onSurfaceVariant}); font-size: var(--m3e-dialog-content-font-size, ${A.typescale.standard.body.medium.fontSize}); font-weight: var(--m3e-dialog-content-font-weight, ${A.typescale.standard.body.medium.fontWeight}); line-height: var(--m3e-dialog-content-line-height, ${A.typescale.standard.body.medium.lineHeight}); letter-spacing: var(--m3e-dialog-content-tracking, ${A.typescale.standard.body.medium.tracking}); } .actions { flex: none; padding: ${A.measurement.space300}; } ::slotted([slot="actions"]) { flex: none; display: flex; align-items: center; min-height: 24px; column-gap: ${A.measurement.space100}; } ::slotted([slot="actions"][end]) { justify-content: flex-end; } :host(:not(:is(:state(--with-actions), :--with-actions))) .content { margin-bottom: ${A.measurement.space300}; } :host(:not(:is(:state(--with-actions), :--with-actions))) .actions { display: none; } .base::backdrop { background-color: color-mix(in srgb, var(--m3e-dialog-scrim-color, ${A.color.scrim}) 0%, transparent); margin-inline-end: -20px; } .base:not([open]) { visibility: hidden; transform: translateY(-50px); transition: ${c(`transform ${A.motion.duration.short3} ${A.motion.easing.emphasized},
        overlay ${A.motion.duration.short3} ${A.motion.easing.emphasized} allow-discrete,
        visibility ${A.motion.duration.short3} ${A.motion.easing.emphasized} allow-discrete`)}; } .base[open] { visibility: visible; transform: translateY(0); transition: ${c(`transform ${A.motion.duration.long2} ${A.motion.easing.emphasized},
        overlay ${A.motion.duration.long2} ${A.motion.easing.emphasized} allow-discrete,
        visibility ${A.motion.duration.long2} ${A.motion.easing.emphasized} allow-discrete`)}; } .base:not([open])::backdrop { transition: ${c(`background-color ${A.motion.duration.short3} ${A.motion.easing.standard}, 
        overlay ${A.motion.duration.short3} ${A.motion.easing.standard} allow-discrete,
        visibility ${A.motion.duration.short3} ${A.motion.easing.standard} allow-discrete`)}; } .base[open]::backdrop { background-color: color-mix( in srgb, var(--m3e-dialog-scrim-color, ${A.color.scrim}) var(--m3e-dialog-scrim-opacity, 32%), transparent ); transition: ${c(`background-color ${A.motion.duration.long2} ${A.motion.easing.standard}, 
        overlay ${A.motion.duration.long2} ${A.motion.easing.standard} allow-discrete,
        visibility ${A.motion.duration.long2} ${A.motion.easing.standard} allow-discrete`)}; } .base:not([open]) .container::before { height: 35%; transition: ${c(`height ${A.motion.duration.short3} ${A.motion.easing.emphasizedAccelerate}`)}; } .base[open] .container::before { height: 100%; transition: ${c(`height ${A.motion.duration.long2} ${A.motion.easing.emphasized}`)}; } .base:not([open]) .header { opacity: 0; transition: opacity ${A.motion.duration.short2} linear; } .base[open] .header { opacity: 1; transition: ${c(`opacity ${A.motion.duration.medium1} linear calc(${A.motion.duration.medium1} * 0.2)`)}; } .base:not([open]) .content { opacity: 0; transition: opacity ${A.motion.duration.short2} linear; } .base[open] .content { opacity: 1; transition: ${c(`opacity ${A.motion.duration.medium1} linear calc(${A.motion.duration.medium1} * 0.2)`)}; } .base:not([open]) .actions { opacity: 0; transition: opacity ${A.motion.duration.short2} linear; } .base[open] .actions { opacity: 1; transition: ${c(`opacity ${A.motion.duration.medium2} linear calc(${A.motion.duration.medium2} * 0.5)`)}; } .base[open] .header { opacity: 1; transition: opacity ${A.motion.duration.medium1} linear; } @starting-style { .base[open] { transform: translateY(-50px); } .base[open] .container, .base[open] .header, .base[open] .content, .base[open] .actions { opacity: 0; } .base[open] .container::before { height: 35%; } .base[open]::backdrop { background-color: color-mix(in srgb, var(--m3e-dialog-scrim-color, ${A.color.scrim}) 0%, transparent); } } @media (forced-colors: active) { .base:not([open])::backdrop, .base[open]::backdrop { transition: none; } .base { border-style: solid; border-width: 1px; border-color: CanvasText; } } @media (prefers-reduced-motion) { .base:not([open]), .base[open], .base:not([open]) .container, .base[open] .container, .base:not([open]) .container::before, .base[open] .container::before, .base:not([open]) .header, .base[open] .header, .base:not([open]) .content, .base[open] .content, .base:not([open]) .actions, .base[open] .actions, .base:not([open])::backdrop, .base[open]::backdrop { transition: none; } }`, IS.__nextId = 0, e([Ze()], IS.prototype, "_withActions", void 0), e([C(".base")], IS.prototype, "_base", void 0), e([C(".content")], IS.prototype, "_content", void 0), e([S({ type: Boolean })], IS.prototype, "alert", void 0), e([S({
	type: Boolean,
	reflect: !0
})], IS.prototype, "open", null), e([S({ type: Boolean })], IS.prototype, "dismissible", void 0), e([S({
	attribute: "disable-close",
	type: Boolean
})], IS.prototype, "disableClose", void 0), e([S({
	attribute: "no-focus-trap",
	type: Boolean
})], IS.prototype, "noFocusTrap", void 0), e([S({ attribute: "close-label" })], IS.prototype, "closeLabel", void 0), IS = FS = e([w("m3e-dialog")], IS);
var LS = class extends Ki(sa) {
	connectedCallback() {
		super.connectedCallback(), this.parentElement && (this.parentElement.ariaHasPopup = "dialog");
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.parentElement && (this.parentElement.ariaHasPopup = null);
	}
	_onClick() {
		this.control instanceof IS && this.control.show();
	}
};
LS = e([w("m3e-dialog-trigger")], LS);
//#endregion
//#region node_modules/@m3e/web/dist/menu.js
var RS = class extends qi(Vi(T(vi(x), !0))) {
	constructor() {
		super(), new sn(this, { callback: (e) => {
			e && this.menu?._activate();
		} });
	}
	get menu() {
		return this.closest("m3e-menu");
	}
	firstUpdated(e) {
		super.firstUpdated(e), [
			this._focusRing,
			this._stateLayer,
			this._ripple
		].forEach((e) => e?.attach(this));
	}
	render() {
		return y`<div class="base"><m3e-state-layer class="state-layer" ?disabled="${this.disabled}"></m3e-state-layer><m3e-focus-ring class="focus-ring" ?disabled="${this.disabled}"></m3e-focus-ring><m3e-ripple class="ripple" ?disabled="${this.disabled}"></m3e-ripple><div class="touch" aria-hidden="true"></div>${Yi(this) ? this[Ji]() : b}<div class="wrapper">${this._renderContent()}</div></div>`;
	}
};
RS.styles = l`:host { display: inline-block; outline: none; user-select: none; flex: none; height: calc(var(--m3e-menu-item-container-height, 44px) + ${A.density.calc(-3)}); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } :host([hidden]) { display: none; } :host(:not(:disabled)) .base { color: var(--m3e-menu-item-color, ${A.color.onSurface}); } :host(:not([aria-expanded="true"])) .base { --m3e-state-layer-hover-color: var(--m3e-menu-item-container-hover-color, ${A.color.onSurface}); --m3e-state-layer-focus-color: var(--m3e-menu-item-container-focus-color, ${A.color.onSurface}); --m3e-ripple-color: var(--m3e-menu-item-ripple-color, ${A.color.onSurface}); } :host(:not(:disabled)[aria-expanded="true"]) .base { background-color: color-mix( in srgb, var(--m3e-menu-item-active-state-layer-color, ${A.color.onSurface}) var(--m3e-menu-active-state-layer-opacity, 8%), transparent ); } :host([aria-expanded="true"]) .state-layer { display: none; } :host(:not(:disabled)[checked]) .base { color: var(--m3e-menu-item-selected-color, ${A.color.onTertiaryContainer}); background-color: var(--m3e-menu-item-selected-container-color, ${A.color.tertiaryContainer}); } :host([checked]) .base { --m3e-state-layer-hover-color: var( --m3e-menu-item-selected-container-hover-color, ${A.color.onTertiaryContainer} ); --m3e-state-layer-focus-color: var( --m3e-menu-item-selected-container-focus-color, ${A.color.onTertiaryContainer} ); --m3e-ripple-color: var(--m3e-menu-item-selected-ripple-color, ${A.color.onTertiaryContainer}); } :host(:not(:disabled)) { cursor: pointer; } :host(:disabled) .base { color: color-mix( in srgb, var(--m3e-menu-item-disabled-color, ${A.color.onSurface}) var(--m3e-menu-item-disabled-opacity, 38%), transparent ); } .base { box-sizing: border-box; vertical-align: middle; display: inline-flex; align-items: center; position: relative; width: 100%; height: 100%; border-radius: var(--m3e-menu-item-shape, ${A.shape.corner.extraSmall}); transition: ${c(`border-radius ${A.motion.spring.fastEffects}`)}; } :host([checked]:not(:is(:state(--first), :--first))) .base { border-top-left-radius: var(--m3e-menu-item-selected-shape, ${A.shape.corner.medium}); border-top-right-radius: var(--m3e-menu-item-selected-shape, ${A.shape.corner.medium}); } :host([checked]:not(:is(:state(--last), :--last))) .base { border-bottom-left-radius: var(--m3e-menu-item-selected-shape, ${A.shape.corner.medium}); border-bottom-right-radius: var(--m3e-menu-item-selected-shape, ${A.shape.corner.medium}); } :host(:is(:state(--first), :--first)) .base { border-top-left-radius: var(--m3e-menu-item-first-child-shape, ${A.shape.corner.medium}); border-top-right-radius: var(--m3e-menu-item-first-child-shape, ${A.shape.corner.medium}); } :host(:is(:state(--last), :--last)) .base { border-bottom-left-radius: var(--m3e-menu-item-last-child-shape, ${A.shape.corner.medium}); border-bottom-right-radius: var(--m3e-menu-item-last-child-shape, ${A.shape.corner.medium}); } .touch { position: absolute; height: calc( var(--m3e-menu-item-container-height, 44px) + calc(var(--m3e-menu-gap, ${A.measurement.space25}) * 2) + ${A.density.calc(-3)} ); left: 0; right: 0; } .wrapper { flex: 1 1 auto; display: inline-flex; align-items: center; column-gap: var(--m3e-menu-item-icon-label-space, ${A.measurement.space100}); padding-inline-start: var(--m3e-menu-item-padding-start, ${A.measurement.space150}); padding-inline-end: var(--m3e-menu-item-padding-end, ${A.measurement.space150}); font-size: var(--m3e-menu-item-label-text-font-size, ${A.typescale.standard.label.large.fontSize}); font-weight: var( --m3e-menu-item-label-text-font-weight, ${A.typescale.standard.label.large.fontWeight} ); line-height: var( --m3e-menu-item-label-text-line-height, ${A.typescale.standard.label.large.lineHeight} ); letter-spacing: var(--m3e-menu-item-label-text-tracking, ${A.typescale.standard.label.large.tracking}); } .focus-ring { border-radius: var(--m3e-menu-item-focus-ring-shape, inherit); } .content { flex: 1 1 auto; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; } ::slotted([slot="icon"]), ::slotted([slot="trailing-icon"]), .trailing-icon { flex: none; width: 1em; font-size: var(--m3e-menu-item-icon-size, 20px) !important; } @media (prefers-reduced-motion) { .base { transition: none; } } @media (forced-colors: active) { .base { background-color: Menu; color: MenuText; } :host(:disabled) .base { color: GrayText; } }`, e([C(".focus-ring")], RS.prototype, "_focusRing", void 0), e([C(".state-layer")], RS.prototype, "_stateLayer", void 0), e([C(".ripple")], RS.prototype, "_ripple", void 0);
var zS = class extends Ki(sa) {
	get menu() {
		return this.control?.tagName === "M3E-MENU" ? this.control : null;
	}
	attach(e) {
		super.attach(e);
		let t = this.menu;
		t && (this.parentElement && (this.parentElement.ariaHasPopup = "menu", this.parentElement.ariaExpanded = "false", t.id && Hu(this.parentElement, "aria-controls", t.id)), this.closest("m3e-menu") && (t.submenu = !0));
	}
	detach() {
		if (this.parentElement) {
			this.parentElement.ariaHasPopup = null, this.parentElement.ariaExpanded = null;
			let e = this.menu;
			e?.id && Uu(this.parentElement, "aria-controls", e.id);
		}
		super.detach();
	}
	_onClick() {
		this.parentElement && (this.parentElement.tagName === "M3E-MENU-ITEM" ? this.menu?.show(this.parentElement) : this.menu?.toggle(this.parentElement));
	}
};
zS = e([w("m3e-menu-trigger")], zS);
var BS, VS, HS, US, WS, GS, KS, qS, JS, YS, XS, ZS, QS = ZS = class extends Zi(j(RS, "menuitem")) {
	constructor() {
		super(), BS.add(this), VS.set(this, (e) => t(this, BS, "m", JS).call(this, e)), HS.set(this, (e) => t(this, BS, "m", YS).call(this, e)), US.set(this, () => t(this, BS, "m", XS).call(this)), this._hasSubmenu = !1, WS.set(this, void 0), new yn(this, {
			startDelay: 500,
			endDelay: 500,
			callback: (e) => {
				e && !this.disabled && t(this, WS, "f") && t(this, WS, "f").menu?.show(this);
			}
		});
	}
	get submenu() {
		return t(this, WS, "f")?.menu ?? null;
	}
	connectedCallback() {
		super.connectedCallback(), this.addEventListener("click", t(this, VS, "f")), this.addEventListener("keydown", t(this, HS, "f")), this.addEventListener("mouseenter", t(this, US, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("click", t(this, VS, "f")), this.removeEventListener("keydown", t(this, HS, "f")), this.removeEventListener("mouseenter", t(this, US, "f"));
	}
	_renderContent() {
		return y`<slot name="icon" aria-hidden="true" @slotchange="${t(this, BS, "m", KS)}"></slot><span class="content"><slot @slotchange="${t(this, BS, "m", GS)}"></slot></span>${this._hasSubmenu ? Sd.current === "ltr" ? y`<svg class="trailing-icon" aria-hidden="true" viewBox="0 -960 960 960" fill="currentColor"><path d="M400-280v-400l200 200-200 200Z"/></svg>` : y`<svg class="trailing-icon" aria-hidden="true" viewBox="0 -960 960 960" fill="currentColor"><path d="M560-280 360-480l200-200v400Z"/></svg>` : y`<slot name="trailing-icon" aria-hidden="true" @slotchange="${t(this, BS, "m", qS)}"></slot>`}`;
	}
};
VS = /* @__PURE__ */ new WeakMap(), HS = /* @__PURE__ */ new WeakMap(), US = /* @__PURE__ */ new WeakMap(), WS = /* @__PURE__ */ new WeakMap(), BS = /* @__PURE__ */ new WeakSet(), GS = function(e) {
	n(this, WS, e.target.assignedElements({ flatten: !0 }).find((e) => e instanceof zS), "f"), this._hasSubmenu = t(this, WS, "f") !== void 0;
}, KS = function(e) {
	D(this, "--with-icon", Rt(e.target));
}, qS = function(e) {
	D(this, "--with-trailing-icon", Rt(e.target));
}, JS = function(e) {
	!e.defaultPrevented && !this._hasSubmenu && this.menu?.hideAll(!0);
}, YS = function(e) {
	if (!(e.defaultPrevented || this.disabled)) switch (e.key) {
		case "Right":
		case "ArrowRight":
			Sd.current === "ltr" && (e.preventDefault(), this.submenu?.show(this));
			break;
		case "Left":
		case "ArrowLeft": Sd.current === "rtl" && (e.preventDefault(), this.submenu?.show(this));
	}
}, XS = function() {
	this.menu?.items.forEach((e) => {
		e instanceof ZS && e !== this && e.submenu?.isOpen && e.submenu.hide();
	});
}, e([Ze()], QS.prototype, "_hasSubmenu", void 0), QS = ZS = e([w("m3e-menu-item")], QS);
function $S(e) {
	return e === "before" || e === "after";
}
function eC(e) {
	return e === "standard" || e === "vibrant";
}
var tC, nC, rC, iC, aC, oC, sC, cC, lC, uC, dC, fC, pC, mC, hC, gC, _C, vC, yC = vC = class extends ia(T(j(x, "menu"))) {
	constructor() {
		super(...arguments), tC.add(this), nC.set(this, void 0), rC.set(this, void 0), iC.set(this, void 0), aC.set(this, new Mu().withWrap().withHomeAndEnd().withVerticalOrientation()), oC.set(this, (e) => t(this, tC, "m", mC).call(this, e)), sC.set(this, () => t(this, tC, "m", hC).call(this)), cC.set(this, new yr(this, {
			target: null,
			callback: (e) => e instanceof vC ? e.items.filter((e) => e instanceof QS).forEach((e) => e.submenu?.hide()) : this.hideAll()
		})), lC.set(this, new yt(this, {
			target: null,
			callback: (e) => {
				!this.submenu && !e.some((e) => e instanceof vC) && this.hide();
			}
		})), uC.set(this, (e) => {
			switch (e.newState) {
				case "open":
					t(this, lC, "f").observe(this), t(this, nC, "f") && t(this, lC, "f").observe(t(this, nC, "f")), setTimeout(() => t(this, aC, "f").setActiveItem(t(this, aC, "f").items.find((e) => !e.disabled)), 40);
					break;
				case "closed": t(this, lC, "f").unobserveAll(), t(this, rC, "f")?.call(this), n(this, rC, void 0, "f"), n(this, iC, void 0, "f");
			}
		}), this.positionX = "after", this.positionY = "below", this.variant = "standard", this.submenu = !1;
	}
	get items() {
		return t(this, aC, "f").items;
	}
	get isOpen() {
		return t(this, nC, "f") !== void 0;
	}
	connectedCallback() {
		super.connectedCallback(), t(this, tC, "m", dC).call(this), t(this, tC, "m", fC).call(this), this.tabIndex = -1, this.setAttribute("popover", "manual"), this.addEventListener("keydown", t(this, oC, "f")), this.addEventListener("mouseenter", t(this, sC, "f")), this.addEventListener("toggle", t(this, uC, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("keydown", t(this, oC, "f")), this.removeEventListener("mouseenter", t(this, sC, "f")), this.removeEventListener("toggle", t(this, uC, "f")), t(this, tC, "m", _C).call(this);
	}
	async show(e) {
		t(this, nC, "f") && t(this, nC, "f") !== e && this.hide();
		let r = this.positionX;
		Sd.current === "rtl" && (r = r === "before" ? "after" : "before"), n(this, rC, await xp(this, e, {
			position: this.submenu ? r === "before" ? "left-start" : "right-start" : this.positionY === "above" ? r === "before" ? "top-end" : "top-start" : r === "before" ? "bottom-end" : "bottom-start",
			inline: !0,
			flip: !0,
			shift: "main",
			offset: this.submenu ? void 0 : 4
		}, (e, r, i) => {
			if (!this.submenu) D(this, "--top", i.includes("top")), D(this, "--bottom", i.includes("bottom"));
			else if (t(this, nC, "f")) {
				let e = t(this, tC, "m", gC).call(this, t(this, nC, "f")).y;
				D(this, "--shift-down", !1), D(this, "--shift-up", !1), D(this, Math.round(r) === Math.round(e) ? "--shift-down" : "--shift-up", !0);
			}
			(t(this, iC, "f")?.dir !== Sd.current || t(this, iC, "f")?.x !== e) && (Sd.current === "rtl" ? this.style.right = `${window.innerWidth - e - this.clientWidth}px` : this.style.left = `${e}px`), t(this, iC, "f")?.y !== r && (this.style.top = `${r}px`), n(this, iC, {
				x: e,
				y: r,
				dir: Sd.current
			}, "f");
		}), "f");
		let i = e.closest("m3e-menu");
		i ? this.variant = i.variant : this._activate(), this.showPopover(), n(this, nC, e, "f"), t(this, nC, "f").ariaExpanded = "true", t(this, cC, "f").observe(t(this, nC, "f"));
	}
	hide(e = !1) {
		for (let e of t(this, aC, "f").items) {
			let t = e.submenu;
			t && t.isOpen && t.hide();
		}
		t(this, tC, "m", _C).call(this), this.hidePopover(), t(this, nC, "f") && (t(this, nC, "f").ariaExpanded = "false", e && t(this, nC, "f").focus(), t(this, cC, "f").unobserve(t(this, nC, "f")), n(this, nC, void 0, "f"));
	}
	hideAll(e = !1) {
		let n = this;
		for (; t(n, nC, "f");) {
			let e = t(n, nC, "f").closest("m3e-menu");
			if (!e) break;
			n = e;
		}
		n.hide(e);
	}
	async toggle(e) {
		t(this, nC, "f") ? this.hide() : await this.show(e);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, tC, "m", dC).call(this), (e.has("positionX") || e.has("positionY")) && t(this, tC, "m", fC).call(this);
	}
	render() {
		return y`<div class="base"><slot @slotchange="${t(this, tC, "m", pC)}"></slot></div>`;
	}
	_activate() {
		this !== vC.__activeMenu && (vC.__activeMenu && E(vC.__activeMenu, "--active"), vC.__activeMenu = this, zr(vC.__activeMenu, "--active"));
	}
};
nC = /* @__PURE__ */ new WeakMap(), rC = /* @__PURE__ */ new WeakMap(), iC = /* @__PURE__ */ new WeakMap(), aC = /* @__PURE__ */ new WeakMap(), oC = /* @__PURE__ */ new WeakMap(), sC = /* @__PURE__ */ new WeakMap(), cC = /* @__PURE__ */ new WeakMap(), lC = /* @__PURE__ */ new WeakMap(), uC = /* @__PURE__ */ new WeakMap(), tC = /* @__PURE__ */ new WeakSet(), dC = function() {
	eC(this.variant) || (this.variant = "standard"), Br(this, this.variant, "standard", "vibrant");
}, fC = function() {
	$S(this.positionX) || (this.positionX = "after"), $S(this.positionY) || (this.positionY = "below");
}, pC = function() {
	let { added: e } = t(this, aC, "f").setItems([...this.querySelectorAll("m3e-menu-item,m3e-menu-item-checkbox,m3e-menu-item-radio")].filter((e) => e.closest("m3e-menu") === this));
	t(this, aC, "f").activeItem || t(this, aC, "f").updateActiveItem(e.find((e) => !e.disabled)), t(this, aC, "f").items.forEach((e, n) => {
		D(e, "--first", n === 0 && !e.previousElementSibling), D(e, "--last", n === t(this, aC, "f").items.length - 1);
	});
}, mC = function(e) {
	switch (e.key) {
		case "Right":
		case "ArrowRight":
			Sd.current === "rtl" ? (e.preventDefault(), this.hide(!0)) : t(this, aC, "f").onKeyDown(e);
			break;
		case "Left":
		case "ArrowLeft":
			Sd.current === "ltr" ? (e.preventDefault(), this.hide(!0)) : t(this, aC, "f").onKeyDown(e);
			break;
		case "Tab":
			this.hideAll();
			break;
		case "Escape":
			!e.shiftKey && !e.ctrlKey && this.hide(!0);
			break;
		default: t(this, aC, "f").onKeyDown(e);
	}
}, hC = function() {
	this._activate();
}, gC = function(e) {
	let t = 0, n = 0;
	for (let r = e; r; r = r.offsetParent instanceof HTMLElement ? r.offsetParent : null) t += r.offsetLeft - r.scrollLeft + r.clientLeft, n += r.offsetTop - r.scrollTop + r.clientTop;
	return {
		x: t,
		y: n
	};
}, _C = function() {
	this === vC.__activeMenu && (E(vC.__activeMenu, "--active"), vC.__activeMenu = void 0);
}, Vt(l`m3e-menu > m3e-divider { margin-block: var(--m3e-menu-divider-spacing, ${A.measurement.space100}); }`), yC.styles = l`:host { position: absolute; padding: unset; margin: unset; border: unset; overflow-y: auto; overflow-x: visible; scrollbar-width: ${A.scrollbar.thinWidth}; scrollbar-color: ${A.scrollbar.color}; scroll-padding-block: calc( var(--m3e-focus-ring-thickness, 3px) + var(--m3e-menu-container-padding-block, ${A.measurement.space50}) ); min-width: var(--m3e-menu-container-min-width, 112px); max-width: var(--m3e-menu-container-max-width, 280px); max-height: var(--m3e-menu-container-max-height, 280px); box-shadow: var(--m3e-menu-container-elevation, ${A.elevation.level3}); opacity: 0; display: none; } :host([hidden]) { display: none; } .base { contain: layout style paint; display: flex; flex-direction: column; row-gap: var(--m3e-menu-gap, ${A.measurement.space25}); min-width: inherit; max-width: inherit; line-height: 0; padding-block: var(--m3e-menu-container-padding-block, ${A.measurement.space50}); padding-inline: var(--m3e-menu-container-padding-inline, ${A.measurement.space50}); --m3e-focus-ring-outward-offset: 0px; --m3e-focus-ring-growth-factor: 1.5; } :host(:not(:is(:state(--active), :--active))) { border-radius: var(--m3e-menu-container-shape, ${A.shape.corner.small}); } :host(:not(:is(:state(--active), :--active))) .base { --m3e-menu-item-first-child-shape: ${A.shape.corner.extraSmall}; --m3e-menu-item-last-child-shape: ${A.shape.corner.extraSmall}; } :host(:is(:state(--active), :--active)) { border-radius: var(--m3e-menu-active-container-shape, ${A.shape.corner.large}); } :host(:not(:is(:state(--no-animate), :--no-animate))) { transition: ${c(`opacity ${A.motion.duration.short2} ${A.motion.easing.standard}, 
        transform ${A.motion.duration.short2} ${A.motion.easing.standard},
        overlay ${A.motion.duration.short2} ${A.motion.easing.standard} allow-discrete,
        display ${A.motion.duration.short2} ${A.motion.easing.standard} allow-discrete,
        border-radius ${A.motion.spring.fastEffects}`)}; } :host(:not([submenu])) { transform: scaleY(0.8); } :host(:not([submenu]):popover-open) { transform: scaleY(1); animation: bounce-open ${A.motion.spring.fastSpatial}; } @keyframes bounce-open { 0% { transform: scaleY(0); } 100% { transform: scaleY(1); } } :host::backdrop { background-color: transparent; } :host(:popover-open) { display: block; opacity: 1; } :host(:is(:state(--bottom), :--bottom)) { transform-origin: top; } :host(:is(:state(--top), :--top)) { transform-origin: bottom; } :host(:is(:state(--shift-down), :--shift-down)) { margin-top: calc(0px - var(--m3e-menu-container-padding-block, ${A.measurement.space50})); } :host(:is(:state(--shift-up), :--shift-up)) { margin-top: var(--m3e-menu-container-padding-block, ${A.measurement.space50}); } :host(:is(:state(--vibrant), :--vibrant)) { background-color: var(--m3e-vibrant-menu-container-color, ${A.color.tertiaryContainer}); --m3e-menu-item-color: var(--m3e-vibrant-menu-item-color, ${A.color.onTertiaryContainer}); --m3e-menu-item-container-hover-color: var( --m3e-vibrant-menu-item-container-hover-color, ${A.color.onTertiaryContainer} ); --m3e-menu-item-container-focus-color: var( --m3e-vibrant-menu-item-container-focus-color, ${A.color.onTertiaryContainer} ); --m3e-menu-item-ripple-color: var(--m3e-vibrant-menu-item-ripple-color, ${A.color.onTertiaryContainer}); --m3e-menu-item-active-state-layer-color: var( --m3e-vibrant-menu-item-active-state-layer-color, ${A.color.onTertiaryContainer} ); --m3e-menu-item-selected-color: var(--m3e-vibrant-menu-item-selected-color, ${A.color.onTertiary}); --m3e-menu-item-selected-container-color: var( --m3e-vibrant-menu-item-selected-container-color, ${A.color.tertiary} ); --m3e-menu-item-selected-container-hover-color: var( --m3e-vibrant-menu-item-selected-container-hover-color, ${A.color.onTertiary} ); --m3e-menu-item-container-selected-focus-color: var( --m3e-vibrant-menu-item-selected-container-focus-color, ${A.color.onTertiary} ); --m3e-menu-item-selected-ripple-color: var( --m3e-vibrant-menu-item-selected-ripple-color, ${A.color.onTertiary} ); --m3e-menu-item-disabled-color: var( --m3e-vibrant-menu-item-disabled-color, ${A.color.onTertiaryContainer} ); } :host(:is(:state(--standard), :--standard)) { background-color: var(--m3e-menu-container-color, ${A.color.surfaceContainer}); } @starting-style { :host(:popover-open) { opacity: 0; } } @media (prefers-reduced-motion) { :host(:not(:is(:state(--no-animate), :--no-animate))) { transition: none; } } @media (forced-colors: active) { :host { background-color: Menu; color: MenuText; outline: 1px solid MenuText; } }`, e([S({ attribute: "position-x" })], yC.prototype, "positionX", void 0), e([S({ attribute: "position-y" })], yC.prototype, "positionY", void 0), e([S({
	reflect: !0,
	useDefault: !0
})], yC.prototype, "variant", void 0), e([S({
	type: Boolean,
	reflect: !0
})], yC.prototype, "submenu", void 0), yC = vC = e([w("m3e-menu")], yC);
var bC, xC, SC, CC, wC, TC, EC, DC, OC, kC, AC, jC, MC = class extends Hr(j(RS, "menuitemcheckbox")) {
	constructor() {
		super(...arguments), bC.add(this), xC.set(this, (e) => t(this, bC, "m", OC).call(this, e)), SC.set(this, (e) => t(this, bC, "m", kC).call(this, e)), CC.set(this, () => t(this, bC, "m", AC).call(this)), wC.set(this, () => t(this, bC, "m", jC).call(this)), TC.set(this, !1);
	}
	connectedCallback() {
		super.connectedCallback(), this.addEventListener("click", t(this, xC, "f")), this.addEventListener("keydown", t(this, SC, "f")), this.addEventListener("keyup", t(this, CC, "f")), this.addEventListener("mouseenter", t(this, wC, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("click", t(this, xC, "f")), this.removeEventListener("keydown", t(this, SC, "f")), this.removeEventListener("keyup", t(this, CC, "f")), this.removeEventListener("mouseenter", t(this, wC, "f"));
	}
	_renderContent() {
		return y`<div class="icon"><svg class="check" viewBox="0 -960 960 960" aria-hidden="true"><path fill="currentColor" d="M382-240 154-468l57-57 171 171 367-367 57 57-424 424Z"/></svg><slot name="icon" @slotchange="${t(this, bC, "m", EC)}"></slot></div><slot></slot><slot name="trailing-icon" aria-hidden="true" @slotchange="${t(this, bC, "m", DC)}"></slot>`;
	}
};
xC = /* @__PURE__ */ new WeakMap(), SC = /* @__PURE__ */ new WeakMap(), CC = /* @__PURE__ */ new WeakMap(), wC = /* @__PURE__ */ new WeakMap(), TC = /* @__PURE__ */ new WeakMap(), bC = /* @__PURE__ */ new WeakSet(), EC = function(e) {
	D(this, "--with-icon", Rt(e.target));
}, DC = function(e) {
	D(this, "--with-trailing-icon", Rt(e.target));
}, OC = function(e) {
	e.defaultPrevented || (this.checked = !this.checked, this.performUpdate(), t(this, TC, "f") || (Bt() ? this.menu?.hideAll(!0) : setTimeout(() => this.menu?.hideAll(!0), 150)));
}, kC = function(e) {
	n(this, TC, e.key === " ", "f");
}, AC = function() {
	n(this, TC, !1, "f");
}, jC = function() {
	this.menu?.items.forEach((e) => {
		e instanceof QS && e.submenu?.isOpen && e.submenu.hide();
	});
}, MC.styles = [RS.styles, l`.icon { display: flex; align-items: center; justify-content: center; transition: margin-inline-start ${A.motion.spring.fastEffects}; } :host(:not(:is(:state(--with-icon), :--with-icon))) .icon { margin-inline-start: calc(0px - var(--m3e-menu-item-icon-label-space, ${A.measurement.space150})); } .check { width: 1em; font-size: var(--m3e-menu-item-icon-size, 20px) !important; } :host(:not([checked])) .check { display: none; } :host([checked]) .icon { margin-inline-start: 0; } :host([checked]) ::slotted([slot="icon"]) { display: none !important; } @media (prefers-reduced-motion) { .icon { transition: none; } }`], MC = e([w("m3e-menu-item-checkbox")], MC);
var NC = class extends j(x, "group") {
	render() {
		return y`<slot></slot>`;
	}
};
NC.styles = l`:host { display: contents; } :host([hidden]) { display: none; }`, NC = e([w("m3e-menu-item-group")], NC);
var PC, FC, IC, LC, RC, zC, BC, VC, HC, UC, WC, GC, KC = class extends Hr(j(RS, "menuitemradio")) {
	constructor() {
		super(...arguments), PC.add(this), FC.set(this, (e) => t(this, PC, "m", HC).call(this, e)), IC.set(this, (e) => t(this, PC, "m", UC).call(this, e)), LC.set(this, () => t(this, PC, "m", WC).call(this)), RC.set(this, () => t(this, PC, "m", GC).call(this)), zC.set(this, !1);
	}
	connectedCallback() {
		super.connectedCallback(), this.addEventListener("click", t(this, FC, "f")), this.addEventListener("keydown", t(this, IC, "f")), this.addEventListener("keyup", t(this, LC, "f")), this.addEventListener("mouseenter", t(this, RC, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("click", t(this, FC, "f")), this.removeEventListener("keydown", t(this, IC, "f")), this.removeEventListener("keyup", t(this, LC, "f")), this.removeEventListener("mouseenter", t(this, RC, "f"));
	}
	update(e) {
		super.update(e), e.has("checked") && this.checked && (this.closest("[role='group']") ?? this.closest("m3e-menu"))?.querySelectorAll("m3e-menu-item-radio").forEach((e) => {
			e !== this && e.checked && (e.checked = !1);
		});
	}
	_renderContent() {
		return y`<div class="icon"><svg class="check" viewBox="0 -960 960 960" aria-hidden="true"><path fill="currentColor" d="M382-240 154-468l57-57 171 171 367-367 57 57-424 424Z"/></svg><slot name="icon" @slotchange="${t(this, PC, "m", BC)}"></slot></div><slot></slot><slot name="trailing-icon" aria-hidden="true" @slotchange="${t(this, PC, "m", VC)}"></slot>`;
	}
};
FC = /* @__PURE__ */ new WeakMap(), IC = /* @__PURE__ */ new WeakMap(), LC = /* @__PURE__ */ new WeakMap(), RC = /* @__PURE__ */ new WeakMap(), zC = /* @__PURE__ */ new WeakMap(), PC = /* @__PURE__ */ new WeakSet(), BC = function(e) {
	D(this, "--with-icon", Rt(e.target));
}, VC = function(e) {
	D(this, "--with-trailing-icon", Rt(e.target));
}, HC = function(e) {
	e.defaultPrevented || (this.checked = !0, this.performUpdate(), t(this, zC, "f") || (Bt() ? this.menu?.hideAll(!0) : setTimeout(() => this.menu?.hideAll(!0), 150)));
}, UC = function(e) {
	n(this, zC, e.key === " ", "f");
}, WC = function() {
	n(this, zC, !1, "f");
}, GC = function() {
	this.menu?.items.forEach((e) => {
		e instanceof QS && e.submenu?.isOpen && e.submenu.hide();
	});
}, KC.styles = MC.styles, KC = e([w("m3e-menu-item-radio")], KC);
//#endregion
//#region skills/android-design-explore/gallery/runtime-src/p-icon.js
var qC = class extends HTMLElement {
	static observedAttributes = ["name", "label"];
	connectedCallback() {
		this.#e();
	}
	attributeChangedCallback() {
		this.isConnected && this.#e();
	}
	#e() {
		let e = this.getAttribute("label"), t = document.createElement("span");
		t.setAttribute("aria-hidden", "true"), t.textContent = this.getAttribute("name") ?? "", e ? (this.setAttribute("role", "img"), this.setAttribute("aria-label", e), this.removeAttribute("aria-hidden")) : (this.removeAttribute("role"), this.removeAttribute("aria-label"), this.setAttribute("aria-hidden", "true")), this.replaceChildren(t);
	}
};
customElements.define("p-icon", qC);
//#endregion
//#region skills/android-design-explore/gallery/runtime-src/p-floating-toolbar.js
var JC = class extends HTMLElement {
	connectedCallback() {
		let e = this.querySelector("m3e-toolbar");
		e && (e.hasAttribute("shape") || e.setAttribute("shape", "rounded"), e.setAttribute("elevated", "")), this.#n(), this.addEventListener("click", this.#t);
	}
	disconnectedCallback() {
		this.removeEventListener("click", this.#t);
	}
	#e() {
		return [...this.querySelectorAll("m3e-button[toggle]")];
	}
	#t = (e) => {
		let t = e.target.closest?.("m3e-button[toggle]");
		if (t) {
			for (let e of this.#e()) e.selected = e === t;
			this.#n();
		}
	};
	#n() {
		for (let e of this.#e()) e.setAttribute("variant", e.selected ?? e.hasAttribute("selected") ? "tonal" : "text");
	}
};
customElements.define("p-floating-toolbar", JC);
//#endregion
//#region skills/android-design-explore/gallery/runtime-src/p-sheet-handle.js
var YC = new CSSStyleSheet();
YC.replaceSync(".handle { outline: none; }");
var XC = /* @__PURE__ */ new WeakSet();
function ZC(e) {
	let t = e.shadowRoot;
	t && !XC.has(t) && (XC.add(t), t.adoptedStyleSheets = [...t.adoptedStyleSheets, YC]);
}
var QC = customElements.get("m3e-bottom-sheet");
if (QC) {
	let e = QC.prototype.connectedCallback;
	QC.prototype.connectedCallback = function() {
		e.call(this), ZC(this);
	}, document.querySelectorAll("m3e-bottom-sheet").forEach(ZC);
}
//#endregion
//#region skills/android-design-explore/gallery/runtime-src/theme-bridge.js
var $C = [
	"monochrome",
	"neutral",
	"tonal-spot",
	"vibrant",
	"expressive",
	"fidelity",
	"content",
	"rainbow",
	"fruit-salad"
], ew = [
	"light",
	"dark",
	"auto"
];
window.addEventListener("message", (e) => {
	if (e.origin !== window.location.origin) return;
	let { type: t, color: n, variant: r, scheme: i, name: a } = e.data ?? {};
	if (t === "state") {
		typeof a == "string" && typeof window.proposalState == "function" && window.runtimeSettle().then(() => window.proposalState(a)).catch(console.error);
		return;
	}
	if (t !== "theme") return;
	let o = document.querySelector("m3e-theme");
	o && (typeof n == "string" && /^#[0-9a-f]{6}$/i.test(n) && o.setAttribute("color", n), $C.includes(r) && o.setAttribute("variant", r), ew.includes(i) && o.setAttribute("scheme", i));
});
var tw = {
	plain: "Google Sans Flex",
	faces: [
		{
			family: "Fraunces",
			use: "brand",
			minPx: 24
		},
		{
			family: "Bricolage Grotesque",
			use: "brand",
			minPx: 24
		},
		{
			family: "Big Shoulders Display",
			use: "brand",
			minPx: 24
		},
		{
			family: "Doto",
			use: "display",
			minPx: 36
		},
		{
			family: "Google Sans Code",
			use: "data",
			minPx: 0
		}
	],
	generics: [
		"serif",
		"sans-serif",
		"monospace"
	]
}, nw = ({ r: e, g: t, b: n }) => {
	let r = (e) => (e /= 255, e <= .03928 ? e / 12.92 : ((e + .055) / 1.055) ** 2.4);
	return .2126 * r(e) + .7152 * r(t) + .0722 * r(n);
}, rw = (e, t) => {
	let [n, r] = [nw(e), nw(t)].sort((e, t) => t - e);
	return (n + .05) / (r + .05);
}, iw = (e, t) => e.a < 1 ? {
	r: e.r * e.a + t.r * (1 - e.a),
	g: e.g * e.a + t.g * (1 - e.a),
	b: e.b * e.a + t.b * (1 - e.a),
	a: 1
} : e, aw = (e, t) => e >= 24 || e >= 18.66 && t >= 700 ? 3 : 4.5, ow = (/* @__PURE__ */ "a[href].button.input.select.textarea.summary.[role=button].[role=link].[role=tab].[role=switch].[role=checkbox].[role=menuitem].[tabindex]:not([tabindex=\"-1\"]).m3e-button.m3e-icon-button.m3e-fab.m3e-fab-menu-trigger.m3e-fab-menu-item.m3e-nav-item.m3e-switch.m3e-slider.m3e-split-button.m3e-card[actionable].m3e-list-item-button.m3e-list-action.m3e-list-option.m3e-menu-item.m3e-menu-item-checkbox.m3e-menu-item-radio.m3e-chip.m3e-assist-chip.m3e-filter-chip.m3e-input-chip.m3e-suggestion-chip".split(".")).join(","), sw = "m3e-fab, m3e-fab-menu, [data-fab]", cw = (e, t) => {
	let n = e.split(",")[0].trim().replace(/^(["'])(.*)\1$/, "$2").toLowerCase();
	if (n === tw.plain.toLowerCase()) return !0;
	let r = tw.faces.find((e) => e.family.toLowerCase() === n);
	return !!r && t >= r.minPx;
}, lw = (e) => (e.getAttribute?.("aria-label") || e.textContent || "").trim().replace(/\s+/g, " ").slice(0, 30), uw = (e) => {
	let t = lw(e);
	return e.localName + (e.id ? `#${e.id}` : "") + (t ? ` "${t}"` : "");
}, dw = (e) => {
	let t = e.getRootNode().host, n = e.localName + (e.id ? `#${e.id}` : "") + (t && e.classList[0] ? `.${e.classList[0]}` : "");
	return t ? `${dw(t)} > ${n}` : n;
}, fw = (e) => {
	let t = lw(e);
	return dw(e) + (t ? ` "${t}"` : "");
}, pw = (e) => {
	let t = e.getBoundingClientRect();
	return t.width <= 0 || t.height <= 0 ? !1 : e.checkVisibility ? e.checkVisibility({ visibilityProperty: !0 }) : getComputedStyle(e).visibility !== "hidden";
};
function mw(e) {
	if (e.assignedSlot) return e.assignedSlot;
	let t = e.parentNode;
	return t ? t instanceof ShadowRoot ? t.host : t instanceof Element && t.shadowRoot ? null : t instanceof Element ? t : null : null;
}
function hw(e) {
	for (let t = e; t; t = mw(t)) if (getComputedStyle(t).display !== "contents") return t;
	return null;
}
var gw = (e) => {
	for (let t = e; t; t = mw(t)) if (parseFloat(getComputedStyle(t).opacity) === 0) return !0;
	return !1;
}, _w = (e) => {
	for (let t = e; t; t = mw(t)) if (t.getAttribute?.("aria-hidden") === "true") return !0;
	return !1;
}, vw = () => [...document.querySelectorAll("m3e-bottom-sheet[modal], m3e-dialog")].filter((e) => e.localName === "m3e-dialog" ? e.shadowRoot?.querySelector("dialog")?.open : e.matches(":popover-open")), yw = (e) => {
	for (let t = e; t; t = mw(t)) if (t.localName === "m3e-bottom-sheet") return !t.open;
	return !1;
}, bw = () => [...document.querySelectorAll("m3e-menu")].filter((e) => e.matches(":popover-open"));
function xw(e, t) {
	for (let n of e.querySelectorAll("*")) t(n) !== !1 && n.shadowRoot && xw(n.shadowRoot, t);
}
function Sw(e, t) {
	let n = (e) => {
		for (let n of e.childNodes) n.nodeType === Node.TEXT_NODE && n.textContent.trim() && t(n);
	};
	e instanceof ShadowRoot && n(e), xw(e, (e) => {
		n(e);
	});
}
var Cw = document.createElement("canvas");
Cw.width = Cw.height = 1;
var ww = Cw.getContext("2d", { willReadFrequently: !0 });
function Tw(e) {
	ww.clearRect(0, 0, 1, 1), ww.fillStyle = "#000", ww.fillStyle = e, ww.fillRect(0, 0, 1, 1);
	let [t, n, r, i] = ww.getImageData(0, 0, 1, 1).data;
	return {
		r: t,
		g: n,
		b: r,
		a: i / 255
	};
}
function Ew(e) {
	for (let t = e; t; t = mw(t)) {
		let e = getComputedStyle(t);
		if (parseFloat(e.opacity) < 1) return { reason: "an ancestor has opacity below 1" };
		if (e.backgroundImage !== "none") return { reason: "background is an image or gradient" };
		let n = Tw(e.backgroundColor);
		if (n.a === 1) return { color: n };
		if (n.a > 0) return { reason: "background is translucent" };
		let r = getComputedStyle(t, "::before");
		if (r.content !== "none" && r.position === "absolute" && parseFloat(r.opacity) === 1 && parseFloat(r.width) >= t.clientWidth - 1 && parseFloat(r.height) >= t.clientHeight - 1) {
			let e = Tw(r.backgroundColor);
			if (e.a === 1) return { color: e };
		}
	}
	return { reason: "no opaque background found" };
}
var Dw = /[^\s\-\u00ad\u200b\u2010-\u2015/]+/g;
function Ow(e, t) {
	if (t.hyphens === "auto" || /[　-鿿가-힯฀-๿]/.test(e.textContent)) return [];
	let n = [], r = document.createRange();
	for (let t of e.textContent.matchAll(Dw)) {
		if (t[0].length < 2) continue;
		r.setStart(e, t.index), r.setEnd(e, t.index + t[0].length);
		let i = [...r.getClientRects()].filter((e) => e.width > 0 && e.height > 0);
		if (i.length < 2) continue;
		let a = i.map((e) => e.top);
		Math.max(...a) - Math.min(...a) > Math.min(...i.map((e) => e.height)) / 2 && n.push(t[0]);
	}
	return n;
}
var kw = .2;
function Aw(e) {
	let t = 1;
	for (let n = e; n; n = mw(n)) t *= parseFloat(getComputedStyle(n).opacity);
	return t;
}
function jw(e, t) {
	for (let n = t; n; n = mw(n)) if (n === e) return !0;
	return !1;
}
function Mw(e, t, n) {
	let r = document.createRange();
	for (let i of e.childNodes) if (i.nodeType === Node.TEXT_NODE && i.textContent.trim()) {
		r.selectNodeContents(i);
		for (let e of r.getClientRects()) if (t >= e.left && t <= e.right && n >= e.top && n <= e.bottom) return !0;
	}
	return !1;
}
function Nw(e, t, n) {
	let r = getComputedStyle(e);
	return r.visibility !== "visible" || Aw(e) < .9 ? !1 : /^(img|svg|canvas|video|picture)$/i.test(e.localName) || r.backgroundImage.includes("url(") || Tw(r.backgroundColor).a >= .9 || Mw(e, t, n) ? !0 : !!e.shadowRoot?.elementsFromPoint(t, n).some((r) => r !== e && e.shadowRoot.contains(r) && Nw(r, t, n));
}
var Pw = (e) => /fixed|sticky/.test(getComputedStyle(e).position);
function Fw(e) {
	for (let t = e; t && t !== document.documentElement && t !== document.body; t = mw(t)) {
		if (Pw(t)) return null;
		let e = getComputedStyle(t);
		if (/auto|scroll/.test(e.overflowY) && t.scrollHeight > t.clientHeight + 1) return t;
	}
	return document.documentElement.scrollHeight > innerHeight + 1 ? document.documentElement : null;
}
function Iw(e, t) {
	for (let n = t; n; n = mw(n)) {
		if (n === e) return !1;
		if (Pw(n)) return !0;
	}
	return e !== document.documentElement;
}
function Lw(e, t, n) {
	let r = Fw(e);
	if (!r || !Iw(r, t)) return !1;
	let i = r === document.documentElement, a = i ? innerHeight : r.clientHeight, o = t.getBoundingClientRect();
	if (o.height >= a - 1) return !1;
	let s = r.scrollHeight - a - r.scrollTop, c = r.scrollTop, l = i ? { top: 0 } : r.getBoundingClientRect();
	return (o.top + o.bottom) / 2 >= l.top + a / 2 ? s > 0 && n.bottom - s <= o.top : c > 0 && n.top + c >= o.bottom;
}
function Rw(e, t, n = () => !1) {
	let r = document.createRange();
	r.selectNodeContents(e);
	let i = [...r.getClientRects()].filter((e) => e.width > 0 && e.height > 0), a = Math.max(1, Math.ceil(i.length / 8)), o = 0, s = 0, c = null;
	for (let e of i.filter((e, t) => t % a === 0)) for (let r of [
		.1,
		.3,
		.5,
		.7,
		.9
	]) {
		let i = e.left + e.width * r, a = e.top + e.height / 2;
		if (i < 0 || a < 0 || i >= innerWidth || a >= innerHeight) continue;
		let l = document.elementsFromPoint(i, a), u = l.findIndex((e) => jw(e, t));
		if (u < 0) continue;
		o++;
		let d = l.slice(0, u).find((r) => !jw(t, r) && !gw(r) && Nw(r, i, a) && !Lw(t, r, e) && !n(r));
		d && (s++, c ??= d);
	}
	return o > 0 && s / o >= kw ? {
		by: c,
		share: Math.round(s / o * 100)
	} : null;
}
var zw = "button, [role=button], m3e-button, m3e-icon-button, m3e-fab, m3e-fab-menu-trigger, m3e-split-button, m3e-chip, m3e-assist-chip, m3e-filter-chip, m3e-input-chip, m3e-suggestion-chip, m3e-button-group";
function Bw(e) {
	let t = {
		x: !1,
		y: !1
	};
	for (let n = e; n; n = mw(n)) if (n.matches?.(zw)) return t;
	let n = getComputedStyle(e), r = n.getPropertyValue("-webkit-line-clamp") || n.getPropertyValue("line-clamp");
	return {
		x: /hidden|clip/.test(n.overflowX) && n.textOverflow.includes("ellipsis"),
		y: /hidden|clip/.test(n.overflowY) && r !== "" && r !== "none"
	};
}
function Vw(e, t) {
	for (let n = hw(t); n; n = hw(mw(n))) {
		let t = getComputedStyle(n);
		if (/auto|scroll/.test(t.overflowX + t.overflowY)) return null;
		if (/hidden|clip/.test(t.overflowX + t.overflowY) && n !== document.documentElement) {
			let t = Bw(n), r = n.getBoundingClientRect(), [i, a] = [r.left + n.clientLeft, r.top + n.clientTop];
			if (!t.x && (e.left < i - 1 || e.right > i + n.clientWidth + 1) || e.top < a - 1 || !t.y && e.bottom > a + n.clientHeight + 1) return n;
		}
	}
	return null;
}
function Hw() {
	let e = innerWidth, t = Math.max(document.documentElement.scrollWidth, document.body.scrollWidth), n = (e) => {
		let t = 0;
		for (let n = mw(e); n; n = mw(n)) t++;
		return t;
	}, r = (t) => {
		for (let n = mw(t); n; n = mw(n)) {
			if (n === document.documentElement || n === document.body) return !1;
			let t = getComputedStyle(n);
			if (/auto|scroll/.test(t.overflowX) && n.getBoundingClientRect().width <= e + 1) return !0;
		}
		return !1;
	}, i = t > e + 1, a = [];
	return xw(document, (t) => {
		let o = getComputedStyle(t);
		if (o.display === "none" || o.display === "contents") return;
		let s = o.position === "fixed" || o.position === "sticky";
		if (!s && !i) return;
		let { left: c, right: l } = t.getBoundingClientRect();
		s ? (c < -1 && l > 1 || l > e + 1 && c < e - 1) && pw(t) && !gw(t) && !r(t) && a.push({
			el: fw(t),
			right: Math.round(l),
			left: Math.round(c),
			depth: n(t)
		}) : l > e + 1 && !r(t) && a.push({
			el: fw(t),
			right: Math.round(l),
			depth: n(t)
		});
	}), a.sort((e, t) => t.right - e.right || e.depth - t.depth), {
		fails: a.length > 0,
		scrollWidth: t,
		offenders: a.slice(0, 5).map(({ depth: e, ...t }) => t)
	};
}
async function Uw() {
	let e = () => new Promise((e) => requestAnimationFrame(() => requestAnimationFrame(e))), t = (e = document) => {
		if (e.getAnimations().some((e) => e.playState === "running" && Number.isFinite(e.effect?.getComputedTiming().endTime))) return !0;
		for (let n of e.querySelectorAll("*")) if (n.shadowRoot && t(n.shadowRoot)) return !0;
		return !1;
	};
	await e();
	for (let n = 0; n < 90 && t(); n++) await e();
}
function Ww() {
	let e = [], t = vw(), n = bw(), r = (e) => t.length > 0 && !t.some((t) => jw(t, e)), i = (e) => (t) => n.some((e) => jw(e, t)) && !n.some((t) => jw(t, e)), a = [];
	xw(document, (e) => {
		if (!e.matches(ow) || e.localName === "m3e-slider-thumb") return !0;
		if (pw(e) && !yw(e)) {
			let t = (e.closest("m3e-form-field") ?? e).getBoundingClientRect(), n = (e.shadowRoot?.querySelector(".touch") ?? e.querySelector(":scope > .handle-touch"))?.getBoundingClientRect(), i = Math.max(t.width, n?.width ?? 0), o = Math.max(t.height, n?.height ?? 0), s = (e.shadowRoot?.querySelector(".base") ?? e).getBoundingClientRect();
			a.push({
				el: uw(e),
				node: e,
				behind: r(e),
				w: Math.round(i * 10) / 10,
				h: Math.round(o * 10) / 10,
				vw: Math.round(s.width * 10) / 10,
				vh: Math.round(s.height * 10) / 10
			});
		}
		return !1;
	});
	let o = a.filter((e) => !e.behind && (e.w < 47.95 || e.h < 47.95)).map(({ el: e, w: t, h: n }) => ({
		el: e,
		w: t,
		h: n
	})), s = (e) => {
		for (let t = e; t; t = mw(t)) if (t.matches?.("m3e-nav-bar, m3e-nav-rail, m3e-nav-menu, m3e-nav-item, m3e-drawer-container > [slot=start], m3e-drawer-container > [slot=end], nav, [role=navigation], p-floating-toolbar m3e-button[toggle]") || t.matches?.("m3e-slider, input[type=range], [role=slider], [role=progressbar], m3e-form-field")) return !0;
		return !1;
	}, c = (e) => e.matches("m3e-button, m3e-icon-button, m3e-fab, m3e-fab-menu-trigger, m3e-split-button, m3e-chip, m3e-assist-chip, m3e-filter-chip, m3e-input-chip, m3e-suggestion-chip") || e.matches("button, [role=button]") && !e.closest("m3e-list-item, m3e-card, li") && !/^(link|listitem|row|option)$/.test(e.getAttribute("role") ?? ""), l = a.filter((e) => !s(e.node) && c(e.node)).reduce((e, t) => !e || t.vh > e.vh ? t : e, null), u = a.filter((e) => e.node.hasAttribute("data-primary")), d = {
		marked: u.length,
		largest: l && {
			el: l.el,
			w: l.vw,
			h: l.vh
		}
	};
	t.length ? (d.status = "skipped", d.reason = "a modal is open, so the page behind it is inert; the default views check the primary action") : u.length === 0 ? (d.status = "unresolved", e.push({
		check: "primary",
		reason: "no visible element has data-primary"
	})) : u.length > 1 ? (d.status = "fail", d.reason = "more than one visible element has data-primary") : (d.status = !l || u[0].vh >= l.vh - 1 ? "pass" : "fail", d.status === "fail" && (d.reason = `${u[0].el} (${u[0].vh}px tall) is shorter than ${l.el} (${l.vh}px tall)`));
	let f = [...document.querySelectorAll(sw)].filter((e) => !e.parentElement?.closest("m3e-fab-menu") && pw(e)).length, p = [], m = [], h = [], ee = [], te = {
		fails: [],
		passed: 0
	}, ne = /* @__PURE__ */ new Set(), g = (...e) => {
		let t = e.join("|");
		return !ne.has(t) && (ne.add(t), !0);
	};
	Sw(document, (t) => {
		let n = mw(t);
		if (!n || n.closest?.("p-icon, m3e-icon") || yw(n)) return;
		let a = getComputedStyle(n), o = document.createRange();
		o.selectNodeContents(t);
		let s = o.getBoundingClientRect();
		if (s.width <= 0 || s.height <= 0 || a.visibility !== "visible" || gw(n)) return;
		let c = fw(hw(n) || n);
		if (!cw(a.fontFamily, parseFloat(a.fontSize)) && g("font", c) && p.push({
			el: c,
			font: a.fontFamily
		}), _w(n)) return;
		let l = Ow(t, a);
		l.length && g("midword", c) && h.push({
			el: c,
			words: l.slice(0, 3)
		});
		let u = null, d = r(n);
		try {
			u = d ? null : Rw(t, n, i(n));
		} catch (t) {
			g("overlap-error", c) && e.push({
				check: "overlaps",
				el: c,
				reason: String(t)
			});
		}
		u && g("overlap", c) && ee.push({
			el: c,
			coveredBy: fw(u.by),
			share: u.share
		});
		let f = hw(n), ne = /auto|scroll/.test(a.overflowX + a.overflowY);
		f && !ne && getComputedStyle(f).display !== "inline" && (!Bw(f).x && f.scrollWidth > f.clientWidth + 1 || !Bw(f).y && f.scrollHeight > f.clientHeight + 1) && g("overflow", c) && m.push({
			el: c,
			scroll: [f.scrollWidth, f.scrollHeight],
			client: [f.clientWidth, f.clientHeight]
		});
		let re = Vw(s, n);
		if (re && re !== f && g("overflow", c, "clip") && m.push({
			el: c,
			clippedBy: fw(re)
		}), d || !g("contrast", c)) return;
		let _ = Ew(n);
		if (!_.color) {
			e.push({
				check: "contrast",
				el: c,
				reason: _.reason
			});
			return;
		}
		let ie = iw(Tw(a.color), _.color), v = aw(parseFloat(a.fontSize), parseInt(a.fontWeight, 10)), ae = rw(ie, _.color);
		ae < v ? te.fails.push({
			el: c,
			ratio: Math.round(ae * 100) / 100,
			need: v
		}) : te.passed++;
	});
	let re = Hw(), _ = e.length === 0, ie = !re.fails && o.length === 0 && p.length === 0 && m.length === 0 && h.length === 0 && ee.length === 0 && d.status !== "fail" && f <= 1 && te.fails.length === 0;
	return JSON.parse(JSON.stringify({
		pass: ie,
		complete: _,
		viewport: [innerWidth, innerHeight],
		touchTargets: {
			checked: a.length,
			under48: o
		},
		fonts: { offenders: p },
		overflow: m,
		midWordBreaks: h,
		overlaps: ee,
		pageOverflow: re,
		primary: d,
		modal: { open: t.length },
		fabs: {
			count: f,
			ok: f <= 1
		},
		contrast: te,
		unresolved: e
	}));
}
window.runtimeChecks = Ww, window.runtimeSettle = Uw;
//#endregion
