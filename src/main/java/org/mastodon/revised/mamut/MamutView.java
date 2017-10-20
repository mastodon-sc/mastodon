package org.mastodon.revised.mamut;

import org.mastodon.app.MastodonView;
import org.mastodon.app.ViewGraph;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Spot;

public class MamutView< VG extends ViewGraph< Spot, Link, V, E >, V extends Vertex< E >, E extends Edge< V > > extends MastodonView< MamutAppModel, VG, Spot, Link, V, E  >
{
	public MamutView( final MamutAppModel appModel, final VG viewGraph )
	{
		super( appModel, viewGraph );
	}
}
