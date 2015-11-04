package net.trackmate.revised.bdv.overlay;

import java.awt.geom.Ellipse2D;

import net.imglib2.RealLocalizable;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;
import net.trackmate.revised.bdv.overlay.util.JamaEigenvalueDecomposition;

/**
 * Computations to extract information from {@link OverlayVertex} and
 * current viewer transform for painting.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class ScreenVertexMath
{
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
	 * center of 2D ellipse obtained by projecting ellipsoid to z=0 plane.
	 */
	private final double[] projectCenter = new double[ 2 ];

	/**
	 * rotation angle of 2D ellipse obtained by projecting ellipsoid to z=0 plane.
	 */
	private double projectTheta;

	/**
	 * 2D ellipse obtained by projecting ellipsoid to z=0 plane. To draw it,
	 * need to translate by {@link #projectCenter} and rotate by
	 * {@link #projectTheta}.
	 */
	private Ellipse2D projectEllipse;

	/**
	 * whether the ellipsoid intersects the z=0 plane.
	 */
	private boolean intersectsViewPlane;

	/**
	 * center of 2D ellipse obtained by intersecting ellipsoid with z=0 plane.
	 */
	private final double[] intersectCenter = new double[ 2 ];

	/**
	 * rotation angle of 2D ellipse obtained by intersecting ellipsoid with z=0 plane.
	 */
	private double intersectTheta;

	/**
	 * 2D ellipse obtained by intersecting ellipsoid with z=0 plane. To draw
	 * it, need to translate by {@link #intersectCenter} and rotate by
	 * {@link #intersectTheta}.
	 */
	private Ellipse2D intersectEllipse;

	private final double nSigmas;

	private boolean projectionComputed;

	private boolean intersectionComputed;

	private boolean precisionComputed;

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
	private final double[][] AAT = new double[ 2 ][ 2 ];

	public ScreenVertexMath( final double nSigmas )
	{
		this.nSigmas = nSigmas;
	}

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
	}

	public double[] getViewPos()
	{
		return vPos;
	}

	public double[] getProjectCenter()
	{
		computeProjection();
		return projectCenter;
	}

	public double getProjectTheta()
	{
		computeProjection();
		return projectTheta;
	}

	public Ellipse2D getProjectEllipse()
	{
		computeProjection();
		return projectEllipse;
	}

	public boolean intersectsViewPlane()
	{
		computeIntersection();
		return intersectsViewPlane;
	}

	public double[] getIntersectCenter()
	{
		computeIntersection();
		return intersectCenter;
	}

	public double getIntersectTheta()
	{
		computeIntersection();
		return intersectTheta;
	}

	public Ellipse2D getIntersectEllipse()
	{
		computeIntersection();
		return intersectEllipse;
	}

	public boolean containsGlobal( final RealLocalizable p )
	{
		p.localize( vm );
		return containsGlobal( vm );
	}

	public boolean containsGlobal( final double[] p )
	{
		computePrecision();
		LinAlgHelpers.subtract( pos, p, diff );
		LinAlgHelpers.mult( P, diff, vn );
		final double d2 = LinAlgHelpers.dot( p, vn );
		return d2 < nSigmas * nSigmas;
	}

	private void computePrecision()
	{
		if ( precisionComputed )
			return;

		invertSymmetric3x3( S, P );
		precisionComputed = true;
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
		final double w = nSigmas * Math.sqrt( eigVals2[ 0 ] );
		final double h = nSigmas * Math.sqrt( eigVals2[ 1 ] );
		final double c = eig2.getV()[ 0 ][ 0 ];
		final double s = eig2.getV()[ 1 ][ 0 ];

		projectTheta = Math.atan2( s, c );
		projectCenter[ 0 ] = vPos[ 0 ];
		projectCenter[ 1 ] = vPos[ 1 ];
		projectEllipse = new Ellipse2D.Double( -w, -h, 2 * w, 2 * h );

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
		final double c2 = LinAlgHelpers.squareLength( vx );
		final double c = Math.sqrt( c2 );
		final double a = LinAlgHelpers.dot( vx, vy ) / c;
		final double a2 = a * a;
		final double b2 = LinAlgHelpers.squareLength( vy ) - a2;

		AAT[ 0 ][ 0 ] = 1.0 / c2 + a2 / ( b2 * c2 );
		AAT[ 0 ][ 1 ] = -a / ( b2 * c );
		AAT[ 1 ][ 0 ] = AAT[ 0 ][ 1 ];
		AAT[ 1 ][ 1 ] = 1.0 / b2;
		/*
		 * now AAT is the 2D covariance ellipsoid of transformed unit circle
		 */

		final double z = vPos[ 2 ];
		LinAlgHelpers.cross( vx, vy, vn );
		LinAlgHelpers.normalize( vn );
		LinAlgHelpers.scale( vz, z, vz );
		final double d = LinAlgHelpers.dot( vn, vz ) / nSigmas;
		if ( d >= 1 )
		{
			intersectsViewPlane = false;
		}
		else
		{
			intersectsViewPlane = true;

			final double radius = Math.sqrt( 1.0 - d * d );
			LinAlgHelpers.scale( vn, LinAlgHelpers.dot( vn, vz ), vn );
			LinAlgHelpers.subtract( vz, vn, vz );
			LinAlgHelpers.mult( T, vz, vn );
			final double xshift = vn[ 0 ];
			final double yshift = vn[ 1 ];

			eig2.decomposeSymmetric( AAT );
			final double[] eigVals2 = eig2.getRealEigenvalues();
			final double w = nSigmas * Math.sqrt( eigVals2[ 0 ] ) * radius;
			final double h = nSigmas * Math.sqrt( eigVals2[ 1 ] ) * radius;
			final double ci = eig2.getV()[ 0 ][ 0 ];
			final double si = eig2.getV()[ 1 ][ 0 ];

			intersectTheta = Math.atan2( si, ci );
			intersectCenter[ 0 ] = vPos[ 0 ] + xshift;
			intersectCenter[ 1 ] = vPos[ 1 ] + yshift;
			intersectEllipse = new Ellipse2D.Double( -w, -h, 2 * w, 2 * h );
		}

		intersectionComputed = true;
	}

	public static void invertSymmetric3x3( final double[][] m, final double[][] inverse )
	{
		final double a00 = m[ 2 ][ 2 ] * m[ 1 ][ 1 ] - m[ 1 ][ 2 ] * m[ 1 ][ 2 ];
		final double a01 = m[ 0 ][ 2 ] * m[ 1 ][ 2 ] - m[ 2 ][ 2 ] * m[ 0 ][ 1 ];
		final double a02 = m[ 0 ][ 1 ] * m[ 1 ][ 2 ] - m[ 0 ][ 2 ] * m[ 1 ][ 1 ];

		final double a11 = m[ 2 ][ 2 ] * m[ 0 ][ 0 ] - m[ 0 ][ 2 ] * m[ 0 ][ 2 ];
		final double a12 = m[ 0 ][ 1 ] * m[ 0 ][ 2 ] - m[ 0 ][ 0 ] * m[ 1 ][ 2 ];

		final double a22 = m[ 0 ][ 0 ] * m[ 1 ][ 1 ] - m[ 0 ][ 1 ] * m[ 0 ][ 1 ];

		final double Dinv = 1.0 / ( ( m[ 0 ][ 0 ] * a00 ) + ( m[ 1 ][ 0 ] * a01 ) + ( m[ 0 ][ 2 ] * a02 ) );

		inverse[0][0] = a00 * Dinv;
		inverse[1][0] = inverse[0][1] = a01 * Dinv;
		inverse[2][0] = inverse[0][2] = a02 * Dinv;
		inverse[1][1] = a11 * Dinv;
		inverse[2][1] = inverse[1][2] = a12 * Dinv;
		inverse[2][2] = a22 * Dinv;
	}
}
