package net.trackmate.model;

/**
 * IO Facilities for Spot objects.
 */
public class SpotIO
{

	public static final int getNumBytes()
	{
		return Spot.SIZE_IN_BYTES - Spot.X_OFFSET;
	}

	public static final void getBytes( final Spot spot, final byte[] bytes )
	{
		final int n = Spot.SIZE_IN_BYTES - Spot.X_OFFSET;
		for ( int i = 0; i < n; ++i )
			bytes[ i ] = spot.getAccess().getByte( Spot.X_OFFSET + i );
	}

	public static final void setBytes( final Spot spot, final byte[] bytes )
	{
		final int n = Spot.SIZE_IN_BYTES - Spot.X_OFFSET;
		for ( int i = 0; i < n; ++i )
			spot.getAccess().putByte( bytes[ i ], Spot.X_OFFSET + i );
	}

	public static final void getSpotByInternalID( final ModelGraph graph, final int id, final Spot ref )
	{
		graph.getVertexPool().getByInternalPoolIndex( id, ref );
	}
}
