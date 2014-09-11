package net.trackmate.model;

public class SpotCollectionExample
{
	public static void main( final String[] args )
	{
		final SpotCollection c = new SpotCollection( 4 );
		System.out.println( c + "\n" );

		for ( int i = 0; i < 5; ++i )
		{
			final Spot spot = c.createSpot();
			spot.setX( 0.1 * i );
			spot.setY( 1.0 * i );
			spot.setZ( 10.0 * i );
		}
		System.out.println( c + "\n" );

		c.addEdge( c.getSpot( 0 ), c.getSpot( 1 ) );
		c.addEdge( c.getSpot( 0 ), c.getSpot( 2 ) );
		c.addEdge( c.getSpot( 0 ), c.getSpot( 4 ) );
		c.addEdge( c.getSpot( 1 ), c.getSpot( 3 ) );
		c.addEdge( c.getSpot( 1 ), c.getSpot( 4 ) );
		System.out.println( c + "\n" );

		for ( final Spot spot : c )
		{
			System.out.println( spot );

			System.out.println( "num incoming edges = " + spot.incomingEdges().size() );
			for ( final Edge edge : spot.incomingEdges() )
				System.out.println( "  " + edge );

			System.out.println( "num outgoing edges = " + spot.outgoingEdges().size() );
			for ( final Edge edge : spot.outgoingEdges() )
				System.out.println( "  " + edge );

			System.out.println( "num edges = " + spot.edges().size() );
			for ( final Edge edge : spot.edges() )
				System.out.println( "  " + edge );

			System.out.println();
		}

		c.releaseSpot( c.getSpot( 1 ) );
		c.releaseSpot( 3 );
		System.out.println( c + "\n" );

		c.clear();
		System.out.println( c + "\n" );

		for ( int i = 0; i < 4; ++i )
		{
			final Spot spot = c.createSpot();
			spot.setX( 0.1 * i );
			spot.setY( 1.0 * i );
			spot.setZ( 10.0 * i );
		}
		System.out.println( c + "\n" );
	}
}
