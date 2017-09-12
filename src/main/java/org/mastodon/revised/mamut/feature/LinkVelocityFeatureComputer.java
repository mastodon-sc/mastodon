package org.mastodon.revised.mamut.feature;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureProjection;
import org.mastodon.revised.model.feature.FeatureProjectors;
import org.mastodon.revised.model.feature.FeatureTarget;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

@Plugin( type = LinkFeatureComputer.class, name = "Link velocity" )
public class LinkVelocityFeatureComputer implements LinkFeatureComputer
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
	public Feature< Link, Double, DoublePropertyMap< Link > > compute( final Model model )
	{
		final ModelGraph graph = model.getGraph();
		final DoublePropertyMap< Link > pm = new DoublePropertyMap<>( graph.edges(), Double.NaN );

		final Spot ref1 = graph.vertexRef();
		final Spot ref2 = graph.vertexRef();

		@SuppressWarnings( "unchecked" )
		final Feature< Link, Double, DoublePropertyMap< Link > > displacementFeature =
				( Feature< Link, Double, DoublePropertyMap< Link > > ) model.getGraphFeatureModel().getFeature( LinkDisplacementComputer.KEY );
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

		final Map< String, FeatureProjection< Link > > projections = Collections.singletonMap( KEY, FeatureProjectors.project( pm ) );
		final Feature< Link, Double, DoublePropertyMap< Link > > feature = new Feature<>( KEY, FeatureTarget.EDGE, pm, projections );
		return feature;
	}
}