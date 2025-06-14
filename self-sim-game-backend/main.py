# app/main.py
import os
import re
import yaml
from pathlib import Path
from contextlib import asynccontextmanager

from fastapi import FastAPI, Body, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy import select, func
from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy.ext.asyncio import AsyncSession

from controller import auth_controller
from controller.story_admin import router as admin_story_router
from database import engine, AsyncSessionLocal
from deps import get_db
from models import Base, Scene, Log

STORY_DIR = Path("story")


def parse_markdown_to_scene(md_text: str):
    match = re.match(r"---\n(.*?)\n---\n(.*)", md_text, re.DOTALL)
    if not match:
        raise ValueError("Invalid markdown format")
    yaml_text, content = match.groups()
    meta = yaml.safe_load(yaml_text)

    lines = content.strip().split("\n")
    text_lines, choices = [], []
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


@asynccontextmanager
async def lifespan(app: FastAPI):
    # 1) 테이블 생성
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

    # 2) 초기 시드: scenes 테이블이 비어 있다면 .md 파일을 모두 읽어서 삽입
    async with AsyncSessionLocal() as session:
        count = await session.scalar(select(func.count()).select_from(Scene))
        if count == 0:
            for md_file in STORY_DIR.glob("*.md"):
                md = md_file.read_text(encoding="utf-8")
                sc = parse_markdown_to_scene(md)
                scene = Scene(
                    id      = sc["id"],
                    speaker = sc.get("speaker", ""),
                    bg      = sc.get("bg", ""),
                    text    = sc["text"],
                    choices = sc["choices"],
                    end     = sc.get("end", False),
                )
                session.add(scene)
            await session.commit()

    yield

    # (Shutdown 시 추가 로직이 필요하면 여기에)


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


@app.get("/story")
async def get_all_story(db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Scene))
    scenes = result.scalars().all()
    return {
        scene.id: {
            "id":      scene.id,
            "speaker": scene.speaker,
            "bg":      scene.bg,       # ← 포함
            "text":    scene.text,
            "choices": scene.choices,
            "end":     scene.end,
        }
        for scene in scenes
    }


@app.get("/story/{scene_id}")
async def get_scene(scene_id: str, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Scene).where(Scene.id == scene_id))
    scene = result.scalar_one_or_none()
    if not scene:
        raise HTTPException(status_code=404, detail="Scene not found")
    return {
        "id":      scene.id,
        "speaker": scene.speaker,
        "bg":      scene.bg,       # ← 포함
        "text":    scene.text,
        "choices": scene.choices,
        "end":     scene.end,
    }


@app.post("/log")
async def save_log(payload: dict = Body(...), db: AsyncSession = Depends(get_db)):
    timestamp = payload.get("timestamp")
    data = payload.get("log", [])
    entry = Log(timestamp=timestamp, data=data)
    db.add(entry)
    try:
        await db.commit()
        await db.refresh(entry)
    except SQLAlchemyError:
        await db.rollback()
        raise HTTPException(status_code=500, detail="DB 저장 중 오류 발생")
    return {"status": "ok", "log_id": entry.id}
