import {useMutation} from "@tanstack/react-query";
import type {PublicSceneRequest} from "../types.ts";
import {postMyScene} from "../api/myScene.ts";

const usePostMyScene = () => {
    return useMutation({
        mutationFn: (data: PublicSceneRequest) => postMyScene(data)
    });
}

export default usePostMyScene;
