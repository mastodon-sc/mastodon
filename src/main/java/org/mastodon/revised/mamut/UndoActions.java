package org.mastodon.revised.mamut;

import javax.swing.ActionMap;
import javax.swing.InputMap;

import org.mastodon.revised.model.mamut.Model;
import org.scijava.ui.behaviour.KeyStrokeAdder;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.InputActionBindings;

public class UndoActions extends Actions
{
	public static final String UNDO = "undo";
	public static final String REDO = "redo";

	static final String[] UNDO_KEYS = new String[] { "meta Z", "ctrl Z" };
	static final String[] REDO_KEYS = new String[] { "meta shift Z", "ctrl shift Z" };

	/**
	 * Create Undo/Redo actions and install them in the specified
	 * {@link InputActionBindings}.
	 *
	 * @param inputActionBindings
	 *            {@link InputMap} and {@link ActionMap} are installed here.
	 * @param model
	 *            Actions are targeted at this {@link Model}s {@code undo()} and
	 *            {@code redo()} methods.
	 */
	public static void installActionBindings(
			final InputActionBindings inputActionBindings,
			final Model model,
			final KeyStrokeAdder.Factory keyConfig )
	{
		final UndoActions actions = new UndoActions( keyConfig );

		actions.runnableAction( model::undo, UNDO, UNDO_KEYS );
		actions.runnableAction( model::redo, REDO, REDO_KEYS );

		actions.install( inputActionBindings, "undo" );
	}

	public UndoActions( final KeyStrokeAdder.Factory keyConfig )
	{
		super( keyConfig, new String[] { "ts", "bdv" } );
	}
}
