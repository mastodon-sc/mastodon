package org.mastodon.mamut.feature;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.mamut.feature.LinkDisplacementFeature.Spec;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.model.mamut.Link;
import org.scijava.plugin.Plugin;

@Plugin( type = LinkDisplacementFeatureSerializer.class )
public class LinkDisplacementFeatureSerializer implements FeatureSerializer< LinkDisplacementFeature, Link >
{

	@Override
	public Spec getFeatureSpec()
	{
		return LinkDisplacementFeature.SPEC;
	}

	@Override
	public void serialize( final LinkDisplacementFeature feature, final ObjectToFileIdMap< Link > idmap, final ObjectOutputStream oos ) throws IOException
	{
		// UNITS.
		final FeatureProjection< Link > proj = feature.projections().iterator().next();
		oos.writeUTF( proj.units() );
		// DATA.
		final DoublePropertyMapSerializer< Link > propertyMapSerializer = new DoublePropertyMapSerializer<>( feature.map );
		propertyMapSerializer.writePropertyMap( idmap, oos );
	}

	@Override
	public LinkDisplacementFeature deserialize( final FileIdToObjectMap< Link > idmap, final RefCollection< Link > pool, final ObjectInputStream ois ) throws IOException, ClassNotFoundException
	{
		// UNITS.
		final String units = ois.readUTF();
		// DATA.
		final DoublePropertyMap< Link > map = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMapSerializer< Link > propertyMapSerializer = new DoublePropertyMapSerializer<>( map );
		propertyMapSerializer.readPropertyMap( idmap, ois );
		return new LinkDisplacementFeature( map, units );
	}
}
