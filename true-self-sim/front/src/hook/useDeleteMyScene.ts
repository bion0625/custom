import {useMutation} from "@tanstack/react-query";
import {deleteMyScene} from "../api/myScene.ts";

const useDeleteMyScene = () => {
    return useMutation({
        mutationFn: (id: string) => deleteMyScene(id)
    })
}

export default useDeleteMyScene;
