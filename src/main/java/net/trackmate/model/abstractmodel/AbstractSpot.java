package net.trackmate.model.abstractmodel;

import static net.trackmate.util.mempool.ByteUtils.INT_SIZE;
import static net.trackmate.util.mempool.ByteUtils.LONG_SIZE;
import net.trackmate.util.mempool.MappedElement;
import net.trackmate.util.mempool.Pool;

/**
 * TODO: javadoc
 *
 * @param <T>
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class AbstractSpot< T extends MappedElement, E extends AbstractEdge< ?, ? > >
{
	protected static final int ID_OFFSET = 0;
	protected static final int FIRST_IN_EDGE_INDEX_OFFSET = ID_OFFSET + INT_SIZE;
	protected static final int FIRST_OUT_EDGE_INDEX_OFFSET = FIRST_IN_EDGE_INDEX_OFFSET + LONG_SIZE;
	protected static final int SIZE_IN_BYTES = FIRST_OUT_EDGE_INDEX_OFFSET + LONG_SIZE;

	protected final T access;

	private long index;

	protected AbstractSpot( final AbstractSpotPool< ?, T, ? > pool )
	{
		this.access = pool.memPool.createAccess();
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

	protected int getId()
	{
		return access.getInt( ID_OFFSET );
	}

	protected void setId( final int id )
	{
		access.putInt( id, ID_OFFSET );
	}

	protected long getFirstInEdgeIndex()
	{
		return access.getLong( FIRST_IN_EDGE_INDEX_OFFSET );
	}

	protected void setFirstInEdgeIndex( final long index )
	{
		access.putLong( index, FIRST_IN_EDGE_INDEX_OFFSET );
	}

	protected long getFirstOutEdgeIndex()
	{
		return access.getLong( FIRST_OUT_EDGE_INDEX_OFFSET );
	}

	protected void setFirstOutEdgeIndex( final long index )
	{
		access.putLong( index, FIRST_OUT_EDGE_INDEX_OFFSET );
	}

	protected void init()
	{
		setFirstInEdgeIndex( -1 );
		setFirstOutEdgeIndex( -1 );
	}

	private IncomingSpotEdges< E > incomingEdges;

	private OutgoingSpotEdges< E > outgoingEdges;

	private AllSpotEdges< E > edges;

	protected IncomingSpotEdges< E > incomingEdges()
	{
		return incomingEdges;
	}

	protected OutgoingSpotEdges< E > outgoingEdges()
	{
		return outgoingEdges;
	}

	protected AllSpotEdges< E > edges()
	{
		return edges;
	}

	void linkEdgePool( final AbstractEdgePool< E, ?, ? > edgePool )
	{
		incomingEdges = new IncomingSpotEdges< E >( this, edgePool );
		outgoingEdges = new OutgoingSpotEdges< E >( this, edgePool );
		edges = new AllSpotEdges< E >( this, edgePool );
	}

	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof AbstractSpot< ?, ? > &&
				access.equals( ( ( AbstractSpot< ?, ? > ) obj ).access );
	}

	@Override
	public int hashCode()
	{
		return access.hashCode();
	}

	public static interface Factory< S extends AbstractSpot< T, ? >, T extends MappedElement >
	{
		public int getSpotSizeInBytes();

		public S createEmptySpotRef( final AbstractSpotPool< S, T, ? > pool );
	}
}
