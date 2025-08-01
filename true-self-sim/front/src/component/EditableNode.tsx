import { memo, useState, useEffect } from 'react';
import { Handle, Position } from 'reactflow';
import { backgroundImgs } from '../constants/backgroundImages';

export interface NodeFormData {
    sceneId: string;
    speaker: string;
    backgroundImage: string;
    text: string;
    start: boolean;
    end: boolean;
    invalid?: boolean;
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
    const [useCustomImg, setUseCustomImg] = useState(false);
    const [fields, setFields] = useState<NodeFormData>({
        sceneId: data.sceneId,
        speaker: data.speaker,
        backgroundImage: data.backgroundImage,
        text: data.text,
        start: data.start,
        end: data.end,
        invalid: data.invalid,
    });


    // Keep local state in sync when start or end flags change externally
    useEffect(() => {
        setFields((prev) => ({ ...prev, start: data.start, end: data.end }));
    }, [data.start, data.end]);

    const onDoubleClick = () => {
        setFields({
            sceneId: data.sceneId,
            speaker: data.speaker,
            backgroundImage: data.backgroundImage,
            text: data.text,
            start: data.start,
            end: data.end,
            invalid: data.invalid,
        });
        setUseCustomImg(!backgroundImgs.includes(data.backgroundImage));
        setEditMode(true);
    }

    const onChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
        const target = e.target as HTMLInputElement;
        const { name, value, type, checked } = target;
        setFields({ ...fields, [name]: type === 'checkbox' ? checked : value });
    };

    const onSave = () => {
        data.onUpdate(id, fields);
        setEditMode(false);
    };

    const borderColor = data.invalid
        ? '2px solid red'
        : selected
            ? '2px solid #007aff'
            : '1px solid #777';

    return (
        <div
            onDoubleClick={onDoubleClick}
            style={{
                padding: 10,
                border: borderColor,
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
                    <label style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                        <input
                            type="checkbox"
                            checked={useCustomImg}
                            onChange={(e) => setUseCustomImg(e.target.checked)}
                        />
                        <span style={{ fontSize: 12 }}>Custom URL</span>
                    </label>
                    {useCustomImg ? (
                        <input
                            name="backgroundImage"
                            value={fields.backgroundImage}
                            onChange={onChange}
                            placeholder="backgroundImage"
                        />
                    ) : (
                        <select
                            name="backgroundImage"
                            value={fields.backgroundImage}
                            onChange={onChange}
                        >
                            <option value="">Select Background</option>
                            {backgroundImgs.map((img, idx) => (
                                <option key={idx} value={img}>{img}</option>
                            ))}
                        </select>
                    )}
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
