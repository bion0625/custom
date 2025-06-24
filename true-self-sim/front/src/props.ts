export interface MemoryLogProps {
    log: string[];
}

export interface RetrospectiveProps {
    log: string[];
    /** 마지막 씬에서 보여줄 텍스트 */
    finalText?: string;
    onRestart: () => void; // ← 여기 중요!
}