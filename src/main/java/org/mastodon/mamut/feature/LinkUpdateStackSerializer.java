package org.mastodon.mamut.feature;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.update.UpdateStackSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.mamut.feature.LinkUpdateStack.Spec;
import org.mastodon.revised.model.mamut.Link;
import org.scijava.plugin.Plugin;

@Plugin( type = LinkUpdateStackSerializer.class )
public class LinkUpdateStackSerializer extends UpdateStackSerializer< LinkUpdateStack, Link >
{

	@Override
	public Spec getFeatureSpec()
	{
		return LinkUpdateStack.SPEC;
	}

	@Override
	public LinkUpdateStack deserialize( final FileIdToObjectMap< Link > idmap, final RefCollection< Link > pool, final ObjectInputStream ois ) throws IOException, ClassNotFoundException
	{
		return new LinkUpdateStack( pool, deserializeStack( idmap, pool, ois ) );
	}
}
