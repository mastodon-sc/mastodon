package org.mastodon.revised.bdv.overlay;

import org.mastodon.revised.bdv.overlay.util.JamaEigenvalueDecomposition;

import net.imglib2.RealLocalizable;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

/**
 * Computations to extract information from {@link OverlayVertex} and current
 * viewer transform for painting.
 * <p>
 * One instance is used repeatedly for multiple {@link OverlayVertex
 * OverlayVertices} as follows:
 * <ol>
 * <li>Call {@link #init(OverlayVertex, AffineTransform3D)} with the
 * {@link OverlayVertex} and the current viewer transform. This resets internal
 * state.
 * <li>Call any of the getters (e.g., {@link #getIntersectEllipse()}). This
 * triggers all necessary computations to provide the requested value.
 * Intermediate results are cached.
 * </ol>
 *
 * @author Tobias Pietzsch
 */
public class ScreenVertexMath
{
	public static class Ellipse
	{
		/**
		 * center of 2D ellipse.
		 */
		private final double[] center;

		/**
		 * rotation angle of 2D ellipse (in radians).
		 */
		private double theta;

		/**
		 * half-width and half-height of axis-aligned 2D ellipse.
		 */
		private final double[] axisHalfLength;

		public Ellipse()
		{
			center = new double[ 2 ];
			axisHalfLength = new double[ 2 ];
		}

		public double[] getCenter()
		{
			return center;
		}

		public void setCenter( final double[] p )
		{
			setCenter( p[ 0 ], p[ 1 ] );
		}

		public void setCenter( final double x, final double y )
		{
			center[ 0 ] = x;
			center[ 1 ] = y;
		}

		public double getTheta()
		{
			return theta;
		}

		public void setTheta( final double theta )
		{
			this.theta = theta;
		}

		public double[] getAxisHalfLength()
		{
			return axisHalfLength;
		}

		public double getHalfWidth()
		{
			return axisHalfLength[ 0 ];
		}

		public double getHalfHeight()
		{
			return axisHalfLength[ 1 ];
		}

		public void setAxisHalfLength( final double[] l )
		{
			setAxisHalfLength( l[ 0 ], l[ 1 ] );
		}

		public void setAxisHalfLength( final double w, final double h )
		{
			axisHalfLength[ 0 ] = w;
			axisHalfLength[ 1 ] = h;
		}
	}

	private AffineTransform3D transform;

	/**
	 * spot position in global coordinate system.
	 */
	private final double[] pos = new double[ 3 ];

	/**
	 * spot covariance in global coordinate system.
	 */
	private final double[][] S = new double[ 3 ][ 3 ];

	/**
	 * spot precision in global coordinate system.
	 */
	private final double[][] P = new double[ 3 ][ 3 ];

	/**
	 * spot position in viewer coordinate system.
	 */
	private final double[] vPos = new double[ 3 ];

	/**
	 * spot covariance in viewer coordinate system.
	 */
	private final double[][] vS = new double[ 3 ][ 3 ];

	/**
	 * precision of 2D ellipse obtained by projecting ellipsoid to z=0 plane.
	 */
	private final double[][] vP = new double[ 2 ][ 2 ];

	/**
	 * 2D ellipse obtained by projecting ellipsoid to z=0 plane.
	 */
	private final Ellipse projectEllipse = new Ellipse();

	/**
	 * whether the ellipsoid intersects the z=0 plane.
	 */
	private boolean intersectsViewPlane;

	/**
	 * 2D ellipse obtained by intersecting ellipsoid with z=0 plane.
	 */
	private final Ellipse intersectEllipse = new Ellipse();

	private boolean projectionComputed;

	private boolean intersectionComputed;

	private boolean precisionComputed;

	private boolean projectedPrecisionComputed;

	// tmp
	private final JamaEigenvalueDecomposition eig2 = new JamaEigenvalueDecomposition( 2 );

	// tmp
	private final JamaEigenvalueDecomposition eig3 = new JamaEigenvalueDecomposition( 3 );

	// tmp
	private final double[][] T = new double[ 3 ][ 3 ];

	// tmp
	private final double[][] TS = new double[ 3 ][ 3 ];

	// tmp
	private final double[] vn = new double[ 3 ];

	// tmp
	private final double[] vm = new double[ 3 ];

	// tmp
	private final double[] diff = new double[ 3 ];

	// tmp
	private final double[] vn2 = new double[ 2 ];

	// tmp
	private final double[] vm2 = new double[ 2 ];

	// tmp
	private final double[] diff2 = new double[ 2 ];

	/**
	 * covariance of 2D ellipse obtained by intersecting ellipsoid with z=0 plane.
	 */
	private final double[][] iS = new double[ 2 ][ 2 ];

	/**
	 * precision of 2D ellipse obtained by intersecting ellipsoid with z=0 plane.
	 */
	private final double[][] iP = new double[ 2 ][ 2 ];

	/**
	 * (Re-)initialize for a new {@code vertex} and the given viewer transform.
	 * Resets the state of this {@link ScreenVertexMath}, discarding all cached
	 * computed values.
	 *
	 * @param vertex
	 *            the vertex.
	 * @param viewerTransform
	 *            the transform.
	 */
	public void init( final OverlayVertex< ?, ? > vertex, final AffineTransform3D viewerTransform )
	{
		this.transform = viewerTransform;

		// transform spot covariance into viewer coordinates => vS
		vertex.getCovariance( S );
		for ( int r = 0; r < 3; ++r )
			for ( int c = 0; c < 3; ++c )
				T[ r ][ c ] = transform.get( r, c );
		LinAlgHelpers.mult( T, S, TS );
		LinAlgHelpers.multABT( TS, T, vS );

		// transform spot position into viewer coordinates => vPos
		vertex.localize( pos );
		transform.apply( pos, vPos );

		projectionComputed = false;
		intersectionComputed = false;
		precisionComputed = false;
		projectedPrecisionComputed = false;
	}

	/**
	 * Get spot position in viewer coordinate system.
	 *
	 * @return spot position in viewer coordinate system.
	 */
	public double[] getViewPos()
	{
		return vPos;
	}

	/**
	 * Get center of 2D ellipse obtained by projecting ellipsoid to z=0 plane.
	 *
	 * @return center of 2D ellipse obtained by projecting ellipsoid to z=0
	 *         plane.
	 */
	public double[] getProjectCenter()
	{
		computeProjection();
		return projectEllipse.getCenter();
	}

	/**
	 * Get the rotation angle (in radian) of 2D ellipse obtained by projecting
	 * ellipsoid to z=0 plane.
	 *
	 * @return rotation angle of 2D ellipse obtained by projecting ellipsoid to
	 *         z=0 plane.
	 */
	public double getProjectTheta()
	{
		computeProjection();
		return projectEllipse.getTheta();
	}

	/**
	 * Get the 2D ellipse obtained by projecting ellipsoid to z=0 plane.
	 *
	 * @return 2D ellipse obtained by projecting ellipsoid to z=0 plane.
	 */
	public Ellipse getProjectEllipse()
	{
		computeProjection();
		return projectEllipse;
	}

	/**
	 * Test whether the ellipsoid intersects the z=0 plane.
	 *
	 * @return {@code true} iff the ellipsoid intersects the z=0 plane.
	 */
	public boolean intersectsViewPlane()
	{
		computeIntersection();
		return intersectsViewPlane;
	}

	/**
	 * Get center of 2D ellipse obtained by intersecting ellipsoid with z=0
	 * plane.
	 *
	 * @return center of 2D ellipse obtained by intersecting ellipsoid with z=0
	 *         plane.
	 */
	public double[] getIntersectCenter()
	{
		computeIntersection();
		return intersectEllipse.getCenter();
	}

	/**
	 * Get the rotation angle ( in radian) of 2D ellipse obtained by
	 * intersecting ellipsoid with z=0 plane.
	 *
	 * @return rotation angle of 2D ellipse obtained by intersecting ellipsoid
	 *         with z=0 plane.
	 */
	public double getIntersectTheta()
	{
		computeIntersection();
		return intersectEllipse.getTheta();
	}

	/**
	 * Get the 2D ellipse obtained by intersecting ellipsoid with z=0 plane.
	 *
	 * @return 2D ellipse obtained by intersecting ellipsoid with z=0 plane.
	 */
	public Ellipse getIntersectEllipse()
	{
		computeIntersection();
		return intersectEllipse;
	}

	/**
	 * Test whether the given 3D point {@code p} in global coordinates lies
	 * within the ellipsoid.
	 *
	 * @param p
	 *            the point to test.
	 * @return {@code true} iff the given point lies within the ellipsoid.
	 */
	public boolean containsGlobal( final RealLocalizable p )
	{
		p.localize( vm );
		return containsGlobal( vm );
	}

	/**
	 * Test whether the given 3D point {@code p} in global coordinates lies
	 * within the ellipsoid.
	 *
	 * @param p
	 *            the point to test.
	 * @return {@code true} iff the given point lies within the ellipsoid.
	 */
	public boolean containsGlobal( final double[] p )
	{
		computePrecision();
		LinAlgHelpers.subtract( pos, p, diff );
		LinAlgHelpers.mult( P, diff, vn );
		final double d2 = LinAlgHelpers.dot( diff, vn );
		return d2 < 1;
	}

	/**
	 * Test whether the given 2D point {@code p} in view coordinates lies within
	 * the ellipse obtained by projecting ellipsoid to z=0 plane.
	 *
	 * @param p
	 *            the point to test.
	 * @return {@code true} iff the given point lies within the projected
	 *         ellipsoid.
	 */
	public boolean projectionContainsView( final RealLocalizable p )
	{
		p.localize( vm2 );
		return projectionContainsView( vm2 );
	}

	/**
	 * Test whether the given 2D point {@code p} in view coordinates lies within
	 * the ellipse obtained by projecting ellipsoid to z=0 plane.
	 *
	 * @param p
	 *            the point to test.
	 * @return {@code true} iff the given point lies within the projected
	 *         ellipsoid.
	 */
	public boolean projectionContainsView( final double[] p )
	{
		computeProjectedPrecision();
		diff2[ 0 ] = vPos[ 0 ] - p[ 0 ];
		diff2[ 1 ] = vPos[ 1 ] - p[ 1 ];
		LinAlgHelpers.mult( vP, diff2, vn2 );
		final double d2 = LinAlgHelpers.dot( diff2, vn2 );
		return d2 < 1;
	}

	/**
	 * Test whether the projection of the ellipsoid onto the z=0 plane
	 * intersects the rectangle defined by
	 * {@code minX <= x <= maxX, minY <= y <= minX}.
	 *
	 * @param minX
	 *            the x min bound of the rectangle.
	 * @param maxX
	 *            the x max bound of the rectangle.
	 * @param minY
	 *            the y min bound of the rectangle.
	 * @param maxY
	 *            the y max bound of the rectangle.
	 * @return {@code true} if the ellipsoid projection intersects with the
	 *         rectangle.
	 */
	public boolean projectionIntersectsViewInterval( final double minX, final double maxX, final double minY, final double maxY )
	{
		computeProjection();
		computeProjectedPrecision();

		final double e0 = ( maxX - minX ) / 2;
		final double e1 = ( maxY - minY ) / 2;

		// Compute the increase in extents for R’.
		final double l0 = Math.sqrt( vS[ 0 ][ 0 ] );
		final double l1 = Math.sqrt( vS[ 1 ][ 1 ] );

		// Transform the ellipse center to rectangle coordinate system.
		diff2[ 0 ] = ( minX + maxX ) / 2;
		diff2[ 1 ] = ( minY + maxY ) / 2;
		LinAlgHelpers.subtract( projectEllipse.getCenter(), diff2, vn2 );

		if ( Math.abs( vn2[ 0 ] ) <= e0 + l0 && Math.abs( vn2[ 1 ] ) <= e1 + l1 )
		{
			final double s0 = ( vn2[ 0 ] >= 0 ) ? 1 : -1;
			final double s1 = ( vn2[ 1 ] >= 0 ) ? 1 : -1;
			vn2[ 0 ] -= s0 * e0;
			vn2[ 1 ] -= s1 * e1;
			vm2[ 0 ] = vP[ 0 ][ 0 ] * vn2[ 0 ] + vP[ 0 ][ 1 ] * vn2[ 1 ];
			vm2[ 1 ] = vP[ 1 ][ 0 ] * vn2[ 0 ] + vP[ 1 ][ 1 ] * vn2[ 1 ];
			if ( s0 * vm2[ 0 ] <= 0 || s1 * vm2[ 1 ] <= 0 )
				return true;
			return LinAlgHelpers.dot( vn2, vm2 ) <= 1;
		}

		return false;
	}

	/**
	 * Test whether the intersection of the ellipsoid with the z=0 plane
	 * intersects the rectangle defined by
	 * {@code minX <= x <= maxX, minY <= y <= minX}.
	 *
	 * @param minX
	 *            the x min bound of the rectangle.
	 * @param maxX
	 *            the x max bound of the rectangle.
	 * @param minY
	 *            the y min bound of the rectangle.
	 * @param maxY
	 *            the y max bound of the rectangle.
	 * @return {@code true} if the ellipsoid plane-intersection intersects with
	 *         the rectangle.
	 */
	public boolean intersectionIntersectsViewInterval( final double minX, final double maxX, final double minY, final double maxY )
	{
		computeIntersection();

		final double e0 = ( maxX - minX ) / 2;
		final double e1 = ( maxY - minY ) / 2;

		// Compute the increase in extents for R’.
		final double l0 = Math.sqrt( iS[ 0 ][ 0 ] );
		final double l1 = Math.sqrt( iS[ 1 ][ 1 ] );

		// Transform the ellipse center to rectangle coordinate system.
		diff2[ 0 ] = ( minX + maxX ) / 2;
		diff2[ 1 ] = ( minY + maxY ) / 2;
		LinAlgHelpers.subtract( intersectEllipse.getCenter(), diff2, vn2 );

		if ( Math.abs( vn2[ 0 ] ) <= e0 + l0 && Math.abs( vn2[ 1 ] ) <= e1 + l1 )
		{
			final double s0 = ( vn2[ 0 ] >= 0 ) ? 1 : -1;
			final double s1 = ( vn2[ 1 ] >= 0 ) ? 1 : -1;
			vn2[ 0 ] -= s0 * e0;
			vn2[ 1 ] -= s1 * e1;
			LinAlgHelpers.invertSymmetric2x2( iS, iP );
			vm2[ 0 ] = iP[ 0 ][ 0 ] * vn2[ 0 ] + iP[ 0 ][ 1 ] * vn2[ 1 ];
			vm2[ 1 ] = iP[ 1 ][ 0 ] * vn2[ 0 ] + iP[ 1 ][ 1 ] * vn2[ 1 ];
			if ( s0 * vm2[ 0 ] <= 0 || s1 * vm2[ 1 ] <= 0 )
				return true;
			return LinAlgHelpers.dot( vn2, vm2 ) <= 1;
		}

		return false;
	}

	private void computePrecision()
	{
		if ( precisionComputed )
			return;

		LinAlgHelpers.invertSymmetric3x3( S, P );
		precisionComputed = true;
	}

	private void computeProjectedPrecision()
	{
		if ( projectedPrecisionComputed )
			return;

		LinAlgHelpers.invertSymmetric2x2( vS, vP );
		projectedPrecisionComputed = true;
	}

	private void computeProjection()
	{
		if ( projectionComputed )
			return;

		/*
		 * decompose the upper 2x2 sub-matrix of S, i.e., the spot
		 * covariance in XY of the viewer coordinate system.
		 */
		eig2.decomposeSymmetric( vS );
		final double[] eigVals2 = eig2.getRealEigenvalues();
		final double w = Math.sqrt( eigVals2[ 0 ] );
		final double h = Math.sqrt( eigVals2[ 1 ] );
		final double c = eig2.getV()[ 0 ][ 0 ];
		final double s = eig2.getV()[ 1 ][ 0 ];

		projectEllipse.setTheta( Math.atan2( s, c ) );
		projectEllipse.setCenter( vPos );
		projectEllipse.setAxisHalfLength( w, h );

		projectionComputed = true;
	}

	private void computeIntersection()
	{
		if ( intersectionComputed )
			return;

		eig3.decomposeSymmetric( vS );
		final double[] eigVals = eig3.getRealEigenvalues();
		final double[][] V = eig3.getV();
		for ( int i = 0; i < 3; ++i )
		{
			final double e = Math.sqrt( eigVals[ i ] );
			final double inve = 1.0 / e;
			for ( int j = 0; j < 3; ++j )
			{
				T[ j ][ i ] = e * V[ j ][ i ];
				TS[ j ][ i ] = inve * V[ j ][ i ];
			}
		}
		/*
		 * now T and TS^T transform from unit sphere to covariance ellipsoid and
		 * vice versa
		 */

		final double[] vx = TS[ 0 ];
		final double[] vy = TS[ 1 ];
		final double[] vz = TS[ 2 ];

		final double z = vPos[ 2 ];
		LinAlgHelpers.cross( vx, vy, vn );
		LinAlgHelpers.normalize( vn );
		LinAlgHelpers.scale( vz, z, vz );
		final double d = LinAlgHelpers.dot( vn, vz );
		if ( Math.abs( d ) >= 1 )
		{
			intersectsViewPlane = false;
		}
		else
		{
			intersectsViewPlane = true;

			final double radius2 = 1.0 - d * d;
			LinAlgHelpers.scale( vn, LinAlgHelpers.dot( vn, vz ), vn );
			LinAlgHelpers.subtract( vz, vn, vz );
			LinAlgHelpers.mult( T, vz, vn );
			final double xshift = vn[ 0 ];
			final double yshift = vn[ 1 ];

			final double c2 = LinAlgHelpers.squareLength( vx );
			final double c = Math.sqrt( c2 );
			final double a = LinAlgHelpers.dot( vx, vy ) / c;
			final double a2 = a * a;
			final double b2 = LinAlgHelpers.squareLength( vy ) - a2;
			iS[ 0 ][ 0 ] = radius2 * ( 1.0 / c2 + a2 / ( b2 * c2 ) );
			iS[ 0 ][ 1 ] = radius2 * -a / ( b2 * c );
			iS[ 1 ][ 0 ] = iS[ 0 ][ 1 ];
			iS[ 1 ][ 1 ] = radius2 / b2;
			/*
			 * now iS is the 2D covariance ellipsoid of transformed circle with radius
			 */

			eig2.decomposeSymmetric( iS );
			final double[] eigVals2 = eig2.getRealEigenvalues();
			final double w = Math.sqrt( eigVals2[ 0 ] );
			final double h = Math.sqrt( eigVals2[ 1 ] );
			final double ci = eig2.getV()[ 0 ][ 0 ];
			final double si = eig2.getV()[ 1 ][ 0 ];

			intersectEllipse.setTheta( Math.atan2( si, ci ) );
			intersectEllipse.setCenter( vPos[ 0 ] + xshift, vPos[ 1 ] + yshift );
			intersectEllipse.setAxisHalfLength( w, h );
		}

		intersectionComputed = true;
	}
}
