package org.mastodon.mamut.importer.trackmate;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.mamut.model.Spot;
import org.scijava.plugin.Plugin;

@Plugin( type = FeatureSerializer.class )
public class TrackMateImportedSpotFeaturesSerializer extends TrackMateImportedFeaturesSerializer< TrackMateImportedSpotFeatures, Spot >
{

	@Override
	public FeatureSpec< TrackMateImportedSpotFeatures, Spot > getFeatureSpec()
	{
		return new TrackMateImportedSpotFeatures.Spec();
	}

	@Override
	public TrackMateImportedSpotFeatures deserialize( final FileIdToObjectMap< Spot > idmap, final RefCollection< Spot > pool, final ObjectInputStream ois ) throws IOException, ClassNotFoundException
	{
		final TrackMateImportedSpotFeatures feature = new TrackMateImportedSpotFeatures();
		deserializeInto( feature, idmap, pool, ois );
		return feature;
	}
}
