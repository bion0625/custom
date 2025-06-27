import { useContext, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import usePublicStory from "../hook/usePublicStory.ts";
import usePostPublicScene from "../hook/usePostPublicScene.ts";
import useDeletePublicScene from "../hook/useDeletePublicScene.ts";
import usePostPublicSceneBulk from "../hook/usePostPublicSceneBulk.ts";
import AuthContext from "../context/AuthContext.tsx";
import { backgroundImgs } from "../constants/backgroundImages.ts";
import AdminSidebar from "../component/AdminSidebar";
import AdminEditorForm, { SceneRequest } from "../component/AdminEditorForm";
import type { PublicSceneRequest } from "../types.ts";

const emptyRequest: PublicSceneRequest = {
    sceneId: "",
    speaker: "",
    backgroundImage: "",
    text: "",
    choiceRequests: [],
    start: false,
    end: false,
};

const PublicAdmin: React.FC = () => {
    const navigate = useNavigate();
    const { refreshUser, logout } = useContext(AuthContext);
    const { data, error } = usePublicStory();
    const { mutate: postPublicScene, isPending: isPostPending } = usePostPublicScene();
    const { mutate: deletePublicScene, isPending: isDeletePending } = useDeletePublicScene();
    const { mutate: postBulk, isPending: isBulkPending } = usePostPublicSceneBulk();

    if (error) navigate("/login");

    const [request, setRequest] = useState<PublicSceneRequest>(emptyRequest);
    const [currentId, setCurrentId] = useState("");
    const otherSceneAlreadyStart = data?.publicScenes?.some(scene => scene.start && scene.sceneId !== currentId);
    const [useCustomImg, setUseCustomImg] = useState(false);

    useEffect(() => {
        if (currentId === "") return;
        if (data?.publicScenes) {
            const currentScene = data.publicScenes.find(scene => currentId ? scene.sceneId === currentId : scene.start);
            setCurrentId(() => currentScene?.sceneId ?? "");
            setRequest({
                sceneId: currentId,
                speaker: currentScene ? currentScene.speaker : "",
                backgroundImage: currentScene ? currentScene.backgroundImage : "",
                text: currentScene ? currentScene.text : "",
                choiceRequests: currentScene?.texts ? currentScene.texts.map(ts => ({
                    nextSceneId: ts.nextPublicSceneId,
                    text: ts.text,
                })) : [],
                start: currentScene ? currentScene.start : false,
                end: currentScene ? currentScene.end : false,
            });
        } else {
            setRequest(emptyRequest);
        }
    }, [currentId, data]);

    const [errorMsg, setErrorMsg] = useState("");
    const [successMsg, setSuccessMsg] = useState("");

    const [bulkPreview, setBulkPreview] = useState<PublicSceneRequest[]>([]);
    const [showBulkModal, setShowBulkModal] = useState(false);
    const fileInputRef = useRef<HTMLInputElement>(null);

    const createNew = () => {
        setCurrentId("");
        setRequest(emptyRequest);
    };

    const handleSceneDelete = () => {
        if (!confirm("정말로 이 장면을 삭제하시겠습니까?")) return;
        deletePublicScene(currentId, {
            onSuccess: () => {
                setSuccessMsg("장면 삭제에 성공했습니다.");
                window.location.reload();
            }
        });
    };

    const handleSceneSave = () => {
        if (!request.speaker.trim() || !request.backgroundImage.trim() || !request.text.trim()) {
            setErrorMsg("모든 필수 항목을 입력해주세요.");
            return;
        }
        if (request.choiceRequests.length > 0 && request.choiceRequests.some(cr => !cr.text.trim() || !cr.nextSceneId)) {
            setErrorMsg("모든 필수 항목을 입력해주세요.");
            return;
        }
        const publicSceneRequest: PublicSceneRequest = {
            sceneId: request.sceneId,
            speaker: request.speaker,
            backgroundImage: request.backgroundImage,
            text: request.text,
            choiceRequests: request.choiceRequests.map(cr => ({ nextSceneId: cr.nextSceneId, text: cr.text })),
            start: request.start,
            end: request.end,
        };
        postPublicScene({ ...publicSceneRequest, sceneId: request.sceneId }, {
            onError: () => setErrorMsg("장면 저장에 실패했습니다."),
            onSuccess: () => {
                setSuccessMsg("장면 저장에 성공했습니다.");
                window.location.reload();
            }
        });
    };

    function handleFileSelect(e: React.ChangeEvent<HTMLInputElement>) {
        const input = e.currentTarget;
        const file = input.files?.[0];
        if (!file) return;
        if (!file.name.toLowerCase().endsWith(".json")) {
            setErrorMsg("JSON 파일만 업로드할 수 있습니다.");
            return;
        }
        const reader = new FileReader();
        reader.onload = (evt) => {
            try {
                const parsed: PublicSceneRequest[] = JSON.parse(evt.target?.result as string);
                const invalid = parsed.find((p) => !p.speaker?.trim() || !p.backgroundImage?.trim() || !p.text?.trim() || !p.sceneId?.trim());
                if (invalid) throw new Error("필수값 누락 행이 있습니다.");
                if (parsed.filter((p) => p.start).length > 1) {
                    throw new Error("start=true 장면은 하나만 있어야 합니다.");
                }
                setBulkPreview(parsed);
                setShowBulkModal(true);
                setErrorMsg("");
                input.value = "";
            } catch (err: unknown) {
                const message = err instanceof Error ? err.message : "JSON 파싱오류";
                setBulkPreview([]);
                setErrorMsg(message);
            }
        };
        reader.readAsText(file);
    }

    const closeModal = () => {
        setShowBulkModal(false);
        setBulkPreview([]);
        fileInputRef.current!.value = "";
    };

    const reset = () => setRequest(emptyRequest);

    const handleLogout = async () => {
        await logout();
        await refreshUser();
        navigate("/");
    };

    return (
        <div className="min-h-screen bg-gray-50 p-4 sm:p-6">
            <div className="max-w-6xl mx-auto flex flex-col md:flex-row gap-6">
                <AdminSidebar
                    scenes={data?.publicScenes}
                    currentId={currentId}
                    onSelect={(id) => { setSuccessMsg(""); setCurrentId(id); }}
                    onBack={() => navigate("/")}
                    onLogout={handleLogout}
                    onCreate={createNew}
                    onGraph={() => navigate("/admin/public/graph")}
                >
                    <input type="file" ref={fileInputRef} accept=".json,application/json" className="hidden" id="bulkFile" onChange={handleFileSelect} />
                    <label htmlFor="bulkFile" className="mb-2 w-full py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition block text-center">
                        Bulk 업로드
                    </label>
                </AdminSidebar>
                <AdminEditorForm
                    request={request as SceneRequest}
                    setRequest={setRequest as React.Dispatch<React.SetStateAction<SceneRequest>>}
                    scenes={data?.publicScenes}
                    otherSceneAlreadyStart={!!otherSceneAlreadyStart}
                    useCustomImg={useCustomImg}
                    setUseCustomImg={setUseCustomImg}
                    onSave={handleSceneSave}
                    onDelete={currentId !== "" ? handleSceneDelete : undefined}
                    onReset={reset}
                    errorMsg={errorMsg}
                    successMsg={successMsg}
                    disableSave={isPostPending}
                    disableDelete={isDeletePending}
                />
                {showBulkModal && (
                    <>
                        <div className="fixed inset-0 bg-black/40 backdrop-blur-sm z-40" onClick={() => setShowBulkModal(false)} />
                        <div className="fixed inset-0 z-50 flex items-center justify-center p-4" aria-modal="true" role="dialog">
                            <div className="bg-white rounded-2xl shadow-xl w-full max-w-3xl max-h-[80vh] overflow-auto p-6 relative">
                                <button className="absolute top-4 right-4 text-gray-500 hover:text-gray-700" onClick={closeModal}>✕</button>
                                <h3 className="text-lg font-semibold mb-4">미리보기 ({bulkPreview.length})</h3>
                                <ul className="space-y-4 mb-4">
                                    {bulkPreview.map((sc, idx) => (
                                        <li key={idx} className="border-b pb-2">
                                            <div className="flex items-center mb-1">
                                                <span className="text-indigo-600 font-mono mr-2">[{sc.speaker}]</span>
                                                <span className="font-medium">{sc.text}</span>
                                            </div>
                                            {sc.choiceRequests.length > 0 && (
                                                <ul className="ml-6 list-disc list-inside text-sm space-y-1">
                                                    {sc.choiceRequests.map((choice, cidx) => (
                                                        <li key={cidx}>{choice.text}<span className="text-gray-500 ml-1">(→ {choice.nextSceneId})</span></li>
                                                    ))}
                                                </ul>
                                            )}
                                        </li>
                                    ))}
                                </ul>
                                <button
                                    className="w-full py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:opacity-50"
                                    disabled={isBulkPending}
                                    onClick={() => postBulk(bulkPreview, {
                                        onSuccess: () => { setSuccessMsg("벌크 저장 완료!"); window.location.reload(); },
                                        onError: () => setErrorMsg("벌크 저장 실패"),
                                    })}
                                >
                                    {isBulkPending ? "저장중…" : "한꺼번에 저장"}
                                </button>
                            </div>
                        </div>
                    </>
                )}
            </div>
        </div>
    );
};

export default PublicAdmin;
