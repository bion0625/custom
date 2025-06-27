import React from 'react';

export interface SidebarScene {
    sceneId: string;
    text: string;
}

interface AdminSidebarProps {
    scenes: SidebarScene[] | undefined;
    currentId: string;
    onSelect: (id: string) => void;
    onBack: () => void;
    onLogout: () => void;
    onCreate: () => void;
    onGraph?: () => void;
    children?: React.ReactNode;
}

const AdminSidebar: React.FC<AdminSidebarProps> = ({
    scenes,
    currentId,
    onSelect,
    onBack,
    onLogout,
    onCreate,
    onGraph,
    children,
}) => {
    return (
        <aside className="w-full md:w-1/4 border-b md:border-b-0 md:border-r bg-gray-100 p-4 rounded-md md:rounded-none overflow-auto">
            <button
                className="mb-2 w-full py-2 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 transition"
                onClick={onBack}
            >
                돌아가기
            </button>
            <button
                className="mb-2 w-full py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition"
                onClick={onLogout}
            >
                로그아웃
            </button>
            {onGraph && (
                <button
                    className="mb-2 w-full py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                    onClick={onGraph}
                >
                    그래프
                </button>
            )}
            <button
                className="mb-2 w-full py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition"
                onClick={onCreate}
            >
                + 새 장면
            </button>
            {children}
            <ul className="space-y-2">
                {scenes?.map((sc) => (
                    <li key={sc.sceneId}>
                        <button
                            onClick={() => onSelect(sc.sceneId)}
                            className={`w-full text-left px-2 py-1 rounded ${sc.sceneId === currentId ? 'bg-indigo-200 font-semibold' : 'hover:bg-gray-200'}`}
                        >
                            <span className="text-sm text-gray-700 font-mono">[{sc.sceneId}]</span> {sc.text}
                        </button>
                    </li>
                ))}
            </ul>
        </aside>
    );
};

export default AdminSidebar;
