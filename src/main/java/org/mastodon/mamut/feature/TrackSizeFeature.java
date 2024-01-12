/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.feature;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.Collections;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.IntFeatureProjection;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.plugin.Plugin;

public class TrackSizeFeature implements Feature< Spot >
{

	public static final String KEY = "Track N spots";

	private static final String INFO_STRING = "Returns the number of spots in a track.";

	private static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY );

	public static final Spec SPEC = new Spec();

	final IntPropertyMap< Spot > map;

	private final IntFeatureProjection< Spot > projection;

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< TrackSizeFeature, Spot >
	{
		public Spec()
		{
			super(
					KEY,
					INFO_STRING,
					TrackSizeFeature.class,
					Spot.class,
					Multiplicity.SINGLE,
					PROJECTION_SPEC );
		}
	}

	TrackSizeFeature( final IntPropertyMap< Spot > map )
	{
		this.map = map;
		this.projection = FeatureProjections.project( key( PROJECTION_SPEC ), map, Dimension.NONE_UNITS );
	}

	public int get( final Spot spot )
	{
		return map.getInt( spot );
	}

	@Override
	public FeatureProjection< Spot > project( final FeatureProjectionKey key )
	{
		return projection.getKey().equals( key ) ? projection : null;
	}

	@Override
	public Set< FeatureProjection< Spot > > projections()
	{
		return Collections.singleton( projection );
	}

	@Override
	public Spec getSpec()
	{
		return SPEC;
	}

	@Override
	public void invalidate( final Spot spot )
	{
		map.remove( spot );
	}
}
