package net.trackmate.graph.collection;

import java.util.Iterator;

import net.trackmate.graph.zzrefcollections.Ref;

/**
 * Wraps an iterator of a {@link Ref} collection such that {@code next()}
 * always returns a new ref object (that stays valid over successive
 * {@code next()} calls).
 *
 * @param <O>
 *            type of {@link Ref} iterated over.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
// TODO: move to collection package
public class SafeRefIteratorWrapper< O extends Ref< O > > implements Iterator< O >
{
	private final Iterator< O > iterator;

	private final RefCollection<O> collection;

	public SafeRefIteratorWrapper( final Iterator< O > iterator, final RefCollection< O > collection )
	{
		this.iterator = iterator;
		this.collection = collection;
	}

	@Override
	public boolean hasNext()
	{
		return iterator.hasNext();
	}

	@Override
	public O next()
	{
		return collection.createRef().refTo( iterator.next() );
	}

	@Override
	public void remove()
	{
		iterator.remove();
	}
}
