const WebSocket = require('ws');
const net = require('net');

const WS_PORT = 8081;
const TCP_HOST = 'localhost';
const TCP_PORT = 9090; // Default Netty port

const wss = new WebSocket.Server({ port: WS_PORT });

console.log(`WebSocket Proxy running on ws://localhost:${WS_PORT}`);
console.log(`Forwarding to TCP server at ${TCP_HOST}:${TCP_PORT}`);

wss.on('connection', (ws) => {
    console.log('New WebSocket connection');

    const tcpClient = new net.Socket();

    tcpClient.connect(TCP_PORT, TCP_HOST, () => {
        console.log('Connected to TCP server');
    });

    // Forward WebSocket messages to TCP
    ws.on('message', (message) => {
        // message is a Buffer
        tcpClient.write(message);
    });

    // Forward TCP data to WebSocket
    tcpClient.on('data', (data) => {
        if (ws.readyState === WebSocket.OPEN) {
            ws.send(data);
        }
    });

    tcpClient.on('close', () => {
        console.log('TCP connection closed');
        ws.close();
    });

    tcpClient.on('error', (err) => {
        console.error('TCP error:', err.message);
        ws.close();
    });

    ws.on('close', () => {
        console.log('WebSocket connection closed');
        tcpClient.end();
    });

    ws.on('error', (err) => {
        console.error('WebSocket error:', err.message);
        tcpClient.end();
    });
});
