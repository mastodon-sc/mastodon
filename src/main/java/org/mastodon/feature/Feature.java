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
	 * Get the projection with the specified {@code key}. Depending of this
	 * features {@link Multiplicity}, source indices need to be specified
	 * additionally.
	 *
	 * @param spec
	 *            spec of the requested projection
	 * @param sources
	 *            indices of 0, 1, or 2 sources (depending on this features
	 *            {@code Multiplicity}) for which to obtain the projection
	 * @return the specified projection, or {@code null} if the projection is
	 *         not available.
	 */
	default public FeatureProjection< T > project( final FeatureProjectionSpec spec, final int... sources )
	{
		return project( FeatureProjectionKey.key( spec, sources ) );
	}

	public FeatureProjection< T > project( final FeatureProjectionKey key );

	/**
	 * Get keys for all {@link FeatureProjection}s that this feature currently provides.
	 *
	 * @return keys for all projections.
	 */
	@Deprecated
	public Set< FeatureProjectionKey > projectionKeys();

	// TODO: should replace projectionKeys()
	// TODO: After that, FeatureProjectionKey can be made to include also FeatureSpec, to provide a unique key for each projection (mastodon-wide)
	public Set< FeatureProjection< T > > projections();

	public FeatureSpec< ? extends Feature<T>, T > getSpec();
}
