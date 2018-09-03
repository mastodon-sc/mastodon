package org.mastodon.mamut.feature;

import org.mastodon.collection.ref.RefIntHashMap;
import org.mastodon.feature.FeatureComputer;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = SpotNLinksComputer.class, name = "Spot N links" )
public class SpotNLinksComputer implements FeatureComputer
{

	@Parameter
	private ModelGraph graph;

	@Parameter( type = ItemIO.OUTPUT )
	private SpotNLinksFeature output;
	
	@Override
	public void run()
	{
		output.map.clear(); // TODO Update map instead.
		for ( final Spot spot : graph.vertices() )
			output.map.put( spot, spot.edges().size() );
	}

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new SpotNLinksFeature( new RefIntHashMap<>( graph.vertices().getRefPool(), -1 ) );
	}
}
