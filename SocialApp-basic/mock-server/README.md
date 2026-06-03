# Mock API server

A dependency-free Node.js server that serves item data to the SocialApp client over HTTP. It reads `../mock-api/db.json` and exposes a small read-only REST surface using only Node built-ins, so there is nothing to install.

## Run

```
node server.js
```

From the Android emulator the client reaches it at `http://10.0.2.2:3000`, which is set as `BASE_URL` in `app/src/main/java/com/pzverkov/socialapp/core/network/NetworkModule.kt`.

### Configuration

All optional, via environment variables:

| Variable | Default | Purpose |
| --- | --- | --- |
| `PORT` | `3000` | Listen port. |
| `HOST` | `0.0.0.0` | Bind address. `0.0.0.0` lets an emulator or a device on the LAN reach it. |
| `LATENCY_MS` | `250` | Per-request delay to exercise loading and error states. Set `0` to disable. |

## Endpoints

| Method | Path | Description |
| --- | --- | --- |
| GET | `/items` | All items. Optional `?q=` filters by title, description, or location. |
| GET | `/items/{id}` | One item by numeric id, otherwise `404`. |
| GET | `/health` | Liveness check with the loaded item count. |

Responses are JSON. CORS is open and an `OPTIONS` preflight returns `204`. Unknown paths return `404`; a known path with the wrong method returns `405`.

### Examples

```
curl http://localhost:3000/items
curl "http://localhost:3000/items?q=camera"
curl http://localhost:3000/items/1
```

```json
{
  "id": 1,
  "title": "...",
  "description": "...",
  "price": 150.0,
  "imageUrl": "https://...",
  "location": "..."
}
```

## Physical device

The bind address is already `0.0.0.0`. Point the client at your machine's LAN IP by changing `BASE_URL` in `NetworkModule.kt` (for example `http://192.168.1.100:3000`) and keep the phone on the same network.

## Troubleshooting

- Connection refused on the emulator: use `10.0.2.2`, not `localhost`, and confirm the server is running.
- Port already in use: inspect with `lsof -i :3000`, or start on another port with `PORT=3001 node server.js`.
