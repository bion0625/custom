from fastapi import APIRouter, Depends, Body
from sqlalchemy.orm import Session
from schemas.user import UserCreate, UserOut, Token
from service import user_service
from core.security import create_access_token
from deps import get_current_user, get_db  # DB 세션 종속성
from models.user import User
from schemas.user import UserLogin

router = APIRouter()

@router.post("/register", response_model=UserOut)
def register(user: UserCreate, db: Session = Depends(get_db)):
    return user_service.register_user(db, user.username, user.password)

@router.post("/token", response_model=Token)
def login(user: UserLogin, db: Session = Depends(get_db)):
    authenticated_user = user_service.authenticate_user(db, user.username, user.password)
    token = create_access_token({"sub": authenticated_user.username})
    return {"access_token": token, "token_type": "bearer"}

@router.get("/me", response_model=UserOut)
def read_current_user(current_user: User = Depends(get_current_user)):
    return current_user
