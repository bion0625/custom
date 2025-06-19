export interface User {
    id: number;
    memberId: string;
    name: string;
    isAdmin: boolean
}

interface Text {
    text: string;
    nextPublicSceneId: number;
    nextText: string;
}

export interface PublicScene {
    sceneId: number;
    speaker: string;
    backgroundImage: string;
    text: string;
    texts: Text[];
    start: boolean;
    end: boolean;
}

export interface PublicStory {
    isSuccess: boolean;
    message: string;
    publicScenes: PublicScene[];
}

interface choiceRequest {
    nextSceneId: number;
    text: string;
    nextText?: string;
}

export interface PublicSceneRequest {
    id?: number;
    speaker: string;
    backgroundImage: string;
    text: string;
    choiceRequests: choiceRequest[];
    start: boolean;
    end: boolean;
}

export interface AuthContextType {
    user: User | null;
    loading: boolean;
    refreshUser: () => Promise<void>;
    logout: () => void;
}

export interface LoginRequest {
    id: string;
    password: string;
}

export interface RegisterRequest {
    id: string;
    password: string;
    name: string;
    phoneNumber: string;
    email: string;
}