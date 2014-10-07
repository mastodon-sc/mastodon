package net.trackmate.graph;

import static net.trackmate.graph.mempool.ByteUtils.INT_SIZE;
import net.trackmate.graph.mempool.MappedElement;

/**
 * A {@link AbstractVertex} that has a unique {@code int} ID.
 *
 * @param <V>
 * @param <E>
 * @param <T>
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class AbstractIdVertex< V extends AbstractIdVertex< V, E, T >, E extends AbstractEdge< E, ?, ? >, T extends MappedElement >
		extends AbstractVertex< V, E, T >
{
	protected static final int ID_OFFSET = AbstractVertex.SIZE_IN_BYTES;
	protected static final int SIZE_IN_BYTES = ID_OFFSET + INT_SIZE;

	protected AbstractIdVertex( final AbstractVertexPool< ?, ?, T > pool )
	{
		super( pool );
	}

	protected int getId()
	{
		return access.getInt( ID_OFFSET );
	}

	protected void setId( final int id )
	{
		access.putInt( id, ID_OFFSET );
	}
}
