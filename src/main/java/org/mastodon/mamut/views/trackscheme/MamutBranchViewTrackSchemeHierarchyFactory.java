package org.mastodon.mamut.views.trackscheme;

import java.util.Map;

import org.mastodon.mamut.MamutBranchViewTrackSchemeHierarchy;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.views.AbstractMamutViewFactory;

public class MamutBranchViewTrackSchemeHierarchyFactory extends AbstractMamutViewFactory< MamutBranchViewTrackSchemeHierarchy >
{

	@Override
	public MamutBranchViewTrackSchemeHierarchy create( final ProjectModel projectModel )
	{
		return new MamutBranchViewTrackSchemeHierarchy( projectModel );
	}

	@Override
	public Map< String, Object > getGuiState( final MamutBranchViewTrackSchemeHierarchy view )
	{
		final Map< String, Object > guiState = super.getGuiState( view );
		MamutViewTrackSchemeFactory.storeTrackSchemeTransform( view.getFrame().getTrackschemePanel(), guiState );
		return guiState;
	}

	@Override
	public void restoreGuiState( final MamutBranchViewTrackSchemeHierarchy view, final Map< String, Object > guiState )
	{
		super.restoreGuiState( view, guiState );
		MamutViewTrackSchemeFactory.restoreTrackSchemeTransform( view.getFrame().getTrackschemePanel(), guiState );
	}
}
