package org.mastodon.feature;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.IntPropertyMapSerializer;
import org.mastodon.properties.IntPropertyMap;

public abstract class IntScalarFeatureSerializer< F extends IntScalarFeature< O >, O > implements FeatureSerializer< F, O >
{

	@Override
	public void serialize( final F feature, final ObjectToFileIdMap< O > idmap, final ObjectOutputStream oos ) throws IOException
	{
		final FeatureSpec< ? extends Feature< O >, O > spec = feature.getSpec();
		final FeatureProjectionSpec projectionSpec = spec.getProjectionSpecs().iterator().next();

		final String key = spec.getKey();
		final String info = spec.getInfo();
		final Dimension dimension = projectionSpec.projectionDimension;
		final String units = feature.projections().iterator().next().units();

		oos.writeUTF( key );
		oos.writeUTF( info );
		oos.writeObject( dimension );
		oos.writeUTF( units );

		final IntPropertyMap< O > map = feature.values;
		final IntPropertyMapSerializer< O > mapSerializer = new IntPropertyMapSerializer<>( map );
		mapSerializer.writePropertyMap( idmap, oos );
	}

	protected DeserializedStruct read( final FileIdToObjectMap< O > idmap, final RefCollection< O > pool, final ObjectInputStream ois ) throws IOException, ClassNotFoundException
	{
		final String key = ois.readUTF();
		final String info = ois.readUTF();
		final Dimension dimension = ( Dimension ) ois.readObject();
		final String units = ois.readUTF();

		final IntPropertyMap< O > map = new IntPropertyMap<>( pool, Integer.MIN_VALUE );
		final IntPropertyMapSerializer< O > mapSerializer = new IntPropertyMapSerializer<>( map );
		mapSerializer.readPropertyMap( idmap, ois );

		return new DeserializedStruct( key, info, dimension, units, map );
	}

	protected class DeserializedStruct
	{

		public final String key;

		public final String info;

		public final Dimension dimension;

		public final String units;

		public final IntPropertyMap< O > map;

		private DeserializedStruct( final String key, final String info, final Dimension dimension, final String units, final IntPropertyMap< O > map )
		{
			this.key = key;
			this.info = info;
			this.dimension = dimension;
			this.units = units;
			this.map = map;
		}
	}
}
