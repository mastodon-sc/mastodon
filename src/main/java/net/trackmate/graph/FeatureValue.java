package net.trackmate.graph;

/**
 * Interface for feature value or data item manipulation.
 * <p>
 * Data items implementing this interface can be read, set and removed from the
 * collection they belong to. Additionally, a boolean flag returns whether this
 * data item has a set value or not.
 *
 * @param <T>
 *            the type of the value stored.
 */
public interface FeatureValue< T >
{

	/**
	 * Sets the value stored in this data item.
	 * 
	 * @param value
	 *            the value to store.
	 */
	public void set( T value );

	/**
	 * Removes this data item from the collection it belongs to.
	 */
	public void remove();

	/**
	 * Returns the value of this data item.
	 * <p>
	 * The returned instance may be reused for several data items. If this data
	 * item is not set, the value is undefined and the returned object may be
	 * <code>null</code>
	 * 
	 * @return the value.
	 */
	public T get();

	/**
	 * Returns whether this feature value is set or not. If <code>false</code>,
	 * the value returned by {@link #get()} is undefined and may be
	 * <code>null</code>. If <code>true</code>, the instance returned by
	 * {@link #get()} will not be <code>null</code>.
	 * 
	 * @return whether this data item is set.
	 */
	public boolean isSet();
}
