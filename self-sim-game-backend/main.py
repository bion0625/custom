from fastapi import FastAPI, Body
from fastapi.middleware.cors import CORSMiddleware
from pathlib import Path
import yaml
import re
import os
import json
from controller import auth_controller
from database import engine
from models import Base
from contextlib import asynccontextmanager
from controller.story_admin import router as admin_story_router

@asynccontextmanager
async def lifespan(app: FastAPI):
    # → Startup 작업: 테이블 생성 등
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    yield
    # → Shutdown 작업: 필요 시 세션 종료나 기타 정리 로직

app = FastAPI(lifespan=lifespan)
app.include_router(auth_controller.router)
app.include_router(admin_story_router)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

STORY_DIR = Path("story")

def parse_markdown_to_scene(md_text: str):
    # YAML 헤더 분리
    match = re.match(r"---\n(.*?)\n---\n(.*)", md_text, re.DOTALL)
    if not match:
        raise ValueError("Invalid markdown format")
    
    yaml_text, content = match.groups()
    meta = yaml.safe_load(yaml_text)

    # 본문 → 텍스트 + 선택지 추출
    lines = content.strip().split("\n")
    text_lines = []
    choices = []

    for line in lines:
        line = line.strip()
        if line.startswith("- "):
            parts = re.split(r"→|->", line[2:], maxsplit=1)
            if len(parts) == 2:
                choices.append({"text": parts[0].strip(), "next": parts[1].strip()})
        elif line:
            text_lines.append(line)

    return {
        **meta,
        "text": " ".join(text_lines),
        "choices": choices
    }

@app.get("/story")
async def get_all_story():
    scenes = {}
    for md_file in STORY_DIR.glob("*.md"):
        md_text = md_file.read_text(encoding="utf-8")
        scene = parse_markdown_to_scene(md_text)
        scenes[scene["id"]] = scene
    return scenes

@app.get("/story/{scene_id}")
async def get_scene(scene_id: str):
    md_path = STORY_DIR / f"{scene_id}.md"
    if not md_path.exists():
        return {}
    
    md_text = md_path.read_text(encoding="utf-8")
    return parse_markdown_to_scene(md_text)

@app.post("/log")
async def save_log(payload: dict = Body(...)):
    timestamp = payload.get("timestamp")
    log = payload.get("log", [])

    os.makedirs("logs", exist_ok=True)  # ✅ 디렉토리 자동 생성

    # 파일 이름에 사용할 수 없는 문자 제거 (특히 ':' → '_')
    safe_timestamp = timestamp.replace(":", "_")

    with open(f"logs/{safe_timestamp}.json", "w", encoding="utf-8") as f:
        json.dump(log, f, ensure_ascii=False, indent=2)

    return {"status": "ok"}