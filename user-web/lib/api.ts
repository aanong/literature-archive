import axios from "axios";

// 客户端 API (浏览器端使用)
// 自动由 Next.js Rewrites 代理到 Gateway
export const api = axios.create({
    baseURL: "/api",
    timeout: 60000,
});

// 服务端 API (SSR 使用)
// 直接调用 Gateway (因 SSR 在服务器端，无法经过 Next.js Rewrites，除非 fetch localhost:3000/api)
// 但通常 SSR 直接调后端更高效，或者调 localhost:3000/api 也可以。
// 为简单起见，如果是在 Server Component 中，建议使用 fetch 或专门的 serverApi
// 这里配置一个 serverApi 指向 Gateway
export const serverApi = axios.create({
    baseURL: process.env.GATEWAY_URL || "http://localhost:8080/api",
    timeout: 60000,
});

// 响应拦截器 (客户端)
api.interceptors.response.use(
    (response) => {
        const res = response.data;
        if (res.code && res.code !== "0000") {
            console.error("API Error:", res.message);
            return Promise.reject(new Error(res.message || "Error"));
        }
        return res.data;
    },
    (error) => {
        console.error("Network Error:", error);
        return Promise.reject(error);
    }
);

// 响应拦截器 (服务端)
serverApi.interceptors.response.use(
    (response) => {
        const res = response.data;
        if (res.code && res.code !== "0000") {
            console.error("Server API Error:", res.message);
            return Promise.reject(new Error(res.message || "Error"));
        }
        return res.data;
    },
    (error) => {
        console.error("Server Network Error:", error);
        return Promise.reject(error);
    }
);
