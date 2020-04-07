package org.mastodon.revised.ui.keymap;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.mastodon.app.ui.settings.ModificationListener;
import org.mastodon.app.ui.settings.SelectAndEditProfileSettingsPage;
import org.mastodon.app.ui.settings.SettingsPanel;
import org.mastodon.app.ui.settings.style.StyleProfile;
import org.mastodon.app.ui.settings.style.StyleProfileManager;
import org.mastodon.revised.bdv.NavigationActionsMamut;
import org.mastodon.revised.mamut.KeyConfigContexts;
import org.scijava.listeners.Listeners;
import org.scijava.ui.behaviour.io.gui.VisualEditorPanel;

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
	public KeymapSettingsPage( final String treePath, final KeymapManager styleManager, final CommandDescriptions commandDescriptions )
	{
		super(
				treePath,
				new StyleProfileManager<>( styleManager, new KeymapManager( false ) ),
				new KeymapProfileEditPanel( styleManager.getDefaultStyle(), commandDescriptions ) );
	}

	static class KeymapProfileEditPanel implements VisualEditorPanel.ConfigChangeListener, ProfileEditPanel< StyleProfile< Keymap > >
	{
		private final Listeners.SynchronizedList< ModificationListener > modificationListeners;

		private final Keymap editedStyle;

		private final VisualEditorPanel styleEditorPanel;

		public KeymapProfileEditPanel( final Keymap initialStyle, final CommandDescriptions commandDescriptions )
		{
			editedStyle = initialStyle.copy( "Edited" );
			styleEditorPanel = new VisualEditorPanel( editedStyle.getConfig(), commandDescriptions.createCommandDescriptionsMap() );
			styleEditorPanel.setButtonPanelVisible( false );
			modificationListeners = new Listeners.SynchronizedList<>();
			styleEditorPanel.addConfigChangeListener( this );
			styleEditorPanel.setPreferredSize( new Dimension( 200, 200 ) );
		}

		private boolean trackModifications = true;

		@Override
		public void configChanged()
		{
			styleEditorPanel.modelToConfig();
			if ( trackModifications )
				modificationListeners.list.forEach( ModificationListener::modified );
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
		final KeymapManager styleManager = new KeymapManager();

		final SettingsPanel settings = new SettingsPanel();

		final CommandDescriptions descriptions = new CommandDescriptions();
		descriptions.setKeyconfigContext( KeyConfigContexts.BIGDATAVIEWER );
		new NavigationActionsMamut.Descriptions().getCommandDescriptions( descriptions );

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
