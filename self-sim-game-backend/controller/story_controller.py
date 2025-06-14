# app/controller/story_controller.py
from typing import Dict
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from deps import get_db
from service.story_service import get_all_scenes, get_scene, get_initial_scene_id

router = APIRouter(tags=["story"])

@router.get("/story", response_model=Dict[str, dict])
async def read_stories(db: AsyncSession = Depends(get_db)):
    return await get_all_scenes(db)

@router.get("/story/start")
async def read_start(db: AsyncSession = Depends(get_db)):
    story_map = await get_all_scenes(db)
    start_id = await get_initial_scene_id(db, story_map)
    return {"startId": start_id}

@router.get("/story/{scene_id}")
async def read_scene(scene_id: str, db: AsyncSession = Depends(get_db)):
    s = await get_scene(db, scene_id)
    if s is None:
        raise HTTPException(status_code=404, detail="Scene not found")
    return s
