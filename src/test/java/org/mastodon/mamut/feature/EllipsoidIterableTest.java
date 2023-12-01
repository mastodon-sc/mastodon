/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Test;
import org.mastodon.mamut.model.ModelGraph;

import bdv.util.RandomAccessibleIntervalSource;
import bdv.viewer.Source;
import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Util;

public class EllipsoidIterableTest
{

	/**
	 * Test that we report the spot center position in source coordinates
	 * properly.
	 */
	@Test
	public void testCenter()
	{
		/*
		 * Creates an anisotropic calibration transform.
		 */
		final AffineTransform3D transform = new AffineTransform3D();
		transform.set(
				1., 0, 0, 0,
				0, 1., 0, 0,
				0, 0, 2., 0 );

		/*
		 * An empty source.
		 */
		final Img< UnsignedByteType > img = ArrayImgs.unsignedBytes( 100, 100, 50 );
		for ( final UnsignedByteType p : img )
			p.setZero();
		final Source< UnsignedByteType > source =
				new RandomAccessibleIntervalSource<>( img, new UnsignedByteType(), transform, "Test source" );

		/*
		 * Now create a model graph with a few test spots (non overlapping).
		 */
		final ModelGraph graph = new ModelGraph();
		graph.addVertex().init( 0,
				new double[] { 50, 50, 50 },
				new double[][] {
						{ 210, 100, 0 },
						{ 100, 110, 10 },
						{ 0, 10, 100 }
				} );
		graph.addVertex().init( 0,
				new double[] { 20, 80, 40 },
				new double[][] {
						{ 90, 0, 0 },
						{ 0, 90, 0 },
						{ 0, 0, 500 }
				} );
		graph.addVertex().init( 0,
				new double[] { 40, 10, 40 },
				new double[][] {
						{ 90, -80, 0 },
						{ -80, 90, 0 },
						{ 0, 0, 90 }
				} );

		/*
		 * We now create an EllipsoidIterable and re-use it for each spot. For
		 * each spot, we iterate inside pixels and **increment** their value by
		 * 1.
		 */
		final EllipsoidIterable< UnsignedByteType > ellipsoidIter = new EllipsoidIterable<>( source );
		final double[] realpos = new double[ 3 ];
		final long[] pos = new long[ 3 ];
		final double[] spotRealpos = new double[ 3 ];
		final long[] spotPos = new long[ 3 ];
		graph.vertices().forEach( spot -> {

			spot.localize( spotRealpos );
			for ( int d = 0; d < spotRealpos.length; d++ )
				spotPos[ d ] = Math.round( spotRealpos[ d ] );

			ellipsoidIter.reset( spot );

			ellipsoidIter.localize( realpos );
			transform.apply( realpos, realpos );
			assertArrayEquals( "Spot center and iterable real position differ", spotRealpos, realpos, 1e-9 );

			ellipsoidIter.localize( pos );
			transform.inverse().apply( spotRealpos, spotRealpos );
			for ( int d = 0; d < 3; d++ )
				assertEquals( "Spot center and iterable integer position differ", Math.round( spotRealpos[ d ] ),
						pos[ d ] );
		} );
	}

	/**
	 * Tests that we iterate over pixels at most once, and that we iterate
	 * inside spots.
	 */
	@Test
	public void testIteration()
	{
		/*
		 * Creates an anisotropic calibration transform.
		 */
		final AffineTransform3D transform = new AffineTransform3D();
		transform.set(
				1., 0, 0, 0,
				0, 1., 0, 0,
				0, 0, 2., 0 );

		/*
		 * An empty source.
		 */
		final Img< UnsignedByteType > img = ArrayImgs.unsignedBytes( 100, 100, 50 );
		for ( final UnsignedByteType p : img )
			p.setZero();
		final Source< UnsignedByteType > source =
				new RandomAccessibleIntervalSource<>( img, new UnsignedByteType(), transform, "Test source" );

		/*
		 * Now create a model graph with a few test spots (non overlapping).
		 */
		final ModelGraph graph = new ModelGraph();
		graph.addVertex().init( 0,
				new double[] { 50, 50, 50 },
				new double[][] {
						{ 210, 100, 0 },
						{ 100, 110, 10 },
						{ 0, 10, 100 }
				} );
		graph.addVertex().init( 0,
				new double[] { 20, 80, 40 },
				new double[][] {
						{ 90, 0, 0 },
						{ 0, 90, 0 },
						{ 0, 0, 500 }
				} );
		graph.addVertex().init( 0,
				new double[] { 40, 10, 40 },
				new double[][] {
						{ 90, -80, 0 },
						{ -80, 90, 0 },
						{ 0, 0, 90 }
				} );

		/*
		 * We now create an EllipsoidIterable and re-use it for each spot. For
		 * each spot, we iterate inside pixels and **increment** their value by
		 * 1.
		 */
		final EllipsoidIterable< UnsignedByteType > ellipsoidIter = new EllipsoidIterable<>( source );
		graph.vertices().forEach( spot -> {
			ellipsoidIter.reset( spot );
			ellipsoidIter.forEach( t -> t.inc() );
		} );

		/*
		 * Now we iterate the image and make sure we have 0 or 1, and if we have
		 * 1 that we are inside a spot.
		 */

		final List< Predicate< Localizable > > testers = graph.vertices()
				.stream()
				.map( spot -> SpotTestUtils.isInsideTest( spot, transform ) )
				.collect( Collectors.toList() );

		final Cursor< UnsignedByteType > cursor = img.localizingCursor();
		while ( cursor.hasNext() )
		{
			cursor.fwd();
			final int val = cursor.get().get();
			if ( val > 1 )
				fail( "Expected values to be 0 or 1, but found " + val + " at location "
						+ Util.printCoordinates( cursor ) );

			/*
			 * This part of the test if a bit circular, since we test whether a
			 * coordinate is inside a spot with a code taken from the code we
			 * test. This test is therefore no more than a regression test.
			 */

			if ( val == 0 )
			{
				// Test that we are outside any spot.
				for ( final Predicate< Localizable > tester : testers )
					assertFalse(
							"Found position " + Util.printCoordinates( cursor )
									+ ", to be inside a spot, expected it to be outside",
							tester.test( cursor ) );
			}
			else if ( val == 1 )
			{
				// Test that we are inside a spot.
				boolean inside = false;
				for ( final Predicate< Localizable > tester : testers )
					inside = inside || tester.test( cursor );

				assertTrue(
						"Found position " + Util.printCoordinates( cursor )
								+ ", to be outside a spot, expected it to be inside",
						inside );
			}
		}
	}
}
