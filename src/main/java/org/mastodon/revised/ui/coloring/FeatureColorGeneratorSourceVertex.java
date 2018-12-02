package org.mastodon.revised.ui.coloring;

import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

public class FeatureColorGeneratorSourceVertex< V extends Vertex< E >, E extends Edge< V > > implements EdgeColorGenerator< V, E >
{
	private final FeatureProjection< V > featureProjection;

	private final ColorMap colorMap;

	private final double min;

	private final double max;

	public FeatureColorGeneratorSourceVertex( final FeatureProjection< V > featureProjection, final ColorMap colorMap, final double min, final double max )
	{
		this.featureProjection = featureProjection;
		this.colorMap = colorMap;
		this.min = min;
		this.max = max;
	}

	@Override
	public int color( final E edge, final V source, final V target )
	{
		if ( !featureProjection.isSet( source ) )
			return 0;

		final double alpha = ( featureProjection.value( source ) - min ) / ( max - min );
		return colorMap.get( alpha );
	}
}
