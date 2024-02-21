/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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



/**
 * Represents the duration of a branch in the lineage tree, which in many cases
 * may be considered a proxy for the cell life cycle time and the displacement
 * of the cell between two consecutive divisions.
 * <br>
 * <br>
 * It is computed as the difference in timepoints between the last timepoint of
 * the branchspot and the last timepoint of the previous branchspot in the
 * lineage tree.
 * <br>
 * <br>
 * For roots, it is computed as the difference in timepoints between the last
 * timepoint of the branchspot and the first timepoint the previous branchspot
 * in the lineage tree.
 * <br>
 * <br>
 * Cf. following example:
 * <br>
 * <br>
 * <strong>Model-Graph (i.e. Graph of Spots)</strong>
 *
 * <pre>
 *                                                Spot( 0, X=1,00, Y=2,00, Z=3,00, tp=0 )
 *                                                                   │
 *                                                                   │
 *                                                Spot( 1, X=2,00, Y=4,00, Z=6,00, tp=1 )
 *                                                                   │
 *                                                                   │
 *                                                Spot( 2, X=3,00, Y=6,00, Z=9,00, tp=2 )
 *                       ┌───────────────────────────────────────────┴──────────────────────┐
 *                       │                                                                  │
 *  Spot( 11, X=12,00, Y=24,00, Z=36,00, tp=3 )                         Spot( 3, X=4,00, Y=8,00, Z=12,00, tp=3 )
 *                       │                                                                  │
 *                       │                                                                  │
 *  Spot( 12, X=13,00, Y=26,00, Z=39,00, tp=4 )                         Spot( 4, X=5,00, Y=10,00, Z=15,00, tp=4 )
 *                       │                                            ┌─────────────────────┴─────────────────────┐
 *                       │                                            │                                           │
 *  Spot( 13, X=14,00, Y=28,00, Z=42,00, tp=5 )   Spot( 8, X=9,00, Y=18,00, Z=27,00, tp=5 )   Spot( 5, X=6,00, Y=12,00, Z=18,00, tp=5 )
 *                                                                    │                                           │
 *                                                                    │                                           │
 *                                               Spot( 9, X=10,00, Y=20,00, Z=30,00, tp=6 )   Spot( 6, X=7,00, Y=14,00, Z=21,00, tp=6 )
 *                                                                    │                                           │
 *                                                                    │                                           │
 *                                               Spot( 10, X=11,00, Y=22,00, Z=33,00, tp=7 )  Spot( 7, X=8,00, Y=16,00, Z=24,00, tp=7 )
 * </pre>
 *
 * <strong>Branch-Graph (i.e. Graph of BranchSpots)</strong>
 *
 * <pre>
 *                        branchSpotA
 * 	       ┌──────────────┴─────────────────┐
 * 	       │                                │
 * 	   branchSpotB                      branchSpotC
 * 	                                 ┌──────┴───────┐
 * 	                                 │              │
 *                                  branchSpotD    branchSpotE
 * </pre>
 *
 * <strong>Duration</strong>
 * <ul>
 * <li>{@code branchSpotA = 2}</li>
 * <li>{@code branchSpotB = 2}</li>
 * <li>{@code branchSpotC = 3}</li>
 * <li>{@code branchSpotD = 3}</li>
 * <li>{@code branchSpotE = 3}</li>
 * </ul>
 *
 * <strong>Displacement</strong>
 * <ul>
 * <li>{@code branchSpot0 = Math.sqrt( 4+16+36 )}</li>
 * <li>{@code branchSpot1 = Math.sqrt( 4+16+36 )}</li>
 * <li>{@code branchSpot2 = Math.sqrt( 121+484+1089 )}</li>
 * <li>{@code branchSpot3 = Math.sqrt( 36+144+324 )}</li>
 * <li>{@code branchSpot4 = Math.sqrt( 9+36+81 )}</li>
 * </ul>
 *
 *
 * <strong>Spot-Graph</strong>
 *
 * <pre>
 *    Spot( 0, X=1,00, Y=2,00, Z=3,00, tp=0 )
 *                      │
 *                      │
 *    Spot( 1, X=2,00, Y=4,00, Z=6,00, tp=1 )
 *                      │
 *                      │
 *    Spot( 2, X=3,00, Y=6,00, Z=9,00, tp=2 )
 *                      │
 *                      │
 *    Spot( 3, X=4,00, Y=8,00, Z=12,00, tp=3 )
 *                      │
 *                      │
 *    Spot( 4, X=5,00, Y=10,00, Z=15,00, tp=3 )
 * </pre>
 *
 * <strong>BranchSpot-Graph</strong>
 * <br>
 * <br>
 * branchSpot0
 * <br>
 * <br>
 * <strong>Duration</strong>
 * <ul>
 * <li>{@code branchSpot0 = 4}</li>
 * </ul>
 *
 * <strong>Displacement</strong>
 * <ul>
 * <li>{@code branchSpot0 = Math.sqrt(16+64+144)}</li>
 * </ul>
 */
public class BranchDisplacementDurationFeature implements Feature< BranchSpot >
{
	public static final String KEY = "Branch duration and displacement";

	private static final String INFO_STRING = "The displacement and duration of a branch.";

	public static final FeatureProjectionSpec DISPLACEMENT_PROJECTION_SPEC =
			new FeatureProjectionSpec( "Displacement", Dimension.LENGTH );

	public static final FeatureProjectionSpec DURATION_PROJECTION_SPEC =
			new FeatureProjectionSpec( "Duration", Dimension.NONE );

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

	BranchDisplacementDurationFeature( final DoublePropertyMap< BranchSpot > dispMap,
			final DoublePropertyMap< BranchSpot > durMap, final String lengthUnits )
	{
		this.dispMap = dispMap;
		this.durMap = durMap;
		this.lengthUnits = lengthUnits;
		this.projectionMap = new LinkedHashMap<>( 2 );
		projectionMap.put( key( DISPLACEMENT_PROJECTION_SPEC ),
				FeatureProjections.project( key( DISPLACEMENT_PROJECTION_SPEC ), dispMap, lengthUnits ) );
		projectionMap.put( key( DURATION_PROJECTION_SPEC ),
				FeatureProjections.project( key( DURATION_PROJECTION_SPEC ), durMap, Dimension.NONE_UNITS ) );
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
