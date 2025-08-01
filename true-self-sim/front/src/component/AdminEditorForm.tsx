import React from 'react';
import { backgroundImgs } from '../constants/backgroundImages';

export interface ChoiceRequest {
    text: string;
    nextSceneId: string | null;
}

export interface SceneRequest {
    sceneId: string;
    speaker: string;
    backgroundImage: string;
    text: string;
    choiceRequests: ChoiceRequest[];
    start: boolean;
    end: boolean;
}

interface AdminEditorFormProps {
    request: SceneRequest;
    setRequest: React.Dispatch<React.SetStateAction<SceneRequest>>;
    scenes: { sceneId: string; text: string }[] | undefined;
    otherSceneAlreadyStart: boolean;
    useCustomImg: boolean;
    setUseCustomImg: React.Dispatch<React.SetStateAction<boolean>>;
    onSave: () => void;
    onDelete?: () => void;
    onReset: () => void;
    errorMsg?: string;
    successMsg?: string;
    disableSave?: boolean;
    disableDelete?: boolean;
}

const AdminEditorForm: React.FC<AdminEditorFormProps> = ({
    request,
    setRequest,
    scenes,
    otherSceneAlreadyStart,
    useCustomImg,
    setUseCustomImg,
    onSave,
    onDelete,
    onReset,
    errorMsg,
    successMsg,
    disableSave,
    disableDelete,
}) => {
    const addChoice = () => {
        setRequest(r => ({
            ...r,
            choiceRequests: [...r.choiceRequests, { text: '', nextSceneId: null }],
        }));
    };

    return (
        <section className="w-full md:flex-1 bg-white p-4 rounded-md shadow flex flex-col space-y-4 overflow-auto">
            <div>
                <label className="block text-sm font-medium">Scene ID</label>
                <input
                    type="text"
                    className="mt-1 w-full border rounded p-2"
                    value={request.sceneId}
                    onChange={(e) => setRequest(r => ({ ...r, sceneId: e.target.value }))}
                    placeholder="예: s01"
                />
            </div>

            <div className="mb-4">
                <label className="block text-sm font-medium">text</label>
                <textarea
                    className="mt-1 w-full border rounded p-2 h-32"
                    placeholder="장면 내용을 입력하세요..."
                    value={request.text}
                    onChange={(e) => setRequest(r => ({ ...r, text: e.target.value }))}
                />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
                <div>
                    <label className="block text-sm font-medium">화자</label>
                    <input
                        type="text"
                        className="mt-1 w-full border rounded p-2"
                        value={request.speaker}
                        onChange={(e) => setRequest(r => ({ ...r, speaker: e.target.value }))}
                    />
                </div>
                <div>
                    <label className="block text-sm font-medium mb-1">배경이미지</label>
                    <label className="inline-flex items-center mb-2 space-x-2">
                        <input
                            type="checkbox"
                            checked={useCustomImg}
                            onChange={(e) => setUseCustomImg(e.target.checked)}
                        />
                        <span>직접 URL 입력</span>
                    </label>
                    <div className="w-full">
                        {useCustomImg ? (
                            <input
                                type="text"
                                className="w-full border rounded p-2"
                                value={request.backgroundImage}
                                onChange={(e) => setRequest(r => ({ ...r, backgroundImage: e.target.value }))}
                                placeholder="https://example.com/bg.jpg"
                            />
                        ) : (
                            <select
                                className="w-full border rounded p-2"
                                value={request.backgroundImage}
                                onChange={(e) => setRequest(r => ({ ...r, backgroundImage: e.target.value }))}
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

            <div className="mb-4">
                <div className="flex justify-between items-center mb-2">
                    <label className="text-sm font-medium">Choice</label>
                    {scenes && scenes.length > 0 && (
                        <button className="text-sm text-green-600" onClick={addChoice}>+ 추가</button>
                    )}
                </div>
                {request.choiceRequests.length === 0 && (
                    <p className="text-sm text-gray-400">선택지를 추가하려면 "+ 추가" 버튼을 누르세요.</p>
                )}
                {request.choiceRequests.map((choice, index) => (
                    <div className="space-y-2" key={index}>
                        <div className="flex flex-col sm:flex-row sm:space-x-2 space-y-2 sm:space-y-0">
                            <input
                                type="text"
                                placeholder="답변 텍스트"
                                className="border rounded p-1 flex-1"
                                value={choice.text}
                                onChange={(e) =>
                                    setRequest(r => ({
                                        ...r,
                                        choiceRequests: r.choiceRequests.map((c, i) => i === index ? { ...c, text: e.target.value } : c),
                                    }))
                                }
                            />
                            <select
                                className="border rounded p-1 flex-1"
                                value={choice.nextSceneId ?? ''}
                                onChange={(e) =>
                                    setRequest(r => ({
                                        ...r,
                                        choiceRequests: r.choiceRequests.map((c, i) => i === index ? { ...c, nextSceneId: e.target.value } : c),
                                    }))
                                }
                            >
                                <option value="" disabled>다음 장면 선택</option>
                                {scenes?.filter(scene => scene.sceneId !== request.sceneId)
                                    .map(scene => (
                                        <option key={scene.sceneId} value={scene.sceneId}>
                                            [{scene.sceneId}] {scene.text.length > 20 ? scene.text.slice(0, 20) + '...' : scene.text}
                                        </option>
                                    ))}
                            </select>
                            <button
                                className="text-red-600 self-center"
                                onClick={() => setRequest(r => ({
                                    ...r,
                                    choiceRequests: r.choiceRequests.filter((_, i) => i !== index),
                                }))}
                            >
                                x
                            </button>
                        </div>
                    </div>
                ))}
            </div>

            <div className="flex flex-col sm:flex-row sm:space-x-4 mb-4">
                {otherSceneAlreadyStart && !request.start && (
                    <p className="text-xs text-gray-500 ml-6 mt-1">
                        시작 장면은 하나만 지정할 수 있습니다.
                    </p>
                )}
                <label className={`inline-flex items-center space-x-2 ${otherSceneAlreadyStart ? 'opacity-50 pointer-events-none' : ''}`}>
                    <input
                        type="checkbox"
                        className="form-checkbox"
                        checked={request.start}
                        onChange={(e) => setRequest(r => ({ ...r, start: e.target.checked }))}
                    />
                    <span>Start Scene</span>
                </label>
                <label className="inline-flex items-center space-x-2">
                    <input
                        type="checkbox"
                        className="form-checkbox"
                        checked={request.end}
                        onChange={(e) => setRequest(r => ({ ...r, end: e.target.checked }))}
                    />
                    <span>End Scene</span>
                </label>
            </div>

            {errorMsg && <div className="text-red-500">{errorMsg}</div>}
            {successMsg && <div className="text-green-600">{successMsg}</div>}

            <div className="flex flex-col sm:flex-row sm:space-x-2 space-y-2 sm:space-y-0">
                <button
                    className="w-full sm:w-auto px-4 py-2 bg-blue-600 text-white rounded-lg"
                    onClick={onSave}
                    disabled={disableSave}
                >
                    저장
                </button>
                {onDelete && (
                    <button
                        className="w-full sm:w-auto px-4 py-2 bg-red-600 text-white rounded-lg"
                        onClick={onDelete}
                        disabled={disableDelete}
                    >
                        삭제
                    </button>
                )}
                <button
                    className="w-full sm:w-auto px-4 py-2 bg-gray-400 text-white rounded-lg"
                    onClick={onReset}
                >
                    입력 초기화
                </button>
            </div>
        </section>
    );
};

export default AdminEditorForm;
