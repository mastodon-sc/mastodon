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
package org.mastodon.views.grapher.display;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;

/**
 * Data class representing a selection of a feature plus a projection in said
 * feature, plus possibly a selection of the source indices in case the
 * multiplicity is not SINGLE.
 * 
 * @author Jean-Yves Tinevez
 */
public class FeatureSpecPair implements Comparable< FeatureSpecPair >
{

	final FeatureSpec< ?, ? > featureSpec;

	final FeatureProjectionSpec projectionSpec;

	private final int c1;

	private final int c2;

	/**
	 * <code>true</code> if this spec pair is created for an edge feature,
	 * <code>false</code> for vertex feature.
	 */
	private final boolean isEdgeFeature;

	/**
	 * <code>true</code> if this spec pair is for an edge feature, and its value
	 * will be read from the incoming edge of a vertex. If <code>false</code> it
	 * will be read from the outgoing edge of a vertex.
	 */
	private final boolean incoming;

	public FeatureSpecPair( final FeatureSpec< ?, ? > f, final FeatureProjectionSpec ps, final boolean isEdgeFeature,
			final boolean incoming )
	{
		assert f.getMultiplicity() == Multiplicity.SINGLE;
		this.featureSpec = f;
		this.projectionSpec = ps;
		this.c1 = -1;
		this.c2 = -1;
		this.isEdgeFeature = isEdgeFeature;
		this.incoming = incoming;
	}

	public FeatureSpecPair( final FeatureSpec< ?, ? > f, final FeatureProjectionSpec ps, final int c1,
			final boolean isEdgeFeature, final boolean incoming )
	{
		assert f.getMultiplicity() == Multiplicity.ON_SOURCES;
		this.featureSpec = f;
		this.projectionSpec = ps;
		this.c1 = c1;
		this.c2 = -1;
		this.isEdgeFeature = isEdgeFeature;
		this.incoming = incoming;
	}

	public FeatureSpecPair( final FeatureSpec< ?, ? > f, final FeatureProjectionSpec ps, final int c1, final int c2,
			final boolean isEdgeFeature, final boolean incoming )
	{
		assert f.getMultiplicity() == Multiplicity.ON_SOURCE_PAIRS;
		this.featureSpec = f;
		this.projectionSpec = ps;
		this.c1 = c1;
		this.c2 = c2;
		this.isEdgeFeature = isEdgeFeature;
		this.incoming = incoming;
	}

	public boolean isEdgeFeature()
	{
		return isEdgeFeature;
	}

	public boolean isIncomingEdge()
	{
		return incoming;
	}

	@Override
	public int compareTo( final FeatureSpecPair o )
	{
		if ( featureSpec == null || projectionSpec == null )
			return 1;

		final int c3 = Boolean.compare( isEdgeFeature, o.isEdgeFeature );
		if ( c3 != 0 )
			return c3;

		final int c1 = featureSpec.getKey().compareTo( o.featureSpec.getKey() );
		if ( c1 != 0 )
			return c1;

		final int c2 = projectionKey().toString().compareTo( o.projectionKey().toString() );
		if ( c2 != 0 )
			return c2;

		return Boolean.compare( incoming, o.incoming );
	}

	@Override
	public boolean equals( final Object obj )
	{
		return ( obj instanceof FeatureSpecPair ) && compareTo( ( FeatureSpecPair ) obj ) == 0;
	}

	@Override
	public String toString()
	{
		if ( featureSpec == null || projectionSpec == null )
			return "";

		final int size = featureSpec.getProjectionSpecs().size();
		if ( featureSpec.getMultiplicity().equals( Multiplicity.SINGLE ) && size == 1 )
			return featureSpec.getKey() + ( isEdgeFeature
					? incoming ? " - incoming" : " - outgoing"
					: "" );

		return featureSpec.getKey() + " - " + projectionKey() + ( isEdgeFeature
				? incoming ? " - incoming" : " - outgoing"
				: "" );
	}

	public FeatureProjectionKey projectionKey()
	{
		final Multiplicity multiplicity = featureSpec.getMultiplicity();
		final FeatureProjectionKey key;
		switch ( multiplicity )
		{
		case ON_SOURCES:
			key = FeatureProjectionKey.key( projectionSpec, c1 );
			break;
		case ON_SOURCE_PAIRS:
			key = FeatureProjectionKey.key( projectionSpec, c1, c2 );
			break;
		case SINGLE:
		default:
			key = FeatureProjectionKey.key( projectionSpec );
			break;
		}
		return key;
	}

	/**
	 * Returns the feature projection found in the specified feature model, and
	 * that matches the specifications of this instance. If the projection
	 * cannot be found in the feature model, returns <code>null</code>.
	 * 
	 * @param <O>
	 *            the type of objects the projection is defined on.
	 * @param featureModel
	 *            the feature model.
	 * @return the feature projection or <code>null</code>.
	 */
	public < O > FeatureProjection< O > getProjection( final FeatureModel featureModel )
	{
		final Feature< ? > feature = featureModel.getFeature( featureSpec );
		if ( null == feature )
			return null;

		final FeatureProjectionKey key = projectionKey();
		@SuppressWarnings( "unchecked" )
		final FeatureProjection< O > projection = ( FeatureProjection< O > ) feature.project( key );
		return projection;
	}

	/**
	 * Returns the key of the feature spec this instance is specifying.
	 * @return the key of the feature spec.
	 */
	public String getFeatureSpecKey()
	{
		return featureSpec.getKey();
	}
}
