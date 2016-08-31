package org.mastodon.revised.trackscheme;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.ui.selection.NavigationHandler;
import org.mastodon.revised.ui.selection.NavigationListener;

public class DefaultModelNavigationProperties< V extends Vertex< E >, E extends Edge< V > > implements
		ModelNavigationProperties,
		NavigationListener< V, E >
{
	private final ReadOnlyGraph< V, E > graph;

	private final GraphIdBimap< V, E > idmap;

	private final NavigationHandler< V, E > navigation;

	private ModelNavigationListener modelNavigationListener;

	public DefaultModelNavigationProperties(
			final ReadOnlyGraph< V, E > graph,
			final GraphIdBimap< V, E > idmap,
			final NavigationHandler< V, E > navigation )
	{
		this.graph = graph;
		this.idmap = idmap;
		this.navigation = navigation;
		navigation.addNavigationListener( this );
	}

	@Override
	public void notifyNavigateToVertex( final int modelVertexId )
	{
		final V ref = graph.vertexRef();
		navigation.notifyNavigateToVertex( idmap.getVertex( modelVertexId, ref ) );
		graph.releaseRef( ref );
	}

	@Override
	public void notifyNavigateToEdge( final int modelEdgeId )
	{
		final E ref = graph.edgeRef();
		navigation.notifyNavigateToEdge( idmap.getEdge( modelEdgeId, ref ) );
		graph.releaseRef( ref );
	}

	@Override
	public void forwardNavigationEventsTo( final ModelNavigationListener listener )
	{
		this.modelNavigationListener = listener;
	}

	@Override
	public void navigateToVertex( final V vertex )
	{
		if ( modelNavigationListener != null )
			modelNavigationListener.navigateToVertex( idmap.getVertexId( vertex ) );
	}

	@Override
	public void navigateToEdge( final E edge )
	{
		if ( modelNavigationListener != null )
			modelNavigationListener.navigateToEdge( idmap.getEdgeId( edge ) );
	}
}
