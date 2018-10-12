package org.mastodon.feature;

import org.mastodon.collection.RefDoubleMap;
import org.mastodon.collection.RefIntMap;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.properties.IntPropertyMap;

/**
 * Static utilities related to common {@link FeatureProjection}s.
 */
public class FeatureProjections
{

	public static final < T > IntFeatureProjection< T > project( final RefIntMap< T > map, final String units )
	{
		return new MyIntFeatureProjection<>( map, units );
	}

	public static final < T > FeatureProjection< T > project( final RefDoubleMap< T > map, final String units )
	{
		return new MyDoubleFeatureProjection<>( map, units );
	}

	public static final < T > FeatureProjection< T > project( final DoublePropertyMap< T > map, final String units )
	{
		return new MyDoublePropertyProjection<>( map, units );
	}

	public static final < T > FeatureProjection< T > project( final IntPropertyMap< T > map, final String units )
	{
		return new MyIntPropertyProjection<>( map, units );
	}

	private static final class MyIntPropertyProjection< T > implements IntFeatureProjection< T >
	{

		private final IntPropertyMap< T > map;

		private final String units;

		public MyIntPropertyProjection( final IntPropertyMap< T > map, final String units )
		{
			this.map = map;
			this.units = units;
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

		private final DoublePropertyMap< T > map;

		private final String units;

		public MyDoublePropertyProjection( final DoublePropertyMap< T > map, final String units )
		{
			this.map = map;
			this.units = units;
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

	private static final class MyDoubleFeatureProjection< T > implements FeatureProjection< T >
	{

		private final RefDoubleMap< T > map;

		private final String units;

		public MyDoubleFeatureProjection( final RefDoubleMap< T > map, final String units )
		{
			this.map = map;
			this.units = units;
		}

		@Override
		public boolean isSet( final T obj )
		{
			return map.containsKey( obj );
		}

		@Override
		public double value( final T obj )
		{
			return map.get( obj );
		}

		@Override
		public String units()
		{
			return units;
		}
	}

	private static final class MyIntFeatureProjection< T > implements IntFeatureProjection< T >
	{

		private final RefIntMap< T > map;

		private final String units;

		public MyIntFeatureProjection( final RefIntMap< T > map, final String units )
		{
			this.map = map;
			this.units = units;
		}

		@Override
		public boolean isSet( final T obj )
		{
			return map.containsKey( obj );
		}

		@Override
		public double value( final T obj )
		{
			return map.get( obj );
		}

		@Override
		public String units()
		{
			return units;
		}
	}
}