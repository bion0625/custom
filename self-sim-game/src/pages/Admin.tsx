import React, { useContext, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../contexts/AuthContext";
import { api } from "../api";
import {
  SceneOut,
  SceneCreate,
  SceneUpdate,
} from "../types/Scene";

const Admin: React.FC = () => {
  const { user } = useContext(AuthContext);
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
  });

  // ─── 1) 씬 목록 로드 ──────────────────────────────────
  useEffect(() => {
    api.get<SceneOut[]>("/admin/story").then((res) => {
      setList(res.data);
    });
  }, []);

  // ─── 2) currentId 변경 시 씬 로드 및 form 초기화 ─────────
  useEffect(() => {
    if (!currentId) {
      setScene(null);
      return;
    }
    if (!list.some((s) => s.id === currentId)) {
      // 새로 생성 중인 씬이니 아직 DB 에 없다고 보고, fetch 건너뛰기
      setScene(null);
      return;
    }
    api.get<SceneOut>(`/admin/story/${currentId}`).then((res) => {
      setScene(res.data);
      // form 초기화
      setForm({
        id: res.data.id,
        speaker: res.data.speaker,
        bg: res.data.bg,
        text: res.data.text,
        choices: res.data.choices,
        end: res.data.end,
      });
    });
  }, [currentId]);

  // ─── 3) 새 씬 생성 버튼 ─────────────────────────────────
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
    });
    setScene(null);
  };

  // ─── 4) 저장 (POST or PUT) ─────────────────────────────
  const save = async () => {
    try {
      if (scene) {
        // 기존 씬 → PUT
        const payload: SceneUpdate = {
          speaker: form.speaker,
          bg: form.bg,
          text: form.text,
          choices: form.choices,
          end: form.end,
        };
        await api.put<SceneOut>(`/admin/story/${currentId}`, payload);
        // 목록도 갱신
        setList((prev) =>
          prev.map((s) => (s.id === currentId ? { ...s, ...payload } as SceneOut : s))
        );
      } else {
        // 신규 씬 → POST
        await api.post<SceneOut>("/admin/story", form);
        setList((prev) => [...prev, form as SceneOut]);
      }
      alert("저장 완료");
    } catch (err) {
      console.error(err);
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
  const addChoice = () => {
    setForm((f) => ({ ...f, choices: [...f.choices, { text: "", next: "" }] }));
  };
  const updateChoice = (idx: number, key: "text" | "next", val: string) => {
    setForm((f) => {
      const c = [...f.choices];
      c[idx] = { ...c[idx], [key]: val };
      return { ...f, choices: c };
    });
  };
  const removeChoice = (idx: number) => {
    setForm((f) => {
      const c = f.choices.filter((_, i) => i !== idx);
      return { ...f, choices: c };
    });
  };

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-6xl mx-auto flex flex-col">
        {/* 헤더 */}
        <nav className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold text-gray-800">스토리 관리</h1>
          <div className="space-x-2">
            <button
              onClick={() => navigate("/game")}
              className="px-4 py-2 bg-indigo-600 text-white rounded-lg"
            >
              돌아가기
            </button>
            <button
              onClick={() => {
                localStorage.removeItem("access_token");
                navigate("/login");
              }}
              className="px-4 py-2 bg-red-600 text-white rounded-lg"
            >
              로그아웃
            </button>
          </div>
        </nav>

        <div className="bg-white shadow rounded-lg overflow-hidden flex flex-col md:flex-row">
          {/* 사이드바: ID 목록 */}
          <aside className="md:w-1/4 border-r bg-gray-100 p-4">
            <button
              onClick={createNew}
              className="mb-4 w-full py-2 bg-green-600 text-white rounded"
            >
              + 새 씬
            </button>
            <ul className="space-y-2 overflow-auto max-h-[60vh]">
              {list.map((s) => (
                <li key={s.id}>
                  <button
                    onClick={() => setCurrentId(s.id)}
                    className={`w-full text-left px-2 py-1 rounded ${
                      s.id === currentId
                        ? "bg-indigo-200 font-semibold"
                        : "hover:bg-gray-200"
                    }`}
                  >
                    {s.id}
                  </button>
                </li>
              ))}
            </ul>
          </aside>

          {/* 에디터 폼 */}
          <section className="flex-1 p-4 flex flex-col space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
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
                  value={form.bg}
                  onChange={(e) =>
                    setForm((f) => ({ ...f, bg: e.target.value }))
                  }
                  className="mt-1 w-full border rounded p-2"
                  placeholder="scene1.jpg"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium">Text</label>
              <textarea
                value={form.text}
                onChange={(e) =>
                  setForm((f) => ({ ...f, text: e.target.value }))
                }
                className="mt-1 w-full border rounded p-2 h-32"
              />
            </div>

            <div>
              <div className="flex justify-between items-center">
                <label className="block text-sm font-medium">Choices</label>
                <button
                  onClick={addChoice}
                  className="text-sm text-green-600"
                >
                  + 추가
                </button>
              </div>
              <div className="space-y-2 mt-1">
                {form.choices.map((c, i) => (
                  <div key={i} className="flex items-center space-x-2">
                    <input
                      type="text"
                      value={c.text}
                      onChange={(e) =>
                        updateChoice(i, "text", e.target.value)
                      }
                      placeholder="버튼 텍스트"
                      className="border rounded p-1 flex-1"
                    />
                    <input
                      type="text"
                      value={c.next}
                      onChange={(e) =>
                        updateChoice(i, "next", e.target.value)
                      }
                      placeholder="다음 씬 ID"
                      className="border rounded p-1 flex-1"
                    />
                    <button
                      onClick={() => removeChoice(i)}
                      className="text-red-600"
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            </div>

            <div className="flex items-center space-x-2">
              <label className="inline-flex items-center">
                <input
                  type="checkbox"
                  checked={form.end}
                  onChange={(e) =>
                    setForm((f) => ({ ...f, end: e.target.checked }))
                  }
                  className="form-checkbox"
                />
                <span className="ml-2 text-sm">End Scene</span>
              </label>
            </div>

            <div className="flex space-x-2">
              <button
                onClick={save}
                className="px-4 py-2 bg-blue-600 text-white rounded"
              >
                저장
              </button>
              {scene && (
                <button
                  onClick={remove}
                  className="px-4 py-2 bg-red-600 text-white rounded"
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
