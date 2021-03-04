/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.context;

import java.util.ArrayList;
import java.util.Collection;

import org.scijava.listeners.Listeners;

public class ContextChooser< V >
{
	public interface UpdateListener
	{
		public void contextChooserUpdated();
	}

	private final ContextProvider< V > theEmptyProvider = new ContextProvider< V >()
	{
		@Override
		public String getName()
		{
			return "full graph";
		}

		@Override
		public Listeners< ContextListener< V > > listeners()
		{
			return listeners;
		}

		private final Listeners< ContextListener< V > > listeners = new Listeners< ContextListener< V > >()
		{
			@Override
			public boolean add( final ContextListener< V > listener )
			{
				listener.contextChanged( null );
				return true;
			}

			@Override
			public boolean add( final int index, final ContextListener< V > listener )
			{
				return add( listener );
			}

			@Override
			public boolean remove( final ContextListener< V > listener )
			{
				return true;
			}
		};
	};

	private final ContextListener< V > listener;

	private ContextProvider< V > provider;

	private final ArrayList< ContextProvider< V > > providers;

	private final Listeners.List< UpdateListener > updateListeners;

	public ContextChooser( final ContextListener< V > listener )
	{
		this.listener = listener;
		provider = theEmptyProvider;
		providers = new ArrayList<>();
		providers.add( theEmptyProvider );
		updateListeners = new Listeners.SynchronizedList<>( l -> l.contextChooserUpdated() );
	}

	public void updateContextProviders( final Collection< ContextProvider< V > > providers )
	{
		this.providers.clear();
		this.providers.add( theEmptyProvider );
		this.providers.addAll( providers );
		if ( !providers.contains( provider ) )
			choose( theEmptyProvider );
		notifyListeners();
	}

	public void choose( final ContextProvider< V > p )
	{
		provider.listeners().remove( listener );
		provider = providers.contains( p ) ? p : theEmptyProvider;
		provider.listeners().add( listener );
		notifyListeners();
	}

	public ArrayList< ContextProvider< V > > getProviders()
	{
		return providers;
	}

	public ContextProvider< V > getChosenProvider()
	{
		return provider;
	}

	private void notifyListeners()
	{
		for ( final ContextChooser.UpdateListener l : updateListeners.list )
			l.contextChooserUpdated();
	}

	public Listeners< UpdateListener > updateListeners()
	{
		return updateListeners;
	}
}
