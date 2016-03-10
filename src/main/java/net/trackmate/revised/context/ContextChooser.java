package net.trackmate.revised.context;

import java.util.ArrayList;
import java.util.Collection;

public class ContextChooser< V >
{
	public interface UpdateListener
	{
		public void contextChooserUpdated();
	}

	private final ContextProvider< V > theEmptyProvider = new ContextProvider< V >()
	{
		@Override
		public String getContextProviderName()
		{
			return "full graph";
		}

		@Override
		public boolean addContextListener( final ContextListener< V > listener )
		{
			listener.contextChanged( null );
			return true;
		}

		@Override
		public boolean removeContextListener( final ContextListener< V > listener )
		{
			return true;
		}
	};

	private final ContextListener< V > listener;

	private ContextProvider< V > provider;

	private final ArrayList< ContextProvider< V > > providers;

	private final ArrayList< ContextChooser.UpdateListener > updateListeners;

	public ContextChooser( final ContextListener< V > listener )
	{
		this.listener = listener;
		provider = theEmptyProvider;
		providers = new ArrayList<>();
		providers.add( theEmptyProvider );
		updateListeners = new ArrayList<>();
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
		provider.removeContextListener( listener );
		provider = providers.contains( p ) ? p : theEmptyProvider;
		provider.addContextListener( listener );
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
		for ( final ContextChooser.UpdateListener l : updateListeners )
			l.contextChooserUpdated();
	}

	public synchronized boolean addUpdateListener( final ContextChooser.UpdateListener l )
	{
		if ( !updateListeners.contains( l ) )
		{
			updateListeners.add( l );
			l.contextChooserUpdated();
			return true;
		}
		return false;
	}

	public synchronized boolean removeUpdateListener( final ContextChooser.UpdateListener l )
	{
		return updateListeners.remove( l );
	}
}
