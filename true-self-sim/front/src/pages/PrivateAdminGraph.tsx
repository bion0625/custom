import AdminGraph from "../component/AdminGraph";
import type { GraphScene } from "../component/AdminGraph";
import useMyStory from "../hook/useMyStory.ts";
import useMyStories from "../hook/useMyStories.ts";
import usePostMySceneBulk from "../hook/usePostMySceneBulk.ts";
import useDeleteMyScene from "../hook/useDeleteMyScene.ts";
import { useEffect, useState } from "react";

const selectScenes = (data: any): GraphScene[] =>
    data.privateScenes.map((scene: any) => ({
        sceneId: scene.sceneId,
        speaker: scene.speaker,
        backgroundImage: scene.backgroundImage,
        text: scene.text,
        start: scene.start,
        end: scene.end,
        choices: scene.texts.map((t: any) => ({ text: t.text, nextSceneId: t.nextPrivateSceneId })),
    }));

const PrivateAdminGraph: React.FC = () => {
    const { data: stories } = useMyStories();
    const [storyId, setStoryId] = useState<number>();

    useEffect(() => {
        if (!storyId && stories && stories.length > 0) setStoryId(stories[0].id);
    }, [stories, storyId]);

    if (!storyId) return null;

    return (
        <div className="p-4">
            <select className="border p-2 mb-4" value={storyId} onChange={e => setStoryId(Number(e.target.value))}>
                {stories?.map(s => (<option key={s.id} value={s.id}>{s.title}</option>))}
            </select>
            <AdminGraph
                useStory={() => useMyStory(storyId)}
                useSaveBulk={() => usePostMySceneBulk(storyId)}
                useDeleteScene={() => useDeleteMyScene(storyId)}
                selectScenes={selectScenes}
                backPath="/my"
            />
        </div>
    );
};

export default PrivateAdminGraph;
