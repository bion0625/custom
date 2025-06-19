import {useSuspenseQuery} from "@tanstack/react-query";
import {getPublicStory} from "../api/adminPublic.ts";

const usePublicStory = () => {
    return useSuspenseQuery({
        queryKey: ['publicStory'],
        queryFn: getPublicStory,
    });
}

export default usePublicStory;