package net.trackmate.model;

import static net.trackmate.graph.mempool.ByteUtils.DOUBLE_SIZE;
import static net.trackmate.graph.mempool.ByteUtils.INT_SIZE;
import net.imglib2.RealLocalizable;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;
import net.trackmate.graph.AbstractVertex;
import net.trackmate.graph.AbstractVertexPool;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.trackscheme.HasTimepoint;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class Spot extends AbstractVertex< Spot, Link, ByteMappedElement > implements RealLocalizable, HasTimepoint
{
	protected static final int X_OFFSET = AbstractVertex.SIZE_IN_BYTES;
	protected static final int Y_OFFSET = X_OFFSET + DOUBLE_SIZE;
	protected static final int Z_OFFSET = Y_OFFSET + DOUBLE_SIZE;
	protected static final int TP_OFFSET = Z_OFFSET + DOUBLE_SIZE;
	protected static final int COVARIANCE_OFFSET = TP_OFFSET + INT_SIZE;
	protected static final int PRECISION_OFFSET = COVARIANCE_OFFSET + 6 * DOUBLE_SIZE;
	protected static final int BOUNDING_SPHERE_RADIUS_SQUARED_OFFSET = PRECISION_OFFSET + 6 * DOUBLE_SIZE;
	protected static final int SIZE_IN_BYTES = BOUNDING_SPHERE_RADIUS_SQUARED_OFFSET + DOUBLE_SIZE;

	public static final double nSigmas = 2;
	public static final double nSigmasSquared = nSigmas * nSigmas;

	@Override
	protected void setToUninitializedState()
	{
		super.setToUninitializedState();
	}

	Spot init( final int timepointId, final double[] pos, final double[][] cov )
	{
		setX( pos[ 0 ] );
		setY( pos[ 1 ] );
		setZ( pos[ 2 ] );
		setCovariance( cov );
		setTimePointId( timepointId );
		return this;
	}

	Spot init( final int timepointId, final double x, final double y, final double z, final double radius )
	{
		setX( x );
		setY( y );
		setZ( z );
		final double[][] T = new double[ 3 ][ 3 ];
		for ( int r = 0; r < 3; ++r )
			T[ r ][ r ] = radius * radius;
		setCovariance( T );
		setBoundingSphereRadiusSquared( radius * radius );
		setTimePointId( timepointId );
		return this;
	}

	Spot init( final int timepointId, final AffineTransform3D transform, final double nu, final double[] m, final double[] W )
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
		final double boundingSphereRadiusSquared = max * nSigmasSquared;

		setX( pos[ 0 ] );
		setY( pos[ 1 ] );
		setZ( pos[ 2 ] );
		setCovariance( S );
		setPrecision( covMat.inverse().getArray() );
		setTimePointId( timepointId );
		setBoundingSphereRadiusSquared( boundingSphereRadiusSquared );
		return this;
	}

	public double getX()
	{
		return access.getDouble( X_OFFSET );
	}

	public void setX( final double x )
	{
		access.putDouble( x, X_OFFSET );
	}

	public double getY()
	{
		return access.getDouble( Y_OFFSET );
	}

	public void setY( final double y )
	{
		access.putDouble( y, Y_OFFSET );
	}

	public double getZ()
	{
		return access.getDouble( Z_OFFSET );
	}

	public void setZ( final double z )
	{
		access.putDouble( z, Z_OFFSET );
	}

	public int getTimePointId()
	{
		return access.getInt( TP_OFFSET );
	}

	public void setTimePointId( final int tp )
	{
		access.putInt( tp, TP_OFFSET );
	}

	@Override
	public int getTimePoint()
	{
		return getTimePointId();
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

	public double getBoundingSphereRadiusSquared()
	{
		return access.getDouble( BOUNDING_SPHERE_RADIUS_SQUARED_OFFSET );
	}

	public void setBoundingSphereRadiusSquared( final double r2 )
	{
		access.putDouble( r2, BOUNDING_SPHERE_RADIUS_SQUARED_OFFSET );
	}

	@Override
	public String toString()
	{
		return String.format( "Spot( %d, X=%.2f, Y=%.2f, Z=%.2f )", getInternalPoolIndex(), getX(), getY(), getZ() );
	}

	Spot( final AbstractVertexPool< Spot, Link, ByteMappedElement > pool )
	{
		super( pool );
	}

	// === RealLocalizable ===

	@Override
	public int numDimensions()
	{
		return 3;
	}

	@Override
	public void localize( final float[] position )
	{
		position[ 0 ] = ( float ) getX();
		position[ 1 ] = ( float ) getY();
		position[ 2 ] = ( float ) getZ();
	}

	@Override
	public void localize( final double[] position )
	{
		position[ 0 ] = getX();
		position[ 1 ] = getY();
		position[ 2 ] = getZ();
	}

	@Override
	public float getFloatPosition( final int d )
	{
		return ( float ) getDoublePosition( d );
	}

	@Override
	public double getDoublePosition( final int d )
	{
		return ( d == 0 ) ? getX() : ( ( d == 1 ) ? getY() : getZ() );
	}

	/**
	 * Exposes the underlying ByteMappedElement for efficient IO operations.
	 *
	 * @return the underlying spot access object.
	 */
	ByteMappedElement getAccess()
	{
		return access;
	}
}
