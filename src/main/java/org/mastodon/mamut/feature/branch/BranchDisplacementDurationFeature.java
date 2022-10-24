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
package org.mastodon.mamut.feature.branch;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

public class BranchDisplacementDurationFeature implements Feature< BranchSpot >
{
	public static final String KEY = "Branch duration and displacement";

	private static final String INFO_STRING = "The displacement and duration of a branch.";

	public static final FeatureProjectionSpec DISPLACEMENT_PROJECTION_SPEC = new FeatureProjectionSpec( "Displacement", Dimension.LENGTH );

	public static final FeatureProjectionSpec DURATION_PROJECTION_SPEC = new FeatureProjectionSpec( "Duration", Dimension.NONE );

	public static final Spec SPEC = new Spec();

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< BranchDisplacementDurationFeature, BranchSpot >
	{
		public Spec()
		{
			super(
					KEY,
					INFO_STRING,
					BranchDisplacementDurationFeature.class,
					BranchSpot.class,
					Multiplicity.SINGLE,
					DISPLACEMENT_PROJECTION_SPEC,
					DURATION_PROJECTION_SPEC );
		}
	}

	private final Map< FeatureProjectionKey, FeatureProjection< BranchSpot > > projectionMap;

	final DoublePropertyMap< BranchSpot > dispMap;

	final DoublePropertyMap< BranchSpot > durMap;

	final String lengthUnits;

	BranchDisplacementDurationFeature( final DoublePropertyMap< BranchSpot > dispMap, final DoublePropertyMap< BranchSpot > durMap, final String lengthUnits )
	{
		this.dispMap = dispMap;
		this.durMap = durMap;
		this.lengthUnits = lengthUnits;
		this.projectionMap = new LinkedHashMap<>( 2 );
		projectionMap.put( key( DISPLACEMENT_PROJECTION_SPEC ), FeatureProjections.project( key( DISPLACEMENT_PROJECTION_SPEC ), dispMap, lengthUnits ) );
		projectionMap.put( key( DURATION_PROJECTION_SPEC ), FeatureProjections.project( key( DURATION_PROJECTION_SPEC ), durMap, Dimension.NONE_UNITS ) );
	}

	public double getDuration( final BranchSpot branch )
	{
		return durMap.get( branch );
	}

	public double getDisplacement( final BranchSpot branch )
	{
		return dispMap.get( branch );
	}

	@Override
	public FeatureProjection< BranchSpot > project( final FeatureProjectionKey key )
	{
		return projectionMap.get( key );
	}

	@Override
	public Set< FeatureProjection< BranchSpot > > projections()
	{
		return new LinkedHashSet<>( projectionMap.values() );
	}

	@Override
	public Spec getSpec()
	{
		return SPEC;
	}

	@Override
	public void invalidate( final BranchSpot branch )
	{
		dispMap.remove( branch );
		durMap.remove( branch );
	}
}
