package pietzsch.spots;

import java.util.Iterator;

public class AllSpotEdges< E extends AbstractEdge< ?, ? > > implements SpotEdges< E >
{
	private final AbstractSpot< ?, ? > spot;
	private final AbstractEdgePool< E, ?, ? > edgePool;

	private EdgesIterator iterator;

	public AllSpotEdges(
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
		long inEdgeIndex = spot.getFirstInEdgeIndex();
		long outEdgeIndex = spot.getFirstOutEdgeIndex();
		if ( inEdgeIndex >= 0 || outEdgeIndex >= 0 )
		{
			final E edge = edgePool.getTmpEdgeRef();
			if ( inEdgeIndex >= 0 )
			{
				while ( inEdgeIndex >= 0 )
				{
					++numEdges;
					edgePool.getByInternalPoolIndex( inEdgeIndex, edge );
					inEdgeIndex = edge.getNextTargetEdgeIndex();
				}
			}
			if ( outEdgeIndex >= 0 )
			{
				while ( outEdgeIndex >= 0 )
				{
					++numEdges;
					edgePool.getByInternalPoolIndex( outEdgeIndex, edge );
					outEdgeIndex = edge.getNextSourceEdgeIndex();
				}
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
	public E get( final int i, final E edge )
	{
		boolean in = true;
		long edgeIndex = spot.getFirstInEdgeIndex();
		if ( edgeIndex < 0 )
		{
			in = false;
			edgeIndex = spot.getFirstOutEdgeIndex();
		}
		edgePool.getByInternalPoolIndex( edgeIndex, edge );
		while( i > 0 )
		{
			if ( in )
			{
				edgeIndex = edge.getNextTargetEdgeIndex();
				if ( edgeIndex < 0 )
				{
					in = false;
					edgeIndex = spot.getFirstOutEdgeIndex();
				}
			}
			else
				edgeIndex = edge.getNextSourceEdgeIndex();
			edgePool.getByInternalPoolIndex( edgeIndex, edge );
		}
		return edge;
	}

	@Override
	public EdgesIterator iterator()
	{
		if ( iterator == null )
			iterator = new EdgesIterator();
		else
			iterator.reset();
		return iterator;
	}

	@Override
	public EdgesIterator safe_iterator()
	{
		return new EdgesIterator();
	}

	public class EdgesIterator implements Iterator< E >
	{
		private long edgeIndex;

		private boolean in;

		private final E edge;

		public EdgesIterator()
		{
			this.edge = edgePool.createEmptyEdgeRef();
			reset();
		}

		public void reset()
		{
			edgeIndex = spot.getFirstInEdgeIndex();
			if ( edgeIndex < 0 )
			{
				in = false;
				edgeIndex = spot.getFirstOutEdgeIndex();
			}
			else
				in = true;
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
			if ( in )
			{
				edgeIndex = edge.getNextTargetEdgeIndex();
				if ( edgeIndex < 0 )
				{
					in = false;
					edgeIndex = spot.getFirstOutEdgeIndex();
				}
			}
			else
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
