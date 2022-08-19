package org.mastodon.ui.coloring;

import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch_v2.BranchGraphV2;

public class OutgoingBranchEdgeFeatureColorGenerator< V extends Vertex< E >, E extends Edge< V >, BV extends Vertex< BE >, BE extends Edge< BV > >
		extends AbstractBranchEdgeColorGenerator< V, E, BV, BE >
		implements EdgeColorGenerator< V, E >
{

	public OutgoingBranchEdgeFeatureColorGenerator(
			final FeatureProjection< BE > featureProjection,
			final BranchGraphV2< BV, BE, V, E > branchGraph,
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
				if(bv.outgoingEdges().size() != 1)
					return 0;
				be = bv.outgoingEdges().get(0, beRef);
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
