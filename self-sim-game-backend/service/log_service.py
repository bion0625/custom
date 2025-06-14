# app/service/log_service.py
from sqlalchemy.ext.asyncio import AsyncSession
from repository.log_repository import create_log
from sqlalchemy.exc import SQLAlchemyError

async def save_log(session: AsyncSession, timestamp: str, data: list) -> int:
    try:
        entry = await create_log(session, timestamp, data)
        return entry.id
    except SQLAlchemyError as e:
        # 필요 시 로깅
        raise
