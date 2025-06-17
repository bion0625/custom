import {type ReactNode, useEffect, useState} from "react";
import type {User} from "../types.ts";
import api from "../api.ts";
import AuthContext from "../context/AuthContext.tsx";

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
            const res = await api.get<User>("/me", {
                headers: {Authorization: `Bearer ${token}`},
            });
            setUser(res.data);
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