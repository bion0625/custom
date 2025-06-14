# app/service/log_service.py
from sqlalchemy.ext.asyncio import AsyncSession

from models import Log


async def save_log(db: AsyncSession, timestamp: str, data: list[str], user_id: int, scene_id: str | None) -> int | None:
    entry = Log(timestamp=timestamp, data=data, user_id=user_id, scene_id=scene_id)
    db.add(entry)
    try:
        await db.commit()
        await db.refresh(entry)
        return entry.id
    except:
        await db.rollback()
        return None
