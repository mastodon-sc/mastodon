package org.mastodon.revised.model.feature;

import java.util.Set;

/**
 * Interface for feature models, classes that manage a collection of features in
 * a model graph.
 *
 * @author Jean-Yves Tinevez
 */
public interface FeatureModel
{

	public Set< Feature< ?, ?, ? > > getFeatureSet( Class< ? > targetClass );

	/**
	 * Clears this feature model.
	 */
	public void clear();

	/**
	 * Registers the feature key and the feature projections provided by the
	 * specified feature.
	 *
	 * @param feature
	 *            the feature.
	 */
	public void declareFeature( final Feature< ?, ?, ? > feature );

	/**
	 * Returns the feature with the specified key.
	 *
	 * @param key
	 *            the key of the feature to retrieve.
	 * @return the feature, or <code>null</code> if a feature with the specified
	 *         key is not registered in this model.
	 */
	public Feature< ?, ?, ? > getFeature( String key );

}
