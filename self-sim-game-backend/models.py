import json
from sqlalchemy import Boolean, Column, Integer, String, Text, JSON, TEXT, TypeDecorator
from sqlalchemy.orm import declarative_base

class JSONUnicode(TypeDecorator):
    impl = TEXT

    def process_bind_param(self, value, dialect):
        if value is None:
            return None
        # DB 에 쓸 때 ensure_ascii=False
        return json.dumps(value, ensure_ascii=False)

    def process_result_value(self, value, dialect):
        if value is None:
            return None
        return json.loads(value)

Base = declarative_base()

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(50), unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    is_admin = Column(Boolean, default=False)  # ← 추가

class Story(Base):
    __tablename__ = "stories"

    id = Column(Integer, primary_key=True, index=True)
    content = Column(Text, nullable=False)
    # 필요에 따라 created_at, updated_at 등을 추가
    

class Scene(Base):
    __tablename__ = "scenes"
    id      = Column(String, primary_key=True, index=True)
    speaker = Column(String, nullable=False)
    bg      = Column(String, nullable=True)      # ← 배경 이미지 파일명
    text    = Column(String, nullable=False)
    choices = Column(JSONUnicode, nullable=False)
    end     = Column(Boolean, default=False)     # ← 회차 종료 여부
    start   = Column(Boolean, default=False)  # ← 추가!

class Log(Base):
    __tablename__ = "logs"
    id        = Column(Integer, primary_key=True, index=True)
    timestamp = Column(String, nullable=False)
    data      = Column(JSONUnicode, nullable=False)