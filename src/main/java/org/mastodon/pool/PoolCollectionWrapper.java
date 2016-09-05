package org.mastodon.pool;

import java.util.Collection;
import java.util.Iterator;

import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.util.AbstractRefPoolCollectionWrapper;

/**
 * Wrap a {@link Pool} as a {@link RefCollection}. This allows for querying the
 * underlying pool using basic {@link Collection} methods. Only the
 * {@code isEmpty(),} {@code size(),} {@code iterator()} methods are
 * implemented. The remaining {@link Collection} methods are unsuited for pools
 * and throw {@link UnsupportedOperationException}.
 * <p>
 * Moreover, pools wrapped like this can be passed to {@link RefCollections}
 * {@code .create...()} methods for creating specialized {@link RefCollection}s
 * of objects in the pool.
 *
 * @param <O>
 *            the type of the pool object used in the wrapped {@link Pool}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class PoolCollectionWrapper< O extends PoolObject< O, ? > > extends AbstractRefPoolCollectionWrapper< O, Pool< O, ? > >
{
	PoolCollectionWrapper( final Pool< O, ? > pool )
	{
		super( pool );
	}

	@Override
	public int size()
	{
		return pool.size();
	}

	@Override
	public Iterator< O > iterator()
	{
		return pool.iterator();
	}
}
