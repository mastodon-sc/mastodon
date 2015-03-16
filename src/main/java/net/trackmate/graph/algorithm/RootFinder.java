package net.trackmate.graph.algorithm;

import java.util.Iterator;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.RefSet;

/**
 * Small algorithm that returns a set of vertices that have no incoming edges.
 *
 * @author Jean-Yves Tinevez
 */
public class RootFinder< V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphAlgorithm< V, E >
{

	private final Iterator< V > iterator;

	private final RefSet< V > roots;

	public RootFinder( final Iterator< V > iterator, final Graph< V, E > graph )
	{
		super( graph );
		this.iterator = iterator;
		this.roots = createVertexSet();
		fetchRoots();
	}

	public RefSet< V > get()
	{
		return roots;
	}

	private void fetchRoots()
	{
		while ( iterator.hasNext() )
		{
			final V v = iterator.next();
			if ( v.incomingEdges().isEmpty() )
			{
				roots.add( v );
			}
		}
	}

}
