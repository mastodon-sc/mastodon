package org.mastodon.mamut.views.trackscheme;

import org.mastodon.app.views.trackscheme.MastodonViewTrackScheme2;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

/**
 * Mamut app-specific TrackScheme view.
 *
 * @author Jean-Yves Tinevez
 *
 */
public class MamutViewTrackScheme2 extends MastodonViewTrackScheme2< Model, ModelGraph, Spot, Link >
{

	public MamutViewTrackScheme2( final MamutAppModel appModel )
	{
		super( appModel, new MamutTrackSchemeProperties( appModel.dataModel().getGraph() ) );
	}
}
