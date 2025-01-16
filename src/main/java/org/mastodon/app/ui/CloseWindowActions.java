/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.app.ui;

import static org.mastodon.app.ui.MastodonFrameViewActions.CLOSE_WINDOW;
import static org.mastodon.app.ui.MastodonFrameViewActions.CLOSE_WINDOW_KEYS;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.KeyConfigScopes;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
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
			super( KeyConfigScopes.MASTODON, KeyConfigContexts.MASTODON );
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
