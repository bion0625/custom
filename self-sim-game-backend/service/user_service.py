from repository.user import get_user_by_username, create_user
from sqlalchemy import select, func
from core.security import verify_password, get_password_hash
from models import User
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


async def register_user(db: AsyncSession, username: str, password: str) -> User:
    # 1) 중복 검사
    existing = await db.execute(select(User).where(User.username == username))
    if existing.scalars().first():
        raise ValueError("Username already taken")

    # 2) 기존 사용자 수 확인
    result = await db.execute(select(func.count()).select_from(User))
    (user_count,) = result.one()  # 튜플 언패킹

    # 3) 첫 유저면 is_admin=True, 이후는 False
    is_admin = (user_count == 0)

    # 4) 새 사용자 생성
    hashed_pw = get_password_hash(password)
    user = User(
        username=username,
        hashed_password=hashed_pw,
        is_admin=is_admin,
    )
    db.add(user)
    await db.commit()
    await db.refresh(user)
    return user

async def authenticate_user(db: AsyncSession, username: str, password: str) -> User | None:
    result = await db.execute(select(User).where(User.username == username))
    user = result.scalars().first()
    if not user or not verify_password(password, user.hashed_password):
        return None
    return user
