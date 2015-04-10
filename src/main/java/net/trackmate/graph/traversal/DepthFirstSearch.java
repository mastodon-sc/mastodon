package net.trackmate.graph.traversal;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Edges;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.RefList;
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

		/*
		 * Collect target vertices and edges.
		 */

		final RefList< V > targets;
		final RefList< E > targetEdges;
		V target = vertexRef();
		if ( directed )
		{
			final Edges< E > edges = vertex.outgoingEdges();
			targets =  createVertexList( edges.size() );
			targetEdges = createEdgeList( edges.size() );
			for ( final E e : edges )
			{
				target = e.getTarget( target );
				targets.add( target );
				targetEdges.add( e );
			}
		}
		else
		{
			final Edges< E > edges = vertex.edges();
			targets =  createVertexList( edges.size() );
			targetEdges = createEdgeList( edges.size() );
			for ( final E e : edges )
			{
				target = Graphs.getOppositeVertex( e, vertex, target );
				targets.add( target );
				targetEdges.add( e );
			}
		}

		/*
		 * Potentially sort vertices and edges according to vertices sort order.
		 */

		if ( null != comparator && targets.size() > 1 )
		{
			Graphs.sort( targets, comparator, targetEdges );
		}

		/*
		 * Discover vertices across these edges.
		 */

		E edge = edgeRef();
		for ( int i = 0; i < targets.size(); i++ )
		{
			edge = targetEdges.get( i, edge );
			target = targets.get( i, target );

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
		releaseRef( target );
		releaseRef( edge );
	}
}
