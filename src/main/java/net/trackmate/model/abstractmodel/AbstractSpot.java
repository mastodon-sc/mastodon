package net.trackmate.model.abstractmodel;

import static net.trackmate.util.mempool.ByteUtils.INDEX_SIZE;
import static net.trackmate.util.mempool.ByteUtils.INT_SIZE;
import net.trackmate.util.mempool.MappedElement;

/**
 * TODO: javadoc
 *
 * @param <T>
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class AbstractSpot< T extends MappedElement, E extends AbstractEdge< ?, ? > > extends PoolObject< T >
{
	protected static final int ID_OFFSET = 0;
	protected static final int FIRST_IN_EDGE_INDEX_OFFSET = ID_OFFSET + INT_SIZE;
	protected static final int FIRST_OUT_EDGE_INDEX_OFFSET = FIRST_IN_EDGE_INDEX_OFFSET + INDEX_SIZE;
	protected static final int SIZE_IN_BYTES = FIRST_OUT_EDGE_INDEX_OFFSET + INDEX_SIZE;

	protected AbstractSpot( final AbstractSpotPool< ?, T, ? > pool )
	{
		super( pool.getMemPool() );
	}

	protected int getId()
	{
		return access.getInt( ID_OFFSET );
	}

	protected void setId( final int id )
	{
		access.putInt( id, ID_OFFSET );
	}

	protected int getFirstInEdgeIndex()
	{
		return access.getIndex( FIRST_IN_EDGE_INDEX_OFFSET );
	}

	protected void setFirstInEdgeIndex( final int index )
	{
		access.putIndex( index, FIRST_IN_EDGE_INDEX_OFFSET );
	}

	protected int getFirstOutEdgeIndex()
	{
		return access.getIndex( FIRST_OUT_EDGE_INDEX_OFFSET );
	}

	protected void setFirstOutEdgeIndex( final int index )
	{
		access.putIndex( index, FIRST_OUT_EDGE_INDEX_OFFSET );
	}

	protected void setToUninitializedState()
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

		public S createEmptySpotRef();
	}
}
