package org.mastodon.revised.ui.coloring;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

public class DefaultEdgeColorGenerator< V extends Vertex< E >, E extends Edge< V > > implements EdgeColorGenerator< V, E >
{
	@Override
	public int color( final E edge, final V source, final V target )
	{
		return 0;
	}
}
