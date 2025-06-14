import React, { useEffect, useState, useContext } from "react";
import { AuthContext } from "../contexts/AuthContext";
import { api } from "../api";

const Admin: React.FC = () => {
  const { user } = useContext(AuthContext);
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

  const save = () => {
    api.post("/admin/story", { scene_id: currentId, content })
      .then(() => alert("저장 완료"))
      .catch(() => alert("실패"));
  };

  const remove = () => {
    api.delete(`/admin/story/${currentId}`)
      .then(() => {
        alert("삭제 완료");
        setList(list.filter(id => id !== currentId));
        setCurrentId("");
        setContent("");
      });
  };

  const createNew = () => {
    const id = prompt("새 씬 ID를 입력하세요:");
    if (id) setCurrentId(id);
    setContent("---\nid: NEW_ID\nspeaker: \nbg: \nsymbol: \n---\n\n본문 내용을 입력하세요");
  };

  return (
    <div className="p-4">
      <h1 className="text-2xl mb-4">스토리 마크다운 관리</h1>
      <div className="flex space-x-4">
        <aside className="w-1/4 border p-2">
          <button onClick={createNew} className="mb-2">+ 새 씬</button>
          <ul>
            {list.map(id =>
              <li key={id}>
                <button onClick={()=>setCurrentId(id)} className={id===currentId?"font-bold":""}>
                  {id}
                </button>
              </li>
            )}
          </ul>
        </aside>
        <section className="flex-1 flex flex-col">
          <textarea
            className="flex-1 border p-2 mb-2 font-mono"
            value={content}
            onChange={e=>setContent(e.target.value)}
          />
          <div className="space-x-2">
            <button onClick={save} className="px-4 py-2 bg-blue-600">저장</button>
            {currentId && <button onClick={remove} className="px-4 py-2 bg-red-600">삭제</button>}
          </div>
        </section>
      </div>
    </div>
  );
};

export default Admin;
