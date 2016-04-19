package net.trackmate.revised.model.mamut;

import java.io.File;
import java.io.IOException;

import net.trackmate.revised.model.IntVertexFeature;
import net.trackmate.revised.model.ObjVertexFeature;

public class Features
{
	static final int NO_ENTRY_VALUE = -1;

	public static final ObjVertexFeature< Spot, String > LABEL = new ObjVertexFeature<>( "label" );
	public static final IntVertexFeature< Spot > TRACKLENGTH = new IntVertexFeature<>( "track length", NO_ENTRY_VALUE );

	private Features() {};

	public static void main( final String[] args )
	{
		final Model model = new Model();
		final Spot ref = model.getGraph().vertexRef();

		final double[] pos = new double[ 3 ];
		final double[][] cov = new double[ 3 ][ 3 ];

		for ( int i = 0; i < 100000; ++i )
		{
			final Spot spot = model.addSpot( 0, pos, cov, ref );

			spot.feature( LABEL ).set( "the vertex label " + i );
			spot.feature( TRACKLENGTH ).set( 3 );
		}
//		System.out.println( "label = " + spot.feature( LABEL ).get() );
//		System.out.println( "tracklength (as Integer) = " + spot.feature( TRACKLENGTH ).get() );
//		System.out.println( "tracklength (as int) = " + spot.feature( TRACKLENGTH ).getInt() );

		model.getGraph().releaseRef( ref );


		try
		{
			final File file = new File( "/Users/pietzsch/Desktop/model_with_features.raw" );

			model.saveRaw( file );
			System.out.println( "saved" );

			final Model loaded = new Model();
			final Spot s = loaded.addSpot( 0, pos, cov, loaded.getGraph().vertexRef() );
			s.feature( LABEL );
			s.feature( TRACKLENGTH );
			System.out.println( "loading" );
			loaded.loadRaw( file );
			System.out.println( "loaded" );

			final Spot next = model.getGraph().vertices().iterator().next();
			System.out.println( next.feature( LABEL ).get() );
			System.out.println( next.feature( TRACKLENGTH ).get() );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}
}
