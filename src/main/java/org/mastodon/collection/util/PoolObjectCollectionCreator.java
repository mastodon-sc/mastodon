package org.mastodon.collection.util;

import java.util.Collection;
import java.util.Iterator;

import org.mastodon.collection.RefCollection;
import org.mastodon.pool.Pool;
import org.mastodon.pool.PoolObject;

/**
 * Wrapper for {@link Pool}s offering the ability to create collections based on
 * the wrapped pool.
 * <p>
 * This class wraps a {@link Pool} and offers methods to generate various
 * collections based on the wrapped pool. It offers a bridge between the
 * {@link Pool} framework and the Java {@link Collection} framework.
 * <p>
 * This class implements the {@link RefCollection} interface itself, and
 * therefore allows for questing the underlying pool using the
 * {@link Collection} methods. Only the {@code isEmpty(),} {@code size(),}
 * {@code iterator(),} {@code createRef()}, and {@code releaseRef()} methods are
 * guaranteed to be implemented. The remaining {@link Collection} methods are
 * unsuited for {@link Pool} and throw an {@link UnsupportedOperationException}.
 *
 * @param <O>
 *            the type of the pool object used in the wrapped {@link Pool}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class PoolObjectCollectionCreator< O extends PoolObject< O, ? > > extends AbstractRefPoolCollectionCreator< O, Pool< O, ? > >
{
	public PoolObjectCollectionCreator( final Pool< O, ? > pool )
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
