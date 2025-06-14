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
