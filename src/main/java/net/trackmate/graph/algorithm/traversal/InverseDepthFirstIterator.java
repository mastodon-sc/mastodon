/**
 *
 */
package net.trackmate.graph.algorithm.traversal;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;

/**
 * Same as {@link DepthFirstIterator} on a graph where all directed edges are
 * pointing in the opposite direction.
 *
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 */
public class InverseDepthFirstIterator< V extends Vertex< E >, E extends Edge< V > > extends DepthFirstIterator< V, E >
{
	public InverseDepthFirstIterator( final V root, final Graph< V, E > graph )
	{
		super( root, graph );
	}

	@Override
	protected Iterable< E > neighbors( final V vertex )
	{
		return vertex.incomingEdges();
	}

	@Override
	protected V targetOf( final E edge, final V ref )
	{
		return edge.getSource( ref );
	}
}