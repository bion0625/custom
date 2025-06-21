import type {PublicSceneRequest, PublicStory} from "../types.ts";
import api from "./api.ts";

export const getPublicStory = async (): Promise<PublicStory> => {
    const res = await api.get<PublicStory>("/admin/public/story");
    return res.data;
}

export const postPublicScene = async ({speaker, backgroundImage, text, choiceRequests, start, end}: PublicSceneRequest) => {
    const res = await api.post("/admin/public/scene", {speaker, backgroundImage, text, choiceRequests, start, end});
    return res.data;
}

export const postPublicSceneBulk = async (body: PublicSceneRequest[]) => {
    const withOutId = body.map(({id: _id, ...rest}) => rest);
    const res = await api.post("/admin/public/scene/bulk", withOutId);
    return res.data;
}

export const putPublicScene = async ({id, speaker, backgroundImage, text, choiceRequests, start, end}: PublicSceneRequest) => {
    const res = await api.put(`/admin/public/scene/${id}`, {speaker, backgroundImage, text, choiceRequests, start, end});
    return res.data;
}

export const deletePublicScene = async (id: number) => {
    const res = await api.delete(`/admin/public/scene/${id}`);
    return res.data;
}