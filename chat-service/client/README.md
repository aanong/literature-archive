# Chat Service Client

This is a simple HTML/JS client for the Literature Archive Chat Service.

## Architecture

Because the Chat Service uses a raw TCP custom binary protocol (Netty), and browsers only support WebSocket, a **Node.js Proxy** is used to bridge the connection.

Browser (WebSocket) <-> Proxy (Node.js) <-> Chat Service (TCP)

## Prerequisites

- Node.js installed
- The Chat Service backend running on port 9090 (TCP)

## Setup

1.  Navigate to this directory:
    ```bash
    cd chat-service/client
    ```

2.  Install dependencies:
    ```bash
    npm install
    ```

## Usage

1.  **Start the Proxy:**
    ```bash
    npm start
    ```
    This will listen on `ws://localhost:8081` and forward to `localhost:9090`.

2.  **Open the Client:**
    Open `index.html` in your browser.

3.  **Login:**
    - Enter a User ID (e.g., `1001`).
    - Click "Connect".
    - The client will authenticate using the token format `user:{id}`.

4.  **Chat:**
    - Enter a Target User ID.
    - Type a message and send.
