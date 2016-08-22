package net.trackmate.revised.bdv.overlay;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.trackmate.Ref;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.model.HasLabel;
import net.trackmate.spatial.HasTimepoint;

public interface OverlayVertex< O extends OverlayVertex< O, E >, E extends OverlayEdge< E, ? > >
		extends Vertex< E >, Ref< O >, RealLocalizable, RealPositionable, HasTimepoint, HasLabel
{
	public boolean isSelected();

	public void getCovariance( final double[][] mat );

	public void setCovariance( final double[][] mat );

	public double getBoundingSphereRadiusSquared();
}
