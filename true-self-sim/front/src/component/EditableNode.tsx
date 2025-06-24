import { memo, useState } from 'react';
import { Handle, Position } from 'reactflow';

export interface NodeFormData {
    sceneId: string;
    speaker: string;
    backgroundImage: string;
    text: string;
    start: boolean;
    end: boolean;
}

export interface EditableNodeData extends NodeFormData {
    onUpdate: (id: string, data: NodeFormData) => void;
}

export interface EditableNodeProps {
    id: string;
    data: EditableNodeData;
    selected: boolean;
}

const EditableNode: React.FC<EditableNodeProps> = memo(({ id, data, selected }) => {
    const [editMode, setEditMode] = useState(false);
    const [fields, setFields] = useState<NodeFormData>({
        sceneId: data.sceneId,
        speaker: data.speaker,
        backgroundImage: data.backgroundImage,
        text: data.text,
        start: data.start,
        end: data.end,
    });

    const onDoubleClick = () => {
        setFields({ ...data });
        setEditMode(true);
    };

    const onChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value, type, checked } = e.target;
        setFields({ ...fields, [name]: type === 'checkbox' ? checked : value });
    };

    const onSave = () => {
        data.onUpdate(id, fields);
        setEditMode(false);
    };

    return (
        <div
            onDoubleClick={onDoubleClick}
            style={{
                padding: 10,
                border: selected ? '2px solid #007aff' : '1px solid #777',
                borderRadius: 5,
                background: '#fff',
                width: 180,
            }}
        >
            <Handle type="target" position={Position.Top} style={{ background: '#555' }} />
            {editMode ? (
                <div style={{ display: 'flex', flexDirection: 'column' }}>
                    <input name="sceneId" value={fields.sceneId} onChange={onChange} placeholder="sceneId" />
                    <input name="speaker" value={fields.speaker} onChange={onChange} placeholder="speaker" />
                    <input name="backgroundImage" value={fields.backgroundImage} onChange={onChange} placeholder="backgroundImage" />
                    <textarea name="text" value={fields.text} onChange={onChange} rows={3} placeholder="text" />
                    <button
                        onClick={onSave}
                        disabled={
                            !fields.sceneId.trim() ||
                            !fields.speaker.trim() ||
                            !fields.backgroundImage.trim() ||
                            !fields.text.trim()
                        }
                        className="mt-1 px-2 py-1 rounded bg-blue-500 text-white disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        Save
                    </button>
                </div>
            ) : (
                <div>
                    <strong>{data.sceneId}</strong>
                    <div style={{ fontStyle: 'italic', fontSize: 12 }}>{data.speaker}</div>
                    <div style={{ fontSize: 12, marginTop: 4 }}>{data.text}</div>
                    {data.start && <div style={{ color: 'green', fontSize: 10 }}>Start</div>}
                    {data.end && <div style={{ color: 'red', fontSize: 10 }}>End</div>}
                </div>
            )}
            <Handle type="source" position={Position.Bottom} id="source" style={{ background: '#555' }} />
        </div>
    );
});

export default EditableNode;
