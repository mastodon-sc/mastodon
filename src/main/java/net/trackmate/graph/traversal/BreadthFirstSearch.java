package net.trackmate.graph.traversal;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Edges;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.RefDeque;
import net.trackmate.graph.util.Graphs;

public class BreadthFirstSearch< V extends Vertex< E >, E extends Edge< V > > extends GraphSearch< V, E >
{
	private final RefDeque< V > queue;

	private final boolean directed;

	public BreadthFirstSearch( final Graph< V, E > graph, final boolean directed )
	{
		super( graph );
		this.directed = directed;
		this.queue = createVertexDeque();
	}

	@Override
	protected void visit( final V start )
	{
		queue.add( start );
		discovered.add( start );

		final V tmpRef = vertexRef();
		while ( !queue.isEmpty() )
		{
			if ( finished )
				return;

			time++;
			final V vertex = queue.poll( tmpRef );
			traversalListener.processVertexEarly( vertex, time, this );
			processed.add( vertex );

			final Edges< E > edges;
			if ( directed )
			{
				edges = vertex.outgoingEdges();
			}
			else
			{
				edges = vertex.edges();
			}
			V target = vertexRef();
			for ( final E edge : edges )
			{
				target = Graphs.getOppositeVertex( edge, vertex, target );
				if ( !processed.contains( target ) )
				{
					traversalListener.processEdge( edge, vertex, target, time, this );
				}
				if ( !discovered.contains( target ) )
				{
					queue.add( target );
					discovered.add( target );
					parents.put( vertex, start );
				}
				if ( finished )
					return;
			}
			time++;
			traversalListener.processVertexLate( vertex, time, this );
		}
	}
}
