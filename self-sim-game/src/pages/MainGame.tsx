import React, { useContext, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../contexts/AuthContext";
import { Scene } from "../types/Scene";
import { DialogueLog } from "../components/DialogueLog";
import { Retrospective } from "../components/Retrospective";
import { api } from "../api";

const MainGame: React.FC = () => {
    const { user } = useContext(AuthContext);
    const navigate = useNavigate();

    const [storyMap, setStoryMap] = useState<Record<string, Scene>>({});
    const [currentId, setCurrentId] = useState<string>("");
    const [isLoading, setIsLoading] = useState(true);
    const [log, setLog] = useState<string[]>([]);
    const [isFinished, setIsFinished] = useState(false);
    const [error, setError] = useState<string | null>(null);

    function getInitialId(storyMap: Record<string, Scene>): string {
        const scenes = Object.values(storyMap);
        const start = scenes.find((s) => s.start);
        if (start) return start.id;
        return Object.keys(storyMap)[0];
    }

    useEffect(() => {
        // 전체 스토리, startId, 마지막 log scene_id를 함께 불러옴
        Promise.all([
            api.get<Record<string, Scene>>("/story"),
            api.get<{ startId: string }>("/story/start"),
            api.get<{ scene_id: string | null }>("/log/last"),
        ])
            .then(([storiesRes, startRes, logRes]) => {
                const data = storiesRes.data;
                setStoryMap(data);

                const lastSceneId = logRes.data.scene_id;

                let initial: string;

                if (lastSceneId && data[lastSceneId]) {
                    // 🎯 최근 로그가 있다면 거기서 시작
                    initial = lastSceneId;
                } else if (startRes.data.startId && data[startRes.data.startId]) {
                    // 🎯 start 플래그 있는 씬부터 시작
                    initial = startRes.data.startId;
                } else {
                    // fallback
                    initial = getInitialId(data);
                }

                setCurrentId(initial);
            })
            .catch((err) => {
                console.error(err);
                setError("스토리를 불러오는 중 오류가 발생했습니다.");
            })
            .finally(() => setIsLoading(false));
    }, []);

    // 로그 저장 함수
    const sendLogToServer = () => {
        if (log.length === 0) return;
        api.post("/log", {
            timestamp: new Date().toISOString(),
            log,
            scene_id: currentId,  // ✅ 마지막 씬 ID
        });
    };

    const onRestart = async () => {
        try {
            await api.delete("/log"); // 서버 로그 삭제
        } catch (err) {
            console.error("로그 삭제 실패", err);
        }
        setLog([]);
        setError(null);
        setIsFinished(false);
        setCurrentId(getInitialId(storyMap));
    };

    if (isLoading) {
        return (
            <div className="flex items-center justify-center h-screen text-white">
                로딩 중...
            </div>
        );
    }

    if (error) {
        sendLogToServer();
        return (
            <div className="relative min-h-screen w-full overflow-hidden bg-black text-white flex items-center justify-center">
                <div className="absolute inset-0 bg-black/60" />
                <div
                    className="relative z-10 bg-white/10 backdrop-blur-md rounded-xl shadow-lg p-8 max-w-lg w-full space-y-6">
                    <h2 className="text-2xl font-bold text-red-400">오류가 발생했습니다</h2>
                    <p className="text-lg">{error}</p>
                    <div>
                        <p className="mb-2 font-medium">당신의 여정은 아래와 같습니다:</p>
                        <ul className="list-decimal list-inside space-y-1 text-sm max-h-40 overflow-auto">
                            {log.map((entry, i) => (
                                <li key={i}>{entry}</li>
                            ))}
                        </ul>
                    </div>
                    <button
                        onClick={onRestart}
                        className="w-full py-2 mt-4 bg-indigo-600 hover:bg-indigo-700 rounded-lg text-white"
                    >
                        처음부터 다시 시작
                    </button>
                </div>
            </div>
        );
    }

    if (isFinished) {
        return <Retrospective log={log} onRestart={onRestart} />;
    }


    const scene = storyMap[currentId];
    if (!scene) {
        return (
            <div className="flex items-center justify-center h-screen text-white">
                씬을 불러올 수 없습니다...
            </div>
        );
    }

    const handleChoice = (choiceText: string, nextId: string) => {
        const nextScene = storyMap[nextId];
        if (!nextScene) {
            setError(`다음 씬("${nextId}")을(를) 찾을 수 없습니다.`);
            return;
        }

        const newLog = [
            ...log,
            `${scene.speaker}: ${scene.text}`,
            `→ 나: ${choiceText}`,
        ];

        setLog(newLog);

        // ✅ 선택 즉시 로그 서버에 저장
        api.post("/log", {
            timestamp: new Date().toISOString(),
            log: newLog,
            scene_id: nextId,
        });

        if (nextScene.end) {
            setIsFinished(true);
        }
        setCurrentId(nextId);
    };

    const isExternalUrl = (url: string) => /^https?:\/\//.test(url);

    const bgSrc = isExternalUrl(scene.bg)
        ? scene.bg
        : `/backgrounds/${scene.bg}`;

    return (
        <div className="relative min-h-screen w-full overflow-hidden bg-black text-white flex flex-col md:flex-row">
            <img
                src={bgSrc}
                alt="배경"
                className="absolute inset-0 w-full h-full object-cover opacity-50"
            />
            <div className="relative z-10 flex-1 flex flex-col items-center pt-6 px-4 space-y-6 md:items-center md:px-8 lg:px-16 lg:pr-72">
                <div className="w-full max-w-xl flex justify-between items-center">
                    <h1 className="text-2xl md:text-3xl font-bold text-indigo-300">
                        환영합니다, {user?.username}님!
                    </h1>
                    <div className="flex space-x-4">
                        {user?.is_admin && (
                            <button
                                onClick={() => navigate("/admin")}
                                className="text-sm md:text-base text-green-400 hover:text-green-600"
                            >
                                관리자 페이지
                            </button>
                        )}
                        <button
                            className="text-sm md:text-base text-red-400 hover:text-red-600"
                            onClick={() => {
                                localStorage.removeItem("access_token");
                                window.location.href = "/login";
                            }}
                        >
                            로그아웃
                        </button>
                    </div>
                </div>

                <div className="bg-white/10 backdrop-blur-md rounded-xl shadow-lg p-6 w-full max-w-xl md:max-w-2xl">
                    <h2 className="text-indigo-300 text-lg md:text-xl mb-2">
                        {scene.speaker}
                    </h2>
                    <p className="text-xl md:text-2xl mb-4">{scene.text}</p>
                    <div className="space-y-2">
                        {scene.choices.map((choice, idx) => (
                            <button
                                key={idx}
                                onClick={() => handleChoice(choice.text, choice.next)}
                                className="block w-full py-2 px-4 bg-indigo-600 hover:bg-indigo-700 rounded-lg text-sm md:text-base"
                            >
                                {choice.text}
                            </button>
                        ))}
                    </div>
                </div>
            </div>

            <div className="hidden lg:block lg:fixed lg:top-4 lg:right-4 lg:w-64 lg:h-[70vh]">
                <DialogueLog log={log} />
            </div>
        </div>
    );
};

export default MainGame;
