import type {PublicSceneRequest, PublicStory} from "../types.ts";
import api from "./api.ts";

export const getPublicStory = async (): Promise<PublicStory> => {
    const res = await api.get<PublicStory>("/admin/public/story");
    return res.data;
}

export const postPublicSceneBulk = async (body: PublicSceneRequest[]) => {
    const res = await api.post("/admin/public/scenes/bulk", body);
    return res.data;
}

export const postPublicScene = async ({sceneId, speaker, backgroundImage, text, choiceRequests, start, end}: PublicSceneRequest) => {
    const res = await api.post(`/admin/public/scene/${sceneId}`, {speaker, backgroundImage, text, choiceRequests, start, end});
    return res.data;
}

export const deletePublicScene = async (id: string) => {
    const res = await api.delete(`/admin/public/scene/${id}`);
    return res.data;
}