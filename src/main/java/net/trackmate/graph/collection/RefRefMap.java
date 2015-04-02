package net.trackmate.graph.collection;


/**
 * Map-like interface for maps that map possibly reusable references to another
 * possibly reusable reference.
 */
public interface RefRefMap< K, L > extends RefObjectMap< K, L >
{
	public L createValueRef();

	public void releaseValueRef( final L obj );

	public L put( K key, L value, L ref );

	public L remove( Object key, L ref );

	public L get( Object key, L ref );

}
