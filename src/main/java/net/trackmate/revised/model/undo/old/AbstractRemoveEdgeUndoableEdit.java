package net.trackmate.revised.model.undo.old;

import static net.trackmate.graph.mempool.ByteUtils.BOOLEAN_SIZE;
import static net.trackmate.graph.mempool.ByteUtils.INT_SIZE;

import net.trackmate.graph.AbstractEdge;
import net.trackmate.graph.AbstractVertexWithFeatures;
import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.revised.model.AbstractModelGraph;
import net.trackmate.revised.undo.UndoIdBimap;

public abstract class AbstractRemoveEdgeUndoableEdit<
		A extends AbstractRemoveEdgeUndoableEdit< A, V, E, T >,
		V extends AbstractVertexWithFeatures< V, E, T >,
		E extends AbstractEdge< E, V, T >,
		T extends MappedElement >
	extends AbstractUndoableEditRef< A >
{
	private static final int IS_UNDO_POINT_OFFSET = 0;
	private static final int EDGE_UNDO_ID_OFFSET = IS_UNDO_POINT_OFFSET + BOOLEAN_SIZE;
	private static final int SOURCE_VERTEX_UNDO_ID_OFFSET = EDGE_UNDO_ID_OFFSET + INT_SIZE;
	private static final int SOURCE_OUT_INDEX_OFFSET = SOURCE_VERTEX_UNDO_ID_OFFSET + INT_SIZE;
	private static final int TARGET_VERTEX_UNDO_ID_OFFSET = SOURCE_OUT_INDEX_OFFSET + INT_SIZE;
	private static final int TARGET_IN_INDEX_OFFSET = TARGET_VERTEX_UNDO_ID_OFFSET + INT_SIZE;
	protected static final int SIZE_IN_BYTES = TARGET_IN_INDEX_OFFSET + INT_SIZE;

	private final AbstractModelGraph< ?, ?, V, E, ? > modelGraph;

	private final UndoIdBimap< V > vertexUndoIdBimap;

	private final UndoIdBimap< E > edgeUndoIdBimap;

	protected AbstractRemoveEdgeUndoableEdit(
			final UndoableEditPool< A > pool,
			final AbstractModelGraph< ?, ?, V, E, ? > modelGraph,
			final UndoIdBimap< V > vertexUndoIdBimap,
			final UndoIdBimap< E > edgeUndoIdBimap
			)
	{
		super( pool );
		this.modelGraph = modelGraph;
		this.vertexUndoIdBimap = vertexUndoIdBimap;
		this.edgeUndoIdBimap = edgeUndoIdBimap;
	}

	@SuppressWarnings( "unchecked" )
	public A init( final E edge )
	{
		setUndoPoint( false );
		final V vref = modelGraph.vertexRef();
		setEdgeUndoId( edgeUndoIdBimap.getId( edge ) );
		setSourceVertexUndoId( vertexUndoIdBimap.getId( edge.getSource( vref ) ) );
		setSourceOutIndex( edge.getSourceOutIndex() );
		setTargetVertexUndoId( vertexUndoIdBimap.getId( edge.getTarget( vref ) ) );
		setTargetInIndex( edge.getTargetInIndex() );
		modelGraph.releaseRef( vref );
		return ( A ) this;
	}

	@Override
	public void undo()
	{
		final E eref = modelGraph.edgeRef();
		final V vref1 = modelGraph.vertexRef();
		final V vref2 = modelGraph.vertexRef();
		final V source = vertexUndoIdBimap.getObject( getSourceVertexUndoId(), vref1 );
		final V target = vertexUndoIdBimap.getObject( getTargetVertexUndoId(), vref2 );
		final E edge = modelGraph.insertEdge( source, getSourceOutIndex(), target, getTargetInIndex(), eref );
		setEdgeFields( edge );
//		modelGraph.notifyEdgeAdded( edge ); // TODO: this should exist obviously, analogous to notifyVertexAdded()
		edgeUndoIdBimap.put( getEdgeUndoId(), edge );
		modelGraph.releaseRef( eref );
		modelGraph.releaseRef( vref1 );
		modelGraph.releaseRef( vref2 );
	}

	protected abstract void setEdgeFields( final E edge );

	@Override
	public void redo()
	{
		final E ref = modelGraph.edgeRef();
		final E edge = edgeUndoIdBimap.getObject( getEdgeUndoId(), ref );
		modelGraph.remove( edge );
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

	private int getEdgeUndoId()
	{
		return access.getInt( EDGE_UNDO_ID_OFFSET );
	}

	private void setEdgeUndoId( final int id )
	{
		access.putInt( id, EDGE_UNDO_ID_OFFSET );
	}

	private int getSourceVertexUndoId()
	{
		return access.getInt( SOURCE_VERTEX_UNDO_ID_OFFSET );
	}

	private void setSourceVertexUndoId( final int id )
	{
		access.putInt( id, SOURCE_VERTEX_UNDO_ID_OFFSET );
	}

	private int getSourceOutIndex()
	{
		return access.getInt( SOURCE_OUT_INDEX_OFFSET );
	}

	private void setSourceOutIndex( final int index )
	{
		access.putInt( index, SOURCE_OUT_INDEX_OFFSET );
	}

	private int getTargetVertexUndoId()
	{
		return access.getInt( TARGET_VERTEX_UNDO_ID_OFFSET );
	}

	private void setTargetVertexUndoId( final int id )
	{
		access.putInt( id, TARGET_VERTEX_UNDO_ID_OFFSET );
	}

	private int getTargetInIndex()
	{
		return access.getInt( TARGET_IN_INDEX_OFFSET );
	}

	private void setTargetInIndex( final int index )
	{
		access.putInt( index, TARGET_IN_INDEX_OFFSET );
	}
}
