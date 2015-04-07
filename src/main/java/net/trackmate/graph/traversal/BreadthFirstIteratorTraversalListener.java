package net.trackmate.graph.traversal;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;

public class BreadthFirstIteratorTraversalListener< V extends Vertex< E >, E extends Edge< V > > extends BreadthFirstIterator< V, E >
{

	private final GraphTraversalListener< V, E > traversalListener;

	BreadthFirstIteratorTraversalListener( final V root, final Graph< V, E > graph, final GraphTraversalListener< V, E > traversalListener )
	{
		super( root, graph );
		this.traversalListener = traversalListener;
		traversalListener.vertexTraversed( root );
	}

	@Override
	protected void fetchNext()
	{
		while ( canFetch() )
		{
			fetched = fetch( fetched );
			for ( final E edge : neighbors( fetched ) )
			{
				final V target = edge.getTarget( tmpRef );
				if ( !visited.contains( target ) )
				{
					traversalListener.edgeTraversed( edge );
					traversalListener.vertexTraversed( target );
					visited.add( target );
					toss( target );
				}
			}
			return;
		}
		releaseRef( tmpRef );
		releaseRef( fetched );
		fetched = null;
	}

	static < V extends Vertex< E >, E extends Edge< V > > BreadthFirstIterator< V, E > create( final V root, final Graph< V, E > graph, final GraphTraversalListener< V, E > traversalListener )
	{
		return new BreadthFirstIteratorTraversalListener< V, E >( root, graph, traversalListener );
	}

}
