package org.mastodon.mamut.feature;

import org.mastodon.feature.Dimension;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutFeatureComputer.class )
public class LinkDisplacementFeatureComputer implements MamutFeatureComputer
{

	@Parameter
	private Model model;

	@Parameter( type = ItemIO.OUTPUT )
	private LinkDisplacementFeature output;

	@Override
	public void createOutput()
	{
		if ( null == output )
		{
			final String units = Dimension.LENGTH.getUnits( model.getSpaceUnits(), model.getTimeUnits() );
			output = new LinkDisplacementFeature( new DoublePropertyMap<>( model.getGraph().edges().getRefPool(), Double.NaN ), units );
		}
	}

	@Override
	public void run()
	{
		output.map.beforeClearPool();

		final ModelGraph graph = model.getGraph();
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
			output.map.set( link, Math.sqrt( d2 ) );
		}

		graph.releaseRef( ref1 );
		graph.releaseRef( ref2 );
	}
}
