# app/repository/log_repository.py
from sqlalchemy.ext.asyncio import AsyncSession
from models import Log

async def create_log(session: AsyncSession, timestamp: str, data: list) -> Log:
    entry = Log(timestamp=timestamp, data=data)
    session.add(entry)
    await session.commit()
    await session.refresh(entry)
    return entry
