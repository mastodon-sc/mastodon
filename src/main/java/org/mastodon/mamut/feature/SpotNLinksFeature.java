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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.IntFeatureProjection;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.Spot;
import org.scijava.plugin.Plugin;

public class SpotNLinksFeature implements Feature< Spot >
{

	public static final String KEY = "Spot N links";

	private static final String HELP_STRING = "Number of outgoing and incoming links of a spot.";

	public static final FeatureProjectionSpec N_LINKS_PROJECTION_SPEC = new FeatureProjectionSpec( KEY );

	public static final FeatureProjectionSpec N_OUTGOING_LINKS_PROJECTION_SPEC = new FeatureProjectionSpec( "N outgoing links" );

	public static final FeatureProjectionSpec N_INCOMING_LINKS_PROJECTION_SPEC = new FeatureProjectionSpec( "N incoming links" );

	public static final Spec SPEC = new Spec();

	private final IntFeatureProjection< Spot > nLinksProjection;

	private final IntFeatureProjection< Spot > nIncomingLinksProjection;

	private final IntFeatureProjection< Spot > nOutgoingLinksProjection;

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< SpotNLinksFeature, Spot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					SpotNLinksFeature.class,
					Spot.class,
					Multiplicity.SINGLE,
					N_LINKS_PROJECTION_SPEC,
					N_OUTGOING_LINKS_PROJECTION_SPEC,
					N_INCOMING_LINKS_PROJECTION_SPEC );
		}
	}

	public SpotNLinksFeature()
	{
		this.nLinksProjection = new NLinkProjection();
		this.nOutgoingLinksProjection = new NOutgoingLinkProjection();
		this.nIncomingLinksProjection = new NIncomingLinkProjection();
	}

	@Override
	public FeatureProjection< Spot > project( final FeatureProjectionKey key )
	{
		if ( nLinksProjection.getKey().equals( key ) )
			return nLinksProjection;
		else if ( nOutgoingLinksProjection.getKey().equals( key ) )
			return nOutgoingLinksProjection;
		else if ( nIncomingLinksProjection.getKey().equals( key ) )
			return nIncomingLinksProjection;
		else
			return null;
	}

	@Override
	public Set< FeatureProjection< Spot > > projections()
	{
		return Collections.unmodifiableSet( new HashSet<>( Arrays.asList(
				nLinksProjection, nOutgoingLinksProjection, nIncomingLinksProjection ) ) );
	}

	@Override
	public Spec getSpec()
	{
		return SPEC;
	}

	@Override
	public void invalidate( final Spot spot )
	{}

	private static final class NLinkProjection implements IntFeatureProjection< Spot >
	{

		@Override
		public FeatureProjectionKey getKey()
		{
			return FeatureProjectionKey.key( N_LINKS_PROJECTION_SPEC );
		}

		@Override
		public boolean isSet( final Spot obj )
		{
			return true;
		}

		@Override
		public double value( final Spot obj )
		{
			return obj.edges().size();
		}

		@Override
		public String units()
		{
			return Dimension.NONE_UNITS;
		}

	}

	private static final class NOutgoingLinkProjection implements IntFeatureProjection< Spot >
	{

		@Override
		public FeatureProjectionKey getKey()
		{
			return FeatureProjectionKey.key( N_OUTGOING_LINKS_PROJECTION_SPEC );
		}

		@Override
		public boolean isSet( final Spot obj )
		{
			return true;
		}

		@Override
		public double value( final Spot obj )
		{
			return obj.outgoingEdges().size();
		}

		@Override
		public String units()
		{
			return Dimension.NONE_UNITS;
		}
	}

	private static final class NIncomingLinkProjection implements IntFeatureProjection< Spot >
	{

		@Override
		public FeatureProjectionKey getKey()
		{
			return FeatureProjectionKey.key( N_INCOMING_LINKS_PROJECTION_SPEC );
		}

		@Override
		public boolean isSet( final Spot obj )
		{
			return true;
		}

		@Override
		public double value( final Spot obj )
		{
			return obj.incomingEdges().size();
		}

		@Override
		public String units()
		{
			return Dimension.NONE_UNITS;
		}
	}
}
