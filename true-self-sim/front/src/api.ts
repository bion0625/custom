import axios, {type InternalAxiosRequestConfig} from "axios";

const api = axios.create({
    baseURL: process.env.VUE_APP_API_URL || "http://localhost:8080"
});

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem("access_token");
    if (token && config.headers) {
        // headers 객체가 있을 때만 안전하게 Authorization 헤더 주입
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
})

export default api;