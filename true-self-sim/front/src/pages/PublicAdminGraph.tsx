import AdminGraph, { GraphScene } from "../component/AdminGraph";
import usePublicStory from "../hook/usePublicStory.ts";
import usePostPublicSceneBulk from "../hook/usePostPublicSceneBulk.ts";
import useDeletePublicScene from "../hook/useDeletePublicScene.ts";

const selectScenes = (data: any): GraphScene[] =>
    data.publicScenes.map((scene: any) => ({
        sceneId: scene.sceneId,
        speaker: scene.speaker,
        backgroundImage: scene.backgroundImage,
        text: scene.text,
        start: scene.start,
        end: scene.end,
        choices: scene.texts.map((t: any) => ({ text: t.text, nextSceneId: t.nextPublicSceneId })),
    }));

const PublicAdminGraph: React.FC = () => (
    <AdminGraph
        useStory={usePublicStory}
        useSaveBulk={usePostPublicSceneBulk}
        useDeleteScene={useDeletePublicScene}
        selectScenes={selectScenes}
        backPath="/admin/public"
    />
);

export default PublicAdminGraph;
