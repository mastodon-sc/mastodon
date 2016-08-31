package org.mastodon.graph.algorithm;

import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Graph;
import org.mastodon.graph.Vertex;

/**
 * Small algorithm that returns a set of vertices that have no incoming edges.
 *
 * @author Jean-Yves Tinevez
 */
public class RootFinder< V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphAlgorithm< V, E >
{
	private final RefSet< V > roots;

	public RootFinder( final Graph< V, E > graph )
	{
		super( graph );
		this.roots = createVertexSet();
		fetchRoots();
	}

	public RefSet< V > get()
	{
		return roots;
	}

	private void fetchRoots()
	{
		for ( final V v : graph.vertices() )
		{
			if ( v.incomingEdges().isEmpty() )
			{
				roots.add( v );
			}
		}
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public static < V extends Vertex< ? >> RefSet< V > getRoots( final Graph< V, ? > graph )
	{
		return new RootFinder( graph ).get();
	}

}
