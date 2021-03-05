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

import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.properties.IntPropertyMap;

/**
 * Static utilities related to common {@link FeatureProjection}s.
 */
public class FeatureProjections
{
	public static final < T > IntFeatureProjection< T > project( final FeatureProjectionKey key, final IntPropertyMap< T > map, final String units )
	{
		return new MyIntPropertyProjection<>( key, map, units );
	}

	public static final < T > FeatureProjection< T > project( final FeatureProjectionKey key, final DoublePropertyMap< T > map, final String units )
	{
		return new MyDoublePropertyProjection<>( key, map, units );
	}

	private static final class MyIntPropertyProjection< T > implements IntFeatureProjection< T >
	{

		private final FeatureProjectionKey key;

		private final IntPropertyMap< T > map;

		private final String units;

		public MyIntPropertyProjection( final FeatureProjectionKey key, final IntPropertyMap< T > map, final String units )
		{
			this.key = key;
			this.map = map;
			this.units = units;
		}

		@Override
		public FeatureProjectionKey getKey()
		{
			return key;
		}

		@Override
		public boolean isSet( final T obj )
		{
			return map.isSet( obj );
		}

		@Override
		public double value( final T obj )
		{
			return map.getInt( obj );
		}

		@Override
		public String units()
		{
			return units;
		}
	}

	private static final class MyDoublePropertyProjection< T > implements FeatureProjection< T >
	{

		private final FeatureProjectionKey key;

		private final DoublePropertyMap< T > map;

		private final String units;

		public MyDoublePropertyProjection( final FeatureProjectionKey key, final DoublePropertyMap< T > map, final String units )
		{
			this.key = key;
			this.map = map;
			this.units = units;
		}

		@Override
		public FeatureProjectionKey getKey()
		{
			return key;
		}

		@Override
		public boolean isSet( final T obj )
		{
			return map.isSet( obj );
		}

		@Override
		public double value( final T obj )
		{
			return map.getDouble( obj );
		}

		@Override
		public String units()
		{
			return units;
		}
	}
}
