package net.trackmate.revised.model;

public interface FeatureValue< T >
{
	public void set( T value );

	public T get();

	public boolean isSet();
}
