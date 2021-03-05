/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.feature.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mastodon.feature.FeatureSpec;
import org.scijava.listeners.Listeners;

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


	public FeatureComputationModel()
	{
		this.updateListeners = new Listeners.SynchronizedList<>();
		this.selectedFeatures = new HashSet<>();
		this.visibleFeatures = new HashMap<>();
		this.featureSpecsTargetMap = new HashMap<>();
		this.featureSpecsKeys = new HashMap<>();
		this.uptodateMap = new HashMap<>();
		this.deps = new HashMap<>();
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
}
