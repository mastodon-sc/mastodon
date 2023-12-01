/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.ui.keymap;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.mastodon.views.bdv.NavigationActionsDescriptions;
import org.scijava.listeners.Listeners;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.io.gui.VisualEditorPanel;

import bdv.ui.keymap.Keymap;
import bdv.ui.keymap.KeymapManager;
import bdv.ui.settings.ModificationListener;
import bdv.ui.settings.SelectAndEditProfileSettingsPage;
import bdv.ui.settings.SettingsPanel;
import bdv.ui.settings.style.StyleProfile;
import bdv.ui.settings.style.StyleProfileManager;

public class KeymapSettingsPage extends SelectAndEditProfileSettingsPage< StyleProfile< Keymap > >
{
	/**
	 * Creates a new settings page for Keymaps.
	 *
	 * @param treePath
	 *            path of this page in the settings tree.
	 * @param styleManager
	 *            the keymap manager.
	 * @param commandDescriptions
	 *            the command descriptions.
	 */
	public KeymapSettingsPage( final String treePath, final KeymapManager styleManager,
			final CommandDescriptions commandDescriptions )
	{
		super(
				treePath,
				new StyleProfileManager<>( styleManager, new MastodonKeymapManager() ),
				new KeymapProfileEditPanel( styleManager.getSelectedStyle(), commandDescriptions ) );
	}

	static class KeymapProfileEditPanel
			implements VisualEditorPanel.ConfigChangeListener, ProfileEditPanel< StyleProfile< Keymap > >
	{
		private final Listeners.SynchronizedList< ModificationListener > modificationListeners;

		private final Keymap editedStyle;

		private final VisualEditorPanel styleEditorPanel;

		public KeymapProfileEditPanel( final Keymap initialStyle, final CommandDescriptions commandDescriptions )
		{
			editedStyle = initialStyle.copy( "Edited" );
			styleEditorPanel = new VisualEditorPanel( editedStyle.getConfig(),
					commandDescriptions.createCommandDescriptionsMap() );
			styleEditorPanel.setButtonPanelVisible( false );
			modificationListeners = new Listeners.SynchronizedList<>();
			styleEditorPanel.modelChangedListeners().add( this );
			styleEditorPanel.setPreferredSize( new Dimension( 200, 200 ) );
		}

		private boolean trackModifications = true;

		@Override
		public void configChanged()
		{
			styleEditorPanel.modelToConfig();
			if ( trackModifications )
				modificationListeners.list.forEach( ModificationListener::setModified );
		}

		@Override
		public void loadProfile( final StyleProfile< Keymap > profile )
		{
			trackModifications = false;
			editedStyle.set( profile.getStyle() );
			styleEditorPanel.configToModel();
			trackModifications = true;
		}

		@Override
		public void storeProfile( final StyleProfile< Keymap > profile )
		{
			trackModifications = false;
			styleEditorPanel.modelToConfig();
			editedStyle.setName( profile.getStyle().getName() );
			trackModifications = true;
			profile.getStyle().set( editedStyle );
		}

		@Override
		public Listeners< ModificationListener > modificationListeners()
		{
			return modificationListeners;
		}

		@Override
		public JPanel getJPanel()
		{
			return styleEditorPanel;
		}
	}

	public static void main( final String[] args )
	{
		final KeymapManager styleManager = new MastodonKeymapManager();

		final SettingsPanel settings = new SettingsPanel();

		final CommandDescriptions descriptions = new CommandDescriptions();
		descriptions.setKeyconfigContext( KeyConfigContexts.BIGDATAVIEWER );
		new NavigationActionsDescriptions().getCommandDescriptions( descriptions );

		settings.addPage( new KeymapSettingsPage( "Style > Keymap", styleManager, descriptions ) );

		final JDialog dialog = new JDialog( ( Frame ) null, "Settings" );
		dialog.getContentPane().add( settings, BorderLayout.CENTER );

		settings.onOk( () -> dialog.setVisible( false ) );
		settings.onCancel( () -> dialog.setVisible( false ) );

		dialog.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
		dialog.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				settings.cancel();
			}
		} );

		dialog.pack();
		dialog.setVisible( true );
	}
}
