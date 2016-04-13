package net.trackmate.revised.model.mamut;

import net.trackmate.revised.model.undo.UndoableEdit;
import net.trackmate.revised.undo.UndoIdBimap;

// TODO: remove this class. Only left here for showing how non-Ref UndoableEdits are implemented
public class RemoveVertexUndoableEdit implements UndoableEdit
{
	private boolean isUndoPoint;

	private final ModelGraph modelGraph;

	private final UndoIdBimap< Spot > undoVertexIdBimap;

	private final int undoVertexId;

	private final int timepoint;

	private final double[] pos;

	private final double[][] cov;

	public RemoveVertexUndoableEdit(
			final ModelGraph modelGraph,
			final UndoIdBimap< Spot > undoVertexIdBimap,
			final Spot spot )
	{
		this.isUndoPoint = false;
		this.modelGraph = modelGraph;
		this.undoVertexIdBimap = undoVertexIdBimap;
		undoVertexId = undoVertexIdBimap.getId( spot );
		timepoint = spot.getTimepoint();
		pos = new double[ 3 ];
		spot.localize( pos );
		cov = new double[ 3 ][ 3 ];
		spot.getCovariance( cov );
	}

	@Override
	public void undo()
	{
		final Spot ref = modelGraph.vertexRef();
		final Spot spot = modelGraph.addVertex( ref ).init( timepoint, pos, cov );
		undoVertexIdBimap.put( undoVertexId, spot );
		modelGraph.notifyVertexAdded( spot );
		modelGraph.releaseRef( ref );
	}

	@Override
	public void redo()
	{
		final Spot ref = modelGraph.vertexRef();
		final Spot spot = undoVertexIdBimap.getObject( undoVertexId, ref );
		modelGraph.remove( spot );
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
