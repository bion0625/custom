import {useSuspenseQuery} from "@tanstack/react-query";
import {getPublicScene} from "../api/publicScene.ts";

const usePublicScene = (id: number) => {
    return useSuspenseQuery({
        queryKey: ["usePublicScene"],
        queryFn: () => getPublicScene(id),
    })
}

export default usePublicScene;