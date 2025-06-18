import {useMutation} from "@tanstack/react-query";
import {postPublicScene} from "../api.ts";
import type {PublicSceneRequest} from "../types.ts";

const usePostPublicScene = () => {
    return useMutation({
        mutationFn: (data: PublicSceneRequest) => postPublicScene(data)
    });
}

export default usePostPublicScene;