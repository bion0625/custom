import {useSuspenseQuery} from "@tanstack/react-query";
import {getPublicFirstScene} from "../api/publicScene.ts";

const usePublicFirstScene = () => {
    return useSuspenseQuery({
        queryKey: ["usePublicFirstScene"],
        queryFn: getPublicFirstScene,
    })
}

export default usePublicFirstScene;