package net.trackmate.model.abstractmodel;

import java.util.Iterator;

public class IncomingSpotEdges< E extends AbstractEdge< ?, ? > > implements SpotEdges< E >
{
	private final AbstractSpot< ?, ? > spot;
	private final AbstractEdgePool< E, ?, ? > edgePool;

	private IncomingEdgesIterator iterator;

	public IncomingSpotEdges(
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
		int edgeIndex = spot.getFirstInEdgeIndex();
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
		return get( i, edgePool.createEmptyRef() );
	}

	// garbage-free version
	@Override
	public E get( int i, final E edge )
	{
		int edgeIndex = spot.getFirstInEdgeIndex();
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
		private int edgeIndex;

		private final E edge;

		public IncomingEdgesIterator()
		{
			this.edge = edgePool.createEmptyRef();
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
			edgePool.release( edge );
		}
	}
}
