package org.mastodon.revised.ui.coloring.feature;

import org.mastodon.feature.FeatureProjection;

/**
 * Provides {@link FeatureProjection}s for {@link FeatureProjectionId} to be
 * used in {@link FeatureColorMode}s.
 *
 * @author Tobias Pietzsch
 */
public interface Projections
{
	/**
	 * Get a {@code FeatureProjection} (of any target type) for the specified
	 * {@code FeatureProjectionId}.
	 *
	 * @param id
	 *            requested id
	 * @return {@code FeatureProjection} with {@code id}, if it exists.
	 *         {@code null} otherwise.
	 */
	public FeatureProjection< ? > getFeatureProjection( final FeatureProjectionId id );

	/**
	 * Get a {@code FeatureProjection} of the given target type for the
	 * specified {@code FeatureProjectionId}.
	 *
	 * @param id
	 *            requested id
	 * @param target
	 *            target type
	 * @param <T>
	 *            target type
	 * @return {@code FeatureProjection} with {@code id} and {@code target}
	 *         type, if it exists. {@code null} otherwise.
	 */
	public < T > FeatureProjection< T > getFeatureProjection( final FeatureProjectionId id, Class< T > target );
}
