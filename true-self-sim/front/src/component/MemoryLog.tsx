import type {MemoryLogProps} from "../props.ts";

const MemoryLog: React.FC<MemoryLogProps> = ({log}) => {
    return (
        <div className="w-full bg-black/60 text-sm p-4 rounded-lg shadow-inner text-white">
            <h3 className="text-indigo-400 font-semibold mb-2">메모리 로그 (LAST 5)</h3>
            <ul className="space-y-1">
                {log.map((entry, idx) => (
                    <li key={idx} className="text-white/90">{entry}</li>
                ))}
            </ul>
        </div>
    )
}

export default MemoryLog;