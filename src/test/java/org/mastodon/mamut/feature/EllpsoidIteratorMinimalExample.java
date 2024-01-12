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

import org.mastodon.mamut.model.ModelGraph;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvStackSource;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class EllpsoidIteratorMinimalExample
{
	public static void main( final String[] args )
	{
		// Create ArrayImg to act as source pixel data.
		// We show it as a Source in BDV with a calibration of (X=1, Y=1, Z=2) as a minimal test whether the EllipsoidIterator handles source transforms correctly.
		// The Img is filled with a checkerboard pattern, so that we can see where the voxel raster is in BDV.
		final Img< UnsignedByteType > img = ArrayImgs.unsignedBytes( 100, 100, 50 );
		final Cursor< UnsignedByteType > cursor = img.cursor();
		while ( cursor.hasNext() )
		{
			cursor.fwd();
			final int s = cursor.getIntPosition( 0 ) + cursor.getIntPosition( 1 ) + cursor.getIntPosition( 2 );
			cursor.get().set( s % 2 == 0 ? 32 : 64 );
		}
		final BdvStackSource< UnsignedByteType > bdv =
				BdvFunctions.show( img, "img", Bdv.options().sourceTransform( 1, 1, 2 ) );

		// Now create a model graph with a few test spots for EllipsoidIterator
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

		// We now create an EllipsoidIterable and re-use it for each spot.
		// For each spot, we iterate inside pixels and set them to 255.
		final EllipsoidIterable< UnsignedByteType > ellipsoidIter =
				new EllipsoidIterable<>( bdv.getSources().get( 0 ).getSpimSource() );
		graph.vertices().forEach( spot -> {
			ellipsoidIter.reset( spot );
			ellipsoidIter.forEach( t -> t.set( 255 ) );
		} );
	}
}
