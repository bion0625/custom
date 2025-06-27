import { useNavigate } from "react-router-dom";
import { useContext } from "react";
import AuthContext from "../context/AuthContext.tsx";
import useUserStories from "../hook/useUserStories.ts";

interface GameHeaderProps {
  title: React.ReactNode;
  showLogin?: boolean;
  logoutRedirect: string;
}

const GameHeader: React.FC<GameHeaderProps> = ({ title, showLogin, logoutRedirect }) => {
  const navigate = useNavigate();
  const { user, logout, refreshUser } = useContext(AuthContext);
  const { data: adminStories } = useUserStories('admin', !!user);

  const handleLogout = async () => {
    await logout();
    await refreshUser();
    navigate(logoutRedirect);
  };

  return (
    <div className="w-full max-w-xl flex justify-between items-center">
      <h1 className="text-2xl md:text-3xl font-bold text-indigo-300">{title}</h1>
      <div className="flex space-x-4">
        {adminStories?.map(s => (
          <button
            key={s.id}
            className="text-sm md:text-base text-yellow-400 hover:text-yellow-600"
            onClick={() => navigate(`/game/admin/${s.id}`)}
          >
            {s.title}
          </button>
        ))}
        {user?.isAdmin && (
          <button
            className="text-sm md:text-base text-green-400 hover:text-green-600"
            onClick={() => navigate('/admin/public')}
          >
            관리자
          </button>
        )}
        {showLogin && !user && (
          <button
            className="text-sm md:text-base text-blue-400 hover:text-blue-600"
            onClick={() => navigate('/login')}
          >
            로그인
          </button>
        )}
        {user && (
          <button
            className="text-sm md:text-base text-red-500 hover:text-red-600"
            onClick={handleLogout}
          >
            로그아웃
          </button>
        )}
      </div>
    </div>
  );
};

export default GameHeader;
