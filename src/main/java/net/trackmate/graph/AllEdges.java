package net.trackmate.graph;

import java.util.Iterator;

public class AllEdges< E extends AbstractEdge< ?, ? > > implements Edges< E >
{
	private final AbstractVertex< ?, ? > vertex;
	private final AbstractEdgePool< E, ?, ? > edgePool;

	private EdgesIterator iterator;

	public AllEdges(
			final AbstractVertex< ?, ? > vertex,
			final AbstractEdgePool< E, ?, ? > edgePool )
	{
		this.vertex = vertex;
		this.edgePool = edgePool;

		iterator = null;
	}

	@Override
	public int size()
	{
		int numEdges = 0;
		int inEdgeIndex = vertex.getFirstInEdgeIndex();
		int outEdgeIndex = vertex.getFirstOutEdgeIndex();
		if ( inEdgeIndex >= 0 || outEdgeIndex >= 0 )
		{
			final E edge = edgePool.getTmpRef();
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
			edgePool.releaseTmpRef( edge );
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
	public E get( final int i, final E edge )
	{
		boolean in = true;
		int edgeIndex = vertex.getFirstInEdgeIndex();
		if ( edgeIndex < 0 )
		{
			in = false;
			edgeIndex = vertex.getFirstOutEdgeIndex();
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
					edgeIndex = vertex.getFirstOutEdgeIndex();
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
		private int edgeIndex;

		private boolean in;

		private final E edge;

		public EdgesIterator()
		{
			this.edge = edgePool.createEmptyRef();
			reset();
		}

		public void reset()
		{
			edgeIndex = vertex.getFirstInEdgeIndex();
			if ( edgeIndex < 0 )
			{
				in = false;
				edgeIndex = vertex.getFirstOutEdgeIndex();
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
					edgeIndex = vertex.getFirstOutEdgeIndex();
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
