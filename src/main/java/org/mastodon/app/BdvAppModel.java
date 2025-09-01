package org.mastodon.app;

import org.mastodon.app.plugin.MastodonPlugins;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.model.MastodonModel;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.util.Actions;

import bdv.ui.keymap.KeymapManager;

public class BdvAppModel<
		M extends MastodonModel< G, V, E >,
		G extends ReadOnlyGraph< V, E >,
		V extends Vertex< E > & HasTimepoint,
		E extends Edge< V > >
		extends AppModel< M, G, V, E >
{

	private final SharedBigDataViewerData sharedBdvData;

	public BdvAppModel(
			final Context context,
			final M model,
			final SharedBigDataViewerData sharedBdvData,
			final Class< ? > viewFactoryType,
			final KeyPressedManager keyPressedManager,
			final KeymapManager keymapManager,
			final MastodonPlugins< ?, ? > plugins,
			final Actions globalActions,
			final String[] keyConfigContexts,
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
				numGroups,
				0,
				sharedBdvData.getNumTimepoints() - 1 );
		this.sharedBdvData = sharedBdvData;
	}

	public SharedBigDataViewerData imageData()
	{
		return sharedBdvData;
	}
}
