import useMyStory from "../hook/useMyStory.ts";
import usePostMyScene from "../hook/usePostMyScene.ts";
import useDeleteMyScene from "../hook/useDeleteMyScene.ts";
import {useContext, useEffect, useState} from "react";
import type {PublicSceneRequest} from "../types.ts";
import {useNavigate} from "react-router-dom";
import AuthContext from "../context/AuthContext.tsx";

const PrivateAdmin: React.FC = () => {
    const navigate = useNavigate();
    const {data, error} = useMyStory();
    const {mutate: saveScene} = usePostMyScene();
    const {mutate: deleteScene} = useDeleteMyScene();
    const {refreshUser, logout} = useContext(AuthContext);

    if (error) navigate("/login");

    const [currentId, setCurrentId] = useState("");
    const [request, setRequest] = useState<PublicSceneRequest>({
        sceneId: "",
        speaker: "",
        backgroundImage: "",
        text: "",
        choiceRequests: [],
        start: false,
        end: false,
    });

    useEffect(() => {
        if (!currentId) return;
        const sc = data?.publicScenes.find(s => s.sceneId === currentId);
        if (sc) {
            setRequest({
                sceneId: sc.sceneId,
                speaker: sc.speaker,
                backgroundImage: sc.backgroundImage,
                text: sc.text,
                choiceRequests: Array.isArray(sc.texts)
                    ? sc.texts.map(t => ({
                        nextSceneId: t.nextPublicSceneId,
                        text: t.text
                    }))
                    : [],
                start: sc.start,
                end: sc.end,
            });
        }
    }, [currentId, data]);

    const handleSave = () => {
        saveScene(request, {onSuccess: () => window.location.reload()});
    };

    const handleDelete = () => {
        if (!currentId) return;
        deleteScene(currentId, {onSuccess: () => window.location.reload()});
    };

    return (
        <div className="p-4">
            <button onClick={() => navigate("/")}>돌아가기</button>
            <button onClick={async () => {await logout();await refreshUser();navigate("/")}}>로그아웃</button>
            <div className="flex">
                <aside className="w-1/4">
                    <ul>
                        {data?.publicScenes?.map(sc => (
                            <li key={sc.sceneId}>
                                <button onClick={() => setCurrentId(sc.sceneId)}>{sc.sceneId}</button>
                            </li>
                        ))}
                    </ul>
                </aside>
                <section className="flex-1">
                    <input value={request.sceneId} onChange={e => setRequest(r=>({...r,sceneId:e.target.value}))} />
                    <input value={request.speaker} onChange={e=>setRequest(r=>({...r,speaker:e.target.value}))}/>
                    <input value={request.backgroundImage} onChange={e=>setRequest(r=>({...r,backgroundImage:e.target.value}))}/>
                    <textarea value={request.text} onChange={e=>setRequest(r=>({...r,text:e.target.value}))}/>
                    <button onClick={handleSave}>저장</button>
                    {currentId && <button onClick={handleDelete}>삭제</button>}
                </section>
            </div>
        </div>
    );
};

export default PrivateAdmin;
