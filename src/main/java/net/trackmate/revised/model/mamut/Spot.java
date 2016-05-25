package net.trackmate.revised.model.mamut;

import static net.trackmate.pool.ByteUtils.DOUBLE_SIZE;
import static net.trackmate.revised.model.mamut.ModelFeatures.LABEL;

import net.trackmate.graph.ref.AbstractVertexPool;
import net.trackmate.pool.ByteMappedElement;
import net.trackmate.revised.bdv.overlay.util.JamaEigenvalueDecomposition;
import net.trackmate.revised.model.AbstractSpot;
import net.trackmate.revised.model.HasLabel;


// TODO: replace Jama stuff by something that doesn't allocate extra memory

/**
 * {@link AbstractSpot} implementation where the spot shape is stored in a
 * covariance matrix.
 *
 * @author Tobias Pietzsch
 */
public class Spot extends AbstractSpot< Spot, Link, ByteMappedElement, ModelGraph > implements HasLabel
{
	// Copied to be package-visible.
	protected static final int X_OFFSET = AbstractSpot.X_OFFSET;
	protected static final int COVARIANCE_OFFSET = AbstractSpot.sizeInBytes( 3 );
	protected static final int BOUNDING_SPHERE_RADIUS_SQUARED_OFFSET = COVARIANCE_OFFSET + 6 * DOUBLE_SIZE;
	protected static final int SIZE_IN_BYTES = BOUNDING_SPHERE_RADIUS_SQUARED_OFFSET + DOUBLE_SIZE;

	public static final double nSigmas = 2;
	public static final double nSigmasSquared = nSigmas * nSigmas;

	private final JamaEigenvalueDecomposition eig = new JamaEigenvalueDecomposition( 3 );

	/**
	 * Initialize a new {@link Spot} with a spherical shape.
	 * <p>
	 * <em>Note that this is equivalent to a constructor. It should be only
	 * called on newly created {@link Spot}s, and only once.</em>
	 *
	 * @param timepointId
	 *            the time-point id to add the spot to in the spatio-temporal
	 *            index.
	 * @param pos
	 *            the position of the spot.
	 * @param radius
	 *            the radius of the spot.
	 * @return this {@link Spot}.
	 */
	public Spot init( final int timepointId, final double[] pos, final double radius )
	{
		final double eigVal = radius * radius / nSigmasSquared;
		final double[][] cov = new double[][] {
				{ eigVal, 0., 0. },
				{ 0., eigVal, 0. },
				{ 0., 0., eigVal }
		};
		final double boundingSphereRadiusSquared = radius * radius;

		for ( int d = 0; d < n; ++d )
			setCoord( pos[ d ], d );
		setCovariance( cov );
		setBoundingSphereRadiusSquared( boundingSphereRadiusSquared );
		setTimepointId( timepointId );
		super.initDone();
		return this;
	}

	/**
	 * Initialize a new {@link Spot}.
	 * <p>
	 * <em>Note that this is equivalent to a constructor. It should be only
	 * called on newly created {@link Spot}s, and only once.</em>
	 *
	 * @param timepointId
	 *            the time-point id to add the spot to in the spatio-temporal
	 *            index.
	 * @param pos
	 *            the position of the spot.
	 * @param cov
	 *            the covariance matrix that determines the shape of the
	 *            ellipsoid, as a {@code double[][]} (line, column). Since the
	 *            covariance matrix is symmetric, only the top-left of the
	 *            specified matrix is read.
	 * @return this {@link Spot}.
	 */
	public Spot init( final int timepointId, final double[] pos, final double[][] cov )
	{
		eig.decomposeSymmetric( cov );
		final double[] eigVals = eig.getRealEigenvalues();
		double max = 0;
		for ( int k = 0; k < eigVals.length; k++ )
			max = Math.max( max, eigVals[ k ] );
		final double boundingSphereRadiusSquared = max * nSigmasSquared;

		setPosition( pos );
		setCovariance( cov );
		setBoundingSphereRadiusSquared( boundingSphereRadiusSquared );
		setTimepointId( timepointId );
		super.initDone();
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
		modelGraph.notifyBeforeVertexCovarianceChange( this );
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
		return String.format( "Spot( %d, X=%.2f, Y=%.2f, Z=%.2f, tp=%d )",
				getInternalPoolIndex(),
				getDoublePosition( 0 ),
				getDoublePosition( 1 ),
				getDoublePosition( 2 ),
				getTimepoint() );
	}

	Spot( final AbstractVertexPool< Spot, Link, ByteMappedElement > pool )
	{
		super( pool, 3 );
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

	protected void notifyVertexAdded()
	{
		super.initDone();
	}
}
