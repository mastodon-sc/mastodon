package net.trackmate;

import java.util.List;

import net.trackmate.model.Model;
import net.trackmate.model.ModelGraph;
import net.trackmate.model.SpotCovariance;
import net.trackmate.model.SpotList;

public class CreateLargeModelExample
{
	private static final int N_STARTING_CELLS = 50;

	private static final int N_DIVISIONS = 16;

	private static final int N_FRAMES_PER_DIVISION = 5;

	private static final double VELOCITY = 5;

	private static final double RADIUS = 3;

	private final Model model;

	public CreateLargeModelExample()
	{
		this.model = new Model( new ModelGraph< SpotCovariance >( new ModelGraph.SpotCovarianceFactory(), 2866900 ) );
		final long start = System.currentTimeMillis();
		run();
		final long end = System.currentTimeMillis();
		System.out.println( "Model created in " + ( end - start ) + " ms." );
		System.out.println( "Total number of spots: " + model.getGraph().numSpots() );
		final int lastFrame = model.frames().last();
		System.out.println( "Total number of cells in the last frame: " + model.getSpots( lastFrame ).size() );

		final List< SpotCovariance > lastTwo = new SpotList( model.getGraph() );
		lastTwo.addAll( model.getSpots( lastFrame ) );
		lastTwo.addAll( model.getSpots( lastFrame - 1 ) );
		System.out.println( "Total number of cells in the last two frames: " + lastTwo.size() );

		System.out.println( String.format( "Total memory used by the model: %.1f MB", ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) / 1e6d ) );
	}

	public void run()
	{
		final SpotCovariance mother = model.getGraph().vertexRef();
		for ( int ic = 0; ic < N_STARTING_CELLS; ic++ )
		{
			final double angle = 2d * ic * Math.PI / N_STARTING_CELLS;
			final double vx = VELOCITY * Math.cos( angle );
			final double vy = VELOCITY * Math.sin( angle );

			final int nframes = N_DIVISIONS * N_FRAMES_PER_DIVISION;
			final double x = nframes * VELOCITY + vx;
			final double y = nframes * VELOCITY + vy;
			final double z = N_DIVISIONS * VELOCITY;

			model.createSpot( 0, x, y, z, RADIUS, mother );

			addBranch( mother, vx, vy, 1 );
		}
		model.getGraph().releaseRef( mother );
	}

	private void addBranch( final SpotCovariance start, final double vx, final double vy, final int iteration )
	{
		if ( iteration >= N_DIVISIONS ) { return; }

		final SpotCovariance previousSpot = model.getGraph().vertexRef();
		final SpotCovariance spot = model.getGraph().vertexRef();
		final SpotCovariance daughter = model.getGraph().vertexRef();

		// Extend
		previousSpot.refTo( start );
		for ( int it = 0; it < N_FRAMES_PER_DIVISION; it++ )
		{
			final double x = previousSpot.getDoublePosition( 0 ) + vx;
			final double y = previousSpot.getDoublePosition( 1 ) + vy;
			final double z = previousSpot.getDoublePosition( 2 );
			final int frame = previousSpot.getTimePoint();
			model.createSpot( frame, x, y, z, RADIUS, spot );
			model.createLink( previousSpot, spot );
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
				z = previousSpot.getDoublePosition( 2 ) + sign * VELOCITY * ( 1 - 0.5d * iteration / N_DIVISIONS ) * 2;
			}
			else
			{
				x = previousSpot.getDoublePosition( 0 ) - sign * vy * ( 1 - 0.5d * iteration / N_DIVISIONS ) * 2;
				y = previousSpot.getDoublePosition( 1 ) + sign * vx * ( 1 - 0.5d * iteration / N_DIVISIONS )* 2;
				z = previousSpot.getDoublePosition( 2 );
			}

			final int frame = previousSpot.getTimePoint() + 1;
			model.createSpot( frame, x, y, z, RADIUS, daughter );
			model.createLink( previousSpot, daughter );

			addBranch( daughter, vx, vy, iteration + 1 );
		}

		model.getGraph().releaseRef( previousSpot );
		model.getGraph().releaseRef( spot );
		model.getGraph().releaseRef( daughter );
	}

	public static void main( final String[] args )
	{
		new CreateLargeModelExample();
	}
}
