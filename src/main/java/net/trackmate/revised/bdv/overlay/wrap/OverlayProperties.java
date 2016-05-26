package net.trackmate.revised.bdv.overlay.wrap;

public interface OverlayProperties< V, E >
{
	public void localize( V v, final double[] position );

	public double getDoublePosition( V v, final int d );

	public void setPosition( V v, double position, int d );

	public void setPosition( V v, final double[] position );

	public void getCovariance( V v, double[][] mat );

	public void setCovariance( V v, double[][] mat );

	public double getBoundingSphereRadiusSquared( V v );

	public int getTimepoint( V v );

	public boolean isVertexSelected( V v );

	public boolean isEdgeSelected( E e );

	public double getMaxBoundingSphereRadiusSquared( int timepoint );

	public V addVertex( final int timepoint, final double[] position, final double radius, V ref );

	public E addEdge( V source, V target, E ref );

	public void notifyGraphChanged();

}
