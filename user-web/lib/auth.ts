export interface AuthUser {
    username: string;
}

export function parseUsernameFromToken(token: string): string {
    try {
        const raw = token.split(".")[1] || "";
        const base64 = raw.replace(/-/g, "+").replace(/_/g, "/");
        const payload = JSON.parse(atob(base64));
        return payload?.sub || "";
    } catch {
        return "";
    }
}

export function saveLogin(token: string, expiresInSeconds: number) {
    if (typeof window === "undefined") return;
    const expireAt = Date.now() + expiresInSeconds * 1000;
    window.localStorage.setItem("token", token);
    window.localStorage.setItem("tokenExpireAt", String(expireAt));
}

export function clearLogin() {
    if (typeof window === "undefined") return;
    window.localStorage.removeItem("token");
    window.localStorage.removeItem("tokenExpireAt");
}

export function getToken(): string {
    if (typeof window === "undefined") return "";
    return window.localStorage.getItem("token") || "";
}

export function isLoggedIn(): boolean {
    if (typeof window === "undefined") return false;
    const token = getToken();
    if (!token) return false;
    const expireAt = Number(window.localStorage.getItem("tokenExpireAt") || "0");
    if (expireAt > 0 && Date.now() > expireAt) {
        clearLogin();
        return false;
    }
    return true;
}

export function getCurrentUser(): AuthUser | null {
    const token = getToken();
    if (!token) return null;
    const username = parseUsernameFromToken(token);
    if (!username) return null;
    return { username };
}
