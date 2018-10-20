package org.mastodon.mamut.feature;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.model.mamut.Link;
import org.scijava.plugin.Plugin;

public class LinkDisplacementFeature implements Feature< Link >
{

	private static final String KEY = "Link displacement";

	private static final String HELP_STRING = "Computes the link displacement in physical units "
			+ "as the distance between the source spot and the target spot.";

	final DoublePropertyMap< Link > map;

	private final FeatureProjection< Link > projection;

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< LinkDisplacementFeature, Link >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					LinkDisplacementFeature.class,
					Link.class,
					FeatureProjectionSpec.standard( KEY, Dimension.LENGTH ) );
		}
	}

	LinkDisplacementFeature( final DoublePropertyMap< Link > map, final String units )
	{
		this.map = map;
		this.projection = FeatureProjections.project( map, units );
	}

	@Override
	public FeatureProjection< Link > project( final String projectionKey )
	{
		return projection;
	}

	@Override
	public String[] projectionKeys()
	{
		return new String[] { KEY };
	}
}
