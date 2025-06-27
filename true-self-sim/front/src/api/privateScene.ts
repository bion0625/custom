import type { PrivateScene } from "../types.ts";
import api from "./api.ts";

export const getPrivateFirstScene = async (memberId?: string): Promise<PrivateScene> => {
    const url = memberId ? `/user/${memberId}/scene` : "/my/scene";
    const res = await api.get<PrivateScene>(url);
    return res.data;
};

export const getPrivateScene = async (id: string, memberId?: string): Promise<PrivateScene> => {
    const url = memberId ? `/user/${memberId}/scene/${id}` : `/my/scene/${id}`;
    const res = await api.get<PrivateScene>(url);
    return res.data;
};
