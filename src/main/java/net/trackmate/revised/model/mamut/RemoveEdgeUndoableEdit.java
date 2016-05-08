package net.trackmate.revised.model.mamut;

import net.trackmate.revised.model.undo.old.UndoableEdit;
import net.trackmate.revised.undo.UndoIdBimap;

//TODO: remove this class. Only left here for showing how non-Ref UndoableEdits are implemented
public class RemoveEdgeUndoableEdit implements UndoableEdit
{
	private boolean isUndoPoint;

	private final ModelGraph modelGraph;

	private final UndoIdBimap< Spot > undoVertexIdBimap;

	private final UndoIdBimap< Link > undoEdgeIdBimap;

	private final int undoEdgeId;

	private final int undoSourceVertexId;

	private final int undoSourceOutIndex;

	private final int undoTargetVertexId;

	private final int undoTargetInIndex;

	public RemoveEdgeUndoableEdit(
			final ModelGraph modelGraph,
			final UndoIdBimap< Spot > undoVertexIdBimap,
			final UndoIdBimap< Link > undoEdgeIdBimap,
			final Link edge )
	{
		this.isUndoPoint = false;
		this.modelGraph = modelGraph;
		this.undoVertexIdBimap = undoVertexIdBimap;
		this.undoEdgeIdBimap = undoEdgeIdBimap;

		final Spot vref = modelGraph.vertexRef();

		undoEdgeId = undoEdgeIdBimap.getId( edge );

		final Spot source = edge.getSource( vref );
		undoSourceVertexId = undoVertexIdBimap.getId( source );
		undoSourceOutIndex = edge.getSourceOutIndex();

		final Spot target = edge.getTarget( vref );
		undoTargetVertexId = undoVertexIdBimap.getId( target );
		undoTargetInIndex = edge.getTargetInIndex();

		modelGraph.releaseRef( vref );
	}

	@Override
	public void undo()
	{
		final Link eref = modelGraph.edgeRef();
		final Spot vref1 = modelGraph.vertexRef();
		final Spot vref2 = modelGraph.vertexRef();
		final Spot source = undoVertexIdBimap.getObject( undoSourceVertexId, vref1 );
		final Spot target = undoVertexIdBimap.getObject( undoTargetVertexId, vref2 );
		final Link edge = modelGraph.insertEdge( source, undoSourceOutIndex, target, undoTargetInIndex, eref );
		undoEdgeIdBimap.put( undoEdgeId, edge );
		modelGraph.releaseRef( eref );
		modelGraph.releaseRef( vref1 );
		modelGraph.releaseRef( vref2 );
	}

	@Override
	public void redo()
	{
		final Link ref = modelGraph.edgeRef();
		final Link edge = undoEdgeIdBimap.getObject( undoEdgeId, ref );
		modelGraph.remove( edge );
		modelGraph.releaseRef( ref );
	}

	@Override
	public boolean isUndoPoint()
	{
		return isUndoPoint;
	}

	@Override
	public void setUndoPoint( final boolean isUndoPoint )
	{
		this.isUndoPoint = isUndoPoint;
	}
}
