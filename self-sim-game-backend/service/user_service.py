from sqlalchemy.orm import Session
from repository.user import get_user_by_username, create_user
from core.security import verify_password, get_password_hash
from fastapi import HTTPException, status

def authenticate_user(db: Session, username: str, password: str):
    user = get_user_by_username(db, username)
    if not user or not verify_password(password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid credentials"
        )
    return user

def register_user(db: Session, username: str, password: str):
    existing = get_user_by_username(db, username)
    if existing:
        raise HTTPException(status_code=400, detail="Username already exists")
    return create_user(db, username, get_password_hash(password))
