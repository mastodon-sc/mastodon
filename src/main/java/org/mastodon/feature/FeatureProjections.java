package org.mastodon.feature;

import org.mastodon.collection.RefDoubleMap;
import org.mastodon.collection.RefIntMap;

/**
 * Static utilities related to common {@link FeatureProjection}s.
 */
public class FeatureProjections
{

	public static final < T > IntFeatureProjection< T > project( final RefIntMap< T > map )
	{
		return new MyIntFeatureProjection<>( map );
	}
	
	public static final < T > FeatureProjection< T > project( final RefDoubleMap< T > map )
	{
		return new MyDoubleFeatureProjection<>( map );
	}

	private static final class MyDoubleFeatureProjection< T > implements IntFeatureProjection< T >
	{

		private final RefDoubleMap< T > map;

		public MyDoubleFeatureProjection( final RefDoubleMap< T > map )
		{
			this.map = map;
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
	}

	private static final class MyIntFeatureProjection< T > implements IntFeatureProjection< T >
	{

		private final RefIntMap< T > map;

		public MyIntFeatureProjection( final RefIntMap< T > map )
		{
			this.map = map;
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
	}
}
