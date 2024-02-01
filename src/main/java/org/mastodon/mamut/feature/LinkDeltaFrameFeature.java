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
package org.mastodon.mamut.feature;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.Collections;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.scijava.plugin.Plugin;

public class LinkDeltaFrameFeature implements Feature< Link >
{

	private static final String KEY = "Link delta T";

	private static final String HELP_STRING = "The time difference in number of time-points between the target and source of links.";

	private static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY, Dimension.NONE );

	public static final Spec SPEC = new Spec();

	final ModelGraph graph;

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< LinkDeltaFrameFeature, Link >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					LinkDeltaFrameFeature.class,
					Link.class,
					Multiplicity.SINGLE,
					PROJECTION_SPEC );
		}
	}

	public LinkDeltaFrameFeature( final ModelGraph graph )
	{
		this.graph = graph;
	}

	@Override
	public FeatureProjection< Link > project( final FeatureProjectionKey key )
	{
		return key( PROJECTION_SPEC ).equals( key ) ? new MyProjection( graph ) : null;
	}

	@Override
	public Set< FeatureProjection< Link > > projections()
	{
		return Collections.singleton( new MyProjection( graph ) );
	}

	@Override
	public Spec getSpec()
	{
		return SPEC;
	}

	@Override
	public void invalidate( final Link link )
	{}

	private static final class MyProjection implements FeatureProjection< Link >
	{

		private final Spot ref1;

		private final Spot ref2;

		public MyProjection( final ModelGraph graph )
		{
			this.ref1 = graph.vertexRef();
			this.ref2 = graph.vertexRef();
		}

		@Override
		public FeatureProjectionKey getKey()
		{
			return key( PROJECTION_SPEC );
		}

		@Override
		public boolean isSet( final Link link )
		{
			return true;
		}

		@Override
		public synchronized double value( final Link link )
		{
			final Spot source = link.getSource( ref1 );
			final Spot target = link.getTarget( ref2 );
			return target.getTimepoint() - source.getTimepoint();
		}

		@Override
		public String units()
		{
			return Dimension.NONE_UNITS;
		}
	}
}
