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
        <div className="min-h-screen bg-gray-50 p-4 sm:p-6">
            <div className="max-w-5xl mx-auto flex flex-col md:flex-row gap-6">
                <aside className="w-full md:w-1/4 border-b md:border-b-0 md:border-r bg-gray-100 p-4 rounded-md md:rounded-none overflow-auto">
                    <button
                        className="mb-2 w-full py-2 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 transition"
                        onClick={() => navigate('/')}
                    >
                        돌아가기
                    </button>
                    <button
                        className="mb-2 w-full py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition"
                        onClick={async () => {
                            await logout();
                            await refreshUser();
                            navigate('/');
                        }}
                    >
                        로그아웃
                    </button>
                    <ul className="space-y-2">
                        {data?.publicScenes?.map((sc) => (
                            <li key={sc.sceneId}>
                                <button
                                    onClick={() => setCurrentId(sc.sceneId)}
                                    className={`w-full text-left px-2 py-1 rounded ${sc.sceneId === currentId ? 'bg-indigo-200 font-semibold' : 'hover:bg-gray-200'}`}
                                >
                                    <span className="text-sm text-gray-700 font-mono">[{sc.sceneId}]</span> {sc.text}
                                </button>
                            </li>
                        ))}
                    </ul>
                </aside>
                <section className="w-full md:flex-1 bg-white p-4 rounded-md shadow flex flex-col space-y-4 overflow-auto">
                    <div>
                        <label className="block text-sm font-medium">Scene ID</label>
                        <input
                            type="text"
                            className="mt-1 w-full border rounded p-2"
                            value={request.sceneId}
                            onChange={(e) => setRequest((r) => ({ ...r, sceneId: e.target.value }))}
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium">Speaker</label>
                        <input
                            type="text"
                            className="mt-1 w-full border rounded p-2"
                            value={request.speaker}
                            onChange={(e) => setRequest((r) => ({ ...r, speaker: e.target.value }))}
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium">Background Image</label>
                        <input
                            type="text"
                            className="mt-1 w-full border rounded p-2"
                            value={request.backgroundImage}
                            onChange={(e) => setRequest((r) => ({ ...r, backgroundImage: e.target.value }))}
                            placeholder="https://example.com/bg.jpg"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium">Text</label>
                        <textarea
                            className="mt-1 w-full border rounded p-2 h-32"
                            value={request.text}
                            onChange={(e) => setRequest((r) => ({ ...r, text: e.target.value }))}
                        />
                    </div>
                    <div className="flex space-x-2">
                        <button className="px-4 py-2 bg-blue-600 text-white rounded-lg" onClick={handleSave}>
                            저장
                        </button>
                        {currentId && (
                            <button className="px-4 py-2 bg-red-600 text-white rounded-lg" onClick={handleDelete}>
                                삭제
                            </button>
                        )}
                    </div>
                </section>
            </div>
        </div>
    );
};

export default PrivateAdmin;
