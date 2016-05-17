package net.trackmate.collection;

import gnu.trove.map.TIntObjectMap;

/**
 * Interface for maps from {@code int} keys to Object values. It provides variants of
 * {@link TIntObjectMap} methods that take object references that can be used for
 * retrieval. Depending on concrete implementation, these object references can
 * be cleared, ignored or re-used.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 *
 * @param <V>
 *            value type.
 */
public interface IntRefMap< V > extends TIntObjectMap< V >
{
	/**
	 * Creates an object reference that can be used for processing with this
	 * map. Depending on concrete implementation, the object return can be
	 * {@code null}.
	 *
	 * @return a new object empty reference.
	 */
	public V createRef();

	/**
	 * Releases a previously created object reference. For standard object maps,
	 * this method does nothing.
	 *
	 * @param obj
	 *            the object reference to release.
	 */
	public void releaseRef( final V obj );

	/**
	 * Returns the value to which the specified key is mapped, or {@code null}
	 * if this map contains no mapping for the key.
	 *
	 * <p>
	 * This method is a (potentially) allocation-free version of
	 * {@link #get(int)}.
	 *
	 * @param key
	 *            the key whose associated value is to be returned.
	 * @param obj
	 *            an object reference that can be used for retrieval. Depending
	 *            on concrete implementation, this object can be cleared,
	 *            ignored or re-used.
	 * @return value to which the specified key is mapped, or {@code null} if
	 *         this map contains no mapping for the key. The object actually
	 *         returned might be the one specified as parameter {@code obj},
	 *         depending on concrete implementation.
	 */
	public V get( final int key, final V obj );

	/**
	 * Associates the specified value with the specified key in this map
	 * (optional operation). If the map previously contained a mapping for the
	 * key, the old value is replaced by the specified value. (A map <tt>m</tt>
	 * is said to contain a mapping for a key <tt>k</tt> if and only if
	 * {@link #containsKey(int) m.containsKey(k)} would return <tt>true</tt>.)
	 *
	 * @param key
	 *            key with which the specified value is to be associated
	 * @param value
	 *            value to be associated with the specified key.
	 * @param obj
	 *            an object reference that can be used for retrieval. Depending
	 *            on concrete implementation, this object can be cleared,
	 *            ignored or re-used.
	 * @return the previous value associated with the specified key, or
	 *         {@code null} if the map contained no mapping for the key. The
	 *         object actually returned might be the one specified as parameter
	 *         {@code replacedObj}, depending on concrete implementation.
	 * @throws UnsupportedOperationException
	 *             if the <tt>put</tt> operation is not supported by this map
	 * @throws ClassCastException
	 *             if the class of the specified key or value prevents it from
	 *             being stored in this map
	 * @throws NullPointerException
	 *             if the specified key or value is null and this map does not
	 *             permit null keys or values
	 * @throws IllegalArgumentException
	 *             if some property of the specified key or value prevents it
	 *             from being stored in this map
	 * @see #getNoEntryKey()
	 */
	public V put( final int key, final V value, final V obj );

	/**
	 * Removes the mapping for a key from this map if it is present. Returns the
	 * value to which this map previously associated the key. or {@code null} if
	 * the map contained no mapping for the key.
	 *
	 * <p>
	 * This method is a (potentially) allocation-free version of
	 * {@link #remove(int)}.
	 *
	 * @param key
	 *            the key whose associated value is to be returned.
	 * @param obj
	 *            an object reference that can be used for retrieval. Depending
	 *            on concrete implementation, this object can be cleared,
	 *            ignored or re-used.
	 * @return the previous value associated with the specified key, or
	 *         {@code null} if the map contained no mapping for the key. The
	 *         object actually returned might be the one specified as parameter
	 *         {@code obj}, depending on concrete implementation.
	 */
	public V remove( final int key, final V obj );

	/**
	 * Inserts a key/value pair into the map if the specified key is not already
	 * associated with a value.
	 * <p>
	 * This method is a (potentially) allocation-free version of
	 * {@link #putIfAbsent(int, Object)}.
	 *
	 * @param key
	 *            key with which the specified value is to be associated.
	 * @param value
	 *            an object reference to be associated with the specified key.
	 * @param obj
	 *            an object reference that can be used for retrieval. Depending
	 *            on concrete implementation, this object can be cleared,
	 *            ignored or re-used.
	 * @return the previous value associated with <tt>key</tt>, or
	 *         <code>null</code> if none was found.
	 */
	public V putIfAbsent( int key, V value, V obj );
}
