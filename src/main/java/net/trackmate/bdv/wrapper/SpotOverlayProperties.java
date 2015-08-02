package net.trackmate.bdv.wrapper;

import net.trackmate.model.tgmm.SpotCovariance;


public class SpotOverlayProperties implements OverlayProperties< SpotCovariance >
{
	@Override
	public void localize( final SpotCovariance v, final float[] position )
	{
		v.localize( position );
	}

	@Override
	public void localize( final SpotCovariance v, final double[] position )
	{
		v.localize( position );
	}

	@Override
	public float getFloatPosition( final SpotCovariance v, final int d )
	{
		return v.getFloatPosition( d );
	}

	@Override
	public double getDoublePosition( final SpotCovariance v, final int d )
	{
		return v.getDoublePosition( d );
	}

	@Override
	public int numDimensions( final SpotCovariance v )
	{
		return v.numDimensions();
	}

	@Override
	public void getCovariance( final SpotCovariance v, final double[][] mat )
	{
		v.getCovariance( mat );
	}

	public static final SpotOverlayProperties instance = new SpotOverlayProperties();
}
