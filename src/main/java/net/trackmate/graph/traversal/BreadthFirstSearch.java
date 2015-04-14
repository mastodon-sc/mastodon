package net.trackmate.graph.traversal;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Edges;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.RefDeque;
import net.trackmate.graph.util.Graphs;

/**
 * Breadth-first search for directed or undirected graph, following the
 * framework of Skiena. http://www.algorist.com/
 * 
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of the graph vertices iterated.
 * @param <E>
 *            the type of the graph edges iterated.
 */
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
	public void start( final V start )
	{
		queue.clear();
		super.start( start );
	}

	@Override
	protected void visit( final V start )
	{
		queue.add( start );
		discovered.add( start );

		final V tmpRef = vertexRef();
		while ( !queue.isEmpty() )
		{
			if ( wasAborted() )
				return;

			time++;
			final V vertex = queue.poll( tmpRef );
			searchListener.processVertexEarly( vertex, time, this );
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
					searchListener.processEdge( edge, vertex, target, time, this );
				}
				if ( !discovered.contains( target ) )
				{
					queue.add( target );
					discovered.add( target );
					parents.put( vertex, start );
				}
				if ( wasAborted() )
					return;
			}
			time++;
			searchListener.processVertexLate( vertex, time, this );
		}
	}
}
