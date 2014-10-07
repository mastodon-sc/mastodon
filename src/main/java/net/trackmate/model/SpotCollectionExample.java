package net.trackmate.model;

public class SpotCollectionExample
{
	public static void main( final String[] args )
	{
		final SpotCollection graph = new SpotCollection();
		final SpotList spots = new SpotList( graph );
//		System.out.println( graph + "\n" );

		final double y = 0;
		final double z = 0;
		final double radius = 5.0;
		final double quality = 1.0;

		final boolean alloc = false;
		if ( alloc )
		{
			for ( int i = 0; i < 5; ++i )
			{
				final double x = i;
				// etc...
				final Spot spot = graph.createSpot().init( x, y, z, radius, quality );
				spots.add( spot );
			}
//			System.out.println( graph + "\n" );

			graph.addEdge( spots.get( 0 ), spots.get( 1 ) );
			graph.addEdge( spots.get( 0 ), spots.get( 2 ) );
			graph.addEdge( spots.get( 0 ), spots.get( 4 ) );
			graph.addEdge( spots.get( 1 ), spots.get( 3 ) );
			graph.addEdge( spots.get( 1 ), spots.get( 4 ) );
			System.out.println( graph );
		}
		else
		{
			final Spot s0 = graph.createEmptySpotRef();
			final Spot s1 = graph.createEmptySpotRef();
			final Edge e0 = graph.createEmptyEdgeRef();

			for ( int i = 0; i < 5; ++i )
			{
				final double x = i;
				// etc...
				final Spot spot = graph.createSpot( s0 ).init( x, y, z, radius, quality );
				spots.add( spot );
			}
//			System.out.println( graph + "\n" );

			graph.addEdge( spots.get( 0, s0 ), spots.get( 1, s1 ), e0 );
			graph.addEdge( spots.get( 0, s0 ), spots.get( 2, s1 ), e0 );
			graph.addEdge( spots.get( 0, s0 ), spots.get( 4, s1 ), e0 );
			graph.addEdge( spots.get( 1, s0 ), spots.get( 3, s1 ), e0 );
			graph.addEdge( spots.get( 1, s0 ), spots.get( 4, s1 ), e0 );
			System.out.println( graph );
		}

		for ( final Spot spot : graph )
		{
			System.out.println( spot );
			for ( final Edge edge : spot.outgoingEdges() )
				System.out.println( "  " + edge );
			System.out.println();
		}
	}
}
