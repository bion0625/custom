import type {RegisterRequest} from "../types.ts";
import api from "./api.ts";

export const register = async ({id, password, name, phoneNumber, email}: RegisterRequest) => {
    const res = await api.post("/register", {id, password, name, phoneNumber, email});
    return res.data;
}