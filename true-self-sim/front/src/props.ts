export interface MemoryLogProps {
    log: string[];
}

export interface RetrospectiveProps {
    log: string[];
    onRestart: () => void; // ← 여기 중요!
}