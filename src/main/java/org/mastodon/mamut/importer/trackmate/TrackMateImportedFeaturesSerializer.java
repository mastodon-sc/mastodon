package org.mastodon.mamut.importer.trackmate;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.io.properties.IntPropertyMapSerializer;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.properties.IntPropertyMap;

public abstract class TrackMateImportedFeaturesSerializer< F extends TrackMateImportedFeatures< O >, O > implements FeatureSerializer< F, O >
{

	@Override
	public void serialize( final F feature, final ObjectToFileIdMap< O > idmap, final ObjectOutputStream oos ) throws IOException
	{
		// WRITE N DOUBLE MAPS.
		oos.writeInt( feature.doublePropertyMapMap.keySet().size() );
		// WRITE EACH PROJECTION.
		for ( final FeatureProjectionKey key : feature.doublePropertyMapMap.keySet() )
		{
			// WRITE PROJECTION KEY.
			oos.writeUTF( key.getSpec().projectionName );
			oos.writeObject( key.getSpec().projectionDimension );
			// WRITE UNITS.
			oos.writeUTF( feature.project( key ).units() );
			// WRITE VALUES
			final DoublePropertyMap< O > map = feature.doublePropertyMapMap.get( key );
			new DoublePropertyMapSerializer<>( map ).writePropertyMap( idmap, oos );
		}

		// WRITE N INT MAPS.
		oos.writeInt( feature.intPropertyMapMap.keySet().size() );
		// WRITE EACH PROJECTION.
		for ( final FeatureProjectionKey key : feature.intPropertyMapMap.keySet() )
		{
			// WRITE PROJECTION KEY.
			oos.writeUTF( key.getSpec().projectionName );
			oos.writeObject( key.getSpec().projectionDimension );
			// WRITE UNITS.
			oos.writeUTF( feature.project( key ).units() );
			// WRITE VALUES
			final IntPropertyMap< O > map = feature.intPropertyMapMap.get( key );
			new IntPropertyMapSerializer<>( map ).writePropertyMap( idmap, oos );
		}
	}

	protected void deserializeInto( final F feature, final FileIdToObjectMap< O > idmap, final RefCollection< O > pool, final ObjectInputStream ois ) throws IOException, ClassNotFoundException
	{
		// READ N DOUBLE MAPS.
		final int nDoubleMaps = ois.readInt();
		// READ EACH MAP.
		for ( int i = 0; i < nDoubleMaps; i++ )
		{
			final String projectionName = ois.readUTF();
			final Dimension dimension = ( Dimension ) ois.readObject();
			// READ UNITS.
			final String units = ois.readUTF();
			// READ VALUES.
			final DoublePropertyMap< O > map = new DoublePropertyMap<>( pool, Double.NaN );
			new DoublePropertyMapSerializer<>( map ).readPropertyMap( idmap, ois );
			// Store.
			feature.store( projectionName, dimension, units, map );
		}

		// READ N DOUBLE MAPS.
		final int nIntMaps = ois.readInt();
		// READ EACH MAP.
		for ( int i = 0; i < nIntMaps; i++ )
		{
			final String projectionName = ois.readUTF();
			final Dimension dimension = ( Dimension ) ois.readObject();
			// READ UNITS.
			final String units = ois.readUTF();
			// READ VALUES.
			final IntPropertyMap< O > map = new IntPropertyMap<>( pool, Integer.MIN_VALUE );
			new IntPropertyMapSerializer<>( map ).readPropertyMap( idmap, ois );
			// Store.
			feature.store( projectionName, dimension, units, map );
		}
	}
}
