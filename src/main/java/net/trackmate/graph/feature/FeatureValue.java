package net.trackmate.graph.feature;

public interface FeatureValue< T >
{
	public void set( T value );

	public T get();

	public boolean isSet();
}
