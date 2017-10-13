package org.mastodon.revised.mamut.feature;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureProjection;
import org.mastodon.revised.model.feature.FeatureProjectors;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

@Plugin( type = LinkFeatureComputer.class, name = "Link displacement" )
public class LinkDisplacementComputer implements LinkFeatureComputer
{

	public static final String KEY = "Link displacement";

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
	public Feature< Link, DoublePropertyMap< Link > > compute( final Model model )
	{
		final ModelGraph graph = model.getGraph();
		final DoublePropertyMap< Link > pm = new DoublePropertyMap<>( graph.edges(), Double.NaN );

		final Spot ref1 = graph.vertexRef();
		final Spot ref2 = graph.vertexRef();

		for ( final Link link : graph.edges() )
		{
			final Spot source = link.getSource( ref1 );
			final Spot target = link.getTarget( ref2 );
			double d2 = 0.;
			for ( int d = 0; d < 3; d++ )
			{
				final double dx = source.getDoublePosition( d ) - target.getDoublePosition( d );
				d2 += dx * dx;
			}
			pm.set( link, Math.sqrt( d2 ) );
		}

		graph.releaseRef( ref1 );
		graph.releaseRef( ref2 );

		return bundle( pm );
	}

	@Override
	public Feature< ?, ? > deserialize( final ObjectInputStream ois, final FileIdToObjectMap< ? > fileIdToObjectMap, final Model model )
	{
		final DoublePropertyMap< Link > pm = new DoublePropertyMap<>( model.getGraph().edges(), Double.NaN );
		final DoublePropertyMapSerializer< Link > serializer = new DoublePropertyMapSerializer<>( pm );
		@SuppressWarnings( "unchecked" )
		final FileIdToObjectMap< Link > idmap = ( FileIdToObjectMap< Link > ) fileIdToObjectMap;
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

	private Feature< Link, DoublePropertyMap< Link > > bundle( final DoublePropertyMap< Link > propertyMap )
	{
		final Map< String, FeatureProjection< Link > > projections = Collections.singletonMap( KEY, FeatureProjectors.project( propertyMap ) );
		final Feature< Link, DoublePropertyMap< Link > > feature = new Feature<>(
				KEY,
				Link.class,
				propertyMap,
				projections,
				new DoublePropertyMapSerializer<>( propertyMap ) );
		return feature;
	}
}
