package org.mastodon.ui.coloring;

import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Edges;
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
		final Edges< E > edges = v.outgoingEdges();
		if ( edges.size() != 1 )
			return 0;

		final E e = edges.iterator().next();
		return fromEdge( e );
	}
}
