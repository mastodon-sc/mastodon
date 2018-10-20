package org.mastodon.mamut.feature;

import org.mastodon.pool.PoolCollectionWrapper;
import org.mastodon.properties.IntPropertyMap;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

@Plugin( type = TrackSizeFeatureComputer.class )
public class TrackSizeFeatureComputer implements MamutFeatureComputer
{

	@Parameter
	private ModelGraph graph;

	@Parameter
	private SpotTrackIDFeature trackID;

	@Parameter( type = ItemIO.OUTPUT )
	private TrackSizeFeature output;

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new TrackSizeFeature( new IntPropertyMap<>( graph.vertices().getRefPool(), -1 ) );
	}

	@Override
	public void run()
	{
		output.map.clear();

		final TIntIntMap nSpots = new TIntIntHashMap();
		final PoolCollectionWrapper< Spot > vertices = graph.vertices();
		for ( final Spot spot : vertices )
		{
			final int id = trackID.map.get( spot );
			if ( !nSpots.increment( id ) )
				nSpots.put( id, 1 );
		}

		for ( final Spot spot : vertices )
			output.map.set( spot, nSpots.get( trackID.map.get( spot ) ) );
	}
}
