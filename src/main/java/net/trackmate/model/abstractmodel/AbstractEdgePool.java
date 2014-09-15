package net.trackmate.model.abstractmodel;

import net.trackmate.util.mempool.MappedElement;
import net.trackmate.util.mempool.MemPool;

public class AbstractEdgePool< E extends AbstractEdge< T, ? >, T extends MappedElement, S extends AbstractVertex< ?, ? > > extends Pool< E, T > implements Iterable< E >
{
	final AbstractVertexPool< S, ?, ? > vertexPool;

	public AbstractEdgePool(
			final int initialCapacity,
			final PoolObject.Factory< E > edgeFactory,
			final MemPool.Factory< T > poolFactory,
			final AbstractVertexPool< S, ?, ? > spotPool )
	{
		super( initialCapacity, edgeFactory, poolFactory );
		this.vertexPool = spotPool;
	}

	public E addEdge( final AbstractVertex< ?, ? > source, final AbstractVertex< ?, ? > target )
	{
		return addEdge( source, target, createEmptyRef() );
	}

	// garbage-free version
	public E addEdge( final AbstractVertex< ?, ? > source, final AbstractVertex< ?, ? > target, final E edge )
	{
		if ( getEdge( source, target, edge ) != null )
			return null;

		create( edge );
		edge.setSourceVertexInternalPoolIndex( source.getInternalPoolIndex() );
		edge.setTargetVertexInternalPoolIndex( target.getInternalPoolIndex() );

		final E tmp = getTmpRef();

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

		releaseTmpRef( tmp );
		return edge;
	}

	public E getEdge( final AbstractVertex< ?, ? > source, final AbstractVertex< ?, ? > target )
	{
		return getEdge( source, target, createEmptyRef() );
	}

	// garbage-free version
	public E getEdge( final AbstractVertex< ?, ? > source, final AbstractVertex< ?, ? > target, final E edge )
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

	public void releaseAllLinkedEdges( final S spot )
	{
		final S tmpSpot = vertexPool.getTmpRef();
		final E edge = getTmpRef();
		final E tmpEdge = getTmpRef();

		// release all outgoing edges
		int index = spot.getFirstOutEdgeIndex();
		spot.setFirstOutEdgeIndex( -1 );
		while ( index >= 0 )
		{
			getByInternalPoolIndex( index, edge );
			unlinkFromTarget( edge, tmpEdge, tmpSpot );
			index = edge.getNextSourceEdgeIndex();
			releaseByInternalPoolIndex( edge.getInternalPoolIndex() );
		}

		// release all incoming edges
		index = spot.getFirstInEdgeIndex();
		spot.setFirstInEdgeIndex( -1 );
		while ( index >= 0 )
		{
			getByInternalPoolIndex( index, edge );
			unlinkFromSource( edge, tmpEdge, tmpSpot );
			index = edge.getNextTargetEdgeIndex();
			releaseByInternalPoolIndex( edge.getInternalPoolIndex() );
		}

		vertexPool.releaseTmpRef( tmpSpot );
		releaseTmpRef( edge );
		releaseTmpRef( tmpEdge );
	}

	public void release( final E edge )
	{
		final S tmpSpot = vertexPool.getTmpRef();
		final E tmp = getTmpRef();

		unlinkFromSource( edge, tmp, tmpSpot );
		unlinkFromTarget( edge, tmp, tmpSpot );
		releaseByInternalPoolIndex( edge.getInternalPoolIndex() );

		vertexPool.releaseTmpRef( tmpSpot );
		releaseTmpRef( tmp );
	}

	/*
	 *
	 * Internal stuff.
	 * If it should be necessary for performance reasons, these can be made protected or public
	 *
	 */

	private void unlinkFromSource( final E edge, final E tmpEdge, final S tmpSpot )
	{
		vertexPool.getByInternalPoolIndex( edge.getSourceVertexInternalPoolIndex(), tmpSpot );
		final int sourceOutIndex = tmpSpot.getFirstOutEdgeIndex();
		if ( sourceOutIndex == edge.getInternalPoolIndex() )
		{
			// this edge is the first in the source's list of outgoing edges
			tmpSpot.setFirstOutEdgeIndex( edge.getNextSourceEdgeIndex() );
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

	private void unlinkFromTarget( final E edge, final E tmpEdge, final S tmpSpot )
	{
		vertexPool.getByInternalPoolIndex( edge.getTargetVertexInternalPoolIndex(), tmpSpot );
		final int targetInIndex = tmpSpot.getFirstInEdgeIndex();
		if ( targetInIndex == edge.getInternalPoolIndex() )
		{
			// this edge is the first in the target list of incoming edges
			tmpSpot.setFirstInEdgeIndex( edge.getNextTargetEdgeIndex() );
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
