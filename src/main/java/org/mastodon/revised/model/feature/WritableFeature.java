package org.mastodon.revised.model.feature;

/**
 * Interface for features that can be written.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <O>
 *            the type of the object this feature is defined for (target).
 * @param <V>
 *            type of value stored in this feature.
 */
public interface WritableFeature< O, V > extends Feature< O, V >
{

	/**
	 * Sets the value stored in this feature associated with the specified
	 * target object.
	 *
	 * @param o
	 *            the target object.
	 * @param v
	 *            the value.
	 */
	public void set( O o, V v );

}
