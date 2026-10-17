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
})(e) : e, { is: f, defineProperty: p, getOwnPropertyDescriptor: m, getOwnPropertyNames: h, getOwnPropertySymbols: g, getPrototypeOf: ee } = Object, te = globalThis, _ = te.trustedTypes, ne = _ ? _.emptyScript : "", v = te.reactiveElementPolyfillSupport, re = (e, t) => e, y = {
	toAttribute(e, t) {
		switch (t) {
			case Boolean:
				e = e ? ne : null;
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
}, ie = (e, t) => !f(e, t), ae = {
	attribute: !0,
	type: String,
	converter: y,
	reflect: !1,
	useDefault: !1,
	hasChanged: ie
};
Symbol.metadata ??= Symbol("metadata"), te.litPropertyMetadata ??= /* @__PURE__ */ new WeakMap();
var oe = class extends HTMLElement {
	static addInitializer(e) {
		this._$Ei(), (this.l ??= []).push(e);
	}
	static get observedAttributes() {
		return this.finalize(), this._$Eh && [...this._$Eh.keys()];
	}
	static createProperty(e, t = ae) {
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
		return this.elementProperties.get(e) ?? ae;
	}
	static _$Ei() {
		if (this.hasOwnProperty(re("elementProperties"))) return;
		let e = ee(this);
		e.finalize(), e.l !== void 0 && (this.l = [...e.l]), this.elementProperties = new Map(e.elementProperties);
	}
	static finalize() {
		if (this.hasOwnProperty(re("finalized"))) return;
		if (this.finalized = !0, this._$Ei(), this.hasOwnProperty(re("properties"))) {
			let e = this.properties, t = [...h(e), ...g(e)];
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
			let i = (n.converter?.toAttribute === void 0 ? y : n.converter).toAttribute(t, n.type);
			this._$Em = e, i == null ? this.removeAttribute(r) : this.setAttribute(r, i), this._$Em = null;
		}
	}
	_$AK(e, t) {
		let n = this.constructor, r = n._$Eh.get(e);
		if (r !== void 0 && this._$Em !== r) {
			let e = n.getPropertyOptions(r), i = typeof e.converter == "function" ? { fromAttribute: e.converter } : e.converter?.fromAttribute === void 0 ? y : e.converter;
			this._$Em = r;
			let a = i.fromAttribute(t, e.type);
			this[r] = a ?? this._$Ej?.get(r) ?? a, this._$Em = null;
		}
	}
	requestUpdate(e, t, n, r = !1, i) {
		if (e !== void 0) {
			let a = this.constructor;
			if (!1 === r && (i = this[e]), n ??= a.getPropertyOptions(e), !((n.hasChanged ?? ie)(i, t) || n.useDefault && n.reflect && i === this._$Ej?.get(e) && !this.hasAttribute(a._$Eu(e, n)))) return;
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
oe.elementStyles = [], oe.shadowRootOptions = { mode: "open" }, oe[re("elementProperties")] = /* @__PURE__ */ new Map(), oe[re("finalized")] = /* @__PURE__ */ new Map(), v?.({ ReactiveElement: oe }), (te.reactiveElementVersions ??= []).push("2.1.2");
//#endregion
//#region node_modules/lit-html/lit-html.js
var se = globalThis, ce = (e) => e, le = se.trustedTypes, ue = le ? le.createPolicy("lit-html", { createHTML: (e) => e }) : void 0, de = "$lit$", fe = `lit$${Math.random().toFixed(9).slice(2)}$`, pe = "?" + fe, me = `<${pe}>`, he = document, ge = () => he.createComment(""), _e = (e) => e === null || typeof e != "object" && typeof e != "function", ve = Array.isArray, ye = (e) => ve(e) || typeof e?.[Symbol.iterator] == "function", be = "[ 	\n\f\r]", xe = /<(?:(!--|\/[^a-zA-Z])|(\/?[a-zA-Z][^>\s]*)|(\/?$))/g, Se = /-->/g, Ce = />/g, we = RegExp(`>|${be}(?:([^\\s"'>=/]+)(${be}*=${be}*(?:[^ \t\n\f\r"'\`<>=]|("|')|))|$)`, "g"), Te = /'/g, Ee = /"/g, De = /^(?:script|style|textarea|title)$/i, Oe = (e) => (t, ...n) => ({
	_$litType$: e,
	strings: t,
	values: n
}), b = Oe(1), ke = Oe(2), Ae = Symbol.for("lit-noChange"), x = Symbol.for("lit-nothing"), je = /* @__PURE__ */ new WeakMap(), Me = he.createTreeWalker(he, 129);
function Ne(e, t) {
	if (!ve(e) || !e.hasOwnProperty("raw")) throw Error("invalid template strings array");
	return ue === void 0 ? t : ue.createHTML(t);
}
var Pe = (e, t) => {
	let n = e.length - 1, r = [], i, a = t === 2 ? "<svg>" : t === 3 ? "<math>" : "", o = xe;
	for (let t = 0; t < n; t++) {
		let n = e[t], s, c, l = -1, u = 0;
		for (; u < n.length && (o.lastIndex = u, c = o.exec(n), c !== null);) u = o.lastIndex, o === xe ? c[1] === "!--" ? o = Se : c[1] === void 0 ? c[2] === void 0 ? c[3] !== void 0 && (o = we) : (De.test(c[2]) && (i = RegExp("</" + c[2], "g")), o = we) : o = Ce : o === we ? c[0] === ">" ? (o = i ?? xe, l = -1) : c[1] === void 0 ? l = -2 : (l = o.lastIndex - c[2].length, s = c[1], o = c[3] === void 0 ? we : c[3] === "\"" ? Ee : Te) : o === Ee || o === Te ? o = we : o === Se || o === Ce ? o = xe : (o = we, i = void 0);
		let d = o === we && e[t + 1].startsWith("/>") ? " " : "";
		a += o === xe ? n + me : l >= 0 ? (r.push(s), n.slice(0, l) + de + n.slice(l) + fe + d) : n + fe + (l === -2 ? t : d);
	}
	return [Ne(e, a + (e[n] || "<?>") + (t === 2 ? "</svg>" : t === 3 ? "</math>" : "")), r];
}, Fe = class e {
	constructor({ strings: t, _$litType$: n }, r) {
		let i;
		this.parts = [];
		let a = 0, o = 0, s = t.length - 1, c = this.parts, [l, u] = Pe(t, n);
		if (this.el = e.createElement(l, r), Me.currentNode = this.el.content, n === 2 || n === 3) {
			let e = this.el.content.firstChild;
			e.replaceWith(...e.childNodes);
		}
		for (; (i = Me.nextNode()) !== null && c.length < s;) {
			if (i.nodeType === 1) {
				if (i.hasAttributes()) for (let e of i.getAttributeNames()) if (e.endsWith(de)) {
					let t = u[o++], n = i.getAttribute(e).split(fe), r = /([.?@])?(.*)/.exec(t);
					c.push({
						type: 1,
						index: a,
						name: r[2],
						strings: n,
						ctor: r[1] === "." ? Be : r[1] === "?" ? Ve : r[1] === "@" ? He : ze
					}), i.removeAttribute(e);
				} else e.startsWith(fe) && (c.push({
					type: 6,
					index: a
				}), i.removeAttribute(e));
				if (De.test(i.tagName)) {
					let e = i.textContent.split(fe), t = e.length - 1;
					if (t > 0) {
						i.textContent = le ? le.emptyScript : "";
						for (let n = 0; n < t; n++) i.append(e[n], ge()), Me.nextNode(), c.push({
							type: 2,
							index: ++a
						});
						i.append(e[t], ge());
					}
				}
			} else if (i.nodeType === 8) {
				if (i.data === pe) c.push({
					type: 2,
					index: a
				});
				else {
					let e = -1;
					for (; (e = i.data.indexOf(fe, e + 1)) !== -1;) c.push({
						type: 7,
						index: a
					}), e += fe.length - 1;
				}
			}
			a++;
		}
	}
	static createElement(e, t) {
		let n = he.createElement("template");
		return n.innerHTML = e, n;
	}
};
function Ie(e, t, n = e, r) {
	if (t === Ae) return t;
	let i = r === void 0 ? n._$Cl : n._$Co?.[r], a = _e(t) ? void 0 : t._$litDirective$;
	return i?.constructor !== a && (i?._$AO?.(!1), a === void 0 ? i = void 0 : (i = new a(e), i._$AT(e, n, r)), r === void 0 ? n._$Cl = i : (n._$Co ??= [])[r] = i), i !== void 0 && (t = Ie(e, i._$AS(e, t.values), i, r)), t;
}
var Le = class {
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
		let { el: { content: t }, parts: n } = this._$AD, r = (e?.creationScope ?? he).importNode(t, !0);
		Me.currentNode = r;
		let i = Me.nextNode(), a = 0, o = 0, s = n[0];
		for (; s !== void 0;) {
			if (a === s.index) {
				let t;
				s.type === 2 ? t = new Re(i, i.nextSibling, this, e) : s.type === 1 ? t = new s.ctor(i, s.name, s.strings, this, e) : s.type === 6 && (t = new Ue(i, this, e)), this._$AV.push(t), s = n[++o];
			}
			a !== s?.index && (i = Me.nextNode(), a++);
		}
		return Me.currentNode = he, r;
	}
	p(e) {
		let t = 0;
		for (let n of this._$AV) n !== void 0 && (n.strings === void 0 ? n._$AI(e[t]) : (n._$AI(e, n, t), t += n.strings.length - 2)), t++;
	}
}, Re = class e {
	get _$AU() {
		return this._$AM?._$AU ?? this._$Cv;
	}
	constructor(e, t, n, r) {
		this.type = 2, this._$AH = x, this._$AN = void 0, this._$AA = e, this._$AB = t, this._$AM = n, this.options = r, this._$Cv = r?.isConnected ?? !0;
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
		e = Ie(this, e, t), _e(e) ? e === x || e == null || e === "" ? (this._$AH !== x && this._$AR(), this._$AH = x) : e !== this._$AH && e !== Ae && this._(e) : e._$litType$ === void 0 ? e.nodeType === void 0 ? ye(e) ? this.k(e) : this._(e) : this.T(e) : this.$(e);
	}
	O(e) {
		return this._$AA.parentNode.insertBefore(e, this._$AB);
	}
	T(e) {
		this._$AH !== e && (this._$AR(), this._$AH = this.O(e));
	}
	_(e) {
		this._$AH !== x && _e(this._$AH) ? this._$AA.nextSibling.data = e : this.T(he.createTextNode(e)), this._$AH = e;
	}
	$(e) {
		let { values: t, _$litType$: n } = e, r = typeof n == "number" ? this._$AC(e) : (n.el === void 0 && (n.el = Fe.createElement(Ne(n.h, n.h[0]), this.options)), n);
		if (this._$AH?._$AD === r) this._$AH.p(t);
		else {
			let e = new Le(r, this), n = e.u(this.options);
			e.p(t), this.T(n), this._$AH = e;
		}
	}
	_$AC(e) {
		let t = je.get(e.strings);
		return t === void 0 && je.set(e.strings, t = new Fe(e)), t;
	}
	k(t) {
		ve(this._$AH) || (this._$AH = [], this._$AR());
		let n = this._$AH, r, i = 0;
		for (let a of t) i === n.length ? n.push(r = new e(this.O(ge()), this.O(ge()), this, this.options)) : r = n[i], r._$AI(a), i++;
		i < n.length && (this._$AR(r && r._$AB.nextSibling, i), n.length = i);
	}
	_$AR(e = this._$AA.nextSibling, t) {
		for (this._$AP?.(!1, !0, t); e !== this._$AB;) {
			let t = ce(e).nextSibling;
			ce(e).remove(), e = t;
		}
	}
	setConnected(e) {
		this._$AM === void 0 && (this._$Cv = e, this._$AP?.(e));
	}
}, ze = class {
	get tagName() {
		return this.element.tagName;
	}
	get _$AU() {
		return this._$AM._$AU;
	}
	constructor(e, t, n, r, i) {
		this.type = 1, this._$AH = x, this._$AN = void 0, this.element = e, this.name = t, this._$AM = r, this.options = i, n.length > 2 || n[0] !== "" || n[1] !== "" ? (this._$AH = Array(n.length - 1).fill(/* @__PURE__ */ new String()), this.strings = n) : this._$AH = x;
	}
	_$AI(e, t = this, n, r) {
		let i = this.strings, a = !1;
		if (i === void 0) e = Ie(this, e, t, 0), a = !_e(e) || e !== this._$AH && e !== Ae, a && (this._$AH = e);
		else {
			let r = e, o, s;
			for (e = i[0], o = 0; o < i.length - 1; o++) s = Ie(this, r[n + o], t, o), s === Ae && (s = this._$AH[o]), a ||= !_e(s) || s !== this._$AH[o], s === x ? e = x : e !== x && (e += (s ?? "") + i[o + 1]), this._$AH[o] = s;
		}
		a && !r && this.j(e);
	}
	j(e) {
		e === x ? this.element.removeAttribute(this.name) : this.element.setAttribute(this.name, e ?? "");
	}
}, Be = class extends ze {
	constructor() {
		super(...arguments), this.type = 3;
	}
	j(e) {
		this.element[this.name] = e === x ? void 0 : e;
	}
}, Ve = class extends ze {
	constructor() {
		super(...arguments), this.type = 4;
	}
	j(e) {
		this.element.toggleAttribute(this.name, !!e && e !== x);
	}
}, He = class extends ze {
	constructor(e, t, n, r, i) {
		super(e, t, n, r, i), this.type = 5;
	}
	_$AI(e, t = this) {
		if ((e = Ie(this, e, t, 0) ?? x) === Ae) return;
		let n = this._$AH, r = e === x && n !== x || e.capture !== n.capture || e.once !== n.once || e.passive !== n.passive, i = e !== x && (n === x || r);
		r && this.element.removeEventListener(this.name, this, n), i && this.element.addEventListener(this.name, this, e), this._$AH = e;
	}
	handleEvent(e) {
		typeof this._$AH == "function" ? this._$AH.call(this.options?.host ?? this.element, e) : this._$AH.handleEvent(e);
	}
}, Ue = class {
	constructor(e, t, n) {
		this.element = e, this.type = 6, this._$AN = void 0, this._$AM = t, this.options = n;
	}
	get _$AU() {
		return this._$AM._$AU;
	}
	_$AI(e) {
		Ie(this, e);
	}
}, We = se.litHtmlPolyfillSupport;
We?.(Fe, Re), (se.litHtmlVersions ??= []).push("3.3.3");
var Ge = (e, t, n) => {
	let r = n?.renderBefore ?? t, i = r._$litPart$;
	if (i === void 0) {
		let e = n?.renderBefore ?? null;
		r._$litPart$ = i = new Re(t.insertBefore(ge(), e), e, void 0, n ?? {});
	}
	return i._$AI(e), i;
}, Ke = globalThis, S = class extends oe {
	constructor() {
		super(...arguments), this.renderOptions = { host: this }, this._$Do = void 0;
	}
	createRenderRoot() {
		let e = super.createRenderRoot();
		return this.renderOptions.renderBefore ??= e.firstChild, e;
	}
	update(e) {
		let t = this.render();
		this.hasUpdated || (this.renderOptions.isConnected = this.isConnected), super.update(e), this._$Do = Ge(t, this.renderRoot, this.renderOptions);
	}
	connectedCallback() {
		super.connectedCallback(), this._$Do?.setConnected(!0);
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this._$Do?.setConnected(!1);
	}
	render() {
		return Ae;
	}
};
S._$litElement$ = !0, S.finalized = !0, Ke.litElementHydrateSupport?.({ LitElement: S });
var qe = Ke.litElementPolyfillSupport;
qe?.({ LitElement: S }), (Ke.litElementVersions ??= []).push("4.2.2");
//#endregion
//#region node_modules/@lit/reactive-element/decorators/property.js
var Je = {
	attribute: !0,
	type: String,
	converter: y,
	reflect: !1,
	hasChanged: ie
}, Ye = (e = Je, t, n) => {
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
function C(e) {
	return (t, n) => typeof n == "object" ? Ye(e, t, n) : ((e, t, n) => {
		let r = t.hasOwnProperty(n);
		return t.constructor.createProperty(n, e), r ? Object.getOwnPropertyDescriptor(t, n) : void 0;
	})(e, t, n);
}
//#endregion
//#region node_modules/@lit/reactive-element/decorators/state.js
function Xe(e) {
	return C({
		...e,
		state: !0,
		attribute: !1
	});
}
//#endregion
//#region node_modules/@lit/reactive-element/decorators/base.js
var Ze = (e, t, n) => (n.configurable = !0, n.enumerable = !0, Reflect.decorate && typeof t != "object" && Object.defineProperty(e, t, n), n);
//#endregion
//#region node_modules/@lit/reactive-element/decorators/query.js
function w(e, t) {
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
			return Ze(n, r, { get() {
				let n = e.call(this);
				return n === void 0 && (n = a(this), (n !== null || this.hasUpdated) && t.call(this, n)), n;
			} });
		}
		return Ze(n, r, { get() {
			return a(this);
		} });
	};
}
//#endregion
//#region node_modules/@lit/reactive-element/decorators/query-assigned-elements.js
function Qe(e) {
	return (t, n) => {
		let { slot: r, selector: i } = e ?? {}, a = "slot" + (r ? `[name=${r}]` : ":not([name])");
		return Ze(t, n, { get() {
			let t = (this.renderRoot?.querySelector(a))?.assignedElements(e) ?? [];
			return i === void 0 ? t : t.filter((e) => e.matches(i));
		} });
	};
}
//#endregion
//#region node_modules/lit-html/directive.js
var $e = {
	ATTRIBUTE: 1,
	CHILD: 2,
	PROPERTY: 3,
	BOOLEAN_ATTRIBUTE: 4,
	EVENT: 5,
	ELEMENT: 6
}, et = (e) => (...t) => ({
	_$litDirective$: e,
	values: t
}), tt = class {
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
}, nt = (e) => e ?? x, rt, it, at, ot, st, ct = class {
	constructor(e, r) {
		rt.set(this, null), it.set(this, 0), at.set(this, !1), ot.set(this, void 0), st.set(this, () => {
			if (!t(this, at, "f")) return;
			let e = performance.now();
			t(this, ot, "f").call(this, (e - t(this, it, "f")) / 1e3, e / 1e3), n(this, it, e, "f"), n(this, rt, requestAnimationFrame(t(this, st, "f")), "f");
		}), n(this, ot, r, "f"), e.addController(this);
	}
	hostDisconnected() {
		this.stop();
	}
	start() {
		t(this, at, "f") || (n(this, at, !0, "f"), n(this, it, performance.now(), "f"), t(this, st, "f").call(this));
	}
	stop() {
		t(this, at, "f") && (n(this, at, !1, "f"), t(this, rt, "f") !== null && (cancelAnimationFrame(t(this, rt, "f")), n(this, rt, null, "f")));
	}
};
rt = /* @__PURE__ */ new WeakMap(), it = /* @__PURE__ */ new WeakMap(), at = /* @__PURE__ */ new WeakMap(), ot = /* @__PURE__ */ new WeakMap(), st = /* @__PURE__ */ new WeakMap();
var lt, ut, dt, ft = class {
	constructor(e, r) {
		lt.set(this, void 0), ut.set(this, void 0), dt.set(this, /* @__PURE__ */ new Set()), n(this, lt, e, "f"), n(this, ut, r.target, "f"), t(this, lt, "f").addController(this);
	}
	get targets() {
		return t(this, dt, "f").values();
	}
	get hasTargets() {
		return t(this, dt, "f").size > 0;
	}
	hostConnected() {
		t(this, ut, "f") !== null && this.observe(t(this, ut, "f") ?? t(this, lt, "f"));
	}
	hostDisconnected() {
		this.unobserveAll();
	}
	observe(e) {
		t(this, dt, "f").has(e) || (t(this, dt, "f").add(e), this._observe(e));
	}
	isObserving(e) {
		return t(this, dt, "f").has(e);
	}
	unobserve(e) {
		t(this, dt, "f").delete(e) && this._unobserve(e);
	}
	unobserveAll() {
		t(this, dt, "f").forEach((e) => this.unobserve(e)), t(this, dt, "f").clear();
	}
};
lt = /* @__PURE__ */ new WeakMap(), ut = /* @__PURE__ */ new WeakMap(), dt = /* @__PURE__ */ new WeakMap();
var pt, mt, ht, gt, _t, vt = class extends ft {
	constructor(e, r) {
		super(e, r), pt.add(this), mt.set(this, void 0), ht.set(this, (e) => t(this, pt, "m", _t).call(this, e)), gt.set(this, !1), n(this, mt, r.callback, "f");
	}
	_observe() {
		t(this, gt, "f") || (document.addEventListener("click", t(this, ht, "f")), n(this, gt, !0, "f"));
	}
	_unobserve() {
		!this.hasTargets && t(this, gt, "f") && (document.removeEventListener("click", t(this, ht, "f")), n(this, gt, !1, "f"));
	}
};
mt = /* @__PURE__ */ new WeakMap(), ht = /* @__PURE__ */ new WeakMap(), gt = /* @__PURE__ */ new WeakMap(), pt = /* @__PURE__ */ new WeakSet(), _t = function(e) {
	let n = e.composedPath();
	n.some((e) => e instanceof HTMLElement && this.isObserving(e)) || t(this, mt, "f").call(this, n);
};
function yt(e) {
	let t = parseFloat(getComputedStyle(e).lineHeight);
	return isNaN(t) || t <= 0 ? 0 : Math.round(e.scrollHeight / t);
}
function bt() {
	return matchMedia("(forced-colors: active)").matches;
}
function xt(e, t, n) {
	return e + (t - e) * n;
}
function St(e, t, n, r) {
	return {
		x: (1 - r) ** 2 * e.x + 2 * (1 - r) * r * t.x + r ** 2 * n.x,
		y: (1 - r) ** 2 * e.y + 2 * (1 - r) * r * t.y + r ** 2 * n.y
	};
}
function Ct(e, t, n, r, i) {
	return {
		x: (1 - i) ** 3 * e.x + 3 * (1 - i) ** 2 * i * t.x + 3 * (1 - i) * i ** 2 * n.x + i ** 3 * r.x,
		y: (1 - i) ** 3 * e.y + 3 * (1 - i) ** 2 * i * t.y + 3 * (1 - i) * i ** 2 * n.y + i ** 3 * r.y
	};
}
function wt(e) {
	return (e.match(/[a-zA-Z][^a-zA-Z]*/g) || []).map((e) => ({
		type: e[0],
		nums: e.slice(1).trim().split(/[\s,]+/).filter(Boolean).map(Number)
	}));
}
function Tt(e, t) {
	let n = wt(e), r = {
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
			let a = e / 20, s = St(r, t, n, a);
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
			let a = e / 20, c = Ct(r, t, n, i, a);
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
				x: xt(e.x, t.x, r),
				y: xt(e.y, t.y, r)
			});
		} else if (n.type === "Q") {
			let [e, t, i] = n.pts;
			s.push(St(e, t, i, r));
		} else if (n.type === "C") {
			let [e, t, i, a] = n.pts;
			s.push(Ct(e, t, i, a, r));
		}
	}
	return s;
}
function Et(e) {
	return `${e.map((e) => {
		let t = Dt(e.x * 100, 0, 100), n = Dt(e.y * 100, 0, 100);
		return `${t.toFixed(2)}% ${n.toFixed(2)}%`;
	}).join(", ")}`;
}
function Dt(e, t, n) {
	return Math.max(t, Math.min(n, e));
}
function Ot(e) {
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
function kt(e, t) {
	let n = e.length, r = Array(n);
	for (let i = 0; i < n; i++) r[i] = e[(i + t) % n];
	return r;
}
function At(e, t) {
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
function jt(e) {
	let t = 0, n = e.length;
	for (let r = 0; r < n; r++) {
		let i = e[r], a = e[(r + 1) % n];
		t += i.x * a.y - a.x * i.y;
	}
	return t / 2;
}
function Mt(e) {
	let t = e.map((e) => {
		let t = Ot(e), n = e.map((e) => ({
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
	}), n = t[0], r = Math.sign(jt(n));
	for (let e = 1; e < t.length; e++) {
		let i = t[e], a = Math.sign(jt(i));
		a !== 0 && a !== r && (i = i.slice().reverse());
		let o = At(n, i);
		t[e] = kt(i, o);
	}
	return t;
}
function Nt(e, t) {
	return Mt(e.map((e) => Tt(e, t))).map((e) => Et(e));
}
function Pt(e, t = !1) {
	let n = "";
	switch (e.nodeType) {
		case Node.TEXT_NODE:
			n = e.nodeValue ?? "";
			break;
		case Node.ELEMENT_NODE: if (e instanceof HTMLSlotElement) for (let r of e.assignedNodes({ flatten: !0 })) n += Pt(r, t);
		else for (let r of e.childNodes) n += Pt(r, t);
	}
	return t && (n = n.trim()), n;
}
function Ft(e) {
	return e.assignedNodes({ flatten: !0 }).length > 0;
}
function It() {
	return matchMedia("(prefers-reduced-motion)").matches;
}
function Lt(e) {
	if (typeof window > "u") return;
	let t = new CSSStyleSheet();
	t.replaceSync(e.toString()), document.adoptedStyleSheets = [...document.adoptedStyleSheets, t];
}
function Rt(e, t = document) {
	return new Promise((n) => {
		let r = t.querySelector(`#${e}`);
		r ? n(r) : document.readyState === "complete" || document.readyState === "interactive" ? n(t.querySelector(`#${e}`)) : document.addEventListener("DOMContentLoaded", () => n(t.querySelector(`#${e}`)), { once: !0 });
	});
}
function zt(e) {
	let t = document?.location ?? null;
	return `url(${t ? (t.pathname + t.search).split("#")[0] : ""}#${e})`;
}
async function Bt(e) {
	e.isUpdatePending && await e.updateComplete;
}
async function Vt(e) {
	let t = e.tagName.toLowerCase();
	if (!t.includes("-")) return;
	customElements.get(t) || await customElements.whenDefined(t);
	let n = customElements.get(t);
	!n || e instanceof n || (await Promise.resolve(), !(e instanceof n) && await Promise.resolve());
}
var Ht, Ut, Wt, Gt, Kt, qt, Jt, Yt, Xt, Zt, Qt, $t, en, tn, nn = class extends ft {
	constructor(e, r) {
		super(e, r), Ht.add(this), Wt.set(this, !1), Gt.set(this, void 0), Kt.set(this, void 0), qt.set(this, (e) => t(this, Ht, "m", $t).call(this, e)), Jt.set(this, (e) => t(this, Ht, "m", en).call(this, e)), Yt.set(this, (e) => t(this, Ht, "m", tn).call(this, e)), Xt.set(this, () => n(this, Wt, !0, "f")), Zt.set(this, () => n(this, Wt, !1, "f")), n(this, Gt, r.callback, "f"), n(this, Kt, r.filter, "f");
	}
	_observe(e) {
		e.addEventListener("keydown", t(this, qt, "f")), e.addEventListener("focusin", t(this, Jt, "f")), e.addEventListener("focusout", t(this, Yt, "f")), e.addEventListener("touchstart", t(this, Xt, "f"), { passive: !0 }), e.addEventListener("touchend", t(this, Zt, "f")), e.addEventListener("touchcancel", t(this, Zt, "f"));
	}
	_unobserve(e) {
		e.removeEventListener("keydown", t(this, qt, "f")), e.removeEventListener("focusin", t(this, Jt, "f")), e.removeEventListener("focusout", t(this, Yt, "f")), e.removeEventListener("touchstart", t(this, Xt, "f")), e.removeEventListener("touchend", t(this, Zt, "f")), e.removeEventListener("touchcancel", t(this, Zt, "f"));
	}
};
Ut = nn, Wt = /* @__PURE__ */ new WeakMap(), Gt = /* @__PURE__ */ new WeakMap(), Kt = /* @__PURE__ */ new WeakMap(), qt = /* @__PURE__ */ new WeakMap(), Jt = /* @__PURE__ */ new WeakMap(), Yt = /* @__PURE__ */ new WeakMap(), Xt = /* @__PURE__ */ new WeakMap(), Zt = /* @__PURE__ */ new WeakMap(), Ht = /* @__PURE__ */ new WeakSet(), $t = function(e) {
	t(this, Kt, "f")?.call(this, e) || e.currentTarget.matches(":focus-within") && t(this, Ht, "m", en).call(this, e);
}, en = function(e) {
	if (t(this, Kt, "f")?.call(this, e) || t(this, Wt, "f")) return;
	let n = e.currentTarget;
	t(this, Gt, "f").call(this, !0, n.matches(":focus-visible") || t(Ut, Ut, "f", Qt) || bt(), n);
}, tn = function(e) {
	t(this, Kt, "f")?.call(this, e) || t(this, Wt, "f") || t(this, Gt, "f").call(this, !1, !1, e.currentTarget);
}, typeof window < "u" && (window.addEventListener("keydown", () => n(Ut, Ut, !0, "f", Qt), {
	capture: !0,
	passive: !0
}), window.addEventListener("pointerdown", () => n(Ut, Ut, !1, "f", Qt), { capture: !0 })), Qt = { value: !1 };
var rn, an, on, sn, cn, ln, un, dn, fn, pn, mn, hn = class extends ft {
	constructor(e, r) {
		super(e, r), rn.add(this), an.set(this, void 0), on.set(this, /* @__PURE__ */ new Map()), sn.set(this, /* @__PURE__ */ new Map()), cn.set(this, (e) => t(this, rn, "m", pn).call(this, e)), ln.set(this, (e) => t(this, rn, "m", mn).call(this, e)), n(this, an, r.callback, "f"), this.startDelay = r.startDelay ?? 0, this.endDelay = r.endDelay ?? 0;
	}
	clearDelays() {
		for (let e of this.targets) t(this, rn, "m", un).call(this, e);
	}
	_observe(e) {
		e.addEventListener("pointerenter", t(this, cn, "f")), e.addEventListener("pointerleave", t(this, ln, "f")), e.addEventListener("touchend", t(this, ln, "f"));
	}
	_unobserve(e) {
		e.removeEventListener("pointerenter", t(this, cn, "f")), e.removeEventListener("pointerleave", t(this, ln, "f")), e.removeEventListener("touchend", t(this, ln, "f")), t(this, rn, "m", un).call(this, e);
	}
};
an = /* @__PURE__ */ new WeakMap(), on = /* @__PURE__ */ new WeakMap(), sn = /* @__PURE__ */ new WeakMap(), cn = /* @__PURE__ */ new WeakMap(), ln = /* @__PURE__ */ new WeakMap(), rn = /* @__PURE__ */ new WeakSet(), un = function(e) {
	t(this, rn, "m", dn).call(this, e), t(this, rn, "m", fn).call(this, e);
}, dn = function(e) {
	return t(this, on, "f").has(e) ? (clearTimeout(t(this, on, "f").get(e)), t(this, on, "f").delete(e)) : !1;
}, fn = function(e) {
	return t(this, sn, "f").has(e) ? (clearTimeout(t(this, sn, "f").get(e)), t(this, sn, "f").delete(e)) : !1;
}, pn = function(e) {
	let n = e.target;
	t(this, rn, "m", fn).call(this, n), this.startDelay > 0 ? (t(this, rn, "m", dn).call(this, n), t(this, on, "f").set(n, setTimeout(() => {
		t(this, on, "f").delete(n), t(this, an, "f").call(this, !0, n);
	}, this.startDelay))) : t(this, an, "f").call(this, !0, n);
}, mn = function(e) {
	let n = e.target;
	t(this, rn, "m", dn).call(this, n) || (this.endDelay > 0 ? (t(this, rn, "m", fn).call(this, n), t(this, sn, "f").set(n, setTimeout(() => {
		t(this, sn, "f").delete(n), t(this, an, "f").call(this, !1, n);
	}, this.endDelay))) : t(this, an, "f").call(this, !1, n));
};
var gn, _n, vn, yn, bn, xn, Sn, Cn, wn, Tn, En, Dn, On = class extends ft {
	constructor(e, r) {
		super(e, r), gn.add(this), _n.set(this, void 0), vn.set(this, !1), yn.set(this, void 0), bn.set(this, void 0), xn.set(this, !0), Sn.set(this, /* @__PURE__ */ new Map()), Cn.set(this, /* @__PURE__ */ new Map()), n(this, _n, r.callback, "f"), n(this, vn, r.skipInitial ?? !1, "f"), n(this, yn, r.config, "f"), window.MutationObserver ? n(this, bn, new MutationObserver((e, n) => t(this, _n, "f").call(this, e, n)), "f") : console.warn("MutationController error: the browser does not support MutationObserver.");
	}
	async hostUpdated() {
		if (t(this, bn, "f") && !t(this, vn, "f") && t(this, xn, "f")) {
			let e = t(this, bn, "f").takeRecords();
			e.length > 0 && t(this, _n, "f").call(this, e, t(this, bn, "f"));
		}
		n(this, xn, !1, "f");
	}
	hostDisconnected() {
		super.hostDisconnected(), t(this, gn, "m", wn).call(this);
	}
	_observe(e) {
		e instanceof HTMLSlotElement ? t(this, gn, "m", Tn).call(this, e) : (t(this, bn, "f")?.observe(e, t(this, yn, "f")), n(this, xn, !0, "f"));
	}
	_unobserve() {
		t(this, gn, "m", wn).call(this);
		for (let e of this.targets) e instanceof HTMLSlotElement ? t(this, gn, "m", Tn).call(this, e) : t(this, bn, "f")?.observe(e, t(this, yn, "f"));
	}
};
_n = /* @__PURE__ */ new WeakMap(), vn = /* @__PURE__ */ new WeakMap(), yn = /* @__PURE__ */ new WeakMap(), bn = /* @__PURE__ */ new WeakMap(), xn = /* @__PURE__ */ new WeakMap(), Sn = /* @__PURE__ */ new WeakMap(), Cn = /* @__PURE__ */ new WeakMap(), gn = /* @__PURE__ */ new WeakSet(), wn = function() {
	t(this, bn, "f")?.disconnect();
	for (let [e] of t(this, Sn, "f")) t(this, gn, "m", En).call(this, e);
	t(this, Sn, "f").clear(), t(this, Cn, "f").clear();
}, Tn = function(e) {
	if (t(this, Sn, "f").has(e)) return;
	let n = () => t(this, gn, "m", Dn).call(this, e);
	e.addEventListener("slotchange", n), t(this, Sn, "f").set(e, () => e.removeEventListener("slotchange", n)), t(this, gn, "m", Dn).call(this, e);
}, En = function(e) {
	let n = t(this, Sn, "f").get(e);
	n && (n(), t(this, Sn, "f").delete(e)), t(this, Cn, "f").delete(e);
}, Dn = function(e) {
	t(this, bn, "f")?.disconnect();
	let r = e.assignedElements({ flatten: !0 });
	t(this, Cn, "f").set(e, r);
	for (let e of r) t(this, bn, "f")?.observe(e, t(this, yn, "f"));
	for (let e of this.targets) e instanceof HTMLSlotElement || t(this, bn, "f")?.observe(e, t(this, yn, "f"));
	n(this, xn, !0, "f");
};
var kn, An, jn, Mn, Nn, Pn, Fn, In, Ln, Rn, zn, Bn, Vn, Hn, Un, Wn, Gn, Kn, qn, Jn, Yn = class extends ft {
	constructor(e, r) {
		super(e, r), kn.add(this), An.set(this, void 0), jn.set(this, void 0), Mn.set(this, void 0), Nn.set(this, void 0), Pn.set(this, /* @__PURE__ */ new Map()), Fn.set(this, /* @__PURE__ */ new Map()), In.set(this, void 0), Ln.set(this, (e) => t(this, kn, "m", Hn).call(this, e)), Rn.set(this, (e) => t(this, kn, "m", Un).call(this, e)), zn.set(this, (e) => t(this, kn, "m", Wn).call(this, e)), Bn.set(this, (e) => t(this, kn, "m", Gn).call(this, e)), Vn.set(this, (e) => t(this, kn, "m", Kn).call(this, e)), n(this, An, r.capture, "f"), n(this, jn, r.callback, "f"), n(this, Mn, r.filter, "f"), n(this, Nn, r.isPressedKey, "f"), n(this, In, r.minPressedDuration ?? 0, "f");
	}
	hostConnected() {
		document.addEventListener("pointerup", t(this, Rn, "f"), { capture: t(this, An, "f") }), document.addEventListener("touchend", t(this, zn, "f"), { capture: t(this, An, "f") }), document.addEventListener("touchcancel", t(this, zn, "f"), { capture: t(this, An, "f") }), super.hostConnected();
	}
	hostDisconnected() {
		document.removeEventListener("pointerup", t(this, Rn, "f"), { capture: t(this, An, "f") }), document.removeEventListener("touchend", t(this, zn, "f"), { capture: t(this, An, "f") }), document.removeEventListener("touchcancel", t(this, zn, "f"), { capture: t(this, An, "f") }), super.hostDisconnected();
		for (let e of t(this, Fn, "f").values()) clearTimeout(e);
		t(this, Fn, "f").clear(), t(this, Pn, "f").clear();
	}
	_observe(e) {
		e.addEventListener("pointerdown", t(this, Ln, "f"), { capture: t(this, An, "f") }), t(this, Nn, "f") && (e.addEventListener("keydown", t(this, Bn, "f"), { capture: t(this, An, "f") }), e.addEventListener("keyup", t(this, Vn, "f"), { capture: t(this, An, "f") }));
	}
	_unobserve(e) {
		e.removeEventListener("pointerdown", t(this, Ln, "f"), { capture: t(this, An, "f") }), t(this, Nn, "f") && (e.removeEventListener("keydown", t(this, Bn, "f"), { capture: t(this, An, "f") }), e.removeEventListener("keyup", t(this, Vn, "f"), { capture: t(this, An, "f") })), t(this, kn, "m", Jn).call(this, e), t(this, Pn, "f").delete(e);
	}
};
An = /* @__PURE__ */ new WeakMap(), jn = /* @__PURE__ */ new WeakMap(), Mn = /* @__PURE__ */ new WeakMap(), Nn = /* @__PURE__ */ new WeakMap(), Pn = /* @__PURE__ */ new WeakMap(), Fn = /* @__PURE__ */ new WeakMap(), In = /* @__PURE__ */ new WeakMap(), Ln = /* @__PURE__ */ new WeakMap(), Rn = /* @__PURE__ */ new WeakMap(), zn = /* @__PURE__ */ new WeakMap(), Bn = /* @__PURE__ */ new WeakMap(), Vn = /* @__PURE__ */ new WeakMap(), kn = /* @__PURE__ */ new WeakSet(), Hn = function(e) {
	if (!t(this, Mn, "f")?.call(this, e) && !(e.pointerType === "mouse" && e.button > 1)) {
		for (let n of e.composedPath()) if (n instanceof HTMLElement && this.isObserving(n)) {
			t(this, kn, "m", Jn).call(this, n), t(this, Pn, "f").set(n, performance.now()), t(this, jn, "f").call(this, !0, {
				x: e.x,
				y: e.y
			}, n);
			break;
		}
	}
}, Un = function(e) {
	e.pointerType === "mouse" && e.button > 1 || t(this, kn, "m", qn).call(this, e.x, e.y);
}, Wn = function(e) {
	t(this, kn, "m", qn).call(this, e.changedTouches[0]?.clientX ?? 0, e.changedTouches[0]?.clientY ?? 0);
}, Gn = function(e) {
	if (t(this, Mn, "f")?.call(this, e) || e.target !== e.currentTarget) return;
	let n = e.currentTarget;
	if (t(this, Nn, "f")?.call(this, e.key) && (e.key === " " && e.preventDefault(), !e.repeat)) {
		t(this, kn, "m", Jn).call(this, n), t(this, Pn, "f").set(n, performance.now());
		let e = n.getBoundingClientRect();
		t(this, jn, "f").call(this, !0, {
			x: e.x + e.width / 2,
			y: e.y + e.height / 2
		}, n);
	}
}, Kn = function(e) {
	if (e.target !== e.currentTarget) return;
	let n = e.currentTarget;
	if (t(this, Pn, "f").has(n) && t(this, Nn, "f")?.call(this, e.key)) {
		t(this, kn, "m", Jn).call(this, n);
		let e = t(this, In, "f") - (performance.now() - t(this, Pn, "f").get(n)), r = n.getBoundingClientRect();
		e > 0 ? t(this, Fn, "f").set(n, setTimeout(() => {
			t(this, Pn, "f").delete(n), t(this, Fn, "f").delete(n), t(this, jn, "f").call(this, !1, {
				x: r.x + r.width / 2,
				y: r.y + r.height / 2
			}, n);
		}, e)) : (t(this, Pn, "f").delete(n), t(this, jn, "f").call(this, !1, {
			x: r.x + r.width / 2,
			y: r.y + r.height / 2
		}, n));
	}
}, qn = function(e, n) {
	for (let r of t(this, Pn, "f")) {
		t(this, kn, "m", Jn).call(this, r[0]);
		let i = t(this, In, "f") - (performance.now() - r[1]);
		i > 0 ? t(this, Fn, "f").set(r[0], setTimeout(() => {
			t(this, Pn, "f").delete(r[0]), t(this, Fn, "f").delete(r[0]), t(this, jn, "f").call(this, !1, {
				x: e,
				y: n
			}, r[0]);
		}, i)) : (t(this, Pn, "f").delete(r[0]), t(this, jn, "f").call(this, !1, {
			x: e,
			y: n
		}, r[0]));
	}
}, Jn = function(e) {
	let n = t(this, Fn, "f").get(e);
	n !== void 0 && (clearTimeout(n), t(this, Fn, "f").delete(e));
};
var Xn, Zn, Qn, $n, er, tr = class extends ft {
	constructor(e, r) {
		super(e, r), Xn.set(this, void 0), Zn.set(this, void 0), Qn.set(this, void 0), $n.set(this, void 0), er.set(this, !0), n(this, Xn, r.callback, "f"), n(this, Zn, r.skipInitial ?? !1, "f"), n(this, Qn, r.config, "f"), window.ResizeObserver ? n(this, $n, new ResizeObserver((e, n) => t(this, Xn, "f").call(this, e, n)), "f") : console.warn("ResizeController error: the browser does not support ResizeObserver.");
	}
	async hostUpdated() {
		t(this, $n, "f") && !t(this, Zn, "f") && t(this, er, "f") && t(this, Xn, "f").call(this, [], t(this, $n, "f")), n(this, er, !1, "f");
	}
	_observe(e) {
		t(this, $n, "f")?.observe(e, t(this, Qn, "f")), n(this, er, !0, "f");
	}
	_unobserve(e) {
		t(this, $n, "f")?.unobserve(e);
	}
};
Xn = /* @__PURE__ */ new WeakMap(), Zn = /* @__PURE__ */ new WeakMap(), Qn = /* @__PURE__ */ new WeakMap(), $n = /* @__PURE__ */ new WeakMap(), er = /* @__PURE__ */ new WeakMap();
var T = (e) => (t, n) => {
	let r = () => {
		typeof window < "u" && !customElements.get(e) && customElements.define(e, t);
	};
	n ? n.addInitializer(r) : r();
};
function nr(e) {
	let t = Symbol("_id");
	return (n, r, i) => {
		let a = i.value;
		return i.value = function(...n) {
			let r = this;
			clearTimeout(r[t]), r[t] = setTimeout(() => a.apply(this, n), e);
		}, i;
	};
}
var rr, ir, ar, or, sr, cr, lr, ur, dr, fr = class extends ft {
	constructor(e, r) {
		super(e, r), rr.add(this), ir.set(this, void 0), ar.set(this, void 0), or.set(this, (e) => t(this, rr, "m", dr).call(this, e)), sr.set(this, /* @__PURE__ */ new Map()), n(this, ir, r.debounce === !0, "f"), n(this, ar, r.callback, "f");
	}
	getScrollContainers(e) {
		return t(this, sr, "f").get(e);
	}
	_observe(e) {
		let n = t(this, rr, "m", ur).call(this, e);
		if (n.length > 0) {
			t(this, sr, "f").set(e, n);
			for (let e of n) (e === document.documentElement ? document : e).addEventListener("scroll", t(this, or, "f"), { passive: !0 });
		}
	}
	_unobserve(e) {
		if (t(this, sr, "f").has(e)) {
			for (let n of t(this, sr, "f").get(e)) (n === document.documentElement ? document : n).removeEventListener("scroll", t(this, or, "f"));
			t(this, sr, "f").delete(e);
		}
	}
	_debounceCallback(e) {
		t(this, ar, "f").call(this, e);
	}
};
ir = /* @__PURE__ */ new WeakMap(), ar = /* @__PURE__ */ new WeakMap(), or = /* @__PURE__ */ new WeakMap(), sr = /* @__PURE__ */ new WeakMap(), rr = /* @__PURE__ */ new WeakSet(), cr = function(e) {
	let n = e.shadowRoot;
	if (!n) return null;
	let r = document.createTreeWalker(n, NodeFilter.SHOW_ELEMENT), i = r.currentNode;
	for (; i;) {
		if (i instanceof Element && t(this, rr, "m", lr).call(this, i)) return i;
		i = r.nextNode();
	}
	return null;
}, lr = function(e) {
	let t = getComputedStyle(e);
	return /(auto|scroll)/.test(t.overflow + t.overflowY + t.overflowX);
}, ur = function(e) {
	let n = [], r = t(this, rr, "m", cr).call(this, e);
	r && n.push(r);
	let i = e;
	for (; i;) t(this, rr, "m", lr).call(this, i) && n.push(i), i = i.parentElement;
	return n;
}, dr = function(e) {
	let n = e.target === document ? document.documentElement : e.target;
	t(this, ir, "f") ? this._debounceCallback(n) : t(this, ar, "f").call(this, n);
}, e([nr(40)], fr.prototype, "_debounceCallback", null);
var pr = "important", mr = " !important", hr = -11, gr = et(class extends tt {
	constructor(e) {
		if (super(e), e.type !== $e.ATTRIBUTE || e.name !== "style" || e.strings?.length > 2) throw Error("The `styleMap` directive must be used in the `style` attribute and must be the only part in the attribute.");
	}
	render(e) {
		return x;
	}
	update(e, [t]) {
		let { style: n } = e.element;
		this._previousStyleProperties === void 0 && (this._previousStyleProperties = new Set(Object.keys(t)));
		for (let e of this._previousStyleProperties) t[e] ?? (this._previousStyleProperties.delete(e), e.includes("-") ? n.removeProperty(e) : n[e] = null);
		for (let e in t) {
			let r = t[e];
			if (r != null) {
				this._previousStyleProperties.add(e);
				let t = typeof r == "string" && r.endsWith(mr);
				e.includes("-") || t ? n.setProperty(e, t ? r.slice(0, hr) : r, t ? pr : "") : n[e] = r;
			}
		}
		return Ae;
	}
});
function _r(e, ...t) {
	return typeof e == "object" && !!e && t.every((t) => t in e);
}
var vr = Symbol("internals");
function yr(e) {
	return _r(e, vr);
}
var br = Symbol("_internals"), xr = Symbol("_customState");
function E(e, t) {
	var n;
	class r extends e {
		constructor() {
			super(...arguments), this[n] = /* @__PURE__ */ new Set();
		}
		get [(n = xr, vr)]() {
			return this[br] ?? (this[br] = this.attachInternals());
		}
	}
	return r.formAssociated = t, r;
}
function Sr(e, t) {
	return xr in e ? e[xr].has(t) : e[vr].states.has(t);
}
function Cr(e, t) {
	Sr(e, t) || (xr in e && e[xr].add(t), e[vr]?.states.add(t), e[vr]?.states.has(t));
}
function D(e, t) {
	return xr in e && e[xr].delete(t), e[vr]?.states.delete(t) ? (e[vr]?.states.has(t), !0) : !1;
}
function O(e, t, n) {
	n ? Cr(e, t) : D(e, t);
}
function wr(e, t, ...n) {
	n.forEach((n) => O(e, `--${n}`, n === t));
}
function Tr(e) {
	return _r(e, "checked");
}
function Er(t) {
	class n extends t {
		constructor() {
			super(...arguments), this.checked = !1;
		}
		update(e) {
			super.update(e), e.has("checked") && (this.role === "button" ? (this.ariaPressed = `${this.checked}`, this.ariaChecked = null) : this.role && this.role !== "none" && this.role !== "presentation" && (this.ariaChecked = `${this.checked}`, this.ariaPressed = null));
		}
	}
	return e([C({
		type: Boolean,
		reflect: !0
	})], n.prototype, "checked", void 0), n;
}
function Dr(e) {
	return _r(e, "indeterminate") && Tr(e);
}
function Or(t) {
	class n extends Er(t) {
		constructor() {
			super(...arguments), this.indeterminate = !1;
		}
		update(e) {
			super.update(e), e.has("indeterminate") && this.role && this.role !== "none" && this.role !== "presentation" && (this.ariaChecked = !this.checked && this.indeterminate ? "mixed" : `${this.checked}`);
		}
	}
	return e([C({
		type: Boolean,
		reflect: !0
	})], n.prototype, "indeterminate", void 0), n;
}
function kr(e) {
	return _r(e, "selected");
}
function Ar(t) {
	class n extends t {
		constructor() {
			super(...arguments), this.selected = !1;
		}
		update(e) {
			super.update(e), e.has("selected") && (this.role === "button" ? (this.ariaPressed = `${this.selected}`, this.ariaSelected = null, this.ariaChecked = null) : this.role === "radio" ? (this.ariaChecked = `${this.selected}`, this.ariaSelected = null, this.ariaPressed = null) : this.role && this.role !== "none" && this.role !== "presentation" && (this.ariaSelected = `${this.selected}`, this.ariaPressed = null, this.ariaChecked = null));
		}
	}
	return e([C({
		type: Boolean,
		reflect: !0
	})], n.prototype, "selected", void 0), n;
}
function jr(e) {
	return Tr(e) && e.checked || kr(e) && e.selected;
}
function Mr(e, t) {
	Tr(e) ? e.checked = t : e.selected = t;
}
function Nr(e) {
	return _r(e, "dirty", "pristine", "markAsDirty", "markAsPristine");
}
var Pr = Symbol("_eventHandler");
function Fr(e) {
	var t;
	class n extends e {
		constructor() {
			super(...arguments), this[t] = () => this.markAsDirty();
		}
		get dirty() {
			return Sr(this, "--dirty");
		}
		get pristine() {
			return !this.dirty;
		}
		connectedCallback() {
			this.markAsPristine(), super.connectedCallback(), this.addEventListener("change", this[Pr]);
		}
		disconnectedCallback() {
			super.disconnectedCallback(), this.removeEventListener("change", this[Pr]);
		}
		markAsPristine() {
			D(this, "--dirty");
		}
		markAsDirty() {
			Cr(this, "--dirty");
		}
	}
	return t = Pr, n;
}
function Ir(e) {
	return _r(e, "touched", "untouched", "markAsTouched", "markAsUntouched");
}
var Lr = Symbol("_eventHandler");
function Rr(e) {
	var t;
	class n extends e {
		constructor() {
			super(...arguments), this[t] = () => this.markAsTouched();
		}
		get touched() {
			return Sr(this, "--touched");
		}
		get untouched() {
			return !this.touched;
		}
		connectedCallback() {
			this.markAsUntouched(), super.connectedCallback(), this.addEventListener("focusout", this[Lr], { capture: !0 });
		}
		disconnectedCallback() {
			super.disconnectedCallback(), this.removeEventListener("focusout", this[Lr], { capture: !0 });
		}
		markAsTouched() {
			Cr(this, "--touched");
		}
		markAsUntouched() {
			D(this, "--touched");
		}
	}
	return t = Lr, n;
}
var zr = {
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
}, Br = {
	scale: c("var(--md-sys-density-scale, 0)"),
	size: c("var(--md-sys-density-size, 4px)")
}, Vr = {
	...Br,
	calc(e) {
		return c(`calc(max(${e}, ${Br.scale}) * ${Br.size})`);
	},
	calcHalf(e) {
		return c(`calc(max(${e}, ${Br.scale}) * (${Br.size} / 2))`);
	}
}, Hr = `color-mix(in srgb, var(--m3e-elevation-color, ${zr.shadow}) 20%, transparent)`, Ur = [
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
], Wr = `color-mix(in srgb, var(--m3e-elevation-color, ${zr.shadow}) 14%, transparent)`, Gr = [
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
], Kr = `color-mix(in srgb, var(--m3e-elevation-color, ${zr.shadow}) 12%, transparent)`, qr = [
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
function Jr(e) {
	return `${Hr} ${Ur[e]},${Wr} ${Gr[e]},${Kr} ${qr[e]}`;
}
var Yr = {
	level0: c(`var(--md-sys-elevation-level0, ${Jr(0)})`),
	level1: c(`var(--md-sys-elevation-level1, ${Jr(1)})`),
	level2: c(`var(--md-sys-elevation-level2, ${Jr(3)})`),
	level3: c(`var(--md-sys-elevation-level3, ${Jr(6)})`),
	level4: c(`var(--md-sys-elevation-level4, ${Jr(8)})`),
	level5: c(`var(--md-sys-elevation-level5, ${Jr(12)})`)
};
function k(e) {
	return c(`var(--md-sys-measurement-space${e}, ${e / 100 * 8}px)`);
}
var Xr = {
	space0: k(0),
	space25: k(25),
	space50: k(50),
	space75: k(75),
	space100: k(100),
	space125: k(125),
	space150: k(150),
	space175: k(175),
	space200: k(200),
	space225: k(225),
	space250: k(250),
	space300: k(300),
	space325: k(325),
	space350: k(350),
	space375: k(375),
	space400: k(400),
	space425: k(425),
	space450: k(450),
	space475: k(475),
	space500: k(500),
	space525: k(525),
	space550: k(550),
	space575: k(575),
	space600: k(600),
	space625: k(625),
	space650: k(650),
	space675: k(675),
	space700: k(700),
	space725: k(725),
	space750: k(750),
	space775: k(775),
	space800: k(800),
	space825: k(825),
	space850: k(850),
	space875: k(875),
	space900: k(900)
}, Zr = {
	emphasized: c("var(--md-sys-motion-easing-emphasized, cubic-bezier(0.2, 0.0, 0, 1.0))"),
	emphasizedDecelerate: c("var(--md-sys-motion-easing-emphasized-decelerate, cubic-bezier(0.05, 0.7, 0.1, 1.0))"),
	emphasizedAccelerate: c("var(--md-sys-motion-easing-emphasized-accelerate, cubic-bezier(0.3, 0.0, 0.8, 0.15))"),
	standard: c("var(--md-sys-motion-easing-standard, cubic-bezier(0.2, 0.0, 0, 1.0))"),
	standardDecelerate: c("var(--md-sys-motion-easing-standard-decelerate, cubic-bezier(0, 0, 0, 1))"),
	standardAccelerate: c("var(--md-sys-motion-easing-standard-accelerate, cubic-bezier(0.3, 0, 1, 1))")
}, Qr = {
	fastSpatial: c("var(--md-sys-motion-spring-fast-spatial, 350ms cubic-bezier(0.27, 1.06, 0.18, 1.00))"),
	defaultSpatial: c("var(--md-sys-motion-spring-default-spatial, 500ms cubic-bezier(0.27, 1.06, 0.18, 1.00))"),
	slowSpatial: c("var(--md-sys-motion-spring-slow-spatial, 750ms cubic-bezier(0.27, 1.06, 0.18, 1.00))"),
	fastEffects: c("var(--md-sys-motion-spring-fast-effects, 150ms cubic-bezier(0.31, 0.94, 0.34, 1.00))"),
	defaultEffects: c("var(--md-sys-motion-spring-default-effects, 200ms cubic-bezier(0.34, 0.80, 0.34, 1.00))"),
	slowEffects: c("var(--md-sys-motion-spring-slow-effects, 200ms cubic-bezier(0.34, 0.88, 0.34, 1.00))")
}, $r = {
	easing: Zr,
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
	spring: Qr
}, ei = {
	width: c("var(--m3e-scrollbar-width, auto)"),
	thinWidth: c("var(--m3e-scrollbar-thin-width, thin)"),
	color: c("var(--m3e-scrollbar-thumb-color, #938f94) var(--m3e-scrollbar-track-color, transparent)")
}, A = {
	none: c("var(--md-sys-shape-corner-value-none, 0)"),
	extraSmall: c("var(--md-sys-shape-corner-value-extra-small, 4px)"),
	small: c("var(--md-sys-shape-corner-value-small, 8px)"),
	medium: c("var(--md-sys-shape-corner-value-medium, 12px)"),
	large: c("var(--md-sys-shape-corner-value-large, 16px)"),
	largeIncreased: c("var(--md-sys-shape-corner-value-large-increased, 20px)"),
	extraLarge: c("var(--md-sys-shape-corner-value-extra-large, 28px)"),
	extraLargeIncreased: c("var(--md-sys-shape-corner-value-extra-large-increased, 32px)"),
	extraExtraLarge: c("var(--md-sys-shape-corner-value-extra-extra-large, 48px)")
}, j = {
	color: zr,
	elevation: Yr,
	motion: $r,
	shape: { corner: {
		full: c("var(--md-sys-shape-corner-full, 9999px)"),
		extraLargeTop: c(`var(--md-sys-shape-corner-extra-large-top, ${A.extraLarge} ${A.extraLarge} ${A.none} ${A.none})`),
		extraLarge: c(`var(--md-sys-shape-corner-extra-large, ${A.extraLarge})`),
		extraLargeEnd: c(`${A.none} ${A.extraLarge} ${A.extraLarge} ${A.none}`),
		extraLargeStart: c(`${A.extraLarge} ${A.none} ${A.none} ${A.extraLarge}`),
		largeTop: c(`var(--md-sys-shape-corner-large-top, ${A.large} ${A.large} ${A.none} ${A.none})`),
		largeEnd: c(`var(--md-sys-shape-corner-large-end, ${A.none} ${A.large} ${A.large} ${A.none})`),
		largeStart: c(`var(--md-sys-shape-corner-large-start, ${A.large} ${A.none} ${A.none} ${A.large})`),
		large: c(`var(--md-sys-shape-corner-large, ${A.large})`),
		medium: c(`var(--md-sys-shape-corner-medium, ${A.medium})`),
		mediumTop: c(`${A.medium} ${A.medium} ${A.none} ${A.none}`),
		mediumEnd: c(`${A.none} ${A.medium} ${A.medium} ${A.none}`),
		mediumStart: c(`${A.medium} ${A.none} ${A.none} ${A.medium}`),
		small: c(`var(--md-sys-shape-corner-small, ${A.small})`),
		smallTop: c(`${A.small} ${A.small} ${A.none} ${A.none}`),
		smallEnd: c(`${A.none} ${A.small} ${A.small} ${A.none}`),
		smallStart: c(`${A.small} ${A.none} ${A.none} ${A.small}`),
		extraSmallTop: c(`var(--md-sys-shape-corner-extra-small-top, ${A.extraSmall} ${A.extraSmall} ${A.none} ${A.none})`),
		extraSmall: c(`var(--md-sys-shape-corner-extra-small, ${A.extraSmall})`),
		extraSmallEnd: c(`${A.none} ${A.extraSmall} ${A.extraSmall} ${A.none}`),
		extraSmallStart: c(`${A.extraSmall} ${A.none} ${A.none} ${A.extraSmall}`),
		extraSmallBottom: c(`${A.none} ${A.none} ${A.extraSmall} ${A.extraSmall}`),
		none: c(`var(--md-sys-shape-corner-none, ${A.none})`),
		largeIncreased: c(`var(--md-sys-shape-corner-large-increased, ${A.largeIncreased})`),
		extraLargeIncreased: c(`var(--md-sys-shape-corner-extra-large-increased, ${A.extraLargeIncreased})`),
		extraExtraLarge: c(`var(--md-sys-shape-corner-extra-extra-large, ${A.extraExtraLarge})`),
		value: A
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
	scrollbar: ei,
	density: Vr,
	measurement: Xr
};
function ti(e) {
	return _r(e, "disabled");
}
function ni(t, n = !0, r = !0) {
	class i extends t {
		constructor() {
			super(...arguments), this.disabled = !1;
		}
		update(e) {
			super.update(e), e.has("disabled") && r && this.role && this.role !== "none" && this.role !== "presentation" && (this.ariaDisabled = this.disabled ? "true" : null);
		}
	}
	return e([C({
		type: Boolean,
		reflect: n
	})], i.prototype, "disabled", void 0), i;
}
function ri(e) {
	return _r(e, "disabledInteractive") && ti(e);
}
var ii = [
	"click",
	"dblclick",
	"auxclick",
	"keydown",
	"keyup"
], ai = [
	"Tab",
	"ArrowLeft",
	"ArrowUp",
	"ArrowRight",
	"ArrowDown",
	"Left",
	"Up",
	"Right",
	"Down"
], oi = Symbol("_suppressedEventHandler");
function si(t) {
	var n;
	class r extends t {
		constructor() {
			super(...arguments), this[n] = (e) => {
				if (this.disabledInteractive) {
					if (e instanceof KeyboardEvent && ai.includes(e.key)) return;
					e.stopImmediatePropagation(), e.preventDefault();
				}
			}, this.disabledInteractive = !1;
		}
		connectedCallback() {
			ii.forEach((e) => this.addEventListener(e, this[oi], !0)), super.connectedCallback();
		}
		disconnectedCallback() {
			ii.forEach((e) => this.removeEventListener(e, this[oi], !0)), super.disconnectedCallback();
		}
		update(e) {
			super.update(e), (e.has("disabled") || e.has("disabledInteractive")) && this.role && this.role !== "none" && this.role !== "presentation" && this.role !== "none" && (this.ariaDisabled = this.disabled || this.disabledInteractive ? "true" : null);
		}
	}
	return n = oi, e([C({
		attribute: "disabled-interactive",
		type: Boolean,
		reflect: !0
	})], r.prototype, "disabledInteractive", void 0), r;
}
var ci = Symbol("updateLabels");
function li(e) {
	return _r(e, "labels") && yr(e);
}
var ui = Symbol("_eventHandler");
function di(e) {
	var t;
	class n extends e {
		constructor() {
			super(...arguments), this[t] = (e) => {
				e.defaultPrevented || this[ci]();
			};
		}
		get labels() {
			return this[vr].labels;
		}
		connectedCallback() {
			super.connectedCallback(), this.addEventListener("focusout", this[ui]), this.addEventListener("change", this[ui]);
		}
		disconnectedCallback() {
			super.disconnectedCallback(), this.removeEventListener("focusout", this[ui]), this.removeEventListener("change", this[ui]);
		}
		update(e) {
			super.update(e), this[ci]();
		}
		[(t = ui, ci)]() {
			let e = this.hasAttribute("tabindex"), t = ti(this) && this.disabled || ri(this) && this.disabledInteractive, n = Ir(this) && this.touched && (this.ariaInvalid || Sr(this, "--invalid"));
			for (let r of this.labels ?? []) r.style.userSelect = e ? "none" : "", r.style.cursor = !t && e ? "pointer" : "", !t && e ? r.style.setProperty("-webkit-tap-highlight-color", "rgba(0, 0, 0, 0)") : r.style.removeProperty("-webkit-tap-highlight-color"), r.style.color = t ? `color-mix(in srgb, ${j.color.onSurface} 38%, transparent)` : n ? `${j.color.error}` : "";
		}
	}
	return n.formAssociated = !0, n;
}
var fi = Symbol("formValue"), pi = Symbol("defaultValue"), mi = Symbol("_defaultValue"), hi = Symbol("_defaultIndeterminate"), gi = Symbol("_formDisabled");
function _i(t) {
	var n, r;
	class i extends t {
		constructor() {
			super(...arguments), this[n] = !1, this[r] = !1;
		}
		get form() {
			return this[vr].form;
		}
		get labels() {
			return this[vr].labels;
		}
		get [(n = hi, r = gi, fi)]() {
			return null;
		}
		get [pi]() {
			return this[mi];
		}
		get name() {
			return this.getAttribute("name") ?? "";
		}
		set name(e) {
			e ? this.setAttribute("name", e) : this.removeAttribute("name");
		}
		get disabled() {
			return super.disabled || this[gi];
		}
		set disabled(e) {
			super.disabled = e;
		}
		connectedCallback() {
			super.connectedCallback(), Tr(this) ? (this[mi] = this.checked, Dr(this) && (this[hi] = this.indeterminate)) : "value" in this && (this[mi] = this.value);
		}
		requestUpdate(e, t, n) {
			super.requestUpdate(e, t, n), this[vr].setFormValue(this[fi]);
		}
		formDisabledCallback(e) {
			let t = this.disabled;
			this[gi] = e, this.disabled != t && this.requestUpdate("disabled", t);
		}
		formResetCallback() {
			Tr(this) ? (this.checked = this[mi] === !0, Dr(this) && (this.indeterminate = this[hi])) : "value" in this && (this.value = this[pi]), Nr(this) && this.markAsPristine(), Ir(this) && this.markAsUntouched();
		}
	}
	return i.formAssociated = !0, e([C({ noAccessor: !0 })], i.prototype, "name", null), e([C({ type: Boolean })], i.prototype, "disabled", null), i;
}
var vi = Symbol("validate"), yi = Symbol("defaultValidationMessages"), bi = Symbol("_updateValidity"), xi = Symbol("_validityMessage"), Si = Symbol("_validationMessages");
function Ci(t) {
	var n;
	class r extends t {
		constructor() {
			super(...arguments), this[n] = {};
		}
		get willValidate() {
			return this[vr].willValidate;
		}
		get validity() {
			return this[bi](), this[vr].validity;
		}
		get validationMessage() {
			return this[bi](), this[vr].validationMessage;
		}
		get [(n = Si, yi)]() {
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
			this[Si] = e;
		}
		get validationMessages() {
			return {
				...this[yi],
				...this[Si]
			};
		}
		[vi]() {
			return this[xi] ? { customError: !0 } : void 0;
		}
		reportValidity() {
			return Ir(this) && this.markAsTouched(), this[bi](), this[vr].reportValidity();
		}
		checkValidity() {
			return this[bi](), this[vr].checkValidity();
		}
		setCustomValidity(e) {
			this[xi] = e || void 0, this[bi]();
		}
		requestUpdate(e, t, n) {
			super.requestUpdate(e, t, n), this[bi]();
		}
		firstUpdated(e) {
			super.firstUpdated(e), this[bi]();
		}
		[bi]() {
			if (!this.isConnected) return;
			let e = this[vi](), t = e?.customError && this[xi] || this._getValidityMessage(e), n = !!t;
			this[vr].setValidity(e, t), this.ariaInvalid = n ? "true" : null, O(this, "--invalid", n === !0), li(this) && this[ci]?.();
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
	return e([C({ type: Object })], r.prototype, "validationMessages", null), r;
}
var wi = Symbol("_tabindex");
function Ti(e) {
	var t;
	class n extends e {
		constructor() {
			super(...arguments), this[t] = 0;
		}
		connectedCallback() {
			this[wi] = Number.parseInt(this.getAttribute("tabindex") ?? "0"), super.connectedCallback();
		}
		firstUpdated(e) {
			super.firstUpdated(e), !this.hasAttribute("tabindex") && !e.has("disabled") && this.setAttribute("tabindex", `${this[wi]}`);
		}
		update(e) {
			if (super.update(e), e.has("disabled")) {
				if (!this.disabled && this.role !== "none") this.hasAttribute("tabindex") || this.setAttribute("tabindex", `${this[wi]}`);
				else {
					let e = this.getAttribute("tabindex");
					e && (this[wi] = Number.parseInt(e)), this.removeAttribute("tabindex");
				}
			}
		}
	}
	return t = wi, n;
}
var Ei = Symbol("_clickHandler");
function Di(t) {
	var n;
	class r extends t {
		constructor() {
			super(...arguments), this.type = "button", this[n] = async (e) => {
				if (e.defaultPrevented || ti(this) && this.disabled || ri(this) && this.disabledInteractive) return;
				let t = this[vr].form;
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
					}), this[vr].setFormValue(this.value), t.requestSubmit();
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
			super.connectedCallback(), this.addEventListener("click", this[Ei]);
		}
		disconnectedCallback() {
			super.disconnectedCallback(), this.removeEventListener("click", this[Ei]);
		}
	}
	return n = Ei, r.formAssociated = !0, e([C()], r.prototype, "name", null), e([C()], r.prototype, "value", null), e([C()], r.prototype, "type", void 0), r;
}
var Oi = Symbol("_control"), ki = Symbol("_firstUpdated");
function Ai(t) {
	var n;
	class r extends t {
		constructor() {
			super(...arguments), this[n] = !1, this.htmlFor = null;
		}
		get control() {
			return this[Oi]?.deref() ?? null;
		}
		connectedCallback() {
			super.connectedCallback();
			let e = this[Oi]?.deref();
			e && this.attach(e);
		}
		disconnectedCallback() {
			super.disconnectedCallback();
			let e = this[Oi];
			e && (this.detach(), this[Oi] = e);
		}
		firstUpdated(e) {
			super.firstUpdated(e), this[ki] = !0;
		}
		update(e) {
			if (super.update(e), e.has("htmlFor")) {
				if (this.htmlFor) {
					let e = this.getRootNode();
					e && Rt(this.htmlFor, e).then((e) => {
						e !== this.control && (this.control && this.detach(), e instanceof HTMLElement && this.attach(e));
					});
				} else this.control && this[ki] && this.detach();
			}
		}
		attach(e) {
			this[Oi] = new WeakRef(e);
		}
		detach() {
			this[Oi] = void 0;
		}
	}
	return n = ki, e([C({ attribute: "for" })], r.prototype, "htmlFor", void 0), r;
}
function ji(e, r = !0) {
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
		e.target !== e.currentTarget || ti(this) && this.disabled || ri(this) && this.disabledInteractive ? n(this, a, !1, "f") : (e.key === " " || r && e.key === "Enter") && n(this, a, !0, "f");
	}, u = function(e) {
		e.target !== e.currentTarget || ti(this) && this.disabled || ri(this) && this.disabledInteractive || !t(this, a, "f") ? n(this, a, !1, "f") : (n(this, a, !1, "f"), this.dispatchEvent(new MouseEvent("click", {
			cancelable: !0,
			bubbles: !0,
			composed: !0
		})));
	}, d;
}
var Mi = Symbol("renderPseudoLink");
function Ni(e) {
	return _r(e, "download", "href", "rel", "target");
}
var Pi = Symbol("_clickHandler");
function Fi(n, r = !1) {
	var i, a, o, s, c;
	class l extends n {
		constructor() {
			super(...arguments), i.add(this), this[c] = async (e) => {
				ri(this) && this.disabledInteractive && (e.preventDefault(), e.stopPropagation());
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
			super.connectedCallback(), r || this.addEventListener("click", this[Pi]), this.hasAttribute("href") && this.role === "button" && (this.role = "link");
		}
		disconnectedCallback() {
			super.disconnectedCallback(), r || this.removeEventListener("click", this[Pi]);
		}
		[(i = /* @__PURE__ */ new WeakSet(), c = Pi, Mi)]() {
			let e = ti(this) && this.disabled, n = ri(this) && this.disabledInteractive;
			return !e && !n && this.href ? b`<a href="${this.href}" target="${nt(this.target || void 0)}" rel="${nt(this.rel || void 0)}" download="${nt(this.download || void 0)}" tabindex="-1" aria-hidden="true" @pointerdown="${t(this, i, "m", a)}" @focus="${t(this, i, "m", o)}" @blur="${t(this, i, "m", s)}"></a>` : x;
		}
	}
	return a = function(e) {
		e.button === 2 ? e.target.removeAttribute("aria-hidden") : e.preventDefault();
	}, o = function(e) {
		e.target.blur(), this.focus();
	}, s = function(e) {
		e.target.setAttribute("aria-hidden", "true");
	}, e([C()], l.prototype, "href", void 0), e([C()], l.prototype, "target", void 0), e([C()], l.prototype, "rel", void 0), e([C({ reflect: !1 })], l.prototype, "download", null), l;
}
var Ii = Symbol("_wasConnected");
function Li(e) {
	var t;
	class n extends e {
		constructor() {
			super(...arguments), this[t] = !1;
		}
		reconnectedCallback() {}
		connectedCallback() {
			super.connectedCallback(), this[Ii] && this.reconnectedCallback();
		}
		disconnectedCallback() {
			super.disconnectedCallback(), this[Ii] = !0;
		}
	}
	return t = Ii, n;
}
function Ri(t, n = !0) {
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
	return e([C({
		type: Boolean,
		reflect: !0
	})], r.prototype, "required", void 0), r;
}
function zi(e) {
	class t extends e {
		[vi]() {
			let e = super[vi]();
			return !e && this.required && (Tr(this) && !this.checked || !this.value) ? { valueMissing: !0 } : e;
		}
	}
	return t;
}
function M(e, t) {
	class n extends e {
		connectedCallback() {
			this.role = this.role || t, super.connectedCallback();
		}
	}
	return n;
}
var Bi = Symbol("resumeAnimation");
function Vi(e) {
	class t extends e {
		connectedCallback() {
			super.connectedCallback(), Cr(this, "--no-animate"), this[Bi]();
		}
		[Bi]() {
			requestAnimationFrame(() => D(this, "--no-animate"));
		}
	}
	return t;
}
function Hi(t) {
	class n extends t {
		constructor() {
			super(...arguments), this.vertical = !1;
		}
		update(e) {
			super.update(e), e.has("vertical") && (this.ariaOrientation = this.vertical ? "vertical" : "horizontal");
		}
	}
	return e([C({
		type: Boolean,
		reflect: !0
	})], n.prototype, "vertical", void 0), n;
}
var Ui, Wi = class extends S {
	constructor() {
		super(...arguments), Ui.set(this, (e) => {
			e.defaultPrevented || this._onClick(e);
		});
	}
	connectedCallback() {
		super.connectedCallback(), this.parentElement?.addEventListener("click", t(this, Ui, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.parentElement?.removeEventListener("click", t(this, Ui, "f"));
	}
	render() {
		return b`<slot></slot>`;
	}
};
Ui = /* @__PURE__ */ new WeakMap(), Wi.styles = l`:host { display: contents; } ::slotted(.material-icons) { font-size: inherit !important; }`;
function Gi(e) {
	return e === "vertical" || e === "horizontal" || e === "both";
}
var Ki, qi, Ji, Yi, Xi, Zi, Qi, $i, ea, ta = class extends E(S) {
	constructor() {
		super(...arguments), Ki.add(this), qi.set(this, !1), Ji.set(this, !1), this.open = !1, this.orientation = "vertical", this.noAnimate = !1, this.slide = !1;
	}
	connectedCallback() {
		super.connectedCallback(), t(this, Ki, "m", Yi).call(this);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("orientation") && t(this, Ki, "m", Yi).call(this);
	}
	update(e) {
		super.update(e);
		let r = this.noAnimate || (e.has("orientation") || e.has("slide")) && !e.has("open");
		Cr(this, "--no-animate"), t(this, qi, "f") ? (this.toggleAttribute("inert", !this.open), this.open ? (n(this, Ji, !0, "f"), r || It() || (t(this, Ki, "m", Zi).call(this), O(this, "--overflows", this.orientation === "vertical" ? this.clientHeight < this.scrollHeight : this.orientation === "horizontal" ? this.clientWidth < this.scrollWidth : this.clientHeight < this.scrollHeight || this.clientWidth < this.scrollWidth), t(this, Ki, "m", Qi).call(this)), D(this, "--closing"), Cr(this, "--opening"), this.dispatchEvent(new Event("opening")), t(this, Ki, "m", Qi).call(this), D(this, "--no-animate"), t(this, Ki, "m", $i).call(this), r || It() ? (t(this, Ki, "m", Zi).call(this), D(this, "--opening"), this.dispatchEvent(new Event("opened"))) : this.addEventListener("transitionend", () => {
			this.open && (t(this, Ki, "m", Zi).call(this), D(this, "--opening"), this.dispatchEvent(new Event("opened")));
		}, { once: !0 })) : (D(this, "--opening"), Cr(this, "--closing"), this.dispatchEvent(new Event("closing")), t(this, Ki, "m", $i).call(this), t(this, Ji, "f") && D(this, "--no-animate"), r || It() ? (t(this, Ki, "m", Qi).call(this), D(this, "--closing"), this.dispatchEvent(new Event("closed"))) : requestAnimationFrame(() => {
			t(this, Ki, "m", Qi).call(this), this.addEventListener("transitionend", () => {
				this.open || (D(this, "--closing"), this.dispatchEvent(new Event("closed")));
			}, { once: !0 });
		}))) : (this.open && (n(this, Ji, !0, "f"), t(this, Ki, "m", Zi).call(this)), n(this, qi, !0, "f"));
	}
	render() {
		return b`<slot @slotchange="${t(this, Ki, "m", Xi)}"></slot>`;
	}
};
qi = /* @__PURE__ */ new WeakMap(), Ji = /* @__PURE__ */ new WeakMap(), Ki = /* @__PURE__ */ new WeakSet(), Yi = function() {
	Gi(this.orientation) || (this.orientation = "horizontal"), wr(this, this.orientation, "both", "horizontal", "vertical");
}, Xi = function() {
	n(this, qi, !0, "f"), t(this, Ki, "m", ea).call(this);
}, Zi = function() {
	switch (this.orientation) {
		case "vertical":
			this.style.height = "auto";
			break;
		case "horizontal":
			this.style.width = "auto";
			break;
		case "both": this.style.height = this.style.width = "auto";
	}
}, Qi = function() {
	switch (this.orientation) {
		case "vertical":
			this.style.height = "";
			break;
		case "horizontal":
			this.style.width = "";
			break;
		case "both": this.style.height = this.style.width = "";
	}
}, $i = function() {
	switch (this.orientation) {
		case "vertical":
			this.style.height = `${this.scrollHeight}px`;
			break;
		case "horizontal":
			this.style.width = `${this.scrollWidth}px`;
			break;
		case "both": this.style.height = `${this.scrollHeight}px`, this.style.width = `${this.scrollWidth}px`;
	}
	t(this, Ki, "m", ea).call(this);
}, ea = function() {
	if (this.slide) switch (this.orientation) {
		case "vertical":
			this.style.setProperty("--_collapsible-vertical-offset", `${-this.scrollHeight}px`);
			break;
		case "horizontal":
			this.style.setProperty("--_collapsible-horizontal-offset", `${-this.scrollWidth}px`);
			break;
		case "both": this.style.setProperty("--_collapsible-vertical-offset", `${-this.scrollHeight}px`), this.style.setProperty("--_collapsible-horizontal-offset", `${-this.scrollWidth}px`);
	}
}, ta.styles = l`:host { display: block; overflow: hidden; --_collapsible-animation: var( --m3e-collapsible-animation, var(--m3e-collapsible-animation-duration, ${j.motion.duration.medium1}) var(--m3e-collapsible-animation-function, ${j.motion.easing.standard}) ); } :host([hidden]) { display: none; } :host(:is(:state(--vertical), :--vertical)) { height: 0px; } :host(:is(:state(--vertical), :--vertical):not([slide])) { transition: ${c("visibility var(--_collapsible-animation),\n        height var(--_collapsible-animation),\n        padding-top var(--_collapsible-animation),\n        padding-bottom var(--_collapsible-animation)")}; } :host(:is(:state(--vertical), :--vertical)[slide]) { transition: ${c("visibility var(--_collapsible-animation),\n        height var(--_collapsible-animation),\n        transform var(--m3e-collapsible-slide-animation, var(--_collapsible-animation)),\n        padding-top var(--_collapsible-animation),\n        padding-bottom var(--_collapsible-animation)")}; } :host(:is(:state(--horizontal), :--horizontal)) { width: 0px; } :host(:is(:state(--horizontal), :--horizontal):not([slide])) { transition: ${c("visibility var(--_collapsible-animation),\n        width var(--_collapsible-animation),\n        padding-left var(--_collapsible-animation),\n        padding-right var(--_collapsible-animation)")}; } :host(:is(:state(--horizontal), :--horizontal)[slide]) { transition: ${c("visibility var(--_collapsible-animation),\n        width var(--_collapsible-animation),\n        transform var(--m3e-collapsible-slide-animation, var(--_collapsible-animation)),\n        padding-left var(--_collapsible-animation),\n        padding-right var(--_collapsible-animation)")}; } :host(:is(:state(--both), :--both)) { height: 0px; width: 0px; } :host(:is(:state(--both), :--both):not([slide])) { transition: ${c("visibility var(--_collapsible-animation),\n        width var(--_collapsible-animation),\n        height var(--_collapsible-animation),\n        padding var(--_collapsible-animation)")}; } :host(:is(:state(--both), :--both)[slide]) { transition: ${c("visibility var(--_collapsible-animation),\n        width var(--_collapsible-animation),\n        height var(--_collapsible-animation),\n        transform var(--m3e-collapsible-slide-animation, var(--_collapsible-animation)),\n        padding var(--_collapsible-animation)")}; } :host(:not(:is(:state(--closing), :--closing)):not([open])) { visibility: hidden; } :host(:is(:state(--vertical), :--vertical):not([open])) { min-height: unset !important; padding-top: 0px !important; padding-bottom: 0px !important; } :host(:is(:state(--horizontal), :--horizontal):not([open])) { min-width: unset !important; padding-left: 0px !important; padding-right: 0px !important; } :host(:is(:state(--both), :--both):not([open])) { min-height: unset !important; min-width: unset !important; padding: 0px !important; } :host([no-animate]), :host(:is(:state(--no-animate), :--no-animate)) { transition-duration: 0ms; } :host(:is(:state(--vertical), :--vertical):is(:state(--opening), :--opening)), :host(:is(:state(--vertical), :--vertical):is(:state(--closing), :--closing)) { overflow-y: hidden !important; } :host(:is(:state(--horizontal), :--horizontal):is(:state(--opening), :--opening)), :host(:is(:state(--horizontal), :--horizontal):is(:state(--closing), :--closing)) { overflow-x: hidden !important; } :host(:is(:state(--both), :--both):is(:state(--opening), :--opening)), :host(:is(:state(--both), :--both):is(:state(--closing), :--closing)) { overflow-y: hidden !important; overflow-x: hidden !important; } :host(:is(:state(--overflows), :--overflows)) { scrollbar-gutter: stable; } :host(:is(:state(--vertical), :--vertical)[slide]:not([open])) { transform: translateY(var(--_collapsible-vertical-offset, 0px)); } :host(:is(:state(--vertical), :--vertical)[slide][open]) { transform: translateY(0px); } :host(:is(:state(--horizontal), :--horizontal)[slide]:not([open])) { transform: translateX(var(--_collapsible-horizontal-offset, 0px)); } :host(:is(:state(--horizontal), :--horizontal)[slide][open]) { transform: translateX(0px); } :host(:is(:state(--both), :--both)[slide]:not([open])) { transform: translateX(var(--_collapsible-horizontal-offset, 0px)) translateY(var(--_collapsible-vertical-offset, 0px)); } :host(:is(:state(--both), :--both)[slide][open]) { transform: translateX(0px) translateY(0px); } ::slotted(*) { --m3e-collapsible-animation: initial; --m3e-collapsible-animation-duration: initial; --m3e-collapsible-animation-function: initial; } @media (prefers-reduced-motion) { :host(:is(:state(--vertical), :--vertical):not([slide])), :host(:is(:state(--vertical), :--vertical)[slide]), :host(:is(:state(--horizontal), :--horizontal):not([slide])), :host(:is(:state(--horizontal), :--horizontal)[slide]), :host(:is(:state(--both), :--both):not([slide])), :host(:is(:state(--both), :--both)[slide]) { transition: none; } }`, e([C({
	type: Boolean,
	reflect: !0
})], ta.prototype, "open", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], ta.prototype, "orientation", void 0), e([C({
	attribute: "no-animate",
	type: Boolean,
	reflect: !0
})], ta.prototype, "noAnimate", void 0), e([C({
	type: Boolean,
	reflect: !0
})], ta.prototype, "slide", void 0), ta = e([T("m3e-collapsible")], ta);
function na(e) {
	return e === 0 || e === 1 || e === 2 || e === 3 || e === 4 || e === 5;
}
var ra = {
	liftDuration: c(`var(--m3e-elevation-lift-duration, ${j.motion.duration.short4})`),
	liftEasing: c(`var(--m3e-elevation-lift-easing, ${j.motion.easing.standard})`),
	settleDuration: c(`var(--m3e-elevation-settle-duration, ${j.motion.duration.short3})`),
	settleEasing: c(`var(--m3e-elevation-settle-easing, ${j.motion.easing.standardAccelerate})`),
	level: c(`var(--m3e-elevation-level, ${j.elevation.level0})`),
	hoverLevel: c(`var(--m3e-elevation-hover-level, ${j.elevation.level0})`),
	focusLevel: c(`var(--m3e-elevation-focus-level, ${j.elevation.level0})`),
	pressedLevel: c(`var(--m3e-elevation-pressed-level, ${j.elevation.level0})`)
}, ia, aa, oa, sa, ca, la, ua, da = class extends Ai(M(S, "none")) {
	constructor() {
		super(...arguments), ia.add(this), aa.set(this, new hn(this, {
			target: null,
			callback: (e) => t(this, ia, "m", ca).call(this, e)
		})), oa.set(this, new nn(this, {
			target: null,
			callback: (e) => t(this, ia, "m", la).call(this, e)
		})), sa.set(this, new Yn(this, {
			target: null,
			callback: (e) => t(this, ia, "m", ua).call(this, e)
		})), this.disabled = !1, this.level = null;
	}
	attach(e) {
		super.attach(e), t(this, aa, "f").observe(e), t(this, oa, "f").observe(e), t(this, sa, "f").observe(e);
	}
	detach() {
		this.control && (t(this, aa, "f").unobserve(this.control), t(this, oa, "f").unobserve(this.control), t(this, sa, "f").unobserve(this.control)), super.detach();
	}
	connectedCallback() {
		this.ariaHidden = "true", super.connectedCallback();
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this._shadow?.classList.toggle("hover", !1), this._shadow?.classList.toggle("focus", !1), this._shadow?.classList.toggle("pressed", !1), this._shadow?.classList.toggle("resting", !1);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("level") && this.level !== null && !na(this.level) && (this.level = null);
	}
	updated(e) {
		super.updated(e), e.has("disabled") && this.disabled && (this._shadow?.classList.toggle("hover", !1), this._shadow?.classList.toggle("focus", !1), this._shadow?.classList.toggle("pressed", !1), this._shadow?.classList.toggle("resting", !0));
	}
	render() {
		return b`<div class="shadow"></div>`;
	}
};
aa = /* @__PURE__ */ new WeakMap(), oa = /* @__PURE__ */ new WeakMap(), sa = /* @__PURE__ */ new WeakMap(), ia = /* @__PURE__ */ new WeakSet(), ca = function(e) {
	this.disabled || (this._shadow?.classList.toggle("hover", e), this._shadow?.classList.toggle("resting", !e));
}, la = function(e) {
	this.disabled || this._shadow?.classList.toggle("focus", e);
}, ua = function(e) {
	this.disabled || this._shadow?.classList.toggle("pressed", e);
}, da.styles = l`:host { display: block; } :host([hidden]) { display: none; } :host, .shadow { position: absolute; left: 0; top: 0; right: 0; bottom: 0; pointer-events: none; border-radius: inherit; } .shadow.resting, .shadow.focus, .shadow.pressed { will-change: box-shadow; transition: ${c(`box-shadow ${ra.settleDuration} ${ra.settleEasing};`)}; } .shadow.hover { will-change: box-shadow; transition: ${c(`box-shadow ${ra.liftDuration} ${ra.liftEasing};`)}; } .shadow { box-shadow: ${ra.level}; } .shadow.focus { box-shadow: ${ra.focusLevel}; } .shadow.hover { box-shadow: ${ra.hoverLevel}; } .shadow.pressed { box-shadow: ${ra.pressedLevel}; } :host([level="0"]) .shadow { --m3e-elevation-level: ${j.elevation.level0}; --m3e-elevation-hover-level: ${j.elevation.level1}; } :host([level="1"]) .shadow { --m3e-elevation-level: ${j.elevation.level1}; --m3e-elevation-hover-level: ${j.elevation.level2}; } :host([level="2"]) .shadow { --m3e-elevation-level: ${j.elevation.level2}; --m3e-elevation-hover-level: ${j.elevation.level3}; } :host([level="3"]) .shadow { --m3e-elevation-level: ${j.elevation.level3}; --m3e-elevation-hover-level: ${j.elevation.level4}; } :host([level="4"]) .shadow { --m3e-elevation-level: ${j.elevation.level4}; --m3e-elevation-hover-level: ${j.elevation.level5}; } :host([level="5"]) .shadow { --m3e-elevation-level: ${j.elevation.level5}; --m3e-elevation-hover-level: ${ra.level}; } :host([level]) .shadow { --m3e-elevation-focus-level: ${ra.level}; --m3e-elevation-pressed-level: ${ra.level}; } @media (prefers-reduced-motion) { .shadow.resting, .shadow.pressed, .shadow.focus, .shadow.hover { transition: none; } } @media (forced-colors: active) { .shadow { display: none; } }`, e([w(".shadow")], da.prototype, "_shadow", void 0), e([C({
	type: Boolean,
	reflect: !0
})], da.prototype, "disabled", void 0), e([C({
	type: Number,
	reflect: !0,
	useDefault: !0
})], da.prototype, "level", void 0), da = e([T("m3e-elevation")], da);
var fa = {
	color: c(`var(--m3e-focus-ring-color, ${j.color.secondary})`),
	duration: c(`var(--m3e-focus-ring-duration, ${j.motion.duration.long2})`),
	thickness: c("var(--m3e-focus-ring-thickness, 3px)"),
	outwardOffset: c("var(--m3e-focus-ring-outward-offset, 2px)"),
	inwardOffset: c("var(--m3e-focus-ring-inward-offset, 0px)"),
	visibility: c("var(--m3e-focus-ring-visibility, visible)"),
	growthFactor: c("var(--m3e-focus-ring-growth-factor, 2)")
}, pa, ma, ha, ga = class extends Ai(M(S, "none")) {
	constructor() {
		super(...arguments), pa.add(this), ma.set(this, new nn(this, {
			target: null,
			callback: (e, n) => t(this, pa, "m", ha).call(this, n)
		})), this.inward = !1, this.disabled = !1;
	}
	show() {
		this._outline?.classList.toggle("visible", !0);
	}
	hide() {
		this._outline?.classList.toggle("visible", !1);
	}
	attach(e) {
		super.attach(e), t(this, ma, "f").observe(e);
	}
	detach() {
		this.control && t(this, ma, "f").unobserve(this.control), super.detach();
	}
	connectedCallback() {
		this.ariaHidden = "true", super.connectedCallback();
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.hide();
	}
	render() {
		return b`<div class="outline"></div>`;
	}
	updated(e) {
		super.updated(e), e.has("disabled") && this.disabled && this.hide();
	}
};
ma = /* @__PURE__ */ new WeakMap(), pa = /* @__PURE__ */ new WeakSet(), ha = function(e) {
	this.disabled || (e ? this.show() : this.hide());
}, ga.styles = l`:host { display: block; position: absolute; left: 0; top: 0; right: 0; bottom: 0; pointer-events: none; border-radius: inherit; outline: none; } :host([hidden]) { display: none; } .outline { contain: layout style; position: absolute; left: 0; top: 0; right: 0; bottom: 0; pointer-events: none; border-radius: inherit; z-index: 1; outline-color: ${fa.color}; outline-width: ${fa.thickness}; visibility: ${fa.visibility}; } .outline.visible { outline-style: solid; } :host(:not([inward])) .outline { outline-offset: ${fa.outwardOffset}; } :host([inward]) .outline { outline-offset: calc(${fa.inwardOffset} - ${fa.thickness}); } :host(:not([inward])) .outline.visible { animation: grow-shrink ${fa.duration}; } :host([inward]) .outline.visible { animation: shrink-grow ${fa.duration}; } @keyframes grow-shrink { 50% { outline-width: calc(${fa.thickness} * ${fa.growthFactor}); } } @keyframes shrink-grow { 50% { outline-offset: calc( ${fa.inwardOffset} - calc(${fa.thickness} * ${fa.growthFactor}) ); outline-width: calc(${fa.thickness} * ${fa.growthFactor}); } } @media (prefers-reduced-motion) { :host(:not([inward])) .outline.visible, :host([inward]) .outline.visible { animation: none; } } @media (forced-colors: active) { .outline { outline-color: Highlight; } }`, e([w(".outline")], ga.prototype, "_outline", void 0), e([C({
	type: Boolean,
	reflect: !0
})], ga.prototype, "inward", void 0), e([C({
	type: Boolean,
	reflect: !0
})], ga.prototype, "disabled", void 0), ga = e([T("m3e-focus-ring")], ga);
var _a = class extends Or(ni(M(S, "none"))) {
	connectedCallback() {
		super.connectedCallback(), this.ariaHidden = "true";
	}
	render() {
		return this.indeterminate ? b`<svg viewBox="0 -960 960 960" fill="currentColor"><path Required d="M240-440v-80h480v80H240Z"/></svg>` : this.checked ? b`<svg viewBox="0 -960 960 960" fill="currentColor"><path d="M382-240 154-468l57-57 171 171 367-367 57 57-424 424Z"/></svg>` : x;
	}
};
_a.styles = l`:host { display: inline-block; vertical-align: middle; width: var(--m3e-checkbox-icon-size, 18px); height: var(--m3e-checkbox-icon-size, 18px); border-radius: var(--m3e-checkbox-container-shape, 2px); box-sizing: border-box; flex: none; contain: layout style paint; line-height: 0; } :host([hidden]) { display: none; } :host(:not([checked]):not([indeterminate])) { border-width: var(--m3e-checkbox-unselected-outline-thickness, 2px); border-style: solid; } :host(:not([disabled])[checked]), :host(:not([disabled])[indeterminate]) { background-color: var(--m3e-checkbox-selected-container-color, ${j.color.primary}); color: var(--m3e-checkbox-selected-icon-color, ${j.color.onPrimary}); } :host(:not([disabled]):not([checked]):not([indeterminate])) { border-color: var(--m3e-checkbox-unselected-outline-color, ${j.color.onSurfaceVariant}); } :host([disabled]:not([checked]):not([indeterminate])) { border-color: color-mix( in srgb, var(--m3e-checkbox-unselected-disabled-outline-color, ${j.color.onSurface}) var(--m3e-checkbox-unselected-disabled-outline-opacity, 38%), transparent ); } :host([disabled][checked]), :host([disabled][indeterminate]) { background-color: color-mix( in srgb, var(--m3e-checkbox-selected-disabled-container-color, ${j.color.onSurface}) var(--m3e-checkbox-selected-disabled-container-opacity, 38%), transparent ); color: color-mix( in srgb, var(--m3e-checkbox-selected-disabled-icon-color, ${j.color.surface}) var(--m3e-checkbox-selected-disabled-icon-opacity, 100%), transparent ); } svg { pointer-events: none; } @media (forced-colors: active) { :host(:not([disabled])[checked]), :host(:not([disabled])[indeterminate]) { border-color: Highlight; background-color: Highlight; color: HighlightText; } :host(:not([disabled]):not([checked]):not([indeterminate])) { border-color: CanvasText; background: Canvas; } :host([disabled]:not([checked]):not([indeterminate])) { border-color: GrayText; background-color: Canvas; } :host([disabled][checked]), :host([disabled][indeterminate]) { background-color: GrayText; color: Canvas; } }`, _a = e([T("m3e-pseudo-checkbox")], _a);
var va = class extends Er(ni(M(S, "none"))) {
	connectedCallback() {
		super.connectedCallback(), this.ariaHidden = "true";
	}
	render() {
		return b`<svg class="icon" viewBox="0 0 20 20"><mask id="cutout2"><rect width="100%" height="100%" fill="white"></rect><circle cx="10" cy="10" r="8" fill="black"></circle></mask><circle class="outer circle" cx="10" cy="10" r="10" mask="url(#cutout2)"></circle><circle class="inner circle" cx="10" cy="10" r="5"></circle></svg>`;
	}
};
va.styles = l`:host { display: inline-block; vertical-align: middle; box-sizing: border-box; width: var(--m3e-radio-icon-size, 20px); height: var(--m3e-radio-icon-size, 20px); flex: none; contain: layout style paint; line-height: 0; } :host([hidden]) { display: none; } .circle { fill: currentColor; } :host(:not([checked])) .circle.inner { opacity: 0; } :host(:not([checked])) { color: var(--m3e-radio-unselected-icon-color, ${j.color.onSurfaceVariant}); } :host([checked]) { color: var(--m3e-radio-selected-icon-color, ${j.color.primary}); } :host([disabled]) { color: color-mix(in srgb, var(--m3e-radio-disabled-icon-color, ${j.color.onSurface}) 38%, transparent); } @media (forced-colors: active) { :host { border-radius: 50%; } :host(:not([checked])) { color: CanvasText; background-color: Canvas; } :host([checked]) { color: HighlightText; background-color: Highlight; } :host([disabled]) { color: GrayText; background-color: Canvas; } }`, va = e([T("m3e-pseudo-radio")], va);
var ya = {
	color: c(`var(--m3e-ripple-color, ${j.color.onSurface})`),
	opacity: c(`var(--m3e-ripple-opacity, ${j.state.pressedStateLayerOpacity})`),
	enterDuration: c(`var(--m3e-ripple-enter-duration, ${j.motion.duration.long1})`),
	exitDuration: c(`var(--m3e-ripple-exit-duration, ${j.motion.duration.medium3})`)
}, ba, xa, Sa, Ca, wa, Ta, Ea = class extends Ai(M(S, "none")) {
	constructor() {
		super(...arguments), ba.add(this), xa.set(this, /* @__PURE__ */ new Set()), Sa.set(this, new Yn(this, {
			target: null,
			minPressedDuration: 0,
			isPressedKey: (e) => e === " ",
			callback: (e, { x: n, y: r }) => t(this, ba, "m", Ta).call(this, e, n, r)
		})), this.disabled = !1, this.centered = !1, this.unbounded = !1, this.radius = null;
	}
	get visible() {
		return t(this, xa, "f").size > 0;
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
		c.classList.add("ripple"), r && c.classList.add("persistent"), c.style.left = `${o - a}px`, c.style.top = `${s - a}px`, c.style.width = `${a * 2}px`, c.style.height = `${a * 2}px`, c.addEventListener("animationend", () => t(this, ba, "m", wa).call(this, c, r), { once: !0 }), c.addEventListener("transitionend", (e) => {
			e.propertyName === "opacity" && t(this, ba, "m", Ca).call(this, c);
		}, { once: !0 }), this.shadowRoot ? (t(this, xa, "f").add(c), this.shadowRoot.appendChild(c)) : t(this, xa, "f").delete(c);
	}
	hide() {
		for (let e of t(this, xa, "f")) e.classList.add("exit");
	}
	attach(e) {
		super.attach(e), t(this, Sa, "f").observe(e);
	}
	detach() {
		this.control && t(this, Sa, "f").unobserve(this.control), super.detach();
	}
	connectedCallback() {
		this.ariaHidden = "true", super.connectedCallback();
	}
	disconnectedCallback() {
		super.disconnectedCallback(), t(this, ba, "m", Ca).call(this);
	}
	updated(e) {
		super.updated(e), e.has("disabled") && this.disabled && this.hide();
	}
};
xa = /* @__PURE__ */ new WeakMap(), Sa = /* @__PURE__ */ new WeakMap(), ba = /* @__PURE__ */ new WeakSet(), Ca = function(e) {
	if (e) e.remove(), t(this, xa, "f").delete(e);
	else {
		for (let e of t(this, xa, "f")) e.remove();
		t(this, xa, "f").clear();
	}
}, wa = function(e, t) {
	t ? e.classList.add("pressed") : e.classList.add("exit");
}, Ta = function(e, t, n) {
	this.disabled || (e ? this.show(t, n, !0) : this.hide());
}, Ea.styles = l`:host { display: block; position: absolute; left: 0; top: 0; right: 0; bottom: 0; pointer-events: none; border-radius: inherit; } :host([hidden]) { display: none; } :host(:not([unbounded])) { overflow: hidden; } :host(:not([unbounded])) .ripple { contain: layout style paint; } :host([unbounded]) .ripple { contain: layout style; } .ripple { display: block; position: absolute; inset: 0; pointer-events: none; transform: scale(0); opacity: ${ya.opacity}; border-radius: 50%; filter: blur(20px); background-color: ${ya.color}; transition: ${c(`background-color ${j.motion.duration.short1} ${j.motion.easing.standard}`)}; will-change: transform, background-color, opacity; animation: ripple ${ya.enterDuration} linear; } .ripple.persistent.pressed { transform: scale(4); } .ripple.exit { transition: ${c(`opacity ${ya.exitDuration} cubic-bezier(0, 0, 0.2, 0.1),
        background-color ${j.motion.duration.short1} ${j.motion.easing.standard}`)}; opacity: 0; } @keyframes ripple { to { transform: scale(4); } } @media (prefers-reduced-motion) { .ripple { transform: scale(4); animation-duration: 90ms; } .ripple:not(.exit), .ripple.exit { transition-duration: 10ms; } } @media (forced-colors: active) { .ripple { display: none; } }`, e([C({
	type: Boolean,
	reflect: !0
})], Ea.prototype, "disabled", void 0), e([C({
	type: Boolean,
	reflect: !0
})], Ea.prototype, "centered", void 0), e([C({
	type: Boolean,
	reflect: !0
})], Ea.prototype, "unbounded", void 0), e([C({ type: Number })], Ea.prototype, "radius", void 0), Ea = e([T("m3e-ripple")], Ea);
function Da(e) {
	return e === "above" || e === "below" || e === "above-below" || e === "none";
}
var Oa, ka = class extends E(S) {
	constructor() {
		super(...arguments), Oa.set(this, () => this._updateScroll()), this.dividers = "above-below", this.thin = !1;
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("scroll", t(this, Oa, "f"));
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("dividers") && !Da(this.dividers) && (this.dividers = "above-below");
	}
	update(e) {
		super.update(e), e.has("dividers") && (this.removeEventListener("scroll", t(this, Oa, "f")), this.dividers !== "none" && this.addEventListener("scroll", t(this, Oa, "f"), { passive: !0 }));
	}
	render() {
		return b`<slot @slotchange="${this._updateScroll}"></slot>`;
	}
	_updateScroll() {
		let e = (this.dividers === "above" || this.dividers === "above-below") && this.scrollTop > 0, t = (this.dividers === "below" || this.dividers === "above-below") && this.scrollHeight - this.scrollTop - this.clientHeight > 1;
		O(this, "--above", e), O(this, "--below", t);
	}
};
Oa = /* @__PURE__ */ new WeakMap(), ka.styles = l`:host { display: block; overflow-y: auto; position: relative; box-sizing: border-box; scrollbar-color: ${j.scrollbar.color}; border-top: var(--m3e-divider-thickness, 1px) solid transparent; border-bottom: var(--m3e-divider-thickness, 1px) solid transparent; outline-color: ${fa.color}; outline-width: ${fa.thickness}; outline-offset: ${fa.outwardOffset}; } :host([hidden]) { display: none; } :host([thin]) { scrollbar-width: ${j.scrollbar.thinWidth}; } :host(:not([thin])) { scrollbar-width: ${j.scrollbar.width}; } :host(:not(:focus-visible):is(:state(--above), :--above)) { border-top-color: var(--m3e-divider-color, ${j.color.outlineVariant}); } :host(:not(:focus-visible):is(:state(--below), :--below)) { border-bottom-color: var(--m3e-divider-color, ${j.color.outlineVariant}); } :host(:focus-visible) { outline-style: solid; animation: grow-shrink ${fa.duration}; } @keyframes grow-shrink { 50% { outline-width: calc(${fa.thickness} * ${fa.growthFactor}); } } @media (forced-colors: active) { :host { border-top: var(--m3e-divider-thickness, 1px) solid Canvas; border-bottom: var(--m3e-divider-thickness, 1px) solid Canvas; } :host(:not(:focus-visible):is(:state(--above), :--above)) { border-top-color: GrayText; } :host(:not(:focus-visible):is(:state(--below), :--below)) { border-bottom-color: GrayText; } } @media (prefers-reduced-motion) { :host(:focus-visible) { animation: none; } }`, e([C()], ka.prototype, "dividers", void 0), e([C({
	type: Boolean,
	reflect: !0
})], ka.prototype, "thin", void 0), e([nr(40)], ka.prototype, "_updateScroll", null), ka = e([T("m3e-scroll-container")], ka);
var Aa, ja, Ma, Na, Pa, Fa = class extends Vi(Li(Ai(M(E(S), "none")))) {
	constructor() {
		super(...arguments), Aa.add(this), ja.set(this, new Yn(this, {
			target: null,
			isPressedKey: (e) => e === " ",
			callback: (e, n) => t(this, Aa, "m", Na).call(this, e, n)
		})), this.disabled = !1, this.selected = !1, this.bounce = !1, this.centered = !1;
	}
	show(e) {
		this._stateLayer?.show(e);
	}
	hide(e) {
		this._stateLayer?.hide(e);
	}
	attach(e) {
		super.attach(e), t(this, ja, "f").observe(e), this._stateLayer?.attach(e);
	}
	detach() {
		this.control && (t(this, ja, "f").unobserve(this.control), this._stateLayer?.detach()), super.detach();
	}
	connectedCallback() {
		this.ariaHidden = "true", super.connectedCallback();
	}
	reconnectedCallback() {
		super.reconnectedCallback(), t(this, Aa, "m", Ma).call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), t(this, Aa, "m", Ma).call(this);
	}
	render() {
		return b`<div class="base" @animationend="${t(this, Aa, "m", Pa)}"><div class="indicator"></div><m3e-state-layer class="state-layer" enable-pressed ?disabled="${this.disabled}"></m3e-state-layer></div>`;
	}
};
ja = /* @__PURE__ */ new WeakMap(), Aa = /* @__PURE__ */ new WeakSet(), Ma = function() {
	let e = this._stateLayer;
	e && this.control !== e.control && (this.control ? e.attach(this.control) : e.detach());
}, Na = function(e, t) {
	if (this.disabled || this.selected || this.centered || It()) return;
	let n = this._base;
	if (e && n) {
		let e = n.getBoundingClientRect(), r = Math.min(Math.max(t.x - e.left, e.height), e.width - e.height);
		n?.style.setProperty("--_pressed-origin-x", `${r}px`);
	}
}, Pa = function() {
	this._base?.style.removeProperty("--_pressed-origin-x");
}, Fa.styles = l`:host { display: block; } :host([hidden]) { display: none; } .state-layer { --m3e-state-layer-hover-color: var( --m3e-selection-indicator-state-layer-hover-color, ${j.color.onSurface} ); --m3e-state-layer-hover-opacity: var( --m3e-selection-indicator-state-layer-hover-opacity, ${j.state.hoverStateLayerOpacity} ); --m3e-state-layer-focus-color: var( --m3e-selection-indicator-state-layer-focus-color, ${j.color.onSurface} ); --m3e-state-layer-focus-opacity: var( --m3e-selection-indicator-state-layer-focus-opacity, ${j.state.focusStateLayerOpacity} ); --m3e-state-layer-pressed-color: var( --m3e-selection-indicator-state-layer-pressed-color, ${j.color.onSurface} ); --m3e-state-layer-pressed-opacity: var( --m3e-selection-indicator-state-layer-pressed-opacity, ${j.state.pressedStateLayerOpacity} ); --m3e-state-layer-duration: var( --m3e-selection-indicator-state-layer-duration, ${j.motion.duration.medium1} ); --m3e-state-layer-easing: var( --m3e-selection-indicator-state-layer-easing, ${j.motion.easing.standard} ); } :host, .base, .indicator { position: absolute; inset: 0; pointer-events: none; border-radius: inherit; } .indicator { opacity: 0; will-change: opacity; } :host(:not([disabled])) .indicator { background-color: var(--m3e-selection-indicator-color, ${j.color.secondaryContainer}); } :host(:not(:is(:state(--no-animate), :--no-animate))) .indicator { transition: ${c(`opacity var(--m3e-selection-indicator-state-layer-duration, ${j.motion.duration.medium1}) var(--m3e-selection-indicator-state-layer-easing, ${j.motion.easing.standard})`)}; } .base { contain: layout style paint; } :host(:not([centered])) .base { transform-origin: var(--_pressed-origin-x, center) center; } :host([centered]) .base { transform-origin: center center; } :host([selected]:not([bounce]):not(:is(:state(--no-animate), :--no-animate))) .base { animation: grow ${j.motion.spring.fastEffects}; } :host([selected][bounce]:not(:is(:state(--no-animate), :--no-animate))) .base { animation: grow ${j.motion.spring.fastSpatial}; } :host([selected]) .indicator { opacity: 1; transition: none; } :host(:not(:is(:state(--no-animate), :--no-animate))) .indicator { transition: ${c(`opacity ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; } @keyframes grow { 0% { transform: scaleX(0); } 100% { transform: scaleX(1); } } @media (prefers-reduced-motion) { :host(:not(:is(:state(--no-animate), :--no-animate))) .indicator { transition: none; } :host([selected]:not([bounce]):not(:is(:state(--no-animate), :--no-animate))) .base, :host([selected][bounce]:not(:is(:state(--no-animate), :--no-animate))) .base { animation: none; } }`, e([w(".base")], Fa.prototype, "_base", void 0), e([w(".state-layer")], Fa.prototype, "_stateLayer", void 0), e([C({
	type: Boolean,
	reflect: !0
})], Fa.prototype, "disabled", void 0), e([C({
	type: Boolean,
	reflect: !0
})], Fa.prototype, "selected", void 0), e([C({
	type: Boolean,
	reflect: !0
})], Fa.prototype, "bounce", void 0), e([C({
	type: Boolean,
	reflect: !0
})], Fa.prototype, "centered", void 0), Fa = e([T("m3e-selection-indicator")], Fa);
var Ia, La, Ra, za, Ba = class extends E(S) {
	constructor() {
		super(...arguments), Ia.add(this), La.set(this, []), this.selectedIndex = null;
	}
	connectedCallback() {
		super.connectedCallback(), Cr(this, "--no-animate");
	}
	update(e) {
		super.update(e), e.has("selectedIndex") && (this.selectedIndex === null && Cr(this, "--no-animate"), t(this, Ia, "m", za).call(this), this.selectedIndex !== null && Sr(this, "--no-animate") && requestAnimationFrame(() => {
			this.selectedIndex !== null && D(this, "--no-animate");
		}));
	}
	render() {
		return b`<slot @slotchange="${t(this, Ia, "m", Ra)}"></slot>`;
	}
};
La = /* @__PURE__ */ new WeakMap(), Ia = /* @__PURE__ */ new WeakSet(), Ra = function(e) {
	let r = [...e.target.assignedElements({ flatten: !0 })];
	for (let e of t(this, La, "f").filter((e) => !r.includes(e))) e.classList.remove("-before"), e.classList.remove("-after"), e.removeAttribute("inert");
	n(this, La, r, "f"), t(this, Ia, "m", za).call(this);
}, za = function() {
	let e = this.selectedIndex ?? t(this, La, "f").length;
	for (let n = 0; n < t(this, La, "f").length; n++) {
		let r = t(this, La, "f")[n];
		r.classList.toggle("-before", n < e), r.classList.toggle("-after", n > e), r.toggleAttribute("inert", n !== e);
	}
}, Ba.styles = l`:host { display: flex; position: relative; overflow: hidden; } :host([hidden]) { display: none; } ::slotted(*) { width: 100%; top: 0; transition: ${c(`inset-inline-start var(--m3e-slide-animation-duration, ${j.motion.duration.long2}) ${j.motion.easing.standard},
        visibility var(--m3e-slide-animation-duration, ${j.motion.duration.long2}) ${j.motion.easing.standard} allow-discrete`)}; } ::slotted(.-before), ::slotted(.-after) { visibility: hidden; position: absolute; } ::slotted(.-before) { inset-inline-start: -100%; } ::slotted(.-after) { inset-inline-start: 100%; } ::slotted(:not(.-before):not(.-after)) { position: relative; inset-inline-start: 0; } :host(:is(:state(--no-animate), :--no-animate)) ::slotted(*) { transition: none; } @media (prefers-reduced-motion) { ::slotted(*) { transition: none; } }`, e([C({
	attribute: "selected-index",
	type: Number,
	reflect: !0
})], Ba.prototype, "selectedIndex", void 0), Ba = e([T("m3e-slide")], Ba);
var Va = {
	hoverColor: c(`var(--m3e-state-layer-hover-color, ${j.color.onSurface})`),
	hoverOpacity: c(`var(--m3e-state-layer-hover-opacity, ${j.state.hoverStateLayerOpacity})`),
	focusColor: c(`var(--m3e-state-layer-focus-color, ${j.color.onSurface})`),
	focusOpacity: c(`var(--m3e-state-layer-focus-opacity, ${j.state.focusStateLayerOpacity})`),
	pressedColor: c(`var(--m3e-state-layer-pressed-color, ${j.color.onSurface})`),
	pressedOpacity: c(`var(--m3e-state-layer-pressed-opacity, ${j.state.pressedStateLayerOpacity})`),
	duration: c(`var(--m3e-state-layer-duration, ${j.motion.duration.medium1})`),
	easing: c(`var(--m3e-state-layer-easing, ${j.motion.easing.standard})`)
}, Ha, Ua, Wa, Ga, Ka, qa, Ja, Ya, Xa, Za = class extends Ai(M(S, "none")) {
	constructor() {
		super(...arguments), Ha.add(this), Ua.set(this, new hn(this, {
			target: null,
			callback: (e) => t(this, Ha, "m", Ka).call(this, e)
		})), Wa.set(this, new nn(this, {
			target: null,
			callback: (e, n) => t(this, Ha, "m", qa).call(this, n)
		})), Ga.set(this, new Yn(this, {
			target: null,
			capture: !0,
			minPressedDuration: 225,
			isPressedKey: (e) => e === " ",
			callback: (e) => t(this, Ha, "m", Ja).call(this, e)
		})), this.disabled = !1, this.disableHover = !1, this.enablePressed = !1;
	}
	show(e) {
		this._layer?.classList.toggle(e, !0);
	}
	hide(e) {
		this._layer?.classList.toggle(e, !1);
	}
	attach(e) {
		super.attach(e), t(this, Wa, "f").observe(e), t(this, Ha, "m", Ya).call(this), t(this, Ha, "m", Xa).call(this);
	}
	detach() {
		this.control && (t(this, Ua, "f").unobserve(this.control), t(this, Wa, "f").unobserve(this.control), t(this, Ga, "f").unobserve(this.control)), super.detach();
	}
	connectedCallback() {
		this.ariaHidden = "true", super.connectedCallback();
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.hide("hover"), this.hide("focused");
	}
	updated(e) {
		super.updated(e), e.has("disabled") && this.disabled && (this.hide("hover"), this.hide("focused")), e.has("disableHover") && this.disableHover && (t(this, Ha, "m", Ya).call(this), this.hide("hover")), e.has("enablePressed") && !this.enablePressed && (t(this, Ha, "m", Xa).call(this), this.hide("pressed"));
	}
	render() {
		return b`<div class="layer"></div>`;
	}
};
Ua = /* @__PURE__ */ new WeakMap(), Wa = /* @__PURE__ */ new WeakMap(), Ga = /* @__PURE__ */ new WeakMap(), Ha = /* @__PURE__ */ new WeakSet(), Ka = function(e) {
	!this.disabled && !this.disableHover && (e ? this.show("hover") : this.hide("hover"));
}, qa = function(e) {
	this.disabled || (e ? this.show("focused") : this.hide("focused"));
}, Ja = function(e) {
	!this.disabled && this.enablePressed && (e ? this.show("pressed") : this.hide("pressed"));
}, Ya = function() {
	this.control && (this.disableHover ? t(this, Ua, "f").unobserve(this.control) : t(this, Ua, "f").observe(this.control));
}, Xa = function() {
	this.control && (this.enablePressed ? t(this, Ga, "f").observe(this.control) : t(this, Ga, "f").unobserve(this.control));
}, Za.styles = l`:host { display: block; } :host([hidden]) { display: none; } :host, .layer { position: absolute; left: 0; top: 0; right: 0; bottom: 0; pointer-events: none; border-radius: inherit; } .layer { contain: layout style paint; will-change: background-color; transition: ${c(`background-color ${Va.duration} ${Va.easing}`)}; } .layer.focused { background-color: color-mix(in srgb, ${Va.focusColor} ${Va.focusOpacity}, transparent); } .layer.hover { background-color: color-mix(in srgb, ${Va.hoverColor} ${Va.hoverOpacity}, transparent); } .layer.pressed { background-color: color-mix( in srgb, ${Va.pressedColor} ${Va.pressedOpacity}, transparent ); } @media (prefers-reduced-motion) { .layer { transition: none; } } @media (forced-colors: active) { .layer { display: none; } }`, e([w(".layer")], Za.prototype, "_layer", void 0), e([C({
	type: Boolean,
	reflect: !0
})], Za.prototype, "disabled", void 0), e([C({
	attribute: "disable-hover",
	type: Boolean,
	reflect: !0
})], Za.prototype, "disableHover", void 0), e([C({
	attribute: "enable-pressed",
	type: Boolean,
	reflect: !0
})], Za.prototype, "enablePressed", void 0), Za = e([T("m3e-state-layer")], Za);
var Qa, $a, eo, to, no, ro, io, ao, oo = ao = class extends S {
	constructor() {
		super(), Qa.add(this), $a.set(this, `m3e-text-highlight-${ao.__nextId++}`), eo.set(this, []), to.set(this, null), this.disabled = !1, this.term = "", this.caseSensitive = !1, this.mode = "contains", this.isSupported && (n(this, to, new CSSStyleSheet(), "f"), t(this, to, "f").replaceSync(l`::highlight(${c(t(this, $a, "f"))}) { background-color: var(--m3e-text-highlight-container-color, Highlight); color: var(--m3e-text-highlight-color, HighlightText); text-decoration: var(--m3e-text-highlight-decoration); text-shadow: var(--m3e-text-highlight-shadow); }`.toString()));
	}
	get isSupported() {
		return !!CSS.highlights;
	}
	get ranges() {
		return t(this, eo, "f");
	}
	firstUpdated(e) {
		super.firstUpdated(e), this.shadowRoot && t(this, to, "f") && !this.shadowRoot.adoptedStyleSheets.includes(t(this, to, "f")) && this.shadowRoot.adoptedStyleSheets.push(t(this, to, "f"));
	}
	updated(e) {
		super.updated(e), (e.has("term") || e.has("caseSensitive") || e.has("disabled")) && t(this, Qa, "m", io).call(this);
	}
	render() {
		return b`<slot @slotchange="${t(this, Qa, "m", io)}"></slot>`;
	}
};
$a = /* @__PURE__ */ new WeakMap(), eo = /* @__PURE__ */ new WeakMap(), to = /* @__PURE__ */ new WeakMap(), Qa = /* @__PURE__ */ new WeakSet(), no = function(e) {
	return !/^\s*$/.test(e.data);
}, ro = function e(n, r) {
	n instanceof HTMLSlotElement ? n.assignedNodes({ flatten: !0 }).forEach((n) => {
		switch (n.nodeType) {
			case Node.TEXT_NODE:
				t(this, Qa, "m", no).call(this, n) && r.push(n);
				break;
			case Node.ELEMENT_NODE: t(this, Qa, "m", e).call(this, n, r);
		}
	}) : n.childNodes.forEach((n) => {
		switch (n.nodeType) {
			case Node.TEXT_NODE:
				t(this, Qa, "m", no).call(this, n) && r.push(n);
				break;
			case Node.ELEMENT_NODE: t(this, Qa, "m", e).call(this, n, r);
		}
	});
}, io = function() {
	if (!this.isSupported || !this.isConnected || (CSS.highlights.delete(t(this, $a, "f")), t(this, eo, "f").length = 0, this.disabled)) return;
	if (!this.term) {
		this.dispatchEvent(new CustomEvent("highlight", {
			detail: [...t(this, eo, "f")],
			bubbles: !1,
			composed: !1
		}));
		return;
	}
	let e = [];
	if (t(this, Qa, "m", ro).call(this, this, e), e.length > 0) {
		let r = this.caseSensitive ? this.term : this.term.toLowerCase();
		switch (this.mode) {
			case "starts-with":
				{
					let n = e[0];
					if (((this.caseSensitive ? n.textContent : n.textContent?.toLowerCase()) ?? "").startsWith(r)) {
						let e = new Range();
						e.setStart(n, 0), e.setEnd(n, r.length), t(this, eo, "f").push(e);
					}
				}
				break;
			case "ends-with":
				{
					let n = e[e.length - 1], i = (this.caseSensitive ? n.textContent : n.textContent?.toLowerCase()) ?? "";
					if (i.endsWith(r)) {
						let e = i.length - r.length, a = e + r.length, o = new Range();
						o.setStart(n, e), o.setEnd(n, a), t(this, eo, "f").push(o);
					}
				}
				break;
			case "contains": n(this, eo, e.map((e) => ({
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
		t(this, eo, "f").length > 0 && CSS.highlights.set(t(this, $a, "f"), new Highlight(...t(this, eo, "f")));
	}
	this.dispatchEvent(new CustomEvent("highlight", {
		detail: [...t(this, eo, "f")],
		bubbles: !1,
		composed: !1
	}));
}, oo.styles = l`:host { display: contents; } :host([hidden]) { display: none; }`, oo.__nextId = 0, e([C({
	type: Boolean,
	reflect: !0
})], oo.prototype, "disabled", void 0), e([C()], oo.prototype, "term", void 0), e([C({
	attribute: "case-sensitive",
	type: Boolean
})], oo.prototype, "caseSensitive", void 0), e([C()], oo.prototype, "mode", void 0), oo = ao = e([T("m3e-text-highlight")], oo);
var so = class extends S {
	render() {
		return b`<span class="base"><slot></slot></span>`;
	}
};
so.styles = l`:host { flex: 1 1 auto; display: inline-flex; align-items: center; flex-wrap: nowrap; min-width: 0; } :host([hidden]) { display: none; } .base { flex: 1 1 auto; display: inline; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }`, so = e([T("m3e-text-overflow")], so);
//#endregion
//#region node_modules/@m3e/web/dist/theme.js
function co(e) {
	return e === "light" || e === "dark" || e === "auto";
}
function lo(e) {
	return e === "high" || e === "medium" || e === "standard";
}
function uo(e) {
	return e < 0 ? -1 : e === 0 ? 0 : 1;
}
function fo(e, t, n) {
	return (1 - n) * e + n * t;
}
function po(e, t, n) {
	return n < e ? e : n > t ? t : n;
}
function mo(e, t, n) {
	return n < e ? e : n > t ? t : n;
}
function ho(e) {
	return e %= 360, e < 0 && (e += 360), e;
}
function go(e) {
	return e %= 360, e < 0 && (e += 360), e;
}
function _o(e, t) {
	return go(t - e) <= 180 ? 1 : -1;
}
function vo(e, t) {
	return 180 - Math.abs(Math.abs(e - t) - 180);
}
function yo(e, t) {
	return [
		e[0] * t[0][0] + e[1] * t[0][1] + e[2] * t[0][2],
		e[0] * t[1][0] + e[1] * t[1][1] + e[2] * t[1][2],
		e[0] * t[2][0] + e[1] * t[2][1] + e[2] * t[2][2]
	];
}
var bo = [
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
], xo = [
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
], So = [
	95.047,
	100,
	108.883
];
function Co(e, t, n) {
	return (255 << 24 | (e & 255) << 16 | (t & 255) << 8 | n & 255) >>> 0;
}
function wo(e) {
	return Co(Io(e[0]), Io(e[1]), Io(e[2]));
}
function To(e) {
	return e >> 16 & 255;
}
function Eo(e) {
	return e >> 8 & 255;
}
function Do(e) {
	return e & 255;
}
function Oo(e, t, n) {
	let r = xo, i = r[0][0] * e + r[0][1] * t + r[0][2] * n, a = r[1][0] * e + r[1][1] * t + r[1][2] * n, o = r[2][0] * e + r[2][1] * t + r[2][2] * n;
	return Co(Io(i), Io(a), Io(o));
}
function ko(e) {
	return yo([
		Fo(To(e)),
		Fo(Eo(e)),
		Fo(Do(e))
	], bo);
}
function Ao(e) {
	let t = Fo(To(e)), n = Fo(Eo(e)), r = Fo(Do(e)), i = bo, a = i[0][0] * t + i[0][1] * n + i[0][2] * r, o = i[1][0] * t + i[1][1] * n + i[1][2] * r, s = i[2][0] * t + i[2][1] * n + i[2][2] * r, c = So, l = a / c[0], u = o / c[1], d = s / c[2], f = Ro(l), p = Ro(u), m = Ro(d);
	return [
		116 * p - 16,
		500 * (f - p),
		200 * (p - m)
	];
}
function jo(e) {
	let t = Io(No(e));
	return Co(t, t, t);
}
function Mo(e) {
	let t = ko(e)[1];
	return 116 * Ro(t / 100) - 16;
}
function No(e) {
	return 100 * zo((e + 16) / 116);
}
function Po(e) {
	return Ro(e / 100) * 116 - 16;
}
function Fo(e) {
	let t = e / 255;
	return t <= .040449936 ? t / 12.92 * 100 : ((t + .055) / 1.055) ** 2.4 * 100;
}
function Io(e) {
	let t = e / 100, n = 0;
	return n = t <= .0031308 ? t * 12.92 : 1.055 * t ** (1 / 2.4) - .055, po(0, 255, Math.round(n * 255));
}
function Lo() {
	return So;
}
function Ro(e) {
	return e > .008856451679035631 ? e ** (1 / 3) : (903.2962962962963 * e + 16) / 116;
}
function zo(e) {
	let t = e * e * e;
	return t > .008856451679035631 ? t : (116 * e - 16) / 903.2962962962963;
}
var Bo = class e {
	static make(t = Lo(), n = 200 / Math.PI * No(50) / 100, r = 50, i = 2, a = !1) {
		let o = t, s = o[0] * .401288 + o[1] * .650173 + o[2] * -.051461, c = o[0] * -.250268 + o[1] * 1.204414 + o[2] * .045854, l = o[0] * -.002079 + o[1] * .048952 + o[2] * .953127, u = .8 + i / 10, d = u >= .9 ? fo(.59, .69, (u - .9) * 10) : fo(.525, .59, (u - .8) * 10), f = a ? 1 : u * (1 - 1 / 3.6 * Math.exp((-n - 42) / 92));
		f = f > 1 ? 1 : f < 0 ? 0 : f;
		let p = u, m = [
			100 / s * f + 1 - f,
			100 / c * f + 1 - f,
			100 / l * f + 1 - f
		], h = 1 / (5 * n + 1), g = h * h * h * h, ee = 1 - g, te = g * n + .1 * ee * ee * Math.cbrt(5 * n), _ = No(r) / t[1], ne = 1.48 + Math.sqrt(_), v = .725 / _ ** .2, re = v, y = [
			(te * m[0] * s / 100) ** .42,
			(te * m[1] * c / 100) ** .42,
			(te * m[2] * l / 100) ** .42
		], ie = [
			400 * y[0] / (y[0] + 27.13),
			400 * y[1] / (y[1] + 27.13),
			400 * y[2] / (y[2] + 27.13)
		], ae = (2 * ie[0] + ie[1] + .05 * ie[2]) * v;
		return new e(_, ae, v, re, d, p, m, te, te ** .25, ne);
	}
	constructor(e, t, n, r, i, a, o, s, c, l) {
		this.n = e, this.aw = t, this.nbb = n, this.ncb = r, this.c = i, this.nc = a, this.rgbD = o, this.fl = s, this.fLRoot = c, this.z = l;
	}
};
Bo.DEFAULT = Bo.make();
var Vo = class e {
	constructor(e, t, n, r, i, a, o, s, c) {
		this.hue = e, this.chroma = t, this.j = n, this.q = r, this.m = i, this.s = a, this.jstar = o, this.astar = s, this.bstar = c;
	}
	distance(e) {
		let t = this.jstar - e.jstar, n = this.astar - e.astar, r = this.bstar - e.bstar;
		return 1.41 * Math.sqrt(t * t + n * n + r * r) ** .63;
	}
	static fromInt(t) {
		return e.fromIntInViewingConditions(t, Bo.DEFAULT);
	}
	static fromIntInViewingConditions(t, n) {
		let r = (t & 16711680) >> 16, i = (t & 65280) >> 8, a = t & 255, o = Fo(r), s = Fo(i), c = Fo(a), l = .41233895 * o + .35762064 * s + .18051042 * c, u = .2126 * o + .7152 * s + .0722 * c, d = .01932141 * o + .11916382 * s + .95034478 * c, f = .401288 * l + .650173 * u - .051461 * d, p = -.250268 * l + 1.204414 * u + .045854 * d, m = -.002079 * l + .048952 * u + .953127 * d, h = n.rgbD[0] * f, g = n.rgbD[1] * p, ee = n.rgbD[2] * m, te = (n.fl * Math.abs(h) / 100) ** .42, _ = (n.fl * Math.abs(g) / 100) ** .42, ne = (n.fl * Math.abs(ee) / 100) ** .42, v = uo(h) * 400 * te / (te + 27.13), re = uo(g) * 400 * _ / (_ + 27.13), y = uo(ee) * 400 * ne / (ne + 27.13), ie = (11 * v + -12 * re + y) / 11, ae = (v + re - 2 * y) / 9, oe = (20 * v + 20 * re + 21 * y) / 20, se = (40 * v + 20 * re + y) / 20, ce = go(Math.atan2(ae, ie) * 180 / Math.PI), le = ce * Math.PI / 180, ue = 100 * (se * n.nbb / n.aw) ** +(n.c * n.z), de = 4 / n.c * Math.sqrt(ue / 100) * (n.aw + 4) * n.fLRoot, fe = ce < 20.14 ? ce + 360 : ce, pe = (5e4 / 13 * (.25 * (Math.cos(fe * Math.PI / 180 + 2) + 3.8)) * n.nc * n.ncb * Math.sqrt(ie * ie + ae * ae) / (oe + .305)) ** .9 * (1.64 - .29 ** n.n) ** .73, me = pe * Math.sqrt(ue / 100), he = me * n.fLRoot, ge = 50 * Math.sqrt(pe * n.c / (n.aw + 4)), _e = (1 + 100 * .007) * ue / (1 + .007 * ue), ve = 1 / .0228 * Math.log(1 + .0228 * he), ye = ve * Math.cos(le), be = ve * Math.sin(le);
		return new e(ce, me, ue, de, he, ge, _e, ye, be);
	}
	static fromJch(t, n, r) {
		return e.fromJchInViewingConditions(t, n, r, Bo.DEFAULT);
	}
	static fromJchInViewingConditions(t, n, r, i) {
		let a = 4 / i.c * Math.sqrt(t / 100) * (i.aw + 4) * i.fLRoot, o = n * i.fLRoot, s = n / Math.sqrt(t / 100), c = 50 * Math.sqrt(s * i.c / (i.aw + 4)), l = r * Math.PI / 180, u = (1 + 100 * .007) * t / (1 + .007 * t), d = 1 / .0228 * Math.log(1 + .0228 * o), f = d * Math.cos(l), p = d * Math.sin(l);
		return new e(r, n, t, a, o, c, u, f, p);
	}
	static fromUcs(t, n, r) {
		return e.fromUcsInViewingConditions(t, n, r, Bo.DEFAULT);
	}
	static fromUcsInViewingConditions(t, n, r, i) {
		let a = n, o = r, s = Math.sqrt(a * a + o * o), c = (Math.exp(s * .0228) - 1) / .0228 / i.fLRoot, l = 180 / Math.PI * Math.atan2(o, a);
		l < 0 && (l += 360);
		let u = t / (1 - (t - 100) * .007);
		return e.fromJchInViewingConditions(u, c, l, i);
	}
	toInt() {
		return this.viewed(Bo.DEFAULT);
	}
	viewed(e) {
		let t = ((this.chroma === 0 || this.j === 0 ? 0 : this.chroma / Math.sqrt(this.j / 100)) / (1.64 - .29 ** e.n) ** .73) ** (1 / .9), n = this.hue * Math.PI / 180, r = .25 * (Math.cos(n + 2) + 3.8), i = e.aw * (this.j / 100) ** (1 / e.c / e.z), a = 5e4 / 13 * r * e.nc * e.ncb, o = i / e.nbb, s = Math.sin(n), c = Math.cos(n), l = 23 * (o + .305) * t / (23 * a + 11 * t * c + 108 * t * s), u = l * c, d = l * s, f = (460 * o + 451 * u + 288 * d) / 1403, p = (460 * o - 891 * u - 261 * d) / 1403, m = (460 * o - 220 * u - 6300 * d) / 1403, h = Math.max(0, 27.13 * Math.abs(f) / (400 - Math.abs(f))), g = uo(f) * (100 / e.fl) * h ** (1 / .42), ee = Math.max(0, 27.13 * Math.abs(p) / (400 - Math.abs(p))), te = uo(p) * (100 / e.fl) * ee ** (1 / .42), _ = Math.max(0, 27.13 * Math.abs(m) / (400 - Math.abs(m))), ne = uo(m) * (100 / e.fl) * _ ** (1 / .42), v = g / e.rgbD[0], re = te / e.rgbD[1], y = ne / e.rgbD[2];
		return Oo(1.86206786 * v - 1.01125463 * re + .14918677 * y, .38752654 * v + .62144744 * re - .00897398 * y, -.0158415 * v - .03412294 * re + 1.04996444 * y);
	}
	static fromXyzInViewingConditions(t, n, r, i) {
		let a = .401288 * t + .650173 * n - .051461 * r, o = -.250268 * t + 1.204414 * n + .045854 * r, s = -.002079 * t + .048952 * n + .953127 * r, c = i.rgbD[0] * a, l = i.rgbD[1] * o, u = i.rgbD[2] * s, d = (i.fl * Math.abs(c) / 100) ** .42, f = (i.fl * Math.abs(l) / 100) ** .42, p = (i.fl * Math.abs(u) / 100) ** .42, m = uo(c) * 400 * d / (d + 27.13), h = uo(l) * 400 * f / (f + 27.13), g = uo(u) * 400 * p / (p + 27.13), ee = (11 * m + -12 * h + g) / 11, te = (m + h - 2 * g) / 9, _ = (20 * m + 20 * h + 21 * g) / 20, ne = (40 * m + 20 * h + g) / 20, v = Math.atan2(te, ee) * 180 / Math.PI, re = v < 0 ? v + 360 : v >= 360 ? v - 360 : v, y = re * Math.PI / 180, ie = 100 * (ne * i.nbb / i.aw) ** +(i.c * i.z), ae = 4 / i.c * Math.sqrt(ie / 100) * (i.aw + 4) * i.fLRoot, oe = re < 20.14 ? re + 360 : re, se = (5e4 / 13 * (1 / 4 * (Math.cos(oe * Math.PI / 180 + 2) + 3.8)) * i.nc * i.ncb * Math.sqrt(ee * ee + te * te) / (_ + .305)) ** .9 * (1.64 - .29 ** i.n) ** .73, ce = se * Math.sqrt(ie / 100), le = ce * i.fLRoot, ue = 50 * Math.sqrt(se * i.c / (i.aw + 4)), de = (1 + 100 * .007) * ie / (1 + .007 * ie), fe = Math.log(1 + .0228 * le) / .0228, pe = fe * Math.cos(y), me = fe * Math.sin(y);
		return new e(re, ce, ie, ae, le, ue, de, pe, me);
	}
	xyzInViewingConditions(e) {
		let t = ((this.chroma === 0 || this.j === 0 ? 0 : this.chroma / Math.sqrt(this.j / 100)) / (1.64 - .29 ** e.n) ** .73) ** (1 / .9), n = this.hue * Math.PI / 180, r = .25 * (Math.cos(n + 2) + 3.8), i = e.aw * (this.j / 100) ** (1 / e.c / e.z), a = 5e4 / 13 * r * e.nc * e.ncb, o = i / e.nbb, s = Math.sin(n), c = Math.cos(n), l = 23 * (o + .305) * t / (23 * a + 11 * t * c + 108 * t * s), u = l * c, d = l * s, f = (460 * o + 451 * u + 288 * d) / 1403, p = (460 * o - 891 * u - 261 * d) / 1403, m = (460 * o - 220 * u - 6300 * d) / 1403, h = Math.max(0, 27.13 * Math.abs(f) / (400 - Math.abs(f))), g = uo(f) * (100 / e.fl) * h ** (1 / .42), ee = Math.max(0, 27.13 * Math.abs(p) / (400 - Math.abs(p))), te = uo(p) * (100 / e.fl) * ee ** (1 / .42), _ = Math.max(0, 27.13 * Math.abs(m) / (400 - Math.abs(m))), ne = uo(m) * (100 / e.fl) * _ ** (1 / .42), v = g / e.rgbD[0], re = te / e.rgbD[1], y = ne / e.rgbD[2];
		return [
			1.86206786 * v - 1.01125463 * re + .14918677 * y,
			.38752654 * v + .62144744 * re - .00897398 * y,
			-.0158415 * v - .03412294 * re + 1.04996444 * y
		];
	}
}, Ho = class e {
	static sanitizeRadians(e) {
		return (e + Math.PI * 8) % (Math.PI * 2);
	}
	static trueDelinearized(e) {
		let t = e / 100, n = 0;
		return n = t <= .0031308 ? t * 12.92 : 1.055 * t ** (1 / 2.4) - .055, n * 255;
	}
	static chromaticAdaptation(e) {
		let t = Math.abs(e) ** .42;
		return uo(e) * 400 * t / (t + 27.13);
	}
	static hueOf(t) {
		let n = yo(t, e.SCALED_DISCOUNT_FROM_LINRGB), r = e.chromaticAdaptation(n[0]), i = e.chromaticAdaptation(n[1]), a = e.chromaticAdaptation(n[2]), o = (11 * r + -12 * i + a) / 11, s = (r + i - 2 * a) / 9;
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
		return uo(e) * n ** (1 / .42);
	}
	static findResultByJ(t, n, r) {
		let i = Math.sqrt(r) * 11, a = Bo.DEFAULT, o = 1 / (1.64 - .29 ** a.n) ** .73, s = .25 * (Math.cos(t + 2) + 3.8) * (5e4 / 13) * a.nc * a.ncb, c = Math.sin(t), l = Math.cos(t);
		for (let t = 0; t < 5; t++) {
			let u = i / 100, d = ((n === 0 || i === 0 ? 0 : n / Math.sqrt(u)) * o) ** (1 / .9), f = a.aw * u ** (1 / a.c / a.z) / a.nbb, p = 23 * (f + .305) * d / (23 * s + 11 * d * l + 108 * d * c), m = p * l, h = p * c, g = (460 * f + 451 * m + 288 * h) / 1403, ee = (460 * f - 891 * m - 261 * h) / 1403, te = (460 * f - 220 * m - 6300 * h) / 1403, _ = yo([
				e.inverseChromaticAdaptation(g),
				e.inverseChromaticAdaptation(ee),
				e.inverseChromaticAdaptation(te)
			], e.LINRGB_FROM_SCALED_DISCOUNT);
			if (_[0] < 0 || _[1] < 0 || _[2] < 0) return 0;
			let ne = e.Y_FROM_LINRGB[0], v = e.Y_FROM_LINRGB[1], re = e.Y_FROM_LINRGB[2], y = ne * _[0] + v * _[1] + re * _[2];
			if (y <= 0) return 0;
			if (t === 4 || Math.abs(y - r) < .002) return _[0] > 100.01 || _[1] > 100.01 || _[2] > 100.01 ? 0 : wo(_);
			i -= (y - r) * i / (2 * y);
		}
		return 0;
	}
	static solveToInt(t, n, r) {
		if (n < 1e-4 || r < 1e-4 || r > 99.9999) return jo(r);
		t = go(t);
		let i = t / 180 * Math.PI, a = No(r), o = e.findResultByJ(i, n, a);
		return o === 0 ? wo(e.bisectToLimit(a, i)) : o;
	}
	static solveToCam(t, n, r) {
		return Vo.fromInt(e.solveToInt(t, n, r));
	}
};
Ho.SCALED_DISCOUNT_FROM_LINRGB = [
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
], Ho.LINRGB_FROM_SCALED_DISCOUNT = [
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
], Ho.Y_FROM_LINRGB = [
	.2126,
	.7152,
	.0722
], Ho.CRITICAL_PLANES = [
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
var N = class e {
	static from(t, n, r) {
		return new e(Ho.solveToInt(t, n, r));
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
		this.setInternalState(Ho.solveToInt(e, this.internalChroma, this.internalTone));
	}
	get chroma() {
		return this.internalChroma;
	}
	set chroma(e) {
		this.setInternalState(Ho.solveToInt(this.internalHue, e, this.internalTone));
	}
	get tone() {
		return this.internalTone;
	}
	set tone(e) {
		this.setInternalState(Ho.solveToInt(this.internalHue, this.internalChroma, e));
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
		let t = Vo.fromInt(e);
		this.internalHue = t.hue, this.internalChroma = t.chroma, this.internalTone = Mo(e), this.argb = e;
	}
	setInternalState(e) {
		let t = Vo.fromInt(e);
		this.internalHue = t.hue, this.internalChroma = t.chroma, this.internalTone = Mo(e), this.argb = e;
	}
	inViewingConditions(t) {
		let n = Vo.fromInt(this.toInt()).xyzInViewingConditions(t), r = Vo.fromXyzInViewingConditions(n[0], n[1], n[2], Bo.make());
		return e.from(r.hue, r.chroma, Po(n[1]));
	}
}, Uo = class e {
	static harmonize(e, t) {
		let n = N.fromInt(e), r = N.fromInt(t), i = vo(n.hue, r.hue), a = Math.min(i * .5, 15), o = go(n.hue + a * _o(n.hue, r.hue));
		return N.from(o, n.chroma, n.tone).toInt();
	}
	static hctHue(t, n, r) {
		let i = e.cam16Ucs(t, n, r), a = Vo.fromInt(i), o = Vo.fromInt(t);
		return N.from(a.hue, o.chroma, Mo(t)).toInt();
	}
	static cam16Ucs(e, t, n) {
		let r = Vo.fromInt(e), i = Vo.fromInt(t), a = r.jstar, o = r.astar, s = r.bstar, c = i.jstar, l = i.astar, u = i.bstar, d = a + (c - a) * n, f = o + (l - o) * n, p = s + (u - s) * n;
		return Vo.fromUcs(d, f, p).toInt();
	}
}, Wo = class e {
	static ratioOfTones(t, n) {
		return t = mo(0, 100, t), n = mo(0, 100, n), e.ratioOfYs(No(t), No(n));
	}
	static ratioOfYs(e, t) {
		let n = e > t ? e : t, r = n === t ? e : t;
		return (n + 5) / (r + 5);
	}
	static lighter(t, n) {
		if (t < 0 || t > 100) return -1;
		let r = No(t), i = n * (r + 5) - 5, a = e.ratioOfYs(i, r), o = Math.abs(a - n);
		if (a < n && o > .04) return -1;
		let s = Po(i) + .4;
		return s < 0 || s > 100 ? -1 : s;
	}
	static darker(t, n) {
		if (t < 0 || t > 100) return -1;
		let r = No(t), i = (r + 5) / n - 5, a = e.ratioOfYs(r, i), o = Math.abs(a - n);
		if (a < n && o > .04) return -1;
		let s = Po(i) - .4;
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
}, Go = class e {
	static isDisliked(e) {
		let t = Math.round(e.hue) >= 90 && Math.round(e.hue) <= 111, n = Math.round(e.chroma) > 16, r = Math.round(e.tone) < 65;
		return t && n && r;
	}
	static fixIfDisliked(t) {
		return e.isDisliked(t) ? N.from(t.hue, t.chroma, 70) : t;
	}
};
function Ko(e, t, n) {
	if (e.name !== n.name) throw Error(`Attempting to extend color ${e.name} with color ${n.name} of different name for spec version ${t}.`);
	if (e.isBackground !== n.isBackground) throw Error(`Attempting to extend color ${e.name} as a ${e.isBackground ? "background" : "foreground"} with color ${n.name} as a ${n.isBackground ? "background" : "foreground"} for spec version ${t}.`);
}
function P(e, t, n) {
	return Ko(e, t, n), F.fromPalette({
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
var F = class e {
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
		let n = Zo(e.specVersion).getHct(e, this);
		return this.hctCache.size > 4 && this.hctCache.clear(), this.hctCache.set(e, n), n;
	}
	getTone(e) {
		return Zo(e.specVersion).getTone(e, this);
	}
	static foregroundTone(t, n) {
		let r = Wo.lighterUnsafe(t, n), i = Wo.darkerUnsafe(t, n), a = Wo.ratioOfTones(r, t), o = Wo.ratioOfTones(i, t);
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
}, qo = class {
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
					Wo.ratioOfTones(t, m) < o && (m = F.foregroundTone(t, o)), Wo.ratioOfTones(t, h) < s && (h = F.foregroundTone(t, s)), n && (m = F.foregroundTone(t, o), h = F.foregroundTone(t, s));
				}
			}
			return (h - m) * p < o && (h = mo(0, 100, m + o * p), (h - m) * p >= o || (m = mo(0, 100, h - o * p))), 50 <= m && m < 60 ? p > 0 ? (m = 60, h = Math.max(h, m + o * p)) : (m = 49, h = Math.min(h, m + o * p)) : 50 <= h && h < 60 && (c ? p > 0 ? (m = 60, h = Math.max(h, m + o * p)) : (m = 49, h = Math.min(h, m + o * p)) : h = p > 0 ? 60 : 49), f ? m : h;
		}
		{
			let r = t.tone(e);
			if (t.background == null || t.background(e) === void 0 || t.contrastCurve == null || t.contrastCurve(e) === void 0) return r;
			let i = t.background(e).getTone(e), a = t.contrastCurve(e).get(e.contrastLevel);
			if (Wo.ratioOfTones(i, r) >= a || (r = F.foregroundTone(i, a)), n && (r = F.foregroundTone(i, a)), t.isBackground && 50 <= r && r < 60 && (r = Wo.ratioOfTones(49, i) >= a ? 49 : 60), t.secondBackground == null || t.secondBackground(e) === void 0) return r;
			let [o, s] = [t.background, t.secondBackground], [c, l] = [o(e).getTone(e), s(e).getTone(e)], [u, d] = [Math.max(c, l), Math.min(c, l)];
			if (Wo.ratioOfTones(u, r) >= a && Wo.ratioOfTones(d, r) >= a) return r;
			let f = Wo.lighter(u, a), p = Wo.darker(d, a), m = [];
			return f !== -1 && m.push(f), p !== -1 && m.push(p), F.tonePrefersLightForeground(c) || F.tonePrefersLightForeground(l) ? f < 0 ? 100 : f : m.length === 1 ? m[0] : p < 0 ? 0 : p;
		}
	}
}, Jo = class {
	getHct(e, t) {
		let n = t.palette(e), r = t.getTone(e), i = n.hue, a = n.chroma * (t.chromaMultiplier ? t.chromaMultiplier(e) : 1);
		return N.from(i, a, r);
	}
	getTone(e, t) {
		let n = t.toneDeltaPair ? t.toneDeltaPair(e) : void 0;
		if (n) {
			let r = n.roleA, i = n.roleB, a = n.polarity, o = n.constraint, s = a === "darker" || a === "relative_lighter" && e.isDark || a === "relative_darker" && !e.isDark ? -n.delta : n.delta, c = t.name === r.name, l = c ? r : i, u = c ? i : r, d = l.tone(e), f = u.getTone(e), p = s * (c ? 1 : -1);
			if (o === "exact" ? d = mo(0, 100, f + p) : o === "nearer" ? d = p > 0 ? mo(0, 100, mo(f, f + p, d)) : mo(0, 100, mo(f + p, f, d)) : o === "farther" && (d = p > 0 ? mo(f + p, 100, d) : mo(0, f + p, d)), t.background && t.contrastCurve) {
				let n = t.background(e), r = t.contrastCurve(e);
				if (n && r) {
					let t = n.getTone(e), i = r.get(e.contrastLevel);
					d = Wo.ratioOfTones(t, d) >= i && e.contrastLevel >= 0 ? d : F.foregroundTone(t, i);
				}
			}
			return t.isBackground && !t.name.endsWith("_fixed_dim") && (d = d >= 57 ? mo(65, 100, d) : mo(0, 49, d)), d;
		}
		{
			let n = t.tone(e);
			if (t.background == null || t.background(e) === void 0 || t.contrastCurve == null || t.contrastCurve(e) === void 0) return n;
			let r = t.background(e).getTone(e), i = t.contrastCurve(e).get(e.contrastLevel);
			if (n = Wo.ratioOfTones(r, n) >= i && e.contrastLevel >= 0 ? n : F.foregroundTone(r, i), t.isBackground && !t.name.endsWith("_fixed_dim") && (n = n >= 57 ? mo(65, 100, n) : mo(0, 49, n)), t.secondBackground == null || t.secondBackground(e) === void 0) return n;
			let [a, o] = [t.background, t.secondBackground], [s, c] = [a(e).getTone(e), o(e).getTone(e)], [l, u] = [Math.max(s, c), Math.min(s, c)];
			if (Wo.ratioOfTones(l, n) >= i && Wo.ratioOfTones(u, n) >= i) return n;
			let d = Wo.lighter(l, i), f = Wo.darker(u, i), p = [];
			return d !== -1 && p.push(d), f !== -1 && p.push(f), F.tonePrefersLightForeground(s) || F.tonePrefersLightForeground(c) ? d < 0 ? 100 : d : p.length === 1 ? p[0] : f < 0 ? 0 : f;
		}
	}
}, Yo = new qo(), Xo = new Jo();
function Zo(e) {
	return e === "2025" ? Xo : Yo;
}
var I = class e {
	static fromInt(t) {
		let n = N.fromInt(t);
		return e.fromHct(n);
	}
	static fromHct(t) {
		return new e(t.hue, t.chroma, t);
	}
	static fromHueAndChroma(t, n) {
		let r = new Qo(t, n).create();
		return new e(t, n, r);
	}
	constructor(e, t, n) {
		this.hue = e, this.chroma = t, this.keyColor = n, this.cache = /* @__PURE__ */ new Map();
	}
	tone(e) {
		let t = this.cache.get(e);
		return t === void 0 && (t = e == 99 && N.isYellow(this.hue) ? this.averageArgb(this.tone(98), this.tone(100)) : N.from(this.hue, this.chroma, e).toInt(), this.cache.set(e, t)), t;
	}
	getHct(e) {
		return N.fromInt(this.tone(e));
	}
	averageArgb(e, t) {
		let n = e >>> 16 & 255, r = e >>> 8 & 255, i = e & 255, a = t >>> 16 & 255, o = t >>> 8 & 255, s = t & 255, c = Math.round((n + a) / 2), l = Math.round((r + o) / 2), u = Math.round((i + s) / 2);
		return (255 << 24 | (c & 255) << 16 | (l & 255) << 8 | u & 255) >>> 0;
	}
}, Qo = class {
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
					if (e === n) return N.from(this.hue, this.requestedChroma, e);
					e = n;
				}
			} else r ? e = n + 1 : t = n;
		}
		return N.from(this.hue, this.requestedChroma, e);
	}
	maxChroma(e) {
		if (this.chromaCache.has(e)) return this.chromaCache.get(e);
		let t = N.from(this.hue, this.maxChromaValue, e).chroma;
		return this.chromaCache.set(e, t), t;
	}
}, $o = class e {
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
			let t = ho(n + e), r = this.hctsByHue[t], a = this.relativeTemperature(r), s = Math.abs(a - i);
			i = a, o += s;
		}
		let s = 1, c = o / t, l = 0;
		for (i = this.relativeTemperature(r); a.length < t;) {
			let e = ho(n + s), r = this.hctsByHue[e], o = this.relativeTemperature(r), u = Math.abs(o - i);
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
			let r = go(o + 1 * t);
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
			let n = N.from(t, this.input.chroma, this.input.tone);
			e.push(n);
		}
		return this.hctsByHueCache = e, this.hctsByHueCache;
	}
	static isBetween(e, t, n) {
		return t < n ? t <= e && e <= n : t <= e || e <= n;
	}
	static rawTemperature(e) {
		let t = Ao(e.toInt()), n = go(Math.atan2(t[2], t[1]) * 180 / Math.PI);
		return -.5 + .02 * Math.sqrt(t[1] * t[1] + t[2] * t[2]) ** 1.07 * Math.cos(go(n - 50) * Math.PI / 180);
	}
}, L = class {
	constructor(e, t, n, r) {
		this.low = e, this.normal = t, this.medium = n, this.high = r;
	}
	get(e) {
		return e <= -1 ? this.low : e < 0 ? fo(this.low, this.normal, (e - -1) / 1) : e < .5 ? fo(this.normal, this.medium, (e - 0) / .5) : e < 1 ? fo(this.medium, this.high, (e - .5) / .5) : this.high;
	}
}, es = class {
	constructor(e, t, n, r, i, a) {
		this.roleA = e, this.roleB = t, this.delta = n, this.polarity = r, this.stayTogether = i, this.constraint = a, this.constraint = a ?? "exact";
	}
}, R;
(function(e) {
	e[e.MONOCHROME = 0] = "MONOCHROME", e[e.NEUTRAL = 1] = "NEUTRAL", e[e.TONAL_SPOT = 2] = "TONAL_SPOT", e[e.VIBRANT = 3] = "VIBRANT", e[e.EXPRESSIVE = 4] = "EXPRESSIVE", e[e.FIDELITY = 5] = "FIDELITY", e[e.CONTENT = 6] = "CONTENT", e[e.RAINBOW = 7] = "RAINBOW", e[e.FRUIT_SALAD = 8] = "FRUIT_SALAD";
})(R ||= {});
function ts(e) {
	return e.variant === R.FIDELITY || e.variant === R.CONTENT;
}
function ns(e) {
	return e.variant === R.MONOCHROME;
}
function rs(e, t, n, r) {
	let i = n, a = N.from(e, t, n);
	if (a.chroma < t) {
		let n = a.chroma;
		for (; a.chroma < t;) {
			i += r ? -1 : 1;
			let o = N.from(e, t, i);
			if (n > o.chroma || Math.abs(o.chroma - t) < .4) break;
			Math.abs(o.chroma - t) < Math.abs(a.chroma - t) && (a = o), n = Math.max(n, o.chroma);
		}
	}
	return i;
}
var is = class {
	primaryPaletteKeyColor() {
		return F.fromPalette({
			name: "primary_palette_key_color",
			palette: (e) => e.primaryPalette,
			tone: (e) => e.primaryPalette.keyColor.tone
		});
	}
	secondaryPaletteKeyColor() {
		return F.fromPalette({
			name: "secondary_palette_key_color",
			palette: (e) => e.secondaryPalette,
			tone: (e) => e.secondaryPalette.keyColor.tone
		});
	}
	tertiaryPaletteKeyColor() {
		return F.fromPalette({
			name: "tertiary_palette_key_color",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => e.tertiaryPalette.keyColor.tone
		});
	}
	neutralPaletteKeyColor() {
		return F.fromPalette({
			name: "neutral_palette_key_color",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.neutralPalette.keyColor.tone
		});
	}
	neutralVariantPaletteKeyColor() {
		return F.fromPalette({
			name: "neutral_variant_palette_key_color",
			palette: (e) => e.neutralVariantPalette,
			tone: (e) => e.neutralVariantPalette.keyColor.tone
		});
	}
	errorPaletteKeyColor() {
		return F.fromPalette({
			name: "error_palette_key_color",
			palette: (e) => e.errorPalette,
			tone: (e) => e.errorPalette.keyColor.tone
		});
	}
	background() {
		return F.fromPalette({
			name: "background",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 6 : 98,
			isBackground: !0
		});
	}
	onBackground() {
		return F.fromPalette({
			name: "on_background",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 90 : 10,
			background: (e) => this.background(),
			contrastCurve: (e) => new L(3, 3, 4.5, 7)
		});
	}
	surface() {
		return F.fromPalette({
			name: "surface",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 6 : 98,
			isBackground: !0
		});
	}
	surfaceDim() {
		return F.fromPalette({
			name: "surface_dim",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 6 : new L(87, 87, 80, 75).get(e.contrastLevel),
			isBackground: !0
		});
	}
	surfaceBright() {
		return F.fromPalette({
			name: "surface_bright",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? new L(24, 24, 29, 34).get(e.contrastLevel) : 98,
			isBackground: !0
		});
	}
	surfaceContainerLowest() {
		return F.fromPalette({
			name: "surface_container_lowest",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? new L(4, 4, 2, 0).get(e.contrastLevel) : 100,
			isBackground: !0
		});
	}
	surfaceContainerLow() {
		return F.fromPalette({
			name: "surface_container_low",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? new L(10, 10, 11, 12).get(e.contrastLevel) : new L(96, 96, 96, 95).get(e.contrastLevel),
			isBackground: !0
		});
	}
	surfaceContainer() {
		return F.fromPalette({
			name: "surface_container",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? new L(12, 12, 16, 20).get(e.contrastLevel) : new L(94, 94, 92, 90).get(e.contrastLevel),
			isBackground: !0
		});
	}
	surfaceContainerHigh() {
		return F.fromPalette({
			name: "surface_container_high",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? new L(17, 17, 21, 25).get(e.contrastLevel) : new L(92, 92, 88, 85).get(e.contrastLevel),
			isBackground: !0
		});
	}
	surfaceContainerHighest() {
		return F.fromPalette({
			name: "surface_container_highest",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? new L(22, 22, 26, 30).get(e.contrastLevel) : new L(90, 90, 84, 80).get(e.contrastLevel),
			isBackground: !0
		});
	}
	onSurface() {
		return F.fromPalette({
			name: "on_surface",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 90 : 10,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new L(4.5, 7, 11, 21)
		});
	}
	surfaceVariant() {
		return F.fromPalette({
			name: "surface_variant",
			palette: (e) => e.neutralVariantPalette,
			tone: (e) => e.isDark ? 30 : 90,
			isBackground: !0
		});
	}
	onSurfaceVariant() {
		return F.fromPalette({
			name: "on_surface_variant",
			palette: (e) => e.neutralVariantPalette,
			tone: (e) => e.isDark ? 80 : 30,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new L(3, 4.5, 7, 11)
		});
	}
	inverseSurface() {
		return F.fromPalette({
			name: "inverse_surface",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 90 : 20,
			isBackground: !0
		});
	}
	inverseOnSurface() {
		return F.fromPalette({
			name: "inverse_on_surface",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 20 : 95,
			background: (e) => this.inverseSurface(),
			contrastCurve: (e) => new L(4.5, 7, 11, 21)
		});
	}
	outline() {
		return F.fromPalette({
			name: "outline",
			palette: (e) => e.neutralVariantPalette,
			tone: (e) => e.isDark ? 60 : 50,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new L(1.5, 3, 4.5, 7)
		});
	}
	outlineVariant() {
		return F.fromPalette({
			name: "outline_variant",
			palette: (e) => e.neutralVariantPalette,
			tone: (e) => e.isDark ? 30 : 80,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new L(1, 1, 3, 4.5)
		});
	}
	shadow() {
		return F.fromPalette({
			name: "shadow",
			palette: (e) => e.neutralPalette,
			tone: (e) => 0
		});
	}
	scrim() {
		return F.fromPalette({
			name: "scrim",
			palette: (e) => e.neutralPalette,
			tone: (e) => 0
		});
	}
	surfaceTint() {
		return F.fromPalette({
			name: "surface_tint",
			palette: (e) => e.primaryPalette,
			tone: (e) => e.isDark ? 80 : 40,
			isBackground: !0
		});
	}
	primary() {
		return F.fromPalette({
			name: "primary",
			palette: (e) => e.primaryPalette,
			tone: (e) => ns(e) ? e.isDark ? 100 : 0 : e.isDark ? 80 : 40,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new L(3, 4.5, 7, 7),
			toneDeltaPair: (e) => new es(this.primaryContainer(), this.primary(), 10, "nearer", !1)
		});
	}
	primaryDim() {}
	onPrimary() {
		return F.fromPalette({
			name: "on_primary",
			palette: (e) => e.primaryPalette,
			tone: (e) => ns(e) ? e.isDark ? 10 : 90 : e.isDark ? 20 : 100,
			background: (e) => this.primary(),
			contrastCurve: (e) => new L(4.5, 7, 11, 21)
		});
	}
	primaryContainer() {
		return F.fromPalette({
			name: "primary_container",
			palette: (e) => e.primaryPalette,
			tone: (e) => ts(e) ? e.sourceColorHct.tone : ns(e) ? e.isDark ? 85 : 25 : e.isDark ? 30 : 90,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new L(1, 1, 3, 4.5),
			toneDeltaPair: (e) => new es(this.primaryContainer(), this.primary(), 10, "nearer", !1)
		});
	}
	onPrimaryContainer() {
		return F.fromPalette({
			name: "on_primary_container",
			palette: (e) => e.primaryPalette,
			tone: (e) => ts(e) ? F.foregroundTone(this.primaryContainer().tone(e), 4.5) : ns(e) ? e.isDark ? 0 : 100 : e.isDark ? 90 : 30,
			background: (e) => this.primaryContainer(),
			contrastCurve: (e) => new L(3, 4.5, 7, 11)
		});
	}
	inversePrimary() {
		return F.fromPalette({
			name: "inverse_primary",
			palette: (e) => e.primaryPalette,
			tone: (e) => e.isDark ? 40 : 80,
			background: (e) => this.inverseSurface(),
			contrastCurve: (e) => new L(3, 4.5, 7, 7)
		});
	}
	secondary() {
		return F.fromPalette({
			name: "secondary",
			palette: (e) => e.secondaryPalette,
			tone: (e) => e.isDark ? 80 : 40,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new L(3, 4.5, 7, 7),
			toneDeltaPair: (e) => new es(this.secondaryContainer(), this.secondary(), 10, "nearer", !1)
		});
	}
	secondaryDim() {}
	onSecondary() {
		return F.fromPalette({
			name: "on_secondary",
			palette: (e) => e.secondaryPalette,
			tone: (e) => ns(e) ? e.isDark ? 10 : 100 : e.isDark ? 20 : 100,
			background: (e) => this.secondary(),
			contrastCurve: (e) => new L(4.5, 7, 11, 21)
		});
	}
	secondaryContainer() {
		return F.fromPalette({
			name: "secondary_container",
			palette: (e) => e.secondaryPalette,
			tone: (e) => {
				let t = e.isDark ? 30 : 90;
				return ns(e) ? e.isDark ? 30 : 85 : ts(e) ? rs(e.secondaryPalette.hue, e.secondaryPalette.chroma, t, !e.isDark) : t;
			},
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new L(1, 1, 3, 4.5),
			toneDeltaPair: (e) => new es(this.secondaryContainer(), this.secondary(), 10, "nearer", !1)
		});
	}
	onSecondaryContainer() {
		return F.fromPalette({
			name: "on_secondary_container",
			palette: (e) => e.secondaryPalette,
			tone: (e) => ns(e) ? e.isDark ? 90 : 10 : ts(e) ? F.foregroundTone(this.secondaryContainer().tone(e), 4.5) : e.isDark ? 90 : 30,
			background: (e) => this.secondaryContainer(),
			contrastCurve: (e) => new L(3, 4.5, 7, 11)
		});
	}
	tertiary() {
		return F.fromPalette({
			name: "tertiary",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => ns(e) ? e.isDark ? 90 : 25 : e.isDark ? 80 : 40,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new L(3, 4.5, 7, 7),
			toneDeltaPair: (e) => new es(this.tertiaryContainer(), this.tertiary(), 10, "nearer", !1)
		});
	}
	tertiaryDim() {}
	onTertiary() {
		return F.fromPalette({
			name: "on_tertiary",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => ns(e) ? e.isDark ? 10 : 90 : e.isDark ? 20 : 100,
			background: (e) => this.tertiary(),
			contrastCurve: (e) => new L(4.5, 7, 11, 21)
		});
	}
	tertiaryContainer() {
		return F.fromPalette({
			name: "tertiary_container",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => {
				if (ns(e)) return e.isDark ? 60 : 49;
				if (!ts(e)) return e.isDark ? 30 : 90;
				let t = e.tertiaryPalette.getHct(e.sourceColorHct.tone);
				return Go.fixIfDisliked(t).tone;
			},
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new L(1, 1, 3, 4.5),
			toneDeltaPair: (e) => new es(this.tertiaryContainer(), this.tertiary(), 10, "nearer", !1)
		});
	}
	onTertiaryContainer() {
		return F.fromPalette({
			name: "on_tertiary_container",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => ns(e) ? e.isDark ? 0 : 100 : ts(e) ? F.foregroundTone(this.tertiaryContainer().tone(e), 4.5) : e.isDark ? 90 : 30,
			background: (e) => this.tertiaryContainer(),
			contrastCurve: (e) => new L(3, 4.5, 7, 11)
		});
	}
	error() {
		return F.fromPalette({
			name: "error",
			palette: (e) => e.errorPalette,
			tone: (e) => e.isDark ? 80 : 40,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new L(3, 4.5, 7, 7),
			toneDeltaPair: (e) => new es(this.errorContainer(), this.error(), 10, "nearer", !1)
		});
	}
	errorDim() {}
	onError() {
		return F.fromPalette({
			name: "on_error",
			palette: (e) => e.errorPalette,
			tone: (e) => e.isDark ? 20 : 100,
			background: (e) => this.error(),
			contrastCurve: (e) => new L(4.5, 7, 11, 21)
		});
	}
	errorContainer() {
		return F.fromPalette({
			name: "error_container",
			palette: (e) => e.errorPalette,
			tone: (e) => e.isDark ? 30 : 90,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new L(1, 1, 3, 4.5),
			toneDeltaPair: (e) => new es(this.errorContainer(), this.error(), 10, "nearer", !1)
		});
	}
	onErrorContainer() {
		return F.fromPalette({
			name: "on_error_container",
			palette: (e) => e.errorPalette,
			tone: (e) => ns(e) ? e.isDark ? 90 : 10 : e.isDark ? 90 : 30,
			background: (e) => this.errorContainer(),
			contrastCurve: (e) => new L(3, 4.5, 7, 11)
		});
	}
	primaryFixed() {
		return F.fromPalette({
			name: "primary_fixed",
			palette: (e) => e.primaryPalette,
			tone: (e) => ns(e) ? 40 : 90,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new L(1, 1, 3, 4.5),
			toneDeltaPair: (e) => new es(this.primaryFixed(), this.primaryFixedDim(), 10, "lighter", !0)
		});
	}
	primaryFixedDim() {
		return F.fromPalette({
			name: "primary_fixed_dim",
			palette: (e) => e.primaryPalette,
			tone: (e) => ns(e) ? 30 : 80,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new L(1, 1, 3, 4.5),
			toneDeltaPair: (e) => new es(this.primaryFixed(), this.primaryFixedDim(), 10, "lighter", !0)
		});
	}
	onPrimaryFixed() {
		return F.fromPalette({
			name: "on_primary_fixed",
			palette: (e) => e.primaryPalette,
			tone: (e) => ns(e) ? 100 : 10,
			background: (e) => this.primaryFixedDim(),
			secondBackground: (e) => this.primaryFixed(),
			contrastCurve: (e) => new L(4.5, 7, 11, 21)
		});
	}
	onPrimaryFixedVariant() {
		return F.fromPalette({
			name: "on_primary_fixed_variant",
			palette: (e) => e.primaryPalette,
			tone: (e) => ns(e) ? 90 : 30,
			background: (e) => this.primaryFixedDim(),
			secondBackground: (e) => this.primaryFixed(),
			contrastCurve: (e) => new L(3, 4.5, 7, 11)
		});
	}
	secondaryFixed() {
		return F.fromPalette({
			name: "secondary_fixed",
			palette: (e) => e.secondaryPalette,
			tone: (e) => ns(e) ? 80 : 90,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new L(1, 1, 3, 4.5),
			toneDeltaPair: (e) => new es(this.secondaryFixed(), this.secondaryFixedDim(), 10, "lighter", !0)
		});
	}
	secondaryFixedDim() {
		return F.fromPalette({
			name: "secondary_fixed_dim",
			palette: (e) => e.secondaryPalette,
			tone: (e) => ns(e) ? 70 : 80,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new L(1, 1, 3, 4.5),
			toneDeltaPair: (e) => new es(this.secondaryFixed(), this.secondaryFixedDim(), 10, "lighter", !0)
		});
	}
	onSecondaryFixed() {
		return F.fromPalette({
			name: "on_secondary_fixed",
			palette: (e) => e.secondaryPalette,
			tone: (e) => 10,
			background: (e) => this.secondaryFixedDim(),
			secondBackground: (e) => this.secondaryFixed(),
			contrastCurve: (e) => new L(4.5, 7, 11, 21)
		});
	}
	onSecondaryFixedVariant() {
		return F.fromPalette({
			name: "on_secondary_fixed_variant",
			palette: (e) => e.secondaryPalette,
			tone: (e) => ns(e) ? 25 : 30,
			background: (e) => this.secondaryFixedDim(),
			secondBackground: (e) => this.secondaryFixed(),
			contrastCurve: (e) => new L(3, 4.5, 7, 11)
		});
	}
	tertiaryFixed() {
		return F.fromPalette({
			name: "tertiary_fixed",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => ns(e) ? 40 : 90,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new L(1, 1, 3, 4.5),
			toneDeltaPair: (e) => new es(this.tertiaryFixed(), this.tertiaryFixedDim(), 10, "lighter", !0)
		});
	}
	tertiaryFixedDim() {
		return F.fromPalette({
			name: "tertiary_fixed_dim",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => ns(e) ? 30 : 80,
			isBackground: !0,
			background: (e) => this.highestSurface(e),
			contrastCurve: (e) => new L(1, 1, 3, 4.5),
			toneDeltaPair: (e) => new es(this.tertiaryFixed(), this.tertiaryFixedDim(), 10, "lighter", !0)
		});
	}
	onTertiaryFixed() {
		return F.fromPalette({
			name: "on_tertiary_fixed",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => ns(e) ? 100 : 10,
			background: (e) => this.tertiaryFixedDim(),
			secondBackground: (e) => this.tertiaryFixed(),
			contrastCurve: (e) => new L(4.5, 7, 11, 21)
		});
	}
	onTertiaryFixedVariant() {
		return F.fromPalette({
			name: "on_tertiary_fixed_variant",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => ns(e) ? 90 : 30,
			background: (e) => this.tertiaryFixedDim(),
			secondBackground: (e) => this.tertiaryFixed(),
			contrastCurve: (e) => new L(3, 4.5, 7, 11)
		});
	}
	highestSurface(e) {
		return e.isDark ? this.surfaceBright() : this.surfaceDim();
	}
};
function z(e, t = 0, n = 100, r = 1) {
	return mo(t, n, os(e.hue, e.chroma * r, 100, !0));
}
function as(e, t = 0, n = 100) {
	return mo(t, n, os(e.hue, e.chroma, 0, !1));
}
function os(e, t, n, r) {
	let i = n, a = N.from(e, t, i);
	for (; a.chroma < t && !(n < 0 || n > 100);) {
		n += r ? -1 : 1;
		let o = N.from(e, t, n);
		a.chroma < o.chroma && (a = o, i = n);
	}
	return i;
}
function B(e) {
	return e === 1.5 ? new L(1.5, 1.5, 3, 5.5) : e === 3 ? new L(3, 3, 4.5, 7) : e === 4.5 ? new L(4.5, 4.5, 7, 11) : e === 6 ? new L(6, 6, 7, 11) : e === 7 ? new L(7, 7, 11, 21) : e === 9 ? new L(9, 9, 11, 21) : e === 11 ? new L(11, 11, 21, 21) : e === 21 ? new L(21, 21, 21, 21) : new L(e, e, 7, 21);
}
var ss = class extends is {
	surface() {
		let e = F.fromPalette({
			name: "surface",
			palette: (e) => e.neutralPalette,
			tone: (e) => (super.surface().tone(e), e.platform === "phone" ? e.isDark ? 4 : N.isYellow(e.neutralPalette.hue) ? 99 : e.variant === R.VIBRANT ? 97 : 98 : 0),
			isBackground: !0
		});
		return P(super.surface(), "2025", e);
	}
	surfaceDim() {
		let e = F.fromPalette({
			name: "surface_dim",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 4 : N.isYellow(e.neutralPalette.hue) ? 90 : e.variant === R.VIBRANT ? 85 : 87,
			isBackground: !0,
			chromaMultiplier: (e) => {
				if (!e.isDark) {
					if (e.variant === R.NEUTRAL) return 2.5;
					if (e.variant === R.TONAL_SPOT) return 1.7;
					if (e.variant === R.EXPRESSIVE) return N.isYellow(e.neutralPalette.hue) ? 2.7 : 1.75;
					if (e.variant === R.VIBRANT) return 1.36;
				}
				return 1;
			}
		});
		return P(super.surfaceDim(), "2025", e);
	}
	surfaceBright() {
		let e = F.fromPalette({
			name: "surface_bright",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 18 : N.isYellow(e.neutralPalette.hue) ? 99 : e.variant === R.VIBRANT ? 97 : 98,
			isBackground: !0,
			chromaMultiplier: (e) => {
				if (e.isDark) {
					if (e.variant === R.NEUTRAL) return 2.5;
					if (e.variant === R.TONAL_SPOT) return 1.7;
					if (e.variant === R.EXPRESSIVE) return N.isYellow(e.neutralPalette.hue) ? 2.7 : 1.75;
					if (e.variant === R.VIBRANT) return 1.36;
				}
				return 1;
			}
		});
		return P(super.surfaceBright(), "2025", e);
	}
	surfaceContainerLowest() {
		let e = F.fromPalette({
			name: "surface_container_lowest",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 0 : 100,
			isBackground: !0
		});
		return P(super.surfaceContainerLowest(), "2025", e);
	}
	surfaceContainerLow() {
		let e = F.fromPalette({
			name: "surface_container_low",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.platform === "phone" ? e.isDark ? 6 : N.isYellow(e.neutralPalette.hue) ? 98 : e.variant === R.VIBRANT ? 95 : 96 : 15,
			isBackground: !0,
			chromaMultiplier: (e) => {
				if (e.platform === "phone") {
					if (e.variant === R.NEUTRAL) return 1.3;
					if (e.variant === R.TONAL_SPOT) return 1.25;
					if (e.variant === R.EXPRESSIVE) return N.isYellow(e.neutralPalette.hue) ? 1.3 : 1.15;
					if (e.variant === R.VIBRANT) return 1.08;
				}
				return 1;
			}
		});
		return P(super.surfaceContainerLow(), "2025", e);
	}
	surfaceContainer() {
		let e = F.fromPalette({
			name: "surface_container",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.platform === "phone" ? e.isDark ? 9 : N.isYellow(e.neutralPalette.hue) ? 96 : e.variant === R.VIBRANT ? 92 : 94 : 20,
			isBackground: !0,
			chromaMultiplier: (e) => {
				if (e.platform === "phone") {
					if (e.variant === R.NEUTRAL) return 1.6;
					if (e.variant === R.TONAL_SPOT) return 1.4;
					if (e.variant === R.EXPRESSIVE) return N.isYellow(e.neutralPalette.hue) ? 1.6 : 1.3;
					if (e.variant === R.VIBRANT) return 1.15;
				}
				return 1;
			}
		});
		return P(super.surfaceContainer(), "2025", e);
	}
	surfaceContainerHigh() {
		let e = F.fromPalette({
			name: "surface_container_high",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.platform === "phone" ? e.isDark ? 12 : N.isYellow(e.neutralPalette.hue) ? 94 : e.variant === R.VIBRANT ? 90 : 92 : 25,
			isBackground: !0,
			chromaMultiplier: (e) => {
				if (e.platform === "phone") {
					if (e.variant === R.NEUTRAL) return 1.9;
					if (e.variant === R.TONAL_SPOT) return 1.5;
					if (e.variant === R.EXPRESSIVE) return N.isYellow(e.neutralPalette.hue) ? 1.95 : 1.45;
					if (e.variant === R.VIBRANT) return 1.22;
				}
				return 1;
			}
		});
		return P(super.surfaceContainerHigh(), "2025", e);
	}
	surfaceContainerHighest() {
		let e = F.fromPalette({
			name: "surface_container_highest",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 15 : N.isYellow(e.neutralPalette.hue) ? 92 : e.variant === R.VIBRANT ? 88 : 90,
			isBackground: !0,
			chromaMultiplier: (e) => e.variant === R.NEUTRAL ? 2.2 : e.variant === R.TONAL_SPOT ? 1.7 : e.variant === R.EXPRESSIVE ? N.isYellow(e.neutralPalette.hue) ? 2.3 : 1.6 : e.variant === R.VIBRANT ? 1.29 : 1
		});
		return P(super.surfaceContainerHighest(), "2025", e);
	}
	onSurface() {
		let e = F.fromPalette({
			name: "on_surface",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.variant === R.VIBRANT ? z(e.neutralPalette, 0, 100, 1.1) : F.getInitialToneFromBackground((e) => e.platform === "phone" ? this.highestSurface(e) : this.surfaceContainerHigh())(e),
			chromaMultiplier: (e) => {
				if (e.platform === "phone") {
					if (e.variant === R.NEUTRAL) return 2.2;
					if (e.variant === R.TONAL_SPOT) return 1.7;
					if (e.variant === R.EXPRESSIVE) return N.isYellow(e.neutralPalette.hue) ? e.isDark ? 3 : 2.3 : 1.6;
				}
				return 1;
			},
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : this.surfaceContainerHigh(),
			contrastCurve: (e) => e.isDark && e.platform === "phone" ? B(11) : B(9)
		});
		return P(super.onSurface(), "2025", e);
	}
	onSurfaceVariant() {
		let e = F.fromPalette({
			name: "on_surface_variant",
			palette: (e) => e.neutralPalette,
			chromaMultiplier: (e) => {
				if (e.platform === "phone") {
					if (e.variant === R.NEUTRAL) return 2.2;
					if (e.variant === R.TONAL_SPOT) return 1.7;
					if (e.variant === R.EXPRESSIVE) return N.isYellow(e.neutralPalette.hue) ? e.isDark ? 3 : 2.3 : 1.6;
				}
				return 1;
			},
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : this.surfaceContainerHigh(),
			contrastCurve: (e) => e.platform === "phone" ? e.isDark ? B(6) : B(4.5) : B(7)
		});
		return P(super.onSurfaceVariant(), "2025", e);
	}
	outline() {
		let e = F.fromPalette({
			name: "outline",
			palette: (e) => e.neutralPalette,
			chromaMultiplier: (e) => {
				if (e.platform === "phone") {
					if (e.variant === R.NEUTRAL) return 2.2;
					if (e.variant === R.TONAL_SPOT) return 1.7;
					if (e.variant === R.EXPRESSIVE) return N.isYellow(e.neutralPalette.hue) ? e.isDark ? 3 : 2.3 : 1.6;
				}
				return 1;
			},
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : this.surfaceContainerHigh(),
			contrastCurve: (e) => e.platform === "phone" ? B(3) : B(4.5)
		});
		return P(super.outline(), "2025", e);
	}
	outlineVariant() {
		let e = F.fromPalette({
			name: "outline_variant",
			palette: (e) => e.neutralPalette,
			chromaMultiplier: (e) => {
				if (e.platform === "phone") {
					if (e.variant === R.NEUTRAL) return 2.2;
					if (e.variant === R.TONAL_SPOT) return 1.7;
					if (e.variant === R.EXPRESSIVE) return N.isYellow(e.neutralPalette.hue) ? e.isDark ? 3 : 2.3 : 1.6;
				}
				return 1;
			},
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : this.surfaceContainerHigh(),
			contrastCurve: (e) => e.platform === "phone" ? B(1.5) : B(3)
		});
		return P(super.outlineVariant(), "2025", e);
	}
	inverseSurface() {
		let e = F.fromPalette({
			name: "inverse_surface",
			palette: (e) => e.neutralPalette,
			tone: (e) => e.isDark ? 98 : 4,
			isBackground: !0
		});
		return P(super.inverseSurface(), "2025", e);
	}
	inverseOnSurface() {
		let e = F.fromPalette({
			name: "inverse_on_surface",
			palette: (e) => e.neutralPalette,
			background: (e) => this.inverseSurface(),
			contrastCurve: (e) => B(7)
		});
		return P(super.inverseOnSurface(), "2025", e);
	}
	primary() {
		let e = F.fromPalette({
			name: "primary",
			palette: (e) => e.primaryPalette,
			tone: (e) => e.variant === R.NEUTRAL ? e.platform === "phone" ? e.isDark ? 80 : 40 : 90 : e.variant === R.TONAL_SPOT ? e.platform === "phone" ? e.isDark ? 80 : z(e.primaryPalette) : z(e.primaryPalette, 0, 90) : e.variant === R.EXPRESSIVE ? e.platform === "phone" ? z(e.primaryPalette, 0, N.isYellow(e.primaryPalette.hue) ? 25 : N.isCyan(e.primaryPalette.hue) ? 88 : 98) : z(e.primaryPalette) : e.platform === "phone" ? z(e.primaryPalette, 0, N.isCyan(e.primaryPalette.hue) ? 88 : 98) : z(e.primaryPalette),
			isBackground: !0,
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : this.surfaceContainerHigh(),
			contrastCurve: (e) => e.platform === "phone" ? B(4.5) : B(7),
			toneDeltaPair: (e) => e.platform === "phone" ? new es(this.primaryContainer(), this.primary(), 5, "relative_lighter", !0, "farther") : void 0
		});
		return P(super.primary(), "2025", e);
	}
	primaryDim() {
		return F.fromPalette({
			name: "primary_dim",
			palette: (e) => e.primaryPalette,
			tone: (e) => e.variant === R.NEUTRAL ? 85 : e.variant === R.TONAL_SPOT ? z(e.primaryPalette, 0, 90) : z(e.primaryPalette),
			isBackground: !0,
			background: (e) => this.surfaceContainerHigh(),
			contrastCurve: (e) => B(4.5),
			toneDeltaPair: (e) => new es(this.primaryDim(), this.primary(), 5, "darker", !0, "farther")
		});
	}
	onPrimary() {
		let e = F.fromPalette({
			name: "on_primary",
			palette: (e) => e.primaryPalette,
			background: (e) => e.platform === "phone" ? this.primary() : this.primaryDim(),
			contrastCurve: (e) => e.platform === "phone" ? B(6) : B(7)
		});
		return P(super.onPrimary(), "2025", e);
	}
	primaryContainer() {
		let e = F.fromPalette({
			name: "primary_container",
			palette: (e) => e.primaryPalette,
			tone: (e) => e.platform === "watch" ? 30 : e.variant === R.NEUTRAL ? e.isDark ? 30 : 90 : e.variant === R.TONAL_SPOT ? e.isDark ? as(e.primaryPalette, 35, 93) : z(e.primaryPalette, 0, 90) : e.variant === R.EXPRESSIVE ? e.isDark ? z(e.primaryPalette, 30, 93) : z(e.primaryPalette, 78, N.isCyan(e.primaryPalette.hue) ? 88 : 90) : e.isDark ? as(e.primaryPalette, 66, 93) : z(e.primaryPalette, 66, N.isCyan(e.primaryPalette.hue) ? 88 : 93),
			isBackground: !0,
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : void 0,
			toneDeltaPair: (e) => e.platform === "phone" ? void 0 : new es(this.primaryContainer(), this.primaryDim(), 10, "darker", !0, "farther"),
			contrastCurve: (e) => e.platform === "phone" && e.contrastLevel > 0 ? B(1.5) : void 0
		});
		return P(super.primaryContainer(), "2025", e);
	}
	onPrimaryContainer() {
		let e = F.fromPalette({
			name: "on_primary_container",
			palette: (e) => e.primaryPalette,
			background: (e) => this.primaryContainer(),
			contrastCurve: (e) => e.platform === "phone" ? B(6) : B(7)
		});
		return P(super.onPrimaryContainer(), "2025", e);
	}
	primaryFixed() {
		let e = F.fromPalette({
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
			contrastCurve: (e) => e.platform === "phone" && e.contrastLevel > 0 ? B(1.5) : void 0
		});
		return P(super.primaryFixed(), "2025", e);
	}
	primaryFixedDim() {
		let e = F.fromPalette({
			name: "primary_fixed_dim",
			palette: (e) => e.primaryPalette,
			tone: (e) => this.primaryFixed().getTone(e),
			isBackground: !0,
			toneDeltaPair: (e) => new es(this.primaryFixedDim(), this.primaryFixed(), 5, "darker", !0, "exact")
		});
		return P(super.primaryFixedDim(), "2025", e);
	}
	onPrimaryFixed() {
		let e = F.fromPalette({
			name: "on_primary_fixed",
			palette: (e) => e.primaryPalette,
			background: (e) => this.primaryFixedDim(),
			contrastCurve: (e) => B(7)
		});
		return P(super.onPrimaryFixed(), "2025", e);
	}
	onPrimaryFixedVariant() {
		let e = F.fromPalette({
			name: "on_primary_fixed_variant",
			palette: (e) => e.primaryPalette,
			background: (e) => this.primaryFixedDim(),
			contrastCurve: (e) => B(4.5)
		});
		return P(super.onPrimaryFixedVariant(), "2025", e);
	}
	inversePrimary() {
		let e = F.fromPalette({
			name: "inverse_primary",
			palette: (e) => e.primaryPalette,
			tone: (e) => z(e.primaryPalette),
			background: (e) => this.inverseSurface(),
			contrastCurve: (e) => e.platform === "phone" ? B(6) : B(7)
		});
		return P(super.inversePrimary(), "2025", e);
	}
	secondary() {
		let e = F.fromPalette({
			name: "secondary",
			palette: (e) => e.secondaryPalette,
			tone: (e) => e.platform === "watch" ? e.variant === R.NEUTRAL ? 90 : z(e.secondaryPalette, 0, 90) : e.variant === R.NEUTRAL ? e.isDark ? as(e.secondaryPalette, 0, 98) : z(e.secondaryPalette) : e.variant === R.VIBRANT ? z(e.secondaryPalette, 0, e.isDark ? 90 : 98) : e.isDark ? 80 : z(e.secondaryPalette),
			isBackground: !0,
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : this.surfaceContainerHigh(),
			contrastCurve: (e) => e.platform === "phone" ? B(4.5) : B(7),
			toneDeltaPair: (e) => e.platform === "phone" ? new es(this.secondaryContainer(), this.secondary(), 5, "relative_lighter", !0, "farther") : void 0
		});
		return P(super.secondary(), "2025", e);
	}
	secondaryDim() {
		return F.fromPalette({
			name: "secondary_dim",
			palette: (e) => e.secondaryPalette,
			tone: (e) => e.variant === R.NEUTRAL ? 85 : z(e.secondaryPalette, 0, 90),
			isBackground: !0,
			background: (e) => this.surfaceContainerHigh(),
			contrastCurve: (e) => B(4.5),
			toneDeltaPair: (e) => new es(this.secondaryDim(), this.secondary(), 5, "darker", !0, "farther")
		});
	}
	onSecondary() {
		let e = F.fromPalette({
			name: "on_secondary",
			palette: (e) => e.secondaryPalette,
			background: (e) => e.platform === "phone" ? this.secondary() : this.secondaryDim(),
			contrastCurve: (e) => e.platform === "phone" ? B(6) : B(7)
		});
		return P(super.onSecondary(), "2025", e);
	}
	secondaryContainer() {
		let e = F.fromPalette({
			name: "secondary_container",
			palette: (e) => e.secondaryPalette,
			tone: (e) => e.platform === "watch" ? 30 : e.variant === R.VIBRANT ? e.isDark ? as(e.secondaryPalette, 30, 40) : z(e.secondaryPalette, 84, 90) : e.variant === R.EXPRESSIVE ? e.isDark ? 15 : z(e.secondaryPalette, 90, 95) : e.isDark ? 25 : 90,
			isBackground: !0,
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : void 0,
			toneDeltaPair: (e) => e.platform === "watch" ? new es(this.secondaryContainer(), this.secondaryDim(), 10, "darker", !0, "farther") : void 0,
			contrastCurve: (e) => e.platform === "phone" && e.contrastLevel > 0 ? B(1.5) : void 0
		});
		return P(super.secondaryContainer(), "2025", e);
	}
	onSecondaryContainer() {
		let e = F.fromPalette({
			name: "on_secondary_container",
			palette: (e) => e.secondaryPalette,
			background: (e) => this.secondaryContainer(),
			contrastCurve: (e) => e.platform === "phone" ? B(6) : B(7)
		});
		return P(super.onSecondaryContainer(), "2025", e);
	}
	secondaryFixed() {
		let e = F.fromPalette({
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
			contrastCurve: (e) => e.platform === "phone" && e.contrastLevel > 0 ? B(1.5) : void 0
		});
		return P(super.secondaryFixed(), "2025", e);
	}
	secondaryFixedDim() {
		let e = F.fromPalette({
			name: "secondary_fixed_dim",
			palette: (e) => e.secondaryPalette,
			tone: (e) => this.secondaryFixed().getTone(e),
			isBackground: !0,
			toneDeltaPair: (e) => new es(this.secondaryFixedDim(), this.secondaryFixed(), 5, "darker", !0, "exact")
		});
		return P(super.secondaryFixedDim(), "2025", e);
	}
	onSecondaryFixed() {
		let e = F.fromPalette({
			name: "on_secondary_fixed",
			palette: (e) => e.secondaryPalette,
			background: (e) => this.secondaryFixedDim(),
			contrastCurve: (e) => B(7)
		});
		return P(super.onSecondaryFixed(), "2025", e);
	}
	onSecondaryFixedVariant() {
		let e = F.fromPalette({
			name: "on_secondary_fixed_variant",
			palette: (e) => e.secondaryPalette,
			background: (e) => this.secondaryFixedDim(),
			contrastCurve: (e) => B(4.5)
		});
		return P(super.onSecondaryFixedVariant(), "2025", e);
	}
	tertiary() {
		let e = F.fromPalette({
			name: "tertiary",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => e.platform === "watch" ? e.variant === R.TONAL_SPOT ? z(e.tertiaryPalette, 0, 90) : z(e.tertiaryPalette) : e.variant === R.EXPRESSIVE || e.variant === R.VIBRANT ? z(e.tertiaryPalette, 0, N.isCyan(e.tertiaryPalette.hue) ? 88 : e.isDark ? 98 : 100) : e.isDark ? z(e.tertiaryPalette, 0, 98) : z(e.tertiaryPalette),
			isBackground: !0,
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : this.surfaceContainerHigh(),
			contrastCurve: (e) => e.platform === "phone" ? B(4.5) : B(7),
			toneDeltaPair: (e) => e.platform === "phone" ? new es(this.tertiaryContainer(), this.tertiary(), 5, "relative_lighter", !0, "farther") : void 0
		});
		return P(super.tertiary(), "2025", e);
	}
	tertiaryDim() {
		return F.fromPalette({
			name: "tertiary_dim",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => e.variant === R.TONAL_SPOT ? z(e.tertiaryPalette, 0, 90) : z(e.tertiaryPalette),
			isBackground: !0,
			background: (e) => this.surfaceContainerHigh(),
			contrastCurve: (e) => B(4.5),
			toneDeltaPair: (e) => new es(this.tertiaryDim(), this.tertiary(), 5, "darker", !0, "farther")
		});
	}
	onTertiary() {
		let e = F.fromPalette({
			name: "on_tertiary",
			palette: (e) => e.tertiaryPalette,
			background: (e) => e.platform === "phone" ? this.tertiary() : this.tertiaryDim(),
			contrastCurve: (e) => e.platform === "phone" ? B(6) : B(7)
		});
		return P(super.onTertiary(), "2025", e);
	}
	tertiaryContainer() {
		let e = F.fromPalette({
			name: "tertiary_container",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => e.platform === "watch" ? e.variant === R.TONAL_SPOT ? z(e.tertiaryPalette, 0, 90) : z(e.tertiaryPalette) : e.variant === R.NEUTRAL ? e.isDark ? z(e.tertiaryPalette, 0, 93) : z(e.tertiaryPalette, 0, 96) : e.variant === R.TONAL_SPOT ? z(e.tertiaryPalette, 0, e.isDark ? 93 : 100) : e.variant === R.EXPRESSIVE ? z(e.tertiaryPalette, 75, N.isCyan(e.tertiaryPalette.hue) ? 88 : e.isDark ? 93 : 100) : e.isDark ? z(e.tertiaryPalette, 0, 93) : z(e.tertiaryPalette, 72, 100),
			isBackground: !0,
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : void 0,
			toneDeltaPair: (e) => e.platform === "watch" ? new es(this.tertiaryContainer(), this.tertiaryDim(), 10, "darker", !0, "farther") : void 0,
			contrastCurve: (e) => e.platform === "phone" && e.contrastLevel > 0 ? B(1.5) : void 0
		});
		return P(super.tertiaryContainer(), "2025", e);
	}
	onTertiaryContainer() {
		let e = F.fromPalette({
			name: "on_tertiary_container",
			palette: (e) => e.tertiaryPalette,
			background: (e) => this.tertiaryContainer(),
			contrastCurve: (e) => e.platform === "phone" ? B(6) : B(7)
		});
		return P(super.onTertiaryContainer(), "2025", e);
	}
	tertiaryFixed() {
		let e = F.fromPalette({
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
			contrastCurve: (e) => e.platform === "phone" && e.contrastLevel > 0 ? B(1.5) : void 0
		});
		return P(super.tertiaryFixed(), "2025", e);
	}
	tertiaryFixedDim() {
		let e = F.fromPalette({
			name: "tertiary_fixed_dim",
			palette: (e) => e.tertiaryPalette,
			tone: (e) => this.tertiaryFixed().getTone(e),
			isBackground: !0,
			toneDeltaPair: (e) => new es(this.tertiaryFixedDim(), this.tertiaryFixed(), 5, "darker", !0, "exact")
		});
		return P(super.tertiaryFixedDim(), "2025", e);
	}
	onTertiaryFixed() {
		let e = F.fromPalette({
			name: "on_tertiary_fixed",
			palette: (e) => e.tertiaryPalette,
			background: (e) => this.tertiaryFixedDim(),
			contrastCurve: (e) => B(7)
		});
		return P(super.onTertiaryFixed(), "2025", e);
	}
	onTertiaryFixedVariant() {
		let e = F.fromPalette({
			name: "on_tertiary_fixed_variant",
			palette: (e) => e.tertiaryPalette,
			background: (e) => this.tertiaryFixedDim(),
			contrastCurve: (e) => B(4.5)
		});
		return P(super.onTertiaryFixedVariant(), "2025", e);
	}
	error() {
		let e = F.fromPalette({
			name: "error",
			palette: (e) => e.errorPalette,
			tone: (e) => e.platform === "phone" ? e.isDark ? as(e.errorPalette, 0, 98) : z(e.errorPalette) : as(e.errorPalette),
			isBackground: !0,
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : this.surfaceContainerHigh(),
			contrastCurve: (e) => e.platform === "phone" ? B(4.5) : B(7),
			toneDeltaPair: (e) => e.platform === "phone" ? new es(this.errorContainer(), this.error(), 5, "relative_lighter", !0, "farther") : void 0
		});
		return P(super.error(), "2025", e);
	}
	errorDim() {
		return F.fromPalette({
			name: "error_dim",
			palette: (e) => e.errorPalette,
			tone: (e) => as(e.errorPalette),
			isBackground: !0,
			background: (e) => this.surfaceContainerHigh(),
			contrastCurve: (e) => B(4.5),
			toneDeltaPair: (e) => new es(this.errorDim(), this.error(), 5, "darker", !0, "farther")
		});
	}
	onError() {
		let e = F.fromPalette({
			name: "on_error",
			palette: (e) => e.errorPalette,
			background: (e) => e.platform === "phone" ? this.error() : this.errorDim(),
			contrastCurve: (e) => e.platform === "phone" ? B(6) : B(7)
		});
		return P(super.onError(), "2025", e);
	}
	errorContainer() {
		let e = F.fromPalette({
			name: "error_container",
			palette: (e) => e.errorPalette,
			tone: (e) => e.platform === "watch" ? 30 : e.isDark ? as(e.errorPalette, 30, 93) : z(e.errorPalette, 0, 90),
			isBackground: !0,
			background: (e) => e.platform === "phone" ? this.highestSurface(e) : void 0,
			toneDeltaPair: (e) => e.platform === "watch" ? new es(this.errorContainer(), this.errorDim(), 10, "darker", !0, "farther") : void 0,
			contrastCurve: (e) => e.platform === "phone" && e.contrastLevel > 0 ? B(1.5) : void 0
		});
		return P(super.errorContainer(), "2025", e);
	}
	onErrorContainer() {
		let e = F.fromPalette({
			name: "on_error_container",
			palette: (e) => e.errorPalette,
			background: (e) => this.errorContainer(),
			contrastCurve: (e) => e.platform === "phone" ? B(4.5) : B(7)
		});
		return P(super.onErrorContainer(), "2025", e);
	}
	surfaceVariant() {
		let e = Object.assign(this.surfaceContainerHighest().clone(), { name: "surface_variant" });
		return P(super.surfaceVariant(), "2025", e);
	}
	surfaceTint() {
		let e = Object.assign(this.primary().clone(), { name: "surface_tint" });
		return P(super.surfaceTint(), "2025", e);
	}
	background() {
		let e = Object.assign(this.surface().clone(), { name: "background" });
		return P(super.background(), "2025", e);
	}
	onBackground() {
		let e = Object.assign(this.onSurface().clone(), {
			name: "on_background",
			tone: (e) => e.platform === "watch" ? 100 : this.onSurface().getTone(e)
		});
		return P(super.onBackground(), "2025", e);
	}
}, V = class e {
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
V.contentAccentToneDelta = 15, V.colorSpec = new ss(), V.primaryPaletteKeyColor = V.colorSpec.primaryPaletteKeyColor(), V.secondaryPaletteKeyColor = V.colorSpec.secondaryPaletteKeyColor(), V.tertiaryPaletteKeyColor = V.colorSpec.tertiaryPaletteKeyColor(), V.neutralPaletteKeyColor = V.colorSpec.neutralPaletteKeyColor(), V.neutralVariantPaletteKeyColor = V.colorSpec.neutralVariantPaletteKeyColor(), V.background = V.colorSpec.background(), V.onBackground = V.colorSpec.onBackground(), V.surface = V.colorSpec.surface(), V.surfaceDim = V.colorSpec.surfaceDim(), V.surfaceBright = V.colorSpec.surfaceBright(), V.surfaceContainerLowest = V.colorSpec.surfaceContainerLowest(), V.surfaceContainerLow = V.colorSpec.surfaceContainerLow(), V.surfaceContainer = V.colorSpec.surfaceContainer(), V.surfaceContainerHigh = V.colorSpec.surfaceContainerHigh(), V.surfaceContainerHighest = V.colorSpec.surfaceContainerHighest(), V.onSurface = V.colorSpec.onSurface(), V.surfaceVariant = V.colorSpec.surfaceVariant(), V.onSurfaceVariant = V.colorSpec.onSurfaceVariant(), V.inverseSurface = V.colorSpec.inverseSurface(), V.inverseOnSurface = V.colorSpec.inverseOnSurface(), V.outline = V.colorSpec.outline(), V.outlineVariant = V.colorSpec.outlineVariant(), V.shadow = V.colorSpec.shadow(), V.scrim = V.colorSpec.scrim(), V.surfaceTint = V.colorSpec.surfaceTint(), V.primary = V.colorSpec.primary(), V.onPrimary = V.colorSpec.onPrimary(), V.primaryContainer = V.colorSpec.primaryContainer(), V.onPrimaryContainer = V.colorSpec.onPrimaryContainer(), V.inversePrimary = V.colorSpec.inversePrimary(), V.secondary = V.colorSpec.secondary(), V.onSecondary = V.colorSpec.onSecondary(), V.secondaryContainer = V.colorSpec.secondaryContainer(), V.onSecondaryContainer = V.colorSpec.onSecondaryContainer(), V.tertiary = V.colorSpec.tertiary(), V.onTertiary = V.colorSpec.onTertiary(), V.tertiaryContainer = V.colorSpec.tertiaryContainer(), V.onTertiaryContainer = V.colorSpec.onTertiaryContainer(), V.error = V.colorSpec.error(), V.onError = V.colorSpec.onError(), V.errorContainer = V.colorSpec.errorContainer(), V.onErrorContainer = V.colorSpec.onErrorContainer(), V.primaryFixed = V.colorSpec.primaryFixed(), V.primaryFixedDim = V.colorSpec.primaryFixedDim(), V.onPrimaryFixed = V.colorSpec.onPrimaryFixed(), V.onPrimaryFixedVariant = V.colorSpec.onPrimaryFixedVariant(), V.secondaryFixed = V.colorSpec.secondaryFixed(), V.secondaryFixedDim = V.colorSpec.secondaryFixedDim(), V.onSecondaryFixed = V.colorSpec.onSecondaryFixed(), V.onSecondaryFixedVariant = V.colorSpec.onSecondaryFixedVariant(), V.tertiaryFixed = V.colorSpec.tertiaryFixed(), V.tertiaryFixedDim = V.colorSpec.tertiaryFixedDim(), V.onTertiaryFixed = V.colorSpec.onTertiaryFixed(), V.onTertiaryFixedVariant = V.colorSpec.onTertiaryFixedVariant();
var cs = class e {
	static maybeFallbackSpecVersion(e, t) {
		switch (t) {
			case R.EXPRESSIVE:
			case R.VIBRANT:
			case R.TONAL_SPOT:
			case R.NEUTRAL: return e;
			default: return "2021";
		}
	}
	constructor(t) {
		this.sourceColorArgb = t.sourceColorHct.toInt(), this.variant = t.variant, this.contrastLevel = t.contrastLevel, this.isDark = t.isDark, this.platform = t.platform ?? "phone", this.specVersion = e.maybeFallbackSpecVersion(t.specVersion ?? "2021", this.variant), this.sourceColorHct = t.sourceColorHct, this.primaryPalette = t.primaryPalette ?? ps(this.specVersion).getPrimaryPalette(this.variant, t.sourceColorHct, this.isDark, this.platform, this.contrastLevel), this.secondaryPalette = t.secondaryPalette ?? ps(this.specVersion).getSecondaryPalette(this.variant, t.sourceColorHct, this.isDark, this.platform, this.contrastLevel), this.tertiaryPalette = t.tertiaryPalette ?? ps(this.specVersion).getTertiaryPalette(this.variant, t.sourceColorHct, this.isDark, this.platform, this.contrastLevel), this.neutralPalette = t.neutralPalette ?? ps(this.specVersion).getNeutralPalette(this.variant, t.sourceColorHct, this.isDark, this.platform, this.contrastLevel), this.neutralVariantPalette = t.neutralVariantPalette ?? ps(this.specVersion).getNeutralVariantPalette(this.variant, t.sourceColorHct, this.isDark, this.platform, this.contrastLevel), this.errorPalette = t.errorPalette ?? ps(this.specVersion).getErrorPalette(this.variant, t.sourceColorHct, this.isDark, this.platform, this.contrastLevel) ?? I.fromHueAndChroma(25, 84), this.colors = new V();
	}
	toString() {
		return `Scheme: variant=${R[this.variant]}, mode=${this.isDark ? "dark" : "light"}, platform=${this.platform}, contrastLevel=${this.contrastLevel.toFixed(1)}, seed=${this.sourceColorHct.toString()}, specVersion=${this.specVersion}`;
	}
	static getPiecewiseHue(e, t, n) {
		let r = Math.min(t.length - 1, n.length), i = e.hue;
		for (let e = 0; e < r; e++) if (i >= t[e] && i < t[e + 1]) return go(n[e]);
		return i;
	}
	static getRotatedHue(t, n, r) {
		let i = e.getPiecewiseHue(t, n, r);
		return Math.min(n.length - 1, r.length) <= 0 && (i = 0), go(t.hue + i);
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
cs.DEFAULT_SPEC_VERSION = "2021", cs.DEFAULT_PLATFORM = "phone";
var ls = class {
	getPrimaryPalette(e, t, n, r, i) {
		switch (e) {
			case R.CONTENT:
			case R.FIDELITY: return I.fromHueAndChroma(t.hue, t.chroma);
			case R.FRUIT_SALAD: return I.fromHueAndChroma(go(t.hue - 50), 48);
			case R.MONOCHROME: return I.fromHueAndChroma(t.hue, 0);
			case R.NEUTRAL: return I.fromHueAndChroma(t.hue, 12);
			case R.RAINBOW: return I.fromHueAndChroma(t.hue, 48);
			case R.TONAL_SPOT: return I.fromHueAndChroma(t.hue, 36);
			case R.EXPRESSIVE: return I.fromHueAndChroma(go(t.hue + 240), 40);
			case R.VIBRANT: return I.fromHueAndChroma(t.hue, 200);
			default: throw Error(`Unsupported variant: ${e}`);
		}
	}
	getSecondaryPalette(e, t, n, r, i) {
		switch (e) {
			case R.CONTENT:
			case R.FIDELITY: return I.fromHueAndChroma(t.hue, Math.max(t.chroma - 32, t.chroma * .5));
			case R.FRUIT_SALAD: return I.fromHueAndChroma(go(t.hue - 50), 36);
			case R.MONOCHROME: return I.fromHueAndChroma(t.hue, 0);
			case R.NEUTRAL: return I.fromHueAndChroma(t.hue, 8);
			case R.RAINBOW: return I.fromHueAndChroma(t.hue, 16);
			case R.TONAL_SPOT: return I.fromHueAndChroma(t.hue, 16);
			case R.EXPRESSIVE: return I.fromHueAndChroma(cs.getRotatedHue(t, [
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
			case R.VIBRANT: return I.fromHueAndChroma(cs.getRotatedHue(t, [
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
			case R.CONTENT: return I.fromHct(Go.fixIfDisliked(new $o(t).analogous(3, 6)[2]));
			case R.FIDELITY: return I.fromHct(Go.fixIfDisliked(new $o(t).complement));
			case R.FRUIT_SALAD: return I.fromHueAndChroma(t.hue, 36);
			case R.MONOCHROME: return I.fromHueAndChroma(t.hue, 0);
			case R.NEUTRAL: return I.fromHueAndChroma(t.hue, 16);
			case R.RAINBOW:
			case R.TONAL_SPOT: return I.fromHueAndChroma(go(t.hue + 60), 24);
			case R.EXPRESSIVE: return I.fromHueAndChroma(cs.getRotatedHue(t, [
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
			case R.VIBRANT: return I.fromHueAndChroma(cs.getRotatedHue(t, [
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
			case R.CONTENT:
			case R.FIDELITY: return I.fromHueAndChroma(t.hue, t.chroma / 8);
			case R.FRUIT_SALAD: return I.fromHueAndChroma(t.hue, 10);
			case R.MONOCHROME: return I.fromHueAndChroma(t.hue, 0);
			case R.NEUTRAL: return I.fromHueAndChroma(t.hue, 2);
			case R.RAINBOW: return I.fromHueAndChroma(t.hue, 0);
			case R.TONAL_SPOT: return I.fromHueAndChroma(t.hue, 6);
			case R.EXPRESSIVE: return I.fromHueAndChroma(go(t.hue + 15), 8);
			case R.VIBRANT: return I.fromHueAndChroma(t.hue, 10);
			default: throw Error(`Unsupported variant: ${e}`);
		}
	}
	getNeutralVariantPalette(e, t, n, r, i) {
		switch (e) {
			case R.CONTENT: return I.fromHueAndChroma(t.hue, t.chroma / 8 + 4);
			case R.FIDELITY: return I.fromHueAndChroma(t.hue, t.chroma / 8 + 4);
			case R.FRUIT_SALAD: return I.fromHueAndChroma(t.hue, 16);
			case R.MONOCHROME: return I.fromHueAndChroma(t.hue, 0);
			case R.NEUTRAL: return I.fromHueAndChroma(t.hue, 2);
			case R.RAINBOW: return I.fromHueAndChroma(t.hue, 0);
			case R.TONAL_SPOT: return I.fromHueAndChroma(t.hue, 8);
			case R.EXPRESSIVE: return I.fromHueAndChroma(go(t.hue + 15), 12);
			case R.VIBRANT: return I.fromHueAndChroma(t.hue, 12);
			default: throw Error(`Unsupported variant: ${e}`);
		}
	}
	getErrorPalette(e, t, n, r, i) {}
}, us = class e extends ls {
	getPrimaryPalette(e, t, n, r, i) {
		switch (e) {
			case R.NEUTRAL: return I.fromHueAndChroma(t.hue, r === "phone" ? N.isBlue(t.hue) ? 12 : 8 : N.isBlue(t.hue) ? 16 : 12);
			case R.TONAL_SPOT: return I.fromHueAndChroma(t.hue, r === "phone" && n ? 26 : 32);
			case R.EXPRESSIVE: return I.fromHueAndChroma(t.hue, r === "phone" ? n ? 36 : 48 : 40);
			case R.VIBRANT: return I.fromHueAndChroma(t.hue, r === "phone" ? 74 : 56);
			default: return super.getPrimaryPalette(e, t, n, r, i);
		}
	}
	getSecondaryPalette(e, t, n, r, i) {
		switch (e) {
			case R.NEUTRAL: return I.fromHueAndChroma(t.hue, r === "phone" ? N.isBlue(t.hue) ? 6 : 4 : N.isBlue(t.hue) ? 10 : 6);
			case R.TONAL_SPOT: return I.fromHueAndChroma(t.hue, 16);
			case R.EXPRESSIVE: return I.fromHueAndChroma(cs.getRotatedHue(t, [
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
			case R.VIBRANT: return I.fromHueAndChroma(cs.getRotatedHue(t, [
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
			case R.NEUTRAL: return I.fromHueAndChroma(cs.getRotatedHue(t, [
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
			case R.TONAL_SPOT: return I.fromHueAndChroma(cs.getRotatedHue(t, [
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
			case R.EXPRESSIVE: return I.fromHueAndChroma(cs.getRotatedHue(t, [
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
			case R.VIBRANT: return I.fromHueAndChroma(cs.getRotatedHue(t, [
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
		return cs.getRotatedHue(e, [
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
		return r === "phone" ? n ? N.isYellow(i) ? 6 : 14 : 18 : 12;
	}
	static getVibrantNeutralHue(e) {
		return cs.getRotatedHue(e, [
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
		return n === "phone" || N.isBlue(r) ? 28 : 20;
	}
	getNeutralPalette(t, n, r, i, a) {
		switch (t) {
			case R.NEUTRAL: return I.fromHueAndChroma(n.hue, i === "phone" ? 1.4 : 6);
			case R.TONAL_SPOT: return I.fromHueAndChroma(n.hue, i === "phone" ? 5 : 10);
			case R.EXPRESSIVE: return I.fromHueAndChroma(e.getExpressiveNeutralHue(n), e.getExpressiveNeutralChroma(n, r, i));
			case R.VIBRANT: return I.fromHueAndChroma(e.getVibrantNeutralHue(n), e.getVibrantNeutralChroma(n, i));
			default: return super.getNeutralPalette(t, n, r, i, a);
		}
	}
	getNeutralVariantPalette(t, n, r, i, a) {
		switch (t) {
			case R.NEUTRAL: return I.fromHueAndChroma(n.hue, (i === "phone" ? 1.4 : 6) * 2.2);
			case R.TONAL_SPOT: return I.fromHueAndChroma(n.hue, (i === "phone" ? 5 : 10) * 1.7);
			case R.EXPRESSIVE:
				let o = e.getExpressiveNeutralHue(n), s = e.getExpressiveNeutralChroma(n, r, i);
				return I.fromHueAndChroma(o, s * (o >= 105 && o < 125 ? 1.6 : 2.3));
			case R.VIBRANT:
				let c = e.getVibrantNeutralHue(n), l = e.getVibrantNeutralChroma(n, i);
				return I.fromHueAndChroma(c, l * 1.29);
			default: return super.getNeutralVariantPalette(t, n, r, i, a);
		}
	}
	getErrorPalette(e, t, n, r, i) {
		let a = cs.getPiecewiseHue(t, [
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
			case R.NEUTRAL: return I.fromHueAndChroma(a, r === "phone" ? 50 : 40);
			case R.TONAL_SPOT: return I.fromHueAndChroma(a, r === "phone" ? 60 : 48);
			case R.EXPRESSIVE: return I.fromHueAndChroma(a, r === "phone" ? 64 : 48);
			case R.VIBRANT: return I.fromHueAndChroma(a, r === "phone" ? 80 : 60);
			default: return super.getErrorPalette(e, t, n, r, i);
		}
	}
}, ds = new ls(), fs = new us();
function ps(e) {
	return e === "2025" ? fs : ds;
}
var ms = class e {
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
		let n = N.fromInt(e), r = n.hue, i = n.chroma;
		t ? (this.a1 = I.fromHueAndChroma(r, i), this.a2 = I.fromHueAndChroma(r, i / 3), this.a3 = I.fromHueAndChroma(r + 60, i / 2), this.n1 = I.fromHueAndChroma(r, Math.min(i / 12, 4)), this.n2 = I.fromHueAndChroma(r, Math.min(i / 6, 8))) : (this.a1 = I.fromHueAndChroma(r, Math.max(48, i)), this.a2 = I.fromHueAndChroma(r, 16), this.a3 = I.fromHueAndChroma(r + 60, 24), this.n1 = I.fromHueAndChroma(r, 4), this.n2 = I.fromHueAndChroma(r, 8)), this.error = I.fromHueAndChroma(25, 84);
	}
}, hs = class e {
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
		return e.lightFromCorePalette(ms.of(t));
	}
	static dark(t) {
		return e.darkFromCorePalette(ms.of(t));
	}
	static lightContent(t) {
		return e.lightFromCorePalette(ms.contentOf(t));
	}
	static darkContent(t) {
		return e.darkFromCorePalette(ms.contentOf(t));
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
function gs(e) {
	let t = To(e), n = Eo(e), r = Do(e), i = [
		t.toString(16),
		n.toString(16),
		r.toString(16)
	];
	for (let [e, t] of i.entries()) t.length === 1 && (i[e] = "0" + t);
	return "#" + i.join("");
}
function _s(e) {
	e = e.replace("#", "");
	let t = e.length === 3, n = e.length === 6, r = e.length === 8;
	if (!t && !n && !r) throw Error("unexpected hex " + e);
	let i = 0, a = 0, o = 0;
	return t ? (i = vs(e.slice(0, 1).repeat(2)), a = vs(e.slice(1, 2).repeat(2)), o = vs(e.slice(2, 3).repeat(2))) : n ? (i = vs(e.slice(0, 2)), a = vs(e.slice(2, 4)), o = vs(e.slice(4, 6))) : r && (i = vs(e.slice(2, 4)), a = vs(e.slice(4, 6)), o = vs(e.slice(6, 8))), (255 << 24 | (i & 255) << 16 | (a & 255) << 8 | o & 255) >>> 0;
}
function vs(e) {
	return parseInt(e, 16);
}
function ys(e, t = []) {
	let n = ms.of(e);
	return {
		source: e,
		schemes: {
			light: hs.light(e),
			dark: hs.dark(e)
		},
		palettes: {
			primary: n.a1,
			secondary: n.a2,
			tertiary: n.a3,
			neutral: n.n1,
			neutralVariant: n.n2,
			error: n.error
		},
		customColors: t.map((t) => bs(e, t))
	};
}
function bs(e, t) {
	let n = t.value, r = n, i = e;
	t.blend && (n = Uo.harmonize(r, i));
	let a = ms.of(n).a1;
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
function xs(e) {
	return e === "standard" || e === "expressive";
}
function Ss(e) {
	return e === "monochrome" || e === "neutral" || e === "tonal-spot" || e === "vibrant" || e === "expressive" || e === "fidelity" || e === "content" || e === "rainbow" || e === "fruit-salad";
}
var Cs, ws, Ts, Es, Ds, Os, ks, As, js, Ms;
function Ns(e) {
	let t = "";
	for (let n in e) {
		let r = e[n].toString();
		if (!r.startsWith("var(")) continue;
		let [i, a] = r.trim().slice(4, -1).split(/,(.+)/).map((e) => e.trim());
		i.startsWith("--") && a && (t += `${i}:${a};`);
	}
	return t;
}
function Ps(e, ...t) {
	let n = { ...e };
	for (let e of t) delete n[e];
	return n;
}
var Fs = class extends S {
	constructor() {
		super(...arguments), Cs.add(this), ws.set(this, new CSSStyleSheet()), Ts.set(this, !1), Es.set(this, void 0), Ds.set(this, void 0), Os.set(this, void 0), ks.set(this, () => t(this, Cs, "m", As).call(this, !0)), this.color = "#6750A4", this.variant = "tonal-spot", this.scheme = "auto", this.contrast = "standard", this.strongFocus = !1, this.density = 0, this.motion = "expressive";
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
				return t(this, Ds, "f")?.matches ?? !1;
		}
	}
	connectedCallback() {
		super.connectedCallback(), this.parentElement instanceof HTMLBodyElement ? (this.shadowRoot?.adoptedStyleSheets.includes(t(this, ws, "f")) && (this.shadowRoot.adoptedStyleSheets = this.shadowRoot.adoptedStyleSheets.filter((e) => e !== t(this, ws, "f"))), document.adoptedStyleSheets.includes(t(this, ws, "f")) || (document.adoptedStyleSheets = [...document.adoptedStyleSheets, t(this, ws, "f")])) : (document.adoptedStyleSheets.includes(t(this, ws, "f")) && (document.adoptedStyleSheets = document.adoptedStyleSheets.filter((e) => e !== t(this, ws, "f"))), this.shadowRoot && !this.shadowRoot.adoptedStyleSheets.includes(t(this, ws, "f")) && (this.shadowRoot.adoptedStyleSheets = [...this.shadowRoot.adoptedStyleSheets, t(this, ws, "f")])), n(this, Es, matchMedia("(prefers-color-scheme: light)"), "f"), n(this, Ds, matchMedia("(prefers-color-scheme: dark)"), "f"), n(this, Os, matchMedia("(forced-colors: active)"), "f"), [
			t(this, Es, "f"),
			t(this, Ds, "f"),
			t(this, Os, "f")
		].forEach((e) => e.addEventListener("change", t(this, ks, "f")));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), [
			t(this, Es, "f"),
			t(this, Ds, "f"),
			t(this, Os, "f")
		].forEach((e) => e?.removeEventListener("change", t(this, ks, "f"))), n(this, Es, n(this, Ds, n(this, Os, void 0, "f"), "f"), "f"), document.adoptedStyleSheets.includes(t(this, ws, "f")) && (document.adoptedStyleSheets = document.adoptedStyleSheets.filter((e) => e !== t(this, ws, "f")));
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && !Ss(this.variant) && (this.variant = "neutral"), e.has("scheme") && !co(this.scheme) && (this.scheme = "auto"), e.has("contrast") && !lo(this.contrast) && (this.contrast = "standard"), e.has("motion") && !xs(this.motion) && (this.motion = "standard");
	}
	updated(e) {
		super.updated(e), t(this, Cs, "m", As).call(this, t(this, Ts, "f") && [
			"color",
			"variant",
			"scheme",
			"contrast"
		].some((t) => e.has(t)));
	}
	firstUpdated(e) {
		super.firstUpdated(e), n(this, Ts, !0, "f");
	}
	render() {
		return b`<slot></slot>`;
	}
};
ws = /* @__PURE__ */ new WeakMap(), Ts = /* @__PURE__ */ new WeakMap(), Es = /* @__PURE__ */ new WeakMap(), Ds = /* @__PURE__ */ new WeakMap(), Os = /* @__PURE__ */ new WeakMap(), ks = /* @__PURE__ */ new WeakMap(), Cs = /* @__PURE__ */ new WeakSet(), As = function(e) {
	let n = _s(this.color), r = new cs({
		sourceColorHct: N.fromInt(n),
		variant: t(this, Cs, "m", js).call(this),
		contrastLevel: t(this, Cs, "m", Ms).call(this),
		isDark: this.isDark,
		specVersion: "2025"
	}), i = "", a = new V(), o = [
		...a.allColors,
		a.surfaceVariant(),
		a.shadow(),
		a.scrim(),
		a.surfaceTint()
	];
	for (let e of o) {
		let t = `--md-sys-color-${e.name.replace(/_/g, "-").toLowerCase()}`, n = gs(e.getArgb(r));
		i += `${t}: ${n};`;
	}
	this.motion === "expressive" ? (i += "--md-sys-motion-spring-fast-spatial: 350ms cubic-bezier(0.42, 1.67, 0.21, 0.90);", i += "--md-sys-motion-spring-default-spatial: 500ms cubic-bezier(0.38, 1.21, 0.22, 1.00);", i += "--md-sys-motion-spring-slow-spatial: 650ms cubic-bezier(0.39, 1.29, 0.35, 0.98);", i += "--md-sys-motion-spring-fast-effects: 150ms cubic-bezier(0.31, 0.94, 0.34, 1.00);", i += "--md-sys-motion-spring-default-effects: 200ms cubic-bezier(0.34, 0.80, 0.34, 1.00);", i += "--md-sys-motion-spring-slow-effects: 300ms cubic-bezier(0.34, 0.88, 0.34, 1.00);") : this.parentElement instanceof HTMLBodyElement || (i += Ns(j.motion.spring)), i += `--md-sys-density-scale: ${this.density};`, i += `--m3e-scrollbar-thumb-color: ${gs(r.neutralPalette.tone(60))};`, i += `--m3e-focus-ring-visibility: ${this.strongFocus ? "visible" : "hidden"};`;
	let s = "";
	if (s += `font-size: ${j.typescale.standard.body.large.fontSize};`, s += `font-weight: ${j.typescale.standard.body.large.fontWeight};`, s += `line-height: ${j.typescale.standard.body.large.lineHeight};`, s += `letter-spacing: ${j.typescale.standard.body.large.tracking};`, this.parentElement instanceof HTMLBodyElement) switch (t(this, ws, "f").replaceSync(`
        html { ${i} }
        body { ${s} }
        @media not (forced-colors: active) {
          body { 
            background-color: var(--md-sys-color-background);
            color: var(--md-sys-color-on-background);
            scrollbar-color: ${j.scrollbar.color};
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
	else t(this, ws, "f").replaceSync(`:host { ${i} }`);
	t(this, Ts, "f") && this.dispatchEvent(new Event("change", { bubbles: !0 })), e && document.body.offsetHeight;
}, js = function() {
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
}, Ms = function() {
	switch (this.contrast) {
		case "high": return 1;
		case "medium": return .5;
		default: return 0;
	}
}, (() => {
	if (typeof window < "u") {
		let e = "";
		e += Ns(j.typescale.standard.display.large), e += Ns(j.typescale.standard.display.medium), e += Ns(j.typescale.standard.display.small), e += Ns(j.typescale.standard.headline.large), e += Ns(j.typescale.standard.headline.medium), e += Ns(j.typescale.standard.headline.small), e += Ns(j.typescale.standard.title.large), e += Ns(j.typescale.standard.title.medium), e += Ns(j.typescale.standard.title.small), e += Ns(j.typescale.standard.label.large), e += Ns(j.typescale.standard.label.medium), e += Ns(j.typescale.standard.label.small), e += Ns(j.typescale.standard.body.large), e += Ns(j.typescale.standard.body.medium), e += Ns(j.typescale.standard.body.small), e += Ns(j.elevation), e += Ns(j.shape.corner.value), e += Ns(Ps(j.shape.corner, "value", "extraLargeEnd", "extraLargeStart", "mediumTop", "mediumEnd", "mediumStart", "smallTop", "smallEnd", "smallStart", "extraSmallEnd", "extraSmallStart", "extraSmallBottom")), e += Ns(j.motion.duration), e += Ns(j.motion.easing), e += Ns(j.motion.spring), e += Ns(Ps(j.density, "calc", "calcHalf")), e += Ns(j.measurement), e += Ns(j.state), Lt(c(`html{${e}}`));
	}
})(), Fs.styles = l`:host { display: contents; } :host([hidden]) { display: none; }`, e([C({ reflect: !1 })], Fs.prototype, "color", void 0), e([C({ reflect: !1 })], Fs.prototype, "variant", void 0), e([C({ reflect: !1 })], Fs.prototype, "scheme", void 0), e([C({ reflect: !1 })], Fs.prototype, "contrast", void 0), e([C({
	attribute: "strong-focus",
	type: Boolean,
	reflect: !1
})], Fs.prototype, "strongFocus", void 0), e([C({
	type: Number,
	reflect: !1
})], Fs.prototype, "density", void 0), e([C({ reflect: !1 })], Fs.prototype, "motion", void 0), Fs = e([T("m3e-theme")], Fs);
var Is, Ls, Rs, zs, Bs, Vs = class extends M(S, "img") {
	constructor() {
		super(...arguments), Is.add(this), Ls.set(this, void 0), Rs.set(this, void 0), zs.set(this, () => this.requestUpdate()), this.color = "#6750A4", this.variant = "neutral", this.scheme = "auto";
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
				return t(this, Rs, "f")?.matches ?? !1;
		}
	}
	connectedCallback() {
		super.connectedCallback(), n(this, Ls, matchMedia("(prefers-color-scheme: light)"), "f"), n(this, Rs, matchMedia("(prefers-color-scheme: dark)"), "f"), [t(this, Ls, "f"), t(this, Rs, "f")].forEach((e) => e.addEventListener("change", t(this, zs, "f")));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), [t(this, Ls, "f"), t(this, Rs, "f")].forEach((e) => e?.removeEventListener("change", t(this, zs, "f"))), n(this, Ls, n(this, Rs, void 0, "f"), "f");
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && !Ss(this.variant) && (this.variant = "neutral"), e.has("scheme") && !co(this.scheme) && (this.scheme = "auto");
	}
	updated(e) {
		super.updated(e);
		let n = this.shadowRoot?.querySelector(".base"), r = _s(this.color), i = ys(r), a = new cs({
			sourceColorHct: N.fromInt(r),
			variant: t(this, Is, "m", Bs).call(this),
			contrastLevel: 0,
			isDark: this.isDark,
			primaryPalette: i.palettes.primary,
			neutralPalette: i.palettes.neutral
		}), o = new V();
		n?.style.setProperty("--_theme-icon-container-color", gs(o.surface().getArgb(a))), n?.style.setProperty("--_theme-icon-color", gs(o.primary().getArgb(a)));
	}
	render() {
		return b`<div class="base"><svg viewBox="0 0 80 80" class="icon"><path class="container" d="M77.87 0C79.05 0 80 .95 80 2.13v75.74c0 1.17-.95 2.13-2.13 2.13H2.13C.96 80 0 79.04 0 77.87V2.13C0 .95.96 0 2.13 0h75.74z"/><path class="color" d="M54 40c3.32 0 6 2.69 6 6 0 1.2 0-1.2 0 0 0 3.31-2.68 6-6 6H26c-3.31 0-6-2.69-6-6 0-1.2 0 1.2 0 0 0-3.31 2.69-6 6-6h28z"/><path class="color" d="M0 0h80v17.24H0V0z"/></svg></div>`;
	}
};
Ls = /* @__PURE__ */ new WeakMap(), Rs = /* @__PURE__ */ new WeakMap(), zs = /* @__PURE__ */ new WeakMap(), Is = /* @__PURE__ */ new WeakSet(), Bs = function() {
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
}, Vs.styles = l`:host { display: inline-block; width: var(--m3e-theme-icon-size, 24px); height: var(--m3e-theme-icon-size, 24px); } :host([hidden]) { display: none; } .base { width: 100%; height: 100%; border-radius: var(--m3e-theme-icon-shape, ${j.shape.corner.extraSmall}); outline: solid 1px color-mix( in srgb, var(--m3e-theme-icon-outline-color, ${j.color.outlineVariant}) var(--m3e-theme-icon-outline-opacity, 50%), transparent ); } .icon { width: 100%; height: 100%; } .container { fill: var(--m3e-theme-icon-container-color, var(--_theme-icon-container-color)); } .color { fill: var(--m3e-theme-icon-color, var(--_theme-icon-color)); }`, e([C()], Vs.prototype, "color", void 0), e([C()], Vs.prototype, "variant", void 0), e([C()], Vs.prototype, "scheme", void 0), Vs = e([T("m3e-theme-icon")], Vs);
//#endregion
//#region node_modules/@m3e/web/dist/app-bar.js
function Hs(e) {
	return e === "small" || e === "medium" || e === "large";
}
var Us = {
	small: {
		containerHeight: c("var(--m3e-app-bar-small-container-height, 64px)"),
		titleTextFontSize: c(`var(--m3e-app-bar-small-title-text-font-size, ${j.typescale.standard.title.large.fontSize})`),
		titleTextFontWeight: c(`var(--m3e-app-bar-small-title-text-font-weight, ${j.typescale.standard.title.large.fontWeight})`),
		titleTextLineHeight: c(`var(--m3e-app-bar-small-title-text-line-height, ${j.typescale.standard.title.large.lineHeight})`),
		titleTextTracking: c(`var(--m3e-app-bar-small-subtitle-text-tracking, ${j.typescale.standard.title.large.tracking})`),
		subtitleTextFontSize: c(`var(--m3e-app-bar-small-subtitle-text-font-size, ${j.typescale.standard.label.medium.fontSize})`),
		subtitleTextFontWeight: c(`var(--m3e-app-bar-small-subtitle-text-font-weight, ${j.typescale.standard.label.medium.fontWeight})`),
		subtitleTextLineHeight: c(`var(--m3e-app-bar-small-subtitle-text-line-height, ${j.typescale.standard.label.medium.lineHeight})`),
		subtitleTextTracking: c(`var(--m3e-app-bar-small-subtitle-text-tracking, ${j.typescale.standard.label.medium.tracking})`),
		headingPaddingLeft: c(`var(--m3e-app-bar-small-heading-padding-left, ${j.measurement.space50})`),
		headingPaddingRight: c(`var(--m3e-app-bar-small-heading-padding-right, ${j.measurement.space50})`)
	},
	medium: {
		containerHeight: c("var(--m3e-app-bar-medium-container-height, 112px)"),
		containerHeightWithSubtitle: c("var(--m3e-app-bar-medium-container-height-with-subtitle, 136px)"),
		titleTextFontSize: c(`var(--m3e-app-bar-medium-title-text-font-size, ${j.typescale.standard.headline.medium.fontSize})`),
		titleTextFontWeight: c(`var(--m3e-app-bar-medium-title-text-font-weight, ${j.typescale.standard.headline.medium.fontWeight})`),
		titleTextLineHeight: c(`var(--m3e-app-bar-medium-title-text-line-height, ${j.typescale.standard.headline.medium.lineHeight})`),
		titleTextTracking: c(`var(--m3e-app-bar-medium-subtitle-text-tracking, ${j.typescale.standard.headline.medium.tracking})`),
		subtitleTextFontSize: c(`var(--m3e-app-bar-medium-subtitle-text-font-size, ${j.typescale.standard.title.small.fontSize})`),
		subtitleTextFontWeight: c(`var(--m3e-app-bar-medium-subtitle-text-font-weight, ${j.typescale.standard.title.small.fontWeight})`),
		subtitleTextLineHeight: c(`var(--m3e-app-bar-medium-subtitle-text-line-height, ${j.typescale.standard.title.small.lineHeight})`),
		subtitleTextTracking: c(`var(--m3e-app-bar-medium-subtitle-text-tracking, ${j.typescale.standard.title.small.tracking})`),
		headingPaddingLeft: c(`var(--m3e-app-bar-medium-heading-padding-left, ${j.measurement.space200})`),
		headingPaddingRight: c(`var(--m3e-app-bar-medium-heading-padding-right, ${j.measurement.space50})`),
		paddingTop: c(`var(--m3e-app-bar-medium-padding-top, ${j.measurement.space100})`),
		paddingBottom: c(`var(--m3e-app-bar-medium-padding-bottom, ${j.measurement.space150})`),
		titleMaxLines: c("var(--m3e-app-bar-medium-title-max-lines, 2)"),
		subtitleMaxLines: c("var(--m3e-app-bar-medium-subtitle-max-lines, 1)")
	},
	large: {
		containerHeight: c("var(--m3e-app-bar-large-container-height, 120px)"),
		containerHeightWithSubtitle: c("var(--m3e-app-bar-large-container-height-with-subtitle, 152px)"),
		titleTextFontSize: c(`var(--m3e-app-bar-large-title-text-font-size, ${j.typescale.standard.display.small.fontSize})`),
		titleTextFontWeight: c(`var(--m3e-app-bar-large-title-text-font-weight, ${j.typescale.standard.display.small.fontWeight})`),
		titleTextLineHeight: c(`var(--m3e-app-bar-large-title-text-line-height, ${j.typescale.standard.display.small.lineHeight})`),
		titleTextTracking: c(`var(--m3e-app-bar-large-subtitle-text-tracking, ${j.typescale.standard.display.small.tracking})`),
		subtitleTextFontSize: c(`var(--m3e-app-bar-large-subtitle-text-font-size, ${j.typescale.standard.title.medium.fontSize})`),
		subtitleTextFontWeight: c(`var(--m3e-app-bar-large-subtitle-text-font-weight, ${j.typescale.standard.title.medium.fontWeight})`),
		subtitleTextLineHeight: c(`var(--m3e-app-bar-large-subtitle-text-line-height, ${j.typescale.standard.title.medium.lineHeight})`),
		subtitleTextTracking: c(`var(--m3e-app-bar-large-subtitle-text-tracking, ${j.typescale.standard.title.medium.tracking})`),
		headingPaddingLeft: c(`var(--m3e-app-bar-large-heading-padding-left, ${j.measurement.space200})`),
		headingPaddingRight: c(`var(--m3e-app-bar-large-heading-padding-right, ${j.measurement.space50})`),
		paddingTop: c(`var(--m3e-app-bar-large-padding-top, ${j.measurement.space100})`),
		paddingBottom: c(`var(--m3e-app-bar-large-padding-bottom, ${j.measurement.space150})`),
		titleMaxLines: c("var(--m3e-app-bar-large-title-max-lines, 2)"),
		subtitleMaxLines: c("var(--m3e-app-bar-large-subtitle-max-lines, 1)")
	}
};
function Ws(e) {
	let t = c(`:is(:state(--${e}), :--${e})`);
	return l`:host(${t}) .base:not(.with-subtitle) { min-height: ${Us[e].containerHeight}; } :host(${t}) .base.with-subtitle { min-height: ${Us[e].containerHeightWithSubtitle ?? Us[e].containerHeight}; } :host(${t}) .title { font-size: ${Us[e].titleTextFontSize}; font-weight: ${Us[e].titleTextFontWeight}; line-height: ${Us[e].titleTextLineHeight}; letter-spacing: ${Us[e].titleTextTracking}; } :host(${t}) .subtitle { font-size: ${Us[e].subtitleTextFontSize}; font-weight: ${Us[e].subtitleTextFontWeight}; line-height: ${Us[e].subtitleTextLineHeight}; letter-spacing: ${Us[e].subtitleTextTracking}; } :host(:not([centered])${t}) .label { padding-inline-start: ${Us[e].headingPaddingLeft}; padding-inline-end: ${Us[e].headingPaddingRight}; } :host([centered]${t}) .label { padding-inline: ${Us[e].headingPaddingLeft}; } :host(${t}) .base { padding-block-start: ${Us[e].paddingTop ?? c("unset")}; padding-block-end: ${Us[e].paddingBottom ?? c("unset")}; } ${Us[e].titleMaxLines ? l`:host(${t}) .title { display: -webkit-box; -webkit-line-clamp: ${Us[e].titleMaxLines}; -webkit-box-orient: vertical; overflow: hidden; line-clamp: ${Us[e].titleMaxLines}; }` : l``} ${Us[e].subtitleMaxLines ? l`:host(${t}) .subtitle { display: -webkit-box; -webkit-line-clamp: ${Us[e].subtitleMaxLines}; -webkit-box-orient: vertical; overflow: hidden; line-clamp: ${Us[e].subtitleMaxLines}; }` : l``}`;
}
var Gs = [
	Ws("small"),
	Ws("medium"),
	Ws("large")
], Ks = {
	containerColor: c(`var(--m3e-app-bar-container-color, ${j.color.surface})`),
	containerColorOnScroll: c(`var(--m3e-app-bar-container-color-on-scroll, ${j.color.surfaceContainer})`),
	containerElevation: c(`var(--m3e-app-bar-container-elevation, ${j.elevation.level0})`),
	containerElevationOnScroll: c(`var(--m3e-app-bar-container-elevation-on-scroll, ${j.elevation.level1})`),
	titleTextColor: c(`var(--m3e-app-bar-title-text-color, ${j.color.onSurface})`),
	subtitleTextColor: c(`var(--m3e-app-bar-subtitle-text-color, ${j.color.onSurfaceVariant})`),
	paddingLeft: c(`var(--m3e-app-bar-padding-left, ${j.measurement.space50})`),
	paddingRight: c(`var(--m3e-app-bar-padding-right, ${j.measurement.space50})`)
}, qs = l`:host { display: block; flex: none; } :host([hidden]) { display: none; } :host(:is(:state(--small), :--small)) .base, :host(:not(:is(:state(--small), :--small)):not([centered])) .heading { padding-inline-start: ${Ks.paddingLeft}; padding-inline-end: ${Ks.paddingRight}; } .base { box-sizing: border-box; display: flex; transition: ${c(`background-color ${j.motion.duration.medium1} ${j.motion.easing.standard},
      box-shadow ${j.motion.duration.medium1} ${j.motion.easing.standard}`)}; } .base:not(.on-scroll) { background-color: ${Ks.containerColor}; box-shadow: ${Ks.containerElevation}; } .base.on-scroll { background-color: ${Ks.containerColorOnScroll}; box-shadow: ${Ks.containerElevationOnScroll}; } .leading-icon, .trailing-icon { display: flex; flex: none; align-items: center; --_icon-button-size: auto; --_icon-button-min-size: 48px; } .leading-icon { min-width: var(--_leading-icon-min-width); } .trailing-icon { min-width: var(--_trailing-icon-min-width); } .heading { display: flex; align-items: center; } :host(:is(:state(--small), :--small)) .heading { flex: 1 1 auto; } .spacer { flex: 1 1 auto; } .label { display: flex; flex-direction: column; flex: none; } .title { color: ${Ks.titleTextColor}; } .subtitle { color: ${Ks.subtitleTextColor}; } .base:not(.with-title) .title, .base:not(.with-subtitle) .subtitle, .base:not(.with-title):not(.with-subtitle) .label, .base:not(.with-trailing-icon) .trailing-icon { display: none; } :host(:is(:state(--small), :--small)) .base { align-items: center; } :host(:is(:state(--small), :--small)) .heading { min-width: 0; } :host(:is(:state(--small), :--small)) .label { flex: 1 1 auto; min-width: 0; } :host(:is(:state(--small), :--small)) .title, :host(:is(:state(--small), :--small)) .subtitle { display: block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; } :host(:not(:is(:state(--small), :--small))) .base { flex-direction: column; } :host([centered]) .title, :host([centered]) .subtitle { text-align: center; } @media (prefers-reduced-motion) { .base { transition: none; } } @media (forced-colors: active) { .base { transition: none; } .base:not(.on-scroll), .base.on-scroll { background-color: Canvas; box-shadow: unset; } .title { color: CanvasText; } .subtitle { color: FieldText; } }`, Js, Ys, Xs, Zs, Qs, $s, ec, tc, nc, rc, ic, ac, oc, sc = class extends Ai(M(E(S), "banner")) {
	constructor() {
		super(...arguments), Js.add(this), Ys.set(this, (e) => this._updateScroll(e)), Xs.set(this, () => t(this, Js, "m", ac).call(this)), this.centered = !1, this.size = "small";
	}
	attach(e) {
		super.attach(e), e instanceof HTMLIFrameElement ? (e.addEventListener("load", t(this, Xs, "f")), t(this, Js, "m", ac).call(this)) : e.addEventListener("scroll", t(this, Ys, "f"), { passive: !0 });
	}
	detach() {
		this.control instanceof HTMLIFrameElement && (this.control.removeEventListener("load", t(this, Xs, "f")), this.control.contentDocument?.removeEventListener("scroll", t(this, Ys, "f"))), this.control && this.control.removeEventListener("scroll", t(this, Ys, "f")), super.detach();
	}
	connectedCallback() {
		super.connectedCallback(), t(this, Js, "m", Zs).call(this);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("size") && t(this, Js, "m", Zs).call(this);
	}
	updated(e) {
		super.updated(e), (e.has("centered") || e.has("size")) && (this.centered && this.size === "small" ? t(this, Js, "m", ic).call(this) : t(this, Js, "m", rc).call(this));
	}
	render() {
		switch (this.size) {
			case "small": return b`<div class="base"><div class="leading-icon"><slot name="leading" @slotchange="${t(this, Js, "m", ec)}"><slot name="leading-icon" @slotchange="${t(this, Js, "m", nc)}"></slot></slot></div><div class="heading"><div class="label"><div class="title"><slot name="title" @slotchange="${t(this, Js, "m", Qs)}"></slot></div><div class="subtitle"><slot name="subtitle" @slotchange="${t(this, Js, "m", $s)}"></slot></div></div></div><div class="trailing-icon"><slot name="trailing" @slotchange="${t(this, Js, "m", tc)}"><slot name="trailing-icon" @slotchange="${t(this, Js, "m", nc)}"></slot></slot></div></div>`;
			default: return b`<div class="base"><div class="heading"><div class="leading-icon"><slot name="leading" @slotchange="${t(this, Js, "m", ec)}"><slot name="leading-icon" @slotchange="${t(this, Js, "m", nc)}"></slot></slot></div><div class="spacer"></div><div class="trailing-icon"><slot name="trailing" @slotchange="${t(this, Js, "m", tc)}"><slot name="trailing-icon" @slotchange="${t(this, Js, "m", nc)}"></slot></slot></div></div><div class="spacer"></div><div class="label"><div class="title"><slot name="title" @slotchange="${t(this, Js, "m", Qs)}"></slot></div><div class="subtitle"><slot name="subtitle" @slotchange="${t(this, Js, "m", $s)}"></slot></div></div></div>`;
		}
	}
	_updateScroll(e) {
		let n = 0;
		this.control instanceof HTMLIFrameElement ? n = t(this, Js, "m", oc).call(this, this.control) : e.target instanceof HTMLElement && (n = e.target.scrollTop), this._base?.classList.toggle("on-scroll", n > 0);
	}
};
Ys = /* @__PURE__ */ new WeakMap(), Xs = /* @__PURE__ */ new WeakMap(), Js = /* @__PURE__ */ new WeakSet(), Zs = function() {
	Hs(this.size) || (this.size = "small"), wr(this, this.size, "small", "medium", "large");
}, Qs = function(e) {
	this._base?.classList.toggle("with-title", Ft(e.target));
}, $s = function(e) {
	this._base?.classList.toggle("with-subtitle", Ft(e.target));
}, ec = function(e) {
	this._base?.classList.toggle("with-leading-icon", Ft(e.target)), this.centered && this.size === "small" && setTimeout(() => t(this, Js, "m", ic).call(this), 40);
}, tc = function(e) {
	this._base?.classList.toggle("with-trailing-icon", Ft(e.target)), this.centered && this.size === "small" && setTimeout(() => t(this, Js, "m", ic).call(this), 40);
}, nc = function(e) {
	let t = e.target.name, n = t.replace("-icon", "");
	console.warn(`[m3e-app-bar] Slot "${t}" is deprecated and will be removed in a future release. Use "${n}" instead.`);
}, rc = function() {
	this._base?.style.removeProperty("--_leading-icon-min-width"), this._base?.style.removeProperty("--_trailing-icon-min-width");
}, ic = function() {
	t(this, Js, "m", rc).call(this);
	let e = this._leadingIcon?.getBoundingClientRect().width ?? 0, n = this._trailingIcon?.getBoundingClientRect().width ?? 0;
	e < n ? this._base?.style.setProperty("--_leading-icon-min-width", `${n}px`) : e > n && this._base?.style.setProperty("--_trailing-icon-min-width", `${e}px`);
}, ac = function() {
	this.control instanceof HTMLIFrameElement && (this.control.contentDocument?.addEventListener("scroll", t(this, Ys, "f"), { passive: !0 }), this._base?.classList.toggle("on-scroll", t(this, Js, "m", oc).call(this, this.control) > 0));
}, oc = function(e) {
	return Math.max(e.contentDocument?.documentElement?.scrollTop ?? 0, e.contentDocument?.body?.scrollTop ?? 0);
}, sc.styles = [qs, Gs], e([w(".base")], sc.prototype, "_base", void 0), e([w(".leading-icon")], sc.prototype, "_leadingIcon", void 0), e([w(".trailing-icon")], sc.prototype, "_trailingIcon", void 0), e([C({
	type: Boolean,
	reflect: !0
})], sc.prototype, "centered", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], sc.prototype, "size", void 0), e([nr(40)], sc.prototype, "_updateScroll", null), sc = e([T("m3e-app-bar")], sc);
//#endregion
//#region node_modules/@m3e/web/dist/button.js
function cc(e) {
	return e === "rounded" || e === "square";
}
function lc(e) {
	return e === "extra-small" || e === "small" || e === "medium" || e === "large" || e === "extra-large";
}
function uc(e) {
	return e === "elevated" || e === "filled" || e === "tonal" || e === "outlined";
}
var H = {
	"extra-small": {
		containerHeight: c(`calc(var(--m3e-button-extra-small-container-height, var(--m3e-button-container-height, 32px)) + ${j.density.calc(-3)})`),
		outlineThickness: c("var(--m3e-button-extra-small-outline-thickness, var(--m3e-button-outline-thickness, 1px))"),
		labelTextFontSize: c(`var(--m3e-button-extra-small-label-text-font-size, var(--m3e-button-label-text-font-size, ${j.typescale.standard.label.large.fontSize}))`),
		labelTextFontWeight: c(`var(--m3e-button-extra-small-label-text-font-weight, var(--m3e-button-label-text-font-weight, ${j.typescale.standard.label.large.fontWeight}))`),
		labelTextLineHeight: c(`var(--m3e-button-extra-small-label-text-line-height, var(--m3e-button-label-text-line-height, ${j.typescale.standard.label.large.lineHeight}))`),
		labelTextTracking: c(`var(--m3e-button-extra-small-label-text-tracking, var(--m3e-button-label-text-tracking, ${j.typescale.standard.label.large.tracking}))`),
		iconSize: c("var(--m3e-button-extra-small-icon-size, var(--m3e-button-icon-size, 20px))"),
		shapeRound: c(`var(--m3e-button-extra-small-shape-round, var(--m3e-button-shape-round, ${j.shape.corner.full}))`),
		shapeSquare: c(`var(--m3e-button-extra-small-shape-square, var(--m3e-button-shape-square, ${j.shape.corner.medium}))`),
		selectedShapeRound: c(`var(--m3e-button-extra-small-selected-shape-round, var(--m3e-button-selected-shape-round, ${j.shape.corner.medium}))`),
		selectedShapeSquare: c(`var(--m3e-button-extra-small-selected-shape-square, var(--m3e-button-selected-shape-square, ${j.shape.corner.full}))`),
		shapePressedMorph: c(`var(--m3e-button-extra-small-shape-pressed-morph, var(--m3e-button-shape-pressed-morph, ${j.shape.corner.small}))`),
		leadingSpace: c(`var(--m3e-button-extra-small-leading-space, var(--m3e-button-leading-space, ${j.measurement.space150}))`),
		trailingSpace: c(`var(--m3e-button-extra-small-trailing-space, var(--m3e-button-trailing-space, ${j.measurement.space150}))`),
		iconLabelSpace: c(`var(--m3e-button-extra-small-icon-label-space, var(--m3e-button-icon-label-space, ${j.measurement.space100}))`)
	},
	small: {
		containerHeight: c(`calc(var(--m3e-button-small-container-height, var(--m3e-button-container-height, 40px)) + ${j.density.calc(-3)})`),
		outlineThickness: c("var(--m3e-button-small-outline-thickness, var(--m3e-button-outline-thickness, 1px))"),
		labelTextFontSize: c(`var(--m3e-button-small-label-text-font-size, var(--m3e-button-label-text-font-size, ${j.typescale.standard.label.large.fontSize}))`),
		labelTextFontWeight: c(`var(--m3e-button-small-label-text-font-weight, var(--m3e-button-label-text-font-weight, ${j.typescale.standard.label.large.fontWeight}))`),
		labelTextLineHeight: c(`var(--m3e-button-small-label-text-line-height, var(--m3e-button-label-text-line-height, ${j.typescale.standard.label.large.lineHeight}))`),
		labelTextTracking: c(`var(--m3e-button-small-label-text-tracking, var(--m3e-button-label-text-tracking, ${j.typescale.standard.label.large.tracking}))`),
		iconSize: c("var(--m3e-button-small-icon-size, var(--m3e-button-icon-size, 20px))"),
		shapeRound: c(`var(--m3e-button-small-shape-round, var(--m3e-button-shape-round, ${j.shape.corner.full}))`),
		shapeSquare: c(`var(--m3e-button-small-shape-square, var(--m3e-button-shape-square, ${j.shape.corner.medium}))`),
		selectedShapeRound: c(`var(--m3e-button-small-selected-shape-round, var(--m3e-button-selected-shape-round, ${j.shape.corner.medium}))`),
		selectedShapeSquare: c(`var(--m3e-button-small-selected-shape-square, var(--m3e-button-selected-shape-square, ${j.shape.corner.full}))`),
		shapePressedMorph: c(`var(--m3e-button-small-shape-pressed-morph, var(--m3e-button-shape-pressed-morph, ${j.shape.corner.small}))`),
		leadingSpace: c(`var(--m3e-button-small-leading-space, var(--m3e-button-leading-space, ${j.measurement.space250}))`),
		trailingSpace: c(`var(--m3e-button-small-trailing-space, var(--m3e-button-trailing-space, ${j.measurement.space250}))`),
		iconLabelSpace: c(`var(--m3e-button-small-icon-label-space, var(--m3e-button-icon-label-space, ${j.measurement.space100}))`)
	},
	medium: {
		containerHeight: c(`calc(var(--m3e-button-medium-container-height, var(--m3e-button-container-height, 56px)) + ${j.density.calc(-3)})`),
		outlineThickness: c("var(--m3e-button-medium-outline-thickness, var(--m3e-button-outline-thickness, 1px))"),
		labelTextFontSize: c(`var(--m3e-button-medium-label-text-font-size, var(--m3e-button-label-text-font-size, ${j.typescale.standard.body.large.fontSize}))`),
		labelTextFontWeight: c(`var(--m3e-button-medium-label-text-font-weight, var(--m3e-button-label-text-font-weight, ${j.typescale.standard.body.large.fontWeight}))`),
		labelTextLineHeight: c(`var(--m3e-button-medium-label-text-line-height, var(--m3e-button-label-text-line-height, ${j.typescale.standard.body.large.lineHeight}))`),
		labelTextTracking: c(`var(--m3e-button-medium-label-text-tracking, var(--m3e-button-label-text-tracking, ${j.typescale.standard.body.large.tracking}))`),
		iconSize: c("var(--m3e-button-medium-icon-size, var(--m3e-button-icon-size, 24px))"),
		shapeRound: c(`var(--m3e-button-medium-shape-round, var(--m3e-button-shape-round, ${j.shape.corner.full}))`),
		shapeSquare: c(`var(--m3e-button-medium-shape-square, var(--m3e-button-shape-square, ${j.shape.corner.large}))`),
		selectedShapeRound: c(`var(--m3e-button-medium-selected-shape-round, var(--m3e-button-selected-shape-round, ${j.shape.corner.large}))`),
		selectedShapeSquare: c(`var(--m3e-button-medium-selected-shape-square, var(--m3e-button-selected-shape-square, ${j.shape.corner.full}))`),
		shapePressedMorph: c(`var(--m3e-button-medium-shape-pressed-morph, var(--m3e-button-shape-pressed-morph, ${j.shape.corner.medium}))`),
		leadingSpace: c(`var(--m3e-button-medium-leading-space, var(--m3e-button-leading-space, ${j.measurement.space300}))`),
		trailingSpace: c(`var(--m3e-button-medium-trailing-space, var(--m3e-button-trailing-space, ${j.measurement.space300}))`),
		iconLabelSpace: c(`var(--m3e-button-medium-icon-label-space, var(--m3e-button-icon-label-space, ${j.measurement.space100}))`)
	},
	large: {
		containerHeight: c(`calc(var(--m3e-button-large-container-height, var(--m3e-button-container-height, 96px)) + ${j.density.calc(-3)})`),
		outlineThickness: c("var(--m3e-button-large-outline-thickness, var(--m3e-button-outline-thickness, 2px))"),
		labelTextFontSize: c(`var(--m3e-button-large-label-text-font-size, var(--m3e-button-label-text-font-size, ${j.typescale.standard.headline.small.fontSize}))`),
		labelTextFontWeight: c(`var(--m3e-button-large-label-text-font-weight, var(--m3e-button-label-text-font-weight, ${j.typescale.standard.headline.small.fontWeight}))`),
		labelTextLineHeight: c(`var(--m3e-button-large-label-text-line-height, var(--m3e-button-label-text-line-height, ${j.typescale.standard.headline.small.lineHeight}))`),
		labelTextTracking: c(`var(--m3e-button-large-label-text-tracking, var(--m3e-button-label-text-tracking, ${j.typescale.standard.headline.small.tracking}))`),
		iconSize: c("var(--m3e-button-large-icon-size, var(--m3e-button-icon-size, 32px))"),
		shapeRound: c(`var(--m3e-button-large-shape-round, var(--m3e-button-shape-round, ${j.shape.corner.full}))`),
		shapeSquare: c(`var(--m3e-button-large-shape-square, var(--m3e-button-shape-square, ${j.shape.corner.extraLarge}))`),
		selectedShapeRound: c(`var(--m3e-button-large-selected-shape-round, var(--m3e-button-selected-shape-round, ${j.shape.corner.extraLarge}))`),
		selectedShapeSquare: c(`var(--m3e-button-large-selected-shape-square, var(--m3e-button-selected-shape-square, ${j.shape.corner.full}))`),
		shapePressedMorph: c(`var(--m3e-button-large-shape-pressed-morph, var(--m3e-button-shape-pressed-morph, ${j.shape.corner.large}))`),
		leadingSpace: c(`var(--m3e-button-large-leading-space, var(--m3e-button-leading-space, ${j.measurement.space600}))`),
		trailingSpace: c(`var(--m3e-button-large-trailing-space, var(--m3e-button-trailing-space, ${j.measurement.space600}))`),
		iconLabelSpace: c(`var(--m3e-button-large-icon-label-space, var(--m3e-button-icon-label-space, ${j.measurement.space150}))`)
	},
	"extra-large": {
		containerHeight: c(`calc(var(--m3e-button-extra-large-container-height, var(--m3e-button-container-height, 136px)) + ${j.density.calc(-3)})`),
		outlineThickness: c("var(--m3e-button-extra-large-outline-thickness, var(--m3e-button-outline-thickness, 3px))"),
		labelTextFontSize: c(`var(--m3e-button-extra-large-label-text-font-size, var(--m3e-button-label-text-font-size, ${j.typescale.standard.headline.large.fontSize}))`),
		labelTextFontWeight: c(`var(--m3e-button-extra-large-label-text-font-weight, var(--m3e-button-label-text-font-weight, ${j.typescale.standard.headline.large.fontWeight}))`),
		labelTextLineHeight: c(`var(--m3e-button-extra-large-label-text-line-height, var(--m3e-button-label-text-line-height, ${j.typescale.standard.headline.large.lineHeight}))`),
		labelTextTracking: c(`var(--m3e-button-extra-large-label-text-tracking, var(--m3e-button-label-text-tracking, ${j.typescale.standard.headline.large.tracking}))`),
		iconSize: c("var(--m3e-button-extra-large-icon-size, var(--m3e-button-icon-size, 40px))"),
		shapeRound: c(`var(--m3e-button-extra-large-shape-round, var(--m3e-button-shape-round, ${j.shape.corner.full}))`),
		shapeSquare: c(`var(--m3e-button-extra-large-shape-square, var(--m3e-button-shape-square, ${j.shape.corner.extraLarge}))`),
		selectedShapeRound: c(`var(--m3e-button-extra-large-selected-shape-round, var(--m3e-button-selected-shape-round, ${j.shape.corner.extraLarge}))`),
		selectedShapeSquare: c(`var(--m3e-button-extra-large-selected-shape-square, var(--m3e-button-selected-shape-square, ${j.shape.corner.full}))`),
		shapePressedMorph: c(`var(--m3e-button-extra-large-shape-pressed-morph, var(--m3e-button-shape-pressed-morph, ${j.shape.corner.large}))`),
		leadingSpace: c(`var(--m3e-button-extra-large-leading-space, var(--m3e-button-leading-space, ${j.measurement.space800}))`),
		trailingSpace: c(`var(--m3e-button-extra-large-trailing-space, var(--m3e-button-trailing-space, ${j.measurement.space800}))`),
		iconLabelSpace: c(`var(--m3e-button-extra-large-icon-label-space, var(--m3e-button-icon-label-space, ${j.measurement.space200}))`)
	}
};
function dc(e) {
	let t = c(`:is(:state(--${e}), :--${e})`);
	return l`:host(${t}) .base { height: ${H[e].containerHeight}; } :host(${t}) .wrapper { padding-inline-start: calc(${H[e].leadingSpace} - calc(var(--_adjacent-shrink, 0px) / 2)); padding-inline-end: calc(${H[e].trailingSpace} - calc(var(--_adjacent-shrink, 0px) / 2)); column-gap: ${H[e].iconLabelSpace}; } :host(${t}) .label { font-size: ${H[e].labelTextFontSize}; font-weight: ${H[e].labelTextFontWeight}; line-height: ${H[e].labelTextLineHeight}; letter-spacing: ${H[e].labelTextTracking}; } :host(${t}) .icon { font-size: ${H[e].iconSize}; --m3e-icon-size: ${H[e].iconSize}; } :host(${t}) .base { outline-offset: calc(0px - ${H[e].outlineThickness}); outline-width: ${H[e].outlineThickness}; } :host(:not(:is(:state(--connected), :--connected))${t}:is(:state(--rounded), :--rounded)) .base { border-radius: var(--_button-shape, ${H[e].shapeRound}); } :host( :is(:state(--connected), :--connected)${t}:is(:state(--rounded), :--rounded)[toggle][selected]:not( :is(:state(--pressed), :--pressed) ) ) .base { border-radius: var(--_button-shape, ${H[e].shapeRound}); } :host( :not(:is(:state(--connected), :--connected))${t}:is(:state(--rounded), :--rounded)[toggle][selected]:not( :is(:state(--pressed), :--pressed) ) ) .base { border-radius: ${H[e].selectedShapeRound}; } :host(:not(:is(:state(--connected), :--connected))${t}:is(:state(--square), :--square)) .base { border-radius: ${H[e].shapeSquare}; } :host( :not(:is(:state(--connected), :--connected))${t}:is(:state(--square), :--square)[toggle][selected]:not( :is(:state(--pressed), :--pressed) ) ) .base { border-radius: var(--_button-shape, ${H[e].selectedShapeSquare}); } :host(:not(:is(:state(--connected), :--connected))${t}:is(:state(--pressed), :--pressed)) .base { border-radius: ${H[e].shapePressedMorph}; } :host(:is(:state(--connected), :--connected)${t}:is(:state(--rounded), :--rounded):not([toggle][selected])) .base { border-start-start-radius: var( --_button-rounded-start-shape, var(--_button-shape, ${H[e].shapeRound}) ); border-end-start-radius: var( --_button-rounded-start-shape, var(--_button-shape, ${H[e].shapeRound}) ); border-start-end-radius: var( --_button-rounded-end-shape, var(--_button-shape, ${H[e].shapeRound}) ); border-end-end-radius: var( --_button-rounded-end-shape, var(--_button-shape, ${H[e].shapeRound}) ); } :host(:is(:state(--connected), :--connected)${t}:is(:state(--square), :--square)) .base { border-start-start-radius: var(--_button-square-start-shape, ${H[e].shapeSquare}); border-end-start-radius: var(--_button-square-start-shape, ${H[e].shapeSquare}); border-start-end-radius: var(--_button-square-end-shape, ${H[e].shapeSquare}); border-end-end-radius: var(--_button-square-end-shape, ${H[e].shapeSquare}); } :host( :is(:state(--connected), :--connected)${t}:is(:state(--square), :--square)[toggle][selected]:not( :is(:state(--pressed), :--pressed) ) ) .base { border-radius: var(--_button-shape, ${H[e].selectedShapeSquare}); } :host(:is(:state(--connected), :--connected)${t}:is(:state(--pressed), :--pressed)) .base { border-start-start-radius: var(--_button-start-shape-pressed-morph, ${H[e].shapePressedMorph}); border-end-start-radius: var(--_button-start-shape-pressed-morph, ${H[e].shapePressedMorph}); border-start-end-radius: var(--_button-end-shape-pressed-morph, ${H[e].shapePressedMorph}); border-end-end-radius: var(--_button-end-shape-pressed-morph, ${H[e].shapePressedMorph}); }`;
}
var fc = [
	dc("extra-small"),
	dc("small"),
	dc("medium"),
	dc("large"),
	dc("extra-large")
], pc = l`:host { display: inline-block; outline: none; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } :host([hidden]) { display: none; } .base { box-sizing: border-box; vertical-align: middle; display: inline-flex; align-items: center; justify-content: center; position: relative; width: 100%; transition: ${c(`background-color ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; } .touch { position: absolute; height: 48px; left: 0; right: 0; } :host(:is(:state(--pressed), :--pressed)) .base, :host(:is(:state(--resting), :--resting)) .base { transition: ${c(`background-color ${j.motion.duration.short4} ${j.motion.easing.standard},
          border-radius ${j.motion.spring.fastEffects}`)}; } .wrapper { box-sizing: border-box; width: 100%; overflow: hidden; display: inline-flex; align-items: center; justify-content: center; } .label { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; transition: ${c(`color ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; } .icon { transition: ${c(`color ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; } :host(:not(:disabled):not([disabled-interactive])) { cursor: pointer; } :host([disabled-interactive]) { cursor: not-allowed; } ::slotted([slot="icon"]), ::slotted([slot="selected-icon"]), ::slotted([slot="trailing-icon"]) { font-size: inherit !important; flex: none; } ::slotted(svg[slot="icon"]), ::slotted(svg[slot="selected-icon"]), ::slotted(svg[slot="trailing-icon"]) { width: 1em; height: 1em; } :host([toggle]:not([selected])) .base.with-selected-icon slot[name="selected-icon"], :host([toggle][selected]) .base.with-selected-icon slot[name="icon"] { display: none; } a { all: unset; display: block; position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px; z-index: 1; } :host(:is(:state(--grouped), :--grouped):is(:state(--connected), :--connected)) { flex: 1 1 auto; } :host(:is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected))) { transition: ${c(`width ${j.motion.spring.fastEffects}`)}; } :host(:is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected))) .wrapper { transition: ${c(`padding-inline ${j.motion.spring.fastEffects}`)}; } :host(:is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected))) { flex-shrink: 0; flex-grow: 0; } :host( :is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected)):not( :is(:state(--pressed), :--pressed, :state(--adjacent-pressed), :--adjacent-pressed) ) ) { width: var(--_button-width); } :host( :is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected)):not( :is(:state(--pressed), :--pressed) ):is(:state(--adjacent-pressed), :--adjacent-pressed) ) { width: calc(var(--_button-width) - var(--_adjacent-shrink, 0px)); } :host( :is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected)):is( :state(--pressed), :--pressed ):not([disabled-interactive]):not(:disabled) ) { width: calc( var(--_button-width) + calc(var(--_button-width) * var(--m3e-standard-button-group-width-multiplier, 0.15)) ); } :host( :is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected)):is( :state(--adjacent-pressed), :--adjacent-pressed ):not(:is(:state(--pressed), :--pressed)) ) .label { text-overflow: clip; } @media (forced-colors: active) { .base, .label, .icon { transition: none; } :host(:is(:state(--pressed), :--pressed)) .base, :host(:is(:state(--resting), :--resting)) .base { transition: ${c(`border-radius ${j.motion.spring.fastEffects}`)}; } :host(:not(:disabled):not([disabled-interactive]):not([toggle])) .base { background-color: ButtonFace !important; outline-color: ButtonText !important; } :host(:not(:disabled):not([disabled-interactive]):not([toggle])) .label, :host(:not(:disabled):not([disabled-interactive]):not([toggle])) .icon { color: ButtonText !important; } :host(:not(:disabled):not([disabled-interactive])[toggle]:not([selected])) .base { background-color: ButtonFace !important; outline-color: ButtonText !important; } :host(:not(:disabled):not([disabled-interactive])[toggle]:not([selected])) .label, :host(:hover:not(:disabled):not([disabled-interactive])[toggle]:not([selected])) .label, :host(:not(:disabled):not([disabled-interactive])[toggle]:not([selected]):focus) .label, :host(:not(:disabled):not([disabled-interactive])[toggle]:not([selected])) .icon, :host(:hover:not(:disabled):not([disabled-interactive])[toggle]:not([selected])) .icon, :host(:not(:disabled):not([disabled-interactive])[toggle]:not([selected]):focus) .icon { color: ButtonText !important; } :host(:not(:disabled):not([disabled-interactive])[toggle][selected]) .base { background-color: ButtonText !important; outline: none !important; } :host(:not(:disabled):not([disabled-interactive])[toggle][selected]) .label, :host(:hover:not(:disabled):not([disabled-interactive])[toggle][selected]) .label, :host(:not(:disabled):not([disabled-interactive])[toggle][selected]:focus) .label, :host(:not(:disabled):not([disabled-interactive])[toggle][selected]) .icon, :host(:hover:not(:disabled):not([disabled-interactive])[toggle][selected]) .icon, :host(:not(:disabled):not([disabled-interactive])[toggle][selected]:focus) .icon { forced-color-adjust: none; color: ButtonFace !important; background-color: ButtonText !important; } :host(:disabled) .base, :host([disabled-interactive]) .base { outline-color: GrayText !important; background-color: unset !important; } :host(:disabled) .label, :host([disabled-interactive]) .label, :host(:disabled) .icon, :host([disabled-interactive]) .icon { color: GrayText !important; } .base { outline-style: solid; } :host(:is(:state(--extra-small), :--extra-small)) .base { outline-offset: calc(0px - var(--m3e-button-extra-small-outline-thickness, 1px)); outline-width: var(--m3e-button-extra-small-outline-thickness, 1px); } :host(:is(:state(--small), :--small)) .base { outline-offset: calc(0px - var(--m3e-button-small-outline-thickness, 1px)); outline-width: var(--m3e-button-small-outline-thickness, 1px); } :host(:is(:state(--medium), :--medium)) .base { outline-offset: calc(0px - var(--m3e-button-medium-outline-thickness, 1px)); outline-width: var(--m3e-button-medium-outline-thickness, 1px); } :host(:is(:state(--large), :--large)) .base { outline-offset: calc(0px - var(--m3e-button-large-outline-thickness, 2px)); outline-width: var(--m3e-button-large-outline-thickness, 2px); } :host(:is(:state(--extra-large), :--extra-large)) .base { outline-offset: calc(0px - var(--m3e-button-extra-large-outline-thickness, 3px)); outline-width: var(--m3e-button-extra-large-outline-thickness, 3px); } } @media (prefers-reduced-motion) { :host(:is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected))), :host(:is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected))) .wrapper, :host(:is(:state(--pressed), :--pressed)) .base, :host(:is(:state(--resting), :--resting)) .base, .base, .label, .icon { transition: none; } }`, U = {
	elevated: {
		labelTextColor: c(`var(--m3e-elevated-button-label-text-color, var(--m3e-button-label-text-color, ${j.color.primary}))`),
		iconColor: c(`var(--m3e-elevated-button-icon-color, var(--m3e-button-icon-color, ${j.color.primary}))`),
		containerColor: c(`var(--m3e-elevated-button-container-color, var(--m3e-button-container-color, ${j.color.surfaceContainerLow}))`),
		containerElevation: c(`var(--m3e-elevated-button-container-elevation, var(--m3e-button-container-elevation, ${j.elevation.level1}))`),
		unselectedLabelTextColor: c(`var(--m3e-elevated-button-unselected-label-text-color, var(--m3e-button-unselected-label-text-color, ${j.color.primary}))`),
		unselectedIconColor: c(`var(--m3e-elevated-button-unselected-icon-color, var(--m3e-button-unselected-icon-color, ${j.color.primary}))`),
		unselectedContainerColor: c(`var(--m3e-elevated-button-unselected-container-color, var(--m3e-button-unselected-container-color, ${j.color.surfaceContainerLow}))`),
		selectedLabelTextColor: c(`var(--m3e-elevated-button-selected-label-text-color, var(--m3e-button-selected-label-text-color, ${j.color.onPrimary}))`),
		selectedIconColor: c(`var(--m3e-elevated-button-selected-icon-color, var(--m3e-button-selected-icon-color, ${j.color.onPrimary}))`),
		selectedContainerColor: c(`var(--m3e-elevated-button-selected-container-color, var(--m3e-button-selected-container-color, ${j.color.primary}))`),
		disabled: {
			containerColor: c(`var(--m3e-elevated-button-disabled-container-color, var(--m3e-button-disabled-container-color, ${j.color.onSurface}))`),
			containerOpacity: c("var(--m3e-elevated-button-disabled-container-opacity, var(--m3e-button-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-elevated-button-disabled-icon-color, var(--m3e-button-disabled-icon-color, ${j.color.onSurface}))`),
			iconOpacity: c("var(--m3e-elevated-button-disabled-icon-opacity, var(--m3e-button-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-elevated-button-disabled-label-text-color, var(--m3e-button-disabled-label-text-color, ${j.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-elevated-button-disabled-label-text-opacity, var(--m3e-button-disabled-label-text-opacity, 38%))"),
			containerElevation: c(`var(--m3e-elevated-button-disabled-container-elevation, var(--m3e-button-disabled-container-elevation, ${j.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-elevated-button-hover-icon-color, var(--m3e-button-hover-icon-color, ${j.color.primary}))`),
			labelTextColor: c(`var(--m3e-elevated-button-hover-label-text-color, var(--m3e-button-hover-label-text-color, ${j.color.primary}))`),
			stateLayerColor: c(`var(--m3e-elevated-button-hover-state-layer-color, var(--m3e-button-hover-state-layer-color, ${j.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-elevated-button-hover-state-layer-opacity, var(--m3e-button-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-elevated-button-hover-container-elevation, var(--m3e-button-hover-container-elevation, ${j.elevation.level2}))`),
			unselectedIconColor: c(`var(--m3e-elevated-button-hover-unselected-icon-color, var(--m3e-button-hover-unselected-icon-color, ${j.color.primary}))`),
			unselectedLabelTextColor: c(`var(--m3e-elevated-button-hover-unselected-label-text-color, var(--m3e-button-hover-unselected-label-text-color, ${j.color.primary}))`),
			unselectedStateLayerColor: c(`var(--m3e-elevated-button-hover-unselected-state-layer-color, var(--m3e-button-hover-unselected-state-layer-color, ${j.color.primary}))`),
			selectedIconColor: c(`var(--m3e-elevated-button-hover-selected-icon-color, var(--m3e-button-hover-selected-icon-color, ${j.color.onPrimary}))`),
			selectedLabelTextColor: c(`var(--m3e-elevated-button-hover-selected-label-text-color, var(--m3e-button-hover-selected-label-text-color, ${j.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-elevated-button-hover-selected-state-layer-color, var(--m3e-button-hover-selected-state-layer-color, ${j.color.onPrimary}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-elevated-button-focus-icon-color, var(--m3e-button-focus-icon-color, ${j.color.primary}))`),
			labelTextColor: c(`var(--m3e-elevated-button-focus-label-text-color, var(--m3e-button-focus-label-text-color, ${j.color.primary}))`),
			stateLayerColor: c(`var(--m3e-elevated-button-focus-state-layer-color, var(--m3e-button-focus-state-layer-color, ${j.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-elevated-button-focus-state-layer-opacity, var(--m3e-button-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-elevated-button-focus-container-elevation, var(--m3e-button-focus-container-elevation, ${j.elevation.level1}))`),
			unselectedLabelTextColor: c(`var(--m3e-elevated-button-focus-unselected-label-text-color, var(--m3e-button-focus-unselected-label-text-color, ${j.color.primary}))`),
			unselectedIconColor: c(`var(--m3e-elevated-button-focus-unselected-icon-color, var(--m3e-button-focus-unselected-icon-color, ${j.color.primary}))`),
			unselectedStateLayerColor: c(`var(--m3e-elevated-button-focus-unselected-state-layer-color, var(--m3e-button-focus-unselected-state-layer-color, ${j.color.primary}))`),
			selectedIconColor: c(`var(--m3e-elevated-button-focus-selected-icon-color, var(--m3e-button-focus-selected-icon-color, ${j.color.onPrimary}))`),
			selectedLabelTextColor: c(`var(--m3e-elevated-button-focus-selected-label-text-color, var(--m3e-button-focus-selected-label-text-color, ${j.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-elevated-button-focus-selected-state-layer-color, var(--m3e-button-focus-selected-state-layer-color, ${j.color.onPrimary}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-elevated-button-pressed-icon-color, var(--m3e-button-pressed-icon-color, ${j.color.primary}))`),
			labelTextColor: c(`var(--m3e-elevated-button-pressed-label-text-color, var(--m3e-button-pressed-label-text-color, ${j.color.primary}))`),
			stateLayerColor: c(`var(--m3e-elevated-button-pressed-state-layer-color, var(--m3e-button-pressed-state-layer-color, ${j.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-elevated-button-pressed-state-layer-opacity, var(--m3e-button-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-elevated-button-pressed-container-elevation, var(--m3e-button-pressed-container-elevation, ${j.elevation.level1}))`),
			unselectedLabelTextColor: c(`var(--m3e-elevated-button-pressed-unselected-label-text-color, var(--m3e-button-pressed-unselected-label-text-color, ${j.color.primary}))`),
			unselectedIconColor: c(`var(--m3e-elevated-button-pressed-unselected-icon-color, var(--m3e-button-pressed-unselected-icon-color, ${j.color.primary}))`),
			unselectedStateLayerColor: c(`var(--m3e-elevated-button-pressed-unselected-state-layer-color, var(--m3e-button-pressed-unselected-state-layer-color, ${j.color.primary}))`),
			selectedIconColor: c(`var(--m3e-elevated-button-pressed-selected-icon-color, var(--m3e-button-pressed-selected-icon-color, ${j.color.onPrimary}))`),
			selectedLabelTextColor: c(`var(--m3e-elevated-button-pressed-selected-label-text-color, var(--m3e-button-pressed-selected-label-text-color, ${j.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-elevated-button-pressed-selected-state-layer-color, var(--m3e-button-pressed-selected-state-layer-color, ${j.color.onPrimary}))`)
		}
	},
	outlined: {
		labelTextColor: c(`var(--m3e-outlined-button-label-text-color, var(--m3e-button-label-text-color, ${j.color.onSurfaceVariant}))`),
		iconColor: c(`var(--m3e-outlined-button-icon-color, var(--m3e-button-icon-color, ${j.color.onSurfaceVariant}))`),
		outlineColor: c(`var(--m3e-outlined-button-outline-color, var(--m3e-button-outline-color, ${j.color.outlineVariant}))`),
		unselectedLabelTextColor: c(`var(--m3e-outlined-button-unselected-label-text-color, var(--m3e-button-unselected-label-text-color, ${j.color.onSurfaceVariant}))`),
		unselectedIconColor: c(`var(--m3e-outlined-button-unselected-icon-color, var(--m3e-button-unselected-icon-color, ${j.color.onSurfaceVariant}))`),
		selectedLabelTextColor: c(`var(--m3e-outlined-button-selected-label-text-color, var(--m3e-button-selected-label-text-color, ${j.color.inverseOnSurface}))`),
		selectedIconColor: c(`var(--m3e-outlined-button-selected-icon-color, var(--m3e-button-selected-icon-color, ${j.color.inverseOnSurface}))`),
		selectedContainerColor: c(`var(--m3e-outlined-button-selected-container-color, var(--m3e-button-selected-container-color, ${j.color.inverseSurface}))`),
		disabled: {
			containerColor: c(`var(--m3e-outlined-button-disabled-container-color, var(--m3e-button-disabled-container-color, ${j.color.onSurface}))`),
			containerOpacity: c("var(--m3e-outlined-button-disabled-container-opacity, var(--m3e-button-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-outlined-button-disabled-icon-color, var(--m3e-button-disabled-icon-color, ${j.color.onSurface}))`),
			iconOpacity: c("var(--m3e-outlined-button-disabled-icon-opacity, var(--m3e-button-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-outlined-button-disabled-label-text-color, var(--m3e-button-disabled-label-text-color, ${j.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-outlined-button-disabled-label-text-opacity, var(--m3e-button-disabled-label-text-opacity, 38%))"),
			outlineColor: c(`var(--m3e-outlined-button-disabled-outline-color, var(--m3e-button-disabled-outline-color, ${j.color.outlineVariant}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-outlined-button-hover-icon-color, var(--m3e-button-hover-icon-color, ${j.color.onSurfaceVariant}))`),
			labelTextColor: c(`var(--m3e-outlined-button-hover-label-text-color, var(--m3e-button-hover-label-text-color, ${j.color.onSurfaceVariant}))`),
			outlineColor: c(`var(--m3e-outlined-button-hover-outline-color, var(--m3e-button-hover-outline-color, ${j.color.outlineVariant}))`),
			stateLayerColor: c(`var(--m3e-outlined-button-hover-state-layer-color, var(--m3e-button-hover-state-layer-color, ${j.color.onSurfaceVariant}))`),
			stateLayerOpacity: c(`var(--m3e-outlined-button-hover-state-layer-opacity, var(--m3e-button-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-outlined-button-hover-unselected-icon-color, var(--m3e-button-hover-unselected-icon-color, ${j.color.onSurfaceVariant}))`),
			unselectedLabelTextColor: c(`var(--m3e-outlined-button-hover-unselected-label-text-color, var(--m3e-button-hover-unselected-label-text-color, ${j.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-outlined-button-hover-unselected-state-layer-color, var(--m3e-button-hover-unselected-state-layer-color, ${j.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-outlined-button-hover-selected-icon-color, var(--m3e-button-hover-selected-icon-color, ${j.color.inverseOnSurface}))`),
			selectedLabelTextColor: c(`var(--m3e-outlined-button-hover-selected-label-text-color, var(--m3e-button-hover-selected-label-text-color, ${j.color.inverseOnSurface}))`),
			selectedStateLayerColor: c(`var(--m3e-outlined-button-hover-selected-state-layer-color, var(--m3e-button-hover-selected-state-layer-color, ${j.color.inverseOnSurface}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-outlined-button-focus-icon-color, var(--m3e-button-focus-icon-color, ${j.color.onSurfaceVariant}))`),
			labelTextColor: c(`var(--m3e-outlined-button-focus-label-text-color, var(--m3e-button-focus-label-text-color, ${j.color.onSurfaceVariant}))`),
			outlineColor: c(`var(--m3e-outlined-button-focus-outline-color, var(--m3e-button-focus-outline-color, ${j.color.outlineVariant}))`),
			stateLayerColor: c(`var(--m3e-outlined-button-focus-state-layer-color, var(--m3e-button-focus-state-layer-color, ${j.color.onSurfaceVariant}))`),
			stateLayerOpacity: c(`var(--m3e-outlined-button-focus-state-layer-opacity, var(--m3e-button-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-outlined-button-focus-unselected-icon-color, var(--m3e-button-focus-unselected-icon-color, ${j.color.onSurfaceVariant}))`),
			unselectedLabelTextColor: c(`var(--m3e-outlined-button-focus-unselected-label-text-color, var(--m3e-button-focus-unselected-label-text-color, ${j.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-outlined-button-focus-unselected-state-layer-color, var(--m3e-button-focus-unselected-state-layer-color, ${j.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-outlined-button-focus-selected-icon-color, var(--m3e-button-focus-selected-icon-color, ${j.color.inverseOnSurface}))`),
			selectedLabelTextColor: c(`var(--m3e-outlined-button-focus-selected-label-text-color, var(--m3e-button-focus-selected-label-text-color, ${j.color.inverseOnSurface}))`),
			selectedStateLayerColor: c(`var(--m3e-outlined-button-focus-selected-state-layer-color, var(--m3e-button-focus-selected-state-layer-color, ${j.color.inverseOnSurface}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-outlined-button-pressed-icon-color, var(--m3e-button-pressed-icon-color, ${j.color.onSurfaceVariant}))`),
			labelTextColor: c(`var(--m3e-outlined-button-pressed-label-text-color, var(--m3e-button-pressed-label-text-color, ${j.color.onSurfaceVariant}))`),
			outlineColor: c(`var(--m3e-outlined-button-pressed-outline-color, var(--m3e-button-pressed-outline-color, ${j.color.outlineVariant}))`),
			stateLayerColor: c(`var(--m3e-outlined-button-pressed-state-layer-color, var(--m3e-button-pressed-state-layer-color, ${j.color.onSurfaceVariant}))`),
			stateLayerOpacity: c(`var(--m3e-outlined-button-pressed-state-layer-opacity, var(--m3e-button-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-outlined-button-pressed-unselected-icon-color, var(--m3e-button-pressed-unselected-icon-color, ${j.color.onSurfaceVariant}))`),
			unselectedLabelTextColor: c(`var(--m3e-outlined-button-pressed-unselected-label-text-color, var(--m3e-button-pressed-unselected-label-text-color, ${j.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-outlined-button-pressed-unselected-state-layer-color, var(--m3e-button-pressed-unselected-state-layer-color, ${j.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-outlined-button-pressed-selected-icon-color, var(--m3e-button-pressed-selected-icon-color, ${j.color.inverseOnSurface}))`),
			selectedLabelTextColor: c(`var(--m3e-outlined-button-pressed-selected-label-text-color, var(--m3e-button-pressed-selected-label-text-color, ${j.color.inverseOnSurface}))`),
			selectedStateLayerColor: c(`var(--m3e-outlined-button-pressed-selected-state-layer-color, var(--m3e-button-pressed-selected-state-layer-color, ${j.color.inverseOnSurface}))`)
		}
	},
	filled: {
		labelTextColor: c(`var(--m3e-filled-button-label-text-color, var(--m3e-button-label-text-color, ${j.color.onPrimary}))`),
		iconColor: c(`var(--m3e-filled-button-icon-color, var(--m3e-button-icon-color, ${j.color.onPrimary}))`),
		containerColor: c(`var(--m3e-filled-button-container-color, var(--m3e-button-container-color, ${j.color.primary}))`),
		containerElevation: c(`var(--m3e-filled-button-container-elevation, var(--m3e-button-container-elevation, ${j.elevation.level0}))`),
		unselectedLabelTextColor: c(`var(--m3e-filled-button-unselected-label-text-color, var(--m3e-button-unselected-label-text-color, ${j.color.onSurfaceVariant}))`),
		unselectedIconColor: c(`var(--m3e-filled-button-unselected-icon-color, var(--m3e-button-unselected-icon-color, ${j.color.onSurfaceVariant}))`),
		unselectedContainerColor: c(`var(--m3e-filled-button-unselected-container-color, var(--m3e-button-unselected-container-color, ${j.color.surfaceContainer}))`),
		selectedLabelTextColor: c(`var(--m3e-filled-button-selected-label-text-color, var(--m3e-button-selected-label-text-color, ${j.color.onPrimary}))`),
		selectedIconColor: c(`var(--m3e-filled-button-selected-icon-color, var(--m3e-button-selected-icon-color, ${j.color.onPrimary}))`),
		selectedContainerColor: c(`var(--m3e-filled-button-selected-container-color, var(--m3e-button-selected-container-color, ${j.color.primary}))`),
		disabled: {
			containerColor: c(`var(--m3e-filled-button-disabled-container-color, var(--m3e-button-disabled-container-color, ${j.color.onSurface}))`),
			containerOpacity: c("var(--m3e-filled-button-disabled-container-opacity, var(--m3e-button-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-filled-button-disabled-icon-color, var(--m3e-button-disabled-icon-color, ${j.color.onSurface}))`),
			iconOpacity: c("var(--m3e-filled-button-disabled-icon-opacity, var(--m3e-button-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-filled-button-disabled-label-text-color, var(--m3e-button-disabled-label-text-color, ${j.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-filled-button-disabled-label-text-opacity, var(--m3e-button-disabled-label-text-opacity, 38%))"),
			containerElevation: c(`var(--m3e-filled-button-disabled-container-elevation, var(--m3e-button-disabled-container-elevation, ${j.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-filled-button-hover-icon-color, var(--m3e-button-hover-icon-color, ${j.color.onPrimary}))`),
			labelTextColor: c(`var(--m3e-filled-button-hover-label-text-color, var(--m3e-button-hover-label-text-color, ${j.color.onPrimary}))`),
			stateLayerColor: c(`var(--m3e-filled-button-hover-state-layer-color, var(--m3e-button-hover-state-layer-color, ${j.color.onPrimary}))`),
			stateLayerOpacity: c(`var(--m3e-filled-button-hover-state-layer-opacity, var(--m3e-button-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-filled-button-hover-container-elevation, var(--m3e-button-hover-container-elevation, ${j.elevation.level1}))`),
			unselectedIconColor: c(`var(--m3e-filled-button-hover-unselected-icon-color, var(--m3e-button-hover-unselected-icon-color, ${j.color.onSurfaceVariant}))`),
			unselectedLabelTextColor: c(`var(--m3e-filled-button-hover-unselected-label-text-color, var(--m3e-button-hover-unselected-label-text-color, ${j.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-filled-button-hover-unselected-state-layer-color, var(--m3e-button-hover-unselected-state-layer-color, ${j.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-filled-button-hover-selected-icon-color, var(--m3e-button-hover-selected-icon-color, ${j.color.onPrimary}))`),
			selectedLabelTextColor: c(`var(--m3e-filled-button-hover-selected-label-text-color, var(--m3e-button-hover-selected-label-text-color, ${j.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-filled-button-hover-selected-state-layer-color, var(--m3e-button-hover-selected-state-layer-color, ${j.color.onPrimary}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-filled-button-focus-icon-color, var(--m3e-button-focus-icon-color, ${j.color.onPrimary}))`),
			labelTextColor: c(`var(--m3e-filled-button-focus-label-text-color, var(--m3e-button-focus-label-text-color, ${j.color.onPrimary}))`),
			stateLayerColor: c(`var(--m3e-filled-button-focus-state-layer-color, var(--m3e-button-focus-state-layer-color, ${j.color.onPrimary}))`),
			stateLayerOpacity: c(`var(--m3e-filled-button-focus-state-layer-opacity, var(--m3e-button-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-filled-button-focus-container-elevation, var(--m3e-button-focus-container-elevation, ${j.elevation.level0}))`),
			unselectedIconColor: c(`var(--m3e-filled-button-focus-unselected-icon-color, var(--m3e-button-focus-unselected-icon-color, ${j.color.onSurfaceVariant}))`),
			unselectedLabelTextColor: c(`var(--m3e-filled-button-focus-unselected-label-text-color, var(--m3e-button-focus-unselected-label-text-color, ${j.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-filled-button-focus-unselected-state-layer-color, var(--m3e-button-focus-unselected-state-layer-color, ${j.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-filled-button-focus-selected-icon-color, var(--m3e-button-focus-selected-icon-color, ${j.color.onPrimary}))`),
			selectedLabelTextColor: c(`var(--m3e-filled-button-focus-selected-label-text-color, var(--m3e-button-focus-selected-label-text-color, ${j.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-filled-button-focus-selected-state-layer-color, var(--m3e-button-focus-selected-state-layer-color, ${j.color.onPrimary}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-filled-button-pressed-icon-color, var(--m3e-button-pressed-icon-color, ${j.color.onPrimary}))`),
			labelTextColor: c(`var(--m3e-filled-button-pressed-label-text-color, var(--m3e-button-pressed-label-text-color, ${j.color.onPrimary}))`),
			stateLayerColor: c(`var(--m3e-filled-button-pressed-state-layer-color, var(--m3e-button-pressed-state-layer-color, ${j.color.onPrimary}))`),
			stateLayerOpacity: c(`var(--m3e-filled-button-pressed-state-layer-opacity, var(--m3e-button-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-filled-button-pressed-container-elevation, var(--m3e-button-pressed-container-elevation, ${j.elevation.level0}))`),
			unselectedIconColor: c(`var(--m3e-filled-button-pressed-unselected-icon-color, var(--m3e-button-pressed-unselected-icon-color, ${j.color.onSurfaceVariant}))`),
			unselectedLabelTextColor: c(`var(--m3e-filled-button-pressed-unselected-label-text-color, var(--m3e-button-pressed-unselected-label-text-color, ${j.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-filled-button-pressed-unselected-state-layer-color, var(--m3e-button-pressed-unselected-state-layer-color, ${j.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-filled-button-pressed-selected-icon-color, var(--m3e-button-pressed-selected-icon-color, ${j.color.onPrimary}))`),
			selectedLabelTextColor: c(`var(--m3e-filled-button-pressed-selected-label-text-color, var(--m3e-button-pressed-selected-label-text-color, ${j.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-filled-button-pressed-selected-state-layer-color, var(--m3e-button-pressed-selected-state-layer-color, ${j.color.onPrimary}))`)
		}
	},
	tonal: {
		labelTextColor: c(`var(--m3e-tonal-button-label-text-color, var(--m3e-button-label-text-color, ${j.color.onSecondaryContainer}))`),
		iconColor: c(`var(--m3e-tonal-button-icon-color, var(--m3e-button-icon-color, ${j.color.onSecondaryContainer}))`),
		containerColor: c(`var(--m3e-tonal-button-container-color, var(--m3e-button-container-color, ${j.color.secondaryContainer}))`),
		containerElevation: c(`var(--m3e-tonal-button-container-elevation, var(--m3e-button-container-elevation, ${j.elevation.level0}))`),
		unselectedLabelTextColor: c(`var(--m3e-tonal-button-unselected-label-text-color, var(--m3e-button-unselected-label-text-color, ${j.color.onSecondaryContainer}))`),
		unselectedIconColor: c(`var(--m3e-tonal-button-unselected-icon-color, var(--m3e-button-unselected-icon-color, ${j.color.onSecondaryContainer}))`),
		unselectedContainerColor: c(`var(--m3e-tonal-button-unselected-container-color, var(--m3e-button-unselected-container-color, ${j.color.secondaryContainer}))`),
		selectedLabelTextColor: c(`var(--m3e-tonal-button-selected-label-text-color, var(--m3e-button-selected-label-text-color, ${j.color.onSecondary}))`),
		selectedIconColor: c(`var(--m3e-tonal-button-selected-icon-color, var(--m3e-button-selected-icon-color, ${j.color.onSecondary}))`),
		selectedContainerColor: c(`var(--m3e-tonal-button-selected-container-color, var(--m3e-button-selected-container-color, ${j.color.secondary}))`),
		disabled: {
			containerColor: c(`var(--m3e-tonal-button-disabled-container-color, var(--m3e-button-disabled-container-color, ${j.color.onSurface}))`),
			containerOpacity: c("var(--m3e-tonal-button-disabled-container-opacity, var(--m3e-button-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-tonal-button-disabled-icon-color, var(--m3e-button-disabled-icon-color, ${j.color.onSurface}))`),
			iconOpacity: c("var(--m3e-tonal-button-disabled-icon-opacity, var(--m3e-button-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-tonal-button-disabled-label-text-color, var(--m3e-button-disabled-label-text-color, ${j.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-tonal-button-disabled-label-text-opacity, var(--m3e-button-disabled-label-text-opacity, 38%))"),
			containerElevation: c(`var(--m3e-tonal-button-disabled-container-elevation, var(--m3e-button-disabled-container-elevation, ${j.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-tonal-button-hover-icon-color, var(--m3e-button-hover-icon-color, ${j.color.onSecondaryContainer}))`),
			labelTextColor: c(`var(--m3e-tonal-button-hover-label-text-color, var(--m3e-button-hover-label-text-color, ${j.color.onSecondaryContainer}))`),
			stateLayerColor: c(`var(--m3e-tonal-button-hover-state-layer-color, var(--m3e-button-hover-state-layer-color, ${j.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-tonal-button-hover-state-layer-opacity, var(--m3e-button-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-tonal-button-hover-container-elevation, var(--m3e-button-hover-container-elevation, ${j.elevation.level1}))`),
			unselectedIconColor: c(`var(--m3e-tonal-button-hover-unselected-icon-color, var(--m3e-button-hover-unselected-icon-color, ${j.color.onSecondaryContainer}))`),
			unselectedLabelTextColor: c(`var(--m3e-tonal-button-hover-unselected-label-text-color, var(--m3e-button-hover-unselected-label-text-color, ${j.color.onSecondaryContainer}))`),
			unselectedStateLayerColor: c(`var(--m3e-tonal-button-hover-unselected-state-layer-color, var(--m3e-button-hover-unselected-state-layer-color, ${j.color.onSecondaryContainer}))`),
			selectedIconColor: c(`var(--m3e-tonal-button-hover-selected-icon-color, var(--m3e-button-hover-selected-icon-color, ${j.color.onSecondary}))`),
			selectedLabelTextColor: c(`var(--m3e-tonal-button-hover-selected-label-text-color, var(--m3e-button-hover-selected-label-text-color, ${j.color.onSecondary}))`),
			selectedStateLayerColor: c(`var(--m3e-tonal-button-hover-selected-state-layer-color, var(--m3e-button-hover-selected-state-layer-color, ${j.color.onSecondary}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-tonal-button-focus-icon-color, var(--m3e-button-focus-icon-color, ${j.color.onSecondaryContainer}))`),
			labelTextColor: c(`var(--m3e-tonal-button-focus-label-text-color, var(--m3e-button-focus-label-text-color, ${j.color.onSecondaryContainer}))`),
			stateLayerColor: c(`var(--m3e-tonal-button-focus-state-layer-color, var(--m3e-button-focus-state-layer-color, ${j.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-tonal-button-focus-state-layer-opacity, var(--m3e-button-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-tonal-button-focus-container-elevation, var(--m3e-button-focus-container-elevation, ${j.elevation.level0}))`),
			unselectedIconColor: c(`var(--m3e-tonal-button-focus-unselected-icon-color, var(--m3e-button-focus-unselected-icon-color, ${j.color.onSecondaryContainer}))`),
			unselectedLabelTextColor: c(`var(--m3e-tonal-button-focus-unselected-label-text-color, var(--m3e-button-focus-unselected-label-text-color, ${j.color.onSecondaryContainer}))`),
			unselectedStateLayerColor: c(`var(--m3e-tonal-button-focus-unselected-state-layer-color, var(--m3e-button-focus-unselected-state-layer-color, ${j.color.onSecondaryContainer}))`),
			selectedIconColor: c(`var(--m3e-tonal-button-focus-selected-icon-color, var(--m3e-button-focus-selected-icon-color, ${j.color.onSecondary}))`),
			selectedLabelTextColor: c(`var(--m3e-tonal-button-focus-selected-label-text-color, var(--m3e-button-focus-selected-label-text-color, ${j.color.onSecondary}))`),
			selectedStateLayerColor: c(`var(--m3e-tonal-button-focus-selected-state-layer-color, var(--m3e-button-focus-selected-state-layer-color, ${j.color.onSecondary}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-tonal-button-pressed-icon-color, var(--m3e-button-pressed-icon-color, ${j.color.onSecondaryContainer}))`),
			labelTextColor: c(`var(--m3e-tonal-button-pressed-label-text-color, var(--m3e-button-pressed-label-text-color, ${j.color.onSecondaryContainer}))`),
			stateLayerColor: c(`var(--m3e-tonal-button-pressed-state-layer-color, var(--m3e-button-pressed-state-layer-color, ${j.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-tonal-button-pressed-state-layer-opacity, var(--m3e-button-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-tonal-button-pressed-container-elevation, var(--m3e-button-pressed-container-elevation, ${j.elevation.level0}))`),
			unselectedIconColor: c(`var(--m3e-tonal-button-pressed-unselected-icon-color, var(--m3e-button-pressed-unselected-icon-color, ${j.color.onSecondaryContainer}))`),
			unselectedLabelTextColor: c(`var(--m3e-tonal-button-pressed-unselected-label-text-color, var(--m3e-button-pressed-unselected-label-text-color, ${j.color.onSecondaryContainer}))`),
			unselectedStateLayerColor: c(`var(--m3e-tonal-button-pressed-unselected-state-layer-color, var(--m3e-button-pressed-unselected-state-layer-color, ${j.color.onSecondaryContainer}))`),
			selectedIconColor: c(`var(--m3e-tonal-button-pressed-selected-icon-color, var(--m3e-button-pressed-selected-icon-color, ${j.color.onSecondary}))`),
			selectedLabelTextColor: c(`var(--m3e-tonal-button-pressed-selected-label-text-color, var(--m3e-button-pressed-selected-label-text-color, ${j.color.onSecondary}))`),
			selectedStateLayerColor: c(`var(--m3e-tonal-button-pressed-selected-state-layer-color, var(--m3e-button-pressed-selected-state-layer-color, ${j.color.onSecondary}))`)
		}
	},
	text: {
		labelTextColor: c(`var(--m3e-text-button-label-text-color, var(--m3e-button-label-text-color, ${j.color.primary}))`),
		iconColor: c(`var(--m3e-text-button-icon-color, var(--m3e-button-icon-color, ${j.color.primary}))`),
		containerColor: c("var(--m3e-text-button-container-color)"),
		unselectedLabelTextColor: c(`var(--m3e-text-button-unselected-label-text-color, var(--m3e-button-unselected-label-text-color, ${j.color.primary}))`),
		unselectedIconColor: c(`var(--m3e-text-button-unselected-icon-color, var(--m3e-button-unselected-icon-color, ${j.color.primary}))`),
		unselectedContainerColor: c("var(--m3e-text-button-unselected-container-color)"),
		selectedLabelTextColor: c(`var(--m3e-text-button-selected-label-text-color, var(--m3e-button-selected-label-text-color, ${j.color.primary}))`),
		selectedIconColor: c(`var(--m3e-text-button-selected-icon-color, var(--m3e-button-selected-icon-color, ${j.color.primary}))`),
		selectedContainerColor: c("var(--m3e-text-button-selected-container-color)"),
		disabled: {
			containerColor: c(`var(--m3e-text-button-disabled-container-color, var(--m3e-button-disabled-container-color, ${j.color.onSurface}))`),
			containerOpacity: c("var(--m3e-text-button-disabled-container-opacity, var(--m3e-button-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-text-button-disabled-icon-color, var(--m3e-button-disabled-icon-color, ${j.color.onSurface}))`),
			iconOpacity: c("var(--m3e-text-button-disabled-icon-opacity, var(--m3e-button-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-text-button-disabled-label-text-color, var(--m3e-button-disabled-label-text-color, ${j.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-text-button-disabled-label-text-opacity, var(--m3e-button-disabled-label-text-opacity, 38%))")
		},
		hover: {
			iconColor: c(`var(--m3e-text-button-hover-icon-color, var(--m3e-button-hover-icon-color, ${j.color.primary}))`),
			labelTextColor: c(`var(--m3e-text-button-hover-label-text-color, var(--m3e-button-hover-label-text-color, ${j.color.primary}))`),
			stateLayerColor: c(`var(--m3e-text-button-hover-state-layer-color, var(--m3e-button-hover-state-layer-color, ${j.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-text-button-hover-state-layer-opacity, var(--m3e-button-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-text-button-hover-unselected-icon-color, var(--m3e-button-hover-unselected-icon-color, ${j.color.primary}))`),
			unselectedLabelTextColor: c(`var(--m3e-text-button-hover-unselected-label-text-color, var(--m3e-button-hover-unselected-label-text-color, ${j.color.primary}))`),
			unselectedStateLayerColor: c(`var(--m3e-text-button-hover-unselected-state-layer-color, var(--m3e-button-hover-unselected-state-layer-color, ${j.color.primary}))`),
			selectedIconColor: c(`var(--m3e-text-button-hover-selected-icon-color, var(--m3e-button-hover-selected-icon-color, ${j.color.primary}))`),
			selectedLabelTextColor: c(`var(--m3e-text-button-hover-selected-label-text-color, var(--m3e-button-hover-selected-label-text-color, ${j.color.primary}))`),
			selectedStateLayerColor: c(`var(--m3e-text-button-hover-selected-state-layer-color, var(--m3e-button-hover-selected-state-layer-color, ${j.color.primary}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-text-button-focus-icon-color, var(--m3e-button-focus-icon-color, ${j.color.primary}))`),
			labelTextColor: c(`var(--m3e-text-button-focus-label-text-color, var(--m3e-button-focus-label-text-color, ${j.color.primary}))`),
			stateLayerColor: c(`var(--m3e-text-button-focus-state-layer-color, var(--m3e-button-focus-state-layer-color, ${j.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-text-button-focus-state-layer-opacity, var(--m3e-button-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-text-button-focus-unselected-icon-color, var(--m3e-button-focus-unselected-icon-color, ${j.color.primary}))`),
			unselectedLabelTextColor: c(`var(--m3e-text-button-focus-unselected-label-text-color, var(--m3e-button-focus-unselected-label-text-color, ${j.color.primary}))`),
			unselectedStateLayerColor: c(`var(--m3e-text-button-focus-unselected-state-layer-color, var(--m3e-button-focus-unselected-state-layer-color, ${j.color.primary}))`),
			selectedIconColor: c(`var(--m3e-text-button-focus-selected-icon-color, var(--m3e-button-focus-selected-icon-color, ${j.color.primary}))`),
			selectedLabelTextColor: c(`var(--m3e-text-button-focus-selected-label-text-color, var(--m3e-button-focus-selected-label-text-color, ${j.color.primary}))`),
			selectedStateLayerColor: c(`var(--m3e-text-button-focus-selected-state-layer-color, var(--m3e-button-focus-selected-state-layer-color, ${j.color.primary}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-text-button-pressed-icon-color, var(--m3e-button-pressed-icon-color, ${j.color.primary}))`),
			labelTextColor: c(`var(--m3e-text-button-pressed-label-text-color, var(--m3e-button-pressed-label-text-color, ${j.color.primary}))`),
			stateLayerColor: c(`var(--m3e-text-button-pressed-state-layer-color, var(--m3e-button-pressed-state-layer-color, ${j.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-text-button-pressed-state-layer-opacity, var(--m3e-button-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-text-button-pressed-unselected-icon-color, var(--m3e-button-pressed-unselected-icon-color, ${j.color.primary}))`),
			unselectedLabelTextColor: c(`var(--m3e-text-button-pressed-unselected-label-text-color, var(--m3e-button-pressed-unselected-label-text-color, ${j.color.primary}))`),
			unselectedStateLayerColor: c(`var(--m3e-text-button-pressed-unselected-state-layer-color, var(--m3e-button-pressed-unselected-state-layer-color, ${j.color.primary}))`),
			selectedIconColor: c(`var(--m3e-text-button-pressed-selected-icon-color, var(--m3e-button-pressed-selected-icon-color, ${j.color.primary}))`),
			selectedLabelTextColor: c(`var(--m3e-text-button-pressed-selected-label-text-color, var(--m3e-button-pressed-selected-label-text-color, ${j.color.primary}))`),
			selectedStateLayerColor: c(`var(--m3e-text-button-pressed-selected-state-layer-color, var(--m3e-button-pressed-selected-state-layer-color, ${j.color.primary}))`)
		}
	}
};
function mc(e) {
	let t = c(`:is(:state(--${e}), :--${e})`);
	return l`:host(${t}:not(:disabled):not([disabled-interactive])) .base { background-color: ${U[e].containerColor ?? c("unset")}; --m3e-state-layer-hover-color: ${U[e].hover.stateLayerColor}; --m3e-state-layer-hover-opacity: ${U[e].hover.stateLayerOpacity}; --m3e-state-layer-focus-color: ${U[e].focus.stateLayerColor}; --m3e-state-layer-focus-opacity: ${U[e].focus.stateLayerOpacity}; --m3e-ripple-color: ${U[e].pressed.stateLayerColor}; --m3e-ripple-opacity: ${U[e].pressed.stateLayerOpacity}; --m3e-elevation-level: ${U[e].containerElevation ?? c("unset")}; --m3e-elevation-hover-level: ${U[e].hover.containerElevation ?? c("unset")}; --m3e-elevation-focus-level: ${U[e].focus.containerElevation ?? c("unset")}; --m3e-elevation-pressed-level: ${U[e].pressed.containerElevation ?? c("unset")}; } :host(${t}[toggle]:not([selected]):not(:disabled):not([disabled-interactive])) .base { background-color: ${U[e].unselectedContainerColor ?? c("unset")}; --m3e-state-layer-hover-color: ${U[e].hover.unselectedStateLayerColor}; --m3e-state-layer-focus-color: ${U[e].focus.unselectedStateLayerColor}; --m3e-ripple-color: ${U[e].pressed.unselectedStateLayerColor}; } :host(${t}[toggle][selected]:not(:disabled):not([disabled-interactive])) .base { background-color: ${U[e].selectedContainerColor ?? c("unset")}; --m3e-state-layer-hover-color: ${U[e].hover.selectedStateLayerColor}; --m3e-state-layer-focus-color: ${U[e].focus.selectedStateLayerColor}; --m3e-ripple-color: ${U[e].pressed.selectedStateLayerColor}; } :host(${t}:not(:disabled):not([disabled-interactive])) .base { outline-color: ${U[e].outlineColor ?? c("unset")}; } :host(${t}:focus:not(:disabled):not([disabled-interactive])) .base { outline-color: ${U[e].focus.outlineColor ?? c("unset")}; } :host(${t}:hover:not(:disabled):not([disabled-interactive])) .base { outline-color: ${U[e].hover.outlineColor ?? c("unset")}; } :host(${t}:is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive])) .base { outline-color: ${U[e].pressed.outlineColor ?? c("unset")}; } :host(${t}:not(:disabled):not([disabled-interactive])) .label { color: ${U[e].labelTextColor}; } :host(${t}[toggle]:not([selected]):not(:disabled):not([disabled-interactive])) .label { color: ${U[e].unselectedLabelTextColor}; } :host(${t}[toggle][selected]:not(:disabled):not([disabled-interactive])) .label { color: ${U[e].selectedLabelTextColor}; } :host(${t}:focus:not(:disabled):not([disabled-interactive])) .label { color: ${U[e].focus.labelTextColor}; } :host(${t}[toggle]:not([selected]):focus:not(:disabled):not([disabled-interactive])) .label { color: ${U[e].focus.unselectedLabelTextColor}; } :host(${t}[toggle][selected]:focus:not(:disabled):not([disabled-interactive])) .label { color: ${U[e].focus.selectedLabelTextColor}; } :host(${t}:hover:not(:disabled):not([disabled-interactive])) .label { color: ${U[e].hover.labelTextColor}; } :host(${t}[toggle]:not([selected]):hover:not(:disabled):not([disabled-interactive])) .label { color: ${U[e].hover.unselectedLabelTextColor}; } :host(${t}[toggle][selected]:hover:not(:disabled):not([disabled-interactive])) .label { color: ${U[e].hover.selectedLabelTextColor}; } :host(${t}:is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive])) .label { color: ${U[e].pressed.labelTextColor}; } :host( ${t}[toggle]:not([selected]):is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive]) ) .label { color: ${U[e].pressed.unselectedLabelTextColor}; } :host(${t}[toggle][selected]:is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive])) .label { color: ${U[e].pressed.selectedLabelTextColor}; } :host(${t}:not(:disabled):not([disabled-interactive])) .icon { color: ${U[e].iconColor}; } :host(${t}[toggle]:not([selected]):not(:disabled):not([disabled-interactive])) .icon { color: ${U[e].unselectedIconColor}; } :host(${t}[toggle][selected]:not(:disabled):not([disabled-interactive])) .icon { color: ${U[e].selectedIconColor}; } :host(${t}:focus:not(:disabled):not([disabled-interactive])) .icon { color: ${U[e].focus.iconColor}; } :host(${t}[toggle]:not([selected]):focus:not(:disabled):not([disabled-interactive])) .icon { color: ${U[e].focus.unselectedIconColor}; } :host(${t}[toggle][selected]:focus:not(:disabled):not([disabled-interactive])) .icon { color: ${U[e].focus.selectedIconColor}; } :host(${t}:hover:not(:disabled):not([disabled-interactive])) .icon { color: ${U[e].hover.iconColor}; } :host(${t}[toggle]:not([selected]):hover:not(:disabled):not([disabled-interactive])) .icon { color: ${U[e].hover.unselectedIconColor}; } :host(${t}[toggle][selected]:hover:not(:disabled):not([disabled-interactive])) .icon { color: ${U[e].hover.selectedIconColor}; } :host(${t}:is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive])) .icon { color: ${U[e].pressed.iconColor}; } :host( ${t}[toggle]:not([selected]):is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive]) ) .icon { color: ${U[e].pressed.unselectedIconColor}; } :host(${t}[toggle][selected]:is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive])) .icon { color: ${U[e].pressed.selectedIconColor}; } :host(${t}:disabled) .base, :host(${t}[disabled-interactive]) .base { --m3e-elevation-level: ${U[e].disabled.containerElevation ?? c("unset")}; outline-color: ${U[e].disabled.outlineColor ?? c("unset")}; background-color: color-mix( in srgb, ${U[e].disabled.containerColor} ${U[e].disabled.containerOpacity}, transparent ); } :host(${t}:disabled) .label, :host(${t}[disabled-interactive]) .label { color: color-mix( in srgb, ${U[e].disabled.labelTextColor} ${U[e].disabled.labelTextOpacity}, transparent ); } :host(${t}:disabled) .icon, :host(${t}[disabled-interactive]) .icon { color: color-mix( in srgb, ${U[e].disabled.iconColor} ${U[e].disabled.iconOpacity}, transparent ); }`;
}
var hc = [
	mc("text"),
	mc("elevated"),
	mc("outlined"),
	mc("filled"),
	mc("tonal"),
	l`:host(:is(:state(--outlined), :--outlined):not([toggle][selected]):not(:disabled):not([disabled-interactive])) .base { outline-style: solid; }`
], gc, _c, vc, yc, bc, xc, Sc, Cc, wc, Tc, Ec = class extends ji(Fi(Di(Ti(si(ni(E(M(S, "button"), !0))))))) {
	constructor() {
		super(), gc.add(this), this._adjacentPressedTimeout = -1, _c.set(this, (e) => t(this, gc, "m", xc).call(this, e)), this.variant = "text", this.shape = "rounded", this.size = "small", this.toggle = !1, this.selected = !1, new tr(this, { callback: () => this._handleResize() }), new nn(this, { callback: (e) => {
			!this.disabledInteractive && !e && !this.grouped && this._base?.style.removeProperty("--_button-shape");
		} }), new Yn(this, {
			isPressedKey: (e) => e === " ",
			minPressedDuration: 225,
			callback: (e) => {
				!this.disabled && !this.disabledInteractive && (e ? (t(this, gc, "m", Cc).call(this), t(this, gc, "m", wc).call(this, !0)) : t(this, gc, "m", wc).call(this, !1));
			}
		});
	}
	get grouped() {
		return Sr(this, "--grouped");
	}
	render() {
		return b`<div class="base"><m3e-elevation class="elevation" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-elevation><m3e-state-layer class="state-layer" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-state-layer><m3e-focus-ring class="focus-ring" ?disabled="${this.disabled}"></m3e-focus-ring><m3e-ripple class="ripple" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-ripple><div class="touch" aria-hidden="true"></div>${this[Mi]()}<div class="wrapper">${this.toggle ? b`<slot class="icon" name="selected-icon" aria-hidden="true" @slotchange="${t(this, gc, "m", Sc)}"></slot>` : x}<slot class="icon" name="icon" aria-hidden="true"></slot><div class="label">${this.toggle && this.selected ? b`<slot name="selected"><slot></slot></slot>` : b`<slot></slot>`}</div><slot class="icon" name="trailing-icon" aria-hidden="true"></slot></div></div>`;
	}
	connectedCallback() {
		super.connectedCallback(), this.addEventListener("click", t(this, _c, "f")), t(this, gc, "m", vc).call(this), t(this, gc, "m", yc).call(this), t(this, gc, "m", bc).call(this);
	}
	disconnectedCallback() {
		super.disconnectedCallback(), [
			"--pressed",
			"--resting",
			"--grouped",
			"--connected"
		].forEach((e) => D(this, e)), this._base?.style.removeProperty("--_button-shape"), this.style.removeProperty("--_button-width"), this.style.removeProperty("--_adjacent-shrink"), D(this, "--adjacent-pressed"), this.removeEventListener("click", t(this, _c, "f"));
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, gc, "m", vc).call(this), e.has("shape") && t(this, gc, "m", yc).call(this), e.has("size") && t(this, gc, "m", bc).call(this);
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
		if (super.updated(e), (e.has("disabled") && this.disabled || e.has("disabledInteractive") && this.disabledInteractive) && (D(this, "--pressed"), D(this, "--resting")), (e.has("toggle") || e.has("selected")) && (this.ariaPressed = this.toggle ? `${this.selected}` : null, this.toggle)) for (let e of this.querySelectorAll("m3e-icon")) e.toggleAttribute("filled", this.selected);
	}
	_handleResize() {
		if (this.grouped && !Sr(this, "--no-resize") && this !== document.activeElement) {
			let e = this.getBoundingClientRect().width;
			e > 0 ? (this.style.setProperty("--_button-width", `${e}px`), t(this, gc, "m", Cc).call(this, !0)) : this.style.removeProperty("--_button-width");
		}
	}
};
_c = /* @__PURE__ */ new WeakMap(), gc = /* @__PURE__ */ new WeakSet(), vc = function() {
	uc(this.variant) || (this.variant = "text"), wr(this, this.variant, "elevated", "filled", "outlined", "text", "tonal");
}, yc = function() {
	cc(this.shape) || (this.shape = "rounded"), wr(this, this.shape, "rounded", "square");
}, bc = function() {
	lc(this.size) || (this.size = "small"), wr(this, this.size, "extra-large", "extra-small", "large", "medium", "small");
}, xc = function(e) {
	(this.disabled || this.disabledInteractive) && (e.preventDefault(), e.stopImmediatePropagation()), this.toggle && !e.defaultPrevented && this.dispatchEvent(new Event("beforeinput", {
		bubbles: !0,
		cancelable: !0
	})) && (this.selected = !this.selected, this.dispatchEvent(new Event("input", { bubbles: !0 })), this.dispatchEvent(new Event("change", { bubbles: !0 })));
}, Sc = function(e) {
	this._base?.classList.toggle("with-selected-icon", Ft(e.target));
}, Cc = function(e = !1) {
	if (!this._base) return;
	let t = parseFloat(getComputedStyle(this._base).borderRadius);
	if (!isNaN(t) || e) {
		let n = this.clientHeight / 2;
		(n < t || e) && this._base?.style.setProperty("--_button-shape", `${n}px`);
	}
}, wc = function(e) {
	let n = this.getBoundingClientRect().width, r = this.closest("m3e-button-group");
	if (r && r.variant === "standard") {
		let i = [...r.querySelectorAll("m3e-button,m3e-icon-button")];
		for (let e of i) clearTimeout(e._adjacentPressedTimeout), e._adjacentPressedTimeout = -1;
		let a = i.indexOf(this);
		if (e) {
			let e = n * parseFloat(getComputedStyle(this).getPropertyValue("--m3e-standard-button-group-width-multiplier") || "0.15");
			a > 0 && a < i.length - 1 && (e /= 2);
			for (let t = 0; t < i.length; t++) t == a - 1 || t == a + 1 ? (Cr(i[t], "--no-resize"), i[t].style.setProperty("--_adjacent-shrink", `${e}px`), Cr(i[t], "--adjacent-pressed")) : t == a ? (Cr(i[t], "--no-resize"), i[t].style.removeProperty("--_adjacent-shrink"), D(i[t], "--adjacent-pressed")) : (D(i[t], "--no-resize"), i[t].style.removeProperty("--_adjacent-shrink"), D(i[t], "--adjacent-pressed"));
		} else {
			for (let e = 0; e < i.length; e++) (e == a - 1 || e == a + 1) && i[e].style.setProperty("--_adjacent-shrink", "0px");
			It() ? t(this, gc, "m", Tc).call(this, i) : this.addEventListener("transitionend", (e) => {
				e.propertyName === "width" && (this._adjacentPressedTimeout = setTimeout(() => {
					this._adjacentPressedTimeout > -1 && t(this, gc, "m", Tc).call(this, i);
				}, 600));
			}, { once: !0 });
		}
	}
	O(this, "--pressed", e), O(this, "--resting", !e);
}, Tc = function(e) {
	for (let t of e) D(t, "--adjacent-pressed"), D(t, "--no-resize"), t.style.removeProperty("--_adjacent-shrink");
}, Ec.styles = [
	fc,
	hc,
	pc
], e([w(".base")], Ec.prototype, "_base", void 0), e([w(".elevation")], Ec.prototype, "_elevation", void 0), e([w(".focus-ring")], Ec.prototype, "_focusRing", void 0), e([w(".state-layer")], Ec.prototype, "_stateLayer", void 0), e([w(".ripple")], Ec.prototype, "_ripple", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], Ec.prototype, "variant", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], Ec.prototype, "shape", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], Ec.prototype, "size", void 0), e([C({
	type: Boolean,
	reflect: !0
})], Ec.prototype, "toggle", void 0), e([C({
	type: Boolean,
	reflect: !0
})], Ec.prototype, "selected", void 0), e([nr(40)], Ec.prototype, "_handleResize", null), Ec = e([T("m3e-button")], Ec);
//#endregion
//#region node_modules/@m3e/web/dist/button-group.js
function Dc(e) {
	return e === "standard" || e === "connected";
}
function Oc(e) {
	return e === "extra-small" || e === "small" || e === "medium" || e === "large" || e === "extra-large";
}
var kc, Ac, jc, Mc, Nc, Pc, Fc, Ic, Lc = class extends M(E(S), "group") {
	constructor() {
		super(...arguments), kc.add(this), Ac.set(this, new Yn(this, {
			target: null,
			capture: !0,
			minPressedDuration: 225,
			isPressedKey: (e) => e === " ",
			callback: (e) => t(this, kc, "m", Fc).call(this, e)
		})), this.variant = "standard", this.size = "small", this.multi = !1;
	}
	connectedCallback() {
		super.connectedCallback(), t(this, kc, "m", jc).call(this), t(this, kc, "m", Mc).call(this), this.hasAttribute("disable-role") && (this.role = null);
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this._base?.style.removeProperty("--_button-group-width"), this._base?.classList.remove("pressed");
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, kc, "m", jc).call(this), e.has("size") && t(this, kc, "m", Mc).call(this);
	}
	update(e) {
		super.update(e), (e.has("multi") || e.has("variant")) && t(this, kc, "m", Nc).call(this), e.has("variant") && this._base?.style.removeProperty("--_button-group-width");
	}
	render() {
		return b`<div class="base"><slot @slotchange="${t(this, kc, "m", Nc)}" @change="${t(this, kc, "m", Pc)}"></slot></div>`;
	}
};
Ac = /* @__PURE__ */ new WeakMap(), kc = /* @__PURE__ */ new WeakSet(), jc = function() {
	Dc(this.variant) || (this.variant = "standard"), wr(this, this.variant, "connected", "standard");
}, Mc = function() {
	Oc(this.size) || (this.size = "small"), wr(this, this.size, "extra-large", "extra-small", "large", "medium", "small");
}, Nc = async function() {
	let e = this.buttons;
	for (let e of t(this, Ac, "f").targets) t(this, Ac, "f").unobserve(e);
	for (let e of this.buttons) await Vt(e), await Bt(e);
	let n = [...e].some((e) => e.toggle);
	this.hasAttribute("disable-role") || (this.role = n && !this.multi ? "radiogroup" : "group");
	let r = this.role === "radiogroup" ? "radio" : "button";
	e.forEach((n, i) => {
		if (t(this, Ac, "f").observe(n), O(n, "--connected", this.variant === "connected"), Cr(n, "--grouped"), O(n, "--first", i == 0), O(n, "--last", i == e.length - 1), !this.hasAttribute("disable-role") && n.role !== r && n.toggle) {
			let e = n.toggle ? n.selected ? "true" : "false" : null;
			n.role = r, n.role === "button" ? (n.ariaPressed = e, n.ariaChecked = null) : (n.ariaChecked = e, n.ariaPressed = null);
		}
	});
}, Pc = function(e) {
	if (!this.multi && e.target instanceof HTMLElement && (e.target.tagName === "M3E-BUTTON" || e.target.tagName === "M3E-ICON-BUTTON")) {
		if (!kr(e.target) || !e.target.selected) return;
		for (let t of this.buttons) t !== e.target && t.selected && (t.selected = !1);
	}
}, Fc = function(e) {
	let n = this._base;
	if (n) {
		if (!e || this.variant === "connected") {
			let e = this.buttons.find((e) => e === document.activeElement);
			!It() && e ? e.addEventListener("transitionend", () => queueMicrotask(() => {
				Sr(e, "--pressed") || t(this, kc, "m", Ic).call(this, n);
			}), { once: !0 }) : t(this, kc, "m", Ic).call(this, n);
		} else n.classList.add("pressed"), n.style.setProperty("--_button-group-width", `${n.getBoundingClientRect().width}px`);
	}
}, Ic = function(e) {
	e.style.removeProperty("--_button-group-width"), e.classList.remove("pressed");
}, Lc.styles = l`:host { display: flex; vertical-align: middle; flex-wrap: nowrap; align-items: center; } :host([hidden]) { display: none; } .base { display: flex; vertical-align: middle; flex-wrap: nowrap; align-items: center; } :host(:is(:state(--standard), :--standard)) { justify-content: center; } :host(:is(:state(--connected), :--connected)) .base { flex: 1 1 auto; } :host(:is(:state(--standard), :--standard)) .base { width: fit-content; flex: none; } :host(:is(:state(--standard), :--standard)) .base.pressed { justify-content: space-between; width: var(--_button-group-width); } :host(:is(:state(--standard), :--standard):is(:state(--extra-small), :--extra-small)) .base { column-gap: var(--m3e-standard-button-group-extra-small-spacing, ${j.measurement.space225}); } :host(:is(:state(--standard), :--standard):is(:state(--small), :--small)) .base { column-gap: var(--m3e-standard-button-group-small-spacing, ${j.measurement.space150}); } :host(:is(:state(--standard), :--standard):is(:state(--medium), :--medium)).base { column-gap: var(--m3e-standard-button-group-medium-spacing, ${j.measurement.space100}); } :host(:is(:state(--standard), :--standard):is(:state(--large), :--large)) .base { column-gap: var(--m3e-standard-button-group-large-spacing, ${j.measurement.space100}); } :host(:is(:state(--standard), :--standard):is(:state(--extra-large), :--extra-large)) .base { column-gap: var(--m3e-standard-button-group-extra-large-spacing, ${j.measurement.space100}); } :host(:is(:state(--connected), :--connected)) .base { column-gap: var(--m3e-connected-button-group-spacing, ${j.measurement.space25}); } :host(:is(:state(--connected), :--connected):is(:state(--extra-small), :--extra-small)) ::slotted(:is(:state(--first), :--first):is(:state(--extra-small), :--extra-small)), :host(:is(:state(--connected), :--connected):is(:state(--extra-small), :--extra-small)) ::slotted( :not(:is(:state(--first), :--first)):not(:is(:state(--last), :--last)):is(:state(--extra-small), :--extra-small) ) { --_button-rounded-end-shape: var( --m3e-connected-button-group-extra-small-inner-shape, ${j.shape.corner.small} ); --_button-square-end-shape: var( --m3e-connected-button-group-extra-small-inner-shape, ${j.shape.corner.small} ); --_button-square-end-pressed-shape: var( --m3e-connected-button-group-extra-small-inner-pressed-shape, ${j.shape.corner.extraSmall} ); } :host(:is(:state(--connected), :--connected):is(:state(--extra-small), :--extra-small)) ::slotted(:is(:state(--last), :--last):is(:state(--extra-small), :--extra-small)), :host(:is(:state(--connected), :--connected):is(:state(--extra-small), :--extra-small)) ::slotted( :not(:is(:state(--first), :--first)):not(:is(:state(--last), :--last)):is(:state(--extra-small), :--extra-small) ) { --_button-rounded-start-shape: var( --m3e-connected-button-group-extra-small-inner-shape, ${j.shape.corner.small} ); --_button-square-start-shape: var( --m3e-connected-button-group-extra-small-inner-shape, ${j.shape.corner.small} ); --_button-square-start-pressed-shape: var( --m3e-connected-button-group-extra-small-inner-pressed-shape, ${j.shape.corner.extraSmall} ); } :host(:is(:state(--connected), :--connected):is(:state(--small), :--small)) ::slotted(:is(:state(--first), :--first):is(:state(--small), :--small)), :host(:is(:state(--connected), :--connected):is(:state(--small), :--small)) ::slotted(:not(:is(:state(--first), :--first)):not(:is(:state(--last), :--last)):is(:state(--small), :--small)) { --_button-rounded-end-shape: var( --m3e-connected-button-group-small-inner-shape, ${j.shape.corner.small} ); --_button-square-end-shape: var( --m3e-connected-button-group-small-inner-shape, ${j.shape.corner.small} ); --_button-end-shape-pressed-morph: var( --m3e-connected-button-group-small-inner-pressed-shape, ${j.shape.corner.extraSmall} ); } :host(:is(:state(--connected), :--connected):is(:state(--small), :--small)) ::slotted(:is(:state(--last), :--last):is(:state(--small), :--small)), :host(:is(:state(--connected), :--connected):is(:state(--small), :--small)) ::slotted(:not(:is(:state(--first), :--first)):not(:is(:state(--last), :--last)):is(:state(--small), :--small)) { --_button-rounded-start-shape: var( --m3e-connected-button-group-small-inner-shape, ${j.shape.corner.small} ); --_button-square-start-shape: var( --m3e-connected-button-group-small-inner-shape, ${j.shape.corner.small} ); --_button-start-shape-pressed-morph: var( --m3e-connected-button-group-small-inner-pressed-shape, ${j.shape.corner.extraSmall} ); } :host(:is(:state(--connected), :--connected):is(:state(--medium), :--medium)) ::slotted(:is(:state(--first), :--first):is(:state(--medium), :--medium)), :host(:is(:state(--connected), :--connected):is(:state(--medium), :--medium)) ::slotted( :not(:is(:state(--first), :--first)):not(:is(:state(--last), :--last)):is(:state(--medium), :--medium) ) { --_button-rounded-end-shape: var( --m3e-connected-button-group-medium-inner-shape, ${j.shape.corner.small} ); --_button-square-end-shape: var( --m3e-connected-button-group-medium-inner-shape, ${j.shape.corner.small} ); --_button-square-end-pressed-shape: var( --m3e-connected-button-group-medium-inner-pressed-shape, ${j.shape.corner.extraSmall} ); } :host(:is(:state(--connected), :--connected):is(:state(--medium), :--medium)) ::slotted(:is(:state(--last), :--last):is(:state(--medium), :--medium)), :host(:is(:state(--connected), :--connected):is(:state(--medium), :--medium)) ::slotted( :not(:is(:state(--first), :--first)):not(:is(:state(--last), :--last)):is(:state(--medium), :--medium) ) { --_button-rounded-start-shape: var( --m3e-connected-button-group-medium-inner-shape, ${j.shape.corner.small} ); --_button-square-start-shape: var( --m3e-connected-button-group-medium-inner-shape, ${j.shape.corner.small} ); --_button-square-start-pressed-shape: var( --m3e-connected-button-group-medium-inner-pressed-shape, ${j.shape.corner.extraSmall} ); } :host(:is(:state(--connected), :--connected):is(:state(--large), :--large)) ::slotted(:is(:state(--first), :--first):is(:state(--large), :--large)), :host(:is(:state(--connected), :--connected):is(:state(--large), :--large)) ::slotted(:not(:is(:state(--first), :--first)):not(:is(:state(--last), :--last)):is(:state(--large), :--large)) { --_button-rounded-end-shape: var( --m3e-connected-button-group-large-inner-shape, ${j.shape.corner.large} ); --_button-square-end-shape: var( --m3e-connected-button-group-large-inner-shape, ${j.shape.corner.large} ); --_button-square-end-pressed-shape: var( --m3e-connected-button-group-large-inner-pressed-shape, ${j.shape.corner.medium} ); } :host(:is(:state(--connected), :--connected):is(:state(--large), :--large)) ::slotted(:is(:state(--last), :--last):is(:state(--large), :--large)), :host(:is(:state(--connected), :--connected):is(:state(--large), :--large)) ::slotted(:not(:is(:state(--first), :--first)):not(:is(:state(--last), :--last)):is(:state(--large), :--large)) { --_button-rounded-start-shape: var( --m3e-connected-button-group-large-inner-shape, ${j.shape.corner.large} ); --_button-square-start-shape: var( --m3e-connected-button-group-large-inner-shape, ${j.shape.corner.large} ); --_button-square-start-pressed-shape: var( --m3e-connected-button-group-large-inner-pressed-shape, ${j.shape.corner.medium} ); } :host(:is(:state(--connected), :--connected):is(:state(--extra-large), :--extra-large)) ::slotted(:is(:state(--first), :--first):is(:state(--extra-large), :--extra-large)), :host(:is(:state(--connected), :--connected):is(:state(--extra-large), :--extra-large)) ::slotted( :not(:is(:state(--first), :--first)):not(:is(:state(--last), :--last)):is(:state(--extra-large), :--extra-large) ) { --_button-rounded-end-shape: var( --m3e-connected-button-group-extra-large-inner-shape, ${j.shape.corner.largeIncreased} ); --_button-square-end-shape: var( --m3e-connected-button-group-extra-large-inner-shape, ${j.shape.corner.largeIncreased} ); --_button-square-end-pressed-shape: var( --m3e-connected-button-group-extra-large-inner-pressed-shape, ${j.shape.corner.large} ); } :host(:is(:state(--connected), :--connected):is(:state(--extra-large), :--extra-large)) ::slotted(:is(:state(--last), :--last):is(:state(--extra-large), :--extra-large)), :host(:is(:state(--connected), :--connected):is(:state(--extra-large), :--extra-large)) ::slotted( :not(:is(:state(--first), :--first)):not(:is(:state(--last), :--last)):is(:state(--extra-large), :--extra-large) ) { --_button-rounded-start-shape: var( --m3e-connected-button-group-extra-large-inner-shape, ${j.shape.corner.largeIncreased} ); --_button-square-start-shape: var( --m3e-connected-button-group-extra-large-inner-shape, ${j.shape.corner.largeIncreased} ); --_button-square-start-pressed-shape: var( --m3e-connected-button-group-extra-large-inner-pressed-shape, ${j.shape.corner.large} ); }`, e([w(".base")], Lc.prototype, "_base", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], Lc.prototype, "variant", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], Lc.prototype, "size", void 0), e([C({ type: Boolean })], Lc.prototype, "multi", void 0), e([Qe({
	slot: "",
	selector: "m3e-button,m3e-icon-button",
	flatten: !0
})], Lc.prototype, "buttons", void 0), Lc = e([T("m3e-button-group")], Lc);
//#endregion
//#region node_modules/@m3e/web/dist/icon-button.js
function Rc(e) {
	return e === "extra-small" || e === "small" || e === "medium" || e === "large" || e === "extra-large";
}
function zc(e) {
	return e === "rounded" || e === "square";
}
function Bc(e) {
	return e === "filled" || e === "tonal" || e === "outlined" || e === "elevated" || e === "standard";
}
function Vc(e) {
	return e === "default" || e === "narrow" || e === "wide";
}
var W = {
	"extra-small": {
		containerHeight: c(`calc(var(--m3e-icon-button-extra-small-container-height, var(--m3e-icon-button-container-height, 32px)) + ${j.density.calc(-3)})`),
		outlineThickness: c("var(--m3e-icon-button-extra-small-outline-thickness, var(--m3e-icon-button-outline-thickness, 1px))"),
		iconSize: c("var(--m3e-icon-button-extra-small-icon-size, var(--m3e-icon-button-icon-size, 20px))"),
		shapeRound: c(`var(--m3e-icon-button-extra-small-shape-round, var(--m3e-icon-button-shape-round, ${j.shape.corner.full}))`),
		shapeSquare: c(`var(--m3e-icon-button-extra-small-shape-square, var(--m3e-icon-button-shape-square, ${j.shape.corner.medium}))`),
		selectedShapeRound: c(`var(--m3e-icon-button-extra-small-selected-shape-round, var(--m3e-icon-button-selected-shape-round, ${j.shape.corner.medium}))`),
		selectedShapeSquare: c(`var(--m3e-icon-button-extra-small-selected-shape-square, var(--m3e-icon-button-selected-shape-square, ${j.shape.corner.full}))`),
		shapePressedMorph: c(`var(--m3e-icon-button-extra-small-shape-pressed-morph, var(--m3e-icon-button-shape-pressed-morph, ${j.shape.corner.small}))`),
		narrowLeadingSpace: c(`calc(var(--m3e-icon-button-extra-small-narrow-leading-space, var(--m3e-icon-button-narrow-leading-space, ${j.measurement.space50})) + ${j.density.calcHalf(-3)})`),
		narrowTrailingSpace: c(`calc(var(--m3e-icon-button-extra-small-narrow-trailing-space, var(--m3e-icon-button-narrow-trailing-space, ${j.measurement.space50})) + ${j.density.calcHalf(-3)})`),
		defaultLeadingSpace: c(`calc(var(--m3e-icon-button-extra-small-default-leading-space, var(--m3e-icon-button-default-leading-space, ${j.measurement.space75})) + ${j.density.calcHalf(-3)})`),
		defaultTrailingSpace: c(`calc(var(--m3e-icon-button-extra-small-default-trailing-space, var(--m3e-icon-button-default-trailing-space, ${j.measurement.space75})) + ${j.density.calcHalf(-3)})`),
		wideLeadingSpace: c(`calc(var(--m3e-icon-button-extra-small-wide-leading-space, var(--m3e-icon-button-wide-leading-space, ${j.measurement.space125})) + ${j.density.calcHalf(-3)})`),
		wideTrailingSpace: c(`calc(var(--m3e-icon-button-extra-small-wide-trailing-space, var(--m3e-icon-button-wide-trailing-space, ${j.measurement.space125})) + ${j.density.calcHalf(-3)})`)
	},
	small: {
		containerHeight: c(`calc(var(--m3e-icon-button-small-container-height, var(--m3e-icon-button-container-height, 40px)) + ${j.density.calc(-3)})`),
		outlineThickness: c("var(--m3e-icon-button-small-outline-thickness, var(--m3e-icon-button-outline-thickness, 1px))"),
		iconSize: c("var(--m3e-icon-button-small-icon-size, var(--m3e-icon-button-icon-size, 24px))"),
		shapeRound: c(`var(--m3e-icon-button-small-shape-round, var(--m3e-icon-button-shape-round, ${j.shape.corner.full}))`),
		shapeSquare: c(`var(--m3e-icon-button-small-shape-square, var(--m3e-icon-button-shape-square, ${j.shape.corner.medium}))`),
		selectedShapeRound: c(`var(--m3e-icon-button-small-selected-shape-round, var(--m3e-icon-button-selected-shape-round, ${j.shape.corner.medium}))`),
		selectedShapeSquare: c(`var(--m3e-icon-button-small-selected-shape-square, var(--m3e-icon-button-selected-shape-square, ${j.shape.corner.full}))`),
		shapePressedMorph: c(`var(--m3e-icon-button-small-shape-pressed-morph, var(--m3e-icon-button-shape-pressed-morph, ${j.shape.corner.small}))`),
		narrowLeadingSpace: c(`calc(var(--m3e-icon-button-small-narrow-leading-space, var(--m3e-icon-button-narrow-leading-space, ${j.measurement.space50})) + ${j.density.calcHalf(-3)})`),
		narrowTrailingSpace: c(`calc(var(--m3e-icon-button-small-narrow-trailing-space, var(--m3e-icon-button-narrow-trailing-space, ${j.measurement.space50})) + ${j.density.calcHalf(-3)})`),
		defaultLeadingSpace: c(`calc(var(--m3e-icon-button-small-default-leading-space, var(--m3e-icon-button-default-leading-space, ${j.measurement.space100})) + ${j.density.calcHalf(-3)})`),
		defaultTrailingSpace: c(`calc(var(--m3e-icon-button-small-default-trailing-space, var(--m3e-icon-button-default-trailing-space, ${j.measurement.space100})) + ${j.density.calcHalf(-3)})`),
		wideLeadingSpace: c(`calc(var(--m3e-icon-button-small-wide-leading-space, var(--m3e-icon-button-wide-leading-space, ${j.measurement.space175})) + ${j.density.calcHalf(-3)})`),
		wideTrailingSpace: c(`calc(var(--m3e-icon-button-small-wide-trailing-space, var(--m3e-icon-button-wide-trailing-space, ${j.measurement.space175})) + ${j.density.calcHalf(-3)})`)
	},
	medium: {
		containerHeight: c(`calc(var(--m3e-icon-button-medium-container-height, var(--m3e-icon-button-container-height, 56px)) + ${j.density.calc(-3)})`),
		outlineThickness: c("var(--m3e-icon-button-medium-outline-thickness, var(--m3e-icon-button-outline-thickness, 1px))"),
		iconSize: c("var(--m3e-icon-button-medium-icon-size, var(--m3e-icon-button-icon-size, 24px))"),
		shapeRound: c(`var(--m3e-icon-button-medium-shape-round, var(--m3e-icon-button-shape-round, ${j.shape.corner.full}))`),
		shapeSquare: c(`var(--m3e-icon-button-medium-shape-square, var(--m3e-icon-button-shape-square, ${j.shape.corner.large}))`),
		selectedShapeRound: c(`var(--m3e-icon-button-medium-selected-shape-round, var(--m3e-icon-button-selected-shape-round, ${j.shape.corner.large}))`),
		selectedShapeSquare: c(`var(--m3e-icon-button-medium-selected-shape-square, var(--m3e-icon-button-selected-shape-square, ${j.shape.corner.full}))`),
		shapePressedMorph: c(`var(--m3e-icon-button-medium-shape-pressed-morph, var(--m3e-icon-button-shape-pressed-morph, ${j.shape.corner.medium}))`),
		narrowLeadingSpace: c(`calc(var(--m3e-icon-button-medium-narrow-leading-space, var(--m3e-icon-button-narrow-leading-space, ${j.measurement.space150})) + ${j.density.calcHalf(-3)})`),
		narrowTrailingSpace: c(`calc(var(--m3e-icon-button-medium-narrow-trailing-space, var(--m3e-icon-button-narrow-trailing-space, ${j.measurement.space150})) + ${j.density.calcHalf(-3)})`),
		defaultLeadingSpace: c(`calc(var(--m3e-icon-button-medium-default-leading-space, var(--m3e-icon-button-default-leading-space, ${j.measurement.space200})) + ${j.density.calcHalf(-3)})`),
		defaultTrailingSpace: c(`calc(var(--m3e-icon-button-medium-default-trailing-space, var(--m3e-icon-button-default-trailing-space, ${j.measurement.space200})) + ${j.density.calcHalf(-3)})`),
		wideLeadingSpace: c(`calc(var(--m3e-icon-button-medium-wide-leading-space, var(--m3e-icon-button-wide-leading-space, ${j.measurement.space300})) + ${j.density.calcHalf(-3)})`),
		wideTrailingSpace: c(`calc(var(--m3e-icon-button-medium-wide-trailing-space, var(--m3e-icon-button-wide-trailing-space, ${j.measurement.space300})) + ${j.density.calcHalf(-3)})`)
	},
	large: {
		containerHeight: c(`calc(var(--m3e-icon-button-large-container-height, var(--m3e-icon-button-container-height, 96px)) + ${j.density.calc(-3)})`),
		outlineThickness: c("var(--m3e-icon-button-large-outline-thickness, var(--m3e-icon-button-outline-thickness, 2px))"),
		iconSize: c("var(--m3e-icon-button-large-icon-size, var(--m3e-icon-button-icon-size, 32px))"),
		shapeRound: c(`var(--m3e-icon-button-large-shape-round, var(--m3e-icon-button-shape-round, ${j.shape.corner.full}))`),
		shapeSquare: c(`var(--m3e-icon-button-large-shape-square, var(--m3e-icon-button-shape-square, ${j.shape.corner.extraLarge}))`),
		selectedShapeRound: c(`var(--m3e-icon-button-large-selected-shape-round, var(--m3e-icon-button-selected-shape-round, ${j.shape.corner.extraLarge}))`),
		selectedShapeSquare: c(`var(--m3e-icon-button-large-selected-shape-square, var(--m3e-icon-button-selected-shape-square, ${j.shape.corner.full}))`),
		shapePressedMorph: c(`var(--m3e-icon-button-large-shape-pressed-morph, var(--m3e-icon-button-shape-pressed-morph, ${j.shape.corner.large}))`),
		narrowLeadingSpace: c(`calc(var(--m3e-icon-button-large-narrow-leading-space, var(--m3e-icon-button-narrow-leading-space, ${j.measurement.space200})) + ${j.density.calcHalf(-3)})`),
		narrowTrailingSpace: c(`calc(var(--m3e-icon-button-large-narrow-trailing-space, var(--m3e-icon-button-narrow-trailing-space, ${j.measurement.space200})) + ${j.density.calcHalf(-3)})`),
		defaultLeadingSpace: c(`calc(var(--m3e-icon-button-large-default-leading-space, var(--m3e-icon-button-default-leading-space, ${j.measurement.space400})) + ${j.density.calcHalf(-3)})`),
		defaultTrailingSpace: c(`calc(var(--m3e-icon-button-large-default-trailing-space, var(--m3e-icon-button-default-trailing-space, ${j.measurement.space400})) + ${j.density.calcHalf(-3)})`),
		wideLeadingSpace: c(`calc(var(--m3e-icon-button-large-wide-leading-space, var(--m3e-icon-button-wide-leading-space, ${j.measurement.space600})) + ${j.density.calcHalf(-3)})`),
		wideTrailingSpace: c(`calc(var(--m3e-icon-button-large-wide-trailing-space, var(--m3e-icon-button-wide-trailing-space, ${j.measurement.space600})) + ${j.density.calcHalf(-3)})`)
	},
	"extra-large": {
		containerHeight: c(`calc(var(--m3e-icon-button-extra-large-container-height, var(--m3e-icon-button-container-height, 136px)) + ${j.density.calc(-3)})`),
		outlineThickness: c("var(--m3e-icon-button-extra-large-outline-thickness, var(--m3e-icon-button-outline-thickness, 3px))"),
		iconSize: c("var(--m3e-icon-button-extra-large-icon-size, var(--m3e-icon-button-icon-size, 40px))"),
		shapeRound: c(`var(--m3e-icon-button-extra-large-shape-round, var(--m3e-icon-button-shape-round, ${j.shape.corner.full}))`),
		shapeSquare: c(`var(--m3e-icon-button-extra-large-shape-square, var(--m3e-icon-button-shape-square, ${j.shape.corner.extraLarge}))`),
		selectedShapeRound: c(`var(--m3e-icon-button-extra-large-selected-shape-round, var(--m3e-icon-button-selected-shape-round, ${j.shape.corner.extraLarge}))`),
		selectedShapeSquare: c(`var(--m3e-icon-button-extra-large-selected-shape-square, var(--m3e-icon-button-selected-shape-square, ${j.shape.corner.full}))`),
		shapePressedMorph: c(`var(--m3e-icon-button-extra-large-shape-pressed-morph, var(--m3e-icon-button-shape-pressed-morph, ${j.shape.corner.large}))`),
		narrowLeadingSpace: c(`calc(var(--m3e-icon-button-extra-large-narrow-leading-space, var(--m3e-icon-button-narrow-leading-space, ${j.measurement.space400})) + ${j.density.calcHalf(-3)})`),
		narrowTrailingSpace: c(`calc(var(--m3e-icon-button-extra-large-narrow-trailing-space, var(--m3e-icon-button-narrow-trailing-space, ${j.measurement.space400})) + ${j.density.calcHalf(-3)})`),
		defaultLeadingSpace: c(`calc(var(--m3e-icon-button-extra-large-default-leading-space, var(--m3e-icon-button-default-leading-space, ${j.measurement.space600})) + ${j.density.calcHalf(-3)})`),
		defaultTrailingSpace: c(`calc(var(--m3e-icon-button-extra-large-default-trailing-space, var(--m3e-icon-button-default-trailing-space, ${j.measurement.space600})) + ${j.density.calcHalf(-3)})`),
		wideLeadingSpace: c(`calc(var(--m3e-icon-button-extra-large-wide-leading-space, var(--m3e-icon-button-wide-leading-space, ${j.measurement.space900})) + ${j.density.calcHalf(-3)})`),
		wideTrailingSpace: c(`calc(var(--m3e-icon-button-extra-large-wide-trailing-space, var(--m3e-icon-button-wide-trailing-space, ${j.measurement.space900})) + ${j.density.calcHalf(-3)})`)
	}
};
function Hc(e) {
	let t = c(`:is(:state(--${e}), :--${e})`);
	return l`:host(${t}) .base { height: ${W[e].containerHeight}; } :host(${t}:is(:state(--default), :--default)) .wrapper { padding-inline-start: calc( ${W[e].defaultLeadingSpace} - calc(var(--_adjacent-shrink, 0px) / 2) ); padding-inline-end: calc( ${W[e].defaultTrailingSpace} - calc(var(--_adjacent-shrink, 0px) / 2) ); } :host(${t}:is(:state(--narrow), :--narrow)) .wrapper { padding-inline-start: calc( ${W[e].narrowLeadingSpace} - calc(var(--_adjacent-shrink, 0px) / 2) ); padding-inline-end: calc( ${W[e].narrowTrailingSpace} - calc(var(--_adjacent-shrink, 0px) / 2) ); } :host(${t}:is(:state(--wide), :--wide)) .wrapper { padding-inline-start: calc( ${W[e].wideLeadingSpace} - calc(var(--_adjacent-shrink, 0px) / 2) ); padding-inline-end: calc(${W[e].wideTrailingSpace} - calc(var(--_adjacent-shrink, 0px) / 2)); } :host(${t}) .icon { font-size: ${W[e].iconSize}; } :host(${t}) .base { outline-offset: calc(0px - ${W[e].outlineThickness}); outline-width: ${W[e].outlineThickness}; } :host( :not(:is(:state(--connected), :--connected))${t}:is(:state(--rounded), :--rounded):not( :is(:state(--pressed), :--pressed) ) ) .base { border-radius: var(--_button-shape, ${W[e].shapeRound}); } :host( :is(:state(--connected), :--connected)${t}:is(:state(--rounded), :--rounded)[toggle][selected]:not( :is(:state(--pressed), :--pressed) ) ) .base { border-radius: var(--_button-shape, ${W[e].shapeRound}); } :host(:not(:is(:state(--connected), :--connected))${t}:is(:state(--square), :--square)) .base { border-radius: ${W[e].shapeSquare}; } :host( :not(:is(:state(--connected), :--connected))${t}:is(:state(--rounded), :--rounded)[toggle][selected]:not( :is(:state(--pressed), :--pressed) ) ) .base { border-radius: ${W[e].selectedShapeRound}; } :host( :not(:is(:state(--connected), :--connected))${t}:is(:state(--square), :--square)[toggle][selected]:not( :is(:state(--pressed), :--pressed) ) ) .base { border-radius: var(--_button-shape, ${W[e].selectedShapeSquare}); } :host(:not(:is(:state(--connected), :--connected))${t}:is(:state(--pressed), :--pressed)) .base { border-radius: ${W[e].shapePressedMorph}; } :host(:is(:state(--connected), :--connected)${t}:is(:state(--rounded), :--rounded):not([toggle][selected])) .base { border-start-start-radius: var( --_button-rounded-start-shape, var(--_button-shape, ${W[e].shapeRound}) ); border-end-start-radius: var( --_button-rounded-start-shape, var(--_button-shape, ${W[e].shapeRound}) ); border-start-end-radius: var( --_button-rounded-end-shape, var(--_button-shape, ${W[e].shapeRound}) ); border-end-end-radius: var( --_button-rounded-end-shape, var(--_button-shape, ${W[e].shapeRound}) ); } :host(:is(:state(--connected), :--connected)${t}:is(:state(--square), :--square)) .base { border-start-start-radius: var(--_button-square-start-shape, ${W[e].shapeSquare}); border-end-start-radius: var(--_button-square-start-shape, ${W[e].shapeSquare}); border-start-end-radius: var(--_button-square-end-shape, ${W[e].shapeSquare}); border-end-end-radius: var(--_button-square-end-shape, ${W[e].shapeSquare}); } :host( :is(:state(--connected), :--connected)${t}:is(:state(--square), :--square)[toggle][selected]:not( :is(:state(--pressed), :--pressed) ) ) .base { border-radius: var(--_button-shape, ${W[e].selectedShapeSquare}); } :host(:is(:state(--connected), :--connected)${t}:is(:state(--pressed), :--pressed)) .base { border-start-start-radius: var( --_button-start-shape-pressed-morph, ${W[e].shapePressedMorph} ); border-end-start-radius: var(--_button-start-shape-pressed-morph, ${W[e].shapePressedMorph}); border-start-end-radius: var(--_button-end-shape-pressed-morph, ${W[e].shapePressedMorph}); border-end-end-radius: var(--_button-end-shape-pressed-morph, ${W[e].shapePressedMorph}); }`;
}
var Uc = [
	Hc("extra-small"),
	Hc("small"),
	Hc("medium"),
	Hc("large"),
	Hc("extra-large")
], Wc = l`:host { display: inline-block; outline: none; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } :host([hidden]) { display: none; } .layout, .base { box-sizing: border-box; vertical-align: middle; display: inline-flex; align-items: center; justify-content: center; } .layout { width: var(--_icon-button-size, 100%); min-width: var(--_icon-button-min-size); min-height: var(--_icon-button-min-size); height: var(--_icon-button-size, 100%); } .base { position: relative; width: var(--_icon-button-size, 100%); transition: ${c(`background-color ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; } .touch { position: absolute; aspect-ratio: 1 / 1; height: 48px; left: auto; right: auto; } :host(:is(:state(--pressed), :--pressed)) .base, :host(:is(:state(--resting), :--resting)) .base { transition: ${c(`background-color ${j.motion.duration.short4} ${j.motion.easing.standard},
          border-radius ${j.motion.spring.fastEffects}`)}; } .wrapper { width: 100%; overflow: hidden; display: inline-flex; align-items: center; justify-content: center; transition: ${c(`padding-inline ${j.motion.spring.fastEffects}`)}; } .icon { transition: ${c(`color ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; --m3e-icon-size: 1em; } :host(:not(:disabled):not([disabled-interactive])) { cursor: pointer; } :host([disabled-interactive]) { cursor: not-allowed; } ::slotted(*) { font-size: inherit !important; flex: none; transform: var(--_icon-button-icon-transform); transform-origin: center center; transition: ${c(`transform var(--_icon-button-icon-transform-transition, ${j.motion.spring.fastEffects})`)}; } ::slotted(svg) { width: 1em; height: 1em; } :host([toggle]:not([selected])) .base.with-selected-icon slot[name="selected"], :host([toggle][selected]) .base.with-selected-icon slot:not([name]) { display: none; } a { all: unset; display: block; position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px; z-index: 1; } :host(:is(:state(--grouped), :--grouped):is(:state(--connected), :--connected)) { flex: 1 1 auto; } :host(:is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected))) { transition: ${c(`width ${j.motion.spring.fastEffects}`)}; } :host(:is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected))) .wrapper { transition: ${c(`padding-inline ${j.motion.spring.fastEffects}`)}; } :host(:is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected))) { flex-shrink: 0; flex-grow: 0; } :host( :is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected)):not( :is(:state(--pressed), :--pressed, :state(--adjacent-pressed), :--adjacent-pressed) ) ) { width: var(--_button-width); } :host( :is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected)):not( :is(:state(--pressed), :--pressed) ):is(:state(--adjacent-pressed), :--adjacent-pressed) ) { width: calc(var(--_button-width) - var(--_adjacent-shrink, 0px)); } :host( :is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected)):is( :state(--pressed), :--pressed ):not([disabled-interactive]):not(:disabled) ) { width: calc( var(--_button-width) + calc(var(--_button-width) * var(--m3e-standard-button-group-width-multiplier, 0.15)) ); } @media (forced-colors: active) { .base, .icon { transition: none; } :host(:is(:state(--pressed), :--pressed)) .base, :host(:is(:state(--resting), :--resting)) .base { transition: border-radius ${j.motion.spring.fastEffects}; } :host(:not(:disabled):not([disabled-interactive]):not([toggle])) .base { background-color: ButtonFace !important; outline-color: ButtonText !important; } :host(:not(:disabled):not([disabled-interactive]):not([toggle])) .icon { color: ButtonText !important; } :host(:not(:disabled):not([disabled-interactive])[toggle]:not([selected])) .base { background-color: ButtonFace !important; outline-color: ButtonText !important; } :host(:not(:disabled):not([disabled-interactive])[toggle]:not([selected])) .icon, :host(:hover:not(:disabled):not([disabled-interactive])[toggle]:not([selected])) .icon, :host(:not(:disabled):not([disabled-interactive])[toggle]:not([selected]):focus) .icon { color: ButtonText !important; } :host(:not(:disabled):not([disabled-interactive])[toggle][selected]) .base { background-color: ButtonText !important; outline: none !important; } :host(:not(:disabled):not([disabled-interactive])[toggle][selected]) .icon, :host(:hover:not(:disabled):not([disabled-interactive])[toggle][selected]) .icon, :host(:not(:disabled):not([disabled-interactive])[toggle][selected]:focus) .icon { forced-color-adjust: none; color: ButtonFace !important; background-color: ButtonText !important; } :host(:disabled) .base, :host([disabled-interactive]) .base { outline-color: GrayText !important; background-color: unset !important; } :host(:disabled) .icon, :host([disabled-interactive]) .icon { color: GrayText !important; } .base { outline-style: solid; } :host(:is(:state(--extra-small), :--extra-small)) .base { outline-offset: calc(0px - var(--m3e-icon-button-extra-small-outline-thickness, 1px)); outline-width: var(--m3e-icon-button-extra-small-outline-thickness, 1px); } :host(:is(:state(--small), :--small)) .base { outline-offset: calc(0px - var(--m3e-icon-button-small-outline-thickness, 1px)); outline-width: var(--m3e-icon-button-small-outline-thickness, 1px); } :host(:is(:state(--medium), :--medium)) .base { outline-offset: calc(0px - var(--m3e-icon-button-medium-outline-thickness, 1px)); outline-width: var(--m3e-icon-button-medium-outline-thickness, 1px); } :host(:is(:state(--large), :--large)) .base { outline-offset: calc(0px - var(--m3e-icon-button-large-outline-thickness, 2px)); outline-width: var(--m3e-icon-button-large-outline-thickness, 2px); } :host(:is(:state(--extra-large), :--extra-large)) .base { outline-offset: calc(0px - var(--m3e-icon-button-extra-large-outline-thickness, 3px)); outline-width: var(--m3e-icon-button-extra-large-outline-thickness, 3px); } } @media (prefers-reduced-motion) { :host(:is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected))), :host(:is(:state(--grouped), :--grouped):not(:is(:state(--connected), :--connected))) .wrapper, :host(:is(:state(--pressed), :--pressed)) .base, :host(:is(:state(--resting), :--resting)) .base, .base, .wrapper, .icon { transition: none; } }`, G = {
	elevated: {
		iconColor: c(`var(--m3e-elevated-icon-button-icon-color, var(--m3e-icon-button-icon-color, ${j.color.primary}))`),
		containerColor: c(`var(--m3e-elevated-icon-button-container-color, var(--m3e-icon-button-container-color, ${j.color.surfaceContainerLow}))`),
		containerElevation: c(`var(--m3e-elevated-icon-button-container-elevation, var(--m3e-icon-button-container-elevation, ${j.elevation.level1}))`),
		unselectedIconColor: c(`var(--m3e-elevated-icon-button-unselected-icon-color, var(--m3e-icon-button-unselected-icon-color, ${j.color.primary}))`),
		unselectedContainerColor: c(`var(--m3e-elevated-icon-button-unselected-container-color, var(--m3e-icon-button-unselected-container-color, ${j.color.surfaceContainerLow}))`),
		selectedIconColor: c(`var(--m3e-elevated-icon-button-selected-icon-color, var(--m3e-icon-button-selected-icon-color, ${j.color.onPrimary}))`),
		selectedContainerColor: c(`var(--m3e-elevated-icon-button-selected-container-color, var(--m3e-icon-button-selected-container-color, ${j.color.primary}))`),
		disabled: {
			containerColor: c(`var(--m3e-elevated-icon-button-disabled-container-color, var(--m3e-icon-button-disabled-container-color, ${j.color.onSurface}))`),
			containerOpacity: c("var(--m3e-elevated-icon-button-disabled-container-opacity, var(--m3e-icon-button-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-elevated-icon-button-disabled-icon-color, var(--m3e-icon-button-disabled-icon-color, ${j.color.onSurface}))`),
			iconOpacity: c("var(--m3e-elevated-icon-button-disabled-icon-opacity, var(--m3e-icon-button-disabled-icon-opacity, 38%))"),
			containerElevation: c(`var(--m3e-elevated-icon-button-disabled-container-elevation, var(--m3e-icon-button-disabled-container-elevation, ${j.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-elevated-icon-button-hover-icon-color, var(--m3e-icon-button-hover-icon-color, ${j.color.primary}))`),
			stateLayerColor: c(`var(--m3e-elevated-icon-button-hover-state-layer-color, var(--m3e-icon-button-hover-state-layer-color, ${j.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-elevated-icon-button-hover-state-layer-opacity, var(--m3e-icon-button-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-elevated-icon-button-hover-container-elevation, var(--m3e-icon-button-hover-container-elevation, ${j.elevation.level2}))`),
			unselectedIconColor: c(`var(--m3e-elevated-icon-button-hover-unselected-icon-color, var(--m3e-icon-button-hover-unselected-icon-color, ${j.color.primary}))`),
			unselectedStateLayerColor: c(`var(--m3e-elevated-icon-button-hover-unselected-state-layer-color, var(--m3e-icon-button-hover-unselected-state-layer-color, ${j.color.primary}))`),
			selectedIconColor: c(`var(--m3e-elevated-icon-button-hover-selected-icon-color, var(--m3e-icon-button-hover-selected-icon-color, ${j.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-elevated-icon-button-hover-selected-state-layer-color, var(--m3e-icon-button-hover-selected-state-layer-color, ${j.color.onPrimary}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-elevated-icon-button-focus-icon-color, var(--m3e-icon-button-focus-icon-color, ${j.color.primary}))`),
			stateLayerColor: c(`var(--m3e-elevated-icon-button-focus-state-layer-color, var(--m3e-icon-button-focus-state-layer-color, ${j.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-elevated-icon-button-focus-state-layer-opacity, var(--m3e-icon-button-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-elevated-icon-button-focus-container-elevation, var(--m3e-icon-button-focus-container-elevation, ${j.elevation.level1}))`),
			unselectedIconColor: c(`var(--m3e-elevated-icon-button-focus-unselected-icon-color, var(--m3e-icon-button-focus-unselected-icon-color, ${j.color.primary}))`),
			unselectedStateLayerColor: c(`var(--m3e-elevated-icon-button-focus-unselected-state-layer-color, var(--m3e-icon-button-focus-unselected-state-layer-color, ${j.color.primary}))`),
			selectedIconColor: c(`var(--m3e-elevated-icon-button-focus-selected-icon-color, var(--m3e-icon-button-focus-selected-icon-color, ${j.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-elevated-icon-button-focus-selected-state-layer-color, var(--m3e-icon-button-focus-selected-state-layer-color, ${j.color.onPrimary}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-elevated-icon-button-pressed-icon-color, var(--m3e-icon-button-pressed-icon-color, ${j.color.primary}))`),
			stateLayerColor: c(`var(--m3e-elevated-icon-button-pressed-state-layer-color, var(--m3e-icon-button-pressed-state-layer-color, ${j.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-elevated-icon-button-pressed-state-layer-opacity, var(--m3e-icon-button-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-elevated-icon-button-pressed-container-elevation, var(--m3e-icon-button-pressed-container-elevation, ${j.elevation.level1}))`),
			unselectedIconColor: c(`var(--m3e-elevated-icon-button-pressed-unselected-icon-color, var(--m3e-icon-button-pressed-unselected-icon-color, ${j.color.primary}))`),
			unselectedStateLayerColor: c(`var(--m3e-elevated-icon-button-pressed-unselected-state-layer-color, var(--m3e-icon-button-pressed-unselected-state-layer-color, ${j.color.primary}))`),
			selectedIconColor: c(`var(--m3e-elevated-icon-button-pressed-selected-icon-color, var(--m3e-icon-button-pressed-selected-icon-color, ${j.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-elevated-icon-button-pressed-selected-state-layer-color, var(--m3e-icon-button-pressed-selected-state-layer-color, ${j.color.onPrimary}))`)
		}
	},
	outlined: {
		iconColor: c(`var(--m3e-outlined-icon-button-icon-color, var(--m3e-icon-button-icon-color, ${j.color.onSurfaceVariant}))`),
		outlineColor: c(`var(--m3e-outlined-icon-button-outline-color, var(--m3e-icon-button-outline-color, ${j.color.outlineVariant}))`),
		unselectedIconColor: c(`var(--m3e-outlined-icon-button-unselected-icon-color, var(--m3e-icon-button-unselected-icon-color, ${j.color.onSurfaceVariant}))`),
		selectedIconColor: c(`var(--m3e-outlined-icon-button-selected-icon-color, var(--m3e-icon-button-selected-icon-color, ${j.color.inverseOnSurface}))`),
		selectedContainerColor: c(`var(--m3e-outlined-icon-button-selected-container-color, var(--m3e-icon-button-selected-container-color, ${j.color.inverseSurface}))`),
		disabled: {
			containerColor: c(`var(--m3e-outlined-icon-button-disabled-container-color, var(--m3e-icon-button-disabled-container-color, ${j.color.onSurface}))`),
			containerOpacity: c("var(--m3e-outlined-icon-button-disabled-container-opacity, var(--m3e-icon-button-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-outlined-icon-button-disabled-icon-color, var(--m3e-icon-button-disabled-icon-color, ${j.color.onSurface}))`),
			iconOpacity: c("var(--m3e-outlined-icon-button-disabled-icon-opacity, var(--m3e-icon-button-disabled-icon-opacity, 38%))"),
			outlineColor: c(`var(--m3e-outlined-icon-button-disabled-outline-color, var(--m3e-icon-button-disabled-outline-color, ${j.color.outlineVariant}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-outlined-icon-button-hover-icon-color, var(--m3e-icon-button-hover-icon-color, ${j.color.onSurfaceVariant}))`),
			outlineColor: c(`var(--m3e-outlined-icon-button-hover-outline-color, var(--m3e-icon-button-hover-outline-color, ${j.color.outlineVariant}))`),
			stateLayerColor: c(`var(--m3e-outlined-icon-button-hover-state-layer-color, var(--m3e-icon-button-hover-state-layer-color, ${j.color.onSurfaceVariant}))`),
			stateLayerOpacity: c(`var(--m3e-outlined-icon-button-hover-state-layer-opacity, var(--m3e-icon-button-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-outlined-icon-button-hover-unselected-icon-color, var(--m3e-icon-button-hover-unselected-icon-color, ${j.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-outlined-icon-button-hover-unselected-state-layer-color, var(--m3e-icon-button-hover-unselected-state-layer-color, ${j.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-outlined-icon-button-hover-selected-icon-color, var(--m3e-icon-button-hover-selected-icon-color, ${j.color.inverseOnSurface}))`),
			selectedStateLayerColor: c(`var(--m3e-outlined-icon-button-hover-selected-state-layer-color, var(--m3e-icon-button-hover-selected-state-layer-color, ${j.color.inverseOnSurface}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-outlined-icon-button-focus-icon-color, var(--m3e-icon-button-focus-icon-color, ${j.color.onSurfaceVariant}))`),
			outlineColor: c(`var(--m3e-outlined-icon-button-focus-outline-color, var(--m3e-icon-button-focus-outline-color, ${j.color.outlineVariant}))`),
			stateLayerColor: c(`var(--m3e-outlined-icon-button-focus-state-layer-color, var(--m3e-icon-button-focus-state-layer-color, ${j.color.onSurfaceVariant}))`),
			stateLayerOpacity: c(`var(--m3e-outlined-icon-button-focus-state-layer-opacity, var(--m3e-icon-button-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-outlined-icon-button-focus-unselected-icon-color, var(--m3e-icon-button-focus-unselected-icon-color, ${j.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-outlined-icon-button-focus-unselected-state-layer-color, var(--m3e-icon-button-focus-unselected-state-layer-color, ${j.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-outlined-icon-button-focus-selected-icon-color, var(--m3e-icon-button-focus-selected-icon-color, ${j.color.inverseOnSurface}))`),
			selectedStateLayerColor: c(`var(--m3e-outlined-icon-button-focus-selected-state-layer-color, var(--m3e-icon-button-focus-selected-state-layer-color, ${j.color.inverseOnSurface}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-outlined-icon-button-pressed-icon-color, var(--m3e-icon-button-pressed-icon-color, ${j.color.onSurfaceVariant}))`),
			outlineColor: c(`var(--m3e-outlined-icon-button-pressed-outline-color, var(--m3e-icon-button-pressed-outline-color, ${j.color.outlineVariant}))`),
			stateLayerColor: c(`var(--m3e-outlined-icon-button-pressed-state-layer-color, var(--m3e-icon-button-pressed-state-layer-color, ${j.color.onSurfaceVariant}))`),
			stateLayerOpacity: c(`var(--m3e-outlined-icon-button-pressed-state-layer-opacity, var(--m3e-icon-button-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-outlined-icon-button-pressed-unselected-icon-color, var(--m3e-icon-button-pressed-unselected-icon-color, ${j.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-outlined-icon-button-pressed-unselected-state-layer-color, var(--m3e-icon-button-pressed-unselected-state-layer-color, ${j.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-outlined-icon-button-pressed-selected-icon-color, var(--m3e-icon-button-pressed-selected-icon-color, ${j.color.inverseOnSurface}))`),
			selectedStateLayerColor: c(`var(--m3e-outlined-icon-button-pressed-selected-state-layer-color, var(--m3e-icon-button-pressed-selected-state-layer-color, ${j.color.inverseOnSurface}))`)
		}
	},
	filled: {
		iconColor: c(`var(--m3e-filled-icon-button-icon-color, var(--m3e-icon-button-icon-color, ${j.color.onPrimary}))`),
		containerColor: c(`var(--m3e-filled-icon-button-container-color, var(--m3e-icon-button-container-color, ${j.color.primary}))`),
		unselectedIconColor: c(`var(--m3e-filled-icon-button-unselected-icon-color, var(--m3e-icon-button-unselected-icon-color, ${j.color.onSurfaceVariant}))`),
		unselectedContainerColor: c(`var(--m3e-filled-icon-button-unselected-container-color, var(--m3e-icon-button-unselected-container-color, ${j.color.surfaceContainer}))`),
		selectedIconColor: c(`var(--m3e-filled-icon-button-selected-icon-color, var(--m3e-icon-button-selected-icon-color, ${j.color.onPrimary}))`),
		selectedContainerColor: c(`var(--m3e-filled-icon-button-selected-container-color, var(--m3e-icon-button-selected-container-color, ${j.color.primary}))`),
		disabled: {
			containerColor: c(`var(--m3e-filled-icon-button-disabled-container-color, var(--m3e-icon-button-disabled-container-color, ${j.color.onSurface}))`),
			containerOpacity: c("var(--m3e-filled-icon-button-disabled-container-opacity, var(--m3e-icon-button-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-filled-icon-button-disabled-icon-color, var(--m3e-icon-button-disabled-icon-color, ${j.color.onSurface}))`),
			iconOpacity: c("var(--m3e-filled-icon-button-disabled-icon-opacity, var(--m3e-icon-button-disabled-icon-opacity, 38%))")
		},
		hover: {
			iconColor: c(`var(--m3e-filled-icon-button-hover-icon-color, var(--m3e-icon-button-hover-icon-color, ${j.color.onPrimary}))`),
			stateLayerColor: c(`var(--m3e-filled-icon-button-hover-state-layer-color, var(--m3e-icon-button-hover-state-layer-color, ${j.color.onPrimary}))`),
			stateLayerOpacity: c(`var(--m3e-filled-icon-button-hover-state-layer-opacity, var(--m3e-icon-button-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-filled-icon-button-hover-unselected-icon-color, var(--m3e-icon-button-hover-unselected-icon-color, ${j.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-filled-icon-button-hover-unselected-state-layer-color, var(--m3e-icon-button-hover-unselected-state-layer-color, ${j.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-filled-icon-button-hover-selected-icon-color, var(--m3e-icon-button-hover-selected-icon-color, ${j.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-filled-icon-button-hover-selected-state-layer-color, var(--m3e-icon-button-hover-selected-state-layer-color, ${j.color.onPrimary}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-filled-icon-button-focus-icon-color, var(--m3e-icon-button-focus-icon-color, ${j.color.onPrimary}))`),
			stateLayerColor: c(`var(--m3e-filled-icon-button-focus-state-layer-color, var(--m3e-icon-button-focus-state-layer-color, ${j.color.onPrimary}))`),
			stateLayerOpacity: c(`var(--m3e-filled-icon-button-focus-state-layer-opacity, var(--m3e-icon-button-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-filled-icon-button-focus-unselected-icon-color, var(--m3e-icon-button-focus-unselected-icon-color, ${j.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-filled-icon-button-focus-unselected-state-layer-color, var(--m3e-icon-button-focus-unselected-state-layer-color, ${j.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-filled-icon-button-focus-selected-icon-color, var(--m3e-icon-button-focus-selected-icon-color, ${j.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-filled-icon-button-focus-selected-state-layer-color, var(--m3e-icon-button-focus-selected-state-layer-color, ${j.color.onPrimary}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-filled-icon-button-pressed-icon-color, var(--m3e-icon-button-pressed-icon-color, ${j.color.onPrimary}))`),
			stateLayerColor: c(`var(--m3e-filled-icon-button-pressed-state-layer-color, var(--m3e-icon-button-pressed-state-layer-color, ${j.color.onPrimary}))`),
			stateLayerOpacity: c(`var(--m3e-filled-icon-button-pressed-state-layer-opacity, var(--m3e-icon-button-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-filled-icon-button-pressed-unselected-icon-color, var(--m3e-icon-button-pressed-unselected-icon-color, ${j.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-filled-icon-button-pressed-unselected-state-layer-color, var(--m3e-icon-button-pressed-unselected-state-layer-color, ${j.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-filled-icon-button-pressed-selected-icon-color, var(--m3e-icon-button-pressed-selected-icon-color, ${j.color.onPrimary}))`),
			selectedStateLayerColor: c(`var(--m3e-filled-icon-button-pressed-selected-state-layer-color, var(--m3e-icon-button-pressed-selected-state-layer-color, ${j.color.onPrimary}))`)
		}
	},
	tonal: {
		iconColor: c(`var(--m3e-tonal-icon-button-icon-color, var(--m3e-icon-button-icon-color, ${j.color.onSecondaryContainer}))`),
		containerColor: c(`var(--m3e-tonal-icon-button-container-color, var(--m3e-icon-button-container-color, ${j.color.secondaryContainer}))`),
		unselectedIconColor: c(`var(--m3e-tonal-icon-button-unselected-icon-color, var(--m3e-icon-button-unselected-icon-color, ${j.color.onSecondaryContainer}))`),
		unselectedContainerColor: c(`var(--m3e-tonal-icon-button-unselected-container-color, var(--m3e-icon-button-unselected-container-color, ${j.color.secondaryContainer}))`),
		selectedIconColor: c(`var(--m3e-tonal-icon-button-selected-icon-color, var(--m3e-icon-button-selected-icon-color, ${j.color.onSecondary}))`),
		selectedContainerColor: c(`var(--m3e-tonal-icon-button-selected-container-color, var(--m3e-icon-button-selected-container-color, ${j.color.secondary}))`),
		disabled: {
			containerColor: c(`var(--m3e-tonal-icon-button-disabled-container-color, var(--m3e-icon-button-disabled-container-color, ${j.color.onSurface}))`),
			containerOpacity: c("var(--m3e-tonal-icon-button-disabled-container-opacity, var(--m3e-icon-button-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-tonal-icon-button-disabled-icon-color, var(--m3e-icon-button-disabled-icon-color, ${j.color.onSurface}))`),
			iconOpacity: c("var(--m3e-tonal-icon-button-disabled-icon-opacity, var(--m3e-icon-button-disabled-icon-opacity, 38%))")
		},
		hover: {
			iconColor: c(`var(--m3e-tonal-icon-button-hover-icon-color, var(--m3e-icon-button-hover-icon-color, ${j.color.onSecondaryContainer}))`),
			stateLayerColor: c(`var(--m3e-tonal-icon-button-hover-state-layer-color, var(--m3e-icon-button-hover-state-layer-color, ${j.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-tonal-icon-button-hover-state-layer-opacity, var(--m3e-icon-button-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-tonal-icon-button-hover-unselected-icon-color, var(--m3e-icon-button-hover-unselected-icon-color, ${j.color.onSecondaryContainer}))`),
			unselectedStateLayerColor: c(`var(--m3e-tonal-icon-button-hover-unselected-state-layer-color, var(--m3e-icon-button-hover-unselected-state-layer-color, ${j.color.onSecondaryContainer}))`),
			selectedIconColor: c(`var(--m3e-tonal-icon-button-hover-selected-icon-color, var(--m3e-icon-button-hover-selected-icon-color, ${j.color.onSecondary}))`),
			selectedStateLayerColor: c(`var(--m3e-tonal-icon-button-hover-selected-state-layer-color, var(--m3e-icon-button-hover-selected-state-layer-color, ${j.color.onSecondary}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-tonal-icon-button-focus-icon-color, var(--m3e-icon-button-focus-icon-color, ${j.color.onSecondaryContainer}))`),
			stateLayerColor: c(`var(--m3e-tonal-icon-button-focus-state-layer-color, var(--m3e-icon-button-focus-state-layer-color, ${j.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-tonal-icon-button-focus-state-layer-opacity, var(--m3e-icon-button-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-tonal-icon-button-focus-unselected-icon-color, var(--m3e-icon-button-focus-unselected-icon-color, ${j.color.onSecondaryContainer}))`),
			unselectedStateLayerColor: c(`var(--m3e-tonal-icon-button-focus-unselected-state-layer-color, var(--m3e-icon-button-focus-unselected-state-layer-color, ${j.color.onSecondaryContainer}))`),
			selectedIconColor: c(`var(--m3e-tonal-icon-button-focus-selected-icon-color, var(--m3e-icon-button-focus-selected-icon-color, ${j.color.onSecondary}))`),
			selectedStateLayerColor: c(`var(--m3e-tonal-icon-button-focus-selected-state-layer-color, var(--m3e-icon-button-focus-selected-state-layer-color, ${j.color.onSecondary}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-tonal-icon-button-pressed-icon-color, var(--m3e-icon-button-pressed-icon-color, ${j.color.onSecondaryContainer}))`),
			stateLayerColor: c(`var(--m3e-tonal-icon-button-pressed-state-layer-color, var(--m3e-icon-button-pressed-state-layer-color, ${j.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-tonal-icon-button-pressed-state-layer-opacity, var(--m3e-icon-button-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-tonal-icon-button-pressed-unselected-icon-color, var(--m3e-icon-button-pressed-unselected-icon-color, ${j.color.onSecondaryContainer}))`),
			unselectedStateLayerColor: c(`var(--m3e-tonal-icon-button-pressed-unselected-state-layer-color, var(--m3e-icon-button-pressed-unselected-state-layer-color, ${j.color.onSecondaryContainer}))`),
			selectedIconColor: c(`var(--m3e-tonal-icon-button-pressed-selected-icon-color, var(--m3e-icon-button-pressed-selected-icon-color, ${j.color.onSecondary}))`),
			selectedStateLayerColor: c(`var(--m3e-tonal-icon-button-pressed-selected-state-layer-color, var(--m3e-icon-button-pressed-selected-state-layer-color, ${j.color.onSecondary}))`)
		}
	},
	standard: {
		iconColor: c(`var(--m3e-standard-icon-button-icon-color, var(--m3e-icon-button-icon-color, ${j.color.onSurfaceVariant}))`),
		containerColor: c("var(--m3e-standard-icon-button-container-color)"),
		unselectedIconColor: c(`var(--m3e-standard-icon-button-unselected-icon-color, var(--m3e-icon-button-unselected-icon-color, ${j.color.onSurfaceVariant}))`),
		unselectedContainerColor: c("var(--m3e-standard-icon-button-unselected-container-color)"),
		selectedContainerColor: c("var(--m3e-standard-icon-button-selected-container-color)"),
		selectedIconColor: c(`var(--m3e-standard-icon-button-selected-icon-color, var(--m3e-icon-button-selected-icon-color, ${j.color.primary}))`),
		disabled: {
			containerColor: c("var(--m3e-standard-icon-button-disabled-container-color, var(--m3e-icon-button-disabled-container-color, transparent))"),
			containerOpacity: c("var(--m3e-standard-icon-button-disabled-container-opacity, var(--m3e-icon-button-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-standard-icon-button-disabled-icon-color, var(--m3e-icon-button-disabled-icon-color, ${j.color.onSurface}))`),
			iconOpacity: c("var(--m3e-standard-icon-button-disabled-icon-opacity, var(--m3e-icon-button-disabled-icon-opacity, 38%))")
		},
		hover: {
			iconColor: c(`var(--m3e-standard-icon-button-hover-icon-color, var(--m3e-icon-button-hover-icon-color, ${j.color.onSurfaceVariant}))`),
			stateLayerColor: c(`var(--m3e-standard-icon-button-hover-state-layer-color, var(--m3e-icon-button-hover-state-layer-color, ${j.color.onSurfaceVariant}))`),
			stateLayerOpacity: c(`var(--m3e-standard-icon-button-hover-state-layer-opacity, var(--m3e-icon-button-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-standard-icon-button-hover-unselected-icon-color, var(--m3e-icon-button-hover-unselected-icon-color, ${j.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-standard-icon-button-hover-unselected-state-layer-color, var(--m3e-icon-button-hover-unselected-state-layer-color, ${j.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-standard-icon-button-hover-selected-icon-color, var(--m3e-icon-button-hover-selected-icon-color, ${j.color.primary}))`),
			selectedStateLayerColor: c(`var(--m3e-standard-icon-button-hover-selected-state-layer-color, var(--m3e-icon-button-hover-selected-state-layer-color, ${j.color.primary}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-standard-icon-button-focus-icon-color, var(--m3e-icon-button-focus-icon-color, ${j.color.onSurfaceVariant}))`),
			stateLayerColor: c(`var(--m3e-standard-icon-button-focus-state-layer-color, var(--m3e-icon-button-focus-state-layer-color, ${j.color.onSurfaceVariant}))`),
			stateLayerOpacity: c(`var(--m3e-standard-icon-button-focus-state-layer-opacity, var(--m3e-icon-button-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-standard-icon-button-focus-unselected-icon-color, var(--m3e-icon-button-focus-unselected-icon-color, ${j.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-standard-icon-button-focus-unselected-state-layer-color, var(--m3e-icon-button-focus-unselected-state-layer-color, ${j.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-standard-icon-button-focus-selected-icon-color, var(--m3e-icon-button-focus-selected-icon-color, ${j.color.primary}))`),
			selectedStateLayerColor: c(`var(--m3e-standard-icon-button-focus-selected-state-layer-color, var(--m3e-icon-button-focus-selected-state-layer-color, ${j.color.primary}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-standard-icon-button-pressed-icon-color, var(--m3e-icon-button-pressed-icon-color, ${j.color.onSurfaceVariant}))`),
			stateLayerColor: c(`var(--m3e-standard-icon-button-pressed-state-layer-color, var(--m3e-icon-button-pressed-state-layer-color, ${j.color.onSurfaceVariant}))`),
			stateLayerOpacity: c(`var(--m3e-standard-icon-button-pressed-state-layer-opacity, var(--m3e-icon-button-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			unselectedIconColor: c(`var(--m3e-standard-icon-button-pressed-unselected-icon-color, var(--m3e-icon-button-pressed-unselected-icon-color, ${j.color.onSurfaceVariant}))`),
			unselectedStateLayerColor: c(`var(--m3e-standard-icon-button-pressed-unselected-state-layer-color, var(--m3e-icon-button-pressed-unselected-state-layer-color, ${j.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-standard-icon-button-pressed-selected-icon-color, var(--m3e-icon-button-pressed-selected-icon-color, ${j.color.primary}))`),
			selectedStateLayerColor: c(`var(--m3e-standard-icon-button-pressed-selected-state-layer-color, var(--m3e-icon-button-pressed-selected-state-layer-color, ${j.color.primary}))`)
		}
	}
};
function Gc(e) {
	let t = c(`:is(:state(--${e}), :--${e})`);
	return l`:host(${t}:not(:disabled):not([disabled-interactive])) .base { background-color: ${G[e].containerColor ?? c("unset")}; --m3e-state-layer-hover-color: ${G[e].hover.stateLayerColor}; --m3e-state-layer-hover-opacity: ${G[e].hover.stateLayerOpacity}; --m3e-state-layer-focus-color: ${G[e].focus.stateLayerColor}; --m3e-state-layer-focus-opacity: ${G[e].focus.stateLayerOpacity}; --m3e-ripple-color: ${G[e].pressed.stateLayerColor}; --m3e-ripple-opacity: ${G[e].pressed.stateLayerOpacity}; --m3e-elevation-level: ${G[e].containerElevation ?? c("unset")}; --m3e-elevation-hover-level: ${G[e].hover.containerElevation ?? c("unset")}; --m3e-elevation-focus-level: ${G[e].focus.containerElevation ?? c("unset")}; --m3e-elevation-pressed-level: ${G[e].pressed.containerElevation ?? c("unset")}; } :host(${t}[toggle]:not([selected]):not(:disabled):not([disabled-interactive])) .base { background-color: ${G[e].unselectedContainerColor ?? c("unset")}; --m3e-state-layer-hover-color: ${G[e].hover.unselectedStateLayerColor}; --m3e-state-layer-focus-color: ${G[e].focus.unselectedStateLayerColor}; --m3e-ripple-color: ${G[e].pressed.unselectedStateLayerColor}; } :host(${t}[toggle][selected]:not(:disabled):not([disabled-interactive])) .base { background-color: ${G[e].selectedContainerColor ?? c("unset")}; --m3e-state-layer-hover-color: ${G[e].hover.selectedStateLayerColor}; --m3e-state-layer-focus-color: ${G[e].focus.selectedStateLayerColor}; --m3e-ripple-color: ${G[e].pressed.selectedStateLayerColor}; } :host(${t}:not(:disabled):not([disabled-interactive])) .base { outline-color: ${G[e].outlineColor ?? c("unset")}; } :host(${t}:focus:not(:disabled):not([disabled-interactive])) .base { outline-color: ${G[e].focus.outlineColor ?? c("unset")}; } :host(${t}:hover:not(:disabled):not([disabled-interactive])) .base { outline-color: ${G[e].hover.outlineColor ?? c("unset")}; } :host(${t}:is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive])) .base { outline-color: ${G[e].pressed.outlineColor ?? c("unset")}; } :host(${t}:not(:disabled):not([disabled-interactive])) .icon { color: ${G[e].iconColor}; } :host(${t}[toggle]:not([selected]):not(:disabled):not([disabled-interactive])) .icon { color: ${G[e].unselectedIconColor}; } :host(${t}[toggle][selected]:not(:disabled):not([disabled-interactive])) .icon { color: ${G[e].selectedIconColor}; } :host(${t}:focus:not(:disabled):not([disabled-interactive])) .icon { color: ${G[e].focus.iconColor}; } :host(${t}[toggle]:not([selected]):focus:not(:disabled):not([disabled-interactive])) .icon { color: ${G[e].focus.unselectedIconColor}; } :host(${t}[toggle][selected]:focus:not(:disabled):not([disabled-interactive])) .icon { color: ${G[e].focus.selectedIconColor}; } :host(${t}:hover:not(:disabled):not([disabled-interactive])) .icon { color: ${G[e].hover.iconColor}; } :host(${t}[toggle]:not([selected]):hover:not(:disabled):not([disabled-interactive])) .icon { color: ${G[e].hover.unselectedIconColor}; } :host(${t}[toggle][selected]:hover:not(:disabled):not([disabled-interactive])) .icon { color: ${G[e].hover.selectedIconColor}; } :host(${t}:is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive])) .icon { color: ${G[e].pressed.iconColor}; } :host( ${t}[toggle]:not([selected]):is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive]) ) .icon { color: ${G[e].pressed.unselectedIconColor}; } :host(${t}[toggle][selected]:is(:state(--pressed), :--pressed):not(:disabled):not([disabled-interactive])) .icon { color: ${G[e].pressed.selectedIconColor}; } :host(${t}:disabled) .base, :host(${t}[disabled-interactive]) .base { outline-color: ${G[e].disabled.outlineColor ?? c("unset")}; background-color: color-mix( in srgb, ${G[e].disabled.containerColor} ${G[e].disabled.containerOpacity}, transparent ); } :host(${t}:disabled) .icon, :host(${t}[disabled-interactive]) .icon { color: color-mix( in srgb, ${G[e].disabled.iconColor} ${G[e].disabled.iconOpacity}, transparent ); }`;
}
var Kc = [
	Gc("standard"),
	Gc("outlined"),
	Gc("filled"),
	Gc("tonal"),
	Gc("elevated"),
	l`:host(:is(:state(--outlined), :--outlined):not([toggle][selected]):not(:disabled):not([disabled-interactive])) .base { outline-style: solid; }`
], qc, Jc, Yc, Xc, Zc, Qc, $c, el, tl, nl, rl, il = class extends ji(Fi(Di(Ti(si(ni(E(M(S, "button"), !0))))))) {
	constructor() {
		super(), qc.add(this), this._adjacentPressedTimeout = -1, Jc.set(this, (e) => t(this, qc, "m", nl).call(this, e)), this.variant = "standard", this.shape = "rounded", this.size = "small", this.width = "default", this.toggle = !1, this.selected = !1, new tr(this, { callback: () => this._handleResize() }), new nn(this, { callback: (e) => {
			!this.disabledInteractive && !e && !this.grouped && this._base?.style.removeProperty("--_button-shape");
		} }), new Yn(this, {
			isPressedKey: (e) => e === " ",
			minPressedDuration: 225,
			callback: (e) => {
				!this.disabled && !this.disabledInteractive && (e ? (t(this, qc, "m", $c).call(this), t(this, qc, "m", el).call(this, !0)) : t(this, qc, "m", el).call(this, !1));
			}
		});
	}
	get grouped() {
		return Sr(this, "--grouped");
	}
	render() {
		return b`<div class="layout"><div class="base"><m3e-state-layer class="state-layer" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-state-layer><m3e-elevation class="elevation" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-elevation><m3e-focus-ring class="focus-ring" ?disabled="${this.disabled}"></m3e-focus-ring><m3e-ripple class="ripple" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-ripple><div class="touch" aria-hidden="true"></div>${this[Mi]()}<div class="wrapper">${this.toggle ? b`<slot class="icon" name="selected" aria-hidden="true" @slotchange="${t(this, qc, "m", rl)}"></slot>` : x}<slot class="icon" aria-hidden="true"></slot></div></div></div>`;
	}
	connectedCallback() {
		super.connectedCallback(), t(this, qc, "m", Yc).call(this), t(this, qc, "m", Xc).call(this), t(this, qc, "m", Zc).call(this), t(this, qc, "m", Qc).call(this), this.addEventListener("click", t(this, Jc, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), [
			"--pressed",
			"--resting",
			"--grouped",
			"--connected"
		].forEach((e) => D(this, e)), this._base?.style.removeProperty("--_button-shape"), this.style.removeProperty("--_button-width"), this.style.removeProperty("--_adjacent-shrink"), D(this, "--adjacent-pressed"), this.removeEventListener("click", t(this, Jc, "f"));
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, qc, "m", Yc).call(this), e.has("shape") && t(this, qc, "m", Xc).call(this), e.has("size") && t(this, qc, "m", Zc).call(this), e.has("width") && t(this, qc, "m", Qc).call(this);
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
		if (super.updated(e), (e.has("disabled") && this.disabled || e.has("disabledInteractive") && this.disabledInteractive) && (D(this, "--pressed"), D(this, "--resting")), (e.has("toggle") || e.has("selected")) && (this.ariaPressed = this.toggle ? `${this.selected}` : null, this.toggle)) for (let e of this.querySelectorAll("m3e-icon")) e.toggleAttribute("filled", this.selected);
	}
	_handleResize() {
		if (this.grouped && !Sr(this, "--no-resize") && this !== document.activeElement) {
			let e = this.getBoundingClientRect().width;
			e > 0 ? (this.style.setProperty("--_button-width", `${e}px`), t(this, qc, "m", $c).call(this, !0)) : this.style.removeProperty("--_button-width");
		}
	}
};
Jc = /* @__PURE__ */ new WeakMap(), qc = /* @__PURE__ */ new WeakSet(), Yc = function() {
	Bc(this.variant) || (this.variant = "standard"), wr(this, this.variant, "elevated", "filled", "outlined", "standard", "tonal");
}, Xc = function() {
	zc(this.shape) || (this.shape = "rounded"), wr(this, this.shape, "rounded", "square");
}, Zc = function() {
	Rc(this.size) || (this.size = "small"), wr(this, this.size, "extra-large", "extra-small", "large", "medium", "small");
}, Qc = function() {
	Vc(this.width) || (this.width = "default"), wr(this, this.width, "default", "narrow", "wide");
}, $c = function(e = !1) {
	if (!this._base) return;
	let t = parseFloat(getComputedStyle(this._base).borderRadius);
	if (!isNaN(t) || e) {
		let n = this.clientHeight / 2;
		(n < t || e) && this._base?.style.setProperty("--_button-shape", `${n}px`);
	}
}, el = function(e) {
	let n = this.getBoundingClientRect().width, r = this.closest("m3e-button-group");
	if (r && r.variant === "standard") {
		let i = [...r.querySelectorAll("m3e-button,m3e-icon-button")];
		for (let e of i) clearTimeout(e._adjacentPressedTimeout), e._adjacentPressedTimeout = -1;
		let a = i.indexOf(this);
		if (e) {
			let e = n * parseFloat(getComputedStyle(this).getPropertyValue("--m3e-standard-button-group-width-multiplier") || "0.15");
			a > 0 && a < i.length - 1 && (e /= 2);
			for (let t = 0; t < i.length; t++) t == a - 1 || t == a + 1 ? (Cr(i[t], "--no-resize"), i[t].style.setProperty("--_adjacent-shrink", `${e}px`), Cr(i[t], "--adjacent-pressed")) : t == a ? (Cr(i[t], "--no-resize"), i[t].style.removeProperty("--_adjacent-shrink"), D(i[t], "--adjacent-pressed")) : (D(i[t], "--no-resize"), i[t].style.removeProperty("--_adjacent-shrink"), D(i[t], "--adjacent-pressed"));
		} else {
			for (let e = 0; e < i.length; e++) (e == a - 1 || e == a + 1) && i[e].style.setProperty("--_adjacent-shrink", "0px");
			It() ? t(this, qc, "m", tl).call(this, i) : this.addEventListener("transitionend", (e) => {
				e.propertyName === "width" && (this._adjacentPressedTimeout = setTimeout(() => {
					this._adjacentPressedTimeout > -1 && t(this, qc, "m", tl).call(this, i);
				}, 600));
			}, { once: !0 });
		}
	}
	O(this, "--pressed", e), O(this, "--resting", !e);
}, tl = function(e) {
	for (let t of e) D(t, "--adjacent-pressed"), D(t, "--no-resize"), t.style.removeProperty("--_adjacent-shrink");
}, nl = function(e) {
	(this.disabled || this.disabledInteractive) && (e.preventDefault(), e.stopImmediatePropagation()), this.toggle && !e.defaultPrevented && this.dispatchEvent(new Event("beforeinput", {
		bubbles: !0,
		cancelable: !0
	})) && (this.selected = !this.selected, this.dispatchEvent(new Event("input", { bubbles: !0 })), this.dispatchEvent(new Event("change", { bubbles: !0 })));
}, rl = function(e) {
	this._base?.classList.toggle("with-selected-icon", Ft(e.target));
}, il.styles = [
	Uc,
	Kc,
	Wc
], e([w(".base")], il.prototype, "_base", void 0), e([w(".elevation")], il.prototype, "_elevation", void 0), e([w(".focus-ring")], il.prototype, "_focusRing", void 0), e([w(".state-layer")], il.prototype, "_stateLayer", void 0), e([w(".ripple")], il.prototype, "_ripple", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], il.prototype, "variant", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], il.prototype, "shape", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], il.prototype, "size", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], il.prototype, "width", void 0), e([C({
	type: Boolean,
	reflect: !0
})], il.prototype, "toggle", void 0), e([C({
	type: Boolean,
	reflect: !0
})], il.prototype, "selected", void 0), e([nr(40)], il.prototype, "_handleResize", null), il = e([T("m3e-icon-button")], il);
//#endregion
//#region node_modules/@m3e/web/dist/split-button.js
function al(e) {
	return e === "elevated" || e === "filled" || e === "tonal" || e === "outlined";
}
var ol, sl, cl, ll, ul, dl, fl, pl, ml, hl, gl, _l, vl, yl, bl = class extends M(E(S), "group") {
	constructor() {
		super(...arguments), ol.add(this), sl.set(this, void 0), cl.set(this, void 0), ll.set(this, !1), ul.set(this, new Yn(this, {
			target: null,
			capture: !0,
			minPressedDuration: 225,
			isPressedKey: (e) => e === " ",
			callback: (e, n, r) => {
				switch (r) {
					case t(this, sl, "f"):
						t(this, ol, "m", _l).call(this, e);
						break;
					case t(this, cl, "f"): t(this, ol, "m", vl).call(this, e || t(this, ll, "f"));
				}
			}
		})), dl.set(this, new hn(this, {
			target: null,
			callback: (e, r) => {
				switch (r) {
					case t(this, cl, "f"): !t(this, cl, "f")?.disabled && !t(this, cl, "f")?.disabledInteractive && (n(this, ll, e, "f"), t(this, ol, "m", vl).call(this, e));
				}
			}
		})), this.variant = "filled", this.size = "small";
	}
	connectedCallback() {
		super.connectedCallback(), t(this, ol, "m", fl).call(this), t(this, ol, "m", pl).call(this);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, ol, "m", fl).call(this), e.has("size") && t(this, ol, "m", pl).call(this);
	}
	update(e) {
		super.update(e), e.has("variant") && t(this, ol, "m", gl).call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), e.has("variant") || t(this, ol, "m", gl).call(this);
	}
	render() {
		return b`<m3e-button-group class="base" disable-role variant="connected" size="${this.size}"><slot name="leading-button" @slotchange="${t(this, ol, "m", ml)}"></slot><slot name="trailing-button" @slotchange="${t(this, ol, "m", hl)}"></slot></m3e-button-group>`;
	}
};
sl = /* @__PURE__ */ new WeakMap(), cl = /* @__PURE__ */ new WeakMap(), ll = /* @__PURE__ */ new WeakMap(), ul = /* @__PURE__ */ new WeakMap(), dl = /* @__PURE__ */ new WeakMap(), ol = /* @__PURE__ */ new WeakSet(), fl = function() {
	al(this.variant) || (this.variant = "filled");
}, pl = function() {
	lc(this.size) || (this.size = "small"), wr(this, this.size, "extra-large", "extra-small", "large", "medium", "small");
}, ml = function(e) {
	t(this, sl, "f") && t(this, ul, "f").unobserve(t(this, sl, "f")), n(this, sl, e.target.assignedElements({ flatten: !0 }).find((e) => e instanceof Ec), "f"), t(this, sl, "f") && t(this, ul, "f").observe(t(this, sl, "f")), t(this, ol, "m", gl).call(this);
}, hl = function(e) {
	t(this, cl, "f") && (t(this, ul, "f").unobserve(t(this, cl, "f")), t(this, dl, "f").unobserve(t(this, cl, "f"))), n(this, cl, e.target.assignedElements({ flatten: !0 }).find((e) => e instanceof il), "f"), t(this, cl, "f") && (t(this, ul, "f").observe(t(this, cl, "f")), t(this, dl, "f").observe(t(this, cl, "f"))), t(this, ol, "m", gl).call(this);
}, gl = function() {
	t(this, sl, "f") && (t(this, sl, "f").variant = this.variant, t(this, sl, "f").size = this.size, t(this, sl, "f").shape = "rounded"), t(this, cl, "f") && (t(this, cl, "f").width = "default", t(this, cl, "f").shape = "rounded", t(this, cl, "f").setAttribute("variant", this.variant), t(this, cl, "f").size = this.size);
}, _l = function(e) {
	e && t(this, sl, "f") ? t(this, ol, "m", yl).call(this, t(this, sl, "f"), "--_leading-button-shape") : this._base?.style.removeProperty("--_leading-button-shape");
}, vl = function(e) {
	e && t(this, cl, "f") ? t(this, ol, "m", yl).call(this, t(this, cl, "f"), "--_trailing-button-shape") : this._base?.style.removeProperty("--_trailing-button-shape");
}, yl = function(e, t) {
	let n = e.clientHeight / 2;
	n && this._base?.style.setProperty(t, `${n}px`);
}, bl.styles = l`:host { display: inline-flex; vertical-align: middle; } :host([hidden]) { display: none; } ::slotted([slot="leading-button"]) { flex: 1 1 auto; min-width: 0; --_button-start-shape-pressed-morph: var(--_leading-button-shape, ${j.shape.corner.full}); } ::slotted([slot="trailing-button"]:not([aria-expanded="true"])) { --m3e-icon-button-extra-small-default-leading-space: var( --m3e-split-button-extra-small-trailing-button-unselected-leading-space, 12px ); --m3e-icon-button-extra-small-default-trailing-space: var( --m3e-split-button-extra-small-trailing-button-unselected-trailing-space, 14px ); --m3e-icon-button-small-default-leading-space: var( --m3e-split-button-small-trailing-button-unselected-leading-space, 12px ); --m3e-icon-button-small-default-trailing-space: var( --m3e-split-button-small-trailing-button-unselected-trailing-space, 14px ); --m3e-icon-button-medium-default-leading-space: var( --m3e-split-button-medium-trailing-button-unselected-leading-space, 13px ); --m3e-icon-button-medium-default-trailing-space: var( --m3e-split-button-medium-trailing-button-unselected-trailing-space, 17px ); --m3e-icon-button-large-default-leading-space: var( --m3e-split-button-large-trailing-button-unselected-leading-space, 26px ); --m3e-icon-button-large-default-trailing-space: var( --m3e-split-button-large-trailing-button-unselected-trailing-space, 32px ); --m3e-icon-button-extra-large-default-leading-space: var( --m3e-split-button-extra-large-trailing-button-unselected-leading-space, 37px ); --m3e-icon-button-extra-large-default-trailing-space: var( --m3e-split-button-extra-large-trailing-button-unselected-trailing-space, 49px ); } ::slotted([slot="trailing-button"][aria-expanded="true"]) { --m3e-icon-button-extra-small-default-leading-space: var( --m3e-split-button-extra-small-trailing-button-selected-leading-space, 13px ); --m3e-icon-button-extra-small-default-trailing-space: var( --m3e-split-button-extra-small-trailing-button-selected-trailing-space, 13px ); --m3e-icon-button-small-default-leading-space: var( --m3e-split-button-small-trailing-button-selected-leading-space, 13px ); --m3e-icon-button-small-default-trailing-space: var( --m3e-split-button-small-trailing-button-selected-trailing-space, 13px ); --m3e-icon-button-medium-default-leading-space: var( --m3e-split-button-medium-trailing-button-selected-leading-space, 15px ); --m3e-icon-button-medium-default-trailing-space: var( --m3e-split-button-medium-trailing-button-selected-trailing-space, 15px ); --m3e-icon-button-large-default-leading-space: var( --m3e-split-button-large-trailing-button-selected-leading-space, 29px ); --m3e-icon-button-large-default-trailing-space: var( --m3e-split-button-large-trailing-button-selected-trailing-space, 29px ); --m3e-icon-button-extra-large-default-leading-space: var( --m3e-split-button-extra-large-trailing-button-selected-leading-space, 43px ); --m3e-icon-button-extra-large-default-trailing-space: var( --m3e-split-button-extra-large-trailing-button-selected-trailing-space, 43px ); } ::slotted([slot="leading-button"]:not(:hover)), ::slotted([slot="leading-button"]:disabled), ::slotted([slot="leading-button"][disabled-interactive]) { --m3e-connected-button-group-extra-small-inner-shape: var( --m3e-split-button-extra-small-inner-corner-size, ${j.shape.corner.extraSmall} ); --m3e-connected-button-group-small-inner-shape: var( --m3e-split-button-small-inner-corner-size, ${j.shape.corner.extraSmall} ); --m3e-connected-button-group-medium-inner-shape: var( --m3e-split-button-medium-inner-corner-size, ${j.shape.corner.extraSmall} ); --m3e-connected-button-group-large-inner-shape: var( --m3e-split-button-large-inner-corner-size, ${j.shape.corner.small} ); --m3e-connected-button-group-extra-large-inner-shape: var( --m3e-split-button-extra-large-inner-corner-size, ${j.shape.corner.medium} ); } ::slotted([slot="leading-button"]:hover:not(:disabled):not([disabled-interactive])), ::slotted([slot="trailing-button"]:not([aria-expanded="true"]):hover:not(:disabled):not([disabled-interactive])) { --m3e-connected-button-group-extra-small-inner-shape: var( --m3e-split-button-extra-small-inner-corner-hover-size, ${j.shape.corner.small} ); --m3e-connected-button-group-small-inner-shape: var( --m3e-split-button-small-inner-corner-hover-size, ${j.shape.corner.medium} ); --m3e-connected-button-group-medium-inner-shape: var( --m3e-split-button-medium-inner-corner-hover-size, ${j.shape.corner.medium} ); --m3e-connected-button-group-large-inner-shape: var( --m3e-split-button-large-inner-corner-hover-size, ${j.shape.corner.largeIncreased} ); --m3e-connected-button-group-extra-large-inner-shape: var( --m3e-split-button-extra-large-inner-corner-hover-size, ${j.shape.corner.largeIncreased} ); } ::slotted([slot="leading-button"]), ::slotted([slot="trailing-button"]) { --m3e-connected-button-group-extra-small-inner-pressed-shape: var( --m3e-split-button-extra-small-inner-corner-pressed-size, ${j.shape.corner.small} ); --m3e-connected-button-group-small-inner-pressed-shape: var( --m3e-split-button-small-inner-corner-pressed-size, ${j.shape.corner.medium} ); --m3e-connected-button-group-medium-inner-pressed-shape: var( --m3e-split-button-medium-inner-corner-pressed-size, ${j.shape.corner.medium} ); --m3e-connected-button-group-large-inner-pressed-shape: var( --m3e-split-button-large-inner-corner-pressed-size, ${j.shape.corner.largeIncreased} ); --m3e-connected-button-group-extra-large-inner-pressed-shape: var( --m3e-split-button-extra-large-inner-corner-pressed-size, ${j.shape.corner.largeIncreased} ); } ::slotted([slot="trailing-button"]:not([aria-expanded="true"]):not(:hover)), ::slotted([slot="trailing-button"]:disabled), ::slotted([slot="trailing-button"][disabled-interactive]) { --m3e-connected-button-group-extra-small-inner-shape: var( --m3e-split-button-extra-small-inner-corner-size, ${j.shape.corner.extraSmall} ); --m3e-connected-button-group-small-inner-shape: var( --m3e-split-button-small-inner-corner-size, ${j.shape.corner.extraSmall} ); --m3e-connected-button-group-medium-inner-shape: var( --m3e-split-button-medium-inner-corner-size, ${j.shape.corner.extraSmall} ); --m3e-connected-button-group-large-inner-shape: var( --m3e-split-button-large-inner-corner-size, ${j.shape.corner.small} ); --m3e-connected-button-group-extra-large-inner-shape: var( --m3e-split-button-extra-large-inner-corner-size, ${j.shape.corner.medium} ); } ::slotted([slot="trailing-button"][aria-expanded="true"]) { --_icon-button-icon-transform: rotate(180deg); --_button-rounded-start-shape: var(--_trailing-button-shape, ${j.shape.corner.full}); --_button-rounded-end-shape: var(--_trailing-button-shape, ${j.shape.corner.full}); } ::slotted([slot="trailing-button"]) { --_button-end-shape-pressed-morph: var(--_trailing-button-shape, ${j.shape.corner.full}); } .base { --m3e-icon-button-extra-small-icon-size: var(--m3e-spit-button-extra-small-trailing-button-icon-size, 22px); --m3e-button-extra-small-icon-size: var(--m3e-spit-button-extra-small-trailing-button-icon-size, 22px); --m3e-icon-button-small-icon-size: var(--m3e-spit-button-small-trailing-button-icon-size, 22px); --m3e-button-small-icon-size: var(--m3e-spit-button-small-trailing-button-icon-size, 22px); --m3e-icon-button-medium-icon-size: var(--m3e-spit-button-medium-trailing-button-icon-size, 26px); --m3e-button-medium-icon-size: var(--m3e-spit-button-medium-trailing-button-icon-size, 26px); --m3e-icon-button-large-icon-size: var(--m3e-spit-button-large-trailing-button-icon-size, 38px); --m3e-button-large-icon-size: var(--m3e-spit-button-large-trailing-button-icon-size, 38px); --m3e-icon-button-extra-large-icon-size: var(--m3e-spit-button-extra-large-trailing-button-icon-size, 50px); --m3e-button-extra-large-icon-size: var(--m3e-spit-button-extra-large-trailing-button-icon-size, 50px); } :host(:is(:state(--extra-small), :--extra-small)) .base { --m3e-connected-button-group-spacing: var(--m3e-split-button-extra-small-between-spacing, 2px); } :host(:is(:state(--small), :--small)) .base { --m3e-connected-button-group-spacing: var(--m3e-split-button-small-between-spacing, 2px); } :host(:is(:state(--medium), :--medium)).base { --m3e-connected-button-group-spacing: var(--m3e-split-button-medium-between-spacing, 2px); } :host(:is(:state(--large), :--large)) .base { --m3e-connected-button-group-spacing: var(--m3e-split-button-large-between-spacing, 2px); } :host(:is(:state(--extra-large), :--extra-large)) .base { --m3e-connected-button-group-spacing: var(--m3e-split-button-extra-large-between-spacing, 2px); }`, e([w(".base")], bl.prototype, "_base", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], bl.prototype, "variant", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], bl.prototype, "size", void 0), bl = e([T("m3e-split-button")], bl);
//#endregion
//#region node_modules/@m3e/web/dist/fab.js
function xl(e) {
	return e === "small" || e === "medium" || e === "large";
}
function Sl(e) {
	return e === "primary" || e === "primary-container" || e === "secondary" || e === "secondary-container" || e === "tertiary" || e === "tertiary-container" || e === "surface";
}
var Cl = l`:host { display: inline-block; outline: none; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } :host([hidden]) { display: none; } .base { box-sizing: border-box; vertical-align: middle; display: inline-flex; align-items: center; justify-content: center; position: relative; width: 100%; transition: ${c(`background-color ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; } .touch { position: absolute; height: 48px; left: 0; right: 0; } .wrapper { width: 100%; overflow: hidden; display: inline-flex; align-items: center; box-sizing: border-box; } .label { white-space: nowrap; transition: ${c(`color ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; } .icon { transition: ${c(`color ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; } :host(:not(:disabled):not([disabled-interactive])) { cursor: pointer; } :host([disabled-interactive]) { cursor: not-allowed; } .close-icon, ::slotted(:not([slot])), ::slotted([slot="close-icon"]) { font-size: inherit !important; flex: none; } .close-icon, ::slotted(svg:not([slot])), ::slotted(svg[slot="close-icon"]) { width: 1em; height: 1em; } .base.with-menu ::slotted([slot="label"]), .base:not(.with-menu) ::slotted([slot="close-icon"]), .base:not(.with-menu) .close-icon, :host([aria-expanded="true"]) .base.with-menu ::slotted(:not([slot])), :host([aria-expanded="false"]) .base.with-menu ::slotted([slot="close-icon"]), :host([aria-expanded="false"]) .base.with-menu .close-icon { display: none; } :host([aria-expanded="true"]) .base.with-menu { border-radius: var(--m3e-fab-menu-close-button-container-shape, ${j.shape.corner.full}); min-height: calc(var(--m3e-fab-menu-close-button-container-height, 56px) + ${j.density.calc(-3)}); } :host([aria-expanded="true"]) .base.with-menu .wrapper { padding-block-start: calc( var(--m3e-fab-menu-close-button-top-space, ${j.measurement.space200}) + ${j.density.calcHalf(-3)} ); padding-block-end: calc( var(--m3e-fab-menu-close-button-bottom-space, ${j.measurement.space200}) + ${j.density.calcHalf(-3)} ); padding-inline-start: calc( var(--m3e-fab-menu-close-button-leading-space, ${j.measurement.space200}) + ${j.density.calcHalf(-3)} ); padding-inline-end: calc( var(--m3e-fab-menu-close-button-trailing-space, ${j.measurement.space200}) + ${j.density.calcHalf(-3)} ); } :host([aria-expanded="true"]) .base.with-menu .icon { font-size: calc(var(--m3e-fab-menu-close-button-icon-size, 24px) + ${j.density.calc(-3)}); --m3e-icon-size: calc(var(--m3e-fab-menu-close-button-icon-size, 24px) + ${j.density.calc(-3)}); } .base.with-menu { transition: min-height ${j.motion.spring.fastEffects}; } .base.with-menu .wrapper { transition: padding ${j.motion.spring.fastEffects}; } a { all: unset; display: block; position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px; z-index: 1; } @media (forced-colors: active) { .base, .icon { transition: none; } .base { outline-style: solid; } :host(:not(:disabled):not([disabled-interactive])) .base { background-color: ButtonFace !important; outline-color: ButtonText !important; } :host(:not(:disabled):not([disabled-interactive])) .label, :host(:not(:disabled):not([disabled-interactive])) .icon { color: ButtonText !important; } :host(:disabled) .base, :host([disabled-interactive]) .base { outline-color: GrayText !important; background-color: unset !important; } :host(:disabled) .label, :host([disabled-interactive]) .label, :host(:disabled) .icon, :host([disabled-interactive]) .icon { color: GrayText !important; } :host(:is(:state(--small), :--small)) .base { outline-offset: calc(0px - var(--m3e-button-small-outline-thickness, 1px)); outline-width: var(--m3e-button-small-outline-thickness, 1px); } :host(:is(:state(--medium), :--medium)) .base { outline-offset: calc(0px - var(--m3e-button-medium-outline-thickness, 1px)); outline-width: var(--m3e-button-medium-outline-thickness, 1px); } :host(:is(:state(--large), :--large)) .base { outline-offset: calc(0px - var(--m3e-button-large-outline-thickness, 2px)); outline-width: var(--m3e-button-large-outline-thickness, 2px); } } @media (prefers-reduced-motion) { .base, .base.resting, .base.pressed, .label, .icon { transition: none; } }`, wl = {
	small: {
		containerHeight: c(`calc(var(--m3e-fab-small-container-height, var(--m3e-fab-container-height, 56px)) + ${j.density.calc(-3)})`),
		labelTextFontSize: c(`var(--m3e-fab-small-label-text-font-size, var(--m3e-fab-label-text-font-size, ${j.typescale.standard.title.medium.fontSize}))`),
		labelTextFontWeight: c(`var(--m3e-fab-small-label-text-font-weight, var(--m3e-fab-label-text-font-weight, ${j.typescale.standard.title.medium.fontWeight}))`),
		labelTextLineHeight: c(`var(--m3e-fab-small-label-text-line-height, var(--m3e-fab-label-text-line-height, ${j.typescale.standard.title.medium.lineHeight}))`),
		labelTextTracking: c(`var(--m3e-fab-small-label-text-tracking, var(--m3e-fab-label-text-tracking, ${j.typescale.standard.title.medium.tracking}))`),
		iconSize: c("var(--m3e-fab-small-icon-size, var(--m3e-fab-icon-size, 24px))"),
		extendedIconSize: c("var(--m3e-fab-small-icon-size, var(--m3e-fab-icon-size, 24px))"),
		shape: c(`var(--m3e-fab-small-shape, var(--m3e-fab-shape, ${j.shape.corner.large}))`),
		leadingSpace: c(`calc(var(--m3e-fab-small-leading-space, var(--m3e-fab-leading-space, ${j.measurement.space200})) + ${j.density.calcHalf(-3)})`),
		trailingSpace: c(`calc(var(--m3e-fab-small-trailing-space, var(--m3e-fab-trailing-space, ${j.measurement.space200})) + ${j.density.calcHalf(-3)})`),
		iconLabelSpace: c(`var(--m3e-fab-small-icon-label-space, var(--m3e-fab-icon-label-space, ${j.measurement.space100}))`),
		extendedLeadingSpace: c(`var(--m3e-fab-small-leading-space, var(--m3e-fab-leading-space, ${j.measurement.space200}))`),
		extendedTrailingSpace: c(`var(--m3e-fab-small-trailing-space, var(--m3e-fab-trailing-space, ${j.measurement.space200}))`)
	},
	medium: {
		containerHeight: c(`calc(var(--m3e-fab-medium-container-height, var(--m3e-fab-container-height, 80px)) + ${j.density.calc(-3)})`),
		labelTextFontSize: c(`var(--m3e-fab-medium-label-text-font-size, var(--m3e-fab-label-text-font-size, ${j.typescale.standard.title.large.fontSize}))`),
		labelTextFontWeight: c(`var(--m3e-fab-medium-label-text-font-weight, var(--m3e-fab-label-text-font-weight, ${j.typescale.standard.title.large.fontWeight}))`),
		labelTextLineHeight: c(`var(--m3e-fab-medium-label-text-line-height, var(--m3e-fab-label-text-line-height, ${j.typescale.standard.title.large.lineHeight}))`),
		labelTextTracking: c(`var(--m3e-fab-medium-label-text-tracking, var(--m3e-fab-label-text-tracking, ${j.typescale.standard.title.large.tracking}))`),
		iconSize: c(`var(--m3e-fab-medium-icon-size, var(--m3e-fab-icon-size, ${j.measurement.space350}))`),
		extendedIconSize: c(`var(--m3e-fab-medium-icon-size, var(--m3e-fab-icon-size, ${j.measurement.space350}))`),
		shape: c(`var(--m3e-fab-medium-shape, var(--m3e-fab-shape, ${j.shape.corner.largeIncreased}))`),
		leadingSpace: c(`calc(var(--m3e-fab-medium-leading-space, var(--m3e-fab-leading-space, ${j.measurement.space325})) + ${j.density.calcHalf(-3)})`),
		trailingSpace: c(`calc(var(--m3e-fab-medium-trailing-space, var(--m3e-fab-trailing-space, ${j.measurement.space325})) + ${j.density.calcHalf(-3)})`),
		iconLabelSpace: c(`var(--m3e-fab-medium-icon-label-space, var(--m3e-fab-icon-label-space, ${j.measurement.space150}))`),
		extendedLeadingSpace: c(`var(--m3e-fab-medium-leading-space, var(--m3e-fab-leading-space, ${j.measurement.space325}))`),
		extendedTrailingSpace: c(`var(--m3e-fab-medium-trailing-space, var(--m3e-fab-trailing-space, ${j.measurement.space325}))`)
	},
	large: {
		containerHeight: c(`calc(var(--m3e-fab-large-container-height, var(--m3e-fab-container-height, 96px)) + ${j.density.calc(-3)})`),
		labelTextFontSize: c(`var(--m3e-fab-large-label-text-font-size, var(--m3e-fab-label-text-font-size, ${j.typescale.standard.headline.small.fontSize}))`),
		labelTextFontWeight: c(`var(--m3e-fab-large-label-text-font-weight, var(--m3e-fab-label-text-font-weight, ${j.typescale.standard.headline.small.fontWeight}))`),
		labelTextLineHeight: c(`var(--m3e-fab-large-label-text-line-height, var(--m3e-fab-label-text-line-height, ${j.typescale.standard.headline.small.lineHeight}))`),
		labelTextTracking: c(`var(--m3e-fab-large-label-text-tracking, var(--m3e-fab-label-text-tracking, ${j.typescale.standard.headline.small.tracking}))`),
		iconSize: c("var(--m3e-fab-large-icon-size, var(--m3e-fab-icon-size, 36px))"),
		extendedIconSize: c("var(--m3e-fab-large-icon-size, var(--m3e-fab-icon-size, 36px))"),
		shape: c(`var(--m3e-fab-large-shape, var(--m3e-fab-shape, ${j.shape.corner.extraLarge}))`),
		leadingSpace: c(`calc(var(--m3e-fab-large-leading-space, var(--m3e-fab-leading-space, ${j.measurement.space350})) + ${j.density.calcHalf(-3)})`),
		trailingSpace: c(`calc(var(--m3e-fab-large-trailing-space, var(--m3e-fab-trailing-space, ${j.measurement.space350})) + ${j.density.calcHalf(-3)})`),
		iconLabelSpace: c(`var(--m3e-fab-large-icon-label-space, var(--m3e-fab-icon-label-space, ${j.measurement.space200}))`),
		extendedLeadingSpace: c(`var(--m3e-fab-large-leading-space, var(--m3e-fab-leading-space, ${j.measurement.space350}))`),
		extendedTrailingSpace: c(`var(--m3e-fab-large-trailing-space, var(--m3e-fab-trailing-space, ${j.measurement.space350}))`)
	}
};
function Tl(e) {
	let t = c(`:is(:state(--${e}), :--${e})`);
	return l`:host(${t}) .base { min-height: ${wl[e].containerHeight}; } :host(${t}) .base { border-radius: ${wl[e].shape}; } :host(${t}) .label { font-size: ${wl[e].labelTextFontSize}; font-weight: ${wl[e].labelTextFontWeight}; line-height: ${wl[e].labelTextLineHeight}; letter-spacing: ${wl[e].labelTextTracking}; } :host(${t}:not([extended])) .wrapper { padding-inline-start: ${wl[e].leadingSpace}; padding-inline-end: ${wl[e].trailingSpace}; } :host(${t}:not([extended])) .icon { font-size: ${wl[e].iconSize}; --m3e-icon-size: ${wl[e].iconSize}; } :host(${t}[extended]) .wrapper { padding-inline-start: ${wl[e].extendedLeadingSpace}; padding-inline-end: ${wl[e].extendedTrailingSpace}; column-gap: ${wl[e].iconLabelSpace}; } :host(${t}[extended]) .icon { font-size: ${wl[e].extendedIconSize}; --m3e-icon-size: ${wl[e].extendedIconSize}; }`;
}
var El = [
	Tl("small"),
	Tl("medium"),
	Tl("large")
], K = {
	primary: {
		labelTextColor: c(`var(--m3e-primary-fab-label-text-color, var(--m3e-fab-label-text-color, ${j.color.onPrimary}))`),
		iconColor: c(`var(--m3e-primary-fab-icon-color, var(--m3e-fab-icon-color, ${j.color.onPrimary}))`),
		containerColor: c(`var(--m3e-primary-fab-container-color, var(--m3e-fab-container-color, ${j.color.primary}))`),
		containerElevation: c(`var(--m3e-primary-fab-container-elevation, var(--m3e-fab-container-elevation, ${j.elevation.level3}))`),
		loweredContainerElevation: c(`var(--m3e-primary-fab-lowered-container-elevation, var(--m3e-fab-lowered-container-elevation, ${j.elevation.level2}))`),
		disabled: {
			containerColor: c(`var(--m3e-primary-fab-disabled-container-color, var(--m3e-fab-disabled-container-color, ${j.color.onSurface}))`),
			containerOpacity: c("var(--m3e-primary-fab-disabled-container-opacity, var(--m3e-fab-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-primary-fab-disabled-icon-color, var(--m3e-fab-disabled-icon-color, ${j.color.onSurface}))`),
			iconOpacity: c("var(--m3e-primary-fab-disabled-icon-opacity, var(--m3e-fab-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-primary-fab-disabled-label-text-color, var(--m3e-fab-disabled-label-text-color, ${j.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-primary-fab-disabled-label-text-opacity, var(--m3e-fab-disabled-label-text-opacity, 38%))"),
			containerElevation: c(`var(--m3e-primary-fab-disabled-container-elevation, var(--m3e-fab-disabled-container-elevation, ${j.elevation.level0}))`),
			loweredContainerElevation: c(`var(--m3e-primary-fab-lowered-disabled-container-elevation, var(--m3e-fab-lowered-disabled-container-elevation, ${j.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-primary-fab-hover-icon-color, var(--m3e-fab-hover-icon-color, ${j.color.onPrimary}))`),
			labelTextColor: c(`var(--m3e-primary-fab-hover-label-text-color, var(--m3e-fab-hover-label-text-color, ${j.color.onPrimary}))`),
			stateLayerColor: c(`var(--m3e-primary-fab-hover-state-layer-color, var(--m3e-fab-hover-state-layer-color, ${j.color.onPrimary}))`),
			stateLayerOpacity: c(`var(--m3e-primary-fab-hover-state-layer-opacity, var(--m3e-fab-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-primary-fab-hover-container-elevation, var(--m3e-fab-hover-container-elevation, ${j.elevation.level4}))`),
			loweredContainerElevation: c(`var(--m3e-primary-fab-lowered-hover-container-elevation, var(--m3e-fab-lowered-hover-container-elevation, ${j.elevation.level3}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-primary-fab-focus-icon-color, var(--m3e-fab-focus-icon-color, ${j.color.onPrimary}))`),
			labelTextColor: c(`var(--m3e-primary-fab-focus-label-text-color, var(--m3e-fab-focus-label-text-color, ${j.color.onPrimary}))`),
			stateLayerColor: c(`var(--m3e-primary-fab-focus-state-layer-color, var(--m3e-fab-focus-state-layer-color, ${j.color.onPrimary}))`),
			stateLayerOpacity: c(`var(--m3e-primary-fab-focus-state-layer-opacity, var(--m3e-fab-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-primary-fab-focus-container-elevation, var(--m3e-fab-focus-container-elevation, ${j.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-primary-fab-lowered-focus-container-elevation, var(--m3e-fab-lowered-focus-container-elevation, ${j.elevation.level2}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-primary-fab-pressed-icon-color, var(--m3e-fab-pressed-icon-color, ${j.color.onPrimary}))`),
			labelTextColor: c(`var(--m3e-primary-fab-pressed-label-text-color, var(--m3e-fab-pressed-label-text-color, ${j.color.onPrimary}))`),
			stateLayerColor: c(`var(--m3e-primary-fab-pressed-state-layer-color, var(--m3e-fab-pressed-state-layer-color, ${j.color.onPrimary}))`),
			stateLayerOpacity: c(`var(--m3e-primary-fab-pressed-state-layer-opacity, var(--m3e-fab-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-primary-fab-pressed-container-elevation, var(--m3e-fab-pressed-container-elevation, ${j.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-primary-fab-lowered-pressed-container-elevation, var(--m3e-fab-lowered-pressed-container-elevation, ${j.elevation.level2}))`)
		}
	},
	secondary: {
		labelTextColor: c(`var(--m3e-secondary-fab-label-text-color, var(--m3e-fab-label-text-color, ${j.color.onSecondary}))`),
		iconColor: c(`var(--m3e-secondary-fab-icon-color, var(--m3e-fab-icon-color, ${j.color.onSecondary}))`),
		containerColor: c(`var(--m3e-secondary-fab-container-color, var(--m3e-fab-container-color, ${j.color.secondary}))`),
		containerElevation: c(`var(--m3e-secondary-fab-container-elevation, var(--m3e-fab-container-elevation, ${j.elevation.level3}))`),
		loweredContainerElevation: c(`var(--m3e-secondary-fab-lowered-container-elevation, var(--m3e-fab-lowered-container-elevation, ${j.elevation.level2}))`),
		disabled: {
			containerColor: c(`var(--m3e-secondary-fab-disabled-container-color, var(--m3e-fab-disabled-container-color, ${j.color.onSurface}))`),
			containerOpacity: c("var(--m3e-secondary-fab-disabled-container-opacity, var(--m3e-fab-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-secondary-fab-disabled-icon-color, var(--m3e-fab-disabled-icon-color, ${j.color.onSurface}))`),
			iconOpacity: c("var(--m3e-secondary-fab-disabled-icon-opacity, var(--m3e-fab-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-secondary-fab-disabled-label-text-color, var(--m3e-fab-disabled-label-text-color, ${j.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-secondary-fab-disabled-label-text-opacity, var(--m3e-fab-disabled-label-text-opacity, 38%))"),
			containerElevation: c(`var(--m3e-secondary-fab-disabled-container-elevation, var(--m3e-fab-disabled-container-elevation, ${j.elevation.level0}))`),
			loweredContainerElevation: c(`var(--m3e-secondary-fab-lowered-disabled-container-elevation, var(--m3e-fab-lowered-disabled-container-elevation, ${j.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-secondary-fab-hover-icon-color, var(--m3e-fab-hover-icon-color, ${j.color.onSecondary}))`),
			labelTextColor: c(`var(--m3e-secondary-fab-hover-label-text-color, var(--m3e-fab-hover-label-text-color, ${j.color.onSecondary}))`),
			stateLayerColor: c(`var(--m3e-secondary-fab-hover-state-layer-color, var(--m3e-fab-hover-state-layer-color, ${j.color.onSecondary}))`),
			stateLayerOpacity: c(`var(--m3e-secondary-fab-hover-state-layer-opacity, var(--m3e-fab-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-secondary-fab-hover-container-elevation, var(--m3e-fab-hover-container-elevation, ${j.elevation.level4}))`),
			loweredContainerElevation: c(`var(--m3e-secondary-fab-lowered-hover-container-elevation, var(--m3e-fab-lowered-hover-container-elevation, ${j.elevation.level3}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-secondary-fab-focus-icon-color, var(--m3e-fab-focus-icon-color, ${j.color.onSecondary}))`),
			labelTextColor: c(`var(--m3e-secondary-fab-focus-label-text-color, var(--m3e-fab-focus-label-text-color, ${j.color.onSecondary}))`),
			stateLayerColor: c(`var(--m3e-secondary-fab-focus-state-layer-color, var(--m3e-fab-focus-state-layer-color, ${j.color.onSecondary}))`),
			stateLayerOpacity: c(`var(--m3e-secondary-fab-focus-state-layer-opacity, var(--m3e-fab-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-secondary-fab-focus-container-elevation, var(--m3e-fab-focus-container-elevation, ${j.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-secondary-fab-lowered-focus-container-elevation, var(--m3e-fab-lowered-focus-container-elevation, ${j.elevation.level2}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-secondary-fab-pressed-icon-color, var(--m3e-fab-pressed-icon-color, ${j.color.onSecondary}))`),
			labelTextColor: c(`var(--m3e-secondary-fab-pressed-label-text-color, var(--m3e-fab-pressed-label-text-color, ${j.color.onSecondary}))`),
			stateLayerColor: c(`var(--m3e-secondary-fab-pressed-state-layer-color, var(--m3e-fab-pressed-state-layer-color, ${j.color.onSecondary}))`),
			stateLayerOpacity: c(`var(--m3e-secondary-fab-pressed-state-layer-opacity, var(--m3e-fab-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-secondary-fab-pressed-container-elevation, var(--m3e-fab-pressed-container-elevation, ${j.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-secondary-fab-lowered-pressed-container-elevation, var(--m3e-fab-lowered-pressed-container-elevation, ${j.elevation.level2}))`)
		}
	},
	tertiary: {
		labelTextColor: c(`var(--m3e-tertiary-fab-label-text-color, var(--m3e-fab-label-text-color, ${j.color.onTertiary}))`),
		iconColor: c(`var(--m3e-tertiary-fab-icon-color, var(--m3e-fab-icon-color, ${j.color.onTertiary}))`),
		containerColor: c(`var(--m3e-tertiary-fab-container-color, var(--m3e-fab-container-color, ${j.color.tertiary}))`),
		containerElevation: c(`var(--m3e-tertiary-fab-container-elevation, var(--m3e-fab-container-elevation, ${j.elevation.level3}))`),
		loweredContainerElevation: c(`var(--m3e-tertiary-fab-lowered-container-elevation, var(--m3e-fab-lowered-container-elevation, ${j.elevation.level2}))`),
		disabled: {
			containerColor: c(`var(--m3e-tertiary-fab-disabled-container-color, var(--m3e-fab-disabled-container-color, ${j.color.onSurface}))`),
			containerOpacity: c("var(--m3e-tertiary-fab-disabled-container-opacity, var(--m3e-fab-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-tertiary-fab-disabled-icon-color, var(--m3e-fab-disabled-icon-color, ${j.color.onSurface}))`),
			iconOpacity: c("var(--m3e-tertiary-fab-disabled-icon-opacity, var(--m3e-fab-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-tertiary-fab-disabled-label-text-color, var(--m3e-fab-disabled-label-text-color, ${j.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-tertiary-fab-disabled-label-text-opacity, var(--m3e-fab-disabled-label-text-opacity, 38%))"),
			containerElevation: c(`var(--m3e-tertiary-fab-disabled-container-elevation, var(--m3e-fab-disabled-container-elevation, ${j.elevation.level0}))`),
			loweredContainerElevation: c(`var(--m3e-tertiary-fab-lowered-disabled-container-elevation, var(--m3e-fab-lowered-disabled-container-elevation, ${j.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-tertiary-fab-hover-icon-color, var(--m3e-fab-hover-icon-color, ${j.color.onTertiary}))`),
			labelTextColor: c(`var(--m3e-tertiary-fab-hover-label-text-color, var(--m3e-fab-hover-label-text-color, ${j.color.onTertiary}))`),
			stateLayerColor: c(`var(--m3e-tertiary-fab-hover-state-layer-color, var(--m3e-fab-hover-state-layer-color, ${j.color.onTertiary}))`),
			stateLayerOpacity: c(`var(--m3e-tertiary-fab-hover-state-layer-opacity, var(--m3e-fab-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-tertiary-fab-hover-container-elevation, var(--m3e-fab-hover-container-elevation, ${j.elevation.level4}))`),
			loweredContainerElevation: c(`var(--m3e-tertiary-fab-lowered-hover-container-elevation, var(--m3e-fab-lowered-hover-container-elevation, ${j.elevation.level3}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-tertiary-fab-focus-icon-color, var(--m3e-fab-focus-icon-color, ${j.color.onTertiary}))`),
			labelTextColor: c(`var(--m3e-tertiary-fab-focus-label-text-color, var(--m3e-fab-focus-label-text-color, ${j.color.onTertiary}))`),
			stateLayerColor: c(`var(--m3e-tertiary-fab-focus-state-layer-color, var(--m3e-fab-focus-state-layer-color, ${j.color.onTertiary}))`),
			stateLayerOpacity: c(`var(--m3e-tertiary-fab-focus-state-layer-opacity, var(--m3e-fab-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-tertiary-fab-focus-container-elevation, var(--m3e-fab-focus-container-elevation, ${j.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-tertiary-fab-lowered-focus-container-elevation, var(--m3e-fab-lowered-focus-container-elevation, ${j.elevation.level2}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-tertiary-fab-pressed-icon-color, var(--m3e-fab-pressed-icon-color, ${j.color.onTertiary}))`),
			labelTextColor: c(`var(--m3e-tertiary-fab-pressed-label-text-color, var(--m3e-fab-pressed-label-text-color, ${j.color.onTertiary}))`),
			stateLayerColor: c(`var(--m3e-tertiary-fab-pressed-state-layer-color, var(--m3e-fab-pressed-state-layer-color, ${j.color.onTertiary}))`),
			stateLayerOpacity: c(`var(--m3e-tertiary-fab-pressed-state-layer-opacity, var(--m3e-fab-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-tertiary-fab-pressed-container-elevation, var(--m3e-fab-pressed-container-elevation, ${j.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-tertiary-fab-lowered-pressed-container-elevation, var(--m3e-fab-lowered-pressed-container-elevation, ${j.elevation.level2}))`)
		}
	},
	"primary-container": {
		labelTextColor: c(`var(--m3e-primary-container-fab-label-text-color, var(--m3e-primary-container-fab-label-text-color, ${j.color.onPrimaryContainer}))`),
		iconColor: c(`var(--m3e-primary-container-fab-icon-color, var(--m3e-primary-container-fab-icon-color, ${j.color.onPrimaryContainer}))`),
		containerColor: c(`var(--m3e-primary-container-fab-container-color, var(--m3e-fab-container-color, ${j.color.primaryContainer}))`),
		containerElevation: c(`var(--m3e-primary-container-fab-container-elevation, var(--m3e-fab-container-elevation, ${j.elevation.level3}))`),
		loweredContainerElevation: c(`var(--m3e-primary-container-fab-lowered-container-elevation, var(--m3e-fab-lowered-container-elevation, ${j.elevation.level2}))`),
		disabled: {
			containerColor: c(`var(--m3e-primary-container-fab-disabled-container-color, var(--m3e-fab-disabled-container-color, ${j.color.onSurface}))`),
			containerOpacity: c("var(--m3e-primary-container-fab-disabled-container-opacity, var(--m3e-fab-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-primary-container-fab-disabled-icon-color, var(--m3e-fab-disabled-icon-color, ${j.color.onSurface}))`),
			iconOpacity: c("var(--m3e-primary-container-fab-disabled-icon-opacity, var(--m3e-fab-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-primary-container-fab-disabled-label-text-color, var(--m3e-fab-disabled-label-text-color, ${j.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-primary-container-fab-disabled-label-text-opacity, var(--m3e-fab-disabled-label-text-opacity, 38%))"),
			containerElevation: c(`var(--m3e-primary-container-fab-disabled-container-elevation, var(--m3e-fab-disabled-container-elevation, ${j.elevation.level0}))`),
			loweredContainerElevation: c(`var(--m3e-primary-container-fab-lowered-disabled-container-elevation, var(--m3e-fab-lowered-disabled-container-elevation, ${j.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-primary-container-fab-hover-icon-color, var(--m3e-fab-hover-icon-color, ${j.color.onPrimaryContainer}))`),
			labelTextColor: c(`var(--m3e-primary-container-fab-hover-label-text-color, var(--m3e-fab-hover-label-text-color, ${j.color.onPrimaryContainer}))`),
			stateLayerColor: c(`var(--m3e-primary-container-fab-hover-state-layer-color, var(--m3e-fab-hover-state-layer-color, ${j.color.onPrimaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-primary-container-fab-hover-state-layer-opacity, var(--m3e-fab-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-primary-container-fab-hover-container-elevation, var(--m3e-fab-hover-container-elevation, ${j.elevation.level4}))`),
			loweredContainerElevation: c(`var(--m3e-primary-container-fab-lowered-hover-container-elevation, var(--m3e-fab-lowered-hover-container-elevation, ${j.elevation.level3}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-primary-container-fab-focus-icon-color, var(--m3e-fab-focus-icon-color, ${j.color.onPrimaryContainer}))`),
			labelTextColor: c(`var(--m3e-primary-container-fab-focus-label-text-color, var(--m3e-fab-focus-label-text-color, ${j.color.onPrimaryContainer}))`),
			stateLayerColor: c(`var(--m3e-primary-container-fab-focus-state-layer-color, var(--m3e-fab-focus-state-layer-color, ${j.color.onPrimaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-primary-container-fab-focus-state-layer-opacity, var(--m3e-fab-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-primary-container-fab-focus-container-elevation, var(--m3e-fab-focus-container-elevation, ${j.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-primary-container-fab-lowered-focus-container-elevation, var(--m3e-fab-lowered-focus-container-elevation, ${j.elevation.level2}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-primary-container-fab-pressed-icon-color, var(--m3e-fab-pressed-icon-color, ${j.color.onPrimaryContainer}))`),
			labelTextColor: c(`var(--m3e-primary-container-fab-pressed-label-text-color, var(--m3e-fab-pressed-label-text-color, ${j.color.onPrimaryContainer}))`),
			stateLayerColor: c(`var(--m3e-primary-container-fab-pressed-state-layer-color, var(--m3e-fab-pressed-state-layer-color, ${j.color.onPrimaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-primary-container-fab-pressed-state-layer-opacity, var(--m3e-fab-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-primary-container-fab-pressed-container-elevation, var(--m3e-fab-pressed-container-elevation, ${j.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-primary-container-fab-lowered-pressed-container-elevation, var(--m3e-fab-lowered-pressed-container-elevation, ${j.elevation.level2}))`)
		}
	},
	"secondary-container": {
		labelTextColor: c(`var(--m3e-secondary-container-fab-label-text-color, var(--m3e-fab-label-text-color, ${j.color.onSecondaryContainer}))`),
		iconColor: c(`var(--m3e-secondary-container-fab-icon-color, var(--m3e-fab-icon-color, ${j.color.onSecondaryContainer}))`),
		containerColor: c(`var(--m3e-secondary-container-fab-container-color, var(--m3e-fab-container-color, ${j.color.secondaryContainer}))`),
		containerElevation: c(`var(--m3e-secondary-container-fab-container-elevation, var(--m3e-fab-container-elevation, ${j.elevation.level3}))`),
		loweredContainerElevation: c(`var(--m3e-secondary-container-fab-lowered-container-elevation, var(--m3e-fab-lowered-container-elevation, ${j.elevation.level2}))`),
		disabled: {
			containerColor: c(`var(--m3e-secondary-container-fab-disabled-container-color, var(--m3e-fab-disabled-container-color, ${j.color.onSurface}))`),
			containerOpacity: c("var(--m3e-secondary-container-fab-disabled-container-opacity, var(--m3e-fab-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-secondary-container-fab-disabled-icon-color, var(--m3e-fab-disabled-icon-color, ${j.color.onSurface}))`),
			iconOpacity: c("var(--m3e-secondary-container-fab-disabled-icon-opacity, var(--m3e-fab-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-secondary-container-fab-disabled-label-text-color, var(--m3e-fab-disabled-label-text-color, ${j.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-secondary-container-fab-disabled-label-text-opacity, var(--m3e-fab-disabled-label-text-opacity, 38%))"),
			containerElevation: c(`var(--m3e-secondary-container-fab-disabled-container-elevation, var(--m3e-fab-disabled-container-elevation, ${j.elevation.level0}))`),
			loweredContainerElevation: c(`var(--m3e-secondary-container-fab-lowered-disabled-container-elevation, var(--m3e-fab-lowered-disabled-container-elevation, ${j.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-secondary-container-fab-hover-icon-color, var(--m3e-fab-hover-icon-color, ${j.color.onSecondaryContainer}))`),
			labelTextColor: c(`var(--m3e-secondary-container-fab-hover-label-text-color, var(--m3e-fab-hover-label-text-color, ${j.color.onSecondaryContainer}))`),
			stateLayerColor: c(`var(--m3e-secondary-container-fab-hover-state-layer-color, var(--m3e-fab-hover-state-layer-color, ${j.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-secondary-container-fab-hover-state-layer-opacity, var(--m3e-fab-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-secondary-container-fab-hover-container-elevation, var(--m3e-fab-hover-container-elevation, ${j.elevation.level4}))`),
			loweredContainerElevation: c(`var(--m3e-secondary-container-fab-lowered-hover-container-elevation, var(--m3e-fab-lowered-hover-container-elevation, ${j.elevation.level3}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-secondary-container-fab-focus-icon-color, var(--m3e-fab-focus-icon-color, ${j.color.onSecondaryContainer}))`),
			labelTextColor: c(`var(--m3e-secondary-container-fab-focus-label-text-color, var(--m3e-fab-focus-label-text-color, ${j.color.onSecondaryContainer}))`),
			stateLayerColor: c(`var(--m3e-secondary-container-fab-focus-state-layer-color, var(--m3e-fab-focus-state-layer-color, ${j.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-secondary-container-fab-focus-state-layer-opacity, var(--m3e-fab-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-secondary-container-fab-focus-container-elevation, var(--m3e-fab-focus-container-elevation, ${j.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-secondary-container-fab-lowered-focus-container-elevation, var(--m3e-fab-lowered-focus-container-elevation, ${j.elevation.level2}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-secondary-container-fab-pressed-icon-color, var(--m3e-fab-pressed-icon-color, ${j.color.onSecondaryContainer}))`),
			labelTextColor: c(`var(--m3e-secondary-container-fab-pressed-label-text-color, var(--m3e-fab-pressed-label-text-color, ${j.color.onSecondaryContainer}))`),
			stateLayerColor: c(`var(--m3e-secondary-container-fab-pressed-state-layer-color, var(--m3e-fab-pressed-state-layer-color, ${j.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-secondary-container-fab-pressed-state-layer-opacity, var(--m3e-fab-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-secondary-container-fab-pressed-container-elevation, var(--m3e-fab-pressed-container-elevation, ${j.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-secondary-container-fab-lowered-pressed-container-elevation, var(--m3e-fab-lowered-pressed-container-elevation, ${j.elevation.level2}))`)
		}
	},
	"tertiary-container": {
		labelTextColor: c(`var(--m3e-tertiary-container-fab-label-text-color, var(--m3e-fab-label-text-color, ${j.color.onTertiaryContainer}))`),
		iconColor: c(`var(--m3e-tertiary-container-fab-icon-color, var(--m3e-fab-icon-color, ${j.color.onTertiaryContainer}))`),
		containerColor: c(`var(--m3e-tertiary-container-fab-container-color, var(--m3e-fab-container-color, ${j.color.tertiaryContainer}))`),
		containerElevation: c(`var(--m3e-tertiary-container-fab-container-elevation, var(--m3e-fab-container-elevation, ${j.elevation.level3}))`),
		loweredContainerElevation: c(`var(--m3e-tertiary-container-fab-lowered-container-elevation, var(--m3e-fab-lowered-container-elevation, ${j.elevation.level2}))`),
		disabled: {
			containerColor: c(`var(--m3e-tertiary-container-fab-disabled-container-color, var(--m3e-fab-disabled-container-color, ${j.color.onSurface}))`),
			containerOpacity: c("var(--m3e-tertiary-container-fab-disabled-container-opacity, var(--m3e-fab-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-tertiary-container-fab-disabled-icon-color, var(--m3e-fab-disabled-icon-color, ${j.color.onSurface}))`),
			iconOpacity: c("var(--m3e-tertiary-container-fab-disabled-icon-opacity, var(--m3e-fab-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-tertiary-container-fab-disabled-label-text-color, var(--m3e-fab-disabled-label-text-color, ${j.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-tertiary-container-fab-disabled-label-text-opacity, var(--m3e-fab-disabled-label-text-opacity, 38%))"),
			containerElevation: c(`var(--m3e-tertiary-container-fab-disabled-container-elevation, var(--m3e-fab-disabled-container-elevation, ${j.elevation.level0}))`),
			loweredContainerElevation: c(`var(--m3e-tertiary-container-fab-lowered-disabled-container-elevation, var(--m3e-fab-lowered-disabled-container-elevation, ${j.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-tertiary-container-fab-hover-icon-color, var(--m3e-fab-hover-icon-color, ${j.color.onTertiaryContainer}))`),
			labelTextColor: c(`var(--m3e-tertiary-container-fab-hover-label-text-color, var(--m3e-fab-hover-label-text-color, ${j.color.onTertiaryContainer}))`),
			stateLayerColor: c(`var(--m3e-tertiary-container-fab-hover-state-layer-color, var(--m3e-fab-hover-state-layer-color, ${j.color.onTertiaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-tertiary-container-fab-hover-state-layer-opacity, var(--m3e-fab-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-tertiary-container-fab-hover-container-elevation, var(--m3e-fab-hover-container-elevation, ${j.elevation.level4}))`),
			loweredContainerElevation: c(`var(--m3e-tertiary-container-fab-lowered-hover-container-elevation, var(--m3e-fab-lowered-hover-container-elevation, ${j.elevation.level3}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-tertiary-container-fab-focus-icon-color, var(--m3e-fab-focus-icon-color, ${j.color.onTertiaryContainer}))`),
			labelTextColor: c(`var(--m3e-tertiary-container-fab-focus-label-text-color, var(--m3e-fab-focus-label-text-color, ${j.color.onTertiaryContainer}))`),
			stateLayerColor: c(`var(--m3e-tertiary-container-fab-focus-state-layer-color, var(--m3e-fab-focus-state-layer-color, ${j.color.onTertiaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-tertiary-container-fab-focus-state-layer-opacity, var(--m3e-fab-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-tertiary-container-fab-focus-container-elevation, var(--m3e-fab-focus-container-elevation, ${j.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-tertiary-container-fab-lowered-focus-container-elevation, var(--m3e-fab-lowered-focus-container-elevation, ${j.elevation.level2}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-tertiary-container-fab-pressed-icon-color, var(--m3e-fab-pressed-icon-color, ${j.color.onTertiaryContainer}))`),
			labelTextColor: c(`var(--m3e-tertiary-container-fab-pressed-label-text-color, var(--m3e-fab-pressed-label-text-color, ${j.color.onTertiaryContainer}))`),
			stateLayerColor: c(`var(--m3e-tertiary-container-fab-pressed-state-layer-color, var(--m3e-fab-pressed-state-layer-color, ${j.color.onTertiaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-tertiary-container-fab-pressed-state-layer-opacity, var(--m3e-fab-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-tertiary-container-fab-pressed-container-elevation, var(--m3e-fab-pressed-container-elevation, ${j.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-tertiary-container-fab-lowered-pressed-container-elevation, var(--m3e-fab-lowered-pressed-container-elevation, ${j.elevation.level2}))`)
		}
	},
	surface: {
		labelTextColor: c(`var(--m3e-surface-fab-label-text-color, var(--m3e-fab-label-text-color, ${j.color.primary}))`),
		iconColor: c(`var(--m3e-surface-fab-icon-color, var(--m3e-fab-icon-color, ${j.color.primary}))`),
		containerColor: c(`var(--m3e-surface-fab-container-color, var(--m3e-fab-container-color, ${j.color.surfaceContainerHigh}))`),
		containerElevation: c(`var(--m3e-surface-fab-container-elevation, var(--m3e-fab-container-elevation, ${j.elevation.level3}))`),
		loweredContainerElevation: c(`var(--m3e-surface-fab-lowered-container-elevation, var(--m3e-fab-lowered-container-elevation, ${j.elevation.level2}))`),
		loweredContainerColor: c(`var(--m3e-surface-fab-lowered-container-color, var(--m3e-fab-lowered-container-color, ${j.color.surfaceContainerLow}))`),
		disabled: {
			containerColor: c(`var(--m3e-surface-fab-disabled-container-color, var(--m3e-fab-disabled-container-color, ${j.color.onSurface}))`),
			containerOpacity: c("var(--m3e-surface-fab-disabled-container-opacity, var(--m3e-fab-disabled-container-opacity, 10%))"),
			iconColor: c(`var(--m3e-surface-fab-disabled-icon-color, var(--m3e-fab-disabled-icon-color, ${j.color.onSurface}))`),
			iconOpacity: c("var(--m3e-surface-fab-disabled-icon-opacity, var(--m3e-fab-disabled-icon-opacity, 38%))"),
			labelTextColor: c(`var(--m3e-surface-fab-disabled-label-text-color, var(--m3e-fab-disabled-label-text-color, ${j.color.onSurface}))`),
			labelTextOpacity: c("var(--m3e-surface-fab-disabled-label-text-opacity, var(--m3e-fab-disabled-label-text-opacity, 38%))"),
			containerElevation: c(`var(--m3e-surface-fab-disabled-container-elevation, var(--m3e-fab-disabled-container-elevation, ${j.elevation.level0}))`),
			loweredContainerElevation: c(`var(--m3e-surface-fab-lowered-disabled-container-elevation, var(--m3e-fab-lowered-disabled-container-elevation, ${j.elevation.level0}))`)
		},
		hover: {
			iconColor: c(`var(--m3e-surface-fab-hover-icon-color, var(--m3e-fab-hover-icon-color, ${j.color.primary}))`),
			labelTextColor: c(`var(--m3e-surface-fab-hover-label-text-color, var(--m3e-fab-hover-label-text-color, ${j.color.primary}))`),
			stateLayerColor: c(`var(--m3e-surface-fab-hover-state-layer-color, var(--m3e-fab-hover-state-layer-color, ${j.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-surface-fab-hover-state-layer-opacity, var(--m3e-fab-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-surface-fab-hover-container-elevation, var(--m3e-fab-hover-container-elevation, ${j.elevation.level4}))`),
			loweredContainerElevation: c(`var(--m3e-surface-fab-lowered-hover-container-elevation, var(--m3e-fab-lowered-hover-container-elevation, ${j.elevation.level3}))`)
		},
		focus: {
			iconColor: c(`var(--m3e-surface-fab-focus-icon-color, var(--m3e-fab-focus-icon-color, ${j.color.primary}))`),
			labelTextColor: c(`var(--m3e-surface-fab-focus-label-text-color, var(--m3e-fab-focus-label-text-color, ${j.color.primary}))`),
			stateLayerColor: c(`var(--m3e-surface-fab-focus-state-layer-color, var(--m3e-fab-focus-state-layer-color, ${j.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-surface-fab-focus-state-layer-opacity, var(--m3e-fab-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-surface-fab-focus-container-elevation, var(--m3e-fab-focus-container-elevation, ${j.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-surface-fab-lowered-focus-container-elevation, var(--m3e-fab-lowered-focus-container-elevation, ${j.elevation.level2}))`)
		},
		pressed: {
			iconColor: c(`var(--m3e-surface-fab-pressed-icon-color, var(--m3e-fab-pressed-icon-color, ${j.color.primary}))`),
			labelTextColor: c(`var(--m3e-surface-fab-pressed-label-text-color, var(--m3e-fab-pressed-label-text-color, ${j.color.primary}))`),
			stateLayerColor: c(`var(--m3e-surface-fab-pressed-state-layer-color, var(--m3e-fab-pressed-state-layer-color, ${j.color.primary}))`),
			stateLayerOpacity: c(`var(--m3e-surface-fab-pressed-state-layer-opacity, var(--m3e-fab-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-surface-fab-pressed-container-elevation, var(--m3e-fab-pressed-container-elevation, ${j.elevation.level3}))`),
			loweredContainerElevation: c(`var(--m3e-surface-fab-lowered-pressed-container-elevation, var(--m3e-fab-lowered-pressed-container-elevation, ${j.elevation.level2}))`)
		}
	}
};
function Dl(e) {
	let t = c(`:is(:state(--${e}), :--${e})`);
	return l`:host(${t}:not([lowered])) .base { background-color: ${K[e].containerColor}; --m3e-elevation-level: ${K[e].containerElevation}; --m3e-elevation-hover-level: ${K[e].hover.containerElevation}; --m3e-elevation-focus-level: ${K[e].focus.containerElevation}; --m3e-elevation-pressed-level: ${K[e].pressed.containerElevation}; } :host(${t}[lowered]) .base { background-color: ${K[e].loweredContainerColor ?? K[e].containerColor}; --m3e-elevation-level: ${K[e].loweredContainerElevation}; --m3e-elevation-hover-level: ${K[e].hover.loweredContainerElevation}; --m3e-elevation-focus-level: ${K[e].focus.loweredContainerElevation}; --m3e-elevation-pressed-level: ${K[e].pressed.loweredContainerElevation}; } :host(${t}) .base { --m3e-state-layer-hover-color: ${K[e].hover.stateLayerColor}; --m3e-state-layer-hover-opacity: ${K[e].hover.stateLayerOpacity}; --m3e-state-layer-focus-color: ${K[e].focus.stateLayerColor}; --m3e-state-layer-focus-opacity: ${K[e].focus.stateLayerOpacity}; --m3e-ripple-color: ${K[e].pressed.stateLayerColor}; --m3e-ripple-opacity: ${K[e].pressed.stateLayerOpacity}; } :host(${t}) .label { color: ${K[e].labelTextColor}; } :host(${t}:focus) .label { color: ${K[e].focus.labelTextColor}; } :host(${t}:hover) .label { color: ${K[e].hover.labelTextColor}; } :host(${t}) .base.pressed .label { color: ${K[e].pressed.labelTextColor}; } :host(${t}) .icon { color: ${K[e].iconColor}; } :host(${t}:focus) .icon { color: ${K[e].focus.iconColor}; } :host(${t}:hover) .icon { color: ${K[e].hover.iconColor}; } :host(${t}) .base.pressed .icon { color: ${K[e].pressed.iconColor}; } :host(${t}:disabled) .base, :host(${t}[disabled-interactive]) .base { --m3e-elevation-level: ${K[e].disabled.containerElevation}; background-color: color-mix( in srgb, ${K[e].disabled.containerColor} ${K[e].disabled.containerOpacity}, transparent ); } :host(${t}:disabled) .label, :host(${t}[disabled-interactive]) .label { color: color-mix( in srgb, ${K[e].disabled.labelTextColor} ${K[e].disabled.labelTextOpacity}, transparent ); } :host(${t}:disabled) .icon, :host(${t}[disabled-interactive]) .icon { color: color-mix( in srgb, ${K[e].disabled.iconColor} ${K[e].disabled.iconOpacity}, transparent ); }`;
}
var Ol = [
	Dl("primary"),
	Dl("secondary"),
	Dl("tertiary"),
	Dl("primary-container"),
	Dl("secondary-container"),
	Dl("tertiary-container"),
	Dl("surface")
], kl, Al, jl, Ml, Nl = class extends ji(Fi(Di(Ti(si(ni(E(M(S, "button"), !0))))))) {
	constructor() {
		super(), kl.add(this), this.variant = "primary-container", this.lowered = !1, this.size = "medium", this.extended = !1, new Yn(this, {
			isPressedKey: (e) => e === " ",
			callback: (e) => {
				!this.disabled && !this.disabledInteractive && (this._base?.classList.toggle("pressed", e), this._base?.classList.toggle("resting", !e));
			}
		});
	}
	connectedCallback() {
		super.connectedCallback(), t(this, kl, "m", Al).call(this), t(this, kl, "m", jl).call(this);
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this._base?.classList.toggle("pressed", !1), this._base?.classList.toggle("resting", !1);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, kl, "m", Al).call(this), e.has("size") && t(this, kl, "m", jl).call(this);
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
		return b`<div class="base"><m3e-elevation class="elevation" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-elevation><m3e-state-layer class="state-layer" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-state-layer><m3e-focus-ring class="focus-ring" ?disabled="${this.disabled}"></m3e-focus-ring><m3e-ripple class="ripple" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-ripple><div class="touch" aria-hidden="true"></div>${this[Mi]()}<div class="wrapper"><slot class="icon" aria-hidden="true" @slotchange="${t(this, kl, "m", Ml)}"></slot><slot class="icon" aria-hidden="true" name="close-icon"><svg class="close-icon" viewBox="0 -960 960 960" fill="currentColor"><path d="m256-200-56-56 224-224-224-224 56-56 224 224 224-224 56 56-224 224 224 224-56 56-224-224-224 224Z"/></svg></slot><m3e-collapsible class="label-wrapper" orientation="horizontal" ?open="${this.extended}"><div class="label"><slot name="label" @slotchange="${t(this, kl, "m", Ml)}"></slot></div></m3e-collapsible></div></div>`;
	}
};
kl = /* @__PURE__ */ new WeakSet(), Al = function() {
	Sl(this.variant) || (this.variant = "primary-container"), wr(this, this.variant, "primary", "primary-container", "secondary", "secondary-container", "surface", "tertiary", "tertiary-container");
}, jl = function() {
	xl(this.size) || (this.size = "medium"), wr(this, this.size, "large", "medium", "small");
}, Ml = function() {
	this._base?.classList.toggle("with-menu", this.querySelector("m3e-fab-menu-trigger") !== null);
}, Nl.styles = [
	El,
	Ol,
	Cl
], e([w(".base")], Nl.prototype, "_base", void 0), e([w(".elevation")], Nl.prototype, "_elevation", void 0), e([w(".focus-ring")], Nl.prototype, "_focusRing", void 0), e([w(".state-layer")], Nl.prototype, "_stateLayer", void 0), e([w(".ripple")], Nl.prototype, "_ripple", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], Nl.prototype, "variant", void 0), e([C({
	type: Boolean,
	reflect: !0
})], Nl.prototype, "lowered", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], Nl.prototype, "size", void 0), e([C({
	type: Boolean,
	reflect: !0
})], Nl.prototype, "extended", void 0), Nl = e([T("m3e-fab")], Nl);
//#endregion
//#region node_modules/@m3e/web/dist/core-a11y.js
var Pl = [
	"alt",
	"shift",
	"ctrl",
	"meta"
], Fl;
(function(e) {
	e[e.Backspace = 8] = "Backspace", e[e.Tab = 9] = "Tab", e[e.Enter = 13] = "Enter", e[e.Shift = 16] = "Shift", e[e.Ctrl = 17] = "Ctrl", e[e.Alt = 18] = "Alt", e[e.PauseBreak = 19] = "PauseBreak", e[e.CapsLock = 20] = "CapsLock", e[e.Escape = 27] = "Escape", e[e.Space = 32] = "Space", e[e.PageUp = 33] = "PageUp", e[e.PageDown = 34] = "PageDown", e[e.End = 35] = "End", e[e.Home = 36] = "Home", e[e.LeftArrow = 37] = "LeftArrow", e[e.UpArrow = 38] = "UpArrow", e[e.RightArrow = 39] = "RightArrow", e[e.DownArrow = 40] = "DownArrow", e[e.Insert = 45] = "Insert", e[e.Delete = 46] = "Delete", e[e.Zero = 48] = "Zero", e[e.ClosedParen = 48] = "ClosedParen", e[e.One = 49] = "One", e[e.ExclamationMark = 49] = "ExclamationMark", e[e.Two = 50] = "Two", e[e.AtSign = 50] = "AtSign", e[e.Three = 51] = "Three", e[e.PoundSign = 51] = "PoundSign", e[e.Hash = 51] = "Hash", e[e.Four = 52] = "Four", e[e.DollarSign = 52] = "DollarSign", e[e.Five = 53] = "Five", e[e.PercentSign = 53] = "PercentSign", e[e.Six = 54] = "Six", e[e.Caret = 54] = "Caret", e[e.Hat = 54] = "Hat", e[e.Seven = 55] = "Seven", e[e.Ampersand = 55] = "Ampersand", e[e.Eight = 56] = "Eight", e[e.Star = 56] = "Star", e[e.Asterik = 56] = "Asterik", e[e.Nine = 57] = "Nine", e[e.OpenParen = 57] = "OpenParen", e[e.A = 65] = "A", e[e.B = 66] = "B", e[e.C = 67] = "C", e[e.D = 68] = "D", e[e.E = 69] = "E", e[e.F = 70] = "F", e[e.G = 71] = "G", e[e.H = 72] = "H", e[e.I = 73] = "I", e[e.J = 74] = "J", e[e.K = 75] = "K", e[e.L = 76] = "L", e[e.M = 77] = "M", e[e.N = 78] = "N", e[e.O = 79] = "O", e[e.P = 80] = "P", e[e.Q = 81] = "Q", e[e.R = 82] = "R", e[e.S = 83] = "S", e[e.T = 84] = "T", e[e.U = 85] = "U", e[e.V = 86] = "V", e[e.W = 87] = "W", e[e.X = 88] = "X", e[e.Y = 89] = "Y", e[e.Z = 90] = "Z", e[e.LeftWindowKey = 91] = "LeftWindowKey", e[e.RightWindowKey = 92] = "RightWindowKey", e[e.SelectKey = 93] = "SelectKey", e[e.Numpad0 = 96] = "Numpad0", e[e.Numpad1 = 97] = "Numpad1", e[e.Numpad2 = 98] = "Numpad2", e[e.Numpad3 = 99] = "Numpad3", e[e.Numpad4 = 100] = "Numpad4", e[e.Numpad5 = 101] = "Numpad5", e[e.Numpad6 = 102] = "Numpad6", e[e.Numpad7 = 103] = "Numpad7", e[e.Numpad8 = 104] = "Numpad8", e[e.Numpad9 = 105] = "Numpad9", e[e.Multiply = 106] = "Multiply", e[e.Add = 107] = "Add", e[e.Subtract = 109] = "Subtract", e[e.DecimalPoint = 110] = "DecimalPoint", e[e.Divide = 111] = "Divide", e[e.F1 = 112] = "F1", e[e.F2 = 113] = "F2", e[e.F3 = 114] = "F3", e[e.F4 = 115] = "F4", e[e.F5 = 116] = "F5", e[e.F6 = 117] = "F6", e[e.F7 = 118] = "F7", e[e.F8 = 119] = "F8", e[e.F9 = 120] = "F9", e[e.F10 = 121] = "F10", e[e.F11 = 122] = "F11", e[e.F12 = 123] = "F12", e[e.NumLock = 144] = "NumLock", e[e.ScrollLock = 145] = "ScrollLock", e[e.SemiColon = 186] = "SemiColon", e[e.Equals = 187] = "Equals", e[e.Comma = 188] = "Comma", e[e.Dash = 189] = "Dash", e[e.Period = 190] = "Period", e[e.UnderScore = 189] = "UnderScore", e[e.PlusSign = 187] = "PlusSign", e[e.ForwardSlash = 191] = "ForwardSlash", e[e.Tilde = 192] = "Tilde", e[e.GraveAccent = 192] = "GraveAccent", e[e.OpenBracket = 219] = "OpenBracket", e[e.ClosedBracket = 221] = "ClosedBracket", e[e.Quote = 222] = "Quote";
})(Fl ||= {});
function Il(e) {
	return e.which || e.charCode || e.keyCode;
}
function Ll(e, ...t) {
	return t.length ? t.some((t) => e[`${t}Key`]) : e.altKey || e.shiftKey || e.ctrlKey || e.metaKey;
}
function Rl(e, ...t) {
	return Pl.every((n) => !Ll(e, n) || t.includes(n));
}
var zl, Bl, Vl, Hl = class {
	constructor() {
		zl.set(this, []), Bl.set(this, null), Vl.set(this, void 0);
	}
	get items() {
		return t(this, zl, "f");
	}
	get activeItem() {
		return t(this, Bl, "f");
	}
	setItems(e) {
		let t = this.items.filter((t) => !e.includes(t)), r = e.filter((e) => !this.items.includes(e));
		return n(this, zl, e, "f"), this.activeItem && !this.items.includes(this.activeItem) && this.updateActiveItem(null), {
			added: r,
			removed: t
		};
	}
	setActiveItem(e) {
		this.activeItem !== e && (this.updateActiveItem(e), t(this, Vl, "f")?.call(this));
	}
	updateActiveItem(e) {
		n(this, Bl, e ?? null, "f");
	}
	onActiveItemChange(e) {
		return n(this, Vl, e, "f"), this;
	}
};
zl = /* @__PURE__ */ new WeakMap(), Bl = /* @__PURE__ */ new WeakMap(), Vl = /* @__PURE__ */ new WeakMap();
var Ul, Wl, Gl, Kl, ql, Jl, Yl, Xl, Zl, Ql, $l = Symbol("typeaheadLabel"), eu = class {
	constructor(e) {
		Ul.add(this), Wl.set(this, void 0), Gl.set(this, -1), Kl.set(this, void 0), ql.set(this, void 0), Jl.set(this, []), Yl.set(this, -1), Xl.set(this, []), n(this, Wl, e.debounceInterval ?? 200, "f"), n(this, Kl, e.callback, "f"), n(this, ql, e.skipPredicate, "f");
	}
	get isTyping() {
		return t(this, Jl, "f").length > 0;
	}
	setItems(e) {
		n(this, Xl, e, "f");
	}
	setSelectedIndex(e) {
		n(this, Yl, e, "f");
	}
	reset() {
		t(this, Jl, "f").length = 0;
	}
	onKeyDown(e) {
		if (e.key && e.key.length === 1) t(this, Ul, "m", Zl).call(this, e.key);
		else {
			let n = Il(e);
			(n >= Fl.A && n <= Fl.Z || n >= Fl.Zero && n <= Fl.Nine) && t(this, Ul, "m", Zl).call(this, String.fromCharCode(n));
		}
	}
};
Wl = /* @__PURE__ */ new WeakMap(), Gl = /* @__PURE__ */ new WeakMap(), Kl = /* @__PURE__ */ new WeakMap(), ql = /* @__PURE__ */ new WeakMap(), Jl = /* @__PURE__ */ new WeakMap(), Yl = /* @__PURE__ */ new WeakMap(), Xl = /* @__PURE__ */ new WeakMap(), Ul = /* @__PURE__ */ new WeakSet(), Zl = function(e) {
	t(this, Jl, "f").push(e.toLocaleUpperCase()), clearTimeout(t(this, Gl, "f")), n(this, Gl, setTimeout(() => t(this, Ul, "m", Ql).call(this), t(this, Wl, "f")), "f");
}, Ql = function() {
	let e = t(this, Jl, "f").join("");
	for (let n = 1; n < t(this, Xl, "f").length + 1; n++) {
		let r = (t(this, Yl, "f") + n) % t(this, Xl, "f").length, i = t(this, Xl, "f")[r], a = i[$l]?.().toLocaleUpperCase().trim();
		if (!t(this, ql, "f")?.call(this, i) && a?.indexOf(e) === 0) {
			t(this, Kl, "f").call(this, i);
			break;
		}
	}
	this.reset();
};
var tu, nu, ru, iu, au, ou, su, cu, lu = class extends Hl {
	constructor() {
		super(...arguments), tu.add(this), nu.set(this, void 0), this.wrap = !1, this.homeAndEnd = !1, this.pageUpAndDown = !1, this.pageDelta = 10, this.vertical = !1, this.allowedModifiers = [], this.skipPredicate = (e) => ti(e) && e.disabled, this.directionality = "ltr";
	}
	setItems(e) {
		return t(this, nu, "f")?.setItems(e), super.setItems(e);
	}
	updateActiveItem(e) {
		super.updateActiveItem(e), t(this, nu, "f") && t(this, nu, "f").setSelectedIndex(e ? this.items.indexOf(e) : -1);
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
		return e ? n(this, nu, new eu({ callback: (e) => this.setActiveItem(e) }), "f") : n(this, nu, void 0, "f"), this;
	}
	withSkipPredicate(e) {
		return this.skipPredicate = e, this;
	}
	withDirectionality(e) {
		return this.directionality = e, this;
	}
	onKeyDown(e) {
		if (e.defaultPrevented) return;
		let n = Rl(e, ...this.allowedModifiers);
		switch (e.key) {
			case "Left":
			case "ArrowLeft":
				if (n && !this.vertical) {
					e.preventDefault();
					let n = this.directionality === "ltr" ? t(this, tu, "m", ou).call(this) : t(this, tu, "m", au).call(this);
					n && this.setActiveItem(n);
				}
				break;
			case "Up":
			case "ArrowUp":
				if (n) {
					e.preventDefault();
					let n = this.directionality === "ltr" ? t(this, tu, "m", ou).call(this) : t(this, tu, "m", au).call(this);
					n && this.setActiveItem(n);
				}
				break;
			case "Right":
			case "ArrowRight":
				if (n && !this.vertical) {
					e.preventDefault();
					let n = this.directionality === "ltr" ? t(this, tu, "m", au).call(this) : t(this, tu, "m", ou).call(this);
					n && this.setActiveItem(n);
				}
				break;
			case "Down":
			case "ArrowDown":
				if (n) {
					e.preventDefault();
					let n = this.directionality === "ltr" ? t(this, tu, "m", au).call(this) : t(this, tu, "m", ou).call(this);
					n && this.setActiveItem(n);
				}
				break;
			case "Home":
				if (n && this.homeAndEnd) {
					e.preventDefault();
					let n = t(this, tu, "m", ru).call(this);
					n && this.setActiveItem(n);
				}
				break;
			case "End":
				if (n && this.homeAndEnd) {
					e.preventDefault();
					let n = t(this, tu, "m", iu).call(this);
					n && this.setActiveItem(n);
				}
				break;
			case "PageUp":
				if (n && this.pageUpAndDown) {
					e.preventDefault();
					let n = this.activeItem ? Math.max(0, this.items.indexOf(this.activeItem) - this.pageDelta) : 0, r = this.directionality === "ltr" ? t(this, tu, "m", su).call(this, n) : t(this, tu, "m", cu).call(this, n);
					r && this.setActiveItem(r);
				}
				break;
			case "PageDown":
				if (n && this.pageUpAndDown) {
					e.preventDefault();
					let n = this.activeItem ? Math.min(this.items.length - 1, this.items.indexOf(this.activeItem) + this.pageDelta) : this.items.length - 1, r = this.directionality === "ltr" ? t(this, tu, "m", cu).call(this, n) : t(this, tu, "m", su).call(this, n);
					r && this.setActiveItem(r);
				}
				break;
			default: (n || Ll(e, "shift")) && t(this, nu, "f")?.onKeyDown(e);
		}
	}
};
nu = /* @__PURE__ */ new WeakMap(), tu = /* @__PURE__ */ new WeakSet(), ru = function() {
	for (let e = 0; e < this.items.length && this.items[e] !== this.activeItem; e++) {
		let t = this.items[e];
		if (!this.skipPredicate(t)) return t;
	}
	return null;
}, iu = function() {
	for (let e = this.items.length - 1; e >= 0 && this.items[e] !== this.activeItem; e--) {
		let t = this.items[e];
		if (!this.skipPredicate(t)) return t;
	}
	return null;
}, au = function() {
	for (let e = 0; e < this.items.length; e++) if (this.items[e] === this.activeItem) {
		for (let t = e + 1; t < this.items.length; t++) {
			let e = this.items[t];
			if (!this.skipPredicate(e)) return e;
		}
		break;
	}
	return this.wrap ? t(this, tu, "m", ru).call(this) : null;
}, ou = function() {
	for (let e = 0; e < this.items.length; e++) if (this.items[e] === this.activeItem) {
		for (let t = e - 1; t >= 0; t--) {
			let e = this.items[t];
			if (!this.skipPredicate(e)) return e;
		}
		break;
	}
	return this.wrap ? t(this, tu, "m", iu).call(this) : null;
}, su = function(e) {
	for (let t = e; t >= 0; t--) {
		let e = this.items[t];
		if (!this.skipPredicate(e)) return e;
	}
	return null;
}, cu = function(e) {
	for (let t = e; t < this.items.length; t++) {
		let e = this.items[t];
		if (!this.skipPredicate(e)) return e;
	}
	return null;
};
var uu, du = class extends lu {
	constructor() {
		super(...arguments), uu.set(this, void 0);
	}
	setActiveItem(e) {
		super.setActiveItem(e), e?.focus(t(this, uu, "f"));
	}
	withOptions(e) {
		return n(this, uu, e, "f"), this;
	}
};
uu = /* @__PURE__ */ new WeakMap();
var fu, pu = class extends du {
	constructor() {
		super(...arguments), fu.set(this, !1);
	}
	updateActiveItem(e) {
		if (super.updateActiveItem(e), !t(this, fu, "f")) {
			e?.setAttribute("tabindex", "0");
			for (let t of this.items) t !== e && t.hasAttribute("tabindex") && t.setAttribute("tabindex", "-1");
		}
	}
	setItems(e) {
		let n = super.setItems(e);
		if (!t(this, fu, "f")) for (let e of n.added) e !== this.activeItem && !this.skipPredicate(e) && e.setAttribute("tabindex", "-1");
		return n;
	}
	disableRovingTabIndex(e = !0) {
		if (e !== t(this, fu, "f")) {
			n(this, fu, e, "f");
			for (let e of this.items) this.skipPredicate(e) || e?.setAttribute("tabindex", t(this, fu, "f") || e === this.activeItem ? "0" : "-1");
		}
		return this;
	}
};
fu = /* @__PURE__ */ new WeakMap();
var mu, hu = class extends pu {
	constructor() {
		super(...arguments), mu.set(this, !1);
	}
	get disabled() {
		return t(this, mu, "f");
	}
	set disabled(e) {
		n(this, mu, e, "f"), this.items.forEach((t) => t.disabled = e);
	}
	setItems(e) {
		this.disabled && e.forEach((e) => e.disabled = !0);
		let { added: t, removed: n } = super.setItems(e);
		if ((t.length > 0 || n.length > 0) && (this.activeItem || this.updateActiveItem(t.find((e) => !this.skipPredicate(e)) ?? null), this.activeItem && (this.activeItem.disabled || !jr(this.activeItem)))) {
			let e = t.find((e) => !this.skipPredicate(e) && jr(e));
			e && this.updateActiveItem(e);
		}
		return {
			added: t,
			removed: n
		};
	}
};
mu = /* @__PURE__ */ new WeakMap();
var gu, _u, vu, yu, bu, q = Symbol("selectionManager"), xu = class extends hu {
	constructor() {
		super(...arguments), gu.add(this), _u.set(this, void 0), vu.set(this, []), yu.set(this, !1);
	}
	get multi() {
		return t(this, yu, "f");
	}
	set multi(e) {
		n(this, yu, e, "f"), t(this, gu, "m", bu).call(this, !0);
	}
	get selectedItems() {
		return t(this, vu, "f");
	}
	notifySelectionChange(e) {
		this.items.includes(e) && (jr(e) ? this.select(e) : this.deselect(e));
	}
	deselect(e) {
		if (this.items.includes(e)) {
			jr(e) && Mr(e, !1);
			let n = t(this, vu, "f").indexOf(e);
			n >= 0 && (t(this, vu, "f").splice(n, 1), t(this, _u, "f")?.call(this));
		}
	}
	select(e, n = !0) {
		if (!e || this.items.includes(e)) {
			if (!this.multi) {
				for (let n of t(this, vu, "f")) n !== e && Mr(n, !1);
				t(this, vu, "f").length = 0;
			}
			e && (t(this, vu, "f").push(e), jr(e) || Mr(e, !0)), n && this.updateActiveItem(e), t(this, _u, "f")?.call(this);
		}
	}
	setItems(e) {
		let { added: r, removed: i } = super.setItems(e);
		return n(this, vu, t(this, vu, "f").filter((e) => !i.includes(e)), "f"), t(this, vu, "f").push(...r.filter((e) => jr(e))), t(this, gu, "m", bu).call(this), t(this, _u, "f")?.call(this), {
			added: r,
			removed: i
		};
	}
	onSelectedItemsChange(e) {
		return n(this, _u, e, "f"), this;
	}
};
_u = /* @__PURE__ */ new WeakMap(), vu = /* @__PURE__ */ new WeakMap(), yu = /* @__PURE__ */ new WeakMap(), gu = /* @__PURE__ */ new WeakSet(), bu = function(e = !1) {
	if (!this.multi && t(this, vu, "f").length > 1) {
		for (let e = 1; e < t(this, vu, "f").length; e++) Mr(t(this, vu, "f")[e], !1);
		t(this, vu, "f").length = 1, e && t(this, _u, "f")?.call(this);
	}
};
function Su(e, t) {
	return e.getAttribute(t)?.match(/\S+/g) ?? [];
}
function Cu(e, t, n) {
	n = n.trim();
	let r = Su(e, t);
	r.some((e) => e.trim() === n) || (r.push(n), e.setAttribute(t, r.join(" ")));
}
function wu(e, t, n) {
	n = n.trim();
	let r = Su(e, t).filter((e) => e !== n);
	r.length > 0 ? e.setAttribute(t, r.join(" ")) : e.removeAttribute(t);
}
function Tu(e) {
	e.position = "absolute", e.appearance = "none", e.visibility = "hidden", e.border = "0", e.outline = "0", e.overflow = "hidden", e.left = "0", e.height = "1px", e.width = "1px", e.margin = "-1px", e.padding = "0", e.whiteSpace = "nowrap";
}
var Eu, Du, Ou, ku, Au, ju = class {
	static describe(e, n, r = "tooltip") {
		let i = e.getRootNode();
		if (!(i instanceof ShadowRoot || i instanceof Document)) return;
		let a = t(this, Eu, "f", Ou).get(i);
		a || (a = {
			containerElement: t(this, Eu, "m", ku).call(this),
			messageRegistry: /* @__PURE__ */ new Map()
		}, (i instanceof Document ? i.body : i).appendChild(a.containerElement), t(this, Eu, "f", Ou).set(i, a));
		let o = `${r}:${n}`, s = a.messageRegistry.get(o);
		s || (s = {
			messageElement: t(this, Eu, "m", Au).call(this, n, r),
			count: 0
		}, a.containerElement.appendChild(s.messageElement), a.messageRegistry.set(o, s)), s.count++, Cu(e, "aria-describedby", s.messageElement.id);
	}
	static removeDescription(e, n, r = "tooltip") {
		let i = e.getRootNode(), a = t(this, Eu, "f", Ou).get(i);
		if (!a) return;
		let o = `${r}:${n}`, s = a.messageRegistry.get(o);
		s && (s.count--, wu(e, "aria-describedby", s.messageElement.id), s.count <= 0 && (s.messageElement.remove(), a.messageRegistry.delete(o)), a.messageRegistry.size == 0 && (a.containerElement?.remove(), t(this, Eu, "f", Ou).delete(i)));
	}
};
Eu = ju, ku = function() {
	let e = document.createElement("div");
	return e.classList.add("m3e-describedby-message-container"), Tu(e.style), e;
}, Au = function(e, r) {
	var i, a;
	let o = document.createElement("span");
	return o.id = `m3e-describedby-message-${n(this, Eu, (a = t(this, Eu, "f", Du), i = a++, a), "f", Du), i}`, o.role = r, o.ariaAtomic = "true", o.innerText = e.trim(), o;
}, Du = { value: 0 }, Ou = { value: /* @__PURE__ */ new Map() }, globalThis.M3eAriaDescriber = ju;
var Mu, Nu, Pu, Fu = class {
	static isFocusable(e, n, r = !1, i = !1) {
		let a = `:is(button,input,select,textarea,object,:is(a,area)[href],[tabindex]${i ? "" : ":not([tabindex='-1'])"},[contenteditable=true])${i ? "" : ":not(:disabled,[disabled])"}${r ? "" : ":not([hidden])"}`;
		return e.matches(a) ? !t(this, Mu, "m", Pu).call(this, e, r) && !t(this, Mu, "m", Nu).call(this, n, r) : !e.localName.includes("-") || !e.matches(":not(:disabled,[disabled])") || e.getAttribute("aria-disabled") === "true" ? !1 : e.shadowRoot?.delegatesFocus ? !t(this, Mu, "m", Pu).call(this, e, r) && !t(this, Mu, "m", Nu).call(this, n, r) : !1;
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
Mu = Fu, Nu = function(e, n = !1) {
	return e?.some((e) => e.matches(`:is([aria-hidden='true'],:disabled,[disabled],[inert]${n ? "" : ",[hidden]"})`) || t(this, Mu, "m", Pu).call(this, e, n)) ?? !1;
}, Pu = function(e, t) {
	if (t) return !1;
	let n = getComputedStyle(e);
	return n.display === "none" || n.visibility === "hidden";
}, globalThis.M3eInteractivityChecker = Fu;
var Iu, Lu, Ru, zu, Bu, Vu, Hu = class extends ni(S) {
	constructor() {
		super(...arguments), Iu.add(this);
	}
	render() {
		let e = b`<div class="trap" .inert="${this.disabled}" tabindex="0" aria-hidden="true" @focus="${t(this, Iu, "m", Lu)}"></div>`;
		return b`${e}<slot></slot>${e}`;
	}
};
Iu = /* @__PURE__ */ new WeakSet(), Lu = function(e) {
	function n(e, t) {
		return e === t || e instanceof HTMLElement && e.shadowRoot !== null && e.shadowRoot.contains(t);
	}
	let [r, i] = t(this, Iu, "m", Ru).call(this), a = e?.target === this._firstTrap, o = n(e.relatedTarget, r), s = n(e.relatedTarget, i), c = !o && !s;
	!a && s || a && c ? r?.focus() : (a && o || !a && c) && i?.focus();
}, Ru = function() {
	let e = null, n = null;
	return t(this, Iu, "m", zu).call(this, this, [], (t, r) => {
		Fu.isFocusable(t, r) && t instanceof HTMLElement && t.tabIndex >= 0 && (e ??= t, n = t);
	}), [e, n];
}, zu = function e(n, r, i) {
	r = [...r, n];
	for (let a of n.childNodes) {
		if (a.nodeType !== Node.ELEMENT_NODE) continue;
		let n = a;
		i(n, r), n.shadowRoot ? t(this, Iu, "m", Bu).call(this, n, r, i) : n instanceof HTMLSlotElement ? t(this, Iu, "m", Vu).call(this, n, r, i) : n.hasChildNodes() && t(this, Iu, "m", e).call(this, n, r, i);
	}
}, Bu = function(e, n, r) {
	for (let i of e.shadowRoot?.childNodes ?? []) {
		if (i.nodeType !== Node.ELEMENT_NODE) continue;
		let e = i;
		r(e, n), e.hasChildNodes() && t(this, Iu, "m", zu).call(this, e, n, r);
	}
}, Vu = function e(n, r, i) {
	r = [...r, n];
	let a = n.assignedElements();
	a.length == 0 && n.hasChildNodes() && (a = n.childNodes);
	for (let n of a) {
		if (n.nodeType !== Node.ELEMENT_NODE) continue;
		let a = n;
		a instanceof HTMLSlotElement ? t(this, Iu, "m", e).call(this, a, r, i) : (i(a, r), a.hasChildNodes() && t(this, Iu, "m", zu).call(this, a, r, i));
	}
}, Hu.styles = l`:host { display: contents; } .trap { outline: none; }`, e([w(".trap")], Hu.prototype, "_firstTrap", void 0), Hu = e([T("m3e-focus-trap")], Hu);
var Uu, Wu, Gu, Ku, qu, Ju, Yu, Xu = class {
	static announce(e, ...r) {
		n(this, Uu, t(this, Uu, "f", Gu) ?? t(this, Uu, "m", Yu).call(this), "f", Gu);
		let i, a;
		r.length === 1 && typeof r[0] == "number" ? a = r[0] : [i, a] = r, this.clear(), clearTimeout(t(this, Uu, "f", Ku)), t(this, Uu, "f", Gu).setAttribute("aria-live", i ?? "polite");
		for (let e of document.querySelectorAll("m3e-dialog")) Cu(e, "aria-owns", t(this, Uu, "f", Gu).id);
		return n(this, Uu, t(this, Uu, "f", qu) ?? new Promise((e) => n(this, Uu, e, "f", Ju)), "f", qu), clearTimeout(t(this, Uu, "f", Ku)), n(this, Uu, setTimeout(() => {
			t(this, Uu, "f", Gu) && (t(this, Uu, "f", Gu).textContent = e, a !== void 0 && n(this, Uu, setTimeout(() => this.clear(), a), "f", Ku), t(this, Uu, "f", Ju)?.call(this), n(this, Uu, n(this, Uu, void 0, "f", Ju), "f", qu));
		}, 100), "f", Ku), t(this, Uu, "f", qu);
	}
	static clear() {
		t(this, Uu, "f", Gu) && (t(this, Uu, "f", Gu).textContent = "");
	}
};
Uu = Xu, Yu = function() {
	var e, r;
	let i = document.getElementsByClassName("m3e-live-announcer");
	for (let e = 0; e < i.length; e++) i[e].remove();
	let a = document.createElement("div");
	return a.classList.add("m3e-live-announcer"), a.setAttribute("aria-atomic", "true"), a.setAttribute("aria-live", "polite"), a.id = `m3e-live-announcer-${n(this, Uu, (r = t(this, Uu, "f", Wu), e = r++, r), "f", Wu), e}`, Tu(a.style), document.body.append(a), a;
}, Wu = { value: 0 }, Gu = { value: void 0 }, Ku = { value: void 0 }, qu = { value: void 0 }, Ju = { value: void 0 }, globalThis.M3eLiveAnnouncer = Xu;
//#endregion
//#region node_modules/@m3e/web/dist/core-bidi.js
var Zu, Qu, $u, ed, td, nd = /^(ar|ckb|dv|he|iw|fa|nqo|ps|sd|ug|ur|yi|.*[-_](Adlm|Arab|Hebr|Nkoo|Rohg|Thaa))(?!.*[-_](Latn|Cyrl)($|-|_))($|-|_)/i, rd = class {
	static get current() {
		return t(this, Zu, "f", Qu);
	}
	static observe(e) {
		return t(this, Zu, "f", $u).push(e), () => {
			t(this, Zu, "f", $u).splice(t(this, Zu, "f", $u).indexOf(e), 1);
		};
	}
};
Zu = rd, ed = function() {
	let e = (document.body?.dir || document.documentElement?.dir)?.toLowerCase() || "auto";
	n(this, Zu, e === "auto" && navigator?.language ? nd.test(navigator.language) ? "rtl" : "ltr" : e === "rtl" ? "rtl" : "ltr", "f", Qu);
}, td = function() {
	t(this, Zu, "m", ed).call(this), t(this, Zu, "f", $u).forEach((e) => e());
}, Qu = { value: "ltr" }, $u = { value: [] }, (() => {
	if (typeof window < "u") {
		if (t(Zu, Zu, "m", ed).call(Zu), MutationObserver) {
			let e = { attributeFilter: ["dir"] }, n = new MutationObserver(() => t(Zu, Zu, "m", td).call(Zu));
			document.body && n.observe(document.body, e), document.documentElement && n.observe(document.documentElement, e);
		}
		window.addEventListener("languagechange", () => t(Zu, Zu, "m", td).call(Zu));
	}
})(), globalThis.M3eDirectionality = rd;
function id(e) {
	var r;
	class i extends e {
		constructor() {
			super(...arguments), r.set(this, void 0);
		}
		connectedCallback() {
			super.connectedCallback(), t(this, r, "f")?.call(this), n(this, r, M3eDirectionality.observe(() => O(this, "--rtl", M3eDirectionality.current === "rtl")), "f"), O(this, "--rtl", M3eDirectionality.current === "rtl");
		}
		disconnectedCallback() {
			super.disconnectedCallback(), t(this, r, "f")?.call(this), D(this, "--rtl");
		}
	}
	return r = /* @__PURE__ */ new WeakMap(), i;
}
//#endregion
//#region node_modules/@m3e/web/dist/core-anchoring.js
var ad = Math.min, od = Math.max, sd = Math.round, cd = Math.floor, ld = (e) => ({
	x: e,
	y: e
}), ud = {
	left: "right",
	right: "left",
	bottom: "top",
	top: "bottom"
};
function dd(e, t, n) {
	return od(e, ad(t, n));
}
function fd(e, t) {
	return typeof e == "function" ? e(t) : e;
}
function pd(e) {
	return e.split("-")[0];
}
function md(e) {
	return e.split("-")[1];
}
function hd(e) {
	return e === "x" ? "y" : "x";
}
function gd(e) {
	return e === "y" ? "height" : "width";
}
function _d(e) {
	let t = e[0];
	return t === "t" || t === "b" ? "y" : "x";
}
function vd(e) {
	return hd(_d(e));
}
function yd(e, t, n) {
	n === void 0 && (n = !1);
	let r = md(e), i = vd(e), a = gd(i), o = i === "x" ? r === (n ? "end" : "start") ? "right" : "left" : r === "start" ? "bottom" : "top";
	return t.reference[a] > t.floating[a] && (o = Od(o)), [o, Od(o)];
}
function bd(e) {
	let t = Od(e);
	return [
		xd(e),
		t,
		xd(t)
	];
}
function xd(e) {
	return e.includes("start") ? e.replace("start", "end") : e.replace("end", "start");
}
var Sd = ["left", "right"], Cd = ["right", "left"], wd = ["top", "bottom"], Td = ["bottom", "top"];
function Ed(e, t, n) {
	switch (e) {
		case "top":
		case "bottom": return n ? t ? Cd : Sd : t ? Sd : Cd;
		case "left":
		case "right": return t ? wd : Td;
		default: return [];
	}
}
function Dd(e, t, n, r) {
	let i = md(e), a = Ed(pd(e), n === "start", r);
	return i && (a = a.map((e) => e + "-" + i), t && (a = a.concat(a.map(xd)))), a;
}
function Od(e) {
	let t = pd(e);
	return ud[t] + e.slice(t.length);
}
function kd(e) {
	return {
		top: 0,
		right: 0,
		bottom: 0,
		left: 0,
		...e
	};
}
function Ad(e) {
	return typeof e == "number" ? {
		top: e,
		right: e,
		bottom: e,
		left: e
	} : kd(e);
}
function jd(e) {
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
function Md(e, t, n) {
	let { reference: r, floating: i } = e, a = _d(t), o = vd(t), s = gd(o), c = pd(t), l = a === "y", u = r.x + r.width / 2 - i.width / 2, d = r.y + r.height / 2 - i.height / 2, f = r[s] / 2 - i[s] / 2, p;
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
	switch (md(t)) {
		case "start":
			p[o] -= f * (n && l ? -1 : 1);
			break;
		case "end": p[o] += f * (n && l ? -1 : 1);
	}
	return p;
}
async function Nd(e, t) {
	t === void 0 && (t = {});
	let { x: n, y: r, platform: i, rects: a, elements: o, strategy: s } = e, { boundary: c = "clippingAncestors", rootBoundary: l = "viewport", elementContext: u = "floating", altBoundary: d = !1, padding: f = 0 } = fd(t, e), p = Ad(f), m = o[d ? u === "floating" ? "reference" : "floating" : u], h = jd(await i.getClippingRect({
		element: await (i.isElement == null ? void 0 : i.isElement(m)) ?? !0 ? m : m.contextElement || await (i.getDocumentElement == null ? void 0 : i.getDocumentElement(o.floating)),
		boundary: c,
		rootBoundary: l,
		strategy: s
	})), g = u === "floating" ? {
		x: n,
		y: r,
		width: a.floating.width,
		height: a.floating.height
	} : a.reference, ee = await (i.getOffsetParent == null ? void 0 : i.getOffsetParent(o.floating)), te = await (i.isElement == null ? void 0 : i.isElement(ee)) && await (i.getScale == null ? void 0 : i.getScale(ee)) || {
		x: 1,
		y: 1
	}, _ = jd(i.convertOffsetParentRelativeRectToViewportRelativeRect ? await i.convertOffsetParentRelativeRectToViewportRelativeRect({
		elements: o,
		rect: g,
		offsetParent: ee,
		strategy: s
	}) : g);
	return {
		top: (h.top - _.top + p.top) / te.y,
		bottom: (_.bottom - h.bottom + p.bottom) / te.y,
		left: (h.left - _.left + p.left) / te.x,
		right: (_.right - h.right + p.right) / te.x
	};
}
var Pd = 50, Fd = async (e, t, n) => {
	let { placement: r = "bottom", strategy: i = "absolute", middleware: a = [], platform: o } = n, s = o.detectOverflow ? o : {
		...o,
		detectOverflow: Nd
	}, c = await (o.isRTL == null ? void 0 : o.isRTL(t)), l = await o.getElementRects({
		reference: e,
		floating: t,
		strategy: i
	}), { x: u, y: d } = Md(l, r, c), f = r, p = 0, m = {};
	for (let n = 0; n < a.length; n++) {
		let h = a[n];
		if (!h) continue;
		let { name: g, fn: ee } = h, { x: te, y: _, data: ne, reset: v } = await ee({
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
		u = te ?? u, d = _ ?? d, m[g] = {
			...m[g],
			...ne
		}, v && p < Pd && (p++, typeof v == "object" && (v.placement && (f = v.placement), v.rects && (l = v.rects === !0 ? await o.getElementRects({
			reference: e,
			floating: t,
			strategy: i
		}) : v.rects), {x: u, y: d} = Md(l, f, c)), n = -1);
	}
	return {
		x: u,
		y: d,
		placement: f,
		strategy: i,
		middlewareData: m
	};
}, Id = function(e) {
	return e === void 0 && (e = {}), {
		name: "flip",
		options: e,
		async fn(t) {
			var n;
			let { placement: r, middlewareData: i, rects: a, initialPlacement: o, platform: s, elements: c } = t, { mainAxis: l = !0, crossAxis: u = !0, fallbackPlacements: d, fallbackStrategy: f = "bestFit", fallbackAxisSideDirection: p = "none", flipAlignment: m = !0, ...h } = fd(e, t);
			if ((n = i.arrow) != null && n.alignmentOffset) return {};
			let g = pd(r), ee = _d(o), te = pd(o) === o, _ = await (s.isRTL == null ? void 0 : s.isRTL(c.floating)), ne = d || (te || !m ? [Od(o)] : bd(o)), v = p !== "none";
			!d && v && ne.push(...Dd(o, m, p, _));
			let re = [o, ...ne], y = await s.detectOverflow(t, h), ie = [], ae = i.flip?.overflows || [];
			if (l && ie.push(y[g]), u) {
				let e = yd(r, a, _);
				ie.push(y[e[0]], y[e[1]]);
			}
			if (ae = [...ae, {
				placement: r,
				overflows: ie
			}], !ie.every((e) => e <= 0)) {
				let e = (i.flip?.index || 0) + 1, t = re[e];
				if (t && (u !== "alignment" || ee === _d(t) || ae.every((e) => _d(e.placement) !== ee || e.overflows[0] > 0))) return {
					data: {
						index: e,
						overflows: ae
					},
					reset: { placement: t }
				};
				let n = ae.filter((e) => e.overflows[0] <= 0).sort((e, t) => e.overflows[1] - t.overflows[1])[0]?.placement;
				if (!n) switch (f) {
					case "bestFit": {
						let e = ae.filter((e) => {
							if (v) {
								let t = _d(e.placement);
								return t === ee || t === "y";
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
function Ld(e) {
	let t = ad(...e.map((e) => e.left)), n = ad(...e.map((e) => e.top)), r = od(...e.map((e) => e.right)), i = od(...e.map((e) => e.bottom));
	return {
		x: t,
		y: n,
		width: r - t,
		height: i - n
	};
}
function Rd(e) {
	let t = e.slice().sort((e, t) => e.y - t.y), n = [], r = null;
	for (let e = 0; e < t.length; e++) {
		let i = t[e];
		!r || i.y - r.y > r.height / 2 ? n.push([i]) : n[n.length - 1].push(i), r = i;
	}
	return n.map((e) => jd(Ld(e)));
}
var zd = function(e) {
	return e === void 0 && (e = {}), {
		name: "inline",
		options: e,
		async fn(t) {
			let { placement: n, elements: r, rects: i, platform: a, strategy: o } = t, { padding: s = 2, x: c, y: l } = fd(e, t), u = Array.from(await (a.getClientRects == null ? void 0 : a.getClientRects(r.reference)) || []), d = Rd(u), f = jd(Ld(u)), p = Ad(s);
			function m() {
				if (d.length === 2 && d[0].left > d[1].right && c != null && l != null) return d.find((e) => c > e.left - p.left && c < e.right + p.right && l > e.top - p.top && l < e.bottom + p.bottom) || f;
				if (d.length >= 2) {
					if (_d(n) === "y") {
						let e = d[0], t = d[d.length - 1], r = pd(n) === "top", i = e.top, a = t.bottom, o = r ? e.left : t.left, s = r ? e.right : t.right;
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
					let e = pd(n) === "left", t = od(...d.map((e) => e.right)), r = ad(...d.map((e) => e.left)), i = d.filter((n) => e ? n.left === r : n.right === t), a = i[0].top, o = i[i.length - 1].bottom, s = r, c = t;
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
}, Bd = /*#__PURE__*/ new Set(["left", "top"]);
async function Vd(e, t) {
	let { placement: n, platform: r, elements: i } = e, a = await (r.isRTL == null ? void 0 : r.isRTL(i.floating)), o = pd(n), s = md(n), c = _d(n) === "y", l = Bd.has(o) ? -1 : 1, u = a && c ? -1 : 1, d = fd(t, e), { mainAxis: f, crossAxis: p, alignmentAxis: m } = typeof d == "number" ? {
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
var Hd = function(e) {
	return e === void 0 && (e = 0), {
		name: "offset",
		options: e,
		async fn(t) {
			var n;
			let { x: r, y: i, placement: a, middlewareData: o } = t, s = await Vd(t, e);
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
}, Ud = function(e) {
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
			} }, ...l } = fd(e, t), u = {
				x: n,
				y: r
			}, d = await a.detectOverflow(t, l), f = _d(pd(i)), p = hd(f), m = u[p], h = u[f];
			if (o) {
				let e = p === "y" ? "top" : "left", t = p === "y" ? "bottom" : "right", n = m + d[e], r = m - d[t];
				m = dd(n, m, r);
			}
			if (s) {
				let e = f === "y" ? "top" : "left", t = f === "y" ? "bottom" : "right", n = h + d[e], r = h - d[t];
				h = dd(n, h, r);
			}
			let g = c.fn({
				...t,
				[p]: m,
				[f]: h
			});
			return {
				...g,
				data: {
					x: g.x - n,
					y: g.y - r,
					enabled: {
						[p]: o,
						[f]: s
					}
				}
			};
		}
	};
}, Wd = function(e) {
	return e === void 0 && (e = {}), {
		options: e,
		fn(t) {
			let { x: n, y: r, placement: i, rects: a, middlewareData: o } = t, { offset: s = 0, mainAxis: c = !0, crossAxis: l = !0 } = fd(e, t), u = {
				x: n,
				y: r
			}, d = _d(i), f = hd(d), p = u[f], m = u[d], h = fd(s, t), g = typeof h == "number" ? {
				mainAxis: h,
				crossAxis: 0
			} : {
				mainAxis: 0,
				crossAxis: 0,
				...h
			};
			if (c) {
				let e = f === "y" ? "height" : "width", t = a.reference[f] - a.floating[e] + g.mainAxis, n = a.reference[f] + a.reference[e] - g.mainAxis;
				p < t ? p = t : p > n && (p = n);
			}
			if (l) {
				let e = f === "y" ? "width" : "height", t = Bd.has(pd(i)), n = a.reference[d] - a.floating[e] + (t && o.offset?.[d] || 0) + (t ? 0 : g.crossAxis), r = a.reference[d] + a.reference[e] + (t ? 0 : o.offset?.[d] || 0) - (t ? g.crossAxis : 0);
				m < n ? m = n : m > r && (m = r);
			}
			return {
				[f]: p,
				[d]: m
			};
		}
	};
};
function Gd() {
	return typeof window < "u";
}
function Kd(e) {
	return Yd(e) ? (e.nodeName || "").toLowerCase() : "#document";
}
function qd(e) {
	var t;
	return (e == null || (t = e.ownerDocument) == null ? void 0 : t.defaultView) || window;
}
function Jd(e) {
	return ((Yd(e) ? e.ownerDocument : e.document) || window.document)?.documentElement;
}
function Yd(e) {
	return Gd() ? e instanceof Node || e instanceof qd(e).Node : !1;
}
function Xd(e) {
	return Gd() ? e instanceof Element || e instanceof qd(e).Element : !1;
}
function Zd(e) {
	return Gd() ? e instanceof HTMLElement || e instanceof qd(e).HTMLElement : !1;
}
function Qd(e) {
	return !Gd() || typeof ShadowRoot > "u" ? !1 : e instanceof ShadowRoot || e instanceof qd(e).ShadowRoot;
}
function $d(e) {
	let { overflow: t, overflowX: n, overflowY: r, display: i } = df(e);
	return /auto|scroll|overlay|hidden|clip/.test(t + r + n) && i !== "inline" && i !== "contents";
}
function ef(e) {
	return /^(table|td|th)$/.test(Kd(e));
}
function tf(e) {
	try {
		if (e.matches(":popover-open")) return !0;
	} catch {}
	try {
		return e.matches(":modal");
	} catch {
		return !1;
	}
}
var nf = /transform|translate|scale|rotate|perspective|filter/, rf = /paint|layout|strict|content/, af = (e) => !!e && e !== "none", of;
function sf(e) {
	let t = Xd(e) ? df(e) : e;
	return af(t.transform) || af(t.translate) || af(t.scale) || af(t.rotate) || af(t.perspective) || !lf() && (af(t.backdropFilter) || af(t.filter)) || nf.test(t.willChange || "") || rf.test(t.contain || "");
}
function cf(e) {
	let t = pf(e);
	for (; Zd(t) && !uf(t);) {
		if (sf(t)) return t;
		if (tf(t)) return null;
		t = pf(t);
	}
	return null;
}
function lf() {
	return of ??= typeof CSS < "u" && CSS.supports && CSS.supports("-webkit-backdrop-filter", "none"), of;
}
function uf(e) {
	return /^(html|body|#document)$/.test(Kd(e));
}
function df(e) {
	return qd(e).getComputedStyle(e);
}
function ff(e) {
	return Xd(e) ? {
		scrollLeft: e.scrollLeft,
		scrollTop: e.scrollTop
	} : {
		scrollLeft: e.scrollX,
		scrollTop: e.scrollY
	};
}
function pf(e) {
	if (Kd(e) === "html") return e;
	let t = e.assignedSlot || e.parentNode || Qd(e) && e.host || Jd(e);
	return Qd(t) ? t.host : t;
}
function mf(e) {
	let t = pf(e);
	return uf(t) ? e.ownerDocument ? e.ownerDocument.body : e.body : Zd(t) && $d(t) ? t : mf(t);
}
function hf(e, t, n) {
	t === void 0 && (t = []), n === void 0 && (n = !0);
	let r = mf(e), i = r === e.ownerDocument?.body, a = qd(r);
	if (i) {
		let e = gf(a);
		return t.concat(a, a.visualViewport || [], $d(r) ? r : [], e && n ? hf(e) : []);
	}
	return t.concat(r, hf(r, [], n));
}
function gf(e) {
	return e.parent && Object.getPrototypeOf(e.parent) ? e.frameElement : null;
}
function _f(e) {
	let t = df(e), n = parseFloat(t.width) || 0, r = parseFloat(t.height) || 0, i = Zd(e), a = i ? e.offsetWidth : n, o = i ? e.offsetHeight : r, s = sd(n) !== a || sd(r) !== o;
	return s && (n = a, r = o), {
		width: n,
		height: r,
		$: s
	};
}
function vf(e) {
	return Xd(e) ? e : e.contextElement;
}
function yf(e) {
	let t = vf(e);
	if (!Zd(t)) return ld(1);
	let n = t.getBoundingClientRect(), { width: r, height: i, $: a } = _f(t), o = (a ? sd(n.width) : n.width) / r, s = (a ? sd(n.height) : n.height) / i;
	return (!o || !Number.isFinite(o)) && (o = 1), (!s || !Number.isFinite(s)) && (s = 1), {
		x: o,
		y: s
	};
}
var bf = /*#__PURE__*/ ld(0);
function xf(e) {
	let t = qd(e);
	return !lf() || !t.visualViewport ? bf : {
		x: t.visualViewport.offsetLeft,
		y: t.visualViewport.offsetTop
	};
}
function Sf(e, t, n) {
	return t === void 0 && (t = !1), !n || t && n !== qd(e) ? !1 : t;
}
function Cf(e, t, n, r) {
	t === void 0 && (t = !1), n === void 0 && (n = !1);
	let i = e.getBoundingClientRect(), a = vf(e), o = ld(1);
	t && (r ? Xd(r) && (o = yf(r)) : o = yf(e));
	let s = Sf(a, n, r) ? xf(a) : ld(0), c = (i.left + s.x) / o.x, l = (i.top + s.y) / o.y, u = i.width / o.x, d = i.height / o.y;
	if (a) {
		let e = qd(a), t = r && Xd(r) ? qd(r) : r, n = e, i = gf(n);
		for (; i && r && t !== n;) {
			let e = yf(i), t = i.getBoundingClientRect(), r = df(i), a = t.left + (i.clientLeft + parseFloat(r.paddingLeft)) * e.x, o = t.top + (i.clientTop + parseFloat(r.paddingTop)) * e.y;
			c *= e.x, l *= e.y, u *= e.x, d *= e.y, c += a, l += o, n = qd(i), i = gf(n);
		}
	}
	return jd({
		width: u,
		height: d,
		x: c,
		y: l
	});
}
function wf(e, t) {
	let n = ff(e).scrollLeft;
	return t ? t.left + n : Cf(Jd(e)).left + n;
}
function Tf(e, t) {
	let n = e.getBoundingClientRect();
	return {
		x: n.left + t.scrollLeft - wf(e, n),
		y: n.top + t.scrollTop
	};
}
function Ef(e) {
	let { elements: t, rect: n, offsetParent: r, strategy: i } = e, a = i === "fixed", o = Jd(r), s = t ? tf(t.floating) : !1;
	if (r === o || s && a) return n;
	let c = {
		scrollLeft: 0,
		scrollTop: 0
	}, l = ld(1), u = ld(0), d = Zd(r);
	if ((d || !d && !a) && ((Kd(r) !== "body" || $d(o)) && (c = ff(r)), d)) {
		let e = Cf(r);
		l = yf(r), u.x = e.x + r.clientLeft, u.y = e.y + r.clientTop;
	}
	let f = o && !d && !a ? Tf(o, c) : ld(0);
	return {
		width: n.width * l.x,
		height: n.height * l.y,
		x: n.x * l.x - c.scrollLeft * l.x + u.x + f.x,
		y: n.y * l.y - c.scrollTop * l.y + u.y + f.y
	};
}
function Df(e) {
	return Array.from(e.getClientRects());
}
function Of(e) {
	let t = Jd(e), n = ff(e), r = e.ownerDocument.body, i = od(t.scrollWidth, t.clientWidth, r.scrollWidth, r.clientWidth), a = od(t.scrollHeight, t.clientHeight, r.scrollHeight, r.clientHeight), o = -n.scrollLeft + wf(e), s = -n.scrollTop;
	return df(r).direction === "rtl" && (o += od(t.clientWidth, r.clientWidth) - i), {
		width: i,
		height: a,
		x: o,
		y: s
	};
}
var kf = 25;
function Af(e, t) {
	let n = qd(e), r = Jd(e), i = n.visualViewport, a = r.clientWidth, o = r.clientHeight, s = 0, c = 0;
	if (i) {
		a = i.width, o = i.height;
		let e = lf();
		(!e || e && t === "fixed") && (s = i.offsetLeft, c = i.offsetTop);
	}
	let l = wf(r);
	if (l <= 0) {
		let e = r.ownerDocument, t = e.body, n = getComputedStyle(t), i = e.compatMode === "CSS1Compat" && parseFloat(n.marginLeft) + parseFloat(n.marginRight) || 0, o = Math.abs(r.clientWidth - t.clientWidth - i);
		o <= kf && (a -= o);
	} else l <= kf && (a += l);
	return {
		width: a,
		height: o,
		x: s,
		y: c
	};
}
function jf(e, t) {
	let n = Cf(e, !0, t === "fixed"), r = n.top + e.clientTop, i = n.left + e.clientLeft, a = Zd(e) ? yf(e) : ld(1);
	return {
		width: e.clientWidth * a.x,
		height: e.clientHeight * a.y,
		x: i * a.x,
		y: r * a.y
	};
}
function Mf(e, t, n) {
	let r;
	if (t === "viewport") r = Af(e, n);
	else if (t === "document") r = Of(Jd(e));
	else if (Xd(t)) r = jf(t, n);
	else {
		let n = xf(e);
		r = {
			x: t.x - n.x,
			y: t.y - n.y,
			width: t.width,
			height: t.height
		};
	}
	return jd(r);
}
function Nf(e, t) {
	let n = pf(e);
	return n === t || !Xd(n) || uf(n) ? !1 : df(n).position === "fixed" || Nf(n, t);
}
function Pf(e, t) {
	let n = t.get(e);
	if (n) return n;
	let r = hf(e, [], !1).filter((e) => Xd(e) && Kd(e) !== "body"), i = null, a = df(e).position === "fixed", o = a ? pf(e) : e;
	for (; Xd(o) && !uf(o);) {
		let t = df(o), n = sf(o);
		!n && t.position === "fixed" && (i = null), (a ? !n && !i : !n && t.position === "static" && i && (i.position === "absolute" || i.position === "fixed") || $d(o) && !n && Nf(e, o)) ? r = r.filter((e) => e !== o) : i = t, o = pf(o);
	}
	return t.set(e, r), r;
}
function Ff(e) {
	let { element: t, boundary: n, rootBoundary: r, strategy: i } = e, a = [...n === "clippingAncestors" ? tf(t) ? [] : Pf(t, this._c) : [].concat(n), r], o = Mf(t, a[0], i), s = o.top, c = o.right, l = o.bottom, u = o.left;
	for (let e = 1; e < a.length; e++) {
		let n = Mf(t, a[e], i);
		s = od(n.top, s), c = ad(n.right, c), l = ad(n.bottom, l), u = od(n.left, u);
	}
	return {
		width: c - u,
		height: l - s,
		x: u,
		y: s
	};
}
function If(e) {
	let { width: t, height: n } = _f(e);
	return {
		width: t,
		height: n
	};
}
function Lf(e, t, n) {
	let r = Zd(t), i = Jd(t), a = n === "fixed", o = Cf(e, !0, a, t), s = {
		scrollLeft: 0,
		scrollTop: 0
	}, c = ld(0);
	function l() {
		c.x = wf(i);
	}
	if (r || !r && !a) {
		if ((Kd(t) !== "body" || $d(i)) && (s = ff(t)), r) {
			let e = Cf(t, !0, a, t);
			c.x = e.x + t.clientLeft, c.y = e.y + t.clientTop;
		} else i && l();
	}
	a && !r && i && l();
	let u = i && !r && !a ? Tf(i, s) : ld(0);
	return {
		x: o.left + s.scrollLeft - c.x - u.x,
		y: o.top + s.scrollTop - c.y - u.y,
		width: o.width,
		height: o.height
	};
}
function Rf(e) {
	return df(e).position === "static";
}
function zf(e, t) {
	if (!Zd(e) || df(e).position === "fixed") return null;
	if (t) return t(e);
	let n = e.offsetParent;
	return Jd(e) === n && (n = n.ownerDocument.body), n;
}
function Bf(e, t) {
	let n = qd(e);
	if (tf(e)) return n;
	if (!Zd(e)) {
		let t = pf(e);
		for (; t && !uf(t);) {
			if (Xd(t) && !Rf(t)) return t;
			t = pf(t);
		}
		return n;
	}
	let r = zf(e, t);
	for (; r && ef(r) && Rf(r);) r = zf(r, t);
	return r && uf(r) && Rf(r) && !sf(r) ? n : r || cf(e) || n;
}
var Vf = async function(e) {
	let t = this.getOffsetParent || Bf, n = this.getDimensions, r = await n(e.floating);
	return {
		reference: Lf(e.reference, await t(e.floating), e.strategy),
		floating: {
			x: 0,
			y: 0,
			width: r.width,
			height: r.height
		}
	};
};
function Hf(e) {
	return df(e).direction === "rtl";
}
var Uf = {
	convertOffsetParentRelativeRectToViewportRelativeRect: Ef,
	getDocumentElement: Jd,
	getClippingRect: Ff,
	getOffsetParent: Bf,
	getElementRects: Vf,
	getClientRects: Df,
	getDimensions: If,
	getScale: yf,
	isElement: Xd,
	isRTL: Hf
};
function Wf(e, t) {
	return e.x === t.x && e.y === t.y && e.width === t.width && e.height === t.height;
}
function Gf(e, t) {
	let n = null, r, i = Jd(e);
	function a() {
		var e;
		clearTimeout(r), (e = n) == null || e.disconnect(), n = null;
	}
	function o(s, c) {
		s === void 0 && (s = !1), c === void 0 && (c = 1), a();
		let l = e.getBoundingClientRect(), { left: u, top: d, width: f, height: p } = l;
		if (s || t(), !f || !p) return;
		let m = cd(d), h = cd(i.clientWidth - (u + f)), g = cd(i.clientHeight - (d + p)), ee = cd(u), te = {
			rootMargin: -m + "px " + -h + "px " + -g + "px " + -ee + "px",
			threshold: od(0, ad(1, c)) || 1
		}, _ = !0;
		function ne(t) {
			let n = t[0].intersectionRatio;
			if (n !== c) {
				if (!_) return o();
				n ? o(!1, n) : r = setTimeout(() => {
					o(!1, 1e-7);
				}, 1e3);
			}
			n === 1 && !Wf(l, e.getBoundingClientRect()) && o(), _ = !1;
		}
		try {
			n = new IntersectionObserver(ne, {
				...te,
				root: i.ownerDocument
			});
		} catch {
			n = new IntersectionObserver(ne, te);
		}
		n.observe(e);
	}
	return o(!0), a;
}
function Kf(e, t, n, r) {
	r === void 0 && (r = {});
	let { ancestorScroll: i = !0, ancestorResize: a = !0, elementResize: o = typeof ResizeObserver == "function", layoutShift: s = typeof IntersectionObserver == "function", animationFrame: c = !1 } = r, l = vf(e), u = i || a ? [...l ? hf(l) : [], ...t ? hf(t) : []] : [];
	u.forEach((e) => {
		i && e.addEventListener("scroll", n, { passive: !0 }), a && e.addEventListener("resize", n);
	});
	let d = l && s ? Gf(l, n) : null, f = -1, p = null;
	o && (p = new ResizeObserver((e) => {
		let [r] = e;
		r && r.target === l && p && t && (p.unobserve(t), cancelAnimationFrame(f), f = requestAnimationFrame(() => {
			var e;
			(e = p) == null || e.observe(t);
		})), n();
	}), l && !c && p.observe(l), t && p.observe(t));
	let m, h = c ? Cf(e) : null;
	c && g();
	function g() {
		let t = Cf(e);
		h && !Wf(h, t) && n(), h = t, m = requestAnimationFrame(g);
	}
	return n(), () => {
		var e;
		u.forEach((e) => {
			i && e.removeEventListener("scroll", n), a && e.removeEventListener("resize", n);
		}), d?.(), (e = p) == null || e.disconnect(), p = null, c && cancelAnimationFrame(m);
	};
}
var qf = Hd, Jf = Ud, Yf = Id, Xf = zd, Zf = Wd, Qf = (e, t, n) => {
	let r = /* @__PURE__ */ new Map(), i = {
		platform: Uf,
		...n
	}, a = {
		...i.platform,
		_c: r
	};
	return Fd(e, t, {
		...i,
		platform: a
	});
};
function $f(e) {
	return tp(e);
}
function ep(e) {
	return e.assignedSlot ? e.assignedSlot : e.parentNode instanceof ShadowRoot ? e.parentNode.host : e.parentNode;
}
function tp(e) {
	for (let t = e; t; t = ep(t)) if (t instanceof Element && getComputedStyle(t).display === "none") return null;
	for (let t = ep(e); t; t = ep(t)) {
		if (!(t instanceof Element)) continue;
		let e = getComputedStyle(t);
		if (e.display !== "contents" && (e.position !== "static" || sf(e) || t.tagName === "BODY")) return t;
	}
	return null;
}
async function np(e, t, n, r) {
	let i = {
		x: -(2 ** 53 - 1),
		y: -(2 ** 53 - 1),
		placement: "bottom"
	};
	async function a() {
		let a = [];
		n?.inline && a.push(Xf()), n.flip && a.push(n.flip === !0 ? Yf() : Yf({ fallbackPlacements: n.flip })), n.shift && a.push(Jf({
			mainAxis: n.shift === "main" || n.shift === "both",
			crossAxis: n.shift === "cross" || n.shift === "both",
			limiter: Zf()
		})), n.offset && !isNaN(n.offset) && a.push(qf(n.offset));
		let { x: o, y: s, placement: c } = await Qf(t, e, {
			placement: n.position,
			middleware: a,
			platform: {
				...Uf,
				getOffsetParent: (e) => Uf.getOffsetParent(e, $f)
			}
		});
		(i.x !== o || i.y !== s || i.placement !== c) && r(o, s, c), Object.assign(i, {
			x: o,
			y: s,
			placement: c
		});
	}
	return await a(), Kf(t, e, async () => await a());
}
var rp, ip, ap, op, sp, cp, lp, up, dp = class extends Vi(E(S)) {
	constructor() {
		super(...arguments), rp.set(this, void 0), ip.set(this, void 0), ap.set(this, void 0), op.set(this, void 0), sp.set(this, new vt(this, {
			target: null,
			callback: () => this.hide()
		})), cp.set(this, (e) => {
			switch (e.newState) {
				case "open":
					t(this, sp, "f").observe(this), t(this, ip, "f") && t(this, sp, "f").observe(t(this, ip, "f"));
					break;
				case "closed": t(this, sp, "f").unobserveAll(), t(this, ap, "f")?.call(this), n(this, ap, void 0, "f"), n(this, op, void 0, "f");
			}
		}), lp.set(this, new fr(this, {
			target: null,
			callback: () => this.hide(!1)
		})), up.set(this, new tr(this, {
			target: null,
			callback: () => {
				t(this, ip, "f") && this.fitAnchorWidth && (this.style.minWidth = `${t(this, ip, "f").clientWidth}px`);
			}
		})), this.scrollStrategy = "hide", this.fitAnchorWidth = !1, this.anchorOffset = 0;
	}
	get isOpen() {
		return t(this, rp, "f") !== void 0;
	}
	get trigger() {
		return t(this, rp, "f") ?? null;
	}
	connectedCallback() {
		super.connectedCallback(), this.setAttribute("popover", "manual"), this.addEventListener("toggle", t(this, cp, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("toggle", t(this, cp, "f"));
	}
	async show(e, r) {
		t(this, rp, "f") && t(this, rp, "f") !== e && this.hide(), n(this, rp, e, "f"), t(this, rp, "f").ariaExpanded = "true", n(this, ip, r ?? e, "f"), this.scrollStrategy === "hide" && t(this, lp, "f").observe(t(this, ip, "f")), this.fitAnchorWidth && (t(this, up, "f").observe(t(this, ip, "f")), this.style.minWidth = `${t(this, ip, "f").clientWidth}px`), n(this, ap, await np(this, t(this, ip, "f"), {
			position: "bottom-start",
			inline: !0,
			flip: !0,
			offset: this.anchorOffset > 0 ? this.anchorOffset : void 0
		}, (e, r, i) => {
			O(this, "--top", i.includes("top")), O(this, "--bottom", i.includes("bottom")), (t(this, op, "f")?.dir !== rd.current || t(this, op, "f")?.x !== e) && (rd.current === "rtl" ? (this.style.right = `${window.innerWidth - e - this.clientWidth}px`, this.style.left = "") : (this.style.left = `${e}px`, this.style.right = "")), t(this, op, "f")?.y !== r && (this.style.top = `${r}px`), n(this, op, {
				x: e,
				y: r,
				dir: rd.current
			}, "f");
		}), "f"), this.showPopover();
	}
	hide(e = !1) {
		this.hidePopover(), t(this, rp, "f") && (t(this, rp, "f").ariaExpanded = "false", e && t(this, rp, "f").focus(), t(this, ip, "f") && (t(this, lp, "f").unobserve(t(this, ip, "f")), this.fitAnchorWidth && t(this, up, "f").unobserve(t(this, ip, "f"))), n(this, rp, void 0, "f"), n(this, ip, void 0, "f"));
	}
	async toggle(e, n) {
		t(this, rp, "f") ? this.hide() : await this.show(e, n);
	}
	render() {
		return b`<div class="base"><slot></slot></div>`;
	}
};
rp = /* @__PURE__ */ new WeakMap(), ip = /* @__PURE__ */ new WeakMap(), ap = /* @__PURE__ */ new WeakMap(), op = /* @__PURE__ */ new WeakMap(), sp = /* @__PURE__ */ new WeakMap(), cp = /* @__PURE__ */ new WeakMap(), lp = /* @__PURE__ */ new WeakMap(), up = /* @__PURE__ */ new WeakMap(), dp.styles = l`:host { position: absolute; flex-direction: column; padding: unset; margin: unset; border: unset; overflow-y: auto; scrollbar-width: ${j.scrollbar.thinWidth}; scrollbar-color: ${j.scrollbar.color}; scroll-padding-block: calc( var(--m3e-focus-ring-thickness, 3px) + var(--m3e-floating-panel-container-padding-block, 4px) ); border-radius: var(--m3e-floating-panel-container-shape, ${j.shape.corner.large}); min-width: var(--m3e-floating-panel-container-min-width, 112px); max-width: var(--m3e-floating-panel-container-max-width, 280px); max-height: var(--m3e-floating-panel-container-max-height, 280px); background-color: var(--m3e-floating-panel-container-color, ${j.color.surfaceContainer}); box-shadow: var(--m3e-floating-panel-container-elevation, ${j.elevation.level3}); opacity: 0; display: none; } :host([hidden]) { display: none; } .base { contain: layout style paint; box-sizing: border-box; display: flex; flex-direction: column; padding-block: var(--m3e-floating-panel-container-padding-block, 4px); padding-inline: var(--m3e-floating-panel-container-padding-inline, 4px); } :host(:not(:is(:state(--no-animate), :--no-animate))) { transition: ${c(`opacity ${j.motion.duration.short2} ${j.motion.easing.standard}, 
            transform ${j.motion.duration.short2} ${j.motion.easing.standard},
            overlay ${j.motion.duration.short2} ${j.motion.easing.standard} allow-discrete,
            display ${j.motion.duration.short2} ${j.motion.easing.standard} allow-discrete`)}; } :host { transform: scaleY(0.8); } :host(:popover-open) { transform: scaleY(1); animation: bounce-open ${j.motion.spring.fastSpatial}; display: block; opacity: 1; } @keyframes bounce-open { 0% { transform: scaleY(0); } 100% { transform: scaleY(1); } } :host::backdrop { background-color: transparent; } :host(:is(:state(--bottom), :--bottom)) { transform-origin: top; } :host(:is(:state(--top), :--top)) { transform-origin: bottom; } @starting-style { :host(:popover-open) { transform: scaleY(0.8); } } @media (prefers-reduced-motion) { :host(:not(:is(:state(--no-animate), :--no-animate))) { transition: none; } } @media (forced-colors: active) { :host { background-color: Menu; color: MenuText; outline: 1px solid MenuText; } }`, e([C({ attribute: "scroll-strategy" })], dp.prototype, "scrollStrategy", void 0), e([C({
	attribute: "fit-anchor-width",
	type: Boolean
})], dp.prototype, "fitAnchorWidth", void 0), e([C({
	attribute: "anchor-offset",
	type: Number
})], dp.prototype, "anchorOffset", void 0), dp = e([T("m3e-floating-panel")], dp);
//#endregion
//#region node_modules/@m3e/web/dist/fab-menu.js
function fp(e) {
	return e === "primary" || e === "secondary" || e === "tertiary";
}
var pp, mp, hp, gp, _p, vp, yp, bp, xp, Sp, Cp, wp, Tp, Ep, Dp = class extends Vi(E(M(S, "menu"))) {
	constructor() {
		super(...arguments), pp.add(this), mp.set(this, void 0), hp.set(this, void 0), gp.set(this, void 0), _p.set(this, new pu().withWrap().withHomeAndEnd().withVerticalOrientation()), vp.set(this, (e) => t(this, pp, "m", wp).call(this, e)), yp.set(this, new fr(this, {
			target: null,
			callback: () => this.hide()
		})), bp.set(this, new vt(this, {
			target: null,
			callback: () => this.hide()
		})), xp.set(this, (e) => {
			switch (e.newState) {
				case "open":
					t(this, bp, "f").observe(this), t(this, hp, "f") && t(this, bp, "f").observe(t(this, hp, "f")), setTimeout(() => t(this, _p, "f").setActiveItem(t(this, _p, "f").items.find((e) => !e.disabled)), 40);
					break;
				case "closed": t(this, bp, "f").unobserveAll(), t(this, gp, "f")?.call(this), n(this, gp, void 0, "f");
			}
		}), this.variant = "primary";
	}
	get isOpen() {
		return t(this, hp, "f") !== void 0;
	}
	async show(e) {
		t(this, hp, "f") && t(this, hp, "f") !== e && this.hide(), t(this, gp, "f")?.call(this), n(this, gp, await np(this, e, {
			position: "top-end",
			inline: !0,
			shift: "main",
			flip: !0,
			offset: 8
		}, (e, t, n) => {
			O(this, "--right", n.includes("end")), O(this, "--left", n.includes("start")), rd.current === "rtl" ? (this.style.right = `${window.innerWidth - e - this.clientWidth}px`, this.style.left = "") : (this.style.left = `${e}px`, this.style.right = ""), this.style.top = `${t}px`;
		}), "f"), this.showPopover(), n(this, hp, e, "f"), t(this, hp, "f").ariaExpanded = "true", t(this, yp, "f").observe(t(this, hp, "f")), t(this, pp, "m", Tp).call(this);
	}
	hide(e = !1) {
		t(this, gp, "f")?.call(this), n(this, gp, void 0, "f"), t(this, hp, "f") && (t(this, hp, "f").ariaExpanded = "false", e && t(this, hp, "f").focus(), t(this, pp, "m", Ep).call(this), t(this, yp, "f").unobserve(t(this, hp, "f")), n(this, hp, void 0, "f")), this.hidePopover();
	}
	async toggle(e) {
		t(this, hp, "f") ? this.hide() : await this.show(e);
	}
	connectedCallback() {
		super.connectedCallback(), t(this, pp, "m", Sp).call(this), this.tabIndex = -1, this.setAttribute("popover", "manual"), this.addEventListener("keydown", t(this, vp, "f")), this.addEventListener("toggle", t(this, xp, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("keydown", t(this, vp, "f")), this.removeEventListener("toggle", t(this, xp, "f"));
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, pp, "m", Sp).call(this);
	}
	render() {
		return b`<div class="base"><slot @slotchange="${t(this, pp, "m", Cp)}"></slot></div>`;
	}
};
mp = /* @__PURE__ */ new WeakMap(), hp = /* @__PURE__ */ new WeakMap(), gp = /* @__PURE__ */ new WeakMap(), _p = /* @__PURE__ */ new WeakMap(), vp = /* @__PURE__ */ new WeakMap(), yp = /* @__PURE__ */ new WeakMap(), bp = /* @__PURE__ */ new WeakMap(), xp = /* @__PURE__ */ new WeakMap(), pp = /* @__PURE__ */ new WeakSet(), Sp = function() {
	fp(this.variant) || (this.variant = "primary"), wr(this, this.variant, "primary", "secondary", "tertiary");
}, Cp = function() {
	let { added: e } = t(this, _p, "f").setItems([...this.querySelectorAll("m3e-fab-menu-item")]);
	t(this, _p, "f").activeItem || t(this, _p, "f").updateActiveItem(e.find((e) => !e.disabled));
}, wp = function(e) {
	switch (e.key) {
		case "Tab":
			this.hide();
			break;
		case "Escape":
			!e.shiftKey && !e.ctrlKey && this.hide(!0);
			break;
		default: t(this, _p, "f").onKeyDown(e);
	}
}, Tp = function() {
	let e = t(this, hp, "f")?.closest("m3e-fab");
	e && (n(this, mp, e.tabIndex, "f"), e.addEventListener("keydown", t(this, vp, "f")), t(this, _p, "f").setItems([...t(this, _p, "f").items, e]));
}, Ep = function() {
	let e = t(this, hp, "f")?.closest("m3e-fab");
	e && (t(this, mp, "f") !== void 0 && (e.tabIndex = t(this, mp, "f")), e.removeEventListener("keydown", t(this, vp, "f")), t(this, _p, "f").setItems([...t(this, _p, "f").items.filter((t) => t !== e)]));
}, Dp.styles = l`:host { contain: layout style; position: absolute; flex-direction: column; row-gap: var(--m3e-fab-menu-spacing, ${j.measurement.space50}); padding: unset; margin: unset; border: unset; overflow: visible; max-width: var(--m3e-fab-menu-max-width, 280px); opacity: 0; background-color: transparent; display: none; } :host([hidden]) { display: none; } :host(:not(:is(:state(--no-animate), :--no-animate))) { transition: ${c(`opacity ${j.motion.spring.fastEffects}, 
        transform ${j.motion.spring.fastSpatial},
        overlay ${j.motion.spring.fastEffects} allow-discrete,
        display ${j.motion.spring.fastEffects} allow-discrete`)}; } .base { display: contents; } :host(:is(:state(--primary), :--primary)) .base { --_fab-menu-item-color: var(--m3e-primary-fab-color, ${j.color.onPrimaryContainer}); --_fab-menu-item-container-color: var(--m3e-primary-fab-container-color, ${j.color.primaryContainer}); --_fab-menu-background-hover-color: var(--m3e-primary-fab-hover-color, ${j.color.onPrimaryContainer}); --_fab-menu-background-focus-color: var(--m3e-primary-fab-focus-color, ${j.color.onPrimaryContainer}); --_fab-menu-ripple-color: var(--m3e-primary-fab-ripple-color, ${j.color.onPrimaryContainer}); } :host(:is(:state(--secondary), :--secondary)) .base { --_fab-menu-item-color: var(--m3e-secondary-fab-color, ${j.color.onSecondaryContainer}); --_fab-menu-item-container-color: var( --m3e-secondary-fab-container-color, ${j.color.secondaryContainer} ); --_fab-menu-background-hover-color: var( --m3e-secondary-fab-hover-color, ${j.color.onSecondaryContainer} ); --_fab-menu-background-focus-color: var( --m3e-secondary-fab-focus-color, ${j.color.onSecondaryContainer} ); --_fab-menu-ripple-color: var(--m3e-secondary-fab-ripple-color, ${j.color.onSecondaryContainer}); } :host(:is(:state(--tertiary), :--tertiary)) .base { --_fab-menu-item-color: var(--m3e-tertiary-fab-color, ${j.color.onTertiaryContainer}); --_fab-menu-item-container-color: var(--m3e-tertiary-fab-container-color, ${j.color.tertiaryContainer}); --_fab-menu-background-hover-color: var(--m3e-tertiary-fab-hover-color, ${j.color.onTertiaryContainer}); --_fab-menu-background-focus-color: var(--m3e-tertiary-fab-focus-color, ${j.color.onTertiaryContainer}); --_fab-menu-ripple-color: var(--m3e-tertiary-fab-ripple-color, ${j.color.onTertiaryContainer}); } :host { transform: scaleX(0); } :host(:is(:state(--left), :--left)) { align-items: flex-start; transform-origin: left; } :host(:is(:state(--right), :--right)) { align-items: flex-end; transform-origin: right; } :host(:popover-open) { transform: scaleX(1); display: inline-flex; opacity: 1; } :host::backdrop { background-color: transparent; } @starting-style { :host(:popover-open) { opacity: 0; } :host(:popover-open) { transform: scaleX(0); } } @media (prefers-reduced-motion) { :host(:not(:is(:state(--no-animate), :--no-animate))) { transition: none; } } @media (forced-colors: active) { :host { border-radius: ${j.shape.corner.medium}; outline: 1px solid MenuText; background-color: Menu; } }`, e([C({
	reflect: !0,
	useDefault: !0
})], Dp.prototype, "variant", void 0), Dp = e([T("m3e-fab-menu")], Dp);
var Op, kp, Ap, jp = class extends ji(Fi(ni(E(M(S, "menuitem"), !0)))) {
	constructor() {
		super(...arguments), Op.add(this), kp.set(this, (e) => t(this, Op, "m", Ap).call(this, e));
	}
	get menu() {
		return this.closest("m3e-fab-menu");
	}
	connectedCallback() {
		super.connectedCallback(), this.addEventListener("click", t(this, kp, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("click", t(this, kp, "f"));
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
		return b`<div class="base"><m3e-elevation class="elevation" ?disabled="${this.disabled}"></m3e-elevation><m3e-state-layer class="state-layer" ?disabled="${this.disabled}"></m3e-state-layer><m3e-focus-ring class="focus-ring" ?disabled="${this.disabled}"></m3e-focus-ring><m3e-ripple class="ripple" ?disabled="${this.disabled}"></m3e-ripple><div class="touch" aria-hidden="true"></div>${this[Mi]()}<div class="wrapper"><slot class="icon" name="icon" aria-hidden="true"></slot><div class="label"><slot></slot></div></div></div>`;
	}
};
kp = /* @__PURE__ */ new WeakMap(), Op = /* @__PURE__ */ new WeakSet(), Ap = function(e) {
	e.defaultPrevented || this.menu?.hide(!0);
}, jp.styles = l`:host { display: inline-block; outline: none; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } :host([hidden]) { display: none; } .base { box-sizing: border-box; vertical-align: middle; display: inline-flex; align-items: center; justify-content: center; position: relative; width: 100%; transition: ${c(`background-color ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; min-height: var(--m3e-fab-menu-item-height, 56px); font-size: var(--m3e-fab-menu-item-font-size, ${j.typescale.standard.label.large.fontSize}); font-weight: var(--m3e-fab-menu-item-font-weight, ${j.typescale.standard.label.large.fontWeight}); line-height: var(--m3e-fab-menu-item-line-height, ${j.typescale.standard.label.large.lineHeight}); letter-spacing: var(--m3e-fab-menu-item-tracking, ${j.typescale.standard.label.large.tracking}); border-radius: var(--m3e-fab-menu-item-shape, ${j.shape.corner.full}); } :host(:not(:disabled)) .label, :host(:not(:disabled)) .icon { color: var(--_fab-menu-item-color); } :host(:not(:disabled)) .base { background-color: var(--_fab-menu-item-container-color); --m3e-state-layer-hover-color: var(--_fab-menu-background-hover-color); --m3e-state-layer-focus-color: var(--_fab-menu-background-focus-color); --m3e-ripple-color: var(--_fab-menu-ripple-color); } :host(:disabled) .base { background-color: color-mix( in srgb, var(--m3e-fab-menu-item-disabled-container-color, ${j.color.onSurface}) var(--m3e-fab-menu-item-disabled-container-opacity, 10%), transparent ); } :host(:disabled) .label, :host(:disabled) .icon { color: color-mix( in srgb, var(--m3e-fab-menu-item-disabled-color, ${j.color.onSurface}) var(--m3e-fab-menu-item-disabled-opacity, 38%), transparent ); } .touch { position: absolute; height: 48px; left: 0; right: 0; } .wrapper { width: 100%; overflow: hidden; display: inline-flex; align-items: center; padding-block-start: var(--m3e-fab-menu-item-top-space, ${j.measurement.space200}); padding-block-end: var(--m3e-fab-menu-item-bottom-space, ${j.measurement.space200}); padding-inline-start: var(--m3e-fab-menu-item-leading-space, ${j.measurement.space300}); padding-inline-end: var(--m3e-fab-menu-item-trailing-space, ${j.measurement.space300}); column-gap: var(--m3e-fab-menu-item-spacing, ${j.measurement.space100}); } .label { justify-self: center; flex: 1 1 auto; text-align: center; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; transition: ${c(`color ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; } .icon { font-size: var(--m3e-fab-menu-item-icon-size, 24px); transition: ${c(`color ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; } :host(:not(:disabled)) { cursor: pointer; } ::slotted([slot="icon"]) { font-size: inherit !important; flex: none; } ::slotted(svg[slot="icon"]) { width: 1em; height: 1em; } a { all: unset; display: block; position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px; z-index: 1; } @media (prefers-reduced-motion) { .base, .label, .icon { transition: none; } } @media (forced-colors: active) { .base, .label, .icon { transition: none; } :host(:not(:disabled)) .base { background-color: Menu; } :host(:not(:disabled)) .label, :host(:not(:disabled)) .icon { color: MenuText; } :host(:disabled) .label, :host(:disabled) .icon { color: GrayText; } }`, e([w(".elevation")], jp.prototype, "_elevation", void 0), e([w(".focus-ring")], jp.prototype, "_focusRing", void 0), e([w(".state-layer")], jp.prototype, "_stateLayer", void 0), e([w(".ripple")], jp.prototype, "_ripple", void 0), jp = e([T("m3e-fab-menu-item")], jp);
var Mp = class extends Ai(Wi) {
	get menu() {
		return this.control instanceof Dp ? this.control : null;
	}
	attach(e) {
		super.attach(e), this.parentElement && e instanceof Dp && (this.parentElement.ariaHasPopup = "menu", this.parentElement.ariaExpanded = "false", e.id && Cu(this.parentElement, "aria-controls", e.id));
	}
	detach() {
		this.parentElement && (this.parentElement.ariaHasPopup = null, this.parentElement.ariaExpanded = null, this.control?.id && wu(this.parentElement, "aria-controls", this.control.id)), super.detach();
	}
	_onClick() {
		this.parentElement && this.menu?.toggle(this.parentElement);
	}
};
Mp = e([T("m3e-fab-menu-trigger")], Mp);
//#endregion
//#region node_modules/@m3e/web/dist/card.js
function Np(e) {
	return e === "elevated" || e === "filled" || e === "outlined";
}
var Pp = {
	padding: c(`var(--m3e-card-padding, ${j.measurement.space200})`),
	shape: c(`var(--m3e-card-shape, ${j.shape.corner.medium});`)
}, J = {
	filled: {
		textColor: c(`var(--m3e-filled-card-text-color, var(--m3e-card-text-color, ${j.color.onSurface}))`),
		containerColor: c(`var(--m3e-filled-card-container-color, var(--m3e-card-container-color, ${j.color.surfaceContainerHighest}))`),
		containerElevation: c(`var(--m3e-filled-card-container-elevation, var(--m3e-card-container-elevation, ${j.elevation.level0}))`),
		disabled: {
			textColor: c(`var(--m3e-filled-card-disabled-text-color, var(--m3e-card-disabled-text-color, ${j.color.onSurface}))`),
			textOpacity: c("var(--m3e-filled-card-disabled-text-opacity, var(--m3e-card-disabled-text-opacity, 38%))"),
			imageOpacity: c("var(--m3e-filled-card-disabled-image-opacity, var(--m3e-card-disabled-image-opacity, 38%))"),
			containerColor: c(`var(--m3e-filled-card-disabled-container-color, var(--m3e-card-disabled-container-color, ${j.color.surfaceVariant}))`),
			containerElevation: c(`var(--m3e-filled-card-disabled-container-elevation, var(--m3e-card-disabled-container-elevation, ${j.elevation.level0}))`),
			containerElevationColor: c(`var(--m3e-filled-card-disabled-container-elevation-color, var(--m3e-card-disabled-container-elevation-color, ${j.color.onSurface}))`),
			containerElevationOpacity: c("var(--m3e-filled-card-disabled-container-elevation-opacity, var(--m3e-card-disabled-container-elevation-opacity, 38%))"),
			containerOpacity: c("var(--m3e-filled-card-disabled-container-opacity, var(--m3e-card-disabled-container-opacity, 38%))")
		},
		hover: {
			textColor: c(`var(--m3e-filled-card-hover-text-color, var(--m3e-card-hover-text-color, ${j.color.onSurface}))`),
			stateLayerColor: c(`var(--m3e-filled-card-hover-state-layer-color, var(--m3e-card-hover-state-layer-color, ${j.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-filled-card-hover-state-layer-opacity, var(--m3e-card-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-filled-card-hover-container-elevation, var(--m3e-card-hover-container-elevation, ${j.elevation.level1}))`)
		},
		focus: {
			textColor: c(`var(--m3e-filled-card-focus-text-color, var(--m3e-card-focus-text-color, ${j.color.onSurface}))`),
			stateLayerColor: c(`var(--m3e-filled-card-focus-state-layer-color, var(--m3e-card-focus-state-layer-color, ${j.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-filled-card-focus-state-layer-opacity, var(--m3e-card-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-filled-card-focus-container-elevation, var(--m3e-card-focus-container-elevation, ${j.elevation.level0}))`)
		},
		pressed: {
			textColor: c(`var(--m3e-filled-card-pressed-text-color, var(--m3e-card-pressed-text-color, ${j.color.onSurface}))`),
			stateLayerColor: c(`var(--m3e-filled-card-pressed-state-layer-color, var(--m3e-card-pressed-state-layer-color, ${j.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-filled-card-pressed-state-layer-opacity, var(--m3e-card-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-filled-card-pressed-container-elevation, var(--m3e-card-pressed-container-elevation, ${j.elevation.level0}))`)
		}
	},
	elevated: {
		textColor: c(`var(--m3e-elevated-card-text-color, var(--m3e-card-text-color, ${j.color.onSurface}))`),
		containerColor: c(`var(--m3e-elevated-card-container-color, var(--m3e-card-container-color, ${j.color.surfaceContainerLow}))`),
		containerElevation: c(`var(--m3e-elevated-card-container-elevation, var(--m3e-card-container-elevation, ${j.elevation.level1}))`),
		disabled: {
			textColor: c(`var(--m3e-elevated-card-disabled-text-color, var(--m3e-card-disabled-text-color, ${j.color.onSurface}))`),
			textOpacity: c("var(--m3e-elevated-card-disabled-text-opacity, var(--m3e-card-disabled-text-opacity, 38%))"),
			imageOpacity: c("var(--m3e-elevated-card-disabled-image-opacity, var(--m3e-card-disabled-image-opacity, 38%))"),
			containerColor: c(`var(--m3e-elevated-card-disabled-container-color, var(--m3e-card-disabled-container-color, ${j.color.surface}))`),
			containerElevation: c(`var(--m3e-elevated-card-disabled-container-elevation, var(--m3e-card-disabled-container-elevation, ${j.elevation.level1}))`),
			containerElevationColor: c(`var(--m3e-elevated-card-disabled-container-elevation-color, var(--m3e-card-disabled-container-elevation-color, ${j.color.onSurface}))`),
			containerElevationOpacity: c("var(--m3e-elevated-card-disabled-container-elevation-opacity, var(--m3e-card-disabled-container-elevation-opacity, 38%))"),
			containerOpacity: c("var(--m3e-elevated-card-disabled-container-opacity, var(--m3e-card-disabled-container-opacity, 38%))")
		},
		hover: {
			textColor: c(`var(--m3e-elevated-card-hover-text-color, var(--m3e-card-hover-text-color, ${j.color.onSurface}))`),
			stateLayerColor: c(`var(--m3e-elevated-card-hover-state-layer-color, var(--m3e-card-hover-state-layer-color, ${j.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-elevated-card-hover-state-layer-opacity, var(--m3e-card-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-elevated-card-hover-container-elevation, var(--m3e-card-hover-container-elevation, ${j.elevation.level2}))`)
		},
		focus: {
			textColor: c(`var(--m3e-elevated-card-focus-text-color, var(--m3e-card-focus-text-color, ${j.color.onSurface}))`),
			stateLayerColor: c(`var(--m3e-elevated-card-focus-state-layer-color, var(--m3e-card-focus-state-layer-color, ${j.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-elevated-card-focus-state-layer-opacity, var(--m3e-card-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-elevated-card-focus-container-elevation, var(--m3e-card-focus-container-elevation, ${j.elevation.level1}))`)
		},
		pressed: {
			textColor: c(`var(--m3e-elevated-card-pressed-text-color, var(--m3e-card-pressed-text-color, ${j.color.onSurface}))`),
			stateLayerColor: c(`var(--m3e-elevated-card-pressed-state-layer-color, var(--m3e-card-pressed-state-layer-color, ${j.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-elevated-card-pressed-state-layer-opacity, var(--m3e-card-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-elevated-card-pressed-container-elevation, var(--m3e-card-pressed-container-elevation, ${j.elevation.level1}))`)
		}
	},
	outlined: {
		textColor: c(`var(--m3e-outlined-card-text-color, var(--m3e-card-text-color, ${j.color.onSurface}))`),
		containerColor: c(`var(--m3e-outlined-card-container-color, var(--m3e-card-container-color, ${j.color.surface}))`),
		containerElevation: c(`var(--m3e-outlined-card-container-elevation, var(--m3e-card-container-elevation, ${j.elevation.level0}))`),
		outlineColor: c(`var(--m3e-outlined-card-outline-color, var(--m3e-card-outline-color, ${j.color.outlineVariant}))`),
		outlineThickness: c("var(--m3e-outlined-card-outline-thickness, var(--m3e-card-outline-thickness, 1px))"),
		disabled: {
			textColor: c(`var(--m3e-outlined-card-disabled-text-color, var(--m3e-card-disabled-text-color, ${j.color.onSurface}))`),
			textOpacity: c("var(--m3e-outlined-card-disabled-text-opacity, var(--m3e-card-disabled-text-opacity, 38%))"),
			imageOpacity: c("var(--m3e-outlined-card-disabled-image-opacity, var(--m3e-card-disabled-image-opacity, 38%))"),
			containerElevation: c(`var(--m3e-outlined-card-disabled-container-elevation, var(--m3e-card-disabled-container-elevation, ${j.elevation.level0}))`),
			containerElevationColor: c(`var(--m3e-outlined-card-disabled-container-elevation-color, var(--m3e-card-disabled-container-elevation-color, ${j.color.onSurface}))`),
			containerElevationOpacity: c("var(--m3e-outlined-card-disabled-container-elevation-opacity, var(--m3e-card-disabled-container-elevation-opacity, 38%))"),
			outlineColor: c(`var(--m3e-outlined-card-disabled-outline-color, var(--m3e-card-disabled-outline-color, ${j.color.outline}))`),
			outlineOpacity: c("var(--m3e-outlined-card-disabled-outline-opacity, var(--m3e-card-disabled-outline-opacity, 12%))")
		},
		hover: {
			textColor: c(`var(--m3e-outlined-card-hover-text-color, var(--m3e-card-hover-text-color, ${j.color.onSurface}))`),
			stateLayerColor: c(`var(--m3e-outlined-card-hover-state-layer-color, var(--m3e-card-hover-state-layer-color, ${j.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-outlined-card-hover-state-layer-opacity, var(--m3e-card-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-outlined-card-hover-container-elevation, var(--m3e-card-hover-container-elevation, ${j.elevation.level1}))`),
			outlineColor: c(`var(--m3e-outlined-card-hover-outline-color, var(--m3e-card-hover-outline-color, ${j.color.outlineVariant}))`)
		},
		focus: {
			textColor: c(`var(--m3e-outlined-card-focus-text-color, var(--m3e-card-focus-text-color, ${j.color.onSurface}))`),
			stateLayerColor: c(`var(--m3e-outlined-card-focus-state-layer-color, var(--m3e-card-focus-state-layer-color, ${j.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-outlined-card-focus-state-layer-opacity, var(--m3e-card-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-outlined-card-focus-container-elevation, var(--m3e-card-focus-container-elevation, ${j.elevation.level0}))`),
			outlineColor: c(`var(--m3e-outlined-card-focus-outline-color, var(--m3e-card-focus-outline-color, ${j.color.onSurface}))`)
		},
		pressed: {
			textColor: c(`var(--m3e-outlined-card-pressed-text-color, var(--m3e-card-pressed-text-color, ${j.color.onSurface}))`),
			stateLayerColor: c(`var(--m3e-outlined-card-pressed-state-layer-color, var(--m3e-card-pressed-state-layer-color, ${j.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-outlined-card-pressed-state-layer-opacity, var(--m3e-card-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			containerElevation: c(`var(--m3e-outlined-card-pressed-container-elevation, var(--m3e-card-pressed-container-elevation, ${j.elevation.level0}))`),
			outlineColor: c(`var(--m3e-outlined-card-pressed-outline-color, var(--m3e-card-pressed-outline-color, ${j.color.outlineVariant}))`)
		}
	}
}, Fp = l`:host { outline: none; } :host([hidden]) { display: none; } :host(:not([inline])) { display: block; } :host(:not([inline])) .base { display: flex; } :host([inline]) { display: inline-block; vertical-align: middle; } :host([inline]) .base { display: inline-flex; } .base { width: 100%; height: 100%; position: relative; box-sizing: border-box; border-radius: ${Pp.shape}; } :host([actionable]) .base { transition: ${c(`background-color ${j.motion.duration.short4} ${j.motion.easing.standard},
      border-color ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; } :host([actionable]) { user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } :host([actionable]:not(:disabled):not([disabled-interactive])) { cursor: pointer; } :host([actionable][disabled-interactive]) { cursor: not-allowed; } :host(:not([actionable])) .focus-ring, :host(:not([actionable])) .state-layer, :host(:not([actionable])) .ripple { display: none; } :host(:is(:state(--outlined), :--outlined)) .base { border-style: solid; } ::slotted([slot="content"]) { flex: 1 1 auto; } ::slotted(img), ::slotted(video) { inset: 0; object-fit: cover; } ::slotted(img[slot="header"]), ::slotted(video[slot="header"]) { border-radius: ${Pp.shape}; } ::slotted([slot="actions"][end]) { justify-content: flex-end; } ::slotted([slot="header"]), ::slotted([slot="actions"]), ::slotted([slot="footer"]) { flex: none; display: flex; align-items: center; } :host(:is(:state(--vertical), :--vertical)) .base, :host(:is(:state(--horizontal), :--horizontal)) ::slotted([slot="header"]), :host(:is(:state(--horizontal), :--horizontal)) ::slotted([slot="actions"]), :host(:is(:state(--horizontal), :--horizontal)) ::slotted([slot="footer"]) { flex-direction: column; } :host(:is(:state(--horizontal), :--horizontal)) .base, :host(:is(:state(--vertical), :--vertical)) ::slotted([slot="header"]), :host(:is(:state(--vertical), :--vertical)) ::slotted([slot="actions"]), :host(:is(:state(--vertical), :--vertical)) ::slotted([slot="footer"]) { flex-direction: row; } :host(:is(:state(--horizontal), :--horizontal)) ::slotted(img), :host(:is(:state(--horizontal), :--horizontal)) ::slotted(video) { aspect-ratio: 16 / 9; } .has-content:not(.has-default) slot[name="content"], .has-content.has-default slot:not([name]) { display: inherit; flex-direction: inherit; flex: 1 1 auto; } .has-header slot[name="header"], .has-actions slot[name="actions"], .has-footer slot[name="footer"] { display: inherit; flex-direction: inherit; flex: none; } :host(:is(:state(--vertical), :--vertical)) .has-content:not(.has-default) slot[name="content"] { margin-inline: ${Pp.padding}; } :host(:is(:state(--vertical), :--vertical)) .has-content:not(.has-default):not(.has-header) slot[name="content"] { margin-block-start: ${Pp.padding}; } :host(:is(:state(--vertical), :--vertical)) .has-content:not(.has-default):not(.has-actions):not(.has-footer) slot[name="content"] { margin-block-end: ${Pp.padding}; } :host(:is(:state(--horizontal), :--horizontal)) .has-content:not(.has-default) slot[name="content"] { margin-block: ${Pp.padding}; } :host(:is(:state(--horizontal), :--horizontal)) .has-content:not(.has-default):not(.has-header) slot[name="content"] { margin-inline-start: ${Pp.padding}; } :host(:is(:state(--horizontal), :--horizontal)) .has-content:not(.has-default):not(.has-actions):not(.has-footer) slot[name="content"] { margin-inline-end: ${Pp.padding}; } :host(:is(:state(--vertical), :--vertical)) .has-header:not(.has-header-media) slot[name="header"] { margin-inline: ${Pp.padding}; margin-block-start: ${Pp.padding}; } :host(:is(:state(--horizontal), :--horizontal)) .has-header:not(.has-header-media) slot[name="header"] { margin-inline-start: ${Pp.padding}; margin-block: ${Pp.padding}; } .has-actions slot[name="actions"] { margin-inline: ${Pp.padding}; margin-block: ${Pp.padding}; } :host(:is(:state(--vertical), :--vertical)) .has-footer slot[name="footer"] { margin-inline: ${Pp.padding}; margin-block-end: ${Pp.padding}; } :host(:is(:state(--horizontal), :--horizontal)) .has-footer slot[name="footer"] { margin-block: ${Pp.padding}; margin-inline-end: ${Pp.padding}; } a { all: unset; display: block; position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px; z-index: 1; } @media (forced-colors: active) { :host([actionable]) .base { transition: none; } .base { border-style: solid !important; border-color: CanvasText !important; border-width: ${J.outlined.outlineThickness ?? c("unset")} !important; } :host([actionable]:disabled) .base, :host([actionable][disabled-interactive]) .base { color: GrayText !important; border-color: GrayText !important; } } @media (prefers-reduced-motion) { :host([actionable]) .base { transition: none; } }`;
function Ip(e) {
	let t = c(`:is(:state(--${e}), :--${e})`);
	return l`:host(${t}) .base { background-color: ${J[e].containerColor ?? c("unset")}; box-shadow: ${J[e].containerElevation ?? c("unset")}; border-width: ${J[e].outlineThickness ?? c("unset")}; } :host([actionable]${t}) .base { --m3e-state-layer-hover-color: ${J[e].hover.stateLayerColor}; --m3e-state-layer-hover-opacity: ${J[e].hover.stateLayerOpacity}; --m3e-state-layer-focus-color: ${J[e].focus.stateLayerColor}; --m3e-state-layer-focus-opacity: ${J[e].focus.stateLayerOpacity}; --m3e-ripple-color: ${J[e].pressed.stateLayerColor}; --m3e-ripple-opacity: ${J[e].pressed.stateLayerOpacity}; --m3e-elevation-level: ${J[e].containerElevation ?? c("unset")}; --m3e-elevation-hover-level: ${J[e].hover.containerElevation ?? c("unset")}; --m3e-elevation-focus-level: ${J[e].focus.containerElevation ?? c("unset")}; --m3e-elevation-pressed-level: ${J[e].pressed.containerElevation ?? c("unset")}; } :host(${t}) .base { border-color: ${J[e].outlineColor ?? c("unset")}; } :host([actionable]${t}:focus .base) { border-color: ${J[e].focus.outlineColor ?? c("unset")}; } :host([actionable]${t}:hover .base) { border-color: ${J[e].hover.outlineColor ?? c("unset")}; } :host([actionable]${t}) .base.pressed { border-color: ${J[e].pressed.outlineColor ?? c("unset")}; } :host(${t}) .base { color: ${J[e].textColor ?? c("unset")}; } :host([actionable]${t}:focus) .base { color: ${J[e].focus.textColor ?? c("unset")}; } :host([actionable]${t}:hover) .base { color: ${J[e].hover.textColor ?? c("unset")}; } :host([actionable]${t}) .base.pressed { color: ${J[e].pressed.textColor ?? c("unset")}; } :host([actionable]${t}:disabled) .base, :host([actionable]${t}[disabled-interactive]) .base { --m3e-elevation-level: ${J[e].disabled.containerElevation ?? c("unset")}; --m3e-elevation-color: color-mix( in srgb, ${J[e].disabled.containerElevationColor} ${J[e].disabled.containerElevationOpacity}, transparent ); color: color-mix( in srgb, ${J[e].disabled.textColor} ${J[e].disabled.textOpacity}, transparent ); background-color: ${J[e].disabled.containerColor && J[e].disabled.containerOpacity ? c(`color-mix(
        in srgb,
        ${J[e].disabled.containerColor} ${J[e].disabled.containerOpacity},
        transparent
      )`) : c("unset")}; border-color: ${J[e].disabled.outlineColor && J[e].disabled.outlineOpacity ? c(`color-mix(
        in srgb,
        ${J[e].disabled.outlineColor} ${J[e].disabled.outlineOpacity},
        transparent
      )`) : c("unset")}; } :host([actionable]${t}:disabled) ::slotted(img), :host([actionable]${t}[disabled-interactive]) ::slotted(img), :host([actionable]${t}:disabled) ::slotted(video), :host([actionable]${t}[disabled-interactive]) ::slotted(video) { opacity: ${J[e].disabled.imageOpacity}; }`;
}
var Lp = [
	Ip("filled"),
	Ip("elevated"),
	Ip("outlined")
];
function Rp(e) {
	return e === "horizontal" || e === "vertical";
}
var zp, Bp, Vp, Hp, Up, Wp, Gp, Kp, qp, Jp, Yp = class extends ji(Fi(Di(Ti(si(ni(E(S), !0)))))) {
	constructor() {
		super(), zp.add(this), Bp.set(this, (e) => t(this, zp, "m", Jp).call(this, e)), this.inline = !1, this.actionable = !1, this.variant = "filled", this.orientation = "vertical", new Yn(this, {
			isPressedKey: (e) => e === " ",
			callback: (e) => {
				this.actionable && !this.disabled && !this.disabledInteractive && this._base?.classList.toggle("pressed", e);
			}
		});
	}
	connectedCallback() {
		this.hasAttribute("actionable") && (this.role = "button"), super.connectedCallback(), this.addEventListener("click", t(this, Bp, "f")), t(this, zp, "m", Hp).call(this), t(this, zp, "m", Vp).call(this);
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this._base?.classList.toggle("pressed", !1), this.removeEventListener("click", t(this, Bp, "f"));
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, zp, "m", Vp).call(this), e.has("orientation") && t(this, zp, "m", Hp).call(this);
	}
	render() {
		return b`<div class="base"><m3e-elevation class="elevation" ?disabled="${!this.actionable || this.disabled || this.disabledInteractive}"></m3e-elevation><m3e-focus-ring class="focus-ring" ?disabled="${!this.actionable || this.disabled}"></m3e-focus-ring><m3e-state-layer class="state-layer" ?disabled="${!this.actionable || this.disabled || this.disabledInteractive}"></m3e-state-layer><m3e-ripple class="ripple" ?disabled="${!this.actionable || this.disabled || this.disabledInteractive}"></m3e-ripple>${this[Mi]()}<slot name="header" @slotchange="${t(this, zp, "m", Up)}"></slot><slot name="content" @slotchange="${t(this, zp, "m", Wp)}"><slot @slotchange="${t(this, zp, "m", Gp)}"></slot></slot><slot name="actions" @slotchange="${t(this, zp, "m", Kp)}"></slot><slot name="footer" @slotchange="${t(this, zp, "m", qp)}"></slot></div>`;
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
Bp = /* @__PURE__ */ new WeakMap(), zp = /* @__PURE__ */ new WeakSet(), Vp = function() {
	Np(this.variant) || (this.variant = "filled"), wr(this, this.variant, "elevated", "filled", "outlined");
}, Hp = function() {
	Rp(this.orientation) || (this.orientation = "vertical"), wr(this, this.orientation, "horizontal", "vertical");
}, Up = function(e) {
	let t = e.target.assignedNodes({ flatten: !0 }), n = this.shadowRoot?.querySelector(".base");
	n?.classList.toggle("has-header", t.length > 0), n?.classList.toggle("has-header-media", t.some((e) => e instanceof HTMLElement && (e.tagName === "IMG" || e.tagName === "VIDEO")));
}, Wp = function() {
	this.shadowRoot?.querySelector(".base")?.classList.toggle("has-content", this.querySelector(":scope > [slot='content']") !== null);
}, Gp = function(e) {
	this.shadowRoot?.querySelector(".base")?.classList.toggle("has-default", Ft(e.target) && this.querySelector(":scope > [slot='content']") === null);
}, Kp = function(e) {
	this.shadowRoot?.querySelector(".base")?.classList.toggle("has-actions", Ft(e.target));
}, qp = function(e) {
	this.shadowRoot?.querySelector(".base")?.classList.toggle("has-footer", Ft(e.target));
}, Jp = function(e) {
	(this.disabled || this.disabledInteractive) && (e.preventDefault(), e.stopImmediatePropagation());
}, Yp.styles = [Lp, Fp], e([w(".base")], Yp.prototype, "_base", void 0), e([w(".elevation")], Yp.prototype, "_elevation", void 0), e([w(".focus-ring")], Yp.prototype, "_focusRing", void 0), e([w(".state-layer")], Yp.prototype, "_stateLayer", void 0), e([w(".ripple")], Yp.prototype, "_ripple", void 0), e([C({
	type: Boolean,
	reflect: !0
})], Yp.prototype, "inline", void 0), e([C({
	type: Boolean,
	reflect: !0
})], Yp.prototype, "actionable", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], Yp.prototype, "variant", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], Yp.prototype, "orientation", void 0), Yp = e([T("m3e-card")], Yp);
//#endregion
//#region node_modules/@m3e/web/dist/list.js
function Xp(e) {
	return e === "standard" || e === "segmented";
}
var Zp, Qp, $p, em, tm, nm, rm, im, am = class extends Li(E(M(S, "listitem"))) {
	constructor() {
		super(...arguments), Zp.add(this), Qp.set(this, new tr(this, {
			target: null,
			callback: () => t(this, Zp, "m", rm).call(this)
		})), $p.set(this, void 0), em.set(this, void 0), tm.set(this, void 0);
	}
	get leadingContentType() {
		return t(this, em, "f");
	}
	get trailingContentType() {
		return t(this, tm, "f");
	}
	reconnectedCallback() {
		super.reconnectedCallback(), t(this, Zp, "m", nm).call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), t(this, Zp, "m", nm).call(this);
	}
	render() {
		return b`<div class="base">${this._renderBase()}</div>`;
	}
	_renderBase() {
		return b`<slot name="leading" @slotchange="${this._handleLeadingSlotChange}"><slot name="leading-icon" @slotchange="${this._handleLeadingSlotChange}"></slot></slot><div class="content"><slot name="overline"></slot><slot></slot><slot name="supporting-text"></slot></div><slot name="trailing" @slotchange="${this._handleTrailingSlotChange}"><slot name="trailing-supporting-text" @slotchange="${this._handleTrailingSlotChange}"></slot><slot name="trailing-icon" @slotchange="${this._handleTrailingSlotChange}"></slot></slot>`;
	}
	_handleLeadingSlotChange(e) {
		let r = t(this, em, "f");
		n(this, em, t(this, Zp, "m", im).call(this, e.target), "f"), O(this, "--has-leading", t(this, em, "f") !== void 0), r !== t(this, em, "f") && this.closest("m3e-list,m3e-action-list,m3e-selection-list")?.notifyLeadingContentTypeChange(r, t(this, em, "f"));
	}
	_handleTrailingSlotChange(e) {
		let r = t(this, tm, "f");
		n(this, tm, t(this, Zp, "m", im).call(this, e.target), "f"), O(this, "--has-trailing", t(this, tm, "f") !== void 0), r !== t(this, tm, "f") && this.closest("m3e-list,m3e-action-list,m3e-selection-list")?.notifyTrailingContentTypeChange(r, t(this, tm, "f"));
	}
};
Qp = /* @__PURE__ */ new WeakMap(), $p = /* @__PURE__ */ new WeakMap(), em = /* @__PURE__ */ new WeakMap(), tm = /* @__PURE__ */ new WeakMap(), Zp = /* @__PURE__ */ new WeakSet(), nm = function() {
	let e = this.shadowRoot?.querySelector(".content");
	e && t(this, Qp, "f").observe(e);
}, rm = function() {
	let e = this.shadowRoot?.querySelector(".content") ?? null, r = e === null ? 0 : yt(e);
	t(this, $p, "f") !== r && (n(this, $p, r, "f"), requestAnimationFrame(() => {
		O(this, "--one-line", r <= 1), O(this, "--two-line", r == 2), O(this, "--three-line", r > 2);
	}));
}, im = function(e) {
	let t = e.assignedElements({ flatten: !0 });
	return t.some((e) => e.tagName === "VIDEO") ? "video" : t.some((e) => e.tagName === "IMG") ? "image" : t.some((e) => e.tagName === "M3E-AVATAR") ? "avatar" : t.some((e) => e.tagName === "M3E-ICON") ? "icon" : t.length > 0 ? "text" : void 0;
}, am.styles = l`:host { display: block; } .base { display: flex; min-height: inherit; width: 100%; box-sizing: border-box; column-gap: var(--m3e-list-item-between-space, ${j.measurement.space200}); padding-inline-start: var(--m3e-list-item-leading-space, ${j.measurement.space200}); padding-inline-end: var(--m3e-list-item-trailing-space, ${j.measurement.space200}); border-top-left-radius: var( --_list-item-top-container-shape, var(--m3e-list-item-container-shape, ${j.shape.corner.none}) ); border-top-right-radius: var( --_list-item-top-container-shape, var(--m3e-list-item-container-shape, ${j.shape.corner.none}) ); border-bottom-left-radius: var( --_list-item-bottom-container-shape, var(--m3e-list-item-container-shape, ${j.shape.corner.none}) ); border-bottom-right-radius: var( --_list-item-bottom-container-shape, var(--m3e-list-item-container-shape, ${j.shape.corner.none}) ); transition: ${c(`border-radius ${j.motion.spring.fastEffects}, background-color ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; } :host(:is(:state(--one-line), :--one-line)) { min-height: calc(var(--m3e-list-item-one-line-height, 56px) + ${j.density.calc(-3)}); } :host(:is(:state(--one-line), :--one-line)) .base { padding-block-start: var(--m3e-list-item-one-line-top-space, ${j.measurement.space100}); padding-block-end: var(--m3e-list-item-one-line-bottom-space, ${j.measurement.space100}); } :host(:is(:state(--two-line), :--two-line)) { min-height: calc(var(--m3e-list-item-two-line-height, 72px) + ${j.density.calc(-3)}); } :host(:is(:state(--two-line), :--two-line)) .base { padding-block-start: var(--m3e-list-item-two-line-top-space, ${j.measurement.space100}); padding-block-end: var(--m3e-list-item-two-line-bottom-space, ${j.measurement.space100}); } :host(:is(:state(--three-line), :--three-line)) { min-height: calc(var(--m3e-list-item-three-line-height, 88px) + ${j.density.calc(-3)}); } :host(:is(:state(--three-line), :--three-line)) .base { padding-block-start: var(--m3e-list-item-three-line-top-space, ${j.measurement.space150}); padding-block-end: var(--m3e-list-item-three-line-bottom-space, ${j.measurement.space150}); } :host(:not(:is(:state(--three-line), :--three-line))) .base { align-items: center; } :host(:is(:state(--three-line), :--three-line)) .base { align-items: flex-start; } :host(:not(:disabled):not([selected])) .base:not(.hover).focus-visible:not(.pressed) { border-top-left-radius: var( --_list-item-top-container-shape, var(--m3e-list-item-focus-container-shape, ${j.shape.corner.large}) ); border-top-right-radius: var( --_list-item-top-container-shape, var(--m3e-list-item-focus-container-shape, ${j.shape.corner.large}) ); border-bottom-left-radius: var( --_list-item-bottom-container-shape, var(--m3e-list-item-focus-container-shape, ${j.shape.corner.large}) ); border-bottom-right-radius: var( --_list-item-bottom-container-shape, var(--m3e-list-item-focus-container-shape, ${j.shape.corner.large}) ); } :host(:not(:disabled):not([selected])) .base.hover, :host(:not(:disabled):not([selected])) .base.pressed { border-top-left-radius: var( --_list-item-top-container-shape, var(--m3e-list-item-hover-container-shape, ${j.shape.corner.medium}) ); border-top-right-radius: var( --_list-item-top-container-shape, var(--m3e-list-item-hover-container-shape, ${j.shape.corner.medium}) ); border-bottom-left-radius: var( --_list-item-bottom-container-shape, var(--m3e-list-item-hover-container-shape, ${j.shape.corner.medium}) ); border-bottom-right-radius: var( --_list-item-bottom-container-shape, var(--m3e-list-item-hover-container-shape, ${j.shape.corner.medium}) ); } .content { flex: 1 1 auto; display: flex; flex-direction: column; align-items: unset; justify-content: unset; } ::slotted([slot="overline"]) { font-size: var(--m3e-list-item-overline-font-size, ${j.typescale.standard.label.small.fontSize}); font-weight: var(--m3e-list-item-overline-font-weight, ${j.typescale.standard.label.small.fontWeight}); line-height: var(--m3e-list-item-overline-line-height, ${j.typescale.standard.label.small.lineHeight}); letter-spacing: var(--m3e-list-item-overline-tracking, ${j.typescale.standard.label.small.tracking}); } ::slotted([slot="supporting-text"]) { font-size: var(--m3e-list-item-supporting-text-font-size, ${j.typescale.standard.body.medium.fontSize}); font-weight: var( --m3e-list-item-supporting-text-font-weight, ${j.typescale.standard.body.medium.fontWeight} ); line-height: var( --m3e-list-item-supporting-text-line-height, ${j.typescale.standard.body.medium.lineHeight} ); letter-spacing: var( --m3e-list-item-supporting-text-tracking, ${j.typescale.standard.body.medium.tracking} ); } ::slotted(:not([slot])) { font-size: var(--m3e-list-item-font-size, ${j.typescale.standard.body.large.fontSize}); font-weight: var(--m3e-list-item-font-weight, ${j.typescale.standard.body.large.fontWeight}); line-height: var(--m3e-list-item-line-height, ${j.typescale.standard.body.large.lineHeight}); letter-spacing: var(--m3e-list-item-tracking, ${j.typescale.standard.body.large.tracking}); } :host(:is(:state(--has-leading), :--has-leading)) slot[name="leading"], :host(:is(:state(--has-trailing), :--has-trailing)) slot[name="trailing"] { display: flex; justify-content: center; } :host(:not(:is(:state(--has-leading), :--has-leading))) slot[name="leading"] { display: var(--_list-item-leading-reserved-display, contents); } :host(:not(:is(:state(--has-trailing), :--has-trailing))) slot[name="trailing"] { display: var(--_list-item-trailing-reserved-display, contents); } slot[name="leading"] { min-width: var(--_list-item-leading-reserved-space, 0px); margin-inline-start: calc(0px - var(--_list-item-leading-reserved-outset, 0px)); } slot[name="trailing"] { min-width: var(--_list-item-trailing-reserved-space, 0px); margin-inline-end: calc(0px - var(--_list-item-trailing-reserved-outset, 0px)); } slot[name="leading"], slot[name="leading"] { --_icon-button-size: auto; --_icon-button-min-size: 48px; } ::slotted(span[slot="trailing"]), ::slotted([slot="trailing-supporting-text"]) { white-space: nowrap; font-size: var(--m3e-list-item-trailing-text-font-size, ${j.typescale.standard.label.small.fontSize}); font-weight: var( --m3e-list-item-trailing-text-font-weight, ${j.typescale.standard.label.small.fontWeight} ); line-height: var( --m3e-list-item-trailing-text-line-height, ${j.typescale.standard.label.small.lineHeight} ); letter-spacing: var( --m3e-list-item-trailing-text-tracking, ${j.typescale.standard.label.small.tracking} ); } ::slotted(video[slot="leading"]), ::slotted(video[slot="trailing"]), ::slotted(img[slot="leading"]), ::slotted(img[slot="trailing"]) { display: block; margin: 0 auto; overflow: hidden; object-fit: cover; } ::slotted(video) { width: var(--m3e-list-item-video-width, 100px); height: var(--m3e-list-item-video-height, 56px); } ::slotted(video[slot="leading"]), ::slotted(video[slot="trailing"]) { border-radius: var(--m3e-list-item-video-shape, ${j.shape.corner.none}); } ::slotted(video[slot="leading"]) { margin-inline-start: calc(0px - var(--_list-item-leading-video-outset, 0px)); } ::slotted(video[slot="trailing"]) { margin-inline-end: calc(0px - var(--_list-item-trailing-video-outset, 0px)); } ::slotted(img) { user-drag: none; user-select: none; pointer-events: none; width: var(--m3e-list-item-image-width, 56px); height: var(--m3e-list-item-image-height, 56px); } ::slotted(img[slot="leading"]), ::slotted(img[slot="trailing"]) { border-radius: var(--m3e-list-item-image-shape, ${j.shape.corner.none}); } ::slotted(m3e-icon[slot="leading"]), ::slotted(m3e-icon[slot="leading-icon"]), ::slotted(m3e-icon[slot="trailing"]), ::slotted(m3e-icon[slot="trailing-icon"]) { --m3e-icon-size: var(--m3e-list-item-icon-size, 24px); } :host(:not(:disabled)) ::slotted(m3e-icon[slot="leading"]), :host(:not(:disabled)) ::slotted(m3e-icon[slot="leading-icon"]) { color: var(--m3e-list-item-leading-color, ${j.color.onSurfaceVariant}); } :host(:not(:disabled)) .content { color: var(--m3e-list-item-label-text-color, ${j.color.onSurface}); } :host(:not(:disabled)) ::slotted([slot="overline"]) { color: var(--m3e-list-item-overline-color, ${j.color.onSurfaceVariant}); } :host(:not(:disabled)) ::slotted([slot="supporting-text"]) { color: var(--m3e-list-item-supporting-text-color, ${j.color.onSurfaceVariant}); } :host(:not(:disabled)) ::slotted([slot="trailing"]), :host(:not(:disabled)) ::slotted([slot="trailing-supporting-text"]), :host(:not(:disabled)) ::slotted([slot="trailing-icon"]) { color: var(--m3e-list-item-trailing-color, ${j.color.onSurfaceVariant}); } :host(:not(:disabled)) .base { background-color: var( --_list-item-container-color, var(--m3e-list-item-container-color, ${j.color.surface}) ); } :host(:disabled) .base { background-color: var(--m3e-list-item-disabled-container-color, transparent); } :host(:disabled) ::slotted(video), :host(:disabled) ::slotted(img), :host(:disabled) ::slotted(m3e-avatar) { opacity: var(--m3e-list-item-disabled-media-opacity, 38%); } :host(:disabled) .content { color: color-mix( in srgb, var(--m3e-list-item-disabled-label-text-color, ${j.color.onSurface}) var(--m3e-list-item-disabled-label-text-opacity, 38%), transparent ); } :host(:disabled) ::slotted([slot="overline"]) { color: color-mix( in srgb, var(--m3e-list-item-disabled-overline-color, ${j.color.onSurface}) var(--m3e-list-item-disabled-overline-opacity, 38%), transparent ); } :host(:disabled) ::slotted([slot="supporting-text"]) { color: color-mix( in srgb, var(--m3e-list-item-disabled-supporting-text-color, ${j.color.onSurface}) var(--m3e-list-item-disabled-supporting-text-opacity, 38%), transparent ); } :host(:disabled) ::slotted([slot="leading"]), :host(:disabled) ::slotted([slot="leading-icon"]) { color: color-mix( in srgb, var(--m3e-list-item-disabled-leading-color, ${j.color.onSurface}) var(--m3e-list-item-disabled-leading-opacity, 38%), transparent ); } :host(:disabled) ::slotted([slot="trailing"]), :host(:disabled) ::slotted([slot="trailing-supporting-text"]), :host(:disabled) ::slotted([slot="trailing-icon"]) { color: color-mix( in srgb, var(--m3e-list-item-disabled-trailing-color, ${j.color.onSurface}) var(--m3e-list-item-disabled-trailing-opacity, 38%), transparent ); } :host(:not(:disabled)) .state-layer { --m3e-state-layer-hover-color: var(--m3e-list-item-hover-state-layer-color, ${j.color.onSurface}); --m3e-state-layer-hover-opacity: var( --m3e-list-item-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity} ); --m3e-state-layer-focus-color: var(--m3e-list-item-focus-state-layer-color, ${j.color.onSurface}); --m3e-state-layer-focus-opacity: var( --m3e-list-item-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity} ); } :host(:not(:disabled)) .ripple { --m3e-ripple-color: var(--m3e-list-item-pressed-state-layer-color, ${j.color.onSurface}); --m3e-ripple-opacity: var( --m3e-list-item-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity} ); } @media (forced-colors: active) { .base { transition: none; } :host(:disabled) ::slotted([slot="leading"]), :host(:disabled) ::slotted([slot="leading-icon"]), :host(:disabled) .content, :host(:disabled) ::slotted([slot="overline"]), :host(:disabled) ::slotted([slot="supporting-text"]), :host(:disabled) ::slotted([slot="trailing"]), :host(:disabled) ::slotted([slot="trailing-supporting-text"]), :host(:disabled) ::slotted([slot="trailing-icon"]) { color: GrayText; } } @media (prefers-reduced-motion) { .base { transition: none; } }`, am = e([T("m3e-list-item")], am);
var om, sm, cm, lm, um, dm, fm = class extends E(M(S, "list")) {
	constructor() {
		super(...arguments), om.add(this), sm.set(this, []), cm.set(this, {
			video: 0,
			image: 0,
			avatar: 0,
			icon: 0,
			text: 0
		}), lm.set(this, {
			video: 0,
			image: 0,
			avatar: 0,
			icon: 0,
			text: 0
		}), this.variant = "standard";
	}
	get items() {
		return t(this, sm, "f");
	}
	get leadingContentType() {
		return t(this, cm, "f").video > 0 ? "video" : t(this, cm, "f").image > 0 ? "image" : t(this, cm, "f").avatar > 0 ? "avatar" : t(this, cm, "f").icon > 0 ? "icon" : t(this, cm, "f").text > 0 ? "text" : void 0;
	}
	get trailingContentType() {
		return t(this, lm, "f").video > 0 ? "video" : t(this, lm, "f").image > 0 ? "image" : t(this, lm, "f").avatar > 0 ? "avatar" : t(this, lm, "f").icon > 0 ? "icon" : t(this, lm, "f").text > 0 ? "text" : void 0;
	}
	connectedCallback() {
		super.connectedCallback(), t(this, om, "m", um).call(this);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, om, "m", um).call(this);
	}
	render() {
		return b`<slot @slotchange="${t(this, om, "m", dm)}"></slot>`;
	}
	notifyItemsChange() {}
	notifyLeadingContentTypeChange(e, n) {
		e && t(this, cm, "f")[e]--, n && t(this, cm, "f")[n]++, [
			"video",
			"image",
			"avatar",
			"icon"
		].forEach((e) => {
			O(this, `--has-leading-${e}`, this.leadingContentType === e);
		});
	}
	notifyTrailingContentTypeChange(e, n) {
		e && t(this, lm, "f")[e]--, n && t(this, lm, "f")[n]--, [
			"video",
			"image",
			"avatar",
			"icon"
		].forEach((e) => {
			O(this, `--has-trailing-${e}`, this.trailingContentType === e);
		});
	}
};
sm = /* @__PURE__ */ new WeakMap(), cm = /* @__PURE__ */ new WeakMap(), lm = /* @__PURE__ */ new WeakMap(), om = /* @__PURE__ */ new WeakSet(), um = function() {
	Xp(this.variant) || (this.variant = "standard"), wr(this, this.variant, "segmented", "standard");
}, dm = function(e) {
	n(this, sm, e.target.assignedElements({ flatten: !0 }).filter((e) => e instanceof am), "f"), t(this, sm, "f").forEach((e, n) => {
		O(e, "--first", n === 0), O(e, "--last", n === t(this, sm, "f").length - 1);
	}), this.notifyItemsChange();
}, fm.styles = l`:host { display: flex; flex-direction: column; overflow-y: auto; box-sizing: border-box; padding-block: var(--m3e-list-block-padding, 0px); --m3e-divider-inset-start-size: var(--m3e-list-divider-inset-start-size, ${j.measurement.space200}); --m3e-divider-inset-end-size: var(--m3e-list-divider-inset-end-size, ${j.measurement.space300}); } :host([hidden]) { display: none; } :host(:is(:state(--standard), :--standard)) { --_list-item-leading-video-outset: var(--m3e-list-item-leading-space, ${j.measurement.space200}); --_list-item-trailing-video-outset: var(--m3e-list-item-trailing-space, ${j.measurement.space200}); --_expandable-list-item-expanded-toggle-icon-container-color: transparent; --_expandable-list-item-animation: ${j.motion.spring.fastEffects}; --_expandable-list-item-slide-animation: 0s linear; } :host(:is(:state(--segmented), :--segmented)) { row-gap: var(--m3e-segmented-list-segment-gap, ${j.measurement.space25}); --_expandable-list-item-animation: ${j.motion.spring.fastSpatial}; } :host(:is(:state(--segmented), :--segmented)) { --m3e-list-item-container-color: var(--m3e-segmented-list-item-container-color, ${j.color.surface}); --m3e-list-item-disabled-container-color: var( --m3e-segmented-list-item-disabled-container-color, ${j.color.surface} ); --m3e-list-item-container-shape: var( --m3e-segmented-list-item-container-shape, ${j.shape.corner.extraSmall} ); --m3e-list-item-hover-container-shape: var( --m3e-segmented-list-item-hover-container-shape, ${j.shape.corner.medium} ); --m3e-list-item-focus-container-shape: var( --m3e-segmented-list-item-focus-container-shape, ${j.shape.corner.large} ); --m3e-list-item-selected-container-shape: var( --m3e-segmented-list-item-selected-container-shape, ${j.shape.corner.large} ); --m3e-list-item-video-shape: var(--m3e-segmented-list-item-video-shape, ${j.shape.corner.small}); --m3e-list-item-image-shape: var(--m3e-segmented-list-item-image-shape, ${j.shape.corner.small}); --m3e-list-item-between-space: var(--m3e-segmented-list-item-spacing, ${j.measurement.space150}); --_expandable-list-item-items-segment-gap: var( --m3e-segmented-list-segment-gap, ${j.measurement.space25} ); --_expandable-list-item-expanded-top-shape: var( --m3e-segmented-list-container-shape, ${j.shape.corner.large} ); --_expandable-list-item-bottom-container-shape: var( --m3e-segmented-list-container-shape, ${j.shape.corner.large} ); } :host(:is(:state(--segmented), :--segmented)) ::slotted(:is(:state(--first), :--first)), :host(:is(:state(--segmented), :--segmented)) ::slotted(:is(:state(--has-previous-open), :--has-previous-open):not([open])) { --_list-item-top-container-shape: var(--m3e-segmented-list-container-shape, ${j.shape.corner.large}); } :host(:is(:state(--segmented), :--segmented)) ::slotted(:is(:state(--has-next-open), :--has-next-open):not([open])), :host(:is(:state(--segmented), :--segmented)) ::slotted(:is(:state(--last), :--last)) { --_list-item-bottom-container-shape: var(--m3e-segmented-list-container-shape, ${j.shape.corner.large}); } :host(:is(:state(--segmented), :--segmented)) ::slotted(m3e-divider) { display: none; } :host(:is(:state(--has-leading-video), :--has-leading-video)) { --_list-item-leading-reserved-display: block; --_list-item-leading-reserved-space: var(--m3e-list-item-video-width, 100px); } :host(:is(:state(--standard), :--standard):is(:state(--has-leading-video), :--has-leading-video)) { --_list-item-leading-reserved-outset: var(--m3e-list-item-leading-space, ${j.measurement.space200}); --_list-item-trailing-reserved-outset: var(--m3e-list-item-trailing-space, ${j.measurement.space200}); } :host(:is(:state(--has-leading-image), :--has-leading-image)) { --_list-item-leading-reserved-display: block; --_list-item-leading-reserved-space: var(--m3e-list-item-image-width, 56px); } :host(:is(:state(--has-leading-avatar), :--has-leading-avatar)) { --_list-item-leading-reserved-display: block; --_list-item-leading-reserved-space: var(--m3e-avatar-size, 40px); } :host(:is(:state(--has-leading-icon), :--has-leading-icon)) { --_list-item-leading-reserved-display: block; --_list-item-leading-reserved-space: var(--m3e-list-item-icon-size, 24px); }`, e([C({
	reflect: !0,
	useDefault: !0
})], fm.prototype, "variant", void 0), fm = e([T("m3e-list")], fm);
var pm, mm, hm, gm, _m, vm, ym, bm, xm, Sm = xm = class extends am {
	constructor() {
		super(...arguments), pm.add(this), mm.set(this, xm.__nextId++), hm.set(this, `m3e-expandable-list-item-${t(this, mm, "f")}-content`), gm.set(this, `m3e-expandable-list-item-${t(this, mm, "f")}-header`), _m.set(this, []), this.disabled = !1, this.open = !1;
	}
	get items() {
		return t(this, _m, "f");
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
			for (let e = this.previousElementSibling; e; e = e.previousElementSibling) if (e instanceof am) {
				O(e, "--has-next-open", this.open);
				break;
			}
			for (let e = this.nextElementSibling; e; e = e.nextElementSibling) if (e instanceof am) {
				O(e, "--has-previous-open", this.open);
				break;
			}
		}
	}
	render() {
		return b`<div class="base"><m3e-list-item-button id="${t(this, gm, "f")}" class="header" ?disabled="${this.disabled}" aria-expanded="${this.open}" aria-controls="${t(this, hm, "f")}" @click="${t(this, pm, "m", vm)}"><slot name="leading" slot="leading" @slotchange="${this._handleLeadingSlotChange}"></slot><slot name="overline" slot="overline"></slot><slot></slot><slot name="supporting-text" slot="supporting-text"></slot><div class="toggle-container" slot="trailing" aria-hidden="true"><div class="toggle"><slot name="toggle-icon"><svg viewBox="0 -960 960 960" fill="currentColor"><path d="M480-344 240-584l56-56 184 184 184-184 56 56-240 240Z"/></svg></slot></div></div></m3e-list-item-button><m3e-collapsible id="${t(this, hm, "f")}" class="items" role="list" aria-labelledby="${t(this, gm, "f")}" ?open="${this.open}" slide @opening="${t(this, pm, "m", ym)}" @opened="${t(this, pm, "m", ym)}" @closing="${t(this, pm, "m", ym)}" @closed="${t(this, pm, "m", ym)}"><slot name="items" @slotchange="${t(this, pm, "m", bm)}"></slot></m3e-collapsible></div>`;
	}
};
mm = /* @__PURE__ */ new WeakMap(), hm = /* @__PURE__ */ new WeakMap(), gm = /* @__PURE__ */ new WeakMap(), _m = /* @__PURE__ */ new WeakMap(), pm = /* @__PURE__ */ new WeakSet(), vm = function(e) {
	e.defaultPrevented || (this.open = !this.open, this.closest("m3e-action-list")?.[q].updateActiveItem(this.button));
}, ym = function(e) {
	e.stopPropagation(), [
		"opening",
		"opened",
		"closing",
		"closed"
	].forEach((t) => {
		this.button?.classList.toggle(t, e.type === t);
	}), this.dispatchEvent(new Event(e.type, { bubbles: !0 }));
}, bm = function(e) {
	n(this, _m, e.target.assignedElements({ flatten: !0 }).flatMap((e) => [...e.childNodes].filter((e) => e instanceof am)), "f"), t(this, _m, "f").forEach((e, n) => {
		n == t(this, _m, "f").length - 1 ? e.style.setProperty("--_list-item-bottom-container-shape", "var(--_expandable-list-item-bottom-container-shape)") : e.style.removeProperty("--_list-item-bottom-container-shape");
	}), this.closest("m3e-list, m3e-action-list, m3e-selection-list")?.notifyItemsChange();
}, Sm.styles = l`:host { display: block; } .header { width: 100%; margin-bottom: 0px; z-index: 1; --_list-item-button-trailing-align-self: stretch; } .toggle-container { display: flex; align-items: center; justify-content: center; align-self: stretch; width: var(--m3e-expandable-list-item-toggle-icon-container-width, 32px); border-radius: var(--m3e-expandable-list-item-toggle-icon-container-shape, ${j.shape.corner.full}); transition: ${c(`background-color ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; } .toggle { display: flex; align-items: center; justify-content: center; vertical-align: middle; font-size: var(--m3e-expandable-list-item-toggle-icon-size, 24px); transition: ${c(`transform ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; } .toggle svg, ::slotted(svg[slot="toggle-icon"]) { width: 1em; height: 1em; } :host([open]) .toggle-container { background-color: var( --_expandable-list-item-expanded-toggle-icon-container-color, var(--m3e-expandable-list-item-expanded-toggle-icon-container-color, ${j.color.surfaceContainer}) ); } :host([open]) .toggle { transform: rotate(180deg); } :host([open]) .header { --_list-item-top-container-shape: var(--_expandable-list-item-expanded-top-shape, initial); --_list-item-bottom-container-shape: initial; margin-bottom: var(--_expandable-list-item-items-segment-gap, 0px); } :host([open]) .header, :host([open]) .items { --_list-item-container-color: var( --m3e-expandable-list-item-expanded-container-color, var(--m3e-list-item-container-color, ${j.color.surface}) ); } .base { overflow-y: hidden; } .base, ::slotted([slot="items"]) { display: flex; flex-direction: column; box-sizing: border-box; } ::slotted([slot="items"]) { flex: none; --_list-item-top-container-shape: initial; --_list-item-bottom-container-shape: initial; row-gap: var(--_expandable-list-item-items-segment-gap, 0px); } .items { --m3e-collapsible-animation: var(--_expandable-list-item-animation); --m3e-collapsible-slide-animation: var(--_expandable-list-item-slide-animation); } @media (forced-colors: active) { .header { transition: none; } :host([open]) .header.opening { margin-bottom: var(--_expandable-list-item-items-segment-gap, 0px); } } @media (prefers-reduced-motion) { .header, .toggle-container, .toggle { transition: none; } }`, Sm.__nextId = 0, e([C({
	type: Boolean,
	reflect: !0
})], Sm.prototype, "disabled", void 0), e([C({
	type: Boolean,
	reflect: !0
})], Sm.prototype, "open", void 0), e([w(".header")], Sm.prototype, "button", void 0), Sm = xm = e([T("m3e-expandable-list-item")], Sm);
var Cm, wm, Tm = class extends fm {
	constructor() {
		super(...arguments), Cm.set(this, (e) => this[q].onKeyDown(e)), this[wm] = new pu().withWrap().withHomeAndEnd().withVerticalOrientation().withSkipPredicate((e) => {
			if (e.disabled) return !0;
			let t = e.getRootNode().host;
			if (t.parentElement?.slot === "items") {
				let e = t.closest("m3e-expandable-list-item");
				for (; e;) {
					if (!e.open) return !0;
					let t = e.parentElement?.closest("m3e-expandable-list-item,m3e-action-list");
					if (t instanceof Sm) e = t;
					else break;
				}
			}
			return !1;
		});
	}
	connectedCallback() {
		super.connectedCallback(), this.addEventListener("keydown", t(this, Cm, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("keydown", t(this, Cm, "f"));
	}
	async notifyItemsChange() {
		let e = [...this.querySelectorAll("m3e-list-action,m3e-expandable-list-item")];
		for (let t of e) await Bt(t);
		await Bt(this);
		let { added: t } = this[q].setItems(e.map((e) => (e instanceof Sm, e.button)));
		this[q].activeItem || this[q].updateActiveItem(t.find((e) => !e.disabled));
	}
};
Cm = /* @__PURE__ */ new WeakMap(), wm = q, Tm = e([T("m3e-action-list")], Tm);
var Em = class extends ji(Fi(Ti(ni(E(M(am, "button"), !0))))) {
	constructor() {
		super(), new nn(this, { callback: (e, t) => this.shadowRoot?.querySelector(".base")?.classList.toggle("focus-visible", t) }), new Yn(this, { callback: (e) => this.shadowRoot?.querySelector(".base")?.classList.toggle("pressed", e) }), new hn(this, { callback: (e) => this.shadowRoot?.querySelector(".base")?.classList.toggle("hover", e) });
	}
	firstUpdated(e) {
		super.firstUpdated(e), [
			this._focusRing,
			this._stateLayer,
			this._ripple
		].forEach((e) => e?.attach(this));
	}
	render() {
		return b`<div class="base"><m3e-state-layer class="state-layer" ?disabled="${this.disabled}"></m3e-state-layer><m3e-focus-ring class="focus-ring" inward ?disabled="${this.disabled}"></m3e-focus-ring><m3e-ripple class="ripple" ?disabled="${this.disabled}"></m3e-ripple>${this[Mi]()} ${super._renderBase()}</div>`;
	}
};
Em.styles = [am.styles, l`:host { outline: none; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } .base { position: relative; } :host(:not(:disabled)) { cursor: pointer; } a { all: unset; display: block; position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px; z-index: 1; } slot[name="trailing"] { align-self: var(--_list-item-button-trailing-align-self, auto); }`], e([w(".focus-ring")], Em.prototype, "_focusRing", void 0), e([w(".state-layer")], Em.prototype, "_stateLayer", void 0), e([w(".ripple")], Em.prototype, "_ripple", void 0), Em = e([T("m3e-list-item-button")], Em);
var Dm, Om, km = class extends Fi(am, !0) {
	constructor() {
		super(...arguments), Dm.add(this), this.disabled = !1;
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
		return b`<m3e-list-item-button class="button" ?disabled="${this.disabled}" href="${nt(this.href || void 0)}" target="${nt(this.target || void 0)}" download="${nt(this.download || void 0)}" rel="${nt(this.rel || void 0)}" @click="${t(this, Dm, "m", Om)}"><slot name="leading" slot="leading" @slotchange="${this._handleLeadingSlotChange}"></slot><slot name="overline" slot="overline"></slot><slot></slot><slot name="supporting-text" slot="supporting-text"></slot><slot name="trailing" slot="trailing" @slotchange="${this._handleTrailingSlotChange}"></slot></m3e-list-item-button>`;
	}
};
Dm = /* @__PURE__ */ new WeakSet(), Om = function(e) {
	e.defaultPrevented || this.closest("m3e-action-list")?.[q].updateActiveItem(this.button);
}, km.styles = l`:host { display: block; } .button { width: 100%; }`, e([w(".button")], km.prototype, "button", void 0), e([C({
	type: Boolean,
	reflect: !0
})], km.prototype, "disabled", void 0), km = e([T("m3e-list-action")], km);
var Am, jm, Mm, Nm, Pm, Fm, Im = class extends ji(Ti(Ar(ni(E(M(am, "option"), !0))))) {
	constructor() {
		super(), Am.add(this), jm.set(this, void 0), Mm.set(this, ""), Nm.set(this, (e) => t(this, Am, "m", Fm).call(this, e)), new nn(this, { callback: (e, t) => this.shadowRoot?.querySelector(".base")?.classList.toggle("focus-visible", t) }), new Yn(this, { callback: (e) => this.shadowRoot?.querySelector(".base")?.classList.toggle("pressed", e) }), new hn(this, { callback: (e) => this.shadowRoot?.querySelector(".base")?.classList.toggle("hover", e) });
	}
	get value() {
		return t(this, jm, "f") ?? t(this, Mm, "f");
	}
	set value(e) {
		n(this, jm, e, "f");
	}
	connectedCallback() {
		super.connectedCallback(), this.addEventListener("click", t(this, Nm, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("click", t(this, Nm, "f"));
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
			this.closest("m3e-selection-list")?.[q].notifySelectionChange(this);
			for (let e of this.querySelectorAll("m3e-icon")) e.toggleAttribute("filled", this.selected);
		}
	}
	render() {
		return b`<div class="base"><m3e-state-layer class="state-layer" ?disabled="${this.disabled}"></m3e-state-layer><m3e-focus-ring class="focus-ring" inward ?disabled="${this.disabled}"></m3e-focus-ring><m3e-ripple class="ripple" ?disabled="${this.disabled}"></m3e-ripple><slot name="leading" @slotchange="${this._handleLeadingSlotChange}"></slot><div class="content"><slot name="overline"></slot><slot @slotchange="${t(this, Am, "m", Pm)}"></slot><slot name="supporting-text"></slot></div><slot name="trailing" @slotchange="${this._handleTrailingSlotChange}"></slot><div class="indicator">${this.closest("m3e-selection-list")?.multi ? b`<m3e-pseudo-checkbox ?checked="${this.selected}" ?disabled="${this.disabled}"></m3e-pseudo-checkbox>` : b`<m3e-pseudo-radio ?checked="${this.selected}" ?disabled="${this.disabled}"></m3e-pseudo-radio>`}</div></div>`;
	}
};
jm = /* @__PURE__ */ new WeakMap(), Mm = /* @__PURE__ */ new WeakMap(), Nm = /* @__PURE__ */ new WeakMap(), Am = /* @__PURE__ */ new WeakSet(), Pm = function(e) {
	n(this, Mm, Pt(e.target), "f");
}, Fm = function(e) {
	let t = this.closest("m3e-selection-list");
	!e.defaultPrevented && t && (t.multi || !this.selected) && this.dispatchEvent(new Event("beforeinput", {
		bubbles: !0,
		cancelable: !0
	})) && (this.selected = !this.selected, t[q].notifySelectionChange(this), this.dispatchEvent(new Event("input", { bubbles: !0 })), this.dispatchEvent(new Event("change", { bubbles: !0 })));
}, Im.styles = [am.styles, l`:host { outline: none; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } .base { position: relative; } .indicator { min-width: 24px; min-height: 24px; display: flex; align-items: center; justify-content: center; } :host(:not(:disabled)) { cursor: pointer; } :host(:is(:state(--three-line), :--three-line)) .indicator { align-self: flex-start; margin-top: var(--m3e-list-item-three-line-top-offset, ${j.measurement.space50}); } :host([selected]) .base, :host([selected]) .state-layer, :host([selected]) .ripple, :host([selected]) .focus-ring { border-radius: var(--m3e-list-item-selected-container-shape, ${j.shape.corner.large}); } :host([selected]:not(:disabled)) .content { color: var(--m3e-list-item-selected-label-text-color, ${j.color.onSecondaryContainer}); } :host([selected]:not(:disabled)) ::slotted([slot="overline"]) { color: var(--m3e-list-item-selected-overline-color, ${j.color.onSecondaryContainer}); } :host([selected]:not(:disabled)) ::slotted([slot="supporting-text"]) { color: var(--m3e-list-item-selected-supporting-text-color, ${j.color.onSecondaryContainer}); } :host([selected]:not(:disabled)) ::slotted([slot="leading"]) { color: var(--m3e-list-item-selected-leading-color, ${j.color.onSecondaryContainer}); } :host([selected]:not(:disabled)) ::slotted([slot="trailing"]) { color: var(--m3e-list-item-selected-trailing-color, ${j.color.onSecondaryContainer}); } :host([selected]:not(:disabled)) .base { background-color: var(--m3e-list-item-selected-container-color, ${j.color.secondaryContainer}); } :host([selected]:disabled) .base { background-color: color-mix( in srgb, var(--m3e-list-item-selected-disabled-container-color, ${j.color.onSurface}) var(--m3e-list-item-selected-disabled-container-opacity, 10%), transparent ); } :host([selected]:not(:disabled)) .state-layer { --m3e-state-layer-hover-color: var( --m3e-list-item-selected-hover-state-layer-color, ${j.color.onSurface} ); --m3e-state-layer-hover-opacity: var( --m3e-list-item-selected-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity} ); --m3e-state-layer-focus-color: var( --m3e-list-item-selected-focus-state-layer-color, ${j.color.onSurface} ); --m3e-state-layer-focus-opacity: var( --m3e-list-item-selected-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity} ); } :host([selected]:not(:disabled)) .ripple { --m3e-ripple-color: var(--m3e-list-item-selected-pressed-state-layer-color, ${j.color.onSurface}); --m3e-ripple-opacity: var( --m3e-list-item-selected-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity} ); } :host(:is(:state(--hide-selection), :--hide-selection)) .indicator, :host(:not(:is(:state(--hide-selection), :--hide-selection))) ::slotted([slot="trailing"]) { display: none; } @media (forced-colors: active) { :host([selected]:not(:disabled)) .content, :host([selected]:not(:disabled)) ::slotted([slot="overline"]), :host([selected]:not(:disabled)) ::slotted([slot="supporting-text"]), :host([selected]:not(:disabled)) ::slotted([slot="leading"]), :host([selected]:not(:disabled)) ::slotted([slot="trailing"]) { color: HighlightText; forced-color-adjust: none; } :host([selected]:not(:disabled)) .base { background-color: Highlight; } }`], e([w(".focus-ring")], Im.prototype, "_focusRing", void 0), e([w(".state-layer")], Im.prototype, "_stateLayer", void 0), e([w(".ripple")], Im.prototype, "_ripple", void 0), e([C()], Im.prototype, "value", null), Im = e([T("m3e-list-option")], Im);
var Lm, Rm, zm, Bm, Vm, Hm = class extends di(Fr(Rr(_i(ni(E(M(fm, "listbox"))))))) {
	constructor() {
		super(...arguments), Lm.add(this), Rm.set(this, (e) => this[q].onKeyDown(e)), zm.set(this, (e) => t(this, Lm, "m", Bm).call(this, e)), this[Vm] = new xu().withWrap().withHomeAndEnd().withVerticalOrientation(), this.multi = !1, this.hideSelectionIndicator = !1;
	}
	get options() {
		return this[q]?.items ?? [];
	}
	get selected() {
		return this[q]?.selectedItems ?? [];
	}
	get value() {
		let e = this.selected.filter((e) => !e.disabled).map((e) => e.value);
		switch (e.length) {
			case 0: return null;
			case 1: return e[0];
			default: return e;
		}
	}
	get [(Rm = /* @__PURE__ */ new WeakMap(), zm = /* @__PURE__ */ new WeakMap(), Lm = /* @__PURE__ */ new WeakSet(), Vm = q, fi)]() {
		let e = this.value;
		if (Array.isArray(e)) {
			let t = new FormData();
			for (let n of e) t.append(this.name, n);
			return t;
		}
		return e;
	}
	connectedCallback() {
		super.connectedCallback(), this.addEventListener("keydown", t(this, Rm, "f")), this.addEventListener("change", t(this, zm, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("keydown", t(this, Rm, "f")), this.removeEventListener("change", t(this, zm, "f"));
	}
	update(e) {
		super.update(e), e.has("disabled") && (e.get("disabled") !== void 0 || this.disabled) && (this[q].disabled = this.disabled), e.has("multi") && (this[q].multi = this.multi, this.multi ? this.setAttribute("aria-multiselectable", "true") : this.removeAttribute("aria-multiselectable"), this[q].items.forEach((e) => e.requestUpdate())), e.has("hideSelectionIndicator") && this[q].items.forEach((e) => O(e, "--hide-selection", this.hideSelectionIndicator));
	}
	notifyItemsChange() {
		let { added: e } = this[q].setItems(this.items.filter((e) => e instanceof Im));
		e.forEach((e) => O(e, "--hide-selection", this.hideSelectionIndicator)), this[q].activeItem || this[q].updateActiveItem(e.find((e) => !e.disabled));
	}
};
Bm = function(e) {
	e.target !== this && (e.stopImmediatePropagation(), e.stopPropagation(), this.dispatchEvent(new Event("change", { bubbles: !0 })));
}, e([C({ type: Boolean })], Hm.prototype, "multi", void 0), e([C({
	attribute: "hide-selection-indicator",
	type: Boolean
})], Hm.prototype, "hideSelectionIndicator", void 0), Hm = e([T("m3e-selection-list")], Hm);
//#endregion
//#region node_modules/@m3e/web/dist/chips.js
function Um(e) {
	return e === "outlined" || e === "elevated";
}
var Wm, Gm, Km, qm, Jm, Ym, Xm, Zm, Qm = class extends E(S) {
	constructor() {
		super(...arguments), Wm.add(this), Gm.set(this, void 0), Km.set(this, ""), this.variant = "outlined";
	}
	get value() {
		return t(this, Gm, "f") ?? t(this, Km, "f");
	}
	set value(e) {
		n(this, Gm, e, "f");
	}
	get label() {
		return t(this, Km, "f");
	}
	connectedCallback() {
		super.connectedCallback(), t(this, Wm, "m", qm).call(this);
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
		super.willUpdate(e), e.has("variant") && t(this, Wm, "m", qm).call(this);
	}
	render() {
		let e = !ti(this) || this.disabled, n = ri(this) && this.disabledInteractive;
		return b`<div class="base"><m3e-elevation class="elevation" ?disabled="${e || n}"></m3e-elevation><m3e-state-layer class="state-layer" ?disabled="${e || n}"></m3e-state-layer><m3e-focus-ring class="focus-ring" ?disabled="${e}"></m3e-focus-ring><m3e-ripple class="ripple" ?disabled="${e || n}"></m3e-ripple><div class="touch" aria-hidden="true"></div>${Ni(this) ? this[Mi]() : x}<div class="wrapper">${t(this, Wm, "m", Jm).call(this)}</div></div>`;
	}
	_renderIcon() {
		return b`<slot name="icon" aria-hidden="true" @slotchange="${t(this, Wm, "m", Ym)}"></slot>`;
	}
	_renderTrailingIcon() {
		return b`<slot name="trailing-icon" aria-hidden="true" @slotchange="${t(this, Wm, "m", Xm)}"></slot>`;
	}
	_renderSlot() {
		return b`<slot @slotchange="${t(this, Wm, "m", Zm)}"></slot>`;
	}
};
Gm = /* @__PURE__ */ new WeakMap(), Km = /* @__PURE__ */ new WeakMap(), Wm = /* @__PURE__ */ new WeakSet(), qm = function() {
	Um(this.variant) || (this.variant = "outlined"), wr(this, this.variant, "elevated", "outlined");
}, Jm = function() {
	return b`${this._renderIcon()}<div class="label">${this._renderSlot()}</div>${this._renderTrailingIcon()}`;
}, Ym = function(e) {
	O(this, "--with-icon", Ft(e.target));
}, Xm = function(e) {
	O(this, "--with-trailing-icon", Ft(e.target));
}, Zm = function(e) {
	n(this, Km, Pt(e.target), "f");
}, Qm.styles = l`:host { display: inline-block; vertical-align: middle; outline: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } :host([hidden]) { display: none; } .base { box-sizing: border-box; vertical-align: middle; display: inline-flex; align-items: center; justify-content: center; position: relative; width: 100%; transition: ${c(`background-color ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; border-radius: var(--m3e-chip-container-shape, ${j.shape.corner.small}); height: calc(var(--m3e-chip-container-height, 32px) + ${j.density.calc(-3)}); font-size: var(--m3e-chip-label-text-font-size, ${j.typescale.standard.label.large.fontSize}); font-weight: var(--m3e-chip-label-text-font-weight, ${j.typescale.standard.label.large.fontWeight}); line-height: var(--m3e-chip-label-text-line-height, ${j.typescale.standard.label.large.lineHeight}); letter-spacing: var(--m3e-chip-label-text-tracking, ${j.typescale.standard.label.large.tracking}); } :host(:not(m3e-chip):not(:disabled):not([disabled-interactive])) { cursor: pointer; } :host(:not(m3e-chip):not(:disabled)[disabled-interactive]) { cursor: not-allowed; } :host(:not(m3e-chip):not(:disabled):not([disabled-interactive])) .base { user-select: none; } .touch { position: absolute; height: 48px; left: 0; right: 0; } .wrapper { width: 100%; overflow: hidden; display: inline-flex; align-items: center; column-gap: var(--m3e-chip-spacing, ${j.measurement.space100}); } .label { flex: 1 1 auto; min-width: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; } a { all: unset; display: block; position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px; z-index: 1; } :host(:is(:state(--elevated), :--elevated)) .base { background-color: var(--m3e-elevated-chip-container-color, ${j.color.surfaceContainerLow}); --m3e-elevation-level: var(--m3e-elevated-chip-elevation, ${j.elevation.level1}); --m3e-elevation-hover-level: var(--m3e-elevated-chip-hover-elevation, ${j.elevation.level2}); --m3e-elevation-focus-level: var(--m3e-elevated-chip-elevation, ${j.elevation.level1}); --m3e-elevation-pressed-level: var(--m3e-elevated-chip-elevation, ${j.elevation.level1}); } :host(:is(:state(--outlined), :--outlined)) .base { outline-width: var(--m3e-outlined-chip-outline-thickness, 1px); outline-style: solid; outline-offset: calc(0px - var(--m3e-outlined-chip-outline-thickness, 1px)); } :host(:not(:disabled):not([disabled-interactive]):is(:state(--outlined), :--outlined)) .base { outline-color: var(--m3e-outlined-chip-outline-color, ${j.color.outlineVariant}); } :host(:disabled:is(:state(--outlined), :--outlined)) .base, :host([disabled-interactive]:is(:state(--outlined), :--outlined)) .base { outline-color: color-mix( in srgb, var(--m3e-outlined-chip-disabled-outline-color, ${j.color.onSurface}) var(--m3e-outlined-chip-disabled-outline-opacity, 12%), transparent ); } :host(:is(:state(--with-icon), :--with-icon)) .wrapper { padding-inline-start: var(--m3e-chip-with-icon-padding-start, ${j.measurement.space100}); } :host(:not(:is(:state(--with-icon), :--with-icon))) .wrapper { padding-inline-start: var(--m3e-chip-padding-start, ${j.measurement.space200}); } :host(:is(:state(--with-trailing-icon), :--with-trailing-icon)) .wrapper { padding-inline-end: var(--m3e-chip-with-icon-padding-end, ${j.measurement.space100}); } :host(:not(:is(:state(--with-trailing-icon), :--with-trailing-icon))) .wrapper { padding-inline-end: var(--m3e-chip-padding-end, ${j.measurement.space200}); } ::slotted([slot="icon"]), ::slotted([slot="trailing-icon"]) { flex: none; width: 1em; font-size: var(--m3e-chip-icon-size, 18px) !important; } :host(:not(:disabled):not([disabled-interactive]):not([selected])) .base { color: var(--m3e-chip-label-text-color, ${j.color.onSurface}); } :host(:not(:disabled):not([disabled-interactive]):not([selected])) ::slotted([slot="icon"]), :host(:not(:disabled):not([disabled-interactive]):not([selected])) ::slotted([slot="trailing-icon"]) { color: var(--m3e-chip-icon-color, ${j.color.primary}); } :host(:disabled) .base, :host([disabled-interactive]) .base { color: color-mix( in srgb, var(--m3e-chip-disabled-label-text-color, ${j.color.onSurface}) var(--m3e-chip-disabled-label-text-opacity, 38%), transparent ); } :host(:disabled) ::slotted([slot="icon"]), :host([disabled-interactive]) ::slotted([slot="icon"]), :host(:disabled) ::slotted([slot="trailing-icon"]), :host([disabled-interactive]) ::slotted([slot="trailing-icon"]) { color: color-mix( in srgb, var(--m3e-chip-disabled-icon-color, ${j.color.onSurface}) var(--m3e-chip-disabled-icon-opacity, 38%), transparent ); } :host(:is(:state(--elevated), :--elevated):disabled) .base, :host(:is(:state(--elevated), :--elevated)[disabled-interactive]) .base { background-color: color-mix( in srgb, var(--m3e-elevated-chip-disabled-container-color, ${j.color.onSurface}) var(--m3e-elevated-chip-disabled-container-opacity, 12%), transparent ); --m3e-elevation-level: var(--m3e-elevated-chip-disabled-elevation, ${j.elevation.level0}); } @media (prefers-reduced-motion) { .base { transition: none; } } @media (forced-colors: active) { .base { transition: none; } :host(:not(:disabled):not([disabled-interactive]):not([selected])) .base, :host(:not(:disabled):not([disabled-interactive]):not([selected])) ::slotted([slot="icon"]), :host(:not(:disabled):not([disabled-interactive]):not([selected])) ::slotted([slot="trailing-icon"]) { color: CanvasText; } :host(:not(:disabled):not([disabled-interactive]):is(:state(--outlined), :--outlined)) .base { outline-color: CanvasText; } :host(:disabled) .base, :host([disabled-interactive]) .base, :host(:disabled) ::slotted([slot="icon"]), :host([disabled-interactive]) ::slotted([slot="icon"]), :host(:disabled) ::slotted([slot="trailing-icon"]), :host([disabled-interactive]) ::slotted([slot="trailing-icon"]) { color: GrayText; } :host(:disabled:is(:state(--outlined), :--outlined)) .base, :host([disabled-interactive]:is(:state(--outlined), :--outlined)) .base { outline-color: GrayText; } }`, e([w(".elevation")], Qm.prototype, "_elevation", void 0), e([w(".focus-ring")], Qm.prototype, "_focusRing", void 0), e([w(".state-layer")], Qm.prototype, "_stateLayer", void 0), e([w(".ripple")], Qm.prototype, "_ripple", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], Qm.prototype, "variant", void 0), e([C()], Qm.prototype, "value", null), Qm = e([T("m3e-chip")], Qm);
var $m = class extends Di(Fi(ji(Ti(si(ni(M(Qm, "button"))))))) {
	_renderTrailingIcon() {
		return x;
	}
};
$m.formAssociated = !0, $m = e([T("m3e-assist-chip")], $m);
var eh = class extends Hi(S) {
	render() {
		return b`<slot></slot>`;
	}
};
eh.styles = l`:host { display: inline-flex; flex-wrap: wrap; vertical-align: middle; gap: var(--m3e-chip-set-spacing, ${j.measurement.space100}); outline: none; } :host([hidden]) { display: none; } :host([vertical]) { flex-direction: column; }`, eh = e([T("m3e-chip-set")], eh);
var th, nh, rh, ih = class extends Ar(ji(Ti(si(ni(M(Qm, "radio")))))) {
	constructor() {
		super(...arguments), th.add(this), nh.set(this, (e) => t(this, th, "m", rh).call(this, e));
	}
	connectedCallback() {
		super.connectedCallback(), this.addEventListener("click", t(this, nh, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("click", t(this, nh, "f"));
	}
	update(e) {
		super.update(e), e.has("selected") && this.closest("m3e-filter-chip-set")?.[q].notifySelectionChange(this);
	}
	_renderIcon() {
		return b`<div class="icon" aria-hidden="true"><svg class="check" viewBox="0 -960 960 960" aria-hidden="true"><path fill="currentColor" d="M382-240 154-468l57-57 171 171 367-367 57 57-424 424Z"/></svg> ${super._renderIcon()}</div>`;
	}
};
nh = /* @__PURE__ */ new WeakMap(), th = /* @__PURE__ */ new WeakSet(), rh = function(e) {
	e.defaultPrevented || this.dispatchEvent(new Event("beforeinput", {
		bubbles: !0,
		cancelable: !0
	})) && (this.selected = !this.selected, this.closest("m3e-filter-chip-set")?.[q].notifySelectionChange(this), this.dispatchEvent(new Event("input", { bubbles: !0 })), this.dispatchEvent(new Event("change", { bubbles: !0 })));
}, ih.formAssociated = !0, ih.styles = [Qm.styles, l`:host([selected]:not(:is(:state(--hide-selection), :--hide-selection))) .wrapper { padding-inline-start: var(--m3e-chip-with-icon-padding-start, ${j.measurement.space100}); } .icon { display: flex; align-items: center; justify-content: center; } .check { width: 1em; font-size: var(--m3e-chip-icon-size, 18px); } :host(:not(:disabled):not([disabled-interactive])) .check { color: var(--m3e-chip-selected-leading-icon-color, ${j.color.onSecondaryContainer}); } :host(:not([selected])) .check, :host(:is(:state(--hide-selection), :--hide-selection)) .check, :host(:is(:state(--hide-selection), :--hide-selection):not(:is(:state(--with-icon), :--with-icon))) .icon { display: none; } :host(:not(:is(:state(--with-icon), :--with-icon))) .icon { margin-inline-start: calc(0px - var(--m3e-chip-with-icon-padding-start, ${j.measurement.space100})); transition: margin-inline-start ${j.motion.spring.fastEffects}; } :host([selected]) .icon { margin-inline-start: 0; } :host([selected]:not(:is(:state(--hide-selection), :--hide-selection))) ::slotted([slot="icon"]) { display: none !important; } :host(:not(:disabled):not([disabled-interactive]):not([selected])) .base { color: var(--m3e-chip-unselected-label-text-color, ${j.color.onSurfaceVariant}); --m3e-ripple-color: var(--m3e-chip-unselected-ripple-color, ${j.color.onSurfaceVariant}); --m3e-state-layer-focus-color: var( --m3e-chip-unselected-state-layer-focus-color, ${j.color.onSurfaceVariant} ); --m3e-state-layer-hover-color: var( --m3e-chip-unselected-state-layer-hover-color, ${j.color.onSurfaceVariant} ); } :host(:not(:disabled):not([disabled-interactive]):not([selected])) ::slotted([slot="icon"]) { color: var(--m3e-chip-unselected-leading-icon-color, ${j.color.primary}); } :host(:not(:disabled):not([disabled-interactive]):not([selected])) ::slotted([slot="trailing-icon"]) { color: var(--m3e-chip-unselected-trailing-icon-color, ${j.color.onSurfaceVariant}); } :host(:not(:disabled):not([disabled-interactive])[selected]) .base { outline-offset: unset; outline-width: var(--m3e-chip-selected-outline-thickness, 0); color: var(--m3e-chip-selected-label-text-color, ${j.color.onSecondaryContainer}); background-color: var(--m3e-chip-selected-container-color, ${j.color.secondaryContainer}); --m3e-state-layer-hover-color: var( --m3e-chip-selected-container-hover-color, ${j.color.onSecondaryContainer} ); --m3e-state-layer-focus-color: var( --m3e-chip-selected-container-focus-color, ${j.color.onSecondaryContainer} ); --m3e-elevation-hover-level: var(--m3e-chip-selected-hover-elevation, ${j.elevation.level1}); --m3e-ripple-color: var(--m3e-chip-selected-ripple-color, ${j.color.onSecondaryContainer}); --m3e-state-layer-focus-color: var( --m3e-chip-selected-state-layer-focus-color, ${j.color.onSecondaryContainer} ); --m3e-state-layer-hover-color: var( --m3e-chip-selected-state-layer-hover-color, ${j.color.onSecondaryContainer} ); } :host(:not(:disabled):not([disabled-interactive])[selected]) ::slotted([slot="icon"]) { color: var(--m3e-chip-selected-leading-icon-color, ${j.color.onSecondaryContainer}); } :host(:not(:disabled):not([disabled-interactive]):not([selected])) ::slotted([slot="trailing-icon"]) { color: var(--m3e-chip-selected-trailing-icon-color, ${j.color.onSecondaryContainer}); } @media (prefers-reduced-motion) { .base, :host(:not(:is(:state(--with-icon), :--with-icon))) .icon { transition: none; } } @media (forced-colors: active) { :host(:not(:disabled):not([disabled-interactive]):not([selected])) .base, :host(:not(:disabled):not([disabled-interactive]):not([selected])) ::slotted([slot="icon"]), :host(:not(:disabled):not([disabled-interactive]):not([selected])) ::slotted([slot="trailing-icon"]) { color: CanvasText; } :host(:not(:disabled):not([disabled-interactive])[selected]) .base, :host(:not(:disabled):not([disabled-interactive])[selected]) ::slotted([slot="icon"]), :host(:not(:disabled):not([disabled-interactive])[selected]) ::slotted([slot="trailing-icon"]), :host(:not(:disabled):not([disabled-interactive])) .check { color: ButtonText; } :host(:not(:disabled):not([disabled-interactive])[selected]) .base { outline-offset: calc(0px - var(--m3e-outlined-chip-outline-thickness, 1px)); outline-width: var(--m3e-outlined-chip-outline-thickness, 1px); outline-color: ButtonText; } }`], ih = e([T("m3e-filter-chip")], ih);
var ah, oh, sh, ch, lh, uh, dh, fh = class extends di(Fr(Rr(_i(ni(E(M(eh, "radiogroup"))))))) {
	constructor() {
		super(...arguments), ah.add(this), oh.set(this, void 0), this[dh] = new xu().onActiveItemChange(() => this[q].activeItem?.focus()).withWrap().withDirectionality(rd.current), this.multi = !1, this.hideSelectionIndicator = !1;
	}
	get chips() {
		return this[q]?.items ?? [];
	}
	get selected() {
		return this[q]?.selectedItems ?? [];
	}
	get value() {
		let e = this.selected.filter((e) => !e.disabled).map((e) => e.value);
		switch (e.length) {
			case 0: return null;
			case 1: return e[0];
			default: return e;
		}
	}
	get [(oh = /* @__PURE__ */ new WeakMap(), ah = /* @__PURE__ */ new WeakSet(), dh = q, fi)]() {
		let e = this.value;
		if (Array.isArray(e)) {
			let t = new FormData();
			for (let n of e) t.append(this.name, n);
			return t;
		}
		return e;
	}
	connectedCallback() {
		super.connectedCallback(), n(this, oh, rd.observe(() => this[q].directionality = rd.current), "f");
	}
	disconnectedCallback() {
		super.disconnectedCallback(), t(this, oh, "f")?.call(this);
	}
	update(e) {
		super.update(e), e.has("vertical") && (this[q].vertical = this.vertical), e.has("disabled") && (e.get("disabled") !== void 0 || this.disabled) && (this[q].disabled = this.disabled), e.has("multi") && (this.role = this.multi ? "group" : "radiogroup", t(this, ah, "m", uh).call(this), this[q].multi = this.multi, this[q].disableRovingTabIndex(this.multi)), (e.has("multi") || e.has("disabled")) && (this.ariaDisabled = this.multi && this.disabled ? "true" : null), e.has("hideSelectionIndicator") && this.chips.forEach((e) => O(e, "--hide-selection", this.hideSelectionIndicator));
	}
	render() {
		return b`<slot @slotchange="${t(this, ah, "m", sh)}" @keydown="${t(this, ah, "m", ch)}" @change="${t(this, ah, "m", lh)}"></slot>`;
	}
};
sh = function() {
	let { added: e } = this[q].setItems([...this.querySelectorAll("m3e-filter-chip")]);
	e.forEach((e) => O(e, "--hide-selection", this.hideSelectionIndicator)), t(this, ah, "m", uh).call(this);
}, ch = function(e) {
	this.multi || this[q].onKeyDown(e);
}, lh = function(e) {
	e.stopPropagation(), this.dispatchEvent(new Event("change", { bubbles: !0 }));
}, uh = function() {
	this.chips.forEach((e) => {
		this.multi ? (e.role = "button", e.ariaPressed = `${e.selected}`, e.ariaSelected = e.ariaChecked = null) : (e.role = "radio", e.ariaChecked = `${e.selected}`, e.ariaPressed = e.ariaSelected = null);
	});
}, e([C({ type: Boolean })], fh.prototype, "multi", void 0), e([C({
	attribute: "hide-selection-indicator",
	type: Boolean
})], fh.prototype, "hideSelectionIndicator", void 0), fh = e([T("m3e-filter-chip-set")], fh);
var ph, mh, hh, gh, _h = class extends si(ni(M(Qm, "row"))) {
	constructor() {
		super(...arguments), ph.add(this), this.removable = !1, this.removeLabel = "Remove";
	}
	connectedCallback() {
		super.connectedCallback(), this.removeAttribute("tabindex");
	}
	update(e) {
		super.update(e), this.removeAttribute("tabindex"), e.has("removable") && O(this, "--with-trailing-icon", this.removable);
	}
	render() {
		return b`<div class="base"><m3e-elevation class="elevation" for="cell" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-elevation><m3e-state-layer class="state-layer" for="cell" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-state-layer><m3e-focus-ring class="focus-ring" for="cell" ?disabled="${this.disabled}"></m3e-focus-ring><m3e-ripple class="ripple" for="cell" ?disabled="${this.disabled || this.disabledInteractive}"></m3e-ripple><div class="wrapper"><div id="cell" class="cell" role="gridcell" tabindex="${nt(this.disabled ? void 0 : "-1")}" @keydown="${t(this, ph, "m", gh)}"><slot name="avatar" @slotchange="${t(this, ph, "m", mh)}"></slot>${this._renderIcon()}<div class="label">${this._renderSlot()}</div><div class="touch" aria-hidden="true"></div></div>${this._renderTrailingIcon()}</div></div>`;
	}
	_renderTrailingIcon() {
		return this.removable ? b`<span role="gridcell" class="remove"><m3e-icon-button class="remove-button" aria-label="${this.removeLabel}" size="extra-small" tabindex="-1" ?disabled="${this.disabled}" ?disabled-interactive="${this.disabledInteractive}" @click="${t(this, ph, "m", hh)}"><slot name="remove-icon"><svg class="remove-icon" viewBox="0 -960 960 960" fill="currentColor"><path d="m256-200-56-56 224-224-224-224 56-56 224 224 224-224 56 56-224 224 224 224-56 56-224-224-224 224Z"/></svg></slot></m3e-icon-button></span>` : x;
	}
};
ph = /* @__PURE__ */ new WeakSet(), mh = function(e) {
	O(this, "--with-avatar", Ft(e.target));
}, hh = function(e) {
	e.stopPropagation(), this.dispatchEvent(new Event("remove"));
}, gh = function(e) {
	if (this.removable) switch (e.key) {
		case "Backspace":
		case "Delete": this.dispatchEvent(new Event("remove"));
	}
}, _h.formAssociated = !0, _h.styles = [Qm.styles, l`.cell { display: inline-flex; align-items: center; outline: none; column-gap: var(--m3e-chip-spacing, ${j.measurement.space100}); min-width: 0; } .remove { line-height: 0; } .remove-button { --m3e-icon-button-extra-small-container-height: 24px; --m3e-icon-button-extra-small-icon-size: var(--m3e-chip-icon-size, 18px); width: 24px; } .remove-icon { flex: none; width: var(--m3e-chip-icon-size, 18px); height: var(--m3e-chip-icon-size, 18px); } .touch { top: calc( 0px - calc(calc(48px - calc(var(--m3e-chip-container-height, 32px) + ${j.density.calc(-3)})) / 2) ); } .wrapper { height: 100%; overflow: visible; min-width: 0; } ::slotted([slot="avatar"]) { flex: none; font-size: var(--m3e-chip-avatar-size, 24px); } ::slotted(m3e-avatar[slot="avatar"]) { --m3e-icon-size: var(--m3e-chip-avatar-icon-size, 18px); --m3e-avatar-size: var(--m3e-chip-avatar-size, 24px); --m3e-avatar-font-size: var( --m3e-chip-avatar-font-size, ${j.typescale.standard.title.small.fontSize} ); --m3e-avatar-font-weight: var( --m3e-chip-avatar-font-height, ${j.typescale.standard.title.small.fontWeight} ); --m3e-avatar-line-height: var( --m3e-chip-avatar-line-height, ${j.typescale.standard.title.small.lineHeight} ); --m3e-avatar-tracking: var(--m3e-chip-avatar-tracking, ${j.typescale.standard.title.small.tracking}); } :host(:disabled) ::slotted([slot="avatar"]), :host([disabled-interactive]) ::slotted([slot="avatar"]) { opacity: var(--m3e-chip-disabled-avatar-opacity, 38%); color: var(--m3e-chip-disabled-icon-color, ${j.color.onSurface}); } :host(:is(:state(--with-avatar), :--with-avatar)) ::slotted([slot="icon"]) { display: none; } :host(:is(:state(--with-avatar), :--with-avatar)) .wrapper { padding-inline-start: var(--m3e-chip-with-avatar-padding-start, ${j.measurement.space50}); } @media (forced-colors: active) { :host(:disabled) ::slotted([slot="avatar"]), :host([disabled-interactive]) ::slotted([slot="avatar"]) { color: CanvasText; } }`], e([w(".cell")], _h.prototype, "cell", void 0), e([w(".remove-button")], _h.prototype, "removeButton", void 0), e([C({ type: Boolean })], _h.prototype, "removable", void 0), e([C({ attribute: "remove-label" })], _h.prototype, "removeLabel", void 0), _h = e([T("m3e-input-chip")], _h);
var vh, yh, bh, xh, Sh, Ch, wh, Th, Eh, Dh, Oh, kh, Ah, jh, Mh, Nh, Ph, Fh, Ih, Lh, Rh, zh, Bh, Vh, Hh, Uh = class extends zi(Ri(Ci(Fr(Rr(_i(ni(E(M(eh, "grid"))))))), !1)) {
	constructor() {
		super(...arguments), vh.add(this), yh.set(this, void 0), bh.set(this, () => t(this, vh, "m", Bh).call(this)), xh.set(this, (e) => t(this, vh, "m", Vh).call(this, e)), Sh.set(this, () => t(this, vh, "m", Fh).call(this)), Ch.set(this, () => t(this, vh, "m", Ih).call(this)), wh.set(this, () => t(this, vh, "m", Lh).call(this)), Th.set(this, (e) => t(this, vh, "m", Rh).call(this, e)), Eh.set(this, (e) => t(this, vh, "m", zh).call(this, e)), Dh.set(this, new Hl()), Oh.set(this, new lu().onActiveItemChange(() => t(this, Oh, "f").activeItem?.focus()).withHomeAndEnd().withSkipPredicate((e) => !e.hasAttribute("tabindex")).withDirectionality(rd.current)), kh.set(this, !1), Ah.set(this, null), jh.set(this, 0), this._focused = !1, this.maxChips = null;
	}
	get chips() {
		return [...this.querySelectorAll("m3e-input-chip")];
	}
	get value() {
		let e = this.chips.filter((e) => !e.disabled).map((e) => e.value);
		return e.length == 0 ? null : e;
	}
	get [(yh = /* @__PURE__ */ new WeakMap(), bh = /* @__PURE__ */ new WeakMap(), xh = /* @__PURE__ */ new WeakMap(), Sh = /* @__PURE__ */ new WeakMap(), Ch = /* @__PURE__ */ new WeakMap(), wh = /* @__PURE__ */ new WeakMap(), Th = /* @__PURE__ */ new WeakMap(), Eh = /* @__PURE__ */ new WeakMap(), Dh = /* @__PURE__ */ new WeakMap(), Oh = /* @__PURE__ */ new WeakMap(), kh = /* @__PURE__ */ new WeakMap(), Ah = /* @__PURE__ */ new WeakMap(), jh = /* @__PURE__ */ new WeakMap(), vh = /* @__PURE__ */ new WeakSet(), fi)]() {
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
		t(this, Ah, "f")?.focus();
	}
	connectedCallback() {
		super.connectedCallback(), customElements.get("m3e-form-field") ? this.closest("m3e-form-field")?.notifyControlStateChange() : customElements.whenDefined("m3e-form-field").then(() => {
			this.closest("m3e-form-field")?.notifyControlStateChange();
		}), n(this, jh, Number.parseInt(this.getAttribute("tabindex") ?? "0"), "f"), this.addEventListener("focus", t(this, Sh, "f")), this.addEventListener("focusin", t(this, Ch, "f")), this.addEventListener("focusout", t(this, wh, "f")), n(this, yh, rd.observe(() => t(this, Oh, "f").directionality = rd.current), "f");
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("focus", t(this, Sh, "f")), this.removeEventListener("focusin", t(this, Ch, "f")), this.removeEventListener("focusout", t(this, wh, "f")), t(this, yh, "f")?.call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), this.hasAttribute("tabindex") || this.setAttribute("tabindex", `${t(this, jh, "f")}`);
	}
	update(e) {
		super.update(e), e.has("vertical") && (this.ariaOrientation = null), e.has("disabled") && (t(this, Dh, "f").items.forEach((e) => e.disabled = this.disabled), t(this, Ah, "f") && (t(this, Ah, "f").disabled = this.disabled));
	}
	updated(e) {
		super.updated(e), (e.has("maxChips") || e.has("_focused")) && t(this, vh, "m", Hh).call(this);
	}
	render() {
		return b`<slot @keydown="${t(this, vh, "m", Mh)}" @slotchange="${t(this, vh, "m", Nh)}"></slot>${!this._focused && this.maxChips && this.chips.length > this.maxChips ? b`<div class="overflow">+${this.chips.length - this.maxChips}</div>` : x} <span role="row"><span role="gridcell"><slot name="input" @slotchange="${t(this, vh, "m", Ph)}"></slot></span></span>`;
	}
};
Mh = function(e) {
	t(this, Oh, "f").onKeyDown(e);
}, Nh = async function() {
	let { added: e, removed: n } = t(this, Dh, "f").setItems([...this.querySelectorAll("m3e-input-chip")]);
	for (let n of e) await Bt(n), this.disabled && (n.disabled = !0), n.addEventListener("remove", t(this, Th, "f")), n.cell.addEventListener("click", t(this, Eh, "f"));
	n.forEach((e) => {
		e.removeEventListener("remove", t(this, Th, "f")), e.cell.removeEventListener("click", t(this, Eh, "f"));
	}), t(this, Oh, "f").setItems(t(this, Dh, "f").items.flatMap((e) => e.removeButton ? [e.cell, e.removeButton] : [e.cell])), t(this, Oh, "f").activeItem || t(this, Oh, "f").updateActiveItem(t(this, Oh, "f").items.find((e) => e.hasAttribute("tabindex"))), t(this, vh, "m", Hh).call(this);
}, Ph = function() {
	let e = this.querySelector("input");
	if (t(this, Ah, "f") && (t(this, Ah, "f").removeEventListener("change", t(this, bh, "f")), t(this, Ah, "f").removeEventListener("keydown", t(this, xh, "f"))), n(this, Ah, e, "f"), t(this, Ah, "f")) {
		t(this, Ah, "f").disabled = this.disabled, t(this, Ah, "f").addEventListener("change", t(this, bh, "f")), t(this, Ah, "f").addEventListener("keydown", t(this, xh, "f"));
		let n = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, "value");
		Object.defineProperty(e, "value", {
			get: () => n.get?.call(e),
			set: (r) => {
				n.set?.call(e, r), t(this, Ah, "f") === e && !t(this, kh, "f") && t(this, vh, "m", Bh).call(this);
			}
		});
	}
}, Fh = function() {
	setTimeout(() => (t(this, Oh, "f").activeItem ?? t(this, Ah, "f"))?.focus());
}, Ih = function() {
	this.setAttribute("tabindex", "-1"), this._focused = !0;
}, Lh = function() {
	this.setAttribute("tabindex", `${t(this, jh, "f")}`), this._focused = !1;
}, Rh = function(e) {
	let n = e.target, r = t(this, Dh, "f").items.indexOf(n), i = t(this, Dh, "f").items.find((e, t) => t > r && !e.disabled && e.removable);
	n.remove(), t(this, Oh, "f").setActiveItem(t(this, Oh, "f").items.find((e) => e === i?.removeButton)), t(this, Oh, "f").activeItem || t(this, Ah, "f")?.focus(), this.dispatchEvent(new CustomEvent("change", {
		bubbles: !0,
		detail: {
			type: "remove",
			value: n.value,
			chip: n
		}
	}));
}, zh = function(e) {
	t(this, Oh, "f").updateActiveItem(e.composedPath().find((e) => e instanceof _h)?.cell);
}, Bh = async function() {
	if (!t(this, Ah, "f")) return;
	let e = t(this, Ah, "f").value;
	if (!e) return;
	let r = null, i = this.getRootNode().querySelector(`m3e-autocomplete[for="${t(this, Ah, "f").id}"]`)?.querySelectorAll("m3e-option");
	if (i) {
		for (let t of i) if (t.label === e) {
			r = t;
			break;
		}
	}
	let a = null;
	if (this.chips.some((t) => t.value === (r?.value ?? e)) || (a = document.createElement("m3e-input-chip"), a.removable = !0, a.appendChild(document.createTextNode(e)), r && (a.value = r.value), this.appendChild(a), await Bt(a)), t(this, Ah, "f")) try {
		n(this, kh, !0, "f"), t(this, Ah, "f").value = "";
	} finally {
		n(this, kh, !1, "f");
	}
	a && this.dispatchEvent(new CustomEvent("change", {
		bubbles: !0,
		detail: {
			type: "add",
			value: e,
			chip: a
		}
	}));
}, Vh = function(e) {
	if (e.key === "Backspace" && !t(this, Ah, "f")?.value) {
		let e = [...t(this, Dh, "f").items].reverse().find((e) => !e.disabled && !e.disabledInteractive && e.removable);
		e && e.dispatchEvent(new Event("remove"));
	}
}, Hh = function() {
	this.chips.forEach((e, t) => e.hidden = !this._focused && this.maxChips != null && t >= this.maxChips);
}, Lt(l`m3e-input-chip-set [slot="input"]::placeholder { user-select: none; color: currentColor; transition: opacity ${j.motion.duration.extraLong1}; } m3e-input-chip-set:not(:focus-within) [slot="input"]::placeholder { opacity: 0; transition: 0s; } m3e-input-chip-set:hover [slot="input"]::placeholder { transition: 0s; } @media (prefers-reduced-motion) { m3e-input-chip-set [slot="input"]::placeholder { transition: none !important; } }`), Uh.styles = [eh.styles, l`::slotted([slot="input"]) { outline: unset; border: unset; background-color: transparent; box-shadow: none; font-family: inherit; font-size: inherit; line-height: initial; letter-spacing: inherit; color: var(--_form-field-input-color, inherit); flex: 1 1 auto; min-width: 0; padding: unset; } ::slotted(m3e-input-chip) { min-width: 0; } span[role="row"], span[role="gridcell"] { display: contents; } .overflow { display: flex; flex: none; align-items: center; justify-content: center; font-size: var(--m3e-chip-label-text-font-size, ${j.typescale.standard.label.large.fontSize}); font-weight: var(--m3e-chip-label-text-font-weight, ${j.typescale.standard.label.large.fontWeight}); line-height: var(--m3e-chip-label-text-line-height, ${j.typescale.standard.label.large.lineHeight}); letter-spacing: var(--m3e-chip-label-text-tracking, ${j.typescale.standard.label.large.tracking}); } :host(:not(:disabled)) .overflow { color: var(--m3e-chip-label-text-color, ${j.color.onSurface}); } :host(:disabled) .overflow { color: color-mix( in srgb, var(--m3e-chip-disabled-label-text-color, ${j.color.onSurface}) var(--m3e-chip-disabled-label-text-opacity, 38%), transparent ); }`], e([Xe()], Uh.prototype, "_focused", void 0), e([C({
	attribute: "max-chips",
	type: Number
})], Uh.prototype, "maxChips", void 0), Uh = e([T("m3e-input-chip-set")], Uh);
var Wh = class extends Di(Fi(ji(Ti(si(ni(M(Qm, "button"))))))) {
	_renderTrailingIcon() {
		return x;
	}
};
Wh.formAssociated = !0, Wh = e([T("m3e-suggestion-chip")], Wh);
//#endregion
//#region node_modules/@m3e/web/dist/toolbar.js
function Gh(e) {
	return e === "standard" || e === "vibrant";
}
function Kh(e) {
	return e === "rounded" || e === "square";
}
var qh = {
	square: {
		shape: c(`var(--m3e-toolbar-square-shape, ${j.shape.corner.none})`),
		leadingSpace: c(`var(--m3e-toolbar-square-leading-space, ${j.measurement.space200})`),
		trailingSpace: c(`var(--m3e-toolbar-square-trailing-space, ${j.measurement.space200})`),
		topSpace: c(`var(--m3e-toolbar-square-top-space, ${j.measurement.space100})`),
		bottomSpace: c(`var(--m3e-toolbar-square-bottom-space, ${j.measurement.space100})`)
	},
	rounded: {
		shape: c(`var(--m3e-toolbar-rounded-shape, ${j.shape.corner.full})`),
		leadingSpace: c(`var(--m3e-toolbar-rounded-leading-space, ${j.measurement.space100})`),
		trailingSpace: c(`var(--m3e-toolbar-rounded-trailing-space, ${j.measurement.space100})`),
		topSpace: c(`var(--m3e-toolbar-rounded-top-space, ${j.measurement.space100})`),
		bottomSpace: c(`var(--m3e-toolbar-rounded-bottom-space, ${j.measurement.space100})`)
	}
};
function Jh(e) {
	let t = c(`:is(:state(--${e}), :--${e})`);
	return l`:host(${t}) .base, :host(${t}) .state-layer, :host(${t}) .elevation { border-radius: ${qh[e].shape}; } :host(${t}:not([vertical])) .base { padding-inline-start: ${qh[e].leadingSpace}; padding-inline-end: ${qh[e].trailingSpace}; padding-block-start: calc(${qh[e].topSpace} + ${j.density.calc(-3)}); padding-block-end: calc(${qh[e].bottomSpace} + ${j.density.calc(-3)}); } :host(${t}[vertical]) .base { padding-block-start: calc(${qh[e].leadingSpace} + ${j.density.calc(-3)}); padding-block-end: calc(${qh[e].trailingSpace} + ${j.density.calc(-3)}); padding-inline-start: ${qh[e].topSpace}; padding-inline-end: ${qh[e].bottomSpace}; }`;
}
var Yh = [Jh("square"), Jh("rounded")], Xh = {
	size: c("var(--m3e-toolbar-size, 64px)"),
	spacing: c(`var(--m3e-toolbar-spacing, ${j.measurement.space50})`)
}, Zh = l`:host { display: inline-block; position: relative; } :host([hidden]) { display: none; } .base { display: flex; align-items: center; box-sizing: border-box; transition: ${c(`color ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; --_icon-button-size: auto; --_icon-button-min-size: 48px; } :host(:not([vertical])) { height: fit-content; } :host(:not([vertical])) .base { min-height: calc(${Xh.size} + ${j.density.calc(-3)}); column-gap: ${Xh.spacing}; } :host([vertical]) { width: fit-content; } :host([vertical]) .base { min-width: calc(${Xh.size} + ${j.density.calc(-3)}); } :host([vertical]) .base { flex-direction: column; justify-content: center; row-gap: ${Xh.spacing}; } .state-layer { transition: ${c(`background-color ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; } @media (forced-colors: active) { :host(:is(:state(--standard), :--standard)) .state-layer, :host(:is(:state(--vibrant), :--vibrant)) .state-layer { background-color: Canvas; } :host(:is(:state(--standard), :--standard)) .base, :host(:is(:state(--vibrant), :--vibrant)) .base { color: CanvasText; outline: 1px solid CanvasText; } .state-layer, .base { transition: none; } } @media (prefers-reduced-motion) { .state-layer, .base { transition: none; } }`, Y = {
	standard: {
		containerColor: c(`var(--m3e-toolbar-standard-container-color, var(--m3e-toolbar-container-color, ${j.color.surfaceContainer}))`),
		buttonContainerColor: c(`var(--m3e-toolbar-standard-button-container-color, var(--m3e-toolbar-button-container-color, ${j.color.surfaceContainer}))`),
		selectedButtonContainerColor: c(`var(--m3e-toolbar-standard-selected-button-container-color, var(--m3e-toolbar-selected-button-container-color, ${j.color.secondaryContainer}))`),
		iconColor: c(`var(--m3e-toolbar-standard-icon-color, var(--m3e-toolbar-icon-color, ${j.color.onSurfaceVariant}))`),
		selectedIconColor: c(`var(--m3e-toolbar-standard-selected-icon-color, var(--m3e-toolbar-selected-icon-color, ${j.color.onSecondaryContainer}))`),
		labelColor: c(`var(--m3e-toolbar-standard-label-color, var(--m3e-toolbar-label-color, ${j.color.onSurfaceVariant}))`),
		selectedLabelColor: c(`var(--m3e-toolbar-standard-selected-label-color, var(--m3e-toolbar-selected-label-color, ${j.color.onSecondaryContainer}))`),
		disabled: {
			iconColor: c(`var(--m3e-toolbar-standard-disabled-icon-color, var(--m3e-toolbar-disabled-icon-color, ${j.color.onSurface}))`),
			iconOpacity: c("var(--m3e-toolbar-standard-disabled-icon-opacity, var(--m3e-toolbar-disabled-icon-opacity, 38%))"),
			labelColor: c(`var(--m3e-toolbar-standard-disabled-label-color, var(--m3e-toolbar-disabled-label-color, ${j.color.onSurface}))`),
			labelOpacity: c("var(--m3e-toolbar-standard-disabled-label-opacity, var(--m3e-toolbar-label-icon-opacity, 38%))")
		},
		hover: {
			stateLayerColor: c(`var(--m3e-toolbar-standard-hover-state-layer-color, var(--m3e-toolbar-hover-state-layer-color, ${j.color.onSurfaceVariant}))`),
			selectedStateLayerColor: c(`var(--m3e-toolbar-standard-hover-selected-state-layer-color, var(--m3e-toolbar-hover-selected-state-layer-color, ${j.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-toolbar-standard-hover-state-layer-opacity, var(--m3e-toolbar-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			iconColor: c(`var(--m3e-toolbar-standard-hover-icon-color, var(--m3e-toolbar-hover-icon-color, ${j.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-toolbar-standard-hover-selected-icon-color, var(--m3e-toolbar-hover-selected-icon-color, ${j.color.onSecondaryContainer}))`),
			labelColor: c(`var(--m3e-toolbar-standard-hover-label-color, var(--m3e-toolbar-hover-label-color, ${j.color.onSurfaceVariant}))`),
			selectedLabelColor: c(`var(--m3e-toolbar-standard-hover-selected-label-color, var(--m3e-toolbar-hover-selected-label-color, ${j.color.onSecondaryContainer}))`)
		},
		focus: {
			stateLayerColor: c(`var(--m3e-toolbar-standard-focus-state-layer-color, var(--m3e-toolbar-focus-state-layer-color, ${j.color.onSurfaceVariant}))`),
			selectedStateLayerColor: c(`var(--m3e-toolbar-standard-focus-selected-state-layer-color, var(--m3e-toolbar-focus-selected-state-layer-color, ${j.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-toolbar-standard-focus-state-layer-opacity, var(--m3e-toolbar-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			iconColor: c(`var(--m3e-toolbar-standard-focus-icon-color, var(--m3e-toolbar-focus-icon-color, ${j.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-toolbar-standard-focus-selected-icon-color, var(--m3e-toolbar-focus-selected-icon-color, ${j.color.onSecondaryContainer}))`),
			labelColor: c(`var(--m3e-toolbar-standard-focus-label-color, var(--m3e-toolbar-focus-label-color, ${j.color.onSurfaceVariant}))`),
			selectedLabelColor: c(`var(--m3e-toolbar-standard-focus-selected-label-color, var(--m3e-toolbar-focus-selected-label-color, ${j.color.onSecondaryContainer}))`)
		},
		pressed: {
			stateLayerColor: c(`var(--m3e-toolbar-standard-pressed-state-layer-color, var(--m3e-toolbar-pressed-state-layer-color, ${j.color.onSurfaceVariant}))`),
			selectedStateLayerColor: c(`var(--m3e-toolbar-standard-pressed-selected-state-layer-color, var(--m3e-toolbar-pressed-selected-state-layer-color, ${j.color.onSecondaryContainer}))`),
			stateLayerOpacity: c(`var(--m3e-toolbar-standard-pressed-state-layer-opacity, var(--m3e-toolbar-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			iconColor: c(`var(--m3e-toolbar-standard-pressed-icon-color, var(--m3e-toolbar-pressed-icon-color, ${j.color.onSurfaceVariant}))`),
			selectedIconColor: c(`var(--m3e-toolbar-standard-pressed-selected-icon-color, var(--m3e-toolbar-pressed-selected-icon-color, ${j.color.onSecondaryContainer}))`),
			labelColor: c(`var(--m3e-toolbar-standard-pressed-label-color, var(--m3e-toolbar-pressed-label-color, ${j.color.onSurfaceVariant}))`),
			selectedLabelColor: c(`var(--m3e-toolbar-standard-pressed-selected-label-color, var(--m3e-toolbar-pressed-selected-label-color, ${j.color.onSecondaryContainer}))`)
		}
	},
	vibrant: {
		containerColor: c(`var(--m3e-toolbar-vibrant-container-color, var(--m3e-toolbar-container-color, ${j.color.primaryContainer}))`),
		buttonContainerColor: c(`var(--m3e-toolbar-vibrant-button-container-color, var(--m3e-toolbar-button-container-color, ${j.color.primaryContainer}))`),
		selectedButtonContainerColor: c(`var(--m3e-toolbar-vibrant-selected-button-container-color, var(--m3e-toolbar-selected-button-container-color, ${j.color.surfaceContainer}))`),
		iconColor: c(`var(--m3e-toolbar-vibrant-icon-color, var(--m3e-toolbar-icon-color, ${j.color.onPrimaryContainer}))`),
		selectedIconColor: c(`var(--m3e-toolbar-vibrant-selected-icon-color, var(--m3e-toolbar-selected-icon-color, ${j.color.onSurface}))`),
		labelColor: c(`var(--m3e-toolbar-vibrant-label-color, var(--m3e-toolbar-label-color, ${j.color.onPrimaryContainer}))`),
		selectedLabelColor: c(`var(--m3e-toolbar-vibrant-selected-label-color, var(--m3e-toolbar-selected-label-color, ${j.color.onSurface}))`),
		disabled: {
			iconColor: c(`var(--m3e-toolbar-vibrant-disabled-icon-color, var(--m3e-toolbar-disabled-icon-color, ${j.color.onSurface}))`),
			iconOpacity: c("var(--m3e-toolbar-vibrant-disabled-icon-opacity, var(--m3e-toolbar-disabled-icon-opacity, 38%))"),
			labelColor: c(`var(--m3e-toolbar-vibrant-disabled-label-color, var(--m3e-toolbar-disabled-label-color, ${j.color.onSurface}))`),
			labelOpacity: c("var(--m3e-toolbar-vibrant-disabled-label-opacity, var(--m3e-toolbar-label-icon-opacity, 38%))")
		},
		hover: {
			stateLayerColor: c(`var(--m3e-toolbar-vibrant-hover-state-layer-color, var(--m3e-toolbar-hover-state-layer-color, ${j.color.onPrimaryContainer}))`),
			selectedStateLayerColor: c(`var(--m3e-toolbar-vibrant-hover-selected-state-layer-color, var(--m3e-toolbar-hover-selected-state-layer-color, ${j.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-toolbar-vibrant-hover-state-layer-opacity, var(--m3e-toolbar-hover-state-layer-opacity, ${j.state.hoverStateLayerOpacity}))`),
			iconColor: c(`var(--m3e-toolbar-vibrant-hover-icon-color, var(--m3e-toolbar-hover-icon-color, ${j.color.onPrimaryContainer}))`),
			selectedIconColor: c(`var(--m3e-toolbar-vibrant-hover-selected-icon-color, var(--m3e-toolbar-hover-selected-icon-color, ${j.color.onSurface}))`),
			labelColor: c(`var(--m3e-toolbar-vibrant-hover-label-color, var(--m3e-toolbar-hover-label-color, ${j.color.onPrimaryContainer}))`),
			selectedLabelColor: c(`var(--m3e-toolbar-vibrant-hover-selected-label-color, var(--m3e-toolbar-hover-selected-label-color, ${j.color.onSurface}))`)
		},
		focus: {
			stateLayerColor: c(`var(--m3e-toolbar-vibrant-focus-state-layer-color, var(--m3e-toolbar-focus-state-layer-color, ${j.color.onPrimaryContainer}))`),
			selectedStateLayerColor: c(`var(--m3e-toolbar-vibrant-focus-selected-state-layer-color, var(--m3e-toolbar-focus-selected-state-layer-color, ${j.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-toolbar-vibrant-focus-state-layer-opacity, var(--m3e-toolbar-focus-state-layer-opacity, ${j.state.focusStateLayerOpacity}))`),
			iconColor: c(`var(--m3e-toolbar-vibrant-focus-icon-color, var(--m3e-toolbar-focus-icon-color, ${j.color.onPrimaryContainer}))`),
			selectedIconColor: c(`var(--m3e-toolbar-vibrant-focus-selected-icon-color, var(--m3e-toolbar-focus-selected-icon-color, ${j.color.onSurface}))`),
			labelColor: c(`var(--m3e-toolbar-vibrant-focus-label-color, var(--m3e-toolbar-focus-label-color, ${j.color.onPrimaryContainer}))`),
			selectedLabelColor: c(`var(--m3e-toolbar-vibrant-focus-selected-label-color, var(--m3e-toolbar-focus-selected-label-color, ${j.color.onSurface}))`)
		},
		pressed: {
			stateLayerColor: c(`var(--m3e-toolbar-vibrant-pressed-state-layer-color, var(--m3e-toolbar-pressed-state-layer-color, ${j.color.onPrimaryContainer}))`),
			selectedStateLayerColor: c(`var(--m3e-toolbar-vibrant-pressed-selected-state-layer-color, var(--m3e-toolbar-pressed-selected-state-layer-color, ${j.color.onSurface}))`),
			stateLayerOpacity: c(`var(--m3e-toolbar-vibrant-pressed-state-layer-opacity, var(--m3e-toolbar-pressed-state-layer-opacity, ${j.state.pressedStateLayerOpacity}))`),
			iconColor: c(`var(--m3e-toolbar-vibrant-pressed-icon-color, var(--m3e-toolbar-pressed-icon-color, ${j.color.onPrimaryContainer}))`),
			selectedIconColor: c(`var(--m3e-toolbar-vibrant-pressed-selected-icon-color, var(--m3e-toolbar-pressed-selected-icon-color, ${j.color.onSurface}))`),
			labelColor: c(`var(--m3e-toolbar-vibrant-pressed-label-color, var(--m3e-toolbar-pressed-label-color, ${j.color.onPrimaryContainer}))`),
			selectedLabelColor: c(`var(--m3e-toolbar-vibrant-pressed-selected-label-color, var(--m3e-toolbar-pressed-selected-label-color, ${j.color.onSurface}))`)
		}
	}
};
function Qh(e, t, n) {
	return c(`
    ${t}-${n}-state-layer-color: ${Y[e].hover.stateLayerColor};
    ${t}-${n}-unselected-state-layer-color: ${Y[e].hover.stateLayerColor};
    ${t}-${n}-selected-state-layer-color: ${Y[e].hover.selectedStateLayerColor};
    ${t}-${n}-state-layer-opacity: ${Y[e].hover.stateLayerOpacity};
    ${t}-${n}-icon-color: ${Y[e].hover.iconColor};
    ${t}-${n}-unselected-icon-color: ${Y[e].hover.iconColor};
    ${t}-${n}-selected-icon-color: ${Y[e].hover.selectedIconColor};`);
}
function $h(e) {
	let t = "--m3e-standard-icon-button";
	return c(`
      ${t}-container-color: ${Y[e].buttonContainerColor};
      ${t}-unselected-container-color: ${Y[e].buttonContainerColor};
      ${t}-selected-container-color: ${Y[e].selectedButtonContainerColor};
      ${t}-icon-color: ${Y[e].iconColor};
      ${t}-unselected-icon-color: ${Y[e].iconColor};
      ${t}-selected-icon-color: ${Y[e].selectedIconColor};
      ${t}-disabled-icon-color: ${Y[e].disabled.iconColor};
      ${t}-disabled-icon-opacity: ${Y[e].disabled.iconOpacity};
      ${t}-disabled-container-color: transparent;
      ${Qh(e, t, "hover")}
      ${Qh(e, t, "focus")}
      ${Qh(e, t, "pressed")}
  `);
}
function eg(e, t, n) {
	return c(`
    ${t}-${n}-state-layer-color: ${Y[e].hover.stateLayerColor};
    ${t}-${n}-unselected-state-layer-color: ${Y[e].hover.stateLayerColor};
    ${t}-${n}-selected-state-layer-color: ${Y[e].hover.selectedStateLayerColor};
    ${t}-${n}-state-layer-opacity: ${Y[e].hover.stateLayerOpacity};
    ${t}-${n}-icon-color: ${Y[e].hover.iconColor};
    ${t}-${n}-unselected-icon-color: ${Y[e].hover.iconColor};
    ${t}-${n}-selected-icon-color: ${Y[e].hover.selectedIconColor};
    ${t}-${n}-label-text-color: ${Y[e].hover.labelColor};
    ${t}-${n}-unselected-label-text-color: ${Y[e].hover.labelColor};
    ${t}-${n}-selected-label-text-color: ${Y[e].hover.selectedLabelColor};`);
}
function tg(e) {
	let t = "--m3e-text-button";
	return c(`
      ${t}-container-color: ${Y[e].buttonContainerColor};
      ${t}-unselected-container-color: ${Y[e].buttonContainerColor};
      ${t}-selected-container-color: ${Y[e].selectedButtonContainerColor};
      ${t}-icon-color: ${Y[e].iconColor};
      ${t}-unselected-icon-color: ${Y[e].iconColor};
      ${t}-selected-icon-color: ${Y[e].selectedIconColor};
      ${t}-label-text-color: ${Y[e].labelColor};
      ${t}-unselected-label-text-color: ${Y[e].labelColor};
      ${t}-selected-label-text-color: ${Y[e].selectedLabelColor};
      ${t}-disabled-icon-color: ${Y[e].disabled.iconColor};
      ${t}-disabled-icon-opacity: ${Y[e].disabled.iconOpacity};
      ${t}-disabled-label-text-color: ${Y[e].disabled.labelColor};
      ${t}-disabled-label-text-opacity: ${Y[e].disabled.labelOpacity};
      ${t}-disabled-container-color: transparent;
      ${eg(e, t, "hover")}
      ${eg(e, t, "focus")}
      ${eg(e, t, "pressed")}
  `);
}
function ng(e) {
	let t = c(`:is(:state(--${e}), :--${e})`);
	return c(`
    :host(${t}) .state-layer {
      background-color: ${Y[e].containerColor};
    }
    :host(${t}) .base {
      color: ${Y[e].labelColor};
      ${$h(e)}
      ${tg(e)}
    }
  `);
}
var rg = [ng("standard"), ng("vibrant")], ig, ag, og, sg, cg, lg, ug, dg, fg = class extends Hi(M(E(S), "toolbar")) {
	constructor() {
		super(...arguments), ig.add(this), ag.set(this, void 0), og.set(this, new pu().withHomeAndEnd().withDirectionality(rd.current)), this.variant = "standard", this.shape = "square", this.elevated = !1;
	}
	connectedCallback() {
		super.connectedCallback(), t(this, ig, "m", cg).call(this), t(this, ig, "m", sg).call(this), n(this, ag, rd.observe(() => t(this, og, "f").directionality = rd.current), "f");
	}
	disconnectedCallback() {
		super.disconnectedCallback(), t(this, ag, "f")?.call(this);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("shape") && t(this, ig, "m", sg).call(this), e.has("variant") && t(this, ig, "m", cg).call(this);
	}
	update(e) {
		super.update(e), e.has("vertical") && (t(this, og, "f").vertical = this.vertical);
	}
	render() {
		return b`<m3e-state-layer class="state-layer"></m3e-state-layer><m3e-elevation class="elevation" level="${this.elevated ? 3 : 0}"></m3e-elevation><div class="base"><slot @click="${t(this, ig, "m", dg)}" @keydown="${t(this, ig, "m", ug)}" @slotchange="${t(this, ig, "m", lg)}"></slot></div>`;
	}
};
ag = /* @__PURE__ */ new WeakMap(), og = /* @__PURE__ */ new WeakMap(), ig = /* @__PURE__ */ new WeakSet(), sg = function() {
	Kh(this.shape) || (this.shape = "square"), wr(this, this.shape, "rounded", "square");
}, cg = function() {
	Gh(this.variant) || (this.variant = "standard"), wr(this, this.variant, "standard", "vibrant");
}, lg = function() {
	let e = Fu.findInteractiveElements(this, !0), { added: n } = t(this, og, "f").setItems(e);
	if (!t(this, og, "f").activeItem) {
		let e = n.find((e) => !e.hasAttribute("disabled"));
		e && t(this, og, "f").updateActiveItem(e);
	}
}, ug = function(e) {
	t(this, og, "f").onKeyDown(e);
}, dg = function(e) {
	let n = e.composedPath().find((e) => e instanceof HTMLElement && t(this, og, "f").items.includes(e));
	n && t(this, og, "f").updateActiveItem(n);
}, fg.styles = [
	Zh,
	Yh,
	rg
], e([C({
	reflect: !0,
	useDefault: !0
})], fg.prototype, "variant", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], fg.prototype, "shape", void 0), e([C({
	type: Boolean,
	reflect: !0
})], fg.prototype, "elevated", void 0), fg = e([T("m3e-toolbar")], fg);
//#endregion
//#region node_modules/@m3e/web/dist/core-layout.js
var pg;
(function(e) {
	e.XSmall = "(max-width: 599.98px)", e.Small = "(min-width: 600px) and (max-width: 959.98px)", e.Medium = "(min-width: 960px) and (max-width: 1279.98px)", e.Large = "(min-width: 1280px) and (max-width: 1919.98px)", e.XLarge = "(min-width: 1920px)", e.Handset = "(max-width: 599.98px) and (orientation: portrait), (max-width: 959.98px) and (orientation: landscape)", e.Tablet = "(min-width: 600px) and (max-width: 839.98px) and (orientation: portrait), (min-width: 960px) and (max-width: 1279.98px) and (orientation: landscape)", e.Web = "(min-width: 840px) and (orientation: portrait), (min-width: 1280px) and (orientation: landscape)", e.HandsetPortrait = "(max-width: 599.98px) and (orientation: portrait)", e.TabletPortrait = "(min-width: 600px) and (max-width: 839.98px) and (orientation: portrait)", e.WebPortrait = "(min-width: 840px) and (orientation: portrait)", e.HandsetLandscape = "(max-width: 959.98px) and (orientation: landscape)", e.TabletLandscape = "(min-width: 960px) and (max-width: 1279.98px) and (orientation: landscape)", e.WebLandscape = "(min-width: 1280px) and (orientation: landscape)";
})(pg ||= {});
var mg = class {
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
globalThis.M3eBreakpointObserver = mg;
//#endregion
//#region node_modules/@m3e/web/dist/nav-bar.js
function hg(e) {
	return e === "compact" || e === "expanded" || e === "auto";
}
var gg, _g, vg, yg, bg, xg, Sg, Cg = class extends Li(E(M(S, "navigation"))) {
	constructor() {
		super(...arguments), gg.add(this), this[Sg] = new xu().disableRovingTabIndex(), _g.set(this, void 0), this.mode = "compact";
	}
	get items() {
		return this[q].items;
	}
	get selected() {
		return this[q].selectedItems[0] ?? null;
	}
	get currentMode() {
		return this._mode ?? (this.mode === "compact" ? "compact" : "expanded");
	}
	set currentMode(e) {
		this._mode = e;
	}
	connectedCallback() {
		super.connectedCallback(), t(this, gg, "m", vg).call(this);
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this._mode = void 0, t(this, _g, "f")?.call(this);
	}
	reconnectedCallback() {
		super.reconnectedCallback(), this.mode === "auto" && t(this, gg, "m", yg).call(this);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("mode") && (t(this, gg, "m", vg).call(this), t(this, _g, "f")?.call(this), this.mode === "auto" ? t(this, gg, "m", yg).call(this) : (this._mode = void 0, this._updateItems())), e.has("_mode") && this._updateItems();
	}
	firstUpdated(e) {
		super.firstUpdated(e), e.has("mode") || this._updateItems();
	}
	render() {
		return b`<div class="base"><slot @change="${t(this, gg, "m", xg)}" @slotchange="${t(this, gg, "m", bg)}"></slot></div>`;
	}
	_updateItems() {
		let e = this.currentMode === "compact" ? "vertical" : "horizontal";
		this._updateOrientation(e), O(this, "--compact", e === "vertical");
	}
	_updateOrientation(e) {
		this[q].items.forEach((t) => t.orientation = e);
	}
};
_g = /* @__PURE__ */ new WeakMap(), gg = /* @__PURE__ */ new WeakSet(), Sg = q, vg = function() {
	hg(this.mode) || (this.mode = "compact");
}, yg = function() {
	n(this, _g, mg.observe([pg.XSmall, pg.Small], (e) => {
		this._mode = e.get(pg.XSmall) || e.get(pg.Small) ? "compact" : "expanded", this._updateItems();
	}), "f");
}, bg = function() {
	this[q].setItems([...this.querySelectorAll("m3e-nav-item")]), this._updateItems();
}, xg = function(e) {
	e.stopPropagation(), this.dispatchEvent(new Event("change", { bubbles: !0 }));
}, Cg.styles = l`:host { display: block; overflow-x: auto; overflow-y: hidden; scrollbar-width: ${j.scrollbar.thinWidth}; scrollbar-color: ${j.scrollbar.color}; min-height: var(--m3e-nav-bar-height, 64px); } :host([hidden]) { display: none; } .base { contain: layout style; display: flex; align-items: stretch; justify-content: center; box-sizing: border-box; min-height: inherit; height: inherit; width: 100%; background-color: var(--m3e-nav-bar-container-color, ${j.color.surfaceContainer}); --_vertical-nav-item-min-width: var(--m3e-nav-bar-vertical-item-width, 112px); --_horizontal-nav-item-leading-space: var( --m3e-nav-bar-horizontal-nav-item-leading-space, ${j.measurement.space200} ); --_horizontal-nav-item-trailing-space: var( --m3e-nav-bar-horizontal-nav-item-trailing-space, ${j.measurement.space200} ); }`, e([Xe()], Cg.prototype, "_mode", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], Cg.prototype, "mode", void 0), Cg = e([T("m3e-nav-bar")], Cg);
function wg(e) {
	return e === "vertical" || e === "horizontal";
}
var Tg, Eg, Dg, Og, kg, Ag, jg, Mg, Ng, Pg = class extends id(Li(Vi(Fi(Ar(ji(Ti(si(ni(E(M(S, "button"), !0)))))))))) {
	constructor() {
		super(...arguments), Tg.add(this), Eg.set(this, !1), Dg.set(this, (e) => t(this, Tg, "m", Ag).call(this, e)), Og.set(this, new tr(this, {
			target: null,
			callback: (e) => t(this, Tg, "m", Mg).call(this, e)
		})), this.orientation = "vertical";
	}
	get navBar() {
		return this.closest("m3e-nav-bar") ?? this.closest("m3e-nav-rail") ?? null;
	}
	connectedCallback() {
		n(this, Eg, this.closest("m3e-nav-rail") !== null, "f"), super.connectedCallback(), t(this, Tg, "m", kg).call(this), this.addEventListener("click", t(this, Dg, "f"), { capture: !0 });
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("click", t(this, Dg, "f"), { capture: !0 }), n(this, Eg, !1, "f");
	}
	reconnectedCallback() {
		super.reconnectedCallback(), t(this, Tg, "m", Ng).call(this);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("orientation") && t(this, Tg, "m", kg).call(this);
	}
	update(e) {
		if (super.update(e), e.has("selected")) {
			this.ariaSelected = null, this.ariaPressed = null, this.ariaCurrent = `${this.selected}`;
			for (let e of this.querySelectorAll("m3e-icon")) e.toggleAttribute("filled", this.selected);
			this.navBar?.[q].notifySelectionChange(this);
		}
	}
	updated(e) {
		super.updated(e), e.has("orientation") && this._focusRing?.attach(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), [this._focusRing, this._indicator].forEach((e) => e?.attach(this)), t(this, Tg, "m", Ng).call(this);
	}
	render() {
		let e = this.disabled || this.disabledInteractive, n = b`<div class="label"><slot></slot></div>`;
		return b`${this.orientation === "vertical" ? b`<m3e-focus-ring class="focus-ring" inward></m3e-focus-ring>` : x}<div class="outer">${this[Mi]()}<div class="inner">${this.orientation === "horizontal" ? b`<m3e-focus-ring class="focus-ring"></m3e-focus-ring>` : x}<m3e-selection-indicator class="indicator" bounce ?centered="${this.orientation === "vertical"}" ?selected="${this.selected}" ?disabled="${e}"></m3e-selection-indicator><div class="touch" aria-hidden="true"></div><div class="base"><div class="icon-wrapper" aria-hidden="true"><div class="icon"><slot name="icon"></slot><slot name="selected-icon" @slotchange="${t(this, Tg, "m", jg)}"></slot></div></div><m3e-collapsible orientation="horizontal" ?no-animate="${!t(this, Eg, "f")}" ?open="${this.orientation === "horizontal"}">${this.orientation === "horizontal" ? n : x}</m3e-collapsible>${this.orientation === "horizontal" ? x : n}</div></div></div>`;
	}
};
Eg = /* @__PURE__ */ new WeakMap(), Dg = /* @__PURE__ */ new WeakMap(), Og = /* @__PURE__ */ new WeakMap(), Tg = /* @__PURE__ */ new WeakSet(), kg = function() {
	wg(this.orientation) || (this.orientation = "vertical"), wr(this, this.orientation, "horizontal", "vertical");
}, Ag = function(e) {
	e.defaultPrevented || this.dispatchEvent(new Event("beforeinput", {
		bubbles: !0,
		cancelable: !0
	})) && (this.selected = !0, this.navBar?.[q].notifySelectionChange(this), this.dispatchEvent(new Event("input", { bubbles: !0 })), this.dispatchEvent(new Event("change", { bubbles: !0 })));
}, jg = function(e) {
	O(this, "--with-selected-icon", Ft(e.target));
}, Mg = function(e) {
	e.length !== 0 && this.orientation !== "vertical" && this._inner?.style.setProperty("--_expanded-width", `${e[0].contentRect.width}px`);
}, Ng = function() {
	this._indicator && t(this, Eg, "f") && t(this, Og, "f").observe(this._indicator);
}, Pg.styles = l`:host { display: inline-block; vertical-align: middle; position: relative; outline: none; user-select: none; flex: 1; font-size: var(--m3e-nav-item-label-text-font-size, ${j.typescale.standard.label.medium.fontSize}); font-weight: var( --m3e-nav-item-label-text-font-weight, ${j.typescale.standard.label.medium.fontWeight} ); line-height: var( --m3e-nav-item-label-text-line-height, ${j.typescale.standard.label.medium.lineHeight} ); letter-spacing: var(--m3e-nav-item-label-text-tracking, ${j.typescale.standard.label.medium.tracking}); border-radius: var(--m3e-nav-item-shape, ${j.shape.corner.full}); align-self: var(--_nav-item-align-self); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } :host([hidden]) { display: none; } :host(:is(:state(--vertical), :--vertical)) { min-width: var(--_vertical-nav-item-min-width); width: var(--_vertical-nav-item-width); margin-inline: var(--_vertical-nav-item-margin-inline); } :host(:is(:state(--horizontal), :--horizontal)) { max-width: fit-content; } :host(:is(:state(--horizontal), :--horizontal)) .outer { margin-inline-start: var(--_horizontal-nav-item-leading-space); margin-inline-end: var(--_horizontal-nav-item-trailing-space); } :host(:not(:disabled):not([disabled-interactive])) { cursor: pointer; } :host([disabled-interactive]) { cursor: not-allowed; } .outer { height: 100%; } .outer, .inner { display: flex; align-items: center; justify-content: var(--_nav-item-justify-content, center); position: relative; border-radius: inherit; } .icon-wrapper { position: relative; flex: none; line-height: 0; } .base { contain: layout style; justify-content: unset; box-sizing: border-box; vertical-align: middle; display: inline-flex; align-items: center; justify-content: center; position: relative; width: 100%; } :host(:not(:is(:state(--no-animate), :--no-animate))) .base { transition: ${c(`margin-top ${j.motion.duration.short1} ${j.motion.easing.standard}`)}; } .icon { position: absolute; } .label { vertical-align: middle; } * { user-select: none; -webkit-user-select: none; } :host(:is(:state(--horizontal), :--horizontal)) .label { white-space: nowrap; } ::slotted([slot="icon"]), ::slotted([slot="selected-icon"]) { width: 1em; font-size: var(--m3e-nav-item-icon-size, 24px) !important; } :host(:not(:is(:state(--no-animate), :--no-animate))) .indicator { transition: ${c(`height ${j.motion.duration.short1} ${j.motion.easing.standard}`)}; } :host(:not([selected])) slot[name="selected-icon"], :host(:not(:is(:state(--with-selected-icon), :--with-selected-icon))) slot[name="selected-icon"], :host([selected]:is(:state(--with-selected-icon), :--with-selected-icon)) slot[name="icon"] { display: none; } .indicator { --m3e-selection-indicator-color: var( --m3e-nav-item-active-container-color, ${j.color.secondaryContainer} ); } :host(:not([selected]):not(:disabled):not([disabled-interactive])) .indicator { --m3e-selection-indicator-state-layer-hover-color: var( --m3e-nav-item-inactive-hover-state-layer-color, ${j.color.onSecondaryContainer} ); --m3e-selection-indicator-state-layer-focus-color: var( --m3e-nav-item-inactive-focus-state-layer-color, ${j.color.onSecondaryContainer} ); --m3e-selection-indicator-state-layer-pressed-color: var( --m3e-nav-item-inactive-pressed-state-layer-color, ${j.color.onSecondaryContainer} ); } :host(:not([selected]):not(:disabled):not([disabled-interactive])) .label { color: var(--m3e-nav-item-inactive-label-text-color, ${j.color.onSurfaceVariant}); } :host(:not([selected]):not(:disabled):not([disabled-interactive])) .icon { color: var(--m3e-nav-item-inactive-icon-color, ${j.color.onSurfaceVariant}); } :host([selected]:not(:disabled):not([disabled-interactive])) .indicator { --m3e-selection-indicator-state-layer-hover-color: var( --m3e-nav-item-active-hover-state-layer-color, ${j.color.onSecondaryContainer} ); --m3e-selection-indicator-state-layer-focus-color: var( --m3e-nav-item-active-focus-state-layer-color, ${j.color.onSecondaryContainer} ); --m3e-selection-indicator-state-layer-pressed-color: var( --m3e-nav-item-active-pressed-state-layer-color, ${j.color.onSecondaryContainer} ); } :host([selected]:not(:disabled):not([disabled-interactive]):not(:is(:state(--horizontal), :--horizontal))) .label { color: var(--m3e-nav-item-active-label-text-color, ${j.color.secondary}); } :host([selected]:not(:disabled):not([disabled-interactive]):is(:state(--horizontal), :--horizontal)) .label, :host([selected]:not(:disabled):not([disabled-interactive])) .icon { color: var(--m3e-nav-item-active-icon-color, ${j.color.onSecondaryContainer}); } :host(:is(:state(--vertical), :--vertical)) .outer { align-self: stretch; align-items: flex-start; } :host(:is(:state(--vertical), :--vertical)) .label { text-align: center; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; line-clamp: 2; } :host(:is(:state(--vertical), :--vertical)) .base { flex-direction: column; row-gap: var(--m3e-nav-item-spacing, ${j.measurement.space50}); } :host(:is(:state(--horizontal), :--horizontal)) .base { margin-top: 0; } :host(:is(:state(--vertical), :--vertical)) .base { margin-top: var(--m3e-vertical-nav-item-active-indicator-margin, ${j.measurement.space75}); margin-bottom: var(--m3e-vertical-nav-item-active-indicator-margin, ${j.measurement.space75}); } :host(:is(:state(--vertical), :--vertical)) .indicator { top: var(--m3e-vertical-nav-item-active-indicator-margin, ${j.measurement.space75}); bottom: unset; } :host(:is(:state(--vertical), :--vertical):is(:state(--rtl), :--rtl)) .indicator { right: var(--_vertical-nav-item-inset-start); left: unset; } :host(:is(:state(--vertical), :--vertical):not(:is(:state(--rtl), :--rtl))) .indicator { left: var(--_vertical-nav-item-inset-start); right: unset; } :host(:is(:state(--vertical), :--vertical)) .indicator, :host(:is(:state(--vertical), :--vertical)) .icon-wrapper { min-width: var(--m3e-vertical-nav-item-active-indicator-width, 56px); } :host(:not(:is(:state(--no-animate), :--no-animate)):is(:state(--vertical), :--vertical)) .state-layer { ${c(`collapse ${j.motion.duration.medium1}, indicator-grow-bounce ${j.motion.duration.medium2} ${j.motion.easing.standardAccelerate}`)}; } :host(:not(:is(:state(--no-animate), :--no-animate)):is(:state(--vertical), :--vertical)) .indicator { animation: collapse ${j.motion.duration.medium1}; } @keyframes collapse { from { width: var(--_expanded-width, var(--m3e-vertical-nav-item-active-indicator-width, 56px)); } to { width: var(--m3e-vertical-nav-item-active-indicator-width, 56px); } } :host(:is(:state(--vertical), :--vertical)) .indicator, :host(:is(:state(--vertical), :--vertical)) .icon-wrapper { height: var(--m3e-vertical-nav-item-active-indicator-height, 32px); } :host(:is(:state(--vertical), :--vertical)) .icon { top: calc( calc(var(--m3e-vertical-nav-item-active-indicator-height, 32px) / 2) - calc( var(--m3e-nav-item-icon-size, 24px) / 2 ) ); left: calc( calc(var(--m3e-vertical-nav-item-active-indicator-width, 56px) / 2) - calc( var(--m3e-nav-item-icon-size, 24px) / 2 ) ); } :host(:is(:state(--vertical), :--vertical)) .focus-ring { border-radius: var(--m3e-nav-item-focus-ring-shape, ${j.shape.corner.medium}); } :host(:is(:state(--horizontal), :--horizontal)) .icon-wrapper { width: var(--m3e-nav-item-icon-size, 24px); height: var(--m3e-nav-item-icon-size, 24px); } :host(:is(:state(--horizontal), :--horizontal)) .base { padding: var(--m3e-horizontal-nav-item-padding, ${j.measurement.space200}); } :host(:is(:state(--horizontal), :--horizontal)) .label { flex: 1 1 auto; } :host(:is(:state(--horizontal), :--horizontal)) .base { column-gap: var(--m3e-nav-item-spacing, ${j.measurement.space50}); } :host(:is(:state(--horizontal), :--horizontal)) .indicator, :host(:is(:state(--horizontal), :--horizontal)) .inner { height: var(--m3e-horizontal-nav-item-active-indicator-height, 40px); } :host(:is(:state(--horizontal), :--horizontal)) .inner { width: fit-content; } .indicator { margin-inline: auto; } :host( :is(:state(--first), :--first):not(:is(:state(--no-animate), :--no-animate)):is( :state(--horizontal), :--horizontal ) ) .icon-wrapper, :host( :not(:is(:state(--first), :--first)):not(:is(:state(--no-animate), :--no-animate)):is( :state(--vertical), :--vertical ) ) .icon-wrapper { animation: ${c(`slide-down ${j.motion.duration.short2} ${j.motion.easing.standard}`)}; } :host( :not(:is(:state(--first), :--first)):not(:is(:state(--no-animate), :--no-animate)):is( :state(--horizontal), :--horizontal ) ) .icon-wrapper, :host( :is(:state(--first), :--first):not(:is(:state(--no-animate), :--no-animate)):is(:state(--vertical), :--vertical) ) .icon-wrapper { animation: ${c(`slide-up ${j.motion.duration.short2} ${j.motion.easing.standard}`)}; } @keyframes slide-down { from { transform: translateY(-4px); } to { transform: translateY(0); } } @keyframes slide-up { from { transform: translateY(4px); } to { transform: translateY(0); } } :host(:not(:is(:state(--no-animate), :--no-animate)):is(:state(--horizontal), :--horizontal)) .label { animation: horizontal-fade-in ${j.motion.duration.medium1}; } :host(:not(:is(:state(--no-animate), :--no-animate)):is(:state(--vertical), :--vertical)) .label { animation: vertical-fade-in ${j.motion.duration.medium1}; } @keyframes horizontal-fade-in { from { opacity: 0; } to { opacity: 1; } } @keyframes vertical-fade-in { from { opacity: 0; } to { opacity: 1; } } :host(:disabled) .label, :host([disabled-interactive]) .label { color: color-mix( in srgb, var(--m3e-nav-item-disabled-label-text-color, ${j.color.onSurface}) var(--m3e-nav-item-disabled-label-text-opacity, 38%), transparent ); } :host(:disabled) .icon, :host([disabled-interactive]) .icon { color: color-mix( in srgb, var(--m3e-nav-item-disabled-icon-color, ${j.color.onSurface}) var(--m3e-nav-item-disabled-icon-opacity, 38%), transparent ); } a { all: unset; display: block; position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px; z-index: 1; } @media (prefers-reduced-motion) { :host( :is(:state(--first), :--first):not(:is(:state(--no-animate), :--no-animate)):is( :state(--horizontal), :--horizontal ) ) .icon-wrapper, :host( :not(:is(:state(--first), :--first)):not(:is(:state(--no-animate), :--no-animate)):is( :state(--vertical), :--vertical ) ) .icon-wrapper, :host( :not(:is(:state(--first), :--first)):not(:is(:state(--no-animate), :--no-animate)):is( :state(--horizontal), :--horizontal ) ) .icon-wrapper, :host( :is(:state(--first), :--first):not(:is(:state(--no-animate), :--no-animate)):is( :state(--vertical), :--vertical ) ) .icon-wrapper, :host(:not(:is(:state(--no-animate), :--no-animate)):is(:state(--vertical), :--vertical)) .state-layer, :host(:not(:is(:state(--no-animate), :--no-animate)):is(:state(--vertical), :--vertical)) .indicator, :host(:not(:is(:state(--no-animate), :--no-animate)):is(:state(--horizontal), :--horizontal)) .label, :host(:not(:is(:state(--no-animate), :--no-animate)):is(:state(--vertical), :--vertical)) .label { animation: none; } :host(:not(:is(:state(--no-animate), :--no-animate))) .state-layer, :host(:not(:is(:state(--no-animate), :--no-animate))) .indicator, :host(:not(:is(:state(--no-animate), :--no-animate))) .base { transition: none; } } @media (forced-colors: active) { :host(:disabled) .label, :host([disabled-interactive]) .label, :host(:disabled) .icon, :host([disabled-interactive]) .icon { color: GrayText; } :host(:not([selected]):not(:disabled):not([disabled-interactive])) .label, :host(:not([selected]):not(:disabled):not([disabled-interactive])) .icon { color: ButtonText; } :host([selected]:not(:disabled):not([disabled-interactive])) .indicator { --m3e-selection-indicator-color: ButtonText; } :host(:is(:state(--vertical), :--vertical)[selected]:not(:disabled):not([disabled-interactive])) .label { color: ButtonText; } :host(:is(:state(--horizontal), :--horizontal)[selected]:not(:disabled):not([disabled-interactive])) .label, :host([selected]:not(:disabled):not([disabled-interactive])) .icon { forced-color-adjust: none; color: ButtonFace; } }`, e([w(".focus-ring")], Pg.prototype, "_focusRing", void 0), e([w(".indicator")], Pg.prototype, "_indicator", void 0), e([w(".inner")], Pg.prototype, "_inner", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], Pg.prototype, "orientation", void 0), Pg = e([T("m3e-nav-item")], Pg);
//#endregion
//#region node_modules/@m3e/web/dist/nav-rail.js
var Fg, Ig, Lg, Rg, zg = class extends Vi(Cg) {
	constructor() {
		super(), Fg.add(this), Ig.set(this, new pu().withHomeAndEnd().withWrap()), Lg.set(this, (e) => t(this, Fg, "m", Rg).call(this, e)), this[q].onSelectedItemsChange(() => {
			t(this, Ig, "f").updateActiveItem(this[q].activeItem);
		});
	}
	connectedCallback() {
		super.connectedCallback(), this.addEventListener("keydown", t(this, Lg, "f"));
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("keydown", t(this, Lg, "f"));
	}
	_updateItems() {
		let e = Fu.findInteractiveElements(this, !0), { added: n } = t(this, Ig, "f").setItems(e);
		if (!t(this, Ig, "f").activeItem) {
			let e = n.find((e) => !e.hasAttribute("disabled"));
			e && t(this, Ig, "f").updateActiveItem(e);
		}
		this.items.forEach((e, t) => O(e, "--first", t === 0)), super._updateItems();
	}
	_updateOrientation(e) {
		super._updateOrientation(e), this.querySelectorAll("m3e-fab").forEach((t) => t.toggleAttribute("extended", e === "horizontal"));
	}
};
Ig = /* @__PURE__ */ new WeakMap(), Lg = /* @__PURE__ */ new WeakMap(), Fg = /* @__PURE__ */ new WeakSet(), Rg = function(e) {
	t(this, Ig, "f").onKeyDown(e);
}, Lt(l`m3e-nav-rail > m3e-icon-button, m3e-nav-rail > m3e-fab { margin-block-end: var(--m3e-nav-rail-button-item-space, ${j.measurement.space200}); } m3e-nav-rail > m3e-icon-button { margin-inline-start: var(--m3e-nav-rail-icon-button-inset, ${j.measurement.space100}); }`), zg.styles = l`:host { display: block; overflow-x: hidden; overflow-y: auto; scrollbar-width: ${j.scrollbar.thinWidth}; scrollbar-color: ${j.scrollbar.color}; } :host([hidden]) { display: none; } :host(:not(:is(:state(--no-animate), :--no-animate))) { transition: ${c(`width ${j.motion.spring.defaultSpatial}`)}; } .base { contain: layout style; display: flex; width: inherit; flex-direction: column; align-items: flex-start; box-sizing: border-box; padding-block-start: var(--m3e-nav-rail-top-space, ${j.measurement.space550}); padding-block-end: var(--m3e-nav-rail-bottom-space, ${j.measurement.space100}); padding-inline: var(--m3e-nav-rail-inline-padding, ${j.measurement.space250}); } :host(:is(:state(--compact), :--compact)) { width: var(--m3e-nav-rail-compact-width, 96px); } :host(:is(:state(--compact), :--compact)) .base { --_vertical-nav-item-width: var(--m3e-nav-rail-compact-width, 96px); --_vertical-nav-item-margin-inline: calc( 0px - var(--m3e-nav-rail-inline-padding, ${j.measurement.space250}) ); --_vertical-nav-item-inset-start: calc(50% - calc(var(--m3e-vertical-nav-item-active-indicator-width, 56px)) / 2); } :host(:not(:is(:state(--compact), :--compact))) { width: var(--m3e-nav-rail-expanded-width, 220px); } :host(:not(:is(:state(--compact), :--compact))) { --m3e-horizontal-nav-item-active-indicator-height: var(--m3e-nav-rail-expanded-item-height, 56px); --_nav-item-align-self: stretch; --_nav-item-justify-content: flex-start; } ::slotted(*) { flex: none; } ::slotted(m3e-fab) { --m3e-fab-container-elevation: ${j.elevation.level0}; --m3e-fab-disabled-container-elevation: ${j.elevation.level0}; --m3e-fab-focus-container-elevation: ${j.elevation.level0}; --m3e-fab-pressed-container-elevation: ${j.elevation.level0}; --m3e-fab-hover-container-elevation: ${j.elevation.level1}; --m3e-fab-lowered-container-elevation: ${j.elevation.level0}; --m3e-fab-lowered-disabled-container-elevation: ${j.elevation.level0}; --m3e-fab-lowered-focus-container-elevation: ${j.elevation.level0}; --m3e-fab-lowered-pressed-container-elevation: ${j.elevation.level0}; --m3e-fab-lowered-hover-container-elevation: ${j.elevation.level1}; } @media (prefers-reduced-motion) { :host(:not(:is(:state(--no-animate), :--no-animate))) { transition: none; } }`, zg = e([T("m3e-nav-rail")], zg);
var Bg, Vg, Hg, Ug = class extends Ai(Wi) {
	constructor() {
		super(...arguments), Bg.add(this), Vg.set(this, new On(this, {
			target: null,
			config: { attributeFilter: ["class"] },
			callback: () => t(this, Bg, "m", Hg).call(this)
		}));
	}
	attach(e) {
		e instanceof zg && t(this, Vg, "f").observe(e), this.htmlFor && this.parentElement && Cu(this.parentElement, "aria-controls", this.htmlFor), super.attach(e), t(this, Bg, "m", Hg).call(this);
	}
	detach() {
		for (let e of t(this, Vg, "f").targets) t(this, Vg, "f").unobserve(e);
		this.parentElement && (this.htmlFor && wu(this.parentElement, "aria-controls", this.htmlFor), this.parentElement.ariaExpanded = null), super.detach();
	}
	_onClick() {
		this.control instanceof zg && (this.control.currentMode = this.control.currentMode === "compact" ? "expanded" : "compact");
	}
};
Vg = /* @__PURE__ */ new WeakMap(), Bg = /* @__PURE__ */ new WeakSet(), Hg = async function() {
	if (!this.parentElement) return;
	let e = this.control instanceof zg && this.control.currentMode === "expanded";
	this.parentElement.ariaExpanded = `${e}`, this.parentElement.hasAttribute("toggle") && this.parentElement.toggleAttribute("selected", e), this.parentElement instanceof S && (await this.parentElement.updateComplete, this.parentElement.ariaPressed = null);
}, Ug = e([T("m3e-nav-rail-toggle")], Ug);
//#endregion
//#region node_modules/@m3e/web/dist/loading-indicator.js
function Wg(e) {
	return e === "uncontained" || e === "contained";
}
var Gg = {
	activeIndicatorSize: c("var(--m3e-loading-indicator-size, 38px)"),
	activeIndicatorColor: c(`var(--m3e-loading-indicator-active-indicator-color, ${j.color.primary})`),
	containedActiveIndicatorColor: c(`var(--m3e-loading-indicator-contained-active-indicator-color, ${j.color.onPrimaryContainer})`),
	containedContainerColor: c(`var(--m3e-loading-indicator-contained-container-color, ${j.color.secondaryContainer})`),
	containerShape: c(`var(--m3e-loading-indicator-container-shape, ${j.shape.corner.full})`),
	containerSize: c("var(--m3e-loading-indicator-container-size, 48px)")
}, Kg = {
	"4-sided-cookie": "M230.389 50.473C293.109 23.2328 356.767 86.8908 329.527 149.611L325.023 159.981C316.707 179.13 316.707 200.87 325.023 220.019L329.527 230.389C356.767 293.109 293.109 356.767 230.389 329.527L220.019 325.023C200.87 316.707 179.13 316.707 159.981 325.023L149.611 329.527C86.8908 356.767 23.2328 293.109 50.473 230.389L54.9768 220.019C63.2934 200.87 63.2934 179.13 54.9768 159.981L50.473 149.611C23.2328 86.8908 86.8908 23.2328 149.611 50.473L159.981 54.9768C179.13 63.2934 200.87 63.2934 220.019 54.9768L230.389 50.473Z",
	"9-sided-cookie": "M154.828 43.2756C156.574 41.8498 157.448 41.1369 158.245 40.535C177.03 26.3548 202.97 26.3548 221.755 40.535C222.552 41.1369 223.425 41.8498 225.172 43.2756C225.952 43.9121 226.342 44.2303 226.727 44.5333C235.567 51.4788 246.406 55.4147 257.652 55.7636C258.143 55.7788 258.647 55.785 259.654 55.7975C261.911 55.8255 263.039 55.8395 264.037 55.8898C287.563 57.0742 307.435 73.7107 312.689 96.6205C312.912 97.5928 313.121 98.6991 313.541 100.911C313.728 101.899 313.822 102.393 313.922 102.872C316.219 113.862 321.986 123.828 330.377 131.308C330.743 131.635 331.125 131.962 331.888 132.618C333.599 134.087 334.454 134.821 335.187 135.5C352.445 151.495 356.95 176.983 346.215 197.903C345.76 198.791 345.208 199.773 344.104 201.737C343.611 202.613 343.364 203.052 343.132 203.483C337.812 213.375 335.809 224.708 337.418 235.82C337.488 236.304 337.569 236.8 337.732 237.792C338.096 240.014 338.278 241.125 338.402 242.115C341.318 265.436 328.347 287.851 306.647 296.991C305.726 297.379 304.67 297.778 302.559 298.574C301.617 298.929 301.146 299.107 300.69 299.289C290.241 303.455 281.406 310.852 275.48 320.395C275.221 320.811 274.964 321.243 274.449 322.107C273.297 324.043 272.721 325.011 272.178 325.849C259.387 345.584 235.011 354.436 212.498 347.521C211.543 347.228 210.477 346.856 208.347 346.112C207.396 345.78 206.921 345.614 206.455 345.461C195.767 341.951 184.233 341.951 173.545 345.461C173.079 345.614 172.603 345.78 171.652 346.112C169.522 346.856 168.457 347.228 167.502 347.521C144.989 354.436 120.613 345.584 107.822 325.849C107.279 325.011 106.703 324.043 105.55 322.107C105.036 321.243 104.779 320.811 104.52 320.395C98.5939 310.852 89.7583 303.455 79.3096 299.289C78.8539 299.107 78.3827 298.929 77.4404 298.574C75.3294 297.778 74.274 297.379 73.3529 296.991C51.6523 287.851 38.6819 265.436 41.598 242.115C41.7218 241.125 41.9039 240.014 42.2682 237.792C42.4308 236.8 42.5121 236.304 42.5822 235.82C44.1908 224.708 42.188 213.375 36.8675 203.483C36.6354 203.052 36.389 202.613 35.8962 201.737C34.7921 199.773 34.2401 198.791 33.7845 197.903C23.0499 176.983 27.5544 151.495 44.8128 135.5C45.5454 134.821 46.4007 134.087 48.1113 132.618C48.875 131.962 49.2568 131.635 49.6228 131.308C58.0134 123.828 63.7804 113.862 66.0777 102.872C66.1779 102.393 66.2715 101.899 66.4588 100.911C66.8783 98.699 67.088 97.5928 67.311 96.6204C72.5652 73.7107 92.4369 57.0742 115.962 55.8898C116.961 55.8395 118.089 55.8255 120.346 55.7975C121.353 55.785 121.857 55.7788 122.347 55.7636C133.594 55.4147 144.432 51.4788 153.272 44.5333C153.658 44.2303 154.048 43.9121 154.828 43.2756Z",
	oval: "M271.309 271.309C201.705 340.913 108.877 360.935 63.9707 316.029C19.0648 271.123 39.0867 178.295 108.691 108.691C178.295 39.0867 271.123 19.0648 316.029 63.9707C360.935 108.877 340.913 201.705 271.309 271.309Z",
	pentagon: "M155.064 49.459C176.093 34.1803 204.569 34.1803 225.598 49.459L322.926 120.171C343.955 135.45 352.754 162.532 344.722 187.253L307.546 301.668C299.514 326.39 276.476 343.127 250.483 343.127H130.18C104.186 343.127 81.1489 326.39 73.1164 301.668L35.9407 187.253C27.9082 162.532 36.7077 135.45 57.737 120.171L155.064 49.459Z",
	pill: "M116.116 71.7851C169.162 18.7383 255.168 18.7383 308.215 71.7851C361.262 124.832 361.262 210.838 308.215 263.884L263.884 308.215C210.838 361.262 124.832 361.262 71.7851 308.215C18.7383 255.168 18.7383 169.162 71.7851 116.116L116.116 71.7851Z",
	"soft-burst": "M175.147 33.1508C181.983 22.2831 198.017 22.2831 204.853 33.1508L221.238 59.2009C225.731 66.3458 234.797 69.2506 242.692 66.0751L271.475 54.4972C283.482 49.6671 296.455 58.9613 295.507 71.7154L293.235 102.288C292.612 110.673 298.215 118.278 306.494 120.284L336.681 127.601C349.275 130.653 354.23 145.692 345.861 155.461L325.8 178.877C320.298 185.3 320.298 194.7 325.8 201.123L345.861 224.539C354.23 234.308 349.275 249.347 336.681 252.399L306.494 259.716C298.215 261.722 292.612 269.327 293.235 277.712L295.507 308.285C296.455 321.039 283.482 330.333 271.475 325.503L242.692 313.925C234.797 310.749 225.731 313.654 221.238 320.799L204.853 346.849C198.017 357.717 181.983 357.717 175.147 346.849L158.762 320.799C154.269 313.654 145.203 310.749 137.308 313.925L108.525 325.503C96.5177 330.333 83.5454 321.039 84.4931 308.285L86.7649 277.712C87.388 269.327 81.785 261.722 73.5056 259.716L43.3186 252.399C30.7252 249.347 25.7702 234.308 34.1391 224.539L54.1997 201.123C59.7018 194.7 59.7018 185.3 54.1997 178.877L34.1391 155.461C25.7702 145.692 30.7252 130.653 43.3186 127.601L73.5056 120.284C81.785 118.278 87.388 110.673 86.7649 102.288L84.4931 71.7154C83.5454 58.9613 96.5177 49.6671 108.525 54.4972L137.308 66.0751C145.203 69.2506 154.269 66.3458 158.762 59.201L175.147 33.1508Z",
	sunny: "M276.453 68.8118C286.405 69.4881 291.381 69.8263 295.404 71.5853C301.223 74.1305 305.87 78.7766 308.415 84.5965C310.174 88.6186 310.512 93.5948 311.188 103.547L312.732 126.259C313.005 130.284 313.142 132.296 313.579 134.219C314.212 136.997 315.31 139.648 316.827 142.059C317.877 143.728 319.203 145.248 321.856 148.288L336.824 165.438C343.384 172.954 346.663 176.712 348.263 180.8C350.579 186.715 350.579 193.285 348.263 199.2C346.663 203.288 343.384 207.046 336.824 214.562L321.856 231.712C319.203 234.752 317.877 236.272 316.827 237.941C315.31 240.352 314.212 243.003 313.579 245.781C313.142 247.704 313.005 249.716 312.732 253.741L311.188 276.453C310.512 286.405 310.174 291.381 308.415 295.404C305.87 301.223 301.223 305.87 295.404 308.415C291.381 310.174 286.405 310.512 276.453 311.188L253.741 312.732C249.716 313.005 247.704 313.142 245.781 313.579C243.003 314.212 240.352 315.31 237.941 316.827C236.272 317.877 234.752 319.203 231.712 321.856L214.562 336.824C207.046 343.384 203.288 346.663 199.2 348.263C193.285 350.579 186.715 350.579 180.8 348.263C176.712 346.663 172.954 343.384 165.438 336.824L148.288 321.856C145.248 319.203 143.728 317.877 142.059 316.827C139.648 315.31 136.997 314.212 134.219 313.579C132.296 313.142 130.284 313.005 126.259 312.732L103.547 311.188C93.5947 310.512 88.6186 310.174 84.5965 308.415C78.7766 305.87 74.1305 301.223 71.5853 295.404C69.8263 291.381 69.4881 286.405 68.8118 276.453L67.2684 253.741C66.9949 249.716 66.8581 247.704 66.4206 245.781C65.7883 243.003 64.6903 240.352 63.173 237.941C62.123 236.272 60.7965 234.752 58.1437 231.712L43.1756 214.562C36.6164 207.046 33.3369 203.288 31.7366 199.2C29.4211 193.285 29.4211 186.715 31.7366 180.8C33.3369 176.712 36.6164 172.954 43.1756 165.438L58.1437 148.288C60.7965 145.248 62.123 143.728 63.173 142.059C64.6903 139.648 65.7883 136.997 66.4206 134.219C66.8581 132.296 66.9949 130.284 67.2684 126.259L68.8118 103.547C69.4881 93.5948 69.8263 88.6186 71.5853 84.5965C74.1305 78.7766 78.7766 74.1305 84.5965 71.5853C88.6186 69.8263 93.5948 69.4881 103.547 68.8118L126.259 67.2684C130.284 66.9949 132.296 66.8581 134.219 66.4206C136.997 65.7883 139.648 64.6903 142.059 63.173C143.728 62.123 145.248 60.7966 148.288 58.1437L165.438 43.1756C172.954 36.6164 176.712 33.3369 180.8 31.7366C186.715 29.4211 193.285 29.4211 199.2 31.7366C203.288 33.3369 207.046 36.6164 214.562 43.1756L231.712 58.1437C234.752 60.7966 236.272 62.123 237.941 63.173C240.352 64.6903 243.003 65.7883 245.781 66.4206C247.704 66.8581 249.716 66.9949 253.741 67.2684L276.453 68.8118Z"
}, qg = [], Jg = /* @__PURE__ */ new Map();
for (let e in Kg) qg.push(Kg[e]), Jg.set(e, qg.length - 1);
var Yg = {};
qg = Nt(qg, 300);
for (let e of Jg) Yg[e[0]] = c(qg[e[1]]);
var Xg = Yg, Zg, Qg, $g = class extends Li(M(E(S), "progressbar")) {
	constructor() {
		super(...arguments), Zg.add(this), this.variant = "uncontained";
	}
	connectedCallback() {
		super.connectedCallback(), t(this, Zg, "m", Qg).call(this), this.ariaValueMin = this.ariaValueMin || "0", this.ariaValueMax = this.ariaValueMax || "100";
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this._container?.classList.toggle("animate", !1);
	}
	reconnectedCallback() {
		super.reconnectedCallback(), this._container?.classList.toggle("animate", !0);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, Zg, "m", Qg).call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), this._container?.classList.toggle("animate", !0);
	}
	render() {
		return b`<div class="container" aria-hidden="true"><div class="active-indicator-wrapper"><div class="active-indicator"></div></div></div>`;
	}
};
Zg = /* @__PURE__ */ new WeakSet(), Qg = function() {
	Wg(this.variant) || (this.variant = "uncontained"), wr(this, this.variant, "contained", "uncontained");
}, $g.styles = l`:host { display: inline-block; aspect-ratio: 1 / 1; contain: strict; vertical-align: middle; content-visibility: auto; } :host([hidden]) { display: none; } :host(:is(:state(--uncontained), :--uncontained)) { width: ${Gg.activeIndicatorSize}; } :host(:is(:state(--contained), :--contained)) { width: ${Gg.containerSize}; } :host(:is(:state(--uncontained), :--uncontained)) .active-indicator { background-color: ${Gg.activeIndicatorColor}; } :host(:is(:state(--contained), :--contained)) .active-indicator { background-color: ${Gg.containedActiveIndicatorColor}; } :host(:is(:state(--contained), :--contained)) .container { background-color: ${Gg.containedContainerColor}; } .container { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; border-radius: ${Gg.containerShape}; } .active-indicator { margin: auto; aspect-ratio: 1 / 1; width: calc(${Gg.activeIndicatorSize} * 0.842); transform-origin: center; transition: clip-path ${j.motion.spring.slowEffects}; will-change: transform, clip-path; --_polygon-soft-burst: polygon(${Xg["soft-burst"]}); --_polygon-9-sided-cookie: polygon(${Xg["9-sided-cookie"]}); --_polygon-pentagon: polygon(${Xg.pentagon}); --_polygon-pill: polygon(${Xg.pill}); --_polygon-sunny: polygon(${Xg.sunny}); --_polygon-4-sided-cookie: polygon(${Xg["4-sided-cookie"]}); --_polygon-oval: polygon(${Xg.oval}); } .container.animate .active-indicator-wrapper { animation: rotate-outer 4666ms linear infinite; transform-origin: center; display: flex; align-items: center; justify-content: center; will-change: transform; } @keyframes rotate-outer { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } } .container.animate .active-indicator { animation: rotate-inner 4666ms cubic-bezier(0.34, 0.88, 0.34, 1) infinite; } @keyframes rotate-inner { 0% { clip-path: var(--_polygon-soft-burst); transform: rotate(0deg); } 14% { clip-path: var(--_polygon-9-sided-cookie); transform: rotate(154deg) scale(1); } 29% { clip-path: var(--_polygon-pentagon); transform: rotate(309deg) scale(1); } 43% { clip-path: var(--_polygon-pill); transform: rotate(463deg) scale(1); } 57% { clip-path: var(--_polygon-sunny); transform: rotate(617deg) scale(1); } 71% { clip-path: var(--_polygon-4-sided-cookie); transform: rotate(771deg) scale(1); } 83% { clip-path: var(--_polygon-oval); transform: rotate(926deg) scale(1); } 100% { clip-path: var(--_polygon-soft-burst); transform: rotate(1080deg) scale(1); } } @media (forced-colors: active) { .active-indicator { background-color: CanvasText !important; } }`, e([w(".container")], $g.prototype, "_container", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], $g.prototype, "variant", void 0), $g = e([T("m3e-loading-indicator")], $g);
//#endregion
//#region node_modules/lit-html/directives/class-map.js
var e_ = et(class extends tt {
	constructor(e) {
		if (super(e), e.type !== $e.ATTRIBUTE || e.name !== "class" || e.strings?.length > 2) throw Error("`classMap()` can only be used in the `class` attribute and must be the only part in the attribute.");
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
		return Ae;
	}
});
//#endregion
//#region node_modules/@m3e/web/dist/progress-indicator.js
function t_(e) {
	return e === "flat" || e === "wavy";
}
var n_, r_, i_ = class extends Li(E(M(S, "progressbar"), !0)) {
	constructor() {
		super(...arguments), n_.add(this), this.value = 0, this.max = 100, this.variant = "flat";
	}
	connectedCallback() {
		super.connectedCallback(), t(this, n_, "m", r_).call(this), this.ariaValueMin = "0";
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("variant") && t(this, n_, "m", r_).call(this);
	}
	update(e) {
		super.update(e), e.has("value") && (this.ariaValueNow = `${this.value}`), e.has("max") && (this.ariaValueMax = `${this.max}`);
	}
};
n_ = /* @__PURE__ */ new WeakSet(), r_ = function() {
	t_(this.variant) || (this.variant = "flat"), wr(this, this.variant, "flat", "wavy");
}, i_.styles = l`:host([hidden]) { display: none; } @media (forced-colors: active) { :host { --m3e-progress-indicator-color: CanvasText; --m3e-progress-indicator-track-color: Canvas; } }`, e([C({
	type: Number,
	reflect: !0
})], i_.prototype, "value", void 0), e([C({ type: Number })], i_.prototype, "max", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], i_.prototype, "variant", void 0);
var X, a_, o_, s_, c_, l_, u_, d_, f_, p_, m_, h_, g_, __, v_, y_, b_, x_, S_, C_, w_, T_, E_, D_, O_, k_, A_ = 1.575, j_ = k_ = class extends i_ {
	constructor() {
		super(...arguments), X.add(this), a_.set(this, `m3e-circular-progress-mask-${k_.__nextMaskId++}`), o_.set(this, 0), s_.set(this, 0), c_.set(this, 0), l_.set(this, 0), u_.set(this, void 0), d_.set(this, void 0), f_.set(this, new tr(this, {
			skipInitial: !0,
			target: null,
			callback: () => {
				t(this, X, "m", y_).call(this), t(this, X, "m", b_).call(this), queueMicrotask(() => this.requestUpdate());
			}
		})), p_.set(this, new ct(this, (e, n) => t(this, X, "m", x_).call(this, n))), this.indeterminate = !1;
	}
	update(e) {
		super.update(e), e.has("indeterminate") && (this.ariaValueNow = this.indeterminate ? null : `${this.value}`);
	}
	reconnectedCallback() {
		super.reconnectedCallback(), t(this, X, "m", m_).call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), t(this, X, "m", m_).call(this);
	}
	updated(e) {
		super.updated(e), e.has("indeterminate") && (n(this, u_, this.shadowRoot?.querySelector(".spinner.active-track"), "f"), n(this, d_, this.shadowRoot?.querySelector(".spinner.track"), "f"), this.indeterminate ? t(this, p_, "f").start() : t(this, p_, "f").stop());
	}
	render() {
		return this.variant === "wavy" ? t(this, X, "m", g_).call(this) : t(this, X, "m", h_).call(this);
	}
};
a_ = /* @__PURE__ */ new WeakMap(), o_ = /* @__PURE__ */ new WeakMap(), s_ = /* @__PURE__ */ new WeakMap(), c_ = /* @__PURE__ */ new WeakMap(), l_ = /* @__PURE__ */ new WeakMap(), u_ = /* @__PURE__ */ new WeakMap(), d_ = /* @__PURE__ */ new WeakMap(), f_ = /* @__PURE__ */ new WeakMap(), p_ = /* @__PURE__ */ new WeakMap(), X = /* @__PURE__ */ new WeakSet(), m_ = function() {
	let e = this.shadowRoot?.querySelector(".diameter-and-stroke");
	e && (t(this, X, "m", y_).call(this), t(this, f_, "f").observe(e));
	let n = this.shadowRoot?.querySelector(".amplitude-and-wavelength");
	n && (t(this, X, "m", y_).call(this), t(this, f_, "f").observe(n));
}, h_ = function() {
	if (this.indeterminate) {
		let e = t(this, X, "m", D_).call(this, {
			startAngle: -45,
			endAngle: 90 + t(this, s_, "f")
		}), n = t(this, X, "m", D_).call(this, {
			startAngle: -t(this, s_, "f"),
			endAngle: 135
		});
		return b`<div class="progress" aria-hidden="true"><div class="spinner"><div class="left"><svg width="${t(this, o_, "f")}" height="${t(this, o_, "f")}" viewBox="${e.viewBox}" class="circle"><path class="active-track" d="${e.path}" stroke="currentColor" stroke-width="${t(this, s_, "f")}" fill="none" stroke-linecap="round"/></svg></div><div class="right"><svg width="${t(this, o_, "f")}" height="${t(this, o_, "f")}" viewBox="${n.viewBox}" class="circle"><path class="active-track" d="${n.path}" stroke="currentColor" stroke-width="${t(this, s_, "f")}" fill="none" stroke-linecap="round"/></svg></div></div></div>${t(this, X, "m", __).call(this)}`;
	}
	let e = t(this, X, "m", C_).call(this, t(this, s_, "f") * 2, t(this, o_, "f")), n = this.value / this.max * 360;
	n > 0 && (n = Math.max(0, e, n));
	let r = t(this, X, "m", D_).call(this, {
		gap: n < 360 ? t(this, s_, "f") : 0,
		endAngle: n
	}), i = t(this, X, "m", D_).call(this, {
		gap: n > 0 ? t(this, s_, "f") : 0,
		startAngle: n,
		endAngle: 360
	});
	return b`<div class="progress" aria-hidden="true"><svg width="${t(this, o_, "f")}" height="${t(this, o_, "f")}" viewBox="${r.viewBox}">${n > 0 ? ke`<path
            class="active-track"
            d="${r.path}"
            stroke="currentColor"
            stroke-width=${t(this, s_, "f")}
            fill="none"
            stroke-linecap="round"
          />` : x} ${360 - n >= e ? ke`<path
                class="track"
                d="${i.path}"
                stroke="currentColor"
                stroke-width=${t(this, s_, "f")}
                fill="none"
                stroke-linecap="round"
              />` : x}</svg></div>${t(this, X, "m", __).call(this)}${t(this, X, "m", v_).call(this)}`;
}, g_ = function() {
	if (this.indeterminate) return b`<div class="progress" aria-hidden="true"><svg width="${t(this, o_, "f")}" height="${t(this, o_, "f")}" viewBox="${t(this, X, "m", O_).call(this, { endAngle: 20 }).viewBox}"><path class="spinner active-track" stroke="currentColor" stroke-width="${t(this, s_, "f")}" stroke-linecap="round" stroke-linejoin="round" fill="none"/><path class="spinner track" stroke="currentColor" stroke-width="${t(this, s_, "f")}" fill="none" stroke-linecap="round"/></svg></div>${t(this, X, "m", __).call(this)}`;
	let e = t(this, X, "m", C_).call(this, t(this, s_, "f") * 2, t(this, o_, "f")), n = this.value / this.max * 360;
	n > 0 && (n = Math.max(0, e, n));
	let r = n <= e + e / 2 || n == 360 ? 0 : t(this, c_, "f"), i = t(this, X, "m", D_).call(this, {
		gap: n < 360 ? t(this, s_, "f") : 0,
		endAngle: n
	}), a = r == 0 ? i : t(this, X, "m", O_).call(this, {
		endAngle: 360,
		amplitude: r
	}), o = t(this, X, "m", D_).call(this, {
		gap: n > 0 ? t(this, s_, "f") : 0,
		startAngle: n,
		endAngle: 360
	}), s = r > 0 ? r + t(this, s_, "f") / 2 : t(this, s_, "f");
	return b`<svg class="progress" viewBox="${o.viewBox}" aria-hidden="true">${n > 0 ? ke`${r > 0 ? ke`<defs>
          <mask id="${t(this, a_, "f")}">
            <path
              d="${i.path}"
              stroke="white"
              stroke-width="${t(this, s_, "f") + s}"
              fill="none"
              stroke-linecap="round"
            />
          </mask>
        </defs>` : x}
        <g class="active-track" mask="${nt(r > 0 ? zt(t(this, a_, "f")) : void 0)}">
          <path
            class="${e_({ wave: r > 0 })}"
            d="${a.path}"
            stroke="currentColor"
            stroke-width=${t(this, s_, "f")}
            stroke-linecap="round"
            stroke-linejoin="round"
            fill="none"
          />
        </g>` : x} ${360 - n >= e ? ke`<path
                class="track"
                d="${o.path}"
                stroke="currentColor"
                stroke-width=${t(this, s_, "f")}
                fill="none"
                stroke-linecap="round"
              />` : x}</svg> ${t(this, X, "m", __).call(this)}${t(this, X, "m", v_).call(this)}`;
}, __ = function() {
	return b`<div class="diameter-and-stroke" aria-hidden="true"></div><div class="amplitude-and-wavelength" aria-hidden="true"></div>`;
}, v_ = function() {
	return b`<div class="content" aria-hidden="true"><slot></slot></div>`;
}, y_ = function() {
	let e = this.shadowRoot?.querySelector(".diameter-and-stroke");
	e && (n(this, o_, e.clientWidth, "f"), n(this, s_, e.clientHeight, "f"));
}, b_ = function() {
	let e = this.shadowRoot?.querySelector(".amplitude-and-wavelength");
	e && (n(this, c_, e.clientWidth, "f"), n(this, l_, e.clientHeight, "f"));
}, x_ = function(e) {
	if (t(this, c_, "f") === 0 || t(this, l_, "f") === 0) return;
	let n = t(this, X, "m", S_).call(this, e);
	t(this, u_, "f")?.setAttribute("d", t(this, X, "m", O_).call(this, { endAngle: n }).path), t(this, d_, "f")?.setAttribute("d", t(this, X, "m", D_).call(this, {
		gap: t(this, X, "m", C_).call(this, t(this, s_, "f"), t(this, o_, "f")),
		startAngle: n
	}).path);
}, S_ = function(e) {
	let n = t(this, X, "m", C_).call(this, t(this, s_, "f")) * 2, r = 18 + n, i = 280 - n, a = A_, o = a, s = a, c = a, l = e % 6.3;
	if (l < o) return r;
	if (l < 3.15) {
		let e = (l - o) / s;
		return r + (i - r) * (e * e * (3 - 2 * e));
	}
	if (l < 4.725) return i;
	let u = (l - 4.725) / c;
	return i - (i - r) * (u * u * (3 - 2 * u));
}, C_ = function(e, n = t(this, c_, "f")) {
	return e * (360 / (2 * Math.PI * t(this, X, "m", E_).call(this, n).r));
}, w_ = function(e) {
	return (e - 90) * (Math.PI / 180);
}, T_ = function(e, n) {
	let r = t(this, X, "m", w_).call(this, n);
	return {
		x: e.cx + e.r * Math.cos(r),
		y: e.cy + e.r * Math.sin(r)
	};
}, E_ = function(e) {
	e += t(this, s_, "f") / 2;
	let n = t(this, o_, "f") / 2;
	return {
		cx: n + e,
		cy: n + e,
		r: n,
		padding: e
	};
}, D_ = function({ startAngle: e = 0, endAngle: n = 360, gap: r = 0, padding: i = t(this, c_, "f") }) {
	if (t(this, o_, "f") === 0 || t(this, s_, "f") === 0) return {
		path: "",
		viewBox: "0 0 0 0"
	};
	let a = t(this, X, "m", E_).call(this, i);
	r > 0 && (e += t(this, X, "m", C_).call(this, r, i), n -= t(this, X, "m", C_).call(this, r, i)), n - e >= 360 && (n = e + 359.999);
	let o = t(this, X, "m", T_).call(this, a, n), s = t(this, X, "m", T_).call(this, a, e);
	return {
		path: `M ${o.x} ${o.y} A ${a.r} ${a.r} 0 ${n - e <= 180 ? "0" : "1"} 0 ${s.x} ${s.y}`,
		viewBox: `0 0 ${t(this, o_, "f") + a.padding * 2} ${t(this, o_, "f") + a.padding * 2}`
	};
}, O_ = function({ startAngle: e = 0, endAngle: n = 360, gap: r = 0, padding: i = t(this, c_, "f"), amplitude: a = t(this, c_, "f"), steps: o = 200 }) {
	if (t(this, o_, "f") === 0 || t(this, s_, "f") === 0) return {
		path: "",
		viewBox: "0 0 0 0"
	};
	let s = t(this, X, "m", E_).call(this, i);
	r > 0 && (e += t(this, X, "m", C_).call(this, r, i), n -= t(this, X, "m", C_).call(this, r, i));
	let c = t(this, X, "m", w_).call(this, e), l = t(this, X, "m", w_).call(this, n);
	e === n ? l = c : l < c && (l += Math.PI * 2);
	let u = l - c, d = 2 * Math.PI * s.r / t(this, l_, "f"), f = Math.PI / 2 * (d - 1), p = [];
	for (let e = 0; e <= o; e++) {
		let t = c + (o === 0 ? 0 : e / o) * u, n = Math.sin(t * d + f), r = s.r - a * n, i = r * Math.cos(t) + s.cx, l = r * Math.sin(t) + s.cy;
		p.push([i, l]);
	}
	return {
		path: p.length === 1 ? `M ${p[0][0]},${p[0][1]}` : `M ${p[0][0]},${p[0][1]} ` + p.slice(1).map(([e, t]) => `L ${e},${t}`).join(" "),
		viewBox: `0 0 ${t(this, o_, "f") + s.padding * 2} ${t(this, o_, "f") + s.padding * 2}`
	};
}, j_.styles = [i_.styles, l`:host { display: inline-flex; vertical-align: middle; aspect-ratio: 1; position: relative; align-items: center; justify-content: center; } :host([hidden]) { display: none; } .progress { --_arc-duration: 1333ms; --_cycle-duration: calc(4 * var(--_arc-duration)); --_linear-rotate-duration: calc(var(--_arc-duration) * 360 / 306); --_indeterminate-easing: cubic-bezier(0.4, 0, 0.2, 1); } .active-track { transition: stroke-dashoffset 500ms cubic-bezier(0, 0, 0.2, 1); } :host(:is(:state(--flat), :--flat)) .progress { flex: 1; align-self: stretch; pointer-events: none; } .progress, .spinner, .left, .right, .content, .circle { position: absolute; inset: 0; } .content { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; } .diameter-and-stroke, .amplitude-and-wavelength { visibility: hidden; position: absolute; } .diameter-and-stroke { width: inherit; height: var(--m3e-circular-progress-indicator-thickness, 4px); } .amplitude-and-wavelength { width: var(--m3e-circular-wavy-progress-indicator-amplitude, 1.6px); height: var(--m3e-circular-wavy-progress-indicator-wavelength, 15px); } :host(:is(:state(--flat), :--flat)) { width: var(--m3e-circular-flat-progress-indicator-diameter, 40px); } :host(:is(:state(--wavy), :--wavy)) { width: var(--m3e-circular-wavy-progress-indicator-diameter, 48px); } :host(:is(:state(--flat), :--flat)[indeterminate]) .progress { animation: linear infinite linear-rotate; animation-duration: var(--_linear-rotate-duration); } :host(:is(:state(--flat), :--flat)[indeterminate]) .spinner { animation: infinite both rotate-arc; animation-duration: var(--_cycle-duration); animation-timing-function: var(--_indeterminate-easing); } :host(:is(:state(--wavy), :--wavy)[indeterminate]) .spinner { transform-origin: 50% 50%; animation: wavy-spin ${A_}s linear infinite; } .left { clip-path: inset(0); inset: 0 50% 0 0; } .right { clip-path: inset(0); inset: 0 0 0 50%; } .circle { animation: expand-arc; animation-iteration-count: infinite; animation-fill-mode: both; animation-duration: var(--_arc-duration), var(--_cycle-duration); animation-timing-function: var(--_indeterminate-easing); } .left .circle { rotate: 135deg; inset: 0 -100% 0 0; } .right .circle { rotate: 100deg; inset: 0 0 0 -100%; animation-delay: calc(-0.5 * var(--_arc-duration)), 0ms; } .track { color: var(--m3e-progress-indicator-track-color, ${j.color.secondaryContainer}); } .active-track { color: var(--m3e-progress-indicator-color, ${j.color.primary}); } .wave { animation: spin-reverse 8s linear infinite; transform-origin: 50% 50%; } @keyframes expand-arc { 0% { transform: rotate(265deg); } 50% { transform: rotate(130deg); } 100% { transform: rotate(265deg); } } @keyframes rotate-arc { 12.5% { transform: rotate(135deg); } 25% { transform: rotate(270deg); } 37.5% { transform: rotate(405deg); } 50% { transform: rotate(540deg); } 62.5% { transform: rotate(675deg); } 75% { transform: rotate(810deg); } 87.5% { transform: rotate(945deg); } 100% { transform: rotate(1080deg); } } @keyframes linear-rotate { to { transform: rotate(360deg); } } @keyframes spin-reverse { from { transform: rotate(360deg); } to { transform: rotate(0deg); } } @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } } @keyframes wavy-spin { 0% { transform: rotate(0deg); } 10% { transform: rotate(90deg); } 100% { transform: rotate(360deg); } } @media (forced-colors: active) { :host(:is(:state(--flat), :--flat)) circle { fill: Canvas; } :host(:is(:state(--flat), :--flat)) .circle { border-color: var(--m3e-progress-indicator-color, ${j.color.primary}) var(--m3e-progress-indicator-color, ${j.color.primary}) Canvas Canvas; } } @media (forced-colors: active) { .progress { --m3e-progress-indicator-track-color: GrayText; --m3e-progress-indicator-color: CanvasText; } }`], j_.__nextMaskId = 0, e([C({
	type: Boolean,
	reflect: !0
})], j_.prototype, "indeterminate", void 0), j_ = k_ = e([T("m3e-circular-progress-indicator")], j_);
function M_(e) {
	return e === "determinate" || e === "indeterminate" || e === "buffer" || e === "query";
}
var N_, P_, F_, I_, L_, R_, z_, B_, V_, H_, U_, W_, G_, K_, q_ = K_ = class extends id(i_) {
	constructor() {
		super(...arguments), N_.add(this), P_.set(this, `m3e-linear-progress-mask-${K_.__nextMaskId++}`), F_.set(this, 0), I_.set(this, 0), L_.set(this, 0), R_.set(this, new tr(this, {
			skipInitial: !0,
			target: null,
			callback: () => {
				t(this, N_, "m", U_).call(this), t(this, N_, "m", W_).call(this), queueMicrotask(() => this.requestUpdate());
			}
		})), this.mode = "determinate", this.bufferValue = 0;
	}
	connectedCallback() {
		super.connectedCallback(), t(this, N_, "m", z_).call(this);
	}
	reconnectedCallback() {
		super.reconnectedCallback(), t(this, N_, "m", B_).call(this);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("mode") && t(this, N_, "m", z_).call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), t(this, N_, "m", B_).call(this);
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
		let e = Math.max(0, Math.min(this.value, this.max)) / this.max * this.clientWidth, n = this.mode === "indeterminate" || this.mode === "query" ? this.clientWidth : e + t(this, L_, "f") * 3, r = this.variant === "wavy" && t(this, F_, "f") > 0 && t(this, I_, "f") > 0 && t(this, L_, "f") > 0 ? t(this, N_, "m", G_).call(this, n) : void 0;
		return b`<div class="progress" aria-hidden="true" style="${gr({ "--_translate-x": `-${e}px` })}">${r ? x : b`<div class="track"></div>`} ${(this.mode === "determinate" || this.mode === "buffer") && this.value <= 0 ? x : b`<div class="primary">${r && (this.mode === "determinate" || this.mode === "buffer") ? t(this, N_, "m", V_).call(this, n, r.height, r.viewBox, r.path) : x}</div>${this.mode === "determinate" || this.mode === "buffer" ? b`<div class="gap"></div>` : x}`} ${this.mode !== "buffer" || this.bufferValue > 0 ? b`<div class="secondary"></div>` : x} ${this.mode === "buffer" && this.bufferValue > 0 ? b`<div class="gap"></div>` : x} ${this.mode === "buffer" ? b`<div class="buffer"></div>` : x} ${(this.mode === "determinate" || this.mode === "buffer") && this.value > 0 ? b`<div class="gap"></div><div class="stop"></div>` : x} ${r && this.mode !== "determinate" && this.mode !== "buffer" ? t(this, N_, "m", V_).call(this, n, r.height, r.viewBox, r.path) : x}</div>${t(this, N_, "m", H_).call(this)}`;
	}
};
P_ = /* @__PURE__ */ new WeakMap(), F_ = /* @__PURE__ */ new WeakMap(), I_ = /* @__PURE__ */ new WeakMap(), L_ = /* @__PURE__ */ new WeakMap(), R_ = /* @__PURE__ */ new WeakMap(), N_ = /* @__PURE__ */ new WeakSet(), z_ = function() {
	M_(this.mode) || (this.mode = "determinate"), wr(this, this.mode, "buffer", "determinate", "indeterminate", "query");
}, B_ = function() {
	t(this, N_, "m", U_).call(this), t(this, N_, "m", W_).call(this);
	let e = this.shadowRoot?.querySelector(".stroke");
	e && t(this, R_, "f").observe(e);
	let n = this.shadowRoot?.querySelector(".amplitude-and-wavelength");
	n && t(this, R_, "f").observe(n);
}, V_ = function(e, n, r, i) {
	let a = this.mode === "indeterminate" || this.mode === "query";
	return !a && this.value / this.max === 1 ? b`<div class="complete"></div>` : b`<svg class="wave" width="${e}" height="${n}" viewBox="${r}" preserveAspectRatio="none">${a ? ke`
        <mask id="${t(this, P_, "f")}" maskUnits="userSpaceOnUse">
          <rect width="${e}" height="${n}" fill="black" />
          <rect class="primary" height="${n}" fill="white" />
          <rect class="secondary" height="${n}" fill="white" />
        </mask>
        <mask id="${`${t(this, P_, "f")}-inverse`}" maskUnits="userSpaceOnUse">
          <rect width="${e}" height="${n}" fill="white" />
          <rect class="primary" height="${n}" fill="black" />
          <rect class="secondary" height="${n}" fill="black" />
        </mask>
        <g mask="${zt(t(this, P_, "f"))}">
          <path d="${i}" stroke="currentColor" stroke-width=${t(this, F_, "f")} fill="none" stroke-linecap="round" />
        </g>
        <g mask="${zt(`${t(this, P_, "f")}-inverse`)}">
          <rect class="track" width="100%" height="${t(this, F_, "f")}" fill="currentColor" />
        </g>` : ke`<path d="${i}" stroke="currentColor" stroke-width=${t(this, F_, "f")} fill="none" stroke-linecap="round" />`}</svg>`;
}, H_ = function() {
	return b`<div class="stroke" aria-hidden="true"></div><div class="amplitude-and-wavelength" aria-hidden="true"></div>`;
}, U_ = function() {
	let e = this.shadowRoot?.querySelector(".stroke");
	e && n(this, F_, e.clientHeight, "f");
}, W_ = function() {
	let e = this.shadowRoot?.querySelector(".amplitude-and-wavelength");
	e && (n(this, I_, e.clientHeight, "f"), n(this, L_, e.clientWidth, "f"));
}, G_ = function(e, n = 0) {
	let r = t(this, I_, "f") + t(this, F_, "f") / 2, i = r, a = [], o = t(this, L_, "f") / 2, s = 0;
	for (a.push(`M ${s},${i}`); s <= e;) {
		let e = s + o, c = i + r * Math.sin(2 * Math.PI * e / t(this, L_, "f") + n), l = s + o / 2, u = i + r * Math.sin(2 * Math.PI * (s + o / 2) / t(this, L_, "f") + n);
		a.push(`Q ${l},${u} ${e},${c}`), s += o;
	}
	let c = `0 -1 ${e} ${r * 2 + 2}`;
	return {
		path: a.join(" "),
		viewBox: c,
		height: t(this, F_, "f") + t(this, I_, "f") * 2,
		padding: 1
	};
}, q_.styles = [i_.styles, l`:host { display: block; position: relative; } .progress { width: 100%; height: 100%; position: relative; align-items: center; border-radius: var(--m3e-linear-progress-indicator-shape, ${j.shape.corner.extraSmall}); } .stroke, .amplitude-and-wavelength { visibility: hidden; position: absolute; } .stroke { width: 100%; height: var(--m3e-linear-progress-indicator-thickness, 4px); } .amplitude-and-wavelength { height: var(--m3e-linear-wavy-progress-indicator-amplitude, 3px); width: var(--m3e-linear-wavy-progress-indicator-wavelength, 40px); } :host(:is(:state(--indeterminate), :--indeterminate)) .amplitude-and-wavelength, :host(:is(:state(--query), :--query)) .amplitude-and-wavelength { width: var(--m3e-linear-wavy-indeterminate-progress-indicator-wavelength, 24px); } .primary, .secondary, .stop { height: var(--m3e-linear-progress-indicator-thickness, 4px); border-radius: inherit; } .stop { aspect-ratio: 1; flex: none; } :host(:is(:state(--flat), :--flat)) { height: var(--m3e-linear-progress-indicator-thickness, 4px); } :host(:is(:state(--wavy), :--wavy)) { height: calc( var(--m3e-linear-progress-indicator-thickness, 4px) + calc(var(--m3e-linear-wavy-progress-indicator-amplitude, 3px) * 2) ); } :host(:is(:state(--wavy), :--wavy)) .primary, :host(:is(:state(--wavy), :--wavy)) .secondary { position: relative; height: 100%; overflow: hidden; } :host(:is(:state(--wavy), :--wavy)) .complete { position: absolute; margin: auto; top: calc(50% - calc(var(--m3e-linear-progress-indicator-thickness, 4px) / 2)); left: 0; right: 0; height: var(--m3e-linear-progress-indicator-thickness, 4px); border-radius: inherit; } :host(:is(:state(--wavy), :--wavy)) .secondary { height: var(--m3e-linear-progress-indicator-thickness, 4px); } .wave { position: absolute; display: block; } .primary .wave, .secondary .wave { margin-inline-start: calc(0px - var(--m3e-linear-wavy-progress-indicator-wavelength, 40px)); } :host(:is(:state(--wavy), :--wavy):is(:state(--determinate), :--determinate)) .primary path, :host(:is(:state(--wavy), :--wavy):is(:state(--buffer), :--buffer)) .primary path { animation: wave-slide 1.5s linear infinite; } @keyframes wave-slide { from { transform: translateX(0); } to { transform: translateX(calc(0px - var(--m3e-linear-wavy-progress-indicator-wavelength, 40px))); } } :host(:is(:state(--determinate), :--determinate)) .progress, :host(:is(:state(--buffer), :--buffer)) .progress { display: flex; overflow: hidden; } :host(:is(:state(--determinate), :--determinate)) .primary, :host(:is(:state(--buffer), :--buffer)) .primary { width: var(--_value, 0px); flex: none; } :host(:is(:state(--determinate), :--determinate)) .gap, :host(:is(:state(--buffer), :--buffer)) .gap { flex-basis: var(--m3e-linear-progress-indicator-thickness, 4px); flex-shrink: 1; } :host(:is(:state(--determinate), :--determinate)) .secondary, :host(:is(:state(--buffer), :--buffer)) .buffer { flex: 1 1 auto; } :host(:is(:state(--buffer), :--buffer)) .buffer { flex-shrink: 5; height: 100%; width: 100%; background-color: var(--m3e-progress-indicator-track-color, ${j.color.secondaryContainer}); mask-image: radial-gradient( circle, black 0, black calc(var(--m3e-linear-progress-indicator-thickness, 4px) / 2), transparent calc(var(--m3e-linear-progress-indicator-thickness, 4px) / 2) ); mask-size: calc(var(--m3e-linear-progress-indicator-thickness, 4px) * 2) 100%; mask-repeat: repeat; animation: buffer 250ms linear infinite; } :host(:is(:state(--rtl), :--rtl):is(:state(--buffer), :--buffer)) .buffer { transform: scaleX(-1); } @keyframes buffer { from { mask-position: 0 0; } to { mask-position: calc(-1 * calc(var(--m3e-linear-progress-indicator-thickness, 4px) * 2)) 0; } } :host(:is(:state(--buffer), :--buffer)) .secondary { width: var(--_buffer-value, 0px); flex: none; } :host(:is(:state(--indeterminate), :--indeterminate)) .primary, :host(:is(:state(--query), :--query)) .primary { position: absolute; top: 0; height: 100%; border-radius: inherit; animation: indeterminate-primary 2.1s infinite linear; } :host(:is(:state(--wavy), :--wavy):is(:state(--indeterminate), :--indeterminate)) .primary, :host(:is(:state(--wavy), :--wavy):is(:state(--query), :--query)) .primary { animation-name: wavy-indeterminate-primary; } :host(:is(:state(--indeterminate), :--indeterminate)) .secondary, :host(:is(:state(--query), :--query)) .secondary { position: absolute; top: 0; height: 100%; border-radius: inherit; animation: indeterminate-secondary 2.1s infinite linear; animation-delay: 1.15s; animation-fill-mode: backwards; } :host(:is(:state(--wavy), :--wavy):is(:state(--indeterminate), :--indeterminate)) .secondary, :host(:is(:state(--wavy), :--wavy):is(:state(--query), :--query)) .secondary { animation-name: wavy-indeterminate-secondary; } :host(:is(:state(--indeterminate), :--indeterminate)) .progress, :host(:is(:state(--query), :--query)) .progress { overflow: hidden; position: relative; } :host(:not(:is(:state(--rtl), :--rtl)):is(:state(--query), :--query)) .progress, :host(:is(:state(--rtl), :--rtl):is(:state(--indeterminate), :--indeterminate)) .progress { transform: scaleX(-1); } :host(:is(:state(--flat), :--flat)) .primary, :host(:is(:state(--flat), :--flat):is(:state(--indeterminate), :--indeterminate)) .secondary, :host(:is(:state(--flat), :--flat):is(:state(--query), :--query)) .secondary, :host(:is(:state(--wavy), :--wavy)) .complete, .stop { background-color: var(--m3e-progress-indicator-color, ${j.color.primary}); } :host(:is(:state(--wavy), :--wavy)) .progress { color: var(--m3e-progress-indicator-color, ${j.color.primary}); } :host(:is(:state(--determinate), :--determinate)) .secondary, :host(:is(:state(--buffer), :--buffer)) .secondary, :host(:is(:state(--flat), :--flat):is(:state(--indeterminate), :--indeterminate)) .track, :host(:is(:state(--flat), :--flat):is(:state(--query), :--query)) .track { background-color: var(--m3e-progress-indicator-track-color, ${j.color.secondaryContainer}); } :host(:is(:state(--wavy), :--wavy):is(:state(--indeterminate), :--indeterminate)) .track, :host(:is(:state(--wavy), :--wavy):is(:state(--query), :--query)) .track { color: var(--m3e-progress-indicator-track-color, ${j.color.secondaryContainer}); } :host(:is(:state(--wavy), :--wavy):is(:state(--indeterminate), :--indeterminate)) .track, :host(:is(:state(--wavy), :--wavy):is(:state(--query), :--query)) .track { y: calc(50% - calc(var(--m3e-linear-progress-indicator-thickness, 4px) / 2)); border-radius: inherit; } :host(:is(:state(--flat), :--flat):is(:state(--indeterminate), :--indeterminate)) .track, :host(:is(:state(--flat), :--flat):is(:state(--query), :--query)) .track { position: absolute; margin: auto; top: calc(50% - calc(var(--m3e-linear-progress-indicator-thickness, 4px) / 2)); left: 0; right: 0; height: var(--m3e-linear-progress-indicator-thickness, 4px); border-radius: inherit; } @keyframes indeterminate-primary { 0% { left: -145.167%; width: 8%; } 20% { left: -113.333%; width: 48%; } 60% { left: 56.333%; width: 78%; } 100% { left: 100%; width: 8%; } } @keyframes indeterminate-secondary { 0% { left: -54.888%; width: 8%; } 20% { left: -20%; width: 48%; } 60% { left: 60%; width: 78%; } 100% { left: 160%; width: 8%; } } @keyframes wavy-indeterminate-primary { 0% { transform: translateX(-145.167%); width: 8%; } 20% { transform: translateX(-113.333%); width: 48%; } 60% { transform: translateX(56.333%); width: 78%; } 100% { transform: translateX(100%); width: 8%; } } @keyframes wavy-indeterminate-secondary { 0% { transform: translateX(-54.888%); width: 8%; } 20% { transform: translateX(-20%); width: 48%; } 60% { transform: translateX(60%); width: 78%; } 100% { transform: translateX(160%); width: 8%; } } @media (forced-colors: active) { .progress { --m3e-progress-indicator-track-color: GrayText; --m3e-progress-indicator-color: CanvasText; } }`], q_.__nextMaskId = 0, e([C({
	reflect: !0,
	useDefault: !0
})], q_.prototype, "mode", void 0), e([C({
	attribute: "buffer-value",
	type: Number
})], q_.prototype, "bufferValue", void 0), q_ = K_ = e([T("m3e-linear-progress-indicator")], q_);
//#endregion
//#region node_modules/@m3e/web/dist/shape.js
var J_ = {
	size: c("var(--m3e-shape-size, 48px)"),
	containerColor: c(`var(--m3e-shape-container-color, ${j.color.primary})`),
	transition: c(`var(--m3e-shape-transition, clip-path ${j.motion.spring.slowEffects})`)
}, Y_ = l`:host { display: inline-block; position: relative; aspect-ratio: 1 / 1; width: ${J_.size}; } :host([hidden]) { display: none; } :host([name]) .wrapper { background-color: ${J_.containerColor}; transition: ${J_.transition}; transform-origin: center; } .wrapper { width: 100%; height: 100%; position: relative; isolation: isolate; overflow: hidden; will-change: clip-path; } ::slotted(img), ::slotted(video) { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; } @media (prefers-reduced-motion) { .wrapper { transition: none; } }`, X_ = {
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
}, Z_ = [], Q_ = /* @__PURE__ */ new Map();
for (let e in X_) Z_.push(X_[e]), Q_.set(e, Z_.length - 1);
var $_ = {};
Z_ = Nt(Z_, 300);
for (let e of Q_) $_[e[0]] = c(Z_[e[1]]);
var ev = $_;
function tv(e) {
	return l`:host([name="${c(e)}"]) .wrapper { clip-path: polygon(${ev[e]}); }`;
}
var nv = [...Object.getOwnPropertyNames(ev).map((e) => tv(e))], rv = class extends S {
	constructor() {
		super(...arguments), this.name = null;
	}
	render() {
		return b`<div class="wrapper"><slot></slot></div>`;
	}
};
rv.styles = [Y_, nv], e([C({ reflect: !0 })], rv.prototype, "name", void 0), rv = e([T("m3e-shape")], rv);
//#endregion
//#region node_modules/@m3e/web/dist/switch.js
var Z = {
	selectedIconColor: c(`var(--m3e-switch-selected-icon-color, ${j.color.primary})`),
	selectedIconSize: c("var(--m3e-switch-selected-icon-size, 16px)"),
	unselectedIconColor: c(`var(--m3e-switch-unselected-icon-color, ${j.color.surfaceContainerHighest})`),
	unselectedIconSize: c("var(--m3e-switch-unselected-icon-size, 16px)"),
	trackHeight: c(`calc(var(--m3e-switch-track-height, 32px) + ${j.density.calc(-3)})`),
	trackWidth: c(`calc(var(--m3e-switch-track-width, 52px) + ${j.density.calc(-3)})`),
	trackOutlineColor: c(`var(--m3e-switch-track-outline-color, ${j.color.outline})`),
	trackOutlineWidth: c("var(--m3e-switch-track-outline-width, 2px)"),
	trackShape: c(`var(--m3e-switch-track-shape, ${j.shape.corner.full})`),
	selectedTrackColor: c(`var(--m3e-switch-selected-track-color, ${j.color.primary})`),
	unselectedTrackColor: c(`var(--m3e-switch-unselected-track-color, ${j.color.surfaceContainerHighest})`),
	unselectedHandleHeight: c(`calc(var(--m3e-switch-unselected-handle-height, 16px) + ${j.density.calc(-3)})`),
	unselectedHandleWidth: c(`calc(var(--m3e-switch-unselected-handle-width, 16px) + ${j.density.calc(-3)})`),
	withIconHandleHeight: c(`calc(var(--m3e-switch-with-icon-handle-height, 24px) + ${j.density.calc(-3)})`),
	withIconHandleWidth: c(`calc(var(--m3e-switch-with-icon-handle-width, 24px) + ${j.density.calc(-3)})`),
	selectedHandleHeight: c(`calc(var(--m3e-switch-selected-handle-height, 24px) + ${j.density.calc(-3)})`),
	selectedHandleWidth: c(`calc(var(--m3e-switch-selected-handle-width, 24px) + ${j.density.calc(-3)})`),
	pressedHandleHeight: c(`calc(var(--m3e-switch-pressed-handle-height, 28px) + ${j.density.calc(-3)})`),
	pressedHandleWidth: c(`calc(var(--m3e-switch-pressed-handle-width, 28px) + ${j.density.calc(-3)})`),
	handleShape: c(`var(--m3e-switch-handle-shape, ${j.shape.corner.full})`),
	selectedHandleColor: c(`var(--m3e-switch-selected-handle-color, ${j.color.onPrimary})`),
	unselectedHandleColor: c(`var(--m3e-switch-unselected-handle-color, ${j.color.outline})`),
	stateLayerSize: c(`calc(var(--m3e-switch-state-layer-size, 40px) + ${j.density.calc(-3)})`),
	stateLayerShape: c(`var(--m3e-switch-state-layer-shape, ${j.shape.corner.full})`),
	disabledSelectedIconColor: c(`var(--m3e-switch-disabled-selected-icon-color, ${j.color.onSurface})`),
	disabledSelectedIconOpacity: c("var(--m3e-switch-disabled-selected-icon-opacity, 38%)"),
	disabledUnselectedIconColor: c(`var(--m3e-switch-disabled-unselected-icon-color, ${j.color.surfaceContainerHighest})`),
	disabledUnselectedIconOpacity: c("var(--m3e-switch-disabled-unselected-icon-opacity, 38%)"),
	disabledTrackOpacity: c("var(--m3e-switch-disabled-track-opacity, 12%)"),
	disabledSelectedTrackColor: c(`var(--m3e-switch-disabled-selected-track-color, ${j.color.onSurface})`),
	disabledUnselectedTrackColor: c(`var(--m3e-switch-disabled-unselected-track-color, ${j.color.surfaceContainerHighest})`),
	disabledUnselectedTrackOutlineColor: c(`var(--m3e-switch-disabled-unselected-track-outline-color, ${j.color.onSurface})`),
	disabledUnselectedHandleOpacity: c("var(--m3e-switch-disabled-unselected-handle-opacity, 38%)"),
	disabledSelectedHandleOpacity: c("var(--m3e-switch-disabled-selected-handle-opacity, 100%)"),
	disabledSelectedHandleColor: c(`var(--m3e-switch-disabled-selected-handle-color, ${j.color.surface})`),
	disabledUnselectedHandleColor: c(`var(--m3e-switch-disabled-unselected-handle-color, ${j.color.onSurface})`),
	selectedHoverIconColor: c(`var(--m3e-switch-selected-hover-icon-color, ${j.color.onPrimaryContainer})`),
	unselectedHoverIconColor: c(`var(--m3e-switch-unselected-hover-icon-color, ${j.color.surfaceContainerHighest})`),
	selectedHoverTrackColor: c(`var(--m3e-switch-selected-hover-track-color, ${j.color.primary})`),
	selectedHoverStateLayerColor: c(`var(--m3e-switch-selected-hover-state-layer-color, ${j.color.primary})`),
	selectedHoverStateLayerOpacity: c("var(--m3e-switch-selected-hover-state-layer-opacity, 8%)"),
	unselectedHoverTrackColor: c(`var(--m3e-switch-unselected-hover-track-color, ${j.color.surfaceContainerHighest})`),
	unselectedHoverTrackOutlineColor: c(`var(--m3e-switch-unselected-hover-track-outline-color, ${j.color.outline})`),
	unselectedHoverStateLayerColor: c(`var(--m3e-switch-unselected-hover-state-layer-color, ${j.color.onSurface})`),
	unselectedHoverStateLayerOpacity: c("var(--m3e-switch-unselected-hover-state-layer-opacity, 8%)"),
	selectedHoverHandleColor: c(`var(--m3e-switch-selected-hover-handle-color, ${j.color.primaryContainer})`),
	unselectedHoverHandleColor: c(`var(--m3e-switch-unselected-hover-handle-color, ${j.color.onSurfaceVariant})`),
	selectedFocusIconColor: c(`var(--m3e-switch-selected-focus-icon-color, ${j.color.onPrimaryContainer})`),
	unselectedFocusIconColor: c(`var(--m3e-switch-unselected-focus-icon-color, ${j.color.surfaceContainerHighest})`),
	selectedFocusTrackColor: c(`var(--m3e-switch-selected-focus-track-color, ${j.color.primary})`),
	selectedFocusStateLayerColor: c(`var(--m3e-switch-selected-focus-state-layer-color, ${j.color.primary})`),
	selectedFocusStateLayerOpacity: c("var(--m3e-switch-selected-focus-state-layer-opacity, 10%)"),
	unselectedFocusTrackColor: c(`var(--m3e-switch-unselected-focus-track-color, ${j.color.surfaceContainerHighest})`),
	unselectedFocusTrackOutlineColor: c(`var(--m3e-switch-unselected-focus-track-outline-color, ${j.color.outline})`),
	unselectedFocusStateLayerColor: c(`var(--m3e-switch-unselected-focus-state-layer-color, ${j.color.onSurface})`),
	unselectedFocusStateLayerOpacity: c("var(--m3e-switch-unselected-focus-state-layer-opacity, 10%)"),
	selectedFocusHandleColor: c(`var(--m3e-switch-selected-focus-handle-color, ${j.color.primaryContainer})`),
	unselectedFocusHandleColor: c(`var(--m3e-switch-unselected-focus-handle-color, ${j.color.onSurfaceVariant})`),
	selectedPressedIconColor: c(`var(--m3e-switch-selected-pressed-icon-color, ${j.color.onPrimaryContainer})`),
	unselectedPressedIconColor: c(`var(--m3e-switch-unselected-pressed-icon-color, ${j.color.surfaceContainerHighest})`),
	selectedPressedTrackColor: c(`var(--m3e-switch-selected-pressed-track-color, ${j.color.primary})`),
	selectedPressedStateLayerColor: c(`var(--m3e-switch-selected-pressed-state-layer-color, ${j.color.primary})`),
	selectedPressedStateLayerOpacity: c("var(--m3e-switch-selected-pressed-state-layer-opacity, 10%)"),
	unselectedPressedTrackColor: c(`var(--m3e-switch-unselected-pressed-track-color, ${j.color.surfaceContainerHighest})`),
	unselectedPressedTrackOutlineColor: c(`var(--m3e-switch-unselected-pressed-track-outline-color, ${j.color.outline})`),
	unselectedPressedStateLayerColor: c(`var(--m3e-switch-unselected-pressed-state-layer-color, ${j.color.onSurface})`),
	unselectedPressedStateLayerOpacity: c("var(--m3e-switch-unselected-pressed-state-layer-opacity, 10%)"),
	selectedPressedHandleColor: c(`var(--m3e-switch-selected-pressed-handle-color, ${j.color.primaryContainer})`),
	unselectedPressedHandleColor: c(`var(--m3e-switch-unselected-pressed-handle-color, ${j.color.onSurfaceVariant})`)
}, iv = l`.handle { position: relative; display: flex; align-items: center; justify-content: center; pointer-events: none; transform-origin: center center; border-radius: ${Z.handleShape}; transition: ${c(`background-color ${j.motion.duration.short4} ${j.motion.easing.standard},
      transform var(--_switch-handle-effect),
      width ${j.motion.spring.fastEffects},
      height ${j.motion.spring.fastEffects}`)}; } .track:not(.pressed) .handle { --_switch-handle-effect: ${j.motion.spring.fastSpatial}; } .track.pressed .handle { --_switch-handle-effect: ${j.motion.spring.fastEffects}; } :host(:not([aria-disabled="true"]):not([checked]):is(:state(--icons-both), :--icons-both)) .track:not(.pressed) .handle { width: ${Z.withIconHandleWidth}; height: ${Z.withIconHandleHeight}; } :host(:not([checked]):not(:is(:state(--icons-both), :--icons-both))) .track:not(.pressed) .handle, :host([aria-disabled="true"]:not([checked])) .handle { width: ${Z.unselectedHandleWidth}; height: ${Z.unselectedHandleHeight}; } :host([checked]) .track:not(.pressed) .handle { width: ${Z.selectedHandleWidth}; height: ${Z.selectedHandleHeight}; } .track.pressed .handle { width: ${Z.pressedHandleWidth}; height: ${Z.pressedHandleHeight}; } :host(:not([aria-disabled="true"]):not([checked]):not(:focus):not(:hover)) .track:not(.pressed) .handle { background-color: ${Z.unselectedHandleColor}; } :host(:not([aria-disabled="true"]):not([checked]):not(:focus):hover) .track:not(.pressed) .handle { background-color: ${Z.unselectedHoverHandleColor}; } :host(:not([aria-disabled="true"]):not([checked]):focus) .track:not(.pressed) .handle { background-color: ${Z.unselectedFocusHandleColor}; } :host(:not([aria-disabled="true"]):not([checked])) .track.pressed .handle { background-color: ${Z.unselectedPressedHandleColor}; } :host(:not([aria-disabled="true"])[checked]:not(:focus):not(:hover)) .track:not(.pressed) .handle { background-color: ${Z.selectedHandleColor}; } :host(:not([aria-disabled="true"])[checked]:not(:focus):hover) .track:not(.pressed) .handle { background-color: ${Z.selectedHoverHandleColor}; } :host(:not([aria-disabled="true"])[checked]:focus) .track:not(.pressed) .handle { background-color: ${Z.selectedFocusHandleColor}; } :host(:not([aria-disabled="true"])[checked]) .track.pressed .handle { background-color: ${Z.selectedPressedHandleColor}; } :host([aria-disabled="true"]:not([checked])) .handle { background-color: color-mix( in srgb, ${Z.disabledUnselectedHandleColor} ${Z.disabledUnselectedHandleOpacity}, transparent ); } :host([aria-disabled="true"][checked]) .handle { background-color: color-mix( in srgb, ${Z.disabledSelectedHandleColor} ${Z.disabledSelectedHandleOpacity}, transparent ); } :host(:not(:is(:state(--rtl), :--rtl))[checked]) .track:not(.pressed) .handle { transform: translateX( calc(${Z.trackWidth} - ${Z.selectedHandleWidth} - calc(${Z.trackOutlineWidth} * 2)) ); } :host(:is(:state(--rtl), :--rtl)[checked]) .track:not(.pressed) .handle { transform: translateX( calc( 0px - calc( ${Z.trackWidth} - ${Z.selectedHandleWidth} - calc(${Z.trackOutlineWidth} * 2) ) ) ); } :host(:not(:is(:state(--rtl), :--rtl))[checked]) .track.pressed .handle { transform: translateX( calc(${Z.trackWidth} - ${Z.pressedHandleWidth} - ${Z.trackOutlineWidth}) ); } :host(:is(:state(--rtl), :--rtl)[checked]) .track.pressed .handle { transform: translateX( calc(0px - calc(${Z.trackWidth} - ${Z.pressedHandleWidth} - ${Z.trackOutlineWidth})) ); } :host(:not(:is(:state(--rtl), :--rtl)):not([checked]):not(:is(:state(--icons-both), :--icons-both))) .track:not(.pressed) .handle, :host(:not(:is(:state(--rtl), :--rtl))[aria-disabled="true"]:not([checked])) .handle { transform: translateX( calc( ${Z.trackOutlineWidth} + calc(${Z.pressedHandleWidth} - ${Z.withIconHandleWidth}) ) ); } :host(:is(:state(--rtl), :--rtl):not([checked]):not(:is(:state(--icons-both), :--icons-both))) .track:not(.pressed) .handle, :host(:is(:state(--rtl), :--rtl)[aria-disabled="true"]:not([checked])) .handle { transform: translateX( calc( 0px - calc( ${Z.trackOutlineWidth} + calc(${Z.pressedHandleWidth} - ${Z.withIconHandleWidth}) ) ) ); } :host( :not(:is(:state(--rtl), :--rtl)):not([aria-disabled="true"]):not([checked]):is( :state(--icons-both), :--icons-both ) ) .track:not(.pressed) .handle { transform: translateX(${Z.trackOutlineWidth}); } :host(:is(:state(--rtl), :--rtl):not([aria-disabled="true"]):not([checked]):is(:state(--icons-both), :--icons-both)) .track:not(.pressed) .handle { transform: translateX(calc(0px - ${Z.trackOutlineWidth})); } @media (forced-colors: active) { .handle { transition: ${c(`transform var(--_switch-handle-effect),
      width ${j.motion.spring.fastEffects},
      height ${j.motion.spring.fastEffects}`)}; } :host(:not([aria-disabled="true"]):not([checked]):not(:focus):not(:hover)) .track:not(.pressed) .handle, :host(:not([aria-disabled="true"]):not([checked]):not(:focus):hover) .track:not(.pressed) .handle, :host(:not([aria-disabled="true"]):not([checked]):focus) .track:not(.pressed) .handle, :host(:not([aria-disabled="true"]):not([checked])) .track.pressed .handle { background-color: ButtonText; } :host([aria-disabled="true"]:not([checked])) .handle { background-color: GrayText; } :host(:not([aria-disabled="true"])[checked]:not(:focus):not(:hover)) .track:not(.pressed) .handle, :host(:not([aria-disabled="true"])[checked]:not(:focus):hover) .track:not(.pressed) .handle, :host(:not([aria-disabled="true"])[checked]:focus) .track:not(.pressed) .handle, :host(:not([aria-disabled="true"])[checked]) .track.pressed .handle { background-color: Canvas; } :host([aria-disabled="true"][checked]) .handle { background-color: Canvas; } } @media (prefers-reduced-motion) { .handle { transition: none; } }`, av = l`:host(:is(:state(--icons-none), :--icons-none)) .icon, :host(:is(:state(--icons-selected), :--icons-selected):not([checked])) .icon, :host([aria-disabled="true"]:not([checked])) .icon { display: none; } .icon { width: 1em; transition: ${c(`color ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; } :host(:not([checked])) .icon { font-size: ${Z.unselectedIconSize}; } :host([checked]) .icon { font-size: ${Z.selectedIconSize}; } :host(:not([aria-disabled="true"]):not([checked]):not(:focus):not(:hover)) .track:not(.pressed) .icon { color: ${Z.unselectedIconColor}; } :host(:not([aria-disabled="true"]):not([checked]):not(:focus):hover) .track:not(.pressed) .icon { color: ${Z.unselectedHoverIconColor}; } :host(:not([aria-disabled="true"]):not([checked]):focus) .track:not(.pressed) .icon { color: ${Z.unselectedFocusIconColor}; } :host(:not([aria-disabled="true"]):not([checked])) .track.pressed .icon { color: ${Z.unselectedPressedIconColor}; } :host(:not([aria-disabled="true"])[checked]:not(:focus):not(:hover)) .track:not(.pressed) .icon { color: ${Z.selectedIconColor}; } :host(:not([aria-disabled="true"])[checked]:not(:focus):hover) .track:not(.pressed) .icon { color: ${Z.selectedHoverIconColor}; } :host(:not([aria-disabled="true"])[checked]:focus) .track:not(.pressed) .icon { color: ${Z.selectedFocusIconColor}; } :host(:not([aria-disabled="true"])[checked]) .track.pressed .icon { color: ${Z.selectedPressedIconColor}; } :host([aria-disabled="true"]:not([checked])) .icon { color: color-mix( in srgb, ${Z.disabledUnselectedIconColor} ${Z.disabledUnselectedIconOpacity}, transparent ); } :host([aria-disabled="true"][checked]) .icon { color: color-mix( in srgb, ${Z.disabledSelectedIconColor} ${Z.disabledSelectedIconOpacity}, transparent ); } @media (forced-colors: active) { :host(:not([aria-disabled="true"]):not([checked]):not(:focus):not(:hover)) .track:not(.pressed) .icon, :host(:not([aria-disabled="true"]):not([checked]):not(:focus):hover) .track:not(.pressed) .icon, :host(:not([aria-disabled="true"]):not([checked]):focus) .track:not(.pressed) .icon, :host(:not([aria-disabled="true"]):not([checked])) .track.pressed .icon { color: Canvas; } :host(:not([aria-disabled="true"])[checked]:not(:focus):not(:hover)) .track:not(.pressed) .icon, :host(:not([aria-disabled="true"])[checked]:not(:focus):hover) .track:not(.pressed) .icon, :host(:not([aria-disabled="true"])[checked]:focus) .track:not(.pressed) .icon, :host(:not([aria-disabled="true"])[checked]) .track.pressed .icon { color: CanvasText; } :host([aria-disabled="true"]:not([checked])) .icon { color: Canvas; } :host([aria-disabled="true"][checked]) .icon { color: GrayText; } } @media (prefers-reduced-motion) { .icon { transition: none; } }`, ov = l`.state-layer { width: ${Z.stateLayerSize}; height: ${Z.stateLayerSize}; border-radius: ${Z.stateLayerShape}; transition: ${c(`top ${j.motion.spring.fastEffects},
          left ${j.motion.spring.fastEffects},
          right ${j.motion.spring.fastEffects}`)}; } :host(:not([checked]):is(:state(--icons-both), :--icons-both)) .track:not(.pressed) .state-layer { inset-inline-start: calc(0px - calc(calc(${Z.stateLayerSize} - ${Z.withIconHandleWidth}) / 2)); top: calc(0px - calc(calc(${Z.stateLayerSize} - ${Z.withIconHandleHeight}) / 2)); } :host(:not([checked]):not(:is(:state(--icons-both), :--icons-both))) .track:not(.pressed) .state-layer { inset-inline-start: calc( 0px - calc(calc(${Z.stateLayerSize} - ${Z.unselectedHandleWidth}) / 2) ); top: calc(0px - calc(calc(${Z.stateLayerSize} - ${Z.unselectedHandleHeight}) / 2)); } :host([checked]) .track:not(.pressed) .state-layer { inset-inline-start: calc(0px - calc(calc(${Z.stateLayerSize} - ${Z.selectedHandleWidth}) / 2)); top: calc(0px - calc(calc(${Z.stateLayerSize} - ${Z.selectedHandleHeight}) / 2)); } .track.pressed .state-layer { inset-inline-start: calc(0px - calc(calc(${Z.stateLayerSize} - ${Z.pressedHandleWidth}) / 2)); top: calc(0px - calc(calc(${Z.stateLayerSize} - ${Z.pressedHandleWidth}) / 2)); } :host(:not([checked])) .state-layer { --m3e-state-layer-hover-color: ${Z.unselectedHoverStateLayerColor}; --m3e-state-layer-hover-opacity: ${Z.unselectedHoverStateLayerOpacity}; --m3e-state-layer-focus-color: ${Z.unselectedFocusStateLayerColor}; --m3e-state-layer-focus-opacity: ${Z.unselectedFocusStateLayerOpacity}; } :host([checked]) .state-layer { --m3e-state-layer-hover-color: ${Z.selectedHoverStateLayerColor}; --m3e-state-layer-hover-opacity: ${Z.selectedHoverStateLayerOpacity}; --m3e-state-layer-focus-color: ${Z.selectedFocusStateLayerColor}; --m3e-state-layer-focus-opacity: ${Z.selectedFocusStateLayerOpacity}; }`, sv = l`:host { display: inline-block; position: relative; outline: none; height: fit-content; width: fit-content; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } :host([hidden]) { display: none; } :host(:not([aria-disabled="true"])) { cursor: pointer; } .focus-ring { border-radius: ${Z.trackShape}; } .touch { position: absolute; height: 48px; left: 0; right: 0; } .base { contain: layout style paint; display: flex; align-items: center; justify-content: center; }`, cv = l`.track { contain: layout style; display: flex; align-items: center; position: relative; box-sizing: border-box; border-radius: ${Z.trackShape}; width: ${Z.trackWidth}; height: ${Z.trackHeight}; transition: ${c(`background-color ${j.motion.duration.short4} ${j.motion.easing.standard}`)}; } :host(:not([checked])) .track { border-width: ${Z.trackOutlineWidth}; border-style: solid; } :host(:not([aria-disabled="true"]):not([checked]):not(:focus):not(:hover)) .track:not(.pressed) { border-color: ${Z.trackOutlineColor}; background-color: ${Z.unselectedTrackColor}; } :host(:not([aria-disabled="true"]):not([checked]):not(:focus):hover) .track:not(.pressed) { border-color: ${Z.unselectedHoverTrackOutlineColor}; background-color: ${Z.unselectedHoverTrackColor}; } :host(:not([aria-disabled="true"]):not([checked]):focus) .track:not(.pressed) { border-color: ${Z.unselectedFocusTrackOutlineColor}; background-color: ${Z.unselectedFocusTrackColor}; } :host(:not([aria-disabled="true"]):not([checked])) .track.pressed { border-color: ${Z.unselectedPressedTrackOutlineColor}; background-color: ${Z.unselectedPressedTrackColor}; } :host([aria-disabled="true"]:not([checked])) .track { border-color: color-mix( in srgb, ${Z.disabledUnselectedTrackOutlineColor} ${Z.disabledTrackOpacity}, transparent ); background-color: color-mix( in srgb, ${Z.disabledUnselectedTrackColor} ${Z.disabledTrackOpacity}, transparent ); } :host(:not([aria-disabled="true"])[checked]:not(:focus):not(:hover)) .track:not(.pressed) { background-color: ${Z.selectedTrackColor}; } :host(:not([aria-disabled="true"])[checked]:not(:focus):hover) .track:not(.pressed) { background-color: ${Z.selectedHoverTrackColor}; } :host(:not([aria-disabled="true"])[checked]:focus) .track:not(.pressed) { background-color: ${Z.selectedFocusTrackColor}; } :host(:not([aria-disabled="true"])[checked]) .track.pressed { background-color: ${Z.selectedPressedTrackColor}; } :host([aria-disabled="true"][checked]) .track { background-color: color-mix( in srgb, ${Z.disabledSelectedTrackColor} ${Z.disabledTrackOpacity}, transparent ); } @media (forced-colors: active) { .track { transition: none; } :host(:not([aria-disabled="true"]):not([checked]):not(:focus):not(:hover)) .track:not(.pressed), :host(:not([aria-disabled="true"]):not([checked]):not(:focus):hover) .track:not(.pressed), :host(:not([aria-disabled="true"]):not([checked]):focus) .track:not(.pressed), :host(:not([aria-disabled="true"]):not([checked])) .track.pressed { border-color: ButtonText; background-color: Canvas; } :host([aria-disabled="true"]:not([checked])) .track { border-color: GrayText; background-color: Canvas; } :host(:not([aria-disabled="true"])[checked]:not(:focus):not(:hover)) .track:not(.pressed), :host(:not([aria-disabled="true"])[checked]:not(:focus):hover) .track:not(.pressed), :host(:not([aria-disabled="true"])[checked]:focus) .track:not(.pressed), :host(:not([aria-disabled="true"])[checked]) .track.pressed { background-color: ButtonText; } :host([aria-disabled="true"][checked]) .track { background-color: GrayText; } } @media (prefers-reduced-motion) { .track { transition: none; } }`;
function lv(e) {
	return e === "none" || e === "selected" || e === "both";
}
var uv, dv, fv, pv, mv, hv, gv, _v = class extends id(di(Fr(Rr(Ci(Er(_i(ji(Ti(ni(E(M(S, "switch")))))))))))) {
	constructor() {
		super(), uv.add(this), dv.set(this, (e) => t(this, uv, "m", gv).call(this, e)), fv.set(this, new hn(this, {
			target: null,
			callback: (e) => {
				this.disabled || (e ? this._stateLayer?.show("hover") : this._stateLayer?.hide("hover"));
			}
		})), pv.set(this, new Yn(this, {
			target: null,
			callback: (e) => this._track?.classList.toggle("pressed", e && !this.disabled)
		})), this.icons = "none", this.value = "on", new Yn(this, {
			isPressedKey: (e) => e === " ",
			callback: (e) => this._track?.classList.toggle("pressed", e && !this.disabled)
		});
	}
	get [(dv = /* @__PURE__ */ new WeakMap(), fv = /* @__PURE__ */ new WeakMap(), pv = /* @__PURE__ */ new WeakMap(), uv = /* @__PURE__ */ new WeakSet(), fi)]() {
		return this.checked && !this.disabled ? this.value : null;
	}
	connectedCallback() {
		super.connectedCallback(), t(this, uv, "m", mv).call(this), this.addEventListener("click", t(this, dv, "f"));
		for (let e of this.labels) t(this, fv, "f").observe(e), t(this, pv, "f").observe(e);
	}
	disconnectedCallback() {
		super.disconnectedCallback(), this.removeEventListener("click", t(this, dv, "f"));
		for (let e of this.labels) t(this, fv, "f").unobserve(e), t(this, pv, "f").unobserve(e);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("icons") && t(this, uv, "m", mv).call(this);
	}
	firstUpdated(e) {
		super.firstUpdated(e), [this._focusRing, this._stateLayer].forEach((e) => e?.attach(this));
	}
	render() {
		return b`<m3e-focus-ring class="focus-ring"></m3e-focus-ring><div class="track" aria-hidden="true"><div class="touch" aria-hidden="true"></div><div class="handle"><m3e-state-layer class="state-layer" ?disabled="${this.disabled}"></m3e-state-layer><div class="base">${t(this, uv, "m", hv).call(this)}</div></div></div>`;
	}
};
mv = function() {
	lv(this.icons) || (this.icons = "none"), O(this, "--icons-none", this.icons === "none"), O(this, "--icons-selected", this.icons === "selected"), O(this, "--icons-both", this.icons === "both");
}, hv = function() {
	return this.checked ? b`<svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M19.69,5.23L8.96,15.96l-4.23-4.23L2.96,13.5l6,6L21.46,7L19.69,5.23z"></path></svg>` : b`<svg class="icon" viewBox="0 -960 960 960" fill="currentColor"><path d="m256-200-56-56 224-224-224-224 56-56 224 224 224-224 56 56-224 224 224 224-56 56-224-224-224 224Z"/></svg>`;
}, gv = function(e) {
	e.defaultPrevented || (this.disabled ? (e.preventDefault(), e.stopImmediatePropagation()) : (this.dispatchEvent(new Event("beforeinput", {
		bubbles: !0,
		cancelable: !0
	})) && (this.checked = !this.checked, this.dispatchEvent(new Event("input", { bubbles: !0 })), this.dispatchEvent(new Event("change", { bubbles: !0 }))), this.closest("label") && e.preventDefault()));
}, _v.styles = [
	sv,
	ov,
	cv,
	iv,
	av
], e([w(".track")], _v.prototype, "_track", void 0), e([w(".focus-ring")], _v.prototype, "_focusRing", void 0), e([w(".state-layer")], _v.prototype, "_stateLayer", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], _v.prototype, "icons", void 0), e([C()], _v.prototype, "value", void 0), _v = e([T("m3e-switch")], _v);
//#endregion
//#region node_modules/@m3e/web/dist/slider.js
var vv, yv, bv = class extends Fr(Rr(_i(Ti(ni(E(M(S, "slider"))))))) {
	constructor() {
		super(...arguments), vv.add(this), this.value = null;
	}
	get [(vv = /* @__PURE__ */ new WeakSet(), fi)]() {
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
		return b`<div class="label" aria-hidden="true">${t(this, vv, "a", yv)}</div><div class="base"><m3e-focus-ring class="focus-ring" ?disabled="${this.disabled}"></m3e-focus-ring><div class="touch" aria-hidden="true"></div><div class="wrapper"><div class="handle"></div></div></div>`;
	}
};
yv = function() {
	return this.closest("m3e-slider")?.displayWith?.(this.value) ?? this.value?.toString() ?? "";
}, bv.styles = l`:host { display: block; box-sizing: border-box; position: absolute; outline: none; top: 0; bottom: 0; border-radius: var(--m3e-slider-thumb-shape, ${j.shape.corner.full}); user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); } :host([hidden]) { display: none; } .base { contain: layout style; box-sizing: border-box; vertical-align: middle; display: inline-flex; align-items: center; justify-content: center; position: relative; width: 100%; height: 100%; border-radius: inherit; } .touch { position: absolute; height: 48px; left: 0; right: 0; touch-action: none; } .wrapper { display: inline-flex; justify-content: center; height: 100%; border-radius: inherit; width: calc(var(--m3e-slider-thumb-width, 4px) + calc(var(--m3e-slider-thumb-padding, 6px) * 2)); } .focus-ring { top: calc(0px - var(--m3e-focus-ring-thickness, 3px)); bottom: calc(0px - var(--m3e-focus-ring-thickness, 3px)); left: var(--m3e-focus-ring-thickness, 3px); right: var(--m3e-focus-ring-thickness, 3px); } .label { user-select: none; position: absolute; display: flex; align-items: center; justify-content: center; visibility: var(--_slider-label-visibility, hidden); opacity: var(--_slider-label-opacity, 0); transform: var(--_slider-label-transform, scale(0)); transition: ${c(`opacity ${j.motion.duration.short3} ${j.motion.easing.standard}, 
          transform ${j.motion.duration.short3} ${j.motion.easing.standard},
          visibility ${j.motion.duration.short3} ${j.motion.easing.standard} allow-discrete`)}; width: var(--m3e-slider-label-width, 48px); height: var(--_m3e-slider-label-height, 44px); top: calc(0px - var(--_m3e-slider-label-height, 44px) - var(--_m3e-slider-label-margin, 4px)); inset-inline-start: calc(0px - 100%); border-radius: var(--m3e-slider-label-shape, ${j.shape.corner.full}); background-color: var(--m3e-slider-label-container-color, ${j.color.inverseSurface}); color: var(--m3e-slider-label-color, ${j.color.inverseOnSurface}); font-size: var(--m3e-slider-label-font-size, ${j.typescale.standard.label.medium.fontSize}); font-weight: var(--m3e-slider-label-font-weight, ${j.typescale.standard.label.medium.fontWeight}); line-height: var(--m3e-slider-label-line-height, ${j.typescale.standard.label.medium.lineHeight}); letter-spacing: var(--m3e-slider-label-tracking, ${j.typescale.standard.label.medium.tracking}); } @starting-style { .label { opacity: 0; transform: scale(0); } } .handle { height: 100%; width: var(--m3e-slider-thumb-width, 4px); border-radius: inherit; transition: ${c(`width ${j.motion.duration.short2} ${j.motion.easing.standard}`)}; } :host(:is(:active, :focus-visible, :is(:state(--active), :--active)):not([aria-disabled="true"])) .handle { width: var(--m3e-slider-thumb-pressed-width, 2px); } :host(:not([aria-disabled="true"])) .handle { background-color: var(--m3e-slider-thumb-color, ${j.color.primary}); } :host([aria-disabled="true"]) .handle { opacity: var(--m3e-slider-thumb-disabled-opacity, 38%); background-color: var(--m3e-slider-thumb-disabled-color, ${j.color.onSurface}); } @media (prefers-reduced-motion) { .label { transition: none; } } @media (forced-colors: active) { .label { forced-color-adjust: none; background-color: CanvasText; color: Canvas; } :host(:not([aria-disabled="true"])) .handle { background-color: CanvasText; } :host([aria-disabled="true"]) .handle { opacity: unset; background-color: GrayText; } } /* ── Vertical orientation ──────────────────────────────────────────────── Set by the parent slider as a custom state: the thumb is slotted light DOM, so its own stylesheet cannot select on the slider's orientation attribute. The thumb is anchored to the bottom, because a vertical slider runs bottom-to-top and the parent positions it with a negative Y translation. Its block size mirrors what .wrapper gives the horizontal thumb on the inline axis, so the parent's clientHeight measurement stays symmetric with clientWidth. */ :host(:is(:state(--vertical), :--vertical)) { top: auto; bottom: 0; inset-inline: 0; height: calc(var(--m3e-slider-thumb-width, 4px) + calc(var(--m3e-slider-thumb-padding, 6px) * 2)); } :host(:is(:state(--vertical), :--vertical)) .touch { height: auto; width: 48px; top: 0; bottom: 0; inset-inline-start: 50%; translate: -50% 0; } :host(:is(:state(--vertical), :--vertical)) .wrapper { width: 100%; height: 100%; align-items: center; } :host(:is(:state(--vertical), :--vertical)) .handle { width: 100%; height: var(--m3e-slider-thumb-width, 4px); transition: ${c(`height ${j.motion.duration.short2} ${j.motion.easing.standard}`)}; } :host( :is(:state(--vertical), :--vertical):is(:active, :focus-visible, :is(:state(--active), :--active)):not( [aria-disabled="true"] ) ) .handle { width: 100%; height: var(--m3e-slider-thumb-pressed-width, 2px); } :host(:is(:state(--vertical), :--vertical)) .focus-ring { top: var(--m3e-focus-ring-thickness, 3px); bottom: var(--m3e-focus-ring-thickness, 3px); left: calc(0px - var(--m3e-focus-ring-thickness, 3px)); right: calc(0px - var(--m3e-focus-ring-thickness, 3px)); } :host(:is(:state(--vertical), :--vertical)) .label { top: calc(50% - calc(var(--_m3e-slider-label-height, 44px) / 2)); inset-inline-start: calc(0px - var(--m3e-slider-label-width, 48px) - var(--_m3e-slider-label-margin, 4px)); }`, e([w(".focus-ring")], bv.prototype, "_focusRing", void 0), e([C({
	type: Number,
	reflect: !0
})], bv.prototype, "value", void 0), bv = e([T("m3e-slider-thumb")], bv);
function xv(e) {
	return e === "horizontal" || e === "vertical";
}
function Sv(e) {
	return e === "extra-small" || e === "small" || e === "medium" || e === "large" || e === "extra-large";
}
var Q, Cv, wv, Tv, $, Ev, Dv, Ov, kv, Av, jv, Mv, Nv, Pv, Fv, Iv, Lv, Rv, zv, Bv, Vv, Hv, Uv, Wv, Gv, Kv, qv, Jv, Yv, Xv, Zv, Qv, $v = class extends id(E(S)) {
	constructor() {
		super(), Q.add(this), Cv.set(this, void 0), this._ticks = [], wv.set(this, /* @__PURE__ */ new Set()), Tv.set(this, []), $.set(this, void 0), Ev.set(this, 0), Dv.set(this, 0), Ov.set(this, 0), kv.set(this, 0), Av.set(this, 0), jv.set(this, 0), this.orientation = "horizontal", this.size = "extra-small", this.disabled = !1, this.discrete = !1, this.min = 0, this.max = 100, this.step = 1, this.labelled = !1, this.displayWith = null, new tr(this, { callback: () => t(this, Q, "m", Hv).call(this, !0) });
	}
	get thumbs() {
		return t(this, Tv, "f");
	}
	get isRange() {
		return t(this, Tv, "f").length > 1;
	}
	get thumb() {
		return t(this, Tv, "f")[0] ?? null;
	}
	get lowerThumb() {
		return this.thumb;
	}
	get upperThumb() {
		return t(this, Tv, "f")[1] ?? null;
	}
	connectedCallback() {
		super.connectedCallback(), t(this, Q, "m", Nv).call(this), t(this, Q, "m", Pv).call(this), n(this, Cv, rd.observe(() => {
			t(this, Q, "m", Hv).call(this, !0), this.requestUpdate();
		}), "f");
	}
	disconnectedCallback() {
		super.disconnectedCallback(), t(this, wv, "f").clear(), t(this, Cv, "f")?.call(this);
	}
	willUpdate(e) {
		super.willUpdate(e), e.has("size") && t(this, Q, "m", Nv).call(this), e.has("orientation") && t(this, Q, "m", Pv).call(this);
	}
	updated(e) {
		super.updated(e), e.has("disabled") && t(this, Tv, "f").forEach((e) => e.disabled = this.disabled), e.has("orientation") && (t(this, Q, "m", Lv).call(this), t(this, Q, "m", Hv).call(this, !0));
	}
	render() {
		return b`<div class="base" tabindex="${nt(this.disabled ? void 0 : "-1")}" @pointerdown="${t(this, Q, "m", Gv)}" @pointermove="${t(this, Q, "m", Kv)}" @pointerup="${t(this, Q, "m", qv)}" @keydown="${t(this, Q, "m", Jv)}" @keyup="${t(this, Q, "m", Yv)}" @value-change="${t(this, Q, "m", Xv)}"><div class="track" aria-hidden="true"><div class="track-inactive start"></div><div class="track-active"></div><div class="track-inactive end"></div></div><div class="ticks" aria-hidden="true">${this._ticks.map((e) => t(this, Q, "m", Fv).call(this, e))}</div><slot @slotchange="${t(this, Q, "m", Iv)}"></slot></div>`;
	}
};
Cv = /* @__PURE__ */ new WeakMap(), wv = /* @__PURE__ */ new WeakMap(), Tv = /* @__PURE__ */ new WeakMap(), $ = /* @__PURE__ */ new WeakMap(), Ev = /* @__PURE__ */ new WeakMap(), Dv = /* @__PURE__ */ new WeakMap(), Ov = /* @__PURE__ */ new WeakMap(), kv = /* @__PURE__ */ new WeakMap(), Av = /* @__PURE__ */ new WeakMap(), jv = /* @__PURE__ */ new WeakMap(), Q = /* @__PURE__ */ new WeakSet(), Mv = function() {
	return this.orientation === "vertical";
}, Nv = function() {
	Sv(this.size) || (this.size = "extra-small"), wr(this, this.size, "extra-large", "extra-small", "large", "medium", "small");
}, Pv = function() {
	xv(this.orientation) || (this.orientation = "horizontal"), wr(this, this.orientation, "horizontal", "vertical");
}, Fv = function(e) {
	return b`<div class="tick ${e.active ? "active" : "inactive"}${e.hidden ? " hidden" : ""}" style="${gr({ transform: t(this, Q, "m", Rv).call(this, t(this, Q, "m", zv).call(this, e.value)) })}"></div>`;
}, Iv = function(e) {
	n(this, Tv, e.target.assignedElements({ flatten: !0 }).filter((e) => e instanceof bv), "f"), t(this, Tv, "f").length > 2 && (t(this, Tv, "f").length = 2), this.isRange ? this._base?.style.setProperty("--_slider-active-track-middle-shape", "0") : this._base?.style.removeProperty("--_slider-active-track-middle-shape"), t(this, Q, "m", Lv).call(this);
}, Lv = function() {
	t(this, Tv, "f").forEach((e, n) => {
		this.disabled && (e.disabled = !0), e.ariaValueMin = `${t(this, Tv, "f")[n - 1]?.value ?? this.min}`, e.ariaValueMax = `${t(this, Tv, "f")[n + 1]?.value ?? this.max}`, e.ariaValueNow = `${e.value ?? t(this, Tv, "f")[n - 1]?.value ?? this.min}`, e.ariaOrientation = this.orientation, t(this, Q, "a", Mv) ? Cr(e, "--vertical") : D(e, "--vertical");
	});
}, Rv = function(e) {
	return t(this, Q, "a", Mv) ? `translate(0, ${-e}px)` : `translate(${rd.current === "rtl" ? -e : e}px, 0)`;
}, zv = function(e) {
	return (t(this, Ev, "f") - t(this, Dv, "f")) * ((e - this.min) / (this.max - this.min));
}, Bv = function(e) {
	let n = t(this, Q, "a", Mv) || rd.current === "rtl" ? t(this, kv, "f") - (t(this, Q, "a", Mv) ? e.clientY : e.clientX) : e.clientX - t(this, Ov, "f"), r = this.step === 0 ? 1 : this.step, i = Math.floor((this.max - this.min) / r), a = t(this, Ev, "f") ? t(this, Dv, "f") / t(this, Ev, "f") : 0, o = (n / t(this, Av, "f") - a / 2) / (1 - a || 1), s = Math.round(o * i) / i * (this.max - this.min) + this.min;
	return Math.round(s / r) * r;
}, Vv = function(e = !1) {
	if (!this.lowerThumb) return;
	let r = t(this, Q, "a", Mv) ? this.clientHeight : this.clientWidth, i = t(this, Q, "a", Mv) ? this.lowerThumb.clientHeight : this.lowerThumb.clientWidth;
	n(this, Ev, !e && t(this, Ev, "f") > 0 ? t(this, Ev, "f") : r, "f"), n(this, Dv, !e && t(this, Dv, "f") > 0 ? t(this, Dv, "f") : i, "f");
	let a = this.getBoundingClientRect(), o = t(this, Q, "a", Mv) ? a.top : a.left, s = t(this, Q, "a", Mv) ? a.bottom : a.right, c = t(this, Q, "a", Mv) ? a.height : a.width;
	n(this, Ov, !e && t(this, Ov, "f") > 0 ? t(this, Ov, "f") : o, "f"), n(this, kv, !e && t(this, kv, "f") > 0 ? t(this, kv, "f") : s, "f"), n(this, Av, !e && t(this, Av, "f") > 0 ? t(this, Av, "f") : c, "f");
}, Hv = function(e = !1) {
	if (t(this, Q, "m", Vv).call(this, e), !this.lowerThumb) return;
	let n = this.lowerThumb.value ?? this.min, r = t(this, Q, "m", zv).call(this, n);
	if (this.lowerThumb.style.transform = t(this, Q, "m", Rv).call(this, r), !this.upperThumb) this._base?.classList.toggle("range", !1), this._base?.style.setProperty("--_slider-active-track-size", `${r}px`), this._base?.style.setProperty("--_slider-inactive-track-after-offset", `${r + t(this, Dv, "f")}px`), this._base?.style.setProperty("--_slider-inactive-track-after-size", `${t(this, Ev, "f") - r - t(this, Dv, "f")}px`), t(this, Q, "m", Uv).call(this, (e) => e < n);
	else {
		let e = this.upperThumb.value ?? n, i = t(this, Q, "m", zv).call(this, e);
		this.upperThumb.style.transform = t(this, Q, "m", Rv).call(this, i), this._base?.classList.toggle("range", !0), this._base?.style.setProperty("--_slider-inactive-track-before-size", `${r}px`), this._base?.style.setProperty("--_slider-active-track-offset", `${r + t(this, Dv, "f")}px`), this._base?.style.setProperty("--_slider-active-track-size", `${i - r - t(this, Dv, "f")}px`), this._base?.style.setProperty("--_slider-inactive-track-after-offset", `${i + t(this, Dv, "f")}px`), this._base?.style.setProperty("--_slider-inactive-track-after-size", `${t(this, Ev, "f") - t(this, Dv, "f") - i}px`), t(this, Q, "m", Uv).call(this, (t) => t > n && t < e);
	}
}, Uv = function(e) {
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
	t(this, Q, "m", Wv).call(this);
}, Wv = function() {
	if (t(this, Ev, "f") === 0 || t(this, Dv, "f") === 0) return;
	let e = t(this, Tv, "f").filter((e) => e.value != null);
	if (e.length === 0) return;
	let n = t(this, Dv, "f") / 2;
	this._ticks = this._ticks.map((r) => {
		let i = t(this, Q, "m", zv).call(this, r.value), a = e.some((e) => {
			let r = t(this, Q, "m", zv).call(this, e.value);
			return Math.abs(i - r) < n;
		});
		return {
			...r,
			hidden: a
		};
	});
}, Gv = function(e) {
	if (t(this, Q, "m", Vv).call(this, !0), e.pointerType === "mouse" && e.button > 1 || !this.lowerThumb || this.disabled || (e.target instanceof HTMLElement && e.target.setPointerCapture(e.pointerId), t(this, wv, "f").clear(), n(this, jv, e.timeStamp, "f"), n(this, $, e.composedPath().find((e) => e instanceof bv), "f"), t(this, $, "f"))) return;
	let r = t(this, Q, "m", Bv).call(this, e), i = this.min, a = this.max;
	if (t(this, $, "f") === this.upperThumb ? i = Math.max(i, this.lowerThumb?.value ?? 0) : this.upperThumb && (a = Math.min(a, this.upperThumb.value ?? this.max)), !this.upperThumb) this.lowerThumb.disabled || (t(this, Q, "m", Zv).call(this, this.lowerThumb, Math.min(a, Math.max(i, r)), !0), n(this, $, this.lowerThumb, "f"));
	else {
		let e = this.lowerThumb.value ?? this.min, o = this.upperThumb.value ?? e;
		r < e ? this.lowerThumb.disabled || (t(this, Q, "m", Zv).call(this, this.lowerThumb, Math.min(a, Math.max(i, r)), !0), n(this, $, this.lowerThumb, "f")) : r > o ? this.upperThumb.disabled || (t(this, Q, "m", Zv).call(this, this.upperThumb, Math.min(a, Math.max(i, r)), !0), n(this, $, this.upperThumb, "f")) : r < (e + o) / 2 && !this.lowerThumb.disabled ? (t(this, Q, "m", Zv).call(this, this.lowerThumb, Math.min(a, Math.max(i, r)), !0), n(this, $, this.lowerThumb, "f")) : this.upperThumb.disabled || (t(this, Q, "m", Zv).call(this, this.upperThumb, Math.min(a, Math.max(i, r)), !0), n(this, $, this.upperThumb, "f"));
	}
	t(this, $, "f") && Cr(t(this, $, "f"), "--active");
}, Kv = function(e) {
	if (!(e.target instanceof HTMLElement) || !e.target.hasPointerCapture(e.pointerId) || !t(this, $, "f") || t(this, $, "f").disabled) return;
	let r = t(this, Q, "m", Bv).call(this, e), i = this.min, a = this.max;
	if (t(this, $, "f") === this.upperThumb ? i = Math.max(i, this.lowerThumb?.value ?? 0) : this.upperThumb && (a = Math.min(a, this.upperThumb.value ?? this.max)), r = Math.min(a, Math.max(i, r)), r === t(this, $, "f").value) return;
	let o = (this.step === 0 ? 1 : this.step) !== 1;
	if (o) for (let r of e.getCoalescedEvents()) r.timeStamp - t(this, jv, "f") < 90 && (o = !1), n(this, jv, r.timeStamp, "f");
	o || Sr(this, "--animating") && (D(this, "--animating"), t(this, $, "f").style.transition = ""), t(this, Q, "m", Zv).call(this, t(this, $, "f"), r, o);
}, qv = function(e) {
	e.pointerType === "mouse" && e.button > 1 || this.lowerThumb && !this.disabled && (e.target instanceof HTMLElement && e.target.releasePointerCapture(e.pointerId), n(this, jv, 0, "f"), t(this, $, "f") && !t(this, $, "f").disabled && (t(this, Q, "m", Qv).call(this, t(this, $, "f")), t(this, $, "f").focus(), D(t(this, $, "f"), "--active")));
}, Jv = function(e) {
	if (n(this, $, e.composedPath().find((e) => e instanceof bv), "f"), !t(this, $, "f")) return;
	let r = t(this, $, "f").value ?? 0, i = this.min, a = this.max;
	t(this, $, "f") === this.upperThumb ? i = Math.max(i, this.lowerThumb?.value ?? 0) : this.upperThumb && (a = Math.max(a, this.upperThumb.value ?? this.max));
	let o = t(this, Q, "a", Mv) || rd.current === "ltr";
	switch (e.key) {
		case "Home":
			t(this, Q, "m", Zv).call(this, t(this, $, "f"), i), e.preventDefault();
			break;
		case "End":
			t(this, Q, "m", Zv).call(this, t(this, $, "f"), a), e.preventDefault();
			break;
		case "PageUp":
			o ? t(this, Q, "m", Zv).call(this, t(this, $, "f"), Math.min(a, r + (this.step > 1 ? this.step : 10))) : t(this, Q, "m", Zv).call(this, t(this, $, "f"), Math.max(i, r - (this.step > 1 ? this.step : 10))), e.preventDefault();
			break;
		case "PageDown":
			o ? t(this, Q, "m", Zv).call(this, t(this, $, "f"), Math.max(i, r - (this.step > 1 ? this.step : 10))) : t(this, Q, "m", Zv).call(this, t(this, $, "f"), Math.min(a, r + (this.step > 1 ? this.step : 10))), e.preventDefault();
			break;
		case "Down":
		case "ArrowDown":
		case "Left":
		case "ArrowLeft":
			o ? t(this, Q, "m", Zv).call(this, t(this, $, "f"), Math.max(i, r - this.step)) : t(this, Q, "m", Zv).call(this, t(this, $, "f"), Math.min(a, r + this.step)), e.preventDefault();
			break;
		case "Up":
		case "ArrowUp":
		case "Right":
		case "ArrowRight":
			o ? t(this, Q, "m", Zv).call(this, t(this, $, "f"), Math.min(a, r + this.step)) : t(this, Q, "m", Zv).call(this, t(this, $, "f"), Math.max(i, r - this.step)), e.preventDefault();
			break;
		case " ": e.preventDefault();
	}
}, Yv = function(e) {
	let n = e.composedPath().find((e) => e instanceof bv);
	n && t(this, Q, "m", Qv).call(this, n);
}, Xv = function(e) {
	e.stopPropagation(), t(this, Q, "m", Lv).call(this), t(this, Q, "m", Hv).call(this);
}, Zv = function(e, n, r = !1) {
	e.value !== n && e.dispatchEvent(new Event("beforeinput", {
		bubbles: !0,
		cancelable: !0
	})) && (r && !It() && (Cr(this, "--animating"), e.addEventListener("transitionend", () => {
		e.style.transition = "", D(this, "--animating");
	}, { once: !0 }), e.style.transition = `transform ${j.motion.spring.fastEffects}`), t(this, wv, "f").add(e), e.value = n, e.markAsDirty(), e.markAsTouched(), e.dispatchEvent(new Event("input", { bubbles: !0 })));
}, Qv = function(e) {
	t(this, wv, "f").has(e) && (e.dispatchEvent(new Event("change", { bubbles: !0 })), t(this, wv, "f").delete(e));
}, $v.styles = l`:host { display: inline-block; vertical-align: middle; min-inline-size: var(--m3e-slider-min-width, 200px); user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); line-height: 0; } :host([hidden]) { display: none; } :host(:not([disabled])) { cursor: pointer; } :host(:is(:state(--extra-small), :--extra-small)), :host(:is(:state(--small), :--small)) { height: var(--m3e-slider-small-height, 44px); } :host(:not(:is(:state(--rtl), :--rtl)):is(:state(--extra-small), :--extra-small)) .base, :host(:not(:is(:state(--rtl), :--rtl)):is(:state(--small), :--small)) .base { --_slider-active-track-shape: var(--m3e-slider-small-active-track-shape, ${j.shape.corner.smallStart}); --_slider-inactive-track-start-shape: var( --m3e-slider-small-inactive-active-track-start-shape, ${j.shape.corner.smallStart} ); --_slider-inactive-track-end-shape: var( --m3e-slider-small-inactive-track-end-shape, ${j.shape.corner.smallEnd} ); } :host(:is(:state(--rtl), :--rtl):is(:state(--extra-small), :--extra-small)) .base, :host(:is(:state(--rtl), :--rtl):is(:state(--small), :--small)) .base { --_slider-active-track-shape: var(--m3e-slider-small-active-track-shape, ${j.shape.corner.smallEnd}); --_slider-inactive-track-start-shape: var( --m3e-slider-small-inactive-active-track-start-shape, ${j.shape.corner.smallEnd} ); --_slider-inactive-track-end-shape: var( --m3e-slider-small-inactive-track-end-shape, ${j.shape.corner.smallStart} ); } :host(:is(:state(--extra-small), :--extra-small)) .track { height: calc(var(--m3e-slider-extra-small-track-height, 16px)); } :host(:is(:state(--small), :--small)) .track { height: calc(var(--m3e-slider-small-track-height, 24px)); } :host(:is(:state(--medium), :--medium)) { height: var(--m3e-slider-medium-height, 52px); } :host(:not(:is(:state(--rtl), :--rtl)):is(:state(--medium), :--medium)) .base { --_slider-active-track-shape: var( --m3e-slider-medium-active-track-shape, ${j.shape.corner.mediumStart} ); --_slider-inactive-track-start-shape: var( --m3e-slider-medium-inactive-active-track-start-shape, ${j.shape.corner.mediumStart} ); --_slider-inactive-track-end-shape: var( --m3e-slider-medium-inactive-track-end-shape, ${j.shape.corner.mediumEnd} ); } :host(:is(:state(--rtl), :--rtl):is(:state(--medium), :--medium)) .base { --_slider-active-track-shape: var(--m3e-slider-medium-active-track-shape, ${j.shape.corner.mediumEnd}); --_slider-inactive-track-start-shape: var( --m3e-slider-medium-inactive-active-track-start-shape, ${j.shape.corner.mediumEnd} ); --_slider-inactive-track-end-shape: var( --m3e-slider-medium-inactive-track-end-shape, ${j.shape.corner.mediumStart} ); } :host(:is(:state(--medium), :--medium)) .track { height: var(--m3e-slider-medium-track-height, 40px); } :host(:is(:state(--large), :--large)) { height: var(--m3e-slider-large-height, 68px); } :host(:not(:is(:state(--rtl), :--rtl)):is(:state(--large), :--large)) .base { --_slider-active-track-shape: var(--m3e-slider-large-active-track-shape, ${j.shape.corner.largeStart}); --_slider-inactive-track-start-shape: var( --m3e-slider-large-inactive-active-track-start-shape, ${j.shape.corner.largeStart} ); --_slider-inactive-track-end-shape: var( --m3e-slider-large-inactive-track-end-shape, ${j.shape.corner.largeEnd} ); } :host(:is(:state(--rtl), :--rtl):is(:state(--large), :--large)) .base { --_slider-active-track-shape: var(--m3e-slider-large-active-track-shape, ${j.shape.corner.largeEnd}); --_slider-inactive-track-start-shape: var( --m3e-slider-large-inactive-active-track-start-shape, ${j.shape.corner.largeEnd} ); --_slider-inactive-track-end-shape: var( --m3e-slider-large-inactive-track-end-shape, ${j.shape.corner.largeStart} ); } :host(:is(:state(--large), :--large)) .track { height: var(--m3e-slider-large-track-height, 56px); } :host(:is(:state(--extra-large), :--extra-large)) { height: var(--m3e-slider-extra-large-height, 108px); } :host(:not(:is(:state(--rtl), :--rtl)):is(:state(--extra-large), :--extra-large)) .base { --_slider-active-track-shape: var( --m3e-slider-extra-large-active-track-shape, ${j.shape.corner.extraLargeStart} ); --_slider-inactive-track-start-shape: var( --m3e-slider-extra-large-inactive-active-track-start-shape, ${j.shape.corner.extraLargeStart} ); --_slider-inactive-track-end-shape: var( --m3e-slider-extra-large-inactive-track-end-shape, ${j.shape.corner.extraLargeEnd} ); } :host(:is(:state(--rtl), :--rtl):is(:state(--extra-large), :--extra-large)) .base { --_slider-active-track-shape: var( --m3e-slider-extra-large-active-track-shape, ${j.shape.corner.extraLargeEnd} ); --_slider-inactive-track-start-shape: var( --m3e-slider-extra-large-inactive-active-track-start-shape, ${j.shape.corner.extraLargeEnd} ); --_slider-inactive-track-end-shape: var( --m3e-slider-extra-large-inactive-track-end-shape, ${j.shape.corner.extraLargeStart} ); } :host(:is(:state(--extra-large), :--extra-large)) .track { height: var(--m3e-slider-extra-large-track-height, 96px); } :host(:is(:state(--animating), :--animating)) .track-active, :host(:is(:state(--animating), :--animating)) .track-inactive.start, :host(:is(:state(--animating), :--animating)) .track-inactive.end { transition: ${c(`margin-inline-start ${j.motion.spring.fastEffects},
        width ${j.motion.spring.fastEffects}`)}; } .base { contain: layout style; display: inline-flex; align-items: center; position: relative; width: 100%; height: 100%; border-radius: inherit; outline: none; touch-action: none; } .track { contain: layout style paint; position: relative; flex: 1 1 auto; touch-action: none; } .track-inactive, .track-active { contain: layout style paint; position: absolute; height: 100%; touch-action: none; } .track-active { margin-inline-start: var(--_slider-active-track-offset, 0px); width: var(--_slider-active-track-size, 0px); border-radius: var(--_slider-active-track-middle-shape, var(--_slider-active-track-shape)); } .track-inactive.start { width: var(--_slider-inactive-track-before-size, 0px); border-radius: var(--_slider-inactive-track-start-shape); } .track-inactive.end { margin-inline-start: var(--_slider-inactive-track-after-offset, 0px); width: var(--_slider-inactive-track-after-size, 0px); border-radius: var(--_slider-inactive-track-end-shape); } .ticks { position: absolute; width: 100%; height: var(--m3e-slider-tick-size, 4px); overflow: visible; touch-action: none; } .tick { position: absolute; top: 0; touch-action: none; inset-inline-start: calc(var(--m3e-slider-tick-size, 4px) + calc(var(--m3e-slider-tick-size, 4px) / 2)); width: var(--m3e-slider-tick-size, 4px); height: var(--m3e-slider-tick-size, 4px); border-radius: var(--m3e-slider-tick-shape, ${j.shape.corner.full}); } .tick.hidden { visibility: hidden; } :host(:not([disabled])) .track-inactive { background-color: var(--m3e-slider-inactive-track-color, ${j.color.secondaryContainer}); } :host([disabled]) .track-inactive { background-color: color-mix( in srgb, var(--m3e-slider-disabled-inactive-track-color, ${j.color.onSurface}) var(--m3e-slider-disabled-inactive-track-opacity, 12%), transparent ); } :host(:not([disabled])) .track-active { background-color: var(--m3e-slider-active-track-color, ${j.color.primary}); } :host([disabled]) .track-active { background-color: color-mix( in srgb, var(--m3e-slider-disabled-active-track-color, ${j.color.onSurface}) var(--m3e-slider-disabled-active-track-opacity, 38%), transparent ); } :host(:not([disabled])) .tick.active { background-color: var(--m3e-slider-tick-active-color, ${j.color.onPrimary}); } :host([disabled]) .tick.active { background-color: var(--m3e-slider-disabled-tick-active-color, ${j.color.inverseOnSurface}); } :host(:not([disabled])) .tick.inactive { background-color: var(--m3e-slider-tick-inactive-color, ${j.color.onSecondaryContainer}); } :host([disabled]) .tick.inactive { background-color: var(--m3e-slider-disabled-tick-inactive-color, ${j.color.onSurface}); } :host(:not([discrete])) .tick.active { display: none; } :host(:hover[labelled]) .base, :host(:focus-within[labelled]) .base { --_slider-label-visibility: visible; --_slider-label-opacity: 1; --_slider-label-transform: scale(1); } @media (forced-colors: active) { :host(:not([disabled])) .track-inactive { background-color: unset; } :host(:not([disabled])) .base.range .track-inactive.start, :host(:not([disabled])) .track-inactive.end { border: 1px solid CanvasText; box-sizing: border-box; } :host(:not([disabled])) .tick.inactive { background-color: CanvasText; } :host(:not([disabled])) .tick.active { background-color: Canvas; } :host(:not([disabled])) .track-active { background-color: CanvasText; } :host([disabled]) .base.range .track-inactive.start, :host([disabled]) .track-inactive.end { border: 1px solid GrayText; box-sizing: border-box; } :host([disabled]) .track-active { background-color: GrayText; } :host([disabled]) .tick.inactive { background-color: GrayText; } :host([disabled]) .tick.active { background-color: Canvas; } } /* ── Vertical orientation ──────────────────────────────────────────────── Everything above lays the slider out along the inline axis. These rules re-map it to the block axis and are deliberately additive: they come last and so win on source order, which keeps the horizontal path untouched. Segments are anchored with bottom rather than margin-inline-start because a vertical slider runs bottom-to-top, so the offsets the element writes — measured from the end holding the minimum — already point the right way. The five --_slider-* custom properties are plain lengths and need no change; only the property they feed does. Corner radii are composed from the symmetric corner tokens rather than the Start/End ones, whose 4-value radii round the inline edges. */ :host(:is(:state(--vertical), :--vertical)) { min-inline-size: auto; min-block-size: var(--m3e-slider-min-width, 200px); /* Containing block for .base below. */ position: relative; } /* .base is taken out of flow rather than sized with height: 100%. A vertical host has no definite block size of its own - its height comes from min-block-size, from a flex parent, or from the consumer - and a percentage height resolves against the parent's *computed* height, which is auto. That collapses .base to zero and takes the track and both thumbs with it, since .base is their containing block. Insets resolve against the used height, so they work in every case. */ :host(:is(:state(--vertical), :--vertical)) .base { position: absolute; inset: 0; width: auto; height: auto; flex-direction: column; } :host(:is(:state(--vertical), :--vertical):is(:state(--extra-small), :--extra-small)), :host(:is(:state(--vertical), :--vertical):is(:state(--small), :--small)) { height: auto; width: var(--m3e-slider-small-height, 44px); } :host(:is(:state(--vertical), :--vertical):is(:state(--extra-small), :--extra-small)) .track { height: auto; width: calc(var(--m3e-slider-extra-small-track-height, 16px)); } :host(:is(:state(--vertical), :--vertical):is(:state(--small), :--small)) .track { height: auto; width: calc(var(--m3e-slider-small-track-height, 24px)); } :host(:is(:state(--vertical), :--vertical):is(:state(--medium), :--medium)) { height: auto; width: var(--m3e-slider-medium-height, 52px); } :host(:is(:state(--vertical), :--vertical):is(:state(--medium), :--medium)) .track { height: auto; width: var(--m3e-slider-medium-track-height, 40px); } :host(:is(:state(--vertical), :--vertical):is(:state(--large), :--large)) { height: auto; width: var(--m3e-slider-large-height, 68px); } :host(:is(:state(--vertical), :--vertical):is(:state(--large), :--large)) .track { height: auto; width: var(--m3e-slider-large-track-height, 56px); } :host(:is(:state(--vertical), :--vertical):is(:state(--extra-large), :--extra-large)) { height: auto; width: var(--m3e-slider-extra-large-height, 108px); } :host(:is(:state(--vertical), :--vertical):is(:state(--extra-large), :--extra-large)) .track { height: auto; width: var(--m3e-slider-extra-large-track-height, 96px); } :host(:is(:state(--vertical), :--vertical):is(:state(--extra-small), :--extra-small)) .base { --_slider-active-track-shape: var( --m3e-slider-extra-small-active-track-shape, 0 0 ${j.shape.corner.small} ${j.shape.corner.small} ); --_slider-inactive-track-start-shape: var( --m3e-slider-extra-small-inactive-active-track-start-shape, 0 0 ${j.shape.corner.small} ${j.shape.corner.small} ); --_slider-inactive-track-end-shape: var( --m3e-slider-extra-small-inactive-track-end-shape, ${j.shape.corner.small} ${j.shape.corner.small} 0 0 ); } :host(:is(:state(--vertical), :--vertical):is(:state(--small), :--small)) .base { --_slider-active-track-shape: var( --m3e-slider-small-active-track-shape, 0 0 ${j.shape.corner.small} ${j.shape.corner.small} ); --_slider-inactive-track-start-shape: var( --m3e-slider-small-inactive-active-track-start-shape, 0 0 ${j.shape.corner.small} ${j.shape.corner.small} ); --_slider-inactive-track-end-shape: var( --m3e-slider-small-inactive-track-end-shape, ${j.shape.corner.small} ${j.shape.corner.small} 0 0 ); } :host(:is(:state(--vertical), :--vertical):is(:state(--medium), :--medium)) .base { --_slider-active-track-shape: var( --m3e-slider-medium-active-track-shape, 0 0 ${j.shape.corner.medium} ${j.shape.corner.medium} ); --_slider-inactive-track-start-shape: var( --m3e-slider-medium-inactive-active-track-start-shape, 0 0 ${j.shape.corner.medium} ${j.shape.corner.medium} ); --_slider-inactive-track-end-shape: var( --m3e-slider-medium-inactive-track-end-shape, ${j.shape.corner.medium} ${j.shape.corner.medium} 0 0 ); } :host(:is(:state(--vertical), :--vertical):is(:state(--large), :--large)) .base { --_slider-active-track-shape: var( --m3e-slider-large-active-track-shape, 0 0 ${j.shape.corner.large} ${j.shape.corner.large} ); --_slider-inactive-track-start-shape: var( --m3e-slider-large-inactive-active-track-start-shape, 0 0 ${j.shape.corner.large} ${j.shape.corner.large} ); --_slider-inactive-track-end-shape: var( --m3e-slider-large-inactive-track-end-shape, ${j.shape.corner.large} ${j.shape.corner.large} 0 0 ); } :host(:is(:state(--vertical), :--vertical):is(:state(--extra-large), :--extra-large)) .base { --_slider-active-track-shape: var( --m3e-slider-extra-large-active-track-shape, 0 0 ${j.shape.corner.extraLarge} ${j.shape.corner.extraLarge} ); --_slider-inactive-track-start-shape: var( --m3e-slider-extra-large-inactive-active-track-start-shape, 0 0 ${j.shape.corner.extraLarge} ${j.shape.corner.extraLarge} ); --_slider-inactive-track-end-shape: var( --m3e-slider-extra-large-inactive-track-end-shape, ${j.shape.corner.extraLarge} ${j.shape.corner.extraLarge} 0 0 ); } :host(:is(:state(--vertical), :--vertical)) .track-inactive, :host(:is(:state(--vertical), :--vertical)) .track-active { width: 100%; height: auto; margin-inline-start: 0; } :host(:is(:state(--vertical), :--vertical)) .track-active { bottom: var(--_slider-active-track-offset, 0px); height: var(--_slider-active-track-size, 0px); } :host(:is(:state(--vertical), :--vertical)) .track-inactive.start { bottom: 0; height: var(--_slider-inactive-track-before-size, 0px); } :host(:is(:state(--vertical), :--vertical)) .track-inactive.end { bottom: var(--_slider-inactive-track-after-offset, 0px); height: var(--_slider-inactive-track-after-size, 0px); } :host(:is(:state(--vertical), :--vertical):is(:state(--animating), :--animating)) .track-active, :host(:is(:state(--vertical), :--vertical):is(:state(--animating), :--animating)) .track-inactive.start, :host(:is(:state(--vertical), :--vertical):is(:state(--animating), :--animating)) .track-inactive.end { transition: ${c(`bottom ${j.motion.spring.fastEffects},
        height ${j.motion.spring.fastEffects}`)}; } :host(:is(:state(--vertical), :--vertical)) .ticks { width: var(--m3e-slider-tick-size, 4px); height: 100%; } :host(:is(:state(--vertical), :--vertical)) .tick { top: auto; inset-inline-start: 0; bottom: calc(var(--m3e-slider-tick-size, 4px) + calc(var(--m3e-slider-tick-size, 4px) / 2)); }`, e([w(".base")], $v.prototype, "_base", void 0), e([Xe()], $v.prototype, "_ticks", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], $v.prototype, "orientation", void 0), e([C({
	reflect: !0,
	useDefault: !0
})], $v.prototype, "size", void 0), e([C({
	type: Boolean,
	reflect: !0
})], $v.prototype, "disabled", void 0), e([C({
	type: Boolean,
	reflect: !0
})], $v.prototype, "discrete", void 0), e([C({ type: Number })], $v.prototype, "min", void 0), e([C({ type: Number })], $v.prototype, "max", void 0), e([C({ type: Number })], $v.prototype, "step", void 0), e([C({ type: Boolean })], $v.prototype, "labelled", void 0), e([C({ attribute: !1 })], $v.prototype, "displayWith", void 0), $v = e([T("m3e-slider")], $v);
//#endregion
//#region skills/android-design-explore/gallery/runtime-src/p-icon.js
var ey = class extends HTMLElement {
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
customElements.define("p-icon", ey);
//#endregion
//#region skills/android-design-explore/gallery/runtime-src/p-floating-toolbar.js
var ty = class extends HTMLElement {
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
customElements.define("p-floating-toolbar", ty);
//#endregion
//#region skills/android-design-explore/gallery/runtime-src/theme-bridge.js
var ny = [
	"monochrome",
	"neutral",
	"tonal-spot",
	"vibrant",
	"expressive",
	"fidelity",
	"content",
	"rainbow",
	"fruit-salad"
], ry = [
	"light",
	"dark",
	"auto"
];
window.addEventListener("message", (e) => {
	if (e.origin !== window.location.origin) return;
	let { type: t, color: n, variant: r, scheme: i } = e.data ?? {};
	if (t !== "theme") return;
	let a = document.querySelector("m3e-theme");
	a && (typeof n == "string" && /^#[0-9a-f]{6}$/i.test(n) && a.setAttribute("color", n), ny.includes(r) && a.setAttribute("variant", r), ry.includes(i) && a.setAttribute("scheme", i));
});
var iy = {
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
}, ay = ({ r: e, g: t, b: n }) => {
	let r = (e) => (e /= 255, e <= .03928 ? e / 12.92 : ((e + .055) / 1.055) ** 2.4);
	return .2126 * r(e) + .7152 * r(t) + .0722 * r(n);
}, oy = (e, t) => {
	let [n, r] = [ay(e), ay(t)].sort((e, t) => t - e);
	return (n + .05) / (r + .05);
}, sy = (e, t) => e.a < 1 ? {
	r: e.r * e.a + t.r * (1 - e.a),
	g: e.g * e.a + t.g * (1 - e.a),
	b: e.b * e.a + t.b * (1 - e.a),
	a: 1
} : e, cy = (e, t) => e >= 24 || e >= 18.66 && t >= 700 ? 3 : 4.5, ly = (/* @__PURE__ */ "a[href].button.input.select.textarea.summary.[role=button].[role=link].[role=tab].[role=switch].[role=checkbox].[role=menuitem].[tabindex]:not([tabindex=\"-1\"]).m3e-button.m3e-icon-button.m3e-fab.m3e-fab-menu-trigger.m3e-fab-menu-item.m3e-nav-item.m3e-switch.m3e-slider.m3e-split-button.m3e-card[actionable].m3e-list-item-button.m3e-list-action.m3e-list-option.m3e-chip.m3e-assist-chip.m3e-filter-chip.m3e-input-chip.m3e-suggestion-chip".split(".")).join(","), uy = "m3e-fab, m3e-fab-menu, [data-fab]", dy = (e, t) => {
	let n = e.split(",")[0].trim().replace(/^(["'])(.*)\1$/, "$2").toLowerCase();
	if (n === iy.plain.toLowerCase()) return !0;
	let r = iy.faces.find((e) => e.family.toLowerCase() === n);
	return !!r && t >= r.minPx;
}, fy = (e) => (e.getAttribute?.("aria-label") || e.textContent || "").trim().replace(/\s+/g, " ").slice(0, 30), py = (e) => {
	let t = fy(e);
	return e.localName + (e.id ? `#${e.id}` : "") + (t ? ` "${t}"` : "");
}, my = (e) => {
	let t = e.getRootNode().host, n = e.localName + (e.id ? `#${e.id}` : "") + (t && e.classList[0] ? `.${e.classList[0]}` : "");
	return t ? `${my(t)} > ${n}` : n;
}, hy = (e) => {
	let t = fy(e);
	return my(e) + (t ? ` "${t}"` : "");
}, gy = (e) => {
	let t = e.getBoundingClientRect();
	return t.width <= 0 || t.height <= 0 ? !1 : e.checkVisibility ? e.checkVisibility({ visibilityProperty: !0 }) : getComputedStyle(e).visibility !== "hidden";
};
function _y(e) {
	if (e.assignedSlot) return e.assignedSlot;
	let t = e.parentNode;
	return t ? t instanceof ShadowRoot ? t.host : t instanceof Element && t.shadowRoot ? null : t instanceof Element ? t : null : null;
}
function vy(e) {
	for (let t = e; t; t = _y(t)) if (getComputedStyle(t).display !== "contents") return t;
	return null;
}
var yy = (e) => {
	for (let t = e; t; t = _y(t)) if (parseFloat(getComputedStyle(t).opacity) === 0) return !0;
	return !1;
}, by = (e) => {
	for (let t = e; t; t = _y(t)) if (t.getAttribute?.("aria-hidden") === "true") return !0;
	return !1;
};
function xy(e, t) {
	for (let n of e.querySelectorAll("*")) t(n) !== !1 && n.shadowRoot && xy(n.shadowRoot, t);
}
function Sy(e, t) {
	let n = (e) => {
		for (let n of e.childNodes) n.nodeType === Node.TEXT_NODE && n.textContent.trim() && t(n);
	};
	e instanceof ShadowRoot && n(e), xy(e, (e) => {
		n(e);
	});
}
var Cy = document.createElement("canvas");
Cy.width = Cy.height = 1;
var wy = Cy.getContext("2d", { willReadFrequently: !0 });
function Ty(e) {
	wy.clearRect(0, 0, 1, 1), wy.fillStyle = "#000", wy.fillStyle = e, wy.fillRect(0, 0, 1, 1);
	let [t, n, r, i] = wy.getImageData(0, 0, 1, 1).data;
	return {
		r: t,
		g: n,
		b: r,
		a: i / 255
	};
}
function Ey(e) {
	for (let t = e; t; t = _y(t)) {
		let e = getComputedStyle(t);
		if (parseFloat(e.opacity) < 1) return { reason: "an ancestor has opacity below 1" };
		if (e.backgroundImage !== "none") return { reason: "background is an image or gradient" };
		let n = Ty(e.backgroundColor);
		if (n.a === 1) return { color: n };
		if (n.a > 0) return { reason: "background is translucent" };
	}
	return { reason: "no opaque background found" };
}
var Dy = /[^\s\-\u00ad\u200b\u2010-\u2015/]+/g;
function Oy(e, t) {
	if (t.hyphens === "auto" || /[　-鿿가-힯฀-๿]/.test(e.textContent)) return [];
	let n = [], r = document.createRange();
	for (let t of e.textContent.matchAll(Dy)) {
		if (t[0].length < 2) continue;
		r.setStart(e, t.index), r.setEnd(e, t.index + t[0].length);
		let i = [...r.getClientRects()].filter((e) => e.width > 0 && e.height > 0);
		if (i.length < 2) continue;
		let a = i.map((e) => e.top);
		Math.max(...a) - Math.min(...a) > Math.min(...i.map((e) => e.height)) / 2 && n.push(t[0]);
	}
	return n;
}
var ky = .2;
function Ay(e) {
	let t = 1;
	for (let n = e; n; n = _y(n)) t *= parseFloat(getComputedStyle(n).opacity);
	return t;
}
function jy(e, t) {
	for (let n = t; n; n = _y(n)) if (n === e) return !0;
	return !1;
}
function My(e, t, n) {
	let r = document.createRange();
	for (let i of e.childNodes) if (i.nodeType === Node.TEXT_NODE && i.textContent.trim()) {
		r.selectNodeContents(i);
		for (let e of r.getClientRects()) if (t >= e.left && t <= e.right && n >= e.top && n <= e.bottom) return !0;
	}
	return !1;
}
function Ny(e, t, n) {
	let r = getComputedStyle(e);
	return r.visibility !== "visible" || Ay(e) < .9 ? !1 : /^(img|svg|canvas|video|picture)$/i.test(e.localName) || r.backgroundImage.includes("url(") || Ty(r.backgroundColor).a >= .9 || My(e, t, n) ? !0 : !!e.shadowRoot?.elementsFromPoint(t, n).some((r) => r !== e && e.shadowRoot.contains(r) && Ny(r, t, n));
}
function Py(e) {
	for (let t = e; t && t !== document.documentElement && t !== document.body; t = _y(t)) {
		let e = getComputedStyle(t);
		if (/auto|scroll/.test(e.overflowY) && t.scrollHeight > t.clientHeight + 1) return t;
	}
	return document.documentElement;
}
function Fy(e, t, n) {
	let r = (e) => {
		for (let t = e; t; t = _y(t)) if (/fixed|sticky/.test(getComputedStyle(t).position)) return !0;
		return !1;
	};
	if (r(t) || !r(n)) return !1;
	let i = Py(t), a = i.scrollHeight - i.clientHeight - i.scrollTop;
	if (a <= 0) return !1;
	let o = n.getBoundingClientRect(), s = e.top - a;
	return e.bottom - a <= o.top || s >= o.bottom || e.right <= o.left || e.left >= o.right;
}
function Iy(e, t) {
	let n = document.createRange();
	n.selectNodeContents(e);
	let r = [...n.getClientRects()].filter((e) => e.width > 0 && e.height > 0), i = Math.max(1, Math.ceil(r.length / 8)), a = 0, o = 0, s = null;
	for (let e of r.filter((e, t) => t % i === 0)) for (let n of [
		.1,
		.3,
		.5,
		.7,
		.9
	]) {
		let r = e.left + e.width * n, i = e.top + e.height / 2;
		if (r < 0 || i < 0 || r >= innerWidth || i >= innerHeight) continue;
		let c = document.elementsFromPoint(r, i), l = c.findIndex((e) => jy(e, t));
		if (l < 0) continue;
		a++;
		let u = c.slice(0, l).find((n) => !jy(t, n) && !yy(n) && Ny(n, r, i) && !Fy(e, t, n));
		u && (o++, s ??= u);
	}
	return a > 0 && o / a >= ky ? {
		by: s,
		share: Math.round(o / a * 100)
	} : null;
}
function Ly(e, t) {
	for (let n = vy(t); n; n = vy(_y(n))) {
		let t = getComputedStyle(n);
		if (/auto|scroll/.test(t.overflowX + t.overflowY)) return null;
		if (/hidden|clip/.test(t.overflowX + t.overflowY) && n !== document.documentElement) {
			let t = n.getBoundingClientRect(), [r, i] = [t.left + n.clientLeft, t.top + n.clientTop];
			if (e.left < r - 1 || e.top < i - 1 || e.right > r + n.clientWidth + 1 || e.bottom > i + n.clientHeight + 1) return n;
		}
	}
	return null;
}
function Ry() {
	let e = innerWidth, t = Math.max(document.documentElement.scrollWidth, document.body.scrollWidth), n = (e) => {
		let t = 0;
		for (let n = _y(e); n; n = _y(n)) t++;
		return t;
	}, r = (t) => {
		for (let n = _y(t); n; n = _y(n)) {
			if (n === document.documentElement || n === document.body) return !1;
			let t = getComputedStyle(n);
			if (/auto|scroll/.test(t.overflowX) && n.getBoundingClientRect().width <= e + 1) return !0;
		}
		return !1;
	}, i = t > e + 1, a = [];
	return xy(document, (t) => {
		let o = getComputedStyle(t);
		if (o.display === "none" || o.display === "contents") return;
		let s = o.position === "fixed" || o.position === "sticky";
		if (!s && !i) return;
		let { left: c, right: l } = t.getBoundingClientRect();
		s ? (c < -1 && l > 1 || l > e + 1 && c < e - 1) && gy(t) && !yy(t) && !r(t) && a.push({
			el: hy(t),
			right: Math.round(l),
			left: Math.round(c),
			depth: n(t)
		}) : l > e + 1 && !r(t) && a.push({
			el: hy(t),
			right: Math.round(l),
			depth: n(t)
		});
	}), a.sort((e, t) => t.right - e.right || e.depth - t.depth), {
		fails: a.length > 0,
		scrollWidth: t,
		offenders: a.slice(0, 5).map(({ depth: e, ...t }) => t)
	};
}
async function zy() {
	let e = () => new Promise((e) => requestAnimationFrame(() => requestAnimationFrame(e))), t = (e = document) => {
		if (e.getAnimations().some((e) => e.playState === "running" && Number.isFinite(e.effect?.getComputedTiming().endTime))) return !0;
		for (let n of e.querySelectorAll("*")) if (n.shadowRoot && t(n.shadowRoot)) return !0;
		return !1;
	};
	await e();
	for (let n = 0; n < 90 && t(); n++) await e();
}
function By() {
	let e = [], t = [];
	xy(document, (e) => {
		if (!e.matches(ly) || e.localName === "m3e-slider-thumb") return !0;
		if (gy(e)) {
			let n = e.getBoundingClientRect(), r = e.shadowRoot?.querySelector(".touch")?.getBoundingClientRect(), i = Math.max(n.width, r?.width ?? 0), a = Math.max(n.height, r?.height ?? 0);
			t.push({
				el: py(e),
				node: e,
				w: Math.round(i * 10) / 10,
				h: Math.round(a * 10) / 10,
				area: i * a
			});
		}
		return !1;
	});
	let n = t.filter((e) => e.w < 47.95 || e.h < 47.95).map(({ el: e, w: t, h: n }) => ({
		el: e,
		w: t,
		h: n
	})), r = t.reduce((e, t) => !e || t.area > e.area ? t : e, null), i = t.filter((e) => e.node.hasAttribute("data-primary")), a = {
		marked: i.length,
		largest: r && {
			el: r.el,
			w: r.w,
			h: r.h
		}
	};
	i.length === 0 ? (a.status = "unresolved", e.push({
		check: "primary",
		reason: "no visible element has data-primary"
	})) : i.length > 1 ? (a.status = "fail", a.reason = "more than one visible element has data-primary") : (a.status = i[0].area >= r.area - .5 ? "pass" : "fail", a.status === "fail" && (a.reason = `${i[0].el} is smaller than ${r.el}`));
	let o = [...document.querySelectorAll(uy)].filter((e) => !e.parentElement?.closest("m3e-fab-menu") && gy(e)).length, s = [], c = [], l = [], u = [], d = {
		fails: [],
		passed: 0
	}, f = /* @__PURE__ */ new Set(), p = (...e) => {
		let t = e.join("|");
		return !f.has(t) && (f.add(t), !0);
	};
	Sy(document, (t) => {
		let n = _y(t);
		if (!n || n.closest?.("p-icon, m3e-icon")) return;
		let r = getComputedStyle(n), i = document.createRange();
		i.selectNodeContents(t);
		let a = i.getBoundingClientRect();
		if (a.width <= 0 || a.height <= 0 || r.visibility !== "visible" || yy(n)) return;
		let o = hy(vy(n) || n);
		if (!dy(r.fontFamily, parseFloat(r.fontSize)) && p("font", o) && s.push({
			el: o,
			font: r.fontFamily
		}), by(n)) return;
		let f = Oy(t, r);
		f.length && p("midword", o) && l.push({
			el: o,
			words: f.slice(0, 3)
		});
		let m = null;
		try {
			m = Iy(t, n);
		} catch (t) {
			p("overlap-error", o) && e.push({
				check: "overlaps",
				el: o,
				reason: String(t)
			});
		}
		m && p("overlap", o) && u.push({
			el: o,
			coveredBy: hy(m.by),
			share: m.share
		});
		let h = vy(n), g = /auto|scroll/.test(r.overflowX + r.overflowY);
		h && !g && getComputedStyle(h).display !== "inline" && (h.scrollWidth > h.clientWidth + 1 || h.scrollHeight > h.clientHeight + 1) && p("overflow", o) && c.push({
			el: o,
			scroll: [h.scrollWidth, h.scrollHeight],
			client: [h.clientWidth, h.clientHeight]
		});
		let ee = Ly(a, n);
		if (ee && ee !== h && p("overflow", o, "clip") && c.push({
			el: o,
			clippedBy: hy(ee)
		}), !p("contrast", o)) return;
		let te = Ey(n);
		if (!te.color) {
			e.push({
				check: "contrast",
				el: o,
				reason: te.reason
			});
			return;
		}
		let _ = sy(Ty(r.color), te.color), ne = cy(parseFloat(r.fontSize), parseInt(r.fontWeight, 10)), v = oy(_, te.color);
		v < ne ? d.fails.push({
			el: o,
			ratio: Math.round(v * 100) / 100,
			need: ne
		}) : d.passed++;
	});
	let m = Ry(), h = e.length === 0, g = !m.fails && n.length === 0 && s.length === 0 && c.length === 0 && l.length === 0 && u.length === 0 && a.status !== "fail" && o <= 1 && d.fails.length === 0;
	return JSON.parse(JSON.stringify({
		pass: g,
		complete: h,
		viewport: [innerWidth, innerHeight],
		touchTargets: {
			checked: t.length,
			under48: n
		},
		fonts: { offenders: s },
		overflow: c,
		midWordBreaks: l,
		overlaps: u,
		pageOverflow: m,
		primary: a,
		fabs: {
			count: o,
			ok: o <= 1
		},
		contrast: d,
		unresolved: e
	}));
}
window.runtimeChecks = By, window.runtimeSettle = zy;
//#endregion
