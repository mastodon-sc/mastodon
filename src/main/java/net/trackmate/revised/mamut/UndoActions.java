package net.trackmate.revised.mamut;

import javax.swing.ActionMap;
import javax.swing.InputMap;

import org.scijava.ui.behaviour.KeyStrokeAdder;

import bdv.util.AbstractActions;
import bdv.viewer.InputActionBindings;
import net.trackmate.revised.bdv.BigDataViewerMaMuT;
import net.trackmate.revised.model.mamut.Model;

public class UndoActions extends AbstractActions
{
	public static final String UNDO = "undo";
	public static final String REDO = "redo";

	static final String[] UNDO_KEYS = new String[] { "meta Z", "ctrl Z" };
	static final String[] REDO_KEYS = new String[] { "meta shift Z", "ctrl shift Z" };

	/**
	 * Create BigDataViewer actions and install them in the specified
	 * {@link InputActionBindings}.
	 *
	 * @param inputActionBindings
	 *            {@link InputMap} and {@link ActionMap} are installed here.
	 * @param bdv
	 *            Actions are targeted at this {@link BigDataViewerMaMuT}.
	 * @param keyProperties
	 *            user-defined key-bindings.
	 */
	public static void installActionBindings(
			final InputActionBindings inputActionBindings,
			final Model model,
			final KeyStrokeAdder.Factory keyProperties )
	{
		final UndoActions actions = new UndoActions( inputActionBindings, keyProperties );
		actions.runnableAction( model::undo, UNDO, UNDO_KEYS );
		actions.runnableAction( model::redo, REDO, REDO_KEYS );
	}

	public UndoActions(
			final InputActionBindings inputActionBindings,
			final KeyStrokeAdder.Factory keyConfig )
	{
		super( inputActionBindings, "undo", keyConfig, new String[] { "ts", "bdv" } );
	}
}
