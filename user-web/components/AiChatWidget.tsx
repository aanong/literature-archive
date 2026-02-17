"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { api } from "@/lib/api";

interface QASession {
    id: number;
    title: string;
}

interface QAMessage {
    id: number;
    role: "USER" | "ASSISTANT" | "SYSTEM";
    content: string;
}

function parseUserIdFromToken(token: string): number | null {
    if (!token) return null;
    try {
        const raw = token.split(".")[1] || "";
        const base64 = raw.replace(/-/g, "+").replace(/_/g, "/");
        const payload = JSON.parse(atob(base64));
        const userId = payload?.userId;
        if (typeof userId === "number") return userId;
        if (typeof userId === "string" && userId.trim()) return Number(userId);
    } catch {
        // ignore
    }
    return null;
}

export function AiChatWidget() {
    const [open, setOpen] = useState(false);
    const [sessionId, setSessionId] = useState<number | null>(null);
    const [messages, setMessages] = useState<QAMessage[]>([]);
    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const userId = useMemo(() => {
        if (typeof window === "undefined") return null;
        return parseUserIdFromToken(window.localStorage.getItem("token") || "");
    }, []);

    useEffect(() => {
        if (!open || !userId) return;
        initSession();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [open, userId]);

    async function initSession() {
        setError("");
        try {
            const sessions = (await api.get("/knowledge/qa/sessions", {
                params: { userId },
            })) as QASession[];
            if (sessions && sessions.length > 0) {
                setSessionId(sessions[0].id);
                await loadMessages(sessions[0].id);
                return;
            }
            const created = (await api.post("/knowledge/qa/sessions", {
                userId,
                title: "AI资料库对话",
            })) as QASession;
            setSessionId(created.id);
            setMessages([]);
        } catch (err: any) {
            setError(err?.message || "初始化会话失败");
        }
    }

    async function loadMessages(id: number) {
        try {
            const data = (await api.get(`/knowledge/qa/sessions/${id}/messages`)) as QAMessage[];
            setMessages(data || []);
        } catch (err: any) {
            setError(err?.message || "加载消息失败");
        }
    }

    async function send(e: FormEvent) {
        e.preventDefault();
        if (!sessionId || !input.trim() || loading) return;
        setLoading(true);
        setError("");
        const question = input.trim();
        setInput("");
        setMessages((prev) => [
            ...prev,
            { id: Date.now(), role: "USER", content: question },
        ]);
        try {
            const answer = (await api.post(
                `/knowledge/qa/sessions/${sessionId}/ask`,
                { question }
            )) as string;
            setMessages((prev) => [
                ...prev,
                { id: Date.now() + 1, role: "ASSISTANT", content: answer || "（无回答）" },
            ]);
        } catch (err: any) {
            setError(err?.message || "提问失败");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="fixed right-6 top-6 z-50">
            <button
                onClick={() => setOpen((v) => !v)}
                className="rounded-full bg-black text-white px-4 py-2 text-sm shadow-lg"
            >
                {open ? "关闭 AI" : "AI资料库"}
            </button>
            {open && (
                <div className="mt-3 w-[360px] h-[420px] rounded-xl border bg-white shadow-xl flex flex-col">
                    <div className="px-4 py-3 border-b text-sm font-semibold">
                        AI 资料库对话
                    </div>
                    <div className="flex-1 overflow-y-auto p-4 space-y-3 text-sm">
                        {!userId && (
                            <div className="text-red-600">未登录，无法开启 AI 对话。</div>
                        )}
                        {messages.map((m) => (
                            <div
                                key={m.id}
                                className={`max-w-[85%] rounded-lg px-3 py-2 ${
                                    m.role === "USER"
                                        ? "ml-auto bg-black text-white"
                                        : "bg-gray-100 text-gray-900"
                                }`}
                            >
                                {m.content}
                            </div>
                        ))}
                        {error && <div className="text-red-600">{error}</div>}
                    </div>
                    <form onSubmit={send} className="p-3 border-t flex gap-2">
                        <input
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            placeholder="请输入问题..."
                            className="flex-1 rounded-md border px-3 py-2 text-sm"
                        />
                        <button
                            type="submit"
                            disabled={loading || !userId}
                            className="rounded-md bg-black text-white px-3 py-2 text-sm disabled:opacity-60"
                        >
                            发送
                        </button>
                    </form>
                </div>
            )}
        </div>
    );
}
