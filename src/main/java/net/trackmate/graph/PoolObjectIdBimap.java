package net.trackmate.graph;


/**
 * Bidirectional mapping between {@link PoolObject}s and their internal pool
 * indices.
 *
 * <p>
 * TODO: in which package should this be?
 *
 * @param <O>
 *            the {@link PoolObject} type.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class PoolObjectIdBimap< O extends PoolObject< O, ? > > implements IdBimap< O >
{
	private final Pool< O, ? > pool;

	public PoolObjectIdBimap( final Pool< O, ? > pool )
	{
		this.pool = pool;}

	@Override
	public int getId( final O o )
	{
		return o.getInternalPoolIndex();
	}

	@Override
	public O getObject( final int id, final O ref )
	{
		pool.getByInternalPoolIndex( id, ref );
		return ref;
	}
}