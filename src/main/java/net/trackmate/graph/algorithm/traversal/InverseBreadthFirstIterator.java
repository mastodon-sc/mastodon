/**
 *
 */
package net.trackmate.graph.algorithm.traversal;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;

/**
 * Same as {@link BreadthFirstIterator} on a graph where all directed edges are
 * pointing in the opposite direction.
 *
 * @author jug
 */
public class InverseBreadthFirstIterator< V extends Vertex< E >, E extends Edge< V > > extends BreadthFirstIterator< V, E >
{
	/**
	 * @param root
	 * @param graph
	 */
	public InverseBreadthFirstIterator( final V root, final Graph< V, E > graph )
	{
		super( root, graph );
	}

	@Override
	protected Iterable< E > neighbors( final V vertex )
	{
		return vertex.incomingEdges();
	}
}
