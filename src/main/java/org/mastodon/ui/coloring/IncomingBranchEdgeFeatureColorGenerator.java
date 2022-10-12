package org.mastodon.ui.coloring;

import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;

public class IncomingBranchEdgeFeatureColorGenerator< V extends Vertex< E >, E extends Edge< V >, BV extends Vertex< BE >, BE extends Edge< BV > >
		extends AbstractBranchEdgeColorGenerator< V, E, BV, BE >
		implements EdgeColorGenerator< V, E >
{

	public IncomingBranchEdgeFeatureColorGenerator(
			final FeatureProjection< BE > featureProjection,
			final BranchGraph< BV, BE, V, E > branchGraph,
			final ColorMap colorMap,
			final double min,
			final double max )
	{
		super( featureProjection, branchGraph, colorMap, min, max );
	}

	@Override
	public int color( final E edge, final V source, final V target )
	{
		final BE beRef = branchGraph.edgeRef();
		final BV bvRef = branchGraph.vertexRef();
		try
		{
			BE be = branchGraph.getBranchEdge( edge, beRef );
			if(be == null) {
				final BV bv = branchGraph.getBranchVertex( edge, bvRef );
				if(bv == null)
					return 0;
				if(bv.incomingEdges().size() != 1)
					return 0;
				be = bv.incomingEdges().get(0, beRef);
			}
			return colorGenerator.color( be, null, null );
		}
		finally
		{
			branchGraph.releaseRef( beRef );
			branchGraph.releaseRef( bvRef );
		}
	}
}
