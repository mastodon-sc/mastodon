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

	private final Set< FeatureSpec< ?, ? > > selectedFeatures;

	private final Map< Class< ? >, Collection< FeatureSpec< ?, ? > > > featureSpecsTargetMap;

	private final Map< String, FeatureSpec< ?, ? > > featureSpecsKeys;

	private final Map< FeatureSpec< ?, ? >, Boolean > uptodateMap;

	public FeatureComputationModel()
	{
		this.updateListeners = new Listeners.SynchronizedList<>();
		this.selectedFeatures = new HashSet<>();
		this.featureSpecsTargetMap = new HashMap<>();
		this.featureSpecsKeys = new HashMap<>();
		this.uptodateMap = new HashMap<>();
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

	public boolean isSelected( final FeatureSpec< ?, ? > spec )
	{
		return selectedFeatures.contains( spec );
	}

	public void setSelected( final FeatureSpec< ?, ? > spec, final boolean selected )
	{
		final boolean changed = selected
				? selectedFeatures.add( spec )
				: selectedFeatures.remove( spec );
		if ( changed )
			notifyListeners();
	}

	public Set< FeatureSpec< ?, ? > > getSelectedFeatureKeys()
	{
		return Collections.unmodifiableSet( selectedFeatures );
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

	public boolean isUptodate( final FeatureSpec< ?, ? > featureSpec )
	{
		return uptodateMap.computeIfAbsent( featureSpec, fs -> Boolean.valueOf( false ) );
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " " + selectedFeatures.toString();
	}

	/**
	 * Marks all the {@link FeatureSpec}s of this model as out of date.
	 */
	public void setOutofdate()
	{
		for ( final FeatureSpec< ?, ? > featureSpec : featureSpecsKeys.values() )
			uptodateMap.put( featureSpec, Boolean.valueOf( false ) );
	}

	/**
	 * Marks all the {@link FeatureSpec}s <b>currently selected</b> in this
	 * model as up to date.
	 */
	public void setUptodate()
	{
		for ( final FeatureSpec< ?, ? > featureSpec : selectedFeatures )
			uptodateMap.put( featureSpec, Boolean.valueOf( true ) );
	}

}
