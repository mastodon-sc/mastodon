package pietzsch.spots;

import java.util.Iterator;

public class IncomingSpotEdges< E extends AbstractEdge< ? > > implements SpotEdges< E >
{
	private final AbstractSpot< ?, ? > spot;
	private final AbstractEdgePool< E, ? > edgePool;
	private final AbstractSpotPool< ? extends AbstractSpot< ?, ? >, ?, ? > spotPool;

	private IncomingEdgesIterator iterator;

	/**
	 * @param spotPool
	 *            May be null. This is only required if the
	 *            {@link Iterator#remove()} operation is used.
	 */
	public IncomingSpotEdges(
			final AbstractSpot< ?, ? > spot,
			final AbstractEdgePool< E, ? > edgePool,
			final AbstractSpotPool< ? extends AbstractSpot< ?, ? >, ?, ? > spotPool )
	{
		this.spot = spot;
		this.edgePool = edgePool;
		this.spotPool = spotPool;

		iterator = null;
	}

	@Override
	public int size()
	{
		int numEdges = 0;
		long edgeIndex = spot.getFirstInEdgeIndex();
		if ( edgeIndex >= 0 )
		{
			final E edge = edgePool.getTmpEdgeRef();
			while ( edgeIndex >= 0 )
			{
				++numEdges;
				edgePool.getByInternalPoolIndex( edgeIndex, edge );
				edgeIndex = edge.getNextTargetEdgeIndex();
			}
			edgePool.releaseTmpEdgeRef( edge );
		}
		return numEdges;
	}

	@Override
	public E get( final int i )
	{
		return get( i, edgePool.createEmptyEdgeRef() );
	}

	// garbage-free version
	@Override
	public E get( int i, final E edge )
	{
		long edgeIndex = spot.getFirstInEdgeIndex();
		edgePool.getByInternalPoolIndex( edgeIndex, edge );
		while( i-- > 0 )
		{
			edgeIndex = edge.getNextTargetEdgeIndex();
			edgePool.getByInternalPoolIndex( edgeIndex, edge );
		}
		return edge;

	}

	@Override
	public IncomingEdgesIterator iterator()
	{
		if ( iterator == null )
			iterator = new IncomingEdgesIterator();
		else
			iterator.reset();
		return iterator;
	}

	@Override
	public IncomingEdgesIterator safe_iterator()
	{
		return new IncomingEdgesIterator();
	}

	public class IncomingEdgesIterator implements Iterator< E >
	{
		private long edgeIndex;

		private final E edge;

		public IncomingEdgesIterator()
		{
			this.edge = edgePool.createEmptyEdgeRef();
			reset();
		}

		public void reset()
		{
			edgeIndex = spot.getFirstInEdgeIndex();
		}

		@Override
		public boolean hasNext()
		{
			return edgeIndex >= 0;
		}

		@Override
		public E next()
		{
			edgePool.getByInternalPoolIndex( edgeIndex, edge );
			edgeIndex = edge.getNextTargetEdgeIndex();
			return edge;
		}

		@Override
		public void remove()
		{
			if ( spotPool == null )
				throw new UnsupportedOperationException();
			else
				edgePool.release( edge, spotPool );
		}
	}
}
