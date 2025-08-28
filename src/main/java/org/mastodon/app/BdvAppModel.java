package org.mastodon.app;

import org.mastodon.app.plugin.MastodonPlugins;
import org.mastodon.graph.ListenableGraph;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.graph.ref.AbstractListenableVertex;
import org.mastodon.model.MastodonModel;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.util.Actions;

import bdv.ui.keymap.KeymapManager;
import net.imglib2.RealLocalizable;

public class BdvAppModel<
			M extends MastodonModel< G, V, E >,
			G extends ListenableGraph< V, E >,
			V extends AbstractListenableVertex< V, E, ?, ? > & HasTimepoint & RealLocalizable,
			E extends AbstractListenableEdge< E, V, ?, ? > >
		extends AppModel< M, G, V, E >
{

	private final SharedBigDataViewerData sharedBdvData;

	public BdvAppModel(
			final Context context,
			final M model,
			final SharedBigDataViewerData sharedBdvData,
			final KeyPressedManager keyPressedManager,
			final KeymapManager keymapManager,
			final MastodonPlugins< ?, ? > plugins,
			final Actions globalActions,
			final String[] keyConfigContexts,
			final int numGroups )
	{
		super( context, model, keyPressedManager, keymapManager, plugins, globalActions, keyConfigContexts, numGroups );
		this.sharedBdvData = sharedBdvData;
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
