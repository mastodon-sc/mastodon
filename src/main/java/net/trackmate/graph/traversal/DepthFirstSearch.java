package net.trackmate.graph.traversal;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Edges;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.algorithm.AbstractGraphAlgorithm;
import net.trackmate.graph.collection.RefRefMap;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.graph.util.Graphs;

public class DepthFirstSearch< V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphAlgorithm< V, E >
{

	private final RefSet< V > discovered;

	private final RefSet< V > processed;

	private final boolean directed;

	private int time;

	private boolean finished;

	private final TraversalListener< V, E > traversalListener;

	private final RefRefMap< V, V > parent;

	public DepthFirstSearch( final Graph< V, E > graph, final V start, final TraversalListener< V, E > traversalListener, final boolean directed )
	{
		super( graph );
		this.traversalListener = traversalListener;
		this.discovered = createVertexSet();
		this.processed = createVertexSet();
		this.parent = createVertexVertexMap();
		this.directed = directed;
		restart( start );
	}

	public void restart( final V start )
	{
		discovered.clear();
		processed.clear();
		parent.clear();
		time = 0;
		finished = false;
		visit( start );
	}

	public void abort()
	{
		finished = true;
	}

	public V parent( final V child )
	{
		return parent.get( child );
	}

	public int timeOf( final V vertex )
	{
		return -1; // TODO :( how can I store this elegantly?
	}

	public EdgeClass edgeClass( final V from, final V to, final int fromTime, final int toTime )
	{
		if ( parent.get( to ).equals( from ) ) { return EdgeClass.TREE; }
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

	private void visit( final V vertex )
	{
		if ( finished )
			return;

		time++;
		discovered.add( vertex );
		traversalListener.processVertexEarly( vertex, time );

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
				parent.put( target, vertex );
				traversalListener.processEdge( edge, vertex, target, time );
				visit( target );
			}

			if ( finished )
				return;
		}

		traversalListener.processVertexLate( vertex, time );
		time++;

		processed.add( vertex );
	}

	public static enum EdgeClass
	{
		TREE,
		BACK,
		FORWARD,
		CROSS,
		UNCLASSIFIED;
	}
}
