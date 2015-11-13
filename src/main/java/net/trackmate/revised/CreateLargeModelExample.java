package net.trackmate.revised;

import net.trackmate.revised.model.mamut.Link;
import net.trackmate.revised.model.mamut.Model;
import net.trackmate.revised.model.mamut.Spot;

public class CreateLargeModelExample
{
	private static final int N_STARTING_CELLS = 6;

	private static final int N_DIVISIONS = 10; // 16;

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
		final Spot mother = model.getGraph().vertexRef();
		for ( int ic = 0; ic < N_STARTING_CELLS; ic++ )
		{
			final double angle = 2d * ic * Math.PI / N_STARTING_CELLS;
			final double vx = VELOCITY * Math.cos( angle );
			final double vy = VELOCITY * Math.sin( angle );

			final int nframes = N_DIVISIONS * N_FRAMES_PER_DIVISION;
			final double x = nframes * VELOCITY + vx;
			final double y = nframes * VELOCITY + vy;
			final double z = N_DIVISIONS * VELOCITY;

			final double[] pos = new double[] { x, y, z };
			final double[][] cov = new double[][] { { RADIUS, 0, 0 }, { 0, RADIUS, 0 }, { 0, 0, RADIUS } };
			model.addSpot( 0, pos, cov, mother );

			addBranch( mother, vx, vy, 1 );
		}
		model.getGraph().releaseRef( mother );
		return model;
	}

	private void addBranch( final Spot start, final double vx, final double vy, final int iteration )
	{
		if ( iteration >= N_DIVISIONS ) { return; }

		final Spot previousSpot = model.getGraph().vertexRef();
		final Spot spot = model.getGraph().vertexRef();
		final Spot daughter = model.getGraph().vertexRef();
		final Link link = model.getGraph().edgeRef();

		final double[] pos = new double[ 3 ];
		final double[][] cov = new double[][] { { RADIUS, 0, 0 }, { 0, RADIUS, 0 }, { 0, 0, RADIUS } };

		// Extend
		previousSpot.refTo( start );
		for ( int it = 0; it < N_FRAMES_PER_DIVISION; it++ )
		{
			pos[ 0 ] = previousSpot.getDoublePosition( 0 ) + vx;
			pos[ 1 ] = previousSpot.getDoublePosition( 1 ) + vy;
			pos[ 2 ] = previousSpot.getDoublePosition( 2 );
			final int frame = previousSpot.getTimepoint() + 1;

			model.addSpot( frame, pos, cov, spot );
			model.addLink( previousSpot, spot, link );
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

			final int frame = previousSpot.getTimepoint() + 1;

			pos[ 0 ] = x;
			pos[ 1 ] = y;
			pos[ 2 ] = z;

			model.addSpot( frame, pos, cov, daughter );
			model.addLink( previousSpot, daughter, link );

			addBranch( daughter, vx, vy, iteration + 1 );
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
		System.out.println( String.format( "Total memory used by the model: %.1f MB", ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) / 1e6d ) );
	}
}
