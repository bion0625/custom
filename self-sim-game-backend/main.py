from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

# CORS 설정 (React가 localhost:3000에서 접근할 수 있도록)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 개발 중엔 *로 열어도 됨
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 샘플 데이터
story_data = {
    "scene1": {
    "id": "scene1",
    "speaker": "나",
    "text": "나는 왜 반복되는 실수를 멈추지 못할까?",
    "bg": "dark-room.jpg",
    "symbol": "mirror.png",
    "choices": [
      { "text": "나는 본질적으로 약한 존재야", "next": "scene2_selfdoubt" },
      { "text": "나는 아직 나를 완전히 이해하지 못했어", "next": "scene2_introspection" }
    ]
  },
  "scene2_selfdoubt": {
    "id": "scene2_selfdoubt",
    "speaker": "또 다른 나",
    "text": "약하다는 건 틀렸다는 뜻일까, 아니면 인간적이라는 뜻일까?",
    "bg": "dark-room.jpg",
    "symbol": "mirror.png",
    "choices": [
      { "text": "틀렸다는 뜻", "next": "scene3_judgement" },
      { "text": "인간적이라는 뜻", "next": "scene5_final" }
    ]
  },
  "scene5_final": {
    "id": "scene5_final",
    "speaker": "나",
    "text": "지금 나는 조금 더 나를 이해하게 되었어.",
    "bg": "forest.jpg",
    "symbol": "sunrise.png",
    "end": "true",
    "choices": []
}
}

@app.get("/story")
def get_all_story():
    return story_data

@app.get("/story/{scene_id}")
def get_scene(scene_id: str):
    return story_data.get(scene_id, {})
