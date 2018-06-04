package org.mastodon.revised.mamut.feature;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.model.feature.DoubleScalarFeature;
import org.mastodon.revised.model.feature.FeatureUtil;
import org.mastodon.revised.model.feature.FeatureUtil.Dimension;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

@Plugin( type = LinkFeatureComputer.class, name = "Link velocity" )
public class LinkVelocityFeatureComputer extends LinkDoubleScalarFeatureComputer
{

	public static final String KEY = "Link velocity";
	private static final String HELP_STRING = "Computes the link velocity as the distance between "
			+ "the source and target spots divided by their frame difference. Units are in physical distance per frame.";

	public LinkVelocityFeatureComputer()
	{
		super( KEY );
	}

	@Override
	public Collection< String > getProjectionKeys()
	{
		return Collections.singleton( KEY );
	}

	@Override
	public Set< String > getDependencies()
	{
		return Collections.singleton( LinkDisplacementComputer.KEY );
	}

	@Override
	public DoubleScalarFeature< Link > compute( final Model model )
	{
		final ModelGraph graph = model.getGraph();
		final DoublePropertyMap< Link > pm = new DoublePropertyMap<>( graph.edges(), Double.NaN );

		final Spot ref1 = graph.vertexRef();
		final Spot ref2 = graph.vertexRef();

		@SuppressWarnings( "unchecked" )
		final DoubleScalarFeature< Link > displacement =
				( DoubleScalarFeature< Link > ) model.getFeatureModel().getFeature( LinkDisplacementComputer.KEY );

		for ( final Link link : graph.edges() )
		{
			if ( displacement.isSet( link ) )
			{
				final double disp = displacement.getValue( link );
				final Spot source = link.getSource( ref1 );
				final Spot target = link.getTarget( ref2 );
				final double dt = Math.abs( source.getTimepoint() - target.getTimepoint() );
				pm.set( link, disp / dt );
			}
		}

		graph.releaseRef( ref1 );
		graph.releaseRef( ref2 );
		return new DoubleScalarFeature<>( KEY, Link.class, pm, FeatureUtil.dimensionToUnits( Dimension.VELOCITY, spaceUnits, timeUnits ) );
	}

	@Override
	public String getHelpString()
	{
		return HELP_STRING;
	}
}
