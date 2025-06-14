# app/controller/log_controller.py
from fastapi import APIRouter, Depends, Body, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession

from deps import get_db, get_current_user
from service.log_service import save_log

router = APIRouter(tags=["log"])

@router.post("/log")
async def create_log(
        payload: dict = Body(...),
        db: AsyncSession = Depends(get_db),
        current_user = Depends(get_current_user),
):
    ts = payload.get("timestamp")
    data = payload.get("log", [])
    scene_id = payload.get("scene_id")  # ✅ 추가
    log_id = await save_log(db, ts, data, current_user.id, scene_id)
    if not log_id:
        raise HTTPException(status_code=500, detail="DB 저장 중 오류 발생")
    return {"status": "ok", "log_id": log_id}

@router.get("/log/last")
async def get_last_log(
        db: AsyncSession = Depends(get_db),
        current_user = Depends(get_current_user),
):
    from models import Log
    from sqlalchemy import select, desc

    result = await db.execute(
        select(Log.scene_id)
        .where(Log.user_id == current_user.id)
        .order_by(desc(Log.timestamp))
        .limit(1)
    )
    scene_id = result.scalar()
    return {"scene_id": scene_id}

@router.delete("/log")
async def delete_user_logs(
        db: AsyncSession = Depends(get_db),
        current_user = Depends(get_current_user),
):
    from models import Log
    from sqlalchemy import delete

    await db.execute(
        delete(Log).where(Log.user_id == current_user.id)
    )
    await db.commit()
    return {"status": "deleted"}