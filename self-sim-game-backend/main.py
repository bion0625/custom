# app/main.py
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.ext.asyncio import AsyncSession

from models import Base
from database import engine, AsyncSessionLocal
from deps import get_db
from service.story_service import init_scenes
from controller.auth_controller import router as auth_router
from controller.story_controller import router as story_router
from controller.log_controller import router as log_router
from controller.story_admin_controller import router as admin_router

@asynccontextmanager
async def lifespan(app: FastAPI):
    # 테이블 생성
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    # 초기 데이터 시드
    async with AsyncSessionLocal() as session:
        await init_scenes(session)
    yield
    # (shutdown 로직 필요 시)

app = FastAPI(lifespan=lifespan)
app.include_router(auth_router)
app.include_router(story_router)
app.include_router(log_router)
app.include_router(admin_router)

app.add_middleware(
    CORSMiddleware, allow_origins=["*"], allow_credentials=True,
    allow_methods=["*"], allow_headers=["*"],
)
