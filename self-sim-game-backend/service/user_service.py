from repository.user import get_user_by_username, create_user
from core.security import verify_password, get_password_hash
from fastapi import HTTPException
from sqlalchemy.ext.asyncio import AsyncSession

async def authenticate_user(db, username: str, password: str):
    # 1) 코루틴을 await 해서 실제 User 인스턴스를 얻는다
    user = await get_user_by_username(db, username)
    if not user:
        return None

    # 2) 비밀번호 검증 (동기 함수)
    if not verify_password(password, user.hashed_password):
        return None

    return user


async def register_user(
    db: AsyncSession,
    username: str,
    password: str
):
    # 1) 사용자 중복 조회 (await 필수)
    existing = await get_user_by_username(db, username)
    if existing:
        raise HTTPException(status_code=400, detail="Username already exists")

    # 2) create_user 역시 async 로 정의했다면 await
    user = await create_user(
        db,
        username=username,
        password_hash=get_password_hash(password)
    )
    return user
