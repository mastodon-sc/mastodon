package org.mastodon.revised.bvv.wrap;

public interface BvvModelGraphProperties< V, E >
{
	double getDoublePosition( V v, final int d );

	void getCovariance( V v, double[][] mat );

	int getTimepoint( V v );

	double getMaxBoundingSphereRadiusSquared( int timepoint );

	E initEdge( E e );

	void removeEdge( E e );

	void removeVertex( V v );

	void notifyGraphChanged();
}
