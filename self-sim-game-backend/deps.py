# deps.py
from typing import Generator
from sqlalchemy.orm import Session
from database import SessionLocal  # 아래에서 설명할 database.py 참고
from fastapi import Depends

def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
