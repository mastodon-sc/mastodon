package net.trackmate.model;

/**
 * IO Facilities for Spot objects.
 */
public class SpotIO
{

	public static final int getNumBytes()
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

	public static final void getSpotByInternalID( final ModelGraph graph, final int id, final SpotCovariance ref )
	{
		graph.getVertexPool().getByInternalPoolIndex( id, ref );
	}
}
