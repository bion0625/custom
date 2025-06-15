# tests/conftest.py
import asyncio
from typing import AsyncGenerator

import httpx
import pytest_asyncio
from sqlalchemy.ext.asyncio import (
    AsyncSession,
    async_sessionmaker,
    create_async_engine,
)

from main import app as real_app
from models import Base
from deps import get_db
from db import init_db as init_module          # ← 모듈 자체 import

###############################################################################
# Async in-memory DB                                                          #
###############################################################################
TEST_DB_URL = "sqlite+aiosqlite:///:memory:"
engine = create_async_engine(TEST_DB_URL, connect_args={"check_same_thread": False})
AsyncSessionLocal = async_sessionmaker(engine, expire_on_commit=False, autoflush=False)

# 테이블 생성 + 초기 시드
async def _prepare_db():
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

    # ✅ 프로덕션 전역을 테스트용으로 덮어쓰기
    init_module.engine = engine
    init_module.AsyncSessionLocal = AsyncSessionLocal

    # 이제 SQLite로 시드 실행
    await init_module.init_models()

asyncio.get_event_loop().run_until_complete(_prepare_db())

###############################################################################
# Dependency override                                                         #
###############################################################################
async def override_get_db() -> AsyncGenerator[AsyncSession, None]:
    async with AsyncSessionLocal() as session:
        yield session

real_app.dependency_overrides[get_db] = override_get_db

###############################################################################
# Fixtures                                                                    #
###############################################################################
@pytest_asyncio.fixture(scope="session")
def event_loop():  # type: ignore[override]
    loop = asyncio.new_event_loop()
    yield loop
    loop.close()

@pytest_asyncio.fixture()
async def client():
    transport = httpx.ASGITransport(app=real_app)
    async with httpx.AsyncClient(transport=transport, base_url="http://test") as ac:
        yield ac
