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
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.plugin.Plugin;



/**
 * Represents the number of spots within a branch spot. In many cases this may
 * be equivalent to {@link BranchDisplacementDurationFeature} (duration).
 * However, in situations where there are spots missing within a branch spot, this
 * may well be different.
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
 * <ul>
 * <li>{@code branchSpotA = 3}</li>
 * <li>{@code branchSpotB = 3}</li>
 * <li>{@code branchSpotD = 2}</li>
 * <li>{@code branchSpotD = 3}</li>
 * <li>{@code branchSpotE = 3}</li>
 * </ul>
 *
 *
 * <strong>Model-Graph (i.e. Graph of Spots)</strong>
 *
 * <pre>
 *    Spot( 0, X=1,00, Y=2,00, Z=3,00, tp=0 )
 *                      │
 *                      │
 *   Spot( 1, X=2,00, Y=4,00, Z=6,00, tp=1 )
 *                      │
 *                      │
 *   Spot( 2, X=3,00, Y=6,00, Z=9,00, tp=2 )
 *                      │
 *                      │
 *  Spot( 3, X=4,00, Y=8,00, Z=12,00, tp=3 )
 *                      │
 *                      │
 *  Spot( 4, X=5,00, Y=10,00, Z=15,00, tp=3 )
 * </pre>
 *
 * <strong>Branch-Graph (i.e. Graph of BranchSpots)</strong>
 * <br>
 * <br>
 * branchSpot0
 *
 * <ul>
 * <li>{@code branchSpot0 = 5}</li>
 * </ul>
 *
 * <strong>Model-Graph (i.e. Graph of Spots)</strong>
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
 *                       │                                              Spot( 4, X=5,00, Y=10,00, Z=15,00, tp=4 )
 *                       │                                            ┌─────────────────────┴─────────────────────┐
 *                       │                                            │                                           │
 *  Spot( 13, X=14,00, Y=28,00, Z=42,00, tp=5 )   Spot( 8, X=9,00, Y=18,00, Z=27,00, tp=5 )   Spot( 5, X=6,00, Y=12,00, Z=18,00, tp=5 )
 *                                                                    │                                           │
 *                                                                    │                                           │
 *                                                                    │                       Spot( 6, X=7,00, Y=14,00, Z=21,00, tp=6 )
 *                                                                    │                                           │
 *                                                                    │                                           │
 *                                               Spot( 10, X=11,00, Y=22,00, Z=33,00, tp=7 )  Spot( 7, X=8,00, Y=16,00, Z=24,00, tp=7 )
 * </pre>
 * <strong>Branch-Graph (i.e. Graph of BranchSpots)</strong>
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
 * <ul>
 *  <li>{@code branchSpotA = 3}</li>
 *  <li>{@code branchSpotB = 2}</li>
 *  <li>{@code branchSpotC = 2}</li>
 *  <li>{@code branchSpotD = 2}</li>
 *  <li>{@code branchSpotE = 3}</li>
 * </ul>
 */
public class BranchNSpotsFeature implements Feature< BranchSpot >
{
	public static final String KEY = "Branch N spots";

	private static final String INFO_STRING = "Returns the number of spots in a branch.";

	static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY );

	public static final Spec SPEC = new Spec();

	final IntPropertyMap< BranchSpot > map;

	private final IntFeatureProjection< BranchSpot > projection;

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< BranchNSpotsFeature, BranchSpot >
	{
		public Spec()
		{
			super(
					KEY,
					INFO_STRING,
					BranchNSpotsFeature.class,
					BranchSpot.class,
					Multiplicity.SINGLE,
					PROJECTION_SPEC );
		}
	}

	BranchNSpotsFeature( final IntPropertyMap< BranchSpot > map )
	{
		this.map = map;
		this.projection = FeatureProjections.project( key( PROJECTION_SPEC ), map, Dimension.NONE_UNITS );
	}

	public int get( final BranchSpot branch )
	{
		return map.getInt( branch );
	}

	@Override
	public FeatureProjection< BranchSpot > project( final FeatureProjectionKey key )
	{
		return projection.getKey().equals( key ) ? projection : null;
	}

	@Override
	public Set< FeatureProjection< BranchSpot > > projections()
	{
		return Collections.singleton( projection );
	}

	@Override
	public Spec getSpec()
	{
		return SPEC;
	}

	@Override
	public void invalidate( final BranchSpot spot )
	{
		map.remove( spot );
	}
}
