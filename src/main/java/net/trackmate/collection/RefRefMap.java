package net.trackmate.collection;


/**
 * Map-like interface for maps that map possibly reusable references to another
 * possibly reusable reference.
 *
 * @param <K>
 *            key type.
 * @param <L>
 *            value type.
 *
 * @author Jean-Yves Tinevez
 */
public interface RefRefMap< K, L > extends RefObjectMap< K, L >
{
	public L createValueRef();

	public void releaseValueRef( final L obj );

	public L put( K key, L value, L ref );

	public L removeWithRef( Object key, L ref );

	public L get( Object key, L ref );

}
