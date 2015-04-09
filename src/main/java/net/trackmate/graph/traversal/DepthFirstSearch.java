package net.trackmate.graph.traversal;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Edges;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.util.Graphs;

public class DepthFirstSearch< V extends Vertex< E >, E extends Edge< V > > extends GraphSearch< V, E >
{
	protected final boolean directed;

	public DepthFirstSearch( final Graph< V, E > graph, final boolean directed )
	{
		super( graph );
		this.directed = directed;
	}

	@Override
	protected void visit( final V vertex )
	{
		if ( finished )
			return;

		time++;
		discovered.add( vertex );
		traversalListener.processVertexEarly( vertex, time, this );

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
			if ( !discovered.contains( target ) )
			{
				parents.put( target, vertex );
				traversalListener.processEdge( edge, vertex, target, time, this );
				visit( target );
			}
			else if ( directed || ( !processed.contains( target ) && !parents.get( vertex ).equals( target ) ) )
			{
				traversalListener.processEdge( edge, vertex, target, time, this );
			}

			if ( finished )
				return;
		}

		traversalListener.processVertexLate( vertex, time, this );
		time++;

		processed.add( vertex );
	}
}
