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
package org.mastodon.graph.revised;

import org.mastodon.mamut.io.importer.ModelImporter;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;

public class CreateLargeModelExample
{
	private static final int N_STARTING_CELLS = 6;

	private static final int N_DIVISIONS = 17;

	private static final int N_FRAMES_PER_DIVISION = 5;

	private static final double VELOCITY = 5;

	private static final double RADIUS = 3;

	private final Model model;

	public CreateLargeModelExample()
	{
		this.model = new Model();
	}

	public Model run()
	{
		return run( N_STARTING_CELLS, N_DIVISIONS, N_FRAMES_PER_DIVISION );
	}

	public Model run( final int nStartingCells, final int nDivisions, final int nFramesPerDivision )
	{
		new ModelImporter( model )
		{
			{
				startImport();
			}
		};
		final Spot tmp = model.getGraph().vertexRef();
		for ( int ic = 0; ic < nStartingCells; ic++ )
		{
			final double angle = 2d * ic * Math.PI / N_STARTING_CELLS;
			final double vx = VELOCITY * Math.cos( angle );
			final double vy = VELOCITY * Math.sin( angle );

			//			final int nframes = N_DIVISIONS * N_FRAMES_PER_DIVISION;
			final double x = 0.; // nframes * VELOCITY + vx;
			final double y = 0.; // nframes * VELOCITY + vy;
			final double z = N_DIVISIONS * VELOCITY;

			final double[] pos = new double[] { x, y, z };
			final double[][] cov = new double[][] { { RADIUS, 0, 0 }, { 0, RADIUS, 0 }, { 0, 0, RADIUS } };

			final Spot mother = model.getGraph().addVertex( tmp ).init( 0, pos, cov );
			addBranch( mother, vx, vy, 1, nDivisions, nFramesPerDivision );
		}
		model.getGraph().releaseRef( tmp );
		new ModelImporter( model )
		{
			{
				finishImport();
			}
		};
		return model;
	}

	private void addBranch( final Spot start, final double vx, final double vy, final int iteration,
			final int nDivisions, final int nFramesPerDivision )
	{
		if ( iteration >= nDivisions )
		{ return; }

		final Spot previousSpot = model.getGraph().vertexRef();
		final Spot spot = model.getGraph().vertexRef();
		final Spot daughter = model.getGraph().vertexRef();
		final Link link = model.getGraph().edgeRef();

		final double[] pos = new double[ 3 ];
		final double[][] cov = new double[][] { { RADIUS, 0, 0 }, { 0, RADIUS, 0 }, { 0, 0, RADIUS } };

		// Extend
		previousSpot.refTo( start );
		for ( int it = 0; it < nFramesPerDivision; it++ )
		{
			pos[ 0 ] = previousSpot.getDoublePosition( 0 ) + vx;
			pos[ 1 ] = previousSpot.getDoublePosition( 1 ) + vy;
			pos[ 2 ] = previousSpot.getDoublePosition( 2 );
			final int frame = previousSpot.getTimepoint() + 1;

			model.getGraph().addVertex( spot ).init( frame, pos, cov );
			model.getGraph().addEdge( previousSpot, spot, link ).init();

			previousSpot.refTo( spot );
		}

		// Divide
		for ( int id = 0; id < 2; id++ )
		{
			final double sign = id == 0 ? 1 : -1;
			final double x;
			final double y;
			final double z;
			if ( iteration % 2 == 0 )
			{
				x = previousSpot.getDoublePosition( 0 );
				y = previousSpot.getDoublePosition( 1 );
				z = previousSpot.getDoublePosition( 2 ) + sign * VELOCITY * ( 1 - 0.5d * iteration / nDivisions ) * 2;
			}
			else
			{
				x = previousSpot.getDoublePosition( 0 ) - sign * vy * ( 1 - 0.5d * iteration / nDivisions ) * 2;
				y = previousSpot.getDoublePosition( 1 ) + sign * vx * ( 1 - 0.5d * iteration / nDivisions ) * 2;
				z = previousSpot.getDoublePosition( 2 );
			}

			final int frame = previousSpot.getTimepoint() + 1;

			pos[ 0 ] = x;
			pos[ 1 ] = y;
			pos[ 2 ] = z;

			model.getGraph().addVertex( daughter ).init( frame, pos, cov );
			model.getGraph().addEdge( previousSpot, daughter, link ).init();

			addBranch( daughter, vx, vy, iteration + 1, nDivisions, nFramesPerDivision );
		}

		model.getGraph().releaseRef( previousSpot );
		model.getGraph().releaseRef( spot );
		model.getGraph().releaseRef( daughter );
		model.getGraph().releaseRef( link );
	}

	public static void main( final String[] args )
	{
		final CreateLargeModelExample clme = new CreateLargeModelExample();
		final long start = System.currentTimeMillis();
		final Model model = clme.run();
		final long end = System.currentTimeMillis();
		System.out.println( "Model created in " + ( end - start ) + " ms." );
		System.out.println( "Total number of spots: " + model.getGraph().vertices().size() );
		System.out.println( String.format( "Total memory used by the model: %.1f MB",
				( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) / 1e6d ) );
	}
}
