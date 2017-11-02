package org.mastodon.adapter;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.mastodon.util.Listeners;

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
	 * Extends {@link ForwardedListeners.List}, making {@code add} and {@code remove}
	 * methods synchronized.
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
		public synchronized  void removeAll()
		{
			super.removeAll();
		}
	}
}
