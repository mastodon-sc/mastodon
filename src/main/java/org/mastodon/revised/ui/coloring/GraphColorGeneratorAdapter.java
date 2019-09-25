package org.mastodon.revised.ui.coloring;

import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

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

	/** report current color for the view graph's vertex */
	@Override
	public int color( final WV vertex )
	{
		if ( colorGenerator == null )
			return 0;
		else
			return colorGenerator.color( vertexMap.getLeft( vertex ) );
	}

	/** report current color for the view graph's edge */
	@Override
	public int color( final WE edge, final WV source, final WV target )
	{
		if ( colorGenerator == null )
			return 0;
		else
			return colorGenerator.color( edgeMap.getLeft( edge ), vertexMap.getLeft( source ), vertexMap.getLeft( target ) );
	}

	/** report current color for the model graph's vertex */
	public int spotColor( final V vertex )
	{
		if ( colorGenerator == null )
			return 0;
		else
			return colorGenerator.color( vertex );
	}

	/** report current color for the model graph's edge */
	public int edgeColor( final E edge, final V source, final V target )
	{
		if ( colorGenerator == null )
			return 0;
		else
			return colorGenerator.color( edge, source, target );
	}
}
