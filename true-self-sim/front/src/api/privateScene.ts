import type { PrivateScene, PrivateStoryInfo } from "../types.ts";
import api from "./api.ts";

export const getPrivateFirstScene = async (storyId: number, memberId?: string): Promise<PrivateScene> => {
    const url = memberId ? `/user/${memberId}/story/${storyId}/scene` : `/my/story/${storyId}/scene`;
    const res = await api.get<PrivateScene>(url);
    return res.data;
};

export const getPrivateScene = async (id: string, storyId: number, memberId?: string): Promise<PrivateScene> => {
    const url = memberId ? `/user/${memberId}/story/${storyId}/scene/${id}` : `/my/story/${storyId}/scene/${id}`;
    const res = await api.get<PrivateScene>(url);
    return res.data;
};

export const getUserStories = async (memberId: string): Promise<PrivateStoryInfo[]> => {
    const res = await api.get<PrivateStoryInfo[]>(`/user/${memberId}/stories`);
    return res.data;
};
