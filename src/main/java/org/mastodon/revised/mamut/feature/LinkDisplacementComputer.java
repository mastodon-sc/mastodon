package org.mastodon.revised.mamut.feature;

import java.util.Collections;
import java.util.Set;

import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureSerializer;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

@Plugin( type = LinkFeatureComputer.class, name = "Link displacement" )
public class LinkDisplacementComputer implements LinkFeatureComputer< DoublePropertyMap< Link > >
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

		return MamutFeatureSerializers.bundle( KEY, pm, Link.class );
	}

	@Override
	public FeatureSerializer< Link, DoublePropertyMap< Link >, Model > getSerializer()
	{
		return MamutFeatureSerializers.doubleLinkSerializer( KEY );
	}

}
