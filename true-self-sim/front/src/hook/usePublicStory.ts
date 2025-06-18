import {useQuery} from "@tanstack/react-query";
import {getPublicStory} from "../api.ts";

const usePublicStory = () => {
    return useQuery({
        queryKey: ['publicStory'],
        queryFn: getPublicStory
    });
}

export default usePublicStory;