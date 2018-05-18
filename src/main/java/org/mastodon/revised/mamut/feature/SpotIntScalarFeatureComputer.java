package org.mastodon.revised.mamut.feature;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.properties.IntPropertyMapSerializer;
import org.mastodon.pool.PoolCollectionWrapper;
import org.mastodon.properties.IntPropertyMap;
import org.mastodon.revised.model.feature.IntScalarFeature;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;

public abstract class SpotIntScalarFeatureComputer implements SpotFeatureComputer
{
	/**
	 * The key of the feature this computer generates.
	 */
	protected final String key;

	public SpotIntScalarFeatureComputer( final String key )
	{
		this.key = key;
	}

	@Override
	public String getKey()
	{
		return key;
	}

	@Override
	public IntScalarFeature< Spot > deserialize( final File file, final Model model, final FileIdToGraphMap< ?, ? > fileIdToGraphMap ) throws IOException
	{
		try (final ObjectInputStream ois = new ObjectInputStream(
				new BufferedInputStream(
						new FileInputStream( file ), 1024 * 1024 ) ))
		{
			final PoolCollectionWrapper< Spot > vertices = model.getGraph().vertices();
			final IntPropertyMap< Spot > pm = new IntPropertyMap<>( vertices, Integer.MIN_VALUE, vertices.size() );
			final IntPropertyMapSerializer< Spot > serializer = new IntPropertyMapSerializer<>( pm );
			@SuppressWarnings( "unchecked" )
			final FileIdToObjectMap< Spot > idToLinkMap = ( FileIdToObjectMap< Spot > ) fileIdToGraphMap.edges();
			serializer.readPropertyMap( idToLinkMap, ois );
			return new IntScalarFeature<>( key, Spot.class, pm );
		}
		catch ( final ClassNotFoundException e )
		{
			e.printStackTrace();
		}
		return null;
	}
}
