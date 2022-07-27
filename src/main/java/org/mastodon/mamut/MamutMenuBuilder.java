/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ActionMap;

import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.ui.SelectionActions;
import org.mastodon.views.bdv.export.RecordMaxProjectionMovieDialog;
import org.mastodon.views.bdv.export.RecordMovieDialog;
import org.mastodon.views.table.TableViewActions;
import org.mastodon.views.trackscheme.display.EditFocusVertexLabelAction;
import org.mastodon.views.trackscheme.display.ShowSelectedTracksActions;
import org.mastodon.views.trackscheme.display.TrackSchemeNavigationActions;

import bdv.BigDataViewerActions;

public class MamutMenuBuilder extends ViewMenuBuilder
{
	static Map< String, String > menuTexts = new HashMap<>();

	static
	{
		menuTexts.put( ProjectManager.CREATE_PROJECT, "New Project" );
		menuTexts.put( ProjectManager.CREATE_PROJECT_FROM_URL, "New Project From URL" );
		menuTexts.put( ProjectManager.LOAD_PROJECT, "Load Project" );
		menuTexts.put( ProjectManager.SAVE_PROJECT, "Save Project" );
		menuTexts.put( ProjectManager.SAVE_PROJECT_AS, "Save Project As..." );
		menuTexts.put( ProjectManager.IMPORT_TGMM, "Import TGMM tracks" );
		menuTexts.put( ProjectManager.IMPORT_SIMI, "Import Simi BioCell tracks" );
		menuTexts.put( ProjectManager.IMPORT_MAMUT, "Import MaMuT project" );
		menuTexts.put( ProjectManager.EXPORT_MAMUT, "Export MaMuT project" );

		menuTexts.put( WindowManager.OPEN_ONLINE_DOCUMENTATION, "Open online documentation" );

		menuTexts.put( WindowManager.NEW_BDV_VIEW, "New Bdv" );
		menuTexts.put( WindowManager.NEW_TRACKSCHEME_VIEW, "New TrackScheme" );
		menuTexts.put( WindowManager.NEW_TABLE_VIEW, "New Data table" );
		menuTexts.put( WindowManager.NEW_SELECTION_TABLE_VIEW, "New Selection table" );
		menuTexts.put( WindowManager.NEW_GRAPHER_VIEW, "New Grapher" );
		menuTexts.put( WindowManager.NEW_BRANCH_BDV_VIEW, "New Bdv Branch" );
		menuTexts.put( WindowManager.NEW_BRANCH_TRACKSCHEME_VIEW, "New TrackScheme Branch" );
		menuTexts.put( WindowManager.NEW_HIERARCHY_TRACKSCHEME_VIEW, "New TrackScheme Hierarchy" );
		menuTexts.put( WindowManager.PREFERENCES_DIALOG, "Preferences..." );

		menuTexts.put( MastodonFrameViewActions.TOGGLE_SETTINGS_PANEL, "Settings Toolbar" );

		menuTexts.put( UndoActions.UNDO, "Undo" );
		menuTexts.put( UndoActions.REDO, "Redo" );

		menuTexts.put( ShowSelectedTracksActions.SHOW_TRACK_DOWNWARD, "Show Track Downward" );
		menuTexts.put( ShowSelectedTracksActions.SHOW_SELECTED_TRACKS, "Show Only Selected Tracks" );
		menuTexts.put( ShowSelectedTracksActions.SHOW_ALL_TRACKS, "Show All Tracks" );

		menuTexts.put( SelectionActions.DELETE_SELECTION, "Delete Selection" );
		menuTexts.put( SelectionActions.SELECT_WHOLE_TRACK, "Select Whole Track" );
		menuTexts.put( SelectionActions.SELECT_TRACK_DOWNWARD, "Select Track Downward" );
		menuTexts.put( SelectionActions.SELECT_TRACK_UPWARD, "Select Track Upward" );

		menuTexts.put( BigDataViewerActions.LOAD_SETTINGS, "Load Bdv Settings" );
		menuTexts.put( BigDataViewerActions.SAVE_SETTINGS, "Save Bdv Settings" );
		menuTexts.put( BigDataViewerActions.BRIGHTNESS_SETTINGS, "Brightness & Color" );
		menuTexts.put( BigDataViewerActions.VISIBILITY_AND_GROUPING, "Visibility & Grouping" );

		menuTexts.put( RecordMovieDialog.RECORD_MOVIE_DIALOG, "Record BDV movie" );
		menuTexts.put( RecordMaxProjectionMovieDialog.RECORD_MIP_MOVIE_DIALOG, "Record BDV max projection movie" );

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

		menuTexts.put( TableViewActions.EDIT_LABEL, "Edit Vertex Label" );
		menuTexts.put( TableViewActions.TOGGLE_TAG, "Toggle Current Tag" );
		menuTexts.put( TableViewActions.EXPORT_TO_CSV, "Export to CSV" );
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

	public static MenuItem colorMenu( final JMenuHandle handle )
	{
		return ViewMenuBuilder.menu( "Coloring", handle );
	}

	public static MenuItem branchColorMenu( final JMenuHandle handle )
	{
		return ViewMenuBuilder.menu( "Branch coloring", handle );
	}

	public static MenuItem tagSetMenu( final JMenuHandle handle )
	{
		return ViewMenuBuilder.menu( "Tags", handle );
	}

	public static MenuItem colorbarMenu( final JMenuHandle handle )
	{
		return ViewMenuBuilder.menu( "Colorbar", handle );
	}

	public static MenuItem editMenu( final MenuItem... items )
	{
		return ViewMenuBuilder.menu( "Edit", items );
	}

	public static MenuItem windowMenu( final MenuItem... items )
	{
		return ViewMenuBuilder.menu( "Window", items );
	}

	/**
	 * Creates a menu item with the specified item name, under the specified
	 * menu path. For instance
	 * <code>makeFullMenuItem( "my action", "Plugins", "Submenu" );</code> will
	 * create a menu item for <code>my action</code> under the menu path
	 * <code>Plugins &gt; Submenu</code> and create the said path if required.
	 * 
	 * @param itemName
	 *            the name of the item.
	 * @param menuPath
	 *            the ordered menu path.
	 * @return a new menu item.
	 */
	public static final MenuItem makeFullMenuItem( final String itemName, final String... menuPath )
	{
		final MenuItem item = MamutMenuBuilder.item( itemName );
		MenuItem menuPathItem = item;
		for ( int i = menuPath.length - 1; i >= 0; i-- )
			menuPathItem = MamutMenuBuilder.menu( menuPath[ i ], menuPathItem );
		return menuPathItem;
	}
}
