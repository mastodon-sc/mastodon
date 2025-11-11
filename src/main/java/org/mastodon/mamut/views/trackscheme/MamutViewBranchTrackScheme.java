package org.mastodon.mamut.views.trackscheme;

import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.model.MastodonModel;
import org.mastodon.views.trackscheme.MastodonViewTrackScheme;
import org.mastodon.views.trackscheme.BranchTrackScheme.BranchTrackSchemeOverlayFactory;
import org.mastodon.views.trackscheme.graph.LongEdgesLineageTreeLayout;
import org.mastodon.views.trackscheme.properties.MastodonBranchTrackSchemeProperties;

/**
 * Mamut app-specific TrackScheme view.
 *
 * @author Jean-Yves Tinevez
 *
 */
public class MamutViewBranchTrackScheme extends MastodonViewTrackScheme<
		MastodonModel< ModelBranchGraph, BranchSpot, BranchLink >,
		ModelBranchGraph,
		BranchSpot,
		BranchLink >
{

	public MamutViewBranchTrackScheme( final MamutAppModel appModel )
	{
		super(
				appModel.dataModel().branchModel(),
				appModel.windowManager(),
				new MastodonBranchTrackSchemeProperties<>(),
				new BranchTrackSchemeOverlayFactory(),
				LongEdgesLineageTreeLayout::new,
				appModel.getTimepointMin(),
				appModel.getTimepointMax() );

		// Adjust title.
		final String title = getFrame().getTitle().replace( "TrackScheme", "TrackScheme Branch" );
		getFrame().setTitle( title );
	}
}
