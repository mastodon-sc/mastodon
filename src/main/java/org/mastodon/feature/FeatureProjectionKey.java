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
package org.mastodon.feature;

import java.util.Arrays;

/**
 * Links a {@link FeatureProjectionSpec} to source indices (according to the
 * features {@link Multiplicity}).
 * <p>
 * Can be used to {@link Feature#project(FeatureProjectionKey) retrieve} a projection from a {@link Feature}.
 */
public final class FeatureProjectionKey
{
	/**
	 * Construct a {@link FeatureProjectionKey} with
	 * {@link Multiplicity#SINGLE}.
	 * 
	 * @param spec
	 *            the feature projection spec.
	 * @return a new {@link FeatureProjectionKey}.
	 */
	public static FeatureProjectionKey key( final FeatureProjectionSpec spec )
	{
		return new FeatureProjectionKey( spec );
	}

	/**
	 * Constructs a {@link FeatureProjectionKey} with
	 * {@link Multiplicity#ON_SOURCES}.
	 * 
	 * @param spec
	 *            the feature projection spec.
	 * @param i0
	 *            the source index.
	 * @return a new {@link FeatureProjectionKey}.
	 */
	public static FeatureProjectionKey key( final FeatureProjectionSpec spec, final int i0 )
	{
		return new FeatureProjectionKey( spec, i0 );
	}

	/**
	 * Constructs a {@link FeatureProjectionKey} with
	 * {@link Multiplicity#ON_SOURCE_PAIRS}.
	 * 
	 * @param spec
	 *            the feature projection spec.
	 * @param i0
	 *            the first source index.
	 * @param i1
	 *            the second source index.
	 * @return a new {@link FeatureProjectionKey}.
	 */
	public static FeatureProjectionKey key( final FeatureProjectionSpec spec, final int i0, final int i1 )
	{
		return new FeatureProjectionKey( spec, i0, i1 );
	}

	private final FeatureProjectionSpec spec;

	private final int[] sourceIndices;

	private FeatureProjectionKey( final FeatureProjectionSpec spec, final int... sourceIndices )
	{
		this.spec = spec;
		this.sourceIndices = ( sourceIndices == null ) ? new int[ 0 ] : sourceIndices;
	}

	public FeatureProjectionSpec getSpec()
	{
		return spec;
	}

	public int[] getSourceIndices()
	{
		return sourceIndices;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder( spec.getKey() );
		for ( final int sourceIndex : sourceIndices )
		{
			sb.append( " ch" );
			sb.append( sourceIndex + 1 );
		}
		return sb.toString();
	}

	@Override
	public boolean equals( final Object o )
	{
		if ( this == o )
			return true;
		if ( o instanceof FeatureProjectionKey )
		{
			final FeatureProjectionKey key = ( FeatureProjectionKey ) o;
			return spec.equals( key.spec ) && Arrays.equals( sourceIndices, key.sourceIndices );
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		int result = spec.hashCode();
		result = 31 * result + Arrays.hashCode( sourceIndices );
		return result;
	}
}
