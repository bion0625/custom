// src/App.tsx
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, AuthContext } from "./contexts/AuthContext";
import Login from "./pages/Login";
import Register from "./pages/Register";
import MainGame from "./pages/MainGame";
import React, { JSX, useContext } from "react";
import Admin from "./pages/Admin";

const PrivateRoute: React.FC<{ children: JSX.Element }> = ({ children }) => {
  const { user, loading } = useContext(AuthContext);
  if (loading) {
    return <div>로딩 중…</div>;
  }
  return user ? children : <Navigate to="/login" />;
};

// 관리자 권한 체크
const AdminRoute: React.FC<{ children: JSX.Element }> = ({ children }) => {
  const { user, loading } = useContext(AuthContext);
  if (loading) return <div>로딩 중…</div>;
  return user?.is_admin ? children : <Navigate to="/game" replace />;
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route
            path="/game"
            element={
              <PrivateRoute>
                <MainGame />
              </PrivateRoute>
            }
          />
          <Route
            path="/admin/*"
            element={
              <AdminRoute>
                <Admin />
              </AdminRoute>
            }
          />
          <Route path="*" element={<Navigate to="/game" />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
