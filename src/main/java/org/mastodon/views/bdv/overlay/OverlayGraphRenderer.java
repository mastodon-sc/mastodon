package org.mastodon.views.bdv.overlay;

import bdv.viewer.OverlayRenderer;
import bdv.viewer.TimePointListener;
import bdv.viewer.TransformListener;
import net.imglib2.realtransform.AffineTransform3D;

/**
 * Interface for renderers of Mastodon graphs as BDV overlays.
 * 
 * @param <V>
 *            the type of vertex.
 * @param <E>
 */
public interface OverlayGraphRenderer< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
		extends OverlayRenderer, TransformListener< AffineTransform3D >, TimePointListener
{

}
