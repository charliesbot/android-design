// Serial writes preserve edits made while a previous request is in flight.
export class FeedbackSaves {
  constructor(revision, onChange, send = body => fetch('/api/feedback', { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) })) {
    this.revision = revision;
    this.onChange = onChange;
    this.send = send;
    this.pending = new Map();
    this.error = null;
    this.running = null;
  }
  enqueue(roundId, choice, note) {
    this.pending.set(roundId, { roundId, choice, note });
    this.onChange();
    this.drain();
  }
  drain() {
    if (this.running || this.error || !this.pending.size) return;
    // Begin on a microtask so running is set even when send throws synchronously.
    this.running = Promise.resolve().then(async () => {
      try {
        while (this.pending.size) {
          const [key, entry] = this.pending.entries().next().value;
          const response = await this.send({ ...entry, revision: this.revision });
          const data = await response.json();
          if (!response.ok) {
            this.error = { message: data.error || 'Feedback was not saved.', conflict: response.status === 409 };
            break;
          }
          this.revision = data.revision;
          if (this.pending.get(key) === entry) this.pending.delete(key);
        }
      } catch {
        this.error = { message: 'Cannot reach the gallery. Keep this tab open and retry.', conflict: false };
      } finally {
        this.running = null;
        this.onChange();
      }
    });
  }
  async flush() {
    this.drain();
    if (this.running) await this.running;
    return !this.error && this.pending.size === 0;
  }
  async retry() {
    if (this.error?.conflict) return false;
    this.error = null;
    return this.flush();
  }
}
