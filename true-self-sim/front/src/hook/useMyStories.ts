import {useSuspenseQuery} from "@tanstack/react-query";
import {getMyStories} from "../api/myScene.ts";
import type {PrivateStoryInfo} from "../types.ts";

const useMyStories = () => {
    return useSuspenseQuery<PrivateStoryInfo[]>({
        queryKey: ['myStories'],
        queryFn: getMyStories,
    });
};

export default useMyStories;
