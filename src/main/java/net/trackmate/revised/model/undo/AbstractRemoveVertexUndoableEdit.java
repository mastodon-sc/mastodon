package net.trackmate.revised.model.undo;

import static net.trackmate.graph.mempool.ByteUtils.BOOLEAN_SIZE;
import static net.trackmate.graph.mempool.ByteUtils.INT_SIZE;

import net.trackmate.graph.AbstractEdge;
import net.trackmate.graph.AbstractVertex;
import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.revised.model.AbstractModelGraph;
import net.trackmate.revised.undo.UndoIdBimap;

public abstract class AbstractRemoveVertexUndoableEdit<
		A extends AbstractRemoveVertexUndoableEdit< A, V, E, T >,
		V extends AbstractVertex< V, E, T >,
		E extends AbstractEdge< E, V, T >,
		T extends MappedElement >
	extends AbstractUndoableEditRef< A >
{
	protected static final int IS_UNDO_POINT_OFFSET = 0;
	protected static final int VERTEX_UNDO_ID_OFFSET = IS_UNDO_POINT_OFFSET + BOOLEAN_SIZE;
	protected static final int SIZE_IN_BYTES = VERTEX_UNDO_ID_OFFSET + INT_SIZE;

	private final AbstractModelGraph< ?, ?, V, E, ? > modelGraph;

	private final UndoIdBimap< V > vertexUndoIdBimap;

	protected AbstractRemoveVertexUndoableEdit(
			final UndoableEditPool< A > pool,
			final AbstractModelGraph< ?, ?, V, E, ? > modelGraph,
			final UndoIdBimap< V > vertexUndoIdBimap
			)
	{
		super( pool );
		this.modelGraph = modelGraph;
		this.vertexUndoIdBimap = vertexUndoIdBimap;
	}

	@SuppressWarnings( "unchecked" )
	public A init( final V vertex )
	{
		setUndoPoint( false );
		setVertexUndoId( vertexUndoIdBimap.getId( vertex ) );
		return ( A ) this;
	}

	@Override
	public void undo()
	{
		final V ref = modelGraph.vertexRef();
		final V vertex = modelGraph.addVertex( ref );
		setVertexFields( vertex );
		vertexUndoIdBimap.put( getVertexUndoId(), vertex );
		modelGraph.notifyVertexAdded( vertex );
		modelGraph.releaseRef( ref );
	}

	protected abstract void setVertexFields( final V vertex );

	@Override
	public void redo()
	{
		final V ref = modelGraph.vertexRef();
		final V vertex = vertexUndoIdBimap.getObject( getVertexUndoId(), ref );
		modelGraph.remove( vertex );
		modelGraph.releaseRef( ref );
	}

	@Override
	public boolean isUndoPoint()
	{
		return access.getBoolean( IS_UNDO_POINT_OFFSET );
	}

	@Override
	public void setUndoPoint( final boolean isUndoPoint )
	{
		access.putBoolean( isUndoPoint, IS_UNDO_POINT_OFFSET );
	}

	protected int getVertexUndoId()
	{
		return access.getInt( VERTEX_UNDO_ID_OFFSET );
	}

	protected void setVertexUndoId( final int id )
	{
		access.putInt( id, VERTEX_UNDO_ID_OFFSET );
	}
}
