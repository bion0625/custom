import type { PrivateScene } from "../types.ts";
import api from "./api.ts";

export const getPrivateFirstScene = async (): Promise<PrivateScene> => {
    const res = await api.get<PrivateScene>("/my/scene");
    return res.data;
};

export const getPrivateScene = async (id: string): Promise<PrivateScene> => {
    const res = await api.get<PrivateScene>(`/my/scene/${id}`);
    return res.data;
};
