import { useSuspenseQuery } from "@tanstack/react-query";
import { getPrivateFirstScene } from "../api/privateScene.ts";

const usePrivateFirstScene = (memberId?: string) => {
    return useSuspenseQuery({
        queryKey: ["usePrivateFirstScene", memberId],
        queryFn: () => getPrivateFirstScene(memberId),
    });
};

export default usePrivateFirstScene;
