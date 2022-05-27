/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.feature;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.Collections;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.Spot;
import org.mastodon.views.bdv.overlay.util.JamaEigenvalueDecomposition;
import org.scijava.plugin.Plugin;

public class SpotRadiusFeature implements Feature< Spot >
{

	private static final String KEY = "Spot radius";

	private static final String HELP_STRING = "Computes the spot equivalent radius. "
			+ "This is the radius of the sphere that would have the same volume "
			+ "that of the spot.";

	private static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY, Dimension.LENGTH );

	public static final Spec SPEC = new Spec();

	private final String units;

	public SpotRadiusFeature( final String units )
	{
		this.units = units;
	}

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< SpotRadiusFeature, Spot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					SpotRadiusFeature.class,
					Spot.class,
					Multiplicity.SINGLE,
					PROJECTION_SPEC );
		}
	}

	@Override
	public FeatureProjection< Spot > project( final FeatureProjectionKey key )
	{
		return key( PROJECTION_SPEC ).equals( key ) ? new MyProjection( units ) : null;
	}

	@Override
	public Set< FeatureProjection< Spot > > projections()
	{
		return Collections.singleton( new MyProjection( units ) );
	}

	@Override
	public Spec getSpec()
	{
		return SPEC;
	}

	@Override
	public void invalidate( final Spot spot )
	{}

	private static final class MyProjection implements FeatureProjection< Spot >
	{

		private final double[][] cov = new double[ 3 ][ 3 ];

		private final JamaEigenvalueDecomposition eig = new JamaEigenvalueDecomposition( 3 );

		private final String units;

		public MyProjection( final String units )
		{
			this.units = units;
		}

		@Override
		public FeatureProjectionKey getKey()
		{
			return key( PROJECTION_SPEC );
		}

		@Override
		public boolean isSet( final Spot obj )
		{
			return true;
		}

		@Override
		public synchronized double value( final Spot spot )
		{
			spot.getCovariance( cov );
			eig.decomposeSymmetric( cov );
			final double[] eigVals = eig.getRealEigenvalues();
			double volume = 4. / 3. * Math.PI;
			for ( int k = 0; k < eigVals.length; k++ )
			{
				final double semiAxis = Math.sqrt( eigVals[ k ] );
				volume *= semiAxis;
			}
			final double radius = Math.pow( volume * 3. / 4. / Math.PI, 1. / 3. );
			return radius;
		}

		@Override
		public String units()
		{
			return units;
		}
	}
}
