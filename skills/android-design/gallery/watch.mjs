import { setTimeout as delay } from 'node:timers/promises';
import { GalleryStore } from './store.mjs';

const WAITING_GRACE_MS = 60_000;
const WORKING_BUDGET_MS = 10 * 60_000;

// Blocks until a refinement request is pending, the exploration is approved, or
// `timeout` seconds pass. Approval wins over pending requests. The session status
// file tells the gallery whether anyone is listening; it expires by itself, so a
// timeout leaves it in place to cover the gap before the agent reruns wait.
export async function wait(root, { after = -1, timeout = 25, interval = 1000, now = Date.now, sleep = delay } = {}) {
  const store = new GalleryStore(root);
  const start = now();
  store.writeSession('waiting', new Date(start + timeout * 1000 + WAITING_GRACE_MS));
  for (;;) {
    const { feedback, pending } = store.read();
    if (feedback.approved) {
      store.clearSession();
      return { status: 'approved', approved: feedback.approved, revision: feedback.revision };
    }
    if (pending.length > 0 && feedback.revision > after) {
      store.writeSession('working', new Date(now() + WORKING_BUDGET_MS));
      const [request, ...superseded] = pending;
      return { status: 'request', request, superseded, revision: feedback.revision };
    }
    if (now() - start >= timeout * 1000) return { status: 'timeout', revision: feedback.revision };
    await sleep(interval);
  }
}
