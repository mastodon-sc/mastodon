package org.mastodon.ui.coloring;

import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;

public class BranchFeatureColorGeneratorOutgoingEdge< V extends Vertex< E >, E extends Edge< V >, BV extends Vertex< BE >, BE extends Edge< BV > >
		extends AbstractBranchEdgeColorGenerator< V, E, BV, BE >
		implements ColorGenerator< V >
{

	public BranchFeatureColorGeneratorOutgoingEdge(
			final FeatureProjection< BE > featureProjection,
			final BranchGraph< BV, BE, V, E > branchGraph,
			final ColorMap colorMap,
			final double min,
			final double max )
	{
		super( featureProjection, branchGraph, colorMap, min, max );
	}

	@Override
	public int color( final V v )
	{
		final BE beRef = branchGraph.edgeRef();
		final BV bvRef = branchGraph.vertexRef();
		try
		{
			final BV bv = branchGraph.getBranchVertex( v, bvRef );
			if(bv == null)
				return 0;
			if(bv.outgoingEdges().size() != 1)
				return 0;
			BE be = bv.outgoingEdges().get(0, beRef);
			return colorGenerator.color( be, null, null );
		}
		finally
		{
			branchGraph.releaseRef( beRef );
			branchGraph.releaseRef( bvRef );
		}
	}
}
