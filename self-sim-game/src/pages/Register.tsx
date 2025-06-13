import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api";

const Register: React.FC = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleRegister = async () => {
    try {
      await api.post(`/register`, {
        username,
        password
      }, {
        headers: {
          "Content-Type": "application/json"
        }
      });

      alert("회원가입 성공! 이제 로그인하세요.");
      navigate("/login");
    } catch (err) {
      setError("회원가입 실패: 이미 존재하는 아이디일 수 있어요.");
    }
  };

  return (
    <div className="flex flex-col items-center justify-center h-screen">
      <div className="w-80 p-6 bg-white rounded shadow">
        <h2 className="text-xl font-bold mb-4">회원가입</h2>
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
          className="w-full p-2 bg-green-500 text-white rounded"
          onClick={handleRegister}
        >
          회원가입
        </button>
        <button
          className="w-full mt-2 p-2 text-sm text-blue-500"
          onClick={() => navigate("/login")}
        >
          이미 계정이 있으신가요? 로그인
        </button>
      </div>
    </div>
  );
};

export default Register;
