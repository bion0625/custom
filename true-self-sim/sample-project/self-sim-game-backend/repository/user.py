from models import User
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

async def get_user_by_username(
    db: AsyncSession, username: str
) -> User | None:
    # 1) select() 객체 생성
    stmt = select(User).where(User.username == username)
    # 2) 비동기 실행
    result = await db.execute(stmt)
    # 3) scalars() → 리스트형념 풀어서 .first()
    return result.scalars().first()

async def create_user(
    db: AsyncSession, username: str, password_hash: str
) -> User:
    user = User(username=username, hashed_password=password_hash)
    db.add(user)
    await db.commit()
    await db.refresh(user)
    return user
