package org.mastodon.app;

import org.mastodon.app.plugin.MastodonPlugins2;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.feature.ui.DefaultFeatureProjectionsManager;
import org.mastodon.feature.ui.FeatureColorModeConfigPage;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.model.MastodonModel;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider.Scope;
import org.scijava.ui.behaviour.util.Actions;

import bdv.ui.keymap.KeymapManager;

public class BdvAppModel<
		AM extends BdvAppModel< AM, M, G, V, E >,
		M extends MastodonModel< G, V, E >,
		G extends ReadOnlyGraph< V, E >,
		V extends Vertex< E > & HasTimepoint,
		E extends Edge< V > >
		extends AppModel< AM, M, G, V, E >
{


	private final SharedBigDataViewerData sharedBdvData;

	public BdvAppModel(
			final Context context,
			final M model,
			final SharedBigDataViewerData sharedBdvData,
			@SuppressWarnings( "rawtypes" ) final Class viewFactoryType,
			final KeyPressedManager keyPressedManager,
			final KeymapManager keymapManager,
			final MastodonPlugins2< ?, ? > plugins,
			final Actions globalActions,
			final String[] keyConfigContexts,
			final Scope scope,
			final int numGroups )
	{
		super(
				context,
				model,
				viewFactoryType,
				keyPressedManager,
				keymapManager,
				plugins,
				globalActions,
				keyConfigContexts,
				scope,
				numGroups,
				0,
				sharedBdvData.getNumTimepoints() - 1 );
		this.sharedBdvData = sharedBdvData;

		/*
		 * Add a settings page for feature color modes, that depends on the data
		 * model and on the number of sources in the bdv data
		 */
		final FeatureColorModeManager featureColorModeManager = new FeatureColorModeManager();
		uiModel.registerInstance( featureColorModeManager );
		final DefaultFeatureProjectionsManager featureProjectionsManager = new DefaultFeatureProjectionsManager( context.getService( FeatureSpecsService.class ), featureColorModeManager );
		featureProjectionsManager.setModel( model, model.getFeatureModel(), sharedBdvData.getSources().size() );
		uiModel.getPreferencesDialog().addPage( new FeatureColorModeConfigPage( "Settings > Feature Color Modes", featureColorModeManager, featureProjectionsManager, "Vertex", "Edge" ) );
	}

	public SharedBigDataViewerData imageData()
	{
		return sharedBdvData;
	}

	@Override
	public int getTimepointMin()
	{
		return 0;
	}

	@Override
	public int getTimepointMax()
	{
		return sharedBdvData.getNumTimepoints() - 1;
	}
}
