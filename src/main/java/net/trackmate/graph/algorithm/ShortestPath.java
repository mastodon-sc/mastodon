package net.trackmate.graph.algorithm;

import net.trackmate.collection.RefList;
import net.trackmate.graph.traversal.BreadthFirstSearch;
import net.trackmate.graph.traversal.SearchListener;
import net.trackmate.graph.zzgraphinterfaces.Edge;
import net.trackmate.graph.zzgraphinterfaces.Graph;
import net.trackmate.graph.zzgraphinterfaces.Vertex;

/**
 * A plain shortest path for unweighted graphs, directed or not. Simply based on
 * {@link BreadthFirstSearch}.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the {@link Vertex} type of the {@link Graph}.
 * @param <E>
 *            the {@link Edge} type of the {@link Graph}.
 */
public class ShortestPath< V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphAlgorithm< V, E >
{
	private final boolean directed;

	/**
	 * Creates a new shortest path searcher.
	 *
	 * @param graph
	 *            the graph to traverse.
	 * @param directed
	 *            whether the search takes into account the direction of edges.
	 */
	public ShortestPath( final Graph< V, E > graph, final boolean directed )
	{
		super( graph );
		this.directed = directed;
	}

	/**
	 * Finds the path between the specified vertices.
	 * <p>
	 * The success and result of this search strongly depends on whether this
	 * search is directed or undirected.
	 *
	 * @param from
	 *            the vertex to start search from.
	 * @param to
	 *            the vertex to reach.
	 * @return a new {@link RefList}, containing the path found <b>in reverse
	 *         order</b> ({@code to → from}). Returns {@code null} if
	 *         a path cannot be found between the specified vertices.
	 */
	public RefList< V > findPath( final V from, final V to )
	{
		final BreadthFirstSearch< V, E > search = new BreadthFirstSearch< V, E >( graph, directed );
		final VertexFinderListener vfl = new VertexFinderListener( to );
		search.setTraversalListener( vfl );
		search.start( from );

		if ( search.wasAborted() )
		{
			// Path found. Create list in reverse order.
			V tmp = vertexRef();
			final RefList< V > path = createVertexList( search.depthOf( to ) + 1 );
			tmp = assign( to, tmp );
			path.add( tmp );
			while ( !tmp.equals( from ) )
			{
				tmp = assign( search.parent( tmp ), tmp );
				path.add( tmp );
			}
			releaseRef( tmp );
			return path;
		}
		else
		{
			// Path not found.
			return null;
		}
	}

	private class VertexFinderListener implements SearchListener< V, E, BreadthFirstSearch< V, E > >
	{

		private final V target;

		public VertexFinderListener( final V target )
		{
			this.target = target;
		}

		@Override
		public void processVertexLate( final V vertex, final BreadthFirstSearch< V, E > search )
		{}

		@Override
		public void processVertexEarly( final V vertex, final BreadthFirstSearch< V, E > search )
		{
			if ( vertex.equals( target ) )
			{
				search.abort();
			}
		}

		@Override
		public void processEdge( final E edge, final V from, final V to, final BreadthFirstSearch< V, E > search )
		{}

	}
}
