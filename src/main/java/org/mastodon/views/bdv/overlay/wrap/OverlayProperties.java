package org.mastodon.views.bdv.overlay.wrap;

public interface OverlayProperties< V, E > // TODO: rename to ModelGraphProperties ???
{
	public void localize( V v, final double[] position );

	public double getDoublePosition( V v, final int d );

	public void setPosition( V v, double position, int d );

	public void setPosition( V v, final double[] position );

	public void getCovariance( V v, double[][] mat );

	public void setCovariance( V v, double[][] mat );

	public String getLabel( V v );

	public void setLabel( V v, String label );

	public double getBoundingSphereRadiusSquared( V v );

	public int getTimepoint( V v );

	public double getMaxBoundingSphereRadiusSquared( int timepoint );

	public V addVertex( V ref );

	// TODO: remove? use covariance version instead?
	public V initVertex( V v, int timepoint, double[] position, double radius );

	public V initVertex( V v, int timepoint, double[] position, double[][] covariance );

	public E addEdge( V source, V target, E ref );

	public E insertEdge( V source, final int sourceOutIndex, V target, final int targetInIndex, final E ref );

	public E initEdge( E e );

	public void removeEdge( E e );

	public void removeVertex( V v );

	public void notifyGraphChanged();
}
