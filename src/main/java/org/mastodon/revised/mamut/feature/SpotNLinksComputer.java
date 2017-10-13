package org.mastodon.revised.mamut.feature;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.properties.IntPropertyMapSerializer;
import org.mastodon.pool.PoolCollectionWrapper;
import org.mastodon.properties.IntPropertyMap;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureProjection;
import org.mastodon.revised.model.feature.FeatureProjectors;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

@Plugin( type = SpotFeatureComputer.class, name = "Spot N links" )
public class SpotNLinksComputer implements SpotFeatureComputer
{

	private static final String KEY = "Spot N links";

	@Override
	public Set< String > getDependencies()
	{
		return Collections.emptySet();
	}

	@Override
	public String getKey()
	{
		return KEY;
	}

	@Override
	public Feature< Spot, IntPropertyMap< Spot > > compute( final Model model )
	{
		final PoolCollectionWrapper< Spot > vertices = model.getGraph().vertices();
		final IntPropertyMap< Spot > pm = new IntPropertyMap<>( vertices, -1 );

		for ( final Spot spot : vertices )
			pm.set( spot, spot.edges().size() );

		return bundle( pm );
	}

	@Override
	public Feature< ?, ? > deserialize( final ObjectInputStream ois, final FileIdToObjectMap< ? > fileIdToObjectMap, final Model model )
	{
		final IntPropertyMap< Spot > pm = new IntPropertyMap<>( model.getGraph().vertices(), -1 );
		final IntPropertyMapSerializer< Spot > serializer = new IntPropertyMapSerializer<>( pm );
		@SuppressWarnings( "unchecked" )
		final FileIdToObjectMap< Spot > idmap = ( FileIdToObjectMap< Spot > ) fileIdToObjectMap;
		try
		{
			serializer.readPropertyMap( idmap, ois );
		}
		catch ( ClassNotFoundException | IOException e1 )
		{
			e1.printStackTrace();
		}
		return bundle( pm );
	}

	private Feature< Spot, IntPropertyMap< Spot> > bundle( final IntPropertyMap< Spot > propertyMap )
	{
		final Map< String, FeatureProjection< Spot > > projections = Collections.singletonMap( KEY, FeatureProjectors.project( propertyMap ) );
		final Feature< Spot, IntPropertyMap< Spot > > feature = new Feature<>(
				KEY,
				Spot.class,
				propertyMap,
				projections,
				new IntPropertyMapSerializer<>( propertyMap ) );
		return feature;
	}
}
