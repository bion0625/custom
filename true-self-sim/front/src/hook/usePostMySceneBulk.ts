import {useMutation} from "@tanstack/react-query";
import type {PrivateSceneRequest} from "../types.ts";
import {postMySceneBulk} from "../api/myScene.ts";

const usePostMySceneBulk = () => {
    return useMutation({
        mutationFn: (data: PrivateSceneRequest[]) => postMySceneBulk(data)
    });
};

export default usePostMySceneBulk;
