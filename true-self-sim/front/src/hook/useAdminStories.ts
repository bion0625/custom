import { useSuspenseQuery } from "@tanstack/react-query";
import { getAdminStories } from "../api/adminStory.ts";
import type { AdminStoryInfo } from "../types.ts";

const useAdminStories = () => {
    return useSuspenseQuery<AdminStoryInfo[]>({
        queryKey: ['adminStories'],
        queryFn: getAdminStories,
    });
};

export default useAdminStories;
