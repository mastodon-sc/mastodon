package net.trackmate.graph.traversal;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.RefList;

public class BreadthFirstIteratorSortedUndirectedTraversalListener< V extends Vertex< E >, E extends Edge< V > > extends BreadthFirstIteratorSortedUndirected< V, E > implements Iterator< V >
{

	private final GraphTraversalListener< V, E > traversalListener;

	private final RefList< E > edgeList;

	public BreadthFirstIteratorSortedUndirectedTraversalListener( final V root, final Graph< V, E > graph, final Comparator< V > comparator, final GraphTraversalListener< V, E > traversalListener )
	{
		super( root, graph, comparator );
		this.edgeList = createEdgeList();
		this.traversalListener = traversalListener;
		traversalListener.vertexTraversed( root );
	}

	@Override
	protected void fetchNext()
	{
		while ( canFetch() )
		{
			fetched = fetch( fetched );
			list.clear();
			edgeList.clear();
			for ( final E e : neighbors( fetched ) )
			{
				final V target = e.getTarget( tmpRef );
				if ( !visited.contains( target ) )
				{
					visited.add( target );
					list.add( target );
					edgeList.add( e );
				}
			}

			Collections.sort( list, comparator );
			for ( int i = 0; i < list.size(); i++ )
			{
				final V vertex = list.get( i );
				final E edge = edgeList.get( i );
				traversalListener.edgeTraversed( edge );
				traversalListener.vertexTraversed( vertex );
				toss( vertex );
			}
			return;
		}
		releaseRef( tmpRef );
		releaseRef( fetched );
		fetched = null;
	}

	static < V extends Vertex< E >, E extends Edge< V > > BreadthFirstIteratorSortedUndirectedTraversalListener< V, E > create( final V root, final Graph< V, E > graph, final Comparator< V > comparator, final GraphTraversalListener< V, E > traversalListener )
	{
		return new BreadthFirstIteratorSortedUndirectedTraversalListener< V, E >( root, graph, comparator, traversalListener );
	}
}