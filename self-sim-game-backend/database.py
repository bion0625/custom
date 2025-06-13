# database.py
from sqlalchemy.orm import sessionmaker
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
import os

SQLALCHEMY_DATABASE_URL = os.getenv(
    "DATABASE_URL",
    "postgresql+asyncpg://postgres_user:postgres_password@localhost:5432/self_sim_game"
)
engine = create_async_engine(
    SQLALCHEMY_DATABASE_URL,  # postgresql+asyncpg://...
    echo=True,
)

SessionLocal = sessionmaker(bind=engine, class_=AsyncSession, autocommit=False, expire_on_commit=False)
