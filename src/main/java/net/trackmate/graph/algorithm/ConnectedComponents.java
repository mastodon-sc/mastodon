package net.trackmate.graph.algorithm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.graph.util.Graphs;

/**
 * A class to generate the connected components of a graph, regardless of edge
 * direction.
 * <p>
 * This version of the algorithm does not listen to changes in the graph. It
 * simply generates a new set of components each time the {@link #get()} method
 * is called.
 * 
 * @author Jean-Yves Tinevez.
 *
 * @param <V>
 *            the type of the vertices of the graph.
 * @param <E>
 *            the type of the edges of the graph.
 */
public class ConnectedComponents< V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphAlgorithm< V, E >
{
	private final RefSet< V > visited;

	public ConnectedComponents( final Graph< V, E > graph )
	{
		super( graph );
		this.visited = createVertexSet();
	}

	private void visit( final V v, final RefSet< V > currentComponent )
	{
		currentComponent.add( v );
		visited.add( v );
		final V tmp = vertexRef();
		for ( final E e : v.edges() )
		{
			final V o = Graphs.getOppositeVertex( e, v, tmp );
			if ( !visited.contains( o ) )
			{
				visit( o, currentComponent );
			};
		}
	}

	/**
	 * Returns the set of connected components of the graph. Edges are traversed
	 * regardless of their direction.
	 * <p>
	 * It is ensured that all the vertices of the graph will be found exactly
	 * once in the connected component set.
	 * <p>
	 * The returned connected components are a snapshot of the graph
	 * connectivity when this method is called. Subsequent calls to this method
	 * will return a new set, that accounts for the latest graph modifications.
	 * 
	 * @return a new {@link Set} containing the connected components of the
	 *         graph.
	 */
	public Set< RefSet< V > > get()
	{
		final HashSet< RefSet< V >> components = new HashSet< RefSet< V > >();
		visited.clear();

		final Iterator< V > it = graph.vertexIterator();
		while ( it.hasNext() )
		{
			final V v = it.next();
			if ( visited.contains( v ) )
			{
				continue;
			}

			final RefSet< V > currentComponent = createVertexSet();
			visit( v, currentComponent );
			components.add( currentComponent );
		}
		return components;
	}
}
