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
};