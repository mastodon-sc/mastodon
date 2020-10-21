package org.mastodon.views.bvv.scene;

import net.imglib2.util.LinAlgHelpers;
import org.mastodon.mamut.model.Spot;
import org.mastodon.views.bdv.overlay.util.JamaEigenvalueDecomposition;
import org.mastodon.views.bvv.BvvVertex;

public class EllipsoidMath
{
	private final JamaEigenvalueDecomposition eig3 = new JamaEigenvalueDecomposition( 3 );
	private final double cov[][] = new double[ 3 ][ 3 ];
	private final float[] edata = new float[ 9 ];
	private final float[] invtedata = new float[ 9 ];

	public void setFromVertex( final BvvVertex< ?, ? > vertex, final ShapeTransform shapeTransform )
	{
		vertex.getCovariance( cov );
		eig3.decomposeSymmetric( cov );
		final double[] eigVals = eig3.getRealEigenvalues();
		final double[][] V = eig3.getV();
		double det = LinAlgHelpers.det3x3(
				V[ 0 ][ 0 ], V[ 0 ][ 1 ], V[ 0 ][ 2 ],
				V[ 1 ][ 0 ], V[ 1 ][ 1 ], V[ 1 ][ 2 ],
				V[ 2 ][ 0 ], V[ 2 ][ 1 ], V[ 2 ][ 2 ] );
		if ( det < 0 )
			LinAlgHelpers.scale( V, -1, V );
		for ( int i = 0; i < 3; ++i )
		{
			final double e = Math.sqrt( eigVals[ i ] );
			final double inve = 1.0 / e;
			for ( int j = 0; j < 3; ++j )
			{
				edata[ j + 3 * i ] = ( float ) ( e * V[ j ][ i ] );
				invtedata[ j + 3 * i ] = ( float ) ( inve * V[ j ][ i ] );
			}
		}

		shapeTransform.e.set( edata );
		shapeTransform.invte.set( invtedata );
		shapeTransform.t.set( vertex.x(), vertex.y(), vertex.z() );
	}

	public void setFromVertex( final BvvVertex< ?, ? > vertex, final Ellipsoid ellipsoid )
	{
		vertex.getCovariance( cov );
		eig3.decomposeSymmetric( cov );
		final double[] eigVals = eig3.getRealEigenvalues();
		final double[][] V = eig3.getV();
		double det = LinAlgHelpers.det3x3(
				V[ 0 ][ 0 ], V[ 0 ][ 1 ], V[ 0 ][ 2 ],
				V[ 1 ][ 0 ], V[ 1 ][ 1 ], V[ 1 ][ 2 ],
				V[ 2 ][ 0 ], V[ 2 ][ 1 ], V[ 2 ][ 2 ] );
		if ( det < 0 )
			LinAlgHelpers.scale( V, -1, V );
		for ( int i = 0; i < 3; ++i )
		{
			final double e = Math.sqrt( eigVals[ i ] );
			final double inve = 1.0 / e;
			for ( int j = 0; j < 3; ++j )
			{
				edata[ j + 3 * i ] = ( float ) ( e * V[ j ][ i ] );
				invtedata[ j + 3 * i ] = ( float ) ( inve * V[ j ][ i ] );
			}
		}

		ellipsoid.e.set( edata );
		ellipsoid.invte.set( invtedata );
		ellipsoid.t.set( vertex.x(), vertex.y(), vertex.z() );
	}

	public void setFromVertex( final Spot vertex, final Ellipsoid ellipsoid )
	{
		vertex.getCovariance( cov );
		eig3.decomposeSymmetric( cov );
		final double[] eigVals = eig3.getRealEigenvalues();
		final double[][] V = eig3.getV();
		double det = LinAlgHelpers.det3x3(
				V[ 0 ][ 0 ], V[ 0 ][ 1 ], V[ 0 ][ 2 ],
				V[ 1 ][ 0 ], V[ 1 ][ 1 ], V[ 1 ][ 2 ],
				V[ 2 ][ 0 ], V[ 2 ][ 1 ], V[ 2 ][ 2 ] );
		if ( det < 0 )
			LinAlgHelpers.scale( V, -1, V );
		for ( int i = 0; i < 3; ++i )
		{
			final double e = Math.sqrt( eigVals[ i ] );
			final double inve = 1.0 / e;
			for ( int j = 0; j < 3; ++j )
			{
				edata[ j + 3 * i ] = ( float ) ( e * V[ j ][ i ] );
				invtedata[ j + 3 * i ] = ( float ) ( inve * V[ j ][ i ] );
			}
		}

		final float x = vertex.getFloatPosition( 0 );
		final float y = vertex.getFloatPosition( 1 );
		final float z = vertex.getFloatPosition( 2 );

		ellipsoid.e.set( edata );
		ellipsoid.invte.set( invtedata );
		ellipsoid.t.set( x, y, z );
	}
}
