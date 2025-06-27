import { useEffect, useState} from "react";
import usePublicFirstScene from "../hook/usePublicFirstScene.ts";
import type {PublicScene} from "../types.ts";
import {getPublicScene} from "../api/publicScene.ts";
import MemoryLog from "../component/MemoryLog.tsx";
import FullLog from "../component/FullLog.tsx";
import GameHeader from "../component/GameHeader.tsx";
import { backgroundSrc } from "../utils/url.ts";

const PublicGame: React.FC = () => {
    const { data: firstScene } = usePublicFirstScene();
    const [scene, setScene] = useState<PublicScene>({
        sceneId: "",
        speaker: "",
        backgroundImage: "",
        text: "",
        texts: [],
        start: false,
        end: false,
    });

    const [log, setLog] = useState<string[]>([])
    const [fullLog, setFullLog] = useState<string[]>([])

    const [isFinished, setIsFinished] = useState(false);

    useEffect(() => {
        if (firstScene) {
            setScene(firstScene);
        }
        setIsFinished(firstScene.end)
    }, [firstScene]);

    // 다음 장면 로드 함수
    const handleNextScene = async (nextSceneId: string, nextText: string) => {

        setFullLog(full => [`${scene.speaker}: ${scene.text}`, `-> U: ${nextText}`, ...full])
        setLog(prev => {
            const logEntry = [`${scene.speaker}: ${scene.text}`, `-> U: ${nextText}`, ...prev]
            return logEntry.slice(0, 5)
        })

        try {
            const nextScene = await getPublicScene(nextSceneId);
            setScene(nextScene);
            setIsFinished(nextScene.end)
        } catch (err) {
            console.error("다음 장면을 불러오는 데 실패했습니다:", err);
        }
    };

    const bgSrc = backgroundSrc(scene.backgroundImage);

    return (
        <div className="relative min-h-screen w-full overflow-hidden bg-black text-white flex flex-col md:flex-row">
            <img className="absolute inset-0 w-full h-full object-cover opacity-50" alt="background"
                 src={bgSrc}
            />
            <div className="relative z-10 flex-1 flex flex-col items-center pt-6 px-4 space-y-6 md:items-center md:px-8 lg:px-16 lg:pr-72">
                <GameHeader title="Welcome to the public page!" showLogin logoutRedirect="/" />
                <div className="bg-white/10 backdrop-blur-md rounded-xl shadow-lg p-6 w-full max-w-xl md:max-w-2xl">
                    <h2 className="text-indigo-300 text-lg md:text-xl mb-2">
                        {scene.speaker}
                    </h2>
                    <p className="text-xl md:text-2xl mb-4">
                        {scene.text}
                    </p>
                    <div className="space-y-2">
                        {scene?.texts?.map((t, index) => (
                            <button className="block w-full py-2 px-4 bg-indigo-600 hover:bg-indigo-700 rounded-lg text-sm md:text-base"
                                    key={index}
                                    onClick={() => handleNextScene(t.nextPublicSceneId, t.text)}
                            >
                                {t.text}
                            </button>
                        ))}
                    </div>
                </div>
                {isFinished && (
                    <FullLog
                        log={fullLog}
                        onRestart={() => {
                            setLog([]);
                            setFullLog([]);
                            setIsFinished(false);
                            if (firstScene) setScene(firstScene);
                        }}
                    />
                )}
            </div>
            {!isFinished && (
                <div className="hidden lg:block lg:fixed lg:top-4 lg:right-4 lg:w-64 lg:h-[70vh] overflow-y-auto">
                    <MemoryLog log={log}/>
                </div>
            )}
        </div>
    )
}

export default PublicGame;
