import {createContext} from "react";
import type {AuthContextType} from "../types.ts";

const AuthContext = createContext<AuthContextType>({
    user: null,
    loading: true,
    refreshUser: async () => {},
    logout: async () => {},
})

export default AuthContext;