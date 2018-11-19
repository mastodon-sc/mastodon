package org.mastodon.feature.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import org.mastodon.app.ui.settings.ModificationListener;
import org.mastodon.app.ui.settings.SelectAndEditProfileSettingsPage;
import org.mastodon.app.ui.settings.style.StyleProfile;
import org.mastodon.app.ui.settings.style.StyleProfileManager;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.ui.coloring.feature.AvailableFeatureProjections;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode;
import org.mastodon.revised.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.revised.ui.coloring.feature.FeatureRangeCalculator;
import org.mastodon.revised.ui.coloring.feature.revised.Playground.AvailableFeatureProjectionsImp;
import org.mastodon.util.Listeners;
import org.mastodon.util.Listeners.SynchronizedList;

public class FeatureColorModeConfigPage extends SelectAndEditProfileSettingsPage< StyleProfile< FeatureColorMode > >
{

	public FeatureColorModeConfigPage(
			final String treePath,
			final FeatureColorModeManager featureColorModeManager,
			final FeatureSpecsService featureSpecsService,
			final int numSources,
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
						numSources,
						featureModel,
						featureColorModeManager,
						vertexFeatureRangeCalculator,
						edgeFeatureRangeCalculator,
						vertexClass,
						edgeClass ) );
	}

	private static class FeatureColorModelEditPanel implements FeatureColorMode.UpdateListener, SelectAndEditProfileSettingsPage.ProfileEditPanel< StyleProfile< FeatureColorMode > >
	{

		private final FeatureColorMode editedMode;

		private final SynchronizedList< ModificationListener > modificationListeners;

		private final FeatureColorModeEditorPanel featureColorModeEditorPanel;

		private boolean trackModifications = true;

		public FeatureColorModelEditPanel(
				final FeatureColorMode initialMode,
				final FeatureSpecsService featureSpecsService,
				final int numSources,
				final FeatureModel featureModel,
				final FeatureColorModeManager featureColorModeManager,
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

			featureColorModeEditorPanel.setAvailableFeatureProjections(
					getFeatureSpecs( featureSpecsService, numSources, featureModel, featureColorModeManager, vertexClass, edgeClass ) );

			// Listen to changes in the feature model.
			featureModel.listeners().add( () -> {
				featureColorModeEditorPanel.setAvailableFeatureProjections(
						getFeatureSpecs( featureSpecsService, numSources, featureModel, featureColorModeManager, vertexClass, edgeClass ) );
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

	/*
	 * Below this: Everything required to generate a comprehensive set of feature specs, from 3 sources:
	 * - discoverable feature specs;
	 * - feature model;
	 * - unknown feature specs mentioned in color modes.
	 */

	private static AvailableFeatureProjections getFeatureSpecs(
			final FeatureSpecsService featureSpecsService,
			final int numSources,
			final FeatureModel featureModel,
			final FeatureColorModeManager featureColorModeManager,
			final Class< ? > vertexClass,
			final Class< ? > edgeClass )
	{
		final AvailableFeatureProjectionsImp projections = new AvailableFeatureProjectionsImp( vertexClass, edgeClass );

		/*
		 * Available source indices from SharedBigDataViewerData.
		 */
		projections.setMinNumSources( Math.max( numSources, 1 ) );

		/*
		 * Feature specs from service.
		 */
		featureSpecsService.getSpecs( vertexClass ).forEach( projections::add );
		featureSpecsService.getSpecs( edgeClass ).forEach( projections::add );

		/*
		 * Feature specs from feature model.
		 */
		featureModel.getFeatureSpecs().forEach( projections::add );

		/*
		 * Feature projections used in existing FeatureColorModes
		 */
		final Collection< FeatureColorMode > modes = new ArrayList<>();
		modes.addAll( featureColorModeManager.getBuiltinStyles() );
		modes.addAll( featureColorModeManager.getUserStyles() );
		for ( final FeatureColorMode mode : modes )
		{
			/*
			 * Vertex mode.
			 */
			projections.add( mode.getVertexFeatureProjection(), mode.getVertexColorMode().targetType() );

			/*
			 * Edge mode.
			 */
			projections.add( mode.getEdgeFeatureProjection(), mode.getEdgeColorMode().targetType() );
		}

		return projections;
	}
}
