package pietzsch.spots;

import static pietzsch.mappedelementpool.ByteUtils.LONG_SIZE;
import pietzsch.mappedelementpool.MappedElement;
import pietzsch.mappedelementpool.Pool;

public class AbstractEdge< T extends MappedElement >
{
	public static final int SOURCE_INDEX_OFFSET = 0;
	public static final int TARGET_INDEX_OFFSET = SOURCE_INDEX_OFFSET + LONG_SIZE;
	public static final int NEXT_SOURCE_EDGE_INDEX_OFFSET = TARGET_INDEX_OFFSET + LONG_SIZE;
	public static final int NEXT_TARGET_EDGE_INDEX_OFFSET = NEXT_SOURCE_EDGE_INDEX_OFFSET + LONG_SIZE;
	public static final int SIZE_IN_BYTES = NEXT_TARGET_EDGE_INDEX_OFFSET + LONG_SIZE;

	protected final T access;

	private long index;

	protected AbstractEdge( final T access )
	{
		this.access = access;
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

	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof AbstractEdge< ? > &&
				access.equals( ( ( AbstractEdge< ? > ) obj ).access );
	}

	@Override
	public int hashCode()
	{
		return access.hashCode();
	}

	public static interface Factory< E extends AbstractEdge< T >, T extends MappedElement >
	{
		public int getEdgeSizeInBytes();

		public E createEmptyEdgeRef( final Pool< T > pool );
	}

}
