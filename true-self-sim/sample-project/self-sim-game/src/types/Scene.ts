export type Choice = {
  text: string;
  next: string;
};

export type Scene = {
  id: string;
  speaker: string;
  text: string;
  bg: string;
  symbol: string;
  choices: Choice[];
  end?: boolean; // ✅ 선택적으로 존재할 수 있는 종료 플래그
  start?: boolean;  // ← 추가
};

/* ────────────────────────────────────────────────────────────── */
/*  이 아래로는 Admin 전용 타입 추가 (maingame.tsx에는 영향 없음) */
/* ────────────────────────────────────────────────────────────── */

/** 서버에서 내려주는 씬 전체 정보 (Admin List/Get) */
export interface SceneOut {
  id: string;
  speaker: string;
  bg: string;
  text: string;
  choices: Choice[];
  end?: boolean;
  start?: boolean;
}

/** 신규 씬 생성용 Payload (POST /admin/story) */
export type SceneCreate = {
  id: string;
  speaker: string;
  bg: string;
  text: string;
  choices: Choice[];
  end?: boolean;
  start?: boolean;  // ← 추가
};

/** 기존 씬 수정용 Payload (PUT /admin/story/{id}) */
export type SceneUpdate = Partial<Omit<SceneCreate, "id">>;