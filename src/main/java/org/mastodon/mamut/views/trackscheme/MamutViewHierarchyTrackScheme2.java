package org.mastodon.mamut.views.trackscheme;

import org.mastodon.app.views.trackscheme.BranchTrackScheme.HierarchyTrackSchemeOverlayFactory;
import org.mastodon.app.views.trackscheme.MastodonViewTrackScheme2;
import org.mastodon.graph.algorithm.util.Graphs;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.model.MastodonModel;
import org.mastodon.views.trackscheme.graph.LongEdgesLineageTreeLayout;
import org.mastodon.views.trackscheme.wrap.MastodonHierarchyTrackSchemeProperties;

/**
 * Mamut app-specific TrackScheme view.
 *
 * @author Jean-Yves Tinevez
 *
 */
public class MamutViewHierarchyTrackScheme2 extends MastodonViewTrackScheme2<
		MastodonModel< ModelBranchGraph, BranchSpot, BranchLink >,
		ModelBranchGraph,
		BranchSpot,
		BranchLink >
{

	private static final int MINIMUM_NUMBER_OF_HIERARCHY_LEVELS_SCROLLABLE = 30;

	public MamutViewHierarchyTrackScheme2( final MamutAppModel appModel )
	{
		super(
				appModel.dataModel().branchModel(),
				appModel.uiModel(),
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
