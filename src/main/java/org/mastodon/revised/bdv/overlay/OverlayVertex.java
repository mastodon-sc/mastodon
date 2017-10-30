package org.mastodon.revised.bdv.overlay;

import org.mastodon.Ref;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.model.HasLabel;
import org.mastodon.spatial.HasTimepoint;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;

public interface OverlayVertex< O extends OverlayVertex< O, E >, E extends OverlayEdge< E, ? > >
		extends Vertex< E >, Ref< O >, RealLocalizable, RealPositionable, HasTimepoint, HasLabel
{
	public void getCovariance( final double[][] mat );

	public void setCovariance( final double[][] mat );

	public double getBoundingSphereRadiusSquared();

	public O init( final int timepoint, final double[] position, final double radius );

	public O init( final int timepoint, final double[] position, final double[][] covariance );
}
