import type {RetrospectiveProps} from "../props.ts";

export const Retrospective: React.FC<RetrospectiveProps> = ({ log, onRestart }) => {
    return (
        <div className="bg-white/10 backdrop-blur-md rounded-xl shadow-lg p-8 max-w-xl w-full space-y-6 text-white">
            <h2 className="text-2xl font-bold text-indigo-300">당신의 여정</h2>
            <ul className="list-decimal list-inside space-y-1 text-sm max-h-60 overflow-auto">
                {log.map((entry, index) => (
                    <li key={index}>{entry}</li>
                ))}
            </ul>

            <button
                onClick={onRestart}
                className="w-full py-2 mt-4 bg-indigo-600 hover:bg-indigo-700 rounded-lg text-white"
            >
                처음부터 다시 시작
            </button>
        </div>
    );
};