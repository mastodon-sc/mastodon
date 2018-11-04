package org.mastodon.mamut.feature;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.IntFeatureProjection;
import org.mastodon.feature.Multiplicity;
import org.mastodon.properties.IntPropertyMap;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

public class SpotNLinksFeature implements Feature< Spot >
{

	public static final String KEY = "Spot N links";

	private static final String HELP_STRING = "Computes the number of links that touch a spot.";

	final IntPropertyMap< Spot > map;

	private final IntFeatureProjection< Spot > projection;

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< SpotNLinksFeature, Spot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					SpotNLinksFeature.class,
					Spot.class,
					Multiplicity.SINGLE,
					new FeatureProjectionSpec( KEY, Dimension.NONE ) );
		}
	}

	SpotNLinksFeature( final IntPropertyMap< Spot > map )
	{
		this.map = map;
		this.projection = FeatureProjections.project( map, Dimension.NONE_UNITS );
	}

	@Override
	public FeatureProjection< Spot > project( final String projectionKey )
	{
		return projectionKey.equals( KEY ) ? projection : null;
	}

	@Override
	public String[] projectionKeys()
	{
		return new String[] { KEY };
	}
}
