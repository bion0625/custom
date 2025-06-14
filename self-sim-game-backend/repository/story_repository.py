# app/repository/story_repository.py
from pathlib import Path
from sqlalchemy import select, func
from sqlalchemy.ext.asyncio import AsyncSession
from models import Scene
from utils.parser import parse_markdown_to_scene

STORY_DIR = Path("story")

async def count_scenes(session: AsyncSession) -> int:
    return await session.scalar(select(func.count()).select_from(Scene))

async def list_scenes(session: AsyncSession) -> list[Scene]:
    result = await session.execute(select(Scene))
    return result.scalars().all()

async def get_scene_by_id(session: AsyncSession, scene_id: str) -> Scene | None:
    result = await session.execute(select(Scene).where(Scene.id == scene_id))
    return result.scalar_one_or_none()

async def seed_scenes_if_empty(session: AsyncSession):
    cnt = await count_scenes(session)
    if cnt == 0:
        for md_file in STORY_DIR.glob("*.md"):
            md = md_file.read_text(encoding="utf-8")
            sc = parse_markdown_to_scene(md)
            scene = Scene(
                id=sc["id"],
                speaker=sc.get("speaker", ""),
                bg=sc.get("bg", ""),
                text=sc["text"],
                choices=sc["choices"],
                end=sc.get("end", False),
                start=sc.get("start", False),   # ← 추가!
            )
            session.add(scene)
        await session.commit()

async def get_start_scene_id(session: AsyncSession) -> str | None:
    # start 플래그 우선
    result = await session.execute(select(Scene).where(Scene.start == True))
    scene = result.scalar_one_or_none()
    return scene.id if scene else None