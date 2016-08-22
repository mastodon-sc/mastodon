package net.trackmate.graph;

/**
 * The value of a feature (for one particular object).
 * <p>
 * Using this interface, the feature value (for a particular object) can be
 * read, set, and removed. Additionally, it can be checked whether the value is
 * set.
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
	 * Clear the value stored in this data item.
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
