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
package org.mastodon.feature;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.scijava.plugin.SciJavaPlugin;

/**
 * Specification for a feature {@code F}.
 *
 * @param <F>
 *            the concrete feature type.
 * @param <T>
 *            the target type (<i>e.g.</i> Spot).
 */
public abstract class FeatureSpec< F extends Feature< T >, T > implements SciJavaPlugin
{
	private final String key;

	private final HashSet< FeatureProjectionSpec > projectionSpecs;

	private final Class< F > featureClass;

	private final Class< T > targetClass;

	private final String info;

	/**
	 * The feature multiplicity.
	 */
	private final Multiplicity multiplicity;

	protected FeatureSpec(
			final String key,
			final String info,
			final Class< F > featureClass,
			final Class< T > targetClass,
			final Multiplicity multiplicity,
			final FeatureProjectionSpec... projectionSpecs )
	{
		this.key = key;
		this.info = info;
		this.featureClass = featureClass;
		this.targetClass = targetClass;
		this.multiplicity = multiplicity;
		this.projectionSpecs = new HashSet<>( Arrays.asList( projectionSpecs ) );
	}

	/**
	 * Get an info string describing the feature (to be displayed in UI, for
	 * example).
	 *
	 * @return info string.
	 */
	public String getInfo()
	{
		return info;
	}

	public String getKey()
	{
		return key;
	}

	public Set< FeatureProjectionSpec > getProjectionSpecs()
	{
		return projectionSpecs;
	}

	public Class< F > getFeatureClass()
	{
		return featureClass;
	}

	public Class< T > getTargetClass()
	{
		return targetClass;
	}

	public Multiplicity getMultiplicity()
	{
		return multiplicity;
	}

	@Override
	public String toString()
	{
		return "\"" + getKey() + "\" (feature = " + getFeatureClass() + ", target = " + getTargetClass() + ")";
	}

	@Override
	public boolean equals( final Object o )
	{
		if ( !( o instanceof FeatureSpec ) )
			return false;
		final FeatureSpec< ?, ? > that = ( FeatureSpec< ?, ? > ) o;

		// Don't test for feature projection.
		return key.equals( that.key )
				&& featureClass.equals( that.featureClass )
				&& targetClass.equals( that.targetClass );
	}

	@Override
	public int hashCode()
	{
		return key.hashCode();
	}
}
