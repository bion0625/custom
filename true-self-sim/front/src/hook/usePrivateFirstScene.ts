import { useSuspenseQuery } from "@tanstack/react-query";
import { getPrivateFirstScene } from "../api/privateScene.ts";

const usePrivateFirstScene = (storyId: number, memberId?: string) => {
    return useSuspenseQuery({
        queryKey: ["usePrivateFirstScene", storyId, memberId],
        queryFn: () => getPrivateFirstScene(storyId, memberId),
    });
};

export default usePrivateFirstScene;
