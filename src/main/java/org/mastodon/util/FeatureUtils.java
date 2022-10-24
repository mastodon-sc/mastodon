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
package org.mastodon.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpec;

/**
 * Collection of utilities related to manipulating a {@link FeatureModel}.
 */
public class FeatureUtils
{

	/**
	 * Returns the map of feature specs to feature stored in the specified
	 * feature model, for features that are defined on objects of the specified
	 * class.
	 * 
	 * @param <O>
	 *            the type of objects whose features we want to extract.
	 * @param featureModel
	 *            the feature model.
	 * @param clazz
	 *            the class of objects whose features we want to extract.
	 * @return a new map.
	 */
	public static final < O > Map< FeatureSpec< ?, O >, Feature< O > > collectFeatureMap( final FeatureModel featureModel, final Class< O > clazz )
	{
		final Set< FeatureSpec< ?, ? > > featureSpecs = featureModel.getFeatureSpecs().stream()
				.filter( ( fs ) -> fs.getTargetClass().isAssignableFrom( clazz ) )
				.collect( Collectors.toSet() );
		final Map< FeatureSpec< ?, O >, Feature< O > > featureMap = new HashMap<>();
		for ( final FeatureSpec< ?, ? > fs : featureSpecs )
		{
			@SuppressWarnings( "unchecked" )
			final Feature< O > feature = ( Feature< O > ) featureModel.getFeature( fs );
			@SuppressWarnings( "unchecked" )
			final FeatureSpec< ?, O > featureSpec = ( FeatureSpec< ?, O > ) fs;
			featureMap.put( featureSpec, feature );
		}
		return featureMap;
	}

	private FeatureUtils()
	{}

}
