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
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;

public abstract class SpotDoubleScalarFeatureComputer extends SpotFeatureComputer
{
	public SpotDoubleScalarFeatureComputer( final String key )
	{
		super( key );
	}

	@Override
	public DoubleScalarFeature< Spot > deserialize( final File file, final Model model, final FileIdToGraphMap< ?, ? > fileIdToGraphMap ) throws IOException
	{
		try (final ObjectInputStream ois = new ObjectInputStream(
				new BufferedInputStream(
						new FileInputStream( file ), 1024 * 1024 ) ))
		{
			// UNITS.
			final String units = ois.readUTF();
			// CONTENT.
			final PoolCollectionWrapper< Spot > vertices = model.getGraph().vertices();
			final DoublePropertyMap< Spot > pm = new DoublePropertyMap<>( vertices, Double.NaN, vertices.size() );
			final DoublePropertyMapSerializer< Spot > serializer = new DoublePropertyMapSerializer<>( pm );
			@SuppressWarnings( "unchecked" )
			final FileIdToObjectMap< Spot > idToLinkMap = ( FileIdToObjectMap< Spot > ) fileIdToGraphMap.edges();
			serializer.readPropertyMap( idToLinkMap, ois );
			return new DoubleScalarFeature<>( getKey(), Spot.class, pm, units );
		}
		catch ( final ClassNotFoundException e )
		{
			e.printStackTrace();
		}
		return null;
	}
}
