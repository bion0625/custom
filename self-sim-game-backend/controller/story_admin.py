# controller/story_admin.py
from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, update, delete
from deps import get_db
from models import Scene

router = APIRouter(
    prefix="/admin/story",
    tags=["admin", "story"],
)

# Pydantic 스키마
class Choice(BaseModel):
    text: str
    next: str

class SceneCreate(BaseModel):
    id: str = Field(..., description="씬 아이디(고유값)")
    speaker: str
    bg: Optional[str] = Field(None, description="배경 이미지 파일명")
    text: str
    choices: List[Choice]
    end: bool = False

class SceneUpdate(BaseModel):
    speaker: Optional[str]
    bg: Optional[str]
    text: Optional[str]
    choices: Optional[List[Choice]]
    end: Optional[bool]

class SceneOut(SceneCreate):
    pass


@router.get("/", response_model=List[SceneOut])
async def list_scenes(db: AsyncSession = Depends(get_db)):
    """모든 씬 조회"""
    result = await db.execute(select(Scene))
    scenes = result.scalars().all()
    return [
        SceneOut(
            id=sc.id,
            speaker=sc.speaker,
            bg=sc.bg,
            text=sc.text,
            choices=sc.choices,
            end=sc.end,
        )
        for sc in scenes
    ]


@router.post(
    "/",
    response_model=SceneOut,
    status_code=status.HTTP_201_CREATED,
)
async def create_scene(
    payload: SceneCreate,
    db: AsyncSession = Depends(get_db),
):
    """새 씬 생성"""
    # 중복 체크
    existing = await db.get(Scene, payload.id)
    if existing:
        raise HTTPException(status_code=400, detail="이미 존재하는 scene id입니다.")
    sc = Scene(
        id=payload.id,
        speaker=payload.speaker,
        bg=payload.bg,
        text=payload.text,
        choices=[c.dict() for c in payload.choices],
        end=payload.end,
    )
    db.add(sc)
    await db.commit()
    await db.refresh(sc)
    return SceneOut(**payload.dict())


@router.get("/{scene_id}", response_model=SceneOut)
async def get_scene(
    scene_id: str,
    db: AsyncSession = Depends(get_db),
):
    """단일 씬 조회"""
    sc = await db.get(Scene, scene_id)
    if not sc:
        raise HTTPException(status_code=404, detail="Scene not found")
    return SceneOut(
        id=sc.id,
        speaker=sc.speaker,
        bg=sc.bg,
        text=sc.text,
        choices=sc.choices,
        end=sc.end,
    )


@router.put("/{scene_id}", response_model=SceneOut)
async def update_scene(
    scene_id: str,
    payload: SceneUpdate,
    db: AsyncSession = Depends(get_db),
):
    """씬 수정"""
    sc = await db.get(Scene, scene_id)
    if not sc:
        raise HTTPException(status_code=404, detail="Scene not found")

    # 변경 가능한 필드만 덮어쓰기
    update_data: Dict[str, Any] = payload.dict(exclude_unset=True)
    for key, val in update_data.items():
        setattr(sc, key, val)

    db.add(sc)
    await db.commit()
    await db.refresh(sc)

    return SceneOut(
        id=sc.id,
        speaker=sc.speaker,
        bg=sc.bg,
        text=sc.text,
        choices=sc.choices,
        end=sc.end,
    )


@router.delete("/{scene_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_scene(
    scene_id: str,
    db: AsyncSession = Depends(get_db),
):
    """씬 삭제"""
    sc = await db.get(Scene, scene_id)
    if not sc:
        raise HTTPException(status_code=404, detail="Scene not found")
    await db.delete(sc)
    await db.commit()
    return
