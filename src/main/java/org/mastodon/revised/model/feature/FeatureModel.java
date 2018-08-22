package org.mastodon.revised.model.feature;

import java.util.Set;

import org.mastodon.util.Listeners;

/**
 * Interface for feature models, classes that manage a collection of features in
 * a model graph.
 *
 * @author Jean-Yves Tinevez
 */
public interface FeatureModel
{
	public interface FeatureModelListener
	{
		/**
		 * Notifies a listener that the feature model it listens to has changed.
		 */
		public void featureModelChanged();
	}

	public < T > Set< Feature< T, ? > > getFeatureSet( Class< T > targetClass );

	/**
	 * Clears this feature model.
	 */
	public void clear();

	/**
	 * Removes the feature with the specified key from this model.
	 *
	 * @param key the key of the feature to remove.
	 */
	public void removeFeature( String key );


	/**
	 * Registers the feature key and the feature projections provided by the
	 * specified feature.
	 *
	 * @param feature
	 *            the feature.
	 */
	public void declareFeature( final Feature< ?, ? > feature );

	/**
	 * Returns the feature with the specified key.
	 *
	 * @param key
	 *            the key of the feature to retrieve.
	 * @return the feature, or <code>null</code> if a feature with the specified
	 *         key is not registered in this model.
	 */
	public Feature< ?, ? > getFeature( String key );

	/**
	 * Exposes the list of listeners that are notified when a change happens to
	 * this feature model. Events are fired for every call to {@link #clear()}
	 * or {@link #declareFeature(Feature)} methods.
	 *
	 * @return the list of the listeners.
	 */
	public Listeners< FeatureModelListener > listeners();

	/**
	 * Pause sending events to the feature model.
	 */
	public void pauseListeners();

	/**
	 * Resume sending events to the feature model. If events were generated
	 * while the listeners were paused, an event is fired by calling this
	 * method.
	 */
	public void resumeListeners();
}
