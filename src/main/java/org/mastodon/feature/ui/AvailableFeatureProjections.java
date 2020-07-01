package org.mastodon.feature.ui;

import java.util.Collection;

import org.mastodon.feature.Multiplicity;
import org.mastodon.ui.coloring.feature.TargetType;

import gnu.trove.list.TIntList;

/**
 * Lists the features and projections (as their respective ({@code String}
 * keys), as well as available source indices (for features with multiplicity).
 *
 * @author Tobias Pietzsch
 */
public interface AvailableFeatureProjections
{
	/**
	 * Get ordered list of source indices available for selection as feature
	 * source.
	 *
	 * @return ordered list of source indices
	 */
	public TIntList getSourceIndices();

	/**
	 * Get ordered list of feature keys for the given {@code targetType}.
	 *
	 * @param targetType
	 *            whether vertex or edge features should be returned
	 * @return ordered list of feature keys
	 */
	public Collection< String > featureKeys( final TargetType targetType );

	/**
	 * Get ordered list of projection keys available for the feature with the
	 * specified {@code targetType} and {@code String} key.
	 *
	 * @param targetType
	 *            whether a vertex or edge feature is queried
	 * @param featureKey
	 *            key of the feature to query
	 * @return ordered list of projection keys
	 */
	public Collection< String > projectionKeys( final TargetType targetType, final String featureKey );


	/**
	 * Get the multiplicity of the feature with the specified {@code targetType}
	 * and {@code String} key.
	 *
	 * @param targetType
	 *            whether a vertex or edge feature is queried
	 * @param featureKey
	 *            key of the feature to query
	 * @return multiplicity of specified feature
	 */
	public Multiplicity multiplicity( final TargetType targetType, final String featureKey );
}
