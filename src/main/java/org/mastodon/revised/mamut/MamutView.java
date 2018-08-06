package org.mastodon.revised.mamut;

import static org.mastodon.app.ui.MastodonViewStateSerialization.VIEW_TYPE_KEY;

import java.util.Map;

import org.mastodon.app.ViewGraph;
import org.mastodon.app.ui.MastodonFrameView;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Spot;

public class MamutView< VG extends ViewGraph< Spot, Link, V, E >, V extends Vertex< E >, E extends Edge< V > >
		extends MastodonFrameView< MamutAppModel, VG, Spot, Link, V, E >
{
	public MamutView( final MamutAppModel appModel, final VG viewGraph, final String[] keyConfigContexts )
	{
		super( appModel, viewGraph, keyConfigContexts );
	}

	@Override
	public Map< String, Object > getGUIState()
	{
		final Map< String, Object > guiState = super.getGUIState();
		guiState.put( VIEW_TYPE_KEY, getClass().getSimpleName() );
		return guiState;
	}
}
