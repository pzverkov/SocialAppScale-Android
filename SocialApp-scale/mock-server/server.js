'use strict';

const http = require('node:http');
const { readFileSync } = require('node:fs');
const { join } = require('node:path');

const HOST = process.env.HOST || '0.0.0.0';
const PORT = Number(process.env.PORT) || 3000;
// Per-request delay so the client exercises its loading and error states.
// Set LATENCY_MS=0 to disable.
const LATENCY_MS = Number(process.env.LATENCY_MS ?? 250);

const DB_PATH = join(__dirname, '..', 'mock-api', 'db.json');

function loadItems() {
    try {
        const parsed = JSON.parse(readFileSync(DB_PATH, 'utf8'));
        return Array.isArray(parsed.items) ? parsed.items : [];
    } catch (err) {
        console.error(`[mock] cannot read ${DB_PATH}: ${err.message}`);
        process.exit(1);
    }
}

const items = loadItems();
const itemById = new Map(items.map((item) => [item.id, item]));

function searchItems(query) {
    const needle = (query.get('q') || '').trim().toLowerCase();
    if (!needle) return items;
    return items.filter((item) =>
        [item.title, item.description, item.location].some((field) =>
            String(field).toLowerCase().includes(needle),
        ),
    );
}

// [method, pattern, handler(captures, query) -> [status, body]]
const routes = [
    ['GET', /^\/items\/?$/, (_captures, query) => [200, searchItems(query)]],
    ['GET', /^\/items\/(\d+)\/?$/, ([id]) => {
        const item = itemById.get(Number(id));
        return item ? [200, item] : [404, { error: 'item not found', id: Number(id) }];
    }],
    ['GET', /^\/(?:health\/?)?$/, () => [200, { status: 'ok', items: items.length }]],
];

function route(method, pathname, query) {
    let pathMatchedWrongMethod = false;
    for (const [routeMethod, pattern, handler] of routes) {
        const match = pattern.exec(pathname);
        if (!match) continue;
        if (routeMethod !== method) {
            pathMatchedWrongMethod = true;
            continue;
        }
        return handler(match.slice(1), query);
    }
    if (pathMatchedWrongMethod) return [405, { error: 'method not allowed' }];
    return [404, { error: 'not found', path: pathname }];
}

const server = http.createServer((req, res) => {
    const startedAt = process.hrtime.bigint();
    const { pathname, searchParams } = new URL(req.url, `http://${req.headers.host}`);

    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
    res.setHeader('Content-Type', 'application/json; charset=utf-8');

    if (req.method === 'OPTIONS') {
        res.writeHead(204);
        res.end();
        return;
    }

    const [status, body] = route(req.method, pathname, searchParams);

    const respond = () => {
        res.writeHead(status);
        res.end(JSON.stringify(body));
        const ms = Number(process.hrtime.bigint() - startedAt) / 1e6;
        console.log(`[mock] ${req.method} ${pathname} ${status} ${ms.toFixed(0)}ms`);
    };

    if (LATENCY_MS > 0) setTimeout(respond, LATENCY_MS);
    else respond();
});

server.listen(PORT, HOST, () => {
    console.log(`[mock] serving ${items.length} items on http://${HOST}:${PORT}`);
    console.log(`[mock] android emulator base url: http://10.0.2.2:${PORT}`);
});

function shutdown(signal) {
    console.log(`[mock] ${signal} received, stopping`);
    server.close(() => process.exit(0));
}

process.on('SIGINT', () => shutdown('SIGINT'));
process.on('SIGTERM', () => shutdown('SIGTERM'));
