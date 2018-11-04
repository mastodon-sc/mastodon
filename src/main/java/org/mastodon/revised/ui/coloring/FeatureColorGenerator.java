package org.mastodon.revised.ui.coloring;

import org.mastodon.feature.FeatureProjection;

public class FeatureColorGenerator< T > implements ColorGenerator< T >
{

	private final FeatureProjection< T > featureProjection;

	private final ColorMap colorMap;

	private final double min;

	private final double max;

	public FeatureColorGenerator( final FeatureProjection< T > featureProjection, final ColorMap colorMap, final double min, final double max )
	{
		this.featureProjection = featureProjection;
		this.colorMap = colorMap;
		this.min = min;
		this.max = max;
	}

	@Override
	public int color( final T object )
	{
		if ( !featureProjection.isSet( object ) )
			return 0;

		final double alpha = ( featureProjection.value( object ) - min ) / ( max - min );
		return colorMap.get( alpha );
	}
}