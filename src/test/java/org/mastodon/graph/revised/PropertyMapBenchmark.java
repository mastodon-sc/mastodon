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
package org.mastodon.graph.revised;

import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.IntPropertyMap;

import net.imglib2.util.BenchmarkHelper;

public class PropertyMapBenchmark
{
	public static void main( final String[] args )
	{
		final int N_RUNS = 100;

		System.out.println( "Creating model." );
		final long s = System.currentTimeMillis();
		final Model model = new CreateLargeModelExample().run( 6, 18, 8 );
		final int size = model.getGraph().vertices().size();
		final long e = System.currentTimeMillis();
		System.out.println( String.format( "Created a model with %d spots in %.1f s.", size, ( ( double ) e - s ) / 1000. ) );

		final int[] storage = new int[ size ];

		final IntPropertyMap< Spot > POSITIVE_NUMBER = new IntPropertyMap<>( model.getGraph().vertices(), -1 );

		System.out.println();
		final long s1 = System.currentTimeMillis();
		BenchmarkHelper.benchmarkAndPrint( N_RUNS, false, () -> putPropertyValue( model, POSITIVE_NUMBER ) );
		final long e1 = System.currentTimeMillis();
		System.out.println( String.format( "Put a int property value in %d spots in %.1f ms.", size, ( ( double ) e1 - s1 ) / N_RUNS ) );

		System.out.println();
		final long s2 = System.currentTimeMillis();
		BenchmarkHelper.benchmarkAndPrint( N_RUNS, false, () -> readGraphValue( model, storage ) );
		final long e2 = System.currentTimeMillis();
		System.out.println( String.format( "Read a int graph value in %d spots in %.1f ms.", size, ( ( double ) e2 - s2 ) / N_RUNS ) );

		System.out.println();
		final long s3 = System.currentTimeMillis();
		BenchmarkHelper.benchmarkAndPrint( N_RUNS, false, () -> readPropertyValue( model, storage, POSITIVE_NUMBER ) );
		final long e3 = System.currentTimeMillis();
		System.out.println( String.format( "Read a int property value in %d spots in %.1f ms.", size, ( ( double ) e3 - s3 ) / N_RUNS ) );

	}

	private static void readPropertyValue( final Model model, final int[] storage, final IntPropertyMap< Spot > feature )
	{
		int index = 0;
		for ( final Spot spot : model.getGraph().vertices() )
		{
			storage[ index++ ] = feature.getInt( spot );
		}
	}

	private static void putPropertyValue( final Model model, final IntPropertyMap< Spot > feature )
	{
		for ( final Spot spot : model.getGraph().vertices() )
		{
			feature.set( spot, spot.getTimepoint() );
		}
	}

	private static void readGraphValue( final Model model, final int[] storage )
	{
		int index = 0;
		for ( final Spot spot : model.getGraph().vertices() )
		{
			storage[ index++ ] = spot.getTimepoint();
		}
	}

}
