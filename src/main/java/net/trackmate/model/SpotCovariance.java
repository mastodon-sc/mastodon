package net.trackmate.model;

import static net.trackmate.graph.mempool.ByteUtils.DOUBLE_SIZE;
import static net.trackmate.graph.mempool.ByteUtils.INT_SIZE;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;
import net.trackmate.graph.AbstractVertexPool;
import net.trackmate.graph.mempool.ByteMappedElement;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * {@link AbstractSpot} implementation where the spot shape is stored in a
 * covariance matrix.
 *
 * @author Tobias Pietzsch
 *
 */
public class SpotCovariance extends AbstractSpot< SpotCovariance >
{
	protected static final int COVARIANCE_OFFSET = TP_OFFSET + INT_SIZE;
	protected static final int PRECISION_OFFSET = COVARIANCE_OFFSET + 6 * DOUBLE_SIZE;
	protected static final int SIZE_IN_BYTES = PRECISION_OFFSET + 6 * DOUBLE_SIZE;

	public static final double nSigmas = 2;
	public static final double nSigmasSquared = nSigmas * nSigmas;

	@Override
	protected void setToUninitializedState()
	{
		super.setToUninitializedState();
	}

	SpotCovariance init( final int timepointId, final double[] pos, final double[][] cov )
	{
		setX( pos[ 0 ] );
		setY( pos[ 1 ] );
		setZ( pos[ 2 ] );
		setCovariance( cov );
		setTimePointId( timepointId );
		return this;
	}

	SpotCovariance init( final int timepointId, final AffineTransform3D transform, final double nu, final double[] m, final double[] W )
	{
		final double[] wtmp = new double[ 9 ];
		LinAlgHelpers.scale( W, nu, wtmp );

		final Matrix precMat = new Matrix( wtmp, 3 );
		final Matrix covMat = precMat.inverse();
		final double[][] S = covMat.getArray();

		final double[] pos = new double[ 3 ];
		transform.apply( m, pos );

		final double[][] T = new double[3][3];
		for ( int r = 0; r < 3; ++r )
			for ( int c = 0; c < 3; ++c )
				T[r][c] = transform.get( r, c );

		final double[][] TS = new double[3][3];
		LinAlgHelpers.mult( T, S, TS );
		LinAlgHelpers.multABT( TS, T, S );

		final EigenvalueDecomposition eig = covMat.eig();
		final double[] eigVals = eig.getRealEigenvalues();
		double max = 0;
		for ( int k = 0; k < eigVals.length; k++ )
			max = Math.max( max, eigVals[ k ] );
//		final double boundingSphereRadiusSquared = max * nSigmasSquared;

		setX( pos[ 0 ] );
		setY( pos[ 1 ] );
		setZ( pos[ 2 ] );
		setCovariance( S );
		setPrecision( covMat.inverse().getArray() );
		setTimePointId( timepointId );
		return this;
	}

	public void getCovariance( final double[][] mat )
	{
		mat[ 0 ][ 0 ] = access.getDouble( COVARIANCE_OFFSET );
		mat[ 0 ][ 1 ] = access.getDouble( COVARIANCE_OFFSET + 1 * DOUBLE_SIZE );
		mat[ 0 ][ 2 ] = access.getDouble( COVARIANCE_OFFSET + 2 * DOUBLE_SIZE );
		mat[ 1 ][ 0 ] = mat[ 0 ][ 1 ];
		mat[ 1 ][ 1 ] = access.getDouble( COVARIANCE_OFFSET + 3 * DOUBLE_SIZE );
		mat[ 1 ][ 2 ] = access.getDouble( COVARIANCE_OFFSET + 4 * DOUBLE_SIZE );
		mat[ 2 ][ 0 ] = mat[ 0 ][ 2 ];
		mat[ 2 ][ 1 ] = mat[ 1 ][ 2 ];
		mat[ 2 ][ 2 ] = access.getDouble( COVARIANCE_OFFSET + 5 * DOUBLE_SIZE );
	}

	public void setCovariance( final double[][] mat )
	{
		access.putDouble( mat[ 0 ][ 0 ], COVARIANCE_OFFSET );
		access.putDouble( mat[ 0 ][ 1 ], COVARIANCE_OFFSET + 1 * DOUBLE_SIZE );
		access.putDouble( mat[ 0 ][ 2 ], COVARIANCE_OFFSET + 2 * DOUBLE_SIZE );
		access.putDouble( mat[ 1 ][ 1 ], COVARIANCE_OFFSET + 3 * DOUBLE_SIZE );
		access.putDouble( mat[ 1 ][ 2 ], COVARIANCE_OFFSET + 4 * DOUBLE_SIZE );
		access.putDouble( mat[ 2 ][ 2 ], COVARIANCE_OFFSET + 5 * DOUBLE_SIZE );
	}

	public void getPrecision( final double[][] mat )
	{
		mat[ 0 ][ 0 ] = access.getDouble( PRECISION_OFFSET );
		mat[ 0 ][ 1 ] = access.getDouble( PRECISION_OFFSET + 1 * DOUBLE_SIZE );
		mat[ 0 ][ 2 ] = access.getDouble( PRECISION_OFFSET + 2 * DOUBLE_SIZE );
		mat[ 1 ][ 0 ] = mat[ 0 ][ 1 ];
		mat[ 1 ][ 1 ] = access.getDouble( PRECISION_OFFSET + 3 * DOUBLE_SIZE );
		mat[ 1 ][ 2 ] = access.getDouble( PRECISION_OFFSET + 4 * DOUBLE_SIZE );
		mat[ 2 ][ 0 ] = mat[ 0 ][ 2 ];
		mat[ 2 ][ 1 ] = mat[ 1 ][ 2 ];
		mat[ 2 ][ 2 ] = access.getDouble( PRECISION_OFFSET + 5 * DOUBLE_SIZE );
	}

	public void setPrecision( final double[][] mat )
	{
		access.putDouble( mat[ 0 ][ 0 ], PRECISION_OFFSET );
		access.putDouble( mat[ 0 ][ 1 ], PRECISION_OFFSET + 1 * DOUBLE_SIZE );
		access.putDouble( mat[ 0 ][ 2 ], PRECISION_OFFSET + 2 * DOUBLE_SIZE );
		access.putDouble( mat[ 1 ][ 1 ], PRECISION_OFFSET + 3 * DOUBLE_SIZE );
		access.putDouble( mat[ 1 ][ 2 ], PRECISION_OFFSET + 4 * DOUBLE_SIZE );
		access.putDouble( mat[ 2 ][ 2 ], PRECISION_OFFSET + 5 * DOUBLE_SIZE );
	}

	@Override
	public String toString()
	{
		return String.format( "CovarianceSpot( %d, X=%.2f, Y=%.2f, Z=%.2f, tp=%d )", getInternalPoolIndex(), getX(), getY(), getZ(), getTimePoint() );
	}

	SpotCovariance( final AbstractVertexPool< SpotCovariance, Link< SpotCovariance >, ByteMappedElement > pool )
	{
		super( pool );
	}
}
