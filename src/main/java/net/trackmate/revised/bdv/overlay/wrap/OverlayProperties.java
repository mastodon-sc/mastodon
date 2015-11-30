package net.trackmate.revised.bdv.overlay.wrap;

public interface OverlayProperties< V, E >
{
	public void localize( V v, final float[] position );

	public void localize( V v, final double[] position );

	public float getFloatPosition( V v, final int d );

	public double getDoublePosition( V v, final int d );

	public int numDimensions( V v );

	public void getCovariance( V v, double[][] mat );

	public double getBoundingSphereRadiusSquared( V v );

	public int getTimepoint( V v );

	public boolean isVertexSelected( V v );

	public boolean isEdgeSelected( E e );

	public double getMaxBoundingSphereRadiusSquared( int timepoint );
}
