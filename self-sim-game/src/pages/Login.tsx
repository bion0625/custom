import React, { useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../contexts/AuthContext";
import { api } from "../api";

const Login: React.FC = () => {
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const { refreshUser } = useContext(AuthContext);

  const handleLogin = async () => {
    try {
      const res = await api.post(`/token`, { username, password }, {
        headers: { "Content-Type": "application/json" },
      });
      localStorage.setItem("access_token", res.data.access_token);
      await refreshUser();           // ← 여기가 추가됨
      navigate("/game");
    } catch {
      setError("로그인 실패: 아이디 또는 비밀번호 확인");
    }
  };

  return (
    <div className="flex flex-col items-center justify-center h-screen">
      <div className="w-80 p-6 bg-white rounded shadow">
        <h2 className="text-xl font-bold mb-4">로그인</h2>
        <input
          className="w-full mb-2 p-2 border rounded"
          type="text"
          placeholder="아이디"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />
        <input
          className="w-full mb-2 p-2 border rounded"
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        {error && <div className="text-red-500 text-sm mb-2">{error}</div>}
        <button
          className="w-full p-2 bg-blue-500 text-white rounded"
          onClick={handleLogin}
        >
          로그인
        </button>
        <button
          className="w-full mt-2 p-2 text-sm text-blue-500"
          onClick={() => navigate("/register")}
        >
          계정이 없으신가요? 회원가입
        </button>
      </div>
    </div>
  );
};

export default Login;
