import type {PublicScene} from "../types.ts";
import api from "./api.ts";

export const getPublicFirstScene = async () : Promise<PublicScene> => {
    const res = await api.get<PublicScene>("/public/scene");
    return res.data;
}

export const getPublicScene = async (id: string) : Promise<PublicScene> => {
    const res = await api.get<PublicScene>(`/public/scene/${id}`);
    return res.data;
}