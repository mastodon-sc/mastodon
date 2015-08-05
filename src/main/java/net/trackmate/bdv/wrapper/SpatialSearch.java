package net.trackmate.bdv.wrapper;

import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.kdtree.ConvexPolytope;

public interface SpatialSearch< V extends OverlayVertex< V, ? > >
{
	public void clip( final ConvexPolytope polytope );

	public Iterable< V > getInsideVertices();

	public void search( RealLocalizable p );

	public V nearestNeighbor();

	public double nearestNeighborSquareDistance();

//	public boolean isInside( AffineTransform3D transform, double[] min, double[] max );
}
