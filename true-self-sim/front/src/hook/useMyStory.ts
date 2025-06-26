import {useSuspenseQuery} from "@tanstack/react-query";
import {getMyStory} from "../api/myScene.ts";

const useMyStory = () => {
    return useSuspenseQuery({
        queryKey: ['myStory'],
        queryFn: getMyStory,
    });
}

export default useMyStory;
