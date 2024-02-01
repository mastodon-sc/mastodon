/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.model;

import org.mastodon.model.AbstractSpot;
import org.mastodon.model.HasLabel;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.views.bdv.overlay.util.JamaEigenvalueDecomposition;

/**
 * {@link AbstractSpot} implementation where the spot shape is stored in a
 * covariance matrix.
 *
 * @author Tobias Pietzsch
 */
public final class Spot extends AbstractSpot< Spot, Link, SpotPool, ByteMappedElement, ModelGraph > implements HasLabel
{
	private final JamaEigenvalueDecomposition eig = new JamaEigenvalueDecomposition( 3 );

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
		for ( int row = 0; row < 3; ++row )
			for ( int col = 0; col < 3; ++col )
				cov[ row ][ col ] = ( row == col ) ? rsqu : 0;
	}

	private void setCovarianceInternal( final double[][] cov )
	{
		int i = 0;
		for ( int row = 0; row < 3; ++row )
			for ( int col = row; col < 3; ++col )
				pool.covariance.setQuiet( this, i++, cov[ row ][ col ] );
	}

	private void getCovarianceInternal( final double[][] cov )
	{
		int i = 0;
		for ( int row = 0; row < 3; ++row )
		{
			cov[ row ][ row ] = pool.covariance.get( this, i++ );
			for ( int col = row + 1; col < 3; ++col )
				cov[ col ][ row ] = cov[ row ][ col ] = pool.covariance.get( this, i++ );
		}
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
		pool.covariance.notifyBeforePropertyChange( this );
		setCovarianceInternal( cov );
		pool.covariance.notifyPropertyChanged( this );

		pool.boundingSphereRadiusSqu.set( this, radiusSquaredFromCovariance( cov ) );
	}

	public double getBoundingSphereRadiusSquared()
	{
		return pool.boundingSphereRadiusSqu.get( this );
	}

	@Override
	public String getLabel()
	{
		if ( pool.label.isSet( this ) )
			return pool.label.get( this );
		else
			return Integer.toString( getInternalPoolIndex() );
	}

	@Override
	public void setLabel( final String label )
	{
		pool.label.set( this, label );
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
