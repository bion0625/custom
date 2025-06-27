import AdminGraph from "../component/AdminGraph";
import type { GraphScene } from "../component/AdminGraph";
import useMyStory from "../hook/useMyStory.ts";
import usePostMySceneBulk from "../hook/usePostMySceneBulk.ts";
import useDeleteMyScene from "../hook/useDeleteMyScene.ts";

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

const PrivateAdminGraph: React.FC = () => (
    <AdminGraph
        useStory={useMyStory}
        useSaveBulk={usePostMySceneBulk}
        useDeleteScene={useDeleteMyScene}
        selectScenes={selectScenes}
        backPath="/my"
    />
);

export default PrivateAdminGraph;
