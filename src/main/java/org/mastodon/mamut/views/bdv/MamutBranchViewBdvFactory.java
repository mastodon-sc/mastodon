package org.mastodon.mamut.views.bdv;

import java.util.Map;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.views.AbstractMamutViewFactory;
import org.mastodon.mamut.views.MamutViewFactory;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutViewFactory.class, priority = Priority.NORMAL - 7 )
public class MamutBranchViewBdvFactory extends AbstractMamutViewFactory< MamutBranchViewBdv >
{

	public static final String NEW_BRANCH_BDV_VIEW = "new branch bdv view";

	public static final String[] NEW_BRANCH_BDV_VIEW_KEYS = new String[] { "not mapped" };

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

	@Override
	public String getCommandName()
	{
		return NEW_BRANCH_BDV_VIEW;
	}

	@Override
	public String[] getCommandKeys()
	{
		return NEW_BRANCH_BDV_VIEW_KEYS;
	}

	@Override
	public String getCommandDescription()
	{
		return "Open a new branch BigDataViewer view.";
	}

	@Override
	public String getCommandMenuText()
	{
		return "New Bdv Branch";
	}

	@Override
	public Class< MamutBranchViewBdv > getViewClass()
	{
		return MamutBranchViewBdv.class;
	}
}
