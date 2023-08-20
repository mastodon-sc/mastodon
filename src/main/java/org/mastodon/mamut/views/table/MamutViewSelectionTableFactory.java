package org.mastodon.mamut.views.table;

import java.util.Map;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.views.AbstractMamutViewFactory;
import org.mastodon.mamut.views.MamutViewFactory;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutViewFactory.class, priority = Priority.NORMAL - 3 )
public class MamutViewSelectionTableFactory extends AbstractMamutViewFactory< MamutViewSelectionTable >
{

	public static final String NEW_SELECTION_TABLE_VIEW = "new selection table view";

	@Override
	public MamutViewSelectionTable create( final ProjectModel projectModel )
	{
		return new MamutViewSelectionTable( projectModel );
	}

	@Override
	public Map< String, Object > getGuiState( final MamutViewSelectionTable view )
	{
		final Map< String, Object > guiState = super.getGuiState( view );
		MamutViewTableFactory.getGuiStateTable( view, guiState );
		return guiState;
	}

	@Override
	public void restoreGuiState( final MamutViewSelectionTable view, final Map< String, Object > guiState )
	{
		super.restoreGuiState( view, guiState );
		MamutViewTableFactory.restoreGuiStateTable( view, guiState );
	}

	@Override
	public String getCommandName()
	{
		return NEW_SELECTION_TABLE_VIEW;
	}

	@Override
	public String getCommandMenuText()
	{
		return "New Selection Table view";
	}

	@Override
	public String getCommandDescription()
	{
		return "Open a new selection table view. "
				+ "The table only displays the current selection and "
				+ "is updated as the selection changes.";
	}

	@Override
	public Class< MamutViewSelectionTable > getViewClass()
	{
		return MamutViewSelectionTable.class;
	}
}
