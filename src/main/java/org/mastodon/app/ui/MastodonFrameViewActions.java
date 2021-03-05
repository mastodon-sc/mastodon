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
package org.mastodon.app.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;

import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.KeyConfigScopes;
import org.mastodon.util.HasSelectedState;
import org.scijava.listeners.Listeners;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;

public class MastodonFrameViewActions
{
	public static final String TOGGLE_SETTINGS_PANEL = "toggle settings panel";
	public static final String CLOSE_WINDOW = "close window";

	static final String[] TOGGLE_SETTINGS_PANEL_KEYS = new String[] { "T" };
	static final String[] CLOSE_WINDOW_KEYS = new String[] { "ctrl W", "meta W" };

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MASTODON, KeyConfigContexts.BIGDATAVIEWER, KeyConfigContexts.TRACKSCHEME );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( TOGGLE_SETTINGS_PANEL, TOGGLE_SETTINGS_PANEL_KEYS, "Toggle the view settings toolbar of the active window." );
			descriptions.add( CLOSE_WINDOW, CLOSE_WINDOW_KEYS, "Close the active window." );
		}
	}

	private final MastodonFrameView< ?, ?, ?, ?, ?, ? > view;

	private final ToggleSettingsPanelAction toggleSettingsPanelAction;

	private final CloseWindowAction closeWindowAction;

	/**
	 * Create Mastodon view actions and install them in the specified
	 * {@link Actions}.
	 *
	 * @param actions
	 *            Actions are added here.
	 * @param view
	 *            Actions are targeted at this view.
	 */
	public static void install(
			final Actions actions,
			final MastodonFrameView< ?, ?, ?, ?, ?, ? > view )
	{
		final MastodonFrameViewActions ba = new MastodonFrameViewActions( view );

		actions.namedAction( ba.toggleSettingsPanelAction, TOGGLE_SETTINGS_PANEL_KEYS );
		actions.namedAction( ba.closeWindowAction, CLOSE_WINDOW_KEYS );
	}

	private MastodonFrameViewActions( final MastodonFrameView< ?, ?, ?, ?, ?, ? > view )
	{
		this.view = view;
		toggleSettingsPanelAction = new ToggleSettingsPanelAction( TOGGLE_SETTINGS_PANEL );
		closeWindowAction = new CloseWindowAction( CLOSE_WINDOW );
		// TODO: add group (lock...) select actions
	}

	private class CloseWindowAction extends AbstractNamedAction
	{

		private static final long serialVersionUID = 1L;

		public CloseWindowAction( final String name )
		{
			super( name );
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			/*
			 * To properly close the view, we send it a WINDOW_CLOSING event.
			 * This way, the listeners of the JFrame are called and the closing
			 * happens gracefully within Mastodon.
			 */
			view.getFrame().dispatchEvent( new WindowEvent( view.getFrame(), WindowEvent.WINDOW_CLOSING ) );
		}
	}

	private class ToggleSettingsPanelAction extends AbstractNamedAction implements HasSelectedState
	{
		private static final long serialVersionUID = 1L;

		protected final Listeners.List< Listener > selectListeners;

		public ToggleSettingsPanelAction( final String name )
		{
			super( name );
			selectListeners = new Listeners.SynchronizedList<>();
			view.frame.settingsPanel.addComponentListener( new ComponentAdapter()
			{
				@Override
				public void componentShown( final ComponentEvent e )
				{
					selectListeners.list.forEach( l -> l.setSelected( true ) );
				}

				@Override
				public void componentHidden( final ComponentEvent e )
				{
					selectListeners.list.forEach( l -> l.setSelected( false ) );
				}
			} );
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			view.frame.setSettingsPanelVisible( !view.frame.isSettingsPanelVisible() );
		}

		@Override
		public boolean isSelected()
		{
			return view.frame.isSettingsPanelVisible();
		}

		@Override
		public Listeners< Listener > selectListeners()
		{
			return selectListeners;
		}
	}
}
