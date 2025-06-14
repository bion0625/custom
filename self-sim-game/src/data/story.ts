import { Scene } from "../types/Scene";

export const storyData: Record<string, Scene> = {
  scene1: {
    id: "scene1",
    speaker: "나",
    text: "나는 왜 반복되는 실수를 멈추지 못할까?",
    bg: "dark-room.jpg",
    symbol: "mirror.png",
    choices: [
      { text: "나는 본질적으로 약한 존재야", next: "scene2_selfdoubt" },
      { text: "나는 아직 나를 완전히 이해하지 못했어", next: "scene2_introspection" }
    ]
  },
  scene2_selfdoubt: {
    id: "scene2_selfdoubt",
    speaker: "또 다른 나",
    text: "약하다는 건 틀렸다는 뜻일까, 아니면 인간적이라는 뜻일까?",
    bg: "dark-room.jpg",
    symbol: "mirror.png",
    choices: [
      { text: "틀렸다는 뜻", next: "scene3_judgement" },
      { text: "인간적이라는 뜻", next: "scene5_final" }
    ]
  },
  scene5_final: {
    id: "scene5_final",
    speaker: "나",
    text: "지금 나는 조금 더 나를 이해하게 되었어.",
    bg: "dark-room.jpg",
    symbol: "sunrise.png",
    end: true, // ✅ 회고 화면으로 전환하는 조건
    choices: []
}
  // 이어지는 scene 생략 가능
};
