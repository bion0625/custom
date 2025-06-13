from fastapi import FastAPI, Body, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pathlib import Path
import yaml
import re
import os
import json
from fastapi.security import OAuth2PasswordRequestForm
from auth import authenticate_user, create_access_token
from controller import auth_controller

app = FastAPI()
app.include_router(auth_controller.router)

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

@app.post("/token")
def login(form_data: OAuth2PasswordRequestForm = Depends()):
    user = authenticate_user(form_data.username, form_data.password)
    if not user:
        raise HTTPException(status_code=400, detail="Invalid credentials")
    token = create_access_token({"sub": user["username"]})
    return {"access_token": token, "token_type": "bearer"}

@app.get("/story")
def get_all_story():
    scenes = {}
    for md_file in STORY_DIR.glob("*.md"):
        md_text = md_file.read_text(encoding="utf-8")
        scene = parse_markdown_to_scene(md_text)
        scenes[scene["id"]] = scene
    return scenes

@app.get("/story/{scene_id}")
def get_scene(scene_id: str):
    md_path = STORY_DIR / f"{scene_id}.md"
    if not md_path.exists():
        return {}
    
    md_text = md_path.read_text(encoding="utf-8")
    return parse_markdown_to_scene(md_text)

@app.post("/log")
def save_log(payload: dict = Body(...)):
    timestamp = payload.get("timestamp")
    log = payload.get("log", [])

    os.makedirs("logs", exist_ok=True)  # ✅ 디렉토리 자동 생성

    # 파일 이름에 사용할 수 없는 문자 제거 (특히 ':' → '_')
    safe_timestamp = timestamp.replace(":", "_")

    with open(f"logs/{safe_timestamp}.json", "w", encoding="utf-8") as f:
        json.dump(log, f, ensure_ascii=False, indent=2)

    return {"status": "ok"}