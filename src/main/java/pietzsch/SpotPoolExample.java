package pietzsch;



public class SpotPoolExample
{
	// TODO: move to SpotCollection.toString()
	public static void printSpotCollection( final SpotCollection c )
	{
		System.out.println( "SpotCollection {" );
		System.out.println( "  spots = {" );
		for ( final Spot spot : c )
			System.out.println( "    " + spot );
		System.out.println( "  }" );
		System.out.println( "  edges = {" );
		for ( final Edge edge : c.edges() )
			System.out.println( "    " + edge.toString( c.spotPool ) );
		System.out.println( "  }" );
		System.out.println( "}" );
		System.out.println();
	}

	public static void main( final String[] args )
	{
		final SpotCollection c = new SpotCollection( 4 );
		printSpotCollection( c );

		for ( int i = 0; i < 5; ++i )
		{
			final Spot spot = c.createSpot();
			spot.setX( 0.1 * i );
			spot.setY( 1.0 * i );
			spot.setZ( 10.0 * i );
		}
		printSpotCollection( c );

		c.addEdge( c.getSpot( 0 ), c.getSpot( 1 ) );
		c.addEdge( c.getSpot( 0 ), c.getSpot( 2 ) );
		c.addEdge( c.getSpot( 0 ), c.getSpot( 4 ) );
		c.addEdge( c.getSpot( 1 ), c.getSpot( 3 ) );
		c.addEdge( c.getSpot( 1 ), c.getSpot( 4 ) );
		printSpotCollection( c );

		for ( final Spot spot : c )
		{
			System.out.println( spot );
			System.out.println( "num incoming edges = " + spot.incomingEdges().size() );
			for ( final Edge edge : spot.incomingEdges() )
			{
			}
			System.out.println( "num outgoing edges = " + spot.outgoingEdges().size() );
			System.out.println( "num edges = " + spot.edges().size() );

			System.out.println();
		}


		c.releaseSpot( c.getSpot( 1 ) );
		c.releaseSpot( 3 );
		printSpotCollection( c );

		c.clear();
		printSpotCollection( c );

		for ( int i = 0; i < 4; ++i )
		{
			final Spot spot = c.createSpot();
			spot.setX( 0.1 * i );
			spot.setY( 1.0 * i );
			spot.setZ( 10.0 * i );
		}
		printSpotCollection( c );
	}
}
