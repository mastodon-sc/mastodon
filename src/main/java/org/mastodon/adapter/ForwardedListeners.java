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

import java.util.ArrayList;
import java.util.function.Consumer;

import org.scijava.listeners.Listeners;

/**
 * A subset of listeners of type {@code T} forwarding to a wrapped
 * {@link Listeners} instance. It extends the {@code Listeners} interface by a
 * {@link #removeAll()} method that can be used to remove all listeners that
 * have been registered through this {@code ForwardedListeners}.
 *
 * @param <T>
 *            listener type
 */
public interface ForwardedListeners< T > extends Listeners< T >
{
	public void removeAll();

	/**
	 * Implements {@link Listeners} using an {@link ArrayList}.
	 *
	 * @param <T>
	 *            the type of listeners.
	 */
	public static class List< T > implements ForwardedListeners< T >
	{
		private final Listeners< T > listeners;

		private final Consumer< T > onAdd;

		public List( final Listeners< T > listeners, final Consumer< T > onAdd )
		{
			this.listeners = listeners;
			this.onAdd = onAdd;
		}

		public List( final Listeners< T > listeners )
		{
			this( listeners, o -> {} );
		}

		public final ArrayList< T > list = new ArrayList<>();

		@Override
		public boolean add( final T listener )
		{
			if ( !list.contains( listener ) )
			{
				list.add( listener );
				onAdd.accept( listener );
			}
			return listeners.add( listener );
		}

		@Override
		public boolean add( final int index, final T listener )
		{
			if ( !list.contains( listener ) )
			{
				list.add( index, listener );
				onAdd.accept( listener );
			}
			return listeners.add( listener );
		}

		@Override
		public boolean remove( final T listener )
		{
			list.remove( listener );
			return listeners.remove( listener );
		}

		@Override
		public void removeAll()
		{
			listeners.removeAll( list );
		}
	}

	/**
	 * Extends {@link ForwardedListeners.List}, making {@code add} and
	 * {@code remove} methods synchronized.
	 *
	 * @param <T>
	 *            the type of listeners.
	 */
	public static class SynchronizedList< T > extends List< T >
	{
		public SynchronizedList( final Listeners< T > listeners, final Consumer< T > onAdd )
		{
			super( listeners, onAdd );
		}

		public SynchronizedList( final Listeners< T > listeners )
		{
			super( listeners );
		}

		@Override
		public synchronized boolean add( final T listener )
		{
			return super.add( listener );
		}

		@Override
		public synchronized boolean remove( final T listener )
		{
			return super.remove( listener );
		}

		@Override
		public synchronized void removeAll()
		{
			super.removeAll();
		}
	}
}
