import {useMutation} from "@tanstack/react-query";
import type {PublicSceneRequest} from "../types.ts";
import {postPublicScene} from "../api/adminPublic.ts";

const usePostPublicScene = () => {
    return useMutation({
        mutationFn: (data: PublicSceneRequest) => postPublicScene(data)
    });
}

export default usePostPublicScene;