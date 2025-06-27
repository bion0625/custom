import {useSuspenseQuery} from "@tanstack/react-query";
import {getMyStory} from "../api/myScene.ts";
import type {PrivateStory} from "../types.ts";

const useMyStory = (storyId: number) => {
    return useSuspenseQuery<PrivateStory>({
        queryKey: ['myStory', storyId],
        queryFn: () => getMyStory(storyId),
    });
}

export default useMyStory;
