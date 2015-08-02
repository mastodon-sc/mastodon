package net.trackmate.model;

/**
 * IO Facilities for AbstractSpot implementations.
 */
public class SpotIO
{

	/*
	 * SPOTCOVARIANCE.
	 */

	public static final int getSpotCovarianceNumBytes()
	{
		return SpotCovariance.SIZE_IN_BYTES - SpotCovariance.X_OFFSET;
	}

	public static final void getBytes( final SpotCovariance spot, final byte[] bytes )
	{
		final int n = SpotCovariance.SIZE_IN_BYTES - SpotCovariance.X_OFFSET;
		for ( int i = 0; i < n; ++i )
			bytes[ i ] = spot.getAccess().getByte( SpotCovariance.X_OFFSET + i );
	}

	public static final void setBytes( final SpotCovariance spot, final byte[] bytes )
	{
		final int n = SpotCovariance.SIZE_IN_BYTES - SpotCovariance.X_OFFSET;
		for ( int i = 0; i < n; ++i )
			spot.getAccess().putByte( bytes[ i ], SpotCovariance.X_OFFSET + i );
	}

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

	/*
	 * GENERAL.
	 */

	public static final < V extends AbstractSpot< V >> void getSpotByInternalID( final ModelGraph< V > graph, final int id, final V ref )
	{
		graph.getVertexPool().getByInternalPoolIndex( id, ref );
	}
}
