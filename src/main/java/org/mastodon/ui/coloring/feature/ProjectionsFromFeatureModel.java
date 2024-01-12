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
package org.mastodon.ui.coloring.feature;

import static org.mastodon.feature.FeatureProjectionKey.key;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;

/**
 * Provides mapping from {@link FeatureProjectionId} to
 * {@link FeatureProjection}s declared in a {@link FeatureModel}.
 *
 * @author Tobias Pietzsch
 */
public class ProjectionsFromFeatureModel implements Projections
{
	private final FeatureModel featureModel;

	public ProjectionsFromFeatureModel( final FeatureModel featureModel )
	{
		this.featureModel = featureModel;
	}

	@Override
	public FeatureProjection< ? > getFeatureProjection( final FeatureProjectionId id )
	{
		if ( id == null )
			return null;

		final FeatureSpec< ?, ? > featureSpec = featureModel.getFeatureSpecs().stream()
				.filter( spec -> spec.getKey().equals( id.getFeatureKey() ) )
				.findFirst()
				.orElse( null );
		return getFeatureProjection( id, featureSpec );
	}

	@Override
	public < T > FeatureProjection< T > getFeatureProjection( final FeatureProjectionId id, final Class< T > target )
	{
		if ( id == null )
			return null;

		@SuppressWarnings( "unchecked" )
		final FeatureSpec< ?, T > featureSpec = ( FeatureSpec< ?, T > ) featureModel.getFeatureSpecs().stream()
				.filter( spec -> target.isAssignableFrom( spec.getTargetClass() ) )
				.filter( spec -> spec.getKey().equals( id.getFeatureKey() ) )
				.findFirst()
				.orElse( null );
		return getFeatureProjection( id, featureSpec );
	}

	private < T > FeatureProjection< T > getFeatureProjection( final FeatureProjectionId id,
			final FeatureSpec< ?, T > featureSpec )
	{
		if ( featureSpec == null )
			return null;

		@SuppressWarnings( "unchecked" )
		final Feature< T > feature = ( Feature< T > ) featureModel.getFeature( featureSpec );
		if ( feature == null )
			return null;

		/*
		 * Regen feature spec from the feature we just found. This is to
		 * accomodate features that are computed 'manually', and that might have
		 * a feature spec that changes every-time they are computed.
		 */
		final FeatureSpec< ? extends Feature< T >, T > featureSpec2 = feature.getSpec();
		final FeatureProjectionSpec projectionSpec = featureSpec2.getProjectionSpecs().stream()
				.filter( spec -> spec.getKey().equals( id.getProjectionKey() ) )
				.findFirst()
				.orElse( null );
		if ( projectionSpec == null )
			return null;

		switch ( id.getMultiplicity() )
		{
		default:
		case SINGLE:
			return feature.project( key( projectionSpec ) );
		case ON_SOURCES:
			return feature.project( key( projectionSpec, id.getI0() ) );
		case ON_SOURCE_PAIRS:
			return feature.project( key( projectionSpec, id.getI0(), id.getI1() ) );
		}
	}
}
