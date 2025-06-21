import usePublicStory from "../hook/usePublicStory.ts";
import usePostPublicScene from "../hook/usePostPublicScene.ts";
import {useContext, useEffect, useRef, useState} from "react";
import type {PublicSceneRequest} from "../types.ts";
import {useNavigate} from "react-router-dom";
import AuthContext from "../context/AuthContext.tsx";
import useDeletePublicScene from "../hook/useDeletePublicScene.ts";
import usePostPublicSceneBulk from "../hook/usePostPublicSceneBulk.ts";

const PublicAdmin: React.FC = () => {

    const navigate = useNavigate();
    const {refreshUser, logout} = useContext(AuthContext);

    const {data, error} = usePublicStory();

    if (error) navigate("/login");

    const [request, setRequest] = useState<PublicSceneRequest>({
        sceneId: "",
        speaker: "",
        backgroundImage: "",
        text: "",
        choiceRequests: [],
        start: false,
        end: false
    });

    const [currentId, setCurrentId] = useState("");

    const otherSceneAlreadyStart = data?.publicScenes?.some(scene => scene.start && scene.sceneId !== currentId);

    const [useCustomImg, setUseCustomImg] = useState(false);
    const backgroundImgs = [
        "mountain.jpg",
        'loading-background1.png',
        'loading-background2.png',
        'loading-background3.png',
        'loading-background4.png',
    ]

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
                    nextText: ts.nextText,
                })) : [],
                start: currentScene ? currentScene.start : false,
                end: currentScene ? currentScene.end : false,
            })
        } else {
            setRequest({
                sceneId: "",
                speaker: "",
                backgroundImage: "",
                text: "",
                choiceRequests: [],
                start: false,
                end: false
            });
        }
    }, [currentId, data]);

    const [errorMsg, setErrorMsg] = useState("");
    const [successMsg, setSuccessMsg] = useState("");

    const [bulkPreview, setBulkPreview] = useState<PublicSceneRequest[]>([]);
    const {mutate: postBulk, isPending: isBulkPending}
        = usePostPublicSceneBulk();
    const [showBulkModal, setShowBulkModal] = useState(false);
    const fileInputRef = useRef<HTMLInputElement>(null);

    const createNew = () => {
        setCurrentId("")
        setRequest({
            sceneId: "",
            speaker: "",
            backgroundImage: "",
            text: "",
            choiceRequests: [],
            start: false,
            end: false
        })
    }

    const {mutate: postPublicScene, isPending: isPostPending} = usePostPublicScene();
    // const {mutate: putPublicScene, isPending: isPutPending} = usePutPublicScene();
    const {mutate: deletePublicScene, isPending: isDeletePending} = useDeletePublicScene();

    const handleSceneDelete = () => {
        if (!confirm("정말로 이 장면을 삭제하시겠습니까?")) return;
        deletePublicScene(currentId, {
            onSuccess: () => {
                setSuccessMsg("장면 삭제에 성공했습니다.");
                window.location.reload();
            }
        });
    }

    const handleSceneSave = () => {
        if (!request.speaker.trim()
            || !request.backgroundImage.trim()
            || !request.text.trim()) {
            setErrorMsg("모든 필수 항목을 입력해주세요.")
            return;
        }

        if ((request.choiceRequests.length > 0 && request.choiceRequests.some(cr => !cr.text.trim() || !cr.nextSceneId))) {
            setErrorMsg("모든 필수 항목을 입력해주세요.")
            return;
        }

        const publicSceneRequest: PublicSceneRequest = {
            sceneId: request.sceneId,
            speaker: request.speaker,
            backgroundImage: request.backgroundImage,
            text: request.text,
            choiceRequests: request.choiceRequests.map(cr => ({nextSceneId: cr.nextSceneId, text: cr.text})),
            start: request.start,
            end: request.end
        };

        postPublicScene({...publicSceneRequest, sceneId: request.sceneId},
            {
                onError: () => setErrorMsg("장면 저장에 실패했습니다."),
                onSuccess: () => {
                    setSuccessMsg("장면 저장에 성공했습니다.");
                    window.location.reload();
                }
            });
    }

    const addChoice = () => {
        setRequest(r => ({...r, choiceRequests: [...r.choiceRequests, {text: "", nextText: "", nextSceneId: null}]}))
    }

    function handleFileSelect(e: React.ChangeEvent<HTMLInputElement>) {
        const input = e.currentTarget;
        const file = input.files?.[0];
        if (!file) return;

        // 확장자 검사 – 혹시 모르니 한 번 더
        if (!file.name.toLowerCase().endsWith(".json")) {
            setErrorMsg("JSON 파일만 업로드할 수 있습니다.");
            return;
        }

        const reader = new FileReader();
        reader.onload = (evt) => {
            try {
                const parsed: PublicSceneRequest[] = JSON.parse(
                    evt.target?.result as string
                );

                // 최소 검증
                const invalid = parsed.find(
                    (p) =>
                        !p.speaker?.trim() || !p.backgroundImage?.trim() || !p.text?.trim() || !p.sceneId?.trim()
                );
                if (invalid) throw new Error("필수값 누락 행이 있습니다.");

                // start 중복 방지
                if (parsed.filter((p) => p.start).length > 1) {
                    throw new Error("start=true 장면은 하나만 있어야 합니다.");
                }

                setBulkPreview(parsed);
                setShowBulkModal(true);
                setErrorMsg("");
                input.value = "";
            } catch (err: unknown) {
                const message = err instanceof Error ? err.message : "JSON 파싱 오류";
                setBulkPreview([]);
                setErrorMsg(message);
            }
        };
        reader.readAsText(file);
    }

    const closeModal = () => {
        setShowBulkModal(false);
        setBulkPreview([]);      // preview 비우기
        fileInputRef.current!.value = "";  // input 초기화
    };


    return (
        <div className="min-h-screen bg-gray-50 p-4 sm:p-6">
            <div className="max-w-6xl mx-auto flex flex-col md:flex-row gap-6">
                {/*사이드바*/}
                <aside className="w-full md:w-1/4 border-b md:border-b-0 md:border-r bg-gray-100 p-4 rounded-md md:rounded-none overflow-auto h-auto md:h-[80vh]">
                    <button className="mb-2 w-full py-2 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 transition"
                            onClick={() => navigate("/")}
                    >
                        돌아가기
                    </button>
                    <button className="mb-4 w-full py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition"
                            onClick={async () => {
                                await logout();
                                await refreshUser();
                                navigate("/")
                            }}
                    >
                        로그아웃
                    </button>
                    <button className="mb-4 w-full py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition"
                            onClick={createNew}
                    >
                        + 새 장면
                    </button>
                    <input type="file"
                           ref={fileInputRef}
                           accept=".json,application/json"
                           className="hidden" id="bulkFile"
                           onChange={handleFileSelect}
                    />
                    <label htmlFor="bulkFile"
                           className="mb-4 w-full py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition block text-center">
                        Bulk 업로드
                    </label>

                    <ul className="space-y-2">
                        {data?.publicScenes?.map((scene, index) => (
                            <li key={index}>
                                <button onClick={() => {
                                    setSuccessMsg("");
                                    setCurrentId(scene.sceneId);
                                }}
                                        className={`w-full text-left px-2 py-1 rounded
                                        ${scene.sceneId === currentId 
                                            ? "bg-indigo-200 font-semibold" 
                                            : "hover:bg-gray-200"}`
                                }
                                >
                                    <span className="text-sm text-gray-700 font-mono">
                                        [{scene.sceneId}]
                                    </span>
                                    {scene.text}
                                </button>
                            </li>
                        ))}
                    </ul>
                </aside>
                {/*에디터 폼*/}
                <section
                    className="w-full md:flex-1 bg-white p-4 rounded-md shadow flex flex-col space-y-4 overflow-auto h-auto md:h-[80vh]">

                    <div>
                        <label className="block text-sm font-medium">Scene ID</label>
                        <input
                            type="text"
                            className="mt-1 w-full border rounded p-2"
                            value={request.sceneId}
                            onChange={(e) =>
                                setRequest((r) => ({...r, id: e.target.value}))
                            }
                            placeholder="예: intro-1"
                        />
                    </div>

                    {/*TITLE TEXT*/}
                    <div className="mb-4">
                        <label className="block text-sm font-medium">text</label>
                        <textarea className="mt-1 w-full border rounded p-2 h-32"
                                  placeholder="장면 내용을 입력하세요..."
                                  value={request.text}
                                  onChange={(e) =>
                                      setRequest(r => ({...r, text: e.target.value}))}
                        >
                        </textarea>
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
                        <div>
                            <label className="block text-sm font-medium">화자</label>
                            <input type="text" className="mt-1 w-full border rounded p-2"
                                   value={request.speaker}
                                   onChange={(e) =>
                                       setRequest((r) => ({...r, speaker: e.target.value}))}
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium mb-1">배경이미지</label>

                            <label className="inline-flex items-center mb-2 space-x-2">
                                <input type="checkbox"
                                       checked={useCustomImg}
                                       onChange={(e) =>
                                           setUseCustomImg(e.target.checked)}
                                />
                                <span>직접 URL 입력</span>
                            </label>

                            <div className="w-full">
                                {useCustomImg ? (
                                    <input type="text" className="w-full border rounded p-2"
                                           value={request.backgroundImage}
                                           onChange={(e) => setRequest((r) => ({
                                               ...r,
                                               backgroundImage: e.target.value
                                           }))}
                                           placeholder="https://example.com/bg.jpg"
                                    />
                                ) : (
                                    <select className="w-full border rounded p-2"
                                            value={request.backgroundImage}
                                            onChange={(e) => setRequest((r) => ({
                                                ...r,
                                                backgroundImage: e.target.value
                                            }))}
                                    >
                                        <option value="">배경 이미지 선택</option>
                                        {backgroundImgs.map((img, index) => (
                                            <option key={index} value={img}>{img}</option>
                                        ))}
                                    </select>
                                )}
                            </div>
                        </div>
                    </div>

                    {/*CHOICE TEXT*/}
                    <div className="mb-4">
                        <div className="flex justify-between items-center mb-2">
                            <label className="text-sm font-medium">Choice</label>
                            {data?.publicScenes?.length > 0 && (
                                <button className="text-sm text-green-600" onClick={addChoice}>
                                    + 추가
                                </button>
                            )}
                        </div>
                        {request.choiceRequests.length === 0 && (
                            <p className="text-sm text-gray-400">선택지를 추가하려면 "+ 추가" 버튼을 누르세요.</p>
                        )}
                        {request.choiceRequests.map((choice, index) => (
                            <div className="space-y-2" key={index}>
                                <div className="flex flex-col sm:flex-row sm:space-x-2 space-y-2 sm:space-y-0">
                                    <input type="text" placeholder="답변 텍스트" className="border rounded p-1 flex-1"
                                           value={choice.text}
                                           onChange={(e) =>
                                               setRequest(r => ({
                                                   ...r,
                                                   choiceRequests: r.choiceRequests.map((c, i) => i === index ? {
                                                       ...c,
                                                       text: e.target.value
                                                   } : c)
                                               }))}
                                    />
                                    <select className="border rounded p-1 flex-1"
                                            value={choice.nextSceneId ?? ""}
                                            onChange={(e) =>
                                                setRequest(r => ({
                                                    ...r,
                                                    choiceRequests: r.choiceRequests.map((c, i) => i === index ? {
                                                        ...c,
                                                        nextSceneId: e.target.value
                                                    } : c)
                                                }))}
                                    >
                                        <option value="" disabled>다음 장면 선택</option>
                                        {data?.publicScenes?.filter(scene => scene.sceneId !== currentId)
                                            .map(scene => (
                                                <option key={scene.sceneId} value={scene.sceneId}>
                                                    [{scene.sceneId}] {scene.text.length > 20 ? scene.text.slice(0, 20) + "..." : scene.text}
                                                </option>
                                            ))}
                                    </select>
                                    <button className="text-red-600 self-center"
                                            onClick={() => setRequest(r => ({
                                                ...r,
                                                choiceRequests: r.choiceRequests.filter((_, i) => i !== index)
                                            }))}
                                    >
                                        x
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>

                    {/*FLAG 스타트(하나만) or 엔드*/}
                    <div className="flex flex-col sm:flex-row sm:space-x-4 mb-4">
                        {otherSceneAlreadyStart && !request.start && (
                            <p className="text-xs text-gray-500 ml-6 mt-1">
                                시작 장면은 하나만 지정할 수 있습니다.
                            </p>
                        )}
                        <label
                            className={`inline-flex items-center space-x-2 ${otherSceneAlreadyStart ? "opacity-50 pointer-events-none" : ""}`}>
                            <input type="checkbox" className="form-checkbox"
                                   checked={request.start}
                                   onChange={(e) =>
                                       setRequest(r => ({...r, start: e.target.checked}))}
                            />
                            <span>Start Scene</span>
                        </label>
                        <label className="inline-flex items-center space-x-2">
                            <input type="checkbox" className="form-checkbox"
                                   checked={request.end}
                                   onChange={(e) =>
                                       setRequest(r => ({...r, end: e.target.checked}))}
                            />
                            <span>End Scene</span>
                        </label>
                    </div>

                    {errorMsg && <div className="text-red-500">{errorMsg}</div>}
                    {successMsg && <div className="text-green-600">{successMsg}</div>}

                    {/*버튼*/}
                    <div className="flex flex-col sm:flex-row sm:space-x-2 space-y-2 sm:space-y-0">
                        <button className="w-full sm:w-auto px-4 py-2 bg-blue-600 text-white rounded-lg"
                                onClick={handleSceneSave}>
                            {isPostPending ? '저장중...' : '저장'}
                        </button>
                        {currentId !== "" && (
                            <button className="w-full sm:w-auto px-4 py-2 bg-red-600 text-white rounded-lg"
                                    onClick={handleSceneDelete}>
                                {isDeletePending ? '삭제중...' : '삭제'}
                            </button>
                        )}
                        <button
                            className="w-full sm:w-auto px-4 py-2 bg-gray-400 text-white rounded-lg"
                            onClick={() => setRequest({
                                sceneId: "",
                                speaker: "",
                                backgroundImage: "",
                                text: "",
                                choiceRequests: [],
                                start: false,
                                end: false
                            })}
                        >
                            입력 초기화
                        </button>
                    </div>
                </section>
                {/* centered modal */}
                {showBulkModal && (
                    <>
                        {/* overlay */}
                        <div
                            className="fixed inset-0 bg-black/40 backdrop-blur-sm z-40"
                            onClick={() => setShowBulkModal(false)}
                        />

                        {/* modal */}
                        <div
                            className="fixed inset-0 z-50 flex items-center justify-center p-4"
                            aria-modal="true"
                            role="dialog"
                        >
                            <div
                                className="bg-white rounded-2xl shadow-xl w-full max-w-3xl max-h-[80vh] overflow-auto p-6 relative"
                            >
                                <button
                                    className="absolute top-4 right-4 text-gray-500 hover:text-gray-700"
                                    onClick={closeModal}
                                >
                                    ✕
                                </button>

                                <h3 className="text-lg font-semibold mb-4">
                                    미리보기 ({bulkPreview.length})
                                </h3>

                                <ul className="space-y-4 mb-4">
                                    {bulkPreview.map((sc, idx) => (
                                        <li key={idx} className="border-b pb-2">
                                            {/* 씬 메인 텍스트 */}
                                            <div className="flex items-center mb-1">
                                                <span className="text-indigo-600 font-mono mr-2">
                                                    [{sc.speaker}]
                                                </span>
                                                <span className="font-medium">{sc.text}</span>
                                            </div>
                                            {/* choiceRequests 렌더 */}
                                            {sc.choiceRequests.length > 0 && (
                                                <ul className="ml-6 list-disc list-inside text-sm space-y-1">
                                                    {sc.choiceRequests.map((choice, cidx) => (
                                                        <li key={cidx}>
                                                            {choice.text}
                                                            <span className="text-gray-500 ml-1">(→ {choice.nextSceneId})</span>
                                                        </li>
                                                    ))}
                                                </ul>
                                            )}
                                        </li>
                                    ))}
                                </ul>

                                <button
                                    className="w-full py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:opacity-50"
                                    disabled={isBulkPending}
                                    onClick={() =>
                                        postBulk(bulkPreview, {
                                            onSuccess: () => {
                                                setSuccessMsg("벌크 저장 완료!");
                                                window.location.reload();
                                            },
                                            onError: () => setErrorMsg("벌크 저장 실패"),
                                        })
                                    }
                                >
                                    {isBulkPending ? "저장중…" : "한꺼번에 저장"}
                                </button>
                            </div>
                        </div>
                    </>
                )}
            </div>
        </div>
    )
}

export default PublicAdmin;