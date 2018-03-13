package org.mastodon.revised.mamut;

import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.ui.keymap.CommandDescriptions;
import org.scijava.ui.behaviour.util.Actions;

public class UndoActions
{
	public static final String UNDO = "undo";
	public static final String REDO = "redo";

	static final String[] UNDO_KEYS = new String[] { "meta Z", "ctrl Z" };
	static final String[] REDO_KEYS = new String[] { "meta shift Z", "ctrl shift Z" };

	/*
	 * Command descriptions for all provided commands
	 */
	public static void getCommandDescriptions( final CommandDescriptions descriptions )
	{
		descriptions.add( UNDO, UNDO_KEYS, "Undo last edit." );
		descriptions.add( REDO, REDO_KEYS, "Redo last undone edit." );
	}

	/**
	 * Create Undo/Redo actions and install them in the specified
	 * {@link Actions}.
	 *
	 * @param actions
	 *            Actions are added here.
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
