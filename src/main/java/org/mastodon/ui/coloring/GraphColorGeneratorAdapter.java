package org.mastodon.ui.coloring;

import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

/**
 * Adapts a {@code GraphColorGenerator<V, E>} as a {@code GraphColorGenerator<WV, WE>}.
 * The mapping between source vertices/edges ({@code V, E}) and wrapped
 * vertices/edges ({@code WV, WE}) is established by {@link RefBimap}s.
 * <p>
 * The adapted source coloring is modifiable.
 *
 * @param <V>
 *            vertex type of wrapped source graph.
 * @param <E>
 *            edge type of wrapped source graph.
 * @param <WV>
 *            vertex type this {@code GraphColorGenerator}.
 * @param <WE>
 *            edge type this {@code GraphColorGenerator}.
 *
 * @author Tobias Pietzsch
 */
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

	/**
	 * Get the currently adapted {@code ColorGenerator} (on model graph).
	 *
	 * @return the currently adapted {@code ColorGenerator}, maybe {@code null}
	 */
	public GraphColorGenerator< V, E > getColorGenerator()
	{
		return colorGenerator;
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
