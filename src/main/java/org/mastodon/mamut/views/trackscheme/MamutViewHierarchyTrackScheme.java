package org.mastodon.mamut.views.trackscheme;

import org.mastodon.graph.algorithm.util.Graphs;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.model.MastodonModel;
import org.mastodon.views.trackscheme.MastodonViewTrackScheme;
import org.mastodon.views.trackscheme.BranchTrackScheme.HierarchyTrackSchemeOverlayFactory;
import org.mastodon.views.trackscheme.graph.LongEdgesLineageTreeLayout;
import org.mastodon.views.trackscheme.properties.MastodonHierarchyTrackSchemeProperties;

/**
 * Mamut app-specific TrackScheme view.
 *
 * @author Jean-Yves Tinevez
 *
 */
public class MamutViewHierarchyTrackScheme extends MastodonViewTrackScheme<
		MastodonModel< ModelBranchGraph, BranchSpot, BranchLink >,
		ModelBranchGraph,
		BranchSpot,
		BranchLink >
{

	private static final int MINIMUM_NUMBER_OF_HIERARCHY_LEVELS_SCROLLABLE = 30;

	public MamutViewHierarchyTrackScheme( final MamutAppModel appModel )
	{
		super(
				appModel.dataModel().branchModel(),
				appModel.windowManager(),
				new MastodonHierarchyTrackSchemeProperties<>( appModel.dataModel().branchModel().getGraph() ),
				new HierarchyTrackSchemeOverlayFactory(),
				LongEdgesLineageTreeLayout::new,
				0,
				Math.max( Graphs.maxDepth( appModel.dataModel().getGraph() ), MINIMUM_NUMBER_OF_HIERARCHY_LEVELS_SCROLLABLE ) );

		// Adjust title.
		final String title = getFrame().getTitle().replace( "TrackScheme", "TrackScheme Hierarchy" );
		getFrame().setTitle( title );
	}
}
