package org.mastodon.feature.util;

import org.mastodon.collection.RefIntMap;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.IntFeatureProjection;

/**
 * Static utilities related to common {@link FeatureProjection}s.
 */
public class FeatureProjections
{

	public static final < T > IntFeatureProjection< T > project( final RefIntMap< T > map )
	{
		return new MyIntFeatureProjection<>( map );
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
