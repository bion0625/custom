import usePublicStory from "../hook/usePublicStory.ts";
import usePostPublicScene from "../hook/usePostPublicScene.ts";
import {useContext, useEffect, useState} from "react";
import type {PublicSceneRequest} from "../types.ts";
import usePutPublicScene from "../hook/usePutPublicScene.ts";
import {useNavigate} from "react-router-dom";
import AuthContext from "../context/AuthContext.tsx";

const PublicAdmin: React.FC = () => {

    const navigate = useNavigate();
    const {refreshUser, logout} = useContext(AuthContext);

    const {data, isLoading, error} = usePublicStory();

    if (error) navigate("/login");

    const [request, setRequest] = useState<PublicSceneRequest>({
        speaker: "",
        backgroundImage: "",
        text: "",
        choiceRequests: [],
        start: false,
        end: false
    });

    const [currentId, setCurrentId] = useState(0);

    const [useCustomImg, setUseCustomImg] = useState(false);
    const backgroundImgs = [
        "mountain.jpg"
    ]

    useEffect(() => {
        if (currentId === 0) return;
        if (data?.publicScenes) {
            const currentScene = data.publicScenes.find(scene => currentId ? scene.sceneId === currentId : scene.start);
            setCurrentId(() => currentScene?.sceneId ?? 0);
            setRequest({
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

    const createNew = () => {
        const newText = prompt("새 장면의 질문을 입력하세요:");
        if (!newText) return;

        setCurrentId(0)
        setRequest({
            speaker: "",
            backgroundImage: "",
            text: newText,
            choiceRequests: [],
            start: false,
            end: false
        })
    }

    const {mutate: postPublicScene, isPending: isPostPending} = usePostPublicScene();
    const {mutate: putPublicScene, isPending: isPutPending} = usePutPublicScene();

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
            speaker: request.speaker,
            backgroundImage: request.backgroundImage,
            text: request.text,
            choiceRequests: request.choiceRequests.map(cr => ({nextSceneId: cr.nextSceneId ?? 1, text: cr.text})),
            start: request.start,
            end: request.end
        };
        if (currentId === 0) {
            postPublicScene(publicSceneRequest,
                {
                    onError: () => setErrorMsg("장면 저장에 실패했습니다."),
                    onSuccess: () => {
                        setSuccessMsg("장면 저장에 성공했습니다.");
                        window.location.reload();
                    }
                });
        } else {
            putPublicScene({...publicSceneRequest, id: currentId},
                {
                    onError: () => setErrorMsg("장면 저장에 실패했습니다."),
                    onSuccess: () => {
                        setSuccessMsg("장면 수정에 성공했습니다.");
                    }
                });
        }
    }

    const addChoice = () => {
        setRequest(r => ({...r, choiceRequests: [...r.choiceRequests, {text: "", nextText: "", nextSceneId: 1}]}))
    }

    /*TODO 추후 로딩 전체 적용시 제거*/
    if (isLoading) return <div>로딩중</div>

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
                                    {scene.text}
                                </button>
                            </li>
                        ))}
                    </ul>
                </aside>
                {/*에디터 폼*/}
                <section className="w-full md:flex-1 bg-white p-4 rounded-md shadow flex flex-col space-y-4 overflow-auto h-auto md:h-[80vh]">
                    {/*TITLE TEXT*/}
                    <div className="mb-4">
                        <label className="block text-sm font-medium">text</label>
                        <textarea className="mt-1 w-full border rounded p-2 h-32"
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
                                           onChange={(e) => setRequest((r) => ({...r, backgroundImage: e.target.value}))}
                                           placeholder="https://example.com/bg.jpg"
                                    />
                                        ) : (
                                        <select className="w-full border rounded p-2"
                                                value={request.backgroundImage}
                                                onChange={(e) => setRequest((r) => ({...r, backgroundImage: e.target.value}))}
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
                            <button className="text-sm text-green-600" onClick={addChoice}>
                                + 추가
                            </button>
                        </div>
                        {request.choiceRequests.map((choice, index) => (
                            <div className="space-y-2" key={index}>
                                <div className="flex flex-col sm:flex-row sm:space-x-2 space-y-2 sm:space-y-0">
                                    <input type="text" placeholder="답변 텍스트" className="border rounded p-1 flex-1"
                                           value={choice.text}
                                           onChange={(e) =>
                                               setRequest(r => ({...r, choiceRequests: r.choiceRequests.map((c, i) => i === index ? {...c, text: e.target.value} : c)}))}
                                    />
                                    <select className="border rounded p-1 flex-1"
                                            value={choice.nextSceneId ?? 0}
                                            onChange={(e) =>
                                                setRequest(r => ({...r, choiceRequests: r.choiceRequests.map((c, i) => i === index ? {...c, nextSceneId: parseInt(e.target.value)} : c)}))}
                                    >
                                        <option value="" disabled>다음 장면 선택</option>
                                        {currentId !== 1 ? data?.publicScenes?.filter(scene => scene.sceneId !== currentId)
                                            .map(scene => (
                                            <option key={scene.sceneId} value={scene.sceneId}>{scene.text}</option>
                                        )) : data?.publicScenes?.map(scene => (
                                            <option key={scene.sceneId} value={scene.sceneId}>{scene.text}</option>))}
                                    </select>
                                    <button className="text-red-600 self-center"
                                            onClick={() => setRequest(r => ({...r, choiceRequests: r.choiceRequests.filter((_, i) => i !== index)}))}
                                    >
                                        x
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>

                    {/*FLAG 스타트(하나만) or 엔드*/}
                    <div className="flex flex-col sm:flex-row sm:space-x-4 mb-4">
                        <label className="inline-flex items-center space-x-2">
                        {/*비활성화시*/}
                        {/*<label className="inline-flex items-center space-x-2 opacity-50 pointer-events-none">*/}
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
                        <button className="w-full sm:w-auto px-4 py-2 bg-blue-600 text-white rounded-lg" onClick={handleSceneSave}>
                            {isPostPending || isPutPending ? '저장중...' : '저장'}
                        </button>
                        <button className="w-full sm:w-auto px-4 py-2 bg-red-600 text-white rounded-lg">삭제</button>
                    </div>
                </section>
            </div>
        </div>
    )
}

export default PublicAdmin;