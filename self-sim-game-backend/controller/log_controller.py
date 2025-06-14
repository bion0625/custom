# app/controller/log_controller.py
from fastapi import APIRouter, Depends, Body, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from deps import get_db
from service.log_service import save_log

router = APIRouter(tags=["log"])

@router.post("/log")
async def create_log(
    payload: dict = Body(...),
    db: AsyncSession = Depends(get_db)
):
    log_id = await save_log(db, payload.get("timestamp"), payload.get("log", []))
    if not log_id:
        raise HTTPException(status_code=500, detail="DB 저장 중 오류 발생")
    return {"status": "ok", "log_id": log_id}
