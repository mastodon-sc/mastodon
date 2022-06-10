package org.mastodon.mamut;

import java.util.HashMap;
import java.util.Map;

import org.mastodon.graph.GraphChangeListener;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.algorithm.traversal.InverseDepthFirstIterator;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.views.trackscheme.HierarchyLayout;
import org.mastodon.views.trackscheme.TrackSchemeEdge;
import org.mastodon.views.trackscheme.TrackSchemeGraph;
import org.mastodon.views.trackscheme.TrackSchemeVertex;
import org.mastodon.views.trackscheme.display.PaintHierarchicalGraph;
import org.mastodon.views.trackscheme.display.PaintDecorations;
import org.mastodon.views.trackscheme.display.TrackSchemeFrame;
import org.mastodon.views.trackscheme.display.TrackSchemeOptions;
import org.mastodon.views.trackscheme.display.TrackSchemeOverlay;
import org.mastodon.views.trackscheme.display.TrackSchemeOverlay.TrackSchemeOverlayFactory;
import org.mastodon.views.trackscheme.wrap.DefaultModelGraphProperties;
import org.mastodon.views.trackscheme.wrap.ModelGraphProperties;

public class MamutBranchViewTrackSchemeHierarchy extends MamutBranchViewTrackScheme
{

	private static final int MINIMUM_NUMBER_OF_HIERARCHY_LEVELS_SCROLLABLE = 30;

	public MamutBranchViewTrackSchemeHierarchy( final MamutAppModel appModel )
	{
		this( appModel, new HashMap<>() );
	}

	public MamutBranchViewTrackSchemeHierarchy( final MamutAppModel appModel, final Map< String, Object > guiState )
	{
		super( appModel, guiState, new BranchHierarchyTrackSchemeFactory(), new HierarchyTrackSchemeOverlayFactory() );

		// Window title.
		final TrackSchemeFrame frame = getFrame();
		frame.setTitle( "TrackScheme Hierarchy" );

		// Remove timepoint listener.
		timepointModel.listeners().removeAll();

		// Min & max levels.
		final GraphChangeListener gcl = () -> {
			int minT = Integer.MAX_VALUE;
			int maxT = Integer.MIN_VALUE;
			for ( final TrackSchemeVertex v : viewGraph.vertices() )
			{
				final int t = v.getTimepoint();
				if ( t > maxT )
					maxT = t;
				if ( t < minT )
					minT = t;
			}
			maxT = Math.max( MINIMUM_NUMBER_OF_HIERARCHY_LEVELS_SCROLLABLE, maxT );
			frame.getTrackschemePanel().setTimepointRange( minT, maxT );
			frame.getTrackschemePanel().graphChanged();
		};
		viewGraph.graphChangeListeners().add( gcl );
		gcl.graphChanged();
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
					new TrackSchemeGraph<>( graph, idmap, properties );
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
				int level = 0;
				while ( it.hasNext() && it.next().incomingEdges().size() > 0 )
					level++;
				return level;
			}
		}
	}

	public static class HierarchyTrackSchemeOverlayFactory extends TrackSchemeOverlayFactory
	{
		@Override
		public TrackSchemeOverlay create(
				final TrackSchemeGraph< ?, ? > graph,
				final HighlightModel< TrackSchemeVertex, TrackSchemeEdge > highlight,
				final FocusModel< TrackSchemeVertex, TrackSchemeEdge > focus,
				final TrackSchemeOptions options )
		{
			options.lineageTreeLayoutFactory( HierarchyLayout::new );
			return new TrackSchemeOverlay( graph, highlight, focus, new PaintDecorations(), new PaintHierarchicalGraph(), options );
		}
	}

}
