import { createServer } from 'node:http';
import { lstatSync, readFileSync } from 'node:fs';
import { extname, join, resolve, sep } from 'node:path';
import { fileURLToPath } from 'node:url';
import { GalleryStore, GalleryError } from './store.mjs';

const PUBLIC = fileURLToPath(new URL('./public/', import.meta.url));
const TYPES = {
  '.html': 'text/html; charset=utf-8', '.js': 'text/javascript; charset=utf-8', '.mjs': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8', '.svg': 'image/svg+xml', '.woff2': 'font/woff2', '.png': 'image/png',
  '.txt': 'text/plain; charset=utf-8', '.json': 'application/json',
};
// Serve a regular file under public/ only: no traversal, no symlinks, no directories. A path ending in / serves its index.html.
function asset(path) {
  let relative;
  try { relative = decodeURIComponent(path.endsWith('/') ? `${path}index.html` : path); } catch { return null; }
  if (relative.includes('\0') || relative.includes('\\')) return null;
  const file = resolve(PUBLIC, `.${relative}`);
  if (!file.startsWith(PUBLIC)) return null;
  try {
    // Reject a symlink at any segment below public/, not only the last one.
    let current = PUBLIC;
    for (const segment of file.slice(PUBLIC.length).split(sep)) {
      current = join(current, segment);
      if (lstatSync(current).isSymbolicLink()) return null;
    }
    if (!lstatSync(file).isFile()) return null;
    return [readFileSync(file), TYPES[extname(file).toLowerCase()] || 'application/octet-stream'];
  } catch { return null; }
}
function send(response, status, body, type = 'application/json') {
  response.writeHead(status, {
    'Content-Type': type, 'Cache-Control': 'no-store', 'X-Content-Type-Options': 'nosniff',
    'Content-Security-Policy': "default-src 'self'; img-src 'self'; style-src 'self'; script-src 'self'; connect-src 'self'; frame-ancestors 'none'; base-uri 'none'; form-action 'self'",
    'Referrer-Policy': 'no-referrer',
  });
  response.end(Buffer.isBuffer(body) || typeof body === 'string' ? body : JSON.stringify(body));
}
async function body(request) {
  let length = 0;
  const chunks = [];
  for await (const chunk of request) {
    length += chunk.length;
    if (length > 20000) throw new GalleryError('Request is too large', 413);
    chunks.push(chunk);
  }
  try {
    const value = JSON.parse(Buffer.concat(chunks).toString());
    if (!value || typeof value !== 'object' || Array.isArray(value)) throw new Error();
    return value;
  } catch { throw new GalleryError('Expected a JSON object'); }
}
export async function startServer(root, port = 0) {
  const store = new GalleryStore(root);
  store.read();
  const server = createServer(async (request, response) => {
    try {
      const authority = `127.0.0.1:${server.address().port}`;
      if (request.headers.host !== authority || (request.headers.origin && request.headers.origin !== `http://${authority}`) || request.headers['sec-fetch-site'] === 'cross-site') {
        throw new GalleryError('Only same-origin localhost requests are allowed', 403);
      }
      const path = new URL(request.url, `http://${authority}`).pathname;
      if (request.method === 'GET') {
        if (path === '/api/exploration') return send(response, 200, store.read());
        const image = /^\/images\/(\d{3,})\/([a-z][a-z0-9-]{0,39})\/(phone|tablet|desktop|foldable)$/.exec(path);
        if (image) return send(response, 200, store.image(`${image[1]}/${image[2]}`, image[3]), 'image/png');
        if (!path.startsWith('/api/')) {
          const found = asset(path);
          if (found) return send(response, 200, found[0], found[1]);
        }
      }
      if ((request.method === 'PUT' && path === '/api/feedback') || (request.method === 'POST' && ['/api/approve', '/api/refine'].includes(path))) {
        if (request.headers['content-type'] !== 'application/json') throw new GalleryError('Use application/json', 403);
        const value = await body(request);
        return send(response, 200, path === '/api/feedback' ? store.save(value) : path === '/api/refine' ? store.refine(value) : store.approve(value));
      }
      throw new GalleryError('Not found', 404);
    } catch (error) {
      send(response, error instanceof GalleryError ? error.status : 500, { error: error instanceof GalleryError ? error.message : 'Could not read or save exploration. Check its files and permissions.' });
    }
  });
  server.requestTimeout = 10000;
  server.headersTimeout = 10000;
  await new Promise((resolve, reject) => {
    server.once('error', reject);
    server.listen(port, '127.0.0.1', () => { server.off('error', reject); resolve(); });
  });
  return server;
}
