package org.mastodon.ui.coloring;

import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;

/**
 * Mother class for color generators that return a color for a vertex based on
 * feature defined for a branch vertex 'upward' or 'downward' in time.
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
public abstract class AbstractBranchVertexColorGenerator< V extends Vertex< E >, E extends Edge< V >, BV extends Vertex< BE >, BE extends Edge< BV > >
{

	protected final FeatureColorGenerator< BV > colorGenerator;

	protected final BranchGraph< BV, BE, V, E > branchGraph;

	public AbstractBranchVertexColorGenerator(
			final FeatureProjection< BV > featureProjection,
			final BranchGraph< BV, BE, V, E > branchGraph,
			final ColorMap colorMap,
			final double min,
			final double max )
	{
		this.branchGraph = branchGraph;
		this.colorGenerator = new FeatureColorGenerator<>( featureProjection, colorMap, min, max );
	}

	protected int branchVertexColor( V v )
	{
		BV bvRef = branchGraph.vertexRef();
		try {
			BV branchVertex = branchGraph.getBranchVertex( v, bvRef );
			return colorGenerator.color( branchVertex );
		}
		finally
		{
			branchGraph.releaseRef( bvRef );
		}
	}
}
