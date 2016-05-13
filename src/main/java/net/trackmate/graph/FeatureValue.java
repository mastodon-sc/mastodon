package net.trackmate.graph;

public interface FeatureValue< T >
{
	public void set( T value );

	public void remove();

	public T get();

	public boolean isSet();
}
