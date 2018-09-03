package org.mastodon.mamut.feature;

import org.mastodon.collection.ref.RefDoubleHashMap;
import org.mastodon.feature.FeatureComputer;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = LinkDisplacementComputer.class )
public class LinkDisplacementComputer implements FeatureComputer
{

	public static final String KEY = "Link displacement";

	@Parameter
	private ModelGraph graph;

	@Parameter( type = ItemIO.OUTPUT )
	private LinkDisplacementFeature output;

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new LinkDisplacementFeature( new RefDoubleHashMap<>( graph.edges().getRefPool(), Double.NaN ) );
	}

	@Override
	public void run()
	{
		output.map.clear(); // TODO Update map instead.

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
			output.map.put( link, Math.sqrt( d2 ) );
		}

		graph.releaseRef( ref1 );
		graph.releaseRef( ref2 );
	}
}
