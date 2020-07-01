package org.mastodon.app.ui;

import static org.mastodon.app.ui.MastodonFrameViewActions.CLOSE_WINDOW;
import static org.mastodon.app.ui.MastodonFrameViewActions.CLOSE_WINDOW_KEYS;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;

public class CloseWindowActions
{
	public static final String CLOSE_DIALOG = "close dialog window";

	static final String[] CLOSE_DIALOG_KEYS = new String[] { "ctrl W", "meta W", "ESCAPE" };

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.MASTODON );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( CLOSE_DIALOG, CLOSE_DIALOG_KEYS, "Close active dialog." );
		}
	}

	/**
	 * Create a close window action and install it in the specified
	 * {@link Actions}.
	 *
	 * @param actions
	 *            Actions are added here.
	 * @param frame
	 *            Actions are targeted at this window.
	 */
	public static void install(
			final Actions actions,
			final JFrame frame )
	{
		actions.namedAction( new CloseWindowAction( frame, CLOSE_WINDOW ), CLOSE_WINDOW_KEYS );
	}

	/**
	 * Create a close dialog action and install it in the specified
	 * {@link Actions}.
	 *
	 * @param actions
	 *            Actions are added here.
	 * @param dialog
	 *            Actions are targeted at this dialog.
	 */
	public static void install(
			final Actions actions,
			final JDialog dialog )
	{
		actions.namedAction( new CloseWindowAction( dialog, CLOSE_DIALOG ), CLOSE_DIALOG_KEYS );
	}

	private static class CloseWindowAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = 1L;

		private final Window window;

		public CloseWindowAction( final Window window, final String name )
		{
			super( name );
			this.window = window;
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			/*
			 * To properly close the view, we send it a WINDOW_CLOSING event.
			 * This way, the listeners of the JFrame are called and the closing
			 * happens gracefully within Mastodon.
			 */
			window.dispatchEvent( new WindowEvent( window, WindowEvent.WINDOW_CLOSING ) );
		}
	}
}
