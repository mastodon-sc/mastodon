package org.mastodon.revised.mamut.feature;

import java.util.Collections;
import java.util.Set;

import org.mastodon.pool.PoolCollectionWrapper;
import org.mastodon.properties.IntPropertyMap;
import org.mastodon.revised.model.feature.IntScalarFeature;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

@Plugin( type = SpotFeatureComputer.class, name = "Track N spots" )
public class TrackSizeFeatureComputer extends SpotIntScalarFeatureComputer implements SpotFeatureComputer
{

	public static final String KEY = "Track N spots";

	private static final String INFO_STRING = "Returns the number of spots in a track.";

	public TrackSizeFeatureComputer()
	{
		super( KEY );
	}

	@Override
	public Set< String > getDependencies()
	{
		return Collections.singleton( SpotTrackIDFeatureComputer.KEY );
	}

	@Override
	public IntScalarFeature< Spot > compute( final Model model )
	{
		@SuppressWarnings( "unchecked" )
		final IntScalarFeature< Spot > trackID = ( IntScalarFeature< Spot > ) model.getFeatureModel().getFeature( SpotTrackIDFeatureComputer.KEY );
		final TIntIntMap nSpots = new TIntIntHashMap();
		final PoolCollectionWrapper< Spot > vertices = model.getGraph().vertices();
		for ( final Spot spot : vertices )
		{
			final int id = trackID.getValue( spot );
			if ( !nSpots.increment( id ) )
				nSpots.put( id, 1 );
		}

		final IntPropertyMap< Spot > trackNSpots = new IntPropertyMap<>( vertices, 0 );
		for ( final Spot spot : vertices )
			trackNSpots.set( spot, nSpots.get( trackID.getValue( spot ) ) );

		return new IntScalarFeature<>( KEY, Spot.class, trackNSpots );
	}

	@Override
	public String getHelpString()
	{
		return INFO_STRING;
	}

}
