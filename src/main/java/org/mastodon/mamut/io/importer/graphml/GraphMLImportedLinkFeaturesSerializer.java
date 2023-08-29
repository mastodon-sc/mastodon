package org.mastodon.mamut.io.importer.graphml;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.mamut.io.importer.trackmate.TrackMateImportedFeaturesSerializer;
import org.mastodon.mamut.model.Link;
import org.scijava.plugin.Plugin;

@Plugin( type = FeatureSerializer.class )
public class GraphMLImportedLinkFeaturesSerializer
		extends TrackMateImportedFeaturesSerializer< GraphMLImportedLinkFeatures, Link >
{

	@Override
	public FeatureSpec< GraphMLImportedLinkFeatures, Link > getFeatureSpec()
	{
		return new GraphMLImportedLinkFeatures.Spec();
	}

	@Override
	public GraphMLImportedLinkFeatures deserialize( final FileIdToObjectMap< Link > idmap,
			final RefCollection< Link > pool, final ObjectInputStream ois ) throws IOException, ClassNotFoundException
	{
		final GraphMLImportedLinkFeatures feature = new GraphMLImportedLinkFeatures();
		deserializeInto( feature, idmap, pool, ois );
		return feature;
	}
}
