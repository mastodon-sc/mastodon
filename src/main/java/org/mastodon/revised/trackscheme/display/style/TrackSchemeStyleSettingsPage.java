package org.mastodon.revised.trackscheme.display.style;

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
import org.scijava.listeners.Listeners;

public class TrackSchemeStyleSettingsPage extends SelectAndEditProfileSettingsPage< StyleProfile< TrackSchemeStyle > >
{
	/**
	 * Creates a new settings page for TrackScheme styles.
	 *
	 * @param treePath
	 * 		path of this page in the settings tree.
	 * @param styleManager
	 *            the style manager.
	 */
	public TrackSchemeStyleSettingsPage( final String treePath, final TrackSchemeStyleManager styleManager )
	{
		super(
				treePath,
				new StyleProfileManager<>( styleManager, new TrackSchemeStyleManager( false ) ),
				new TrackSchemeProfileEditPanel( styleManager.getDefaultStyle() ) );
	}

	static class TrackSchemeProfileEditPanel implements TrackSchemeStyle.UpdateListener, SelectAndEditProfileSettingsPage.ProfileEditPanel< StyleProfile< TrackSchemeStyle > >
	{
		private final Listeners.SynchronizedList< ModificationListener > modificationListeners;

		private final TrackSchemeStyle editedStyle;

		private final TrackSchemeStyleEditorPanel styleEditorPanel;

		public TrackSchemeProfileEditPanel( final TrackSchemeStyle initialStyle )
		{
			editedStyle = initialStyle.copy( "Edited" );
			styleEditorPanel = new TrackSchemeStyleEditorPanel( editedStyle );
			modificationListeners = new Listeners.SynchronizedList<>();
			editedStyle.updateListeners().add( this );
		}

		private boolean trackModifications = true;

		@Override
		public void trackSchemeStyleChanged()
		{
			if ( trackModifications )
				modificationListeners.list.forEach( ModificationListener::modified );
		}

		@Override
		public void loadProfile( final StyleProfile< TrackSchemeStyle > profile )
		{
			trackModifications = false;
			editedStyle.set( profile.getStyle() );
			trackModifications = true;
		}

		@Override
		public void storeProfile( final StyleProfile< TrackSchemeStyle > profile )
		{
			trackModifications = false;
			editedStyle.name( profile.getStyle().getName() );
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
		final TrackSchemeStyleManager styleManager = new TrackSchemeStyleManager();

		final SettingsPanel settings = new SettingsPanel();
		settings.addPage( new TrackSchemeStyleSettingsPage( "Style > TrackScheme", styleManager ) );

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
