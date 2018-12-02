package org.mastodon.revised.ui.coloring;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

public class CompositeGraphColorGenerator< V extends Vertex< E >, E extends Edge< V > > implements GraphColorGenerator< V, E >
{
	private final ColorGenerator< V > vertexColorGenerator;

	private final EdgeColorGenerator< V, E > edgeColorGenerator;

	public CompositeGraphColorGenerator( final ColorGenerator< V > vertexColorGenerator, final EdgeColorGenerator< V, E > edgeColorGenerator )
	{
		this.vertexColorGenerator = vertexColorGenerator;
		this.edgeColorGenerator = edgeColorGenerator;
	}

	@Override
	public int color( final V vertex )
	{
		return vertexColorGenerator.color( vertex );
	}

	@Override
	public int color( final E edge, final V source, final V target )
	{
		return edgeColorGenerator.color( edge, source, target );
	}
}
