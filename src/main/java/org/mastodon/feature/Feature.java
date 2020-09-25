package org.mastodon.feature;

import java.util.Set;

/**
 * Interface for Mastodon features.
 * <p>
 * A feature is a read-only quantity calculated for a model at runtime. They
 * store values that are defined for a model but are not required for the model
 * consistency. They are typically defined for a specific application by the
 * user; they are optional and their selection and calculation are triggered at
 * runtime.
 * <p>
 * A Feature is defined for a specific target in the model (vertex, edge, graph,
 * etc.) specified as a type parameter of this class.
 * <p>
 *
 * @param <T>
 *            target the type of the object this feature is defined for.
 */
public interface Feature< T >
{
	/**
	 * Get the projection with the specified {@link FeatureProjectionKey}.
	 *
	 * @param key
	 *            the requested projection
	 * @return the specified projection, or {@code null} if the projection is
	 *         not available.
	 */
	public FeatureProjection< T > project( final FeatureProjectionKey key );

	/**
	 * Get all {@link FeatureProjection}s that this feature currently provides.
	 *
	 * @return set of all projections.
	 */
	public Set< FeatureProjection< T > > projections();

	public FeatureSpec< ? extends Feature<T>, T > getSpec();

	/**
	 * Removes the specified object from this feature storage.
	 * <p>
	 * This method is used to invalidate a feature value when an object has been
	 * modified under it.
	 *
	 * @param obj
	 *            the object to remove.
	 */
	// TODO: Would invalidate() be a better name for this method?
	public void remove( T obj );
}
