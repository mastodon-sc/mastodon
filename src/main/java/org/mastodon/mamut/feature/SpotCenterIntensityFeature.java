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
package org.mastodon.mamut.feature;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

public class SpotCenterIntensityFeature implements Feature< Spot >
{
	public static final String KEY = "Spot center intensity";

	private static final String HELP_STRING =
			"Computes the intensity at the center of spots by taking the mean of pixel intensity "
					+ "weigthted by a gaussian. The gaussian weights are centered int the spot, "
					+ "and have a sigma value equal to the minimal radius of the ellipsoid divided by "
					+ SpotCenterIntensityFeatureComputer.SIGMA_FACTOR + ".";

	public static final FeatureProjectionSpec PROJECTION_SPEC =
			new FeatureProjectionSpec( "Center", Dimension.INTENSITY );

	public static final Spec SPEC = new Spec();

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< SpotCenterIntensityFeature, Spot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					SpotCenterIntensityFeature.class,
					Spot.class,
					Multiplicity.ON_SOURCES,
					PROJECTION_SPEC );
		}
	}

	private final Map< FeatureProjectionKey, FeatureProjection< Spot > > projectionMap;

	final List< DoublePropertyMap< Spot > > maps;

	SpotCenterIntensityFeature( final List< DoublePropertyMap< Spot > > maps )
	{
		this.maps = maps;
		this.projectionMap = new LinkedHashMap<>( 2 * maps.size() );
		for ( int iSource = 0; iSource < maps.size(); iSource++ )
		{
			final FeatureProjectionKey mkey = key( PROJECTION_SPEC, iSource );
			projectionMap.put( mkey, FeatureProjections.project( mkey, maps.get( iSource ), Dimension.COUNTS_UNITS ) );
		}
	}

	public double getCenterIntensity( final Spot spot, final int source )
	{
		return maps.get( source ).getDouble( spot );
	}

	@Override
	public FeatureProjection< Spot > project( final FeatureProjectionKey key )
	{
		return projectionMap.get( key );
	}

	@Override
	public Set< FeatureProjection< Spot > > projections()
	{
		return new LinkedHashSet<>( projectionMap.values() );
	}

	@Override
	public Spec getSpec()
	{
		return SPEC;
	}

	@Override
	public void invalidate( final Spot spot )
	{
		for ( final DoublePropertyMap< Spot > map : maps )
			map.remove( spot );
	}
}
