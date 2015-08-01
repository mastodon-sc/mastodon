package net.trackmate.bdv.wrapper;

import net.trackmate.model.Spot;


public class SpotOverlayProperties implements OverlayProperties< Spot >
{
	@Override
	public void localize( final Spot v, final float[] position )
	{
		v.localize( position );
	}

	@Override
	public void localize( final Spot v, final double[] position )
	{
		v.localize( position );
	}

	@Override
	public float getFloatPosition( final Spot v, final int d )
	{
		return v.getFloatPosition( d );
	}

	@Override
	public double getDoublePosition( final Spot v, final int d )
	{
		return v.getDoublePosition( d );
	}

	@Override
	public int numDimensions( final Spot v )
	{
		return v.numDimensions();
	}

	@Override
	public void getCovariance( final Spot v, final double[][] mat )
	{
		v.getCovariance( mat );
	}

	@Override
	public double getBoundingSphereRadiusSquared( final Spot v )
	{
		return v.getBoundingSphereRadiusSquared();
	}

	public static final SpotOverlayProperties instance = new SpotOverlayProperties();
}
