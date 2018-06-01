package org.mastodon.revised.mamut.feature;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.pool.PoolCollectionWrapper;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.model.feature.DoubleScalarFeature;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;

public abstract class LinkDoubleScalarFeatureComputer extends  LinkFeatureComputer
{

	public LinkDoubleScalarFeatureComputer( final String key )
	{
		super( key );
	}

	@Override
	public DoubleScalarFeature< Link > deserialize( final File file, final Model model, final FileIdToGraphMap< ?, ? > fileIdToGraphMap ) throws IOException
	{
		try (final ObjectInputStream ois = new ObjectInputStream(
				new BufferedInputStream(
						new FileInputStream( file ), 1024 * 1024 ) ))
		{
			// UNITS.
			final String units = ois.readUTF();
			// CONTENT.
			final PoolCollectionWrapper< Link > edges = model.getGraph().edges();
			final DoublePropertyMap< Link > pm = new DoublePropertyMap<>( edges, Double.NaN, edges.size() );
			final DoublePropertyMapSerializer< Link > serializer = new DoublePropertyMapSerializer<>( pm );
			@SuppressWarnings( "unchecked" )
			final FileIdToObjectMap< Link > idToLinkMap = ( FileIdToObjectMap< Link > ) fileIdToGraphMap.edges();
			serializer.readPropertyMap( idToLinkMap, ois );
			return new DoubleScalarFeature<>( getKey(), Link.class, pm, units );
		}
		catch ( final ClassNotFoundException e )
		{
			e.printStackTrace();
		}
		return null;
	}
}
