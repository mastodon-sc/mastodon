package org.mastodon.feature.ui;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import org.mastodon.app.ui.settings.ModificationListener;
import org.mastodon.app.ui.settings.SelectAndEditProfileSettingsPage;
import org.mastodon.app.ui.settings.style.StyleProfile;
import org.mastodon.app.ui.settings.style.StyleProfileManager;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode;
import org.mastodon.revised.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.revised.ui.coloring.feature.FeatureRangeCalculator;
import org.mastodon.util.Listeners;
import org.mastodon.util.Listeners.SynchronizedList;

public class FeatureColorModeConfigPage extends SelectAndEditProfileSettingsPage< StyleProfile< FeatureColorMode > >
{

	public FeatureColorModeConfigPage(
			final String treePath,
			final FeatureColorModeManager featureColorModeManager,
			final FeatureSpecsService featureSpecsService,
			final FeatureModel featureModel,
			final FeatureRangeCalculator vertexFeatureRangeCalculator,
			final FeatureRangeCalculator edgeFeatureRangeCalculator,
			final Class< ? > vertexClass,
			final Class< ? > edgeClass )
	{
		super( treePath,
				new StyleProfileManager<>( featureColorModeManager, new FeatureColorModeManager( false ) ),
				new FeatureColorModelEditPanel(
						featureColorModeManager.getDefaultStyle(),
						featureSpecsService,
						featureModel,
						vertexFeatureRangeCalculator,
						edgeFeatureRangeCalculator,
						vertexClass,
						edgeClass ) );
	}

	private static final Set< FeatureSpec< ?, ? > > getFeatureSpecs( final FeatureSpecsService featureSpecsService, final FeatureModel featureModel, final Class< ? > clazz )
	{
		final Set< FeatureSpec< ?, ? > > fs = new HashSet<>( featureSpecsService.getSpecs( clazz ) );
		final Set< FeatureSpec< ?, ? > > fmFs = featureModel.getFeatureSpecs().stream()
				.filter( ( f ) -> f.getTargetClass().isAssignableFrom( clazz ) )
				.collect( Collectors.toSet() );
		fs.addAll( fmFs );
		return fs;
	}

	static class FeatureColorModelEditPanel implements FeatureColorMode.UpdateListener, SelectAndEditProfileSettingsPage.ProfileEditPanel< StyleProfile< FeatureColorMode > >
	{

		private final FeatureColorMode editedMode;

		private final SynchronizedList< ModificationListener > modificationListeners;

		private final FeatureColorModeEditorPanel featureColorModeEditorPanel;

		private boolean trackModifications = true;

		public FeatureColorModelEditPanel(
				final FeatureColorMode initialMode,
				final FeatureSpecsService featureSpecsService,
				final FeatureModel featureModel,
				final FeatureRangeCalculator vertexFeatureRangeCalculator,
				final FeatureRangeCalculator edgeFeatureRangeCalculator,
				final Class< ? > vertexClass,
				final Class< ? > edgeClass )
		{
			this.editedMode = initialMode.copy( "Edited" );
			this.featureColorModeEditorPanel = new FeatureColorModeEditorPanel(
					editedMode,
					vertexFeatureRangeCalculator,
					edgeFeatureRangeCalculator );

			featureColorModeEditorPanel.setFeatureSpecs(
					getFeatureSpecs( featureSpecsService, featureModel, vertexClass ),
					getFeatureSpecs( featureSpecsService, featureModel, edgeClass ) );
			featureModel.listeners().add( () -> {
				featureColorModeEditorPanel.setFeatureSpecs(
						getFeatureSpecs( featureSpecsService, featureModel, vertexClass ),
						getFeatureSpecs( featureSpecsService, featureModel, edgeClass ) );
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