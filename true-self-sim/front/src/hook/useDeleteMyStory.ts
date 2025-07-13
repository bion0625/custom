import { useMutation } from "@tanstack/react-query";
import { deleteMyStory } from "../api/myScene.ts";

const useDeleteMyStory = () => {
    return useMutation({
        mutationFn: (id: number) => deleteMyStory(id)
    });
};

export default useDeleteMyStory;
