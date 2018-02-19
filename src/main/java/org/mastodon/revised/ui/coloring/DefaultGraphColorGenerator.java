package org.mastodon.revised.ui.coloring;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

public class DefaultGraphColorGenerator< V extends Vertex< E >, E extends Edge< V > > implements GraphColorGenerator< V, E >
{
	public DefaultGraphColorGenerator()
	{}

	@Override
	public int color( final V vertex )
	{
		return 0;
	}

	@Override
	public int color( final E edge, final V source, final V target )
	{
		return 0;
	}
}
