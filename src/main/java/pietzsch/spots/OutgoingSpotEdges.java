package pietzsch.spots;

import java.util.Iterator;



public class OutgoingSpotEdges< E extends AbstractEdge< ?, ? > > implements SpotEdges< E >
{
	private final AbstractSpot< ?, ? > spot;
	private final AbstractEdgePool< E, ?, ? > edgePool;

	private OutgoingEdgesIterator iterator;

	public OutgoingSpotEdges(
			final AbstractSpot< ?, ? > spot,
			final AbstractEdgePool< E, ?, ? > edgePool )
	{
		this.spot = spot;
		this.edgePool = edgePool;

		iterator = null;
	}

	@Override
	public int size()
	{
		int numEdges = 0;
		long edgeIndex = spot.getFirstOutEdgeIndex();
		if ( edgeIndex >= 0 )
		{
			final E edge = edgePool.getTmpEdgeRef();
			while ( edgeIndex >= 0 )
			{
				++numEdges;
				edgePool.getByInternalPoolIndex( edgeIndex, edge );
				edgeIndex = edge.getNextSourceEdgeIndex();
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
		long edgeIndex = spot.getFirstOutEdgeIndex();
		edgePool.getByInternalPoolIndex( edgeIndex, edge );
		while( i-- > 0 )
		{
			edgeIndex = edge.getNextSourceEdgeIndex();
			edgePool.getByInternalPoolIndex( edgeIndex, edge );
		}
		return edge;

	}

	@Override
	public OutgoingEdgesIterator iterator()
	{
		if ( iterator == null )
			iterator = new OutgoingEdgesIterator();
		else
			iterator.reset();
		return iterator;
	}

	@Override
	public OutgoingEdgesIterator safe_iterator()
	{
		return new OutgoingEdgesIterator();
	}

	public class OutgoingEdgesIterator implements Iterator< E >
	{
		private long edgeIndex;

		private final E edge;

		public OutgoingEdgesIterator()
		{
			this.edge = edgePool.createEmptyEdgeRef();
			reset();
		}

		public void reset()
		{
			edgeIndex = spot.getFirstOutEdgeIndex();
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
			edgeIndex = edge.getNextSourceEdgeIndex();
			return edge;
		}

		@Override
		public void remove()
		{
			edgePool.release( edge );
		}
	}
}
