package net.trackmate.collection.ref;

import net.trackmate.Ref;
import net.trackmate.RefPool;
import net.trackmate.collection.IdBimap;
import net.trackmate.pool.PoolObject;

/**
 * Bidirectional mapping between {@link PoolObject}s and their internal pool
 * indices.
 *
 * @param <O>
 *            the {@link PoolObject} type.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
// TODO: This is not necessary anymore, because Pool implements IdBimap directly. Don't use it. Possibly remove it.
public class RefIdBimap< O extends Ref< O > > implements IdBimap< O >
{
	private final RefPool< O > pool;

	public RefIdBimap( final RefPool< O > pool )
	{
		this.pool = pool;
	}

	@Override
	public int getId( final O o )
	{
		return o.getInternalPoolIndex();
	}

	@Override
	public O getObject( final int id, final O ref )
	{
		return pool.getObject( id, ref );
	}

	@Override
	public O createRef()
	{
		return pool.createRef();
	}

	@Override
	public void releaseRef( final O ref )
	{
		pool.releaseRef( ref );
	}

	@Override
	public Class< O > getRefClass()
	{
		return pool.getRefClass();
	}
}
