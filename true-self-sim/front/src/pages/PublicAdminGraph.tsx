import React, { useState, useCallback, useRef, useEffect } from 'react';
import ReactFlow, {
    MiniMap,
    Controls,
    Background,
    MarkerType,
    ReactFlowProvider,
    useNodesState,
    useEdgesState,
    applyEdgeChanges,
    ConnectionMode,
    type EdgeChange,
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
import { backgroundImgs } from "../constants/backgroundImages.ts";
import CurvedEdge from '../component/CurvedEdge';

type Selection = {
    nodes: FlowNode[];
    edges: FlowEdge[];
};

const nodeTypes = { editableNode: EditableNode };
const edgeTypes = { curved: CurvedEdge };

const assignOffsets = (eds: FlowEdge[]): FlowEdge[] => {
    const groups: Record<string, FlowEdge[]> = {};
    for (const e of eds) {
        const key = `${e.source}-${e.target}`;
        if (!groups[key]) groups[key] = [];
        groups[key].push(e);
    }
    return eds.map((e) => {
        const group = groups[`${e.source}-${e.target}`];
        const index = group.indexOf(e);
        const offsetIndex = index - (group.length - 1) / 2;
        return {
            ...e,
            type: 'curved',
            data: { ...(e.data || {}), label: e.label, offset: offsetIndex },
        };
    });
};

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
    const [edges, setEdges] = useEdgesState<FlowEdge[]>([]);
    const onEdgesChange = useCallback((changes: EdgeChange[]) => {
        setEdges((eds) => assignOffsets(applyEdgeChanges(changes, eds)));
    }, [setEdges]);
    const [selection, setSelection] = useState<Selection>({ nodes: [], edges: [] });
    const [edgeLabel, setEdgeLabel] = useState('');
    const { data } = usePublicStory();
    const { mutate: saveBulk, isPending } = usePostPublicSceneBulk();
    const { mutate: deletePublicScene, isPending: isDeletePending } = useDeletePublicScene();
    const [errorMsg, setErrorMsg] = useState('');
    const [successMsg, setSuccessMsg] = useState('');
    const [invalidNodes, setInvalidNodes] = useState<Record<string, boolean>>({});
    const navigate = useNavigate();

    const handleNodeUpdate = useCallback((id: string, newData: NodeFormData) => {
        setNodes((nds) =>
            nds.map((n) =>
                n.id === id
                    ? { ...n, data: { ...newData, onUpdate: handleNodeUpdate } }
                    : n
            )
        );
        setInvalidNodes((prev) => {
            const copy = { ...prev };
            delete copy[id];
            return copy;
        });
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
                invalid: false,
                onUpdate: handleNodeUpdate,
            },
        }));
        const loadedEdges = data.publicScenes.flatMap((scene) =>
            scene.texts.map((choice, idx) => ({
                id: `${scene.sceneId}-${choice.nextPublicSceneId}-${idx}`,
                source: scene.sceneId,
                target: choice.nextPublicSceneId,
                label: choice.text,
                markerEnd: { type: MarkerType.ArrowClosed },
            }))
        );
        setNodes(loadedNodes);
        setEdges(assignOffsets(loadedEdges));
        idCounter.current = loadedNodes.length + 1;
    }, [data, setNodes, setEdges, handleNodeUpdate]);

    function handleDeleteScene() {
        const selNode = selection.nodes[0];
        if (!selNode) return;
        if (!confirm("정말로 이 장면을 삭제하시겠습니까?")) return;
        deletePublicScene(selNode.id, {
            onSuccess: () => {
                setNodes((nds) => nds.filter((n) => n.id !== selNode.id));
                setEdges((eds) => assignOffsets(eds.filter((e) => e.source !== selNode.id && e.target !== selNode.id)));
                setSelection({ nodes: [], edges: [] });
            }
        });
    }

    function handleDeleteEdge() {
        const selEdge = selection.edges[0]; if (!selEdge) return;
        setEdges((eds) => assignOffsets(eds.filter((e) => e.id !== selEdge.id)));
        setSelection({ nodes: [], edges: [] });
    }


    const onConnect = useCallback((connection: Connection) => {
        const newEdge: FlowEdge = {
            id: `e${Date.now()}`,
            source: connection.source!,
            target: connection.target!,
            sourceHandle: connection.sourceHandle,
            targetHandle: connection.targetHandle,
            type: 'default',
            markerEnd: { type: MarkerType.ArrowClosed },
        };
        setEdges((eds) =>
            // 기존 addEdge 대신, 배열에 직접 추가
            assignOffsets([...eds, newEdge])
        );
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
        setEdges((eds) =>
            assignOffsets(
                eds.map((edge) =>
                    edge.id === edgeId ? { ...edge, label: edgeLabel } : edge
                )
            )
        );
    };

    const handleAddScene = () => {
        const newId = `s${idCounter.current++}`;
        setNodes((nds) => nds.concat({
            id: newId,
            type: 'editableNode',
            position: { x: 100, y: 100 },
            data: {
                sceneId: newId,
                speaker: '',
                backgroundImage: backgroundImgs[0],
                text: '',
                start: false,
                end: false,
                invalid: false,
                onUpdate: handleNodeUpdate
            }
        }));
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

    // update invalid flag styling
    useEffect(() => {
        setNodes((nds) =>
            nds.map((n) => ({
                ...n,
                data: { ...n.data, invalid: invalidNodes[n.id] ?? false },
            }))
        );
    }, [invalidNodes, setNodes]);

    const handleExport = () => {
        const scenes = exportAsScenes(nodes, edges);
        const blob = new Blob([JSON.stringify(scenes, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a'); a.href = url; a.download = 'scenes.json'; a.click(); URL.revokeObjectURL(url);
    };

    const handleSave = () => {
        setErrorMsg('');
        setSuccessMsg('');
        setInvalidNodes({});
        const requests = toRequests(nodes, edges);

        for (const scene of requests) {
            if (!scene.speaker.trim()) {
                setInvalidNodes({ [scene.sceneId]: true });
                setErrorMsg(`[${scene.sceneId}] 화자 입력이 필요합니다`);
                return;
            }
            if (!scene.backgroundImage.trim()) {
                setInvalidNodes({ [scene.sceneId]: true });
                setErrorMsg(`[${scene.sceneId}] backgroundImage가 비어 있습니다`);
                return;
            }
            if (!scene.text.trim()) {
                setInvalidNodes({ [scene.sceneId]: true });
                setErrorMsg(`[${scene.sceneId}] text가 비어 있습니다`);
                return;
            }
            for (const cr of scene.choiceRequests) {
                if (!cr.text.trim()) {
                    setInvalidNodes({ [scene.sceneId]: true });
                    setErrorMsg(`[${scene.sceneId}] 선택지 text가 비어 있습니다`);
                    return;
                }
                if (!cr.nextSceneId) {
                    setInvalidNodes({ [scene.sceneId]: true });
                    setErrorMsg(`[${scene.sceneId}] nextSceneId가 비어 있습니다`);
                    return;
                }
            }
        }

        saveBulk(requests, {
            onError: () => setErrorMsg('장면 저장에 실패했습니다.'),
            onSuccess: () => setSuccessMsg('장면 저장에 성공했습니다.'),
        });
    };

    return (
        <ReactFlowProvider>
        <div className="relative w-full h-screen">
            <div className="absolute z-10 top-4 left-4 bg-white/90 p-4 rounded shadow flex flex-wrap items-center gap-2">
                <button
                    onClick={() => navigate('/admin/public')}
                    className="px-2 py-1 bg-gray-200 rounded hover:bg-gray-300"
                >
                    돌아가기
                </button>
                <button
                    onClick={handleAddScene}
                    className="px-2 py-1 bg-green-600 text-white rounded hover:bg-green-700"
                >
                    New Scene
                </button>
                <button
                    onClick={handleExport}
                    className="px-2 py-1 bg-purple-600 text-white rounded hover:bg-purple-700"
                >
                    Export JSON
                </button>
                <button
                    onClick={handleSave}
                    disabled={isPending}
                    className="px-2 py-1 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
                >
                    Save
                </button>
                {errorMsg && <span className="text-red-500 mr-2">{errorMsg}</span>}
                {successMsg && <span className="text-green-600 mr-2">{successMsg}</span>}
                <button
                    onClick={handleDeleteScene}
                    disabled={selection.nodes.length === 0 || isDeletePending}
                    className="px-2 py-1 bg-red-600 text-white rounded hover:bg-red-700 disabled:opacity-50"
                >
                    {isDeletePending ? "Deleting…" : "Delete Scene"}
                </button>
                <button
                    onClick={handleDeleteEdge}
                    disabled={selection.edges.length === 0}
                    className="px-2 py-1 bg-red-500 text-white rounded hover:bg-red-600 disabled:opacity-50"
                >
                    Delete Edge
                </button>
                {selection.edges.length === 1 && (
                    <span className="flex items-center gap-1 ml-2">
                        <input
                            value={edgeLabel}
                            onChange={handleEdgeLabelChange}
                            placeholder="Edge label"
                            className="border rounded p-1 text-sm"
                        />
                        <button
                            onClick={handleEdgeLabelSave}
                            className="px-2 py-1 bg-blue-500 text-white rounded hover:bg-blue-600"
                        >
                            Save Label
                        </button>
                    </span>
                )}
            </div>
            <ReactFlow
                nodes={nodes}
                edges={edges}
                nodeTypes={nodeTypes}
                edgeTypes={edgeTypes}
                connectionMode={ConnectionMode.Loose}
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
