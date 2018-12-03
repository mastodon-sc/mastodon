package org.mastodon.adapter;

import java.util.Collection;
import java.util.Iterator;

/**
 * Adapts a {@code Collection<O>} as a {@code Collection<WO>}. The mapping
 * between source elements {@code O} and wrapped elements {@code WO} is
 * established by a {@code RefBimap<WO, O>}.
 *
 * For the reverse wrapper taking a {@code RefBimap<O, WO>}, see
 * {@link CollectionAdapter}.
 *
 * @param <O>
 *            element type of source collection being wrapped.
 * @param <WO>
 *            element type of this wrapper collection.
 *
 * @author Tobias Pietzsch
 */
public class CollectionAdapterReverse< O, WO >
		implements Collection< WO >
{
	private final Collection< O > collection;

	private final RefBimap< WO, O > map;

	public CollectionAdapterReverse(
			final Collection< O > collection,
			final RefBimap< WO, O > map )
	{
		this.collection = collection;
		this.map = map;
	}

	@Override
	public int size()
	{
		return collection.size();
	}

	@Override
	public boolean isEmpty()
	{
		return collection.isEmpty();
	}

	@Override
	public boolean contains( final Object o )
	{
		final O ref = map.reusableRightRef();
		@SuppressWarnings( "unchecked" )
		final boolean result = collection.contains( map.getRight( ( WO ) o, ref ) );
		map.releaseRef( ref );
		return result;
	}

	@Override
	public Iterator< WO > iterator()
	{
		final Iterator< O > iter = collection.iterator();
		return new Iterator< WO >() {
			@Override
			public boolean hasNext()
			{
				return iter.hasNext();
			}

			@Override
			public WO next()
			{
				return map.getLeft( iter.next() );
			}
		};
	}

	@Override
	public Object[] toArray()
	{
		throw new UnsupportedOperationException( "not implemented (yet)" );
	}

	@Override
	public < T > T[] toArray( final T[] a )
	{
		throw new UnsupportedOperationException( "not implemented (yet)" );
	}

	@Override
	public boolean add( final WO e )
	{
		final O ref = map.reusableRightRef();
		final boolean result = collection.add( map.getRight( e, ref ) );
		map.releaseRef( ref );
		return result;
	}

	@Override
	public boolean remove( final Object o )
	{
		final O ref = map.reusableRightRef();
		@SuppressWarnings( "unchecked" )
		final boolean result = collection.remove( map.getRight( ( WO ) o, ref ) );
		map.releaseRef( ref );
		return result;
	}

	@Override
	public boolean containsAll( final Collection< ? > c )
	{
		throw new UnsupportedOperationException( "not implemented (yet)" ); // TODO
	}

	@Override
	public boolean addAll( final Collection< ? extends WO > c )
	{
		throw new UnsupportedOperationException( "not implemented (yet)" ); // TODO
	}

	@Override
	public boolean removeAll( final Collection< ? > c )
	{
		throw new UnsupportedOperationException( "not implemented (yet)" ); // TODO
	}

	@Override
	public boolean retainAll( final Collection< ? > c )
	{
		throw new UnsupportedOperationException( "not implemented (yet)" ); // TODO
	}

	@Override
	public void clear()
	{
		collection.clear();
	}
}
