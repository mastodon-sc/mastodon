package org.mastodon.mamut.views.trackscheme;

import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.ModelGraphTrackSchemeProperties;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.views.MamutView2;

public class MamutViewTrackScheme2 extends MastodonViewTrackScheme2< Model, ModelGraph, Spot, Link >
		implements MamutView2
{

	public MamutViewTrackScheme2( final MamutAppModel appModel )
	{
		super( appModel, new ModelGraphTrackSchemeProperties( appModel.dataModel().getGraph() ) );
	}
}
