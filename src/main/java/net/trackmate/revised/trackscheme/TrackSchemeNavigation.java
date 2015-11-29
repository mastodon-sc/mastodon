package net.trackmate.revised.trackscheme;

import java.util.HashSet;
import java.util.Set;

import net.trackmate.revised.ui.selection.NavigationListener;

public class TrackSchemeNavigation implements
		ModelNavigationListener
{
	private final Set< NavigationListener< TrackSchemeVertex > > listeners = new HashSet< NavigationListener< TrackSchemeVertex > >();

	private final ModelNavigationProperties props;

	private final TrackSchemeGraph< ?, ? > graph;

	public TrackSchemeNavigation(
			final ModelNavigationProperties props,
			final TrackSchemeGraph< ?, ? > graph )
	{
		this.props = props;
		this.graph = graph;

		props.forwardNavigationEventsTo( this );
	}

	/**
	 * Registers the specified listener to this handler. The specified
	 * {@link NavigationGroupReceiver} will be used to determine to what groups this
	 * listener belongs to when passing navigation events.
	 *
	 * @param l
	 *            the {@link NavigationListener} to register.
	 * @param g
	 *            the {@link NavigationGroupReceiver} that determines to what groups it
	 *            belongs.
	 */
	public boolean addNavigationListener( final NavigationListener< TrackSchemeVertex > l )
	{
		return listeners.add( l );
	}

	public boolean removeNavigationListener( final NavigationListener< TrackSchemeVertex > l )
	{
		return listeners.remove( l );
	}

	// forward tp ModelNavigationProperties
	public void notifyNavigateToVertex( final TrackSchemeVertex v )
	{
		props.notifyNavigateToVertex( v.getModelVertexId() );
	}

	/*
	 * ModelNavigationListener implementation
	 * converts forwarded model id events to TrackSchemeVertex
	 */
	@Override
	public void navigateToVertex( final int mid )
	{
		final TrackSchemeVertex ref = graph.vertexRef();
		final TrackSchemeVertex v = graph.getTrackSchemeVertexForModelId( mid, ref );
		for ( final NavigationListener< TrackSchemeVertex > l : listeners )
			l.navigateToVertex( v );
		graph.releaseRef( ref );
	}
}
