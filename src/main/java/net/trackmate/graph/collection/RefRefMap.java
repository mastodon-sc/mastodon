package net.trackmate.graph.collection;

import java.util.Map;

/**
 * Map-like interface for maps that map possibly reusable references to another
 * possibly reusable reference.
 */
public interface RefRefMap< K, L > extends Map< K, L >
{
	public K createRef();

	public void releaseRef( final K obj );

	public L createValueRef();

	public void releaseValueRef( final L obj );

	public L put( K key, L value, L ref );

	public L remove( Object key, L ref );

	public L get( Object key, L ref );

}
