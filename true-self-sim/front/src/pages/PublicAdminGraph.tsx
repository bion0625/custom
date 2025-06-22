import React, { useState, useCallback, useRef, memo } from 'react';
import ReactFlow, {
    MiniMap,
    Controls,
    Background,
    addEdge,
    applyNodeChanges,
    applyEdgeChanges,
    MarkerType,
    Handle,
    Position,
} from 'reactflow';
import type {
    Node as FlowNode,
    Edge as FlowEdge,
    Connection,
    NodeChange,
    EdgeChange,
    Selection,
} from 'reactflow';
import 'reactflow/dist/style.css';

// Custom editable node with handles
const EditableNode = memo(({ id, data, selected }: any) => {
    const [editMode, setEditMode] = useState(false);
    const [fields, setFields] = useState({ ...data });
    const onDoubleClick = () => setEditMode(true);
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
                    <label style={{ fontSize: 12 }}>
                        <input type="checkbox" name="start" checked={fields.start} onChange={onChange} /> Start
                    </label>
                    <label style={{ fontSize: 12 }}>
                        <input type="checkbox" name="end" checked={fields.end} onChange={onChange} /> End
                    </label>
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

const nodeTypes = { editableNode: EditableNode };

interface ChoiceRequest { text: string; nextSceneId: string; }
interface Scene { sceneId: string; speaker: string; backgroundImage: string; text: string; start: boolean; end: boolean; choiceRequests: ChoiceRequest[]; }

// Convert nodes and edges to JSON
const exportAsScenes = (nodes: FlowNode[], edges: FlowEdge[]): Scene[] =>
    nodes.map((node) => {
        const data: any = node.data;
        const outgoing = edges.filter((e) => e.source === node.id);
        return {
            sceneId: data.sceneId,
            speaker: data.speaker,
            backgroundImage: data.backgroundImage,
            text: data.text,
            start: data.start,
            end: data.end,
            choiceRequests: outgoing.map((e) => ({ text: e.label as string, nextSceneId: e.target })),
        };
    });

const PublicAdminGraph: React.FC = () => {
    const idCounter = useRef(1);
    const [nodes, setNodes] = useState<FlowNode[]>([]);
    const [edges, setEdges] = useState<FlowEdge[]>([]);
    const [selection, setSelection] = useState<Selection>({ nodes: [], edges: [] });
    const [edgeLabel, setEdgeLabel] = useState('');

    // Add initial node once
    React.useEffect(() => {
        const initialId = `s0`;
        setNodes([{ id: initialId, type: 'editableNode', position: { x: 50, y: 50 }, data: { sceneId: initialId, speaker: '', backgroundImage: '', text: '', start: true, end: false, onUpdate: handleNodeUpdate } }]);
    }, []);

    function handleNodeUpdate(id: string, newData: any) {
        setNodes((nds) => nds.map((n) => (n.id === id ? { ...n, data: { ...newData, onUpdate: handleNodeUpdate } } : n)));
    }

    function handleDeleteScene() {
        const selNode = selection.nodes[0]; if (!selNode) return;
        setNodes((nds) => nds.filter((n) => n.id !== selNode.id));
        setEdges((eds) => eds.filter((e) => e.source !== selNode.id && e.target !== selNode.id));
        setSelection({ nodes: [], edges: [] });
    }

    function handleDeleteEdge() {
        const selEdge = selection.edges[0]; if (!selEdge) return;
        setEdges((eds) => eds.filter((e) => e.id !== selEdge.id));
        setSelection({ nodes: [], edges: [] });
    }

    const onConnect = useCallback((connection: Connection) => {
        // Add edge with temporary empty label
        setEdges((eds) => addEdge({ ...connection, label: '' , markerEnd: { type: MarkerType.ArrowClosed } }, eds));
    }, []);

    const onNodesChange = useCallback((changes: NodeChange[]) => setNodes((nds) => applyNodeChanges(changes, nds)), []);
    const onEdgesChange = useCallback((changes: EdgeChange[]) => setEdges((eds) => applyEdgeChanges(changes, eds)), []);
    const onSelectionChange = useCallback((sel: Selection) => {
        setSelection(sel);
        if (sel.edges.length === 1) setEdgeLabel(sel.edges[0].label as string || '');
        else setEdgeLabel('');
    }, []);

    const handleEdgeLabelChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setEdgeLabel(e.target.value);
    };
    const handleEdgeLabelSave = () => {
        if (selection.edges.length !== 1) return;
        const edgeId = selection.edges[0].id;
        setEdges((eds) => eds.map((edge) => edge.id === edgeId ? { ...edge, label: edgeLabel } : edge));
    };

    const handleAddScene = () => {
        const newId = `s${idCounter.current++}`;
        setNodes((nds) => nds.concat({ id: newId, type: 'editableNode', position: { x: 100, y: 100 }, data: { sceneId: newId, speaker: '', backgroundImage: '', text: '', start: false, end: false, onUpdate: handleNodeUpdate } }));
    };

    const handleExport = () => {
        const scenes = exportAsScenes(nodes, edges);
        const blob = new Blob([JSON.stringify(scenes, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a'); a.href = url; a.download = 'scenes.json'; a.click(); URL.revokeObjectURL(url);
    };

    return (
        <div style={{ width: '100%', height: '100vh', position: 'relative' }}>
            <div style={{ position: 'absolute', zIndex: 10, top: 10, left: 10, background: 'rgba(255,255,255,0.9)', padding: 8, borderRadius: 4 }}>
                <button onClick={handleAddScene} style={{ marginRight: 8 }}>New Scene</button>
                <button onClick={handleExport} style={{ marginRight: 8 }}>Export JSON</button>
                <button onClick={handleDeleteScene} disabled={selection.nodes.length === 0} style={{ marginRight: 8 }}>Delete Scene</button>
                <button onClick={handleDeleteEdge} disabled={selection.edges.length === 0} style={{ marginRight: 8 }}>Delete Edge</button>
                {selection.edges.length === 1 && (
                    <span style={{ marginLeft: 8 }}>
            <input value={edgeLabel} onChange={handleEdgeLabelChange} placeholder="Edge label" style={{ marginRight: 4 }} />
            <button onClick={handleEdgeLabelSave}>Save Label</button>
          </span>
                )}
            </div>
            <ReactFlow
                nodes={nodes}
                edges={edges}
                nodeTypes={nodeTypes}
                onConnect={onConnect}
                onNodesChange={onNodesChange}
                onEdgesChange={onEdgesChange}
                onSelectionChange={onSelectionChange}
                fitView
            >
                <MiniMap />
                <Controls />
                <Background />
            </ReactFlow>
        </div>
    );
};

export default PublicAdminGraph;
