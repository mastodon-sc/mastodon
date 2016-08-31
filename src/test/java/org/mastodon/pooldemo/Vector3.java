package org.mastodon.pooldemo;

import static org.mastodon.pool.ByteUtils.DOUBLE_SIZE;

import java.util.Map;

import org.mastodon.collection.UniqueHashcodeArrayMap;
import org.mastodon.graph.FeatureValue;
import org.mastodon.graph.features.Feature;
import org.mastodon.graph.features.Features;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.PoolObject;

import net.imglib2.Localizable;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;

public class Vector3 extends PoolObject< Vector3, ByteMappedElement > implements RealLocalizable, RealPositionable
{
	private static final int n = 3;

	protected static final int X_OFFSET = 0;
	protected static final int SIZE_IN_BYTES = X_OFFSET + 3 * DOUBLE_SIZE;

//	Vector3( final Vector3Pool pool )
//	{
//		super( pool );
//	}
//
	@Override
	public double getDoublePosition( final int d )
	{
		return access.getDouble( X_OFFSET + d * DOUBLE_SIZE );
	}

	@Override
	public void setPosition( final double position, final int d )
	{
		access.putDouble( position, X_OFFSET + d * DOUBLE_SIZE );
	}

	public Vector3 init( final double... pos )
	{
		setPosition( pos );
		return this;
	}

	@Override
	protected void setToUninitializedState()
	{}


	/*
	 * ==============================================================
	 *
	 * ... ImgLib2 RealLocalizable / RealPositionable boilerplate ...
	 *
	 * ==============================================================
	 */

	@Override
	public int numDimensions()
	{
		return n;
	}

	@Override
	public void localize( final float[] position )
	{
		for ( int d = 0; d < n; ++d )
			position[ d ] = getFloatPosition( d );
	}

	@Override
	public void localize( final double[] position )
	{
		for ( int d = 0; d < n; ++d )
			position[ d ] = getDoublePosition( d );
	}

	@Override
	public float getFloatPosition( final int d )
	{
		return ( float ) getDoublePosition( d );
	}

	@Override
	public void fwd( final int d )
	{
		move( 1, d );
	}

	@Override
	public void bck( final int d )
	{
		move( -1, d );
	}

	@Override
	public void move( final int distance, final int d )
	{
		move( ( double ) distance, d );
	}

	@Override
	public void move( final long distance, final int d )
	{
		move( ( double ) distance, d );
	}

	@Override
	public void move( final Localizable localizable )
	{
		move( ( RealLocalizable ) localizable );
	}

	@Override
	public void move( final int[] distance )
	{
		for ( int d = 0; d < n; ++d )
			move( distance[ d ], d );
	}

	@Override
	public void move( final long[] distance )
	{
		for ( int d = 0; d < n; ++d )
			move( distance[ d ], d );
	}

	@Override
	public void setPosition( final Localizable localizable )
	{
		setPosition( ( RealLocalizable ) localizable );
	}

	@Override
	public void setPosition( final int[] position )
	{
		for ( int d = 0; d < n; ++d )
			setPosition( position[ d ], d );
	}

	@Override
	public void setPosition( final long[] position )
	{
		for ( int d = 0; d < n; ++d )
			setPosition( position[ d ], d );
	}

	@Override
	public void setPosition( final int position, final int d )
	{
		setPosition( ( double ) position, d );
	}

	@Override
	public void setPosition( final long position, final int d )
	{
		setPosition( ( double ) position, d );
	}

	@Override
	public void move( final float distance, final int d )
	{
		move( ( double ) distance, d );
	}

	@Override
	public void move( final double distance, final int d )
	{
		setPosition( getDoublePosition( d ) + distance, d );
	}

	@Override
	public void move( final RealLocalizable localizable )
	{
		for ( int d = 0; d < n; ++d )
			move( localizable.getDoublePosition( d ), d );
	}

	@Override
	public void move( final float[] distance )
	{
		for ( int d = 0; d < n; ++d )
			move( distance[ d ], d );
	}

	@Override
	public void move( final double[] distance )
	{
		for ( int d = 0; d < n; ++d )
			move( distance[ d ], d );
	}

	@Override
	public void setPosition( final RealLocalizable localizable )
	{
		for ( int d = 0; d < n; ++d )
			setPosition( localizable.getDoublePosition( d ), d );
	}

	@Override
	public void setPosition( final float[] position )
	{
		for ( int d = 0; d < n; ++d )
			setPosition( position[ d ], d );
	}

	@Override
	public void setPosition( final double[] position )
	{
		for ( int d = 0; d < n; ++d )
			setPosition( position[ d ], d );
	}

	@Override
	public void setPosition( final float position, final int d )
	{
		setPosition( ( double ) position, d );
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		char c = '(';
		for ( int i = 0; i < numDimensions(); i++ )
		{
			sb.append( c );
			sb.append( getDoublePosition( i ) );
			c = ',';
		}
		sb.append( ")" );
		return sb.toString();
	}





	Vector3( final Vector3Pool pool, final Features< Vector3 > features )
	{
		super( pool );
		this.features = features;
		featureValues = new UniqueHashcodeArrayMap<>();
	}

	private final Features< Vector3 > features;

	private final Map< Feature< ?, Vector3, ? >, FeatureValue< ? > > featureValues;

	@SuppressWarnings( "unchecked" )
	public < F extends FeatureValue< ? >, M > F feature( final Feature< M, Vector3, F > feature )
	{
		F fv = ( F ) featureValues.get( feature );
		if ( fv == null )
		{
			fv = feature.createFeatureValue( this, features );
			featureValues.put( feature, fv );
		}
		return fv;
	}


}
