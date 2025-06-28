import { useContext, useEffect, useState } from "react";
import { useNavigate, Link, useSearchParams } from "react-router-dom";
import useMyStory from "../hook/useMyStory.ts";
import useMyStories from "../hook/useMyStories.ts";
import usePostMyScene from "../hook/usePostMyScene.ts";
import useDeleteMyScene from "../hook/useDeleteMyScene.ts";
import AuthContext from "../context/AuthContext.tsx";
import { backgroundImgs } from "../constants/backgroundImages.ts";
import AdminSidebar from "../component/AdminSidebar";
import AdminEditorForm from "../component/AdminEditorForm";
import type { SceneRequest } from "../component/AdminEditorForm";
import type { PrivateSceneRequest } from "../types.ts";

const PrivateAdmin: React.FC = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const { data: stories } = useMyStories();
    const initial = searchParams.get('storyId');
    const [storyId, setStoryId] = useState<number | undefined>(initial ? Number(initial) : undefined);
    const { data, error } = useMyStory(storyId ?? 0);
    const { mutate: saveScene } = usePostMyScene();
    const { mutate: deleteScene } = useDeleteMyScene(storyId ?? 0);
    const { refreshUser, logout } = useContext(AuthContext);

    const noStories = stories && stories.length === 0;

    if (error) navigate("/login");

    const [currentId, setCurrentId] = useState("");
    const [request, setRequest] = useState<PrivateSceneRequest>({
        sceneId: "",
        speaker: "",
        backgroundImage: "",
        text: "",
        choiceRequests: [],
        start: false,
        end: false,
        storyId: 0,
    });
    const [useCustomImg, setUseCustomImg] = useState(false);

    const otherSceneAlreadyStart = data?.privateScenes?.some(
        scene => scene.start && scene.sceneId !== currentId
    );

    useEffect(() => {
        if (!storyId && stories && stories.length > 0) {
            setStoryId(stories[0].id);
            setRequest(r => ({...r, storyId: stories[0].id}));
        }
    }, [stories, storyId]);

    const createNew = () => {
        setCurrentId("");
        setRequest({
            sceneId: "",
            speaker: "",
            backgroundImage: "",
            text: "",
            choiceRequests: [],
            start: false,
            end: false,
            storyId: storyId ?? 0,
        });
        setUseCustomImg(false);
    };

    useEffect(() => {
        if (!currentId) return;
        const sc = data?.privateScenes.find(s => s.sceneId === currentId);
        if (sc) {
            setRequest({
                sceneId: sc.sceneId,
                speaker: sc.speaker,
                backgroundImage: sc.backgroundImage,
                text: sc.text,
                choiceRequests: Array.isArray(sc.texts)
                    ? sc.texts.map(t => ({ nextSceneId: t.nextPrivateSceneId, text: t.text }))
                    : [],
                start: sc.start,
                end: sc.end,
            });
            setUseCustomImg(!backgroundImgs.includes(sc.backgroundImage));
        }
    }, [currentId, data]);

    const handleSave = () => {
        saveScene({ ...request, storyId: storyId ?? 0 }, { onSuccess: () => window.location.reload() });
    };

    const handleDelete = () => {
        if (!currentId) return;
        deleteScene(currentId, { onSuccess: () => window.location.reload() });
    };

    const reset = () => setRequest({
        sceneId: "",
        speaker: "",
        backgroundImage: "",
        text: "",
        choiceRequests: [],
        start: false,
        end: false,
        storyId: storyId ?? 0,
    });

    const handleLogout = async () => {
        await logout();
        await refreshUser();
        navigate("/");
    };

    if (noStories) {
        return (
            <div className="p-4">No stories found. Create one first. <Link className="text-blue-600 underline" to="/my/stories">Go to list</Link></div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50 p-4 sm:p-6">
            <div className="mb-4">
                <select
                    className="border p-2"
                    value={storyId}
                    onChange={e => { const id = Number(e.target.value); setStoryId(id); setRequest(r=>({...r, storyId:id})); }}
                >
                    {stories?.map(s => (
                        <option key={s.id} value={s.id}>{s.title}</option>
                    ))}
                </select>
            </div>
            <div className="max-w-5xl mx-auto flex flex-col md:flex-row gap-6">
                <AdminSidebar
                    scenes={data?.privateScenes}
                    currentId={currentId}
                    onSelect={setCurrentId}
                    onBack={() => navigate("/my/stories")}
                    onLogout={handleLogout}
                    onCreate={createNew}
                />
                <AdminEditorForm
                    request={request as SceneRequest}
                    setRequest={setRequest as React.Dispatch<React.SetStateAction<SceneRequest>>}
                    scenes={data?.privateScenes}
                    otherSceneAlreadyStart={!!otherSceneAlreadyStart}
                    useCustomImg={useCustomImg}
                    setUseCustomImg={setUseCustomImg}
                    onSave={handleSave}
                    onDelete={currentId ? handleDelete : undefined}
                    onReset={reset}
                />
            </div>
        </div>
    );
};

export default PrivateAdmin;
