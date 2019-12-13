package org.mastodon.feature.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mastodon.feature.FeatureComputationSettings;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.util.Listeners;

import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;

public class FeatureComputationModel
{
	public interface UpdateListener
	{
		public void settingsChanged();
	}

	private final Listeners.List< UpdateListener > updateListeners;

	private final Set< FeatureSpec< ?, ? > > selectedFeatures;

	private final Map< FeatureSpec< ?, ? >, Boolean > visibleFeatures;

	private final Map< Class< ? >, Collection< FeatureSpec< ?, ? > > > featureSpecsTargetMap;

	private final Map< String, FeatureSpec< ?, ? > > featureSpecsKeys;

	private final Map< FeatureSpec< ?, ? >, Boolean > uptodateMap;

	private final Map< FeatureSpec< ?, ? >, Collection< FeatureSpec< ?, ? > > > deps;

	private final FeatureComputationSettings featureComputationSettings;

	public FeatureComputationModel( final AbstractSequenceDescription< ?, ?, ? > sequenceDescription )
	{
		this.updateListeners = new Listeners.SynchronizedList<>();
		this.selectedFeatures = new HashSet<>();
		this.visibleFeatures = new HashMap<>();
		this.featureSpecsTargetMap = new HashMap<>();
		this.featureSpecsKeys = new HashMap<>();
		this.uptodateMap = new HashMap<>();
		this.deps = new HashMap<>();
		this.featureComputationSettings = new FeatureComputationSettings( sequenceDescription );
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

	public boolean isVisible( final FeatureSpec< ?, ? > spec )
	{
		return visibleFeatures.getOrDefault( spec, Boolean.TRUE ).booleanValue();
	}

	public void setVisible( final FeatureSpec< ?, ? > spec, final boolean visible )
	{
		visibleFeatures.put( spec, Boolean.valueOf( visible ) );
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

	/**
	 * Stores the give specification, with the given target class and
	 * dependencies.
	 *
	 * @param target
	 *            the target class of the the feature .
	 * @param spec
	 *            the specification of the feature.
	 * @param dependencies
	 *            its dependencies.
	 */
	public void put( final Class< ? > target, final FeatureSpec< ?, ? > spec, final Collection< FeatureSpec< ?, ? > > dependencies )
	{
		featureSpecsKeys.put( spec.getKey(), spec );
		featureSpecsTargetMap.computeIfAbsent( target, ( k ) -> new ArrayList<>() ).add( spec );
		deps.put( spec, dependencies );
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
	 * Marks all the specified {@link FeatureSpec}s in this model as up to date.
	 *
	 * @param featureSpecs
	 *            the feature specs to mark as up to date.
	 */
	public void setUptodate( final Set< FeatureSpec< ?, ? > > featureSpecs )
	{
		for ( final FeatureSpec< ?, ? > featureSpec : featureSpecs )
			uptodateMap.put( featureSpec, Boolean.valueOf( true ) );
	}

	public Collection< FeatureSpec< ?, ? > > getDependencies( final FeatureSpec< ?, ? > spec )
	{
		return Collections.unmodifiableCollection( deps.computeIfAbsent( spec, s -> Collections.emptyList() ) );
	}

	public FeatureComputationSettings getFeatureComputationSettings()
	{
		return featureComputationSettings;
	}
}
