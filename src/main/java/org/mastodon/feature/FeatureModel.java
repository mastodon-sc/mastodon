package org.mastodon.feature;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mastodon.util.Listeners;

/**
 * Class that manage a collection of features in a model graph.
 *
 * @author Jean-Yves Tinevez
 */
public class FeatureModel
{

	/**
	 * Interface for listeners that listens to changes in a FeatureModel.
	 */
	public interface FeatureModelListener
	{
		/**
		 * Notifies a listener that the feature model has changed.
		 */
		public void featureModelChanged();
	}

	private final Map< FeatureSpec< ?, ? >, Feature< ? > > features;

	private final Listeners.List< FeatureModelListener > listeners;

	/**
	 * If <code>false</code>, listeners will not be notified when a
	 * featureModelChanged event happens.
	 */
	private boolean emitEvents = true;

	/**
	 * Is <code>true</code> if a featureModelChanged happened while the
	 * listeners were paused.
	 */
	private boolean shouldEmitEvent;

	public FeatureModel()
	{
		this.features = new HashMap<>();
		this.listeners = new Listeners.SynchronizedList<>();
	}

	/**
	 * Clears this feature model.
	 */
	public void clear()
	{
		features.clear();
		notifyFeatureModelChanged();
	}

	/**
	 * Removes the feature with the specified specification from this model.
	 *
	 * @param key
	 *            the {@link FeatureSpec} of the feature to remove.
	 */
	public void clear( final FeatureSpec< ?, ? > key )
	{
		final boolean removed = features.remove( key ) != null;
		if ( removed )
			notifyFeatureModelChanged();
	}

	/**
	 * Registers the feature key and the feature projections provided by the
	 * specified feature.
	 *
	 * @param feature
	 *            the feature.
	 */
	public void declareFeature( final Feature< ? > feature )
	{
		features.put( feature.getSpec(), feature );
		notifyFeatureModelChanged();
	}

	/**
	 * Returns the feature with the specified key.
	 *
	 * @param key
	 *            the {@link FeatureSpec} of the feature to retrieve.
	 * @return the feature, or {@code null} if a feature with the specified
	 *         key is not registered in this model.
	 */
	public Feature< ? > getFeature( final FeatureSpec< ?, ? > key )
	{
		return features.get( key );
	}

	/**
	 * Returns the collection of the {@link FeatureSpec}s declared in this
	 * feature model.
	 *
	 * @return the collection of the {@link FeatureSpec}s declared in this
	 *         feature model.
	 */
	public Collection< FeatureSpec< ?, ? > > getFeatureSpecs()
	{
		return Collections.unmodifiableSet( features.keySet() );
	}

	/**
	 * Exposes the list of listeners that are notified when a change happens to
	 * this feature model. Events are fired for every call to {@link #clear()}
	 * or {@link #declareFeature(Feature)} methods.
	 *
	 * @return the list of the listeners.
	 */
	public Listeners< FeatureModelListener > listeners()
	{
		return listeners;
	}

	/**
	 * Pause sending events from this feature model.
	 */
	public void pauseListeners()
	{
		emitEvents = false;
	}

	/**
	 * Resume sending events to the feature model. If events were generated
	 * while the listeners were paused, an event is fired by calling this
	 * method.
	 */
	public void resumeListeners()
	{
		emitEvents = true;
		if ( shouldEmitEvent )
		{
			listeners.list.forEach( FeatureModelListener::featureModelChanged );
			shouldEmitEvent = false;
		}
	}

	private void notifyFeatureModelChanged()
	{
		if ( emitEvents )
			listeners.list.forEach( FeatureModelListener::featureModelChanged );
		else
			shouldEmitEvent = true;
	}
}
