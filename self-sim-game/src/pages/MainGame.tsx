// src/pages/MainGame.tsx
import React, { useContext, useEffect, useState } from "react";
import { AuthContext } from "../contexts/AuthContext";
import { Scene } from "../types/Scene";
import { DialogueLog } from "../components/DialogueLog";
import { Retrospective } from "../components/Retrospective";

const MainGame: React.FC = () => {
  const { user } = useContext(AuthContext);

  const [storyMap, setStoryMap] = useState<Record<string, Scene>>({});
  const [currentId, setCurrentId] = useState("scene1");
  const [isLoading, setIsLoading] = useState(true);
  const [log, setLog] = useState<string[]>([]);
  const [isFinished, setIsFinished] = useState(false);

  // 1) 스토리 데이터 로드
  useEffect(() => {
    fetch("http://localhost:8000/story")
      .then(res => res.json())
      .then(data => {
        setStoryMap(data);
        setIsLoading(false);
      })
      .catch(() => {
        // 에러 처리 로직
        setIsLoading(false);
      });
  }, []);

  // 2) 로그 전송 (log가 바뀔 때마다)
  useEffect(() => {
    if (log.length === 0) return;
    fetch("http://localhost:8000/log", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        timestamp: new Date().toISOString(),
        log,
      }),
    });
  }, [log]);

  // 조건부 렌더링: 로딩 중
  if (isLoading) {
    return <div className="text-white p-8">로딩 중...</div>;
  }

  // 조건부 렌더링: 게임 종료 후 회고 화면
  if (isFinished) {
    return (
      <Retrospective
        log={log}
        onRestart={() => {
          setCurrentId("scene1");
          setLog([]);
          setIsFinished(false);
        }}
      />
    );
  }

  // 현재 씬 정보
  const scene = storyMap[currentId];

  // 사용자의 선택 처리
  const handleChoice = (choiceText: string, nextId: string) => {
    setLog(prev => [
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
    <div className="relative min-h-screen w-full overflow-hidden bg-black text-white">
      {/* 배경 이미지 */}
      <img
        src={require(`../assets/backgrounds/${scene.bg}`)}
        alt="배경"
        className="absolute inset-0 w-full h-full object-cover opacity-50"
      />

      <div className="relative z-10 flex flex-col items-center pt-6 px-4 space-y-6">
        {/* 환영 인사 + 로그아웃 */}
        <div className="w-full max-w-xl flex justify-between items-center">
          <h1 className="text-2xl font-bold text-indigo-300">
            환영합니다, {user?.username}님!
          </h1>
          <button
            className="text-sm text-red-400 hover:text-red-600"
            onClick={() => {
              localStorage.removeItem("access_token");
              window.location.href = "/login";
            }}
          >
            로그아웃
          </button>
        </div>

        {/* 스토리 콘텐츠 */}
        <div className="bg-white/10 backdrop-blur-md rounded-xl shadow-lg p-6 w-full max-w-xl">
          <h2 className="text-indigo-300 text-lg mb-2">{scene.speaker}</h2>
          <p className="text-xl mb-4">{scene.text}</p>
          <div className="space-y-2">
            {scene.choices.map((choice, idx) => (
              <button
                key={idx}
                onClick={() => handleChoice(choice.text, choice.next)}
                className="block w-full py-2 px-4 bg-indigo-600 hover:bg-indigo-700 rounded-lg"
              >
                {choice.text}
              </button>
            ))}
          </div>
        </div>

        {/* 대화 로그 */}
        <DialogueLog log={log} />
      </div>
    </div>
  );
};

export default MainGame;
