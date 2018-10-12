package org.mastodon.mamut.feature;

import org.mastodon.collection.ref.RefDoubleHashMap;
import org.mastodon.feature.Dimension;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = LinkVelocityFeatureComputer.class )
public class LinkVelocityFeatureComputer implements MamutFeatureComputer
{

	@Parameter
	private LinkDisplacementFeature displacement;

	@Parameter
	private Model model;

	@Parameter( type = ItemIO.OUTPUT )
	private LinkVelocityFeature output;

	@Override
	public void createOutput()
	{
		if ( null == output )
		{
			final String units = Dimension.VELOCITY.getUnits( model.getSpaceUnits(), model.getTimeUnits() );
			output = new LinkVelocityFeature( new RefDoubleHashMap<>( model.getGraph().edges().getRefPool(), Double.NaN ), units );
		}
	}

	@Override
	public void run()
	{
		final ModelGraph graph = model.getGraph();
		final Spot ref1 = graph.vertexRef();
		final Spot ref2 = graph.vertexRef();

		for ( final Link link : graph.edges() )
		{
			if ( displacement.map.containsKey( link ) )
			{
				final double disp = displacement.map.get( link );
				final Spot source = link.getSource( ref1 );
				final Spot target = link.getTarget( ref2 );
				final double dt = Math.abs( source.getTimepoint() - target.getTimepoint() );
				output.map.put( link, disp / dt );
			}
		}

		graph.releaseRef( ref1 );
		graph.releaseRef( ref2 );
	}
}
