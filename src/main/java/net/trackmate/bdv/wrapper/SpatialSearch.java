package net.trackmate.bdv.wrapper;

import net.imglib2.algorithm.kdtree.ConvexPolytope;

public interface SpatialSearch< V extends OverlayVertex< V, ? > >
{
	public void clip( final ConvexPolytope polytope );

	public Iterable< V > getInsideVertices();

//	public boolean isInside( AffineTransform3D transform, double[] min, double[] max );
}
