// src/pages/Admin.tsx

import React, {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import {api} from "../api";
import {SceneCreate, SceneOut, SceneUpdate,} from "../types/Scene";

const Admin: React.FC = () => {
  const navigate = useNavigate();

  // ─── State ───────────────────────────────────────────
  const [list, setList] = useState<SceneOut[]>([]);
  const [currentId, setCurrentId] = useState<string>("");
  const [scene, setScene] = useState<SceneOut | null>(null);

  // Form 에 바인딩할 임시 State
  const [form, setForm] = useState<SceneCreate>({
    id: "",
    speaker: "",
    bg: "",
    text: "",
    choices: [],
    end: false,
    start: false,
  });

  // 이미 다른 씬에 start=true 가 있는지
  const hasOtherStart = list.some((s) => s.start && s.id !== currentId);

  // ─── 1) 씬 목록 로드 ──────────────────────────────────
  useEffect(() => {
    api.get<SceneOut[]>("/admin/story").then((res) => setList(res.data));
  }, []);

  // ─── 1-1) 리스트가 변경되면 첫 ID 자동 선택 ────────────────
  useEffect(() => {
    if (list.length > 0 && !currentId) {
      setCurrentId(list[0].id);
    }
  }, [list, currentId]);

  // ─── 2) currentId 변경 시 씬 로드 및 form 초기화 ─────────
  useEffect(() => {
    if (!currentId) {
      setScene(null);
      return;
    }
    if (!list.some((s) => s.id === currentId)) {
      setScene(null);
      return;
    }
    api.get<SceneOut>(`/admin/story/${currentId}`).then((res) => {
      setScene(res.data);
      setForm({
        id:      res.data.id,
        speaker: res.data.speaker,
        bg:      res.data.bg,
        text:    res.data.text,
        choices: res.data.choices,
        end:     res.data.end,
        start:   res.data.start ?? false,
      });
    });
  }, [currentId, list]);

  // ─── 3) 새 씬 생성 ─────────────────────────────────────
  const createNew = () => {
    const id = prompt("새 씬 ID를 입력하세요:");
    if (!id) return;
    setCurrentId(id);
    setForm({
      id,
      speaker: "",
      bg: "",
      text: "",
      choices: [],
      end: false,
      start: false,
    });
    setScene(null);
  };

  // ─── 4) 저장 (POST or PUT) ─────────────────────────────
  const save = async () => {
    try {
      if (scene) {
        const payload: SceneUpdate = { ...form };
        await api.put<SceneOut>(`/admin/story/${currentId}`, payload);
        setList((prev) =>
          prev.map((s) =>
            s.id === currentId ? ({ ...s, ...payload } as SceneOut) : s
          )
        );
      } else {
        await api.post<SceneOut>("/admin/story", form);
        setList((prev) => [...prev, form as SceneOut]);
      }
      alert("저장 완료");
    } catch {
      alert("저장 실패");
    }
  };

  // ─── 5) 삭제 ───────────────────────────────────────────
  const remove = async () => {
    if (!scene) return;
    if (!window.confirm("정말 이 씬을 삭제하시겠습니까?")) return;
    try {
      await api.delete(`/admin/story/${currentId}`);
      setList((prev) => prev.filter((s) => s.id !== currentId));
      setCurrentId("");
      setScene(null);
      alert("삭제 완료");
    } catch {
      alert("삭제 실패");
    }
  };

  // ─── 6) choice 항목 추가/삭제 헬퍼 ────────────────────────
  const addChoice = () =>
    setForm((f) => ({ ...f, choices: [...f.choices, { text: "", next: "" }] }));
  const updateChoice = (idx: number, key: "text" | "next", val: string) =>
    setForm((f) => {
      const c = [...f.choices];
      c[idx] = { ...c[idx], [key]: val };
      return { ...f, choices: c };
    });
  const removeChoice = (idx: number) =>
    setForm((f) => ({
      ...f,
      choices: f.choices.filter((_, i) => i !== idx),
    }));

  return (
    <div className="min-h-screen bg-gray-50 p-4 sm:p-6">
      <div className="max-w-6xl mx-auto flex flex-col md:flex-row gap-6">
        {/* 사이드바 */}
        <aside className="w-full md:w-1/4 border-b md:border-b-0 md:border-r bg-gray-100 p-4 rounded-md md:rounded-none overflow-auto h-auto md:h-[80vh]">
          {/* 게임으로 돌아가기 */}
          <button
            onClick={() => navigate("/game")}
            className="mb-2 w-full py-2 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 transition"
          >
            게임으로 돌아가기
          </button>
          {/* 로그아웃 */}
          <button
            onClick={() => {
              localStorage.removeItem("access_token");
              navigate("/login");
            }}
            className="mb-4 w-full py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition"
          >
            로그아웃
          </button>
          {/* 새 씬 생성 */}
          <button
            onClick={createNew}
            className="mb-4 w-full py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition"
          >
            + 새 씬
          </button>

          <ul className="space-y-2">
            {list.map((s) => (
              <li key={s.id}>
                <button
                  onClick={() => setCurrentId(s.id)}
                  className={`w-full text-left px-2 py-1 rounded 
                    ${s.id === currentId
                      ? "bg-indigo-200 font-semibold"
                      : "hover:bg-gray-200"}`
                  }
                >
                  {s.id}
                </button>
              </li>
            ))}
          </ul>
        </aside>

        {/* 에디터 폼 */}
        <section className="w-full md:flex-1 bg-white p-4 rounded-md shadow flex flex-col space-y-4 overflow-auto h-auto md:h-[80vh]">
          {/* Grid for inputs */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
            <div>
              <label className="block text-sm font-medium">Speaker</label>
              <input
                type="text"
                value={form.speaker}
                onChange={(e) =>
                  setForm((f) => ({ ...f, speaker: e.target.value }))
                }
                className="mt-1 w-full border rounded p-2"
              />
            </div>
            <div>
              <label className="block text-sm font-medium">Background</label>
              <input
                type="text"
                value={form.bg || "dark-room.jpg"}
                onChange={(e) =>
                  setForm((f) => ({ ...f, bg: e.target.value }))
                }
                className="mt-1 w-full border rounded p-2"
                placeholder="scene1.jpg"
              />
            </div>
          </div>

          {/* Text */}
          <div className="mb-4">
            <label className="block text-sm font-medium">Text</label>
            <textarea
              value={form.text}
              onChange={(e) =>
                setForm((f) => ({ ...f, text: e.target.value }))
              }
              className="mt-1 w-full border rounded p-2 h-32"
            />
          </div>

          {/* Choices */}
          <div className="mb-4">
            <div className="flex justify-between items-center mb-2">
              <label className="text-sm font-medium">Choices</label>
              <button onClick={addChoice} className="text-sm text-green-600">
                + 추가
              </button>
            </div>
            <div className="space-y-2">
              {form.choices.map((c, i) => (
                <div
                  key={i}
                  className="flex flex-col sm:flex-row sm:space-x-2 space-y-2 sm:space-y-0"
                >
                  <input
                    type="text"
                    value={c.text}
                    onChange={(e) => updateChoice(i, "text", e.target.value)}
                    placeholder="버튼 텍스트"
                    className="border rounded p-1 flex-1"
                  />
                  <input
                    type="text"
                    value={c.next}
                    onChange={(e) => updateChoice(i, "next", e.target.value)}
                    placeholder="다음 씬 ID"
                    className="border rounded p-1 flex-1"
                  />
                  <button
                    onClick={() => removeChoice(i)}
                    className="text-red-600 self-center"
                  >
                    ×
                  </button>
                </div>
              ))}
            </div>
          </div>

          {/* Flags */}
          <div className="flex flex-col sm:flex-row sm:space-x-4 mb-4">
            <label className="inline-flex items-center space-x-2">
              <input
                type="checkbox"
                checked={form.end}
                onChange={(e) =>
                  setForm((f) => ({ ...f, end: e.target.checked }))
                }
                className="form-checkbox"
              />
              <span>End Scene</span>
            </label>
            <label
              className={`inline-flex items-center space-x-2 ${
                hasOtherStart && !form.start
                  ? "opacity-50 pointer-events-none"
                  : ""
              }`}
            >
              <input
                type="checkbox"
                checked={form.start}
                onChange={(e) =>
                  setForm((f) => ({ ...f, start: e.target.checked }))
                }
                className="form-checkbox"
              />
              <span>Start Scene</span>
            </label>
          </div>

          {/* Buttons */}
          <div className="flex flex-col sm:flex-row sm:space-x-2 space-y-2 sm:space-y-0">
            <button
              onClick={save}
              className="w-full sm:w-auto px-4 py-2 bg-blue-600 text-white rounded-lg"
            >
              저장
            </button>
            {scene && (
              <button
                onClick={remove}
                className="w-full sm:w-auto px-4 py-2 bg-red-600 text-white rounded-lg"
              >
                삭제
              </button>
            )}
          </div>
        </section>
      </div>
    </div>
  );
};

export default Admin;
