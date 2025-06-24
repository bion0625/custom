import type { FullLogProps } from "../props.ts";

const FullLog: React.FC<FullLogProps> = ({ log, onRestart }) => {
    return (
        <div className="flex flex-col items-center space-y-4 w-full">
            <div className="bg-white/10 backdrop-blur-md rounded-xl p-6 w-full max-w-xl max-h-[70vh] overflow-y-auto text-sm">
                <h2 className="text-indigo-300 font-bold mb-2">전체 로그</h2>
                <ul className="list-decimal list-inside space-y-1">
                    {log.map((entry, idx) => (
                        <li key={idx}>{entry}</li>
                    ))}
                </ul>
            </div>
            <button
                onClick={onRestart}
                className="py-2 px-4 bg-indigo-600 hover:bg-indigo-700 rounded-lg text-white"
            >
                처음으로 돌아가기
            </button>
        </div>
    );
};

export default FullLog;
