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
  const [error, setError] = useState<string | null>(null);  // ← 에러 상태

  // MainGame.tsx 상단
  function getInitialId(storyMap: Record<string, Scene>): string {
    const scenes = Object.values(storyMap);
    // 1) start 플래그가 붙은 씬
    const start = scenes.find((s) => s.start);
    if (start) return start.id;
    // 2) 없으면 첫 번째 key
    return Object.keys(storyMap)[0];
  }

  // 1) 스토리 맵 + 시작 ID를 함께 불러오기
  useEffect(() => {
    Promise.all([
      api.get<Record<string, Scene>>("/story"),
      api.get<{ startId: string }>("/story/start"),
    ])
      .then(([storiesRes, startRes]) => {
        setStoryMap(storiesRes.data);
        // startId가 없으면 fallback으로 메타데이터에 start=true인 씬을 찾고, 
        // 그래도 없으면 첫 키를 씁니다.
        const data = storiesRes.data;
        // start API 결과(또는 메타에 있는 start 플래그) 기반으로
        // 항상 getInitialId 하나만 호출
        const initial = startRes.data.startId && data[startRes.data.startId]
          ? startRes.data.startId
          : getInitialId(data);
        setCurrentId(initial);
      })
      .catch((err) => {
        console.error(err);
        setError("스토리를 불러오는 중 오류가 발생했습니다.");
      })
      .finally(() => setIsLoading(false));
  }, []);

  // 3) 로그가 쌓이면 서버에 저장
  useEffect(() => {
    if (log.length === 0) return;
    api.post("/log", {
      timestamp: new Date().toISOString(),
      log,
    });
  }, [log]);

  // ─── 렌더링 가드 ─────────────────────────────────

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-screen text-white">
        로딩 중...
      </div>
    );
  }

  
if (error) {
  return (
    <div className="relative min-h-screen w-full overflow-hidden bg-black text-white flex items-center justify-center">
      {/* 배경 반투명 레이어 (이미지나 컬러) */}
      <div className="absolute inset-0 bg-black/60" />

      {/* 패널: 회고와 동일한 스타일 */}
      <div className="relative z-10 bg-white/10 backdrop-blur-md rounded-xl shadow-lg p-8 max-w-lg w-full space-y-6">
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
          onClick={() => {
            setError(null);
            setLog([]);
            setIsFinished(false);
            setCurrentId(getInitialId(storyMap));
          }}
          className="w-full py-2 bg-indigo-600 hover:bg-indigo-700 rounded-lg text-white"
        >
          처음부터 다시 시작
        </button>
      </div>
    </div>
  );
}

  if (isFinished) {
    return (
      <Retrospective
        log={log}
        onRestart={() => {
          setIsFinished(false);
          setLog([]);
          setCurrentId(getInitialId(storyMap));
        }}
      />
    );
  }

  const scene = storyMap[currentId];
  if (!scene) {
    return (
      <div className="flex items-center justify-center h-screen text-white">
        씬을 불러올 수 없습니다...
      </div>
    );
  }

  // 4) 선택지 핸들러
  const handleChoice = (choiceText: string, nextId: string) => {
    const nextScene = storyMap[nextId];
    if (!nextScene) {
      // 없는 씬으로 넘어가려 하면 에러 처리
      setError(`다음 씬("${nextId}")을(를) 찾을 수 없습니다.`);
      return;
    }

    setLog((prev) => [
      ...prev,
      `${scene.speaker}: ${scene.text}`,
      `→ 나: ${choiceText}`,
    ]);

    if (nextScene.end) {
      setIsFinished(true);
    }
    setCurrentId(nextId);
  };

  return (
    <div className="relative min-h-screen w-full overflow-hidden bg-black text-white flex flex-col md:flex-row">
      {/* 배경 이미지 */}
      <img
        src={`/backgrounds/${scene.bg}`}   // ← public 폴더에서 서빙됨
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
