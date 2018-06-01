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
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;

public abstract class LinkIntScalarFeatureComputer extends LinkFeatureComputer
{

	public LinkIntScalarFeatureComputer( final String key )
	{
		super( key );
	}

	@Override
	public IntScalarFeature< Link > deserialize( final File file, final Model model, final FileIdToGraphMap< ?, ? > fileIdToGraphMap ) throws IOException
	{
		try (final ObjectInputStream ois = new ObjectInputStream(
				new BufferedInputStream(
						new FileInputStream( file ), 1024 * 1024 ) ))
		{
			// UNITS.
			final String units = ois.readUTF();
			// CONTENT.
			final PoolCollectionWrapper< Link > edges = model.getGraph().edges();
			final IntPropertyMap< Link > pm = new IntPropertyMap<>( edges, Integer.MIN_VALUE, edges.size() );
			final IntPropertyMapSerializer< Link > serializer = new IntPropertyMapSerializer<>( pm );
			@SuppressWarnings( "unchecked" )
			final FileIdToObjectMap< Link > idToLinkMap = ( FileIdToObjectMap< Link > ) fileIdToGraphMap.edges();
			serializer.readPropertyMap( idToLinkMap, ois );
			return new IntScalarFeature<>( getKey(), Link.class, pm, units );
		}
		catch ( final ClassNotFoundException e )
		{
			e.printStackTrace();
		}
		return null;
	}

}
