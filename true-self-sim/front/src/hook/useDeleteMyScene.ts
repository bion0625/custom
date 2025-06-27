import {useMutation} from "@tanstack/react-query";
import {deleteMyScene} from "../api/myScene.ts";

const useDeleteMyScene = (storyId: number) => {
    return useMutation({
        mutationFn: (id: string) => deleteMyScene(id, storyId)
    })
}

export default useDeleteMyScene;
