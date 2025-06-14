// src/api.ts
import axios from "axios";
import type { InternalAxiosRequestConfig } from "axios";

export const api = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL || "http://localhost:8000",
});

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem("access_token");
  if (token && config.headers) {
    // headers 객체가 있을 때만 안전하게 Authorization 헤더 주입
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});