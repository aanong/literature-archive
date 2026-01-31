import socket
import threading
import time
import struct
import json

HOST = '127.0.0.1'
PORT = 9090
CLIENT_COUNT = 100
MSG_COUNT_PER_CLIENT = 10

def encode_varint(value):
    """Encodes an integer as a varint."""
    buffer = bytearray()
    while True:
        if (value & ~0x7F) == 0:
            buffer.append(value)
            break
        else:
            buffer.append((value & 0x7F) | 0x80)
            value >>= 7
    return buffer

def encode_auth_payload(token):
    # message AuthPayload { string token = 1; }
    # Tag 1 (0x0A) -> Length -> Bytes
    token_bytes = token.encode('utf-8')
    payload = bytearray()
    payload.append(0x0A) # Field 1, Type String (2) -> (1<<3|2) = 10
    payload.extend(encode_varint(len(token_bytes)))
    payload.extend(token_bytes)
    return payload

def encode_chat_payload(timestamp, sender_id, target_id, content):
    # message ChatPayload { int64 timestamp = 1; int64 senderId = 2; int64 targetId = 3; string content = 4; }
    payload = bytearray()
    
    # 1. timestamp (int64/varint) -> Tag 1 (0x08)
    payload.append(0x08)
    payload.extend(encode_varint(timestamp))
    
    # 2. senderId (int64/varint) -> Tag 2 (0x10)
    payload.append(0x10)
    payload.extend(encode_varint(sender_id))
    
    # 3. targetId (int64/varint) -> Tag 3 (0x18)
    payload.append(0x18)
    payload.extend(encode_varint(target_id))
    
    # 4. content (string) -> Tag 4 (0x22)
    content_bytes = content.encode('utf-8')
    payload.append(0x22)
    payload.extend(encode_varint(len(content_bytes)))
    payload.extend(content_bytes)
    
    return payload

def encode_message(cmd, body_bytes):
    header_magic = 0xCAFE
    header_version = 1
    header_serial = 1
    header_cmd_type = cmd
    header_req_id = int(time.time() * 1000)
    
    length = len(body_bytes)
    
    # Magic(2) + Version(1) + Serial(1) + Cmd(1) + ReqId(8) + Length(4) = 17 bytes
    header = struct.pack('!HBBBQI', header_magic, header_version, header_serial, header_cmd_type, header_req_id, length)
    return header + body_bytes

def mock_client(client_id):
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect((HOST, PORT))
        
        # 1. Auth
        auth_body = encode_auth_payload(f'user:{client_id}')
        s.sendall(encode_message(1, auth_body)) # Cmd 1 = Auth
        time.sleep(0.1)
        
        # 2. Send Messages
        for i in range(MSG_COUNT_PER_CLIENT):
            timestamp = int(time.time() * 1000)
            target_id = client_id + 1 if client_id % 2 != 0 else client_id - 1
            if target_id < 1 or target_id > CLIENT_COUNT:
                target_id = 1
                
            chat_body = encode_chat_payload(timestamp, client_id, target_id, f'Hello from {client_id} msg {i}')
            s.sendall(encode_message(2, chat_body)) # Cmd 2 = Single Chat, 3 = Message
            time.sleep(0.1) 
            
        s.close()
        # print(f"Client {client_id} finished")
    except Exception as e:
        print(f"Client {client_id} error: {e}")

if __name__ == "__main__":
    print(f"Starting benchmark with {CLIENT_COUNT} clients...")
    threads = []
    start_time = time.time()
    
    for i in range(1, CLIENT_COUNT + 1):
        t = threading.Thread(target=mock_client, args=(i, ))
        threads.append(t)
        t.start()
        
    for t in threads:
        t.join()
        
    print(f"Benchmark finished in {time.time() - start_time:.2f} seconds")
