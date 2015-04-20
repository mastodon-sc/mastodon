package net.trackmate.graph.traversal;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Edges;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.RefDeque;
import net.trackmate.graph.collection.RefIntMap;
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
	private static final int NO_ENTRY_VALUE = -1;

	protected final RefDeque< V > queue;

	protected final RefIntMap< V > depths;

	private final boolean directed;

	public BreadthFirstSearch( final Graph< V, E > graph, final boolean directed )
	{
		super( graph );
		this.directed = directed;
		this.queue = createVertexDeque();
		this.depths = createVertexIntMap( NO_ENTRY_VALUE );
	}

	@Override
	public void start( final V start )
	{
		queue.clear();
		queue.add( start );
		depths.clear();
		depths.put( start, 0 );
		super.start( start );
	}

	@Override
	protected void visit( final V start )
	{
		discovered.add( start );

		final V tmpRef = vertexRef();
		while ( !queue.isEmpty() )
		{
			if ( wasAborted() )
				return;

			final V vertex = queue.poll( tmpRef );
			final int level = depths.get( vertex );

			if ( null != searchListener )
				searchListener.processVertexEarly( vertex, this );
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
				if ( !discovered.contains( target ) )
				{
					queue.add( target );
					discovered.add( target );
					parents.put( target, vertex );
					depths.put( target, level + 1 );
				}
				if ( null != searchListener && ( directed || !processed.contains( target ) ) )
				{
					searchListener.processEdge( edge, vertex, target, this );
				}
				if ( wasAborted() )
					return;
			}
			if ( null != searchListener )
				searchListener.processVertexLate( vertex, this );
		}
	}

	@Override
	public EdgeClass edgeClass( final V from, final V to )
	{
		if ( !discovered.contains( from ) )
		{
			return EdgeClass.BACK;
		}
		else if ( !discovered.contains( to ) )
		{
			return EdgeClass.FORWARD;
		}
		else
		{
			final int depthFrom = depths.get( from );
			final int depthTo = depths.get( to );

			if ( depthFrom == depthTo )
			{
				return EdgeClass.CROSS;
			}
			else if ( depthFrom > depthTo )
			{
				return EdgeClass.BACK;
			}
			else
			{
				if ( from.equals( parents.get( to ) ) )
				{
					return EdgeClass.TREE;
				}
				else
				{
					return EdgeClass.FORWARD;

				}
			}
		}
	}
}
