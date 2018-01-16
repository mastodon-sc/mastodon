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

@Plugin( type = LinkFeatureComputer.class, name = "Link velocity" )
public class LinkVelocityFeatureComputer implements LinkFeatureComputer< DoublePropertyMap< Link > >
{

	public static final String KEY = "Link velocity";

	@Override
	public Set< String > getDependencies()
	{
		return Collections.singleton( LinkDisplacementComputer.KEY );
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

		@SuppressWarnings( "unchecked" )
		final Feature< Link, DoublePropertyMap< Link > > displacementFeature =
				( Feature< Link, DoublePropertyMap< Link > > ) model.getFeatureModel().getFeature( LinkDisplacementComputer.KEY );
		final DoublePropertyMap< Link > displacement = displacementFeature.getPropertyMap();

		for ( final Link link : graph.edges() )
		{
			if ( displacement.isSet( link ) )
			{
				final double disp = displacement.getDouble( link );
				final Spot source = link.getSource( ref1 );
				final Spot target = link.getTarget( ref2 );
				final double dt = Math.abs( source.getTimepoint() - target.getTimepoint() );
				pm.set( link, disp / dt );
			}
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
