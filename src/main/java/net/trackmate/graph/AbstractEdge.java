package net.trackmate.graph;

import static net.trackmate.graph.mempool.ByteUtils.INDEX_SIZE;
import net.trackmate.graph.mempool.MappedElement;

/**
 * TODO: javadoc
 *
 * @param <E>
 * @param <V>
 * @param <T>
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class AbstractEdge< E extends AbstractEdge< E, V, T >, V extends AbstractVertex< V, ?, ? >, T extends MappedElement >
		extends PoolObject< E, T >
		implements Edge< V >
{
	protected static final int SOURCE_INDEX_OFFSET = 0;
	protected static final int TARGET_INDEX_OFFSET = SOURCE_INDEX_OFFSET + INDEX_SIZE;
	protected static final int NEXT_SOURCE_EDGE_INDEX_OFFSET = TARGET_INDEX_OFFSET + INDEX_SIZE;
	protected static final int NEXT_TARGET_EDGE_INDEX_OFFSET = NEXT_SOURCE_EDGE_INDEX_OFFSET + INDEX_SIZE;
	protected static final int SIZE_IN_BYTES = NEXT_TARGET_EDGE_INDEX_OFFSET + INDEX_SIZE;

	protected final AbstractVertexPool< V, ?, ? > vertexPool;

	protected AbstractEdge( final AbstractEdgePool< E, V, T > pool )
	{
		super( pool );
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

	@Override
	public V getSource()
	{
		return getSource( vertexPool.createRef() );
	}

	@Override
	public V getSource( final V vertex )
	{
		vertexPool.getByInternalPoolIndex( getSourceVertexInternalPoolIndex(), vertex );
		return vertex;
	}

	@Override
	public int getSourceOutIndex()
	{
		final V ref = vertexPool.createRef();
		final V source = getSource( ref );
		int outIndex = 0;
		for ( final Object e : source.outgoingEdges() )
		{
			if ( e.equals( this ) )
				break;
			++outIndex;
		}
		vertexPool.releaseRef( ref );
		return outIndex;
	}

	@Override
	public V getTarget()
	{
		return getTarget( vertexPool.createRef() );
	}

	@Override
	public V getTarget( final V vertex )
	{
		vertexPool.getByInternalPoolIndex( getTargetVertexInternalPoolIndex(), vertex );
		return vertex;
	}

	@Override
	public int getTargetInIndex()
	{
		final V ref = vertexPool.createRef();
		final V target = getTarget( ref );
		int inIndex = 0;
		for ( final Object e : target.incomingEdges() )
		{
			if ( e.equals( this ) )
				break;
			++inIndex;
		}
		vertexPool.releaseRef( ref );
		return inIndex;
	}
}
