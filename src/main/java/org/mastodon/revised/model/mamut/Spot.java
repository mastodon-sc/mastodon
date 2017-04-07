package org.mastodon.revised.model.mamut;

import static org.mastodon.pool.ByteUtils.DOUBLE_SIZE;
import static org.mastodon.revised.model.mamut.ModelFeatures.LABEL;

import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.PoolObjectAttributeSerializer;
import org.mastodon.revised.bdv.overlay.util.JamaEigenvalueDecomposition;
import org.mastodon.revised.model.AbstractSpot;
import org.mastodon.revised.model.HasLabel;
import org.mastodon.revised.model.mamut.ModelGraph.SpotPool;
import org.mastodon.undo.attributes.AttributeUndoSerializer;

/**
 * {@link AbstractSpot} implementation where the spot shape is stored in a
 * covariance matrix.
 *
 * @author Tobias Pietzsch
 */
public final class Spot extends AbstractSpot< Spot, Link, SpotPool, ByteMappedElement, ModelGraph > implements HasLabel
{
	// Copied to be package-visible.
	protected static final int X_OFFSET = AbstractSpot.X_OFFSET;
	protected static final int COVARIANCE_OFFSET = AbstractSpot.sizeInBytes( 3 );
	protected static final int BOUNDING_SPHERE_RADIUS_SQUARED_OFFSET = COVARIANCE_OFFSET + 6 * DOUBLE_SIZE;
	protected static final int SIZE_IN_BYTES = BOUNDING_SPHERE_RADIUS_SQUARED_OFFSET + DOUBLE_SIZE;

	private final JamaEigenvalueDecomposition eig = new JamaEigenvalueDecomposition( 3 );

	static AttributeUndoSerializer< Spot > createCovarianceAttributeSerializer()
	{
		/*
		 * Note: Because the {@link #getBoundingSphereRadiusSquared() radius} can
		 * only be changed through the covariance, it is serialized as part of the
		 * covariance attribute.
		 */
		return new PoolObjectAttributeSerializer< Spot >( COVARIANCE_OFFSET, 6 * DOUBLE_SIZE + DOUBLE_SIZE)
		{
			@Override
			public void notifySet( final Spot spot )
			{
				spot.modelGraph.notifyRadiusChanged( spot );
			}
		};
	}

	private double radiusSquaredFromCovariance( final double[][] cov )
	{
		eig.decomposeSymmetric( cov );
		final double[] eigVals = eig.getRealEigenvalues();
		double max = 0;
		for ( int k = 0; k < eigVals.length; k++ )
			max = Math.max( max, eigVals[ k ] );
		return max;
	}

	private void covarianceFromRadiusSquared( final double rsqu, final double[][] cov )
	{
		for( int row = 0; row < 3; ++row )
			for( int col = 0; col < 3; ++col )
				cov[ row ][ col ] = ( row == col ) ? rsqu : 0;
	}

	private void setCovarianceEntryInternal( final double value, final int d )
	{
		access.putDouble( value, COVARIANCE_OFFSET + d * DOUBLE_SIZE );
	}

	private double getCovarianceEntryInternal( final int d )
	{
		return access.getDouble( COVARIANCE_OFFSET + d * DOUBLE_SIZE );
	}

	private void setCovarianceInternal( final double[][] cov )
	{
		int i = 0;
		for( int row = 0; row < 3; ++row )
			for ( int col = row; col < 3; ++col )
				setCovarianceEntryInternal( cov[ row ][ col ], i++ );
	}

	private void getCovarianceInternal( final double[][] cov )
	{
		int i = 0;
		for( int row = 0; row < 3; ++row )
		{
			cov[ row ][ row ] = getCovarianceEntryInternal( i++ );
			for ( int col = row + 1; col < 3; ++col )
				cov[ col ][ row ] = cov[ row ][ col ] = getCovarianceEntryInternal( i++ );
		}
	}

	private void setBoundingSphereRadiusSquaredInternal( final double value )
	{
		access.putDouble( value, BOUNDING_SPHERE_RADIUS_SQUARED_OFFSET );
	}

	private double getBoundingSphereRadiusSquaredInternal()
	{
		return access.getDouble( BOUNDING_SPHERE_RADIUS_SQUARED_OFFSET );
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

	void notifyVertexAdded()
	{
		super.initDone();
	}

	/*
	 * Public API
	 */

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
		super.partialInit( timepointId, pos );

		final double[][] cov = new double[ 3 ][ 3 ];
		covarianceFromRadiusSquared( radius * radius, cov );
		setCovarianceInternal( cov );
		setBoundingSphereRadiusSquaredInternal( radius * radius );

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
		super.partialInit( timepointId, pos );

		setCovarianceInternal( cov );
		setBoundingSphereRadiusSquaredInternal( radiusSquaredFromCovariance( cov ) );

		super.initDone();
		return this;
	}

	public void getCovariance( final double[][] cov )
	{
		getCovarianceInternal( cov );
	}

	public void setCovariance( final double[][] cov )
	{
		modelGraph.notifyBeforeVertexCovarianceChange( this );
		setCovarianceInternal( cov );
		setBoundingSphereRadiusSquaredInternal( radiusSquaredFromCovariance( cov ) );
		modelGraph.notifyRadiusChanged( this );
	}

	public double getBoundingSphereRadiusSquared()
	{
		return getBoundingSphereRadiusSquaredInternal();
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

	Spot( final SpotPool pool )
	{
		super( pool );
	}
}
