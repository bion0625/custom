import {useSuspenseQuery} from "@tanstack/react-query";
import {getMyStory} from "../api/myScene.ts";
import type {PrivateStory} from "../types.ts";

const useMyStory = () => {
    return useSuspenseQuery<PrivateStory>({
        queryKey: ['myStory'],
        queryFn: getMyStory,
    });
}

export default useMyStory;
