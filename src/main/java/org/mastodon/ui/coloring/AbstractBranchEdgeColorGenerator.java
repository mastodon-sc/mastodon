package org.mastodon.ui.coloring;

import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;

/**
 * Mother class for color generators that return a color based on feature
 * defined for a branch edge 'upward' or 'downward' in time.
 * 
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of vertex in the core graph.
 * @param <E>
 *            the type of edge in the core graph.
 * @param <BV>
 *            the type of vertices in he branch graph.
 * @param <BE>
 *            the type of vertices in he branch graph.
 */
public abstract class AbstractBranchEdgeColorGenerator< V extends Vertex< E >, E extends Edge< V >, BV extends Vertex< BE >, BE extends Edge< BV > >
{

	protected final FeatureEdgeColorGenerator< BV, BE > colorGenerator;

	protected final BranchGraph< BV, BE, V, E > branchGraph;

	public AbstractBranchEdgeColorGenerator(
			final FeatureProjection< BE > featureProjection,
			final BranchGraph< BV, BE, V, E > branchGraph,
			final ColorMap colorMap,
			final double min,
			final double max )
	{
		this.branchGraph = branchGraph;
		this.colorGenerator = new FeatureEdgeColorGenerator<>( featureProjection, colorMap, min, max );
	}

	protected final int fromEdge( final E e )
	{
		final BE beref = branchGraph.edgeRef();
		try
		{
			final BE be = branchGraph.getBranchEdge( e, beref );
			return colorGenerator.color( be, null, null );
		}
		finally
		{
			branchGraph.releaseRef( beref );
		}
	}
}
