package net.trackmate.kdtree;

import static net.trackmate.graph.mempool.ByteUtils.*;
import net.imglib2.RealLocalizable;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.zzrefcollections.PoolObject;

class RealPoint extends PoolObject< RealPoint, ByteMappedElement > implements RealLocalizable
{
	protected static final int MAGIC_NUMBER_OFFSET = 0;

	protected static final int X_OFFSET = MAGIC_NUMBER_OFFSET + INDEX_SIZE;

	protected static final int SIZE_IN_BYTES( final int numDimensions )
	{
		return X_OFFSET + numDimensions * DOUBLE_SIZE;
	}

	private final int n;

	RealPoint( final RealPointPool pool )
	{
		super( pool );
		n = pool.numDimensions();
	}

	public RealPoint init( final double... position )
	{
		setPosition( position );
		return this;
	}

	public RealPoint init( final RealLocalizable position )
	{
		setPosition( position );
		return this;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "( " );
		for ( int d = 0; d < n; d++ )
		{
			sb.append( getDoublePosition( d ) );
			if ( d < n - 1 )
				sb.append( ", " );
		}
		sb.append( " )" );
		return sb.toString();
	}

	@Override
	protected void setToUninitializedState()
	{}

	// === subset of RealPositionable ===

	public void setPosition( final double[] position )
	{
		for ( int d = 0; d < n; ++d )
			access.putDouble( position[ d ], X_OFFSET + d * DOUBLE_SIZE );
	}

	public void setPosition( final RealLocalizable position )
	{
		for ( int d = 0; d < n; ++d )
			access.putDouble( position.getDoublePosition( d ), X_OFFSET + d * DOUBLE_SIZE );
	}

	public void setPosition( final double position, final int d )
	{
		access.putDouble( position, X_OFFSET + d * DOUBLE_SIZE );
	}

	// === RealLocalizable ===

	@Override
	public int numDimensions()
	{
		return n;
	}

	@Override
	public void localize( final float[] position )
	{
		for ( int d = 0; d < n; ++d )
			position[ d ] = ( float ) access.getDouble( X_OFFSET + d * DOUBLE_SIZE );
	}

	@Override
	public void localize( final double[] position )
	{
		for ( int d = 0; d < n; ++d )
			position[ d ] = access.getDouble( X_OFFSET + d * DOUBLE_SIZE );
	}

	@Override
	public float getFloatPosition( final int d )
	{
		return ( float ) getDoublePosition( d );
	}

	@Override
	public double getDoublePosition( final int d )
	{
		return access.getDouble( X_OFFSET + d * DOUBLE_SIZE );
	}

}
