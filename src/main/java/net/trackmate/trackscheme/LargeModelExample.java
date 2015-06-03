package net.trackmate.trackscheme;

import java.util.Random;

public class LargeModelExample
{
	private static final int N_STARTING_CELLS = 50;

	private static final int N_DIVISIONS = 16;

	private static final int N_FRAMES_PER_DIVISION = 7;

	private static final double TERMINATION_PROBABILITY = 0.01;

	public static void main( final String[] args )
	{
		new ShowTrackScheme( new LargeModelExample().graph );
	}

	public LargeModelExample()
	{
		graph = new TrackSchemeGraph();
		rand = new Random( 123104 );

		final long t0 = System.currentTimeMillis();
		for ( int i = 0; i < N_STARTING_CELLS; ++i )
		{
//			String label = Integer.toString( ++labelGenerator );
			final String label = generateRandomString( 3 );
			final TrackSchemeVertex mother = graph.addVertex().init( label, 0, false );
			addBranch( mother, 1 );
		}
		final long t1 = System.currentTimeMillis();
		System.out.println( labelGenerator + " vertices" );
		System.out.println( "model build in " + ( t1 - t0 ) + "ms" );
		System.out.println();
	}

	final private TrackSchemeGraph graph;

	private int labelGenerator;

	private final Random rand;

	private void addBranch( final TrackSchemeVertex start, final int iteration )
	{
		if ( iteration >= N_DIVISIONS ) { return; }

		final TrackSchemeVertex previousSpot = graph.vertexRef();
		final TrackSchemeVertex spot = graph.vertexRef();
		final TrackSchemeEdge edge = graph.edgeRef();

		// Extend
		previousSpot.refTo( start );
		for ( int it = 0; it < N_FRAMES_PER_DIVISION; it++ )
		{
			if ( rand.nextDouble() < TERMINATION_PROBABILITY ) { return; }

			graph.addVertex( spot ).init( Integer.toString( ++labelGenerator ), previousSpot.getTimePoint() + 1, false );
			graph.addEdge( previousSpot, spot, edge );
			previousSpot.refTo( spot );
		}

		// Divide
		for ( int id = 0; id < 2; id++ )
		{
			graph.addVertex( spot ).init( Integer.toString( ++labelGenerator ), previousSpot.getTimePoint() + 1, false );
			graph.addEdge( previousSpot, spot, edge );
			addBranch( spot, iteration + 1 );
		}

		graph.releaseRef( edge );
		graph.releaseRef( spot );
		graph.releaseRef( previousSpot );
	}

	public static String generateRandomString( int length )
	{
		final StringBuffer buffer = new StringBuffer();
		final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		final int charactersLength = characters.length();

		for ( int i = 0; i < length; i++ )
		{
			final double index = Math.random() * charactersLength;
			buffer.append( characters.charAt( ( int ) index ) );
		}
		return buffer.toString();
	}
}
