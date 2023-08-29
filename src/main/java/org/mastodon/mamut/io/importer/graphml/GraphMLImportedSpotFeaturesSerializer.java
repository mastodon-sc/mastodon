package org.mastodon.mamut.io.importer.graphml;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.mamut.io.importer.trackmate.TrackMateImportedFeaturesSerializer;
import org.mastodon.mamut.model.Spot;
import org.scijava.plugin.Plugin;

@Plugin( type = FeatureSerializer.class )
public class GraphMLImportedSpotFeaturesSerializer
		extends TrackMateImportedFeaturesSerializer< GraphMLImportedSpotFeatures, Spot >
{

	@Override
	public FeatureSpec< GraphMLImportedSpotFeatures, Spot > getFeatureSpec()
	{
		return new GraphMLImportedSpotFeatures.Spec();
	}

	@Override
	public GraphMLImportedSpotFeatures deserialize( final FileIdToObjectMap< Spot > idmap,
			final RefCollection< Spot > pool, final ObjectInputStream ois ) throws IOException, ClassNotFoundException
	{
		final GraphMLImportedSpotFeatures feature = new GraphMLImportedSpotFeatures();
		deserializeInto( feature, idmap, pool, ois );
		return feature;
	}
}
