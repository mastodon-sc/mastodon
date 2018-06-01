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
		Set< Feature< ?, ? > > featureSet = targetClassToFeatures.get( clazz );
		if ( null == featureSet )
		{
			featureSet = new HashSet<>();
			targetClassToFeatures.put( clazz, featureSet );
		}
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
			listeners.list.forEach( ( l ) -> l.featureModelChanged() );
		fireAtResume = false;
	}

	private void fireFeatureModelChangedEvent()
	{
		if ( emitEvents )
			listeners.list.forEach( ( l ) -> l.featureModelChanged() );
		else if ( !fireAtResume )
			fireAtResume = true;
	}
}
