package net.trackmate.graph.algorithm.traversal;

import java.util.Comparator;

import net.trackmate.collection.RefRefMap;
import net.trackmate.collection.RefSet;
import net.trackmate.graph.Edge;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.algorithm.AbstractGraphAlgorithm;

public abstract class GraphSearch< T extends GraphSearch< T, V, E >, V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphAlgorithm< V, E >
{
	public static enum SearchDirection
	{
		/**
		 * The graph will be iterated regardless of the edges direction.
		 */
		UNDIRECTED,
		/**
		 * The graph will be iterated following only edges direction.
		 */
		DIRECTED,
		/**
		 * The graph will be iterated following reversed edges direction (target
		 * to source).
		 */
		REVERSED;
	}

	protected final RefSet< V > discovered;

	protected final RefSet< V > processed;

	private boolean aborted;

	protected SearchListener< V, E, T > searchListener;

	protected final RefRefMap< V, V > parents;

	protected Comparator< V > comparator;

	public GraphSearch(final ReadOnlyGraph< V, E > graph)
	{
		super( graph );
		this.discovered = createVertexSet();
		this.processed = createVertexSet();
		this.parents = createVertexVertexMap();
	}

	/**
	 * Starts the search at the specified vertex.
	 * <p>
	 * This method returns when the search is complete, or when the
	 * {@link SearchListener} aborts the search by calling the {@link #abort()}
	 * method on this search.
	 *
	 * @param start
	 *            the vertex to start the search with.
	 */
	public void start( final V start )
	{
		discovered.clear();
		processed.clear();
		parents.clear();
		aborted = false;
		visit( start );
	}

	/**
	 * Sets the {@link SearchListener} to use for next search.
	 * <p>
	 * If it is not {@code null}, this listener will be notified in proper
	 * order when discovering vertices, crossing edges and finishing processing
	 * vertices. If {@code null}, there are no notifications.
	 *
	 * @param searchListener
	 *            the search listener to use for next search. Can be
	 *            {@code null}.
	 */
	public void setTraversalListener( final SearchListener< V, E, T > searchListener )
	{
		this.searchListener = searchListener;
	}

	/**
	 * Sets the comparator to use for next search.
	 * <p>
	 * This comparator is used when several children of the current vertex can
	 * be visited. If the specified comparator is not {@code null}, it is
	 * used to sort these children, which are then visited according to the
	 * order it sets. If it is {@code null}, the order is unspecified.
	 *
	 * @param comparator
	 *            the vertex comparator to use for next search. Can be
	 *            {@code null}.
	 */
	public void setComparator( final Comparator< V > comparator )
	{
		this.comparator = comparator;
	}

	/**
	 * Aborts the current search before its normal termination.
	 */
	public void abort()
	{
		aborted = true;
	}

	/**
	 * Returns {@code true} if the search was aborted before its normal
	 * completion.
	 *
	 * @return {@code true} if the search was aborted.
	 */
	public boolean wasAborted()
	{
		return aborted;
	}

	/**
	 * Returns the parent of the specified vertex in the current search tree.
	 * Returns {@code null} if the specified vertex has not been visited
	 * yet.
	 *
	 * @param child
	 *            the vertex to find the parent of.
	 * @return the vertex parent in the search tree.
	 */
	public V parent( final V child )
	{
		return parents.get( child );
	}

	/**
	 * Computes the specified edge class in the current search. Return
	 * {@link EdgeClass#UNCLASSIFIED} if the edge has not been visited yet.
	 *
	 * @param from
	 *            the vertex visited first while crossing the edge.
	 * @param to
	 *            the vertex visited last while crossing the edge.
	 * @return the edge class.
	 */
	public abstract EdgeClass edgeClass( final V from, final V to );

	/**
	 * Enumeration of the possible edge class during a graph search.
	 *
	 * @author Jean-Yves Tinevez
	 */
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
