import {useMutation} from "@tanstack/react-query";
import type {PublicSceneRequest} from "../types.ts";
import {putPublicScene} from "../api.ts";

const usePutPublicScene = () => {
    return useMutation({
        mutationFn: (data: PublicSceneRequest) => putPublicScene(data)
    })
}
export default usePutPublicScene