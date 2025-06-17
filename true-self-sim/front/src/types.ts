export interface User {
    id: number;
    name: string;
    isAdmin: boolean
}

export interface AuthContextType {
    user: User | null;
    loading: boolean;
    refreshUser: () => Promise<void>;
    logout: () => void;
}