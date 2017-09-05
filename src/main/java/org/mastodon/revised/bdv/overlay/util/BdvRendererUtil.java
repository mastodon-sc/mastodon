package org.mastodon.revised.bdv.overlay.util;

import net.imglib2.RealInterval;
import net.imglib2.algorithm.kdtree.ConvexPolytope;
import net.imglib2.algorithm.kdtree.HyperPlane;
import net.imglib2.realtransform.AffineTransform3D;

public class BdvRendererUtil
{
	/**
	 * Gets the {@link ConvexPolytope} described by the specified interval in
	 * viewer coordinates, transformed to global coordinates.
	 *
	 * @param transform
	 *            the transform to transform viewer coordinates back in global
	 *            coordinates.
	 * @param viewerInterval
	 *            the view interval.
	 * @return {@code viewerInterval} transformed to global coordinates.
	 */
	public static ConvexPolytope getPolytopeGlobal(
			final AffineTransform3D transform,
			final RealInterval viewerInterval )
	{
		return getPolytopeGlobal( transform,
				viewerInterval.realMin( 0 ), viewerInterval.realMax( 0 ),
				viewerInterval.realMin( 1 ), viewerInterval.realMax( 1 ),
				viewerInterval.realMin( 2 ), viewerInterval.realMax( 2 ) );
	}

	/**
	 * Gets the {@link ConvexPolytope} described by the specified interval in
	 * viewer coordinates, transformed to global coordinates.
	 *
	 * @param transform
	 *            the transform to transform viewer coordinates back in global
	 *            coordinates.
	 * @param viewerMinX
	 *            the x min bound of the view interval.
	 * @param viewerMaxX
	 *            the x max bound of the view interval.
	 * @param viewerMinY
	 *            the y min bound of the view interval.
	 * @param viewerMaxY
	 *            the y max bound of the view interval.
	 * @param viewerMinZ
	 *            the z min bound of the view interval.
	 * @param viewerMaxZ
	 *            the z max bound of the view interval.
	 * @return the specified viewer interval, transformed to global coordinates.
	 */
	public static ConvexPolytope getPolytopeGlobal(
			final AffineTransform3D transform,
			final double viewerMinX, final double viewerMaxX,
			final double viewerMinY, final double viewerMaxY,
			final double viewerMinZ, final double viewerMaxZ )
	{
		final ConvexPolytope polytopeViewer = new ConvexPolytope(
				new HyperPlane(  1,  0,  0, viewerMinX ),
				new HyperPlane( -1,  0,  0, -viewerMaxX ),
				new HyperPlane(  0,  1,  0, viewerMinY ),
				new HyperPlane(  0, -1,  0, -viewerMaxY ),
				new HyperPlane(  0,  0,  1, viewerMinZ),
				new HyperPlane(  0,  0, -1, -viewerMaxZ ) );
		final ConvexPolytope polytopeGlobal = ConvexPolytope.transform( polytopeViewer, transform.inverse() );
		return polytopeGlobal;
	}


	private BdvRendererUtil()
	{}
}
