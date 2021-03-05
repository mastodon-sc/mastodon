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
package org.mastodon.feature;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.scijava.listeners.Listeners;

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

	private final Map< FeatureSpec< ?, ? >, Feature< ? > > features;

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
	 * Registers the specified feature.
	 * 
	 * @param feature
	 *            the feature to register.
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

	/*
	 *
	 * Listener handling
	 * =======================================================================
	 *
	 */

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
