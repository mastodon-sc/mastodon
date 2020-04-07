package org.mastodon.feature.ui;

import javax.swing.JPanel;

import org.mastodon.app.ui.settings.ModificationListener;
import org.mastodon.app.ui.settings.SelectAndEditProfileSettingsPage;
import org.mastodon.app.ui.settings.style.StyleProfile;
import org.mastodon.app.ui.settings.style.StyleProfileManager;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode;
import org.mastodon.revised.ui.coloring.feature.FeatureColorModeManager;
import org.scijava.listeners.Listeners;
import org.scijava.listeners.Listeners.SynchronizedList;

public class FeatureColorModeConfigPage extends SelectAndEditProfileSettingsPage< StyleProfile< FeatureColorMode > >
{
	public FeatureColorModeConfigPage(
			final String treePath,
			final FeatureColorModeManager featureColorModeManager,
			final FeatureProjectionsManager featureProjectionsManager )
	{
		super( treePath,
				new StyleProfileManager<>( featureColorModeManager, new FeatureColorModeManager( false ) ),
				new FeatureColorModelEditPanel(
						featureColorModeManager.getDefaultStyle(),
						featureProjectionsManager ) );
	}

	public static class FeatureColorModelEditPanel implements FeatureColorMode.UpdateListener, SelectAndEditProfileSettingsPage.ProfileEditPanel< StyleProfile< FeatureColorMode > >
	{

		private final FeatureColorMode editedMode;

		private final SynchronizedList< ModificationListener > modificationListeners;

		private final FeatureColorModeEditorPanel featureColorModeEditorPanel;

		private boolean trackModifications = true;

		public FeatureColorModelEditPanel(
				final FeatureColorMode initialMode,
				final FeatureProjectionsManager featureProjectionsManager )
		{
			this.editedMode = initialMode.copy( "Edited" );
			this.featureColorModeEditorPanel = new FeatureColorModeEditorPanel(
					editedMode,
					featureProjectionsManager.getFeatureRangeCalculator() );
			featureColorModeEditorPanel.setAvailableFeatureProjections( featureProjectionsManager.getAvailableFeatureProjections() );
			featureProjectionsManager.listeners().add( () -> {
				featureColorModeEditorPanel.setAvailableFeatureProjections( featureProjectionsManager.getAvailableFeatureProjections() );
			} );
			this.modificationListeners = new Listeners.SynchronizedList<>();
			editedMode.updateListeners().add( this );
		}

		@Override
		public Listeners< ModificationListener > modificationListeners()
		{
			return modificationListeners;
		}

		@Override
		public void loadProfile( final StyleProfile< FeatureColorMode > profile )
		{
			trackModifications = false;
			editedMode.set( profile.getStyle() );
			trackModifications = true;
		}

		@Override
		public void storeProfile( final StyleProfile< FeatureColorMode > profile )
		{
			trackModifications = false;
			editedMode.setName( profile.getStyle().getName() );
			trackModifications = true;
			profile.getStyle().set( editedMode );
		}

		@Override
		public JPanel getJPanel()
		{
			return featureColorModeEditorPanel;
		}

		@Override
		public void featureColorModeChanged()
		{
			if ( trackModifications )
				modificationListeners.list.forEach( ModificationListener::modified );
		}
	}
}
