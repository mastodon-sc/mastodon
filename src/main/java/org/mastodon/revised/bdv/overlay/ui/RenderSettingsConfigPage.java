package org.mastodon.revised.bdv.overlay.ui;

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
import org.mastodon.revised.bdv.overlay.RenderSettings;
import org.mastodon.util.Listeners;

public class RenderSettingsConfigPage extends SelectAndEditProfileSettingsPage< StyleProfile< RenderSettings > >
{
	/**
	 * Creates a new render-settings config page.
	 *
	 * @param treePath
	 * 		path of this page in the settings tree.
	 * @param renderSettingsManager
	 *            the render settings manager.
	 */
	public RenderSettingsConfigPage( final String treePath, final RenderSettingsManager renderSettingsManager )
	{
		super(
				treePath,
				new StyleProfileManager<>( renderSettingsManager, new RenderSettingsManager( false ) ),
				new RenderSettingsProfileEditPanel( renderSettingsManager.getDefaultStyle() ) );
	}

	static class RenderSettingsProfileEditPanel implements RenderSettings.UpdateListener, SelectAndEditProfileSettingsPage.ProfileEditPanel< StyleProfile< RenderSettings > >
	{
		private final Listeners.SynchronizedList< ModificationListener > modificationListeners;

		private final RenderSettings editedStyle;

		private final JPanel styleEditorPanel;

		private final DummyBdvPanel dummyModelCanvas;

		public RenderSettingsProfileEditPanel( final RenderSettings initialStyle )
		{
			editedStyle = initialStyle.copy( "Edited" );
			styleEditorPanel = new JPanel();
			styleEditorPanel.setLayout( new BorderLayout() );
			styleEditorPanel.add( new RenderSettingsPanel( editedStyle ), BorderLayout.CENTER );

			this.dummyModelCanvas = new DummyBdvPanel();
			dummyModelCanvas.setRenderSettings( initialStyle );
			styleEditorPanel.add( dummyModelCanvas, BorderLayout.EAST );
			modificationListeners = new Listeners.SynchronizedList<>();
			editedStyle.updateListeners().add( this );
		}

		private boolean trackModifications = true;

		@Override
		public void renderSettingsChanged()
		{
			dummyModelCanvas.setRenderSettings( editedStyle );
			if ( trackModifications )
				modificationListeners.list.forEach( ModificationListener::modified );
		}

		@Override
		public void loadProfile( final StyleProfile< RenderSettings > profile )
		{
			trackModifications = false;
			editedStyle.set( profile.getStyle() );
			trackModifications = true;
		}

		@Override
		public void storeProfile( final StyleProfile< RenderSettings > profile )
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
		final RenderSettingsManager styleManager = new RenderSettingsManager();

		final SettingsPanel settings = new SettingsPanel();
		settings.addPage( new RenderSettingsConfigPage( "Style > BDV", styleManager ) );

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
