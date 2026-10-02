// Keeps the latest snapshot per case until the server acknowledges it.
export class PreferenceSaves {
  constructor(onChange) {
    this.onChange = onChange;
    this.pending = new Map();
    this.timers = new Map();
    this.sending = false;
    this.failed = false;
  }

  enqueue(name, pick) {
    const entry = { body: JSON.stringify({ case: name, ...pick, at: new Date().toISOString() }), ready: false };
    this.pending.set(name, entry);
    clearTimeout(this.timers.get(name));
    this.timers.set(name, setTimeout(() => {
      this.timers.delete(name);
      entry.ready = true;
      this.drain();
    }, 250));
    this.onChange();
  }

  retry() {
    this.failed = false;
    for (const timer of this.timers.values()) clearTimeout(timer);
    this.timers.clear();
    for (const entry of this.pending.values()) entry.ready = true;
    this.drain();
  }

  async drain() {
    if (this.sending || this.failed) return;
    this.sending = true;
    this.onChange();
    try {
      while (true) {
        const next = [...this.pending].find(([, entry]) => entry.ready);
        if (!next) break;
        const [name, entry] = next;
        const response = await fetch('/api/select', {
          method: 'POST', headers: { 'Content-Type': 'application/json' }, body: entry.body,
        });
        if (!response.ok) throw new Error(`Save failed: HTTP ${response.status}`);
        // An edit made during this request must still be sent, not acknowledged by it.
        if (this.pending.get(name) === entry) this.pending.delete(name);
        this.onChange();
      }
    } catch {
      this.failed = true;
    } finally {
      this.sending = false;
      this.onChange();
    }
  }
}
