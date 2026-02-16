"use client";

import Link from "next/link";
import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";

export default function RegisterPage() {
    const router = useRouter();
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    async function onSubmit(e: FormEvent) {
        e.preventDefault();
        if (!username.trim() || !password.trim() || loading) return;
        if (password !== confirmPassword) {
            setError("两次输入密码不一致");
            return;
        }

        setLoading(true);
        setError("");
        try {
            await api.post("/auth/register", {
                username: username.trim(),
                password,
            });
            router.replace("/login");
        } catch (err: any) {
            setError(err?.message || "注册失败");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="min-h-[calc(100vh-8rem)] flex items-center justify-center px-4">
            <div className="w-full max-w-md rounded-xl border bg-white p-8 shadow-sm">
                <h1 className="text-2xl font-bold text-center mb-6">用户注册</h1>
                <form onSubmit={onSubmit} className="space-y-4">
                    <input
                        className="w-full rounded-md border px-3 py-2 text-sm"
                        placeholder="用户名（4-32位）"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                    />
                    <input
                        type="password"
                        className="w-full rounded-md border px-3 py-2 text-sm"
                        placeholder="密码（6位以上）"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                    <input
                        type="password"
                        className="w-full rounded-md border px-3 py-2 text-sm"
                        placeholder="确认密码"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                    />
                    {error && <p className="text-sm text-red-600">{error}</p>}
                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full rounded-md bg-black text-white py-2 text-sm disabled:opacity-60"
                    >
                        {loading ? "注册中..." : "注册"}
                    </button>
                </form>
                <div className="mt-4 text-center text-sm text-gray-600">
                    已有账号？<Link href="/login" className="text-black underline">去登录</Link>
                </div>
            </div>
        </div>
    );
}
