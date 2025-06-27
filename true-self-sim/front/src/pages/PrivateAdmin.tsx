import { useContext, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import useMyStory from "../hook/useMyStory.ts";
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
    const { data, error } = useMyStory();
    const { mutate: saveScene } = usePostMyScene();
    const { mutate: deleteScene } = useDeleteMyScene();
    const { refreshUser, logout } = useContext(AuthContext);

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
    });
    const [useCustomImg, setUseCustomImg] = useState(false);

    const otherSceneAlreadyStart = data?.privateScenes?.some(
        scene => scene.start && scene.sceneId !== currentId
    );

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
        saveScene(request, { onSuccess: () => window.location.reload() });
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
    });

    const handleLogout = async () => {
        await logout();
        await refreshUser();
        navigate("/");
    };

    return (
        <div className="min-h-screen bg-gray-50 p-4 sm:p-6">
            <div className="max-w-5xl mx-auto flex flex-col md:flex-row gap-6">
                <AdminSidebar
                    scenes={data?.privateScenes}
                    currentId={currentId}
                    onSelect={setCurrentId}
                    onBack={() => navigate("/")}
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
