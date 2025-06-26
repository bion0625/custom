import {useMutation} from "@tanstack/react-query";
import type {PrivateSceneRequest} from "../types.ts";
import {postMyScene} from "../api/myScene.ts";

const usePostMyScene = () => {
    return useMutation({
        mutationFn: (data: PrivateSceneRequest) => postMyScene(data)
    });
}

export default usePostMyScene;
