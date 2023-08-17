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
package org.mastodon.mamut.io.importer.trackmate;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.properties.IntPropertyMap;

public abstract class TrackMateImportedFeatures< O > implements Feature< O >
{

	private final Map< FeatureProjectionKey, FeatureProjection< O > > projectionMap;

	final Map< FeatureProjectionKey, DoublePropertyMap< O > > doublePropertyMapMap;

	final Map< FeatureProjectionKey, IntPropertyMap< O > > intPropertyMapMap;

	public TrackMateImportedFeatures()
	{
		this.projectionMap = new HashMap<>();
		this.doublePropertyMapMap = new HashMap<>();
		this.intPropertyMapMap = new HashMap<>();
	}

	void store( final String key, final Dimension dimension, final String units, final DoublePropertyMap< O > values )
	{
		final FeatureProjectionSpec spec = new FeatureProjectionSpec( key, dimension );
		final FeatureProjectionKey fpkey = FeatureProjectionKey.key( spec );
		projectionMap.put( fpkey, FeatureProjections.project( fpkey, values, units ) );
		doublePropertyMapMap.put( fpkey, values );
	}

	void store( final String key, final Dimension dimension, final String units, final IntPropertyMap< O > values )
	{
		final FeatureProjectionSpec spec = new FeatureProjectionSpec( key, dimension );
		final FeatureProjectionKey fpkey = FeatureProjectionKey.key( spec );
		projectionMap.put( fpkey, FeatureProjections.project( fpkey, values, units ) );
		intPropertyMapMap.put( fpkey, values );
	}

	@Override
	public FeatureProjection< O > project( final FeatureProjectionKey key )
	{
		return projectionMap.get( key );
	}

	@Override
	public Set< FeatureProjection< O > > projections()
	{
		return new LinkedHashSet<>( projectionMap.values() );
	}

	@Override
	public void invalidate( final O obj )
	{
		for ( final DoublePropertyMap< O > map : doublePropertyMapMap.values() )
			map.remove( obj );

		for ( final IntPropertyMap< O > map : intPropertyMapMap.values() )
			map.remove( obj );
	}
}
