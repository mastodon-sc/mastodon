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
import org.mastodon.mamut.feature.SpotRadiusFeature.Spec;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

@Plugin( type = FeatureSerializer.class )
public class SpotRadiusFeatureSerializer implements FeatureSerializer< SpotRadiusFeature, Spot >
{

	@Override
	public Spec getFeatureSpec()
	{
		return SpotRadiusFeature.SPEC;
	}

	@Override
	public void serialize( final SpotRadiusFeature feature, final ObjectToFileIdMap< Spot > idmap, final ObjectOutputStream oos ) throws IOException
	{
		// UNITS.
		final FeatureProjection< Spot > proj = feature.projections().iterator().next();
		oos.writeUTF( proj.units() );
		// DATA.
		final DoublePropertyMapSerializer< Spot > propertyMapSerializer = new DoublePropertyMapSerializer<>( feature.map );
		propertyMapSerializer.writePropertyMap( idmap, oos );
	}

	@Override
	public SpotRadiusFeature deserialize( final FileIdToObjectMap< Spot > idmap, final RefCollection< Spot > pool, final ObjectInputStream ois ) throws IOException, ClassNotFoundException
	{
		// UNITS.
		final String units = ois.readUTF();
		// DATA.
		final DoublePropertyMap< Spot > map = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMapSerializer< Spot > propertyMapSerializer = new DoublePropertyMapSerializer<>( map );
		propertyMapSerializer.readPropertyMap( idmap, ois );
		return new SpotRadiusFeature( map, units );
	}
}
