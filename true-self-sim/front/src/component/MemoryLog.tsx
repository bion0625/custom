import type {MemoryLogProps} from "../props.ts";

const MemoryLog: React.FC<MemoryLogProps> = ({log}) => {
    return (
        <div className="absolute top-4 right-4 w-72 h-80 overflow-y-auto bg-black/60 text-sm p-4 rounded-lg shadow-inner text-white">
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