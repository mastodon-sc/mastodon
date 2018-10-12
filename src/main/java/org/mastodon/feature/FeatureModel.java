package org.mastodon.feature;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mastodon.util.Listeners;
import org.mastodon.util.Listeners.SynchronizedList;

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
		 * Notifies a listener that the feature model it listens to has changed.
		 */
		public void featureModelChanged();
	}

	private final Map< String, Feature< ? > > featureMap;

	private final Set< FeatureSpec< ?, ? > > featureSpecs;

	private final SynchronizedList< FeatureModelListener > listeners;

	private boolean emitEvents = true;

	private boolean fireAtResume = false;

	public FeatureModel()
	{
		this.featureMap = new HashMap<>();
		this.featureSpecs = new HashSet<>();
		this.listeners = new Listeners.SynchronizedList<>();
	}

	/**
	 * Clears this feature model.
	 */
	public void clear()
	{
		featureMap.clear();
		featureSpecs.clear();
		fireFeatureModelChangedEvent();
	}

	/**
	 * Removes the feature with the specified specification from this model.
	 *
	 * @param spec
	 *            the {@link FeatureSpec} of the feature to remove.
	 */
	public void clear( final FeatureSpec< ?, ? > spec )
	{
		featureMap.remove( spec.getKey() );
		final boolean removed = featureSpecs.remove( spec );
		if (removed)
			fireFeatureModelChangedEvent();
	}

	/**
	 * Registers the feature key and the feature projections provided by the
	 * specified feature.
	 *
	 * @param key
	 *            the feature key.
	 * @param feature
	 *            the feature.
	 */
	public void declareFeature( final FeatureSpec< ?, ? > spec, final Feature< ? > feature )
	{
		featureMap.put( spec.getKey(), feature );
		featureSpecs.add( spec );
		fireFeatureModelChangedEvent();
	}

	/**
	 * Returns the feature with the specified key.
	 *
	 * @param key
	 *            the key of the feature to retrieve.
	 * @return the feature, or <code>null</code> if a feature with the specified
	 *         key is not registered in this model.
	 */
	public Feature< ? > getFeature( final String key )
	{
		return featureMap.get( key );
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
		return Collections.unmodifiableSet( featureSpecs );
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
		if ( fireAtResume )
			listeners.list.forEach( FeatureModelListener::featureModelChanged );
		fireAtResume = false;
	}

	private void fireFeatureModelChangedEvent()
	{
		if ( emitEvents )
			listeners.list.forEach( FeatureModelListener::featureModelChanged );
		else
			fireAtResume = true;
	}
}
