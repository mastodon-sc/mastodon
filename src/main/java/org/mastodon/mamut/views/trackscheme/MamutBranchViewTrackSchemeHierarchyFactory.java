package org.mastodon.mamut.views.trackscheme;

import java.util.Map;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.views.AbstractMamutViewFactory;
import org.mastodon.mamut.views.MamutViewFactory;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutViewFactory.class, priority = Priority.NORMAL - 6 )
public class MamutBranchViewTrackSchemeHierarchyFactory extends AbstractMamutViewFactory< MamutBranchViewTrackSchemeHierarchy >
{

	public static final String NEW_HIERARCHY_TRACKSCHEME_VIEW = "new hierarchy trackscheme view";

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

	@Override
	public String getCommandName()
	{
		return NEW_HIERARCHY_TRACKSCHEME_VIEW;
	}

	@Override
	public String getCommandDescription()
	{
		return "Open a new hierarchy TrackScheme view.";
	}

	@Override
	public String getCommandMenuText()
	{
		return "New TrackScheme Hierarchy";
	}

	@Override
	public Class< MamutBranchViewTrackSchemeHierarchy > getViewClass()
	{
		return MamutBranchViewTrackSchemeHierarchy.class;
	}
}
