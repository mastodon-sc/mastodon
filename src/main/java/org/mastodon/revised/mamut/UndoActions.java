package org.mastodon.revised.mamut;

import org.mastodon.revised.model.mamut.Model;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.InputActionBindings;

public class UndoActions
{
	public static final String UNDO = "undo";
	public static final String REDO = "redo";

	static final String[] UNDO_KEYS = new String[] { "meta Z", "ctrl Z" };
	static final String[] REDO_KEYS = new String[] { "meta shift Z", "ctrl shift Z" };

	/**
	 * Create Undo/Redo actions and install them in the specified
	 * {@link InputActionBindings}.
	 *
	 * @param actions
	 *            Actions are added here
	 * @param model
	 *            Actions are targeted at this {@link Model}s {@code undo()} and
	 *            {@code redo()} methods.
	 */
	public static void install(
			final Actions actions,
			final Model model )
	{
		actions.runnableAction( model::undo, UNDO, UNDO_KEYS );
		actions.runnableAction( model::redo, REDO, REDO_KEYS );
	}
}
