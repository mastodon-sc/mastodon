package org.mastodon.feature.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.app.ui.settings.ModificationListener;
import org.mastodon.app.ui.settings.SelectAndEditProfileSettingsPage;
import org.mastodon.app.ui.settings.style.StyleProfile;
import org.mastodon.app.ui.settings.style.StyleProfileManager;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.feature.Multiplicity;
import org.mastodon.revised.model.mamut.Link;
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

			final Map< Class< ? >, Set< FeatureSpec< ?, ? > > > featureSpecsMap =
					getFeatureSpecs( featureSpecsService, featureModel, featureColorModeManager, vertexClass, edgeClass );
			featureColorModeEditorPanel.setFeatureSpecs(
					featureSpecsMap.get( vertexClass ),
					featureSpecsMap.get( edgeClass ) );

			// Listen to changes in the feature model.
			featureModel.listeners().add( () -> {
				final Map< Class< ? >, Set< FeatureSpec< ?, ? > > > fsMap =
						getFeatureSpecs( featureSpecsService, featureModel, featureColorModeManager, vertexClass, edgeClass );
				featureColorModeEditorPanel.setFeatureSpecs(
						fsMap.get( vertexClass ),
						fsMap.get( edgeClass ) );
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

	private static Map< Class< ? >, Set< FeatureSpec< ?, ? > > > getFeatureSpecs(
			final FeatureSpecsService featureSpecsService,
			final FeatureModel featureModel,
			final FeatureColorModeManager featureColorModeManager,
			final Class< ? > vertexClass,
			final Class< ? > edgeClass )
	{
		/*
		 * Feature specs from service.
		 */
		// Vertex feature specs.
		final Set< FeatureSpec< ?, ? > > vfs = new HashSet<>( featureSpecsService.getSpecs( vertexClass ) );
		// Edge feature specs.
		final Set< FeatureSpec< ?, ? > > efs = new HashSet<>( featureSpecsService.getSpecs( edgeClass ) );

		/*
		 * Feature specs from feature model.
		 */
		// Vertex feature specs.
		final Set< FeatureSpec< ?, ? > > fmVfs = featureModel.getFeatureSpecs().stream()
				.filter( ( fs ) -> fs.getTargetClass().isAssignableFrom( vertexClass ) )
				.collect( Collectors.toSet() );
		vfs.addAll( fmVfs );
		// Edge feature specs.
		final Set< FeatureSpec< ?, ? > > fmEfs = featureModel.getFeatureSpecs().stream()
				.filter( ( fs ) -> fs.getTargetClass().isAssignableFrom( Link.class ) )
				.collect( Collectors.toSet() );
		efs.addAll( fmEfs );

		/*
		 * Feature keys.
		 */
		final Set< String > vertexFeatureKeys = vfs.stream()
				.map( FeatureSpec::getKey )
				.collect( Collectors.toSet() );
		final Set< String > edgeFeatureKeys = efs.stream()
				.map( FeatureSpec::getKey )
				.collect( Collectors.toSet() );

		// Add all mode keys, in case of invalid yet editable ones.
		final Collection< FeatureColorMode > modes = new ArrayList<>();
		modes.addAll( featureColorModeManager.getBuiltinStyles() );
		modes.addAll( featureColorModeManager.getUserStyles() );
		for ( final FeatureColorMode mode : modes )
		{
			/*
			 * Vertex mode.
			 */
			final String[] vertexKeys = mode.getVertexFeatureProjection();
			final String vertexFeatureKey = vertexKeys[ 0 ];
			// Can we find a known FeatureSpec for this key?
			switch ( mode.getVertexColorMode() )
			{
			case VERTEX:
				if ( !vertexFeatureKeys.contains( vertexFeatureKey ) )
					vfs.add( createFeatureSpec( vertexFeatureKey, vertexKeys[ 1 ], vertexClass ) );
				break;
			case INCOMING_EDGE:
			case OUTGOING_EDGE:
				if ( !edgeFeatureKeys.contains( vertexFeatureKey ) )
					efs.add( createFeatureSpec( vertexFeatureKey, vertexKeys[ 1 ], edgeClass ) );
				break;
			default:
				break;
			}
			/*
			 * Edge mode.
			 */
			final String[] edgeKeys = mode.getEdgeFeatureProjection();
			final String edgeFeatureKey = edgeKeys[ 0 ];
			switch ( mode.getEdgeColorMode() )
			{
			case EDGE:
				if ( !edgeFeatureKeys.contains( edgeFeatureKey ) )
					efs.add( createFeatureSpec( edgeFeatureKey, edgeKeys[ 1 ], edgeClass ) );
				break;
			case SOURCE_VERTEX:
			case TARGET_VERTEX:
				if ( !vertexFeatureKeys.contains( edgeFeatureKey ) )
					vfs.add( createFeatureSpec( edgeFeatureKey, edgeKeys[ 1 ], vertexClass ) );
				break;
			default:
				break;

			}
		}

		final Map< Class< ? >, Set< FeatureSpec< ?, ? > > > featureSpecsMap = new HashMap<>();
		featureSpecsMap.put( vertexClass, vfs );
		featureSpecsMap.put( edgeClass, efs );
		return featureSpecsMap;
	}

	private static < T > FeatureSpec< ?, T > createFeatureSpec( final String featureKey, final String projectionKey, final Class< T > clazz )
	{
		return new DummyFeatureSpec<>(
				featureKey,
				"Dummy feature spec for " + featureKey,
				DummyFeature.class,
				clazz,
				Multiplicity.SINGLE,
				new FeatureProjectionSpec( projectionKey, Dimension.NONE ) );
	}

	private static class DummyFeatureSpec< F extends Feature< T >, T > extends FeatureSpec< F, T >
	{

		protected DummyFeatureSpec(
				final String key,
				final String info,
				final Class< F > featureClass,
				final Class< T > targetClass,
				final Multiplicity multiplicity,
				final FeatureProjectionSpec... projectionSpecs )
		{
			super( key, info, featureClass, targetClass, multiplicity, projectionSpecs );
		}
	}

	// Dummy Feature class used to instantiate a feature spec.
	private static class DummyFeature< T > implements Feature< T >
	{

		@Override
		public FeatureProjection< T > project( final FeatureProjectionKey key )
		{
			return null;
		}

		@Override
		public Set< FeatureProjectionKey > projectionKeys()
		{
			return null;
		}

		@Override
		public Set< FeatureProjection< T > > projections()
		{
			return null;
		}

		@Override
		public FeatureSpec< ? extends Feature< T >, T > getSpec()
		{
			return null;
		}
	}
}
