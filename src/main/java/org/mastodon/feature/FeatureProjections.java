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
