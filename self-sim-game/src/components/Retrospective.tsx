interface RetrospectiveProps {
  log: string[];
  onRestart: () => void;
}

export const Retrospective: React.FC<RetrospectiveProps> = ({ log, onRestart }) => {
  return (
    <div className="min-h-screen bg-black text-white flex items-center justify-center p-8">
      <div className="bg-white/10 p-6 rounded-lg max-w-2xl w-full text-sm shadow-xl">
        <h2 className="text-2xl text-indigo-300 font-bold mb-4">회고</h2>
        <p className="mb-6 text-white/80">당신이 걸어온 내면의 여정은 다음과 같아요:</p>
        <ul className="space-y-2 max-h-96 overflow-y-auto mb-6">
          {log.map((entry, idx) => (
            <li key={idx} className="text-white/90">{entry}</li>
          ))}
        </ul>
        <button
          onClick={onRestart}
          className="mt-4 bg-indigo-600 hover:bg-indigo-700 px-4 py-2 rounded"
        >
          다시 시작하기
        </button>
      </div>
    </div>
  );
};
