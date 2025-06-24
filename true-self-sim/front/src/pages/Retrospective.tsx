import type { RetrospectiveProps } from "../props.ts";
import FullLog from "../component/FullLog.tsx";

export const Retrospective: React.FC<RetrospectiveProps> = ({ log, onRestart }) => {
    return (
        <div className="min-h-screen bg-black text-white flex items-center justify-center">
            <FullLog log={log} onRestart={onRestart} />
        </div>
    );
};