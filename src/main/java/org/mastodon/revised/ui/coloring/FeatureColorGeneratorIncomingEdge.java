package org.mastodon.revised.ui.coloring;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Edges;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.model.feature.FeatureProjection;

public class FeatureColorGeneratorIncomingEdge< V extends Vertex< E >, E extends Edge< V > > implements ColorGenerator< V >
{

	private final FeatureProjection< E > featureProjection;

	private final ColorMap colorMap;

	private final double min;

	private final double max;

	private final E ref;

	public FeatureColorGeneratorIncomingEdge( final FeatureProjection< E > featureProjection, final ColorMap colorMap, final double min, final double max, final E ref )
	{
		this.featureProjection = featureProjection;
		this.colorMap = colorMap;
		this.min = min;
		this.max = max;
		this.ref = ref;
	}

	@Override
	public int color( final V vertex )
	{
		final Edges< E > edges = vertex.incomingEdges();
		if ( edges.size() != 1 )
			return 0;

		final E e = edges.get( 0, ref );
		if ( !featureProjection.isSet( e ) )
			return 0;

		final double alpha = ( featureProjection.value( e ) - min ) / ( max - min );
		return colorMap.get( alpha );
	}
}
