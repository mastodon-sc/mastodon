package org.mastodon.views.bvv;

import org.mastodon.views.bvv.scene.Ellipsoids;

public interface ColoredEllipsoidsPerTimepoint< V extends BvvVertex< V, E >, E extends BvvEdge< E, V > >
{
	ColoredEllipsoids< V, E > forTimepoint( final int timepoint );
}
