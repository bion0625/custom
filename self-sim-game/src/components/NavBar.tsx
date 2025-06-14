// src/components/NavBar.tsx
import { Link } from "react-router-dom";
import { useContext } from "react";
import { AuthContext } from "../contexts/AuthContext";

export default function NavBar() {
  const { user, logout } = useContext(AuthContext);

  return (
    <nav className="p-4 bg-gray-100 flex justify-between">
      <div className="space-x-4">
        <Link to="/story">게임 시작</Link>
        {user?.is_admin && (
          <Link to="/admin" className="text-red-600">
            관리자
          </Link>
        )}
      </div>
      <div>
        {user ? (
          <button onClick={logout}>로그아웃</button>
        ) : (
          <Link to="/login">로그인</Link>
        )}
      </div>
    </nav>
  );
}
