package net.trackmate.graph.traversal;

import java.util.Comparator;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;

public class DepthFirstIteratorSortedUndirected< V extends Vertex< E >, E extends Edge< V > > extends DepthFirstIteratorSorted< V, E >
{

	DepthFirstIteratorSortedUndirected( final V root, final Graph< V, E > graph, final Comparator< V > comparator )
	{
		super( root, graph, comparator );
	}

	@Override
	protected Iterable< E > neighbors( final V vertex )
	{
		return vertex.edges();
	}

	static < V extends Vertex< E >, E extends Edge< V > > DepthFirstIteratorSortedUndirected< V, E > create( final V root, final Graph< V, E > graph, final Comparator< V > comparator )
	{
		return new DepthFirstIteratorSortedUndirected< V, E >( root, graph, comparator );
	}
}