from fastapi import APIRouter, Depends, HTTPException, Body
from pathlib import Path
from typing import List
from deps import get_current_admin

STORY_DIR = Path("story")
router = APIRouter(
    prefix="/admin/story",
    tags=["admin-story"],
    dependencies=[Depends(get_current_admin)],  # ← get_current_user → get_current_admin
)

@router.get("/", response_model=List[str])
async def list_scenes():
    return [p.stem for p in STORY_DIR.glob("*.md")]

@router.get("/{scene_id}", response_model=str)
async def get_raw_markdown(scene_id: str):
    path = STORY_DIR / f"{scene_id}.md"
    if not path.exists():
        raise HTTPException(404, "Scene not found")
    return path.read_text(encoding="utf-8")

@router.post("/", status_code=201)
async def upsert_scene(
    scene_id: str = Body(...),
    content: str = Body(..., embed=True),
):
    safe = scene_id.replace("/", "_")
    path = STORY_DIR / f"{safe}.md"
    path.write_text(content, encoding="utf-8")
    return {"status": "ok", "scene_id": safe}

@router.delete("/{scene_id}")
async def delete_scene(scene_id: str):
    path = STORY_DIR / f"{scene_id}.md"
    if not path.exists():
        raise HTTPException(404, "Scene not found")
    path.unlink()
    return {"status": "deleted"}
