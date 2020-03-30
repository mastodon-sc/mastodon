package org.mastodon.revised.model.mamut.trackmate;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.revised.model.mamut.Link;
import org.scijava.plugin.Plugin;

@Plugin( type = FeatureSerializer.class )
public class TrackMateImportedLinkFeaturesSerializer extends TrackMateImportedFeaturesSerializer< TrackMateImportedLinkFeatures, Link >
{

	@Override
	public FeatureSpec< TrackMateImportedLinkFeatures, Link > getFeatureSpec()
	{
		return new TrackMateImportedLinkFeatures.Spec();
	}

	@Override
	public TrackMateImportedLinkFeatures deserialize( final FileIdToObjectMap< Link > idmap, final RefCollection< Link > pool, final ObjectInputStream ois ) throws IOException, ClassNotFoundException
	{
		final TrackMateImportedLinkFeatures feature = new TrackMateImportedLinkFeatures();
		deserializeInto( feature, idmap, pool, ois );
		return feature;
	}
}
