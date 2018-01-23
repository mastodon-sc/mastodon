package org.mastodon.revised.mamut;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ActionMap;

import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.revised.bdv.BigDataViewerActionsMamut;
import org.mastodon.revised.trackscheme.display.EditFocusVertexLabelAction;
import org.mastodon.revised.trackscheme.display.TrackSchemeNavigationActions;
import org.mastodon.revised.ui.SelectionActions;

public class MamutMenuBuilder extends ViewMenuBuilder
{
	static Map< String, String > menuTexts = new HashMap<>();

	static
	{
		menuTexts.put( ProjectManager.CREATE_PROJECT, "New Project" );
		menuTexts.put( ProjectManager.LOAD_PROJECT, "Load Project" );
		menuTexts.put( ProjectManager.SAVE_PROJECT, "Save Project" );
		menuTexts.put( ProjectManager.IMPORT_TGMM, "Import TGMM tracks" );

		menuTexts.put( WindowManager.NEW_BDV_VIEW, "New Bdv" );
		menuTexts.put( WindowManager.NEW_TRACKSCHEME_VIEW, "New Trackscheme" );
		menuTexts.put( WindowManager.PREFERENCES_DIALOG, "Preferences..." );

		menuTexts.put( MastodonFrameViewActions.TOGGLE_SETTINGS_PANEL, "Settings Toolbar" );

		menuTexts.put( UndoActions.UNDO, "Undo" );
		menuTexts.put( UndoActions.REDO, "Redo" );

		menuTexts.put( SelectionActions.DELETE_SELECTION, "Delete Selection" );
		menuTexts.put( SelectionActions.SELECT_WHOLE_TRACK, "Select Whole Track" );
		menuTexts.put( SelectionActions.SELECT_TRACK_DOWNWARD, "Select Track Downward" );
		menuTexts.put( SelectionActions.SELECT_TRACK_UPWARD, "Select Track Upward" );

		menuTexts.put( BigDataViewerActionsMamut.LOAD_SETTINGS, "Load Bdv Settings" );
		menuTexts.put( BigDataViewerActionsMamut.SAVE_SETTINGS, "Save Bdv Settings" );
		menuTexts.put( BigDataViewerActionsMamut.BRIGHTNESS_SETTINGS, "Brightness & Color" );
		menuTexts.put( BigDataViewerActionsMamut.VISIBILITY_AND_GROUPING, "Visibility & Grouping" );

		menuTexts.put( TrackSchemeNavigationActions.NAVIGATE_CHILD, "Navigate to Child" );
		menuTexts.put( TrackSchemeNavigationActions.NAVIGATE_PARENT, "Navigate to Parent" );
		menuTexts.put( TrackSchemeNavigationActions.NAVIGATE_LEFT, "Navigate to Left" );
		menuTexts.put( TrackSchemeNavigationActions.NAVIGATE_RIGHT, "Navigate to Right" );
		menuTexts.put( TrackSchemeNavigationActions.SELECT_NAVIGATE_CHILD, "Select to Child" );
		menuTexts.put( TrackSchemeNavigationActions.SELECT_NAVIGATE_PARENT, "Select to Parent" );
		menuTexts.put( TrackSchemeNavigationActions.SELECT_NAVIGATE_LEFT, "Select to Left" );
		menuTexts.put( TrackSchemeNavigationActions.SELECT_NAVIGATE_RIGHT, "Select to Right" );
		menuTexts.put( TrackSchemeNavigationActions.TOGGLE_FOCUS_SELECTION, "Toggle Focused Vertex Selection" );

		menuTexts.put( EditFocusVertexLabelAction.EDIT_FOCUS_LABEL, "Edit Vertex Label" );
	}

	public static void build( final ViewMenu viewMenu, final ActionMap actionMap, final MenuItem... items )
	{
		final MenuItemVisitor visitor = new MenuItemVisitor( viewMenu, actionMap, menuTexts, null );
		Arrays.asList( items ).forEach( item -> item.accept( visitor ) );
	}

	public static MenuItem fileMenu( final MenuItem... items )
	{
		return ViewMenuBuilder.menu( "File", items );
	}

	public static MenuItem viewMenu( final MenuItem... items )
	{
		return ViewMenuBuilder.menu( "View", items );
	}

	public static MenuItem editMenu( final MenuItem... items )
	{
		return ViewMenuBuilder.menu( "Edit", items );
	}

	public static MenuItem windowMenu( final MenuItem... items )
	{
		return ViewMenuBuilder.menu( "Window", items );
	}
}
