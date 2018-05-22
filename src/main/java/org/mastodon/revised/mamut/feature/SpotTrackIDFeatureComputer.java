package org.mastodon.revised.mamut.feature;

import java.util.Collections;
import java.util.Set;

import org.mastodon.graph.algorithm.traversal.BreadthFirstCrossComponentSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch.SearchDirection;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.properties.IntPropertyMap;
import org.mastodon.revised.model.feature.IntScalarFeature;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

@Plugin( type = SpotFeatureComputer.class, name = "Spot track ID" )
public class SpotTrackIDFeatureComputer extends SpotIntScalarFeatureComputer
{

	public static final String KEY = "Spot track ID";

	private static final String HELP_STRING = "Returns the ID of the track each spot belongs to.";

	public SpotTrackIDFeatureComputer()
	{
		super( KEY );
	}

	@Override
	public Set< String > getDependencies()
	{
		return Collections.emptySet();
	}

	@Override
	public IntScalarFeature< Spot > compute( final Model model )
	{
		final ModelGraph graph = model.getGraph();
		final BreadthFirstCrossComponentSearch< Spot, Link > search = new BreadthFirstCrossComponentSearch<>( graph, SearchDirection.UNDIRECTED );
		final IntPropertyMap< Spot > trackID = new IntPropertyMap<>( graph.vertices(), -1 );
		final SearchListener< Spot, Link, BreadthFirstCrossComponentSearch< Spot, Link > > l = new SearchListener< Spot, Link, BreadthFirstCrossComponentSearch< Spot, Link > >()
		{

			private int id = 0;

			@Override
			public void processVertexLate( final Spot spot, final BreadthFirstCrossComponentSearch< Spot, Link > search )
			{}

			@Override
			public void processVertexEarly( final Spot spot, final BreadthFirstCrossComponentSearch< Spot, Link > search )
			{
				trackID.set( spot, id );
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
		return new IntScalarFeature<>( KEY, Spot.class, trackID );
	}

	@Override
	public String getHelpString()
	{
		return HELP_STRING;
	}

}
