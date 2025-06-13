# app/db/init_db.py

import asyncio
from database import engine
from models import Base

async def init_models():
    # async 컨텍스트 안에서 sync DDL 호출을 래핑
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

if __name__ == "__main__":
    asyncio.run(init_models())
