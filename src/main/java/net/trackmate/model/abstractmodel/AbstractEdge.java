package net.trackmate.model.abstractmodel;

import static net.trackmate.util.mempool.ByteUtils.LONG_SIZE;
import net.trackmate.util.mempool.MappedElement;
import net.trackmate.util.mempool.Pool;

public class AbstractEdge< T extends MappedElement, S extends AbstractSpot< ?, ? > >
{
	protected static final int SOURCE_INDEX_OFFSET = 0;
	protected static final int TARGET_INDEX_OFFSET = SOURCE_INDEX_OFFSET + LONG_SIZE;
	protected static final int NEXT_SOURCE_EDGE_INDEX_OFFSET = TARGET_INDEX_OFFSET + LONG_SIZE;
	protected static final int NEXT_TARGET_EDGE_INDEX_OFFSET = NEXT_SOURCE_EDGE_INDEX_OFFSET + LONG_SIZE;
	protected static final int SIZE_IN_BYTES = NEXT_TARGET_EDGE_INDEX_OFFSET + LONG_SIZE;

	protected final T access;

	private long index;

	protected final AbstractSpotPool< S, ?, ? > spotPool;

	protected AbstractEdge( final AbstractEdgePool< ?, T, S > pool )
	{
		this.access = pool.memPool.createAccess();
		this.spotPool = pool.spotPool;
	}

	public long getInternalPoolIndex()
	{
		return index;
	}

	void updateAccess( final Pool< T > pool, final long index )
	{
		this.index = index;
		pool.updateAccess( access, index );
	}

	protected long getSourceSpotInternalPoolIndex()
	{
		return access.getLong( SOURCE_INDEX_OFFSET );
	}

	protected void setSourceSpotInternalPoolIndex( final long index )
	{
		access.putLong( index, SOURCE_INDEX_OFFSET );
	}

	protected long getTargetSpotInternalPoolIndex()
	{
		return access.getLong( TARGET_INDEX_OFFSET );
	}

	protected void setTargetSpotInternalPoolIndex( final long index )
	{
		access.putLong( index, TARGET_INDEX_OFFSET );
	}

	protected long getNextSourceEdgeIndex()
	{
		return access.getLong( NEXT_SOURCE_EDGE_INDEX_OFFSET );
	}

	protected void setNextSourceEdgeIndex( final long index )
	{
		access.putLong( index, NEXT_SOURCE_EDGE_INDEX_OFFSET );
	}

	protected long getNextTargetEdgeIndex()
	{
		return access.getLong( NEXT_TARGET_EDGE_INDEX_OFFSET );
	}

	protected void setNextTargetEdgeIndex( final long index )
	{
		access.putLong( index, NEXT_TARGET_EDGE_INDEX_OFFSET );
	}

	protected void init()
	{
		setNextSourceEdgeIndex( -1 );
		setNextTargetEdgeIndex( -1 );
	}

	protected S getSourceSpot( final S spot )
	{
		spotPool.getByInternalPoolIndex( getSourceSpotInternalPoolIndex(), spot );
		return spot;
	}

	protected S getTargetSpot( final S spot )
	{
		spotPool.getByInternalPoolIndex( getTargetSpotInternalPoolIndex(), spot );
		return spot;
	}


	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof AbstractEdge< ?, ? > &&
				access.equals( ( ( AbstractEdge< ?, ? > ) obj ).access );
	}

	@Override
	public int hashCode()
	{
		return access.hashCode();
	}

	public static interface Factory< E extends AbstractEdge< T, ? >, T extends MappedElement, S extends AbstractSpot< ?, ? > >
	{
		public int getEdgeSizeInBytes();

		public E createEmptyEdgeRef( final AbstractEdgePool< ?, T, S > pool );
	}

}
