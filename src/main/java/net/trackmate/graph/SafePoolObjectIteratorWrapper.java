package net.trackmate.graph;

import java.util.Iterator;

/**
 * Wraps an iterator of a {@link PoolObject} collection such that {@code next()}
 * always returns a new ref object (that stays valid over successive
 * {@code next()} calls).
 *
 * @param <O>
 *            type of {@link PoolObject} iterated over.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class SafePoolObjectIteratorWrapper< O extends PoolObject< O, ? > > implements Iterator< O >
{
	private final Iterator< O > iterator;

	public SafePoolObjectIteratorWrapper( final Iterator< O > iterator )
	{
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext()
	{
		return iterator.hasNext();
	}

	@Override
	public O next()
	{
		final O next = iterator.next();
		return next.creatingPool.createRef().refTo( next );
	}

	@Override
	public void remove()
	{
		iterator.remove();
	}
}
