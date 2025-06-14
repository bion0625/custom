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

  // 1) 스토리 맵 불러오기
  useEffect(() => {
    api
      .get<Record<string, Scene>>("/story")
      .then((res) => {
        setStoryMap(res.data);
      })
      .catch((err) => {
        console.error("스토리 로드 실패:", err);
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, []);

  // 2) 로딩 끝나면 currentId를 첫 씬으로 초기화
  useEffect(() => {
    if (!isLoading && Object.keys(storyMap).length > 0) {
      setCurrentId(Object.keys(storyMap)[0]);
    }
  }, [isLoading, storyMap]);

  // 3) 로그가 쌓이면 서버에 저장
  useEffect(() => {
    if (log.length === 0) return;
    api.post("/log", {
      timestamp: new Date().toISOString(),
      log,
    });
  }, [log]);

  // 4) 로딩 중
  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-screen text-white">
        로딩 중...
      </div>
    );
  }

  // 5) 회고 화면
  if (isFinished) {
    return (
      <Retrospective
        log={log}
        onRestart={() => {
          setIsFinished(false);
          setLog([]);
          // 다시 첫 씬으로
          setCurrentId(Object.keys(storyMap)[0]);
        }}
      />
    );
  }

  // 6) 현재 씬 가져오기 & 가드
  const scene = storyMap[currentId];
  if (!scene) {
    return (
      <div className="flex items-center justify-center h-screen text-white">
        씬을 불러오는 중입니다...
      </div>
    );
  }

  // 7) 선택지 핸들러
  const handleChoice = (choiceText: string, nextId: string) => {
    setLog((prev) => [
      ...prev,
      `${scene.speaker}: ${scene.text}`,
      `→ 나: ${choiceText}`,
    ]);
    const nextScene = storyMap[nextId];
    if (nextScene?.end) {
      setIsFinished(true);
    }
    setCurrentId(nextId);
  };

  return (
    <div className="relative min-h-screen w-full overflow-hidden bg-black text-white flex flex-col md:flex-row">
      {/* 배경 이미지 (public/backgrounds 폴더 사용) */}
      <img
        src={require(`../assets/backgrounds/${scene.bg}`)}
        alt="배경"
        className="absolute inset-0 w-full h-full object-cover opacity-50"
      />

      {/* 주요 콘텐츠 */}
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

      {/* 대화 로그 패널 */}
      <div className="hidden lg:block lg:fixed lg:top-4 lg:right-4 lg:w-64 lg:h-[70vh]">
        <DialogueLog log={log} />
      </div>
    </div>
  );
};

export default MainGame;
