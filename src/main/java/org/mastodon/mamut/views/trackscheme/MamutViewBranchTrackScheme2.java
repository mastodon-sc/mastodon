package org.mastodon.mamut.views.trackscheme;

import org.mastodon.app.views.trackscheme.MastodonViewTrackScheme2;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.mamut.views.trackscheme.MamutBranchViewTrackScheme.BranchTrackSchemeOverlayFactory;
import org.mastodon.model.MastodonModel;
import org.mastodon.views.trackscheme.LongEdgesLineageTreeLayout;

/**
 * Mamut app-specific TrackScheme view.
 *
 * @author Jean-Yves Tinevez
 *
 */
public class MamutViewBranchTrackScheme2 extends MastodonViewTrackScheme2<
		MastodonModel< ModelBranchGraph, BranchSpot, BranchLink >,
		ModelBranchGraph,
		BranchSpot,
		BranchLink >
{

	public MamutViewBranchTrackScheme2( final MamutAppModel appModel )
	{
		super(
				appModel.dataModel().branchModel(),
				appModel.uiModel(),
				new MamutBranchTrackSchemeProperties( appModel.dataModel().branchModel().getGraph() ),
				new BranchTrackSchemeOverlayFactory(),
				LongEdgesLineageTreeLayout::new,
				appModel.getTimepointMin(),
				appModel.getTimepointMax() );
	}
}
