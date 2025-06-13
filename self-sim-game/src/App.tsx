// src/App.tsx
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, AuthContext } from "./contexts/AuthContext";
import Login from "./pages/Login";
import Register from "./pages/Register";
import MainGame from "./pages/MainGame";
import React, { JSX, useContext } from "react";

const PrivateRoute: React.FC<{ children: JSX.Element }> = ({ children }) => {
  const { user, loading } = useContext(AuthContext);
  if (loading) {
    return <div>로딩 중…</div>;
  }
  return user ? children : <Navigate to="/login" />;
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
          <Route path="*" element={<Navigate to="/game" />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
