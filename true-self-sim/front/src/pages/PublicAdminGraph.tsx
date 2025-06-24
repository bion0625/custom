import React, { useState, useCallback, useRef, useEffect } from 'react';
import ReactFlow, {
    MiniMap,
    Controls,
    Background,
    addEdge,
    MarkerType,
    ReactFlowProvider,
    useNodesState,
    useEdgesState,
} from 'reactflow';
import type {
    Node as FlowNode,
    Edge as FlowEdge,
    Connection,
} from 'reactflow';
import 'reactflow/dist/style.css';

import EditableNode from '../component/EditableNode';
import type { EditableNodeData, NodeFormData } from '../component/EditableNode';
import usePublicStory from "../hook/usePublicStory.ts";
import usePostPublicSceneBulk from "../hook/usePostPublicSceneBulk.ts";
import useDeletePublicScene from "../hook/useDeletePublicScene.ts";
import { useNavigate } from 'react-router-dom';

type Selection = {
    nodes: FlowNode[];
    edges: FlowEdge[];
};

const nodeTypes = { editableNode: EditableNode };

interface ChoiceRequest { text: string; nextSceneId: string; }
interface Scene { sceneId: string; speaker: string; backgroundImage: string; text: string; start: boolean; end: boolean; choiceRequests: ChoiceRequest[]; }

// Convert nodes and edges to JSON
const exportAsScenes = (nodes: FlowNode<EditableNodeData>[], edges: FlowEdge[]): Scene[] =>
    nodes.map((node) => {
        const data = node.data;
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

// Convert nodes and edges to API request type
const toRequests = (nodes: FlowNode<EditableNodeData>[], edges: FlowEdge[]) =>
    nodes.map((node) => {
        const data = node.data;
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
    const [nodes, setNodes, onNodesChange] = useNodesState<EditableNodeData>([]);
    const [edges, setEdges, onEdgesChange] = useEdgesState([]);
    const [selection, setSelection] = useState<Selection>({ nodes: [], edges: [] });
    const [edgeLabel, setEdgeLabel] = useState('');
    const { data } = usePublicStory();
    const { mutate: saveBulk, isPending } = usePostPublicSceneBulk();
    const { mutate: deletePublicScene, isPending: isDeletePending } = useDeletePublicScene();
    const navigate = useNavigate();

    const handleNodeUpdate = useCallback((id: string, newData: NodeFormData) => {
        setNodes((nds) =>
            nds.map((n) =>
                n.id === id
                    ? { ...n, data: { ...newData, onUpdate: handleNodeUpdate } }
                    : n
            )
        );
    }, [setNodes]);

    useEffect(() => {
        if (!data) return;
        const loadedNodes = data.publicScenes.map((scene, idx) => ({
            id: scene.sceneId,
            type: 'editableNode',
            position: { x: idx * 200, y: 50 },
            data: {
                sceneId: scene.sceneId,
                speaker: scene.speaker,
                backgroundImage: scene.backgroundImage,
                text: scene.text,
                start: scene.start,
                end: scene.end,
                onUpdate: handleNodeUpdate,
            },
        }));
        const loadedEdges = data.publicScenes.flatMap((scene) =>
            scene.texts.map((choice) => ({
                id: `${scene.sceneId}-${choice.nextPublicSceneId}`,
                source: scene.sceneId,
                target: choice.nextPublicSceneId,
                label: choice.text,
                markerEnd: { type: MarkerType.ArrowClosed },
            }))
        );
        setNodes(loadedNodes);
        setEdges(loadedEdges);
        idCounter.current = loadedNodes.length + 1;
    }, [data, setNodes, setEdges, handleNodeUpdate]);

    function handleDeleteScene() {
        const selNode = selection.nodes[0];
        if (!selNode) return;
        if (!confirm("정말로 이 장면을 삭제하시겠습니까?")) return;
        deletePublicScene(selNode.id, {
            onSuccess: () => {
                setNodes((nds) => nds.filter((n) => n.id !== selNode.id));
                setEdges((eds) => eds.filter((e) => e.source !== selNode.id && e.target !== selNode.id));
                setSelection({ nodes: [], edges: [] });
            }
        });
    }

    function handleDeleteEdge() {
        const selEdge = selection.edges[0]; if (!selEdge) return;
        setEdges((eds) => eds.filter((e) => e.id !== selEdge.id));
        setSelection({ nodes: [], edges: [] });
    }

    const onConnect = useCallback((connection: Connection) => {
        // Add edge with temporary empty label
        setEdges((eds) => addEdge({ ...connection, label: '' , markerEnd: { type: MarkerType.ArrowClosed } }, eds));
    }, [setEdges]);

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

    // keep end flag in sync with outgoing edges and enforce single start node
    useEffect(() => {
        setNodes((nds) => {
            if (nds.length === 0) return nds;
            const firstId = nds[0].id;
            return nds.map((n) => {
                const hasOutgoing = edges.some((e) => e.source === n.id);
                const shouldStart = n.id === firstId;
                const shouldEnd = !hasOutgoing;
                if (n.data.start === shouldStart && n.data.end === shouldEnd) return n;
                return { ...n, data: { ...n.data, start: shouldStart, end: shouldEnd } };
            });
        });
    }, [edges, nodes.length, setNodes]);

    const handleExport = () => {
        const scenes = exportAsScenes(nodes, edges);
        const blob = new Blob([JSON.stringify(scenes, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a'); a.href = url; a.download = 'scenes.json'; a.click(); URL.revokeObjectURL(url);
    };

    const handleSave = () => {
        const requests = toRequests(nodes, edges);
        saveBulk(requests);
    };

    return (
        <ReactFlowProvider>
        <div style={{ width: '100%', height: '100vh', position: 'relative' }}>
            <div style={{ position: 'absolute', zIndex: 10, top: 10, left: 10, background: 'rgba(255,255,255,0.9)', padding: 8, borderRadius: 4 }}>
                <button onClick={() => navigate('/admin/public')} style={{ marginRight: 8 }}>돌아가기</button>
                <button onClick={handleAddScene} style={{ marginRight: 8 }}>New Scene</button>
                <button onClick={handleExport} style={{ marginRight: 8 }}>Export JSON</button>
                <button onClick={handleSave} style={{ marginRight: 8 }} disabled={isPending}>Save</button>
                <button
                    onClick={handleDeleteScene}
                    disabled={selection.nodes.length === 0 || isDeletePending}
                    style={{ marginRight: 8 }}
                >
                    {isDeletePending ? "Deleting…" : "Delete Scene"}
                </button>
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
        </ReactFlowProvider>
    );
};

export default PublicAdminGraph;
