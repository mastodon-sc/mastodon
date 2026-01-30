package org.mastodon.views.bdv.overlay.shapes.ellipsoid;

import org.mastodon.model.HasCovariance;
import org.mastodon.views.bdv.overlay.OverlayVertex;

public interface EllipsoidOverlayVertex< O extends EllipsoidOverlayVertex< O, E >, E extends EllipsoidOverlayEdge< E, ? > >
		extends OverlayVertex< O, E >, HasCovariance
{

	double getBoundingSphereRadiusSquared();

	O init( int timepoint, double[] position, double radius );

	O init( int timepoint, double[] position, double[][] covariance );
}
