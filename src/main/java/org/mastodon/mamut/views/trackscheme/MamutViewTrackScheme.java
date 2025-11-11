package org.mastodon.mamut.views.trackscheme;

import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.views.trackscheme.MastodonViewTrackScheme;
import org.mastodon.views.trackscheme.display.TrackSchemeOverlay.TrackSchemeOverlayFactory;
import org.mastodon.views.trackscheme.graph.LineageTreeLayoutImp;

/**
 * Mamut app-specific TrackScheme view.
 *
 * @author Jean-Yves Tinevez
 *
 */
public class MamutViewTrackScheme extends MastodonViewTrackScheme< Model, ModelGraph, Spot, Link >
{

	public MamutViewTrackScheme( final MamutAppModel appModel )
	{
		super( appModel.dataModel(),
				appModel.windowManager(),
				new MamutTrackSchemeProperties( appModel.dataModel().getGraph() ),
				new TrackSchemeOverlayFactory(),
				LineageTreeLayoutImp::new,
				appModel.getTimepointMin(),
				appModel.getTimepointMax() );
	}
}
