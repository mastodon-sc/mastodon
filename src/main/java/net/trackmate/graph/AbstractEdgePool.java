package net.trackmate.graph;

import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.graph.zzrefcollections.Pool;
import net.trackmate.graph.zzrefcollections.PoolObject;

public class AbstractEdgePool<
			E extends AbstractEdge< E, V, T >,
			V extends AbstractVertex< V, ?, ? >,
			T extends MappedElement >
		extends Pool< E, T >
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

	public E insertEdge( final AbstractVertex< ?, ?, ? > source, final int sourceOutInsertAt, final AbstractVertex< ?, ?, ? > target, final int targetInInsertAt, final E edge )
	{
		if ( getEdge( source, target, edge ) != null )
			return null;

		create( edge );
		edge.setSourceVertexInternalPoolIndex( source.getInternalPoolIndex() );
		edge.setTargetVertexInternalPoolIndex( target.getInternalPoolIndex() );

		final E tmp = createRef();

		int nextSourceEdgeIndex = source.getFirstOutEdgeIndex();
		int insertIndex = 0;
		while( nextSourceEdgeIndex >= 0 && insertIndex < sourceOutInsertAt )
		{
			getByInternalPoolIndex( nextSourceEdgeIndex, tmp );
			nextSourceEdgeIndex = tmp.getNextSourceEdgeIndex();
			++insertIndex;
		}
		edge.setNextSourceEdgeIndex( nextSourceEdgeIndex );
		if ( insertIndex == 0 )
			source.setFirstOutEdgeIndex( edge.getInternalPoolIndex() );
		else
			tmp.setNextSourceEdgeIndex( edge.getInternalPoolIndex() );



		int nextTargetEdgeIndex = target.getFirstInEdgeIndex();
		insertIndex = 0;
		while( nextTargetEdgeIndex >= 0 && insertIndex < targetInInsertAt )
		{
			getByInternalPoolIndex( nextTargetEdgeIndex, tmp );
			nextTargetEdgeIndex = tmp.getNextTargetEdgeIndex();
			++insertIndex;
		}
		edge.setNextTargetEdgeIndex( nextTargetEdgeIndex );
		if ( insertIndex == 0 )
			target.setFirstInEdgeIndex( edge.getInternalPoolIndex() );
		else
			tmp.setNextTargetEdgeIndex( edge.getInternalPoolIndex() );

		releaseRef( tmp );
		return edge;
	}


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

	public void deleteAllLinkedEdges( final AbstractVertex< ?, ?, ? > vertex )
	{
		final V tmpVertex = vertexPool.createRef();
		final E edge = createRef();
		final E tmpEdge = createRef();

		// release all outgoing edges
		int index = vertex.getFirstOutEdgeIndex();
		vertex.setFirstOutEdgeIndex( -1 );
		while ( index >= 0 )
		{
			getByInternalPoolIndex( index, edge );
			unlinkFromTarget( edge, tmpEdge, tmpVertex );
			index = edge.getNextSourceEdgeIndex();
			deleteByInternalPoolIndex( edge.getInternalPoolIndex() );
		}

		// release all incoming edges
		index = vertex.getFirstInEdgeIndex();
		vertex.setFirstInEdgeIndex( -1 );
		while ( index >= 0 )
		{
			getByInternalPoolIndex( index, edge );
			unlinkFromSource( edge, tmpEdge, tmpVertex );
			index = edge.getNextTargetEdgeIndex();
			deleteByInternalPoolIndex( edge.getInternalPoolIndex() );
		}

		vertexPool.releaseRef( tmpVertex );
		releaseRef( edge );
		releaseRef( tmpEdge );
	}

	public void delete( final E edge )
	{
		final V tmpVertex = vertexPool.createRef();
		final E tmp = createRef();

		unlinkFromSource( edge, tmp, tmpVertex );
		unlinkFromTarget( edge, tmp, tmpVertex );
		deleteByInternalPoolIndex( edge.getInternalPoolIndex() );

		vertexPool.releaseRef( tmpVertex );
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
			// this edge is the first in the sources list of outgoing edges
			tmpVertex.setFirstOutEdgeIndex( edge.getNextSourceEdgeIndex() );
		}
		else
		{
			// find this edge in the sources list of outgoing edges and remove it
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
			// this edge is the first in the targets list of incoming edges
			tmpVertex.setFirstInEdgeIndex( edge.getNextTargetEdgeIndex() );
		}
		else
		{
			// find this edge in the targets list of incoming edges and remove it
			getByInternalPoolIndex( targetInIndex, tmpEdge );
			int nextTargetEdgeIndex = tmpEdge.getNextTargetEdgeIndex();
			while( nextTargetEdgeIndex != edge.getInternalPoolIndex() )
			{
				getByInternalPoolIndex( nextTargetEdgeIndex, tmpEdge );
				nextTargetEdgeIndex = tmpEdge.getNextTargetEdgeIndex();
			}
			tmpEdge.setNextTargetEdgeIndex( edge.getNextTargetEdgeIndex() );
		}
	}
}
