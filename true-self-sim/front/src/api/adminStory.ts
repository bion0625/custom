import type { AdminStoryInfo } from "../types.ts";
import api from "./api.ts";

export const getAdminStories = async (): Promise<AdminStoryInfo[]> => {
    const res = await api.get<AdminStoryInfo[]>("/public/admin/stories");
    return res.data;
};
