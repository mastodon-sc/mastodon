package net.trackmate.graph.traversal;

import java.util.Comparator;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;

class BreadthFirstIteratorSortedUndirected< V extends Vertex< E >, E extends Edge< V > > extends BreadthFirstIteratorSorted< V, E >
{

	BreadthFirstIteratorSortedUndirected( final V root, final Graph< V, E > graph, final Comparator< V > comparator )
	{
		super( root, graph, comparator );
	}

	@Override
	protected Iterable< E > neighbors( final V vertex )
	{
		return vertex.edges();
	}

	static < V extends Vertex< E >, E extends Edge< V > > BreadthFirstIteratorSortedUndirected< V, E > create( final V root, final Graph< V, E > graph, final Comparator< V > comparator )
	{
		return new BreadthFirstIteratorSortedUndirected< V, E >( root, graph, comparator );
	}
}
