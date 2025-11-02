package org.mastodon.mamut.views.grapher;

import org.mastodon.app.views.grapher.MastodonViewGrapher;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.model.MastodonModel;

/**
 * Mamut app-specific Grapher view for the branch data.
 *
 * @author Jean-Yves Tinevez
 */
public class MamutViewBranchGrapher extends MastodonViewGrapher< 
			MastodonModel< ModelBranchGraph, BranchSpot, BranchLink >, 
			ModelBranchGraph, 
			BranchSpot, 
			BranchLink >
{

	public MamutViewBranchGrapher( final MamutAppModel appModel )
	{
		super(
				appModel.dataModel().branchModel(),
				appModel.uiModel(),
				new MamutDataBranchGraphProperties( appModel.dataModel().getGraph(), appModel.imageData().getSources().size() ) );
	}
}
