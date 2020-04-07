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
