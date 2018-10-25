package org.mastodon.mamut.feature;

import org.mastodon.properties.IntPropertyMap;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = SpotNLinksFeatureComputer.class )
public class SpotNLinksFeatureComputer implements MamutFeatureComputer
{

	@Parameter
	private ModelGraph graph;

	@Parameter( type = ItemIO.OUTPUT )
	private SpotNLinksFeature output;

	@Override
	public void run()
	{
		output.map.beforeClearPool();
		for ( final Spot spot : graph.vertices() )
			output.map.set( spot, spot.edges().size() );
	}

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new SpotNLinksFeature( new IntPropertyMap<>( graph.vertices().getRefPool(), -1 ) );
	}
}
