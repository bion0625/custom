import { useSuspenseQuery } from "@tanstack/react-query";
import { getUserStories } from "../api/privateScene.ts";
import type { PrivateStoryInfo } from "../types.ts";

const useUserStories = (memberId: string, enabled = true) => {
    return useSuspenseQuery<PrivateStoryInfo[]>({
        queryKey: ["userStories", memberId],
        queryFn: () => getUserStories(memberId),
        enabled,
    });
};

export default useUserStories;
