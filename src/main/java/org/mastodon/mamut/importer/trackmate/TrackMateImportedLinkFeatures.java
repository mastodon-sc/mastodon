package org.mastodon.mamut.importer.trackmate;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.Link;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.plugin.Plugin;

public class TrackMateImportedLinkFeatures extends TrackMateImportedFeatures< Link >
{

	public static final String KEY = "TrackMate Link features";

	private static final String HELP_STRING =
			"Stores the link feature values imported from a TrackMate or MaMuT file.";

	private final Spec spec = new Spec();

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< TrackMateImportedLinkFeatures, Link >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					TrackMateImportedLinkFeatures.class,
					Link.class,
					Multiplicity.SINGLE );
		}
	}

	@Override
	void store( final String key, final Dimension dimension, final String units, final DoublePropertyMap< Link > values )
	{
		super.store( key, dimension, units, values );
		spec.getProjectionSpecs().add( new FeatureProjectionSpec( key, dimension ) );
	}

	@Override
	void store( final String key, final Dimension dimension, final String units, final IntPropertyMap< Link > values )
	{
		super.store( key, dimension, units, values );
		spec.getProjectionSpecs().add( new FeatureProjectionSpec( key, dimension ) );
	}

	@Override
	public FeatureSpec< ? extends Feature< Link >, Link > getSpec()
	{
		return spec;
	}
}
