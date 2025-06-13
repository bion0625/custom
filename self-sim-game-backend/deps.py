# deps.py
from typing import Generator
from sqlalchemy.orm import Session
from database import SessionLocal  # 아래에서 설명할 database.py 참고
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from core.security import decode_access_token
from repository.user import get_user_by_username
from schemas.user import TokenData
from models.user import User

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

def get_current_user(
    token: str = Depends(oauth2_scheme),
    db: Session = Depends(get_db),
) -> User:
    payload = decode_access_token(token)
    if payload is None or "sub" not in payload:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token")

    username: str = payload.get("sub")
    user = get_user_by_username(db, username)
    if user is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")

    return user
