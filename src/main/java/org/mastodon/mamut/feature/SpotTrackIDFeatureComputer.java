package org.mastodon.mamut.feature;

import org.mastodon.graph.algorithm.traversal.BreadthFirstCrossComponentSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch.SearchDirection;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutFeatureComputer.class )
public class SpotTrackIDFeatureComputer implements MamutFeatureComputer
{

	@Parameter
	private ModelGraph graph;

	@Parameter( type = ItemIO.OUTPUT )
	private SpotTrackIDFeature output;

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new SpotTrackIDFeature( new IntPropertyMap<>( graph.vertices().getRefPool(), -1 ) );
	}

	@Override
	public void run()
	{
		output.map.beforeClearPool();

		if ( graph.vertices().isEmpty() )
			return;

		final BreadthFirstCrossComponentSearch< Spot, Link > search = new BreadthFirstCrossComponentSearch<>( graph, SearchDirection.UNDIRECTED );
		final SearchListener< Spot, Link, BreadthFirstCrossComponentSearch< Spot, Link > > l =
				new SearchListener< Spot, Link, BreadthFirstCrossComponentSearch< Spot, Link > >()
				{

					private int id = 0;

					@Override
					public void processVertexLate( final Spot spot, final BreadthFirstCrossComponentSearch< Spot, Link > search )
					{}

					@Override
					public void processVertexEarly( final Spot spot, final BreadthFirstCrossComponentSearch< Spot, Link > search )
					{
						output.map.set( spot, id );
					}

					@Override
					public void processEdge( final Link link, final Spot source, final Spot target, final BreadthFirstCrossComponentSearch< Spot, Link > search )
					{}

					@Override
					public void crossComponent( final Spot endSpot, final Spot startSpot, final BreadthFirstCrossComponentSearch< Spot, Link > search )
					{
						id++;
					}
				};
		search.setTraversalListener( l );
		search.start( graph.vertices().iterator().next() );
	}
}
