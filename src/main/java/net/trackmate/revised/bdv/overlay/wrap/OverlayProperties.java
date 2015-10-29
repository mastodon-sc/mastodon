package net.trackmate.revised.bdv.overlay.wrap;

public interface OverlayProperties< V >
{
	public void localize( V v, final float[] position );

	public void localize( V v, final double[] position );

	public float getFloatPosition( V v, final int d );

	public double getDoublePosition( V v, final int d );

	public int numDimensions( V v );

	public void getCovariance( V v, double[][] mat );
}
