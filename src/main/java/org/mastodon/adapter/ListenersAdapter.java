package org.mastodon.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.scijava.listeners.Listeners;

/**
 * Wraps a {@code Listeners<T>} as a {@code Listeners<WT>}.
 *
 * @param <T>
 *            listener type of source {@code Listeners} being wrapped.
 * @param <WT>
 *            listener type of this wrapper {@code Listeners}.
 */
public interface ListenersAdapter< T, WT > extends Listeners< WT >
{
	class List< T, WT > implements ListenersAdapter< T, WT >
	{
		private final Listeners< T > listeners;

		private final Function< WT, T > wrap;

		private final Map< WT, T > wrappers = new HashMap<>();

		/**
		 * @param listeners
		 * 		the {@code Listeners<T>} to wrap
		 * @param wrap
		 * 		function that wraps a listener {@code WT} as a listener {@code T}
		 */
		public List( final Listeners< T > listeners, final Function< WT, T > wrap )
		{
			this.listeners = listeners;
			this.wrap = wrap;
		}

		@Override
		public boolean add( final WT listener )
		{
			final T wrapper = wrap.apply( listener );
			wrappers.put( listener, wrapper );
			return listeners.add( wrapper );
		}

		@Override
		public boolean add( final int index, final WT listener )
		{
			final T wrapper = wrap.apply( listener );
			wrappers.put( listener, wrapper );
			return listeners.add( index, wrapper );
		}

		@Override
		public boolean remove( final WT listener )
		{
			final T wrapper = wrappers.remove( listener );
			return listeners.remove( wrapper );
		}
	}

	class SynchronizedList< T, WT > extends List< T, WT >
	{
		/**
		 * @param listeners
		 * 		the {@code Listeners<T>} to wrap
		 * @param wrap
		 * 		function that wraps a listener {@code WT} as a listener {@code T}
		 */
		public SynchronizedList( final Listeners< T > listeners, final Function< WT, T > wrap )
		{
			super( listeners, wrap );
		}

		@Override
		public synchronized boolean add( final WT listener )
		{
			return super.add( listener );
		}

		@Override
		public synchronized boolean add( final int index, final WT listener )
		{
			return super.add( index, listener );
		}

		@Override
		public synchronized boolean remove( final WT listener )
		{
			return super.remove( listener );
		}
	}
}
