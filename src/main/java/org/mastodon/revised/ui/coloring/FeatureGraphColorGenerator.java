package org.mastodon.revised.ui.coloring;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

public class FeatureGraphColorGenerator< V extends Vertex< E >, E extends Edge< V > > implements GraphColorGenerator< V, E >
{

	private final FeatureColorGenerator< V > vertexColorGenerator;

	private final FeatureColorGenerator< E > edgeColorGenerator;

	public FeatureGraphColorGenerator( final FeatureColorGenerator< V > vertexColorGenerator, final FeatureColorGenerator< E > edgeColorGenerator )
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
