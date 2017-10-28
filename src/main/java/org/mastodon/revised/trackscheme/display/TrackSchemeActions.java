package org.mastodon.revised.trackscheme.display;

import javax.swing.ActionMap;
import javax.swing.InputMap;

import org.scijava.ui.behaviour.KeyStrokeAdder;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.InputActionBindings;

public class TrackSchemeActions extends Actions
{
	public static final String EDIT_FOCUS = "ts edit focused vertex label";

	public static final String[] EDIT_FOCUS_KEYS = new String[] { "ENTER" };

	/**
	 * Creates TrackScheme actions and install them in the specified
	 * {@link InputActionBindings}.
	 *
	 * @param inputActionBindings
	 *            {@link InputMap} and {@link ActionMap} are installed here.
	 * @param frame
	 *            the TrackScheme frame in which TrackScheme is displayed.
	 * @param keyConfig
	 *            the key-config.
	 */
	public static void installActionBindings(
			final InputActionBindings inputActionBindings,
			final TrackSchemeFrame frame,
			final KeyStrokeAdder.Factory keyConfig )
	{
		final TrackSchemeActions actions = new TrackSchemeActions( keyConfig );

		actions.runnableAction( frame.getEditFocusVertex(), EDIT_FOCUS, EDIT_FOCUS_KEYS );

		actions.install( inputActionBindings, "ts" );
	}

	public TrackSchemeActions( final KeyStrokeAdder.Factory keyConfig )
	{
		super( keyConfig, new String[] { "ts" } );
	}
}
