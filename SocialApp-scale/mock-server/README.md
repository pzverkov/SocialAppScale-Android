# Mock Server

A simple Node.js mock server for the SocialApp challenge.

## Quick Start

```bash
# Navigate to mock-server directory
cd mock-server

# Start the server (no npm install required - uses only Node.js built-ins)
node server.js
```

You should see:

```
🚀 ================================
   SocialApp Mock Server
   ================================

   Local:    http://localhost:3000
   Network:  http://0.0.0.0:3000

   For Android Emulator use:
   http://10.0.2.2:3000/items
```

## Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Server info and available endpoints |
| GET | `/items` | Get all items (12 items) |
| GET | `/items/:id` | Get a specific item by ID |
| GET | `/health` | Health check |

## Usage with Android

### For Emulator
The Android emulator uses a special IP to access the host machine:
```
http://10.0.2.2:3000/items
```

This is already configured in `NetworkHelper.kt`.

### For Physical Device
1. Find your computer's IP address (e.g., `192.168.1.100`)
2. Update `BASE_URL` in `NetworkHelper.kt`:
   ```kotlin
   private const val BASE_URL = "http://192.168.1.100:3000"
   ```
3. Ensure your phone is on the same WiFi network

## Sample Response

```json
[
  {
    "id": 1,
    "title": "Vintage Camera Sony Alpha",
    "description": "A beautiful vintage camera...",
    "price": 150.00,
    "imageUrl": "https://picsum.photos/seed/camera1/400/400",
    "location": "New York"
  }
]
```

## Simulated Latency

The server adds a random delay of 300-800ms to simulate real network conditions.

## Troubleshooting

### "Connection refused" on emulator
- Make sure the server is running (`node server.js`)
- Verify you're using `10.0.2.2` not `localhost`
- Check that port 3000 is not blocked by firewall

### Server not starting
- Make sure Node.js is installed: `node --version`
- Check if port 3000 is in use: `lsof -i :3000`
