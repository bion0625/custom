import React, { useEffect, useState } from "react";
import { storyData } from "../data/story";
import { Scene } from "../types/Scene";
import { DialogueLog } from "../components/DialogueLog";
import { Retrospective } from "../components/Retrospective";

export const GameEngine: React.FC = () => {
  const [storyMap, setStoryMap] = useState<Record<string, Scene>>({});
  const [currentId, setCurrentId] = useState("scene1");
  const [isLoading, setIsLoading] = useState(true);
  const [log, setLog] = useState<string[]>([]);
  const [isFinished, setIsFinished] = useState(false);

  useEffect(() => {
  fetch("http://localhost:8000/story")
    .then(res => res.json())
    .then(data => {
      setStoryMap(data);
      setIsLoading(false);
    });
  }, []);

  if (isLoading) {
  return <div className="text-white p-8">로딩 중...</div>;
  }

  const scene = storyMap[currentId];

  const handleChoice = (choiceText: string, nextId: string) => {
  setLog(prev => [
    ...prev,
    `${scene.speaker}: ${scene.text}`,
    `→ 나: ${choiceText}`
  ]);

  const nextScene = storyData[nextId];
  if (nextScene?.end) {
    setIsFinished(true);
  }

  setCurrentId(nextId);
  };

  if (isFinished) {
    return <Retrospective log={log} onRestart={() => {
        setCurrentId("scene1");
        setLog([]);
        setIsFinished(false);
    }} />;
    }

  return (
    <div className="relative min-h-screen w-full overflow-hidden bg-black text-white">
      {/* 배경 이미지 */}
      <img
        src={require(`../assets/backgrounds/${scene.bg}`)}
        alt="배경"
        className="absolute inset-0 w-full h-full object-cover opacity-50"
      />

      {/* 중앙 콘텐츠 */}
      <div className="relative z-10 flex flex-col items-center justify-center h-screen px-4 py-8">
        {/* 상징 이미지 */}
        <img
          src={require(`../assets/symbols/${scene.symbol}`)}
          alt="상징"
          className="w-32 h-32 mb-6 opacity-90"
        />

        {/* 대사와 선택지 */}
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
        <DialogueLog log={log} />
      </div>
    </div>
  );
};
