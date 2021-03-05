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
package org.mastodon.feature;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.Collections;
import java.util.Set;

import org.mastodon.RefPool;
import org.mastodon.properties.DoublePropertyMap;

/**
 * Feature made of a double scalar value.
 * <p>
 * They are not connected to a feature computer and are used to wrap a map that
 * stores static values.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <O>
 *            the feature target.
 */
public abstract class DoubleScalarFeature< O > implements Feature< O >
{

	private final FeatureProjection< O > projection;

	final DoublePropertyMap< O > values;

	/**
	 * Creates a new scalar double feature instance.
	 *
	 * @param key
	 *            the feature unique key. Must be unique within the application
	 *            scope.
	 * @param dimension
	 *            the dimension of the quantity of this scalar feature.
	 * @param units
	 *            the projection units.
	 * @param pool
	 *            the pool of objects on which to define the feature.
	 */
	public DoubleScalarFeature( final String key, final Dimension dimension, final String units, final RefPool< O > pool )
	{
		this( key, dimension, units, new DoublePropertyMap<>( pool, Double.NaN ) );
	}

	/**
	 * Only used for deserialization.
	 *
	 * @param key
	 *            the feature unique key. Must be unique within the application
	 *            scope.
	 * @param dimension
	 *            the dimension of the quantity of this scalar feature.
	 * @param units
	 *            the projection units.
	 * @param map
	 *            the values to store in this feature.
	 */
	protected DoubleScalarFeature( final String key, final Dimension dimension, final String units, final DoublePropertyMap< O > map )
	{
		final FeatureProjectionSpec projectionSpec = new FeatureProjectionSpec( key, dimension );
		this.values = map;
		this.projection = FeatureProjections.project( key( projectionSpec ), values, units );
	}

	@Override
	public FeatureProjection< O > project( final FeatureProjectionKey key )
	{
		return projection.getKey().equals( key ) ? projection : null;
	}

	@Override
	public Set< FeatureProjection< O > > projections()
	{
		return Collections.singleton( projection );
	}

	public boolean isSet( final O o )
	{
		return values.isSet( o );
	}

	public double value( final O o )
	{
		return values.getDouble( o );
	}

	public void set( final O o, final double value )
	{
		values.set( o, value );
	}

	@Override
	public void invalidate( final O o )
	{
		values.remove( o );
	}

	/**
	 * Returns the values of the feature as an array of double values. Changes
	 * to the array of values will not be reflected in the feature nor
	 * vice-versa.
	 *
	 * @return the values of the map as an array of double values.
	 */
	public double[] values()
	{
		return values.getMap().values();
	}
}
