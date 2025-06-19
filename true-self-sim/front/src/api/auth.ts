import type {LoginRequest, User} from "../types.ts";
import api from "./api.ts";

export const login = async ({id, password}: LoginRequest) => {
    const res = await api.post("/login", {id, password});
    return res.data;
}

export const getMe = async (): Promise<User> => {
    const res = await api.get<User>("/me");
    return res.data;
}