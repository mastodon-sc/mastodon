package net.trackmate.graph.traversal;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;

public class DepthFirstIteratorUndirected< V extends Vertex< E >, E extends Edge< V > > extends DepthFirstIterator< V, E >
{

	DepthFirstIteratorUndirected( final V root, final Graph< V, E > graph )
	{
		super( root, graph );
	}

	@Override
	protected Iterable< E > neighbors( final V vertex )
	{
		return vertex.edges();
	}


	static < V extends Vertex< E >, E extends Edge< V > > DepthFirstIteratorUndirected< V, E > create( final V root, final Graph< V, E > graph )
	{
		return new DepthFirstIteratorUndirected< V, E >( root, graph );
	}
}
