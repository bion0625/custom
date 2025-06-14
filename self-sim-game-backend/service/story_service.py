# app/service/story_service.py
from sqlalchemy.ext.asyncio import AsyncSession
from typing import Dict
from repository.story_repository import (
    list_scenes,
    get_scene_by_id,
    seed_scenes_if_empty,
    get_start_scene_id,
)

async def init_scenes(session: AsyncSession):
    """시드 로직"""
    await seed_scenes_if_empty(session)

async def get_all_scenes(session: AsyncSession) -> Dict[str, dict]:
    scenes = await list_scenes(session)
    return {
        s.id: {
            "id": s.id,
            "speaker": s.speaker,
            "bg": s.bg,
            "text": s.text,
            "choices": s.choices,
            "end": s.end,
        }
        for s in scenes
    }

async def get_scene(session: AsyncSession, scene_id: str) -> dict:
    s = await get_scene_by_id(session, scene_id)
    if not s:
        return None
    return {
        "id": s.id,
        "speaker": s.speaker,
        "bg": s.bg,
        "text": s.text,
        "choices": s.choices,
        "end": s.end,
    }

async def get_initial_scene_id(session: AsyncSession, story_map: dict[str, dict]) -> str:
    # DB에 start=true가 있으면 그걸, 없으면 map[0]
    start_id = await get_start_scene_id(session)
    return start_id if start_id in story_map else next(iter(story_map))

