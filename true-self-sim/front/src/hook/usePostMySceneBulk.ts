import {useMutation} from "@tanstack/react-query";
import type {PrivateSceneRequest} from "../types.ts";
import {postMySceneBulk} from "../api/myScene.ts";

const usePostMySceneBulk = (storyId: number) => {
    return useMutation({
        mutationFn: (data: PrivateSceneRequest[]) => postMySceneBulk(storyId, data)
    });
};

export default usePostMySceneBulk;
