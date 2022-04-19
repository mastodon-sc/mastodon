package org.mastodon.mamut;

import java.util.HashMap;
import java.util.Map;

import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.algorithm.traversal.InverseDepthFirstIterator;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.views.trackscheme.TrackSchemeGraph;
import org.mastodon.views.trackscheme.wrap.DefaultModelGraphProperties;
import org.mastodon.views.trackscheme.wrap.ModelGraphProperties;

public class MamutBranchViewTrackSchemeHierarchy extends MamutBranchViewTrackScheme
{

	public MamutBranchViewTrackSchemeHierarchy( final MamutAppModel appModel )
	{
		this( appModel, new HashMap<>() );
	}

	public MamutBranchViewTrackSchemeHierarchy( final MamutAppModel appModel, final Map< String, Object > guiState )
	{
		super( appModel, guiState, new BranchHierarchyTrackSchemeFactory() );
	}

	/**
	 * A {@link BranchTrackSchemeFactory} that returns a TrackScheme graph where
	 * the Y coordinates of nodes are taken from the time-point they belong to.
	 */
	public static class BranchHierarchyTrackSchemeFactory extends BranchTimeTrackSchemeFactory
	{

		@Override
		public TrackSchemeGraph< BranchSpot, BranchLink > createViewGraph( final MamutAppModel appModel )
		{
			final Model model = appModel.getModel();
			final ModelBranchGraph graph = model.getBranchGraph();
			final GraphIdBimap< BranchSpot, BranchLink > idmap = graph.getGraphIdBimap();
			final ModelGraphProperties< BranchSpot, BranchLink > properties = new MyModelGraphProperties( graph );
			final TrackSchemeGraph< BranchSpot, BranchLink > trackSchemeGraph =
					new TrackSchemeGraph< BranchSpot, BranchLink >( graph, idmap, properties );
			return trackSchemeGraph;
		}

		/*
		 * NOT THREAD SAFE! If issues arise when multithreading TS graph
		 * creation, they will be caused here. But for now, there is no
		 * concurrent creation of vertices or editing of vertex properties.
		 */
		private static class MyModelGraphProperties extends DefaultModelGraphProperties< BranchSpot, BranchLink >
		{

			private final InverseDepthFirstIterator< BranchSpot, BranchLink > it;

			public MyModelGraphProperties( final ModelBranchGraph graph )
			{
				this.it = new InverseDepthFirstIterator<>( graph );
			}

			@Override
			public int getTimepoint( final BranchSpot v )
			{
				it.reset( v );
				int level = -1;
				do
				{
					level++;
				}
				while ( it.hasNext() && it.next().incomingEdges().size() > 0 );
				return level;
			}
		}
	}
}
