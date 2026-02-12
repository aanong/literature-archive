"use client";

import { useState, useEffect, useRef } from "react";
import { MessageSquare, X, Send, Loader2 } from "lucide-react";
import { useChatSession } from "@/hooks/useChatSession";
import { cn } from "@/lib/utils";
import ReactMarkdown from "react-markdown";

interface AIChatProps {
    userId?: number; // Should come from auth context
    context?: string; // Current reading content
}

export function AIChat({ userId = 1, context }: AIChatProps) {
    const [isOpen, setIsOpen] = useState(false);
    const [input, setInput] = useState("");
    const { messages, sendMessage, loading } = useChatSession(userId);
    const scrollRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (scrollRef.current) {
            scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
        }
    }, [messages, isOpen]);

    return (
        <div className="fixed bottom-6 right-6 z-50 flex flex-col items-end gap-4">
            {/* Chat Window */}
            {isOpen && (
                <div className="w-[350px] sm:w-[400px] h-[500px] bg-background border rounded-xl shadow-2xl flex flex-col overflow-hidden animate-in slide-in-from-bottom-5 fade-in duration-300">
                    {/* Header */}
                    <div className="p-4 border-b bg-primary text-primary-foreground flex justify-between items-center bg-black text-white">
                        <h3 className="font-semibold flex items-center gap-2">
                            <MessageSquare className="h-4 w-4" />
                            AI Assistant
                        </h3>
                        <button
                            onClick={() => setIsOpen(false)}
                            className="hover:bg-white/20 p-1 rounded-md transition-colors"
                        >
                            <X className="h-4 w-4" />
                        </button>
                    </div>

                    {/* Messages */}
                    <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50/50" ref={scrollRef}>
                        {messages.length === 0 && (
                            <div className="text-center text-muted-foreground text-sm py-8">
                                Ask me anything about the content!
                            </div>
                        )}
                        {messages.map((msg, idx) => (
                            <div
                                key={idx}
                                className={cn(
                                    "flex w-full",
                                    msg.role === "USER" ? "justify-end" : "justify-start"
                                )}
                            >
                                <div
                                    className={cn(
                                        "max-w-[80%] rounded-lg px-3 py-2 text-sm",
                                        msg.role === "USER"
                                            ? "bg-black text-white"
                                            : "bg-white border shadow-sm text-gray-800"
                                    )}
                                >
                                    {msg.role === "ASSISTANT" ? <ReactMarkdown>{msg.content}</ReactMarkdown> : msg.content}
                                </div>
                            </div>
                        ))}
                        {loading && (
                            <div className="flex justify-start">
                                <div className="bg-white border shadow-sm rounded-lg px-3 py-2">
                                    <Loader2 className="h-4 w-4 animate-spin text-gray-500" />
                                </div>
                            </div>
                        )}
                    </div>

                    {/* Input */}
                    <div className="p-3 border-t bg-background">
                        <form
                            onSubmit={(e) => {
                                e.preventDefault();
                                if (input.trim()) {
                                    sendMessage(input, context);
                                    setInput("");
                                }
                            }}
                            className="flex gap-2"
                        >
                            <input
                                value={input}
                                onChange={(e) => setInput(e.target.value)}
                                placeholder="Ask a question..."
                                className="flex-1 rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-black/20"
                            />
                            <button
                                type="submit"
                                disabled={loading || !input.trim()}
                                className="bg-black text-white p-2 rounded-md hover:bg-black/90 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                            >
                                <Send className="h-4 w-4" />
                            </button>
                        </form>
                    </div>
                </div>
            )}

            {/* Toggle Button */}
            <button
                onClick={() => setIsOpen(!isOpen)}
                className="h-14 w-14 rounded-full bg-black text-white shadow-lg hover:scale-105 transition-transform flex items-center justify-center p-0"
            >
                {isOpen ? <X className="h-6 w-6" /> : <MessageSquare className="h-6 w-6" />}
            </button>
        </div>
    );
}
