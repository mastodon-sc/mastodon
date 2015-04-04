package net.trackmate.graph.traversal;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;

public class BreadthFirstIteratorUndirected< V extends Vertex< E >, E extends Edge< V > > extends BreadthFirstIterator< V, E >
{

	public BreadthFirstIteratorUndirected( final V root, final Graph< V, E > graph )
	{
		super( root, graph );
	}

	@Override
	protected Iterable< E > neighbors( final V vertex )
	{
		return vertex.edges();
	}

	public static < V extends Vertex< E >, E extends Edge< V > > BreadthFirstIteratorUndirected< V, E > create( final V root, final Graph< V, E > graph )
	{
		return new BreadthFirstIteratorUndirected< V, E >( root, graph );
	}

}
