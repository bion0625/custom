import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import useMyStories from "../hook/useMyStories.ts";
import usePostMyStory from "../hook/usePostMyStory.ts";

const MyStories: React.FC = () => {
    const { data: stories } = useMyStories();
    const { mutate: createStory } = usePostMyStory();
    const [title, setTitle] = useState("");
    const navigate = useNavigate();

    const handleCreate = () => {
        if (!title.trim()) return;
        createStory(title, {
            onSuccess: (story) => navigate(`/my?storyId=${story.id}`)
        });
    };

    return (
        <div className="p-4 space-y-4">
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
                        <span className="space-x-2">
                            <Link className="text-blue-600" to={`/my?storyId=${s.id}`}>Scenes</Link>
                            <Link className="text-blue-600" to={`/my/graph?storyId=${s.id}`}>Graph</Link>
                        </span>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default MyStories;
