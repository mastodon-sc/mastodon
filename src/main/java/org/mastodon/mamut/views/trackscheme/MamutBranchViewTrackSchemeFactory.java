package org.mastodon.mamut.views.trackscheme;

import java.util.Map;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.views.AbstractMamutViewFactory;
import org.mastodon.mamut.views.MamutViewFactory;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutViewFactory.class, priority = Priority.NORMAL - 5 )
public class MamutBranchViewTrackSchemeFactory extends AbstractMamutViewFactory< MamutBranchViewTrackScheme >
{

	public static final String NEW_BRANCH_TRACKSCHEME_VIEW = "new branch trackscheme view";

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

	@Override
	public String getCommandName()
	{
		return NEW_BRANCH_TRACKSCHEME_VIEW;
	}

	@Override
	public String getCommandDescription()
	{
		return "Open a new branch TrackScheme view.";
	}

	@Override
	public String getCommandMenuText()
	{
		return "New TrackScheme Branch";
	}

	@Override
	public Class< MamutBranchViewTrackScheme > getViewClass()
	{
		return MamutBranchViewTrackScheme.class;
	}
}
