import { createServer } from 'node:http';
import { readFileSync } from 'node:fs';
import { GalleryStore, GalleryError } from './store.mjs';

const PUBLIC = new URL('./public/', import.meta.url);
const ASSETS = new Map([
  ['/', ['index.html', 'text/html; charset=utf-8']],
  ['/app.mjs', ['app.mjs', 'text/javascript; charset=utf-8']],
  ['/saves.mjs', ['saves.mjs', 'text/javascript; charset=utf-8']],
  ['/select-arrows.svg', ['select-arrows.svg', 'image/svg+xml']],
  ['/style.css', ['style.css', 'text/css; charset=utf-8']],
  ['/fonts/google-sans-latin.woff2', ['fonts/google-sans-latin.woff2', 'font/woff2']],
]);
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
        if (ASSETS.has(path)) {
          const [file, type] = ASSETS.get(path);
          return send(response, 200, readFileSync(new URL(file, PUBLIC)), type);
        }
        if (path === '/api/exploration') return send(response, 200, store.read());
        const image = /^\/images\/(\d{3,})\/([a-z][a-z0-9-]{0,39})\/(phone|tablet|desktop|foldable)$/.exec(path);
        if (image) return send(response, 200, store.image(`${image[1]}/${image[2]}`, image[3]), 'image/png');
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
