package org.mastodon.revised.model.feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mastodon.util.Listeners;

/**
 * Default feature model.
 *
 * @author Jean-Yves Tinevez
 */
public class DefaultFeatureModel implements FeatureModel
{
	private final Listeners.List< FeatureModelListener > listeners;

	private boolean emitEvents = true;

	private boolean fireAtResume = false;

	private final Map< Class< ? >, Set< Feature< ?, ? > > > targetClassToFeatures;

	private final Map< String, Feature< ?, ? > > keyToFeature;

	/**
	 * Creates a new, empty, feature model.
	 */
	public DefaultFeatureModel()
	{
		targetClassToFeatures = new HashMap<>();
		keyToFeature = new HashMap<>();
		listeners = new Listeners.SynchronizedList<>();
	}

	@Override
	public void declareFeature( final Feature< ?, ? > feature )
	{
		// Features.
		final Class< ? > clazz = feature.getTargetClass();
		final Set< Feature< ?, ? > > featureSet = targetClassToFeatures.computeIfAbsent( clazz, k -> new HashSet<>() );
		featureSet.add( feature );

		// Feature keys.
		keyToFeature.put( feature.getKey(), feature );

		fireFeatureModelChangedEvent();
	}

	@Override
	public void clear()
	{
		targetClassToFeatures.clear();
		keyToFeature.clear();
		fireFeatureModelChangedEvent();
	}

	@Override
	public void removeFeature( final String key )
	{
		final Feature< ?, ? > feature = keyToFeature.remove( key );
		if ( null == feature )
			return;

		final Class< ? > clazz = feature.getTargetClass();
		targetClassToFeatures.get( clazz ).remove( feature );
		fireFeatureModelChangedEvent();
	}

	@Override
	public Set< Feature< ?, ? > > getFeatureSet( final Class< ? > targetClass )
	{
		return targetClassToFeatures.get( targetClass );
	}

	@Override
	public Feature< ?, ? > getFeature( final String key )
	{
		return keyToFeature.get( key );
	}

	@Override
	public Listeners< FeatureModelListener > listeners()
	{
		return listeners;
	}

	@Override
	public void pauseListeners()
	{
		emitEvents = false;
	}

	@Override
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
