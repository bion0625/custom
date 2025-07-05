import type {PublicScene} from "../types.ts";
import api from "./api.ts";

export const getPublicFirstScene = async () : Promise<PublicScene> => {
    const res = await api.get<PublicScene>("/public/scene");
    return res.data;
}

export const getPublicScene = async (id: string, choice?: string) : Promise<PublicScene> => {
    const url = choice ? `/public/scene/${id}?choice=${encodeURIComponent(choice)}` : `/public/scene/${id}`;
    const res = await api.get<PublicScene>(url);
    return res.data;
}