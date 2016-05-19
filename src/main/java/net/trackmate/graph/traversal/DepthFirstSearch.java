package net.trackmate.graph.traversal;

import net.trackmate.collection.RefIntMap;
import net.trackmate.collection.RefList;
import net.trackmate.graph.Edge;
import net.trackmate.graph.Edges;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.util.Graphs;

/**
 * Depth-first search for directed or undirected graph, following the framework
 * of Skiena. http://www.algorist.com/
 * 
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of the graph vertices iterated.
 * @param <E>
 *            the type of the graph edges iterated.
 */
public class DepthFirstSearch< V extends Vertex< E >, E extends Edge< V > > extends GraphSearch< DepthFirstSearch< V, E >, V, E >
{
	private static final int NO_ENTRY_VALUE = -1;

	protected final boolean directed;

	protected final RefIntMap< V > entryTime;

	protected int time;

	public DepthFirstSearch( final Graph< V, E > graph, final boolean directed )
	{
		super( graph );
		this.directed = directed;
		this.entryTime = createVertexIntMap( NO_ENTRY_VALUE );
	}

	@Override
	public void start( final V start )
	{
		time = 0;
		entryTime.clear();
		super.start( start );
	}

	@Override
	protected void visit( final V vertex )
	{
		if ( wasAborted() )
			return;

		time++;
		entryTime.put( vertex, time );
		discovered.add( vertex );
		if ( null != searchListener )
			searchListener.processVertexEarly( vertex, this );

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
				if ( null != searchListener )
					searchListener.processEdge( edge, vertex, target, this );
				visit( target );
			}
			else if ( null != searchListener && ( directed || ( !processed.contains( target ) && !parents.get( vertex ).equals( target ) ) ) )
			{
				searchListener.processEdge( edge, vertex, target, this );
			}

			if ( wasAborted() )
				return;
		}

		if ( null != searchListener )
			searchListener.processVertexLate( vertex, this );
		time++;

		processed.add( vertex );
		releaseRef( target );
		releaseRef( edge );
	}

	/**
	 * Returns the time of visit for the specified vertex.
	 * 
	 * @param vertex
	 *            the vertex to time.
	 * @return the vertex discovery time.
	 */
	public int timeOf( final V vertex )
	{
		return entryTime.get( vertex );
	}

	@Override
	public EdgeClass edgeClass( final V from, final V to )
	{
		if ( from.equals( parents.get( to ) ) ) { return EdgeClass.TREE; }
		if ( discovered.contains( to ) && !processed.contains( to ) ) { return EdgeClass.BACK; }
		if ( processed.contains( to ) )
		{
			if ( timeOf( from ) < timeOf( to ) )
			{
				return EdgeClass.FORWARD;
			}
			else
			{
				return EdgeClass.CROSS;
			}
		}
		return EdgeClass.UNCLASSIFIED;
	}
}
