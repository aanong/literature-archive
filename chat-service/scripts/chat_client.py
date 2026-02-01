#!/usr/bin/env python3
"""
Chat Service 高并发测试客户端
支持 10000+ 并发连接，单聊、群聊、心跳保活、离线消息测试

协议格式:
  Magic(2) + Version(1) + Serial(1) + Cmd(1) + ReqId(8) + Length(4) + Protobuf Body

CmdType:
  UNKNOWN = 0, HEARTBEAT = 1, AUTH = 2, SINGLE_CHAT = 3, GROUP_CHAT = 4, ACK = 5, ERROR = 6
"""

import asyncio
import argparse
import struct
import time
import random
import statistics
import signal
import sys
from dataclasses import dataclass, field
from typing import Dict, List, Optional, Callable
from collections import defaultdict
from concurrent.futures import ThreadPoolExecutor
import threading


# ============== 协议常量 ==============
MAGIC = 0xCAFE
VERSION = 1
SERIAL = 1

class CmdType:
    UNKNOWN = 0
    HEARTBEAT = 1
    AUTH = 2
    SINGLE_CHAT = 3
    GROUP_CHAT = 4
    ACK = 5
    ERROR = 6


# ============== Protobuf 编码 ==============
def encode_varint(value: int) -> bytes:
    """编码 varint"""
    buffer = bytearray()
    while True:
        if (value & ~0x7F) == 0:
            buffer.append(value)
            break
        else:
            buffer.append((value & 0x7F) | 0x80)
            value >>= 7
    return bytes(buffer)


def encode_string_field(field_num: int, value: str) -> bytes:
    """编码字符串字段"""
    if not value:
        return b''
    tag = (field_num << 3) | 2  # wire type 2 for length-delimited
    data = value.encode('utf-8')
    return bytes([tag]) + encode_varint(len(data)) + data


def encode_varint_field(field_num: int, value: int) -> bytes:
    """编码 varint 字段"""
    if value == 0:
        return b''
    tag = (field_num << 3) | 0  # wire type 0 for varint
    return bytes([tag]) + encode_varint(value)


def encode_auth_payload(token: str, device_id: str = "") -> bytes:
    """编码 AuthPayload: token=1, device_id=2"""
    payload = bytearray()
    payload.extend(encode_string_field(1, token))
    payload.extend(encode_string_field(2, device_id))
    return bytes(payload)


def encode_chat_payload(
    cmd: int,
    session_id: int = 0,
    sender_id: int = 0,
    target_id: int = 0,
    content: str = "",
    content_type: str = "text",
    timestamp: int = 0,
    extra: str = ""
) -> bytes:
    """编码 ChatPayload"""
    payload = bytearray()
    payload.extend(encode_varint_field(1, cmd))
    payload.extend(encode_varint_field(2, session_id))
    payload.extend(encode_varint_field(3, sender_id))
    payload.extend(encode_varint_field(4, target_id))
    payload.extend(encode_string_field(5, content))
    payload.extend(encode_string_field(6, content_type))
    payload.extend(encode_varint_field(7, timestamp or int(time.time() * 1000)))
    payload.extend(encode_string_field(8, extra))
    return bytes(payload)


def encode_message(cmd: int, body: bytes, req_id: int = 0) -> bytes:
    """编码完整消息帧"""
    if req_id == 0:
        req_id = int(time.time() * 1000000) & 0xFFFFFFFFFFFFFFFF
    header = struct.pack('!HBBBQI', MAGIC, VERSION, SERIAL, cmd, req_id, len(body))
    return header + body


def decode_header(data: bytes) -> Optional[dict]:
    """解码消息头 (17 bytes)"""
    if len(data) < 17:
        return None
    magic, version, serial, cmd, req_id, length = struct.unpack('!HBBBQI', data[:17])
    if magic != MAGIC:
        return None
    return {
        'magic': magic,
        'version': version,
        'serial': serial,
        'cmd': cmd,
        'req_id': req_id,
        'length': length
    }


# ============== 统计收集器 ==============
@dataclass
class Stats:
    """测试统计数据"""
    connections_attempted: int = 0
    connections_success: int = 0
    connections_failed: int = 0
    auth_success: int = 0
    auth_failed: int = 0
    messages_sent: int = 0
    messages_received: int = 0
    heartbeats_sent: int = 0
    heartbeats_received: int = 0
    errors: int = 0
    latencies: List[float] = field(default_factory=list)
    start_time: float = 0
    end_time: float = 0
    
    _lock: threading.Lock = field(default_factory=threading.Lock)
    
    def add_latency(self, latency: float):
        with self._lock:
            self.latencies.append(latency)
    
    def increment(self, field_name: str, value: int = 1):
        with self._lock:
            setattr(self, field_name, getattr(self, field_name) + value)
    
    def report(self) -> str:
        duration = (self.end_time or time.time()) - self.start_time
        
        lines = [
            "\n" + "=" * 60,
            "                 📊 压力测试报告",
            "=" * 60,
            f"⏱️  总耗时: {duration:.2f} 秒",
            "",
            "📡 连接统计:",
            f"   尝试连接: {self.connections_attempted}",
            f"   成功连接: {self.connections_success}",
            f"   失败连接: {self.connections_failed}",
            f"   连接成功率: {self.connections_success / max(self.connections_attempted, 1) * 100:.2f}%",
            "",
            "🔐 认证统计:",
            f"   认证成功: {self.auth_success}",
            f"   认证失败: {self.auth_failed}",
            "",
            "💬 消息统计:",
            f"   发送消息: {self.messages_sent}",
            f"   接收消息: {self.messages_received}",
            f"   发送 QPS: {self.messages_sent / max(duration, 1):.2f}",
            f"   接收 QPS: {self.messages_received / max(duration, 1):.2f}",
            "",
            "💓 心跳统计:",
            f"   发送心跳: {self.heartbeats_sent}",
            f"   接收心跳: {self.heartbeats_received}",
            "",
        ]
        
        if self.latencies:
            sorted_latencies = sorted(self.latencies)
            lines.extend([
                "⚡ 延迟统计 (ms):",
                f"   最小值: {min(self.latencies):.2f}",
                f"   最大值: {max(self.latencies):.2f}",
                f"   平均值: {statistics.mean(self.latencies):.2f}",
                f"   中位数: {statistics.median(self.latencies):.2f}",
                f"   P95: {sorted_latencies[int(len(sorted_latencies) * 0.95)]:.2f}",
                f"   P99: {sorted_latencies[int(len(sorted_latencies) * 0.99)]:.2f}",
            ])
        
        lines.extend([
            "",
            f"❌ 错误数: {self.errors}",
            "=" * 60,
        ])
        
        return "\n".join(lines)


# ============== 异步 Chat 客户端 ==============
class ChatClient:
    """异步 Chat 客户端"""
    
    def __init__(
        self,
        client_id: int,
        host: str,
        port: int,
        stats: Stats,
        token: Optional[str] = None
    ):
        self.client_id = client_id
        self.host = host
        self.port = port
        self.stats = stats
        self.token = token or f"user:{client_id}"
        
        self.reader: Optional[asyncio.StreamReader] = None
        self.writer: Optional[asyncio.StreamWriter] = None
        self.connected = False
        self.authenticated = False
        self.running = False
        
        self.pending_requests: Dict[int, float] = {}  # req_id -> send_time
        self.heartbeat_task: Optional[asyncio.Task] = None
        self.receive_task: Optional[asyncio.Task] = None
    
    async def connect(self) -> bool:
        """建立 TCP 连接"""
        self.stats.increment('connections_attempted')
        try:
            self.reader, self.writer = await asyncio.wait_for(
                asyncio.open_connection(self.host, self.port),
                timeout=10.0
            )
            self.connected = True
            self.stats.increment('connections_success')
            return True
        except Exception as e:
            self.stats.increment('connections_failed')
            self.stats.increment('errors')
            return False
    
    async def authenticate(self) -> bool:
        """发送认证请求"""
        if not self.connected or not self.writer or not self.reader:
            return False
        
        try:
            body = encode_auth_payload(self.token, f"device_{self.client_id}")
            msg = encode_message(CmdType.AUTH, body)
            self.writer.write(msg)
            await self.writer.drain()
            
            header_data = await asyncio.wait_for(
                self.reader.read(17),
                timeout=5.0
            )
            header = decode_header(header_data)
            if header and header['length'] > 0:
                await self.reader.read(header['length'])
            
            self.authenticated = True
            self.stats.increment('auth_success')
            return True
        except Exception as e:
            self.stats.increment('auth_failed')
            self.stats.increment('errors')
            return False
    
    async def send_single_chat(self, target_id: int, content: str) -> int:
        """发送单聊消息"""
        if not self.authenticated or not self.writer:
            return 0
        
        req_id = int(time.time() * 1000000) & 0xFFFFFFFFFFFFFFFF
        body = encode_chat_payload(
            cmd=CmdType.SINGLE_CHAT,
            sender_id=self.client_id,
            target_id=target_id,
            content=content,
            timestamp=int(time.time() * 1000)
        )
        msg = encode_message(CmdType.SINGLE_CHAT, body, req_id)
        
        try:
            send_time = time.time()
            self.pending_requests[req_id] = send_time
            self.writer.write(msg)
            await self.writer.drain()
            self.stats.increment('messages_sent')
            return req_id
        except Exception:
            self.stats.increment('errors')
            return 0
    
    async def send_group_chat(self, session_id: int, content: str) -> int:
        """发送群聊消息"""
        if not self.authenticated or not self.writer:
            return 0
        
        req_id = int(time.time() * 1000000) & 0xFFFFFFFFFFFFFFFF
        body = encode_chat_payload(
            cmd=CmdType.GROUP_CHAT,
            session_id=session_id,
            sender_id=self.client_id,
            content=content,
            timestamp=int(time.time() * 1000)
        )
        msg = encode_message(CmdType.GROUP_CHAT, body, req_id)
        
        try:
            send_time = time.time()
            self.pending_requests[req_id] = send_time
            self.writer.write(msg)
            await self.writer.drain()
            self.stats.increment('messages_sent')
            return req_id
        except Exception:
            self.stats.increment('errors')
            return 0
    
    async def send_heartbeat(self):
        """发送心跳"""
        if not self.connected or not self.writer:
            return
        
        try:
            msg = encode_message(CmdType.HEARTBEAT, b'')
            self.writer.write(msg)
            await self.writer.drain()
            self.stats.increment('heartbeats_sent')
        except Exception:
            self.stats.increment('errors')
    
    async def _heartbeat_loop(self, interval: float = 30.0):
        """心跳循环"""
        while self.running and self.connected:
            await asyncio.sleep(interval)
            await self.send_heartbeat()
    
    async def _receive_loop(self):
        """接收消息循环"""
        while self.running and self.connected and self.reader:
            try:
                header_data = await asyncio.wait_for(
                    self.reader.read(17),
                    timeout=60.0
                )
                if not header_data:
                    break
                
                header = decode_header(header_data)
                if not header:
                    continue
                
                body = b''
                if header['length'] > 0:
                    body = await self.reader.read(header['length'])
                
                receive_time = time.time()
                
                cmd = header['cmd']
                req_id = header['req_id']
                
                if cmd == CmdType.HEARTBEAT:
                    self.stats.increment('heartbeats_received')
                elif cmd in (CmdType.SINGLE_CHAT, CmdType.GROUP_CHAT, CmdType.ACK):
                    self.stats.increment('messages_received')
                    if req_id in self.pending_requests:
                        latency = (receive_time - self.pending_requests[req_id]) * 1000
                        self.stats.add_latency(latency)
                        del self.pending_requests[req_id]
                elif cmd == CmdType.ERROR:
                    self.stats.increment('errors')
                    
            except asyncio.TimeoutError:
                continue
            except Exception:
                self.stats.increment('errors')
                break
    
    async def start(self, heartbeat_interval: float = 30.0):
        """启动客户端"""
        self.running = True
        self.heartbeat_task = asyncio.create_task(self._heartbeat_loop(heartbeat_interval))
        self.receive_task = asyncio.create_task(self._receive_loop())
    
    async def stop(self):
        """停止客户端"""
        self.running = False
        
        if self.heartbeat_task:
            self.heartbeat_task.cancel()
            try:
                await self.heartbeat_task
            except asyncio.CancelledError:
                pass
        
        if self.receive_task:
            self.receive_task.cancel()
            try:
                await self.receive_task
            except asyncio.CancelledError:
                pass
        
        if self.writer:
            self.writer.close()
            try:
                await self.writer.wait_closed()
            except Exception:
                pass
        
        self.connected = False


# ============== 测试场景 ==============
class TestScenario:
    """测试场景基类"""
    
    def __init__(
        self,
        host: str,
        port: int,
        client_count: int,
        stats: Stats
    ):
        self.host = host
        self.port = port
        self.client_count = client_count
        self.stats = stats
        self.clients: List[ChatClient] = []
    
    async def setup(self):
        """创建并连接客户端"""
        print(f"🔌 创建 {self.client_count} 个客户端连接...")
        
        # 分批连接，避免瞬时压力过大
        batch_size = min(500, self.client_count)
        
        for batch_start in range(0, self.client_count, batch_size):
            batch_end = min(batch_start + batch_size, self.client_count)
            batch_tasks = []
            
            for i in range(batch_start, batch_end):
                client = ChatClient(i + 1, self.host, self.port, self.stats)
                self.clients.append(client)
                batch_tasks.append(self._connect_and_auth(client))
            
            await asyncio.gather(*batch_tasks, return_exceptions=True)
            print(f"   已连接: {batch_end}/{self.client_count}")
        
        # 启动心跳和接收
        for client in self.clients:
            if client.authenticated:
                await client.start(heartbeat_interval=30.0)
    
    async def _connect_and_auth(self, client: ChatClient):
        """连接并认证"""
        if await client.connect():
            await asyncio.sleep(0.01)  # 短暂延迟避免瞬时压力
            await client.authenticate()
    
    async def run(self):
        """运行测试"""
        raise NotImplementedError
    
    async def teardown(self):
        """清理资源"""
        print("🧹 清理连接...")
        tasks = [client.stop() for client in self.clients]
        await asyncio.gather(*tasks, return_exceptions=True)


class SingleChatScenario(TestScenario):
    """单聊测试场景"""
    
    def __init__(
        self,
        host: str,
        port: int,
        client_count: int,
        messages_per_client: int,
        stats: Stats
    ):
        super().__init__(host, port, client_count, stats)
        self.messages_per_client = messages_per_client
    
    async def run(self):
        print(f"💬 开始单聊测试: {self.messages_per_client} 条消息/客户端")
        
        async def send_messages(client: ChatClient):
            if not client.authenticated:
                return
            for i in range(self.messages_per_client):
                # 随机选择目标用户
                target_id = random.randint(1, self.client_count)
                while target_id == client.client_id:
                    target_id = random.randint(1, self.client_count)
                
                await client.send_single_chat(
                    target_id,
                    f"Hello from {client.client_id}, msg #{i}"
                )
                await asyncio.sleep(0.05)  # 控制发送速率
        
        tasks = [send_messages(c) for c in self.clients if c.authenticated]
        await asyncio.gather(*tasks, return_exceptions=True)


class GroupChatScenario(TestScenario):
    """群聊测试场景"""
    
    def __init__(
        self,
        host: str,
        port: int,
        client_count: int,
        group_count: int,
        messages_per_client: int,
        stats: Stats
    ):
        super().__init__(host, port, client_count, stats)
        self.group_count = group_count
        self.messages_per_client = messages_per_client
    
    async def run(self):
        print(f"👥 开始群聊测试: {self.group_count} 个群, {self.messages_per_client} 条消息/客户端")
        
        async def send_group_messages(client: ChatClient):
            if not client.authenticated:
                return
            for i in range(self.messages_per_client):
                # 随机选择群组
                session_id = random.randint(1, self.group_count)
                await client.send_group_chat(
                    session_id,
                    f"Group msg from {client.client_id}, msg #{i}"
                )
                await asyncio.sleep(0.05)
        
        tasks = [send_group_messages(c) for c in self.clients if c.authenticated]
        await asyncio.gather(*tasks, return_exceptions=True)


class HeartbeatScenario(TestScenario):
    """心跳保活测试场景"""
    
    def __init__(
        self,
        host: str,
        port: int,
        client_count: int,
        duration: int,
        stats: Stats
    ):
        super().__init__(host, port, client_count, stats)
        self.duration = duration
    
    async def run(self):
        print(f"💓 开始心跳测试: 持续 {self.duration} 秒")
        await asyncio.sleep(self.duration)
        print("💓 心跳测试完成")


class OfflineMessageScenario(TestScenario):
    """离线消息测试场景"""
    
    def __init__(
        self,
        host: str,
        port: int,
        client_count: int,
        messages_per_client: int,
        stats: Stats
    ):
        super().__init__(host, port, client_count, stats)
        self.messages_per_client = messages_per_client
    
    async def run(self):
        print("📤 开始离线消息测试...")
        
        # 1. 让一半客户端下线
        offline_count = self.client_count // 2
        online_clients = self.clients[:offline_count]
        offline_clients = self.clients[offline_count:]
        
        print(f"   断开 {len(offline_clients)} 个客户端...")
        for client in offline_clients:
            await client.stop()
        
        await asyncio.sleep(1)
        
        # 2. 在线客户端发送消息给离线客户端
        print(f"   发送消息给离线客户端...")
        async def send_to_offline(client: ChatClient):
            if not client.authenticated:
                return
            for i in range(self.messages_per_client):
                target_id = random.choice(offline_clients).client_id
                await client.send_single_chat(
                    target_id,
                    f"Offline msg from {client.client_id}, msg #{i}"
                )
                await asyncio.sleep(0.05)
        
        tasks = [send_to_offline(c) for c in online_clients if c.authenticated]
        await asyncio.gather(*tasks, return_exceptions=True)
        
        await asyncio.sleep(1)
        
        # 3. 离线客户端重新上线
        print(f"   离线客户端重新连接...")
        for client in offline_clients:
            if await client.connect():
                await client.authenticate()
                if client.authenticated:
                    await client.start()
        
        # 等待离线消息推送
        print("   等待离线消息推送...")
        await asyncio.sleep(5)


class StressTestScenario(TestScenario):
    """压力测试场景: 最大连接数和消息吞吐量"""
    
    def __init__(
        self,
        host: str,
        port: int,
        client_count: int,
        duration: int,
        messages_per_second: int,
        stats: Stats
    ):
        super().__init__(host, port, client_count, stats)
        self.duration = duration
        self.messages_per_second = messages_per_second
    
    async def run(self):
        print(f"🔥 开始压力测试: {self.duration}秒, 目标 {self.messages_per_second} msg/s")
        
        # 计算每个客户端每秒需要发送的消息数
        authenticated_clients = [c for c in self.clients if c.authenticated]
        if not authenticated_clients:
            print("❌ 没有已认证的客户端")
            return
        
        interval = len(authenticated_clients) / self.messages_per_second
        
        start_time = time.time()
        message_count = 0
        
        while time.time() - start_time < self.duration:
            client = authenticated_clients[message_count % len(authenticated_clients)]
            target_id = random.randint(1, self.client_count)
            while target_id == client.client_id:
                target_id = random.randint(1, self.client_count)
            
            await client.send_single_chat(target_id, f"Stress test msg #{message_count}")
            message_count += 1
            
            # 控制发送速率
            expected_time = start_time + (message_count * interval)
            sleep_time = expected_time - time.time()
            if sleep_time > 0:
                await asyncio.sleep(sleep_time)
        
        print(f"🔥 压力测试完成: 实际发送 {message_count} 条消息")


# ============== 主程序 ==============
async def run_benchmark(args):
    """运行基准测试"""
    stats = Stats()
    stats.start_time = time.time()
    
    scenarios = []
    
    # 根据参数创建测试场景
    if args.scenario == 'all' or args.scenario == 'single':
        scenarios.append(SingleChatScenario(
            args.host, args.port, args.clients,
            args.messages, stats
        ))
    
    if args.scenario == 'all' or args.scenario == 'group':
        scenarios.append(GroupChatScenario(
            args.host, args.port, args.clients,
            args.groups, args.messages, stats
        ))
    
    if args.scenario == 'all' or args.scenario == 'heartbeat':
        scenarios.append(HeartbeatScenario(
            args.host, args.port, args.clients,
            args.duration, stats
        ))
    
    if args.scenario == 'all' or args.scenario == 'offline':
        scenarios.append(OfflineMessageScenario(
            args.host, args.port, args.clients,
            args.messages, stats
        ))
    
    if args.scenario == 'stress':
        scenarios.append(StressTestScenario(
            args.host, args.port, args.clients,
            args.duration, args.qps, stats
        ))
    
    # 运行所有场景
    for scenario in scenarios:
        print(f"\n{'='*60}")
        print(f"🚀 启动场景: {scenario.__class__.__name__}")
        print('='*60)
        
        try:
            await scenario.setup()
            await scenario.run()
        except KeyboardInterrupt:
            print("\n⚠️ 测试被中断")
        finally:
            await scenario.teardown()
    
    stats.end_time = time.time()
    print(stats.report())


def main():
    parser = argparse.ArgumentParser(
        description='Chat Service 高并发测试客户端',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
示例:
  # 运行所有测试场景 (100个客户端)
  python chat_client.py --scenario all --clients 100

  # 单聊压测 (1000个客户端, 每客户端10条消息)
  python chat_client.py --scenario single --clients 1000 --messages 10

  # 群聊压测 (500个客户端, 10个群)
  python chat_client.py --scenario group --clients 500 --groups 10 --messages 5

  # 心跳保活测试 (10000个连接, 持续60秒)
  python chat_client.py --scenario heartbeat --clients 10000 --duration 60

  # 离线消息测试
  python chat_client.py --scenario offline --clients 100 --messages 5

  # 压力测试 (目标 5000 QPS)
  python chat_client.py --scenario stress --clients 5000 --duration 30 --qps 5000
        """
    )
    
    parser.add_argument('--host', default='127.0.0.1', help='服务器地址 (默认: 127.0.0.1)')
    parser.add_argument('--port', type=int, default=9090, help='服务器端口 (默认: 9090)')
    parser.add_argument('--clients', type=int, default=100, help='客户端数量 (默认: 100)')
    parser.add_argument('--messages', type=int, default=10, help='每客户端消息数 (默认: 10)')
    parser.add_argument('--groups', type=int, default=5, help='群组数量 (默认: 5)')
    parser.add_argument('--duration', type=int, default=30, help='测试持续时间/秒 (默认: 30)')
    parser.add_argument('--qps', type=int, default=1000, help='目标 QPS (默认: 1000)')
    parser.add_argument(
        '--scenario',
        choices=['all', 'single', 'group', 'heartbeat', 'offline', 'stress'],
        default='all',
        help='测试场景 (默认: all)'
    )
    
    args = parser.parse_args()
    
    print(f"""
╔══════════════════════════════════════════════════════════════╗
║          Chat Service 高并发测试客户端 v1.0                  ║
╠══════════════════════════════════════════════════════════════╣
║  服务器: {args.host}:{args.port:<42}║
║  客户端数: {args.clients:<48}║
║  测试场景: {args.scenario:<48}║
╚══════════════════════════════════════════════════════════════╝
    """)
    
    # 设置信号处理
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    
    try:
        loop.run_until_complete(run_benchmark(args))
    except KeyboardInterrupt:
        print("\n⚠️ 测试被用户中断")
    finally:
        loop.close()


if __name__ == '__main__':
    main()
