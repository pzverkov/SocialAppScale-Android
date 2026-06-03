const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;

// Load mock data
const dbPath = path.join(__dirname, '..', 'mock-api', 'db.json');
let mockData = { items: [] };

try {
    const rawData = fs.readFileSync(dbPath, 'utf8');
    mockData = JSON.parse(rawData);
    console.log(`✅ Loaded ${mockData.items.length} items from db.json`);
} catch (error) {
    console.error('❌ Error loading db.json:', error.message);
    console.log('Using empty items array');
}

// Simple router
const routes = {
    'GET /items': () => ({
        status: 200,
        data: mockData.items
    }),
    'GET /items/': (id) => {
        const item = mockData.items.find(i => i.id === parseInt(id));
        if (item) {
            return { status: 200, data: item };
        }
        return { status: 404, data: { error: 'Item not found' } };
    }
};

// Create server
const server = http.createServer((req, res) => {
    // Enable CORS
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
    res.setHeader('Content-Type', 'application/json');

    // Handle preflight
    if (req.method === 'OPTIONS') {
        res.writeHead(204);
        res.end();
        return;
    }

    const url = req.url || '/';
    const method = req.method || 'GET';
    
    console.log(`📥 ${method} ${url}`);

    // Add artificial delay to simulate network latency (300-800ms)
    const delay = Math.floor(Math.random() * 500) + 300;
    
    setTimeout(() => {
        let response = { status: 404, data: { error: 'Not found' } };

        // Match routes
        if (url === '/items' || url === '/items/') {
            response = routes['GET /items']();
        } else if (url.startsWith('/items/')) {
            const id = url.split('/items/')[1];
            response = routes['GET /items/'](id);
        } else if (url === '/' || url === '/health') {
            response = { 
                status: 200, 
                data: { 
                    status: 'ok', 
                    message: 'SocialApp Mock Server',
                    endpoints: [
                        'GET /items - List all items',
                        'GET /items/:id - Get item by ID',
                        'GET /health - Health check'
                    ]
                } 
            };
        }

        res.writeHead(response.status);
        res.end(JSON.stringify(response.data, null, 2));
        console.log(`📤 ${response.status} (${delay}ms delay)`);
    }, delay);
});

server.listen(PORT, '0.0.0.0', () => {
    console.log('');
    console.log('🚀 ================================');
    console.log('   SocialApp Mock Server');
    console.log('   ================================');
    console.log('');
    console.log('   For Android Emulator use:');
    console.log(`   http://10.0.2.2:${PORT}/items`);
    console.log('');
    console.log('   For Physical Device use:');
    console.log(`   http://<your-local-ip>:${PORT}/items`);
    console.log('');
    console.log('   Endpoints:');
    console.log('   GET /items     - List all items');
    console.log('   GET /items/:id - Get item by ID');
    console.log('   GET /health    - Health check');
    console.log('');
    console.log('   Press Ctrl+C to stop');
    console.log('');
});

// Graceful shutdown
process.on('SIGINT', () => {
    console.log('\n👋 Shutting down server...');
    server.close(() => {
        console.log('✅ Server stopped');
        process.exit(0);
    });
});
