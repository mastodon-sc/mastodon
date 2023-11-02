/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.app.plugin.PluginUtils;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.SciJavaService;

/**
 * Helper class to discover and hold {@link FeatureSpec}s.
 */
@Plugin( type = FeatureSpecsService.class )
public class FeatureSpecsService extends AbstractService implements SciJavaService
{

	// NB: The {@link PluginService} needs to be a {@link Parameter} so that
	// it is initialized before the {@link FeatureSpecsService}.
	@Parameter
	private PluginService pluginService;

	private final List< FeatureSpec< ?, ? > > specs = new ArrayList<>();

	private final Map< Class< ? >, List< FeatureSpec< ?, ? > > > targetToSpecs = new HashMap<>();

	private final Map< String, FeatureSpec< ?, ? > > keyToSpec = new HashMap<>();

	private final Map< Class< ? extends Feature< ? > >, FeatureSpec< ?, ? > > featureClassToSpec = new HashMap<>();

	@Override
	public void initialize()
	{
		clear();
		discover();
	}

	private void clear()
	{
		specs.clear();
		targetToSpecs.clear();
		keyToSpec.clear();
		featureClassToSpec.clear();
	}

	/**
	 * Add all {@link FeatureSpec}s that are found by the {@code PluginService}
	 * to the index.
	 */
	private void discover()
	{
		PluginUtils.forEachDiscoveredPlugin( FeatureSpec.class, this::add, pluginService.getContext() );
	}

	/**
	 * Add a {@link FeatureSpec} to the index.
	 *
	 * @throws IllegalArgumentException
	 *             if a {@link FeatureSpec} with the same name as {@code spec}
	 *             is already present
	 */
	private void add( final FeatureSpec< ?, ? > spec ) throws IllegalArgumentException
	{
		if ( keyToSpec.containsKey( spec.getKey() ) )
			throw new IllegalArgumentException(
					"Trying to add " + spec + ". A feature with that name is already present." );
		specs.add( spec );
		targetToSpecs.computeIfAbsent( spec.getTargetClass(), k -> new ArrayList<>() ).add( spec );
		keyToSpec.put( spec.getKey(), spec );
		featureClassToSpec.put( spec.getFeatureClass(), spec );
	}

	/**
	 * Returns the specification of the feature with the specified key. Returns
	 * <code>null</code> if a feature with the specified key is not known to
	 * this service.
	 *
	 * @param key
	 *            the feature key.
	 * @return the feature specification.
	 */
	public FeatureSpec< ?, ? > getSpec( final String key )
	{
		return keyToSpec.get( key );
	}

	/**
	 * Returns the specification of the feature with the specified class.
	 * Returns <code>null</code> if a feature with the specified class is not
	 * known to this service.
	 *
	 * @param featureClass
	 *            the feature class.
	 * @return the feature specification.
	 */
	public FeatureSpec< ?, ? > getSpec( final Class< ? extends Feature< ? > > featureClass )
	{
		return featureClassToSpec.get( featureClass );
	}

	/**
	 * Returns a list of feature specifications for features that operate on the
	 * specified target.
	 * 
	 * @param <T>
	 *            the type of the target.
	 * @param target
	 *            the target features returned operate on.
	 * @return a new unmodifiable list, never <code>null</code>.
	 */
	@SuppressWarnings( "unchecked" )
	public < T > List< FeatureSpec< ?, T > > getSpecs( final Class< T > target )
	{
		final List< ? > list = targetToSpecs.get( target );
		return list == null
				? Collections.emptyList()
				: Collections.unmodifiableList( ( List< FeatureSpec< ?, T > > ) list );
	}
}
