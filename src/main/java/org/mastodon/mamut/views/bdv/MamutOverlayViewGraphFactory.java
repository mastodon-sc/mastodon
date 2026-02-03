package org.mastodon.mamut.views.bdv;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.views.bdv.overlay.OverlayViewGraphFactory;
import org.mastodon.views.bdv.overlay.shapes.ellipsoid.wrap.OverlayEdgeWrapper;
import org.mastodon.views.bdv.overlay.shapes.ellipsoid.wrap.OverlayGraphWrapper;
import org.mastodon.views.bdv.overlay.shapes.ellipsoid.wrap.OverlayVertexWrapper;

public class MamutOverlayViewGraphFactory implements OverlayViewGraphFactory<
		Model,
		MamutBdvOverlayProperties,
		OverlayVertexWrapper< Spot, Link >,
		OverlayEdgeWrapper< Spot, Link >,
		Spot,
		Link >
{

	@Override
	public OverlayGraphWrapper< Spot, Link > createViewGraph( final Model dataModel, final MamutBdvOverlayProperties properties )
	{
		return new OverlayGraphWrapper< Spot, Link >(
				dataModel.getGraph(),
				dataModel.getGraphIdBimap(),
				dataModel.getSpatioTemporalIndex(),
				properties.getLock(),
				properties );
	}
}
