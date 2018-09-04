package org.mastodon.mamut.feature;

import org.mastodon.collection.ref.RefIntHashMap;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.IntFeatureProjection;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

public class SpotNLinksFeature implements Feature< Spot >
{

	private static final String KEY = "Spot N links";

	final RefIntHashMap< Spot > map;

	private final IntFeatureProjection< Spot > projection;

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< SpotNLinksFeature, Spot >
	{
		public Spec()
		{
			super( KEY, SpotNLinksFeature.class, Spot.class, KEY );
		}
	}

	SpotNLinksFeature(final RefIntHashMap<Spot> map)
	{
		this.map = map;
		this.projection = FeatureProjections.project( map );
	}

	@Override
	public FeatureProjection< Spot > project( final String projectionKey )
	{
		return projectionKey.equals( KEY ) ? projection : null;
	}
}
