import { useContext } from "react";
import type { ReactElement } from "react";
import { Navigate } from "react-router-dom";
import AuthContext from "../context/AuthContext.tsx";

const PrivateRoute: React.FC<{ children: ReactElement }> = ({ children }) => {
    const { user, loading } = useContext(AuthContext);

    if (loading) {
        return <div>로딩 중...</div>;
    }

    return user ? children : <Navigate to="/login" replace />;
};

export default PrivateRoute;
