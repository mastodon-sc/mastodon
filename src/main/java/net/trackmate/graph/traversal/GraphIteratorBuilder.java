package net.trackmate.graph.traversal;

import java.util.Comparator;
import java.util.Iterator;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;

public class GraphIteratorBuilder< V extends Vertex< E >, E extends Edge< V > >
{

	private enum IterationType
	{
		DEFAULT,
		DFS,
		BFS;
	}

	private final Graph< V, E > graph;

	private IterationType iterationType;

	private V root;

	private Comparator< V > comparator;

	private boolean directed = true;

	private GraphTraversalListener< V, E > traversalListener;

	private GraphIteratorBuilder( final Graph< V, E > graph )
	{
		this.graph = graph;
		this.iterationType = IterationType.DEFAULT;
	}

	public GraphIteratorBuilder< V, E > depthFirst( final V root )
	{
		this.root = root;
		this.iterationType = IterationType.DFS;
		return this;
	}

	public GraphIteratorBuilder< V, E > breadthFirst( final V root )
	{
		this.root = root;
		this.iterationType = IterationType.BFS;
		return this;
	}

	public GraphIteratorBuilder< V, E > defaultIteration()
	{
		this.root = null;
		this.iterationType = IterationType.DEFAULT;
		return this;
	}

	public GraphIteratorBuilder< V, E > sorted( final Comparator< V > comparator )
	{
		this.comparator = comparator;
		return this;
	}

	public GraphIteratorBuilder< V, E > unsorted()
	{
		this.comparator = null;
		return this;
	}

	public GraphIteratorBuilder< V, E > directed()
	{
		this.directed = true;
		return this;
	}

	public GraphIteratorBuilder< V, E > undirected()
	{
		this.directed = false;
		return this;
	}

	public GraphIteratorBuilder< V, E > withTraversalListener( final GraphTraversalListener< V, E > listener )
	{
		traversalListener = listener;
		return this;
	}

	public Iterator< V > build()
	{
		switch ( iterationType )
		{
		case BFS:
		{
			if ( null != comparator )
			{
				// bfs, sorted
				if ( directed )
				{
					// bfs, sorted, directed
					if ( null == traversalListener )
					{
						// bfs, sorted, directed, no traversal listener
						return BreadthFirstIteratorSorted.create( root, graph, comparator );
					}
					else
					{
						// bfs, sorted, directed, with traversal listener
						return BreadthFirstIteratorSortedTraversalListener.create( root, graph, comparator, traversalListener );
					}
				}
				else
				{
					// bfs, sorted, undirected
					if ( null == traversalListener )
					{
						// bfs, sorted, undirected, no traversal listener
						return BreadthFirstIteratorSortedUndirected.create( root, graph, comparator );
					}
					else
					{
						// bfs, sorted, undirected, with traversal listener
						return new BreadthFirstIteratorSortedUndirectedTraversalListener< V, E >( root, graph, comparator, traversalListener );

					}
				}
			}
			else
			{
				// bfs, unsorted
				if ( directed )
				{
					// bfs, unsorted, directed
					if ( null == traversalListener )
					{
						// bfs, unsorted, directed, no traversal listener, the
						// standard one.
						return BreadthFirstIterator.create( root, graph );
					}
					else
					{
						// bfs, unsorted, directed, with traversal listener
						return BreadthFirstIteratorTraversalListener.create( root, graph, traversalListener );
					}
				}
				else
				{
					// bfs, unsorted, undirected
					if ( null == traversalListener )
					{
						// bfs, unsorted, undirected, no traversal listener
						return BreadthFirstIteratorUndirected.create( root, graph );
					}
					else
					{
						// bfs, unsorted, undirected, with traversal listener
						return BreadthFirstIteratorUndirectedTraversalListener.create( root, graph, traversalListener );
					}
				}
			}
		}
		case DFS:
		{
			// dfs
			if ( null != comparator )
			{
				// dfs, sorted
				if ( directed )
				{
					// dfs, sorted, directed
					if ( null == traversalListener )
					{
						// dfs, sorted, directed, no traversal listener
						return DepthFirstIteratorSorted.create( root, graph, comparator );
					}
					else
					{
						// dfs, sorted, directed, with traversal listener
						return DepthFirstIteratorSortedTraversalListener.create( root, graph, comparator, traversalListener );
					}
				}
				else
				{
					// dfs, sorted, undirected
					if ( null == traversalListener )
					{
						// dfs, sorted, directed, no traversal listener
						return DepthFirstIteratorSortedUndirected.create( root, graph, comparator );
					}
					else
					{
						// dfs, sorted, directed, with traversal listener
						return DepthFirstIteratorSortedUndirectedTraversalListener.create( root, graph, comparator, traversalListener );
					}
				}
			}
			else
			{
				// dfs, unsorted
				if ( directed )
				{
					// dfs, unsorted, directed
					if ( null == traversalListener )
					{
						// dfs, unsorted, directed, no traversal listener. The
						// standard one.
						return DepthFirstIterator.create( root, graph );
					}
					else
					{
						// dfs, unsorted, directed, with traversal listener.
						return DepthFirstIteratorTraversalListener.create( root, graph, traversalListener );
					}
				}
				else
				{
					// dfs, unsorted, undirected
					if ( null == traversalListener )
					{
						// dfs, unsorted, undirected, no traversal listener
						return DepthFirstIteratorUndirected.create( root, graph );
					}
					else
					{
						// dfs, unsorted, undirected, with traversal listener
						return DepthFirstIteratorUndirectedTraversalListener.create( root, graph, traversalListener );
					}
				}
			}
		}
		case DEFAULT:
		default:
		{
			return graph.vertexIterator();
		}
		}
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();

		/*
		 * Iteration type.
		 */
		switch ( iterationType )
		{
		case BFS:
			sb.append( "Breadth-first iterator over graph = " + graph + ", " );
			break;
		case DFS:
			sb.append( "Depth-first iterator over graph = " + graph + ", " );
			break;
		case DEFAULT:
		default:
			sb.append( "Default iterator over graph = " + graph + "." );
			return sb.toString();
		}

		/*
		 * Sorted.
		 */

		if ( null != comparator )
		{
			sb.append( "sorted with comparator = " + comparator.toString() + ", " );
		}
		else
		{
			sb.append( "unsorted, " );
		}

		/*
		 * Directed.
		 */

		if ( directed )
		{
			sb.append( "directed" );
		}
		else
		{
			sb.append( "undirected" );
		}

		/*
		 * Graph listener
		 */

		if ( null == traversalListener )
		{
			sb.append( "." );
		}
		else
		{
			sb.append( "With traversal listener = " + traversalListener.toString() + '.' );
		}

		return sb.toString();
	}
}
