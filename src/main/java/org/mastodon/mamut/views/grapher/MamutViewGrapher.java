package org.mastodon.mamut.views.grapher;

import org.mastodon.app.views.grapher.MastodonViewGrapher;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

/**
 * Mamut app-specific Grapher view.
 *
 * @author Jean-Yves Tinevez
 *
 */
public class MamutViewGrapher extends MastodonViewGrapher< Model, ModelGraph, Spot, Link >
{

	public MamutViewGrapher( final MamutAppModel appModel )
	{
		super(
				appModel.dataModel(),
				appModel.uiModel(),
				new MamutDataGraphProperties( appModel.dataModel().getGraph(), appModel.imageData().getSources().size() ) );
	}
}
