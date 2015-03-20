package net.trackmate.graph.algorithm;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.RefList;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.graph.collection.RefStack;

public class DepthFirstIteratorSorted< V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphIteratorAlgorithm< V, E > implements Iterator< V >
{
	private final RefStack< V > stack;

	private final RefSet< V > visited;

	/**
	 * is returned by {@link #next()}.
	 */
	private V next;

	/**
	 * will be returned by following {@link #next()}.
	 */
	private V fetched;

	/**
	 * A utility ref.
	 */
	private final V tmpRef;

	private final Comparator< V > comparator;

	private final RefList< V > list;

	/*
	 * CONSTRUCTORS
	 */

	public DepthFirstIteratorSorted( final V root, final Graph< V, E > graph, final Comparator< V > comparator )
	{
		super( graph );
		this.comparator = comparator;
		list = createVertexList();
		visited = createVertexSet();
		stack = createVertexStack();
		next = vertexRef();
		fetched = vertexRef();
		tmpRef = vertexRef();

		stack.push( root );
		fetchNext();
	}

	public DepthFirstIteratorSorted( final V root, final Graph< V, E > graph )
	{
		this( root, graph, null );
	}

	/*
	 * METHODS
	 */

	@Override
	public boolean hasNext()
	{
		return fetched != null;
	}

	@Override
	public V next()
	{
		next = assign( fetched, next );
		fetchNext();
		return next;
	}

	private void fetchNext()
	{
		while( !stack.isEmpty() )
		{
			fetched = stack.pop( fetched );
			if ( !visited.contains( fetched ) )
			{
				visited.add( fetched );
				list.clear();
				for ( final E e : fetched.outgoingEdges() )
				{
					final V target = e.getTarget( tmpRef );
					list.add( target );
				}
				Collections.sort( list, comparator );
				// To have right order when pop from stack:
				for ( int i = list.size() - 1; i >= 0; i-- )
				{
					stack.push( list.get( i ) );
				}
				return;
			}
		}
		releaseRef( tmpRef );
		releaseRef( fetched );
		// we cannot release next, because it might still be in used outside of the iterator
		fetched = null;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException( "Remove is not supported for DepthFirstIterator." );
	}

	/*
	 * STATIC METHODS
	 */

	public static < V extends Vertex< E >, E extends Edge< V > > DepthFirstIteratorSorted< V, E > create( final V root, final Graph< V, E > graph, final Comparator< V > comparator )
	{
		return new DepthFirstIteratorSorted< V, E >( root, graph, comparator );
	}

	public static < V extends Vertex< E >, E extends Edge< V > > DepthFirstIteratorSorted< V, E > create( final V root, final Graph< V, E > graph )
	{
		return new DepthFirstIteratorSorted< V, E >( root, graph );
	}

}
