import { useMutation } from "@tanstack/react-query";
import { postMyStory } from "../api/myScene.ts";
import type { PrivateStoryInfo } from "../types.ts";

const usePostMyStory = () => {
    return useMutation<PrivateStoryInfo, unknown, string>({
        mutationFn: (title: string) => postMyStory(title)
    });
};

export default usePostMyStory;
