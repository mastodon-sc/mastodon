/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.adapter;

import java.util.Collection;
import java.util.Iterator;

/**
 * Adapts a {@code Collection<O>} as a {@code Collection<WO>}. The mapping
 * between source elements {@code O} and wrapped elements {@code WO} is
 * established by a {@code RefBimap<O, WO>}.
 *
 * For the reverse wrapper taking a {@code RefBimap<WO, O>}, see
 * {@link CollectionAdapterReverse}.
 *
 * @param <O>
 *            element type of source collection being wrapped.
 * @param <WO>
 *            element type of this wrapper collection.
 *
 * @author Tobias Pietzsch
 */
public class CollectionAdapter< O, WO >
		implements Collection< WO >
{
	private final Collection< O > collection;

	private final RefBimap< O, WO > map;

	public CollectionAdapter(
			final Collection< O > collection,
			final RefBimap< O, WO > map )
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

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean contains( final Object o )
	{
		return collection.contains( map.getLeft( ( WO ) o ) );
	}

	@Override
	public Iterator< WO > iterator()
	{
		final Iterator< O > iter = collection.iterator();
		final WO ref = map.reusableRightRef();
		return new Iterator< WO >()
		{
			@Override
			public boolean hasNext()
			{
				return iter.hasNext();
			}

			@Override
			public WO next()
			{
				return map.getRight( iter.next(), ref );
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
		return collection.add( map.getLeft( e ) );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean remove( final Object o )
	{
		return collection.remove( map.getLeft( ( WO ) o ) );
	}

	@Override
	public boolean containsAll( final Collection< ? > c )
	{
		// TODO
		throw new UnsupportedOperationException( "not implemented (yet)" );
	}

	@Override
	public boolean addAll( final Collection< ? extends WO > c )
	{
		// TODO
		throw new UnsupportedOperationException( "not implemented (yet)" );
	}

	@Override
	public boolean removeAll( final Collection< ? > c )
	{
		// TODO
		throw new UnsupportedOperationException( "not implemented (yet)" );
	}

	@Override
	public boolean retainAll( final Collection< ? > c )
	{
		// TODO
		throw new UnsupportedOperationException( "not implemented (yet)" );
	}

	@Override
	public void clear()
	{
		collection.clear();
	}
}
