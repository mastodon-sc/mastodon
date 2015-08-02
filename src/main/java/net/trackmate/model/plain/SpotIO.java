package net.trackmate.model.plain;

/**
 * IO Facilities for AbstractSpot implementations.
 */
public class SpotIO
{
	/*
	 * SPOT.
	 */

	public static final int getSpotNumBytes()
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
}
