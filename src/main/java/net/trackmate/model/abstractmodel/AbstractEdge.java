package net.trackmate.model.abstractmodel;

import static net.trackmate.util.mempool.ByteUtils.INDEX_SIZE;
import net.trackmate.util.mempool.MappedElement;

public class AbstractEdge< T extends MappedElement, V extends AbstractVertex< ?, ? > > extends PoolObject< T >
{
	protected static final int SOURCE_INDEX_OFFSET = 0;
	protected static final int TARGET_INDEX_OFFSET = SOURCE_INDEX_OFFSET + INDEX_SIZE;
	protected static final int NEXT_SOURCE_EDGE_INDEX_OFFSET = TARGET_INDEX_OFFSET + INDEX_SIZE;
	protected static final int NEXT_TARGET_EDGE_INDEX_OFFSET = NEXT_SOURCE_EDGE_INDEX_OFFSET + INDEX_SIZE;
	protected static final int SIZE_IN_BYTES = NEXT_TARGET_EDGE_INDEX_OFFSET + INDEX_SIZE;

	protected final AbstractVertexPool< V, ?, ? > vertexPool;

	protected AbstractEdge( final AbstractEdgePool< ?, T, V > pool )
	{
		super( pool.getMemPool() );
		this.vertexPool = pool.vertexPool;
	}

	protected int getSourceVertexInternalPoolIndex()
	{
		return access.getIndex( SOURCE_INDEX_OFFSET );
	}

	protected void setSourceVertexInternalPoolIndex( final int index )
	{
		access.putIndex( index, SOURCE_INDEX_OFFSET );
	}

	protected int getTargetVertexInternalPoolIndex()
	{
		return access.getIndex( TARGET_INDEX_OFFSET );
	}

	protected void setTargetVertexInternalPoolIndex( final int index )
	{
		access.putIndex( index, TARGET_INDEX_OFFSET );
	}

	protected int getNextSourceEdgeIndex()
	{
		return access.getIndex( NEXT_SOURCE_EDGE_INDEX_OFFSET );
	}

	protected void setNextSourceEdgeIndex( final int index )
	{
		access.putIndex( index, NEXT_SOURCE_EDGE_INDEX_OFFSET );
	}

	protected int getNextTargetEdgeIndex()
	{
		return access.getIndex( NEXT_TARGET_EDGE_INDEX_OFFSET );
	}

	protected void setNextTargetEdgeIndex( final int index )
	{
		access.putIndex( index, NEXT_TARGET_EDGE_INDEX_OFFSET );
	}

	@Override
	protected void setToUninitializedState()
	{
		setNextSourceEdgeIndex( -1 );
		setNextTargetEdgeIndex( -1 );
	}

	protected V getSourceVertex( final V vertex )
	{
		vertexPool.getByInternalPoolIndex( getSourceVertexInternalPoolIndex(), vertex );
		return vertex;
	}

	protected V getTargetVertex( final V vertex )
	{
		vertexPool.getByInternalPoolIndex( getTargetVertexInternalPoolIndex(), vertex );
		return vertex;
	}


	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof AbstractEdge< ?, ? > &&
				access.equals( ( ( AbstractEdge< ?, ? > ) obj ).access );
	}

	@Override
	public int hashCode()
	{
		return access.hashCode();
	}
}
