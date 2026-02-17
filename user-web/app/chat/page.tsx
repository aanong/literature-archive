"use client";

import { FormEvent, useEffect, useMemo, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { getCurrentUser, getToken, isLoggedIn } from "@/lib/auth";

interface ChatMessage {
    id: string;
    sessionId: string;
    senderId: string;
    senderName?: string;
    content: string;
    createdAt: string;
}

interface ChatSession {
    id: string;
    title: string;
    updatedAt: string;
    peerUserId?: string;
    peerUserType?: string;
    peerUsername?: string;
    lastMessage?: ChatMessage;
}

export default function ChatPage() {
    const router = useRouter();
    const [sessions, setSessions] = useState<ChatSession[]>([]);
    const [currentSessionId, setCurrentSessionId] = useState<string>("");
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [peerUsername, setPeerUsername] = useState("");
    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const currentSessionIdRef = useRef<string>("");
    const wsRef = useRef<WebSocket | null>(null);
    const wsStatusRef = useRef<"disconnected" | "connecting" | "connected">("disconnected");
    const reconnectTimerRef = useRef<number | null>(null);

    const currentUser = useMemo(() => getCurrentUser(), []);

    useEffect(() => {
        if (!isLoggedIn()) {
            router.replace("/login");
            return;
        }
        loadSessions();
    }, [router]);

    useEffect(() => {
        currentSessionIdRef.current = currentSessionId;
    }, [currentSessionId]);

    useEffect(() => {
        connectWebSocket();
        return () => {
            disconnectWebSocket();
        };
    }, []);

    async function loadSessions() {
        try {
            const data = (await api.get("/chat/sessions/mine")) as ChatSession[];
            setSessions(data || []);
            if (!currentSessionId && data?.length) {
                setCurrentSessionId(data[0].id);
                loadMessages(data[0].id);
            }
        } catch (err: any) {
            setError(err?.message || "加载会话失败");
        }
    }

    async function loadMessages(sessionId: string) {
        try {
            const data = (await api.get(`/chat/sessions/${sessionId}/messages?page=1&pageSize=100`)) as ChatMessage[];
            setMessages(data || []);
        } catch (err: any) {
            setError(err?.message || "加载消息失败");
        }
    }

    async function createPrivateSession(e: FormEvent) {
        e.preventDefault();
        if (!peerUsername.trim()) return;
        setError("");
        try {
            const session = (await api.post("/chat/private-sessions", {
                peerUsername: peerUsername.trim(),
            })) as ChatSession;
            setCurrentSessionId(session.id);
            setPeerUsername("");
            await loadSessions();
            await loadMessages(session.id);
        } catch (err: any) {
            setError(err?.message || "创建会话失败");
        }
    }

    async function sendMessage(e: FormEvent) {
        e.preventDefault();
        if (!currentSessionId || !input.trim() || loading) return;
        setLoading(true);
        setError("");
        try {
            const session = sessions.find((s) => s.id === currentSessionId);
            const targetId = session?.peerUserId ? Number(session.peerUserId) : null;
            const targetUserType = session?.peerUserType || null;
            if (!targetId) {
                throw new Error("无法确定对方用户");
            }
            const ws = wsRef.current;
            if (!ws || ws.readyState !== WebSocket.OPEN) {
                const connected = await connectWebSocket(true);
                if (!connected) {
                    throw new Error("WebSocket 未连接");
                }
            }
            ws.send(
                JSON.stringify({
                    cmd: "SINGLE_CHAT",
                    sessionId: Number(currentSessionId),
                    targetId,
                    targetUserType,
                    content: input.trim(),
                    contentType: "text",
                    timestamp: Date.now(),
                })
            );
            setInput("");
            await loadMessages(currentSessionId);
            await loadSessions();
        } catch (err: any) {
            setError(err?.message || "发送失败");
        } finally {
            setLoading(false);
        }
    }

    function connectWebSocket(waitForOpen = false): Promise<boolean> | void {
        if (wsStatusRef.current === "connected" || wsStatusRef.current === "connecting") {
            return waitForOpen ? Promise.resolve(wsStatusRef.current === "connected") : undefined;
        }
        const token = getToken();
        if (!token) return;
        const wsUrl =
            process.env.NEXT_PUBLIC_CHAT_WS_URL ||
            `${window.location.protocol === "https:" ? "wss" : "ws"}://${window.location.hostname}:18092/ws`;
        const ws = new WebSocket(wsUrl);
        wsRef.current = ws;
        wsStatusRef.current = "connecting";

        ws.onopen = () => {
            ws.send(JSON.stringify({ cmd: "AUTH", token }));
            wsStatusRef.current = "connected";
        };

        ws.onmessage = () => {
            loadSessions();
            if (currentSessionIdRef.current) {
                loadMessages(currentSessionIdRef.current);
            }
        };

        ws.onerror = () => {
            wsStatusRef.current = "disconnected";
        };

        ws.onclose = () => {
            wsRef.current = null;
            wsStatusRef.current = "disconnected";
            scheduleReconnect();
        };

        if (!waitForOpen) return;
        return new Promise((resolve) => {
            const start = Date.now();
            const timer = window.setInterval(() => {
                if (wsStatusRef.current === "connected") {
                    window.clearInterval(timer);
                    resolve(true);
                    return;
                }
                if (Date.now() - start > 2000) {
                    window.clearInterval(timer);
                    resolve(false);
                }
            }, 50);
        });
    }

    function disconnectWebSocket() {
        if (reconnectTimerRef.current) {
            window.clearTimeout(reconnectTimerRef.current);
            reconnectTimerRef.current = null;
        }
        if (wsRef.current) {
            wsRef.current.close();
            wsRef.current = null;
        }
        wsStatusRef.current = "disconnected";
    }

    function scheduleReconnect() {
        if (reconnectTimerRef.current) return;
        reconnectTimerRef.current = window.setTimeout(() => {
            reconnectTimerRef.current = null;
            connectWebSocket();
        }, 1000);
    }

    return (
        <div className="container py-6">
            <h1 className="text-2xl font-bold mb-4">用户会话</h1>
            <div className="grid grid-cols-1 lg:grid-cols-[280px_1fr] gap-4">
                <aside className="border rounded-lg bg-white p-4 space-y-4">
                    <form onSubmit={createPrivateSession} className="space-y-2">
                        <input
                            value={peerUsername}
                            onChange={(e) => setPeerUsername(e.target.value)}
                            placeholder="输入对方用户名（如 admin）"
                            className="w-full rounded-md border px-3 py-2 text-sm"
                        />
                        <button className="w-full rounded-md bg-black text-white py-2 text-sm">
                            新建/进入私聊
                        </button>
                    </form>
                    <div className="space-y-2">
                        {sessions.map((s) => (
                            <button
                                key={s.id}
                                onClick={() => {
                                    setCurrentSessionId(s.id);
                                    loadMessages(s.id);
                                }}
                                className={`w-full text-left rounded-md border px-3 py-2 text-sm ${currentSessionId === s.id ? "bg-black text-white" : "bg-white"}`}
                            >
                                <div className="font-medium truncate">{s.title}</div>
                                <div className="text-xs opacity-70 truncate">{s.lastMessage?.content || "暂无消息"}</div>
                            </button>
                        ))}
                    </div>
                </aside>

                <section className="border rounded-lg bg-white flex flex-col h-[70vh]">
                    <div className="flex-1 overflow-y-auto p-4 space-y-3">
                        {messages.map((m) => {
                            const mine = m.senderName === currentUser?.username;
                            return (
                                <div key={m.id} className={`flex ${mine ? "justify-end" : "justify-start"}`}>
                                    <div className={`max-w-[75%] rounded-lg px-3 py-2 text-sm ${mine ? "bg-black text-white" : "bg-gray-100 text-gray-900"}`}>
                                        {!mine && <div className="text-xs opacity-70 mb-1">{m.senderName || m.senderId}</div>}
                                        <div>{m.content}</div>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                    <form onSubmit={sendMessage} className="border-t p-3 flex gap-2">
                        <input
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            placeholder={currentSessionId ? "输入消息..." : "请先选择会话"}
                            disabled={!currentSessionId}
                            className="flex-1 rounded-md border px-3 py-2 text-sm"
                        />
                        <button
                            type="submit"
                            disabled={!currentSessionId || loading || !input.trim()}
                            className="rounded-md bg-black text-white px-4 py-2 text-sm disabled:opacity-60"
                        >
                            发送
                        </button>
                    </form>
                </section>
            </div>
            {error && <p className="mt-3 text-sm text-red-600">{error}</p>}
        </div>
    );
}
