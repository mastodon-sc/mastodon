package net.trackmate.graph.traversal;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.algorithm.AbstractGraphAlgorithm;
import net.trackmate.graph.collection.RefRefMap;
import net.trackmate.graph.collection.RefSet;

public abstract class GraphSearch< V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphAlgorithm< V, E >
{

	protected final RefSet< V > discovered;

	protected final RefSet< V > processed;

	protected int time;

	protected boolean finished;

	protected SearchListener< V, E > traversalListener;

	protected final RefRefMap< V, V > parents;

	public GraphSearch(final Graph< V, E > graph)
	{
		super( graph );
		this.discovered = createVertexSet();
		this.processed = createVertexSet();
		this.parents = createVertexVertexMap();
	}

	public void start( final V start, final SearchListener< V, E > traversalListener )
	{
		this.traversalListener = traversalListener;
		discovered.clear();
		processed.clear();
		parents.clear();
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
		return parents.get( child );
	}

	public int timeOf( final V vertex )
	{
		return -1; // TODO :( how can I store this elegantly?
	}

	public EdgeClass edgeClass( final V from, final V to )
	{
		if ( parents.get( to ).equals( from ) ) { return EdgeClass.TREE; }
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

	public static enum EdgeClass
	{
		TREE,
		BACK,
		FORWARD,
		CROSS,
		UNCLASSIFIED;
	}

	protected abstract void visit( V vertex );

}
