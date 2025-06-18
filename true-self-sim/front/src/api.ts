import axios, {type InternalAxiosRequestConfig} from "axios";
import type {LoginRequest, RegisterRequest, User} from "./types.ts";

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL  || "http://localhost:8080"
});

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem("access_token");
    if (token && config.headers) {
        // headers 객체가 있을 때만 안전하게 Authorization 헤더 주입
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
})

export const login = async ({id, password}: LoginRequest) => {
    const res = await api.post("/login", {id, password});
    return res.data;
}

export const register = async ({id, password, name, phoneNumber, email}: RegisterRequest) => {
    const res = await api.post("/register", {id, password, name, phoneNumber, email});
    return res.data;
}

export const getMe = async (): Promise<User> => {
    const res = await api.get<User>("/me");
    return res.data;
}