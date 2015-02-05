package net.trackmate.graph.algorithm.object;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.trackmate.graph.object.ObjectEdge;
import net.trackmate.graph.object.ObjectVertex;

public class TopologicalSort< V extends ObjectVertex< ? >>
{
	private final Iterator< V > vertices;

	private boolean failed;

	private Set< V > marked;

	private Set< V > temporaryMarked;

	private final List< V > list;

	public TopologicalSort( final Iterator< V > vertices )
	{
		this.vertices = vertices;
		this.failed = false;
		this.marked = new HashSet< V >();
		this.temporaryMarked = new HashSet< V >();
		this.list = new ArrayList< V >();
		fetchList();
	}

	/**
	 * Returns the topologically sorted vertices in a list.
	 * 
	 * @return the list resulting from this sort operation.
	 */
	public List< V > get()
	{
		return list;
	}

	/**
	 * Returns <code>true</code> if the graph iterated has a cycle.
	 *
	 * @return <code>true</code> if the graph iterated is not a directed acyclic
	 *         graph.
	 */
	public boolean hasFailed()
	{
		return failed;
	}

	private void fetchList()
	{
		while ( vertices.hasNext() && !failed )
		{
			final V v1 = vertices.next();
			if ( !marked.contains( v1 ) )
			{
				visit( v1 );
			}
		}
		release();
	}

	private void visit( final V vertex )
	{
		if ( temporaryMarked.contains( vertex ) )
		{
			failed = true;
			return;
		}

		if ( !marked.contains( vertex ) )
		{
			temporaryMarked.add( vertex );
			for ( final ObjectEdge< ? > e : vertex.outgoingEdges() )
			{
				@SuppressWarnings( "unchecked" )
				final V v2 = ( V ) e.getTarget();
				visit( v2 );
			}

			marked.add( vertex );
			temporaryMarked.remove( vertex );
			list.add( vertex );
		}
	}

	private void release()
	{
		marked = null;
		temporaryMarked = null;
	}

}
