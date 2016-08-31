package org.mastodon.collection;


/**
 * Map-like interface for maps that map possibly reusable references to another
 * possibly reusable reference.
 *
 * @param <K>
 *            key type.
 * @param <V>
 *            value type.
 *
 * @author Jean-Yves Tinevez
 */
public interface RefRefMap< K, V > extends RefObjectMap< K, V >
{
	public V createValueRef();

	public void releaseValueRef( final V obj );

	// TODO javadoc
	public V put( K key, V value, V ref );

	// TODO javadoc
	public V removeWithRef( Object key, V ref );

	// TODO javadoc
	public V get( Object key, V ref );

}
