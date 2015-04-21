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
					parents.put( target, vertex );
					depths.put( target, level + 1 );
				}
				if ( null != searchListener && ( directed || !processed.contains( target ) ) )
				{
					searchListener.processEdge( edge, vertex, target, this );
				}
				if ( !discovered.contains( target ) )
				{
					discovered.add( target );
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
		if ( !discovered.contains( to ) ) { return EdgeClass.TREE; }

		int toDepth = depths.get( to );
		int fromDepth = depths.get( from );

		V b = vertexRef();
		b = assign( to, b );
		while ( toDepth > 0 && fromDepth < toDepth )
		{
			b = parents.get( b, b );
			toDepth = depths.get( b );
		}

		V a = vertexRef();
		a = assign( from, a );
		while ( fromDepth > 0 && toDepth < fromDepth )
		{
			a = parents.get( a, a );
			fromDepth = depths.get( a );
		}

		if ( a.equals( b ) )
		{
			releaseRef( a );
			releaseRef( b );
			return EdgeClass.BACK;
		}
		else
		{
			releaseRef( a );
			releaseRef( b );
			return EdgeClass.CROSS;
		}

	}
}
