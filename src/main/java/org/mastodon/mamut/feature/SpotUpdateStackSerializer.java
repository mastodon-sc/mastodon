package org.mastodon.mamut.feature;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.feature.update.UpdateStackSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.mamut.feature.SpotUpdateStack.Spec;
import org.mastodon.mamut.model.Spot;
import org.scijava.plugin.Plugin;

@Plugin( type = FeatureSerializer.class )
public class SpotUpdateStackSerializer extends UpdateStackSerializer< SpotUpdateStack, Spot >
{

	@Override
	public Spec getFeatureSpec()
	{
		return SpotUpdateStack.SPEC;
	}

	@Override
	public SpotUpdateStack deserialize( final FileIdToObjectMap< Spot > idmap, final RefCollection< Spot > pool, final ObjectInputStream ois ) throws IOException, ClassNotFoundException
	{
		return new SpotUpdateStack( pool, deserializeStack( idmap, pool, ois ) );
	}
}
