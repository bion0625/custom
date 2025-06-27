import { useSuspenseQuery } from "@tanstack/react-query";
import { getPrivateFirstScene } from "../api/privateScene.ts";

const usePrivateFirstScene = () => {
    return useSuspenseQuery({
        queryKey: ["usePrivateFirstScene"],
        queryFn: getPrivateFirstScene,
    });
};

export default usePrivateFirstScene;
