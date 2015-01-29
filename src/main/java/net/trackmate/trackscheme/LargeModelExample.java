package net.trackmate.trackscheme;

public class LargeModelExample
{
	private static final int N_STARTING_CELLS = 20;

	private static final int N_DIVISIONS = 14;

	private static final int N_FRAMES_PER_DIVISION = 5;

	public static void main( final String[] args )
	{
		new ShowTrackScheme( new LargeModelExample().graph );
	}

	public LargeModelExample()
	{
		graph = new TrackSchemeGraph();

		for ( int i = 0; i < N_STARTING_CELLS; ++i )
		{
			final TrackSchemeVertex mother = graph.addVertex().init( Integer.toString( ++labelGenerator ), 0, false );
			addBranch( mother, 1 );
		}

		System.out.println( labelGenerator + " vertices" );
	}

	final private TrackSchemeGraph graph;

	private int labelGenerator;

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

}
