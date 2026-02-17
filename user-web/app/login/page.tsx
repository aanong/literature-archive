"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { api } from "@/lib/api";
import { saveLogin } from "@/lib/auth";

interface LoginResponse {
    token: string;
    expiresIn: number;
}

export default function LoginPage() {
    const router = useRouter();
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    async function onSubmit(e: FormEvent) {
        e.preventDefault();
        if (!username.trim() || !password.trim() || loading) return;

        setLoading(true);
        setError("");
        try {
            const data = (await api.post("/auth/login", {
                username: username.trim(),
                password,
            })) as LoginResponse;
            if (!data?.token) {
                setError("登录响应缺少 token");
                return;
            }
            saveLogin(data.token, data.expiresIn);
            router.replace("/chat");
        } catch (err: any) {
            setError(err?.message || "登录失败");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="min-h-[calc(100vh-8rem)] flex items-center justify-center px-4">
            <div className="w-full max-w-md rounded-xl border bg-white p-8 shadow-sm">
                <h1 className="text-2xl font-bold text-center mb-6">用户登录</h1>
                <form onSubmit={onSubmit} className="space-y-4">
                    <input
                        className="w-full rounded-md border px-3 py-2 text-sm"
                        placeholder="用户名"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                    />
                    <input
                        type="password"
                        className="w-full rounded-md border px-3 py-2 text-sm"
                        placeholder="密码"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                    {error && <p className="text-sm text-red-600">{error}</p>}
                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full rounded-md bg-black text-white py-2 text-sm disabled:opacity-60"
                    >
                        {loading ? "登录中..." : "登录"}
                    </button>
                </form>
                <div className="mt-4 text-center text-sm text-gray-600">
                    还没有账号？<Link href="/register" className="text-black underline">去注册</Link>
                </div>
            </div>
        </div>
    );
}
