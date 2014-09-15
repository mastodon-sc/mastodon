package net.trackmate.model.abstractmodel;

import static net.trackmate.util.mempool.ByteUtils.INT_SIZE;
import net.trackmate.util.mempool.MappedElement;

/**
 * A {@link AbstractVertex} that has a unique {@code int} ID.
 *
 * @param <T>
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class AbstractIdVertex< T extends MappedElement, E extends AbstractEdge< ?, ? > > extends AbstractVertex< T, E >
{
	protected static final int ID_OFFSET = AbstractVertex.SIZE_IN_BYTES;
	protected static final int SIZE_IN_BYTES = FIRST_OUT_EDGE_INDEX_OFFSET + INT_SIZE;

	protected AbstractIdVertex( final AbstractVertexPool< ?, T, ? > pool )
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
