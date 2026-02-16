import { useState, useEffect } from "react";
import { api } from "@/lib/api";

export interface ChatSession {
    id: number;
    title: string;
}

export interface Message {
    id?: number;
    role: "USER" | "ASSISTANT";
    content: string;
}

export function useChatSession(userId: number) {
    const [sessionId, setSessionId] = useState<number | null>(null);
    const [messages, setMessages] = useState<Message[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (userId) {
            loadSession(userId);
        }
    }, [userId]);

    const loadSession = async (uid: number) => {
        try {
            // 1. Get existing sessions
            const sessions = (await api.get(`/knowledge/qa/sessions?userId=${uid}`)) as ChatSession[] || [];
            if (sessions.length > 0) {
                // Use the latest session
                const latest = sessions[0];
                setSessionId(latest.id);
                fetchMessages(latest.id);
            } else {
                // Create new session
                const newSession = (await api.post("/knowledge/qa/sessions", {
                    userId: uid,
                    title: "Reader Assistant",
                })) as ChatSession | null;
                if (newSession) {
                    setSessionId(newSession.id);
                }
            }
        } catch (e) {
            console.error("Failed to load session", e);
        }
    };

    const fetchMessages = async (sid: number) => {
        try {
            const msgs = (await api.get(`/knowledge/qa/sessions/${sid}/messages`)) as Message[] || [];
            setMessages(msgs);
        } catch (e) {
            console.error("Failed to fetch messages", e);
        }
    };

    const sendMessage = async (content: string, context?: string) => {
        if (!sessionId) return;

        // Optimistic update
        const userMsg: Message = { role: "USER", content };
        setMessages((prev) => [...prev, userMsg]);
        setLoading(true);

        try {
            const res: string = await api.post(`/knowledge/qa/sessions/${sessionId}/ask`, {
                question: content,
                context: context,
            });

            const aiMsg: Message = { role: "ASSISTANT", content: res };
            setMessages((prev) => [...prev, aiMsg]);
        } catch (e) {
            console.error("Failed to send message", e);
            setMessages((prev) => [...prev, { role: "ASSISTANT", content: "Error: Failed to get response." }]);
        } finally {
            setLoading(false);
        }
    };

    return { sessionId, messages, sendMessage, loading };
}
