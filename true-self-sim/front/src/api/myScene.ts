import type {PrivateSceneRequest, PrivateStory, PrivateStoryInfo} from "../types.ts";
import api from "./api.ts";

export const getMyStories = async (): Promise<PrivateStoryInfo[]> => {
    const res = await api.get<PrivateStoryInfo[]>("/my/stories");
    return res.data;
}

export const getMyStory = async (storyId: number): Promise<PrivateStory> => {
    const res = await api.get<PrivateStory>(`/my/story/${storyId}`);
    return res.data;
}

export const postMyScene = async (data: PrivateSceneRequest) => {
    const res = await api.post(`/my/story/${data.storyId}/scene/${data.sceneId}`, data);
    return res.data;
}

export const deleteMyScene = async (id: string, storyId: number) => {
    const res = await api.delete(`/my/story/${storyId}/scene/${id}`);
    return res.data;
}

export const postMySceneBulk = async (storyId: number, data: PrivateSceneRequest[]) => {
    const res = await api.post(`/my/story/${storyId}/scenes/bulk`, data);
    return res.data;
}
