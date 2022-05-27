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
package org.mastodon.mamut.feature;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.Spot;
import org.scijava.plugin.Plugin;

public class SpotPositionFeature implements Feature< Spot >
{

	private static final String KEY = "Spot position";

	private static final String HELP_STRING = "Exposes the spot center position.";

	private final LinkedHashMap< FeatureProjectionKey, FeatureProjection< Spot > > projections;

	private static final List< FeatureProjectionSpec > PROJECTION_SPECS = new ArrayList<>( 3 );
	static
	{
		for ( int d = 0; d < 3; d++ )
			PROJECTION_SPECS.add( new FeatureProjectionSpec(  "" + ( char ) ( 'X' + d ), Dimension.POSITION ) );
	}

	public static final Spec SPEC = new Spec();

	public SpotPositionFeature( final String units )
	{
		this.projections = new LinkedHashMap<>( 3 );
		for ( int d = 0; d < 3; d++ )
			projections.put(
					FeatureProjectionKey.key( PROJECTION_SPECS.get( d ) ),
					new MyProjection( d, units ) );
	}

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< SpotPositionFeature, Spot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					SpotPositionFeature.class,
					Spot.class,
					Multiplicity.SINGLE,
					PROJECTION_SPECS.toArray( new FeatureProjectionSpec[] {} ) );
		}
	}

	@Override
	public FeatureProjection< Spot > project( final FeatureProjectionKey key )
	{
		return projections.get( key );
	}

	@Override
	public Set< FeatureProjection< Spot > > projections()
	{
		return new LinkedHashSet<>( projections.values() );
	}

	@Override
	public Spec getSpec()
	{
		return SPEC;
	}

	@Override
	public void invalidate( final Spot spot )
	{}

	private static final class MyProjection implements FeatureProjection< Spot >
	{

		private final String units;

		private final int dimension;

		public MyProjection( final int dimension, final String units )
		{
			this.dimension = dimension;
			this.units = units;
		}

		@Override
		public FeatureProjectionKey getKey()
		{
			return key( PROJECTION_SPECS.get( dimension ) );
		}

		@Override
		public boolean isSet( final Spot obj )
		{
			return true;
		}

		@Override
		public double value( final Spot o )
		{
			return o.getDoublePosition( dimension );

		}

		@Override
		public String units()
		{
			return units;
		}
	}
}
