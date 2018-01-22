package org.mastodon.revised.ui.keymap;

import java.awt.BorderLayout;
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
import org.mastodon.util.Listeners;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.gui.VisualEditorPanel;

public class KeymapSettingsPage extends SelectAndEditProfileSettingsPage< StyleProfile< Keymap > >
{
	/**
	 * @param treePath
	 * 		path of this page in the settings tree.
	 */
	public KeymapSettingsPage( final String treePath, final KeymapManager styleManager )
	{
		super(
				treePath,
				new StyleProfileManager<>( styleManager, new KeymapManager( false ) ),
				new KeymapProfileEditPanel( styleManager.getDefaultStyle() ) );
	}

	static class KeymapProfileEditPanel implements VisualEditorPanel.ConfigChangeListener, ProfileEditPanel< StyleProfile< Keymap > >
	{
		private final Listeners.SynchronizedList< ModificationListener > modificationListeners;

		private final Keymap editedStyle;

		private final VisualEditorPanel styleEditorPanel;

		public KeymapProfileEditPanel( final Keymap initialStyle )
		{
			editedStyle = initialStyle.copy( "Edited" );
			styleEditorPanel = new VisualEditorPanel( editedStyle.getConfig() );
			modificationListeners = new Listeners.SynchronizedList<>();
			styleEditorPanel.addConfigChangeListener( this );
		}

		private boolean trackModifications = true;

		@Override
		public void configChanged()
		{
			if ( trackModifications )
				modificationListeners.list.forEach( ModificationListener::modified );
		}

		@Override
		public void loadProfile( final StyleProfile< Keymap > profile )
		{
			trackModifications = false;
			editedStyle.set( profile.getStyle() );
			trackModifications = true;
		}

		@Override
		public void storeProfile( final StyleProfile< Keymap > profile )
		{
			trackModifications = false;
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
		settings.addPage( new KeymapSettingsPage( "Style > Keymap", styleManager ) );

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
