package pietzsch.spots;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import pietzsch.mappedelementpool.MappedElement;
import pietzsch.mappedelementpool.Pool;
import pietzsch.mappedelementpool.Pool.PoolIterator;

public class AbstractEdgePool< E extends AbstractEdge< T, ? >, T extends MappedElement, S extends AbstractSpot< ?, ? > > implements Iterable< E >
{
	final Pool< T > memPool;

	private final AbstractEdge.Factory< E, T, S > edgeFactory;

	final AbstractSpotPool< S, ?, ? > spotPool;

	public AbstractEdgePool(
			final int initialCapacity,
			final AbstractEdge.Factory< E, T, S > edgeFactory,
			final Pool.Factory< T > poolFactory,
			final AbstractSpotPool< S, ?, ? > spotPool )
	{
		this.edgeFactory = edgeFactory;
		this.spotPool = spotPool;
		this.memPool = poolFactory.createPool( initialCapacity, edgeFactory.getEdgeSizeInBytes() );
	}

	public void clear()
	{
		memPool.clear();
	}

	public E createEmptyEdgeRef()
	{
		return edgeFactory.createEmptyEdgeRef( this );
	}

	public E addEdge( final AbstractSpot< ?, ? > source, final AbstractSpot< ?, ? > target )
	{
		return addEdge( source, target, createEmptyEdgeRef() );
	}

	// garbage-free version
	public E addEdge( final AbstractSpot< ?, ? > source, final AbstractSpot< ?, ? > target, final E edge )
	{
		if ( getEdge( source, target, edge ) != null )
			return null;

		create( edge );
		edge.setSourceSpotInternalPoolIndex( source.getInternalPoolIndex() );
		edge.setTargetSpotInternalPoolIndex( target.getInternalPoolIndex() );

		final E tmp = getTmpEdgeRef();

		final long sourceOutIndex = source.getFirstOutEdgeIndex();
		if ( sourceOutIndex < 0 )
		{
			// source has no outgoing edge yet. Set this one as the first.
			source.setFirstOutEdgeIndex( edge.getInternalPoolIndex() );
		}
		else
		{
			// source has outgoing edges. Append this one to the end of the linked list.
			getByInternalPoolIndex( sourceOutIndex, tmp );
			long nextSourceEdgeIndex = tmp.getNextSourceEdgeIndex();
			while( nextSourceEdgeIndex >= 0 )
			{
				getByInternalPoolIndex( nextSourceEdgeIndex, tmp );
				nextSourceEdgeIndex = tmp.getNextSourceEdgeIndex();
			}
			tmp.setNextSourceEdgeIndex( edge.getInternalPoolIndex() );
		}

		final long targetInIndex = target.getFirstInEdgeIndex();
		if ( targetInIndex < 0 )
		{
			// target has no incoming edge yet. Set this one as the first.
			target.setFirstInEdgeIndex( edge.getInternalPoolIndex() );
		}
		else
		{
			// target has incoming edges. Append this one to the end of the linked list.
			getByInternalPoolIndex( targetInIndex, tmp );
			long nextTargetEdgeIndex = tmp.getNextTargetEdgeIndex();
			while( nextTargetEdgeIndex >= 0 )
			{
				getByInternalPoolIndex( nextTargetEdgeIndex, tmp );
				nextTargetEdgeIndex = tmp.getNextTargetEdgeIndex();
			}
			tmp.setNextTargetEdgeIndex( edge.getInternalPoolIndex() );
		}

		releaseTmpEdgeRef( tmp );
		return edge;
	}

	public E getEdge( final AbstractSpot< ?, ? > source, final AbstractSpot< ?, ? > target )
	{
		return getEdge( source, target, createEmptyEdgeRef() );
	}

	// garbage-free version
	public E getEdge( final AbstractSpot< ?, ? > source, final AbstractSpot< ?, ? > target, final E edge )
	{
		long nextSourceEdgeIndex = source.getFirstOutEdgeIndex();
		if ( nextSourceEdgeIndex < 0 )
			return null;
		getByInternalPoolIndex( nextSourceEdgeIndex, edge );
		do
		{
			if ( edge.getTargetSpotInternalPoolIndex() == target.getInternalPoolIndex() )
				return edge;
			getByInternalPoolIndex( nextSourceEdgeIndex, edge );
			nextSourceEdgeIndex = edge.getNextSourceEdgeIndex();
		}
		while ( nextSourceEdgeIndex >= 0 );
		return null;
	}

	public void releaseAllLinkedEdges( final S spot )
	{
		final S tmpSpot = spotPool.getTmpSpotRef();
		final E edge = getTmpEdgeRef();
		final E tmpEdge = getTmpEdgeRef();

		// release all outgoing edges
		long index = spot.getFirstOutEdgeIndex();
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

		spotPool.releaseTmpSpotRef( tmpSpot );
		releaseTmpEdgeRef( edge );
		releaseTmpEdgeRef( tmpEdge );
	}

	public void release( final E edge )
	{
		final S tmpSpot = spotPool.getTmpSpotRef();
		final E tmp = getTmpEdgeRef();

		unlinkFromSource( edge, tmp, tmpSpot );
		unlinkFromTarget( edge, tmp, tmpSpot );
		releaseByInternalPoolIndex( edge.getInternalPoolIndex() );

		spotPool.releaseTmpSpotRef( tmpSpot );
		releaseTmpEdgeRef( tmp );
	}

	@Override
	public Iterator< E > iterator()
	{
		final PoolIterator< T > pi = memPool.iterator();
		final E edge = createEmptyEdgeRef();
		return new Iterator< E >()
		{
			@Override
			public boolean hasNext()
			{
				return pi.hasNext();
			}

			@Override
			public E next()
			{
				final long index = pi.next();
				edge.updateAccess( memPool, index );
				return edge;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	/*
	 *
	 * Internal stuff.
	 * If it should be necessary for performance reasons, these can be made protected or public
	 *
	 */

	private void create( final E edge )
	{
		final long index = memPool.create();
		edge.updateAccess( memPool, index );
		edge.init();
	}

	void getByInternalPoolIndex( final long index, final E edge )
	{
		edge.updateAccess( memPool, index );
	}

	private void releaseByInternalPoolIndex( final long index )
	{
		memPool.free( index );
	}

	private void unlinkFromSource( final E edge, final E tmpEdge, final S tmpSpot )
	{
		spotPool.getByInternalPoolIndex( edge.getSourceSpotInternalPoolIndex(), tmpSpot );
		final long sourceOutIndex = tmpSpot.getFirstOutEdgeIndex();
		if ( sourceOutIndex == edge.getInternalPoolIndex() )
		{
			// this edge is the first in the source's list of outgoing edges
			tmpSpot.setFirstOutEdgeIndex( edge.getNextSourceEdgeIndex() );
		}
		else
		{
			// find this edge in the source's list of outgoing edges and remove it
			getByInternalPoolIndex( sourceOutIndex, tmpEdge );
			long nextSourceEdgeIndex = tmpEdge.getNextSourceEdgeIndex();
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
		spotPool.getByInternalPoolIndex( edge.getTargetSpotInternalPoolIndex(), tmpSpot );
		final long targetInIndex = tmpSpot.getFirstInEdgeIndex();
		if ( targetInIndex == edge.getInternalPoolIndex() )
		{
			// this edge is the first in the target list of incoming edges
			tmpSpot.setFirstInEdgeIndex( edge.getNextTargetEdgeIndex() );
		}
		else
		{
			// find this edge in the target's list of incoming edges and remove it
			getByInternalPoolIndex( targetInIndex, tmpEdge );
			long nextTargetEdgeIndex = tmpEdge.getNextTargetEdgeIndex();
			while( nextTargetEdgeIndex >= 0 )
			{
				getByInternalPoolIndex( nextTargetEdgeIndex, tmpEdge );
				nextTargetEdgeIndex = tmpEdge.getNextTargetEdgeIndex();
			}
			tmpEdge.setNextTargetEdgeIndex( edge.getNextTargetEdgeIndex() );
		}
	}

	private final ConcurrentLinkedQueue< E > tmpEdgeRefs = new ConcurrentLinkedQueue< E >();

	public E getTmpEdgeRef()
	{
		final E edge = tmpEdgeRefs.poll();
		return edge == null ? createEmptyEdgeRef() : edge;
	}

	public void releaseTmpEdgeRef( final E edge )
	{
		tmpEdgeRefs.add( edge );
	}
}
