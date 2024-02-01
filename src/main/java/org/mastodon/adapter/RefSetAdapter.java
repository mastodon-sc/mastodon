/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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

import org.mastodon.collection.RefSet;

/**
 * Adapts a {@code RefSet<O>} as a {@code RefSet<WO>}. The mapping between
 * source elements {@code O} and wrapped elements {@code WO} is established by a
 * {@code RefBimap<O, WO>}.
 *
 * @param <O>
 *            element type of source collection being wrapped.
 * @param <WO>
 *            element type of this wrapper collection.
 *
 * @author Tobias Pietzsch
 */
public class RefSetAdapter< O, WO >
		implements RefSet< WO >
{
	private final RefSet< O > set;

	private final RefBimap< O, WO > map;

	public RefSetAdapter(
			final RefSet< O > set,
			final RefBimap< O, WO > map )
	{
		this.set = set;
		this.map = map;
	}

	@Override
	public WO createRef()
	{
		return map.reusableRightRef();
	}

	@Override
	public void releaseRef( final WO obj )
	{
		map.releaseRef( obj );
	}

	@Override
	public int size()
	{
		return set.size();
	}

	@Override
	public boolean isEmpty()
	{
		return set.isEmpty();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean contains( final Object o )
	{
		return set.contains( map.getLeft( ( WO ) o ) );
	}

	@Override
	public Iterator< WO > iterator()
	{
		return new Iterator< WO >()
		{
			private final Iterator< O > iterator = set.iterator();

			private final WO ref = createRef();

			@Override
			public boolean hasNext()
			{
				return iterator.hasNext();
			}

			@Override
			public WO next()
			{
				return map.getRight( iterator.next(), ref );
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
		return set.add( map.getLeft( e ) );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean remove( final Object o )
	{
		return set.remove( map.getLeft( ( WO ) o ) );
	}

	@Override
	public boolean containsAll( final Collection< ? > c )
	{
		throw new UnsupportedOperationException( "not implemented (yet)" );
	}

	@Override
	public boolean addAll( final Collection< ? extends WO > c )
	{
		throw new UnsupportedOperationException( "not implemented (yet)" );
	}

	@Override
	public boolean removeAll( final Collection< ? > c )
	{
		throw new UnsupportedOperationException( "not implemented (yet)" );
	}

	@Override
	public boolean retainAll( final Collection< ? > c )
	{
		throw new UnsupportedOperationException( "not implemented (yet)" );
	}

	@Override
	public void clear()
	{
		set.clear();
	}
}
