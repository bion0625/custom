import type {PrivateSceneRequest, PrivateStory} from "../types.ts";
import api from "./api.ts";

export const getMyStory = async (): Promise<PrivateStory> => {
    const res = await api.get<PrivateStory>("/my/story");
    return res.data;
}

export const postMyScene = async (data: PrivateSceneRequest) => {
    const res = await api.post(`/my/scene/${data.sceneId}`, data);
    return res.data;
}

export const deleteMyScene = async (id: string) => {
    const res = await api.delete(`/my/scene/${id}`);
    return res.data;
}
