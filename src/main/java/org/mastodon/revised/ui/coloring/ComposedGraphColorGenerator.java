package org.mastodon.revised.ui.coloring;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

public class ComposedGraphColorGenerator< V extends Vertex< E >, E extends Edge< V > > implements GraphColorGenerator< V, E >
{

	private final ColorGenerator< V > vertexColorGenerator;

	private final ColorGenerator< E > edgeColorGenerator;

	public ComposedGraphColorGenerator( final ColorGenerator< V > vertexColorGenerator, final ColorGenerator< E > edgeColorGenerator )
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
		return edgeColorGenerator.color( edge );
	}

}