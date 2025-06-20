import {useNavigate} from "react-router-dom";
import {useContext, useEffect, useState} from "react";
import AuthContext from "../context/AuthContext.tsx";
import usePublicFirstScene from "../hook/usePublicFirstScene.ts";
import type {PublicScene} from "../types.ts";
import {getPublicScene} from "../api/publicScene.ts";
import MemoryLog from "../component/MemoryLog.tsx";

const PublicGame: React.FC = () => {
    const navigate = useNavigate();
    const { user, logout, refreshUser} = useContext(AuthContext);
    const { data: firstScene } = usePublicFirstScene();
    const [scene, setScene] = useState<PublicScene>({
        sceneId: 0,
        speaker: "",
        backgroundImage: "",
        text: "",
        texts: [],
        start: false,
        end: false,
    });

    const [log, setLog] = useState<string[]>([])

    useEffect(() => {
        if (firstScene) {
            setScene(firstScene);
        }
    }, [firstScene]);

    // 다음 장면 로드 함수
    const handleNextScene = async (nextSceneId: number, nextText: string) => {

        setLog(log => [`${scene.speaker}: ${scene.text}`, `-> me: ${nextText}`, ...log])

        try {
            const nextScene = await getPublicScene(nextSceneId);
            setScene(nextScene);
        } catch (err) {
            console.error("다음 장면을 불러오는 데 실패했습니다:", err);
        }
    };

    const isExternalUrl = (url: string) => /^https?:\/\//.test(url);

    const bgSrc = isExternalUrl(scene.backgroundImage) ? scene.backgroundImage : `background/${scene.backgroundImage}`;

    return (
        <div className="relative min-h-screen w-full overflow-hidden bg-black text-white flex flex-col md:flex-row">
            <img className="absolute inset-0 w-full h-full object-cover opacity-50" alt="background"
                 src={bgSrc}
            />
            <div className="relative z-10 flex-1 flex flex-col items-center pt-6 px-4 space-y-6 md:items-center md:px-8 lg:px-16 lg:pr-72">
                <div className="w-full max-w-xl flex justify-between items-center">
                    <h1 className="text-2xl md:text-3xl font-bold text-indigo-300">
                        Welcome to the public page!
                    </h1>
                    <div className="flex space-x-4">
                        {user?.isAdmin && (
                            <button className="text-sm md:text-base text-green-400 hover:text-green-600"
                                    onClick={() => navigate("/admin/public")}
                            >
                                관리자
                            </button>
                        )}
                        {!user && (
                            <button className="text-sm md:text-base text-blue-400 hover:text-blue-600"
                                    onClick={() => navigate("/login")}
                            >
                                로그인
                            </button>
                        )}
                        {user && (
                            <button className="text-sm md:text-base text-red-500 hover:text-red-600"
                                    onClick={async () => {
                                        await logout();
                                        await refreshUser();
                                        navigate("/")
                                    }}
                            >
                                로그아웃
                            </button>
                        )}
                    </div>
                </div>
                <div className="bg-white/10 backdrop-blur-md rounded-xl shadow-lg p-6 w-full max-w-xl md:max-w-2xl">
                    <h2 className="text-indigo-300 text-lg md:text-xl mb-2">
                        {scene.speaker}
                    </h2>
                    <p className="text-xl md:text-2xl mb-4">
                        {scene.text}
                    </p>
                    <div className="space-y-2">
                        {scene.texts.map((t, index) => (
                            <button className="block w-full py-2 px-4 bg-indigo-600 hover:bg-indigo-700 rounded-lg text-sm md:text-base"
                                    key={index}
                                    onClick={() => handleNextScene(t.nextPublicSceneId, t.nextText)}
                            >
                                {t.text}
                            </button>
                        ))}
                    </div>
                </div>
            </div>
            <div className="hidden lg:block lg:fixed lg:top-4 lg:right-4 lg:w-64 lg:h-[70vh]">
                <MemoryLog log={log}/>
            </div>
        </div>
    )
}

export default PublicGame;