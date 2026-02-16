"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { BookOpen, Search, User } from "lucide-react";
import { clearLogin, getCurrentUser } from "@/lib/auth";

export function Navbar() {
    const [username, setUsername] = useState("");

    useEffect(() => {
        const user = getCurrentUser();
        setUsername(user?.username || "");
    }, []);

    return (
        <header className="sticky top-0 z-50 w-full border-b border-border/40 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 font-serif">
            <div className="container flex h-16 items-center">
                <Link href="/" className="mr-8 flex items-center space-x-3 transition-colors hover:opacity-80">
                    <BookOpen className="h-6 w-6 text-primary-foreground" />
                    <span className="hidden text-lg font-bold tracking-widest text-primary sm:inline-block">
                        云章·经籍
                    </span>
                </Link>
                <nav className="flex items-center space-x-6 text-sm font-medium">
                    <Link
                        href="/library"
                        className="transition-colors hover:text-primary text-muted-foreground hover:font-bold"
                    >
                        藏书阁
                    </Link>
                    <Link
                        href="/about"
                        className="transition-colors hover:text-primary text-muted-foreground hover:font-bold"
                    >
                        关于
                    </Link>
                    <Link
                        href="/chat"
                        className="transition-colors hover:text-primary text-muted-foreground hover:font-bold"
                    >
                        聊天
                    </Link>
                </nav>
                <div className="ml-auto flex items-center space-x-4">
                    <button className="inline-flex items-center justify-center rounded-md text-sm font-medium transition-colors hover:bg-accent/20 hover:text-accent-foreground h-9 w-9">
                        <Search className="h-5 w-5 text-muted-foreground hover:text-primary" />
                        <span className="sr-only">搜索</span>
                    </button>
                    <button className="inline-flex items-center justify-center rounded-md text-sm font-medium transition-colors hover:bg-accent/20 hover:text-accent-foreground h-9 w-9">
                        <User className="h-5 w-5 text-muted-foreground hover:text-primary" />
                        <span className="sr-only">用户</span>
                    </button>
                    {username ? (
                        <div className="flex items-center gap-2 text-xs text-muted-foreground">
                            <span>{username}</span>
                            <button
                                className="rounded border px-2 py-1 hover:bg-accent/20"
                                onClick={() => {
                                    clearLogin();
                                    window.location.href = "/login";
                                }}
                            >
                                退出
                            </button>
                        </div>
                    ) : (
                        <Link href="/login" className="text-xs rounded border px-2 py-1 hover:bg-accent/20">
                            登录
                        </Link>
                    )}
                </div>
            </div>
        </header>
    );
}
