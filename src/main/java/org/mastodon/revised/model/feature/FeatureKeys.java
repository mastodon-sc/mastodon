package org.mastodon.revised.model.feature;

import java.util.Set;

/**
 * Interface for classes that can return the keys, targets and projections of
 * the several features they store.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public interface FeatureKeys
{

	/**
	 * Returns the feature target of the feature with the specified key. If the
	 * feature is not registered in this class, then this method returns
	 * <code>null</code>
	 * 
	 * @param featureKey
	 *            the feature key.
	 * @return the feature target.
	 */
	public FeatureTarget getFeatureTarget( final String featureKey );

	/**
	 * Returns the set of feature keys registered in this class, that are
	 * defined on the specified target.
	 * 
	 * @param target
	 *            the feature target. Must not be <code>null</code>.
	 * @return the set of feature keys.
	 */
	public Set< String > getFeatureKeys( FeatureTarget target );

	/**
	 * Returns the feature target of the feature projection with the specified
	 * key. If the feature projection is not registered in this class, then this
	 * method returns <code>null</code>
	 * 
	 * @param projectionKey
	 *            the feature projection key.
	 * @return the feature projection target.
	 */
	public FeatureTarget getProjectionTarget( final String projectionKey );

	/**
	 * Returns the set of feature projection keys registered in this class, that
	 * are defined on the specified target.
	 * 
	 * @param target
	 *            the feature target. Must not be <code>null</code>.
	 * @return the set of feature projection keys.
	 */
	public Set< String > getProjectionKeys( FeatureTarget target );
}