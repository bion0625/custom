import { BaseEdge, EdgeLabelRenderer, type EdgeProps } from 'reactflow';

const OFFSET_DISTANCE = 40;

export default function CurvedEdge({ id, sourceX, sourceY, targetX, targetY, markerEnd, style, label, data }: EdgeProps) {
  const index = data?.offset ?? 0;
  const offset = index * OFFSET_DISTANCE;
  const horizontal = Math.abs(sourceX - targetX) > Math.abs(sourceY - targetY);
  const controlX = (sourceX + targetX) / 2 + (horizontal ? 0 : offset);
  const controlY = (sourceY + targetY) / 2 + (horizontal ? offset : 0);

  const path = `M${sourceX},${sourceY} Q${controlX},${controlY} ${targetX},${targetY}`;

  return (
    <>
      <BaseEdge id={id} path={path} markerEnd={markerEnd} style={style} />
      {(data?.label || label) && (
        <EdgeLabelRenderer>
          <div
            style={{
              position: 'absolute',
              transform: `translate(-50%, -50%) translate(${controlX}px, ${controlY}px)`,
              pointerEvents: 'all',
              fontSize: 12,
            }}
            className="nodrag nopan"
          >
            {data?.label ?? label}
          </div>
        </EdgeLabelRenderer>
      )}
    </>
  );
}
