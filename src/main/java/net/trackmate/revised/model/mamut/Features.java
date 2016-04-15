package net.trackmate.revised.model.mamut;

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
		final Spot spot = model.addSpot( 0, pos, cov, ref );

		spot.feature( LABEL ).set( "my first labeled spot" );
		spot.feature( TRACKLENGTH ).set( 3 );

		System.out.println( "label = " + spot.feature( LABEL ).get() );
		System.out.println( "tracklength (as Integer) = " + spot.feature( TRACKLENGTH ).get() );
		System.out.println( "tracklength (as int) = " + spot.feature( TRACKLENGTH ).getInt() );

		model.getGraph().releaseRef( ref );
	}
}
