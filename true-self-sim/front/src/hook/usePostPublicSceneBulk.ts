import {useMutation} from "@tanstack/react-query";
import type {PublicSceneRequest} from "../types.ts";
import {postPublicSceneBulk} from "../api/adminPublic.ts";

const usePostPublicSceneBulk = () => {
    return useMutation({
        mutationFn: (data: PublicSceneRequest[]) => postPublicSceneBulk(data)
    });
}

export default usePostPublicSceneBulk;