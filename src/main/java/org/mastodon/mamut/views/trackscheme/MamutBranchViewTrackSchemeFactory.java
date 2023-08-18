package org.mastodon.mamut.views.trackscheme;

import java.util.Map;

import org.mastodon.mamut.MamutBranchViewTrackScheme;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.views.AbstractMamutViewFactory;

public class MamutBranchViewTrackSchemeFactory extends AbstractMamutViewFactory< MamutBranchViewTrackScheme >
{

	@Override
	public MamutBranchViewTrackScheme create( final ProjectModel projectModel )
	{
		return new MamutBranchViewTrackScheme( projectModel );
	}

	@Override
	public Map< String, Object > getGuiState( final MamutBranchViewTrackScheme view )
	{
		final Map< String, Object > guiState = super.getGuiState( view );
		MamutViewTrackSchemeFactory.storeTrackSchemeTransform( view.getFrame().getTrackschemePanel(), guiState );
		return guiState;
	}

	@Override
	public void restoreGuiState( final MamutBranchViewTrackScheme view, final Map< String, Object > guiState )
	{
		super.restoreGuiState( view, guiState );
		MamutViewTrackSchemeFactory.restoreTrackSchemeTransform( view.getFrame().getTrackschemePanel(), guiState );
	}
}
