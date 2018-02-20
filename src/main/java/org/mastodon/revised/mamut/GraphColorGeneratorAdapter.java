package org.mastodon.revised.mamut;

import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.ui.coloring.GraphColorGenerator;

public class GraphColorGeneratorAdapter< V extends Vertex< E >, E extends Edge< V >, WV extends Vertex< WE >, WE extends Edge< WV > >
		implements GraphColorGenerator< WV, WE >
{
	private GraphColorGenerator< V, E > colorGenerator;

	private final RefBimap< V, WV > vertexMap;

	private final RefBimap< E, WE > edgeMap;

	public GraphColorGeneratorAdapter(
			final RefBimap< V, WV > vertexMap,
			final RefBimap< E, WE > edgeMap )
	{
		this.vertexMap = vertexMap;
		this.edgeMap = edgeMap;
	}

	public void setColorGenerator( final GraphColorGenerator< V, E > colorGenerator )
	{
		this.colorGenerator = colorGenerator;
	}

	@Override
	public int color( final WV vertex )
	{
		if ( colorGenerator == null )
			return 0;
		else
			return colorGenerator.color( vertexMap.getLeft( vertex ) );
	}

	@Override
	public int color( final WE edge, final WV source, final WV target )
	{
		if ( colorGenerator == null )
			return 0;
		else
			return colorGenerator.color( edgeMap.getLeft( edge ), vertexMap.getLeft( source ), vertexMap.getLeft( target ) );
	}
}
