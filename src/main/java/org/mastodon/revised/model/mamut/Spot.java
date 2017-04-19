package org.mastodon.revised.model.mamut;

import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.PoolObjectAttributeSerializer;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.pool.attributes.DoubleArrayAttribute;
import org.mastodon.pool.attributes.DoubleAttribute;
import org.mastodon.revised.bdv.overlay.util.JamaEigenvalueDecomposition;
import org.mastodon.revised.model.AbstractSpot;
import org.mastodon.revised.model.AbstractSpotPool;
import org.mastodon.revised.model.AbstractSpotPool.AbstractSpotLayout;
import org.mastodon.revised.model.HasLabel;
import org.mastodon.revised.model.mamut.Spot.SpotPool;
import org.mastodon.undo.attributes.AttributeSerializer;

/**
 * {@link AbstractSpot} implementation where the spot shape is stored in a
 * covariance matrix.
 *
 * @author Tobias Pietzsch
 */
public final class Spot extends AbstractSpot< Spot, Link, SpotPool, ByteMappedElement, ModelGraph > implements HasLabel
{
	public static class SpotLayout extends AbstractSpotLayout
	{
		public SpotLayout()
		{
			super( 3 );
		}

		final DoubleArrayField covariance = doubleArrayField( 6 );
		final DoubleField boundingSphereRadiusSqu = doubleField();
	}

	public static final SpotLayout layout = new SpotLayout();

	static class SpotPool extends AbstractSpotPool< Spot, Link, ByteMappedElement, ModelGraph >
	{
		final DoubleArrayAttribute< Spot > covariance = new DoubleArrayAttribute<>( layout.covariance );
		final DoubleAttribute< Spot > boundingSphereRadiusSqu = new DoubleAttribute<>( layout.boundingSphereRadiusSqu );

		SpotPool( final int initialCapacity )
		{
			super( initialCapacity, layout, Spot.class, SingleArrayMemPool.factory( ByteMappedElementArray.factory ) );
		}

		@Override
		protected Spot createEmptyRef()
		{
			return new Spot( this );
		}
	}

	private final JamaEigenvalueDecomposition eig = new JamaEigenvalueDecomposition( 3 );

	static AttributeSerializer< Spot > createCovarianceAttributeSerializer()
	{
		/*
		 * Note: Because the {@link #getBoundingSphereRadiusSquared() radius} can
		 * only be changed through the covariance, it is serialized as part of the
		 * covariance attribute.
		 */
		return new PoolObjectAttributeSerializer< Spot >(
				layout.covariance.getOffset(),
				layout.covariance.getSizeInBytes() + layout.boundingSphereRadiusSqu.getSizeInBytes() )
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

	private void setCovarianceInternal( final double[][] cov )
	{
		int i = 0;
		for( int row = 0; row < 3; ++row )
			for ( int col = row; col < 3; ++col )
				pool.covariance.setQuiet( this, i++, cov[ row ][ col ] );
	}

	private void getCovarianceInternal( final double[][] cov )
	{
		int i = 0;
		for( int row = 0; row < 3; ++row )
		{
			cov[ row ][ row ] = pool.covariance.get( this, i++ );
			for ( int col = row + 1; col < 3; ++col )
				cov[ col ][ row ] = cov[ row ][ col ] = pool.covariance.get( this, i++ );
		}
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
		pool.boundingSphereRadiusSqu.setQuiet( this, radius * radius );

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
		pool.boundingSphereRadiusSqu.setQuiet( this, radiusSquaredFromCovariance( cov ) );

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
		pool.boundingSphereRadiusSqu.setQuiet( this, radiusSquaredFromCovariance( cov ) );
		modelGraph.notifyRadiusChanged( this );
	}

	public double getBoundingSphereRadiusSquared()
	{
		return pool.boundingSphereRadiusSqu.get( this );
	}

	@Override
	public String getLabel()
	{
		if ( modelGraph.VERTEX_LABEL.isSet( this ) )
			return modelGraph.VERTEX_LABEL.get( this );
		else
			return Integer.toString( getInternalPoolIndex() );
	}

	@Override
	public void setLabel( final String label )
	{
		modelGraph.VERTEX_LABEL.set( this, label );
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
