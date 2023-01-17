/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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

import java.util.function.Predicate;

import org.mastodon.mamut.model.Spot;

import net.imglib2.Localizable;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

/**
 * Static utilities to help building test on
 * {@link org.mastodon.mamut.model.Spot} features.
 * 
 * @author Jean--Yves Tinevez
 *
 */
public class SpotTestUtils
{

	/**
	 * Returns a new predicate that returns <code>true</code> if a source with
	 * the specified transform position is inside the specified spot. Adapted
	 * from {@link EllipsoidIterable} by Tobias.
	 * 
	 * @param spot
	 *            the spot to test.
	 * @param sourceTransform
	 *            the transform of the source.
	 * @return a new predicate.
	 */
	public static final Predicate< Localizable > isInsideTest( final Spot spot,
			final AffineTransform3D sourceTransform )
	{
		/** Spot position in global coordinate system. */
		final double[] pos = new double[ 3 ];
		/** Spot covariance in global coordinate system. */
		final double[][] S = new double[ 3 ][ 3 ];
		/** Spot precision in global coordinate system. */
		final double[][] P = new double[ 3 ][ 3 ];
		// tmp
		final double[][] T = new double[ 3 ][ 3 ];
		// tmp
		final double[][] TS = new double[ 3 ][ 3 ];
		// tmp
		final double[] p = new double[ 3 ];
		// tmp
		final double[] diff = new double[ 3 ];

		// transform spot covariance into source coordinates
		spot.getCovariance( S );
		for ( int r = 0; r < 3; ++r )
			for ( int c = 0; c < 3; ++c )
				T[ r ][ c ] = sourceTransform.inverse().get( r, c );
		LinAlgHelpers.mult( T, S, TS );
		LinAlgHelpers.multABT( TS, T, S );

		// transform spot position into source coordinates
		spot.localize( pos );
		sourceTransform.inverse().apply( pos, pos );

		// inflate ellipsoid by .5 pixels on either side
		for ( int r = 0; r < 3; ++r )
			for ( int c = 0; c < 3; ++c )
				if ( r == c )
				{
					final double radius = Math.sqrt( S[ r ][ c ] );
					T[ r ][ c ] = ( radius + 0.5 ) / radius;
				}
				else
				{
					T[ r ][ c ] = 0;
				}
		LinAlgHelpers.mult( T, S, TS );
		LinAlgHelpers.multABT( TS, T, S );

		// get precision from covariance
		LinAlgHelpers.invertSymmetric3x3( S, P );
		return l -> {
			l.localize( p );
			LinAlgHelpers.subtract( pos, p, diff );
			LinAlgHelpers.mult( P, diff, p );
			final double d2 = LinAlgHelpers.dot( diff, p );
			return d2 < 1;
		};
	}
}
