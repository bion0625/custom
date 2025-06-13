# controller/auth_controller.py

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from schemas.user import UserCreate, UserOut, Token, UserLogin
from service.user_service import register_user, authenticate_user
from core.security import create_access_token
from deps import get_current_user, get_db

router = APIRouter()

@router.post("/register", response_model=UserOut)
async def register(
    user: UserCreate,
    db: AsyncSession = Depends(get_db),
):
    return await register_user(db, user.username, user.password)

@router.post("/token", response_model=Token)
async def login(
    user: UserLogin,
    db: AsyncSession = Depends(get_db),
):
    authenticated_user = await authenticate_user(db, user.username, user.password)
    if not authenticated_user:
        raise HTTPException(status_code=400, detail="Invalid credentials")
    token = create_access_token({"sub": authenticated_user.username})
    return {"access_token": token, "token_type": "bearer"}

@router.get("/me", response_model=UserOut)
async def read_current_user(
    current_user: UserOut = Depends(get_current_user),
):
    return current_user
