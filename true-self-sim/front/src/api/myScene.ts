import type {PublicSceneRequest, PublicStory} from "../types.ts";
import api from "./api.ts";

export const getMyStory = async (): Promise<PublicStory> => {
    const res = await api.get<PublicStory>("/my/story");
    return res.data;
}

export const postMyScene = async (data: PublicSceneRequest) => {
    const res = await api.post(`/my/scene/${data.sceneId}`, data);
    return res.data;
}

export const deleteMyScene = async (id: string) => {
    const res = await api.delete(`/my/scene/${id}`);
    return res.data;
}
