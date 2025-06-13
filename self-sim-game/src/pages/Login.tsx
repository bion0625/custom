import React, { useState } from "react";
import axios from "axios";

const Login: React.FC = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleLogin = async () => {
    try {
      const response = await axios.post("http://localhost:8000/token", new URLSearchParams({
        username,
        password,
      }), {
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        }
      });

      localStorage.setItem("access_token", response.data.access_token);
      setError("");
      alert("로그인 성공!");
      // TODO: 이후 다른 페이지로 이동
    } catch (err) {
      setError("로그인 실패: 아이디 또는 비밀번호를 확인하세요.");
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
      </div>
    </div>
  );
};

export default Login;
