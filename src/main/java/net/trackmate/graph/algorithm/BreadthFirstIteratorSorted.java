package net.trackmate.graph.algorithm;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.RefDeque;
import net.trackmate.graph.collection.RefList;
import net.trackmate.graph.collection.RefSet;

public class BreadthFirstIteratorSorted< V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphIteratorAlgorithm< V, E > implements Iterator< V >
{
	private V next;

	private final V tmpRef;

	private final RefSet< V > visited;

	private final RefDeque< V > queue;

	private final Comparator< V > comparator;

	private final RefList< V > list;

	/*
	 * CONSTRUCTORS
	 */

	public BreadthFirstIteratorSorted( final V root, final Graph< V, E > graph, final Comparator< V > comparator )
	{
		super( graph );
		this.comparator = comparator;
		this.list = createVertexList();
		this.visited = createVertexSet();
		this.queue = createVertexDeque();
		queue.offer( root );
		this.next = vertexRef();
		this.tmpRef = vertexRef();
	}

	public BreadthFirstIteratorSorted( final V root, final Graph< V, E > graph )
	{
		this( root, graph, null );
	}

	/*
	 * METHODS
	 */

	@Override
	public boolean hasNext()
	{
		return !queue.isEmpty();
	}

	@Override
	public V next()
	{
		while ( !queue.isEmpty() )
		{
			next = queue.poll( next );
			list.clear();
			for ( final E e : next.outgoingEdges() )
			{
				final V target = e.getTarget( tmpRef );
				list.add( target );
			}

			Collections.sort( list, comparator );
			// To have right order when pop from stack:
			for ( int i = 0; i < list.size(); i++ )
			{
				final V target = list.get( i );
				if ( !visited.contains( target ) )
				{
					visited.add( target );
					queue.offer( target );
				}
			}
			return next;
		}
		releaseRef( tmpRef );
		return null;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException( "Remove is not supported for BreadthFirstIterator." );
	}

	@Override
	public boolean isRefIterator()
	{
		return next instanceof PoolObject;
	}

	/*
	 * STATIC METHODS
	 */

	public static < V extends Vertex< E >, E extends Edge< V > > BreadthFirstIteratorSorted< V, E > create( final V root, final Graph< V, E > graph, final Comparator< V > comparator )
	{
		return new BreadthFirstIteratorSorted< V, E >( root, graph, comparator );
	}

	public static < V extends Vertex< E >, E extends Edge< V > > BreadthFirstIteratorSorted< V, E > create( final V root, final Graph< V, E > graph )
	{
		return new BreadthFirstIteratorSorted< V, E >( root, graph );
	}
}
