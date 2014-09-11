package net.trackmate;

import net.trackmate.model.Spot;
import net.trackmate.model.SpotCollection;

public class CreateLargeModelExample
{
	private static final int N_STARTING_CELLS = 50;

	private static final int N_DIVISIONS = 14;

	private static final int N_FRAMES_PER_DIVISION = 5;

	private static final double VELOCITY = 5;

	private static final double RADIUS = 3;

	private final SpotCollection spotCollection;

	public CreateLargeModelExample()
	{
		this.spotCollection = new SpotCollection( 3000000 );
		final long start = System.currentTimeMillis();
		run();
		final long end = System.currentTimeMillis();
		System.out.println( "Model created in " + ( end - start ) + " ms." );
		System.out.println( "Total number of spots: " + spotCollection.numSpots() );
		System.out.println( String.format( "Total memory used by the model: %.1f MB", ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) / 1e6d ) );
	}

	public void run()
	{
		final Spot mother = spotCollection.getTmpSpotRef();
		for ( int ic = 0; ic < N_STARTING_CELLS; ic++ )
		{
			final double angle = 2d * ic * Math.PI / N_STARTING_CELLS;
			final double vx = VELOCITY * Math.cos( angle );
			final double vy = VELOCITY * Math.sin( angle );

			final int nframes = N_DIVISIONS * N_FRAMES_PER_DIVISION;
			final double x = nframes * VELOCITY + vx;
			final double y = nframes * VELOCITY + vy;
			final double z = N_DIVISIONS * VELOCITY;

			spotCollection.createSpot( mother ).init( x, y, z, RADIUS, angle );
//			model.addSpotTo( mother, 0 ); // TODO

			addBranch( mother, vx, vy, 1 );
		}
		spotCollection.releaseTmpSpotRef( mother );
	}

	private void addBranch( final Spot start, final double vx, final double vy, final int iteration )
	{
		if ( iteration >= N_DIVISIONS ) { return; }

		final Spot previousSpot = spotCollection.getTmpSpotRef();
		final Spot spot = spotCollection.getTmpSpotRef();
		final Spot daughter = spotCollection.getTmpSpotRef();

		// Extend
		previousSpot.referenceTo( start );
		for ( int it = 0; it < N_FRAMES_PER_DIVISION; it++ )
		{
			final double x = previousSpot.getDoublePosition( 0 ) + vx;
			final double y = previousSpot.getDoublePosition( 1 ) + vy;
			final double z = previousSpot.getDoublePosition( 2 );
			spotCollection.createSpot( spot ).init( x, y, z, RADIUS, iteration );
			final int frame = previousSpot.getFrame() + 1;
			spot.setFrame( frame );
//			model.addSpotTo( spot, frame ); // TODO
			spotCollection.addEdge( previousSpot, spot ).init( iteration );
			previousSpot.referenceTo( spot );
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

			spotCollection.createSpot( daughter ).init( x, y, z, RADIUS, sign );
			final int frame = previousSpot.getFrame() + 1;
			daughter.setFrame( frame );
//			model.addSpotTo( daughter, frame ); // TODO
			spotCollection.addEdge( previousSpot, daughter ).init( sign );

			addBranch( daughter, vx, vy, iteration + 1 );
		}

		spotCollection.releaseTmpSpotRef( previousSpot );
		spotCollection.releaseTmpSpotRef( spot );
		spotCollection.releaseTmpSpotRef( daughter );
	}

	public static void main( final String[] args )
	{
		new CreateLargeModelExample();
	}
}