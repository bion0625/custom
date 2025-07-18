# Self-Sim Game Backend & Frontend

**Self-Sim Game**은 사용자의 자기성찰을 유도하는 철학적 시뮬레이션 게임입니다. 이 저장소는 해당 게임의 **FastAPI 백엔드**와 **React( CRA ) 프런트엔드**가 한 곳에 담겨 있으며, Docker Compose 로 통합 실행할 수 있습니다.

---

## ✨ 주요 기능

### 백엔드 (FastAPI)

* JWT 기반 로그인·인증
* 이야기( JSON ) 전달 및 사용자 선택 저장
* 선택 로그 조회 API
* 관리자용 이야기 CRUD API
* Swagger 자동 문서 `/docs`

### 프런트엔드 (React + CRA)

* JWT 로그인 화면
* 이야기/대화 진행 UI
* 선택 로그 요약·회고 화면
* SPA 구조(React Router)

---

## 📁 폴더 구조

```
custom-main/
├── .env.sample                   # 환경 변수 예시
├── docker-compose.yml            # 백엔드 단독 구성
├── docker-compose-with-front.yml # 프런트 포함 전체 구성
├── self-sim-game-backend/        # FastAPI 백엔드
│   └── ...
└── self-sim-game/                # React 프런트엔드(CRA)
    ├── package.json              # CRA 설정 및 스크립트
    ├── tsconfig.json             # TypeScript 설정
    ├── public/
    │   └── index.html            # HTML 진입점
    └── src/
        ├── main.tsx             # React 진입점
        ├── App.tsx              # 루트 컴포넌트
        ├── pages/               # 페이지 단위 컴포넌트
        ├── components/          # 공통 UI 컴포넌트
        └── api/                 # axios 래퍼 & API 훅
```

---

## 🚀 빠른 시작

### 1) 클론 & 환경 변수

```bash
git clone <your-repo-url>
cd custom-main
cp .env.sample .env  # DB·JWT 시크릿 등 설정
```

### 2) Docker 통합 실행

```bash
docker-compose -f docker-compose-with-front.yml up --build
```

### 3) 개별 실행

* **백엔드만**

  ```bash
  docker-compose up --build  # FastAPI + DB
  ```
* **프런트만 (로컬 개발)**

  ```bash
  cd self-sim-game
  npm install
  npm start   # http://localhost:3000
  ```

---

## 📌 주요 API 스케치

| 영역    | 메서드  | 엔드포인트          | 설명        |
| ----- | ---- | -------------- | --------- |
| Auth  | POST | `/login`       | JWT 발급    |
| Story | GET  | `/story/{id}`  | 이야기 단건 조회 |
| Story | POST | `/story/log`   | 사용자 선택 저장 |
| Admin | POST | `/admin/story` | 이야기 등록·수정 |

> 상세 스키마와 응답은 실행 후 `/docs`(Swagger) 확인

---

## 🧩 기술 스택

| 레이어      | 기술                                               |
| -------- | ------------------------------------------------ |
| Backend  | Python 3.9, FastAPI, SQLAlchemy, PostgreSQL, JWT |
| Frontend | React 18, TypeScript, CRA, Tailwind CSS, axios   |
| DevOps   | Docker, Docker Compose                           |

---

## 🛠 개발 팁

1. **DB 초기화**: `self-sim-game-backend/db/init_db.py` 실행 또는 컨테이너 첫 기동 시 자동 실행.
2. **환경 변수**: `.env.sample` 기반으로 JWT 시크릿·DB 접속 정보 기입.
3. **API 테스트**: VSCode REST Client 스니펫이나 Thunder Client 권장.

---

## 🪪 라이선스

MIT License