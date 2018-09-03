package org.mastodon.mamut.feature;

import org.mastodon.collection.RefDoubleMap;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.util.FeatureProjections;
import org.mastodon.revised.model.mamut.Link;
import org.scijava.plugin.Plugin;

public class LinkVelocityFeature implements Feature< Link >
{

	private static final String KEY = "Link velocity";

	final RefDoubleMap< Link > map;

	private FeatureProjection< Link > projection;

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< LinkVelocityFeature, Link >
	{
		public Spec()
		{
			super( KEY, LinkVelocityFeature.class, Link.class, KEY );
		}
	}

	LinkVelocityFeature( final RefDoubleMap< Link > map )
	{
		this.map = map;
		this.projection = FeatureProjections.project( map );
	}

	@Override
	public FeatureProjection< Link > project( final String projectionKey )
	{
		return projection;
	}
}
