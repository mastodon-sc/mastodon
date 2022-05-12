package org.mastodon.ui.coloring;

import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;

public class BranchEdgeFeatureColorGenerator< V extends Vertex< E >, E extends Edge< V >, BV extends Vertex< BE >, BE extends Edge< BV > >
		extends AbstractBranchEdgeColorGenerator< V, E, BV, BE >
		implements EdgeColorGenerator< V, E >
{

	public BranchEdgeFeatureColorGenerator(
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
		return fromEdge( edge );
	}
}
