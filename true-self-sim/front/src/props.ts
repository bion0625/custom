export interface MemoryLogProps {
    log: string[];
}

export interface FullLogProps {
    log: string[];
    onRestart: () => void;
}

export interface RetrospectiveProps {
    log: string[];
    onRestart: () => void; // ← 여기 중요!
}