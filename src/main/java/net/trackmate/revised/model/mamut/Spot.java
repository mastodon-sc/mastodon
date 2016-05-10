package net.trackmate.revised.model.mamut;

import static net.trackmate.graph.mempool.ByteUtils.DOUBLE_SIZE;
import static net.trackmate.revised.model.mamut.Features.LABEL;

import net.trackmate.graph.AbstractVertexPool;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.revised.bdv.overlay.util.JamaEigenvalueDecomposition;
import net.trackmate.revised.model.AbstractSpot3D;
import net.trackmate.revised.model.HasLabel;


// TODO: replace Jama stuff by something that doesn't allocate extra memory

/**
 * {@link AbstractSpot3D} implementation where the spot shape is stored in a
 * covariance matrix.
 *
 * @author Tobias Pietzsch
 */
public class Spot extends AbstractSpot3D< Spot, Link, ByteMappedElement > implements HasLabel
{
	// Copied to be package-visible.
	protected static final int X_OFFSET = AbstractSpot3D.X_OFFSET;
	protected static final int COVARIANCE_OFFSET = AbstractSpot3D.SIZE_IN_BYTES;
	protected static final int BOUNDING_SPHERE_RADIUS_SQUARED_OFFSET = COVARIANCE_OFFSET + 6 * DOUBLE_SIZE;
	protected static final int SIZE_IN_BYTES = BOUNDING_SPHERE_RADIUS_SQUARED_OFFSET + DOUBLE_SIZE;

	public static final double nSigmas = 2;
	public static final double nSigmasSquared = nSigmas * nSigmas;

	@Override
	protected void setToUninitializedState()
	{
		super.setToUninitializedState();
	}

	Spot init( final int timepointId, final double[] pos, final double radius )
	{
		final double eigVal = radius * radius / nSigmasSquared;
		final double[][] cov = new double[][] {
				{ eigVal, 0., 0. },
				{ 0., eigVal, 0. },
				{ 0., 0., eigVal }
		};
		final double boundingSphereRadiusSquared = radius * radius;

		setX( pos[ 0 ] );
		setY( pos[ 1 ] );
		setZ( pos[ 2 ] );
		setCovariance( cov );
		setBoundingSphereRadiusSquared( boundingSphereRadiusSquared );
		setTimepointId( timepointId );
		return this;
	}

	private final JamaEigenvalueDecomposition eig = new JamaEigenvalueDecomposition( 3 );

	Spot init( final int timepointId, final double[] pos, final double[][] cov )
	{
		eig.decomposeSymmetric( cov );
		final double[] eigVals = eig.getRealEigenvalues();
		double max = 0;
		for ( int k = 0; k < eigVals.length; k++ )
			max = Math.max( max, eigVals[ k ] );
		final double boundingSphereRadiusSquared = max * nSigmasSquared;

		setX( pos[ 0 ] );
		setY( pos[ 1 ] );
		setZ( pos[ 2 ] );
		setCovariance( cov );
		setBoundingSphereRadiusSquared( boundingSphereRadiusSquared );
		setTimepointId( timepointId );
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

	protected void setCovariance( final double[][] mat )
	{
		access.putDouble( mat[ 0 ][ 0 ], COVARIANCE_OFFSET );
		access.putDouble( mat[ 0 ][ 1 ], COVARIANCE_OFFSET + 1 * DOUBLE_SIZE );
		access.putDouble( mat[ 0 ][ 2 ], COVARIANCE_OFFSET + 2 * DOUBLE_SIZE );
		access.putDouble( mat[ 1 ][ 1 ], COVARIANCE_OFFSET + 3 * DOUBLE_SIZE );
		access.putDouble( mat[ 1 ][ 2 ], COVARIANCE_OFFSET + 4 * DOUBLE_SIZE );
		access.putDouble( mat[ 2 ][ 2 ], COVARIANCE_OFFSET + 5 * DOUBLE_SIZE );
	}

	public double getBoundingSphereRadiusSquared()
	{
		return access.getDouble( BOUNDING_SPHERE_RADIUS_SQUARED_OFFSET );
	}

	protected void setBoundingSphereRadiusSquared( final double r2 )
	{
		access.putDouble( r2, BOUNDING_SPHERE_RADIUS_SQUARED_OFFSET );
	}

	@Override
	public String getLabel()
	{
		if ( feature( LABEL ).isSet() )
			return feature( LABEL ).get();
		else
			return Integer.toString( getInternalPoolIndex() );
	}

	@Override
	public void setLabel( final String label )
	{
		feature( LABEL ).set( label );
	}

	@Override
	public String toString()
	{
		return String.format( "Spot( %d, X=%.2f, Y=%.2f, Z=%.2f, tp=%d )", getInternalPoolIndex(), getX(), getY(), getZ(), getTimepoint() );
	}

	Spot( final AbstractVertexPool< Spot, Link, ByteMappedElement > pool )
	{
		super( pool );
	}

	/**
	 * Exposes the underlying ByteMappedElement for efficient IO operations.
	 *
	 * @return the underlying spot access object.
	 */
	protected ByteMappedElement getAccess()
	{
		return access;
	}

	/**
	 * Exposes raw covariance entries for undo.
	 */
	protected double getFlattenedCovarianceEntry( final int index )
	{
		return access.getDouble( COVARIANCE_OFFSET + index * DOUBLE_SIZE );
	}

	/**
	 * Exposes raw covariance entries for undo.
	 */
	protected void setFlattenedCovarianceEntry( final double entry, final int index )
	{
		access.putDouble( entry, COVARIANCE_OFFSET + index * DOUBLE_SIZE );
	}
}
