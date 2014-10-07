package net.trackmate.graph;

import net.trackmate.graph.mempool.MappedElement;

public class AbstractEdgePool<
			E extends AbstractEdge< E, V, T >,
			V extends AbstractVertex< V, ?, ? >,
			T extends MappedElement >
		extends Pool< E, T > implements Iterable< E >
{
	final AbstractVertexPool< V, ?, ? > vertexPool;

	public AbstractEdgePool(
			final int initialCapacity,
			final PoolObject.Factory< E, T > edgeFactory,
			final AbstractVertexPool< V, ?, ? > vertexPool )
	{
		super( initialCapacity, edgeFactory );
		this.vertexPool = vertexPool;
	}

	// TODO: remove
	public E addEdge( final AbstractVertex< ?, ?, ? > source, final AbstractVertex< ?, ?, ? > target )
	{
		return addEdge( source, target, createRef() );
	}

	// garbage-free version
	public E addEdge( final AbstractVertex< ?, ?, ? > source, final AbstractVertex< ?, ?, ? > target, final E edge )
	{
		if ( getEdge( source, target, edge ) != null )
			return null;

		create( edge );
		edge.setSourceVertexInternalPoolIndex( source.getInternalPoolIndex() );
		edge.setTargetVertexInternalPoolIndex( target.getInternalPoolIndex() );

		final E tmp = createRef();

		final int sourceOutIndex = source.getFirstOutEdgeIndex();
		if ( sourceOutIndex < 0 )
		{
			// source has no outgoing edge yet. Set this one as the first.
			source.setFirstOutEdgeIndex( edge.getInternalPoolIndex() );
		}
		else
		{
			// source has outgoing edges. Append this one to the end of the linked list.
			getByInternalPoolIndex( sourceOutIndex, tmp );
			int nextSourceEdgeIndex = tmp.getNextSourceEdgeIndex();
			while( nextSourceEdgeIndex >= 0 )
			{
				getByInternalPoolIndex( nextSourceEdgeIndex, tmp );
				nextSourceEdgeIndex = tmp.getNextSourceEdgeIndex();
			}
			tmp.setNextSourceEdgeIndex( edge.getInternalPoolIndex() );
		}

		final int targetInIndex = target.getFirstInEdgeIndex();
		if ( targetInIndex < 0 )
		{
			// target has no incoming edge yet. Set this one as the first.
			target.setFirstInEdgeIndex( edge.getInternalPoolIndex() );
		}
		else
		{
			// target has incoming edges. Append this one to the end of the linked list.
			getByInternalPoolIndex( targetInIndex, tmp );
			int nextTargetEdgeIndex = tmp.getNextTargetEdgeIndex();
			while( nextTargetEdgeIndex >= 0 )
			{
				getByInternalPoolIndex( nextTargetEdgeIndex, tmp );
				nextTargetEdgeIndex = tmp.getNextTargetEdgeIndex();
			}
			tmp.setNextTargetEdgeIndex( edge.getInternalPoolIndex() );
		}

		releaseRef( tmp );
		return edge;
	}

	// TODO: remove
	public E getEdge( final AbstractVertex< ?, ?, ? > source, final AbstractVertex< ?, ?, ? > target )
	{
		return getEdge( source, target, createRef() );
	}

	// garbage-free version
	public E getEdge( final AbstractVertex< ?, ?, ? > source, final AbstractVertex< ?, ?, ? > target, final E edge )
	{
		int nextSourceEdgeIndex = source.getFirstOutEdgeIndex();
		if ( nextSourceEdgeIndex < 0 )
			return null;
		getByInternalPoolIndex( nextSourceEdgeIndex, edge );
		do
		{
			if ( edge.getTargetVertexInternalPoolIndex() == target.getInternalPoolIndex() )
				return edge;
			getByInternalPoolIndex( nextSourceEdgeIndex, edge );
			nextSourceEdgeIndex = edge.getNextSourceEdgeIndex();
		}
		while ( nextSourceEdgeIndex >= 0 );
		return null;
	}

	public void releaseAllLinkedEdges( final AbstractVertex< ?, ?, ? > vertex )
	{
		final V tmpSpot = vertexPool.createRef();
		final E edge = createRef();
		final E tmpEdge = createRef();

		// release all outgoing edges
		int index = vertex.getFirstOutEdgeIndex();
		vertex.setFirstOutEdgeIndex( -1 );
		while ( index >= 0 )
		{
			getByInternalPoolIndex( index, edge );
			unlinkFromTarget( edge, tmpEdge, tmpSpot );
			index = edge.getNextSourceEdgeIndex();
			releaseByInternalPoolIndex( edge.getInternalPoolIndex() );
		}

		// release all incoming edges
		index = vertex.getFirstInEdgeIndex();
		vertex.setFirstInEdgeIndex( -1 );
		while ( index >= 0 )
		{
			getByInternalPoolIndex( index, edge );
			unlinkFromSource( edge, tmpEdge, tmpSpot );
			index = edge.getNextTargetEdgeIndex();
			releaseByInternalPoolIndex( edge.getInternalPoolIndex() );
		}

		// TODO: use Pool.releaseRefs() when it is available
		vertexPool.releaseRef( tmpSpot );
		releaseRef( edge );
		releaseRef( tmpEdge );
	}

	public void release( final E edge )
	{
		final V tmpSpot = vertexPool.createRef();
		final E tmp = createRef();

		unlinkFromSource( edge, tmp, tmpSpot );
		unlinkFromTarget( edge, tmp, tmpSpot );
		releaseByInternalPoolIndex( edge.getInternalPoolIndex() );

		// TODO: use Pool.releaseRefs() when it is available
		vertexPool.releaseRef( tmpSpot );
		releaseRef( tmp );
	}

	/*
	 *
	 * Internal stuff.
	 * If it should be necessary for performance reasons, these can be made protected or public
	 *
	 */

	private void unlinkFromSource( final E edge, final E tmpEdge, final V tmpVertex )
	{
		vertexPool.getByInternalPoolIndex( edge.getSourceVertexInternalPoolIndex(), tmpVertex );
		final int sourceOutIndex = tmpVertex.getFirstOutEdgeIndex();
		if ( sourceOutIndex == edge.getInternalPoolIndex() )
		{
			// this edge is the first in the source's list of outgoing edges
			tmpVertex.setFirstOutEdgeIndex( edge.getNextSourceEdgeIndex() );
		}
		else
		{
			// find this edge in the source's list of outgoing edges and remove it
			getByInternalPoolIndex( sourceOutIndex, tmpEdge );
			int nextSourceEdgeIndex = tmpEdge.getNextSourceEdgeIndex();
			while( nextSourceEdgeIndex != edge.getInternalPoolIndex() )
			{
				getByInternalPoolIndex( nextSourceEdgeIndex, tmpEdge );
				nextSourceEdgeIndex = tmpEdge.getNextSourceEdgeIndex();
			}
			tmpEdge.setNextSourceEdgeIndex( edge.getNextSourceEdgeIndex() );
		}
	}

	private void unlinkFromTarget( final E edge, final E tmpEdge, final V tmpVertex )
	{
		vertexPool.getByInternalPoolIndex( edge.getTargetVertexInternalPoolIndex(), tmpVertex );
		final int targetInIndex = tmpVertex.getFirstInEdgeIndex();
		if ( targetInIndex == edge.getInternalPoolIndex() )
		{
			// this edge is the first in the target list of incoming edges
			tmpVertex.setFirstInEdgeIndex( edge.getNextTargetEdgeIndex() );
		}
		else
		{
			// find this edge in the target's list of incoming edges and remove it
			getByInternalPoolIndex( targetInIndex, tmpEdge );
			int nextTargetEdgeIndex = tmpEdge.getNextTargetEdgeIndex();
			while( nextTargetEdgeIndex >= 0 )
			{
				getByInternalPoolIndex( nextTargetEdgeIndex, tmpEdge );
				nextTargetEdgeIndex = tmpEdge.getNextTargetEdgeIndex();
			}
			tmpEdge.setNextTargetEdgeIndex( edge.getNextTargetEdgeIndex() );
		}
	}
}
