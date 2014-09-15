package net.trackmate.model.abstractmodel;

import java.util.Iterator;

public class OutgoingEdges< E extends AbstractEdge< ?, ? > > implements Edges< E >
{
	private final AbstractVertex< ?, ? > vertex;
	private final AbstractEdgePool< E, ?, ? > edgePool;

	private OutgoingEdgesIterator iterator;

	public OutgoingEdges(
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
		int edgeIndex = vertex.getFirstOutEdgeIndex();
		if ( edgeIndex >= 0 )
		{
			final E edge = edgePool.getTmpRef();
			while ( edgeIndex >= 0 )
			{
				++numEdges;
				edgePool.getByInternalPoolIndex( edgeIndex, edge );
				edgeIndex = edge.getNextSourceEdgeIndex();
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
	public E get( int i, final E edge )
	{
		int edgeIndex = vertex.getFirstOutEdgeIndex();
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
		private int edgeIndex;

		private final E edge;

		public OutgoingEdgesIterator()
		{
			this.edge = edgePool.createEmptyRef();
			reset();
		}

		public void reset()
		{
			edgeIndex = vertex.getFirstOutEdgeIndex();
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
