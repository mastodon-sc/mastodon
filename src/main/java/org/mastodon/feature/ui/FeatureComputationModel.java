package org.mastodon.feature.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mastodon.feature.FeatureSpec;
import org.mastodon.util.Listeners;

public class FeatureComputationModel
{

	public interface UpdateListener
	{
		public void settingsChanged();
	}

	private final Listeners.List< UpdateListener > updateListeners;

	private final Set< String > selectedFeatureKeys;

	private final Map< Class< ? >, Collection< FeatureSpec< ?, ? > > > featureSpecsTargetMap;

	private final Map< String, FeatureSpec< ?, ? > > featureSpecsKeys;

	public FeatureComputationModel()
	{
		this.updateListeners = new Listeners.SynchronizedList<>();
		this.selectedFeatureKeys = new HashSet<>();
		this.featureSpecsTargetMap = new HashMap<>();
		this.featureSpecsKeys = new HashMap<>();
	}

	private void notifyListeners()
	{
		for ( final UpdateListener l : updateListeners.list )
			l.settingsChanged();
	}

	public Listeners< UpdateListener > updateListeners()
	{
		return updateListeners;
	}

	public boolean isSelected( final String featureKey )
	{
		return selectedFeatureKeys.contains( featureKey );
	}

	public void setSelected( final String featureKey, final boolean selected )
	{
		if ( isSelected( featureKey ) != selected )
		{
			if ( selected )
				selectedFeatureKeys.add( featureKey );
			else
				selectedFeatureKeys.remove( featureKey );
			notifyListeners();
		}
	}

	public Set< String > getSelectedFeatureKeys()
	{
		return Collections.unmodifiableSet( selectedFeatureKeys );
	}

	public Collection< FeatureSpec< ?, ? > > getFeatureSpecs( final Class< ? > target )
	{
		final Collection< FeatureSpec< ?, ? > > fs = featureSpecsTargetMap.get( target );
		return null == fs ? null : Collections.unmodifiableCollection( fs );
	}

	public FeatureSpec< ?, ? > getFeatureSpec( final String featureKey )
	{
		return featureSpecsKeys.get( featureKey );
	}

	public void put( final Class< ? > target, final FeatureSpec< ?, ? > spec )
	{
		featureSpecsKeys.put( spec.getKey(), spec );
		featureSpecsTargetMap.computeIfAbsent( target, ( k ) -> new ArrayList<>() ).add( spec );
	}

	@Override
	public String toString()
	{
		return ( super.toString() + " " + selectedFeatureKeys.toString() );
	}


}
