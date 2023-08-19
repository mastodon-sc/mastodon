package org.mastodon.mamut.views.bdv;

import java.util.Map;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.views.AbstractMamutViewFactory;
import org.mastodon.mamut.views.MamutViewFactory;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutViewFactory.class )
public class MamutBranchViewBdvFactory extends AbstractMamutViewFactory< MamutBranchViewBdv >
{

	@Override
	public MamutBranchViewBdv create( final ProjectModel projectModel )
	{
		return new MamutBranchViewBdv( projectModel );
	}

	@Override
	public Map< String, Object > getGuiState( final MamutBranchViewBdv view )
	{
		final Map< String, Object > guiState = super.getGuiState( view );
		MamutViewBdvFactory.getBdvGuiState( view.getViewerPanelMamut(), guiState );
		return guiState;
	}

	@Override
	public void restoreGuiState( final MamutBranchViewBdv view, final Map< String, Object > guiState )
	{
		super.restoreGuiState( view, guiState );
		MamutViewBdvFactory.restoreBdvGuiState( view.getViewerPanelMamut(), guiState );
	}
}
