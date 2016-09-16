package org.mastodon.revised.trackscheme.display;

import javax.swing.ActionMap;
import javax.swing.InputMap;

import org.scijava.ui.behaviour.KeyStrokeAdder;

import bdv.util.AbstractActions;
import bdv.viewer.InputActionBindings;

public class TrackSchemeActions extends AbstractActions
{
	public static final String EDIT_FOCUS = "ts edit focused vertex label";
	public static final String TOGGLE_SETTINGS_PANEL = "ts toggle settings panel";
	public static final String SEARCH = "ts search";

	public static final String[] EDIT_FOCUS_KEYS = new String[] { "shift ENTER" };
	public static final String[] TOGGLE_SETTINGS_PANEL_KEYS = new String[] { "T" };
	public static final String[] SEARCH_KEYS = new String[] { "ctrl F", "meta F", "SLASH" };

	/**
	 * Create TrackScheme actions and install them in the specified
	 * {@link InputActionBindings}.
	 *
	 * @param inputActionBindings
	 *            {@link InputMap} and {@link ActionMap} are installed here.
	 * @param keyProperties
	 *            user-defined key-bindings.
	 */
	public static void installActionBindings(
			final InputActionBindings inputActionBindings,
			final TrackSchemeFrame frame,
			final KeyStrokeAdder.Factory keyProperties )
	{
		final TrackSchemeActions actions = new TrackSchemeActions( inputActionBindings, keyProperties );
		actions.runnableAction( frame.getEditFocusVertex(), EDIT_FOCUS, EDIT_FOCUS_KEYS );
		actions.runnableAction(
				() -> frame.setSettingsPanelVisible( !frame.isSettingsPanelVisible() ),
				TOGGLE_SETTINGS_PANEL, TOGGLE_SETTINGS_PANEL_KEYS );
		actions.runnableAction(
				() -> frame.searchField.requestFocusInWindow(),
				SEARCH, SEARCH_KEYS );
	}

	public TrackSchemeActions(
			final InputActionBindings inputActionBindings,
			final KeyStrokeAdder.Factory keyConfig )
	{
		this( inputActionBindings, keyConfig, "ts" );
	}

	public TrackSchemeActions(
			final InputActionBindings inputActionBindings,
			final KeyStrokeAdder.Factory keyConfig,
			final String name )
	{
		super( inputActionBindings, name, keyConfig, new String[] { "ts" } );
	}
}
