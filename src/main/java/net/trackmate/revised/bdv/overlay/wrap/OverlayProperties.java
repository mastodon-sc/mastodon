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

	// TODO move to separate interface? OverlayModifyProperties?
	public V addVertex( final int timepoint, final double[] position, final double radius, V ref );

	// TODO move to separate interface? OverlayModifyProperties?
	public V addVertex( final int timepoint, final double[] position, final double[][] covariance, final V ref );

	// TODO move to separate interface? OverlayModifyProperties?
	public E addEdge( V source, V target, E ref );

	// TODO move to separate interface? OverlayModifyProperties?
	public void removeEdge( E e );

	// TODO move to separate interface? OverlayModifyProperties?
	public void removeVertex( V v );

	// TODO move to separate interface? OverlayModifyProperties?
	public void notifyGraphChanged();

	// TODO investigate! Why is this needed? Everything calling this is highly suspicious.
	public E getEdge( V source, V target, E edge );
}
