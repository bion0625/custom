import { useState, useContext } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useQueryClient } from "@tanstack/react-query";
import AuthContext from "../context/AuthContext.tsx";
import useMyStories from "../hook/useMyStories.ts";
import usePostMyStory from "../hook/usePostMyStory.ts";
import useDeleteMyStory from "../hook/useDeleteMyStory.ts";

const MyStories: React.FC = () => {
    const { data: stories } = useMyStories();
    const { user } = useContext(AuthContext);
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const { mutate: createStory } = usePostMyStory();
    const { mutate: deleteStory } = useDeleteMyStory();
    const [title, setTitle] = useState("");

    const handleCreate = () => {
        if (!title.trim()) return;
        createStory(title, {
            onSuccess: () => {
                queryClient.invalidateQueries({ queryKey: ['myStories'] });
                setTitle('');
            }
        });
    };

    return (
        <div className="p-4 space-y-4">
            <button
                className="px-4 py-2 bg-gray-700 text-white rounded"
                onClick={() => navigate('/')}
            >
                Home
            </button>
            <div className="space-y-2">
                <input
                    className="border p-2"
                    value={title}
                    onChange={e => setTitle(e.target.value)}
                    placeholder="Story title"
                />
                <button className="ml-2 px-4 py-2 bg-indigo-600 text-white rounded" onClick={handleCreate}>Create</button>
            </div>
            <ul className="space-y-2">
                {stories?.map(s => (
                    <li key={s.id} className="border p-2 flex justify-between">
                        <span>{s.title}</span>
                        <span className="flex flex-wrap gap-2">
                            <Link
                                className="px-2 py-1 bg-blue-600 text-white rounded hover:bg-blue-700"
                                to={`/my?storyId=${s.id}`}
                            >
                                Scenes
                            </Link>
                            <Link
                                className="px-2 py-1 bg-purple-600 text-white rounded hover:bg-purple-700"
                                to={`/my/graph?storyId=${s.id}`}
                            >
                                Graph
                            </Link>
                            {user && (
                                <Link
                                    className="px-2 py-1 bg-indigo-600 text-white rounded hover:bg-indigo-700"
                                    to={`/game/${user.memberId}/${s.id}`}
                                >
                                    My Game
                                </Link>
                            )}
                            <button
                                className="px-2 py-1 bg-red-600 text-white rounded hover:bg-red-700"
                                onClick={() => deleteStory(s.id, { onSuccess: () => queryClient.invalidateQueries({ queryKey: ['myStories'] }) })}
                            >
                                Delete
                            </button>
                        </span>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default MyStories;
