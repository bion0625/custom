import {type ReactNode, useEffect, useState} from "react";
import type {User} from "../types.ts";
import AuthContext from "../context/AuthContext.tsx";
import {getMe} from "../api/auth.ts";

const AuthProvider: React.FC<{children: ReactNode}> = ({children}) => {
    const [user, setUser] = useState<User | null>(null);
    const [loading, setLoading] = useState(true);

    const refreshUser = async () => {
        const token = localStorage.getItem("access_token");
        if (!token) {
            setUser(null);
            setLoading(false);
            return;
        }
        try {
            const user = await getMe();
            setUser(user);
        } catch {
            setUser(null);
            localStorage.removeItem("access_token");
        } finally {
            setLoading(false);
        }
    };

    const logout = () => {
        localStorage.removeItem("access_token");
        setUser(null);
    }

    useEffect(() => {
        refreshUser();
    }, []);

    return (
        <AuthContext.Provider value={{user, loading, refreshUser, logout}}>
            {children}
        </AuthContext.Provider>
    )
}

export default AuthProvider;