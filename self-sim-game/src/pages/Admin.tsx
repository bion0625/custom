import React, { useContext, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../contexts/AuthContext";
import { api } from "../api";

const Admin: React.FC = () => {
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();
  const [list, setList] = useState<string[]>([]);
  const [currentId, setCurrentId] = useState<string>("");
  const [content, setContent] = useState<string>("");

  // 씬 목록 로드
  useEffect(() => {
    api.get<string[]>("/admin/story").then(res => setList(res.data));
  }, []);

  // 특정 씬 불러오기
  useEffect(() => {
    if (!currentId) return;
    api.get<string>(`/admin/story/${currentId}`)
       .then(res => setContent(res.data));
  }, [currentId]);

  const save = async () => {
    try {
      await api.post("/admin/story", { scene_id: currentId, content });
      alert("저장 완료");
    } catch {
      alert("저장 실패");
    }
  };

  const remove = async () => {
    try {
      await api.delete(`/admin/story/${currentId}`);
      setList(prev => prev.filter(id => id !== currentId));
      setCurrentId("");
      setContent("");
      alert("삭제 완료");
    } catch {
      alert("삭제 실패");
    }
  };

  const createNew = () => {
    const id = prompt("새 씬 ID를 입력하세요:");
    if (!id) return;
    setCurrentId(id);
    setContent(
      `---\nid: ${id}\nspeaker: \nbg: \nsymbol: \n---\n\n여기에 내용을 입력하세요`
    );
  };

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-6xl mx-auto flex flex-col">
        <nav className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold text-gray-800">스토리 마크다운 관리</h1>
          <div className="space-x-2">
            <button
              onClick={() => navigate('/game')}
              className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition"
            >
              게임으로 돌아가기
            </button>
            <button
              onClick={() => {
                localStorage.removeItem("access_token");
                navigate("/login");
              }}
              className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
            >
              로그아웃
            </button>
          </div>
        </nav>

        <div className="bg-white shadow rounded-lg overflow-hidden flex flex-col md:flex-row">
          <aside className="md:w-1/4 border-r bg-gray-100 p-4">
            <button
              onClick={createNew}
              className="mb-4 w-full py-2 bg-green-600 text-white rounded hover:bg-green-700 transition"
            >
              + 새 씬
            </button>
            <ul className="space-y-2 overflow-auto max-h-[60vh]">
              {list.map(id => (
                <li key={id}>
                  <button
                    onClick={() => setCurrentId(id)}
                    className={`w-full text-left px-2 py-1 rounded ${
                      id === currentId ? 'bg-indigo-200 font-semibold' : 'hover:bg-gray-200'
                    }`}
                  >
                    {id}
                  </button>
                </li>
              ))}
            </ul>
          </aside>

          <section className="flex-1 p-4 flex flex-col">
            <textarea
              value={content}
              onChange={e => setContent(e.target.value)}
              className="w-full border border-gray-300 bg-white p-4 rounded-lg font-mono resize-y focus:outline-none focus:ring-2 focus:ring-indigo-400 min-h-[60vh] overflow-auto"
            />
            <div className="mt-4 flex space-x-2">
              <button
                onClick={save}
                className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
              >
                저장
              </button>
              {currentId && (
                <button
                  onClick={remove}
                  className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition"
                >
                  삭제
                </button>
              )}
            </div>
          </section>
        </div>
      </div>
    </div>
  );
};

export default Admin;
